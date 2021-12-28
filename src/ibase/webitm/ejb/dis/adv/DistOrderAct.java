/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :16/11/2005
*/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.lang.String;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;

import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DistOrderAct extends ActionHandlerEJB implements DistOrderActLocal, DistOrderActRemote
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

	public String actionHandler(String actionType,String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("DistOrderAddAll called");
		Document dom1 = null;
		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1); 
			}
			System.out.println("actionType:"+actionType);
			if (actionType.equalsIgnoreCase("AddAll"))
			{
				resString = actionAddAll(dom1,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("MultiItems"))
			{
				resString = actionMultiItems(dom1,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("Damaged"))
			{
				resString = actionDamaged(dom1,objContext,xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :DistOrderAddAll :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from action["+actionType+"] actionHandler"+resString);
	    return (resString);
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
				}
			}
			System.out.println("actionType:"+actionType+":");
						
			if (actionType.equalsIgnoreCase("MultiItems"))
			{
				retString = multiItemTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :DistOrderAct :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from DistOrderAct : actionHandlerTransform"+retString);
	    return retString;
	}

	private String actionAddAll(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "";
		String saleOrder = "";
		String priceList = "";
		String siteCodeShip = "";
		String distOrder = "";
		String lineNo ="";
		double quantity = 0;
		String taxClass = "";
		String taxChap = "";
		String taxEnv = "";
		java.sql.Date dspDate = null;
		String itemCode = "";
		String itemDescr = "";
		String unit = "";
		String unitSal = "";
		String stkDspDate = "";
		double rateClg = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
	
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{				
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			saleOrder = genericUtility.getColumnValue("sale_order",dom1);
			System.out.println("Value From DOM :saleOrder:"+saleOrder);
			if(saleOrder != null && saleOrder.trim().length() > 0)			
			{
				priceList = genericUtility.getColumnValue("price_list",dom1);
				siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom1);
				distOrder = genericUtility.getColumnValue("dist_order",dom1);
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				System.out.println("Value From DOM :priceList:"+priceList+":siteCodeShip:"+siteCodeShip+":distOrder:"+distOrder);
				sql = "SELECT LINE_NO,QUANTITY,TAX_CLASS,TAX_CHAP,TAX_ENV,DSP_DATE, "+
						" ITEM_CODE,RATE__CLG FROM SORDDET "+ 
						" WHERE SALE_ORDER ='"+saleOrder+"'  AND "+
						" LINE_NO NOT IN (SELECT LINE_NO__SORD FROM DISTORDER_DET "+
						" WHERE DIST_ORDER ='"+distOrder+"' AND SALE_ORDER ='"+saleOrder+"')";
				System.out.println("DistOrderAddAll:actionAddAll:sql:"+sql);
				rs1 = stmt1.executeQuery(sql);
				while(rs1.next())
				{
				  //:ls_line,:lc_qty,:ls_taxclass,:ls_taxchap,:ls_taxenv,:ldt_dspdt,:ls_item,:
				  //lc_rate__clg;
					lineNo = rs1.getString(1);
					quantity = rs1.getDouble(2);
					taxClass = rs1.getString(3);
					taxChap = rs1.getString(4);
					taxEnv = rs1.getString(5);
					dspDate = rs1.getDate(6);
					itemCode = rs1.getString(7);
					rateClg = rs1.getDouble(8);
					System.out.println("lineNo:"+lineNo+":quantity:"+quantity+":");
					System.out.println("taxClass:"+taxClass+":taxChap:"+taxChap+":");
					if(dspDate != null)
					{
						stkDspDate = sdf.format(dspDate);
					}
					System.out.println("itemCode:"+itemCode+":dspDate:"+dspDate+":");
					System.out.println("taxEnv:"+taxEnv+":rateClg:"+rateClg+":");
					lineNo = "     "+lineNo.trim() ;
					lineNo= lineNo.substring((lineNo.length()-3),lineNo.length());
					System.out.println("lineNo:"+lineNo+":");
					// 27/02/10 manoharan unit__alt should be set as unit__sal of item
					sql="SELECT DESCR,UNIT, UNIT__SAL FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
					System.out.println("DistOrderAddAll:actionAddAll:sql:"+sql);
					rs=stmt.executeQuery(sql);
					if(rs.next())
					{
						 itemDescr = rs.getString(1);
						 unit = rs.getString(2);
						 unitSal = rs.getString(3); // 27/02/10 manoharan unit__alt should be set as unit__sal of item
					}
					if (unitSal == null)
					{
						unitSal = unit;
					}
					//System.out.println("itemDescr:"+itemDescr+":unit:"+unit+":");
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNo).append("]]>").append("</line_no__sord>\r\n");
					valueXmlString.append("<qty_order isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity).append("]]>").append("</qty_order>\r\n");
					valueXmlString.append("<qty_confirm isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity).append("]]>").append("</qty_confirm>\r\n");
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append(taxClass == null?"":taxClass.trim()).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append(taxChap==null?"":taxChap.trim()).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv==null?"":taxEnv.trim()).append("]]>").append("</tax_env>\r\n");
					valueXmlString.append("<due_date isSrvCallOnChg='0'>").append("<![CDATA[").append(stkDspDate).append("]]>").append("</due_date>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode== null?"":itemCode.trim()).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr== null?"":itemDescr.trim()).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit==null?"":unit.trim()).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<unit__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(unitSal==null?"":unitSal.trim()).append("]]>").append("</unit__alt>\r\n");
					valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}//end While 
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : DistOrderAddAll : actionAddAll " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : DistOrderAddAll : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.......");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	
	private String actionMultiItems(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql="";
		String saleOrder="";
		String lineNo="";
		String itemCode="";
		String itemDescr="";
		double qtyStduom= 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
	
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{				
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
	  		saleOrder = genericUtility.getColumnValue("sale_order",dom1);
			System.out.println("actionMultiItems:Value From DOM :saleOrder:"+saleOrder);
			if(saleOrder != null && saleOrder.trim().length() > 0)			
			{
				sql="SELECT SORDDET.LINE_NO,SORDDET.ITEM_CODE,ITEM.DESCR,SORDDET.QUANTITY__STDUOM  "+
					" FROM SORDDET,ITEM  "+
					" WHERE SORDDET.ITEM_CODE = ITEM.ITEM_CODE "+
					" AND SORDDET.SALE_ORDER ='"+saleOrder+"'";
				System.out.println("DistOrderAddAll:actionMultiItems:sql:"+sql);
				rs= stmt.executeQuery(sql);
				while(rs.next())
				{
					lineNo = rs.getString(1);
					itemCode = rs.getString(2);
					itemDescr = rs.getString(3);
					qtyStduom = rs.getDouble(4);
					System.out.println("lineNo:"+lineNo+":itemCode:"+itemCode+":");
					System.out.println("qtyStduom:"+qtyStduom+":itemDescr:"+itemDescr+":");
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no__sord>").append("<![CDATA[").append(lineNo== null ?"":lineNo.trim()).append("]]>").append("</line_no__sord>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode==null?"":itemCode.trim()).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(qtyStduom).append("]]>").append("</quantity__stduom>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}//end While
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : DistOrderAddAll : actionAddAll " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : DistOrderAddAll : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.........");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();	
	}
	
	private String multiItemTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		NodeList detailList = null;
		Node currDetail = null;
		int detailListLength = 0;
		String lineNo = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		try
		{
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			for (int ctr=0;ctr <  detailListLength;ctr++ )
			{
				valueXmlString.append("<Detail>");
				currDetail = detailList.item(ctr);
				lineNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("line_no__sord",currDetail);
				valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append(lineNo).append("</line_no__sord>");
				valueXmlString.append("</Detail>");
			}
			valueXmlString.append("</Root>");
			System.out.println("Returning XML String ::"+valueXmlString.toString());
		}
		catch (Exception e)
		{
			System.out.println("Exception :: DistOrderAct ::multiItemTransform ::"+e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString.toString();
	}

	private String actionDamaged(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql="";
		String locCodes="";
		String locCode="";
		String stockItemCode = "";
		double stockQuantity = 0;
		int liCnt = 0;
		int rows = 0;
		String avail = "";
		String available = "";
		String site = "";
		String strLocCode = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");

		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{				
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			//comment by manazir on 2/4/2009
			/*sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='DAMAGED_ISS_LOC'";
			System.out.println("DISPARM:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				locCodes = rs.getString(1);
			}*/ 

			//changed  by manaazir hasan on 2/4/2009
			locCodes = genericUtility.getColumnValue( "loc_code__damaged", dom1 );
			// end of code on 2/4/2009 
			System.out.println("locCodes:"+locCodes+":");

			if(locCodes != null && locCodes.trim().length() >0)
			{
				StringTokenizer st = new StringTokenizer(locCodes,",");
				ArrayList totLocCode = new ArrayList();
				int totCnt = 0;
				while(st.hasMoreTokens())
				{
					locCode = st.nextToken();
					totLocCode.add("'"+locCode+"'");
					System.out.println("From StringToken:locCode:"+locCode+":");
					sql= "SELECT COUNT(*) FROM GENCODES WHERE  FLD_NAME  = 'LOCATIONS'"+
						" AND MOD_NAME  = 'W_DIST_ORDER' and fld_value = '"+locCode+"'";
					System.out.println("sql:GENCODES:"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						liCnt = rs.getInt(1);
					}
					System.out.println("liCnt:"+liCnt+":");
					// to note the location not in gencodes 
					if(liCnt == 0)
					{
						//totLocCode.remove(totCnt,"");
						if(!totLocCode.isEmpty())
						{
							System.out.println("At liCnt Condition:Removing LocCode:"+locCode+":");
							totLocCode.remove(totLocCode.indexOf("'"+locCode+"'"));
						}
					}
					// added to compare inventory status of location 
					// with header's availableyn feild 
					if(locCode != null && locCode.trim().length()!= 0)   
					{	
						avail = " ";
						sql="SELECT CASE WHEN A.AVAILABLE IS NULL THEN 'N' ELSE A.AVAILABLE END "+
							" FROM INVSTAT A,LOCATION B	WHERE  A.INV_STAT = B.INV_STAT "+
							" AND B.LOC_CODE ='"+locCode+"'" ;
						System.out.println("sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							avail = rs.getString(1); 
						}
						available =  genericUtility.getColumnValue("avaliable_yn",dom1);
						System.out.println("avail:"+avail+":available:"+available);
						if (available == null)
						{
							available = "";
						}
						if(!avail.equalsIgnoreCase(available)) 
						{
							//totLocCode.set(totCnt,"");
							if(!totLocCode.isEmpty())
							{
								System.out.println("At Avail Condition :Removing LocCode:"+locCode+":");
								totLocCode.remove(totLocCode.indexOf("'"+locCode+"'"));
							}
						}
					}
					totCnt++;
				} //end stToken
				System.out.println("totLocCode:"+totLocCode+":");
				if(!totLocCode.isEmpty())
				{
					strLocCode = totLocCode.toString();
					strLocCode = strLocCode.substring(1,strLocCode.length()-1);
				}	
				else
				{
				   strLocCode ="''";
				}
				System.out.println("strLocCode:"+strLocCode+":");
				site =  genericUtility.getColumnValue("site_code__ship",dom1);
				System.out.println("site:"+site+":");
				
				sql="SELECT STOCK.ITEM_CODE,SUM(STOCK.QUANTITY) AS QUANTITY "+
					" FROM STOCK WHERE ( STOCK.SITE_CODE ='"+site+"' ) AND " + 
					" ( STOCK.LOC_CODE IN ("+strLocCode+") ) AND "+ 
					" ( STOCK.QUANTITY > 0 ) GROUP BY STOCK.ITEM_CODE ";
				System.out.println("STOCK:sql:"+sql+":");
				rs = stmt.executeQuery(sql);
				while (rs.next())
				{
					stockItemCode = rs.getString(1);
					stockQuantity = rs.getDouble(2);
					valueXmlString.append("<Detail>\r\n");
					//commented by rajendra on 10/21/2008 for  calling itemchange at item_code for error in sun rcp 
						//valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(stockItemCode== null ?"":stockItemCode.trim()).append("]]>").append("</item_code>\r\n"); 
						valueXmlString.append("<item_code isSrvCallOnChg='1'>").append("<![CDATA[").append(stockItemCode== null ?"":stockItemCode.trim()).append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<qty_order isSrvCallOnChg='1'>").append("<![CDATA[").append(stockQuantity).append("]]>").append("</qty_order>\r\n");
					valueXmlString.append("</Detail>\r\n");
				 }//end while
			}//locCode != null
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : DistOrderAddAll : actionAddAll " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : DistOrderAddAll : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.........");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();
	}
}