 package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.SalesOrderIC;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
// added for ejb3


@Stateless // added for ejb3
public class SorderAct extends ActionHandlerEJB implements SorderActLocal, SorderActRemote
{
	E12GenericUtility genericUtility= new E12GenericUtility();
	DistCommon distCommon = new DistCommon();//added by nandkumar gadkari on 11/06/19
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

    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		String  retString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			System.out.println("actionType ::::::"+actionType);
			System.out.println("XML String ::::::"+xmlString);
			System.out.println("objContext ::::::"+objContext);
			System.out.println("xtraParams ::::::"+xtraParams);
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);
			}
			if (actionType.equalsIgnoreCase("Default"))
			{
				System.out.println("XML String ::::::"+xmlString);
				retString = actionDefault(dom, xtraParams);
			}
			//Added by Rohini Telang on 10/02/2021 [Start]
			if (actionType.equalsIgnoreCase("Quotation"))
			{
				System.out.println("actionType :@@@"+actionType);
				retString = actionQuotation(dom, objContext, xtraParams) ;
			}
			//Added by Rohini Telang on 10/02/2021 [End]
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Sorder :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionDefault actionHandler"+retString);
	    return (retString);
	}
	//Added by Rohini Telang on 10/02/2021 [Start]
	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			System.out.println("xmlString ::"+xmlString);
			System.out.println("xmlString1 ::"+xmlString1);
			System.out.println("selDataStr ::"+selDataStr);

			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString);				
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);				
			}
			if(selDataStr != null && selDataStr.length() > 0)
			{
				selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
			}
			System.out.println("actionType:"+actionType+":");
						
			if (actionType.equalsIgnoreCase("Quotation"))
			{
                retString = quotationTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :SorderAct :actionHandlerTransform(String xmlString):" +e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from SorderAct : actionHandlerTransform"+retString);
	    return retString;
	}
	//Added by Rohini Telang on 10/02/2021 [End]
	private String actionDefault(Document dom, String xtraParams) throws RemoteException , ITMException
	{
		String creditTerm = "";
		String errCode = "";
		String errString = "";
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		String detCnt = "0";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Jiten
			if (detCnt == null || detCnt.trim().length() == 0)
			{
				detCnt = "0";
			}
			if(detCnt.equals("0"))
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				creditTerm = genericUtility.getColumnValue("cr_term",dom);	
				System.out.println("creditTerm :"+creditTerm);
				sql="SELECT "
						+"CRTERM_REL.REL_AGNST, "   
						+"CRTERM_REL.AMT_TYPE, "   
						+"CRTERM_REL.REL_AMT, "   
						+"CRTERM_REL.REL_AFTER, "   
						+"CRTERM_REL.ADJ_METHOD, "   
						+"CRTERM_REL.ACCT_CODE, "   
						+"CRTERM_REL.CCTR_CODE, "   
						+"CRTERM_REL.PROC_CODE, "   
						+"CRTERM_REL.NO_RELEASE, "   
						+"CRTERM_REL.LAST_RELEASE "   
					+"FROM CRTERM_REL "  
					+"WHERE CRTERM_REL.CR_TERM = '"+ creditTerm +"'";
				System.out.println("Sorder SQL :="+sql);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<rel_agnst>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</rel_agnst>\r\n");
					valueXmlString.append("<amt_type>").append("<![CDATA[").append(rs.getString(2)).append("]]>").append("</amt_type>\r\n");
					valueXmlString.append("<rel_amt>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</rel_amt>\r\n");
					valueXmlString.append("<rel_after>").append("<![CDATA[").append(rs.getString(4)).append("]]>").append("</rel_after>\r\n");
					valueXmlString.append("<adj_method>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</adj_method>\r\n");
					valueXmlString.append("<acct_code>").append("<![CDATA[").append(rs.getString(6)).append("]]>").append("</acct_code>\r\n");
					valueXmlString.append("<cctr_code>").append("<![CDATA[").append(rs.getString(7)).append("]]>").append("</cctr_code>\r\n");
					valueXmlString.append("<proc_code>").append("<![CDATA[").append(rs.getString(8)).append("]]>").append("</proc_code>\r\n");
					valueXmlString.append("<no_release>").append("<![CDATA[").append(rs.getString(9)).append("]]>").append("</no_release>\r\n");
					valueXmlString.append("<last_release>").append("<![CDATA[").append(rs.getString(10)).append("]]>").append("</last_release>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				stmt.close();
				valueXmlString.append("</Root>\r\n");			
			}
			else
			{
				errCode = "VTDETCNT";
			}
			if (!errCode.equals(""))
			{
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				return errString;
			}
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Sorder : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Sorder : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
			conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	@Override
	//public String k(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException 
	public String getFreeSchemeaction(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;

		String  retString = null;
		try
		{
			System.out.println("xmlString :::"+xmlString);
			System.out.println("xmlString1 :::"+xmlString1);
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			System.out.println("actionType:"+actionType+":");
						
			if (actionType.equalsIgnoreCase("sorderScheme"))
			{
				retString = getFreeSchemeaction(dom, dom1, objContext, xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Porcp :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from Porcp : actionHandlerTransform"+retString);
	    return retString;
	
	}

	@Override
	public String getFreeSchemeaction(Document dom, Document dom1,String objContext, String xtraParams)throws ITMException 
	{
		System.out.println("getFreeSchemeAction is calling.............");
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String sql = "",newDomXml = "", tranID ="",outSql="";
		String detCnt = "0";
		int noOfDetails = 0,domID1=0;
		Timestamp TranDateDet = null;
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null, pstmtOut=null,pstmtIn=null;
		ResultSet rs = null ,rs1 = null,rs2 = null,rs3 = null,rs4=null;
		int count = 0 , domID = 0;
		String itemCode = "",schemeCode = "",itemDescription="",updateFlag="";
		NodeList detailList = null;
		Node currDetail = null, headerNode = null;
		String itmCode="",s = "",s1 = "",quantity="",schemeType="",schAllowence="",siteCode="",s12="",schemeDescr="",dsp_date="",nature="";
		double qty = 0.0, freeQuantity=0.0;
		double purcBase=0.0;
		double schAllowence1=0.0,discount=0.0,purRate=0.0;
		Iterator<String> itr = null;
		List<String> itemList = new ArrayList<String>();
		List<String> getSchemeList = new ArrayList<String>();
		HashMap<String,Double> itemQuantityMap = new HashMap<String,Double>();
		HashMap<String,Double> itemRateMap = new HashMap<String,Double>();
		HashMap<String,List<String>> getSchemeItemList = new HashMap<String,List<String>>();
		//HashMap<String,List<String>> freeItemInfoMap = new HashMap<String,List<String>>();//Added by sarita on 15JAN2018
		List<String> itemCodeListScheme = new ArrayList<String>();
		List<String> freeItemList = new ArrayList<String>();
		List<String> freeItemListApplcbleType0 = new ArrayList<String>();
		List<String> freeItemListApplcbleType1 = new ArrayList<String>();
		List<String> freeItemListApplcbleType2 = new ArrayList<String>();
		List<String> finalFreeItems = new ArrayList<String>();
		HashMap<String,List<String>> freeItemInfoMap = new HashMap<String,List<String>>();
		HashMap<String,String> detai1ValueMap = new HashMap<String,String>();
		List<String> schemeInfo = null;
		Set<String> finalApplicableSchemeSet = new HashSet<String>();
		String itemValues = "",itemCodeSchm = "",rate = "",SaleOrder = "",schemeCode1 = "",siteCode1="",stateCodeDlv="",countCodeDlv="";
		double finRate =0.0;
		String freeBalOrd="",pointBaseSchemeXml="",schemeCodeList="";
		List<String> getSchemeListPur = new ArrayList<String>();
		
		try
		{
			String custcodedlv="",itemSer="",custCode="",orderDate="";
			conn = getConnection();
			headerNode = dom1.getElementsByTagName("Detail1").item(0);
			custcodedlv = checkNull(genericUtility.getColumnValueFromNode("cust_code__dlv", headerNode));
			itemSer = checkNull(genericUtility.getColumnValueFromNode("item_ser", headerNode));
			dsp_date = checkNull(genericUtility.getColumnValueFromNode("due_date", headerNode));
			custCode = checkNull(genericUtility.getColumnValueFromNode("cust_code", headerNode));
			orderDate = checkNull(genericUtility.getColumnValueFromNode("order_date", headerNode));
			siteCode1 = checkNull(genericUtility.getColumnValueFromNode("site_code", headerNode));
		//	state code and count code aaded by nandkumar gadkari on 18/03/19
			stateCodeDlv = checkNull(genericUtility.getColumnValueFromNode("state_code__dlv", headerNode));
			countCodeDlv = checkNull(genericUtility.getColumnValueFromNode("count_code__dlv", headerNode));
			System.out.println("custcodedlv["+custcodedlv+"]" + "itemSer["+itemSer+"]" + "dsp_date["+dsp_date+"]" + "custCode["+custCode+"]" + "orderDate["+orderDate+"]" + "siteCode1["+siteCode1+"]");
			detai1ValueMap.put("cust_code__dlv", custcodedlv);
			detai1ValueMap.put("item_ser", itemSer);
			detai1ValueMap.put("due_date", dsp_date);
			detai1ValueMap.put("cust_code", custCode);
			detai1ValueMap.put("order_date", orderDate);
			detai1ValueMap.put("site_code", siteCode1);
			
			
			detailList = dom1.getElementsByTagName("Detail2");
			noOfDetails = detailList.getLength();
			//updateFlag = getAttributeVal(dom1,"updateFlag");	//Added by sarita on 15JAN2018		
			System.out.println("noOfDetails >>"+noOfDetails);

			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				currDetail = detailList.item(ctr);
				
				itemCode = checkNull(genericUtility.getColumnValueFromNode("item_code__ord", currDetail));
				quantity = checkNull(genericUtility.getColumnValueFromNode("quantity", currDetail));	
				siteCode = checkNull(genericUtility.getColumnValueFromNode("site_code", currDetail));
				SaleOrder = checkNull(genericUtility.getColumnValueFromNode("sale_order", currDetail));
				rate = checkNull(genericUtility.getColumnValueFromNode("rate", currDetail));
				
				System.out.println("itemCode["+itemCode+"]" + "quantity["+quantity+"]" + "siteCode["+siteCode+"]" + "SaleOrder["+SaleOrder+"]" + "rate["+rate+"]");
				
				//dsp_date = getDueDate(SaleOrder, conn);
				/*TranDateDet = Timestamp.valueOf(genericUtility.getValidDateString(dsp_date,
						 (genericUtility.getDBDateFormat()  + " 00:00:00.0"), genericUtility.getApplDateFormat())+" 00:00:00.0");*/
				TranDateDet = getTimeStamp(dsp_date);
				//Added by sarita on 19/JAN/2018
				if(quantity != null && quantity.trim().length() > 0){
				qty = Double.valueOf(checkDouble(quantity));}
				if(rate != null && rate.trim().length() > 0){
				purRate = Double.valueOf(checkDouble(rate));}
				
				itemList.add(itemCode);
				
				if(itemQuantityMap.containsKey(itemCode))
				{
					if(quantity != null)
					{
						qty =  qty + Double.valueOf(itemQuantityMap.get(itemCode));
					}
					itemQuantityMap.put(itemCode.trim(), qty);
					//itemQuantityMap.put(itemCode.trim(), purRate);
				}
				else
				{
					itemQuantityMap.put(itemCode.trim(), qty);
				}
				//Added by sarita on 02FEB2018
				if(itemRateMap.containsKey(itemCode))
				{
					if(rate != null)
					{
						purRate =  purRate + Double.valueOf(itemRateMap.get(itemCode));
					}
					itemRateMap.put(itemCode.trim(), purRate);
					//itemQuantityMap.put(itemCode.trim(), purRate);
				}
				else
				{
					itemRateMap.put(itemCode.trim(), purRate);
				}
			}	
			
			itr = itemList.iterator();
			while(itr.hasNext())
			{
			  	s = s + "'"+(String) itr.next() + "'"+",";
			}
			if(s != null && s.length() != 0)
			{
				s = s.substring(0,s.length()-1);
			}
			//Added by sarita on 18/JAN/2018
			else
			{
				s = "''";
			}
			System.out.println("Purchase Item Code are :::"+s);
			//Getting Distinct Scheme Code for Purchased Item -------------------------------  
			sql = "select distinct(scheme_code),item_code from sch_pur_items where item_code in ("+s+")";
			System.out.println("sql ==="+sql);   		  
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next())
			{				
				schemeCode1 = rs.getString("scheme_code");	
				//getSchemeList.add(schemeCode);
				itmCode = rs.getString("item_code");	
				System.out.println("Applicable Scheme for Purchase Item ["+itmCode+"]" + " is === Scheme Code ["+schemeCode1+"]");
				getSchemeListPur.add(schemeCode1);
			}
			if(pstmt != null)
			{
				pstmt.close(); 
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			} 
			//	added by nandkumar gadkari on 12/06/19
			schemeCodeList = "";
			itr = getSchemeListPur.iterator();
			while(itr.hasNext())
			{
				schemeCodeList = schemeCodeList + "'"+(String) itr.next() + "'";
				if(itr.hasNext())
				{
					schemeCodeList=schemeCodeList+ ",";
				}
			}
			schemeCodeList=schemeCodeList == null || schemeCodeList.trim().length() == 0 ? "''"	: schemeCodeList;	
			sql = "select distinct(a.scheme_code) from scheme_applicability a,scheme_applicability_det b"
						+ " where a.scheme_code	= b.scheme_code"
						+ " and a.prod_sch = 'Y'"
						+ " and a.app_from<= ?" + " and a.valid_upto>= ?" 
						+ " and (b.site_code=?"
						+ " or b.state_code = ?" + " or b.count_code= ?) and a.scheme_code in ("+schemeCodeList+")";// condition change by nandkumar gadkari on 12/06/19
				pstmt2 = conn.prepareStatement(sql);
				//pstmt2.setString(1, itmCode);
				pstmt2.setTimestamp(1, TranDateDet);
				pstmt2.setTimestamp(2, TranDateDet);
				//pstmt2.setString(3, schemeCode1);
				pstmt2.setString(3, siteCode);
				pstmt2.setString(4, stateCodeDlv);//set by nandkumar gadkari on 18/03/19
				pstmt2.setString(5, countCodeDlv);
				rs2 = pstmt2.executeQuery();
				while(rs2.next())
				{
					schemeCode = rs2.getString("scheme_code");
					//getSchemeList.add(schemeCode); commented and aaded by nandkumar gadkari on 12/06/19
					finalApplicableSchemeSet.add(schemeCode);
					//System.out.println("All Applicable Schemes in scheme_applicability Table ["+getSchemeList+"]");
					//System.out.println("schemeCode["+schemeCode+"]" + "itmCode["+itmCode+"]" + "\t");
				}
				if(pstmt2 != null)
				{
					pstmt2.close(); 
					pstmt2 = null;
				}
				if(rs2 != null)
				{
					rs2.close();
					rs2 = null;
				} 
				
				   //checking Applicability of schemes
                   /*if(getSchemeList.contains(schemeCode1))
                   {
                	   if(getSchemeItemList.containsKey(schemeCode1))
                	   {
                		   //System.out.println("Inside 1");
                		   schemeInfo = getSchemeItemList.get(schemeCode1);	
                		   schemeInfo.add(itmCode.trim());
                		   finalApplicableSchemeSet.add(schemeCode1);
                		   getSchemeItemList.put(schemeCode1,schemeInfo);
                	   }
                	   else
                	   {
                		   //System.out.println("Inside 2");
                		   schemeInfo = new ArrayList<String>();
                		   schemeInfo.add(itmCode.trim());
                		   finalApplicableSchemeSet.add(schemeCode1);
                		   //System.out.println("schemeInfo1"+schemeInfo);
                		   getSchemeItemList.put(schemeCode1,schemeInfo);	
                	   }
                	   
                   }*/// commented by nandkumar gadkari on 12/06/19
				
				
				System.out.println("Applicable Scheme code and Item code are :::"+getSchemeItemList);
				System.out.println("Scheme Applicable for Purchased Item in ArrayList ::["+finalApplicableSchemeSet+"]");
			/*}
			if(pstmt != null)
			{
				pstmt.close(); 
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			} */
			
			//itr = getSchemeList.iterator();			
			//if(getSchemeItemList.size() > 0) condition chnage by nandkumar gadkari on 12/06/19
				if(getSchemeListPur.size() > 0)	
			{
				//List<String> finalApplicableSchemeList = getSchemeItemList.get("schemeInfo");
				System.out.println("finalApplicableSchemeSet :: ["+finalApplicableSchemeSet+"]");
				//itr = getSchemeItemList.get("schemeInfo");
				itr = finalApplicableSchemeSet.iterator(); 
				while(itr.hasNext())
				{
					s1 = s1 + "'"+(String) itr.next() + "'"+",";
				}
				//System.out.println("s1 is "+s1);
				if(s1 != null && s1.length() != 0)
				{
					s1 = s1.substring(0,s1.length()-1);
			    	// System.out.println("s1 is "+s1);
				}
			}
						
			//Working on Scheme Applicability...
			System.out.println("Scheme Applicability...."+getSchemeItemList.size());
			
			if(finalApplicableSchemeSet.size() > 0)
			{
				sql = "select scheme_code,descr,scheme_type,purc_base,sch_allowence,discount from sch_group_def where scheme_code in ("+s1+")";
				System.out.println("sql >> ["+sql+"]");
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1,s1);			
				rs = pstmt.executeQuery();
				while(rs.next())
				{	
					domID++;
					System.out.println("DomId is ===="+domID);
					schemeCode = rs.getString("scheme_code");
					schemeType = rs.getString("scheme_type");
					schemeDescr = rs.getString("descr");
					purcBase = Double.valueOf(checkDouble(rs.getString("purc_base")));
					schAllowence1= Double.valueOf(checkDouble(rs.getString("sch_allowence")));
					discount = Double.valueOf(checkDouble(rs.getString("discount")));
					System.out.println("Applicable schemeType ["+schemeType+"]" + "for schemeCode ["+schemeCode+"]" + "purcBase ["+purcBase+"]" +"schAllowence1 ["+schAllowence1+"]" +"Scheme Description ["+schemeDescr+"]" + "Discount is :::["+discount+"]");
					
					if("0".equalsIgnoreCase(schemeType))
					{
						double xmlQty = 0.0;
						System.out.println("Applied Scheme Type is ::"+schemeType);
						itemCodeListScheme = getSchemeItemList.get(schemeCode);
						System.out.println("Purchased Item Code for Scheme Type 0 is "+itemCodeListScheme);
						System.out.println("Total Purchased Item for Scheme Type 0 is "+itemCodeListScheme.size());
						if(itemCodeListScheme.size() > 0)
						{
							for(int cntr=0; cntr<itemCodeListScheme.size();cntr++)
							{	
								itemCodeSchm = itemCodeListScheme.get(cntr);
								xmlQty +=  itemQuantityMap.get(itemCodeSchm);
								freeItemListApplcbleType0.add(itemCodeSchm);
							}

							System.out.println("Quantity in XML is ::["+xmlQty+"]" + "\t" + "Quantity Applicable for Scheme is ::["+purcBase+"]");

							if(xmlQty >= purcBase)
							{
								double freeQty = (int)(xmlQty / purcBase); System.out.println("freeQty is ::["+freeQty+"]");
								freeQty = freeQty * schAllowence1;
								System.out.println("Items Applicable for Free Schemes :::"+itemCodeSchm + "free quantity"+(int)freeQty);//Added by sarita on 15JAN2018
								itemValues = getItemCode(freeItemListApplcbleType0);	
								System.out.println("Item Values for Scheme Type 0 is :::["+itemValues+"]");
								//sql = "SELECT S.item_code FROM SCH_PUR_ITEMS P ,SCH_OFFER_ITEMS S WHERE P.SCHEME_CODE=S.SCHEME_CODE AND P.ITEM_CODE='"+itemCodeSchm+"'";
								sql = "SELECT S.item_code FROM SCH_PUR_ITEMS P ,SCH_OFFER_ITEMS S WHERE P.SCHEME_CODE=S.SCHEME_CODE AND P.ITEM_CODE in ("+itemValues+")";
								System.out.println("sql :::::"+sql);
								pstmt1 = conn.prepareStatement(sql);
								//pstmt.setString(1,itemCodeSchm);			
								rs1 = pstmt1.executeQuery();
								freeItemListApplcbleType0.clear();
								freeItemList.clear();
								freeItemInfoMap.clear();
								//System.out.println("Values in list after clear for Scheme 1:::"+freeItemList + "\t" +"freeItemInfoMap ["+freeItemInfoMap+"] ");
								while(rs1.next())
								{
									String freeItem = rs1.getString("item_code");										
									freeItemList.add(freeItem);
									freeItemInfoMap.put(itemCodeSchm, freeItemList);
								}
								System.out.println("Free Item List ::::["+freeItemList+"]" + "freeItemInfoMap ::::["+freeItemInfoMap+"]");
								if(pstmt1 != null)
								{
									pstmt1.close(); 
									pstmt1 = null;
								}
								if(rs1 != null)
								{
									rs1.close();
									rs1 = null;
								}
								finalFreeItems = freeItemInfoMap.get(itemCodeSchm);	
								System.out.println("Final List of Free Items for ::::Scheme Type 0 is "+finalFreeItems);
								//s12 = returnFinalFreeItemDataUsingList(finalFreeItems, conn,schemeType,schemeCode,schemeDescr);
								s12 = returnFinalFreeItemDataUsingList(freeItemList, conn,schemeType,schemeCode,schemeDescr,discount,freeQty,itemCodeSchm,detai1ValueMap);
								//System.out.println("***********"+s12);
								valueXmlString.append(s12);					
							}
							else
							{
								System.out.println("Scheme is NOT applicable for ::"+itemCodeSchm + "Purchased Item quantity::"+xmlQty +"is NOT greater  than or equal to applicable quantity"+purcBase+"for scheme "+schemeCode);
								
							}
						}
					}//end of if block for type0 
					if("1".equalsIgnoreCase(schemeType))
					{
						System.out.println("Applied Scheme Type is ::"+schemeType);
						itemCodeListScheme = getSchemeItemList.get(schemeCode);
						System.out.println("Purchased Item Code for Scheme Type 1 is "+itemCodeListScheme);
						System.out.println("Total Purchased Item for Scheme Type 1 is "+itemCodeListScheme.size());
						if(itemCodeListScheme.size() > 0)
						{
							double purcAmt = 0.0;
							for(int cntr=0; cntr<itemCodeListScheme.size();cntr++)
							{	
								double xmlQty = 0.0;
								itemCodeSchm = itemCodeListScheme.get(cntr);
								finRate = itemRateMap.get(itemCodeSchm);
								xmlQty =  itemQuantityMap.get(itemCodeSchm);
								freeItemListApplcbleType1.add(itemCodeSchm);
								purcAmt += (finRate * xmlQty);
							}
							
																
							if(itemQuantityMap.containsKey(itemCodeSchm))
							{
								double xmlQty =  itemQuantityMap.get(itemCodeSchm);
								//finRate = itemQuantityMap.get(itemCodeSchm);
								//ContractNo=salesOrderIC.getContract(detai1ValueMap.get("site_code"), detai1ValueMap.get("cust_code"), getTimeStamp(detai1ValueMap.get("order_date")), itemCodeSchm, "", "", conn);
								//SalesPriceList = distCommon.getSalesPriceList(detai1ValueMap.get("cust_code"),detai1ValueMap.get("cust_code__dlv"),detai1ValueMap.get("site_code"),ContractNo,itemCodeSchm,detai1ValueMap.get("order_date"),conn);
								System.out.println("Applicable Rate for Scheme Type 1 is ==["+finRate+"]"+"Quantity in XML is ::["+purcAmt+"]" + "\t" +"Purchased Items ["+itemCodeSchm+"]"+ "Quantity Applicable for Scheme is ::["+purcBase+"]" + " Scheme Allowence for Scheme is ::["+schAllowence1+"]");
								
								if(purcAmt >= purcBase)
								{
									//double freeQty = (int)(xmlQty / purcBase); System.out.println("freeQty is ::["+freeQty+"]");
									//freeQty = freeQty * schAllowence1;
									//Added by sarita on 02FEB2018
									//double chargedQty = (finRate * xmlQty);
									int freeQty = (int) ((purcAmt * schAllowence1)/purcBase);
									System.out.println("purcAmt ["+purcAmt+"]" + "freeQty ["+freeQty+"]");
									System.out.println("Items Applicable for Free Schemes :::"+itemCodeSchm + "free quantity"+(int)freeQty);
									freeItemListApplcbleType1.add(itemCodeSchm);										
									itemValues = getItemCode(freeItemListApplcbleType1);
									System.out.println("Item Values for Scheme Type 1 is :::["+itemValues+"]");
									sql = "SELECT S.item_code FROM SCH_PUR_ITEMS P ,SCH_OFFER_ITEMS S WHERE P.SCHEME_CODE=S.SCHEME_CODE AND P.ITEM_CODE in ("+itemValues+")";
									//System.out.println("sql :::::"+sql);
									pstmt1 = conn.prepareStatement(sql);
									//pstmt.setString(1,itemCodeSchm);			
									rs1 = pstmt1.executeQuery();
									freeItemListApplcbleType1.clear();
									freeItemList.clear();
									freeItemInfoMap.clear();
									//System.out.println("Values in list after clear for Scheme 1:::"+freeItemList);
									while(rs1.next())
									{
										String freeItem = rs1.getString("item_code");										
										freeItemList.add(freeItem);
										freeItemInfoMap.put(itemCodeSchm, freeItemList);
									}
									if(pstmt1 != null)
									{
										pstmt1.close(); 
										pstmt1 = null;
									}
									if(rs1 != null)
									{
										rs1.close();
										rs1 = null;
									}
									finalFreeItems = freeItemInfoMap.get(itemCodeSchm);		
									System.out.println("Final List of Free Items for ::::Scheme Type 1 is "+finalFreeItems);
									s12 = returnFinalFreeItemDataUsingList(finalFreeItems, conn,schemeType,schemeCode,schemeDescr,discount,freeQty,itemCodeSchm,detai1ValueMap);
									//System.out.println("***********"+s12);										
									valueXmlString.append(s12);					
								}
								else
								{
									
									
									System.out.println("Scheme is NOT applicable for ::"+itemCodeSchm + "Purchased Item quantity::"+xmlQty +"is NOT greater  than or equal to applicable quantity"+purcBase+"for scheme "+schemeCode);
								}
							}
						}
					
					}//end of if block for type1//Added by sarita on 15JAN2018 
					if("2".equalsIgnoreCase(schemeType))
					{
						System.out.println("Applied Scheme Type is ::"+schemeType);
						itemCodeListScheme = getSchemeItemList.get(schemeCode);
						System.out.println("Purchased Item Code for Scheme Type 2 is "+itemCodeListScheme);
						System.out.println("Total Purchased Item for Scheme Type 2 is "+itemCodeListScheme.size());
						if(itemCodeListScheme.size() > 0)
						{
							double xmlQty = 0.0;
							/*for(int cntr=0; cntr<itemCodeListScheme.size();cntr++)
							{	
								double xmlQty = 0.0;
								itemCodeSchm = itemCodeListScheme.get(cntr);
								finRate = itemRateMap.get(itemCodeSchm);
								xmlQty =  itemQuantityMap.get(itemCodeSchm);
								freeItemListApplcbleType2.add(itemCodeSchm);
								purcAmt += (finRate * xmlQty);
							}*/
							
							for(int cntr=0; cntr<itemCodeListScheme.size();cntr++)
							{	
								itemCodeSchm = itemCodeListScheme.get(cntr);
								xmlQty +=  itemQuantityMap.get(itemCodeSchm);
								freeItemListApplcbleType2.add(itemCodeSchm);
							}


							if(itemQuantityMap.containsKey(itemCodeSchm))
							{
								//double xmlQty =  itemQuantityMap.get(itemCodeSchm);
								finRate = itemRateMap.get(itemCodeSchm);
								System.out.println("Applicable Rate for Scheme Type 2 is ==["+finRate+"]"+"Quantity in XML is ::["+xmlQty+"]" + "\t" +"Purchased Items ["+itemCodeSchm+"]"+ "Quantity Applicable for Scheme is ::["+purcBase+"]");

								if(xmlQty >= purcBase)
								{
									//double freeQty = (int)(xmlQty / purcBase); System.out.println("freeQty is ::["+freeQty+"]");
									//freeQty = freeQty * schAllowence1;
									//Added by sarita on 2FEB2018
									//double chargedQty = (finRate * xmlQty);
									//int freeQty = (int) ((purcAmt * schAllowence1)/purcBase);
									double freeQty = (xmlQty / purcBase); System.out.println("freeQty is ::["+freeQty+"]");
									freeQty = freeQty * schAllowence1;
									System.out.println("xmlQty ["+xmlQty+"]" + "freeQty ["+freeQty+"]");
									System.out.println("Items Applicable for Free Schemes :::"+itemCodeSchm + "free quantity"+(int)freeQty);
									freeItemListApplcbleType2.add(itemCodeSchm);										
									itemValues = getItemCode(freeItemListApplcbleType2);
									System.out.println("Item Values for Scheme Type 2 is :::["+itemValues+"]");
									sql = "SELECT S.item_code FROM SCH_PUR_ITEMS P ,SCH_OFFER_ITEMS S WHERE P.SCHEME_CODE=S.SCHEME_CODE AND P.ITEM_CODE in ("+itemValues+")";
									//System.out.println("sql :::::"+sql);
									pstmt1 = conn.prepareStatement(sql);
									//pstmt.setString(1,itemCodeSchm);			
									rs1 = pstmt1.executeQuery();
									freeItemListApplcbleType2.clear();
									freeItemList.clear();
									freeItemInfoMap.clear();
									//System.out.println("Values in list after clear for Scheme 2:::"+freeItemList);
									while(rs1.next())
									{
										String freeItem = rs1.getString("item_code");										
										freeItemList.add(freeItem);
										freeItemInfoMap.put(itemCodeSchm, freeItemList);
									}
									if(pstmt1 != null)
									{
										pstmt1.close(); 
										pstmt1 = null;
									}
									if(rs1 != null)
									{
										rs1.close();
										rs1 = null;
									}
									finalFreeItems = freeItemInfoMap.get(itemCodeSchm);		
									System.out.println("Final List of Free Items for ::::Scheme Type 2 is "+finalFreeItems);
									s12 = returnFinalFreeItemDataUsingList(finalFreeItems, conn,schemeType,schemeCode,schemeDescr,discount,freeQty,itemCodeSchm,detai1ValueMap);
									//System.out.println("***********"+s12);										
									valueXmlString.append(s12);					
								}
								 else
								{
									System.out.println("Scheme is NOT applicable for ::"+itemCodeSchm + "Purchased Item quantity::"+xmlQty +"is NOT greater  than or equal to applicable quantity"+purcBase+"for scheme "+schemeCode);
								}
								
							}
						}					
					}
					
					
				}//end of while loop
				if(pstmt != null)
				{
					pstmt.close(); 
					pstmt = null;
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				//valueXmlString.append(s12);	
			}//end of if block for list
			//valueXmlString.append("</Root>\r\n");
			//conditon of FREE_BAL_ORD added by nandkumar gadkari on 11/06/19
			freeBalOrd = distCommon.getDisparams( "999999", "FREE_BAL_ORD", conn );
			if(freeBalOrd==null || freeBalOrd.trim().length() ==0 || freeBalOrd.equalsIgnoreCase("NULLFOUND"))
			{
				freeBalOrd="N";
			}
			else
			{
				freeBalOrd=freeBalOrd.trim();
			}
			if("Y".equalsIgnoreCase(freeBalOrd))
			{
				/**
				 * New code added by kaustubh on 31 Dec 2018  regarding fetching scheme details from scheme_balance table -------------start 
				 * **/
				
			
				System.out.println("inside Scheme balance Changes");
				TranDateDet = getTimeStamp(dsp_date);
				System.out.println("cust_code "+custCode+"TranDate "+ TranDateDet);
				
				outSql = " select TRAN_ID from SCHEME_BALANCE where EFF_FROM <= ? AND VALID_UPTO >= ? AND CUST_CODE = ? " ;
					
				pstmtOut = conn.prepareStatement(outSql);
				pstmtOut.setTimestamp(1, TranDateDet);
				pstmtOut.setTimestamp(2, TranDateDet);
				pstmtOut.setString(3,custCode);
				
				rs3 = pstmtOut.executeQuery();
				
				while(rs3.next())
				{	
					tranID = rs3.getString("TRAN_ID");
					
					sql = " SELECT tran_id,"
							+ "CASE WHEN item_code = 'X' THEN 'X' " 
							+" ELSE item_code end as item_code , SCHEME_CODE,SCHEME_TYPE, " 
							+" CASE WHEN item_code = 'X' THEN 'V' " 
							+" when item_code <> 'X' THEN 'I' END AS nature," 
							+" CASE WHEN (BALANCE_FREE_VALUE - USED_FREE_VALUE  > 0)  THEN (BALANCE_FREE_VALUE - USED_FREE_VALUE ) " 
							+" WHEN  ( balance_free_qty - USED_FREE_qty > 0 ) THEN ( balance_free_qty - USED_FREE_qty )" 
							+" END AS free_qty " 
							+" FROM SCHEME_BALANCE  " 
							+" WHERE  (BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 or balance_free_qty - USED_FREE_qty > 0 ) " 
							+" and  EFF_FROM <= ? AND VALID_UPTO >= ? "  
							+" AND CUST_CODE = ? and TRAN_ID = ? ";
	
					pstmtIn = conn.prepareStatement(sql);
					pstmtIn.setTimestamp(1, TranDateDet);
					pstmtIn.setTimestamp(2, TranDateDet);
					pstmtIn.setString(3,custCode);
					pstmtIn.setString(4, tranID);
					rs4 = pstmtIn.executeQuery();
					
					while(rs4.next())
					{
						domID++;
						
						System.out.println("DomId"+domID);
						itemCode = rs4.getString("item_code");
						schemeCode = rs4.getString("SCHEME_CODE");
						schemeType = rs4.getString("SCHEME_TYPE");
						nature = rs4.getString("nature");
						freeQuantity =rs4.getDouble("free_qty");
								
						valueXmlString.append("<Detail domId='"+(domID)+"'>\r\n");
						System.out.println("Free Items "+itemCode);
						itemDescription = getItemDescr(itemCode,conn);	
						valueXmlString.append("<scheme_type>").append("<![CDATA[").append(schemeType).append("]]>").append("</scheme_type>\r\n");
						valueXmlString.append("<applicable_scheme>").append("<![CDATA[").append(schemeCode).append("]]>").append("</applicable_scheme>\r\n");
						valueXmlString.append("<purchase_item_code>").append("<![CDATA[").append(itemValues).append("]]>").append("</purchase_item_code>\r\n");
						valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescription).append("]]>").append("</item_descr>\r\n");
						valueXmlString.append("<scheme_descr>").append("<![CDATA[").append(schemeDescr).append("]]>").append("</scheme_descr>\r\n");
						valueXmlString.append("<quantity>").append("<![CDATA[").append(freeQuantity).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<discount>").append("<![CDATA[").append(discount).append("]]>").append("</discount>\r\n");
						valueXmlString.append("<item_code__ord>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code__ord>\r\n");
						valueXmlString.append("<rate>").append("<![CDATA[").append(0).append("]]>").append("</rate>\r\n");
						valueXmlString.append("<nature>").append("<![CDATA[").append(nature).append("]]>").append("</nature>\r\n");
						valueXmlString.append("</Detail>\r\n");		
					}
				
				}
			}
			if("P".equalsIgnoreCase(freeBalOrd))//conditon of FREE_BAL_ORD added by nandkumar gadkari on 11/06/19
			{
				if(finalApplicableSchemeSet.size() > 0)
				{
					pointBaseSchemeXml=pointBaseScheme(dom,dom1,xtraParams,s,conn);
					valueXmlString.append(pointBaseSchemeXml);
				}
			}
			valueXmlString.append("</Root>\r\n");
			
			/**
			 * New code added by kaustubh on 31 Dec 2018 regarding fetching scheme details from scheme_balance table --------------- End 
			 ***/
		
			
			
			/* Added by kaustubh on 30 jan 2018 start */
			
			/* to resolved issue of improper domID */
			
			System.out.println("@@xmlString For Scheme :: "+valueXmlString.toString());
			Document finalxml = genericUtility.parseString(valueXmlString.toString());
			NodeList detail1NodeList = finalxml.getElementsByTagName("Detail");
			
			System.out.println("@SchemeXml length:"+detail1NodeList.getLength());
			
			for(int nodeCnt = 0;nodeCnt<detail1NodeList.getLength();nodeCnt++)
			{	
				domID1++;
				System.out.println("New DomID"+domID1);
				//if(detail1NodeList.item(nodeCnt).getAttributes().getNamedItem("dbID") != null)
				//{
					detail1NodeList.item(nodeCnt).getAttributes().getNamedItem("domId").setNodeValue(String.valueOf(domID1));
				//}
			}
			
			System.out.println("");
			newDomXml = genericUtility.serializeDom(finalxml);
			System.out.println("XML For Scheme with new domID ["+newDomXml+"]");
			
			/* Added by kaustubh on 30 jan 2018 end */
		}
		catch(Exception e)
		{
			System.out.println("Exception : Sorder : getFreeSchemeAction " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
				if(pstmtOut != null)
				{
					pstmtOut.close();
					pstmtOut = null;
				}
				if(pstmtIn != null)
				{
					pstmtIn.close();
					pstmtIn = null;
				}
				
			}
			catch(Exception ex)
			{
				System.out.println("Finally Exception : "+ex.getMessage());
				ex.printStackTrace();
			}	
		}
		//return valueXmlString.toString();
	      return newDomXml;
	}
	
	public String getItemDescr(String itemCode , Connection conn)
	{
		String descr = "",sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			conn = getConnection();
			sql = "select descr from item where item_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);			
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				descr = rs.getString("descr");
				System.out.println("Item Description is :::::::::::::"+descr);
			}
			if(pstmt != null)
			{
				pstmt.close(); 
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
			}
			catch(Exception ex)
			{
				System.out.println("Finally Exception : "+ex.getMessage());
				ex.printStackTrace();
			}	
		}
		return descr;
	}
	
	public String getAttributeVal(Node dom, String attribName )throws ITMException
	{
		String AttribValue = null;
		try
		{
			NodeList detailList = dom.getChildNodes(); 
			int detListLength = detailList.getLength();
			for(int ctr = 0; ctr < detListLength; ctr++)
			{
				Node curDetail = detailList.item(ctr);
				if(curDetail.getNodeName().equals("attribute")) 
				{
					AttribValue = curDetail.getAttributes().getNamedItem(attribName).getNodeValue();
					break;
				}
				else
				{
					continue;
				}
			}		
		}
		catch (Exception e)
		{
			System.out.println("Exception : : searchNode :"+e); 
			throw new ITMException(e);
		}
		return AttribValue;
	}
	//Added by sarita on 15JAN2018
	public String returnFinalFreeItemDataUsingList(List<String> list,Connection conn,String schemeType,String schemeCode,String schemeDescr,double discount,double freeQty,String itemValues,HashMap<String,String> detai1ValueMap)
	{
		System.out.println("finalFreeItems :::"+list);
		//System.out.println("finalFreeItems :::"+list.size());
		StringBuffer valueXmlString = new StringBuffer();
		DistCommon distCommon = new DistCommon();
		SalesOrderIC salesOrderIC = new SalesOrderIC();
		String itemDescription = "",nature = "",sch_attr="Y",taxChap="",taxClass="",taxEnv="",stationTo="",stationFrom="",SalesPriceList="",ContractNo="";
		Set<String> listSet = null;
		int domID = 0;
		double newRate =0;
		try
		{
			stationFrom = getStationFrom(detai1ValueMap.get("site_code"),conn);
			stationTo = getStationTo(detai1ValueMap.get("cust_code"), conn);
			if("0".equalsIgnoreCase(schemeType) || "1".equalsIgnoreCase(schemeType))
			{
				nature="F";	
				//discount=0.0;
				//finRate=0.0;
				System.out.println("Nature is "+nature);
			}
			else if("2".equalsIgnoreCase(schemeType))
			{
				nature="C";
				System.out.println("Nature is "+nature);
			}
			System.out.println("Final List is :::to iterate ["+list+"]" +"\t"+ "Purchased Items are :::["+itemValues+"]");
			if(list != null )
			{
				System.out.println("List is not Enpty!!!");
				listSet = new HashSet<String>(list);			
				System.out.println("Final listSet to iterate ["+listSet+"]");
				if(listSet.size() > 0)
				{
					for(String item_Code : listSet)    
					{	
						System.out.println("Free Item in listSet are :::"+listSet);
						
						domID++;
						valueXmlString.append("<Detail domId='"+(domID)+"'>\r\n");	
						System.out.println("Free Items "+item_Code);
						itemDescription = getItemDescr(item_Code,conn);	
						taxChap  = (distCommon.getTaxChap(item_Code,detai1ValueMap.get("item_ser"), "C", detai1ValueMap.get("cust_code__dlv"),detai1ValueMap.get("site_code"), conn));
						taxClass = (distCommon.getTaxClass("C", detai1ValueMap.get("cust_code__dlv"), item_Code, detai1ValueMap.get("site_code"), conn));
						taxEnv   = (distCommon.getTaxEnv(stationFrom, stationTo, taxChap, taxClass, detai1ValueMap.get("site_code"), conn));
						ContractNo=salesOrderIC.getContract(detai1ValueMap.get("site_code"), detai1ValueMap.get("cust_code"), getTimeStamp(detai1ValueMap.get("order_date")), item_Code, "", "", conn);
						SalesPriceList = distCommon.getSalesPriceList(detai1ValueMap.get("cust_code"),detai1ValueMap.get("cust_code__dlv"),detai1ValueMap.get("site_code"),ContractNo,item_Code,detai1ValueMap.get("order_date"),conn);
						newRate = distCommon.getSalesRate(SalesPriceList,detai1ValueMap.get("site_code"),detai1ValueMap.get("cust_code"),detai1ValueMap.get("order_date"),detai1ValueMap.get("order_date"),"C",ContractNo,ContractNo,item_Code,0.0,conn);						
						System.out.println("taxChap["+taxChap+"]" + "taxClass["+taxClass+"]" + "taxEnv["+taxEnv+"]" + "ContractNo["+ContractNo+"]" + "SalesPriceList["+SalesPriceList+"]" + "newRate["+newRate+"]");
						valueXmlString.append("<scheme_type>").append("<![CDATA[").append(schemeType).append("]]>").append("</scheme_type>\r\n");
						valueXmlString.append("<applicable_scheme>").append("<![CDATA[").append(schemeCode).append("]]>").append("</applicable_scheme>\r\n");
						//valueXmlString.append("<scheme_description visible = \"1\">").append("<![CDATA[").append(schemeDescr).append("]]>").append("</scheme_description>\r\n");//changes by sarita on 18JAN2018
						//valueXmlString.append("<scheme_description>").append("<![CDATA[").append(schemeDescr).append("]]>").append("</scheme_description>\r\n");
						valueXmlString.append("<purchase_item_code>").append("<![CDATA[").append(itemValues).append("]]>").append("</purchase_item_code>\r\n");
						valueXmlString.append("<item_code>").append("<![CDATA[").append(item_Code).append("]]>").append("</item_code>\r\n");
						//valueXmlString.append("<item_code__ord>").append("<![CDATA[").append(item_Code).append("]]>").append("</item_code__ord>\r\n");
						//valueXmlString.append("<item_code__ord>").append("<![CDATA[").append(item_Code).append("]]>").append("</item_code__ord>\r\n");					
						valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescription).append("]]>").append("</item_descr>\r\n");
						valueXmlString.append("<scheme_descr>").append("<![CDATA[").append(schemeDescr).append("]]>").append("</scheme_descr>\r\n");
						valueXmlString.append("<quantity>").append("<![CDATA[").append(freeQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(freeQty).append("]]>").append("</quantity__stduom>\r\n");
						valueXmlString.append("<discount>").append("<![CDATA[").append(discount).append("]]>").append("</discount>\r\n");
						valueXmlString.append("<nature>").append("<![CDATA[").append(nature).append("]]>").append("</nature>\r\n");
						//valueXmlString.append("<sch_attr>").append("<![CDATA[").append(sch_attr).append("]]>").append("</sch_attr>\r\n");
						valueXmlString.append("<rate>").append("<![CDATA[").append(newRate).append("]]>").append("</rate>\r\n");
						valueXmlString.append("<item_code__ord>").append("<![CDATA[").append(item_Code).append("]]>").append("</item_code__ord>\r\n");
						valueXmlString.append("<tax_chap>").append("<![CDATA[").append(taxChap).append("]]>").append("</tax_chap>\r\n");
						valueXmlString.append("<tax_class>").append("<![CDATA[").append(taxClass).append("]]>").append("</tax_class>\r\n");
						valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv).append("]]>").append("</tax_env>\r\n");
						//valueXmlString.append("<contract_no>").append("<![CDATA[").append(ContractNo).append("]]>").append("</contract_no>\r\n");
						valueXmlString.append("</Detail>\r\n");		
					}
				}
			}
		}
		catch(Exception e)
		{
			
			System.out.println("Exception in returnFinalFreeItemDataUsingList is:::"+e);
			e.printStackTrace();
		}
		return valueXmlString.toString();
	}
	
	//Commented by sarita on 19/JAN/2018
