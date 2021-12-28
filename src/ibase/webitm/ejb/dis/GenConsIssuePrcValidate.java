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
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



@Stateless
public class GenConsIssuePrcValidate  extends ValidatorEJB implements GenConsIssuePrcValidateLocal,GenConsIssuePrcValidateRemote
{

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("------------@ wfvalData method called-----------------");
		System.out.println("Xml String : ["+xmlString+"]");
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println("below genericUtility--------------->>>>>>>>>");
		try
		{
			
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
				System.out.println("xmlString d" + xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
				System.out.println("xmlString1 f" + xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
				System.out.println("xmlString2 f" + xmlString2);
			}
			
//			dom = parseString(xmlString);
//			dom1 = parseString(xmlString1);
//			dom2 = parseString(xmlString2);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : budgetgroup.java : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		
		return errString;
	} //end of wfValData 
	
	
	 public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
		{
		
		
		  	String cons_order__fr,cons_order__to,site_code = "";
		  	String order_date__to,order_date__fr = "";
			NodeList parentNodeList = null;
			NodeList childNodeList = null;
			Node parentNode = null;
			Node childNode = null;
			int ctr=0;
			String childNodeName = null;
			String errString = "";
			Connection conn = null;
			PreparedStatement pstmt = null ;
			ResultSet rs = null;
			String sql = "";
			int cnt=0;
			int currentFormNo=0;
			int childNodeListLength;
			ConnDriver connDriver = new ConnDriver();
			String consOrdFrom = null;
			String SiteCode = null;
			String consOrdTo = null;
			String fromDateStr = null;
			String toDateStr = null;
			Timestamp fromDate = null;
			String mdate = null;
			Timestamp toDate = null;
			Timestamp mtdate = null;
			String userId="",errCode="",locgrp ="";
			//GenericUtility genericUtility = GenericUtility.getInstance();
		    String	bud_group_code ="",acct_code="",cctr_code="",anal_code="",descr="",sh_descr =""; 
	        try
			   {
				//Changes and Commented By Poonam on 08-06-2016 :START
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				//Changes and Commented By Poonam on 08-06-2016 :END

				connDriver = null;
				userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				System.out.println("user ID form XtraParam : "+userId);
				
			
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
					
					
						if (childNodeName.equalsIgnoreCase("cons_order__fr"))
						{
							cons_order__fr = genericUtility.getColumnValue("cons_order__fr",dom);							


							if (cons_order__fr == null || cons_order__fr.trim().length() == 0 )
							{
								System.out.println("cons_order__fr null validation fire");
									errCode = "CONSORFRN";
									errString = getErrorString("cons_order__fr",errCode,userId);
									break;
							
							}else
							{

								
								sql = "select count(1) from consume_ord where cons_order = ? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,cons_order__fr);
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
									System.out.println("cons_order__fr not exist validation fire");
									errCode = "CONSORFRNE";
									errString = getErrorString("cons_order__fr",errCode,userId);
									break;
								}
								
								
								

							
							}
	 }
						
						
						else if (childNodeName.equalsIgnoreCase("cons_order__to"))
						{
							cons_order__to = genericUtility.getColumnValue("cons_order__to",dom);							


							if (cons_order__to == null || cons_order__to.trim().length() == 0 )
							{
								System.out.println("cons_order__to null validation fire");
									errCode = "CONSORTON";
									errString = getErrorString("cons_order__to",errCode,userId);
									break;
							
							}else
							{
								
								sql = "select count(1) from consume_ord where cons_order = ? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,cons_order__to);
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
									System.out.println("cons_order__fr not exist validation fire");
									errCode = "CONSORTONE";
									errString = getErrorString("cons_order__to",errCode,userId);
									break;
								}
								
								
								
								
							}
	 }
						
						
						else if (childNodeName.equalsIgnoreCase("order_date__fr"))
						{
							 fromDateStr = genericUtility.getColumnValue( "order_date__fr", dom );
							 System.out.println("dfhfhfghfgvfvb hjkddfgrrg"+fromDateStr);
							  toDateStr = genericUtility.getColumnValue( "order_date__to", dom );
							  System.out.println("dfhfhfghfgvfvb hdssdswererjkddfgrrg"+toDateStr);
							 
							  cons_order__fr = genericUtility.getColumnValue("cons_order__fr",dom);
							//order_date__fr = genericUtility.getColumnValue("order_date__fr",dom);
							
							System.out.println("dates here"+fromDate+""+toDate);
									System.out.println("in date validation");	
									
									if (fromDateStr == null || fromDateStr.trim().length() == 0 )
									{
										errCode = "POCONSODFE";
										errString = getErrorString("order_date__fr",errCode,userId);
										break;
									}else{
									
										if (fromDateStr != null || fromDateStr.trim().length() > 0 )
										{
											
											if (toDateStr != null)
											{
												
												fromDateStr = genericUtility.getValidDateString( fromDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
											     fromDate = java.sql.Timestamp.valueOf( fromDateStr + " 00:00:00.0" );
												
											     
											     toDateStr = genericUtility.getValidDateString( toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
												 toDate =java.sql.Timestamp.valueOf( toDateStr + " 00:00:00.0");
								        		System.out.println("fromDate is after toDate");
												
										if(fromDate.compareTo(toDate)>0)
										{
											
										    System.out.println("order_date__fr main validation fire");
											errCode = "CONSDAFRGR";
											errString = getErrorString("order_date__fr",errCode,userId);
											break;
										}
									
										Calendar cal = Calendar.getInstance();
									SimpleDateFormat sdf = new  SimpleDateFormat(genericUtility.getApplDateFormat());
									mdate = sdf.format(cal.getTime());
									mdate = genericUtility.getValidDateString(mdate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
									
									
									   java.sql.Timestamp ordatfr = null;
									
										sql = "select ORDER_DATE from consume_ord where CONS_ORDER = '" + cons_order__fr + "'";

										pstmt = conn.prepareStatement( sql );
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											ordatfr = rs.getTimestamp( "ORDER_DATE" );
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									
									
									System.out.println("date is  >>>>:::::"+mtdate);
									
							
									
									if(ordatfr.compareTo(fromDate)>0)
									{
						        		System.out.println("mtdate vvvvisweweegrgrg after toDate");
						        	
									    System.out.println("order_date__fr main validation fire");
										errCode = "POCONSBDTR";
										errString = getErrorString("order_date__fr",errCode,userId);
										break;
									}
										}
									}
						}
						 }
						
						else if (childNodeName.equalsIgnoreCase("order_date__to"))
						{
							 toDateStr = genericUtility.getColumnValue( "order_date__to", dom );
							 cons_order__to = genericUtility.getColumnValue("cons_order__to",dom);
							 
							 System.out.println("dfhfhfghfgvfvdvfvb hjk"+toDateStr);
							 if (toDateStr == null || toDateStr.trim().length() == 0 )
								{
									errCode = "POCONSODTE";
									errString = getErrorString("order_date__to",errCode,userId);
									break;
								}
						else{
							
							 
							 toDateStr = genericUtility.getValidDateString( toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							 toDate =java.sql.Timestamp.valueOf( toDateStr + " 00:00:00.0");
							
							    //order_date__to = genericUtility.getColumnValue("order_date__to",dom);
							 
							    Calendar cal = Calendar.getInstance();
								SimpleDateFormat sdf = new  SimpleDateFormat(genericUtility.getApplDateFormat());
						
								 java.sql.Timestamp ordatto = null;
									
									sql = "select ORDER_DATE from consume_ord where CONS_ORDER = '" + cons_order__to + "'";

									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										ordatto = rs.getTimestamp( "ORDER_DATE" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								
								
								System.out.println("date is  >>>>:::::"+mtdate);
								
						
						          if(ordatto.compareTo(toDate)>0)
								{
					        		System.out.println("mtdatfrtttyyytyyyedsdsd is after toDate");
					        	
								    System.out.println("order_date__fr main validation fire");
									errCode = "POCONSBDTR";
									errString = getErrorString("order_date__fr",errCode,userId);
									break;
								}
						}
						}
						
						
				
						
						
						else if (childNodeName.equalsIgnoreCase("site_code"))
						{
							site_code = genericUtility.getColumnValue("site_code",dom);							


							if (site_code == null || site_code.trim().length() == 0 )
							{
								System.out.println("site_code null validation fire");
									errCode = "CONSSITEN";
									errString = getErrorString("site_code",errCode,userId);
									break;
							
							}else
							{
								
								sql = "select count(1) from site where site_code = ? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,site_code);
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
									errCode = "CONSSITENE";
									errString = getErrorString("site_code",errCode,userId);
									break;
								}
							
							}
	 }
						
						else if (childNodeName.equalsIgnoreCase("loc_group"))
						{   System.out.println("deepak");
						    locgrp = genericUtility.getColumnValue("loc_group",dom);
						    System.out.println("loc group if-->["+locgrp+"]");
						    if (locgrp != null && locgrp.trim().length() > 0 )
							{
						    System.out.println("loc code else-->["+locgrp+"]");
							sql = "select count(1) from location where loc_group = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,locgrp);
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
								
								errCode = "CONSLOCNE";
								errString = getErrorString("loc_group",errCode,userId);
								break;
							}
							
						}	
						    

							 System.out.println("ENTER IN COMB VAL");
							 consOrdFrom = genericUtility.getColumnValue( "cons_order__fr", dom );
							 consOrdTo = genericUtility.getColumnValue( "cons_order__to", dom );
							 fromDateStr = genericUtility.getColumnValue( "order_date__fr", dom );
							 toDateStr = genericUtility.getColumnValue( "order_date__to", dom );
							 SiteCode = genericUtility.getColumnValue( "site_code", dom );
							
							 
							 fromDateStr = genericUtility.getValidDateString( fromDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
							 fromDate = java.sql.Timestamp.valueOf( fromDateStr + " 00:00:00.0" );
						     
							 toDateStr = genericUtility.getValidDateString( toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							 toDate =java.sql.Timestamp.valueOf( toDateStr + " 00:00:00.0");
							
							 sql = "SELECT COUNT(1) FROM CONSUME_ORD"+
						                " WHERE SITE_CODE__ORD = ? AND CONS_ORDER between ? AND ?"+ 
						                "AND ORDER_DATE between ? AND ?";
								

								System.out.println(sql+"1st sql");
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, SiteCode);
							    pstmt.setString(2, consOrdFrom);
								pstmt.setString(3, consOrdTo);
								pstmt.setTimestamp( 4, fromDate );
							    pstmt.setTimestamp( 5, toDate );
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
									
									errCode = "CONSCOMB";
									errString = getErrorString("",errCode,userId);
									break;
								}
					 
						    
						}	
						
						
//							 
//								
//								
//						       if(childNodeName.equalsIgnoreCase("cons_order__fr") || childNodeName.equalsIgnoreCase("cons_order__to") ||
//								childNodeName.equalsIgnoreCase("order_date__fr") || childNodeName.equalsIgnoreCase("site_code"))
//								{
//								
//							
//								sql = "select count(1) from consume_iss where cons_order = ? ";
//								pstmt=conn.prepareStatement(sql);
//								pstmt.setString(1,consOrdFrom);
//								rs = pstmt.executeQuery();
//								if(rs.next())
//								{
//									cnt = rs.getInt(1);
//								}
//								pstmt.close();
//								rs.close();
//								pstmt = null;
//								rs = null;
//								if(cnt > 0)
//								{
//									System.out.println("order already generated");
//									errCode = "";
//									errString = getErrorString("",errCode,userId);
//									break;
//								}
//								
//								
//								
//						}
						
				
						
					 } 
					
					
						break;
						default:
						
				} //end switch
			
			} //end try
			catch(Exception e)
			{
				System.out.println("Exception ::"+e);
				e.printStackTrace();
				errString=e.getMessage();
	            throw new ITMException(e);
			}
			finally
			{
				try
				{
					if(conn!=null)
					{
						if(rs != null )
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null )pstmt.close();
						pstmt =null;
						conn.close();
					}
					conn = null;
				}
				catch(Exception d)
				{
				  d.printStackTrace();
				}
			}
			
