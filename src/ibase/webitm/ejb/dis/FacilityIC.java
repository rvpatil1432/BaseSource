
/********************************************************
    Title : FacilityICLocal(D14CSUN012)
	Date  : 05/05/14
	Developer: Mahendra Jadhav

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class FacilityIC extends ValidatorEJB implements FacilityICRemote, FacilityICLocal
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	
	//method for validation
		public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
		{
			String errString = "";
			System.out.println("wfValdata() called for facility");
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
			String errCode = "";
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

			int currentFormNo =0,recCnt=0,ct=0;
			
			String facilityCode="",facilityDescr="",keyFlag="",stanCode="" ;
			
			double exchRate=0;

			try
			{
				
				System.out.println("FacilityIC.java ::wfvaldata called !!!!!!");
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
				userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
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
		
						
						if(childNodeName.equalsIgnoreCase("facility_code"))
						{   
							facilityCode = checkNull(genericUtility.getColumnValue("facility_code", dom));
							System.out.println("facilityCode !!!! "+facilityCode);

							sql = "select key_flag from transetup where tran_window='w_facility'";
							
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
								if(facilityCode == null || facilityCode.trim().length() == 0  )
								{
									errCode = "VMFCEMT";//facility code should not be null for manually transetup
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("facility Code should not be blank", errCode, userId);
									System.out.println("Error due to facility code blank!!");
								}
								else
								{
									sql = " select count (*) from facility where facility_code = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, facilityCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct = rs.getInt(1);	
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									
									if(ct > 0)
									{
										errCode = "VMFCTEXT";//Invalid Facility code!!!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										errString = getErrorString("Invalid facility code", errCode, userId);
										System.out.println("Error due to facility code already exist in master!!");
									}

								 }

							}//end of case transetup


						}
						
						if(childNodeName.equalsIgnoreCase("descr"))
						{    
							facilityDescr = genericUtility.getColumnValue("descr", dom);
							if( facilityDescr == null)	
							{
								errCode = "VMDESEMT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty Facility Description", errCode, userId);
								System.out.println("Facaility description should not be blank!!!");
							}
							
						}
						
						if(childNodeName.equalsIgnoreCase("stan_code"))
						{    
							stanCode = genericUtility.getColumnValue("stan_code", dom);
							if( stanCode == null)	
							{
								errCode = "VMSTNEMT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty Station Code", errCode, userId);
								System.out.println("station code should not be blank!!!");
							}
							else
							{
								sql = " select count (*) from station where stan_code = ?   ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, stanCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									ct = rs.getInt(1);	
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								System.out.println("ct !!!"+ct);
								if(ct == 0)
								{
									errCode = "VESTACD1";//Invalid Station code!!!
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("Invalid station code", errCode, userId);
									System.out.println("Error due to station code not exist in master!!");
								}

							 }
							
							
							
							
						}
						
					} // end for
					break;  // case 1 end

				}//end of switch 

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
			PreparedStatement pstmt = null;
			ResultSet rs = null, rs1 = null;
			
			String stanCode="" , city="", pin="",stateCode="";
			
			int currentFormNo = 0;
			int childNodeListLength = 0;
			int length = 0;
			
			
			NodeList parentNodeList = null;
			NodeList childNodeList = null;
			Node parentNode = null;
			Node childNode = null;
			String childNodeName = null;
			StringBuffer valueXmlString = new StringBuffer();
			List amtList =  new ArrayList();
			//GenericUtility genericUtility = GenericUtility.getInstance();
			ConnDriver connDriver = new ConnDriver();
			Date currentDate = new Date();
			
			try
			{
				System.out.println("FacilityIC.java ::: Itemchanged called !!!!!");
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
				System.out.println("Entry in itemChanged with form no 06 JULY 2014 3:25    :"+currentFormNo);
				switch (currentFormNo)
				{
				
					case 1:
						parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						valueXmlString.append("<Detail1>");
						childNodeListLength = childNodeList.getLength();
						
						System.out.println("currentColumn !!!"+currentColumn.trim());
						if(currentColumn.trim().equalsIgnoreCase("stan_code"))
						{
							
							stanCode=checkNull(genericUtility.getColumnValue("stan_code", dom));
							System.out.println("stanCode is :"+stanCode);
							
							sql = "select pin,city,state_code from station where stan_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								
								city=rs.getString("city");
								pin=rs.getString("pin");
								stateCode=rs.getString("state_code");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("city:"+city);
							System.out.println("pin:"+pin);
							System.out.println("stateCode:"+stateCode);
							
						}	
						valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");
						valueXmlString.append("<pin>").append("<![CDATA["+pin+"]]>").append("</pin>");
						valueXmlString.append("<state_code>").append("<![CDATA["+stateCode+"]]>").append("</state_code>");
						valueXmlString.append("</Detail1>"); // close tag
						System.out.println("mahendra itemchanged case 1 valueXmlString : "+valueXmlString);
				        break;
								
				}//end of switch 
				
				valueXmlString.append("</Root>");
				System.out.println("final valueXmlString :"+valueXmlString);
				
			}
			catch (Exception e)
			{
					e.printStackTrace();
					System.out.println("Exception ::" + e.getMessage());
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




		private String checkNull(String input)
		{
			if (input == null)
			{
				input = "";
			}
			return input;
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