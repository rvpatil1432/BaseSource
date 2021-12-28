/********************************************************
	Title : TransporterIC
	Date  : 11/04/2012
	Developer: Mahesh Patidar
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
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class TransporterIC extends ValidatorEJB implements TransporterICLocal, TransporterICRemote 
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
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}

//	@SuppressWarnings("null")
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		int childNodeListLength;
		int currentFormNo = 0;
		String contactCode = "";
		String stanCode = "";
		String transporterType = "";
		String groupCode = "";
		String tranCodePay = "";
		String tranCode = "";
		String currCode = "";
		String acctCodeFp = "";
		String cctrCodeFp = "";
		String sitecodepay = "";
		String taxenv = "";
		String taxcls = "";
		String active = "";
		String stateCode = "";
		String crTerm = "";
		String countCode = "";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String sql = "";
		String sql1 = "";
		String errFldName = "";
		String userId = "";
		String pin="",pin_pattern="";
		long cnt = 0;
		String keyFlag = "";
		int cntv = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		PreparedStatement pstmt1 = null ;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ConnDriver connDriver = new ConnDriver();
		String regCode="",descr="",	validUpto="",regDate="";
		SimpleDateFormat simpleDateFormat1 = null;
		java.sql.Timestamp currDate = null;
		String currAppdate ="";
		Timestamp validDate=null,registerDate=null;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			simpleDateFormat1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate);
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
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("trancode>>>><<<<");
				if(childNodeName.equalsIgnoreCase("tran_code"))
				{
					
					    sql = "select key_flag from transetup where tran_window = 'w_transporter'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							keyFlag = rs.getString(1)==null ? "M" : rs.getString(1).trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						tranCode = genericUtility.getColumnValue("tran_code", dom);
						  
						
						if(keyFlag.equalsIgnoreCase("M"))
						{
						
							if(tranCode == null || tranCode.trim().length() == 0 )
							{
								System.out.println("trancode null validatioon fire");
								errCode="VMTRANCD1 ";						
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							else
							{
								//if(keyFlag.equalsIgnoreCase("M") || keyFlag.equalsIgnoreCase("A"))
								if(editFlag.equals("A"))
								{
									sql = "select count(*) from transporter where tran_code = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, tranCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cntv = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
							    	
									 if(cntv > 0)
									 {
										 System.out.println("trancode already esist validatioon fire");
										    errCode = "VMDUPL1";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;
									 }
							  }
						 }
					}

				}
				
				else if(childNodeName.equalsIgnoreCase("contact_code"))  //done
				{
					contactCode = genericUtility.getColumnValue("contact_code", dom);
					if(contactCode == null || contactCode.trim().length() == 0)
					{
						errCode = "VTCONTNULL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						break;
					}
					else
					{
						sql = " Select Count(*) from contact where contact_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, contactCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
							if(cnt == 0) 
							{
								errCode = "VMCONTACT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}									
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
	
				else if(childNodeName.equalsIgnoreCase("transporter_type"))  //done
				{
					transporterType = genericUtility.getColumnValue("transporter_type", dom);
					if(transporterType == null || transporterType.trim().length() == 0)
					{
						//errCode = "VMTYPECK";
						errCode = "VMTYPE";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						break;
					}
				}
				
				else if(childNodeName.equalsIgnoreCase("group_code"))
				{
					groupCode = genericUtility.getColumnValue("group_code", dom);
					tranCode = genericUtility.getColumnValue("tran_code", dom);
					System.out.println("<<<<<<+"+groupCode+"+>>>>>>>"+tranCode);
					
					//Modified by Anjali R. on[15/03/2018][To check tran code is same as group code or not][Start]
					tranCode = tranCode == null ? "": tranCode;
					groupCode = groupCode == null ? "":groupCode;	
					//if((tranCode != null && tranCode.trim().length() > 0) && (groupCode != null && groupCode.trim().length() >0))
					if((tranCode != null) && (groupCode != null))
					{
						//if(!(groupCode.equals(tranCode)))
						if(!(groupCode.trim().equals(tranCode.trim())))
						//Modified by Anjali R. on[15/03/2018][To check tran code is same as group code or not][End]
						{
						//sql = " select count(*) from transporter where tran_code =  ? ";
						sql = " select count(*) from transporter where group_code =  ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, groupCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
							if(cnt == 0) 
							{
								//Changed By PriyankaC to add correct description.[START]
								 errCode = "INVGPCO";
								//errCode = "VMGRPCD"; 
								//Changed By PriyankaC to add correct description.[END]
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}									
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("tran_code__pay"))
				{
					tranCodePay = genericUtility.getColumnValue("tran_code__pay", dom);
					tranCode = genericUtility.getColumnValue("tran_code", dom);
					
					//Changed By PriyankaC to remove the null validation on 24DEC2018.[START]
					/*
					if(tranCodePay == null || tranCodePay.trim().length() == 0)
					{
						System.out.println("enter for null validation tran code pay");
						errCode = "VEPAYTO";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						
					}*/
					//added by priyanka as per pb code
					//else if((tranCodePay!=null && tranCodePay.trim().length()>0) &&(tranCode!=null && tranCode.trim().length()>0))	
					
					 if((tranCodePay!=null && tranCodePay.trim().length()>0) &&(tranCode!=null && tranCode.trim().length()>0))						
					{
						//if(!tranCodePay.equals(tranCode))
						if(!tranCodePay.trim().equals(tranCode.trim()))
						//Changed By PriyankaC on 24DEC2018 [END]
						{
							sql = " select count(*) from transporter where tran_code =  ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranCodePay);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMPAYTO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("site_code__pay"))
				{
					sitecodepay = genericUtility.getColumnValue("site_code__pay",dom);							
                    System.out.println("enter in site code pay");

					if (sitecodepay != null && sitecodepay.trim().length() > 0 )
					{
						sql = "select count(1) from site where site_code = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,sitecodepay);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						if(cnt == 0)
						{
							System.out.println("site_code not exist validation fire");
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
							
						}
					
					}
                }
				
				 else if(childNodeName.equalsIgnoreCase("stan_code"))
				  {
						stanCode = genericUtility.getColumnValue("stan_code", dom);
							
						if(stanCode != null && stanCode.trim().length() > 0 )
						{

						   System.out.println("stanCode>>>>"+stanCode);
                     
						   sql = " select count(*) from station where stan_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSTANCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
						    pstmt = null;						
							
						}
					}
				
				
				 else if(childNodeName.equalsIgnoreCase("state_code"))
					{
						stateCode = genericUtility.getColumnValue("state_code", dom);
						if(stateCode != null && stateCode.trim().length() > 0)
						{
							sql = " select count(*) from state where state_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stateCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSTATE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				
				
					else if(childNodeName.equalsIgnoreCase("count_code"))
					{
						countCode = genericUtility.getColumnValue("count_code", dom);
						if(countCode != null && countCode.trim().length() > 0)
						{
							sql = " select count(*) from country where count_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VTCONTCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
						}
					}
				
				
				 else if(childNodeName.equalsIgnoreCase("cr_term"))
					{
						crTerm = genericUtility.getColumnValue("cr_term", dom);
						System.out.println("crtermmmm"+crTerm);
						if (crTerm != null && crTerm.trim().length() > 0 )
						{
							sql = " select count(*) from crterm where cr_term = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VTCRTERM1 ";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
					}
					}
				
				
//				 else if(childNodeName.equalsIgnoreCase("tax_env"))
//					{
//						taxenv = genericUtility.getColumnValue("tax_env",dom);
//						if (taxenv != null && taxenv.trim().length() > 0 )
//						{
//					
//						sql = "select count(1) from taxenv where tax_env = ? ";
//						pstmt=conn.prepareStatement(sql);
//						pstmt.setString(1,taxenv);
//						rs = pstmt.executeQuery();
//						if(rs.next())
//						{
//							cnt = rs.getInt(1);
//						}
//						pstmt.close();
//						rs.close();
//						pstmt = null;
//						rs = null;
//						if(cnt == 0)
//						{
//							errCode = "TAXENVNE";
//							errList.add(errCode);
//							errFields.add(childNodeName.toLowerCase());
//							break;
//							
//						}
//						}
//					}
					
//					else if(childNodeName.equalsIgnoreCase("tax_class"))
//					{
//						taxcls = genericUtility.getColumnValue("tax_class",dom);
//						if (taxcls != null && taxcls.trim().length() > 0 )
//						{
//						
//						sql = "select count(1) from taxclass where tax_class = ? ";
//						pstmt=conn.prepareStatement(sql);
//						pstmt.setString(1,taxcls);
//						rs = pstmt.executeQuery();
//						if(rs.next())
//						{
//							cnt = rs.getInt(1);
//						}
//						pstmt.close();
//						rs.close();
//						pstmt = null;
//						rs = null;
//						if(cnt == 0)
//						{
//							errCode = "TAXCLSNE";
//							errList.add(errCode);
//							errFields.add(childNodeName.toLowerCase());
//							break;
//							
//						}
//						}
//					}
				
				
				
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						//changes by deepak sawant (23/10/13)
						currCode = genericUtility.getColumnValue("curr_code", dom);
						if(currCode!=null && currCode.trim().length()>0)
						{
							sql = " select count(*) from currency where curr_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMCURRCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					
						}
										
				
					else if(childNodeName.equalsIgnoreCase("acct_code__fp"))
					{
						acctCodeFp = genericUtility.getColumnValue("acct_code__fp", dom);
						
						//if (acctCodeFp != null && acctCodeFp.trim().length() > 0 )
						//{
							sql = " select count(*) from accounts where acct_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeFp);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt != 0) 
								{          	
									sql1 = " select active from accounts where acct_code = ? ";
									pstmt1 = conn.prepareStatement(sql1);
									pstmt1.setString(1, acctCodeFp);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										active = rs1.getString(1);
										if(!(active.equals("Y"))) 
										{
											errCode = "VMACCTA";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;
										}									
									}
									else
									{
										errCode = "VMACCTCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										break;
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								}
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					//}
				}
									
					else if(childNodeName.equalsIgnoreCase("cctr_code__fp"))
					{
						cctrCodeFp = genericUtility.getColumnValue("cctr_code__fp", dom);

						if (cctrCodeFp != null && cctrCodeFp.trim().length() > 0 )
						{
							sql = " select count(*) from costctr where cctr_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCodeFp);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMCCTRCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				
				//added by manish mhatre on 20-dec-2019
				//start manish
					else if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxcls = genericUtility.getColumnValue("tax_class", dom);

						if (taxcls != null && taxcls.trim().length() > 0 )
						{
							sql = " select count(*) from taxclass where tax_class = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxcls);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "TAXCLSNE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}   //end manish
				
				
			     //Added by Shubham.S.B on 01-02-2021
				// pinPattern Validation
				else if (childNodeName.equalsIgnoreCase("pin"))
				{
					pin = checkNull(genericUtility.getColumnValue("pin", dom));
					countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
					
					sql = "select pin_pattern from country where count_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, countCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						pin_pattern = checkNull(rs.getString("pin_pattern"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					System.out.println("pin_pattern is:::::"+pin_pattern);
					if (pin != null && pin.trim().length() > 0)
					{
						
						if (!pin.trim().matches(pin_pattern)) 
						{
							errCode = "VTINVLPIN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
					}
				}
				
				// pinPattern Validation ended
				
//					else if(childNodeName.equalsIgnoreCase("acct_code__adv"))
//					{
//						
//						acctCodeFp = genericUtility.getColumnValue("acct_code__adv", dom);
//					
//						if (acctCodeFp != null && acctCodeFp.trim().length() > 0 )
//						{
//							sql = " select count(*) from accounts where acct_code = ? ";
//							pstmt = conn.prepareStatement(sql);
//							pstmt.setString(1, acctCodeFp);
//							rs = pstmt.executeQuery();
//							if(rs.next())
//							{
//								cnt = rs.getInt(1);
//								if(cnt == 0) 
//								{
//									errCode = "ACTCODNOEE";
//									errList.add(errCode);
//									errFields.add(childNodeName.toLowerCase());
//									break;
//								}									
//							}
//							rs.close();
//							rs = null;
//							pstmt.close();
//							pstmt = null;
//					}
//					}
				
//					else if(childNodeName.equalsIgnoreCase("cctr_code__adv"))
//					{
//						cctrCodeFp = genericUtility.getColumnValue("cctr_code__adv", dom);
//						
//						if (cctrCodeFp != null && cctrCodeFp.trim().length() > 0 )
//						{
//							sql = " select count(*) from costctr where cctr_code = ? ";
//							pstmt = conn.prepareStatement(sql);
//							pstmt.setString(1, cctrCodeFp);
//							rs = pstmt.executeQuery();
//							if(rs.next())
//							{
//								cnt = rs.getInt(1);
//								if(cnt == 0) 
//								{
//									errCode = "CTRCODNOEE";
//									errList.add(errCode);
//									errFields.add(childNodeName.toLowerCase());
//									break;
//								}									
//							}
//							rs.close();
//							rs = null;
//							pstmt.close();
//							pstmt = null;
//					}
//					}
			}
			//added by priyanka
				
			     break;
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
			
								if(childNodeName.equalsIgnoreCase("reg_code"))
								{
									
									regCode = genericUtility.getColumnValue("reg_code", dom);
									if(regCode==null || regCode.trim().length()==0)
									{
										errCode = "VTRCODNULL";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										break;
									}
									else
									{
										sql=" select count(*) from reg_requirements where reg_code=?";
										pstmt = conn.prepareStatement(sql);
									    pstmt.setString(1, regCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
												cnt=rs.getInt(1);						
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										if(cnt==0)
										{
											errCode = "VTRCODEXT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;
										}
									}
									
								}
								 else if(childNodeName.equalsIgnoreCase("reg_date"))
								 {
									 regDate = genericUtility.getColumnValue("reg_date", dom);										
									 System.out.println(">>>>>>>>>>>regDate===="+regDate);
									 
									 if(regDate==null || regDate.trim().length()==0)
									 {
										 	errCode = "VTREGNULL";//entry message error
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;
									 }								
								
								 }
									
								 else if(childNodeName.equalsIgnoreCase("valid_upto"))
								 {			
									 validUpto = genericUtility.getColumnValue("valid_upto", dom);		
									 regDate = genericUtility.getColumnValue("reg_date", dom);	
									 System.out.println(">>>>>>>>>>>validUpto===="+validUpto);
									 if(validUpto==null || validUpto.trim().length()==0)
									 {
										 errCode = "VTVALNULL";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;
									 }
									 else
									 {
										 if(regDate!=null && regDate.trim().length()>0)
										 {
											     registerDate = Timestamp.valueOf(genericUtility.getValidDateString(regDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
												 validDate = Timestamp.valueOf(genericUtility.getValidDateString(validUpto, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
											   
												 if(validDate.equals(registerDate)||validDate.compareTo(registerDate)<0)
											    {
											    	errCode = "VTVALREGDT";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
													break;
											    }
										 }
										
									 }							
									
								 }							
							
							} 						
			}
				
			
			int errListSize = errList.size();
			cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
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
				//Commented and Added by sarita on 13NOV2017
				/*if(conn != null)
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
				conn = null;*/
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [TransporterIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	} 

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		int ctr = 0;
		int crDays = 0;
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String tranCode = "",regCode="",regDescr="";
		String tranCodePay = "";
		String groupCode = "";
		String tranName = "";
		String chqName = "";
		String stanCode = "";
		String stateCode = "";
		String countCode = "";
		String currCode = "";
		String contactCode = "";
		String currencyDescr = "";
		String crTerm = "";
		String contPers = "";
		String add1="";
		String emailadd="";
		String addr1 = "";
		String addr2 = "";
		String addr3 = "";
		String city = "";
		String pin = "";
		String state = "";
		String tele1 = "";
		String tele2 = "";
		String tele3 = "";
		String teleExt = "";
		String shName = "";
		String contPfx = "";
		String fax = "";
		String mail = "";
		String tranName1="";
		String contactPer="";
		String sql1 = "";
		String sql2 = "";
		int childNodeListLength = 0;
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs1 = null ;
		ResultSet rs2 = null ;
		
		int currentFormNo = 0;
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
	
			
			System.out.println("editFlag@@ : ["+editFlag+"]");
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
				System.out.println("Transporter itemchanged case 1");
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
			
			
			//Changed by Dadaso pawar on 02/04/15 [Start][W14LSUN006]
			if(currentColumn.trim().equalsIgnoreCase("prono_from"))
			{
				String proNoFrom = "",tranCodeL = "";
				tranCodeL = genericUtility.getColumnValue("tran_code", dom);
				proNoFrom = genericUtility.getColumnValue("prono_from", dom);
				proNoFrom = proNoFrom == null ? "" : proNoFrom.trim();
				tranCodeL = tranCodeL == null ? "" : tranCodeL.trim();
				
				System.out.println(" item change proNoFrom@@ : ["+proNoFrom+"]");
				System.out.println("tranCodeL@@ : ["+tranCodeL+"]");
				
				if(isClearProNoLast(tranCodeL,proNoFrom,conn))
				{
					valueXmlString.append( "<prono_last><![CDATA[]]></prono_last>\r\n" );
					valueXmlString.append( "<prono_to><![CDATA[]]></prono_to>\r\n" );// Changed by Dadaso pawar on 22/04/15 [D15ASUN006] [For make blank prono_to]
				}		
				
			}
			if(currentColumn.trim().equalsIgnoreCase("prono_to"))
			{
				String proNoTo = "",tranCodeL = "";
				tranCodeL = genericUtility.getColumnValue("tran_code", dom);
				proNoTo = genericUtility.getColumnValue("prono_to", dom);
				proNoTo = proNoTo == null ? "" : proNoTo.trim();
				tranCodeL = tranCodeL == null ? "" : tranCodeL.trim();
				
				System.out.println(" item change proNoTo@@ : ["+proNoTo+"]");
				System.out.println("tranCodeL@@ : ["+tranCodeL+"]");
				
				if(isClearProNoLast(tranCodeL,proNoTo,conn))
				{
					valueXmlString.append( "<prono_last><![CDATA[]]></prono_last>\r\n" );
				}
			}
			//Changed by Dadaso pawar on 02/04/15 [End] [W14LSUN006]
			if(currentColumn.trim().equalsIgnoreCase("tran_code"))
			{
				tranCode = genericUtility.getColumnValue("tran_code", dom);
				tranCodePay = genericUtility.getColumnValue("tran_code__pay", dom);
				groupCode = genericUtility.getColumnValue("group_code", dom);
				
				//changes done by priyanka as per pb code on 26/9/14
				if(tranCode!=null && tranCode.trim().length()>0)
				{
					valueXmlString.append("<tran_code__pay>").append("<![CDATA[" +  tranCode + "]]>").append("</tran_code__pay>");
					valueXmlString.append("<group_code>").append("<![CDATA[" +  tranCode + "]]>").append("</group_code>");
				}
				else
				{
					valueXmlString.append("<tran_code__pay>").append("<![CDATA["+" "+"]]>").append("</tran_code__pay>");
					valueXmlString.append("<group_code>").append("<![CDATA[" + " " + "]]>").append("</group_code>");
				}
															
			}
			else if(currentColumn.trim().equalsIgnoreCase("tran_name"))
			{
				tranName = genericUtility.getColumnValue("tran_name", dom);
				chqName = genericUtility.getColumnValue("chq_name", dom);
				
				if(tranName!=null && tranName.trim().length()>0)
				{
					valueXmlString.append("<chq_name>").append("<![CDATA[" +  tranName + "]]>").append("</chq_name>");
				}
				else
				{
					valueXmlString.append("<chq_name>").append("<![CDATA[" + "" + "]]>").append("</chq_name>");

				}
				
			}
			
			else if(currentColumn.trim().equalsIgnoreCase("stan_code"))
			{
				stanCode = genericUtility.getColumnValue("stan_code", dom);			
				sql1 = " select state_code,city from station where stan_code = ? ";    //city added by manish mhatre on 20-dec-2019
				pstmt1 =  conn.prepareStatement(sql1);
				pstmt1.setString(1, stanCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					stateCode =checkNull(rs1.getString(1));
					city = checkNull(rs1.getString(2));                //added by manish mhatre on 20-dec-2019
					sql2 = " select count_code from state where state_code = ? ";
					pstmt2 =  conn.prepareStatement(sql2);
					pstmt2.setString(1, stateCode);
					rs2 = pstmt2.executeQuery();
					if(rs2.next())
					{
						if(rs1.getString(1) != null)
							countCode = checkNull( rs2.getString(1));
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
				}
				if(stanCode != null && stanCode.trim().length() > 0)
				{
				valueXmlString.append("<state_code protect=\"1\">").append("<![CDATA[" +  stateCode + "]]>").append("</state_code>");
				valueXmlString.append("<count_code protect=\"1\">").append("<![CDATA[" +  countCode + "]]>").append("</count_code>");
				valueXmlString.append("<city protect=\"1\">").append("<![CDATA[" +  city + "]]>").append("</city>");   //added by manish mhatre on 20-dec-2019
				}
				else
				{
					valueXmlString.append("<state_code protect=\"1\">").append("<![CDATA[]]>").append("</state_code>");
					valueXmlString.append("<count_code protect=\"1\">").append("<![CDATA[]]>").append("</count_code>");
					valueXmlString.append("<city protect=\"1\">").append("<![CDATA[]]>").append("</city>");          //added by manish mhatre on 20-dec-2019
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}
			
			else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
			{
				currCode = genericUtility.getColumnValue("curr_code", dom);				
				
				sql1 = " select descr from currency where curr_code = ? ";
				pstmt1 =  conn.prepareStatement(sql1);
				pstmt1.setString(1, currCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					if(rs1.getString(1) != null)
						currencyDescr = checkNull(rs1.getString(1));
				}
				
				
				if(currCode != null && currCode.trim().length() > 0)
				{
					valueXmlString.append("<currency_descr>").append("<![CDATA[" +  currencyDescr + "]]>").append("</currency_descr>");
				}else
				{
					valueXmlString.append("<currency_descr>").append("<![CDATA[]]>").append("</currency_descr>");
				}				
				
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}
			
			else if(currentColumn.trim().equalsIgnoreCase("cr_term"))
			{
				crTerm = genericUtility.getColumnValue("cr_term", dom);
				
				sql1 = " select cr_days from crterm where cr_term = ? ";
				pstmt1 =  conn.prepareStatement(sql1);
				pstmt1.setString(1, crTerm);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					crDays =  rs1.getInt(1);
				}
				if(crTerm != null && crTerm.trim().length() > 0)
				{
					valueXmlString.append("<credit_prd>").append("<![CDATA[" +  crDays + "]]>").append("</credit_prd>");
				}else
				{
					valueXmlString.append("<credit_prd>").append("<![CDATA[]]>").append("</credit_prd>");
				}
								
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}
			
			else if(currentColumn.trim().equalsIgnoreCase("contact_code"))
			{
				contactCode = genericUtility.getColumnValue("contact_code", dom);
				//changes by priyanka as per pb code on 29/09/14
				if(contactCode != null && contactCode.trim().length() > 0)
				{
				
					sql1 = " select name, sh_name, cont_pers, cont_pfx, addr1, addr2, " +   
								"addr3, city, pin, state_code, count_code, tele1, tele2, tele3,  " + 
								"tele_ext, fax, email_addr from contact where contact_code = ? ";
					pstmt1 =  conn.prepareStatement(sql1);
					pstmt1.setString(1, contactCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						if(rs1.getString(1) != null)
							tranName1 =checkNull( rs1.getString(1));
						if(rs1.getString(2) != null)
							shName = checkNull(rs1.getString(2));
						if(rs1.getString(3) != null)
							contactPer = checkNull(rs1.getString(3));
						if(rs1.getString(4) != null)
							contPfx = checkNull(rs1.getString(4));
						if(rs1.getString(5) != null)
							addr1 =checkNull(rs1.getString(5));
						if(rs1.getString(6) != null)
							addr2 = checkNull(rs1.getString(6));
						if(rs1.getString(7) != null)
							addr3 = checkNull(rs1.getString(7));
						if(rs1.getString(8) != null)
							city = checkNull(rs1.getString(8));
						if(rs1.getString(9) != null)
							pin = checkNull(rs1.getString(9).trim());
						
						if(rs1.getString(10) != null)
							stateCode =checkNull( rs1.getString(10));
						if(rs1.getString(11) != null)
							countCode = checkNull(rs1.getString(11));
						if(rs1.getString(12) != null)
							tele1 = checkNull(rs1.getString(12));
						if(rs1.getString(13) != null)
							tele2 = checkNull(rs1.getString(13));
						if(rs1.getString(14) != null)
							tele3 = checkNull(rs1.getString(14));
						if(rs1.getString(15) != null)
							teleExt = checkNull(rs1.getString(15));
						if(rs1.getString(16) != null)
							fax = checkNull(rs1.getString(16));
						if(rs1.getString(17) != null)
							mail = checkNull(rs1.getString(17));
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					tranName = genericUtility.getColumnValue("tran_name", dom);
					
					if(tranName==null||tranName.trim().length()==0)
					{
						valueXmlString.append("<tran_name>").append("<![CDATA[" +  tranName1 + "]]>").append("</tran_name>");
						valueXmlString.append("<sh_name>").append("<![CDATA[" +  shName + "]]>").append("</sh_name>");
					}
					
					contPers = genericUtility.getColumnValue("cont_pers", dom);
					if(contPers==null||contPers.trim().length()==0)
					{
						valueXmlString.append("<cont_pfx>").append("<![CDATA[" +  contPfx + "]]>").append("</cont_pfx>");
						valueXmlString.append("<cont_pers>").append("<![CDATA[" +  contactPer + "]]>").append("</cont_pers>");
					}
					
					add1 = genericUtility.getColumnValue("addr1", dom);
					if(add1==null||add1.trim().length()==0)
					{
						valueXmlString.append("<addr1>").append("<![CDATA[" +  addr1 + "]]>").append("</addr1>");
						valueXmlString.append("<addr2>").append("<![CDATA[" +  addr2 + "]]>").append("</addr2>");
						valueXmlString.append("<addr3>").append("<![CDATA[" +  addr3 + "]]>").append("</addr3>");
					//	valueXmlString.append("<city>").append("<![CDATA[" +  city + "]]>").append("</city>");   //commented by manish mhatre [city pick from station code]
						valueXmlString.append("<pin>").append("<![CDATA[" +  pin + "]]>").append("</pin>");
					  //valueXmlString.append("<state_code protect=\"1\">").append("<![CDATA[" +  stateCode + "]]>").append("</state_code>");    //commented by manish mhatre  [state code pick up from station code]
					  //valueXmlString.append("<count_code protect=\"1\">").append("<![CDATA[" +  countCode + "]]>").append("</count_code>");    //commented by manish mhatre  [count code pick up from station code]
						valueXmlString.append("<tele1>").append("<![CDATA[" +  tele1 + "]]>").append("</tele1>");
						valueXmlString.append("<tele2>").append("<![CDATA[" +  tele2 + "]]>").append("</tele2>");
						valueXmlString.append("<tele3>").append("<![CDATA[" +  tele3 + "]]>").append("</tele3>");
						valueXmlString.append("<tele_ext>").append("<![CDATA[" +  teleExt + "]]>").append("</tele_ext>");
						valueXmlString.append("<fax>").append("<![CDATA[" +  fax + "]]>").append("</fax>");
					}
					emailadd = genericUtility.getColumnValue("email_addr", dom);
					if(emailadd==null||emailadd.trim().length()==0)
					{
						valueXmlString.append("<email_addr>").append("<![CDATA[" +  mail + "]]>").append("</email_addr>");
					}

				}
				
				else
				{
					valueXmlString.append("<tran_name>").append("<![CDATA[]]>").append("</tran_name>");
					valueXmlString.append("<sh_name>").append("<![CDATA[]]>").append("</sh_name>");
					valueXmlString.append("<cont_pfx>").append("<![CDATA[]]>").append("</cont_pfx>");
					valueXmlString.append("<cont_pers>").append("<![CDATA[]]>").append("</cont_pers>");
					valueXmlString.append("<addr1>").append("<![CDATA[]]>").append("</addr1>");
					valueXmlString.append("<addr2>").append("<![CDATA[]]>").append("</addr2>");
					valueXmlString.append("<addr3>").append("<![CDATA[]]>").append("</addr3>");
				//	valueXmlString.append("<city>").append("<![CDATA[]]>").append("</city>");     //commented by manish mhatre  [city pick up from station code]
					valueXmlString.append("<pin>").append("<![CDATA[]]>").append("</pin>");
//					valueXmlString.append("<state_code protect=\"1\">").append("<![CDATA[]]>").append("</state_code>");   //commented by manish mhatre  [state code pick up from station code]
//					valueXmlString.append("<count_code protect=\"1\">").append("<![CDATA[]]>").append("</count_code>");   //commented by manish mhatre  [count code pick up from station code]
					valueXmlString.append("<tele1>").append("<![CDATA[]]>").append("</tele1>");
					valueXmlString.append("<tele2>").append("<![CDATA[]]>").append("</tele2>");
					valueXmlString.append("<tele3>").append("<![CDATA[]]>").append("</tele3>");
					valueXmlString.append("<tele_ext>").append("<![CDATA[]]>").append("</tele_ext>");
					valueXmlString.append("<fax>").append("<![CDATA[]]>").append("</fax>");
					valueXmlString.append("<mail_option>").append("<![CDATA[]]>").append("</mail_option>");
					valueXmlString.append("<stan_code protect=\"0\">").append("").append("</stan_code>");
				}
								
			}
			valueXmlString.append("</Detail1>");
			break;

			//added by priyanka 
			//case 2 itemchange
		 case 2:

			System.out.println("Transporter itemchanged case 2");
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
			System.out.println("CURRENT COLUMN Case 2 Transporter *******["+currentColumn+"]");
						
			if (currentColumn.trim().equalsIgnoreCase("reg_code"))
			{
				System.out.println("reg_code========itemChanged");
				regCode=genericUtility.getColumnValue("reg_code", dom);
				sql1="select descr from reg_requirements where reg_code=?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, regCode);
				
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					regDescr = checkNull(rs1.getString("descr"));
					
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("Reg Code description:=="+regDescr);
				
				if(regDescr == null)
				{
					valueXmlString.append("<descr>").append("<![CDATA["+""+"]]>").append("</descr>");
				}
				else
				{					
					valueXmlString.append("<descr>").append("<![CDATA["+regDescr+"]]>").append("</descr>");

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
					if(pstmt1 != null)
						pstmt1.close();
					if(pstmt2 != null)
						pstmt2.close();
					if(rs1 != null)
						rs1.close();
					rs1 = null;
					if(rs2 != null)
						rs2.close();
					rs2 = null;
					pstmt1 = null;
					pstmt2 = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}
	//Added by Dadadso pawar on 05/14/15 [Start]
	private boolean isClearProNoLast(String tranCode,String proNoFrom ,Connection conn) throws ITMException,Exception
	{
		ResultSet rs1 = null;
		PreparedStatement pstmt1 = null;
		String sql1 = "",proNoFromD = "";
		try
		{
			sql1 = "SELECT PRONO_FROM,PRONO_TO,PRONO_LAST FROM TRANSPORTER WHERE TRAN_CODE = ?";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, tranCode);
			rs1 = pstmt1.executeQuery();
			if(rs1.next())
			{
				proNoFromD = rs1.getString("PRONO_FROM") == null ? "" : rs1.getString("PRONO_FROM").trim();					
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;
			System.out.println("isClearProNoLast proNoFromD :["+proNoFromD+"] proNoFrom : ["+proNoFrom+"]");			
			if(proNoFromD.equalsIgnoreCase(proNoFrom))
			{
				return false;
			}
			else
			{
				return true;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			if(rs1 != null)
			{
				rs1.close();
				rs1 = null;
			}
			if(pstmt1 != null)
			{
				pstmt1.close();
				pstmt1 = null;
			}
		}
	}
	
	//Added by Dadadso pawar on 05/14/15 [End]
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, errorCode);			
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
}
