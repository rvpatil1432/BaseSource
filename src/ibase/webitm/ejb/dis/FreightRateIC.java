
/********************************************************
	Title 	 : FreightRateIC [DI3GSUN047]
	Date  	 : 26/MAR/14
	Developer: Priyanka Shinde
 ********************************************************/
package ibase.webitm.ejb.dis;

import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
@Stateless
public class FreightRateIC extends ValidatorEJB implements FreightRateICLocal,FreightRateICRemote  
{

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String winName = null;
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("Priyanka testing : Inside wfValData 0 ");
		System.out.println("Priyanka testing : xmlString :"+xmlString);
		System.out.println("Priyanka testing : xmlString1 :"+xmlString1);
		System.out.println("Priyanka testing : xmlString2 :"+xmlString2);
		System.out.println("Priyanka testing : objContext :"+objContext);
		System.out.println("Priyanka testing : editFlag :"+editFlag);
		System.out.println("Priyanka testing : xtraParams :"+xtraParams);
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

		String frtChgstable = "",loadType="",chargeCode="",chargeMode="",frtList="",standCodeFrom="",standCodeTo="" ;
		String transitType="",transMode="";

		double amount=0.0,basicFreight=0.0,stdPickUp=0.0,stdTransitTime=0.0,minWeight=0.0,maxWeight=0.0;
		//int count=0;
		int sequence= 0, seq=0;

		int currentFormNo = 0;
		int childNodeListLength;
		int ctr = 0;
		int cnt=0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
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
		System.out.println("Priyanka testing : Inside wfValData 1 ");
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		System.out.println("Priyanka testing : editFlag :" + editFlag);

		try
		{
	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("Priyanka testing : currentFormNo :"+currentFormNo);
			}
	
