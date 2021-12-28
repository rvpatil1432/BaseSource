package ibase.webitm.ejb.dis;

import ibase.scheduler.utility.interfaces.Schedule;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.MasterApplyEJB;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;

public class BatchLoadIdSpecificSch implements Schedule
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	//FileOutputStream fos1 = null;
	@Override
	public String schedule(String scheduleParamXML)throws Exception
	{
		System.out.println("BatchLoadIdSpecificSch : schedule( )");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String batchId = null;
		int count=0;
		ibase.utility.UserInfoBean userInfoBean = new ibase.utility.UserInfoBean( scheduleParamXML );
		String objName="";
		String sql="",update_sql="",retString="";
		HashMap<String, ArrayList<String>> batchIdHashMap=new HashMap<String, ArrayList<String>>();
		ArrayList<String> tempBatchIdList=null;
		ArrayList<String> batchIdList=new ArrayList<String>();
		String keyWinName="",keyStr="",distOrdId="",adjRcpId="",ditIssId="",runMode="";
		try
		{
			E12GenericUtility genericUtility= new E12GenericUtility();
			System.out.println("scheduleParamXML-----"+scheduleParamXML);
			ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();			
			Document dom=genericUtility.parseString(scheduleParamXML);
			//Condition Added By Manoj 25/01/2013 to load batch Win Name wise.
			objName=genericUtility.getColumnValue("ACTUALPARAMETER",dom);
			/*if(objName==null)
			{
				//Changed By Mahesh Patidar on 23-SEP-13[to avoid batchloading if tran_id_ref is not null]
//				query = "SELECT BATCH_ID FROM BATCHLOAD WHERE LOAD_STAT = 'N' ";
				query = "SELECT BATCH_ID FROM BATCHLOAD WHERE LOAD_STAT = 'N' AND TRAN_ID__REF IS NULL ";
				//Ended By Mahesh Patidar on 23-SEP-13[to avoid batchloading if tran_id_ref is not null]
			}
			else
			{
				//Changed By Mahesh Patidar on 23-SEP-13[to avoid batchloading if tran_id_ref is not null]
//				query = "SELECT BATCH_ID FROM BATCHLOAD WHERE LOAD_STAT = 'N' and WIN_NAME='"+objName+"'";
				query = "SELECT BATCH_ID FROM BATCHLOAD WHERE LOAD_STAT = 'N' and WIN_NAME='"+objName+"'  AND TRAN_ID__REF IS NULL ";
				//Ended By Mahesh Patidar on 23-SEP-13[to avoid batchloading if tran_id_ref is not null]
			}*/
			/*
			sql = "SELECT BATCH_ID FROM BATCHLOAD WHERE LOAD_STAT='N' AND TRAN_ID__REF IS NULL AND "+
					"BATCH_ID LIKE '117%' AND WIN_NAME ='w_wo_close' ";
			*/
			
			/**
			 * Commented by Vallabh Kadam
			 * 15/JAN/16
			 * */
//			sql = "SELECT BATCH_ID FROM BATCHLOAD WHERE LOAD_STAT='N' AND TRAN_ID__REF IS NULL AND "+
//			      "BATCH_ID LIKE '900%' AND WIN_NAME IN('w_adj_rcp','w_dist_issue')";
//			
//			System.out.println("Query executed [" + sql + "]");
//			pstmt =  conn.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//			String xtraParams = null;
//			AppConnectParm appConnect = new AppConnectParm();
//			InitialContext ctx = new InitialContext ( appConnect.getProperty() );
//			
//			while( rs.next())
//			{
//				batchId = rs.getString("BATCH_ID");
//				System.out.println("batchId>>>>>"+batchId);
//				try 
//				{
//					retString = masterApplyLocal.applyMasterTable( batchId, xtraParams, userInfoBean );
//					System.out.println("@@@@@@@@@@@ retString["+retString+"]");
//				}
//				catch (Exception e) 
//				{
//					e.printStackTrace();
//					FileOutputStream fileOutputStream = new FileOutputStream( CommonConstants.JBOSSHOME + File.separator +"LOG"+File.separator+userInfoBean.getEmpCode()+"_"+batchId+"_BatchUpload.log" );
//					byte convertStringToByte[] = e.toString().getBytes();
//					fileOutputStream.write( convertStringToByte );
//					fileOutputStream.close( );
//				}
//			}
//			rs.close();
//			rs=null;
//			pstmt.close();
//			pstmt = null;
			
			/**
			 * Commented by Vallabh Kadam
			 * 15/JAN/16
			 * */
			runMode="I";			
			String xtraParams = null;
			
			UserInfoBean userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
			xtraParams = "loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+userInfo.getSiteCode()+"~~loginEmpCode="+userInfo.getEmpCode()+"~~runMode="+runMode;					
			MasterApplyEJB masterApplyLocal = new MasterApplyEJB();
			
			/*Added update sql to change the window name by Nikhil on dated 15/07/16*/
			int upCnt= 0;
			update_sql= " UPDATE BATCHLOAD SET WIN_NAME='w_dist_order_mid' WHERE WIN_NAME='w_dist_order' " +
					    " AND LOAD_STAT='N' AND TRAN_ID__REF IS NULL AND BATCH_ID LIKE '900%' ";
			
			pstmt = conn.prepareStatement(update_sql);
			upCnt = pstmt.executeUpdate();
			if (upCnt != 0)
			{
				conn.commit();
				System.out.println("Update Successfully");
			}
			
			pstmt.close();
			pstmt = null;
			
			
			/*Ended update sql to change the window name by Nikhil on dated 15/07/16*/
			
			/*Change win_name w_dist_order_mid by Nikhil on dated 15-Jul-16*/
			sql="SELECT BATCH_ID,WIN_NAME FROM BATCHLOAD WHERE LOAD_STAT='N' AND TRAN_ID__REF IS NULL"
					+ " AND BATCH_ID LIKE '900%' AND WIN_NAME IN('w_dist_order_mid','w_adj_rcp','w_dist_issue')";
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				keyWinName=checkNull(rs.getString("WIN_NAME"));
				/**
				 * Collect batch_id
				 * for Distribution order 'w_dist_order'
				 * */
				/*Change win_name w_dist_order_mid by Nikhil on dated 15-Jul-16*/
				if(keyWinName.equalsIgnoreCase("w_dist_order_mid"))
				{
					if(batchIdHashMap.get(keyWinName)==null)
					{
						tempBatchIdList=new ArrayList<String>();
						tempBatchIdList.add(rs.getString("BATCH_ID"));
						batchIdHashMap.put(keyWinName, tempBatchIdList);
						tempBatchIdList=null;
					}
					else
					{
						tempBatchIdList=new ArrayList<String>();
						tempBatchIdList=batchIdHashMap.get(keyWinName);
						tempBatchIdList.add(rs.getString("BATCH_ID"));
						batchIdHashMap.put(keyWinName, tempBatchIdList);
						tempBatchIdList=null;
					}
				}
				/**
				 * Collect batch_id
				 * for Distribution order 'w_adj_rcp'
				 * */
				else if(keyWinName.equalsIgnoreCase("w_adj_rcp"))
				{
					if(batchIdHashMap.get(keyWinName)==null)
					{
						tempBatchIdList=new ArrayList<String>();
						tempBatchIdList.add(rs.getString("BATCH_ID"));
						batchIdHashMap.put(keyWinName, tempBatchIdList);
						tempBatchIdList=null;
					}
					else
					{
						tempBatchIdList=new ArrayList<String>();
						tempBatchIdList=batchIdHashMap.get(keyWinName);
						tempBatchIdList.add(rs.getString("BATCH_ID"));
						batchIdHashMap.put(keyWinName, tempBatchIdList);
						tempBatchIdList=null;
					}
				}
				/**
				 * Collect batch_id
				 * for Distribution order 'w_dist_issue'
				 * */
				else if(keyWinName.equalsIgnoreCase("w_dist_issue"))
				{
					if(batchIdHashMap.get(keyWinName)==null)
					{
						tempBatchIdList=new ArrayList<String>();
						tempBatchIdList.add(rs.getString("BATCH_ID"));
						batchIdHashMap.put(keyWinName, tempBatchIdList);
						tempBatchIdList=null;
					}
					else
					{
						tempBatchIdList=new ArrayList<String>();
						tempBatchIdList=batchIdHashMap.get(keyWinName);
						tempBatchIdList.add(rs.getString("BATCH_ID"));
						batchIdHashMap.put(keyWinName, tempBatchIdList);
						tempBatchIdList=null;
					}
				}
			}			
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			
			System.out.println("Batch id hash map size :- ["+batchIdHashMap.size()+"]");
			/**
			 * Check Hash map Size
			 * */
			if(batchIdHashMap.size()>0)
			{
				/**
				 * Get batch_id list
				 * according win_name 'w_dist_order'  
				 * */
				
				/*Change win_name w_dist_order_mid by Nikhil on dated 15-Jul-16*/
				System.out.println("Dist_order map size :- ["+batchIdHashMap.size()+"]");
					batchIdList=batchIdHashMap.get("w_dist_order_mid");
					if(batchIdList!=null)
					{
						System.out.println("Dist_order batch id list size :- ["+batchIdList.size()+"]");
						/**
						 * Get batch_id from
						 * batch_id list
						 * */
						for (int i = 0; i < batchIdList.size(); i++) 
						{
							batchId=batchIdList.get(i);
							System.out.println("Batch id at Distribution order :- ["+batchId+"]");
							/**
							 * Batch load of
							 * 'w_dist_order' 
							 * */
							retString = masterApplyLocal.applyMasterTable( batchId, xtraParams, userInfoBean );
							System.out.println("Retstring of batch load of Dist order :- ["+retString+"] Batch Id :- ["+batchId+"]");
							/**
							 * Get tran_id ie. distOrdId
							 * from retString 
							 * of Distribution order 
							 * */
//							if (retString.indexOf("Success") > -1)//VTEDISU
								if (retString.indexOf("VTEDISU") > -1)//VTEDISU
							{
//								String[] arrayForTranIdIssue = retString.split("<TranID>");
//								int endIndexDistOrd = arrayForTranIdIssue[1].indexOf("</TranID>");
//								distOrdId = arrayForTranIdIssue[1].substring(0, endIndexDistOrd);
								distOrdId = getGeneratedTranId(batchId,conn);
								System.out.println("Distribution order id :- ["+distOrdId+"]");
								/**
								 * Call Auto confirm
								 * component for 
								 * obj name 'dist_order' 
								 * and respective distOrdId
								 * */
								retString = confirmTran("dist_order", distOrdId, xtraParams, "", conn);
								System.out.println("Retstring after Dist order auto confirm :- ["+retString+"] Dis ordId :- ["+distOrdId+"]");
							}						
						}
						batchIdList.clear();
					}
					/**
					 * Get batch_id list
					 * according win_name 'w_adj_rcp'  
					 * */
					batchIdList=batchIdHashMap.get("w_adj_rcp");
					if(batchIdList!=null)
					{
						System.out.println("Adj_rcp batch id list size :- ["+batchIdList.size()+"]");
						/**
						 * Get batch_id from
						 * batch_id list
						 * */
						for (int i = 0; i < batchIdList.size(); i++) 
						{
							batchId=batchIdList.get(i);
							System.out.println("Batch id at Adjustment receipt :- ["+batchId+"]");
							/**
							 * Batch load of
							 * 'w_adj_rcp' 
							 * */
							retString = masterApplyLocal.applyMasterTable( batchId, xtraParams, userInfoBean );
							System.out.println("Retstring of batch load of Adj Receipt :- ["+retString+"] Batch Id :- ["+batchId+"]");
							/**
							 * Get tran_id ie. adjRcpId
							 * from retString 
							 * of Adjustment receipt 
							 * */
//							if (retString.indexOf("Success") > -1)
								if (retString.indexOf("VTEDISU") > -1)
							{
//								String[] arrayForTranIdIssue = retString.split("<TranID>");
//								int endIndexAdjRcp = arrayForTranIdIssue[1].indexOf("</TranID>");
								adjRcpId = getGeneratedTranId(batchId, conn);
								System.out.println("Adj Receipt id :- ["+adjRcpId+"]");
								/**
								 * Call Auto confirm
								 * component for 
								 * obj name 'adj_rcp' 
								 * and respective adjRcpId
								 * */
								retString = confirmTran("adj_rcp", adjRcpId, xtraParams, "", conn);
								System.out.println("Retstring after Adj Receipt auto confirm :- ["+retString+"] Adj ReceiptId :- ["+adjRcpId+"]");
							}
						}
						batchIdList.clear();
					}
					
					/**
					 * Get batch_id list
					 * according win_name 'w_dist_issue'  
					 * */
					batchIdList=batchIdHashMap.get("w_dist_issue");
					if(batchIdList!=null)
					{
						System.out.println("Dist_issue batch id list size :- ["+batchIdList.size()+"]");
						/**
						 * Get batch_id from
						 * batch_id list
						 * */
						for (int i = 0; i < batchIdList.size(); i++) 
						{
							batchId=batchIdList.get(i);
							System.out.println("Batch Id at Distribution issue :- ["+batchId+"]");
							/**
							 * Batch load of
							 * 'w_dist_issue' 
							 * */
							retString = masterApplyLocal.applyMasterTable( batchId, xtraParams, userInfoBean );
							System.out.println("Retstring of batch load of Dist Issue :- ["+retString+"] Batch Id :- ["+batchId+"]");
							/**
							 * Get tran_id ie. ditIssId
							 * from retString 
							 * of Distribution issue 
							 * */
//							if (retString.indexOf("Success") > -1)
								if (retString.indexOf("VTEDISU") > -1)
							{
//								String[] arrayForTranIdIssue = retString.split("<TranID>");
//								int endIndexDistIss = arrayForTranIdIssue[1].indexOf("</TranID>");
								ditIssId = getGeneratedTranId(batchId, conn);
								System.out.println("Dist Issue id :- ["+ditIssId+"]");
								/**
								 * Call Auto confirm
								 * component for 
								 * obj name 'dist_issue' 
								 * and respective ditIssId
								 * */
								retString = confirmTran("dist_issue", ditIssId, xtraParams, "", conn);
								System.out.println("Retstring after Dist Issue auto confirm :- ["+retString+"] Dist IssueId :- ["+ditIssId+"]");
							}						
						}
						batchIdList.clear();
					}
			}
		}
		
		
		catch(Exception e)
		{
			conn.rollback();
			System.out.println(">>>>>>>>>>>>>BatchLoadIdSpecificSch In catch:");
			e.printStackTrace();
			System.out.println(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			if(conn != null)
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
		return retString;

	}
	private String getGeneratedTranId(String batchId, Connection conn) throws ITMException
    {
		String sql="",tranId="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try
        {
	        sql="select tran_id__ref from batchload where batch_id=?";
	        pstmt=conn.prepareStatement(sql);
	        pstmt.setString(1, batchId);
	        rs=pstmt.executeQuery();
	        if(rs.next())
	        {
	        	tranId=checkNull(rs.getString("tran_id__ref"));
	        }
	        pstmt.close();
	        pstmt=null;
	        rs.close();
	        rs=null;
	        
        } catch (SQLException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
        }
		
		
	    // TODO Auto-generated method stub
	    return tranId;
    }
	private String checkNull(String checkStr)
    {
	    // TODO Auto-generated method stub
		if(checkStr==null || checkStr.trim().length()==0)
		{
			checkStr="";
		}
	    return checkStr;
    }
	public String confirmTran(String businessObj, String tranId, String xtraParams, String forceFlag, Connection conn) throws ITMException
	{
		String methodName = "",compName = "",retString = "",serviceCode = "",serviceURI = "",actionURI = "",sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, businessObj);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("serviceCode = " + serviceCode + " compName " + compName);

			sql = "SELECT SERVICE_URI,METHOD_NAME FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, serviceCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				methodName = rs.getString("METHOD_NAME");
				serviceURI = rs.getString("SERVICE_URI");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			actionURI = "http://NvoServiceurl.org/" + methodName;
			System.out.println("serviceURI = " + serviceURI + " compName = " + compName);

			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranId);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String("");

			// System.out.println("@@@@@@@@@@loginEmpCode:"
			// +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);

			retString = (String) call.invoke(aobj);

			System.out.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>[" + retString + "]");

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{

				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				/*
				 * if( conn != null ){ conn.close(); conn = null; }
				 */
			} catch (Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try
				{
					conn.rollback();

				} catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
				}
		}
		return retString;
	}
	
	@Override
	public String schedule(HashMap arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String schedulePriority(String arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
