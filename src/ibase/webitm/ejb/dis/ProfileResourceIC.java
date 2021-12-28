/********************************************************
	Title ResourceIC[D16ASUN003]
	Date  : 12/04/16
	Developer: Abhijit Gaikwad

 ********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
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

public class ProfileResourceIC extends ValidatorEJB implements TaxChapterICLocal, TaxChapterICRemote
{
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
			System.out.println("Exception : [ProfileResourceIC][wfValData( String, String )] :==>\n" + e.getMessage());
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
		int ctr=0;
		int currentFormNo = 0;
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
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
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
					int seriesCount=0;
					 if(childNodeName.equalsIgnoreCase("profile_id"))
					{
						 //Added by Santosh on 07-11-2017 to validate in add mode only
						 if("A".equalsIgnoreCase(editFlag))
						 {
							 String id = genericUtility.getColumnValue("profile_id", dom);
							 String Sql = "SELECT COUNT(1) AS COUNT FROM profile WHERE profile_id=?";
							 pstmt = conn.prepareStatement(Sql);
							 pstmt.setString(1,id);
							 rs = pstmt.executeQuery();
							 System.out.println("Sql" + Sql);
							 if (rs.next()) {
								 seriesCount = rs.getInt("COUNT");
							 }
							 rs.close();
							 rs = null;
							 pstmt.close();
							 pstmt = null;
							 if (seriesCount > 0)
							 {
								 errString = getErrorString(" ", "VMPRFLIDEX", userId);				
								 break;
							 }
						 }
						 profileId = checkNull(genericUtility.getColumnValue("profile_id", dom));
						System.out.println("profileId="+profileId);
						if(profileId == null || profileId.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMNUPROID ",userId,"",conn);
							break ;
						}
						//Commented by Santosh on 07-11-2017 to remove invalid validation
						/*else
						{
							sql = " select count ( *) from users where profile_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, profileId);
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
								errString = itmDBAccessEJB.getErrorString("","VTINVPROID ",userId,"",conn);
								break ;
							}
						}*/
						
					}
				}
				break;
			case 2 :
				System.out.println( "Detail 2 Validation called " );
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				String profileId1="";
				int seriesCount = 0;
				for( ctr = 0; ctr < childNodeListLength;ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					String id = genericUtility.getColumnValue("profile_id", dom);
					String id1 = genericUtility.getColumnValue("res_id", dom);
					String id2 = genericUtility.getColumnValue("line_no",dom);
					String Sql = "SELECT COUNT(1) AS COUNT FROM profile_resource WHERE profile_id=? AND res_id=?AND line_no=?";
				    pstmt = conn.prepareStatement(Sql);
				    pstmt.setString(1,id);
				    pstmt.setString(2,id1);
				    pstmt.setString(3,id2);
				    rs = pstmt.executeQuery();
					System.out.println("Sql" + Sql);
					if (rs.next()) {
						seriesCount = rs.getInt("COUNT");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (seriesCount > 0) {
						errString = getErrorString(" ", "VMTRANIDEX", userId);				
						break;
					}
				
					 if(childNodeName.equalsIgnoreCase("profile_id"))
					{
						 profileId1 = checkNull(genericUtility.getColumnValue("profile_id", dom));
						System.out.println("profileId="+profileId1);
						if(profileId1 == null || profileId1.trim().length() == 0)
						{
							System.out.println("Profile id is" + profileId1);
							errString = itmDBAccessEJB.getErrorString("","VMNUPROID ",userId,"",conn);
							break ;
						}
						else
						{
							sql = " select count ( *) from users where profile_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, profileId1);
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
								errString = itmDBAccessEJB.getErrorString("","VTINVPROID ",userId,"",conn);
								break ;
							}
						}
						
					}

					 else if (childNodeName.equalsIgnoreCase("res_id"))
						{
					
							ResId = checkNull(this.genericUtility.getColumnValue("res_id", dom));
								if(ResId == null || ResId.trim().length() == 0  )
								{
									errString = itmDBAccessEJB.getErrorString("","VMNURESID ",userId,"",conn);
									break ;
								}
								else
								{
									sql = " select count ( *) from resources where res_id = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ResId);
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
										errCode = "";
										errString = itmDBAccessEJB.getErrorString("","VMINVRESID ",userId,"",conn);
										break ;
									}
								}
							}
						else if(childNodeName.equalsIgnoreCase("line_no"))
						{
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
							System.out.println("LineNo="+lineNo);
							if(lineNo == null || lineNo.trim().length() == 0)
							{
								errCode = "VMLINENO";
								errString = itmDBAccessEJB.getErrorString("","VMLINENO ",userId,"",conn);
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
			System.out.println("Exception :ProfileResourceIC:itemChanged(String,String,String,String,String,String):" + e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("returning from ProfileResourceIC itemChanged");
		return (valueXmlString);
	 }

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		StringBuffer valueXmlString = new StringBuffer();

		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
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
		String  profileId = "",profileName="";
		String profileIdItmDe="" , profileIdItmDedesc="";;
		String profileIdDetail="",profileIdDetaildesc="";
		String resId="",resoucrDesc="";
		String itmEditprofile="",itmEditResid="",itmEditline="",itmDesc="";
		
		try 
		{
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
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
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));

				 if (currentColumn.trim().equalsIgnoreCase("profile_id")) 
				 {
					 profileId = genericUtility.getColumnValue("profile_id", dom);
					 profileId = profileId == null ? "" : profileId.trim();
						sql = "select name from users where profile_id =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, profileId);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							profileName = rs.getString(1);

						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						profileName = profileName == null ? "" : profileName.trim();

						valueXmlString.append("<descr>").append(profileName).append("</descr>\r\n");
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
							}
						}
						ctr++;
					}
				    while(ctr < childNodeListLength && !childNodeName.equals(currentColumn));
					if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
						profileIdItmDe = genericUtility.getColumnValue("profile_id",dom1);
						System.out.println("@@@@@3 profileId [" + profileIdItmDe + "]");
						profileIdItmDe = profileIdItmDe == null ? "" : profileIdItmDe.trim();
						valueXmlString.append("<profile_id>").append("<![CDATA[" + profileIdItmDe + "]]>").append("</profile_id>");
						System.out.println("profileIdItmDe["+profileIdItmDe+"]");
							sql = "select name from users where profile_id =?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, profileIdItmDe);
							rs = pStmt.executeQuery();
							if (rs.next())
							{
								profileIdItmDedesc = rs.getString(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							profileIdItmDedesc = profileIdItmDedesc == null ? "" : profileIdItmDedesc.trim();
							System.out.println("profileIdItmDedesc["+profileIdItmDedesc+"]");
							valueXmlString.append("<profile_descr>").append(profileIdItmDedesc).append("</profile_descr>\r\n");
							if (!editFlag.equalsIgnoreCase("E")) 
							{
								System.out.println("Edit Falg is "+ editFlag);
								//String profileIdItmDeFlagA="";
								String resIdFalga="";
								//String lineFlagA="";
								System.out.println("IN IF EditFlag["+editFlag+"]");
							//	profileIdItmDeFlagA = genericUtility.getColumnValue("profile_id",dom1);
								resIdFalga = genericUtility.getColumnValue("res_id",dom);
							//	lineFlagA = genericUtility.getColumnValue("line_no",dom);
								//profileIdItmDeFlagA = profileIdItmDeFlagA == null ? "" : profileIdItmDeFlagA.trim();
								resIdFalga = resIdFalga == null ? "" : resIdFalga.trim();
							//	System.out.println("##### profileIdItmDeFlagA [" + profileIdItmDeFlagA + "]");
								System.out.println("##### resIdFalga [" + resIdFalga + "]");
							//	System.out.println("##### lineFlagA [" + lineFlagA + "]");
							//	valueXmlString.append("<profile_id>").append("<![CDATA[" + profileIdItmDeFlagA + "]]>").append("</profile_id>");
								//valueXmlString.append("<profile_id protect = \"1\" >").append("<![CDATA[" + profileIdItmDeFlagA + "]]>").append("</profile_id>");
								valueXmlString.append("<res_id protect = \"0\" >").append("<![CDATA[" + resIdFalga + "]]>").append("</res_id>");
								//valueXmlString.append("<line_no protect = \"1\" >").append("<![CDATA[" + lineFlagA + "]]>").append("</line_no>");
							}
							

					}
					else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
					{
						 itmEditprofile=genericUtility.getColumnValue("profile_id", dom);
						 itmEditResid=genericUtility.getColumnValue("res_id", dom);
						 itmEditline=genericUtility.getColumnValue("line_no", dom);
						 itmEditResid = itmEditResid == null ? "" : itmEditResid.trim();
						 System.out.println("itmEditResid["+itmEditResid+"]");
							
						 sql = "select descr from resources where res_id=? ";
						 pStmt = conn.prepareStatement(sql);
						 pStmt.setString(1, itmEditResid);
						 rs = pStmt.executeQuery();
						 if (rs.next())
						   {
							  itmDesc = rs.getString(1);

							}
						 rs.close();
						 rs = null;
						 pStmt.close();
						 pStmt = null;

						itmDesc = itmDesc == null ? "" : itmDesc.trim();
						System.out.println("resoucrDesc["+itmDesc+"]");
						valueXmlString.append("<profile_id>").append("<![CDATA[" + itmEditprofile + "]]>").append("</profile_id>");
						valueXmlString.append("<res_id protect = \"1\" >").append("<![CDATA[" + itmEditResid + "]]>").append("</res_id>");
						valueXmlString.append("<line_no>").append("<![CDATA[" + itmEditline + "]]>").append("</line_no>");
						valueXmlString.append("<descr protect= \"1\" >").append("<![CDATA[" + itmDesc + "]]>").append("</descr>");
						System.out.println("EditFlag["+editFlag+"]");
						if (!editFlag.equalsIgnoreCase("E")) 
						{
							System.out.println("IN IF EditFlag["+editFlag+"]");
							//valueXmlString.append("<profile_id protect = \"0\" >").append("<![CDATA[" + itmEditprofile + "]]>").append("</profile_id>");
							valueXmlString.append("<res_id protect = \"0\" >").append("<![CDATA[" + itmEditResid + "]]>").append("</res_id>");
						}
					}
					else if(currentColumn.trim().equalsIgnoreCase("profile_id"))
					{
						profileIdDetail = genericUtility.getColumnValue("profile_id", dom);
						System.out.println("profileIdDetail["+profileIdDetail+"]");
						profileIdDetail = profileIdDetail == null ? "" : profileIdDetail.trim();
							sql = "select name from users where profile_id =?";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, profileIdDetail);
							rs = pStmt.executeQuery();
							if (rs.next())
							{
								profileIdDetaildesc = rs.getString(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							profileIdDetaildesc = profileIdDetaildesc == null ? "" : profileIdDetaildesc.trim();
							System.out.println("profileIdDetaildesc["+profileIdDetaildesc+"]");
							valueXmlString.append("<profile_id protect = \"1\" >").append("<![CDATA[" + profileIdDetail + "]]>").append("</profile_id>");
							valueXmlString.append("<profile_descr>").append(profileIdDetaildesc).append("</profile_descr>\r\n");
					}
					else if(currentColumn.trim().equalsIgnoreCase("res_id"))
					{
						resId = genericUtility.getColumnValue("res_id", dom);
						resId = resId == null ? "" : resId.trim();
						System.out.println("resId["+resId+"]");
							sql = "select descr from resources where res_id=? ";
							pStmt = conn.prepareStatement(sql);
							pStmt.setString(1, resId);
							rs = pStmt.executeQuery();
							if (rs.next())
							{
								resoucrDesc = rs.getString(1);

							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;

							resoucrDesc = resoucrDesc == null ? "" : resoucrDesc.trim();
							System.out.println("resoucrDesc["+resoucrDesc+"]");
							valueXmlString.append("<descr>").append(resoucrDesc).append("</descr>\r\n");
					}
					valueXmlString.append("</Detail2>");
					break;  
			}
			valueXmlString.append("</Root>\r\n");
		}   
		catch (Exception e)
		{
			System.out.println("Exception :ProfileResourceICitemChange•••••••(Document,String):" + e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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


