package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PAMDActivityStatus 
{
 public String confirm(String tranId, String empCodeAprv, String xmlDataAll, String processId, String keyFlag) throws RemoteException, ITMException 
	{
		System.out.println("Activity Check JB");
		System.out.println("Parameters ::: tranId[" + tranId + "], empCodeAprv[" + empCodeAprv + "], xmlDataAll[" + xmlDataAll + "],processId["+processId+"],keyFlag["+keyFlag+"]");
		String retString = "";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		ConnDriver connDriver = new ConnDriver();
		try 
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			connDriver = null;
			conn.setAutoCommit(false);
			empCodeAprv = empCodeAprv == null ? "" : empCodeAprv.trim();
			
			int resultCnt = 0;
			String signStatus = "", userCode = "", processInfo[] = null, activityId = "", roleCode = "";
			String prvActivityId = "";
			boolean flag = true;
			
			processInfo = processId.split(":");
			prvActivityId = processInfo[2];
			System.out.println("prvActivityId["+prvActivityId+"],activityId["+activityId+"]");
			
			if("CHKUSRCOND".equalsIgnoreCase(keyFlag))
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add("P-AMD_SIGN_Act1");
				list.add("P-AMD_SIGN_Act13");
				list.add("P-AMD_SIGN_Act3");
				list.add("P-AMD_SIGN_Act16");
				list.add("P-AMD_SIGN_Act54");
				list.add("P-AMD_SIGN_Act58");
				list.add("P-AMD_SIGN_Act61");
				list.add("P-AMD_ON_REL_ISER_SV");
				list.add("P-AMD_ON_REL_ISER_SD");

				sql = "select previous_activity, seq_id "
						+"from wf_prc_status "
						+"where ltrim(rtrim(ref_ser)) = ? and ltrim(rtrim(ref_id)) = ? "
						+"and activity_id = ? and seq_id = ? and instance_id = ?";
						
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "P-AMD");
				pstmt.setString(2, tranId);
				pstmt.setString(3, prvActivityId);
				pstmt.setString(4, processInfo[3]);
				pstmt.setString(5, processInfo[1]);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					prvActivityId = rs.getString("previous_activity");
					if(list.contains(prvActivityId))
					{
						activityId = prvActivityId;
						flag = false;
					}
				}
				else
				{
					activityId = "NOTHERACT";
					flag = false;
				}
				System.out.println("prvActivityId["+prvActivityId+"],activityId["+activityId+"]");
	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				while(flag)
				{
					sql = "select previous_activity "
							+"from ( "
							+"select previous_activity, seq_id "
							+"from wf_prc_status "
							+"where ltrim(rtrim(ref_ser)) = ? and ltrim(rtrim(ref_id)) = ? "
							+"and activity_id = ? and seq_id <> 1 and instance_id = ? "
							+"order by seq_id desc "
							+") tab "
							+"where rownum = 1";
							
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "P-AMD");
					pstmt.setString(2, tranId);
					pstmt.setString(3, prvActivityId);
					pstmt.setString(4, processInfo[1]);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						prvActivityId = rs.getString("previous_activity");
						if(list.contains(prvActivityId))
						{
							activityId = prvActivityId;
							flag = false;
						}
					}
					else
					{
						activityId = "NOTHERACT";
						flag = false;
					}
					System.out.println("prvActivityId["+prvActivityId+"],activityId["+activityId+"]");
		
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				retString = activityId;
			}
			else
			{		
				if("POAMDAPPR1".equalsIgnoreCase(keyFlag)) 
				{
					activityId = "P-AMD_SIGN_Act1"; roleCode = "POAMD_APR1";
					
				}
				else if("POAMDAPPR2".equalsIgnoreCase(keyFlag)) 
				{
					activityId = "P-AMD_SIGN_Act13"; roleCode = "POAMD_APR2";
					
				}
				else if("POAMDAPPR3".equalsIgnoreCase(keyFlag))
				{
					activityId = "P-AMD_SIGN_Act3"; roleCode = "POAMD_APR3";
					
				}
				else if("POAMDAPPR4".equalsIgnoreCase(keyFlag))
				{
					activityId = "P-AMD_SIGN_Act16"; roleCode = "POAMD_APR4";
					
				}
				else if("POAMDAPPR5".equalsIgnoreCase(keyFlag))
				{
					activityId = "P-AMD_SIGN_Act54"; roleCode = "POAMD_APR5";
					
				}
				if("POAMDAPPR6".equalsIgnoreCase(keyFlag)) 
				{
					activityId = "P-AMD_SIGN_Act58"; roleCode = "POAMD_APR6";
				}
				else if("POAMDAPPR7".equalsIgnoreCase(keyFlag)) 
				{
					activityId = "P-AMD_SIGN_Act61"; roleCode = "POAMD_APR7";
				}
				else if("P-AMD_ON_REL_ISER_SV".equalsIgnoreCase(keyFlag))
				{
					activityId = "P-AMD_ON_REL_ISER_SV"; roleCode = "P-AMD_SV_A";
				}
				else if("P-AMD_ON_REL_ISER_SD".equalsIgnoreCase(keyFlag))
				{
					activityId = "P-AMD_ON_REL_ISER_SD"; roleCode = "P-AMD_SD_A";
				}
				else if("HOD1".equalsIgnoreCase(keyFlag))
				{
					activityId = "HOD1_SIGN"; roleCode = "ESCHOD";
				}
				else if("HOD2".equalsIgnoreCase(keyFlag))
				{
					activityId = "HOD2_SIGN"; roleCode = "ESCHOD";
				}
							
				sql = "select SIGN_STATUS from OBJ_SIGN_TRANS "
						+ "WHERE ltrim(rtrim(REF_SER)) = 'P-AMD' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
						+ "AND SIGN_STATUS = 'U'";
						
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				pstmt.setString(2, roleCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					signStatus = rs.getString("SIGN_STATUS");
				}
				System.out.println("signStatus = " + signStatus);
	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("U".equalsIgnoreCase(signStatus))
				{
					sql = "select code from users where emp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, empCodeAprv);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						userCode = rs.getString("code");
					}
					System.out.println("userCode = " + userCode);
		
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "UPDATE OBJ_SIGN_TRANS "
							+ "SET SIGN_DATE = sysdate, USER_ID__SIGN = ?, SIGN_REMARKS = 'HR_SPAN is over', SIGN_STATUS = 'V', EMP_CODE = ? "
							+ "WHERE ltrim(rtrim(REF_SER)) = 'P-AMD' AND ltrim(rtrim(REF_ID)) = ? AND ltrim(rtrim(ROLE_CODE__SIGN)) = ? "
							+ "AND SIGN_STATUS = 'U'";
							
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, userCode);
					pstmt.setString(2, empCodeAprv);
					pstmt.setString(3, tranId);
					pstmt.setString(4, roleCode);
					resultCnt += pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					System.out.println("Update Successfully:"+resultCnt);
					
					processInfo = processId.split(":");
					sql = "UPDATE WF_PRC_STATUS "
							+ "SET STATUS_DATE = sysdate, PROCESS_STATUS = 4, ERR_STATUS = '0', "
							+ "STATUS_REMARKS='Escalated -> Completing :"+processId+" Invoked :"+processId+"' "
							+ "WHERE ltrim(rtrim(REF_SER)) = 'P-AMD' AND REF_ID = ? AND PROCESS_ID = ? AND INSTANCE_ID = ? AND ACTIVITY_ID = ? AND PROCESS_STATUS != 3";
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					pstmt.setString(2, processInfo[0]);
					pstmt.setString(3, processInfo[1]);
					pstmt.setString(4, activityId);
					resultCnt += pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					System.out.println("Update Successfully:"+resultCnt);
					if(resultCnt > 1) 
					{
						conn.commit();
						retString = "Y";
					} 
					else 
					{
						conn.rollback();
					}
				} 
				else 
				{
					retString = "N";
				}
			} 
		}
		catch (Exception e) 
		{
			try 
			{
				conn.rollback();
			} 
			catch (SQLException e1) 
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
			System.out.println("Exeption occured");

			throw new ITMException(e);
		} 
		finally 
		{
			if (conn != null) 
			{
				try 
				{
					conn.commit();
					conn.close();
					conn = null;
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
					System.out.println("Exeption in Finally");
				}
			}
		}

		return retString;
	}
}
