
/********************************************************
	Title : BenefitDisPrcEJB
	Date  : 08-07-09	

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
import java.util.StringTokenizer;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class BenefitDisPrc extends ProcessEJB implements BenefitDisPrcLocal,BenefitDisPrcRemote 
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
		ResultSet rs = null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmtItem=null;
		Statement st = null;
		StringBuffer valueXmlString = new StringBuffer();			
		ConnDriver connDriver = new ConnDriver();	
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String userId = "",benefitType  ="",tranId="",despId="",sqlItemCodeAl="",sqlItem="";	
		int count = 0;
		try
		{
			ConnDriver conndriver = new ConnDriver();			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 			
			connDriver = null;
			tranId  = genericUtility.getColumnValue("tran_id",headerDom);	
			tranId = tranId == null ? "" :tranId.trim();
			benefitType  = genericUtility.getColumnValue("benefit_type",headerDom);					
			if(benefitType == null )
			{
				
				errString = itmDBAccessEJB.getErrorString("","VTBENUL",userId,"",conn);
				resultString =errString ;
				return resultString;
			}	
			
			/*
			sql = " select count(*)  from benefit_trace	where  ref_ser = 'S-DSP' and	ref_no  = ? ";
			*/
			//Benefit_type condition is Added in select query by Chandni Shah - 14/09/10   ---DI89UNI060
			
			sql = " select count(*)  from benefit_trace	where benefit_type = ? and ref_ser = 'S-DSP' and	ref_no  = ? ";
			pstmt = conn.prepareStatement(sql);	
			pstmt.setString(1,benefitType.trim());
			pstmt.setString(2,tranId.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;			
			if(count > 0 )
			{				
				errString = itmDBAccessEJB.getErrorString("","VTIBENT",userId,"",conn);
				resultString =errString ;
				return resultString;
				
			}
			else
			{
				if(benefitType.trim().equalsIgnoreCase("AL"))
				{				
					sql = "	select distinct rtrim(tran_id) tran_id, rtrim(x.item_code) item_code, "
						+"	rtrim(file_no) file_no, rtrim(al_no) al_no, rtrim(x.custom_descr) item_descr, "
						+"	x.quantity quantity,x.quantity_exp export_qty,x.valid_upto exp_date  "
						+"	from "
						+"	(SELECT a.tran_id,a.file_no,d.item_code,al_no,a.site_code, "
						+"	a.eff_from ,a.extend_upto, a.valid_upto, a.ext_valid1, a.ext_valid2, a. ext_valid3, "
						+"	c.fin_entity,d.custom_descr,d.quantity,d.quantity_exp "
						+"	FROM adv_licence a,site b,finent c ,adv_licence_exp d "
						+"	where a.tran_id = d.tran_id and 	a.site_code = b.site_code "
						+"	and b.fin_entity = c.fin_entity and a.status = 'O' order by d.item_code) X, "
						+"	(select p.desp_id,s.item_code,p.site_code,r.fin_entity,eff_date "
						+"	from despatch p,site q,finent r, despatchdet s  "
						+"	where p.desp_id = s.desp_id and  "
						+"	p.site_code = q.site_code and  "
						+"	q.fin_entity = r.fin_entity order by s.item_code) Y  "
						+"	where x.eff_from <= y.eff_date "
						+"	and  ((x.valid_upto >= y.eff_date and x.valid_upto is not null ) "
						+"	OR   (x.extend_upto >= y.eff_date and x.extend_upto is not null )  "
						+"	OR   (x.ext_valid1 >= y.eff_date and x.ext_valid1 is not null)  "
						+"	OR	  (x.ext_valid2 >= y.eff_date and x.ext_valid2 is not null)   "
						+"	OR	  (x.ext_valid3 >= y.eff_date and x.ext_valid3 is not null) )  "
						+"	and 	x.item_code in (select item_code from item where item_code__al = (select item_code__al from item where item_code = y.item_code)) "
						+"	and   x.quantity > x.quantity_exp "
						+"	and	y.desp_id =  ? order by item_code ";
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,tranId.trim());
					rs = pstmt.executeQuery();
					while(rs.next()) 
					{
						
						valueXmlString.append(rs.getString(1)==null ?"":rs.getString(1)).append("\t");
						valueXmlString.append(rs.getString(2)==null ?"":rs.getString(2)).append("\t");
						valueXmlString.append(rs.getString(3)==null ?"":rs.getString(3)).append("\t");
						valueXmlString.append(rs.getString(4)==null ?"":rs.getString(4)).append("\t");
						valueXmlString.append(rs.getString(5)==null ?"":rs.getString(5)).append("\t");
						valueXmlString.append(rs.getString(6)==null ?"":rs.getString(6)).append("\t");
						valueXmlString.append(rs.getString(7)==null ?"":rs.getString(7)).append("\t");
						valueXmlString.append(rs.getString(8)==null ?"":rs.getString(8)).append("\t");
						valueXmlString.append("\n");					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					resultString = valueXmlString.toString();
					System.out.println("valueXmlString.append(valueXmlString.append()"+resultString.trim());

				} // end of first case
				if(benefitType != null && benefitType.trim().equalsIgnoreCase("DP"))
				{
					sql="	SELECT tran_id,file_no,x.valid_upto exp_date from "
						+"	(select tran_id,file_no,a.site_code__depb,a.eff_date,a.valid_upto,"
						+"	c.fin_entity FROM depb a,site b,finent c "
						+"	where a.site_code__depb = b.site_code "
						+"	and b.fin_entity = c.fin_entity "
						+"	and a.status = 'O') X, "
						+"	(select p.desp_id,p.site_code,r.fin_entity,eff_date from despatch p,site q,finent r "
						+"	where p.site_code = q.site_code and "
						+"	q.fin_entity = r.fin_entity) Y "
						+"	where x.fin_entity = y.fin_entity "
						+"	and  	x.eff_date <= y.eff_date 	"
						+"	and   x.valid_upto >= y.eff_date "
						+"	and	y.desp_id = ? " ;
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,tranId.trim());
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						valueXmlString.append(rs.getString(1)==null ?"":rs.getString(1).trim()).append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append(rs.getString(2)==null ?"":rs.getString(2).trim()).append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append(rs.getTimestamp(3)).append("\t");																	
						valueXmlString.append("\n");					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					resultString = valueXmlString.toString();
				}
				System.out.println("resultString"+resultString.trim());
				/*if(benefitType != null && benefitType.trim().equalsIgnoreCase("DR"))
				{
					sql="	SELECT tran_id,account_no_edi,location FROM "
						+"	(SELECT a.tran_id,a.account_no_edi,a.site_code__edi,a.tran_date, "
						+"	c.fin_entity,a.location FROM drawback a,site b,finent c "
						+"	where a.site_code__edi = b.site_code "
						+"	and b.fin_entity = c.fin_entity "
						+"	and a.status = 'O') X, "
						+"	(select p.desp_id,p.site_code,r.fin_entity,eff_date "
						+"	from despatch p,site q,finent r "
						+"	where p.site_code = q.site_code and "
						+"	q.fin_entity = r.fin_entity) Y "
						+"	where x.fin_entity = y.fin_entity "
						+"	and  	x.tran_date <= y.eff_date 	"
						+"	and	y.desp_id = ? ";
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,despId.trim());
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						valueXmlString.append(rs.getString(1)==null ?"":rs.getString(1).trim()).append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append(rs.getString(2)==null ?"":rs.getString(2).trim()).append("\t");
						valueXmlString.append(rs.getString(3)==null ?"":rs.getString(3).trim()).append("\t");					
						valueXmlString.append("\n");					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					resultString = valueXmlString.toString();				
				}*/
				 
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
			System.out.println("Exception :BenefitDisPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retStr;
	}//END OF PROCESS (1)

	
	
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{		
		String retStr = "";
		PreparedStatement pstmt = null;
		PreparedStatement UpdatePstmt = null;
		PreparedStatement UpdateDtPstmt = null;
		PreparedStatement AprvPstmt = null,pstmtThr=null,pstmtSec= null;
		ResultSet rs = null,rsSec=null,rsThr=null;
		String sql = "" ,sql1 = "",errString = "";
        String retString = "" ,errCode = "",tranId="";
		String loginSiteCode="",empCode="",chgUser="",chgTerm="",loginCode="",termid = "";
		String 	policyNo="",insCertNo="",agent_code="",invoiceId="";
		String confirm ="";
		String invId = "" ;
		String conf = "";
		int count =0,cnt =0;
		String curr_code="",dayNo = "",shiftOfDay = "";
	    double exch_rate=0,netAmt=0;
	    Timestamp tranDate =null;
		String siteCode = "",wokCntr = "",datefrom = "",dateto = "",holTblno = "",insertQry = "";
		connDriver = new ConnDriver();
		String userId = "";		
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
		// added valriable
		// add on 14/10/09 
		//String advLicMulTranId[] = new String[99999];
		ArrayList advLicMulTranId = new ArrayList();
		// end of code on 14/10/09
		String merrcode = "";		
		String qcTableNo = "";
		String tableDescr = "" ;
		Timestamp exprDate = null;
		Timestamp chgDate = null;
		String specCode = "";
		String expctedRes = "";
		String sqlSpec = "";
		String descr = "";		
		String benefitType = "";		
		String str = "";
		String despCurrCode = "";
		String keyString = "";
		String alType = "";
		String ls_advlic_line_no = "";
		String ls_pocldet_line_no = "";				
		String currCode = "";
		String exchRate = "";
		double exchRateDec = 0.0;		
		String value ="";
		double valueDec =0.00;
		String qtyImp = "";
		double qtyImpDec = 0.00;
		String amtImp = "";
		double amtImpDec = 0.00;
		String qty = "";
		double qtyDec =0.00;
		String amt = "";
		double amtDec=0.0;
		double  baseValue = 0.00;	
		String fileNo = "";
		String advCurrCode = "";
		String despId = "";
		double returnExchRateDec = 0.00;
		String sqlInsert="",exchangeRate="",exchangeRateStr="";
		// comment on 15/10/09 for changes 
		//String[] asAdvLic = new String[999999];
		String advLicTranId ="",sqlSec="",sqlThr="",msgType="",resultString="";
		//double[] exchRateArry = new double[99999];
		ArrayList exchRateArry = new ArrayList();
		//String[]  currCodeArry =  new String[999999];
		ArrayList currCodeArry = new ArrayList();
		//String[] fileNoArry = new String[999999];
		ArrayList fileNoArry = new ArrayList();
		
		String advLicStr = "";
		double mexch_rate =0;
		int cntSec = 0,i=0;
		// end of code 		
		try
		{	
		
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
			connDriver = null;
			Timestamp currDate = null;
			String currAppdate = null,quotNo = "",quotNoDup = "";
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;			
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();			
			currAppdate = genericUtility.getValidDateString(currAppdate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());			
			currDate = Timestamp.valueOf(currAppdate + " 00:00:00.00");						
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();		
			benefitType = genericUtility.getColumnValue("benefit_type",headerDom);
			benefitType = benefitType == null ? "" :benefitType.trim();
			tranId = genericUtility.getColumnValue("tran_id",headerDom);			
			tranId = tranId == null ? "" :tranId.trim();
			exchangeRate = genericUtility.getColumnValue("exchange_rate",headerDom);
			exchangeRate = exchangeRate == null ? "1.000":exchangeRate.trim();		
			
			//-Added by Chandni Shah 26/08/10  ---DI89UNI060
			
			//if selectd despatch is confirmed and invoice is generated then only the process is completed
			sql="select confirmed from despatch where desp_id = ?  ";
			pstmt = conn.prepareStatement(sql);	
			pstmt.setString(1,tranId.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirm = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if ("Y".equalsIgnoreCase(confirm))
			{
				sql = "select invoice_id,confirmed from invoice where desp_id = ? ";
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,tranId.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					invId = rs.getString(1);
					conf = rs.getString(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (invId != null  && ("Y".equalsIgnoreCase(conf)) )
				{
			
					for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{
						parentNode = parentNodeList.item(selectedRow);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();				
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{					
							childNode = childNodeList.item(childRow);
							childNodeName = childNode.getNodeName();
										
							if (childNodeName.equals("tran_id"))
							{
								if(childNode.getFirstChild()!=null)
								{
									// add on 14/10/09
									//advLicMulTranId[selectedRow] = childNode.getFirstChild().getNodeValue();
									String advLicMulTranIdStr =  childNode.getFirstChild().getNodeValue();
									advLicMulTranId.add(advLicMulTranIdStr);
									//advLicMulTranId[selectedRow] = childNode.getFirstChild().getNodeValue();
									// end of code on 14/10/09
									System.out.println("advLicMulTranIdStr>>>>>"+advLicMulTranIdStr);
								}
							}	
							if (childNodeName.equals("exchange_rate"))
							{
								if(childNode.getFirstChild()!=null)
								{
									exchangeRate = childNode.getFirstChild().getNodeValue();
								}						
							}	
						}//inner for loop]	lotNoFromStr
						//if((exchangeRate==null || exchangeRate.length()==0 ) &&  benefitType.trim().equalsIgnoreCase("AL")) --- chandni 29/12/10
						if((exchangeRate==null || exchangeRate.length()==0 ) )
						{
							ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
							exchangeRateStr = itmDBAccessEJB.getErrorString("","VTEXHEMTY",userId,"",conn);					
							 return exchangeRateStr ;					
						}
					}// OUT FOR LOOP
					/////////////code to be migrated for dispatch//////////////////
					// all details is in site the 
					// changes by manazir on 30-05-09
					/*StringTokenizer stringTokenizer = null;
					stringTokenizer = new  StringTokenizer(advLicMulTranId[0] ,","); 
					// actually this is tranId of follwing datawindow
					int i=0;
					while(stringTokenizer.hasMoreElements())
					{
						 asAdvLic[i]=  Double.parseDouble(stringTokenizer.nextToken());	
						 i++;
					}*/
					// changes on 15/10/09  Chandni Shah
					//	asAdvLic =  advLicMulTranId ;
					System.out.println("advLicMulTranId.get(0)>>>"+advLicMulTranId.get(0));
					//	System.out.println("asAdvLic>>>"+asAdvLic[0]);
					sql="select curr_code, exch_rate  "
							+" from despatch where desp_id = ?  ";
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,tranId.trim());
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						despCurrCode = rs.getString(1) == null ?"":rs.getString(1);
						exchRate = rs.getString(2) == null ?"":rs.getString(2);
						if(!("".equalsIgnoreCase(exchRate) && exchRate != null ))
						{
							exchRateDec = Double.parseDouble(exchRate );
						}
						else{exchRateDec=0;}
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					/////////////////end of code////////////////////////
					//s_pass1.mdecimal = exchRateDec		
					//Added By Jasmina 22/10/08 -DI89SUN109
					//advLicTranId = gfGetSqlInString(asAdvLic);
					// changes on the 15/10/09 as ArrayList pass to the function 
					advLicTranId = gfGetSqlInString(advLicMulTranId);
					System.out.println(" advLicTranId ==============>>>>>>>>>>>>>>>>>>>>>"+advLicTranId);
					
					// changes on 13/09/10 -- Chandni Shah  ---DI89UNI060
					// advLicTranId is only for  benefit_type  == 'AL'
					if ("AL".equalsIgnoreCase(benefitType))
					{
						//advLicTranId = gfGetSqlInString(advLicMulTranId);
						//ls_sql = ' '
						//advLicTranId = asAdvLic[0];
						System.out.println(" return value of String parameter advLicTranId>>>"+advLicTranId);
						sql=" SELECT  count(1) from "
							+" (SELECT b.site_code,d.item_code,a.eff_from ,a.extend_upto, a.valid_upto, "
							+" d.quantity,d.quantity_exp, a.ext_valid1, a.ext_valid2, a. ext_valid3 "
							+" FROM adv_licence a,site b,finent c ,adv_licence_exp d  "
							+" where a.tran_id = d.tran_id and a.site_code = b.site_code and b.fin_entity = c.fin_entity and "
							+" a.tran_id in (" +advLicTranId+ ") and d.quantity > d.quantity_exp and a.status = 'O' ) X, "
							+"(select p.desp_id,s.item_code,eff_date, p.site_code from despatch p,site q,finent r, despatchdet s  "
							+" where p.desp_id = s.desp_id and p.site_code = q.site_code and q.fin_entity = r.fin_entity and "
							+" p.desp_id = ?  ) Y where x.eff_from <= y.eff_date "
							+" and x.site_code = y.site_code and "
							+"((x.valid_upto >= y.eff_date and x.valid_upto is not null )  "
							+"OR (x.extend_upto >= y.eff_date and x.extend_upto is not null) "
							+"OR (x.ext_valid1 >= y.eff_date and x.ext_valid1 is not null) "
							+"OR (x.ext_valid2 >= y.eff_date and x.ext_valid2 is not null)  "
							+"OR (x.ext_valid3 >= y.eff_date and x.ext_valid3 is not null) ) "
							+"and x.item_code in (select item_code from item where item_code__al = (select item_code__al from item where item_code = y.item_code)) ";
							pstmt = conn.prepareStatement(sql);	
							pstmt.setString(1,tranId.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt ==0)
							{
								sqlSec=" SELECT  count(1) FROM "
									+"	(SELECT b.site_code,d.item_code,a.eff_from ,a.extend_upto, a.valid_upto, "
									+"	d.quantity,d.quantity_exp, a.ext_valid1, a.ext_valid2, a. ext_valid3 "
									+"	FROM adv_licence a,site b,finent c ,adv_licence_exp d  "
									+"	where a.tran_id = d.tran_id and a.site_code = b.site_code and b.fin_entity = c.fin_entity and "
									+"	a.tran_id in (" +advLicTranId+") and d.quantity > d.quantity_exp and a.status = 'O' ) X, "
									+"	(select p.desp_id,s.item_code,eff_date, p.site_code from despatch p,site q,finent r, despatchdet s  "
									+"	where p.desp_id = s.desp_id and p.site_code = q.site_code and q.fin_entity = r.fin_entity and "
									+"	p.desp_id = ? ) Y where x.eff_from <= y.eff_date "
									+"	and ((x.valid_upto >= y.eff_date and x.valid_upto is not null )  "
									+"	OR (x.extend_upto >= y.eff_date and x.extend_upto is not null) "
									+"	OR (x.ext_valid1 >= y.eff_date and x.ext_valid1 is not null) "
									+"	OR	(x.ext_valid2 >= y.eff_date and x.ext_valid2 is not null)  "
									+"	OR	(x.ext_valid3 >= y.eff_date and x.ext_valid3 is not null) ) "
									+"	and x.item_code in (select item_code from item where item_code__al = (select item_code__al from item where item_code = y.item_code))  ";
								pstmtSec = conn.prepareStatement(sqlSec);	
								pstmtSec.setString(1,tranId.trim());
								rsSec = pstmtSec.executeQuery();
								if(rsSec.next())
								{
									cntSec = rsSec.getInt(1);
								}
								rsSec.close();
								rsSec = null;
								pstmtSec.close();
								pstmtSec = null;	
								if(cntSec > 0)
								{
									sqlThr = "select msg_type "
										+" from messages_level "
										+ "where profile_id = ?  "   ////////////????????????????
										+" and msg_no = 'VNOALSITE' "
										+ "and UPPER(win_name) = 'W_DESPATCH' ";
									pstmtThr = conn.prepareStatement(sqlThr);	
									pstmtThr.setString(1,chgUser.trim());
									rsThr = pstmtThr.executeQuery();
									if(rsThr.next())
									{
										msgType = rsThr.getString(1) == null ?"":rsThr.getString(1);
									}
									rsThr.close();
									rsThr = null;
									pstmtThr.close();
									pstmtThr = null;
									if(msgType==null || msgType.trim().length()==0)
									{
										sqlThr = "select msg_type "
											+" from messages "
											+ "where  msg_no =  'VNOALSITE' "	;
										pstmtThr = conn.prepareStatement(sqlThr);	
										rsThr = pstmtThr.executeQuery();
										if(rsThr.next())
										{
											msgType = rsThr.getString(1) == null ?"":rsThr.getString(1);
										}
										rsThr.close();
										rsThr = null;
										pstmtThr.close();
										pstmtThr = null;						
									}
									if(msgType.equalsIgnoreCase("E"))
									{
										errString = itmDBAccessEJB.getErrorString("","VNOALSITE",userId,"",conn);
										resultString =errString ;
										return resultString ;							
									}						
								}
								else
								{
									errString = itmDBAccessEJB.getErrorString("","VNOAL",userId,"",conn);
									resultString =errString ;
									return resultString ;						
								}
							}	// end of if cnt==0
							// comment on 15/10/09 
							//for(i=0;i<asAdvLic.length ; i++)
							for(i=0; i<advLicMulTranId.size(); i++)						
							{
								advLicStr = (String)advLicMulTranId.get(i);
								System.out.println("advLicMulTranId.get(i) "+advLicMulTranId.get(i));
								System.out.println("get From ArrayList "+advLicStr);
								sql= "select curr_code, file_no "
									+"	from adv_licence where tran_id = ? ";
								pstmt = conn.prepareStatement(sql);	
								pstmt.setString(1,advLicStr);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									currCode = rs.getString("curr_code")==null ?"":rs.getString("curr_code");
									fileNo = rs.getString("file_no")==null ?"":rs.getString("file_no");
								}
								pstmt.close();
								pstmt =null;
								rs.close();
								rs=null;
								// add on 15/10/09
								
								//currCodeArry[i]=currCode;
								currCodeArry.add(currCode);
								fileNoArry.add(fileNo);
								//fileNoArry[i] = fileNo; 
								//exchRateArry[i] = exchRateDec;
								exchRateArry.add(new Double(exchRateDec));

								// end of add on 15/10/09
								//if(despCurrCode.equalsIgnoreCase(currCodeArry[i]))
								//{						
								//}	
								/*if (ls_desp_curr_code) <> (las_curr_code[li_cnt]) then
									s_pass1.mvar1 = asAdvLic[li_cnt]
									s_pass1.mvar2 = las_file_no[li_cnt]
									s_pass1.mvar3 = ls_desp_curr_code
									s_pass1.mvar4 = las_curr_code[li_cnt]
									openwithparm(w_benefit_trace,s_pass1)
									s_pass2 = message.powerobjectparm
									if s_pass2.mdecimal > 0 then 
										lcExchrate[li_cnt] = s_pass2.mdecimal
									end if
								end if
								next*/
							}
					} // end of IF stmt for benefit_type = 'AL'
						if(advLicTranId.trim().length() > 0 && benefitType.trim().equalsIgnoreCase("AL"))
						{
							// merrcode = advanceLicence(tranId ,asAdvLic, exchRateArry , conn);
							System.out.println("call the advanceLicence() method  ");
							merrcode = advanceLicence(tranId ,advLicMulTranId, exchRateArry , conn);
							
							System.out.println("Returned  merrcode>>>>>>>>>>>>>>>>>>"+merrcode );
							
						}
						if(advLicTranId.trim().length() > 0 && benefitType.trim().equalsIgnoreCase("DP"))
						{
							System.out.println("call the wfDepb() method  ");
							merrcode = wfDepb(tranId,advLicMulTranId , conn);
							System.out.println("Returned  merrcode>>>>>>>>>>>>>>>>>>"+merrcode );
						}				
						if ("Error".equalsIgnoreCase(merrcode))
						{
							conn.rollback();
							errString = itmDBAccessEJB.getErrorString("","VTERRNT",userId,"",conn);
							resultString =errString ;
							return resultString;					        
						}
						if ("Success".equalsIgnoreCase(merrcode))
						{
							errString = itmDBAccessEJB.getErrorString("","VTSUCCE",userId,"",conn);
							resultString =errString ;
							conn.commit();
							return resultString;					
														
						}	
				}
				else
				{
				errString = itmDBAccessEJB.getErrorString("","VTINVN ",userId,"",conn);
				resultString =errString ;
				return resultString ;	
				}
			}  // end of if stmt of confirmed
			else
			{
			errString = itmDBAccessEJB.getErrorString("","VTCONFN ",userId,"",conn);
			resultString =errString ;
			return resultString ;	
			}
			
		}
	   	catch(Exception e)
		{
			try{
			conn.rollback();
			}catch(Exception e1)
			{e1.printStackTrace();}
				
		   System.out.println("Exception in BenefitDisPrcEJB..."+e.getMessage());
		   e.printStackTrace();
		   errorString = e.getMessage();
		   throw new ITMException(e);
			   
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
				throw new ITMException(sqle);
			}
		}
		System.out.println("returning from  BenefitDisPrcEJB   "+merrcode);
		return merrcode;
	} //end process
	
	public String  advanceLicence(String despId, ArrayList lsAdvLic,ArrayList lcExchrate , Connection conn ) throws RemoteException, ITMException 
	{		
		String keyString = "";
		String alType = "";
		String ls_advlic_line_no = "";
		String ls_pocldet_line_no = "";
		String unit = "";
		String itemCode = "";
		String currCode = "";
		String exchRate = "";
		double exchRateDec = 0.0;
		String quantity  = "";
		double quantityDec = 0.00;
		String value ="";
		double valueDec =0.00;
		String qtyImp = "";
		double qtyImpDec = 0.00;
		String amtImp = "";
		double amtImpDec = 0.00;
		String qty = "";
		double qtyDec =0.00;
		String amt = "";
		double amtDec=0.0;
		double  baseValue = 0.00;
		String merrcode = "";
		String sql ="";
		PreparedStatement pstmt =null,pstmtSec = null,pstmtInsert=null,pstmt1 = null ;
		ResultSet rs = null;
		ResultSet rs1 = null;
		Timestamp  tranDate = null;
		tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		Timestamp chgDate = null;
		chgDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		double despQtyBal =0.00;
		String tranId = "";
		double[] acAxchrate = new double[100];
		String  lineNo = "";
		String alqty ="";
		double alqtyDec = 0.00;
		String itemCodeDeal = "";
		String  unitAl = "";
		int lcConv = 0;
		String siteCode= "";
		String despLineNo = "";
		double updqty = 0.00;
		double benefitValue = 0.00,lc_updqty=0,lc_alqty=0;
		String sqlSec = "";
		ResultSet rsItem = null,rsItemCodeAl = null;						
		PreparedStatement pstmtItem = null,pstmtItemCodeAl = null;
		//StringBuffer valueXmlString = null;
		DistCommon distCommon = new DistCommon();
		ArrayList convQuantityFactArryList = new ArrayList();
		// comment on 15/10/09
	//	double lcAlqtyexp[] = new double[999999];
		ArrayList lcAlqtyexp = new ArrayList();
		// changes on 14/10/09
		//String[] advLicMulTranId = new String[9999999]; 
		ArrayList advLicMulTranId = new ArrayList(); 
		// end of code 
		String sqlItemCodeAl="",sqlItem="", sqlInsert="";	
		String xmlValues="";
		//valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");	
		try
		{
			conn.setAutoCommit(false);
			// comment on 15/10/09
		//	for(int i=0; i<lsAdvLic.length ; i++)
			for(int i=0; i<lsAdvLic.size() ; i++)
			{
				lcAlqtyexp.add(new Double(0));
				System.out.println("lsAdvLic.get(i) "+(String)lsAdvLic.get(i));
			}
			System.out.println("add the value of "+lsAdvLic.size());
			sql ="select key_string  from transetup "
				+"	where upper(tran_window) = 'W_BENEFIT_TRACE' ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyString =rs.getString("key_string")==null?"": rs.getString("key_string");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql ="select key_string  from transetup "
				+"	where upper(tran_window) = 'GENERAL' ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyString =rs.getString("key_string")==null?"": rs.getString("key_string");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql =" select despatchdet.item_code , "  
				+" despatch.curr_code , "  
				+" despatch.exch_rate , "  
				+" despatch.site_code , "   
				+" quantity__stduom,"    
				+" quantity__stduom * rate__stduom, "
				+" despatchdet.line_no, "  
				+" despatchdet.unit__std " 
				+" from despatch,despatchdet  "	
				+" where ( despatchdet.desp_id = despatch.desp_id ) "
				+" and  ( ( despatch.desp_id = ? ) )  " 
				+" order by despatchdet.item_code "; 
			pstmt = conn.prepareStatement(sql);	
			pstmt.setString(1,despId);
			rs1 = pstmt.executeQuery();
			//if(rs.next())
			while (rs1.next())
			{
				 itemCode = rs1.getString(1)==null ?"":rs1.getString(1);
				 currCode =rs1.getString(2)==null ?"":rs1.getString(2);
				 exchRate = rs1.getString(3)==null ?"":rs1.getString(3);
				 siteCode =rs1.getString(4)==null ?"":rs1.getString(4);
				 quantity = rs1.getString(5)==null ?"":rs1.getString(5);
				 if(!("".equalsIgnoreCase(quantity)))
				{
				 quantityDec = Double.parseDouble(quantity);
				}
				value = rs1.getString(6)==null ?"":rs1.getString(6);
				if(!("".equalsIgnoreCase(value)))
				{
					valueDec  = Double.parseDouble(value);
				}
				 despLineNo=rs1.getString(7)==null ?"":rs1.getString(7);
				 unit =rs1.getString(8)==null ?"":rs1.getString(8);
			//}
			//rs.close();
			//rs = null;
			//pstmt.close();
			//pstmt = null;
			despQtyBal = quantityDec; 
			System.out.println("lsAdvLic.size() "+lsAdvLic.size() );
			//for(int i=0; i<lsAdvLic.size() ;  i++)
			//{
				//baseValue =  valueDec * acAxchrate[i];
				// added on 15/10/09
				int i = 0;
				baseValue = valueDec * ((Double)lcExchrate.get(i)).doubleValue();
				// end of code on 15/10/09
				sql="	select adv_licence.tran_id, adv_licence_exp.line_no, "
				+"	(case when adv_licence_exp.quantity is null then 0 else adv_licence_exp.quantity end) - "
				+"	(case when adv_licence_exp.quantity_exp is null then 0 else adv_licence_exp.quantity_exp end),"
				+"	(case when adv_licence_exp.quantity is null then 0 else adv_licence_exp.quantity end) "
				+"	from adv_licence_exp , adv_licence "
				+"	where adv_licence.tran_id =  ? "
				+"	and adv_licence_exp.tran_id = adv_licence.tran_id "
				+"	and  adv_licence_exp.item_code in (select item_code "
				+"	from item where item_code__al = (select item_code__al "
				+"	from item 	where item_code = ? )) "																							
				+"	and (case when adv_licence_exp.quantity is null then 0 else adv_licence_exp.quantity end) - "
				+"	(case when adv_licence_exp.quantity_exp is null then 0 else adv_licence_exp.quantity_exp end) > 0	";
				pstmt1 = conn.prepareStatement(sql);	
				//pstmt.setString(1,advLicMulTranId[i]);
				pstmt1.setString(1,(String)lsAdvLic.get(i));
				pstmt1.setString(2,itemCode);
				rs = pstmt1.executeQuery();
				System.out.println("ITEM CODE ==============="+itemCode);
				while(rs.next())
				{
					tranId = rs.getString(1) == null ?"":rs.getString(1);
					lineNo = rs.getString(2) == null ?"":rs.getString(2);
					qty = rs.getString(3)== null ?"":rs.getString(3);
					if(!("".equalsIgnoreCase(qty)))
					{
						qtyDec = Double.parseDouble(qty);
					}
					alqty = rs.getString(4)==null ?"":rs.getString(4);	
					if(!("".equalsIgnoreCase(alqty)))
					{
						alqtyDec = Double.parseDouble(alqty);
					}
					
					sqlItemCodeAl ="select item_code__al  "
						+" from item where item_code = ? ";	
					pstmtItemCodeAl = conn.prepareStatement(sqlItemCodeAl);	
					pstmtItemCodeAl.setString(1,itemCode);
					rsItemCodeAl = pstmtItemCodeAl.executeQuery();
					if(rsItemCodeAl.next())
					{
						itemCodeDeal =rsItemCodeAl.getString(1)==null ?"":rsItemCodeAl.getString(1);	
					}
					rsItemCodeAl.close();
					rsItemCodeAl = null;
					pstmtItemCodeAl.close();
					pstmtItemCodeAl = null;
					sqlItem= "select trim(unit)  from item "
						+" where item_code = ? ";	
						
					pstmtItem = conn.prepareStatement(sqlItem);	
					pstmtItem.setString(1,itemCodeDeal);
					rsItem = pstmtItem.executeQuery();
					System.out.println(" UNIT ========"+unit);
					if(rsItem.next())
					{
						unitAl = rsItem.getString(1) == null ?"" : rsItem.getString(1);
						System.out.println(" UNITALLL11 ========"+unitAl);
					}
					rsItem.close();
					rsItem = null;
					pstmtItem.close();
					pstmtItem = null;
					System.out.println(" UNITALLL ========"+unitAl);
					System.out.println("unit.equalsIgnoreCase(unitAl)[[[" +(unit.trim().equalsIgnoreCase(unitAl.trim())));
					if(!(unit.trim().equalsIgnoreCase(unitAl.trim())))
					{
						// ??? despQtyBal = gf_conv_qty_fact(unit, unitAl, itemCode, despQtyBal, lcConv) ; 
						convQuantityFactArryList = distCommon.getConvQuantityFact(unit,unitAl,itemCode,despQtyBal,lcConv,conn);
						System.out.println("convQuantityFactArryList[[["+convQuantityFactArryList.get(0));
						String despQtyBalStr = (String)convQuantityFactArryList.get(0);
						despQtyBal = Double.parseDouble(despQtyBalStr);
						/*?gf_conv_qty_fact(ls_unit, ls_unital, ls_item_code, lc_DespQtyBal, lcConv)*/						
					}
					else
					{
						lcConv = 1 ;
					}					
					if(qtyDec <= despQtyBal )
					{
						updqty = qtyDec;
						despQtyBal = despQtyBal - qtyDec;						
					
					}
					else
					{
						
						updqty = despQtyBal ;
						despQtyBal = 0 ;					
					}
					if (((Double)lcAlqtyexp.get(i)).doubleValue() + updqty > alqtyDec )
					{
						continue;
					}										
					//((Double)lcAlqtyexp.get(i)).doubleValue() = ((Double)lcAlqtyexp.get(i)).doubleValue() + updqty ;
					System.out.println("break1");
					lcAlqtyexp.add(i,new Double( ((Double)lcAlqtyexp.get(i)).doubleValue() + updqty ));
					System.out.println("valueDec"+valueDec);
					System.out.println("quantityDec" +quantityDec);
					System.out.println("lcConv"+lcConv);
					System.out.println("updqty"+updqty);
					benefitValue = (valueDec / (quantityDec * lcConv)) * updqty  ;	
					System.out.println("benefitValue"+benefitValue);
					if(unitAl.equalsIgnoreCase(unit))
					{
						despQtyBal = despQtyBal/ lcConv ;
					
					}	
					System.out.println("break2"+i);
					//  ls_tran_id = gf_gen_key_nvo(lds_benefit,'S-BTR', 'tran_id', keystr)	 ??????//
					System.out.println("despQtyBal[[[["+despQtyBal);

					//????????????????????//
					/*if(despQtyBal == 0)
					{	 //errorString = "Quantity exported is more than advance license quantity";
						errorString = "Error";
						break;
					}*/
					errorString = "Success";
					if(errorString !=null && errorString.trim().length()>0)
					{
						System.out.println("break3" + lcExchrate.get(i));
						
						
						sqlSec = "update adv_licence_exp  "
							+"SET quantity_exp = 	(case when quantity_exp is null then 0 else quantity_exp end) + ?  , "
							+"exp_obl_exp		=	(case when exp_obl_exp is null then 0 else exp_obl_exp end)  +	( ?  *  ? ) "
							+"WHERE ( adv_licence_exp.tran_id  = ?  ) AND  "
							+"( adv_licence_exp.line_no 	= ?  ) " ;
						pstmtSec = conn.prepareStatement(sqlSec);
						pstmtSec.setDouble(1,updqty);
						pstmtSec.setDouble(2,benefitValue);
						pstmtSec.setDouble(3,((Double)lcExchrate.get(i)).doubleValue());
						pstmtSec.setString(4,(String)lsAdvLic.get(i)); 
						pstmtSec.setString(5,lineNo);
						int noUpdate  = pstmtSec.executeUpdate();
						pstmtSec.close();
						pstmtSec = null;
						xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
						xmlValues = xmlValues + "<Header></Header>";
						xmlValues = xmlValues + "<Detail1>";
						xmlValues = xmlValues +	"<tran_id></tran_id>";						
						xmlValues = xmlValues + "<tramn_date>"+tranDate+"</tramn_date>" ;
						xmlValues = xmlValues + "</Detail1></Root>";	
						String genTranId = generateTranId("w_benefit_trace", xmlValues, conn);
						System.out.println("genTranId >>>>>>>>>"+ genTranId);
						sqlInsert = " INSERT INTO benefit_trace ( "
									+"	tran_id, tran_date, adj_type, "
									+"	benefit_type, benefit_no, line_no__benefit,  "
									+"	ref_ser, ref_no, line_no__ref, "
									+"	item_code, unit, quantity, "
									+"	curr_code, exch_rate, benefit_value, "
									+"	status, chg_date, chg_user, "
									+"	chg_term, value__base, benefit_perc, "
									+"	exch_rate__base, item_code__desp, unit__desp, "
									+"	quantity__desp, conv__qty_alunit ) "
									+"	VALUES ( ?, ?,?, "
									+"	?, ?, ?, "
									+"	?, ?,?,  "
									+"	?, ?, ?, "
									+"	?, ?, ?, "
									+"	?, ?, ?, "
									+"	?,?,?,	"
									+"	?,?,?,  "
									+"  ?,? ) " ;
						pstmtInsert = conn.prepareStatement(sqlInsert);
						pstmtInsert.setString(1, genTranId);
						pstmtInsert.setTimestamp(2, tranDate);
						pstmtInsert.setString(3, "P");
						pstmtInsert.setString(4, "AL");
						pstmtInsert.setString(5, (String)lsAdvLic.get(i));
						pstmtInsert.setString(6, lineNo);
						pstmtInsert.setString(7, "S-DSP");
						pstmtInsert.setString(8, despId);
						pstmtInsert.setString(9, despLineNo);
						pstmtInsert.setString(10, itemCode);
						pstmtInsert.setString(11, unit);
						pstmtInsert.setDouble(12, updqty);
						pstmtInsert.setString(13, currCode);
						pstmtInsert.setDouble(14, ((Double)lcExchrate.get(i)).doubleValue());
						pstmtInsert.setDouble(15, benefitValue);
						pstmtInsert.setString(16, "O");
						pstmtInsert.setTimestamp(17,tranDate);
						pstmtInsert.setString(18, "BASE"); //chg USer
						pstmtInsert.setString(19, "BASE"); // LoginCOde
						pstmtInsert.setDouble(20, baseValue);
						pstmtInsert.setString(21, "0.000");
						pstmtInsert.setDouble(22, ((Double)lcExchrate.get(i)).doubleValue());
						pstmtInsert.setString(23, itemCode);
						pstmtInsert.setString(24, unit);
						pstmtInsert.setDouble(25, updqty);
						pstmtInsert.setDouble(26, ((Double)lcAlqtyexp.get(i)).doubleValue());
						pstmtInsert.executeUpdate();
						pstmtInsert.close();
						pstmtInsert = null;
						updqty = 0.00;
						benefitValue = 0.00;						
						//conn.commit();
						merrcode = "Success";						
					}
				} // end of while loop
				rs.close();
				rs = null;
				pstmt1.close();
				pstmt1 = null;
				//}  // end of  for loop
			
			}
			rs1.close();
			rs1 = null;
			pstmt.close();
			pstmt = null;
				
				
		}// try
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		
		}
			//valueXmlString.append("</Root>\r\n");
		//return errorString ;			
		return merrcode;
	} // end of advanceLicence		
	public String wfDepb(String despId,ArrayList lsTranId ,Connection conn ) throws RemoteException, ITMException 
	{
		String keyString = "";
		String alType = "";
		String ls_advlic_line_no = "";
		String ls_pocldet_line_no = "";
		String unit = "";
		String itemCode = "";
		String currCode = "";
		String exchRate = "";
		String invoiceId ="";
		double exchRateDec = 0.0;
		String commAmt = "";
		double commAmtDec = 0.00;
		String exchangeRate = "";
		double exeRateDec = 0.00;
		String commAmtOc = "" ;
		double commAmtOcDec = 0.00;
		String invAmt = "";
		double invAmtDec = 0.00;
		String frtRate = "",exchFrtRate ="" ,insRate="",exchInsRate=""; 
		double frtRateInv = 0.00,exchFrtRateInv = 0.00,insRateInv = 0.00,exchInsRateInv= 0.00;
		double frtValue = 0.00, insValue = 0.00;
		String quantity  = "";
		double quantityDec = 0.00;
		String value ="";
		double valueDec =0.00;
		String qtyImp = "";
		double qtyImpDec = 0.00;
		String amtImp = "";
		double amtImpDec = 0.00;
		String qty = "";
		double qtyDec =0.00;
		String amt = "";
		double amtDec=0.0;			
		String sqlSec = "";
		String creditPerc = "";
		double creditPercDec = 0.00;
		double depbvalue = 0.00;
		double invoicevalue = 0.00;
		double benefitValue = 0.00;
		double invAmtRate = 0.00;
		double invoiceAmt = 0.00;
		String  despLineNo ="";
		String tranId = "";
		String sql = "";
		String merrcode = "";
		PreparedStatement pstmt = null,pstmtSec = null,pstmtInsert=null,pstmtCrper=null,pstmtInv=null;
		ResultSet rs = null , rsSec = null,rsCrper=null; 
		Timestamp  tranDate = null;
		tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		Timestamp chgDate = null;
		chgDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		StringBuffer valueXmlString = null;
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");	
		//String despId = "";
		double updQty = 0.00;
		String errorString = "",sqlInsert="",xmlValues="";
		try
		{
			sql ="select key_string  from transetup  where upper(tran_window) = 'W_BENEFIT_TRACE' ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyString =rs.getString("key_string")==null?"": rs.getString("key_string");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql ="select key_string  from transetup  where upper(tran_window) = 'GENERAL' ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyString =rs.getString("key_string")==null?"": rs.getString("key_string");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			/* commented by Chandni Shah 26/08/10 ---DI89UNI060
			sql= "select b.line_no,b.unit__std,b.item_code,d.curr_code,d.exch_rate, "
				+"	sum(b.quantity__stduom), sum(b.quantity__stduom * b.rate__stduom) "
				+ " from depb a , despatchdet b ,despatch d "
				+ " where d.desp_id = b.desp_id "
				+"	and   d.desp_id = ?  "
				+"	and   a.tran_id = ?  "
				+"	group by b.line_no,b.unit__std,b.item_code, "
				+"	d.curr_code,d.exch_rate ";
			*/
			// Changes done by Chandni Shah 26/08/10 ---DI89UNI060
			sql= " select distinct i.invoice_id,b.line_no,b.unit__std,b.item_code,d.curr_code,d.exch_rate, "
				+" sum(b.quantity__stduom), sum(b.quantity__stduom * b.rate__stduom), "
				+" i.comm_amt,i.exch_rate,i.comm_amt__oc,i.inv_amt,i.frt_amt,i.exch_rate__frt, "
				+" i.ins_amt ,i.exch_rate__ins "
				+" from invoice i, depb a , despatchdet b ,despatch d "
				+" where d.desp_id = b.desp_id "
				+" and d.desp_id = i.desp_id "
				+" and   d.desp_id = ? "
				+" and   a.tran_id = ? "
				+" group by i.inv_amt,i.invoice_id,b.line_no,b.unit__std,b.item_code, "
				+" d.curr_code,d.exch_rate,i.exch_rate,i.comm_amt__oc,i.comm_amt , " 
				+" i.frt_amt,i.exch_rate__frt,i.ins_amt ,i.exch_rate__ins " ;
				

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,(String)despId.get(0));
			//pstmt.setString(2,lsTranId);
			pstmt.setString(1,despId);
			pstmt.setString(2,(String)lsTranId.get(0));
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				invoiceId = rs.getString(1); 
				despLineNo = rs.getString(2);
				unit = rs.getString(3);
				itemCode = rs.getString(4);
				currCode = rs.getString(5);
				exchRate = rs.getString(6);
				exchRateDec = Double.parseDouble(exchRate);
				quantity = rs.getString(7);
				quantityDec = Double.parseDouble(quantity);
				value = rs.getString(8);
				valueDec = Double.parseDouble(value);
				commAmt = rs.getString(9);
				commAmtDec = Double.parseDouble(commAmt);
				exchangeRate = rs.getString(10);
				exeRateDec = Double.parseDouble(exchangeRate);
				commAmtOc = rs.getString(11);
				commAmtOcDec = Double.parseDouble(commAmtOc);
				invAmt = rs.getString(12);
				invAmtDec = Double.parseDouble(invAmt);
				frtRate = rs.getString(13);
				frtRateInv = Double.parseDouble (frtRate);
				exchFrtRate = rs.getString(14);
				exchFrtRateInv = Double.parseDouble (exchFrtRate);
				insRate = rs.getString(15);
				insRateInv = Double.parseDouble (insRate);
				exchInsRate = rs.getString(16);
				exchInsRateInv = Double.parseDouble (exchInsRate);
				
				sqlSec="select credit_perc "
					+"	from depb_rate	where item_code__depb = ?  "
					+"	and eff_from <= ?  "
					+"	and valid_upto >= ?  ";
				pstmtCrper = conn.prepareStatement(sqlSec);	
				pstmtCrper.setString(1,itemCode.trim());
				pstmtCrper.setTimestamp(2,tranDate);
				pstmtCrper.setTimestamp(3,tranDate);
				rsCrper = pstmtCrper.executeQuery();
				if(rsCrper.next())
				{

					creditPercDec = rsCrper.getDouble(1);
					//creditPercDec = Double.parseDouble(creditPerc); 
					
				}
				if(creditPercDec==0)
				{
					errorString ="VTDEPBRT1" ;
				}
				depbvalue = valueDec * creditPercDec * 0.01 * exeRateDec ; //exeRateDec is Added ..
				System.out.println(" depbvalue ========>>>>>>>>>" +depbvalue);
				invoicevalue = (commAmtOcDec * exeRateDec); //Chandni Shah 26/08/10 
				System.out.println(" invoicevalue ========>>>>>>>>>" +invoicevalue);
				frtValue = (frtRateInv * exchFrtRateInv) ; 
				System.out.println(" frtValue ========>>>>>>>>>" +frtValue);
				insValue = (insRateInv * exchInsRateInv);
				System.out.println(" insValue ========>>>>>>>>>" +insValue);
				//benefitValue = (depbvalue - invoicevalue) + commAmtDec ;//Chandni Shah 06/09/10 
				benefitValue = (depbvalue )+ (invoicevalue) - (frtValue) - (insValue);
				
				System.out.println(" benefitValue ========>>>>>>>>>" +benefitValue);
				invAmtRate = (invAmtDec * exeRateDec )* (12.5/100);//Added by Chandni Shah 06/09/10 as Y
			
				invoiceAmt = (depbvalue - invoicevalue)+invAmtRate;
				//commAmt = ()
				rsCrper.close();
				rsCrper = null;	
				//Chandni Shah 13/09/2010
				//To check [ comm_amt__oc > (inv_amt*exh_rate*12.5%)]
				System.out.println(" commAmtOCDec ========>>>>>>>>>" +invoicevalue);
				System.out.println(" invAmtRate ========>>>>>>>>>" +invAmtRate);
				System.out.println(" commAmtOCDec > invAmtRate ========>>>>>>>>>" +(invoicevalue > invAmtRate));
				
				if (invoicevalue > invAmtRate)
				{
					System.out.println(" Inside If condition :::::::::" );
					
					sqlSec = "UPDATE depb  "
					+" SET   amount_cr = (case when amount_cr is null "
					+" then 0 else amount_cr end)  +  ?  "
					+" WHERE tran_id = ?   ";
					pstmtInv = conn.prepareStatement(sqlSec);
					pstmtInv.setDouble(1,invoiceAmt);
					pstmtInv.setString(2,(String)lsTranId.get(0));
					rsCrper = pstmtInv.executeQuery();
					rsCrper.close();
					rsCrper = null;	
					pstmtInv.close();
					pstmtInv = null;	
					
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";						
					xmlValues = xmlValues + "<tramn_date>"+tranDate+"</tramn_date>" ;
					xmlValues = xmlValues + "</Detail1></Root>";
					String genTranId = generateTranId("w_benefit_trace", xmlValues, conn);
					System.out.println("genTranId >>>>>>>>>"+ genTranId);
					sqlInsert = " INSERT INTO benefit_trace ( "
									+"	tran_id, tran_date, adj_type, "
									+"	benefit_type, benefit_no, line_no__benefit, "
									+"	ref_ser, ref_no, line_no__ref, "
									+"	item_code, unit, quantity, "
									+"	curr_code, exch_rate, benefit_value,"
									+"	status, chg_date, chg_user, "
									+"	chg_term, value__base, benefit_perc, "
									+"	exch_rate__base ) "
									+"	VALUES ( ?, ?,?, "
									+"	?, ?, ?, "
									+"	?, ?,?,  "
									+"	?, ?, ?, "
									+"	?, ?, ?, "
									+"	?, ?, ?, "
									+"	?,?,?,	"
									+"  ? ) " ;
						pstmtInsert = conn.prepareStatement(sqlInsert);
						pstmtInsert.setString(1, genTranId);
						pstmtInsert.setTimestamp(2, tranDate);
						pstmtInsert.setString(3, "P");
						pstmtInsert.setString(4, "DP");
						pstmtInsert.setString(5, (String)lsTranId.get(0));//
						pstmtInsert.setInt(6, 0);
						pstmtInsert.setString(7, "S-DSP");
						pstmtInsert.setString(8, despId);//
						pstmtInsert.setString(9, despLineNo);
						//pstmtInsert.setString(9, ls_pocldet_line_no);
						pstmtInsert.setString(10, itemCode);
						pstmtInsert.setString(11, unit);
						pstmtInsert.setDouble(12, quantityDec);
						pstmtInsert.setString(13, currCode);
						pstmtInsert.setDouble(14, exchRateDec);
						//pstmtInsert.setDouble(15, depbvalue);
						//pstmtInsert.setDouble(15, invoiceAmt); //
						pstmtInsert.setDouble(15, benefitValue); //22/12/10
						pstmtInsert.setString(16, "O");
						pstmtInsert.setTimestamp(17,tranDate);
						pstmtInsert.setString(18, "BASE"); //chg USer
						pstmtInsert.setString(19, "BASE"); // LoginCOde
						//pstmtInsert.setDouble(20, depbvalue);
						//pstmtInsert.setDouble(20, invoiceAmt); //-----
						pstmtInsert.setDouble(20, benefitValue); //22/12/10
						pstmtInsert.setString(21, "0.000");
						pstmtInsert.setDouble(22, exchRateDec);	
						pstmtInsert.executeUpdate();
				}
				else //To check [ comm_amt__oc <= (inv_amt*exh_rate*12.5%)] Chandni Shah 13/09/2010 ---DI89UNI060
				{				
					System.out.println(" Inside Else condition :::::::::" );
					/* Commented by Chandni Shah 26/08/10 
					sqlSec= "UPDATE depb  "
						+"SET   amount_cr = (case when amount_cr is null "
						+"	then 0 else amount_cr end)  + ( ? * 1.0 ) "
						+"	WHERE tran_id = ?   ";
					*/
					// Changes done by Chandni Shah 26/08/10  ---DI89UNI060
					sqlSec= "UPDATE depb  "
						+" SET   amount_cr = (case when amount_cr is null "
						+" then 0 else amount_cr end)  +  ?  "
						+" WHERE tran_id = ?   ";
					pstmtSec = conn.prepareStatement(sqlSec);
					pstmtSec.setDouble(1,benefitValue);
					pstmtSec.setString(2,(String)lsTranId.get(0));
					pstmtSec.executeUpdate();		
					pstmtSec.close();
					pstmtSec = null;					
				
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";						
					xmlValues = xmlValues + "<tramn_date>"+tranDate+"</tramn_date>" ;
					xmlValues = xmlValues + "</Detail1></Root>";
					String genTranId = generateTranId("w_benefit_trace", xmlValues, conn);
					System.out.println("genTranId >>>>>>>>>"+ genTranId);
					sqlInsert = " INSERT INTO benefit_trace ( "
									+"	tran_id, tran_date, adj_type, "
									+"	benefit_type, benefit_no, line_no__benefit, "
									+"	ref_ser, ref_no, line_no__ref, "
									+"	item_code, unit, quantity, "
									+"	curr_code, exch_rate, benefit_value,"
									+"	status, chg_date, chg_user, "
									+"	chg_term, value__base, benefit_perc, "
									+"	exch_rate__base ) "
									+"	VALUES ( ?, ?,?, "
									+"	?, ?, ?, "
									+"	?, ?,?,  "
									+"	?, ?, ?, "
									+"	?, ?, ?, "
									+"	?, ?, ?, "
									+"	?,?,?,	"
									+"  ? ) " ;
						pstmtInsert = conn.prepareStatement(sqlInsert);
						pstmtInsert.setString(1, genTranId);
						pstmtInsert.setTimestamp(2, tranDate);
						pstmtInsert.setString(3, "P");
						pstmtInsert.setString(4, "DP");
						pstmtInsert.setString(5, (String)lsTranId.get(0));//
						pstmtInsert.setInt(6, 0);
						pstmtInsert.setString(7, "S-DSP");
						pstmtInsert.setString(8, despId);//
						pstmtInsert.setString(9, despLineNo);
						//pstmtInsert.setString(9, ls_pocldet_line_no);
						pstmtInsert.setString(10, itemCode);
						pstmtInsert.setString(11, unit);
						pstmtInsert.setDouble(12, quantityDec);
						pstmtInsert.setString(13, currCode);
						pstmtInsert.setDouble(14, exchRateDec);
						//pstmtInsert.setDouble(15, depbvalue);
						pstmtInsert.setDouble(15, benefitValue); //
						pstmtInsert.setString(16, "O");
						pstmtInsert.setTimestamp(17,tranDate);
						pstmtInsert.setString(18, "BASE"); //chg USer
						pstmtInsert.setString(19, "BASE"); // LoginCOde
						//pstmtInsert.setDouble(20, depbvalue);
						pstmtInsert.setDouble(20, benefitValue); //-----
						pstmtInsert.setString(21, "0.000");
						pstmtInsert.setDouble(22, exchRateDec);	
										
						pstmtInsert.executeUpdate();
						pstmtInsert.close();
						pstmtInsert = null;
						//conn.commit();
						merrcode = "Success";
						
						
				}
			}  // end of while code
			pstmtCrper.close();
			pstmtCrper = null;	
			//pstmtInsert.close();
			//pstmtInsert = null;
			//pstmtSec.close();
			//pstmtSec = null;
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//conn.close();
			//conn = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		
		}
		valueXmlString.append("</Root>\r\n");	
		return merrcode ;
	}  // end of wf_depb

	private Timestamp getCurrdateAppFormat() throws ITMException
    {
        String s = "";	
		 Timestamp timestamp = null;		
       // GenericUtility genericUtility = GenericUtility.getInstance();
		 E12GenericUtility genericUtility= new  E12GenericUtility();
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
            throw new ITMException(exception); //Added By Mukesh Chauhan on 02/08/19
        }
        return timestamp;
    }
	private String generateTranId(String windowName,String xmlValues,Connection conn) throws Exception 
	{
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "",errCode ="",errString ="";
		String tranId = null;
		String newKeystring = "";
		boolean found =false;
		 try
		 {
			sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)=UPPER( ? )";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,windowName);
			rs = pstmt.executeQuery();
			System.out.println("keyString :"+rs.toString());
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rs.next())
			{	
				found =true;
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(!found)
			{	  
				  sql ="SELECT key_string,TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
				  pstmt	= conn.prepareStatement(sql);
				  rs = pstmt.executeQuery();
				  if (rs.next())
				  {	
					keyString = rs.getString(1);
					keyCol = rs.getString(2);
					tranSer1 = rs.getString(3);	
				  }
				  rs.close();
				  rs = null;
				  pstmt.close();
				  pstmt = null ;
			}
			if(keyString ==null || keyString.trim().length() ==0)
			{
				errCode = "VTSEQ";
				System.out.println("errcode......"+errCode);
				errString = itmDBAccessEJB.getErrorString("","VTSEQ","BASE","",conn);

			}
			System.out.println("keyString=>"+keyString);
			System.out.println("keyCol=>"+keyCol);
			System.out.println("tranSer1"+tranSer1);
			
			System.out.println("xmlValues  :["+xmlValues+"]");
			
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
		
			System.out.println(" new tranId :"+tranId);
			if(rs!=null)
			 {
				rs.close();
			 }
			 if(pstmt!=null)
			 {	
				pstmt.close();
			 }
		}
		catch(SQLException ex)
		{
			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");			
			ex.printStackTrace();
			tranId=null;
			throw new  Exception(ex);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			tranId=null;
			throw new  Exception(e);
		}
		return tranId;
	}//generateTranTd()	
public String 	gfGetSqlInString(ArrayList  advLicTranId)
{
	int i=0;
	int length = advLicTranId.size();
	String retrunStr="";
	for(i=0;i<length;i++)
	{
		if(i==length-1)
		{
			retrunStr = retrunStr + "'" +advLicTranId.get(i)+"'";
		}
		else
		{
			retrunStr =retrunStr+"'"+advLicTranId.get(i)+"'"+",";
		}
	}
	return  retrunStr ;
}


}//end class