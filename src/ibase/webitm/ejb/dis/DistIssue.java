package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3


//public class DistIssueEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class DistIssue extends ValidatorEJB implements DistIssueLocal, DistIssueRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
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
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		System.out.println("itemChanged calling after clcking on Default button...........");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String retString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2);
			}
			retString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :DespatchEJB :itemChanged(String,String) :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return retString; 
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("itemChanged calling after clcking on Default button...........");
		String lineNoSorder = "", tranType = "", sql = "", distOrder = "", priceList = "", availableYn = ""; 
		String itemCode = "", taxClass = "", taxChap = "", taxEnv = "", columnValue = "";
		String descr = "", unit = "", unitAlt = "", packCode = "", tranTypeParent = "";
		String packInstr = ""; //Gulzar 01/03/07
		double quantity = 0d, qtyShipped = 0d,rate = 0d, discount = 0d, rateClg = 0d;  
		double convQtyAlt = 0d; //Gulzar 24/03/07
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int currentFormNo = 0; 
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
			System.out.println("\n\nDespatchEJB : itemChanged["+currentColumn+"] : xtraParams :"+xtraParams);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			editFlag = genericUtility.getColumnValue("editFlag",dom);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root>");
			System.out.println("\n\nDespatchEJB : itemChanged Obj Context: "+objContext);

			switch (currentFormNo)
			{
				case 2:
					valueXmlString.append("<Detail2>");
					//if(currentColumn.equalsIgnoreCase("line_no_ord"))
					if(currentColumn.equalsIgnoreCase("line_no_dist_order"))
					{
						lineNoSorder = genericUtility.getColumnValue(currentColumn, dom);
						if (lineNoSorder != null)
						{
							System.out.println("lineNoOrder :"+lineNoSorder);
							distOrder = genericUtility.getColumnValue("dist_order",dom);
							System.out.println("distOrder :"+distOrder);
							tranType = genericUtility.getColumnValue("tran_type",dom1);
							System.out.println("tranType :"+tranType);
							sql = "SELECT ITEM_CODE, CASE WHEN QTY_CONFIRM IS NULL THEN 0 ELSE QTY_CONFIRM END - " 
								 +"CASE WHEN QTY_SHIPPED IS NULL THEN 0 ELSE QTY_SHIPPED END, "	
								 +"CASE WHEN QTY_SHIPPED IS NULL THEN 0 ELSE QTY_SHIPPED END - "
								 +"CASE WHEN QTY_RETURN IS NULL THEN 0 ELSE QTY_RETURN END, "
								 +"CASE WHEN TAX_CLASS IS NULL THEN '' ELSE TAX_CLASS END, CASE WHEN TAX_CHAP IS NULL THEN '' ELSE TAX_CHAP END, CASE WHEN TAX_ENV IS NULL THEN '' ELSE TAX_ENV END, CASE WHEN RATE IS NULL THEN 0 ELSE RATE END, "
								 +"CASE WHEN DISCOUNT IS NULL THEN 0 ELSE DISCOUNT END, RATE__CLG, UNIT, UNIT__ALT, " 
								 +"PACK_INSTR, "  //Added By Gulzar 01/03/07 as added by Fatima in itemchange
								 +"CONV__QTY__ALT " //Added By Gulzar 24/03/07 as it is present in itemchange
								 +"FROM DISTORDER_DET WHERE DIST_ORDER = '"+distOrder+"' "
								 +"AND LINE_NO = "+lineNoSorder+"";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								itemCode = rs.getString(1);
								System.out.println("itemCode :"+itemCode);
								quantity = rs.getDouble(2);
								System.out.println("quantity :"+quantity);
								qtyShipped = rs.getDouble(3);
								System.out.println("qtyShipped :"+qtyShipped);
								taxClass = rs.getString(4);

								System.out.println("taxClass :"+taxClass);
								taxChap = rs.getString(5);
								System.out.println("taxChap :"+taxChap);
								taxEnv = rs.getString(6);
								System.out.println("taxEnv :"+taxEnv);
								rate = rs.getDouble(7);
								System.out.println("rate :"+rate);
								discount = rs.getDouble(8);
								System.out.println("discount :"+discount);
								rateClg = rs.getDouble(9);
								System.out.println("rateClg :"+rateClg);
								unit = rs.getString(10);
								System.out.println("Unit :"+unit);
								unitAlt = rs.getString(11);
								System.out.println("Alt Unit :"+unitAlt);
								packInstr = rs.getString(12); //Added By Gulzar 01/03/07
								System.out.println("packInstr :"+packInstr); //Added By Gulzar 01/03/07
								convQtyAlt = rs.getDouble(13); //Added By Gulzar 24/03/07
								System.out.println("convQtyAlt :"+convQtyAlt); //Added By Gulzar 24/03/07
							}
							//Added By Gulzar 29/03/07 - This logic is present in itemchange of line_no_dist_order in nvo
							if (unitAlt == null || unitAlt.trim().length() == 0)
							{
								unitAlt = unit;
								convQtyAlt = 1;
							}
							//End Added By Gulzar 29/03/07
							valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>");
							valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>");
							valueXmlString.append("<unit__alt>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>");
							valueXmlString.append("<pack_instr>").append("<![CDATA[").append(packInstr).append("]]>").append("</pack_instr>"); //Gulzar 01/03/07 as added by Fatima in in itemchange
							valueXmlString.append("<conv__qty__alt>").append("<![CDATA[").append(convQtyAlt).append("]]>").append("</conv__qty__alt>"); //Gulzar 24/03/07 it is not set in default button
							sql = "SELECT DESCR, PACK_CODE FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								descr = rs.getString(1);
								System.out.println("descr :"+descr);
								packCode = rs.getString(2);
								System.out.println("packCode :"+packCode);
								valueXmlString.append("<item_descr>").append("<![CDATA[").append(descr).append("]]>").append("</item_descr>");
							}
							sql = "SELECT TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE ='"+tranType+"'";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								tranTypeParent = rs.getString(1);
								System.out.println("tranTypeParent :"+tranTypeParent);
							}
							if (!tranType.equals(tranTypeParent))
							{
								valueXmlString.append("<quantity>").append("<![CDATA[").append(qtyShipped).append("]]>").append("</quantity>");
								valueXmlString.append("<qty_order__alt>").append("<![CDATA[").append(qtyShipped).append("]]>").append("</qty_order__alt>");
							}
							else
							{
								valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>");
								valueXmlString.append("<qty_order__alt>").append("<![CDATA[").append(quantity).append("]]>").append("</qty_order__alt>");
							}
							/*-- Commented By Gulzar 01/03/07 as it has been commented in itemchange of nvo_bo_dist_issue
							sql = "SELECT PRICE_LIST FROM DISTORDER WHERE DIST_ORDER = '"+distOrder+"'";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								priceList = rs.getString(1);
								System.out.println("priceList :"+priceList);
							}
							availableYn = genericUtility.getColumnValue("available_yn",dom1);
							System.out.println("availableYn :"+availableYn);
							if ((priceList == null || priceList.trim().length() == 0) && availableYn.equals("N") && rate == 0)
							{
								rate = 0.01;
							}
							*///End Comment Gulzar 01/03/07
							valueXmlString.append("<tax_class>").append("<![CDATA[").append((taxClass == null) ? "":taxClass).append("]]>").append("</tax_class>");
							valueXmlString.append("<tax_chap>").append("<![CDATA[").append((taxChap == null) ? "":taxChap).append("]]>").append("</tax_chap>");
							valueXmlString.append("<tax_env>").append("<![CDATA[").append((taxEnv == null) ? "":taxEnv).append("]]>").append("</tax_env>");
							valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>");
							valueXmlString.append("<rate__clg>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>");
							valueXmlString.append("<discount>").append("<![CDATA[").append(discount).append("]]>").append("</discount>");
						}//end if
					}
				valueXmlString.append("</Detail2>");
			}//switch end
			conn.close();
			valueXmlString.append("</Root>");	
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :DistIssueEJB :itemChanged(String,String) :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("The return XML-String from DistIssueEJB :"+valueXmlString.toString());
		return valueXmlString.toString();
	}//function end
}//class end