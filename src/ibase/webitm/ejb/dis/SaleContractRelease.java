package ibase.webitm.ejb.dis;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import javax.ejb.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import javax.naming.InitialContext;

import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;

import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.ejb.Stateless; // added for ejb3


//public class SaleContractReleaseEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class SaleContractRelease extends ProcessEJB implements SaleContractReleaseLocal, SaleContractReleaseRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	//Added By PriyankaC.
	FinCommon finCommon = new FinCommon(); 
	String userId = "";
	String custCode = "";
	String contractNo = "";
	String blankTaxString = null;
	String taxAmount = "0.0";
	//commented by manish mhatre on 07-nov-2019
	//start manish
	/*File filePtr = new File("C:\\pb10\\log\\pbnitrace.log");
	static
	{
			File mkd = new File("C:\\pb10\\log");
			if(!mkd.exists())
			{
				mkd.mkdirs(); 
				System.out.println(" Directory Built................ "+mkd);
			}
			
	}*/      //end manish
	/*
	public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("SaleContractReleaseEJB ejbCreate called.........");
			
		}
		catch (Exception e)
		{
			System.out.println("Exception :SaleContractReleaseEJB :ejbCreate :==>"+e);
			throw new CreateException();
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
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0 )
			{
				System.out.println("XML String *.....*:"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 				
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :SaleContractReleaseEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return rtrStr; 
	}

	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//Changed By PriyankaC on 17JAN2018...to generate [START]
	//	StringBuffer retTabSepStrBuff = new StringBuffer();
		StringBuffer retTabSepStrBuff = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?><DocumentRoot><description>Datawindow Root</description><group0>" +
		"<description>Group0 description</description><Header0><description>Header0 members</description>");
		////Changed By PriyankaC on 17JAN2018...to generate [END]
		java.sql.Timestamp contractDate = null;
		java.sql.Timestamp dspDate = null;
		Object date = null;
		String sql = "";
		String errCode = "";
		String errString = "";
		String resultString = "";
		String confirmed = "";
		String scontractDate = "";
		String sdspDate = "";
		double quantity = 0.0,pendingQty = 0.0;
		int count = 0;
		int i = 1;
		try
		{
//			writeLog(filePtr,"G E T D A T A I N P R O C E S S ",false);   //commented by manish mhatre on  07-nov-2019
//			writeLog(filePtr,"S C O N T R A C T U P D A T E D",false);   //commented by manish mhatre on  07-nov-2019
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out.println("xtraParams $$$$$$$$$$$$$$$$$$$$$$$$ "+xtraParams);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			SimpleDateFormat sdf  = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdfOutput = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			contractNo = genericUtility.getColumnValue("contract_no",headerDom);
			System.out.println("contractNo ::- "+ contractNo);
			if(contractNo == null || contractNo.trim().length() == 0)
			{
				//resultString = "VMSCONT1";
				errString = itmDBAccessEJB.getErrorString("","VMSCONT1","","",conn);
				return errString;
			}
			if(contractNo != null && contractNo.trim().length() != 0)
			{
				try
				{
					
					sql = "SELECT COUNT(1) AS COUNT FROM SCONTRACT WHERE CONTRACT_NO = '"+contractNo.trim()+"'";
//					writeLog(filePtr,sql,true);     //commented by manish mhatre on  07-nov-2019
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						count = rs.getInt("COUNT");
						System.out.println("CONRACT_NO count is ::- "+count );
						
					}
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					if(count == 0)
					{
//						writeLog(filePtr,"contract_no NOT FOUND IN SCONTRACT ",true);   //commented by manish mhatre on  07-nov-2019
						errCode = "VTSCONT1";
						errString = itmDBAccessEJB.getErrorString("contract_no",errCode,userId,"",conn);
						return errString;
					}
				}
				catch(Exception ex)
				{
					System.out.println("Exception []::"+sql+ex.getMessage());
					ex.printStackTrace();
				}
			}	
			custCode = genericUtility.getColumnValue("cust_code",headerDom);
			if(custCode == null || custCode.trim().length() ==0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTMSG","","",conn);
				return errString;
			}
			if(custCode != null && custCode.trim().length() != 0)
			{
				try
				{
					sql = "SELECT COUNT(1) AS COUNT FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
//					writeLog(filePtr,"Detecting Record Exist In CUSTOMER["+sql+"]",true);   //commented by manish mhatre on  07-nov-2019
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						count = rs.getInt("COUNT");
						System.out.println("CUST_CODE count is ::- "+count );
//						writeLog(filePtr,"Count:-"+count,true);   //commented by manish mhatre on  07-nov-2019
					}
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					
					
					if(count == 0)
					{
//						writeLog(filePtr,"CUSTOMER_CODE NOT FOUND IN CUSTOMER ",true);  //commented by manish mhatre on  07-nov-2019
						errCode = "VMCUSTCDX";
						errString = itmDBAccessEJB.getErrorString("cust_code",errCode,userId,"",conn);
						return errString;
					} 
					//*********Added on date 10102006 from by Taranisen meher validate to test customer no entry in filter should be 
					//contract customer
					else
					{
//						writeLog(filePtr,"Customer Found In Customer Master",true);   //commented by manish mhatre on  07-nov-2019
						if(contractNo!=null)
						{
							int count1=0,count2=0;
							
							sql = "SELECT COUNT(1) AS COUNT FROM SCONTACT_CUST WHERE CUST_CODE = '"+custCode+"' and CONTRACT_NO = '"+contractNo+"'" ;
//							writeLog(filePtr,"Detecting Record Exist In SCONTACT_CUST["+sql+"]",true);  //commented by manish mhatre on  07-nov-2019
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{ 
								count1 = rs.getInt("COUNT");
								System.out.println("Count[1] ::- "+count1 );
//								writeLog(filePtr,"Count[1]:::"+count1,true);    //commented by manish mhatre on  07-nov-2019
							}
							
							rs.close();
							stmt.close();
							rs = null;
							stmt = null;
							sql = "SELECT COUNT(1) AS COUNT FROM SCONTRACT WHERE CUST_CODE = '"+custCode+"' and CONTRACT_NO = '"+contractNo+"'" ;
//							writeLog(filePtr,"Detecting Record Exist In SCONTRACT   ["+sql+"]",true);   //commented by manish mhatre on  07-nov-2019
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								count2 = rs.getInt("COUNT");
//								writeLog(filePtr,"Count[2]:::"+count2,true);   //commented by manish mhatre on  07-nov-2019
								System.out.println("Count[2]::: ::- "+count2 );
							}
							rs.close();
							rs = null;
							stmt.close();
							stmt = null;
							if(count1==0 && count2==0)
							{
//								writeLog(filePtr,"CUSTOMER_CODE NOT FOUND IN CUSTOMER ",true);    //commented by manish mhatre on  07-nov-2019
								errCode = "VMCUSTCDX";
								errString = itmDBAccessEJB.getErrorString("cust_code",errCode,userId,"",conn);
								return errString;
							}
							
						}
					}
					
					
				}
				catch(Exception ex)
				{
					System.out.println("Exception []::"+sql+ex.getMessage());
					ex.printStackTrace();
				}
			}
			try
			{
				sql = "SELECT CONFIRMED FROM SCONTRACT WHERE CONTRACT_NO = '"+contractNo+"'";
//				writeLog(filePtr,sql,true);    //commented by manish mhatre on  07-nov-2019
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					confirmed = rs.getString(1);
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				System.out.println("confirmed =  "+confirmed );
				if(confirmed.trim().equalsIgnoreCase("N"))
				{
//					writeLog(filePtr,"CONFIRMED STATUS IS [N] ",true);     //commented by manish mhatre on  07-nov-2019
					errString = itmDBAccessEJB.getErrorString("","VTPCONUNC","","",conn);
					return errString;
				}
			}
			catch(Exception ex)
			{
				System.out.println("Exception []::"+sql+ex.getMessage());
				ex.printStackTrace();
			}
			try 
			{
				sql = "SELECT A.CONTRACT_NO,B.LINE_NO,A.CONTRACT_TYPE,A.CONTRACT_DATE,A.ITEM_SER,"
					+ "A.PRICE_LIST,A.CURR_CODE,C.DESCR,B.SITE_CODE,B.ITEM_CODE,B.ITEM_FLG,B.QUANTITY,"
					+ "(B.QUANTITY - CASE WHEN B.REL_QTY IS NULL THEN 0 ELSE B.REL_QTY END) PENDING_QUANTITY,"
					+ "B.UNIT,B.RATE,B.DISCOUNT,B.TAX_CLASS,B.TAX_CHAP,B.TAX_ENV,B.REMARKS,"
					+ "B.UNIT__RATE,B.CONV__QTY_STDUOM,B.CONV__RTUOM_STDUOM,B.UNIT__STD,"
					+ "B.QUANTITY__STDUOM,B.RATE__STDUOM,B.PACK_INSTR,B.NO_ART,B.REL_QTY_PER,"
					+ "B.PACK_CODE,B.RATE__CLG "
					+ "FROM SCONTRACT A,SCONTRACTDET B,CURRENCY C "
					+ "WHERE A.CONTRACT_NO = B.CONTRACT_NO "
					+ "AND A.CURR_CODE = C.CURR_CODE "
					+ "AND (B.QUANTITY - CASE WHEN B.REL_QTY IS NULL THEN 0 ELSE B.REL_QTY END) > 0 "
					+ "AND A.CONTRACT_NO = '"+contractNo+"'";
				pstmt = conn.prepareStatement(sql);
//				writeLog(filePtr,"GETDATA SQL=========>"+sql,true);  //commented by manish mhatre on  07-nov-2019
		
				System.out.println("The getData sql is.................:"+ sql);	
				rs = pstmt.executeQuery();
				
	       		while (rs.next())
				{
	       			//Changed By PriyankaC on 16JAN18 [START..]
	       			retTabSepStrBuff.append( "<Detail2 domID='"+i+++"'>" );
					retTabSepStrBuff.append( "<attribute updateFlag='A'/>" );
					//CONTRACT_NO
					//retTabSepStrBuff.append(rs.getString(1)==null?"":rs.getString(1)).append("\t");
	       			retTabSepStrBuff.append("<contract_no >").append(rs.getString(1)==null?"":rs.getString(1)).append("</contract_no>");

					//LINE_NO
				//	retTabSepStrBuff.append(rs.getString(2)==null?"":rs.getString(2)).append("\t");
	       			retTabSepStrBuff.append("<line_no >").append(rs.getString(2)==null?"":rs.getString(2)).append("</line_no>");

					//CONTRACT_TYPE
				//	retTabSepStrBuff.append(rs.getString(3)==null?"":rs.getString(3)).append("\t");
	       			retTabSepStrBuff.append("<contract_type >").append(rs.getString(3)==null?"":rs.getString(3)).append("</contract_type>");

					//CONTRACT_DATE
					contractDate = rs.getTimestamp(4);
					date = sdf.parse(contractDate.toString());
					scontractDate = sdfOutput.format(date).toString();
				//	retTabSepStrBuff.append(scontractDate).append("\t");
	       			retTabSepStrBuff.append("<contract_date >").append(scontractDate).append("</contract_date>");
					//ITEM_SER
					//retTabSepStrBuff.append(rs.getString(5)==null?"":rs.getString(5)).append("\t");
	       			retTabSepStrBuff.append("<item_ser >").append(rs.getString(5)==null?"":rs.getString(5)).append("</item_ser>");

					//PRICE_LIST
				//	retTabSepStrBuff.append(rs.getString(6)==null?"":rs.getString(6)).append("\t");	
	       			retTabSepStrBuff.append("<price_list >").append(rs.getString(6)==null?"":rs.getString(6)).append("</price_list>");

					//CURR_CODE
					//retTabSepStrBuff.append(rs.getString(7)==null?"":rs.getString(7)).append("\t");
	       			retTabSepStrBuff.append("<curr_code >").append(rs.getString(7)==null?"":rs.getString(7)).append("</curr_code>");

					//DESCR
					//retTabSepStrBuff.append(rs.getString(8)==null?"":rs.getString(8)).append("\t");
	       			retTabSepStrBuff.append("<descr>").append(rs.getString(8)==null?"":rs.getString(8)).append("</descr>");

					//SITE_CODE
					//retTabSepStrBuff.append(rs.getString(9)==null?"":rs.getString(9)).append("\t");
	       			retTabSepStrBuff.append("<site_code>").append(rs.getString(9)==null?"":rs.getString(9)).append("</site_code>");

					//ITEM_CODE
					//retTabSepStrBuff.append(rs.getString(10)==null?"":rs.getString(10)).append("\t");
	       			retTabSepStrBuff.append("<item_code>").append(rs.getString(10)==null?"":rs.getString(10)).append("</item_code>");

					//ITEM_FLG
				//	retTabSepStrBuff.append(rs.getString(11)==null?"":rs.getString(11)).append("\t");
	       			retTabSepStrBuff.append("<item_flg>").append(rs.getString(11)==null?"":rs.getString(11)).append("</item_flg>");

					//QUANTITY
					//retTabSepStrBuff.append(rs.getDouble(12)).append("\t");
	       			retTabSepStrBuff.append("<quantity>").append(rs.getString(12)==null?"":rs.getString(12)).append("</quantity>");

					//PENDING_QUANTITY
				//	retTabSepStrBuff.append(rs.getDouble(13)).append("\t");
	       			retTabSepStrBuff.append("<pending_quantity>").append(rs.getString(13)==null?"":rs.getString(13)).append("</pending_quantity>");

					//REL_QTY
					int relQty = 0;
					//retTabSepStrBuff.append(relQty).append("\t");
	       			retTabSepStrBuff.append("<rel_qty>").append(relQty).append("</rel_qty>");
					//UNIT
					//retTabSepStrBuff.append(rs.getString(14)).append("\t");
	       			retTabSepStrBuff.append("<unit>").append(rs.getString(14)==null?"":rs.getString(14)).append("</unit>");

					//RATE
					//retTabSepStrBuff.append(rs.getDouble(15)).append("\t");
	       			retTabSepStrBuff.append("<rate>").append(rs.getString(15)==null?"":rs.getString(15)).append("</rate>");

					//DISCOUNT
				//	retTabSepStrBuff.append(rs.getDouble(16)).append("\t");
	       			retTabSepStrBuff.append("<discount>").append(rs.getString(16)==null?"":rs.getString(16)).append("</discount>");

					//TAX_CLASS
				//	retTabSepStrBuff.append(rs.getString(17)==null?"":rs.getString(17)).append("\t");
	       			retTabSepStrBuff.append("<tax_class>").append(rs.getString(17)==null?"":rs.getString(17)).append("</tax_class>");

					//TAX_CHAP
					//retTabSepStrBuff.append(rs.getString(18)==null?"":rs.getString(18)).append("\t");
	       			retTabSepStrBuff.append("<tax_chap>").append(rs.getString(18)==null?"":rs.getString(18)).append("</tax_chap>");

					//TAX_ENV
					//retTabSepStrBuff.append(rs.getString(19)==null?"":rs.getString(19)).append("\t");
	       			retTabSepStrBuff.append("<tax_env>").append(rs.getString(19)==null?"":rs.getString(19)).append("</tax_env>");

					//REMARKS
				//	retTabSepStrBuff.append(rs.getString(20)==null?"":rs.getString(20)).append("\t");
	       			retTabSepStrBuff.append("<remarks>").append(rs.getString(20)==null?"":rs.getString(20)).append("</remarks>");
					//UNIT__RATE
				//	retTabSepStrBuff.append(rs.getString(21)==null?"":rs.getString(21)).append("\t");
	       			retTabSepStrBuff.append("<unit__rate>").append(rs.getString(21)==null?"":rs.getString(21)).append("</unit__rate>");

					//CONV__QTY_STDUOM
					//retTabSepStrBuff.append(rs.getString(22)==null?"":rs.getString(22)).append("\t");
	       			retTabSepStrBuff.append("<conv__qty_stduom>").append(rs.getString(22)==null?"":rs.getString(22)).append("</conv__qty_stduom>");

					//CONV__RTUOM_STDUOM
				//	retTabSepStrBuff.append(rs.getDouble(23)).append("\t");
	       			retTabSepStrBuff.append("<conv__rtuom_stduom>").append(rs.getString(23)==null?"":rs.getString(23)).append("</conv__rtuom_stduom>");

					//UNIT__STD
				//	retTabSepStrBuff.append(rs.getString(24)==null?"":rs.getString(24)).append("\t");
	       			retTabSepStrBuff.append("<unit__std>").append(rs.getString(24)==null?"":rs.getString(24)).append("</unit__std>");

					//QUANTITY__STDUOM
				//	retTabSepStrBuff.append(rs.getDouble(25)).append("\t");
	       			retTabSepStrBuff.append("<quantity__stduom>").append(rs.getString(25)==null?"":rs.getString(25)).append("</quantity__stduom>");

					//RATE__STDUOM
				//	retTabSepStrBuff.append(rs.getDouble(26)).append("\t");
	       			retTabSepStrBuff.append("<rate__stduom>").append(rs.getString(26)==null?"":rs.getString(26)).append("</rate__stduom>");

					//PACK_INSTR
					//retTabSepStrBuff.append(rs.getString(27)==null?"":rs.getString(27)).append("\t");
	       			retTabSepStrBuff.append("<pack_instr>").append(rs.getString(27)==null?"":rs.getString(27)).append("</pack_instr>");

					//NO_ART
				//	retTabSepStrBuff.append(rs.getDouble(28)).append("\t");
	       			retTabSepStrBuff.append("<no_art>").append(rs.getString(28)==null?"":rs.getString(28)).append("</no_art>");

					//REL_QTY_PER
				//	retTabSepStrBuff.append(rs.getDouble(29)).append("\t");
	       			retTabSepStrBuff.append("<rel_qty_per>").append(rs.getString(29)==null?"":rs.getString(29)).append("</rel_qty_per>");

					//PACK_CODE
					//retTabSepStrBuff.append(rs.getString(30)==null?"":rs.getString(30)).append("\t");
	       			retTabSepStrBuff.append("<pack_code>").append(rs.getString(30)==null?"":rs.getString(30)).append("</pack_code>");

					//RATE__CLG
					//retTabSepStrBuff.append(rs.getDouble(31)).append("\n");
	       			retTabSepStrBuff.append("<rate__clg>").append(rs.getString(31)==null?"":rs.getString(31)).append("</rate__clg>");
	       			retTabSepStrBuff.append( "</Detail2>" );
	       			//Changed By PriyankaC on 16JAN18 [END..]

				}//while
	       		
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				retTabSepStrBuff.append("</Header0></group0></DocumentRoot>"); //Added By PriyankaC on 17JAN2018
			}//try
			
			
			catch (SQLException e)
			{
//				writeLog(filePtr,e,true);     //commented by manish mhatre on  07-nov-2019
				System.out.println("SQLException ::" +sql+ e.getMessage() + ":");
			}
			catch(Exception ex)
			{
//				writeLog(filePtr,ex,true);     //commented by manish mhatre on  07-nov-2019
				System.out.println("Exception []::"+ex.getMessage());
				ex.printStackTrace();
			}
			resultString = retTabSepStrBuff.toString();	
//			writeLog(filePtr,"RETURN STRING ---"+resultString,true);    //commented by manish mhatre on  07-nov-2019
			System.out.println("resultString.........: " + resultString);
			if (!errCode.equals(""))
			{
				resultString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				System.out.println("resultString.........: " + resultString);
			}
		}//outer try
		catch (SQLException e)
		{
//			writeLog(filePtr,e,true);      //commented by manish mhatre on  07-nov-2019
			System.out.println("SQLException ::" +sql+ e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch(Exception ex)
		{
//			writeLog(filePtr,ex,true);      //commented by manish mhatre on  07-nov-2019
			System.out.println("Exception []::"+ex.getMessage());
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception e)
			{}
		}		
		return resultString;	
		
	}//getData()
	
	//process()
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		System.out.println("xmlString2--------------*>"+ xmlString2);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{	
		/*	this.fw = new FileWriter("C:\\pb10\\log\\trace.log");
			this.fw.close();	
			this.fw = new FileWriter("C:\\pb10\\log\\trace.log", true);*/
			
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
			}
		    retStr = process(headerDom, detailDom, windowName, xtraParams);  
		}
		catch (Exception e)
		{
			System.out.println("Exception :SaleContractReleaseEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
//			writeLog(filePtr,e,true);   //commented by manish mhatre on  07-nov-2019
			throw new ITMException(e);
		}
		return retStr;
	}
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		java.sql.Timestamp orderDate = new java.sql.Timestamp(System.currentTimeMillis());
		java.sql.Timestamp udfDate = null;
		ArrayList relArray = new ArrayList();
		String returnString = "";
		String sql = "",sql2 = "",chgUser = " ",chgTerm = " ";
		String saleOrder = "",contractType = "",siteCode = "",siteCodeShip = "",itemSer = "",transMode = "",currCode = "",currCodeIns = "";
		String orderDb = "",stanCodeInit = "",tele1 = "",tele2 = "",tele3 = "",fax = "",crTerm = "",dlvTerm = "";
		String accCodeSal = "",cctrCodeSal = "",remark = "",remark2 = "",remark3 = "",custCodeBill = "",custCodeDlv = "";
		String addr1 = "",addr2 = "",addr3 = "",city = "",pin = "",state = "",station = "",country = "",custName = "";
		String quotNo = "",taxOpt = "",priceList = "",partyQty = "",tranCode = "",custPord = "",spendingQty = "";
		String frtTerm = "",currCodeFrt = "",salesPers = "",commPercOn = "",commPercOn2 = "",distRoute = "";
		String consumeFc = "",projCode = "",currCodeCom = "",currCodeCom1 = "",currCodeCom2 = "",udfStr1 = "",udfStr3 = "",salesPers1 = "",salesPers2 = "";
		String squantity = "",srelQty = "",itemCode = "",srelQtyPer = "",unit = "",sconvQtyStd = "",srate = "",unitRate = "",sconvRtuomStd = "",sdiscount = "";
		String unitStd = "",srateStduom = "",packCode = "",packInstr = "",itemFlag = "",sqtyStuom = "",taxClass = "",taxChap = "",taxEnv = "",srateClg = "",snoArt = "";
		String priceListClg="";// added by nandkumar gadkari on 03/05/19
		double exchRateFrt=0,exchRateIns=0, balQtyStduom = 0,detailCnt = 0;// added by nandkumar gadkari on 14/05/19
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;	
		String childNodeName = "",empCode = "",itemDescr = "";
		java.sql.Timestamp today = null;							//added by manish mhatre on 7-nov-2019
		StringBuffer xmlBuff = null;                      			//added by manish mhatre on 7-nov-2019
		String xmlString = null, retString = null,sysDate = "",errString = "";     //added by manish mhatre on 7-nov-2019
		boolean bDetailFound = false;
		double taxAmt = 0.0,totAmt = 0.0,totAmtHdr = 0.0,taxAmtHdr = 0.0,ordAmtHdr = 0.0,exchRateCom = 0.0,exchRateCom1 = 0.0,exchRateCom2 = 0.0;
		double exchRate = 0.0,frtAmt = 0.0,commPerc = 0.0,commPerc1 = 0.0,commPerc2 = 0.0,advPerc = 0.0,udfNum1 = 0.0,udfNum2 = 0.0,pendingQty = 0.0;
		double quantity = 0.0,relQty = 0.0,relQtyPer = 0.0,convQtyStd = 0.0,rate = 0.0,convRtuomStd = 0.0,discount = 0.0,rateStduom = 0.0,qtyStuom = 0.0,rateClg = 0.0,noArt = 0.0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		String lineNo = "  1",line_No = "  1", status = "";
		
		java.sql.Timestamp dspDate = null;
		int row = 1,cnt = 0,cntClass = 0,cntEnv = 0,cntChap = 0;
		
		System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
		siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
		chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		System.out.println("empCode..............."+empCode);
		System.out.println("Site Code............."+siteCode);
		System.out.println("userId............."+chgUser);
		System.out.println("termId............."+chgTerm);
		try
		{
//			writeLog(filePtr,"I N P R O C E S S --------> ",true);     //commented by manish mhatre on  07-nov-2019
			contractNo = genericUtility.getColumnValue("contract_no",headerDom);
			custCode = genericUtility.getColumnValue("cust_code",headerDom);
			System.out.println("contractNo in process()  "+contractNo);
			System.out.println("custCode in process()  "+custCode);
			
			Object date = null;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(orderDate.toString());
			orderDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
			System.out.println("ORDER DATE ::::::"+orderDate);
			
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());	
			String tranDate = sdf1.format(date).toString();
			today = java.sql.Timestamp.valueOf(sdf.format(new java.util.Date()).toString() + " 00:00:00.0"); //added by manish mhatre on 7-nov-2019
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			//commented by rajendra for adding order_type
			//			saleOrder = generateTranTd("w_sorder",tranDate,siteCode);
			//			System.out.println("saleOrder....::- "+saleOrder);
			try
			{
				sql = "SELECT CONTRACT_TYPE,ITEM_SER,TRANS_MODE,CURR_CODE,CURR_CODE__INS,ORDER_DB,STAN_CODE__INIT,"
					+ "EXCH_RATE__COMM,EXCH_RATE__COMM_1,EXCH_RATE__COMM_2,ACCT_CODE__SAL,CCTR_CODE__SAL,REMARKS,REMARKS2,REMARKS3,"
					+ "CUST_PORD,QUOT_NO,TAX_OPT,PRICE_LIST,PART_QTY,EXCH_RATE,TRAN_CODE,"
					+ "FRT_TERM,FRT_AMT,CURR_CODE__FRT,SALES_PERS,COMM_PERC,COMM_PERC_1,COMM_PERC_2,COMM_PERC__ON,COMM_PERC_ON_2,ADV_PERC,"
					+ "DIST_ROUTE,CONSUME_FC,PROJ_CODE,UDF__NUM1,UDF__NUM2,UDF__STR1,UDF__STR3,UDF__DATE1,"
					+ "SALES_PERS__1,SALES_PERS__2,CURR_CODE__COMM,CURR_CODE__COMM_1,CURR_CODE__COMM_2,SITE_CODE,DLV_TERM,CR_TERM,  "
					+ "CASE WHEN SITE_CODE__SHIP IS NULL THEN SITE_CODE ELSE SITE_CODE__SHIP END AS SITE_CODE__SHIP,TAX_CLASS,TAX_CHAP,TAX_ENV "
					+ " ,PRICE_LIST__CLG,EXCH_RATE__FRT,EXCH_RATE__INS " //added by nandkumar gadkari on 03/05/19  
					+ "FROM SCONTRACT "
					+ "WHERE CONTRACT_NO = '"+contractNo+"'";
//					writeLog(filePtr,"PROCESS SQL"+sql,true);      //commented by manish mhatre on  07-nov-2019
					
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					//contractType = rs.getString("CONTRACT_TYPE");
					contractType = checkNullandTrim(rs.getString("CONTRACT_TYPE"));//changed by jaffar S on 20-03-2019
					System.out.println("CONTRACT_TYPE.............."+contractType);
					itemSer = checkNull(rs.getString("ITEM_SER"));
					transMode = checkNull(rs.getString("TRANS_MODE"));
					currCode = checkNull(rs.getString("CURR_CODE"));
					currCodeIns = checkNull(rs.getString("CURR_CODE__INS"));
					orderDb = checkNull(rs.getString("ORDER_DB"));
					stanCodeInit = checkNull(rs.getString("STAN_CODE__INIT"));
					exchRateCom = rs.getDouble("EXCH_RATE__COMM");
					exchRateCom1 = rs.getDouble("EXCH_RATE__COMM_1");
					exchRateCom2 = rs.getDouble("EXCH_RATE__COMM_2");
					accCodeSal = checkNull(rs.getString("ACCT_CODE__SAL"));
					cctrCodeSal = checkNull(rs.getString("CCTR_CODE__SAL"));
					remark = checkNull(rs.getString("REMARKS"));
					remark2 = checkNull(rs.getString("REMARKS2"));
					remark3 = checkNull(rs.getString("REMARKS3"));
					custPord = checkNull(rs.getString("CUST_PORD"));
					quotNo = checkNull(rs.getString("QUOT_NO"));
					taxOpt = checkNull(rs.getString("TAX_OPT"));
					priceList = checkNull(rs.getString("PRICE_LIST"));
					partyQty = checkNull(rs.getString("PART_QTY"));
					exchRate = rs.getDouble("EXCH_RATE");
					tranCode = checkNull(rs.getString("TRAN_CODE"));
					frtTerm = checkNull(rs.getString("FRT_TERM"));
					frtAmt = rs.getDouble("FRT_AMT");
					currCodeFrt = checkNull(rs.getString("CURR_CODE__FRT"));
					salesPers = checkNull(rs.getString("SALES_PERS"));
					commPerc = rs.getDouble("COMM_PERC");
					commPerc1 = rs.getDouble("COMM_PERC_1");
					commPerc2 = rs.getDouble("COMM_PERC_2");
					commPercOn = checkNull(rs.getString("COMM_PERC__ON"));
					commPercOn2 = checkNull(rs.getString("COMM_PERC_ON_2"));
					//commPercOn2= commPercOn2 == null ? "" : commPercOn2;//added by nandkumar gadkari on 16/12/19
					advPerc = rs.getDouble("ADV_PERC");
					distRoute = checkNull(rs.getString("DIST_ROUTE"));
					consumeFc = checkNull(rs.getString("CONSUME_FC"));
					projCode = checkNull(rs.getString("PROJ_CODE"));
					udfNum1 = rs.getDouble("UDF__NUM1");
					udfNum2 = rs.getDouble("UDF__NUM2");
					udfStr1 = checkNull(rs.getString("UDF__STR1"));
					udfStr3 = checkNull(rs.getString("UDF__STR3"));
					udfDate = rs.getTimestamp("UDF__DATE1");
					salesPers1 = checkNull(rs.getString("SALES_PERS__1"));
					salesPers2 = checkNull(rs.getString("SALES_PERS__2"));
					currCodeCom = checkNull(rs.getString("CURR_CODE__COMM"));
					currCodeCom1 = checkNull(rs.getString("CURR_CODE__COMM_1"));
					currCodeCom2 = checkNull(rs.getString("CURR_CODE__COMM_2"));
					siteCode = checkNull(rs.getString("SITE_CODE"));
					dlvTerm = checkNull(rs.getString("DLV_TERM"));
					crTerm = checkNull(rs.getString("CR_TERM"));
					// 09-08-2006 manoharan
					siteCodeShip = rs.getString("SITE_CODE__SHIP");
					// end 09-08-2006 manoharan
					// added by manaszir on 2/27/2009
					taxClass = rs.getString("TAX_CLASS")==null ?"":rs.getString("TAX_CLASS");
					System.out.println("taxClass.............."+taxClass);
					taxChap = rs.getString("TAX_CHAP") == null ?"":rs.getString("TAX_CHAP");
					System.out.println("taxChap.............."+taxChap);
					taxEnv = rs.getString("TAX_ENV")== null ? "":rs.getString("TAX_ENV");
					System.out.println("taxEnv.............."+taxEnv);
					priceListClg = rs.getString("PRICE_LIST__CLG");//added by nandkumar gadkari on 03/05/19		
					//added by nandkumar gadkari on 14/05/19	----------start
					exchRateFrt = rs.getDouble("EXCH_RATE__FRT");
					exchRateIns = rs.getDouble("EXCH_RATE__INS");
					//added by nandkumar gadkari on 14/05/19	----------end
					// end of code on 2/27/2009 manazir
					System.out.println("DLV_TERM.............."+dlvTerm);
					System.out.println("siteCode.............."+siteCode);
					if(itemSer == null)
					{
						itemSer = "";
					}
					//Added by PriyankaC to set account code sale as per item account detrmination on 9june2019 [Start]
					if(accCodeSal == null || accCodeSal.trim().length() == 0)
					{
						accCodeSal = finCommon.getAcctDetrTtype(itemCode, itemSer, "SAL", contractType, conn);
						String lsAcctcodeSalArr[] = accCodeSal.split(",");
						
						if (lsAcctcodeSalArr.length > 0) 
						{
							accCodeSal = lsAcctcodeSalArr[0];
						} else 
						{
							accCodeSal = "";
						}
						if (lsAcctcodeSalArr.length > 0) {
							cctrCodeSal = lsAcctcodeSalArr[1];
						} else 
						{
							cctrCodeSal = "";
						}
					    System.out.println("Value oF acctCodeSal :"+accCodeSal);
						
					}
					// Added By PriyankaC to set account code sale as per item account detrmination on 9june2019 [END]
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			}
			catch (SQLException ex)
			{
				System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
				ex.printStackTrace();
//				writeLog(filePtr,ex,true);     //commented by manish mhatre on  07-nov-2019
				throw new ITMException(ex);
			}
			catch (Exception e)
			{
				System.out.println("Exception ::process():" +sql+ e.getMessage() + ":");
				e.printStackTrace();
//				writeLog(filePtr,e,true);     //commented by manish mhatre on  07-nov-2019
				throw new ITMException(e);
			}
			try
			{	
				sql = "SELECT ADDR1,ADDR2,ADDR3,CITY,STATE_CODE,PIN,COUNT_CODE,STAN_CODE,"+
					  "CUST_NAME,TELE1,TELE2,TELE3,FAX "+
					  "FROM CUSTOMER WHERE CUST_CODE = '"+custCode+"'";
//					  	writeLog(filePtr," sql"+sql,true);      //commented by manish mhatre on  07-nov-2019
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					addr1 = rs.getString(1);
					addr2 = rs.getString(2);
					addr3 = rs.getString(3);
					city  = rs.getString(4);
					state = rs.getString(5);
					pin = rs.getString(6);
					country = rs.getString(7);
					station = rs.getString(8);
					custName = rs.getString(9);
					tele1 = rs.getString(10);
					tele2 = rs.getString(11); 
					tele3 = rs.getString(12); 
					fax = rs.getString(13);
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			}//try
			catch (SQLException ex)
			{
				System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
				ex.printStackTrace();
//				writeLog(filePtr,ex,true);       //commented by manish mhatre on  07-nov-2019
				
				throw new ITMException(ex);
			}
			catch (Exception e)
			{
				System.out.println("Exception ::process():" +sql+ e.getMessage() + ":");
				e.printStackTrace();
//				writeLog(filePtr,e,true);       //commented by manish mhatre on  07-nov-2019
				
				throw new ITMException(e);
			}
			try
			{
				//Inserting vlues in SORDER
				/*saleOrder = generateTranTd("w_sorder",tranDate,siteCode,contractType, conn);
				System.out.println("saleOrder....::- "+saleOrder);*/

			//Commented and added by sarita to add pl_date in query and set orderDate to pl_date on 1 AUG 18 [START]	
			/*	sql = "INSERT INTO SORDER "+
					  "(SALE_ORDER,ORDER_TYPE,ORDER_DATE,CUST_CODE,CUST_CODE__DLV,CUST_CODE__BIL,TAX_OPT,ITEM_SER,"+
					  "CR_TERM,SITE_CODE,CURR_CODE,EXCH_RATE,DLV_TERM,TRANS_MODE,CHG_DATE,CHG_USER,CHG_TERM,"+
					  "DLV_ADD1,DLV_ADD2,DLV_ADD3,DLV_CITY,STATE_CODE__DLV,COUNT_CODE__DLV,DLV_PIN,STAN_CODE,"+
					  "CURR_CODE__INS,CUST_PORD,PRICE_LIST,TRAN_CODE,ORDER_DB,STAN_CODE__INIT,EXCH_RATE__COMM,"+
					  "EXCH_RATE__COMM_1,EXCH_RATE__COMM_2,ACCT_CODE__SAL,CCTR_CODE__SAL,REMARKS,REMARKS2,REMARKS3,"+
					  "QUOT_NO,PART_QTY,FRT_TERM,FRT_AMT,CURR_CODE__FRT,SALES_PERS,COMM_PERC,COMM_PERC_1,COMM_PERC_2,"+
					  "COMM_PERC__ON,COMM_PERC_ON_2,ADV_PERC,DIST_ROUTE,CONSUME_FC,PROJ_CODE,UDF__NUM1,UDF__NUM2,UDF__STR1,"+
					  "UDF__DATE1,SALES_PERS__1,SALES_PERS__2,CURR_CODE__COMM,CURR_CODE__COMM_1,CURR_CODE__COMM_2,CONTRACT_NO, " +
					  "CONFIRMED, STATUS,DUE_DATE, SITE_CODE__SHIP, TAX_DATE,EMP_CODE__ORD,TAX_CLASS,TAX_CHAP,TAX_ENV) "+
					  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'N','P',?,?,?,?,?,?,?)";*/
				//commented by manish mhatre on 7-nov-2019
				//start manish
			/*	sql = "INSERT INTO SORDER "+
						  "(SALE_ORDER,ORDER_TYPE,ORDER_DATE,CUST_CODE,CUST_CODE__DLV,CUST_CODE__BIL,TAX_OPT,ITEM_SER,"+
						  "CR_TERM,SITE_CODE,CURR_CODE,EXCH_RATE,DLV_TERM,TRANS_MODE,CHG_DATE,CHG_USER,CHG_TERM,"+
						  "DLV_ADD1,DLV_ADD2,DLV_ADD3,DLV_CITY,STATE_CODE__DLV,COUNT_CODE__DLV,DLV_PIN,STAN_CODE,"+
						  "CURR_CODE__INS,CUST_PORD,PRICE_LIST,TRAN_CODE,ORDER_DB,STAN_CODE__INIT,EXCH_RATE__COMM,"+
						  "EXCH_RATE__COMM_1,EXCH_RATE__COMM_2,ACCT_CODE__SAL,CCTR_CODE__SAL,REMARKS,REMARKS2,REMARKS3,"+
						  "QUOT_NO,PART_QTY,FRT_TERM,FRT_AMT,CURR_CODE__FRT,SALES_PERS,COMM_PERC,COMM_PERC_1,COMM_PERC_2,"+
						  "COMM_PERC__ON,COMM_PERC_ON_2,ADV_PERC,DIST_ROUTE,CONSUME_FC,PROJ_CODE,UDF__NUM1,UDF__NUM2,UDF__STR1,"+
						  "UDF__DATE1,SALES_PERS__1,SALES_PERS__2,CURR_CODE__COMM,CURR_CODE__COMM_1,CURR_CODE__COMM_2,CONTRACT_NO, " +
						  "CONFIRMED, STATUS,DUE_DATE, SITE_CODE__SHIP, TAX_DATE,EMP_CODE__ORD,TAX_CLASS,TAX_CHAP,TAX_ENV,PL_DATE ,PRICE_LIST__CLG,EXCH_RATE__FRT,EXCH_RATE__INS) "+ //PRICE_LIST__CLG column added by nandkumar gadkari on 03/05/19
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'N','P',?,?,?,?,?,?,?,?,?,?,?)";
				//Commented and added by sarita to add pl_date in query and set orderDate to pl_date on 1 AUG 18 [END]
				pstmt = conn.prepareStatement(sql);
				
				pstmt.clearParameters();
				pstmt.setString(1,saleOrder);
				pstmt.setString(2,contractType);
				pstmt.setTimestamp(3,orderDate);
				pstmt.setString(4,custCode);	
				pstmt.setString(5,custCode);	
				pstmt.setString(6,custCode);	
				pstmt.setString(7,taxOpt);	
				pstmt.setString(8,itemSer);
				pstmt.setString(9,crTerm);	
				pstmt.setString(10,siteCode);	
				pstmt.setString(11,currCode);
				if(currCode == null)
				{
					pstmt.setNull(11,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(11,currCode);
				}
				pstmt.setDouble(12,exchRate);
				pstmt.setString(13,dlvTerm);
				pstmt.setString(14,transMode);
				pstmt.setTimestamp(15,orderDate);
				pstmt.setString(16,chgUser);
				pstmt.setString(17,chgTerm);
				pstmt.setString(18,addr1);
				pstmt.setString(19,addr2);
				pstmt.setString(20,addr3);
				pstmt.setString(21,city);
				pstmt.setString(22,state);
				pstmt.setString(23,country);
				pstmt.setString(24,pin);
				pstmt.setString(25,station);
				pstmt.setString(26,currCodeIns);
				if(currCodeIns == null)
				{
					pstmt.setNull(26,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(26,currCodeIns);
				}
				pstmt.setString(27,custPord);
				pstmt.setString(28,priceList);
				pstmt.setString(29,tranCode);
				pstmt.setString(30,orderDb);
				pstmt.setString(31,stanCodeInit);
				pstmt.setDouble(32,exchRateCom);
				pstmt.setDouble(33,exchRateCom1);
				pstmt.setDouble(34,exchRateCom2);
				pstmt.setString(35,accCodeSal);
				pstmt.setString(36,cctrCodeSal);
				pstmt.setString(37,remark);
				pstmt.setString(38,remark2);
				pstmt.setString(39,remark3);
				pstmt.setString(40,quotNo);
				pstmt.setString(41,partyQty);
				pstmt.setString(42,frtTerm);
				pstmt.setDouble(43,frtAmt);
				pstmt.setString(44,currCodeFrt);
				pstmt.setString(45,salesPers);
				pstmt.setDouble(46,commPerc);
				pstmt.setDouble(47,commPerc1);
				pstmt.setDouble(48,commPerc2);
				pstmt.setString(49,commPercOn);
				pstmt.setString(50,commPercOn2);
				pstmt.setDouble(51,advPerc);
				if(distRoute == null)
				{
					pstmt.setNull(52,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(52,distRoute);
				}
				pstmt.setString(53,consumeFc);
				pstmt.setString(54,projCode);
				pstmt.setDouble(55,udfNum1);
				pstmt.setDouble(56,udfNum2);
				pstmt.setString(57,udfStr1);
				pstmt.setTimestamp(58,udfDate);
				if(salesPers1 == null)
				{
					pstmt.setNull(59,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(59,salesPers1);
				}
				if(salesPers2 == null)
				{
					pstmt.setNull(60,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(60,salesPers2);
				}
				if(currCodeCom == null || currCodeCom.trim().length() == 0)
				{
					pstmt.setNull(61,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(61,currCodeCom);
				}
				if(currCodeCom1 == null || currCodeCom1.trim().length() == 0)
				{
					pstmt.setNull(62,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(62,currCodeCom1);
				}
				if(currCodeCom2 == null || currCodeCom2.trim().length() == 0)
				{
					pstmt.setNull(63,java.sql.Types.VARCHAR);
				}
				else
				{
					pstmt.setString(63,currCodeCom2);
				}
				pstmt.setString(64,contractNo);
				// 09-08-2006 manoharan
				pstmt.setTimestamp(65,orderDate);
				pstmt.setString(66,siteCodeShip);
				// end 09-08-2006 manoharan
				// 10-08-06 Jiten
				pstmt.setTimestamp(67,orderDate);
				// END 10-08-06 
				pstmt.setString(68,empCode);//added by 21-09-06 wasim

				// added by manazir on 2/27/2009
				pstmt.setString(69,taxClass);
				pstmt.setString(70,taxChap);
				pstmt.setString(71,taxEnv);

				// end of code on 2/27/2009 by manazir
				//Added by sarita to set orderDate to pl_date on 1 AUG 18 [START]
				pstmt.setTimestamp(72,orderDate);
				//Added by sarita to set orderDate to pl_date on 1 AUG 18 [END]
				pstmt.setString(73,priceListClg);//PRICE_LIST__CLG column added by nandkumar gadkari on 03/05/19
				//added by nandkumar gadkari on 14/05/19----start------------
				pstmt.setDouble(74,exchRateFrt);
				pstmt.setDouble(75,exchRateIns);
				//added by nandkumar gadkari on 14/05/19----end------------
				int count = pstmt.executeUpdate(); */  //end manish
				
				//added by manish mhatre on 7-nov-2019
				//start manish
				xmlBuff = null;
				xmlBuff = new StringBuffer();
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
	
                //xmlBuff.append("<objName><![CDATA[").append("sorder_insert").append("]]></objName>"); 
                xmlBuff.append("<objName><![CDATA[").append("sorder").append("]]></objName>"); //manish
				
				xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
				xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuff.append("<description>").append("Header0 members").append("</description>");
                //xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorder_insert\" objContext=\"1\">");
                xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorder\" objContext=\"1\">");  //manish
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<sale_order/>");
				xmlBuff.append("<order_type><![CDATA[" + contractType +"]]></order_type>");
				System.out.println("contarct type>>>>>"+contractType);
				xmlBuff.append("<order_date><![CDATA[" + sdf1.format(orderDate).toString() + "]]></order_date>");
				xmlBuff.append("<cust_code><![CDATA[" + custCode +"]]></cust_code>");
				xmlBuff.append("<cust_code__dlv><![CDATA[" + custCode +"]]></cust_code__dlv>");
				xmlBuff.append("<cust_code__bil><![CDATA[" + custCode +"]]></cust_code__bil>");
				xmlBuff.append("<tax_opt><![CDATA[" + taxOpt +"]]></tax_opt>");
				xmlBuff.append("<item_ser><![CDATA[" + itemSer +"]]></item_ser>");
				xmlBuff.append("<cr_term><![CDATA[" + crTerm +"]]></cr_term>");
				xmlBuff.append("<site_code><![CDATA[" + siteCode +"]]></site_code>");
				if(currCode == null)
				{
					xmlBuff.append("<curr_code/>");
				}
				else
				{
					xmlBuff.append("<curr_code><![CDATA[" + currCode +"]]></curr_code>");
				}
				
				xmlBuff.append("<exch_rate><![CDATA[" + exchRate +"]]></exch_rate>");
				xmlBuff.append("<dlv_term><![CDATA[" + dlvTerm +"]]></dlv_term>");
				xmlBuff.append("<trans_mode><![CDATA[" + transMode +"]]></trans_mode>");
				xmlBuff.append("<chg_date><![CDATA[" + sdf1.format(orderDate).toString() + "]]></chg_date>");
				xmlBuff.append("<chg_user><![CDATA[" + chgUser +"]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA[" + chgTerm +"]]></chg_term>");
				xmlBuff.append("<dlv_add1><![CDATA[" + addr1 +"]]></dlv_add1>");
				xmlBuff.append("<dlv_add2><![CDATA[" + addr2 +"]]></dlv_add2>");
				xmlBuff.append("<dlv_add3><![CDATA[" + addr3 +"]]></dlv_add3>");
				xmlBuff.append("<dlv_city><![CDATA[" + city +"]]></dlv_city>");
				xmlBuff.append("<state_code__dlv><![CDATA[" + state +"]]></state_code__dlv>");
				xmlBuff.append("<count_code__dlv><![CDATA[" + country +"]]></count_code__dlv>");
				xmlBuff.append("<dlv_pin><![CDATA[" + pin +"]]></dlv_pin>");
				xmlBuff.append("<stan_code><![CDATA[" + station +"]]></stan_code>");
				if(currCodeIns == null)
				{
					xmlBuff.append("<curr_code__ins/>");
				}
				else
				{
					xmlBuff.append("<curr_code__ins><![CDATA[" + currCodeIns +"]]></curr_code__ins>");
				}
			
				xmlBuff.append("<cust_pord><![CDATA[" + custPord +"]]></cust_pord>");
				xmlBuff.append("<price_list><![CDATA[" + priceList +"]]></price_list>");
				xmlBuff.append("<tran_code><![CDATA[" + tranCode +"]]></tran_code>");
				xmlBuff.append("<order_db><![CDATA[" + orderDb +"]]></order_db>");
				xmlBuff.append("<stan_code__init><![CDATA[" + stanCodeInit +"]]></stan_code__init>");
				xmlBuff.append("<exch_rate__comm><![CDATA[" + exchRateCom +"]]></exch_rate__comm>");
				xmlBuff.append("<exch_rate__comm_1><![CDATA[" + exchRateCom1 +"]]></exch_rate__comm_1>");
				xmlBuff.append("<exch_rate__comm_2><![CDATA[" + exchRateCom2 +"]]></exch_rate__comm_2>");
				xmlBuff.append("<acct_code__sal><![CDATA[" + accCodeSal +"]]></acct_code__sal>");
				xmlBuff.append("<cctr_code__sal><![CDATA[" + cctrCodeSal +"]]></cctr_code__sal>");
				xmlBuff.append("<remarks><![CDATA[" + remark +"]]></remarks>");
				xmlBuff.append("<remarks2><![CDATA[" + remark2 +"]]></remarks2>");
				xmlBuff.append("<remarks3><![CDATA[" + remark3 +"]]></remarks3>");
				xmlBuff.append("<quot_no><![CDATA[" + quotNo +"]]></quot_no>");
				xmlBuff.append("<part_qty><![CDATA[" + partyQty +"]]></part_qty>");
				xmlBuff.append("<frt_term><![CDATA[" + frtTerm +"]]></frt_term>");
				xmlBuff.append("<frt_amt><![CDATA[" + frtAmt +"]]></frt_amt>");
				xmlBuff.append("<curr_code__frt><![CDATA[" + currCodeFrt +"]]></curr_code__frt>");
				xmlBuff.append("<sales_pers><![CDATA[" + salesPers +"]]></sales_pers>");
				xmlBuff.append("<comm_perc><![CDATA[" + commPerc +"]]></comm_perc>");
				xmlBuff.append("<comm_perc_1><![CDATA[" + commPerc1 +"]]></comm_perc_1>");
				xmlBuff.append("<comm_perc_2><![CDATA[" + commPerc2 +"]]></comm_perc_2>");
				xmlBuff.append("<comm_perc__on><![CDATA[" + commPercOn +"]]></comm_perc__on>");
				xmlBuff.append("<comm_perc_on_2><![CDATA[" + commPercOn2 +"]]></comm_perc_on_2>");
				xmlBuff.append("<adv_perc><![CDATA[" + advPerc +"]]></adv_perc>");
				if(distRoute == null)
				{
					xmlBuff.append("<dist_route/>");
				}
				else
				{
					xmlBuff.append("<dist_route><![CDATA[" + distRoute +"]]></dist_route>");
				}
				
				xmlBuff.append("<consume_fc><![CDATA[" + consumeFc +"]]></consume_fc>");
				xmlBuff.append("<proj_code><![CDATA[" + projCode +"]]></proj_code>");
				xmlBuff.append("<udf__num1><![CDATA[" + udfNum1 +"]]></udf__num1>");
				xmlBuff.append("<udf__num2><![CDATA[" + udfNum2 +"]]></udf__num2>");
				xmlBuff.append("<udf__str1><![CDATA[" + udfStr1 +"]]></udf__str1>");
				if(udfDate!= null)
				{	
					xmlBuff.append("<udf__date1><![CDATA[" + sdf.format(udfDate) +"]]></udf__date1>");
				}else {
					
					xmlBuff.append("<udf__date1/>");
				}
				if(salesPers1 == null)
				{
					xmlBuff.append("<sales_pers__1/>");
				}
				else
				{
					xmlBuff.append("<sales_pers__1><![CDATA[" + salesPers1 +"]]></sales_pers__1>");
				}
				if(salesPers2 == null)
				{
					xmlBuff.append("<sales_pers__2/>");
				}
				else
				{
					xmlBuff.append("<sales_pers__2><![CDATA[" + salesPers2 +"]]></sales_pers__2>");
				}
				if(currCodeCom == null || currCodeCom.trim().length() == 0)
				{
					xmlBuff.append("<curr_code__comm/>");
				}
				else
				{
					xmlBuff.append("<curr_code__comm><![CDATA[" + currCodeCom +"]]></curr_code__comm>");
				}
				if(currCodeCom1 == null || currCodeCom1.trim().length() == 0)
				{
					xmlBuff.append("<curr_code__comm_1/>");
				}
				else
				{
					xmlBuff.append("<curr_code__comm_1><![CDATA[" + currCodeCom1 +"]]></curr_code__comm_1>");
				}
				if(currCodeCom2 == null || currCodeCom2.trim().length() == 0)
				{
					xmlBuff.append("<curr_code__comm_2/>");
				}
				else
				{
					xmlBuff.append("<curr_code__comm_2><![CDATA[" + currCodeCom2 +"]]></curr_code__comm_2>");
				}
			
				xmlBuff.append("<contract_no><![CDATA[" + contractNo +"]]></contract_no>");
				xmlBuff.append("<due_date><![CDATA[" + sdf1.format(orderDate).toString() + "]]></due_date>");
				xmlBuff.append("<site_code__ship><![CDATA[" + siteCodeShip +"]]></site_code__ship>");
				xmlBuff.append("<tax_date><![CDATA[" + sdf1.format(today).toString() + "]]></tax_date>");
				xmlBuff.append("<emp_code__ord><![CDATA[" + empCode +"]]></emp_code__ord>");
				xmlBuff.append("<tax_class><![CDATA[" + taxClass +"]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA[" + taxChap +"]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA[" + taxEnv +"]]></tax_env>");
				xmlBuff.append("<pl_date><![CDATA[" + sdf1.format(orderDate).toString() + "]]></pl_date>");
				xmlBuff.append("<price_list__clg><![CDATA[" + priceListClg +"]]></price_list__clg>");
				xmlBuff.append("<exch_rate__frt><![CDATA[" + exchRateFrt +"]]></exch_rate__frt>");
				xmlBuff.append("<exch_rate__ins><![CDATA[" + exchRateIns +"]]></exch_rate__ins>");
				xmlBuff.append("</Detail1>");	
				//end manish
				
				//System.out.println("No of records inserted into sorder........."+count);
				/*pstmt.clearParameters();
				pstmt.close();
				pstmt = null;*/
			}
			
			/*catch (SQLException ex)
			{
				System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
				ex.printStackTrace();
				//writeLog(filePtr,ex,true);  //commented by manish mhatre on  07-nov-2019
				
				conn.rollback();
				returnString= itmDBAccessEJB.getErrorString("","VTSOGENF1","","",conn);
				return returnString;
			}*/
			catch (Exception e)
			{
				System.out.println("Exception ::process():" +sql+ e.getMessage() + ":");
				e.printStackTrace();
				//writeLog(filePtr,e,true);    //commented by manish mhatre on  07-nov-2019
			
				conn.rollback();
				throw new ITMException(e);
			}
			try
			{
				parentNodeList = detailDom.getElementsByTagName("Detail2");
				parentNodeListLength = parentNodeList.getLength(); 
				System.out.println("ParentNodeListLength....::-  "+parentNodeListLength);
				for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
				{
					parentNode = parentNodeList.item(selectedRow);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("ChildNodeListLength.........:-"+ childNodeListLength);
					for (int childRow = 0; childRow < childNodeListLength; childRow++)
					{
						childNode = childNodeList.item(childRow);
						childNodeName = childNode.getNodeName();

						
						if (childNodeName.equals("line_no"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								line_No = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								line_No = "";
							}
							System.out.println("line_no:::::"+ line_No);	
						}
						if (childNodeName.equals("quantity"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								squantity = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								squantity = "0";
							}
							quantity = Double.parseDouble(squantity);
							System.out.println("quantity:::::"+ quantity);	
						} 
						if (childNodeName.equals("rel_qty"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								srelQty = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								srelQty = "0";
							}
							relArray.add(srelQty);
							System.out.println("Release quantity array..."+relArray);
							relQty = Double.parseDouble(srelQty);
							System.out.println("rel_qty:::::"+ relQty);
						}
						if (childNodeName.equals("item_code"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								itemCode = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								itemCode = "";
							}
							System.out.println("item_code:::::"+ itemCode);	
						}
						if (childNodeName.equals("rel_qty_per"))
						{

							if (childNode.getFirstChild() != null) // ****************
							{
								srelQtyPer = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								srelQtyPer = "0";
							}
							relQtyPer = Double.parseDouble(srelQtyPer);
							System.out.println("rel_qty_per:::::"+ relQtyPer);	
						}
						if (childNodeName.equals("unit"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								unit = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								unit = "";
							}
							System.out.println("unit:::::"+ unit);	
						}
						if (childNodeName.equals("conv__qty_stduom"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								sconvQtyStd = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								sconvQtyStd = "0";
							}
							
							convQtyStd = Double.parseDouble(sconvQtyStd);
							System.out.println("conv__qty_stduom:::::"+ convQtyStd);	
						}
						if (childNodeName.equals("rate"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								srate = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								srate = "0";
							}
							rate = Double.parseDouble(srate);
							System.out.println("rate:::::"+ rate);	
						}
						if (childNodeName.equals("unit__rate"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								unitRate = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								unitRate = "";
							}
							
							System.out.println("unit__rate:::::"+ unitRate);	
						}
						if (childNodeName.equals("conv__rtuom_stduom"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								sconvRtuomStd = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								sconvRtuomStd = "0";
							}
							
							convRtuomStd = Double.parseDouble(sconvRtuomStd);
							System.out.println("conv__rtuom_stduom:::::"+ convRtuomStd);	
						}
						if (childNodeName.equals("discount"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								sdiscount = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								sdiscount = "0";
							}
							
							discount = Double.parseDouble(sdiscount);
							System.out.println("discount:::::"+ discount);	
						}
						if (childNodeName.equals("unit__std"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								unitStd = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								unitStd = "";
							}
							
							System.out.println("unit__std:::::"+ unitStd);	
						}
						if (childNodeName.equals("rate__stduom"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								srateStduom = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								srateStduom = "0";
							}
							
							rateStduom = Double.parseDouble(srateStduom);
							System.out.println("rate__stduom:::::"+ rateStduom);	
						}
						if (childNodeName.equals("pack_code"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								packCode = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								packCode = "";
							}
							
							System.out.println("pack_code:::::"+ packCode);	
						}
						if (childNodeName.equals("pack_instr"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								packInstr = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								packInstr = "";
							}
							
							System.out.println("pack_instr:::::"+ packInstr);	
						}
						if (childNodeName.equals("item_flg"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								itemFlag = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								itemFlag = "";
							}
							
							System.out.println("item_flg:::::"+ itemFlag);	
						}
						if (childNodeName.equals("quantity__stduom"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								sqtyStuom = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								sqtyStuom = "0";
							}
							qtyStuom = Double.parseDouble(sqtyStuom);
							System.out.println("quantity__stduom:::::"+ qtyStuom);	
						}
						if (childNodeName.equals("tax_class"))
						{
							taxClass = "";
							if (childNode.getFirstChild() != null)
							{
								taxClass = (childNode.getFirstChild().getNodeValue()).trim();
							}
							
							//if(taxClass == null || taxClass.trim().length() == 0 )
							//{
//								writeLog(filePtr,"taxClass::::::::::"+taxClass,true);      //commented by manish mhatre on  07-nov-2019
								
							//}
							//System.out.println("tax_class:::::"+ taxClass);	
						}
						if (childNodeName.equals("tax_env"))
						{
							taxEnv = "";
							if (childNode.getFirstChild() != null)
							{
								taxEnv = (childNode.getFirstChild().getNodeValue()).trim();
							}
							//if(taxEnv == null || taxEnv.trim().length() == 0 || taxEnv.equals("null"))
							//{
							//	taxEnv = "";
							//}
							System.out.println("tax_env:::::"+ taxEnv);	
						}
						if (childNodeName.equals("tax_chap"))
						{
							taxChap = "";
							if (childNode.getFirstChild() != null)
							{
								taxChap = (childNode.getFirstChild().getNodeValue()); //.trim();
							}
							//if(taxChap == null || taxChap.trim().length() == 0)
							//{
							//	taxChap = "";
							//}
							System.out.println("tax_chap:::::"+ taxChap);	
						}
						
						if (childNodeName.equals("rate__clg"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								srateClg = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								srateClg = "0";
							}
							
							rateClg = Double.parseDouble(srateClg);
							System.out.println("rate__clg:::::"+ rateClg);	
						}
						if (childNodeName.equals("no_art"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								snoArt = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								snoArt = "0";
							}
							noArt = Double.parseDouble(snoArt);
							System.out.println("no_art:::::"+ noArt);	
						}
						if (childNodeName.equals("pending_quantity"))
						{
							if (childNode.getFirstChild() != null) // ****************
							{
								spendingQty = (childNode.getFirstChild().getNodeValue()).trim();
							}
							else
							{
								spendingQty = "0";
							}
							pendingQty = Double.parseDouble(spendingQty);
							System.out.println("pending_quantity:::::"+ pendingQty);	
						}
					}//inner for 
					line_No = (("    "+line_No).substring(("    "+line_No).length()-3));
					if (relQty > 0 )
					{
						bDetailFound = true;
						if((pendingQty - relQty)<0)
						{
							conn.rollback();
							returnString= itmDBAccessEJB.getErrorString("","VTSRQTY1","","",conn);
							return returnString;
						}
						try
						{
							sql = "UPDATE SCONTRACTDET SET REL_QTY = (CASE WHEN REL_QTY IS NULL THEN 0 ELSE REL_QTY END + ? ),REL_DATE = ? "
								+ "WHERE CONTRACT_NO = '"+contractNo+"' AND LINE_NO = '"+line_No+"'";
							pstmt = conn.prepareStatement(sql);
							System.out.println("sql.........."+sql);
							pstmt.setDouble(1,relQty);
							pstmt.setTimestamp(2,orderDate);
							cnt = pstmt.executeUpdate();
							System.out.println("No of records updated in SCONTRACTDET for rel_qty is : "+cnt);
//							writeLog(filePtr," No of records updated in SCONTRACTDET for rel_qty is "+cnt,true);     //commented by manish mhatre on  07-nov-2019
							pstmt.clearParameters();
							pstmt.close();
							pstmt = null;
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
						}
						try
						{	//Pavan Rane 13sep19 start[to update the status of line item on full quantity released.]
							balQtyStduom = 0;
							sql = "SELECT QUANTITY__STDUOM - CASE WHEN REL_QTY IS NULL THEN 0 ELSE REL_QTY END FROM SCONTRACTDET "
									+ " WHERE CONTRACT_NO = ? AND LINE_NO = ? ";
								pstmt = conn.prepareStatement(sql);														
								pstmt.setString(1, contractNo);
								pstmt.setString(2, line_No);
								rs = pstmt.executeQuery();
								if(rs.next()) {
									balQtyStduom = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(balQtyStduom <= 0)
								{
									status = "C";
								}else {
									status = "";
								}
								System.out.println("balQtyStduom["+balQtyStduom+"]status["+status+"]");
								/*sql = "UPDATE SCONTRACTDET SET BAL_QTY_STDUOM = QUANTITY__STDUOM - CASE WHEN REL_QTY IS NULL THEN 0 ELSE REL_QTY END "
									+ "WHERE CONTRACT_NO = '"+contractNo+"' AND LINE_NO = '"+line_No+"'";
								stmt = conn.createStatement();
								cnt = stmt.executeUpdate(sql);
								stmt.close();
								stmt = null;*/
								sql = "UPDATE SCONTRACTDET SET BAL_QTY_STDUOM = QUANTITY__STDUOM - CASE WHEN REL_QTY IS NULL THEN 0 ELSE REL_QTY END, STATUS = ?,  STATUS_DATE = ? "
										+ "WHERE CONTRACT_NO = ? AND LINE_NO = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, status);
								pstmt.setTimestamp(2, orderDate);
								pstmt.setString(3, contractNo);
								pstmt.setString(4, line_No);
								cnt = pstmt.executeUpdate();
								pstmt.close();
								pstmt = null;
								System.out.println("No of records updated in SCONTRACTDET for bal_qty_stduom is : "+cnt);
								//Pavan Rane 13sep19 end[to update the status of line item on full quantity released.]
						}
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
							throw new ITMException(ex);
						}
						try
						{
							if (itemCode == null || itemCode.trim().length() == 0)
							{
								unitStd = unit;
							}			
							ArrayList convValue = new ArrayList();
							convValue = itmDBAccessEJB.getConvQuantityFact(unit,unitStd,itemCode,relQty,relQty,conn);
							System.out.println("conv__qty_stduom.........."+convValue.get(0));
							System.out.println("quantity__stduom.........."+convValue.get(1));
							sconvQtyStd = (String)convValue.get(0);
							sqtyStuom = (String)convValue.get(1);
							convQtyStd = Double.parseDouble(sconvQtyStd);
							qtyStuom = Double.parseDouble(sqtyStuom);
							
							ArrayList rateValue = new ArrayList();
							rateValue = itmDBAccessEJB.getConvQuantityFact(unitRate,unitStd,itemCode,rate,convRtuomStd,conn);	
							System.out.println("conv__rtuom_stduom.........."+rateValue.get(0));
							System.out.println("rate__stduom................"+rateValue.get(1));
							sconvRtuomStd = (String)rateValue.get(0);
							srateStduom = (String)rateValue.get(1);
							convRtuomStd = Double.parseDouble(sconvRtuomStd);
							rateStduom = Double.parseDouble(srateStduom);
							//21-09-2006 wasim start
							sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								itemDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							stmt.close();
							stmt = null;
							//21-09-2006 wasim end
							
							//Inserting values in SORDDET
							//Modified by Anjali R. on [20/11/2018][Added nature column and set value for nature is 'C'][Start]
							/*sql = "INSERT INTO SORDDET(SALE_ORDER,LINE_NO,SITE_CODE,QUANTITY,UNIT,RATE,DISCOUNT,UNIT__RATE,CONV__QTY_STDUOM,"+
									  "CONV__RTUOM_STDUOM,UNIT__STD,QUANTITY__STDUOM,RATE__STDUOM,ITEM_CODE,ITEM_SER,ITEM_FLG,"+
									  "CHG_DATE,CHG_USER,CHG_TERM,ITEM_CODE__ORD,TAX_CLASS,TAX_CHAP,TAX_ENV,TAX_AMT,NO_ART,RATE__CLG,NET_AMT,CONTRACT_NO, DSP_DATE, LINE_NO__CONTR,ITEM_DESCR) "+
									  " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";*/
								//commented by manish mhatre on 07-nov-2019
								//start manish
						/*	sql = "INSERT INTO SORDDET(SALE_ORDER,LINE_NO,SITE_CODE,QUANTITY,UNIT,RATE,DISCOUNT,UNIT__RATE,CONV__QTY_STDUOM,"+
								  "CONV__RTUOM_STDUOM,UNIT__STD,QUANTITY__STDUOM,RATE__STDUOM,ITEM_CODE,ITEM_SER,ITEM_FLG,"+
								  "CHG_DATE,CHG_USER,CHG_TERM,ITEM_CODE__ORD,TAX_CLASS,TAX_CHAP,TAX_ENV,TAX_AMT,NO_ART,RATE__CLG,NET_AMT,CONTRACT_NO, DSP_DATE, LINE_NO__CONTR,ITEM_DESCR,NATURE) "+
								  " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";*/
							//end manish
							//Modified by Anjali R. on [20/11/2018][Added nature column and set value for nature is 'C'][End]
								  
							// 09-08-2006 manoharan
							String sqlDet = "SELECT DSP_DATE, SITE_CODE FROM SCONTRACTDET  "
									+ "WHERE CONTRACT_NO = '"+contractNo+"' AND LINE_NO = '"+line_No+"'";
						
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sqlDet);
							if(rs.next())
							{
								siteCode = rs.getString("SITE_CODE");
								dspDate = rs.getTimestamp("DSP_DATE");
							}
							rs.close();
							rs = null;
							stmt.close();
							stmt = null;
							// end 09-08-2006 manoharan
							//commented by manish mhatre on 07-nov-2019
							//start manish
						/*	pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							pstmt.setString(2,lineNo); Integer.parseInt(lineNo.trim());
							pstmt.setString(3,siteCode);
							pstmt.setDouble(4,relQty);
							pstmt.setString(5,unit);
							pstmt.setDouble(6,rate);
							pstmt.setDouble(7,discount);
							pstmt.setString(8,unitRate);
							pstmt.setDouble(9,convQtyStd);
							pstmt.setDouble(10,convRtuomStd);
							pstmt.setString(11,unitStd);
							pstmt.setDouble(12,qtyStuom);
							pstmt.setDouble(13,rateStduom);
							pstmt.setString(14,itemCode);
							pstmt.setString(15,itemSer);
							pstmt.setString(16,itemFlag);
							pstmt.setTimestamp(17,orderDate);
							pstmt.setString(18,chgUser);
							pstmt.setString(19,chgTerm);
							pstmt.setString(20,itemCode);
							if(taxClass.equals("null") || taxClass == null || taxClass.trim().length() == 0)
							{
								pstmt.setNull(21,java.sql.Types.VARCHAR);
								System.out.println("Tax class is null....."+taxClass);
							}
							else
							{
								pstmt.setString(21,taxClass);
								System.out.println("Tax class is ....."+taxClass);
							}
							if(taxChap.equals("null") || taxChap == null || taxChap.trim().length() == 0)
							{
								pstmt.setNull(22,java.sql.Types.VARCHAR);
							}
							else
							{
								pstmt.setString(22,taxChap);
								System.out.println("Tax chapter is ....."+taxChap);
							}
							if(taxEnv.equals("null") || taxEnv == null || taxEnv.trim().length() == 0)
							{
								pstmt.setNull(23,java.sql.Types.VARCHAR);
							}
							else
							{
								pstmt.setString(23,taxEnv);
								System.out.println("Tax Env is ....."+taxChap);
							}
							taxAmount = "0.0";
							taxAmt = 0.0;
							writeLog(filePtr,"Tax Calculation parameter::: ",true);
							writeLog(filePtr,"saleOrder::: "+saleOrder,true);
							writeLog(filePtr,"row::: "+row,true);
							writeLog(filePtr,"tranDate::: "+tranDate.toString(),true);
							writeLog(filePtr,"relQty::: "+relQty,true);
							writeLog(filePtr,"currCode::: "+currCode,true);
							writeLog(filePtr,"siteCode::: "+siteCode,true);
							writeLog(filePtr,"taxClass::: "+taxClass,true);
							writeLog(filePtr,"taxChap::: "+taxChap,true);
							writeLog(filePtr,"taxEnv::: "+taxEnv,true);
							writeLog(filePtr,"rateClg::: "+rateClg,true);
							writeLog(filePtr,"discount::: "+discount,true);
							


							
							
							//Changes and Commented By Ajay on 29-12-2017:START
							
							//handleTax(saleOrder,row,tranDate.toString(),relQty,rate,currCode,siteCode,row,taxClass,taxChap,taxEnv,rateClg,discount,contractNo,line_No,conn);
														pstmt.setDouble(12,qtyStuom);
							pstmt.setDouble(13,rateStduom);
						
							//handleTax(saleOrder,row,tranDate.toString(),relQty,rate,currCode,siteCode,row,taxClass,taxChap,taxEnv,rateClg,discount,contractNo,line_No,conn,qtyStuom,rateStduom);
							//taxAmt = Double.parseDouble(taxAmount);							
							//writeLog(filePtr,"Calculated taxAmt::: "+taxAmt,true);
							writeLog(filePtr,"Calculated taxAmt::: "+0.0,true);
							//pstmt.setDouble(24,taxAmt);
							pstmt.setDouble(24,0.0);
							pstmt.setDouble(25,noArt);
							pstmt.setDouble(26,rateClg);
							double netAmt = 0.0;
							totAmt = (qtyStuom * rateStduom);
							//totAmt = ((totAmt - ((totAmt * discount) / 100)) + taxAmt);
							totAmt = ((totAmt - ((totAmt * discount) / 100)) + 0.0);
							netAmt = totAmt;
							pstmt.setDouble(27,netAmt);
							pstmt.setString(28,contractNo);
							totAmtHdr = totAmtHdr + netAmt;
							//taxAmtHdr = taxAmtHdr + taxAmt;
							
							
							taxAmtHdr = taxAmtHdr + 0.0;
							//Changes and Commented By Ajay on 29-12-2017:END
							// 09-08-2006 manoharan
							pstmt.setTimestamp(29,dspDate);
							// end 09-08-2006
							// 19-09-2006 manoharan
							pstmt.setString(30,line_No);
							// end 19-09-2006
							pstmt.setString(31,itemDescr);//21-09-2006 wasim
							
							//Modified by Anjali R. on [20-11-2018][Set nature in sales order detail as 'C'][Start]
							pstmt.setString(32,"C");
							//Modified by Anjali R. on [20-11-2018][Set nature in sales order detail as 'C'][End]
							
							cnt = pstmt.executeUpdate();*/  //end manish
							//added by manish mhatre on 07-nov-2019
							//start manish
                            //xmlBuff.append("<Detail2 dbID=\"\" domID=\""+Integer.parseInt(lineNo.trim())+"\" objContext=\"2\" objName=\"sorder_insert\">");		
                            xmlBuff.append("<Detail2 dbID=\"\" domID=\""+Integer.parseInt(lineNo.trim())+"\" objContext=\"2\" objName=\"sorder\">");		
							xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
							xmlBuff.append("<sale_order/>");
							xmlBuff.append("<line_no><![CDATA[" +lineNo +"]]></line_no>");
							xmlBuff.append("<site_code><![CDATA[" + siteCode +"]]></site_code>");
							xmlBuff.append("<quantity><![CDATA[" + relQty +"]]></quantity>");
							xmlBuff.append("<unit><![CDATA[" + unit +"]]></unit>");
							xmlBuff.append("<rate><![CDATA[" + rate +"]]></rate>");
							xmlBuff.append("<discount><![CDATA[" + discount +"]]></discount>");
							xmlBuff.append("<unit__rate><![CDATA[" + unitRate +"]]></unit__rate>");
							xmlBuff.append("<conv__qty_stduom><![CDATA[" + convQtyStd +"]]></conv__qty_stduom>");
							xmlBuff.append("<conv__rtuom_stduom><![CDATA[" + convRtuomStd +"]]></conv__rtuom_stduom>");
							xmlBuff.append("<unit__std><![CDATA[" + unitStd +"]]></unit__std>");
							xmlBuff.append("<quantity__stduom><![CDATA[" + qtyStuom +"]]></quantity__stduom>");
							xmlBuff.append("<rate__stduom><![CDATA[" + rateStduom +"]]></rate__stduom>");
							xmlBuff.append("<item_code><![CDATA[" + itemCode +"]]></item_code>");
							xmlBuff.append("<item_ser><![CDATA[" + itemSer +"]]></item_ser>");
							xmlBuff.append("<item_flg><![CDATA[" + itemFlag +"]]></item_flg>");
							xmlBuff.append("<chg_date><![CDATA[" + sdf1.format(orderDate).toString() + "]]></chg_date>");
					   		xmlBuff.append("<chg_user><![CDATA[" + chgUser +"]]></chg_user>");
							xmlBuff.append("<chg_term><![CDATA[" + chgTerm +"]]></chg_term>");
							xmlBuff.append("<item_code__ord><![CDATA[" + itemCode +"]]></item_code__ord>");
							if(taxClass.equals("null") || taxClass == null || taxClass.trim().length() == 0)
							{
								xmlBuff.append("<tax_class/>");
							}
							else
							{
								xmlBuff.append("<tax_class><![CDATA[" + taxClass +"]]></tax_class>");
							}
							if(taxChap.equals("null") || taxChap == null || taxChap.trim().length() == 0)
							{
								xmlBuff.append("<tax_chap/>");
							}
							else
							{
								xmlBuff.append("<tax_chap><![CDATA[" + taxChap +"]]></tax_chap>");
							}
							if(taxEnv.equals("null") || taxEnv == null || taxEnv.trim().length() == 0)
							{
								xmlBuff.append("<tax_env/>");
							}
							else
							{
								xmlBuff.append("<tax_env><![CDATA[" + taxEnv +"]]></tax_env>");
							}
							
							taxAmount = "0.0";
							taxAmt = 0.0;
							xmlBuff.append("<tax_amt><![CDATA[" + 0.0 +"]]></tax_amt>");
							xmlBuff.append("<no_art><![CDATA[" + noArt +"]]></no_art>");
							xmlBuff.append("<rate__clg><![CDATA[" + rateClg +"]]></rate__clg>");
							double netAmt = 0.0;
							totAmt = (qtyStuom * rateStduom);
							//totAmt = ((totAmt - ((totAmt * discount) / 100)) + taxAmt);
							totAmt = ((totAmt - ((totAmt * discount) / 100)) + 0.0);
							netAmt = totAmt;
							xmlBuff.append("<net_amt><![CDATA[" + netAmt +"]]></net_amt>");
							xmlBuff.append("<contract_no><![CDATA[" + contractNo +"]]></contract_no>");
							totAmtHdr = totAmtHdr + netAmt;				
							taxAmtHdr = taxAmtHdr + 0.0;
							xmlBuff.append("<desp_date><![CDATA[" + sdf.format(dspDate) + "]]></desp_date>");
							xmlBuff.append("<line_no__contr><![CDATA[" + line_No +"]]></line_no__contr>");
							xmlBuff.append("<item_descr><![CDATA[" + itemDescr +"]]></item_descr>");
							xmlBuff.append("<nature><![CDATA[").append("C").append("]]></nature>");
							xmlBuff.append("</Detail2>");
							//end manish
//							writeLog(filePtr,"No of records inserted in SORDDET is : "+cnt,true);     //commented by manish mhatre on  07-nov-2019
							System.out.println("No of records inserted in SORDDET is : "+cnt);
							/*pstmt.clearParameters();
							pstmt.close();
							pstmt = null;*/
						}//try
						catch (SQLException ex)
						{
							System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
							ex.printStackTrace();
//							writeLog(filePtr,ex,true);     //commented by manish mhatre on  07-nov-2019
						
							conn.rollback();
							returnString= itmDBAccessEJB.getErrorString("","VTSOGENF1","","",conn);
							return returnString;
						}
						catch (Exception e)
						{
							System.out.println("Exception ::" + e.getMessage() + ":");
							e.printStackTrace();
//							writeLog(filePtr,e,true);       //commented by manish mhatre on  07-nov-2019
							
							conn.rollback();
							returnString= itmDBAccessEJB.getErrorString("","VTSOGENF1","","",conn);
							return returnString;
						}
						row = row+1;
						lineNo = Integer.toString(row);
						lineNo = (("    "+lineNo).substring(("    "+lineNo).length()-3));
					}
				}//outer for
				//added by manish mhatre on 07-nov-2019
				//start manish
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("...............just before savdata()"+xmlString);
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				System.out.println("xml string >>>>>>"+xmlString);
				retString = saveData(userId, siteCode, xmlString, conn);
				System.out.println("retString>>>>>>>>>>>:" + retString);
				if (retString.indexOf("Success") > -1)
				{								
					//end manish
					//Pavan Rane 13sep19 start[to update the status of line item on full quantity released.]
					System.out.println("Pavan####:@:SCR:: contractNo["+contractNo+"]line_No["+line_No+"]");				
					try
					{
						sql = "select count(1) from SCONTRACTDET WHERE CONTRACT_NO = ? and (CASE WHEN STATUS IS NULL THEN 'N' ELSE STATUS END) <> 'C'";					
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, contractNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							detailCnt  = rs.getInt(1);
						}
						rs.close();
						rs = null;					
						pstmt.close();
						pstmt = null;					
						System.out.println("For update :: detailCnt["+detailCnt+"]");
						if(detailCnt == 0)					
						{
							sql = "UPDATE SCONTRACT SET STATUS = ?, STATUS_DATE=? WHERE CONTRACT_NO = ?";
							pstmt = conn.prepareStatement(sql);		
							pstmt.setString(1, "C");
							pstmt.setTimestamp(2, orderDate);
							pstmt.setString(3, contractNo);						
							cnt = pstmt.executeUpdate();
							System.out.println("No of records updated in SCONTRACTDET for headrer status is : "+cnt);
							pstmt.clearParameters();
							pstmt.close();
							pstmt = null;						
						}
						conn.commit();// added by nandkumar gadkari on 17/12/19
						
					}catch (SQLException ex)
					{
						conn.rollback();
						System.out.println("SaleContractRelease::SQLException ::" +sql+ ex.getMessage() + ":");
						ex.printStackTrace();
						throw new ITMException(ex);
					}				
					//Pavan Rane 13sep19 end[to update the status of line item on full quantity released.]
				}else {
					conn.rollback();
					//System.out.println("Inside else");
					//retString = itmDBAccessEJB.getErrorString("","VTPROUNSU2","","",conn);    //added by manish mhatre on 11-nov-2019
					//System.out.println("retstring>>>>>>"+retString);
					return retString;
				}
			}
			catch (Exception e)
			{
				System.out.println("Exception ::" + e.getMessage() + ":");
				e.printStackTrace();
//				writeLog(filePtr,e,true);       //commented by manish mhatre on  07-nov-2019
			
				conn.rollback();
				returnString= itmDBAccessEJB.getErrorString("","VTSOGENF1","","",conn);
				return returnString;
			}
			//pstmt.close();
			//pstmt = null;
			ordAmtHdr = totAmtHdr - taxAmtHdr;
			System.out.println("Order Amount ::::::::::: "+ordAmtHdr);
			if (bDetailFound)
			{	
				//updating SORDER with TOT_AMT,ORD_AMT AND TAX_AMT
				try
				{
					sql = "UPDATE SORDER SET ORD_AMT = ?, TAX_AMT = ?, TOT_AMT = ? WHERE SALE_ORDER = '"+saleOrder+"'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1,ordAmtHdr);
					pstmt.setDouble(2,taxAmtHdr);
					pstmt.setDouble(3,totAmtHdr);
					cnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					System.out.println("No of records updated in SORDER is : "+cnt);
				}
				catch (SQLException ex)
				{
					System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
					ex.printStackTrace();
//					writeLog(filePtr,ex,true);      //commented by manish mhatre on  07-nov-2019
				
					throw new ITMException(ex);
					
				}
				catch (Exception e)
				{
					System.out.println("Exception ::" + e.getMessage() + ":");
					e.printStackTrace();
//					writeLog(filePtr,e,true);        //commented by manish mhatre on  07-nov-2019
					throw new ITMException(e);
				}
				conn.commit();
				System.out.println("Transaction commited...............");
			}
			else
			{
				conn.rollback();
				System.out.println("Transaction rollbacked...............");
				returnString= itmDBAccessEJB.getErrorString("","VTSRQTY2","","",conn);
				return returnString;
			}
	
		}//outer try
		catch (Exception e)
		{
			returnString = "ERROR";
			System.out.println("Exception ::process():" + e.getMessage() + ":");
			e.printStackTrace();
//			writeLog(filePtr,e,true);        //commented by manish mhatre on  07-nov-2019
			throw new ITMException(e);
		}
		finally
		{
			System.out.println("Closing Connection2....");
			try
			{
				
				conn.close();
				conn = null;
			}
			catch(Exception se){}
		}//
		if(returnString.equals(""))
		{
			boolean flag = false;
			for(int i = 0;i < relArray.size(); i++)
			{
				if(Double.parseDouble((String)relArray.get(i)) > 0)
				{
					flag = true;
				}
			}
			if(flag)
			{
				returnString= itmDBAccessEJB.getErrorString("","VTSOGEN","","",conn);
				return returnString;
			}
		}
