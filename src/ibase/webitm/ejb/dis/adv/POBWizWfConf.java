/********************************************************
	Title : POBWizWfConf (D15FSUN007)
	Date  : 07/10/15
	Author: Sagar Mane

********************************************************/
package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.utility.ITMException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class POBWizWfConf 
{
	
	//Changes made by Jaffar S. for multi tenancy[Userinfo] on 13-11-18 [Start]
	public String confirm(String tranId, String empCodeAprv, String xmlDataAll, String processId, String keyFlag)throws RemoteException, ITMException
	{

		String userInfoStr = "";
		String errString = "";
		try
		{
			userInfoStr = confirm(tranId, empCodeAprv, xmlDataAll, processId, keyFlag, userInfoStr);
			System.out.println("userInfoStr of confirm::::: " +userInfoStr);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [POBWizWfConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
		
	
	}
	//Changes made by Jaffar S. for multi tenancy[Userinfo] on 13-11-18 [End]
	
	public String confirm(String tranId, String empCodeAprv, String xmlDataAll, String processId, String keyFlag, String userInfoStr)
			throws RemoteException, ITMException 
	{
		System.out.println(">>>>> POBWizWfConf confirm called <<<<<");
		System.out.println(">>> POBWizWfConf Parameters tranId:"+tranId);
		System.out.println(">>> POBWizWfConf Parameters empCodeAprv:"+ empCodeAprv);
		System.out.println(">>> POBWizWfConf Parameters processId:"+ processId);
		System.out.println(">>> POBWizWfConf confirm xmlDataAll:"+ xmlDataAll);
		System.out.println(">>> POBWizWfConf confirm keyFlag:"+ keyFlag);
		
		String sql = "";
		String loginCode = "";
		String loginSiteCode = "";
		String loginEmpCode = "", termId="SYSTEM";
		String xtraParams="";
		String processInfo[] = null;
		String pobProcId="",instanceId="",activityId="",seqId="";
		String suHeadDiscStr= "",clusterHeadDiscStr="";
		String retString = "";
		int pobHdrUpdCnt=0;
		double maxDiscount=0.0, suHeadDiscount=0.0, clusterHeadDisc=0.0,countHeadDisc=0.0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();			
		POBWizConf pobWizConfLocal=null;
		try 
		{
			//Commented and changes done for passing UserInfo by Jaffar S. on 12/11/18 [Start]
			/*conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			connDriver = null;
			conn.setAutoCommit(false);*/
			
			System.out.println("Inside Try block of POBWizWfConf.confirm()");
			System.out.println("Inside POBWizWfConf confirm section: "+userInfoStr);
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
	    	String transDB = userInfo.getTransDB();
	    	System.out.println("get TransDB connection in POBWizWfConf : "+transDB);
	    	
		    if (transDB != null && transDB.trim().length() > 0)
		    {
		        conn = connDriver.getConnectDB(transDB);
		    }
		    else
		    {
		    	conn = connDriver.getConnectDB("DriverITM");
		    }
		    connDriver = null;
			conn.setAutoCommit(false);
			
			//Commented and changes done for passing UserInfo by Jaffar S. on 12/11/18 [End]
			
			DistCommon distCommon= new DistCommon();
			
			if(empCodeAprv!=null && empCodeAprv.trim().length() >0)
			{
				empCodeAprv= empCodeAprv.trim();
			}
			sql = " select code from users where emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCodeAprv);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				loginCode = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println(">>>>from users loginCode:"+loginCode);
			sql = " select site_code from pob_hdr where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				loginSiteCode = rs.getString(1);
			}
			loginSiteCode = loginSiteCode == null ? "" : loginSiteCode.trim();
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			loginCode = loginCode == null ? "" : loginCode.trim();
			xtraParams = "loginCode=" + loginCode + "~~" + "loginSiteCode=" + loginSiteCode + "~~" + "loginEmpCode=" + empCodeAprv + "~~" + "termId=" + termId;

			System.out.println(">>>Before calling confirm xtraParams:"+xtraParams);
			
			System.out.println(">>>Before Check keyFlag:"+ keyFlag);
			if("POBWFUPDATE".equalsIgnoreCase(keyFlag)) 
			{
				// Add this condition if Request is Revert then wf_status will update as 'O' Open.
				System.out.println(">>>>Check POBWFUPDATE for update workflow status as Open");
				if(tranId!= null && tranId.trim().length() > 0)
				{
					sql= "update pob_hdr set wf_status='O' where tran_id= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					pobHdrUpdCnt=pstmt.executeUpdate();
					pstmt.close();
					pstmt= null;
					System.out.println(">>>pobHdrUpdCnt:"+pobHdrUpdCnt);
					if(pobHdrUpdCnt > 0)
					{
						conn.commit();
						System.out.println("Transaction Commited Successfully");
						retString = "Y";
					}
				}
			}
			else if("POBREJECT".equalsIgnoreCase(keyFlag))
			{
				// Add this condition if Request is Rejected then wf_status will update as 'R' Rejected
				System.out.println(">>>>Check POBREJECT for update workflow status as Rejected");
				if(tranId!= null && tranId.trim().length() > 0)
				{
					sql= "update pob_hdr set wf_status='R' where tran_id= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					pobHdrUpdCnt=pstmt.executeUpdate();
					pstmt.close();
					pstmt= null;
					System.out.println(">>>pobHdrUpdCnt:"+pobHdrUpdCnt);
					if(pobHdrUpdCnt > 0)
					{
						conn.commit();
						System.out.println("Transaction Commited Successfully");
						retString = "Y";
					}
				}
			}
			else if("POBCONFIRM".equalsIgnoreCase(keyFlag))
			{
				// Add this condition if Request is Approved then POB Confirmation Will call and Approved mail will be sent
				System.out.println(">>>>Check POBCONFIRM for Confirmation");
				pobWizConfLocal = new POBWizConf();
				//retString = pobWizConfLocal.pobConfirm(tranId, xtraParams, "Y");
				retString = pobWizConfLocal.pobConfirm(tranId, xtraParams, "Y",userInfoStr);
				System.out.println(">>>>In Check POBWizWfConf return from confirm:"  + retString);
				//if((errString != null ) &&  errString.indexOf("CONFSUCCES") > -1)
				if(retString!=null && retString.indexOf("VTCNFSUCC") > -1) 
				{
					System.out.println(">>>POBWizConf Transaction confirm Successfull");
					retString = "Y";
				} 
			}
			else if("POBWFCON".equalsIgnoreCase(keyFlag))
			{
				// Add this condition for check the Maximum Discount in POB_DET table  and Check (THIRD,FOURTH,FIFTH) Approver Discount Limit from disparm table for defined variable.
				System.out.println(">>>>Check POBWFCON for Maximum Discount");
				sql = " select max(discount) as discount from pob_det where tran_id= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					maxDiscount = rs.getDouble("discount");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println(">>Get pob_det maxDiscount:"+ maxDiscount);
				if(processId!= null && processId.trim().length() > 0)
				{
					processInfo= processId.split(":");
					pobProcId= processInfo[0];
					instanceId= processInfo[1];
					activityId= processInfo[2];
					seqId= processInfo[3];
				}
				System.out.println(">>>Current processInfo:"+ processInfo);
				System.out.println(">>>Current pobProcId:"+ pobProcId);
				System.out.println(">>>Current instanceId:"+ instanceId);
				System.out.println(">>>Current activityId:"+ activityId);
				System.out.println(">>>Current seqId:"+ seqId);
				
				if(activityId!= null && activityId.trim().length() > 0)
				{
					suHeadDiscStr= checkNull(distCommon.getDisparams("999999", "SUHEAD_DISCOUNT", conn));
					clusterHeadDiscStr= checkNull(distCommon.getDisparams("999999", "CLUSTERHD_DISCOUNT", conn));
					//String countHeadDiscStr= checkNull(distCommon.getDisparams("999999", "COUNTRYHD_DISCOUNT", conn));
					if("THIRD_CON".equalsIgnoreCase(activityId.trim()))
					{
						System.out.println(">>>Checking Max Discount for THIRD_CON(SU Head) :");
						if(suHeadDiscStr.trim().length() > 0)
						{
							if(checkIsNumber(suHeadDiscStr))
							{
								suHeadDiscount=Double.parseDouble(suHeadDiscStr);
								System.out.println(">>Check THIRD_CON suHeadDiscount:"+suHeadDiscount);
								System.out.println(">>Check THIRD_CON maxDiscount:"+maxDiscount);
								if(maxDiscount <= suHeadDiscount)
								{
									retString="Y";
								}
								else
								{
									retString="N";
								}
							}
						}
					}
					else if("FORTH_CON".equalsIgnoreCase(activityId.trim()))
					{
						System.out.println(">>>Checking Max Discount for FRTH_SIGN(Cluster Head):");
						if(clusterHeadDiscStr.trim().length() > 0)
						{
							if(checkIsNumber(clusterHeadDiscStr))
							{
								clusterHeadDisc= Double.parseDouble(clusterHeadDiscStr);
								suHeadDiscount=Double.parseDouble(suHeadDiscStr);
								System.out.println(">>Check FORTH_CON maxDiscount:"+suHeadDiscount);
								System.out.println(">>Check FORTH_CON suHeadDiscount:"+suHeadDiscount);
								System.out.println(">>Check FORTH_CON clusterHeadDisc:"+clusterHeadDisc);
								
								if(maxDiscount > suHeadDiscount && maxDiscount <= clusterHeadDisc )
								{
									retString="Y";
								}
								else
								{
									retString="N";
								}
							}
						}
						System.out.println(">>>>FORTH_CON return retString:"+retString);
					}
					else if("FIFTH_CON".equalsIgnoreCase(activityId.trim()))
					{
						System.out.println(">>>Checking Max Discount for FIFTH_CON(Country Head):");
						retString="Y";
					}
				}
			}
		} 
		catch (Exception e)
		{
			try 
			{
				conn.rollback();
			} catch (SQLException e1) 
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
					//conn.commit();
					conn.close();
					conn = null;
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
					System.out.println("Exeption in Finaly");
				}
			}
		}
		System.out.println(">>>Before return final retString:"+retString);
		return retString;
	}
	
	private boolean checkIsNumber(String headDiscountStr)
	{
		char disChar;
		int charIndex=0;
		boolean isNumber= true;
		headDiscountStr= headDiscountStr.trim();
		System.out.println(">>>noOfMonthStr.length():"+headDiscountStr.length());
		for(charIndex=0; charIndex < headDiscountStr.length(); charIndex++)
		{
			disChar= headDiscountStr.charAt(charIndex);
			System.out.println(">>>disChar:"+disChar);
			if(disChar >='0' && disChar <='9' ||  disChar==46 )
			{
				System.out.println(">>>Is number");
			}
			else
			{
				System.out.println(">>>Not number");
				isNumber=false;
			}
		}
		System.out.println(">>retun isNumber:"+isNumber);
		return isNumber;
	}
	private String checkNull(String inputDisc) 
	{
		if (inputDisc == null || inputDisc.trim().equalsIgnoreCase("NULLFOUND"))
		{
			inputDisc="";
		}
		return inputDisc;
	}
}
