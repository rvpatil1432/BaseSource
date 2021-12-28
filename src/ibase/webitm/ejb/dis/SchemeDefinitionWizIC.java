/**
 * Wizard Screen for Free Offer on multiple product
 * obj_name: scheme_def_gwt_wiz
 * Developer: Varsha V.
 * */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.xml.internal.messaging.saaj.util.transform.EfficientStreamingTransformer;

import ibase.system.config.AppConnectParm;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class SchemeDefinitionWizIC extends ValidatorEJB implements SchemeDefinitionWizICRemote, SchemeDefinitionWizICLocal 
{
	public String globalXtraParams = "";
	E12GenericUtility genericUtility = new E12GenericUtility();
	DecimalFormat deciFormater = new DecimalFormat("0.00");
   	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
    {
   	   Document dom = null; 
   	   Document dom1 = null; 
   	   Document dom2 = null;
   	   String retString = "";	
       try
       {
    	    System.out.println("*************** Inside wfValData *******************");   	   
	   		System.out.println("xmlString:::["+xmlString+"] \nxmlString1:::["+xmlString1+"] \nxmlString2:::["+xmlString2+"]");
    	    if(xmlString != null && xmlString.trim().length()>0)
	   		{
	   			dom = genericUtility.parseString(xmlString);		
	   		}
	   		if(xmlString1 != null && xmlString1.trim().length()>0)
	   		{
	   			dom1 = genericUtility.parseString(xmlString1);
	   		}
	   		if(xmlString2 != null && xmlString2.trim().length()>0)
	   		{
	   			dom2 = genericUtility.parseString(xmlString2);
	   		}
	   		retString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
	   		System.out.println("errorString::::::::::"+retString);
		}
        catch(Exception e)
        {
	        System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			e.getMessage(); 
			e.printStackTrace();
			throw new ITMException(e);
        }
        return retString;	    
	}
   	
	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		System.out.println("********************** Inside SchemeDefinitionWizIC class ******************");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String sql="";
		String errorType = "",errString="";
		String errCode = "",userId = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int currentFormNo = 0;
		NodeList parentNodeList = null;
		Node parentNode = null;
		NodeList childNodeList = null;
		Node childNode = null;
		int childNodeLength = 0;
	    int cnt = 0;
		String childNodeName = "",siteCode = "",custCode = "";
		Timestamp effDate = null , validToDate = null;
		double baseQty=0.0 ,freeQty=0.0;
		String validToDateStr = "", effDateStr = "", CurrentLineNo="", updateFlag="";
		int allocNumber = 0;
		int detlCnt1 = 0;
		boolean result = true;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{
			conn = getConnection();
			
			if(objContext != null && objContext.trim().length()>0)
			{
			    currentFormNo = Integer.parseInt(objContext);
			    System.out.println("currentFormNo::: in Validation"+currentFormNo);
		        switch(currentFormNo)
		    	{
		    		case 1:
		    		{	
		    			parentNodeList = dom.getElementsByTagName("Detail1");
		    			parentNode = parentNodeList.item(0);
		    			childNodeList = parentNode.getChildNodes();
		    			childNodeLength = childNodeList.getLength();
		    			
		    			for(int ctr = 0; ctr < childNodeLength; ctr++)
		    			{
		    				childNode =  childNodeList.item(ctr);
		    				childNodeName = childNode.getNodeName();
		    				System.out.println("childNodeName:::"+childNodeName);
		    						    					    				
		    				if("site_code".equalsIgnoreCase(childNodeName))
		    				{
		    					siteCode = checkNull(genericUtility.getColumnValue("site_code",dom)).trim();
		    					System.out.println("site_code******"+siteCode);	    					        
		    					if(siteCode == null || siteCode.trim().length() ==0)
				    			{
				    				 errCode = "VMPSITECD";
				    				 errList.add(errCode);
				    				 errFields.add(childNodeName.toLowerCase());
				    				 errorType = errorType(conn, errCode);
			    					 if (errorType.equalsIgnoreCase("E"))
			 			    		 {
			 			    			break;
			 			    		 }
				    			}
				    			else
				    			{
				    				 sql = "select count(*) as cnt from site where site_code=?";
				    				 pstmt = conn.prepareStatement(sql);
				    				 pstmt.setString(1,siteCode);
				    				 rs = pstmt.executeQuery();
				    				 if(rs.next())
				    				 {
				    					 cnt = rs.getInt("cnt");
				    				 }
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
				    				 if(cnt==0)
				    				 {
				    					 errCode = "VTSITEXT";
				    					 errList.add(errCode);
				    					 errFields.add(childNodeName.toLowerCase());
				    					 errorType = errorType(conn, errCode);
				    					 if (errorType.equalsIgnoreCase("E"))
				 			    		 {
				 			    			break;
				 			    		 }
				    				 }
				    			}
		    					
		    			    }
		    				else if("cust_code".equalsIgnoreCase(childNodeName))
		    				{
			    				  custCode = checkNull(genericUtility.getColumnValue("cust_code",dom)).trim();
			    				  System.out.println("Customer Code is :::"+custCode);
			    				  if(custCode == null || custCode.trim().length() == 0)
				    			  {
				    				 	errCode = "VMCUSTCD1";
		  								errList.add(errCode);
		  								errFields.add(childNodeName.toLowerCase());
		  								errorType = errorType(conn, errCode);
		  								if (errorType.equalsIgnoreCase("E"))
				 			    		{
				 			    			break;
				 			    		}
				    			  }
				    			  else 
				    			  {
				    				 sql = "select count(*) as cnt from customer where cust_code=?";
				    				 pstmt = conn.prepareStatement(sql);
				    				 pstmt.setString(1,custCode);
				    				 rs = pstmt.executeQuery();
				    				 if(rs.next())
				    				 {
				    					 cnt = rs.getInt("cnt");
				    				 }
				    				 if(pstmt != null)
				    				 {
				    					 pstmt.close(); 
				    					 pstmt = null;
				    				 }
				    				 if(rs != null)
				    				 {
				    					 rs.close();
				    					 rs = null;
				    				 } 
				    				 if(cnt==0)
				    				 {
				    					 errCode = "VTCUSTCD1";
				    					 errList.add(errCode);
				    					 errFields.add(childNodeName.toLowerCase());
				    					 errorType = errorType(conn, errCode);
				    					 if (errorType.equalsIgnoreCase("E"))
				 			    		 {
				 			    			break;
				 			    		 }
				    				 }
				    			  }
		    					}
			    				else if("eff_from".equalsIgnoreCase(childNodeName) || "valid_upto".equalsIgnoreCase(childNodeName))
			    				{
			    					effDateStr     = checkNull(genericUtility.getColumnValue("eff_from",dom)).trim();
			    					validToDateStr = checkNull(genericUtility.getColumnValue("valid_upto",dom)).trim();
			    					if((effDateStr != null && effDateStr.trim().length() > 0) && (validToDateStr != null && validToDateStr.trim().length() > 0))
			    					{
			    						effDate     = Timestamp.valueOf(genericUtility.getValidDateString(effDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+" 00:00:00.0"));
			    						validToDate = Timestamp.valueOf(genericUtility.getValidDateString(validToDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+" 00:00:00.0"));
			    						if(validToDate.before(effDate))
			    						{
			    							 errCode = "INVDATEBFR";
					    					 errList.add(errCode);
					    					 errFields.add(childNodeName.toLowerCase());
					    					 errorType = errorType(conn, errCode);
					    					 if (errorType.equalsIgnoreCase("E"))
					 			    		 {
					 			    			break;
					 			    		 }
			    						}
			    					}
			    					else
			    					{
			    						 errCode = "SCMINVDATE";
				    					 errList.add(errCode);
				    					 errFields.add(childNodeName.toLowerCase());
				    					 errorType = errorType(conn, errCode);
				    					 if (errorType.equalsIgnoreCase("E"))
				 			    		 {
				 			    			break;
				 			    		 }
			    					}
			    				}
			    				else if("base_quantity".equalsIgnoreCase(childNodeName))
			    				{
			    					baseQty = checkDoubleNull(genericUtility.getColumnValue("base_quantity",dom));
			    					if(baseQty <= 0)
			    					{
			    						 errCode = "VTIBASEQTY";
				    					 errList.add(errCode);
				    					 errFields.add(childNodeName.toLowerCase());
				    					 errorType = errorType(conn, errCode);
				    					 if (errorType.equalsIgnoreCase("E"))
				 			    		 {
				 			    			break;
				 			    		 }
			    					}
			    				}
			    				else if("free_quantity".equalsIgnoreCase(childNodeName))
			    				{
			    					freeQty = checkDoubleNull(genericUtility.getColumnValue("free_quantity",dom));
			    					if(freeQty <= 0)
			    					{
			    						 errCode = "VTIFREEQTY";
				    					 errList.add(errCode);
				    					 errFields.add(childNodeName.toLowerCase());
				    					 errorType = errorType(conn, errCode);
				    					 if (errorType.equalsIgnoreCase("E"))
				 			    		 {
				 			    			break;
				 			    		 }
			    					}
			    				}
		    		      }
		    		}
		    		break;
		    		case 2:
		    		{ 
		    			System.out.println("Inside Case2 Validation:::");	
		    		    parentNodeList = dom2.getElementsByTagName("Detail2");
		    			parentNode = parentNodeList.item(0);
		    			childNodeList = parentNode.getChildNodes();
		    			childNodeLength = childNodeList.getLength();
		    			for(int ctr = 0; ctr < childNodeLength; ctr++)
		    			{
		    				childNode =  childNodeList.item(ctr);
		    				childNodeName = childNode.getNodeName();
		    				System.out.println("childNodeName::: for Case3"+childNodeName);
		    			}	
		    		}
		    		break;	
		    	}	    	
		        int errListSize = errList.size();
			    System.out.println("errListSize::::::::::"+errListSize);
			    int count = 0;
			    String errFldName = null;
			    if (errList != null && errListSize > 0)
			    {
			    	for (count = 0; count < errListSize; count++)
			    	{
			    		errCode = errList.get(count);
			    		errFldName = errFields.get(count);
			    		System.out.println(" testing :errCode .:" + errCode);
			    		errString = getErrorString(errFldName, errCode, userId);
			    		System.out.println("errString>>>>>>>>>"+errString);
			    		errorType = errorType(conn, errCode);
			    		if (errString.length() > 0)
			    		{
			    			String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
			    			bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
			    			errStringXml.append(bifurErrString);
			    			errString = "";
			    		}
			    		if (errorType.equalsIgnoreCase("E"))
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
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errStringXml.append("</Errors> </Root> \r\n");
			throw new ITMException(e);
		}
		finally
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
				} 
				catch (Exception d)
				{
					d.printStackTrace();
					throw new ITMException(d);
				}
		}
		errString = errStringXml.toString();
		System.out.println("testing : final errString : " + errString);
		return errString;
	}
		
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		try
		{
			System.out.println ( "**********************itemChanged Called********");
			System.out.println ( "xmlString :" + xmlString);
			System.out.println ( "xmlString1 :" + xmlString1);
			System.out.println ( "xmlString2 :" + xmlString2);

			if (xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if (xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if (xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch (Exception e)
		{
			String errString1 = "";
			StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
			errString1 = genericUtility.createErrorString( e ) ;
			
			if ( errString.length() > 0)
			{
				String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
				bifurErrString =bifurErrString;//+"<trace>"+errMsg+"</trace>";
				bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
				errStringXml.append(bifurErrString);
				System.out.println("errStringXml .........."+errStringXml);
				errString1 = "";
			}
			errStringXml.append("</Errors></Root>\r\n");  
			throw new ITMException(e);//Added by sarita on 06FEB2018
		}
		System.out.println("final retsting from ic["+errString+"]");
		
		return errString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String returnStr="";
		try{

			returnStr = defaultDataWiz(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ITMException(e);
		}
		return returnStr;
	}

	private String defaultDataWiz( Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
    {
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;;
		ResultSet rs = null, rs1 = null ;
		int currentFormNo=0,ctr = 0;	
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		NodeList childNodeList = null;
		String columnValue="",loginSite="",siteCode="",siteDescr="",custCode="",custName="";
		String sSQL ="", userId = "", chgTerm = "";
		String itemCode = "", itemDescr ="", todayDate = "";
		int domID=0;
		String effFrom = "", validUpto = "";
		SimpleDateFormat sdf = null;
		//added by nandkumar gadkari on 22/08/19-------------
		String effDateStr="",year="",month="",refNo="",itemList="",nature="",schemeCode="",effDate="";
		ArrayList<String> itemCodeList = new ArrayList<String>();
		double qtyPer=0,baseQty=0,freeQty=0;
		int cnt=0;
		Timestamp EffDate=null;
		Iterator<String> itr = null;
		//added by nandkumar gadkari on 22/08/19---------------
		try
		{
			conn = getConnection();
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			todayDate = sdf.format(new java.util.Date());
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			System.out.println("loginSiteCode is>>>>>>>>>>>"+loginSite);
			System.out.println("[SchemeDefinitionWizIC] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>\r\n");
			valueXmlString.append(editFlag).append("</editFlag></header>\r\n");
			
			switch(currentFormNo)
			{
				case 1 :
				{
					domID++;
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNode = childNodeList.item(ctr);
					valueXmlString.append("<Detail1 domID='" + domID + "' selected=\"Y\">\r\n");
					valueXmlString.append("<attribute selected='Y' updateFlag='A' status='N' pkNames='' />");
					System.out.println("currentColumn-------->>[" + currentColumn + "]");
					
					if("itm_default".equalsIgnoreCase(currentColumn.trim()))
					{
						valueXmlString.append("<site_code><![CDATA[").append(loginSite).append( "]]></site_code>");
						sSQL = "select descr from site where site_code=?";
						pstmt = conn.prepareStatement(sSQL);
						pstmt.setString(1,loginSite);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteDescr = rs.getString("descr");	
							System.out.println("SiteDescr>>>>>>>>>"+siteDescr);
						}
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
						valueXmlString.append("<site_descr><![CDATA[").append(siteDescr).append( "]]></site_descr>");
					}
					else if("site_code".equalsIgnoreCase(currentColumn.trim()))
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						System.out.println("Site Code is ===="+siteCode);
						sSQL = "select descr from site where site_code = ?";
						pstmt = conn.prepareStatement(sSQL);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteDescr = rs.getString("descr");	
							System.out.println("SiteDescr>>>>>>>>>"+siteDescr);
						}
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
						valueXmlString.append("<site_descr><![CDATA[").append(siteDescr).append( "]]></site_descr>\r\n");
					}
					else if("cust_code".equalsIgnoreCase(currentColumn.trim()))
					{
						custCode = checkNullandTrim(genericUtility.getColumnValue("cust_code", dom));
						System.out.println("Customer Code is ===="+custCode);
						sSQL = "select cust_name from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sSQL);
						pstmt.setString(1,custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custName = rs.getString("cust_name");	
							System.out.println("custName>>>>>>>>>"+custName);
						}
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
						valueXmlString.append("<cust_name><![CDATA[").append(custName).append( "]]></cust_name>\r\n");
					//added by nandkumar gadkari on 22/08/19----------------start-----------------------------------------------------
						effDate     = checkNull(genericUtility.getColumnValue("eff_from",dom)).trim();
    					if((effDate != null && effDate.trim().length() > 0))
    					{
    						
    						
    					//	sSQL = "select to_char(to_date(?),'YYYY') || to_char(to_date(?),'MM') from dual";
    						EffDate=java.sql.Timestamp.valueOf(genericUtility.getValidDateString( effDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() )+ " 00:00:00.0");
    						sSQL = "select to_char((?),'YYYY') || to_char((?),'MM') from dual";
    						pstmt = conn.prepareStatement(sSQL);
    						/*pstmt.setString(1,effDate);
    						pstmt.setString(2,effDate);*/
    						pstmt.setTimestamp(1,EffDate);
    				          pstmt.setTimestamp(2,EffDate);     
    						rs = pstmt.executeQuery();
    						if(rs.next())
    						{
    							year = rs.getString(1);	
    							System.out.println("year>>>>>>>>>"+custCode+year);
    						}
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
    						valueXmlString.append("<ref_no><![CDATA[").append(custCode+year).append( "]]></ref_no>");
    					}
						
					}
					else if("eff_from".equalsIgnoreCase(currentColumn.trim()))
					{
						custCode = checkNullandTrim(genericUtility.getColumnValue("cust_code", dom));
						
						effDate  = checkNull(genericUtility.getColumnValue("eff_from",dom)).trim();
    					if((effDate != null && effDate.trim().length() > 0))
    					{
    					
    						
    						//sSQL = "select to_char(to_date(?),'YYYY') || to_char(to_date(?),'MM') from dual";
    						EffDate=java.sql.Timestamp.valueOf(genericUtility.getValidDateString( effDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() )+ " 00:00:00.0");
        					
    						sSQL = "select to_char((?),'YYYY') || to_char((?),'MM') from dual";
    						pstmt = conn.prepareStatement(sSQL);
    						/*pstmt.setString(1,effDate);
    						pstmt.setString(2,effDate);*/
    						pstmt.setTimestamp(1,EffDate);
  				          pstmt.setTimestamp(2,EffDate);
    						rs = pstmt.executeQuery();
    						if(rs.next())
    						{
    							year = rs.getString(1);	
    							System.out.println("year>>>>>>>>>"+custCode+year);
    						}
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
    						valueXmlString.append("<ref_no><![CDATA[").append(custCode+year).append( "]]></ref_no>");
    					}
						

					}
					//added by nandkumar gadkari on 22/08/19----------------end-----------
					valueXmlString.append("<chg_user><![CDATA[").append(userId).append( "]]></chg_user>");
					valueXmlString.append("<chg_term><![CDATA[").append(chgTerm).append( "]]></chg_term>");
					valueXmlString.append("<chg_date><![CDATA[").append(todayDate).append( "]]></chg_date>");
					valueXmlString.append("</Detail1>\r\n");
					break;
				}
				case 2 :
				{
					
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNode = childNodeList.item(ctr);
					System.out.println("currentColumn-------->>[" + currentColumn + "]");
					
					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					effFrom = checkNull(genericUtility.getColumnValue("eff_from", dom1));
					validUpto = checkNull(genericUtility.getColumnValue("valid_upto", dom1));
					baseQty = checkDoubleNull(genericUtility.getColumnValue("base_quantity", dom1));
					freeQty = checkDoubleNull(genericUtility.getColumnValue("free_quantity", dom1));
					refNo = checkNull(genericUtility.getColumnValue("ref_no", dom1));//added by nandkumar gadkari on 22/08/19
					System.out.println("custCode ::::["+custCode+"]" + "siteCode ::::["+siteCode+"]");
					System.out.println("effFrom ::::["+effFrom+"]" + "validUpto ::::["+validUpto+"]");
					System.out.println("baseQty ::::["+baseQty+"]" + "freeQty ::::["+freeQty+"]");
					System.out.println("refNo ::::["+refNo+"]");
					if("itm_default".equalsIgnoreCase(currentColumn.trim()))
					{
					
						//added by nandkumar gadkari on 22/08/19----------------START-----------
						sSQL = "SELECT SCHEME_CODE FROM SCHEME_APPLICABILITY WHERE  REF_NO= ? ";
						pstmt = conn.prepareStatement(sSQL);
						pstmt.setString(1,refNo);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							schemeCode = rs.getString(1);	
							if(schemeCode != null && schemeCode.trim().length() > 0)
							{
								
								sSQL = "SELECT ITEM_CODE from SCHEME_APPLICABILITY where SCHEME_CODE= ? ";
								pstmt1 = conn.prepareStatement(sSQL);
								pstmt1.setString(1,schemeCode);
								rs1 = pstmt1.executeQuery();
								
								if(rs1.next())
								{
									itemCode = rs1.getString(1);	
									
								}
								if(rs1 != null)
								{
									rs1.close();
									rs1 = null;
								}
								if(pstmt1 != null)
							    {
									 pstmt1.close(); 
									 pstmt1 = null;
								}
								sSQL = "select qty_per,NATURE from bomdet where BOM_CODE= ? ";
								pstmt1 = conn.prepareStatement(sSQL);
								pstmt1.setString(1,schemeCode);
								rs1 = pstmt1.executeQuery();
								
								while(rs1.next())
								{
									qtyPer = rs1.getDouble(1);
									nature=rs1.getString(2);
									nature = nature == null || nature.trim().length() == 0 ? "" : nature.trim();
									if("C".equalsIgnoreCase(nature))
									{
										baseQty=qtyPer;
									}
									if("F".equalsIgnoreCase(nature))
									{
										freeQty=qtyPer;
									}
								}
								if(rs1 != null)
								{
									rs1.close();
									rs1 = null;
								}
								if(pstmt1 != null)
							    {
									 pstmt1.close(); 
									 pstmt1 = null;
								}
								
								sSQL = "select descr from item where item_code = ?";
								pstmt1 = conn.prepareStatement(sSQL);
								pstmt1.setString(1,itemCode);
								rs1 = pstmt1.executeQuery();
								
								if(rs1.next())
								{
									itemDescr = checkNull(rs1.getString("descr"));
								}
								if(rs1 != null)
								{
									rs1.close();
									rs1 = null;
								}
								domID++;
								valueXmlString.append("<Detail2 domID='" + domID + "' selected=\"Y\">\r\n");
								valueXmlString.append("<attribute selected='Y' updateFlag='A' status='N' pkNames='' />");
								valueXmlString.append("<item_code><![CDATA[" ).append(itemCode).append( "]]></item_code>\r\n" );
								valueXmlString.append("<descr><![CDATA[" ).append(itemDescr).append( "]]></descr>\r\n" );
								valueXmlString.append("<tran_id><![CDATA[" ).append("").append( "]]></tran_id>\r\n" );
								valueXmlString.append("<line_no><![CDATA[" ).append(domID).append( "]]></line_no>\r\n" );
								valueXmlString.append("<chargeable_qty><![CDATA[" ).append(baseQty).append( "]]></chargeable_qty>\r\n" );
								valueXmlString.append("<free_qty><![CDATA[" ).append(freeQty).append( "]]></free_qty>\r\n" );
								valueXmlString.append("<cust_code><![CDATA[" ).append(custCode).append( "]]></cust_code>\r\n" );
								valueXmlString.append("<site_code><![CDATA[" ).append(siteCode).append( "]]></site_code>\r\n" );
								valueXmlString.append("<ref_no><![CDATA[" ).append(refNo).append( "]]></ref_no>\r\n" );
								valueXmlString.append("<scheme_code><![CDATA[" ).append(schemeCode).append( "]]></scheme_code>\r\n" );
								valueXmlString.append("</Detail2>\r\n");
								
								itemCodeList.add(itemCode);
							}
						}
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
						cnt =itemCodeList.size();
						System.out.println("^^^^^^^itemCodeList itemCodeList["+itemCodeList);
						
						if(cnt > 0)
						{
							itemList = "";
							itr = itemCodeList.iterator();
							while(itr.hasNext())
							{
								itemList = itemList + "'"+(String) itr.next() + "'";
								if(itr.hasNext())
								{
									itemList=itemList+ ",";
								}
							}
								
							
						}
						itemList=itemList == null || itemList.trim().length() == 0 ? "' '"	: itemList;
						baseQty = checkDoubleNull(genericUtility.getColumnValue("base_quantity", dom1));
						freeQty = checkDoubleNull(genericUtility.getColumnValue("free_quantity", dom1));
						//added by nandkumar gadkari on 22/08/19----------------end-----------
						
						sSQL = "select descr from item where item_code = ?";
						pstmt1 = conn.prepareStatement(sSQL);
						
						sSQL = "select item_code from siteitem where site_code = ? and item_code not in ("+itemList+")";		//item_code not in ( ) Condition added by nandkumar gadkari on 22/08/19 			 
						pstmt = conn.prepareStatement(sSQL);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery();
						
						while(rs.next())
						{
							itemCode = checkNull(rs.getString("item_code"));
							pstmt1.setString(1,itemCode);
							rs1 = pstmt1.executeQuery();
							
							if(rs1.next())
							{
								itemDescr = checkNull(rs1.getString("descr"));
							}
							if(rs1 != null)
							{
								rs1.close();
								rs1 = null;
							}
							domID++;
							valueXmlString.append("<Detail2 domID='" + domID + "' selected=\"N\">\r\n");
							valueXmlString.append("<attribute selected='N' updateFlag='A' status='N' pkNames='' />");
							valueXmlString.append("<item_code><![CDATA[" ).append(itemCode).append( "]]></item_code>\r\n" );
							valueXmlString.append("<descr><![CDATA[" ).append(itemDescr).append( "]]></descr>\r\n" );
							valueXmlString.append("<tran_id><![CDATA[" ).append("").append( "]]></tran_id>\r\n" );
							valueXmlString.append("<line_no><![CDATA[" ).append(domID).append( "]]></line_no>\r\n" );
							valueXmlString.append("<chargeable_qty><![CDATA[" ).append(baseQty).append( "]]></chargeable_qty>\r\n" );
							valueXmlString.append("<free_qty><![CDATA[" ).append(freeQty).append( "]]></free_qty>\r\n" );
							valueXmlString.append("<cust_code><![CDATA[" ).append(custCode).append( "]]></cust_code>\r\n" );
							valueXmlString.append("<site_code><![CDATA[" ).append(siteCode).append( "]]></site_code>\r\n" );
							valueXmlString.append("<ref_no><![CDATA[" ).append(refNo).append( "]]></ref_no>\r\n" );//added by nandkumar gadkari on 22/08/19
							valueXmlString.append("</Detail2>\r\n");
						}
						
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
					break;
				}
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e);
			System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
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
			} 
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}//end of finally block
		return valueXmlString.toString();
    }//end of defaultDataWiz
	
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
			if(pstmt != null)
			{
			    pstmt.close(); 
			    pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
			    rs = null;
			}
		} 
		catch (Exception ex)
		{
			System.out.println("Exception inside errorType method"+ex);
			ex.printStackTrace();
			throw new ITMException(ex);
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
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}//end of method errorType
	
	/*@Override
	public String handleRequest(HashMap<String, String> reqParamMap) 
	{
		System.out.println("Inside SchemeDefinitionWizIC handleRequest method!!!!");
		System.out.println("reqParamMap :::"+reqParamMap);
		String action = "", retXMLStr = "";
		InitialContext ctx = null;
		Connection conn = null;
		boolean isError = false;
		
		try
		{
			action = (String)reqParamMap.get("action");
			System.out.println("action is :::"+action);
			if("ITEM_CHANGE".equalsIgnoreCase(action))
			{
				String currXmlDataStr = "", hdrXmlDataStr = "", allXmlDataStr = "", currentColumn = "", objContext = "", editFlag = "";
				
				ctx=getInitialContext();
				currXmlDataStr = (String)reqParamMap.get("CUR_XML_STR");
				hdrXmlDataStr = (String)reqParamMap.get("HDR_XML_STR");
				allXmlDataStr = (String)reqParamMap.get("ALL_XML_STR");
				currentColumn = (String)reqParamMap.get("CUR_COLUMN");
				objContext = (String)reqParamMap.get("OBJ_CONTEXT");
				editFlag = (String)reqParamMap.get("EDIT_FLAG");
				
				System.out.println("currXmlDataStr###"+currXmlDataStr);
				retXMLStr = itemChanged(currXmlDataStr, hdrXmlDataStr, allXmlDataStr, objContext, currentColumn, editFlag, globalXtraParams);
				
				System.out.println("retXMLStr["+retXMLStr+"] for action ["+action+"]");
			}
			else if("deleteRow".equalsIgnoreCase(action))
			{
				String userId,signMethod,docType;
				int count;
				PreparedStatement pstmt = null;
				conn = getConnection();
				
				userId=(String)reqParamMap.get("userid");
				signMethod=(String)reqParamMap.get("signmethod");
				docType=(String)reqParamMap.get("doctype");
				
				String sql = "delete from  user_sign_acc where user_id= ? and sign_method=? and doc_type=?";
				System.out.println("sql"+sql);
				pstmt = conn.prepareStatement(sql);
					
				pstmt.setString(1,userId);
				pstmt.setString(2,signMethod);
				pstmt.setString(3,docType);
					
				count=	pstmt.executeUpdate();
					

				if(pstmt != null)
				{
					 pstmt.close(); 
					 pstmt = null;
				}
				
				if (count > 0)
				{
					conn.commit();

				}
				else if(count == 0)
				{
					conn.rollback();
				}
					
				retXMLStr="row deleted successfully";
			}	
		}
		catch(Exception e)
		{
			isError = true;
			System.out.println("GstRegNoPosEJB.handleRequest()"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try{
				if (conn != null && !conn.isClosed())
				{
					conn.close();
					conn=null;
				}
			}
			catch(Exception ex)
			{
				System.out.println("exception "+ex);
			}
		}
			
		return retXMLStr;
	}
	
	public InitialContext getInitialContext()throws ITMException
	{
		InitialContext ctx = null;
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
		}
		catch(ITMException itme)
		{
			System.out.println("GstRegNoPosEJB.getInitialContext()");
			throw itme;
		}
		catch(Exception e)
		{
			System.out.println("GstRegNoPosEJB.getInitialContext()"+e.getMessage());
			throw new ITMException(e);
		}
		return ctx;
	}//end of method getInitialContext
*/	
	/*public boolean preDomExists(Document dom, String currentFormNo) throws ITMException
	{
		NodeList parentList = null;
		NodeList childList = null;
		Node childNode = null;
		boolean selected = false;

		try
		{
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);
			if ( parentList.item(0) != null )
			{
				childList = parentList.item(0).getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);
					if((childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null))					
					{
						System.out.println("Column found!!!" + childNode.getNodeName());
						selected = true; 
						break;
					}
				}
			}
		}
		catch ( Exception e )
		{
			System.out.println( "Exception preDomExists :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("preDomExists =["+selected+"] and currentFormNo =["+currentFormNo+"]");
		return selected;
	}//end of method preDomExists
*/	
	/*public String getPrevFormVal( Document dom, String currentFormNo, ArrayList temp, String colName ) throws ITMException
	{		
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		Element elementAttr = null;
		String childNodeName = "";
		String columnValue = "";
		String returnPrevStr = "";
		Document chgPreDom = null;
		int domID = 0;
		
		try
		{
			chgPreDom = genericUtility.parseString("<Root/>");
			//System.out.println("currentFormNo ["+currentFormNo+"] \n dom["+genericUtility.serializeDom(dom)+"]");
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);
			int parentNodeListLength = parentList.getLength();
			System.out.println("parentlistlength["+parentNodeListLength+"]");
			for (int prntCtr = 0; prntCtr < parentNodeListLength; prntCtr++ )
			{							
				parentNode = parentList.item(prntCtr);
				childList = parentNode.getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);
					if((childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null))					
					{
						columnValue = childNode.getFirstChild().getNodeValue().trim();
						//	System.out.println("childNode.getNodeName()["+childNode.getNodeName()+"]columnValue["+columnValue+"]temp["+temp+"] ");
						if ( childNode.getNodeName().equalsIgnoreCase(colName) && temp.contains(columnValue))
						{
							elementAttr = (Element)parentNode;
							if ( parentNode.getAttributes().getNamedItem( "domID" ) != null )
							{
								//Changes By Prajyot On 22-11-2011 
								//[ For Items Details to maintain the default domID it is necessary to discard the new domID generation ]
								//elementAttr.setAttribute( "domID" , Integer.toString(domID) );
								elementAttr.setAttribute( "selected" , "Y" );
								domID++;
							}
							Node importedNode = chgPreDom.importNode( parentNode, true );
							chgPreDom.getDocumentElement().appendChild( importedNode );
							break;
						}

					}
				}
			}//END OF FOR LOOP
			if(currentFormNo.equals("1"))
			{
				domID = 1;
			}
			returnPrevStr = genericUtility.serializeDom(chgPreDom);
			if(returnPrevStr.indexOf("Detail"+currentFormNo) != -1)
			{
				returnPrevStr = returnPrevStr.substring(returnPrevStr.indexOf(">") + 1, returnPrevStr.lastIndexOf("</"));
			}
			returnPrevStr = returnPrevStr.indexOf("Detail"+currentFormNo) != -1 ? returnPrevStr : "";
		}//end of try block
		catch ( Exception e )
		{
			System.out.println( "Exception : :getPrevFormValues :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return returnPrevStr;
	}//end of method getPrevFormVal 
*/	
	/*public String getAttributeVal(Node dom, String attribName )throws ITMException
	{
		System.out.println("Inside getAttributeVal method is !!!!!!!!!!!!!!");
		String AttribValue = null;
		try
		{
			//NodeList detailList = dom2.getChildNodes();
			NodeList detailList = dom.getChildNodes();
			System.out.println("Details NodeList is ====["+detailList+"]" + "Length is ======"+detailList.getLength());
			int detListLength = detailList.getLength();
			for(int ctr = 0; ctr < detListLength; ctr++)
			{
				Node curDetail = detailList.item(ctr);
				if(curDetail.getNodeName().equals("attribute")) 
				{
					AttribValue = curDetail.getAttributes().getNamedItem(attribName).getNodeValue();
					System.out.println("Attribute Value is =====["+AttribValue+"]");
					break;
				}
				else
				{
					continue;
				}
			}		
		}
		catch (Exception e)
		{
			System.out.println("Exception : : searchNode :"+e); 
			throw new ITMException(e);
		}
		return AttribValue;
	}*/
	
	/*public int getNumOfNonDelDetail(Document dom2,int detailNo) throws ITMException
	{
		Node childNode = null;
		NodeList updateList;
		String childNodeName = "";
		String updateFlag="";
		int cntr=0;
		System.out.println("Inside getXmlDocument method!!!!!!!!!!!!!!");
		try
		{
			System.out.println("detailString value is =="+genericUtility.serializeDom(dom2));
			NodeList detailNoteList = dom2.getElementsByTagName("Detail"+detailNo);
			for(int cnt = 0;cnt<detailNoteList.getLength();cnt++)
			{
				Node pNode=detailNoteList.item(cnt);
				NodeList cNodeList=pNode.getChildNodes();
				childNodeListLength = cNodeList.getLength();			
				childNodeName = pNode.getNodeName();
				//System.out.println("pNode::["+pNode+"]"+"cNodeList::["+cNodeList+"]");
				updateFlag = getAttributeVal(pNode,"updateFlag");
				System.out.println("Before updateFlag counter is ===["+cntr+"]"+"\t"+"updateFlag [" + updateFlag + "]");
				//if(!updateFlag.equalsIgnoreCase("D"))
				
				if("A".equalsIgnoreCase(updateFlag))//changes by sarita on 06FEB2018
				{
					cntr++;
				}
				System.out.println("After updateFlag counter is ===["+cntr+"]"+"\t"+"updateFlag [" + updateFlag + "]");
				//System.out.println("Counter is ==="+cntr);	
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : : getNumOfNonDelDetail :"+e); 
			e.printStackTrace();
			throw new ITMException(e);
		}
		return cntr;
	}*/
	
	/*public String getUpdateFlag(Document dom2,int detailNo) throws ITMException
	{
		String updateFlag="";
		Node childNode = null;
		NodeList updateList;
		String childNodeName = "";
		try
		{
			System.out.println("detailString value is =="+genericUtility.serializeDom(dom2));
			NodeList detailNoteList = dom2.getElementsByTagName("Detail"+detailNo);
			for(int cnt = 0;cnt<detailNoteList.getLength();cnt++)
			{
				Node pNode=detailNoteList.item(cnt);
				NodeList cNodeList=pNode.getChildNodes();
				childNodeListLength = cNodeList.getLength();			
				childNodeName = pNode.getNodeName();
				//System.out.println("pNode::["+pNode+"]"+"cNodeList::["+cNodeList+"]");
				updateFlag = getAttributeVal(pNode,"updateFlag");
				System.out.println("updateFlag [" + updateFlag + "]");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : : getNumOfNonDelDetail :"+e); 
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		return updateFlag;
	}*/
	
	private double checkDoubleNull(String str) 
	{
		if ("null".equals(str) || str == null || str.trim().length() == 0) {
			return 0;
		} else {
			return Double.parseDouble(str);
		}
	}
	
	private String checkNull(String input) 
	{
		return input == null ? "" : input;
	}
	private String checkNullandTrim(String input) 
	{
		return input == null ? "" : input.trim();
	}
}














