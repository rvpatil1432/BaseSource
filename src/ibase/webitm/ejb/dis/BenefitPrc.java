
/********************************************************
	Title : BenefitPrcEJB
	Date  : 28/02/09	

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

@Stateless // added for ejb3
public class BenefitPrc extends ProcessEJB implements BenefitPrcLocal,BenefitPrcRemote // SessionBean
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
			System.out.println("Exception :BenefitPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
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
		String ls_adv_lic = "";		
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
							ls_adv_lic = childNode.getFirstChild().getNodeValue();						
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
				if((exchangeRate==null || exchangeRate.length()==0 )&&  benefitType.trim().equalsIgnoreCase("AL"))
				{
					ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
					exchangeRateStr = itmDBAccessEJB.getErrorString("","VTEXHEMTY",userId,"",conn);					
					 return exchangeRateStr ;							
					
				}
			}// OUT FOR LOOP
			sql="select curr_code, exch_rate from po_clearance where tran_id = '"+tranId+"' ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				despCurrCode= rs.getString(1)==null ?"":rs.getString(1);
				exchRate= rs.getString(2)==null ?"0.00":rs.getString(2);
				exchRateDec = Double.parseDouble(exchRate);					
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql="select curr_code, file_no, al_type	from adv_licence where tran_id = '"+ls_adv_lic+"' ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				advCurrCode= rs.getString(1)==null ?"":rs.getString(1);
				fileNo= rs.getString(2)==null ?"":rs.getString(2);
				alType= rs.getString(3)==null ?"":rs.getString(3);
			
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(!(advCurrCode.trim().equalsIgnoreCase(despCurrCode.trim())))
			{
					/*  ?????????????
					s_pass1.mvar1 = ls_adv_lic
					s_pass1.mvar2 = ls_file_no
					s_pass1.mvar3 = ls_desp_curr_code
					s_pass1.mvar4 = ls_adv_curr_code
					openwithparm(w_benefit_trace,s_pass1)
					s_pass2 = message.powerobjectparm
					if s_pass2.mdecimal > 0 then 
						s_pass1.mdecimal = s_pass2.mdecimal
					end if						*/	
					returnExchRateDec	 = Double.parseDouble(exchangeRate)	;								
			}
			else
			{
				returnExchRateDec = exchRateDec ;
			}
			if(ls_adv_lic.trim().length() > 0 && benefitType.trim().equalsIgnoreCase("AL"))
			{
				merrcode = advanceLicence(tranId,ls_adv_lic ,returnExchRateDec , conn);				
			}
			if(ls_adv_lic.trim().length() > 0 && benefitType.trim().equalsIgnoreCase("DP"))
			{
				merrcode = depb(tranId,ls_adv_lic, conn);
			}
			if(	merrcode.equals("success"))
			{
				conn.commit();
				merrcode = itmDBAccessEJB.getErrorString("","VTCOMSUC",userId,"",conn);
			}			
			else
			{
				
				conn.rollback();
								
			}			
			
		}
	   	catch(Exception e)
		{
				try{
				conn.rollback();
				}catch(Exception e1)
				{e1.printStackTrace();}
				
			   System.out.println("Exception in BENEFITPrcEJB..."+e.getMessage());
			   e.printStackTrace();
			   errorString = e.getMessage();
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
		System.out.println("returning from  BenefitPrcEJB   "+merrcode);
		return merrcode;
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
			rtrStr = e.getMessage();			
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
		PreparedStatement pstmt = null,pstmt1 = null;
		Statement st = null;
		StringBuffer valueXmlString = new StringBuffer();			
		ConnDriver connDriver = new ConnDriver();	
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String userId = "",benefitType  ="",tranId="";	
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
			benefitType  = genericUtility.getColumnValue("benefit_type",headerDom);		
				
			if(benefitType == null )
			{
				
				errString = itmDBAccessEJB.getErrorString("","VTBENUL",userId,"",conn);
				resultString =errString ;
				return resultString;
			}		
			/*
			sql = " select count(*)  from benefit_trace	where ref_ser = 'P-BE' and	ref_no  = ? ";
			*/
			//Benefit_type condition is Added in select query by Chandni Shah - 14/09/10	---DI89UNI059	
			
			sql = " select count(*)  from benefit_trace	where benefit_type= ? and ref_ser = 'P-BE' and	ref_no  = ? ";
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
			if(count>0 )
			{				
				errString = itmDBAccessEJB.getErrorString("","VTEMPTI",userId,"",conn);
				resultString =errString ;
				return resultString;
				
			}
			else
			{
				if(benefitType != null && benefitType.trim().equalsIgnoreCase("AL"))
				{
					sql = "select distinct x.tran_id,x.file_no,x.al_no, x.line_no__exp, x.item_code__exp  from "
					+ "(SELECT a.tran_id,a.file_no,d.item_code,a.al_no,a.site_code, "
					+"a.eff_from ,(case when a.extend_upto is null then a.valid_upto else a.extend_upto end) valid_upto, "
					+" c.fin_entity,d.line_no__exp,d.item_code__exp FROM adv_licence a,site b,finent c , "
					+" adv_licence_imp d	where a.tran_id = d.tran_id and 	a.site_code = b.site_code "
					+" and b.fin_entity = c.fin_entity and a.status = 'O') X, "
					+"(select p.tran_id,s.item_code,p.site_code,r.fin_entity "
					+" from po_clearance p,site q,finent r, po_clearance_det s "
					+" where p.tran_id = s.tran_id and "
					+" p.site_code = q.site_code and "
					+" q.fin_entity = r.fin_entity) Y "
					+" where x.fin_entity = y.fin_entity "
					+ "and 	x.item_code = y.item_code "
					+"and	y.tran_id =  ? ";
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
					while(rs.next())
					{						
						valueXmlString.append(rs.getString(1)==null ?"":rs.getString(1).trim()).append("\t");
						valueXmlString.append(rs.getString(2)==null ?"":rs.getString(2).trim()).append("\t");
						valueXmlString.append(rs.getString(3)==null ?"":rs.getString(3).trim()).append("\t");
						valueXmlString.append(rs.getString(4)==null ?"":rs.getString(4).trim()).append("\t");
						valueXmlString.append(rs.getString(5)==null ?"":rs.getString(5).trim()).append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("\n");
					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;						
					resultString = valueXmlString.toString();
					
				} // end of if
				if(benefitType != null && benefitType.trim().equalsIgnoreCase("DP"))
				{
					sql =" SELECT x.tran_id,x.file_no FROM "
						+"(SELECT a.tran_id,a.file_no,a.site_code__depb,a.eff_date,a.valid_upto, "
						+"c.fin_entity FROM depb a,site b,finent c "
						+"where a.site_code__depb = b.site_code "
						+"and b.fin_entity = c.fin_entity "
						+"and a.status = 'O') X, "
						+"(select p.tran_id,p.site_code,r.fin_entity from po_clearance p,site q,finent r "
						+"where p.site_code = q.site_code and "
						+"q.fin_entity = r.fin_entity) Y "
						+"where x.fin_entity = y.fin_entity "
						+"and	y.tran_id = ? ";
					
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,tranId.trim());
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						valueXmlString.append(rs.getString(1)==null ?"":rs.getString(1).trim()).append("\t");
						valueXmlString.append(rs.getString(2)==null ?"":rs.getString(2).trim()).append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						valueXmlString.append("").append("\t");
						
						valueXmlString.append("\n");					
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					resultString = valueXmlString.toString();				
				}
				if(benefitType != null && benefitType.trim().equalsIgnoreCase("DR"))
				{
					sql="SELECT x.tran_id,x.account_no_edi,x.location FROM "
						+"(SELECT a.tran_id,a.account_no_edi,a.site_code__edi,a.tran_date, "
						 +"c.fin_entity,a.location FROM drawback a,site b,finent c "
						+"where a.site_code__edi = b.site_code "
						+ "and b.fin_entity = c.fin_entity "
						+" and a.status = 'O') X, "
						+"(select p.tran_id,p.site_code,r.fin_entity "
						+"from po_clearance p,site q,finent r "
						+" where p.site_code = q.site_code and "
						+ "q.fin_entity = r.fin_entity) Y "
						+ "where x.fin_entity = y.fin_entity "
						+"and	y.tran_id = ?  ";
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						valueXmlString.append(rs.getString(1)==null ?"":rs.getString(1).trim()).append("\t");
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

				}// end of 
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
	public String  advanceLicence(String tranId, String ls_adv_lic,double returnExchRateDec, Connection conn ) throws RemoteException, ITMException 
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
		String netAmount = "";
		double netAmountDec = 0.00 ;
		String amtImp = "";
		double amtImpDec = 0.00;
		String qty = "";
		double qtyDec =0.00;
		String amt = "";
		double amtDec=0.0;
		double  baseValue = 0.00;
		String merrcode = "";
		String sql ="",sqlSec = "" ;
		String ll_exp_lineno = "";
		String ls_item_code = "";
		String lc_exp = "";
		String amt_imp ="";
		String amt_impBc ="";
		String sqlsec = "";
		String noUpdate = "";
		PreparedStatement pstmt =null,pstmtSec = null ,pstmtInsert = null  ;
		ResultSet rs = null;
		Timestamp  tranDate = null;
		tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		Timestamp chgDate = null;
		chgDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String userId = "";
		//StringBuffer valueXmlString = null;
		//valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");	
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
			sql ="select al_type  from adv_licence where tran_id = '"+ls_adv_lic+"' ";  //???:as_advlic_no;
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				 alType = rs.getString("al_type")==null ?"":rs.getString("al_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = " select a.line_no , d.line_no, d.unit, a.item_code, b.curr_code	, b.exch_rate, b.net_amt , "
				+ "sum(case when d.quantity is null then 0 else d.quantity end), "
				+" sum(case when d.asseable_amt is null then 0 else d.asseable_amt end),"
				+ "(case when a.quantity_imp is null then 0 else a.quantity_imp end), "
				+ "(case when a.amount_imp_bc is null then 0 else a.amount_imp_bc end)	, "
				+"(case when a.quantity is null then 0 else a.quantity end), "
				+ "(case when a.amount_bc is null then 0 else a.amount_bc end) "
				+" from adv_licence_imp a , po_clearance b ,po_clearance_det d "
				+"where d.tran_id   = b.tran_id "
				+ "and   a.item_code = d.item_code "
				+ "and   d.tran_id 	= '"+tranId+"' "
				+ "and   a.tran_id 	= '"+ls_adv_lic+"' "  
				+ "group by a.line_no , d.line_no , d.unit , a.item_code , "
				+" b.curr_code	, b.exch_rate	,b.net_amt, a.quantity_imp	, a.amount_imp_bc, " 
				+"a.quantity	, a.amount_bc ";
				pstmt = conn.prepareStatement(sql);	
				rs = pstmt.executeQuery();
				while(rs.next())
				{					
					ls_advlic_line_no = rs.getString(1);
					ls_pocldet_line_no = rs.getString(2);
					unit  =  rs.getString(3);
					itemCode = rs.getString(4);
					currCode = rs.getString(5);
					exchRate = rs.getString(6);
					exchRateDec = Double.parseDouble(exchRate);
					netAmount = rs.getString(7);
					netAmountDec = Double.parseDouble(netAmount);
					quantity = rs.getString(8);
					quantityDec = Double.parseDouble(quantity);
					value = rs.getString(9);
					valueDec = Double.parseDouble(value);
					qtyImp = rs.getString(10);
					qtyImpDec = Double.parseDouble(qtyImp);
					amtImp=  rs.getString(11);
					amtImpDec = Double.parseDouble(amtImp);
					qty = rs.getString(12);
					qtyDec = Double.parseDouble(qty);
					amt = rs.getString(13);
					amtDec = Double.parseDouble(amt);
					System.out.println("exchRateDec>>>>>>>>>>"+exchRateDec);
					System.out.println("quantityDec>>>>>>>>>>"+quantityDec);
					System.out.println("valueDec>>>>>>>>>>"+valueDec);
					System.out.println("qtyImpDec>>>>>>>>>>"+qtyImpDec);
					System.out.println("amtImpDec>>>>>>>>>>"+amtImpDec);
					System.out.println("qtyDec>>>>>>>>>>"+qtyDec);
					System.out.println("amtDec>>>>>>>>>>"+amtDec);
					System.out.println("netAmount>>>>>>>>>>"+netAmountDec);
					
					System.out.println("returnExchRateDec>>>>>>>>>>"+returnExchRateDec);
					baseValue = valueDec * returnExchRateDec ;	//??lc_value * s_pass1.mdecimal consider  s_pass1.mdecimal =returnExchRateDec;
					System.out.println("valueDec * returnExchRateDec>>>>>>>>>>"+baseValue);
					if(alType.equalsIgnoreCase("B"))
					{
						if( ((qtyDec - qtyImpDec) <= quantityDec) || ((amtDec -amtImpDec) <=baseValue  ) )  //Al type = Both
						{							
							merrcode = itmDBAccessEJB.getErrorString("","VTQVB","BASE","",conn);	
							return merrcode	;					
					
						}
					}
					else if(alType.equalsIgnoreCase("Q"))
					{
						if((qtyDec - qtyImpDec) <= quantityDec)   //Al type = Quantity
						{							
							merrcode = itmDBAccessEJB.getErrorString("","VTQB","BASE","",conn);							
							return merrcode ;
					
						}
					}
					else if(alType.equalsIgnoreCase("V"))
					{
						if((amtDec -amtImpDec) <=baseValue  )  //Al type = Value
						{							
							merrcode = itmDBAccessEJB.getErrorString("","VTVB","BASE","",conn);	
							return merrcode	;											
						}
					}					
					String genTranId = generateTranId("w_benefit_trace", "S-BTR", conn);
					System.out.println("genTranId >>>>>>>>>"+ genTranId);
					String sqlInsert = "INSERT INTO benefit_trace (tran_id, tran_date, adj_type, benefit_type, "
										+"	benefit_no, line_no__benefit, ref_ser, "
										+"	ref_no, line_no__ref, item_code, unit, "
										+"	quantity, curr_code, exch_rate, benefit_value,	"
										+"	status,chg_date, chg_user, chg_term, value__base, "
										+"	benefit_perc, exch_rate__base ) "
										+"	VALUES ( ?, ?,?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?,?,?,?) ";
					pstmtInsert = conn.prepareStatement(sqlInsert);
					pstmtInsert.setString(1, genTranId);
					pstmtInsert.setTimestamp(2, tranDate);
					pstmtInsert.setString(3, "M");
					pstmtInsert.setString(4, "AL");
					pstmtInsert.setString(5, ls_adv_lic);
					pstmtInsert.setString(6, ls_advlic_line_no);
					pstmtInsert.setString(7, "P-BE");
					pstmtInsert.setString(8, tranId);
					pstmtInsert.setString(9, ls_pocldet_line_no);
					pstmtInsert.setString(10, itemCode);
					pstmtInsert.setString(11, unit);
					pstmtInsert.setDouble(12, quantityDec);
					pstmtInsert.setString(13, currCode);
					pstmtInsert.setDouble(14, returnExchRateDec);
					//pstmtInsert.setDouble(15, valueDec);
					pstmtInsert.setDouble(15, netAmountDec);//22/12/10
					pstmtInsert.setString(16, "O");
					pstmtInsert.setTimestamp(17,tranDate);
					pstmtInsert.setString(18, "BASE");
					pstmtInsert.setString(19, "BASE");
					//pstmtInsert.setDouble(20, baseValue);
					pstmtInsert.setDouble(20, netAmountDec);//22/12/10
					pstmtInsert.setString(21, "0.000");
					pstmtInsert.setDouble(22, exchRateDec);
					pstmtInsert.executeUpdate();
					pstmtInsert.close();
					pstmtInsert = null;
					/*
					sqlSec="update adv_licence_imp  "
					+" SET quantity_imp 	= (case when quantity_imp is null then 0 else quantity_imp end )  + '"+quantityDec+"' , "
					 +"amount_imp			=	(case when amount_imp is null then 0 else amount_imp end)	   + '"+valueDec+"' , "
					+" amount_imp_bc		=	(case when amount_imp_bc is null then 0 else amount_imp_bc end)	+ ('"+valueDec+"' * '"+returnExchRateDec+"' ) "
					+ " WHERE tran_id  = '"+ls_adv_lic+"'  "
					+" AND  	line_no 	= '"+ls_advlic_line_no+"'  ";
					*/
					
					//Chandni Shah 14/09/10   ---DI89UNI059
					sqlSec="update adv_licence_imp  "
						+" SET quantity_imp 	= (case when quantity_imp is null then 0 else quantity_imp end )  + '"+quantityDec+"' , "
						+"amount_imp			=	(case when amount_imp is null then 0 else amount_imp end)	   + '"+netAmountDec+"' , "
						+" amount_imp_bc		=	(case when amount_imp_bc is null then 0 else amount_imp_bc end)	+ ('"+netAmountDec+"' * '"+returnExchRateDec+"' ) "
						+ " WHERE tran_id  = '"+ls_adv_lic+"'  "
						+" AND  	line_no 	= '"+ls_advlic_line_no+"'  ";
					
					pstmtSec = conn.prepareStatement(sqlSec);	
					pstmtSec.executeUpdate();
					pstmtSec.close();
					pstmtSec = null;
					
				}// end of while loop
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				sql= "select line_no , item_code from   adv_licence_exp  where  tran_id = '"+ls_adv_lic+"' ";
				pstmt = conn.prepareStatement(sql);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ll_exp_lineno = rs.getString(1) == null ? "" : rs.getString(1) ;
					ls_item_code =  rs.getString(2) == null ? "" : rs.getString(2) ;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				
				/*  
				
				//chandni Shah ---> 14/09/2010  ---DI89UNI059
				// instead of adv_licence_exp table ,adv_licence table is updated
				sql= " select max((amount_imp/amount)*100) 	"
					+" from   adv_licence_imp "
					+ "where  tran_id 		 = '"+ls_adv_lic+"'  "
					+ "and 	 line_no__exp   = '"+ll_exp_lineno+"'  "
					+" and	 item_code__exp = '"+ls_item_code+"'  "
					+"and	 amount	 > 0 ";
				pstmt = conn.prepareStatement(sql);	
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					lc_exp= rs.getString(1) == null ? "" : rs.getString(1) ;
					sqlsec = "update adv_licence_exp "
						 +" set exp_obl_act = ((case when exp_obl is null "
						 +"then 0 else exp_obl end) * '"+ lc_exp +"'  )/100  where tran_id   = '"+ls_adv_lic+"' " 
						 + " and	line_no 	 = '"+ll_exp_lineno+"' "
						 + " and	item_code = '"+ls_item_code+"'  " ;
					pstmtSec = conn.prepareStatement(sqlsec);	
					pstmtSec.executeUpdate();
					pstmtSec.close();
					pstmtSec = null;				
				}
				*/
				//chandni Shah ---> 14/09/2010   ---DI89UNI059
				sql= " select amount_imp,amount_imp_bc	"
					+" from   adv_licence_imp "
					+ "where  tran_id 		 = '"+ls_adv_lic+"'  "
					+ "and 	 line_no__exp   = '"+ll_exp_lineno+"'  "
					+" and	 item_code__exp = '"+ls_item_code+"'  "
					+"and	 amount	 > 0 ";
				pstmt = conn.prepareStatement(sql);	
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					amt_imp = rs.getString(1) == null ? "" : rs.getString(1) ;
					amt_impBc = rs.getString(2) == null ? "" : rs.getString(2) ;
					// update query for adv_licence
					sqlsec = "update adv_licence "
						+" set import_amt =  (case when import_amt is null then 0 else import_amt end )  + '"+  amt_imp +"' , "
						+" import_amt__bc = (case when import_amt__bc is null then 0 else import_amt__bc end )  + '"+ amt_impBc  +"' "
						+"where  tran_id = '"+ls_adv_lic+"' " ;
						 
					pstmtSec = conn.prepareStatement(sqlsec);	
					pstmtSec.executeUpdate();
					pstmtSec.close();
					pstmtSec = null;				
				}
				if(merrcode.length()==0)
				{
					merrcode ="success";					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
			}
			catch(Exception e)
			{
				e.printStackTrace();
			
			}
			//valueXmlString.append("</Root>\r\n");
			return merrcode ;				
	} // end of advanceLicence
	public String depb(String ls_tran_id,String ls_depb_no ,Connection conn ) throws RemoteException, ITMException 
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
		String taxAmount = "";
		double taxAmountDec = 0.00;
		String sqlSec = "";
		String creditPerc = "";
		double creditPercDec = 0.00;
		double depbvalue = 0.00;
		String  despLineNo ="";
		String tranId = "";			
		String merrcode = "";
		String sql ="" ;
		String ll_exp_lineno = "";
		String ls_item_code = "";
		String lc_exp = "";
		String sqlsec = "";
		PreparedStatement pstmt = null,pstmtSec = null,pstmtInsert = null ;
		ResultSet rs = null , rsSec = null; 
		Timestamp  tranDate = null;
		tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;	
		Timestamp chgDate = null;
		chgDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
		int noUpdate = 0;
		String userId="";
		//StringBuffer valueXmlString = null;
		//valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");	
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
			sql= "  select b.line_no,b.unit,b.item_code,d.curr_code,d.exch_rate,"
				+"	sum(b.quantity), sum(b.asseable_amt), d.tax_amt "
				+"	from depb a , po_clearance_det b ,po_clearance d "
				+"	where d.tran_id = b.tran_id "
				+"	and   d.tran_id = '"+ ls_tran_id+"'  "
				+"	and   a.tran_id = '"+ls_depb_no+"' "  //?? as_depb_no
				+"	group by b.line_no,b.unit,b.item_code, "
				+"	d.curr_code,d.exch_rate,d.tax_amt ";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				
				despLineNo = rs.getString(1);
				unit = rs.getString(2);
				itemCode = rs.getString(3);
				currCode = rs.getString(4);
				exchRate = rs.getString(5);
				exchRateDec = Double.parseDouble(exchRate);
				quantity = rs.getString(6);
				quantityDec = Double.parseDouble(quantity);
				value = rs.getString(7);
				valueDec = Double.parseDouble(value);
				taxAmount = rs.getString(8);
				taxAmountDec = Double.parseDouble(taxAmount);
				sqlSec="select credit_perc "
					+" from depb_rate	where item_code__depb = '"+itemCode+"' ";
				pstmtSec = conn.prepareStatement(sqlSec);	
				rsSec = pstmtSec.executeQuery();
				if(rsSec.next())
				{

					creditPerc = rsSec.getString(1);
					creditPercDec = Double.parseDouble(creditPerc);  				
				}
				depbvalue = valueDec * creditPercDec * 0.01 ;
				rsSec.close();
				rsSec = null;
				pstmtSec.close();
				pstmtSec = null;
				 System.out.println("despLineNo>>>>>>>" + despLineNo);
				System.out.println("unit>>>>>>>" + unit);
				System.out.println("itemCode>>>>>>>" + itemCode);
				System.out.println("currCode>>>>>>>" + currCode);
				System.out.println("exchRateDec>>>>>>>" + exchRateDec);
				System.out.println("quantityDec>>>>>>>" + quantityDec);
				System.out.println("valueDec>>>>>>>" + valueDec);
				System.out.println("creditPercDec>>>>>>>" + creditPercDec);
				System.out.println("depbvalue>>>>>>>" + depbvalue);
				System.out.println ("taxAmount>>>>>>>>>>>"+taxAmountDec);
				String genTranId = generateTranId("w_benefit_trace", "S-BTR", conn);
				System.out.println("tranId>>>>>>>" + genTranId);
				String sqlInsert = "INSERT INTO benefit_trace (tran_id, tran_date, adj_type, "
									+"	benefit_type, benefit_no, line_no__benefit, ref_ser, "
									+"	ref_no, line_no__ref, item_code, unit, quantity, "
									+"	curr_code, exch_rate, benefit_value,status,chg_date, "
									+"	chg_user, chg_term, value__base, benefit_perc, exch_rate__base )"
									+"	VALUES ( ?, ?,?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?,?,?,?) ";
				pstmtInsert = conn.prepareStatement(sqlInsert);
				pstmtInsert.setString(1, genTranId);
				pstmtInsert.setTimestamp(2, tranDate);
				pstmtInsert.setString(3, "M");
				pstmtInsert.setString(4, "DP");
				pstmtInsert.setString(5, ls_depb_no);
				pstmtInsert.setString(6, despLineNo);
				pstmtInsert.setString(7, "P-BE");
				pstmtInsert.setString(8, ls_tran_id);
				pstmtInsert.setString(9, despLineNo);
				pstmtInsert.setString(10, itemCode);
				pstmtInsert.setString(11, unit);
				pstmtInsert.setDouble(12, quantityDec);
				pstmtInsert.setString(13, currCode);
				pstmtInsert.setDouble(14, exchRateDec);
				pstmtInsert.setDouble(15, taxAmountDec);//
				pstmtInsert.setString(16, "O");
				pstmtInsert.setTimestamp(17, tranDate);
				pstmtInsert.setString(18, "BASE");
				pstmtInsert.setString(19, "");
				pstmtInsert.setDouble(20, taxAmountDec);//
				pstmtInsert.setString(21, "0.000");
				pstmtInsert.setDouble(22, exchRateDec);
				pstmtInsert.executeUpdate();
				pstmtInsert.close();
				pstmtInsert = null;
				sqlSec = "update depb  SET   amount_dr  =  "
						+" (case when amount_dr is null then 0 else amount_dr end) + (? * ?) "
						+ " WHERE tran_id  = ? " ;
				pstmtInsert = conn.prepareStatement(sqlSec);
				//pstmtInsert.setDouble(1,depbvalue);
				pstmtInsert.setDouble(1,taxAmountDec);
				pstmtInsert.setDouble(2,exchRateDec);
				pstmtInsert.setString(3,ls_depb_no);
				int i= pstmtInsert.executeUpdate(); 
				pstmtInsert.close();
				pstmtInsert=null;				
				if(merrcode.length()==0)
				{
					merrcode = "success";//"itmDBAccessEJB.getErrorString("","VTCOMSUC",userId);		
										
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;											
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		
		}
		//valueXmlString.append("</Root>\r\n");	
		return merrcode ;
	}  // end of depb

	private Timestamp getCurrdateAppFormat()
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
        }
        return timestamp;
    }
	public String generateTranId(String s, String s1, Connection connection)
        throws Exception
    {
        Statement statement = null;
        ResultSet resultset = null;
        Object obj = null;
        String s3 = "";
        String s4 = null;
        Object obj1 = null;
        try
        {
            System.out.println("Welcome ur in tranid generator.......!");
            String s2 = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = '" + s + "'";           
            statement = connection.createStatement();
            resultset = statement.executeQuery(s2);
            String s5 = "";
            String s6 = "";
            String s7 = "";
            if(resultset.next())
            {
                s6 = resultset.getString(1);
                s7 = resultset.getString(2);
                s5 = resultset.getString(3);
            }
            resultset.close();
            resultset = null;                     
            String s8 = "";
            s8 = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
            s8 = s8 + "<Header></Header>";
            s8 = s8 + "<Detail1>";
            s8 = s8 + "<benefit>" + s1 + "</benefit>";
            s8 = s8 + "</Detail1></Root>";
            System.out.println("xmlValues :: " + s8);
            TransIDGenerator transidgenerator = new TransIDGenerator(s8, "LCK", CommonConstants.DB_NAME);
            s3 = transidgenerator.generateTranSeqID(s5, s7, s6, connection);
            System.out.println("tranId :: " + s3);
        }
        catch(SQLException sqlexception)
        {
            System.out.println("SQLException :Generating id[failed] : \n" + sqlexception.getMessage());
            sqlexception.printStackTrace();
        }
        catch(Exception exception)
        {
            System.out.println("Exception:Generating id [failed]:\n" + exception.getMessage());
        }
        finally
        {
            if(statement != null)
            {
                statement.close();
                statement = null;
            }
            if(resultset != null)
                resultset.close();
            resultset = null;
        }
        return s3;
    } // end of generateTranId 
}//end class