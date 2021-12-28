 /* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 28/10/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SorderFormAct extends ActionHandlerEJB implements SorderFormActLocal, SorderFormActRemote
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
	   System.out.println("SorderForm called");
		Document dom = null;
		String  resString = null;
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 
			}
			System.out.println("actionType:"+actionType+":");
			if (actionType.equalsIgnoreCase("Default"))
			{
			  resString = actionDefault(dom,objContext,xtraParams);
			}
		} catch(Exception e)
		{
			System.out.println("Exception :SorderForm :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionVoucher actionHandler"+resString);
	    return resString;
	}
	private String actionDefault(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Connection conn1 = null;
		Statement stmt = null, stmt1 = null;
		ResultSet rs = null,rs1 = null;
		String sql = "";
		String errCode = "";
		int detCnt =0;
		String itemCode= "";
		String itemDescr = "";
		String itemUnit = "";
		String locType = "";
		String itemSeries = "";
		String custCode = "";
		String itemSer = "";
		String orderDate = "";
		String siteCode = "";
		String tranId = "";
		String othSer = "";
		String errString ="";
		int cnt=0;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		try
		{	
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();
			//Getting values from dom
			custCode = genericUtility.getColumnValue("cust_code",dom);
			itemSer = genericUtility.getColumnValue("item_ser",dom);
			orderDate = genericUtility.getColumnValue("order_date",dom);
			siteCode = genericUtility.getColumnValue("site_code",dom);
			tranId = genericUtility.getColumnValue("tran_id",dom);
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); 
			System.out.println("SorderForm:custCode:"+custCode+":itemSer:"+itemSer+":orderDate:"+orderDate+":siteCode:"+siteCode+":tranId:"+tranId+":");
			if(detCnt == 0)
			{
				sql="SELECT COUNT(*) FROM CUSTOMERITEM WHERE CUST_CODE ='"+custCode+"'";
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				System.out.println(" \n@@@@@ cnt @@@@:"+cnt+":");
				rs = null;
				if(cnt == 0)
				{
					sql ="SELECT A.ITEM_CODE, B.DESCR, B.UNIT, B.LOC_TYPE "+
				 	  		" FROM SITEITEM A, ITEM B "+ 
	  						" WHERE A.SITE_CODE = '"+siteCode+"'"+
							" AND A.ITEM_CODE = B.ITEM_CODE "+
							" ORDER BY A.ITEM_SER,B.DESCR ";
					System.out.println("SorderForm:IF:SITEITEM,ITEM :sql:"+sql);
					rs = stmt.executeQuery(sql);
					while(rs.next())
					{
						itemCode =rs.getString("ITEM_CODE");
						itemDescr =rs.getString("DESCR");
						itemUnit =rs.getString("UNIT");
						locType =rs.getString("LOC_TYPE");
						System.out.println(" *IF:itemCode:"+itemCode+":itemDescr:"+itemDescr+":itemUnit:"+itemUnit+":locType:"+locType);
						itemSeries = itmDBAccess.getItemSeries(itemCode,siteCode,orderDate,custCode,'C',conn1);
						System.out.println("\n IF:itemSeries:"+itemSeries+":");
						if(itemSer != null && itemSer.trim().length() > 0 && itemSeries != null && itemSeries.trim().length() > 0)
						{
							System.out.println("\n IF:itemSeries:"+itemSeries+": itemSer :"+itemSer);
							
							if(!(itemSer.trim()).equalsIgnoreCase(itemSeries.trim()))
							{
								sql="SELECT OTH_SERIES FROM ITEMSER WHERE ITEM_SER ='"+itemSer+"'";
								System.out.println("SorderForm:IF ITEMSER:sql:"+sql);
								rs1 =stmt1.executeQuery(sql);
								if(rs1.next())
								{
									othSer = rs1.getString("OTH_SERIES");
								}
								System.out.println("IF:othSer:"+othSer+":");
								rs1 = null;
								if (othSer == null  || othSer.equalsIgnoreCase("N"))
								{
									continue ;
								}
							}//end if
						}//end if
						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<descr>").append("<![CDATA[").append(itemDescr).append("]]>").append("</descr>\r\n");
						valueXmlString.append("<unit>").append("<![CDATA[").append(itemUnit).append("]]>").append("</unit>\r\n");
						valueXmlString.append("<loc_type>").append("<![CDATA[").append(locType).append("]]>").append("</loc_type>\r\n");
						valueXmlString.append("<item_ser>").append("<![CDATA[").append((itemSeries == null) ? "":itemSeries).append("]]>").append("</item_ser>\r\n");
						valueXmlString.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
						valueXmlString.append("</Detail>\r\n");
					}//end while
					valueXmlString.append("</Root>\r\n");
				}//end cnt
				else
				{
					System.out.println("Else :when cnt != 0");
					sql="SELECT A.ITEM_CODE, B.DESCR, B.UNIT, B.LOC_TYPE "+
				 	  " FROM CUSTOMERITEM A, ITEM B "+  
				  	  "	WHERE A.CUST_CODE ='"+custCode+"'"	+
					  " AND A.ITEM_CODE = B.ITEM_CODE "+
					  " AND B.ACTIVE = 'Y'"+
					  " AND (A.RESTRICT_UPTO IS NULL OR A.RESTRICT_UPTO < '"+orderDate+"')"+
					  " ORDER BY B.DESCR";
					 System.out.println("SorderForm:Else:CUSTOMERITEM,ITEM :sql:"+sql);
					 rs = stmt.executeQuery(sql);
					 while(rs.next())
					 {
						itemCode =rs.getString("ITEM_CODE");
						itemDescr =rs.getString("DESCR");
						itemUnit =rs.getString("UNIT");
						locType =rs.getString("LOC_TYPE");
						System.out.println("Else:itemCode:"+itemCode+":itemDescr:"+itemDescr+":itemUnit:"+itemUnit+":locType:"+locType);
						itemSeries = itmDBAccess.getItemSeries(itemCode,siteCode,orderDate,custCode,'C',conn);
						System.out.println("\n Else:itemSeries:"+itemSeries+":");
						if(itemSer != null && itemSer.trim().length() > 0 && itemSeries != null && itemSeries.trim().length() > 0)
						{
							if(!(itemSer.trim()).equalsIgnoreCase(itemSeries.trim()))
							{
								sql="SELECT OTH_SERIES FROM ITEMSER WHERE ITEM_SER ='"+itemSer+"'";
								System.out.println("SorderForm:ITEMSER :sql:"+sql);
								rs1 = stmt1.executeQuery(sql);
								if(rs1.next())
								{
								  othSer = rs1.getString("OTH_SERIES");
								}
								System.out.println("Else:othSer:"+othSer+":");
								rs1 = null;
								if(othSer == null || othSer.equalsIgnoreCase("N"))
								{
									continue;
								}//end if								
							}//end if
						}//end if
						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<descr>").append("<![CDATA[").append(itemDescr).append("]]>").append("</descr>\r\n");
						valueXmlString.append("<unit>").append("<![CDATA[").append(itemUnit).append("]]>").append("</unit>\r\n");
						valueXmlString.append("<loc_type>").append("<![CDATA[").append(locType).append("]]>").append("</loc_type>\r\n");
						valueXmlString.append("<item_ser>").append("<![CDATA[").append((itemSeries == null)? "":itemSeries).append("]]>").append("</item_ser>\r\n");
						valueXmlString.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
						valueXmlString.append("</Detail>\r\n");
					}//end loop
					valueXmlString.append("</Root>\r\n");
				}//end else Cnt
			}//end if detCnt
		}
		catch(Exception e)
		{
			System.out.println("Exception :SorderForm :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}
}