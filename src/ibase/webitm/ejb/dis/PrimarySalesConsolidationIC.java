package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
@Stateless
public class PrimarySalesConsolidationIC extends ValidatorEJB implements PrimarySalesConsolidationICLocal , PrimarySalesConsolidationICRemote
{
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("In wfValData");
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = "";
		try 
		{
			System.out.println("currFrmXmlStr..." + currFrmXmlStr);
			System.out.println("hdrFrmXmlStr..." + hdrFrmXmlStr);
			System.out.println("allFrmXmlStr..." + allFrmXmlStr);
			if ((currFrmXmlStr != null) && (currFrmXmlStr.trim().length() != 0)) 
			{
				currDom = parseString(currFrmXmlStr);
			}
			if ((hdrFrmXmlStr != null) && (hdrFrmXmlStr.trim().length() != 0)) 
			{
				hdrDom = parseString(hdrFrmXmlStr);
			}
			if ((allFrmXmlStr != null) && (allFrmXmlStr.trim().length() != 0)) 
			{
				allDom = parseString(allFrmXmlStr);
			}
			errString = wfValData(currDom, hdrDom, allDom, objContext, editFlag, xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : [PrimarySalesConsolidationIC][wfValData(String currFrmXmlStr)] : ==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return errString;
	}


	public String wfValData(Document currDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String errString = "" , loginSiteCode = "" , userId ="" , isPrdClosed = "",overWrite="" ;
	String childNodeName = "";
	String sql = "";
	int noOfChilds = 0;
	ResultSet rs = null;
	Connection conn = null;
	PreparedStatement pstmt = null;
	int currentFormNo = 0;
	int cnt = 0,count=0,unConfCnt=0,excnt=0;
	ConnDriver connDriver = null;
	Node childNode = null;
	String itemSer="" , prdCode = "" ,maxPrdCode="" ,countryCode="" ;
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	try {
		System.out.println("************xtraParams*************" + xtraParams);
		connDriver = new ConnDriver();
		//conn = connDriver.getConnectDB("DriverITM");
		conn = getConnection();
		System.out.println("In wfValData PrimarySalesConsolidationIC :::");
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
		System.out.println("**************loginCode************" + userId);
		
		if ((objContext != null) && (objContext.trim().length() > 0)) 
		{
			currentFormNo = Integer.parseInt(objContext);
		}
		NodeList parentList = currDom.getElementsByTagName("Detail"+ currentFormNo);
		NodeList childList = null;
		System.out.println("hdrDom..." + hdrDom.toString());
		switch (currentFormNo)
		{
		case 1:
		{
			childList = parentList.item(0).getChildNodes();
			noOfChilds = childList.getLength();
			for (int ctr = 0; ctr < noOfChilds; ctr++) 
			{
				childNode = childList.item(ctr);
				if (childNode.getNodeType() != 1) 
				{
					continue;
				}
				childNodeName = childNode.getNodeName();
				System.out.println("Editflag =" + editFlag);
				System.out.println("parentList = " + parentList);
				System.out.println("childList = " + childList);
				if ("item_ser".equalsIgnoreCase(childNodeName))
				{
					itemSer = genericUtility.getColumnValue("item_ser", currDom);
					System.out.println("wfValData>>itemSer>>"+itemSer);
					if(itemSer == null || itemSer.trim().length()==0 )
					{
						errString = itmDBAccessEJB.getErrorString("item_ser","VMNULLDIV",userId,"",conn);
						break;
					}
					else 
					{
						sql = "SELECT COUNT(*) AS COUNT FROM ITEMSER WHERE ITEM_SER = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							count = rs.getInt("COUNT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Count: " + count);
						if (count == 0) 
						{
							errString = itmDBAccessEJB.getErrorString("item_ser","VTINVDIV",userId,"",conn);							break;
						}
					}
				}
				else if ("overwrite".equalsIgnoreCase(childNodeName))
				{
					overWrite = genericUtility.getColumnValue("overwrite", currDom);
					System.out.println("wfValData>>overWrite>>"+overWrite);
					if("N".equalsIgnoreCase(overWrite))
					{
						sql = " SELECT COUNT(*) AS COUNT FROM SALES_CONSOLIDATION WHERE PRD_CODE = ? AND ITEM_SER=? AND SOURCE='S' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, prdCode);
						pstmt.setString(2, itemSer);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count = rs.getInt("COUNT");
						}
						if(count > 0)
						{
							errString = itmDBAccessEJB.getErrorString("prd_code","VTDATA",userId,"",conn);
							break ;
						}
					}
					
				}
				else if ("prd_code".equalsIgnoreCase(childNodeName))
				{
					prdCode = genericUtility.getColumnValue("prd_code", currDom);
					itemSer = genericUtility.getColumnValue("item_ser", currDom);
					sql= "select count_code from state where " +
							"state_code in (select state_code from site where site_code=?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSiteCode );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						countryCode = checkNull(rs.getString("count_code")).trim();
						System.out.println("countryCode >>> :"+countryCode);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("wfValData>>prdCode>>"+prdCode+">>itemSer"+itemSer);
					if(prdCode == null || prdCode.trim().length() == 0)
					{
						errString = itmDBAccessEJB.getErrorString("prd_code","VMNULLPRD ",userId,"",conn);
						break ;
					}
					else 
					{
						sql = "  SELECT COUNT(*)  FROM  PERIOD A , PERIOD_TBL B WHERE A.CODE = B.PRD_CODE AND B.PRD_CODE = ? and B.PRD_TBLNO= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,prdCode);
						pstmt.setString(2,countryCode+"_"+itemSer.trim());
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0)
						{
							System.out.println("Error :Period not exist in period_tbl master ");
							errString = itmDBAccessEJB.getErrorString("","VMINVPRDTB",userId,"",conn);
							break ;
						}
						else
						{
							sql = "SELECT B.PRD_CLOSED FROM  PERIOD A , PERIOD_TBL B WHERE A.CODE = B.PRD_CODE AND B.PRD_CODE = ? and B.PRD_TBLNO= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,prdCode.trim());
							pstmt.setString(2,countryCode+"_"+itemSer.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								isPrdClosed = rs.getString("PRD_CLOSED");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if("N".equalsIgnoreCase(isPrdClosed))
							{
								errString = itmDBAccessEJB.getErrorString("","VMPRDNCL",userId,"",conn);
								break ;
							}
							else
							{
								sql="select max(prd_code) from period_tbl where PRD_TBLNO=? and PRD_CLOSED='Y' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,countryCode+"_"+itemSer.trim());
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									maxPrdCode = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(!prdCode.equalsIgnoreCase(maxPrdCode))
								{
									//max period check
									errString = itmDBAccessEJB.getErrorString("","VTINVPCD",userId,"",conn);
									break ;
								}
								else
								{
									sql = " SELECT COUNT(*) AS COUNT FROM SALES_FACT WHERE PRD_CODE = ? and ITEM_SER=? " ;
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, prdCode);
										pstmt.setString(2,itemSer);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											excnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if (excnt == 0)
										{
											System.out.println("No record found ");
											errString = itmDBAccessEJB.getErrorString("","VTNULLRCD",userId,"",conn);
											break ;
										}
										
								}
							}
						}
					}
				}
			}
		}
		break;
		
		}
	}
	catch (Exception e) 
	{
		System.out.println("Exception in "+this.getClass().getSimpleName()+"  == >");
		e.printStackTrace();
		throw new ITMException(e);
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
			if (pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			if ((conn != null) && (!conn.isClosed()))
				conn.close();
		}
		catch (Exception e) 
		{
			System.out.println("Exception :"+this.getClass().getSimpleName()+":wfValData :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
	}
	return errString;
}
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("XmlString :::::::::: "+xmlString);
		System.out.println("XmlString1 :::::::::: "+xmlString1);
		System.out.println("XmlString2 :::::::::: "+xmlString2);
		try 
		{
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				dom = parseString(xmlString);
				System.out.println("Dom ::::::: "+dom);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("Dom1 ::::::: "+dom1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("Dom2 ::::::: "+dom2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [SalesConsolidation] :==>\n"+ e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}
	
	public String itemChanged(Document currDom, Document hdrDom, Document allDom,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException
	{
		E12GenericUtility genericUtility= new  E12GenericUtility();
		int currentFormNo = 0;
		StringBuffer valueXmlString = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null,  columnValue = "",maxPrdCode="",itemSer="",sql="",countryCode="",loginSiteCode="";
		int ctr = 0, childNodeListLength = 0 ; 
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ConnDriver connDriver = null;
		try 
		{
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			currentColumn = checkNull(currentColumn);
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
			switch (currentFormNo) 
			{
			case 1:
			{
				System.out.println("Inside Case 1 Of Itemchange");
				parentNodeList = currDom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				/*do 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr++;
				}while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));*/
				System.out.println(" currentColumn : "+ currentColumn);
				System.out.println("current form::::::::::::" + currentFormNo);
				if ( "itm_default".equalsIgnoreCase(currentColumn)) 
				{
					valueXmlString.append("<prd_code>").append("").append("</prd_code>");
					valueXmlString.append("<item_ser>").append("").append("</item_ser>");
					valueXmlString.append("<overwrite>").append("<![CDATA[N]]>").append("</overwrite>");
				}
				else if ("item_ser".equalsIgnoreCase(currentColumn))
				{
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
					System.out.println("itemSer>>>"+itemSer);
					if(itemSer.trim().length()>0){
					sql= "select count_code from state where " +
							"state_code in (select state_code from site where site_code=?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSiteCode );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						countryCode = checkNull(rs.getString("count_code")).trim();
						System.out.println("countryCode >>> :"+countryCode);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql="select max(prd_code) from period_tbl where PRD_TBLNO=? and PRD_CLOSED='Y' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,countryCode+"_"+itemSer.trim());
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						maxPrdCode = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(maxPrdCode.length()>0)
					{
						valueXmlString.append("<prd_code>").append("<![CDATA[" + maxPrdCode + "]]>").append("</prd_code>");
					}
					else
					{
						valueXmlString.append("<prd_code>").append("<![CDATA[]]>").append("</prd_code>");
					}
					}
					else
					{
						valueXmlString.append("<prd_code>").append("<![CDATA[]]>").append("</prd_code>");
					}
				}
				valueXmlString.append("</Detail1>\r\n");
			}
			break;
			}
		}
		catch (Exception e) 
		{
			try 
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			catch (Exception ex) 
			{
				ex.printStackTrace();
				throw new ITMException(ex);
			}
		}
		finally 
		{
			try {
		
				if (conn != null) {
					conn.close();
					conn = null;
				}
				if(rs != null )
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
		
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		valueXmlString.append("</Root>\r\n");
		return valueXmlString.toString();
	}
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
}
