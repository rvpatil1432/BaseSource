package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;


import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;


@Stateless
public class GroupSchemeIC extends ValidatorEJB implements GroupSchemeICLocal,GroupSchemeICRemote {
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

			int currentFormNo =0,recCnt=0,ct=0,n1=0,pcp1=0,pco1=0,ct3=0;
		
			
			String schemeCode="",purchaseBaser="",keyFlag="",schemeAllowence="" ,itemCode="",pcp="",pco="",descr="",shdescr="",schemeType="";
			
			double exchRate=0,sch_allowence=0,purc_base=0;

			try
			{
				
				System.out.println("GroupSchemeIC.java ::wfvaldata called !!!!!!");
		
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
 
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
		
						
						if(childNodeName.equalsIgnoreCase("scheme_code"))
						{   
							schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
							System.out.println("scheme_code!!!! "+schemeCode);

							sql = "select key_flag from transetup where tran_window='w_groupscheme'";
							
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
								if(schemeCode == null || schemeCode.trim().length() == 0  )
								{
									errCode = "VMSCHCD1";//Scheme code should not be null 
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("scheme Code should not be blank", errCode, userId);
									System.out.println("Error due to scheme Code blank!!");
								}
								else
								{
									sql = " select count (*) from scheme_applicability where scheme_code = ? and prod_sch ='Y' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct = rs.getInt(1);	
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									
									if(ct == 0)
									{
										errCode = "VMSCHEME1";//Invalid Scheme code!!!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										errString = getErrorString("Invalid Scheme code", errCode, userId);
										System.out.println("Error due to scheme  code not exist in master!!");
									}
									
									sql = " select count (*) from sch_group_def where scheme_code = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										n1 = rs.getInt(1);	
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									
									if(n1 == 1)
									{
										errCode = "VMGSCD";//Invalid Scheme code!!!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										errString = getErrorString("Invalid Scheme code", errCode, userId);
										System.out.println("Scheme Code entered is already exists for Group Scheme.!!");
									}
									
								

								 }

							}//end of case transetup


						}
						
						
						if(childNodeName.equalsIgnoreCase("descr"))
						{    
							descr = genericUtility.getColumnValue("descr", dom);
							if( descr == null)	
							{
								errCode = "VEADN3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty description", errCode, userId);
								System.out.println("Description should not be blank!!!");
							}
							
						}
						
						if(childNodeName.equalsIgnoreCase("purc_base"))
						{ 
							
							schemeType=checkNull(this.genericUtility.getColumnValue("scheme_type", dom));
							purchaseBaser = genericUtility.getColumnValue("purc_base", dom);
							
							if (purchaseBaser!=null )
							{
								purc_base= Double.parseDouble(purchaseBaser);
							}
							if( (purchaseBaser == null || purc_base <= 0) && !"3".equalsIgnoreCase(schemeType))	
							{
								errCode = "UVGPSCHPB";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty purchase Baser", errCode, userId);
								System.out.println("purchase Baser should not be blank!!!");
							}
							
						}
						
						if(childNodeName.equalsIgnoreCase("sch_allowence"))
						{    
							schemeType=checkNull(this.genericUtility.getColumnValue("scheme_type", dom));
							schemeAllowence = genericUtility.getColumnValue("sch_allowence", dom);
							
							if (schemeAllowence !=null )
							{
								sch_allowence= Double.parseDouble(schemeAllowence);
							}
							if( (schemeAllowence == null || sch_allowence <= 0) && !"3".equalsIgnoreCase(schemeType))	
							{
								errCode = "UVGPSCHSA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty Scheme Allowence", errCode, userId);
								System.out.println("Scheme Allowence should not be blank!!!");
							}
							
							
							
							
							
						}
						
						if(childNodeName.equalsIgnoreCase("discount"))
							{ 
							
							double discount =Double.parseDouble(genericUtility.getColumnValue("discount", dom));
							schemeType = genericUtility.getColumnValue("scheme_type", dom);
							if( "2".equalsIgnoreCase(schemeType))	
							{
								
								if(discount == 0)
								{
								errCode = "UVGPSCHDP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty Discount percentage", errCode, userId);
								System.out.println("Discount percentage should not be blank!!!");
								}
							}
							
						}
						
						
						
						
						if(childNodeName.equalsIgnoreCase("prod_code__pur"))
						{    
							
							
							pcp =genericUtility.getColumnValue("prod_code__pur", dom);
							
							
							if( pcp == null)
							{
								
								System.out.println("Productcode Pur null");
							}
							
							else 
							{
								
							sql = " select count (*) from item where product_code = ?   ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, pcp);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								pcp1 = rs.getInt(1);	
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							System.out.println("pcp1 !!!"+pcp1);
							if(pcp1 == 0)
							{
								errCode = "VMGSPROC";//Invalid P code!!!
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Invalid Product code", errCode, userId);
								System.out.println("Error due to Product Code does not exists in Product master.!!");
							}
						  }
						}
						
						
						if(childNodeName.equalsIgnoreCase("prod_code__off"))
						{    
							pco = genericUtility.getColumnValue("prod_code__off", dom);
							
							
							if( pco == null)
							{
								
								System.out.println("Productcode offer null");
							}
							
							else 
							{
							sql = " select count (*) from item where product_code = ?   ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, pco);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								pco1 = rs.getInt(1);	
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							System.out.println("pco1 !!!"+pco1);
							if(pco1 == 0)
							{
								errCode = "VMGSPROC";//Invalid P code!!!
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Invalid Product code", errCode, userId);
								System.out.println("Error due to Product Code does not exists in Product master.!!");
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

						if(childNodeName.equalsIgnoreCase("scheme_code"))
						{   
							schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
							System.out.println("scheme_code!!!! "+schemeCode);

							sql = "select key_flag from transetup where tran_window='w_groupscheme'";
							
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
								if(schemeCode == null || schemeCode.trim().length() == 0  )
								{
									errCode = "VMSCHCD1";//scheme code should not be null for manually transetup
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("scheme Code should not be blank", errCode, userId);
									System.out.println("Error due to scheme Code blank!!");
								}
								else
								{
									sql = " select count (*) from scheme_applicability where scheme_code = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct = rs.getInt(1);	
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									
									if(ct == 0)
									{
										errCode = "VMSCHEME1";//Invalid Scheme code!!!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										errString = getErrorString("Invalid Scheme code", errCode, userId);
										System.out.println("Error due to scheme  code not exist in master!!");
									}

								 }

							}
						
						}
						
						if(childNodeName.equalsIgnoreCase("item_code"))
						{    
							itemCode = genericUtility.getColumnValue("item_code", dom);
							if( itemCode == null)	
							{
								errCode = "VMITEMCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty item code ", errCode, userId);
								System.out.println("item code should not be blank!!!");
							}
							
						
							else
							{  
								schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
								SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
								
								Timestamp currDate = new java.sql.Timestamp(System.currentTimeMillis());
								String currDateStr = sdf.format(currDate);
									
								Timestamp toDate= Timestamp.valueOf(genericUtility.getValidDateString(currDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								System.out.println("itemcodecheckDate!!"+toDate);
								sql = " select count (*) from scheme_applicability a ,sch_pur_items b where a.scheme_code =  b. scheme_code and  a.app_from<= ? and a.valid_upto>= ? and a.prod_sch='Y' and b.item_code = ? ";
								 
								pstmt = conn.prepareStatement(sql);
								 pstmt.setTimestamp(1, toDate);
								 pstmt.setTimestamp(2, toDate);
										 pstmt.setString(3, itemCode);
					
									
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct3 = rs.getInt(1);	
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									if(ct3 > 0)
									{
										errCode = "VTICAGP";//Invalid item code!!!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										errString = getErrorString("Invalid item code", errCode, userId);
										System.out.println("Error due to item code  exist in Group Scheme!!");
									}
								
								
								
								
								
								
								pcp = genericUtility.getColumnValue("prod_code__pur", dom1);
								/*sql = " select count (*) from item where item_code = ? and product_code = ?  ";*/
								sql = " select count (*) from item where item_code = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								/*pstmt.setString(2, pcp);*/
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
									errCode = "VTIMPCPU";//Invalid item code!!!
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("Invalid item code", errCode, userId);
									System.out.println("Error due to item code not exist in Purchase group!!");
								}

							 }
							
						}
						
						
						
					}break;// case 2 end
					
					
				case 3:
					
					Node currDetailF=null;
					/*int count=0;*/
					NodeList detailList1 = dom2.getElementsByTagName("Detail1");
					
					
					
						currDetailF = detailList1.item(0);
					pco = genericUtility.getColumnValueFromNode("prod_code__off",currDetailF);
					System.out.println("productCodeoffer:"+pco);
					
					
					parentNodeList = dom.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("@@@@@@@@@@@@childNodeListLength["+childNodeListLength+"]");
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						
						
						

						if(childNodeName.equalsIgnoreCase("scheme_code"))
						{   
							schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
							System.out.println("scheme_code!!!! "+schemeCode);

							sql = "select key_flag from transetup where tran_window='w_groupscheme'";
							
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
								if(schemeCode == null || schemeCode.trim().length() == 0  )
								{
									errCode = "VMSCHCD1";//scheme code should not be null for manually transetup
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("scheme Code should not be blank", errCode, userId);
									System.out.println("Error due to scheme Code blank!!");
								}
								else
								{
									sql = " select count (*) from scheme_applicability where scheme_code = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct = rs.getInt(1);	
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									
									if(ct == 0)
									{
										errCode = "VMSCHEME1";//Invalid Scheme code!!!
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
										errString = getErrorString("Invalid Scheme code", errCode, userId);
										System.out.println("Error due to scheme  code not exist in master!!");
									}

								 }

							}
						}
						
						if(childNodeName.equalsIgnoreCase("item_code"))
						{    
							itemCode = genericUtility.getColumnValue("item_code", dom);
							if( itemCode == null)	
							{
								errCode = "VMITEMCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								errString = getErrorString("Empty item code ", errCode, userId);
								System.out.println("item code should not be blank!!!");
							}
							
						
							else
							{
								/*sql = " select count (*) from item where item_code = ? and product_code = ?  ";*/
								sql = " select count (*) from item where item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								/*pstmt.setString(2, pco);*/
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
									errCode = "VTIMPCOF";//Invalid item code!!!
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									errString = getErrorString("Invalid item code", errCode, userId);
									System.out.println("Error due to item code not exist offer group  !!");
								}

							 }
							
						}
						
						
						
					}

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
				System.out.println("Exception : [GeoupSchemeIC][itemChanged( String, String )] :==>\n" + e.getMessage());
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
			
			String columnValue="",loginSite="";
			
			int currentFormNo = 0;
			int childNodeListLength = 0;
			int length = 0;
			int ctr = 0;
			
			String ordetType = "",schemeCode="",itemCode="",itemDescr="",schemeType="";
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
				System.out.println("GroupSchemeIC.java ::: Itemchanged called !!!!!");
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			conn = getConnection();
		
				conn.setAutoCommit(false);
				connDriver = null;
				if (objContext != null && objContext.trim().length() > 0)
				{
					currentFormNo = Integer.parseInt(objContext);
				}
				valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> ");
				System.out.println("Entry in itemChanged with form no 08 JULY 2017 3:25    :"+currentFormNo);
				switch (currentFormNo)
				{
				
					case 1:
						parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();

						ctr = 0; 
						valueXmlString.append("<Detail1>");
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
						
						
						System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
						if(currentColumn.trim().equalsIgnoreCase("itm_default"))
						{
							
							
							schemeType=checkNull(this.genericUtility.getColumnValue("scheme_type", dom));
							
							
								System.out.println("in side itm_default:::::::::::::::::::::");
								valueXmlString.append("<discount protect =\"1\">").append("<![CDATA[").append(0).append("]]>").append("</discount>\r\n");

						}

						if(currentColumn.trim().equalsIgnoreCase("scheme_type"))
						{
							
							
							schemeType=checkNull(this.genericUtility.getColumnValue("scheme_type", dom));
							
							System.out.println("schemeType:::::::::::::::::::::"+schemeType);
							if("2".equalsIgnoreCase(schemeType) )
							{
								System.out.println("inside if schemeType:::::::::::::::::::::"+schemeType);
								valueXmlString.append("<discount protect =\"0\">").append("<![CDATA[").append(0).append("]]>").append("</discount>\r\n");

							}
							else
							{
								valueXmlString.append("<discount protect =\"1\">").append("<![CDATA[").append(0).append("]]>").append("</discount>\r\n");
							}
							
				
						}

						String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
						
						loginSite = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");

						String chgUser = this.genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
					     
					    String chgTerm = this.genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
					    
						valueXmlString.append("</Detail1>"); // close tag
						System.out.println("NANDKUMAR itemchanged case 1 valueXmlString : "+valueXmlString);
				        break;
				        
				        
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
						if(currentColumn.trim().equalsIgnoreCase("item_code"))
						{
							
							
							itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
							if(itemCode != null && itemCode.trim().length() > 0 )
							{
								sql = "select descr from item where item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									itemDescr=rs.getString("descr");
									/*ITEMDESCR = CHECKNULL(RS.GETSTRING("DESCR") ==NULL?"":RS.GETSTRING("DESCR"));
									SYSTEM.OUT.PRINTLN("DESCRITEM ---->>>["+ITEMDESCR +"]");
									VALUEXMLSTRING.APPEND("<DESCR>").APPEND("<![CDATA["+ITEMDESCR+"]]>").APPEND("</DESCR>");*/
									valueXmlString.append("<descr>").append("<![CDATA["+itemDescr+"]]>").append("</descr>");
									setNodeValue( dom, "descr", (String) getAbsString( itemDescr ) );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								/*valueXmlString.append("<descr>").append("<![CDATA["+itemDescr+"]]>").append("</descr>");
								setNodeValue( dom, "descr", (String) getAbsString( itemDescr ) );*/
							}
							
							
						/*	itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							itemDescr = isExist("item", "item_code", itemCode, conn, "descr");
							valueXmlString.append("<descr>").append("<![CDATA["+itemDescr+"]]>").append("</descr>");
							setNodeValue( dom, "descr", (String) getAbsString( itemDescr ) );*/
						}	
						valueXmlString.append("</Detail2>");
						break;
						// case 2 end
					
					
					
				case 3 :
					System.out.println("**********************In case 3 ***********************8");
					parentNodeList = dom.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0; 
					valueXmlString.append("<Detail3>");
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
					if(currentColumn.trim().equalsIgnoreCase("item_code"))
					{
						
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						if(itemCode != null && itemCode.trim().length() > 0 )
						{
							sql = "select descr from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								itemDescr=rs.getString("descr");
								/*ITEMDESCR = CHECKNULL(RS.GETSTRING("DESCR") ==NULL?"":RS.GETSTRING("DESCR"));
								SYSTEM.OUT.PRINTLN("DESCRITEM ---->>>["+ITEMDESCR +"]");
								VALUEXMLSTRING.APPEND("<DESCR>").APPEND("<![CDATA["+ITEMDESCR+"]]>").APPEND("</DESCR>");*/
								valueXmlString.append("<descr>").append("<![CDATA["+itemDescr+"]]>").append("</descr>");
								setNodeValue( dom, "descr", (String) getAbsString( itemDescr ) );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							/*valueXmlString.append("<descr>").append("<![CDATA["+itemDescr+"]]>").append("</descr>");
							setNodeValue( dom, "descr", (String) getAbsString( itemDescr ) );*/
						}
						/*else
						{
							valueXmlString.append("<descr>").append("<![CDATA[ ]]>").append("</descr>");
						}
						*/
						
						
						
						/*itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						itemDescr = isExist("item", "item_code", itemCode, conn, "descr");*/
						
					}	
					valueXmlString.append("</Detail3>");
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




		private void setNodeValue(Document dom, String nodeName, String nodeVal) throws Exception  {
			
			Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

			if( tempNode != null )
			{
			if( tempNode.getFirstChild() == null )
			{
			CDATASection cDataSection = dom.createCDATASection( nodeVal );
			tempNode.appendChild( cDataSection );
			}
			else
			{
			tempNode.getFirstChild().setNodeValue(nodeVal);
			}
			}
			tempNode = null;
			
			// TODO Auto-generated method stub
			
		}




		private Object getAbsString(String str) {
			//Object str;
			return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
			
			// TODO Auto-generated method stub
			
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
