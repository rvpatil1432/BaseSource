/********************************************************
	Title : StockDeAllocIC[W14CSUN004]
	Date  : 09/06/2014
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3


@Stateless // added for ejb3

public class StockDeAllocIC extends ValidatorEJB implements StockDeAllocICLocal, StockDeAllocICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	//method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}

	//method for validation
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String siteCode = "";
		String tranDate = "";
		String saleOrder = "";
		String itemCode = "";
		String lotNo = "";
		String lotSl = "";
		String locCode = "";
		String lineNoSord = "";
		String expLev = "";
		String saleOrderHdr = "",tranId="",keyFlag="";
        String batchId = "";
		int ctr=0;
		int cnt = 0;
		int cnt1 = 0;
		int ct=0;
		int currentFormNo = 0;
		int childNodeListLength;

		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String siteCodeShip="";       
		String activePickAllow="";
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					 if(childNodeName.equalsIgnoreCase("tran_date"))
					{    
						tranDate = genericUtility.getColumnValue("tran_date", dom);
						if(tranDate == null || tranDate.trim().length() == 0)
						{
							errCode = "VTTRAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase()); 
						}

					}
					else if(childNodeName.equalsIgnoreCase("site_code"))
					{ 
						siteCode = genericUtility.getColumnValue("site_code", dom);
						if(siteCode == null || (siteCode.length() == 0))
						{
							errList.add("VMSITECOD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCode != null && (siteCode.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}	
						}
					}else if(childNodeName.equalsIgnoreCase("tran_id"))
					{   
						tranId = checkNull(genericUtility.getColumnValue("tran_id", dom));
						System.out.println("tranId="+tranId);

						sql = "select key_flag from transetup where tran_window='w_sorddealloc'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							keyFlag = rs.getString("key_flag");									
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if("M".equalsIgnoreCase(keyFlag) && "A".equalsIgnoreCase(editFlag))
						{
							if(tranId == null || tranId.trim().length() == 0  )
							{
								errCode = "INVTRANID1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = " select count (*) from sord_alloc where tran_id = ?   ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									ct = rs.getInt(1);	
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								System.out.println("@@ct>>>>>>>>>>"+ct);
								if(ct > 0)
								{
									errCode = "VTDUPTRNID";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}

						}


					}else if(childNodeName.equalsIgnoreCase("sale_order"))
					{    
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						if(saleOrder == null || saleOrder.trim().length() == 0)
						{
							errCode = "VMSORD2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from sorder where sale_order = ?  and confirmed='Y'  and  status= (case when status is null then 'P' else status end) ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VMSORD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("site_code__ship"))
					{ 
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom);
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						if(siteCodeShip == null || (siteCodeShip.length() == 0))
						{
							errList.add("VMSITECOD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCodeShip != null && (siteCodeShip.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCodeShip);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
							else
							{
								sql = " select count(*) from sord_alloc where sale_order= ? and site_code__ship=? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,saleOrder);
								pstmt.setString(2,siteCodeShip);
								rs = pstmt.executeQuery();
								if(rs.next())
								{	
									cnt1 =  rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt1 == 0) 
								{
									errCode = "INVSITSHIP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}

							}
								
						}
					}
					else if(childNodeName.equalsIgnoreCase("active_pick_allow"))
					{
						activePickAllow = genericUtility.getColumnValue("active_pick_allow", dom);
						System.out.println("activePickAllow----"+activePickAllow);
						if(activePickAllow==null || activePickAllow.length()==0 || ((!"Y".equalsIgnoreCase(activePickAllow)) && (!"N".equalsIgnoreCase(activePickAllow))))
						{
							errCode = "VTINVPICK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						
					}
					 if(childNodeName.equalsIgnoreCase("batch_id"))//added by vishakha on 23-JaN-2015 for D14JSUN006
						{
							batchId = genericUtility.getColumnValue("batch_id", dom);
							System.out.println("batch_id----"+batchId);
							if(batchId != null && batchId.trim().length() > 0 )
							{
							sql = "select count(*) from sord_alloc where batch_id = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,batchId);
							rs=pstmt.executeQuery();
							if(rs.next()){
								cnt1=rs.getInt(1);
							}
							if(rs!=null){
							rs.close();
							rs=null;
							}
							if(pstmt!=null){
							pstmt.close();
							pstmt=null;
							}
							System.out.println("Count value for BATCH ID -->"+cnt1);	
							if(cnt1 == 0){	errCode = "VMDBTCH";
						
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
							}
							}
							}
							
				}
				break;

			case 2 : 
				parentNodeList = dom1.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				saleOrderHdr = genericUtility.getColumnValue("sale_order", dom1 );
				siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
				System.out.println("saleOrderHdr==*****==>"+saleOrderHdr+"::::siteCodeShip===>"+siteCodeShip);
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("sale_order"))
					{    
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						if(saleOrder == null || saleOrder.trim().length() == 0)
						{
							errCode = "VMSORD2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from sorder where sale_order = ?  and confirmed='Y'  and  status= (case when status is null then 'P' else status end) ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VMSORD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}	
							if(!(saleOrder.equalsIgnoreCase(saleOrderHdr)))
							{
								errCode = "VMSORDER";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("site_code"))
					{ 
						siteCode = genericUtility.getColumnValue("site_code", dom);
						if(siteCode == null || (siteCode.length() == 0))
						{
							errList.add("VMSITECOD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCode != null && (siteCode.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("item_code"))
					{    
						itemCode = genericUtility.getColumnValue("item_code", dom);
						if(itemCode == null || itemCode.trim().length() == 0)
						{
							errCode = "VMITMC2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VMITMC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("loc_code"))
					{    
						locCode = genericUtility.getColumnValue("loc_code", dom);
						if(locCode == null || locCode.trim().length() == 0)
						{
							errCode = "VTLOCN2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from location where loc_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,locCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VTLOCN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}

					}
					else if(childNodeName.equalsIgnoreCase("line_no__sord"))
					{
						lineNoSord = genericUtility.getColumnValue("line_no__sord", dom);
						if(lineNoSord == null || lineNoSord.trim().length() == 0)
						{
							errCode = "VTSORDLINE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					else if(childNodeName.equalsIgnoreCase("exp_lev"))
					{
						expLev = genericUtility.getColumnValue("exp_lev", dom);
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						lineNoSord = genericUtility.getColumnValue("line_no__sord", dom);
						if(expLev == null || expLev.trim().length() == 0)
						{
							errCode = "VTEXPL2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							if(lineNoSord != null && lineNoSord.trim().length() > 0)
							{
								lineNoSord = "   " + lineNoSord;
								lineNoSord = lineNoSord.substring( lineNoSord.length()-3 );

								sql = " select count(*) from sorditem where sale_order = ? and line_no = ? and exp_lev = ? and line_type = 'I' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,saleOrder);
								pstmt.setString(2,lineNoSord);
								pstmt.setString(3,expLev);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt =  rs.getInt(1);
								}
								rs.close(); rs = null;
								pstmt.close(); pstmt = null;
								if(cnt == 0) 
								{
									errCode = "VTEXPL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
							}
						}
					}

					else if(childNodeName.equalsIgnoreCase("lot_no"))
					{    
						lotNo = genericUtility.getColumnValue("lot_no", dom);

						if(lotNo == null || lotNo.trim().length() == 0)
						{
							errCode = "VTLOTNO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					else if(childNodeName.equalsIgnoreCase("lot_sl"))
					{    
						lotSl = genericUtility.getColumnValue("lot_sl", dom);

						if(lotSl == null || lotSl.trim().length() == 0)
						{
							errCode = "VTLOTSL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						
					}
					else if(childNodeName.equalsIgnoreCase("quantity"))
					{
						String pendingQtyDom = "";
						String quantityDom = "";
						quantityDom = genericUtility.getColumnValue("quantity", dom);
						pendingQtyDom = genericUtility.getColumnValue("pending_qty", dom);
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						lineNoSord = genericUtility.getColumnValue("line_no__sord", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						lotNo = genericUtility.getColumnValue("lot_no", dom);
						lotSl = genericUtility.getColumnValue("lot_sl", dom);
						if ( quantityDom == null || quantityDom.trim().length() == 0  )
						{
							quantityDom = "0";
						}
						if( Double.parseDouble(quantityDom) == 0 )
						{
							errCode = "VTQUANTITY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(Double.parseDouble(quantityDom) > Double.parseDouble(pendingQtyDom) )
						{
							errCode = "VTSORDQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				break;

			}//end switch
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
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
		}//end try
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
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
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("XML STRING======>----------------------------");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [JvVal][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		String childNodeName = null;
		String siteCode = "";
		String itemCode = "";
		String itemDescr = "";
		String dateNow = "";
		String saleOrder = "";
		String lineNoSord = "";
		String expLev = "";
		String custCode = "";
		String custDescr = "";
		String chgUser = "";
		String chgTerm = "";
		String siteDescr = "";
		String locCode = "";
		String locDescr = "";
		
		String lotNo = "";
		String sSQL = "";
		int ctr = 0;
		int currentFormNo = 0;
		String siteCodeShip="",siteCodeShipDescr="",unit="", unitStd ="",columnValue="";  
		double orderQuantity = 0.0,convQtyStduom = 0.0,quantityStduom = 0.0,pendingQty = 0.0,qty = 0.0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		String activePickAllow="",getDataSql1="",custCodeHdr="",lotSL = "", waveFlag = "";
		try
		{ 
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());

			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}

			siteCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));

			siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
			chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
			
			dateNow = simpleDateFormat.format(currentDate.getTime());

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
			case 1 : 
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if(childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();  
						}
					}
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("@@@@@@  itm_default itemchange called");
					System.out.println("@@@@@@ 2 siteCode["+siteCode+"]::::::siteDescr["+siteDescr+"]");
					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");
					valueXmlString.append("<tran_date>").append("<![CDATA[" + dateNow + "]]>").append("</tran_date>");
					valueXmlString.append("<add_date>").append("<![CDATA[" + dateNow + "]]>").append("</add_date>");
					valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
					valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
					valueXmlString.append("<chg_date>").append("<![CDATA[" + dateNow + "]]>").append("</chg_date>");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");

				}//end of if
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					activePickAllow=genericUtility.getColumnValue("active_pick_allow", dom);
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					valueXmlString.append("<active_pick_allow protect = \"1\">").append("<![CDATA[" +  activePickAllow + "]]>").append("</active_pick_allow>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("sale_order"))
				{
					System.out.println("@@@@@@  sale order itemchange called");
					saleOrder = genericUtility.getColumnValue("sale_order", dom);
					custCode = findValue(conn, "cust_code" ,"sorder", "sale_order", saleOrder);
					valueXmlString.append("<cust_code>").append("<![CDATA[" +  custCode + "]]>").append("</cust_code>");
					custDescr = findValue(conn, "cust_name" ,"customer", "cust_code", custCode);
					valueXmlString.append("<cust_name>").append("<![CDATA[" +  custDescr + "]]>").append("</cust_name>");
					siteCodeShip = findValue(conn, "site_code__ship" ,"sorder", "sale_order", saleOrder);
					siteCodeShipDescr = findValue(conn, "descr" ,"site", "site_code", siteCodeShip);
					System.out.println("@@@@@ siteCodeShip["+siteCodeShip+"]siteCodeShipDescr["+siteCodeShipDescr+"]");
					valueXmlString.append("<site_code__ship>").append("<![CDATA[" +  siteCodeShip + "]]>").append("</site_code__ship>");
					valueXmlString.append("<site_ship_descr>").append("<![CDATA[" +  siteCodeShipDescr + "]]>").append("</site_ship_descr>");
					sSQL = " SELECT   W.ACTIVE_PICK_ALLOW " +							
							" FROM SORDER S, CUSTOMER C, WAVE_TYPE W  WHERE SALE_ORDER = ? " +										
							" AND C.CUST_CODE = S.CUST_CODE__DLV " +
							" AND C.WAVE_TYPE = W.WAVE_TYPE ";
					pstmt=conn.prepareStatement(sSQL);
					pstmt.setString(1,saleOrder);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						activePickAllow=rs.getString(1);
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					valueXmlString.append("<active_pick_allow>").append("<![CDATA[" +  activePickAllow + "]]>").append("</active_pick_allow>");
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code__ship"))
				{
					System.out.println("@@@@@@  site_code__ship itemchange called");
					siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom);
					siteCodeShipDescr = findValue(conn, "descr" ,"site", "site_code", siteCodeShip);
					System.out.println("@@@@@ siteCodeShip["+siteCodeShip+"]siteCodeShipDescr["+siteCodeShipDescr+"]");
					valueXmlString.append("<site_ship_descr>").append("<![CDATA[" +  siteCodeShipDescr + "]]>").append("</site_ship_descr>");
				}
				
				valueXmlString.append("</Detail1>");
				break;       

			case 2 : 
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if(childNode.getFirstChild() != null)
						{
						}
					}
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
				System.out.println("siteCodeShip----"+siteCodeShip);
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					saleOrder = genericUtility.getColumnValue("sale_order", dom1, "1");
					//siteCode = genericUtility.getColumnValue("site_code", dom1, "1");
					siteCode = genericUtility.getColumnValue("site_code__ship", dom1, "1");//Done by Manoj dtd 03/09/2014 Set SiteCode Ship
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
					valueXmlString.append("<sale_order>").append("<![CDATA[" +  saleOrder + "]]>").append("</sale_order>");
					valueXmlString.append("<site_code protect = \"1\">").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");
					valueXmlString.append("<quantity>").append("<![CDATA[0]]>").append("</quantity>");

				}
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode = genericUtility.getColumnValue("item_code", dom);
					itemDescr = findValue(conn, "descr" ,"item", "item_code", itemCode);
					valueXmlString.append("<item_descr>").append("<![CDATA[" +  itemDescr + "]]>").append("</item_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = genericUtility.getColumnValue("site_code", dom);
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
					valueXmlString.append("<site_descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("loc_code"))
				{
					locCode = genericUtility.getColumnValue("loc_code", dom);
					locDescr = findValue(conn, "descr" ,"location", "loc_code", locCode);
					valueXmlString.append("<location_descr>").append("<![CDATA[" +  locDescr + "]]>").append("</location_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("sale_order") || currentColumn.trim().equalsIgnoreCase("line_no__sord") )
				{
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
					lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					expLev = checkNull(genericUtility.getColumnValue("exp_lev", dom));
					activePickAllow=genericUtility.getColumnValue("active_pick_allow", dom1);
					
					custCodeHdr=genericUtility.getColumnValue("cust_code", dom1);
					System.out.println("custCodeHdr["+custCodeHdr+"]");
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					lineNoSord = "   " + lineNoSord.trim();
					lineNoSord = lineNoSord.substring( lineNoSord.length()-3 );
					
					if (lineNoSord.trim().length() > 0 )
					{
						valueXmlString.append("<line_no__sord>").append("<![CDATA[" +  lineNoSord + "]]>").append("</line_no__sord>");
					}
					if (saleOrder.trim().length() > 0 && lineNoSord.trim().length() > 0 )
					{
						getDataSql1 = "SELECT SORDITEM.EXP_LEV ,"
								+" SORDITEM.ITEM_CODE,ITEM.DESCR,SORDITEM.QUANTITY,"
								+" SORDALLOC.QTY_ALLOC   PENDING_QUANTITY," 
								+" SORDALLOC.LOT_NO,SORDALLOC.LOT_SL,SORDALLOC.LOC_CODE, "
								+" CASE WHEN SORDALLOC.WAVE_FLAG IS NULL THEN 'N' ELSE SORDALLOC.WAVE_FLAG END AS WAVE_FLAG "
								+",SORDDET.UNIT,SORDDET.UNIT__STD,SORDDET.CONV__QTY_STDUOM,SORDDET.QUANTITY__STDUOM,SORDDET.QUANTITY " 
								+" FROM SORDDET,SORDALLOC,SORDITEM,CUSTOMER,ITEM, SORDER  "
								+" WHERE SORDER.SALE_ORDER = SORDDET.SALE_ORDER AND "
								+"(SORDALLOC.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
								+"( SORDALLOC.LINE_NO = SORDDET.LINE_NO ) AND "
								+"( SORDALLOC.EXP_LEV = SORDITEM.EXP_LEV ) AND "
								+"( SORDITEM.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
								+"( SORDITEM.LINE_NO = SORDDET.LINE_NO ) AND "
								+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
								+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND "
								+" SORDER.SITE_CODE = ? AND "
								+" SORDER.SALE_ORDER = ? AND "
								+" CUSTOMER.CUST_CODE =? AND "  
								+" SORDITEM.LINE_NO = ? "
								+"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end  in ( 'P','H')  " 
								+"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
								+"AND CASE WHEN SORDALLOC.STATUS IS NULL THEN 'P' ELSE SORDALLOC.STATUS end = 'P' "
								+"AND SORDITEM.QTY_ALLOC> 0 "
								+"AND SORDITEM.LINE_TYPE = 'I'"   
							    +"ORDER BY SORDER.SALE_ORDER,SORDITEM.EXP_LEV,SORDITEM.ITEM_CODE__ORD";

						pstmt = conn.prepareStatement(getDataSql1);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,saleOrder);
						pstmt.setString(3,custCodeHdr);
						pstmt.setString(4,lineNoSord);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							expLev = rs.getString(1);
							itemCode = rs.getString(2);
							itemDescr  =  rs.getString(3);
							qty  =  rs.getDouble(4);
							pendingQty  =  rs.getDouble(5);
							lotNo = rs.getString(6);
							lotSL = rs.getString(7);
							locCode = rs.getString(8);
							waveFlag = rs.getString(9);
							unit = rs.getString(10);
							unitStd = rs.getString(11);
							convQtyStduom = rs.getDouble(12);
							quantityStduom = rs.getDouble(13);
							orderQuantity = rs.getDouble(13);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						System.out.println("pendingQty["+pendingQty+"]");
						valueXmlString.append("<pending_qty>").append("<![CDATA[" +  pendingQty + "]]>").append("</pending_qty>");
						valueXmlString.append("<item_code>").append("<![CDATA[" +  itemCode + "]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" +  itemDescr + "]]>").append("</item_descr>");
						valueXmlString.append("<exp_lev>").append("<![CDATA[" +  expLev + "]]>").append("</exp_lev>");
						valueXmlString.append("<quantity>").append("<![CDATA[" +  pendingQty + "]]>").append("</quantity>");
						valueXmlString.append("<lot_no>").append("<![CDATA[" +  lotNo + "]]>").append("</lot_no>");
						valueXmlString.append("<lot_sl>").append("<![CDATA[" + lotSL+ "]]>").append("</lot_sl>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" + locCode  + "]]>").append("</loc_code>");
						locDescr = findValue(conn, "descr" ,"location", "loc_code", locCode);
						valueXmlString.append("<location_descr>").append("<![CDATA[" +  locDescr + "]]>").append("</location_descr>");
						valueXmlString.append("<order_quantity>").append( orderQuantity ).append("</order_quantity>");
						valueXmlString.append("<unit>").append( unit ).append("</unit>");
						valueXmlString.append("<unit__std>").append( unitStd ).append("</unit__std>");
						valueXmlString.append("<conv__qty_stduom>").append( convQtyStduom ).append("</conv__qty_stduom>");
						valueXmlString.append("<quantity__stduom>").append( quantityStduom ).append("</quantity__stduom>");
						
						
					}
				}
				valueXmlString.append("</Detail2>");
				break;

			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}	 


	private String findValue(Connection conn, String columnName ,String tableName, String columnName2, String value) throws  ITMException, RemoteException
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
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from findValue " + findValue);
		return findValue;
	}

	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
		finally
		{
			try
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
	private String checkNull( String input )
	{
		if (input == null )
		{
			input = "";
		}
		return input;
	}
}	