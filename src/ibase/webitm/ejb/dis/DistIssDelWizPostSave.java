package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@javax.ejb.Stateless
public class DistIssDelWizPostSave extends ValidatorEJB implements DistIssDelWizPostSaveLocal,DistIssDelWizPostSaveRemote{
	
	boolean isLocalConn = false;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String postSave(String xmlString,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println(">>>>>>>>>>>>>>CONNECTION"+conn);
		System.out.println("------------ DisIssDelWizPostSave postSave method called-----------------tranId : "+ tranId);		
		Document dom = null;
		String errString="";
        
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
				System.out.println("xmlString  *===> " + xmlString);
			}
			
			tranId = genericUtility.getColumnValue("tran_id",dom);

			System.out.println("------------ DisIssDelWizPostSave postSave method called-----------------tranId from dom: "+ tranId);
			
			//conn = null;
			if(conn==null){
				
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				//connDriver= null;
				conn.setAutoCommit(false);
				isLocalConn = true;
			}
			errString = postSave(dom,tranId,editFlag,xtraParams,conn);


		}
		catch(Exception e)
		{
			System.out.println("Exception : DisIssDelWizPostSave : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}

	public String postSave(Document dom,String tranId,String editflag,String xtraParams,Connection conn)
	{
		System.out.println("post save dom arg  --------------- > "+dom);
		String sql = "", error = "";
		String lotSl="", chgTerm = "", chgUser = "";
		int cnt = 0;
		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		int childNodeListLength, detailListLength = 0;

		System.out.println("tranId in postSave dom ----> "+tranId);
		
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try
		{
			//lotSl = GenericUtility.getInstance().getColumnValue("lot_sl",dom);
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			
			parentNodeList = dom.getElementsByTagName("Detail3");
			System.out.println("parentNodeList length >>>>>>>>>>>>>>>>>>>>> "+parentNodeList.getLength());
			detailListLength = parentNodeList.getLength();
			for(ctr = 0; ctr < detailListLength; ctr++)
			{
			parentNode = parentNodeList.item(ctr);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			
			/*for(ctr = 0; ctr < childNodeListLength; ctr++)
			{*/
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("Child name --->> "+childNodeName);
				
				lotSl = genericUtility.getColumnValue("lot_sl",dom,"3");
				
				System.out.println("lotSl in DOM --->>" +lotSl);
						
				sql = "delete from distord_issdet where tran_id = ? and lot_sl = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				pstmt.setString(2, lotSl);
				cnt = pstmt.executeUpdate();
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
				System.out.println(">>>>>>>successfully deleted record DisIssDelWizPostSave cnt = :" + cnt);
				
				if(cnt > 0){
					
					sql = "update distord_iss set chg_date = sysdate, chg_user = ?, chg_term = ? where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, chgUser);
					pstmt.setString(2, chgTerm);
					pstmt.setString(3, tranId);
					cnt = pstmt.executeUpdate();
					
					if(pstmt != null){
						pstmt.close();
						pstmt = null;
					}
				}
			}	 
			
			//conn.commit();
			System.out.println(">>>>>>> commit executed ");

		}catch(Exception e)
		{
			try {
				System.out.println(">>>>>>>>>>>>In catch Before rollback>>>");
				conn.rollback();
				System.out.println(">>>>>>>>>>>>rollback  issued >>>");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				error=e1.getMessage();
			}
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			error=e.getMessage();

		}	
		finally
		{
			try {
				System.out.println(">>>>>>>>>>>>In finally Before Commit>>>");
				
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}

				if(rs != null){
					rs.close();
					rs = null;
				}
				
				if(isLocalConn){
					
					if(conn != null)
					{
						conn.close();	
						conn = null;
						isLocalConn = false;
					}
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error=e.getMessage();
			}		
		}

		return error;

	}
}
