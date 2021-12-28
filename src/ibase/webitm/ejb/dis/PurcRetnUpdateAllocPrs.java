package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class PurcRetnUpdateAllocPrs extends ValidatorEJB implements PurcRetnUpdateAllocPrsLocal,PurcRetnUpdateAllocPrsRemote ///SessionBean
{
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
	}
	*/
    public String preSaveRec() throws RemoteException,ITMException
	{
		return "";
	}
	
	//public String preSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	public String preSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		//System.out.println("PurcRetnUpdateAllocPrsEJB called xmlString [" + xmlString + "] xmlString1 [" + xmlString1 + "]");
		Document dom = null, dom1 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);
				//executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = genericUtility.parseString(xmlString1);			
				//executepreSaveRec(dom,xmlString1,objContext,editFlag,xtraParams,conn);
				executepreSaveRec(dom,dom1,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	//private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	//private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	private String executepreSaveRec(Document dom, Document dom1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null,stmt1 = null;
		PreparedStatement  pstmt = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", line = "", tranType = "", line1 = "", stockOpt = "", sqlItem = "";
		String errInvAllocTrace = ""; // add on 09/10/09
		HashMap strAllocate = new HashMap();
		double quantity = 0d;
		int retVal = 0;
		java.sql.Date tranDate = null;
		//DistStkUpdLocal distStkUpd = null; // comment on 09/10/09
		String updateStatus = "",quantityStr = "",tranId = ""; 
		NodeList hdrDom = null;
		Node currDetail = null;
		//String stockOpt = "";
		try
		{		
					
			//DistStkUpdLocal distStkUpd = null; // comment on 09/10/09
			InvAllocTraceBean  invAllocTraceBean = null;		
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("\n tranDate :"+tranDate);
			// changes on 24/11/09
			/*hdrDom = dom.getElementsByTagName("Detail1");			
			siteCode = GenericUtility.getInstance().getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = GenericUtility.getInstance().getColumnValueFromNode("tran_id",hdrDom.item(0));
			*/
			hdrDom = dom1.getElementsByTagName("Detail1");
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			// end of code 

				
			System.out.println("siteCode :"+siteCode);
			System.out.println("\n tranId :"+tranId);

			//currDetail = getCurrentDetailFromDom(dom,domID);
			currDetail = dom.getElementsByTagName("Detail2").item(0);
			updateStatus = getCurrentUpdateFlag(currDetail);
			System.out.println("updateStatus updateStatus[[[[:"+updateStatus);
			if (currDetail != null && !updateStatus.equalsIgnoreCase("A")&& !updateStatus.equalsIgnoreCase("N"))//&& !updateStatus.equalsIgnoreCase("N") CONDITION ADDED BY NANDKUMAR GADKARI ON 14/01/18
			{
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				// 31/12/11 manoharan quantity__stduom should be updated 
				//quantityStr = GenericUtility.getInstance().getColumnValueFromNode("quantity",currDetail);
				quantityStr = genericUtility.getColumnValueFromNode("quantity__stduom",currDetail);
				// end 31/12/11 manoharan quantity__stduom should be updated
				//String stockOpt = chkStockOption(itemCode, siteCode);
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
					stockOpt = rs.getString(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if (stockOpt == null || stockOpt.trim().length() == 0 || stockOpt.equals("N"))
				{
					sql = "SELECT CASE WHEN STK_OPT IS NULL THEN '0' ELSE STK_OPT END "
						 +"FROM ITEM "
						 +"WHERE ITEM_CODE = ? ";
						 pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);				
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							stockOpt = rs.getString(1);
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;
				}//end if
				if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))
				{
					System.out.println("updateStatus updateStatus[[[[:"+updateStatus);
					sql = "SELECT ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY__stduom as QUANTITY FROM PORCPDET WHERE TRAN_ID = ? AND LINE_NO =? "; // Gulzar 17/05/07
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					pstmt.setString(2,lineNo);					
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemCode	= rs.getString("ITEM_CODE");
						locCode		= rs.getString("LOC_CODE"); 
						lotNo		= rs.getString("LOT_NO"); 
						lotSl		= rs.getString("LOT_SL"); 
						quantityStr = rs.getString("QUANTITY");
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;					
				}				
				quantity = Double.parseDouble(quantityStr);
				if (lotNo == null || lotNo.trim().length() == 0)
				{
					lotNo = "               ";
				}
				if (lotSl == null || lotSl.trim().length() == 0)
				{
					lotSl = "     ";
				}				
				if (!stockOpt.equalsIgnoreCase("0"))
				{
					line = "   " + lineNo;
					System.out.println("line1  :"+line);
					
					strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","P-RET");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));					
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(-1*quantity)); 
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_PRETURN");
					
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
					errInvAllocTrace = invAllocTraceBean.updateInvallocTrace(strAllocate, conn);
					if(errInvAllocTrace.trim().length()>0)
					{
						System.out.println("invAllocTraceBeanupdateInvallocTrace(hashMap, conn) : Sucessuful");
					}
					/*END*/
				}// end if
			}// end if
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in PurcRetnUpdateAllocPrs :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in PurcRetnUpdateAllocPrsEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}							
			}
			catch(Exception e)
			{
				System.err.println("Exception :DBAccessEJB :getITMVersion :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return "";
	}//ConsumeIssuePrs end

	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail2");
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
}