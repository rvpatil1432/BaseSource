/********************************************************
	Title : DistIssWiz EJB
	Date  : 20 - Aug - 2014
	Author: Deepak Sawant.

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class DistIssWiz extends ValidatorEJB implements DistIssWizLocal, DistIssWizRemote {

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String userId = null;
	String chgUser = null;
	String chgTerm = null;
	NumberFormat nf = null;
	boolean isError=false;



	public DistIssWiz() 
	{
		System.out.println("^^^^^^^ inside Distribution Issue Wizard ^^^^^^^");
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside Distribution Issue Wizard >^^^^^^^");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";

		try {
			System.out.println("Distribution Issue Wizard xmlString [" + xmlString + "] \r\n xmlString1 [" + xmlString1 + "]" );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
				System.out.println("Distribution Issue Wizard xmlString2 [" + xmlString2 + "]" );
			}
			if (objContext != null && Integer.parseInt(objContext) == 1) 
			{
				parentNodeList = dom2.getElementsByTagName("Header0");
				parentNode = parentNodeList.item(1);
				childNodeList = parentNode.getChildNodes();
				for (int x = 0; x < childNodeList.getLength(); x++) 
				{
					childNode = childNodeList.item(x);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("Detail1")) 
					{
						errString = wfValData(dom, dom1, dom2, "1", editFlag, xtraParams);
						if (errString != null && errString.trim().length() > 0)
							break;
					} else if (childNodeName.equalsIgnoreCase("Detail2")) 
					{
						errString = wfValData(dom, dom1, dom2, "2", editFlag, xtraParams);
						break;
					}
				}
			} else 
			{
				errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			}
		} catch (Exception e) {
			System.out.println("Exception : Inside DocumentMaster wfValData Method ..> " + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside Distribution Issue wfValData >^^^^^^^");
		//GenericUtility genericUtility;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0, currentFormNo = 0, childNodeListLength = 0, cnt = 0;
		String childNodeName = null;
		String errString = "",errorType="";
		String errCode = "";
		Connection conn = null;
		String userId = "",qcLockValue ="";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String sql = "",locCode ="",invstat="",aval="",avalyn="";
        double qtyConf=0,qtyShip=0,totQty=0,detquantity=0,stkquantity=0,quantity=0,totquantity=0,dodetquantity=0,allQty=0;

		String squantity="",distOrder = "",itemCode="",confirmed="",tranId="",siteCode="",lotSl="",addUser="",tranCode="",siteCodeDlv="",lotno="";
		
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		
		try {

			System.out.println("editFlag>>>>wf"+editFlag);
			System.out.println("xtraParams>>>wf"+xtraParams);


			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//genericUtility = GenericUtility.getInstance();
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {


					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("dist_order1"))
					{

						distOrder = genericUtility.getColumnValue("dist_order1",dom);

						if (distOrder == null || distOrder.trim().length() == 0)
						{
							 errCode = "DIDONULL";
							 errList.add( errCode );
							 errFields.add( childNodeName.toLowerCase() );
							
						}
						else
						{

							sql = "select count(1) from distorder where dist_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if(cnt == 0)
							{
								errCode = "DIDONOTEX";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}else
							{
								sql = "select confirmed from distorder where dist_order = ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,distOrder);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									confirmed = rs.getString(1) == null ? "":rs.getString(1);
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;

								if(!confirmed.equals("Y"))
								{
									errCode = "DIDONOTCO";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}else
								{

									sql = "select count(1) from ship_docs where ref_ser = 'D-ISS' and ref_id = ?";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1,distOrder);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
									}
									pstmt.close();
									rs.close();
									pstmt = null;
									rs = null;
									if(cnt > 0)
									{
										errCode = "DIDOEISHDO";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}

							}

						}

					}

				}
				break;
			case 2:


				//System.out.println("DOM>>>> Elements>>["+genericUtility.serializeDom(dom).toString()+"]");
				//System.out.println("DOM1>> Elements>>["+genericUtility.serializeDom(dom1).toString()+"]");
				//System.out.println("DOM2>> Elements>>["+genericUtility.serializeDom(dom2).toString()+"]");	

				parentNodeList = dom2.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				//System.out.println("parentNode >>>{"+parentNode+"}");
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();


				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("value of child node : "+childNode);


					if(childNodeName.equalsIgnoreCase("add_user"))
					{/*
						System.out.println("in add user val");
					
						addUser = genericUtility.getColumnValue("add_user",dom2);
						chgUser = genericUtility.getColumnValue("chg_user",dom2);
						distOrder = genericUtility.getColumnValue("dist_order1", dom1);
						System.out.println("addUser><><"+addUser+"chgUserDD"+chgUser+"<D<V<V<V"+distOrder);
						if ((addUser != null && addUser.trim().length() > 0) && (chgUser != null && chgUser.trim().length() > 0))
						{
							
							sql = "select count(1) from distorder where dist_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if(cnt > 0)
							{
								if(!addUser.equalsIgnoreCase(chgUser))
								{
									errCode = "DIDOADDUM";
									errString = getErrorString("add_user",errCode,userId);
									break;
								}
							}
							
							
						}
					
				*/}
					if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = genericUtility.getColumnValue("tran_code",dom2);
						if(childNode.getFirstChild()!= null)
						{
							tranCode = childNode.getFirstChild().getNodeValue();
						}

						System.out.println("tranCode>>"+tranCode);

						if (tranCode == null || tranCode.trim().length() == 0)
						{
							errCode = "DIDOTRCONU";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							sql = "select count(1) from transporter where tran_code = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;

							if(cnt == 0)
							{
								errCode = "DIDOTRCONE";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("tran_id"))
					{

						//tranId = genericUtility.getColumnValue("tran_id",dom2);
						if(childNode.getFirstChild()!= null)
						{
							tranId = childNode.getFirstChild().getNodeValue();
							System.out.println("tranId ["+tranId + "]");
							sql = "select count(1) from ship_docs where ref_ser = 'D-ISS' and ref_id = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,tranId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if(cnt > 0)
							{
								errCode = "DIDOEISHDO";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						
					}


				}
				break;
			case 3:

				parentNodeList = dom2.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{


					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					
					System.out.println("Distribution Issue Wizard validation for  [" + childNodeName + "]" );
					if(childNodeName.equalsIgnoreCase("lot_sl"))
					{
						siteCode = genericUtility.getColumnValue("site_code",dom2,"2");
						lotSl = genericUtility.getColumnValue("lot_sl",dom);
						distOrder = genericUtility.getColumnValue("dist_order",dom);

						if (lotSl == null || lotSl.trim().length() == 0)
						{
							errCode = "DIDOLSNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							sql = "select count(1) from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							pstmt.setString(2,lotSl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;

							if(cnt == 0)
							{
								errCode = "DIDOLSSTK";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						sql = "select qty_confirm,qty_shipped from distorder_det where dist_order = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,distOrder);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							qtyConf = rs.getDouble(1);
							qtyShip = rs.getDouble(2);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;

						totQty = qtyConf - qtyShip;
						
						if(totQty < 0)
						{
							errCode = "DIDOTQLTZ";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						
					}
					else if(childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode = genericUtility.getColumnValue("item_code",dom);
						tranId = genericUtility.getColumnValue("tran_id",dom);
						distOrder = genericUtility.getColumnValue("dist_order",dom);
						System.out.println("itemCode>>"+itemCode+"tranId>"+tranId+"distOrder>"+distOrder);
						String itemCodeH="";
						
						List<String> itemCodeList = new ArrayList<String>();
						
						if (itemCode == null || itemCode.trim().length() == 0)
						{
							errCode = "DIDIICNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							sql = "select item_code from distorder_det where dist_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrder);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								//itemCodeH = rs.getString(1) == null ? "":rs.getString(1);
								itemCodeList.add(rs.getString(1) == null ? "":rs.getString(1).trim());
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;

							//if(!itemCode.equalsIgnoreCase(itemCodeH))
							if(!itemCodeList.contains(itemCode))
							{
								errCode = "DIDIICNOMA";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}

					else if(childNodeName.equalsIgnoreCase("quantity"))
					{	
						distOrder = genericUtility.getColumnValue("dist_order",dom);
						squantity = genericUtility.getColumnValue("quantity",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom2,"2");
						itemCode = genericUtility.getColumnValue("item_code",dom);
						lotSl = genericUtility.getColumnValue("lot_sl",dom);
						locCode = genericUtility.getColumnValue("loc_code",dom);
						lotno = genericUtility.getColumnValue("lot_no",dom);
						tranId = genericUtility.getColumnValue("tran_id",dom);
						ArrayList<String> lockList=new ArrayList<String>();
						
						    HashMap<String,String>invHoldMap=new HashMap<String,String>();
						    invHoldMap.put("item_code", itemCode);
	                        invHoldMap.put("site_code", siteCode);
	                        invHoldMap.put("loc_code", locCode);
	                        invHoldMap.put("lot_no", lotno);
	                        invHoldMap.put("lot_sl", lotSl);
	                        
	                        avalyn=getColumnDescr(conn, "available_yn", "distord_iss", "tran_id", tranId);
							avalyn=avalyn==null ? "N" : avalyn.trim();
	                        
                        System.out.println("distIssWiz 3 validate squantity ["+squantity+"] distOrder ["+distOrder+"] siteCode ["+siteCode+"]itemCode [" + itemCode + "] locCode [" + locCode + "] lotno [" + lotno + "] lotSl ["+lotSl);
						if (squantity == null || squantity.trim().length() == 0)
						{
							quantity = 0;
						}
						else
						{
							quantity = Double.parseDouble(squantity);
						}
						System.out.println("quantity>>>>>>{{{"+quantity);	
					
						qcLockValue=getColumnDescr(conn, "var_value", "disparm", "var_name", "QUARNTINE_LOCKCODE");
						System.out.println("qcLockValue-------->>["+qcLockValue+"]");
						lockList=getInvHoldQCLock(invHoldMap, conn);	
						System.out.println("Return lockList-------->>["+lockList+"]");
						int listCount=lockList.size();
						int occurence=Collections.frequency(lockList, qcLockValue);
						int otherLockCount= listCount - occurence;
						System.out.println("Occurence-------->>["+occurence+"]");
						if("N".equalsIgnoreCase(avalyn)){
							if(otherLockCount > 0){								
								errCode = "VTSTKHOLD";//Despatch can not be created as qunaity is on hold.
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}	
						
						/*sql = "select quantity from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,lotSl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stkquantity = rs.getDouble(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						System.out.println("stkquantity>>"+stkquantity);*/
						sql = "select sum(quantity) from distord_issdet where dist_order = ? and item_code = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,distOrder);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							detquantity = rs.getDouble(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						//System.out.println("detquantity>>trsn"+detquantity+">>>"+tranId+"<VCXCXc"+tranId.trim().length()+"}}");
                        System.out.println("distIssWiz 3 validate squantity ["+squantity+"] detquantity ["+detquantity+"] siteCode ["+siteCode+"]itemCode [" + itemCode + "] locCode [" + locCode + "] lotno [" + lotno + "] lotSl ["+lotSl + "]");
						totquantity = detquantity + quantity;
						
						sql = "select sum(qty_order) from distorder_det where dist_order = ? and item_code = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,distOrder);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							dodetquantity = rs.getDouble(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
                        System.out.println("distIssWiz 3 validate squantity ["+squantity+"] detquantity ["+detquantity+"] dodetquantity ["+dodetquantity+"]");
						if(totquantity > dodetquantity)
						{
							errCode = "DIDOQUANDT";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							
							/*sql = "select ALLOC_QTY from stock WHERE site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
							pstmt= conn.prepareStatement(sql);
							//System.out.println("Second sql ["+sql + "]");
							pstmt.setString( 1, siteCode);
							pstmt.setString( 2, lotSl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								allQty = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;*/
							
							if(quantity <= 0)
							{
								errCode = "DIDOQUALSM";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							
						}
						
						/*else
						{
						if(totquantity > stkquantity)
						{
							errCode = "DIDOQUANST";
							errString = getErrorString("quantity",errCode,userId);
							break;
						}
					}*/
				}
					

					else if(childNodeName.equalsIgnoreCase("loc_code"))
					{
						locCode = genericUtility.getColumnValue("loc_code",dom);
						tranId = genericUtility.getColumnValue("tran_id",dom);
						distOrder = genericUtility.getColumnValue("dist_order",dom);
						if (locCode == null || locCode.trim().length() == 0)
						{
							errCode = "DIDOLCNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}else
						{

							sql = "select inv_stat from location where loc_code = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,locCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								invstat =  rs.getString("inv_stat")==null?"":rs.getString("inv_stat").trim();
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;

							sql = "select available from invstat where inv_stat = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,invstat);
							rs = pstmt.executeQuery();
							if(rs.next())
							{ 
								aval =  rs.getString("available")==null?"":rs.getString("available").trim();
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;

							sql = "select case when avaliable_yn is null then 'N' else avaliable_yn end as avaliable_yn from distorder where dist_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								avalyn =  rs.getString("avaliable_yn")==null?"":rs.getString("avaliable_yn").trim();
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
						
							
							if(avalyn.equalsIgnoreCase("N") && "Y".equalsIgnoreCase(aval)){
								errCode = "VTAVAIL";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
								
							}else if(avalyn.equalsIgnoreCase("Y") && "N".equalsIgnoreCase(aval)){
								errCode = "VTAVAIL";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							
						}

					}
				}
				break;
			}
			
			int errListSize = errList.size();
			System.out.println("errListSize----- >>"+errListSize);
			cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					//String errMsg = hashMap.get(errCode)!=null ? hashMap.get(errCode).toString():"";
					//System.out.println("errMsg .........."+errMsg);
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
			
		} catch (Exception e) {
			e.printStackTrace();			
			errString = e.getMessage();
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return errStringXml.toString();
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
			System.out.println("currentColumn"+currentColumn);
			System.out.println("editFlag"+editFlag);
			System.out.println("xtraParams"+xtraParams);


			System.out.println("xmlString ["+xmlString + "]");
			System.out.println("xmlString1 ["+xmlString1 + "]");
			System.out.println("xmlString2 ["+xmlString2 + "]");
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [itemChanged(String,String)] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}


	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {

		StringBuffer valueXmlString = null;
		int currentFormNo = 0, lineNo = 0;
		Connection conn = null;
		double squantity = 0.0;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null ,rs1 = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat simpleDateFormat = null;
		//GenericUtility genutility = new GenericUtility();
		E12GenericUtility genutility= new  E12GenericUtility();
		String locCode="",lotNo="",itmdesc="",siteCode="",locdesc="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon disCommon = new DistCommon();
		System.out.println("DOM111 Elements>>["+genericUtility.serializeDom(dom).toString()+"]");
		System.out.println("DOM222 Elements>>["+genericUtility.serializeDom(dom1).toString()+"]");
		System.out.println("DOM322 Elements>>["+genericUtility.serializeDom(dom2).toString()+"]");
		String distOrder = "",itemCode = "",sql="",unit="",packCode="",tranType ="",siteCodeDlv="";
		java.sql.Timestamp currDate = null;
		double grossWeight = 0,netWeight=0,tareWeight=0,noOfArt=0,allQty=0,setQty=0;
		SimpleDateFormat sdf = null;
		String currAppdate = "",qcLockValue="";
		String tranid ="";
		String rate = "";
		double amount = 0;
		int cnt = 0;
        String addUser="",avalyn="";
		String tranCode = "",tranName="",transMode="";
		StringBuffer detail2xml = new StringBuffer();
		String tranDate = null;

		PreparedStatement pstmt2 ,pstmt3=null;
		ResultSet rs2 = null,rs3 = null,rsp=null;


		String unitAlt = null;
		int count = 0;
		double minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,grossWt = 0d,tarePer = 0d,netWt = 0d,tareWt =0d, rateClgVal = 0d, rate2 = 0d;
		double disAmount = 0d, shipperQty = 0d,discount =0,netAmt = 0,taxAmt=0;
		int  minShelfLife = 0, noArt1 = 0;
		int mLineNoDist =0;
		double qtyConfirm =0,qtyShipped =0,lcQtyOrderAlt =0,lcFact =0;
		double	ratefd =0,ratealt= 0;
		String siteCodeMfg = "", sundryCode = "";
		String priceList = "", tabValue = "", priceListClg = "", chkDate = "";
		String res = "", locCodeDamaged = "",availableYn ="";
		String checkIntegralQty = "", tranTypeParent ="";
		String rate1 = "";
		String active = "",errCode ="",sql2 ="",noArt ="",itemDescr="",shelflifetype="";
		String errString ="",siteCodeShip ="";
		String  lotSl ="",rateClg ="",rateFmDistOrd = "";
		java.util.Date chkDate1 = null;
		String prvDeptCode = null,deptCode ="";
		String locGroupJwiss="";
		String subSQL="",gdistOrder="";
		 List<String> addUsers = new ArrayList<String>();
		 List<String> gdistOrders = new ArrayList<String>();
		 
		try
		{   
			sdf=new SimpleDateFormat(genutility.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate = sdf.format(currDate);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userId");

			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO IS"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			switch (currentFormNo) {

			case 1 :
				break;

			case 2 : 
				//System.out.println("DOM2 Elements["+genericUtility.serializeDom(dom2).toString()+"]");

				//System.out.println("itm_default >>>>> 2>>");
				distOrder = genericUtility.getColumnValue("dist_order1", dom1);
				
				System.out.println("addUser><><jfjf"+addUser+"chgUserDDhfhf"+chgUser+"<D<V<V<Vfff"+distOrder);
				
				sql = "select count(1) from distord_iss "
				+ " where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' "
				+ " and dist_order = ? and tran_ser = 'D-ISS' and add_user = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				pstmt.setString(2,chgUser);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				pstmt.close();
				rs.close();
				pstmt = null;
				rs = null;
				chgUser = chgUser.trim();

				if(cnt > 0 && currentColumn.trim().equalsIgnoreCase("itm_default") )
				{
					System.out.println("in itm default for checked itm_default");
					sql =   "SELECT SITE_A.DESCR,SITE_B.DESCR,LOCATION.DESCR,DISTORD_ISS.TRAN_ID,DISTORD_ISS.TRAN_DATE,DISTORD_ISS.EFF_DATE,DISTORD_ISS.DIST_ORDER,DISTORD_ISS.SITE_CODE,DISTORD_ISS.SITE_CODE__DLV,"
						+"DISTORD_ISS.DIST_ROUTE,DISTORD_ISS.TRAN_CODE,DISTORD_ISS.LR_NO,DISTORD_ISS.LR_DATE,DISTORD_ISS.LORRY_NO,DISTORD_ISS.GROSS_WEIGHT,"
						+"DISTORD_ISS.TARE_WEIGHT,DISTORD_ISS.NET_WEIGHT,DISTORD_ISS.FRT_AMT,DISTORD_ISS.AMOUNT,DISTORD_ISS.TAX_AMT,DISTORD_ISS.NET_AMT,DISTORD_ISS.REMARKS,"
						+"DISTORD_ISS.FRT_TYPE,DISTORD_ISS.CHG_USER,DISTORD_ISS.CHG_TERM,DISTORD_ISS.CURR_CODE,DISTORD_ISS.CHG_DATE,"
						+"TRANSPORTER.TRAN_NAME,CURRENCY_A.DESCR,DISTORD_ISS.CONFIRMED,DISTORD_ISS.LOC_CODE__GIT,DISTORD_ISS.CONF_DATE,DISTORD_ISS.NO_ART,DISTORD_ISS.TRANS_MODE,"
						+"DISTORD_ISS.GP_NO,DISTORD_ISS.GP_DATE,DISTORD_ISS.CONF_PASSWD,DISTORD_ISS.ORDER_TYPE,DISTORD_ISS.GP_SER,DISTORD_ISS.REF_NO,DISTORD_ISS.REF_DATE,DISTORD_ISS.AVAILABLE_YN,"
						+"SITE_B.ADD1,SITE_B.ADD2,SITE_B.CITY,SITE_B.PIN,SITE_B.STATE_CODE,DISTORD_ISS.EXCH_RATE,DISTORD_ISS.TRAN_TYPE,DISTORD_ISS.EMP_CODE__APRV,DISTORD_ISS.DISCOUNT,DISTORD_ISS.PERMIT_NO,"
						+"DISTORD_ISS.SHIPMENT_ID,DISTORD_ISS.CURR_CODE__FRT,DISTORD_ISS.EXCH_RATE__FRT,CURRENCY_B.DESCR,DISTORD_ISS.RD_PERMIT_NO,DISTORD_ISS.DC_NO,DISTORD_ISS.TRAN_SER,DISTORD_ISS.PART_QTY,SPACE(100) "
						+"AS SUNDRY_DETAILS,SPACE(100) AS "
						+"SUNDRY_NAME,DISTORD_ISS.PROJ_CODE,SITE_B.TELE1,SITE_B.TELE2,SITE_B.TELE3,DISTORD_ISS.SITE_CODE__BIL,SITE_C.DESCR,SITE_C.ADD1,SITE_C.ADD2,SITE_C.CITY,SITE_C.PIN,SITE_C.STATE_CODE,"
						+"DISTORD_ISS.PALLET_WT,DISTORDER.AUTO_RECEIPT,DISTORD_ISS.CR_TERM,DISTORD_ISS.DLV_TERM,DISTORD_ISS.OUTSIDE_INSPECTION,DISTORD_ISS.LABEL_TYPE,DISTORD_ISS.ADD_USER,DISTORD_ISS.ADD_TERM "
						+"FROM DISTORD_ISS  DISTORD_ISS,SITE SITE_A,SITE SITE_B,LOCATION "
						+"LOCATION,TRANSPORTER  TRANSPORTER,CURRENCY CURRENCY_A,CURRENCY" 
						+" CURRENCY_B,SITE SITE_C,DISTORDER  DISTORDER WHERE ( "
						+"DISTORD_ISS.SITE_CODE = SITE_A.SITE_CODE ) AND ( "
						+"DISTORD_ISS.SITE_CODE__DLV = SITE_B.SITE_CODE ) AND ( "
						+"DISTORD_ISS.LOC_CODE__GIT = LOCATION.LOC_CODE ) AND ( "
						+"DISTORD_ISS.CURR_CODE = CURRENCY_A.CURR_CODE  ) AND ( "
						+"DISTORD_ISS.DIST_ORDER = DISTORDER.DIST_ORDER ) AND ( "
						+"DISTORD_ISS.TRAN_CODE=TRANSPORTER.TRAN_CODE(+)) AND ( "
						+"DISTORD_ISS.CURR_CODE__FRT=CURRENCY_B.CURR_CODE(+)) AND (" 
						+"DISTORD_ISS.SITE_CODE__BIL=SITE_C.SITE_CODE(+)) AND "
						+"DISTORD_ISS.DIST_ORDER    = '"+distOrder+"' AND DISTORD_ISS.ADD_USER = '"+chgUser+"'"
						+ "and CASE WHEN DISTORD_ISS.confirmed IS NULL THEN 'N' ELSE DISTORD_ISS.CONFIRMED  END = 'N' ";



					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{

						valueXmlString.append("<Detail2  domID='1' objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
						valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
						valueXmlString.append("<tran_id>").append("<![CDATA[" + (rs.getString("tran_id")==null?"":rs.getString("tran_id").trim()) + "]]>").append("</tran_id>");
						
						
						simpleDateFormat=new SimpleDateFormat(genutility.getApplDateFormat());
						currDate = rs.getTimestamp("tran_date");
						if(currDate != null)
						{
							currAppdate = simpleDateFormat.format(currDate);
						}
						valueXmlString.append("<tran_date>").append("<![CDATA[").append((rs.getDate("tran_date") == null) ? "":sdf.format(rs.getDate("tran_date"))).append("]]>").append("</tran_date>");
						valueXmlString.append("<eff_date>").append("<![CDATA[").append((rs.getDate("eff_date") == null) ? "":sdf.format(rs.getDate("eff_date"))).append("]]>").append("</eff_date>");
						/*valueXmlString.append("<tran_date><![CDATA["+currAppdate+"]]></tran_date>");
						valueXmlString.append("<eff_date><![CDATA["+currAppdate+"]]></eff_date>");*/
						valueXmlString.append("<dist_order><![CDATA["+(rs.getString("dist_order")==null?"":rs.getString("dist_order").trim())+"]]></dist_order>");
						valueXmlString.append("<site_code><![CDATA["+(rs.getString("site_code")==null?"":rs.getString("site_code").trim())+"]]></site_code>");
						valueXmlString.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
						valueXmlString.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
						valueXmlString.append("<tran_code>").append("<![CDATA[" + (rs.getString("tran_code")==null?"":rs.getString("tran_code").trim()) + "]]>").append("</tran_code>");
						valueXmlString.append("<lr_no><![CDATA[]]></lr_no>");
						valueXmlString.append("<lr_date><![CDATA[]]></lr_date>");
						valueXmlString.append("<lorry_no><![CDATA[]]></lorry_no>");
						valueXmlString.append("<gross_weight><![CDATA["+(rs.getDouble("gross_weight"))+"]]></gross_weight>");
						valueXmlString.append("<tare_weight><![CDATA["+(rs.getDouble("tare_weight"))+"]]></tare_weight>");
						valueXmlString.append("<net_weight><![CDATA["+(rs.getDouble("net_weight"))+"]]></net_weight>");
						valueXmlString.append("<frt_amt><![CDATA[0]]></frt_amt>");
						valueXmlString.append("<amount><![CDATA[0]]></amount>");
						/*valueXmlString.append("<tax_amt><![CDATA[0]]></tax_amt>");
						valueXmlString.append("<net_amt><![CDATA[0]]></net_amt>");*/
						valueXmlString.append("<tax_amt><![CDATA["+rs.getDouble("tax_amt")+"]]></tax_amt>");
						valueXmlString.append("<net_amt><![CDATA["+rs.getDouble("net_amt")+"]]></net_amt>");
						valueXmlString.append("<remarks>").append("<![CDATA[" + (rs.getString("remarks")==null?"":rs.getString("remarks").trim()) + "]]>").append("</remarks>");
						valueXmlString.append("<frt_type><![CDATA[T]]></frt_type>");
						valueXmlString.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
						valueXmlString.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
						valueXmlString.append("<curr_code><![CDATA["+(rs.getString("curr_code")==null?"":rs.getString("curr_code").trim())+"]]></curr_code>");
						valueXmlString.append("<chg_date><![CDATA["+currAppdate+"]]></chg_date>");
						valueXmlString.append("<site_descr><![CDATA["+(rs.getString(1)==null?"":rs.getString(1).trim())+"]]></site_descr>");
						valueXmlString.append("<site_to_descr><![CDATA["+(rs.getString(2)==null?"":rs.getString(2).trim())+"]]></site_to_descr>");
						valueXmlString.append("<location_descr><![CDATA["+(rs.getString(3)==null?"":rs.getString(3).trim())+"]]></location_descr>");
						valueXmlString.append("<tran_name>").append("<![CDATA[" + (rs.getString("tran_name")==null?"":rs.getString("tran_name").trim()) + "]]>").append("</tran_name>");
						valueXmlString.append("<currency_descr><![CDATA[]]></currency_descr>");
						valueXmlString.append("<confirmed><![CDATA[N]]></confirmed>");
						valueXmlString.append("<loc_code__git><![CDATA["+(rs.getString("loc_code__git")==null?"":rs.getString("loc_code__git"))+"]]></loc_code__git>");
						valueXmlString.append("<conf_date>").append("<![CDATA[").append((rs.getDate("conf_date") == null) ? "":sdf.format(rs.getDate("conf_date"))).append("]]>").append("</conf_date>");
						valueXmlString.append("<no_art><![CDATA["+(rs.getDouble("no_art"))+"]]></no_art>");
						valueXmlString.append("<trans_mode>").append("<![CDATA[" + (rs.getString("trans_mode")==null?"":rs.getString("trans_mode").trim()) + "]]>").append("</trans_mode>");
						valueXmlString.append("<gp_no><![CDATA[]]></gp_no>");
						valueXmlString.append("<gp_date>").append("<![CDATA[").append((rs.getDate("gp_date") == null) ? "":sdf.format(rs.getDate("gp_date"))).append("]]>").append("</gp_date>");
						valueXmlString.append("<conf_passwd/>");
						valueXmlString.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
						valueXmlString.append("<gp_ser><![CDATA[I]]></gp_ser>");
						valueXmlString.append("<ref_no><![CDATA[]]></ref_no>");
						valueXmlString.append("<ref_date><![CDATA[]]></ref_date>");
						valueXmlString.append("<available_yn><![CDATA["+(rs.getString("available_yn")==null?"N":rs.getString("available_yn").trim())+"]]></available_yn>");
						valueXmlString.append("<site_add1><![CDATA["+(rs.getString("ADD1")==null?"":rs.getString("ADD1").trim())+"]]></site_add1>");
						valueXmlString.append("<site_add2><![CDATA["+(rs.getString("ADD2")==null?"":rs.getString("ADD2").trim())+"]]></site_add2>");
						valueXmlString.append("<site_city><![CDATA["+(rs.getString("CITY")==null?"":rs.getString("CITY").trim())+"]]></site_city>");
						valueXmlString.append("<site_pin><![CDATA["+(rs.getString("PIN")==null?"":rs.getString("PIN").trim())+"]]></site_pin>");
						valueXmlString.append("<site_state_code><![CDATA["+(rs.getString("STATE_CODE")==null?"":rs.getString("STATE_CODE").trim())+"]]></site_state_code>");
						valueXmlString.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
						valueXmlString.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
						valueXmlString.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
						valueXmlString.append("<discount><![CDATA[0]]></discount>");
						valueXmlString.append("<permit_no><![CDATA[]]></permit_no>");
						valueXmlString.append("<shipment_id><![CDATA[]]></shipment_id>");
						valueXmlString.append("<curr_code__frt><![CDATA["+(rs.getString("curr_code__frt")==null?"":rs.getString("curr_code__frt").trim())+"]]></curr_code__frt>");
						valueXmlString.append("<exch_rate__frt><![CDATA["+rs.getDouble("exch_rate__frt")+"]]></exch_rate__frt>");
						valueXmlString.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
						valueXmlString.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
						valueXmlString.append("<dc_no><![CDATA[]]></dc_no>");
						valueXmlString.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
						valueXmlString.append("<part_qty><![CDATA[A]]></part_qty>");
						valueXmlString.append("<sundry_details><![CDATA[]]></sundry_details>");
						valueXmlString.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
						valueXmlString.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
						valueXmlString.append("<site_tele1><![CDATA[]]></site_tele1>");
						valueXmlString.append("<site_tele2><![CDATA[]]></site_tele2>");
						valueXmlString.append("<site_tele3><![CDATA[]]></site_tele3>");
						valueXmlString.append("<site_code__bil><![CDATA[]]></site_code__bil>");
						valueXmlString.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
						valueXmlString.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
						valueXmlString.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
						valueXmlString.append("<site_city_bill><![CDATA[]]></site_city_bill>");
						valueXmlString.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
						valueXmlString.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
						valueXmlString.append("<pallet_wt><![CDATA[]]></pallet_wt>");
						valueXmlString.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
						System.out.println("chgUser>>"+rs.getString("add_user")==null?"":rs.getString("add_user")+">>>chgTerm"+rs.getString("add_term")==null?"":rs.getString("add_term")+"currAppdate>>>"+currAppdate);
						valueXmlString.append("<add_user><![CDATA["+(rs.getString("add_user")==null?"":rs.getString("add_user").trim())+"]]></add_user>");
						valueXmlString.append("<add_term><![CDATA["+(rs.getString("add_term")==null?"":rs.getString("add_term").trim())+"]]></add_term>");
						valueXmlString.append("<add_date>").append("<![CDATA[" + currAppdate + "]]>").append("</add_date>");
						valueXmlString.append("</Detail2>");
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
				}
				/*else
				{

						sql="SELECT D.DIST_ORDER AS DIST_ORDER,D.ORDER_DATE AS ORDER_DATE,D.SITE_CODE__SHIP AS SITE_CODE__SHIP,D.SITE_CODE__DLV AS SITE_CODE__DLV,D.SHIP_DATE AS SHIP_DATE,D.DUE_DATE AS DUE_DATE,D.REMARKS AS REMARKS,"
							+" D.DIST_ROUTE AS DIST_ROUTE,D.PRICE_LIST AS PRICE_LIST,D.CONFIRMED AS CONFIRMED,D.CHG_USER AS CHG_USER,D.CHG_TERM AS CHG_TERM,D.TARGET_WGT AS TARGET_WGT,D.TARGET_VOL AS TARGET_VOL,D.LOC_CODE__GIT AS LOC_CODE__GIT,"
							+" D.CHG_DATE AS CHG_DATE,SITE_A.DESCR AS SITEA_DESCR,SITE_B.DESCR AS SITEB_DESCR,LOCATION.DESCR AS LOCATION_DESCR,D.CONF_DATE AS CONF_DATE,D.SITE_CODE AS SITE_CODE,D.STATUS AS STATUS,D.SALE_ORDER AS SALE_ORDER,"
							+" D.REMARKS1 AS REMARK1,D.REMARKS2 AS REMARK2,TRIM(D.ORDER_TYPE) AS ORDER_TYPE,SITE_A.ADD1 AS SITEA_ADD1,SITE_A.ADD2 AS SITEA_ADD2,SITE_A.CITY AS SITEA_CITY,SITE_A.PIN AS SITEA_PIN,SITE_A.STATE_CODE AS SITEA_STATE_CODE,"
							+" SITE_B.ADD1 AS SITEB_ADD1,SITE_B.ADD2 AS SITEB_ADD2,SITE_B.CITY AS SITEB_CITY,SITE_B.PIN AS SITEB_PIN,SITE_B.STATE_CODE AS SITEB_STATE_CODE,D.LOC_CODE__CONS AS LOC_CODE__CONS,D.SUNDRY_TYPE AS SUNDRY_TYPE,"
							+" D.SUNDRY_CODE AS SUNDRY_CODE,D.AUTO_RECEIPT AS AUTO_RECEIPT,D.TRAN_TYPE AS TRAN_TYPE,D.CURR_CODE AS CURR_CODE,D.EXCH_RATE AS EXCH_RATE,D.SALES_PERS AS SALES_PERS,SALES_PERS.SP_NAME AS SP_NAME,"
							+" D.LOC_CODE__GITBF AS LOC_CODE__GITBF,D.CUST_CODE__DLV AS CUST_CODE__DLV,D.DLV_TO AS DLV_TO,D.DLV_ADD1 AS DLV_ADD1,D.DLV_ADD2 AS DLV_ADD2,D.DLV_ADD3 AS DLV_ADD3,D.DLV_CITY AS DLV_CITY,"
							+" D.STATE_CODE__DLV AS STATE_CODE__DLV,D.COUNT_CODE__DLV AS COUNT_CODE__DLV,D.DLV_PIN AS DLV_PIN,D.STAN_CODE AS STAN_CODE,D.TEL1__DLV AS TEL1__DLV,D.TEL2__DLV AS TEL2__DLV,D.TEL3__DLV AS TEL3__DLV,"
							+" D.FAX__DLV AS FAX__DLV,D.AVALIABLE_YN AS AVALIABLE_YN,D.PURC_ORDER AS PURC_ORDER,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,D.NET_AMT AS NET_AMT,D.TRAN_SER AS TRAN_SER,"
							+" D.PRICE_LIST__CLG AS PRICE_LIST__CLG,SPACE(25) AS LOC,FN_SUNDRY_NAME(D.SUNDRY_TYPE,D.SUNDRY_CODE,'N') AS SUNDRY_NAME,"
							+" D.PROJ_CODE AS PROJ_CODE,SITE_C.DESCR AS SITEC_DESCR,D.POLICY_NO AS POLICY_NO,D.LOC_CODE__DAMAGED AS LOC_CODE__DAMAGED,D.SITE_CODE__BIL AS SITE_CODE__BIL,SITE_D.DESCR AS SITED_DESCR,SITE_D.ADD1 AS SITED_ADD1,"
							+" SITE_D.ADD2 AS SITED_ADD2,SITE_D.CITY AS SITED_CITY,SITE_D.PIN SITED_PIN ,SITE_D.STATE_CODE AS SITED_STATE_CODE,D.TRANS_MODE AS TRANS_MODE"
							+"  FROM DISTORDER  D,SITE SITE_A,SITE SITE_B,LOCATION  LOCATION,SALES_PERS  SALES_PERS,SITE SITE_C,SITE SITE_D "
							+"  WHERE ( D.SITE_CODE__SHIP      = SITE_A.SITE_CODE  ) AND "
							+" ( D.SITE_CODE__DLV      = SITE_B.SITE_CODE (+)  ) AND "
							+" ( D.LOC_CODE__GIT      = LOCATION.LOC_CODE (+)  ) AND "
							+" ( D.SITE_CODE      = SITE_C.SITE_CODE (+)  ) AND "
							+" ( D.SALES_PERS=SALES_PERS.SALES_PERS(+)) AND "
							+" ( D.SITE_CODE__BIL=SITE_D.SITE_CODE(+)) "
							+"  AND ( ( D.DIST_ORDER    = '"+distOrder+"' ) ) ";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = 1;
								valueXmlString.append("<Detail2 domID='" + count + "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
								valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\"/>\r\n");
								valueXmlString.append("<tran_id/>");
								valueXmlString.append("<tran_date><![CDATA["+currAppdate+"]]></tran_date>");
								valueXmlString.append("<eff_date><![CDATA["+currAppdate+"]]></eff_date>");
								valueXmlString.append("<gp_date><![CDATA["+currAppdate+"]]></gp_date>");
								valueXmlString.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
								valueXmlString.append("<site_code><![CDATA["+(rs.getString("SITE_CODE__SHIP")==null?"":rs.getString("SITE_CODE__SHIP").trim())+"]]></site_code>");
								valueXmlString.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
								valueXmlString.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
								valueXmlString.append("<tran_code><![CDATA[]]></tran_code>");
								valueXmlString.append("<lr_no><![CDATA[]]></lr_no>");
								valueXmlString.append("<lr_date><![CDATA[]]></lr_date>");
								valueXmlString.append("<lorry_no><![CDATA[]]></lorry_no>");
								valueXmlString.append("<gross_weight><![CDATA[0]]></gross_weight>");
								valueXmlString.append("<tare_weight><![CDATA[0]]></tare_weight>");
								valueXmlString.append("<net_weight><![CDATA[0]]></net_weight>");
								valueXmlString.append("<frt_amt><![CDATA[0]]></frt_amt>");
								valueXmlString.append("<amount><![CDATA[0]]></amount>");
							//	valueXmlString.append("<tax_amt><![CDATA[0]]></tax_amt>");
							//	valueXmlString.append("<net_amt><![CDATA[0]]></net_amt>");
								valueXmlString.append("<tax_amt><![CDATA["+rs.getDouble("tax_amt")+"]]></tax_amt>");
								valueXmlString.append("<net_amt><![CDATA["+rs.getDouble("net_amt")+"]]></net_amt>");
								valueXmlString.append("<remarks><![CDATA["+(rs.getString("remarks")==null?"":rs.getString("remarks").trim())+"]]></remarks>");
								valueXmlString.append("<frt_type><![CDATA[T]]></frt_type>");
								valueXmlString.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
								valueXmlString.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
								valueXmlString.append("<curr_code><![CDATA["+(rs.getString("curr_code")==null?"":rs.getString("curr_code").trim())+"]]></curr_code>");
								valueXmlString.append("<chg_date><![CDATA["+currAppdate+"]]></chg_date>");
								valueXmlString.append("<site_descr><![CDATA["+(rs.getString("SITEA_DESCR")==null?"":rs.getString("SITEA_DESCR").trim())+"]]></site_descr>");
								valueXmlString.append("<site_to_descr><![CDATA["+(rs.getString("SITEB_DESCR")==null?"":rs.getString("SITEB_DESCR").trim())+"]]></site_to_descr>");
								valueXmlString.append("<location_descr><![CDATA["+(rs.getString("LOCATION_DESCR")==null?"":rs.getString("LOCATION_DESCR").trim())+"]]></location_descr>");
								valueXmlString.append("<tran_name><![CDATA[]]></tran_name>");
								valueXmlString.append("<currency_descr><![CDATA[]]></currency_descr>");
								valueXmlString.append("<confirmed><![CDATA[N]]></confirmed>");
								valueXmlString.append("<loc_code__git><![CDATA["+(rs.getString("LOC_CODE__GITBF")==null?"":rs.getString("LOC_CODE__GITBF"))+"]]></loc_code__git>");
								//valueXmlString.append("<conf_date><![CDATA["+sdf.format(rs.getTimestamp("CONF_DATE"))+"]]></conf_date>");
								valueXmlString.append("<conf_date><![CDATA[]]></conf_date>");
								valueXmlString.append("<no_art><![CDATA[0]]></no_art>");
								valueXmlString.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE").trim())+"]]></trans_mode>");
								valueXmlString.append("<gp_no><![CDATA[]]></gp_no>");
								valueXmlString.append("<conf_passwd/>");
								valueXmlString.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
								valueXmlString.append("<gp_ser><![CDATA[I]]></gp_ser>");
								valueXmlString.append("<ref_no><![CDATA[]]></ref_no>");
								valueXmlString.append("<ref_date><![CDATA[]]></ref_date>");
								valueXmlString.append("<available_yn><![CDATA["+(rs.getString("AVALIABLE_YN")==null?"N":rs.getString("AVALIABLE_YN").trim())+"]]></available_yn>");
								valueXmlString.append("<site_add1><![CDATA["+(rs.getString("SITEA_ADD1")==null?"":rs.getString("SITEA_ADD1").trim())+"]]></site_add1>");
								valueXmlString.append("<site_add2><![CDATA["+(rs.getString("SITEA_ADD2")==null?"":rs.getString("SITEA_ADD2").trim())+"]]></site_add2>");
								valueXmlString.append("<site_city><![CDATA["+(rs.getString("SITEA_CITY")==null?"":rs.getString("SITEA_CITY").trim())+"]]></site_city>");
								valueXmlString.append("<site_pin><![CDATA["+(rs.getString("SITEA_PIN")==null?"":rs.getString("SITEA_PIN").trim())+"]]></site_pin>");
								valueXmlString.append("<site_state_code><![CDATA["+(rs.getString("SITEA_STATE_CODE")==null?"":rs.getString("SITEA_STATE_CODE").trim())+"]]></site_state_code>");
								valueXmlString.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
								valueXmlString.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
								valueXmlString.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
								valueXmlString.append("<discount><![CDATA[0]]></discount>");
								valueXmlString.append("<permit_no><![CDATA[]]></permit_no>");
								valueXmlString.append("<shipment_id><![CDATA[]]></shipment_id>");
								valueXmlString.append("<curr_code__frt><![CDATA[ ]]></curr_code__frt>");
								valueXmlString.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
								valueXmlString.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
								valueXmlString.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
								valueXmlString.append("<dc_no><![CDATA[]]></dc_no>");
								valueXmlString.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
								valueXmlString.append("<part_qty><![CDATA[A]]></part_qty>");
								valueXmlString.append("<sundry_details><![CDATA[]]></sundry_details>");
								valueXmlString.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
								valueXmlString.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
								valueXmlString.append("<site_tele1><![CDATA[]]></site_tele1>");
								valueXmlString.append("<site_tele2><![CDATA[]]></site_tele2>");
								valueXmlString.append("<site_tele3><![CDATA[]]></site_tele3>");
								valueXmlString.append("<site_code__bil><![CDATA[]]></site_code__bil>");
								valueXmlString.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
								valueXmlString.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
								valueXmlString.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
								valueXmlString.append("<site_city_bill><![CDATA[]]></site_city_bill>");
								valueXmlString.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
								valueXmlString.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
								valueXmlString.append("<pallet_wt><![CDATA[]]></pallet_wt>");
								valueXmlString.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
								System.out.println("chgUser>>"+chgUser+">>>chgTerm"+chgTerm+"currAppdate>>>"+currAppdate);
								valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>"); //changes done by deepak
								valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
								valueXmlString.append("<add_date>").append("<![CDATA[" + currAppdate + "]]>").append("</add_date>");
								valueXmlString.append("</Detail2>");

							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;

						
						
						
						
						}
						
					}
					
				} */
				else   
				{

					sql="SELECT D.DIST_ORDER AS DIST_ORDER,D.ORDER_DATE AS ORDER_DATE,D.SITE_CODE__SHIP AS SITE_CODE__SHIP,D.SITE_CODE__DLV AS SITE_CODE__DLV,D.SHIP_DATE AS SHIP_DATE,D.DUE_DATE AS DUE_DATE,D.REMARKS AS REMARKS,"
							+" D.DIST_ROUTE AS DIST_ROUTE,D.PRICE_LIST AS PRICE_LIST,D.CONFIRMED AS CONFIRMED,D.CHG_USER AS CHG_USER,D.CHG_TERM AS CHG_TERM,D.TARGET_WGT AS TARGET_WGT,D.TARGET_VOL AS TARGET_VOL,D.LOC_CODE__GIT AS LOC_CODE__GIT,"
							+" D.CHG_DATE AS CHG_DATE,SITE_A.DESCR AS SITEA_DESCR,SITE_B.DESCR AS SITEB_DESCR,LOCATION.DESCR AS LOCATION_DESCR,D.CONF_DATE AS CONF_DATE,D.SITE_CODE AS SITE_CODE,D.STATUS AS STATUS,D.SALE_ORDER AS SALE_ORDER,"
							+" D.REMARKS1 AS REMARK1,D.REMARKS2 AS REMARK2,TRIM(D.ORDER_TYPE) AS ORDER_TYPE,SITE_A.ADD1 AS SITEA_ADD1,SITE_A.ADD2 AS SITEA_ADD2,SITE_A.CITY AS SITEA_CITY,SITE_A.PIN AS SITEA_PIN,SITE_A.STATE_CODE AS SITEA_STATE_CODE,"
							+" SITE_B.ADD1 AS SITEB_ADD1,SITE_B.ADD2 AS SITEB_ADD2,SITE_B.CITY AS SITEB_CITY,SITE_B.PIN AS SITEB_PIN,SITE_B.STATE_CODE AS SITEB_STATE_CODE,D.LOC_CODE__CONS AS LOC_CODE__CONS,D.SUNDRY_TYPE AS SUNDRY_TYPE,"
							+" D.SUNDRY_CODE AS SUNDRY_CODE,D.AUTO_RECEIPT AS AUTO_RECEIPT,D.TRAN_TYPE AS TRAN_TYPE,D.CURR_CODE AS CURR_CODE,D.EXCH_RATE AS EXCH_RATE,D.SALES_PERS AS SALES_PERS,SALES_PERS.SP_NAME AS SP_NAME,"
							+" D.LOC_CODE__GITBF AS LOC_CODE__GITBF,D.CUST_CODE__DLV AS CUST_CODE__DLV,D.DLV_TO AS DLV_TO,D.DLV_ADD1 AS DLV_ADD1,D.DLV_ADD2 AS DLV_ADD2,D.DLV_ADD3 AS DLV_ADD3,D.DLV_CITY AS DLV_CITY,"
							+" D.STATE_CODE__DLV AS STATE_CODE__DLV,D.COUNT_CODE__DLV AS COUNT_CODE__DLV,D.DLV_PIN AS DLV_PIN,D.STAN_CODE AS STAN_CODE,D.TEL1__DLV AS TEL1__DLV,D.TEL2__DLV AS TEL2__DLV,D.TEL3__DLV AS TEL3__DLV,"
							+" D.FAX__DLV AS FAX__DLV,D.AVALIABLE_YN AS AVALIABLE_YN,D.PURC_ORDER AS PURC_ORDER,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,D.NET_AMT AS NET_AMT,D.TRAN_SER AS TRAN_SER,"
							+" D.PRICE_LIST__CLG AS PRICE_LIST__CLG,SPACE(25) AS LOC,FN_SUNDRY_NAME(D.SUNDRY_TYPE,D.SUNDRY_CODE,'N') AS SUNDRY_NAME,"
							+" D.PROJ_CODE AS PROJ_CODE,SITE_C.DESCR AS SITEC_DESCR,D.POLICY_NO AS POLICY_NO,D.LOC_CODE__DAMAGED AS LOC_CODE__DAMAGED,D.SITE_CODE__BIL AS SITE_CODE__BIL,SITE_D.DESCR AS SITED_DESCR,SITE_D.ADD1 AS SITED_ADD1,"
							+" SITE_D.ADD2 AS SITED_ADD2,SITE_D.CITY AS SITED_CITY,SITE_D.PIN SITED_PIN ,SITE_D.STATE_CODE AS SITED_STATE_CODE,D.TRANS_MODE AS TRANS_MODE"
							+"  FROM DISTORDER  D,SITE SITE_A,SITE SITE_B,LOCATION  LOCATION,SALES_PERS  SALES_PERS,SITE SITE_C,SITE SITE_D "
							+"  WHERE ( D.SITE_CODE__SHIP      = SITE_A.SITE_CODE  ) AND "
							+" ( D.SITE_CODE__DLV      = SITE_B.SITE_CODE (+)  ) AND "
							+" ( D.LOC_CODE__GIT      = LOCATION.LOC_CODE (+)  ) AND "
							+" ( D.SITE_CODE      = SITE_C.SITE_CODE (+)  ) AND "
							+" ( D.SALES_PERS=SALES_PERS.SALES_PERS(+)) AND "
							+" ( D.SITE_CODE__BIL=SITE_D.SITE_CODE(+)) "
							+"  AND ( ( D.DIST_ORDER    = '"+distOrder+"' ) ) ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = 1;
						valueXmlString.append("<Detail2 domID='" + count + "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
						valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\"/>\r\n");
						valueXmlString.append("<tran_id/>");
						valueXmlString.append("<tran_date><![CDATA["+currAppdate+"]]></tran_date>");
						valueXmlString.append("<eff_date><![CDATA["+currAppdate+"]]></eff_date>");
						valueXmlString.append("<gp_date><![CDATA["+currAppdate+"]]></gp_date>");
						valueXmlString.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
						valueXmlString.append("<site_code><![CDATA["+(rs.getString("SITE_CODE__SHIP")==null?"":rs.getString("SITE_CODE__SHIP").trim())+"]]></site_code>");
						valueXmlString.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
						valueXmlString.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
						valueXmlString.append("<tran_code><![CDATA[]]></tran_code>");
						valueXmlString.append("<lr_no><![CDATA[]]></lr_no>");
						valueXmlString.append("<lr_date><![CDATA[]]></lr_date>");
						valueXmlString.append("<lorry_no><![CDATA[]]></lorry_no>");
						valueXmlString.append("<gross_weight><![CDATA[0]]></gross_weight>");
						valueXmlString.append("<tare_weight><![CDATA[0]]></tare_weight>");
						valueXmlString.append("<net_weight><![CDATA[0]]></net_weight>");
						valueXmlString.append("<frt_amt><![CDATA[0]]></frt_amt>");
						valueXmlString.append("<amount><![CDATA[0]]></amount>");
						valueXmlString.append("<tax_amt><![CDATA["+rs.getDouble("tax_amt")+"]]></tax_amt>");
						valueXmlString.append("<net_amt><![CDATA["+rs.getDouble("net_amt")+"]]></net_amt>");
						valueXmlString.append("<remarks><![CDATA["+(rs.getString("remarks")==null?"":rs.getString("remarks").trim())+"]]></remarks>");
						valueXmlString.append("<frt_type><![CDATA[T]]></frt_type>");
						valueXmlString.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
						valueXmlString.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
						valueXmlString.append("<curr_code><![CDATA["+(rs.getString("curr_code")==null?"":rs.getString("curr_code").trim())+"]]></curr_code>");
						valueXmlString.append("<chg_date><![CDATA["+currAppdate+"]]></chg_date>");
						valueXmlString.append("<site_descr><![CDATA["+(rs.getString("SITEA_DESCR")==null?"":rs.getString("SITEA_DESCR").trim())+"]]></site_descr>");
						valueXmlString.append("<site_to_descr><![CDATA["+(rs.getString("SITEB_DESCR")==null?"":rs.getString("SITEB_DESCR").trim())+"]]></site_to_descr>");
						valueXmlString.append("<location_descr><![CDATA["+(rs.getString("LOCATION_DESCR")==null?"":rs.getString("LOCATION_DESCR").trim())+"]]></location_descr>");
						valueXmlString.append("<tran_name><![CDATA[]]></tran_name>");
						valueXmlString.append("<currency_descr><![CDATA[]]></currency_descr>");
						valueXmlString.append("<confirmed><![CDATA[N]]></confirmed>");
						valueXmlString.append("<loc_code__git><![CDATA["+(rs.getString("loc_code__git")==null?"":rs.getString("loc_code__git"))+"]]></loc_code__git>");
					//	valueXmlString.append("<conf_date><![CDATA["+sdf.format(rs.getTimestamp("CONF_DATE"))+"]]></conf_date>");
						valueXmlString.append("<conf_date><![CDATA[]]></conf_date>");
						valueXmlString.append("<no_art><![CDATA[0]]></no_art>");
						valueXmlString.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE").trim())+"]]></trans_mode>");
						valueXmlString.append("<gp_no><![CDATA[]]></gp_no>");
						valueXmlString.append("<conf_passwd/>");
						valueXmlString.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
						valueXmlString.append("<gp_ser><![CDATA[I]]></gp_ser>");
						valueXmlString.append("<ref_no><![CDATA[]]></ref_no>");
						valueXmlString.append("<ref_date><![CDATA[]]></ref_date>");
						valueXmlString.append("<available_yn><![CDATA["+(rs.getString("AVALIABLE_YN")==null?"N":rs.getString("AVALIABLE_YN").trim())+"]]></available_yn>");
						valueXmlString.append("<site_add1><![CDATA["+(rs.getString("SITEA_ADD1")==null?"":rs.getString("SITEA_ADD1").trim())+"]]></site_add1>");
						valueXmlString.append("<site_add2><![CDATA["+(rs.getString("SITEA_ADD2")==null?"":rs.getString("SITEA_ADD2").trim())+"]]></site_add2>");
						valueXmlString.append("<site_city><![CDATA["+(rs.getString("SITEA_CITY")==null?"":rs.getString("SITEA_CITY").trim())+"]]></site_city>");
						valueXmlString.append("<site_pin><![CDATA["+(rs.getString("SITEA_PIN")==null?"":rs.getString("SITEA_PIN").trim())+"]]></site_pin>");
						valueXmlString.append("<site_state_code><![CDATA["+(rs.getString("SITEA_STATE_CODE")==null?"":rs.getString("SITEA_STATE_CODE").trim())+"]]></site_state_code>");
						valueXmlString.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
						valueXmlString.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
						valueXmlString.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
						valueXmlString.append("<discount><![CDATA[0]]></discount>");
						valueXmlString.append("<permit_no><![CDATA[]]></permit_no>");
						valueXmlString.append("<shipment_id><![CDATA[]]></shipment_id>");
						valueXmlString.append("<curr_code__frt><![CDATA[ ]]></curr_code__frt>");
						valueXmlString.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
						valueXmlString.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
						valueXmlString.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
						valueXmlString.append("<dc_no><![CDATA[]]></dc_no>");
						valueXmlString.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
						valueXmlString.append("<part_qty><![CDATA[A]]></part_qty>");
						valueXmlString.append("<sundry_details><![CDATA[]]></sundry_details>");
						valueXmlString.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
						valueXmlString.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
						valueXmlString.append("<site_tele1><![CDATA[]]></site_tele1>");
						valueXmlString.append("<site_tele2><![CDATA[]]></site_tele2>");
						valueXmlString.append("<site_tele3><![CDATA[]]></site_tele3>");
						valueXmlString.append("<site_code__bil><![CDATA[]]></site_code__bil>");
						valueXmlString.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
						valueXmlString.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
						valueXmlString.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
						valueXmlString.append("<site_city_bill><![CDATA[]]></site_city_bill>");
						valueXmlString.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
						valueXmlString.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
						valueXmlString.append("<pallet_wt><![CDATA[]]></pallet_wt>");
						valueXmlString.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
						System.out.println("chgUser>>"+chgUser+">>>chgTerm"+chgTerm+"currAppdate>>>"+currAppdate);
						valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>"); //changes done by deepak
						valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
						valueXmlString.append("<add_date>").append("<![CDATA[" + currAppdate + "]]>").append("</add_date>");
						valueXmlString.append("</Detail2>");

					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;

				}
				

				break;
			case 3 : 
				tranDate = getCurrdateAppFormat() ;
				tranid = genericUtility.getColumnValue("tran_id", dom);
				distOrder = genericUtility.getColumnValue("dist_order1", dom1);
				siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom2, "2");
				//System.out.println("tran val"+tranid+"dist_order1>>>"+distOrder);
				
				
				sql = "select add_user,dist_order from distord_iss where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' and dist_order = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					addUser = rs.getString("add_user")==null?"":rs.getString("add_user").trim();
					addUsers.add(addUser);
					gdistOrder = rs.getString("dist_order")==null?"":rs.getString("dist_order").trim();
					gdistOrders.add(gdistOrder);
				}
				pstmt.close();
				rs.close();
				pstmt = null;
				rs = null;
				
				
				
				if(addUsers.contains(chgUser))
				{
					for(int i = 0;i < addUsers.size(); i++)
					{
						
						addUser = addUsers.get(i);
						System.out.println("addUser>>>>>"+addUser);
						
						if(chgUser.equalsIgnoreCase(addUser))
						{
							sql = "select tran_id from distord_iss where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' and dist_order = ? and add_user = ?";
							pstmt1=conn.prepareStatement(sql);
							pstmt1.setString(1,distOrder);
							pstmt1.setString(2,addUser);
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{

								tranid = rs1.getString(1) == null ? "":rs1.getString(1);

							}
							pstmt1.close();
							rs1.close();
							pstmt1 = null;
							rs1 = null;
						}
					}
					
					
				}
				else
				{
					
					sql = "select tran_id from distord_iss where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' and dist_order = ?";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,distOrder);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{

						tranid = rs1.getString(1) == null ? "":rs1.getString(1);

					}
					pstmt1.close();
					rs1.close();
					pstmt1 = null;
					rs1 = null;	
					
				
					
				}

				
				System.out.println("tranid FROM QUERY"+tranid);
				
				
				chgUser = chgUser.trim();
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					double holdQty = 0.0;
					String holdsts = "HOLD";

					lotSl = genericUtility.getColumnValue("lot_sl",dom);
					siteCode = genericUtility.getColumnValue("site_code", dom2,"2");
					
					System.out.println("in itm default for>>> check itm_default");
					System.out.println("FIND CHK ...>>>>>>.."+addUser+">>>>>>"+chgUser);
					
						System.out.println("in itm default for checkcccc itm_default");
						sql = 	 "SELECT item.descr,location.descr,"   
								+"distord_issdet.tran_id,"   
								+"distord_issdet.line_no,"   
								+"distord_issdet.dist_order,"   
								+"distord_issdet.line_no_dist_order,"  
								+"distord_issdet.item_code,"    
								+"distord_issdet.quantity,"    
								+"distord_issdet.unit,"    
								+"distord_issdet.tax_class,"    
								+"distord_issdet.tax_chap,"    
								+"distord_issdet.tax_env,"    
								+"distord_issdet.loc_code,"    
								+"distord_issdet.lot_no,"    
								+"distord_issdet.lot_sl,"    
								+"distord_issdet.pack_code,"    
								+"distord_issdet.rate,"    
								+"distord_issdet.amount,"    
								+"distord_issdet.tax_amt,"    
								+"distord_issdet.net_amt,"    
								+"distord_issdet.site_code__mfg,"    
								+"distord_issdet.mfg_date,"    
								+"distord_issdet.exp_date,"    
								+"distord_issdet.potency_perc,"    
								+"distord_issdet.no_art,"    
								+"distord_issdet.gross_weight,"    
								+"distord_issdet.tare_weight,"    
								+"distord_issdet.net_weight,"    
								+"distord_issdet.pack_instr,"    
								+"distord_issdet.dimension,"    
								+"distord_issdet.supp_code__mfg,"    
								+"distord_issdet.batch_no,"    
								+"distord_issdet.grade,"    
								+"distord_issdet.retest_date,"    
								+"distord_issdet.rate__clg,"    
								+"distord_issdet.discount,"    
								+"distord_issdet.disc_amt,"    
								+"distord_issdet.remarks,"    
								+"distord_issdet.cost_rate,"    
								+"space(300) as qty_details,"    
								+"distord_issdet.unit__alt,"    
								+"distord_issdet.conv__qty__alt,"    
								+"distord_issdet.qty_order__alt,"    
								+"distord_issdet.pallet_wt,"    
								+"distorder_det.reas_code,"    
								+"distord_issdet.rate__alt,"    
								+"distord_issdet.conv__rate_alt,"    
								+"distord_issdet.batch_size,"    
								+"distord_issdet.shelf_life_type "   
								+"FROM distord_issdet,"    
								+"item,"    
								+"location,"    
								+"distorder_det "   
								+"WHERE ( distord_issdet.item_code = item.item_code ) and "  
								+"( distord_issdet.loc_code = location.loc_code ) and "  
								+"( distord_issdet.dist_order = distorder_det.dist_order ) and "  
								+"( distord_issdet.line_no_dist_order = distorder_det.line_no ) and "  
								+" distord_issdet.tran_id = '"+tranid+"'";

						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							lineNo = rs.getInt(4);
							
							valueXmlString.append("<Detail3 domID='" +lineNo+ "'  objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
							valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
							//valueXmlString.append("<tran_id><![CDATA["+tranid+"]]></tran_id>");
							valueXmlString.append("<tran_id>").append("<![CDATA[" + tranid + "]]>").append("</tran_id>");
							valueXmlString.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
							valueXmlString.append("<line_no_dist_order><![CDATA["+rs.getInt("line_no_dist_order")+"]]></line_no_dist_order>");
							valueXmlString.append("<line_no><![CDATA["+rs.getInt("line_no")+"]]></line_no>");
							valueXmlString.append("<item_code>").append("<![CDATA["+(rs.getString("item_code")==null?"":rs.getString("item_code").trim())+"]]>").append("</item_code>\r\n");
							valueXmlString.append("<item_descr>").append("<![CDATA["+(rs.getString(1)==null?"":rs.getString(1).trim())+"]]>").append("</item_descr>\r\n");
							valueXmlString.append("<location_descr>").append("<![CDATA["+(rs.getString(2)==null?"":rs.getString(2).trim())+"]]>").append("</location_descr>\r\n");
							valueXmlString.append("<unit>").append("<![CDATA["+(rs.getString("unit")==null?"":rs.getString("unit").trim())+"]]>").append("</unit>\r\n");
							valueXmlString.append("<unit__alt>").append("<![CDATA["+(rs.getString("unit__alt")==null?"":rs.getString("unit__alt").trim())+"]]>").append("</unit__alt>\r\n");
							valueXmlString.append("<conv__qty__alt>").append("<![CDATA["+rs.getDouble("conv__qty__alt")+"]]>").append("</conv__qty__alt>\r\n");
							valueXmlString.append("<qty_order__alt>").append("<![CDATA["+rs.getDouble("qty_order__alt")+"]]>").append("</qty_order__alt>\r\n");
							valueXmlString.append("<loc_code>").append("<![CDATA["+(rs.getString("loc_code")==null?"":rs.getString("loc_code").trim())+"]]>").append("</loc_code>\r\n");
							valueXmlString.append("<rate>").append("<![CDATA["+rs.getDouble("rate")+"]]>").append("</rate>\r\n");
							valueXmlString.append("<rate__clg>").append("<![CDATA["+rs.getDouble("rate__clg")+"]]>").append("</rate__clg>\r\n"); //Commented - jiten - 05/04/06 -  as set in itemChange of lot_no
							valueXmlString.append("<quantity>").append("<![CDATA["+rs.getDouble("quantity")+"]]>").append("</quantity>\r\n");
							valueXmlString.append("<amount>").append("<![CDATA["+rs.getDouble("amount")+"]]>").append("</amount>\r\n");
							valueXmlString.append("<lot_sl>").append("<![CDATA["+(rs.getString("lot_sl")==null?"":rs.getString("lot_sl").trim())+"]]>").append("</lot_sl>\r\n");
							valueXmlString.append("<pack_code>").append("<![CDATA["+(rs.getString("pack_code")==null?"":rs.getString("pack_code").trim())+"]]>").append("</pack_code>\r\n");
							valueXmlString.append("<disc_amt>").append("<![CDATA["+rs.getDouble("disc_amt")+"]]>").append("</disc_amt>\r\n");
							valueXmlString.append("<tax_class>").append("<![CDATA["+(rs.getString("tax_class")==null?"":rs.getString("tax_class").trim())+"]]>").append("</tax_class>\r\n");
							valueXmlString.append("<tax_chap>").append("<![CDATA["+(rs.getString("tax_chap")==null?"":rs.getString("tax_chap").trim())+"]]>").append("</tax_chap>\r\n");
							valueXmlString.append("<tax_env>").append("<![CDATA["+(rs.getString("tax_env")==null?"":rs.getString("tax_env").trim())+"]]>").append("</tax_env>\r\n");
							valueXmlString.append("<gross_weight>").append("<![CDATA["+rs.getDouble("gross_weight")+"]]>").append("</gross_weight>\r\n");
							valueXmlString.append("<net_weight>").append("<![CDATA["+rs.getDouble("net_weight")+"]]>").append("</net_weight>\r\n");
							valueXmlString.append("<tare_weight>").append("<![CDATA["+rs.getDouble("tare_weight")+"]]>").append("</tare_weight>\r\n");
							valueXmlString.append("<pack_instr>").append("<![CDATA["+(rs.getString("pack_instr")==null?"":rs.getString("pack_instr").trim())+"]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
							valueXmlString.append("<retest_date>").append("<![CDATA[").append((rs.getDate("retest_date") == null) ? "":sdf.format(rs.getDate("retest_date"))).append("]]>").append("</retest_date>\r\n");
							valueXmlString.append("<dimension>").append("<![CDATA[").append((rs.getString("dimension") == null) ? "":rs.getString("dimension")).append("]]>").append("</dimension>\r\n");
							valueXmlString.append("<supp_code__mfg>").append("<![CDATA[").append((rs.getString("supp_code__mfg") == null) ? "":rs.getString("supp_code__mfg")).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
							valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append((rs.getString("site_code__mfg") == null) ? "":rs.getString("site_code__mfg")).append("]]>").append("</site_code__mfg>\r\n");
							valueXmlString.append("<mfg_date>").append("<![CDATA[").append((rs.getDate("mfg_date") == null) ? "":sdf.format(rs.getDate("mfg_date"))).append("]]>").append("</mfg_date>\r\n");
							valueXmlString.append("<exp_date>").append("<![CDATA[").append((rs.getDate("exp_date") == null) ? "":sdf.format(rs.getDate("exp_date"))).append("]]>").append("</exp_date>\r\n");
							valueXmlString.append("<potency_perc>").append("<![CDATA[").append( ( (rs.getString("potency_perc") == null) ? "": rs.getString("potency_perc") ) ).append("]]>").append("</potency_perc>\r\n");
							valueXmlString.append("<no_art>").append("<![CDATA["+rs.getDouble("no_art")+"]]>").append("</no_art>\r\n");
							valueXmlString.append("<batch_no>").append("<![CDATA[").append( ( (rs.getString("batch_no") == null) ? "":rs.getString("batch_no") ) ).append("]]>").append("</batch_no>\r\n");
							valueXmlString.append("<grade>").append("<![CDATA[").append( ( (rs.getString("grade") == null) ? "": rs.getString("grade") ) ).append("]]>").append("</grade>\r\n");
							valueXmlString.append("<lot_no>").append("<![CDATA[").append(( (rs.getString("lot_no") == null) ? "": rs.getString("lot_no"))).append("]]>").append("</lot_no>\r\n");
							lotSl = rs.getString("lot_sl")==null?"":rs.getString("lot_sl").trim();
							sql = "select HOLD_QTY  from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
							pstmt1=conn.prepareStatement(sql);
							pstmt1.setString(1,siteCode);
							pstmt1.setString(2,lotSl);
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								holdQty = rs1.getDouble(1);
							}
							pstmt1.close();
							rs1.close();
							pstmt1 = null;
							rs1 = null;
							System.out.println("holdQty value >>>>>>>"+holdQty);
							if(holdQty > 0)
							{
								valueXmlString.append("<lot_status>").append("<![CDATA[" + holdsts + "]]>").append("</lot_status>\r\n");
								
							}
							else
							{
								//not on hold
							}
							
							
							valueXmlString.append("</Detail3>");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					

					sql = "select max(line_no)  "
							+ "from distord_issdet where tran_id = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,tranid);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lineNo = rs.getInt(1);
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;

					lineNo++;
					//System.out.println("lineNo IN ITM DEFAULT ADD"+lineNo);
					//System.out.println(">>>>>>>>>>>>>..dom"+lineNo+">><><"+tranid);
				
					valueXmlString.append("<Detail3 domID='" + lineNo + "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
					valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\"/>\r\n");
					valueXmlString.append("<tran_id><![CDATA["+tranid+"]]></tran_id>");
					valueXmlString.append("<line_no><![CDATA["+lineNo+"]]></line_no>");
					//xmldetail2hdr.append("<tran_id/>");
					valueXmlString.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
					valueXmlString.append("<lot_sl><![CDATA[]]></lot_sl>");
	
					valueXmlString.append("</Detail3>");
				}

				else if(currentColumn.trim().equalsIgnoreCase("lot_sl"))
				{
					lotSl = genericUtility.getColumnValue("lot_sl",dom);
					siteCode = genericUtility.getColumnValue("site_code", dom2,"2");
					distOrder = genericUtility.getColumnValue("dist_order1", dom1);
					double holdQty = 0.0;
					String holdsts = "HOLD";
					System.out.println("<<<sdsdsddS"+siteCode);

					String currDomStr = genericUtility.serializeDom(dom);
					if(lotSl != null && lotSl.trim().length() > 0)
					{
						//item_code,no_art,quantity,amount,net_amt,discount,tax_amt,gross_weight,tare_weight,net_weight
						sql = "select LOC_CODE,LOT_NO,ITEM_CODE,QUANTITY,GROSS_WEIGHT,NET_WEIGHT,TARE_WEIGHT,NO_ART,ALLOC_QTY,HOLD_QTY from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,siteCode);
						pstmt1.setString(2,lotSl);

						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							locCode =  rs1.getString("loc_code")==null?"":rs1.getString("loc_code").trim();
							lotNo = rs1.getString("lot_no")==null?"":rs1.getString("lot_no").trim();
							itemCode = rs1.getString("item_code")==null?"":rs1.getString("item_code").trim();
							squantity = rs1.getDouble("quantity");
							grossWeight = rs1.getDouble("gross_weight");
							netWeight = rs1.getDouble("net_weight");
							tareWeight = rs1.getDouble("tare_weight");
							noOfArt = rs1.getDouble("no_art");
							allQty = rs1.getDouble("alloc_qty");
							holdQty = rs1.getDouble("hold_qty");
							
                        }
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;

						sql = "select line_no from distorder_det where item_code = ? and dist_order = ?";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
						pstmt1.setString(2,distOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							lineNo = rs1.getInt(1);
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;
						
						System.out.println("lineNo IN LOT SL"+lineNo);

						valueXmlString.append("<dist_order protect=\"0\">").append("<![CDATA[" + distOrder + "]]>").append("</dist_order>");
						setNodeValue( dom, "dist_order", getAbsString(""+distOrder));

						valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
						setNodeValue( dom, "loc_code", getAbsString(""+locCode)); 

						sql = "select descr from location where loc_code = ?";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,locCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							locdesc =  rs1.getString("descr")==null?"":rs1.getString("descr").trim();
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;



						valueXmlString.append("<location_descr protect=\"0\">").append("<![CDATA[" + locdesc + "]]>").append("</location_descr>");
						setNodeValue( dom, "location_descr", getAbsString(""+locdesc)); 

						valueXmlString.append("<lot_no protect=\"0\">").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
						setNodeValue( dom, "lot_no", getAbsString(""+lotNo)); 

						sql = "select descr from item where item_code = ?";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							itmdesc =  rs1.getString("descr")==null?"":rs1.getString("descr").trim();
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;


						valueXmlString.append("<item_descr protect=\"0\">").append("<![CDATA[" + itmdesc + "]]>").append("</item_descr>");
						setNodeValue( dom, "item_descr", getAbsString(""+itmdesc)); 

						valueXmlString.append("<item_code protect=\"0\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						setNodeValue( dom, "item_code", getAbsString(""+itemCode)); 

						
						sql = "select AVAILABLE_YN from distord_iss where tran_id = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,tranid);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							avalyn =  rs.getString("AVAILABLE_YN")==null?"":rs.getString("AVAILABLE_YN").trim();
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						
						if("Y".equalsIgnoreCase(avalyn))
						{
							
							setQty = squantity - (allQty + holdQty);
							
						}
						else
						{
							setQty = squantity - allQty;
						}
						
						valueXmlString.append("<quantity protect=\"0\">").append("<![CDATA[" + setQty + "]]>").append("</quantity>");
						setNodeValue( dom, "quantity", getAbsString(""+setQty)); 

						valueXmlString.append("<qty_order__alt protect=\"0\">").append("<![CDATA[" + setQty + "]]>").append("</qty_order__alt>");
						setNodeValue( dom, "qty_order__alt", getAbsString(""+setQty));
						
						valueXmlString.append("<gross_weight protect=\"0\">").append("<![CDATA[" + grossWeight + "]]>").append("</gross_weight>");
						setNodeValue( dom, "gross_weight", getAbsString(""+grossWeight)); 
						
						valueXmlString.append("<net_weight protect=\"0\">").append("<![CDATA[" + netWeight + "]]>").append("</net_weight>");
						setNodeValue( dom, "net_weight", getAbsString(""+netWeight)); 
						
						valueXmlString.append("<tare_weight protect=\"0\">").append("<![CDATA[" + tareWeight + "]]>").append("</tare_weight>");
						setNodeValue( dom, "tare_weight", getAbsString(""+tareWeight)); 
						
						valueXmlString.append("<no_art protect=\"0\">").append("<![CDATA[" + noOfArt + "]]>").append("</no_art>");
						setNodeValue( dom, "no_art", getAbsString(""+noOfArt));

						qcLockValue=getColumnDescr(conn, "var_value", "disparm", "var_name", "QUARNTINE_LOCKCODE");
						
						//valueXmlString.append("<lock_code><![CDATA["+ qcLockValue +"]]></lock_code>");
						valueXmlString.append("<lock_code protect=\"0\">").append("<![CDATA[" + qcLockValue + "]]>").append("</lock_code>");
						setNodeValue( dom, "lock_code", getAbsString(""+qcLockValue)); 
						
						sql = "select HOLD_QTY  from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,lotSl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							holdQty = rs.getDouble(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						System.out.println("holdQty value >>>>>>>"+holdQty);
						if(holdQty > 0)
						{
							
							valueXmlString.append("<lot_status protect=\"0\">").append("<![CDATA[" + holdsts + "]]>").append("</lot_status>");
							setNodeValue( dom, "lot_status", getAbsString(""+holdsts)); 
							//on hold
						}
						else
						{
							//not on hold
						}
						
						prvDeptCode = "NULL";
						sql="SELECT D.DIST_ORDER,D.LINE_NO AS LINE_NO,D.TRAN_ID__DEMAND,D.ITEM_CODE AS ITEM_CODE,D.QTY_ORDER AS QTY_ORDER,D.QTY_CONFIRM AS QTY_CONFIRM,"
						+" D.QTY_RECEIVED AS QTY_RECEIVED,D.QTY_SHIPPED AS QTY_SHIPPED,D.DUE_DATE AS DUE_DATE,D.TAX_CLASS AS TAX_CLASS,D.TAX_CHAP AS TAX_CHAP,D.TAX_ENV AS TAX_ENV,D.UNIT AS UNIT,ITEM.DESCR AS ITEM_DESCR,"
						+" D.SALE_ORDER AS SALE_ORDER,D.LINE_NO__SORD AS LINE_NO__SORD,D.RATE AS RATE,D.QTY_RETURN AS QTY_RETURN,D.RATE__CLG AS RATE__CLG,D.DISCOUNT AS DISCOUNT,D.REMARKS AS REMARKS,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,"
						+" D.NET_AMT AS NET_AMT,D.OVER_SHIP_PERC AS OVER_SHIP_PERC,SPACE(300) AS QTY_DETAILS,D.UNIT__ALT AS UNIT__ALT,D.CONV__QTY__ALT AS CONV__QTY__ALT,"
						+" D.QTY_ORDER__ALT AS QTY_ORDER__ALT,D.SHIP_DATE AS SHIP_DATE,D.PACK_INSTR AS PACK_INSTR ,"
						+" ( CASE WHEN ITEM.DEPT_CODE__ISS IS NULL then ' ' else ITEM.DEPT_CODE__ISS END ) AS DEPT_CODE, "
						+" H.AVALIABLE_YN, H.TRAN_TYPE AS TRAN_TYPE, CASE WHEN H.LOC_GROUP__JWISS IS NULL THEN ' ' ELSE H.LOC_GROUP__JWISS END AS LOC_GROUP "
						+" FROM DISTORDER_DET  D,ITEM  ITEM, DISTORDER H "
						+" WHERE D.DIST_ORDER = H.DIST_ORDER "
						+" AND D.ITEM_CODE = ITEM.ITEM_CODE "
						+" AND H.DIST_ORDER    = '"+distOrder+"'"
						+"  AND D.LINE_NO = '"+lineNo+"'"
						+ " AND   CASE WHEN D.STATUS IS NULL THEN 'O' ELSE D.STATUS END<>'C' "//Added by manoj dtd 24/12/2013 to exclude closed lines
						+" ORDER BY item.dept_code__iss ASC,"   //added by rajendra on 02/09/08
						+" D.LINE_NO ASC ";
						//System.out.println( "sql....................... " + sql );
						pstmt= conn.prepareStatement( sql );
						rs = pstmt.executeQuery();
						count = 0;
						detail2xml=new StringBuffer();
						
						locGroupJwiss="";
						if(rs.next())
						{
							rateClg = Double.toString(rs.getDouble( "RATE__CLG" ));  // ADDED BY RITESH  ON 17/SEP/2014
							rateFmDistOrd = Double.toString(rs.getDouble( "RATE" ));  // ADDED BY RITESH  ON 18/SEP/2014
							System.out.println(" rateClg received from dist order iss det"+rateClg);
							System.out.println(" rate received from dist order iss det"+rateFmDistOrd);
							//added by msalam on 180609 to get tran_type from distorder start
							tranType = rs.getString( "TRAN_TYPE" );
									//added by msalam on 180609 to get tran_type from distorder end 
									locGroupJwiss=rs.getString( "LOC_GROUP" );
									System.out.println("(locGroupJwiss.trim()).length()----"+(locGroupJwiss.trim()).length());
									if((locGroupJwiss.trim()).length()>0)
									{
										subSQL=" AND C.LOC_GROUP ='"+locGroupJwiss+"' ";
									}
									else
									{
										subSQL="";
									}
									// 28/05/09 manoharan available_yn added
									availableYn = rs.getString("AVALIABLE_YN");
									if( availableYn == null )
									{
										availableYn = "Y";
									}
									// end 28/05/09 manoharan available_yn added
									deptCode = rs.getString("DEPT_CODE");
									//System.out.println( "deptCode....................... " + deptCode );
									if("NULL".equalsIgnoreCase(prvDeptCode))
									{
										prvDeptCode = deptCode;
										//System.out.println( "prvDeptCode....................... " + prvDeptCode );
									}
									System.out.println("prvDeptCode----deptCode--"+prvDeptCode+"----"+deptCode);

									System.out.println( "match dept....................... " );
								
									//valueXmlString.append("<Detail3 domID='" + lineNo + "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
									//valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\"/>\r\n");
									//valueXmlString.append("<tran_id><![CDATA["+tranid+"]]></tran_id>");
									//xmldetail2hdr.append("<tran_id/>");
									//valueXmlString.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
									System.out.println("value of line_no_dist_order no >>>"+rs.getInt("LINE_NO")+"{}>>"+rs.getString("LINE_NO"));
									//valueXmlString.append("<line_no_dist_order><![CDATA["+rs.getInt("LINE_NO")+"]]></line_no_dist_order>");

									valueXmlString.append("<line_no_dist_order protect=\"0\">").append("<![CDATA[" + lineNo + "]]>").append("</line_no_dist_order>");
									setNodeValue( dom, "line_no_dist_order", getAbsString(""+lineNo));
									
									
									mLineNoDist = rs.getInt("LINE_NO");
									unit = rs.getString("UNIT");
									unitAlt = rs.getString("UNIT__ALT");
									itemCode = rs.getString("ITEM_CODE");
									qtyConfirm = rs.getDouble("QTY_CONFIRM");
									qtyShipped = rs.getDouble("QTY_SHIPPED");
									discount =	rs.getDouble("DISCOUNT");
									netAmt =	rs.getDouble("NET_AMT");
									taxAmt =	rs.getDouble("TAX_AMT");
									remQuantity = qtyConfirm - qtyShipped;
									System.out.println("calling getDetails");
									valueXmlString.append(getDetails(siteCode,mLineNoDist,distOrder,tranType,conn));
									System.out.println("calling getDetails exit>>>>>");

									if (tranType != null && tranType.trim().length() > 0)
									{
										System.out.println("tranType != null && tranType.trim().length() > 0");
										sql = "SELECT CHECK_INTEGRAL_QTY, TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE = '"+tranType+"' ";

										pstmt1   = conn.prepareStatement(sql);
										//pstmt1.setString(1,tranType);
										rs1 = pstmt1.executeQuery();
										if (rs1.next())
										{
											//System.out.println( "CHECK_INTEGRAL_QTY :" + rs.getString( 1 ) );
											checkIntegralQty = rs1.getString( 1 );
											tranTypeParent = rs1.getString( 2 );
											//System.out.println( "tranTypeParent : " + tranTypePparent );
											if (checkIntegralQty == null || checkIntegralQty.trim().length() == 0)
											{
												checkIntegralQty = "Y";
											}
										}
										// added 18/06/09 manoharan
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
										// end added 18/06/09 manoharan
									}
									if (!tranType.equals(tranTypeParent))
									{
										System.out.println("!tranType.equals(tranTypeParent)");
									}
									else
									{
										System.out.println("!tranType.equals(tranTypeParent else)");
									}
									//availableYn ="Y"; // 28/05/09 manoharan commented taken from distorder table
									sql =  " SELECT (CASE WHEN ACTIVE IS NULL THEN 'Y' ELSE ACTIVE END) ACT, MIN_SHELF_LIFE, "
											+ " (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) TRK_SHELF_LIFE, "
											+ " (CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END) SUP_SOUR, DESCR, "
											+ " (case when shelf_life_type is null then 'E' else shelf_life_type end) "
											+ " FROM ITEM WHERE ITEM_CODE = '"+itemCode+"' ";
									pstmt1= conn.prepareStatement(sql);
									rs1 = pstmt1.executeQuery();
									if ( rs1.next() )
									{
										active = rs1.getString( 1 );
										minShelfLife = rs1.getInt( 2 );
										itemDescr = rs1.getString( 5 );
										shelflifetype = rs1.getString( 6 );
										if( active.equals("N") )
										{
											System.out.println("VTITEM4 error through");
											errCode = "VTITEM4";
											errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
											return errString;
										}
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null ;
									
									//valueXmlString.append("<shelf_life_type><![CDATA["+shelflifetype+"]]></shelf_life_type>");
									valueXmlString.append("<shelf_life_type protect=\"0\">").append("<![CDATA[" + shelflifetype + "]]>").append("</shelf_life_type>");
									setNodeValue( dom, "shelf_life_type", getAbsString(""+shelflifetype));
									
									
									valueXmlString.append("<discount protect=\"0\">").append("<![CDATA[" + discount + "]]>").append("</discount>");
									setNodeValue( dom, "discount", getAbsString(""+discount));
									
									valueXmlString.append("<net_amt protect=\"0\">").append("<![CDATA[" + netAmt + "]]>").append("</net_amt>");
									setNodeValue( dom, "net_amt", getAbsString(""+netAmt));
									
									valueXmlString.append("<tax_amt protect=\"0\">").append("<![CDATA[" + taxAmt + "]]>").append("</tax_amt>");
									setNodeValue( dom, "tax_amt", getAbsString(""+taxAmt));
									
									sql = " SELECT LOC_CODE__DAMAGED, SUNDRY_CODE, PRICE_LIST, PRICE_LIST__CLG, SITE_CODE__SHIP "
											+ " FROM DISTORDER WHERE DIST_ORDER = '"+distOrder+"' ";
									pstmt1= conn.prepareStatement(sql);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										siteCodeShip = rs1.getString("SITE_CODE__SHIP");

										locCodeDamaged = rs1.getString("LOC_CODE__DAMAGED");
										//System.out.println("locCodeDamaged :"+locCodeDamaged);
										sundryCode = rs1.getString( 2 );
										priceList = rs1.getString( 3 );
										//System.out.println("priceList :" + priceList);

										priceListClg = rs1.getString( 4 );
										//System.out.println( "priceListClg :" + priceListClg );

										if (locCodeDamaged == null)
										{
											locCodeDamaged = "";
										}
										if (locCodeDamaged != null && locCodeDamaged.trim().length() > 0)
										{
											StringTokenizer st = new StringTokenizer(locCodeDamaged,",");
											res = ""; //  28/05/09 manoharan
											while (st.hasMoreTokens())
											{
												res = res + "'" + st.nextToken() + "',";
											}
											res = res.substring(0,res.length()-1);
											//System.out.println("res ::" + res);
											locCodeDamaged = res;
											//System.out.println("locCodeDamaged After String Tockenized ::"+locCodeDamaged);
										}
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null ;
									boolean isRecordFound = false;



									sql = "SELECT A.LOT_NO, A.LOT_SL, A.QUANTITY, A.EXP_DATE, A.UNIT, A.ITEM_SER, "
											+"A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
											+"A.PACK_CODE, A.LOC_CODE, A.BATCH_NO, A.GRADE , "
											+"A.GROSS_WEIGHT, A.TARE_WEIGHT, A.NET_WEIGHT, A.DIMENSION, A.RETEST_DATE, "
											+"A.SUPP_CODE__MFG, A.PACK_INSTR,A.RATE,C.DESCR "
											+"FROM STOCK A, INVSTAT B, LOCATION C "
											+"WHERE C.INV_STAT = B.INV_STAT "
											+"AND A.LOC_CODE = C.LOC_CODE "
											+"AND A.ITEM_CODE = '"+itemCode+"'  "
											+"AND A.SITE_CODE = '"+siteCodeShip+"'  "
											+"AND B.AVAILABLE = '"+availableYn+"'  "
											+"AND B.USABLE = '"+availableYn+"' "
											+"AND B.STAT_TYPE <> 'S' "
											+""+subSQL+""
											+" AND A.QUANTITY - A.ALLOC_QTY > 0 ";

									if( availableYn != null && availableYn.equals("Y") )
									{
										sql = sql + " AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
												+"WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";
									}
									if( locCodeDamaged != null && locCodeDamaged.trim().length() > 0 )
									{
										sql = sql + "AND A.LOC_CODE IN (" + locCodeDamaged + ")";
									}
									sql =  sql + " ORDER BY CASE WHEN A.EXP_DATE IS NULL THEN A.CREA_DATE ELSE A.EXP_DATE END,A.CREA_DATE,A.LOT_NO, A.LOT_SL ";
									pstmt1= conn.prepareStatement(sql);
									rs1 = pstmt1.executeQuery();

									System.out.println("Resetting detail2xml----"+detail2xml);
									System.out.println("ItemCode----"+itemCode);

									while (rs1.next())
										//changed by msalam on 180609 for stopping processing only 999 rows. end
									{
										System.out.println( "inside while........................" );
										isRecordFound = true;
										lotNo = rs1.getString(1);
										lotSl = rs1.getString(2);
										packCode = rs1.getString(11);
										/*if (remQuantity == 0)
										{
											break;
										}*/
										System.out.println( "inside while........................>>1" );
										// 11/09/09 manoharan if in stock there is invalid site_code__mfg then skip the item 
										siteCodeMfg = rs1.getString(7);
										if (siteCodeMfg != null && siteCodeMfg.trim().length() > 0)
										{
											sql2 = "SELECT COUNT(*) FROM SITE "
													+ "WHERE SITE_CODE = ?";
											pstmt2= conn.prepareStatement(sql2);
											pstmt2.setString(1,siteCodeMfg);
											rs2 = pstmt2.executeQuery();
											if (rs2.next())
											{
												count = rs2.getInt(1);
											}
											rs2.close();
											rs2 = null;
											pstmt2.close();
											pstmt2 = null;

											System.out.println("count :" + count);
											if (count == 0)
											{
												System.out.println("count >>>>>:" + count);
												continue;
											}

										} 
										// end 11/09/09 manoharan if in stock there is invalid site_code__mfg then skip the item

										stockQty = rs1.getDouble(3) - rs1.getDouble(10);
										//System.out.println("stockQty :" + stockQty);
										if (availableYn.equals("Y"))
										{
											if (minShelfLife > 0)
											{
												chkDate = calcExpiry(tranDate,minShelfLife); //calcExpiry function to be checked.
												//System.out.println("chkDate :" + chkDate);
												chkDate1 = sdf.parse(chkDate);
												java.sql.Date date1 = rs1.getDate(4);
												//System.out.println("date1 :" + date1);
												java.util.Date date2 = null;
												if(date1 != null)
												{
													date2 = new java.util.Date(date1.getTime());
													//System.out.println("chkDate1 :" + chkDate1);
													//System.out.println("date2 :" + date2);
													if((chkDate1.compareTo(date2) > 0))
													{
														System.out.println("count :>c>c>c>c>C" + count);
														continue;
													}
												}
											}
										}
										if (!checkIntegralQty.equals("N"))
										{
											//integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode,conn );
											integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode, checkIntegralQty );
											//System.out.println("integralQty :"+integralQty);
											if (integralQty <= 0)
											{
												errCode = "VINTGRLQTY";
												errString = itmDBAccessEJB.getErrorString( "", errCode, "", "", conn);
												//System.out.println("errString:" + errString + ":");
												return errString;
											}
										}
										if (stockQty >= remQuantity)
										{
											if (checkIntegralQty.equals("Y"))
											{
												remQuantity = remQuantity - (remQuantity % integralQty);
												//System.out.println("remQuantity :"+remQuantity);
											}
											minputQty = remQuantity;
											remQuantity = 0;
										}
										else if (stockQty < remQuantity)
										{
											if (checkIntegralQty.equals("Y"))
											{
												stockQty = stockQty - (stockQty % integralQty);
											}
											minputQty = stockQty;
											remQuantity = remQuantity - stockQty;
										}
										/*if (minputQty == 0)
										{
											System.out.println("count :ldfdfdfdf" + count);
											continue;
										}*/
										rate1 = rate;
										if (rate1.equals(""))
										{
											rate1 = "0";
										}
										System.out.println("rate1----"+rate1+"---priceList----"+priceList);
										if (Double.parseDouble(rate1) == 0)
										{
											if( priceList != null && priceList.trim().length() > 0 )
											{
												sql2 = "SELECT COUNT(*) FROM PRICELIST "
														+ "WHERE PRICE_LIST = '"+priceList+"'  AND LIST_TYPE = 'I' ";
												pstmt2= conn.prepareStatement(sql2);
												rs2 = pstmt2.executeQuery();
												if (rs2.next())
												{
													count = rs2.getInt(1);
												}
												rs2.close();
												rs2 = null;
												pstmt2.close();
												pstmt2 = null;

												//System.out.println("count :" + count);
												if (count == 0)
												{
													rate2 = disCommon.pickRate(priceList, tranDate, itemCode, rs1.getString(1),"D",conn);
													//System.out.println("rate2 :" + rate2);
												}
												else
												{
													tabValue = siteCode + "~t" + rs1.getString(12) + "~t" + rs1.getString(1) + "~t";
													System.out.println("printing tabValue----"+tabValue);
													//System.out.println("tabValue :" + tabValue);
													rate2 = disCommon.pickRate(priceList, tranDate, itemCode, tabValue, "I",conn);
													//System.out.println("rate2 :" + rate2);
												}
											}
											rate1 = Double.toString(rate2);
										}
									//commented by ritesh on 17/SEP/2014 as per instruction by manoj start
//									rateClg = null;
//									if (rateClg == null || rateClg.equals("") || Double.parseDouble(rateClg) == 0)
//									{
//										if (priceListClg != null && priceListClg.trim().length() > 0 )
//										{
//											rateClgVal = disCommon.pickRate(priceListClg, tranDate, itemCode, rs1.getString(1),"D",conn);
//											//System.out.println("rateClgVal :"+rateClgVal);
//										}
//										if (rateClgVal <= 0)
//										{
//											rateClgVal = rate2;
//											//System.out.println("rateClgVal :"+rateClgVal);
//										}
//										rateClg = Double.toString(rateClgVal);
//									}
									//commented by ritesh  end
										if (Double.parseDouble(rs1.getString(3)) > 0)
										{
											grossPer    = rs1.getDouble(15) / rs1.getDouble(3);
											//System.out.println("grossPer :"+grossPer);
											netPer 	    = rs1.getDouble(17) 	/ rs1.getDouble(3);
											//System.out.println("netPer :"+netPer);
											tarePer 	= rs1.getDouble(16) / rs1.getDouble(3);
											//System.out.println("tarePer :"+tarePer);
											grossWt = minputQty * grossPer;
											//System.out.println("grossWt :"+grossWt);
											netWt   = minputQty * netPer;
											//System.out.println("netWt :"+netWt);
											tareWt  = minputQty * tarePer;
											//System.out.println("tareWt :"+tareWt);
										}
										disAmount = (amount * ( discount / 100));
										//if( sundryCode != null && sundryCode.trim().length() > 0 )
										//{
										noArt1 = 0;
										//noArt1 = disCommon.getNoArt(siteCode, sundryCode, itemCode, packCode,minputQty, 'B', shipperQty, integralQty,conn);
										noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode,minputQty, 'B', shipperQty, integralQty);
										//System.out.println("noArt1 :"+noArt1);
										noArt = "" + noArt1;
										System.out.println("%^%^%^%^%^%NoArt is&***&*&*&*&["+noArt+"]");
										//}
										//minputQty
										double shipperSize=0,shipQty=0,noArt11=0,remainder=0;
										double integralqty=0;
										double noArt12=0,acShipperQty=0,acIntegralQty=0;
										Statement stmt1 = conn.createStatement();
										sql ="select (case when shipper_size is null then 0 else shipper_size end) shipper_size"
												+" from item_lot_packsize where item_code = '"+itemCode+"'"
												+" and  '"+lotNo+"' >= lot_no__from "
												+" and  '"+lotNo+"'  <= lot_no__to ";
										System.out.println("sql :"+sql);
										rs3 = stmt1.executeQuery(sql);
										if (rs3.next())
										{
											shipperSize = rs3.getDouble(1);
										}
										System.out.println("shipperSize .............:"+shipperSize);	
										System.out.println("minputQty .............:"+minputQty);	
										if( shipperSize > 0)
										{
											shipQty = shipperSize;
											noArt11 = (minputQty - (minputQty % shipQty))/shipQty;
											System.out.println("noArt11 .............:"+noArt11);
											remainder = minputQty % shipQty;
											System.out.println("remainder .............:"+remainder);
											sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
													+" from customeritem where cust_code = '"+sundryCode+"' and item_code ='"+itemCode+"'";
											System.out.println("sql :"+sql);
											rs3 = stmt1.executeQuery(sql);
											if (rs3.next())
											{
												integralqty = rs3.getDouble(1);

											}
											System.out.println("integralqty .............:"+integralqty);
											if(integralqty ==0)
											{
												sql ="select  ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
														+" from siteitem where site_code = '"+siteCode+"' and item_code ='"+itemCode+"'";
												System.out.println("sql :"+sql);
												rs3 = stmt1.executeQuery(sql);
												if (rs3.next())
												{
													integralqty = rs3.getDouble(1);

												}
												if(integralqty ==0)
												{
													sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
															+" from item where item_code ='"+itemCode+"'";
													System.out.println("sql :"+sql);
													rs3 = stmt1.executeQuery(sql);
													if (rs3.next())
													{
														integralqty = rs3.getDouble(1);
														//System.out.println("integralqty .............:"+integralqty);
													}
												}

											} 
											System.out.println("integralqty .............:"+integralqty);
											if(integralqty > 0)
											{
												noArt12 = (remainder -(remainder % integralqty))/integralqty;
												//System.out.println("noArt12 ....2.........:"+noArt12);
											}
											if(noArt12 > 0)
											{
												noArt12 =1;
												//System.out.println("noArt2 ....0.........:"+noArt12);
											}
											noArt1			= (int)(noArt11 + noArt12);
											noArt = "" + noArt1;
											System.out.println("noArt .............:"+noArt);
											acShipperQty	= shipQty;
											acIntegralQty	= integralqty;
										}
										if(shipperSize ==0)
										{
											noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode, minputQty, 'B', acShipperQty, acIntegralQty);
											noArt = "" + noArt1;
											//System.out.println("noArt .............:"+noArt);
										}

										lcFact =0;
										ArrayList QtyFactorList = new ArrayList();

										QtyFactorList =	disCommon.convQtyFactor(unitAlt, unit, itemCode, minputQty, lcFact,conn);
										lcQtyOrderAlt =	((Double)QtyFactorList.get(1)).doubleValue() ;
										lcFact 	=	((Double)QtyFactorList.get(0)).doubleValue() ;
										QtyFactorList = null;

										
									 
									    //valueXmlString.append("<unit>").append("<![CDATA[").append(rs1.getString(5)).append("]]>").append("</unit>\r\n");
										valueXmlString.append("<unit protect=\"0\">").append("<![CDATA[" + rs1.getString(5) == null ? "":rs1.getString(5) + "]]>").append("</unit>");
										setNodeValue( dom, "unit", getAbsString(""+rs1.getString(5) == null ? "":rs1.getString(5)));
									    
									    //valueXmlString.append("<unit__alt>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>\r\n");
									    valueXmlString.append("<unit__alt protect=\"0\">").append("<![CDATA[" +unitAlt + "]]>").append("</unit__alt>");
										setNodeValue( dom, "unit__alt", getAbsString(""+unitAlt));
									    
									    
									    //valueXmlString.append("<conv__qty__alt>").append("<![CDATA[").append(lcFact).append("]]>").append("</conv__qty__alt>\r\n");
										valueXmlString.append("<conv__qty__alt protect=\"0\">").append("<![CDATA[" + lcFact + "]]>").append("</conv__qty__alt>");
										setNodeValue( dom, "conv__qty__alt", getAbsString(""+lcFact));
									    
									    ratefd	= Double.parseDouble(rateFmDistOrd);
										ratealt = lcFact * ratefd;
										
										//valueXmlString.append("<rate__alt>").append("<![CDATA[").append(ratealt).append("]]>").append("</rate__alt>\r\n");
										valueXmlString.append("<rate__alt protect=\"0\">").append("<![CDATA[" + ratealt + "]]>").append("</rate__alt>");
										setNodeValue( dom, "rate__alt", getAbsString(""+ratealt));
									    
										
										//valueXmlString.append("<conv__rate_alt>").append("<![CDATA[").append(lcFact).append("]]>").append("</conv__rate_alt>\r\n");
										valueXmlString.append("<conv__rate_alt protect=\"0\">").append("<![CDATA[" + lcFact + "]]>").append("</conv__rate_alt>");
										setNodeValue( dom, "conv__rate_alt", getAbsString(""+lcFact));
									    
										//valueXmlString.append("<qty_order__alt>").append("<![CDATA[").append(0).append("]]>").append("</qty_order__alt>\r\n");
										/*valueXmlString.append("<qty_order__alt protect=\"0\">").append("<![CDATA[" + rs1.getString(5) == null ? "":rs1.getString(5) + "]]>").append("</qty_order__alt>");
										setNodeValue( dom, "qty_order__alt", getAbsString(""+rs1.getString(5) == null ? "":rs1.getString(5)));
									    */
										
										//valueXmlString.append("<qty_details>").append("<![CDATA[").append((rs1.getString("qty_details") == null) ? "":rs1.getString("qty_details")).append("]]>").append("</qty_details>\r\n");
										
										
										String tLocCode = null;
										tLocCode = rs1.getString(12);
									//	valueXmlString.append("<loc_code>").append("<![CDATA[").append( (tLocCode == null ? "" : tLocCode.trim()) ).append("]]>").append("</loc_code>\r\n");
										//commented for rajendra on 04/09/08 for pick up rate from stock
										//xmldetail2stock.append("<rate>").append("<![CDATA[").append(ratefromStock).append("]]>").append("</rate>\r\n");
									    
										//valueXmlString.append("<rate>").append("<![CDATA[").append(rateFmDistOrd).append("]]>").append("</rate>\r\n"); // CHANGED BY RITESH  ON 18/SEP/2014
										valueXmlString.append("<rate protect=\"0\">").append("<![CDATA[" + rateFmDistOrd + "]]>").append("</rate>");
										setNodeValue( dom, "rate", getAbsString(""+rateFmDistOrd));
									    
										//valueXmlString.append("<rate__clg>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");  // CHANGED BY RITESH  ON 17/SEP/2014
										valueXmlString.append("<rate__clg protect=\"0\">").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
										setNodeValue( dom, "rate__clg", getAbsString(""+rateClg));
										
										//	valueXmlString.append("<quantity>").append("<![CDATA[").append(rs1.getDouble("quantity")).append("]]>").append("</quantity>\r\n");
										//valueXmlString.append("<amount>").append("<![CDATA[").append(minputQty*Double.parseDouble(rate1)).append("]]>").append("</amount>\r\n");
										valueXmlString.append("<amount protect=\"0\">").append("<![CDATA[" + minputQty*Double.parseDouble(rate1) + "]]>").append("</amount>");
										setNodeValue( dom, "amount", getAbsString(""+minputQty*Double.parseDouble(rate1)));
										
										String tLotSl = null;
										tLotSl = rs1.getString(2);
										
									//	valueXmlString.append("<lot_sl>").append("<![CDATA[").append( ( tLotSl == null ? "    " : tLotSl) ).append("]]>").append("</lot_sl>\r\n");
										
										//valueXmlString.append("<pack_code>").append("<![CDATA[").append((rs1.getString(11) == null) ? "":rs1.getString(11)).append("]]>").append("</pack_code>\r\n");
										valueXmlString.append("<pack_code protect=\"0\">").append("<![CDATA[" + rs1.getString(11) == null ? "":rs1.getString(11) + "]]>").append("</pack_code>");
										setNodeValue( dom, "pack_code", getAbsString(""+rs1.getString(11) == null ? "":rs1.getString(11)));
										
										//valueXmlString.append("<disc_amt>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
										valueXmlString.append("<disc_amt protect=\"0\">").append("<![CDATA[" + disAmount + "]]>").append("</disc_amt>");
										setNodeValue( dom, "disc_amt", getAbsString(""+disAmount));
										
										
										//valueXmlString.append("<tax_class>").append("<![CDATA[").append( ( taxClass == null ? "": taxClass ) ).append("]]>").append("</tax_class>\r\n");
										//valueXmlString.append("<tax_chap>").append("<![CDATA[").append( ( taxChap == null ? "": taxChap ) ).append("]]>").append("</tax_chap>\r\n");
										//valueXmlString.append("<tax_env>").append("<![CDATA[").append( ( taxEnv == null ? "": taxEnv ) ).append("]]>").append("</tax_env>\r\n");
										grossWt = Double.parseDouble(getFormatedValue(grossWt,3));
										//System.out.println("[DistIssueActEJB] Gross Wt=============>"+grossWt);
									//	valueXmlString.append("<gross_weight>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
										netWt = Double.parseDouble(getFormatedValue(netWt,3));
										//System.out.println("[DistIssueActEJB] Net Wt=============>"+netWt);
										
										//valueXmlString.append("<net_weight>").append("<![CDATA[").append(netWt).append("]]>").append("</net_weight>\r\n");
										/*valueXmlString.append("<net_weight protect=\"0\">").append("<![CDATA[" + netWt + "]]>").append("</net_weight>");
										setNodeValue( dom, "net_weight", getAbsString(""+netWt));
										*/
										
										tareWt = Double.parseDouble(getFormatedValue(netWt,3));
										//System.out.println("[DistIssueActEJB] Tare Wt=============>"+tareWt);
										
										//valueXmlString.append("<tare_weight>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
										/*valueXmlString.append("<tare_weight protect=\"0\">").append("<![CDATA[" + tareWt + "]]>").append("</tare_weight>");
										setNodeValue( dom, "tare_weight", getAbsString(""+tareWt));
										*/
										//valueXmlString.append("<pack_instr>").append("<![CDATA[").append((rs1.getString(21) == null) ? "":rs1.getString(21)).append("]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
										valueXmlString.append("<pack_instr protect=\"0\">").append("<![CDATA[" + rs1.getString(21) == null ? "":rs1.getString(21) + "]]>").append("</pack_instr>");
										setNodeValue( dom, "pack_instr", getAbsString(""+rs1.getString(21) == null ? "":rs1.getString(21)));
										
								//		select RETEST_DATE,MFG_DATE,EXP_DATE from stock where lot_sl = '12S'
										lotSl = genericUtility.getColumnValue("lot_sl",dom);
										sql = "select RETEST_DATE,MFG_DATE,EXP_DATE from stock where lot_sl = ?";
										pstmt3=conn.prepareStatement(sql);
										pstmt3.setString(1,lotSl);
										rsp = pstmt3.executeQuery();
										if(rsp.next())
										{

											String resetDate = rsp.getDate(1) == null ? "":sdf.format(rsp.getDate(1));
											
											if(resetDate != null)
											{
												valueXmlString.append("<retest_date protect=\"0\">").append("<![CDATA[" + resetDate + "]]>").append("</retest_date>");
												setNodeValue( dom, "retest_date", getAbsString(""+resetDate));

											}
											String mfgDate = rsp.getDate(2) == null ? "":sdf.format(rsp.getDate(2));
											
											if(mfgDate != null)
											{
												valueXmlString.append("<mfg_date protect=\"0\">").append("<![CDATA[" + mfgDate + "]]>").append("</mfg_date>");
												setNodeValue( dom, "mfg_date", getAbsString(""+mfgDate));

											}

											String expDate = rsp.getDate(3) == null ? "":sdf.format(rsp.getDate(3));
											
											if(expDate != null)
											{
												valueXmlString.append("<exp_date protect=\"0\">").append("<![CDATA[" + expDate + "]]>").append("</exp_date>");
												setNodeValue( dom, "exp_date", getAbsString(""+expDate));

											}

										}
										pstmt3.close();
										rsp.close();
										pstmt3 = null;
										rsp = null;
										
										
										//valueXmlString.append("<dimension>").append("<![CDATA[").append((rs1.getString(18) == null) ? "":rs1.getString(18)).append("]]>").append("</dimension>\r\n");
										valueXmlString.append("<dimension protect=\"0\">").append("<![CDATA[" + rs1.getString(18) == null ? "":rs1.getString(18) + "]]>").append("</dimension>");
										setNodeValue( dom, "dimension", getAbsString(""+rs1.getString(18) == null ? "":rs1.getString(18)));
										
										System.out.println("supp_code__mfg>>>>>>>>DD>>>>ass"+rs1.getString(20) == null ? "":rs1.getString(20));
										
										//valueXmlString.append("<supp_code__mfg>").append("<![CDATA[").append((rs1.getString(20) == null) ? "":rs1.getString(20)).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
										valueXmlString.append("<supp_code__mfg protect=\"0\">").append("<![CDATA[" + rs1.getString(20) == null ? "":rs1.getString(20) + "]]>").append("</supp_code__mfg>");
										setNodeValue( dom, "supp_code__mfg", getAbsString(""+rs1.getString(20) == null ? "":rs1.getString(20)));
										
										
										System.out.println("site_code__mfg>>>>>>DDFF>>>>>>ass"+siteCodeDlv+" F>>>>>>>>"+rs1.getString("site_code__mfg") == null ? "":rs1.getString("site_code__mfg"));
									    
										//valueXmlString.append("<cost_rate>").append("<![CDATA["+rs1.getDouble("rate")+"]]>").append("</cost_rate>\r\n"); // CHANGED BY RITESH  ON 18/SEP/2014
										/*valueXmlString.append("<cost_rate protect=\"0\">").append("<![CDATA[" + rs1.getDouble("rate") + "]]>").append("</cost_rate>");
										setNodeValue( dom, "cost_rate", getAbsString(""+rs1.getDouble("rate")));
										*/
									    
										valueXmlString.append("<cost_rate protect=\"0\">").append("<![CDATA[0]]>").append("</cost_rate>");
										setNodeValue( dom, "cost_rate", getAbsString(""+0));
										
									    //valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
										valueXmlString.append("<site_code__mfg protect=\"0\">").append("<![CDATA[" + siteCodeMfg + "]]>").append("</site_code__mfg>");
										setNodeValue( dom, "site_code__mfg", getAbsString(""+siteCodeMfg));
										
									    
									    //valueXmlString.append("<mfg_date>").append("<![CDATA[").append((rs1.getDate(8) == null) ? "":sdf.format(rs1.getDate(8))).append("]]>").append("</mfg_date>\r\n");
									   /* valueXmlString.append("<mfg_date protect=\"0\">").append("<![CDATA[" + rs1.getDate(8) == null ? "":sdf.format(rs1.getDate(8)) + "]]>").append("</mfg_date>");
										setNodeValue( dom, "mfg_date", getAbsString(""+rs1.getDate(8) == null ? "":sdf.format(rs1.getDate(8))));
										
									    //valueXmlString.append("<exp_date>").append("<![CDATA[").append((rs1.getDate(4) == null) ? "":sdf.format(rs1.getDate(4))).append("]]>").append("</exp_date>\r\n");
									    valueXmlString.append("<exp_date protect=\"0\">").append("<![CDATA[" + rs1.getDate(4) == null ? "":sdf.format(rs1.getDate(4)) + "]]>").append("</exp_date>");
										setNodeValue( dom, "exp_date", getAbsString(""+rs1.getDate(4) == null ? "":sdf.format(rs1.getDate(4))));
										*/
									    
									   // valueXmlString.append("<potency_perc>").append("<![CDATA[").append( ( (rs1.getString(9) == null) ? "": rs1.getString(9) ) ).append("]]>").append("</potency_perc>\r\n");
									    valueXmlString.append("<potency_perc protect=\"0\">").append("<![CDATA[" + rs1.getString(9) == null ? "": rs1.getString(9) + "]]>").append("</potency_perc>");
										setNodeValue( dom, "potency_perc", getAbsString(""+rs1.getString(9) == null ? "": rs1.getString(9)));
										
									    //valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
										System.out.println("no_artno_artno_artno_art"+noArt);
									    /*valueXmlString.append("<no_art protect=\"0\">").append("<![CDATA[" + noArt + "]]>").append("</no_art>");
										setNodeValue( dom, "no_art", getAbsString(""+noArt));*/
									    
									    //valueXmlString.append("<batch_no>").append("<![CDATA[").append( ( (rs1.getString(13) == null) ? "":rs1.getString(13) ) ).append("]]>").append("</batch_no>\r\n");
									    valueXmlString.append("<batch_no protect=\"0\">").append("<![CDATA[" + rs1.getString(13) == null ? "":rs1.getString(13) + "]]>").append("</batch_no>");
										setNodeValue( dom, "batch_no", getAbsString(""+rs1.getString(13) == null ? "":rs1.getString(13)));
									    
									    //valueXmlString.append("<grade>").append("<![CDATA[").append( ( (rs1.getString(14) == null) ? "": rs1.getString(14) ) ).append("]]>").append("</grade>\r\n");
										valueXmlString.append("<grade protect=\"0\">").append("<![CDATA[" + rs1.getString(14) == null ? "":rs1.getString(14) + "]]>").append("</grade>");
										setNodeValue( dom, "grade", getAbsString(""+rs1.getString(14) == null ? "":rs1.getString(14)));
									    
									    
									    //	valueXmlString.append("<lot_no>").append("<![CDATA[").append(( (rs1.getString(1) == null) ? "               ": rs1.getString(1))).append("]]>").append("</lot_no>\r\n");

										/*	detail2stock = xmldetail2stock.toString();
										xmldetail2stock = null;

										valueXmlString.append(xmldetail2hdr.toString());
										valueXmlString.append(detail2stock);
										valueXmlString.append(xmldetail2ftr.toString());*/
										System.out.println("xmlString detail2......" + valueXmlString.toString());
										noArt1 = 0;
										grossWt = 0;
										tareWt = 0;
										netWt = 0;
										//cnt++;
										//System.out.println("The cnt :" + ++cnt);
									}//while end
									// added 18/06/09 manoharan
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									sql = null;
									// end added 18/06/09 manoharan
									if ( isRecordFound == false )
									{

										//System.out.println("record not found.....................");
										//errCode = "VTDIST16";
										//errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
										//return errString;
									}
									//}
								//	valueXmlString.append("</Detail3>");
								
	}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						/*		
						 */
						
						currDomStr = currDomStr.replace("</Detail3>", valueXmlString.toString() + "</Detail3>");
						System.out.println("after currDomStr[" + currDomStr + "]");
						valueXmlString.append(currDomStr);
					}
					


					System.out.println(">>>>END>>>>>>>>>");
				}


				break;

			}

			if(("lot_sl".equalsIgnoreCase(currentColumn)))
			{
				System.out.println("CHK VAL");
				String currDomStr = genericUtility.serializeDom(dom);
				System.out.println("currDomStr[" + currDomStr + "]");
				StringBuffer valueXmlStr = new StringBuffer(currDomStr);
				System.out.println("@@@@@@@@@@@ after serialize : valueXmlStr ["+valueXmlStr+"]");
				StringBuffer valueXmlString1 = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
				valueXmlString1.append(editFlag).append("</editFlag></header>");
				valueXmlString1.append(valueXmlStr);
				valueXmlString = valueXmlString1;
			}

			valueXmlString.append("</Root>"); 
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
			throw new ITMException(e); 
		}
		finally 
		{
			try
			{
				if(conn != null)
				{
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
					conn.close(); 
				}
				conn = null;
			}
			catch(Exception d)
			{
				d.printStackTrace(); 
			}
		}
		return valueXmlString.toString();
	}

	private ArrayList<String> getInvHoldQCLock(Map<String,String> invHoldMap,Connection conn) throws ITMException
	{
		System.out.println("----in getInvHoldQuantity method------");
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		String sql="";
		ArrayList<String> lockListL=new ArrayList<String>();
		
		try{			
			sql="select lock_code from inv_hold h,inv_hold_det d where h.tran_id = d.tran_id " 
					+"and d.item_code = ? and d.site_code = ? "
					+"and d.loc_code = ? and d.lot_no= ? AND h.confirmed= ? "
					+"and d.lot_sl= ? and d.hold_status = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, invHoldMap.get("item_code"));
			pstmt.setString(2, invHoldMap.get("site_code"));
			pstmt.setString(3, invHoldMap.get("loc_code"));
			pstmt.setString(4, invHoldMap.get("lot_no"));
			pstmt.setString(5, "Y");
			pstmt.setString(6, invHoldMap.get("lot_sl"));
			pstmt.setString(7, "H");
			
			rs=pstmt.executeQuery();
			while(rs.next()){
				lockListL.add(rs.getString(1) == null ? "":rs.getString(1).trim());				
			}
			if(rs!=null){
			rs.close();
			rs=null;
			}
			if(pstmt!=null){
			pstmt.close();
			pstmt=null;
			}
			
			
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}		
		
		return lockListL;
		
	}
	
	private String getColumnDescr(Connection conn, String columnName ,String tableName, String columnName2, String value) throws ITMException 
	{

			PreparedStatement pstmt = null ;
			ResultSet rs = null ; 
			String sql = "";
			String findValue = "";
			try
			{			
				sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,value);
				rs = pstmt.executeQuery();
				if(rs.next())
				{					
					findValue = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	        
			}
			catch(Exception e)
			{
				System.out.println("Exception in getColumnDescr ");
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			}
			System.out.println("returning String from getColumnDescr " + findValue);
			return findValue;
		 
	}
	

	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}

	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
	private String getDetails(String mSiteCode,int mLineNoDist,String mDistOrder,String lsTranType,Connection conn)throws RemoteException,ITMException
	{//this method will return xml data
		String mItemCode = null,mTaxClass=null,mTaxChap=null,mTaxEnv = null;
		String lsUnitAlt = null,lsUnit = null,lsPackInstr =null;
		double mQty= 0,lcQty= 0,mRate = 0,mDiscount =0;

		String sql = null,lsTranTypeParent ="";
		ResultSet rs = null,rs1 =null;
		PreparedStatement pstmt = null,pstmt1 =null;
		StringBuffer detail2hdr = new StringBuffer("");
		try
		{
			sql="select item_code,((case when qty_confirm is null then 0 else qty_confirm end) - "
					+" (case when qty_shipped is null then 0 else qty_shipped end))	as qty,"
					+" ((case when qty_shipped is null then 0 else qty_shipped end) - "
					+" (case when qty_return is null then 0 else qty_return end)) as lcqty,"
					+" tax_class,tax_chap,tax_env,case when rate is null then 0 else rate end as rate,"
					+" case when discount is null then 0 else discount end as discount,	"
					+" rate__clg  ,UNIT__ALT ,UNIT,CONV__QTY__ALT,pack_instr "
					+" from 	distorder_det "
					+" where dist_order = '"+mDistOrder+"'  "
					+" and 	line_no    = "+mLineNoDist+"" 
					+ " AND   CASE WHEN STATUS IS NULL THEN 'O' ELSE STATUS END<>'C' ";//Added by manoj dtd 24/12/2013 to exclude closed line"

			//System.out.println("[DistIssueItemChangeEJB] sql=>"+sql);

			pstmt   = conn.prepareStatement(sql);
			//	pstmt.setString(1,mDistOrder);
			//pstmt.setInt(2,mLineNoDist);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mItemCode = rs.getString("item_code")==null?"":rs.getString("item_code");
				mQty = rs.getDouble("qty");
				lcQty = rs.getDouble("lcqty");
				mTaxClass = rs.getString("tax_class")==null?"":rs.getString("tax_class");
				mTaxChap = rs.getString("tax_chap")==null?"":rs.getString("tax_chap");
				mTaxEnv = rs.getString("tax_env")==null?"":rs.getString("tax_env");
				mRate = rs.getDouble("rate");
				mDiscount = rs.getDouble("discount");
				lsUnitAlt = rs.getString("UNIT__ALT")==null?"":rs.getString("UNIT__ALT");
				lsPackInstr = rs.getString("pack_instr")==null?"":rs.getString("pack_instr");

				if(lsUnitAlt.trim().length() == 0)
				{
					lsUnitAlt = lsUnit;
				}

			//	detail2hdr.append("<item_code><![CDATA["+mItemCode+"]]></item_code>");
				detail2hdr.append("<unit><![CDATA["+lsUnit+"]]></unit>");
				detail2hdr.append("<unit__alt><![CDATA["+lsUnitAlt+"]]></unit__alt>");
				detail2hdr.append("<pack_instr><![CDATA["+lsPackInstr+"]]></pack_instr>");


				sql=" select tran_type__parent "
						+" from	distorder_type where  tran_type = '"+lsTranType+"' ";
				//System.out.println("[DistIssueItemChangeEJB] sql=>"+sql);
				pstmt1= conn.prepareStatement(sql);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lsTranTypeParent = rs1.getString("tran_type__parent") == null ? "": rs1.getString("tran_type__parent").trim();
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				if(! lsTranTypeParent.equalsIgnoreCase(lsTranType.trim()))
				{
				//	detail2hdr.append("<quantity>"+lcQty+"</quantity>");
				//	detail2hdr.append("<qty_order__alt>"+0+"</qty_order__alt>");
					lcQty = lcQty;
				}
				else
				{
				//	detail2hdr.append("<quantity>"+mQty+"</quantity>");
				//	detail2hdr.append("<qty_order__alt>"+0+"</qty_order__alt>");
					lcQty = mQty;
				}
				detail2hdr.append("<tax_class><![CDATA["+mTaxClass+"]]></tax_class>");
				detail2hdr.append("<tax_chap><![CDATA["+mTaxChap+"]]></tax_chap>");
				detail2hdr.append("<tax_env><![CDATA["+mTaxEnv+"]]></tax_env>");
				detail2hdr.append("<rate>"+mRate+"</rate>");
				detail2hdr.append("<discount>"+mDiscount+"</discount>");
				//System.out.println("[CreateDistIssue] xml return ==>"+detail2hdr.toString());
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("chandni inside detail::"+detail2hdr.toString());
		return detail2hdr.toString();
	}

	private String calcExpiry(String tranDate, int months) throws ITMException
	{
		java.util.Date expDate = new java.util.Date();
		java.util.Date retDate = new java.util.Date();
		String retStrInDate = "";
		//System.out.println("tranDate :"+tranDate+"\nmonths :"+months);
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (months > 0)
			{
				Calendar  cal = Calendar.getInstance();
				expDate = sdf.parse(tranDate);
				//System.out.println("expDate :"+expDate);
				cal.setTime(expDate);
				cal.add(Calendar.MONTH,months);
				//for last day of the month
				cal.add(Calendar.MONTH,1);
				cal.set(Calendar.DATE,0);
				//sets zero to get the last day of the given date
				retDate = cal.getTime();
				retStrInDate = sdf.format(retDate);
			}
			else
			{
				retStrInDate = tranDate;
			}
		}
		catch(Exception e)
		{
			//System.out.println("The Exception occurs in calcExpiry :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		//System.out.println("retStrInDate :"+retStrInDate);
		return retStrInDate;
	}
	private double getIntegralQty(String siteCode, String itemCode, String lotNo, String packCode, String checkIntegralQty) throws ITMException
	{
		double integralQty = 0;
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		Statement stmt = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			char type = checkIntegralQty.charAt(0);
			//System.out.println("type==>"+type);
			switch (type)
			{
			case 'S':
				sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
						+"FROM ITEM_LOT_PACKSIZE "
						+"WHERE ITEM_CODE = '"+itemCode+"' "
						+"AND LOT_NO__FROM <= '"+lotNo+"' "
						+"AND LOT_NO__TO   >= '"+lotNo+"' ";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
							+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT REO_QTY FROM SITEITEM "
								+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);	
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						if (integralQty == 0)
						{
							sql = "SELECT REO_QTY FROM ITEM "
									+"WHERE ITEM_CODE = '"+itemCode+"'";
							System.out.println("sql :"+sql);	
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								integralQty = rs.getDouble(1);
								//System.out.println("integralQty :"+integralQty);
							}
						}
					}
				}

				break;
			case 'P':
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
						+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}

				break;
			case 'I':
				sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);	
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
				}

			}
			/* sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
				+"FROM ITEM_LOT_PACKSIZE "
				+"WHERE ITEM_CODE = '"+itemCode+"' "
				+"AND LOT_NO__FROM <= '"+lotNo+"' "
				+"AND LOT_NO__TO   >= '"+lotNo+"' ";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				integralQty = rs.getDouble(1);
				System.out.println("integralQty :"+integralQty);
			}
			if (integralQty == 0)
			{
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
					 +"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						 +"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);	
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							 integralQty = rs.getDouble(1);
							System.out.println("integralQty :"+integralQty);
						}
					}
				}
			} */
			conn.close();	
		}
		catch(Exception e)
		{
			System.out.println("the exception occurs in getIntegralQty :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("integralQty :"+integralQty);
		return integralQty;
	}

	private int getNoArt(String siteCode, String custCode, String itemCode, String packCode, double qty, char type, double shipperQty, double integralQty1) throws ITMException
	{
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		Statement stmt = null;
		double reoQty = 0d, capacity = 0d, integralQty = 0d, mod = 0d, noArt3 = 0d;
		double noArt = 0, noArt1 = 0, noArt2 = 0; 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			System.out.println("type :"+type);
			switch (type)
			{
			case 'S':
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END " 
						+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					capacity = rs.getDouble(1);
					//System.out.println("capacity :"+capacity);
				}
				else
				{
					capacity = 0;
				}
				sql = "SELECT REO_QTY FROM SITEITEM WHERE SITE_CODE = '"+siteCode+"' " 
						+"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					reoQty = rs.getDouble(1);
					//System.out.println("reoQty :"+reoQty);
				}
				if( reoQty == 0 )
				{
					sql = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						reoQty = rs.getDouble(1);
						//System.out.println("reoQty :"+reoQty);
					}
				}
				if (reoQty == 0)
				{
					reoQty = 0;
				}
				if (capacity > 0)
				{
					shipperQty = capacity;
					//System.out.println("shipperQty :"+shipperQty);
				}
				else
				{
					shipperQty = reoQty;
					//System.out.println("shipperQty :"+shipperQty);
				}
				System.out.println("shipperQty :"+shipperQty);
				if (shipperQty > 0)
				{
					mod = qty%shipperQty;
					System.out.println("mod :"+mod);
					noArt = (qty - mod) / shipperQty;
				}
				//System.out.println("noArt :"+noArt);
				break;
			case 'I':
				sql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM "
						+"WHERE CUST_CODE = '"+custCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
							+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
					}
				}
				if (integralQty > 0)
				{
					mod = qty%integralQty;
					System.out.println("mod :"+mod);
					noArt = (qty - mod) / integralQty;
					//System.out.println("noArt :"+noArt);
				}
				break;
			case 'B' :
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END " 
						+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					capacity = rs.getDouble(1);
					//System.out.println("capacity :"+capacity);
				}
				else
				{
					capacity = 0;
				}
				sql = "SELECT REO_QTY FROM SITEITEM WHERE SITE_CODE = '"+siteCode+"' " 
						+"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					reoQty = rs.getDouble(1);
					//System.out.println("reoQty :"+reoQty);
				}
				if( reoQty == 0 )
				{
					sql = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						reoQty = rs.getDouble(1);
						//System.out.println("reoQty :"+reoQty);
					}
				}
				if (capacity > 0)
				{
					shipperQty = capacity;
					//System.out.println("shipperQty :"+shipperQty);
				}
				else
				{
					shipperQty = reoQty;
					//System.out.println("shipperQty :"+shipperQty);
				}
				if (shipperQty > 0)
				{
					mod = (qty % shipperQty);
					noArt1 = (qty - mod) / shipperQty;
				}
				sql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM "
						+"WHERE CUST_CODE = '"+custCode+"' "
						+"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql ="SELECT INTEGRAL_QTY FROM SITEITEM "
							+"WHERE SITE_CODE = '"+siteCode+"' " 
							+"AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					if(integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
					}
				}
				double remainder1 = 0d;
				if (integralQty > 0)
				{
					remainder1 = mod % integralQty;
					System.out.println("remainder1 :"+remainder1);
					noArt3 =(mod - remainder1) / integralQty;
					noArt2 = (int)noArt3;
				}
				if (noArt2 > 0)
				{
					noArt2 = 1;
				}
				noArt  = noArt1 + noArt2;
				System.out.println("noArt :"+noArt);
			}
			conn.close();
			if (noArt == 0)
			{
				noArt = 0;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception occures in getNoArt :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("(int)noArt :"+(int)noArt);
		return (int)noArt;
	}
	public String getFormatedValue(double actVal,int prec)throws RemoteException//This method is added by nisar on 11/23/2007
	{//this method is used to return double with appropriate precison
		NumberFormat numberFormat = NumberFormat.getIntegerInstance ();
		Double DoubleValue = new Double (actVal);
		numberFormat.setMaximumFractionDigits(prec);
		String strValue = numberFormat.format(DoubleValue);
		//System.out.println(strValue);
		strValue = strValue.replaceAll(",","");
		return strValue;
	}
	
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	
	private String getCurrdateAppFormat()
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//System.out.println(genericUtility.getDBDateFormat());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		}
		catch(Exception exception)
		{
			//System.out.println("Exception in [MPSOrder] getCurrdateAppFormat " + exception.getMessage());
		}
		return s;
	}
}
