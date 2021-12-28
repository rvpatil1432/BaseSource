package  ibase.webitm.ejb.dis;
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
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.Stateless; // added for ejb3


//public class DistordRcpDetPrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class DistordRcpDetPrc extends ProcessEJB implements DistordRcpDetPrcLocal, DistordRcpDetPrcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	ConnDriver connDriver = new ConnDriver();
	CommonConstants commonConstants = new CommonConstants();
	
	Connection conn = null;
	
	StringBuffer retBuf = null;
	String mfgDate=null;
	String siteCode=null;
	String errorString=null;
	String loginCode=null;
	String chgTerm=null;

	int quantity=0;
	int demand=0;
	int minQty=0;
	int maxQty=0;
	int batches=0;
	double amount=0.0;
	double demandAmt=0.0;

	File filePtr = new File("C:\\pb10\\log\\pbnitrace.log");

	static
	{
			File mkd = new File("C:\\pb10\\log");
			if(!mkd.exists())
			{
				mkd.mkdirs(); 
				System.out.println("Directory Built................ "+mkd);
			}			
	}

	/*public void ejbCreate() throws RemoteException, CreateException
	{		
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
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;		

		writeLog(filePtr,"DistordRcpDetPrc :getData() function called",true);
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
			writeLog(filePtr,e,true);
			writeLog(filePtr,"Exception :DistordRcpDetPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":",true);
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
		ResultSet rs1 = null;
		Statement pstmt = null;
		Statement st = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();		
		String receiptNo=null;
		String expDate=null;
	
		java.util.HashMap refMap = new java.util.HashMap();

		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);	
			//DatabaseMetaData d=conn.getMetaData();
			//writeLog(filePtr,"Connection URL =>"+d.getURL(),true);
			//writeLog(filePtr,"Connection User=>"+d.getUserName(),true);
		}
		catch (Exception e)
		{
			writeLog(filePtr,"Exception :DistordRcpDetPrc :ejbCreate :==>"+e,true);
			writeLog(filePtr,e,true);
			e.printStackTrace();
		} 
		
		try
		{
			receiptNo = genericUtility.getColumnValue("receipt_no",headerDom);
			/*if(receiptNo!=null && receiptNo.equals(""))
			{
				receiptNo = "";
				writeLog(filePtr,"Receipt No. is Null...",true);
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			}*/
			if ( receiptNo == null || receiptNo.trim().length() == 0 )
			{
				receiptNo = "";
				writeLog(filePtr,"Receipt No. is Null...",true);
				errString = itmDBAccessEJB.getErrorString("","VTMRPPARM","","",conn);
				return errString;
			} 
			receiptNo=blanknull(receiptNo).trim();
		
			if((commonConstants.DB_NAME).equalsIgnoreCase("ORACLE"))
			{
				 getDataSql=" SELECT A.TRAN_ID,A.LINE_NO,A.DIST_ORDER,A.LINE_NO_DIST_ORDER,"
	+" A.ITEM_CODE,A.QUANTITY,case when A.ACTUAL_QTY is null then A.QUANTITY else A.ACTUAL_QTY end,A.UNIT,A.TAX_CLASS,A.TAX_CHAP,A.TAX_ENV,A.LOC_CODE,"
	+" A.LOT_NO,A.LOT_SL,A.PACK_CODE,A.RATE,A.AMOUNT,A.TAX_AMT,A.NET_AMT,"
	+" A.SITE_CODE__MFG,A.MFG_DATE,A.EXP_DATE,A.POTENCY_PERC,A.NO_ART,"   
	+" A.TRANS_MODE,A.GROSS_WEIGHT,A.TARE_WEIGHT,A.NET_WEIGHT,A.PACK_INSTR,A.DIMENSION,"   
	+" A.SUPP_CODE__MFG,A.BATCH_NO,A.GRADE,RATE__CLG,A.COST_RATE,A.DISCOUNT,A.PHYSICAL_STATUS "
	+" FROM DISTORD_RCPDET A,DISTORD_RCP B "   
	+" WHERE A.TRAN_ID=B.TRAN_ID AND "
	+" case when B.CONFIRMED is null then 'N' else B.CONFIRMED end <>'Y' AND A.TRAN_ID='"+receiptNo+"'";
			}
			else if((commonConstants.DB_NAME).equalsIgnoreCase("DB2")||(commonConstants.DB_NAME).equalsIgnoreCase("MSSQL"))
			{
		 getDataSql=" SELECT A.TRAN_ID,A.LINE_NO,A.DIST_ORDER,A.LINE_NO_DIST_ORDER,"
	+" A.ITEM_CODE,A.QUANTITY,case when A.ACTUAL_QTY is null then A.QUANTITY else A.ACTUAL_QTY end,A.UNIT,A.TAX_CLASS,A.TAX_CHAP,A.TAX_ENV,A.LOC_CODE,"
	+" A.LOT_NO,A.LOT_SL,A.PACK_CODE,A.RATE,A.AMOUNT,A.TAX_AMT,A.NET_AMT,"
	+" A.SITE_CODE__MFG,A.MFG_DATE,A.EXP_DATE,A.POTENCY_PERC,A.NO_ART,"   
	+" A.TRANS_MODE,A.GROSS_WEIGHT,A.TARE_WEIGHT,A.NET_WEIGHT,A.PACK_INSTR,A.DIMENSION,"   
	+" A.SUPP_CODE__MFG,A.BATCH_NO,A.GRADE,RATE__CLG,A.COST_RATE,A.DISCOUNT,A.PHYSICAL_STATUS "
	+" FROM DISTORD_RCPDET A,DISTORD_RCP B "   
	+" WHERE A.TRAN_ID=B.TRAN_ID AND "
	+" case when B.CONFIRMED is null then 'N' else B.CONFIRMED end <>'Y' AND A.TRAN_ID='"+receiptNo+"'";
			}

			writeLog(filePtr,"Tran ID =>"+receiptNo,true);
			writeLog(filePtr,"Query =>"+getDataSql,true);
		
			pstmt=conn.createStatement();
			rs1 = pstmt.executeQuery(getDataSql);
	
			boolean isCheck=false;
			while(rs1.next())
			{
				isCheck=true;
					//tran_id
					retTabSepStrBuff.append(blanknull(rs1.getString(1))).append("\t");						
					//line_no
					retTabSepStrBuff.append(blanknull(rs1.getString(2))).append("\t");
					//dist_order
					retTabSepStrBuff.append(blanknull(rs1.getString(3))).append("\t");
					//LINE_NO_DIST_ORDER
					retTabSepStrBuff.append(blanknull(rs1.getString(4))).append("\t");
					//ITEM_CODE
					retTabSepStrBuff.append(blanknull(rs1.getString(5))).append("\t");
					//QUANTITY
					retTabSepStrBuff.append(rs1.getDouble(6)).append("\t");
					//ACTUAL_QTY
					retTabSepStrBuff.append(rs1.getDouble(7)).append("\t");
					//UNIT
					retTabSepStrBuff.append(blanknull(rs1.getString(8))).append("\t");
					//TAX_CLASS
					retTabSepStrBuff.append(blanknull(rs1.getString(9))).append("\t");
					//TAX_CHAP
					retTabSepStrBuff.append(blanknull(rs1.getString(10))).append("\t");
					//TAX_ENV
					retTabSepStrBuff.append(blanknull(rs1.getString(11))).append("\t");
					//LOC_CODE
					retTabSepStrBuff.append(blanknull(rs1.getString(12))).append("\t");
					//LOT_NO
					retTabSepStrBuff.append(blanknull(rs1.getString(13))).append("\t");
					//LOT_SL
					retTabSepStrBuff.append(blanknull(rs1.getString(14))).append("\t");
					//PACK_CODE
					retTabSepStrBuff.append(blanknull(rs1.getString(15))).append("\t");
					//RATE
					retTabSepStrBuff.append(rs1.getDouble(16)).append("\t");
					//AMOUNT
					retTabSepStrBuff.append(rs1.getDouble(17)).append("\t");
					//TAX_AMT
					retTabSepStrBuff.append(rs1.getDouble(18)).append("\t");
					//NET_AMT
					retTabSepStrBuff.append(rs1.getDouble(19)).append("\t");
					//SITE_CODE__MFG
					retTabSepStrBuff.append(blanknull(rs1.getString(20))).append("\t");
					//MFG_DATE
					mfgDate=this.genericUtility.getValidDateString(rs1.getTimestamp(21).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat());
					retTabSepStrBuff.append(mfgDate).append("\t");
					//EXP_DATE
					expDate=this.genericUtility.getValidDateString(rs1.getTimestamp(22).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat());
					retTabSepStrBuff.append(expDate).append("\t");
					//POTENCY_PERC
					retTabSepStrBuff.append(rs1.getDouble(23)).append("\t");
					//NO_ART
					retTabSepStrBuff.append(rs1.getDouble(24)).append("\t");
					//TRANS_MODE
					retTabSepStrBuff.append(blanknull(rs1.getString(25))).append("\t");
					//GROSS_WEIGHT
					retTabSepStrBuff.append(rs1.getDouble(26)).append("\t");
					//TARE_WEIGHT
					retTabSepStrBuff.append(rs1.getDouble(27)).append("\t");
					//NET_WEIGHT
					retTabSepStrBuff.append(rs1.getDouble(28)).append("\t");
					//PACK_INSTR     
					retTabSepStrBuff.append(blanknull(rs1.getString(29))).append("\t");
					//DIMENSION
					retTabSepStrBuff.append(blanknull(rs1.getString(30))).append("\t");
					//SUPP_CODE__MFG
					retTabSepStrBuff.append(blanknull(rs1.getString(31))).append("\t");
					//BATCH_NO
					retTabSepStrBuff.append(blanknull(rs1.getString(32))).append("\t");
					//GRADE
					retTabSepStrBuff.append(blanknull(rs1.getString(33))).append("\t");
					//RATE__CLG
					retTabSepStrBuff.append(rs1.getDouble(34)).append("\t");
					//COST_RATE
					retTabSepStrBuff.append(rs1.getDouble(35)).append("\t");
					//DISCOUNT
					retTabSepStrBuff.append(rs1.getDouble(36)).append("\t");
					// PHYSICAL_STATUS        
					retTabSepStrBuff.append(rs1.getDouble(37)).append("\t");					
					retTabSepStrBuff.append("\n");					
			}
			resultString = retTabSepStrBuff.toString();
			
			writeLog(filePtr,"ResultString....." + resultString,true);
			writeLog(filePtr,"ResultSet Checked "+isCheck,true);
			rs1.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			writeLog(filePtr,"SQLException :DistordRcpDetPrc :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":",true);
			writeLog(filePtr,e,true);
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			writeLog(filePtr,"SQLException :DistordRcpDetPrc :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":",true);
			writeLog(filePtr,e,true);
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
				writeLog(filePtr,errString,true);
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
		String retStr = "";		
		
		try
		{				
				writeLog(filePtr,"xmlString [process]::::::::::::::"+xmlString,true);
				writeLog(filePtr,"xmlString2[process]:::::::::::::"+xmlString2,true);
				writeLog(filePtr,"windowName[process]:::::::::::::"+windowName,true);
				writeLog(filePtr,"xtraParams[process]:::::::::::::"+xtraParams,true);						
		}
		catch(Exception e)
		{
			writeLog(filePtr,"[DistordRcpDetPrc]Exception ="+e.getMessage(),true);
			writeLog(filePtr,e,true);
			e.printStackTrace();
		}
	
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				writeLog(filePtr,"headerDom" + headerDom,true);				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				writeLog(filePtr,"detailDom" + detailDom,true);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			writeLog(filePtr,"Exception :DistordRcpDetPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":",true);
			e.printStackTrace();
			retStr = e.getMessage();
			writeLog(filePtr,e,true);
		}
		return retStr;
	}//END OF PROCESS (1)
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		int updCnt = 0;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		double actualQty=0.0;

		String errString=null;
		String tranId1=null;
		String childNodeName = "";
			
		String receiptNo=null;//for receipt number	
		int lineNo=0;//for line no.
			
		NodeList parentNodeList = null;
		NodeList childNodeList = null;

		Node parentNode = null;
		Node childNode = null; 	
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);				
		}
		catch (Exception e)
		{			
			writeLog(filePtr,"[DistordRcpDetPrc]Exception ="+e.getMessage(),true);
			writeLog(filePtr,e,true);
			e.printStackTrace();
		} 
	
		
		loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		loginCode=blanknull(loginCode).trim();
		chgTerm=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		chgTerm=blanknull(chgTerm).trim();
		
		receiptNo = genericUtility.getColumnValue("receipt_no",headerDom);	//from header	
		
		receiptNo=blanknull(receiptNo).trim();

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
					if (childNodeName.equals("tran_id"))
					{
						tranId1 = childNode.getFirstChild().getNodeValue();
					}	
					if (childNodeName.equals("line_no"))
					{
						if(childNode.getFirstChild().getNodeValue()!=null)
						{
							lineNo=Integer.parseInt(childNode.getFirstChild().getNodeValue());
						}
					}
					if (childNodeName.equals("actual_qty"))
					{
						if(childNode.getFirstChild().getNodeValue()!=null)
						{
							actualQty=Double.parseDouble(childNode.getFirstChild().getNodeValue());
						}
					}
				}//inner for loop	
				
				writeLog(filePtr,"[DistordRcpDetPrc]tranId====>"+tranId1,true);
				writeLog(filePtr,"[DistordRcpDetPrc]line_no===>"+lineNo,true);
				writeLog(filePtr,"[DistordRcpDetPrc]actualQty=>"+actualQty,true);

				errorString=updateDistOrdRcp(tranId1,lineNo,actualQty,conn);	
				if(errorString!=null)
				{
					try
					{
						conn.rollback();
						writeLog(filePtr,"Connection is roll back",true);
						break;
					}
					catch(Exception d)
					{
						writeLog(filePtr,"Exception : DistordRcpDetPrc =>"+d.toString(),true);
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
				writeLog(filePtr,"Exception : DistordRcpDetPrc =>"+d.toString(),true);
				d.printStackTrace();
			}
			e.printStackTrace();
			errorString = e.getMessage();			
			return errorString;
		}
		finally
		{
			writeLog(filePtr,"Closing Connection....",true);
			try
			{
				if(errorString==null)
				{
					writeLog(filePtr,"Connection Commited",true);
					conn.commit();
				}
				else if(errorString!=null)
				{
					writeLog(filePtr,"Connection is rollback",true);
					conn.rollback();
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
				writeLog(filePtr,e,true);
				errorString = e.getMessage();
				e.printStackTrace();
				return errorString ;
			}
		}
		writeLog(filePtr,"Error Message=>"+errorString,true);
		return errorString;
	}//END OF PROCESS(2)	

	private String updateDistOrdRcp(String tranId1,int lineNo,double actualQty,Connection conn)
	{
		ResultSet rs1 = null,rsSql1=null,rsSql2=null;
		PreparedStatement pstmt = null,pstmt1=null;
		String errorString1=null;
		
		int i=0;
		double quantity=0.0;

		//GenericUtility genericUtility = GenericUtility.getInstance();
		
		tranId1=blanknull(tranId1).trim();

		String sqlSelect=" SELECT A.TRAN_ID,A.LINE_NO,A.DIST_ORDER,A.LINE_NO_DIST_ORDER,"
	+" A.ITEM_CODE,A.QUANTITY,case when A.ACTUAL_QTY is null then A.QUANTITY else A.ACTUAL_QTY end,A.UNIT,A.TAX_CLASS,A.TAX_CHAP,A.TAX_ENV,A.LOC_CODE,"
	+" A.LOT_NO,A.LOT_SL,A.PACK_CODE,A.RATE,A.AMOUNT,A.TAX_AMT,A.NET_AMT,"
	+" A.SITE_CODE__MFG,A.MFG_DATE,A.EXP_DATE,A.POTENCY_PERC,A.NO_ART,"   
	+" A.TRANS_MODE,A.GROSS_WEIGHT,A.TARE_WEIGHT,A.NET_WEIGHT,A.PACK_INSTR,A.DIMENSION,"   
	+" A.SUPP_CODE__MFG,A.BATCH_NO,A.GRADE,RATE__CLG,A.COST_RATE,A.DISCOUNT,A.PHYSICAL_STATUS "
	+" FROM DISTORD_RCPDET A,DISTORD_RCP B "   
	+" WHERE A.TRAN_ID=B.TRAN_ID AND "
	+" case when B.CONFIRMED is null then 'N' else B.CONFIRMED end <>'Y' AND A.TRAN_ID=? AND A.LINE_NO=?";

		String sqlUpdate="UPDATE DISTORD_RCPDET SET ACTUAL_QTY=? WHERE TRAN_ID=? AND LINE_NO=?";

		writeLog(filePtr,"[DistordRcpDetPrc]updateDistOrdRcp() method called",true);
		
		try
		{
			pstmt = conn.prepareStatement(sqlSelect);
			pstmt.setString(1,tranId1);
			pstmt.setInt(2,lineNo);
			rs1 = pstmt.executeQuery();

			pstmt1=conn.prepareStatement(sqlUpdate);

			while(rs1.next())
			{
				quantity=rs1.getDouble("quantity");
				//Comented by Gautam on 05/01/07
				//if(actualQty > quantity)
				//{
				//	/errorString = itmDBAccessEJB.getErrorString("","VTMAXQTY","","",conn);
				//	break;
				//}				
				//else
				//{					
				pstmt1.setDouble(1,actualQty);
				pstmt1.setString(2,tranId1);
				pstmt1.setInt(3,lineNo);
				writeLog(filePtr,"[DistordRcpDetPrc]executed ",true);
				pstmt1.addBatch();
				//}
			}//end of while loop
			pstmt1.executeBatch();						
			pstmt1.close();
			rs1.close();
			pstmt.close();
		}
		catch(Exception e)
		{
			errorString=e.getMessage();
			writeLog(filePtr,"[DistordRcpDetPrc]ERROR OCCURED"+e,true);
			e.printStackTrace();			
		}
		writeLog(filePtr,"[DistordRcpDetPrc]Error Message="+errorString,true);
		return errorString;
	}//END OF Method

	public String blanknull(String s)
	{
		if(s==null)
			return "";
		else
			return s;
	}//end of blanknull()	

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
}//END OF EJB