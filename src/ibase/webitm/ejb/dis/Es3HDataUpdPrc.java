package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class Es3HDataUpdPrc extends ProcessEJB implements Es3HDataUpdPrcLocal,Es3HDataUpdPrcRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	Connection conn = null;
	
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
			System.out.println("Exception :Es3HDataUpdPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):"+ e.getMessage() + ":");
			e.printStackTrace();
			/*retStr = e.getMessage();*/ //Commented By Mukesh Chauhan on 07/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return retStr;
	}// END OF PROCESS (1)
	
	public String process(Document headerDom, Document detailDom,String windowName, String xtraParams) throws RemoteException,ITMException 
	{
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		String errString = null;
		String childNodeName = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null, pstmt3 = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null;
		String sql = "", tranId = "", itemCode = "", invoiceId = "", dlvFlag = "",fromDateStr="",custCodeDom="",toDateStr="";
		//int sreturnCnt = 0;
		double retQty = 0, retRate = 0,retDiscnt=0,totalRetVal=0;
		double recptRetQty = 0, recptRetVal = 0;
		double recptReplQty = 0, recptReplVal = 0;
		double transitReplQty = 0, transitReplVal = 0;
		String retReplFlag = "";
		double invQty = 0, invRate = 0,invDisCnt=0,netAmt=0;
		double recptInvQty = 0, recptInvValue = 0;
		double sretQtyAllTot=0 ,primarySalesAll=0;
		double transitInvQty = 0, transitInvValue = 0;
		double FreeQty = 0, freeValue = 0;
		double recptFreeQty = 0, recptFreeValue = 0;
		double transitFreeQty = 0, transitFreeValue = 0;
		String lineType = "",calEnablePrice="",calPriceDivision="",checkItemSer="";
		double billRetQtyBonusQty = 0, billRetQtyBonusVal = 0,totalInvAmt=0,netAmtRet=0,netAmtRep=0;//,replNetVal=0;
		Timestamp prdFromoDateTmstmp = null, prdtoDateTmstmp = null;
		String custCode = "";
		double rateStd = 0, quantityStd = 0, formulaValue = 0, closingValue = 0,retQtyFreeDom=0;
		String priceList = "",tranIdLast="";
		//String sysDate = "";
		String invoiceMonths = "",itemSer="",prdCode="";//,invoiceIdList="",selectedInvList="";
		int invoiceMonthsPrevious = 0,retCnt=0,chkCnt=0;
		Timestamp thirdMonthDay = null;
		//Date currentDate = new Date();
		String refSer = "";// ,calCriItemSerStr="";
		double priceListRate = 0, clStock = 0,opStkDom=0,grossSecondaryQty=0,netSecondarySalesValue=0,grossSecondarySalesValue=0;
		double closingRate = 0,UpdCnt=0,grossSecondaryRate=0,salesQtyCal=0;//,rcpQtmDom=0;
		double clStockLast=0.0,rateOld=0.0,rateOrgOld=0.0,opValue=0.0;//,calRate=0.0;
		boolean isItemFound=false;//,isItemSerLocal=false;
		//ArrayList<String> calCriItemSerList=null;
		ClosingStockBean closingStockBean=null;
		HashMap<String,ClosingStockBean> closingStockMap=null;
		UtilMethods utlmethd = new UtilMethods();
		ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
		String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		HashSet<String> invoiceItemSet=null;
		String invoiceItemKey="";
		Timestamp fromDate=null,toDate=null;
		try {
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
		} catch (Exception e) {
			System.out.println("Exception :Es3HDataUpdPrc :ejbCreate :==>" + e);
			e.printStackTrace();
		}

		try {
			/*calCriItemSerStr =  dist.getDisparams("999999","CAL_CRIT_ITEMSER",conn);
			System.out.println("calCriItemSerStr.." + calCriItemSerStr);
			System.out.println("isItemSer@@@@@@@before>>>>"+isItemSerLocal);
			if (("NULLFOUND".equalsIgnoreCase(calCriItemSerStr) || calCriItemSerStr == null || calCriItemSerStr.trim().length() == 0) )
			{
				isItemSerLocal=false;
				System.out.println("isItemSer@@>>>>"+isItemSerLocal);
			}else
			{
				calCriItemSerList= new ArrayList<String>(Arrays.asList(calCriItemSerStr.split(",")));
				isItemSerLocal=false;
				System.out.println("isItemSer@@Chk>>>>"+isItemSerLocal);
			}*/
			
			invoiceMonths = dist.getDisparams("999999","INVOICE_MONTHS", conn);
			System.out.println("invoiceMonths.." + invoiceMonths);
			if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0)) 
			{
				invoiceMonthsPrevious = -3;
			}
			else 
			{
				invoiceMonthsPrevious = Integer.parseInt(invoiceMonths);
			}
			// thirdMonthDay= utlmethd.AddMonths(prdtoDateTmstmp, -3);
			System.out.println("invoiceMonthsPrevious>>>>>"+ invoiceMonthsPrevious);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//sysDate = sdf2.format(currentDate.getTime());
			
			parentNodeList = headerDom.getElementsByTagName("Detail1");
			parentNodeListLength = parentNodeList.getLength();
			if(parentNodeListLength == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPSELONERD","","",conn); 
				return errString;
			}
			System.out.println("::::::parentNodeListLength["+parentNodeListLength+"]");
			for (int i = 0; i < parentNodeListLength; i++) {
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
					if("item_ser".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						itemSer=childNode.getFirstChild().getNodeValue();
						itemSer= itemSer==null ? "" : itemSer.trim();
					}
					if("prd_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						prdCode=childNode.getFirstChild().getNodeValue();
						prdCode= prdCode==null ? "" : prdCode.trim();
					}
					//Two additional filters added to run process as per tran date and cust code which will avoid unnesscary update [25/07/17|Start]
					if("from_date".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						fromDateStr=childNode.getFirstChild().getNodeValue();
						fromDateStr= fromDateStr==null ? "" : fromDateStr.trim();
					}
					if("to_date".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						toDateStr=childNode.getFirstChild().getNodeValue();
						toDateStr= toDateStr==null ? "" : toDateStr.trim();
					}
					if("cust_code".equalsIgnoreCase(childNodeName) && childNode.getFirstChild()!=null)
					{
						custCodeDom=childNode.getFirstChild().getNodeValue();
						custCodeDom= custCodeDom==null ? "" : custCodeDom.trim();
					}
					//Two additional filters added to run process as per tran date and cust code which will avoid unnesscary update [25/07/17|End]
				}
			}
			
			if(custCodeDom!=null && custCodeDom.trim().length()>0)
			{
				custCodeDom="'"+custCodeDom+"'";
				if(custCodeDom.contains(",")){
					custCodeDom = custCodeDom.replaceAll(",", "','");
				}
			}
			fromDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			toDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("custCode::::"+custCode+":::fromDate::::"+fromDate+":::toDate::::"+toDate);
			/*if(calCriItemSerList.contains(itemSer.trim()))
			{
				System.out.println("Inside ItemSer true::::["+calCriItemSerList.contains(itemSer.trim())+"]");
				isItemSerLocal=true;
			}
			else
			{
				System.out.println("Inside ItemSer false::::["+calCriItemSerList.contains(itemSer.trim())+"]");
				isItemSerLocal=false;
			}*/
			//Modified by santosh to set priceList(14/SEP/2017).[START]
			calEnablePrice =  dist.getDisparams("999999","ENABLE_SPEC_PRICELIST",conn);
			calPriceDivision =  dist.getDisparams("999999","SPEC_PRICELIST",conn);
			System.out.println("calEnablePrice["+calEnablePrice+"]calPriceDivision["+calPriceDivision+"]");
			if (("NULLFOUND".equalsIgnoreCase(calEnablePrice) || calEnablePrice == null || calEnablePrice.trim().length() == 0) )
			{
				 calEnablePrice="N";
			}
			if (("NULLFOUND".equalsIgnoreCase(calPriceDivision) || calPriceDivision == null || calPriceDivision.trim().length() == 0) )
			{
				 calEnablePrice="N";
			}
			System.out.println("calEnablePrice["+calEnablePrice+"]calPriceDivision["+calPriceDivision+"]");
			//Modified by santosh to set priceList(14/SEP/2017).[END]
			sql = " select tran_id,from_date,to_date,cust_code,tran_id__last,item_ser from cust_stock where pos_code is not null and prd_code=? AND ITEM_SER=? " +
					" and tran_date between ? and ? and cust_code in ("+custCodeDom+") ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, prdCode);
			pstmt.setString(2, itemSer);
			pstmt.setTimestamp(3, fromDate);
			pstmt.setTimestamp(4, toDate);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				tranId = checkNull(rs.getString("tran_id"));
				prdFromoDateTmstmp = rs.getTimestamp("from_date");
				prdtoDateTmstmp = rs.getTimestamp("to_date");
				custCode = checkNull(rs.getString("cust_code"));
				tranIdLast = checkNull(rs.getString("tran_id__last"));
				checkItemSer = checkNull(rs.getString("item_ser"));
				System.out.println("tranId>>"+tranId+" prdFromoDateTmstmp>>"+prdFromoDateTmstmp+" prdtoDateTmstmp>>"+prdtoDateTmstmp+" custCode>>"+custCode);
				//Modified by saurabh[13/02/17|Start]
				thirdMonthDay = utlmethd.AddMonths(prdFromoDateTmstmp,invoiceMonthsPrevious);
				System.out.println("thirdMonthDay>>>>>>" + thirdMonthDay);
				//Modified by santosh to set priceList(14/SEP/2017).[START]
				if("Y".equalsIgnoreCase(calEnablePrice))
				{
					if("BR".equalsIgnoreCase(checkItemSer))
					{
						priceList = calPriceDivision.substring(calPriceDivision.indexOf(",")+1,calPriceDivision.length());
						System.out.println("@S@priceList["+priceList+"]");
					}
					else
					{
						priceList= calPriceDivision.substring(0,calPriceDivision.indexOf(","));
						System.out.println("@S@priceList["+priceList+"]");
					}
				}
				else
				{
					sql = "select price_list from customer where cust_code =? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, custCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) {
						priceList = checkNull(rs1.getString("price_list"));
						System.out.println("priceList edit :" + priceList);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
				//Modified by santosh to set priceList(14/SEP/2017).[END]
				closingStockMap=new HashMap<String, ClosingStockBean>();
				sql = "	select item_code,cl_stock,CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
						" CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org from " +
						//"cust_stock_det where tran_id=? and item_code=? ";
						"cust_stock_det where tran_id=?  ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranIdLast);
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) 
				{
					closingStockBean=new ClosingStockBean();
					closingStockBean.setOpStock(rs1.getDouble("cl_stock"));
					closingStockBean.setOpeningRate(rs1.getDouble("rate"));
					closingStockBean.setOpeningRateOrg(rs1.getDouble("rate__org"));
					closingStockMap.put(checkNull(rs1.getString(1)), closingStockBean);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				//Modified by saurabh[13/02/17|End]
				sql="update cust_stock_inv set net_amt=0 where tran_id=? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranId);
				UpdCnt = pstmt1.executeUpdate();
				if(UpdCnt>0)
				{
					System.out.println("Record reset for tran_id>>"+tranId+" net_amt>>"+netAmt);
				}
				pstmt1.close();
				pstmt1 = null;
				
				sql = "select item_code,cl_stock,op_stock from cust_stock_det where tran_id='"+tranId+"'";
				pstmt1 = conn.prepareStatement(sql);
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {
					itemCode = checkNull(rs1.getString("item_code"));
					clStock = rs1.getDouble("cl_stock");
					opStkDom = rs1.getDouble("op_stock");
					System.out.println("itemCode>>> "+itemCode+" clStock>>>"+clStock+" opStkDom>>"+opStkDom);
					clStockLast=0.0;rateOld=0.0;rateOrgOld=0.0;
					transitInvQty=0;transitInvValue=0;recptInvQty=0;recptInvValue=0;transitFreeQty=0;transitFreeValue=0;recptFreeQty=0;recptFreeValue=0;
					recptRetVal=0;recptRetQty=0;billRetQtyBonusQty=0;billRetQtyBonusVal=0;transitReplQty=0;transitReplVal=0;recptReplQty=0;recptReplVal=0;
					salesQtyCal=0;closingRate = 0;grossSecondaryRate=0;grossSecondaryQty=0;closingValue=0;netSecondarySalesValue=0;opValue=0;sretQtyAllTot=0;primarySalesAll=0;
					totalInvAmt=0;totalRetVal=0;
					/*sql = "	select cl_stock,CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
							" CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org from " +
							"cust_stock_det where tran_id=? and item_code=? ";
					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, tranIdLast);
					pstmt2.setString(2, itemCode);
					rs2 = pstmt2.executeQuery();
					if (rs2.next()) 
					{
						clStockLast = rs2.getDouble("cl_stock");
						rateOld = rs2.getDouble("rate");
						rateOrgOld = rs2.getDouble("rate__org");
						opStkDom=clStockLast;
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;*/
					if(closingStockMap.size()>0)
					{
						if(closingStockMap.containsKey(itemCode))
						{
							clStockLast=closingStockMap.get(itemCode).getOpStock();
							rateOld=closingStockMap.get(itemCode).getOpeningRate();
							rateOrgOld=closingStockMap.get(itemCode).getOpeningRateOrg();
							opStkDom=clStockLast;
						}
						else
						{
							clStockLast=0;
							rateOld=0;
							rateOrgOld=0;
							opStkDom=0;
						}	
					}
					else
					{
						clStockLast=0;
						rateOld=0;
						rateOrgOld=0;
						opStkDom=0;
					}					
					System.out.println("itemCode>>>"+itemCode +"rateOld>>"+rateOld+" rateOrgOld>>>"+rateOrgOld+" opStkDom>>>"+opStkDom);

					invoiceItemSet=new HashSet<String>();
					sql="select invoice_id,item_code from invoice_trace where invoice_id in(select invoice_id from cust_stock_inv where  tran_id='"+tranId+"' and ref_ser='S-INV')";
					pstmt2 = conn.prepareStatement(sql);
					rs2 = pstmt2.executeQuery();
					while(rs2.next())
					{
						invoiceItemSet.add(checkNull(rs2.getString(1))+"@"+checkNull(rs2.getString(2)));	
					}
					rs2.close();
					rs2=null;
					pstmt2.close();
					pstmt2=null;
					
					sql = "select invoice_id,dlv_flg,ref_ser from cust_stock_inv where tran_id='"+tranId+"'";
					pstmt2 = conn.prepareStatement(sql);
					rs2 = pstmt2.executeQuery();
					while (rs2.next()) 
					{
						isItemFound=false;
						netAmt=0;netAmtRet=0;netAmtRep=0;
						//sreturnCnt = 0;
						invoiceId = checkNull(rs2.getString("invoice_id"));
						dlvFlag = checkNull(rs2.getString("dlv_flg"));
						refSer = checkNull(rs2.getString("ref_ser"));
						System.out.println("invoiceId>>>"+invoiceId+">>dlvFlag>>>"+dlvFlag+">>refSer>>"+refSer);
						/*if("Y".equalsIgnoreCase(dlvFlag))
						{
							invoiceIdList = invoiceIdList + "'"+invoiceId.trim() + "',";
						}*/
						
						/*sql = "SELECT tran_id  FROM sreturn WHERE tran_id='"+invoiceId+"'";
						pstmt3 = conn.prepareStatement(sql);
						rs3 = pstmt3.executeQuery();
						if (rs3.next()) 
						{
							sreturnCnt++;
						}
						rs3.close();
						rs3 = null;
						pstmt3.close();
						pstmt3 = null;*/
						//if (sreturnCnt == 0)
						if("S-INV".equalsIgnoreCase(refSer))
						{
							invoiceItemKey=invoiceId+"@"+itemCode;
							if(invoiceItemSet.contains(invoiceItemKey))
							{
							//refSer="S-INV";
							//sql = "select QUANTITY__STDUOM,RATE__STDUOM,DISCOUNT from invoice_trace where invoice_id='"+invoiceId+"' and item_code='"+itemCode+"' and RATE__STDUOM>0.001";
							//sql = "select QUANTITY__STDUOM,RATE__STDUOM,DISCOUNT from invoice_trace where invoice_id='"+invoiceId+"' and item_code='"+itemCode+"' ";
							sql = "SELECT itrc.QUANTITY__STDUOM as QUANTITY__STDUOM,itrc.RATE__STDUOM as RATE__STDUOM,itrc.DISCOUNT as DISCOUNT "
									+ " FROM invoice_trace itrc,invoice inv WHERE itrc.invoice_id=inv.invoice_id "
									+ " and inv.invoice_id='"+invoiceId+"' and inv.cust_code = '"+custCode+"' and itrc.item_code='"+itemCode+"' ";
							pstmt3 = conn.prepareStatement(sql);
							rs3 = pstmt3.executeQuery();
							while (rs3.next()) 
							{
								invQty = rs3.getDouble("QUANTITY__STDUOM");
								invRate = rs3.getDouble("RATE__STDUOM");
								invDisCnt = rs3.getDouble("DISCOUNT");
								if(invRate<=0.001){ 
									isItemFound=true;
								}
								if(invRate>0.001)
								{
									//Formula
									totalInvAmt = getRequiredDcml(((invQty * invRate)-((invQty *invRate * invDisCnt)/100)),3);
									netAmt=(long)Math.round(totalInvAmt);
									if (dlvFlag.equalsIgnoreCase("N")) 
									{
										transitInvQty += invQty;//transit_qty
										//transitInvValue += (invQty * invRate);// transit_bill_val
										transitInvValue += totalInvAmt;// transit_bill_val
										transitInvValue=getRequiredDcml(transitInvValue,3);
									}
									else 
									{
										recptInvQty += invQty;//purc_rcp
										//recptInvValue += (invQty * invRate);// rcp_val
										recptInvValue += totalInvAmt;// rcp_val
										recptInvValue=getRequiredDcml(recptInvValue,3);
									}
								}
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
							if(isItemFound)
							{
								/*sql = "select sum(totfreeQty) as freeqty,sum(totFreeValue) as totalvalue from"
										+ " (select invoice_id,sum(quantity) totfreeQty,sum(rate),sum(quantity)*sum(rate) as totFreeValue "
										+ "from ( select invoice.invoice_id as invoice_id,0 quantity, max(rate__stduom) "
										+ "rate from invoice invoice,invoice_trace itrace  where invoice.invoice_id= Itrace.invoice_id "
										+ "and invoice.invoice_id ='"+invoiceId+"' and itrace.item_code='"+itemCode+"' ";
								sql = sql
										+ " and rate__stduom>0.001 group by invoice.invoice_id "
										+ "union all"
										+ " select invoice.invoice_id as invoice_id,quantity__stduom quantity, 0 rate from "
										+ "invoice invoice,invoice_trace itrace  where invoice.invoice_id= Itrace.invoice_id and "
										+ "invoice.invoice_id ='"+invoiceId+"' and itrace.item_code='"+itemCode+"' ";
								sql = sql
										+ " and itrace.rate__stduom<=0.001 ) group by invoice_id ) HAVING sum(totfreeQty)>0 ";*/
								sql=" SELECT (totfreeQty) AS freeqty, (totFreeValue) AS totalvalue " +
										" FROM (SELECT SUM(quantity) as totfreeQty, SUM(quantity) * SUM(rate) AS totFreeValue FROM " +
										" ( SELECT invoice.invoice_id AS invoice_id,0 quantity, MAX(rate__stduom) rate " +
										" FROM invoice invoice, invoice_trace itrace WHERE invoice.invoice_id= Itrace.invoice_id " +
										" AND invoice.invoice_id  ='"+invoiceId+"' AND itrace.item_code ='"+itemCode+"' AND rate__stduom        >0.001 " +
										" GROUP BY invoice.invoice_id UNION ALL SELECT invoice.invoice_id AS invoice_id,quantity__stduom quantity, " +
										" 0 rate FROM invoice invoice, invoice_trace itrace WHERE invoice.invoice_id= Itrace.invoice_id " +
										" AND invoice.invoice_id  ='"+invoiceId+"' AND itrace.item_code  ='"+itemCode+"' AND itrace.rate__stduom<=0.001 ) " +
										" GROUP BY invoice_id ) WHERE totFreeValue >0 ";
								pstmt3 = conn.prepareStatement(sql);
								rs3 = pstmt3.executeQuery();
								while (rs3.next()) 
								{
									//Bonus
									FreeQty = rs3.getDouble(1);
									freeValue = rs3.getDouble(2);
									if (dlvFlag.equalsIgnoreCase("N")) 
									{
										transitFreeQty += FreeQty;// transit_qty__free
										transitFreeValue += (freeValue);// Transit_free_val
										transitFreeValue=getRequiredDcml(transitFreeValue,3);
									}
									else 
									{
										recptFreeQty += FreeQty;// purc_rcp__free
										recptFreeValue += (freeValue);// rcp_free_val
										recptFreeValue=getRequiredDcml(recptFreeValue,3);
									}
								}
								rs3.close();
								rs3 = null;
								pstmt3.close();
								pstmt3 = null;
							}
						}
						}
						else 
						{
							//refSer="S-RET";
							sql = " select count(1) as count from cust_stock_inv where tran_id=? and invoice_id=? ";
							pstmt3 = conn.prepareStatement(sql);
							pstmt3.setString(1, tranIdLast);
							pstmt3.setString(2, invoiceId);
							rs3 = pstmt3.executeQuery();
							while(rs3.next())
							{
								retCnt=rs3.getInt("count");
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
							
							sql = "select quantity__stduom,rate__stduom,RET_REP_FLAG,LINE_TYPE,discount from sreturndet where tran_id='"+invoiceId+"' and item_code='"+itemCode+"'";
							pstmt3 = conn.prepareStatement(sql);
							rs3 = pstmt3.executeQuery();
							while (rs3.next()) 
							{
								retQty = rs3.getDouble("quantity__stduom");
								retRate = rs3.getDouble("rate__stduom");
								retReplFlag = rs3.getString("RET_REP_FLAG");
								lineType = rs3.getString("LINE_TYPE");
								retDiscnt = rs3.getDouble("discount");
								totalRetVal=getRequiredDcml(((retQty * retRate)-((retQty * retRate * retDiscnt)/100)),3);
								if (retReplFlag.equalsIgnoreCase("R")) 
								{
									//recptRetVal += (retQty * retRate);// ret_val
									if(retCnt>0)
									{
										netAmtRet=0;
										recptRetQty=0;
										recptRetVal=0;
									}
									else{
										recptRetQty += retQty;// purc_ret
										recptRetVal += totalRetVal;// ret_val
										netAmtRet=(long)Math.round(totalRetVal);
									}
									
									if ("F".equalsIgnoreCase(lineType)) 
									{
										billRetQtyBonusQty += retQty;// purc_ret__free
										//billRetQtyBonusVal += (retQty * retRate);// ret_free_val
										billRetQtyBonusVal += totalRetVal;// ret_free_val
										billRetQtyBonusVal=getRequiredDcml(billRetQtyBonusVal,3);
									}
									
								}
								else 
								{
									if (dlvFlag.equalsIgnoreCase("N")) 
									{
										transitReplQty += retQty;// transit_qty__repl
										//transitReplVal += (retQty * retRate);// transit_repl_val
										transitReplVal += totalRetVal;// transit_repl_val
										transitReplVal=getRequiredDcml(transitReplVal,3);
									}
									else 
									{
										recptReplQty += retQty;// purc_rcp__repl
										//recptReplVal += (retQty * retRate);// rcp_repl_val
										recptReplVal += totalRetVal;// rcp_repl_val
										recptReplVal=getRequiredDcml(recptReplVal,3);
									}
									netAmtRep=(long)Math.round(totalRetVal);
								}
								netAmt = (-netAmtRet+netAmtRep);
							}
							rs3.close();
							rs3 = null;
							pstmt3.close();
							pstmt3 = null;
						}
						if(netAmt != 0){
						sql="update cust_stock_inv set net_amt=net_amt+?,ref_ser=? where tran_id=? and invoice_id=? ";
							pstmt3 = conn.prepareStatement(sql);
							pstmt3.setDouble(1, netAmt);
							pstmt3.setString(2, refSer);
							pstmt3.setString(3, tranId);
							pstmt3.setString(4, invoiceId);
							UpdCnt = pstmt3.executeUpdate();
							if(UpdCnt>0)
							{
								System.out.println("Record updated for tran_id>>"+tranId+" invoice_id>>"+invoiceId+" net_amt>>"+netAmt+" ref_ser>>"+refSer);
							}
							pstmt3.close();
							pstmt3 = null;
						}
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;

					/*if(invoiceIdList.trim().length() > 0)
					{
						selectedInvList = invoiceIdList.substring(0,invoiceIdList.length() - 1);
					}*/
					
/*					if(clStock>0)
					{*/
					formulaValue=clStock;
					//if(!isItemSerLocal)
					//{	
						System.out.println("formulaValue>>"+formulaValue);
	
						sql = "SELECT inv.invoice_id,itrc.rate__stduom as rate__stduom,itrc.quantity__stduom as quantity__stduom,inv.tran_date "
								+ "FROM invoice_trace itrc,invoice inv WHERE itrc.invoice_id=inv.invoice_id "
								+ "and itrc.item_code=? and inv.tran_date>=? "
								+ "and inv.tran_date<=?  and inv.cust_code=? AND itrc.rate__stduom >0.001 "
								+ " ORDER BY inv.tran_date DESC";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, itemCode);
						pstmt2.setTimestamp(2, thirdMonthDay);
						pstmt2.setTimestamp(3, prdtoDateTmstmp);
						pstmt2.setString(4, custCode);
						rs2 = pstmt2.executeQuery();
						while (rs2.next()) 
						{
							rateStd = rs2.getDouble("rate__stduom");
							quantityStd = rs2.getDouble("quantity__stduom");
							System.out.println("rateStd :" + rateStd);
							System.out.println("quantityStd :" + quantityStd);
							if (formulaValue >= quantityStd) 
							{
								closingValue = closingValue + quantityStd * rateStd;
								System.out.println("closing value" + closingValue);
								formulaValue = formulaValue - quantityStd;
								System.out.println("formulaValue2>>"+formulaValue);
							}
							else 
							{
								closingValue = closingValue + formulaValue * rateStd;
								formulaValue = 0;
								System.out.println("closing value>>>>" + closingValue);
								System.out.println("formulaValue3>>"+formulaValue);
							}
							closingValue = getRequiredDcml(closingValue, 3);
							if (formulaValue == 0) 
							{
								break;
							}
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						System.out.println("closingValue>>>>>>" + closingValue);
						System.out.println("formulaValue>>>>>>" + formulaValue);
						if (formulaValue > 0) 
						{
							// System.out.println("dbSysDate>>>>>>"+dbSysDate);
							// priceListRate = getRate(custCodeStatic,dbSysDate,itemCode1,conn);
							/*sql = "select price_list from customer where cust_code =? ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, custCode);
							rs2 = pstmt2.executeQuery();
							if (rs2.next()) {
								priceList = checkNull(rs2.getString("price_list"));
								System.out.println("priceList edit :" + priceList);
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;*/
							
							//sysDate = genericUtility.getValidDateString(sysDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
							sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, SYSDATE , ? ) FROM DUAL ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, priceList);
							//pstmt2.setString(2, sysDate);
							//pstmt2.setString(3, genericUtility.getDBDateFormat());
							pstmt2.setString(2, itemCode);
							rs2 = pstmt2.executeQuery();
							if (rs2.next()) 
							{
								priceListRate = rs2.getDouble(1);
								System.out.println("rateAll-----------> ["+ priceListRate + "]");
							}
							if (rs2 != null) 
							{
								rs2.close();
								rs2 = null;
							}
							if (pstmt2 != null) {
								pstmt2.close();
								pstmt2 = null;
							}
							priceListRate = getRequiredDcml(priceListRate, 3);
							// Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|end]
							System.out.println("rateAll>>>>>" + priceListRate);
							closingValue = closingValue + formulaValue * priceListRate;
							closingValue = getRequiredDcml(closingValue, 3);//cl_value
						}
				//}else
			/*{
					
					if(recptInvQty > 0)
					{
						sql =  " SELECT itrace.RATE__STDUOM as RATE__STDUOM FROM" +
								" invoice invoice,invoice_trace itrace " +
								" 	where invoice.invoice_id=itrace.invoice_id and itrace.invoice_id in("+selectedInvList+")" +
								" AND  invoice.cust_code = ? " +
								//" and invoice.inv_type=? " +
								" and itrace.item_ser__prom in("+table_no+")  "+
								" and itrace.item_code = ? " +
								" AND itrace.RATE__STDUOM>0.001 ORDER BY invoice.tran_date DESC " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode  );
						//pstmt.setString(2, orderType );
						pstmt.setString(2, itemCode );
						rs = pstmt.executeQuery( );
						if( rs.next() )
						{
							calRate = rs.getDouble("RATE__STDUOM" );
							System.out.println("openingRate>>>>>> :"+calRate);
							 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
							invoiceMonths = dist.getDisparams("999999", "INVOICE_MONTHS", conn);
							System.out.println("invoiceMonths>>>>.." + invoiceMonths);
							if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0))
							{
								invoiceMonthsPrevious = -3;
							} else
							{
								invoiceMonthsPrevious = Integer.parseInt(invoiceMonths);
							}
							System.out.println("invoiceMonthsPrevious>>>>>" + invoiceMonthsPrevious);
							thirdMonthDay = utlmethd.AddMonths(prdFromoDateTmstmp, invoiceMonthsPrevious);
							System.out.println("thirdMonthDay from method>>>>>>" + thirdMonthDay);
		
							sql = "SELECT inv.invoice_id,itrc.rate__stduom as rate__stduom,inv.tran_date " + 
							"FROM invoice_trace itrc,invoice inv WHERE itrc.invoice_id=inv.invoice_id " + 
									"and itrc.item_code=? and inv.tran_date>=? " +
							"and inv.tran_date<=? and inv.cust_code=? AND itrc.rate__stduom >0.001  " + 
									" ORDER BY inv.tran_date DESC";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setTimestamp(2, thirdMonthDay);
							pstmt.setTimestamp(3, prdtoDateTmstmp);
							pstmt.setString(4, custCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								calRate = rs.getDouble("rate__stduom");
								System.out.println("calRate>>>>>> :" + calRate);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (calRate == 0)
							{
								sql = "select price_list from customer where cust_code =? ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, custCode);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									priceList = checkNull(rs1.getString("price_list"));
									System.out.println("priceList edit :" + priceList);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								//Commented by Manoj dtd 26/10/2016
								//openingRate = discmn.pickRate(priceList, sysDatetemp, itemCode, conn);
								//Changed by Manoj dtd 26/10/2016
								//sysDate = genericUtility.getValidDateString( sysDate , genericUtility.getApplDateFormat() , genericUtility.getDBDateFormat());
								sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, SYSDATE , ? ) FROM DUAL ";
								pstmt1 = conn.prepareStatement( sql );
								pstmt1.setString( 1, priceList );
								pstmt1.setString( 2, sysDate );
								pstmt1.setString( 3, genericUtility.getDBDateFormat() );
								pstmt1.setString( 2, itemCode );
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									calRate = rs1.getDouble(1);
									System.out.println("calRate-----------> [" +calRate+ "]");				
								}
								if (rs1 != null)
								{
									rs1.close();
									rs1 = null;
								}
								if (pstmt1 != null)
								{
									pstmt1.close();
									pstmt1 = null;
								}
							}
					}
					calRate=getRequiredDcml(calRate,3);
					closingValue=getRequiredDcml(clStock*calRate,3);
				}*/
							if (clStock > 0) 
							{
								closingRate = closingValue / clStock;
							}
							else 
							{
								closingRate = 0.0;
							}
							closingRate=getRequiredDcml(closingRate,3);//rate
							System.out.println("closingRate@@@@@@@@@"+closingRate);
							
							System.out.println("transitInvQty>>"+transitInvQty+"transitInvValue>>"+transitInvValue);
							System.out.println("recptInvQty>>"+recptInvQty+"recptInvValue>>"+recptInvValue);
							System.out.println("transitFreeQty>>"+transitFreeQty+"transitFreeValue>>"+transitFreeValue);
							System.out.println("recptFreeQty>>"+recptFreeQty+"recptFreeValue>>"+recptFreeValue);
							System.out.println("recptRetQty>>"+recptRetQty+"recptRetVal>>"+recptRetVal);
							System.out.println("billRetQtyBonusQty>>"+billRetQtyBonusQty+"billRetQtyBonusVal>>"+billRetQtyBonusVal);
							System.out.println("transitReplQty>>"+transitReplQty+"transitReplVal>>"+transitReplVal);
							System.out.println("recptReplQty>>"+recptReplQty+"recptReplVal>>"+recptReplVal);
							System.out.println("opStkDom>>"+opStkDom+"clStock>>>"+clStock);

							sretQtyAllTot= recptRetQty - recptReplQty - billRetQtyBonusQty ;
							System.out.println("sretQtyAllTot>>"+sretQtyAllTot);
							primarySalesAll= recptInvQty - (sretQtyAllTot );
							System.out.println("primarySalesAll>>"+primarySalesAll);
							
							grossSecondaryQty=opStkDom+recptInvQty+recptReplQty+recptFreeQty-recptRetQty-clStock;//sales__org
							System.out.println("grossSecondaryQty>>>>>>"+grossSecondaryQty);
							
							formulaValue=0;
							System.out.println("formulaValue>>>>>"+formulaValue);
							
							netSecondarySalesValue=(opStkDom*rateOld)+recptInvValue+recptReplVal-recptRetVal-closingValue;
							netSecondarySalesValue=getRequiredDcml(netSecondarySalesValue,3);//sales_value
							System.out.println("netSecondarySalesValue>>>>>"+netSecondarySalesValue);
							
							grossSecondarySalesValue=(opStkDom*rateOld)+recptInvValue+recptReplVal+recptFreeValue-recptRetVal-closingValue;
							grossSecondarySalesValue=getRequiredDcml(grossSecondarySalesValue,3);
							System.out.println("grossSecondarySalesValue>>>>>"+grossSecondarySalesValue);
							
							if(grossSecondaryQty>0)
							{
								grossSecondaryRate = grossSecondarySalesValue / grossSecondaryQty;
							}else
							{
								grossSecondaryRate=0.0;
							}
							if(grossSecondaryRate<0)
							{
								grossSecondaryRate=0.0;
							}
							else
							{
								grossSecondaryRate=getRequiredDcml(grossSecondaryRate,3);//rate__org
							}
							System.out.println("grossSecondaryRate>>>"+grossSecondaryRate);
							//End by chandrashekar on 29-dec-2015
							salesQtyCal = opStkDom + (recptInvQty + recptReplQty) - (recptRetQty + retQtyFreeDom) - clStock;//sales
							salesQtyCal=getRequiredDcml(salesQtyCal,3);
							System.out.println("salesQtyCal>>>"+salesQtyCal);
							
							
							opValue=getRequiredDcml(opStkDom*rateOld,3);//op_value
							
					/*}
					else
					{
						clStock=0.0;//cl_stock
					}*/
					
					
					sql = "update cust_stock_det set rcp_val=? , rcp_repl_val=? , rcp_free_val=? , ret_val=? , ret_free_val=? , transit_bill_val=? , " +
							" transit_repl_val=? , transit_free_val=? , " +
							" purc_rcp=? ,purc_rcp__repl=? ,purc_rcp__free=? ,purc_ret=? ,purc_ret__free=? , " +
							" transit_qty=? ,transit_qty__repl=? ,transit_qty__free=? ," +
							" sales=? ,rate=? ,rate__org=? ,sales__org=? ,cl_value=? ,sales_value=? ,op_value=? ,primary_sales=? ,op_stock=? , " +
							" CHG_DATE=SYSDATE ,CHG_TERM=? ,CHG_USER=? " +
							" where tran_id=? and item_code=? ";
					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setDouble(1, recptInvValue);//rcp_val
					pstmt2.setDouble(2, recptReplVal);//rcp_repl_val
					pstmt2.setDouble(3, recptFreeValue);//rcp_free_val
					pstmt2.setDouble(4, recptRetVal);//ret_val
					pstmt2.setDouble(5, billRetQtyBonusVal);//ret_free_val
					pstmt2.setDouble(6, transitInvValue);//transit_bill_val
					pstmt2.setDouble(7, transitReplVal);//transit_repl_val
					pstmt2.setDouble(8, transitFreeValue);//Transit_free_val
					pstmt2.setDouble(9, recptInvQty);//purc_rcp
					pstmt2.setDouble(10, recptReplQty);//purc_rcp__repl
					pstmt2.setDouble(11, recptFreeQty);//purc_rcp__free
					pstmt2.setDouble(12, recptRetQty);//purc_ret
					pstmt2.setDouble(13, billRetQtyBonusQty);//purc_ret__free
					pstmt2.setDouble(14, transitInvQty);//transit_qty
					pstmt2.setDouble(15, transitReplQty);//transit_qty__repl
					pstmt2.setDouble(16, transitFreeQty);//transit_qty__free
					pstmt2.setDouble(17, salesQtyCal );//sales
					pstmt2.setDouble(18, closingRate );//rate
					pstmt2.setDouble(19, grossSecondaryRate );//rate__org
					pstmt2.setDouble(20, grossSecondaryQty );//sales__org
					pstmt2.setDouble(21, closingValue );//cl_value
					pstmt2.setDouble(22, netSecondarySalesValue );//sales_value
					pstmt2.setDouble(23, opValue );//op_value
					pstmt2.setDouble(24, primarySalesAll );//primary_sales
					pstmt2.setDouble(25, opStkDom );//op_stock
					pstmt2.setString(26, chgTerm);
					pstmt2.setString(27, loginCode);
					pstmt2.setString(28, tranId);
					pstmt2.setString(29, itemCode);
					UpdCnt = pstmt2.executeUpdate();
					if(UpdCnt>0)
					{
						System.out.println("No of record updated:"+UpdCnt);
						System.out.println("Data updated for tran_id:"+tranId+" and item_code>>"+itemCode);
						chkCnt++;
					}
					System.out.println("Records done>>>>"+chkCnt+" >>>upto tranId"+tranId);
					pstmt2.close();
					pstmt2 = null;
					
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		}// try end
		catch (Exception e) 
		{
			try 
			{
				System.out.println("inside");
				errString = itmDBAccessEJB.getErrorString("", "VTES3RLBCK","", "", conn);
				conn.rollback();
			}
			catch (Exception d) 
			{
				System.out.println("Exception : Es3HDataUpdPrc =>"+ d.toString());
				d.printStackTrace();
			}
			System.out.println("Exception :Es3HDataUpdPrc :process(String xmlString2, String xmlString2, String windowName, String xtraParams):"+ e.getMessage() + ":");
			e.printStackTrace();
			errString = e.getMessage();
			return errString;
		}
		finally 
		{
			System.out.println("Closing Connection....");
			try {
				if (errString == null) {
					System.out.println("Connection Commited");
					errString = itmDBAccessEJB.getErrorString("", "VTES3SUCSS","", "", conn);
					conn.commit();
				}
				else if (errString != null) 
				{
					System.out.println("Connection is rollback");
					errString = itmDBAccessEJB.getErrorString("", "VTES3RLBCK","", "", conn);
					conn.rollback();
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

	private String checkNull(String input) 
	{
		input = input==null ? "" : input.trim();
		return input;
	}

	public double getRequiredDcml(double actVal, int prec) 
	{
		double value = 0.0;
		String fmtStr = "############0";
		if (prec > 0) {
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		if (decFormat.format(actVal) != null
				&& decFormat.format(actVal).trim().length() > 0) {
			value = Double.parseDouble(decFormat.format(actVal));
		} else {
			value = 0.00;
		}
		return value;
	}
	class ClosingStockBean
	{
		private double opStock;
		private double openingRate;
		private double openingRateOrg;
		
		public double getOpStock() {
			return opStock;
		}
		public void setOpStock(double opStock) {
			this.opStock = opStock;
		}
		public double getOpeningRate() {
			return openingRate;
		}
		public void setOpeningRate(double openingRate) {
			this.openingRate = openingRate;
		}
		public double getOpeningRateOrg() {
			return openingRateOrg;
		}
		public void setOpeningRateOrg(double openingRateOrg) {
			this.openingRateOrg = openingRateOrg;
		}
	}
}// END OF EJB