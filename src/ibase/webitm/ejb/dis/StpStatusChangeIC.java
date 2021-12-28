/*
 * Request Id=W19LSUN009
 * author: Mrunalini Sinkar
 * date 24-march-2020
 * 
 */
package ibase.webitm.ejb.dis;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
@javax.ejb.Stateless
public class StpStatusChangeIC extends ValidatorEJB implements SalePersonICRemote,SalePersonICLocal//implements SessionBean
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	java.sql.PreparedStatement pstmt=null;
	java.sql.ResultSet rs = null;

	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println("defaultDataWiz xmlString:::: ["+xmlString+"]");
		System.out.println("defaultDataWiz xmlString1::: ["+xmlString1+"]");
		System.out.println("defaultDataWiz xmlString2::: ["+xmlString2+"]");
		try
		{
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
			errString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams, formName );
			//System.out.println ( "ErrString :" + errString);
		}
		catch (Exception e)
		{
			System.out.println ( "Exception :StpStatusChangeIC :defaultDataWiz(String,String):" + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
		}
		//System.out.println ( "returning from RealTimeReportEJB defaultDataWiz" );
		return errString;
	}
	public String itemChanged( Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName ) throws RemoteException,ITMException
	{
		System.out.println("Inside the itemChanged of slales person link");
		StringBuffer valueXmlString = new StringBuffer();
		
		String columnValue = null;
		Connection conn = null;
		int currentFormNo = 0;

		try
		{
			conn = getConnection();
			String loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			String loginEmpCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			InetAddress ownIP=InetAddress.getLocalHost();
			String chgTerm = ownIP.getHostAddress();
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			columnValue = genericUtility.getColumnValue( currentColumn, dom );
			System.out.println("FORM NO:::"+currentFormNo+"columnValue::::"+columnValue+"chgTerm"+chgTerm+"loginEmpCode["+loginEmpCode+"]");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");
			java.util.Date date = Calendar.getInstance().getTime();
			DateFormat dtFormat = new SimpleDateFormat(getApplDateFormat());
			String currentDate = dtFormat.format( date );

			if("stpstatus".equalsIgnoreCase(formName))
			{
				if( currentColumn.equalsIgnoreCase( "itm_default" ))
				{
					String sprsCode = checkNull(genericUtility.getColumnValue("sales_pers", dom2, "1"));
					System.out.println("sprsCode NO:::"+sprsCode+"currentDate::::"+currentDate);

					valueXmlString.append( "<Detail"+objContext+" domID='1' formName='"+formName+"'>\r\n" );
					valueXmlString.append( "<attribute pkNames='' status='N' updateFlag='A' selected='N' />\r\n");
					valueXmlString.append( "<sales_pers  protect='1'><![CDATA[" ).append( sprsCode ).append( "]]></sales_pers>\r\n" );
					valueXmlString.append( "<tran_date><![CDATA[" ).append( currentDate ).append( "]]></tran_date>\r\n" );
					valueXmlString.append( "<chg_date><![CDATA[" ).append( currentDate ).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append( loginEmpCode ).append( "]]></chg_user>\r\n" );
					valueXmlString.append( "</Detail1>\r\n" );
				}
				//Added by Bhagyashri T for Set mandatorystp reason when resubmit[11/02/21][Start]
				else if(currentColumn.equalsIgnoreCase( "stp_status" ))
				{
					String stpStatus = checkNull(genericUtility.getColumnValue("stp_status", dom, "1"));
					//String scName =checkNull( genericUtility.getColumnValueFromNode( "sc_name",parentNode ) );
					System.out.println("stpStatus is ::"+stpStatus);
					valueXmlString.append( "<Detail"+objContext+" domID='1' formName='"+formName+"'>\r\n" );
					if(stpStatus.trim().equalsIgnoreCase("2")|| stpStatus.trim().equalsIgnoreCase("Resubmit"))
					{
						valueXmlString.append( "<stp_status_reason mandatory='1'><![CDATA[" ).append( "" ).append( "]]></stp_status_reason>\r\n" );
					}
					else
					{
						valueXmlString.append( "<stp_status_reason mandatory='0'><![CDATA[" ).append( "" ).append( "]]></stp_status_reason>\r\n" );
					}
					valueXmlString.append( "</Detail1>\r\n" );
				}
				
				//Added by Bhagyashri T for Set mandatorystp reason when resubmit[11/02/21][End]
			}
			valueXmlString.append("</Root>\r\n");
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
				if(conn!=null)
				{
					conn.close();
					conn = null;
					System.out.println("[StpStatusChangeIC]connection 2 is closed......");
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}
			System.out.println("[StpStatusChangeIC] Connection is Closed");
		}
		System.out.println("valueXmlString:::::"+valueXmlString.toString());
		return valueXmlString.toString();
	}//END OF ITEMCHNGE

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			System.out.println("xmlString ["+xmlString+"]");
			System.out.println("xmlString1 ["+xmlString1+"]");
			System.out.println("xmlString2:::::: ["+xmlString2+"]");
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams, formName);
			System.out.println ( "ErrString: " + errString);
		}
		catch(Exception e)
		{
			System.out.println ( "Exception: StpStatusChangeIC: wfValData(String xmlString): " + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
		}
		//System.out.println ( "Returning from RealTimeReportEJB wfValData" );
		return (errString);
	}
	
	
	
	
	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException, ITMException
	{
		System.out.println("Inside the wfValData of sales person link");
		NodeList parentList =null;
		NodeList childList= null;
		int parentNodeListLength =0;
		int noOfChilds = 0;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		int currentFormNo = 0;
		int ctr = 0;
		String childNodeName = "";
		String columnValue = "";
		String errString="";
		
		try
		{
			conn = getConnection();
			String userId = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			System.out.println("formName is:["+formName+"]");

			if("stpstatus".equalsIgnoreCase(formName))
			{
				parentList = dom2.getElementsByTagName("Detail"+ currentFormNo);
				parentNodeListLength = parentList.getLength();
				System.out.println("parentNodeListLength::"+parentNodeListLength);

				for (int prntCtr = 0; prntCtr < parentNodeListLength; prntCtr++ )
				{
					parentNode = parentList.item(prntCtr);
					childList = parentNode.getChildNodes();
					noOfChilds = childList.getLength();
					System.out.println("noOfChilds::"+noOfChilds);
					for (ctr = 0; ctr < noOfChilds; ctr++)
					{
						childNode = childList.item(ctr);
						columnValue ="";
						if( childNode.getNodeType() != Node.ELEMENT_NODE )
						{
							continue;
						}
						if ( childNode != null && childNode.getFirstChild() != null )
						{
							columnValue = childNode.getFirstChild().getNodeValue();
						}
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName::"+childNodeName);

						if (childNodeName.equalsIgnoreCase("tran_date"))
						{
							if((childNode.getFirstChild() == null)||(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() == null) ||
									( childNode.getFirstChild().getNodeValue() != null && "null".equalsIgnoreCase(childNode.getFirstChild().getNodeValue())))
							{
								//System.out.println("Tran date cannot be null:");
								errString = itmDBAccessLocal.getErrorString("event_date","NULEVTDATE",userId);
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("sales_pers"))
						{
							if((childNode.getFirstChild() == null)||(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() == null) ||
									( childNode.getFirstChild().getNodeValue() != null && "null".equalsIgnoreCase(childNode.getFirstChild().getNodeValue())))
							{
								//System.out.println("Tran date cannot be null:");
								errString = itmDBAccessLocal.getErrorString("sales_pers","BLNKSALESP",userId);
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("stp_status"))
						{
							if((childNode.getFirstChild() == null)||(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() == null) ||
									( childNode.getFirstChild().getNodeValue() != null && "null".equalsIgnoreCase(childNode.getFirstChild().getNodeValue())))
							{
								//System.out.println("Tran date cannot be null:");
								errString = itmDBAccessLocal.getErrorString("stp_status","NULLSTP",userId);
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("stp_status_reason"))
						{
							String stpStatus = checkNull(genericUtility.getColumnValue( "stp_status" , dom2 ,objContext));

							if((childNode.getFirstChild() == null)||(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() == null) ||
									( childNode.getFirstChild().getNodeValue() != null && "null".equalsIgnoreCase(childNode.getFirstChild().getNodeValue())))
							{
								System.out.println("stpStatus::"+stpStatus);

								if("2".equalsIgnoreCase(stpStatus))
								{
									errString = itmDBAccessLocal.getErrorString("stp_status","NULLSTPSTA",userId);
									break;
								}
							}
						}


					}
				}
			}
			
			

		} catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
				{
					conn.close();
					conn = null;

				}
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return errString;
	}
	
	public String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
	
}