
/********************************************************
	Title : QuotationApprvPrcEJB
	Date  : 23/12/08
	Author: pankaj singh

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
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3


//public class QuotationApprvPrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class QuotationApprvPrc extends ProcessEJB implements QuotationApprvPrcLocal, QuotationApprvPrcRemote
{
	Connection conn = null;
	String errorString = null;
	NodeList parentNodeList = null;
	NodeList enqNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node enqNode = null;
	Node childNode = null;
	int ctr = 0,cnt = 0;
	String childNodeName = null;
	String errCode = "";
	int currentFormNo=0;
	int childNodeListLength;
	String selectQry = "";
	String selectItem = "";	
	ResultSet rs = null;
	PreparedStatement psmt = null;
	Statement stmt = null;
	Statement dtl2Stmt = null;
	ConnDriver connDriver = null;
	ResultSet dtlRs = null;
	String working = "";
	StringBuffer xmlString = null;
	int date = 0,month= 0,year= 0,hour = 0,min = 0,sec = 0;
	Date d = null;	
	//BasePreparedStatement oraPsmt = null;
	Statement st = null;
	SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd");
	int upd = 0;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
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
			System.out.println("Exception :WokCalendarPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{		
		String retStr = "";
		PreparedStatement pstmt = null;
		PreparedStatement UpdatePstmt = null;
		PreparedStatement UpdateDtPstmt = null;
		PreparedStatement AprvPstmt = null;
		ResultSet rs = null;
		String sql = "" ,sql1 = "",errString = "";
        String retString = "" ,errCode = "",tranId="";
		String loginSiteCode="",empCode="",chgUser="",chgTerm="",loginCode="",termid = "";
		String 	policyNo="",insCertNo="",agent_code="",invoiceId="";
		int count =0,cnt =0;
		String curr_code="",dayNo = "",shiftOfDay = "";
	    double exch_rate=0,netAmt=0;
	    Timestamp tranDate =null;
		String siteCode = "",wokCntr = "",datefrom = "",dateto = "",holTblno = "",insertQry = "";
		connDriver = new ConnDriver();
		String userId = "";
		Map shift = null;
		shift = new HashMap();
		Calendar cal = null;
		java.sql.Timestamp ToDate =null,FromDate = null;
		String suppCode = "",enqNo = "",itemCode = "",status = "",errorString = "";
		String [] itemCodeAr = null;
		try
		{
			System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			System.out.println("empCode..............."+empCode);
			System.out.println("Login Site Code............."+loginSiteCode);
			System.out.println("userId............."+chgUser);
			System.out.println("termId............."+chgTerm);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			Timestamp currDate = null;
			String currAppdate = null,quotNo = "",quotNoDup = "";
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			System.out.println("\n today date----" + currDate);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			System.out.println("\n today currDate in apl----" + currAppdate);
			currAppdate = genericUtility.getValidDateString(currAppdate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
			System.out.println("\n today currDate in DB----" + currAppdate);
			currDate = Timestamp.valueOf(currAppdate + " 00:00:00.00");
			System.out.println("\n today currDate in timestamp----" + currDate);
			quotNo  = genericUtility.getColumnValue("quot_no",headerDom);	
			System.out.println("Values from dom \n quotNo " + quotNo );
			sql = "SELECT PQUOT_HDR.SUPP_CODE,"   
					 + " PQUOT_DET.ENQ_NO,"   
					 + " PQUOT_DET.ITEM_CODE,"   
					 + " PQUOT_DET.STATUS"   
					 + " FROM PQUOT_HDR,PQUOT_DET"   
					 + " WHERE ( PQUOT_HDR.QUOT_NO = PQUOT_DET.QUOT_NO ) and "  
					 + " ( ( pquot_det.quot_no = '" + quotNo +"' ) ) ";
			System.out.println("select qry from ITEM_CODE.." + sql);
			pstmt= conn.prepareStatement(sql);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
			   suppCode = rs.getString(1);
			   enqNo = rs.getString(2);
			   itemCode = rs.getString(3);
			   status = rs.getString(4);
			}							
			pstmt.close();
			rs.close();
			pstmt = null;
			//sql = "select	pquot_det.quot_no, pquot_det.item_code "
			sql = "select	pquot_hdr.quot_no  "
					  + " from	pquot_hdr, pquot_det"
					  + " where	pquot_hdr.quot_no =  pquot_det.quot_no and"
								+ " pquot_det.status     = 'A' and"
								+ " pquot_det.enq_no     = '" + enqNo + "'"
								+ " and pquot_det.item_code  = '" + itemCode + "'"      
								+ " and pquot_hdr.supp_code <> '" + suppCode + "'"
								+ " and pquot_hdr.quot_no   <> '" + quotNo + "'";
			System.out.println("select qry from count.." + sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery(); 
			if(rs.next())
			{
			  quotNoDup  = rs.getString(1);
			}							
			pstmt.close();
			rs.close();
			pstmt = null;
			rs = null;

			
			if (quotNoDup != null && quotNoDup.trim().length() > 0)
			{
				errString = itmDBAccessEJB.getErrorString("","QAPRDUP",chgUser,"",conn);
				System.out.println("errString length :- " + errString.trim().length());
				System.out.println("errString before :- " + errString);
				System.out.println("index has :- " + errString.indexOf("has"));
				System.out.println("index enquiry :- " + errString.indexOf("enquiry "));
				System.out.println("index description :- " + errString.indexOf("</description>"));
				errString = errString.substring(0,errString.indexOf("has") ) + quotNoDup + errString.substring(errString.indexOf("has"),errString.indexOf("</description>") )  + enqNo + errString.substring(errString.indexOf("</description>") );
				System.out.println("errString after:- " + errString);
			}
			
			else
			{
				sql = "select status from pquot_hdr where quot_no = '" + quotNo + "'";
				System.out.println("select qry from status.." + sql);
				pstmt= conn.prepareStatement(sql);
				rs = pstmt.executeQuery(); 
				if(rs.next())
				{
				   status = rs.getString(1);
				}							
				pstmt.close();
				rs.close();
				pstmt = null;
				if(status == null || status.trim().length() == 0 || !status.equalsIgnoreCase("A"))
				{
					parentNodeList =  detailDom.getElementsByTagName("item_code");
					enqNodeList =  detailDom.getElementsByTagName("enq_no");
					int Length = parentNodeList.getLength();
					System.out.println("No of itemCode " + Length);
					for( int custIdx = 0; custIdx < Length ; custIdx++ )
					{
						parentNode = ( parentNodeList.item(  custIdx ) ).getFirstChild();
						itemCode = parentNode.getNodeValue();
						enqNode = ( enqNodeList.item(  custIdx ) ).getFirstChild();
						enqNo = enqNode.getNodeValue();
						System.out.println("itemcode in MSALAM deatil dom................. "  + itemCode );
						System.out.println("enqNo in MSALAM deatil dom................. "  + enqNo );
						sql = "select status from pquot_det where quot_no = '" + quotNo + "'"
								+ " and item_code = '" + itemCode + "'";
						System.out.println("select qry from status.." + sql);
						pstmt= conn.prepareStatement(sql);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
						   status = rs.getString(1);
						}							
						pstmt.close();
						rs.close();
						pstmt = null;
						System.out.println("status in detail..." + status);
						if(status == null || status.trim().length() == 0 || !status.equalsIgnoreCase("A"))
						{
							sql = " update enq_det set status = 'C',status_date = ?" 
									+ " where enq_no = '" + enqNo + "'";
									//+ " and item_code = '" + itemCode + "'";
							System.out.println("\n sql for update----" + sql);
							UpdatePstmt = conn.prepareStatement(sql);
							UpdatePstmt.setTimestamp(1, currDate);
							UpdatePstmt.executeUpdate();
							sql = " update pquot_det set status = 'A',status_date = ?"
										+ " where quot_no = '" + quotNo + "'"
										+ " and item_code = '" + itemCode + "'";
							System.out.println("\n sql for update----" + sql);
							UpdateDtPstmt = conn.prepareStatement(sql);
							UpdateDtPstmt.setTimestamp(1, currDate);
							UpdateDtPstmt.executeUpdate();
						}//end if status detail
						
					}//end for detail
					sql = " update pquot_hdr set status = 'A',status_date = ?,"
								+ " chg_date = ?,"
									+ " chg_user = '" + chgUser + "'"
									+ " ,chg_term = '" + chgTerm + "'"
								+ " where quot_no = '" + quotNo + "'";
					System.out.println("\n sql for update----" + sql);
					AprvPstmt = conn.prepareStatement(sql);
					AprvPstmt.setTimestamp(1, currDate);
					AprvPstmt.setTimestamp(2, currDate);
					AprvPstmt.executeUpdate();
					errString = itmDBAccessEJB.getErrorString("","QAPRSUCC",chgUser,"",conn); 
				}//end if(!status.equalIgnoreCase("A"))
				else 
				{
					errString = itmDBAccessEJB.getErrorString("","QAPRDUPS",chgUser,"",conn);
				}
			}
				
		}
	   	catch(Exception e)
		{
			   System.out.println("Exception in QuotationApprvPrcEJB..."+e.getMessage());
			   e.printStackTrace();
			   /*errorString = e.getMessage();*///Commented By Mukesh Chauhan on 06/08/19
			   throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		
		try
		{
			if(errorString == null || errorString.trim().length() == 0)
			{
				conn.commit();
				System.out.println("Connection commited");
				System.out.println("errString before :--- " + errString);
				if(errString != null && errString.trim().length() > 0 )
				{
					errString = errString;
				}
				System.out.println("errString after :--- " + errString);
			}
			else
			{
				conn.rollback();
				System.out.println("Conection rolbacked for errstring.....");
				errString = "";
			}	
		}
		catch (Exception ke )
		{
			ke.printStackTrace();
			throw new ITMException(ke); //Added By Mukesh Chauhan on 06/08/19
		}
		finally
		{
			try
			{
				if(rs != null)rs.close();
				rs = null;
				if(pstmt != null)pstmt.close();
				pstmt = null;
				if(UpdatePstmt != null) UpdatePstmt.close();
				UpdatePstmt = null;
				if(AprvPstmt != null) AprvPstmt.close();
				AprvPstmt = null;
				if(UpdateDtPstmt != null) UpdateDtPstmt.close();
				UpdateDtPstmt = null;
				if(conn != null)conn.close();
				conn = null;	
			}
			catch(SQLException sqle)
			{
				sqle.printStackTrace();
			}
		}
		System.out.println("returning from  QuotationApprvPrcEJB   "+errString);
		return errString;
	} //end process
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{	
		System.out.println("Xform :getData() function called");
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;		

		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 				
			}

			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :Xform :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			/*rtrStr = e.getMessage();*/
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errString=null;		
		String errCode = "";
		String getDataSql= "" ;
		String sql= "",sql1 ="" ;
		String resultString = "";
		ResultSet rs = null,rs1 = null;
		PreparedStatement pstmt = null,pstmt1 = null;
		Statement st = null;
		//StringBuffer retTabSepStrBuff = new StringBuffer();	
		StringBuffer retTabSepStrBuff = new StringBuffer("<?xml version = \"1.0\"?>");
		retTabSepStrBuff.append("<DocumentRoot>");
		retTabSepStrBuff.append("<description>").append("Datawindow Root").append("</description>");
		retTabSepStrBuff.append("<group0>");
		retTabSepStrBuff.append("<description>").append("Group0 description").append("</description>");
		retTabSepStrBuff.append("<Header0>");

		//String siteCode=null;
		String saleOrderFrom ="",saleOrderTo = "",sOrderDateFrom = "",sOrderDateTo ="";
		String lineNo = "",itemCode = "",enqNo = "",status = "",quotNo = "";
		ConnDriver connDriver = new ConnDriver();
		
			
			quotNo  = genericUtility.getColumnValue("quot_no",headerDom);	
			System.out.println("quotNo from header..............."+quotNo);
			
			try
			{
				ConnDriver conndriver = new ConnDriver();
				System.out.println("oracle driver found:-" + conndriver);
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				System.out.println("U r connected to oracle :-" + conn );
				connDriver = null;
				quotNo  = genericUtility.getColumnValue("quot_no",headerDom);	
				sql = " select quot_no,line_no,item_code,enq_no,status"
							+ " from pquot_det "
							+ " where quot_no = '" + quotNo + "'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			System.out.println("sql for entries:- " + sql );
			while ( rs.next())
			{
				quotNo = rs.getString("quot_no");
				lineNo = rs.getString("line_no");
				itemCode = rs.getString("item_code");
				enqNo = rs.getString("enq_no");
				status = rs.getString("status");
				//procDate = genericUtility.getValidDateString( procDateStr,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
				//retTabSepStrBuff.append(quotNo).append("\t");		
				//retTabSepStrBuff.append(lineNo).append("\t");		
				//retTabSepStrBuff.append(itemCode).append("\t");
				//retTabSepStrBuff.append(enqNo).append("\t");
				//retTabSepStrBuff.append(status).append("\t");	
				//retTabSepStrBuff.append("\n");
				
				retTabSepStrBuff.append("<Detail2>");
				retTabSepStrBuff.append("<quot_no>").append("<![CDATA[" + rs.getString("quot_no") + "]]>").append("</quot_no>");
				retTabSepStrBuff.append("<line_no>").append("<![CDATA[" + rs.getString("line_no") + "]]>").append("</line_no>");
				retTabSepStrBuff.append("<item_code>").append("<![CDATA[" + rs.getString("item_code") + "]]>").append("</item_code>");
				retTabSepStrBuff.append("<enq_no>").append("<![CDATA[" + rs.getString("enq_no") + "]]>").append("</enq_no>");
				retTabSepStrBuff.append("<status>").append("<![CDATA[" + rs.getString("status") + "]]>").append("</status>");
				
				retTabSepStrBuff.append("</Detail2>");
			}
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			retTabSepStrBuff.append("</Header0>");
			retTabSepStrBuff.append("</group0>");
			retTabSepStrBuff.append("</DocumentRoot>");

			resultString = retTabSepStrBuff.toString();
			System.out.println("ResultString....." + resultString);
		}
		catch(Exception e)
      {	
         e.printStackTrace();
         System.out.println("Exception ::" + e.getMessage());
         throw new ITMException(e);
      }
		finally
		{
			try
			{
				retTabSepStrBuff = null;
				if(conn != null)
				{					
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					conn.close();
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return resultString;	
	}//END OF GETDATA(2)
}//end class