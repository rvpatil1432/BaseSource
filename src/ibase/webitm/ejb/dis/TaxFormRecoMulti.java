/*
* In TaxFormRecoMulti the "summary" case is not added so it's added, And for "Detail" VOUC and M-VOUCH cases are not added which are added Afterwards.
* In the case of M-VOUC for the both summary and detail the sandry_type is needed so the case for this is added.
* And the connection was globly declared which is removed and declared in proper method
* by Jaimin on 30/08/2007 According to the requst-ID :DI78DIS028
*/

//object name : TaxFormRecoMulti
package ibase.webitm.ejb.dis;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.*;
import org.omg.CORBA.ORB;
import org.w3c.dom.*;

import java.util.Properties;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.Stateless; // added for ejb3



//public class TaxFormRecoMultiEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class TaxFormRecoMulti extends ProcessEJB implements TaxFormRecoMultiLocal, TaxFormRecoMultiRemote
{
	DistCommon distCommonObj = new DistCommon();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	//ConnDriver connDriver = new ConnDriver();//commented by Jaimin 30/08/2007 no need to declare globaly as it will giving error for reprocessing.
	//Connection conn= null; //commented by Jaimin 30/08/2007  requst-ID :DI78DIS028
	//conn.setAutoCommit(false);
	
	/*public void ejbCreate() throws RemoteException, CreateException
	{ 
		try
		{
			if(conn==null) 
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				/*Class.forName ("oracle.jdbc.driver.OracleDriver");
				java.util.Properties prop = new java.util.Properties();
			    prop.setProperty("user","livaho");
			    prop.setProperty("password","livaho");
			    prop.setProperty("DelimitIdentifier","Yes");
			    prop.setProperty("Trimspaces","No");
		
			    conn = DriverManager.getConnection ("jdbc:oracle:thin:@192.168.0.205:1521:itm90", prop) ;
			    
			    //conn = DriverManager.getConnection ("jdbc:oracle:thin:@192.168.0.205:1521:@9i", prop) ; // "livaho", "livaho");
			    
				
				conn.setAutoCommit(false);		
			}
		}
		catch(Exception e)
		{
		}
		
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
	//getData Method
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
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
			
			System.out.println("Exception :TaxFormRecoMultiEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();
		
		}
		return rtrStr; 
	}//END OF GETDATA(1)
	public String blanknull(String s)
	{
		if(s==null)
			return " ";
		else
			return s;
	}
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		String errCode = "";
		String errString = "";
		String getDataSql= "" ;

		String resultString = "";
		ResultSet rs1 = null;
		PreparedStatement pstmt = null;
		//StringBuffer retTabSepStrBuff = new StringBuffer();  //comment by cpatil 12/02/14
		//added by cpatil
		StringBuffer retTabSepStrBuff = new StringBuffer("<?xml version = \"1.0\"?>");
		retTabSepStrBuff.append("<DocumentRoot>");
		retTabSepStrBuff.append("<description>").append("Datawindow Root").append("</description>");
		retTabSepStrBuff.append("<group0>");
		retTabSepStrBuff.append("<description>").append("Group0 description").append("</description>");
		retTabSepStrBuff.append("<Header0>");
		//end by cpatil
		//==========new variables====================
		String sundryType = null;
		String refSeriesFr = null;
		String refSeriesTo = null;
		String refSer = null;//For ref. series
		String siteCode = null;
		String tranCode = null;
		String dateFrom = null;//for tran_date from
		String dateTo = null;//for tran_date to
		String taxCodeFrom = null;//for tax_code from
		String taxCodeTo = null;//for tax_code to
		String sundCodeFrom = null;//for cust_code from
		String sundCodeTo = null;//for cust_code to
		String type = null;//for type
		
		/*added by chandrakant patil on 9-APR-12 Start*/
		String sql = "";
		int cnt=0;
		/*added by chandrakant patil end*/
		//============================================

		double pendQty = 0;
		double allocQty = 0;	
		boolean bappend = false ;
		boolean frsappend = false ;	
		int count=0;//added by jaimin 23/08/07  requst-ID :DI78DIS028

		ConnDriver connDriver = new ConnDriver();//Added by Jaimin 30/08/2007 requst-ID :DI78DIS028
		Connection conn= null;//Added by Jaimin 30/08/2007
		try
		{
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				/*Class.forName ("oracle.jdbc.driver.OracleDriver");
			    java.util.Properties prop = new java.util.Properties();
			    prop.setProperty("user","livaho");
			    prop.setProperty("password","livaho");
			    prop.setProperty("DelimitIdentifier","No");
			    prop.setProperty("Trimspaces","Yes");
			    conn = DriverManager.getConnection ("jdbc:oracle:thin:@192.168.0.205:1521:itm90", prop) ;*/
			    
			    //conn = DriverManager.getConnection ("jdbc:oracle:oci8:@9i", prop) ; // "livaho", "livaho");
				 
				conn.setAutoCommit(false);	
			}
			DatabaseMetaData dbmd = conn.getMetaData();
			System.out.println("DriverName["+dbmd.getDriverName() + "]");
			System.out.println("DriverURI["+dbmd.getURL()  + "]");
			System.out.println("DriverUSER["+dbmd.getUserName() +"]");
					
			System.out.println("TaxFormRecoMulti : getData() Method Called");
			String tranType = distCommonObj.getDisparams("999999","TAX_CLASS_CHAP",conn);
			
		
			
	
			//=============new==============================
			sundryType = genericUtility.getColumnValue("sundry_type",headerDom);
			siteCode = genericUtility.getColumnValue("site_code",headerDom);
			dateFrom = genericUtility.getColumnValue("date_fr",headerDom);
			dateTo = genericUtility.getColumnValue("date_to",headerDom);
			taxCodeFrom = genericUtility.getColumnValue("tax_code_fr",headerDom);
			taxCodeTo = genericUtility.getColumnValue("tax_code_to",headerDom);
			sundCodeFrom = genericUtility.getColumnValue("sund_code_fr",headerDom);
			sundCodeTo = genericUtility.getColumnValue("sund_code_to",headerDom);
			refSer = genericUtility.getColumnValue("ref_ser",headerDom);
			type = genericUtility.getColumnValue("type",headerDom);

			
			sundryType = blanknull(sundryType).trim();
			siteCode = blanknull(siteCode).trim();
			dateFrom = blanknull(dateFrom).trim();
			dateTo = blanknull(dateTo).trim();
			taxCodeFrom = blanknull(taxCodeFrom).trim();
			taxCodeTo = blanknull(taxCodeTo).trim();
			sundCodeFrom = blanknull(sundCodeFrom).trim();
			sundCodeTo = blanknull(sundCodeTo).trim();
			refSer = blanknull(refSer).trim();
			type = blanknull(type).trim();



			//===============Convert date to TimeStamp date format===========
			Timestamp chgdateFrom = null,chgdateTo = null;
			Object date = null;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			date = sdf.parse(dateFrom);			
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			chgdateFrom =	java.sql.Timestamp.valueOf(sdf1.format(date).toString() + " 00:00:00.0");
			date = sdf.parse(dateTo);
			chgdateTo =	java.sql.Timestamp.valueOf(sdf1.format(date).toString() + " 00:00:00.0");

			System.out.println("Change DB dateFrom=>"+chgdateFrom+"=DB date to=>"+chgdateTo);

			//===============================================================

			
			System.out.println("sundryType =>"+sundryType);
			System.out.println("siteCode =>"+siteCode);
			System.out.println("dateFrom =>"+dateFrom);
			System.out.println("dateTo =>"+dateTo);
			System.out.println("taxCodeFrom =>"+taxCodeFrom);
			System.out.println("taxCodeTo =>"+taxCodeTo);
			System.out.println("sundCodeFrom =>"+sundCodeFrom);
			System.out.println("sundCodeTo =>"+sundCodeTo);
			System.out.println("refSer =>"+refSer);
			System.out.println("type =>"+type);
			
			if(	tranType.equalsIgnoreCase("NULLFOUND"))
			{
				resultString = itmDBAccessEJB.getErrorString("TaxProcess","VTTAXADJ1","",errString,conn); 
				return "";
				
			}		
			if ( sundryType == null || sundryType.trim().length() == 0 )
			{
				System.out.println("Sundry Type is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}
			if ( refSer == null || refSer.trim().length() == 0 )
			{
				System.out.println("Ref Ser. is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			} 
			if ( siteCode == null || siteCode.trim().length() == 0 )
			{
				System.out.println("Site code is Null...");
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			} 
			
			/* added by Chandrakant patil on 9-APR-2012  start*/

						if (refSer == null || refSer.trim().length() == 0)
						{
							errCode = "REFSERNULL";
							errString = itmDBAccessEJB.getErrorString("","REFSERNULL","","",conn);
							return errString;
						}
						else
						{
							sql = "select count(1) from refser where ref_ser ='" + refSer + "'";
							    pstmt = conn.prepareStatement(sql);
							    rs1 = pstmt.executeQuery();
							    if (rs1.next())
							    {
								cnt = rs1.getInt(1);
							    }
							    pstmt.close();
							    rs1.close();
							    pstmt = null;
							    rs1 = null;
							    if (cnt == 0)
							    {
								errCode = "REFSERINV";
								errString = itmDBAccessEJB.getErrorString("","REFSERINV","","",conn);
								return errString;
							    }
							}
						    
						    if (siteCode == null || siteCode.trim().length() == 0)
								{
								    errCode = "SITECONULL";
								    errString = itmDBAccessEJB.getErrorString("","SITECONULL","","",conn);
									return errString;
								}
								else
								{
								    sql = "select count(1) from SITE where site_code ='" + siteCode + "'";
								    pstmt = conn.prepareStatement(sql);
								    rs1 = pstmt.executeQuery();
								    if (rs1.next())
								    {
									cnt = rs1.getInt(1);
								    }
								    pstmt.close();
								    rs1.close();
								    pstmt = null;
								    rs1 = null;
								    if (cnt == 0)
								    {
									errCode = "SITECODINV";
									errString = itmDBAccessEJB.getErrorString("","SITECODINV","","",conn);
									return errString;
								    }
								}
							    
						    
						   if (dateFrom == null || dateFrom.trim().length() == 0)
							{
							    errCode = "DATEFRNULL";
							    errString = itmDBAccessEJB.getErrorString("","DATEFRNULL","","",conn);
								return errString;
							}
							else
							{
							    java.util.Date currDate = new java.util.Date(System.currentTimeMillis());
							    java.util.Date Date2 = sdf.parse(dateFrom);
							    if (Date2.after(currDate))
							    {
								errCode = "DATEFRMINV";
								errString = itmDBAccessEJB.getErrorString("","DATEFRMINV","","",conn);
								return errString;
							    }
							}
						    
						 
							if (dateTo == null || dateTo.trim().length() == 0)
							{
							    errCode = "DATETONULL";
							    errString = itmDBAccessEJB.getErrorString("","DATETONULL","","",conn);
								return errString;
							}
							else if (dateFrom != null)
							{
							    java.util.Date dateto1 = sdf.parse(dateTo);
							    java.util.Date datefr1 = sdf.parse(dateFrom);
							    if (dateto1.before(datefr1))
							    {
								errCode = "DATETOINV";
								errString = itmDBAccessEJB.getErrorString("","DATETOINV","","",conn);
								return errString;
							    }
							}
						    
						    if (sundCodeFrom == null || sundCodeFrom.trim().length() == 0)
								{
								    errCode = "SUNDCFRNUL";
								    errString = itmDBAccessEJB.getErrorString("","SUNDCFRNUL","","",conn);
									return errString;
								}
								/*else if(!(("0".equalsIgnoreCase(sundCodeFrom))||("ZZ".equalsIgnoreCase(sundCodeFrom))))
								{
								    sql = "select count(1) from customer where cust_code ='" + sundCodeFrom + "'";
								    pstmt = conn.prepareStatement(sql);
								    rs1 = pstmt.executeQuery();
								    if (rs1.next())
								    {
									cnt = rs1.getInt(1);
								    }
								    pstmt.close();
								    rs1.close();
								    pstmt = null;
								    rs1 = null;
								    if (cnt == 0)
								    {
									errCode = "SUNDCFRINV";
									errString = itmDBAccessEJB.getErrorString("","SUNDCFRINV","","",conn);
									return errString;
								    }
								}*/
							   if (sundCodeTo == null || sundCodeTo.trim().length() == 0)
								{
								    errCode = "SUNDCTONUL";
								    errString = itmDBAccessEJB.getErrorString("","SUNDCTONUL","","",conn);
									return errString;
								}
								/*else if(!(("0".equalsIgnoreCase(sundCodeTo))||("ZZ".equalsIgnoreCase(sundCodeTo))))
								{
								    sql = "select count(1) from customer where cust_code ='" + sundCodeTo + "'";
								    pstmt = conn.prepareStatement(sql);
								    rs1 = pstmt.executeQuery();
								    if (rs1.next())
								    {
									cnt = rs1.getInt(1);
								    }
								    pstmt.close();
								    rs1.close();
								    pstmt = null;
								    rs1 = null;
								    if (cnt == 0)
								    {
									errCode = "SUNDCTOINV";
									errString = itmDBAccessEJB.getErrorString("","SUNDCTOINV","","",conn);
									return errString;
								    }
								}*/
							    
							   if (taxCodeFrom == null || taxCodeFrom.trim().length() == 0)
								{
								    errCode = "TAXCFRNULL";
								    errString = itmDBAccessEJB.getErrorString("","TAXCFRNULL","","",conn);
									return errString;
								}
								else if(!(("0".equalsIgnoreCase(taxCodeFrom))||("ZZ".equalsIgnoreCase(taxCodeFrom))))
								{    
								    sql = "select count(1) from tax where tax_code ='" + taxCodeFrom + "'";
								    pstmt = conn.prepareStatement(sql);
								    rs1 = pstmt.executeQuery();
								    if (rs1.next())
								    {
									cnt = rs1.getInt(1);
								    }
								    pstmt.close();
								    rs1.close();
								    pstmt = null;
								    rs1 = null;
								    if (cnt == 0)
								    {
									errCode = "TAXCOFRINV";
									errString = itmDBAccessEJB.getErrorString("","TAXCOFRINV","","",conn);
									return errString;
								    }
								}
							    
							   if (taxCodeTo == null || taxCodeTo.trim().length() == 0)
								{
								    errCode = "TAXCOTONUL";
								    errString = itmDBAccessEJB.getErrorString("","TAXCOTONUL","","",conn);
									return errString;
								}
								else if(!(("0".equalsIgnoreCase(taxCodeTo))||("ZZ".equalsIgnoreCase(taxCodeTo))))
								{    
								    sql = "select count(1) from tax where tax_code ='" + taxCodeTo + "'";
								    pstmt = conn.prepareStatement(sql);
								    rs1 = pstmt.executeQuery();
								    if (rs1.next())
								    {
									cnt = rs1.getInt(1);
								    }
								    pstmt.close();
								    rs1.close();
								    pstmt = null;
								    rs1 = null;
								    if (cnt == 0)
								    {
									errCode = "TAXCOTOINV";
									errString = itmDBAccessEJB.getErrorString("","TAXCOTOINV","","",conn);
									return errString;
								    }
								}
							   
			    
			/*added by chandrakant patil end*/
							   
			tranType=tranType.trim();
			System.out.println("tranType=>"+tranType);
			
			System.out.println("Setting Parameters .....");
			System.out.println("refSer:::["+refSer+"]");
			System.out.println("siteCode:::["+siteCode+"]");
			System.out.println("chgdateFrom:::["+chgdateFrom+"]");
			System.out.println("chgdateTo:::["+chgdateTo+"]");
			System.out.println("sundCodeFrom:::["+sundCodeFrom+"]");
			System.out.println("sundCodeTo:::["+sundCodeTo+"]");
			System.out.println("taxCodeFrom:::["+taxCodeFrom+"]");
			System.out.println("taxCodeTo:::["+taxCodeTo+"]");
		
			
			System.out.println("tranType :::::["+tranType+"]");
			String refSerWs = refSer.trim()+"      ";
			refSerWs = refSerWs.substring(0,6);
			String siteCodeWs = siteCode.trim()+"     ";
			siteCodeWs = siteCodeWs.substring(0,5);
			String sundCodeFromWs = sundCodeFrom.trim();
			String sundCodeToWs = sundCodeTo.trim()+"          ";
			sundCodeToWs = sundCodeToWs.substring(0,10);
			String taxCodeFromWs = taxCodeFrom.trim();
			String taxCodeToWs = taxCodeTo.trim()+"     ";
			taxCodeToWs = taxCodeToWs.substring(0,5);
			
			
			System.out.println("A F T E R A D D I N G S P A C E");
			
			System.out.println("refSerWs:::::::::["+refSerWs+"]");
			System.out.println("siteCodeWs:::::::["+siteCodeWs+"]");
			System.out.println("sundCodeFromWs:::["+sundCodeFromWs+"]");
			System.out.println("sundCodeToWs:::::["+sundCodeToWs+"]");
			System.out.println("taxCodeFromWs::::["+taxCodeFromWs+"]");
			System.out.println("taxCodeToWs::::::["+taxCodeToWs+"]");
			
		//====================For class type ===================================
		if(type.equalsIgnoreCase("D"))
		{
			if(tranType!=null )
			{
				
				if(tranType.equalsIgnoreCase("CLASS"))
				{
					if(refSer!=null)
					{
						if(refSer.equals("S-RET"))
						{
							
							getDataSql="SELECT T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"
									+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"
									+"sum( T.TAX_AMT ) as tax_amt,C.CUST_NAME,S.TRAN_DATE,S.CUST_CODE,"
									+"S.EXCISE_REF_NO,S.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(C.CUST_TYPE,C.CUST_CODE) as CITY,S.SITE_CODE,S.NET_AMT " +
									" FROM TAXTRAN T,"  
									+ "TAX T1,CUSTOMER C,SRETURN S "
									+ "WHERE ( T1.TAX_CODE = T.TAX_CODE ) and ( C.CUST_CODE = S.CUST_CODE ) "
									+"and ( T.TRAN_ID = S.TRAN_ID ) and ( ( T.TRAN_CODE = ? ) "
									+"AND ( S.SITE_CODE = ? ) AND " 
									+"( S.TRAN_DATE between ? And ?   ) AND " 
									+"( S.CUST_CODE >= ? And S.CUST_CODE <= ?  ) AND " 
									+"( T.TAX_CODE   >= ? And T.TAX_CODE <= ?  ) AND " 
									+"( T1.FORM_REQD  = 'Y' ) AND ( T.TAX_CLASS is not null ) AND " 
									+"( T.TAX_FORM   is null OR T.TAX_FORM_DATE   is null )  ) "  
									+"GROUP BY T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,"   
									+"T.TAX_FORM_DATE,T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,"
									+"T.TAX_CODE,T.TAX_CHAP,S.TRAN_DATE,S.CUST_CODE,C.CUST_NAME,"  
									+"S.EXCISE_REF_NO,S.EXCISE_REF_DATE,C.CUST_TYPE,C.CUST_CODE,S.SITE_CODE, S.NET_AMT ";
							
							System.out.println("type:D::tranType:CLASS::refSer:S-RET---->Executed ");	
						}
						else if(refSer.equals("P-RCP"))
						{
							getDataSql="SELECT T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"
										+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"
										+" sum( T.TAX_AMT ) as tax_amt,S.SUPP_NAME,P.TRAN_DATE,P.SUPP_CODE," +
										"P.EXCISE_REF,P.EXCISE_REF_DATE, " 
										+" DDF_GET_SUNDRY_DETAILS(S.SUPP_TYPE,S.SUPP_CODE) as CITY,P.SITE_CODE," +
										"P.TOT_AMT FROM TAXTRAN T,TAX T1,PORCP P," 
										+"SUPPLIER S WHERE ( T1.TAX_CODE = T.TAX_CODE ) and  "
										+"( S.SUPP_CODE = P.SUPP_CODE ) and " 
										+"( T.TRAN_ID = P.TRAN_ID ) and ( ( T.TRAN_CODE = ? ) AND  "
										+"( P.SITE_CODE = ? ) AND ( P.TRAN_DATE between ? And ? ) AND "  
										+"( P.SUPP_CODE >= ? And P.SUPP_CODE <= ?  ) AND  "
										+"( T.TAX_CODE   >= ? And T.TAX_CODE <= ?  ) AND ( T1.FORM_REQD  = 'Y' ) AND "  
										+"( T.TAX_CLASS is not null) AND ( T.TAX_FORM is null OR T.TAX_FORM_DATE is null )"  
										+" AND P.TRAN_SER = 'P-RCP' ) GROUP BY T.TRAN_CODE,"   
										+"T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"   
										+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,"   
										+"T.TAX_CHAP,P.TRAN_DATE,P.SUPP_CODE,S.SUPP_NAME,P.EXCISE_REF,"   
										+"P.EXCISE_REF_DATE ,S.SUPP_TYPE,S.SUPP_CODE,P.SITE_CODE, P.TOT_AMT"; 
							
							System.out.println("type:D::tranType:CLASS::refSer:P-RCP---->Executed ");
						}
						else if(refSer.equals("P-RET"))
						{
							getDataSql="SELECT T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"
										+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"
										+"sum( T.TAX_AMT ) as tax_amt,S.SUPP_NAME,P.TRAN_DATE,P.SUPP_CODE,P.EXCISE_REF,P.EXCISE_REF_DATE, "
										+"DDF_GET_SUNDRY_DETAILS(S.SUPP_TYPE,S.SUPP_CODE) as CITY,P.SITE_CODE,P.TOT_AMT FROM TAXTRAN T,TAX T1,PORCP P,SUPPLIER S WHERE ( T1.TAX_CODE = T.TAX_CODE ) and " 
										+"( S.SUPP_CODE = P.SUPP_CODE ) and ( T.TRAN_ID = P.TRAN_ID ) and "  
										+"( ( T.TRAN_CODE = ? ) AND ( P.SITE_CODE = ? ) AND "  
										+"( P.TRAN_DATE between ? And ?   ) AND ( P.SUPP_CODE >= ? And P.SUPP_CODE <= ?  ) AND " 
										+"( T.TAX_CODE   >= ? And T.TAX_CODE <= ?  ) AND ( T1.FORM_REQD  = 'Y' ) AND  "
										+"( T.TAX_CLASS is not null)AND(T.TAX_FORM is null OR  T.TAX_FORM_DATE is null)"
										+" AND P.TRAN_SER = 'P-RET' )GROUP BY T.TRAN_CODE,"
										+" T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"   
										+" T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"   
										+"P.TRAN_DATE,P.SUPP_CODE,S.SUPP_NAME,P.EXCISE_REF,P.EXCISE_REF_DATE,S.SUPP_TYPE,S.SUPP_CODE,P.SITE_CODE, P.TOT_AMT";
							
							System.out.println("type:D::tranType:CLASS::refSer:P-RET---->Executed ");				
						}
						else if( refSer.equals("D-RCP"))
						{        
		                
						getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"
						+"A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"
						+"sum( A.TAX_AMT ) as tax_amt,case when D.sundry_type = 'O' then '0' else  " 
						+"fn_get_sundry(A.TRAN_CODE,C.dist_order,'N') end as  sundry_name,C.TRAN_DATE,"
						+"case when D.sundry_type = 'O' "
						+" then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C') end as sundry_code,C.GP_NO,C.GP_DATE," 
						+" DDF_GET_SUNDRY_DETAILS(D.SUNDRY_TYPE,D.SUNDRY_CODE) as CITY,D.SITE_CODE, D.NET_AMT FROM TAXTRAN A,TAX B,DISTORD_RCP C,DISTORDER D "  
						+" WHERE ( B.TAX_CODE = A.TAX_CODE ) and( A.TRAN_ID = C.TRAN_ID ) and ( C.DIST_ORDER = D.DIST_ORDER ) and " 
						+" ( ( A.TRAN_CODE = ? ) AND( C.site_code = ? ) AND ( C.tran_date between ? And  ? ) AND  "
						+" ( case when D.sundry_type = 'O' then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C') end between " 
						+" ? And ?  ) AND ( A.TAX_CODE   >= ? And A.TAX_CODE <= ?  ) AND ( B.FORM_REQD  = 'Y' ) AND "  
						+" ( A.TAX_CLASS is not null ) AND  ( A.TAX_FORM   is null OR A.TAX_FORM_DATE   is null )  ) "  
						+" GROUP BY A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,A.TAX_CLASS, "  
						+" A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,C.TRAN_DATE,D.SUNDRY_TYPE,C.DIST_ORDER, "   
						+" C.GP_NO,C.GP_DATE,D.SUNDRY_TYPE,D.SUNDRY_CODE,D.SITE_CODE, D.NET_AMT "; 
						
						System.out.println("type:D::tranType:CLASS::refSer:D-RCP---->Executed ");
						}
						else if(refSer.equals("D-ISS"))
						{             
		
							getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"
										+"A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"
										+"sum( A.TAX_AMT ) as tax_amt,case when D.sundry_type = 'O'  then '0' else "
										+"fn_get_sundry(A.TRAN_CODE,C.dist_order,'N')  end as sundry_name,C.TRAN_DATE,"
										+"case when D.sundry_type = 'O' then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C') "
										+"end as sundry_code,C.GP_NO,C.GP_DATE,DDF_GET_SUNDRY_DETAILS(D.SUNDRY_TYPE,D.SUNDRY_CODE) as CITY,D.SITE_CODE, D.NET_AMT " 
										+" FROM TAXTRAN A,TAX B,DISTORD_ISS C,DISTORDER D WHERE ( B.TAX_CODE = A.TAX_CODE ) and " 
										+" ( A.TRAN_ID = C.TRAN_ID ) and  ( C.DIST_ORDER = D.DIST_ORDER ) and  "
										+"( ( A.TRAN_CODE = ? ) AND ( C.site_code = ? ) AND ( C.tran_date between ? And ?   ) AND  "
										+" ( case when D.sundry_type = 'O' then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C')  "
										+" end between ? And ?  ) AND ( A.TAX_CODE   >= ? And A.TAX_CODE <= ?  ) AND ( B.FORM_REQD  = 'Y' ) AND  "
										+"( A.TAX_CLASS is not null ) AND ( A.TAX_FORM   is null OR A.TAX_FORM_DATE   is null )  )  " 
										+" GROUP BY A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,A.TAX_CLASS, "   
										+" A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,C.TRAN_DATE,D.SUNDRY_TYPE,C.DIST_ORDER, "  
										+" C.GP_NO, C.GP_DATE,D.SUNDRY_TYPE,D.SUNDRY_CODE,D.SITE_CODE, D.NET_AMT  ";
						
							System.out.println("type:D::tranType:CLASS::refSer:D-ISS---->Executed ");
						}
						else if(refSer.equals("S-DSP"))  
						{
				
							getDataSql=	"SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE , "
										+"A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,sum( A.TAX_AMT ) as tax_amt, "
										+"D.CUST_NAME,C.DESP_DATE,C.CUST_CODE,C.GP_NO,C.GP_DATE," +
										"DDF_GET_SUNDRY_DETAILS(D.CUST_TYPE,D.CUST_CODE) as CITY,C.SITE_CODE,C.TOT_VALUE as AMOUNT "
										+" FROM TAX B,TAXTRAN A,DESPATCH C,CUSTOMER D "
										+" WHERE ( A.TAX_CODE = B.TAX_CODE ) and ( A.TRAN_ID = C.DESP_ID ) "
										+" and ( C.CUST_CODE = D.CUST_CODE ) and "
										+" ( A.TRAN_CODE = ? ) AND (C.SITE_CODE = ?) AND  "
										+" (C.desp_date between ? AND ? ) AND  "
										+" (C.cust_code >= ?)  AND (C.cust_code<= ? ) AND  "
										+" ( A.TAX_CODE >= ?) AND (A.TAX_CODE <= ? ) AND  "
										//+" ( B.FORM_REQD = 'Y' ) AND ( A.tax_chap is not null ) AND " //Commented by Jaimin 05/09/2007
										+" ( B.FORM_REQD = 'Y' ) AND ( A.TAX_CLASS IS NOT NULL ) AND " //Added by Jaimin 05/09/2007
										+" ( A.TAX_FORM is null OR A.TAX_FORM_DATE is null )  " 
										+" GROUP BY A.TRAN_CODE, A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,"   
										+"A.TAX_FORM_DATE,A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,"   
										+"A.TAX_CODE,A.TAX_CHAP,C.DESP_DATE,C.CUST_CODE,D.CUST_NAME,C.GP_NO,C.GP_DATE,D.CUST_TYPE,D.CUST_CODE,C.SITE_CODE,C.TOT_VALUE";
						System.out.println("type:D::tranType:CLASS::refSer:S-DSP---->Executed ");
						}
						else if(refSer.equals("S-INV"))
						{
					    	getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"
									+"A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"
									+" sum( A.TAX_AMT ) as tax_amt,D.CUST_NAME,C.TRAN_DATE,C.CUST_CODE,'  ',' ' ," +
									"DDF_GET_SUNDRY_DETAILS(D.CUST_TYPE,D.CUST_CODE) as CITY,C.SITE_CODE,C.NET_AMT"  
									+" FROM TAXTRAN A,TAX B,INVOICE C,CUSTOMER D WHERE ( B.TAX_CODE = A.TAX_CODE ) and "  
									+" ( A.TRAN_ID = C.INVOICE_ID ) and ( C.CUST_CODE = D.CUST_CODE ) and "  
									+"( ( A.TRAN_CODE =? ) AND ( C.site_code = ? ) AND "  
									+"( C.tran_date between ? And ? ) AND ( C.cust_code >= ? And C.cust_code <= ? ) AND " 
									+"( A.TAX_CODE >= ? And A.TAX_CODE <= ? ) AND ( B.FORM_REQD = 'Y' ) AND " 
									//+"( A.TAX_CHAP is not null ) AND ( A.TAX_FORM is null OR " //Commented by Jaimin 05/09/2007
									+"( A.TAX_class is not null ) AND ( A.TAX_FORM is null OR " //Added by Jaimin 05/09/2007
									+" A.TAX_FORM_DATE is null ) ) "  
									+" GROUP BY A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"   +" A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"   
									+" C.TRAN_DATE,C.CUST_CODE,D.CUST_NAME,D.CUST_TYPE,D.CUST_CODE,C.SITE_CODE, C.NET_AMT ";
					    System.out.println("type:D::tranType:CLASS::refSer:S-INV---->Executed ");			
						}
						else if (refSer.equals("VOUCH"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.LINE_NO,TAXTRAN.LINE_NO__TAX,"
							+"TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_PERC,TAXTRAN.TAX_ENV"
							+",TAXTRAN.TAXABLE_AMT,TAXTRAN.TAX_CODE,TAXTRAN.TAX_CHAP,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,"
							+" SUPPLIER.SUPP_NAME,VOUCHER.TRAN_DATE,"
							+" VOUCHER.SUPP_CODE,VOUCHER.BILL_NO,VOUCHER.BILL_DATE," +
							"DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,VOUCHER.SITE_CODE,VOUCHER.NET_AMT"
							+" FROM TAXTRAN,TAX,SUPPLIER,VOUCHER WHERE "
							+"( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND ( VOUCHER.SUPP_CODE = SUPPLIER.SUPP_CODE ) AND "
							+" ( TAXTRAN.TRAN_ID = VOUCHER.TRAN_ID ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND ( VOUCHER.SITE_CODE = ? )"
							+" AND ( VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND ( VOUCHER.SUPP_CODE BETWEEN ? AND ? )"
							+" AND ( TAXTRAN.TAX_CODE   BETWEEN ? AND ? ) AND ( TAX.FORM_REQD  = 'Y' ) AND "
							+" ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR TAXTRAN.TAX_FORM_DATE IS NULL ))"
							+" GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,TAXTRAN.LINE_NO,TAXTRAN.LINE_NO__TAX,"
							+"TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_PERC,TAXTRAN.TAX_ENV,"
							+"TAXTRAN.TAXABLE_AMT,TAXTRAN.TAX_CODE,TAXTRAN.TAX_CHAP, SUPPLIER.SUPP_NAME,VOUCHER.TRAN_DATE,"
							+"VOUCHER.SUPP_CODE,VOUCHER.BILL_NO,VOUCHER.BILL_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,VOUCHER.SITE_CODE, VOUCHER.NET_AMT";   
						System.out.println("type:D::tranType:CLASS::refSer:VOUCH---->Executed ");
						}
						else if (refSer.equals("M-VOUC"))
						{   
							getDataSql = "SELECT taxtran.tran_code,taxtran.tran_id,taxtran.line_no,taxtran.line_no__tax,taxtran.tax_form,"
							+" taxtran.tax_form_date,taxtran.tax_class,taxtran.tax_perc,taxtran.tax_env,taxtran.taxable_amt,taxtran.tax_code,"
							+" taxtran.tax_chap,sum(TAXTRAN.TAX_AMT) as tax_amt,FN_SUNDRY_NAME(MISC_VOUCHER.SUNDRY_TYPE,"
							+" MISC_VOUCHER.SUNDRY_CODE,' ') as sundry_name,misc_voucher.tran_date,misc_voucher.sundry_code,"
							+" misc_voucher.bill_no,misc_voucher.bill_date,DDF_GET_SUNDRY_DETAILS(misc_voucher.SUNDRY_TYPE,misc_voucher.SUNDRY_CODE) as CITY,misc_voucher.SITE_CODE,misc_voucher.NET_AMT"
							+" FROM taxtran,tax,misc_voucher"
							+" WHERE ( tax.tax_code = taxtran.tax_code ) and ( taxtran.tran_id = misc_voucher.tran_id ) and "
							+" ( ( TAXTRAN.TRAN_CODE = ? ) AND ( misc_voucher.site_code = ? ) AND "
							+" ( misc_voucher.tran_date between ? And ? ) AND "
							+" ( misc_voucher.sundry_code between ? And ? ) AND "
							+" ( TAXTRAN.TAX_CODE   between ? And ? ) AND ( TAX.FORM_REQD  = 'Y' ) AND "
							+" ( TAXTRAN.TAX_CLASS is not null ) AND ( TAXTRAN.TAX_FORM is null OR TAXTRAN.TAX_FORM_DATE is null ))"
							+" AND (misc_voucher.sundry_type = ?) " 
							+" GROUP BY taxtran.tran_code,taxtran.tran_id,taxtran.line_no,taxtran.line_no__tax,taxtran.tax_form,"
							+" taxtran.tax_form_date,taxtran.tax_class,taxtran.tax_perc,taxtran.tax_env,taxtran.taxable_amt,taxtran.tax_code,"
							+" taxtran.tax_chap,misc_voucher.tran_date,misc_voucher.sundry_code,misc_voucher.bill_no,misc_voucher.bill_date"
							+" ,misc_voucher.sundry_type,misc_voucher.SUNDRY_CODE,misc_voucher.SITE_CODE,misc_voucher.NET_AMT";
						System.out.println("type:D::tranType:CLASS::refSer:M-VOUC---->Executed ");
						}
					}//end of ref ser
				}//END OF CLASS
				else if(tranType.equalsIgnoreCase("CHAP"))
				{
					if(refSer!=null)
					{
						if( refSer.equals("S-RET"))
						{
							getDataSql="SELECT T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"
									+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"
									+"sum( T.TAX_AMT ) as tax_amt,C.CUST_NAME,S.TRAN_DATE,S.CUST_CODE,"
									+"S.EXCISE_REF_NO,S.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(C.CUST_TYPE,C.CUST_CODE) as CITY,S.SITE_CODE,S.NET_AMT FROM TAXTRAN T,"  
									+ "TAX T1,CUSTOMER C,SRETURN S "
									+ "WHERE ( T1.TAX_CODE = T.TAX_CODE ) and ( C.CUST_CODE = S.CUST_CODE ) "
									+"and ( T.TRAN_ID = S.TRAN_ID ) and ( ( T.TRAN_CODE = ? ) "
									+"AND ( S.SITE_CODE = ? ) AND " 
									+"( S.TRAN_DATE between ? And ?   ) AND " 
									+"( S.CUST_CODE >= ? And S.CUST_CODE <= ?  ) AND " 
									+"( T.TAX_CODE   >= ? And T.TAX_CODE <= ?  ) AND " 
									//+"( T1.FORM_REQD  = 'Y' ) AND ( T.TAX_CLASS is not null ) AND " //Commented by Jaimin 05/09/2007
									+"( T1.FORM_REQD  = 'Y' ) AND ( T.TAX_CHAP is not null ) AND " //Added by Jaimin 05/09/2007
									+"( T.TAX_FORM   is null OR T.TAX_FORM_DATE   is null )  ) "  
									+"GROUP BY T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,"   
									+"T.TAX_FORM_DATE,T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,"
									+"T.TAX_CODE,T.TAX_CHAP,S.TRAN_DATE,S.CUST_CODE,C.CUST_NAME,"  
									+"S.EXCISE_REF_NO,S.EXCISE_REF_DATE,C.CUST_TYPE,C.CUST_CODE,S.SITE_CODE, S.NET_AMT";
							
							System.out.println("type:D::tranType:CHAP::refSer:S-RET---->Executed ");
							}
							else if( refSer.equals("P-RCP"))
							{
								getDataSql="SELECT T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"
									+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"
									+" sum( T.TAX_AMT ) as tax_amt,S.SUPP_NAME,P.TRAN_DATE,P.SUPP_CODE,P.EXCISE_REF,P.EXCISE_REF_DATE, " 
									+" DDF_GET_SUNDRY_DETAILS(S.SUPP_TYPE,S.SUPP_CODE) as CITY,P.SITE_CODE,P.TOT_AMT  FROM TAXTRAN T,TAX T1,PORCP P," 
									+"SUPPLIER S WHERE ( T1.TAX_CODE = T.TAX_CODE ) and  "
									+"( S.SUPP_CODE = P.SUPP_CODE ) and " 
									+"( T.TRAN_ID = P.TRAN_ID ) and ( ( T.TRAN_CODE = ? ) AND  "
									+"( P.SITE_CODE = ? ) AND ( P.TRAN_DATE between ? And ? ) AND "  
									+"( P.SUPP_CODE >= ? And P.SUPP_CODE <= ?  ) AND  "
									+"( T.TAX_CODE   >= ? And T.TAX_CODE <= ?  ) AND ( T1.FORM_REQD  = 'Y' ) AND "  
									//+"( T.TAX_CLASS is not null) AND ( T.TAX_FORM is null OR T.TAX_FORM_DATE is null )"  //Commented by Jaimin 05/09/2007
									+"( T.TAX_CHAP is not null) AND ( T.TAX_FORM is null OR T.TAX_FORM_DATE is null )"  //Added by Jaimin 05/09/2007
									+" AND P.TRAN_SER = 'P-RCP' ) GROUP BY T.TRAN_CODE,"   
									+"T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"   
									+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,"   
									+"T.TAX_CHAP,P.TRAN_DATE,P.SUPP_CODE,S.SUPP_NAME,P.EXCISE_REF,"   
									+"P.EXCISE_REF_DATE,S.SUPP_TYPE,S.SUPP_CODE,P.SITE_CODE, P.TOT_AMT"; 
							
							System.out.println("type:D::tranType:CHAP::refSer:P-RCP---->Executed ");
							}
							else if(refSer.equals("P-RET"))
							{
								getDataSql="SELECT T.TRAN_CODE,T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"
										+"T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"
										+"sum( T.TAX_AMT ) as tax_amt,S.SUPP_NAME,P.TRAN_DATE,P.SUPP_CODE,P.EXCISE_REF,P.EXCISE_REF_DATE, "
										+"DDF_GET_SUNDRY_DETAILS(S.SUPP_TYPE,S.SUPP_CODE) as CITY,P.SITE_CODE,P.TOT_AMT FROM TAXTRAN T,TAX T1,PORCP P,SUPPLIER S WHERE ( T1.TAX_CODE = T.TAX_CODE ) and " 
										+"( S.SUPP_CODE = P.SUPP_CODE ) and ( T.TRAN_ID = P.TRAN_ID ) and "  
										+"( ( T.TRAN_CODE = ? ) AND ( P.SITE_CODE = ? ) AND "  
										+"( P.TRAN_DATE between ? And ?   ) AND ( P.SUPP_CODE >= ? And P.SUPP_CODE <= ?  ) AND " 
										+"( T.TAX_CODE   >= ? And T.TAX_CODE <= ?  ) AND ( T1.FORM_REQD  = 'Y' ) AND  "
										//+"( T.TAX_CLASS is not null)AND(T.TAX_FORM is null OR  T.TAX_FORM_DATE is null)" //Commented by Jaimin 05/09/2007
										+"( T.TAX_CHAP is not null)AND(T.TAX_FORM is null OR  T.TAX_FORM_DATE is null)" //Added by Jaimin 05/09/2007
										+" AND P.TRAN_SER = 'P-RET' )GROUP BY T.TRAN_CODE,"
										+" T.TRAN_ID,T.LINE_NO,T.LINE_NO__TAX,T.TAX_FORM,T.TAX_FORM_DATE,"   
										+" T.TAX_CLASS,T.TAX_PERC,T.TAX_ENV,T.TAXABLE_AMT,T.TAX_CODE,T.TAX_CHAP,"   
										+"P.TRAN_DATE,P.SUPP_CODE,S.SUPP_NAME,P.EXCISE_REF,P.EXCISE_REF_DATE,S.SUPP_TYPE,S.SUPP_CODE,P.SITE_CODE, P.TOT_AMT";
							
							System.out.println("type:D::tranType:CHAP::refSer:P-RET---->Executed ");	
							}
							
							else if( refSer.equals("D-RCP"))
							{
						
								getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE," +"A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"
									+" sum( A.TAX_AMT ) as tax_amt,case when D.sundry_type = 'O' then '0' else  "  +"fn_get_sundry(A.TRAN_CODE,C.dist_order,'N') end as  sundry_name,C.TRAN_DATE,"
									+" case when D.sundry_type = 'O' "
									+" then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C') end as sundry_code,C.GP_NO,C.GP_DATE," 
									+" DDF_GET_SUNDRY_DETAILS(D.SUNDRY_TYPE,D.SUNDRY_CODE) as CITY,D.SITE_CODE, D.NET_AMT,A.TRAN_CODE FROM TAXTRAN A,TAX B,DISTORD_RCP C,DISTORDER D "  
									+" WHERE ( B.TAX_CODE = A.TAX_CODE ) and( A.TRAN_ID = C.TRAN_ID ) and ( C.DIST_ORDER = D.DIST_ORDER ) and " 
									+" ( ( A.TRAN_CODE = ? ) AND( C.site_code = ? ) AND ( C.tran_date between ? And  ? ) AND  "
									+" ( case when D.sundry_type = 'O' then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C') end between " 
									+" ? And ?  ) AND ( A.TAX_CODE   >= ? And A.TAX_CODE <= ?  ) AND ( B.FORM_REQD  = 'Y' ) AND "  
									//+" ( A.TAX_CLASS is not null ) AND  ( A.TAX_FORM   is null OR A.TAX_FORM_DATE   is null )  ) "  //Commented by Jaimin 05/09/2007
									+" ( A.TAX_CHAP is not null ) AND  ( A.TAX_FORM   is null OR A.TAX_FORM_DATE   is null )  ) " //Added by Jaimin 05/09/2007 
									+" GROUP BY A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,A.TAX_CLASS, "  
									+" A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,C.TRAN_DATE,D.SUNDRY_TYPE,C.DIST_ORDER, "   
									+" C.GP_NO,C.GP_DATE,D.SUNDRY_TYPE,D.SUNDRY_CODE,D.SITE_CODE, D.NET_AMT ";
								
							System.out.println("type:D::tranType:CHAP::refSer:D-RCP---->Executed ");
							}
							else if( refSer.equals("D-ISS"))
							{
								
								getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"
									+" A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"
									+" sum( A.TAX_AMT ) as tax_amt,case when D.sundry_type = 'O'  then '0' else " +"fn_get_sundry(A.TRAN_CODE,C.dist_order,'N')  end as sundry_name,C.TRAN_DATE,"
									+" case when D.sundry_type = 'O' then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C') "
									+" end as sundry_code,C.GP_NO,C.GP_DATE,DDF_GET_SUNDRY_DETAILS(D.SUNDRY_TYPE,D.SUNDRY_CODE) as CITY,D.SITE_CODE, D.NET_AMT " 
									+" FROM TAXTRAN A,TAX B,DISTORD_ISS C,DISTORDER D WHERE ( B.TAX_CODE = A.TAX_CODE ) and " 
									+" ( A.TRAN_ID = C.TRAN_ID ) and  ( C.DIST_ORDER = D.DIST_ORDER ) and  "
									+"( ( A.TRAN_CODE = ? ) AND ( C.site_code = ? ) AND ( C.tran_date between ? And ?   ) AND  "
									+" ( case when D.sundry_type = 'O' then '0' else fn_get_sundry(A.TRAN_CODE,C.dist_order,'C')  "
									+" end between ? And ?  ) AND ( A.TAX_CODE   >= ? And A.TAX_CODE <= ?  ) AND ( B.FORM_REQD  = 'Y' ) AND  "
									//+"( A.TAX_CLASS is not null ) AND ( A.TAX_FORM   is null OR A.TAX_FORM_DATE   is null )  )  " //Commented by Jaimin 05/09/2007
									+"( A.TAX_CHAP is not null ) AND ( A.TAX_FORM   is null OR A.TAX_FORM_DATE   is null )  )  " //Added by Jaimin 05/09/2007
									+" GROUP BY A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,A.TAX_CLASS, "   
									+" A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,C.TRAN_DATE,D.SUNDRY_TYPE,C.DIST_ORDER, "  
									+" C.GP_NO, C.GP_DATE,D.SUNDRY_TYPE,D.SUNDRY_CODE,D.SITE_CODE, D.NET_AMT  ";
							
								System.out.println("type:D::tranType:CHAP::refSer:D-ISS---->Executed ");
							}
							else  if( refSer.equals("S-DSP"))
							{
								
							      getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"
										+" A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,sum( A.TAX_AMT ) as tax_amt,"
										+ " D.CUST_NAME,C.DESP_DATE,C.CUST_CODE,C.GP_NO,C.GP_DATE,DDF_GET_SUNDRY_DETAILS(D.CUST_TYPE,D.CUST_CODE) as CITY,C.SITE_CODE,C.TOT_VALUE as AMOUNT "
										+" FROM TAX B,TAXTRAN A,DESPATCH C,CUSTOMER D "
										+" WHERE ( A.TAX_CODE = B.TAX_CODE ) and ( A.TRAN_ID = C.DESP_ID ) "
										+" and ( C.CUST_CODE = D.CUST_CODE ) and "
										+" ( ( A.TRAN_CODE = ? ) AND (C.site_code = ?) AND  "
										+" (C.desp_date between ? And ? ) AND  "
										+" (C.cust_code >= ? And C.cust_code <= ? ) AND  "
										+" ( A.TAX_CODE >= ? And A.TAX_CODE <= ? ) AND  "
										+" ( B.FORM_REQD = 'Y' ) AND ( A.tax_chap is not null ) AND " 
										+" ( A.TAX_FORM is null OR A.TAX_FORM_DATE is null ) ) " 
										+" GROUP BY A.TRAN_CODE, A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,"   
										+"A.TAX_FORM_DATE,A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,"   
										+"A.TAX_CODE,A.TAX_CHAP,C.DESP_DATE,C.CUST_CODE,D.CUST_NAME,C.GP_NO,C.GP_DATE,D.CUST_TYPE,D.CUST_CODE,C.SITE_CODE,C.TOT_VALUE"; 
													
						   System.out.println("type:D::tranType:CHAP::refSer:S-DSP---->Executed ");
							}
							else  if( refSer.equals("S-INV"))
							{
							
								getDataSql="SELECT A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"
										+" A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"
										+" sum( A.TAX_AMT ) as tax_amt,D.CUST_NAME,C.TRAN_DATE,C.CUST_CODE,'  ',' '," +
										"DDF_GET_SUNDRY_DETAILS(D.CUST_TYPE,D.CUST_CODE) as CITY,C.SITE_CODE,C.NET_AMT "  
										+" FROM TAXTRAN A,TAX B,INVOICE C,CUSTOMER D WHERE ( B.TAX_CODE = A.TAX_CODE ) and "  
										+" ( A.TRAN_ID = C.INVOICE_ID ) and ( C.CUST_CODE = D.CUST_CODE ) and "  
										+"( ( A.TRAN_CODE =? ) AND ( C.site_code = ? ) AND "  
										+"( C.tran_date between ? And ? ) AND ( C.cust_code >= ? And C.cust_code <= ? ) AND " 
										+"( A.TAX_CODE >= ? And A.TAX_CODE <= ? ) AND ( B.FORM_REQD = 'Y' ) AND " 
										+"( A.TAX_CHAP is not null ) AND ( A.TAX_FORM is null OR " 
										+" A.TAX_FORM_DATE is null ) ) "  
										+" GROUP BY A.TRAN_CODE,A.TRAN_ID,A.LINE_NO,A.LINE_NO__TAX,A.TAX_FORM,A.TAX_FORM_DATE,"   
										+" A.TAX_CLASS,A.TAX_PERC,A.TAX_ENV,A.TAXABLE_AMT,A.TAX_CODE,A.TAX_CHAP,"   
										+" C.TRAN_DATE,C.CUST_CODE,D.CUST_NAME,D.CUST_TYPE,D.CUST_CODE,C.SITE_CODE, C.NET_AMT  ";								
							
							System.out.println("type:D::tranType:CHAP::refSer:S-INV---->Executed ");
							}
							else if (refSer.equals("VOUCH"))
							{
								getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.LINE_NO,TAXTRAN.LINE_NO__TAX,"
								+"TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_PERC,"
								+"TAXTRAN.TAX_ENV,TAXTRAN.TAXABLE_AMT,TAXTRAN.TAX_CODE,"
								+"TAXTRAN.TAX_CHAP,SUM( TAXTRAN.TAX_AMT) AS TAX_AMT,"
								+"SUPPLIER.SUPP_NAME,VOUCHER.TRAN_DATE,VOUCHER.SUPP_CODE,VOUCHER.BILL_NO,VOUCHER.BILL_DATE," +
								"DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,VOUCHER.SITE_CODE,VOUCHER.NET_AMT "
								+" FROM TAX,TAXTRAN,SUPPLIER,VOUCHER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND "
								+"( VOUCHER.SUPP_CODE = SUPPLIER.SUPP_CODE ) AND ( TAXTRAN.TRAN_ID = VOUCHER.TRAN_ID )"
								+"AND ( ( TAXTRAN.TRAN_CODE = ? ) AND (VOUCHER.SITE_CODE = ? ) AND "
								+" (VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND (VOUCHER.SUPP_CODE BETWEEN ? AND ? ) AND "
								+" ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND ( TAX.FORM_REQD = 'Y' ) AND "
								+" ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR "
								+" TAXTRAN.TAX_FORM_DATE IS NULL )) GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,"
								+" TAXTRAN.LINE_NO,TAXTRAN.LINE_NO__TAX,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,"
								+"TAXTRAN.TAX_CLASS,TAXTRAN.TAX_PERC,TAXTRAN.TAX_ENV,TAXTRAN.TAXABLE_AMT,TAXTRAN.TAX_CODE,"
								+" TAXTRAN.TAX_CHAP,SUPPLIER.SUPP_NAME,VOUCHER.TRAN_DATE,VOUCHER.SUPP_CODE,"
								+" VOUCHER.BILL_NO,VOUCHER.BILL_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,VOUCHER.SITE_CODE, VOUCHER.NET_AMT ";  
							
							System.out.println("type:D::tranType:CHAP::refSer:VOUCH---->Executed ");
							}
							else if (refSer.equals("M-VOUC"))
							{ 
									getDataSql =" SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.LINE_NO,TAXTRAN.LINE_NO__TAX,"
									+"TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_PERC,"
									+"TAXTRAN.TAX_ENV,TAXTRAN.TAXABLE_AMT,TAXTRAN.TAX_CODE,TAXTRAN.TAX_CHAP,"
									+" SUM(TAXTRAN.TAX_AMT) AS TAX_AMT,"
									+" FN_SUNDRY_NAME(MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE,' ') AS SUNDRY_NAME,"
									+"MISC_VOUCHER.TRAN_DATE,MISC_VOUCHER.SUNDRY_CODE,"
									+"MISC_VOUCHER.BILL_NO,MISC_VOUCHER.BILL_DATE,DDF_GET_SUNDRY_DETAILS(MISC_VOUCHER.SUNDRY_TYPE," +
									"MISC_VOUCHER.SUNDRY_CODE) as CITY,MISC_VOUCHER.SITE_CODE,MISC_VOUCHER.NET_AMT"
									+"  FROM TAX,TAXTRAN,MISC_VOUCHER WHERE"
									+" ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND ( TAXTRAN.TRAN_ID = MISC_VOUCHER.TRAN_ID ) AND"
									+"( ( TAXTRAN.TRAN_CODE = ? ) AND (MISC_VOUCHER.SITE_CODE = ? ) AND "
									+"(MISC_VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND (MISC_VOUCHER.SUNDRY_CODE "
									+" BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND"
									+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR "
									+" TAXTRAN.TAX_FORM_DATE IS NULL ) ) AND (MISC_VOUCHER.SUNDRY_TYPE = ?) GROUP BY"
									+" TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.LINE_NO,"
									+" TAXTRAN.LINE_NO__TAX,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
									+" TAXTRAN.TAX_PERC,TAXTRAN.TAX_ENV,TAXTRAN.TAXABLE_AMT,TAXTRAN.TAX_CODE,"
									+" TAXTRAN.TAX_CHAP,MISC_VOUCHER.TRAN_DATE, MISC_VOUCHER.SUNDRY_CODE,"
									+" MISC_VOUCHER.BILL_NO,MISC_VOUCHER.BILL_DATE"
									+",MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE,MISC_VOUCHER.SITE_CODE, MISC_VOUCHER.NET_AMT";
							
							System.out.println("type:D::tranType:CHAP::refSer:M-VOUC---->Executed ");
							}
							//end of inner else if
						} //NOT NULL
					}//END OF TAX CHAPTER
					
			}//end Of tran type 
			System.out.println("Sql Fired :::::"+getDataSql.trim().length());
			if (getDataSql.trim().length()>0) //Case added by Jaimin 23/08/2007
			{
				System.out.println("Sql Fired :::::"+getDataSql);					
				pstmt = conn.prepareStatement(getDataSql);
				pstmt.setString(1,refSerWs);
				pstmt.setString(2,siteCodeWs);
				pstmt.setTimestamp(3,chgdateFrom); 
				pstmt.setTimestamp(4,chgdateTo);					
				pstmt.setString(5,sundCodeFromWs);
				pstmt.setString(6,sundCodeToWs);
				pstmt.setString(7,taxCodeFromWs);
				pstmt.setString(8,taxCodeToWs);
				if (refSer.equals("M-VOUC")) //Condition added by Jaimin 06/09/2007
					pstmt.setString(9,sundryType);

				System.out.println("QUERY IS IN PROCESS..........");
				rs1 = pstmt.executeQuery();
				System.out.println("QUERY  PROCESS FINISED.......");
				while(rs1.next()) 
				{
						count++;
						retTabSepStrBuff.append("<Detail2>");
						//TRAN_CODE
						//retTabSepStrBuff.append((rs1.getString(1)==null?" ":rs1.getString(1))).append("\t");	
						retTabSepStrBuff.append("<tran_code>").append("<![CDATA[" +(rs1.getString("TRAN_CODE")) +"]]>").append("</tran_code>");
						//TRAN_ID
						//retTabSepStrBuff.append((rs1.getString(2)==null?" ":rs1.getString(2))).append("\t");
						retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						//LINE_NO
						//retTabSepStrBuff.append((rs1.getString(3)==null?" ":rs1.getString(3))).append("\t");
						retTabSepStrBuff.append("<line_no>").append("<![CDATA[" +(rs1.getString("LINE_NO")) +"]]>").append("</line_no>");
						//TRANDATE
						/*
						if(rs1.getTimestamp(15)!=null)
						{
							retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(15).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
							retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						}
						else
						{
							retTabSepStrBuff.append(" ").append("\t");
							retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						}
						*/
						if(refSer.equalsIgnoreCase("S-DSP"))  // DESP_DATE
						{
							if(rs1.getTimestamp("DESP_DATE")!=null)
				            {
								retTabSepStrBuff.append("<tran_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("DESP_DATE").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</tran_date>");
				            }
							else
							{
								retTabSepStrBuff.append("<tran_date>").append(" ").append("</tran_date>");
							}
						}
						else
						{
						if(rs1.getTimestamp("tran_date")!=null)
			            {
							retTabSepStrBuff.append("<tran_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("tran_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</tran_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<tran_date>").append(" ").append("</tran_date>");
						}
						}
						//EXCISE_REF_NO	  ////EXCISE_REF_DATE
					if( refSer.equals("S-RET"))
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("excise_ref_no")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("excise_ref_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("excise_ref_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}	
					else if ( refSer.equals("P-RCP") || refSer.equals("P-RET") )
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("excise_ref")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("excise_ref_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("excise_ref_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}
					else if( refSer.equals("D-RCP") || refSer.equals("D-ISS") || refSer.equals("S-DSP") )
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("gp_no")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("gp_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("gp_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}
					else if ( refSer.equals("S-INV") )
					{

						retTabSepStrBuff.append("<excise_ref_no>").append(" ").append("</excise_ref_no>");
						retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
					
						
					}
					else if( refSer.equals("VOUCH") || refSer.equals("M-VOUCH") )
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("bill_no")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("bill_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("bill_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}
						
						
						
						
					
						//sundry_code  // //sundry_name
					if( refSer.equals("S-RET") ||  refSer.equals("S-DSP") ||  refSer.equals("S-INV"))
					{
						retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("cust_code")) +"]]>").append("</sundry_code>");
						retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("cust_name")) +"]]>").append("</sundry_name>");
					}
					else if( refSer.equals("P-RCP") ||  refSer.equals("P-RET") ||  refSer.equals("VOUCH"))
					{
						retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("supp_code")) +"]]>").append("</sundry_code>");
						retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("supp_name")) +"]]>").append("</sundry_name>");
					}
					else if( refSer.equals("D-RCP") ||  refSer.equals("D-ISS") ||  refSer.equals("M-VOUCH"))
					{
						retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("sundry_code")) +"]]>").append("</sundry_code>");
						retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("sundry_name")) +"]]>").append("</sundry_name>");
					}
					
					//retTabSepStrBuff.append((rs1.getString(14)==null?" ":rs1.getString(14))).append("\t");
						//retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("sundry_code")) +"]]>").append("</sundry_code>");
						//sundry_name
						//retTabSepStrBuff.append((rs1.getString(16)==null?" ":rs1.getString(16))).append("\t");
						//retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("sundry_name")) +"]]>").append("</sundry_name>");
						
						//added by chandrakant patil start 29 March
						//CITY
						
						//
						//if( (refSer.equals("D-RCP")) || (refSer.equals("D-ISS")) || (refSer.equals("M-VOUC")) || (refSer.equals("D-RCP")) || (refSer.equals("D-ISS")) || (refSer.equals("M-VOUC")))
						//{
						  String str=rs1.getString("CITY")==null?" ":rs1.getString("CITY");
						    if (str.equalsIgnoreCase(null) || str.equalsIgnoreCase(" ") || str.equalsIgnoreCase(""))
						    {
						    	// retTabSepStrBuff.append(" ").append("\t");
						    	retTabSepStrBuff.append("<city>").append(" ").append("</city>");
						    }
						    else
						    {
						    	String strArr[] = str.split("~n");
							  //  retTabSepStrBuff.append((strArr[4]==null?" ":strArr[4])).append("\t");
						    	System.out.println("@@@@ city ["+strArr[4]==null?" ":strArr[4]+"]");
							    retTabSepStrBuff.append("<city>").append("<![CDATA[" +(strArr[4]==null?" ":strArr[4]) +"]]>").append("</city>");
						    }	
						/* }
						else
						{	
						retTabSepStrBuff.append((rs1.getString(19)==null?" ":rs1.getString(19))).append("\t");						
						}
						*/
						//SITE CODE
						//retTabSepStrBuff.append((rs1.getString(20)==null?" ":rs1.getString(20))).append("\t");
						retTabSepStrBuff.append("<site_code>").append("<![CDATA[" +(rs1.getString("site_code")) +"]]>").append("</site_code>");
						//NET AMOUNT
						//retTabSepStrBuff.append((rs1.getString(21)==null?" ":rs1.getString(21))).append("\t");
						
						
						//retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +(rs1.getString("NET_AMT")) +"]]>").append("</net_amount>");
						//chandrakant Finish
						if( refSer.equals("S-RET") ||  refSer.equals("D-RCP") ||  refSer.equals("D-ISS") ||  refSer.equals("S-INV") ||  refSer.equals("VOUCH") ||  refSer.equals("M-VOUCH") )
						{
							retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +checkNull(rs1.getString("NET_AMT")) +"]]>").append("</net_amount>");
						}
						else if( refSer.equals("P-RCP") ||  refSer.equals("P-RET") )
						{
							retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +checkNull(rs1.getString("TOT_AMT")) +"]]>").append("</net_amount>");
						}
						else if( refSer.equals("S-DSP") )
						{
							retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +checkNull(rs1.getString("amount")) +"]]>").append("</net_amount>");
						}
						
						//TAX_FORM
						//retTabSepStrBuff.append((rs1.getString(5)==null?" ":rs1.getString(5))).append("\t");
						retTabSepStrBuff.append("<tax_form>").append("<![CDATA[" +checkNull(rs1.getString("tax_form")) +"]]>").append("</tax_form>");
						
						//TAX_FORM_DATE
						/*
						if(rs1.getTimestamp(6)!=null)
						{
							retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(6).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
							retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						}
						else
						{
								retTabSepStrBuff.append(" ").append("\t");
								retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						}
						*/
						if(rs1.getTimestamp("tax_form_date")!=null )
			            {
							retTabSepStrBuff.append("<tax_form_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("tax_form_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</tax_form_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<tax_form_date>").append(" ").append("</tax_form_date>");
						}

						
						/*
						
						
						//TAX_FORM
						retTabSepStrBuff.append((rs1.getString(5)==null?" ":rs1.getString(5))).append("\t");
						retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						//TAX_FORM_DATE
						if(rs1.getTimestamp(6)!=null)
						{
							retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(6).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
							retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						}
						else
						{
								retTabSepStrBuff.append(" ").append("\t");
								retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
						}
						
					
						
						*/
						//TRAN_CODE  //tax_code
						//retTabSepStrBuff.append((rs1.getString(11)==null?" ":rs1.getString(11))).append("\t");
						retTabSepStrBuff.append("<tax_code>").append("<![CDATA[" +(rs1.getString("tax_code")) +"]]>").append("</tax_code>");
						
						//LINE_NO__TAX
						//retTabSepStrBuff.append((rs1.getString(4)==null?" ":rs1.getString(4))).append("\t");
						retTabSepStrBuff.append("<line_no__tax>").append("<![CDATA[" +(rs1.getString("line_no__tax")) +"]]>").append("</line_no__tax>");
						
						//taxable_amt
						//TAXABLE_AMT
						//retTabSepStrBuff.append((rs1.getString(10)==null?" ":rs1.getString(10))).append("\t");
						retTabSepStrBuff.append("<taxable_amt>").append("<![CDATA[" +(rs1.getString("taxable_amt")) +"]]>").append("</taxable_amt>");
						
						//TAX_AMT
						//retTabSepStrBuff.append((rs1.getString(13)==null?" ":rs1.getString(13))).append("\t");
						retTabSepStrBuff.append("<tax_amt>").append("<![CDATA[" +(rs1.getString("tax_amt")) +"]]>").append("</tax_amt>");
						
						//TAX_PERC
						//retTabSepStrBuff.append((rs1.getString(8)==null?" ":rs1.getString(8))).append("\t");
						retTabSepStrBuff.append("<tax_perc>").append("<![CDATA[" +(rs1.getString("tax_perc")) +"]]>").append("</tax_perc>");
						
						
						//TAX_CLASS
						//retTabSepStrBuff.append((rs1.getString(7)==null?" ":rs1.getString(7))).append("\t");
						retTabSepStrBuff.append("<tax_class>").append("<![CDATA[" +(rs1.getString("tax_class")) +"]]>").append("</tax_class>");
						//TRAN_CHAP
						//retTabSepStrBuff.append((rs1.getString(12)==null?" ":rs1.getString(12))).append("\t");
						retTabSepStrBuff.append("<tax_chap>").append("<![CDATA[" +(rs1.getString("tax_chap")) +"]]>").append("</tax_chap>");
						//TAX_ENV
						//retTabSepStrBuff.append((rs1.getString(9)==null?" ":rs1.getString(9))).append("\t");
						retTabSepStrBuff.append("<tax_env>").append("<![CDATA[" +(rs1.getString("tax_env")) +"]]>").append("</tax_env>");
						
						
						retTabSepStrBuff.append("</Detail2>");

						
						//retTabSepStrBuff.append("\n");				
						//count++;
						//System.out.println("#####Counter"+count);
				}//END WHILE
				rs1.close();
				pstmt.close();
				rs1=null;
				pstmt=null;
				if (count == 0 )//Condition added by Jaimin 23/08/07 for desplaying the message of "process completed" requst-ID :DI78DIS028
				{
					errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
				}
			}
			else//added by jaimin 23/08/07 requst-ID :DI78DIS028
			{
				System.out.println("Sql:::::is Null ::::::::::::::");
				errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
			}//end by jaimin 23/08/07 requst-ID :DI78DIS028

		}//endType
		else if (type.equalsIgnoreCase("S")) //Case added by Jaimin 31/08/2007 for summary requst-ID :DI78DIS028
		{
			System.out.println("::::For Summary:::::");
			if(tranType!=null )
			{
				if(tranType.equalsIgnoreCase("CLASS"))
				{
					if(refSer!=null)
					{
						if(refSer.equals("S-RET"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,SRETURN.TRAN_DATE,SRETURN.CUST_CODE,CUSTOMER.CUST_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,"
							+"SRETURN.EXCISE_REF_NO,SRETURN.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE) as CITY," +
							"SRETURN.SITE_CODE,SRETURN.NET_AMT FROM TAXTRAN,TAX,CUSTOMER,SRETURN WHERE "
							+"( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND  ( CUSTOMER.CUST_CODE = SRETURN.CUST_CODE ) AND "
							+"( TAXTRAN.TRAN_ID = SRETURN.TRAN_ID ) AND  ( ( TAXTRAN.TRAN_CODE = ? ) AND  ( SRETURN.SITE_CODE = ? ) AND"
							+" ( SRETURN.TRAN_DATE BETWEEN ? AND ?   ) AND ( SRETURN.CUST_CODE BETWEEN ? AND ?  ) AND "
							+"( TAXTRAN.TAX_CODE   BETWEEN ? AND ?  ) AND  ( TAX.FORM_REQD  = 'Y' ) AND ( TAXTRAN.TAX_CLASS IS NOT NULL )"
							+" AND  ( TAXTRAN.TAX_FORM   IS NULL OR  TAXTRAN.TAX_FORM_DATE   IS NULL )  )"
							+"GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,"
							+"SRETURN.TRAN_DATE,SRETURN.CUST_CODE,CUSTOMER.CUST_NAME,SRETURN.EXCISE_REF_NO,SRETURN.EXCISE_REF_DATE,CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE,SRETURN.SITE_CODE, SRETURN.NET_AMT";
						
						System.out.println("type:S::tranType:CLASS::refSer:S-RET---->Executed ");
						}
						else if (refSer.equals("P-RCP"))
						{
							getDataSql = " SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV, PORCP.TRAN_DATE,PORCP.SUPP_CODE,SUPPLIER.SUPP_NAME,sum( TAXTRAN.TAX_AMT ) as tax_amt,"
							+"PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,PORCP.SITE_CODE,PORCP.TOT_AMT   FROM TAXTRAN,TAX,PORCP,SUPPLIER  WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) "
							+"AND  ( SUPPLIER.SUPP_CODE = PORCP.SUPP_CODE ) AND  ( TAXTRAN.TRAN_ID = PORCP.TRAN_ID ) AND  "
							+"( ( TAXTRAN.TRAN_CODE = ? ) AND  ( PORCP.SITE_CODE = ? ) AND  ( PORCP.TRAN_DATE BETWEEN ? AND ?  ) AND  "
							+"( PORCP.SUPP_CODE BETWEEN ? AND ?  ) AND  ( TAXTRAN.TAX_CODE   BETWEEN ? AND ?  ) "
							+"AND  ( TAX.FORM_REQD  = 'Y' ) AND  ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND  ( TAXTRAN.TAX_FORM   IS NULL OR  "
							+" TAXTRAN.TAX_FORM_DATE   IS NULL )  AND  PORCP.TRAN_SER = 'P-RCP' )   GROUP BY TAXTRAN.TRAN_CODE,   TAXTRAN.TRAN_ID,"
							+"TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,PORCP.TRAN_DATE,PORCP.SUPP_CODE,SUPPLIER.SUPP_NAME,"
							+"PORCP.EXCISE_REF, PORCP.EXCISE_REF_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,PORCP.SITE_CODE, PORCP.TOT_AMT ";
						
							System.out.println("type:S::tranType:CLASS::refSer:P-RCP---->Executed ");
						}
						else if (refSer.equals("P-RET"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,PORCP.TRAN_DATE,PORCP.SUPP_CODE,SUPPLIER.SUPP_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,"
							+"PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,PORCP.SITE_CODE,PORCP.TOT_AMT FROM TAXTRAN,TAX,PORCP,SUPPLIER WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE )"
							+" AND ( SUPPLIER.SUPP_CODE = PORCP.SUPP_CODE ) AND ( TAXTRAN.TRAN_ID = PORCP.TRAN_ID ) AND"
							+" ( ( TAXTRAN.TRAN_CODE = ? ) AND ( PORCP.SITE_CODE = ?) AND ( PORCP.TRAN_DATE BETWEEN ? AND ? )"
							+"AND ( PORCP.SUPP_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE   BETWEEN ? AND ? )"
							+" AND ( TAX.FORM_REQD  = 'Y' ) AND ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND  ( TAXTRAN.TAX_FORM   IS NULL OR"
							+" TAXTRAN.TAX_FORM_DATE   IS NULL )  AND  PORCP.TRAN_SER = 'P-RET' ) GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,"
							+" TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,PORCP.TRAN_DATE, "
							+"PORCP.SUPP_CODE,SUPPLIER.SUPP_NAME,PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,PORCP.SITE_CODE, PORCP.TOT_AMT";
						
						System.out.println("type:S::tranType:CLASS::refSer:P-RET---->Executed ");
						}
						else if (refSer.equals("D-RCP"))
						{    
							System.out.println("2nd executed");
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
								+"TAXTRAN.TAX_ENV,DISTORD_RCP.TRAN_DATE, "			
								+"CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_RCP.DIST_ORDER,'C') END AS SUNDRY_CODE, "	
								+"CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_RCP.DIST_ORDER,'N') END AS SUNDRY_NAME, "
								+" SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,DISTORD_RCP.GP_NO,DISTORD_RCP.GP_DATE,DDF_GET_SUNDRY_DETAILS(DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE) as CITY,"
								+" DISTORDER.SITE_CODE, DISTORDER.NET_AMT FROM TAXTRAN,TAX,DISTORD_RCP,DISTORDER WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND "
								+" ( TAXTRAN.TRAN_ID = DISTORD_RCP.TRAN_ID ) AND ( DISTORD_RCP.DIST_ORDER = DISTORDER.DIST_ORDER ) AND ( ( TAXTRAN.TRAN_CODE = ? )"
								+" AND ( DISTORD_RCP.SITE_CODE = ? ) AND ( DISTORD_RCP.TRAN_DATE BETWEEN ? AND ?  ) AND"
								+" ( CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_RCP.DIST_ORDER,'C') END BETWEEN ? AND ?  )"
								+" AND( TAXTRAN.TAX_CODE   BETWEEN ? AND ? ) AND ( TAX.FORM_REQD  = 'Y' ) AND"
								+" ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND  ( TAXTRAN.TAX_FORM   IS NULL OR TAXTRAN.TAX_FORM_DATE   IS NULL )  )"
								+" GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID, TAXTRAN.TAX_FORM, TAXTRAN.TAX_FORM_DATE, TAXTRAN.TAX_CLASS,"
								+"TAXTRAN.TAX_ENV,DISTORD_RCP.TRAN_DATE,DISTORDER.SUNDRY_TYPE, DISTORD_RCP.DIST_ORDER, DISTORD_RCP.GP_NO,DISTORDER.SITE_CODE, DISTORDER.NET_AMT,"
								+"DISTORD_RCP.GP_DATE,DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE,DISTORDER.SITE_CODE, DISTORDER.NET_AMT"; 

							System.out.println("type:S::tranType:CLASS::refSer:D-RCP---->Executed ");
						}
						else if (refSer.equals("D-ISS"))
						{    
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,DISTORD_ISS.TRAN_DATE,"
							+"CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_ISS.DIST_ORDER,'C') END AS SUNDRY_CODE,"
							+"CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_ISS.DIST_ORDER,'N')  END AS SUNDRY_NAME,"							
							+" SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,DISTORD_ISS.GP_NO,DISTORD_ISS.GP_DATE,DDF_GET_SUNDRY_DETAILS(DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE) as CITY,DISTORDER.SITE_CODE, DISTORDER.NET_AMT FROM TAXTRAN,TAX,DISTORD_ISS,DISTORDER WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE )"
							+" AND ( TAXTRAN.TRAN_ID = DISTORD_ISS.TRAN_ID ) AND ( DISTORD_ISS.DIST_ORDER = DISTORDER.DIST_ORDER ) AND"
							+" ( ( TAXTRAN.TRAN_CODE = ? ) AND ( DISTORD_ISS.SITE_CODE = ? ) AND"
							+" ( DISTORD_ISS.TRAN_DATE BETWEEN ? AND ? ) AND "
							+"( CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_ISS.DIST_ORDER,'C') END BETWEEN ? AND ?  )"
							+" AND ( TAXTRAN.TAX_CODE   BETWEEN ? AND ? ) AND ( TAX.FORM_REQD  = 'Y' ) AND ( TAXTRAN.TAX_CLASS IS NOT NULL )"
							+" AND( TAXTRAN.TAX_FORM   IS NULL OR  TAXTRAN.TAX_FORM_DATE   IS NULL )  ) GROUP BY TAXTRAN.TRAN_CODE,"
							+" TAXTRAN.TRAN_ID, TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,DISTORD_ISS.TRAN_DATE,"
							+"DISTORDER.SUNDRY_TYPE,DISTORD_ISS.DIST_ORDER,DISTORD_ISS.GP_NO, DISTORD_ISS.GP_DATE,DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE,DISTORDER.SITE_CODE,DISTORDER.NET_AMT"; 
						
							System.out.println("type:S::tranType:CLASS::refSer:D-ISS---->Executed ");
						}
						else if (refSer.equals("S-DSP"))
						{  
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,DESPATCH.DESP_DATE,DESPATCH.CUST_CODE,"
							+"CUSTOMER.CUST_NAME, SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,DESPATCH.GP_NO,"
							+"DESPATCH.GP_DATE,DDF_GET_SUNDRY_DETAILS(CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE) as CITY,DESPATCH.SITE_CODE,DESPATCH.TOT_VALUE as AMOUNT FROM TAXTRAN,TAX,DESPATCH,CUSTOMER WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND"
							+" ( TAXTRAN.TRAN_ID = DESPATCH.DESP_ID ) AND ( DESPATCH.CUST_CODE = CUSTOMER.CUST_CODE ) AND "
							+"( ( TAXTRAN.TRAN_CODE = ? ) AND ( DESPATCH.SITE_CODE = ? ) AND "
							+"( DESPATCH.DESP_DATE BETWEEN ? AND ? ) AND ( DESPATCH.CUST_CODE BETWEEN ? AND ? ) AND"
							+" ( TAXTRAN.TAX_CODE   BETWEEN ? AND ? ) AND ( TAX.FORM_REQD  = 'Y' ) AND"
							+" ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND ( TAXTRAN.TAX_FORM   IS NULL OR TAXTRAN.TAX_FORM_DATE   IS NULL )  )"
							+" GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,"
							+"DESPATCH.DESP_DATE,DESPATCH.CUST_CODE,CUSTOMER.CUST_NAME,DESPATCH.GP_NO,DESPATCH.GP_DATE,CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE,DESPATCH.SITE_CODE,DESPATCH.TOT_VALUE";
							
							System.out.println("type:S::tranType:CLASS::refSer:S-DSP---->Executed ");
						}
						else if (refSer.equals("S-INV"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,"
							+"TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,INVOICE.TRAN_DATE,INVOICE.CUST_CODE,CUSTOMER.CUST_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT, ' ' ,' ',"
							+"DDF_GET_SUNDRY_DETAILS(CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE) as CITY,INVOICE.SITE_CODE,INVOICE.NET_AMT FROM TAXTRAN,TAX,INVOICE,CUSTOMER WHERE "
							+"( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND ( TAXTRAN.TRAN_ID = INVOICE.INVOICE_ID )"
							+" AND ( INVOICE.CUST_CODE = CUSTOMER.CUST_CODE ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND"
							+" ( INVOICE.SITE_CODE = ? ) AND ( INVOICE.TRAN_DATE BETWEEN ? AND ? ) AND "
							+"( INVOICE.CUST_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) "
							+"AND ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR "
							+"TAXTRAN.TAX_FORM_DATE IS NULL ) ) GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,"
							+"TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,INVOICE.TRAN_DATE,"
							+"INVOICE.CUST_CODE,CUSTOMER.CUST_NAME,CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE,INVOICE.SITE_CODE, INVOICE.NET_AMT  ";
						
							System.out.println("type:S::tranType:CLASS::refSer:S-INV---->Executed ");
						}
						else if (refSer.equals("VOUCH"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,"
							+"TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,VOUCHER.TRAN_DATE,VOUCHER.SUPP_CODE,SUPPLIER.SUPP_NAME,"
							+" SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,VOUCHER.BILL_NO,VOUCHER.BILL_DATE,DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,VOUCHER.SITE_CODE,VOUCHER.NET_AMT "
							+" FROM TAXTRAN,TAX,SUPPLIER,VOUCHER"
							+" WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND ( TAXTRAN.TRAN_ID = VOUCHER.TRAN_ID ) AND "
							+"( VOUCHER.SUPP_CODE = SUPPLIER.SUPP_CODE ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND "
							+"( VOUCHER.SITE_CODE = ? ) AND ( VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND "
							+"( VOUCHER.SUPP_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE   BETWEEN ? AND ? ) "
							+"AND ( TAX.FORM_REQD  = 'Y' ) AND ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND ( TAXTRAN.TAX_FORM   IS NULL OR "
							+"TAXTRAN.TAX_FORM_DATE   IS NULL )  ) GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,"
							+"TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,"
							+"VOUCHER.TRAN_DATE,VOUCHER.SUPP_CODE,SUPPLIER.SUPP_NAME,VOUCHER.BILL_NO,VOUCHER.BILL_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,VOUCHER.SITE_CODE, VOUCHER.NET_AMT";
						
							System.out.println("type:S::tranType:CLASS::refSer:VOUCH---->Executed ");
						}
						else if (refSer.equals("M-VOUC"))
						{    
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,"
							+"TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,MISC_VOUCHER.TRAN_DATE,MISC_VOUCHER.SUNDRY_CODE,"
							+" FN_SUNDRY_NAME(MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE,' ') AS SUNDRY_NAME,"
							+" SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,MISC_VOUCHER.BILL_NO,MISC_VOUCHER.BILL_DATE,DDF_GET_SUNDRY_DETAILS(MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE) as CITY,MISC_VOUCHER.SITE_CODE,MISC_VOUCHER.NET_AMT "
							+" FROM TAXTRAN,TAX,MISC_VOUCHER WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND "
							+"( TAXTRAN.TRAN_ID = MISC_VOUCHER.TRAN_ID ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND "
							+" ( MISC_VOUCHER.SITE_CODE = ? ) AND ( MISC_VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND "
							+"( MISC_VOUCHER.SUNDRY_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE   BETWEEN ? AND ? ) AND "
							+"( TAX.FORM_REQD  = 'Y' ) AND ( TAXTRAN.TAX_CLASS IS NOT NULL ) AND ( TAXTRAN.TAX_FORM   IS NULL OR "
							+"TAXTRAN.TAX_FORM_DATE   IS NULL )  ) AND (MISC_VOUCHER.SUNDRY_TYPE = ?)  GROUP BY "
							+" TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,"
							+"TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,MISC_VOUCHER.TRAN_DATE,"
							+" MISC_VOUCHER.SUNDRY_CODE,"
							+"MISC_VOUCHER.BILL_NO,MISC_VOUCHER.BILL_DATE,MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE,MISC_VOUCHER.SITE_CODE, MISC_VOUCHER.NET_AMT"  ;
						
							System.out.println("type:S::tranType:CLASS::refSer:M-VOUC---->Executed ");
						}
					}
				}
				else if (tranType.equalsIgnoreCase("CHAP"))
				{
					if(refSer!=null)
					{
						if (refSer.equals("S-RET"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS"
							+",TAXTRAN.TAX_ENV,SRETURN.TRAN_DATE,SRETURN.CUST_CODE,CUSTOMER.CUST_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,"
							+"SRETURN.EXCISE_REF_NO,SRETURN.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE) as CITY," +
							"SRETURN.SITE_CODE,SRETURN.NET_AMT " +
							"FROM TAX,TAXTRAN,CUSTOMER,SRETURN WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE )"
							+" AND ( CUSTOMER.CUST_CODE = SRETURN.CUST_CODE ) AND ( TAXTRAN.TRAN_ID = SRETURN.TRAN_ID ) AND ( ( TAXTRAN.TRAN_CODE = ? )"
							+" AND (SRETURN.SITE_CODE = ? ) AND (SRETURN.TRAN_DATE BETWEEN ? AND ? ) AND "
							+"(SRETURN.CUST_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND"
							+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR TAXTRAN.TAX_FORM_DATE IS NULL ) )"
							+" GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,SRETURN.TRAN_DATE,SRETURN.CUST_CODE,"
							+"CUSTOMER.CUST_NAME,SRETURN.EXCISE_REF_NO,SRETURN.EXCISE_REF_DATE,CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE,SRETURN.SITE_CODE, SRETURN.NET_AMT";   
						
							System.out.println("type:S::tranType:CHAP::refSer:S-RET---->Executed ");	
						}
						else if (refSer.equals("P-RCP"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,PORCP.TRAN_DATE,PORCP.SUPP_CODE ,SUPPLIER.SUPP_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,"
							+"PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,PORCP.SITE_CODE,PORCP.TOT_AMT  FROM TAX,TAXTRAN,PORCP,SUPPLIER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND"
							+" ( TAXTRAN.TRAN_ID = PORCP.TRAN_ID ) AND ( SUPPLIER.SUPP_CODE = PORCP.SUPP_CODE ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND"
							+" (PORCP.SITE_CODE = ? ) AND (PORCP.TRAN_DATE BETWEEN ? AND ? ) AND"
							+" (PORCP.SUPP_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND"
							+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL"
							+" OR TAXTRAN.TAX_FORM_DATE IS NULL )  AND (PORCP.TRAN_SER = 'P-RCP')) GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,"
							+" TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,PORCP.TRAN_DATE,PORCP.SUPP_CODE,SUPPLIER.SUPP_NAME,"
							+" PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,PORCP.SITE_CODE, PORCP.TOT_AMT"; 
						
							System.out.println("type:S::tranType:CHAP::refSer:P-RCP---->Executed ");
						}
						else if (refSer.equals("P-RET"))
						{
							getDataSql = "	SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV, PORCP.TRAN_DATE,PORCP.SUPP_CODE,SUPPLIER.SUPP_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,"
							+"PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY,PORCP.SITE_CODE,PORCP.TOT_AMT FROM TAX,TAXTRAN,PORCP,SUPPLIER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND"
							+" ( TAXTRAN.TRAN_ID = PORCP.TRAN_ID ) AND ( SUPPLIER.SUPP_CODE = PORCP.SUPP_CODE ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND "
							+"(PORCP.SITE_CODE = ? ) AND (PORCP.TRAN_DATE BETWEEN ? AND ? ) AND"
							+" (PORCP.SUPP_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND"
							+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR"
							+" TAXTRAN.TAX_FORM_DATE IS NULL )  AND (PORCP.TRAN_SER = 'P-RET')) GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,"
							+" TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,PORCP.TRAN_DATE,PORCP.SUPP_CODE,"
							+"SUPPLIER.SUPP_NAME, PORCP.EXCISE_REF,PORCP.EXCISE_REF_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,PORCP.SITE_CODE, PORCP.TOT_AMT";
						
							System.out.println("type:S::tranType:CHAP::refSer:P-RET---->Executed ");
						}
						else if (refSer.equals("D-RCP"))
						{   
							System.out.println("4th executed");
							getDataSql = "	SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,DISTORD_RCP.TRAN_DATE,"
							+" CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_RCP.DIST_ORDER,'C') END AS SUNDRY_CODE,"
							+"CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_RCP.DIST_ORDER,'N') END AS SUNDRY_NAME,"		
							+" SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,DISTORD_RCP.GP_NO,DISTORD_RCP.GP_DATE,DDF_GET_SUNDRY_DETAILS(DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE) as CITY,DISTORDER.SITE_CODE, DISTORDER.NET_AMT FROM TAX,TAXTRAN,DISTORD_RCP,DISTORDER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND"
							+" ( TAXTRAN.TRAN_ID = DISTORD_RCP.TRAN_ID ) AND ( DISTORD_RCP.DIST_ORDER = DISTORDER.DIST_ORDER ) AND"
							+" ( ( TAXTRAN.TRAN_CODE = ? ) AND (DISTORD_RCP.SITE_CODE = ? ) AND (DISTORD_RCP.TRAN_DATE BETWEEN ? AND ? ) AND"
							+" (CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_RCP.DIST_ORDER,'C') END BETWEEN ? AND ?) AND"
							+" ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND"
							+" ( TAXTRAN.TAX_FORM IS NULL OR TAXTRAN.TAX_FORM_DATE IS NULL ) ) GROUP BY TAXTRAN.TRAN_CODE, TAXTRAN.TRAN_ID,"
							+" TAXTRAN.TAX_FORM, TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,DISTORD_RCP.TRAN_DATE,"
							+" DISTORDER.SUNDRY_TYPE,DISTORD_RCP.DIST_ORDER,DISTORD_RCP.GP_NO,DISTORD_RCP.GP_DATE,DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE,DISTORDER.SITE_CODE, DISTORDER.NET_AMT";   
						
							System.out.println("type:S::tranType:CHAP::refSer:D-RCP---->Executed ");
						}
						else if (refSer.equals("D-ISS"))
						{   
							getDataSql = " SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,DISTORD_ISS.TRAN_DATE,"
							+"CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_ISS.DIST_ORDER,'C') END AS SUNDRY_CODE,"
							+" CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_ISS.DIST_ORDER,'N') END AS SUNDRY_NAME,"
							+"SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,DISTORD_ISS.GP_NO,DISTORD_ISS.GP_DATE,DDF_GET_SUNDRY_DETAILS(DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE) as CITY,DISTORDER.SITE_CODE, DISTORDER.NET_AMT FROM TAX,TAXTRAN,DISTORD_ISS,DISTORDER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND"
							+" ( TAXTRAN.TRAN_ID = DISTORD_ISS.TRAN_ID ) AND ( DISTORD_ISS.DIST_ORDER = DISTORDER.DIST_ORDER ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND"
							+" (DISTORD_ISS.SITE_CODE = ? ) AND (DISTORD_ISS.TRAN_DATE BETWEEN ? AND ? ) AND"
							+" (CASE WHEN DISTORDER.SUNDRY_TYPE = 'O' THEN '0' ELSE FN_GET_SUNDRY(TAXTRAN.TRAN_CODE,DISTORD_ISS.DIST_ORDER,'C')"
							+" END BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ?) AND"
							+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR TAXTRAN.TAX_FORM_DATE IS NULL ) )"
							+" GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,"
							+" DISTORD_ISS.TRAN_DATE,DISTORDER.SUNDRY_TYPE, DISTORD_ISS.DIST_ORDER, DISTORD_ISS.GP_NO,DISTORD_ISS.GP_DATE,DISTORDER.SUNDRY_TYPE,DISTORDER.SUNDRY_CODE,DISTORDER.SITE_CODE, DISTORDER.NET_AMT";
						
							System.out.println("type:S::tranType:CHAP::refSer:D-ISS---->Executed ");
						}
						else if (refSer.equals("S-DSP"))
						{   
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,DESPATCH.DESP_DATE,"
							+"DESPATCH.CUST_CODE,CUSTOMER.CUST_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,DESPATCH.GP_NO,DESPATCH.GP_DATE,DDF_GET_SUNDRY_DETAILS(CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE) as CITY,DESPATCH.SITE_CODE,DESPATCH.TOT_VALUE as AMOUNT  FROM TAX, TAXTRAN,DESPATCH,CUSTOMER WHERE"
							+" ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND ( TAXTRAN.TRAN_ID = DESPATCH.DESP_ID ) AND ( DESPATCH.CUST_CODE = CUSTOMER.CUST_CODE )"
							+" AND ( ( TAXTRAN.TRAN_CODE = ? ) AND (DESPATCH.SITE_CODE = ? ) AND"
							+" (DESPATCH.DESP_DATE BETWEEN ? AND ? ) AND (DESPATCH.CUST_CODE BETWEEN ? AND ? )"
							+" AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL )"
							+" AND ( TAXTRAN.TAX_FORM IS NULL OR TAXTRAN.TAX_FORM_DATE IS NULL ) )"
							+"GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,"
							+"DESPATCH.DESP_DATE,DESPATCH.CUST_CODE,CUSTOMER.CUST_NAME,DESPATCH.GP_NO,DESPATCH.GP_DATE,CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE,DESPATCH.SITE_CODE,DESPATCH.TOT_VALUE";
						
							System.out.println("type:S::tranType:CHAP::refSer:S-DSP---->Executed ");
						}
						else if (refSer.equals("S-INV"))
						{  
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,"
							+"TAXTRAN.TAX_ENV,INVOICE.TRAN_DATE,INVOICE.CUST_CODE,CUSTOMER.CUST_NAME,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,' ',' ',DDF_GET_SUNDRY_DETAILS(CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE) as CITY,INVOICE.SITE_CODE,INVOICE.NET_AMT "
							+" FROM TAXTRAN,TAX,INVOICE,CUSTOMER WHERE ( TAX.TAX_CODE = TAXTRAN.TAX_CODE ) AND ( TAXTRAN.TRAN_ID = INVOICE.INVOICE_ID ) AND "
							+" ( INVOICE.CUST_CODE = CUSTOMER.CUST_CODE ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND ( INVOICE.SITE_CODE = ?) AND"
							+" ( INVOICE.TRAN_DATE BETWEEN ? AND ? ) AND ( INVOICE.CUST_CODE BETWEEN ? AND ? ) AND"
							+" ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND "
							+" ( TAXTRAN.TAX_FORM IS NULL OR TAXTRAN.TAX_FORM_DATE IS NULL ) ) GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,"
							+" TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,INVOICE.TRAN_DATE,"
							+"INVOICE.CUST_CODE, CUSTOMER.CUST_NAME,CUSTOMER.CUST_TYPE,CUSTOMER.CUST_CODE,INVOICE.SITE_CODE,INVOICE.NET_AMT ";
						
							System.out.println("type:S::tranType:CHAP::refSer:S-INV---->Executed ");
						}
						else if (refSer.equals("VOUCH"))
						{
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,"
							+"TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,VOUCHER.TRAN_DATE,VOUCHER.SUPP_CODE,SUPPLIER.SUPP_NAME,"
							+"SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT,VOUCHER.BILL_NO,VOUCHER.BILL_DATE,DDF_GET_SUNDRY_DETAILS(SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE) as CITY," +
							"VOUCHER.SITE_CODE,VOUCHER.NET_AMT FROM TAX,TAXTRAN,SUPPLIER,"
							+" VOUCHER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND ( TAXTRAN.TRAN_ID = VOUCHER.TRAN_ID ) AND "
							+" ( VOUCHER.SUPP_CODE = SUPPLIER.SUPP_CODE ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND "
							+" (VOUCHER.SITE_CODE = ? ) AND (VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND "
							+" (VOUCHER.SUPP_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND"
							+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR "
							+" TAXTRAN.TAX_FORM_DATE IS NULL ) ) GROUP BY TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,"
							+" TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,VOUCHER.TRAN_DATE,"
							+" VOUCHER.SUPP_CODE,SUPPLIER.SUPP_NAME, VOUCHER.BILL_NO,VOUCHER.BILL_DATE,SUPPLIER.SUPP_TYPE,SUPPLIER.SUPP_CODE,VOUCHER.SITE_CODE, VOUCHER.NET_AMT";   
						
							System.out.println("type:S::tranType:CHAP::refSer:VOUCH---->Executed ");
						}
						else if (refSer.equals("M-VOUC"))
						{   
							getDataSql = "SELECT TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,TAXTRAN.TAX_FORM_DATE,"
							+" TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,MISC_VOUCHER.TRAN_DATE,MISC_VOUCHER.SUNDRY_CODE,"
							+" FN_SUNDRY_NAME(MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE,' ') AS SUNDRY_NAME "
							+" ,SUM( TAXTRAN.TAX_AMT ) AS TAX_AMT"
							+",MISC_VOUCHER.BILL_NO,MISC_VOUCHER.BILL_DATE," +
							"DDF_GET_SUNDRY_DETAILS(MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE) as CITY," +
							"MISC_VOUCHER.SITE_CODE,MISC_VOUCHER.NET_AMT "
							+" FROM TAX,TAXTRAN,MISC_VOUCHER WHERE ( TAXTRAN.TAX_CODE = TAX.TAX_CODE ) AND "
							+" ( TAXTRAN.TRAN_ID = MISC_VOUCHER.TRAN_ID ) AND ( ( TAXTRAN.TRAN_CODE = ? ) AND "
							+" (MISC_VOUCHER.SITE_CODE = ? ) AND (MISC_VOUCHER.TRAN_DATE BETWEEN ? AND ? ) AND "
							+" (MISC_VOUCHER.SUNDRY_CODE BETWEEN ? AND ? ) AND ( TAXTRAN.TAX_CODE BETWEEN ? AND ? ) AND "
							+" ( TAX.FORM_REQD = 'Y' ) AND ( TAXTRAN.TAX_CHAP IS NOT NULL ) AND ( TAXTRAN.TAX_FORM IS NULL OR "
							+" TAXTRAN.TAX_FORM_DATE IS NULL ) ) AND (MISC_VOUCHER.SUNDRY_TYPE = ?) GROUP BY "
							+" TAXTRAN.TRAN_CODE,TAXTRAN.TRAN_ID,TAXTRAN.TAX_FORM,"
							+" TAXTRAN.TAX_FORM_DATE,TAXTRAN.TAX_CLASS,TAXTRAN.TAX_ENV,MISC_VOUCHER.TRAN_DATE,"
							+" MISC_VOUCHER.SUNDRY_CODE,MISC_VOUCHER.BILL_NO,MISC_VOUCHER.BILL_DATE"
							+",MISC_VOUCHER.SUNDRY_TYPE,MISC_VOUCHER.SUNDRY_CODE,MISC_VOUCHER.SITE_CODE, MISC_VOUCHER.NET_AMT";
						
							System.out.println("type:S::tranType:CHAP::refSer:M-VOUC---->Executed ");
						}
					}
				}
			}
			System.out.println("Sql Fired :::::"+getDataSql.trim().length());
			if (getDataSql.trim().length()>0) //Case added by Jaimin 23/08/2007 for executing query for summary. requst-ID :DI78DIS028
			{
				System.out.println("Sql Fired :::::"+getDataSql);					
				pstmt = conn.prepareStatement(getDataSql);
				pstmt.setString(1,refSerWs);
				pstmt.setString(2,siteCodeWs);
				pstmt.setTimestamp(3,chgdateFrom); 
				pstmt.setTimestamp(4,chgdateTo);					
				pstmt.setString(5,sundCodeFromWs);
				pstmt.setString(6,sundCodeToWs);
				pstmt.setString(7,taxCodeFromWs);
				pstmt.setString(8,taxCodeToWs);
				if (refSer.equals("M-VOUC")) //Condition added by Jaimin 06/09/2007 for M-VOUC b'coz query returns valur for sandry type. requst-ID :DI78DIS028
					pstmt.setString(9,sundryType);

				System.out.println("QUERY IS IN PROCESS..........");
				rs1 = pstmt.executeQuery();
				System.out.println("QUERY  PROCESS FINISED.......");
				while(rs1.next()) 
				{
					//////////////////cpatil

					count++;
					retTabSepStrBuff.append("<Detail2>");
					//TRAN_CODE
					retTabSepStrBuff.append("<tran_code>").append("<![CDATA[" +(rs1.getString("TRAN_CODE")) +"]]>").append("</tran_code>");
					//TRAN_ID
					retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" +(rs1.getString("TRAN_ID")) +"]]>").append("</tran_id>");
					//LINE_NO
					retTabSepStrBuff.append("<line_no>").append("0").append("</line_no>");
					//TRANDATE
					
					if(refSer.equalsIgnoreCase("S-DSP"))  // DESP_DATE
					{
						if(rs1.getTimestamp("DESP_DATE")!=null)
			            {
							retTabSepStrBuff.append("<tran_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("DESP_DATE").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</tran_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<tran_date>").append(" ").append("</tran_date>");
						}
					}
					else
					{
					if(rs1.getTimestamp("tran_date")!=null)
		            {
						retTabSepStrBuff.append("<tran_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("tran_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</tran_date>");
		            }
					else
					{
						retTabSepStrBuff.append("<tran_date>").append(" ").append("</tran_date>");
					}
					}
					
					/*
					//EXCISE_REF_NO
					retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +(rs1.getString("BILL_NO")) +"]]>").append("</excise_ref_no>");
					//EXCISE_REF_DATE
					if(rs1.getTimestamp("BILL_DATE")!=null )
		            {
						retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("BILL_DATE").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
		            }
					else
					{
						retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
					}
					*/
					
					//EXCISE_REF_NO	  ////EXCISE_REF_DATE
					if( refSer.equals("S-RET"))
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("excise_ref_no")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("excise_ref_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("excise_ref_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}	
					else if ( refSer.equals("P-RCP") || refSer.equals("P-RET") )
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("excise_ref")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("excise_ref_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("excise_ref_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}
					else if( refSer.equals("D-RCP") || refSer.equals("D-ISS") || refSer.equals("S-DSP") )
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("gp_no")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("gp_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("gp_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}
					else if ( refSer.equals("S-INV") )
					{

						retTabSepStrBuff.append("<excise_ref_no>").append(" ").append("</excise_ref_no>");
						retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
					}
					else if( refSer.equals("VOUCH") || refSer.equals("M-VOUCH") )
					{
						retTabSepStrBuff.append("<excise_ref_no>").append("<![CDATA[" +checkNull(rs1.getString("bill_no")) +"]]>").append("</excise_ref_no>");
						
						if(rs1.getTimestamp("bill_date")!=null )
			            {
							retTabSepStrBuff.append("<excise_ref_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("bill_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</excise_ref_date>");
			            }
						else
						{
							retTabSepStrBuff.append("<excise_ref_date>").append(" ").append("</excise_ref_date>");
						}
					}
						
					
					//sundry_code  // //sundry_name
					if( refSer.equals("S-RET") ||  refSer.equals("S-DSP") ||  refSer.equals("S-INV"))
					{
						retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("cust_code")) +"]]>").append("</sundry_code>");
						retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("cust_name")) +"]]>").append("</sundry_name>");
					}
					else if( refSer.equals("P-RCP") ||  refSer.equals("P-RET") ||  refSer.equals("VOUCH"))
					{
						retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("supp_code")) +"]]>").append("</sundry_code>");
						retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("supp_name")) +"]]>").append("</sundry_name>");
					}
					else if( refSer.equals("D-RCP") ||  refSer.equals("D-ISS") ||  refSer.equals("M-VOUCH"))
					{
						retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("sundry_code")) +"]]>").append("</sundry_code>");
						retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("sundry_name")) +"]]>").append("</sundry_name>");
					}
					
					/*
					//sundry_code
					retTabSepStrBuff.append("<sundry_code>").append("<![CDATA[" +(rs1.getString("sundry_code")) +"]]>").append("</tran_id>");
					//sundry_name
					retTabSepStrBuff.append("<sundry_name>").append("<![CDATA[" +(rs1.getString("sundry_name")) +"]]>").append("</sundry_name>");
					*/
					
					//CITY
					  String str=rs1.getString("CITY")==null?" ":rs1.getString("CITY");
					    if (str.equalsIgnoreCase(null) || str.equalsIgnoreCase(" ") || str.equalsIgnoreCase(""))
					    {
					    	retTabSepStrBuff.append("<city>").append(" ").append("</city>");
					    }
					    else
					    {
					    	String strArr[] = str.split("~n");
					    	System.out.println("@@@@2 city ["+strArr[4]==null?" ":strArr[4]+"]");
						    retTabSepStrBuff.append("<city>").append("<![CDATA[" +(strArr[4]==null?" ":strArr[4]) +"]]>").append("</city>");
					    }	
				    //SITE CODE
					retTabSepStrBuff.append("<site_code>").append("<![CDATA[" +(rs1.getString("site_code")) +"]]>").append("</site_code>");
					//NET AMOUNT
					//retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +(rs1.getString("NET_AMT")) +"]]>").append("</net_amount>");
					
					if( refSer.equals("S-RET") ||  refSer.equals("D-RCP") ||  refSer.equals("D-ISS") ||  refSer.equals("S-INV") ||  refSer.equals("VOUCH") ||  refSer.equals("M-VOUCH") )
					{
						retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +checkNull(rs1.getString("NET_AMT")) +"]]>").append("</net_amount>");
					}
					else if( refSer.equals("P-RCP") ||  refSer.equals("P-RET") )
					{
						retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +checkNull(rs1.getString("TOT_AMT")) +"]]>").append("</net_amount>");
					}
					else if( refSer.equals("S-DSP") )
					{
						retTabSepStrBuff.append("<net_amount>").append("<![CDATA[" +checkNull(rs1.getString("amount")) +"]]>").append("</net_amount>");
					}
					
					//TAX_FORM
					retTabSepStrBuff.append("<tax_form>").append("<![CDATA[" + checkNull(rs1.getString("tax_form")) +"]]>").append("</tax_form>");
					//TAX_FORM_DATE
					if(rs1.getTimestamp("tax_form_date")!=null )
		            {
						retTabSepStrBuff.append("<tax_form_date>").append("<![CDATA[" +(this.genericUtility.getValidDateString(rs1.getTimestamp("tax_form_date").toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</tax_form_date>");
		            }
					else
					{
						retTabSepStrBuff.append("<tax_form_date>").append(" ").append("</tax_form_date>");
					}
					//TRAN_CODE  //tax_code
					retTabSepStrBuff.append("<tax_code>").append(" ").append("</tax_code>");
					//LINE_NO__TAX
					retTabSepStrBuff.append("<line_no__tax>").append("0").append("</line_no__tax>");
					//taxable_amt
					retTabSepStrBuff.append("<taxable_amt>").append("0").append("</taxable_amt>");
					//TAX_AMT
					retTabSepStrBuff.append("<tax_amt>").append("<![CDATA[" +(rs1.getString("tax_amt")) +"]]>").append("</tax_amt>");
					//TAX_PERC
					retTabSepStrBuff.append("<tax_perc>").append("0").append("</tax_perc>");
					//TAX_CLASS
					retTabSepStrBuff.append("<tax_class>").append("<![CDATA[" +(rs1.getString("tax_class")) +"]]>").append("</tax_class>");
					//TRAN_CHAP
					retTabSepStrBuff.append("<tax_chap>").append(" ").append("</tax_chap>");
					//TAX_ENV
					retTabSepStrBuff.append("<tax_env>").append("<![CDATA[" +(rs1.getString("tax_env")) +"]]>").append("</tax_env>");
					
					retTabSepStrBuff.append("</Detail2>");
					
					
					/////////////// cpatil
				
				/*	
					
						//TRAN_CODE ok 
						retTabSepStrBuff.append((rs1.getString(1)==null?" ":rs1.getString(1))).append("\t");						
						//TRAN_ID    ok
						retTabSepStrBuff.append((rs1.getString(2)==null?" ":rs1.getString(2))).append("\t");
						//LINE_NO    ok
						retTabSepStrBuff.append("0").append("\t");
						//LINE_NO__TAX    ok
						retTabSepStrBuff.append("0").append("\t");
						//TAX_FORM     ok
						retTabSepStrBuff.append((rs1.getString(3)==null?" ":rs1.getString(3))).append("\t");
						//TAX_FORM_DATE     ok
						if(rs1.getTimestamp(4)!=null)
						{
							retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(4).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
						}
						else
						{
								retTabSepStrBuff.append(" ").append("\t");
						}
						//TAX_CLASS    ok
						retTabSepStrBuff.append((rs1.getString(5)==null?" ":rs1.getString(5))).append("\t");
						//TAX_PERC     ok
						retTabSepStrBuff.append("0").append("\t");
						//TAX_ENV     ok
						retTabSepStrBuff.append((rs1.getString(6)==null?" ":rs1.getString(6))).append("\t");
						//TAXABLE_AMT     ok
						retTabSepStrBuff.append("0").append("\t");
						//TAX_CODE   ok
						retTabSepStrBuff.append(" ").append("\t");
						//TRAN_CHAP    ok
						retTabSepStrBuff.append(" ").append("\t");
						//TAX_AMT     ok
						retTabSepStrBuff.append((rs1.getString(10)==null?" ":rs1.getString(10))).append("\t");
						//SUNDRY_NAME    ok
						retTabSepStrBuff.append((rs1.getString(9)==null?" ":rs1.getString(9))).append("\t");
						//TRANDATE    ok
						if(rs1.getTimestamp(7)!=null)
						{
							retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(7).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
						}
						else
						{
								retTabSepStrBuff.append(" ").append("\t");
						}
						//SUNDRY_CODE     ok
						retTabSepStrBuff.append((rs1.getString(8)==null?" ":rs1.getString(8))).append("\t");
						//EXCISE_REF_NO    ok
						retTabSepStrBuff.append((rs1.getString(11)==null?" ":rs1.getString(11))).append("\t");
						//EXCISE_REF_DATE    ok
						if (refSer.equals("S-INV"))//Condition added by Jaimin 11/09/2007 requst-ID :DI78DIS028
						{
							retTabSepStrBuff.append((rs1.getString(12)==null?" ":rs1.getString(12))).append("\t");
						}
						else
						{
							if(rs1.getTimestamp(12)!=null)
							{								
								retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(12).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
							}
							else
							{
									retTabSepStrBuff.append(" ").append("\t");
							}
						}		
						
						//added by chandrakant patil start 29 March
						//CITY      ok
						//if( (refSer.equals("D-RCP")) || (refSer.equals("D-ISS")) || (refSer.equals("M-VOUC")))
						//{
						 
						//String
						  str=rs1.getString(13)==null?" ":rs1.getString(13);
						 	if (str.equalsIgnoreCase(null) || str.equalsIgnoreCase(" ") || str.equalsIgnoreCase(""))
						    {
						 		retTabSepStrBuff.append(" ").append("\t");
						    }
						    else
						    {
						    	String strArr[] = str.split("~n");
							    retTabSepStrBuff.append((strArr[4]==null?" ":strArr[4])).append("\t");
						    }
					//	
					//	  }
					//	 
					//	else
					//	{	
					//	retTabSepStrBuff.append((rs1.getString(13)==null?" ":rs1.getString(13))).append("\t");						
					//	}
					//	
						//SITE CODE   ok
						retTabSepStrBuff.append((rs1.getString(14)==null?" ":rs1.getString(14))).append("\t");
						//NET AMOUNT   ok
						retTabSepStrBuff.append((rs1.getString(15)==null?" ":rs1.getString(15))).append("\t");
						//chandrakant Finish
						retTabSepStrBuff.append("\n");				
						count++;
						
					*/	
						
						System.out.println("#####Counter"+count);
				}//END WHILE
				rs1.close();
				pstmt.close();
				rs1=null;
				pstmt=null;
				if (count == 0 )//Condition added by Jaimin 23/08/07 requst-ID :DI78DIS028
				{
					errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
				}
			}
			else//added by jaimin 23/08/07 requst-ID :DI78DIS028
			{
				System.out.println("Sql:::::is Null ::::::::::::::");
				errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
			}//end by jaimin 23/08/07 requst-ID :DI78DIS028
		}//End by jaimin 31/08/2007 requst-ID :DI78DIS028
		
		retTabSepStrBuff.append("</Header0>");
		retTabSepStrBuff.append("</group0>");
		retTabSepStrBuff.append("</DocumentRoot>");
		
		System.out.println("retTabSepStrBuff:::["+retTabSepStrBuff.toString()+"]");
		System.out.println("#####Counter"+count);
			//end of outer else if
	}	//end of try	
	catch (SQLException e)
	{
		e.printStackTrace();
		System.out.println("SQLException :StockAllocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
		throw new ITMException(e);
	}
	catch (Exception e)
	{
		e.printStackTrace();
		System.out.println("Exception :StockAllocationPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
		throw new ITMException(e);
	}
	finally
	{		
		try
		{		
			conn.close();//added by Jaimin 30/08/2007 requst-ID :DI78DIS028
			conn = null;
		}
		catch(Exception e)
		{
			errString = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
	}		
	if (getDataSql.trim().length()>0 && count > 0)//Condition added by Jaimin 23/08/07 requst-ID :DI78DIS028
		return retTabSepStrBuff.toString();	
	else 
		return errString;
	}//END OF GETDATA(2)
	
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
		//PROCESS
		public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
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
				
				System.out.println("Exception :TaxFormRecoMultiEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
				e.printStackTrace();
				retStr = e.getMessage();
			}
			return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		//=====Newly added fields================
		String tranCode=null;
		String tranId=null;
		String lineNo=null;
		String lineNoTax=null;
		String taxForm=null;
		String taxFormDate=null;
		//==============================
		int updCnt = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		String childNodeName = "";
		String errCode = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		String errString=" ";


		Node parentNode = null;
		Node childNode = null; 
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		PreparedStatement pstmt = null;
		Statement st = null; 
		String errFrSret ="";
		String frsFlag=null;
		ConnDriver connDriver = new ConnDriver();//Added by Jaimin 30/08/2007 requst-ID :DI78DIS028
		Connection conn= null;//Added by Jaimin 30/08/2007		requst-ID :DI78DIS028
		String type = genericUtility.getColumnValue("type",headerDom);//Added by jaimin 01/09/2007 requst-ID :DI78DIS028
		try
		{
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);	
			}
			System.out.println("TaxFormRecoMultiEJB ejbCreate called.........");
		}
		catch (Exception e)
		{
			System.out.println("Exception :TaxFormRecoMultiEJB :ejbCreate :==>"+e.getMessage());
			
		} 
		
	//	**************************** comming from brow========================
		
	
		try
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
					if (childNodeName.equals("tran_code"))
					{
						tranCode = childNode.getFirstChild().getNodeValue();
					}
					if (childNodeName.equals("tran_id"))
					{
						tranId = childNode.getFirstChild().getNodeValue();
					}

					if (childNodeName.equals("line_no"))
					{
						lineNo = childNode.getFirstChild().getNodeValue();
					}	
					if (childNodeName.equals("line_no__tax"))
					{
						lineNoTax = childNode.getFirstChild().getNodeValue();
					}
					if (childNodeName.equals("tax_form") )
					{
						//taxForm = childNode.getFirstChild().getNodeValue();    // modify for null pointer exception on 19/02/14
						taxForm = childNode.getFirstChild() == null?"":childNode.getFirstChild().getNodeValue();
						System.out.println("=====================Form:"+taxForm);
					}
					if (childNodeName.equals("tax_form_date"))
					{
						//taxFormDate = childNode.getFirstChild().getNodeValue();      // modify for null pointer exception on 19/02/14
						taxFormDate = childNode.getFirstChild() == null?"":childNode.getFirstChild().getNodeValue();
						System.out.println("Date=====================Date:"+taxFormDate);
					}
				}//inner for loop
				if(type.equalsIgnoreCase("D")) //Condition added by jaimin 03/09/2007 requst-ID :DI78DIS028
				{
					calTaxFormRecoMultiUpdate(tranCode,tranId,lineNo,lineNoTax,taxForm,taxFormDate,conn);				
				}
				else
				{
					calTaxFormRecoMultiUpdate(tranCode,tranId,taxForm,taxFormDate,conn);
				}//Condition end by jaimin 03/09/2007 requst-ID :DI78DIS028
			}// OUT FOR LOOP 
			conn.commit(); //added by kunal on 13/03/13 as per manoharan sir instuction 
			errString = itmDBAccessEJB.getErrorString("","VPSUCC1","","",conn);
		}//try end	
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception d)
			{
				System.out.println("Exception : TaxFormRecoMultiEJB =>"+d.toString());
			}
			System.out.println("Exception :TaxFormRecoMultiEJB :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			errString = e.getMessage();
			
			return errString ;
		}
		finally
		{
			System.out.println("Closing Connection....");
			try
			{
				conn.close();
				conn = null;
				//conn.commit();//Commeted by Jaimin 30/08/2007
				/*if(conn != null) //Commeted by Jaimin 30/08/2007
				{					
					if(pstmt != null)
					{ 
						pstmt.close(); 
						pstmt=null;
					}
					conn.close();
					conn = null;
				}*/
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString ;
			}
		}
		return errString;
	}//END OF PROCESS(2)

	private void calTaxFormRecoMultiUpdate(String tranCode,String tranId,String lineNo,String lineNoTax,String taxForm,String taxFormDate,Connection conn)
	{
		PreparedStatement pstmt=null;
		String sql=null;
		System.out.println("calTaxFormRecoMultiUpdate() method Called");		
		try
		{ 
			System.out.println("===================tranCode"+tranCode);
			System.out.println("===================tranId"+tranId);
			System.out.println("===================lineNo"+lineNo);
			System.out.println("===================lineNoTax"+lineNoTax);
			System.out.println("===================taxForm"+taxForm);
			System.out.println("===================conn"+conn);


			Timestamp chgDate=null;
			Object date = null;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("Date=====================Date"+taxFormDate);
			System.out.println("===================sdf"+sdf);
			date = sdf.parse(taxFormDate);			
			System.out.println("===================sdf"+sdf);
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			chgDate =	java.sql.Timestamp.valueOf(sdf1.format(date).toString()+ " 00:00:00.0");
			System.out.println("Change DB date=>"+chgDate);
			
			sql="UPDATE TAXTRAN SET TAX_FORM= ?,TAX_FORM_DATE=? WHERE TRAN_CODE=? AND TRAN_ID= ? AND LINE_NO= ? AND LINE_NO__TAX=? ";
			System.out.println("Update Sql:::["+sql+"]");
			System.out.println("Setting Parameter..........");
			System.out.println("taxForm:::["+taxForm+"]");
			System.out.println("tranCode:::["+tranCode+"]");
			System.out.println("chgDate:::["+chgDate+"]");
			System.out.println("tranId:::["+tranId+"]"); 
			System.out.println("lineNo:::["+lineNo+"]");
			System.out.println("lineNoTax:::["+lineNoTax+"]");
			
			
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,taxForm);
			pstmt.setTimestamp(2,chgDate);
			pstmt.setString(3,tranCode);
			pstmt.setString(4,tranId);
			pstmt.setString(5,lineNo);
			pstmt.setString(6,lineNoTax);
			int update = pstmt.executeUpdate();
			System.out.println("No Of Record Update["+update+"]");	
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	private void calTaxFormRecoMultiUpdate(String tranCode,String tranId,String taxForm,String taxFormDate,Connection conn)//Added by Jaimin 03/09/2007 requst-ID :DI78DIS028
	{
		PreparedStatement pstmt=null;
		String sql=null;
		System.out.println("calTaxFormRecoMultiUpdate() method Called");		
		try
		{ 
			System.out.println("===================tranCode"+tranCode);
			System.out.println("===================tranId"+tranId);			
			System.out.println("===================taxForm"+taxForm);
			System.out.println("===================conn"+conn);


			Timestamp chgDate=null;
			Object date = null;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("Date=====================Date"+taxFormDate);
			System.out.println("===================sdf"+sdf);
			date = sdf.parse(taxFormDate);			
			System.out.println("===================sdf"+sdf);
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			chgDate =	java.sql.Timestamp.valueOf(sdf1.format(date).toString()+ " 00:00:00.0");
			System.out.println("Change DB date=>"+chgDate);
			
			sql="UPDATE TAXTRAN SET TAX_FORM= ?,TAX_FORM_DATE=? WHERE TRAN_CODE=? AND TRAN_ID= ? ";
			System.out.println("Update Sql:::["+sql+"]");
			System.out.println("Setting Parameter..........");
			System.out.println("taxForm:::["+taxForm+"]");
			System.out.println("tranCode:::["+tranCode+"]");
			System.out.println("chgDate:::["+chgDate+"]");
			System.out.println("tranId:::["+tranId+"]"); 
			
			
			
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,taxForm);
			pstmt.setTimestamp(2,chgDate);
			pstmt.setString(3,tranCode);
			pstmt.setString(4,tranId);			
			int update = pstmt.executeUpdate();
			System.out.println("No Of Record Update["+update+"]");	
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}//End by jaimin 03/09/2007
}//end of ejb