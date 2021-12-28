


package ibase.webitm.ejb.dis;


import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless;
@Stateless

public class DiscListAplIC extends ValidatorEJB implements DiscListAplICLocal,DiscListAplICRemote{
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
			System.out.println("Exception : [DiscListAplIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String lineNo = "";
		String ResId="";
		String childNodeName = null;
		String profileId="";
		String sql="";

		String errString = "";
		String errCode = "";
		String userId = "";
		String errorType = "";

		int ct=0;
		int count = 0;
		int ctr=0,seriesCount1=0;
		int currentFormNo = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList parentNodeList123 = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		System.out.println("editFlag=["+editFlag+"]");
		String discList="",seqNo="";
		double discRate1=0.0,discRateChkNeg=0.0;
		int ctr1 = 0;
		try
		{
			/*conn = connDriver.getConnectDB("DriverITM");
			 * 
			 * 
*/			
			conn = getConnection();
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
					int seriesCount=0;
					discList = genericUtility.getColumnValue("disc_list", dom);
					String discListType = genericUtility.getColumnValue("disc_list_type", dom);
					seqNo = genericUtility.getColumnValue("seq_no", dom);
					
					
					
					
					if(editFlag.equals("A"))
			    	{
						String Sql = "SELECT COUNT(1) AS COUNT FROM disc_list_appl WHERE disc_list=? and disc_list_type =? ";
					    pstmt = conn.prepareStatement(Sql);
					    pstmt.setString(1,discList);
					    pstmt.setString(2,discListType);
				
					      
				         
						// pstmt.setString(7,userId);
					    rs = pstmt.executeQuery();
						System.out.println("Sql" + Sql);
						if (rs.next()) {
							seriesCount1 = rs.getInt("COUNT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Duplicate count@@"+seriesCount1);
						
					if (seriesCount1 > 0) {
						errString = getErrorString(" ", "VMDUPREC", userId);				
						break;
					}
			    	}
					
				/*	if (seriesCount1 > 0) {
						errString = getErrorString(" ", "VMDUPREC", userId);				
						break;
					}
				*/
					
					
					 if(childNodeName.equalsIgnoreCase("disc_list"))
					{
							
						 discList = checkNull(genericUtility.getColumnValue("disc_list", dom));
						System.out.println("discList="+discList);
						if(discList == null || discList.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMDISLCB",userId,"",conn);
							break ;
						}
						else
						{
							sql = " select count ( *) from disc_list where disc_list = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, discList);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ct = rs.getInt(1);									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ct == 0)
							{
								errString = itmDBAccessEJB.getErrorString("","VMDISCLNV ",userId,"",conn);
								break ;
							}
						}
						
					}else if(childNodeName.equalsIgnoreCase("seq_no"))
						{
							 seqNo = checkNull(genericUtility.getColumnValue("seq_no", dom));
							System.out.println("seqNo="+seqNo);
							if(seqNo == null || seqNo.trim().length() == 0)
							{
								errString = itmDBAccessEJB.getErrorString("","VMDISSNB",userId,"",conn);
								break ;
							}
							
							
						}
					 
					 //seq_no
					 
				}
				break;
			case 2 :
				int seriesCount = 0,editFlagcnt=0;
				String from_date="";
				String to_date="",discRate="",discType="";
				Timestamp fromDate=null;
				Timestamp toDate=null;
				System.out.println( "Detail 2 Validation called " );
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				System.out.println("parentNode@@@"+parentNode);
				childNodeList = parentNode.getChildNodes();
				System.out.println("childNodeList@@@"+childNodeList);

				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength@@"+childNodeListLength);
				NodeList detail2List = dom2.getElementsByTagName("Detail2");
				int noOfParentD = detail2List.getLength();
				
				if(noOfParentD>1){
					System.out.println("condition");
					errString = itmDBAccessEJB.getErrorString("","VMRECNALW",userId,"",conn);
					
                    break ;
					
				}
				for( ctr = 0; ctr < childNodeListLength;ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					discList = genericUtility.getColumnValue("disc_list", dom);
					String discListType = genericUtility.getColumnValue("disc_list_type", dom);
					discRate= genericUtility.getColumnValue("disc_rate", dom);
					discType = genericUtility.getColumnValue("disc_type", dom);
					System.out.println("discRate@@"+discRate);
					System.out.println("Edit flag"+editFlag);
					from_date=checkNull(genericUtility.getColumnValue("from_date", dom));
				      to_date=checkNull(genericUtility.getColumnValue("to_date", dom));
				    				  	
					if(from_date !=null && from_date.trim().length()>0 )
					{
						fromDate = Timestamp.valueOf(genericUtility.getValidDateString(from_date, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					if(to_date !=null && to_date.trim().length()>0 )
					{
						toDate = Timestamp.valueOf(genericUtility.getValidDateString(to_date, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					System.out.println("fromdate@@@@"+fromDate);
					System.out.println("todate@@@@"+toDate);
					
					if(editFlag.equals("A"))
			    	{
						 String Sql = "SELECT COUNT(1) AS COUNT FROM disc_list_rate WHERE disc_list=? AND disc_list_type=? ";//and chg_user=? ";
						    pstmt = conn.prepareStatement(Sql);
						    pstmt.setString(1,discList);
						    pstmt.setString(2,discListType);
					
						   /* pstmt.setTimestamp(3,fromDate);	         
					         pstmt.setTimestamp(4,toDate);
					         pstmt.setString(5,discRate);
							 pstmt.setString(6,discType);*/
							// pstmt.setString(7,userId);
						    rs = pstmt.executeQuery();
							System.out.println("Sql" + Sql);
							if (rs.next()) {
								seriesCount = rs.getInt("COUNT");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Duplicate count@@"+seriesCount);
							System.out.println("editFlag@@"+editFlag);
					    if (seriesCount > 0) {
						errString = getErrorString(" ", "VMDUPREC", userId);				
						break;
					}
			    	}
					 if(childNodeName.equalsIgnoreCase("disc_list"))
					{
						 String discList1="";
						 discList1 = genericUtility.getColumnValue("disc_list", dom);
						if(discList1 == null || discList1.trim().length() == 0)
						{
							System.out.println("discList id is" + discList1);
							errString = itmDBAccessEJB.getErrorString("","VMNUPROID ",userId,"",conn);
							break ;
						}
					/*	else
						{
							sql = " select count ( *) from disc_list where disc_list = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, discList1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ct = rs.getInt(1);									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ct == 0)
							{
								errString = itmDBAccessEJB.getErrorString("disc_list","VTINVPROID ",userId,"",conn);
								break ;
							}
						}*/
						
						
					}else if (childNodeName.equalsIgnoreCase("from_date"))
					{
						System.out.println("INSIDE FROM DATE ");
						
						from_date = checkNull(this.genericUtility.getColumnValue("from_date", dom));
						System.out.println("from_date@@@"+from_date);

							if(from_date == null || from_date.trim().length() == 0  )
							{
								errString = itmDBAccessEJB.getErrorString("from_date","VMFRMDTNB",userId,"",conn);
								break ;
							}
							  /*else if(from_date !=null && from_date.trim().length() > 0)
		                        {
		                            System.out.println("eff_from"+from_date);
		                            System.out.println("valid_upto"+to_date);
		                       
		                            if(from_date !=null && from_date.trim().length()>0 )
		        					{
		        						fromDate = Timestamp.valueOf(genericUtility.getValidDateString(from_date, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
		        					}
		        					if(to_date !=null && to_date.trim().length()>0 )
		        					{
		        						toDate = Timestamp.valueOf(genericUtility.getValidDateString(to_date, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
		        					}System.out.println("fromDate@@@"+fromDate);
		                            System.out.println("toDate@@@"+toDate);
		                            if(fromDate != null && toDate.before(fromDate))
		                            {
		                            	System.out.println("In COndition@from Date");
		                                errString = itmDBAccessEJB.getErrorString("from_date","VMFRMDGT",userId,"",conn);
		                                break ;
		                            }
		                        }*/
							
					}else if(childNodeName.equalsIgnoreCase("to_date"))
					{
							
						System.out.println("validation VALID_UPTO  executed");
					from_date = checkNull(this.genericUtility.getColumnValue("from_date", dom));
					to_date = checkNull(this.genericUtility.getColumnValue("to_date", dom));
						
						
                     System.out.println(" from_date " +from_date);
                     System.out.println(" TO_DATE " +to_date);
                    /* if( (from_date == null || from_date.trim().length() == 0)&&(to_date == null || to_date.trim().length() == 0))
                     {
                    	 errString = itmDBAccessEJB.getErrorString("","VMFRMDTNB",userId,"",conn);
							break ;
                    	 
                     }
                     */
                     if(to_date == null || to_date.trim().length() == 0)
                     {
                         errString = itmDBAccessEJB.getErrorString("to_date","VMTODTNB",userId,"",conn);
                         break ;
                     }
                    
                     
                     else if(to_date !=null && to_date.trim().length() > 0)
                     {
                         
                         if(from_date !=null && from_date.trim().length()>0 )
     					{
     						fromDate = Timestamp.valueOf(genericUtility.getValidDateString(from_date, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
     					}
                         
                       
                         
     					if(to_date !=null && to_date.trim().length()>0 )
     					{
     						toDate = Timestamp.valueOf(genericUtility.getValidDateString(to_date, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
     					} System.out.println("fromDate@@@"+fromDate);
                         System.out.println("toDate@@@"+toDate);
                         if(toDate != null && toDate.before(fromDate))
                         {
                         	System.out.println("In COndition");
                             errString = itmDBAccessEJB.getErrorString("to_date","VMTODTLT",userId,"",conn);
                             break ;
                         }
                         }
                     
						}				
					else if(childNodeName.equalsIgnoreCase("disc_rate"))
						{
						    String discRate2="";
							discRate2= genericUtility.getColumnValue("disc_rate", dom);
							System.out.println("discRate@@"+discRate1);
							discRateChkNeg= discRate2 == null ? 0 : Double.parseDouble(discRate);
							System.out.println("discRateChkNeg["+discRateChkNeg+"]");
							 if(discRateChkNeg<=0)
							{
								 System.out.println("Rate is@"+discRateChkNeg);
								errString = itmDBAccessEJB.getErrorString("disc_rate","VMNEGRT",userId,"",conn);
                                break ;
							}
							
						}else if(childNodeName.equalsIgnoreCase("disc_type"))
						{
							String discType1="";
							discType1 = genericUtility.getColumnValue("disc_type", dom);
							System.out.println("discType["+discType1+"]");
						/*	discRate= genericUtility.getColumnValue("disc_rate", dom);
							System.out.println("discRate@@"+discRate);
							*/
							if((discType.equals("P")||discType.equalsIgnoreCase("P"))&& (discRateChkNeg>100))
							{
								System.out.println("If Dist Type is ["+discType+"]");
								errString = itmDBAccessEJB.getErrorString("disc_type","VMINVDRT",userId,"",conn);
                                break ;
								
							}
							
						}

				}
				
				break;
				
			}
			
		}
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
					if(rs != null )rs.close();
					rs = null;
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
		System.out.println("ErrString ::"+errString);


		return errString;
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	 {
		Document dom1 = null;
		Document dom = null;
		Document dom2 = null;
		String valueXmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try 
		{
			if (xmlString != null && xmlString.trim().length() != 0)
			{
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0)
			{
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) 
			{
				dom2 = genericUtility.parseString(xmlString2);
			}
			  valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch (Exception e) 
		{
			System.out.println("Exception :disc_listIC:itemChanged(String,String,String,String,String,String):" + e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("returning from disc_listitemChanged");
		return (valueXmlString);
	 }

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		StringBuffer valueXmlString = new StringBuffer();
		String errString="";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null,pstmt=null;
		ResultSet rs = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String sql = "";
		int currentFormNo = 0;
		int ctr=0;
		String childNodeName = null;
		String columnValue = null;
	/*	String  profileId = "",profileName="";
		String profileIdItmDe="" , profileIdItmDedesc="";;
		String profileIdDetail="",profileIdDetaildesc="";
		String resId="",resoucrDesc="";
		String itmEditprofile="",itmEditResid="",itmEditline="",itmDesc="";
		*/
		String descList="",listDescr="",discDesc="";
	//	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		try 
		{
			String userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("currentFormNo@@"+currentFormNo);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			
			switch (currentFormNo) 
			{
			case 1:
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
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
							System.out.println("columnValue@@"+columnValue);
							
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));

				 if (currentColumn.trim().equalsIgnoreCase("disc_list")) 
				 {
					 descList = genericUtility.getColumnValue("disc_list", dom);
					 System.out.println("descList@@@"+descList);
					 descList = descList == null ? "" : descList.trim();
						sql = "select descr from disc_list where disc_list =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, descList);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							listDescr = rs.getString(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						listDescr = listDescr == null ? "" : listDescr.trim();
						valueXmlString.append("<disc_list>").append(descList).append("</disc_list>");
						valueXmlString.append("<descr>").append(listDescr).append("</descr>");
				 }
				 valueXmlString.append("</Detail1>");					
					break;
			   case 2:
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
						if(childNodeName.equals(currentColumn.trim()))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue();
								System.out.println("columnValue@@"+columnValue);
							}
						}
						ctr++;
					}
				    while(ctr < childNodeListLength && !childNodeName.equals(currentColumn));
					if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
						descList = genericUtility.getColumnValue("disc_list",dom);
						System.out.println("@@@@@3 descList [" + descList + "]");
						//profileIdItmDe = profileIdItmDe == null ? "" : profileIdItmDe.trim();
						valueXmlString.append("<disc_list>").append("<![CDATA[" + descList + "]]>").append("</disc_list>");
						//System.out.println("profileIdItmDe["+profileIdItmDe+"]");
							sql = "select descr from disc_list where disc_list =?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, descList);
							rs = pStmt.executeQuery();
							if (rs.next())
							{
							            discDesc = rs.getString(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							discDesc = discDesc == null ? "" : discDesc.trim();
							
							System.out.println("disc_listDESCR["+discDesc+"]");
							valueXmlString.append("<disc_list>").append("<![CDATA[" + descList + "]]>").append("</disc_list>");
							valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							
							
							
							
							/*if (!editFlag.equalsIgnoreCase("E")) 
							{
								System.out.println("Edit Falg is "+ editFlag);
								valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
								
								
							}*/
							/*if(childNodeListLength==1)
							{
								
							}*/
							

					}
					else if (currentColumn.trim().equalsIgnoreCase("disc_list"))
					{
						descList = genericUtility.getColumnValue("disc_list",dom);
						sql = "select descr from disc_list where disc_list =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, descList);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
						            discDesc = rs.getString(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						discDesc = discDesc == null ? "" : discDesc.trim();
						
						System.out.println("disc_listDESCR["+discDesc+"]");
						valueXmlString.append("<disc_list>").append("<![CDATA[" + descList + "]]>").append("</disc_list>");
						valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
						
						
						/* descList=genericUtility.getColumnValue("disc_list", dom1);
						 itmEditResid=genericUtility.getColumnValue("res_id", dom);
						 itmEditline=genericUtility.getColumnValue("line_no", dom);
						 descList = descList == null ? "" : descList.trim();
						 System.out.println("descList["+descList+"]");
							
						 sql = "select descr from disc_list where disc_list=? ";
						 pStmt = conn.prepareStatement(sql);
						 pStmt.setString(1, descList);
						 rs = pStmt.executeQuery();
						 if (rs.next())
						   {
							  discDesc = rs.getString(1);

							}
						 rs.close();
						 rs = null;
						 pStmt.close();
						 pStmt = null;
						 System.out.println("itm_defaultedit@@"+discDesc);
						 discDesc = discDesc == null ? "" : discDesc.trim();
						System.out.println("discDesc["+discDesc+"]");
						valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
						if (!editFlag.equalsIgnoreCase("E")) 
						{
							System.out.println("IN IF EditFlag["+editFlag+"]");
							//valueXmlString.append("<profile_id protect = \"0\" >").append("<![CDATA[" + itmEditprofile + "]]>").append("</profile_id>");
							valueXmlString.append("<disc_list>").append("<![CDATA[" + descList + "]]>").append("</disc_list>");
							valueXmlString.append("<descr protect = \"0\" >").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							//valueXmlString.append("<descr>").append("<![CDATA[" + discDesc + "]]>").append("</descr>");
							
						}*/
					}

					/*else if(currentColumn.trim().equalsIgnoreCase("res_id"))
					{}*/
					valueXmlString.append("</Detail2>");
					break;  
			}
			valueXmlString.append("</Root>\r\n");
		}   
		catch (Exception e)
		{
			System.out.println("Exception :DiscListRateICitemChange•••••••(Document,String):" + e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());
			
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			/*errString=e.getMessage();
			throw new ITMException(e);*/
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
		System.out.println("\n***** ValueXmlString :" + valueXmlString + ":*******");
		return valueXmlString.toString();
	}
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input.trim();
	}


}
