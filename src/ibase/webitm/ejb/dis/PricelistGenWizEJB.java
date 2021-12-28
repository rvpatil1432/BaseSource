package ibase.webitm.ejb.dis;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.AppConnectParm;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

@javax.ejb.Stateless
public class PricelistGenWizEJB extends ValidatorEJB implements PricelistGenWizEJBLocal, PricelistGenWizEJBRemote
{
	public String globalXtraParams = "";
	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String retString = null;
		
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			globalXtraParams = xtraParams;
			
			if (xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if (xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if (xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			retString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams );
		}
		catch (Exception e)
		{
			System.out.println ( "Exception :PricelistGenEJB :itemChanged(String,String):" + e.getMessage() + ":" );
			retString = genericUtility.createErrorString(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println ( "Return String from PricelistGenEJB ["+retString+"]" );
		
		return retString;
	}
	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		E12GenericUtility e12genericUtility = new E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer();
		
		String productCode="",descr="",loginUser="";
		int currentFormNo = 0;
		String siteCode = "", currDate = "", retString ="", dateFormat = "",grpCode="",
		priceList="",plistParent,orderType="",manageType="",methodType="",freeQty="",freeOn="",discount="",remarks1="", tranType = "A";
		boolean selectFlag=false,offerSelect=false;
		DistCommon distCommon = new DistCommon();
		InitialContext ctx = null;
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		
		
		Connection conn = null;

		try
		{
			System.out.println("itemChanged called for PricelistGen");
			
			conn = getConnection();
			
			siteCode = e12genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			loginUser=e12genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			dateFormat = e12genericUtility.getApplDateFormat();
			
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat(e12genericUtility.getApplDateFormat());
			SimpleDateFormat adf = new SimpleDateFormat(e12genericUtility.getApplDateFormat());
			currDate = sdf.format(new java.util.Date());
			System.out.println("currDate"+currDate);
			
			System.out.println("currentColumn["+currentColumn+"] currentFormNo["+currentFormNo+"]" );
			
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?><Root><Header><editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag></Header>" );
			
			switch(currentFormNo)
			{
				case 1:
				{
					System.out.println(" -------- Inside itemchange case 1111111 ------------ ");
					int existCnt=0;
				
					
					Timestamp effFrom=null,validUpto=null;
					if( currentColumn.trim().equalsIgnoreCase( "itm_default" ))
					{
						System.out.println("loginUser"+loginUser);
						String sql = "select count(1) from PRICING_MANAGE where chg_user= ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,loginUser );
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							existCnt=rs.getInt(1);
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;
						if(existCnt>0)
						{
							String getPrvData="Select a.grp_code,b.descr,a.product_code,a.eff_from,a.valid_upto,a.remarks,a.offer_free_qty,a.offer_free_on,a.disc_perc,A.tran_type "
									+ "from PRICING_MANAGE a,PRODUCT b "
									+ "where a.product_code=b.product_code "
									+ " and a.chg_user=?";
							pstmt=conn.prepareStatement(getPrvData);
							pstmt.setString(1, loginUser);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								productCode=E12GenericUtility.checkNull(rs.getString("product_code"));
								remarks1=E12GenericUtility.checkNull(rs.getString("remarks"));
								freeQty=E12GenericUtility.checkNull(rs.getString("offer_free_qty"));
								freeOn=E12GenericUtility.checkNull(rs.getString("offer_free_on"));
								discount=E12GenericUtility.checkNull(rs.getString("disc_perc"));
								effFrom=rs.getTimestamp("eff_from");
								validUpto=rs.getTimestamp("valid_upto");
								descr=E12GenericUtility.checkNull(rs.getString("descr"));
								tranType = E12GenericUtility.checkNull(rs.getString("tran_type"));
								grpCode=E12GenericUtility.checkNull(rs.getString("grp_code"));
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
							if(discount==null || discount.length()==0 )
							{
								offerSelect=true;
								
							}
						}
						
						valueXmlString.append("<Detail1 domID='1'>");
						valueXmlString.append("<tran_date><![CDATA[").append(currDate).append( "]]></tran_date>");
						valueXmlString.append("<product_code><![CDATA[" ).append(productCode).append( "]]></product_code>");
						valueXmlString.append("<descr><![CDATA[").append(descr).append( "]]></descr>");
						if(effFrom!=null)
						{
							valueXmlString.append("<eff_from><![CDATA[").append(adf.format(effFrom)).append( "]]></eff_from>");
						}
						else
						{
							valueXmlString.append("<eff_from><![CDATA[]]></eff_from>");
						}
						if(validUpto!=null)
						{
							valueXmlString.append("<valid_upto><![CDATA[").append(adf.format(validUpto)).append( "]]></valid_upto>");
						}
						else
						{
							valueXmlString.append("<valid_upto><![CDATA[]]></valid_upto>");
						}
						valueXmlString.append("<tran_type><![CDATA[").append(tranType).append( "]]></tran_type>");
						valueXmlString.append("<remarks><![CDATA[").append(remarks1).append( "]]></remarks>");
						valueXmlString.append("<offer_free_qty><![CDATA[").append(freeQty).append( "]]></offer_free_qty>");
						valueXmlString.append("<offer_free_on><![CDATA[").append(freeOn).append( "]]></offer_free_on>");
						valueXmlString.append("<disc_perc><![CDATA[").append(discount).append( "]]></disc_perc>");
						valueXmlString.append("<chg_date><![CDATA[").append("").append( "]]></chg_date>");
						valueXmlString.append("<chg_user><![CDATA[").append(loginUser).append( "]]></chg_user>");
						valueXmlString.append("<chg_term><![CDATA[").append("").append( "]]></chg_term>");
						
						valueXmlString.append("<grp_code protect = \"1\"><![CDATA[").append(grpCode).append( "]]></grp_code>");
						
						valueXmlString.append("</Detail1>" );
						
					}
					else if("product_code".equalsIgnoreCase(currentColumn))
					{
						productCode=e12genericUtility.getColumnValue("product_code",dom );
						grpCode=e12genericUtility.getColumnValue("grp_code",dom );
						System.out.println("productCode"+productCode);
						
						String sql="Select descr from product where product_code= ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, productCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							descr=rs.getString(1);
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;
						valueXmlString.append("<Detail1 domID='1'>");
						valueXmlString.append("<descr><![CDATA[").append(descr).append( "]]></descr>");
						valueXmlString.append("<grp_code protect = \"0\"><![CDATA[").append(grpCode).append( "]]></grp_code>");
						valueXmlString.append("</Detail1>" );
					}
					
					break;
				}
				case 2:
				{
					System.out.println(" -------- Inside itemchange case 222222 ------------ ");
					if( currentColumn.trim().equalsIgnoreCase( "itm_default" ))
					{
						String nodeName = "";
						String custCode = "", custName = "", saleOrder = "", custPO = "", pordDate = "", dlvCity = "", errCode ="", plistType = "",mstprice="",priceListTar="",calcMethod="",listType="";
						String effFrom1=null,validUpto1=null,remarks=null,prdCode=null;
						Map<String,HashMap<String,String>> detailDataMap = new LinkedHashMap<String,HashMap<String,String>>();
						HashMap<String,String> saleOrderDataMap = null;
						Set<String> detailDataMapKeySet = new LinkedHashSet<String>();

						HashMap<String,ArrayList<HashMap<String, String>>> plistMst=new HashMap<String,ArrayList<HashMap<String, String>>>();


						int domID = 0, detDomId = 0,previousCnt=0;
						boolean isoffer=false,isdiscount=false;
						ArrayList<String> prvPlist=new ArrayList<String>();

						String prvdet="Select price_list from PRICING_MANAGE_LIST where chg_user= ?";
						pstmt=conn.prepareStatement(prvdet);
						pstmt.setString(1, loginUser);
						rs=pstmt.executeQuery();
						while(rs.next())
						{
							prvPlist.add(rs.getString("price_list"));

						}
						System.out.println("Previous pricelist"+prvPlist+"Size"+prvPlist.size());


						freeQty=E12GenericUtility.checkNull(e12genericUtility.getColumnValue( "offer_free_qty",dom1));
						freeOn=E12GenericUtility.checkNull(e12genericUtility.getColumnValue( "offer_free_on",dom1));
						discount=E12GenericUtility.checkNull(e12genericUtility.getColumnValue( "disc_perc",dom1));
						System.out.println("freeQty ["+freeQty+"] freeOn ["+freeOn+"] discount["+discount+"]");



						effFrom1=e12genericUtility.getColumnValue("eff_from",dom1);
						validUpto1=e12genericUtility.getColumnValue("valid_upto",dom1);
						tranType=e12genericUtility.getColumnValue("tran_type",dom1);
						remarks=checkNull(e12genericUtility.getColumnValue("remarks",dom1));
						prdCode=e12genericUtility.getColumnValue("product_code",dom1);
						grpCode=checkNull(e12genericUtility.getColumnValue("grp_code",dom1));
						descr=checkNull(e12genericUtility.getColumnValue("descr",dom1));

						valueXmlString.append("<Detail2 domID='0'>");
						valueXmlString.append("<tran_date><![CDATA[").append(currDate).append( "]]></tran_date>");
						valueXmlString.append("<product_code><![CDATA[" ).append(prdCode).append( "]]></product_code>");
						valueXmlString.append("<eff_from><![CDATA[").append(effFrom1).append( "]]></eff_from>");
						valueXmlString.append("<valid_upto><![CDATA[").append(validUpto1).append( "]]></valid_upto>");
						valueXmlString.append("<tran_type><![CDATA[").append(tranType).append( "]]></tran_type>");
						valueXmlString.append("<remarks><![CDATA[").append(remarks).append( "]]></remarks>");
						valueXmlString.append("<offer_free_qty><![CDATA[").append(freeQty).append( "]]></offer_free_qty>");
						valueXmlString.append("<offer_free_on><![CDATA[").append(freeOn).append( "]]></offer_free_on>");
						valueXmlString.append("<disc_perc><![CDATA[").append(discount).append( "]]></disc_perc>");
						valueXmlString.append("<descr><![CDATA[").append(descr).append( "]]></descr>");
						valueXmlString.append("<grp_code><![CDATA[").append(grpCode).append( "]]></grp_code>");
						valueXmlString.append("</Detail2>" );


						if( discount.trim().length() >  0 )
						{
							plistType="D";

						}
						else
						{
							plistType="O";
						}
						mstprice=distCommon.getDisparams("999999", "MRP", conn);
						System.out.println("mstprice"+mstprice);

						String sql="select a.descr,a.list_type,b.price_list__tar, b.price_list__parent, b.calc_method"
								+ " from pricelist_mst a,pricelist_mst_det b"
								+ " where a.price_list=b.price_list and b.price_list = ?";						
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, mstprice);
						rs=pstmt.executeQuery();
						while(rs.next())
						{


							priceListTar=checkNull(rs.getString("price_list__tar"));
							plistParent=rs.getString("price_list__parent");
							calcMethod=rs.getString("calc_method");
							listType=rs.getString("list_type");
							//descr=checkNull(rs.getString("descr"));

							HashMap<String, String> eachPlist=new HashMap<String, String>();
							eachPlist.put("descr", getDescription(priceListTar, conn));
							eachPlist.put("calc_method",calcMethod);
							eachPlist.put("list_type",getListType(priceListTar, conn));
							eachPlist.put("isSelected","false");
							if(plistParent!=null )
							{
								eachPlist.put("plist", priceListTar);

								if(plistMst.containsKey(plistParent))
								{
									ArrayList<HashMap<String, String>> temp=new ArrayList<HashMap<String, String>>();
									temp=plistMst.get(plistParent);
									if(prvPlist.contains(priceListTar))
									{
										eachPlist.put("isSelected","true");

									}
									temp.add(eachPlist);

									System.out.println("parent key found"+temp);
									plistMst.put(plistParent,temp);
								}
								else
								{
									HashMap<String, String> eachPlistparent=new HashMap<String, String>();
									eachPlistparent.put("plist", plistParent);
									eachPlistparent.put("descr", getDescription(plistParent, conn));
									eachPlistparent.put("calc_method",calcMethod);
									eachPlistparent.put("list_type",getListType(plistParent, conn));
									if(prvPlist.contains(priceListTar))
									{
										eachPlist.put("isSelected","true");
									}
									else
									{
										eachPlist.put("isSelected","false");
									}
									ArrayList<HashMap<String, String>> n1=new ArrayList<HashMap<String, String>>();
									n1.add(eachPlist);
									n1.add(eachPlistparent);
									plistMst.put(plistParent, n1);

								}

							}
							else
							{
								eachPlist.put("plist",priceListTar);

								if(prvPlist.contains(priceListTar))
								{
									System.out.println("inside true");
									eachPlist.put("isSelected","true");
								}
								ArrayList<HashMap<String,String>> n1=new ArrayList<HashMap<String,String>>();
								n1.add(eachPlist);
								plistMst.put(priceListTar, n1);
							}

							priceList=null;
							plistParent=null;
							descr=null;
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;
						System.out.println("hasmap final"+plistMst);

						Set <String> plistmstkeys = plistMst.keySet();

						for(String eachplistkey : plistmstkeys)
						{

							ArrayList<HashMap<String,String>> getplist = plistMst.get(eachplistkey);
							int listLength = getplist.size();

							/*for(HashMap<String,String> itr: getplist)
							{*/
							HashMap<String, String> itr=getplist.get(0);

							if(listLength==1 )
							{
								domID++;
								valueXmlString.append("<Detail2 domID='"+(++detDomId)+"'>");
								valueXmlString.append("<price_list><![CDATA[").append(itr.get("plist")).append( "]]></price_list>");
								valueXmlString.append("<descr><![CDATA[").append(itr.get("descr")).append( "]]></descr>");
								valueXmlString.append("<is_expandable><![CDATA[").append("false").append( "]]></is_expandable>");
								valueXmlString.append("<is_selected><![CDATA[").append(itr.get("isSelected")).append( "]]></is_selected>");
								valueXmlString.append("<plist_type><![CDATA[").append(plistType).append( "]]></plist_type>");
								valueXmlString.append("<list_type><![CDATA[").append(itr.get("list_type")).append( "]]></list_type>");
								valueXmlString.append("<dom_id><![CDATA[").append(domID).append( "]]></dom_id>");
								valueXmlString.append("<offer_free_qty><![CDATA[").append(freeQty).append( "]]></offer_free_qty>");
								valueXmlString.append("<offer_free_on><![CDATA[").append(freeOn).append( "]]></offer_free_on>");
								valueXmlString.append("<calc_method><![CDATA[").append(itr.get("calc_method")).append( "]]></calc_method>");
								valueXmlString.append("<disc_perc><![CDATA[").append(discount).append( "]]></disc_perc>");

							}
							else
							{
								String firstplist = "";
								for(HashMap<String,String> itr1: getplist)
								{
									firstplist = itr1.get("plist");
									String firstDescr = itr1.get("descr");
									if( firstplist.equalsIgnoreCase(eachplistkey) )
									{	valueXmlString.append("<Detail2 domID='"+(++detDomId)+"'>");
									valueXmlString.append("<price_list><![CDATA[").append(firstplist).append( "]]></price_list>");
									valueXmlString.append("<descr><![CDATA[").append(firstDescr).append( "]]></descr>");
									valueXmlString.append("<is_expandable><![CDATA[").append("true").append( "]]></is_expandable>");
									break;
									}
								}

								valueXmlString.append("<plist_childs>");
								int i=0;
								for(HashMap<String, String> eachHMap : getplist)
								{

									firstplist = eachHMap.get("plist");
									if( ! firstplist.equalsIgnoreCase(eachplistkey) )
									{	i++;
									domID++;
									valueXmlString.append("<plist>");
									valueXmlString.append("<price_list><![CDATA[").append(eachHMap.get("plist")).append( "]]></price_list>");
									valueXmlString.append("<descr><![CDATA[").append(eachHMap.get("descr")).append( "]]></descr>");
									valueXmlString.append("<is_selected><![CDATA[").append(eachHMap.get("isSelected")).append( "]]></is_selected>");
									valueXmlString.append("<plist_type><![CDATA[").append(plistType).append( "]]></plist_type>");
									valueXmlString.append("<list_type><![CDATA[").append(eachHMap.get("list_type")).append( "]]></list_type>");
									valueXmlString.append("<offer_free_qty><![CDATA[").append(freeQty).append( "]]></offer_free_qty>");
									valueXmlString.append("<offer_free_on><![CDATA[").append(freeOn).append( "]]></offer_free_on>");
									valueXmlString.append("<calc_method><![CDATA[").append(eachHMap.get("calc_method")).append( "]]></calc_method>");
									valueXmlString.append("<disc_perc><![CDATA[").append(discount).append( "]]></disc_perc>");
									valueXmlString.append("<dom_id><![CDATA[").append(domID).append( "]]></dom_id>");
									valueXmlString.append("</plist>");
									System.out.println("Inside loop before increment"+domID);
									System.out.println("after increment"+domID);
									}

								}
								valueXmlString.append("</plist_childs>");
							}
							valueXmlString.append("</Detail2>");
						}
					}
					break;
				}
				case 3:
				{
					HashMap<String,ArrayList<HashMap<String, String>>> plistmstrate;
					List<String> itemCodeList = new ArrayList<String>();
					LinkedHashMap<String, ArrayList<HashMap<String,String>>> plistWiseHMap = new LinkedHashMap<String, ArrayList<HashMap<String, String>>>();

					System.out.println(" -------- Inside itemchange case 33333 ------------ ");
					if( currentColumn.trim().equalsIgnoreCase( "itm_default" ))
					{

						plistmstrate=insertDB(dom, dom1, dom2,loginUser,xtraParams,conn);

						Set <String> itemcodekey = plistmstrate.keySet();

						valueXmlString.append("<Detail3 domID='1'><price_list><![CDATA[]]></price_list>");
						int index=1;
						for(String eachplistkey : itemcodekey)
						{
							valueXmlString.append("<item_det"+index+"><![CDATA["+eachplistkey+"]]></item_det"+index+">");
							index++;
							itemCodeList.add(eachplistkey);
						}
						valueXmlString.append("</Detail3>");

						for(String eachItemCode : itemCodeList)
						{
							ArrayList<HashMap<String, String>> tempList = plistmstrate.get(eachItemCode);

							for(HashMap<String,String> tempHMap : tempList)
							{
								String priceListKey = tempHMap.get("price_list");

								if(plistWiseHMap.containsKey(priceListKey))
								{
									ArrayList<HashMap<String, String>> oldList = plistWiseHMap.get(priceListKey);
									oldList.add(tempHMap);
									plistWiseHMap.put(priceListKey, oldList);
								}
								else
								{
									ArrayList<HashMap<String, String>> tempList1 = new ArrayList<HashMap<String, String>>();
									tempList1.add(tempHMap);
									plistWiseHMap.put(priceListKey, tempList1);
								}
							}
						}

						Set<String> plistKeySet = plistWiseHMap.keySet();
						int domID = 1;
						for(String eachPriceList : plistKeySet)
						{
							ArrayList<HashMap<String, String>> plistWiseList = plistWiseHMap.get(eachPriceList);

							valueXmlString.append("<Detail3 domID='"+(++domID)+"'><price_list><![CDATA["+eachPriceList+"]]></price_list>");
							index = 1;
							for(HashMap<String, String> eachPlistWiseHMap : plistWiseList)
							{
								valueXmlString.append("<item_rate"+index+"><![CDATA["+eachPlistWiseHMap.get("rate")+"]]></item_rate"+index+">");
								index++;
							}
							valueXmlString.append("</Detail3>");
						}
					}
				}
			}
			valueXmlString.append( "</Root>" );	
		}
		catch(Exception e)
		{
			System.out.println("PricelistGenEJB.itemChanged():catchBlock"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}		
		finally
		{
			try
			{
				if(!conn.isClosed() && conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("PricelistGenEJB.itemChanged():finally block");
				e.printStackTrace();
			}
		}
		
		return valueXmlString.toString();
	}
	@Override
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext,String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("PricelistGenEJB.wfValData()");
		System.out.println("xmlString :"+xmlString);
		System.out.println("xmlString1 :"+xmlString1);
		System.out.println("xmlString2 :"+xmlString2);
		System.out.println("objContext :"+objContext);
		System.out.println("editFlag :"+editFlag);
		System.out.println("xtraParams :"+xtraParams);
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("Before calling function wfvalData****");
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);

			System.out.println("After calling method wfVAlData Error String===="+errString);
		} 
		catch (Exception e)
		{
			System.out.println("Exception : [PricelistGenEJB][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}
	
	@Override
	public String wfValData(Document currFormDataDom, Document hdrDataDom, Document allFormDataDom, String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		String errString = "";
		int currentFormNo = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		String childNodeName = null;
		Node parentNode = null;
		int childNodeListLength;
		Node childNode = null;
		String errCode=null,errorType=null,productCode=null,userId=null,grpCode=null;
		int ctr=0,count=0,cnt=0;
		E12GenericUtility e12genericUtility = new E12GenericUtility();
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		
		Connection conn = null;
		try
		{
			conn=getConnection();
			userId = e12genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("currentFormNo ["+currentFormNo+"]");
			}
			switch (currentFormNo)
			{
				case 1:
				{
					
					parentNodeList = currFormDataDom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("Child name --->> "+childNodeName);				
						
						if(childNodeName.equalsIgnoreCase("product_code")) 
						{
							productCode=e12genericUtility.getColumnValue("product_code", currFormDataDom);
							String sql="Select count(*) from product where product_code= ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, productCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								count=rs.getInt(1);
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
							if(count==0)
							{
								errCode="VTNFBYPROD";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							
							
						}
						if(childNodeName.equalsIgnoreCase("grp_code")) 
						{
							grpCode=e12genericUtility.getColumnValue("grp_code", currFormDataDom);
							productCode=e12genericUtility.getColumnValue("product_code", currFormDataDom);
							
							if(grpCode!=null && grpCode.trim().length()>0 )
							{
							String sql1="Select count(*) from item where product_code= ? and grp_code= ?";
							pstmt=conn.prepareStatement(sql1);
							pstmt.setString(1, productCode);
							pstmt.setString(2, grpCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								count=rs.getInt(1);
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
							if(count==0)
							{
								errCode="VTINVGRPCD";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
								
							}
							
							}
							
						}
					}
					
					System.out.println("errString["+errString+"]");
				
					break;
				}
				case 2:
				{
					System.out.println("errString["+errString+"]");
				
					break;
				}
			}
			
			int errListSize = errList.size();
			cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					errString = getErrorString( errFldName, errCode, userId );
					errorType =  errorType( conn, errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				
				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}	
			
		}
		catch(Exception e)
		{
			System.out.println("PricelistGenEJB.wfValData()");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return errStringXml.toString();
	}
	
	@Override
	public String handleRequest(HashMap<String, String> reqParamMap) 
	{
		String action = "", retXMLStr = "";

		try
		{
			
			action = (String)reqParamMap.get("action");
			
			if("ITEM_CHANGE".equalsIgnoreCase(action))
			{
				String currXmlDataStr = "", hdrXmlDataStr = "", allXmlDataStr = "", currentColumn = "", objContext = "", editFlag = "";
				
				currXmlDataStr = (String)reqParamMap.get("CUR_XML_STR");
				hdrXmlDataStr = (String)reqParamMap.get("HDR_XML_STR");
				allXmlDataStr = (String)reqParamMap.get("ALL_XML_STR");
				currentColumn = (String)reqParamMap.get("CUR_COLUMN");
				objContext = (String)reqParamMap.get("OBJ_CONTEXT");
				editFlag = (String)reqParamMap.get("EDIT_FLAG");
				
				retXMLStr = itemChanged(currXmlDataStr, hdrXmlDataStr, allXmlDataStr, objContext, currentColumn, editFlag, globalXtraParams);
				
				System.out.println("retXMLStr["+retXMLStr+"] for action ["+action+"]");
			}
			else if("DELETE".equalsIgnoreCase(action))
			{
				String loginUser="";
				loginUser=(String)reqParamMap.get("loginUser");
				System.out.println("Current loginUser"+loginUser);
				
				retXMLStr=deleteCurr(loginUser);
				
				System.out.println("Delete"+retXMLStr);
						
				
			}
			
		}
		catch(Exception e)
		{
			System.out.println("PricelistGenEJB.handleRequest()"+e.getMessage());
			e.printStackTrace();
		}
		
		return retXMLStr;
	}
	
	private static String checkNull(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input;
	}
	
	public InitialContext getInitialContext()throws ITMException
	{
		InitialContext ctx = null;
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
		}
		catch(ITMException itme)
		{
			System.out.println("PricelistGenEJB.getInitialContext()");
			throw itme;
		}
		catch(Exception e)
		{
			System.out.println("PricelistGenEJB.getInitialContext()"+e.getMessage());
			throw new ITMException(e);
		}
		return ctx;
	}
	
	public String getValue(String lineItemStr, String column) throws ITMException
	{
		String retValue = "";
		Document lineDom = null;
		
		try
		{
			lineDom =new E12GenericUtility().parseString("<detail>"+lineItemStr+"</detail>");
			retValue = lineDom.getElementsByTagName(column).item(0).getTextContent();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		return retValue;
	}
	
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}
	private String errorType( Connection conn , String errorCode ) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, checkNull(errorCode));			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		}		
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
	
	public String deleteCurr(String loginUser) throws ITMException
	{
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Connection conn=null;
		int hdelcnt=0,detdelcnt=0;
		try
		{
			conn=getConnection();
			String sqldel = "DELETE FROM PRICING_MANAGE WHERE chg_user = ? ";
			pstmt=conn.prepareStatement(sqldel);
			pstmt.setString(1,loginUser);
			hdelcnt=pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			String sqldeldet = "DELETE FROM PRICING_MANAGE_LIST WHERE chg_user = ? ";
			pstmt=conn.prepareStatement(sqldeldet);
			pstmt.setString(1,loginUser);
			detdelcnt=pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			if(hdelcnt>0 && detdelcnt>0)
			{
				System.out.println("Connection Commited");
				conn.commit();
				return "Success";
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "ERROR";
		
		
	}	
	
	
	
	@SuppressWarnings("unchecked")
	public HashMap<String,ArrayList<HashMap<String,String>>> insertDB(Document dom,Document dom1,Document dom2,String loginUser,String xtraParams,Connection conn) throws SQLException, ITMException
	{
		String tranId="",qty="",qtyFreeOn="",tranType=null,discPerc=null,productCode=null,remarks=null,priceList=null,plistParent=null,listType=null,calcMethod=null,mstprice=null,grpCode=null;
		int lineNo=0,headcnt=0,hdelcnt=0,detdelcnt=0;
		int [] batchCount;
		ArrayList<HashMap<String, String>> targetPlist =new ArrayList();
		 HashMap<String, ArrayList<HashMap<String, String>>> generateAndInsertPriceList=new HashMap();
		String xmlValues=null,uniqueKey = null;
		Timestamp effFrom=null,validUpto=null,tranDate=null,currDate=null,currAppdate=null;
		E12GenericUtility genericUtility=new E12GenericUtility();
		PreparedStatement pstmt=null,pstmt1=null;
		Statement stmt=null;
		ResultSet rs=null,rs1=null;
		DistCommon distCommon = new DistCommon();
		boolean isError = false;
		try
		{
			
			mstprice=distCommon.getDisparams("999999", "MRP", conn);
			StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        
	        transformer.transform(new DOMSource(dom2), new StreamResult(sw));
	        System.out.println("All DOM"+sw.toString());
	        SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			
			qty=genericUtility.getColumnValue("offer_free_qty", dom1);
			productCode=genericUtility.getColumnValue("product_code", dom1);
			
			grpCode=checkNull(genericUtility.getColumnValue("grp_code", dom1));
			
			qtyFreeOn=genericUtility.getColumnValue("offer_free_on", dom1);
			remarks=genericUtility.getColumnValue("remarks", dom1);
			
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate=java.sql.Timestamp.valueOf(sdf1.format(currDate) + " 00:00:00.000");
			System.out.println("currAppdate"+currAppdate);
			
			//Deleting previous Data
			String sqldel = "DELETE FROM PRICING_MANAGE WHERE chg_user = ? ";
			pstmt=conn.prepareStatement(sqldel);
			pstmt.setString(1,loginUser);
			hdelcnt=pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			String sqldeldet = "DELETE FROM PRICING_MANAGE_LIST WHERE chg_user = ? ";
			pstmt=conn.prepareStatement(sqldeldet);
			pstmt.setString(1,loginUser);
			detdelcnt=pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			System.out.println("hdelcnt["+hdelcnt+"] detdelcnt["+detdelcnt+"]");
			
			effFrom = Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("eff_from", dom1), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			validUpto = Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("valid_upto", dom1), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("Eff_From"+effFrom);
			System.out.println("valid_upto"+validUpto);
			
			tranType=genericUtility.getColumnValue("tran_type", dom1);
			discPerc=genericUtility.getColumnValue("disc_perc", dom1);
			
			String keyStringQuery = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'W_PLISTGEN_WIZ'";
			pstmt = conn.prepareStatement(keyStringQuery);
			rs = pstmt.executeQuery();
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			//String uniqueKey = null;
			String insertSql = "";
			String updateSql = "";
			int insertCnt = 0;
			int updateCnt = 0;
			if (rs.next()) {
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);
			}
			xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :[" + xmlValues + "]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE",
					"");
			uniqueKey = tg.generateTranSeqID("P-WIZ", keyCol, keyString,
					conn);
			System.out.println("uniqueKey :" + uniqueKey);
			String sql="Insert into pricing_manage(tran_id,tran_date,product_code,eff_from,valid_upto,remarks,tran_type,offer_free_qty,offer_free_on,disc_perc,chg_user,grp_code) values(?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, uniqueKey);
			pstmt.setTimestamp(2, currAppdate);
			pstmt.setString(3, productCode);
			pstmt.setTimestamp(4, effFrom);
			pstmt.setTimestamp(5, validUpto);
			pstmt.setString(6, checkNull(remarks));
			pstmt.setString(7, checkNull(tranType));
			pstmt.setString(8, checkNull(qty));
			pstmt.setString(9, checkNull(qtyFreeOn));
			pstmt.setString(10, checkNull(discPerc));
			pstmt.setString(11, checkNull(loginUser));
			pstmt.setString(12, checkNull(grpCode));
			System.out.println("uniqueKey["+uniqueKey+"] currAppdate["+currAppdate+"] productCode["+productCode+"] effFrom["+effFrom+"] "
					+ "validUpto["+validUpto+"]remarks["+remarks+"]tranType"+tranType+"] qty["+qty+"]qtyFreeOn["+qtyFreeOn+"]discPerc["+discPerc+"]");
			headcnt=pstmt.executeUpdate();
			
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			
			
			Node currDetail=null;
			NodeList detailList = dom2.getElementsByTagName("Detail2");
			int noOfDetails = detailList.getLength();
			String sql2="Insert into PRICING_MANAGE_LIST(tran_id,line_no,price_list,price_list__parent,offer_free_qty,offer_free_on,disc_perc,chg_user) "
					+ "values(?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql2);
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				lineNo++;
				currDetail = detailList.item(ctr);
				HashMap<String,String> eachPlist=new HashMap();
				qty=genericUtility.getColumnValueFromNode("offer_free_qty", currDetail);
				qtyFreeOn=genericUtility.getColumnValueFromNode("offer_free_on", currDetail);
				discPerc=genericUtility.getColumnValueFromNode("disc_perc", currDetail);
				priceList=genericUtility.getColumnValueFromNode("price_list", currDetail);
				plistParent=genericUtility.getColumnValueFromNode("price_list__parent", currDetail);
				listType=genericUtility.getColumnValueFromNode("list_type", currDetail);
				calcMethod=genericUtility.getColumnValueFromNode("calc_method", currDetail);
				
				
				eachPlist.put("price_list_mrp", mstprice);
				eachPlist.put("price_list_tar", priceList);
				eachPlist.put("price_list_parent", plistParent);
				eachPlist.put("calc_method", calcMethod);
				eachPlist.put("free_qty", checkNull(qty));
				eachPlist.put("free_on_qty", checkNull(qtyFreeOn));
				eachPlist.put("disc_perc", checkNull(discPerc));
				eachPlist.put("list_type", checkNull(listType));
				targetPlist.add(eachPlist);
				
				System.out.println("ctr"+lineNo+"]qty["+qty+"] qtyFreeOn["+qtyFreeOn+"] discPerc["+discPerc+"] priceList["+priceList+"] plistParent["+plistParent+"]");
				
				
				/*String sql2="Insert into PRICING_MANAGE_LIST(tran_id,line_no,price_list,price_list_parent,offer_free_qty,offer_free_on,disc_perc) "
						+ "values(?,?,?,?,?,?,?)";*/
				
				pstmt.setString(1,uniqueKey);
				pstmt.setInt(2,lineNo);
				pstmt.setString(3,checkNull(priceList));
				pstmt.setString(4,checkNull(plistParent));
				pstmt.setString(5,checkNull(qty));
				pstmt.setString(6,checkNull(qtyFreeOn));
				pstmt.setString(7,checkNull(discPerc));
				pstmt.setString(8,checkNull(loginUser));
				
				
				pstmt.addBatch();
				qty=null;
				qtyFreeOn=null;
				discPerc=null;	
				priceList=null;
				plistParent=null;
			}
			System.out.println("sending ArrayList"+targetPlist);
			
			PricelistGenEJB ins = new PricelistGenEJB();
			Object retObj = ins.generateAndInsertPriceList(productCode,grpCode, targetPlist, genericUtility.getColumnValue("eff_from", dom1), genericUtility.getColumnValue("valid_upto", dom1), xtraParams, true, conn);
			if(retObj instanceof HashMap<?, ?>)
			{
				generateAndInsertPriceList = (HashMap<String, ArrayList<HashMap<String, String>>>) retObj;
			}
			
			System.out.println("generateAndInsertPriceList"+generateAndInsertPriceList);
			batchCount=pstmt.executeBatch();
			if(batchCount.length==lineNo && headcnt==1 && hdelcnt>0 && detdelcnt>0)
			{
				System.out.println("Returning true");
				return generateAndInsertPriceList;
			}
			
			pstmt.close();
			pstmt=null;
			
			
		}
		catch(Exception e)
		{
			isError = true;
			System.out.println(e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(!isError)
				{
					if(conn != null)
					{
						conn.commit();
					}
				}
				else
				{
					if(conn!=null)
					{
						conn.rollback();
					}
				}
			}
			catch(SQLException se)
			{
				System.out.println("PricelistGenWizEJB.insertDB()["+se.getMessage()+"]");
				throw se;
			}
		}

		return generateAndInsertPriceList;
	}
	public String getDescription(String pricelist,Connection conn) throws ITMException
	{
		String result="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try
		{
			String sql="Select descr from pricelist_mst where price_list= ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,pricelist );
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				result=E12GenericUtility.checkNull(rs.getString(1));
				
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return result;
	}
	public String getListType(String pricelist,Connection conn) throws ITMException
	{
		String result="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try
		{
			String sql="Select list_type from pricelist_mst where price_list = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,pricelist );
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				result= E12GenericUtility.checkNull(rs.getString(1));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return result;
	}
	
	
}