/********************************************************
	Title : SaleOrderOpenAct[D15BSUN005]
	Date  : 02/06/15
	Developer: Priyanka Shinde

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.CreatePoRcpVoucher;
import ibase.webitm.ejb.fin.FinCommon;

import ibase.webitm.utility.ITMException;

@Stateless

public class SaleOrderOpenAct extends ActionHandlerEJB implements SaleOrderOpenActLocal,SaleOrderOpenActRemote
{
	
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;

		System.out.println(".......tranId......."+tranId);
		System.out.println(".......xtraParams..."+xtraParams);
		System.out.println(".......forcedFlag..."+forcedFlag);
		if(tranId!=null && tranId.trim().length()>0)
		{
			returnString = openSaleOrder(tranId,xtraParams,forcedFlag);
		}
		return returnString;
	}
	
	public String openSaleOrder(String saleOrder,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("openSaleOrder called........");
		String sql = "",sql1="";
		String errString = "" ;		
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;		
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= null;
		Timestamp sysdate = null;
		String chgTerm = "", chgUser = "";
		int upCount=0,fulDspCnt=0,rowCnt=0,openCnt=0,rowSkip=0;
		String saleOrd="",lineNo="",status="",status1 ="",confirm = "",soitStatus="";
		double qty=0,qtydesp=0,qtyDiffCnt=0;
//		boolean flag=false;
		
		try
		{
			
			connDriver = new ConnDriver();
//			conn = connDriver.getConnectDB("DriverITM");
			conn=getConnection();
			connDriver = null;
			conn.setAutoCommit(false);
			genericUtility =new  ibase.utility.E12GenericUtility();			
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			sysdate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");			
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_TERM" );
			chgUser =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_USER" );	
			System.out.println("SALES ORDER==========="+saleOrder);
			sql1="select confirmed from sorder where sale_order= ?  ";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, saleOrder);
			rs1 = pstmt1.executeQuery();
			if (rs1.next())
			{
				confirm= rs1.getString("confirmed");	
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;	
			System.out.println("Confirmed is : "+ confirm);
			if(confirm.equals("N"))
			{
				errString = itmDBAccessLocal.getErrorString("","VTSLONTCFM","","",conn); 
				return errString;
			}
			else
			{
			//sql = "select status,qty_desp,quantity,sale_order,line_no from sorditem where sale_order= ?";
				
				sql="select count(*) as cnt from sorditem where sale_order=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rowCnt=rs.getInt("cnt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			sql="select b.status,a.qty_desp,a.quantity,a.line_no,a.status as soit_status from sorditem a,sorder b" +
					" where b.sale_order=a.sale_order" +
					" and  b.sale_order= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				status=rs.getString("status");
				qtydesp = rs.getDouble("qty_desp");	
				qty = rs.getDouble("quantity");
				lineNo=rs.getString("line_no");
				soitStatus=rs.getString("soit_status");
				
				System.out.println("lineNo======" + lineNo);	
				System.out.println("status=========" + status);	
				System.out.println("qty=====" + qty);
				System.out.println("qtydesp======="+qtydesp);
				System.out.println("Value is :+ "+ !status.equals("C"));
				/*if(!status.equals("C"))
				{
					System.out.println("In C");
					//errString = itmDBAccessLocal.getErrorString("","VTSLONTOPN","","",conn);
					errString = itmDBAccessLocal.getErrorString("","VTSLONTOPN","","",conn); 
					return errString;
				}*/
				if(qtydesp!=qty)
				{
					if(!"P".equalsIgnoreCase(soitStatus))
					{
						qtyDiffCnt++;
						System.out.println("qtydesp!=qty");
						upCount=updateSorditem(saleOrder,lineNo,conn); // VALLABH KADAM [24/FEB/17]
						System.out.println("@V@ Update Sorditem :- ["+upCount+"]");
					}
					else
					{
						rowSkip++;
					}
				}
				else
				{
					fulDspCnt++;
				}
			}			
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;			
			
			System.out.println("@V@ Quentity mismatch count :- ["+qtyDiffCnt+"]");
			System.out.println("@V@ fulDspCnt count :- ["+fulDspCnt+"]");
					
//			if(flag==true )
//			{
			if(qtyDiffCnt>0) // VALLABH KADAM [24/FEB/17]
			{
//				sql1=" update sorditem set status = 'P' where  sale_order=?";
//				pstmt = conn.prepareStatement(sql1);					
//				pstmt.setString(1, saleOrder);
//				upCount = pstmt.executeUpdate();
//				pstmt.close();
//				pstmt= null;					
//				System.out.println("Update Count===="+upCount);
				
				System.out.println("Flag is saleorderitem true@@@@");
				sql1=" update sorder set status = 'P' where  sale_order=?";
				pstmt = conn.prepareStatement(sql1);					
				pstmt.setString(1, saleOrder);
				upCount = pstmt.executeUpdate();
				pstmt.close();
				pstmt= null;					
				System.out.println("Update saleorderitem Count===="+upCount);
			}
			else if(rowCnt==fulDspCnt)
			{
				System.out.println("Sale Order is Fully Despatch!!!!!!!!!!!!");					
				errString = itmDBAccessLocal.getErrorString("","VTFULDESP","","",conn);
				return errString;
			}
			/**
			 * If all rows are pending
			 * or
			 * non pending rows are dispatched already
			 * */
			else if((rowCnt==rowSkip)||(qtyDiffCnt ==0  && fulDspCnt >0))
			{
				System.out.println("Sale Order is already open!!!!!!!!!!!!");					
				errString = itmDBAccessLocal.getErrorString("","VTSOALOPN","","",conn);
				return errString;
			}
			if(upCount>0)
			{				
				System.out.println("Sale Order is open successfully!!!!!!!!!!!!");
				errString = itmDBAccessLocal.getErrorString("","VTSORDOP","","",conn);
				return errString;
			}
			}
		} 
		catch( Exception e)
		{			

			System.out.println("SaleOrderOpenAct..."+e.getMessage());
		    e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
			   System.out.println("SaleOrderOpenAct..."+e1.getMessage());
			   e1.printStackTrace();
			}
			throw new ITMException(e); //Added By Mukesh Chauhan on 09/08/19
		}
		finally
		{		
				try
				{
					
					if (errString != null && errString.trim().length() > 0) 
					{
						
						System.out.println("--going to commit tranaction--");
							if (errString.indexOf("VTSORDOP") > -1)
		
							{						
								conn.commit();
								System.out.println("--transaction commited--");
							} 
							else 
							{
								conn.rollback();
								System.out.println("--transaction rollback--");
							}
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
					if(conn != null)
					{
						conn.close();
						conn = null;
					}
				}
				catch(Exception e)
				{
					System.out.println("Exception : "+e);
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
		return errString;
	} //end of  method
/**
 * VALLABH KADAM [24/FEB/17]
 * The private method to created to
 * update each 'sorditem' status for
 * respective sale order
 * @throws ITMException 
 * */
	private int updateSorditem(String saleOrder,String lineNo, Connection conn) throws ITMException 
	{
		// TODO Auto-generated method stub
		int updateCnt=0;
		PreparedStatement pstmt=null;
		String sql="";
		try 
		{
			sql="update sorditem set status = 'P' where  sale_order=? and line_no=?";
			pstmt = conn.prepareStatement(sql);					
			pstmt.setString(1, saleOrder);
			pstmt.setInt(2, Integer.parseInt(lineNo.trim()));
			updateCnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt= null;				
			
			System.out.println("@V@ Update Count :- ["+updateCnt+"]");	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 09/08/19
		}
		return updateCnt;
	}
	
}
