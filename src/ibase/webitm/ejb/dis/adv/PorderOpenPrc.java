package ibase.webitm.ejb.dis.adv;

//Added By Dhiraj Chavan on 18-APR-2016
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.rmi.RemoteException;

import java.sql.*;



import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless; // added for ejb3

import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.POrderAmdConf;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Stateless
public class PorderOpenPrc extends ProcessEJB implements PorderOpenPrcLocal,PorderOpenPrcRemote{

	
	String errorString=null;
	String loginCode=null;
	String chgTerm=null;
	E12GenericUtility genericUtility=new E12GenericUtility();
	//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	CommonConstants commonConstants = new CommonConstants();

	
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	//getData Method
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("PorderOpenPrc :getData() function called");
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

			rtrStr = getdata(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :PorderOpenPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();
			throw new ITMException(e);
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
	public String getdata(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errString=null;
		String sql= "" ;
		String resultString = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		StringBuffer PorderOpenPrc = new StringBuffer();
		int TotStatusCnt=0,totLineNo=0;
		String purcOrder="",confirmed="",empCode="",loginEmpCode="",deptCode_po="",deptCode_userlogin="";
		String headerstatus="";
		SimpleDateFormat sdf = null;
		Connection conn = null;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
		}
		catch (Exception e)
		{
			System.out.println("Exception :PorderOpenPrc :ejbCreate :==>"+e);
			e.printStackTrace();
		}

		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			purcOrder = genericUtility.getColumnValue("purc_order",headerDom);
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("loginEmpCode is @@@@@::"+loginEmpCode);
			
			if ((purcOrder == null || purcOrder.trim().length() == 0) )
			{
				//If purchase order should not blank or null
				errString = itmDBAccessEJB.getErrorString("","VTPUREMP","","",conn);
				return errString;
			}
			else
			{
				
				sql =" select count(*) from porder where purc_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,purcOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					int cnt = rs.getInt(1);
					if(cnt == 0) 
					{
						//if purchase order not found in porder table the throw error message
						errString = itmDBAccessEJB.getErrorString("","VTINVPORD","","",conn);
						return errString;
					}									
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
		
			}
			//checking status of purchase order
			sql =" select confirmed,status from porder where purc_order = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,purcOrder);
			rs = pstmt.executeQuery();
			System.out.println("sql-->"+sql);
			if(rs.next())
			{
				confirmed = rs.getString(1);
				headerstatus=rs.getString(2);
											
			}
			if(confirmed.equals("N")||confirmed.equalsIgnoreCase("N"))
				{
				errString = itmDBAccessEJB.getErrorString("","VTPORNTCF","","",conn);
				return errString;
				}else
			if(headerstatus.equals("X")||headerstatus.equalsIgnoreCase("X"))
				{
				errString = itmDBAccessEJB.getErrorString("","VPCANC","","",conn);
				return errString;
				}else
			if(headerstatus.equals("C")||headerstatus.equalsIgnoreCase("C"))
				{
					errString = itmDBAccessEJB.getErrorString("","VPCLOSE","","",conn);
					return errString;
				}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = null;
			
			sql = " select count(line_no) from porddet where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) {

				totLineNo = rs.getInt(1);

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql=null;
			sql = " select count(status) from porddet where purc_order = ? and status='O'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				TotStatusCnt = rs.getInt(1);
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if ((totLineNo == TotStatusCnt)) {
				// VTDNFFP No data found for processing
				errString = itmDBAccessEJB.getErrorString("", "VTPODNFP", "",
						"", conn);
				return errString;
			}
			/* After enter Purchase Order No, system should validate dept code of PO Emp Code & logged user dept should be same.
			 *  If not, show a message as "You not authorized to view PO Details as your & Purchaser's Dept code is different."*/ 
			
			sql ="select porder.emp_code from porddet ,porder  where porddet.purc_order=porder.purc_order and porder.purc_order=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,purcOrder);
			rs = pstmt.executeQuery();
			System.out.println("sql-->"+sql);
			if(rs.next())
			{
				//confirmed = rs.getString(1);
				empCode=rs.getString(1);
				//deptCode=rs.getString(2);
												
			}
			
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql=null;
			sql ="select dept_code from employee where emp_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,empCode);
			rs = pstmt.executeQuery();
			System.out.println("sql-->"+sql);
			if(rs.next())
			{
				deptCode_po=rs.getString(1);
												
			}
			
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql=null;
			sql ="select dept_code from employee where emp_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,loginEmpCode);
			rs = pstmt.executeQuery();
			System.out.println("sql-->"+sql);
			if(rs.next())
			{	
				deptCode_userlogin=rs.getString(1);
												
			}
			if(!(deptCode_po).equals(deptCode_userlogin))
			{
				System.out.println("you are not autherized!!!!!!!!");
				errString = itmDBAccessEJB.getErrorString("","VPNTEVAL","","",conn);
				return errString;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql=null;
				
			sql="SELECT PORDDET.purc_order,PORDER.ord_date,porder.pord_type,PORDDET.line_no,PORDDET.status," +
					"PORDDET.quantity,item.descr,porddet.dlv_qty,porddet.rate," +
					"PORDDET.tot_amt,porddet.proj_code,PORDDET.site_code,PORDDET.ind_no,PORDDET.item_code," +
					"porder.supp_code,supplier.supp_name" +
					" FROM PORDDET,PORDER,item,supplier " +
					"WHERE porder.purc_order=porddet.purc_order and porddet.item_code=item.item_code and " +
					"porder.supp_code=supplier.supp_code " +
					"and (porddet.dlv_qty<porddet.quantity) AND porddet.STATUS='C' and porddet.purc_order=? " +
					"order by porddet.line_no";
			System.out.println("sql..."+ sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,purcOrder );
			
			rs = pstmt.executeQuery();

			while(rs.next())
			{
				System.out.println("Inside while...");
				// tranid_purc_order
				PorderOpenPrc.append(
						rs.getString(1) == null ? "" : rs.getString(1)).append(
						"\t");
				PorderOpenPrc.append(rs.getDate(2)).append("\t");
				
				PorderOpenPrc.append(
						rs.getString(3) == null ? "" : rs.getString(3)).append(
						"\t");

				

				PorderOpenPrc.append(
						rs.getString(4) == null ? "" : rs.getString(4)).append(
						"\t");
				
				PorderOpenPrc.append(
						rs.getString(5) == null ? "" : rs.getString(5)).append(
						"\t");
				
				PorderOpenPrc.append(rs.getDouble(6)).append("\t");

				PorderOpenPrc.append(
						rs.getString(7) == null ? "" : rs.getString(7)).append(
						"\t");

				

				PorderOpenPrc.append(rs.getDouble(8)).append("\t");

				PorderOpenPrc.append(rs.getDouble(9)).append("\t");

				PorderOpenPrc.append(rs.getDouble(10)).append("\t");

				PorderOpenPrc.append(
						rs.getString(11) == null ? "" : rs.getString(11))
						.append("\t");

				PorderOpenPrc.append(
						rs.getString(12) == null ? "" : rs.getString(12))
						.append("\t");

				PorderOpenPrc.append(
						rs.getString(13) == null ? "" : rs.getString(13))
						.append("\t");

				PorderOpenPrc.append(
						rs.getString(14) == null ? "" : rs.getString(14))
						.append("\t");

				PorderOpenPrc.append(
						rs.getString(15) == null ? "" : rs.getString(15))
						.append("\t");
				PorderOpenPrc.append(
						rs.getString(16) == null ? "" : rs.getString(16))
						.append("\t");

				PorderOpenPrc.append("\n");
			}
		
			resultString = PorderOpenPrc.toString();
			System.out.println("ResultString....." + resultString);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
		}
		catch (SQLException e)
		{
			System.out.println("SQLException :PorderOpenPrc:getdata(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :PorderOpenPrc :getdata(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
		//		retTabSepStrBuff = null;
				if(conn != null && !conn.isClosed())
				{
					if(rs != null)
					{
						rs.close();
						rs=null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					conn.rollback();
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
	}
	
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		E12GenericUtility genericUtility= new E12GenericUtility();
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
			System.out.println("Exception :PorderOpenPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
			throw new ITMException(e);
		}
		return retStr;
	}//END OF PROCESS (1)
	
	
	
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		
		
		//Connection conn = null;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		int selectedRow;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;

		Node parentNode = null;
		Node childNode = null;
		
		String errString="";
		
		String childNodeName = "";
		
		//For Header
		
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1=null;
		String sql="",sql1="";
		StringBuffer xmlBuff = new StringBuffer();
		SimpleDateFormat sdf=null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String  fname = "", mname = "", lname = "",  crTerm = "",empCode = "",commPercOn = "",specialInstr = "";
		String  pordType = "",  siteCodeDlv = "", siteCodeOrd = "", siteCodeBill = "", status = "";
		String deptCode = "", orderDb = "", itemSer = "", taxOpt = "";
		String currCode = "", taxChap = "", taxClass = "", taxEnv = "", remarks = "", projCode = "", salesPers = "", common = "";
		String quotNo = "", confirmed = "", tranCode = "", currCodeFrt = "", frtTerm = "", dlvTerm = "", currCodeIns = "",  empCodeAprv = "";
		String benefitType="",unit = "",locCode = "",indNo = "",suppCode = "", siteCode = "",supp_code_mnfr = "", itemCode = "";

		double ordAmt = 0, taxAmt = 0, totAmt = 0, commPerc = 0,  discount = 0, frtAmt = 0, insuranceAmt = 0;
		double  conv_qty_stduom = 0, conv_rtuom_stduom = 0,  exchRate = 0,  rate_stduom = 0, rate = 0;

		String  packInstr = "",  acct_dr = "", cctr_dr = "", acct_cr = "", cctr_cr = "", disc_type = "";
		String currCodeComm = "", analCode = "";
		Timestamp refDate = null, ordDate = null;
		Timestamp  taxDate = null, confDate = null;
		String suppName = "", suppAdd1 = "", suppAdd2 = "", suppCity = "", suppStanCode = "";
		int currentFormNo = 0,count=0;
		double quantity = 0, dlvQty = 0, quantityStduom = 0, noArt = 0, rateClg = 0,indQty=0,poDetTotalQty=0.0;
		Timestamp dlvDate = null, reqDate = null, duedate = null;
		String WorkOrder = "", packCode = "", discountType = "", specificInstr = "", licenceNo = "", formNo = "", dutyPaid = "";
		DistCommon disscommon = new DistCommon();
		
		String unitRate = "",retString="", unitStd = "";
		String frtType = "";
		double frtRate = 0, frtAmtQty = 0, frtAmtFixed = 0;
		String lineNo="",statusHdr="";
		String refDateStr = "";
		String taxDateStr = "";
		String ordDateStr = "",lineNo1="",empCodeQcApr="",poHdrStatus="";
		String purcOrder=null;
		int forcount=0,lineCnt=0;
		
		genericUtility= new  E12GenericUtility();
		Connection conn = null;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Calendar currentDate2 = Calendar.getInstance();
			String sysDateStr = sdf.format(currentDate2.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			System.out.println("parentNodeListLength:::::::::"+parentNodeListLength);
			purcOrder = genericUtility.getColumnValue("purc_order",detailDom);
			lineNo = genericUtility.getColumnValue("line_no",detailDom);
			status = genericUtility.getColumnValue("status",detailDom);
			System.out.println("Purchase"+purcOrder);
			System.out.println("lineno"+lineNo);
			System.out.println("status"+status);
			sql = "select count(line_no) from porddet where purc_order=? AND status='C'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				lineCnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select status from porder where purc_order=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				statusHdr = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("statusHdr@@@"+statusHdr);
			if ((lineCnt == parentNodeListLength) && (statusHdr.equals("C"))) {
				
				
				
				/*
				 * Error Msg=Use Po open option for full open po.
				 * Description:LineCnt:--taking total line count of those
				 * purchase order having status is 'O' parenNodeListLenght:--*/
				 
				errString = itmDBAccessEJB.getErrorString("", "VTNOCLOSE", "",
						"", conn);
				return errString;

				
				
			}
			
			sql = " select purc_order,ord_date,pord_type,supp_code,site_code__dlv,site_code__ord,site_code__bill,"
					+ "  status,status_date,dept_code,emp_code,order_db,item_ser,tax_opt,cr_term,ord_amt,tax_amt,tot_amt,"
					+ "  curr_code,exch_rate,tax_chap,tax_class,tax_env,remarks,tax_date,proj_code,sales_pers,comm_perc,"
					+ "  comm_perc__on,curr_code__comm,quot_no,confirmed,conf_date,tran_code,frt_amt,curr_code__frt,frt_term,"
					+ "  dlv_term,insurance_amt,curr_code__ins,emp_code__aprv,ref_date ,anal_code,"
					+ "  FRT_TYPE, FRT_RATE,FRT_AMT__QTY, FRT_AMT__FIXED  "
					+ "  from porder where purc_order = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				purcOrder = checkNull(rs.getString("purc_order"));
				ordDate = rs.getTimestamp("ord_date");
				pordType = checkNull(rs.getString("pord_type"));
				suppCode = checkNull(rs.getString("supp_code"));
				siteCodeDlv = checkNull(rs.getString("site_code__dlv"));
				siteCodeOrd = checkNull(rs.getString("site_code__ord"));
				siteCodeBill = checkNull(rs.getString("site_code__bill"));
				status = checkNull(rs.getString("status"));
				//statusDate = rs.getTimestamp("status_date");
				deptCode = checkNull(rs.getString("dept_code"));
				empCode = checkNull(rs.getString("emp_code"));
				orderDb = checkNull(rs.getString("order_db"));
				itemSer = checkNull(rs.getString("item_ser"));
				taxOpt = checkNull(rs.getString("tax_opt"));
				crTerm = checkNull(rs.getString("cr_term"));
				ordAmt = rs.getDouble("ord_amt");
				taxAmt = rs.getDouble("tax_amt");
				totAmt = rs.getDouble("tot_amt");
				currCode = checkNull(rs.getString("curr_code"));
				exchRate = rs.getDouble("exch_rate");
				taxChap = checkNull(rs.getString("tax_chap"));
				taxClass = checkNull(rs.getString("tax_class"));
				taxEnv = checkNull(rs.getString("tax_env"));
				remarks = checkNull(rs.getString("remarks"));
				taxDate = rs.getTimestamp("tax_date");
				projCode = checkNull(rs.getString("proj_code"));
				salesPers = checkNull(rs.getString("sales_pers"));
				commPerc = rs.getDouble("comm_perc");
				commPercOn = checkNull(rs.getString("comm_perc__on"));
				currCodeComm = checkNull(rs.getString("curr_code__comm"));
				quotNo = checkNull(rs.getString("quot_no"));
				confirmed = checkNull(rs.getString("confirmed"));
				confDate = rs.getTimestamp("conf_date");
				tranCode = checkNull(rs.getString("tran_code"));
				frtAmt = rs.getDouble("frt_amt");
				currCodeFrt = checkNull(rs.getString("curr_code__frt"));
				frtTerm = checkNull(rs.getString("frt_term"));
				dlvTerm = checkNull(rs.getString("dlv_term"));
				insuranceAmt = rs.getDouble("insurance_amt");
				currCodeIns = checkNull(rs.getString("curr_code__ins"));
				empCodeAprv = checkNull(rs.getString("emp_code__aprv"));
				refDate = rs.getTimestamp("ref_date");
				analCode = checkNull(rs.getString("anal_code"));
				
				frtType = checkNull(rs.getString("FRT_TYPE"));
				frtRate = rs.getDouble("FRT_RATE");
				frtAmtQty = rs.getDouble("FRT_AMT__QTY");
				frtAmtFixed = rs.getDouble("FRT_AMT__FIXED");
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n").append("<Root>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("porderamd").append("]]></objName>");
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

			
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"porderamd\" objContext=\"1\">");
		
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			
			
			ordDateStr = sdf.format(ordDate).toString();
			
			xmlBuff.append("<purc_order>").append("<![CDATA[" + purcOrder + "]]>").append("</purc_order>");
			
			System.out.println("@@@@@ordDateStr" + ordDateStr);
			if (ordDateStr != null && ordDateStr.trim().length() > 0) {
				xmlBuff.append("<ord_date>").append("<![CDATA[" + ordDateStr + "]]>").append("</ord_date>");
			} else 
			{
				xmlBuff.append("<ord_date>").append("<![CDATA[]]>").append("</ord_date>");
			}
			xmlBuff.append("<pord_type>")
					.append("<![CDATA[" + pordType + "]]>")
					.append("</pord_type>");
			xmlBuff.append("<pord_type>")
					.append("<![CDATA[" + pordType + "]]>")
					.append("</pord_type>");
			xmlBuff.append("<supp_code>")
					.append("<![CDATA[" + suppCode + "]]>")
					.append("</supp_code>");
			xmlBuff.append("<supp_code__o>")
					.append("<![CDATA[" + suppCode + "]]>")
					.append("</supp_code__o>");
			xmlBuff.append("<site_code__dlv>")
					.append("<![CDATA[" + siteCodeDlv + "]]>")
					.append("</site_code__dlv>");
			xmlBuff.append("<site_code__dlv__o>")
					.append("<![CDATA[" + siteCodeDlv + "]]>")
					.append("</site_code__dlv__o>");
			xmlBuff.append("<site_code__ord>")
					.append("<![CDATA[" + siteCodeOrd + "]]>")
					.append("</site_code__ord>");
			xmlBuff.append("<site_code__ord__o>")
					.append("<![CDATA[" + siteCodeOrd + "]]>")
					.append("</site_code__ord__o>");
			xmlBuff.append("<site_code__bill>")
					.append("<![CDATA[" + siteCodeBill + "]]>")
					.append("</site_code__bill>");
			xmlBuff.append("<site_code__bill__o>")
					.append("<![CDATA[" + siteCodeBill + "]]>")
					.append("</site_code__bill__o>");
			xmlBuff.append("<dept_code>")
					.append("<![CDATA[" + deptCode + "]]>")
					.append("</dept_code>");
			xmlBuff.append("<dept_code__o>")
					.append("<![CDATA[" + deptCode + "]]>")
					.append("</dept_code__o>");
			xmlBuff.append("<emp_code>")
					.append("<![CDATA[" + empCode + "]]>")
					.append("</emp_code>");
			xmlBuff.append("<emp_code__o>")
					.append("<![CDATA[" + empCode + "]]>")
					.append("</emp_code__o>");
			xmlBuff.append("<anal_code>")
					.append("<![CDATA[" + analCode + "]]>")
					.append("</anal_code>");

			sql = " select case when employee.emp_fname is null then ' ' else employee.emp_fname end as fname,"
					+ " case when employee.emp_mname is null then ' ' else employee.emp_mname end as mname,"
					+ " case when employee.emp_lname is null then ' ' else employee.emp_lname end as lname"
					+ " from employee where emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				fname = checkNull(rs.getString(1));
				mname = checkNull(rs.getString(2));
				lname = checkNull(rs.getString(3));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			xmlBuff.append("<emp_fname>")
					.append("<![CDATA[" + fname + "]]>")
					.append("</emp_fname>");
			xmlBuff.append("<emp_mname>")
					.append("<![CDATA[" + mname + "]]>")
					.append("</emp_mname>");
			xmlBuff.append("<emp_lname>")
					.append("<![CDATA[" + lname + "]]>")
					.append("</emp_lname>");

			xmlBuff.append("<order_db>")
					.append("<![CDATA[" + orderDb + "]]>")
					.append("</order_db>");
			xmlBuff.append("<item_ser>")
					.append("<![CDATA[" + itemSer + "]]>")
					.append("</item_ser>");
			xmlBuff.append("<item_ser__o>")
					.append("<![CDATA[" + itemSer + "]]>")
					.append("</item_ser__o>");
			xmlBuff.append("<tax_opt>")
					.append("<![CDATA[" + taxOpt + "]]>")
					.append("</tax_opt>");
			xmlBuff.append("<tax_opt__o>")
					.append("<![CDATA[" + taxOpt + "]]>")
					.append("</tax_opt__o>");
			xmlBuff.append("<cr_term>")
					.append("<![CDATA[" + crTerm + "]]>")
					.append("</cr_term>");
			xmlBuff.append("<cr_term__o>")
					.append("<![CDATA[" + crTerm + "]]>")
					.append("</cr_term__o>");
			xmlBuff.append("<ord_amt__o>")
					.append("<![CDATA[" + ordAmt + "]]>")
					.append("</ord_amt__o>");
			xmlBuff.append("<tax_amt__o>")
					.append("<![CDATA[" + taxAmt + "]]>")
					.append("</tax_amt__o>");
			xmlBuff.append("<tot_amt__o>")
					.append("<![CDATA[" + totAmt + "]]>")
					.append("</tot_amt__o>");
			xmlBuff.append("<ord_amt>")
					.append("<![CDATA[" + ordAmt + "]]>")
					.append("</ord_amt>");
			xmlBuff.append("<tax_amt>")
					.append("<![CDATA[" + taxAmt + "]]>")
					.append("</tax_amt>");
			xmlBuff.append("<tot_amt>")
					.append("<![CDATA[" + totAmt + "]]>")
					.append("</tot_amt>");
			xmlBuff.append("<curr_code>")
					.append("<![CDATA[" + currCode + "]]>")
					.append("</curr_code>");
			xmlBuff.append("<curr_code__o>")
					.append("<![CDATA[" + currCode + "]]>")
					.append("</curr_code__o>");
			xmlBuff.append("<exch_rate>")
					.append("<![CDATA[" + exchRate + "]]>")
					.append("</exch_rate>");
			xmlBuff.append("<exch_rate__o>")
					.append("<![CDATA[" + exchRate + "]]>")
					.append("</exch_rate__o>");
			xmlBuff.append("<tax_chap>")
					.append("<![CDATA[" + taxChap + "]]>")
					.append("</tax_chap>");
			xmlBuff.append("<tax_chap__o>")
					.append("<![CDATA[" + taxChap + "]]>")
					.append("</tax_chap__o>");
			xmlBuff.append("<tax_class>")
					.append("<![CDATA[" + taxClass + "]]>")
					.append("</tax_class>");
			xmlBuff.append("<tax_class__o>")
					.append("<![CDATA[" + taxClass + "]]>")
					.append("</tax_class__o>");
			xmlBuff.append("<tax_env>")
					.append("<![CDATA[" + taxEnv + "]]>")
					.append("</tax_env>");
			xmlBuff.append("<tax_env__o>")
					.append("<![CDATA[" + taxEnv + "]]>")
					.append("</tax_env__o>");
			xmlBuff.append("<remarks>")
					.append("<![CDATA[" + remarks + "]]>")
					.append("</remarks>");

			System.out.println("Taxdate>>>" + taxDate);

			if (taxDate != null) {
				taxDateStr = sdf.format(taxDate).toString();
				System.out.println("Taxdate>>>" + taxDateStr);
				xmlBuff.append("<tax_date__o>")
						.append("<![CDATA[" + taxDateStr + "]]>")
						.append("</tax_date__o>");
			} else {
				xmlBuff.append("<tax_date__o>")
						.append("<![CDATA[]]>")
						.append("</tax_date__o>");
			}
			xmlBuff.append("<proj_code>")
					.append("<![CDATA[" + projCode + "]]>")
					.append("</proj_code>");
			xmlBuff.append("<proj_code__o>")
					.append("<![CDATA[" + projCode + "]]>")
					.append("</proj_code__o>");
			xmlBuff.append("<sales_pers>")
					.append("<![CDATA[" + salesPers + "]]>")
					.append("</sales_pers>");
			xmlBuff.append("<sales_pers__o>")
					.append("<![CDATA[" + salesPers + "]]>")
					.append("</sales_pers__o>");
			xmlBuff.append("<comm_perc>")
					.append("<![CDATA[" + commPerc + "]]>")
					.append("</comm_perc>");
			xmlBuff.append("<comm_perc__o>")
					.append("<![CDATA[" + commPerc + "]]>")
					.append("</comm_perc__o>");
			xmlBuff.append("<comm_perc__on>")
					.append("<![CDATA[" + common + "]]>")
					.append("</comm_perc__on>");
			xmlBuff.append("<comm_perc__on__o>")
					.append("<![CDATA[" + common + "]]>")
					.append("</comm_perc__on__o>");
			xmlBuff.append("<curr_code__comm>")
					.append("<![CDATA[" + currCodeComm + "]]>")
					.append("</curr_code__comm>");
			xmlBuff.append("<curr_code__comm__o>")
					.append("<![CDATA[" + currCodeComm + "]]>")
					.append("</curr_code__comm__o>");
			xmlBuff.append("<quot_no>")
					.append("<![CDATA[" + quotNo + "]]>")
					.append("</quot_no>");
			xmlBuff.append("<quot_no__o>")
					.append("<![CDATA[" + quotNo + "]]>")
					.append("</quot_no__o>");
			xmlBuff.append("<tran_code>")
					.append("<![CDATA[" + tranCode + "]]>")
					.append("</tran_code>");
			xmlBuff.append("<tran_code__o>")
					.append("<![CDATA[" + tranCode + "]]>")
					.append("</tran_code__o>");
			xmlBuff.append("<frt_amt__o>")
					.append("<![CDATA[" + frtAmt + "]]>")
					.append("</frt_amt__o>");
			xmlBuff.append("<curr_code__frt>")
					.append("<![CDATA[" + currCodeFrt + "]]>")
					.append("</curr_code__frt>");
			xmlBuff.append("<curr_code__frt__o>")
					.append("<![CDATA[" + currCodeFrt + "]]>")
					.append("</curr_code__frt__o>");
			xmlBuff.append("<frt_term>")
					.append("<![CDATA[" + frtTerm + "]]>")
					.append("</frt_term>");
			xmlBuff.append("<frt_term__o>")
					.append("<![CDATA[" + frtTerm + "]]>")
					.append("</frt_term__o>");
			xmlBuff.append("<dlv_term>")
					.append("<![CDATA[" + dlvTerm + "]]>")
					.append("</dlv_term>");
			xmlBuff.append("<dlv_term__o>")
					.append("<![CDATA[" + dlvTerm + "]]>")
					.append("</dlv_term__o>");
			xmlBuff.append("<insurance_amt>")
					.append("<![CDATA[" + insuranceAmt + "]]>")
					.append("</insurance_amt>");
			xmlBuff.append("<insurance_amt__o>")
					.append("<![CDATA[" + insuranceAmt + "]]>")
					.append("</insurance_amt__o>");
			xmlBuff.append("<curr_code__ins>")
					.append("<![CDATA[" + currCodeIns + "]]>")
					.append("</curr_code__ins>");
			xmlBuff.append("<curr_code__ins__o>")
					.append("<![CDATA[" + currCodeIns + "]]>")
					.append("</curr_code__ins__o>");
			xmlBuff.append("<emp_code__aprv>")
					.append("<![CDATA[" + empCodeAprv + "]]>")
					.append("</emp_code__aprv>");
			xmlBuff.append("<emp_code__aprv__o>")
					.append("<![CDATA[" + empCodeAprv + "]]>")
					.append("</emp_code__aprv__o>");
			if (refDate != null) {
				refDateStr = sdf.format(refDate).toString();
			}

			xmlBuff.append("<ref_date>")
					.append("<![CDATA[" + refDateStr + "]]>")
					.append("</ref_date>");
			xmlBuff.append("<ref_date__o>")
					.append("<![CDATA[" + refDateStr + "]]>")
					.append("</ref_date__o>");

			if (suppCode != null && suppCode.length() > 0) {
				sql = "select supp_name, addr1, addr2, city, stan_code from supplier where supp_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					suppName = checkNull(rs.getString("supp_name"));
					suppAdd1 = checkNull(rs.getString("addr1"));
					suppAdd2 = checkNull(rs.getString("addr2"));
					suppCity = checkNull(rs.getString("city"));
					suppStanCode = checkNull(rs.getString("stan_code"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			xmlBuff.append("<supp_name>")
					.append("<![CDATA[" + checkNull(suppName) + "]]>")
					.append("</supp_name>");
			xmlBuff.append("<supplier_addr1>")
					.append("<![CDATA[" + checkNull(suppAdd1) + "]]>")
					.append("</supplier_addr1>");
			xmlBuff.append("<supplier_addr2>")
					.append("<![CDATA[" + checkNull(suppAdd2) + "]]>")
					.append("</supplier_addr2>");
			xmlBuff.append("<supplier_city>")
					.append("<![CDATA[" + checkNull(suppCity) + "]]>")
					.append("</supplier_city>");
			xmlBuff
					.append("<supplier_stan_code>")
					.append("<![CDATA[" + checkNull(suppStanCode)
							+ "]]>").append("</supplier_stan_code>");

			
			xmlBuff.append("<frt_type>")
					.append("<![CDATA[" + checkNull(frtType) + "]]>")
					.append("</frt_type>");
			xmlBuff.append("<frt_type__o>")
					.append("<![CDATA[" + checkNull(frtType) + "]]>")
					.append("</frt_type__o>");

			
			// xmlBuff.append("<frt_rate>").append("<![CDATA["+frtRate+"]]>").append("</frt_rate>");
			xmlBuff.append("<frt_rate__o>")
					.append("<![CDATA[" + frtRate + "]]>")
					.append("</frt_rate__o>");
			// xmlBuff.append("<frt_amt__qty>").append("<![CDATA["+frtAmtQty+"]]>").append("</frt_amt__qty>");
			xmlBuff.append("<frt_amt__qty__o>")
					.append("<![CDATA[" + frtAmtQty + "]]>")
					.append("</frt_amt__qty__o>");
			// xmlBuff.append("<frt_amt__fixed>").append("<![CDATA["+frtAmtFixed+"]]>").append("</frt_amt__fixed>");
			xmlBuff.append("<frt_amt__fixed__o>")
					.append("<![CDATA[" + frtAmtFixed + "]]>")
					.append("</frt_amt__fixed__o>");

			xmlBuff.append("</Detail1>");
		System.out.println("end detail1@@@"+xmlBuff);
			
			
					for ( selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{
					parentNode = parentNodeList.item( selectedRow );
				    childNodeList = parentNode.getChildNodes();
				    childNodeListLength = childNodeList.getLength();
					//rejectionExist = false;
				    forcount++;
				    System.out.println("for loop counter@@@"+forcount);
					for (int childRow = 0; childRow < childNodeListLength; childRow++)
					{

					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					System.out.println( "childNodeName.........:-"+ childNodeName );
					if (childNodeName.equals("purc_order"))
					{
						System.out.println( "purc_order@@@ before.........:-"+ purcOrder );
						purcOrder = childNode.getFirstChild().getNodeValue();
						System.out.println( "purc_order@@@ after.........:-"+ purcOrder );
						
					} if(childNodeName.equals("line_no"))
					{
						System.out.println( "lineNo@@@ before.........:-"+ lineNo );
						lineNo = childNode.getFirstChild().getNodeValue();
						System.out.println( "lineNo@@@ after.........:-"+ lineNo );
						
					}
					 
					
				    }
				sql = " select site_code,ind_no,item_code,quantity,unit,rate,discount,tax_amt,tot_amt,loc_code,req_date,"
						+ " dlv_date,dlv_qty,status,status_date,tax_class,tax_chap,tax_env,remarks,work_order,unit__rate,"
						+ "	conv__qty_stduom,conv__rtuom_stduom,unit__std,quantity__stduom,RATE__STDUOM,pack_code,no_art,pack_instr,"
						+ "	acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,discount_type,status,supp_code__mnfr,specific_instr,"
						+ "	RATE__CLG,special_instr,benefit_type,licence_no,form_no,duty_paid,dept_code,proj_code,emp_code__qcaprv,line_no"
						+ "   from  porddet where purc_order = ? and line_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				pstmt.setString(2, lineNo);
				rs = pstmt.executeQuery();
				while(rs.next()) {
					count++;
					siteCode = checkNull(rs.getString("site_code"));
					indNo = checkNull(rs.getString("ind_no"));
					itemCode = checkNull(rs.getString("item_code"));
					quantity = rs.getDouble("quantity");
					unit = checkNull(rs.getString("unit"));
					rate = rs.getDouble("rate");
					discount = rs.getDouble("discount");
					taxAmt = rs.getDouble("tax_amt");
					totAmt = rs.getDouble("tot_amt");
					locCode = checkNull(rs.getString("loc_code"));
					reqDate = rs.getTimestamp("req_date");
					dlvDate = rs.getTimestamp("dlv_date");
					dlvQty = rs.getDouble("dlv_qty");
					status = checkNull(rs.getString("status"));
					//statusDate = rs.getTimestamp("status_date");
					taxClass = checkNull(rs.getString("tax_class"));
					taxChap = checkNull(rs.getString("tax_chap"));
					taxEnv = checkNull(rs.getString("tax_env"));
					remarks = checkNull(rs.getString("remarks"));
					WorkOrder = checkNull(rs.getString("work_order"));
					unitRate = checkNull(rs.getString("unit__rate"));
					conv_qty_stduom = rs.getDouble("conv__qty_stduom");
					conv_rtuom_stduom = rs.getDouble("conv__rtuom_stduom");
					unitStd = checkNull(rs.getString("unit__std"));
					quantityStduom = rs.getDouble("quantity__stduom");
					rate_stduom = rs.getDouble("RATE__STDUOM");
					packCode = checkNull(rs.getString("pack_code"));
					noArt = rs.getDouble("no_art");
					packInstr = checkNull(rs.getString("pack_instr"));
					acct_dr = (rs.getString("acct_code__dr"));
					cctr_dr = (rs.getString("cctr_code__dr"));
					acct_cr = (rs.getString("acct_code__cr"));
					cctr_cr = (rs.getString("cctr_code__cr"));
					discountType = checkNull(rs.getString("discount_type"));
					//status = checkNull(rs.getString("status"));
					supp_code_mnfr = checkNull(rs.getString("supp_code__mnfr"));
					specificInstr = checkNull(rs.getString("specific_instr"));
					rateClg = rs.getDouble("RATE__CLG");
					specialInstr = checkNull(rs.getString("special_instr"));
					benefitType = checkNull(rs.getString("benefit_type"));
					licenceNo = checkNull(rs.getString("licence_no"));
					formNo = checkNull(rs.getString("form_no"));
					dutyPaid = checkNull(rs.getString("duty_paid"));
					deptCode = checkNull(rs.getString("dept_code"));
					projCode = checkNull(rs.getString("proj_code"));
					empCodeQcApr=checkNull(rs.getString("emp_code__qcaprv"));
					//line_no
					lineNo1=checkNull(rs.getString("line_no"));
					xmlBuff.append("<Detail2 dbID=\"\" domID=\"1\" objContext=\"2\" objName=\"porderamd\">");
					xmlBuff.append("<attribute selected=\"N\" updateFlag=\"N\" status=\"O\" pkNames=\"\"/>");
					xmlBuff.append("<purc_order><![CDATA[" + purcOrder + "]]></purc_order>");
					xmlBuff.append("<line_no><![CDATA[" + lineNo + "]]></line_no>");
					xmlBuff.append("<line_no__ord><![CDATA[" + lineNo1 + "]]></line_no__ord>");
					xmlBuff.append("<site_code><![CDATA[" + siteCode + "]]></site_code>");
					xmlBuff.append("<ind_no><![CDATA[" + indNo + "]]></ind_no>");
					xmlBuff.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
					xmlBuff.append("<quantity><![CDATA[" + quantity + "]]></quantity>");
					xmlBuff.append("<unit><![CDATA[" + unit + "]]></unit>");
					xmlBuff.append("<rate><![CDATA[" + rate + "]]></rate>");
					xmlBuff.append("<rate__o><![CDATA[" + rate + "]]></rate__o>");
					
					//rate__stduom
					xmlBuff.append("<rate__stduom><![CDATA[" + rate_stduom + "]]></rate__stduom>");
					xmlBuff.append("<discount><![CDATA[" + discount + "]]></discount>");
					xmlBuff.append("<req_date><![CDATA[" + sdf.format(reqDate).toString()+ "]]></req_date>");
					xmlBuff.append("<req_date_o><![CDATA[" + sdf.format(reqDate).toString()+ "]]></req_date_o>");
					
					if(status.equals("C")||status.equalsIgnoreCase("C"))
					{
						status="O";
					xmlBuff.append("<status><![CDATA[" + status + "]]></status>");
					}else
					{	
						xmlBuff.append("<status><![CDATA[" + status + "]]></status>");
					}
					
					//xmlBuff.append("<status_date><![CDATA[" + statusDate + "]]></status_date>");
					xmlBuff.append("<loc_code><![CDATA[" + locCode + "]]></loc_code>");
					xmlBuff.append("<tax_class><![CDATA[" + taxClass + "]]></tax_class>");
					xmlBuff.append("<tax_chap><![CDATA[" + taxChap + "]]></tax_chap>");
					System.out.println("taxEnv@@before-->"+taxEnv);
					
					xmlBuff.append("<tax_env><![CDATA[" + taxEnv + "]]></tax_env>");
					
					System.out.println("taxEnv@@after-->"+taxEnv);
					xmlBuff.append("<remarks><![CDATA[" + remarks + "]]></remarks>");
					xmlBuff.append("<work_order><![CDATA[" + WorkOrder + "]]></work_order>");
					xmlBuff.append("<tax_amt><![CDATA[" + taxAmt + "]]></tax_amt>");
					xmlBuff.append("<tot_amt><![CDATA[" + totAmt + "]]></tot_amt>");
					xmlBuff.append("<dlv_qty><![CDATA[" + dlvQty + "]]></dlv_qty>");
					xmlBuff.append("<unit__rate><![CDATA[" + unitRate + "]]></unit__rate>");
					xmlBuff.append("<conv__qty_stduom><![CDATA[" + conv_qty_stduom + "]]></conv__qty_stduom>");
					xmlBuff.append("<conv__rtuom_stduom><![CDATA[" + conv_rtuom_stduom + "]]></conv__rtuom_stduom>");
					xmlBuff.append("<unit__std><![CDATA[" + unitStd + "]]></unit__std>");
					xmlBuff.append("<quantity__stduom><![CDATA[" + quantityStduom + "]]></quantity__stduom>");
					xmlBuff.append("<no_art><![CDATA[" + noArt + "]]></no_art>");
					xmlBuff.append("<acct_code__dr><![CDATA[" + acct_dr + "]]></acct_code__dr>");
					xmlBuff.append("<cctr_code__dr><![CDATA[" + cctr_dr + "]]></cctr_code__dr>");
					xmlBuff.append("<acct_code__cr><![CDATA[" + acct_cr + "]]></acct_code__cr>");
					xmlBuff.append("<cctr_code__cr><![CDATA[" + cctr_cr + "]]></cctr_code__cr>");
					xmlBuff.append("<discount_type><![CDATA[" + discountType + "]]></discount_type>");
					xmlBuff.append("<rate__clg><![CDATA[" + rateClg + "]]></rate__clg>");
					xmlBuff.append("<specific_instr><![CDATA[" + specificInstr + "]]></specific_instr>");
					xmlBuff.append("<special_instr><![CDATA[" + specialInstr + "]]></special_instr>");
					xmlBuff.append("<emp_code__qcaprv><![CDATA[" + empCodeQcApr + "]]></emp_code__qcaprv>");
					xmlBuff.append("<emp_code__qcaprv_o><![CDATA[" + empCodeQcApr + "]]></emp_code__qcaprv_o>");
					
					
					xmlBuff.append("<proj_code><![CDATA[" + projCode + "]]></proj_code>");
					//xmlBuff.append("<spl_instr><![CDATA[" + specificInstr + "]]></spl_instr>");
					//xmlBuff.append("<exch_rate><![CDATA[" + exchRate + "]]></exch_rate>");
					xmlBuff.append("<dept_code><![CDATA[" + deptCode + "]]></dept_code>");
				
					xmlBuff.append("<benefit_type><![CDATA[" + benefitType + "]]></benefit_type>");
					xmlBuff.append("<licence_no><![CDATA[" + licenceNo + "]]></licence_no>");
					//xmlBuff.append("<spec_metadata><![CDATA[" + specMetadata + "]]></spec_metadata>");
					//xmlBuff.append("<spec_dimension><![CDATA[" + specDimension + "]]></spec_dimension>");
					//xmlBuff.append("<supp_item__ref><![CDATA[" + suppItemRef + "]]></supp_item__ref>");
					//xmlBuff.append("<quantity__fc><![CDATA[" + qtyFc + "]]></quantity__fc>");
					//xmlBuff.append("<prd_code__rfc><![CDATA[" + prdCodeRfc + "]]></prd_code__rfc>");
					xmlBuff.append("<form_no><![CDATA[" + formNo + "]]></form_no>");
					xmlBuff.append("<duty_paid><![CDATA[" + dutyPaid + "]]></duty_paid>");
					xmlBuff.append("<dlv_date><![CDATA[" + sdf.format(dlvDate).toString() + "]]></dlv_date>");
					xmlBuff.append("<site_code__o><![CDATA[" + siteCode + "]]></site_code__o>");
					xmlBuff.append("<quantity__o><![CDATA[" + quantity + "]]></quantity__o>");
					//xmlBuff.append("<rate_o><![CDATA[" + rate + "]]></rate__o>");
					xmlBuff.append("<tax_class__o><![CDATA[" + taxClass + "]]></tax_class__o>");
					xmlBuff.append("<tax_chap__o><![CDATA[" + taxChap + "]]></tax_chap__o>");
					xmlBuff.append("<tax_env__o><![CDATA[" + taxEnv + "]]></tax_env__o>");
					xmlBuff.append("<dlv_date__o><![CDATA[" + sdf.format(dlvDate).toString() + "]]></dlv_date__o>");
					xmlBuff.append("<specific_instr__o><![CDATA[" + specificInstr + "]]></specific_instr__o>");
					//xmlBuff.append("<supp_code_mnfr__o><![CDATA[" + suppCodeMnfr + "]]></supp_code_mnfr__o>");
					
					xmlBuff.append("<special_instr__o><![CDATA[" + specialInstr + "]]></special_instr__o>");
					xmlBuff.append("<benefit_type__o><![CDATA[" + benefitType + "]]></benefit_type__o>");
					xmlBuff.append("<licence_no__o><![CDATA[" + licenceNo + "]]></licence_no__o>");
					xmlBuff.append("<form_no__o><![CDATA[" + formNo + "]]></form_no__o>");
					xmlBuff.append("<duty_paid__o><![CDATA[" + dutyPaid + "]]></duty_paid__o>");
					xmlBuff.append("<acct_code__dr__o><![CDATA[" + acct_dr + "]]></acct_code__dr__o>");
					xmlBuff.append("<cctr_code__dr__o><![CDATA[" + cctr_dr + "]]></cctr_code__dr__o>");
					xmlBuff.append("<acct_code__cr__o><![CDATA[" + acct_cr + "]]></acct_code__cr__o>");
					xmlBuff.append("<cctr_code__cr__o><![CDATA[" + cctr_cr + "]]></cctr_code__cr__o>");
					xmlBuff.append("<proj_code__o><![CDATA[" + projCode + "]]></proj_code__o>");
					xmlBuff.append("</Detail2>");
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

							
					}
					
					xmlBuff.append("</Header0>");
					xmlBuff.append("</group0>");
					xmlBuff.append("</DocumentRoot>");
					xmlBuff.append("</Root>\r\n");
					String xmlString = xmlBuff.toString();
					System.out.println("String xmlString@@@"+xmlString);
					System.out.println("XML@@@"+xmlBuff.toString());
					
					siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					System.out.println("== site code =="+siteCode);
					//Changes and Commented By Ajay on 08-01-2018:START
	                String userId1 = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  
	                System.out.println("--login code--"+userId1);
					//retString=saveData(siteCode,xmlString,conn);
					retString=saveData(siteCode,xmlString,userId1,conn);
					//Changes and Commented By Ajay on 08-01-2018:END
					System.out.println("@@@@@2: retString:"+retString);
					System.out.println("--retString finished--");
					
					if(retString.indexOf("Success") > -1 )
					{
						System.out.println("comming into confirm function");
						POrderAmdConf Conf=new POrderAmdConf();
						//errString = Conf.confirm(tranId, xtraParams, forcedFlag);
						
						String[] arrayForTranId1 = retString.split("<TranID>");

		                   System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId1);
		                   System.out.println("Tran ID :::::in conf:::::::"+arrayForTranId1[1]);

		                   int endIndex1 = arrayForTranId1[1].indexOf("</TranID>");

		                   System.out.println("endIndex1:::::::"+endIndex1);

		                   String tranIdPe = arrayForTranId1[1].substring(0, endIndex1);
		                   System.out.println("AmdTranId=====["+tranIdPe+"]");
						
		                  // String tranIdPe;
		                  String  forcedFlag="";
		                  /**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]
						[changed method signature to pass xtraParams to update login user and login terminal]*/
						//errString = Conf.ConfirmPordAmd(tranIdPe, conn);//(tranIdPe, xtraParams, forcedFlag);
		                  errString = Conf.ConfirmPordAmd(tranIdPe, xtraParams, conn);
		                  /**Modified by Pavan Rane 24dec19 end[changed the mtheod signature in POrderAmdConf component]*/
						System.out.println("errString@conf@"+errString);
						if(errString==null||errString.trim().length()==0 ||  errString.indexOf("Success")>-1 || errString.equalsIgnoreCase("PRCUSUCCES")||errString.equals("PRCUSUCCES")||errString.equals("PRCUSUCCES"))
						{
						
							
							//System.out.println("status_date="+sdf.format(sysDateStr).toString());
							
							try {
								double ordQty=0;
								Timestamp sysDate1 = null;
								SimpleDateFormat sdf1 = null;
								String sysDateStr1 = "",errString1="",loginEmpCode="";
								double orderQty=0, convIndOrdQty=0;
								double totalOrdqty=0 , indOrdQty=0;
								
								DistCommon distCommon = new DistCommon();
								int cnt = 0, detCnt = 0;
								Calendar currentDate = Calendar.getInstance();
								sdf1 = new SimpleDateFormat(
										genericUtility.getApplDateFormat());
								sysDateStr1 = sdf1.format(currentDate.getTime());
								System.out
										.println("after conf method Now the date is :=>  "
												+ sysDateStr1);
								sysDate1 = Timestamp.valueOf(genericUtility
										.getValidDateString(sysDateStr1,
												genericUtility.getApplDateFormat(),
												genericUtility.getDBDateFormat())
										+ " 00:00:00.0");
								String userId = genericUtility.getValueFromXTRA_PARAMS(
										xtraParams, "loginCode");
								loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
								System.out.println("dlvQty@@At before"+dlvQty);
								System.out.println("loginEmpCode@@"+loginEmpCode);
								sql = " update poamd_hdr set confirmed = 'Y',conf_date = ?, emp_code__aprv = ? where amd_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, sysDate1);
								pstmt.setString(2, loginEmpCode);
								pstmt.setString(3, tranIdPe);
								cnt = pstmt.executeUpdate();
								System.out.println("cnt@@update@"+cnt);
								pstmt.close();
								pstmt = null;
								rs=null;
								//rs.close();
								
								
								
								sql1 = "SELECT LINE_NO__ORD,IND_NO FROM POAMD_DET WHERE amd_no=? AND purc_order=? ";
								pstmt1 = conn.prepareStatement(sql1);

								pstmt1.setString(1, tranIdPe);
								pstmt1.setString(2, purcOrder);
								rs1 = pstmt1.executeQuery();
								while (rs1.next()) {
									lineNo1 = rs1.getString(1);
									indNo = rs1.getString(2);
									System.out.println("lineNo@@newhile" + lineNo1);
									System.out.println("indNo@@newhile" + indNo);

									if (indNo != null) {
										System.out
												.println("Comming  after success msg@@");

										/*
										 * Below code is used for one indent one porder
										 * case and also checking if receipt exist then
										 * set indent status should be closed'C'
										 */
										
										sql = " select quantity,ord_qty,quantity__stduom from indent where  ind_no = ? ";
										pstmt = conn.prepareStatement(sql);

										pstmt.setString(1, indNo);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											indQty = rs.getDouble(1);
											ordQty=rs.getDouble(2);
											quantityStduom=rs.getDouble(3);
											
											orderQty=ordQty;
											System.out.println("orderQty@@for dlvQty@@"+orderQty);
											System.out.println("ordQty@@for NOT dlvQty@@"+ordQty);
										System.out.println("indQty@@" + indQty);
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										sql = " select d.quantity,d.dlv_qty ,d.unit,d.unit__std ,d.item_code from  porder h,porddet d where h.purc_order=d.purc_order and d.purc_order=?  and d.ind_no=? and d.line_no=? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, purcOrder);
										pstmt.setString(2, indNo);
										pstmt.setString(3, lineNo1);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											poDetTotalQty = rs.getDouble(1);
											dlvQty=rs.getDouble(2);
											unit=rs.getString(3);
											unitStd=rs.getString(4);
											itemCode=rs.getString(5);
										}
										System.out.println("poDetTotalQty@@" + poDetTotalQty);
										System.out.println("dlvQty@@At before" + dlvQty);
										
										System.out.println("dlvQty@@" + dlvQty);
										System.out.println("unit@@" + unit);
										System.out.println("unitStd@@" + unitStd);
										System.out.println("itemCode@@" + itemCode);
										
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										sql = " select count(1)  from porder a, porddet b "
												+ "where a.purc_order = b.purc_order	"
												+ " and a.purc_order <> ? "
												+ "and b.ind_no = ? and b.status='C' ";
											/*	+ "and (case when a.status is null then 'O' else a.status end) = 'O' "
												+ "and (case when b.status is null then 'O' else b.status end) = 'O'";*/

										pstmt = conn.prepareStatement(sql);

										pstmt.setString(1, indNo);
										pstmt.setString(2, purcOrder);
										rs = pstmt.executeQuery();
										if (rs.next()) {
											cnt = rs.getInt(1);

										}
										System.out.println("cnt@@useless" + cnt);
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										/*
										 * total qty of purchase order detail is equal
										 * to indent quantity
										 */
										if(poDetTotalQty>=indQty)
										{
										
											if(dlvQty>0)
											{
												System.out.println("dlvQty@@At before" + dlvQty);
												System.out.println("poDetTotalQty@@At before" + poDetTotalQty);
												 totalOrdqty = poDetTotalQty - dlvQty;
												System.out.println("totalOrdqty@@[poqty-dlvqty:::]"+totalOrdqty);
												
												convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, totalOrdqty, conn)	;
												System.out.println("convIndOrdQty"+convIndOrdQty);
												
												
												
												 indOrdQty=orderQty+convIndOrdQty;
												System.out.println("indOrdQty@@[orderQty+totalOrdqty]::"+indOrdQty);
												
												//double totalOrdqty=indQty-dlvQty;
												
												System.out.println("comming into dlvQty>0 condition@@");
												int updCnt1=0;
												if(indOrdQty==quantityStduom)
												{
													sql = " update indent set status = 'L',ord_qty="+indOrdQty+" where  ind_no = ? ";
													
												}else
												{
												sql = " update indent set status = 'O',ord_qty="+indOrdQty+" where  ind_no = ? ";
												}
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, indNo);
												updCnt1 = pstmt.executeUpdate();
												System.out.println("Indent status updated@@"+updCnt1);
												pstmt.close();
												pstmt = null;
												sql = " update porddet set status = 'O' where  ind_no = ? and purc_order=? and line_no=? ";
												pstmt = conn.prepareStatement(sql);
												
												pstmt.setString(1, indNo);
												pstmt.setString(2, purcOrder);
												//lineNo1
												pstmt.setString(3, lineNo1);
												int updCnt = pstmt.executeUpdate();
												System.out.println("porder status updated@@"+updCnt);
												pstmt.close();
												pstmt = null;

												
											}else
											{
											
												System.out.println("dlvQty@@At before" + dlvQty);
												System.out.println("poDetTotalQty@@At before" + poDetTotalQty);
												 totalOrdqty = poDetTotalQty - dlvQty;
												System.out.println("totalOrdqty@@[poqty-dlvqty:::]"+totalOrdqty);
												
												convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, totalOrdqty, conn)	;
												System.out.println("convIndOrdQty"+convIndOrdQty);
												
												
												
												 indOrdQty=orderQty+convIndOrdQty;
												System.out.println("indOrdQty@@[orderQty+totalOrdqty]::"+indOrdQty);
												
												
											System.out.println("comming into quantity>=indQty condition@@");
											int updCnt=0;
											
											if(indOrdQty==quantityStduom)
											{
												sql = " update indent set status = 'L',ord_qty="+indOrdQty+" where  ind_no = ? ";
												
											}
											
											sql = " update indent set status = 'L' ,ord_qty="+indOrdQty+" where  ind_no = ? ";
											pstmt = conn.prepareStatement(sql);
											
											pstmt.setString(1, indNo);
											updCnt = pstmt.executeUpdate();
											System.out.println("Indent status updated@@"+updCnt);
											//rs.close();
											//rs = null;
											pstmt.close();
											pstmt = null;
											sql = " update porddet set status = 'O' where  ind_no = ? and purc_order=? and line_no=? ";
											pstmt = conn.prepareStatement(sql);
											
											pstmt.setString(1, indNo);
											pstmt.setString(2, purcOrder);
									
											pstmt.setString(3, lineNo1);
											updCnt = pstmt.executeUpdate();
											System.out.println("porder status updated@@"+updCnt);
										//	rs.close();
											//rs = null;
											pstmt.close();
											pstmt = null;
											}
											
											
										}else if(poDetTotalQty<indQty)
										{
											
											if(dlvQty>0)
											{
												
												System.out.println("dlvQty@@At before" + dlvQty);
												System.out.println("poDetTotalQty@@At before" + poDetTotalQty);
												 totalOrdqty = poDetTotalQty - dlvQty;
												System.out.println("totalOrdqty@@[poqty-dlvqty:::]"+totalOrdqty);
												
												convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, totalOrdqty, conn)	;
												System.out.println("convIndOrdQty"+convIndOrdQty);
												
												
												
												 indOrdQty=orderQty+convIndOrdQty;
												System.out.println("indOrdQty@@[orderQty+totalOrdqty]::"+indOrdQty);
												
											
											System.out.println("comming into quantity<indQty condition@@");
											int updCnt=0;
											
											sql = " update porddet set status = 'O' where  ind_no = ? and purc_order=? and line_no=? ";
											pstmt = conn.prepareStatement(sql);
											
											pstmt.setString(1, indNo);
											pstmt.setString(2, purcOrder);
											pstmt.setString(3, lineNo1);
											updCnt = pstmt.executeUpdate();
											System.out.println("porder status updated@@"+updCnt);
											//rs.close();
										//	rs = null;
											pstmt.close();
											pstmt = null;
											//dlv_qty-ord_qty
											
											if(indOrdQty==quantityStduom)
											{
												sql = " update indent set status = 'L',ord_qty="+indOrdQty+" ,status_date=?  where  ind_no = ? ";
												
											}else
											{
												
												sql = " update indent set status = 'O' ,ord_qty="+indOrdQty+" ,status_date=?  where  ind_no = ? ";
											}
											pstmt = conn.prepareStatement(sql);
											
											pstmt.setTimestamp(1, sysDate1);
											pstmt.setString(2, indNo);
											updCnt = pstmt.executeUpdate();
											System.out.println("Indent status updated@@"+updCnt);
										//	rs.close();
										//	rs = null;
											pstmt.close();
											pstmt = null;
											}else
											{
												System.out.println("dlvQty@@At before" + dlvQty);
												System.out.println("poDetTotalQty@@At before" + poDetTotalQty);
												 totalOrdqty = poDetTotalQty - dlvQty;
												System.out.println("totalOrdqty@@[poqty-dlvqty:::]"+totalOrdqty);
												convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, totalOrdqty, conn)	;
												System.out.println("convIndOrdQty"+convIndOrdQty);
												 indOrdQty=orderQty+convIndOrdQty;
												System.out.println("indOrdQty@@[orderQty+totalOrdqty]::"+indOrdQty);
												System.out.println("comming into dlvQty==0 condition@@");
												int updCnt=0;
												//double ordQty=0.0;
												
												/*convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, indOrdQty, conn)	;
												System.out.println("convIndOrdQty"+convIndOrdQty);
												*/
												sql = " update porddet set status = 'O' where  ind_no = ? and purc_order=? and line_no=? ";
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, indNo);
												pstmt.setString(2, purcOrder);
												pstmt.setString(3, lineNo1);
												updCnt = pstmt.executeUpdate();
												System.out.println("porder status updated@@"+updCnt);
												pstmt.close();
												pstmt = null;
												//ordQty=ordQty+quantity;
												if(indOrdQty==quantityStduom)
												{
													System.out
															.println("dlvQty==0 condition@(orderQty==indQty)"+orderQty);
													sql = " update indent set status = 'L',ord_qty="+indOrdQty+" where  ind_no = ? ";
													
												}else
												{
												System.out.println("updating poDetTotalQty condition@@"+poDetTotalQty);
												sql = " update indent set status = 'O' ,ord_qty="+indOrdQty+" where  ind_no = ? ";
												}
												pstmt = conn.prepareStatement(sql);
												
												pstmt.setString(1, indNo);
												updCnt = pstmt.executeUpdate();
												System.out.println("Indent status and ordQty updated@@"+updCnt);
												pstmt.close();
												pstmt = null;
												
											}
										}
								}

								
								}
								//}
								
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								
								try {
									
									
									errString1 = itmDBAccessEJB.getErrorString(" ", "VMPOSUCC", userId, "",conn);
									return errString1;
									
								} catch (Exception e) {
									e.printStackTrace();
									if(errString1==null)
									{System.out.println("got errString exception"+e.getMessage());
										conn.rollback();
										conn.close();
									}
									// TODO: handle exception
								}finally
								{
									if(errString1!=null || errString1.trim().length()>0||errString1.indexOf("VMPOSUCC")>-1)
									{
										System.out.println("Connection commiting@dj");
										conn.commit();
										conn.close();
										
									}
								}
								
								
								
								
							}
						 catch (Exception e) {
							e.printStackTrace();
							System.out.println("Exception after confirmed@@"+e.getMessage());
							errString = e.getMessage();
							throw new ITMException(e);
							
						}
							
						}else
						{
							
							System.out.println("ErrorString not null");
							conn.rollback();
							conn.close();
						}
						
						
						System.out.println("After confiremed error String-->"+errString);
						return errString;
							
					}
					
