/********************************************
	Title : Failed Business Logic Override
	Date  : 24/09/18
	Developer: Pavan Rane
 ********************************************/
package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.adv.SOrderAmdConf;
import ibase.webitm.ejb.dis.adv.SorderConf;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.ejb.Stateless;

/**
 * Session Bean implementation class BusinessLogicChkOverride
 */
@Stateless
public class BusinessLogicChkOverride extends ActionHandlerEJB{

	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("---------:: Inside BusinessLogicChkOverride confirm()::-------");
		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.override(tranID, xtraParams, forcedFlag, conn); 				
		}
		catch(Exception e)
		{
			System.out.println("Exception in [BusinessLogicChkOverride] confirm()" + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String override(String tranId, String xtraParams, String forcedFlag, Connection conn)throws RemoteException, ITMException
	{
		System.out.println("-------------:: Inside BusinessLogicChkOverride Override() ::-------------");
		E12GenericUtility genericUtility = null;		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccess = null;
		SorderConf sordConf = null;
		SOrderAmdConf sordAmdConf = null;		
		String sql = "";	
		String sorder = "";
		String crPolicy = "";
		//String custCode = "";
		String aprvStat = "";
		String confirmed = "";
		String amdNo = "";
		String userId="";
		int cnt = 0;
		int updCnt = 0;
		int overrideCnt = 0;
		//double totAmt = 0.0d;
		Timestamp today = null;
		String errString = "";
		String tranType="";//added by nandkumar gadkari on 01/10/19
		boolean isLocal=false;  //added by manish mhatre on 21-may-2020
		
		try 
		{	
			//added by manish mhatre on 21-may-2020
			if(conn==null)
			{
			conn = getConnection();
			conn.setAutoCommit(false);	
			}
			else
			{
				isLocal=true;
			}  //end manish
			genericUtility = new E12GenericUtility();
			itmDBAccess = new ITMDBAccessEJB();			

			today = new java.sql.Timestamp(System.currentTimeMillis()) ;
			String empCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginEmpCode" );
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			System.out.println("Pavan Transaction Id::["+tranId+"]userId["+userId+"]empCode["+empCode+"]");
			sql = "select aprv_stat, sale_order, cr_policy, amd_no,tran_type from business_logic_check where tran_id = ?";//tran type added by nandkumar gadkari on 01/10/19
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				aprvStat = checkNull(rs.getString("aprv_stat"));
				sorder = checkNull(rs.getString("sale_order"));
				crPolicy = checkNull(rs.getString("cr_policy"));
				amdNo = checkNull(rs.getString("amd_no"));
				tranType = checkNull(rs.getString("tran_type"));//tran type added by nandkumar gadkari on 01/10/19
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("Pavan>>>:: aprvStat["+aprvStat+"]sorder["+sorder+"]crPolicy["+crPolicy+"]amdNo["+amdNo+"]");
			if ("O".equals(aprvStat))
			{					
				errString = itmDBAccess.getErrorString("", "VTCRCHKOVR", "","",conn);
				return errString;			        
			} 
			else 
			{				
				/*sql = "select tot_amt, cust_code__bil from sorder where sale_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, sorder);
				rs = pstmt.executeQuery(); 
				if (rs.next()) 
				{
					totAmt = rs.getDouble("tot_amt");
					custCode = checkNull(rs.getString("cust_code__bil"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				 */
				sql = "update BUSINESS_LOGIC_CHECK set APRV_STAT = 'O', EMP_CODE__APRV = ?, APRV_DATE = ? where TRAN_ID = ?";				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, userId);
				pstmt.setTimestamp(2, today);
				pstmt.setString(3, tranId);
				updCnt = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("BUSINESS_LOGIC_CHECK 1st updCnt[ "+updCnt+" ]");
				//Added by Anagha Rane 05/02/2020 START
				//for Fail Bussiness logic not giving any message that its successfully done 

				errString = itmDBAccess.getErrorString("", "OVRRIDSUCC", "","",conn);

				//Added by Anagha Rane 05/02/2020 END
			}
			if(amdNo!= null && amdNo.trim().length() > 0)
			{				
				sql = "select (case when CONFIRMED is null then 'N' else CONFIRMED end) as CONFIRMED from SORDAMD where AMD_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs = pstmt.executeQuery(); 
				if (rs.next()) 
				{					
					confirmed = checkNull(rs.getString("CONFIRMED"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if("N".equals(confirmed))
				{					
					sql = "select count(*) as cnt from business_logic_check where aprv_stat <> 'O' and  amd_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, amdNo);
					rs = pstmt.executeQuery(); 
					if (rs.next()) 
					{					
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(cnt == 0 )        //Update Aprv_stat as M for confirm so amd.
					{						
						sql = "update business_logic_check set aprv_stat = 'M', emp_code__aprv = ?, aprv_date = ? where tran_id = ?";
						pstmt = conn.prepareStatement(sql);						
						pstmt.setString(1, userId);
						pstmt.setTimestamp(2, today);
						pstmt.setString(3, tranId);
						updCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("BUSINESS_LOGIC_CHECK 3rd updCnt[ "+updCnt+" ]");			         																					
						sordAmdConf = new SOrderAmdConf();						
						errString = sordAmdConf.confirmSorderAmd(amdNo,sorder,"",xtraParams ,conn);
						sordAmdConf = null;
						System.out.println("SorderAmd Confirm returning... ["+errString+"]");
						if (errString.indexOf("VTCNFSUCC") > 0 || errString.trim().length() == 0)						
						{									
							errString = itmDBAccess.getErrorString("", "OVRRIDSUCC", "","",conn);
						}else
						{
							return errString;
						}
					}    

				}
			}
			else
			{


				if(!"C".equalsIgnoreCase(tranType))//condition added by nandkumar gadkari on 01/10/19
				{
					sql = "select (case when confirmed is null then 'N' else confirmed end) as CONFIRMED from sorder where sale_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, sorder);
					rs = pstmt.executeQuery(); 
					if (rs.next()) 
					{					
						confirmed = checkNull(rs.getString("CONFIRMED"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select count(*) as cnt from business_logic_check where sale_order = ? and aprv_stat  = 'F'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, sorder);
					rs = pstmt.executeQuery(); 
					if (rs.next()) 
					{					
						overrideCnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if( overrideCnt == 0 )
					{ 

						sql = "update sorder set cr_check_stat = 'O' where sale_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sorder);
						updCnt = pstmt.executeUpdate();
						System.out.println("");
						pstmt.close();
						pstmt = null;
						System.out.println("BUSINESS_LOGIC_CHECK 4rd updCnt[ "+updCnt+" ]");
					}
				}
				else//condition added by nandkumar gadkari on 01/10/19
				{
					sql = "select (case when confirmed is null then 'N' else confirmed end) as confirmed from charge_back where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, sorder);
					rs = pstmt.executeQuery(); 
					if (rs.next()) 
					{					
						confirmed = checkNull(rs.getString("CONFIRMED"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}

				if("N".equals(confirmed))
				{        					
					sql = "select count(*) as cnt from business_logic_check  where aprv_stat <> 'O' and sale_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, sorder);
					rs = pstmt.executeQuery(); 
					if (rs.next()) 
					{					
						cnt = rs.getInt("cnt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("unconfirmed cnt "+cnt);
					if( cnt == 0 )
					{	
						if(!"C".equalsIgnoreCase(tranType))//condition added by nandkumar gadkari on 01/10/19
						{
							sordConf = new SorderConf();			        				         
							errString =  sordConf.confirmSorder(sorder, "", xtraParams, conn);
							sordConf = null;	
						}
						System.out.println("Sorder Confirm returning... ["+errString+"]");
						if (errString.indexOf("VTCNFSUCC") > 0 || errString.trim().length() == 0)			    
						{							
							errString = itmDBAccess.getErrorString("", "OVRRIDSUCC", "","",conn);
						}else
						{
							return errString;
						}
					}
				}

			}
		}
		catch (Exception e) 
		{
			if(conn!=null)
			{
				try 
				{	
					System.out.println("@@@@  Transaction rollback... ");					
					conn.rollback();					
				} 
				catch (SQLException ex) 
				{
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			System.out.println("Exception in BusinessLogicChkOverride Override()::::["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{		
			try
			{
				System.out.println("--going to commit tranaction--["+errString+"]"+"islocal"+isLocal);
				//if(errString.indexOf("OVRRIDSUCC") > -1 || errString.trim().length() == 0)
				//Commented and added below if condition by Varsha V to consider empty error string as no error
				//if(errString.indexOf("OVRRIDSUCC") > -1)
				if((errString == null || errString.trim().length() == 0) || (errString.indexOf("OVRRIDSUCC") > -1))
				{
					//if condition added by manish mhatre on 21-may-2020
					if(!isLocal)
					{
					conn.commit();
					System.out.println("--transaction commited--");
					}
				}
				else
				{
					conn.rollback();
					System.out.println("--transaction rollback--");
				}			
				if(conn != null && !isLocal)// added by nandkumar
				{
					conn.close();
					conn = null;
				}
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
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}

		return errString;
	}

	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str;
		}

	}

}