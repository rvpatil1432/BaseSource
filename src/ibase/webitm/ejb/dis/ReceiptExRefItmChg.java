package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

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


//public class ReceiptExRefItmChgEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class ReceiptExRefItmChg extends ValidatorEJB implements ReceiptExRefItmChgLocal, ReceiptExRefItmChgRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*
	public void ejbCreate() throws RemoteException, CreateException
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
			
			System.out.println("In item change xmlString [" + xmlString + "]");
			System.out.println("In item change xmlString1 [" + xmlString1 + "]");
			System.out.println("In item change xmlString2 [" + xmlString2 + "]");
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
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
		String columnValue = null,confirmed=null;
		String Col_name = "";
		int currentFormNo = 0 ,cnt = 0;
		String  tranId="",deptCode = "", roleCodePrfmer = "",    siteCode = "", empCode = "";
		String sql = "",descr = "",empFName = "", empMName = "", empLName = "",roleCodeAprv="";
		ConnDriver connDriver = new ConnDriver();
		Timestamp tranDate =null;		
		String disptNo ="";	
		Timestamp disptDate  =null;  	
		String InvcNo =""; 		 
		Timestamp InvcDate = null;  
		String exciseRef =""; 	
		Timestamp exciseRefDate =null; 		
		Timestamp exciseDateNew =null;
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
					valueXmlString.append("<Detail>");
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

					System.out.println("xtraParams>>>>>>>>>>>>>"+xtraParams);
					if(currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
					{
						//tranId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"tran_id");
						tranId = getColumnValue("tran_id",dom);
						System.out.println("tranId>>>>>>>>>>>>>"+tranId);
						
						if (tranId != null && tranId.trim().length() > 0) 
						{
						sql = "select confirmed  from porcp where tran_id = '"+tranId+"' " ;
						pstmt=conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							confirmed =  rs.getString(1) == null ? "N" : rs.getString(1);
						
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs= null;
						if(confirmed.equalsIgnoreCase("Y"))
						{
							sql = "select dc_no,dc_date,invoice_no,invoice_date,excise_ref,excise_ref_date "
								+" from porcp  where tran_id = '"+tranId+"' " ;
							pstmt=conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								disptNo =  rs.getString(1) == null ? "" : rs.getString(1);
								disptDate =  rs.getTimestamp(2) ;
								InvcNo =  rs.getString(3) == null ? "" : rs.getString(3);
								InvcDate =  rs.getTimestamp(4) ;
								exciseRef =  rs.getString(5) == null ? "" : rs.getString(5);
								exciseRefDate =  rs.getTimestamp(6) ;				
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs= null;
						}	
						if(tranId != null && tranId.trim().length()>0)
						{
							valueXmlString.append("<tran_id>").append(tranId).append("</tran_id>\r\n");						
							valueXmlString.append("<delivery_no_o>").append(disptNo).append("</delivery_no_o>\r\n");
							valueXmlString.append("<delivery_no>").append(disptNo).append("</delivery_no>\r\n");
							if (disptDate != null)
							{
								valueXmlString.append("<challan_date_o>").append(new SimpleDateFormat(genericUtility.getApplDateFormat()).format(disptDate)).append("</challan_date_o>\r\n");
								valueXmlString.append("<challan_date>").append(new SimpleDateFormat(genericUtility.getApplDateFormat()).format(disptDate)).append("</challan_date>\r\n");
							}
							else
							{
								valueXmlString.append("<challan_date_o/>\r\n");
								valueXmlString.append("<challan_date/>\r\n");
							}
							
							valueXmlString.append("<invoice_no_o>").append(InvcNo).append("</invoice_no_o>\r\n");
							valueXmlString.append("<invoice_no>").append(InvcNo).append("</invoice_no>\r\n");
							if (InvcDate != null)
							{
								valueXmlString.append("<invoice_date_o>").append(new SimpleDateFormat(genericUtility.getApplDateFormat()).format(InvcDate)).append("</invoice_date_o>\r\n");
								valueXmlString.append("<invoice_date>").append(new SimpleDateFormat(genericUtility.getApplDateFormat()).format(InvcDate)).append("</invoice_date>\r\n");
							}
							else
							{
								valueXmlString.append("<invoice_date_o/>\r\n");
								valueXmlString.append("<invoice_date/>\r\n");
							}
							valueXmlString.append("<excise_ref_o>").append(exciseRef).append("</excise_ref_o>\r\n");
							valueXmlString.append("<excise_ref>").append(exciseRef).append("</excise_ref>\r\n");
							if (exciseRefDate != null)
							{
								valueXmlString.append("<excise_date_o>").append(new SimpleDateFormat(genericUtility.getApplDateFormat()).format(exciseRefDate)).append("</excise_date_o>\r\n");
								valueXmlString.append("<excise_date>").append(new SimpleDateFormat(genericUtility.getApplDateFormat()).format(exciseRefDate)).append("</excise_date>\r\n");
							}
							else
							{
								valueXmlString.append("<excise_date_o/>\r\n");
								valueXmlString.append("<excise_date/>\r\n");
							}
						}
					}
						
						
				    }//end of itm default	
			}  				
			valueXmlString.append("</Detail>");
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
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("valueXmlString["+valueXmlString.toString() + "]");
		return valueXmlString.toString();
	 }//END OF ITEMCHANGE			
 }// END OF MAIN CLASS