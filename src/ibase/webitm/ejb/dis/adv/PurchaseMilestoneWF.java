package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PurchaseMilestoneWF 
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
			int signcount = 0,penlatycnt = 0;
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
				list.add("INIT_SIGN");
				list.add("FRST_SIGN");
				list.add("THRD_SIGN");
				list.add("PNINIT_SIGN");
				list.add("PNFRST_SIGN");
				list.add("PNTHRD_SIGN");

				sql = "select previous_activity, seq_id "
						+"from wf_prc_status "
						+"where ltrim(rtrim(ref_ser)) = ? and ltrim(rtrim(ref_id)) = ? "
						+"and activity_id = ? and seq_id = ? and instance_id = ?";
						
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "U-REC");
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
					pstmt.setString(1, "U-REC");
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
				if(activityId.equalsIgnoreCase("THRD_SIGN") || activityId.equalsIgnoreCase("PNTHRD_SIGN"))
				{
					activityId = "NOTHERACT";
				}
				retString = activityId;
			}
			else if("CHKPURMILA".equalsIgnoreCase(keyFlag))
			{
				System.out.println("in CHKPURMILA");
				//select count(*) from tarodev.wf_prc_status where ref_id = '0000000332' and ref_ser = 'U-REC' and activity_id = 'SCND_SIGN' ;
				sql = "select count(*) "
						+"from wf_prc_status "
						+"where ltrim(rtrim(ref_ser)) = ? and ltrim(rtrim(ref_id)) = ? "
						+"and activity_id = ? and instance_id = ?";
						
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "U-REC");
				pstmt.setString(2, tranId);
				pstmt.setString(3, "SCND_SIGN");
				pstmt.setString(4, processInfo[1]);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					signcount = rs.getInt(1);
					if(signcount>0)
					{
						return "N";
					}
					else
					{
						return "Y";
					}
				}
				
				System.out.println("after check sign");
	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if("CHKPNLTYA".equalsIgnoreCase(keyFlag))
			{
				System.out.println("in CHKPNLTYA");
				
				sql = "select count(*) "
						+"from wf_prc_status "
						+"where ltrim(rtrim(ref_ser)) = ? and ltrim(rtrim(ref_id)) = ? "
						+"and activity_id = ? and instance_id = ?";
						
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "U-REC");
				pstmt.setString(2, tranId);
				pstmt.setString(3, "PNSCND_SIGN");
				pstmt.setString(4, processInfo[1]);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					penlatycnt = rs.getInt(1);
					if(penlatycnt>0)
					{
						return "N";
					}
					else
					{
						return "Y";
					}
				}
				System.out.println("after check penlaty");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
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
