
/********************************************************
	Title : QuotationCancelApprvPrcEJB
	Date  : 28/02/09
	Author: Manazir Hasan Khan

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

//public class QuotationCancelApprvPrcEJB extends ProcessEJB implements SessionBean

@Stateless // added for ejb3
public class QuotationCancelApprvPrc extends ProcessEJB implements QuotationCancelApprvPrcLocal, QuotationCancelApprvPrcRemote
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
	String returnString = "";
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
		String lineNo="";
		int parentNodeListLength = 0;		
		int childNodeListLength = 0;
		int no = 0;
		int cntstatusUn = 0;
		int noOfrowSel = 0;
		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null; 
		try
		{
			System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
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
			sql = " select count(1) from pquot_det where quot_no = '" + quotNo + "' and status ='A' " ;
			System.out.println("sql....."+sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt=null;
			sql = " select count(1) from pquot_hdr where quot_no = '" + quotNo.trim() + "' and status ='U' " ;
			System.out.println("sql....."+sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				cntstatusUn = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt=null;
			if(cntstatusUn >0)
			{
				returnString = itmDBAccessEJB.getErrorString("","QDAPRDUP",userId,"",conn);
				System.out.println("returnString before:- " + returnString + "\n" + returnString.indexOf("AlreadyUnApproved"));
				returnString = returnString.substring(0,returnString.indexOf("UnApproved") ) + " for quot_no = " + quotNo + returnString.substring(returnString.indexOf("</description>") );
				System.out.println("new returnString changed....."+returnString);	
			}
			else
			{
					parentNodeList = detailDom.getElementsByTagName("Detail2");
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
							System.out.println("childNodeNamechildNodeName ["+childNodeName+"]");
							if (childNodeName.equals("line_no"))
							{
								if(childNode.getFirstChild()!=null)
								{
									lineNo = childNode.getFirstChild().getNodeValue();
									lineNo = lineNo == null ?"" :lineNo;								
									if(lineNo!=null && lineNo.trim().length()>0)
									{
										noOfrowSel++;										
										sql = " update pquot_det "
											+ " set status = 'U',status_date = ?"										
											+ " where quot_no = '" + quotNo + "' and "
											+"  line_no = '"+lineNo+"' " ;										
											pstmt = conn.prepareStatement(sql);
											pstmt.setTimestamp(1, getCurrdateAppFormat() );							
											count = pstmt.executeUpdate();	
									}
								}
								
							}							

						}//inner for loop]	lotNoFromStr				

					}// OUT FOR LOOP
				// end of code				
				System.out.println("noOfrowSel....."+noOfrowSel);
				System.out.println("cnt....."+cnt);
				if( noOfrowSel == cnt && count >0 )
				{
					sql = " update pquot_hdr "
								+ " set status = 'U',status_date = ?,"
								+ " chg_date = ?,"
								+ " chg_user = '" + chgUser + "',"
								+ " chg_term = '" + chgTerm + "'"
								+ " where quot_no = '" + quotNo + "'"  ;
					System.out.println("sql....."+sql);
					System.out.println("setting date....."+getCurrdateAppFormat());
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, getCurrdateAppFormat() );
					pstmt.setTimestamp(2, getCurrdateAppFormat() );
					pstmt.executeUpdate();
					returnString = itmDBAccessEJB.getErrorString("","QDAPRSUC",userId,"",conn);
					System.out.println("returnString before:- " + returnString + "\n" + returnString.indexOf("succesfully"));
					returnString = returnString.substring(0,returnString.indexOf("succesfully") ) + " for quot_no = " + quotNo + returnString.substring(returnString.indexOf("</description>") );
					System.out.println("new returnString changed....."+returnString);				
				}
				else 
				{
					if(count>0)
					{
						returnString = itmDBAccessEJB.getErrorString("","QDAPRSUC",userId,"",conn);
						System.out.println("returnString before:- " + returnString + "\n" + returnString.indexOf("succesfully"));
						returnString = returnString.substring(0,returnString.indexOf("succesfully") ) + " for quot_no = " + quotNo + returnString.substring(returnString.indexOf("</description>") );
						System.out.println("new returnString changed....."+returnString);				
					}
					
				}
			}		
			
		}
	   	catch(Exception e)
		{
			   System.out.println("Exception in QuotationCancelApprvPrcEJB..."+e.getMessage());
			   e.printStackTrace();
			   /*errorString = e.getMessage();*/ //Commented By Mukesh Chauhan on 06/08/19
			   throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}		
		try
		{
			if(errorString == null || errorString.trim().length() == 0)
			{
				conn.commit();
				System.out.println("Connection commited");
				System.out.println("errString before :--- " + errString);
				if(errString != null)
				{
					errString = returnString;
				}
				System.out.println("errString after :--- " + errString);
			}
			else
			{
				conn.rollback();
				System.out.println("Conection rolbacked for errstring.....");
				errString = returnString ;
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
			/*rtrStr = e.getMessage();*/  //Commented By Mukesh Chuahan on 06/08/19
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
		StringBuffer retTabSepStrBuff = new StringBuffer();		
		//String siteCode=null;
		String saleOrderFrom ="",saleOrderTo = "",sOrderDateFrom = "",sOrderDateTo ="";
		String lineNo = "",itemCode = "",itemCodeDescr = "",enqNo = "",status = "",quotNo = "",indNo = "";
		String rate = "",quantity="",pOrder="";		
		ConnDriver connDriver = new ConnDriver();		
		quotNo  = genericUtility.getColumnValue("quot_no",headerDom);	
		System.out.println("quotNo from header..............."+quotNo);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String userId = "";
		
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

			//code by manazir hasan on 2/26/2009 
			sql = " select status from pquot_hdr where quot_no = '" + quotNo + "'" ;
			System.out.println("sql....."+sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				status = rs.getString(1)==null ? "":rs.getString(1);
			}
			rs.close();
			rs = null;
			System.out.println("status....."+status);
			if(status.trim().equalsIgnoreCase("A"))
			{
				status = "";

				sql = " select	purc_order, (case when status is null then 'O' else status end) "
						+" from		porder 	where 	quot_no = '" + quotNo + "' " ;
				System.out.println("sql....."+sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					pOrder = rs.getString(1) == null ? "" : rs.getString(1);
					status = rs.getString(2)==null ? "" : rs.getString(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(status.trim().equalsIgnoreCase("X")|| status.trim().equalsIgnoreCase("C")||status.trim().equalsIgnoreCase(""))
				{

					sql="SELECT pquot_hdr.supp_code, "
						+" pquot_det.quot_no, "
						+" pquot_det.ind_no, "
						+"pquot_det.enq_no, "
						+"pquot_det.line_no,"
						+"pquot_det.item_code, "  
						+"pquot_det.rate, "  
						+"pquot_det.quantity, "  
						+"pquot_det.status,"
						+"item.descr "
						+"FROM pquot_hdr, "
						+"pquot_det, item "
						+"WHERE ( pquot_hdr.quot_no = pquot_det.quot_no ) and "
						+"( pquot_det.item_code = item.item_code ) and "
						+"( pquot_det.status = 'A' ) and "
						+"( ( pquot_det.quot_no = '"+quotNo+"'  ) )  ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						status = rs.getString("status");
						itemCode = rs.getString("item_code");
						itemCodeDescr = rs.getString("descr");
						rate = rs.getString("rate");
						quantity = rs.getString("quantity")==null?"":rs.getString("quantity");
						indNo = rs.getString("ind_no");
						lineNo = rs.getString("line_no");
						enqNo = rs.getString("enq_no");							
						//procDate = genericUtility.getValidDateString( procDateStr,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						retTabSepStrBuff.append(status).append("\t");
						retTabSepStrBuff.append(lineNo).append("\t");
						retTabSepStrBuff.append(itemCode).append("\t");		
						retTabSepStrBuff.append(itemCodeDescr).append("\t");
						retTabSepStrBuff.append(rate).append("\t");
						retTabSepStrBuff.append(quantity).append("\t");	
						retTabSepStrBuff.append(indNo).append("\t");							
						retTabSepStrBuff.append(enqNo).append("\t");	
						retTabSepStrBuff.append("\n");				
					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					resultString = retTabSepStrBuff.toString();
				
				} // end of if
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTPOALMADE","Purchase Order"+pOrder+ "is allready made for this quotation","",conn);
					resultString =errString ;
				
				}
			}	// end of if status "A" code 
			else
			{
				resultString = itmDBAccessEJB.getErrorString("","QDAPRDUP",userId,"",conn);
				System.out.println("returnString before:- " + returnString + "\n" + returnString.indexOf("AlreadyUnApproved"));
				//returnString = returnString.substring(0,returnString.indexOf("Already UnApproved") ) + " for quot_no = " + quotNo + returnString.substring(returnString.indexOf("</description>") );
				System.out.println("new returnString changed....."+returnString);
				
			}				
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

	private Timestamp getCurrdateAppFormat() throws ITMException
    {
        String s = "";	
		 Timestamp timestamp = null;		
       // GenericUtility genericUtility = GenericUtility.getInstance();
        try
        {
            java.util.Date date = null;
            timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println(genericUtility.getDBDateFormat());
            
            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
           // s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
        }
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
            throw new ITMException(exception); //Added By Mukesh Chauhan on 06/08/19
        }
        return timestamp;
    }
}//end class