
package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import ibase.webitm.ejb.mfg.ExplodeBom;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3


public class MaterialTransferPrc extends ProcessEJB //implements SessionBean
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	ArrayList itemQtyList = new ArrayList();
	StringBuffer msgString = null;
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		System.out.println("Create Method Called....");
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
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		Document dom = null;
		String retStr = "";
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null;
		ResultSet rs = null,rs1 = null,rs2 = null;
		String sql = "";
        String retString = "" ,errCode = "",siteCodeSupp ="";
		String workOrderFrom ="",workOrderTo ="", workOrderFromDate ="",workOrderToDate ="";
		String workorder = null;
		String itemCode = "",bomCode = "",site ="" ,itemCodeBean ="",retXmlString ="",dueDate2 ="";
		double quantity =0,balQty =0, demandQty = 0, suppQty = 0,qtyfromStock =0;
		double qcleadtime = 0,mfgleadtime = 0,yieldperc =0;
		Timestamp fromDate = null, toDate =null,dueDate =null;
		///////////////////////////////////////////
		Connection conn = null;
		String errorString = null;
		HashMap siteMap =new HashMap();
		Vector siteList = new Vector();
		ExplodeBom exBom = new ExplodeBom();
		msgString = new StringBuffer( "" );
		///////////////////////////////////////////
		StringBuffer comXmlString = new StringBuffer("<Root>");

		System.out.println("xmlString[process]::::::::::;;;"+xmlString);
		System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
		System.out.println("windowName[process]::::::::::;;;"+windowName);
		System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString);
				System.out.println("dom" + dom);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception PrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
		}
		try
		{
			 ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Poonam on 08-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Poonam on 08-06-2016 :END

			 connDriver = null;
			 conn.setAutoCommit(false);
			 site = genericUtility.getColumnValue("site_code",dom);
			 workOrderFrom = genericUtility.getColumnValue("work_order__from",dom);
			 workOrderTo = genericUtility.getColumnValue("work_order__to",dom);
			 workOrderFromDate = genericUtility.getColumnValue("date_from",dom);
			 workOrderToDate = genericUtility.getColumnValue("date_to",dom);
			 workOrderFromDate = genericUtility.getValidDateString(workOrderFromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
			 fromDate =java.sql.Timestamp.valueOf( workOrderFromDate+ " 00:00:00.0");
			 System.out.println("fromDate..............."+fromDate);
			 workOrderToDate = genericUtility.getValidDateString(workOrderToDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
			 toDate =java.sql.Timestamp.valueOf( workOrderToDate+ " 00:00:00.0");
			 System.out.println("toDate..............."+toDate);
			int trfCnt = 0;
			 
			 // Changes done due to multiple ditribution orders creation on pressing of 
			//Process button multiple times.(transfer_status column has been added in workorder table)
			//START
			sql = "Select count(1) from workorder where "
				 +" work_order >= ? and work_order <= ? "
				 +" and site_code = ? "
				 +" and ord_date >= ? and ord_date <= ? "
				 +" and status in ('P','M')";
			pstmt  = conn.prepareStatement(sql);
			pstmt.setString( 1, workOrderFrom );
			pstmt.setString( 2, workOrderTo );
			pstmt.setString( 3, site );
			pstmt.setTimestamp(4,fromDate);
		    pstmt.setTimestamp(5,toDate);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				trfCnt = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
			if( trfCnt == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","VTNORECFND","BASE","",conn);
				return retString;
			}
			trfCnt = 0;
			
			sql = "Select count(1) from workorder where "
				 +" work_order >= ? and work_order <= ? "
				 +" and site_code = ? "
				 +" and ord_date >= ? and ord_date <= ? "
				 +" and status in ('P','M')"
                 +" and (transfer_status = ? or transfer_status is null) ";			
			pstmt  = conn.prepareStatement(sql);
			pstmt.setString( 1, workOrderFrom );
			pstmt.setString( 2, workOrderTo );
			pstmt.setString( 3, site );
			pstmt.setTimestamp(4,fromDate);
		    pstmt.setTimestamp(5,toDate);
			pstmt.setString( 6, "N" );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				trfCnt = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
			if( trfCnt == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("","VTMTRLADYT","BASE","",conn);
				return retString;
			}
			// Changes done due to multiple ditribution orders creation on pressing of 
			//Process button multiple times.(transfer_status column has been added in workorder table)
			//END
			
			 sql = "SELECT WORK_ORDER, ITEM_CODE, QUANTITY, BOM_CODE , DUE_DATE "
			       +" FROM WORKORDER "
			       +" WHERE WORK_ORDER >= '"+workOrderFrom+"' "
				   +" AND WORK_ORDER <= '"+workOrderTo+"' "
				   +" AND ORD_DATE >= ? AND ORD_DATE <= ? "
				   +" AND SITE_CODE = '"+site+"' "
				   +" AND STATUS IN ('P','M')" 
				   //change -- START
				   +" AND ( TRANSFER_STATUS IS NULL OR TRANSFER_STATUS = 'N') ";
				   //change -- END
			 System.out.println("sql..............."+sql);
			System.out.println("workOrderFrom..............."+workOrderFrom);
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,fromDate);
		    pstmt.setTimestamp(2,toDate);
			rs = pstmt.executeQuery();
			 int siteCtr = 0;
		 while (rs.next())
		 {
				workorder = rs.getString("WORK_ORDER") ;
				itemCode = rs.getString("ITEM_CODE") ;
			    quantity = rs.getDouble("QUANTITY");
				bomCode = rs.getString("BOM_CODE");
				dueDate = rs.getTimestamp("DUE_DATE");
				System.out.println("workorder..............."+workorder);
				System.out.println("itemCode..............."+itemCode);
				System.out.println("bomCode..............."+bomCode);

				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
				dueDate2 = sdf.format(dueDate);
				//System.out.println("dueDate2..... :: "+dueDate2);

				sql = "SELECT MFG_TYPE,QC_LEAD_TIME,MFG_LEAD_TIME,YIELD_PERC "
					  +" FROM SITEITEM  WHERE ITEM_CODE = '"+itemCode+"' AND SITE_CODE = '"+site+"' ";
				System.out.println("sql..... :: "+sql);
				pstmt1 = conn.prepareStatement(sql);
			//	pstmt1.setString(1,itemCode);
			//	pstmt1.setString(2,site);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					yieldperc = rs1.getDouble("YIELD_PERC");
					qcleadtime = rs1.getDouble("QC_LEAD_TIME");
					mfgleadtime = rs1.getDouble("MFG_LEAD_TIME");

				}
				rs1.close();
				rs1 =null;
				pstmt1.close();
				pstmt1 = null;

				sql = "SELECT QC_LEAD_TIME,MFG_LEAD,YIELD_PERC "
					  +" FROM ITEM  WHERE ITEM_CODE = '"+itemCode+"' ";
				System.out.println("sql..... :: "+sql);
				pstmt1   = conn.prepareStatement(sql);
			//	pstmt1.setString(1,itemCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					if(qcleadtime == 0 ) qcleadtime = rs1.getDouble("QC_LEAD_TIME");
					if(mfgleadtime == 0 ) mfgleadtime = rs1.getDouble("MFG_LEAD");
					if(yieldperc == 0 ) yieldperc = rs1.getDouble("YIELD_PERC");
				}
				rs1.close();
				rs1 =null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("yieldperc..... :: "+yieldperc);



				StringBuffer valueXmlString = new StringBuffer("<Root>");
				valueXmlString.append("<Detail>");
				valueXmlString.append("<site_code>").append("<![CDATA[").append(site).append("]]>").append("</site_code>");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>");
				valueXmlString.append("<line_type>").append("<![CDATA[").append("B").append("]]>").append("</line_type>");
				valueXmlString.append("<bom_code>").append("<![CDATA[").append(bomCode).append("]]>").append("</bom_code>");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>");
				valueXmlString.append("<due_date>").append("<![CDATA[").append(dueDate2).append("]]>").append("</due_date>");
				valueXmlString.append("<mfg_lead_time>").append("<![CDATA[").append(mfgleadtime).append("]]>").append("</mfg_lead_time>");
				valueXmlString.append("<qc_lead_time>").append("<![CDATA[").append(qcleadtime).append("]]>").append("</qc_lead_time>");
				valueXmlString.append("</Detail>");
				valueXmlString.append("</Root>");
				if(bomCode != null && bomCode.trim().length() > 0)
				{
					System.out.println("calling ....ExplodeBom");
					if(exBom == null)
					{
						System.out.println("exbom is null ");
					}
					//Changed By PriyankaC on 21SEP2018.
					//retXmlString = exBom.explodeBom(valueXmlString.toString());
					retXmlString = exBom.explodeBom(valueXmlString.toString(),conn);

					System.out.println("retXmlString..... :: "+retXmlString);
					populateTransferList(retXmlString,site,workorder,conn);
				}
			 if (itemQtyList.size() == 0)
			 {
			 	System.out.println("* * * * * *  No shortage of material sites  * * * * * *");
			 }
			// exBom = null;  //COMMENTED BY RAJENDRA ON 05/09/08
			 for(int ctr = 0; ctr < itemQtyList.size(); ctr ++)
			 {
				 MaterialtransferBean materialtransferBean =(MaterialtransferBean)itemQtyList.get(ctr);
				 itemCodeBean =materialtransferBean.getItemCode();
				 demandQty =materialtransferBean.getDemandQty();
				 suppQty =materialtransferBean.getSuppQty();
				 balQty =materialtransferBean.getBalQty();
				 System.out.println("itemCode..............."+itemCodeBean);
				 System.out.println("demandQty..............."+demandQty);
				 //System.out.println("suppQty..............."+suppQty);
				 System.out.println("balQty original..............."+balQty);
				 if(balQty > 0)
				 {
					  HashMap suppQtyMap =new HashMap();

					  sql = " SELECT SITE_CODE__SUPP "
						   +" FROM SUPPLY_SITES WHERE SITE_CODE = '"+site+"' "
						   +" ORDER BY PREF_NO " ;
					 System.out.println("sql..............."+sql);

					 pstmt2   = conn.prepareStatement(sql);
					// pstmt.setString(1,site);
					 rs2 = pstmt2.executeQuery();

					 while (rs2.next())
					 {
						 siteCodeSupp = rs2.getString("SITE_CODE__SUPP");
						 sql = " SELECT SUM(A.QUANTITY - A.ALLOC_QTY) as QUANTITY "
							  +" FROM STOCK A, INVSTAT B, LOCATION C	 "
							  +" WHERE A.LOC_CODE = C.LOC_CODE "
							  +" AND B.INV_STAT  = C.INV_STAT  "
							  +" AND A.ITEM_CODE = '"+itemCodeBean+"' "
							  +" AND A.SITE_CODE = '"+siteCodeSupp+"' "
							  +" AND (A.QUANTITY - A.ALLOC_QTY)  > 0	"
							  +" AND B.AVAILABLE = 'Y' "
							  +" AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
							   +"WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";

					    System.out.println("sql..............."+sql);
					    pstmt1   = conn.prepareStatement(sql);
					   // pstmt1.setString(1,itemCodeBean);
					   // pstmt1.setString(2,siteCodeSupp);
						System.out.println("itemCodeBean..............."+itemCodeBean);
						System.out.println("siteCodeSupp..............."+siteCodeSupp);
					    rs1 = pstmt1.executeQuery();
					    if (rs1.next())
					    {
							qtyfromStock = rs1.getDouble("QUANTITY");
							System.out.println("qtyfromStock..............."+qtyfromStock);
						//	siteMap.put(siteCodeSupp,siteCodeSupp);	  //rajendra
					    }
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 =null;
						
						
						//For keeping minimum inventry in stock as per request Id : DI89SUN213 --- START
						double minQtyStk = 0;
						sql = "Select min_qty from siteitem where site_code = ? and item_code = ?";
						System.out.println("sql..............."+sql);
					    pstmt1   = conn.prepareStatement(sql);
						pstmt1.setString( 1, siteCodeSupp );
						pstmt1.setString( 2, itemCodeBean );
						rs1 = pstmt1.executeQuery();
					    if (rs1.next())
					    {
							minQtyStk = rs1.getDouble("min_qty");
							System.out.println("minQtyStk..............."+minQtyStk);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 =null;
						if( minQtyStk == 0 )
						{
							sql = "Select min_qty from item where site_code = ? and item_code = ?";
							System.out.println("sql..............."+sql);
							pstmt1   = conn.prepareStatement(sql);
							pstmt1.setString( 1, siteCodeSupp );
							pstmt1.setString( 2, itemCodeBean );
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								minQtyStk = rs1.getDouble("min_qty");
								System.out.println("minQtyStk..............."+minQtyStk);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 =null;
						}
						qtyfromStock = qtyfromStock - minQtyStk;
						//For keeping minimum inventry in stock as per request Id : DI89SUN213 --- END
						
						
						if(qtyfromStock >= balQty )
						{
							suppQtyMap.put(siteCodeSupp,new Double(balQty));
							siteMap.put(siteCodeSupp,siteCodeSupp);	    //add
							System.out.println("siteCodeSupp 1..............."+siteCodeSupp);
							if (!siteList.contains(siteCodeSupp) )
							{
								siteList.add(siteCodeSupp);
								siteCtr++ ;
								System.out.println("Supply Site adding in siteList ["+ siteCtr + "][" + siteCodeSupp + "]");

							}
							else
							{
								System.out.println("Supply Site already exists in siteList [" + siteCodeSupp + "]");
							}
							//System.out.println("suppQtyMap....>=..........."+suppQtyMap);
							break;
						}
					//	else
						else if(qtyfromStock > 0)
						{
							siteMap.put(siteCodeSupp,siteCodeSupp);  //add
							suppQtyMap.put(siteCodeSupp,new Double(qtyfromStock));
							System.out.println("siteCodeSupp else..............."+siteCodeSupp);
							if (!siteList.contains(siteCodeSupp) )
							{
								siteList.add(siteCodeSupp);
								siteCtr++ ;
								System.out.println("Supply Site adding in siteList ["+ siteCtr + "][" + siteCodeSupp + "]");
							}
							else
							{
								System.out.println("Supply Site already exists in siteList [" + siteCodeSupp + "]");
							}
							//System.out.println("suppQtyMap....else..........."+suppQtyMap);
							balQty = balQty - qtyfromStock;
							System.out.println("balQty after..............."+balQty);
						}
					 } //end while

					 rs2.close();
					 rs2 =null;
					 pstmt2.close();
					 pstmt2 = null;
					 materialtransferBean.setSiteQtyMap(suppQtyMap);
					 itemQtyList.set(ctr,materialtransferBean);


				 }//end if

			 }//end for loop
			//added by rajendra on 28/08/08 for req id DI89sun061
			if(siteList.size() > 0 )
			 {
				System.out.println("calling ..............................createDistOrder");
				CreateDistOrder  createDistOrder  = new CreateDistOrder();
				System.out.println("siteList....["+ siteList.size() + "][" + siteList.toString()+ "]");
				retString = createDistOrder.createDistributionOrder(site,siteList,itemQtyList,xtraParams,workorder, conn) ;
				System.out.println("retString ...1.................."+retString);
				if(retString.indexOf("Success") > -1)
				{
					msgString = msgString.append(retString.substring( retString.indexOf("</Root>")+7, retString.length() ));
				}
				createDistOrder = null;
				itemQtyList.clear();
				siteMap.clear();
				siteList.clear();
			 }
			 else
			 {
			 	System.out.println("* * * * * *  No material found in defined sites  * * * * * *");

			 } 
		 } //end while
		 rs.close();
		 rs = null;
		 pstmt.close();
		 pstmt = null;
         //commented by rajendra on 28/08/08 for req id DI89sun061
			 //System.out.println("site..............."+site);
			 //System.out.println("siteMap..............."+siteMap);
			 //calling this function for creating distribution order for each supplier site
		/*	 if(siteList.size() > 0 )
			 {
				CreateDistOrder  createDistOrder  = new CreateDistOrder();
				// retString = createDistOrder.createDistributionOrder(site,siteMap,itemQtyList) ;
				System.out.println("siteList....["+ siteList.size() + "][" + siteList.toString()+ "]");
				retString = createDistOrder.createDistributionOrder(site,siteList,itemQtyList,xtraParams, conn) ;
				createDistOrder = null;
				itemQtyList.clear();
				siteMap.clear();
				siteList.clear();
			 }
			 else
			 {
			 	System.out.println("* * * * * *  No material found in defined sites  * * * * * *");

			 }   */
			
			 //VTCICONF3
			 //retString = itmDBAccessEJB.getErrorString("","VTCICONF3","BASE","",conn);

		}
	   	catch(Exception e)

		{
			   retString = "ERROR";
			   System.out.println("Exception in...."+e.getMessage());
			   e.printStackTrace();
		}
		finally
		{
			try
			{
				if (retString == null || retString.trim().length() ==0 || retString.indexOf("Success") > -1 )
				{
					conn.commit();
					System.out.println("commiting connection.............");
					//retString = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);
					// Changes made to show which distribution order nos are generated.--- start
					//String distOrdrNos = "";
					/* if(retString.indexOf("Success") > -1)
					{
						distOrdrNos = retString.substring( retString.indexOf("</Root>")+7, retString.length() );
					} */
					retString = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);
					if( msgString != null && msgString.toString().trim().length() > 0 )
					{	
						String begPart = retString.substring( 0, retString.indexOf("<trace>") + 7 );
						String endPart = retString.substring( retString.indexOf("</trace>"));
						String mainStr = begPart + "Fallowing are the Distribution Order generated. :\n" + msgString.toString() + endPart;
						retString = mainStr;
						begPart =null;
						endPart =null;
						mainStr =null;
					}
					System.out.println(" final return string when confirmed ==>"+retString);
					// Changes made to show which distribution order nos are generated.--- end
				}
				else
			    {
					conn.rollback();
					System.out.println("connection rollback.............");
					retString = itmDBAccessEJB.getErrorString("","VTDESNCONF","BASE","",conn);
				}
				if (pstmt != null)
				{
					pstmt.close();
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
			   System.out.println("Error In closing connection::==> "+e);
		       e.printStackTrace();
			}
		}
		System.out.println("returning from     "+retString);
	    return (retString);
	}

	private String populateTransferList(String retXmlString,String site,String workorder, Connection conn) throws ITMException, Exception
	{
		Document dom;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println("retXmlString ......"+retXmlString);
		String itemCode =null,itemCodeBean ="",sql ="",retString ="";
		double quantity = 0,qtyfromStock = 0,demandQty =0,balQty = 0;
		NodeList nodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String nodeName = null;
		String nodeValue = null;
		int totalNodes = 0;
        int ctr = 0;
             try
             {
                dom = genericUtility.parseString(retXmlString);
                nodeList = dom.getElementsByTagName("Detail");
                totalNodes = nodeList.getLength();
			    for (ctr = 0;ctr < totalNodes; ctr ++)
                {
					Node node = nodeList.item(ctr);
					nodeName = node.getNodeName();
					Node valueNode = node.getFirstChild();
					while (valueNode != null)
					{
						   nodeName = valueNode.getNodeName();
						   //System.out.println("nodeName..... :: "+nodeName);
						   nodeValue = valueNode.getFirstChild().getNodeValue();
						   if (nodeName.equals("item_code"))
						   {
								   itemCode = nodeValue.trim();
								   System.out.println("itemCode..... :: "+itemCode);
						   }
						   else if (nodeName.equals("quantity"))
						   {
								   quantity = Double.parseDouble(nodeValue);
								   System.out.println("quantity..... :: "+quantity);
						   }

						   valueNode = valueNode.getNextSibling();
					}//end while
					int index =itemExist(itemCode);

				    if(index > -1)
					{
					   MaterialtransferBean materialtransferBean =(MaterialtransferBean)itemQtyList.get(index);
					   itemCodeBean =materialtransferBean.getItemCode();
					   //balQty =materialtransferBean.getBalQty();
					   //System.out.println("balQty.if.............."+balQty);
					   materialtransferBean.setDemandQty(quantity);

					   itemQtyList.set(index,materialtransferBean);
					}
					else
					{
						   MaterialtransferBean materialtransferBean1 = new MaterialtransferBean();
						   materialtransferBean1.setItemCode(itemCode);

						  sql=" SELECT SUM(A.QUANTITY - A.ALLOC_QTY) as QUANTITY "
							  +" FROM STOCK A, INVSTAT B, LOCATION C	 "
							  +" WHERE A.LOC_CODE = C.LOC_CODE "
							  +" AND B.INV_STAT  = C.INV_STAT  "
							  +" AND A.ITEM_CODE = '"+itemCode+"' "
							  +" AND A.SITE_CODE = '"+site+"' "
							  +" AND (A.QUANTITY - A.ALLOC_QTY)  > 0	"
							  +" AND B.AVAILABLE = 'Y' "
							  +" AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
							  +" WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";

						  System.out.println("sql..............."+sql);
						   pstmt   = conn.prepareStatement(sql);
						  // pstmt.setString(1,itemCode);
						  // pstmt.setString(2,site);
						   rs = pstmt.executeQuery();
						   if (rs.next())
						   {
							 qtyfromStock = rs.getDouble("QUANTITY");
						   }
						   rs.close();
						   rs = null;
						   pstmt.close();
						   pstmt = null;
						   System.out.println("qtyfromStock.else.............."+qtyfromStock);

						   
						   //For keeping minimum inventry in stock as per request Id : DI89SUN213 --- START
							double minQtyStk = 0;
							sql = "Select min_qty from siteitem where site_code = ? and item_code = ?";
							System.out.println("sql..............."+sql);
							pstmt   = conn.prepareStatement(sql);
							pstmt.setString( 1, site );
							pstmt.setString( 2, itemCode );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								minQtyStk = rs.getDouble("min_qty");
								System.out.println("minQtyStk..............."+minQtyStk);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt =null;
							if( minQtyStk == 0 )
							{
								sql = "Select min_qty from item where site_code = ? and item_code = ?";
								System.out.println("sql..............."+sql);
								pstmt   = conn.prepareStatement(sql);
								pstmt.setString( 1, site );
								pstmt.setString( 2, itemCode );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									minQtyStk = rs.getDouble("min_qty");
									System.out.println("minQtyStk..............."+minQtyStk);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt =null;
							}
							qtyfromStock = qtyfromStock - minQtyStk;
							//For keeping minimum inventry in stock as per request Id : DI89SUN213 --- END
						   
						   
						   materialtransferBean1.setSuppQty(qtyfromStock);
						   materialtransferBean1.setDemandQty(quantity);
						   itemQtyList.add(materialtransferBean1);
						   materialtransferBean1 =null;
					}

                }//end for

             }	//end try
			catch(SQLException se)
			{	retString  = "ERROR";
				while(se.getNextException() != null)
				{
					 System.out.println("SQL Exception  "+se.getNextException().getMessage());
				}
			}
            catch(Exception e)
            {
				   retString  = "ERROR";
                   System.out.println("Exception : XML String ");
                   e.printStackTrace();
            }
		return retString;
	}
	private int itemExist(String itemCode) throws ITMException, Exception
	{
		int index = -1;
		String itemCodeList ="";
		MaterialtransferBean materialtransferBean=null;
		for( int ctr=0;ctr < itemQtyList.size(); ctr++)
		{
			 materialtransferBean = (MaterialtransferBean) itemQtyList.get(ctr);
			 itemCodeList	= materialtransferBean.getItemCode();
			 if(itemCode.trim().equalsIgnoreCase(itemCodeList.trim()))
			 {
					 index = ctr;
			 }
		}
		System.out.println("index.itemExist............."+index);
		return index;
	}


}

