/*
	Developed by : HATIM LAXMIDHAR
	Discription	 : Pre Save for the Adjustment Issue.
	Date		 : 01/01/2006
*/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.util.HashMap;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

//public class AdjIssPrs extends ValidatorEJB implements AdjIssPrsRemote, AdjIssPrsLocal // commentd for ejb3
@Stateless // added for ejb3
public class AdjIssPrs extends ValidatorEJB implements AdjIssPrsRemote, AdjIssPrsLocal // added for ejb3
{
	/*  comments for ejb3
	public void ejbCreate() throws RemoteException, CreateException 
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

    public String preSaveRec() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String preSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		System.out.println("AdjIssPrsEJB called");
		Document dom = null;
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				//dom = GenericUtility.getInstance().parseString(xmlString1);
				executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :AdjIssPrsEJB :preSaveRec() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		HashMap hashMap = new HashMap(); 
		String errCode = "", errString = "";
		String lineNo = ""; //kfld2
		String tranSer = "ADJISS";
		String line = "";
		String siteCode = "", itemCode = "", quantity = "", locCode = "", lotNo = "", lotSl = "";
		double allocQty = 0d;
		int cnt = 0;
		DistStkUpd distStkUpd = null; // for ejb3 
		String updateStatus = "",tranId = "";
		NodeList hdrDom = null;
		Node currDetail = null;
		Statement stmt = null; // Added by Jiten 19/05/06
		ResultSet rs = null; // Added by Jiten 19/05/06
		String sql = ""; // Added by Jiten 19/05/06
		try
		{	
			stmt = conn.createStatement(); // Added by Jiten 19/05/06				
			E12GenericUtility genericUtility= new  E12GenericUtility();	
			hdrDom = dom.getElementsByTagName("Detail1");
			/*siteCode = GenericUtility.getInstance().getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = GenericUtility.getInstance().getColumnValueFromNode("tran_id",hdrDom.item(0));*/
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));

			System.out.println("tran_id (kfld1) : " + tranId);
			currDetail = getCurrentDetailFromDom(dom,domID);		
			updateStatus = getCurrentUpdateFlag(currDetail);
								
			if (currDetail != null && !updateStatus.equalsIgnoreCase("A") && !updateStatus.equalsIgnoreCase("N")) //&& !updateStatus.equalsIgnoreCase("N") CONDITION ADDED BY NANDKUMAR GADKARI ON 10/01/18
			{
				/*itemCode	= GenericUtility.getInstance().getColumnValueFromNode("item_code",currDetail);
				locCode		= GenericUtility.getInstance().getColumnValueFromNode("loc_code",currDetail);
				lotNo		= GenericUtility.getInstance().getColumnValueFromNode("lot_no",currDetail);
				lotSl		= GenericUtility.getInstance().getColumnValueFromNode("lot_sl",currDetail);
				quantity	= GenericUtility.getInstance().getColumnValueFromNode("quantity",currDetail);
				lineNo		= GenericUtility.getInstance().getColumnValueFromNode("line_no",currDetail);*/
				itemCode	= genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode		= genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo		= genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl		= genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				quantity	= genericUtility.getColumnValueFromNode("quantity",currDetail);
				lineNo		= genericUtility.getColumnValueFromNode("line_no",currDetail);
				
				if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))	 //if condition added by jiten 19/05/06
				{
					//Commented And Changes Below - Gulzar 15/05/07
					//sql = "SELECT QUANTITY FROM ADJ_ISSRCPDET WHERE TRAN_ID = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
					sql = "SELECT ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY FROM ADJ_ISSRCPDET WHERE TRAN_ID = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						itemCode	= rs.getString("ITEM_CODE"); //Gulzar 15/05/07
						locCode		= rs.getString("LOC_CODE"); //Gulzar 15/05/07
						lotNo		= rs.getString("LOT_NO"); //Gulzar 15/05/07
						lotSl		= rs.getString("LOT_SL"); //Gulzar 15/05/07
						quantity	= rs.getString("QUANTITY");
					}
					//End Changes - Gulzar 15/05/07
				}
				System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCode :"+locCode+" lotNo :"+lotNo+" lotSl :"+lotSl+" quantity :"+quantity); //Gulzar 15/05/07
				allocQty = -1 * Double.parseDouble(quantity);
				
				System.out.println("lineNo: "+lineNo);
				line = "    " + lineNo;
				hashMap.put("tran_date", new java.sql.Date(System.currentTimeMillis()));
				hashMap.put("ref_ser",tranSer);
				hashMap.put("ref_id", tranId);
				hashMap.put("ref_line", line.substring(line.length()-4));
				hashMap.put("item_code", itemCode);
				hashMap.put("site_code", siteCode);
				hashMap.put("loc_code",locCode);
				hashMap.put("lot_no",lotNo);
				hashMap.put("lot_sl",lotSl);
				hashMap.put("alloc_qty", new Double(allocQty));
				hashMap.put("chg_win","W_ADJ_ISS");
				/*hashMap.put("chg_user", GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				hashMap.put("chg_term", GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "termId"));*/
				hashMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				hashMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));

				//Calling DistStkUpdEJB
				System.out.println("Calling DistStkUpdEJB.....");
				//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
				distStkUpd = new DistStkUpd();
				if (distStkUpd.updAllocTrace(hashMap, conn) > 0)
				{
					distStkUpd= null;
					System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					//TO BE ASKED!!!
				}
			}			
		}
		catch(SQLException e)
		{
			System.out.println("Exception : AdjIssPrsEJB : actionStock " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : AdjIssPrsEJB : actionHandler :" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (stmt !=null)
				{
					stmt.close();
				}
				if (rs != null)
				{
					rs.close();
				}
			}catch(Exception e){}	 
		}
		return errString;
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