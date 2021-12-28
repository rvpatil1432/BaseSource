package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;

import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

public class FailedLogicDetails extends ValidatorEJB
{
	
	public FailedLogicDetails(UserInfoBean userInfoBean)
	{
		setUserInfo(userInfoBean);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getFailedLogicDetails(String saleOrder)throws ITMException, SQLException
	{
			String sql = "";
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String failedInfo = "";
			String crPolicy = "";
			String descr = "";
			ArrayList<HashMap> failedLogictList = new ArrayList<HashMap>();
			JSONArray array = null;
			JSONObject result = null;
			HashMap argDataMap = null;
			HashMap argDataMapRtn = new HashMap();
			
			System.out.println("Inside getFailedLogicDetails..........##");
			try
			{
				conn = getConnection();
				sql = "select failed_info,cr_policy,descr from business_logic_check where sale_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, checkNull(saleOrder));
				rs = pstmt.executeQuery();
				int cnt = 0;
				while (rs.next())
				{
					argDataMap = new HashMap();
					cnt++;
					failedInfo = rs.getString("failed_info");
					crPolicy = checkNull(rs.getString("cr_policy"));
					descr = checkNull(rs.getString("descr"));
					System.out.println("failedInfo......$$1" + failedInfo);
					array = new JSONArray(failedInfo);
					System.out.println("array......##" + array);
					argDataMap.put("DATA", array);
					argDataMap.put("cr_policy", crPolicy);
					argDataMap.put("descr", descr);
					failedLogictList.add(argDataMap);
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				System.out.println("argDataMap......111" + argDataMap);
				System.out.println("failedLogictList......!!!" + failedLogictList);
				System.out.println("cnt...@@" + cnt);
			} catch (Exception e) {
				System.out.println(" Exception in FailedLogicDetails.getFailedLogicDetails()[" + e.getMessage() + "]");
				e.printStackTrace();
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					if (conn != null) {
						conn.close();
						conn = null;
					}
				} catch (Exception e) {
					System.out.println("Exception in FailedLogicDetails.getFailedLogicDetails()");
					e.printStackTrace();
				}
			}
			return failedLogictList;
		}
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
}
