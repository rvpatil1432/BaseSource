package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.adv.CustStockGWTConf;
import ibase.webitm.utility.ITMException;

@Stateless
public class SecSalesSubPrc extends ProcessEJB implements SecSalesSubPrcLocal , SecSalesSubPrcRemote {

	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
	@Override
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException {
		String rtStr = "";
		Document dom = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			rtStr = getData(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::"+this.getClass().getSimpleName()+"::getDataString" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return rtStr;
	}
	
	
	@Override
	public String getData(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException {
		
		String errString = "";
		String sql = "" , itemSer = "" , prdCode = "" , custCode = "" , custName = "" , posCode = "" , empCode = "" , tranId = "" , 
				empName = "" , positionDescr = "" , frdatStr = ""  , todatStr = "" , stmtdatStr = "";
		StringBuffer retTabSepStrBuff = new StringBuffer();
		PreparedStatement pstmt = null , pstmt1 = null;
		ResultSet rs = null , rs1 = null;
		Connection conn = null;
		Timestamp frData = null , toData =  null , stmtDate = null;
		int cnt=0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDispDateFormat());
			//ConnDriver con = new ConnDriver();
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection();
			
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
			System.out.println("itemSer ::::::::"+itemSer + " prdCode :::::::: " +prdCode);
			
			retTabSepStrBuff.append("<?xml version=\"1.0\"?>\r\n<DocumentRoot>\r\n<description>Datawindow Root</description>\r\n<group0>\r\n<description>Group0 description</description>\r\n<Header0>\r\n<description>Header0 members</description>\r\n");
			if(itemSer != null && prdCode != null)
			{
				sql = "SELECT CUST_CODE , FROM_DATE , TO_DATE , ITEM_SER , STMT_DATE , POS_CODE , EMP_CODE , TRAN_ID FROM CUST_STOCK WHERE ITEM_SER = ? AND PRD_CODE = ? AND POS_CODE IS NOT NULL AND  CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END = 'N' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemSer);
				pstmt.setString(2, prdCode);
				rs = pstmt.executeQuery();
			
			   while(rs.next()) 
			   {
				custCode = rs.getString("CUST_CODE");
				frData = rs.getTimestamp("FROM_DATE");
				toData = rs.getTimestamp("TO_DATE");
				itemSer = checkNull(rs.getString("ITEM_SER"));
				stmtDate = rs.getTimestamp("STMT_DATE");
				posCode = rs.getString("POS_CODE");
				empCode = rs.getString("EMP_CODE");
				tranId = checkNull(rs.getString("TRAN_ID"));
				cnt++;
				
				System.out.println("CUST_CODE::::::::"+custCode);
				System.out.println("frData:::::::::::::"+frData);
				System.out.println("toData::::::::"+toData);
				System.out.println("stmtDate::::::::"+stmtDate);
				System.out.println("empCode::::::::::"+empCode);
				System.out.println("tranId::::::::::"+tranId);
				if( custCode != null && custCode.length() > 0 )
				{
					sql = "SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, custCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						custName = checkNull(rs1.getString("CUST_NAME"));
						System.out.println("custName is :" + custName);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
			
				if( posCode != null && posCode.length() > 0 )
				{
				sql = "select FN_GET_POSCODE_DESCR(?) as descr from dual ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, rs.getString("POS_CODE"));
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					positionDescr = checkNull(rs1.getString("descr"));
					System.out.println("descr :::"+positionDescr);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				}
				
				if( empCode != null && empCode.length() > 0 )
				{
					sql = "select emp_fname||' '||emp_mname||' '||emp_lname as name from employee where emp_code=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, empCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						empName = checkNull(rs1.getString("name"));
						System.out.println("empName  :" + empName);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
				
				frdatStr = checkNull(genericUtility.getValidDateString(frData.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
				todatStr = checkNull(genericUtility.getValidDateString(toData.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
				stmtdatStr = checkNull(genericUtility.getValidDateString(stmtDate.toString(), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
				
				retTabSepStrBuff.append("<Detail domID='"+cnt+"' >\r\n");
				retTabSepStrBuff.append("<attribute updateFlag='N' selected='N' />\r\n");
				retTabSepStrBuff.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>\r\n");
				retTabSepStrBuff.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>\r\n");
				retTabSepStrBuff.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>\r\n");
				retTabSepStrBuff.append("<pos_code>").append("<![CDATA["+posCode+"]]>").append("</pos_code>\r\n");
				retTabSepStrBuff.append("<position_descr>").append("<![CDATA["+positionDescr+"]]>").append("</position_descr>\r\n");
				retTabSepStrBuff.append("<from_date>").append("<![CDATA["+frdatStr+"]]>").append("</from_date>\r\n");
				retTabSepStrBuff.append("<to_date>").append("<![CDATA["+todatStr+"]]>").append("</to_date>\r\n");
				retTabSepStrBuff.append("<stmt_date>").append("<![CDATA["+stmtdatStr+"]]>").append("</stmt_date>\r\n");
				retTabSepStrBuff.append("<emp_code>").append("<![CDATA["+empCode+"]]>").append("</emp_code>\r\n");
				retTabSepStrBuff.append("<emp_name>").append("<![CDATA["+empName+"]]>").append("</emp_name>\r\n");
				retTabSepStrBuff.append("<tran_id>").append("<![CDATA["+tranId+"]]>").append("</tran_id>\r\n");
				retTabSepStrBuff.append("</Detail>\r\n");
			}
			}
			retTabSepStrBuff.append("</Header0>\r\n");
			retTabSepStrBuff.append("</group0>\r\n");
			retTabSepStrBuff.append("</DocumentRoot>\r\n");
			errString = retTabSepStrBuff.toString();

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(":::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			throw new ITMException(e);
		} finally {
			try {
				conn.close();
				conn = null;
			} catch (Exception e) {
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	
	@Override
	public String process(String xmlString, String xmlString2,String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtStr = "";
		Document dom = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
				System.out.println("Process Dom::::::::::::::::"+dom );
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
				System.out.println("Process Dom2::::::::::::::::"+dom2 );
			}
			rtStr = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::"+this.getClass().getSimpleName()+"::processDataString" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return rtStr;
	}
@Override
public String process(Document dom, Document dom2, String windowName,String xtraParams) throws RemoteException, ITMException {
	
	String errString = "", userID = "", prdCode = "" , itemSer = "" ,  tranId = "" , loginSiteCode = "" , forcedFlag = "" , result = "";
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	Connection conn=null;
	int ctr = 0, childNodeListLength = 0, parentNodeListLength = 0;
	String childNodeName = "";
	boolean val=false,num=false;
	String chg_term="",chg_user="";
	java.sql.Date valid_upto = null;
	int cnt=0;
	try {
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		System.out.println("In process Secondary Sales :::");
		chg_term = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		chg_user = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDispDateFormat());
		CustStockGWTConf  custStockGWTConf = new CustStockGWTConf();
		parentNodeList = dom2.getElementsByTagName("Detail2");
		parentNodeListLength = parentNodeList.getLength();
		if(parentNodeListLength == 0)
		{
			errString = itmDBAccessEJB.getErrorString("","VPSELONERD","","",conn); 
			return errString;
		}
		System.out.println("::::::parentNodeListLength["+parentNodeListLength+"]");
		for (int i = 0; i < parentNodeListLength; i++) 
		{
			parentNode = parentNodeList.item(i);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();	
			System.out.println("childNodeListLength : "+childNodeListLength+" childNodeList : "+childNodeList);
			for (int childRow = 0; childRow < childNodeListLength; childRow++)
			{
				childNode = childNodeList.item(childRow);
				childNodeName = childNode.getNodeName();
				System.out.println("childNodeList.item(childRow) : "+ childNode);
				System.out.println("childNode Name : "+childNode.getNodeName()+" value::"+childNode.getNodeValue());
				if("prd_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
				{
					prdCode = checkNull(childNode.getFirstChild().getNodeValue());
					System.out.println("prdCode :::::::"+prdCode);
				}
				else if("item_ser".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
				{
					itemSer = checkNull(childNode.getFirstChild().getNodeValue());
					System.out.println("itemSer :::::::"+itemSer);
				}
				else if("tran_id".equalsIgnoreCase(childNodeName) )
				{
					tranId = checkNull(childNode.getFirstChild().getNodeValue());
					System.out.println("tranId :::::::"+tranId);
				}
				
			}
			if(tranId!=null && tranId.trim().length()>0)
			{
				result = custStockGWTConf.submit(tranId, xtraParams, forcedFlag);
				if (result.toUpperCase().indexOf("VTSUBM1") > -1)
				{
					cnt++;
				}
			}
		}
		if(parentNodeListLength==cnt)
		{
			errString = itmDBAccessEJB.getErrorString("", "PROCSUCFUL", "","", conn);
		}
		else if(parentNodeListLength!=0 && cnt>0 && parentNodeListLength>cnt)
		{
			errString = itmDBAccessEJB.getErrorString("","TRNPARSUCC","","",conn); 
			return errString;
		}
		else if(cnt==0)
		{
			errString = itmDBAccessEJB.getErrorString("", "VPINVPRCFL", "","", conn);
		}
		
	}
	catch (Exception e) {
		System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
		e.printStackTrace();
		errString = itmDBAccessEJB.getErrorString("", "VPINVPRCFL", "","", conn);
		throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
	}
	return errString;
}
}

