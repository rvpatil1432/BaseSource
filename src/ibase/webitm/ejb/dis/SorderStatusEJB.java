package ibase.webitm.ejb.dis;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

@javax.ejb.Stateless
public class SorderStatusEJB extends ValidatorEJB implements SorderStatusEJBRemote, SorderStatusEJBLocal  
{
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	PostOrdCreditChk postOrdCreditChk = new PostOrdCreditChk();
	@Override
	//Added by sarita on 8JAN2018
	//public String getSorderStatusXML(String tranId, String ref_series) throws ITMException
	public String getSorderStatusXML(String tranId, String ref_series,UserInfoBean userInfo) throws ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null ;
		String sql="",saleOrder="";
		String columnName="";
		String apply_time = "", desp_id = "";
		String asCustCodeBil="",asItemSer="",asSorder="",asSiteCode="";
		Timestamp adtTranDate=null;
		String pordDate = "", orderDate = "",maxDate = "", minDate = "",despDate = "",tranDate = "";
		//Added by Pooja S on[11-12-2018] for the Unconfirmed Status
		String pordDate1 = "",orderDate1="",minDate1="",maxDate1="",despDate1 = "",tranDate1="",despqty="",confirmed="";	
		double adNetAmt = 0.0;
		int cnt = 0;
		HashMap<String,Object> sordData = null;
		ArrayList<HashMap> sordDataList= new ArrayList<HashMap>();
		HashMap<String,Object> finalData = null;
		String policyDescr = "";
		String lsCrPolicy = "",lsdescr="",lsStatus="";
		try
		{
			//added by sarita to set userInfo on 8JAN2018
			setUserInfo(userInfo);
			conn = getConnection();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("Inside getSorderStatusXML method of SorderStatusEJB");
			System.out.println("saleOrder ::"+tranId + "\t"+ "ref_series ::"+ref_series);
			
			if("S-ORD".equalsIgnoreCase(ref_series))//ref-series for w_sorder
			{
				saleOrder = tranId;
				System.out.println("Value of saleOrder in SALE ORDER is :::"+tranId);
			}
			else if("S-DSP".equalsIgnoreCase(ref_series))//ref-series for w_despatch
			{
				System.out.println("Inside Despatch Transaction ID ::"+tranId);
				sql = "select sord_no from despatch where desp_id=?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
			    rs = pstmt.executeQuery();
			    if(rs.next())
			    {
			    	saleOrder = rs.getString("sord_no");
			    }
			    System.out.println("Value of saleOrder in Despatch is :::"+saleOrder);
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
			else if("S-INV".equalsIgnoreCase(ref_series))//ref-series for w_despatch
			{
				System.out.println("Inside Invoice Transaction ID ::"+tranId);
				sql = "select sale_order from invoice where invoice_id=?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
			    rs = pstmt.executeQuery();
			    if(rs.next())
			    {
			    	saleOrder = rs.getString("sale_order");
			    }
			    System.out.println("Value of saleOrder in Invoice is :::"+saleOrder);
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
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header>");
			valueXmlString.append("</header>");
			
		/*	sql = "SELECT SORDITEM.SALE_ORDER,SORDITEM.LINE_NO,SORDITEM.EXP_LEV" +
				  ",SORDITEM.SITE_CODE,SORDITEM.ITEM_CODE__ORD,SORDITEM.ITEM_FLAG," +
				  "SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT," +
				  "SORDITEM.QTY_ALLOC,SORDITEM.DATE_ALLOC,SORDITEM.NATURE,SORDITEM.REAS_CODE," +
				  "SORDITEM.REAS_DETAIL,INVOICE.INVOICE_ID,INVOICE.TRAN_DATE,DESPATCHDET.DESP_ID," +
				  "SORDITEM.QTY_DESP,FN_GET_DESP_DATE(DESPATCHDET.DESP_ID) DESP_DATE," +
				  "SUM((CASE WHEN DESPATCHDET.QUANTITY IS NULL THEN 0 ELSE DESPATCHDET.QUANTITY END)) " +
				  "DESP_QTY,SORDITEM.STATUS,SORDITEM.PLAN_PROD_DATE,SORDITEM.EXP_DLV_DATE,SORDITEM.REMARKS," +
				  "(SELECT DESPATCH.PRO_NO FROM DESPATCH  WHERE  DESPATCH.DESP_ID = DESPATCHDET.DESP_ID ) " +
				  "PRO_NO,(SELECT DESPATCH.LR_DATE FROM DESPATCH  WHERE  DESPATCH.DESP_ID = DESPATCHDET.DESP_ID )  " +
				  "SHIPMENT_DATE,(SELECT TRAN_NAME FROM TRANSPORTER WHERE TRAN_CODE = (SELECT DESPATCH.TRAN_CODE " +
				  "FROM DESPATCH  WHERE  DESPATCH.DESP_ID = DESPATCHDET.DESP_ID  ))  TRAN_NAME,SORDDET.HOLD_FLAG," +
				  "DESPATCH.LR_NO,DESPATCH.LR_DATE,'' AS DUMMY,SORDER.STATUS,(SELECT SUM(QUANTITY - ALLOC_QTY - " +
				  "CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END) FROM STOCK WHERE ITEM_CODE = SORDITEM.ITEM_CODE__ORD " +
				  "GROUP BY ITEM_CODE) STK_QTY,(SORDITEM.QUANTITY - SUM((CASE WHEN DESPATCHDET.QUANTITY IS NULL " +
				  "THEN 0 ELSE DESPATCHDET.QUANTITY END))) AS PEND_QTY,SORDER.PORD_DATE,DESPATCH.TRANS_MODE,SORDER." +
				  "CUST_PORD,SORDER.ORDER_DATE,SORDDET.DSP_DATE,FN_GET_MIN_MAX_DUEDATE(SORDITEM.SALE_ORDER,'MIN') " +
				  "MIN_DUE_DATE,FN_GET_MIN_MAX_DUEDATE(SORDITEM.SALE_ORDER,'MAX') MAX_DUE_DATE FROM SORDITEM  " +
				  "SORDITEM,ITEM  ITEM,DESPATCHDET  DESPATCHDET,INVOICE  INVOICE,SORDDET  SORDDET,DESPATCH  " +
				  "DESPATCH,SORDER  SORDER WHERE ( SORDITEM.ITEM_CODE      = ITEM.ITEM_CODE   ) AND ( SORDITEM.SALE_ORDER      = " +
				  "SORDDET.SALE_ORDER   ) AND ( SORDITEM.LINE_NO      = SORDDET.LINE_NO   ) AND ( SORDER.SALE_ORDER      = SORDDET.SALE_ORDER   ) " +
				  "AND ( SORDER.SALE_ORDER      = SORDITEM.SALE_ORDER   ) AND ( SORDITEM.SALE_ORDER=DESPATCHDET.SORD_NO(+)) " +
				  "AND ( SORDITEM.LINE_NO=DESPATCHDET.LINE_NO__SORD(+)) AND ( SORDITEM.EXP_LEV=DESPATCHDET.EXP_LEV(+)) AND ( DESPATCH.DESP_ID(+) =DESPATCHDET.DESP_ID )  " +
				  "AND ( INVOICE.DESP_ID(+) =DESPATCH.DESP_ID )  AND SORDITEM.SALE_ORDER    = ?  " +
				  "GROUP BY SORDITEM.SALE_ORDER,SORDITEM.SITE_CODE,SORDITEM.ITEM_CODE__ORD,SORDITEM.ITEM_FLAG," +
				  "SORDITEM.EXP_LEV,SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT," +
				  "SORDITEM.DATE_ALLOC,SORDITEM.QTY_ALLOC,SORDITEM.STATUS,SORDITEM.NATURE,SORDITEM.LINE_NO," +
				  "SORDITEM.REAS_CODE,SORDITEM.REAS_DETAIL,INVOICE.INVOICE_ID,INVOICE.TRAN_DATE,DESPATCHDET.DESP_ID," +
				  "SORDITEM.QTY_DESP,SORDITEM.PLAN_PROD_DATE,SORDITEM.REMARKS,SORDITEM.EXP_DLV_DATE,SORDDET.HOLD_FLAG," +
				  "DESPATCH.LR_NO,DESPATCH.LR_DATE,SORDER.STATUS,SORDER.PORD_DATE,DESPATCH.TRANS_MODE,SORDER.CUST_PORD," +
				  "SORDER.ORDER_DATE,SORDDET.DSP_DATE ORDER BY SORDITEM.LINE_NO DESC,SORDITEM.EXP_LEV ASC";
			
			       pstmt =  conn.prepareStatement(sql);
			       SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			       pstmt.setString(1,saleOrder);
			       rs = pstmt.executeQuery();
			       
			       if(rs.next())
			       {
			    	   int total_rows = rs.getMetaData().getColumnCount();
			    	   System.out.println("Total Rows ::"+total_rows);
			    	   for (int i = 0; i < total_rows; i++) 
			    	   {
			    		   String columnValue = "";			    		  
			    		   //jsonObj.put(rs.getMetaData().getColumnLabel(i+1).toLowerCase(getMetaData), rs.getObject(i + 1));
			    		   int type = rs.getMetaData().getColumnType(i+1);
			    		   System.out.println("column type ::"+type);
			    		   columnName = rs.getMetaData().getColumnLabel(i+1).toLowerCase();
			    		   if (type == Types.DATE || type == Types.TIMESTAMP)
			    		   {
			    			   System.out.println("Inside if block of type!!!!!!!");
			    			   if(rs.getDate(i+1) != null)
			    			   {			    				  
			    				   columnValue = sdf.format(rs.getDate(i+1));
			    				   System.out.println("columnValue is::::"+columnValue);
			    				   genericUtility.getValidDateTimeString(columnValue,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
			    			   }
			    		   }
			    		   else
			    		   {
			    			   columnValue = checkNull(rs.getString(i + 1));
			    		   }
			    		   valueXmlString.append("<"+columnName+"><![CDATA["+(columnValue)+"]]></"+columnName+">"+"\n");			    		  
			    	   }
			       }	*/  		
			valueXmlString.append("<Detail1>");
			
			sql = "SELECT SORDER.SALE_ORDER,SORDER.CUST_PORD,SORDER.PORD_DATE,SORDER.ORDER_DATE,SORDER.ITEM_SER," +
				  "SORDER.CUST_CODE,SORDER.TOT_AMT,SORDER.CONFIRMED,SORDER.DUE_DATE,SORDER.SITE_CODE,SORDER.STATUS," +
		   		  "FN_GET_MIN_MAX_DUEDATE(SORDER.SALE_ORDER,'MIN') MIN_DUE_DATE, FN_GET_MIN_MAX_DUEDATE(SORDER.SALE_ORDER,'MAX') " +
		   		  "MAX_DUE_DATE FROM SORDER SORDER WHERE SALE_ORDER =?";
		   pstmt1 =  conn.prepareStatement(sql);
		   pstmt1.setString(1,saleOrder);
	       rs1 = pstmt1.executeQuery();
	       if(rs1.next())
	       { 
	    	   pordDate = rs1.getString("PORD_DATE");
	    	   orderDate = rs1.getString("ORDER_DATE");
	    	   minDate = rs1.getString("MIN_DUE_DATE");
	    	   maxDate = rs1.getString("MAX_DUE_DATE");
	    	   asItemSer = rs1.getString("ITEM_SER");
	    	 //Added by Pooja S on[11-12-2018] for the Unconfirmed Status
	    	   confirmed = rs1.getString("CONFIRMED");
	    	   System.out.println("pordDate ::"+pordDate +"\t" +"orderDate ::"+orderDate + "minDate ::"+minDate +"maxDate ::"+maxDate);
	    	   
	    	   if(pordDate != null)
	    	   {
	    		   pordDate1 =  genericUtility.getValidDateString(pordDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat()); 
	    	   }
	    	   if(orderDate != null)
	    	   {
	    		   orderDate1 =  genericUtility.getValidDateString(orderDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat()); 
	    	   }
	    	   if(minDate != null)
	    	   {
	    		   minDate1 =  genericUtility.getValidDateString(minDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat()); 
	    	   }
	    	   if(maxDate != null)
	    	   {
	    		   maxDate1 =  genericUtility.getValidDateString(maxDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
	    	   }	    	   
	    	   	valueXmlString.append("<header_data>");
	    	   	valueXmlString.append("<sale_order><![CDATA["+checkNull(rs1.getString("SALE_ORDER"))+"]]></sale_order>\n");
	    	   	valueXmlString.append("<cust_pord><![CDATA["+checkNull(rs1.getString("CUST_PORD"))+"]]></cust_pord>\n");
	    	   	valueXmlString.append("<pord_date><![CDATA["+pordDate1+"]]></pord_date>\n");
	    	   	valueXmlString.append("<order_date><![CDATA["+orderDate1+"]]></order_date>\n");
	    	   	valueXmlString.append("<status><![CDATA["+checkNull(rs1.getString("STATUS"))+"]]></status>\n");
	    	   	/*valueXmlString.append("<item_ser><![CDATA["+rs.getString("ITEM_SER")+"]]>></item_ser>\n");*/
	    	   	/*valueXmlString.append("<cust_code__bil><![CDATA["+rs.getString("CUST_CODE__BIL")+"]]>></cust_code__bil>\n");*/
	    	   	/*valueXmlString.append("<net_tot_amt><![CDATA["+rs.getString("NET_TOT_AMT")+"]]>></net_tot_amt>\n");*/
	    		valueXmlString.append("<confirmed><![CDATA["+checkNull(rs1.getString("CONFIRMED"))+"]]></confirmed>\n");// uncommented by nandkumar gadkari on 17/01/19
	    	   	/*valueXmlString.append("<desp_id><![CDATA["+rs.getString("DESP_ID")+"]]>></desp_id>\n");*/
	    	   	valueXmlString.append("<min_due_date><![CDATA["+minDate1+"]]></min_due_date>\n");
	    	   	valueXmlString.append("<max_due_date><![CDATA["+maxDate1+"]]></max_due_date>\n");
	    	   	valueXmlString.append("</header_data>\n");	    	  
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

	    	 	       
	           sql = "select * from BUSINESS_LOGIC_CHECK where CR_POLICY in " +
	                 "(select CR_POLICY from ITEMSER_CR_POLICY where ITEM_SER=?) " +
	           		 "AND SALE_ORDER=? order by BUSINESS_LOGIC_CHECK.APRV_STAT";
	           pstmt =  conn.prepareStatement(sql);
	           pstmt.setString(1,asItemSer);
			   pstmt.setString(2,saleOrder);			   
			   rs = pstmt.executeQuery();
			   
			   while(rs.next())
			   {				   
				   lsCrPolicy = rs.getString("CR_POLICY");
				   lsdescr = rs.getString("DESCR");
				   lsStatus = rs.getString("APRV_STAT");
				   sordData = new HashMap<String,Object>();
	    	       sordData.put("as_cr_policy", lsCrPolicy);
		    	   sordData.put("as_descr", lsdescr);
		    	   sordData.put("as_status",lsStatus );		    	   
				   sordDataList.add(sordData);				   
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
	    	   System.out.println("sordData ::::::::::::::"+sordData);
	    	   System.out.println("Value of sorderDataList ::::"+sordDataList);
			
	    	   int dataSize = sordDataList.size();	    	   
	    	   if(dataSize > 0)
	    	   {
	    		   lsCrPolicy = (String)sordData.get("as_cr_policy");
	    		   lsdescr = (String)sordData.get("as_descr");
	    		   lsStatus = (String)sordData.get("as_status");
	    		     		   
	    		   for(int val=0;val<dataSize;val++)
	    		   {
	    			   valueXmlString.append("<policy>");
	    			   valueXmlString.append("<lsCrPolicy><![CDATA["+lsCrPolicy+"]]></lsCrPolicy>\n");
	    			   valueXmlString.append("<lsStr><![CDATA["+lsdescr+"]]></lsStr>\n");
	    			   if("F".equalsIgnoreCase(lsStatus))
	    			   {
	    				   valueXmlString.append("<lsStatus><![CDATA[Failed]]></lsStatus>\n");
	    			   }
	    			   else
	    			   {
	    				   valueXmlString.append("<lsStatus><![CDATA[Overridden]]></lsStatus>\n");
	    			   }	    			  
	    			   valueXmlString.append("</policy>");
	    		   }	   
	    	   }//end of if block for dataSize
			   // Committed and Added by sarita on 01DEC2017
	    	  /* sql = "SELECT SORDITEM.SALE_ORDER,SORDDET.ITEM_CODE__ORD,SORDDET.LINE_NO,SORDITEM.EXP_LEV ," +
	    	   		 "SORDITEM.SITE_CODE,SORDITEM.ITEM_CODE__ORD,SORDITEM.ITEM_FLAG,SORDITEM.ITEM_CODE," +
	    	   		 "ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT,SORDITEM.QTY_ALLOC,SORDITEM.DATE_ALLOC," +
	    	   		 "SORDITEM.NATURE,SORDITEM.REAS_CODE,SORDITEM.REAS_DETAIL,SORDITEM.QTY_DESP," +
	    	   		 "SORDITEM.STATUS,SORDITEM.PLAN_PROD_DATE,SORDITEM.EXP_DLV_DATE,SORDITEM.REMARKS," +
	    	   		 "SORDDET.DSP_DATE,(SELECT SUM(QUANTITY - ALLOC_QTY - CASE WHEN HOLD_QTY IS NULL THEN 0 " +
	    	   		 "ELSE HOLD_QTY END) FROM STOCK WHERE ITEM_CODE = SORDITEM.ITEM_CODE__ORD GROUP BY " +
	    	   		 "ITEM_CODE) STK_QTY,(SORDITEM.QUANTITY -(SELECT COALESCE((SELECT SUM(QUANTITY) FROM " +
	    	   		 "DESPATCHDET WHERE SORD_NO = SORDITEM.SALE_ORDER AND ITEM_CODE = SORDITEM.ITEM_CODE " +
	    	   		 "AND LINE_NO__SORD = SORDDET.LINE_NO GROUP BY ITEM_CODE),0 ) FROM DUAL)) AS PEND_QTY " +
	    	   		 "FROM SORDITEM SORDITEM, ITEM ITEM, DESPATCHDET DESPATCHDET, DESPATCH DESPATCH,SORDDET " +
	    	   		 "SORDDET WHERE ( SORDDET.ITEM_CODE__ORD = ITEM.ITEM_CODE ) AND (SORDITEM.SALE_ORDER  " +
	    	   		 " = SORDDET.SALE_ORDER) AND (SORDITEM.ITEM_CODE    = SORDDET.ITEM_CODE) AND " +
	    	   		 "(SORDITEM.LINE_NO      = SORDDET.LINE_NO) AND ( DESPATCH.DESP_ID     = " +
	    	   		 "DESPATCHDET.DESP_ID ) AND (SORDITEM.ITEM_CODE    = DESPATCHDET.ITEM_CODE__ORD(+)) " +
	    	   		 "AND SORDITEM.SALE_ORDER    = ? GROUP BY SORDITEM.SALE_ORDER, " +
	    	   		 "SORDDET.ITEM_CODE__ORD,SORDITEM.SITE_CODE,SORDITEM.ITEM_CODE__ORD,SORDITEM.ITEM_FLAG," +
	    	   		 "SORDITEM.EXP_LEV,SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT," +
	    	   		 "SORDITEM.DATE_ALLOC,SORDITEM.QTY_ALLOC,SORDITEM.STATUS,SORDITEM.NATURE," +
	    	   		 "SORDDET.LINE_NO,SORDITEM.REAS_CODE,SORDITEM.REAS_DETAIL,SORDITEM.QTY_DESP," +
	    	   		 "SORDITEM.PLAN_PROD_DATE,SORDITEM.REMARKS,SORDDET.DSP_DATE,SORDITEM.EXP_DLV_DATE " +
	    	   		 "ORDER BY SORDDET.LINE_NO ASC, SORDITEM.EXP_LEV ASC";*/
	    	 
	    	   //Added by Pooja S on[11-12-2018] for the Unconfirmed Status
	    	   if("N".equalsIgnoreCase(confirmed))
	    	   {
	    		   sql="SELECT SORDITEM.SALE_ORDER,SORDITEM.DSP_DATE,SORDITEM.LINE_NO ,SORDITEM.SITE_CODE,SORDITEM.ITEM_CODE,"
	    				   + "ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT,SORDITEM.NATURE,"
	    				   + "SORDITEM.STATUS,SORDITEM.REMARKS,"
	    				   + "(SELECT SUM(QUANTITY - ALLOC_QTY - CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END) FROM STOCK,LOCATION,INVSTAT"
	    				   + "  WHERE STOCK.ITEM_CODE = SORDITEM.ITEM_CODE AND STOCK.SITE_CODE = SORDITEM.SITE_CODE AND LOCATION.LOC_CODE=STOCK.LOC_CODE"
	    				   + " AND LOCATION.INV_STAT = INVSTAT.INV_STAT AND INVSTAT.AVAILABLE= 'Y') STK_QTY, (SORDITEM.QUANTITY) AS PEND_QTY "
	    				   + "FROM SORDDET SORDITEM, ITEM WHERE (SORDITEM.ITEM_CODE        = item.ITEM_CODE) AND SORDITEM.SALE_ORDER        = ?"
	    				   + "GROUP BY SORDITEM.SALE_ORDER,"
	    				   + "SORDITEM.SITE_CODE,SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT,"
	    				   + "SORDITEM.STATUS,SORDITEM.NATURE,SORDITEM.LINE_NO,"
	    				   + " SORDITEM.REMARKS,SORDITEM.DSP_DATE ORDER BY SORDITEM.LINE_NO ASC";


	    		   pstmt =  conn.prepareStatement(sql);
	    		   pstmt.setString(1,saleOrder);
	    		   rs = pstmt.executeQuery();
	    		   while(rs.next())
	    		   {
	    			   despqty="0";
	    			   despDate = rs.getString("DSP_DATE");					
						if(despDate != null)
				    	{
							despDate1 =  genericUtility.getValidDateString(despDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
				    	}

	    			   valueXmlString.append("<Item>\n");
	    			   valueXmlString.append("<sale_order><![CDATA["+checkNull(rs.getString("SALE_ORDER"))+"]]></sale_order>\n");
	    			   valueXmlString.append("<line_no><![CDATA["+checkNull(rs.getString("LINE_NO"))+"]]></line_no>\n");
	    			   valueXmlString.append("<item_code><![CDATA["+checkNull(rs.getString("ITEM_CODE"))+"]]></item_code>\n");
	    			   valueXmlString.append("<item_descr><![CDATA["+checkNull(rs.getString("DESCR"))+"]]></item_descr>\n");
	    			   valueXmlString.append("<quantity><![CDATA["+checkNull(rs.getString("QUANTITY"))+"]]></quantity>\n");
	    			   valueXmlString.append("<unit><![CDATA["+checkNull(rs.getString("UNIT"))+"]]></unit>\n");
	    			   //valueXmlString.append("<date_alloc><![CDATA["+checkNull(rs.getString("DATE_ALLOC"))+"]]></date_alloc>\n");
	    			   //valueXmlString.append("<qty_alloc><![CDATA["+checkNull(rs.getString("QTY_ALLOC"))+"]]></qty_alloc>\n");
	    			   valueXmlString.append("<status><![CDATA["+checkNull(rs.getString("STATUS"))+"]]></status>\n");
	    			   valueXmlString.append("<nature><![CDATA["+checkNull(rs.getString("NATURE"))+"]]></nature>\n");
	    			   //valueXmlString.append("<read_code><![CDATA["+checkNull(rs.getString("REAS_CODE"))+"]]></read_code>\n");
	    			   //valueXmlString.append("<reas_detail><![CDATA["+checkNull(rs.getString("REAS_DETAIL"))+"]]></reas_detail>\n");
	    			   valueXmlString.append("<desp_qty><![CDATA["+ despqty +"]]></desp_qty>\n");
	    			   //valueXmlString.append("<plan_prod_date><![CDATA["+checkNull(rs.getString("PLAN_PROD_DATE"))+"]]></plan_prod_date>\n");
	    			   valueXmlString.append("<remarks><![CDATA["+checkNull(rs.getString("REMARKS"))+"]]></remarks>\n");
	    			   valueXmlString.append("<dsp_date><![CDATA["+despDate1+"]]></dsp_date>\n");
	    			   //valueXmlString.append("<exp_dlv_date><![CDATA["+checkNull(rs.getString("EXP_DLV_DATE"))+"]]></exp_dlv_date>\n");
	    			   valueXmlString.append("<pend_qty><![CDATA["+checkNull(rs.getString("PEND_QTY"))+"]]></pend_qty>\n");
	    			   valueXmlString.append("<stk_qty><![CDATA["+checkNull(rs.getString("STK_QTY"))+"]]></stk_qty>\n");
	    			   valueXmlString.append("</Item>");
	    		   }
	    	   }
	    	   else
	    	   {
	    		   sql = "SELECT SORDITEM.SALE_ORDER,SORDITEM.ITEM_CODE,SORDITEM.LINE_NO,SORDITEM.EXP_LEV ," +
	    				   "SORDITEM.SITE_CODE,SORDITEM.ITEM_FLAG,SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY," +
	    				   "SORDITEM.UNIT,SORDITEM.QTY_ALLOC,SORDITEM.DATE_ALLOC,SORDITEM.NATURE,SORDITEM.REAS_CODE," +
	    				   "SORDITEM.REAS_DETAIL,SORDITEM.QTY_DESP,SORDITEM.STATUS,SORDITEM.PLAN_PROD_DATE,SORDITEM.EXP_DLV_DATE," +
	    				   "SORDITEM.REMARKS,SORDITEM.DUE_DATE,(SELECT SUM(QUANTITY - ALLOC_QTY - CASE WHEN HOLD_QTY IS NULL THEN 0 ELSE HOLD_QTY END) " +
	    				   "FROM STOCK,LOCATION,INVSTAT WHERE STOCK.ITEM_CODE = SORDITEM.ITEM_CODE AND STOCK.SITE_CODE = SORDITEM.SITE_CODE " +
	    				   "AND LOCATION.LOC_CODE=STOCK.LOC_CODE AND LOCATION.INV_STAT = INVSTAT.INV_STAT AND INVSTAT.AVAILABLE= 'Y') STK_QTY, " +
	    				   "(SORDITEM.QUANTITY - SORDITEM.qty_desp) AS PEND_QTY FROM SORDITEM, ITEM WHERE (SORDITEM.ITEM_CODE        = item.ITEM_CODE) " +
	    				   "AND SORDITEM.SALE_ORDER        = ? GROUP BY SORDITEM.SALE_ORDER,SORDITEM.SITE_CODE,SORDITEM.ITEM_FLAG,SORDITEM.EXP_LEV," +
	    				   "SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,SORDITEM.UNIT,SORDITEM.DATE_ALLOC,SORDITEM.QTY_ALLOC,SORDITEM.STATUS,SORDITEM.NATURE," +
	    				   "SORDITEM.LINE_NO,SORDITEM.REAS_CODE,SORDITEM.REAS_DETAIL,SORDITEM.QTY_DESP,SORDITEM.PLAN_PROD_DATE,SORDITEM.REMARKS," +
	    				   "SORDitem.due_DATE,SORDITEM.EXP_DLV_DATE ORDER BY SORDITEM.LINE_NO ASC, SORDITEM.EXP_LEV ASC";
	    		   pstmt =  conn.prepareStatement(sql);
	    		   pstmt.setString(1,saleOrder);
	    		   rs = pstmt.executeQuery();
	    		   while(rs.next())
	    		   {
	    			 //Changed by Pooja S on[11-12-2018] for the Unconfirmed Status
	    			despDate = rs.getString("DUE_DATE");					
					if(despDate != null)
			    	{
						despDate1 =  genericUtility.getValidDateString(despDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
			    	}

	    			   valueXmlString.append("<Item>\n");
	    			   valueXmlString.append("<sale_order><![CDATA["+checkNull(rs.getString("SALE_ORDER"))+"]]></sale_order>\n");
	    			   valueXmlString.append("<line_no><![CDATA["+checkNull(rs.getString("LINE_NO"))+"]]></line_no>\n");
	    			   valueXmlString.append("<exp_lev><![CDATA["+checkNull(rs.getString("EXP_LEV"))+"]]></exp_lev>\n");
	    			   //valueXmlString.append("<item_code__ord><![CDATA["+checkNull(rs.getString("ITEM_CODE__ORD"))+"]]></item_code__ord>\n");
	    			   valueXmlString.append("<item_flag><![CDATA["+checkNull(rs.getString("ITEM_FLAG"))+"]]></item_flag>\n");
	    			   valueXmlString.append("<item_code><![CDATA["+checkNull(rs.getString("ITEM_CODE"))+"]]></item_code>\n");
	    			   valueXmlString.append("<item_descr><![CDATA["+checkNull(rs.getString("DESCR"))+"]]></item_descr>\n");
	    			   valueXmlString.append("<quantity><![CDATA["+checkNull(rs.getString("QUANTITY"))+"]]></quantity>\n");
	    			   valueXmlString.append("<unit><![CDATA["+checkNull(rs.getString("UNIT"))+"]]></unit>\n");
	    			   valueXmlString.append("<date_alloc><![CDATA["+checkNull(rs.getString("DATE_ALLOC"))+"]]></date_alloc>\n");
	    			   valueXmlString.append("<qty_alloc><![CDATA["+checkNull(rs.getString("QTY_ALLOC"))+"]]></qty_alloc>\n");
	    			   valueXmlString.append("<status><![CDATA["+checkNull(rs.getString("STATUS"))+"]]></status>\n");
	    			   valueXmlString.append("<nature><![CDATA["+checkNull(rs.getString("NATURE"))+"]]></nature>\n");
	    			   valueXmlString.append("<read_code><![CDATA["+checkNull(rs.getString("REAS_CODE"))+"]]></read_code>\n");
	    			   valueXmlString.append("<reas_detail><![CDATA["+checkNull(rs.getString("REAS_DETAIL"))+"]]></reas_detail>\n");
	    			   valueXmlString.append("<desp_qty><![CDATA["+checkNull(rs.getString("QTY_DESP"))+"]]></desp_qty>\n");
	    			   valueXmlString.append("<plan_prod_date><![CDATA["+checkNull(rs.getString("PLAN_PROD_DATE"))+"]]></plan_prod_date>\n");
	    			   valueXmlString.append("<remarks><![CDATA["+checkNull(rs.getString("REMARKS"))+"]]></remarks>\n");
	    			   valueXmlString.append("<dsp_date><![CDATA["+despDate1+"]]></dsp_date>\n");
	    			   valueXmlString.append("<exp_dlv_date><![CDATA["+checkNull(rs.getString("EXP_DLV_DATE"))+"]]></exp_dlv_date>\n");
	    			   valueXmlString.append("<pend_qty><![CDATA["+checkNull(rs.getString("PEND_QTY"))+"]]></pend_qty>\n");
	    			   valueXmlString.append("<stk_qty><![CDATA["+checkNull(rs.getString("STK_QTY"))+"]]></stk_qty>\n");
	    			   valueXmlString.append("</Item>");
	    		   }
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
				
		    sql = "SELECT DESPATCH.DESP_ID,DESPATCH.TRANS_MODE,INVOICE.INVOICE_ID,(SELECT TRAN_NAME FROM TRANSPORTER " +
		    	  "WHERE TRAN_CODE =(SELECT D.TRAN_CODE FROM DESPATCH D WHERE D.DESP_ID = DESPATCH.DESP_ID)) TRAN_NAME," +
		    	  "INVOICE.TRAN_DATE FROM DESPATCH DESPATCH,INVOICE INVOICE WHERE ( INVOICE.DESP_ID  = DESPATCH.DESP_ID ) " +
		    	  "AND ( INVOICE.SALE_ORDER = DESPATCH.SORD_NO ) AND INVOICE.SALE_ORDER   = ?";
		    
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1,saleOrder);
			rs = pstmt.executeQuery();
			
			while(rs.next())
			{
				tranDate = rs.getString("TRAN_DATE");
				if(tranDate != null)
		    	{
					tranDate1 =  genericUtility.getValidDateString(tranDate, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
		    	}
				valueXmlString.append("<Invoice>\n");
				valueXmlString.append("<desp_id><![CDATA["+checkNull(rs.getString("DESP_ID"))+"]]></desp_id>\n");
				valueXmlString.append("<trans_mode><![CDATA["+checkNull(rs.getString("TRANS_MODE"))+"]]></trans_mode>\n");
				valueXmlString.append("<tran_name><![CDATA["+checkNull(rs.getString("TRAN_NAME"))+"]]></tran_name>\n");
				valueXmlString.append("<invoice_id><![CDATA["+checkNull(rs.getString("INVOICE_ID"))+"]]></invoice_id>\n");
				valueXmlString.append("<tran_date><![CDATA["+checkNull(tranDate1)+"]]></tran_date>\n");
				valueXmlString.append("</Invoice>\n");
			}				   
   			valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");
		}//end of try block
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Inside SorderStatusEJB getSorderStatusXML method ::"+e.getMessage());
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
								
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}//end of method getSorderStatusXML
	
	public String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input.trim();
	}
}
	

