/*
 * Component created by saurabh[07/07/16] for new station code update process for flat table.  
 * */

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class StanCodeUpdPrc extends ProcessEJB implements StanCodeUpdPrcRemote, StanCodeUpdPrcLocal {

E12GenericUtility genericUtility =new E12GenericUtility();
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
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		String prdCodeFromDom="",prdCodeToDom="",custCodeDom="";
		String custCode="",custName="",prdCode="",itemSer="",stanCode="",stanCodeNew="",salesValue="",tranId="";
		int cnt=0;
		try {
			//ConnDriver con = new ConnDriver();
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection();
			System.out.println("In getdata Station update process:::");
			prdCodeFromDom = checkNull(genericUtility.getColumnValue("prd_code_from", dom));
			prdCodeToDom = checkNull(genericUtility.getColumnValue("prd_code_to", dom));
			custCodeDom = checkNull(genericUtility.getColumnValue("cust_code", dom));
			
			if(custCodeDom.contains(",")){
				custCodeDom=custCodeDom.replaceAll(",", "','");
			}
			
			retTabSepStrBuff.append("<?xml version=\"1.0\"?>\r\n<DocumentRoot>\r\n<description>Datawindow Root</description>\r\n<group0>\r\n<description>Group0 description</description>\r\n<Header0>\r\n<description>Header0 members</description>\r\n");
			
			sql = "SELECT SC.CUST_CODE, C.CUST_NAME, SC.PRD_CODE, SC.ITEM_SER, SC.STAN_CODE, ST.STAN_CODE AS STAN_CODE_NEW, SC.SALES_VALUE, SC.TRAN_ID " +
						" FROM SALES_CONSOLIDATION SC, CUSTOMER C, STATION ST WHERE SC.CUST_CODE = C.CUST_CODE AND ST.STAN_CODE = C.STAN_CODE AND " +
						" ST.STAN_CODE <> SC.STAN_CODE_NEW AND SC.SOURCE='E' AND SC.PRD_CODE BETWEEN ? AND ? AND SC.CUST_CODE IN ('"+custCodeDom+"')";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCodeFromDom);
				if(prdCodeToDom.length()>0){
				pstmt.setString(2, prdCodeToDom);
				}else{
				pstmt.setString(2, prdCodeFromDom);
				}
				rs = pstmt.executeQuery();
				while(rs.next()) {
				custCode = checkNull(rs.getString("CUST_CODE"));
				custName = checkNull(rs.getString("CUST_NAME"));
				prdCode = checkNull(rs.getString("PRD_CODE"));
				itemSer = checkNull(rs.getString("ITEM_SER"));
				stanCode = checkNull(rs.getString("STAN_CODE"));
				stanCodeNew = checkNull(rs.getString("STAN_CODE_NEW"));
				salesValue = checkNull(rs.getString("SALES_VALUE"));
				tranId = checkNull(rs.getString("TRAN_ID"));
				cnt++;
			
				retTabSepStrBuff.append("<Detail2>\r\n");
				retTabSepStrBuff.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>\r\n");
				retTabSepStrBuff.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>\r\n");
				retTabSepStrBuff.append("<prd_code>").append("<![CDATA["+prdCode+"]]>").append("</prd_code>\r\n");
				retTabSepStrBuff.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>\r\n");
				retTabSepStrBuff.append("<stan_code>").append("<![CDATA["+stanCode+"]]>").append("</stan_code>\r\n");
				retTabSepStrBuff.append("<stan_code_new>").append("<![CDATA["+stanCodeNew+"]]>").append("</stan_code_new>\r\n");
				retTabSepStrBuff.append("<sales_value>").append("<![CDATA["+salesValue+"]]>").append("</sales_value>\r\n");
				retTabSepStrBuff.append("<tran_id>").append("<![CDATA["+tranId+"]]>").append("</tran_id>\r\n");
				retTabSepStrBuff.append("</Detail2>\r\n");
				
			}
			
			retTabSepStrBuff.append("</Header0>\r\n");
			retTabSepStrBuff.append("</group0>\r\n");
			retTabSepStrBuff.append("</DocumentRoot>\r\n");
			errString = retTabSepStrBuff.toString();

			callPstRs(pstmt, rs);
			
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
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException {
		
		String rtStr = "";
		Document dom = null;
		Document dom2 = null;
		System.out.println("xmlString: "+xmlString);
		System.out.println("xmlString2: "+xmlString2);
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			rtStr = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::"+this.getClass().getSimpleName()+"::processString" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return rtStr;
	}

	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException {

		String errString = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn=null;
		int childNodeListLength = 0, parentNodeListLength = 0;
		String childNodeName = "";
		boolean result=false;
		String custCode="",custName="",prdCode="",itemSer="",stanCode="",stanCodeNew="",salesValue="",tranId="";
		String chgTerm="",chgUser="",selectedCheck="";
		try {
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			System.out.println("In process pricelist:::");
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
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
				System.out.println("parentNodeList>>>>>"+parentNodeList.item(i));
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
						System.out.println("custCode>>>>>>>"+custCode);
					}
					else if("cust_name".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						custName=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("custName>>>>>>>"+custName);
					}
					else if("prd_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						prdCode=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("prdCode>>>>>>>"+prdCode);
					}
					else if("item_ser".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						itemSer=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("itemSer>>>>>>>"+itemSer);
					}
					else if("stan_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						stanCode=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("stanCode>>>>>>>"+stanCode);
					}
					else if("stan_code_new".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						stanCodeNew=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("stanCodeNew>>>>>>>"+stanCodeNew);
					}
					else if("sales_value".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						salesValue=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("salesValue>>>>>>>"+salesValue);
					}
					else if("tran_id".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						tranId=checkNull(childNode.getFirstChild().getNodeValue());
						System.out.println("tranId>>>>>>>"+tranId);
					}
				}
				System.out.println("tranId>>>"+tranId+">>stanCodeNew>>"+stanCodeNew);
				result=updateData(tranId,stanCodeNew,chgTerm,chgUser, conn);
				if (!result) {
					conn.rollback();
					errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
				}
				else
				{
					conn.commit();
				}
			}
			if (result) {
				//conn.commit();
				errString = itmDBAccessEJB.getErrorString("", "VTDATASUCC", "","", conn);
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
			}
			
		} catch (Exception e) { 
			System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	
	public boolean updateData(String tranId,String stanCodeNew,String chgTerm,String chgUser,Connection conn) 
	{
		String sql = null;
		PreparedStatement pstmt = null;
		int upd = 0;
		try
		{
			sql = "UPDATE SALES_CONSOLIDATION set STAN_CODE_NEW=?,CHG_DATE=SYSDATE,CHG_TERM=?,CHG_USER=? where tran_id=?";
			System.out.println("upd sql............." + sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, stanCodeNew);
			pstmt.setString(2, chgTerm);
			pstmt.setString(3, chgUser);
			pstmt.setString(4, tranId);
			upd = pstmt.executeUpdate();
			System.out.println("upd count::::"+upd);
			pstmt.close();
			pstmt = null;
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			upd=0;
		}
		if (upd == 0){
			return false;
		}
		else{
			return true;
		}
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
		input = input == null ? "" : input.trim();
		return input;
	}
}
