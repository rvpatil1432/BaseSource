package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class CustStockGWTCancel extends ActionHandlerEJB implements CustStockGWTCancelLocal,CustStockGWTCancelRemote{
	int updateCount=0;
	@Override
	public String cancel(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		
		
		// TODO Auto-generated method stub
		System.out.println("Inside Sales transaction  Cancel() method");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", sql1 = "",loginSiteCode="",countryCode="",prdCode="",itemSer="";
		ConnDriver connDriver = null;		
		connDriver = null;
		String errString = "",isPrdClosed="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		ValidatorEJB validatorEJB = null;
		int cnt=0;
		Timestamp sysDate = null;
		Timestamp dbSysDate = null;
		E12GenericUtility genericUtility=new E12GenericUtility();
		String userId="",userinfo="";
		try{
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			//connDriver = null;
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
			userinfo=getUserInfo(xtraParams);
			System.out.println("userinfo>>>suppejb>>"+userinfo);
			System.out.println("userinfo>>>suppejbneft >>"+userinfo);
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));

			//loginCode=getValueFromXTRAPARAMS(xtraParams);
			UserInfoBean userInfoBean = new UserInfoBean(userinfo);
			if (userInfoBean.getTransDB().length() > 0)
			{
			conn = connDriver.getConnectDB(userInfoBean.getTransDB());
			}
			else
			{
			try
			{
			conn = getConnection();
			}
			catch(Exception e)
			{
			conn = connDriver.getConnectDB("DriverITM");
			}
			}
		 
		 
		
		 
		 
	
			conn.setAutoCommit(false);
	
	        
	        System.out.println("Tran Id found by transaction :- ["+tranID+"]");
	        sql = " select  count(*) as cnt from cust_stock where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID.trim());
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				cnt  = rs.getInt("cnt");
			}
			System.out.println("cntof>>"+cnt);
			rs.close();
			rs = null;			
			pstmt.close();
			pstmt=null;
			/*if(cnt > 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VTSLRACNFM","","",conn);
			    return errString;
			}*/			