					return retString;
		}
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception d)
			{
				System.out.println("Exception : PorderClosePrcEJB =>"+d.toString());
				d.printStackTrace();
			System.out.println("Exception :PorderClosePrcEJB :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
			}
		}
		finally
		{
			System.out.println("errorString...." + errString);
		 	System.out.println("Closing Connection....");
			try
			{
				if(conn != null && !conn.isClosed())
				{
					//conn.rollback();
					conn.commit();
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
		System.out.println("Error Message=>"+errString);
		return errString;
	}//END OF PROCESS(2)
	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input;
	}
	

	/*public String getUserInfo()throws ITMException{
		String xtraParams="";
		StringBuffer userInfoStr = new StringBuffer();

		String userId = "", loginEmpCode = "", loginSiteCode = "", entityCode = "", profileId = "",
		userType = "", chgTerm = "";
		try{

		E12GenericUtility genericUtility = new E12GenericUtility();

		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
		loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		entityCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"entityCode");
		profileId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"profileId");
		userType = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userType");
		chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");

		System.out.println("xtraParams  is @@@@@ " + xtraParams);

		userInfoStr.append("<UserInfo>");
		userInfoStr.append("<loginCode>").append("<![CDATA["+userId+"]]>").append("</loginCode>\r\n");
		userInfoStr.append("<empCode>").append("<![CDATA["+loginEmpCode+"]]>").append("</empCode>\r\n");
		userInfoStr.append("<siteCode>").append("<![CDATA["+loginSiteCode+"]]>").append("</siteCode>\r\n");
		userInfoStr.append("<entityCode>").append("<![CDATA["+entityCode+"]]>").append("</entityCode>\r\n");
		userInfoStr.append("<profileId>").append("<![CDATA["+profileId+"]]>").append("</profileId>\r\n");
		userInfoStr.append("<userType>").append("<![CDATA["+userType+"]]>").append("</userType>\r\n");
		userInfoStr.append("<remoteHost>").append("<![CDATA["+chgTerm+"]]>").append("</remoteHost>\r\n");
		userInfoStr.append("</UserInfo>");
		}
		catch (Exception e){

		throw new ITMException(e);
		}
		return userInfoStr.toString();
		}
	*/
	
	private String saveData(String siteCode,String xmlString,String userId1, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = userId1;
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			//UserInfoBean userInfo = getUserInfo();
			//String userInfo=getUserInfo();
			
			//System.out.println("userInfo@at masterSate@"+userInfo.toString());
			
			//retString = masterStateful.processRequest(userInfo, xmlString, true, conn);
		//	System.out.println("--retString - -" + retString);
			
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
			System.out.println("--retString - -"+retString);
		}
		catch(ITMException itme)
		{
			System.out.println("ITMException :porderOpenPrc :saveData :==>");
			throw itme;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :porderOpenPrc :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}
	
	
}