//		writeLog(filePtr,"Return String from process "+returnString,true);      //commented by manish mhatre on  07-nov-2019
		return returnString;
	}//process()
	//private String generateTranTd(String windowName,String orderDate,String siteCode)throws Exception
	//ADDED ORDER_TYPE BY RAJENDRA ON 21/12/07 
	private String generateTranTd(String windowName,String orderDate,String siteCode,String orderType, Connection conn )throws Exception
	
	{
		//ConnDriver connDriver = new ConnDriver();
		//Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String tranId = "";
		String newKeystring = "";
		try
	 	{
			//conn = connDriver.getConnectDB("DriverITM");
			sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = '"+windowName+"'";
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
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer1 :"+tranSer1);
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
			//ADDED ORDER_type BY RAJENDRA ON 21/12/07  
			xmlValues = xmlValues + "<order_type>" + orderType + "</order_type>";
			xmlValues = xmlValues + "<order_date>"+ orderDate + "</order_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			
			System.out.println("tranId :"+tranId);
		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
//			writeLog(filePtr,ex,true);      //commented by manish mhatre on  07-nov-2019
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
//			writeLog(filePtr,e,true);      //commented by manish mhatre on  07-nov-2019
			throw new ITMException(e);
		}
		/*finally
		{
			System.out.println("Closing Connection3...");
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception se){}
		}// */
		return tranId;
	}//generateTranTd()
	
	// Commented By Ajay on 29-12-2017:START 
