/*
 * Author:Wasim Ansari
 * Date: 30-01-2017
 * Request ID:D16JSUN002 (Sales Contract GWT Wizard)
 */

package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import org.apache.xerces.dom.AttributeMap;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ScontractGwtIC extends ValidatorEJB implements ScontractGwtICLocal, ScontractGwtICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	int domIDs = 1;
    /**
     * Default constructor. 
     */
    public ScontractGwtIC() {
        // TODO Auto-generated constructor stub
    }
    
	@Override
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		
		try
		{
			System.out.println("Inside wfvalData ScontractGwtIC");
			System.out.println("@@xmlString : ["+ xmlString+ "] \nxmlString1 : ["+ xmlString1 +"] \nxmlString2 : ["+ xmlString2 +"]");
			
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
			
			String editSatus = genericUtility.getColumnValue("edit_status", dom1);
			System.out.println("WfValData editStatus["+editSatus+"]");
			if(!"V".equalsIgnoreCase(editSatus))
			{
				errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : Cpp : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		return(errString);
	}
	
	
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String columnValue = "";
		String userId = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String sql = "";
		int ctr=0;
		int childNodeListLength;
		int currentFormNo = 0;
		long cnt = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		boolean flag = false; 
		
		int count = 0;
		Timestamp date1 = null, date2 = null, date3 = null;
		int parentNodeListLength = 0;
		String custCode = "",transMode = "",crTerm = "",currCode = "",custType = "",orderTypeAppl = "",priceRule = "",
				scope = "",nodeValue = "",lineNo = "",rateOpt = "",priceList = "",division = "";
		String effFrom = "",ValidUpto = "",discount = "",msgNo = "";
		int countRecord = 0;
		
		String selected = "",updFlag = "",status = "";
		boolean deleteFlag = false;
		int countSelectd = 0;
		ArrayList recordList = new ArrayList();
		String siteCode = "";
		
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			conn = getConnection();
			
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			switch(currentFormNo)
			{
				case 1:
				{
					custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom));
					if(custCode.length() == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","NULLCUSTCD","","",conn);
						return errString;
					}
					else
					{
						custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom));
						
						if("S".equalsIgnoreCase(custType))
						{
							sql = " SELECT COUNT(*) FROM STRG_CUSTOMER WHERE SC_CODE = ? ";
							msgNo = "VMSTGCST";
						}
						else
						{
							sql = " SELECT COUNT(*) FROM CUSTOMER WHERE CUST_CODE = ? ";
							msgNo = "VMREGCUST";
						}
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
							if(cnt==0)
							{
								errString = itmDBAccessLocal.getErrorString("",msgNo,"","",conn);
								return errString;
							}
						}
						if(rs != null) 
						{
							rs.close();rs = null;
						}
						if(pstmt != null) 
						{
							pstmt.close();pstmt = null;
						}
					}
					
					
					orderTypeAppl = checkNullAndTrim(genericUtility.getColumnValue("order_type__appl", dom));
					if(orderTypeAppl.length() == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","NULLORDAPL","","",conn);
						return errString;
					}
					else
					{
						String [] orderList = null;
						orderList = orderTypeAppl.split(",");
		    			
		    			//for(String type : orderList)
						String type = "";
						for(int i = 0; i< orderList.length ; i++)
		    			{
							type = orderList[i];
		    				System.out.println("TYPE ["+type+"] Position["+i+"]");
		    				
		    				if(type != null && type.trim().length() > 0)
		    				{
		    					sql = " SELECT COUNT(*) FROM SORDERTYPE WHERE ORDER_TYPE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, type.trim());
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										return getError(" \n Order Type: "+type+", Position : "+i+" ", "VMINVAPPL", conn);
									}
								}
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}
		    				
		    				}
		    				else
		    				{
		    					return getError(" \n Order Type: "+type+", Position : "+i+" ", "NULLORDAPL", conn);
		    				}
		    			}
						
					}
					
					
					effFrom = checkNullAndTrim(genericUtility.getColumnValue("eff_from", dom));
					ValidUpto = checkNullAndTrim(genericUtility.getColumnValue("valid_upto", dom));
					
					if(effFrom.length() == 0 || ValidUpto.length() == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VMEFFUPTO","","",conn);
						return errString;
					}
					
					date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_from", dom)==null?getCurrdateAppFormat():genericUtility.getColumnValue("eff_from", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
					date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("valid_upto", dom)==null?getCurrdateAppFormat():genericUtility.getColumnValue("valid_upto", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
					date3 = Timestamp.valueOf(genericUtility.getValidDateString( getCurrdateAppFormat() , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
					if(date1 == null)
					{
						errString = itmDBAccessLocal.getErrorString("","VFTDT1","","",conn);
						return errString;
					}
					else if(date1.after(date2))
					{
						errString = itmDBAccessLocal.getErrorString("","VEFFDTVLER","","",conn);
						return errString;
					}
					else if(date1.before(date3))
					{
						errString = itmDBAccessLocal.getErrorString("","VEFFDTTD","","",conn);
						return errString;
					}
					
					date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("valid_upto", dom)==null?getCurrdateAppFormat():genericUtility.getColumnValue("valid_upto", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
					date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_from", dom)==null?getCurrdateAppFormat():genericUtility.getColumnValue("eff_from", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
					date3 = Timestamp.valueOf(genericUtility.getValidDateString( getCurrdateAppFormat() , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
					if(date1 == null)
					{
						errString = itmDBAccessLocal.getErrorString("","VMVAL_UPT1","","",conn);
						return errString;
					}
					else if(date1.before(date2))
					{
						errString = itmDBAccessLocal.getErrorString("","VEFFDTVLER","","",conn);
						return errString;
					}
					else if(date1.before(date3))
					{
						errString = itmDBAccessLocal.getErrorString("","VVDTERR","","",conn);
						return errString;
					}

					priceRule = checkNullAndTrim(genericUtility.getColumnValue("price_rule", dom));
					if(priceRule.length() == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VMBLKPRCRL","","",conn);
						return errString;
					}
					
					custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom));
					if(custType.length() == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VMNLCSTYPE","","",conn);
						return errString;
					}
					
					transMode = genericUtility.getColumnValue("trans_mode", dom);
					if(transMode == null || transMode.trim().length() == 0 )
					{
						errString = itmDBAccessLocal.getErrorString("","VTITMOD","","",conn);
						return errString;
					}
					
					
					date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("contract_date", dom) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					sql = "select stat_sal from period where fr_date <= ? and to_date >= ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setTimestamp(1, date1);
					pstmt.setTimestamp(2, date1);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						String statSal = rs.getString(1);
						if(!statSal.equals("Y"))
						{
							errString = itmDBAccessLocal.getErrorString("","VTPRDSAL","","",conn);
							return errString;
						}
					}
					else
					{
						errString = itmDBAccessLocal.getErrorString("","VTSAL1","","",conn);
						return errString;
					}
					if(rs != null) 
					{
						rs.close();rs = null;
					}
					if(pstmt != null) 
					{
						pstmt.close();pstmt = null;
					}
					
					if("R".equalsIgnoreCase(custType))
					{
						crTerm = genericUtility.getColumnValue("cr_term", dom);
						sql = "Select Count(*) from crterm where cr_term = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, crTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
							if(cnt == 0 )
							{
								errString = itmDBAccessLocal.getErrorString("","VMCRTRM","","",conn);
								return errString;
							}
						}
						if(rs != null) 
						{
							rs.close();rs = null;
						}
						if(pstmt != null) 
						{
							pstmt.close();pstmt = null;
						}
						
						
						currCode = genericUtility.getColumnValue("curr_code", dom);
						sql = "Select Count(*) from currency where curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, currCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
							if(cnt == 0 )
							{
								errString = itmDBAccessLocal.getErrorString("","VMCRCODE","","",conn);
								return errString;
							}
						}
						if(rs != null) 
						{
							rs.close();rs = null;
						}
						if(pstmt != null) 
						{
							pstmt.close();pstmt = null;
						}
					}
					
					siteCode = genericUtility.getColumnValue("site_code", dom);
					
					if(siteCode == null || siteCode.trim().length() == 0 )
					{
						errString = itmDBAccessLocal.getErrorString("","VMNULLSITE","","",conn);
						return errString;
					}
					
					sql = " SELECT COUNT(*) FROM SITE WHERE SITE_CODE = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt(1);
						if(cnt == 0 )
						{
							errString = itmDBAccessLocal.getErrorString("","VMINVSTE","","",conn);
							return errString;
						}
					}
					if(rs != null) 
					{
						rs.close();rs = null;
					}
					if(pstmt != null) 
					{
						pstmt.close();pstmt = null;
					}

				}//End case1
				break;
				case 2:
				{
					parentNodeList = dom2.getElementsByTagName("Detail2");
					parentNodeListLength = parentNodeList.getLength(); 
					
					for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{
						division = "";scope = "";lineNo = "";priceList = "";effFrom = "";ValidUpto = "";discount = "";
						deleteFlag = false;
						
						parentNode = parentNodeList.item(selectedRow);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{
							childNode = childNodeList.item(childRow);
							childNodeName = childNode.getNodeName();
							
							//System.out.println("Detail2 childNodeName ["+childNodeName+"] childNode["+childNode+"] ");
							
							if("attribute".equalsIgnoreCase(childNodeName))
							{
								selected = childNode.getAttributes().getNamedItem("selected").getNodeValue();
								updFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
								status = childNode.getAttributes().getNamedItem("status").getNodeValue();
								
								System.out.println("Inside selected Attribute Selected ["+selected+"]  UpdFlag["+updFlag+"] Status["+status+"]");
								if("D".equalsIgnoreCase(updFlag))
								{
									deleteFlag = true;
									break;
								}
							}
							
							if(childNode == null || childNode.getNodeType() != childNode.ELEMENT_NODE)
							{
								continue;
							}
							
							if(childNode != null && "division".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								division = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "contract_scope".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								scope = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "line_no".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								lineNo = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "price_list".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								priceList = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "eff_from".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								effFrom = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "valid_upto".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								ValidUpto = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "discount".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								discount = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
						}
						System.out.println("Contract_scope["+scope+"] lineNo["+lineNo+"] priceList["+priceList+"] eff["+effFrom+"] valid["+ValidUpto+"]");
						System.out.println("Delete Flag="+deleteFlag);
						
						if(deleteFlag)
						{
							countRecord++;
							continue;
						}
						
						if(lineNo.length() > 0)
						{	
							countRecord++;
							
							if(scope.length() == 0)
							{
								return getError("\n Line Number: "+lineNo+", Division: "+division+" ", "VMNULSCOPE", conn);
							}
						
							if(priceList.length() == 0)
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "NULPRLIST", conn);
							}
							else
							{
								sql = " SELECT COUNT(*) FROM PRICELIST_MST WHERE PRICE_LIST = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "STKVALPRAE", conn);
									}
								}
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}
							}
							
							if(division.length() == 0)
							{	
								return getError(" Line Number: "+lineNo+",  Division: "+division+" ", "VMNULDVSN", conn);
							}
							else
							{
								String [] dvsn = null;
								dvsn = division.split(",");
				    			
				    			String itemSer = "";
								for(int i = 0; i< dvsn.length; i++)
				    			{
									itemSer = dvsn[i]; 
				    				System.out.println("Division ["+itemSer+"] Position ["+i+"]");
				    				
				    				if(itemSer != null && itemSer.trim().length() > 0)
				    				{
				    					sql = " SELECT COUNT(*) FROM ITEMSER WHERE ITEM_SER = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, itemSer.trim());
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											cnt = rs.getInt(1);
											if(cnt == 0 )
											{
												return getError(" \n Line Number: "+lineNo+", Division: "+itemSer+", Position : "+i+" ", "VMINVDVSN", conn);
											}
										}
										if(rs!=null)
										{
											rs.close();rs = null;
										}
										if(pstmt!=null)
										{
											pstmt.close();pstmt = null;
										}
				    				
				    				}
				    				else
				    				{
				    					return getError(" \n Line Number: "+lineNo+", Division: "+itemSer+", Position : "+i+" ", "VMNULDVSN", conn);
				    				}
				    			}
								
							}
							
							/*if(effFrom.length() == 0 || ValidUpto.length() == 0)
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VMEFFUPTO", conn);
							}
							
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( effFrom==null?getCurrdateAppFormat():effFrom , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( ValidUpto==null?getCurrdateAppFormat():ValidUpto , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							date3 = Timestamp.valueOf(genericUtility.getValidDateString( getCurrdateAppFormat() , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(date1 == null)
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VFTDT1", conn);
							}
							else if(date1.after(date2))
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VEFFDTVLER", conn);
							}
							else if(date1.before(date3))
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VEFFDTTD", conn);
							}
							
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( ValidUpto==null?getCurrdateAppFormat():ValidUpto , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( effFrom==null?getCurrdateAppFormat():effFrom , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date3 = Timestamp.valueOf(genericUtility.getValidDateString( getCurrdateAppFormat() , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							if(date1 == null)
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VMVAL_UPT1", conn);
							}
							else if(date1.before(date2))
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VEFFDTVLER", conn);
							}
							else if(date1.before(date3))
							{
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VVDTERR", conn);
							}*/
							
							
							try
							{
								if(discount.length() == 0)
								{
									return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VMNULDISCP", conn);
								}
								double discountPerc = Double.parseDouble(discount);
								
								if(discountPerc > 100 || discountPerc < 0)
								{
									return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VMINVDISCP", conn);
								}
							}
							catch(Exception e)
							{
								System.out.println("Exception at discount perc"+e.getMessage());
								return getError(" \n Line Number: "+lineNo+", Division: "+division+" ", "VMINVDISCP", conn);
							}
						
						}
					}
					
					System.out.println("Total Record Add for Form 2="+countRecord);
					/*if(countRecord == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VMNODETAIL","","",conn);
						return errString;
					}*/
					
				}//End case2
				break;
				case 3:
				{
					String itemCode = "",itemSer = "";
					int selCnt = 0;
					
					parentNodeList = dom2.getElementsByTagName("Detail3");
					parentNodeListLength = parentNodeList.getLength(); 
					
					for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{
						itemCode = "";rateOpt = "";lineNo = "";selected = "";priceList = "";
						deleteFlag = false;
						
						parentNode = parentNodeList.item(selectedRow);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{
							childNode = childNodeList.item(childRow);
							childNodeName = childNode.getNodeName();
							
							//System.out.println("Detail3 childNodeName ["+childNodeName+"]");
							
							if("attribute".equalsIgnoreCase(childNodeName))
							{
								selected = childNode.getAttributes().getNamedItem("selected").getNodeValue();
								updFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
								status = childNode.getAttributes().getNamedItem("status").getNodeValue();
								
								System.out.println("Inside selected Attribute Selected ["+selected+"]  UpdFlag["+updFlag+"] Status["+status+"]");
								if("D".equalsIgnoreCase(updFlag))
								{
									deleteFlag = true;
									break;
								}
							}
							
							if(childNode == null || childNode.getNodeType() != childNode.ELEMENT_NODE)
							{
								continue;
							}
							
							if(childNode != null && "item_code".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								itemCode = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "rate_opt".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								rateOpt = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "line_no".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								lineNo = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "eff_from".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								effFrom = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "valid_upto".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								ValidUpto = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "price_list".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								priceList = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
						}
						
						System.out.println("Item Code ["+itemCode+"] Line Number["+lineNo+"] Rate Opt["+rateOpt+"]");
						System.out.println("Delete Flag="+deleteFlag);
						if(deleteFlag)
						{
							countRecord++;
							continue;
						}
						
						if(lineNo.length() > 0)
						{
							countRecord++;
							
							if(itemCode.length() == 0)
							{
								return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "VTITMCODE", conn);
							}
							else
							{
								sql = "Select Count(*) from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "VTITEM1", conn);
									}
									else
									{
										if(rs != null) 
										{
											rs.close();rs = null;
										}
										if(pstmt != null) 
										{
											pstmt.close();pstmt = null;
										}
										
										sql = " Select item_ser from item where item_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											String itemSer1 = rs.getString(1);
											if(itemSer1.trim().equals("Y"))
											{
												itemSer = genericUtility.getColumnValue("Item_ser", dom);
												if(itemSer != null && itemSer.trim().length() > 0 && !(itemSer.trim().equals(itemSer1.trim())))
												{
													return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "VTITEM2", conn);
												}
											}
										}
									}
								}
								if(rs != null) 
								{
									rs.close();rs = null;
								}
								if(pstmt != null) 
								{
									pstmt.close();pstmt = null;
								}
								if(errList.size() == 0 )
								{
									date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_from", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
									date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("valid_upto", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
									custCode = genericUtility.getColumnValue("item_code", dom1);
									siteCode = genericUtility.getColumnValue("item_code", dom1);
									sql = " select count(*) from scontract a, scontractdet b " +
											"where a.contract_no = b.contract_no and a.site_code = ? " +
											" and a.cust_code = ? and b.item_code = ? and " +
											" (a.eff_from between ? and ? or a.valid_upto between ? and ? " +
											" or ? between a.eff_from and a.valid_upto or ? between a.eff_from and a.valid_upto) " +
											" and a.confirmed = 'Y'	and case when a.status is null then ' ' else a.status end <> 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, siteCode);
									pstmt.setString(2, custCode);
									pstmt.setString(3, itemCode);
									pstmt.setTimestamp(4, date1);
									pstmt.setTimestamp(5, date2);
									pstmt.setTimestamp(6, date1);
									pstmt.setTimestamp(7, date2);
									pstmt.setTimestamp(8, date1);
									pstmt.setTimestamp(9, date2);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
										if(cnt > 0 )
										{
											return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "VCUSITMDUP", conn);
										}
									}
									if(rs != null) 
									{
										rs.close();rs = null;
									}
									if(pstmt != null) 
									{
										pstmt.close();pstmt = null;
									}
								}
							}
							
							if(rateOpt.length() == 0)
							{
								return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "VMNULLOPT", conn);
							}
							
							if(recordList.contains(itemCode))
							{
								return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "VMDUPITEM", conn);
							}
							else
							{
								recordList.add(itemCode);
							}
							
							if(priceList != null && priceList.length() > 0)
							{	
								sql = " SELECT COUNT(*) FROM PRICELIST_MST WHERE PRICE_LIST = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, priceList);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt == 0 )
									{
										return getError(" \n Line Number: "+lineNo+", Item Code: "+itemCode+" ", "STKVALPRAE", conn);
									}
								}
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}
							}
						}
						
					}
					
					System.out.println("Total Record Add for Form 3="+countRecord+"" );
					/*if(countRecord == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VMNODETAIL","","",conn);
						return errString;
					}*/
					
					/*custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom1));
					System.out.println("Customer Type in Detail3 Validation ["+custType+"]");
					
					if("C".equalsIgnoreCase(custType))
					{
						errString = itmDBAccessLocal.getErrorString("","VMCUSTFNSH","","",conn);
						return errString;
					}*/
		
				}//End case3
				break;
				case 4:
				{
					parentNodeList = dom2.getElementsByTagName("Detail4");
					parentNodeListLength = parentNodeList.getLength(); 
					
					for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
					{
						custCode = "";custType = "";lineNo = "";
						
						parentNode = parentNodeList.item(selectedRow);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for (int childRow = 0; childRow < childNodeListLength; childRow++)
						{
							childNode = childNodeList.item(childRow);
							childNodeName = childNode.getNodeName();
							
							//System.out.println("Detail4 childNodeName ["+childNodeName+"]");
							
							if(childNode == null || childNode.getNodeType() != childNode.ELEMENT_NODE)
							{
								continue;
							}
							
							if(childNode != null && "cust_code".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								custCode = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "cust_type".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								custType = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
							else if(childNode != null && "line_no".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
							{
								lineNo = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
							}
						}
						
						System.out.println("Item Code ["+custCode+"] Line Number["+lineNo+"] Cust Type["+custType+"]");
						
						
						if(lineNo.length() > 0)
						{
							//countRecord++;
							if(custCode.length() == 0)
							{
								return getError(" \n Line Number: "+lineNo+", Customer Code: "+custCode+" ", "NULLCUSTCD", conn);
							}
							else
							{
								//if("C".equalsIgnoreCase(custType))
								if("S".equalsIgnoreCase(custType))
								{
									sql = " SELECT COUNT(*) FROM STRG_CUSTOMER WHERE SC_CODE = ? ";
									msgNo = "VMSTGCST";
								}
								else
								{
									sql = " SELECT COUNT(*) FROM CUSTOMER WHERE CUST_CODE = ? ";
									msgNo = "VMREGCUST";
								}
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
									if(cnt==0)
									{
										return getError(" \n Line Number: "+lineNo+", Customer Code: "+custCode+" ", msgNo , conn);
									}
								}
								if(rs != null) 
								{
									rs.close();rs = null;
								}
								if(pstmt != null) 
								{
									pstmt.close();pstmt = null;
								}
							}
							
							if(recordList.contains(custCode))
							{
								return getError(" \n Line Number: "+lineNo+", Customer Code: "+custCode+" ", "VMDUPCUST" , conn);
							}
							else
							{
								recordList.add(custCode);
							}
						}
					}	
					
					/*System.out.println("Total Record Add for Form 4="+countRecord);
					if(countRecord == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VMNODETAIL","","",conn);
						return errString;
					}*/
					
				}//End case 4
				break;
			}//End Switch
			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
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
		}// End of try
		catch(Exception e)
		{
			System.out.println("Inside Catch ScontractGwtIC wfValData Exception="+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null) 
				{
					rs.close();rs = null;
				}
				if(pstmt != null) 
				{
					pstmt.close();pstmt = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
	
	//public  String getError(String itemCode, String Code, Connection conn)  throws ITMException, Exception
	public  String getError(String message,String Code, Connection conn)  throws ITMException, Exception
	{
		String mainStr ="";

		try
		{
			String errString = "";
			errString =  new ITMDBAccessEJB().getErrorString("",Code,"","",conn);
			
			String begPart = errString.substring(0,errString.indexOf("</description>"));
			String endDesc = errString.substring(errString.indexOf("</description>"),errString.length());
			
			mainStr = begPart + message + endDesc;
			begPart = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return mainStr;
	}
	
	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		Document dom1 = null;
		Document dom = null;
		Document dom2 = null;
		String valueXmlString = "";
		
		try
		{
			System.out.println("Inside item change ScontractGwtIC");
			System.out.println("xmlString : ["+ xmlString+ "] \nxmlString1 : ["+ xmlString1 +"] \nxmlString2 : ["+ xmlString2 +"]");
			
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
			
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			valueXmlString = genericUtility.createErrorString(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		return (valueXmlString);
	}
	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		Connection conn = null;
		PreparedStatement pstmt = null, pstmtDescr = null;
		ResultSet rs = null, rsDescr = null;
		
		String sql = "";
		int currentFormNo = 0;
		
		StringBuffer valueXmlString = new StringBuffer();
		
		int ctr = 0;
		int num = 0;
		int num1 = 0;
		int num2 = 0;
		int num3 = 0;
		int quantity = 0 ;
		double rate = 0;
		double rateClg = 0;
		int pos = 0;
		int prdCdFr = 0;
		int prdCdTo = 0;
		int forecastQtn = 0;
		int despQtn = 0;
		int despValue = 0;
		int balQtn = 0;
		int nespRate = 0;
		int nrpRate = 0;
		int balValue = 0;
		String siteCode = "";
		String itemSer = "";
		String itemSerInv = "";
		String itemCode = "";
		String contractNo = "";
		String validUpTostr = "";
		String effFromStr = "";
		String currDescr = "";
		String priceList = "";
		String custCode = "";
		String toStation = "";
		String fromStation = "";
		String siteCodeShip = "";
		String contractDateStr = "";
		String priceListClg = "";
		String crTerm = "";
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";
		String salesPers = "";
		String salesPers1 = "";
		String salesPers2 = "";
		String pList = "";
		String pListDisc = "";
		String contractType = "";
		String custName = "";
		String frState = "";
		String toState = "";
		String addr1 = "";
		String addr2 = "";
		String addr3 = "";
		String city = "";
		String pin = "";
		String countCode = "";
		String stanCode = "";
		String tranCode = "";
		String transMode = "";
		String stateCode = "";
		String tele1 = "";
		String tele2 = "";
		String tele3 = "";
		String fax = "";
		String dlvTerm = "";
		String currCode = "";
		String crDescr = "";
		String descr = "";
		String descr1 = "";
		String curr = "";
		String uom = "";
		String unitStd = "";
		String unitRate = "";
		String type = "";
		String pack = "";
		String nrp = "";
		String acctPrd = "";
		Timestamp contractDate = null;
		String ldDate = null;
		Timestamp frDate = null;
		Timestamp toDate = null;
		String userId = "",chgTerm = "",loginSiteCode = "";
		String mode = "";
		String updateFlag = "",status = "";
		String drgLicNo = "";
		String itemDescr = "";
		String unit = "";
		String itemFlag = "",rateNesp = "",rateStduom = "",convRtuomStduom = "";
		int count = 0;
		int domID = 0;
		String lineNo = "",rateOptDescr = "",rateOpt = "";
		String discount = "0";
		String custType = "",custTypeDescr = "";
		String priceRule = "",priceRuleDescr = "",orderType = "",orderTypeDescr = "";
		String contractScope = "",contractScopeDescr = "";
		String effFrom = "",division = "",validUpto = "";
		String confirmed = "";
		try
		{
			conn = getConnection();
			ldDate = getCurrdateAppFormat();
			
			System.out.println("Xtra Params="+xtraParams);
			
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
			
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?>\r\n<Root>\r\n<header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</header>\r\n");
			
			System.out.println("currentColumn["+currentColumn+"] && currentFormNo["+currentFormNo+"] && EditFlag["+editFlag+"]");
			
			switch ( currentFormNo )
			{
				case 1:
				{ 
					//valueXmlString.append("<Detail1>");
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("prv_tran_id", dom1));
						if(contractNo.length() > 0)
						{
							contractNo = contractNo.substring(0, contractNo.length()-1);
						}
						
						editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("editFlag", dom1);
						
						System.out.println("ScontractGWTIC via Homepage...editFlag>>>>"+editFlag + " tranId>>>>[" + contractNo + "]");
						
						if( contractNo != null && "E".equalsIgnoreCase(editFlag) || "V".equalsIgnoreCase(editFlag)) //Edit Mode
						{
							
							valueXmlString.append("<Detail1 domID = '"+1+"' objContext='1' selected = 'Y'>\r\n");
							valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\" />\r\n");
							
							sql = " SELECT * FROM SCONTRACT WHERE CONTRACT_NO = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, contractNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								priceRule = checkNullAndTrim(rs.getString("PRICE_RULE"));
								custType = checkNullAndTrim(rs.getString("CUST_TYPE"));
								orderType = checkNullAndTrim(rs.getString("ORDER_TYPE__APPL"));
								custCode = checkNullAndTrim(rs.getString("CUST_CODE"));
								
								if("V".equalsIgnoreCase(editFlag))
								{
									valueXmlString.append("<cust_code protect = '1'>").append("<![CDATA["+rs.getString("CUST_CODE")+"]]>").append("</cust_code>");
									valueXmlString.append("<cust_type protect= '1'>").append("<![CDATA["+rs.getString("CUST_TYPE")+"]]>").append("</cust_type>");
									valueXmlString.append("<price_rule protect = '1'>").append("<![CDATA["+rs.getString("PRICE_RULE")+"]]>").append("</price_rule>");
									valueXmlString.append("<order_type__appl protect = '1'>").append("<![CDATA["+rs.getString("ORDER_TYPE__APPL")+"]]>").append("</order_type__appl>");
									valueXmlString.append("<eff_from protect = '1'>").append("<![CDATA[" +  genericUtility.getValidDateString(rs.getString("EFF_FROM"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
									valueXmlString.append("<valid_upto protect = '1'>").append("<![CDATA["+genericUtility.getValidDateString(rs.getString("VALID_UPTO"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) +"]]>").append("</valid_upto>");
									valueXmlString.append("<dlv_city protect = '1'>").append("<![CDATA["+rs.getString("DLV_CITY")+"]]>").append("</dlv_city>");
									valueXmlString.append("<site_code protect = '1'>").append("<![CDATA["+rs.getString("SITE_CODE")+"]]>").append("</site_code>");
								}
								else
								{
									valueXmlString.append("<cust_code>").append("<![CDATA["+rs.getString("CUST_CODE")+"]]>").append("</cust_code>");
									valueXmlString.append("<cust_type>").append("<![CDATA["+rs.getString("CUST_TYPE")+"]]>").append("</cust_type>");
									valueXmlString.append("<price_rule>").append("<![CDATA["+rs.getString("PRICE_RULE")+"]]>").append("</price_rule>");
									valueXmlString.append("<order_type__appl>").append("<![CDATA["+rs.getString("ORDER_TYPE__APPL")+"]]>").append("</order_type__appl>");
									valueXmlString.append("<eff_from >").append("<![CDATA[" +  genericUtility.getValidDateString(rs.getString("EFF_FROM"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
									valueXmlString.append("<valid_upto>").append("<![CDATA["+genericUtility.getValidDateString(rs.getString("VALID_UPTO"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) +"]]>").append("</valid_upto>");
									valueXmlString.append("<dlv_city >").append("<![CDATA["+rs.getString("DLV_CITY")+"]]>").append("</dlv_city>");
									valueXmlString.append("<site_code >").append("<![CDATA["+rs.getString("SITE_CODE")+"]]>").append("</site_code>");
								}
								
								valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
								valueXmlString.append("<contract_no>").append("<![CDATA["+contractNo+"]]>").append("</contract_no>");
								valueXmlString.append("<emp_code__con >").append("<![CDATA[" +  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode") + "]]>").append("</emp_code__con>");
								valueXmlString.append("<status_date >").append("<![CDATA[" +  ldDate + "]]>").append("</status_date>");
								valueXmlString.append("<tax_date >").append("<![CDATA[" +  ldDate + "]]>").append("</tax_date>");
								valueXmlString.append("<contract_date >").append("<![CDATA[" +  ldDate + "]]>").append("</contract_date>");
								valueXmlString.append("<pl_date >").append("<![CDATA[" +  ldDate + "]]>").append("</pl_date>");
								valueXmlString.append("<part_qty >").append("<![CDATA[Y]]>").append("</part_qty>");
								//valueXmlString.append("<site_code >").append("<![CDATA["+rs.getString("SITE_CODE")+"]]>").append("</site_code>");
								valueXmlString.append("<tax_opt>").append("<![CDATA["+rs.getString("TAX_OPT")+"]]>").append("</tax_opt>");
								valueXmlString.append("<cust_code__bil >").append("<![CDATA["+rs.getString("CUST_CODE__BIL")+"]]>").append("</cust_code__bil>");
								valueXmlString.append("<cr_term >").append("<![CDATA["+rs.getString("CR_TERM")+"]]>").append("</cr_term>");
								valueXmlString.append("<crterm_descr >").append("<![CDATA[]]>").append("</crterm_descr>");
								valueXmlString.append("<cust_code__dlv >").append("<![CDATA["+rs.getString("CUST_CODE__DLV")+"]]>").append("</cust_code__dlv>");
								valueXmlString.append("<tax_chap >").append("<![CDATA["+rs.getString("TAX_CHAP")+"]]>").append("</tax_chap>");
								valueXmlString.append("<tax_class >").append("<![CDATA["+rs.getString("TAX_CLASS")+"]]>").append("</tax_class>");
								valueXmlString.append("<price_list >").append("<![CDATA["+rs.getString("PRICE_LIST")+"]]>").append("</price_list>");
								valueXmlString.append("<price_list__disc >").append("<![CDATA[]]>").append("</price_list__disc>");
								valueXmlString.append("<price_list__clg >").append("<![CDATA[]]>").append("</price_list__clg>");
								valueXmlString.append("<contract_type >").append("<![CDATA[]]>").append("</contract_type>");
								valueXmlString.append("<dlv_to >").append("<![CDATA[]]>").append("</dlv_to>");
								valueXmlString.append("<dlv_add1 >").append("<![CDATA["+rs.getString("DLV_ADD1")+"]]>").append("</dlv_add1>");
								valueXmlString.append("<dlv_add2 >").append("<![CDATA["+rs.getString("DLV_ADD2")+"]]>").append("</dlv_add2>");
								valueXmlString.append("<dlv_add3 >").append("<![CDATA["+rs.getString("DLV_ADD3")+"]]>").append("</dlv_add3>");
								valueXmlString.append("<dlv_pin >").append("<![CDATA["+rs.getString("DLV_PIN")+"]]>").append("</dlv_pin>");
								valueXmlString.append("<count_code__dlv >").append("<![CDATA["+rs.getString("COUNT_CODE__DLV")+"]]>").append("</count_code__dlv>");
								valueXmlString.append("<tran_code >").append("<![CDATA["+rs.getString("TRAN_CODE")+"]]>").append("</tran_code>");
								valueXmlString.append("<trans_mode >").append("<![CDATA["+rs.getString("TRANS_MODE")+"]]>").append("</trans_mode>");
								valueXmlString.append("<stan_code >").append("<![CDATA["+rs.getString("STAN_CODE")+"]]>").append("</stan_code>");
								valueXmlString.append("<state_code__dlv >").append("<![CDATA["+rs.getString("STATE_CODE__DLV")+"]]>").append("</state_code__dlv>");
								valueXmlString.append("<tel1__dlv >").append("<![CDATA["+rs.getString("TEL1__DLV")+"]]>").append("</tel1__dlv>");
								valueXmlString.append("<tel2__dlv >").append("<![CDATA["+rs.getString("TEL2__DLV")+"]]>").append("</tel2__dlv>");
								valueXmlString.append("<tel3__dlv >").append("<![CDATA["+rs.getString("TEL3__DLV")+"]]>").append("</tel3__dlv>");
								valueXmlString.append("<fax__dlv >").append("<![CDATA[]]>").append("</fax__dlv>");
								valueXmlString.append("<curr_code >").append("<![CDATA["+rs.getString("CURR_CODE")+"]]>").append("</curr_code>");
								valueXmlString.append("<exch_rate >").append("<![CDATA["+rs.getString("EXCH_RATE")+"]]>").append("</exch_rate>");
								valueXmlString.append("<currency_descr >").append("<![CDATA[]]>").append("</currency_descr>");
								valueXmlString.append("<station_descr >").append("<![CDATA[]]>").append("</station_descr>");
								valueXmlString.append("<tran_name >").append("<![CDATA[]]>").append("</tran_name>");
								valueXmlString.append("<frt_term >").append("<![CDATA[]]>").append("</frt_term>");
								valueXmlString.append("<curr_code__frt >").append("<![CDATA[]]>").append("</curr_code__frt>");
								valueXmlString.append("<curr_code__ins >").append("<![CDATA[]]>").append("</curr_code__ins>");
								valueXmlString.append("<exch_rate__frt >").append("<![CDATA[]]>").append("</exch_rate__frt>");
								valueXmlString.append("<exch_rate__ins >").append("<![CDATA[]]>").append("</exch_rate__ins>");
								valueXmlString.append("<sales_pers>").append("<![CDATA["+rs.getString("SALES_PERS")+"]]>").append("</sales_pers>");
								valueXmlString.append("<sales_pers__1>").append("<![CDATA["+rs.getString("SALES_PERS__1")+"]]>").append("</sales_pers__1>");
								valueXmlString.append("<sales_pers__2>").append("<![CDATA["+rs.getString("SALES_PERS__2")+"]]>").append("</sales_pers__2>");
								valueXmlString.append("<sp_name>").append("<![CDATA[]]>").append("</sp_name>");
								valueXmlString.append("<comm_perc >").append("<![CDATA[]]>").append("</comm_perc>");
								valueXmlString.append("<curr_code__comm >").append("<![CDATA[]]>").append("</curr_code__comm>");
								valueXmlString.append("<comm_perc__on >").append("<![CDATA[]]>").append("</comm_perc__on>");
								valueXmlString.append("<sp_name_1 >").append("<![CDATA[]]>").append("</sp_name_1>");
								valueXmlString.append("<comm_perc_1 >").append("<![CDATA[]]>").append("</comm_perc_1>");
								valueXmlString.append("<curr_code__comm_1 >").append("<![CDATA[]]>").append("</curr_code__comm_1>");
								valueXmlString.append("<dlv_term >").append("<![CDATA["+rs.getString("DLV_TERM")+"]]>").append("</dlv_term>");
								
								valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
								valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
								valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
								
								sql = " SELECT DESCR FROM SORDERTYPE WHERE ORDER_TYPE = ? ";
								pstmtDescr = conn.prepareStatement(sql);
								pstmtDescr.setString(1, orderType);
								rsDescr = pstmtDescr.executeQuery();
								if(rsDescr.next())
								{
									orderTypeDescr = rsDescr.getString(1)==null?"":rsDescr.getString(1);
								}
								if(rsDescr!=null)
								{
									rsDescr.close();rsDescr = null;
								}
								if(pstmtDescr!=null)
								{
									pstmtDescr.close();pstmtDescr = null;
								}
								
								if("S".equalsIgnoreCase(custType))
								{
									sql = " SELECT FIRST_NAME FROM STRG_CUSTOMER WHERE SC_CODE = ? ";
								}
								else
								{
									sql = " SELECT CUST_NAME FROM CUSTOMER WHERE CUST_CODE = ? ";
								}
								pstmtDescr = conn.prepareStatement(sql);
								pstmtDescr.setString(1, custCode);
								rsDescr = pstmtDescr.executeQuery();
								if(rsDescr.next())
								{
									custName = rsDescr.getString(1)==null?"":rsDescr.getString(1);
								}
								if(rsDescr!=null)
								{
									rsDescr.close();rsDescr = null;
								}
								if(pstmtDescr!=null)
								{
									pstmtDescr.close();pstmtDescr = null;
								}
								
								valueXmlString.append("<cust_name >").append("<![CDATA["+checkNullAndTrim(custName)+"]]>").append("</cust_name>");
								valueXmlString.append("<order_type__appl_descr>").append("<![CDATA["+orderTypeDescr+"]]>").append("</order_type__appl_descr>");
								valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						else //Add Mode
						{
							valueXmlString.append("<Detail1 domID = '"+1+"' objContext='1' selected = 'N'>\r\n");
							valueXmlString.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\" />\r\n");
							
							valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
							valueXmlString.append("<contract_no>").append("<![CDATA[]]>").append("</contract_no>");
							valueXmlString.append("<emp_code__con >").append("<![CDATA[" +  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode") + "]]>").append("</emp_code__con>");
							valueXmlString.append("<status_date >").append("<![CDATA[" +  ldDate + "]]>").append("</status_date>");
							valueXmlString.append("<tax_date >").append("<![CDATA[" +  ldDate + "]]>").append("</tax_date>");
							valueXmlString.append("<contract_date >").append("<![CDATA[" +  ldDate + "]]>").append("</contract_date>");
							valueXmlString.append("<pl_date >").append("<![CDATA[" +  ldDate + "]]>").append("</pl_date>");
							valueXmlString.append("<eff_from >").append("<![CDATA[" +  ldDate + "]]>").append("</eff_from>");
							valueXmlString.append("<part_qty >").append("<![CDATA[Y]]>").append("</part_qty>");
							valueXmlString.append("<site_code >").append("<![CDATA["+genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" )+"]]>").append("</site_code>");
							valueXmlString.append("<cust_code>").append("<![CDATA[]]>").append("</cust_code>");
							valueXmlString.append("<cust_type>").append("<![CDATA[]]>").append("</cust_type>");
							valueXmlString.append("<cust_type__descr>").append("<![CDATA[]]>").append("</cust_type__descr>");
							valueXmlString.append("<price_rule>").append("<![CDATA[]]>").append("</price_rule>");
							valueXmlString.append("<price_rule__descr>").append("<![CDATA[]]>").append("</price_rule__descr>");
							valueXmlString.append("<order_type__appl>").append("<![CDATA[]]>").append("</order_type__appl>");
							valueXmlString.append("<order_type__appl_descr>").append("<![CDATA[]]>").append("</order_type__appl_descr>");
							valueXmlString.append("<valid_upto>").append("<![CDATA["+ldDate+"]]>").append("</valid_upto>");
							valueXmlString.append("<tax_opt>").append("<![CDATA[L]]>").append("</tax_opt>");
							valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
							valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
							valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
							valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
							valueXmlString.append("<dlv_city>").append("<![CDATA[]]>").append("</dlv_city>");
						}
					}
					else if(currentColumn.trim().equalsIgnoreCase("order_type__appl"))
					{
						orderType = checkNullAndTrim(genericUtility.getColumnValue("order_type__appl", dom));
						priceRule = checkNullAndTrim(genericUtility.getColumnValue("price_rule", dom));
						priceRuleDescr = checkNullAndTrim(genericUtility.getColumnValue("price_rule__descr", dom));
						custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom));
						custName = checkNullAndTrim(genericUtility.getColumnValue("cust_name", dom));
						city = checkNullAndTrim(genericUtility.getColumnValue("dlv_city", dom));
						effFromStr = checkNullAndTrim(genericUtility.getColumnValue("eff_from", dom));
						validUpTostr = checkNullAndTrim(genericUtility.getColumnValue("valid_upto", dom));
						custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom));
						custTypeDescr = checkNullAndTrim(genericUtility.getColumnValue("cust_type__descr", dom));
						siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
						contractType = checkNullAndTrim(genericUtility.getColumnValue("contract_type", dom));
						
						sql = " SELECT DESCR FROM SORDERTYPE WHERE ORDER_TYPE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, orderType);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							orderTypeDescr = rs.getString(1)==null?"":rs.getString(1);
						}
						else
						{
							orderTypeDescr = "";
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}
						
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<order_type__appl>").append("<![CDATA["+orderType+"]]>").append("</order_type__appl>");
						valueXmlString.append("<order_type__appl_descr>").append("<![CDATA["+orderTypeDescr+"]]>").append("</order_type__appl_descr>");

						valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
						valueXmlString.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>");
						valueXmlString.append("<dlv_city>").append("<![CDATA["+city+"]]>").append("</dlv_city>");
						valueXmlString.append("<valid_upto>").append("<![CDATA["+genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
						valueXmlString.append("<eff_from>").append("<![CDATA["+genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
						valueXmlString.append("<price_rule>").append("<![CDATA["+priceRule+"]]>").append("</price_rule>");
						valueXmlString.append("<price_rule__descr>").append("<![CDATA["+priceRuleDescr+"]]>").append("</price_rule__descr>");
						valueXmlString.append("<cust_type>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
						valueXmlString.append("<cust_type__descr>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
						valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
						valueXmlString.append("<contract_type>").append("<![CDATA["+contractType+"]]>").append("</contract_type>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("cust_code") || currentColumn.trim().equalsIgnoreCase("cust_type"))
					{
						
						custCode = genericUtility.getColumnValue("cust_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom));
						orderType = checkNullAndTrim(genericUtility.getColumnValue("order_type__appl", dom));
						orderTypeDescr = checkNullAndTrim(genericUtility.getColumnValue("order_type__appl_descr", dom));
						effFromStr = checkNullAndTrim(genericUtility.getColumnValue("eff_from", dom));
						validUpTostr = checkNullAndTrim(genericUtility.getColumnValue("valid_upto", dom));
						priceRule = checkNullAndTrim(genericUtility.getColumnValue("price_rule", dom));
						custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom));
						
						//contractDateAppl = checkNullAndTrim(genericUtility.getColumnValue("contract_date", dom));
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						
						System.out.println("Inside customer item change CustCode["+custCode+"] CustType["+custType+"] ContractDate["+contractDate+"]");
						if("S".equalsIgnoreCase(custType))
						{
							sql = " select  first_name, addr1, addr2,addr3, city, pin, count_code, stan_code, " +
								  " email_addr,mobile_no, state_code, tele1, tele2, tele3,fax ,curr_code,tax_chap,tax_class,tax_env " +
								  " from strg_customer where sc_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custName = rs.getString(1)==null?"":rs.getString(1);
								addr1 = rs.getString(2)==null?"":rs.getString(2);
								addr2 = rs.getString(3)==null?"":rs.getString(3);
								addr3 = rs.getString(4)==null?"":rs.getString(4);
								city = rs.getString(5)==null?"":rs.getString(5);
								pin = rs.getString(6)==null?"":rs.getString(6);
								countCode = rs.getString(7)==null?"":rs.getString(7);
								stanCode = rs.getString(8)==null?"":rs.getString(8);
								stateCode = rs.getString(11)==null?"":rs.getString(11);
								tele1 = rs.getString(12)==null?"":rs.getString(12);
								tele2 = rs.getString(13)==null?"":rs.getString(13);
								tele3 = rs.getString(14)==null?"":rs.getString(14);
								fax = rs.getString(15)==null?"":rs.getString(15);
								curr = rs.getString(16)==null?"":rs.getString(16);
								taxClass = rs.getString(17)==null?"":rs.getString(17);
								taxChap = rs.getString(18)==null?"":rs.getString(18);
								taxEnv = rs.getString(19)==null?"":rs.getString(19);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							//valueXmlString.append("<Detail1 domID='1' objContext='1'>");
							valueXmlString.append("<cust_code>").append("<![CDATA[" +  custCode + "]]>").append("</cust_code>");
							valueXmlString.append("<cust_code__bil >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code__bil>");
							valueXmlString.append("<cust_code__dlv >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code__dlv>");
							valueXmlString.append("<tax_chap >").append("<![CDATA[" +  taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class >").append("<![CDATA[" +  taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<contract_type >").append("<![CDATA[" +  contractType + "]]>").append("</contract_type>");
							
							valueXmlString.append("<cust_name >").append("<![CDATA[" +  custName + "]]>").append("</cust_name>");
							valueXmlString.append("<dlv_to >").append("<![CDATA[" +  custName + "]]>").append("</dlv_to>");
							valueXmlString.append("<dlv_add1 >").append("<![CDATA[" +  addr1 + "]]>").append("</dlv_add1>");
							valueXmlString.append("<dlv_add2 >").append("<![CDATA[" +  addr2 + "]]>").append("</dlv_add2>");
							valueXmlString.append("<dlv_add3 >").append("<![CDATA[" +  addr3 + "]]>").append("</dlv_add3>");
							valueXmlString.append("<dlv_city >").append("<![CDATA[" +  city + "]]>").append("</dlv_city>");
							valueXmlString.append("<dlv_pin >").append("<![CDATA[" +  pin + "]]>").append("</dlv_pin>");
							valueXmlString.append("<count_code__dlv >").append("<![CDATA[" +  countCode + "]]>").append("</count_code__dlv>");
							valueXmlString.append("<tran_code >").append("<![CDATA[" +  tranCode + "]]>").append("</tran_code>");
							valueXmlString.append("<stan_code >").append("<![CDATA[" +  stanCode + "]]>").append("</stan_code>");
							valueXmlString.append("<state_code__dlv >").append("<![CDATA[" +  stateCode.trim() + "]]>").append("</state_code__dlv>");
							valueXmlString.append("<tel1__dlv >").append("<![CDATA[" +  tele1 + "]]>").append("</tel1__dlv>");
							valueXmlString.append("<tel2__dlv >").append("<![CDATA[" +  tele2 + "]]>").append("</tel2__dlv>");
							valueXmlString.append("<tel3__dlv >").append("<![CDATA[" +  tele3 + "]]>").append("</tel3__dlv>");
							valueXmlString.append("<fax__dlv >").append("<![CDATA[" +  fax + "]]>").append("</fax__dlv>");
							valueXmlString.append("<curr_code >").append("<![CDATA[" +  curr + "]]>").append("</curr_code>");
							valueXmlString.append("<dlv_term >").append("<![CDATA[NA]]>").append("</dlv_term>");
							valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
							
							valueXmlString.append("<trans_mode >").append("<![CDATA[R]]>").append("</trans_mode>");
							valueXmlString.append("<exch_rate >").append("<![CDATA[0]]>").append("</exch_rate>");
							valueXmlString.append("<cr_term>").append("<![CDATA[NA]]>").append("</cr_term>");
							
							valueXmlString.append("<valid_upto>").append("<![CDATA["+genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
							valueXmlString.append("<eff_from>").append("<![CDATA["+genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
							valueXmlString.append("<price_rule>").append("<![CDATA["+priceRule+"]]>").append("</price_rule>");
							valueXmlString.append("<cust_type>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
							valueXmlString.append("<order_type__appl>").append("<![CDATA["+orderType+"]]>").append("</order_type__appl>");
							valueXmlString.append("<order_type__appl_descr>").append("<![CDATA["+orderTypeDescr+"]]>").append("</order_type__appl_descr>");
							valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
							valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
							valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
							
							valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
							valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
						}
						//else //Regular customer
						else if("R".equalsIgnoreCase(custType))
						{
							
							sql = " select price_list__clg from site_customer where cust_code = ? and site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								priceListClg = rs.getString(1)==null?"":rs.getString(1);
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}
								
								if(priceListClg == null || !(priceListClg.trim().length() > 0))
								{
									sql = " select price_list__clg from customer where cust_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										priceListClg = rs.getString(1)==null?"":rs.getString(1);
										if(priceListClg == null || !(priceListClg.trim().length() > 0) || priceListClg.trim().equals("NULLFOUND"))
										{
											if(rs!=null)
											{
												rs.close();rs = null;
											}
											if(pstmt!=null)
											{
												pstmt.close();pstmt = null;
											}
											
											sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, "999999");
											pstmt.setString(2, "PRICE_LIST__CLG");
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												priceListClg = rs.getString(1)==null?"":rs.getString(1);
											}
											else
											{
												priceListClg = "NULLFOUND";
											}
											if(rs!=null)
											{
												rs.close();rs = null;
											}
											if(pstmt!=null)
											{
												pstmt.close();pstmt = null;
											}
										}
									}
								}
							}
							sql = " select  PRICE_LIST from site_customer where site_code = ? and cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								priceList = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select cr_term, tax_chap, tax_class, sales_pers, sales_pers__1,sales_pers__2, price_list," +
									" price_list__disc , ORDER_TYPE  from   customer where  cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								crTerm = rs.getString(1)==null?"":rs.getString(1);
								taxChap = rs.getString(2)==null?"":rs.getString(2);
								taxClass = rs.getString(3)==null?"":rs.getString(3);
								salesPers = rs.getString(4)==null?"":rs.getString(4);
								salesPers1 = rs.getString(5)==null?"":rs.getString(5);
								salesPers2 = rs.getString(6)==null?"":rs.getString(6);
								pList = rs.getString(7)==null?"":rs.getString(7);
								pListDisc = rs.getString(8)==null?"":rs.getString(8);
								contractType = rs.getString(9)==null?"":rs.getString(9);
								
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select descr from crterm where cr_term = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								crDescr = rs.getString(1)==null?"":rs.getString(1);							
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							if(priceList == null || priceList.trim().length()==0)
							{
								priceList = pList;
							}
							
							//valueXmlString.append("<Detail1 domID='1' objContext='1'>");
							valueXmlString.append("<cust_code>").append("<![CDATA[" +  custCode + "]]>").append("</cust_code>");
							valueXmlString.append("<cust_code__bil >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code__bil>");
							valueXmlString.append("<cr_term >").append("<![CDATA[" +  crTerm + "]]>").append("</cr_term>");
							valueXmlString.append("<crterm_descr >").append("<![CDATA[" +  crDescr + "]]>").append("</crterm_descr>");
							valueXmlString.append("<cust_code__dlv >").append("<![CDATA[" +  custCode + "]]>").append("</cust_code__dlv>");
							valueXmlString.append("<tax_chap >").append("<![CDATA[" +  taxChap + "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class >").append("<![CDATA[" +  taxClass + "]]>").append("</tax_class>");
							valueXmlString.append("<price_list >").append("<![CDATA[" +  priceList + "]]>").append("</price_list>");
							valueXmlString.append("<price_list__disc >").append("<![CDATA[" +  pListDisc + "]]>").append("</price_list__disc>");
							valueXmlString.append("<price_list__clg >").append("<![CDATA[" +  priceListClg + "]]>").append("</price_list__clg>");
							valueXmlString.append("<contract_type >").append("<![CDATA[" +  contractType + "]]>").append("</contract_type>");
	
							sql = "select	cust_name, addr1, addr2,addr3, city, pin, count_code, stan_code, " +
									"tran_code, trans_mode,state_code, tele1, tele2, tele3,fax ,curr_code" +
									" from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custName = rs.getString(1)==null?"":rs.getString(1);
								addr1 = rs.getString(2)==null?"":rs.getString(2);
								addr2 = rs.getString(3)==null?"":rs.getString(3);
								addr3 = rs.getString(4)==null?"":rs.getString(4);
								city = rs.getString(5)==null?"":rs.getString(5);
								pin = rs.getString(6)==null?"":rs.getString(6);
								countCode = rs.getString(7)==null?"":rs.getString(7);
								stanCode = rs.getString(8)==null?"":rs.getString(8);
								tranCode = rs.getString(9)==null?"":rs.getString(9);
								transMode = rs.getString(10)==null?"":rs.getString(10);
								stateCode = rs.getString(11)==null?"":rs.getString(11);
								tele1 = rs.getString(12)==null?"":rs.getString(12);
								tele2 = rs.getString(13)==null?"":rs.getString(13);
								tele3 = rs.getString(14)==null?"":rs.getString(14);
								fax = rs.getString(15)==null?"":rs.getString(15);
								curr = rs.getString(16)==null?"":rs.getString(16);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							valueXmlString.append("<cust_name >").append("<![CDATA[" +  custName + "]]>").append("</cust_name>");
							valueXmlString.append("<dlv_to >").append("<![CDATA[" +  custName + "]]>").append("</dlv_to>");
							valueXmlString.append("<dlv_add1 >").append("<![CDATA[" +  addr1 + "]]>").append("</dlv_add1>");
							valueXmlString.append("<dlv_add2 >").append("<![CDATA[" +  addr2 + "]]>").append("</dlv_add2>");
							valueXmlString.append("<dlv_add3 >").append("<![CDATA[" +  addr3 + "]]>").append("</dlv_add3>");
							//valueXmlString.append("<dlv_city >").append("<![CDATA[" +  city + "]]>").append("</dlv_city>");
							valueXmlString.append("<dlv_pin >").append("<![CDATA[" +  pin + "]]>").append("</dlv_pin>");
							valueXmlString.append("<count_code__dlv >").append("<![CDATA[" +  countCode + "]]>").append("</count_code__dlv>");
							valueXmlString.append("<tran_code >").append("<![CDATA[" +  tranCode + "]]>").append("</tran_code>");
							valueXmlString.append("<trans_mode >").append("<![CDATA[" +  transMode + "]]>").append("</trans_mode>");
							valueXmlString.append("<stan_code >").append("<![CDATA[" +  stanCode + "]]>").append("</stan_code>");
							valueXmlString.append("<state_code__dlv >").append("<![CDATA[" +  stateCode.trim() + "]]>").append("</state_code__dlv>");
							valueXmlString.append("<tel1__dlv >").append("<![CDATA[" +  tele1 + "]]>").append("</tel1__dlv>");
							valueXmlString.append("<tel2__dlv >").append("<![CDATA[" +  tele2 + "]]>").append("</tel2__dlv>");
							valueXmlString.append("<tel3__dlv >").append("<![CDATA[" +  tele3 + "]]>").append("</tel3__dlv>");
							valueXmlString.append("<fax__dlv >").append("<![CDATA[" +  fax + "]]>").append("</fax__dlv>");
							valueXmlString.append("<curr_code >").append("<![CDATA[" +  curr + "]]>").append("</curr_code>");
							
							sql = "select descr,std_exrt from   currency where  curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, curr);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								currDescr = rs.getString(1)==null?"":rs.getString(1); 
								num = rs.getInt(2);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
							valueXmlString.append("<exch_rate >").append("<![CDATA[" +  num + "]]>").append("</exch_rate>");
							valueXmlString.append("<currency_descr >").append("<![CDATA[" +  currDescr + "]]>").append("</currency_descr>");
							
							//descr = "                                        ";
							//descr1 = "                                        ";
							//num = 0;
							
							/*sql = "select descr from station where  stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}*/
							
							//valueXmlString.append("<station_descr>").append("<![CDATA[" +  descr + "]]>").append("</station_descr>");
							
							descr = "";
							descr1 = "";
							num = 0;
							
							sql = "select tran_name, frt_term, curr_code from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
								descr1 = rs.getString(2)==null?"":rs.getString(2);
								curr = rs.getString(3)==null?"":rs.getString(3);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							valueXmlString.append("<tran_name >").append("<![CDATA[" +  descr + "]]>").append("</tran_name>");
							valueXmlString.append("<frt_term >").append("<![CDATA[" +  descr1.trim() + "]]>").append("</frt_term>");
							valueXmlString.append("<curr_code__frt >").append("<![CDATA[" +  curr + "]]>").append("</curr_code__frt>");
							valueXmlString.append("<curr_code__ins >").append("<![CDATA[" +  curr + "]]>").append("</curr_code__ins>");
							valueXmlString.append("<exch_rate__frt >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__frt>");
							valueXmlString.append("<exch_rate__ins >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__ins>");
							
							//descr = "                                        ";
							//descr1 = "                                        ";
							//num = 0;
							//currCode = "          ";
							
							/*sql = "select sp_name, comm_perc, comm_perc__on,curr_code from sales_pers where sales_pers = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, salesPers);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
								num = rs.getInt(2);
								descr1 = rs.getString(3)==null?"":rs.getString(3);
								currCode = rs.getString(4)==null?"":rs.getString(4);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}*/
							
							/*valueXmlString.append("<sales_pers >").append("<![CDATA[" +  salesPers + "]]>").append("</sales_pers>");
							valueXmlString.append("<sp_name >").append("<![CDATA[" +  descr + "]]>").append("</sp_name>");
							valueXmlString.append("<comm_perc >").append("<![CDATA[" +  num + "]]>").append("</comm_perc>");
							valueXmlString.append("<curr_code__comm >").append("<![CDATA[" +  currCode + "]]>").append("</curr_code__comm>");*/
							
							/*if(descr1 != null)
							{
								valueXmlString.append("<comm_perc__on >").append("<![CDATA[" +  descr1 + "]]>").append("</comm_perc__on>");
							}*/
							//siteCode = genericUtility.getColumnValue("site_code", dom);
							/*contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");*/
							//num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
							//valueXmlString.append("<exch_rate__comm >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm>");
							
							//descr = "                                        ";
							//descr1 = "                                        ";
							//num = 0;
							//currCode = "          ";
	
							/*sql = "select sp_name, comm_perc, comm_perc__on ,curr_code from  sales_pers	where sales_pers = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, salesPers1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
								num = rs.getInt(2);
								descr1 = rs.getString(3)==null?"":rs.getString(3);
								currCode = rs.getString(4)==null?"":rs.getString(4);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}*/
							
							/*valueXmlString.append("<sales_pers__1 >").append("<![CDATA[" +  salesPers1 + "]]>").append("</sales_pers__1>");
							valueXmlString.append("<sp_name_1 >").append("<![CDATA[" +  descr + "]]>").append("</sp_name_1>");
							valueXmlString.append("<comm_perc_1 >").append("<![CDATA[" +  num + "]]>").append("</comm_perc_1>");
							valueXmlString.append("<curr_code__comm_1 >").append("<![CDATA[" +  checkNullAndTrim(currCode) + "]]>").append("</curr_code__comm_1>");
							
							if(descr1 != null)
							{
								valueXmlString.append("<comm_perc_on_1 >").append("<![CDATA[" +  checkNullAndTrim(descr1) + "]]>").append("</comm_perc_on_1>");
							}*/
							
							//siteCode = genericUtility.getColumnValue("site_code", dom);
							//contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							//contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							//num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
							//valueXmlString.append("<exch_rate__comm_1 >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm_1>");
							
							//descr = "                                        ";
							//descr1 = "                                        ";
							//num = 0;
							//currCode = "          ";
	
							/*sql = "select sp_name, comm_perc, comm_perc__on ,curr_code from  sales_pers	where sales_pers = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, salesPers2);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = rs.getString(1)==null?"":rs.getString(1);
								num = rs.getInt(2);
								descr1 = rs.getString(3)==null?"":rs.getString(3);
								currCode = rs.getString(4)==null?"":rs.getString(4);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}*/
							
							/*valueXmlString.append("<sales_pers__2 >").append("<![CDATA[" +  salesPers2 + "]]>").append("</sales_pers__2>");
							valueXmlString.append("<sp_name_2 >").append("<![CDATA[" +  descr + "]]>").append("</sp_name_2>");
							valueXmlString.append("<comm_perc_2 >").append("<![CDATA[" +  num + "]]>").append("</comm_perc_2>");
							valueXmlString.append("<curr_code__comm_2 >").append("<![CDATA[" +  checkNullAndTrim(currCode) + "]]>").append("</curr_code__comm_2>");
							
							if(descr1 != null)
							{
								valueXmlString.append("<comm_perc_on_2 >").append("<![CDATA[" + checkNullAndTrim(descr1) + "]]>").append("</comm_perc_on_2>");
							}*/
							
							siteCode = genericUtility.getColumnValue("site_code", dom);
							itemSer = genericUtility.getColumnValue("item_ser", dom);
							//contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							//contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							//num = getDailyExchRate(curr , "",siteCode , contractDate , "S" , conn);
							//valueXmlString.append("<exch_rate__comm_2 >").append("<![CDATA[" +  num + "]]>").append("</exch_rate__comm_2>");
							
	
							sql = "select dlv_term from customer_series where cust_code = ? and item_ser = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemSer);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								dlvTerm = rs.getString(1)==null?"":rs.getString(1);
							} 
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							if(dlvTerm == null || dlvTerm.trim().length() == 0)
							{
								sql = "select dlv_term from customer where  cust_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									dlvTerm = rs.getString(1)==null?"":rs.getString(1);
								}
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}
							}
							
							if(dlvTerm == null || dlvTerm.trim().length() == 0)
							{
								dlvTerm = "NA";
							}
							valueXmlString.append("<dlv_term >").append("<![CDATA[" +  dlvTerm + "]]>").append("</dlv_term>");
							valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
							valueXmlString.append("<valid_upto>").append("<![CDATA["+genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
							valueXmlString.append("<eff_from>").append("<![CDATA["+genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
							valueXmlString.append("<price_rule>").append("<![CDATA["+priceRule+"]]>").append("</price_rule>");
							valueXmlString.append("<cust_type>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
							valueXmlString.append("<order_type__appl>").append("<![CDATA["+orderType+"]]>").append("</order_type__appl>");
							valueXmlString.append("<order_type__appl_descr>").append("<![CDATA["+orderTypeDescr+"]]>").append("</order_type__appl_descr>");
							valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
							valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
							valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
							valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
							valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
							valueXmlString.append("<dlv_city>").append("<![CDATA[" +  city + "]]>").append("</dlv_city>");
						}
					}
					valueXmlString.append("</Detail1>");
				}
				break;
				
				case 2:
				{
					editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("edit_status", dom1);
					System.out.println("Case 2 ItemChange editFlag["+editFlag+"]");
					
					mode = checkEditMode(dom,"2");
					
					if("E".equalsIgnoreCase(mode))
					{
						updateFlag = "E";
						status = "O";
					}
					else
					{
						updateFlag = "A";
						status = "N";
					}
					
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						//Changed by wasim on 27-03-2017 for discount editable and non edit able.
						priceRule = checkNullAndTrim(genericUtility.getColumnValue("price_rule", dom1));
						
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						
						if(preDomExists(dom2, "2") && !"V".equalsIgnoreCase(editFlag))
						{
							System.out.println("Detail2 previous form exist");
							
							valueXmlString.append(getPreFormXML(dom2,"2",priceRule));
						}
						else
						{	
							System.out.println("Contract Number["+contractNo+"] valid_upto["+validUpTostr+"] effFromStr["+effFromStr+"]");
							if(contractNo.length() > 0)
							{
								sql = " SELECT LINE_NO,VALID_UPTO,EFF_FROM,DIVISION,CONTRACT_SCOPE,PRICE_LIST,DISCOUNT FROM SCONTRACT_APPL WHERE CONTRACT_NO = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, contractNo);
								rs = pstmt.executeQuery();
								
								while(rs.next())
								{
									lineNo = checkNullAndTrim(rs.getString("LINE_NO"));
									contractScope = checkNullAndTrim(rs.getString("CONTRACT_SCOPE"));
									
									//lineNo = "   " + lineNo;
									//lineNo = lineNo.substring( lineNo.length()-3 );
									
									valueXmlString.append("<Detail2 domID = '"+lineNo.trim()+"' objContext='2' selected = 'Y'>\r\n");
									valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\" />\r\n");
									valueXmlString.append("<contract_no protect ='1'>").append("<![CDATA[" +  contractNo + "]]>").append("</contract_no>");
									valueXmlString.append("<line_no protect ='1'>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
									
									if("V".equalsIgnoreCase(editFlag))
									{
										//valueXmlString.append("<valid_upto protect ='1'>").append("<![CDATA[" + genericUtility.getValidDateString(rs.getString("VALID_UPTO"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
										//valueXmlString.append("<eff_from protect ='1'>").append("<![CDATA[" + genericUtility.getValidDateString(rs.getString("EFF_FROM"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
										valueXmlString.append("<valid_upto protect ='1'>").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
										valueXmlString.append("<eff_from protect ='1'>").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
										valueXmlString.append("<division protect ='1'>").append("<![CDATA["+ rs.getString("DIVISION") +"]]>").append("</division>");
										valueXmlString.append("<contract_scope protect ='1'>").append("<![CDATA["+  rs.getString("CONTRACT_SCOPE") +"]]>").append("</contract_scope>");
										valueXmlString.append("<contract_scope__descr protect ='1'>").append("<![CDATA["+contractScopeDescr+"]]>").append("</contract_scope__descr>");
										valueXmlString.append("<price_list protect ='1'>").append("<![CDATA["+  checkNullAndTrim(rs.getString("PRICE_LIST")) +"]]>").append("</price_list>");
										valueXmlString.append("<discount protect ='1'>").append("<![CDATA["+  rs.getDouble("DISCOUNT") +"]]>").append("</discount>");
									}
									else
									{
										//valueXmlString.append("<valid_upto >").append("<![CDATA[" + genericUtility.getValidDateString(rs.getString("VALID_UPTO"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
										//valueXmlString.append("<eff_from >").append("<![CDATA[" + genericUtility.getValidDateString(rs.getString("EFF_FROM"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
										valueXmlString.append("<valid_upto>").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
										valueXmlString.append("<eff_from>").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
										valueXmlString.append("<division>").append("<![CDATA["+ rs.getString("DIVISION") +"]]>").append("</division>");
										valueXmlString.append("<contract_scope>").append("<![CDATA["+  rs.getString("CONTRACT_SCOPE") +"]]>").append("</contract_scope>");
										valueXmlString.append("<contract_scope__descr>").append("<![CDATA["+contractScopeDescr+"]]>").append("</contract_scope__descr>");
										valueXmlString.append("<price_list>").append("<![CDATA["+  checkNullAndTrim(rs.getString("PRICE_LIST")) +"]]>").append("</price_list>");
										//Changed by wasim on 27-03-2017 for discount editable and non edit able [START]
										//valueXmlString.append("<discount>").append("<![CDATA["+  rs.getDouble("DISCOUNT") +"]]>").append("</discount>");
										if("1".equalsIgnoreCase(priceRule))
										{
											valueXmlString.append("<discount protect ='0'>").append("<![CDATA["+  rs.getDouble("DISCOUNT") +"]]>").append("</discount>");
										}
										else
										{
											valueXmlString.append("<discount protect ='1'>").append("<![CDATA["+  rs.getDouble("DISCOUNT") +"]]>").append("</discount>");
										}
										//Changed by wasim on 27-03-2017 for discount editable and non edit able [END]
									}
									
									valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
									valueXmlString.append("</Detail2>");
									
									count++;
								}
								if(rs != null)
								{
									rs.close();rs = null;
								}	
								if(pstmt != null)
								{	
									pstmt.close();pstmt = null;
								}
							}
						}
					}
					else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						//Changed by wasim on 27-03-2017 for discount editable and non edit able.
						priceRule = checkNullAndTrim(genericUtility.getColumnValue("price_rule", dom1));
						
						domID = getMaxLineNoFromDOM(dom,"2");
						lineNo = "   " + domID;
						lineNo = lineNo.substring( lineNo.length()-3 );

						valueXmlString.append("<Detail2 domID='" + domID + "' objContext=\"2\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						valueXmlString.append("<line_no protect ='1'>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
						valueXmlString.append("<contract_no protect ='1'>").append("<![CDATA[" +  contractNo + "]]>").append("</contract_no>");
						valueXmlString.append("<valid_upto >").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
						valueXmlString.append("<eff_from >").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
						valueXmlString.append("<division>").append("<![CDATA[]]>").append("</division>");
						valueXmlString.append("<contract_scope>").append("<![CDATA[]]>").append("</contract_scope>");
						valueXmlString.append("<contract_scope__descr>").append("<![CDATA[]]>").append("</contract_scope__descr>");
						valueXmlString.append("<price_list>").append("<![CDATA[]]>").append("</price_list>");
						//Changed by wasim on 27-03-2017 for discount editable and non edit able [START]
						//valueXmlString.append("<discount>").append("<![CDATA[0]]>").append("</discount>");
						if("1".equalsIgnoreCase(priceRule))
						{
							valueXmlString.append("<discount protect ='0'>").append("<![CDATA[0]]>").append("</discount>");
						}
						else
						{
							valueXmlString.append("<discount protect ='1'>").append("<![CDATA[0]]>").append("</discount>");
						}
						//Changed by wasim on 27-03-2017 for discount editable and non edit able [END]
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("</Detail2>");
					}
				}
				break;
				case 3:
				{
					editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("edit_status", dom1);
					System.out.println("Case 3 ItemChange editFlag["+editFlag+"]");
					
					mode = checkEditMode(dom, "3");
					if("E".equalsIgnoreCase(mode))
					{
						updateFlag = "E";
						status = "O";
					}
					else
					{
						updateFlag = "A";
						status = "N";
					}
					
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						siteCode = genericUtility.getColumnValue("site_code", dom1); 
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
						custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom1));
						
						if(siteCodeShip == null || siteCodeShip.trim().length() == 0)
						{
							siteCodeShip = siteCode;
						}
						
						if(preDomExists(dom2, "3"))
						{
							System.out.println("Detail3 previous form exist");
						}
						else
						{
						
							if(contractNo.length() > 0)
							{
								sql = " SELECT S.LINE_NO,S.RATE_OPT,S.ITEM_CODE,I.DESCR,S.SITE_CODE,S.VALID_UPTO,S.EFF_FROM,S.UNIT,S.UNIT__STD,S.ITEM_FLG,"
									+ " S.TAX_CHAP,S.TAX_ENV,S.TAX_CLASS,S.RATE,S.RATE__CLG,S.UNIT__RATE,S.RATE__STDUOM,S.CONV__RTUOM_STDUOM,"
									+ " S.PRICE_LIST,S.DISCOUNT,I.ITEM_SER "
									+ " FROM SCONTRACTDET S, ITEM I WHERE S.CONTRACT_NO = ? AND I.ITEM_CODE = S.ITEM_CODE ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, contractNo);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									lineNo = checkNullAndTrim(rs.getString("LINE_NO"));
									
									lineNo = "   " + lineNo;
									lineNo = lineNo.substring( lineNo.length()-3 );
									
									rateOpt = checkNullAndTrim(rs.getString("RATE_OPT"));
									
									itemSer = checkNullAndTrim(rs.getString("ITEM_SER"));
									
									//Changed by wasim on 08-09-2017 as commented bcoz in edit mode it will be set from database [START]
									/*HashMap hm = new HashMap();
									hm = getDiscountPer(dom2,"2",itemSer);
									discount = (String) hm.get("DISCOUNT");
									priceList = (String) hm.get("PRICE_LIST");*/
									discount = checkNullAndTrim(rs.getString("DISCOUNT"));
									priceList = checkNullAndTrim(rs.getString("PRICE_LIST"));
									//Changed by wasim on 08-09-2017 as commented bcoz in edit mode it will be set from database [END]
									
									
									valueXmlString.append("<Detail3 domID='"+lineNo.trim()+"' objContext='3' selected=\"Y\">\r\n");
									valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\" />\r\n");
									
									valueXmlString.append("<contract_no>").append("<![CDATA["+contractNo+"]]>").append("</contract_no>");
									valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
									
									if("V".equalsIgnoreCase(editFlag))
									{
										valueXmlString.append("<item_code protect = '1'>").append("<![CDATA["+rs.getString("ITEM_CODE")+"]]>").append("</item_code>");
										valueXmlString.append("<rate_opt protect = '1'>").append("<![CDATA["+checkNullAndTrim(rs.getString("RATE_OPT"))+"]]>").append("</rate_opt>");
										valueXmlString.append("<rate_opt__descr>").append("<![CDATA["+rateOptDescr+"]]>").append("</rate_opt__descr>");
										valueXmlString.append("<discount protect = '1'>").append("<![CDATA["+discount+"]]>").append("</discount>");
										valueXmlString.append("<price_list protect = '1'>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
									}
									else
									{
										valueXmlString.append("<item_code>").append("<![CDATA["+rs.getString("ITEM_CODE")+"]]>").append("</item_code>");
										valueXmlString.append("<rate_opt>").append("<![CDATA["+checkNullAndTrim(rs.getString("RATE_OPT"))+"]]>").append("</rate_opt>");
										valueXmlString.append("<rate_opt__descr>").append("<![CDATA["+rateOptDescr+"]]>").append("</rate_opt__descr>");
										valueXmlString.append("<discount>").append("<![CDATA["+discount+"]]>").append("</discount>");
										valueXmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
									}
									
									
									valueXmlString.append("<item_descr>").append("<![CDATA["+rs.getString("DESCR")+"]]>").append("</item_descr>");
									//valueXmlString.append("<eff_from>").append("<![CDATA["+ genericUtility.getValidDateString(rs.getString("EFF_FROM"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) +"]]>").append("</eff_from>");
									//valueXmlString.append("<valid_upto>").append("<![CDATA["+ genericUtility.getValidDateString(rs.getString("VALID_UPTO"), genericUtility.getDBDateTimeFormat(), genericUtility.getApplDateFormat()) +"]]>").append("</valid_upto>");
									valueXmlString.append("<valid_upto>").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
									valueXmlString.append("<eff_from>").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
									valueXmlString.append("<site_code>").append("<![CDATA["+rs.getString("SITE_CODE")+"]]>").append("</site_code>");
									valueXmlString.append("<quantity>").append("<![CDATA[0]]>").append("</quantity>");
									valueXmlString.append("<status_date>").append("<![CDATA["+ldDate+"]]>").append("</status_date>");
									valueXmlString.append("<dsp_date>").append("<![CDATA[]]>").append("</dsp_date>");
									valueXmlString.append("<unit>").append("<![CDATA["+checkNullAndTrim(rs.getString("UNIT"))+"]]>").append("</unit>");
									valueXmlString.append("<unit__std>").append("<![CDATA["+checkNullAndTrim(rs.getString("UNIT__STD"))+"]]>").append("</unit__std>");
									valueXmlString.append("<item_flg>").append("<![CDATA["+checkNullAndTrim(rs.getString("ITEM_FLG"))+"]]>").append("</item_flg>");	
									valueXmlString.append("<tax_chap>").append("<![CDATA["+checkNullAndTrim(rs.getString("TAX_CHAP"))+"]]>").append("</tax_chap>");
									valueXmlString.append("<tax_class>").append("<![CDATA["+checkNullAndTrim(rs.getString("TAX_CLASS"))+"]]>").append("</tax_class>");
									valueXmlString.append("<tax_env>").append("<![CDATA["+checkNullAndTrim(rs.getString("TAX_ENV"))+"]]>").append("</tax_env>");
									valueXmlString.append("<rate>").append("<![CDATA["+rs.getDouble("RATE")+"]]>").append("</rate>");
									valueXmlString.append("<rate__clg>").append("<![CDATA["+rs.getDouble("RATE__CLG")+"]]>").append("</rate__clg>");
									valueXmlString.append("<rate__nesp>").append("<![CDATA[]]>").append("</rate__nesp>");
									valueXmlString.append("<unit__rate>").append("<![CDATA["+checkNullAndTrim(rs.getString("UNIT__RATE"))+"]]>").append("</unit__rate>");
									valueXmlString.append("<rate__stduom>").append("<![CDATA["+rs.getDouble("RATE__STDUOM")+"]]>").append("</rate__stduom>");
									valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA["+rs.getDouble("CONV__RTUOM_STDUOM")+"]]>").append("</conv__rtuom_stduom>");
									valueXmlString.append("<chg_date>").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
									valueXmlString.append("<chg_user>").append("<![CDATA["+userId+"]]>").append("</chg_user>");
									valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
									
									valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
									valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
									valueXmlString.append("</Detail3>");
									
									count++;
								}
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}
							}
						}
					}
					else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom1));
						siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom1));
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						
						System.out.println("itm_default_add Case 3-->valid_upto["+validUpTostr+"] and eff_from["+effFromStr+"]");
						
						domID = getMaxLineNoFromDOM(dom,"3");
						
						lineNo = "   " + domID;
						lineNo = lineNo.substring( lineNo.length()-3 );
						//lineNo = ""+domID;
						
						valueXmlString.append("<Detail3 domID='" + domID + "' objContext=\"3\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						
						valueXmlString.append("<contract_no >").append("<![CDATA[" +  contractNo + "]]>").append("</contract_no>");
						valueXmlString.append("<item_code>").append("<![CDATA[]]>").append("</item_code>");
						valueXmlString.append("<item_descr >").append("<![CDATA[]]>").append("</item_descr>");
						valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
						valueXmlString.append("<quantity>").append("<![CDATA[0]]>").append("</quantity>");
						valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
						valueXmlString.append("<status_date >").append("<![CDATA[]]>").append("</status_date>");
						valueXmlString.append("<dsp_date>").append("<![CDATA[]]>").append("</dsp_date>");
						valueXmlString.append("<valid_upto>").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
						valueXmlString.append("<eff_from>").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
						valueXmlString.append("<unit >").append("<![CDATA["+unit+"]]>").append("</unit>");
						valueXmlString.append("<item_flg >").append("<![CDATA["+itemFlag+"]]>").append("</item_flg>");	
						valueXmlString.append("<tax_chap >").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class >").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");
						valueXmlString.append("<tax_env >").append("<![CDATA["+taxEnv+"]]>").append("</tax_env>");
						valueXmlString.append("<rate>").append("<![CDATA["+rate+"]]>").append("</rate>");
						valueXmlString.append("<rate__clg >").append("<![CDATA["+rateClg+"]]>").append("</rate__clg>");
						valueXmlString.append("<rate__nesp >").append("<![CDATA["+rateNesp+"]]>").append("</rate__nesp>");
						valueXmlString.append("<unit__rate >").append("<![CDATA["+unitRate+"]]>").append("</unit__rate>");
						valueXmlString.append("<rate__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");
						valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA["+convRtuomStduom+"]]>").append("</conv__rtuom_stduom>");
						valueXmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
						valueXmlString.append("<rate_opt>").append("<![CDATA["+rateOpt+"]]>").append("</rate_opt>");
						valueXmlString.append("<rate_opt__descr>").append("<![CDATA["+rateOptDescr+"]]>").append("</rate_opt__descr>");
						valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
						valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
						valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
						valueXmlString.append("<discount>").append("<![CDATA["+discount+"]]>").append("</discount>");
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
						valueXmlString.append("</Detail3>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("item_code"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
						custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom1));
						itemCode = genericUtility.getColumnValue("item_code", dom);
						lineNo = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));
						priceList = checkNullAndTrim(genericUtility.getColumnValue("price_list", dom));
						rateOpt = checkNullAndTrim(genericUtility.getColumnValue("rate_opt", dom));
						rateOptDescr = genericUtility.getColumnValue("rate_opt__descr", dom);
						//effFromStr = genericUtility.getColumnValue("eff_from", dom);
						//validUpTostr = genericUtility.getColumnValue("valid_upto", dom);
						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						
						System.out.println("Detail 2effFromStr["+effFromStr+"] and validUpTostr["+validUpTostr+"]");
						
						lineNo = "   " + lineNo;
						lineNo = lineNo.substring( lineNo.length()-3 );
						
						valueXmlString.append("<Detail3 domID='" + lineNo.trim() + "' objContext=\"3\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\""+updateFlag+"\"  status=\""+status+"\" pkNames=\"\" />\r\n");
						
						valueXmlString.append("<contract_no>").append("<![CDATA["+contractNo+"]]>").append("</contract_no>");
						valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
						valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
						//valueXmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
						//valueXmlString.append("<rate_opt>").append("<![CDATA["+rateOpt+"]]>").append("</rate_opt>");
						//valueXmlString.append("<rate_opt__descr>").append("<![CDATA["+rateOptDescr+"]]>").append("</rate_opt__descr>");
						valueXmlString.append("<valid_upto >").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
						valueXmlString.append("<eff_from >").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
						valueXmlString.append("<status_date >").append("<![CDATA["+ ldDate +"]]>").append("</status_date>");
						
						if(siteCodeShip == null || siteCodeShip.trim().length() == 0)
						{
							siteCodeShip = siteCode;
						}
						contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
						contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
						//custCode = genericUtility.getColumnValue("cust_code", dom1);
						toStation = genericUtility.getColumnValue("stan_code", dom1);
						
						sql = "select item_ser from siteitem where site_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							itemSer = rs.getString(1)==null?"":rs.getString(1);
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}

						if(itemSer ==null || itemSer.trim().length() == 0)
						{
							sql = "select item_ser from itemser_change" +
									" where item_code = ? and eff_date <= ? and " +
									" (valid_upto >= ? or valid_upto is null)";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setTimestamp(2, contractDate);
							pstmt.setTimestamp(3, contractDate);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						if(itemSer ==null || itemSer.trim().length() == 0)
						{
							sql = "select item_ser__old ser from itemser_change where item_code = ? " +
									" and eff_date >= ? and (valid_upto >= ? or valid_upto is null) " +
									" and eff_date = (select min(eff_date) from itemser_change where item_code = ?)";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setTimestamp(2, contractDate);
							pstmt.setTimestamp(3, contractDate);
							pstmt.setString(4, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						if(itemSer ==null || itemSer.trim().length() == 0)
						{
							sql = "select item_ser from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								itemSer = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						
						//Changed on 09-MAY-2017 as per mail suggested by Sudessh Achari
						/*sql = "select item_ser__inv from customer_series where cust_code = ? and item_ser = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemSer);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							itemSerInv = rs.getString(1)==null?"":rs.getString(1);
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}
						
						if(itemSerInv != null && itemSerInv.trim().length() > 0)
						{
							itemSer = itemSerInv;
						}*/
						
						sql = "Select descr, unit, item_stru,pack_instr from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString(1)==null?"":rs.getString(1);
							uom = rs.getString(2)==null?"":rs.getString(2);
							type = rs.getString(3)==null?"":rs.getString(3);
							pack = rs.getString(4)==null?"":rs.getString(4);
							
							valueXmlString.append("<item_code>").append("<![CDATA[" +  itemCode + "]]>").append("</item_code>");
							valueXmlString.append("<item_descr protect ='1'>").append("<![CDATA[" +  descr + "]]>").append("</item_descr>");
							valueXmlString.append("<unit >").append("<![CDATA[" +  uom + "]]>").append("</unit>");
							valueXmlString.append("<unit__std >").append("<![CDATA[" +  uom + "]]>").append("</unit__std>");
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							if(unitRate == null || unitRate.trim().length() == 0 )
							{
								valueXmlString.append("<unit__rate >").append("<![CDATA[" +  uom + "]]>").append("</unit__rate>");
							}
							if(type.equals("F"))
							{
								valueXmlString.append("<item_flg >").append("<![CDATA[F]]>").append("</item_flg>");	
							}
							else 
							{
								valueXmlString.append("<item_flg >").append("<![CDATA[I]]>").append("</item_flg>");
							}
							valueXmlString.append("<pack_instr >").append("<![CDATA[" +  pack + "]]>").append("</pack_instr>");
						}
						else 
						{
							valueXmlString.append("<item_code >").append("<![CDATA[" +  itemCode + "]]>").append("</item_code>");
							valueXmlString.append("<item_descr >").append("<![CDATA[" +  descr + "]]>").append("</item_descr>");
							valueXmlString.append("<unit >").append("<![CDATA[" +  uom + "]]>").append("</unit>");
							valueXmlString.append("<unit__std >").append("<![CDATA[" +  uom + "]]>").append("</unit__std>");
							unitRate = genericUtility.getColumnValue("unit__rate", dom);
							if(unitRate == null || unitRate.trim().length() == 0 )
							{
								valueXmlString.append("<unit__rate >").append("<![CDATA[" +  uom + "]]>").append("</unit__rate>");
							}
							valueXmlString.append("<item_flg >").append("<![CDATA[B]]>").append("</item_flg>");
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}
						
						//Commented by wasim on 09-MAY-17 for unnecessory code [START]
						/*sql = "SELECT stan_code FROM site WHERE site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeShip);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							fromStation = rs.getString(1)==null?"":rs.getString(1);
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}*/
						
						/*sql = "select tax_chap from customeritem where cust_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxChap = rs.getString(1)==null?"":rs.getString(1);
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}
						
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from siteitem where site_code = ? and item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from itemser where item_ser = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						if(taxChap == null || taxChap.trim().length() == 0 )
						{
							sql = "select tax_chap from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxChap = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						sql = "select tax_class from customeritem where cust_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxClass = rs.getString(1)==null?"":rs.getString(1);
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}

						if(taxClass == null || taxClass.trim().length() == 0 )
						{
							sql = "select tax_class from site_customer where site_code = ? and cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, custCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxClass = rs.getString(1)==null?"":rs.getString(1);
							}
							else
							{
								if(rs!=null)
								{
									rs.close();rs = null;
								}
								if(pstmt!=null)
								{
									pstmt.close();pstmt = null;
								}

								sql = "select tax_class from customer where cust_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next()) 
								{
									taxClass = rs.getString(1)==null?"":rs.getString(1);
								}
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						if((taxClass == null || taxClass.trim().length() == 0) && itemCode != null && itemCode.trim().length() > 0 )
						{
							sql = "select tax_class from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxClass = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}
						sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class = ? and tax_chap = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1, fromStation);
						pstmt.setString(2, toStation);
						pstmt.setString(3, taxClass);
						pstmt.setString(4, taxChap);
						rs = pstmt.executeQuery();
						if(rs.next()) 
						{
							taxEnv = rs.getString(1)==null?"":rs.getString(1);
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class	  = '     ' and tax_chap = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							pstmt.setString(2, toStation);
							pstmt.setString(3, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class = ? and tax_chap = '     '";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							pstmt.setString(2, toStation);
							pstmt.setString(3, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE stan_code__fr = ? and stan_code__to = ? and tax_class = '     ' and tax_chap  = '     ' ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							pstmt.setString(2, toStation);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "select state_code from station where stan_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, fromStation);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								frState = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select state_code from station where stan_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, toStation);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								frState = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}

							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? and state_code__to = ? and stan_code__fr = '     ' and stan_code__to = '     ' and tax_class = ?  and tax_chap = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							pstmt.setString(3, taxClass);
							pstmt.setString(4, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? and " +
									" state_code__to = ? and stan_code__fr = '     ' and " +
									" stan_code__to = '     ' and tax_class = '     ' and tax_chap      = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							pstmt.setString(3, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? AND state_code__to = ? AND stan_code__fr = '     ' and " +
									" stan_code__to = '     ' and tax_class = ? and tax_chap = '          ' ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							pstmt.setString(3, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}

						if(taxEnv == null || taxEnv.trim().length() == 0 )
						{
							sql = "SELECT tax_env FROM tenvstan WHERE state_code__fr = ? and " +
									"state_code__to = ? and stan_code__fr = '     ' and " +
									" stan_code__to = '     ' and tax_class  = '     ' and tax_chap   = '          '";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, frState);
							pstmt.setString(2, toState);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								taxEnv = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
						}*/
						//Commented by wasim on 09-MAY-17 for unnecessory code [END]
						valueXmlString.append("<tax_chap >").append("<![CDATA[" +  taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class >").append("<![CDATA[" +  taxClass + "]]>").append("</tax_class>");
						valueXmlString.append("<tax_env >").append("<![CDATA[" +  taxEnv + "]]>").append("</tax_env>");
						
						/*priceList = genericUtility.getColumnValue("PRICE_LIST", dom1);
						priceListClg = genericUtility.getColumnValue("PRICE_LIST__CLG", dom1);
						//String quantityStr = genericUtility.getColumnValue("QUANTITY", dom).trim();
						String quantityStr = genericUtility.getColumnValue("QUANTITY", dom);
						System.out.println("QUANTITY CHECK ["+quantityStr+"]");
						
						try
						{
							quantity = Integer.parseInt(quantityStr);
						}
						catch(Exception exc)
						{
							System.out.println("line no 1160 error");
						}
						
						if(priceList != null && priceList.trim().length() > 0 )
						{
							rate = getPickRate(priceList, contractDate,itemCode,"","L",quantity,conn);
							if(rate == -1 && getPriceListType(priceList,conn).equals("B"))
							{
								rate = 0;
							}
							valueXmlString.append("<rate>").append("<![CDATA[" +  rate + "]]>").append("</rate>");
						}
						if(priceListClg != null && priceListClg.trim().length() > 0)
						{
							rate = getPickRate(priceList, contractDate,itemCode,"","L",quantity,conn);
							if(rate == -1 && getPriceListType(priceList,conn).equals("B"))
							{
								rateClg = 0;
							}
							valueXmlString.append("<rate__clg >").append("<![CDATA[" +  rateClg + "]]>").append("</rate__clg>");
						}
						else
						{
							valueXmlString.append("<rate__clg >").append("<![CDATA[" +  rate + "]]>").append("</rate__clg>");
						}*/
						
						//Changed by wasim for rate [START]
						//Commented temporary
						/*reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail3>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail3>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);*/
						/*try
						{
							num = Integer.parseInt(genericUtility.getColumnValue("rate", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1449 linr"+e1);
						}
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						try
						{
							num1 = Integer.parseInt(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						}
						catch(Exception e1)
						{
							System.out.println("Error at 1460 linr"+e1);
						}
						num3 = num1;
						if(unitRate == null || unitRate.trim().length() == 0)
						{
							sql = "Select unit from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								unitRate = rs.getString(1)==null?"":rs.getString(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							num = getConvQuantityFact(unitStd,unitRate,itemCode,num,num1,conn);
							valueXmlString.append("<unit__rate >").append("<![CDATA[" +  unitRate + "]]>").append("</unit__rate>");
						}
						else
						{
							num = getConvQuantityFact(unitStd,unitRate,itemCode,num,num1,conn);
						}
						if(num3 == 0)
						{
							valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" +  num1 + "]]>").append("</conv__rtuom_stduom>");
						}
						valueXmlString.append("<rate__stduom >").append("<![CDATA[" +  num2 + "]]>").append("</rate__stduom>");
						priceListClg = genericUtility.getColumnValue("PRICE_LIST__CLG", dom1);
						if(priceListClg == null || priceListClg.trim().length() == 0)
						{
							valueXmlString.append("<rate__clg >").append("<![CDATA[" +  num + "]]>").append("</rate__clg>");
						}*/
						//Changed by wasim for rate [END]
						
						/*if(itemCode != null && itemCode.trim().length() > 0 )
						{
							contractDateStr = genericUtility.getValidDateString(genericUtility.getColumnValue("contract_date", dom1) , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
							contractDate = Timestamp.valueOf(contractDateStr + " 00:00:00.0");
							sql = "select fr_date, to_date, code from acctprd where fr_date <= ? and to_date >= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, contractDate);
							pstmt.setTimestamp(2, contractDate);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								frDate = rs.getTimestamp(1);
								toDate = rs.getTimestamp(2);
								acctPrd = rs.getString(3)==null?"":rs.getString(3);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select min(code), max(code) from period where acct_prd = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, acctPrd);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								prdCdFr = rs.getInt(1);
								prdCdTo = rs.getInt(2);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select sum(b.quantity) from salesforecast_hdr a, salesforecast_det b" +
									" where a.tran_id = b.tran_id and " +
									"(case when a.confirmed is null then 'N' else a.confirmed end) = 'Y' " +
									"and b.item_code = ? and	b.prd_code__for >= ? " +
									"and b.prd_code__for <= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setInt(2, prdCdFr);
							pstmt.setInt(3, prdCdTo);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								forecastQtn = rs.getInt(1);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select sum(b.quantity__stduom), sum(b.quantity__stduom * b.rate__stduom) " +
									"from despatch a, despatchdet b	where a.desp_id = b.desp_id" +
									" and (case when a.confirmed is null then 'N' else a.confirmed end) = 'Y' " +
									" and a.conf_date between ? and ?	and	b.item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, frDate);
							pstmt.setTimestamp(2, toDate);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								despQtn = rs.getInt(1);
								despValue = rs.getInt(2);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "select sum(b.bal_qty_stduom), sum(b.bal_qty_stduom * b.rate) " +
									"from scontract a, scontractdet b where a.contract_no = b.contract_no " +
									"and (case when a.confirmed is null then 'N' else a.confirmed end) = 'Y'" +
									" and a.conf_date between ? and ? and b.item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setTimestamp(1, frDate);
							pstmt.setTimestamp(2, toDate);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								balQtn = rs.getInt(1);
								balValue = rs.getInt(2);
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							int balForecastQtn = forecastQtn - balQtn - despQtn;
							sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, "999999");
							pstmt.setString(2, "NRP");
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								nrp = rs.getString(1)==null?"":rs.getString(1);
							}
							else
							{
								nrp = "NULLFOUND";
							}
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							if(nrp.equals("NULLFOUND") || nrp.trim().length()== 0)
							{
								nrpRate = 0;
							}
							else
							{
								priceList = genericUtility.getColumnValue("PRICE_LIST", dom1);
								nrpRate = getPickRate(priceList,nrp,contractDate,itemCode,"","L",conn);
								if(nrpRate < 0)
								{
									nrpRate = 0;
								}
							}
							int nespValue = despValue + balValue +( balForecastQtn * nrpRate );
							int nespQtn =  despQtn + balQtn + balForecastQtn;
							if(nespQtn > 0 && nespValue > 0)
							{
								nespRate = nespValue / nespQtn;
							}
							else
							{
								nespRate = 0;
							}
						}
						else
						{
							nespRate = 0;
						}*/
						valueXmlString.append("<rate__nesp >").append("<![CDATA[" +  nespRate + "]]>").append("</rate__nesp>");
						
						valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
						valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
						valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
						
						
						/*HashMap hm = new HashMap();
						hm = getDiscountPer(dom2,"2",itemSer);
						discount = (String) hm.get("DISCOUNT");
						priceList = (String) hm.get("PRICE_LIST");
						valueXmlString.append("<discount>").append("<![CDATA["+discount+"]]>").append("</discount>");
						valueXmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");*/
					
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
						valueXmlString.append("</Detail3>");
					}
					else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						effFromStr = genericUtility.getColumnValue("eff_from", dom1);
						validUpTostr = genericUtility.getColumnValue("valid_upto", dom1);
						siteCode = genericUtility.getColumnValue("site_code", dom1);
						custCode = genericUtility.getColumnValue("cust_code", dom1);
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
						rateOpt = genericUtility.getColumnValue("rate_opt", dom);
						rateOptDescr = genericUtility.getColumnValue("rate_opt__descr", dom);
						priceList = genericUtility.getColumnValue("price_list", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						itemDescr = genericUtility.getColumnValue("item_descr", dom);
						lineNo = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));
						unit = genericUtility.getColumnValue("unit", dom);
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						itemFlag = genericUtility.getColumnValue("item_flg", dom);
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						unitRate = genericUtility.getColumnValue("unit__rate", dom);
						rateNesp = genericUtility.getColumnValue("rate__nesp", dom);
						rateStduom = genericUtility.getColumnValue("rate__stduom", dom);
						convRtuomStduom = genericUtility.getColumnValue("conv__rtuom_stduom", dom);
						discount = genericUtility.getColumnValue("discount", dom);
						
						try
						{
							rate = Double.parseDouble(genericUtility.getColumnValue("rate", dom));
							rateClg = Double.parseDouble(genericUtility.getColumnValue("rate__clg", dom));
						}
						catch(Exception e)
						{
							System.out.println("Excpetion for rate and rate__clg="+e.getMessage());
						}
						
						lineNo = "   " + lineNo;
						lineNo = lineNo.substring( lineNo.length()-3 );
						
						valueXmlString.append("<Detail3 domID='" + lineNo.trim() + "' objContext=\"3\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\""+updateFlag+"\"  status=\""+status+"\" pkNames=\"\" />\r\n");
						
						valueXmlString.append("<contract_no >").append("<![CDATA[" +  contractNo + "]]>").append("</contract_no>");
						valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");
						valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
						valueXmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
						valueXmlString.append("<quantity>").append("<![CDATA[0]]>").append("</quantity>");
						valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
						valueXmlString.append("<status_date >").append("<![CDATA["+ ldDate +"]]>").append("</status_date>");
						valueXmlString.append("<dsp_date>").append("<![CDATA["+contractDateStr+"]]>").append("</dsp_date>");
						valueXmlString.append("<valid_upto >").append("<![CDATA[" +  genericUtility.getValidDateString(validUpTostr , genericUtility.getApplDateFormat()) + "]]>").append("</valid_upto>");
						valueXmlString.append("<eff_from >").append("<![CDATA[" +  genericUtility.getValidDateString(effFromStr , genericUtility.getApplDateFormat()) + "]]>").append("</eff_from>");
						valueXmlString.append("<unit >").append("<![CDATA["+unit+"]]>").append("</unit>");
						valueXmlString.append("<unit__std>").append("<![CDATA["+unitStd+"]]>").append("</unit__std>");
						valueXmlString.append("<item_flg >").append("<![CDATA["+itemFlag+"]]>").append("</item_flg>");	
						valueXmlString.append("<tax_chap >").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class >").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");
						valueXmlString.append("<tax_env >").append("<![CDATA["+taxEnv+"]]>").append("</tax_env>");
						valueXmlString.append("<rate>").append("<![CDATA["+rate+"]]>").append("</rate>");
						valueXmlString.append("<rate__clg>").append("<![CDATA["+rateClg+"]]>").append("</rate__clg>");
						valueXmlString.append("<rate__nesp>").append("<![CDATA["+rateNesp+"]]>").append("</rate__nesp>");
						valueXmlString.append("<unit__rate>").append("<![CDATA["+unitRate+"]]>").append("</unit__rate>");
						valueXmlString.append("<rate__stduom>").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA["+convRtuomStduom+"]]>").append("</conv__rtuom_stduom>");
						valueXmlString.append("<rate_opt>").append("<![CDATA["+rateOpt+"]]>").append("</rate_opt>");
						valueXmlString.append("<rate_opt__descr>").append("<![CDATA["+rateOptDescr+"]]>").append("</rate_opt__descr>");
						valueXmlString.append("<chg_date >").append("<![CDATA["+ldDate+"]]>").append("</chg_date>");
						valueXmlString.append("<chg_user >").append("<![CDATA["+userId+"]]>").append("</chg_user>");
						valueXmlString.append("<chg_term >").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
						valueXmlString.append("<discount>").append("<![CDATA["+discount+"]]>").append("</discount>");
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
						valueXmlString.append("</Detail3>");
					}
				}
				break;
				case 4:
				{
					editFlag = (editFlag != null) ? editFlag : genericUtility.getColumnValue("edit_status", dom1);
					System.out.println("Case 4 ItemChange editFlag["+editFlag+"]");
					
					mode = checkEditMode(dom, "4");
					if("E".equalsIgnoreCase(mode))
					{
						updateFlag = "E";
						status = "O";
					}
					else
					{
						updateFlag = "A";
						status = "N";
					}
					
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom1));
						
						if(preDomExists(dom2, "4"))
						{
							System.out.println("Detail4 previous form exist");
						}
						else
						{
							if(contractNo.length() > 0)
							{
								sql = " SELECT * FROM SCONTACT_CUST WHERE CONTRACT_NO = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, contractNo);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									lineNo = rs.getString("LINE_NO");
									custCode = checkNullAndTrim(rs.getString("CUST_CODE"));
									custType = checkNullAndTrim(rs.getString("CUST_TYPE"));
									
									if("S".equalsIgnoreCase(custType))
									{
										sql = " SELECT FIRST_NAME,CITY FROM STRG_CUSTOMER WHERE SC_CODE = ? ";
									}
									else
									{
										sql = " SELECT CUST_NAME,CITY FROM CUSTOMER WHERE CUST_CODE = ? ";
									}
									pstmtDescr = conn.prepareStatement(sql);
									pstmtDescr.setString(1, custCode);
									rsDescr = pstmtDescr.executeQuery();
									if(rsDescr.next())
									{
										custName = checkNullAndTrim(rsDescr.getString(1));
										city = checkNullAndTrim(rsDescr.getString(2));
									}
									else
									{
										custName = "";
									}
									if(rsDescr != null)
									{
										rsDescr.close();rsDescr = null;
									}	
									if(pstmtDescr != null)
									{	
										pstmtDescr.close();pstmtDescr = null;
									}
									
									valueXmlString.append("<Detail4 domID = '"+lineNo+"' objContext='4' selected = 'Y'>\r\n");
									valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\" />\r\n");
									valueXmlString.append("<contract_no>").append("<![CDATA[" +contractNo+"]]>").append("</contract_no>");
									valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
									
									if("V".equalsIgnoreCase(editFlag))
									{
										valueXmlString.append("<cust_code protect = '1'>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
										valueXmlString.append("<cust_name protect = '1'>").append("<![CDATA["+custName+"]]>").append("</cust_name>");
										valueXmlString.append("<city protect = '1'>").append("<![CDATA["+city+"]]>").append("</city>");
										valueXmlString.append("<drug_lic_no protect = '1'>").append("<![CDATA["+checkNullAndTrim(rs.getString("DRUG_LIC_NO"))+"]]>").append("</drug_lic_no>");
										valueXmlString.append("<cust_type protect = '1'>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
										valueXmlString.append("<cust_type__descr protect = '1'>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
									}
									else
									{
										valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
										valueXmlString.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>");
										valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");
										valueXmlString.append("<drug_lic_no>").append("<![CDATA["+checkNullAndTrim(rs.getString("DRUG_LIC_NO"))+"]]>").append("</drug_lic_no>");
										valueXmlString.append("<cust_type>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
										valueXmlString.append("<cust_type__descr>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
									}
									
									valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
									valueXmlString.append("</Detail4>");
									count++;
								}
								if(rs != null)
								{
									rs.close();rs = null;
								}	
								if(pstmt != null)
								{	
									pstmt.close();pstmt = null;
								}
							}
						}	
					}
					else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom1));
						
						domID = getMaxLineNoFromDOM(dom,"4");
						lineNo = "   " + domID;
						lineNo = lineNo.substring( lineNo.length()-3 );

						valueXmlString.append("<Detail4 domID='" + domID + "' objContext=\"4\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
						
						valueXmlString.append("<contract_no>").append("<![CDATA[" +contractNo+"]]>").append("</contract_no>");
						valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
						valueXmlString.append("<cust_code>").append("<![CDATA[]]>").append("</cust_code>");
						valueXmlString.append("<cust_name>").append("<![CDATA[]]>").append("</cust_name>");
						valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");
						valueXmlString.append("<drug_lic_no>").append("<![CDATA[]]>").append("</drug_lic_no>");
						valueXmlString.append("<cust_type>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
						valueXmlString.append("<cust_type__descr>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("</Detail4>");
					}
					else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
					{
						contractNo = checkNullAndTrim(genericUtility.getColumnValue("contract_no", dom1));
						lineNo = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));
						custCode = checkNullAndTrim(genericUtility.getColumnValue("cust_code", dom));
						custName = checkNullAndTrim(genericUtility.getColumnValue("cust_name", dom));
						city = checkNullAndTrim(genericUtility.getColumnValue("city", dom));
						drgLicNo = checkNullAndTrim(genericUtility.getColumnValue("drug_lic_no", dom));
						//custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom));
						custType = checkNullAndTrim(genericUtility.getColumnValue("cust_type", dom1));
						custTypeDescr = checkNullAndTrim(genericUtility.getColumnValue("cust_type__descr", dom));
						
						lineNo = "   " + lineNo;
						lineNo = lineNo.substring( lineNo.length()-3 );
						
						if("S".equalsIgnoreCase(custType))
						{
							sql = " SELECT FIRST_NAME,CITY FROM STRG_CUSTOMER WHERE SC_CODE = ? ";
						}
						else
						{
							sql = " SELECT CUST_NAME,CITY FROM CUSTOMER WHERE CUST_CODE = ? ";
						}
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custName = checkNullAndTrim(rs.getString(1));
							city = checkNullAndTrim(rs.getString(2));
						}
						else
						{
							custName = "";
							city = "";
						}
						if(rs != null)
						{
							rs.close();rs = null;
						}	
						if(pstmt != null)
						{	
							pstmt.close();pstmt = null;
						}
						
						valueXmlString.append("<Detail4 domID='" + lineNo.trim() + "' objContext=\"4\" selected=\"Y\">\r\n");
						valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\""+updateFlag+"\"  status=\""+status+"\" pkNames=\"\" />\r\n");
						
						valueXmlString.append("<contract_no>").append("<![CDATA[" +contractNo+"]]>").append("</contract_no>");
						valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
						valueXmlString.append("<cust_code>").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
						valueXmlString.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>");
						valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");
						valueXmlString.append("<drug_lic_no>").append("<![CDATA["+drgLicNo+"]]>").append("</drug_lic_no>");
						valueXmlString.append("<cust_type>").append("<![CDATA["+custType+"]]>").append("</cust_type>");
						valueXmlString.append("<cust_type__descr>").append("<![CDATA["+custTypeDescr+"]]>").append("</cust_type__descr>");
						valueXmlString.append("<edit_status>").append("<![CDATA["+editFlag+"]]>").append("</edit_status>");
						valueXmlString.append("</Detail4>");
					}
				}
				break;
			}
		}
		catch(Exception e)
		{
			System.out.println("Inside ScontractGwtIC ItemChange Exception="+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}	
				if(pstmt != null)
				{	
					pstmt.close();pstmt = null;
				}
				if(rsDescr!=null)
				{
					rsDescr.close();rsDescr = null;
				}
				if(pstmtDescr!=null)
				{
					pstmtDescr.close();pstmtDescr = null;
				}
				if(conn != null)
				{	
					conn.close();conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}			
		}

		valueXmlString.append("</Root>");
		System.out.println("valueXmlString ::"+valueXmlString.toString());

		return valueXmlString.toString();
	}
	
	/**
	 * select error description from MESSAGES
	 * @param conn
	 * @param errorCode
	 * @return
	 * @throws ITMException 
	 */
	private String errorType(Connection conn , String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		}		
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}
	
	private int getDailyExchRate(String curr, String string, String siteCode,Timestamp contractDate, String string2, Connection conn) throws ITMException 
	{
		int exchRateSell = 0;
		String sql = "";
		String varValue = "";
		String finEntity = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(curr == null || curr.trim().length() == 0)
		{
			try
			{
				sql = "select fin_entity from site where site_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					finEntity = rs.getString(1)==null?"":rs.getString(1);

					if(rs != null)
					{
						rs.close();rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();pstmt = null;
					}
					
					sql = "select curr_code from finent where  fin_entity = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						curr = rs.getString(1)==null?"":rs.getString(1);
					}
					if(rs != null)
					{
						rs.close();rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();pstmt = null;
					}
				}
				sql = "select exch_rate__sell from daily_exch_rate_sell_buy where curr_code = ? and " +
						" curr_code__to = ?  and ? between from_date and to_date ";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1, curr);
				pstmt.setString(2, curr);
				pstmt.setTimestamp(3, contractDate);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					exchRateSell = rs.getInt(1);
				}
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
				if(exchRateSell == 0)
				{
					sql = "select exch_rate from daily_exch_rate_sell_buy where curr_code = ? " +
							" and curr_code__to = ? and ? between from_date and to_date";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, curr);
					pstmt.setString(2, curr);
					pstmt.setTimestamp(3, contractDate);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						exchRateSell = rs.getInt(1);
					}
					if(rs != null)
					{
						rs.close();rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();pstmt = null;
					}
				}
				if(exchRateSell != 0)
				{
					exchRateSell = 1 / exchRateSell;
				}
				sql = "select rtrim(case when var_value is null then 'Y' else var_value end) " +
						" from finparm where prd_code = '999999' and var_name = 'EXCRT_CURR'";
				pstmt =  conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					varValue = rs.getString(1);
				}
				if(!rs.next() || varValue.equals("Y"))
				{
					if(rs != null)
					{
						rs.close();rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();pstmt = null;
					}
					
					sql = "select std_exrt from currency where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, curr);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						exchRateSell = rs.getInt(1);
					}
				}
				else
				{
					return 0;
				}
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
				
			}
			catch (Exception e)
			{
				System.out.println("Exception in getDailyExchRate ........");
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
			finally
			{
				try
				{
					if(rs != null)
					{
						rs.close();rs = null;
					}
					if(pstmt != null)
					{
						pstmt.close();pstmt = null;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}	
		}
		return exchRateSell;
	}
	
	private int getPickRate(String priceList,String nrp, Timestamp contractDate,String itemCode, String string, String string2, Connection conn) throws ITMException 
	{
		int nrpRate = 0;
		String sql = "";
		String priceListParent = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try
		{
			sql = "select rate from pricelist where price_list = ? and item_code  = ?" +
				" and list_type = 'L' and eff_from <= ?  and valid_upto >= ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, contractDate);
			pstmt.setTimestamp(4, contractDate);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				nrpRate = rs.getInt(1);				
			}

			else
			{
				String pList = priceList;
				do
				{
					if(rs!=null)
					{
						rs.close();rs = null;
					}
					if(pstmt!=null)
					{
						pstmt.close();pstmt = null;
					}
					
					sql = "lect (case when price_list__parent is null  then '' else price_list__parent end ) " +
							" from pricelist_mst where price_list =  ? and list_type = 'L'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, pList);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						priceListParent = rs.getString(1)==null?"":rs.getString(1);
						if(priceListParent.trim().length() > 0)
						{
							sql = "select rate from pricelist where price_list = ? and item_code  = ? " +
									" and list_type = 'L' and eff_from <= ?  and valid_upto >= ?";
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1, priceListParent);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, contractDate);
							pstmt1.setTimestamp(4, contractDate);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()) 
							{
								nrpRate = rs.getInt(1);
							}
							if(rs1!=null)
							{
								rs1.close();rs1 = null;
							}
							if(pstmt1!=null)
							{
								pstmt1.close();pstmt1 = null;
							}

							if(nrpRate > 0)
							{
								break;
							}
							else
							{
								pList = priceListParent;
								priceListParent = null;
							}
						}
					}
					else
					{
						return -1;
					}
				}while(true);
			}
			if(rs!=null)
			{
				rs.close();rs = null;
			}
			if(pstmt!=null)
			{
				pstmt.close();pstmt = null;
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in Pick rate get.......");
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
				if(rs1!=null)
				{
					rs1.close();rs1 = null;
				}
				if(pstmt1!=null)
				{
					pstmt1.close();pstmt1 = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		return nrpRate;
	}
	private String getPriceListType(String priceList,Connection conn) throws ITMException 
	{
		String sql = "";
		String priceListType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			sql = " select list_type from pricelist where price_list  = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				priceListType = rs.getString(1)==null?"":rs.getString(1);
			}
			if(rs!=null)
			{
				rs.close();rs = null;
			}
			if(pstmt!=null)
			{
				pstmt.close();pstmt = null;
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in getPriceListType ........" );
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		return priceListType;
	}

	private int getPickRate(String priceList, Timestamp contractDate,String itemCode, String string, String string2, int quantity, Connection conn) throws ITMException 
	{
		int rate = 0;
		String sql = "";
		String priceListParent = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try
		{
			if(getPriceListType(priceList,conn).equals(""))
			{
				return -1;
			}
			sql = "select rate from pricelist where price_list = ? and item_code  = ? and list_type = 'L' " +
					" and eff_from <= ? and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			pstmt.setString(2, itemCode);
			pstmt.setTimestamp(3, contractDate);
			pstmt.setTimestamp(4, contractDate);
			pstmt.setInt(5, quantity);
			pstmt.setInt(6, quantity);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				rate = rs.getInt(1);
			}
			else
			{
				String pList = priceList;
				do
				{
					if(rs!=null)
					{
						rs.close();rs = null;
					}
					if(pstmt!=null)
					{
						pstmt.close();pstmt = null;
					}
					
					sql = "select (case when price_list__parent is null  then '' else price_list__parent end ) " +
							" from pricelist_mst where price_list = ? and list_type = 'L'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, pList);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						priceListParent = rs.getString(1)==null?"":rs.getString(1);
						if(priceListParent.trim().length() > 0)
						{
							sql = "select rate from pricelist where price_list = ? " +
									" and item_code  = ?  and list_type = 'L' and eff_from <= ? " +
									" and valid_upto >= ? and min_qty <= ? and max_qty >= ?";
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1, priceListParent);
							pstmt1.setString(2, itemCode);
							pstmt1.setTimestamp(3, contractDate);
							pstmt1.setTimestamp(4, contractDate);
							pstmt1.setInt(5, quantity);
							pstmt1.setInt(6, quantity);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()) 
							{
								rate = rs.getInt(1);
							}
							if(rs1!=null)
							{
								rs1.close();rs1 = null;
							}
							if(pstmt1!=null)
							{
								pstmt1.close();pstmt1 = null;
							}
							if(rate > 0)
							{
								break;
							}
							else
							{
								pList = priceListParent;
								priceListParent = null;
							}
						}
					}
					else
					{
						return -1;
					}
				}while(true);
			}
			if(rs!=null)
			{
				rs.close();rs = null;
			}
			if(pstmt!=null)
			{
				pstmt.close();pstmt = null;
			}
		}
		catch(Exception exc1)
		{
			System.out.println("GET PICK RATE EXCEPTION////////");
			throw new ITMException(exc1); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
				if(rs1!=null)
				{
					rs1.close();rs1 = null;
				}
				if(pstmt1!=null)
				{
					pstmt1.close();pstmt1 = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		return rate;
	}
	
	private int getConvQuantityFact(String uom, String unitStd,String itemCode, int quantity, int num1,Connection conn) throws ITMException 
	{
		int cnt = 0;
		int fact = 0;
		int newQty = 0;
		int roundTo = 0;
		int conv = 0;
		String sql = "";
		String round = "";
		String order = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(uom == null && unitStd == null)
		{
			fact = 1;
			return num1;
		}
		else if(uom != null && unitStd != null && uom.equals(unitStd))
		{
			fact = 1;
			return num1;
		}
		try
		{
			sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and item_code = ?";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1, uom);
			pstmt.setString(2, unitStd);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				cnt = rs.getInt(1);
				if(cnt == 0 )
				{
					if(rs!=null)
					{
						rs.close();rs = null;
					}
					if(pstmt!=null)
					{
						pstmt.close();pstmt = null;
					}
					
					sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and item_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, unitStd);
					pstmt.setString(2, uom);
					pstmt.setString(3, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						cnt = rs.getInt(1);
						if(cnt == 0 )
						{
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and  item_code = 'X'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, uom);
							pstmt.setString(2, unitStd);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								cnt = rs.getInt(1);
								if(cnt == 0)
								{
									if(rs!=null)
									{
										rs.close();rs = null;
									}
									if(pstmt!=null)
									{
										pstmt.close();pstmt = null;
									}
									
									sql = "Select Count(*) from UomConv where unit__fr = ? and unit__to = ? and  item_code = 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, unitStd);
									pstmt.setString(2, uom);
									pstmt.setString(3, itemCode);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										if(cnt == 0)
										{
											return -999999999;
										}
										else
										{
											if(rs!=null)
											{
												rs.close();rs = null;
											}
											if(pstmt!=null)
											{
												pstmt.close();pstmt = null;
											}
											
											sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
											pstmt =  conn.prepareStatement(sql);
											pstmt.setString(1, unitStd);
											pstmt.setString(2, uom);
											rs = pstmt.executeQuery();
											if(rs.next()) 
											{
												conv = rs.getInt(1);
												round = rs.getString(2)==null?"":rs.getString(2);
												roundTo = rs.getInt(3);
												order = "REVORD";
											}											
										}
									}
								}
								else
								{
									if(rs!=null)
									{
										rs.close();rs = null;
									}
									if(pstmt!=null)
									{
										pstmt.close();pstmt = null;
									}
									
									sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, uom);
									pstmt.setString(2, unitStd);
									rs = pstmt.executeQuery();
									if(rs.next()) 
									{
										conv = rs.getInt(1);
										round = rs.getString(2)==null?"":rs.getString(2);
										roundTo = rs.getInt(3);
										order = "ACTORD";
									}			
								}
							}
						}
						else
						{
							if(rs!=null)
							{
								rs.close();rs = null;
							}
							if(pstmt!=null)
							{
								pstmt.close();pstmt = null;
							}
							
							sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, unitStd);
							pstmt.setString(2, uom);
							rs = pstmt.executeQuery();
							if(rs.next()) 
							{
								conv = rs.getInt(1);
								round = rs.getString(2)==null?"":rs.getString(2);
								roundTo = rs.getInt(3);
								order = "REVORD";
							}										
						}
					}
				}
				else
				{
					if(rs!=null)
					{
						rs.close();rs = null;
					}
					if(pstmt!=null)
					{
						pstmt.close();pstmt = null;
					}
					
					sql = "Select Fact, Round, round_to from UomConv where unit__fr = ? and unit__to = ? and item_code = 'X'";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1, uom);
					pstmt.setString(2, unitStd);
					rs = pstmt.executeQuery();
					if(rs.next()) 
					{
						conv = rs.getInt(1);
						round = rs.getString(2)==null?"":rs.getString(2);
						roundTo = rs.getInt(3);
						order = "ACTORD";
					}			
				}
			}
			if(rs!=null)
			{
				rs.close();rs = null;
			}
			if(pstmt!=null)
			{
				pstmt.close();pstmt = null;
			}
			if(fact == 0)
			{
				if(order.equals("ACTORD"))
				{
					newQty = conv * num1;
					fact = conv;
				}
				else
				{
					newQty = 1 / conv * num1;
					fact = 1 / conv;
				}
			}
			else
			{
				newQty = fact * num1;
			}
			newQty = getQuantity(newQty,round,roundTo);
			
		}
		catch (Exception e) 
		{
			System.out.println("Exception in getConvQuantityFact........");
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		return newQty;
	}
	
	private int getQuantity(int newQty, String round, int roundTo) 
	{
		if(newQty < 0)
		{
			newQty = Math.abs(newQty);
		}
		else if(newQty == 0)
		{
			return newQty;
		}
		if(round.equals("N"))
		{
			return newQty;
		}
		if(roundTo == 0)
		{
			return newQty;
		}
		if(round.equals("X"))
		{
			if(newQty % roundTo > 0)
			{
				newQty = newQty - (newQty % roundTo) + roundTo;
			}
		}
		else if(round.equals("P"))
		{
			newQty = newQty - (newQty % roundTo);
		}
		else if(round.equals("R"))
		{
			if(newQty % roundTo < roundTo/2)
			{
				newQty = newQty - (newQty % roundTo);
			}
			else
			{
				newQty = newQty - (newQty % roundTo) + roundTo;
			}
		}
		return newQty;
	}
	
	private String getCurrdateAppFormat() 
	{
		String s = "";
		GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(timestamp).toString();
		}
		catch (Exception localException)
		{
		}

		return s;
	}
	
	public int getMaxLineNoFromDOM(Document dom,String objContext)throws Exception
	{
		int lineNo = 1;
		ArrayList lineNoList = new ArrayList();
		Integer maxLineNo = new Integer("1");
		try
		{
			lineNoList.add(1);//Initially assign default line number 1
			
			System.out.println("DOM="+genericUtility.serializeDom(dom));
			System.out.println("OBJContext["+objContext+"]");
			
			Node parentNode = null;
			NodeList detailNodeList =dom.getElementsByTagName("Detail"+objContext);
			int detailNodeListlen = detailNodeList.getLength();
			System.out.println("Detail Length =========================================="+detailNodeListlen);
			for(int ctrH = 0; ctrH < detailNodeListlen ; ctrH++)
			{
				parentNode = detailNodeList.item(ctrH);
				
				if (parentNode.getAttributes().getNamedItem( "domID" ) != null )
				{
					lineNo = Integer.parseInt((parentNode.getAttributes().getNamedItem( "domID" ).getNodeValue()).trim());
					System.out.println("Line number is="+lineNo);
					lineNoList.add(lineNo);
				}
			}	
			System.out.println("LineNumberList ["+lineNoList+"]");
			/*maxLineNo = Collections.max(lineNoList);*/  //commented for error
		}
		catch(Exception e)
		{
			System.out.println("Exception="+e.getMessage());
			throw new Exception(e);
		}
		
		System.out.println("Returning maxLine Number="+maxLineNo);
		return lineNo;
	}
	
	public String checkEditMode(Document dom,String objContext)throws Exception
	{
		String mode = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int childNodeListLength;
		int ctr = 0;
		try
		{
			System.out.println("Inside checkEditMode objContext["+objContext+"]");
			
			parentNodeList = dom.getElementsByTagName("Detail"+objContext);
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			
			System.out.println("childNodeListLength="+childNodeListLength);
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				
				//System.out.println("childNodeName["+childNodeName+"]");
				if("attribute".equalsIgnoreCase(childNodeName))
				{
					mode = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
					//statusChk = childNode.getAttributes().getNamedItem("status").getNodeValue();
					//selectedChk = childNode.getAttributes().getNamedItem("selected").getNodeValue();
					System.out.println("@@Inside Mode["+mode+"]");
				}
			}	
		}
		catch(Exception e)
		{
			System.out.println("Exception checkEditMode="+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		System.out.println("Returning Mode for ObjContext["+objContext+"]-->["+mode+"]");
		return mode;
	}
	
	public boolean preDomExists(Document dom, String currentFormNo) throws ITMException
	{
		System.out.println("Inside preDomExists method::["+dom+"]");
		NodeList parentList = null;
		NodeList childList = null;
		Node childNode = null;
		boolean selected = false;

		try
		{
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);			
			if (parentList == null || parentList.getLength()==0)
			{
				System.out.println("Inside preDomExists method parentList null::");
				return selected;
			}

			if ( parentList.item(0) != null )
			{
				childList = parentList.item(0).getChildNodes();
				System.out.println("Inside preDomExists method childList ::"+childList);
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);
					System.out.println("Inside preDomExists method childNode ::"+childNode);
					if((childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null))					
					{
						System.out.println("Column found!!!" + childNode.getNodeName());
						selected = true; 
						break;
					}
				}
			}


		}
		catch ( Exception e )
		{
			System.out.println( "Exception :Scontract :preDomExists :==>\n"+e.getMessage());
			throw new ITMException(e);
		}

		return selected;
	}
	
	
	
	public HashMap getDiscountPer(Document dom,String objContext,String itemSer)throws Exception
	{
		HashMap hm = new HashMap();
		
		String mode = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int childNodeListLength,parentNodeListLength;
		int ctr = 0;
		String division = "",priceList = "",discount = "0";
		String [] divList = null;
		try
		{
			System.out.println("Inside getDiscountPer objContext["+objContext+"] ItemSer["+itemSer+"]");
			
			hm.put("PRICE_LIST", "");
			hm.put("DISCOUNT", "0");
			
			parentNodeList = dom.getElementsByTagName("Detail"+objContext+"");
			parentNodeListLength = parentNodeList.getLength(); 
			
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				division = "";discount = "";priceList = "";
				divList = null;
				
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					
					if(childNode == null || childNode.getNodeType() != childNode.ELEMENT_NODE)
					{
						continue;
					}
					
					if(childNode != null && "division".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						division = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
					}
					else if(childNode != null && "discount".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						discount = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
					}
					else if(childNode != null && "price_list".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						priceList = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
					}
				}
				
				System.out.println("Division ["+division+"] Discount ["+discount+"] PriceList ["+priceList+"]");
				
				if(division!= null && division.length() > 0)
				{
					divList = division.split(",");
					
					for(String dvsn : divList)
					{
						System.out.println("Divsn["+dvsn+"]====["+itemSer+"]");
						if(itemSer.trim().equalsIgnoreCase(dvsn.trim()))
						{
							System.out.println("Match Found-->Division ["+dvsn.trim()+"] Discount ["+discount+"] PriceList ["+priceList+"]");
							
							hm.put("PRICE_LIST", priceList);
							hm.put("DISCOUNT", discount);
							return hm;	
						}
					}
				}
			}	
		}
		catch(Exception e)
		{
			System.out.println("Exception getDiscountPer="+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("Returning from getDiscountPer"+discount);
		return hm;	
	}
	
	public String getPreFormXML(Document dom2,String objContext,String priceRule)throws Exception
	{
		String nodeValue = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int childNodeListLength,parentNodeListLength;
		int ctr = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String domID = "",updateFlag = "",status = "",selected = "",isChanged = "",prntSelected = "";
		try
		{
			System.out.println("Inside getPreFormXML objContext["+objContext+"] priceRule["+priceRule+"]");
			
			parentNodeList = dom2.getElementsByTagName("Detail"+objContext+"");
			parentNodeListLength = parentNodeList.getLength(); 
			
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				
				domID = parentNode.getAttributes().getNamedItem( "domID" ).getNodeValue();
				//prntSelected = parentNode.getAttributes().getNamedItem( "selected" ).getNodeValue();
				
				System.out.println("childNodeListLength"+childNodeListLength);

				//valueXmlString.append("<Detail2 domID = '"+domID+"' objContext='2' selected = 'Y'>");
				
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					nodeValue = "";
					
					if("attribute".equalsIgnoreCase(childNodeName))
					{
						selected = childNode.getAttributes().getNamedItem("selected").getNodeValue();
						updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
						status = childNode.getAttributes().getNamedItem("status").getNodeValue();
						//isChanged = childNode.getAttributes().getNamedItem("IS_CHANGE").getNodeValue();
						
						System.out.println("Inside selected Attribute Selected ["+selected+"]  UpdFlag["+updateFlag+"] Status["+status+"] DOMID["+domID+"]");
						
						if("N".equalsIgnoreCase(selected) && "A".equalsIgnoreCase(updateFlag) && "N".equalsIgnoreCase(status))
						{
							break;
						}
						valueXmlString.append("<Detail2 domID = '"+domID+"' objContext='2' selected = 'Y'>");
						valueXmlString.append("<attribute selected=\""+selected+"\" updateFlag=\""+updateFlag+"\" status=\""+status+"\" pkNames=\"\" />");
					}
					
					if(childNode != null && childNode.getFirstChild() != null)
					{
						nodeValue = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
					}
					
					if("discount".equalsIgnoreCase(childNode.getNodeName()) && "1".equalsIgnoreCase(priceRule))
					{
						valueXmlString.append("<"+childNodeName+" protect = '0'>").append("<![CDATA["+nodeValue+"]]>").append("</"+childNodeName+">");
					}
					else if("discount".equalsIgnoreCase(childNode.getNodeName()) && "0".equalsIgnoreCase(priceRule))
					{
						valueXmlString.append("<"+childNodeName+" protect = '1'>").append("<![CDATA["+nodeValue+"]]>").append("</"+childNodeName+">");
					}
					else if(!"attribute".equalsIgnoreCase(childNodeName))
					{	
						valueXmlString.append("<"+childNodeName+">").append("<![CDATA["+nodeValue+"]]>").append("</"+childNodeName+">");
					}	
				}
				
				if("N".equalsIgnoreCase(selected) && "A".equalsIgnoreCase(updateFlag) && "N".equalsIgnoreCase(status))
				{
					System.out.println("This was dummy Detail");
				}	
				else
				{
					valueXmlString.append("</Detail2>");
				}
			}	
		}
		catch(Exception e)
		{
			System.out.println("Exception getPreFormXML="+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("Returning Detail2 XML from getPreFormXML="+valueXmlString.toString());
		return valueXmlString.toString();	
	}
}
