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
public class ChargeBackLocPostSave extends ValidatorEJB implements
		ChargeBackLocPostSaveLocal, ChargeBackLocPostSaveRemote
		{
	public String postSave() throws RemoteException,ITMException
	{
		return "";
	}
	public String postSaveRec() throws RemoteException, ITMException
	{
		return "";
	}
	public String postSaveRec(String xmlString1, String domId, String objContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		return "";
	}

	boolean isLocalConn = false;



	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		
		String sql = "", error = "";
		String issDspNo = "", chgTerm = "", chgUserNew = "";
		String errString = "", errCode = "", chgUser = "", chgUserDom = "", userId = "";
		int cnt = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		double damount = 0;
		double hAmount = 0;
		double dTaxAmount = 0;
		NodeList parentNodeList = null;
		int ctr = 0;
		int detailListLength = 0;
		double dDiscAmount = 0;
		double dNetamount = 0;
		double hNetAmount = 0;
		double hTaxAmount =0;
		System.out.println("tranId in postSave dom ----> " + tranId);

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			if(conn == null){
				
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver= null;
				isLocalConn = true;
				conn.setAutoCommit(false);
			}
			
			

			sql = "select sum(amount) amount,sum(discount_amt) discount_amt " +
					"from charge_back_det where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);

			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				damount = rs.getDouble("amount");
				dDiscAmount = rs.getDouble("discount_amt");
				hAmount = damount + dDiscAmount;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "UPDATE charge_back set amount =  ? "
					+ " WHERE  tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, hAmount);
			pstmt.setString(2, tranId);
			cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;

			sql = "select sum(net_amt) net_amt,sum(tax_amt) tax_amt " +
					"from charge_back_det where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);

			rs = pstmt.executeQuery();
			if (rs.next())
			{
				dNetamount = rs.getDouble("net_amt");
				dTaxAmount = rs.getDouble("tax_amt");
				hNetAmount = dNetamount + dTaxAmount;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "UPDATE charge_back set net_amt =  ?,tax_amt = ?"
					+ " WHERE  tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, hNetAmount);
			pstmt.setDouble(2, dTaxAmount);
			pstmt.setString(3, tranId);
			cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;

			/*
			 * chgUserDom = chgUserDom == null ? "" : chgUserDom.trim();
			 * chgUserNew = chgUserNew == null ? "" : chgUserNew.trim();
			 * 
			 * System.out.println("chguser >>>>>>>>>> "+chgUserDom+
			 * " chgusernew >>>>>>>>>>>>> "+chgUserNew);
			 * 
			 * if (chgUserNew == null || chgUserNew.trim().length() == 0) {
			 * errCode = "USERNULL"; errString =
			 * getErrorString("code",errCode,userId); //break; } else {
			 * 
			 * sql = "select count(1) from users where code = ?";
			 * pstmt=conn.prepareStatement(sql); pstmt.setString(1,chgUserNew);
			 * rs = pstmt.executeQuery(); if(rs.next()) { cnt = rs.getInt(1); }
			 * pstmt.close(); rs.close(); pstmt = null; rs = null; if(cnt == 0)
			 * { errCode = "USERNOTEXT"; errString =
			 * getErrorString("code",errCode,userId); //break; }else
			 * if(chgUserNew.equals(chgUserDom)){ errCode = "SAMEUSER";
			 * errString = getErrorString("code",errCode,userId); //break;
			 * }else{ parentNodeList = dom.getElementsByTagName("Detail2");
			 * System
			 * .out.println("parentNodeList length >>>>>>>>>>>>>>>>>>>>> "+
			 * parentNodeList.getLength()); detailListLength =
			 * parentNodeList.getLength(); for(ctr = 0; ctr < detailListLength;
			 * ctr++) { issDspNo =
			 * GenericUtility.getInstance().getColumnValue("iss_dsp_no",dom);
			 * //chgUserNew =
			 * GenericUtility.getInstance().getColumnValue("code",dom);
			 * 
			 * System.out.println("issDspNo in DOM --->>" +issDspNo);
			 * System.out.println("chgUserNew in DOM --->>" +chgUserNew);
			 * 
			 * sql =
			 * "update distord_iss set add_user = ?, add_term = ?, add_date = sysdate where tran_id = ? and (confirmed is null or confirmed = 'N')"
			 * ; pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			 * chgUserNew); pstmt.setString(2, chgTerm); pstmt.setString(3,
			 * issDspNo); cnt = pstmt.executeUpdate();
			 * 
			 * if(pstmt != null){ pstmt.close(); pstmt = null; }
			 * 
			 * sql =
			 * "update despatch set add_user = ?, add_term = ?, add_date = sysdate where desp_id = ? and (confirmed is null or confirmed = 'N')"
			 * ; pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			 * chgUserNew); pstmt.setString(2, chgTerm); pstmt.setString(3,
			 * issDspNo); cnt = pstmt.executeUpdate();
			 * 
			 * if(pstmt != null){ pstmt.close(); pstmt = null; }
			 * 
			 * System.out.println(
			 * ">>>>>>>successfully deleted record ChgUserAllocPostSave cnt = :"
			 * + cnt); }
			 * 
			 * 
			 * 
			 * */
			  
			
			
			
			

		} catch (Exception e) {
			try {
				System.out.println(">>>>>>>>>>>>In catch Before rollback>>>");
				conn.rollback();
				System.out.println(">>>>>>>>>>>>rollback  issued >>>");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				error = e1.getMessage();
			}
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			/*error = e.getMessage();*/ //Commented By Mukesh Chauhan on 02/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		} finally 
		{
			
				try
				{
					if(isLocalConn){
						conn.commit();
						conn.close();
						conn = null;
					}
					if(pstmt != null){
						pstmt.close();
						pstmt = null;
					}
					
				}catch(Exception d)
				{
					d.printStackTrace();
					throw new ITMException(d);
				}
			}
			return "";
		}
		}

	