/*	
	private void handleTax(String tranId,int lineNo,String tranDate,double quantity,double rate, String currCode,String siteCode,int ctr,String taxClass,String taxChap,String taxEnv,double rateClg,double discount,String contractNo, String lineNoContr, Connection conn)throws Exception
	{
		try
		{
			handleTax(tranId,lineNo, tranDate, quantity, rate, currCode, siteCode, ctr, taxClass, taxChap, taxEnv, rateClg, discount, contractNo,  lineNoContr,  conn,  quantity,  rate);
		}
		catch(Exception e)
		{
			writeLog(filePtr,e,true);
			throw new ITMException(e);
		}
	}
*/	/*private void handleTax(String tranId,int lineNo,String tranDate,double quantity,double rate, String currCode,String siteCode,int ctr,String taxClass,String taxChap,String taxEnv,double rateClg,double discount,String contractNo, String lineNoContr, Connection conn, double qtyStdUom, double rateStdUom)throws Exception
	{
		StringBuffer valueXmlString =null;
		try
		{
			//xml String in the foll. format
			String tranRateCol = null;
			String tranQtyCol = null;
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>");
			valueXmlString.append("<Root>");
			valueXmlString.append("<Detail2 dbID='' domID='"+ctr+"' objName='sorder' objContext='2'>");
			valueXmlString.append("<attribute pkNames='' status='N' updateFlag='A' selected='N' />");
			valueXmlString.append("<tran_id>").append(tranId).append("</tran_id>");
			valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
			valueXmlString.append("<tax_date>").append(tranDate).append("</tax_date>");
			valueXmlString.append("<rate>").append(rate).append("</rate>");
			valueXmlString.append("<rate__clg>").append(rateClg).append("</rate__clg>");
			valueXmlString.append("<tax_class>").append(taxClass).append("</tax_class>");
			valueXmlString.append("<tax_chap>").append(taxChap).append("</tax_chap>");
			valueXmlString.append("<tax_env>").append(taxEnv).append("</tax_env>");
			valueXmlString.append("<tax_amt>").append("0").append("</tax_amt>");
			valueXmlString.append("<discount>").append(discount).append("</discount>");
			valueXmlString.append("<quantity>").append(quantity).append("</quantity>");	
			// 04/10/11 manoharan
			valueXmlString.append("<quantity__stduom>").append(qtyStdUom).append("</quantity__stduom>");	
			valueXmlString.append("<rate__stduom>").append(rateStdUom).append("</rate__stduom>");	
			// end 04/10/11 manoharan
			valueXmlString.append("<contract_no>").append(contractNo).append("</contract_no>");	
			valueXmlString.append("<line_no__contr>").append(lineNoContr).append("</line_no__contr>");	
			valueXmlString.append("<Taxes/>");
			valueXmlString.append("</Detail2>");
			valueXmlString.append("</Root>");
			
			Document itemDoc = genericUtility.parseString(valueXmlString.toString());
			Node currRecordNode = itemDoc.getElementsByTagName("Detail2").item(0);
			// 19/05/09 manoharan old signature call commented and new signature called
			//TaxCalculation taxCal = new TaxCalculation();
			TaxCalculation taxCal = new TaxCalculation("sorder");
			// end 19/05/09 manoharan
			writeLog(filePtr,"CurrNode:::(Before (appendOrReplaceTaxesNode)"+serializeDom(currRecordNode),true);
			 
			appendOrReplaceTaxesNode(currRecordNode);
			writeLog(filePtr,"CurrNode:::(After (appendOrReplaceTaxesNode)"+serializeDom(currRecordNode),true);
			
			NodeList currRecordChildList = currRecordNode.getChildNodes();
			int childListLength = currRecordChildList.getLength();
			Node currTaxNode = null;
			for (int i = 0; i < childListLength; i++)
			{
				if (currRecordChildList.item(i).getNodeName().equalsIgnoreCase("Taxes"))
				{
					currTaxNode = currRecordChildList.item(i);
				}
			}
			taxCal.setUpdatedTaxDom(currTaxNode);
			taxCal.setTaxDom(currTaxNode);
			writeLog(filePtr,"(currRecordNode)>>>"+serializeDom(currRecordNode),true);
			writeLog(filePtr,"(currTaxNode)>>>"+serializeDom(currTaxNode),true);
			//taxCal.setDataNode(currRecordNode);///old
			String domId = Integer.toString(ctr);
			taxCal.setDataNode(currRecordNode,"2",domId);
			writeLog(filePtr,"After:::(setDataNode)"+serializeDom(currRecordNode),true);
			//commented by rajendra on 21/12/07 
			
			writeLog(filePtr,"Calling tax calculation................",true);
			//String retStr = taxCal.taxCalc("S-ORD", tranId,tranDate,"rate__stduom", "quantity__stduom", currCode,siteCode);
			tranRateCol = "rate__stduom";
			tranQtyCol = "quantity__stduom";
			String retStr = taxCal.taxCalc("S-ORD", tranId,tranDate,"rate__stduom", "quantity__stduom", tranRateCol, tranQtyCol, currCode, siteCode,"2");//added form no. 2 ****Vishakha
			writeLog(filePtr,"Returned from taxcalc................[" +retStr + "]" ,true);
			//System.out.println("CurrentTaxNode ::"+serializeDom(currTaxNode));
			//System.out.println("CurrentRecordNode ::"+serializeDom(currRecordNode));
			
			taxAmount = genericUtility.getColumnValueFromNode("tax_amt",currRecordNode);
			//taxAmt = Double.parseDouble(taxAmount);
			currRecordChildList = currRecordNode.getChildNodes();
			for (int i = 0; i < childListLength; i++)
			{
				if (currRecordChildList.item(i).getNodeName().equalsIgnoreCase("Taxes"))
				{
					currTaxNode = currRecordChildList.item(i);
					//writeLog(filePtr,"Saving Data for ["+ i + "] "+ serializeDom(currTaxNode),true);					
					saveData(currTaxNode,conn);
				}
			}
		}
		catch(Exception e)
		{
			writeLog(filePtr,e,true);
			throw new ITMException(e);
		}
	}
	*/
	//Commented By Ajay on 29-12-2017:END
	private void appendOrReplaceTaxesNode(Node currRecordNode)throws ITMException 
	{
		boolean found = false;
		try
		{

			if(	this.blankTaxString == null)
			{
				MasterDataStatefulEJB masterDataStateful = new MasterDataStatefulEJB();
				blankTaxString = masterDataStateful.getBlankTaxDomForAdd("2");//previously it was blank now added "2" **vishakha
			}
			NodeList dataNodeChildList = currRecordNode.getChildNodes();
			int dataNodeChildListLen = dataNodeChildList.getLength();
			for (int i=0; i < dataNodeChildListLen; i++)
			{
				if (dataNodeChildList.item(i) != null)
				{
					if (dataNodeChildList.item(i).getNodeName().equalsIgnoreCase("Taxes") )
					{
						currRecordNode.replaceChild(currRecordNode.getOwnerDocument().importNode(genericUtility.parseString(blankTaxString).getFirstChild(), true),dataNodeChildList.item(i));
						found = true;
						break;
					}
				}
			}
			if (!found)
			{
				currRecordNode.appendChild(currRecordNode.getOwnerDocument().importNode(genericUtility.parseString(blankTaxString).getFirstChild(), true));
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :: removeTaxNodeInDetail :==>\n"+e);
//			writeLog(filePtr,e,true);       //commented by manish mhatre on  07-nov-2019
			throw new ITMException(e);
		}
	}
	private String serializeDom(Node dom)throws ITMException
	{
		String retString = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
			retString = out.toString();
			out.flush();
			out.close();
			out = null;	
		}
		catch (Exception e)
		{
			System.out.println("Exception :: serializeDom :"+e);
//			writeLog(filePtr,e,true);      //commented by manish mhatre on  07-nov-2019
			throw new ITMException(e);
		}
		return retString;
	}
