package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;
import javax.ejb.*;
import java.util.HashMap;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class PurcRetnUpdateAllocPos extends ValidatorEJB implements PurcRetnUpdateAllocPosLocal,PurcRetnUpdateAllocPosRemote   //SessionBean
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("ejbCreate() method calling........");
	}
	public void ejbRemove()
	{
		System.out.println("ejbRemeove() method calling........");
	}
	public void ejbActivate() 
	{
		System.out.println("ejbActivate() method calling........");
	}
	public void ejbPassivate() 
	{
		System.out.println("ejbPassivate() method calling........");
	}*/
	public String postSaveRec()throws RemoteException,ITMException
	{
		return "";
	}
	
	public String postSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		System.out.println("PurcRetnUpdateAllocPosEJB called");
		//System.out.println("xmlString1 called"+xmlString1);
		System.out.println("xmlString1 called"+xmlString1);
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);
				//executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = genericUtility.parseString(xmlString1);
				executepostSaveRec(dom,dom1,objContext,editFlag,xtraParams,conn);
			}	
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :PurcRetnUpdateAllocPosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	//private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	private String executepostSaveRec(Document dom, Document dom1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{

		HashMap hashMap = new HashMap(); 
		String lineNo = "", tranId = "", tranSer = "P-RET", line = "", updateStatus = "";
		String siteCode = "", itemCode = "", quantity = "", locCode = "", lotNo = "", lotSl = "";
		String errInvAllocTrace = ""; // add on 09/10/09
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String stlOption = "";		
		NodeList hdrDom = null;
		Node currDetail = null;
		try
		{		
			
			InvAllocTraceBean  invAllocTraceBean = null;	
			// changes on 24/11/09

			/*
			hdrDom = dom.getElementsByTagName("Detail1");
			siteCode = GenericUtility.getInstance().getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = GenericUtility.getInstance().getColumnValueFromNode("tran_id",hdrDom.item(0));
			*/
			hdrDom = dom1.getElementsByTagName("Detail1");
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			// end of changes 
			System.out.println("tran_id : " + tranId);

			//currDetail = getCurrentDetailFromDom(dom,domID);
			currDetail = dom.getElementsByTagName("Detail2").item(0);
			updateStatus = getCurrentUpdateFlag(currDetail);
			System.out.println("updateStatus[[[["+updateStatus);

			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				// 31/12/11 manoharan quantity__stduom should be updated
				//quantity = GenericUtility.getInstance().getColumnValueFromNode("quantity",currDetail);
				quantity = genericUtility.getColumnValueFromNode("quantity__stduom",currDetail);
				// end 31/12/11 manoharan quantity__stduom should be updated
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCodeFr :"+locCode+" lotNoFr :"+lotNo+" lotSlFr :"+lotSl+" quantity :"+quantity);
				//String stlOption = chkStockOption(itemCode, siteCode);
				// add code on 26/11/09
				sql = "SELECT CASE WHEN STK_OPT IS NULL THEN 'N' ELSE STK_OPT END "
				 +"FROM SITEITEM "
				 +"WHERE ITEM_CODE = ? "
				 +"AND SITE_CODE = ? ";
				 pstmt = conn.prepareStatement(sql);
				 pstmt.setString(1,itemCode);
				 pstmt.setString(2,siteCode);			
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					stlOption = rs.getString(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if (stlOption == null || stlOption.trim().length() == 0 || stlOption.equals("N"))
				{
					sql = "SELECT CASE WHEN STK_OPT IS NULL THEN '0' ELSE STK_OPT END "
						 +"FROM ITEM "
						 +"WHERE ITEM_CODE = ? ";
						 pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);				
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							stlOption = rs.getString(1);
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;
				}//end if
				// end of code 
				System.out.println("stlOption  :"+stlOption);
				if (!stlOption.equals("0"))
				{
					if (quantity == null)
					{
						quantity = "0.000";
					}
					hashMap.put("tran_date", new java.sql.Date(System.currentTimeMillis()));
					hashMap.put("ref_ser",tranSer);
					hashMap.put("ref_id", tranId);
					hashMap.put("ref_line", lineNo);
					hashMap.put("item_code", itemCode);
					hashMap.put("site_code", siteCode);
					hashMap.put("loc_code",locCode);
					hashMap.put("lot_no",lotNo);
					hashMap.put("lot_sl",lotSl);
					hashMap.put("alloc_qty", new Double(quantity.trim().length() == 0?"0":quantity));
					hashMap.put("chg_win","W_PRETURN");
					hashMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
					hashMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
					//Calling DistStkUpdEJB
					//connent on 09/10/09					
					/*
						
					System.out.println("Calling DistStkUpdEJB.....");				
					if (distStkUpd.updAllocTrace(hashMap, conn) > 0)
					{
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}					
					*/
					invAllocTraceBean =  new InvAllocTraceBean();
					errInvAllocTrace = invAllocTraceBean.updateInvallocTrace(hashMap, conn);
					if(errInvAllocTrace.trim().length()>0)
					{
						System.out.println("invAllocTraceBeanupdateInvallocTrace(hashMap, conn) : Sucessuful");
					}
					

					// end of code 
				}//end if
			}//end if
		}//end try
		catch(Exception e)
		{
			System.out.println("Exception : PurcRetnUpdateAllocPosEJB : actionHandler :" +e.getMessage());
			throw new ITMException(e);
		}
		return "";
	}

	private String chkStockOption(String itemCode, String siteCode)throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String sql = "", stkOption = "";
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			sql = "SELECT CASE WHEN STK_OPT IS NULL THEN 'N' ELSE STK_OPT END "
				 +"FROM SITEITEM "
				 +"WHERE ITEM_CODE = ? "
				 +"AND SITE_CODE = ? ";
				 pstmt = conn.prepareStatement(sql);
				 pstmt.setString(1,itemCode);
				 pstmt.setString(2,siteCode);			
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				stkOption = rs.getString(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if (stkOption == null || stkOption.equals("N"))
			{
				sql = "SELECT CASE WHEN STK_OPT IS NULL THEN '0' ELSE STK_OPT END "
					 +"FROM ITEM "
					 +"WHERE ITEM_CODE = ? ";
					 pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);				
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						stkOption = rs.getString(1);
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
			}//end if			
		}//end try
		catch(SQLException e)
		{
			
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				
			}
		}
		return stkOption;
	}
	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;
	
		detailList = dom.getElementsByTagName("Detail2");
		detailListLength = detailList.getLength();
		System.out.println("detailListLength[[["+detailListLength);
		System.out.println("domId[[["+domId);
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