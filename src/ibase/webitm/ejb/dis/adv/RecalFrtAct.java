/********************************************************
	Title : RecalFrtAct
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/

package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless; 

@Stateless
public class RecalFrtAct extends ActionHandlerEJB implements RecalFrtActLocal, RecalFrtActRemote
{
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;

		System.out.println(".......tranId......."+tranId);
		System.out.println(".......xtraParams..."+xtraParams);
		System.out.println(".......forcedFlag..."+forcedFlag);
		if(tranId!=null && tranId.trim().length() > 0 )
		{
			returnString = recalFrtActShipment(tranId,xtraParams,forcedFlag);
		}
		return returnString;
	}	
	public String recalFrtActShipment(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("RecalFrtAct called........");
		String sql = "";
		String errString = "" ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessLocal = null;
		System.out.println("tran id = "+tranId);
		Timestamp sysdate = null;
		String confirmed="";		
		ConnDriver connDriver = new ConnDriver();


		try
		{

			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			String userID = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "LoginCode" );
			System.out.println(" userID ["+userID+"]");

			itmDBAccessLocal = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			connDriver = null;
			conn.setAutoCommit(false);
			genericUtility =new  ibase.utility.E12GenericUtility();
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			sysdate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");

			sql = " select confirmed from   shipment  where  shipment_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				confirmed = rs.getString("confirmed");
			}
			System.out.println("@@@@confirmed["+confirmed+"]tranId["+tranId+"]");
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if( "Y".equalsIgnoreCase(confirmed))
			{
				errString = itmDBAccessLocal.getErrorString("","VTDIST26","","",conn);
				return errString;
			}
			else
			{
				String errCode = recalFrtAct(tranId,1, xtraParams,conn);
				System.out.println("@@@@ recalFrtAct::errCode["+errCode+"]");
				if( errCode != null && errCode.trim().length() > 0 )
				{
					conn.rollback();
					System.out.println("@@@@@@@@@ recalculate failed...........");
					errString = itmDBAccessLocal.getErrorString("",errCode,"","",conn);
				}
				else
				{
					conn.commit();
					System.out.println("@@@@@@@@@ recalculate successfully...........");
					errString = itmDBAccessLocal.getErrorString("","VMRECALSUC","","",conn);
				}
			}
		} 
		catch( Exception e)
		{			
			try 
			{
				conn.rollback(); 
				System.out.println("Exception.. "+e.getMessage());
				e.printStackTrace();	
				errString=e.getMessage();
				throw new ITMException(e);
			} 
			catch (SQLException ex) 
			{
				ex.printStackTrace();
				errString=ex.getMessage();
				throw new ITMException(ex);
			}
		}
		finally
		{		
			try
			{

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



	private String recalFrtAct(String tranId, int i, String xtraParams, Connection conn)
			throws RemoteException, ITMException

			{
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		int cnt = 0,updCnt=0;
		String retString = "", sql = "", sql1 = "",errCode = "";
		Timestamp sysDate = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();		
		int sequence=0;
		double frtAmt=0;

		String freightType="",chargesMode="",lineNo="",chargeCodeAdd="",chargeCode="",chargeAdd1="",chargeAdd="";
		double grossWeightDesp=0,grossWeightDist=0,val=0,freightRate=0,minValue=0,addChgs=0,chargeRate=0,tempNetWeight=0,tempNettWeight=0;

		double grossWeightCIss=0;

		try 
		{
			System.out.println("@@@@@@@@@@@@@@@ recalFrtAct method called next..............");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

			sql = " select freight_type,min_value,freight_rate from shipment  where  shipment_id = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				freightType = rs.getString("freight_type");
				minValue = rs.getDouble("min_value");
				freightRate = rs.getDouble("freight_rate");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@tranId["+tranId+"]freightType["+freightType+"]minValue["+minValue+"]freightRate["+freightRate+"]");


			if("G".equalsIgnoreCase(freightType))   // ls_frt_type = 'G' then
			{	
				sql = " select nvl(sum(gross_weight),0) " +
						" from despatch where shipment_id = ? 	and confirmed = 'Y' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if( rs.next())
				{
					grossWeightDesp = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("@@@@@@@@grossWeightDesp["+grossWeightDesp+"]");

				sql = "	select nvl(sum(gross_weight),0) " +
						" from distord_iss where shipment_id = ? and confirmed = 'Y' ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if( rs.next())
				{
					grossWeightDist = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("@@@@@@@@grossWeightDist["+grossWeightDist+"]");
				// added cpatil for c-iss on 19/04/14

				sql = "	 select nvl(sum(gross_weight),0)   from   CONSUME_ISS " +
						"  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) and confirmed = 'Y' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if( rs.next())
				{
					grossWeightCIss = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("@@@@@@@@grossWeightCIss["+grossWeightCIss+"]");
				// end cpatil

				//lc_val = lc_frt_rate * (lc_gross + lc_gross_dist)
				val = freightRate * ( grossWeightDesp + grossWeightDist + grossWeightCIss );   // added grossWeightCIss for c-iss recalculation

				System.out.println("@@@@@@@val["+val+"]minValue["+minValue+"]");

				if( val > minValue )  // lc_val > lc_minval then
				{	
					sql = " update shipment set freight_amt = ? where  shipment_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, val);
					pstmt.setString(2, tranId);
					updCnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					System.out.println("updCnt["+updCnt+"]");
					frtAmt = val;
				}
				else
				{	
					sql = " update shipment set freight_amt = ? where  shipment_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1, minValue);
					pstmt.setString(2, tranId);
					updCnt = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					System.out.println("updCnt["+updCnt+"]");
					frtAmt = minValue;

				} //end if

				sql = " select add_chgs from shipment  where  shipment_id = ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if( rs.next())
				{
					addChgs = rs.getDouble("add_chgs");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("@@@@add_chgs["+addChgs+"]");

				val = frtAmt + addChgs;

				System.out.println("@@@@@@@@ val["+val+"]");

				sql = " update shipment set total_freight = ? where  shipment_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1, val);
				pstmt.setString(2, tranId);
				updCnt = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;

			}
			else if("F".equalsIgnoreCase(freightType))  // ls_frt_type = 'F' then
			{		

				sql = " select freight_amt from shipment  where  shipment_id = ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if( rs.next())
				{
					frtAmt = rs.getDouble("freight_amt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("@@@@frtAmt["+frtAmt+"]");	

			}//		end if

			sql = " select line_no,charge_code,charges_mode,charge_rate,charge_code__add,sequence" +
					" from shipment_det  where  shipment_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while( rs.next())
			{

				lineNo = rs.getString("line_no");
				chargeCode = rs.getString("charge_code");
				chargesMode = rs.getString("charges_mode");
				chargeRate = rs.getDouble("charge_rate");
				chargeCodeAdd = rs.getString("charge_code__add");
				sequence = rs.getInt("sequence");

				System.out.println("@@@@@@@@@ chargesMode["+chargesMode+"]chargeCodeAdd["+chargeCodeAdd+"]chargeRate["+chargeRate+"]");
				if("P".equalsIgnoreCase(chargesMode)) //ls_mode = 'P' then//percentage on basic freight
				{
					if( chargeCodeAdd != null )  // not isnull(ls_chgs_add) then
					{
						//i= 1
						val = 0;
						String[] tempChargeAdd;
						double tempAmount=0;
						chargeAdd1 = chargeCodeAdd;
						tempChargeAdd = chargeAdd1.split(",");
						for( i = 0; i < tempChargeAdd.length ; i++ )
						{
							sql1 = " select amount " +
									" from 	 shipment_det where  shipment_id =  ?  " +
									" and    charge_code =  ?  ";
							pstmt1 =  conn.prepareStatement(sql1);
							pstmt1.setString(1,tranId  );
							pstmt1.setString(2,tempChargeAdd[i] );
							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{
								tempAmount = rs1.getDouble("amount");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close(); 
							pstmt1 = null;		

							val = val + tempAmount;
						}
						val = val + frtAmt;
					}
					else
					{	
						val = frtAmt;
					} // end if
					System.out.println("@@@@@@ val["+val+"]chargeRate["+chargeRate+"]");
					val = ( val * chargeRate)/100 ;
					System.out.println("@@@@@@ val["+val+"]");
				}
				else if("W".equalsIgnoreCase(chargesMode))  // ls_mode = 'W' then//weight
				{
					sql1 = " select nvl(sum(nett_weight),0) " +
							" from despatch where shipment_id = ? 	and confirmed = 'Y' ";
					pstmt1 =  conn.prepareStatement(sql1);
					pstmt1.setString(1,tranId  );
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						tempNettWeight = rs1.getDouble(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1 = null;	
					System.out.println("@@@@@@@@tempNettWeight["+tempNettWeight+"]");
					

					sql1 = " select nvl(sum(net_weight),0) " +
							" from distord_iss  where shipment_id = ? 	and confirmed = 'Y' ";

					pstmt1 =  conn.prepareStatement(sql1);
					pstmt1.setString(1,tranId  );
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						tempNetWeight = rs1.getDouble(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1 = null;	
					System.out.println("@@@@@@@@tempNetWeight["+tempNetWeight+"]");
					
					// added cpatil for c-iss on 19/04/14

					sql1 = "	 select nvl(sum(gross_weight),0)   from   CONSUME_ISS " +
							"  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) and confirmed = 'Y' ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, tranId);
					rs1 = pstmt1.executeQuery();
					if( rs1.next())
					{
						grossWeightCIss = rs1.getDouble(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("@@@@@@@@grossWeightCIss["+grossWeightCIss+"]");

					// end cpatil


					val = ( tempNettWeight + tempNetWeight + grossWeightCIss ) * chargeRate ;
					
					System.out.println("@@@@@@ val["+val+"]chargeRate["+chargeRate+"]");

				} //	end if

				if( "W".equalsIgnoreCase(chargesMode) || "P".equalsIgnoreCase(chargesMode) ) 
					//ls_mode = 'W' or ls_mode = 'P' then
				{		

					sql1 = " update shipment_det set charge_code = ?, sequence = ?, charges_mode= ?, charge_rate= ?," +
							" charge_code__add= ? , amount=?   where  shipment_id = ? and  line_no = ?  ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, chargeCode);
					pstmt1.setInt(2, sequence);
					pstmt1.setString(3, chargesMode);
					pstmt1.setDouble(4, chargeRate);
					pstmt1.setString(5, chargeCodeAdd);
					pstmt1.setDouble(6, val);
					pstmt1.setString(7, tranId);
					pstmt1.setString(8, lineNo);
					updCnt = pstmt1.executeUpdate();
					pstmt1.close();
					pstmt1 = null;

					System.out.println("@@@@@ updCnt["+updCnt+"]");

				}  //		next

			}  // end while

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		}  // end try
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}

		return errCode;
			}





}