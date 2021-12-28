package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class EwaybillIC extends ValidatorEJB implements EwaybillICLocal,EwaybillICRemote{
	boolean flag;//this flag will apply validation on the invoice value if the state is either Intra or Inter
	@Override
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
		   String xtraParams) throws RemoteException, ITMException 
	{
		String errString="";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("Calling inside the wfvaldata of e-way bill...");
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString=wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return (errString);
	}
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException 
	{
		String errString="";
		String errCode = "";
		String errorType="";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		String userId="";
		ResultSet rs = null;
		String sql = "";
		int cnt=0;
		int currentFormNo=0;
		int childNodeListLength;
		/*ConnDriver connDriver = new ConnDriver();*/
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//variables used for different fields in screen.
		String siteCode="";
		String tranDateFr="";
		String tranDateTo="";
		String sundryCodeFr="";
		String sundryCodeTo="";
		String tranSer="";
		String invValue="";
		int limit=0;
		String stateValue="";
		E12GenericUtility genericUtility = new  E12GenericUtility();
		DistCommon distCommon = new DistCommon();
		try
		{
			System.out.println("@@@@@@@@ wfvaldata called");
			/*conn = connDriver.getConnectDB("DriverITM");
			connDriver = null;*/
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
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
						
						if(childNodeName.equals("site_code"))
						{
							siteCode=genericUtility.getColumnValue("site_code", dom);
							System.out.println("@narendra site_code : "+siteCode);
							if(siteCode==null || siteCode.trim().length()==0)
							{
								errCode="BLSITE1";
								errList.add(errCode);
					    		errFields.add(childNodeName.toLowerCase());
							}
							if(siteCode!=null && siteCode.trim().length()>0)
							{
								sql="select COUNT(*) from site where site_code= ? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt=rs.getInt(1);
								}
								rs.close();
								rs = null;
							    pstmt.close();
								pstmt = null;
								if(cnt==0)
								{
									errCode="VMSITE1";
									errList.add(errCode);
						    		errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									String gstnNo = "";
									sql="select ddf_get_siteregno(?,'EXC_GSTNO') AS GSTNNO from dual";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1, siteCode);
									rs=pstmt.executeQuery();
									if(rs.next())
									{
										gstnNo=rs.getString("GSTNNO");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(gstnNo == null || gstnNo.trim().length() == 0 || gstnNo.equalsIgnoreCase("null"))
									{
										errCode="VTEW017";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
						if(childNodeName.equals("tran_ser"))
						{
							tranSer=genericUtility.getColumnValue("tran_ser", dom);
							if(tranSer!=null && tranSer.trim().length()>0)
							{
								
							}else
							{
								errCode="VTTRANSER";
								errList.add(errCode);
					    		errFields.add(childNodeName.toLowerCase());
							}
						}
						if(childNodeName.equals("tran_date_fr"))
						{
							tranDateFr=genericUtility.getColumnValue("tran_date_fr", dom);
							tranDateTo=genericUtility.getColumnValue("tran_date_to", dom);
							SimpleDateFormat dateSetter=new SimpleDateFormat(genericUtility.getApplDateFormat());
							
							if(tranDateFr!=null && tranDateFr.length()>0)
							{
								if(dateSetter.parse(tranDateFr).compareTo(dateSetter.parse(tranDateTo))>0)
								{
									errCode = "VFRTODATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}else
							{
								errCode = "VTFRDTBLK ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if(childNodeName.equals("tran_date_to"))
						{
							tranDateFr=genericUtility.getColumnValue("tran_date_fr", dom);
							tranDateTo=genericUtility.getColumnValue("tran_date_to", dom);
							if(tranDateTo!=null && tranDateTo.length()>0)
							{
								
							}else
							{
								errCode="VTTODTBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
						//checking for the values inserted inside the field inv value ..initial value is min 50k
						if(childNodeName.equals("inv_value"))
						{
							invValue=genericUtility.getColumnValue("inv_value", dom);
							System.out.println("invoice value is ::"+invValue);
							
							if(invValue!=null && invValue.trim().length()>0)
							{
								int valueOfInvoice=Integer.parseInt(invValue);
								System.out.println("@narendra inv value is : "+valueOfInvoice);
								
								limit = Integer.parseInt(distCommon.getDisparams("999999", "EWAY_BILL_LIMIT", conn));
								if(limit<valueOfInvoice && flag)
								{
									errCode="VTBILLAMT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}else
							{
								errCode="VTINVALBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if(childNodeName.equals("sundry_code_fr"))
						{
							
							sundryCodeFr=genericUtility.getColumnValue("sundry_code_fr", dom);
							
							if(sundryCodeFr!=null && sundryCodeFr.trim().length()>0)
							{
								
							}else
							{
								errCode="VSUNDFRBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if(childNodeName.equals("sundry_code_to"))
						{
							sundryCodeTo=genericUtility.getColumnValue("sundry_code_to", dom);
							
							if(sundryCodeTo!=null && sundryCodeTo.trim().length()>0)
							{
								
							}else{
								errCode="VSUNDTOBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
					    if(childNodeName.equalsIgnoreCase("state"))
					    {
					    	 stateValue=genericUtility.getColumnValue("state", dom);
					    	 System.out.println("Selected value of the state is :"+stateValue);
					    	 
					    	 if(stateValue.trim().equals("A"))
					    	 {
					    		 flag=true;
					    	 }
					    	 if(stateValue.trim().equals("B"))
					    	 {
					    		 flag=false;
					    	 }
					    	 System.out.println("Value of the flag : "+flag);
					     }
					}
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
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
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
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
	}//END OF VALIDATION
	
	/**
	 * itemchanged method
	 */
	public String itemChanged(String xmlString, String xmlString1, String xmlString2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ default itemChanged called");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [EwaybillIC][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
		return valueXmlString;
	}
	
	/**
	 * itemchanged method
	 **/
	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException {
		StringBuffer valueXmlString=new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String childNodeName = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		/*ConnDriver connDriver = new ConnDriver();*/
		String columnValue="",chgUser = "",chgTerm = "",loginSiteCode = "",itemCode="",itemDescr="", fSysDate = "", commCriteria = "",code="";
		int currentFormNo =0;
		int ctr=0;
		String sql="";
		int level=0;
		Calendar currentDate = Calendar.getInstance();
		String dateNow="";
		String tranSeries="";
		int invValue=0;
		boolean flag = false;
		DistCommon distCommon = new DistCommon();
		try
		{
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat(genericUtility.getApplDateFormat());
			dateNow=simpleDateFormat.format(currentDate.getTime());
			/*conn = connDriver.getConnectDB("DriverITM");
			conn.setAutoCommit(false);
			connDriver=null;*/
			conn = getConnection();			
			System.out.println("Object context is:"+objContext);
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			chgUser = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgUser" );
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
			switch(currentFormNo)
			{
				case 1:
					 parentNodeList=dom.getElementsByTagName("Detail1");
					 parentNode=parentNodeList.item(0);
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
				    		 if(childNode.getFirstChild()!=null)
				    		 {
				    			 columnValue=childNode.getFirstChild().getNodeValue().trim();
				    		 }
				    	 }
				    	 ctr++;
				     }
				    
				     while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				     
				     System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				     
				     if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
					 {
				    	 System.out.println("Inside item default column");
				    	//getting comma separated tran series from this disparm variable
				    	tranSeries = distCommon.getDisparams("999999", "EWAY_TRAN_SER", conn);
				    	invValue = Integer.parseInt(distCommon.getDisparams("999999", "EWAY_BILL_LIMIT", conn));
						valueXmlString.append("<site_code>").append("<![CDATA["+loginSiteCode+"]]>").append("</site_code>");
						valueXmlString.append("<tran_ser>").append("<![CDATA["+tranSeries+"]]>").append("</tran_ser>");			
						valueXmlString.append("<tran_date_fr>").append("<![CDATA["+dateNow+"]]>").append("</tran_date_fr>");
						valueXmlString.append("<tran_date_to>").append("<![CDATA["+dateNow+"]]>").append("</tran_date_to>");
						valueXmlString.append("<sundry_code_fr>").append("<![CDATA[00]]>").append("</sundry_code_fr>");
						valueXmlString.append("<sundry_code_to>").append("<![CDATA[ZZ]]>").append("</sundry_code_to>");
						valueXmlString.append("<state>").append("<![CDATA[A]]>").append("</state>");
						valueXmlString.append("<inv_value>").append("<![CDATA["+invValue+"]]>").append("</inv_value>");
						
						 code = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				         sql  = "select usr_lev from users where code = ? ";
				    	 pstmt=conn.prepareStatement(sql);
				    	 pstmt.setString(1, code);
				    	 rs=pstmt.executeQuery();
				    	 if(rs.next())
				    	 {
				    		 level=rs.getInt(1);
				    	 }
				    	 rs.close();
						 rs = null;
						 pstmt.close();
						 pstmt = null;
						 //checking user level for making editiability of site code
						 if(level>1)
						 {
							valueXmlString.append("<site_code protect = \"1\">").append("<![CDATA[" + loginSiteCode +"]]>").append("</site_code>");
						 }
						 else
						 {
								valueXmlString.append("<site_code>").append("<![CDATA[" + loginSiteCode +"]]>").append("</site_code>");
						 }
					 }
				     valueXmlString.append("</Detail1>");
				     break;
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		 finally 
		 {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					if ((conn != null) && (!conn.isClosed())) {
						conn.close();
					}
				} catch (Exception e) 
				{
					System.out.println("Exception :NearExpiryIC:default_ItemChanged :==>\n"+ e.getMessage());
					throw new ITMException(e);
				}
			}
		
		return  valueXmlString.toString();
	}//END OF ItemChanged
	
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
}