			switch (currentFormNo)
			{
			 case 1:
				System.out.println("Priyanka testing case 1 for validation ");
	
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("Priyanka testing :parentNode : "+parentNode);
				System.out.println("Priyanka testing :childNodeListLength : "+childNodeListLength);
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("Priyanka testing :childNodeName : "+childNodeName);
						
					if(childNodeName.equalsIgnoreCase("frt_list"))
					{
						frtList = checkNull(genericUtility.getColumnValue("frt_list",dom));
						System.out.println(" testing :frtList =" + frtList);
	
						if (frtList == null || frtList.trim().length() == 0)
						{
							errCode = "VMFRTLST";//Freight List should not be Empty.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("freight list can not be blank!!!");
							System.out.println("freight list VALDATION!!!");
						} 
						
						if(!isExist(conn, "freight_list", "FRT_LIST", frtList))
  						 {
							 errCode = "VTFRLNTEXT";//Freight List does not exists
				    			errList.add(errCode);
				    			errFields.add(childNodeName.toLowerCase());
				    			System.out.println("Freight List does not exists");
  						 }

	
					}
					else if(childNodeName.equalsIgnoreCase("load_type"))
					{
							loadType = checkNull(genericUtility.getColumnValue("load_type",dom));
							frtList = checkNull(genericUtility.getColumnValue("frt_list",dom));
							System.out.println(" testing :loadType =" + loadType);
							System.out.println(" testing :frtlist =" + frtList);
	
							if (loadType == null || loadType.trim().length() == 0)
							{
								errCode = "VMLOADTY";//load type can not be blank
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("load type can not be blank!!!");
	
							} 
	
						    if(frtList!=null && loadType!=null)
						    {
						    	sql="select count(1) from freight_list where frt_list =? and load_type =?";
						    	pstmt = conn.prepareStatement(sql);
						    	pstmt.setString(1,frtList);
						    	pstmt.setString(2,loadType);
						    	rs = pstmt.executeQuery();	    
							    	if (rs.next())
							    	{
							    		cnt = rs.getInt(1);
							    		System.out.println("Count of loadtype: ===="+cnt);
							    	}
						    		pstmt.close();
							    	rs.close();
							    	pstmt = null;
							    	rs = null;
		
						    		if(cnt==0)
						    		{
						    			errCode = "VMFLTVLD";//Load type not defined for freight list master
						    			errList.add(errCode);
						    			errFields.add(childNodeName.toLowerCase());
						    			System.out.println("Load type not defined for freight list master");
						    		}
						    	
						     }
						}
	
						else if(childNodeName.equalsIgnoreCase("stan_code__from"))
						{
						  standCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from",dom));
						  standCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to",dom));
						  System.out.println(" testing :standCodeFrom =" + standCodeFrom);
						  System.out.println(" testing :standCodeTo =" + standCodeTo);
							
							
							if (standCodeFrom == null || standCodeFrom.trim().length() == 0)
							{
								errCode = "VMSTNCDFR";//STATION CODE FROM CAN NOT BE NULL 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("STATION CODE FROM CAN NOT BE NULL ");
							 } 
							
							 if(standCodeFrom!=null && standCodeFrom.trim().length()>0)
							 {
								sql=" select count(1) from station where stan_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,standCodeFrom);
								rs = pstmt.executeQuery();	    
									if (rs.next())
									{
										cnt = rs.getInt(1);
										System.out.println("Count: ===="+cnt);
									}
									pstmt.close();
							    	rs.close();
							    	pstmt = null;
							    	rs = null;
									if(cnt==0)
									{
	               					    errCode = "VMSTAN2";//Station code doesn't exist in master
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										System.out.println("Station code doesn't exist in master!!");
										
									}
	
						    }
	
							    if(standCodeFrom!=null && standCodeTo!=null)
							    {
							    	if(childNodeName.equalsIgnoreCase("stan_code__from") )
							    	{
							    		sql=" select count(1) from distance where stan_code__from = ? and stan_code__to = ?";
							    		pstmt = conn.prepareStatement(sql);
							    		pstmt.setString(1,standCodeFrom);
							    		pstmt.setString(2,standCodeTo);
							    		rs = pstmt.executeQuery();	
							    		if (rs.next())
							    		{
							    			cnt = rs.getInt(1);
							    			System.out.println("Count: ===="+cnt);
							    		}
							    		pstmt.close();
								    	rs.close();
								    	pstmt = null;
								    	rs = null;
							    	}
							    	
							     }
						 }
	
						    //stand_code_to
	
							else if(childNodeName.equalsIgnoreCase("stan_code__to"))
							{
								System.out.println("STANDCODE TOOO");

								standCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from",dom));
								standCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to",dom));
								System.out.println(" testing :standCodeFrom =" + standCodeFrom);
								System.out.println(" testing :standCodeTo =" + standCodeTo);

								if (standCodeTo == null || standCodeTo.trim().length() == 0)
								{
									errCode = "VMSTNCDTO";//STATION CODE To CAN NOT BE NULL 
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("STATION CODE TO CAN NOT BE NULL ");

								} 

								if(standCodeTo!=null && standCodeTo.trim().length()>0)
								{
									sql=" select count(1) from station where stan_code = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,standCodeTo);
									rs = pstmt.executeQuery();	    
										if (rs.next()) 
										{
											cnt = rs.getInt(1);
											System.out.println("Count: ===="+cnt);
										}
										pstmt.close();
								    	rs.close();
								    	pstmt = null;
								    	rs = null;
										if(cnt==0)
										{
											errCode = "VMSTAN2";//Station code doesn't exist in master
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											System.out.println("Station code doesn't exist in master!!");
										
										}

								}

								if(standCodeFrom!=null && standCodeTo!=null)
								{
									if(childNodeName.equalsIgnoreCase("stan_code__from") )
									{
										sql=" select count(1) from distance where stan_code__from = ? and stan_code__to = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,standCodeFrom);
										pstmt.setString(2,standCodeTo);
										rs = pstmt.executeQuery();	
										if (rs.next())
										{
											cnt = rs.getInt(1);
											System.out.println("Count: ===="+cnt);
										}
										pstmt.close();
								    	rs.close();
								    	pstmt = null;
								    	rs = null;
									}
									
								}

							}
	
	
						//basic freight
						else if(childNodeName.equalsIgnoreCase("basic_freight"))
						{
							basicFreight = checkDoubleNull(genericUtility.getColumnValue("basic_freight",dom));
							System.out.println("testing :basicFreight =" + basicFreight);
							if (basicFreight<=0)
							{
								errCode = "VMBSFRT";//Basic Freight should be greater than Zero.
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Basic Freight should be greater than Zero.!!!");
							} 
							System.out.println("basic freight VALDATION!!!");
						}
						//transit_type
						else if(childNodeName.equalsIgnoreCase("transit_type"))
						{
							transitType = checkNull(genericUtility.getColumnValue("transit_type",dom));
							System.out.println("tetsting :transitType =" + transitType);
	
							if (transitType== null||transitType.trim().length()==0)
							{
								errCode = "VMBSFRT";//Basic Freight should be greater than Zero.;.
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Basic Freight should be greater than Zero..!!");
							} 
							System.out.println("transit_type VALDATION!!!");
						}
						//std_pick_up
						else if(childNodeName.equalsIgnoreCase("std_pick_up"))
						{
							stdPickUp = checkDoubleNull(genericUtility.getColumnValue("std_pick_up",dom));
							System.out.println(" testing :sequence =" + stdPickUp);
	
							if (stdPickUp<=0)
							{
								errCode = "VMSTDPKT";//Standard Pick up time should be greater than Zero..
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Standard Pick up time should be greater than Zero..!!");
							} 
						}
							//std_transit_time
	
						else if(childNodeName.equalsIgnoreCase("std_transit_time"))
						{
							stdTransitTime = checkDoubleNull(genericUtility.getColumnValue("std_transit_time",dom));
							System.out.println(" testing :sequence =" + stdTransitTime);


							if (stdTransitTime<=0)
							{
								errCode = "VMSTDTNT";//Standard Transit Time should be greater than Zero...
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Standard Transit Time should be greater than Zero...!!");
							} 

						}

						else if(childNodeName.equalsIgnoreCase("trans_mode"))
						{
							transMode = checkNull(genericUtility.getColumnValue("trans_mode",dom));
							System.out.println(" testing :transMode =" + transMode);


							if (transMode==null||transMode.trim().length()==0)
							{
								errCode = "VTTRMODE";//Trans-Mode Should not be empty...
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Transporter Mode cannot be empty...!!");
							} 

						}


						else if(childNodeName.equalsIgnoreCase("min_weight"))
						{
							minWeight= checkDoubleNull(genericUtility.getColumnValue("min_weight",dom));
							loadType = checkNull(genericUtility.getColumnValue("load_type",dom));
							System.out.println(" testing :minWeight =" + minWeight);
							System.out.println(" testing :loadType =" + loadType);

							if(loadType.equals("D"))
							{
								System.out.println("LAOD TYPE IS DDD");
								
								if (minWeight<=0)
								{
									errCode = "VTMINWGT";//Minimum Weight cannot be empty
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("Minimum Weight cannot be empty");
								} 
							}
						}
							//max weight
							
						else if(childNodeName.equalsIgnoreCase("max_weight"))
						{
							 maxWeight= checkDoubleNull(genericUtility.getColumnValue("max_weight",dom));
							 minWeight= checkDoubleNull(genericUtility.getColumnValue("min_weight",dom));
							 loadType = checkNull(genericUtility.getColumnValue("load_type",dom));
							 System.out.println(" testing :max_weight =" + maxWeight);
							 System.out.println(" testing :loadType =" + loadType);

							if(loadType.equals("D"))
							{
								System.out.println("LAOD TYPE IS DDD");
								
								if (maxWeight<=0)
								{
									errCode = "VTMAXWGT";//MaximumW Weight cannot be empty.
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("MaximumW Weight cannot be empty.");
								} 
								else if (maxWeight<minWeight)
								{
									errCode = "VMMINWGT";//Min weight should be less than max weight
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("MaximumW Weight should be greater than min weight");
								
								}
							}
					   }
							
				  }//end of else  if loop
					//end of for 	
			     break;//end of switch
					//case2
				 case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
	
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
	
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
	
