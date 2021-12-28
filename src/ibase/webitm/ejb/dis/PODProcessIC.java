package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.util.Date;
import java.sql.*;

import org.w3c.dom.*;

import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;

import javax.ejb.Stateless; 
@Stateless
public class PODProcessIC extends ValidatorEJB implements PODProcessICLocal,PODProcessICRemote
{	

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

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

			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : PODProcessIC.java : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		
		return errString;
	} //end of wfValData 
	
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
	  	
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
		int cnt=0;
		int currentFormNo=0;
		int childNodeListLength;
		ConnDriver connDriver = new ConnDriver();		
		String userId="",errCode="";
		//GenericUtility genericUtility = GenericUtility.getInstance();
	     
        try
		   {
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
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
				
				    if (childNodeName.equalsIgnoreCase("site_code"))
					{
				    	String siteCode="";
				    	siteCode = genericUtility.getColumnValue("site_code",dom);	
				    	siteCode=siteCode==null ? "" :siteCode.trim();
						if (siteCode.trim().length() == 0 )
						{							
								errCode = "VTSITECNE";
								errString = getErrorString("site_code",errCode,userId);
								break;
						
						}else
						{							
							cnt=ProofOfDelivery.getInstance().getDBRowCount(conn,"site","site_code",siteCode);
							if(cnt == 0)
							{
								System.out.println("site_code not exist validation fire");
								errCode = "VTSITENEX";
								errString = getErrorString("site_code",errCode,userId);
								break;
							}
						
						}
                      }
				    if (childNodeName.equalsIgnoreCase("cust_code_from"))
					{
				    	String custCodeFrm="";
				    	custCodeFrm = genericUtility.getColumnValue("cust_code_from",dom);
				    	custCodeFrm=custCodeFrm==null ? "" :custCodeFrm.trim();
						if (custCodeFrm.trim().length() == 0 )
						{							
								errCode = "VTCUSTCNE";
								errString = getErrorString("cust_code_from",errCode,userId);
								break;
						
						}else
						{							
							cnt=ProofOfDelivery.getInstance().getDBRowCount(conn,"customer","cust_code",custCodeFrm);
							if(cnt == 0)
							{
								System.out.println("custCodeFrom not exist validation fire");
								errCode = "VTCUSTNEX";
								errString = getErrorString("cust_code_from",errCode,userId);
								break;
							}
						
						}
				    	
					}
				    if (childNodeName.equalsIgnoreCase("cust_code_to"))
					{
				    	String custCodeTo="";
				    	custCodeTo = genericUtility.getColumnValue("cust_code_to",dom);
				    	custCodeTo=custCodeTo==null ? "" :custCodeTo.trim();
						if (custCodeTo.trim().length() == 0 )
						{							
								errCode = "VTCUSTCNE";
								errString = getErrorString("cust_code_to",errCode,userId);
								break;
						
						}else
						{							
							cnt=ProofOfDelivery.getInstance().getDBRowCount(conn,"customer","cust_code",custCodeTo);
							if(cnt == 0)
							{
								System.out.println("custCodeTo not exist validation fire");
								errCode = "VTCUSTNEX";
								errString = getErrorString("cust_code_to",errCode,userId);
								break;
							}
						
						}
				    	
					   }	
					
				    } //end for loop
				
				
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
	
	
	
	
	}//END OF VALIDATION 

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("------------ itemChanged method called process POD-----------------");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try
		{
			dom = parseString(xmlString); 
			System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1); 
			System.out.println("xmlString1" + xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [EpaymentICEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();		
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
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
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
					
					if( currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						String siteDescr="";
						System.out.println("------in item default process screen of POD----------");						
						loginSite=loginSite==null ? "" :loginSite.trim();
						System.out.println("Login Site code-=-=-=>>["+loginSite+"]");
						siteDescr=ProofOfDelivery.getInstance().getNameOrDescrForCode(conn, "SITE", "DESCR", "SITE_CODE", loginSite);
						valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
						valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>");
						

					}
					if ( currentColumn.trim().equals( "site_code" ) )
					{
						String siteCode="",siteDescr;
						siteCode = genericUtility.getColumnValue( "site_code", dom );
						siteCode=siteCode==null ? "" :siteCode.trim();
						System.out.println("item chng Site code-=-=-=>>["+siteCode+"]");
						if(siteCode.length() > 0){
							siteDescr=ProofOfDelivery.getInstance().getNameOrDescrForCode(conn, "SITE", "DESCR", "SITE_CODE", siteCode);
						    valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>");
						
						}else{
							 valueXmlString.append("<descr>").append("<![CDATA[]]>").append("</descr>");
						}	
						
												
					}			
					
					valueXmlString.append("</Detail1>");						
					break;
			}//END OF switch
			valueXmlString.append("</Root>");
			
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(conn!=null)
				{					
					conn.close();
				}
				conn = null;
			}
			catch(Exception d)
			{
			  d.printStackTrace();
			}
		}
		
		
		return valueXmlString.toString();
	}//END OF ITEMCHANGE	


}
