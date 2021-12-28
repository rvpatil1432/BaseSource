
package ibase.webitm.ejb.dis;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class PriceListIC extends ValidatorEJB implements PriceListICLocal, PriceListICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = null;
	ValidatorEJB validator = null;


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
			System.out.println("Val xmlString2 :: " + xmlString2 );

			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString( xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}


	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String errorType = "";
		String userId = "";
		String sql = "";
		String keyFlag = "";
		String priceListParent = "";
		String priceList = "";
		String priceListTar = "";
		String calcMethod = "";
		String childNodeValue = "" ;
		String active = "";
		String effDateStr ="", validUptoStr ="";
		Timestamp effDate = null,  validUpto = null;
		int ctr = 0;
		int count = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		double minQty = 0.0, maxQty = 0.0, minRate = 0.0, rate = 0.0;
		StringBuffer valueXmlString = new StringBuffer();
		ArrayList<String> errList = new ArrayList();
		ArrayList<String> errFields = new ArrayList<String>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
		System.out.println("date format"+dateFormat);
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String pListNxLev = "";
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			finCommon = new FinCommon();
			validator = new ValidatorEJB();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat dbDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("calc_method"))
					{
						calcMethod = genericUtility.getColumnValue("calc_method", dom);
						if (calcMethod !=null && calcMethod.trim().length() > 0)
						{
							sql ="SELECT COUNT(*) FROM calc_method WHERE calc_method = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, calcMethod);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							if(count == 0)
							{
								errCode = "VTCALMINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
					/*	else
						{
							errCode = "VTCALMNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}
					
				
					else if(childNodeName.equalsIgnoreCase("price_list"))
						{
							priceList = genericUtility.getColumnValue("price_list", dom);
							sql="select key_flag  from transetup where tran_window = 'w_pricelist_mst' ";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyFlag = rs.getString(1);
							}
							if(keyFlag == null)
							{
								keyFlag ="M";
								priceList = genericUtility.getColumnValue("price_list", dom);
								
							}
							if(keyFlag.equalsIgnoreCase("M") && (priceList == null ||priceList.trim().length() == 0 ))
									{
									errCode = "VMCODNULL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						
							if(editFlag.equalsIgnoreCase("A"))
							{
								sql="select count(*)  from pricelist_mst where price_list = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt(1);
								}
								if(count>0)
								{
									errCode = "VMDUPL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
										

							
						}
					                                                                      
					 if(childNodeName.equalsIgnoreCase("price_list__parent"))
					  {
						priceListParent = genericUtility.getColumnValue("price_list__parent", dom);
						if (priceListParent !=null && priceListParent.trim().length() > 0)
						{
							priceList = genericUtility.getColumnValue("price_list", dom);
							if(priceListParent.trim().equals(priceList.trim())) 
							{
								errCode = "VTPARENTPL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}                                                                        
						}

					}
					 //Changed By Nasruddin [20-SEP-16] start
					 else if(childNodeName.equalsIgnoreCase("slab_no") || childNodeName.equalsIgnoreCase("tax_base") || childNodeName.equalsIgnoreCase("item_code"))
					 {
						 childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
						 if (childNodeValue !=null && childNodeValue.trim().length() > 0)
						 {
							 count = 0;
							 sql = "SELECT COUNT(1) FROM ITEM WHERE ITEM_CODE = ?";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, childNodeValue);
							 rs = pstmt.executeQuery();
							 if( rs.next() )
							 {
								 count = rs.getInt(1);
							 }
							 pstmt.close();
							 pstmt=null;
							 rs.close();
							 rs = null;
							 
							 if( count == 0)
							 {
							    errCode = "VMITEM_CD"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							 }
							 else
							 {
								 sql = "SELECT ACTIVE FROM ITEM WHERE ITEM_CODE = ?";
								 pstmt = conn.prepareStatement(sql);
								 pstmt.setString(1, childNodeValue);
								 rs = pstmt.executeQuery();
								 if( rs.next() )
								 {
									 active = rs.getString("ACTIVE");
								 }
								 pstmt.close();
								 pstmt=null;
								 rs.close();
								 rs = null; 
								 if("N".equalsIgnoreCase(active))
								 {
									 errCode = "VTITEM4"; 
									 errList.add(errCode);
									 errFields.add(childNodeName.toLowerCase());
								 }
							 }
						 }

					 }
					//Changed By Nasruddin [20-SEP-16] end
					//Pavan R on 31oct18 [to validate nxt level price list]
					 else if(childNodeName.equalsIgnoreCase("price_list_nxlev"))
					 {
						 System.out.println("Inside PavanR.....Validation");
						 pListNxLev = checkNullAndTrim(genericUtility.getColumnValue("price_list_nxlev", dom));
						 if(pListNxLev != null && pListNxLev.trim().length() > 0)
						 {
							 sql="select count(*) from pricelist_mst where price_list = ?";
							 pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, pListNxLev);
							 rs = pstmt.executeQuery();
							 if(rs.next())
							 {
								 count = rs.getInt(1);
							 }
							 rs.close();rs = null;
							 pstmt.close();pstmt = null; 
							 if(count == 0)
							 {
								 errCode = "VTNXTPLIST";
								 errList.add(errCode);
								 errFields.add(childNodeName.toLowerCase());
							 }else
							 {
								 priceList = checkNullAndTrim(genericUtility.getColumnValue("price_list", dom));
								 priceListParent = checkNullAndTrim(genericUtility.getColumnValue("price_list__parent", dom));								 
								 if(priceList.equals(pListNxLev))
								 {									 
									 errCode = "VTNXTPLST1";
									 errList.add(errCode);
									 errFields.add(childNodeName.toLowerCase());
								 }else if(priceListParent.equals(pListNxLev))
								 {
									 errCode = "VTNXTPLST2";
									 errList.add(errCode);
									 errFields.add(childNodeName.toLowerCase());
								 }
							}								
						}
					 } //Pavan R on 31oct18 end
				}
				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					
					  if(childNodeName.equalsIgnoreCase("calc_method"))
					{
						calcMethod = genericUtility.getColumnValue("calc_method", dom);
						if (calcMethod !=null && calcMethod.trim().length() > 0)
						{
							sql ="SELECT COUNT(*) FROM calc_method WHERE calc_method = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, calcMethod);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							if(count == 0)
							{
								errCode = "VTCALMINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						/*else
						{
							errCode = "VTCALMNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}
					 if(childNodeName.equalsIgnoreCase("price_list__tar"))
					{
						priceListTar = genericUtility.getColumnValue("price_list__tar", dom);
						if (priceListTar == null || priceListTar.trim().length() == 0)
						{
							errCode = "VTPLTARNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}                                                                        
					}
					else if(childNodeName.equalsIgnoreCase("price_list__parent"))
					{
						priceListParent = genericUtility.getColumnValue("price_list__parent", dom);
						if (priceListParent !=null && priceListParent.trim().length() > 0)
						{
							priceListTar = genericUtility.getColumnValue("price_list__tar", dom);
							if(priceListParent.trim().equals(priceListTar.trim())) 
							{
								errCode = "VTTARGETPL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}                                                                        
						}

					}

					
					if(childNodeName.equalsIgnoreCase("min_qty"))
					{
						minQty = convertNumber(checkNullAndTrim(genericUtility.getColumnValue("min_qty", dom)));
						maxQty = convertNumber(checkNullAndTrim(genericUtility.getColumnValue("max_qty", dom)));
						if(maxQty < minQty) 
						{
							errCode = "VMMINQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}                                                                        
					}
					else if(childNodeName.equalsIgnoreCase("min_rate"))
					{
						minRate = convertNumber(checkNullAndTrim(genericUtility.getColumnValue("min_rate", dom)));
						rate = convertNumber(checkNullAndTrim(genericUtility.getColumnValue("rate", dom)));
						if(rate  <  minRate) 
						{
							errCode = "VMMINQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}                                                                        
					}
					else if(childNodeName.equalsIgnoreCase("valid_upto"))
					{
						effDateStr = genericUtility.getColumnValue("eff_date",dom);
						validUptoStr = genericUtility.getColumnValue("valid_upto",dom);

						if((effDateStr != null) && (validUptoStr != null))
						{
							effDate   = Timestamp.valueOf(genericUtility.getValidDateString(effDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							validUpto = Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if( !validUpto.after(effDate)  )
							{
								errCode = "VMVAL_UPTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_code"))
					{
						 childNodeValue = checkNullAndTrim(genericUtility.getColumnValue("tax_code", dom));
						 count = 0;
						 sql = "SELECT COUNT(1) FROM TAX WHERE TAX_CODE = ?";
						 pstmt = conn.prepareStatement(sql);
						 pstmt.setString(1, childNodeValue);
						 rs = pstmt.executeQuery();
						 if( rs.next() )
						 {
							 count = rs.getInt(1);
						 }
						 pstmt.close();
						 pstmt=null;
						 rs.close();
						 rs = null;
						 if( count == 0)
						 {
						    errCode = "VTTAX1"; 
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						 }
						
					}
					else if(childNodeName.equalsIgnoreCase("rate"))
					{
						rate = convertNumber(checkNullAndTrim(genericUtility.getColumnValue("rate", dom)));
						if(rate <= 0) 
						{
							errCode = "VTRATE2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}                                                                        
					}
					
				}
				valueXmlString.append("</Detail2>");
				break;
			}
			int errListSize = errList.size();
			count = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(count = 0; count < errListSize; count ++)
				{
					errCode = errList.get((int) count);
					errFldName = errFields.get((int) count);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString +errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
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

		}

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
	}

private double convertNumber(String columnValue)
{
	return columnValue.trim().length() == 0 ? 0 : Double.parseDouble(columnValue);

}


// Changed By Nasruddin 20-SEP-16 STARt
	private String checkNullAndTrim(String value)
	{
		return value == null ? "" : value.trim();
	}
	// Changed By Nasruddin 20-SEP-16 END

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;


	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{

		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Node parentNode1 = null ; 
		String childNodeName = null;
		String currCode = "";
		String defCrTerm = "";
		String calcMethod = "";
		String calcMethodDescr = "";
		String columnValue = "";
		String priceList = "";
		String sql = "";
		int ctr = 0;
		int currentFormNo = 0;
		double lineNo = 0;
		java.util.Date date1 = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			defCrTerm = finCommon.getFinparams("999999","DEF_CR_TERM",conn);
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
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
						}
					}
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));	
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{                
					valueXmlString.append("<list_type protect = \"0\">").append("<![CDATA[" + 0 +"]]>").append("</list_type>");
					valueXmlString.append("<order_type protect = \"0\">").append("<![CDATA[" + 0 +"]]>").append("</order_type>");

				}if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{


				}
				else if(currentColumn.trim().equalsIgnoreCase("calc_method"))
				{
					calcMethod = genericUtility.getColumnValue("calc_method", dom);
					if(calcMethod != null && calcMethod.trim().length() > 0)
					{
						sql =" SELECT descr FROM calc_method WHERE calc_method = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, calcMethod);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							calcMethodDescr = rs.getString(1);
						}
						else 
						{

							calcMethodDescr = "";
						}
						valueXmlString.append("<calc_method_descr >").append("<![CDATA[" + calcMethodDescr +"]]>").append("</calc_method_descr>");
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}

				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
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
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr ++;
				}
				while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					priceList = genericUtility.getColumnValueFromNode("price_list", parentNode);

					valueXmlString.append("<price_list >").append("<![CDATA[" + priceList +"]]>").append("</price_list>");
				}

				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{

				}
				else if(currentColumn.trim().equalsIgnoreCase("calc_method"))
				{
					calcMethod = genericUtility.getColumnValue("calc_method", dom);
					if(calcMethod != null && calcMethod.trim().length() > 0)
					{
						sql =" SELECT descr FROM calc_method WHERE calc_method = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, calcMethod);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							calcMethodDescr = rs.getString(1);
						}
						else 
						{

							calcMethodDescr = "";
						}
						valueXmlString.append("<descr>").append("<![CDATA[" + calcMethodDescr +"]]>").append("</descr>");
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				valueXmlString.append("</Detail2>");
				break;
			}
			valueXmlString.append("</Root>");
		}catch(Exception e)
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
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
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
}



