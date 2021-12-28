	
/********************************************************
	Title		:	TaxInputCrtXmlPrcEJB
	Developed	:	Manazir 
	Date		:	01/09/09

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
import ibase.system.config.AppConnectParm;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class TaxInputCrtXmlPrc extends ProcessEJB implements TaxInputCrtXmlPrcLocal,TaxInputCrtXmlPrcRemote //SessionBean
{
	Connection conn = null,connITM2INI=null;
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
	int count=0;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		//System.out.println("Create Method Called....");
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
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";		
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				//System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				//System.out.println("detailDom" + detailDom);
			}
			retStr = taxInputCtrXmlPrc(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			//System.out.println("Exception :TaxInputCrtXmlPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}//END OF PROCESS (1)

	public String taxInputCtrXmlPrc(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{		
		String retStr = "";
		PreparedStatement pstmt = null,pstmtItem=null;
		PreparedStatement UpdatePstmt = null;
		PreparedStatement UpdateDtPstmt = null;
		PreparedStatement pstmtSiteReg = null;
		ResultSet rs = null,rsSiteReg=null;
		String sql = "" ,sql1 = "",errString = "",sqlItem=""; 	   
	    Timestamp tranDate =null;		
		connDriver = new ConnDriver();
		String userId = "";		
		String errorString = "";		
		int parentNodeListLength = 0;		
		int childNodeListLength = 0;
		int no = 0;
		int cntstatusUn = 0;
		int noOfrowSel = 0;		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;	
		String loginSiteCode="",empCode="",chgUser="",chgTerm="",loginCode="",custCode="";
		StringBuffer xmlBuff = new StringBuffer();
		InitialContext ctx = null;					
		AppConnectParm appConnect = new AppConnectParm();			
		String validUpTo="",effFrom="",siteCodeInv="",CustSiteCode="",invoiceId="",suppName="",tranDateStr="",SqlSiteReg="",siteRegno="",custSiteRegno="",tranDateprd="";
		Timestamp effFromTs =null,validUpToTs=null;
		//Timestamp tranDate=null;
		double taxAmt =0.00,taxAbleAmt=0.00;
		String custSiteCodeTo= "",custSiteCodeFr="";
		String returnErrorString="",currDate="";
		StringTokenizer stringTokenizer = null;		
		String modifiedInvSiteCode="" ,invSiteCode="",fileName="";

		try
		{
			
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");		
			
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//connITM2INI = connDriver.getConnectDB("DriverCP");
			connITM2INI = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			
			connDriver = null;
			connITM2INI.setAutoCommit(false);
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;			
			String currAppdate = null,quotNo = "",quotNoDup = "";			
			SimpleDateFormat sdf = null;
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			currDate = simpleDateFormat.format(currDateTs).toString();			
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");			
			custSiteCodeFr = genericUtility.getColumnValue("cust_site_code_fr",detailDom);
			custSiteCodeFr = custSiteCodeFr==null?"":custSiteCodeFr;
			
			//custSiteCodeTo = genericUtility.getColumnValue("cust_site_code_to",detailDom);
			//custSiteCodeTo = custSiteCodeTo==null ?"":custSiteCodeTo;

			// changes on the 10/09/09

			invSiteCode = genericUtility.getColumnValue("inv_site_code",detailDom);
			invSiteCode = invSiteCode==null ?"":invSiteCode;

			stringTokenizer = new  StringTokenizer(invSiteCode ,",");
			while(stringTokenizer.hasMoreElements())
			{
				modifiedInvSiteCode = modifiedInvSiteCode + "'"+stringTokenizer.nextToken()+"'," ;
			}
			modifiedInvSiteCode = modifiedInvSiteCode.substring(0,modifiedInvSiteCode.length()-1);

			// end of changes on 10/09/09

			effFrom = genericUtility.getColumnValue("from_date",detailDom);	
			effFrom = effFrom + " 00:00:00"; 
			System.out.println("effFrom"+effFrom);
			effFromTs = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(effFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + effFrom.substring(8) + ".0");			
			validUpTo = genericUtility.getColumnValue("valid_upto",detailDom);	
			validUpTo = validUpTo + " 00:00:00";
			validUpToTs = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(validUpTo,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + validUpTo.substring(8) + ".0");			
			xmlBuff.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
			xmlBuff.append("\t<PurchaseDetails>\n");
			if(custSiteCodeFr.trim().length()>0 && custSiteCodeFr!=null &&  modifiedInvSiteCode.trim().length()>0 && modifiedInvSiteCode!=null)
			{
				xmlBuff.append(getTaxCrtXmlValue(custSiteCodeFr,modifiedInvSiteCode,effFromTs,validUpToTs,conn ));
			}
			if(custSiteCodeFr.trim().length()>0 && custSiteCodeFr!=null &&  modifiedInvSiteCode.trim().length()>0 && modifiedInvSiteCode!=null)
			{
				xmlBuff.append(getTaxCrtXmlValue(custSiteCodeFr,modifiedInvSiteCode,effFromTs,validUpToTs,connITM2INI ));
			}			
			xmlBuff.append("\n</PurchaseDetails>");
			count=0;
			//start comment on 30/09/09
			/*String fileName = "C:"+File.separator+"CNF"+File.separator+"401"+File.separator+"INPUT_TAX_XML_FILES"+File.separator+"invxml_"+custSiteCodeFr+"_"+currDate+".xml ";
			
			File file = new File(fileName);
			FileOutputStream fileOutput = new FileOutputStream(file);
			PrintWriter  printWriter = new PrintWriter(fileOutput,true);			
			printWriter.println(xmlBuff.toString());
			*/
			//end comment on 30/09/09
			// changes on 30/09/09 
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse( "/IBASEHOME/ibase.xml" );
			String taxInputCerdStr = dom.getElementsByTagName( "tax_input_credit" ).item(0).getFirstChild().getNodeValue();
			fileName = taxInputCerdStr + File.separator+"invxml_"+custSiteCodeFr+"_"+currDate+".xml";
			dom = null;
			db = null;
			dbf = null;
			File taxDir = new File( fileName );
			File subDirFile = null;
			// 19/01/10 manoharan both on linux and windows should work
			/*
			if( !taxDir.exists() )
			{
				if(taxInputCerdStr.indexOf("\\") > 0)
				{
					String subDirs[] = taxInputCerdStr.split( "\\\\" );
					String subDirName = subDirs[0];
					
					int subDirLen = subDirs.length;
					for( int idx = 1; idx < subDirLen; idx++ )
					{
						subDirName = subDirName + File.separator + subDirs[ idx ]; 
						subDirFile = new File( subDirName );
						if( !subDirFile.exists() )
							subDirFile.mkdir();
					}
				}
				if(taxInputCerdStr.indexOf("//") > 0)
				{
					String subDirs[] = taxInputCerdStr.split( "////" );
					String subDirName = subDirs[0];
					
					int subDirLen = subDirs.length;
					for( int idx = 1; idx < subDirLen; idx++ )
					{
						subDirName = subDirName + File.separator + subDirs[ idx ]; 
						subDirFile = new File( subDirName );
						if( !subDirFile.exists() )
							subDirFile.mkdir();
					}
				}
				subDirFile = null;
			}			
			*/

			if( !taxDir.exists() )
			{
				String subDirName = "";
				
				if (taxInputCerdStr.indexOf("\\") > 0)
				{
					String subDirs[] = taxInputCerdStr.split( "\\\\" );
					subDirName = subDirs[0];
				   System.out.println("windows stype path");
					for( int idx = 1; idx < subDirs.length; idx++ )
					{
						subDirName = subDirName + File.separator + subDirs[ idx ]; 
						System.out.println("subDirs[ idx ] [" + subDirs[ idx ] + "]");
					}
				}
				else
				{
					String subDirs[] = taxInputCerdStr.split( "/" );
					subDirName = subDirs[0];
					System.out.println("Unix style path");
					for( int idx = 1; idx < subDirs.length; idx++ )
					{
						subDirName = subDirName + File.separator + subDirs[ idx ];
						System.out.println("subDirs[ idx ] [" + subDirs[ idx ] + "]");
					}
					
				}
				subDirFile = new File( subDirName );
				System.out.println("File path [" + subDirName + "]");
				if( !subDirFile.exists() )
				{
					System.out.println("creating folders [" + subDirName + "]");
					subDirFile.mkdirs();
				}
			}
			// end 19/01/10 manoharan both on linux and windows should work
			
			FileOutputStream fileOutput = new FileOutputStream(taxDir);
			PrintWriter  printWriter = new PrintWriter(fileOutput,true);			
			printWriter.println(xmlBuff.toString());
			taxDir = null;	
			// end of changes on 30/09/09
			conn.close();
			conn=null;
			connITM2INI.close();
			connITM2INI=null;

		} // end of try code 
	   	catch(Exception e)
		{
				try{
				conn.rollback();
				}catch(Exception e1)
				{
					e1.printStackTrace();
					//new throw Exception(e1);
				}				
			   //System.out.println("Exception in TaxInputCrtXmlPrc..."+e.getMessage());
			   e.printStackTrace();
			   errorString = e.getMessage();
				throw  new ITMException(e);
		}		
		finally
		{ 
			try
			{
				
				if(conn != null)conn.close();
				conn = null;	
			}
			catch(SQLException sqle)
			{
				sqle.printStackTrace();
			//	throw new  Exception(sqle);

			}
		}

		//System.out.println("returning from  TaxInputCrtXmlPrc   "+xmlBuff.toString());
		returnErrorString = "<?xml version=\"1.0\"?><Root><Errors><error id=\"VTPROCESS1\" type=\"P\" column_name=\"\"><message>Xml Generate Successfully</message><description>Xml File Name \n invxml_"+custSiteCodeFr+"_"+currDate+".xml \t \n </description><type>P</type><option>Y</option><time>null</time><alarm>null</alarm><source>null</source><trace>"+fileName+"..</trace><redirect>1</redirect></error></Errors></Root>"; 
		return returnErrorString ;	
		//return itmDBAccessEJB.getErrorString("","VTSALORSUC",userId);
	} //end process	
	public String getTaxCrtXmlValue(String custSiteCode ,String modifiedInvSiteCode ,Timestamp effFromTs , Timestamp validUpToTs , Connection conn)throws RemoteException,ITMException
	{
		 Timestamp tranDate =null;
		PreparedStatement pstmt = null;		
		String custSiteCodeInvc ="";
		PreparedStatement pstmtSiteReg = null;
		ResultSet rs = null,rsSiteReg=null;
		StringBuffer xmlBuff = new StringBuffer();		
		String sql="",siteCodeInv="",CustSiteCode="",invoiceId="",suppName="",tranDateStr="",SqlSiteReg="",siteRegno="",custSiteRegno="",tranDateprd="";
		double taxAmt =0.00,taxAbleAmt=0.00;		
		String tranDateInv = "";
		try{
		
			sql="	select a.site_code,d.site_code cust_site,A.invoice_id,A.tran_date,f.supp_name, "
				+"	round(sum(e.taxable_amt),2) taxable_amt,round(sum(e.tax_amt),2) tax_amt "
				+"	from   customer d, supplier f, taxtran e, invoice a "
				+"	where  a.site_code in ("+modifiedInvSiteCode+") " 
				+"	and    a.tran_date between ? and ? "
				+"	and    f.site_code = a.site_code "
				+"	and    e.tran_code = 'S-INV' "
				+"	and    e.tran_id = a.invoice_id "
				+"	and    e.tax_perc <> 0 "
				+"	and    e.tax_amt > 0 "
				+"	and    d.cust_code = a.cust_code "
				+"	AND    D.site_code = ?  "
				+"	AND    D.channel_partner = 'Y' "
				+"	group  by a.site_code,d.site_code,a.invoice_id, "
				+"	a.tran_date,f.supp_name "
				+"	order by a.tran_date,a.invoice_id ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,effFromTs);  // need to change for date 
				pstmt.setTimestamp(2,validUpToTs); // need to change for date 
				pstmt.setString(3,custSiteCode);
				rs = pstmt.executeQuery();
			while(rs.next())
			{
				count = count +1 ; // from no of retrieve
				siteCodeInv = rs.getString("site_code")==null?"":rs.getString("site_code");
				custSiteCodeInvc = rs.getString("cust_site")==null?"":rs.getString("cust_site");
				invoiceId = rs.getString("invoice_id")==null?"":rs.getString("invoice_id");
				tranDate = rs.getTimestamp("tran_date"); // db fromate 
				suppName = rs.getString("supp_name")==null?"":rs.getString("supp_name");
				taxAbleAmt = rs.getDouble("taxable_amt");
				taxAmt = rs.getDouble("tax_amt");				
				SqlSiteReg = "select reg_no from siteregno where site_code = ? and trim(ref_code) = 'LST/001'" ;
				pstmtSiteReg =  conn.prepareStatement(SqlSiteReg);
				pstmtSiteReg.setString(1,siteCodeInv);
				rsSiteReg = pstmtSiteReg.executeQuery();
				if(rsSiteReg.next())
				{
						siteRegno = rsSiteReg.getString("reg_no")==null?"":rsSiteReg.getString("reg_no");
				
				}
				rsSiteReg.close();
				rsSiteReg=null;
				pstmtSiteReg.close();
				pstmtSiteReg=null;
				
				SqlSiteReg = "select reg_no  from siteregno where site_code = ? and trim(ref_code) = 'LST/001' " ;
				pstmtSiteReg =  conn.prepareStatement(SqlSiteReg);
				pstmtSiteReg.setString(1,custSiteCodeInvc);
				rsSiteReg = pstmtSiteReg.executeQuery();
				if(rsSiteReg.next())
				{
						custSiteRegno = rsSiteReg.getString("reg_no")==null?"":rsSiteReg.getString("reg_no");
				
				}
				rsSiteReg.close();
				rsSiteReg=null;
				pstmtSiteReg.close();
				pstmtSiteReg=null;

				xmlBuff.append("\t\t<PurchaseInvoiceDetails>\n");
				tranDateStr = tranDate.toString();
				tranDateprd = tranDateStr.substring(0,4)+tranDateStr.substring(5,7);
				tranDateInv = tranDateStr.substring(0,10);
				xmlBuff.append("\t\t\t<TinNo><![CDATA[").append(custSiteRegno).append("]]></TinNo>\n");
				xmlBuff.append("\t\t\t<RetPerdEnd><![CDATA[").append(tranDateprd).append("]]></RetPerdEnd>\n");
				xmlBuff.append("\t\t\t<Sno><![CDATA[").append(count).append("]]></Sno>\n");
				xmlBuff.append("\t\t\t<SelName><![CDATA[").append(suppName).append("]]></SelName>\n");
				xmlBuff.append("\t\t\t<SelTin><![CDATA[").append(siteRegno).append("]]></SelTin>\n");
				xmlBuff.append("\t\t\t<InvNo><![CDATA[").append(invoiceId).append("]]></InvNo>\n");
				xmlBuff.append("\t\t\t<InvDate><![CDATA[").append(tranDateInv).append("]]></InvDate>\n");
				xmlBuff.append("\t\t\t<NetVal><![CDATA[").append(taxAbleAmt).append("]]></NetVal>\n");
				xmlBuff.append("\t\t\t<TaxCh><![CDATA[").append(taxAmt).append("]]></TaxCh>\n");
				xmlBuff.append("\t\t</PurchaseInvoiceDetails>\n");			
				
			}
		}
		catch(Exception e)
		{
			try{
			conn.rollback();
			}catch(Exception e1)
			{
				e1.printStackTrace();
				//new throw Exception(e1);
			}				
		  // System.out.println("Exception in TaxInputCrtXmlPrc..."+e.getMessage());
		   e.printStackTrace();
		   errorString = e.getMessage();
			throw  new ITMException(e);
		}	
		return xmlBuff.toString();
	} // end of  getTaxCrtXmlValue()
}//end class