			System.out.println("ErrString ::[ "+errString+" ]");
			return errString;
		
		
		
		}

	 
	 public String itemChanged(String xmlString,String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
		{
			System.out.println("------------------ itemChanged called------------------");
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			String valueXmlString = "";
			try
			{   
				dom = parseString(xmlString);
				dom1 = parseString(xmlString1);
				dom2 = parseString(xmlString2);
				valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
			}
			catch(Exception e)
			{
				System.out.println("Exception : [budgetgroup ][itemChanged(String,String)] :==>\n"+e.getMessage());
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			}
			return valueXmlString;
		}

	 
	 public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
		{
		    Connection conn = null;
			ConnDriver connDriver = new ConnDriver();
			PreparedStatement pStmt = null;
			ResultSet rs = null;
			String sql = null;
			int currentFormNo = 0;
			StringBuffer valueXmlString = new StringBuffer();
			String columnValue = null;

			NodeList parentNodeList = null;
			Node parentNode = null; 
			Node childNode = null;
			NodeList childNodeList = null;
			String childNodeName = null;
			int childNodeListLength = 0;
			int ctr = 0;
			
			String  loginSite = null; 
			
			try
			{
				//Changes and Commented By Poonam on 08-06-2016 :START
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				//Changes and Commented By Poonam on 08-06-2016 :END

				connDriver = null;
				if(objContext != null && objContext.trim().length()>0)
				{
					currentFormNo = Integer.parseInt(objContext);
				}
				
				loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				
				System.out.println("[EpaymentICEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
				valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
				valueXmlString.append(editFlag).append("</editFlag></header>");
				
				System.out.println("Current Form No ["+currentFormNo+"]");							
				switch (currentFormNo)
				{
					case 1:
						valueXmlString.append("<Detail1>");	
						//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
						parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						ctr = 0;
						childNodeListLength = childNodeList.getLength();
						do
						{
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							if(childNodeName.equals(currentColumn))
							{
								if (childNode.getFirstChild() != null)
								{
									columnValue=childNode.getFirstChild().getNodeValue().trim();
								}
							}
							ctr++;
						}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
						System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
						
						if (currentColumn.trim().equals( "itm_default" ))
						{
							
							valueXmlString.append("<site_code>").append("<![CDATA[" + ( loginSite != null ? loginSite.trim() : ""  )+ "]]>").append("</site_code>");
							
						}
						
						
						if ( currentColumn.trim().equals( "cons_order__fr" ) )
						{
							java.sql.Timestamp orderDate = null;
							
							if( columnValue != null )
							{
								sql = "select ORDER_DATE from consume_ord where CONS_ORDER = '" + columnValue + "'";

								pStmt = conn.prepareStatement( sql );
								rs = pStmt.executeQuery();
								if( rs.next() )
								{
									orderDate = rs.getTimestamp( "ORDER_DATE" );
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
								
							
							}
							valueXmlString.append("<order_date__fr>").append("<![CDATA[" + ( orderDate != null ? genericUtility.getValidDateString(orderDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) : ""  )+ "]]>").append("</order_date__fr>");
							
						
						}	
						if ( currentColumn.trim().equals( "cons_order__to" ) )
						{
							java.sql.Timestamp orderDate = null;
							if( columnValue != null )
							{
								sql = "select ORDER_DATE from consume_ord where CONS_ORDER = '" + columnValue + "'";

								pStmt = conn.prepareStatement( sql );
								rs = pStmt.executeQuery();
								if( rs.next() )
								{
									orderDate = rs.getTimestamp( "ORDER_DATE" );
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null;
							}
							valueXmlString.append("<order_date__to>").append("<![CDATA[" + ( orderDate != null ? genericUtility.getValidDateString(orderDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) : ""  )+ "]]>").append("</order_date__to>");
						}	
						valueXmlString.append("</Detail1>");
						valueXmlString.append("</Root>");	
						break;
					///////////////
										
									
					////////////////
				}//END OF TRY
			}
			catch(Exception e)
			{
				System.out.println("Exception ::"+ e.getMessage());
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			}
			finally
			{
				try
				{
					if ( rs != null )
					{
						rs.close();
						rs = null;
					}
					if ( pStmt != null )
					{
						pStmt.close();
						pStmt = null;					
					}
					if ( conn != null )
					{
						conn.close();
						conn = null;
					}
				}
				catch(Exception e)
				{
					System.out.println("Exception ::" + e);
					e.printStackTrace();
				}			
			}
			return valueXmlString.toString();
		
	}
}
