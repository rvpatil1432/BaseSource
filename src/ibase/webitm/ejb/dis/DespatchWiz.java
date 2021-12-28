
package ibase.webitm.ejb.dis;

import ibase.bean.GetServersStatusBean;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.adv.DespatchAct;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Stateless
public class DespatchWiz extends ValidatorEJB implements DespatchWizLocal, DespatchWizRemote {

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String userId = null;
	String chgUser = null;
	String chgTerm = null;
	NumberFormat nf = null;
	boolean isError=false;



	public DespatchWiz() 
	{
		System.out.println("^^^^^^^ inside DespatchWiz  Wizard ^^^^^^^");
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside DespatchWiz  Wizard 111^^^^^^^");
		System.out.println("xmlString--------->>["+xmlString+"]");
		System.out.println("xmlString1--------->>["+xmlString1+"]");
		System.out.println("xmlString2--------->>["+xmlString2+"]");

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
			if(xmlString !=null && xmlString.trim().length() > 0){
			dom = parseString(xmlString);
			}
			if(xmlString1 !=null && xmlString1.trim().length() > 0){
				dom1 = parseString(xmlString1);
			}			
			if (xmlString2 !=null && xmlString2.trim().length() > 0) 
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
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
		System.out.println("----------in wfvalData of DespatchWiz........");
		//GenericUtility genericUtility;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0, currentFormNo = 0, childNodeListLength = 0, cnt = 0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		Connection conn = null;
		String userId = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		String sql = "",locCode ="",invstat="",aval="",avalyn="",status="",errorType="",despId = "";
        double qtyConf=0,qtyShip=0,totQty=0,detquantity=0,stkquantity=0,quantity=0,totquantity=0;
		String squantity="",distOrder = "",itemCode="",confirmed="",tranId="",siteCode="",lotSl="",addUser="",tranCode="";
		String itemCodeL="",SiteCodeL="",CustCodeL="",ChanPart="",sorderNo="", custCodeDlv = "";
		ArrayList<String> lockList=new ArrayList<String>();
		HashMap<String,String>invHoldMap=new HashMap<String,String>();
		String lotNo="",locCodeL="",qcLockValue="";
		double sdetQty=0,despatchQty=0,actQty=0;
		ArrayList <String> itemCodeList=new ArrayList<String>();					
		int count=0;

		try {
			//genericUtility = GenericUtility.getInstance();
			System.out.println("editFlag------>>["+editFlag+"]");
			System.out.println("xtraParam----->>["+xtraParams+"]");
			System.out.println("DOM---->>["+genericUtility.serializeDom(dom).toString()+"]");
			System.out.println("DOM1----->>["+genericUtility.serializeDom(dom1).toString()+"]");
			System.out.println("DOM2----->>["+genericUtility.serializeDom(dom2).toString()+"]");	

			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			
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

					System.out.println("childNodeName------->>["+childNodeName+"]");
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("sale_order_no"))
					{
						int despCount=0;
						sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no",dom));
						System.out.println("sorderNo---->>["+sorderNo+"]");
						if (sorderNo.length() == 0){
						errCode = "VTSORDNNN";
						errString = getErrorString("sale_order_no",errCode,userId);
						break;
						}else{
							cnt=getDBRowCount(conn,"sorder","sale_order",sorderNo);
							System.out.println("cnt1------>>["+cnt+"]");
							if(cnt==0){
								errCode = "VTSORDNND";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}else{
								confirmed=checkNull(getColumnDescr(conn,"confirmed","sorder","sale_order",sorderNo));
								
								System.out.println("Sale order Confirmed---->>["+confirmed+"]");
								sql="select status,confirmed from sorder where sale_order = ? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, sorderNo);
								rs=pstmt.executeQuery();
								if(rs.next()){
									status=rs.getString(1);
									confirmed=rs.getString(2);
																		
								}
								if(rs!=null){
								rs.close();
								rs=null;
								}
								if(pstmt!=null){
								pstmt.close();
								pstmt=null;
								}
								confirmed=confirmed == null ? "N" :confirmed.trim();
								status=status == null ? "P" :status.trim();
								despCount=getDBRowCount(conn,"despatch","sord_no",sorderNo);
								double sordItemQty=getQtyFromSordItem(sorderNo,conn);
								
								avalyn=getColumnDescr(conn, "available_yn", "sorder", "sale_order", sorderNo);
								avalyn=avalyn==null ? "N" : avalyn.trim();
								System.out.println("avalyn--->>["+avalyn+"]");
								System.out.println("Sale order Confirmed---->>["+confirmed+"]");
								System.out.println("Sale order status---->>["+status+"]");
								System.out.println("despCount---->>["+despCount+"]");
								System.out.println("sordItemQty---->>["+sordItemQty+"]");							
								
								if("N".equalsIgnoreCase(confirmed)){
									errCode = "VTSORDNNC";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}else if(! "P".equalsIgnoreCase(status)){
									errCode = "VTSORDSNP";
									errList.add( errCode );
									errFields.add( childNodeName.toLowerCase() );
								}else if(sordItemQty == 0){
									errCode = "VTSORDDEF";  
									errString = getErrorString("sale_order_no",errCode,userId);
									break;
								}else if("N".equalsIgnoreCase(avalyn)){									
									
									sql="select site_code,cust_code, cust_code__dlv from sorder where sale_order = ?";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1,sorderNo);
									rs=pstmt.executeQuery();
									if(rs.next()){
										SiteCodeL=checkNull(rs.getString(1));
										CustCodeL=checkNull(rs.getString(2));
										custCodeDlv=checkNull(rs.getString(3));
									}
									rs.close();
									pstmt.close();	
									rs = null;
									pstmt = null;								
									sql="select channel_partner from site_customer where site_code= ? and "
											+ "cust_code = ? and available_yn = ?";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1,SiteCodeL);
									//pstmt.setString(2,CustCodeL); // 12/02/15 manoharan commented and cust_code__dlv considered
									pstmt.setString(2,custCodeDlv);
									pstmt.setString(3,"N");
									rs=pstmt.executeQuery();
									if(rs.next()){										
										ChanPart=rs.getString(1);
									}
									ChanPart=ChanPart ==null ? ""  : ChanPart.trim();
									System.out.println("ChanPart---->>["+ChanPart+"]");
									if("N".equalsIgnoreCase(ChanPart)){
										errCode = "VTCUSTCD9";
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
									rs.close();
									pstmt.close();	
									rs = null;
									pstmt = null;
									if(ChanPart.length() == 0){
										sql="select channel_partner from customer where cust_code = ? and available_yn = ? ";
										pstmt=conn.prepareStatement(sql);
										//pstmt.setString(1,CustCodeL); // 12/02/15 manoharan commented and cust_code__dlv considered
										pstmt.setString(1,custCodeDlv); 
										pstmt.setString(2,"N");
										rs=pstmt.executeQuery();
										if(rs.next()){
											ChanPart=rs.getString(1);
										}
										ChanPart=ChanPart ==null ? "N"  : ChanPart.trim();
										System.out.println("ChanPart cust----->>["+ChanPart+"]");
										if("N".equalsIgnoreCase(ChanPart) ||  ChanPart.length() ==0 ){
											errCode = "VTCUSTCD9"; //The Customer should be channel partner Y and available N !
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );
										}
									}
									
								}
								
							}
							
							
						} //end main else

					}

				}
				break;
			case 2:

				parentNodeList = dom2.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				System.out.println("parentNode >>>{"+parentNode+"}");
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();


				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("value of child node name ["+childNodeName + "]");



					if("E".equalsIgnoreCase(editFlag))
					{
						addUser = genericUtility.getColumnValue("add_user",dom);
						chgUser = genericUtility.getColumnValue("chg_user",dom);
						System.out.println("addUser><><"+addUser+"chgUserDD"+chgUser);
						if ((addUser != null && addUser.trim().length() > 0) && (chgUser != null && chgUser.trim().length() > 0))
						{
							if(!addUser.equalsIgnoreCase(chgUser))
							{
								errCode = "DIDOADDUM";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}

					if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code",dom));
						if(childNode.getFirstChild()!= null)
						{
							tranCode = childNode.getFirstChild().getNodeValue();
						}

						System.out.println("tranCode>>"+tranCode);

						if (tranCode.trim().length() == 0)
						{
							errCode = "DIDOTRCONU";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							cnt=getDBRowCount(conn, "transporter", "tran_code", tranCode);
							if(cnt == 0)
							{
								errCode = "DIDOTRCONE";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("desp_id"))
					{

						//despId = genericUtility.getColumnValue("desp_id",dom2);
						if(childNode.getFirstChild()!= null)
						{
							despId = childNode.getFirstChild().getNodeValue();
							System.out.println("despId ["+despId + "]");
							sql = "select count(1) from ship_docs where ref_ser = 'S-DSP' and ref_id = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,despId);
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
				
				System.out.println("DOM Elements-->["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("wfvaldata case 3 DOM1 Elements-->["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 Elements-->["+genericUtility.serializeDom(dom2).toString()+"]");

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("value of child node : "+childNode);

					if(childNodeName.equalsIgnoreCase("lot_sl"))
					{
						sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no",dom));
						siteCode = genericUtility.getColumnValue("site_code",dom2,"2");
						lotSl=checkNull(genericUtility.getColumnValue("lot_sl", dom));
						//distOrder = genericUtility.getColumnValue("dist_order",dom2);

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

						
					}
					else if(childNodeName.equalsIgnoreCase("item_code"))
					{
						System.out.println("----------in wfvaldata of item_code............");
						qcLockValue="";
						sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no", dom1));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						//lotSlL=checkNull(genericUtility.getColumnValue("lot_sl", dom2,"3"));
						
						avalyn=getColumnDescr(conn, "available_yn", "sorder", "sale_order", sorderNo);
						avalyn=avalyn==null ? "N" : avalyn.trim();
						System.out.println("Dertail3 avalyn--->>["+avalyn+"]");
						System.out.println("sorderNo------>>["+sorderNo+"]");
						System.out.println("itemCode------>>["+itemCode+"]");						
						if (itemCode.trim().length() == 0)
						{
							errCode = "DIDIICNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else  
						{
							sql = "select item_code from sorddet where sale_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,sorderNo);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								itemCodeList.add(checkNull(rs.getString(1)));
							}
							System.out.println("itemCodeList------>>["+itemCodeList+"]");
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							if(!(itemCodeList.contains(itemCode)))
							{
								errCode = "DIDIICNOMA";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}							
							
						}
					}

					else if(childNodeName.equalsIgnoreCase("quantity")) 
					{	
						System.out.println("-------------wfvalData..quantity.............");
						sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no", dom1));
						squantity = genericUtility.getColumnValue("quantity",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom2,"2");
						//lotSl = genericUtility.getColumnValue("lot_sl",dom2);
						lotSl=checkNull(genericUtility.getColumnValue("lot_sl", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						locCodeL = checkNull(genericUtility.getColumnValue("loc_code", dom));
						lotNo = checkNull(genericUtility.getColumnValue("lot_no", dom));						
						despId = checkNull(genericUtility.getColumnValue("desp_id", dom));
						
						avalyn=getColumnDescr(conn, "available_yn", "sorder", "sale_order", sorderNo);
						avalyn=avalyn==null ? "N" : avalyn.trim();
                        System.out.println("sorderNo--->>["+sorderNo+"]");
                        System.out.println("DespId--->>["+despId+"]");
                        System.out.println("lotSl--->>["+lotSl+"]");
                        System.out.println("siteCode--->>["+siteCode+"]");
                        System.out.println("locCodeL--->>["+locCodeL+"]");
                        System.out.println("lotNo--->>["+lotNo+"]");
                        System.out.println("itemCode--->>["+itemCode+"]");
                        System.out.println("squantity--->>["+squantity+"]");                       
            			
                        invHoldMap.put("item_code", itemCode);
                        invHoldMap.put("site_code", siteCode);
                        invHoldMap.put("loc_code", locCodeL);
                        invHoldMap.put("lot_no", lotNo);
                        invHoldMap.put("lot_sl", lotSl);
						if (squantity == null || squantity.trim().length() == 0)
						{
							quantity = 0;
						}
						else
						{
							quantity = Double.parseDouble(squantity);
						}
						qcLockValue=checkNull(getColumnDescr(conn, "var_value", "disparm", "var_name", "QUARNTINE_LOCKCODE"));
						System.out.println("qcLockValue-------->>["+qcLockValue+"]");
						lockList=getInvHoldQCLock(invHoldMap, conn);	
						System.out.println("Return lockList-------->>["+lockList+"]");
						int listCount=lockList.size();
						int occurence=Collections.frequency(lockList, qcLockValue);
						int otherLockCount= listCount - occurence;
						System.out.println("Occurence-------->>["+occurence+"]");
						if("N".equalsIgnoreCase(avalyn)){
							if(otherLockCount > 0){								
								errCode = "VTSTKHOLD";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}						
						
						sql = "select sum(quantity) from despatchdet where sord_no = ?  and item_code = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,sorderNo);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							despatchQty = rs.getDouble(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						System.out.println("despatchQty--->>["+despatchQty+"]");//280
						
						
							totquantity = despatchQty + quantity;//0+20=20
						
						
						sql = "select sum(quantity) from sorddet where sale_order = ? and item_code = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,sorderNo);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							sdetQty = rs.getDouble(1);//10
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						
						actQty=getQtyFromSordItem(sorderNo, conn);
						System.out.println("actQty--->["+actQty+"]");//280
						System.out.println("totquantity--->>["+totquantity+"]");
						System.out.println("sdetQty--->>["+sdetQty+"]");
						if(totquantity > sdetQty)
						{
							errCode = "VTDPCCSQ";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}else if(quantity > actQty){
							errCode = "VTDPCCMPQ";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
				}
					

					else if(childNodeName.equalsIgnoreCase("loc_code"))
					{
						String availYN = "";	// Added by Sandesh 29-Sep-2014
						sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no", dom1));
						
						locCode = genericUtility.getColumnValue("loc_code",dom);
						//tranId = genericUtility.getColumnValue("tran_id",dom2);
						System.out.println("sorderNo---loc_code---->>["+sorderNo+"]");
						
						if (locCode == null || locCode.trim().length() == 0)
						{
							errCode = "DIDOLCNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}else
						{
							invstat=getColumnDescr(conn, "inv_stat", "location", "loc_code", locCode);
							System.out.println("invstat---loc_code---->>["+invstat+"]");

							// Added by Sandesh 29-Sep-2014

							sql = "select case when available_yn is null then 'N' else available_yn end from sorder where sale_order = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,sorderNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								availYN = rs.getString(1);
							}
							if(pstmt!=null){
								pstmt.close();
								pstmt = null;
							}
							if(rs!=null){
								rs.close();
								rs = null;
							}
							
							
							System.out.println("available_yn from sorder header : "+availYN);
							//----------------------
							
							aval=getColumnDescr(conn, "available", "invstat", "inv_stat", invstat);		
							
							System.out.println("available from invstat "+aval);
							
							// Added by Sandesh 29-Sep-2014
							
							if(availYN.equalsIgnoreCase("N") && "Y".equalsIgnoreCase(aval)){
								errCode = "VTAVAIL";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
								
							}else if(availYN.equalsIgnoreCase("Y") && "N".equalsIgnoreCase(aval)){
								errCode = "VTAVAIL";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							// Added by Sandesh 29-Sep-2014
							
							/*if( "N".equalsIgnoreCase(aval)){	// Commented by Sandesh 29-Sep-2014
								errCode = "VTAVAIL";
								errString = getErrorString("loc_code",errCode,userId);
								break;
							}*/						
						}

					}
				}
				break;
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
					conn = null;
				}
				
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return errStringXml.toString();
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


			System.out.println("xmlString111>>"+xmlString);
			System.out.println("xmlString222>>"+xmlString1);
			System.out.println("xmlString333>>"+xmlString2);
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
		System.out.println("DOM Elements---->>["+genericUtility.serializeDom(dom).toString()+"]");
		System.out.println("DOM111 Elements---->>["+genericUtility.serializeDom(dom1).toString()+"]");
		System.out.println("DOM222 Elements---->>["+genericUtility.serializeDom(dom2).toString()+"]");
		String distOrder = "",itemCode = "",unit="",packCode="",tranType ="";
		java.sql.Timestamp currDate = null;
		double grossWeight = 0,netWeight=0,tareWeight=0,noOfArt=0;
		SimpleDateFormat sdf = null;
		String currAppdate = "";
		String tranid ="";
		String rate = "";
		double amount = 0;
		int cnt = 0,lineNo1=0;;
		String tranCode = "",tranName="",transMode="";
		StringBuffer detail2xml = new StringBuffer();
		String tranDate = null;
		PreparedStatement pstmt2 =null;
		ResultSet rs2 = null,rs3 = null;
		String unitAlt = null;
		int count = 0;
		double minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,grossWt = 0d,tarePer = 0d,netWt = 0d,tareWt =0d, rateClgVal = 0d, rate2 = 0d;
		double disAmount = 0d, shipperQty = 0d,discount =0,holdQtyL=0;
		int  minShelfLife = 0, noArt1 = 0;
		int mLineNoDist =0;
		double qtyConfirm =0,qtyShipped =0,lcQtyOrderAlt =0,lcFact =0;
		
		
		//Dadaso
		String sql="",currencyCode="",exchRate1="",currCodeFrt="",currCodeIns="",currCodedlv="",stanCode="",custCode="",custCodeDlv="",siteCodeDesc="";
		String stanCodeDlv="",dlvCity="",dlvPin="",countCodeDlv="",stanCodeInit="",status="",statusRemarks="",despIdNew="",natureLinkType="",
				custCodeBil="",dlvAdd1="",dlvAdd2="",dlvAdd3="",despId="",remarksDes="",noArtDes="",sorderNo="",
				siteCodeShip="",availableYn="",addUser="",chgUserL="",chgTermL="",lotslL="",lotStatus="HOLD";
		String confirm = "", licnNo = "", licnNo1 ="", licnNo2 = "", licnNo3 ="", availYN = "";
		double insuranceAmt = 0, frghtAmt = 0;											// Added by sandesh 2-Oct-2014
		
		java.sql.Timestamp effDate = null, chgDate =  null,licnDate1= null, licnDate2 = null, licnDate3 = null;
		
		
		ArrayList<String> addUsersList=new ArrayList<String>();
		Date orderDt = null,statusDate=null,mfgDate=null,expiryDate=null,sOrdDate=null,retestDate=null;
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
			
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));			
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));			

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO IS---->>"+currentFormNo);
			System.out.println("currentColumn--------->>["+currentColumn+"]");
			System.out.println("editFlag111--------->>["+editFlag+"]");
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			switch (currentFormNo) 
			{

			case 1 :
				break;

			case 2 : 
				System.out.println("DOM1 Elements-->["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 Elements-->["+genericUtility.serializeDom(dom2).toString()+"]");
							
				sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no", dom1));
				System.out.println("sorderNo in case211---->>["+sorderNo+"]");	
				sql="select count(1) from despatch where CASE WHEN confirmed IS NULL "
						+ "THEN 'N' ELSE confirmed END = 'N' and sord_no = ? and add_user = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,sorderNo);
				pstmt.setString(2,chgUser);
				
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				System.out.println("sorderNo cnt123/@-@/>>----->>["+cnt+"]");//GROSS_WEIGHT, TARE_WEIGHT, NETT_WEIGHT		
				
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					if (cnt > 0 )
					{
						sql="select d.desp_id,d.site_code,d.desp_date,d.curr_code,d.exch_rate,d.trans_mode,d.curr_code__frt,d.curr_code__ins,d.stan_code,"
								+ "d.state_code__dlv,d.dlv_city,d.dlv_pin,d.count_code__dlv,d.dlv_add1,d.dlv_add2,d.dlv_add3,"
								+ "d.tran_code,d.stan_code__init,d.status,d.status_remarks,d.remarks,d.no_art,d.status_date,d.cust_code,d.cust_code__dlv,"
								+ "s.site_code__ship,s.CHG_USER,s.CHG_TERM, s.order_date,d.gross_weight,d.nett_weight,d.tare_weight,d.no_art,"
								+ "d.eff_date,d.confirmed,d.chg_date,d.chg_term,d.chg_user,d.licence_no,d.licence_no_1,d.licence_no_2,d.licence_no_3,"	// Sandesh 2-Oct-2014
								+ "d.licence_date_1,d.licence_date_2,d.licence_date_3,d.available_yn,d.insurance,d.freight "							// Sandesh 2-Oct-2014
								+ " from despatch d,sorder s where d.sord_no = ? and d.sord_no = s.sale_order  and d.add_user = ? "
								+ " and CASE WHEN d.confirmed IS NULL THEN 'N' ELSE d.confirmed END = 'N'";

						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,sorderNo);
						pstmt.setString(2,chgUser);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							System.out.println("Data found3334455...................");
							despId=checkNull(rs.getString("desp_id"));
							siteCode = checkNull(rs.getString("site_code"));
							currencyCode = checkNull(rs.getString("curr_code"));
							exchRate1 = checkNull(rs.getString("exch_rate"));
							transMode = rs.getString("trans_mode") == null ? "R " : rs.getString("trans_mode");
							currCodeFrt = checkNull(rs.getString("curr_code__frt"));
							currCodeIns = checkNull(rs.getString("curr_code__ins"));						

							orderDt = rs.getDate("desp_date");						

							stanCode = checkNull(rs.getString("stan_code"));							
							stanCodeDlv = checkNull(rs.getString("state_code__dlv"));
							dlvCity = checkNull(rs.getString("dlv_city"));
							dlvPin = checkNull(rs.getString("dlv_pin"));
							countCodeDlv = checkNull(rs.getString("count_code__dlv"));
							tranCode = checkNull(rs.getString("tran_code"));
							stanCodeInit = checkNull(rs.getString("stan_code__init"));
							status = checkNull(rs.getString("status"));
							
							//---- Added by Sandesh 2-Oct-2014  -
							/*if(status.equals("")){
								
								status = " ";
							}*/
							// -----------------------
							
							statusRemarks = checkNull(rs.getString("status_remarks"));
							statusDate = rs.getDate("status_date");
							custCode=checkNull(rs.getString("cust_code"));
							custCodeDlv=checkNull(rs.getString("cust_code__dlv"));
							siteCodeShip=checkNull(rs.getString("site_code__ship"));

							dlvAdd1=checkNull(rs.getString("dlv_add1"));
							dlvAdd2=checkNull(rs.getString("dlv_add2"));
							dlvAdd3=checkNull(rs.getString("dlv_add3"));

							remarksDes=checkNull(rs.getString("remarks"));
							noArtDes=checkNull(rs.getString("no_art"));
							sOrdDate=rs.getDate("order_date");	
							grossWeight=rs.getDouble("gross_weight");
							netWeight=rs.getDouble("nett_weight");
							tareWeight=rs.getDouble("tare_weight");
							
							//------- Added by Sandesh 2-Oct-2014  -------
							
							effDate = rs.getTimestamp("EFF_DATE"); 
							confirm = checkNull(rs.getString("CONFIRMED"));
							chgDate = rs.getTimestamp("CHG_DATE"); 
							licnNo  = checkNull(rs.getString("LICENCE_NO"));
							licnNo1 = checkNull(rs.getString("LICENCE_NO_1"));
							licnNo2 = checkNull(rs.getString("LICENCE_NO_2"));
							licnNo3 = checkNull(rs.getString("LICENCE_NO_3"));
							licnDate1 = rs.getTimestamp("LICENCE_DATE_1");
							licnDate2 = rs.getTimestamp("LICENCE_DATE_2");
							licnDate3 = rs.getTimestamp("LICENCE_DATE_3");
							availYN = checkNull(rs.getString("AVAILABLE_YN")); 
							insuranceAmt = rs.getDouble("INSURANCE");
							frghtAmt = rs.getDouble("FREIGHT");
							
							System.out.println("\n New values -> ");
							
							System.out.println("\n effDate : "+effDate+" confirm : "+confirm+" chgDate : "+chgDate+" licnNo : "+licnNo+" frghtAmt : "+frghtAmt);
							System.out.println("\n licnNo1 : "+licnNo1+" licnNo2 : "+licnNo2+" licnNo3 : "+licnNo3+" insuranceAmt : "+insuranceAmt);
							System.out.println("\n licnDate1 : "+licnDate1+" licnDate2 : "+licnDate2+" licnDate3 : "+licnDate3+" availYN : "+availYN);
							
							// ------------------------------
							
							
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;
						System.out.println("tranCode----->>["+tranCode+"]");
						System.out.println("orderDt----->>["+orderDt+"]");
						System.out.println("sOrdDate----->>["+sOrdDate+"]");
						System.out.println("custCodeDlv----->>["+custCodeDlv+"]");
						valueXmlString.append("<Detail2 domID='" + count + "'  objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
						valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
						valueXmlString.append("<desp_id><![CDATA["+ despId +"]]></desp_id>");
						if(orderDt != null)
						{
							valueXmlString.append("<desp_date><![CDATA["+ sdf.format(orderDt).toString() +"]]></desp_date>");										
						}
						if(sOrdDate != null)
						{
							valueXmlString.append("<sord_date><![CDATA["+ sdf.format(sOrdDate).toString() +"]]></sord_date>");
						}
						valueXmlString.append("<sord_no><![CDATA["+ sorderNo +"]]></sord_no>");
						valueXmlString.append("<cust_code><![CDATA["+ custCode +"]]></cust_code>");
						valueXmlString.append("<cust_code__dlv><![CDATA["+ custCodeDlv +"]]></cust_code__dlv>");
						valueXmlString.append("<cust_code__bil><![CDATA["+ custCodeBil +"]]></cust_code__bil>");
						valueXmlString.append("<stan_code><![CDATA["+ stanCode.trim() +"]]></stan_code>");
						valueXmlString.append("<trans_mode><![CDATA["+ transMode.trim() +"]]></trans_mode>");

						//valueXmlString.append("<lr_date><![CDATA["+ lrDateVal.toString() +"]]></lr_date>");
						//valueXmlString.append("<shipment_id><![CDATA["+ shipmentId.trim() +"]]></shipment_id>");
						valueXmlString.append("<curr_code__frt><![CDATA["+ currCodeFrt.trim() +"]]></curr_code__frt>");
						valueXmlString.append("<curr_code__ins><![CDATA["+ currCodeIns.trim() +"]]></curr_code__ins>");
						valueXmlString.append("<curr_code><![CDATA["+ currencyCode.trim() +"]]></curr_code>");
						valueXmlString.append("<exch_rate><![CDATA["+  exchRate1 +"]]></exch_rate>");// CAN NOT BE NULL
						valueXmlString.append("<exch_rate__frt><![CDATA["+  exchRate1 +"]]></exch_rate__frt>");	
						valueXmlString.append("<exch_rate__ins><![CDATA["+ exchRate1 +"]]></exch_rate__ins>");


						valueXmlString.append("<site_code__ship><![CDATA["+ siteCodeShip +"]]></site_code__ship>");
						if(siteCodeShip.length() > 0)
						{
							siteCodeDesc=checkNull(getColumnDescr(conn, "descr", "site", "site_code", siteCodeShip));
						}
						valueXmlString.append("<site_descr><![CDATA["+ siteCodeDesc+"]]></site_descr>");
						valueXmlString.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
						if(siteCode.length() > 0)
						{
							siteCodeDesc=checkNull(getColumnDescr(conn, "descr", "site", "site_code", siteCode));	
						}

						valueXmlString.append("<descr><![CDATA["+ siteCodeDesc+"]]></descr>");
						valueXmlString.append("<state_code__dlv><![CDATA["+ stanCodeDlv.trim()+"]]></state_code__dlv>");

						valueXmlString.append("<dlv_add1><![CDATA["+ dlvAdd1 +"]]></dlv_add1>");
						valueXmlString.append("<dlv_add2><![CDATA["+ dlvAdd2 +"]]></dlv_add2>");
						valueXmlString.append("<dlv_add3><![CDATA["+ dlvAdd3 +"]]></dlv_add3>");

						valueXmlString.append("<dlv_city><![CDATA["+ dlvCity +"]]></dlv_city>");				
						valueXmlString.append("<dlv_pin><![CDATA["+ dlvPin +"]]></dlv_pin>");
						valueXmlString.append("<count_code__dlv><![CDATA["+ countCodeDlv +"]]></count_code__dlv>");
						valueXmlString.append("<tran_code><![CDATA["+ tranCode +"]]></tran_code>");
						valueXmlString.append("<stan_code__init><![CDATA["+ stanCodeInit +"]]></stan_code__init>");
						valueXmlString.append("<status_remarks><![CDATA["+ statusRemarks +"]]></status_remarks>");
						
						status = " ";		// Added by sandesh
						valueXmlString.append("<status><![CDATA["+ status +"]]></status>");
						tranName=getColumnDescr(conn,"tran_name" , "transporter", "tran_code", tranCode);
						valueXmlString.append("<tran_name><![CDATA["+ tranName +"]]></tran_name>");

						valueXmlString.append("<remarks><![CDATA["+ remarksDes +"]]></remarks>");
						valueXmlString.append("<no_art><![CDATA["+ noArtDes+"]]></no_art>");	
						valueXmlString.append("<gross_weight>").append("<![CDATA[" + grossWeight + "]]>").append("</gross_weight>");
						valueXmlString.append("<nett_weight>").append("<![CDATA[" + netWeight + "]]>").append("</nett_weight>");
						valueXmlString.append("<tare_weight>").append("<![CDATA[" + tareWeight + "]]>").append("</tare_weight>");
						
						valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>"); 
						valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
						valueXmlString.append("<add_date>").append("<![CDATA[" + currAppdate + "]]>").append("</add_date>");
						
						//---------- Added by Sandesh 2-Oct-2014  ---------
						
						if(effDate!=null)
						{
							valueXmlString.append("<eff_date>").append("<![CDATA[" + simpleDateFormat.format(effDate).toString()+ "]]>").append("</eff_date>");
						}
						 
						valueXmlString.append("<confirmed>").append("<![CDATA[" + confirm + "]]>").append("</confirmed>");
						
						if(chgDate!=null)
						{
							valueXmlString.append("<chg_date><![CDATA["+ simpleDateFormat.format(chgDate).toString() +"]]></chg_date>");
						}
						
						valueXmlString.append("<licence_no_1><![CDATA["+ licnNo1 +"]]></licence_no_1>");
						valueXmlString.append("<licence_no_2><![CDATA["+ licnNo2 +"]]></licence_no_2>");
						valueXmlString.append("<licence_no_3><![CDATA["+ licnNo3 +"]]></licence_no_3>");
						
						if(licnDate1!=null)
						{		
							valueXmlString.append("<licence_date_1><![CDATA["+  simpleDateFormat.format(licnDate1).toString()+"]]></licence_date_1>");
						}	
						if(licnDate2!=null)
						{		
							valueXmlString.append("<licence_date_2><![CDATA["+  simpleDateFormat.format(licnDate2).toString()+"]]></licence_date_2>");
						}	
						if(licnDate3!=null)
						{		
							valueXmlString.append("<licence_date_3><![CDATA["+  simpleDateFormat.format(licnDate3).toString()+"]]></licence_date_3>");
						}	
						
						valueXmlString.append("<available_yn><![CDATA["+ availYN +"]]></available_yn>");

						valueXmlString.append("<insurance><![CDATA["+ insuranceAmt +"]]></insurance>");
						
						valueXmlString.append("<freight><![CDATA["+ frghtAmt +"]]></freight>");	
						//-------------------------------------------------
					
						valueXmlString.append("</Detail2>");
						
					}
					else
					{												
						valueXmlString.append(insertNewRecord(sorderNo,currentFormNo,conn));
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code_123"))//comment
				{
					System.out.println(">>>>START tran_code38>>>>");
					tranCode = genericUtility.getColumnValue("tran_code",dom);
					System.out.println("tranCode----->>["+tranCode+"]");
					String currDomStr = genericUtility.serializeDom(dom);

					if(tranCode != null && tranCode.trim().length() > 0)
					{
						tranName=getColumnDescr(conn,"tran_name" , "transporter", "tran_code", tranCode);						
						System.out.println("tranName----->>["+tranName+"]");
						valueXmlString.append("<tran_name protect=\"0\">").append("<![CDATA[" + tranName + "]]>").append("</tran_name>");
						setNodeValue( dom, "tran_name", getAbsString(""+tranName));					
						transMode=getColumnDescr(conn,"trans_mode" , "transporter_mode", "tran_code", tranCode);
						
						valueXmlString.append("<trans_mode protect=\"0\">").append("<![CDATA[" + transMode + "]]>").append("</trans_mode>");
						setNodeValue( dom, "trans_mode", getAbsString(""+transMode)); 						
						currDomStr = currDomStr.replace("</Detail2>", valueXmlString.toString() + "</Detail2>");
						System.out.println("after currDomStr[" + currDomStr + "]");
						valueXmlString.append(currDomStr);
					}
					else
					{
						System.out.println("-------in else of tran mode --------------------");
						valueXmlString.append("<tran_name protect=\"0\">").append("<![CDATA[]]>").append("</tran_name>");
						setNodeValue( dom, "sh_name", getAbsString("")); 

						valueXmlString.append("<trans_mode protect=\"0\">").append("<![CDATA[]]>").append("</trans_mode>");
						setNodeValue( dom, "trans_mode", getAbsString("")); 

						currDomStr = currDomStr.replace("</Detail2>", valueXmlString.toString() + "</Detail2>");
						System.out.println("after currDomStr[" + currDomStr + "]");
						valueXmlString.append(currDomStr);
					}


					System.out.println(">>>>END>>>>>>>>>trancode");

				}


				break;
			case 3 : 
				//System.out.println("------Case 3 called or form 3 called------@@-----------");
				//System.out.println("dom in form3---->>["+genericUtility.serializeDom(dom)+"]");
				//System.out.println("dom111 in form3---->>["+genericUtility.serializeDom(dom1)+"]");
				//System.out.println("dom222 in form3---->>["+genericUtility.serializeDom(dom2)+"]");
				tranDate = getCurrdateAppFormat() ;			
				
				sorderNo = checkNull(genericUtility.getColumnValue("sale_order_no", dom1));
				despId = checkNull(genericUtility.getColumnValue("desp_id", dom2,"2"));
				String domID = checkNull(getColumnValueMy("Detail3", dom2, "3"));
				System.out.println("sorderNo---->>["+sorderNo+"]");
				System.out.println("despId---->>["+despId+"]");
				System.out.println("domID---->>["+domID+"]");
				System.out.println("addUsersList2@---->>["+addUsersList+"]");
				if(addUsersList !=null){
					addUsersList.clear();
				}
				
				sql = "select add_user from despatch where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' and sord_no= ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,sorderNo);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					addUser = rs.getString("add_user")==null?"":rs.getString("add_user").trim();
					addUsersList.add(addUser);					
				}
				if(rs!=null){
				rs.close();
				rs=null;
				}
				if(pstmt!=null){
				pstmt.close();
				pstmt = null;
				}
				if(addUsersList.contains(chgUser))
				{
					for(int i = 0;i < addUsersList.size(); i++)
					{
						
						addUser = addUsersList.get(i);
						System.out.println("addUser>>>>>"+addUser);
						
						if(chgUser.equalsIgnoreCase(addUser))
						{
							sql = "select desp_id from despatch where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' and sord_no = ? and add_user = ?";
							pstmt1=conn.prepareStatement(sql);
							pstmt1.setString(1,sorderNo);
							pstmt1.setString(2,addUser);
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								despIdNew = rs1.getString(1) == null ? "":rs1.getString(1);
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
					sql = "select desp_id from despatch where CASE WHEN confirmed IS NULL THEN 'N' ELSE CONFIRMED  END = 'N' and sord_no = ?";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,sorderNo);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						despIdNew = rs1.getString(1) == null ? "":rs1.getString(1);
					}
					pstmt1.close();
					rs1.close();
					pstmt1 = null;
					rs1 = null;						
				}

				
				System.out.println("tranid FROM QUERY----->["+despIdNew+"]");
				
				
				
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("Form 3 itm_default called234.................");
					sql="select despatchdet.desp_id,despatchdet.line_no,despatchdet.sord_no,despatchdet.line_no__sord,"
							+ "despatchdet.exp_lev,despatchdet.item_code__ord,despatchdet.item_code,despatchdet.lot_no, "   
				         +"despatchdet.lot_sl,despatchdet.quantity__ord,despatchdet.quantity,item.descr, "
				         +"despatchdet.loc_code,despatchdet.status,despatchdet.conv__qty_stduom, "   
				         +"despatchdet.unit__std,despatchdet.unit, " 
				         +"despatchdet.quantity__stduom,despatchdet.quantity_real, "   
				         +"despatchdet.rate__stduom,sorddet.tax_class, "   
				         +"sorddet.tax_chap,sorddet.tax_env, "
				         +"sorddet.discount,despatchdet.pack_instr, "
				         +"despatchdet.no_art,sorditem.quantity - sorditem.qty_desp as pending_qty, "   
				         +"sorddet.rate,despatchdet.pack_qty, "   
				         +"despatchdet.exp_date,despatchdet.site_code, "   
				         +"despatchdet.mfg_date,despatchdet.chg_date, "   
				         +"despatchdet.chg_user,despatchdet.chg_term, "   
				         +"despatchdet.site_code__mfg,despatchdet.rate__clg, "   
				         +"despatchdet.gross_weight,despatchdet.tare_weight, "   
				         +"despatchdet.nett_weight,despatchdet.dimension, "   
				         +"despatchdet.tax_amt,despatchdet.disc_amt, "
				         +"despatchdet.chg_term,despatchdet.chg_user, "
				         
				         +"despatchdet.conf_diff_amt,despatchdet.rate__std, "   
				         +"despatchdet.cost_rate,'' as qty_details, "  
				         +"despatchdet.line_type,0 as tot_net_amt, "
				         +"despatchdet.conv__rtuom_stduom,despatchdet.pallet_wt, "
				         +"despatchdet.tran_id__invpack,despatchdet.cust_item__ref, "
				         +"fn_custitem_dscr((despatch.cust_code),despatchdet.item_code,despatchdet.cust_item__ref)  as custitem_desc, "   
				         +"despatchdet.retest_date,despatchdet.part_no, "
				         +"despatchdet.disc_schem_billback_amt,despatchdet.disc_schem_offinv_amt, "   
				         +"despatchdet.pallet_no " 
				         +"from despatchdet, "   
				         +"item, "
				         +"sorddet, "   
				         +"sorditem, "   
				         +"despatch "  
				         +"where ( despatchdet.item_code = item.item_code (+)) and "  
				         +"( despatchdet.sord_no = sorddet.sale_order (+)) and "  
				         +"( despatchdet.line_no__sord = sorddet.line_no (+)) and "  
				         +"( despatchdet.sord_no = sorditem.sale_order (+)) and "
				         +"( despatchdet.line_no__sord = sorditem.line_no (+)) and "  
				         +"( despatchdet.exp_lev = sorditem.exp_lev (+)) and "  
				         +"( despatch.desp_id = despatchdet.desp_id ) and "
				         + " ( case when despatch.confirmed is null then 'N' else despatch.confirmed end <>'Y' ) and  "  
				         +"( ( despatchdet.desp_id = ? ))";
					
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, despId);
					rs=pstmt.executeQuery();
					while(rs.next()){				
					
						siteCode=checkNull(rs.getString("site_code"));
						lotslL=checkNull(rs.getString("lot_sl"));
					valueXmlString.append("<Detail3 domID='" +rs.getInt("line_no")+ "'  objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
					valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
					
					
					valueXmlString.append("<desp_id><![CDATA["+ despId +"]]></desp_id>");
					valueXmlString.append("<sord_no><![CDATA["+ checkNull(rs.getString("sord_no")) +"]]></sord_no>");
					valueXmlString.append("<line_no><![CDATA["+ rs.getString("line_no") +"]]></line_no>");					
					valueXmlString.append("<line_no__sord><![CDATA["+ rs.getString("line_no__sord") +"]]></line_no__sord>");					
					valueXmlString.append("<lot_no><![CDATA["+ checkNull(rs.getString("lot_no")) +"]]></lot_no>");					
					valueXmlString.append("<lot_sl><![CDATA["+ checkNull(rs.getString("lot_sl")) +"]]></lot_sl>");					
					valueXmlString.append("<loc_code><![CDATA["+ checkNull(rs.getString("loc_code")) +"]]></loc_code>");					
					valueXmlString.append("<exp_lev><![CDATA["+ checkNull(rs.getString("exp_lev")) +"]]></exp_lev>");
					
					
										
					//valueXmlString.append("<rate><![CDATA["+rate +"]]></rate>");					
					valueXmlString.append("<item_code__ord><![CDATA["+ checkNull(rs.getString("item_code__ord")) +"]]></item_code__ord>");					
					itemCode=checkNull(rs.getString("item_code"));
					valueXmlString.append("<item_code><![CDATA["+itemCode+"]]></item_code>");
					if(itemCode.length() > 0){						
						 valueXmlString.append("<item_descr><![CDATA["+ checkNull(getColumnDescr(conn, "descr", "item", "item_code", itemCode)) +"]]></item_descr>");	
					}
					
					valueXmlString.append("<quantity><![CDATA["+ (rs.getDouble("quantity")) +"]]></quantity>");					
					valueXmlString.append("<unit><![CDATA["+ checkNull(rs.getString("unit"))+"]]></unit>");					
					valueXmlString.append("<conv__qty_stduom><![CDATA["+ (rs.getDouble("conv__qty_stduom")) +"]]></conv__qty_stduom>");
					
					
					//valueXmlString.append("<unit__rate><![CDATA["+ (rs.getDouble("unit__rate")) +"]]></unit__rate>");				
					valueXmlString.append("<tax_class><![CDATA["+checkNull(rs.getString("tax_class")) +"]]></tax_class>");				
					valueXmlString.append("<tax_chap><![CDATA["+checkNull(rs.getString("tax_chap")) +"]]></tax_chap>");				
					valueXmlString.append("<tax_env><![CDATA["+ checkNull(rs.getString("tax_env"))+"]]></tax_env>");					
					valueXmlString.append("<rate__clg><![CDATA["+ (rs.getDouble("rate__clg")) +"]]></rate__clg>");					
					//valueXmlString.append("<tot_net_amt><![CDATA["+ netTotAmt.trim()+"]]></tot_net_amt>");					
					valueXmlString.append("<site_code><![CDATA["+ checkNull(rs.getString("site_code"))+"]]></site_code>");	
					valueXmlString.append("<site_code__mfg><![CDATA["+ checkNull(rs.getString("site_code__mfg")) +"]]></site_code__mfg>");
					valueXmlString.append("<quantity__stduom><![CDATA["+ (rs.getDouble("quantity__stduom"))+"]]></quantity__stduom>");					
					//valueXmlString.append("<pack_code><![CDATA["+  checkNull(rs.getString("pack_code"))+"]]></pack_code>");
					valueXmlString.append("<unit__std><![CDATA["+ checkNull(rs.getString("unit__std"))+"]]></unit__std>");
				
					valueXmlString.append("<no_art><![CDATA["+ (rs.getDouble("no_art")) +"]]></no_art>");
					valueXmlString.append("<rate__stduom><![CDATA["+ (rs.getDouble("rate__stduom")) +"]]></rate__stduom>");
					valueXmlString.append("<conv__rtuom_stduom><![CDATA["+ (rs.getDouble("conv__rtuom_stduom")) +"]]></conv__rtuom_stduom>");
					//valueXmlString.append("<tot_net_amt><![CDATA["+ netAmt +"]]></tot_net_amt>");
					//valueXmlString.append("<quantity_inv><![CDATA["+ quantity +"]]></quantity_inv>");
					valueXmlString.append("<quantity_real><![CDATA["+ (rs.getDouble("quantity_real")) +"]]></quantity_real>");
					
					mfgDate=rs.getDate("mfg_date");
					expiryDate=rs.getDate("exp_date");
					if(mfgDate!=null){
					valueXmlString.append("<mfg_date>").append("<![CDATA[").append(sdf.format(mfgDate).toString()).append("]]>").append("</mfg_date>");
					}
					if(expiryDate !=null){
					valueXmlString.append("<exp_date>").append("<![CDATA[").append(sdf.format(expiryDate).toString()).append("]]>").append("</exp_date>");
					}
					valueXmlString.append("<pending_qty>").append("<![CDATA[").append((rs.getDouble("pending_qty"))).append("]]>").append("</pending_qty>");
					valueXmlString.append("<quantity__ord>").append("<![CDATA[").append((rs.getDouble("quantity__ord"))).append("]]>").append("</quantity__ord>");
					
					valueXmlString.append("<gross_weight>").append("<![CDATA[").append((rs.getDouble("gross_weight"))).append("]]>").append("</gross_weight>");
					valueXmlString.append("<nett_weight>").append("<![CDATA[").append((rs.getDouble("nett_weight"))).append("]]>").append("</nett_weight>");
					valueXmlString.append("<tare_weight>").append("<![CDATA[").append((rs.getDouble("tare_weight"))).append("]]>").append("</tare_weight>");
					valueXmlString.append("<dimension><![CDATA["+ checkNull(rs.getString("dimension")) +"]]></dimension>");
					valueXmlString.append("<pallet_wt><![CDATA["+ (rs.getDouble("pallet_wt")) +"]]></pallet_wt>");
					
					
					valueXmlString.append("<chg_user><![CDATA["+ checkNull(rs.getString("chg_user")) +"]]></chg_user>");
					valueXmlString.append("<chg_term><![CDATA["+checkNull(rs.getString("chg_term")) +"]]></chg_term>");
					
					
					//for hold status Start
					
					sql = "select hold_qty from stock where site_code = ? and lot_sl = ? and quantity > 0";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,siteCode);
					pstmt1.setString(2,lotslL);

					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						holdQtyL=rs1.getDouble(1);
					}
					System.out.println("Edit mode holdQtyL---->>["+holdQtyL+"]");
					
					if(holdQtyL > 0){
						valueXmlString.append("<lot_status><![CDATA["+ lotStatus +"]]></lot_status>");
					}else{
						valueXmlString.append("<lot_status><![CDATA[]]></lot_status>");
					}
					//for hold status End
					
					valueXmlString.append("</Detail3>");
					}//end while
					if(rs!=null){
					rs.close();
					rs=null;
					}
					if(pstmt!=null){
					pstmt.close();
					pstmt=null;
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					String lineNoAdd="",lnNoSord="",quantity="",rate1="",conQtyStd="",itemDescr="",itemCodeOrd="";
					String unitRate="",taxClass="",taxChap="",taxEnv="",netTotAmt="",quntyStduom="",
							siteCodeDet="",unitStd="", noArt="", convRtuomStduom="",netAmt="",rateStduom="";
					System.out.println("Form 3 itm_default_add called BLANK.................");
					
					double rateClg=0;					
					/*Commented by Manoj dtd 07/11/2014 set lineno from dom
					 sql = "select max(line_no)  "
							+ "from despatchdet where sord_no = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,sorderNo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lineNo1 = rs.getInt(1);						
					}				
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;					
					 lineNo1++;*/
					 NodeList nodeList = dom2.getElementsByTagName("Detail3");
						System.out.println("sds"+nodeList.item(0).getNodeName());
						String domId =  nodeList.item(0).getAttributes().getNamedItem("domID").getNodeValue();
						System.out.println("domId=="+domId);
						
					 
				   valueXmlString.append("<Detail3 domID='" +domId+ "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
					valueXmlString.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>\r\n");
					
					
					valueXmlString.append("<desp_id><![CDATA[]]></desp_id>");
					valueXmlString.append("<lot_sl><![CDATA[]]></lot_sl>");		
					valueXmlString.append("<item_code><![CDATA[]]></item_code>");
					valueXmlString.append("<item_descr><![CDATA[]]></item_descr>");
					valueXmlString.append("<lot_status><![CDATA[]]></lot_status>");
					valueXmlString.append("<loc_code><![CDATA[]]></loc_code>");
					valueXmlString.append("<lot_no><![CDATA[]]></lot_no>");
					valueXmlString.append("<no_art><![CDATA[]]></no_art>");
					valueXmlString.append("<gross_weight><![CDATA[]]></gross_weight>");
					valueXmlString.append("<line_no><![CDATA["+ domId+"]]></line_no>");
					
					valueXmlString.append("</Detail3>");					
					
				}

				else if(currentColumn.trim().equalsIgnoreCase("lot_sl"))
				{
					String sOrdItemLineNo = "", expLvl ="" ;		// Added by Sandesh 29-Sep-2014
					String lineType = "",partNo = "";							// Added by Sandesh 6-Oct-2014
					double qtyOrd = 0;	
					String currDomStr = genericUtility.serializeDom(dom);					// Added by Sandesh 6-Oct-2014
					String lineNoAdd="",lnNoSord="",itemDescr="",itemCodeOrd="";
					String unitRate="",taxClass="",taxChap="",taxEnv="",netTotAmt="",avalyn="",siteCodeMfg="",
							siteCodeDet="",unitStd="",packInstr="",unit1="",unitStd1="",statusL="",qtyDetailStr="";
					String expLev="",qcLockValueL="";
					ArrayList qtyFact=new ArrayList();
					ArrayList rateValue=new ArrayList();
					System.out.println("Form 3 itm_default_add called.................");
					int noArt=0;
					DecimalFormat df = new DecimalFormat("#########.###");
					double rateClg=0,taxAmt=0,rateStd=0,quntyStduom=0,ordQuantity=0,rate1=0,convQtyStduom=0,rateStduom=0,balQty=0,looseQty=0,
							convRtuomStduom=0,quantity=0,inputQty=0,costRate=0,packQty=0,qtyPerArt=0,shipperQtyNew=0,integralQtyNew=0;
					DistCommon distCommon = new DistCommon();
					System.out.println("Form 3 lot_sl item change1111111222..............");
					System.out.println("EDIT fLAG----->>["+editFlag+"]");
					String lotSlL="",siteCodeL="",dimension="",holdQtyS="HOLD";
					double qtyStk=0,palletWt=0,cAllocQty=0,holdQty=0,allocQty=0,actualQty=0,netAmt=0,discountL=0, conv = 0;					
					lotSlL=checkNull(genericUtility.getColumnValue("lot_sl", dom2,"3"));
					System.out.println("lotSlL--->2222["+lotSlL+"]");
					siteCodeL=getColumnDescr(conn, "site_code", "sorder", "sale_order", sorderNo);
					avalyn=getColumnDescr(conn, "available_yn", "sorder", "sale_order", sorderNo);
					avalyn=avalyn==null ? "N" : avalyn.trim();
					sql = "select max(line_no)  "
							+ "from despatchdet where sord_no = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,sorderNo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lineNoAdd = checkNull(rs.getString(1));						
					}				
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;					
					

					sql = "select loc_code,lot_no,item_code,quantity,gross_weight,net_weight,tare_weight,qty_per_art,"
							+ "no_art,pack_instr,dimension,hold_qty,alloc_qty,site_code__mfg,mfg_date,exp_date,rate,"
							+ "retest_date from stock where site_code = ? and lot_sl = ? and quantity > 0";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,siteCodeL);
					pstmt1.setString(2,lotSlL);

					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						locCode =  checkNull(rs1.getString("loc_code"));
						lotNo = checkNull(rs1.getString("lot_no"));
						itemCode = checkNull(rs1.getString("item_code"));
						squantity = rs1.getDouble("quantity");
						grossWeight = rs1.getDouble("gross_weight");
						netWeight = rs1.getDouble("net_weight");
						tareWeight = rs1.getDouble("tare_weight");
						noOfArt = rs1.getDouble("no_art");
						holdQty = rs1.getDouble("hold_qty");
						allocQty = rs1.getDouble("alloc_qty");
						
						siteCodeMfg = rs1.getString("site_code__mfg")==null?"":rs1.getString("site_code__mfg").trim();
						
						siteCodeMfg= checkNull(rs1.getString("site_code__mfg"));
						mfgDate=rs1.getDate("mfg_date");
						expiryDate=rs1.getDate("exp_date");
						retestDate=rs1.getDate("retest_date");
						
						packInstr = checkNull(rs1.getString("pack_instr"));
						dimension = checkNull(rs1.getString("dimension"));
						costRate = rs1.getDouble("rate");
						qtyPerArt = rs1.getDouble("qty_per_art");
						
                    }
					pstmt1.close();
					rs1.close();
					pstmt1 = null;
					rs1 = null;
					System.out.println("holdQty123-------->>["+holdQty+"]");
					System.out.println("Stock_itemCode-------->>["+itemCode+"]");
					
					itemDescr=getColumnDescr(conn, "descr", "item", "item_code", itemCode);
					String domID1="1";		
					
					//-------- Added by Sandesh 29-Sep-2014 as per suggestion by Manoharan sir -------------
					
					sql = "select line_no,exp_lev,line_type from sorditem where sale_order = ? and item_code = ? and item_flag = 'I'";
					
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,sorderNo);
					pstmt1.setString(2,itemCode);

					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						sOrdItemLineNo = rs1.getString("line_no");
						expLvl = rs1.getString("exp_lev");
						lineType = rs1.getString("line_type");	// Added by Sandesh 6-Oct-2014
						
						System.out.println("Getting values from SORDITEM => line_no : "+sOrdItemLineNo+" exp_lev : "+expLvl);
						System.out.println("Values from SORDITEM => lineType : "+lineType);	// Added by Sandesh 6-Oct-2014
					}

					if(pstmt1!=null){
						pstmt1.close();
						pstmt1 = null;
					}
					if(rs1!=null){
						rs1.close();
						rs1 = null;
					}

					lineNo1++;
					System.out.println("lineNo1 lotSl----->>["+lineNo1+"]");
					System.out.println("squantity------>>["+squantity+"]");
					System.out.println("holdQty------>>["+holdQty+"]");
					System.out.println("allocQty------>>["+allocQty+"]");
					if("Y".equalsIgnoreCase(avalyn)){
						actualQty=squantity - (holdQty + allocQty);
					}else{	
						actualQty=squantity - allocQty;					
					}
					if (actualQty > 0) // 10/12/14 manoharan this condition added
					{
						valueXmlString.append("<Detail3 domID='"+lineNo1+"'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
						valueXmlString.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>\r\n");
						valueXmlString.append("<line_no__sord><![CDATA["+ sOrdItemLineNo+"]]></line_no__sord>");
						//setNodeValue( dom, "line_no__sord", sOrdItemLineNo);
						valueXmlString.append("<exp_lev><![CDATA["+ expLvl+"]]></exp_lev>");
						//setNodeValue( dom, "exp_lev", getAbsString(""+expLvl));

						System.out.println("after setting line_no__sord : "+sOrdItemLineNo+ " and exp_lev : "+expLvl);
						//----------------------------------------------------
						
										
						valueXmlString.append("<item_code><![CDATA["+ itemCode+"]]></item_code>");
						valueXmlString.append("<item_descr><![CDATA["+ itemDescr+"]]></item_descr>");
						
						valueXmlString.append("<item_code__ord><![CDATA["+ itemCode+"]]></item_code__ord>");
						
						valueXmlString.append("<loc_code><![CDATA["+ locCode+"]]></loc_code>");
						valueXmlString.append("<lot_no><![CDATA["+ lotNo+"]]></lot_no>");
						valueXmlString.append("<lot_sl><![CDATA["+ lotSlL +"]]></lot_sl>");
						valueXmlString.append("<no_art><![CDATA["+ noOfArt+"]]></no_art>");
						
						
						valueXmlString.append("<site_code__mfg><![CDATA["+ siteCodeMfg +"]]></site_code__mfg>");
						valueXmlString.append("<pack_instr><![CDATA["+ packInstr+"]]></pack_instr>");
						valueXmlString.append("<dimension><![CDATA["+ dimension+"]]></dimension>");
						
						valueXmlString.append("<chg_user><![CDATA["+ chgUser +"]]></chg_user>");
						valueXmlString.append("<chg_term><![CDATA["+ chgTerm +"]]></chg_term>");
						valueXmlString.append("<chg_date><![CDATA["+ currAppdate +"]]></chg_date>");					
						
						System.out.println("quantity actualQty------>>["+actualQty+"]");
						valueXmlString.append("<quantity><![CDATA["+ actualQty+"]]></quantity>");
						
						valueXmlString.append("<quantity_inv><![CDATA["+actualQty +"]]></quantity_inv>");
						valueXmlString.append("<quantity_real><![CDATA["+ actualQty +"]]></quantity_real>");
						
						//-------- Added by Sandesh 06-Oct-2014 -------------
						
						sql = "select sum(case when quantity is null then 0 else quantity end) "+
								" from sorditem where sale_order = ? and line_no = ? and line_type  <> 'B'";
						
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,sorderNo);
						pstmt1.setString(2,sOrdItemLineNo);

						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							qtyOrd = rs1.getDouble(1);
							
							System.out.println("Getting sum(qtyOrd) from SORDITEM => "+qtyOrd);
						}

						if(pstmt1!=null){
							pstmt1.close();
							pstmt1 = null;
						}
						if(rs1!=null){
							rs1.close();
							rs1 = null;
						}
						
						partNo = getColumnDescr(conn, "MFR_PART_NO", "item", "item_code", itemCode);
						partNo = partNo==null ? "":partNo;
						System.out.println("partNo : "+partNo+" for despatchdet from item "+itemCode);
						valueXmlString.append("<part_no><![CDATA["+ partNo+"]]></part_no>");
						
						
						//-------------------- 06-Oct-2014  -----------------
						
						//valueXmlString.append("<quantity__ord><![CDATA["+ actualQty +"]]></quantity__ord>");	// Commented sandesh 6-Oct-2014
						valueXmlString.append("<quantity__ord><![CDATA["+ qtyOrd +"]]></quantity__ord>");	// Commented sandesh 6-Oct-2014
						
						valueXmlString.append("<gross_weight><![CDATA["+ grossWeight+"]]></gross_weight>");
						valueXmlString.append("<nett_weight><![CDATA["+ netWeight+"]]></nett_weight>");
						valueXmlString.append("<tare_weight><![CDATA["+ tareWeight+"]]></tare_weight>");				
						
						
						if(mfgDate!=null){
						valueXmlString.append("<mfg_date>").append("<![CDATA[").append(sdf.format(mfgDate).toString()).append("]]>").append("</mfg_date>");
						}
						if(expiryDate !=null){
						valueXmlString.append("<exp_date>").append("<![CDATA[").append(sdf.format(expiryDate).toString()).append("]]>").append("</exp_date>");
						}
						if(retestDate !=null){
							valueXmlString.append("<retest_date>").append("<![CDATA[").append(sdf.format(retestDate).toString()).append("]]>").append("</retest_date>");
						}		
						
						qcLockValueL=checkNull(getColumnDescr(conn, "var_value", "disparm", "var_name", "QUARNTINE_LOCKCODE"));					
						//valueXmlString.append("<lock_code><![CDATA["+ qcLockValueL +"]]></lock_code>");		// Commented by Sandesh 
						statusL = " ";	// Added by Sandesh
						valueXmlString.append("<status><![CDATA["+ statusL +"]]></status>");
						valueXmlString.append("<cost_rate><![CDATA["+ costRate +"]]></cost_rate>");
						
						if(holdQty > 0){
							valueXmlString.append("<lot_status><![CDATA["+ holdQtyS +"]]></lot_status>");
						}else{
							valueXmlString.append("<lot_status><![CDATA[]]></lot_status>");
						}
						/*valueXmlString.append("<lot_sl protect=\"0\">").append("<![CDATA[" + lotSlL + "]]>").append("</item_code>");
						setNodeValue( dom2, "lot_sl", getAbsString(""+lotSlL));*/
						qtyDetailStr = "Shipper Quantity = " + shipperQtyNew +"  Integral Quantity = " + integralQtyNew + "  Loose Quantity = " + looseQty ;
						
						valueXmlString.append("<qty_details><![CDATA["+ qtyDetailStr +"]]></qty_details>");
						
						
						
						//sale order Data [START]
						
						//ADDED nEW							
						//String currDomStr = genericUtility.serializeDom(dom);
						System.out.println("currDomStr--------->>["+currDomStr+"]");
						sql = "select sorddet.line_no,"
								+ "sorditem.item_code,"
								+ "sorddet.item_code__ord,"
								+"sorddet.quantity as ord_qty, "
								+ "sorditem.quantity - sorditem.qty_desp as quantity,"
								+ "sorddet.rate as rate,"
								+ "sorddet.unit,"
								+ "sorddet.conv__qty_stduom,"
								+ "sorddet.unit__rate,"
								+"sorddet.status, "		   
								
								+ "sorddet.tax_amt,"
								+ "sorddet.tax_class,"
								+ "sorddet.tax_chap,"
								+ "sorddet.tax_env,"
								+ "sorddet.pack_code,"
								+ "sorddet.rate__clg,"
								+ "sorddet.quantity__stduom,"
								+ "sorddet.net_tot_amt,"
								+ "sorddet.net_amt,"
								+ "sorddet.site_code,"
								+ "sorddet.unit__std,"
								+ "sorddet.no_art,"
								+ "sorddet.rate__std,"
								+ "sorddet.rate__stduom,"
								+ "sorddet.conv__rtuom_stduom,"
								+ "sorddet.pack_qty,"
								+ "sorddet.discount, "  
								+ "sorddet.nature,"
								 +"sorditem.exp_lev "
								+ "from sorddet,sorditem where sorddet.sale_order = sorditem.sale_order "
								+ "and sorditem.line_no = sorddet.line_no"
								+" AND SORDITEM.SALE_ORDER = ?"
								+" AND sorddet.item_code = ?"
								+" AND SORDITEM.LINE_TYPE = ? "
								+" ORDER BY SORDITEM.LINE_NO, SORDITEM.EXP_LEV " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,sorderNo);
						pstmt.setString(2,itemCode);
						pstmt.setString(3,"I");
						rs = pstmt.executeQuery();
					   if( rs.next() )
					   {
						   lnNoSord = rs.getString("line_no");						  
							//itemCode = rs.getString("item_code") == null ? " " : rs.getString("item_code");						
							//itemCodeOrd = rs.getString("item_code__ord") == null ? " " : rs.getString("item_code__ord");						
							quantity = rs.getDouble("quantity");						
							ordQuantity = rs.getDouble("ord_qty");						
							rate1 = rs.getDouble("rate");					
							unit1 = checkNull(rs.getString("unit"));					
							unitStd1=checkNull(rs.getString("unit__std"));						
							convQtyStduom = rs.getDouble("conv__qty_stduom");						
							unitRate = rs.getString("unit__rate") == null ? " " : rs.getString("unit__rate");
						
							taxAmt=rs.getDouble("tax_amt");						
							taxClass = rs.getString("tax_class") == null ? " " : rs.getString("tax_class");						
							taxChap = rs.getString("tax_chap") == null ? " " : rs.getString("tax_chap");						
							taxEnv = rs.getString("tax_env") == null ? " " : rs.getString("tax_env");						
							packCode = rs.getString("pack_code") == null ? " " : rs.getString("pack_code");
						
							rateClg = rs.getDouble("rate__clg");						
							rateStd = rs.getDouble("rate__std");						
							quntyStduom = rs.getDouble("quantity__stduom");						
							netTotAmt = rs.getString("net_tot_amt") == null ? " " : rs.getString("net_tot_amt");						
							siteCodeDet = rs.getString("site_code") == null ? " " : rs.getString("site_code");						
							unitStd = rs.getString("unit__std") == null ? " " : rs.getString("unit__std");						
							noArt = rs.getInt("no_art");						
							rateStduom = rs.getDouble("rate__stduom");				
							convRtuomStduom = rs.getDouble("conv__rtuom_stduom");
							
							netAmt = rs.getDouble("net_amt");						
							packQty = rs.getDouble("pack_qty");						
							discountL=rs.getDouble("discount");						
							//expLev=checkNull(rs.getString("exp_lev"));	
							statusL=checkNull(rs.getString("status"));
							natureLinkType=checkNull(rs.getString("nature"));
							
					   } 
					   pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					  
						inputQty=actualQty;
						
					   //System.out.println("lnNoSord lotSl item change@#-------->>["+lnNoSord+"]"); 
					   if (!unit1.equals(unitStd1))
							//if (unit1 == null)
							{
								System.out.println("Calling getConvQuantity...........");
								System.out.println("unitStd1 :"+unitStd1+" \nunit1 :"+unit1+" \nitemCode :"+itemCode+" \nqtyStd :"+quantity+" \nconv :"+conv); 
								qtyFact = distCommon.getConvQuantityFact(unitStd1, unit1, itemCode, actualQty, conv, conn);
								System.out.println("qtyFact.get(1) :"+qtyFact.get(1));
								System.out.println("qtyFact.get(1) :"+qtyFact.get(2));
							}
							else
							{
								qtyFact.add(Integer.toString(1));
								qtyFact.add(Double.toString(actualQty));
							}
						System.out.println("qtyFact.size() :"+qtyFact.size());
						conv = (Double.parseDouble(qtyFact.get(0).toString()));
						convQtyStduom = (Double.parseDouble(qtyFact.get(0).toString())); //Gulzar - 23/01/08
						System.out.println("conv :"+conv);
						inputQty = (Double.parseDouble(qtyFact.get(1).toString()));		
						
						discountL=(discountL/100) * ( inputQty *  rateStduom ); 
					 
						double[] noArtInfo = getNoArt(siteCodeDet , custCode , itemCode , packCode , Double.parseDouble(qtyFact.get(1).toString()) , "B" , 0 , 0 , conn); 
						System.out.println("getNoArt111 :: "+noArtInfo[0]+" "+noArtInfo[1]+" "+noArtInfo[2]);
						
						if ( qtyPerArt > 0 )
						{
							noArt = new Double((Double.parseDouble(qtyFact.get(1).toString())) / qtyPerArt).intValue();
							System.out.println("noArt [qtyFact.get(1).toString())) / qtyPerArt)] ::"+noArt);
							//grossWeight = (df.parse(df.format(grossWtPerArt / qtyPerArt)).doubleValue()) * qtyStk;
							//tareWeight	= (df.parse(df.format(tareWtPerArt / qtyPerArt)).doubleValue()) * qtyStk;
							//netWeight	= df.parse(df.format(grossWeight - tareWeight)).doubleValue();
							/*System.out.println("grossWeight [if ( qtyPerArt > 0 )] ::"+grossWeight);
							System.out.println("tareWeight [if ( qtyPerArt > 0 )] ::"+tareWeight);
							System.out.println("netWeight [if ( qtyPerArt > 0 )] ::"+netWeight);*/
						}
						packQty = 0d; 
						if ( noArt == 0)
						{
							noArt = 1;
						}
						if (noArt > 0)
						{
							packQty = 0d;
							packQty = (Double.parseDouble(qtyFact.get(1).toString())) / noArt;
							packQty = df.parse(df.format(packQty)).doubleValue();
						}
						
						shipperQtyNew = noArtInfo[1];
						integralQtyNew = noArtInfo[2];
						System.out.println("bal Qty  (double) :: "+Double.parseDouble(qtyFact.get(1).toString()));
						double[] noArtInfo1 = getNoArt(siteCodeDet , custCode , itemCode , packCode , Double.parseDouble(qtyFact.get(1).toString()) , "S" , 0 , 0 , conn); 
						balQty = Double.parseDouble(qtyFact.get(1).toString()) - ( shipperQtyNew * noArtInfo1[0]);
						double[] noArtInfo2 = getNoArt(siteCodeDet , custCode , itemCode , packCode , balQty , "I" , 0 , 0 , conn); 
						integralQtyNew = noArtInfo2[2];

						shipperQtyNew = shipperQtyNew * noArtInfo1[0];
						integralQtyNew = integralQtyNew * noArtInfo2[0];
						
						looseQty = Double.parseDouble(qtyFact.get(1).toString()) - ( shipperQtyNew + integralQtyNew );					
					 
						System.out.println("lineNo@@@@--------->>["+lineNo1+"]");
						 
						valueXmlString.append("<desp_id><![CDATA["+ despIdNew +"]]></desp_id>");
						valueXmlString.append("<sord_no><![CDATA["+ sorderNo +"]]></sord_no>");	
						valueXmlString.append("<line_no><![CDATA["+ lineNo1+"]]></line_no>");
						//valueXmlString.append("<line_no__sord><![CDATA["+ lnNoSord +"]]></line_no__sord>");
						
									
							
						//valueXmlString.append("<exp_lev><![CDATA["+expLev +"]]></exp_lev>");					
											
						//valueXmlString.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");					
						//valueXmlString.append("<quantity><![CDATA["+ quantity.trim() +"]]></quantity>");					
											
						valueXmlString.append("<conv__qty_stduom><![CDATA["+ convQtyStduom +"]]></conv__qty_stduom>");					
						
						
						valueXmlString.append("<tax_amt><![CDATA["+taxAmt+"]]></tax_amt>");
						valueXmlString.append("<tax_class><![CDATA["+taxClass.trim() +"]]></tax_class>");				
						valueXmlString.append("<tax_chap><![CDATA["+taxChap +"]]></tax_chap>");				
						valueXmlString.append("<tax_env><![CDATA["+ taxEnv+"]]></tax_env>");					
						valueXmlString.append("<rate__clg><![CDATA["+ rateClg +"]]></rate__clg>");	
						//disc_amt,conf_diff_amt,cost_rate
						valueXmlString.append("<sorddet_rate><![CDATA["+rate1 +"]]></sorddet_rate>");			
						valueXmlString.append("<rate__std><![CDATA["+ rateStd +"]]></rate__std>");
						
						//valueXmlString.append("<line_type><![CDATA["+ natureLinkType +"]]></line_type>");	// Commented by Sandesh 2-Oct-2014 
						valueXmlString.append("<line_type><![CDATA["+ lineType +"]]></line_type>");	// Commented by Sandesh 2-Oct-2014
						
						
						valueXmlString.append("<tot_net_amt><![CDATA["+ netTotAmt.trim()+"]]></tot_net_amt>");					
						valueXmlString.append("<site_code><![CDATA["+ siteCodeDet.trim()+"]]></site_code>");
						
						//valueXmlString.append("<unit><![CDATA["+ unit+"]]></unit>");		// Commented by Sandesh 2-Oct-2014 
						valueXmlString.append("<unit><![CDATA["+ unit1+"]]></unit>");		// Added by Sandesh 2-Oct-2014 
						
						valueXmlString.append("<unit__rate><![CDATA["+ unitRate +"]]></unit__rate>");
						valueXmlString.append("<unit__std><![CDATA["+ unitStd+"]]></unit__std>");
						
						
						valueXmlString.append("<quantity__stduom><![CDATA["+ actualQty+"]]></quantity__stduom>");
						valueXmlString.append("<pack_qty><![CDATA["+ packQty+"]]></pack_qty>");
						valueXmlString.append("<pack_code><![CDATA["+ packCode.trim()+"]]></pack_code>");
						
						
						valueXmlString.append("<no_art><![CDATA["+ noArt +"]]></no_art>");
						
						
						rateValue = disCommon.convQtyFactor(unitRate,unitStd,itemCode,rate1,convRtuomStduom,conn);
						System.out.println("rateValue------>>["+rateValue+"]");
						
						valueXmlString.append("<conv__rtuom_stduom>").append(rateValue.get(0).toString()).append("</conv__rtuom_stduom>\r\n");
						//valueXmlString.append("<rate__stduom>").append(rateValue.get(1).toString()).append("</rate__stduom>\r\n");	// Commented by Sandesh 6-Oct-2014
						valueXmlString.append("<rate__stduom>").append(rateStduom).append("</rate__stduom>\r\n");						// Added by Sandesh 6-Oct-2014
							
						
						
						valueXmlString.append("<tot_net_amt><![CDATA["+ netAmt +"]]></tot_net_amt>");						
						
						valueXmlString.append("<disc_amt><![CDATA["+ discountL +"]]></disc_amt>");
					valueXmlString.append("</Detail3>");
						
					}
					//Addd New End
					//Sale order data [END]
					
					
					
					
				}


				break;

			} //end switch

			/*if(("lot_sl".equalsIgnoreCase(currentColumn)) || ("tran_code".equalsIgnoreCase(currentColumn)))
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
			}*/

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
					conn = null;
				}
				
			}
			catch(Exception d)
			{
				d.printStackTrace(); 
			}
		}
		System.out.println("Return item_change xml---->>["+valueXmlString.toString()+"]");
		return valueXmlString.toString();
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
	

	private String getCurrdateAppFormat() throws ITMException
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
			throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
		}
		return s;
	}
	protected int getDBRowCount(Connection conn, String table_name, String whrCondCol, String whrCondVal) throws ITMException
	{
		int count=0;				
		ResultSet rs=null;
		PreparedStatement pstmt = null;
		
		String sql="select count(1) from "+table_name+" where "+whrCondCol+" = ?";
		System.out.println("SQL in getDBRowCount method : "+sql);
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,whrCondVal);
			rs = pstmt.executeQuery();
			if(rs.next()){
			count = rs.getInt(1);
			}
			if(pstmt!=null){
			pstmt.close();
			pstmt = null;
			}
			if(rs!=null){
			rs.close();
			rs = null;
			}
		}
		
		catch(Exception ex){
			System.out.println("Exception In getDBRowCount method of DespatchWiz Class : "+ex.getMessage());
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("Return count from getDBRowCount ----->>["+count+"]");					
		return count;
	}
	private String insertNewRecord(String sorderId,int currentFormNo,Connection conn) throws ITMException
	{
		System.out.println("------in insertNewRecord method----------");
		 StringBuffer valueXmlString = new StringBuffer();
		 ResultSet rs=null;
		 PreparedStatement pstmt=null;
		 SimpleDateFormat sdf = null;
		 java.sql.Timestamp currDate = null;
		 int count=0;
		 double frtAmt=0,insurance=0;
		 String sql="",siteCode="",currencyCode="",exchRate1="",transMode="",currCodeFrt="",currCodeIns="",stanCode="",stanCodeDlv="";
		 String dlvCity="",dlvPin="",countCodeDlv="",tranCode="",stanCodeInit="",status="",statusRemarks="",custCode="",custCodeDlv="";
		 String custCodeBil="",siteCodeShip="",dlvAdd1="",dlvAdd2="",dlvAdd3="",siteCodeDesc="",tranName="",currAppdate="",availableYn="",
				 remarks2="",remarks3="",remarks="",licenceNo1="",licenceNo2="",licenceNo3="",orderType="",gpSer="",confirmed="N";
		 Date orderDt=null,statusDate=null,licenceDate1=null,licenceDate2=null,licenceDate3=null;
		System.out.println("--------no data found in Despatch-------------");
		System.out.println("chgUser567-------->>["+chgUser+"]");
		try{
			sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate = sdf.format(currDate);
		sql = "select order_type,item_ser,site_code,curr_code,exch_rate,tran_code, trans_mode,curr_code__frt,curr_code__ins,cust_code__dlv," +
			    "stan_code,order_date,exch_rate__frt,exch_rate__ins,fob_value,conf_date,chg_date,state_code__dlv,udf__str1,udf__str2," +
				"dlv_city,dlv_pin,count_code__dlv,tran_code,stan_code,stan_code__init,parent__tran_id,rev__tran,status_remarks,"
				+ "spec_reason,dist_route,status,status_date,cust_code,cust_code__dlv,available_yn,"
				+ "frt_amt,ins_amt,remarks,remarks2,remarks3,licence_date_1,licence_no_1,licence_date_2,licence_no_2,licence_date_3,licence_no_3,"
				+ "cust_code__bil,site_code__ship,dlv_add1,dlv_add2,dlv_add3"+
				" from sorder where sale_order = ? and confirmed = ?" ;
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,sorderId);
		pstmt.setString(2,"Y");
		rs = pstmt.executeQuery();
		if( rs.next() )
		{
			count = 1;
			orderType=checkNull(rs.getString("order_type"));
			siteCode = checkNull(rs.getString("site_code"));
			 currencyCode = checkNull(rs.getString("curr_code"));
			exchRate1 = checkNull(rs.getString("exch_rate"));
			transMode = checkNull(rs.getString("trans_mode"));
			currCodeFrt = checkNull(rs.getString("curr_code__frt"));
			currCodeIns = checkNull(rs.getString("curr_code__ins"));
			
			stanCode = checkNull(rs.getString("stan_code"));
			orderDt = rs.getDate("order_date");
			stanCodeDlv = checkNull(rs.getString("state_code__dlv"));
			dlvCity = checkNull(rs.getString("dlv_city"));
			dlvPin = checkNull(rs.getString("dlv_pin"));
			countCodeDlv = checkNull(rs.getString("count_code__dlv"));
			tranCode = checkNull(rs.getString("tran_code"));
			stanCodeInit = checkNull(rs.getString("stan_code__init"));
			status = checkNull(rs.getString("status"));
			statusRemarks = checkNull(rs.getString("status_remarks"));
			statusDate = rs.getDate("status_date");
			custCode=checkNull(rs.getString("cust_code"));
			custCodeDlv=checkNull(rs.getString("cust_code__dlv"));
			custCodeBil=checkNull(rs.getString("cust_code__bil"));
			siteCodeShip=checkNull(rs.getString("site_code__ship"));
			availableYn=checkNull(rs.getString("available_yn"));
			
			dlvAdd1=checkNull(rs.getString("dlv_add1"));
			dlvAdd2=checkNull(rs.getString("dlv_add2"));
			dlvAdd3=checkNull(rs.getString("dlv_add3"));
			
			remarks=checkNull(rs.getString("remarks"));
			remarks2=checkNull(rs.getString("remarks2"));
			remarks3=checkNull(rs.getString("remarks3"));
			frtAmt=rs.getDouble("frt_amt");
			insurance=rs.getDouble("ins_amt");
			
			licenceNo1=checkNull(rs.getString("licence_no_1"));
			licenceDate1=rs.getDate("licence_date_1");
			
			licenceNo2=checkNull(rs.getString("licence_no_2"));
			licenceDate2=rs.getDate("licence_date_2");
			
			licenceNo3=checkNull(rs.getString("licence_no_3"));
			licenceDate3=rs.getDate("licence_date_3");
			
		}
		if(rs!=null){
		rs.close();
		rs=null;
		}
		if(pstmt!=null){
		pstmt.close();
		pstmt=null;
		}
		valueXmlString.append("<Detail2 domID='" + count + "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
		valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\"/>\r\n");
		if(orderDt != null){
		valueXmlString.append("<desp_date><![CDATA["+ currAppdate +"]]></desp_date>");
		valueXmlString.append("<sord_date><![CDATA["+ sdf.format(orderDt).toString() +"]]></sord_date>");
		}
		valueXmlString.append("<sord_no><![CDATA["+ sorderId +"]]></sord_no>");
		valueXmlString.append("<cust_code><![CDATA["+ custCode +"]]></cust_code>");
		valueXmlString.append("<cust_code__dlv><![CDATA["+ custCodeDlv +"]]></cust_code__dlv>");
		valueXmlString.append("<cust_code__bil><![CDATA["+ custCodeBil +"]]></cust_code__bil>");
		valueXmlString.append("<stan_code><![CDATA["+ stanCode +"]]></stan_code>");
		valueXmlString.append("<trans_mode><![CDATA["+ transMode +"]]></trans_mode>");
		
		//valueXmlString.append("<lr_date><![CDATA["+ lrDateVal.toString() +"]]></lr_date>");
		//valueXmlString.append("<shipment_id><![CDATA["+ shipmentId.trim() +"]]></shipment_id>");
		valueXmlString.append("<curr_code__frt><![CDATA["+ currCodeFrt +"]]></curr_code__frt>");
		valueXmlString.append("<curr_code__ins><![CDATA["+ currCodeIns+"]]></curr_code__ins>");
		valueXmlString.append("<curr_code><![CDATA["+ currencyCode+"]]></curr_code>");
		valueXmlString.append("<exch_rate><![CDATA["+  exchRate1 +"]]></exch_rate>");
		valueXmlString.append("<exch_rate__frt><![CDATA["+  exchRate1 +"]]></exch_rate__frt>");	
		valueXmlString.append("<exch_rate__ins><![CDATA["+ exchRate1 +"]]></exch_rate__ins>");
		
		valueXmlString.append("<available_yn><![CDATA["+ availableYn +"]]></available_yn>");
		
		
		valueXmlString.append("<site_code__ship><![CDATA["+ siteCodeShip +"]]></site_code__ship>");
		siteCodeDesc=checkNull(getColumnDescr(conn, "descr", "site", "site_code", siteCodeShip));
		valueXmlString.append("<site_descr><![CDATA["+ siteCodeDesc+"]]></site_descr>");
		valueXmlString.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
		siteCodeDesc=checkNull(getColumnDescr(conn, "descr", "site", "site_code", siteCode));				
		valueXmlString.append("<descr><![CDATA["+ siteCodeDesc+"]]></descr>");
		
		valueXmlString.append("<state_code__dlv><![CDATA["+ stanCodeDlv.trim()+"]]></state_code__dlv>");		
		valueXmlString.append("<dlv_add1><![CDATA["+ dlvAdd1 +"]]></dlv_add1>");
		valueXmlString.append("<dlv_add2><![CDATA["+ dlvAdd2 +"]]></dlv_add2>");
		valueXmlString.append("<dlv_add3><![CDATA["+ dlvAdd3 +"]]></dlv_add3>");		
		valueXmlString.append("<dlv_city><![CDATA["+ dlvCity +"]]></dlv_city>");				
		valueXmlString.append("<dlv_pin><![CDATA["+ dlvPin +"]]></dlv_pin>");		
		valueXmlString.append("<count_code__dlv><![CDATA["+ countCodeDlv +"]]></count_code__dlv>");
		
		valueXmlString.append("<tran_code><![CDATA["+ tranCode +"]]></tran_code>");
		valueXmlString.append("<stan_code__init><![CDATA["+ stanCodeInit +"]]></stan_code__init>");
		valueXmlString.append("<status_remarks><![CDATA["+ statusRemarks +"]]></status_remarks>");
		status = " ";	// Added by sandesh
		valueXmlString.append("<status><![CDATA["+ status +"]]></status>");
		tranName=getColumnDescr(conn,"tran_name" , "transporter", "tran_code", tranCode);
		valueXmlString.append("<tran_name><![CDATA["+ tranName +"]]></tran_name>");
		
		valueXmlString.append("<remarks><![CDATA["+ remarks +"]]></remarks>");
		valueXmlString.append("<remarks2><![CDATA["+ remarks2 +"]]></remarks2>");
		valueXmlString.append("<remarks3><![CDATA["+ remarks3 +"]]></remarks3>");
		
		//valueXmlString.append("<freight><![CDATA["+ frtAmt +"]]></freight>");			// Commented by Sandesh 2-Oct-2014 
		valueXmlString.append("<freight><![CDATA[0]]></freight>");					// Added by Sandesh 2-Oct-2014 
		valueXmlString.append("<insurance><![CDATA["+ insurance +"]]></insurance>");
		
		valueXmlString.append("<confirmed><![CDATA["+ confirmed +"]]></confirmed>");
		;
		
		
		if(licenceNo1.length() == 0){
			sql="select c.drug_lic_no_1,c.drug_licno1_upto from sorder s,customer c	"
					+ "where s.cust_code__dlv = c.cust_code and s.sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,sorderId);
			rs=pstmt.executeQuery();
			if(rs.next()){
				licenceNo1=checkNull(rs.getString(1));
				licenceDate1=rs.getDate(2);
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
		if(licenceNo2.length() == 0){
			sql="select c.drug_lic_no_1,c.drug_licno1_upto from sorder s,customer c	"
					+ "where s.cust_code__dlv = c.cust_code and s.sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,sorderId);
			rs=pstmt.executeQuery();
			if(rs.next()){
				licenceNo2=checkNull(rs.getString(1));
				licenceDate2=rs.getDate(2);
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
		
		
		valueXmlString.append("<licence_no_1><![CDATA["+ licenceNo1 +"]]></licence_no_1>");
		valueXmlString.append("<licence_no_2><![CDATA["+ licenceNo2 +"]]></licence_no_2>");
		valueXmlString.append("<licence_no_3><![CDATA["+ licenceNo3 +"]]></licence_no_3>");
		
		if(licenceDate1!=null){		
			valueXmlString.append("<licence_date_1><![CDATA["+  sdf.format(licenceDate1).toString()+"]]></licence_date_1>");
		}	
		if(licenceDate2!=null){		
			valueXmlString.append("<licence_date_2><![CDATA["+  sdf.format(licenceDate2).toString()+"]]></licence_date_2>");
		}	
		if(licenceDate3!=null){		
			valueXmlString.append("<licence_date_3><![CDATA["+  sdf.format(licenceDate3).toString()+"]]></licence_date_3>");
		}		
		
		gpSer=checkNull(getColumnDescr(conn, "gp_ser", "sordertype", "order_type", orderType));		
		
		
		valueXmlString.append("<gp_ser>").append("<![CDATA["+gpSer +"]]>").append("</gp_ser>");
		valueXmlString.append("<eff_date><![CDATA["+ currAppdate +"]]></eff_date>");
		valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>"); 
		valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
		valueXmlString.append("<chg_date>").append("<![CDATA[" +currAppdate +"]]>").append("</chg_date>");
		
		valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>"); 
		valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
		valueXmlString.append("<add_date>").append("<![CDATA[" + currAppdate + "]]>").append("</add_date>");
		
		valueXmlString.append("</Detail2>");
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString.toString();
	
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
	public String getColumnValueMy(String colName, Document dom, String formNo) throws ITMException 
	{
		Node elementName = null, parentNode = null;
		NodeList elementList = null;
		Element elementAttr = null;
		String columnName = "";
		String columnValue = null;
		boolean continueLoop = true;
		boolean isFormNo = false;
		int ctr;
		try
		{
			elementList = dom.getElementsByTagName(colName);
			
			if (Integer.parseInt(formNo) < 1)//parseInt takes the String type as input and treats the value as a particulas integer value.
			{
				isFormNo = true;
			}
			System.out.println("elementList length--->>"+ elementList.getLength());
			for(ctr = 0; ctr < elementList.getLength(); ctr++)
			{
				elementName = elementList.item(ctr);//item returns the indexed item in the collection
				parentNode = elementName.getParentNode();//get the parent node name
				elementAttr = (Element)parentNode;
				String domId=elementAttr.getAttribute("domID");
				return domId;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : [GenericUtility][getColumnValue(2)] :==>\n"+e.getMessage());
			//throw new BaseException(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return columnValue;
	}
	
	private double getQtyFromSordItem(String sorderNo,Connection conn) throws ITMException
	{
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		String sql="";
		double actualQty=0;
		try{
			sql="select quantity - qty_desp as actual from sorditem where sale_order = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sorderNo);
			rs=pstmt.executeQuery();
			if(rs.next()){
				actualQty=rs.getDouble(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("actualQty---->>["+actualQty+"]");
		return actualQty;
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
				lockListL.add(checkNull(rs.getString(1)));				
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
	public String checkNull(String str)
	{
		if(str == null){
			str="";
		}
		return str.trim();
	}
	private double[] getNoArt(String siteCode , String custCode , String itemCode , String packCode , double qty , String qtyType , double shipperQty , double integralQty, Connection conn) throws ITMException
	{

		String tempSql = "";
		String tempSql1 = "";
		String tempSql2 = "";
		ResultSet rsTemp = null;
		ResultSet rsTemp1 = null;
		ResultSet rsTemp2 = null;

		Statement stmt = null;
		double capacity = 0d;
		double reoQty = 0d;
		double shipperQty1 = 0d;
		double remainder = 0d;
		double integralQty1 = 0d;
		double result[] = new double[3];
		double noArt1 = 0d, noArt2 = 0d, noArt = 0d;
		try
		{
			stmt = conn.createStatement();
			if (qtyType.equals("S"))
			{
				tempSql = "SELECT (CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END) AS CAPACITY FROM PACKING WHERE PACK_CODE = '" + packCode + "'";
				System.out.println("SQL :: "+tempSql);
				rsTemp = stmt.executeQuery(tempSql);
				if (rsTemp.next())
				{
					capacity = rsTemp.getDouble("CAPACITY");
				}
				else
				{
					capacity = 0d;
				}
				tempSql = "";
				rsTemp.close();

				tempSql = "SELECT  REO_QTY " + 
							" FROM SITEITEM WHERE SITE_CODE = '" + siteCode + "' " +  //by jaimin 
							" AND ITEM_CODE = '" + itemCode + "' ";
				System.out.println("SQL :: "+tempSql);
				rsTemp = stmt.executeQuery(tempSql);
				if (rsTemp.next())
				{
					reoQty = rsTemp.getDouble("REO_QTY");
				}
				else
				{
					tempSql1 = "SELECT REO_QTY FROM ITEM  WHERE ITEM_CODE = '"+ itemCode +"' ";
					System.out.println("SQL :: "+tempSql1);
					rsTemp1 = stmt.executeQuery(tempSql1);
					if (rsTemp1.next())
					{
						reoQty = rsTemp1.getDouble("REO_QTY");
					}
					rsTemp1.close();
					tempSql1 = "";
				}
				tempSql = "";
				rsTemp.close();

				if ( capacity > 0 )
				{
					shipperQty1 = capacity;
				}
				else
				{
					shipperQty1 = reoQty;
				}
				if ( shipperQty1 > 0 )
				{
					noArt = (qty - (qty % shipperQty1)) / shipperQty1;
				}
				else
				{
					noArt =1;
				}
			}		
			else if ( qtyType.equals("I") )
			{
				tempSql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM WHERE CUST_CODE = '"+ custCode +"' "
							 +" AND ITEM_CODE = '"+ itemCode +"' ";
				System.out.println("SQL :: "+tempSql);
				rsTemp = stmt.executeQuery(tempSql);
				if (rsTemp.next())
				{
					integralQty1 = rsTemp.getDouble("INTEGRAL_QTY");
				}
				else
				{
					tempSql1 = "SELECT INTEGRAL_QTY FROM SITEITEM WHERE SITE_CODE = '"+ siteCode +"' AND "
									+" ITEM_CODE = '"+ itemCode +"' ";
					System.out.println("SQL :: "+tempSql1);
					rsTemp1 = stmt.executeQuery(tempSql1);
					if (rsTemp1.next())
					{
						integralQty1 = rsTemp1.getDouble("INTEGRAL_QTY");
					}
					else
					{
						tempSql2 = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+ itemCode +"' ";
						System.out.println("SQL :: "+tempSql2);
						rsTemp2 = stmt.executeQuery(tempSql2);
						if ( rsTemp2.next() )
						{
							integralQty1 = rsTemp2.getDouble("INTEGRAL_QTY");
						}
						rsTemp2.close();
						tempSql2 = "";
					}
					rsTemp1.close();
					tempSql1 = "";
				}
				tempSql = "";
				rsTemp.close();
				
				if ( integralQty1 > 0 )
				{
					noArt = (qty - (qty % integralQty1)) / integralQty1;
				}
				else
				{
					noArt = 1;
				}
			}
			else if ( qtyType.equals("B") )
			{
				tempSql = "SELECT (CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END) AS CAPACITY FROM PACKING WHERE PACK_CODE = '"+ packCode +"'";
				System.out.println("SQL :: "+tempSql);
				rsTemp = stmt.executeQuery(tempSql);
				if (rsTemp.next())
				{
					capacity = rsTemp.getDouble("CAPACITY");
				}
				else
				{
					capacity = 0d;
				}
				tempSql = "";
				rsTemp.close();

				tempSql = "SELECT REO_QTY "
							 +" FROM SITEITEM "
							 +" WHERE SITE_CODE = '"+ siteCode +"' " //jaimin
							 +" AND ITEM_CODE = '"+ itemCode +"' ";
				System.out.println("SQL :: "+tempSql);
				rsTemp = stmt.executeQuery(tempSql);
				if (rsTemp.next())
				{
					reoQty = rsTemp.getDouble("REO_QTY");
				}
				else
				{
					tempSql1 = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = '"+ itemCode +"' ";
					System.out.println("SQL :: "+tempSql1);
					rsTemp1 =stmt.executeQuery(tempSql1);
					if (rsTemp1.next())
					{
						reoQty = rsTemp1.getDouble("REO_QTY");
					}
					rsTemp1.close();
					tempSql1 = "";
				}
				tempSql = "";
				rsTemp.close();


				if ( capacity > 0 )
				{
					shipperQty1 = capacity;
				}
				else
				{
					shipperQty1 = reoQty;
				}
				if ( shipperQty1 > 0 )
				{
					noArt1 = (qty - (qty % shipperQty1)) / shipperQty1;
					remainder = ( qty % shipperQty1 );
				}

				tempSql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM WHERE CUST_CODE = '"+ custCode +"' "
							+" AND ITEM_CODE = '"+ itemCode +"' ";
				System.out.println("SQL :: "+tempSql);
				rsTemp = stmt.executeQuery(tempSql);
				if (rsTemp.next())
				{
					integralQty1 = rsTemp.getDouble("INTEGRAL_QTY");
				}
				else
				{
					tempSql1 =  "SELECT INTEGRAL_QTY "
						+"  FROM SITEITEM WHERE SITE_CODE = '"+ siteCode +"' "
						+" 	AND ITEM_CODE = '"+ itemCode +"' ";
					System.out.println("SQL :: "+tempSql1);
					rsTemp1 = stmt.executeQuery(tempSql1);
					if ( rsTemp1.next() )
					{
						integralQty1 = rsTemp1.getDouble("INTEGRAL_QTY");
					}
					else
					{
						tempSql2 = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+ itemCode +"' ";
						System.out.println("SQL :: "+tempSql2);
						rsTemp2 = stmt.executeQuery(tempSql1);
						if ( rsTemp2.next() )
						{
							integralQty1 = rsTemp2.getDouble("INTEGRAL_QTY");
						}
						tempSql2 = "";
						rsTemp2.close();
					}
					tempSql1 = "";
					rsTemp1.close();
				}
				if ( integralQty1 > 0 )
				{
					noArt2 = (remainder - (remainder % integralQty1)) / integralQty1;
				}
				if ( noArt2 > 0 )
				{
					noArt2 = 1;
				}
				noArt = noArt1 + noArt2;
			}
			else
			{
				noArt = 0;
			}			
		}
		catch (Exception e)
		{
			System.out.println("Exception in getNoArt :: "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		result[0] = noArt;
		result[1] = shipperQty1;
		result[2] = integralQty1;
		return result;
	
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
}
