package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.Document;

@javax.ejb.Stateless
public class SalePersonTransComplete extends ValidatorEJB implements  SalePersonTransCompleteLocal , SalePersonTransCompleteRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public String transComplete (String xmlString, String xmlString1, String xmlString2, String objContext, String winName, String xtraParams,Connection conn) throws RemoteException ,ITMException
	{
		System.out.println("sachin ss xmlString:>>:["+xmlString+"]::xmlString1:>>:["+xmlString1+"]:::xmlString2:>>:["+xmlString2+"]::objContext::["+objContext+"]::winName:::["+winName+"]::conn::["+conn+"]");
		String str=""; 
		
		return str;
	}*/
	//Added by sachin satre on [12-OCT-17] for request id [W17ASUN003] [START]
	public String transComplete (String xmlString, String xmlString1, String xmlString2, String objContext, String winName, String xtraParams,Connection conn) throws RemoteException ,ITMException
	{
		System.out.println("sachin ss::ss:xmlString2:>>:["+xmlString2+"]::objContext::["+objContext+"]::winName:::["+winName+"]::conn::["+conn+"]");
		String str=""; 
		String mailHeader = "";
		String formatCode = "";
		boolean isMailSend = false;
		String infoType = "ITM";
		String objName ="slpers";
		String salesPers ="";
		String stpStatus ="";
		Document dom2=null;
		
		try
		{
			EMail em = new EMail();
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			formatCode = "STPDARBLCK";
			salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom2));
			stpStatus = checkNull(genericUtility.getColumnValue("stp_status", dom2));
			System.out.println("salesPers:::["+salesPers+"]::stpStatus:::["+stpStatus+"]");
			if(stpStatus != null && !stpStatus.equalsIgnoreCase("") &&  stpStatus.equalsIgnoreCase("2")  )
			{	
				mailHeader = 	"<ROOT>"+
				"<TRANS_INFO>"+
				"<OBJ_NAME>"+objName+"</OBJ_NAME>"+
				"<REF_SER>"+"CUSSTR"+"</REF_SER>"+
				"<LINE_NO>"+""+"</LINE_NO>"+
				"<XSL_FILE_NAME></XSL_FILE_NAME>"+
				"</TRANS_INFO>"+
				"<MAIL>"+
				"<SUBJECT></SUBJECT>"+
				"<ENTITY_CODE>"+salesPers.trim()+"</ENTITY_CODE>"+
				"<BODY_TEXT></BODY_TEXT>"+
				"<TO_ADD></TO_ADD>"+
				"<CC_ADD></CC_ADD>"+
				"<FORMAT_CODE>"+formatCode+"</FORMAT_CODE>"+
				"<ATTACHMENT><BODY></BODY><LOCATION></LOCATION></ATTACHMENT>"+
				"</MAIL>"+
				"<XML_DATA>"+xmlString2+"</XML_DATA>"+
				"</ROOT>";
				System.out.println("DAR blocked in sales per master mailHeader ------- "+mailHeader);
				//String returnstrFromSendMailMethod = em.sendMail(mailHeader, infoType);
				String returnstrFromSendMailMethod = em.sendMail(mailHeader, infoType,conn);//Change By bhagyashri T for send mail[11-05-21][W20LSUN008]
				System.out.println("@@@@ mail function called successfully : "+returnstrFromSendMailMethod);
				
				//isMailSend = sendMailStatus(mailHeader, infoType);
				//System.out.println("[CustSeriesUploadPos::process()] mail delivered successful" + isMailSend);
			}	
			
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		} 
		
		return str;
	}
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		else
		{
			input =input.trim();
		}
		return input;
	}
	//Added by sachin satre on [12-OCT-17] for request id [W17ASUN003] [END]
}
