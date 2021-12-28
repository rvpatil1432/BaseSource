package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import javax.ejb.*;
import javax.naming.InitialContext;

import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.InvAllocTraceBean;
import ibase.system.config.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import javax.ejb.Stateless; // added for ejb3


//public class DistIssuePosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class DistIssuePos extends ValidatorEJB implements DistIssuePosLocal, DistIssuePosRemote
{
	String objName="",detailNode="";
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
	}

	public void ejbRemove()
	{
	}

	public void ejbActivate() 
	{
	}

	public void ejbPassivate() 
	{
	}*/

    public String postSaveRec()throws RemoteException,ITMException
	{
		return "";
	}
	
	/* public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("DistIssuePosEJB called");
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = GenericUtility.getInstance().parseString(xmlString1);
				executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :DistIssuePosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	} */
	//public String postSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException

	{
		Document dom = null,dom1 = null;
		System.out.println("DistIssuePosEJB called xmlString : [" + xmlString1 + "] ");
		
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				if( (xmlString1.toUpperCase()).contains("DISTISSWIZ") )
				{
					objName="distisswiz";
					detailNode="detail3";
					if( (xmlString1.toUpperCase()).contains("DETAIL3") )
					{						
						executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
						
					}
				}
				
				else
				{
					objName="";
					detailNode="detail2";
					executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				
			}	
		
			/*if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = GenericUtility.getInstance().parseString(xmlString);
				//executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = GenericUtility.getInstance().parseString(xmlString1);		
				
				if( (xmlString1.toUpperCase()).contains("DISTISSWIZ") )
				{
					objName="distisswiz";
					detailNode="detail3";
					if( (xmlString1.toUpperCase()).contains("DETAIL3") )
					{						
						executepostSaveRec(dom,dom1,objContext,editFlag,xtraParams,conn);
						
					}
				}
				
				else
				{
					objName="";
					detailNode="detail2";
					executepostSaveRec(dom,dom1,objContext,editFlag,xtraParams,conn);
				}
			}	*/		
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :DistIssuePosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	//private String executepostSaveRec(Document dom, Document dom1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null, stmt1 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		PreparedStatement pstmt = null;
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", line = "", stockOpt = "", sqlItem = "";
		HashMap strAllocate = new HashMap();
		double quantity = 0d;
		int retVal = 0;
		java.sql.Date tranDate = null;		
		Node currDetail = null;
		NodeList hdrDom = null;
		String errString = "";
		String updateStatus = "",quantityStr = "",tranId = "";
		try
		{
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();			
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("\n tranDate :"+tranDate);
			//commented by Dadso Pawar on 01/10/14
			//hdrDom = dom1.getElementsByTagName("Detail1");
			/*if("distisswiz".equalsIgnoreCase(objName))
			{
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			else
			{
				hdrDom = dom.getElementsByTagName("Detail1");
			}*/
			if("distisswiz".equalsIgnoreCase(objName))
			{
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			else
			{
				hdrDom = dom.getElementsByTagName("Detail1");
			}
			
			
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			//currDetail = getCurrentDetailFromDom(dom,domID);
			//currDetail = dom.getElementsByTagName("Detail2").item(0);
			/*if("distisswiz".equalsIgnoreCase(objName))
			{
				currDetail = dom.getElementsByTagName("Detail3").item(0);
			}
			else
			{
				currDetail = dom.getElementsByTagName("Detail2").item(0);
			}
			updateStatus = getCurrentUpdateFlag(currDetail);
			*/
			currDetail  = getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);
			
			System.out.println("Site Code :: "+siteCode);
			System.out.println("Tran ID :: "+tranId);
			if (tranId == null || tranId.trim().length() == 0)
			{
				tranId  = genericUtility.getColumnValueFromNode("tran_id",currDetail);
			}
			System.out.println("Tran ID from currDetail  ["+tranId + "]");
			System.out.println("DomID :: "+domID+"\n Update Status ::"+updateStatus);

			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				quantityStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
				quantity = Double.parseDouble(quantityStr);
				line = "   " + lineNo;
				line = line.substring(line.length()-3);				
				
				if (lotNo == null || lotNo.trim().length() == 0)
				{
					lotNo = "               ";
				}
				if (lotSl == null || lotSl.trim().length() == 0)
				{
					lotSl = "     ";
				}

                //Changed by Anagha R on 12/01/2021 for Error while confirmation of Dist Issue Transaction     
                //sqlItem = "SELECT STK_OPT FROM SITEITEM WHERE ITEM_CODE = '"+itemCode+"' "
                //	 +"AND SITE_CODE = '"+siteCode+"'";
                //System.out.println("The select sql sqlItem:"+sqlItem);
				//pstmt = conn.prepareStatement( sqlItem );
				//rs1 = pstmt.executeQuery();
				//if (rs1.next())
				//{
				//	stockOpt = rs1.getString(1);
				//	System.out.println("stockOpt :"+stockOpt);
				//}
				//rs1.close();
				//rs1 = null;
				//pstmt.close();
				//pstmt = null;
				//if (stockOpt == null)
				//{
                //Commented by Anagha R on 12/01/2021 for Error while confirmation of Dist Issue Transaction END        
					sqlItem = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
					System.out.println("The select sqlItem 654:"+sqlItem);
					pstmt = conn.prepareStatement( sqlItem );
					rs2 = pstmt.executeQuery();
					if (rs2.next())
					{
						stockOpt = rs2.getString(1);
						System.out.println("stockOpt :"+stockOpt);
					}
					rs2.close();
					rs2 = null;
					pstmt.close();
					pstmt = null;
				//}
				if (!stockOpt.equalsIgnoreCase("0"))
				{
					strAllocate.put("tran_date",tranDate);
					//strAllocate.put("ref_ser","D-ISS");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));					
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(quantity)); 
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					//strAllocate.put("chg_win","W_DIST_ISSUE");
					
					if("distisswiz".equalsIgnoreCase(objName))
					{
						strAllocate.put("chg_win","W_DISTISSWIZ");
						//strAllocate.put("ref_ser","WI-ISS");
						strAllocate.put("ref_ser","D-ISS");	// SANDESH
					}
					else
					{
						strAllocate.put("chg_win","W_DIST_ISSUE");
						strAllocate.put("ref_ser","D-ISS");
					}
					
					System.out.println("Calling DistStkUpdEJB.....");				
					//if (distStkUpd.updAllocTrace(strAllocate) > 0)
					/*Changed by HATIM on 13/01/2006*/
					InvAllocTraceBean invAllocTrace = new InvAllocTraceBean();
					
					/*if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
					{
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}*/
					
					errString = invAllocTrace.updateInvallocTrace(strAllocate, conn);
					System.out.println("errString ::: " + errString);

					if (errString == "")
					{
						System.out.println("invAllocTrace.updateInvallocTrace : Sucessuful!");
					}
					invAllocTrace = null;
					/*END*/
				}
			}//while end
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in DistIssuePostSaveEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in DistIssuePostSaveEJB  :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if( rs1 != null )
				{
					rs1.close();
					rs1 = null;
				}
				if( rs2 != null )
				{
					rs2.close();
					rs2 = null;
				}
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if(stmt1 != null)
				{
					stmt1.close();
					stmt1 = null;
				}			
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}							
			}
			catch(Exception e)
			{
				System.err.println("Exception :DistIssuePosEJB ::\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return "";
	}

	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;

		//-------- Added by Sandesh 02-Oct-2014  -------------
		
		if("distisswiz".equalsIgnoreCase(objName))
		{
			detailList = dom.getElementsByTagName("Detail3");
		}
		else
		{
			detailList = dom.getElementsByTagName("Detail2");
		}		
		
		// -------------------------------------------------
		
		//detailList = dom.getElementsByTagName("Detail2");		// Commented by Sandesh 02-Oct-2014
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
			currDomId = currDetail.getAttributes().getNamedItem("domID").getNodeValue();
			if (currDomId.equals(domId))
			{
				reqDetail = currDetail;
				break;
			}			
		}
		return reqDetail;
	}

	private String getCurrentUpdateFlag(Node currDetail)
	{
		NodeList currDetailList = null;
		String updateStatus = "",nodeName = "";
		int currDetailListLength = 0;

		currDetailList = currDetail.getChildNodes();
		currDetailListLength = currDetailList.getLength();
		for (int i=0;i< currDetailListLength;i++)
		{
			nodeName = currDetailList.item(i).getNodeName();
			if (nodeName.equalsIgnoreCase("Attribute"))
			{
				updateStatus = currDetailList.item(i).getAttributes().getNamedItem("updateFlag").getNodeValue();
				break;
			}
		}
		return updateStatus;		
	}	
}