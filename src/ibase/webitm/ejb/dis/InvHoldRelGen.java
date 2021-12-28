package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.*;

import org.w3c.dom.*;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.Stateless; // added for ejb3
import ibase.webitm.ejb.dis.adv.*;

@Stateless
public class InvHoldRelGen extends ProcessEJB implements InvHoldRelGenLocal, InvHoldRelGenRemote
{

	DistCommon distCommonObj = new DistCommon();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	@Override
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	//getData Method
	@Override
	public String process(Document dom, Document dom2, String windowName,
			String xtraParams) throws RemoteException, ITMException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;	

		try
		{

			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 				
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{

			System.out.println("Exception :InvHoldRelGenEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();
			throw new ITMException(e);

		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String blanknull(String s)
	{
		if( s==null )
			return " ";
		else
			return s;
	}


	@Override
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		String errCode = "";
		String errString = "";
		String getDataSql= "" ;
		String lockType="";
		String resultString = "";
		ResultSet rs1 = null,descrs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		PreparedStatement pstmt = null,descpstmt = null,pstmt1 = null, pstmt2 = null;
		// 09/04/14 manoharan
		//StringBuffer retTabSepStrBuff = new StringBuffer();
		StringBuffer retTabSepStrBuff = new StringBuffer("<?xml version = \"1.0\"?>");
		retTabSepStrBuff.append("<DocumentRoot>");
		retTabSepStrBuff.append("<description>").append("Datawindow Root").append("</description>");
		retTabSepStrBuff.append("<group0>");
		retTabSepStrBuff.append("<description>").append("Group0 description").append("</description>");
		retTabSepStrBuff.append("<Header0>");
		int cnt=0,count=0,i=7;
		String sql="";
		String subSql="";
		String lockCodeFrom = "",lockCodeTo = "",tranIdFrom = "",tranIdTo="",refIdFrom="",refIdTo="",autoConfirmed="",schRelDateFromstr="",schRelDateTostr="";
		Timestamp schRelDateFrom=null,schRelDateTo=null;
		String lotNoFrom="",lotNoTo="",invoiceNo="",holdQuantity="";	
		String siteCode="",itemCode="",lotNo="",lotsl="",locCode="";//Added by chandrashekar 0n 19-09-2014
		String subQuery = "";		

		ConnDriver connDriver = new ConnDriver();
		Connection conn= null;
		try
		{
			System.out.println("mahendra testing@@@@@@@@@ 12-09-2014 !!!!");
			System.out.println("windowName!!!!! "+windowName);


			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

				conn.setAutoCommit(false);	
			}
			DatabaseMetaData dbmd = conn.getMetaData();
			System.out.println("DriverName["+dbmd.getDriverName() + "]");
			System.out.println("DriverURI["+dbmd.getURL()  + "]");
			System.out.println("DriverUSER["+dbmd.getUserName() +"]");

			System.out.println("InvHoldRelGen : getData() Method Called");

			tranIdFrom = genericUtility.getColumnValue("tran_id__from",headerDom);
			tranIdTo = genericUtility.getColumnValue("tran_id__to",headerDom);
			schRelDateFromstr = genericUtility.getColumnValue("sch_rel_date__from",headerDom);
			schRelDateTostr = genericUtility.getColumnValue("sch_rel_date__to",headerDom);
			refIdFrom = genericUtility.getColumnValue("ref_id__from",headerDom);
			refIdTo = genericUtility.getColumnValue("ref_id__to",headerDom);
			lockCodeFrom = genericUtility.getColumnValue("lock_code__from",headerDom);    //added by Ritesh on 08/05/13
			lockCodeTo = genericUtility.getColumnValue("lock_code__to",headerDom);		 //added by Ritesh on 08/05/13
			autoConfirmed = genericUtility.getColumnValue("auto_confirmed",headerDom);
			lotNoFrom = genericUtility.getColumnValue("lot_no__from",headerDom);//Added by manoj dtd 15/04/2014 to getdata based on entered lotNo
			lotNoTo = genericUtility.getColumnValue("lot_no__to",headerDom);//Added by manoj dtd 15/04/2014 to getdata based on entered lotNo



			//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("schRelDateFromstr : ["+schRelDateFromstr+"]:::schRelDateTostr:["+schRelDateTostr+"]");
			if(schRelDateFromstr != null)
			{
				System.out.println("schRelDateFromstr : ["+schRelDateFromstr);
				schRelDateFrom= Timestamp.valueOf(genericUtility.getValidDateString(schRelDateFromstr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(schRelDateTostr != null)
			{
				System.out.println(" :::schRelDateTostr:["+schRelDateTostr+"]");
				//schRelDateTo= Timestamp.valueOf(genericUtility.getValidDateString(schRelDateTostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");//Commented by Manoj dtd 04/10/2013 to set hrs and minutes also
				schRelDateTo= Timestamp.valueOf(genericUtility.getValidDateString(schRelDateTostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 23:59:59.0");
			}
			System.out.println("schRelDateFrom : ["+schRelDateFrom+"]:::schRelDateTo:["+schRelDateTo+"]");
			System.out.println("tranIdFrom=>"+tranIdFrom);
			System.out.println("tranIdTo=>"+tranIdTo);
			System.out.println("refIdFrom =>"+refIdFrom);
			System.out.println("refIdTo =>"+refIdTo);
			System.out.println("autoConfirmed =>"+autoConfirmed);
			System.out.println("lockCodeFrom=>"+lockCodeFrom);			  //added by Ritesh on 08/05/13
			System.out.println("lockCodeTo=>"+lockCodeTo);				 //added by Ritesh on 08/05/13

			System.out.println("lotNoFrom=>"+lotNoFrom);			 
			System.out.println("lotNoTo=>"+lotNoTo);

			/*if ( tranIdFrom == null || tranIdFrom.trim().length() == 0 )
			{
				System.out.println("tran id from is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMTRIDFRNU","","",conn);
				return errString;
			}
			else
			{*/
			/*
				sql = "select count(1) from inv_hold_det where tran_id ='" + tranIdFrom + "'";
			    pstmt = conn.prepareStatement(sql);
			    rs1 = pstmt.executeQuery();
			    if (rs1.next())
			    {
				cnt = rs1.getInt(1);
			    }
			    pstmt.close();
			    rs1.close();
			    pstmt = null;
			    rs1 = null;
			    	if (cnt == 0)
			    	{
			    		errString = itmDBAccessEJB.getErrorString("","TRIDFRINV","","",conn);
			    		return errString;
			    	}
			 */   	
			//}

			/*if ( tranIdTo == null || tranIdTo.trim().length() == 0 )
			{
				System.out.println("tran id to is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMTRIDTONU","","",conn);
				return errString;
			}
			else
			{*/
			/*	
				sql = "select count(1) from inv_hold_det where tran_id ='" + tranIdTo + "'";
			    pstmt = conn.prepareStatement(sql);
			    rs1 = pstmt.executeQuery();
			    if (rs1.next())
			    {
				cnt = rs1.getInt(1);
			    }
			    pstmt.close();
			    rs1.close();
			    pstmt = null;
			    rs1 = null;
			    	if (cnt == 0)
			    	{
			    		errString = itmDBAccessEJB.getErrorString("","TRIDTOINV","","",conn);
			    		return errString;
			    	}
			 */  	
			//}

			/*if ( schRelDateFrom == null )
			{
				System.out.println("schedule release date from is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMSCDTFRNU","","",conn);
				return errString;
			}
			else
			{
				if(schRelDateTo != null)
				{
					if(schRelDateFrom.after(schRelDateTo))
					{
						errString = itmDBAccessEJB.getErrorString("","SCDTTOINV","","",conn);
			    		return errString;
					}
				}
			}
			 */

			/*if ( schRelDateTo == null )
			{
				System.out.println("schedule release date to is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMSCDTTONU","","",conn);
				return errString;
			}
			else
			{
				if(schRelDateTo != null)
				{
					if(schRelDateTo.before(schRelDateFrom))
					{
						errString = itmDBAccessEJB.getErrorString("","SCDTTOINV","","",conn);
			    		return errString;
					}
				}
			}*/


			/*if ( refIdFrom == null )
			{
				//refIdFrom="0";
				//System.out.println("ref id from is Null...");
				//errString = itmDBAccessEJB.getErrorString("","VMRFIDFRNU","","",conn);
				//return errString;
			}
			else
			{*/
			/*
				sql = "select count(1) from inv_hold where ref_id ='" + refIdFrom + "'";
			    pstmt = conn.prepareStatement(sql);
			    rs1 = pstmt.executeQuery();
			    if (rs1.next())
			    {
				cnt = rs1.getInt(1);
			    }
			    pstmt.close();
			    rs1.close();
			    pstmt = null;
			    rs1 = null;
			    	if (cnt == 0)
			    	{
			    		errString = itmDBAccessEJB.getErrorString("","REFIDFRINV","","",conn);
			    		return errString;
			    	}
			 */   	
			//}


			/*if ( refIdTo == null )
			{
				//refIdTo="ZZ";
				//System.out.println("ref id to is Null...");
				//errString = itmDBAccessEJB.getErrorString("","VMRFIDTONU","","",conn);
				//return errString;
			}
			else
			{*/
			/*	
				sql = "select count(1) from inv_hold where ref_id ='" + refIdTo + "'";
			    pstmt = conn.prepareStatement(sql);
			    rs1 = pstmt.executeQuery();
			    if (rs1.next())
			    {
				cnt = rs1.getInt(1);
			    }
			    pstmt.close();
			    rs1.close();
			    pstmt = null;
			    rs1 = null;
			    	if (cnt == 0)
			    	{
			    		errString = itmDBAccessEJB.getErrorString("","REFIDTOINV","","",conn);
			    		return errString;
			    	}
			 */    	
			//}
			/*	
			if ( autoConfirmed == null )
			{
				System.out.println("auto Confirmed is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMAUTCONNU","","",conn);
				return errString;
			}
			 */	
			/*if ( lotNoFrom == null || lotNoFrom.trim().length() == 0 )
			{
				System.out.println("lotNoFrom is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMINVLTFR","","",conn);
				return errString;
			}
			if ( lotNoTo == null || lotNoTo.trim().length() == 0 )
			{
				System.out.println("lotNoTo is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMINVLTTO","","",conn);
				return errString;
			}*/

			/*
			 * changes done by mahendra on 29-05-2014 
			 * [to get data by filtering lock_type]
			 * [value of lock_type is 0==warehouse lock,1==quality lock,2==Administrative lock,3==-System managed locks]
			 * [if menu is InvHoldRelGen then data filter with lock type 'warehouse lock'
			 * If menu is InvHoldRelGen For Quality then data filter with lock type 'quality lock'
			 * if menu is InvHoldRelGen For Administrative then data filter with lock type 'Administrative lock'  
			 * */

			System.out.println("windowName !!!"+windowName);
			if(windowName.equalsIgnoreCase("w_invholdrelgen_wh"))
			{
				System.out.println("windowName 1"+windowName);
				subSql = "and il.lock_type='0'";//warehouse lock
			}

			if(windowName.equalsIgnoreCase("w_invholdrelgen_quality"))
			{
				System.out.println("windowName 1"+windowName);
				subSql = "and il.lock_type='1'";//Quality Lock
			}
			if(windowName.equalsIgnoreCase("w_invholdrelgen_adm"))
			{
				System.out.println("windowName 2"+windowName);
				subSql = "and il.lock_type='2'";//Administrative Lock
			}

			System.out.println("windowName !!! "+windowName +"lockType !!!!"+lockType);


			System.out.println("lockType :"+lockType);
			System.out.println("refIdFrom["+refIdFrom+"]:::::::refIdTo["+refIdTo+"]");
			//ih.lock_code ,ihd.lot_no,ihd.lot_sl,ihd.remarks  ADDED BY RITESH 
			//	getDataSql= " select ihd.tran_id,ihd.line_no,ihd.line_no_sl," +
			//				" case when ihd.hold_status is null then 'H' else ihd.hold_status end as hold_status ," +
			//				" ihd.item_code,ihd.loc_code,ihd.site_code,ihd.status_date,ihd.reas_code,ihd.sch_rel_date," +
			//				" ih.ref_ser,ih.ref_id,ih.ref_no, ih.confirmed, ih.lock_code ,ihd.lot_no,ihd.lot_sl,ihd.remarks  " +
			//				" from inv_hold_det ihd,inv_hold ih , inv_lock il where ( ihd.tran_id = ih.tran_id )  and (ih.lock_code=il.lock_code) " +
			//				" and ( ihd.tran_id >= ? and ihd.tran_id <= ? ) and ( ih.lock_code >= ? and ih.lock_code <= ? )" +
			//				" and ( ihd.sch_rel_date between ? and ? ) and ihd.hold_status <> 'R' "
			//			  // +" and ( ihd.lot_no >= '"+lotNoFrom+"' and ihd.lot_no <= '"+lotNoTo+"' ) and ih.confirmed='Y' " //Added by Manoj dtd 15/04/2014 to retrieve data based on entered lot No range
			//			   +" and (( ihd.lot_no >= '"+lotNoFrom+"' and ihd.lot_no <= '"+lotNoTo+"' ) OR ihd.lot_no IS NULL) and ih.confirmed='Y' " //change by chandrashekar on 30-10-2014
			//			   + " and nvl((select count(1) from inv_hold_rel_det r where r.tran_id__hold = ihd.tran_id and  r.line_no__hold = ihd.line_no ),0) = 0   "+subSql; // 14/04/14 manoharan this condition added
			getDataSql= " select ihd.tran_id,ihd.line_no,ihd.line_no_sl, " +
					"case when ihd.hold_status is null then 'H' else ihd.hold_status end as hold_status ," +
					"ihd.item_code,ihd.loc_code,ihd.site_code,ihd.status_date,ihd.reas_code,ihd.sch_rel_date," +
					" ih.ref_ser,ih.ref_id,ih.ref_no, ih.confirmed,ih.lock_code ,ihd.lot_no,ihd.lot_sl,ihd.remarks  " +
					" from inv_hold_det ihd,inv_hold ih , inv_lock il where ( ihd.tran_id = ih.tran_id )  and (ih.lock_code=il.lock_code) " +
					" and ( ihd.tran_id >= ? and ihd.tran_id <= ? )and ( ih.lock_code >= ? and ih.lock_code <= ? )" +
					" and ( ihd.sch_rel_date between ? and ? ) and case when ihd.hold_status is null then 'H' else ihd.hold_status end='H' "+//Condition changed by manoj dtd 20012014 from hold_status<>'R'
					" and (( ihd.lot_no >= '"+lotNoFrom+"' and ihd.lot_no <= '"+lotNoTo+"' ) OR ihd.lot_no IS NULL)  "+
					" and (select count(1) from inv_hold_rel_trace t      " +
					" where t.ref_no = ihd.tran_id and t.lot_no >= '"+lotNoFrom+"' and   t.lot_no <= '"+lotNoTo+"'  and t.hold_qty >0   ) > 0 " // 12/11/14 manoharan filter lot from trace
					+ " and ih.confirmed='Y' " 
					+ " and nvl((select count(1) from  inv_hold_rel_det r where r.tran_id__hold = ihd.tran_id and  r.line_no__hold = ihd.line_no ),0) = 0   "+subSql; // 14/04/14 manoharan

			//and ( ihd.lock_code between ? and ? )
			if(refIdFrom != null )	
			{	
				getDataSql = getDataSql + " and ( ih.ref_id >= ? or ih.ref_id is null ) ";
			}

			if(refIdTo != null )	
			{	
				getDataSql = getDataSql + " and ( ih.ref_id <= ?  or ih.ref_id is null ) ";
			}





			System.out.println("Sql Fired :::::"+getDataSql.trim().length());
			if (getDataSql.trim().length()>0)
			{
				System.out.println("Sql Fired :::::"+getDataSql);					
				pstmt = conn.prepareStatement(getDataSql);
				pstmt.setString(1,tranIdFrom);
				pstmt.setString(2,tranIdTo);
				pstmt.setString(3,lockCodeFrom); 						 //added by Ritesh on 08/05/13
				pstmt.setString(4,lockCodeTo);						    //added by Ritesh on 08/05/13				
				pstmt.setTimestamp(5,schRelDateFrom); 
				pstmt.setTimestamp(6,schRelDateTo);		
				//pstmt.setString(7,lockType);	

				if(refIdFrom != null )	
				{
					pstmt.setString(i,refIdFrom);
					i++;
				}

				if( refIdTo != null)	
				{
					pstmt.setString(i,refIdTo);
				}

				System.out.println("QUERY IS IN PROCESS..........");
				rs1 = pstmt.executeQuery();
				System.out.println("QUERY  PROCESS FINISED.......");
				while(rs1.next()) 
				{

					// 09/04/14 manoharan return xml
					//retTabSepStrBuff.append("<Detail2>");  // move inside if
					System.out.println("ref_id !!!!!"+rs1.getString("ref_id"));
					System.out.println("site_code !!!!!"+rs1.getString("site_code"));
					/*----------------changes done by mahendra dated 01/AUG/2014----------------------*/
					System.out.println("ref id ::: "+rs1.getString("ref_id"));
					System.out.println("Tran id ::: "+rs1.getString("ref_ser"));
					System.out.println("site code ::: "+rs1.getString("site_code"));


					if(rs1.getString("ref_id") != null)
					{
						sql = "select INVOICE_NO from porcp where tran_id=? and 'P-RCP'=?  and site_code=?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,rs1.getString("ref_id"));
						pstmt1.setString(2,rs1.getString("ref_ser"));
						pstmt1.setString(3,rs1.getString("site_code"));

						rs2 = pstmt1.executeQuery();
						if (rs2.next())
						{
							invoiceNo = rs2.getString(1);
						}
						pstmt1.close();
						rs2.close();
						pstmt1 = null;
						rs2 = null;

					}
					else
					{
						invoiceNo="";
					}
					System.out.println("invoiceNo "+invoiceNo);
					System.out.println("item_code "+rs1.getString("item_code"));
					System.out.println("loc_code "+rs1.getString("loc_code"));
					System.out.println("lot_no "+rs1.getString("lot_no"));
					//Start added by chandrashekar on 19-09-2014

					siteCode = rs1.getString("site_code");
					itemCode = rs1.getString("item_code");
					lotNo    = rs1.getString("lot_no");
					lotsl    = rs1.getString("lot_sl");
					locCode  = rs1.getString("loc_code");	

					sql = "select sum(hold_qty) as hold_qty from stock where ";
					if(itemCode != null)
					{	
						subQuery=" item_code='"+itemCode+"'";
						sql = sql +subQuery;
					}
					if(siteCode != null)
					{
						if (subQuery.trim().length() == 0)
						{
							subQuery = " site_code='" + siteCode+"'";
							sql = sql + subQuery;
						} else
						{
							subQuery = " and site_code='" +siteCode+"'";
							sql = sql + subQuery;
						}
					}
					if(lotNo != null)
					{
						if (subQuery.trim().length() == 0)
						{
							subQuery = " lot_no='" + lotNo+"'";
							sql = sql + subQuery;
						} else
						{
							subQuery = " and lot_no='" + lotNo+"'";
							sql = sql + subQuery;
						}
					}
					if(locCode != null)
					{
						if (subQuery.trim().length() == 0)
						{
							subQuery = " loc_code='" + locCode+"'";
							sql = sql + subQuery;
						} else
						{
							subQuery = " and loc_code='" + locCode+"'";
							sql = sql + subQuery;
						}
					}
					if(lotsl != null)
					{
						if (subQuery.trim().length() == 0)
						{
							subQuery = " lot_sl='" +lotsl+"'";
							sql = sql + subQuery;
						} else
						{
							subQuery = " and lot_sl='" + lotsl+"'";
							sql = sql + subQuery;
						}
					}
					pstmt2 = conn.prepareStatement(sql);
					rs3 = pstmt2.executeQuery();
					//end added by chandrashekar 0n19-09-2014

					/*sql = "select hold_qty from stock where item_code=? and loc_code=? and lot_no=? and lot_sl=? and site_code=? ";
				    pstmt2 = conn.prepareStatement(sql);
				    pstmt2.setString(1,rs1.getString("item_code"));
				    pstmt2.setString(2,rs1.getString("loc_code"));
				    pstmt2.setString(3,rs1.getString("lot_no"));
				    pstmt2.setString(4,rs1.getString("lot_sl"));
				    pstmt2.setString(5,rs1.getString("site_code"));

				    rs3 = pstmt2.executeQuery();*/
					if (rs3.next())
					{
						holdQuantity = rs3.getString(1);
						System.out.println("holdQuantity : "+holdQuantity);
					}
					pstmt2.close();
					rs3.close();
					pstmt2 = null;
					rs3 = null;

					double holdQty = holdQuantity==null?0:Double.parseDouble(holdQuantity); 
					System.out.println("@@@@@ holdQuantity["+holdQuantity+"]holdQty["+holdQty+"]");
					if( holdQty > 0 )
					{	
						/*-------------------------------------------------------------------------------*/


						retTabSepStrBuff.append("<Detail2>");
						//tran_id
						//retTabSepStrBuff.append((rs1.getString(1)==null?" ":rs1.getString(1))).append("\t");
						//retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" + (rs1.getString(1)==null?" ":rs1.getString(1)) +"]]>").append("</tran_id>");
						//line_no
						//retTabSepStrBuff.append((rs1.getString(2)==null?" ":rs1.getString(2))).append("\t");
						//retTabSepStrBuff.append("<line_no>").append("<![CDATA[" + (rs1.getString(2)==null?" ":rs1.getString(2)) +"]]>").append("</line_no>");
						//line_no_sl
						//retTabSepStrBuff.append((rs1.getString(3)==null?" ":rs1.getString(3))).append("\t");
						//retTabSepStrBuff.append("<line_no_sl>").append("<![CDATA[" + (rs1.getString(3)==null?" ":rs1.getString(3)) +"]]>").append("</line_no_sl>");
						//hold_status
						if(rs1.getString(4)!=null)
						{
							//retTabSepStrBuff.append((rs1.getString(4)==null?" ":rs1.getString(4))).append("\t");
							retTabSepStrBuff.append("<hold_status>").append("<![CDATA[" + (rs1.getString(4)==null?" ":rs1.getString(4)) +"]]>").append("</hold_status>");
						}
						else
						{
							//retTabSepStrBuff.append("H").append("\t");
							retTabSepStrBuff.append("<hold_status>").append("<![CDATA[H]]>").append("</hold_status>");
						}
						//item_code
						//retTabSepStrBuff.append((rs1.getString(5)==null?" ":rs1.getString(5))).append("\t");
						retTabSepStrBuff.append("<item_code>").append("<![CDATA[" + (rs1.getString(5)==null?" ":rs1.getString(5)) +"]]>").append("</item_code>");

						//ADDED BY RITESH ON 25/DEC/13 FOR SET ITEM DESCR. AND LOC CODE DESC. START
						sql= " select descr from item where item_code = ?";
						descpstmt = conn.prepareStatement(sql);
						descpstmt.setString(1,rs1.getString(5));
						descrs = descpstmt.executeQuery();
						if(descrs.next())
						{
							//retTabSepStrBuff.append((descrs.getString(1)==null?" ":descrs.getString(1))).append("\t");
							retTabSepStrBuff.append("<item_descr>").append("<![CDATA[" + (descrs.getString(1)==null?" ":descrs.getString(1)) +"]]>").append("</item_descr>");
						}else{
							//retTabSepStrBuff.append("").append("\t");
							retTabSepStrBuff.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>");

						}
						descpstmt.close();
						descpstmt = null;
						descrs.close();
						descrs = null;

						//loc_code
						//retTabSepStrBuff.append((rs1.getString(6)==null?" ":rs1.getString(6))).append("\t");
						retTabSepStrBuff.append("<loc_code>").append("<![CDATA[" + (rs1.getString(6)==null?" ":rs1.getString(6)) +"]]>").append("</loc_code>");
						//lot_no
						//retTabSepStrBuff.append((rs1.getString(16)==null?" ":rs1.getString(16))).append("\t");			//added by ritesh on 25/12/13 
						retTabSepStrBuff.append("<lot_no>").append("<![CDATA[" + (rs1.getString(16)==null?" ":rs1.getString(16)) +"]]>").append("</lot_no>");
						//lot_sl
						//retTabSepStrBuff.append((rs1.getString(17)==null?" ":rs1.getString(17))).append("\t");			//added by ritesh on 25/12/13 
						retTabSepStrBuff.append("<lot_sl>").append("<![CDATA[" + (rs1.getString(17)==null?" ":rs1.getString(17)) +"]]>").append("</lot_sl>");
						//lock code
						//retTabSepStrBuff.append((rs1.getString(15)==null?" ":rs1.getString(15))).append("\t");
						retTabSepStrBuff.append("<lock_code>").append("<![CDATA[" + (rs1.getString(15)==null?" ":rs1.getString(15)) +"]]>").append("</lock_code>");

						sql= " select descr from inv_lock where lock_code = ?";
						descpstmt = conn.prepareStatement(sql);
						descpstmt.setString(1,rs1.getString(15));
						descrs = descpstmt.executeQuery();
						if(descrs.next())
						{
							//retTabSepStrBuff.append((descrs.getString(1)==null?" ":descrs.getString(1))).append("\t");
							retTabSepStrBuff.append("<lock_descr>").append("<![CDATA[" + (descrs.getString(1)==null?" ":descrs.getString(1)) +"]]>").append("</lock_descr>");
						}else{
							//retTabSepStrBuff.append("").append("\t");
							retTabSepStrBuff.append("<lock_descr>").append("<![CDATA[]]>").append("</lock_descr>");

						}
						descpstmt.close();
						descpstmt = null;
						descrs.close();
						descrs = null;

						//changed squence order of tran_id,line_no,line_no_sl y mahendra dated 16/APR/14
						retTabSepStrBuff.append("<tran_id>").append("<![CDATA[" + (rs1.getString(1)==null?" ":rs1.getString(1)) +"]]>").append("</tran_id>");
						//line_no
						//retTabSepStrBuff.append((rs1.getString(2)==null?" ":rs1.getString(2))).append("\t");
						retTabSepStrBuff.append("<line_no>").append("<![CDATA[" + (rs1.getString(2)==null?" ":rs1.getString(2)) +"]]>").append("</line_no>");
						//line_no_sl
						//retTabSepStrBuff.append((rs1.getString(3)==null?" ":rs1.getString(3))).append("\t");
						retTabSepStrBuff.append("<line_no_sl>").append("<![CDATA[" + (rs1.getString(3)==null?" ":rs1.getString(3)) +"]]>").append("</line_no_sl>");
						//hold_status

						//ADDED BY RITESH ON 25/DEC/13 FOR SET ITEM DESCR. AND LOCK CODE DESC. end	

						//site_code
						//retTabSepStrBuff.append((rs1.getString(7)==null?" ":rs1.getString(7))).append("\t");
						retTabSepStrBuff.append("<site_code>").append("<![CDATA[" + (rs1.getString(7)==null?" ":rs1.getString(7)) +"]]>").append("</site_code>");
						//status_date
						if(rs1.getTimestamp(8)!=null)
						{
							//retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(8).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
							retTabSepStrBuff.append("<status_date>").append("<![CDATA[" + (this.genericUtility.getValidDateString(rs1.getTimestamp(8).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</status_date>");
						}
						else
						{
							//retTabSepStrBuff.append(" ").append("\t");
							retTabSepStrBuff.append("<status_date>").append("<![CDATA[]]>").append("</status_date>");
						}
						//reas_code
						//retTabSepStrBuff.append((rs1.getString(9)==null?" ":rs1.getString(9))).append("\t");
						retTabSepStrBuff.append("<reas_code>").append("<![CDATA[" + (rs1.getString(9)==null?" ":rs1.getString(9)) +"]]>").append("</reas_code>");
						//sch_rel_date
						if(rs1.getTimestamp(10)!=null)
						{
							//retTabSepStrBuff.append(this.genericUtility.getValidDateString(rs1.getTimestamp(10).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())).append("\t");
							retTabSepStrBuff.append("<sch_rel_date>").append("<![CDATA[" + (this.genericUtility.getValidDateString(rs1.getTimestamp(10).toString(),this.genericUtility.getDBDateFormat(),this.genericUtility.getApplDateFormat())) +"]]>").append("</sch_rel_date>");
						}
						else
						{
							//retTabSepStrBuff.append(" ").append("\t");
							retTabSepStrBuff.append("<sch_rel_date>").append("<![CDATA[]]>").append("</sch_rel_date>");
						}
						//ref_ser
						//retTabSepStrBuff.append((rs1.getString(11)==null?" ":rs1.getString(11))).append("\t");
						retTabSepStrBuff.append("<ref_ser>").append("<![CDATA[" + (rs1.getString(11)==null?" ":rs1.getString(11)) +"]]>").append("</ref_ser>");
						//ref_id
						//retTabSepStrBuff.append((rs1.getString(12)==null?" ":rs1.getString(12))).append("\t");
						retTabSepStrBuff.append("<ref_id>").append("<![CDATA[" + (rs1.getString(12)==null?" ":rs1.getString(12)) +"]]>").append("</ref_id>");
						//ref_no
						//retTabSepStrBuff.append((rs1.getString(13)==null?" ":rs1.getString(13))).append("\t");			//added by ritesh on 27/05/13 
						retTabSepStrBuff.append("<ref_no>").append("<![CDATA[" + (rs1.getString(13)==null?" ":rs1.getString(13)) +"]]>").append("</ref_no>");

						// confirmed
						//retTabSepStrBuff.append((rs1.getString(14)==null?" ":rs1.getString(14))).append("\t");			//added by ritesh on 27/05/13 
						//remove confirmed column .changed by mahendra dated 01-AUG-2014
						//retTabSepStrBuff.append("<confirmed>").append("<![CDATA[" + (rs1.getString(14)==null?" ":rs1.getString(14)) +"]]>").append("</confirmed>");
						//remarks
						//retTabSepStrBuff.append((rs1.getString(18)==null?" ":rs1.getString(18))).append("\t");			//added by ritesh on 27/05/13 
						retTabSepStrBuff.append("<remarks>").append("<![CDATA[" + (rs1.getString(18)==null?" ":rs1.getString(18)) +"]]>").append("</remarks>");
						retTabSepStrBuff.append("<invoice_no>").append("<![CDATA[" + (invoiceNo==null?" ":invoiceNo) +"]]>").append("</invoice_no>");
						retTabSepStrBuff.append("<hold_quantity>").append("<![CDATA[" + (holdQuantity==null?" ":holdQuantity) +"]]>").append("</hold_quantity>");
						//retTabSepStrBuff.append("\n");				
						retTabSepStrBuff.append("</Detail2>");

						count++;
						System.out.println("#####Counter:["+count+"]");
					}//end if
				}//END WHILE
				rs1.close();
				pstmt.close();
				rs1=null;
				pstmt=null;
				retTabSepStrBuff.append("</Header0>");
				retTabSepStrBuff.append("</group0>");
				retTabSepStrBuff.append("</DocumentRoot>");

				if (count == 0 )
				{
					errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
				}

			}
			else
			{
				System.out.println("Sql:::::is Null ::::::::::::::");
				errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
			}

			System.out.println("retTabSepStrBuff:::["+retTabSepStrBuff.toString()+"]");



		}	//end of try	
		catch (SQLException e)
		{ 
			e.printStackTrace();
			System.out.println("SQLException :InvHoldRelGenEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :InvHoldRelGenEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{		
			try
			{		
				conn.close();
				conn = null;
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		if (getDataSql.trim().length()>0 && count > 0)
		{
			return retTabSepStrBuff.toString();	
		}
		else 
		{
			errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
			return errString;
		}
	}	


	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";
		Connection conn=null;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Connection conn= null;
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

				conn.setAutoCommit(false);	
			}
			//DatabaseMetaData dbmd = conn.getMetaData();

			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}

		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}

			retStr = process(xmlString, xmlString2, windowName, xtraParams,conn);

		}
		catch (Exception e)
		{

			System.out.println("Exception : InvHoldRelGenEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			retStr = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "",tranId="",sql="",errString="",lockCode="",holdStatus="";//xmlString="",
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ConnDriver connDriver = new ConnDriver();
		//Connection conn= null;
		int cnt=0,updCnt=0,ctr=0,count=0;
		String invHoldRelSql = "",invHoldRelDetSql;
		PreparedStatement pRelHdr = null;
		PreparedStatement pRelDet = null;
		java.sql.Timestamp chgDate = null,currDate = null;
		Date date = null;
		String chgTerm = "",autoConfirmed="";
		String userId = "",siteCode="",confirmed="",empCode="",tranIdGenerate="",lineNo="",remark="",tranIdRel="",lineNoRel="",remarks="";
		Document dom = null,headerDom=null,detailDom=null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		int parentNodeListLength = 0;
		int childNodeListLength = 0;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";

		boolean isConn= false;   // added by cpatil on 29/05/13
		String lotNo="";//Added by manoj dtd 15/04/2014 to update lotNo in inv_hold_rel table

		try
		{	
			System.out.println("method : process(xmlString, xmlString2, windowName, xtraParams,conn) called");
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("xmlString[process]::::::::::;;;"+xmlString);
				System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
				headerDom = genericUtility.parseString(xmlString); 
				detailDom= genericUtility.parseString(xmlString2);
				//System.out.println("::::::headerDom :::::[" + headerDom+"]");

				tranId = genericUtility.getColumnValue("tran_id",detailDom);
				autoConfirmed = genericUtility.getColumnValue("auto_confirmed",headerDom);
				remarks = genericUtility.getColumnValue("remarks",headerDom);
				System.out.println("remarks  :::"+remarks);
				if(autoConfirmed == null || autoConfirmed.trim().length() == 0 )
				{
					autoConfirmed = "Y";
				}
				System.out.println("tranId:["+tranId+"]:::::autoConfirmed:["+autoConfirmed+"]");
				//System.out.println("INV_HOLD_REL sql ");
				invHoldRelSql = "INSERT INTO INV_HOLD_REL(TRAN_ID, TRAN_DATE, SITE_CODE, REMARKS, CONFIRMED, EMP_CODE__APRV,CHG_USER ,CHG_DATE, CHG_TERM) VALUES(?,?,?,?,?,?,?,?,?)";
				pRelHdr = conn.prepareStatement(invHoldRelSql);

				//System.out.println("INV_HOLD_REL_DET sql ");
				invHoldRelDetSql = "INSERT INTO INV_HOLD_REL_DET(TRAN_ID, LINE_NO, TRAN_ID__HOLD, LINE_NO__HOLD, REMARKS) VALUES(?,?,?,?,?)";
				pRelDet = conn.prepareStatement(invHoldRelDetSql);

				currDate = new java.sql.Timestamp(System.currentTimeMillis());

				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
				siteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "site_code" );
				if (chgTerm == null || chgTerm.trim().length() == 0)
				{
					chgTerm = "SYSTEM";
				}
				confirmed = "N";
				empCode = "";

				SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
				String currDateStr = sdfAppl.format(currDate);
				Timestamp dateNew = Timestamp.valueOf(genericUtility.getValidDateString(currDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				System.out.println("dateNew["+dateNew+"]");
				tranIdGenerate = generateTranId( "w_inv_hold_rel", siteCode, currDateStr, conn );

				pRelHdr.setString( 1, tranIdGenerate );
				//pRelHdr.setTimestamp( 2, currDate );
				pRelHdr.setTimestamp( 2, dateNew );//Added by chandrashekar 01-10-2014
				pRelHdr.setString( 3, siteCode );
				pRelHdr.setString( 4, remarks);
				pRelHdr.setString( 5, confirmed );
				pRelHdr.setString( 6, empCode );
				pRelHdr.setString( 7, userId );
				//pRelHdr.setTimestamp( 8, currDate );
				pRelHdr.setTimestamp( 8, dateNew );//Added by chandrashekar 01-10-2014
				pRelHdr.setString( 9, chgTerm );

				updCnt = pRelHdr.executeUpdate();
				pRelHdr.clearParameters();
				pRelHdr.close();
				pRelHdr=null;
				if( updCnt > 0 )
				{					
					System.out.println( updCnt + "INV_HOLD_REL rows inserted successfully" );
				}
				// 09/09/12 manoharan commented and added new logical code
				/*lineNo = genericUtility.getColumnValue("line_no",detailDom);
			sql = "select count(*) from inv_hold_det where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				count = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;					

			if(count != 0)
			{
				ctr++;	
				sql = "select tran_id, line_no from inv_hold_det where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);

				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					tranIdRel = rs.getString("tran_id");
					lineNoRel = rs.getString("line_no");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;					

				pRelDet.setString( 1, tranIdGenerate );
				pRelDet.setString( 2,lineNo);
				pRelDet.setString( 3, tranIdRel );
				pRelDet.setString( 4, lineNoRel );
				pRelDet.setString( 5, remark );
				pRelDet.addBatch();
				//updCnt = pRelDet.executeUpdate();
				pRelDet.clearParameters();
				if( updCnt > 0 )
				{					
					System.out.println( updCnt + " INV_HOLD_REL_DET rows updated successfully" );
				}
			}
				 */
				// 09/09/12 manoharan commented upto here and added the following code
				/////////////////////////////////////////////////////////////////////////
				parentNodeList = detailDom.getElementsByTagName("Detail2");
				parentNodeListLength = parentNodeList.getLength(); 
				//System.out.println("parentNodeListLength------------------->"+parentNodeListLength);
				HashSet<String> lotSet=new HashSet<String>();
				for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
				{
					parentNode = parentNodeList.item(selectedRow);

					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("childNodeListLength---->>> "+ childNodeListLength);
					for (int childRow = 0; childRow < childNodeListLength; childRow++)
					{
						childNode = childNodeList.item(childRow);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName---->>> "+ childNodeName);
						if (childNodeName.equals("tran_id"))
						{
							tranId = childNode.getFirstChild().getNodeValue();
						}
						if (childNodeName.equals("line_no"))
						{
							lineNo = childNode.getFirstChild().getNodeValue();
						}
						if (childNodeName.equals("lot_no"))
						{
							lotNo = childNode.getFirstChild().getNodeValue();
							//lotSet.add(lotNo);
						}
					}
					ctr++;
					pRelDet.setString( 1, tranIdGenerate );
					pRelDet.setInt( 2,ctr);
					pRelDet.setString( 3, tranId );
					pRelDet.setString( 4, lineNo );
					pRelDet.setString( 5, remark );
					pRelDet.addBatch();
					//updCnt = pRelDet.executeUpdate();
					pRelDet.clearParameters();
				}
				/////////////////////////////////////////////////////////////////////////
				pRelDet.executeBatch();
				pRelDet.clearBatch();
				/*Iterator<String> itr=lotSet.iterator();
			String strLotNo="";
			while(itr.hasNext())
			{
				strLotNo+=itr.next()+",";
			}
			if(strLotNo.trim().length()>0)
			{
				strLotNo=strLotNo.substring(0, strLotNo.length()-1);	
			}
			lotSet.clear();
			pRelHdr=conn.prepareStatement("UPDATE INV_HOLD_REL SET LOT_INFO='"+strLotNo+"' WHERE TRAN_ID=? ");
			pRelHdr.setString(1, tranIdGenerate);
			pRelHdr.executeUpdate();*///commemnted by chandrashekar on 29-09-14


				if("Y".equalsIgnoreCase(autoConfirmed))
				{
					InvHoldRelConf invHoldRelConf =  new InvHoldRelConf();

					errString = invHoldRelConf.confirm(tranIdGenerate,xtraParams, "N" , conn,isConn);   // modify by cpatil on 29/05/13
					invHoldRelConf = null;
					if (errString.indexOf("VTCNFSUCC") > -1)
					{
						errString = "";
					}
					//sql = " update inv_hold set confirmed = 'Y', conf_date = ? where tran_id = ?";
					/*sql = "update inv_hold_det set  hold_status = 'R', status_date = ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,currDate);
				pstmt.setString(2,tranId);
				int rowcnt = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
					 */

				}
				if (errString == null || errString.trim().length() == 0)
				{
					conn.commit();
					System.out.println("inv_hold_det transaction hold_status release succesfully........");
					//	errString = itmDBAccessLocal.getErrorString("","VTMCONF2","");
					errString = itmDBAccessLocal.getErrorString("","VTCOMPL","","",conn);//Error message changed by Manoj dtd 02/09/2014
				}
				else
				{
					conn.rollback();
					System.out.println("Process failed........");
					//errString = itmDBAccessLocal.getErrorString("","VTDESNCONF","");
					errString = itmDBAccessLocal.getErrorString("","VTPRCERR","","",conn);//Error message changed by Manoj dtd 02/09/2014
				}
			}

		}
		catch (Exception e)
		{
			errString = e.getMessage();
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		finally
		{

			try
			{
				if(pRelHdr != null)
				{
					pRelHdr.close();
					pRelHdr = null;	
				}
				if(pRelDet != null)
				{
					pRelDet.close();
					pRelDet = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return errString;
	}

	private String generateTranId( String windowName, String siteCode, String tranDateStr, Connection conn )throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		String paySiteCode = "";
		String effectiveDate = "";
		java.sql.Date effDate = null;

		try
		{
			System.out.println("generateTranId() called");

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			//System.out.println("selSql :"+selSql);
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +		"<tran_id></tran_id>";
			xmlValues = xmlValues +		"<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +		"<tran_date>" + tranDateStr + "</tran_date>"; 
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);

		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +selSql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
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
			catch(Exception e)
			{
				//errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return tranId;
	}



}
