/* This file is Cteated it incorporate the Post Save logic from Window (w_lc_det)
 * Gulzar 08/02/07
*/

package ibase.webitm.ejb.dis;

import java.sql.*;
import java.rmi.RemoteException;
import java.util.*;

import org.w3c.dom.*;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

//public class LCDetPosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class LCDetPos extends ValidatorEJB implements LCDetPosLocal, LCDetPosRemote
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
	}*/
	public String postSaveRec() throws RemoteException, ITMException
	{
		return "";
	}
	public String postSave(String xmlString1, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("LCDetPosEJB Called..postSave..........!");
		Document dom = null;
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				executePostSave(dom,editFlag,xtraParams,conn);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :LCDetPosEJB :postSave(): " + e.getMessage()+ ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	
	}
	
	public String postSaveRec(String xmlString, String domId, String objContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("LCDetPosEJB Called............!");
		Document dom = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);
				executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :LCDetPosEJB :postSave(): " + e.getMessage()+ ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	private String executePostSave(Document dom, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String sql = "",dlvTo = "",	dlvAdd1 = "", dlvAdd2 = "", dlvAdd3 = "", city = "";
		String stateCodeDlv = "", countCodeDlv = "", pinDlv = "", stanCodeDlv = "", tel1Dlv = "";
		String tel2Dlv = "", tel3Dlv = "", faxDlv = "", saleOrder = "";
		NodeList hdrDom = null;
		Node currDetail = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int  count = 0;

		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{

			dlvTo = genericUtility.getColumnValue("dlv_to",dom);
			dlvAdd1 = genericUtility.getColumnValue("dlv_add1",dom);
			dlvAdd2 = genericUtility.getColumnValue("dlv_add2",dom);
			dlvAdd3 = genericUtility.getColumnValue("dlv_add3",dom);
			city = genericUtility.getColumnValue("dlv_city",dom);
			stateCodeDlv = genericUtility.getColumnValue("state_code__dlv",dom);
			countCodeDlv = genericUtility.getColumnValue("count_code__dlv",dom);
			pinDlv = genericUtility.getColumnValue("dlv_pin",dom);
			stanCodeDlv = genericUtility.getColumnValue("stan_code__dlv",dom);
			tel1Dlv = genericUtility.getColumnValue("tel1__dlv",dom);
			tel2Dlv = genericUtility.getColumnValue("tel2__dlv",dom);
			tel3Dlv = genericUtility.getColumnValue("tel3__dlv",dom);
			faxDlv = genericUtility.getColumnValue("fax__dlv",dom);
			saleOrder = genericUtility.getColumnValue("sale_order",dom);
		
		
			if (dlvTo != null && dlvTo.trim().length() > 0)
			{
				sql = "select count(1) from sorder where sale_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1) ;
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;

				if (count > 0 )
				{
					sql = "update sorder set dlv_to = ?, "
						+ " dlv_add1 = ?, "
						+ " dlv_add2 = ?, "
						+ " dlv_add3 = ?, "
						+ " dlv_city = ?, "
						+ " state_code__dlv = ?, "
						+ " count_code__dlv = ?, "
						+ " dlv_pin = ?, "
						+ " stan_code = ?, "
						+ " tel1__dlv = ?, "
						+ " tel2__dlv = ?, "
						+ " tel3__dlv = ?, "
						+ " fax__dlv  = ? " 
						+ " where sale_order = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,dlvTo);
					pstmt.setString(2,dlvAdd1);
					pstmt.setString(3,dlvAdd2);
					pstmt.setString(4,dlvAdd3);
					pstmt.setString(5,city);
					pstmt.setString(6,stateCodeDlv);
					pstmt.setString(7,countCodeDlv);
					pstmt.setString(8,pinDlv);
					pstmt.setString(9,stanCodeDlv);
					pstmt.setString(10,tel1Dlv);
					pstmt.setString(11,tel2Dlv);
					pstmt.setString(12,tel3Dlv);
					pstmt.setString(13,faxDlv);
					pstmt.setString(14,saleOrder);
					count = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;

					if (count > 0)
					{
						System.out.println("Update Into SORDDET Successfully....:: " + count);
					}
					else if (count == 0)
					{
						System.out.println("No Updation Occur Into SORDER ....:: " + count);
					}
				}
			}
		}
		catch (SQLException sqe)
		{
			System.out.println("The SQLException occurs in LCDetPosEJB...... :"+sqe);
			try
			{
				conn.rollback();
			}
			catch (Exception sqe1){}
			sqe.printStackTrace();
			throw new ITMException(sqe);
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in LCDetPosEJB........ :"+e);	
			try
			{
				conn.rollback();
			}
			catch (Exception e1){}
			e.printStackTrace();
			throw new ITMException(e);
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
				System.err.println("Exception :LCDetPosEJB......... :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return "";
	}
	private String executepostSaveRec(Document dom, String domID, String ObjContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String saleOrder = "", lineNoSord = "", descr = "", sqlUpd = "";
		String updateStatus = "";
		NodeList hdrDom = null;
		Node currDetail = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		int  updCnt = 0;

		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			currDetail 	= getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);
			
			System.out.println("updateStatus :: "+updateStatus);

			if (currDetail != null)
			{
				saleOrder	= genericUtility.getColumnValueFromNode("sale_order",currDetail);
				lineNoSord	= genericUtility.getColumnValueFromNode("line_no__sord",currDetail);
				descr		= genericUtility.getColumnValueFromNode("descr",currDetail);

				System.out.println("saleOrder -->>:: "+saleOrder);
				System.out.println("lineNoSord -->>::"+lineNoSord);
				System.out.println("descr-->> ::"+descr);

				lineNoSord = "   " + lineNoSord;
				lineNoSord = lineNoSord.substring(lineNoSord.length()-3);
				System.out.println("lineNoSord ::"+lineNoSord);
				//	sqlUpd = "UPDATE SORDDET SET ITEM_DESCR = '"+descr+"' WHERE SALE_ORDER = '"+saleOrder+"' AND LINE_NO = '"+lineNoSord+"'";
				//changes done by cpandey on 24/01/13
				sqlUpd = "update sorddet set item_descr = ? where sale_order = ? and line_no = ?";
				System.out.println("PREPARE STATEMENT --->>");
				pstmt = conn.prepareStatement(sqlUpd);
				pstmt.setString(1,descr);
				pstmt.setString(2,saleOrder);
				pstmt.setString(3,lineNoSord);
				updCnt = pstmt.executeUpdate();
				System.out.println("updCnt-->>["+updCnt+"]");
				//end of changes done by cpandey on 24/01/13
				if (updCnt > 0)
				{
					System.out.println("Update Into SORDDET Successfully....:: " + updCnt);
					//conn.commit();
				}
				else if (updCnt == 0)
				{
					System.out.println("No Updation Occur Into SORDDET ....:: " + updCnt);
				}
				else if (updCnt < 0)
				{
					System.out.println("Update Into SORDDET Failed and Rolling Back The Changes....:: " + updCnt);
					conn.rollback();
				}
			}
		}
		catch (SQLException sqe)
		{
			System.out.println("The SQLException occurs in LCDetPosEJB...... :"+sqe);
			try
			{
				conn.rollback();
			}
			catch (Exception sqe1){}
			sqe.printStackTrace();
			throw new ITMException(sqe);
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in LCDetPosEJB........ :"+e);	
			try
			{
				conn.rollback();
			}
			catch (Exception e1){}
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
				System.err.println("Exception :LCDetPosEJB......... :\n"+e.getMessage());
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
}
