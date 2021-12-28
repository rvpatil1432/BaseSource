
/********************************************************
	Title : RecoveryPattern
	Date  : 19/01/09
	Author: Manazir Hasan

********************************************************/

package ibase.webitm.ejb.dis.adv;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.text.DecimalFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class StoreToPrc extends ProcessEJB implements StoreToPrcLocal, StoreToPrcRemote
{
	Connection sqlConn = null;
	Connection conn = null;
	String errorString = null;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	int ctr = 0,cnt = 0;
	String childNodeName = null;
	String errCode = "";
	int currentFormNo=0;
	int childNodeListLength;
	String selectQry = "";
	String insertQry = "";	
	String upDateQry = "";	
	String selectDtl1Qry = "";	
	String selectDtl2Qry = "";	
	String insertDtl1Qry = "";	
	String insertDtl2Qry = "";	
	String selectItem = "";	
	ResultSet rs = null;
	ResultSet detail1Rs = null;
	ResultSet detail2Rs = null;
	PreparedStatement psmt = null;
	PreparedStatement dtl1Psmt = null;
	PreparedStatement dtl2Psmt = null;
	Statement stmt = null;
	Statement dtl2Stmt = null;
	ResultSet selectRs = null;
	ResultSet dtlRs = null;
	boolean recFound = false;	
	PreparedStatement oraPsmt = null;
	Statement st = null;
	String exprNo  = null;
	//String confirmed = null;
	String conformation = "";
	SimpleDateFormat simpleDateFrormat  = null;
	int upd = 0;
	String procCode = "",procDateStr = "",template = "",mcCode = "",procesName = "",mcName = "",stageNo = "",procDate = "";
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Create Method Called....");
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}
		*/
	
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
		throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		String retStr = "";
		try
		{				
			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :SoHoldRefPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{		
		String retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "" ,sql1 = "",errString = "";
        String retString = "" ,errCode = "",tranId="";
		String loginSiteCode="",empCode="",chgUser="",chgTerm="",loginCode="";
		String 	policyNo="",insCertNo="",agent_code="",invoiceId="";
		int count =0,cnt =0;
		String curr_code="";
	    double exch_rate=0,netAmt=0;	
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		int no = 0;		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null; 
		ConnDriver connDriver = new ConnDriver();
		String specReason = "";
		String status = "";
		String saleOrder = "";
		String  conformation = "";
		String locCode ="";
		
		try
		{
			System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 		
			connDriver=null;			
			parentNodeList = detailDom.getElementsByTagName("Detail1");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("parentNodeListLength:::::::::"+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength::: "+ childNodeListLength+"\n");			
				
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{					
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					
					if (childNodeName.equals("loc_code"))
					{
						if(childNode.getFirstChild()!=null)
						{
							locCode = childNode.getFirstChild().getNodeValue();
							//System.out.println("locCode......"+locCode);
						}					
						
					}									
					if (childNodeName.equals("tran_id"))
					{
						if(childNode.getFirstChild()!=null)
						{
							tranId = childNode.getFirstChild().getNodeValue();					
													
						}					
						
					}
					if(childNodeName.equalsIgnoreCase("confirmed"))
					{
						if(childNode.getFirstChild()!=null)
						{
							conformation = childNode.getFirstChild().getNodeValue();						
							if(conformation != null && conformation.trim().length() > 0 && conformation.equalsIgnoreCase("Y"))
							{								
								errCode = "VMNTCONCOD";
								errString =itmDBAccess.getErrorString("confirmed",errCode,chgUser,"",conn);
								return (errString);
															
							}
						}
					}
					
				}//inner for loop]	lotNoFromStr	+
				if(locCode == null || locCode.trim().length() ==0 )	
				{
					errCode = "VMLOCCODEN";
					errString =itmDBAccess.getErrorString("LOC_CODE",errCode,chgUser,"",conn);
					return (errString);
				
				}
				if( locCode != null && locCode.trim().length() >0  && conformation.equalsIgnoreCase("N")  )
				{
					
					sql = "update distord_rcpdet  set  loc_code = ?  where tran_id = '"+tranId+"'  " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,locCode);						
					int i = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					if(i >0 )
					{
						conn.commit();
						errCode = "SUCCPRCLOC";
						errString =itmDBAccess.getErrorString("SUCCESS",errCode,chgUser,"",conn);
						
						break ;	
					}						
				}				
					
			} // outer for loop		
			// end of code 
	
		}  // end try 
	   	catch(Exception e)
		{
			
			 System.out.println("Exception in SoHoldRefPrc..."+e.getMessage());
			 e.printStackTrace();
			  errString = e.getMessage();			
		}
		System.out.println("returning from  SoHoldRefPrc   "+errString);
	    return (errString);
	} //end process
	
	
	// fuctions for transfering data
	 
}