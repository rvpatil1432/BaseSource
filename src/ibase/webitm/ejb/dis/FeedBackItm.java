package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ITMDBAccessEJB;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.ejb.CreateException;
//import javax.ejb.SessionBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class FeedBackItm extends ValidatorEJB implements FeedBackItmLocal,FeedBackItmRemote //SessionBean
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		//return "";
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate()
	{
	}
	public void ejbPassivate()
	{
	}*/
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;		
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = " ",errCode = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String columnValue = null;
		String childNodeName = null;
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		ResultSet rs1 = null;
		String sql1 = null;
		String requestId = "",timeReq = "",compStatus="",confirmed="",timeSpend = "",actionCode="",userId="",loginSite="",statusCode="";				
	    ConnDriver connDriver = new ConnDriver();
		int count=0;
    	try
		{
			// GenericUtility genericUtility = GenericUtility.getInstance();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//genericUtility = GenericUtility.getInstance(); 
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
						if ( childNodeName.equalsIgnoreCase( "req_id" ) )
						{							
							requestId = genericUtility.getColumnValue( "req_id", dom);
							requestId = requestId == null ? "" : requestId.trim();
							if(requestId ==null || requestId.trim().length()==0 )
							{
								errCode = "VTREQNUL";
								errString = getErrorString( "req_id", errCode, userId );
								break ;
							}
							sql="select comp_stat, confirmed  from ser_request where req_id = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,requestId .trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
							  compStatus = rs.getString("comp_stat")==null ? "":rs.getString("comp_stat");		
							  confirmed = rs.getString("confirmed")==null ? "":rs.getString("confirmed");	
							}					
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;		
							if(	"C".equalsIgnoreCase(compStatus.substring(0,1)) )
							{
								errCode = "VTREQCL";
								errString = getErrorString( "req_id", errCode, userId );
							}
							/*else
							{
								if(!("Y".equalsIgnoreCase(compStatus.substring(0,1))))
								{
									errCode = "VTNOTCON";
									errString = getErrorString( "req_id", errCode, userId );
								}
							}*/							
						}
						if ( childNodeName.equalsIgnoreCase( "time_spend" ) )
						{
							timeSpend = genericUtility.getColumnValue( "time_spend", dom);
							actionCode = genericUtility.getColumnValue( "action_code", dom);
							actionCode = actionCode == null ?"": actionCode.trim();
							sql="select time_req from feedbk_status where status_code = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,actionCode .trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								timeReq = rs.getString("time_req")== null ?"" :rs.getString("time_req") ;								  
							}							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(( actionCode == null || actionCode.trim().length()==0))
							{
								errCode = "VTTIM2";
								errString = getErrorString( "time_spend", errCode, userId );

							}
							if("Y".equalsIgnoreCase(timeReq) && (timeSpend ==null || Float.parseFloat(timeSpend)==0.00 || Float.parseFloat(timeSpend)==.00 || Float.parseFloat(timeSpend) < 0 || Float.parseFloat(timeSpend) > 24 ))
							{
								errCode = "VTTIME";
								errString = getErrorString( "time_spend", errCode, userId );
								
							}
							if("N".equalsIgnoreCase(timeReq) && Float.parseFloat(timeSpend) >0)
							{
								errCode = "VTTIME1";
								errString = getErrorString( "time_spend", errCode, userId );
							}
						}// timspend 
						if ( childNodeName.equalsIgnoreCase( "action_code" ) )
						{
							statusCode = genericUtility.getColumnValue( "action_code", dom);
							statusCode = statusCode==null?"":statusCode.trim();
							sql="select count(*) from feedbk_status where active = 'Y' and "
								+"	status_code = ?  order by serial_no " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,statusCode.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1) ;								  
							}							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(count == 0)
							{
								errCode = "VTSTCOD";
								errString = getErrorString( "status_code", errCode, userId );
							}
						}




					} //END OF FOR LOOP OF CASE1
					break;				
			}//END SWITCH 
		}//END TRY
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pstmt != null )
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
			  throw new ITMException ( d );
			}			
		}
		return errString;
	}//END OF VALIDATION 
	public String itemChanged() throws RemoteException,ITMException
	{
		System.out.println("In item change ,.............");
		return "";
	}
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
        return valueXmlString;
	}
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int ctr = 0;
		String childNodeName = null;
		String columnValue = null,confirmed="";
		String Col_name = "";
		int currentFormNo = 0 ,cnt = 0;
		String  tranId="",deptCode = "", roleCodePrfmer = "",    siteCode = "", empCode = "";
		String sql = "";
		ConnDriver connDriver = new ConnDriver();
		String empLname	="",empFname ="",compStatus="",descr="",statusDescr="",actionStatus="",user="",actionCode="",requestId=""; 
		int serialNo = 0;
		SimpleDateFormat sdf = null;	
		String actionTaken = "",timeSpend="";
		Timestamp currDateTs = null;
		try
		{
		   // GenericUtility genericUtility = GenericUtility.getInstance();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver=null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}			
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");			
			switch(currentFormNo)
			{
				case 1 :
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
					
					if(currentColumn.trim().equalsIgnoreCase( "itm_default" ))
					{	
						user = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");

						sql= "select fn_sysdate()  from dual ";
						pstmt = conn.prepareStatement(sql);												
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currDateTs = rs.getTimestamp(1);
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("currDateTs"+currDateTs);
						sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						//Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
						String currDate = sdf.format(currDateTs).toString();
						sql="select emp_lname, emp_fname "
							+" from employee  "
							+"	where emp_code =? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,user.trim());						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
						  empLname = rs.getString("emp_lname")==null ? "":rs.getString("emp_lname");
						  empFname = rs.getString("emp_fname")==null ? "":rs.getString("emp_fname");
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<emp_lname protect =\"1\">").append("<![CDATA["+empFname+"]]>").append("</emp_lname>"); // initial
						valueXmlString.append("<emp_fname protect =\"1\">").append("<![CDATA["+empLname+"]]>").append("</emp_fname>"); // initial						
						valueXmlString.append("<tran_date protect =\"1\">").append("<![CDATA["+currDate+"]]>").append("</tran_date>"); // initial 
						valueXmlString.append("<eff_date protect =\"1\">").append("<![CDATA["+currDate+"]]>").append("</eff_date>"); // initial 
						valueXmlString.append("<emp_code protect =\"1\">").append("<![CDATA["+user+"]]>").append("</emp_code>"); // initial 
						valueXmlString.append("<time_spend>").append("<![CDATA[.00]]>").append("</time_spend>"); // initial 
			
				    }
					if(currentColumn.trim().equalsIgnoreCase( "req_id" ))
					{
						requestId =genericUtility.getColumnValue("req_id",dom);
						requestId = requestId == null ? "": requestId.trim();
						sql="select comp_stat, dtl_descr  from ser_request  "
							+"	where req_id = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,requestId.trim());						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
						  compStatus = rs.getString("comp_stat")==null ? "":rs.getString("comp_stat");
						  descr = rs.getString("dtl_descr")==null ? "":rs.getString("dtl_descr");
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<dtl_descr protect =\"1\">").append("<![CDATA["+descr+"]]>").append("</dtl_descr>"); // initial 
						if("C".equalsIgnoreCase(compStatus))
						{
							sql="select status_descr  from feedbk_status where status_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,compStatus.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
							  statusDescr = rs.getString("status_descr")==null ? "":rs.getString("status_descr");						 
							}					
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;					
						}
						else
						{
							sql="select min(action_status)  from feedbk_single where req_id = ? and action_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,requestId.trim());
							pstmt.setString(2,compStatus.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
							  actionStatus = rs.getString(1)==null ? "":rs.getString(1);						 
							}					
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if("C".equalsIgnoreCase(actionStatus))
							{
								sql="select serial_no  from feedbk_status where status_code = ?  ";									
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,compStatus.trim());								
								rs = pstmt.executeQuery();
								if(rs.next())
								{
								  serialNo = rs.getInt("serial_no");						 
								}	
								serialNo = serialNo + 10 ;
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;	
								sql="	select status_code, status_descr  from feedbk_status where serial_no = ?  ";									
								pstmt = conn.prepareStatement(sql);
								pstmt.setInt(1,serialNo);									
								rs = pstmt.executeQuery();
								if(rs.next())
								{
								  compStatus = rs.getString("status_code")==null ? "":rs.getString("status_code");	;
								  statusDescr = rs.getString("status_descr")==null ? "":rs.getString("status_descr");	
								}								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;								
							}
							else
							{
								sql="	select status_descr  from feedbk_status where status_code = ?  ";									
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,compStatus.trim());									
								rs = pstmt.executeQuery();
								if(rs.next())
								{								  
								  statusDescr = rs.getString("status_descr")==null ? "":rs.getString("status_descr");	
								}								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;							
							}
						}						
						valueXmlString.append("<action_code>").append("<![CDATA["+compStatus.trim()+"]]>").append("</action_code>"); // initial 
						valueXmlString.append("<status_descr protect =\"1\">").append("<![CDATA["+statusDescr+"]]>").append("</status_descr>"); // initial 
					}
					if(currentColumn.trim().equalsIgnoreCase( "action_code" ))
					{
						actionCode =genericUtility.getColumnValue("action_code",dom);
						actionCode = actionCode == null ? "" :actionCode.trim();
						sql="select status_descr  from feedbk_status where status_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,actionCode.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
						  statusDescr = rs.getString("status_descr")==null ? "":rs.getString("status_descr");						  
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<status_descr protect =\"1\">").append("<![CDATA["+statusDescr+"]]>").append("</status_descr>"); // initial 					
					}
					if(currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )
					{
						user = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
						requestId =genericUtility.getColumnValue("req_id",dom);							
						actionCode =genericUtility.getColumnValue("action_code",dom);
						actionCode = actionCode==null ? "": actionCode.trim();

						timeSpend =genericUtility.getColumnValue("time_spend",dom);
						actionTaken =genericUtility.getColumnValue("action_taken",dom);
						actionTaken = actionTaken == null ? "" :actionTaken.trim();
						actionStatus =genericUtility.getColumnValue("action_status",dom);
						valueXmlString.append("<emp_code protect =\"1\">").append("<![CDATA["+user+"]]>").append("</emp_code>");	
						valueXmlString.append("<action_code protect =\"1\">").append("<![CDATA["+actionCode+"]]>").append("</action_code>");
						valueXmlString.append("<time_spend protect =\"1\">").append("<![CDATA["+timeSpend+"]]>").append("</time_spend>");
						valueXmlString.append("<action_taken protect =\"1\">").append("<![CDATA["+actionTaken+"]]>").append("</action_taken>");	
						valueXmlString.append("<req_id protect =\"1\">").append("<![CDATA["+requestId+"]]>").append("</req_id>");	
						valueXmlString.append("<action_status protect =\"1\">").append("<![CDATA["+actionStatus+"]]>").append("</action_status>");	
					}
					/*if(currentColumn.trim().equalsIgnoreCase( "emp_code" ) )
					{
						empCode =genericUtility.getColumnValue("emp_code",dom);
						sql="select emp_lname, emp_fname "
							+" from employee  "
							+"	where emp_code =? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,empCode.trim());						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
						  empLname = rs.getString("emp_lname")==null ? "":rs.getString("emp_lname");
						  empFname = rs.getString("emp_fname")==null ? "":rs.getString("emp_fname");
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<emp_lname protect =\"1\">").append("<![CDATA["+empFname+"]]>").append("</emp_lname>"); // initial
						valueXmlString.append("<emp_fname protect =\"1\">").append("<![CDATA["+empLname+"]]>").append("</emp_fname>"); // initial
					}*/
					
			}  				
			valueXmlString.append("</Detail1>");
		    valueXmlString.append("</Root>");	
			
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pstmt != null )
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
			  throw new ITMException ( d );
			}
		}		
		return valueXmlString.toString();
	 }//END OF ITEMCHANGE
	 
 }// END OF MAIN CLASS