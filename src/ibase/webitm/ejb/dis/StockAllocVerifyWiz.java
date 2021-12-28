/**
 * @author : PAVAN RANE
 * @Version : 1.0
 * Date : 01/07/18
 * Request Id: F18BSHL001
 */
package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import ibase.utility.E12GenericUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Session Bean implementation class StockAllocVerifyWiz
 */
@Stateless
public class StockAllocVerifyWiz extends ValidatorEJB implements StockAllocVerifyWizRemote, StockAllocVerifyWizLocal {

	public String globalXtraParams = "";
    E12GenericUtility genericUtility = new E12GenericUtility();
    
    /**
	 * The public method is used for converting the current form data into a document(DOM)
	 * The dom is then given as argument to the overloaded function wfValData to perform validation
	 * Returns validation string if exists else returns null in XML format
	 * @param xmlString contains the current form data in XML format
	 * @param xmlString1 contains all the header information in the XML format
	 * @param xmlString2 contains the data of all the forms in XML format
	 * @param objContext represents the form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
    public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
    {
    	Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String retString = "";
		System.out.println("StockAllocVerifyWiz.wfValData()");
		System.out.println("xmlString :" + xmlString);
		System.out.println("xmlString1 :" + xmlString1);
		System.out.println("xmlString2 :" + xmlString2);
		System.out.println("objContext :" + objContext);
		System.out.println("editFlag :" + editFlag);
		System.out.println("xtraParams :" + xtraParams);
		try
		   {
			   if(xmlString != null && xmlString.trim().length() > 0)
			   {
				  dom =  genericUtility.parseString(xmlString);
			   }
			   if(xmlString1 != null && xmlString1.trim().length() > 0)
			   {
				   dom1 = genericUtility.parseString(xmlString1);
			   }
			   if(xmlString2 != null && xmlString2.trim().length() > 0)
			   {
				   dom2 = genericUtility.parseString(xmlString2);
			   }
			   retString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			   
			   System.out.println("retString:::"+retString);
		   }
		   catch(Exception e)
		   {
			   System.out.println("Exception in StockAllocVerifyWiz wfValData :: " + getClass().getSimpleName() + "::"+ e.getMessage());			  
			   e.printStackTrace();
				throw new ITMException(e);
		   }
		   return retString;
	}
    /**
	 * The public overloaded method takes a document as input and is used for the validation of required fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currFormDataDom contains the current form data as a document object model
	 * @param hdrDataDom contains all the header information
	 * @param allFormDataDom contains the field data of all the forms 
	 * @param objContext represents form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("******************* Pavan Rane *******************");
		System.out.println("******* Inside StockAllocVerifyIC wfValData **********");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		E12GenericUtility genericUtility;
		String errString = "", userId = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";		
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		String itemCodeFrom="",itemCodeTo="";
		try
		{
			int currentFormNo = 0, childNodeListLength = 0, ctr = 0, cnt = 0;
			String childNodeName = "", errorType = "", errCode = "";
			String siteCode="";
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();
			conn = getConnection();
			genericUtility = new E12GenericUtility();	
			//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("xtraParam----->>["+xtraParams+"]");
			System.out.println("editFlag ------------>>["+editFlag+"]");
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo  = Integer.parseInt(objContext);
			}	
			switch (currentFormNo)  
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength  = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();										
					if (childNodeName.equalsIgnoreCase("site_code"))
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));																								
						if (siteCode == null || siteCode.trim().length() == 0 )
						{
							errList.add("NULLSITE");
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*) as cnt from site where site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}	
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0)
							{
								errList.add("VMINVSITE ");
								errFields.add(childNodeName.toLowerCase());
							}
						}						
					}	
					// Validation added  by Nandkumar Gadkari on 04/12/18-------------------start---------
					else if(childNodeName.equalsIgnoreCase("item_code__from"))
					{
						itemCodeFrom = checkNullandTrim(genericUtility.getColumnValue("item_code__from",dom));
						System.out.println(" itemCodeFrom ====" + itemCodeFrom);
						if (itemCodeFrom == null || itemCodeFrom.trim().length() == 0)
						{
							errCode = "VMITMCDFR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
						else 
						{
							if(!"00".equalsIgnoreCase(itemCodeFrom))
							{
								sql = "select count(1) from item where item_code= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCodeFrom);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
						    	
								 if(cnt == 0)
								 {	
									 errCode = "VMINVITMFR";
						    		 errList.add(errCode);
						    		 errFields.add(childNodeName.toLowerCase());	
									
								 }	
						    }
						}	
					}
					//item_code__to
					else if(childNodeName.equalsIgnoreCase("item_code__to"))
					{
						itemCodeTo = checkNullandTrim(genericUtility.getColumnValue("item_code__to",dom));
						System.out.println(" itemCodeTo ====" + itemCodeTo);

						if (itemCodeTo == null || itemCodeTo.trim().length() == 0)
						{
							errCode = "VMITMCDTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
						else 
						{
						
							if(!"ZZ".equalsIgnoreCase(itemCodeTo))
							{
								sql = "select count(1) from item where item_code= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCodeTo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
						    	
								 if(cnt == 0)
								 {	
									    errCode = "VMINVITMTO";
						    			errList.add(errCode);
						    			errFields.add(childNodeName.toLowerCase());		
									
								 }	
							}							
						}	
					}
					// Validation added  by Nandkumar Gadkari on 04/12/18-------------------end---------
				}//end of for loop
				break;// end of switch																	
			}// end of switch
			int errListSize = errList.size();
			int count = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) 
			{
				for (count = 0; count < errListSize; count++) {
					errCode = errList.get(count);
					errFldName = errFields.get(count);
					System.out.println(" testing :errCode .:" + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) {
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		}catch ( Exception e )
		{
			System.out.println ( "Exception: StockAllocVerifyWiz: wfValData( Document currFormDataDom ): " + e.getMessage() + ":" );
			e.printStackTrace();
			throw new ITMException(e);
		}finally
		{
			try
			{
				if(conn != null)
				{
					if(rs != null) 
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null) 
					{
						pstmt.close();
						pstmt = null;
						
					}
					conn.close();
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		
		return errStringXml.toString();
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String retString = "";
		E12GenericUtility genericUtility = new E12GenericUtility();		
		System.out.println("----------------Inside StockAllocVerifyWiz itemChanged-------------------");		
		try {
			globalXtraParams = xtraParams;
			System.out.println("xmlString::["+xmlString+"]\nxmlString1::["+xmlString1+"]\nxmlString2::["+xmlString2+"]");
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			retString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception: StockAllocVerifyIC: itemChanged:" + this.getClass().getSimpleName() + "::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		E12GenericUtility e12genericUtility = new E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer();

		String loginUser = "";
		int currentFormNo = 0;
		String siteCode = "", currDate = "", retString = "", dateFormat = "";

		boolean selectFlag = false, offerSelect = false;
		int existCnt = 0;
		InitialContext ctx = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		String finEntity = "", prdCodeFrm = "", prdCodeTo = "";
		String nodeName = "";
		String errCode = "";
		String effFrom1 = null, validUpto1 = null, remarks = null, prdCode = null;
		String sql = "";
		String sql1 = "";
		String lotNo = "";
		String lotSl = "";
		String locCode = "";
		String allocType = "";
		String siteCodeDom = "";
		String itemCode = "";		
		String errString = "";
		String xmlRetString = "";		
		String siteCodeTrc=""; 
		String itemCodeTrc="";
		String locCodeTrc="";
		String lotNoTrc="";
		String lotSlTrc="";
		String itemCodeFrom="",itemCodeTo="";// DECLARE BY NANDKUMAR GADKARI ON 04/12/18
		double unconfQty = 0.0d;	
		double stockQty = 0.0d; 
		
		double StkAllocQty = 0.0d; 
		double availStkQty = 0.0d; 
		int domID = 0, detDomId = 0;
		int rowCnt = 0;
		Connection conn = null;

		try {
			System.out.println("itemChanged called for StockAllocVerifyWiz");
			conn = getConnection();
			siteCode = e12genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			loginUser = e12genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			dateFormat = e12genericUtility.getApplDateFormat();
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(e12genericUtility.getApplDateFormat());
			currDate = sdf.format(new java.util.Date());
			System.out.println("currDate" + currDate);
			System.out.println("currentColumn[" + currentColumn + "] currentFormNo[" + currentFormNo + "]");

			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><Header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></Header>");

			switch (currentFormNo) {
			case 1: {
				System.out.println(" -------- Inside itemchange case 1111111 ------------ ");
				existCnt = 0;

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					System.out.println("loginUser" + loginUser);
					String loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					valueXmlString.append("<Detail1 domID='1'>");					
					valueXmlString.append("<site_code><![CDATA[").append(loginSite).append("]]></site_code>");
					valueXmlString.append("<alloc_type><![CDATA[").append("0").append("]]></alloc_type>");		
					//ADDED BY NANDKUMAR GADKARI ON 04/12/18----------------START----------------------
					valueXmlString.append("<item_code__to><![CDATA[").append("ZZ").append("]]></item_code__to>");
					valueXmlString.append("<item_code__from><![CDATA[").append("00").append("]]></item_code__from>");
					//ADDED BY NANDKUMAR GADKARI ON 04/12/18----------------END----------------------
					valueXmlString.append("</Detail1>");
				}
				break;
			}
			case 2: {
				System.out.println(" -------- Inside itemchange case 222222 ------------ ");
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					String loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					valueXmlString.append("<Detail1 domID='1'>");
					valueXmlString.append("<site_code><![CDATA[").append(loginSite).append("]]></site_code>");
					valueXmlString.append("</Detail1>");
 
					siteCode = checkNull(e12genericUtility.getColumnValue("site_code", dom1));
					allocType = checkNull(e12genericUtility.getColumnValue("alloc_type", dom1));
					//ADDED BY NANDKUMAR GADKARI ON 04/12/18----------------START----------------------
					itemCodeFrom = checkNull(e12genericUtility.getColumnValue("item_code__from", dom1));
					itemCodeTo = checkNull(e12genericUtility.getColumnValue("item_code__to", dom1));
					//ADDED BY NANDKUMAR GADKARI ON 04/12/18----------------END----------------------
					System.out.println("345::allocation Type["+allocType+"]");
					System.out.println("itemCodeForm["+itemCodeFrom+"]");
					System.out.println("itemCodeTo["+itemCodeTo+"]");
					sql = "SELECT F.SITE_CODE, F.ITEM_CODE, F.LOT_NO, F.LOC_CODE, F.LOT_SL, E.QUANTITY AS STOCK_QTY, E.ALLOC_QTY AS STOCK_ALLOC_QTY, (E.QUANTITY-E.ALLOC_QTY) AS AVAILABLE_STOCK, SUM(F.ISS_QTY) ISS_QTY" 
							+ " FROM"
							+ " (" 		//INV_PACK_ISS
								+ " SELECT B.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY"
								+ " FROM INV_PACK_ISS A, INV_PACK B"
								+ " WHERE A.TRAN_ID = B.TRAN_ID"
								+ " AND B.SITE_CODE = ?"
								+ " AND B.CONFIRMED = 'N'"
								+ "AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//WORKORDER_ISS
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, (A.QUANTITY+NVL(A.POTENCY_ADJ,0)) AS ISS_QTY"
								+ " FROM WORKORDER_ISSDET A, WORKORDER_ISS D"
								+ " WHERE A.TRAN_ID = D.TRAN_ID" 
								+ " AND D.SITE_CODE = ?"
								+ " AND D.CONFIRMED = 'N'"
								+ " AND D.TRAN_TYPE <> 'R'"
								+ " AND A.QUANTITY + NVL(A.POTENCY_ADJ,0) > 0" 
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//INV_ALLOCATE
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, (A.QUANTITY+NVL(A.POTENCY_ADJ,0)) AS ISS_QTY"
								+ " FROM INV_ALLOC_DET A, INV_ALLOCATE D"
								+ " WHERE A.TRAN_ID = D.TRAN_ID"
								+ " AND D.SITE_CODE = ?"
								+ " AND (NVL(A.DEALLOCATED,'N') <> 'Y' OR A.DEALLOCATED IS NULL)" 
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
								+ " AND NOT EXISTS (SELECT H.WORK_ORDER FROM WORKORDER_ISS H, WORKORDER_ISSDET I WHERE I.TRAN_ID = H.TRAN_ID"
								+ " AND H.WORK_ORDER = D.WORK_ORDER"
								+ " AND H.TRAN_DATE >= '01-apr-00' AND SITE_CODE = ?"
								+ " AND NVL(H.STATUS,'O') <> 'X'"
								+ " AND A.SITE_CODE = H.SITE_CODE"
								+ " AND A.EXP_LEV = I.EXP_LEV"
								+ " AND A.ITEM_CODE = I.ITEM_CODE"
								+ " AND A.LOC_CODE = I.LOC_CODE"
								+ " AND A.LOT_NO = I.LOT_NO"
								+ " AND A.LOT_SL = I.LOT_SL)" 
											//CONSUME_ISS
								+ " UNION ALL"
								+ " SELECT D.SITE_CODE__ORD, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY"
								+ " FROM CONSUME_ISS_DET A, CONSUME_ISS D" 
								+ " WHERE D.CONS_ORDER >' ' AND D.CONS_ISSUE = A.CONS_ISSUE AND D.CONFIRMED = 'N'" 
								+ " AND D.TRAN_TYPE = 'I'" 
								+ " AND D.SITE_CODE__ORD = ?"
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//DESPATCH
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY" 
								+ " FROM DESPATCHDET A, DESPATCH D" 
								+ " WHERE D.DESP_ID = A.DESP_ID AND D.DESP_DATE >= '01-jan-2000'" 
								+ " AND D.SITE_CODE = ?"
								+ " AND D.CONFIRMED = 'N'" 
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//SORDALLOC	
								+ " UNION ALL" 
								+ " SELECT A.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, SUM(A.QUANTITY__STDUOM) AS ISS_QTY" 
								+ " FROM SORDALLOC A"
								+ " WHERE  A.SITE_CODE = ?"
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
								+ " AND NOT EXISTS (SELECT H.SORD_NO FROM DESPATCH H, DESPATCHDET I WHERE I.DESP_ID = H.DESP_ID"
								+ " AND H.SORD_NO = A.SALE_ORDER"
								+ " AND I.LINE_NO__SORD = A.LINE_NO"
								+ " AND H.SITE_CODE = ?"
								+ " AND NVL(H.CONFIRMED,'N') = 'N'"
								+ " AND A.EXP_LEV = I.EXP_LEV"
								+ " AND A.ITEM_CODE = I.ITEM_CODE"
								+ " AND A.LOC_CODE = I.LOC_CODE"
								+ " AND A.LOT_NO = I.LOT_NO"
								+ " AND A.LOT_SL = I.LOT_SL	)"    
								+ " GROUP BY A.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL"
											//DISTORD_ISS
								+ " UNION ALL"
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY" 
								+ " FROM DISTORD_ISSDET A, DISTORD_ISS D" 
								+ " WHERE D.TRAN_ID = A.TRAN_ID AND D.CONFIRMED = 'N'" 
								+ " AND D.SITE_CODE = ?"
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//ADJ_ISSRCP
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY" 
								+ " FROM ADJ_ISSRCPDET A, ADJ_ISSRCP D" 
								+ " WHERE D.TRAN_ID = A.TRAN_ID" 
								+ " AND D.SITE_CODE = ?"
								+ " AND D.CONFIRMED = 'N' AND D.REF_SER = 'ADJISS'" 
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//WORDER_ISS_RND
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, (A.QUANTITY+NVL(A.POTENCY_ADJ,0)) AS ISS_QTY" 
								+ " FROM WORDER_ISSDET_RND A, WORDER_ISS_RND D" 
								+ " WHERE  A.TRAN_ID = D.TRAN_ID" 
								+ " AND D.CONFIRMED <> 'Y'" 
								+ " AND A.QUANTITY > 0" 
								+ " AND D.SITE_CODE =  ?"
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//RECEIPT_BACKFLUSH
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY" 
								+ " FROM RECEIPT_BACKFLUSH_DET A, RECEIPT_BACKFLUSH D" 
								+ " WHERE D.TRAN_ID = A.TRAN_ID" 
								+ " AND D.SITE_CODE = ?"
								+ " AND D.CONFIRMED = 'N'" 
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//STOCK_TRANSFER
								+ " UNION ALL" 
								+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO__FR AS LOT_NO, A.LOC_CODE__FR AS LOC_CODE, A.LOT_SL__FR AS LOT_SL, A.QUANTITY AS ISS_QTY" 
								+ " FROM STOCK_TRANSFER_DET A, STOCK_TRANSFER D" 
								+ " WHERE D.TRAN_ID = A.TRAN_ID" 
								+ " AND D.SITE_CODE = ?"
								+ " AND D.CONFIRMED = 'N'" 
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//QC_SAMPLE
								+ " UNION ALL"
								+ " SELECT D.SITE_CODE, D.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QTY_SAMPLE AS ISS_QTY" 
								+ " FROM QC_SAMPLE_STK A, QC_SAMPLE D" 
								+ " WHERE D.TRAN_ID = A.TRAN_ID" 
								+ " AND D.SITE_CODE =  ?"
								+ " AND D.CONFIRMED = 'N'"
								+ " AND D.ITEM_CODE >= ? AND D.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//PORCP
								+ " UNION ALL" 
								+ " SELECT A.SITE_CODE, B.ITEM_CODE, B.LOT_NO, B.LOC_CODE, B.LOT_SL, B.QUANTITY AS ISS_QTY"
								+ " FROM PORCP A, PORCPDET B" 
								+ " WHERE A.TRAN_ID = B.TRAN_ID" 
								+ " AND A.SITE_CODE = ?"
								+ " AND A.CONFIRMED = 'N'"
								+ " AND A.TRAN_SER ='P-RET'" 
								+ " AND B.ITEM_CODE >= ? AND B.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
											//STOCK
								+ " UNION ALL" 
								+ " SELECT A.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, 0 AS ISS_QTY"
								+ " FROM STOCK A"
								+ " WHERE  A.SITE_CODE = ?"
								+ " AND NVL(A.ALLOC_QTY,0) > 0"
								+ " AND A.ITEM_CODE >= ? AND A.ITEM_CODE <= ? "//ADDED BY NANDKUMAR GADKARI ON 04/12/18
								+ " ) F, STOCK E"
							+ " WHERE F.ITEM_CODE = E.ITEM_CODE" 
							+ " AND F.SITE_CODE = E.SITE_CODE" 
							+ " AND F.LOC_CODE = E.LOC_CODE "
							+ " AND F.LOT_NO = E.LOT_NO "
							+ " AND F.LOT_SL = E.LOT_SL"
							+ " AND E.INV_STAT IN ('SALE','NOSL')" 
							+ " GROUP BY F.SITE_CODE, F.ITEM_CODE,  F.LOT_NO, F.LOC_CODE, F.LOT_SL, E.QUANTITY, E.ALLOC_QTY";
							//if true then popukate inconsistent data
							if("1".equalsIgnoreCase(allocType))
							{
								sql = sql +  " HAVING NVL(SUM(F.ISS_QTY),0) <> E.ALLOC_QTY";
							}
							sql = sql + " ORDER BY F.SITE_CODE,F.ITEM_CODE, F.LOT_NO, F.LOC_CODE, F.LOT_SL";
						System.out.println(" --------sql ------------ "+sql);	
						pstmt = conn.prepareStatement(sql);					
						/*pstmt.setString(1, siteCode);
						pstmt.setString(2, siteCode);
						pstmt.setString(3, siteCode);
						pstmt.setString(4, siteCode);
						pstmt.setString(5, siteCode);
						pstmt.setString(6, siteCode);
						pstmt.setString(7, siteCode);
						pstmt.setString(8, siteCode);
						pstmt.setString(9, siteCode);
						pstmt.setString(10, siteCode);
						pstmt.setString(11, siteCode);
						pstmt.setString(12, siteCode);
						pstmt.setString(13, siteCode);
						pstmt.setString(14, siteCode);
						pstmt.setString(15, siteCode);
						pstmt.setString(16, siteCode);*/// commented by Nandkumar Gadkari 
						
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCodeFrom);
						pstmt.setString(3, itemCodeTo);
						pstmt.setString(4, siteCode);
						pstmt.setString(5, itemCodeFrom);
						pstmt.setString(6, itemCodeTo);
						pstmt.setString(7, siteCode);
						pstmt.setString(8, itemCodeFrom);
						pstmt.setString(9, itemCodeTo);
						pstmt.setString(10, siteCode);
						pstmt.setString(11, siteCode);
						pstmt.setString(12, itemCodeFrom);
						pstmt.setString(13, itemCodeTo);
						pstmt.setString(14, siteCode);
						pstmt.setString(15, itemCodeFrom);
						pstmt.setString(16, itemCodeTo);
						pstmt.setString(17, siteCode);
						pstmt.setString(18, itemCodeFrom);
						pstmt.setString(19, itemCodeTo);
						pstmt.setString(20, siteCode);
						pstmt.setString(21, siteCode);
						pstmt.setString(22, itemCodeFrom);
						pstmt.setString(23, itemCodeTo);
						pstmt.setString(24, siteCode);
						pstmt.setString(25, itemCodeFrom);
						pstmt.setString(26, itemCodeTo);
						pstmt.setString(27, siteCode);
						pstmt.setString(28, itemCodeFrom);
						pstmt.setString(29, itemCodeTo);
						pstmt.setString(30, siteCode);
						pstmt.setString(31, itemCodeFrom);
						pstmt.setString(32, itemCodeTo);
						pstmt.setString(33, siteCode);
						pstmt.setString(34, itemCodeFrom);
						pstmt.setString(35, itemCodeTo);
						pstmt.setString(36, siteCode);
						pstmt.setString(37, itemCodeFrom);
						pstmt.setString(38, itemCodeTo);
						pstmt.setString(39, siteCode);
						pstmt.setString(40, itemCodeFrom);
						pstmt.setString(41, itemCodeTo);
						pstmt.setString(42, siteCode);
						pstmt.setString(43, itemCodeFrom);
						pstmt.setString(44, itemCodeTo);
						
						rs = pstmt.executeQuery();			
						while(rs.next())
						{							
							siteCode = checkNull(rs.getString("SITE_CODE"));
							itemCode = checkNull(rs.getString("ITEM_CODE"));
							lotNo = checkNull(rs.getString("LOT_NO"));
							locCode = checkNull(rs.getString("LOC_CODE"));
							lotSl = checkNull(rs.getString("LOT_SL"));
							stockQty = rs.getDouble("STOCK_QTY");
							StkAllocQty = rs.getDouble("STOCK_ALLOC_QTY");				
							availStkQty = rs.getDouble("AVAILABLE_STOCK");
							unconfQty = rs.getDouble("ISS_QTY");
							
							double totAllocQtyTrc = 0.0d;
							sql1 = "SELECT ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL,SUM(ALLOC_QTY) ALLOC_QTY_TRC"
									+ " FROM INVALLOC_TRACE"
									+ " WHERE ITEM_CODE = ?"
									+ " AND SITE_CODE = ?"
									+ " AND LOC_CODE = ?"
									+ " AND LOT_NO = ?"
									+ " AND LOT_SL = ?"
									+ " GROUP BY ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL";
							pstmt1 = conn.prepareStatement(sql1);					
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, siteCode);
							pstmt1.setString(3, locCode);
							pstmt1.setString(4, lotNo);
							pstmt1.setString(5, lotSl);
							rs1 = pstmt1.executeQuery();	
							if(rs1.next())
							{
								rowCnt ++;
								itemCodeTrc = checkNull(rs1.getString("ITEM_CODE"));
								siteCodeTrc = checkNull(rs1.getString("SITE_CODE"));					
								locCodeTrc = checkNull(rs1.getString("LOC_CODE"));
								lotNoTrc = checkNull(rs1.getString("LOT_NO"));					
								lotSlTrc = checkNull(rs1.getString("LOT_SL"));					
								totAllocQtyTrc = rs1.getDouble("ALLOC_QTY_TRC");								
							}
							pstmt1.close();
							rs1.close();
							pstmt1 = null;
							rs1 = null;	
							System.out.println("rowCnt["+rowCnt+"");
							valueXmlString.append("<Detail2 domID='" + (++detDomId) + "'>");
							//valueXmlString.append("<Detail2>");			
							valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");						
							valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
							valueXmlString.append("<lot_no>").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
							valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
							valueXmlString.append("<lot_sl>").append("<![CDATA[" + lotSl + "]]>").append("</lot_sl>");
							valueXmlString.append("<stock_qty>").append("<![CDATA[" + stockQty + "]]>").append("</stock_qty>");
							valueXmlString.append("<stock_alloc_qty>").append("<![CDATA[" + StkAllocQty + "]]>").append("</stock_alloc_qty>");
							valueXmlString.append("<alloc_qty_trace>").append("<![CDATA[" + totAllocQtyTrc + "]]>").append("</alloc_qty_trace>");
							valueXmlString.append("<pending_qty>").append("<![CDATA[" + unconfQty + "]]>").append("</pending_qty>");								
							valueXmlString.append("</Detail2>");
							valueXmlString.append("\n");
							//rows ++;												
						}	
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;					
				}
			}//case2 end

			}
			valueXmlString.append("</Root>");
		}catch (SQLException se) {
			System.out.println("StockAllocVerifyWiz.itemChanged():catchBlock" + se.getMessage());
			se.printStackTrace();
			throw new ITMException(se);
		}catch (Exception e) {
			System.out.println("StockAllocVerifyWiz.itemChanged():catchBlock" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		} finally{
			try
			{
				if (conn != null)
				{
					if(rs != null) 
					{
						rs.close();
						rs = null;
					}
					if(pstmt != null) 
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs1 != null) 
					{
						rs1.close();
						rs1 = null;
					}
					if(pstmt1 != null) 
					{
						pstmt1.close();
						pstmt1 = null;
					}
					conn.close();
					conn = null;
				}				
			} catch (Exception e)
			{	
				e.printStackTrace();
			}
		}

		return valueXmlString.toString();
	}	
	
	private String errorType(Connection conn, String errorCode) throws ITMException {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ITMException(ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}
	private String checkNull(String str) {
		if (str == null) {
			return "";
		} else {
			return str;
		}
	}
	private String checkNullandTrim(String input) {
		if (input == null) 
		{
			input = "";
		}
		return input.trim();
	}
	@Override
	public String handleRequest(HashMap<String, String> reqParamMap) {
		String action = "", retXMLStr = "";

		try {

			action = (String) reqParamMap.get("action");

			if ("ITEM_CHANGE".equalsIgnoreCase(action)) {
				String currXmlDataStr = "", hdrXmlDataStr = "", allXmlDataStr = "", currentColumn = "", objContext = "",
						editFlag = "";

				currXmlDataStr = (String) reqParamMap.get("CUR_XML_STR");
				hdrXmlDataStr = (String) reqParamMap.get("HDR_XML_STR");
				allXmlDataStr = (String) reqParamMap.get("ALL_XML_STR");
				currentColumn = (String) reqParamMap.get("CUR_COLUMN");
				objContext = (String) reqParamMap.get("OBJ_CONTEXT");
				editFlag = (String) reqParamMap.get("EDIT_FLAG");

				retXMLStr = itemChanged(currXmlDataStr, hdrXmlDataStr, allXmlDataStr, objContext, currentColumn,
						editFlag, globalXtraParams);

				System.out.println("retXMLStr[" + retXMLStr + "] for action [" + action + "]");
			} else if ("DELETE".equalsIgnoreCase(action)) {

			}

		} catch (Exception e) {
			System.out.println("StockAllocVerifyWiz.handleRequest()" + e.getMessage());
			e.printStackTrace();
		}

		return retXMLStr;
	}	
}

