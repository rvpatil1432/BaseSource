package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;

import ibase.webitm.utility.ITMException;

@Stateless 
public class MissingItem extends ValidatorEJB {
	CommonConstants commonConstants = new CommonConstants();


	public ArrayList getMissingSordDet(String salesOrder,UserInfoBean userInfo) throws RemoteException, ITMException {
		// TODO Auto-generated method stub
		E12GenericUtility genericUtility = new E12GenericUtility();
		ArrayList<MissingItemBean> missingitemList = new ArrayList<MissingItemBean>();
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs1=null,rs2=null;
		Connection conn = null;
		String sql,itemCodeOrd = null,itemDescr,chPartner = null;
		String retString="";
		String siteCode="",custCode="";
		String disLink="",pOrder="";
		double sqty;
		String suppCodemnfr,suppCodeCh= null,siteCodeCh = null,itemCode = null,lineno=null;
		int cntPO = 0;
		String statushdr=null,statusdet = null;
		//Connection connCP=null;
		//ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		String itemSer = null;
		String status=null;
		double potqty=0;
		int cnt=0;
		double qtydlv = 0;
		double qtyhdr = 0;

		try
		{
			System.out.println("enter the loop");
			ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB(userInfo);

			if (conn == null)
			{
				setUserInfo(userInfo);
				conn = getConnection();
			}
			conn.setAutoCommit(false);
			connDriver = null;

			//to find cust_code,site_code from sorder
			sql="select cust_code,site_code,item_ser from sorder where sale_order=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,salesOrder);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				siteCode=rs.getString("site_code");
				custCode=rs.getString("cust_code");
				itemSer=rs.getString("item_ser");
			}
			System.out.println("SITE_CODE IS:"+siteCode);
			System.out.println("custCode IS:"+custCode);
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;

			//to find channelpartner,site_code_ch from site_customer
			sql="select channel_partner, DIS_LINK, SITE_CODE__CH  from site_customer  where cust_code = ?  and site_code = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,custCode);
			pstmt.setString(2,siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				chPartner=rs.getString(1);
				disLink=rs.getString(2);
				siteCodeCh=rs.getString(3);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;

			System.out.println("channel partner:"+chPartner);
			System.out.println("channel partner:"+disLink);
			System.out.println("channel partner:"+siteCodeCh);
			if(chPartner.trim().length()==0)
			{
				//to find channelpartner,site_code_ch from customer
				sql="select channel_partner, dis_link,site_code  from customer where cust_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					chPartner=rs.getString(1);
					disLink=rs.getString(2);
					siteCodeCh=rs.getString(3);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
			if("Y".equalsIgnoreCase(chPartner))
			{
				//to find supp_code ,from site_supplier
				sql=" select supp_code from site_supplier where site_code = ? and site_code__ch = ? and channel_partner = 'Y' ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeCh);
				pstmt.setString(2,siteCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					suppCodeCh=rs.getString("supp_code");
					suppCodemnfr=suppCodeCh;
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(suppCodeCh.trim().length()==0)
				{
					sql=" select supp_code from supplier  where site_code = ?  and channel_partner = 'Y'";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,siteCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						suppCodeCh=checkNull(rs.getString("supp_code")).trim();
						suppCodemnfr=suppCodeCh;
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}
				sql="select item_code__ord,item_descr,quantity from sorddet where sale_order=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,salesOrder);
				rs=pstmt.executeQuery();
				while(rs.next())
				{
					itemCode=rs.getString("item_code__ord");
					itemDescr=rs.getString("item_descr");
					sqty=rs.getDouble("quantity");

					sql = "select count(*) from	porder a, porddet b"
							+ "	where a.purc_order =  b.purc_order "
							+ "	and a.supp_code = ? and	a.site_code__dlv = ? "	
							+ "	and	a.item_ser = ? and b.item_code = ? "
							+ "	and	a.status = 'O' and b.status='O'";

					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,suppCodeCh);
					pstmt1.setString(2,siteCodeCh);
					pstmt1.setString(3,itemSer);
					pstmt1.setString(4,itemCode);
					rs1=pstmt1.executeQuery();
					if(rs1.next()) 
					{
						cntPO=rs1.getInt(1);
						System.out.println("count missing data:1"+cntPO);
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
					if(cntPO>1)
					{
						sql = "select a.purc_order,b.quantity,b.dlv_qty,b.line_no from porder a, porddet b"
								+ "	where a.purc_order =  b.purc_order "
								+ "	and a.supp_code = ? and	a.site_code__dlv = ? "	
								+ "	and	a.item_ser = ? and b.item_code = ? ";

						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,suppCodeCh);
						pstmt1.setString(2,siteCodeCh);
						pstmt1.setString(3,itemSer);
						pstmt1.setString(4,itemCode);
						rs1=pstmt1.executeQuery();
						while(rs1.next())
						{
							pOrder=rs1.getString("purc_order");
							qtyhdr=rs1.getDouble("quantity");
							qtydlv=rs1.getDouble("dlv_qty");
							lineno=rs1.getString("line_no");
							potqty=qtyhdr-qtydlv;
							retString="Multiple opened order line  for Purchase Order("+pOrder+") line no("+lineno+")";
							MissingItemBean missingbean=new MissingItemBean();
							missingbean.setItemCode(itemCode);
							missingbean.setItemDesc(itemDescr);
							missingbean.setItemQuantity(potqty);
							missingbean.setStatus(retString);
							missingitemList.add(missingbean);
						}
						rs1.close();
						rs1=null;
						pstmt1.close();
						pstmt1=null;	
					}
					if(cntPO==1)	//po==1
					{
						sql = "select a.purc_order,b.quantity,b.dlv_qty,b.line_no from porder a, porddet b"
								+ "	where a.purc_order =  b.purc_order "
								+ "	and a.supp_code = ? and	a.site_code__dlv = ? "	
								+ "	and	a.item_ser = ? and b.item_code = ? ";

						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,suppCodeCh);
						pstmt1.setString(2,siteCodeCh);
						pstmt1.setString(3,itemSer);
						pstmt1.setString(4,itemCode);
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							pOrder=rs1.getString("purc_order");
							qtyhdr=rs1.getDouble("quantity");
							qtydlv=rs1.getDouble("dlv_qty");
							lineno=rs1.getString("line_no");
							potqty=qtyhdr-qtydlv;

							if(sqty>potqty)
							{
								retString = "Order status is Open where Purchase Order quantity ("+potqty+") is less than SaleOrder quantity ("+sqty+") for orderno ("+pOrder+") and line no("+lineno+")";
								MissingItemBean missingbean=new MissingItemBean();
								missingbean.setItemCode(itemCode);
								missingbean.setItemDesc(itemDescr);
								missingbean.setItemQuantity(potqty);
								missingbean.setStatus(retString);
								missingitemList.add(missingbean);	
							}
						}
						rs1.close();
						rs1=null;
						pstmt1.close();
						pstmt1=null;
					}
					//else cnt<0
					if (cntPO == 0) 
					{
						
						cnt=0;
						sql = "select a.purc_order,a.status,b.status,b.quantity,b.dlv_qty,b.line_no from porder a, porddet b"
								+ "	where a.purc_order =  b.purc_order "
								+ "	and a.supp_code = ? and	a.site_code__dlv = ? "	
								+ "	and	a.item_ser = ? and b.item_code = ? ";

						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,suppCodeCh);
						pstmt1.setString(2,siteCodeCh);
						pstmt1.setString(3,itemSer);
						pstmt1.setString(4,itemCode);
						rs1=pstmt1.executeQuery();
						while(rs1.next())
						{
							pOrder=rs1.getString("purc_order");
							statushdr=rs1.getString("status");
							statusdet=rs1.getString("status");
							qtyhdr=rs1.getDouble("quantity");
							qtydlv=rs1.getDouble("dlv_qty");
							lineno=rs1.getString("line_no");
							System.out.println("count missing data:1"+cntPO);
							cnt++;
				
							if("X".equalsIgnoreCase(statushdr))//CANCEL STATUS IN HEADER
							{
								//	retString = "Purchase order Cancelled for the "+pOrder;//order no
								retString = "Order status is cancelled for the Purchase order("+pOrder+")";//order no
								MissingItemBean missingbean=new MissingItemBean();
								missingbean.setItemCode(itemCode);
								missingbean.setItemDesc(itemDescr);
								missingbean.setItemQuantity(qtyhdr);
								missingbean.setStatus(retString);
								missingitemList.add(missingbean);	
								System.out.println("Purchase order already canceled("+pOrder+")");

							}
							if("C".equalsIgnoreCase(statushdr))//Closed STATUS IN HEADER
							{
								//retString = "Purchase order Closed for the "+salesOrder;
								retString = "Order status is closed for the Purchase order("+pOrder+")";//order no
								MissingItemBean missingbean=new MissingItemBean();
								missingbean.setItemCode(itemCode);
								missingbean.setItemDesc(itemDescr);
								missingbean.setItemQuantity(qtyhdr);
								missingbean.setStatus(retString);
								missingitemList.add(missingbean);	
								System.out.println("purchase order already closed"+itemCode);
							}
							if("X".equalsIgnoreCase(statusdet))//CANCEL STATUS IN DETAIL
							{
								//retString = "Purchase order Cancelled for line item for "+itemCode +" "+itemDescr;

								retString = "Order status is opened for Purchase order ("+pOrder+") but line no("+lineno+")  status is Cancelled";
								MissingItemBean missingbean=new MissingItemBean();
								missingbean.setItemCode(itemCode);
								missingbean.setItemDesc(itemDescr);
								missingbean.setItemQuantity(qtyhdr);
								missingbean.setStatus(retString);
								missingitemList.add(missingbean);	
								System.out.println("Purchase order Cancelled for line item for "+itemCode);
							}

							if("C".equalsIgnoreCase(statusdet))//Closed STATUS IN DETAIL
							{
								//retString = "Purchase order Closed for line item for "+itemCode +" "+itemDescr;
								retString = "Order status is opened for ("+pOrder+") but line no("+lineno+") status is Closed";
								MissingItemBean missingbean=new MissingItemBean();
								missingbean.setItemCode(itemCode);
								missingbean.setItemDesc(itemDescr);
								missingbean.setItemQuantity(qtyhdr);
								missingbean.setStatus(retString);
								missingitemList.add(missingbean);	
								System.out.println("Purchase order Closed for line item for "+itemCode);
							}
							else 
							{
								/*sql = "select b.quantity,b.dlv_qty from porder a, porddet b"
										+ "	where a.purc_order =  b.purc_order "
										+ "	and a.supp_code = ? and	a.site_code__dlv = ? "	
										+ "	and	a.item_ser = ? and b.item_code = ? ";

								System.out.println("count missing data111:2"+cntPO);
								pstmt1=conn.prepareStatement(sql);
								pstmt1.setString(1,suppCodeCh);
								pstmt1.setString(2,siteCodeCh);
								pstmt1.setString(3,itemSer);
								pstmt1.setString(4,itemCode);*/
								sql = "select b.quantity,b.dlv_qty,a.purc_order from porder a, porddet b"
										+ "	where a.purc_order =  b.purc_order "
										+ "	and b.line_no=? ";

								System.out.println("count missing data111:2"+cntPO);
								pstmt2=conn.prepareStatement(sql);
								pstmt2.setString(1,lineno);

								rs2=pstmt2.executeQuery();
								if(rs2.next())
								{
									pOrder=rs2.getString("purc_order");
									qtyhdr=rs2.getDouble("quantity");
									qtydlv=rs2.getDouble("dlv_qty");
									System.out.println("count missing data:2"+cntPO);
								}
								rs2.close();
								rs2=null;
								pstmt2.close();
								pstmt2=null;

								potqty=qtyhdr-qtydlv;
								if(sqty>potqty)
								{
									System.out.println("item is missing2"+itemCode);
									retString = "Purchase Order quantity ("+potqty+") is less than SaleOrder quantity ("+sqty+") for orderno ("+pOrder+") and line no("+lineno+")";

									MissingItemBean missingbean=new MissingItemBean();
									missingbean.setItemCode(itemCode);
									missingbean.setItemDesc(itemDescr);
									missingbean.setItemQuantity(potqty);
									missingbean.setStatus(retString);
									missingitemList.add(missingbean);	
									System.out.println("Purchase Order quantity ("+potqty+") is less than SaleOrder quantity ("+sqty+") for orderno ("+pOrder+")");
								}
							}//end else
						}
						rs1.close();
						rs1=null;
						pstmt1.close();
						pstmt1=null;						
						if(cnt==0)
						{
	 							retString = "Item Missing";
								MissingItemBean missingbean=new MissingItemBean();
								missingbean.setItemCode(itemCode);
								missingbean.setItemDesc(itemDescr);
								missingbean.setItemQuantity(qtyhdr);
								missingbean.setStatus(retString);
								missingitemList.add(missingbean);	
						}
					}//end if
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
		}


		catch(Exception e) 
		{

			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{			
				genericUtility = null;				
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(rs != null)
				{
					rs.close(); 
					rs = null;				
				}
				if(conn != null)
				{					
					conn.close();
					conn=null;
				}				
			}
			catch(Exception e)
			{
				System.out.println("Exception :MissingItem detail:==>\n"+e.getMessage());
			}
		}
		return missingitemList;
	}

	private String checkNull(String string) {
		// TODO Auto-generated method stub

		if (string == null) {
			string = "";
		}
		return string;
	}



}
