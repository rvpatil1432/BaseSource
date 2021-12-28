/********************************************************
Title : NsmEmailIC[F16ASUN011]
Date  : 26/04/16
Developer: Sachin Satre

********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.utility.E12GenericUtility;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Session Bean implementation class NsmEmailIC
 */
@Stateless
public class NsmEmailIC extends ValidatorEJB implements NsmEmailICRemote, NsmEmailICLocal 
{

    /**
     * Default constructor. 
     */
    public NsmEmailIC()
    {
        // TODO Auto-generated constructor stub
    }
    
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
    
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		int childNodeListLength;
		int currentFormNo = 0;
	
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String sql = "";
		String userId = "";
		String errFldName="";
		String columnValue="";
		String	division="";
		String fromDate="";
		String toDate="";
		String currAppdate ="";
		String sqlDiv="";
		long cnt = 0;
		int cntv = 0;
		int cntItmSer=0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat simpleDateFormat1 = null;
		java.sql.Timestamp currDate = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
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
			}
			switch (currentFormNo)
			{
			 case 1:
				 	parentNodeList = dom.getElementsByTagName("Detail1");
				 	parentNode = parentNodeList.item(0);
				 	childNodeList = parentNode.getChildNodes();
				 	childNodeListLength = childNodeList.getLength();
				 	for(ctr = 0; ctr < childNodeListLength; ctr ++)
				 	{
				 		childNode = childNodeList.item(ctr);
				 		childNodeName = childNode.getNodeName();
				 		
				 		if(childNodeName.equalsIgnoreCase("division"))
				 		{
					
				 			division= checkNull(genericUtility.getColumnValue("division",dom));
				 			System.out.println("Division  fire== "+division );
								
								if(division == null || division.length() ==0)				
								{
									//errString =  getErrorString("tran_cat","VMTRANCAT",userId);
									errCode = "VMDIVBLK";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									
								}
								else 
								{
									if(editFlag.equals("A"))
				 					{
										sqlDiv="select count(*) from itemSer where item_ser= ? ";
										pstmt=conn.prepareStatement(sqlDiv);
										pstmt.setString(1,division);
										rs = pstmt.executeQuery();
										while(rs.next())
										{	
											cntItmSer = rs.getInt(1);
											System.out.println("Division inside while  count fire== "+cntItmSer );
										}
										rs.close();
				 						rs = null;
				 						pstmt.close();
				 						pstmt = null;
				 						System.out.println("Division aftr while  count fire== "+cntItmSer );
				 						
				 						if(cntItmSer==0)
				 						{
				 							System.out.println("division is not present in itemseer validatioon fire");
				 							//errString =  getErrorString("tran_cat","VMDUPTRCAT",userId);
				 							errCode = "VMDIVNOTEX";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											
				 						 }
				 						
				 						
				 						sql = "select count(*) from NSM_EMAIL where DIVISION = ?";
				 						pstmt = conn.prepareStatement(sql);
				 						pstmt.setString(1, division);
				 						rs = pstmt.executeQuery();
				 						if(rs.next())
				 						{
				 							cntv = rs.getInt(1);
				 							System.out.println("Division inside while of duplicate count fire== "+cntv );
				 						}
				 						rs.close();
				 						rs = null;
				 						pstmt.close();
				 						pstmt = null;
				 						System.out.println("Division aftr while of duplicate count fire== "+cntv );
				 						if(cntv > 0)
				 						{
				 							System.out.println("Division  already exists validatioon fire");
				 							//errString =  getErrorString("tran_cat","VMDUPTRCAT",userId);
				 							errCode = "VMDUPDIV";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											
				 						 }
							        }				
								 }
				 		     }
				 		
				 		else if(childNodeName.equalsIgnoreCase("travel_date__from")) 
						{
				 			fromDate= checkNull(genericUtility.getColumnValue("travel_date__from",dom));
							
						}
						else if(childNodeName.equalsIgnoreCase("travel_date__to"))
						{
							toDate= checkNull(genericUtility.getColumnValue("travel_date__to",dom));
							
						}
				 	 }
				 	
				 	if(fromDate.length()>0 && toDate.length()>0)
					{
						Date date1 = sdf.parse(fromDate);
						Date date2 = sdf.parse(toDate);
						
						if(date1.compareTo(date2)>0)
						{
							System.out.println("Date1 is after Date2");
							//errString = getErrorString("valid_upto","INDDTRNG",userId);
							errCode = "VTTRDTTOLS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				 	else if(fromDate.length()>0 && toDate.length()==0)
					{
				 		System.out.println("select travel to date validation fire");
						errCode = "VTTRDTTONL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				 	else if(fromDate.length()==0 && toDate.length()>0)
				 	{
				 		System.out.println("select travel from date validation fire");
						errCode = "VTTRDTFRNL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
				 	}
				 	
				 	
		   break;
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return valueXmlString;
	} 
	
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("intered in itemchnage method NSM Email..");
		
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		String logInEmpCode = "";
		String columnValue = "";
		int childNodeListLength = 0;
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs1 = null ;
		ResultSet rs2 = null ;
		String divisionDom ="";
		String division ="";
		String descr="";
		String sqlDiv="",nsmCode="",nsmName="",travelCode="",travelName="",ccTo="",ccToName="",optName="",optCode="",groupCode="";
		
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
	      
			columnValue = genericUtility.getColumnValue(currentColumn,dom);
			logInEmpCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			
			System.out.println("editFlag@@ : ["+editFlag+"]");
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			  case 1:
				System.out.println("NSM Email itemchanged case 1");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
							
				if (currentColumn.trim().equalsIgnoreCase("division")) 
				{
					System.out.println("intered in division mode hhho..");
					divisionDom = checkNull(genericUtility.getColumnValue("division",dom));
					
					sqlDiv="select item_ser,descr from itemSer where item_ser= ? ";
					pstmt1=conn.prepareStatement(sqlDiv);
					pstmt1.setString(1,divisionDom);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 System.out.println("intered in result set hhho..");
						 division = checkNull(rs1.getString("item_ser"));
						 descr= checkNull(rs1.getString("descr"));	
						 System.out.println("intered in rs division  hhho.."+division);
						 System.out.println("intered in rs descr descr hhho.."+descr);
						 
						 valueXmlString.append("<descr ><![CDATA[").append(descr).append("]]></descr>\r\n");
					}
					else
					{
						System.out.println("intered in rs else to check active hhho.."+descr);
						valueXmlString.append("<descr ><![CDATA[").append("").append("]]></descr>\r\n");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					
					
				}
				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{
					division = checkNull(genericUtility.getColumnValue("division", dom)).trim();
					nsmCode = checkNull(genericUtility.getColumnValue("nsm_code", dom)).trim();
					nsmName = checkNull(genericUtility.getColumnValue("nsm_name", dom)).trim();
					travelCode = checkNull(genericUtility.getColumnValue("travel_code", dom)).trim();
					travelName = checkNull(genericUtility.getColumnValue("travel_name", dom)).trim();
					ccTo = checkNull(genericUtility.getColumnValue("cc_to", dom)).trim();
					ccToName = checkNull(genericUtility.getColumnValue("cc_to_name", dom)).trim();
					optCode = checkNull(genericUtility.getColumnValue("opt1_code", dom)).trim();
					optName = checkNull(genericUtility.getColumnValue("opt1_name", dom)).trim();
					groupCode = checkNull(genericUtility.getColumnValue("group_code", dom)).trim();
					
					
					valueXmlString.append("<division ><![CDATA[").append(division).append("]]></division>\r\n");
					valueXmlString.append("<nsm_code ><![CDATA[").append(nsmCode).append("]]></nsm_code>\r\n");
					valueXmlString.append("<nsm_name ><![CDATA[").append(nsmName).append("]]></nsm_name>\r\n");
					valueXmlString.append("<travel_code ><![CDATA[").append(travelCode).append("]]></travel_code>\r\n");
					valueXmlString.append("<travel_name ><![CDATA[").append(travelName).append("]]></travel_name>\r\n");
					valueXmlString.append("<cc_to ><![CDATA[").append(ccTo).append("]]></cc_to>\r\n");
					valueXmlString.append("<cc_to_name ><![CDATA[").append(ccToName).append("]]></cc_to_name>\r\n");
					valueXmlString.append("<opt1_code ><![CDATA[").append(optCode).append("]]></opt1_code>\r\n");
					valueXmlString.append("<opt1_name ><![CDATA[").append(optName).append("]]></opt1_name>\r\n");
					valueXmlString.append("<group_code ><![CDATA[").append(groupCode).append("]]></group_code>\r\n");
					
					
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
    
    
    
    
	private String errorType(Connection conn , String errorCode) throws ITMException
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
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
