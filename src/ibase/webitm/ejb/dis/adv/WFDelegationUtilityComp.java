
/**
 * Request ID: W14KSUN002
 * Purpose: generate E-mail link for the sale order.
 * Date: 23-Feb-15
 * Created By : Rahul Barve
 **/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
@Stateless 
public  class WFDelegationUtilityComp extends ValidatorEJB implements WFDelegationUtilityCompLocal,WFDelegationUtilityCompRemote
{
	
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	public void generateEmailLink(String xmlString ,String xtraParam, String lineNo)
	{
		ConnDriver connDriver 	= null;
		Connection conn 		= null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selectSql = null;
	
		StringBuffer valueXmlString=null;
		
		Document detailDom = null;
		Document currDom = null;
		String xmlData="";
		String infoType="ITM";
		String xslFileName="";
		String formateCode="";
		String objName="";
		String emailType="link";
		String mailXmlString = "";
		String refSer = "";
		String refId = "";
		//String lineNo = "";
		String empCodeTo = "";
		
		
		try
		{
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			System.out.println("Connection created.....");
			
			valueXmlString = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?><ROOT><TRANS_INFO>");
		
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				currDom = genericUtility.parseString(xmlString); 
				 
			}
			
			refSer = checkNull(genericUtility.getColumnValue( "ref_series" , currDom ));
			refId = checkNull(genericUtility.getColumnValue( "ref_id" , currDom ));
			empCodeTo = checkNull(genericUtility.getColumnValue( "emp_code__to" , currDom));
			 
		    selectSql = " SELECT OST.TRANS_INFO_XML.getClobval()  , MSG_FRM_FNAME  FROM OBJ_SIGN_TRANS OST WHERE REF_SER = ? AND REF_ID = ? AND LINE_NO = ?";
			pstmt = conn.prepareStatement(selectSql);
			pstmt.setString(1, checkNull(refSer));
			pstmt.setString(2, checkNull(refId));
			pstmt.setString(3, checkNull(lineNo));
		    rs = pstmt.executeQuery();
		    if (rs.next())
		    {
		    	xmlData = rs.getString(1)== null ? "" :rs.getString(1); 
		    	xslFileName =  rs.getString(2)== null ? "" :rs.getString(2); 
		    }
		    if (xmlData != null && xmlData.trim().length() > 0)
		    {
		    	detailDom = genericUtility.parseString(xmlData);
		    }
		 
		    objName = getObjNameFromDom(detailDom,"objName",1);
		    
			if(refSer.equalsIgnoreCase("S-ORD"))
			{
				formateCode = "S-ORD_DELGN";
			}
			
		 	valueXmlString.append("<OBJ_NAME>").append("<![CDATA[" + objName + "]]>").append("</OBJ_NAME>");
			valueXmlString.append("<REF_SER>").append("<![CDATA[" + refSer + "]]>").append("</REF_SER>");
			valueXmlString.append("<REF_ID>").append("<![CDATA[" + refId + "]]>").append("</REF_ID>");
			valueXmlString.append("<LINE_NO>").append("<![CDATA[" + lineNo + "]]>").append("</LINE_NO>");
			valueXmlString.append("<XSL_FILE_NAME>").append("<![CDATA[" + xslFileName + "]]>").append("</XSL_FILE_NAME>");
			valueXmlString.append("</TRANS_INFO>");
			
			valueXmlString.append("<MAIL>");
			valueXmlString.append("<EMAIL_TYPE>").append("<![CDATA[" + emailType + "]]>").append("</EMAIL_TYPE>");
			valueXmlString.append("<ENTITY_CODE>").append("<![CDATA[" + empCodeTo + "]]>").append("</ENTITY_CODE>");
			valueXmlString.append("<ENTITY_TYPE>").append("E").append("</ENTITY_TYPE>");
			valueXmlString.append("<FORMAT_CODE>").append(formateCode).append("</FORMAT_CODE>");
			valueXmlString.append("<ATTACHMENT><BODY></BODY><LOCATION></LOCATION></ATTACHMENT>");
			valueXmlString.append("</MAIL>");
			
			valueXmlString.append("<XML_DATA>").append(xmlData).append("</XML_DATA>");
			valueXmlString.append("</ROOT>");
			
			mailXmlString = valueXmlString.toString();
			System.out.println("mailXmlString ==== >"+mailXmlString);
			
			EMail em = new EMail();
		    String mail_status = em.sendMail(mailXmlString, infoType);
			System.out.println("mail_status=="+mail_status);
		
		}
		catch (Exception e)
		{
		    e.printStackTrace();
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
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e){}
		}
		
		
	}
	

	
	private String getObjNameFromDom( Document dom, String attribute , int currFormNo) throws RemoteException,ITMException
	{
		String objName = "";
		try
		{
			NodeList detailList = null;
			Node currDetail = null,reqDetail = null;			
			int	detailListLength = 0;

			detailList = dom.getElementsByTagName("Detail" + currFormNo);
			detailListLength = detailList.getLength();
			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				currDetail = detailList.item(ctr);
				objName = currDetail.getAttributes().getNamedItem(attribute).getNodeValue();
						
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return objName;
	}
	
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}

}
