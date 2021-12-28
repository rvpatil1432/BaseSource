package ibase.webitm.ejb.dis;
import java.rmi.RemoteException; 
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*; 

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import java.text.SimpleDateFormat;

// Date : 15/12/2006 
import javax.ejb.Stateless; // added for ejb3


//public class CreditNotePrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class CreditNotePrc extends ProcessEJB implements CreditNotePrcLocal, CreditNotePrcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	//ConnDriver connDriver = new ConnDriver();
		
	Connection conn = null;
	String loginCode=null;	
	StringBuffer retBuf = null;
	String errorString=null;	

	/*public void ejbCreate() throws RemoteException, CreateException
	{	
		System.out.println("[CreditNotePrcEJB]ejbCreate() called successfully..");
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
	
	//=>getData Method
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{	
		System.out.println("CreditNotePrcEJB :getData() function called");
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
			System.out.println("Exception :CreditNotePrcEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();			
		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errString=null;		
		String errCode = "";
		String getDataSql= "" ;
		String sql= "" ;
		String resultString = "";
		ResultSet rs1 = null,rs = null;
		PreparedStatement pstmt = null;
		Statement st = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();	
		String fromDate=null,toDate=null;
		String tranDate="",compRecvDt="",invoiceDate="",chgDate="";
		String acctCode = "",cctrCode = "";
		int cnt = 0;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			CommonConstants.setIBASEHOME();	
		}
		catch (Exception e)
		{
			System.out.println("Exception :CreditNotePrcEJB :ejbCreate :==>"+e);			
			e.printStackTrace();
		} 
		
		try
		{
			fromDate = genericUtility.getColumnValue("from_date",headerDom);
			System.out.println("[CreditNotePrcEJB]FromDate ="+fromDate);
			toDate = genericUtility.getColumnValue("to_date",headerDom);
			System.out.println("[CreditNotePrcEJB]toDate ="+toDate);
			/****************************ADDED BY DURGA 10/04/07 ***********************************************/
			acctCode = genericUtility.getColumnValue("acct_code",headerDom);
			cctrCode = genericUtility.getColumnValue("cctr_code",headerDom);
		/****************************ADDED BY DURGA 10/04/07 ***********************************************/
			if(fromDate!=null && fromDate.equals(""))
			{
				fromDate = "";
				System.out.println("From Date is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			if ( fromDate == null || fromDate.trim().length() == 0 )
			{
				fromDate = "";
				System.out.println("From Date is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			} 
			if(toDate!=null && toDate.equals(""))
			{
				toDate = "";
				System.out.println("To Date is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			if ( toDate == null || toDate.trim().length() == 0 )
			{
				toDate = "";
				System.out.println("To Date is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			} 
			/****************************ADDED BY DURGA 10/04/07 ***********************************************/
			if ( acctCode == null || toDate.trim().length() == 0 )
			{
				System.out.println(" ***********ACCOUNT CODE IS NULL************** ");
				errString = itmDBAccessEJB.getErrorString("","VMACTCDMT","","",conn);
				return errString;
			}
			else
			{
				sql = "SELECT  COUNT(ACCT_CODE)  AS COUNT  FROM ACCOUNTS WHERE ACCT_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, acctCode);
				rs =  pstmt.executeQuery();
				if(rs.next())
				{
					cnt =  rs.getInt("COUNT");
					rs.close();
					pstmt.close();
				}
				if(cnt==0)
				{
					System.out.println(" **********ACCT CODE IS NULL ************** ");
					errString = itmDBAccessEJB.getErrorString("","VMACTCDMT","","",conn);
					return errString;
				}
			}
			if(cctrCode == null || cctrCode.trim().length() == 0)
			{
				System.out.println(" ***********CCTR CODE IS NULL************** ");
				errString = itmDBAccessEJB.getErrorString("","VMCCTRMT","","",conn);
				return errString;
			}
			else
			{
				sql = "SELECT  COUNT(CCTR_CODE) AS COUNT  FROM COSTCTR WHERE CCTR_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, cctrCode);
				rs =  pstmt.executeQuery();
				if(rs.next())
				{
					cnt =  rs.getInt("COUNT");
					rs.close();
					pstmt.close();
				}
				if(cnt==0) 
				{
					System.out.println(" ***********CCTR CODE IS  DOES NOT EXIST IN COSTCTR table************** ");
					errString = itmDBAccessEJB.getErrorString("","VMCCTRMT","","",conn);
					return errString;
				}
			}
/****************************End Adding BY DURGA 10/04/07 ***********************************************/
		
			//==============Conversion of date from app to db format=========================
			fromDate=this.genericUtility.getValidDateString(fromDate.trim(),this.genericUtility.getApplDateFormat(),this.genericUtility.getDBDateFormat());
			toDate=this.genericUtility.getValidDateString(toDate.trim(),this.genericUtility.getApplDateFormat(),this.genericUtility.getDBDateFormat());
			
			System.out.println("[CreditNotePrcEJB]FromDate[dbformat] ="+fromDate+ " 00:00:00.0");
			System.out.println("[CreditNotePrcEJB]toDate[dbformat] ="+toDate+ " 00:00:00.0");
		
			if((CommonConstants.DB_NAME).equalsIgnoreCase("ORACLE"))
			{
				getDataSql="SELECT A.COMPLAINT_NO,A.TRAN_DATE,A.COMP_RECV_DT,"   
					+" A.INVOICE_NO,A.INVOICE_DATE,A.CUST_CODE,B.CUST_NAME,A.SALES_PERS,C.SP_NAME,"
					+" A.COMPLAINT_TYPE,A.RETURN_BASIS,A.AMOUNT,A.CHG_USER,A.CHG_DATE,A.CHG_TERM,"
					+" A.SITE_CODE,D.DESCR,A.TRAN_ID_CRN,A.STATUS_APRV "
					+" FROM COMPLAINT_HDR A,CUSTOMER B,SALES_PERS C,SITE D  "
					+" WHERE ( B.cust_code (+) = A.cust_code) and ( A.sales_pers = C.sales_pers (+)) "
					+" and ( A.site_code = D.site_code (+)) and ((A.TRAN_DATE >=?) AND "
					+" ( A.TRAN_DATE <= ? ) ) AND A.STATUS_APRV = 'Y'";

			}
			else if((CommonConstants.DB_NAME).equalsIgnoreCase("DB2")||(CommonConstants.DB_NAME).equalsIgnoreCase("MSSQL"))
			{
				getDataSql="SELECT A.COMPLAINT_NO,A.TRAN_DATE,A.COMP_RECV_DT,"   
					+" A.INVOICE_NO,A.INVOICE_DATE,A.CUST_CODE,B.CUST_NAME,A.SALES_PERS,C.SP_NAME,"
					+" A.COMPLAINT_TYPE,A.RETURN_BASIS,A.AMOUNT,A.CHG_USER,A.CHG_DATE,A.CHG_TERM,"
					+" A.SITE_CODE,D.DESCR,A.TRAN_ID_CRN,A.STATUS_APRV "
					+" FROM COMPLAINT_HDR A LEFT OUTER JOIN CUSTOMER B ON A.CUST_CODE=B.CUST_CODE AND "
					+" COMPLAINT_HDR A LEFT OUTER JOIN SALES_PERS C ON A.SALES_PERS=C.SALES_PERS AND "
					+" COMPLAINT_HDR A LEFT OUTER JOIN SITE D ON A.SITE_CODE=D.SITE_CODE "
					+" WHERE A.TRAN_DATE >=? AND A.TRAN_DATE<=? AND A.STATUS_APRV = 'Y' ";
			}
			pstmt = conn.prepareStatement(getDataSql);			
			pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(fromDate.trim()+ " 00:00:00.0"));//for from_date
			pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(toDate.trim()+ " 00:00:00.0"));//for to_date
			rs1 = pstmt.executeQuery();
	
			while(rs1.next())
			{
					//complaint_no
					retTabSepStrBuff.append(blanknull(rs1.getString(1))).append("\t");						
					//tran_date
					tranDate=rs1.getString(2);
					if(tranDate!=null)
					{
						tranDate=this.genericUtility.getValidDateString(tranDate,this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat());
					}
					else
					{
						tranDate="";					
					}
					retTabSepStrBuff.append(tranDate).append("\t");
					//comp_recv_dt
					compRecvDt=rs1.getString(3);
					if(compRecvDt!=null)
					{
						compRecvDt=this.genericUtility.getValidDateString(compRecvDt,this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat());
					}
					else
					{
						compRecvDt="";					
					}
					retTabSepStrBuff.append(compRecvDt).append("\t");
					//invoice_no
					retTabSepStrBuff.append(blanknull(rs1.getString(4))).append("\t");
					//invoice_date
					invoiceDate=rs1.getString(5);
					if(invoiceDate!=null)
					{
						invoiceDate=this.genericUtility.getValidDateString(invoiceDate,this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat());
					}
					else
					{
						invoiceDate="";					
					}
					retTabSepStrBuff.append(invoiceDate).append("\t");
					//cust_code
					retTabSepStrBuff.append(blanknull(rs1.getString(6))).append("\t");
					//cust_name
					retTabSepStrBuff.append(blanknull(rs1.getString(7))).append("\t");	
					//sales_pers
					retTabSepStrBuff.append(blanknull(rs1.getString(8))).append("\t");					
					//sp_name
					retTabSepStrBuff.append(blanknull(rs1.getString(9))).append("\t");
					//complaint_type				
					retTabSepStrBuff.append(blanknull(rs1.getString(10))).append("\t");
					//return_basis
					retTabSepStrBuff.append(blanknull(rs1.getString(11))).append("\t");
					//amount
					retTabSepStrBuff.append(rs1.getDouble(12)).append("\t");
					//chg_user
					retTabSepStrBuff.append(blanknull(rs1.getString(13))).append("\t");
					//chg_date
					chgDate=rs1.getString(14);
					if(chgDate!=null)
					{
						chgDate=this.genericUtility.getValidDateString(chgDate,this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat());
					}
					else
					{
						chgDate="";					
					}
					retTabSepStrBuff.append(chgDate).append("\t");
					//chg_term
					retTabSepStrBuff.append(blanknull(rs1.getString(15))).append("\t");
					//site_code
					retTabSepStrBuff.append(blanknull(rs1.getString(16))).append("\t");
					//descr
					retTabSepStrBuff.append(blanknull(rs1.getString(17))).append("\t");
					//tran_id_crn
					retTabSepStrBuff.append(blanknull(rs1.getString(18))).append("\t");
					//status_aprv
					if(rs1.getString(19)!=null)
						retTabSepStrBuff.append(blanknull(rs1.getString(19))).append("\t");
					else
						retTabSepStrBuff.append("N").append("\t");					
					retTabSepStrBuff.append("\n");					
			}
			resultString = retTabSepStrBuff.toString();
			System.out.println("ResultString....." + resultString);
			rs1.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("SQLException :CreditNotePrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :CreditNotePrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
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

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
		throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
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
			
			System.out.println("Exception :CreditNotePrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		int updCnt = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		String errString=null;
		String childNodeName = "";
		
		String complaintNo=null;
		String statusAprv=null;
		String tranIdCrn=null;
		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;

		Node parentNode = null;
		Node childNode = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);				
		}
		catch (Exception e)
		{
			System.out.println("Exception :CreditNotePrcEJB :ejbCreate :==>"+e);	
			e.printStackTrace();
		} 
		
		try
		{
			loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginCode=blanknull(loginCode).trim();

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
					if (childNodeName.equalsIgnoreCase("complaint_no"))
					{
						complaintNo = childNode.getFirstChild().getNodeValue();						
					}					
					if (childNodeName.equalsIgnoreCase("status_aprv"))
					{
						statusAprv = childNode.getFirstChild().getNodeValue();
					}
					if (childNodeName.equalsIgnoreCase("tran_id_crn"))
					{
						tranIdCrn = childNode.getFirstChild().getNodeValue();
					}

				}//inner for loop	
				System.out.println("[CreditNotePrcEJB]complaintNo=>"+complaintNo);
				System.out.println("[CreditNotePrcEJB]statusAprv=>"+statusAprv);

				statusAprv=blanknull(statusAprv).trim();
				complaintNo=blanknull(complaintNo).trim();
				tranIdCrn=blanknull(tranIdCrn).trim();
				System.out.println("[CreditNotePrcEJB]tranIdCrn=>"+tranIdCrn);
				
				String sql = "",adjConfirm = "",returnBasis = "";
				
				sql = "select return_basis from complaint_hdr where COMPLAINT_NO = '"+complaintNo+"'";
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				
				if(rs.next()){
				    returnBasis = rs.getString(1);
				}
				
				//Added by Durga 10/04/07
				String acctCode = genericUtility.getColumnValue("acct_code",headerDom);
				String cctrCode = genericUtility.getColumnValue("cctr_code",headerDom);
				System.out.println("*************ACCTOUNT CODE IS ="+acctCode);
				System.out.println("*************CCTR CODE IS= "+acctCode);
				//End Adding Durga

				if(returnBasis  != null && returnBasis.equalsIgnoreCase("V")){
				    if(statusAprv.equalsIgnoreCase("Y") && tranIdCrn.length()== 0 ){
				        errorString = insertRecord(complaintNo,acctCode,cctrCode,conn);	
				    }
				}
				else if(returnBasis  != null && returnBasis.equalsIgnoreCase("Q")){
				    sql = "select CONFIRMED from adj_issrcp where order_id = '"+complaintNo+"'";
					 
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					
					if(rs.next()){
					    adjConfirm = rs.getString(1);
						System.out.println("[CreditNotePrcEJB]CONFIRMED.........=>"+adjConfirm);
					}
					rs.close();
					stmt.close();
				    if(statusAprv.equalsIgnoreCase("Y") && tranIdCrn.length()== 0 && adjConfirm.equalsIgnoreCase("Y")){
				        errorString=insertRecord(complaintNo,acctCode,cctrCode,conn);	
				    }
				}				
				/*if(statusAprv.equalsIgnoreCase("Y") && tranIdCrn.length()==0 && adjConfirm.equalsIgnoreCase("Y"))
				{
					errorString=insertRecord(complaintNo,conn);	
				}	*/			
				if(errorString!=null)
				{
					try
					{
						conn.rollback();
						System.out.println("Connection is roll back");
						break;
					}
					catch(Exception d)
					{
						System.out.println("Exception : CreditNotePrcEJB =>"+d.toString());
						d.printStackTrace();
						break;
					}
				}//end of if
			}// OUT FOR LOOP 
		}//try end	
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception d)
			{
				System.out.println("Exception : CreditNotePrcEJB =>"+d.toString());
				d.printStackTrace();
			}
			System.out.println("Exception :CreditNotePrcEJB :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			errString = e.getMessage();			
			return errString ;
		}
		finally
		{
			System.out.println("Closing Connection....");
			try
			{
				if(errorString==null)
				{
					System.out.println("Connection Commited");
					conn.commit();
					errorString = itmDBAccessEJB.getErrorString("","VTGENCR","","",conn);
				}
				else if(errorString!=null)
				{
					System.out.println("Connection is rollback");
					conn.rollback();
					errorString = itmDBAccessEJB.getErrorString("","VTCRNGN","","",conn);
				}
				retBuf = null;
				if(conn != null)
				{					
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString ;
			}
		}
		System.out.println("Error Message=>"+errorString);
		return errorString;
	}//END OF PROCESS(2)	

	private String insertRecord(String complaintNo,String acctCodeDet, String cctrCodeDet, Connection con)
	{
		ResultSet rsHdr=null,rsDet=null;
		PreparedStatement pstmt1=null,pstmt2=null;
		String tranDate=null,finEntity=null,siteCode=null;
		String custCode=null,acctCode=null,cctrCode=null;
		double amount=0.0,quantity=0.0,rate=0.0;
		String currCode=null,exrt=null,status=null;
		String chgUser=null,chgDate=null,chgTerm=null;
		boolean isFound=false;
		String xmlValues=null,newTranId=null;
		String tranDate1=null;
		String errString =null;
		
		int aLineno=0;//for line_no 

		complaintNo=blanknull(complaintNo).trim();

		String qrySeHdr="SELECT A.TRAN_DATE,C.FIN_ENTITY,A.SITE_CODE,A.CUST_CODE,"
		+" B.ACCT_CODE__AR,B.CCTR_CODE__AR,A.AMOUNT,B.CURR_CODE,D.STD_EXRT,A.STATUS_APRV,"
		+" A.CHG_USER,A.CHG_DATE,A.CHG_TERM "
		+" FROM COMPLAINT_HDR A,CUSTOMER B,SITE C,CURRENCY D "
		+" WHERE A.CUST_CODE=B.CUST_CODE AND A.SITE_CODE=C.SITE_CODE AND "
		+" B.CURR_CODE=D.CURR_CODE AND A.COMPLAINT_NO='"+complaintNo.trim()+"'";

		String qryInHdr="INSERT INTO MISC_DRCR_RCP(TRAN_ID,TRAN_SER,TRAN_DATE,EFF_DATE,FIN_ENTITY,"
		+" SITE_CODE,SUNDRY_TYPE,SUNDRY_CODE,ACCT_CODE,CCTR_CODE,AMOUNT,CURR_CODE,"
		+" EXCH_RATE,DRCR_FLAG,CONFIRMED,CHG_USER,CHG_DATE,CHG_TERM,REMARKS) "
		+" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

		String qrySeDet=" SELECT INV_QTY,INV_RATE, "
		+" (CASE WHEN INV_QTY IS NULL THEN 0 ELSE INV_QTY END) "
		+" *(CASE WHEN INV_RATE IS NULL THEN 0 ELSE INV_RATE END)AS AMOUNT "
		+" FROM COMPLAINT_DET WHERE COMPLAINT_NO='"+complaintNo.trim()+"'";

		String qryInDet=" INSERT INTO MISC_DRCR_RDET "
		+" (TRAN_ID,LINE_NO,ACCT_CODE,CCTR_CODE,AMOUNT,QUANTITY,RATE) "
		+" VALUES(?,?,?,?,?,?,?)";

		String qryUpHdr="UPDATE COMPLAINT_HDR SET TRAN_ID_CRN=? WHERE COMPLAINT_NO=?";
		
		try
		{
			//=-=-=-=-=-=-=-=-=-=-=-=-=Code for header=-=-=-=-=--=-=-=-=-=-=-=-=
			pstmt1=con.prepareStatement(qrySeHdr);
			rsHdr=pstmt1.executeQuery();
			if(rsHdr.next())
			{
				isFound=true;//record is found
				tranDate=rsHdr.getString(1);//for tran_date
				finEntity=rsHdr.getString(2);//for fin_entity
				siteCode=rsHdr.getString(3);//for site_code
				custCode=rsHdr.getString(4);//for cust_code
				acctCode=rsHdr.getString(5);//for acct_code__ar
				cctrCode=rsHdr.getString(6);//for cctr_code__ar
				amount=rsHdr.getDouble(7);//for amount
				currCode=rsHdr.getString(8);//for curr_code
				exrt=rsHdr.getString(9);//for std_exrt
				status=rsHdr.getString(10);//for status_aprv
				chgUser=rsHdr.getString(11);//for chg_user
				chgDate=rsHdr.getString(12);//for chg_date
				chgTerm=rsHdr.getString(13);//for chg_term

				tranDate=blanknull(tranDate);
				finEntity=blanknull(finEntity);
				siteCode=blanknull(siteCode);
				custCode=blanknull(custCode);
				acctCode=blanknull(acctCode);
				cctrCode=blanknull(cctrCode);
				currCode=blanknull(currCode);
				exrt=blanknull(exrt);
				status=blanknull(status);
				chgUser=blanknull(chgUser);
				chgDate=blanknull(chgDate);
				chgTerm=blanknull(chgTerm);
			}	
			rsHdr.close();
			pstmt1.close();
			System.out.println("[CreditNotePrcEJB]isFound=>"+isFound);
			if(isFound)//if record found
			{
				pstmt1=con.prepareStatement(qryInHdr);

				tranDate1=genericUtility.getValidDateString(new Timestamp(System.currentTimeMillis()).toString(),						genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
			
				xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues +	"<tran_id></tran_id>";
				xmlValues = xmlValues + "<site_code>"+siteCode.trim()+"</site_code>";
				xmlValues = xmlValues + "<drcr_flag>C</drcr_flag>";
				xmlValues = xmlValues +  "<tran_date>"+tranDate1.trim()+"</tran_date>";
				xmlValues = xmlValues + "</Detail1></Root>";

				newTranId = generateTranId("w_misc_drcr_rcp",xmlValues);//function to generate NEW transaction id
				newTranId=blanknull(newTranId).trim();

				if(newTranId==null || blanknull(newTranId).trim().equals("") || newTranId.equalsIgnoreCase("error"))
				{
					errString="Error Occured";
				}
				System.out.println("[CreditNotePrcEJB] new Tran ID="+newTranId);

				pstmt1.setString(1,newTranId);//for tran_id
				pstmt1.setString(2,"MDRCRC");//for tran_ser//previously "CRNPAY" was there
				pstmt1.setTimestamp(3,java.sql.Timestamp.valueOf(getCurrdateAppFormat().toString()));//for tran_date
				pstmt1.setTimestamp(4,java.sql.Timestamp.valueOf(tranDate.trim()));//for eff_date
				pstmt1.setString(5,finEntity.trim());//for fin_entity
				pstmt1.setString(6,siteCode.trim());//for site_Code
				pstmt1.setString(7,"C");//for sundry_type
				pstmt1.setString(8,custCode.trim());//for sundry_code
				if(acctCode == null || acctCode.trim().length() == 0){
				    pstmt1.setNull(9,java.sql.Types.VARCHAR);
				}else{
				    pstmt1.setString(9,acctCode.trim());//for acct_code
				}
				if(cctrCode == null || cctrCode.trim().length() == 0){
				    pstmt1.setNull(10,java.sql.Types.VARCHAR);
				}else{
				    pstmt1.setString(10,cctrCode.trim());//for cctr_code
				}				
				pstmt1.setDouble(11,amount);//for amount
				pstmt1.setString(12,currCode.trim());//for curr_code
				pstmt1.setString(13,exrt.trim());//for exch_rate
				pstmt1.setString(14,"C");//for drcr_flag
				pstmt1.setString(15,"N");//for confirmed
				pstmt1.setString(16,chgUser.trim());//for chg_user
				pstmt1.setTimestamp(17,java.sql.Timestamp.valueOf(chgDate.trim()));//for chg_date
				pstmt1.setString(18,chgTerm.trim());//for chg_term
        pstmt1.setString(19,"Credit Note Generated For Complaint "+complaintNo);//for chg_term

				pstmt1.executeUpdate();
				pstmt1.close();


				//-=-=-=Code for update record in Complaint_hdr=-=-=-=-=-=
			   System.out.println("[CreditNotePrcEJB] newTranId=>............"+newTranId.trim());
				pstmt1=con.prepareStatement(qryUpHdr);
				pstmt1.setString(1,newTranId.trim());
				pstmt1.setString(2,complaintNo.trim());
				System.out.println("[CreditNotePrcEJB]Record Updated in Complaint_hdr =>"+pstmt1.executeUpdate());
				pstmt1.close();			

			}//end of if(isFound)
			//=-=-=-=-=-=-=-=-=-=-=-=-=End of Code for header=-=-=-=-=--=-=-=-=-=-=-=-=

			//=-=-=-=-=-=-=-=-=-=-=-=-=Code for Details=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=			
			pstmt1=con.prepareStatement(qrySeDet);
			rsDet=pstmt1.executeQuery();

			pstmt2=con.prepareStatement(qryInDet);			

			while(rsDet.next())
			{
				quantity=rsDet.getDouble(1);//for inv_qty
				rate=rsDet.getDouble(2);//for inv_rate
				amount=rsDet.getDouble(3);//for amount
			
				aLineno++;//auto increment

				pstmt2.setString(1,newTranId.trim());//for tran_id
				pstmt2.setInt(2,aLineno);//for line_no
/***************** CHANGED BY DURGA 10/04/07 ************************************************/
				
				pstmt2.setString(3,acctCodeDet);
				pstmt2.setString(4,cctrCodeDet);
				pstmt2.setDouble(5,amount);

				
				//pstmt2.setString(3,acctCode);//for acct_code 
				//pstmt2.setString(4,cctrCode);//for cctr_code
				//pstmt2.setDouble(5,amount);//for amount

/******************CHANGED BY DURGA 10/04/07 ************************************************/								
				pstmt2.setDouble(6,quantity);//for quantity
				pstmt2.setDouble(7,rate);//for rate				
				pstmt2.addBatch();
			}

			System.out.println("[CreditNotePrcEJB]Record record inserted in misc_drcr_rdet =>"+pstmt2.executeBatch());
			pstmt2.close();
			//=-=-=-=-=-=-=-=-=-=-=-=-=End of Code for Details=-=-=-=-=--=-=-=-=-=-=-=-=
		}
		catch (Exception e)
		{
			e.printStackTrace();
			errString=e.getMessage();
		}
		System.out.println("[CreditNotePrcEJB]==>errString=>"+errString);
		return errString;
	}
	
	public String blanknull(String s)
	{
		if(s==null)
			return " ";
		else
			return s;
	}//end of blanknull()	

	private String generateTranId(String windowName,String xmlValues)
	{
		System.out.println("[CreditNotePrcEJB]==inside generateTranId>");
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String tranId = null;
		String newKeystring = "";
		String srType = "RS";
		 try
	     {
	    	sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)=UPPER('"+windowName+"')";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			System.out.println("keyString :"+rs.toString());
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rs.next())
			{
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);				
			}
			// Changed by Sneha on 21-07-2016, for Closing the Open Cursor [Start]
			if ( stmt != null )
			{
				stmt.close();
				stmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			// Changed by Sneha on 21-07-2016, for Closing the Open Cursor [End]
			
			System.out.println("keyString=>"+keyString);
			System.out.println("keyCol=>"+keyCol);
			System.out.println("tranSer1"+tranSer1);
			
			System.out.println("xmlValues  :["+xmlValues+"]");
			loginCode=blanknull(loginCode);
			TransIDGenerator tg = new TransIDGenerator(xmlValues, loginCode.trim(), CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
			if(rs!=null)
			 {
				rs.close();
			 }
			 if(stmt!=null)
			 {	
				stmt.close();
			 }
		}
		catch(SQLException ex)
		{
			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");			
			ex.printStackTrace();	
			tranId=null;
		}
		catch(Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			tranId=null;
		}
		return tranId;
	}//generateTranTd()

	private Timestamp getCurrdateAppFormat()
	{
		String currAppdate ="";
		java.sql.Timestamp currDate = null;
		try
		{
				Object date = null;
				currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
				date = sdf.parse(currDate.toString());
				currDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");						
		}
		catch(Exception e)
		{
			System.out.println("Exception in :::CreditNotePrcEJB"+e.getMessage());
			e.printStackTrace();
		}
		return (currDate);
	}//end of getCurrdateAppFormat()
}//END OF EJB