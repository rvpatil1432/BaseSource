/*    Created   : Base Information Management
      purpose   : validation and data Entry
	  Period    : 20/03/07 to 20/03/07
*/ 
 
package ibase.webitm.ejb.dis;  
 
import java.rmi.RemoteException;  
import java.util.*;
import java.util.Date;
import java.text.*;
import java.sql.*;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import javax.ejb.*;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3


//public class SordFormAttAmdEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class SordFormAttAmd extends ValidatorEJB implements SordFormAttAmdLocal, SordFormAttAmdRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("*****************SordFormAttAmd EJB CALLED***********************");
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
		System.out.println("***************IN wfValData()*******************");
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		System.out.println("*****************default itemChanged called************************");
		return "";
	}
	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("\n\n\n\n****************itemChanged called********************\n\n\n\n");
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
			System.out.println("Exception : [PreShipInvoiceEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement  stmt = null;
		ResultSet    rs = null;
		String      sql = "";
			
		
		int childListLength=0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		StringBuffer valueXmlString = null;  
		int currentFormNo = 0;
		try
		{
			conn = getConnection(); 
					
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
						
			
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			switch(currentFormNo)
			{		
				case 1:
					parentNodeList = dom1.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail1>");			
					System.out.println("\n\n\n\n**************IN CASE 1 BEFORE ITM_DEFAULT CALLING************\n\n\n\n\n");
					if(currentColumn.trim().equals("itm_default"))
					{
						//GenericUtility genericUtility = GenericUtility.getInstance();
		 	  			Calendar cal = Calendar.getInstance();
			 			Date date = cal.getTime();
			 			SimpleDateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			 			String tranDate = dateFormat.format(date);
			 			tranDate = tranDate.toString();
						System.out.println("\n\n\n\n**************IN CASE 1 AFTER ITM_DEFAULT CALLING************\n\n\n\n\n");
		 		    	valueXmlString.append("<tran_date>").append(tranDate).append("</tran_date>");
		 		    	valueXmlString.append("<confirmed>").append("N").append("</confirmed>");
		 		     }			
			         		    			
					valueXmlString.append("</Detail1>");
					break;
				
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			System.out.println("Exception :[][itemChanged::case 1:] :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			conn = null;
		}
	return valueXmlString.toString();
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
			System.out.println("Exception : PayableOpeningsEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return (errString);
	}
	
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errCode = "";
		Connection conn = null;
		PreparedStatement  pstmt = null ;
		Statement stmt = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = ""; 
		String childNodeValue; 
		String  errString = "";
		int currentFormNo = 0;
		int childNodeListLength;
		ResultSet rs = null;
		String sql = "";
		String userId = "";
		System.out.println("\n\n\n\n******************WfValData called*********************\n\n\n\n");
		int cnt;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		userId = getValueFromXTRA_PARAMS(xtraParams,"userId");
		try
		{
			conn = getConnection();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("  currentFormNo "+currentFormNo);
			switch(currentFormNo)
			{
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(int ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						childNodeValue=null;
						if(childNode.getFirstChild()!= null)
						{
							childNodeValue = childNode.getFirstChild().getNodeValue();
						}
						if("site_code__fr".equalsIgnoreCase(childNodeName))
						{	
							System.out.println("**************value of site_code_fr "+childNodeValue);
							if(childNodeValue == null)
							{
								errCode = "VMSITECD1";//SITE CODE SHOULD BE NOT NULL
								errString = itmDBAccessEJB.getErrorString("site_code__fr",errCode,userId,"",conn);
								break;
							}
						}
						
						if("site_code__to".equalsIgnoreCase(childNodeName))
						{	
							System.out.println("**************value of site_code_to "+childNodeValue);
							if(childNodeValue == null)
							{
								errCode = "VMSITECD1";//SITE CODE SHOULD BE NOT NULL
								errString = itmDBAccessEJB.getErrorString("site_code__fr",errCode,userId,"",conn);
								break;
							}
						}

						if ("tran_id__fr".equalsIgnoreCase(childNodeName))
						{	
							System.out.println("**************value of sale_order_fr "+childNodeValue);
							if(childNodeValue == null || childNodeValue.trim().length()== 0 )
							{
								errCode = "VTSO1"; //SALE ORDER SHOULD BE NOT NULL
								errString = itmDBAccessEJB.getErrorString("tran_id__fr",errCode,userId,"",conn);
								break;
							}
						}
						
						if ("tran_id__to".equalsIgnoreCase(childNodeName))// NOT NULL TESTING  FOR PREF_INV//
						{	
							System.out.println("**************value of sale_order_to "+childNodeValue);
							if(childNodeValue == null || childNodeValue.trim().length()==0)
							{
								errCode = "VTSO1";//SALE ORDER SHOULD BE NOT NULL
								errString = itmDBAccessEJB.getErrorString("tran_id__to",errCode,userId,"",conn);
								break;
							}
						}
			
					}// FOR LOOP END//02SD670001
					break;
						
				}  //SWITCH end
		}   //try  end
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}					
		finally
		{
			pstmt = null ;
			stmt = null ;
			rs = null ;
		}
		return errString;
	}
} // CLASS END