/*	private void saveData (Node currNode,Connection conn) throws Exception
	{
		try
		{
			StringBuffer fieldNameBuff = new StringBuffer();
			StringBuffer fieldValueBuff = new StringBuffer();
			StringBuffer insertQueryBuff = new StringBuffer();
			Statement stmt =null;
			String insertSql =null;

			int q =0,noOfField = 0,nodeLength = 0;
			
			stmt = conn.createStatement();
					
			Node currChildNode = null;
			NodeList currNodeList = currNode.getChildNodes();
			nodeLength = currNodeList.getLength();
			for (int i = 0;i < nodeLength ; i++ )
			{
				Node taxNode = currNodeList.item(i);
				NodeList taxfield = taxNode.getChildNodes();
				noOfField = taxfield.getLength();
				for (int j = 0;j < noOfField ; j++ )
				{																							 
					String fieldName = 	taxfield.item(j).getNodeName();
					if (!fieldName.equalsIgnoreCase("attribute") && !fieldName.equalsIgnoreCase("tax_descr") && !fieldName.equalsIgnoreCase("cc_editopt"))
					{
						if (taxfield.item(j).getFirstChild() != null && taxfield.item(j).getFirstChild().getNodeValue() != null && taxfield.item(j).getFirstChild().getNodeValue().trim().length() > 0)
						{
							fieldNameBuff.append(fieldName).append(",");
							if (fieldName.equalsIgnoreCase("TAX_AMT__TCURR") || fieldName.equalsIgnoreCase("EXCH_RATE") || fieldName.equalsIgnoreCase("EXCH_RATE_TRAN") || fieldName.equalsIgnoreCase("ROUND_TO") || fieldName.equalsIgnoreCase("RECO_PERC") || fieldName.equalsIgnoreCase("RECO_AMOUNT") || fieldName.equalsIgnoreCase("TAXABLE_AMT") || fieldName.equalsIgnoreCase("TAX_PERC") || fieldName.equalsIgnoreCase("TAX_AMT"))
							{
								fieldValueBuff.append(taxfield.item(j).getFirstChild().getNodeValue().trim()).append(",");
							}
							else
							{
								
								fieldValueBuff.append("'").append(taxfield.item(j).getFirstChild().getNodeValue().trim()).append("'").append(",");
							}
						}																		
					}
				}
				fieldNameBuff.deleteCharAt((fieldNameBuff.length() - 1));
				fieldValueBuff.deleteCharAt((fieldValueBuff.length() - 1));
				String tempString = "INSERT INTO TAXTRAN (" +fieldNameBuff+") VALUES ("+fieldValueBuff+")";
				insertQueryBuff.append(tempString);
				fieldNameBuff.delete(0,fieldNameBuff.length());
				fieldValueBuff.delete(0,fieldValueBuff.length());
				System.out.println("insertQueryBuff ::"+insertQueryBuff.toString());
				stmt.addBatch(insertQueryBuff.toString());				
				insertQueryBuff.delete(0,insertQueryBuff.length());
			}
			stmt.executeBatch();			
		}
		catch(Exception e)
		{
			writeException(e);
			throw e;
		}
	}*/
	
	
	
	//Added by tarani sen meher on 06102006 
	private void saveData (Node currNode,Connection conn) throws Exception
	{
		try
		{
//			writeLog(filePtr,"UPDATE JAR...."+currNode,true);       //commented by manish mhatre on  07-nov-2019
//			writeLog(filePtr,"In:::(saveData)"+currNode,true);     //commented by manish mhatre on  07-nov-2019
			StringBuffer fieldNameBuff = new StringBuffer();
			StringBuffer fieldValueBuff = new StringBuffer();
			StringBuffer insertQueryBuff = new StringBuffer();
			PreparedStatement pstmt =null;
			String taxtranSql =null;
			int q =0,noOfField = 0,nodeLength = 0,prepCount=0,i=0;
			
	
		//******this block is used to collect field and corresponding type of taxtran
		String sql ="SELECT * FROM TAXTRAN";
		HashMap columnAndTypeMap = new HashMap();
		HashMap xmlFldTypeMap = new HashMap();
		ResultSet rs =null;
		 pstmt = conn.prepareStatement(sql);
	 	rs=pstmt.executeQuery();
	  	ResultSetMetaData rsmd = rs.getMetaData();
	 	int noOfColumn =rsmd.getColumnCount();
	 	//writeLog(filePtr,"T A B L E S T R U C T U R E(TAXTRAN)",true);
	 	
	 	for( i=1;i<=noOfColumn;i++)
	 	{
	 		
	 		 
	 		String colName = rsmd.getColumnName(i);
	 		String colType = rsmd.getColumnTypeName(i);
	 		//writeLog(filePtr,"COLUMN_NAME["+colName+"]   "+"COLUMN_TYPE["+colType+"]",true);
	 		columnAndTypeMap.put(colName.toUpperCase().trim(),colType.toUpperCase().trim());
	 		if(i==noOfColumn)
	 		{
		 		fieldNameBuff.append(rsmd.getColumnName(i));
		 		fieldValueBuff.append("?");
	 		}
	 		else
	 		{	 		
		 		fieldNameBuff.append(rsmd.getColumnName(i)).append(",");
		 		fieldValueBuff.append("?").append(",");
	 		}
	 		
	 	  	
	 	}
	 	taxtranSql = "INSERT INTO TAXTRAN (" +fieldNameBuff.toString()+") VALUES ("+fieldValueBuff.toString()+")";
	 	//writeLog(filePtr,"taxtranSql**************"+taxtranSql,true);
	 	rs.close();
	 	pstmt.close();
	 	 
	 	pstmt= conn.prepareStatement(taxtranSql);
	 	Node currChildNode = null;
		NodeList currNodeList = currNode.getChildNodes();
		nodeLength = currNodeList.getLength();
		for ( i = 0;i < nodeLength ; i++ )
		{ 
				prepCount =0;
				Node taxNode = currNodeList.item(i);
				NodeList taxfield = taxNode.getChildNodes();
				noOfField = taxfield.getLength();
				for (int j = 0;j < noOfField ; j++ )
				{																							 
					String fieldName = 	taxfield.item(j).getNodeName();
					if (taxfield.item(j).getFirstChild() != null && taxfield.item(j).getFirstChild().getNodeValue() != null && taxfield.item(j).getFirstChild().getNodeValue().trim().length() > 0)
					{
						String ColValue =taxfield.item(j).getFirstChild().getNodeValue().trim();
						if(!ColValue.equalsIgnoreCase("null"))
						xmlFldTypeMap.put(fieldName.toUpperCase().trim(),ColValue);
						//writeLog(filePtr,"FROM XML COLUMN_NAME["+fieldName+"] COLUMN_TYPE["+ColValue+"]",true);
		 			}																		
				} 
				String[] totColInQry = fieldNameBuff.toString().split(",");
				//writeLog(filePtr,"Total Column in taxtran:::["+totColInQry.length+"]" ,true);
				
				for(int j =0;j<totColInQry.length;j++)
				{
					String column = totColInQry[j];
					String dataType = columnAndTypeMap.get(column.toUpperCase()).toString();
					prepCount++;
					System.out.println("column:::["+column+"]");
					if("TAX_EDITABLE".equalsIgnoreCase(column))
					{
						column = "CC_EDITOPT";
						System.out.println("Replacing the field TAX_EDITABLE to CC_EDITOPT......");
					}
					
					if(xmlFldTypeMap.containsKey(column.toUpperCase())) 
					{
					
						//writeLog(filePtr,"Found in Dom["+column+"]" ,true);
						
						String dataValue = xmlFldTypeMap.get(column.toUpperCase()).toString();
						if((dataType.toUpperCase().equalsIgnoreCase("CHAR"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR2"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR")))
						{
							//writeLog(filePtr,"setting String value===>"+dataValue+"<===["+prepCount+"]" ,true);
							if (column.equalsIgnoreCase("line_no") )
							{
								String line_No = (("    "+dataValue).substring(("    "+dataValue).length()-3));
								pstmt.setString(prepCount,line_No);
							}
							else
							{
								pstmt.setString(prepCount,String.valueOf(dataValue));
							}
						}
						else if ((dataType.toUpperCase().equalsIgnoreCase("NUMBER")) || (dataType.toUpperCase().equalsIgnoreCase("DECIMAL")))
						{
							//writeLog(filePtr,"setting Number value===>"+dataValue+"<===["+prepCount+"]" ,true);
							pstmt.setDouble(prepCount, Double.valueOf(dataValue).doubleValue());
						}
						else if ((dataType.toUpperCase().equalsIgnoreCase("DATE"))||(dataType.toUpperCase().equalsIgnoreCase("TIMESTAMP")))
						{
							//writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.TIMESTAMP);
						}
										
					}
					else
					{
						//writeLog(filePtr,"Not Found in Dom{"+column+"}" ,true);
						if((dataType.toUpperCase().equalsIgnoreCase("CHAR"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR"))||(dataType.toUpperCase().equalsIgnoreCase("VARCHAR2")))
						{
							
							//writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.VARCHAR);
						}
						else if((dataType.toUpperCase().equalsIgnoreCase("NUMBER"))|| (dataType.toUpperCase().equalsIgnoreCase("DECIMAL")))
						{
							//writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.DOUBLE);
						}
						else if ((dataType.toUpperCase().equalsIgnoreCase("DATE"))||(dataType.toUpperCase().equalsIgnoreCase("TIMESTAMP")))
						{
							//writeLog(filePtr,"setting String NULL===>["+prepCount+"]" ,true);
							pstmt.setNull(prepCount, java.sql.Types.TIMESTAMP);
						} 
					} 
					
						
				}
				pstmt.addBatch();
		}
		pstmt.executeBatch();
		pstmt.close();
		pstmt = null;
	}
	catch(SQLException se)
	{
//		writeLog(filePtr,se,true);     //commented by manish mhatre on  07-nov-2019
		throw se;
	}
	catch(Exception e)
	{	
//		writeLog(filePtr,e,true);       //commented by manish mhatre on  07-nov-2019
		throw e;
	}
}
	//added by manish mhatre  on 07-nov-2019
	//start manish
	private String saveData(String userId,String siteCode, String xmlString, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String[] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("14-sep-2020 xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
		} catch (ITMException itme)
		{
			System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}
	//end manish
private static void writeLog(File f,String ex,boolean flag)
	{
		try
		{
			PrintWriter pw = new PrintWriter((new FileOutputStream(f,flag)),flag);
			pw.println(ex);
			pw.close();
		}
		catch(Exception exWm){exWm.printStackTrace();}
	}
	private static void writeLog(File f,Exception ex,boolean flag)
	{
		try
		{
			PrintWriter pw = new PrintWriter((new FileOutputStream(f,flag)),flag);
			ex.printStackTrace(pw);
			pw.close();
		}
		catch(Exception exWe){exWe.printStackTrace();}
	}
	
	private String checkNullandTrim(String input) {
		if (input == null) {
			input = "";
		}
		return input.trim();
	}
	
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}

/*	private void writeException(Exception e)
	{
		try
		{
			fw.write("Eception ::::::::::::::::::::::::"+e.getMessage() + "\r\n");
			PrintStream t = new PrintStream(new FileOutputStream(new File("C:\\pb10\\log\\trace.log"),true));
			e.printStackTrace(t);	
		}	
		catch(Exception t){}
		
	}*/
}//class




