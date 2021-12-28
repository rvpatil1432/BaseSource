package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;
import org.w3c.dom.*;
import java.text.SimpleDateFormat;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3


//public class ItemLotPacksizePrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class ItemLotPacksizePrs extends ValidatorEJB implements ItemLotPacksizePrsLocal, ItemLotPacksizePrsRemote
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

	public String preSaveForm()throws RemoteException,ITMException
	{
		return "";
	}
	
	public String preSaveForm(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("ItemLotPacksizePrsEJB called...");
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
			System.out.println("Exception :ItemLotPacksizePrsEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	
	private String executepreSaveForm(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		PreparedStatement pstmt = null;
		String sql = "", itemCode = "", lotNoFrom = "", set = "", newLotNoStr = "", oldLotNo = "", updateStatus = "";
		int updCnt = 0, count = 0, lenlotNoFrom = 0, counter = 0, oldLen = 0, newLotNoFrom  = 0, newLotLen = 0;
		Statement stmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		NodeList hdrDom = null;
		Node currDetail = null;
		try
		{
			stmt = conn.createStatement();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			hdrDom = dom.getElementsByTagName("Detail1");
			itemCode = genericUtility.getColumnValueFromNode("item_code",hdrDom.item(0));
			lotNoFrom = genericUtility.getColumnValueFromNode("lot_no__from",hdrDom.item(0));
			System.out.println("itemCode :"+itemCode+" lotNoFrom :"+lotNoFrom);
			currDetail = hdrDom.item(0);
			updateStatus = getCurrentUpdateFlag(currDetail);
			if (currDetail != null && updateStatus.equalsIgnoreCase("A"))
			{
				oldLotNo = lotNoFrom;
				sql = "SELECT COUNT(*) FROM ITEM_LOT_PACKSIZE "
					 +"WHERE ITEM_CODE = '"+itemCode+"' "
					 +"AND LOT_NO__FROM <= '"+lotNoFrom+"' "
					 +"AND LOT_NO__TO >= '"+lotNoFrom+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					count = rs.getInt(1);
				}
				System.out.println("count :"+count);
				if (count > 0)
				{
					if (lotNoFrom != null)
					{
						lenlotNoFrom = lotNoFrom.trim().length();
					}
					counter = 1;
					while (counter <= lenlotNoFrom)
					{
						if (checkIsNumber(lotNoFrom))
						{
							if (lotNoFrom != null && lotNoFrom.startsWith("0"))
							{
								lotNoFrom = lotNoFrom.substring(1);
								System.out.println("After getting mid lotNoFrom :"+lotNoFrom);
								set = set + "0";
							}
							else
							{
								oldLen = lotNoFrom.trim().length();
								newLotNoFrom = Integer.parseInt(lotNoFrom == null ?"0":lotNoFrom.trim()) - 1; 
								newLotLen = String.valueOf(newLotNoFrom).trim().length();
								System.out.println("newLotLen :"+newLotLen+"oldLen  :"+oldLen+"newLotNoFrom :"+newLotNoFrom);
								newLotNoStr = "";
								if (oldLen != newLotLen)
								{
									int cnt = oldLen - newLotLen;
									for (int i = 0; i< cnt; i++)
									{
										newLotNoStr = newLotNoStr + "0";
									}
									System.out.println("newLotNoStr :"+newLotNoStr);
								}
							}//end else
						}//end if
						else
						{
							String original = lotNoFrom.substring(0,counter);
							set = set + original;
							//lotNoFrom = lotNoFrom.substring(counter+1);	// 18/10/13 Manoharan commented as one character skipped
							lotNoFrom = lotNoFrom.substring(counter);	// 18/10/13 Manoharan added as one character skipped
							
							
							
						}
						counter ++;
					}//end while
					lotNoFrom = set + newLotNoStr.trim() + String.valueOf(newLotNoFrom);
					System.out.println("lotNoFrom  :"+lotNoFrom);
					sql = "UPDATE ITEM_LOT_PACKSIZE "
						 +"SET LOT_NO__TO = ? "
						 +"WHERE ITEM_CODE = ? "
						 +"AND LOT_NO__FROM <= ? "
						 +"AND LOT_NO__TO >= ? ";
					System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,lotNoFrom);
					pstmt.setString(2,itemCode);
					pstmt.setString(3,oldLotNo);
					pstmt.setString(4,oldLotNo);
					updCnt = pstmt.executeUpdate();
					System.out.println(updCnt+" Records Updated");
				}// end if
			}//end if
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ItemLotPacksizePrsEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in ItemLotPacksizePrsEJB  :"+e);			
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
				System.out.println("The SQLException occurs in ItemLotPacksizePrsEJB  :"+e);			
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
	private boolean checkIsNumber(String checkStr)
	{
		char ch = ' ';
		int len = 0;
		len = checkStr.length();
		boolean retBool = true;
		System.out.println("len :"+len);
		for (int i = 0; i < len; i++)
		{
			ch = checkStr.charAt(i);
			if (Character.isLetter(ch))
			{
				retBool = false;
				break;
			}
			else
			{
				retBool = true;
			}
		}//end for
		return retBool;
	}
}