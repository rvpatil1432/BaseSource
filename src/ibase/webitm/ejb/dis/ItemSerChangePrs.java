package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import javax.ejb.*;
import org.w3c.dom.*;
import java.text.SimpleDateFormat;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3


//public class ItemSerChangePrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class ItemSerChangePrs extends ValidatorEJB implements ItemSerChangePrsLocal, ItemSerChangePrsRemote
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
		System.out.println("ejbPassivate() method calling........");
	}*/
	
	public String preSaveForm()throws RemoteException,ITMException
	{
		return "";
	}
	
	public String preSaveForm(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("ItemSerChangePrsEJB called...");
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				executepreSaveForm(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :ItemSerChangePrsEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	
	private String executepreSaveForm(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		PreparedStatement pstmt = null;
		String sql = "", itemCode = "", effDate = "", itemSer = "", updateStatus = "";
		int updCnt = 0;
		java.util.Date effDate1 = null, preDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		NodeList hdrDom = null;
		Node currDetail = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			hdrDom = dom.getElementsByTagName("Detail1");
			itemCode = genericUtility.getColumnValueFromNode("item_code",hdrDom.item(0));
			itemSer = genericUtility.getColumnValueFromNode("item_ser",hdrDom.item(0));
			effDate = genericUtility.getColumnValueFromNode("eff_date",hdrDom.item(0));
			System.out.println("itemCode :"+itemCode+" effDate :"+effDate+" itemSer :"+itemSer);
			effDate1 = sdf.parse(effDate);
			System.out.println("effDate1   :"+effDate1);
			//for getting the previous Date from the effDate
			Calendar  cal = Calendar.getInstance();
			cal.setTime(effDate1);
			cal.add(Calendar.DATE,-1);
			preDate = cal.getTime();
			System.out.println("preDate  :"+preDate);
			currDetail = hdrDom.item(0);
			updateStatus = getCurrentUpdateFlag(currDetail);
			System.out.println("currDetail  :"+currDetail+" updateStatus :"+updateStatus);
			if (currDetail != null && updateStatus.equalsIgnoreCase("A"))
			{
				sql = "UPDATE ITEMSER_CHANGE SET VALID_UPTO = ? "
					 +"WHERE ITEM_CODE = ? "
					 +"AND VALID_UPTO IS NULL ";
				System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1,new java.sql.Date(preDate.getTime()));
				pstmt.setString(2,itemCode);
				updCnt = pstmt.executeUpdate();
				System.out.println(updCnt+" Records Updated");
				sql = "UPDATE ITEM SET ITEM_SER = ? "
					 +"WHERE ITEM_CODE = ? ";
				System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemSer);
				pstmt.setString(2,itemCode);
				updCnt = pstmt.executeUpdate();
				System.out.println(updCnt+" Records Updated");				
			}//end if
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ItemSerChangePrsEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in ItemSerChangePrsEJB  :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			try
			{
				if (pstmt!= null)
				{
					pstmt = null;
				}	
			}
			catch(Exception e)
			{
				System.out.println("The SQLException occurs in ItemSerChangePrsEJB  :"+e);			
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return "";
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