						if(childNodeName.equalsIgnoreCase("charge_code"))
						{
							System.out.println("Case 2");
							chargeCode = checkNull(genericUtility.getColumnValue("charge_code",dom));
							System.out.println(" testing :chargeCode =" + chargeCode);
							if(chargeCode.trim().length() == 0 || chargeCode == null)
							{
								errCode = "VMCHGCODE";//Charge Code should not be Empty....
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Charge Code should not be Empty....!!");
							}
						}
							//chargeMode
						 else if(childNodeName.equalsIgnoreCase("charges_mode"))
						 {
	
							chargeMode = checkNull(genericUtility.getColumnValue("charges_mode",dom));
							System.out.println(" testing :charges_mode =" + chargeMode);
							if(chargeMode.trim().length() == 0 || chargeMode == null)
							{
								errCode = "VMCHGMOD";//Charge Mode should not be Empty.....
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Charge Mode should not be Empty.....!!");
							}
						}
						//amount
					  else if(childNodeName.equalsIgnoreCase("amount"))
					   {
						    amount = checkDoubleNull(genericUtility.getColumnValue("amount",dom));
						    System.out.println(" testing :amount =" + amount);
							if(amount==0)
							{
								errCode = "VMCHGAMT";//Amount should be greater than Zero...
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Amount should be greater than Zero...!!");
							}
						}
						//sequence
						else if(childNodeName.equalsIgnoreCase("sequence"))
					    {

						    sequence = checkIntNull(genericUtility.getColumnValue("sequence",dom));
						    System.out.println(" testing :sequence =" + sequence);
							if(sequence==0)
							{
								errCode = "VMSEQ2";//Sequence should not be Zero....
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Sequence should not be Zero.....!!");
							}
							else
							{
								frtList = checkNull(genericUtility.getColumnValue("frt_list",dom));
								loadType = checkNull(genericUtility.getColumnValue("load_type",dom));
								standCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from",dom));
								standCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to",dom));
								chargeCode = checkNull(genericUtility.getColumnValue("charge_code",dom));
	
								sql="select sequence from freight_rate_det " +
										"where frt_list = ? and  " +
										" load_type = ? and stan_code__from = ? and stan_code__to = ? and" +
										" charge_code = ?";
	
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,frtList);
								pstmt.setString(2, loadType);
								pstmt.setString(3,standCodeFrom);
								pstmt.setString(4, standCodeTo);
								pstmt.setString(5,chargeCode);
								rs = pstmt.executeQuery();	
									if(rs.next())
									{
										seq= rs.getInt(1);
										System.out.println("Sequence getting"+seq);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(seq==0)
									{
										seq=sequence;
										System.out.println("Sequence getting"+seq);
									}
												
									if(seq!=sequence)
									{
										sql="select count(1) from  freight_rate_det " +
												"where frt_list=? and load_type= ? and 	stan_code__from = ?	" +
												"and	stan_code__to= ? and   sequence = ?";
		
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,frtList);
										pstmt.setString(2, loadType);
										pstmt.setString(3,standCodeFrom);
										pstmt.setString(4, standCodeTo);
										pstmt.setInt(5,sequence);
										rs = pstmt.executeQuery();	
											if(rs.next())
											{
												cnt=rs.getInt(1);
												System.out.println("Count of sequence: "+cnt);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											if(cnt>0)
											{
												errCode = "VMSEQ1";//Sequence number already present.....
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
												System.out.println("Sequence number already present......!!");
											
											}
		
									}
							    }
					      }
	
					} 
	
				} 
	
				int errListSize = errList.size();
				int count = 0;
				String errFldName = null;
				if (errList != null && errListSize > 0)
				{
					for (count = 0; count < errListSize; count++)
					{
						errCode = errList.get(count);
						errFldName = errFields.get(count);
						System.out.println(" testing :errCode .:" + errCode);
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
			} 
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		System.out.println("testing : final errString : "+errString);
		return errString;

	}//end of validation method

