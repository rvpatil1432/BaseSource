/*
 * ActionDefault has been transferred as Service Handler 4
 * 
 */
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import javax.annotation.*;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class EnquiryAct extends ActionHandlerEJB implements EnquiryActLocal, EnquiryActRemote
{
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
		String  resString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			if (actionType.equalsIgnoreCase("Indents"))
			{
				resString = actionPickList();
			}
			if (actionType.equalsIgnoreCase("Term Table"))
			{
				dom = genericUtility.parseString(xmlString); 
				resString = actionTermTable(dom, objContext, xtraParams);
			}
			if (actionType.equalsIgnoreCase("Default"))
			{
				dom = genericUtility.parseString(xmlString); 
				resString = actionDefault(dom, objContext, xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Enquiry :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionPickList actionHandler"+resString);
	    return (resString);
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		System.out.println("actionHandlerTransform is calling.............");
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			if(selDataStr != null && selDataStr.length() > 0)
			{
				selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
			}

			System.out.println("actionType:"+actionType+":");
						
			if (actionType.equalsIgnoreCase("Indents"))
			{
				retString = pickListTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("Default"))
			{
				retString = defaultTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Enquiry :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from Enquiry : actionHandlerTransform"+retString);
	    return retString;
	}
   
	private String actionPickList() throws RemoteException , ITMException
	{
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		java.sql.Date reqDate = null, indDate = null;
		String reqDate1 = "", indDate1 = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		int cnt = 0;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			sql="SELECT INDENT.IND_NO, "   
					    +"INDENT.IND_DATE, "   
				        +"INDENT.DEPT_CODE, "   
				        +"INDENT.REQ_DATE, "   
						+"INDENT.ITEM_CODE, "   
				        +"INDENT.ITEM_DESCR, "   
						+"INDENT.QUANTITY, "   
						+"INDENT.UNIT__IND, "   
						+"INDENT.SITE_CODE, "   
						+"INDENT.WORK_ORDER, "   
						+"INDENT.PROJ_CODE, "   
						+"INDENT.ORD_QTY, "   
						+"INDENT.SITE_CODE__DLV, "   
						+"INDENT.QUANTITY__STDUOM, "   
						+"INDENT.CONV__QTY_STDUOM "  
				+"FROM INDENT "  
					    +"WHERE INDENT.STATUS IN ( 'A','O' ) AND QUANTITY > ORD_QTY";
			System.out.println("PickList SQL :="+sql);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			while (rs.next())
			{
				indDate = rs.getDate(2);
				if (indDate != null)
				{
					indDate1 = sdf.format(indDate);
				}
				else
				{
					indDate1 = "";
				}
				reqDate = rs.getDate(4);
				if (reqDate != null)
				{
					reqDate1 = sdf.format(reqDate);
				}
				else
				{
					reqDate1 = "";
				}
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<ind_no>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</ind_no>\r\n");
				valueXmlString.append("<ind_date>").append("<![CDATA[").append(indDate1).append("]]>").append("</ind_date>\r\n");
				valueXmlString.append("<dept_code>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</dept_code>\r\n");
				valueXmlString.append("<req_date>").append("<![CDATA[").append(reqDate1).append("]]>").append("</req_date>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<item_descr>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</item_descr>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getString(7)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(8) == null?"":rs.getString(8)).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<site_code>").append("<![CDATA[").append(rs.getString(9) == null?"":rs.getString(9)).append("]]>").append("</site_code>\r\n");
				valueXmlString.append("<work_order>").append("<![CDATA[").append(rs.getString(10) == null?"":rs.getString(10)).append("]]>").append("</work_order>\r\n");
				valueXmlString.append("<proj_code>").append("<![CDATA[").append(rs.getString(11) == null?"":rs.getString(11)).append("]]>").append("</proj_code>\r\n");
				valueXmlString.append("<ord_qty>").append("<![CDATA[").append(rs.getString(12)).append("]]>").append("</ord_qty>\r\n");
				valueXmlString.append("<site_code__dlv>").append("<![CDATA[").append(rs.getString(13) == null?"":rs.getString(13)).append("]]>").append("</site_code__dlv>\r\n");
				valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(rs.getString(14)).append("]]>").append("</quantity__stduom>\r\n");
				valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(rs.getString(15)).append("]]>").append("</conv__qty_stduom>\r\n");
				valueXmlString.append("</Detail>\r\n");
				cnt++; 
			}
			stmt.close();
			System.out.println("cnt :"+cnt);
			valueXmlString.append("</Root>\r\n");			
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Enquiry : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Enquiry : actionHandler " +e.getMessage());
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

	private String pickListTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		System.out.println("pickListTransform is calling.............");
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String sql = "", descr = "";
		String detCnt = "0";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		int count = 0;
		double quantity = 0;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Aviprash 30/01/06
			String enquiryNo = new  ibase.utility.E12GenericUtility().getColumnValue("enq_no", dom1);
			System.out.println("enquiryNo :"+enquiryNo);
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				valueXmlString.append("<Detail>");
				Node currDetail = detailList.item(ctr);
				String indNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("ind_no", currDetail);
				String code = indNo.substring(0,indNo.trim().length()-3);
				System.out.println("indNo :"+indNo+"\n Code :"+code);

				String itemCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("item_code", currDetail);
				String siteCodeDlv = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("site_code__dlv", currDetail);
				String reqDate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("req_date", currDetail);
				//String cPendingQty = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity", currDetail);
				String unitIndent = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit__ind", currDetail);
				String workOrder = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("work_order", currDetail);
				String packInstr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("pack_instr", currDetail);
				String specificInstr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("specific_instr", currDetail);
				String specialInstr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("special_instr", currDetail);
				String remarks = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("remarks", currDetail);
				System.out.println("itemCode :"+itemCode+ "\n siteCodeDlv :"+siteCodeDlv+"\n reqDate :"+reqDate+" \n cPendingQty :"+" \nunitIndent :"+"\n workOrder :"+workOrder+"\n packInstr :"+packInstr+"\n specificInstr :"+specificInstr+"\n specialInstr :"+specialInstr+"\n remarks :"+remarks);
				
				/* Commented becz the enq_dtl table not found in database -- Aviprash 30/01/06
				sql = "SELECT COUNT(*) FROM ENQ_DTL WHERE ENQ_NO = '"+enquiryNo+"' AND IND_NO = '"+indNo+"'";
				System.out.println("sqlEnqDtl :"+sql);
				rset = stmt.executeQuery(sql);
				if (rset.next())
				{
					count = rset.getInt(1);
					System.out.println("count :"+count);
				}*/
				if (count == 0 || detCnt.equals("0"))
				{
					sql = "SELECT QUANTITY - ORD_QTY FROM INDENT WHERE IND_NO = '"+indNo+"'";
					System.out.println("sql :"+sql);
					rset = stmt.executeQuery(sql);
					if (rset.next())
					{
						quantity = rset.getDouble(1);
					}
					sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rset = stmt.executeQuery(sql);
					if (rset.next())
					{
						descr = rset.getString(1);
						System.out.println("descr :"+descr);
					}						
					valueXmlString.append("<ind_no isSrvCallOnChg='1'>").append("<![CDATA[").append(indNo).append("]]>").append("</ind_no>\r\n");
					//comment by manazir on 3/5/2009
					/*valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr>").append("<![CDATA[").append(descr).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<req_date>").append("<![CDATA[").append(reqDate).append("]]>").append("</req_date>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(unitIndent == null?"":unitIndent).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<site_code__dlv>").append("<![CDATA[").append(siteCodeDlv).append("]]>").append("</site_code__dlv>\r\n");
					valueXmlString.append("<work_order>").append("<![CDATA[").append(workOrder == null?"":workOrder).append("]]>").append("</work_order>\r\n");
					valueXmlString.append("<pack_instr>").append("<![CDATA[").append(packInstr == null?"":packInstr).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("<specific_instr>").append("<![CDATA[").append(specificInstr == null?"":specificInstr).append("]]>").append("</specific_instr>\r\n");
					valueXmlString.append("<special_instr>").append("<![CDATA[").append(specialInstr == null?"":specialInstr).append("]]>").append("</special_instr>\r\n");
					valueXmlString.append("<remarks>").append("<![CDATA[").append(remarks == null?"":remarks).append("]]>").append("</remarks>\r\n");*/
					// end of code on 3/5/2009
					valueXmlString.append("</Detail>");
				}
			}
			valueXmlString.append("</Root>");
		}
		catch(ITMException itme)
		{
			throw itme;
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		System.out.println("valueXmlString from :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionTermTable(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		String termTable = "", termTableVal  = "", varValue = "", sql = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		int count = 0;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			termTable = genericUtility.getColumnValue("term_table",dom1);
			
			if (termTable != null)
			{
				varValue = termTable;
			}
			else 
			{
				sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='DEF_PTERM_ENQ' ";
				System.out.println("sql :"+sql);
				
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					varValue = rs.getString(1);					
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
			}
			System.out.println("Term Table :: "+varValue);
			
			sql = "SELECT COUNT(*) FROM PUR_TERM_TABLE WHERE TERM_TABLE = '"+varValue+"'";
			System.out.println("sql :"+sql);
			
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				count = rs.getInt(1);
				System.out.println("count :"+count);
			}
			if (count > 0)
			{
				sql = "SELECT TERM_CODE FROM PUR_TERM_TABLE WHERE TERM_TABLE = '"+varValue+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<term_code>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</term_code>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				//Added by sarita on 15NOV2017 for Closing the Open Cursor [Start]
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				//Added by sarita on 15NOV2017 for Closing the Open Cursor [end]
			}
			//commented and added by sarita for Closing the Open Cursor [Start]
			//stmt.close();
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			if( stmt != null)
			{
				stmt.close();
				stmt = null;
			}
			valueXmlString.append("</Root>\r\n");			
		}
		catch (SQLException sqx)
		{
			System.out.println("Exception : AdjIss : actionStock " +sqx.getMessage());
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("Exception : AdjIss : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String actionDefault(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		NodeList detailDom = null;
		int detailListLength = 0;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			detailDom = dom1.getElementsByTagName("Detail2");
			detailListLength = detailDom.getLength();
			if (detailListLength > 0)
			{
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<supp_code>").append("<![CDATA[").append("**********").append("]]>").append("</supp_code>\r\n");
				valueXmlString.append("<supplier_supp_name>").append("<![CDATA[").append("All Suppliers").append("]]>").append("</supplier_supp_name>\r\n");						
				valueXmlString.append("</Detail>\r\n");				

				sql = "SELECT SUPP_CODE, SUPP_NAME FROM SUPPLIER";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<supp_code>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</supp_code>\r\n");
					valueXmlString.append("<supplier_supp_name>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</supplier_supp_name>\r\n");						
					valueXmlString.append("</Detail>\r\n");
				}
			}
			else
			{
				System.out.println("No Items Found In Detail");
			}
			stmt.close();
			valueXmlString.append("</Root>\r\n");		
		}
		catch (SQLException sqx)
		{
			System.out.println("SQLException : EnquiryAct : actionDefault : " +sqx.getMessage());
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("Exception : EnquiryAct : actionDefault :(Document dom) : " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();			
	}
	
	private String defaultTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		Statement stmt = null,stmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		Connection conn = null;
		String itemCode = " ", siteCode = " ", suppCode = " ", supp  = " ", tranSer = "ENQ", enqNo = "", changed = "N", sql = "", sql1 = "", sql2 = "", keyVal = "", errCode = "", name = "", lsSite = "", lsValue = "", descr = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		int count = 0, i = 0, grow = 0, row = 0, llGrow = 0, iGrow = 0;
		int stop = 0;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			ArrayList suppItemArr = suppItemList(dom1);
			ArrayList itemArr = itemList(dom);
			stmt = conn.createStatement();

			NodeList nodeList = dom.getElementsByTagName("Detail1");
			Node node = nodeList.item(0);
			enqNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("enq_no",node);
			
			NodeList selNode = 	selDataDom.getElementsByTagName("Detail");			
			lsValue = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("supp_code",selNode.item(0));

			System.out.println("Enq No : " + enqNo);
			llGrow = itemArr.size();
			System.out.println("llGrow : " + llGrow);
			if (llGrow == 0)
			{
				System.out.println("No Item Code Present in Detail");
				errCode = itmDBAccess.getErrorString("","VTNOREC2","","",conn);
				System.out.println("Error :: "+errCode);
			}
			System.out.println("lsValue : " + lsValue);
			if (errCode.length() == 0)
			{
				if (lsValue.equalsIgnoreCase("**********") )
				{
					for(iGrow = 0; iGrow < llGrow; iGrow++)
					{
						itemCode = itemArr.get(iGrow).toString();
						System.out.println("Item Code : " + itemCode);
					
						sql = "SELECT COUNT(*) FROM SUPPLIERITEM WHERE ITEM_CODE = '"+ itemCode + "' AND PO_ENQ = 'Y'";
						System.out.println("SQL1 : " + sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							count = rs.getInt(1);
							System.out.println("Count : " + count);
						}
						if (count == 0)
						{
							errCode = itmDBAccess.getErrorString("","VTNOITSUPP","","",conn);
							System.out.println("Error :: "+errCode);
						}
						if (errCode.trim().length() == 0)
						{
							sql = "SELECT SUPP_CODE FROM SUPPLIERITEM WHERE ITEM_CODE = '"+ itemCode + "' AND PO_ENQ = 'Y'";
							System.out.println("SQL2 : " + sql);
							rs = stmt.executeQuery(sql);
							while(rs.next())
							{
								suppCode = rs.getString(1);
								System.out.println("Supp Code : "  + suppCode);
								nodeList = dom.getElementsByTagName("Detail1");
								node = nodeList.item(0);
							   	siteCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("site_code",node);
								System.out.println("Site Code From Dom1 : " + siteCode);
								errCode = supplier(siteCode, suppCode, tranSer); 
								System.out.println("Error Code..." + errCode);
								
								if(errCode.trim().length() == 0)
								{
									row = find(suppCode, itemCode, suppItemArr); 
									System.out.println("Rows : " + row);
									if(row == 0)
									{
										sql1 = "SELECT SUPP_NAME FROM SUPPLIER WHERE SUPP_CODE = '" + suppCode + "'";
										System.out.println("SQL11 : " + sql1);
										stmt1 = conn.createStatement();
										rs1 = stmt1.executeQuery(sql1);
										if (rs1.next())
										{
											name = rs1.getString(1);
											System.out.println("Supp Name ... : " + name);
										}
																				
										sql2 = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
										System.out.println("SQL2 : " + sql2);
										rs2 = stmt1.executeQuery(sql2);
										if (rs2.next())
										{
											descr = rs2.getString(1);
											System.out.println("Descr...: " + descr);
										}
										valueXmlString.append("<Detail>\r\n");
										valueXmlString.append("<enq_no>").append("<![CDATA[").append(enqNo).append("]]>").append("</enq_no>\r\n");
										valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
										valueXmlString.append("<item_descr>").append("<![CDATA[").append(descr).append("]]>").append("</item_descr>\r\n");
										valueXmlString.append("<supp_code>").append("<![CDATA[").append(suppCode).append("]]>").append("</supp_code>\r\n");
										valueXmlString.append("<supplier_supp_name>").append("<![CDATA[").append(name).append("]]>").append("</supplier_supp_name>\r\n");
										valueXmlString.append("<print_status>").append("<![CDATA[").append("N").append("]]>").append("</print_status>\r\n");
										valueXmlString.append("</Detail>\r\n");	
										changed = "Y";
										suppItemArr.add(suppCode+"@"+itemCode);
										stmt1.close(); 
									}
								}
							}
						}
					}
				}
				else
				{
					for(i=0; i < llGrow; i++)
					{
						itemCode = itemArr.get(i).toString();
						System.out.println("Item Code : " + itemCode);
						System.out.println("Supp Code : " + lsValue);
						row = find(lsValue, itemCode, suppItemArr);
						System.out.println("Rows : " + row);
						if(row == 0)
						{
							sql = "SELECT SUPP_NAME FROM SUPPLIER WHERE SUPP_CODE = '" + lsValue + "'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								name = rs.getString(1);
							}
							rs.close();
							sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								descr = rs.getString(1);
							}
							valueXmlString.append("<Detail>\r\n");
							valueXmlString.append("<enq_no>").append("<![CDATA[").append(enqNo).append("]]>").append("</enq_no>\r\n");
							valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
							valueXmlString.append("<item_descr>").append("<![CDATA[").append(descr).append("]]>").append("</item_descr>\r\n");
							valueXmlString.append("<supp_code>").append("<![CDATA[").append(lsValue).append("]]>").append("</supp_code>\r\n");
							valueXmlString.append("<supplier_supp_name>").append("<![CDATA[").append(name).append("]]>").append("</supplier_supp_name>\r\n");
							valueXmlString.append("<print_status>").append("<![CDATA[").append("N").append("]]>").append("</print_status>\r\n");
							valueXmlString.append("</Detail>\r\n");	
							changed = "Y";	
							suppItemArr.add(suppCode+"@"+itemCode);					
						}
					}
				}
			}
			valueXmlString.append("</Root>\r\n");
			/*if (errCode.trim().length() > 0 )
			{
				valueXmlString = errCode;
			} */
			stmt.close();
		}
		catch (SQLException sqx)
		{
			System.out.println("SQLException : EnquiryAct : defaultTransform : " +sqx.getMessage());
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("Exception : EnquiryAct : defaultTransform :(Document dom) : " + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();				
	}

	private ArrayList suppItemList(Document dom)
	{
		ArrayList retArrList = new ArrayList();
		String nodeName = "", nodeValue = "";
		String suppCode = " ", itemCode = " ";
		int totalNodes = 0;
		int ctr = 0;
		NodeList nodeList = null;
		try
		{
			nodeList = dom.getElementsByTagName("Detail4");
			totalNodes = nodeList.getLength();
			for (ctr = 0;ctr < totalNodes; ctr++)
			{
				Node node = nodeList.item(ctr);
							
				Node valueNode = node.getFirstChild();
				while (valueNode != null)
				{
					nodeName = valueNode.getNodeName();
					if(nodeName.trim().equalsIgnoreCase("supp_code"))
					{
						suppCode = valueNode.getFirstChild().getNodeValue();
					}
					if(nodeName.trim().equalsIgnoreCase("item_code"))
					{
						itemCode = valueNode.getFirstChild().getNodeValue();
					}
					valueNode = valueNode.getNextSibling();					
				}
				retArrList.add(suppCode.trim()+ "@" + itemCode.trim());
			}			
		}
		catch(Exception e)
		{
			System.out.println("Exception [suppItemList]: " + e);
		}
		return retArrList;
	}

	private ArrayList itemList(Document dom)
	{
		ArrayList retArrList = new ArrayList();
		String nodeName = "", nodeValue = "";
		String itemCode = " ";
		int totalNodes = 0;
		int ctr = 0;
		NodeList nodeList = null;
		try
		{
			nodeList = dom.getElementsByTagName("Detail2");
			totalNodes = nodeList.getLength();
			for (ctr = 0;ctr < totalNodes; ctr ++)
			{
				Node node = nodeList.item(ctr);
								
				Node valueNode = node.getFirstChild();
				while (valueNode != null)
				{
					nodeName = valueNode.getNodeName();
					if(nodeName.trim().equalsIgnoreCase("item_code"))
					{
						itemCode = valueNode.getFirstChild().getNodeValue();
					}
					valueNode = valueNode.getNextSibling();
				}
				retArrList.add(itemCode);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception [suppItemList]: " + e);
		}
		return retArrList;
	}

	private int find(String suppCode, String itemCode, ArrayList suppItemArr)
	{
		int retCnt = 0;
		String suppItem = suppCode.trim()+"@"+itemCode.trim();
		System.out.println("SuppItem : " + suppItem);
		if(suppItemArr.contains(suppItem.trim()))
		{
			retCnt = 1;
		}
		else
		{
			retCnt = 0;
		}
		return retCnt;
	}

	private String supplier(String siteCode, String suppCode, String tranSer)
	{
		String errCode = "", mVarValue = "" , blkList = "", sql = "";
		long count = 0;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			
			sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'SITE_SPECIFIC_SUPP'";
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				mVarValue = rs.getString(1);
			}
			// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
			if ( stmt != null )
			{
				stmt.close();
				stmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
			if(mVarValue.equalsIgnoreCase("Y"))
			{
				sql = "SELECT COUNT(*) FROM SITE_SUPPLIER WHERE SITE_CODE = '" + siteCode + "' AND SUPP_CODE = '" + suppCode + "'";
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				if(count == 0)
				{
					errCode = itmDBAccess.getErrorString("","VTSUPP2","","",conn);
				}
			}
			sql = "SELECT COUNT(*) FROM SUPPLIER WHERE SUPP_CODE = '" + suppCode + "'";
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			if(count == 0)
			{
				errCode = itmDBAccess.getErrorString("","VTSUPP1","","",conn);
			}
			if (errCode.trim().length() == 0)
			{
				sql = "SELECT CASE WHEN  BLACK_LIST IS NULL THEN 'N' ELSE BLACK_LIST END FROM SUPPLIER WHERE SUPP_CODE = '" + suppCode + "'";
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					blkList = rs.getString(1);
				}
				if(blkList.equalsIgnoreCase("Y"))
				{
					errCode = "VTSUPPBL";
				}
			}			
		}
		catch (SQLException sqx)
		{
			System.out.println("SQLException : EnquiryAct : supplier : " +sqx.getMessage());
			errCode = sqx.getMessage();
		}
		catch(Exception e)
		{
			System.out.println("Exception : EnquiryAct : supplier :(Document dom)" +e.getMessage());
			errCode = e.getMessage();
		}
		finally
		{
			try{
				conn.close();
				conn = null;
			}catch(Exception e){}
		}					
		return errCode;
	}
}