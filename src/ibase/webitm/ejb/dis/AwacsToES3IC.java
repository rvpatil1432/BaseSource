package ibase.webitm.ejb.dis;
/**
 * @author Saurabh Jarande[19/07/17]
 * This component is used for validating AWACS process.
 *
 */
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
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

@Stateless
public class AwacsToES3IC extends ValidatorEJB implements AwacsToES3ICRemote,AwacsToES3ICLocal {
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String editFlag,String xtraParams) throws RemoteException 
	{
		System.out.println("In wfValData");
		Document currDom = null, hdrDom = null, allDom = null;
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
			System.out.println("Exception :"+this.getClass().getSimpleName()+"[wfValData] : ==>\n" + e.getMessage());
		}
		return errString;
	}

	public String validate(Document currDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		System.out.println("In validate Data");
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String>  errFields = new ArrayList<String>();
		int noOfChilds = 0, currentFormNo = 0, cnt = 0, prdCnt=0, invDivisionCnt=0 , custCnt=0 , genCnt=0 , awcsCnt=0 , orgCustCnt=0 , orgCustCnt1 =0;
		String errString = "",errorType = "",errCode = "", childNodeName = "", custCode="",prdCode="",sql="",prdTblNo="";
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ConnDriver connDriver = null;
		Node childNode = null;
		ArrayList <String> opPrdList = new ArrayList<String>();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try 
		{
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			System.out.println("************xtraParams*************" + xtraParams);
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("**************loginCode************" + userId);
			if (objContext != null && objContext.trim().length() > 0) 
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
					if ("prd_code".equalsIgnoreCase(childNodeName) ) 
					{
						prdCode = checkNull(genericUtility.getColumnValue("prd_code", currDom));
						if(prdCode==null || prdCode.trim().length()==0)
						{
							errList.add("VMNULLPRD");//Invalid-Period can not be blank 
							errFields.add(childNodeName.toLowerCase());
							//break;
						}
						else
						{
							sql=" SELECT COUNT(*) AS COUNT FROM PERIOD WHERE CODE=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,prdCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								prdCnt = rs.getInt(1);
							}
							callPstRs(pstmt, rs);
							if(prdCnt == 0)
							{
								errList.add("VMINVPRD");//INVALID PRD CODE 
								errFields.add(childNodeName.toLowerCase());
								//break;
							}
							else
							{
								sql=" SELECT SUBSTR(PRD_TBLNO,5,10) AS ITEM_SER FROM PERIOD_TBL WHERE PRD_CODE=? AND PRD_CLOSED='Y' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,prdCode);
								rs = pstmt.executeQuery();
								while (rs.next())
								{
									prdTblNo = rs.getString(1);
									opPrdList.add(prdTblNo);
									invDivisionCnt++;
								}
								callPstRs(pstmt, rs);
								if(invDivisionCnt > 0)
								{
									errList.add("VMINVPRDCL");//Closed division 
									errFields.add(childNodeName.toLowerCase());
									//break;
								}
							}
						}
					}
					if ("cust_code".equalsIgnoreCase(childNodeName) ) 
					{
						custCode = checkNull(genericUtility.getColumnValue("cust_code", currDom));
						prdCode = checkNull(genericUtility.getColumnValue("prd_code", currDom));
						if(custCode==null || custCode.trim().length()==0)
						{
							errList.add("VPBLKCUSCD");//Invalid-Customer code can not be blank 
							errFields.add(childNodeName.toLowerCase());
							//break;
						}
						else
						{
							sql=" SELECT COUNT(*) AS COUNT FROM CUSTOMER WHERE CUST_CODE=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								custCnt = rs.getInt(1);
							}
							callPstRs(pstmt, rs);
							if(custCnt == 0)
							{
								//INVALID CUSTOMER
								errList.add("VPINVCSCDM"); 
								errFields.add(childNodeName.toLowerCase());
								//break;
							}
							else
							{
								sql=" SELECT COUNT(*) FROM ORG_STRUCTURE_CUST WHERE CUST_CODE=? AND VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									orgCustCnt = rs.getInt(1);
								}
								callPstRs(pstmt, rs);
								if(orgCustCnt==0)
								{
									// does not exist
									errList.add("VPINVCSOG"); 
									errFields.add(childNodeName.toLowerCase());
									//break;
								}
								else
								{
									sql=" SELECT COUNT(*) FROM ORG_STRUCTURE_CUST WHERE CUST_CODE=? AND VERSION_ID = (SELECT FN_GET_VERSION_ID FROM DUAL) and source = 'A' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,custCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										orgCustCnt1 = rs.getInt(1);
									}
									callPstRs(pstmt, rs);
									if(orgCustCnt1 == 0 )
									{
										//not awacs cust
										errList.add("VPINVCSOGA"); 
										errFields.add(childNodeName.toLowerCase());
										//break;
									}
									else
									{
										sql=" SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE=? AND PRD_CODE=? AND POS_CODE IS NULL AND CONFIRMED = 'Y' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,custCode);
										pstmt.setString(2,prdCode);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											awcsCnt = rs.getInt(1);
										}
										callPstRs(pstmt, rs);
										if(awcsCnt == 0)
										{
											//transactions already created
											errList.add("VTNULLRCD"); 
											errFields.add(childNodeName.toLowerCase());
											//break;
										}
										else
										{
											sql=" SELECT COUNT(*) FROM CUST_STOCK WHERE CUST_CODE=? AND PRD_CODE=? AND POS_CODE IS NOT NULL ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,custCode);
											pstmt.setString(2,prdCode);
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												genCnt = rs.getInt(1);
											}
											callPstRs(pstmt, rs);
											if(genCnt > 0)
											{
												//transactions already created
												errList.add("VPINVCSCDA"); 
												errFields.add(childNodeName.toLowerCase());
												//break;
											}
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
					if(opPrdList.size()>0 && errString.length() > 0 )
					{
						 String begPart = errString.substring( 0, errString.indexOf("]]></description>") );
						  String mainStr="";
						    for(int i=0;i<opPrdList.size();i++)
						    {
						    	mainStr=mainStr+ opPrdList.get(i)+",";
						    }
						    
						    String endPart=errString.substring( errString.indexOf("]]></description>"), errString.length() );
						    mainStr=" Following Divisions are closed :: "+mainStr.substring(0,mainStr.length()-1);
						    errString = begPart+mainStr +  endPart;
					}
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
			System.out.println("Exception in "+this.getClass().getSimpleName()+"  == >"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			try 
			{
				callPstRs(pstmt, rs);
				if (conn != null && !conn.isClosed())
				{
					conn.close();
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

	private String checkNull(String inputVal)
	{
		try{
		inputVal = inputVal==null? "" : inputVal.trim();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return inputVal;
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 02/08/19
		}
		return msgType;
	}
	
}
