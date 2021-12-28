package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.text.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;

import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3


//public class PriceListEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class PriceList extends ValidatorEJB implements PriceListLocal, PriceListRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	/*public void ejbCreate() throws RemoteException, CreateException
	{
		System.out.println("sEntering into SQLChangeEJB.............");
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
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}


	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString); //returns the DOM Object for the passed XML Stirng
			//System.out.println("xmlString :" + xmlString);
			BaseLogger.log("9", null, null, "xmlString :" + xmlString);
			dom1 = parseString(xmlString1); //returns the DOM Object for the passed XML Stirng
			//System.out.println("xmlString1 :" + xmlString1);
			BaseLogger.log("9", null, null, "xmlString1 :" + xmlString1);

			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : [PriceListEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			BaseLogger.log("0", null, null, "Exception : [PriceListEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
        return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		//Statement stmt = null,stmt1 = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		StringBuffer valueXmlString = new StringBuffer();
		StringBuffer retString = new StringBuffer();
		String errCode = "";
		String sql = "",sql1 ="";
		String columnValue = "";
		String childNodeName = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		int n = 0;
		int currentFormNo = 0;
		String priceList = "";
		String listType="";
		String itemCode	= "";
		String unit = "";
		String slabno = "";
		String col_name = "";
		//ConnDriver connDriver = new ConnDriver();
		String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
		//System.out.println("userId ::-"+userId);
		BaseLogger.log("3", null, null, "userId ::-"+userId);
		//String mmaxqtyStr = "" , mminqtyStr = "" , mrate ="" , minrate="" , maxrate="" ;
		try
		{
			conn = getConnection();
			//conn = connDriver.getConnectDB("DriverITM");
			//stmt = conn.createStatement();
			//stmt1 = conn.createStatement();

			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			//System.out.println("[PriceListEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
			BaseLogger.log("3", null, null, "[PriceListEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			valueXmlString.append("<Detail>");
			switch (currentFormNo)
			{
				case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				int childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue=childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				try
				{

						if (currentColumn.trim().equals("itm_defaultedit"))
						{



							priceList = genericUtility.getColumnValue("price_list",dom) ;
							itemCode = genericUtility.getColumnValue("item_code",dom) ;
							unit = genericUtility.getColumnValue("unit",dom) ;
							listType = genericUtility.getColumnValue("list_type",dom) ;
							slabno = genericUtility.getColumnValue("slab_no",dom) ;
							// Added By Sagar On 1-FEB-2017 Start
							/*mrate = checkNull(genericUtility.getColumnValue("rate",dom)) ;
							minrate = checkNull(genericUtility.getColumnValue("min_rate",dom)) ;
							maxrate = checkNull(genericUtility.getColumnValue("max_rate",dom)) ;
							mminqtyStr = checkNull(genericUtility.getColumnValue("min_qty",dom)) ;
							mmaxqtyStr = checkNull(genericUtility.getColumnValue("max_qty",dom)) ;*/
							//Added By Sagar On 1-FEB-2017 End
							valueXmlString.append("<price_list protect=\"1\">").append("<![CDATA["+priceList+"]]>").append("</price_list>");
							valueXmlString.append("<item_code protect=\"1\">").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
							valueXmlString.append("<unit protect=\"1\">").append("<![CDATA["+unit+"]]>").append("</unit>");
							valueXmlString.append("<list_type protect=\"1\">").append("<![CDATA["+listType+"]]>").append("</list_type>");
							valueXmlString.append("<slab_no protect=\"1\">").append("<![CDATA["+slabno+"]]>").append("</slab_no>");
							//Added By Sagar On 1-FEB-2017 Start
							/*valueXmlString.append("<min_qty>").append("<![CDATA["+mminqtyStr+"]]>").append("</min_qty>");
							valueXmlString.append("<max_qty>").append("<![CDATA["+mmaxqtyStr+"]]>").append("</max_qty>");
							valueXmlString.append("<rate>").append("<![CDATA["+mrate+"]]>").append("</rate>");
							valueXmlString.append("<min_rate>").append("<![CDATA["+minrate+"]]>").append("</min_rate>");
							valueXmlString.append("<max_rate>").append("<![CDATA["+maxrate+"]]>").append("</max_rate>");*/
							//Added By Sagar On 1-FEB-2017 End
							/* sql = "SELECT DISTINCT list_type FROM PRICELIST WHERE  price_list = '"+ priceList +"'";

							rs = stmt.executeQuery(sql);
							System.out.println("SQL....."+sql);
							if (rs.next())
							{
								listType = rs.getString("list_type");
							}
							rs.close();
							rs = null;
							valueXmlString.append("<list_type protect='1\'>").append(listType).append("</list_type>");
							*/
						}// Added By Sagar On 1-FEB-2017 Start
						/*else if (currentColumn.trim().equalsIgnoreCase("itm_default") )
						{
							
							valueXmlString.append("<min_qty>").append("0").append("</min_qty>");
							valueXmlString.append("<max_qty>").append("0").append("</max_qty>");
							valueXmlString.append("<rate>").append("0").append("</rate>");
							valueXmlString.append("<min_rate>").append("0").append("</min_rate>");
							valueXmlString.append("<max_rate>").append("0").append("</max_rate>");
						}// Added By Sagar On 1-FEB-2017 End
*/					// Commented By Sagar On 27-JAN-2017 Start
						/*	else
						if (currentColumn.trim().equals("itm_default") )
						{
							long mslab_no=0;
							priceList = genericUtility.getColumnValue("price_list",dom) ;
				            String mprice_list,mitem_code,munit,mlist_type;
							// 21/06813 manoharan  check for value
							valueXmlString.append("<item_code protect=\"0\">").append("<![CDATA[").append(" ").append("]]>").append("</item_code>");
							valueXmlString.append("<unit protect=\"0\">").append("<![CDATA[ ]]>").append("</unit>");
							if (priceList != null && priceList.trim().length() > 0 )
							{
								valueXmlString.append("<price_list protect=\"1\">").append("<![CDATA["+priceList+"]]>").append("</price_list>");
								// 31/01/2008 manoharan distinct not required
								//sql = "SELECT DISTINCT list_type FROM PRICELIST WHERE  price_list = '"+ priceList +"'";
								sql = "SELECT list_type FROM PRICELIST WHERE  price_list = '"+ priceList +"'";

								rs = stmt.executeQuery(sql);
								System.out.println("SQL....."+sql);
								if (rs.next())
								{
									listType = rs.getString("list_type");
									valueXmlString.append("<list_type protect=\"1\">").append("<![CDATA["+listType+"]]>").append("</list_type>");
								}
								else
								{
									// 31/01/2008 manoharan
									rs.close();
									rs = null;
									sql = "SELECT list_type FROM PRICELIST_MST WHERE  price_list = '"+ priceList +"'";

									rs = stmt.executeQuery(sql);
									System.out.println("SQL....."+sql);
									if (rs.next())
									{
										listType = rs.getString("list_type");
										valueXmlString.append("<list_type protect=\"1\">").append("<![CDATA["+listType+"]]>").append("</list_type>");
									}
									else
									{
										valueXmlString.append("<list_type protect=\"0\">").append("<![CDATA[ ]]>").append("</list_type>");
									}
									// end 31/01/2008 manoharan
								}
								rs.close();
								rs = null;

								sql="SELECT MAX(SLAB_NO) FROM PRICELIST WHERE PRICE_LIST = '" + priceList + "'";

								rs=stmt.executeQuery(sql);
								System.out.println("SQL query....."+sql);
								if(rs.next())
								{
									mslab_no=rs.getLong(1);
									valueXmlString.append("<slab_no protect=\"1\">").append(mslab_no + 1).append("</slab_no>");
								}
								else
								{
									valueXmlString.append("<slab_no protect=\"1\">").append("1").append("</slab_no>");
								}
								rs.close();
								rs = null;
							}




							Timestamp mfr_date=null,mto_date=null;
							sql="SELECT FR_DATE,TO_DATE FROM PARAMETER";

							rs=stmt.executeQuery(sql);
							if(rs.next())
							{
								mfr_date=rs.getTimestamp("fr_date")	;
								mto_date=rs.getTimestamp("to_date") ;
							}
							rs.close();
							rs = null;
							//commented by rajendra on 11/10/07 for avoid null
//							mprice_list	= genericUtility.getColumnValue("price_list",dom);
//							mitem_code  = genericUtility.getColumnValue("item_code",dom);
//							munit       = genericUtility.getColumnValue("unit",dom);
//							mlist_type  = genericUtility.getColumnValue("list_type",dom);
//
//							valueXmlString.append("<fr_date>").append(mfr_date).append("</fr_date>");
//							valueXmlString.append("<to_date>").append(mto_date).append("</to_date>");
//							valueXmlString.append("<price_list>").append(mprice_list).append("</price_list>");
//							valueXmlString.append("<item_code>").append(mitem_code).append("</item_code>");
//							valueXmlString.append("<unit>").append(munit).append("</unit>");
//						    valueXmlString.append("<list_type>").append(mlist_type).append("</list_type>");

						} */ //Commented By Sagar On 27-JAN-2017 End
					else
					if(currentColumn.trim().equals("price_list"))
					{
						String mlist_type="";
						String ls_price_list=genericUtility.getColumnValue("price_list",dom);
						//20feb19[stmt changed to pstmt and closed rs and pstmt]
						//sql="select distinct list_type  from pricelist_mst where  price_list = '"+ls_price_list+"'";
						sql="select distinct list_type  from pricelist_mst where  price_list = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_price_list);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							mlist_type=rs.getString("list_type");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<list_type protect=\"1\">").append("<![CDATA["+mlist_type+"]]>").append("</list_type>");
					}
					else
					if(currentColumn.trim().equals("item_code"))
					{
						String mcode="",mdescr="",munit="",msite_code="";
						long mslabno = 0;
						mcode=genericUtility.getColumnValue("item_code",dom);
						System.out.println("Item code>>>>>>>" + mcode);
						priceList = genericUtility.getColumnValue("price_list",dom) ;
						System.out.println("pRICELIST>>>>>>>>>" + priceList);
						System.out.println("itemcode>>>>>" + mcode);
						//20feb19[stmt changed to pstmt and closed rs and pstmt]
						//sql="SELECT DESCR,UNIT ,SITE_CODE FROM ITEM WHERE ITEM_CODE='"+mcode+"'";
						sql="SELECT DESCR,UNIT ,SITE_CODE FROM ITEM WHERE ITEM_CODE= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							mdescr=rs.getString("descr");
							munit=rs.getString("unit");
							msite_code=rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<item_descr>").append("<![CDATA["+mdescr+"]]>").append("</item_descr>");
						/*valueXmlString.append("<unit>").append("<![CDATA["+munit+"]]>").append("</unit>");*/       //commented by manish mhatre on 25-july-2019
						valueXmlString.append("<site_code>").append("<![CDATA["+msite_code+"]]>").append("</site_code>");
						valueXmlString.append("<unit protect=\"1\">").append("<![CDATA["+munit+"]]>").append("</unit>");  //added by manish mhatre  on 25-july-2019
						
						//sql1="SELECT MAX(SLAB_NO) FROM PRICELIST WHERE PRICE_LIST = '" + priceList + "'" +"AND ITEM_CODE = '"+ mcode +"'";
						sql1="SELECT MAX(SLAB_NO) FROM PRICELIST WHERE PRICE_LIST = ? AND ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql1);
						pstmt.setString(1, priceList);
						pstmt.setString(2, mcode);
						rs1=pstmt.executeQuery();
						//System.out.println("SQL query....."+sql1);
						if(rs1.next())
						{
							mslabno=rs1.getLong(1);
							valueXmlString.append("<slab_no protect=\"1\">").append(mslabno + 1).append("</slab_no>");
						}
						else
						{
							valueXmlString.append("<slab_no protect=\"1\">").append("1").append("</slab_no>");
						}
						rs1.close();
						rs1 = null;
						pstmt.close();
						pstmt = null;
					}
					else
					if(currentColumn.trim().equals("unit"))
					{
						String mdescr="";
						String munit=genericUtility.getColumnValue("unit",dom)	;
						//sql="SELECT DESCR FROM UOM WHERE UNIT='"+munit+"'";
						sql="SELECT DESCR FROM UOM WHERE UNIT= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, munit);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
					    	mdescr=rs.getString("descr");
					    }
					    rs.close();
					    rs = null;
					    pstmt.close();
						pstmt = null;
					    valueXmlString.append("<descr>").append("<![CDATA["+mdescr+"]]>").append("</descr>");
					}
					else
					if(currentColumn.trim().equals("list_type")	)
					{
						String 	mlist_type="",mrate_type="",mlot_no__from="",mlot_no__to="" ;
						mlist_type=	genericUtility.getColumnValue("list_type",dom);

						if( (mlist_type.equals("M") || mlist_type.equals("N")) )
						{
							mrate_type	  = genericUtility.getColumnValue("rate_type",dom);
							mlot_no__from = genericUtility.getColumnValue("lot_no__from",dom);
							mlot_no__to   = genericUtility.getColumnValue("mlot_no__to",dom);
							valueXmlString.append("<rate_type protect = \"1\">").append("<![CDATA["+mrate_type+"]]>").append("</rate_type>");
							valueXmlString.append("<lot_no__from protect = \"1\">").append("<![CDATA["+mlot_no__from+"]]>").append("</lot_no__from>");
							valueXmlString.append("<lot_no__to protect= \"1\">").append("<![CDATA["+mlot_no__to+"]]>").append("</lot_no__to>");
						}
						else
						{
							valueXmlString.append("<rate_type protect = \"1\">").append("<![CDATA["+mrate_type+"]]>").append("</rate_type>");
							valueXmlString.append("<lot_no__from protect = \"0\">").append("<![CDATA["+mlot_no__from+"]]>").append("</lot_no__from>");
							valueXmlString.append("<lot_no__to protect = \"0\">").append("<![CDATA["+mlot_no__to+"]]>").append("</lot_no__to>");

					    }

					}

				}//try
				catch(Exception e)
				{
				//System.out.println("Exception :[PriceListEJB][itemChanged::case 1::order_type] :==>\n"+e.getMessage());
				BaseLogger.log("0", null, null, "Exception :[PriceListEJB][itemChanged::case 1::order_type] :==>\n"+e.getMessage());	
				throw new ITMException(e);
				}
				valueXmlString.append("</Detail>");
				break;
			}//Switch
			valueXmlString.append("</Root>");
		}//try


		catch(Exception e)
		{
			//System.out.println("Exception ::"+e.getMessage());
			BaseLogger.log("0", null, null, "Exception ::"+e.getMessage());
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception s){}
		}
		return valueXmlString.toString();
	}//itemChanged()
	
	// Added By Sagar On 25-JAN-2017 Start
		private String checkNull(String input)	
		{
			return input == null ? "" : input.trim();
		}
	// Added By Sagar On 25-JAN-2017 End
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		//System.out.println("Entering into  validations ...................");
		BaseLogger.log("3", null, null, "Entering into  validations ...................");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			//System.out.println("Exception : PayableOpeningsEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			BaseLogger.log("0", null, null, "Exception : PriceList : wfValData(String xmlString) : ==>"+e.getMessage());
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String errString = "";
		String errCode = "";
		String sql = "";
		String userId = "";
		String custCode = "";
		String empCodeGiven = "";
		String empCodeMerge = "";
		String childNodeName = "";
		String priceList = "";
		String lskeyflag = "";
		 int  cnt1 = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr,currentFormNo=0;
		int childNodeListLength;
		String itemCode = "", unit = "", listType = "", effFromStr = "", validUptoStr = "", lotNoFrom = "", lotNoTo = "", slabno = "",minQty = "",maxQty = "";
		Timestamp effFrom =null, validUpto = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		// Added By Sagar on [25-JAN-2017] [Start]
		double mmaxqty=0 , mminqty = 0,cnt=0;
		double mrate = 0.0 , minrate = 0.0 , maxrate = 0.0; 
		String mmaxqtyStr = "" , mminqtyStr = "" , strvalid_upto="" , strmeff_from = "" , valid_uptoDate = "" , minrateStr="" , maxrateStr="",
				eff_fromDate = "" , mitem_code ="" , mactive ="" , mrateStr= "" , mValue ="", ls_plist_parent = "" , Is_price_list = "" ;
		//ConnDriver connDriver = new ConnDriver();
		// Added By Sagar on [25-JAN-2017] [End]
		try
		{
			conn = getConnection();
			//conn = connDriver.getConnectDB("DriverITM");
			//stmt = conn.createStatement();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			//System.out.println("userId = "+userId);
			//System.out.println("xtraParams = "+xtraParams);
			BaseLogger.log("3", null, null, "userId = "+userId + "xtraParams = "+xtraParams);
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}

			//parentNodeList = dom.getElementsByTagName("Detail" + currentFormNo);
			//parentNode = parentNodeList.item(0);
			//childNodeList = parentNode.getChildNodes();
			//childNodeListLength = childNodeList.getLength();
			//for (ctr = 0; ctr < childNodeListLength; ctr++)
			//{
				//childNode = childNodeList.item(ctr);
				//childNodeName = childNode.getNodeName();
				 //System.out.println("Validation for [" + childNodeName + "]");
				switch (currentFormNo)
				{
					case 1:
					{
						parentNodeList = dom.getElementsByTagName("Detail1");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();

						for(ctr = 0; ctr < childNodeListLength; ctr++)
						{
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							if (childNodeName.equals("slab_no"))
							{
							}
							if(childNodeName.equals("tax_base"))
							{
							}
							// ADDED BY RITESH ON 23/SEP/13 START
							if(childNodeName.equals("price_list"))
							{
							   NamedNodeMap attrMap = parentNode.getAttributes();
							   String objName = attrMap.getNamedItem( "objName" ).getNodeValue();	
							   String winName = "w_"+ objName;	
							   priceList   = genericUtility.getColumnValue("price_list",dom);
								//System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] priceList [ " + priceList + "]");
							   BaseLogger.log("3", null, null, "20/10/14 manohar validation for  [" + childNodeName + "] priceList [ " + priceList + "]");
							   // 14/11/13 manoharan duplicate checking to consider all parameter
							   itemCode = genericUtility.getColumnValue("item_code",dom);
							   unit = genericUtility.getColumnValue("unit",dom);
							   listType = genericUtility.getColumnValue("list_type",dom);
							   effFromStr = genericUtility.getColumnValue("eff_from",dom);
							   validUptoStr = genericUtility.getColumnValue("valid_upto",dom);
							   lotNoFrom = genericUtility.getColumnValue("lot_no__from",dom);
							   lotNoTo = genericUtility.getColumnValue("lot_no__to",dom);
							   // Modified By Sagar to check null for dom value on [25-JAN-2017] [Start]
							   slabno = checkNull(genericUtility.getColumnValue("slab_no",dom));
							   minQty = checkNull(genericUtility.getColumnValue("min_qty",dom));
							   maxQty = checkNull(genericUtility.getColumnValue("max_qty", dom));
							   // Modified By Sagar to to check null for dom value on [25-JAN-2017] [End]
							   // end 14/11/13 manoharan duplicate checking to consider all parameter
							   //System.out.println("price_list wfvalData :::: " + priceList);
							   BaseLogger.log("3", null, null, "price_list wfvalData :::: " + priceList);
							   // 14/11/13 manoharan commented as always it will be manual
							   //sql = "select key_flag  from transetup where tran_window = '"+ winName +"'";
							   //pstmt = conn.prepareStatement(sql);
							   //rs = pstmt.executeQuery();
							   //if(rs.next())
							   //{
								//  lskeyflag = rs.getString("key_flag")== null ?"M":rs.getString("key_flag");
								//   
							   //}
							   //rs.close();rs = null;
							   //pstmt.close();pstmt = null;
							  // if("M".equals(lskeyflag) && (priceList == null || priceList.trim().length() == 0))
							// Modified By Sagar to check null for dom value on [25-JAN-2017] [Start]
							   //if(priceList == null || priceList.trim().length() == 0 || itemCode == null || itemCode.trim().length() == 0 || unit == null || unit.trim().length() == 0 || listType == null || listType.trim().length() == 0 || effFromStr == null || effFromStr.trim().length() == 0 || validUptoStr == null || validUptoStr.trim().length() == 0 || lotNoFrom == null || lotNoFrom.trim().length() == 0 || lotNoTo == null || lotNoTo.trim().length() == 0)
							   if(priceList == null || priceList.trim().length() == 0 )
							   {
								   	errCode = "VTLPRCNUL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(itemCode == null || itemCode.trim().length() == 0)
							   {
								   errCode = "VTITMNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(unit == null || unit.trim().length() == 0 )
							   {
								   errCode = "VTINVUNIT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(listType == null || listType.trim().length() == 0 )
							   {
								   errCode = "VTLSTNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(effFromStr == null || effFromStr.trim().length() == 0 )
							   {
								   	errCode = "VTEFFNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(validUptoStr == null || validUptoStr.trim().length() == 0 )
							   {
								   errCode = "VTVLDNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(lotNoFrom == null || lotNoFrom.trim().length() == 0 )
							   {
								   errCode = "VTLTFRNUL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else if(lotNoTo == null || lotNoTo.trim().length() == 0 )
							   {
								   errCode = "VTLTTONUL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							   else // if ("A".equals(editFlag))
							   {
								   //System.out.println("before effFromStr  [" + effFromStr + "] validUptoStr [" + validUptoStr + "]");
								   BaseLogger.log("3", null, null, "before effFromStr  [" + effFromStr + "] validUptoStr [" + validUptoStr + "]");
								   /*effFromStr = genericUtility.getValidDateString(effFromStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								   validUptoStr = genericUtility.getValidDateString(validUptoStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								   System.out.println("after effFromStr  [" + effFromStr + "] validUptoStr [" + validUptoStr + "]");
								   effFrom = Timestamp.valueOf(effFromStr + " 00:00:00");
								   validUpto =  Timestamp.valueOf(validUptoStr + " 00:00:00");*/
								   
								   if(effFromStr.trim().length() > 0 )
								   {
									   effFromStr = genericUtility.getValidDateString(effFromStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
									   effFrom = Timestamp.valueOf(effFromStr + " 00:00:00");
								   }
								   if(validUptoStr.trim().length() > 0 )
									{
									   validUptoStr = genericUtility.getValidDateString(validUptoStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
									   validUpto =  Timestamp.valueOf(validUptoStr + " 00:00:00");	
									}
								// Modified By Sagar to check null for dom value on [25-JAN-2017] [End]
								   //System.out.println("after effFromStr  [" + effFromStr + "] validUptoStr [" + validUptoStr + "]");
								   //System.out.println("Quantity From [" + minQty + "] Quantity To [" + maxQty + "]");
								   BaseLogger.log("3", null, null, "after effFromStr  [" + effFromStr + "] validUptoStr [" + validUptoStr + "Quantity From [" + minQty + "] Quantity To [" + maxQty + "]");
								   sql = "select count(*)  from pricelist where price_list = ? "
									+ " and item_code = ?  and unit = ? and list_type = ? "
									+ " and eff_from = ? and valid_upto = ? "
									+ " and lot_no__from = ? and lot_no__to = ? "
									+ " and min_qty = ? and max_qty = ?";
									if ("E".equals(editFlag))
									{
										sql =  sql + " and slab_no <> " + slabno ;
									}
									
								  pstmt = conn.prepareStatement(sql);
								  pstmt.setString(1,priceList);
								  pstmt.setString(2,itemCode);
								  pstmt.setString(3,unit);
								  pstmt.setString(4,listType);
								  pstmt.setTimestamp(5,effFrom);
								  pstmt.setTimestamp(6,validUpto);
								  pstmt.setString(7,lotNoFrom);
								  pstmt.setString(8,lotNoTo);
								  pstmt.setString(9,minQty);
								  pstmt.setString(10,maxQty);
								  
								  rs = pstmt.executeQuery();
								  if(rs.next())
								  {
									cnt1 = rs.getInt(1);
								  }
								  if(rs != null){
									  rs.close();
									  rs = null;
								  }
								  if(pstmt != null){
								  pstmt.close();
								  pstmt = null;
								  }
								  
								  if(cnt1 > 0)
								  {
									errCode = "VMDUPL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								  }
							   }
							   
							} 	// ADDED BY RITESH ON 23/SEP/13 END
							// Modified By Sagar to check null for dom value On [25-JAN-2017] [Start]
							/*else if(childNodeName.equals("item_code"))
							{
								System.out.println("childNodeName 1. : " + childNodeName);
								String 	mitem_code = genericUtility.getColumnValue("item_code",dom);
								System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] mitem_code [ " + mitem_code + "]");
								sql="select count(*) as count from item where item_code = '"+mitem_code+"'";
								rs=stmt.executeQuery(sql);
								System.out.println("SQL"+sql);
								int cnt=0;
								if(rs.next())
								{
									cnt=rs.getInt("count");
								}
								rs.close();
								rs = null;
								if(cnt==0)
								{
									errCode = "VMITEM_CD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								sql="select active from item where item_code='"+mitem_code+"'";
								String mactive=null;
								rs=stmt.executeQuery(sql);
								if(rs.next())
								{
									mactive=rs.getString("active");
								}
								rs.close();
								rs = null;
							   if(mactive.equals("N"))
							   {
									errCode="VTITEM4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }

							}*/
							if("item_code".equalsIgnoreCase(childNodeName))
							{
								//System.out.println("childNodeName 1. : " + childNodeName);
								BaseLogger.log("3", null, null, "childNodeName 1. : " + childNodeName);
								mitem_code = checkNull(genericUtility.getColumnValue("item_code",dom));
								if(mitem_code == null  ||  mitem_code.length() == 0)
								{
									errCode = "VTITMNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] mitem_code [ " + mitem_code + "]");
								//20feb19[stmt changed to pstmt and closed rs and pstmt]
								BaseLogger.log("3", null, null, "25-JAN-2017 validation for  [" + childNodeName + "] mitem_code [ " + mitem_code + "]");
								//sql="select count(*) as count from item where item_code = '"+mitem_code+"'";
								sql="select count(*) as count from item where item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mitem_code);
								rs=pstmt.executeQuery();
								//System.out.println("SQL"+sql);
								//int cnt=0;
								if(rs.next())
								{
									cnt=rs.getInt("count");
								}
								if(rs != null){
								rs.close();
								rs = null;
								}
								pstmt.close(); pstmt =null;
								if(cnt==0)
								{
									errCode = "VMITEM_CD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									//20feb19[stmt changed to pstmt and closed rs and pstmt]
								//sql="select active from item where item_code='"+mitem_code+"'";
									sql="select active from item where item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mitem_code);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									mactive=checkNull(rs.getString("active"));
								}
								
								if(rs != null){
									rs.close();
									rs = null;
									}
								pstmt.close(); pstmt =null;
							   if("N".equalsIgnoreCase(mactive))
							   {
									errCode="VTITEM4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							   }
							}
							}
							// Modified By Sagar to check null for dom value On [25-JAN-2017] [End]
							// Modified By Sagar to check null for dom value On [25-JAN-2017] [Start]
							/*else if (childNodeName.equals("price_list__parent"))
							{
								String ls_plist_parent,Is_price_list;
								System.out.println("childNodeName 1. : " + childNodeName);
								ls_plist_parent = genericUtility.getColumnValue("price_list__parent",dom);
								Is_price_list   = genericUtility.getColumnValue("price_list",dom);
								System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] ls_plist_parent [ " + ls_plist_parent + "] Is_price_list [" + Is_price_list + "]");
								//System.out.println("childNode.getFirstChild() 1. : " + childNode.getFirstChild());
								//if (ls_plist_parent == null || ls_plist_parent.trim().length() == 0) // 
								if (ls_plist_parent != null && ls_plist_parent.trim().length() > 0)// manoharan commented wrong condition/
								{
									if(ls_plist_parent.trim().equals(Is_price_list.trim()) )

									errCode = "VTPARENTPL";
									errString = getErrorString("price_list__parent",errCode,userId);
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}*/
							if ("price_list__parent".equalsIgnoreCase(childNodeName))
							{
								//System.out.println("childNodeName 1. : " + childNodeName);
								BaseLogger.log("3", null, null, "childNodeName 1. : " + childNodeName);
								ls_plist_parent = genericUtility.getColumnValue("price_list__parent",dom);
								Is_price_list   = genericUtility.getColumnValue("price_list",dom);
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] ls_plist_parent [ " + ls_plist_parent + "] Is_price_list [" + Is_price_list + "]");
								BaseLogger.log("3", null, null, "25-JAN-2017 validation for  [" + childNodeName + "] ls_plist_parent [ " + ls_plist_parent + "] Is_price_list [" + Is_price_list + "]");
								if (ls_plist_parent != null && ls_plist_parent.trim().length() > 0)// manoharan commented wrong condition/
								{
									if(ls_plist_parent.equalsIgnoreCase(Is_price_list))
									{
									errCode = "VTPARENTPL";
									//errString = getErrorString("price_list__parent",errCode,userId);
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									//System.out.println("Inside Parent list :::::::");
									BaseLogger.log("3", null, null,"Inside Parent list :::::::");
									}
								}
							}
							// Modified By Sagar to check null for dom value On [25-JAN-2017] [End]
							// Modified By Sagar to check null for dom value On [25-JAN-2017] [Start]
							/*else if (childNodeName.equals("min_qty"))
							{
								int mmaxqty,mminqty;
								mminqty =Integer.parseInt(genericUtility.getColumnValue("min_qty",dom));
								mmaxqty=Integer.parseInt(genericUtility.getColumnValue("max_qty",dom));
								System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] mmaxqty [ " + mmaxqty + "] mminqty [" + mminqty + "]");


								if (mmaxqty < mminqty )
								{
									errCode = "VMMINQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}*/ 
							if ("min_qty".equalsIgnoreCase(childNodeName))
							{
								mminqtyStr = checkNull(genericUtility.getColumnValue("min_qty",dom));
								mmaxqtyStr = checkNull(genericUtility.getColumnValue("max_qty",dom));
								if(mminqtyStr == null || mminqtyStr.length() == 0)
								{
									errCode = "VTMMINQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(mminqtyStr.length() > 0)
								{
									mminqty = Double.parseDouble(mminqtyStr);
									//System.out.println("pasring mi mminqty ::: "+mminqty);
									BaseLogger.log("3", null, null,"pasring mi mminqty ::: "+mminqty);
									if( mminqty <= 0 )
									{
										errCode = "VTMINLESS";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								if(mmaxqtyStr.length() > 0)
								{
									mmaxqty=Double.parseDouble(mmaxqtyStr);
									//System.out.println("pasring mi qty ::: "+mmaxqty);
									BaseLogger.log("3", null, null,"pasring mi qty ::: "+mmaxqty);
									if( mmaxqty <= 0 )
									{
										errCode = "VTMAXLESS";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] mmaxqty [ " + mmaxqty + "] mminqty [" + mminqty + "]");
								BaseLogger.log("3", null, null,"25-JAN-2017 validation for  [" + childNodeName + "] mmaxqty [ " + mmaxqty + "] mminqty [" + mminqty + "]");
								/*if (mmaxqty < mminqty )
								{
									errCode = "VMMINQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}*/
							}
							if("max_qty".equalsIgnoreCase(childNodeName))
							{
								mmaxqtyStr = checkNull(genericUtility.getColumnValue("max_qty",dom));
								mminqtyStr = checkNull(genericUtility.getColumnValue("min_qty",dom));
								if(mmaxqtyStr == null || mmaxqtyStr.length() == 0)
								{
									errCode = "VTMMAXQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(mmaxqtyStr.length() > 0)
								{
									mmaxqty=Integer.parseInt(mmaxqtyStr);
									//System.out.println("pasring mi qty ::: "+mmaxqty);
									BaseLogger.log("3", null, null,"pasring mi qty ::: "+mmaxqty);
									if( mmaxqty <= 0 )
									{
										errCode = "VTMAXLESS";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								if(mminqtyStr.length() > 0)
								{
									mminqty =Integer.parseInt(mminqtyStr);
									//System.out.println("pasring mi mminqty ::: "+mminqty);
									BaseLogger.log("3", null, null,"pasring mi mminqty ::: "+mminqty);
									if( mminqty <= 0 )
									{
										errCode = "VTMINLESS";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] mmaxqty [ " + mmaxqty + "] mminqty [" + mminqty + "]");
								BaseLogger.log("3", null, null,"25-JAN-2017 validation for  [" + childNodeName + "] mmaxqty [ " + mmaxqty + "] mminqty [" + mminqty + "]");
								if (mmaxqty < mminqty )
								{
									errCode = "VMMINQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//}
							// Modified By Sagar to check null for dom value On [25-JAN-2017] [End]
							//Modified By Sagar to check null for dom value On [25-JAN-2017] [Start]
							/*else if(childNodeName.equals("valid_upto"))
							{
								Timestamp mvalid_upto,meff_from;
								String strvalid_upto,strmeff_from;
								strvalid_upto=genericUtility.getColumnValue("valid_upto",dom) ;
								strmeff_from=genericUtility.getColumnValue("eff_from",dom);
								String valid_uptoDate=getValidDateTimeString(strvalid_upto,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								String eff_fromDate= getValidDateTimeString(strmeff_from,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] valid_uptoDate [ " + valid_uptoDate + "] eff_fromDate [" + eff_fromDate + "]");

								 mvalid_upto = Timestamp.valueOf(valid_uptoDate+" 00:00:00");
								 meff_from = Timestamp.valueOf(eff_fromDate+" 00:00:00");

								if( ( mvalid_upto.compareTo( meff_from) ) ==0 )
								{
									errCode="VMVAL_UPTO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							 */ 
							if("valid_upto".equalsIgnoreCase(childNodeName))
							{
								strvalid_upto = checkNull(genericUtility.getColumnValue("valid_upto",dom)) ;
								strmeff_from = checkNull(genericUtility.getColumnValue("eff_from",dom));
								
								if(strvalid_upto.length() > 0 )
								{
									valid_uptoDate = getValidDateTimeString(strvalid_upto,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
									//mvalid_upto = Timestamp.valueOf(valid_uptoDate+" 00:00:00");	
								}else{
									
									errCode="VTVLDNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(strmeff_from.length() > 0 )
								{
									eff_fromDate= getValidDateTimeString(strmeff_from,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
									//meff_from = Timestamp.valueOf(eff_fromDate+" 00:00:00");
								}
								else
								{
									errCode="VTEFFNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] valid_uptoDate [ " + valid_uptoDate + "] eff_fromDate [" + eff_fromDate + "]");
								BaseLogger.log("3", null, null,"25-JAN-2017 validation for  [" + childNodeName + "] valid_uptoDate [ " + valid_uptoDate + "] eff_fromDate [" + eff_fromDate + "]");
								if(valid_uptoDate.compareTo(eff_fromDate) <= 0)
								{
									errCode="VMVAL_UPTO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
					            }
							}
							
							if("eff_from".equalsIgnoreCase(childNodeName))
							{
								strvalid_upto = checkNull(genericUtility.getColumnValue("valid_upto",dom)) ;
								strmeff_from = checkNull(genericUtility.getColumnValue("eff_from",dom));
								
								if(strvalid_upto.length() > 0 )
								{
									valid_uptoDate = getValidDateTimeString(strvalid_upto,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());	
								}
								else
								{									
									errCode="VTVLDNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(strmeff_from.length() > 0 )
								{
									eff_fromDate= getValidDateTimeString(strmeff_from,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
								}
								else
								{
									errCode="VTEFFNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] valid_uptoDate [ " + valid_uptoDate + "] eff_fromDate [" + eff_fromDate + "]");
								BaseLogger.log("3", null, null,"25-JAN-2017 validation for  [" + childNodeName + "] valid_uptoDate [ " + valid_uptoDate + "] eff_fromDate [" + eff_fromDate + "]");
								if(valid_uptoDate.compareTo(eff_fromDate) <= 0)
								{
									errCode="VMVAL_UPTO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
					            }
							}
							   // Modified By Sagar to check null for dom value On [25-JAN-2017] [End]
							//else if(childNodeName.equals("tax_code"))
							// Modified By Sagar to check null for dom value[25-JAN-17] [Start]
							//SIR 
							if("tax_code".equalsIgnoreCase(childNodeName))
							{
								//String mValue=genericUtility.getColumnValue("tax_code",dom);
								mValue=checkNull(genericUtility.getColumnValue("tax_code",dom));
								// Modified By Sagar to check null for dom value[25-JAN-17] [End]
								//20feb19[stmt changed to pstmt and closed rs and pstmt]
								//System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] mValue [ " + mValue + "]");
								BaseLogger.log("3", null, null,"20/10/14 manohar validation for  [" + childNodeName + "] mValue [ " + mValue + "]");
								//sql="Select Count(*) as count from tax where tax_code = '"+mValue+"'";
								sql="Select Count(*) as count from tax where tax_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mValue);
								rs=pstmt.executeQuery();
								System.out.println("SQL"+sql);
								if(rs.next())
								{
									cnt=rs.getInt("count");
								}
								if(rs != null){
									rs.close();
									rs = null;
									}
								pstmt.close(); pstmt =null;
								if(cnt==0)
								{
									errCode = "VTTAX1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							// //Modified By Sagar to check null for dom value On [25-JAN-2017] [Start]
						/*else if(childNodeName.equals("rate") || childNodeName.equals("min_rate")  || childNodeName.equals("max_rate") ) // 20/10/14 manoharan min_rate and max_rate included
						{
							double mrate; // 20/10/14 manoharan changes from integer to double
							mrate=	Double.parseDouble(genericUtility.getColumnValue(childNodeName,dom) );
							System.out.println("20/10/14 manohar validation for  [" + childNodeName + "] mrate [ " + mrate + "]");
							if(mrate<=0)
							{
								errCode="VTRATE2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}*/  
							if("rate".equalsIgnoreCase(childNodeName))
							{
								
								mrateStr = checkNull(genericUtility.getColumnValue(childNodeName,dom));
								//System.out.println("mrateStr if :: "+mrateStr);
								BaseLogger.log("3", null, null, "mrateStr if :: "+mrateStr);
								if(mrateStr == null || mrateStr.length() == 0)
								{
									errCode="VTINVRATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(mrateStr.length() > 0)
								{
									mrate =	Double.parseDouble(mrateStr);
									//System.out.println("pasring mrate ::::" +mrate);
									BaseLogger.log("3", null, null, "pasring mrate ::::" +mrate);
									if( mrate <= 0 )
									{
										System.out.println("<=0 :::: "+mrate);
										errCode="VTRATE2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("25-JAN-2017 validation for  [" + childNodeName + "] mrate [ " + mrate + "]");
								BaseLogger.log("3", null, null, "25-JAN-2017 validation for  [" + childNodeName + "] mrate [ " + mrate + "]");
								
							}
							if("min_rate".equalsIgnoreCase(childNodeName))
							{
								minrateStr = checkNull(genericUtility.getColumnValue("min_rate",dom));
								maxrateStr = checkNull(genericUtility.getColumnValue("max_rate",dom));
								//System.out.println(" minrateStr :: "+minrateStr);
								//System.out.println(" maxrateStr :: "+maxrateStr);
								BaseLogger.log("3", null, null," minrateStr :: "+minrateStr +" maxrateStr :: "+maxrateStr); 
								if(minrateStr == null || minrateStr.length() == 0)
								{
									errCode="VTMINRATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(minrateStr.length() > 0)
								{
									minrate =	Double.parseDouble(minrateStr);
									//System.out.println("parsing minrate :::: " +minrate);
									BaseLogger.log("3", null, null,"parsing minrate :::: " +minrate);
									if( minrate <= 0 )
									{
										//System.out.println("<=0 :::::: "+minrate);
										BaseLogger.log("3", null, null,"<=0 :::::: "+minrate);
										errCode="VTMINERR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("31-JAN-2017 validation for  [" + childNodeName + "] minrate [ " + minrate + "]");
								BaseLogger.log("3", null, null,"31-JAN-2017 validation for  [" + childNodeName + "] minrate [ " + minrate + "]");
								if(maxrateStr.length() > 0)
								{
									maxrate =	Double.parseDouble(maxrateStr);
									//System.out.println("parsing maxrate :::: " +maxrate);
									BaseLogger.log("3", null, null,"parsing maxrate :::: " +maxrate);
									if( maxrate <= 0 )
									{
										//System.out.println("<=0 :::::: "+maxrate);
										BaseLogger.log("3", null, null,"<=0 :::::: "+maxrate);
										errCode="VTMAXERR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("31-JAN-2017 validation for  [" + childNodeName + "] maxrate [ " + maxrate + "]");
								BaseLogger.log("3", null, null,"31-JAN-2017 validation for  [" + childNodeName + "] maxrate [ " + maxrate + "]");
								/*
								if(minrate > maxrate)
								{
									errCode="VTRATEER";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}*/
								
							}
							if("max_rate".equalsIgnoreCase(childNodeName))
							{
								minrateStr = checkNull(genericUtility.getColumnValue("min_rate",dom));
								maxrateStr = checkNull(genericUtility.getColumnValue("max_rate",dom));
								//System.out.println(" minrateStr :: "+minrateStr);
								//System.out.println(" maxrateStr :: "+maxrateStr);
								BaseLogger.log("3", null, null," minrateStr :: "+minrateStr + " maxrateStr :: "+maxrateStr);
								if(maxrateStr == null || maxrateStr.length() == 0)
								{
									errCode="VTMAXVRATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if(maxrateStr.length() > 0)
								{
									maxrate =	Double.parseDouble(maxrateStr);
									//System.out.println("parsing maxrate :::: " +maxrate);
									BaseLogger.log("3", null, null,"parsing maxrate :::: " +maxrate);
									if( maxrate <= 0 )
									{
										//System.out.println("<=0 :::::: "+maxrate);
										BaseLogger.log("3", null, null,"<=0 :::::: "+maxrate);
										errCode="VTMAXERR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("31-JAN-2017 validation for  [" + childNodeName + "] maxrate [ " + maxrate + "]");
								BaseLogger.log("3", null, null,"31-JAN-2017 validation for  [" + childNodeName + "] maxrate [ " + maxrate + "]");
								if(minrateStr.length() > 0)
								{
									minrate =	Double.parseDouble(minrateStr);
									//System.out.println("parsing minrate :::: " +minrate);
									BaseLogger.log("3", null, null,"parsing minrate :::: " +minrate);
									if( minrate <= 0 )
									{
										//System.out.println("<=0 :::::: "+minrate);
										BaseLogger.log("3", null, null,"<=0 :::::: "+minrate);
										errCode="VTMINERR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								//System.out.println("31-JAN-2017 validation for  [" + childNodeName + "] minrate [ " + minrate + "]");
								BaseLogger.log("3", null, null,"31-JAN-2017 validation for  [" + childNodeName + "] minrate [ " + minrate + "]");
								
								if(minrate > maxrate)
								{
									errCode="VTRATEER";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
							//Modified By Sagar to check null for dom value  On [25-JAN-2017] [End]
						
					}
					
				}
				break;	
					
			}//switch
			//}//for
			//System.out.println("Error list ::::: [" +errList.toString()+"] >>>>>> size>>"+errList.size());
			BaseLogger.log("3", null, null,"Error list ::::: [" +errList.toString()+"] >>>>>> size>>"+errList.size());
			int errListSize = errList.size();
			int cntErr = 0;
			String errFldName = "", errorType = "";
			//System.out.println("errListSize ..........[" + errListSize + "]");
			BaseLogger.log("3", null, null,"errListSize ..........[" + errListSize + "]");
			if(errList != null && errListSize > 0)
			{
				for(cntErr = 0; cntErr < errListSize; cntErr ++)
				{
					errCode = errList.get(cntErr);
					errFldName = errFields.get(cntErr);
					//System.out.println("errCode ..........[" + errCode + "]");
					BaseLogger.log("3", null, null,"errCode ..........[" + errCode + "]");
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
			errString = errStringXml.toString();
			//System.out.println("ErrString ::"+errString);
			BaseLogger.log("3", null, null,"ErrString ::"+errString);
		}//try
		catch(Exception e)
		{
			//System.out.println("Exception ::"+e);
			BaseLogger.log("0", null, null,"Exception ::"+e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				//System.out.println("Closing Connection.....");
				BaseLogger.log("3", null, null,"Closing Connection.....");
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception se){}
		}
		
		//System.out.println("ErrString2 ::"+errString);
		BaseLogger.log("9", null, null,"ErrString2 ::"+errString);
		return errString;
	}//wfValData







public String getValidDateTimeString(String dateTimeStr, String sourceDateTimeFormat, String targetDateTimeFormat) throws ITMException
	{
		//System.out.println("Getting the valid datetime string for dateTimeStr :"+dateTimeStr+": which is in format :"+sourceDateTimeFormat);
		BaseLogger.log("3", null, null,"Getting the valid datetime string for dateTimeStr :"+dateTimeStr+": which is in format :"+sourceDateTimeFormat);
		Object date = null;
		String retDateStr = "";
		try
		{
			if (!(sourceDateTimeFormat.equalsIgnoreCase(targetDateTimeFormat)))
			{
				if (sourceDateTimeFormat.indexOf("/") != -1)
				{
					dateTimeStr.replace('/', '-');
				}
				else if (sourceDateTimeFormat.indexOf(".") != -1)
				{
					dateTimeStr.replace('.', '-');
				}
				date = new SimpleDateFormat(sourceDateTimeFormat).parse(dateTimeStr);
				SimpleDateFormat sdfOutput = new SimpleDateFormat(targetDateTimeFormat);
				retDateStr = sdfOutput.format(date);
			}
			else
			{
				retDateStr = dateTimeStr;
			}
		}
		catch (Exception e)
		{
			//System.out.println("Exception :GenericUtility :getValidDateString :==>"+e.getMessage());
			BaseLogger.log("0", null, null, "Exception :GenericUtility :getValidDateString :==>"+e.getMessage());
			throw new ITMException(e);
		}
		//System.out.println("retDateStr :"+retDateStr);
		BaseLogger.log("3", null, null, "retDateStr :"+retDateStr);
		return retDateStr;
	}
		private String errorType(Connection conn, String errorCode) throws ITMException
		{
			String msgType = "";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, errorCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					msgType = rs.getString("MSG_TYPE");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new ITMException(ex);
			} finally
			{
				try
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			return msgType;
		}
}//class