public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)	throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("hELLO PRINT");
		try
		{
			System.out.println("xmlString@@@@@@@"+xmlString);

			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			System.out.println("dom@@@@@@@"+dom);

			System.out.println("xmlString1@@@@@@@"+xmlString1);

			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}

			System.out.println("xmlString2@@@@@@@"+xmlString2);

			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("VALUE HELLO PRINT["+valueXmlString+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [FreightRateIC][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA["+valueXmlString+"]");
		return valueXmlString;
  }


public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException
	{
		System.out.println("sTART PRINT ");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";
		//added
		String frtlist= "";
		String frtChangeTable = "";
		String loadType = "";
		String genDesc="";
		String descrTo ="";
		String descr ="";
		String descrFrom ="";
		String stanCodeTo = "";
		String stanCodeFrom = "";
		String stanCode="";
		String distance="";
		String transitType="";
		String chargeCode="";
		String chargeMode="";
		double amount=0.0;
		int sequence=0;
		String charCodeAdd="";
		double minWeight=0.0,maxWeight=0.0;
		String loginSite = "";
		double  stdTransitTime=0.0;
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy");
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		//FinCommon finCommon = new FinCommon();
		//DistCommon distCommon = new DistCommon();
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			//this.finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("Priyanka itemchanged 1 currentFormNo : "+currentFormNo); 
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			  case 1:
				System.out.println("Freight Rate itemchanged case 1");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}

					ctr++;
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));

				System.out.println("CURRENT COLUMN Case 1 Freight Rate *******["+currentColumn+"]");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
				
					chargeCode=genericUtility.getColumnValue("charge_code", dom2);
					valueXmlString.append("<charge_code protect='0'>").append("<![CDATA["+chargeCode+"]]>").append("</charge_code>");

				}
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					loadType = checkNull(genericUtility.getColumnValue("load_type", dom));
					if(loadType.equals("F"))
					{
						System.out.println("LAOD TYPE IS FFF");
						valueXmlString.append("<min_weight protect = \"1\">").append("<![CDATA[]]>").append("</min_weight>");
						valueXmlString.append("<max_weight protect = \"1\">").append("<![CDATA[]]>").append("</max_weight>");
					}
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("frt_list"))
				{
					frtlist = checkNull(genericUtility.getColumnValue("frt_list",dom));
					System.out.println("frtlist"+frtlist);
					sql="SELECT A.FRT_CHGS_TABLE ,A.LOAD_TYPE ,B.DESCR from FREIGHT_LIST A,GENCODES B " +
							"where A.FRT_LIST = ? AND B.FLD_NAME  = 'LOAD_TYPE'  " +
							" AND B.FLD_VALUE = trim(A.LOAD_TYPE) " +
							" AND  B.MOD_NAME = 'X' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,frtlist);		
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						frtChangeTable = rs.getString("FRT_CHGS_TABLE");
						loadType = rs.getString("LOAD_TYPE");
						genDesc = rs.getString("DESCR");
						System.out.println("frtChangeTable--"+frtChangeTable);
						System.out.println("loadType---"+loadType);
						System.out.println("genDesc====="+genDesc);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(loadType.equals("F"))
					{
						System.out.println("LAOD TYPE IS FFF");
						valueXmlString.append("<min_weight protect = \"1\">").append("<![CDATA[]]>").append("</min_weight>");
						valueXmlString.append("<max_weight protect = \"1\">").append("<![CDATA[]]>").append("</max_weight>");
					}
					else
					{
						System.out.println("LAOD TYPE IS DDDDDDDDD");
						valueXmlString.append("<min_weight protect = \"0\">").append("<![CDATA[]]>").append("</min_weight>");
						valueXmlString.append("<max_weight protect = \"0\">").append("<![CDATA[]]>").append("</max_weight>");
					}

						valueXmlString.append("<load_type>").append("<![CDATA["+loadType+"]]>").append("</load_type>");
						valueXmlString.append("<gencodes_descr>").append("<![CDATA["+genDesc+"]]>").append("</gencodes_descr>");
						valueXmlString.append("<frt_chgs_table>").append("<![CDATA["+frtChangeTable+"]]>").append("</frt_chgs_table>");
			    } 
				//load_type**
				else if (currentColumn.trim().equalsIgnoreCase("load_type"))
				{
					loadType = checkNull(genericUtility.getColumnValue("load_type", dom));
					minWeight= checkDoubleNull(genericUtility.getColumnValue("min_weight",dom));
					maxWeight= checkDoubleNull(genericUtility.getColumnValue("max_weight",dom));
					if(loadType.equals("F"))
					{
						System.out.println("LAOD TYPE IS FFFFFFFFFFFF");
						valueXmlString.append("<min_weight protect = \"1\">").append("<![CDATA[]]>").append("</min_weight>");
						valueXmlString.append("<max_weight protect = \"1\">").append("<![CDATA[]]>").append("</max_weight>");
					}
					else
					{
						System.out.println("LAOD TYPE IS DDDDDDDDD");
						valueXmlString.append("<min_weight protect = \"0\">").append("<![CDATA[]]>").append("</min_weight>");
						valueXmlString.append("<max_weight protect = \"0\">").append("<![CDATA[]]>").append("</max_weight>");
					}
					sql = "SELECT DESCR FROM GENCODES where FLD_NAME ='LOAD_TYPE' AND FLD_VALUE =? AND MOD_NAME = 'X'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,loadType);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						genDesc = rs.getString("DESCR");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<gencodes_descr>").append("<![CDATA["+genDesc+"]]>").append("</gencodes_descr>");
					this.itemChanged(dom, dom1, dom2, objContext, "transit_type", editFlag, xtraParams);
				  }
				//stan_code__from
				else if (currentColumn.trim().equalsIgnoreCase("stan_code__from"))
				{
					stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from", dom));
					stanCodeTo=checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					sql = "Select descr from station where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCodeFrom);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descrFrom = rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					valueXmlString.append("<descr>").append("<![CDATA["+descrFrom+"]]>").append("</descr>");
					if(stanCodeTo!=null && stanCodeFrom!=null)
					{
						sql="select distance from distance where stan_code__from = ? and stan_code__to = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, stanCodeFrom);
						pstmt.setString(2, stanCodeTo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							distance=rs.getString("DISTANCE");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<distance>").append("<![CDATA["+distance+"]]>").append("</distance>");
					}
					this.itemChanged(dom, dom1, dom2, objContext, "transit_type", editFlag, xtraParams);
				}

				//stan_code__to
				else if (currentColumn.trim().equalsIgnoreCase("stan_code__to"))
				{
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from", dom));
					sql="Select descr from station where stan_code = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeTo);
					rs=	pstmt.executeQuery();
					if(rs.next())
					{
						descrTo=rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					valueXmlString.append("<descr_1>").append("<![CDATA["+descrTo+"]]>").append("</descr_1>");
					if(stanCodeTo!=null && stanCodeFrom!=null)
					{
						sql="select distance from distance where stan_code__from = ? and stan_code__to = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, stanCodeFrom);
						pstmt.setString(2, stanCodeTo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							distance=rs.getString("DISTANCE");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<distance>").append("<![CDATA["+distance+"]]>").append("</distance>");
					}
					this.itemChanged(dom, dom1, dom2, objContext, "transit_type", editFlag, xtraParams);
				}

				//transit_type
				else if (currentColumn.trim().equalsIgnoreCase("transit_type"))
				{
					transitType = checkNull(genericUtility.getColumnValue("transit_type", dom));
					stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from", dom));
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					loadType = checkNull(genericUtility.getColumnValue("load_type", dom));
					if(transitType!=null && stanCodeFrom!=null && stanCodeTo!=null && loadType!=null)
					{
						sql="select std_transit_time from  distancedet " +
								"where stan_code__from = ? and  stan_code__to= ? and load_type = ? " +
								"and transit_type = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeFrom);
						pstmt.setString(2,stanCodeTo );
						pstmt.setString(3,loadType );
						pstmt.setString(4,transitType );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stdTransitTime=rs.getDouble("STD_TRANSIT_TIME");
							System.out.println("stdTransitTime==========="+stdTransitTime);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<std_transit_time>").append("<![CDATA["+stdTransitTime+"]]>").append("</std_transit_time>");
					}
				  }

				valueXmlString.append("</Detail1>");
		     break;

				////CASE2
			 case 2:

				System.out.println("Freight Rate itemchanged case 2");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}

					ctr++;
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN Case 2 Freight Rate *******["+currentColumn+"]");
				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					chargeCode=genericUtility.getColumnValue("charge_code", dom2);
					valueXmlString.append("<charge_code protect='1'>").append("<![CDATA["+chargeCode+"]]>").append("</charge_code>");

				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("@@@@@@@ itm_default detail called....");
					chargeCode=genericUtility.getColumnValue("charge_code", dom1);

					frtlist = genericUtility.getColumnValue("frt_list", dom1);
					loadType = genericUtility.getColumnValue("load_type", dom1);
					stanCodeFrom = genericUtility.getColumnValue("stan_code__from", dom1);
					stanCodeTo=genericUtility.getColumnValue("stan_code__to", dom1);

					genDesc = genericUtility.getColumnValue("gencodes_descr", dom1);
					descrFrom = genericUtility.getColumnValue("descr", dom1);
					descrTo=genericUtility.getColumnValue("descr_1", dom1);
					
					System.out.println("frt_list===="+frtlist);
					System.out.println("load_type==="+loadType);
					System.out.println("stan_code__from===="+stanCodeFrom);
					System.out.println("stan_code__to===="+stanCodeTo);
					System.out.println("gencodes_descr==="+genDesc);
					System.out.println("descr=="+descrFrom);
					System.out.println("descr_1==="+descrTo);
					System.out.println("charge_code==="+chargeCode);

					valueXmlString.append("<frt_list>").append("<![CDATA[" + frtlist +"]]>").append ("</frt_list>");	
					valueXmlString.append("<load_type>").append("<![CDATA[" + loadType +"]]>").append("</load_type>");	
					valueXmlString.append("<stan_code__from>").append("<![CDATA[" + stanCodeFrom +"]]>").append ("</stan_code__from>");	
					valueXmlString.append("<stan_code__to>").append("<![CDATA[" + stanCodeTo +"]]>").append("</stan_code__to>");	
					valueXmlString.append("<gencodes_descr>").append("<![CDATA[" + genDesc +"]]>").append ("</gencodes_descr>");	
					valueXmlString.append("<descr>").append("<![CDATA[" + descrFrom +"]]>").append("</descr>");	
					valueXmlString.append("<descr_1>").append("<![CDATA[" + descrTo +"]]>").append ("</descr_1>");	
					//	valueXmlString.append("<load_type>").append("<![CDATA[" + descrFrom +"]]>").append("</load_type>");	****
				}

			    //charge code
				else if (currentColumn.trim().equalsIgnoreCase("charge_code"))
				{
					frtChangeTable=genericUtility.getColumnValue("frt_chgs_table", dom1);
					loadType=genericUtility.getColumnValue("load_type", dom1);
					chargeCode=genericUtility.getColumnValue("charge_code", dom2);

					sql="select charges_mode,amount,charge_code__add,sequence from freight_charges_table " +
							"where frt_chgs_table = ? and load_type= ? and charge_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, frtChangeTable);
					pstmt.setString(2, loadType);
					pstmt.setString(3,chargeCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						chargeMode = rs.getString("CHARGES_MODE");
						amount =  rs.getDouble("AMOUNT");
						charCodeAdd = rs.getString("CHARGE_CODE__ADD");
						sequence = rs.getInt("SEQUENCE");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<charges_mode>").append("<![CDATA["+chargeMode+"]]>").append("</charges_mode>");
					valueXmlString.append("<amount>").append("<![CDATA["+amount+"]]>").append("</amount>");				
					valueXmlString.append("<charge_code__add>").append("<![CDATA["+charCodeAdd+"]]>").append("</charge_code__add>");
					valueXmlString.append("<sequence>").append("<![CDATA["+sequence+"]]>").append("</sequence>");
				}
				valueXmlString.append("</Detail2>");
				break;
			}
			valueXmlString.append("</Root>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					if (pstmt != null)
						pstmt.close();
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return valueXmlString.toString();
			}

	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}
	private int checkIntNull(String str)
	{
		if(str == null || str.trim().length() == 0 )
		{
			return 0;
		}
		else
		{
			return Integer.parseInt(str) ;
		}

	}
	private double checkDoubleNull(String str)
	{
		if(str == null || str.trim().length() == 0)
		{
			return 0.0;
		}
		else
		{
			return Double.parseDouble(str) ;
		}

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
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}
		return msgType;
	}

	
	private boolean isExist(Connection conn, String tableName, String columnName, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ;
		String sql = "";
		boolean status = false;
		try
		{
			sql = "SELECT count(*) from " + tableName + " where " + columnName +"  = ?";
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
}
