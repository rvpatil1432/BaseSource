/**
 * @author Saurabh Jarande[24/03/17]
 * This component is used to create Secondary sales transactions by process after Period closed.
 *
 */
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class SecSalesGenIc extends ValidatorEJB implements SecSalesGenIcRemote,SecSalesGenIcLocal 
{
	E12GenericUtility genericUtility =new E12GenericUtility();
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
			errString = validate(currDom, hdrDom, allDom, objContext, editFlag, xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
	}

	public String validate(Document currDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		System.out.println("In validate Data");
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		int noOfChilds = 0,excnt=0,currentFormNo = 0,cnt = 0, count = 0;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ConnDriver connDriver = null;
		Node childNode = null;
		Timestamp frDate=null,toDate=null;
		String errString = "", errorType = "", errCode = "",maxPrdCode="", itemSer="",prdCode="",
				loginSiteCode = "",countryCode="",isPrdClosed="",childNodeName = "", sql = "";
		try {
			System.out.println("************xtraParams*************" + xtraParams);
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
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
							errList.add("VMNULLDIV"); 
							errFields.add(childNodeName.toLowerCase());
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
								errList.add("VTINVDIV"); 
								errFields.add(childNodeName.toLowerCase());
								break;
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
							errList.add("VMNULLPRD"); 
							errFields.add(childNodeName.toLowerCase());
							break;
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
								errList.add("VMINVPRDTB"); 
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							else
							{
								sql = " SELECT B.PRD_CLOSED,B.FR_DATE,B.TO_DATE FROM  PERIOD A , PERIOD_TBL B " +
									  " WHERE A.CODE = B.PRD_CODE AND B.PRD_CODE = ? and B.PRD_TBLNO= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,prdCode.trim());
								pstmt.setString(2,countryCode+"_"+itemSer.trim());
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									isPrdClosed = rs.getString("PRD_CLOSED");
									frDate=rs.getTimestamp("FR_DATE");
									toDate=rs.getTimestamp("TO_DATE");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if("N".equalsIgnoreCase(isPrdClosed))
								{
									errList.add("VMPRDNCL"); 
									errFields.add(childNodeName.toLowerCase());
									break;
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
										errList.add("VTINVPCD"); 
										errFields.add(childNodeName.toLowerCase());
										break;
									}
									else
									{
										sql = " SELECT COUNT(*) AS COUNT FROM ( " +
											  " SELECT C.CUST_CODE FROM ORG_STRUCTURE A ,ORG_STRUCTURE_CUST C " +
											  " WHERE A.VERSION_ID=C.VERSION_ID AND A.TABLE_NO=C.TABLE_NO AND A.POS_CODE=C.POS_CODE " +
											  " AND C.VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) AND C.TABLE_NO= ? " +
											  " AND C.EFF_DATE <= ?  AND C.VALID_UPTO >= ? AND CASE WHEN C.SOURCE IS NULL THEN 'Y' ELSE C.SOURCE END <> 'A' " +
											  " MINUS " +
											  " SELECT CUST_CODE FROM CUST_STOCK WHERE PRD_CODE = ? AND POS_CODE IS NOT NULL AND ITEM_SER = ? ) " ;
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, itemSer);
											pstmt.setTimestamp(2, frDate);
											pstmt.setTimestamp(3, toDate);
											pstmt.setString(4, prdCode);
											pstmt.setString(5, itemSer);
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
												errList.add("VTNULLRCD"); 
												errFields.add(childNodeName.toLowerCase());
												break;

											}
									}
								}
							}
						}
					}
				}//for
			}//switch
			break;
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = "";
			if ((errList != null) && (errListSize > 0)) 
			{
				for (cnt = 0; cnt < errListSize; cnt++) 
				{
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("cc" + errStringXml);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) 
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
				errStringXml = new StringBuffer("");
			}
			errString = errStringXml.toString();
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
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch (Exception e) 
			{
				System.out.println("Exception :"+this.getClass().getSimpleName()+":wfValData :==>\n" + e.getMessage());
				throw new ITMException(e);
			}
		}
		return errString;
	}
	
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = null;
		try 
		{
			if ((currFrmXmlStr != null) && (currFrmXmlStr.trim().length() != 0)) 
			{
				currDom = genericUtility.parseString(currFrmXmlStr);
				System.out.println("currFrmXmlStr : " + currFrmXmlStr);
			}
			if ((hdrFrmXmlStr != null) && (hdrFrmXmlStr.trim().length() != 0)) 
			{
				hdrDom = genericUtility.parseString(hdrFrmXmlStr);
				System.out.println("hdrFrmXmlStr : " + hdrFrmXmlStr);
			}
			if ((allFrmXmlStr != null) && (allFrmXmlStr.trim().length() != 0)) 
			{
				allDom = genericUtility.parseString(allFrmXmlStr);
				System.out.println("allFrmXmlStr : " + allFrmXmlStr);
			}
			errString = itemChanged(currDom, hdrDom, allDom, objContext,currentColumn, editFlag, xtraParams);
			System.out.println("ErrString :" + errString);
		}
		catch (Exception e) 
		{
			System.out.println("Exception :"+this.getClass().getSimpleName()+":itemChanged :==>\n" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		return errString;
	}

	public String itemChanged(Document currDom, Document hdrDom,Document allDom, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		int currentFormNo = 0;
		String childNodeName = null;
		int ctr = 0;
		int childNodeListLength = 0;
		Connection conn = null;
		StringBuffer valueXmlString = new StringBuffer();
		String itemSer="",sql="",countryCode="",maxPrdCode="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		try 
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();

			if ((objContext != null) && (objContext.trim().length() > 0)) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");

			System.out.println("currentFormNo-------*************** = "+ currentFormNo);
			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = currDom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr++;
				}while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));

				System.out.println(" currentColumn : "+ currentColumn);

				if ( "itm_default".equalsIgnoreCase(currentColumn)) 
				{
					valueXmlString.append("<prd_code>").append("").append("</prd_code>");
					valueXmlString.append("<item_ser>").append("").append("</item_ser>");
				}
				else if ("item_ser".equalsIgnoreCase(currentColumn))
				{
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
					System.out.println("itemSer>>>"+itemSer);
					if(itemSer.trim().length()>0)
					{
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
						callPstRs(pstmt, rs);
						
						sql="select max(prd_code) from period_tbl where PRD_TBLNO=? and PRD_CLOSED='Y' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,countryCode+"_"+itemSer.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							maxPrdCode = checkNull(rs.getString(1));
						}
						callPstRs(pstmt, rs);
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
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception :"+this.getClass().getSimpleName()+":itemChanged :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		finally 
		{
				try 
				{
					if (conn != null)
					{
						conn.close();
						conn = null;
					}
					if (pstmt != null) 
					{
						pstmt.close();
						pstmt = null;
					}
					if (rs != null) 
					{
						rs.close();
						rs = null;
					}
				}
				catch (SQLException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ITMException(e);
				}
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("\n****ValueXmlString :" + valueXmlString.toString()+ ":********");
		return valueXmlString.toString();
	}
		
	public void callPstRs(PreparedStatement pstmt, ResultSet rs) 
	{
		try 
		{
			if (pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) 
			{
				rs.close();
				rs = null;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
	
	private String errorType(Connection conn, String errorCode) throws ITMException 
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()){
				msgType = rs.getString("MSG_TYPE");
			}
			callPstRs(pstmt, rs);
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		}
		return msgType;
	}
}
