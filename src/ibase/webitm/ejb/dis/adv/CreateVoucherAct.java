package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.CreatePoRcpVoucher;
import ibase.webitm.ejb.fin.FinCommon;

import ibase.webitm.utility.ITMException;

@Stateless

public class CreateVoucherAct extends ActionHandlerEJB implements CreateVoucherActLocal,CreateVoucherActRemote
{
	E12GenericUtility genericUtility = new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon = new DistCommon();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	String  retString = "";
	public String actionHandler(String tranId,String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null, pstmt1 = null, pstmtUpd = null;
		ResultSet rs = null, rs1 = null;
		Connection conn = null;boolean connStatus=false;
		String sql = "",tranSer = "";//Added by Anjali R. on [06/08/2018]
		//Modified by Anjali R. on[31/07/2018][resultFlag is declare to indicate whether voucher is created succfully or not.][Start]
		boolean resultFlag = false;
		//Modified by Anjali R. on[31/07/2018][resultFlag is declare to indicate whether voucher is created succfully or not.][End]
		try
		{
            
            if (conn == null)// changed by Gulzar - 25/11/11
			{
            	System.out.println("New Connection Created***");
				ConnDriver connDriver = null;
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				connDriver = null;
				connStatus = true;
			}
			System.out.println("Calling......actionHandler for Common Voucher");
			CreatePoRcpVoucher createVouc = new CreatePoRcpVoucher();
			//Commented and Added by sarita on 10 AUG 18 [START]
			/*retString = createVouc.createPoRcpVoucher(tranId,
					xtraParams, conn);*/
			retString = createVouc.poRcpVouchRetrieve(tranId, xtraParams, conn);
			//Commented and Added by sarita on 10 AUG 18 [END]
			System.out.println("CreateVoucher retString"+retString);
			
			if(retString.indexOf("Success") != -1)
			{
				//Modified by Anjali R. on[31/07/2018][resultFlag set to true if voucher created successfully.][Start]
				resultFlag = true;
				System.out.println("resultFlag--["+resultFlag+"]");
				//Modified by Anjali R. on[31/07/2018][resultFlag set to true if voucher created successfully.][End]
				String tranIdVoucher=retString.substring( retString.indexOf("<TranID>")+8, retString.indexOf("</TranID>"));
				System.out.println("tranIdVoucher is :"+tranIdVoucher);
				//Changes made by Anjali R on [03/08/2018][Start]
				retString = itmDBAccessEJB.getErrorString("", "VTVOUGEN", "","",conn);
				System.out.println("retString--["+retString+"]");
							
				//Modified by Anjali R. on [06/08/2018][To display receipt and return wise message][Start]
				sql = "select tran_ser from porcp where tran_id  = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					tranSer = rs.getString("tran_ser");
				}
				System.out.println("tranSer---["+tranSer+"]");
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
				if("P-RCP".equalsIgnoreCase(tranSer))
				{
					retString = replaceErrorMsg(retString,"trace","  Voucher no " + tranIdVoucher + "  generated against purchase receipt");
				}
				else if ("P-RET".equalsIgnoreCase(tranSer))
				{
					retString = replaceErrorMsg(retString,"trace","  Voucher no " + tranIdVoucher + "  generated against purchase return");
				}
				//retString = replaceErrorMsg(retString,"trace","  Voucher no " + tranIdVoucher + "  generated against purchase return");
				//Modified by Anjali R. on [06/08/2018][To display receipt and return wise message][End]
				System.out.println("CreateVoucher retString----["+retString+"]");
				//Changes made by Anjali R on [03/08/2018][End]
				return retString;
				
			}
		}//try
		catch(Exception exception)
		{   try {
			conn.rollback();
		} catch (Exception e1) {
		}
			exception.printStackTrace();
			throw new ITMException(exception);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close(); rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close(); pstmt = null;
				}
				//Modified by Anjali R. on[31/07/2018][If voucher created successfully,then connection commit else rollback ][Start]
				if(resultFlag == true)
				{
					conn.commit();
				}
				else
				{
					conn.rollback();
				}
				//Modified by Anjali R. on[31/07/2018][If voucher created successfully,then connection commit else rollback ][End]
				if (conn != null && !conn.isClosed() && connStatus) // Gulzar -
					// 25/11/11
                   {
                     conn.close();
                     conn = null;
                   }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		
		return retString;
	}
	//Changes made by Anjali R on [03/08/2018][To replace tag value from dom][Start]
	private String replaceErrorMsg (String retErrXml ,String tagName , String replaceWith)
	{
		E12GenericUtility genericUtility = new E12GenericUtility();
		Document dom = null;
		NodeList detail1NodeList = null;
		Node parentNode = null;
		Node childNode = null;
		NodeList childNodeList = null;
		int childNodeListLength = 0;
		String changedErrorString = null;
		try
		{
			if(retErrXml != null && retErrXml.trim().length() > 0)
			{
				dom = genericUtility.parseString(retErrXml);
				detail1NodeList = dom.getElementsByTagName("error");
				parentNode  = detail1NodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (int ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					String childNodeName = childNode.getNodeName();
					System.out.println("childNodeName---["+childNodeName+"]");
					if(tagName.equalsIgnoreCase(childNodeName))
					{
						childNode.getFirstChild().setNodeValue(replaceWith);
					}
				}
				changedErrorString = genericUtility.serializeDom(dom);
				System.out.println("changedErrorString---["+changedErrorString+"]");
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return changedErrorString;
	}
	//Changes made by Anjali R on [03/08/2018][To replace tag value from dom][En]
}
