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
import org.w3c.dom.NodeList;

@javax.ejb.Stateless
public class ChgUserAllocPostSave extends ValidatorEJB implements ChgUserAllocPostSaveLocal,ChgUserAllocPostSaveRemote{
	
	boolean isLocalConn = false;
	
	public String postSave(String xmlString,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println(">>>>>>>>>>>>>>CONNECTION"+conn);
		System.out.println("------------ DisIssDelWizPostSave postSave method called-----------------");		
		Document dom = null;
		String errString="";
		E12GenericUtility genericUtility= new  E12GenericUtility();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
				System.out.println("xmlString  *===> " + xmlString);
			}
			
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
		String issDspNo="", chgTerm = "", chgUserNew = "";
		String errString = "", errCode = "", chgUser = "", chgUserDom = "", userId = "";
		int cnt = 0;
		//GenericUtility genericUtility;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		
		
		NodeList parentNodeList = null;
		int ctr=0;
		int detailListLength = 0;

		System.out.println("tranId in postSave dom ----> "+tranId);
		
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try
		{
			/*chgTerm = GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			chgUser = GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			genericUtility = GenericUtility.getInstance();*/
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			
			
			chgUserNew = genericUtility.getColumnValue("code",dom);
			chgUserDom = genericUtility.getColumnValue("chg_user",dom);
			
			chgUserDom = chgUserDom == null ? "" : chgUserDom.trim();
			chgUserNew = chgUserNew == null ? "" : chgUserNew.trim();
			
			System.out.println("chguser >>>>>>>>>> "+chgUserDom+" chgusernew >>>>>>>>>>>>> "+chgUserNew);
			
			if (chgUserNew == null || chgUserNew.trim().length() == 0)
			{
				errCode = "USERNULL";
				errString = getErrorString("code",errCode,userId);
				//break;
			}
			else
			{

				sql = "select count(1) from users where code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,chgUserNew);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				pstmt.close();
				rs.close();
				pstmt = null;
				rs = null;
				if(cnt == 0)
				{
					errCode = "USERNOTEXT";
					errString = getErrorString("code",errCode,userId);
					//break;
				}else if(chgUserNew.equals(chgUserDom)){
					errCode = "SAMEUSER";
					errString = getErrorString("code",errCode,userId);
					//break;
				}else{
					parentNodeList = dom.getElementsByTagName("Detail2");
					System.out.println("parentNodeList length >>>>>>>>>>>>>>>>>>>>> "+parentNodeList.getLength());
					detailListLength = parentNodeList.getLength();
					for(ctr = 0; ctr < detailListLength; ctr++)
					{
						//issDspNo = GenericUtility.getInstance().getColumnValue("iss_dsp_no",dom);
						issDspNo = genericUtility.getColumnValue("iss_dsp_no",dom);
						//chgUserNew = GenericUtility.getInstance().getColumnValue("code",dom);
						
						System.out.println("issDspNo in DOM --->>" +issDspNo);
						System.out.println("chgUserNew in DOM --->>" +chgUserNew);
								
							sql = "update distord_iss set add_user = ?, add_term = ?, add_date = sysdate where tran_id = ? and (confirmed is null or confirmed = 'N')";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, chgUserNew);
							pstmt.setString(2, chgTerm);
							pstmt.setString(3, issDspNo);
							cnt = pstmt.executeUpdate();
							
							if(pstmt != null){
								pstmt.close();
								pstmt = null;
							}
							
							sql = "update despatch set add_user = ?, add_term = ?, add_date = sysdate where desp_id = ? and (confirmed is null or confirmed = 'N')";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, chgUserNew);
							pstmt.setString(2, chgTerm);
							pstmt.setString(3, issDspNo);
							cnt = pstmt.executeUpdate();
							
							if(pstmt != null){
								pstmt.close();
								pstmt = null;
							}
							
							System.out.println(">>>>>>>successfully deleted record ChgUserAllocPostSave cnt = :" + cnt);
					}	 
					
					conn.commit();
					System.out.println(">>>>>>> commit executed ");
				}
			}
			
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

		return errString;

	}
}