//			else
//			{
				/**
				 * Hence the Transaction is not Accepted
				 * Cancel the transaction
				 * Set STATUS='X'
				 * */
			
				System.out.println("Tran Id Found by transaction :- ["+tranID+"]");
		        sql = " select  count(*) as cnt from cust_stock where tran_id = ? and STATUS = 'X' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID.trim());
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					cnt  = rs.getInt("cnt");
				}
				System.out.println("cntof X>>"+cnt);
				rs.close();
				rs = null;			
				pstmt.close();
				pstmt=null;
				
				sql= "select count_code from state where " +
						"state_code in (select state_code from site where site_code=?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, loginSiteCode );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					countryCode = checkNull(rs.getString("count_code")).trim();
					System.out.println("countryCode >>> :"+countryCode);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("Tran Id Found by transaction 111:- ["+tranID+"]");
		        sql = " select   PRD_CODE,ITEM_SER from cust_stock where tran_id = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID.trim());
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					prdCode  = checkNull(rs.getString("PRD_CODE"));
					itemSer=checkNull(rs.getString("ITEM_SER"));
				}
				System.out.println("prdCode X>>"+prdCode);
				System.out.println("itemSer X>>"+itemSer);

				rs.close();
				rs = null;			
				pstmt.close();
				pstmt=null;
				
				
				if(cnt > 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VTSLRACNCL","","",conn);
				    return errString;
				}
				else
				{				
				/*
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			    String sysDateStr = sdf.format(currentDate.getTime());
			    System.out.println(">>>>>>>Now sysDateStr :=>  " + sysDateStr);	
			    sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			    System.out.println(">>>>>>>>sysDate:"+sysDate);	
				*/
					//changes by sangita start

					sql = "select count(*) from period_appl a,period_tbl b " +
							"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
							" AND b.prd_code = ? " +
							"and b.prd_tblno=? " +
							"AND case when a.type is null then 'X' else a.type end='S' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,prdCode.trim());
					pstmt.setString(2,countryCode+"_"+itemSer.trim());
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (cnt == 0)
					{
						System.out.println("Error :Period not exist in period_tbl master ");
						/*errCode = "VMINVPRDTB";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						*/
						errString = itmDBAccessEJB.getErrorString("","VMINVPRDTB","","",conn);
					    return errString;
					}
					else
					{
						sql = "select b.FR_DATE as FR_DATE,b.TO_DATE as TO_DATE " +
								",b.entry_start_dt as entry_start_dt" +
								",b.entry_end_dt as entry_end_dt ,b.prd_closed" +
								" from period_appl a,period_tbl b " +
								"where a.ref_code=a.prd_tblno and a.prd_tblno=b.prd_tblno " +
								" AND b.prd_code = ? " +
								"and b.prd_tblno=? " +
								"AND case when a.type is null then 'X' else a.type end='S' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,prdCode.trim());
						pstmt.setString(2,countryCode+"_"+itemSer.trim());	
						rs = pstmt.executeQuery();
						if(rs.next())
						{
						isPrdClosed = rs.getString("prd_closed");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if("Y".equalsIgnoreCase(isPrdClosed))
						{
							/*errCode = "VMPRDCLOSE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
							
*/	
							errString = itmDBAccessEJB.getErrorString("","VMPRDCLOSE","","",conn);
						    return errString;
						    }
					}
					//changes by sangita end
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				    String sysDateStr = sdf.format(currentDate.getTime());
				    System.out.println(">>>>>>>Now sysDateStr :=>  " + sysDateStr);	
				    sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				sql = "UPDATE cust_stock set status='X' ,CANCEL_DATE= ? where tran_id= ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, tranID.trim());
			   // pstmt.setDate(2, (java.sql.Date) currDate);
				//pstmt.setTimestamp(2, sysDate);

				updateCount = pstmt.executeUpdate();				
				pstmt.close();
				pstmt = null;				
				}
				if(updateCount>0)
				{
					if(errString==null || errString.trim().length()<=0)
					{
						errString = itmDBAccessEJB.getErrorString("","VTSCNLSUCS","","",conn);
					}					
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTSCNLFAIL	","","",conn);
				}
//			
	        
		}
		
		catch (Exception e)
        {
	        e.printStackTrace();
        }
		finally 
		{
			try
			{
				if(!conn.isClosed())
				{
					if(updateCount>0)
					{
						conn.commit();
					}
					else
					{
						conn.rollback();
					}
				conn.close();
				}				
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		 return errString;
	}
	public String getUserInfo( String xtraParams )throws ITMException
	{
		StringBuffer userInfoStr = new StringBuffer();
		String userId = "";
		String loginEmpCode = "";
		String loginSiteCode = "";
		String entityCode = "";
		String profileId = "";
		String userType = "";
		String charEnc = "";
		String chgTerm = "";
		String transDb="";
		String sql = "";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try
		{
			
			
			
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility = new E12GenericUtility();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			entityCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"entityCode");
			profileId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"profileId");
			userType = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userType");
			//charEnc = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"charEnc");
			//Modified by Hemlata on[25/09/2014][To get character encoding ][Start]
			charEnc = CommonConstants.ENCODING;
			System.out.println("charEnc......"+charEnc);
			//Modified by Hemlata on[25/09/2014][To get character encoding ][Start]
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
			UserInfoBean userInfo = new UserInfoBean();
			DBAccessEJB dbAccessEJB = new DBAccessEJB();
			userInfo = dbAccessEJB.createUserInfo(userId);
			transDb = userInfo.getTransDB();
			System.out.println("transDb::::@@@ " + transDb);
			

			userInfoStr.append("<UserInfo>");
			userInfoStr.append("<loginCode>").append("<![CDATA["+userId+"]]>").append("</loginCode>\r\n");
			userInfoStr.append("<empCode>").append("<![CDATA["+loginEmpCode+"]]>").append("</empCode>\r\n");
			userInfoStr.append("<siteCode>").append("<![CDATA["+loginSiteCode+"]]>").append("</siteCode>\r\n");
			userInfoStr.append("<entityCode>").append("<![CDATA["+entityCode+"]]>").append("</entityCode>\r\n");
			userInfoStr.append("<profileId>").append("<![CDATA["+profileId+"]]>").append("</profileId>\r\n");
			userInfoStr.append("<userType>").append("<![CDATA["+userType+"]]>").append("</userType>\r\n");
			userInfoStr.append("<charEnc>").append("<![CDATA["+charEnc+"]]>").append("</charEnc>\r\n");
			userInfoStr.append("<remoteHost>").append("<![CDATA["+chgTerm+"]]>").append("</remoteHost>\r\n");
			userInfoStr.append("<transDB>").append("<![CDATA["+transDb+"]]>").append("</transDB>\r\n");

			userInfoStr.append("</UserInfo>");
		}
		catch ( Exception e )
		{
			throw new ITMException(e);
		}
		return userInfoStr.toString();
	}
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}}
