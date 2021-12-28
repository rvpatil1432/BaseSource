package ibase.webitm.ejb.dis;


import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;

@Stateless
public class SecSalesSubIC extends ValidatorEJB  implements SecSalesSubICLocal ,SecSalesSubICRemote {

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
			System.out.println("Exception : [SecSalesSubIC][wfValData(String currFrmXmlStr)] : ==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return errString;
	}

	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
	public String wfValData(Document currDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int count = 0;
		String errString = "" , loginSiteCode = "" , userId ="" , countryCode = "" , isPrdClosed = "" ;
		String errorType = "";
		String errCode = "";
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String childNodeName = "";
		String sql = "";
		int noOfChilds = 0;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		int currentFormNo = 0;
		int cnt = 0;
		ConnDriver connDriver = null;
		Node childNode = null;
		String itemSer="" , prdCode = "" ;
		try {
			System.out.println("************xtraParams*************" + xtraParams);
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			System.out.println("In wfValData Secondary Sales Submit:::");
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
					
					if ("prd_code".equalsIgnoreCase(childNodeName))
					{
						prdCode = checkNull(genericUtility.getColumnValue("prd_code", currDom));
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
						System.out.println("Period Code :::::::: "+prdCode);
						if(prdCode==null || prdCode.length()==0)
						{
							errList.add("VMNULLPRD"); 
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						if(itemSer==null || itemSer.trim().length()==0)
						{
							errList.add("VMNULLSER");//Invalid-Division can not be blank 
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else if(itemSer != null || itemSer.trim().length()>0)
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
								errList.add("VTINVDIV");//Invalid-Division Does not exist
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
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
							
							sql = "select count(*) from period_appl a,period_tbl b " +
									"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
									" AND b.prd_code = ? " +
									"and b.prd_tblno=? " +
									"AND case when a.type is null then 'X' else a.type end='S' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,prdCode.trim());
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
								errCode = "VMINVPRDTB";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
										",b.entry_start_dt as entry_start_dt" +
										",b.entry_end_dt as entry_end_dt ,b.prd_closed" +
										" from period_appl a,period_tbl b " +
										"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
										" AND b.prd_code = ? " +
										"and b.prd_tblno=? " +
										"AND case when a.type is null then 'X' else a.type end='S' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,prdCode.trim());
								pstmt.setString(2,countryCode+"_"+itemSer.trim());	
								rs = pstmt.executeQuery();
								if(rs.next())
								{
								isPrdClosed = rs.getString("prd_closed");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if("Y".equalsIgnoreCase(isPrdClosed))
								{
									errCode = "VMPRDCLOSE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						/*else
						{
							errList.add("VMNULLSER");//Invalid-Division can not be blank 
							errFields.add(childNodeName.toLowerCase());
							break;
						}*/
						
					}
					/* if ("item_ser".equalsIgnoreCase(childNodeName) ) 
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
						System.out.println("Item Seris ::::::::: " +itemSer);
						if(itemSer==null || itemSer.trim().length()==0)
						{
							errList.add("VMNULLSER");//Invalid-Division can not be blank 
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
									errList.add("VTITMERR");//Invalid-Division Does not exist
									errFields.add(childNodeName.toLowerCase());
									break;
								}
						}
					}*/
					
				}
			}
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
						System.out.println("errStringXml .........." + errStringXml);
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
			while (rs.next())
				msgType = rs.getString("MSG_TYPE");
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
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
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
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
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		return msgType;
	}
}
