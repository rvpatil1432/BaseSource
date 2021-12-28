/********************************************************
	Title : FreightChargeIC(DI3LSUN002)
	Date  : 10/03/14
	Developer: Mahendra Jadhav

********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
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
public class FreightChargeIC extends ValidatorEJB implements FreightChargeICLocal,FreightChargeICRemote{
	
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

		//method for validation
		public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
		{
			String errString = "";
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			
			try
			{
				if (xmlString != null && xmlString.trim().length() > 0) 
				{
					dom = parseString(xmlString);
				}
				if (xmlString1 != null && xmlString1.trim().length() > 0)
				{
					dom1 = parseString(xmlString1);
				}
				if (xmlString2 != null && xmlString2.trim().length() > 0)
				{
					dom2 = parseString(xmlString2);
				}
				errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			} catch (Exception e)
			{
				System.out.println("Exception : [FlatBookingIC][wfValData( String, String )] :==>\n" + e.getMessage());
				throw new ITMException(e);
			}
			return (errString);
		}
		
		
		public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
		{
			
			String frtChgstable = "",loadType="",chargeCode="",chargeMode="",addCharges="" ;
			double amount=0.0;
			int sequence= 0;
			int sequence_o=0;
			int currentFormNo = 0;
			int childNodeListLength;
			int ctr = 0;
			int cnt=0;
			String childNodeName = null;
			String errString = "";
			String errCode = "";
			String userId = "";
			String sql = "" ,sql1="";
			String errorType = "";
			NodeList parentNodeList = null;
			NodeList childNodeList = null;
			Node parentNode = null;
			Node childNode = null;
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();

			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			ConnDriver connDriver = new ConnDriver();
			DistCommon distCommon = new DistCommon();
			
			StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
			System.out.println("Enter wfValData method  !!");
			
			try
			{
				
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				if (objContext != null && objContext.trim().length() > 0)
				{
					currentFormNo = Integer.parseInt(objContext);
					
				}
				
				
				switch (currentFormNo)
				{
				    case 1:
				    					    	
				       	parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for (ctr = 0; ctr < childNodeListLength; ctr++)
						{
								childNode = childNodeList.item(ctr);
								childNodeName = childNode.getNodeName();
								System.out.println("childNodeName : "+childNodeName);
								if(childNodeName.equalsIgnoreCase("frt_chgs_table"))
								{
											frtChgstable = checkNull(genericUtility.getColumnValue("frt_chgs_table",dom));
											frtChgstable = frtChgstable==null?null:frtChgstable.trim();	
											System.out.println("frtChgstable :"+frtChgstable);
											
											if (frtChgstable == null || frtChgstable.trim().length() == 0)
											{
												errCode = "VMFCTEMT";//freight charges table can not be blank
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
												System.out.println("freight charges table can not be blank!!!");
												
											} 
																					 											
								}
								else if(childNodeName.equalsIgnoreCase("load_type"))
								{
											loadType = checkNull(genericUtility.getColumnValue("load_type",dom));
											loadType = loadType==null?null:loadType.trim();	
											System.out.println("loadType :"+loadType);
																					
											if (loadType == null || loadType.trim().length() == 0)
											{
												errCode = "VMLTEMT";//load type can not be blank
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
												System.out.println("load type can not be blank!!!");
												//break;
											} 
											
								}
								else if(childNodeName.equalsIgnoreCase("charge_code"))
								{
											chargeCode = checkNull(genericUtility.getColumnValue("charge_code",dom));
											chargeCode = chargeCode==null?null:chargeCode.trim();	
											System.out.println("chargeCode :"+chargeCode);
											
											if (chargeCode == null || chargeCode.trim().length() == 0)
											{
												errCode = "VMCHCOEMT";//Charge code can not be blank
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
												System.out.println("Charge code can not be blank!");
												//break;
											} 
											System.out.println("editFlag !! "+editFlag);
											
											if(editFlag.equalsIgnoreCase("A"))
											{
											sql = "select count(1) from freight_charges_table "
													  +" where frt_chgs_table =? and load_type = ? and charge_code =?";
											    pstmt = conn.prepareStatement(sql);
											    pstmt.setString(1, frtChgstable);
											    pstmt.setString(2, loadType);
											    pstmt.setString(3, chargeCode);
											    rs = pstmt.executeQuery();	    
											    if (rs.next()) 
											    {
											    	cnt = rs.getInt(1);
											    }
											    pstmt.close();
											    rs.close();
											    pstmt = null;
											    rs = null;
											    
											    if(cnt > 0)
											    {
											    	errCode = "VMFCTEX";//Freight charges table,load type,charge code already exist!!!
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
													System.out.println("Freight charges table,load type,charge code already exist!!!");
													//break;
											    }
											    
											} 
											
								}
								else if(childNodeName.equalsIgnoreCase("charges_mode"))
								{
									chargeMode = checkNull(genericUtility.getColumnValue("charges_mode",dom));
									chargeMode = chargeMode==null?null:chargeMode.trim();	
									System.out.println("chargeMode :"+chargeMode);
									
									if (chargeMode == null || chargeMode.trim().length() == 0)
									{
										errCode = "VMCHMDEMT";//charge Mode can not be blank
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("charge Mode can not be blank!!");
										//break;
									} 
									
								}  
								else if(childNodeName.equalsIgnoreCase("amount"))
								{
									amount = Double.parseDouble(genericUtility.getColumnValue("amount",dom));
																		
									if (amount <= 0)
									{
										errCode = "VMAMTEMT";//amount can not be zero value.
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("amount can not be zero value.!!");
										//break;
									} 
									
								}
								else if(childNodeName.equalsIgnoreCase("CHARGE_CODE__ADD"))
								{
									addCharges = genericUtility.getColumnValue("charge_code__add",dom);
																		
									if (addCharges == null || addCharges.trim().length() == 0)
									{
										errCode = "VMADCHEMT";//add charges can not be blank.
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("add charges can not be blank");
										//break;
									} 
									
								}
								else if(childNodeName.equalsIgnoreCase("sequence"))
								{
									sequence = Integer.parseInt(genericUtility.getColumnValue("sequence",dom));
									System.out.println("sequence :"+sequence);								
									if (sequence == 0.0 || sequence == 0)
									{
										errCode = "VMSEQEMT";//sequence can not be zero value.
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("sequence can not be zero value.!!");
										//break;
									} 
									else
									{
										System.out.println("mahendra testing : frtChgstable "+frtChgstable);
										System.out.println("mahendra testing : loadType "+loadType);
										System.out.println("mahendra testing : chargeCode "+chargeCode);
										
										  sql = "select sequence from freight_charges_table "
												  +" where frt_chgs_table =? and load_type = ? and charge_code = ?";
										    pstmt = conn.prepareStatement(sql);
										    pstmt.setString(1, frtChgstable);
										    pstmt.setString(2, loadType);
										    pstmt.setString(3, chargeCode);
										    rs = pstmt.executeQuery();	    
										    if (rs.next())
										    {
										    	sequence_o = rs.getInt(1);
										    }
										    pstmt.close();
										    rs.close();
										    pstmt = null;
										    rs = null;
										
										    
										  if(sequence_o == 0)
										  {
											  sequence_o=sequence;
										  }
										  else if(sequence_o != sequence)
										  {
											  sql = "select count(1) from freight_charges_table "
													  +" where frt_chgs_table =? and load_type = ? and sequence =?";
											    pstmt = conn.prepareStatement(sql);
											    pstmt.setString(1, frtChgstable);
											    pstmt.setString(2, loadType);
											    pstmt.setInt(3, sequence);
											   // pstmt.setInt(4, sequence);
											    rs = pstmt.executeQuery();	    
											    if (rs.next()) {
											    	cnt = rs.getInt(1);
											    }
											    pstmt.close();
											    rs.close();
											    pstmt = null;
											    rs = null;
											    
											    if(cnt > 0)
											    {
											    	errCode = "VMSEQ1";//sequence count greater than zero!
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
													System.out.println("sequence count greater than zero!!");
													
											    }
											  
										  }
										
										
										
									}
									
								}
								
						}//end of for loop
						break;
				    case 2:
				    	break;
			}//end of switch
				
				int errListSize = errList.size();
				int count = 0;
				String errFldName = null;
				if (errList != null && errListSize > 0)
				{
					for (count = 0; count < errListSize; count++)
					{
						errCode = errList.get(count);
						errFldName = errFields.get(count);
						errString = getErrorString(errFldName, errCode, userId);
						errorType = errorType(conn, errCode);
						if (errString.length() > 0)
						{
							String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
							bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
							errStringXml.append(bifurErrString);
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
					errStringXml.append("</Errors> </Root> \r\n");
				} else
				{
					errStringXml = new StringBuffer("");
				}
				
				
				
			}//end of try
			catch (Exception e)
			{
				e.printStackTrace();
				errString = e.getMessage();
				throw new ITMException(e);
			} finally
			{
				try
				{
					if (conn != null)
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
						conn.close();
					}
					conn = null;
				} catch (Exception d)
				{
					d.printStackTrace();
					throw new ITMException(d);
				}
			}
			errString = errStringXml.toString();
			System.out.println("mahendra testing : final errString : "+errString);
			return errString;
			
		}//end of validation method
		
		
		
		        // method for item change
				public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
				{
					Document dom = null;
					Document dom1 = null;
					Document dom2 = null;
					String valueXmlString = "";
				
					try
					{
						if (xmlString != null && xmlString.trim().length() > 0)
						{
							dom = parseString(xmlString);
						}
						if (xmlString1 != null && xmlString1.trim().length() > 0)
						{
							dom1 = parseString(xmlString1);
						}
						if (xmlString2 != null && xmlString2.trim().length() > 0)
						{
							dom2 = parseString(xmlString2);
						}
						valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
					} catch (Exception e)
					{
						System.out.println("Exception : [FlatBookingIC][itemChanged( String, String )] :==>\n" + e.getMessage());
						throw new ITMException(e);
					}
					return valueXmlString;
				}

				
				
				public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
				{
					
					Connection conn = null;
					String sql="";
					PreparedStatement pstmt = null, pstmt1 = null;
					ResultSet rs = null, rs1 = null;
					
					String chargeCode="",loadType = "";
					String loadDescr="" , chargeDescr="" ;
					int currentFormNo = 0;
					int childNodeListLength = 0;
					ArrayList convAr = null;
					NodeList parentNodeList = null;
					NodeList childNodeList = null;
					Node parentNode = null;
					Node childNode = null;
					String childNodeName = null;
					StringBuffer valueXmlString = new StringBuffer();
					List amtList =  new ArrayList();
					//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					//GenericUtility genericUtility = GenericUtility.getInstance();
					ConnDriver connDriver = new ConnDriver();
					Date currentDate = new Date();
					
					try
					{
						System.out.println("Enter itemChanged method ");
						SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
						conn.setAutoCommit(false);
						connDriver = null;
						if (objContext != null && objContext.trim().length() > 0)
						{
							currentFormNo = Integer.parseInt(objContext);
							
						}
						valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> ");
						
						
						switch (currentFormNo)
						{
						
							case 1:
								
								parentNodeList = dom.getElementsByTagName("Detail1");
								parentNode = parentNodeList.item(0);
								childNodeList = parentNode.getChildNodes();
								valueXmlString.append("<Detail1>");
								childNodeListLength = childNodeList.getLength();
								
								if(currentColumn.trim().equalsIgnoreCase("load_type"))
								{
									loadType = checkNull(genericUtility.getColumnValue("load_type", dom));
									loadType = loadType==null?null:loadType.trim();	
									System.out.println("loadType :"+loadType);
									System.out.println("mahendra itemchanged case 1 chargeCode : "+chargeCode);
									if (loadType != null && loadType.trim().length() > 0)
									{
										
										sql = "select descr from gencodes where fld_name='LOAD_TYPE' and fld_value=? and mod_name='X'";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, loadType);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
										    loadDescr = rs.getString(1);
										}
										
										loadDescr = loadDescr==null?"":loadDescr.trim();
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										valueXmlString.append("<gencodes_descr>").append("<![CDATA[" + loadDescr + "]]>").append("</gencodes_descr>");
									 }
									
									
								}
								else if(currentColumn.trim().equalsIgnoreCase("charge_code"))
								{
									chargeCode = checkNull(genericUtility.getColumnValue("charge_code", dom));
									chargeCode = chargeCode==null?null:chargeCode.trim();	
									System.out.println("chargeCode :"+chargeCode);
									
									if (chargeCode != null && chargeCode.trim().length() > 0)
									{
										
										sql = "select descr from gencodes where fld_name='CHARGE_CODE' and fld_value=? and mod_name='X'";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, chargeCode);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
										    chargeDescr = rs.getString(1);
										}
										
										chargeDescr = chargeDescr==null?"":chargeDescr.trim();
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										valueXmlString.append("<descr>").append("<![CDATA[" + chargeDescr + "]]>").append("</descr>");
									}
									
									
								}

								valueXmlString.append("</Detail1>"); // close tag
								
						        break;
								
							case 2:	
								System.out.println("mahendra itemchanged case 2 05.03.14 12:16");
								break;
						}//end of switch
						
						valueXmlString.append("</Root>");
						
						
					}
					catch (Exception e)
					{
							e.printStackTrace();
							throw new ITMException(e);
					} finally
					{
							try
							{
								if (conn != null)
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
									conn.close();
								}
								conn = null;
							} catch (Exception e)
							{
								e.printStackTrace();
								throw new ITMException(e);
							}
					}
					return valueXmlString.toString();

				}

	
				
				

				
				private String checkNullTrim(String input)
				{
					if (input == null)
					{
						input = "";
					} else
					{
						input = input.trim();
					}
					return input;
				}

				/** End of New function added by Swati on 06 sep 2013 **/

				private String checkNull(String input)
				{
					if (input == null)
					{
						input = "";
					}
					return input;
				}

				private double round(double round, int scale) throws ITMException
				{
					return Math.round(round * Math.pow(10, scale)) / Math.pow(10, scale);
				}

				private boolean isExist(Connection conn, String tableName, String columnName, String value) throws ITMException, RemoteException
				{
					PreparedStatement pstmt = null;
					ResultSet rs = null;
					String sql = "";
					boolean status = false;
					try
					{
						sql = "SELECT count(*) from " + tableName + " where " + columnName + "  = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, value);
						rs = pstmt.executeQuery();

						if (rs.next())
						{
							if (rs.getBoolean(1))
							{
								status = true;
							}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} catch (Exception e)
					{
						System.out.println("Exception in isExist ");
						e.printStackTrace();
						throw new ITMException(e);
					}
					System.out.println("returning String from isExist ");
					return status;
				}

				private String findValue(Connection conn, String columnName, String tableName, String columnName2, String value) throws ITMException, RemoteException
				{

					PreparedStatement pstmt = null;
					ResultSet rs = null;
					String sql = "";
					String findValue = "";

					try
					{
						sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 + "  = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, value);
						rs = pstmt.executeQuery();

						if (rs.next())
						{
							findValue = rs.getString(columnName);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} catch (Exception e)
					{
						System.out.println("Exception in findValue ");
						e.printStackTrace();
						throw new ITMException(e);
					}
					System.out.println("returning String from findValue ");
					return findValue;
				}

				private String errorType(Connection conn, String errorCode) throws ITMException
				{
					String msgType = "";
					PreparedStatement pstmt = null;
					ResultSet rs = null;
					try
					{
						String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, errorCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							msgType = rs.getString("MSG_TYPE");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} catch (Exception ex)
					{
						ex.printStackTrace();
						throw new ITMException(ex);
					} finally
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
						} catch (Exception e)
						{
							e.printStackTrace();
							throw new ITMException(e);
						}
					}
					return msgType;
				}

				



}
