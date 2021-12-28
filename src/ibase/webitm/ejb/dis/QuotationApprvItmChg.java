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
public class QuotationApprvItmChg extends ValidatorEJB implements QuotationApprvItmChgLocal, QuotationApprvItmChgRemote
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

					System.out.println("xtraParams>>>>>>>>>>>>>"+xtraParams);
					if(currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
					{
						
						
				    }//end of itm default	
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
		}
		System.out.println("valueXmlString>>>>>>>>>>>"+valueXmlString);
		return valueXmlString.toString();
	 }//END OF ITEMCHANGE
	 
 }// END OF MAIN CLASS