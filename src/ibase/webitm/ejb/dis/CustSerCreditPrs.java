package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
//public class CustSerCreditPrsEJB extends ValidatorEJB implements SessionBean
public class CustSerCreditPrs extends ValidatorEJB implements CustSerCreditPrsLocal, CustSerCreditPrsRemote
{
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
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String preSaveForm()throws RemoteException,ITMException
	{
		return "";
	}
	
	public String preSaveForm(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("CustSerCreditPrsEJB called...");
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
//				dom = GenericUtility.getInstance().parseString(xmlString1);
				dom = genericUtility.parseString(xmlString1);
				executepreSaveForm(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :CustSerCreditPrsEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	
	private String executepreSaveForm(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String sql = "", bList = "", custCode = "", itemSer = "", updateStatus = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		NodeList hdrDom = null;
		Node currDetail = null;
		try
		{
			hdrDom = dom.getElementsByTagName("Detail1");
			/*bList = GenericUtility.getInstance().getColumnValueFromNode("black_listed",hdrDom.item(0));
			custCode = GenericUtility.getInstance().getColumnValueFromNode("cust_code",hdrDom.item(0));
			itemSer = GenericUtility.getInstance().getColumnValueFromNode("item_ser",hdrDom.item(0));*/
			bList = genericUtility.getColumnValueFromNode("black_listed",hdrDom.item(0));
			custCode = genericUtility.getColumnValueFromNode("cust_code",hdrDom.item(0));
			itemSer = genericUtility.getColumnValueFromNode("item_ser",hdrDom.item(0));
			System.out.println("bList :"+bList+" custCode :"+custCode+" itemSer :"+itemSer);
			currDetail = hdrDom.item(0);
			updateStatus = getCurrentUpdateFlag(currDetail);
			System.out.println("currDetail  :"+currDetail+" updateStatus :"+updateStatus);
			if (currDetail != null && (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("A")))
			{
				if (bList != null && bList.equalsIgnoreCase("Y"))
				{
					updateSorder(custCode, itemSer, conn);
				}
			}//end if
		}//try end
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in CustSerCreditPrsEJB  :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private void updateSorder(String custCode, String itemSer, Connection conn)throws RemoteException,ITMException
	{
		Statement stmtSorder = null, stmtSorderItem = null;
		ResultSet rsSorder = null, rsSorderItem = null;
		PreparedStatement pstmt = null;
		String sql = "", sql1 = "",sOrder = "", status = "";
		double qtyAlloc = 0, qtyDesp = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			stmtSorder = conn.createStatement();
			stmtSorderItem = conn.createStatement();
			sql = "SELECT SALE_ORDER "
				 +"FROM SORDER "
				 +"WHERE CUST_CODE = '"+custCode+"' "
			     +"AND ITEM_SER = '"+itemSer+"' "
			     +"AND STATUS = 'P' ";
			System.out.println("sql :"+sql);
			rsSorder = stmtSorder.executeQuery(sql);
			while (rsSorder.next())
			{
				sOrder = rsSorder.getString("SALE_ORDER");
				System.out.println("sOrder :"+sOrder);
				sql1 = "SELECT CASE WHEN SUM(QTY_ALLOC) IS NULL THEN 0 ELSE SUM(QTY_ALLOC) END , " 
			          +"CASE WHEN SUM(QTY_DESP) IS NULL THEN 0 ELSE SUM(QTY_DESP) END " 
					  +"FROM SORDITEM "
					  +"WHERE SALE_ORDER = '"+sOrder+"' "
				      +"AND LINE_TYPE = 'I' ";
				System.out.println("sql1 :"+sql1);
				rsSorderItem = stmtSorderItem.executeQuery(sql1);
				if (rsSorderItem.next())
				{
					qtyAlloc = rsSorderItem.getDouble(1);
					qtyDesp = rsSorderItem.getDouble(2);
					System.out.println("qtyAlloc  :"+qtyAlloc+"qtyDesp   :"+qtyDesp);
				}
				if (qtyAlloc == 0 && qtyDesp == 0)
				{
					status = "X";
				} 
				else
				{
					status = "C";
				}
				System.out.println("status :"+status);

				sql = "UPDATE SORDER SET STATUS = ? WHERE SALE_ORDER = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,status);
				pstmt.setString(2,sOrder);
				int updCnt = pstmt.executeUpdate();
				System.out.println(updCnt+" Records Updated");
			}
		}
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in updateSorder(CustSerCreditPrsEJB) :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in updateSorder(CustSerCreditPrsEJB)  :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (stmtSorder != null)
				{
					stmtSorder.close();
					stmtSorder = null;
				}
				if (stmtSorderItem != null)
				{
					stmtSorderItem.close();
					stmtSorderItem = null;
				}
				if (rsSorder != null)
				{
					rsSorder.close();
					rsSorder = null;
				}
				if (rsSorderItem != null)
				{
					rsSorderItem.close();
					rsSorderItem = null;
				}
				if (pstmt != null)
				{
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.err.println("Exception :updateSorder(CustSerCreditPrsEJB) : \n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
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