/*	public String returnFinalDataUsingList(List<String> list,Connection conn,String siteCode)
	{
		System.out.println("finalFreeItems :::"+list);
		System.out.println("finalFreeItems :::"+list.size());
		StringBuffer valueXmlString = new StringBuffer();
		String itemDescription = "";
		
		try
		{
			Set<String> listSet = new HashSet<String>(list);
			if(listSet.size() > 0)
			{
				for(String item_Code : listSet)
				{
					
					System.out.println("Free Items "+item_Code);
					itemDescription = getItemDescr(item_Code,conn);				
					valueXmlString.append("<item_code__ord visible = \"1\">").append("<![CDATA[").append(item_Code).append("]]>").append("</item_code__ord>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(item_Code).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescription).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<site_code visible = \"1\">").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[]]>").append("</quantity>\r\n");
					valueXmlString.append("<rate visible = \"1\">").append("<![CDATA[0]]>").append("</rate>\r\n");								
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in returnFinalDataUsingList is:::"+e);
			e.printStackTrace();
		}
		return valueXmlString.toString();
	}*/
	
	public String getItemCode(List<String> itemCodeListScheme)
	{
		Iterator<String> itr = null;
		String itemValues = "";
		try
		{
			itr = itemCodeListScheme.iterator();
			
			while(itr.hasNext())
			{
				itemValues = itemValues + "'"+(String) itr.next() + "'"+",";
			}
			//System.out.println("s1 is "+s1);
			if(itemValues != null && itemValues.length() != 0)
			{
				itemValues = itemValues.substring(0,itemValues.length()-1);
		    	// System.out.println("s1 is "+s1);
			}
			System.out.println("itemValues is "+itemValues);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return itemValues;
	}
	private String checkDouble(String input)	
	{
		if (input == null || input.trim().length() == 0)
		{
			input="0";
		}
		return input;
	}
	//Added by sarita on 19/JAN/2018
	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str.trim() ;
		}
	}
	
	private String getDueDate(String sale_order,Connection conn) 
	{
		String dueDate = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = "select due_date from sorder where sale_order=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,sale_order);			
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				dueDate = checkNull(rs.getString("due_date"));
				System.out.println("Due Date is ======="+dueDate);
			}
		}
		catch(Exception e)
		{
			System.out.println("Inside getDueDate() ---- "+e);
			e.printStackTrace();
		}
		return dueDate;
	}
	private java.sql.Timestamp getTimeStamp(String dateStr) throws ITMException, Exception 
	{ 
		String dbDateStr = "";
		if(dateStr != null && !dateStr.equals(""))
		{
			if(dateStr.indexOf(":") != -1)
			{
				System.out.println("inside getTimeStamp METHOD logic"+dateStr);
				return java.sql.Timestamp.valueOf(dateStr);      
			}
			else
			{
				System.out.println("inside getTimeStamp METHOD ");
				dbDateStr = genericUtility.getValidDateTimeString(dateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateTimeFormat());
				return java.sql.Timestamp.valueOf(dbDateStr); 
			}
		}
		else
		{
			return null;
		}
	}
	public String getStationFrom(String siteCode,Connection conn)
	{
		String sql = "",stationFrom="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = " SELECT STAN_CODE FROM SITE WHERE  SITE_CODE =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				stationFrom = checkNull(rs.getString("STAN_CODE"));
			}
			if(rs != null)
			{
			rs.close();rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();pstmt = null;
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e);
		}
		return stationFrom;
	}
	
	public String getStationTo(String custCode,Connection conn)
	{
		String sql = "",stationTo="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = " SELECT STAN_CODE FROM CUSTOMER WHERE CUST_CODE =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				stationTo = checkNull(rs.getString("STAN_CODE"));
			}
			if(rs != null)
			{
				rs.close();rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close();pstmt = null;
			}
			System.out.println("stationTo"+stationTo);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e);
		}
		return stationTo;
		
	}
	//added by nandkumar gadkari on 12/06/19
	public String pointBaseScheme(Document dom, Document dom1, String xtraParams,String s,Connection conn)
	{
		String sql = "",custCode="";
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null, pstmt4=null,pstmt5=null;
		ResultSet rs = null ,rs1 = null,rs2 = null,rs3 = null,rs4=null;
		Node currDetail = null, headerNode = null;
		StringBuffer valueXmlString = new StringBuffer();
		String itmCode="",schemeCode = "",itemCodeOrd = "",schemeType="",saleOrder="",siteCode="",itemDescription="",schemeDescr="",dsp_date="",nature="",schemeCodePur="";
		Timestamp orderDate=null;
		double offerPoints=0,totalpoints=0,freePoints=0,reqPoints=0,availQty=0,unConfFreeQty=0,unconfreqPoints=0,unConfTotFreePoints=0,quantity=0,prvFreePoints=0;
		int cnt=0,domID=0,len=0,noOfDetails=0;
		String countCodeDlv="",stateCodeDlv="",schemeCodeList="",browItemCode="",schemeStkChk="";
		List<String> getSchemeList = new ArrayList<String>();
		List<String> schemeList = new ArrayList<String>();
		NodeList detailList = null;
		String arrStr[] = null;
		Iterator<String> itr = null;
		try
		{
			
			headerNode = dom1.getElementsByTagName("Detail1").item(0);
			custCode = checkNull(genericUtility.getColumnValueFromNode("cust_code", headerNode));
			orderDate = Timestamp.valueOf(genericUtility.getValidDateString(
					genericUtility.getColumnValueFromNode("due_date", headerNode),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
			siteCode = checkNull(genericUtility.getColumnValueFromNode("site_code", headerNode));
			saleOrder = checkNull(genericUtility.getColumnValueFromNode("sale_order", headerNode));
			stateCodeDlv = checkNull(genericUtility.getColumnValueFromNode("state_code__dlv", headerNode));
			countCodeDlv = checkNull(genericUtility.getColumnValueFromNode("count_code__dlv", headerNode));
			if(saleOrder.trim().length() == 0)
			{
				saleOrder=" ";
			}
		
			sql = "select distinct(scheme_code),item_code from sch_pur_items where item_code in ("+s+")";
			System.out.println("sql ==="+sql);   		  
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next())
			{				
				schemeCodePur = rs.getString(1);	
				getSchemeList.add(schemeCodePur);
				System.out.println("Applicable Scheme for Purchase Item Scheme Code ["+schemeCodePur+"]");
			}
			schemeCodeList = "";
			itr = getSchemeList.iterator();
			while(itr.hasNext())
			{
				schemeCodeList = schemeCodeList + "'"+(String) itr.next() + "'";
				if(itr.hasNext())
				{
					schemeCodeList=schemeCodeList+ ",";
				}
			}
			
			sql = "select distinct(a.scheme_code) from scheme_applicability a,scheme_applicability_det b"
						+ " where a.scheme_code	= b.scheme_code"
						+ " and a.prod_sch = 'Y'"
						+ " and a.app_from<= ?" + " and a.valid_upto>= ?" 
						+ " and (b.site_code=?"
						+ " or b.state_code = ?" + " or b.count_code= ?) and a.scheme_code in ("+schemeCodeList+")";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setTimestamp(1, orderDate);
				pstmt2.setTimestamp(2, orderDate);
				pstmt2.setString(3, siteCode);
				pstmt2.setString(4, stateCodeDlv);
				pstmt2.setString(5, countCodeDlv);
				rs2 = pstmt2.executeQuery();
				while(rs2.next())
				{
					schemeCode = rs2.getString("scheme_code");
					schemeList.add(schemeCode);
				}
				if(pstmt2 != null)
				{
					pstmt2.close(); 
					pstmt2 = null;
				}
				if(rs2 != null)
				{
					rs2.close();
					rs2 = null;
				} 
				schemeCodeList="";
				itr = schemeList.iterator();
				/*while(itr.hasNext())
				{
					schemeCodeList = schemeCodeList + "'"+(String) itr.next() + "'"+",";
				}
				if(schemeCodeList != null && schemeCodeList.length() != 0)
				{
					schemeCodeList = schemeCodeList.substring(0,schemeCodeList.length()-1);
				}*/
				while(itr.hasNext())
				{
					schemeCodeList = schemeCodeList + "'"+(String) itr.next() + "'";
					if(itr.hasNext())
					{
						schemeCodeList=schemeCodeList+ ",";
					}
				}
			sql = "select scheme_code,descr,scheme_type from sch_group_def where scheme_code in ("+schemeCodeList+") and scheme_type = ?";
			System.out.println("sql >> ["+sql+"]");
			pstmt4 = conn.prepareStatement(sql);
			pstmt4.setInt(1,3);
			rs4 = pstmt4.executeQuery();
			while(rs4.next())
			{
				schemeCode = rs4.getString("scheme_code");
				schemeDescr = rs4.getString("descr");
				schemeType = rs4.getString("scheme_type");
							
				sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
						+ "  AND CUST_CODE = ?  AND ITEM_CODE= ?  AND EFF_FROM <= ? AND VALID_UPTO >=?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					pstmt.setString(2,"X");
					pstmt.setTimestamp(3, orderDate);
					pstmt.setTimestamp(4, orderDate);
					rs = pstmt.executeQuery();

					if (rs.next()) 
					{
						freePoints = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(freePoints > 0)
					{
						sql = " select b.item_code__ord ,b.quantity " +								
								" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
								+ " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
								+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('P')";

						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, siteCode);
						pstmt1.setString(2, custCode);
						pstmt1.setString(3, saleOrder);
						pstmt1.setTimestamp(4, orderDate);
						pstmt1.setTimestamp(5, orderDate);
						
						rs1 = pstmt1.executeQuery();
					while (rs1.next()) {
						itemCodeOrd=rs1.getString(1);
							unConfFreeQty = rs1.getDouble(2);
							System.out.println("unConfFreeQty" + unConfFreeQty);
							sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							pstmt.setString(2, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
								
							if(cnt > 0)
							{
								sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									unconfreqPoints = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								unConfTotFreePoints = unConfTotFreePoints + (unConfFreeQty  * unconfreqPoints);
							
							}
							
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;
						freePoints= freePoints - unConfTotFreePoints;
						
						detailList = dom1.getElementsByTagName("Detail2");
						noOfDetails = detailList.getLength();
					    System.out.println("noOfDetails >>"+noOfDetails);

						for(int ctr = 0; ctr < noOfDetails; ctr++)
						{
							currDetail = detailList.item(ctr);
							
							browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code__ord", currDetail));
							quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("quantity", currDetail));	
							nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail));
													
							System.out.println("itemCode["+browItemCode+"]" + "quantity["+quantity+"]" + "siteCode["+siteCode+"]" + "nature["+nature+"]");
							if (nature.equals("P")) {
								
								sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, browItemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
									
								if(cnt > 0)
								{
									sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									pstmt.setString(2, browItemCode);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										unconfreqPoints = rs.getDouble(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
								
										prvFreePoints = prvFreePoints + quantity * unconfreqPoints;
									
								}
								

							}
						}
						freePoints= freePoints - prvFreePoints;
						//added by nandkumar gadkari on 09/09/19
						schemeStkChk = distCommon.getDisparams( "999999", "SCHEME_STOCK_CHECK", conn );
						if(schemeStkChk==null || schemeStkChk.trim().length() ==0 || schemeStkChk.equalsIgnoreCase("NULLFOUND"))
						{
							schemeStkChk="Y";
						}
						sql = "select item_code, required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, schemeCode);
							rs2 = pstmt2.executeQuery();
							while (rs2.next()) 
							{
								
								if(freePoints > 0)
								{
									
									itemCodeOrd = rs2.getString(1);
									reqPoints = rs2.getDouble(2);
									//if condition added by nandkumar gadkari on 09/09/19
									
									if("Y".equalsIgnoreCase(schemeStkChk.trim()))
									{
										sql="SELECT SUM(a.QUANTITY - a.ALLOC_QTY - CASE WHEN a.HOLD_QTY IS NULL THEN 0 ELSE a.HOLD_QTY END ) AVAIL_QTY "
										+" FROM STOCK A, "
										+"LOCATION B, "
										+"INVSTAT C "
										+"WHERE A.LOC_CODE = B.LOC_CODE "
										+"AND B.INV_STAT = C.INV_STAT "
										+"AND A.ITEM_CODE = ? "
										+"AND A.SITE_CODE = ? "
										+"AND C.AVAILABLE = 'Y' "
										+"AND C.STAT_TYPE = 'M' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, itemCodeOrd);
										pstmt.setString(2, siteCode);
										
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											availQty = rs.getDouble(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										if(availQty <= 0)
										{
											continue;
										}
									}
									quantity=integralPartQty(freePoints/reqPoints); 
									if(quantity <= 0)
									{
										continue;
									}
			
									domID++;
									valueXmlString.append("<Detail domId='"+(domID)+"'>\r\n");	
									System.out.println("Free Items "+itemCodeOrd);
									itemDescription = getItemDescr(itemCodeOrd,conn);	
									
									valueXmlString.append("<scheme_type>").append("<![CDATA[").append("Point Base Scheme").append("]]>").append("</scheme_type>\r\n");
									valueXmlString.append("<applicable_scheme>").append("<![CDATA[").append(schemeCode).append("]]>").append("</applicable_scheme>\r\n");
									valueXmlString.append("<purchase_item_code>").append("<![CDATA[").append(itemCodeOrd).append("]]>").append("</purchase_item_code>\r\n");
									valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCodeOrd).append("]]>").append("</item_code>\r\n");
									valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescription).append("]]>").append("</item_descr>\r\n");
									valueXmlString.append("<scheme_descr>").append("<![CDATA[").append(schemeDescr).append("]]>").append("</scheme_descr>\r\n");
									valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
									valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity__stduom>\r\n");
									valueXmlString.append("<discount>").append("<![CDATA[").append(0).append("]]>").append("</discount>\r\n");
									valueXmlString.append("<item_code__ord>").append("<![CDATA[").append(itemCodeOrd).append("]]>").append("</item_code__ord>\r\n");
									valueXmlString.append("<rate>").append("<![CDATA[").append(0).append("]]>").append("</rate>\r\n");
									valueXmlString.append("<nature>").append("<![CDATA[").append("P").append("]]>").append("</nature>\r\n");
									//Added By Mukesh Chauhan on 21/08/19------Start
									valueXmlString.append("<Scheme_point>").append("<![CDATA[").append(reqPoints).append("]]>").append("</Scheme_point>\r\n");
									valueXmlString.append("<Scheme_balance_point>").append("<![CDATA[").append(freePoints).append("]]>").append("</Scheme_balance_point>\r\n");
									//End
									valueXmlString.append("</Detail>\r\n");							
								}
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;
					}
				
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e);
		}
		return valueXmlString.toString();
		
	}
	private double integralPartQty(double value) {
		double fractionalPart = value % 1;
		double integralPart = value - fractionalPart;
		System.out.println(integralPart +" integralPart     "+ fractionalPart);
		return integralPart;
	}
	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}
	//added by nandkumar gadkari on 11/02/19------------------------end--------------------------------

	//Added by Rohini Telang on 10/02/2021 [Start]
	private String actionQuotation(Document dom, String objContext, String xtraParams) throws ITMException
	{
		String quotationNo = "";
		String errCode = "";
		String errString = "";
		String sql = "";
		String itemCode = "";
		String quantity = "";
		String itemCodeDescr = "";
		String rate = "";
		String unit = "";
		ResultSet rs = null,rs1 = null;
		
		java.sql.Date reqDate = null;
		Connection conn = null;
		PreparedStatement pstmt = null ,pstmt1 = null;
	
		char c = 32;  // Ascii charecter of empty space or whitespace
		
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
        ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
        UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			
			conn = getConnection();
			
			quotationNo = genericUtility.getColumnValue("quot_no",dom);	
			System.out.println("quotationNo  ::::"+quotationNo);
			if(quotationNo != null)
			{	
				sql = "select item_code,quantity,rate,unit from sales_quotdet where quot_no='"+quotationNo+"'";					
				System.out.println("sorder SQL :="+sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{	
					valueXmlString.append("<Detail>\r\n");
					itemCode = rs.getString("item_code");
					quantity = rs.getString("quantity");
					rate = rs.getString("rate");
					unit = rs.getString("unit");
					if(itemCode != null && itemCode.trim().length()>0)
					{
						valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					}
					else
					{
						valueXmlString.append("<item_code>").append("<![CDATA["+ String.valueOf(c) +"]]>").append("</item_code>\r\n");
					}
					sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					pstmt1 = conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						itemCodeDescr = rs1.getString("DESCR");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("itemCodeDescr  ::::"+itemCodeDescr);
					valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemCodeDescr).append("]]>").append("</item_descr>\r\n");
					
					
					System.out.println("quantity  ::::"+quantity);
					if(quantity != null && quantity.trim().length()>0)
					{
                        double qty =0.0;
                        qty = Double.parseDouble(quantity);
                        System.out.println("qty  ::::"+qty);
					    valueXmlString.append("<quantity>").append("<![CDATA[").append(utilMethods.getReqDecString((qty), 3)).append("]]>").append("</quantity>\r\n");//Changed by Anagha R on 26/10/2020 for PO with Quotation Error
					}
					else
					{
						valueXmlString.append("<quantity>").append("<![CDATA[").append("0").append("]]>").append("</quantity>\r\n");
						
					}		
                    valueXmlString.append("<rate>").append("<![CDATA[").append((rate == null) ?"0.00":utilMethods.getReqDecString(Double.parseDouble(rate), 4)).append("]]>").append("</rate>\r\n");
					if(unit != null && unit.length()>0 )
					{
						valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					}
					else
					{
						
						valueXmlString.append("<unit><![CDATA["+   String.valueOf(c) +"]]></unit>\r\n");
					}
					
					valueXmlString.append("</Detail>\r\n");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				
				valueXmlString.append("</Root>\r\n");			
			}
			else
			{
				System.out.println("Quotation found null");
				errCode = "VTQUOTNULL";
			}
			if (!errCode.equals(""))
			{
				errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				System.out.println("Errcode found not null");
				return errString;
			}
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Sorder : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Sorder : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}catch(Exception e){}
		}		
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	private String quotationTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		NodeList detailList = null;
		Node currentDetail = null;
		int detailListLength = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		ResultSet rs = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		String quotNo = "",unitSampl = "",itemCode = "",quantity = "",rate = "",samplQty = "",unit = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
        UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
            
			conn = getConnection();
			
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				currentDetail = detailList.item(ctr);
				
				itemCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("item_code",currentDetail);
				quantity = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity",currentDetail);
				rate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate",currentDetail);
				unit = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit",currentDetail);
				valueXmlString.append("<Detail>");			
				//valueXmlString.append("<item_code isSrvCallOnChg='1'>").append(itemCode).append("</item_code>");//Commented and added by Rohini T on 15/02/2021
				valueXmlString.append("<item_code__ord isSrvCallOnChg='1'>").append(itemCode).append("</item_code__ord>");
				sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append(rs.getString(1)).append("</item_descr>");
				}				
				rs.close();
				rs = null;
				pstmt.close();
				pstmt= null;
                valueXmlString.append("<quantity isSrvCallOnChg='1'>").append(utilMethods.getReqDecString(Double.parseDouble(quantity), 3)).append("</quantity>");
                valueXmlString.append("<rate isSrvCallOnChg='1'>").append(utilMethods.getReqDecString(Double.parseDouble(rate), 4)).append("</rate>");
				valueXmlString.append("<unit isSrvCallOnChg='1'>").append(unit).append("</unit>");
				
				valueXmlString.append("</Detail>");
			}
			valueXmlString.append("</Root>");			
		}
		catch (Exception e)
		{
			System.out.println("Exception SorderAct quotationTransform :: "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection......");
				conn.close();
				conn = null;
			}
			catch (Exception se){}
		}
		return valueXmlString.toString();
	}

	//Added by Rohini Telang on 10/02/2021 [End]

}