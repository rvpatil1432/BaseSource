
/********************************************************
	Title : RecoveryPatternEJB
	Date  : 19/01/09
	Author: Manazir Hasan

 ********************************************************/

package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
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


//public class ReceiptExRefPrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class ReceiptExRefPrc extends ProcessEJB implements ReceiptExRefPrcLocal, ReceiptExRefPrcRemote
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
	String confirmed = null;
	SimpleDateFormat simpleDateFrormat  = null;
	int upd = 0;
	String procCode = "",procDateStr = "",template = "",mcCode = "",procesName = "",mcName = "",stageNo = "",procDate = "";
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	/*
	public void ejbCreate() throws RemoteException, CreateException 
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
	}*/


	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
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
			System.out.println("Exception :ImportDataPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
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
		Timestamp tranDate =null;		
		String deliveryNoOld ="";        
		String deliveryNoNew  =""; 
		String challanDateOldStr = "";
		String challanDateNewStr="";
		Timestamp challanDateOld  =null;     
		Timestamp challanDateNew  =null;    
		String invoiceNoOld ="";    
		String invoiceNoNew ="";  
		String invoiceDateOldStr = "";
		String invoiceDateNewStr = "";
		Timestamp invoiceDateOld =null;  
		Timestamp invoiceDateNew =null; 
		String exciseRefOld =""; 
		String exciseRefNew ="";
		String exciseDateOldStr = "";
		String exciseDateNewStr = "";
		Timestamp exciseDateOld = null;
		Timestamp exciseDateNew =null;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		int no = 0;		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null; 
		ConnDriver connDriver = new ConnDriver();
		//addedd  by monika 19 sept 2019
		String chpartner=null;
		//end
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
			conn.setAutoCommit(false);
			connDriver=null;
			//tranId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"tran_id");
			tranId = genericUtility.getColumnValue("tran_id",headerDom);
			System.out.println("tranId>>>>>>>>>>>>>"+tranId);
			//commented by monika on 19 sept 2019
			//	sql = "select confirmed  from porcp where tran_id = '"+tranId+"' " ;
			//addedd by monika on 19 sept 2019
			sql = "select confirmed,channel_partner from porcp where tran_id = '"+tranId+"' " ;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed =  rs.getString(1) == null ? "N" : rs.getString(1);
				chpartner= checkNull( rs.getString("channel_partner"));	//addedd by monika on 19 sept 2019
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs= null;
			//ADDED BY MONIKA S -0N 22 AUGUST 2019-TO SHOW MESSAGE TO USER WETHER RECEIPT IS CONFIRMED OR NOT.
			if("N".equalsIgnoreCase(confirmed))
			{
				errCode = "RCPNOTCONF";
				errString =itmDBAccessEJB.getErrorString("updated",errCode,chgUser,"",conn);						
				return (errString);//added by monika 23 sept 2019
			}//END

			//ADDED BY MONIKA -ON 20 SEPT TO CHECK channel partner
			if("Y".equalsIgnoreCase(confirmed))
			{
				//System.out.println("receipt status:"+confirmed);
				if("Y".equalsIgnoreCase(chpartner))
				{
					errCode = "VINVCHNRCP";
					errString =itmDBAccessEJB.getErrorString("updated",errCode,chgUser,"",conn);						
					return (errString);//added by monika 23 sept 2019
				}
			}
			//end
			// code to   getn coloumn value  detail 
			//-------------detail 1--------------------------
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
					if (childNodeName.equals("delivery_no_o"))
					{
						if(childNode.getFirstChild()!=null)
						{
							deliveryNoOld = childNode.getFirstChild().getNodeValue();
							System.out.println("deliveryNoOld code ......"+deliveryNoOld);
						}						

					}
					if (childNodeName.equals("delivery_no"))
					{
						if(childNode.getFirstChild()!=null)
						{
							deliveryNoNew = childNode.getFirstChild().getNodeValue();
							System.out.println("deliveryNoNew code ......"+deliveryNoNew);
						}						

					}
					if (childNodeName.equals("challan_date_o"))
					{
						if(childNode.getFirstChild()!=null)
						{
							challanDateOldStr = childNode.getFirstChild().getNodeValue();
							System.out.println("challanDateOldStr......"+challanDateOldStr);

						}						

					}
					if (childNodeName.equals("challan_date"))
					{
						if(childNode.getFirstChild()!=null)
						{
							challanDateNewStr = childNode.getFirstChild().getNodeValue();
							System.out.println("challanDateNewStr......"+challanDateNewStr);
							challanDateNewStr = genericUtility.getValidDateString(challanDateNewStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							challanDateNew = java.sql.Timestamp.valueOf(challanDateNewStr + " 00:00:00.00");
							System.out.println("challanDateNew......"+challanDateNew);
						}						

					}
					if (childNodeName.equals("invoice_no_o"))
					{
						if(childNode.getFirstChild()!=null)
						{
							invoiceNoOld = childNode.getFirstChild().getNodeValue();
							System.out.println("invoiceNoOld......"+invoiceNoOld);
						}						

					}
					if (childNodeName.equals("invoice_no"))
					{
						if(childNode.getFirstChild()!=null)
						{
							invoiceNoNew = childNode.getFirstChild().getNodeValue();
							System.out.println("invoiceNoNew......"+invoiceNoNew);
						}						

					}
					if (childNodeName.equals("invoice_date_o"))
					{
						if(childNode.getFirstChild()!=null)
						{
							invoiceDateOldStr = childNode.getFirstChild().getNodeValue();
							System.out.println("invoiceDateOld......"+invoiceDateOldStr);
						}						

					}
					if (childNodeName.equals("invoice_date"))
					{
						if(childNode.getFirstChild()!=null)
						{
							invoiceDateNewStr = childNode.getFirstChild().getNodeValue();
							System.out.println("invoiceDateNewStr......"+invoiceDateNewStr);
							invoiceDateNewStr = genericUtility.getValidDateString(invoiceDateNewStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							invoiceDateNew = java.sql.Timestamp.valueOf(invoiceDateNewStr + " 00:00:00.00");
							System.out.println("invoiceDateNew......"+invoiceDateNew);
						}						
					}
					if (childNodeName.equals("excise_ref_o"))
					{
						if(childNode.getFirstChild()!=null)
						{
							exciseRefOld = childNode.getFirstChild().getNodeValue();
							System.out.println("exciseRefOld......"+exciseRefOld);
						}						
					}
					if (childNodeName.equals("excise_ref"))
					{
						if(childNode.getFirstChild()!=null)
						{
							exciseRefNew = childNode.getFirstChild().getNodeValue();
							System.out.println("exciseRefNew......"+exciseRefNew);
						}						
					}
					if (childNodeName.equals("excise_date_o"))
					{
						if(childNode.getFirstChild()!=null)
						{
							exciseDateOldStr = childNode.getFirstChild().getNodeValue();
							System.out.println("exciseDateOld ......"+exciseDateOldStr );
						}						
					}
					if (childNodeName.equals("excise_date"))
					{
						if(childNode.getFirstChild()!=null)
						{
							exciseDateNewStr = childNode.getFirstChild().getNodeValue();
							System.out.println("exciseDateNewStr ......"+exciseDateNewStr );
							exciseDateNewStr = genericUtility.getValidDateString(exciseDateNewStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							exciseDateNew = java.sql.Timestamp.valueOf(exciseDateNewStr + " 00:00:00.00");
							System.out.println("exciseDateNew ......"+exciseDateNew );
						}						
					}				

				}//inner for loop]	lotNoFromStr
			}			

			// end of code 
			//	if(confirmed.equalsIgnoreCase("Y"))//COMMENTED BY MONIKA 22 AUGUST 2019
			if("Y".equalsIgnoreCase(confirmed))
			{
				// ??? how to get value  coloumn value 
				sql = "update porcp set dc_no = ? , dc_date= ?,invoice_no = ?, invoice_date= ?  ,excise_ref = ?, excise_ref_date = ?  where tran_id = '"+tranId+"' " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,deliveryNoNew);
				pstmt.setTimestamp(2,challanDateNew);
				pstmt.setString(3,invoiceNoNew);
				pstmt.setTimestamp(4,invoiceDateNew);
				pstmt.setString(5,exciseRefNew);
				pstmt.setTimestamp(6,exciseDateNew);				
				int i = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				if(i >0 )
				{
					errCode = "SUCCESSPRC";
					errString =itmDBAccessEJB.getErrorString("updated",errCode,chgUser,"",conn);						
					conn.commit();					
				}	
			}
		}
		catch(Exception e)
		{

			System.out.println("Exception in ImportDataPrcEJB..."+e.getMessage());
			e.printStackTrace();
			/*errString = e.getMessage();*/ ///Commented BY Mukesh Chauhan on 06/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
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

		System.out.println("returning from  ImportDataPrcEJB   "+errString);
		return (errString);
	} //end process
	//added by monika 19 sept 2019
	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}//end

	// fuctions for transfering data

}