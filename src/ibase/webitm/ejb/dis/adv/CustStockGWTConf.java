/********************************************************
	Title : CustStockGWTConf[D15ESUN013]
	Date  : 27/10/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;


import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import java.util.Calendar;

@Stateless
public class CustStockGWTConf extends ActionHandlerEJB implements CustStockGWTConfLocal, CustStockGWTConfRemote
{

	public String submit(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>CustStockGWTConf submit called>>>>>>>>>>>>>>>>>>>");
		String sql = "",status="",confirmed="",sql1="",sql2="";
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		String errString = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String missingInserted="",transitUpdate="",loginEmpCode="",userId="",empCode="";
		String transitFlag="",siteCode="",custCode="",totclValue="",totSalesValue="",tranType="",stockMode="";
		String lineNo="",invoiceId="",itemCode="";
		double transitQty=0.0,invoiceQty=0.0,clStock=0.0;
		int cnt = 0,cnfCnt=0;
		Timestamp currDate = null,tranDate=null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		try
		{
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("loginEmpCode>>>>"+loginEmpCode+">>>userId>>>"+userId);
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			currDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			System.out.println("currDate>>>>"+currDate);
			ConnDriver connDriver = null;
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);

			sql="select emp_code from users where code=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				empCode = rs.getString("emp_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("empCode>>>>>"+empCode);
			
			if (tranId != null && tranId.trim().length() > 0)
			{
				System.out.println("tranId>>>["+tranId+"]");
				sql = "	select status,confirmed,missing_inserted, transit_update from  cust_stock where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					status = rs.getString("status");
					confirmed = rs.getString("confirmed");
					missingInserted = rs.getString("missing_inserted");
					transitUpdate = rs.getString("transit_update");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("status>>>>>>>>"+status);
				if(!"Y".equalsIgnoreCase(confirmed) && !"S".equalsIgnoreCase(status))
				{
					//start added by chandrashekar on 31-dec-2015
					
					if(!"Y".equalsIgnoreCase(missingInserted))
					{
						errString = itmDBAccessLocal.getErrorString("", "VTMISSITEM", "","",conn);
						return errString;
					}
					
					if(transitUpdate == null || transitUpdate.trim().length()==0 || "N".equalsIgnoreCase(transitUpdate))
					{
						sql = "	select transit_flag, site_code, cust_code, tran_date, tot_cl_value, tot_sales_value,tran_type" +
								" from  cust_stock where tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							transitFlag = rs.getString("transit_flag");
							siteCode = rs.getString("site_code");
							custCode = rs.getString("cust_code");
							tranDate = rs.getTimestamp("tran_date");
							totclValue = rs.getString("tot_cl_value");
							totSalesValue = rs.getString("tot_sales_value");
							tranType = rs.getString("tran_type");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	
						
						sql = " SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'CUST_STOCK_MODE'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							stockMode = checkNull(rs.getString("VAR_VALUE"));
							System.out.println("stockMode :" + stockMode);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(stockMode == null || "NULLFOUND".equalsIgnoreCase(stockMode)|| stockMode.trim().length()==0)
						{
							stockMode="S";
						}
						if(transitFlag == null || transitFlag.trim().length()==0)
						{
							transitFlag="N";
						}
						if("S".equalsIgnoreCase(tranType))
						{
							sql = "Select line_no, item_code, transit_qty, cl_stock, sales,op_stock, purc_rcp," +
									" adj_qty, purc_ret, adhoc_repl_qty, unit	" +
									"From cust_stock_det Where tran_id = ? " +
									"and case when transit_qty is null then 0 else transit_qty end = 0";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							while (rs.next()) 
							{
								lineNo = rs.getString("line_no");
								clStock = rs.getDouble("cl_stock");
								transitQty = rs.getDouble("transit_qty");
								itemCode = rs.getString("item_code");
								
								System.out.println("itemcode@@@@@@>>"+itemCode);
								sql1 = "update cust_stock_det set original_cl_stock = ? "
										+ "where tran_id 	= ? and 	line_no	= ?";
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setDouble(1, clStock);
								pstmt1.setString(2, tranId);
								pstmt1.setString(3, lineNo);
								cnt = pstmt1.executeUpdate();
								pstmt1.close();
								pstmt1 = null;

								if (transitQty == 0)
								{
									sql1 = "Select invoice_id From cust_stock_inv Where tran_id = ? and dlv_flg = 'N'";
									pstmt1 = conn.prepareStatement(sql1);
									pstmt1.setString(1, tranId);
									rs1 = pstmt1.executeQuery();
									while (rs1.next())
									{
										invoiceId = checkNull(rs1.getString("invoice_id"));
										System.out.println("invoiceId :" + invoiceId);

										sql2 = "Select sum(quantity__stduom) as invoice_qty From 	 " 
										+ "invdet	Where  invoice_id = ? " + "and 	 item_code  = ? ";
										pstmt2 = conn.prepareStatement(sql2);
										pstmt2.setString(1, invoiceId);
										pstmt2.setString(2, itemCode);
										rs2 = pstmt2.executeQuery();
										if (rs2.next())
										{
											invoiceQty = rs2.getDouble("invoice_qty");
											System.out.println("invoiceQty :" + invoiceQty);
										}
										rs2.close();
										rs2 = null;
										pstmt2.close();
										pstmt2 = null;

										transitQty = transitQty + invoiceQty;
									}// invoice loop
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								}
								if (transitQty != 0)
								{
									sql = "update cust_stock_det set transit_qty 	=  ? " 
											+ " where tran_id 	= ?	" 
											+ " and	line_no = ? ";
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setDouble(1, transitQty);
									pstmt1.setString(2, tranId);
									pstmt1.setString(3, lineNo);
									cnt = pstmt1.executeUpdate();
									pstmt1.close();
									pstmt1 = null;
								}

							}//cust_stock_det loop
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							sql = "update cust_stock	set transit_upd_flag  = 'Y'	where tran_id = ? ";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, tranId);
							cnt = pstmt1.executeUpdate();
							pstmt1.close();
							pstmt1 = null;
							
						}
						
					}
					sql = " update cust_stock set confirmed = 'Y', conf_date = ?, emp_code__aprv = ?,status = 'S' " +
							" where tran_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, currDate);
					//pstmt.setString(2, loginEmpCode);//empCode
					pstmt.setString(2, empCode);
					pstmt.setString(3, tranId);
					cnfCnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					if (cnfCnt>0) 
					{	
						conn.commit();
						errString = itmDBAccessLocal.getErrorString("", "VTSUBM1", "","",conn);
					}
					//End added by chandrashekar on 31-dec-2015
					/*
					
					methodName = "gbf_post";
					actionURI = "http://NvoServiceurl.org/" + methodName;

					sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = 'cust_stock' AND EVENT_CODE = 'pre_confirm' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						serviceCode = rs.getString("SERVICE_CODE");
						compName = rs.getString("COMP_NAME");
					}
					System.out.println(">>>cust stock confirmation serviceCode = " + serviceCode + " compName " + compName);
					sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, serviceCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						serviceURI = rs.getString("SERVICE_URI");
					}
					System.out.println(">>>cust stock confirmation serviceURI = " + serviceURI + " compName = " + compName);
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
					aobj[3] = new String(forcedFlag);

					System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
					call.setReturnType(XMLType.XSD_STRING);
					errString = (String) call.invoke(aobj);

					System.out.println(">>>>Confirm Complete Return string from NVO is:==>[" + errString + "]");
					if((errString != null ) && (errString.indexOf("VTSUCC1") > -1))
					{
						sql = " update cust_stock set status = 'S' where tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						if (cnt > 0)
						{
							errString = itmDBAccessLocal.getErrorString("", "VTSUBM1", "","",conn);
							conn.commit();

						}
					}*///commented by chandrashekar on 04-01-2016
				}else
				{
					errString = itmDBAccessLocal.getErrorString("", "VTINVSUB2", "","",conn);
				}
				}
			
			//}

			// end if errstrng
		} catch (Exception e)
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if(conn != null && !conn.isClosed())
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
			} catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		return errString;

	}
	public String open(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>CustStockGWTConf open called>>>>>>>>>>>>>>>>>>>");
		String sql = "",status="",confirmed="";
		Connection conn = null;
		PreparedStatement pstmt = null;
		String errString = null;
		ResultSet rs = null;
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
	    String custCode="";
	    String itemSer="";
	    String maxTranId="";
		int cnt = 0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{

			ConnDriver connDriver = null;
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			

			
			
			if (tranId != null && tranId.trim().length() > 0)
			{
				System.out.println("tranId>>>[" + tranId + "]");
				sql = "	select status,cust_code,item_ser  from  cust_stock where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					status = checkNull(rs.getString("status"));
					custCode=checkNull(rs.getString("cust_code"));
					itemSer=checkNull(rs.getString("item_ser"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("status>>>>>>>>"+status);
				System.out.println("cust_code>>>>>>>"+custCode);

				//sql = "	select max(tran_id) as maxTran  from  cust_stock where cust_code = ? and item_ser= ? and status in ('S','X')";
				sql="select max(tran_id) as maxTran  from  cust_stock where cust_code = ?  and item_ser= ? and status in ('S','X') "+
						" and tran_date=(select max(tran_date) from cust_stock  where cust_code = ?  and item_ser= ? and status in ('S','X') )";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemSer);
				pstmt.setString(3, custCode);
				pstmt.setString(4, itemSer);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					//status = checkNull(rs.getString("status"));
					maxTranId=checkNull(rs.getString("maxTran"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("maxTranId>>>>>>>"+maxTranId);

				sql = "	select status  from  cust_stock where cust_code = ? and tran_id=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, maxTranId);

				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					status = checkNull(rs.getString("status"));
					//maxTranId=checkNull(rs.getString("tran_id"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("status1111111g>>>>>>>"+status);

				// added by adnan cancel transaction also reopen [12/01/21]
				if ("S".equalsIgnoreCase(status) || "X".equalsIgnoreCase(status) )
				{
					System.out.println("@adnan@@@"+status);
					/*
					 * sql = " select max(tran_id) as tran_id  from cust_stock where  cust_code=? ";
					 * pstmt=conn.prepareStatement(sql); pstmt.setString(1, custCode);
					 * rs=pstmt.executeQuery(); if(rs.next()) {
					 * maxTranId=checkNull(rs.getString("tran_id"));
					 * 
					 * }
					 */

					sql = " update cust_stock set status = 'O',confirmed='N', conf_date='' where tran_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, maxTranId);
					cnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					if (cnt > 0)
					{
						errString = itmDBAccessLocal.getErrorString("", "VTSTAOPEN", "","",conn);
						conn.commit();

					}
				}else 
				{
					errString =  itmDBAccessLocal.getErrorString("","VTNOTOPEN","","",conn);
				}

			}
			
			// end if errstrng
		} catch (Exception e)
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if(conn != null && !conn.isClosed())
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
			} catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		return errString;

	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
}