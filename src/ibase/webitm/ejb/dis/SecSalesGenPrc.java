package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterDataStatefulLocal;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.adv.CustStockGWTConf;
import ibase.webitm.utility.ITMException;

import java.io.File;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Saurabh Jarande[24/03/17]
 * This component is used to create Secondary sales transactions by process after Period closed.
 *
 */
@Stateless
public class SecSalesGenPrc extends ProcessEJB implements SecSalesGenPrcLocal,SecSalesGenPrcRemote 
{
	E12GenericUtility genericUtility = new E12GenericUtility();

	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{
		String rtStr = "";
		Document dom = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString2);
			}
			rtStr = getData(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::"+this.getClass().getSimpleName()+"::getDataString" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return rtStr;
	}

	@Override
	public String getData(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		String sql = "";
		StringBuffer retTabSepStrBuff = new StringBuffer();
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		Connection conn = null;
		String custCode="",custName="",prdCode="",sysDate="",itemSer="",countryCode="",empCode="",empName="",posCode="",blacklisted="",posCodeDescr="";
		Timestamp frDate=null,toDate=null;
		SimpleDateFormat sdf=null;
		String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		try {
			//ConnDriver con = new ConnDriver();
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("In getdata Station update process:::");
			prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			sysDate = sdf.format(Calendar.getInstance().getTime());
			retTabSepStrBuff.append("<?xml version=\"1.0\"?>\r\n<DocumentRoot>\r\n<description>Datawindow Root</description>\r\n<group0>\r\n<description>Group0 description</description>\r\n<Header0>\r\n<description>Header0 members</description>\r\n");
			
			sql= "select count_code from state where " +
					"state_code in (select state_code from site where site_code=?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginSiteCode );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				countryCode = checkNull(rs.getString("count_code")).trim();
				System.out.println("countryCode >>> :"+countryCode);
			}
			callPstRs(pstmt, rs);
			
			sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
					" from period_appl a,period_tbl b " +
					"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
					" AND b.prd_code = ? " +
					"and b.prd_tblno=? " +
					"AND case when a.type is null then 'X' else a.type end='S' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,prdCode.trim());
			pstmt.setString(2,countryCode+"_"+itemSer.trim());	
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				frDate=rs.getTimestamp("FR_DATE");
				toDate=rs.getTimestamp("TO_DATE");
			}
			callPstRs(pstmt, rs);
			
			/*sql = " SELECT POS_CODE,CUST_CODE,EMP_CODE FROM " +
					" (SELECT C.POS_CODE,C.CUST_CODE,A.EMP_CODE FROM ORG_STRUCTURE A ,ORG_STRUCTURE_CUST C " +
					" WHERE A.VERSION_ID=C.VERSION_ID AND A.TABLE_NO=C.TABLE_NO AND A.POS_CODE=C.POS_CODE  " +
					" AND C.VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) AND C.TABLE_NO= ? " +
					" AND C.EFF_DATE < ?  AND C.VALID_UPTO > ? AND CASE WHEN C.SOURCE IS NULL THEN 'Y' ELSE C.SOURCE END <> 'A' " +
					" MINUS " +
					" SELECT POS_CODE,CUST_CODE,EMP_CODE FROM CUST_STOCK WHERE PRD_CODE = ? AND POS_CODE IS NOT NULL AND ITEM_SER = ? ) ";
			*/
			sql=" SELECT A.POS_CODE,A.CUST_CODE,A.EMP_CODE FROM " +
				" (SELECT ROW_NUMBER() OVER (PARTITION BY C.CUST_CODE ORDER BY C.CUST_CODE) RN,C.POS_CODE,C.CUST_CODE,A.EMP_CODE " +
				" FROM ORG_STRUCTURE A INNER JOIN ORG_STRUCTURE_CUST C " +
				" ON A.VERSION_ID=C.VERSION_ID AND A.TABLE_NO=C.TABLE_NO AND A.POS_CODE=C.POS_CODE " +
				" INNER JOIN " +
				" (SELECT D.CUST_CODE FROM ORG_STRUCTURE B ,ORG_STRUCTURE_CUST D " +
				" WHERE B.VERSION_ID=D.VERSION_ID AND B.TABLE_NO=D.TABLE_NO AND B.POS_CODE=D.POS_CODE " +
				" AND D.VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) AND D.TABLE_NO= ? " +
				" AND D.EFF_DATE <= ?  AND D.VALID_UPTO >= ? AND CASE WHEN D.SOURCE IS NULL THEN 'Y' ELSE D.SOURCE END <> 'A' " +
				" MINUS " +
				" SELECT CUST_CODE FROM CUST_STOCK WHERE PRD_CODE = ? AND POS_CODE IS NOT NULL AND ITEM_SER = ? " +
				" ) B " +
				" ON C.CUST_CODE = B.CUST_CODE WHERE C.VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) AND C.TABLE_NO= ? " +
				" ) A WHERE A.RN = 1 ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemSer);
				pstmt.setTimestamp(2, frDate);
				pstmt.setTimestamp(3, toDate);
				pstmt.setString(4, prdCode);
				pstmt.setString(5, itemSer);
				pstmt.setString(6, itemSer);
				rs = pstmt.executeQuery();
				while(rs.next()) {
				posCode = checkNull(rs.getString("POS_CODE"));
				custCode = checkNull(rs.getString("CUST_CODE"));
				empCode = checkNull(rs.getString("EMP_CODE"));
	
				custName="";blacklisted="";empName="";posCodeDescr="";
				
				sql="SELECT CUST_NAME,BLACK_LISTED FROM CUSTOMER WHERE CUST_CODE=? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next()){
					custName = checkNull(rs1.getString("CUST_NAME"));
					blacklisted = checkNull(rs1.getString("BLACK_LISTED"));
				}
				callPstRs(pstmt1, rs1);
				
				sql="SELECT EMP_FNAME||' '||EMP_LNAME||' '||EMP_LNAME AS EMP_NAME FROM EMPLOYEE WHERE EMP_CODE=? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, empCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next()){
					empName = checkNull(rs1.getString("EMP_NAME"));
				}
				callPstRs(pstmt1, rs1);
				
				sql="SELECT DESCR FROM ORG_STRUCTURE WHERE POS_CODE=? AND TABLE_NO=? AND VERSION_ID=(SELECT FN_GET_VERSION_ID FROM DUAL) ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, posCode);
				pstmt1.setString(2, itemSer);
				rs1 = pstmt1.executeQuery();
				if(rs1.next()){
					posCodeDescr = checkNull(rs1.getString("DESCR"));
				}
				callPstRs(pstmt1, rs1);
				
				retTabSepStrBuff.append("<Detail2>\r\n");
				retTabSepStrBuff.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>\r\n");
				retTabSepStrBuff.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>\r\n");
				retTabSepStrBuff.append("<blacklisted>").append("<![CDATA["+blacklisted+"]]>").append("</blacklisted>\r\n");
				retTabSepStrBuff.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>\r\n");
				retTabSepStrBuff.append("<pos_code>").append("<![CDATA["+posCode+"]]>").append("</pos_code>\r\n");
				retTabSepStrBuff.append("<pos_code_descr>").append("<![CDATA["+posCodeDescr+"]]>").append("</pos_code_descr>\r\n");
				retTabSepStrBuff.append("<from_date>").append("<![CDATA["+sdf.format(frDate)+"]]>").append("</from_date>\r\n");
				retTabSepStrBuff.append("<to_date>").append("<![CDATA["+sdf.format(toDate)+"]]>").append("</to_date>\r\n");
				retTabSepStrBuff.append("<stmt_date>").append("<![CDATA["+sysDate+"]]>").append("</stmt_date>\r\n");
				retTabSepStrBuff.append("<emp_code>").append("<![CDATA["+empCode+"]]>").append("</emp_code>\r\n");
				retTabSepStrBuff.append("<emp_name>").append("<![CDATA["+empName+"]]>").append("</emp_name>\r\n");
				retTabSepStrBuff.append("<prd_code>").append("<![CDATA["+prdCode+"]]>").append("</prd_code>\r\n");
				retTabSepStrBuff.append("</Detail2>\r\n");
			}
			callPstRs(pstmt, rs);

			retTabSepStrBuff.append("</Header0>\r\n");
			retTabSepStrBuff.append("</group0>\r\n");
			retTabSepStrBuff.append("</DocumentRoot>\r\n");
			errString = retTabSepStrBuff.toString();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println(":::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			throw new ITMException(e);
		}
		finally 
		{
			try 
			{
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch (Exception e) 
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	
	public String process(String xmlString, String xmlString2,String windowName, String xtraParams) throws RemoteException,ITMException 
	{
		Document detailDom = null;
		Document headerDom = null;
		String retStr = "";
		System.out.println("windowName[process]::::::::::;;;" + windowName);
		System.out.println("xtraParams[process]:::::::::;;;" + xtraParams);
		try 
		{
			System.out.println("xmlString[process]::::::::::;;;" + xmlString);
			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				headerDom = genericUtility.parseString(xmlString);
				System.out.println("headerDom" + headerDom);
			}
			System.out.println("xmlString2[process]::::::::::;;;" + xmlString2);
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				detailDom = genericUtility.parseString(xmlString2);
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e) 
		{
			System.out.println(":::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}// END OF PROCESS (1)
	
	public String process(Document headerDom, Document detailDom,String windowName, String xtraParams) throws RemoteException,ITMException 
	{
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Connection conn = null;
		int parentNodeListLength = 0, childNodeListLength = 0;
		String childNodeName = "";
		NodeList parentNodeList = null,childNodeList = null;
		Node parentNode = null, childNode = null;
		boolean result=false;
		int custCount=0;
		String errString="",itemSer="",prdCode="",custCode="",posCode="",empCode="",
				fromDateStr="",toDateStr="",stmtDateStr="";
		String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		//Set UserINfo
		UserInfoBean userInfo = new UserInfoBean();
		userInfo.setLoginCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
		userInfo.setEmpCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
		userInfo.setSiteCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
		userInfo.setEntityCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "entityCode"));
		userInfo.setProfileId(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "profileId"));
		userInfo.setUserType(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userType"));
		userInfo.setRemoteHost(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
		
		try 
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
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
				
					if("cust_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						custCode=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("item_ser".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						itemSer=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("pos_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						posCode=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("from_date".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						fromDateStr=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("to_date".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						toDateStr=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("stmt_date".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						stmtDateStr=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("emp_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						empCode=checkNull(childNode.getFirstChild().getNodeValue());
					}
					if("prd_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						prdCode=checkNull(childNode.getFirstChild().getNodeValue());
					}
				}
				
				custCount=isCustExist(prdCode,custCode,itemSer,conn);
				
				if(custCount==0)
				{
					result = pendTranGenProcess(xtraParams,loginSiteCode,chgUser,chgTerm,prdCode,custCode,itemSer,posCode,fromDateStr,toDateStr,stmtDateStr,empCode,userInfo,conn);
					System.out.println("result>>"+result);
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("", "VMINVPRDCU", "","", conn);
					return errString;
				}
				
			}
			System.out.println("result>>"+result);
			
			if(result) 
			{
				errString = itmDBAccessEJB.getErrorString("", "VTES3GENS", "","", conn);
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("", "VTES3GENF", "","", conn);
			}
			
		}// try end
		catch (Exception e) 
		{
			try 
			{
				System.out.println("inside");
				errString = itmDBAccessEJB.getErrorString("", "VTES3GENF","", "", conn);
				conn.rollback();
			}
			catch (Exception d) 
			{
				System.out.println("Exception : SecSalesGenPrc =>"+ d.toString());
				d.printStackTrace();
			}
			e.printStackTrace();
			System.out.println(":::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			throw new ITMException(e);
		}
		finally 
		{
			System.out.println("In finally....");
			try 
			{
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e) 
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString;
			}
		}
		System.out.println("Error Message=>" + errString);
		return errString;
	}// END OF PROCESS(2)

	private int isCustExist(String prdCode, String custCode, String itemSer,Connection conn) throws ITMException 
	{
		// TODO Auto-generated method stub
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		int custCntr=0;
		try
		{
			sql=" select count(*) as count from cust_stock where cust_code=? and item_ser=? and prd_code=? and pos_code is not null ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, itemSer);
			pstmt.setString(3, prdCode);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				custCntr = rs.getInt("count");
			}
			callPstRs(pstmt, rs);
			System.out.println("custCntr>>>>>"+custCntr);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.out.println("custCnt SQLException"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try 
			{
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return custCntr;
	}

	private boolean pendTranGenProcess(String xtraParams,String loginSiteCode,String chgUser,String chgTerm,String prdCode,String custCode,String itemSer, String posCode,String fromDateStr, String toDateStr, String stmtDateStr,String empCode,UserInfoBean userInfo,Connection conn) throws ITMException 
	{
		boolean result=false;
		CustStockGWTIC custStockGWTIC =new CustStockGWTIC();
		CustStockGWTConf confTran=new CustStockGWTConf();
		ArrayList<String>logList=null;
		String xmlInEditMode="",xmlInEditMode2="",xmlInEditMode3="",sql="",orderType="",custType="",tranIdLast="",tranId="",
				sysDate="",logDate="",countryCode="",xmlDetail2="",xmlParseStr="",retString="",retString1="",errString="",
				custStockItemDetails="",custStockInvDetails="";
		StringBuffer xmlBuff=null;
		SimpleDateFormat sdf=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			logDate= sdf.format(Calendar.getInstance().getTime());
			sysDate = sdf.format(Calendar.getInstance().getTime());
			logList=new ArrayList<String>();
			xmlInEditMode = getHeaderXML(userInfo,"1","2");
			xmlInEditMode2 = getHeaderXML(userInfo,"2","1");
			xmlInEditMode3 = getHeaderXML(userInfo,"3","1");
			System.out.println("xmlInEditMode:::"+ xmlInEditMode);
			System.out.println("xmlInEditMode2>>>>"+xmlInEditMode2);
			System.out.println("xmlInEditMode3>>>>"+xmlInEditMode3);
			StringBuffer xmlDetail1 = new StringBuffer();
	
			sql= "select count_code from state where " +
					"state_code in (select state_code from site where site_code=?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginSiteCode );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				countryCode = checkNull(rs.getString("count_code")).trim();
				System.out.println("countryCode >>> :"+countryCode);
			}
			callPstRs(pstmt, rs);
			
			sql= " select order_type,cust_type from customer where cust_code=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				orderType=checkNull(rs.getString("order_type"));
				custType=checkNull(rs.getString("cust_type"));
			}
			callPstRs(pstmt, rs);
			
			tranIdLast=getTranIdLast(orderType, itemSer, custCode,conn);
			System.out.println("tranIdLast>>>"+tranIdLast+">>orderType>>>"+orderType+"custType>>>"+custType);
			Document detailDom1 = genericUtility.parseString(xmlInEditMode);
			NodeList parentNodeList1 = detailDom1.getElementsByTagName("Detail1");
			Node parentNode1 = parentNodeList1.item(0);
			NodeList childNodeList1 = parentNode1.getChildNodes();
			int childNodeListLength1 = childNodeList1.getLength();
			for (int ctr = 0; ctr < childNodeListLength1; ctr++) 
			{
				Node childNode1 = childNodeList1.item(ctr);
				String childNodeName1 = childNode1.getNodeName().trim();
				
				if ("tran_id".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(tranId);
				} else if ("tran_date".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(sysDate);
				} else if ("cust_code".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(custCode);
				} else if ("item_ser".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(itemSer);
				} else if ("order_type".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(orderType);
				} else if ("from_date".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(fromDateStr);
				} else if ("to_date".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(toDateStr);
				} else if ("site_code".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(loginSiteCode);
				} else if ("tran_id__last".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(tranIdLast);
				} else if ("stmt_date".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(sysDate);
				} else if ("confirmed".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent("N");
				} else if ("status".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent("O");
				} else if ("cust_type".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(custType);
				} else if ("prd_code".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(prdCode);
				} else if ("missing_inserted".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent("Y");
				} else if ("adm_chk".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent("N");
				} else if ("login_poscode".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(posCode);
				} else if ("pos_code".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(posCode);
				} else if ("emp_code".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(empCode);
				} else if ("country_code".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(countryCode);
				}  else if ("edit_status".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent("A");
				} else if ("sale_per".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(empCode);
				} else if ("chg_user".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(chgUser);
				} else if ("chg_term".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(chgTerm);
				} else if ("chg_date".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(sysDate);
				} else if ("add_user".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(chgUser);
				} else if ("add_date".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(sysDate);
				} else if ("add_term".equalsIgnoreCase(childNodeName1)) {
					childNode1.setTextContent(chgTerm);
				} 
			}//for loop end
			xmlDetail1 = xmlDetail1.append(genericUtility.serializeDom(detailDom1));							
			//header details end
			System.out.println("xmlDetail1 final>>>>"+xmlDetail1.toString());
			custStockInvDetails=custStockGWTIC.itemChanged("", xmlDetail1.toString(), xmlDetail1.toString(), "2", "itm_default", "A", xtraParams);
			System.out.println("custStockInvDetails>>>>"+custStockInvDetails);
			if(custStockInvDetails.contains("Detail2"))
			{
				xmlDetail2=custStockInvDetails.substring(custStockInvDetails.indexOf("<Detail2"), custStockInvDetails.lastIndexOf("</Detail2>")+10);
				System.out.println("xmlDetail2>>>"+xmlDetail2);
				
				xmlBuff = new StringBuffer();
				xmlBuff.append(xmlDetail1.substring(0,xmlDetail1.indexOf("</Header0>")));
				xmlBuff.append(xmlDetail2);
				xmlBuff.append(xmlDetail1.substring(xmlDetail1.indexOf("</Header0>")));
				xmlParseStr = xmlBuff.toString();
				xmlBuff = null;
				System.out.println(":::xmlParseStr::with Invoice:" + xmlParseStr);
				custStockItemDetails=custStockGWTIC.itemChanged(xmlInEditMode3, xmlParseStr, xmlParseStr, "3", "itm_default", "A", xtraParams,"sec_sale_gen_prc");
			}
			else
			{
				//xmlDetail2=xmlInEditMode2.substring(xmlInEditMode2.indexOf("<Detail2"), xmlInEditMode2.lastIndexOf("</Detail2>")+10);
				//System.out.println("xmlDetail2>>>>>"+xmlDetail2);
				//xmlBuff = new StringBuffer();
				//xmlBuff.append(xmlDetail1.substring(0,xmlDetail1.indexOf("</Header0>")));
				//xmlBuff.append(xmlDetail2);
				//xmlBuff.append(xmlDetail1.substring(xmlDetail1.indexOf("</Header0>")));
				xmlParseStr = xmlDetail1.toString();
				//xmlBuff = null;
				System.out.println(":::xmlParseStr::without Invoice:" + xmlParseStr);
				custStockItemDetails=custStockGWTIC.itemChanged(xmlInEditMode3, xmlParseStr, xmlParseStr, "3", "itm_default", "A", xtraParams,"sec_sale_gen_prc");
			}
				//String custStockItemDetails=custStockGWTIC.itemChanged(xmlInEditMode3, xmlParseStr, xmlParseStr, "3", "itm_default", "A", xtraParams,"sec_sale_gen_prc");
				System.out.println("custStockItemDetails>>>>>"+custStockItemDetails);
				
				String xmlDetail3=custStockItemDetails.substring(custStockItemDetails.indexOf("<Detail3"), custStockItemDetails.lastIndexOf("</Detail3>")+10);
				System.out.println("xmlDetail3>>>>"+xmlDetail3);
				
				xmlBuff = new StringBuffer();
				xmlBuff.append(xmlParseStr.substring(0,xmlParseStr.indexOf("<Header0>") + 9));
				xmlBuff.append("<objName><![CDATA[").append("secondory_sale_gwt_wiz_dummy").append("]]></objName>");
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
				xmlBuff.append("<taxInFocus><![CDATA[").append(true).append("]]></taxInFocus>");
				xmlBuff.append(xmlParseStr.substring(xmlParseStr.indexOf("<Header0>") + 9,xmlParseStr.indexOf("</Header0>")));
				xmlBuff.append(xmlDetail3);
				xmlBuff.append(xmlParseStr.substring(xmlParseStr.indexOf("</Header0>")));

				String xmlParseStrFinal = xmlBuff.toString();
				xmlBuff = null;
				System.out.println("xmlParseStrFinal>>>>"+xmlParseStrFinal);
				retString=saveData(xmlParseStrFinal, conn, userInfo);
				System.out.println("retString>>>>"+retString);
				
				if (retString.toUpperCase().indexOf("SUCCESS") > -1)
				{
					conn.commit();
					String[] arrayForTranId = retString.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					String newTranIdGen = arrayForTranId[1].substring(0, endIndex);
				
					if(newTranIdGen!=null && newTranIdGen.trim().length()>0)
					{
						retString1=confTran.submit(newTranIdGen, xtraParams, "");
						System.out.println("retString1>>>"+retString1);
						if (retString1.toUpperCase().indexOf("VTSUBM1") > -1)
						{
							errString = "Confirmed Transaction "+newTranIdGen+" Created for Customer code >>"+custCode+" of Position code >>"+posCode+" and Employee code >>"+empCode;
							logList.add(errString);
							errString=null;
							result=true;
						}
						else
						{
							result=false;
						}
					}
				}
				else 
				{
					String description = "";
					Document parseString = genericUtility.parseString(retString);
					NodeList nlErrorTag = null;
					nlErrorTag = parseString.getElementsByTagName("error");
					if (nlErrorTag.getLength() <= 0) 
					{
						nlErrorTag = parseString.getElementsByTagName("Error");
					}
					for (int err = 0; err < nlErrorTag.getLength(); err++)
					{
						Node itemNode = nlErrorTag.item(err);
						NamedNodeMap errorAttributes = itemNode.getAttributes();
						Node errorTypeNode = errorAttributes.getNamedItem("type");
						Node errorIdNode = errorAttributes.getNamedItem("type");
						String errorType = errorTypeNode.getTextContent();
						String errorId = errorIdNode.getTextContent();
						NodeList childNodeListErr = itemNode.getChildNodes();
						for (int k = 0; k < childNodeListErr.getLength(); k++) 
						{
							Node childNodeErr = childNodeListErr.item(k);
							if ("description".equalsIgnoreCase(childNodeErr.getNodeName())) 
							{
								description = childNodeErr.getFirstChild().getNodeValue();
							}
						}

						if ("W".equals(errorType)) {
							errString = "Warnings: " + errorId + " : " + description;
						}
						else 
						{
							errString = "Errors: " + errorId + " : " + description;
						}
						logList.add(errString);
					}
				}
			System.out.println("result>>>"+result);
		}
		catch(Exception e)
		{
			result=false;
			logList.add(e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try 
			{
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		writeLog(this.getClass().getSimpleName()+"_"+itemSer+"_"+prdCode, logList,logDate);
		return result;
	}

	private String getHeaderXML(UserInfoBean userInfo,String formNo,String pagContext) throws Exception 
	{
		InitialContext ctx = null;
		String retString = "";
		MasterDataStatefulLocal masterStateful = null;
		AppConnectParm appConnect = new AppConnectParm();
		try{
		ctx = new InitialContext(appConnect.getProperty());
		masterStateful = (MasterDataStatefulLocal) ctx.lookup("ibase/MasterDataStatefulEJB/local");
		retString=masterStateful.getBlankDomForAdd("secondory_sale_gwt_wiz", formNo, pagContext, null, userInfo.toString(), "");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return retString;
	}
	
	private String saveData(String xmlString, Connection conn,UserInfoBean userInfo) throws Exception 
	{
		String retString = "";
		InitialContext ctx = null;
		MasterStatefulLocal masterStateful = null;
		try 
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			String[] authencate = new String[2];
			authencate[0] = "";
			authencate[1] = "";
			System.out.println("xmlString:::::" + xmlString);
			retString = masterStateful.processRequest(userInfo, xmlString,true, conn);
			System.out.println("ProcessRequest::::::" + retString);
		}
		catch (Exception e) 
		{
			System.out.println("Exception: EJBName ["+ getClass().getSimpleName() + "] -method [saveData]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	
	private String getTranIdLast(String orderType, String itemSer,String custCode,Connection conn) throws ITMException
	{
		String sql="",tranIdLast="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Timestamp toDateLast=null;
		try
		{
			sql = " SELECT max(to_date) as to_date FROM CUST_STOCK WHERE CUST_CODE = ?  " +
					" AND ITEM_SER = ? and order_type=? and pos_code is not null and confirmed='Y' and status='S' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, itemSer);
			pstmt.setString(3, orderType);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				toDateLast = rs.getTimestamp("to_date");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = " SELECT max(tran_id) as oldTranId FROM CUST_STOCK WHERE CUST_CODE = ?  " +
					" AND ITEM_SER = ? and order_type=? and pos_code is not null and confirmed='Y' and status='S' and to_date=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			pstmt.setString(2, itemSer);
			pstmt.setString(3, orderType);
			pstmt.setTimestamp(4, toDateLast);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				tranIdLast = checkNull(rs.getString("oldTranId"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try 
			{
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return tranIdLast;
	}

	public void callPstRs(PreparedStatement pstmt, ResultSet rs) 
	{
		try 
		{
			if (pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) 
			{
				rs.close();
				rs = null;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}

	private void writeLog(String fileName, ArrayList<String> logList,String logDate) throws ITMException 
	{
		String jBossHome = CommonConstants.JBOSSHOME;
		FileWriter localFileWriter = null;
		try {
			File logDir = new File(jBossHome + File.separator+ "log" + File.separator + "SecSalesGenProcLog");
			if (!logDir.exists()) {
				logDir.mkdirs();
			}
			localFileWriter = new FileWriter(new File(jBossHome + File.separator + "log" + File.separator + "SecSalesGenProcLog" + File.separator + fileName + ".log"), true);
			localFileWriter.write("Log for Seondary Sales Generation Process for date::"+logDate+" \n");
			for(int i=0;i<logList.size();i++)
			{
				localFileWriter.write((logList.get(i)).toString()+"\n");
			}
			localFileWriter.write("\n\n");
			localFileWriter.flush();
			localFileWriter.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
	}
	
}// END OF EJB