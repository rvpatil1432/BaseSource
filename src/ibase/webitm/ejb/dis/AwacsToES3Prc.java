package ibase.webitm.ejb.dis;
/**
 * @author Saurabh Jarande[19/07/17]
 * This component is used for creating ES3 transactions from AWACS data uploaded by CFA. 
 *
 */
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
import java.util.Date;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class AwacsToES3Prc extends ProcessEJB implements AwacsToES3PrcLocal,AwacsToES3PrcRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	public String process(String xmlString, String xmlString2,String windowName, String xtraParams) throws RemoteException,ITMException 
	{
		Document detailDom = null, headerDom = null;
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
			System.out.println("Exception :"+this.getClass().getName()+" :process(String xmlString, String xmlString2, String windowName, String xtraParams):"+ e.getMessage() + ":");
			e.printStackTrace();
			/*retStr = e.getMessage();*/
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retStr;
	}
	
	public String process(Document headerDom, Document detailDom,String windowName, String xtraParams) throws RemoteException,ITMException 
	{
		String errString = "",sql="",prdCode="",custCode="",prdCodeDom="",custCodeDom="",toDateStr="",fromDateStr="",
				itemSer="",posCode="",empCode="",tranIdParent="",tranIdDel="";
		Date tranDate=null;
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		SimpleDateFormat sdf=null;
		HashMap<String,String> invMap = new HashMap<String,String>();
		HashMap<String,Integer> itemMap = new HashMap<String,Integer>();
		String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
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
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			prdCodeDom = checkNull(genericUtility.getColumnValue("prd_code", headerDom));
			custCodeDom = checkNull(genericUtility.getColumnValue("cust_code", headerDom));
			
			sql=" SELECT TRAN_ID FROM CUST_STOCK WHERE PRD_CODE=? AND POS_CODE IS NOT NULL AND TRAN_ID__PARENT IS NOT NULL " +
				" AND TRAN_ID__PARENT NOT IN(SELECT TRAN_ID FROM CUST_STOCK WHERE PRD_CODE=? AND POS_CODE IS NULL AND CONFIRMED = 'Y' )";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, prdCodeDom);
			pstmt.setString(2, prdCodeDom);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				tranIdDel=checkNull(rs.getString("tran_id"));
				System.out.println("tranIdDel::::"+tranIdDel);
				sql="DELETE FROM CUST_STOCK WHERE TRAN_ID=?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranIdDel);
				int i=pstmt1.executeUpdate();
				if(pstmt1!=null)
				{
					pstmt1.close();
					pstmt1=null;
				}
				if(i>0)
				{
					sql="DELETE FROM CUST_STOCK_INV WHERE TRAN_ID=?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranIdDel);
					pstmt1.executeUpdate();
				}
				if(pstmt1!=null)
				{
					pstmt1.close();
					pstmt1=null;
				}
			}
			callPstRs(pstmt, rs);
			
			sql=" SELECT FR_DATE,TO_DATE FROM PERIOD WHERE CODE=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, prdCodeDom);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				fromDateStr=sdf.format(rs.getDate("FR_DATE"));
				toDateStr=sdf.format(rs.getDate("TO_DATE"));
			}
			System.out.println("fromDateStr:::"+fromDateStr+":::toDateStr:::"+toDateStr);
			callPstRs(pstmt, rs);
			
			sql = " SELECT TRAN_ID,TRAN_DATE,CUST_CODE,PRD_CODE FROM CUST_STOCK WHERE CUST_CODE=? AND PRD_CODE=? AND POS_CODE IS NULL AND CONFIRMED = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCodeDom);
			pstmt.setString(2, prdCodeDom);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranIdParent=checkNull(rs.getString("tran_id"));
				tranDate=rs.getDate("tran_date");
				custCode=checkNull(rs.getString("cust_code"));
				prdCode=checkNull(rs.getString("prd_code"));

				invMap=getInvoiceDetails(tranIdParent,conn);
				
				sql=" SELECT A.TABLE_NO,A.POS_CODE,A.CUST_CODE,A.EMP_CODE FROM "+ 
					" (SELECT ROW_NUMBER() OVER (PARTITION BY C.TABLE_NO ORDER BY C.TABLE_NO) RN,C.POS_CODE,C.CUST_CODE,A.EMP_CODE,A.TABLE_NO "+ 
					" FROM ORG_STRUCTURE A INNER JOIN ORG_STRUCTURE_CUST C ON A.VERSION_ID=C.VERSION_ID AND A.TABLE_NO=C.TABLE_NO AND A.POS_CODE=C.POS_CODE "+
					" WHERE A.VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) AND C.CUST_CODE= ? AND C.SOURCE = 'A' "+
					" )A WHERE A.RN=1 ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					itemSer=checkNull(rs1.getString("TABLE_NO"));
					posCode=checkNull(rs1.getString("POS_CODE"));
					custCode=checkNull(rs1.getString("CUST_CODE"));
					empCode=checkNull(rs1.getString("EMP_CODE"));
					itemMap.clear();
					itemMap=getItemDetails(tranIdParent,itemSer,conn);
					System.out.println("itemMap::::"+itemMap.toString());
					errString=awacsGenProcess(itemSer,posCode,custCode,empCode,prdCode,tranIdParent,tranDate,loginSiteCode,fromDateStr,toDateStr,invMap,itemMap,chgUser,chgTerm,xtraParams,userInfo,conn);
				}
				callPstRs(pstmt1, rs1);
			}
			callPstRs(pstmt, rs);
			
		}// try end
		catch (Exception e) 
		{
			try{
			System.out.println("Exception :"+this.getClass().getName()+":process(String xmlString2, String xmlString2, String windowName, String xtraParams):"+ e.getMessage() + ":");
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTES3GENF", "","", conn);
			conn.rollback();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			throw new ITMException(e);
		}
		finally 
		{
			System.out.println("IN ["+this.getClass().getName()+"]>> Closing Connection....");
			try {
				if(errString==null || errString.trim().length()==0)
				{
					errString = itmDBAccessEJB.getErrorString("", "VTES3GENS", "","", conn);
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("", "VTES3GENF", "","", conn);
				}
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

	private HashMap<String, Integer> getItemDetails(String tranIdParent,String itemSer,Connection conn) throws ITMException 
	{
		HashMap<String,Integer> itemMap = new HashMap<String,Integer>();
		String sql="",itemCode="",itemSerHd="";
		int clStock=0;
		PreparedStatement pstmt=null;
		ResultSet rs =null;
		try 
		{
			itemSerHd=getItemSerList(itemSer, conn);
			sql=" SELECT ITEM_CODE,CL_STOCK,OP_STOCK FROM CUST_STOCK_DET WHERE TRAN_ID = ? AND ITEM_SER in ("+itemSerHd+") ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranIdParent);
			//pstmt.setString(2, itemSerHd);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				itemCode=checkNull(rs.getString("ITEM_CODE"));
				clStock=rs.getInt("CL_STOCK");
				if(clStock <= 0)
				{
					clStock=0;
				}
				itemMap.put(itemCode, clStock);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			itemMap=null;
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return itemMap;
	}

	public String getItemSerList(String itemser, Connection conn) throws ITMException
	{
		String itemSerGrpValue="",itemSerSplit="",resultItemSer="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try
		{
			sql= " select distinct item_ser from" +
					"(select item_ser from itemser where grp_code=?  " +
					"union all " +
					"select item_ser from itemser where item_ser=?) ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, itemser);
		pstmt.setString(2, itemser);
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			itemSerGrpValue=checkNull(rs.getString("item_ser")).trim();
			itemSerSplit=itemSerSplit+"'"+itemSerGrpValue+"',";
		}
		rs.close();
		rs = null;
		pstmt.close();
		resultItemSer = itemSerSplit.substring(0, itemSerSplit.length() - 1);
		System.out.println("resultItemSer>>>>>"+resultItemSer);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			try
            {
                throw new ITMException( exception );
            } catch (ITMException e)
            {
                e.printStackTrace();
            }
			throw new ITMException(exception); //Added By Mukesh Chauhan on 02/08/19
		}
		return resultItemSer;
	}
	
	private HashMap<String, String> getInvoiceDetails(String tranIdParent,Connection conn) throws ITMException 
	{
		HashMap<String,String> invMap = new HashMap<String,String>();
		String sql="",invoiceId="",dlvFlg="";
		PreparedStatement pstmt=null;
		ResultSet rs =null;
		try
		{
			sql=" SELECT INVOICE_ID,DLV_FLG FROM CUST_STOCK_INV WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranIdParent);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				invoiceId=checkNull(rs.getString("INVOICE_ID"));
				dlvFlg=checkNull(rs.getString("DLV_FLG"));
				invMap.put(invoiceId , dlvFlg);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			invMap=null;
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return invMap;
	}

	private String awacsGenProcess(String itemSer, String posCode,String custCode, String empCode, String prdCode,String tranIdParent, Date tranDate, String loginSiteCode,
			String fromDateStr, String toDateStr,HashMap invMap,HashMap itemMap, String chgUser, String chgTerm, String xtraParams,UserInfoBean userInfo, Connection conn) throws ITMException 
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
		int custCount = 0;
		try 
		{
			custCount=isCustExist(prdCode,custCode,itemSer,pstmt,rs,conn);
			if(custCount==0)
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
						} else if ("tran_id__parent".equalsIgnoreCase(childNodeName1)) {
							childNode1.setTextContent(tranIdParent);
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
					
					custStockInvDetails=custStockGWTIC.itemChanged("", xmlDetail1.toString(), xmlDetail1.toString(), "2", "itm_default", "A", xtraParams,"awacs_to_es3_prc",invMap);
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
						custStockItemDetails=custStockGWTIC.itemChanged(xmlInEditMode3, xmlParseStr, xmlParseStr, "3", "itm_default", "A", xtraParams,"awacs_to_es3_prc",itemMap);
					}
					else
					{
						xmlParseStr = xmlDetail1.toString();
						System.out.println(":::xmlParseStr::without Invoice:" + xmlParseStr);
						custStockItemDetails=custStockGWTIC.itemChanged(xmlInEditMode3, xmlParseStr, xmlParseStr, "3", "itm_default", "A", xtraParams,"awacs_to_es3_prc",itemMap);
					}
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
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			result=false;
			logList.add(e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
		writeLog(this.getClass().getSimpleName()+"_"+custCode+"_"+prdCode, logList,logDate);
		return errString;
	}

	private void callPstRs(PreparedStatement pstmt, ResultSet rs) throws ITMException {
		try {
			if(pstmt!=null)
			{
				pstmt.close();
				pstmt =null;
			}
			if(rs!=null)
			{
				rs.close();
				rs =null;
			}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private String checkNull(String input) 
	{
		input = input==null ? "" : input.trim();
		return input;
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retString;
	}
	
	private void writeLog(String fileName, ArrayList<String> logList,String logDate) throws ITMException 
	{
		String jBossHome = CommonConstants.JBOSSHOME;
		FileWriter localFileWriter = null;
		try {
			if(logList.size()>0){
			File logDir = new File(jBossHome + File.separator+ "log" + File.separator + "AwacsToEs3GenProcLog");
			if (!logDir.exists()) {
				logDir.mkdirs();
			}
			localFileWriter = new FileWriter(new File(jBossHome + File.separator + "log" + File.separator + "AwacsToEs3GenProcLog" + File.separator + fileName + ".log"), true);
			localFileWriter.write("Log for AWACS to Seondary Sales Generation Process for date::"+logDate+" \n");
			for(int i=0;i<logList.size();i++)
			{
				localFileWriter.write((logList.get(i)).toString()+"\n");
			}
			localFileWriter.write("\n\n");
			localFileWriter.flush();
			localFileWriter.close();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
	}
	
	private int isCustExist(String prdCode, String custCode, String itemSer, PreparedStatement pstmt, ResultSet rs, Connection conn) throws ITMException 
	{
		String sql="";
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
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
	
}// END OF EJB