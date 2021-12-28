/* Name of Developer : Nisar S. Khatib */
package ibase.webitm.ejb.dis.adv;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.ejb.dis.CreateDistReceipt;
import ibase.webitm.ejb.dis.StockUpdate;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.InsuranceUpdate;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import javax.ejb.*;
import org.w3c.dom.*;
import java.sql.Timestamp;
import java.util.HashMap;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class DistIssueConfirmAct extends ActionHandlerEJB
{
	FinCommon fcom = new FinCommon();
	//GenericUtility genericUtility = null;
	E12GenericUtility genericUtility = new E12GenericUtility();

	/*public void ejbCreate() throws RemoteException, CreateException
	{
		System.out.println("INSIDE DistIssueConfirmActEJB CREATE ");
	}

	public void ejbRemove()
	{
	}

	public void ejbActivate()
	{
	}

	public void ejbPassivate()
	{
	}*/

	public String actionHandler(String custCode, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String  retString = null;
		try
		{
			retString = actionConfirm(custCode, xtraParams, forcedFlag);
		}

		catch(Exception e)
		{
			System.out.println("Exception :DistIssueConfirmActEJB :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return retString;
	}
	public String actionConfirm(String tranId, String xtraParams, String forcedFlag) throws Exception,ITMException
	{
		Connection conn=  null;
		String errString = "";
		try
		{
			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				//Changed by Poonam Gole for changing connection object :Start
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection() ;
				//Changed by Poonam Gole for changing connection object :End
				connDriver = null;
				errString = actionConfirm(tranId, xtraParams, forcedFlag, conn);
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			System.out.println("The Exception occure in DistIssueConfirmActEJB :"+e1);
		}
		return errString;

	}
	public String actionConfirm(String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{

		String sql="";
		String updFlag = null;
		String countCode= null,stateCode = null;
		String city = null,stanCode = "";
		String custCode = null;
		String errString = "";
		String prvPolicyNo = "";
		String asAvailable = null;
		String mLocCodeGit = null,lsTranType=null,lsSiteCode = null;
		String lsRdPermitNo = null;
		String mDistOrder = null,distOrder = null,policyNo=null;
		Timestamp mTranDate = null,ldtRetestDate=null;
		//Connection conn=  null;
		PreparedStatement pstmt = null,pstmtForInsert = null,pstmtForUpdate = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		int no = 0,i=0,counter=0,mLineNo = 0,mLineDist = 0,llNoArt=0;
		String mItemCode = null,mUnit = null,mLocCode = null,mPackCode=null;
		String mLotNoDist= null,mLotSlDist=null,lsBatchNo=null,lsGrade=null;
		double mQuantity = 0,mRate=0,lcGrossWeight=0,lcTareWeight=0,lcNetWeight=0;
		double mTotStk = 0;

		String mLotNo=null,mLotSl=null,mQtyStk;
		String mItemSer=null,mSiteCdMfg=null,mGrade=null;
		Timestamp mmfgDate=null,mlTranDate=null,mExpDate=null;
		String tranDate = null;
		String mRemarks=null,mInvStat=null;
		String lcAcctCodeInv=null,lsCctrCodeInv=null,lsAcctCodeOh=null,lsCctrCodeOh=null;
		String lsUnitAlt=null,lsPackInstr=null;
		String lsTranTypeParent=null,lsIssCriteria=null,mSiteCode=null,lsAcctCodeInv=null;
		String lsTranStat = null,lsStdPost=null,lsInvAcct=null;
		String lsPriceListCost = null;
		String lsPriceList = null;
		String lsPolicyNo = null,lsCurrCd = null,errcode = null;
		String lsTranId = null,lsAgentCode=null,lsTranSer=null;
		String errCode = null,lsAutoRcp=null,siteCodeISS ="";
		String xmlString = null;

		//genericUtility = GenericUtility.getInstance();

		double ldNetAmt = 0;
		double lcExch = 0;
		double mPotencyPerc=0;
		double lcQtyConfirm = 0,lcOverShip=0,lcQtyShipped=0,lcBalQty=0;
		double mQtyStock = 0,lcRate=0;
		double lcCounQtyStduom=0,lcCostRate=0;

		int mcnt = 0;


		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

		try
		{
			String empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("loginCode.............."+chgUser);
			String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			String siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("Xtra Params  \n " +xtraParams );
			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				//Changed by Poonam Gole for changing connection object :Start
		//conn = connDriver.getConnectDB("DriverITM");
		conn = getConnection() ;
		//Changed by Poonam Gole for changing connection object :End
				connDriver = null;
			}

			sql="SELECT	SITE_CODE,LOC_CODE__GIT, TRAN_DATE, TRAN_TYPE, SITE_CODE__DLV , RD_PERMIT_NO,AVAILABLE_YN "
			+" FROM DISTORD_ISS WHERE TRAN_ID = '" + tranId + "'";

			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteCodeISS = rs.getString("SITE_CODE");
				mLocCodeGit = rs.getString("LOC_CODE__GIT");
				mTranDate = rs.getTimestamp("TRAN_DATE");
				lsTranType = rs.getString("TRAN_TYPE");
				lsSiteCode = rs.getString("SITE_CODE__DLV");
				lsRdPermitNo = rs.getString("RD_PERMIT_NO");
				asAvailable  = rs.getString("AVAILABLE_YN");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			sql=" SELECT COUNT(DISTINCT DIST_ORDER)AS COUNTER FROM DISTORD_ISSDET "
			+" WHERE TRAN_ID = '" + tranId + "' ";
			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				counter = rs.getInt("COUNTER");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("[DistIssueConfirmActEJB] counter=>"+counter);

			if(counter > 1)
			{

				sql="SELECT COUNT(1)AS COUNTER FROM DISTORDER "
				+" WHERE DIST_ORDER IN (SELECT DISTINCT DIST_ORDER FROM DISTORD_ISSDET "
				+" WHERE TRAN_ID = '" + tranId + "' ) AND POLICY_NO IS NOT NULL ";

				//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					counter = rs.getInt("COUNTER");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(counter > 0)
				{
					sql=" SELECT DISTINCT DIST_ORDER FROM DISTORD_ISSDET "
					+" WHERE TRAN_ID = '" + tranId + "' ";
					//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						distOrder = rs.getString("DIST_ORDER");
						sql="SELECT POLICY_NO FROM DISTORDER WHERE DIST_ORDER = '" + distOrder + "'";
						//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
						pstmt1 = conn.prepareStatement(sql);
						//pstmt1.setString(1,distOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							policyNo = rs1.getString("POLICY_NO")==null?"":rs1.getString("POLICY_NO");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if(prvPolicyNo.trim().length() == 0)
						{
							prvPolicyNo = policyNo;
						}
						if(!prvPolicyNo.equalsIgnoreCase(policyNo))
						{
							System.out.println("[DistIssueConfirmActEJB]Policy No. not Same(VTPONOSAME)");
							errString = itmDBAccessEJB.getErrorString("","VTPONOSAME",chgUser,"",conn);
							break ;
						}
					}//end of while(rs.next())
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}//end of if(counter > 0)
			}//END OF IF(counter > 1)

			sql="SELECT LINE_NO, DIST_ORDER, LINE_NO_DIST_ORDER, ITEM_CODE, QUANTITY,"
			+" UNIT, LOC_CODE, PACK_CODE, RATE, LOT_NO, LOT_SL, BATCH_NO, GRADE,"
			+" RETEST_DATE,GROSS_WEIGHT,TARE_WEIGHT,NET_WEIGHT,NO_ART "
			+" FROM DISTORD_ISSDET "
			+" WHERE trim(TRAN_ID) = '" + tranId.trim() + "' "
			+" ORDER BY LINE_NO";

			System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();

			while(rs.next())
			{
				HashMap stkUpdMap = new HashMap();
				
				mLineNo = rs.getInt("LINE_NO");
				mDistOrder = rs.getString("DIST_ORDER");
				mLineDist = rs.getInt("LINE_NO_DIST_ORDER");
				mItemCode = rs.getString("ITEM_CODE");
				mQuantity = rs.getDouble("QUANTITY");
				mUnit = rs.getString("UNIT");
				mLocCode = rs.getString("LOC_CODE");
				mPackCode = rs.getString("PACK_CODE");
				mRate = rs.getDouble("RATE");
				mLotNoDist= rs.getString("LOT_NO");
				mLotSlDist = rs.getString("LOT_SL");
				System.out.println( "mLotNoDist :: " + mLotNoDist );
				System.out.println( "mLotSlDist :: " + mLotSlDist );
				lsBatchNo = rs.getString("BATCH_NO");
				lsGrade = rs.getString("GRADE");
				ldtRetestDate = rs.getTimestamp("RETEST_DATE");
				lcGrossWeight = rs.getDouble("GROSS_WEIGHT");
				lcTareWeight = rs.getDouble("TARE_WEIGHT");
				lcNetWeight = rs.getDouble("NET_WEIGHT");
				llNoArt = rs.getInt("NO_ART");

				sql="SELECT QTY_CONFIRM     , OVER_SHIP_PERC  , QTY_SHIPPED "
				+" FROM   DISTORDER_DET "
				+" WHERE  DIST_ORDER = '" + mDistOrder + "' "
				+" AND LINE_NO = ? ";

				//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				//pstmt1.setString(1,mDistOrder);
				pstmt1.setInt( 1, mLineDist );
				rs1 = pstmt1.executeQuery();

				if(rs1.next())
				{
					lcQtyConfirm = rs1.getDouble("QTY_CONFIRM");
					lcOverShip = rs1.getDouble("OVER_SHIP_PERC");
					lcQtyShipped = rs1.getDouble("QTY_SHIPPED");

					lcBalQty =(lcQtyConfirm+(lcQtyConfirm * (lcOverShip/100)))-lcQtyShipped;

					System.out.println("lcBalQty=======>"+lcBalQty);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				sql="SELECT TRAN_TYPE__PARENT FROM DISTORDER_TYPE "
				+" WHERE TRAN_TYPE = '" + lsTranType + "' ";
				//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				//pstmt1.setString(1,lsTranType);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lsTranTypeParent = rs1.getString("TRAN_TYPE__PARENT");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;


				sql="SELECT ISS_CRITERIA FROM ITEM "
				+" WHERE ITEM_CODE = '" + mItemCode + "'";

				System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				//pstmt1.setString(1,mItemCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lsIssCriteria = rs1.getString("ISS_CRITERIA");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				if((mQuantity > lcBalQty) && (lsTranType!=null && lsTranType.equals(lsTranTypeParent)) && !lsIssCriteria.equalsIgnoreCase("W"))
				{
					System.out.println("[DistIssueConfirmActEJB]Error(VTDIST19)");
					errString = itmDBAccessEJB.getErrorString("","VTDIST19",chgUser,"",conn);
					break ;
				}

				sql="SELECT COUNT(A.ITEM_CODE) as MCNT FROM STOCK A, INVSTAT B, LOCATION C "
				+" WHERE B.INV_STAT  = C.INV_STAT "
				+" AND C.LOC_CODE = A.LOC_CODE "
				+" AND A.ITEM_CODE = '" + mItemCode + "' "
				+" AND A.SITE_CODE = '" + siteCodeISS + "' "
				+" AND A.LOC_CODE  = '" + mLocCode + "' "
				+" AND A.LOT_NO    = '" + mLotNoDist + "' "
				+" AND A.LOT_SL    = '" + mLotSlDist + "' "
				+" AND B.AVAILABLE = '" + asAvailable + "'"
				+" AND B.USABLE    = '" + asAvailable + "'";

				System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				//pstmt1.setString(1,mItemCode);
				//pstmt1.setString(2,siteCodeISS);
				//pstmt1.setString(3,mLocCode);
				//pstmt1.setString(4,mLotNoDist);
				//pstmt1.setString(5,mLotSlDist);
				
				//pstmt1.setString(1,asAvailable);
				//pstmt1.setString(2,asAvailable);

				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					mcnt = rs1.getInt("MCNT");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				if(mcnt == 0)
				{
					System.out.println("( Rajendra VTDIST16)");
					//errString = itmDBAccessEJB.getErrorString("","VTDIST16",chgUser,"",conn);
					errString = itmDBAccessEJB.getErrorString("","VTDIST16","BASE","",conn);
					return errString;
				}

/*				sql="SELECT QUANTITY,RATE FROM STOCK "
					+" WHERE ITEM_CODE = '"+mItemCode+"'"
					+" AND SITE_CODE = '"+siteCodeISS+"'"
					+" AND LOC_CODE  = '"+mLocCode+"'"
					+" AND LOT_NO    = '"+mLotNoDist+"'"
					+" AND LOT_SL    = '"+mLotSlDist+"'";

				System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					mQtyStock = rs1.getDouble("QUANTITY");
					lcRate = rs1.getDouble("RATE");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
*/
				sql=" SELECT A.LOT_NO, A.LOT_SL, A.QUANTITY, A.EXP_DATE, "
				+" A.UNIT, A.ITEM_SER,A.MFG_DATE, A.SITE_CODE__MFG, A.GRADE,"
				+" A.LTRAN_DATE, A.REMARKS,A.INV_STAT, A.POTENCY_PERC ,"
				+" A.ACCT_CODE__INV, A.CCTR_CODE__INV,A.ACCT_CODE__OH,"
				+" A.CCTR_CODE__OH, A.UNIT__ALT, A.PACK_INSTR,"
				+" A.CONV__QTY_STDUOM,A.QUANTITY,A.RATE  "
				+" FROM STOCK A, INVSTAT B "
				+" WHERE A.INV_STAT  = B.INV_STAT "
				+" AND A.ITEM_CODE = '" + mItemCode + "' "
				+" AND A.SITE_CODE = '" + siteCodeISS + "' "
				+" AND A.LOC_CODE  = '" + mLocCode + "' "
				+" AND A.LOT_NO    = '" + mLotNoDist + "' "
				+" AND A.LOT_SL    = '" + mLotSlDist + "' ";

				System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				//pstmt1.setString(1,mItemCode);
				//pstmt1.setString(2,siteCodeISS);
				//pstmt1.setString(3,mLocCode);
				//pstmt1.setString(4,mLotNoDist);
				//pstmt1.setString(5,mLotSlDist);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					mQtyStock = rs1.getDouble("QUANTITY");
					lcRate = rs1.getDouble("RATE");

					mLotNo = rs1.getString("LOT_NO");
					mLotSl= rs1.getString("LOT_SL");
					mQtyStk= rs1.getString("QUANTITY");
					mExpDate= rs1.getTimestamp("EXP_DATE");
					mUnit= rs1.getString("UNIT");
					mItemSer= rs1.getString("ITEM_SER");
					mmfgDate= rs1.getTimestamp("MFG_DATE");
					mSiteCdMfg= rs1.getString("SITE_CODE__MFG");
					mGrade= rs1.getString("GRADE");
					mlTranDate= rs1.getTimestamp("LTRAN_DATE");
					mRemarks= rs1.getString("REMARKS");
					mInvStat= rs1.getString("INV_STAT");
					mPotencyPerc= rs1.getDouble("POTENCY_PERC");
					lsAcctCodeInv= rs1.getString("ACCT_CODE__INV");
					lsCctrCodeInv= rs1.getString("CCTR_CODE__INV");
					lsAcctCodeOh= rs1.getString("ACCT_CODE__OH");
					lsCctrCodeOh= rs1.getString("CCTR_CODE__OH");
					lsUnitAlt= rs1.getString("UNIT__ALT");
					lsPackInstr= rs1.getString("PACK_INSTR");
					lcCounQtyStduom = rs1.getDouble("CONV__QTY_STDUOM");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				System.out.println("Manohar Stock Line no=>"+mLineNo+"Site : "+lsSiteCode+" Item : "+mItemCode+" Loc : "+mLocCode+"~r~n"+" Lot No : "+mLotNoDist+" Lot Sl : "+mLotSlDist+"~r~n"+" Available Stk : "+mQtyStock+" Issue Qty : "+mQuantity);
				//if(mTotStk < mQuantity)
				if(mQtyStock < mQuantity)
				{
					System.out.println("Less Stock Line no=>"+mLineNo+"Site : "+lsSiteCode+" Item : "+mItemCode+" Loc : "+mLocCode+"~r~n"+" Lot No : "+mLotNoDist+" Lot Sl : "+mLotSlDist+"~r~n"+" Available Stk : "+mQtyStock+" Issue Qty : "+mQuantity);
					errString = itmDBAccessEJB.getErrorString("","VTDIST4",chgUser,"",conn);
					break ;
				}

				stkUpdMap.put("site_code",siteCodeISS);
				stkUpdMap.put("item_code",mItemCode);
				stkUpdMap.put("loc_code",mLocCode);
				stkUpdMap.put("lot_no",mLotNo);
				stkUpdMap.put("lot_sl",mLotSl);
				stkUpdMap.put("unit",mUnit);
				stkUpdMap.put("quantity",Double.toString(mQuantity));
				if(lcCounQtyStduom == 0)
				{
					lcCounQtyStduom = 1;
				}
				stkUpdMap.put("qty_stduom",Double.toString(lcCounQtyStduom * mQuantity));
				stkUpdMap.put("tran_type","ID");
				stkUpdMap.put("tran_date",mTranDate);
				stkUpdMap.put("tran_ser","D-ISS");
				stkUpdMap.put("tran_id",tranId);
				stkUpdMap.put("line_no",new Integer(mLineNo));
				stkUpdMap.put("rate",Double.toString(0));
				stkUpdMap.put("gross_rate",Double.toString(0));
				stkUpdMap.put("sitecode_mfg",mSiteCdMfg);
				stkUpdMap.put("potency_perc",Double.toString(0));
				stkUpdMap.put("mfg_date",mmfgDate);
				stkUpdMap.put("acct_code_inv",lsAcctCodeInv);
				stkUpdMap.put("cctr_code_inv",lsCctrCodeInv);
				stkUpdMap.put("rate_oh",Double.toString(0));
				stkUpdMap.put("acct_code_oh",lsAcctCodeOh);
				stkUpdMap.put("cctr_code_oh",lsCctrCodeOh);
				stkUpdMap.put("dimension"," ");
				stkUpdMap.put("no_art",Double.toString(1));
				stkUpdMap.put("grade",mGrade);
				stkUpdMap.put("batch_no",lsBatchNo);
				StockUpdate stkUpd =  new StockUpdate();
				errString = stkUpd.updateStock(stkUpdMap,xtraParams,conn);
				System.out.println("Returning Result "+errString);
				stkUpd =  null;
				stkUpdMap.clear();

				if(errString.trim().length()>0 && errString.indexOf("Error") != -1)
				{
					return errString;
				}
				else
				{
					sql=" SELECT INV_STAT FROM LOCATION "
					+" WHERE LOC_CODE = '" + mLocCodeGit + "' ";

					//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

					pstmt1 = conn.prepareStatement(sql);
					//pstmt1.setString(1,mLocCodeGit);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						lsTranStat = rs1.getString("INV_STAT");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					lsStdPost = fcom.getFinparams("999999","DIST_ISS_RCP_POST",conn);
					if(lsStdPost.equals("NULLFOUND"))
					{
						lsStdPost="A";
					}
					lsInvAcct = fcom.getFinparams("999999","INV_ACCT_DISS",conn);
					if(lsInvAcct.trim().length()==0)
					{
						lsInvAcct="N";
					}
					if(lsStdPost.equals("S") && lsInvAcct.equals("Y"))
					{
						sql="SELECT B.PRICE_LIST__COST "
						+" FROM ITEM A , ITEMSER B "
						+" WHERE A.ITEM_SER  = B.ITEM_SER "
						+" AND A.ITEM_CODE = '" + mItemCode + "' ";

						System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

						pstmt1 = conn.prepareStatement(sql);
						//pstmt1.setString(1,mItemCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							lsPriceListCost = rs1.getString("PRICE_LIST__COST")==null?"":rs1.getString("PRICE_LIST__COST");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if(lsPriceListCost.trim().length()==0)
						{
							lsPriceList = fcom.getFinparams("999999","STD_COST_PRICE",conn);
						}
						else
						{
							lsPriceList = lsPriceListCost;
						}

						if(lsInvAcct.equals("NULLFOUND"))
						{
							System.out.println("'VTFINPARM~tVariabe STD_COST_PRICE not defined under Financial Variables'");
						}
						if(lsPriceList.trim().length() > 0)
						{
							String trDateSrt = genericUtility.getValidDateString(mTranDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() );
							lcCostRate = new DistCommon().pickRate(lsPriceList,trDateSrt,mItemCode,"NULL","L",mQuantity,conn);
						}
					}//end of if(lsStdPost.equals("S") && lsInvAcct.equals("Y"))
					else if(lsStdPost.equals("S") && lsInvAcct.equals("N"))
					{
						lcCostRate = 0.00;
					}
					else if(lsStdPost.equals("A"))
					{
						lcCostRate = lcRate;
					}

					sql="UPDATE DISTORD_ISSDET "
					+" SET COST_RATE =  ? "
					+" WHERE TRAN_ID = '" + tranId + "' "
					+" AND LINE_NO   = ? "
					+" AND ITEM_CODE = '" + mItemCode + "' "
					+" AND LOC_CODE  = '" + mLocCode + "' "
					+" AND LOT_NO	  = '" + mLotNo + "' "
					+" AND LOT_SL	  = '" + mLotSl + "' ";

					System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setDouble(1,lcCostRate);
					//pstmt1.setString(2,tranId);
					pstmt1.setInt(2,mLineNo);
					//pstmt1.setString(4,mItemCode);
					//pstmt1.setString(5,mLocCode);
					//pstmt1.setString(6,mLotNo);
					//pstmt1.setString(7,mLotSl);

					no = pstmt1.executeUpdate();
					pstmt1 = null;
				}
				if(!lsTranType.equals(lsTranTypeParent))
				{
					sql="UPDATE DISTORD_ISSDET "
					+" SET QTY_RETURN = CASE WHEN QTY_RETURN IS NULL THEN 0 "
					+" ELSE QTY_RETURN END + ?"
					+" WHERE DIST_ORDER = '" + mDistOrder + "' "
					+" AND LINE_NO = ? ";

				}//end of if(!lsTranType.equals(lsTranTypeParent))
				else
				{
					sql=" UPDATE DISTORDER_DET "
					+" SET QTY_SHIPPED = CASE WHEN QTY_SHIPPED IS NULL THEN 0 "
					+" ELSE QTY_SHIPPED END + ? "
					+" WHERE DIST_ORDER = '" + mDistOrder + "' "
					+" AND LINE_NO = ? ";

				}
				//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setDouble(1,mQuantity);
				//pstmt1.setString(2,mDistOrder);
				pstmt1.setInt(2,mLineDist);

				no = pstmt1.executeUpdate();
				pstmt1 = null;
			}//end of while(rs.next())
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			HashMap insUpdHMap = new HashMap();

			sql="SELECT POLICY_NO	 ,	CURR_CODE	,	EXCH_RATE "
			+" FROM DISTORDER WHERE DIST_ORDER = '" + mDistOrder + "' ";

			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,mDistOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				lsPolicyNo = rs.getString("POLICY_NO");
				lsCurrCd = rs.getString("CURR_CODE");
				lcExch = rs.getDouble("EXCH_RATE");

				sql="SELECT TRAN_ID	,AGENT_CODE "
				+" FROM INSURANCE WHERE POLICY_NO = ? ";

				//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,lsPolicyNo);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lsTranId = rs1.getString("TRAN_ID") == null ? " " :rs1.getString("TRAN_ID");
					lsAgentCode = rs1.getString("AGENT_CODE");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}//end of if(rs.next())
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql="SELECT NET_AMT FROM DISTORD_ISS WHERE TRAN_ID = '" + tranId + "' ";
			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ldNetAmt = rs.getDouble("NET_AMT");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			lsTranId = lsTranId == null ? "" :lsTranId ;
			if(lsTranId.trim().length()  > 0)
			{
				insUpdHMap.put("tran_id__ins",lsTranId);
				insUpdHMap.put("doc_no",tranId);
				insUpdHMap.put("doc_date",mTranDate);
				insUpdHMap.put("ref_ser",lsTranSer);
				insUpdHMap.put("doc_value",new Double(ldNetAmt));
				insUpdHMap.put("curr_code",lsCurrCd);

				if(lsCurrCd==null)
				{
					errString = itmDBAccessEJB.getErrorString("","VTCURRCD1",chgUser,"",conn);
					return errString;
				}
				insUpdHMap.put("exch_rate",lsCurrCd);

				if(lsCurrCd.trim().length()==0)
				{
					errString = itmDBAccessEJB.getErrorString("","VTEXCH1",chgUser,"",conn);
					return errString;
				}
				insUpdHMap.put("doc_type","I");
				insUpdHMap.put("cert_no",lsPolicyNo);
				insUpdHMap.put("bulk","Y");

				String docNo = null,certNo = null;
				int llCnt = 0;

				sql=" SELECT COUNT(*)as COUNTER FROM INSURANCE_DET "
				+" WHERE TRAN_ID__INS = '" + lsTranId + "' AND "
				+" REF_SER = '" + lsTranSer + "' AND "
				+" REF_ID  = '" + docNo + "' AND "
				+" CERT_NO = '" + certNo + "' ";

				//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1,lsTranId);
				//pstmt.setString(2,lsTranSer);
				//pstmt.setString(3,docNo);
				//pstmt.setString(4,certNo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					llCnt = rs.getInt("NET_AMT");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(llCnt > 0)
				{
					errCode="VTINSNF1";
					errString = itmDBAccessEJB.getErrorString("","VTINSNF1",chgUser,"",conn);
					return errString;
				}
				else
				{
					errString = new InsuranceUpdate().gf_ins_upd(insUpdHMap,ldNetAmt,conn);
				}
			}//end of if(lsTranId.trim().length()  > 0)

			sql="UPDATE DISTORD_ISS SET CONFIRMED = 'Y',CONF_DATE = sysdate "
			+" WHERE TRAN_ID = '" + tranId + "' ";
			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			no = pstmt.executeUpdate();
			pstmt = null;

			sql="UPDATE ROADPERMIT SET STATUS = 'C' "
			+" WHERE RD_PERMIT_NO = ?  ";

			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,lsRdPermitNo);
			no = pstmt.executeUpdate();
			pstmt = null;

			sql="SELECT DIST_ORDER FROM DISTORD_ISS "
			+" WHERE TRAN_ID = ? ";

			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mDistOrder = rs.getString("DIST_ORDER");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql="SELECT AUTO_RECEIPT FROM DISTORDER "
			+" WHERE DIST_ORDER = '" + mDistOrder + "' ";
			//System.out.println("[DistIssueConfirmActEJB] sql=>"+sql);
			pstmt = conn.prepareStatement(sql); //added by rajendra on 31/3/08
			//pstmt.setString(1,mDistOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				lsAutoRcp = rs.getString("AUTO_RECEIPT")== null ? "":rs.getString("AUTO_RECEIPT").trim();
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("lsAutoRcp..................["+lsAutoRcp+"]");
			System.out.println("errString..................["+errString+"]");

			if( errString.trim().length() == 0 &&("Y".equals(lsAutoRcp) || "C".equals(lsAutoRcp) ) )
			{
				xmlString = createXmlForIssue(tranId,conn);
				System.out.println("xmlString.................."+xmlString);
				CreateDistReceipt distRcpt = new CreateDistReceipt();
				errString = distRcpt.createDistributionReceipt(xmlString,xtraParams, conn);
				distRcpt = null;

			}
		}
		catch (SQLException e1)
		{
			errString = "ERROR";
			e1.printStackTrace();
			System.out.println("The Exception occure in DistIssueConfirmActEJB :"+e1);
		}
		catch (Exception e)
		{
			errString = "ERROR";
			e.printStackTrace();
			System.out.println("The Exception occure in DistIssueConfirmActEJB :"+e);
		}
		finally
		{
			pstmt = null;
			rs = null;
			//conn = null;
		}
		return errString;
	}

	public String createXmlForIssue(String tranId,Connection conn)throws RemoteException,ITMException
	{
		String xmlString = null;
		StringBuffer xmlBuff = new StringBuffer();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try
		{
			System.out.println("[DistIssueConfirmActEJB] createXmlForIssue() method called.........");

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//System.out.println("[DistIssueConfirmActEJB]createXmlForIssue() method called..............");
			sql=" SELECT D.TRAN_ID,D.TRAN_DATE,D.EFF_DATE,D.DIST_ORDER,D.SITE_CODE,"
			+" D.SITE_CODE__DLV,"
			+" D.DIST_ROUTE,D.TRAN_CODE,D.LR_NO,D.LR_DATE,D.LORRY_NO,D.GROSS_WEIGHT,"
			+" D.TARE_WEIGHT,D.NET_WEIGHT,D.FRT_AMT,D.AMOUNT,D.TAX_AMT,D.NET_AMT,"
			+" D.REMARKS,D.FRT_TYPE,D.CHG_USER,D.CHG_TERM,D.CURR_CODE,D.CHG_DATE,"
			+" SITE_A.DESCR ADESCR,SITE_B.DESCR BDESCR,LOCATION.DESCR locdescr,TRANSPORTER.TRAN_NAME,CURRENCY_A.DESCR CURRADESCR,"
			+" D.CONFIRMED,D.LOC_CODE__GIT,D.CONF_DATE,D.NO_ART,D.TRANS_MODE,D.GP_NO,"
			+" D.GP_DATE,D.CONF_PASSWD,D.ORDER_TYPE,D.GP_SER,D.REF_NO,D.REF_DATE,"
			+" D.AVAILABLE_YN,SITE_B.ADD1 BADD1,SITE_B.ADD2 BADD2,SITE_B.CITY BCITY,SITE_B.PIN BPIN,SITE_B.STATE_CODE BSTATE_CODE,"
			+" D.EXCH_RATE,D.TRAN_TYPE,D.EMP_CODE__APRV,D.DISCOUNT,D.PERMIT_NO,D.SHIPMENT_ID,"
			+" D.CURR_CODE__FRT,D.EXCH_RATE__FRT,CURRENCY_B.DESCR CURRBDESCR,D.RD_PERMIT_NO,D.DC_NO,"
			+" D.TRAN_SER,D.PART_QTY,SPACE(100) AS SUNDRY_DETAILS,SPACE(100) AS SUNDRY_NAME,"
			+" D.PROJ_CODE,SITE_B.TELE1 BTELE1,SITE_B.TELE2 BTELE2,SITE_B.TELE3 BTELE3,D.SITE_CODE__BIL,SITE_C.DESCR CDESCR,"
			+" SITE_C.ADD1 CADD1,SITE_C.ADD2 CADD2,SITE_C.CITY CCITY,SITE_C.PIN CPIN,SITE_C.STATE_CODE CSTATE_CODE,D.PALLET_WT,"
			+" DISTORDER.AUTO_RECEIPT  AUTO_RECEIPT"
			+"  FROM DISTORD_ISS  D,SITE SITE_A,SITE SITE_B,LOCATION  LOCATION, "
			+"  TRANSPORTER  TRANSPORTER,CURRENCY CURRENCY_A,CURRENCY CURRENCY_B, "
			+"  SITE SITE_C,DISTORDER  DISTORDER "
			+"  WHERE ( D.SITE_CODE      = SITE_A.SITE_CODE   ) AND  "
			+" ( D.SITE_CODE__DLV      = SITE_B.SITE_CODE   ) AND "
			+" ( D.LOC_CODE__GIT      = LOCATION.LOC_CODE (+)  ) AND "
			+" ( D.CURR_CODE = CURRENCY_A.CURR_CODE   ) AND "
			+" ( D.DIST_ORDER = DISTORDER.DIST_ORDER   ) AND "
			+" ( D.TRAN_CODE=TRANSPORTER.TRAN_CODE(+)) AND "
			+" ( D.CURR_CODE__FRT=CURRENCY_B.CURR_CODE(+)) AND "
			+" ( D.SITE_CODE__BIL=SITE_C.SITE_CODE(+))"
			+" AND D.TRAN_ID = '" + tranId + "' ";

			//System.out.println("[DistIssueConfirmActEJB] sql for header=>"+sql);

			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			xmlBuff.append("<Root>");
			if(rs.next())
			{
				xmlBuff.append("<Detail1>");
				xmlBuff.append("<tran_id>"+rs.getString("TRAN_ID")+"</tran_id>");
				xmlBuff.append("<tran_date>"+sdf.format(rs.getTimestamp("TRAN_DATE"))+"</tran_date>");
				xmlBuff.append("<eff_date>"+sdf.format(rs.getTimestamp("EFF_DATE"))+"</eff_date>");
				xmlBuff.append("<dist_order><![CDATA["+rs.getString("DIST_ORDER")+"]]></dist_order>");
				xmlBuff.append("<site_code><![CDATA["+(rs.getString("SITE_CODE")==null?"":rs.getString("SITE_CODE"))+"]]></site_code>");
				xmlBuff.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV"))+"]]></site_code__dlv>");
				xmlBuff.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
				xmlBuff.append("<tran_code><![CDATA["+(rs.getString("TRAN_CODE")==null?"":rs.getString("TRAN_CODE"))+"]]></tran_code>");
				xmlBuff.append("<lr_no><![CDATA["+(rs.getString("LR_NO")==null?"":rs.getString("LR_NO"))+"]]></lr_no>");
				xmlBuff.append("<lr_date><![CDATA[]]></lr_date>");
				xmlBuff.append("<lorry_no><![CDATA["+(rs.getString("LORRY_NO")==null?"":rs.getString("LORRY_NO"))+"]]></lorry_no>");
				xmlBuff.append("<gross_weight><![CDATA["+rs.getDouble("GROSS_WEIGHT")+"]]></gross_weight>");
				xmlBuff.append("<tare_weight><![CDATA["+rs.getDouble("TARE_WEIGHT")+"]]></tare_weight>");
				xmlBuff.append("<net_weight><![CDATA["+rs.getDouble("NET_WEIGHT")+"]]></net_weight>");
				xmlBuff.append("<frt_amt><![CDATA["+rs.getDouble("FRT_AMT")+"]]></frt_amt>");
				xmlBuff.append("<amount><![CDATA["+rs.getDouble("AMOUNT")+"]]></amount>");
				xmlBuff.append("<tax_amt><![CDATA["+rs.getDouble("TAX_AMT")+"]]></tax_amt>");
				xmlBuff.append("<net_amt><![CDATA["+rs.getDouble("NET_AMT")+"]]></net_amt>");
				xmlBuff.append("<remarks><![CDATA["+(rs.getString("REMARKS")==null?"":rs.getString("REMARKS"))+"]]></remarks>");
				xmlBuff.append("<frt_type><![CDATA["+(rs.getString("FRT_TYPE")==null?"":rs.getString("FRT_TYPE"))+"]]></frt_type>");
				xmlBuff.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER"))+"]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM"))+"]]></chg_term>");
				xmlBuff.append("<curr_code><![CDATA["+(rs.getString("CURR_CODE")==null?"":rs.getString("CURR_CODE"))+"]]></curr_code>");
				xmlBuff.append("<chg_date><![CDATA["+sdf.format(rs.getTimestamp("CHG_DATE"))+"]]></chg_date>");
				xmlBuff.append("<site_descr><![CDATA["+(rs.getString("ADESCR")==null?"":rs.getString("ADESCR"))+"]]></site_descr>");
				xmlBuff.append("<site_to_descr><![CDATA["+(rs.getString("BDESCR")==null?"":rs.getString("BDESCR"))+"]]></site_to_descr>");
				xmlBuff.append("<location_descr><![CDATA["+(rs.getString("locdescr")==null?"":rs.getString("locdescr"))+"]]></location_descr>");
				xmlBuff.append("<tran_name><![CDATA["+(rs.getString("TRAN_NAME")==null?"":rs.getString("TRAN_NAME"))+"]]></tran_name>");
				xmlBuff.append("<currency_descr><![CDATA["+(rs.getString("CURRADESCR")==null?"":rs.getString("CURRADESCR"))+"]]></currency_descr>");
				xmlBuff.append("<confirmed><![CDATA[Y]]></confirmed>");
				xmlBuff.append("<loc_code__git><![CDATA["+(rs.getString("LOC_CODE__GIT")==null?"":rs.getString("LOC_CODE__GIT"))+"]]></loc_code__git>");
				xmlBuff.append("<conf_date><![CDATA["+sdf.format(rs.getTimestamp("CONF_DATE"))+"]]></conf_date>");
				xmlBuff.append("<no_art><![CDATA["+rs.getInt("NO_ART")+"]]></no_art>");
				xmlBuff.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE"))+"]]></trans_mode>");
				xmlBuff.append("<gp_no><![CDATA["+(rs.getString("GP_NO")==null?"":rs.getString("GP_NO"))+"]]></gp_no>");
				xmlBuff.append("<gp_date><![CDATA["+(rs.getTimestamp("GP_DATE") ==null?"":sdf.format(rs.getTimestamp("GP_DATE")))+"]]></gp_date>");
				xmlBuff.append("<conf_passwd/>");
				xmlBuff.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE"))+"  ]]></order_type>");
				xmlBuff.append("<gp_ser><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE"))+"]]></gp_ser>");
				xmlBuff.append("<ref_no><![CDATA["+(rs.getString("REF_NO")==null?"":rs.getString("REF_NO"))+"]]></ref_no>");
				xmlBuff.append("<ref_date><![CDATA["+(rs.getTimestamp("REF_DATE") == null?"":sdf.format(rs.getTimestamp("REF_DATE")))+"]]></ref_date>");
				xmlBuff.append("<available_yn><![CDATA["+(rs.getString("AVAILABLE_YN")==null?"":rs.getString("AVAILABLE_YN"))+"]]></available_yn>");
				xmlBuff.append("<site_add1><![CDATA["+(rs.getString("BADD1")==null?"":rs.getString("BADD1"))+"  ]]></site_add1>");
				xmlBuff.append("<site_add2><![CDATA["+(rs.getString("BADD2")==null?"":rs.getString("BADD2"))+"  ]]></site_add2>");
				xmlBuff.append("<site_city><![CDATA["+(rs.getString("BCITY")==null?"":rs.getString("BCITY"))+"  ]]></site_city>");
				xmlBuff.append("<site_pin><![CDATA["+(rs.getString("BPIN")==null?"":rs.getString("BPIN"))+"  ]]></site_pin>");
				xmlBuff.append("<site_state_code><![CDATA["+(rs.getString("BSTATE_CODE")==null?"":rs.getString("BSTATE_CODE"))+"  ]]></site_state_code>");
				xmlBuff.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
				xmlBuff.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE"))+"  ]]></tran_type>");
				xmlBuff.append("<emp_code__aprv><![CDATA["+(rs.getString("EMP_CODE__APRV")==null?"":rs.getString("EMP_CODE__APRV"))+"]]></emp_code__aprv>");
				xmlBuff.append("<discount><![CDATA["+rs.getDouble("DISCOUNT")+"]]></discount>");
				xmlBuff.append("<permit_no><![CDATA["+(rs.getString("PERMIT_NO")==null?"":rs.getString("PERMIT_NO"))+"]]></permit_no>");
				xmlBuff.append("<shipment_id><![CDATA["+(rs.getString("SHIPMENT_ID")==null?"":rs.getString("SHIPMENT_ID"))+"]]></shipment_id>");
				xmlBuff.append("<curr_code__frt><![CDATA["+(rs.getString("CURR_CODE__FRT")==null?"":rs.getString("CURR_CODE__FRT"))+"]]></curr_code__frt>");
				xmlBuff.append("<exch_rate__frt><![CDATA["+rs.getDouble("EXCH_RATE__FRT")+"]]></exch_rate__frt>");
				xmlBuff.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
				xmlBuff.append("<rd_permit_no><![CDATA["+(rs.getString("RD_PERMIT_NO")==null?"":rs.getString("RD_PERMIT_NO"))+"]]></rd_permit_no>");
				xmlBuff.append("<dc_no><![CDATA["+(rs.getString("DC_NO")==null?"":rs.getString("DC_NO"))+"]]></dc_no>");
				xmlBuff.append("<tran_ser><![CDATA["+(rs.getString("TRAN_SER")==null?"":rs.getString("TRAN_SER"))+"]]></tran_ser>");
				xmlBuff.append("<part_qty><![CDATA["+(rs.getString("PART_QTY")==null?"":rs.getString("PART_QTY"))+"]]></part_qty>");
				xmlBuff.append("<sundry_details><![CDATA[]]></sundry_details>");
				xmlBuff.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
				xmlBuff.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE"))+" ]]></proj_code>");
				xmlBuff.append("<site_tele1><![CDATA["+(rs.getString("BTELE1")==null?"":rs.getString("BTELE1"))+"]]></site_tele1>");
				xmlBuff.append("<site_tele2><![CDATA["+(rs.getString("BTELE2")==null?"":rs.getString("BTELE2"))+"]]></site_tele2>");
				xmlBuff.append("<site_tele3><![CDATA["+(rs.getString("BTELE3")==null?"":rs.getString("BTELE3"))+"]]></site_tele3>");
				xmlBuff.append("<site_code__bil><![CDATA["+(rs.getString("SITE_CODE__BIL")==null?"":rs.getString("SITE_CODE__BIL"))+"]]></site_code__bil>");
				xmlBuff.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
				xmlBuff.append("<site_add1_bill><![CDATA["+(rs.getString("CADD1")==null?"":rs.getString("CADD1"))+"]]></site_add1_bill>");
				xmlBuff.append("<site_add2_bill><![CDATA["+(rs.getString("CADD2")==null?"":rs.getString("CADD2"))+"]]></site_add2_bill>");
				xmlBuff.append("<site_city_bill><![CDATA["+(rs.getString("CCITY")==null?"":rs.getString("CCITY"))+"]]></site_city_bill>");
				xmlBuff.append("<site_pin_bill><![CDATA["+(rs.getString("CPIN")==null?"":rs.getString("CPIN"))+"]]></site_pin_bill>");
				xmlBuff.append("<site_state_code_bill><![CDATA["+(rs.getString("CSTATE_CODE")==null?"":rs.getString("CSTATE_CODE"))+"]]></site_state_code_bill>");
				xmlBuff.append("<pallet_wt><![CDATA["+rs.getDouble("PALLET_WT")+"]]></pallet_wt>");
				xmlBuff.append("<auto_receipt><![CDATA["+(rs.getString("AUTO_RECEIPT")==null?"":rs.getString("AUTO_RECEIPT"))+"]]></auto_receipt>");
				xmlBuff.append("</Detail1>");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql="SELECT D.TRAN_ID TRAN_ID,D.LINE_NO LINE_NO,D.DIST_ORDER DIST_ORDER,D.LINE_NO_DIST_ORDER LINE_NO_DIST_ORDER,"
			+" D.ITEM_CODE ITEM_CODE,D.QUANTITY QUANTITY,D.UNIT UNIT,D.TAX_CLASS TAX_CLASS,D.TAX_CHAP TAX_CHAP,D.TAX_ENV TAX_ENV,"
			+" D.LOC_CODE LOC_CODE,D.LOT_NO LOT_NO,D.LOT_SL LOT_SL,D.PACK_CODE PACK_CODE,D.RATE RATE,D.AMOUNT AMOUNT,D.TAX_AMT TAX_AMT,"
			+" D.NET_AMT NET_AMT,ITEM.DESCR ITEMDESCR,LOCATION.DESCR LOCDESCR,D.SITE_CODE__MFG SITE_CODE__MFG,D.MFG_DATE MFG_DATE,"
			+" D.EXP_DATE EXP_DATE,D.POTENCY_PERC POTENCY_PERC,D.NO_ART NO_ART,D.GROSS_WEIGHT GROSS_WEIGHT,D.TARE_WEIGHT TARE_WEIGHT,"
			+" D.NET_WEIGHT NET_WEIGHT,D.PACK_INSTR PACK_INSTR,D.DIMENSION DIMENSION,D.SUPP_CODE__MFG SUPP_CODE__MFG,D.BATCH_NO BATCH_NO,"
			+" D.GRADE GRADE,D.RETEST_DATE RETEST_DATE,D.RATE__CLG RATE__CLG,D.DISCOUNT DISCOUNT,D.DISC_AMT DISC_AMT,D.REMARKS REMARKS,"
			+" D.COST_RATE COST_RATE,SPACE(300) AS QTY_DETAILS,D.UNIT__ALT UNIT__ALT,D.CONV__QTY__ALT CONV__QTY__ALT,"
			+" D.QTY_ORDER__ALT QTY_ORDER__ALT,D.PALLET_WT  PALLET_WT"
			+"  FROM DISTORD_ISSDET  D,ITEM  ITEM,LOCATION  LOCATION "
			+"  WHERE ( D.ITEM_CODE      = ITEM.ITEM_CODE   ) AND  "
			+" ( D.LOC_CODE = LOCATION.LOC_CODE   )"
			+" AND ( ( D.TRAN_ID = '" + tranId + "') ) "
			+" ORDER BY D.LINE_NO ASC ";

			//System.out.println("[DistIssueConfirmActEJB] sql for detail=>"+sql);
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();

			while(rs.next())
			{
				xmlBuff.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"2\">");
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				//xmlBuff.append("<tran_id>"+(rs.getString("TRAN_ID"))+"</tran_id>");
				xmlBuff.append("<line_no><![CDATA["+(rs.getInt("LINE_NO"))+"]]></line_no>");
				xmlBuff.append("<dist_order><![CDATA["+rs.getString("DIST_ORDER")+"]]></dist_order>");
				//next line changed for lin_no to LINE_NO_DIST_ORDER on 020309
				xmlBuff.append("<line_no_dist_order><![CDATA["+rs.getInt("LINE_NO_DIST_ORDER")+"]]></line_no_dist_order>");
				//xmlBuff.append("<line_no_dist_order><![CDATA["+rs.getInt("LINE_NO")+"]]></line_no_dist_order>");
				xmlBuff.append("<item_code><![CDATA["+(rs.getString("ITEM_CODE"))+"]]></item_code>");
				xmlBuff.append("<quantity><![CDATA["+rs.getDouble("QUANTITY")+"]]></quantity>");
				xmlBuff.append("<unit><![CDATA["+(rs.getString("UNIT")==null?"":rs.getString("UNIT"))+"]]></unit>");
				xmlBuff.append("<tax_class><![CDATA["+(rs.getString("TAX_CLASS")==null?"":rs.getString("TAX_CLASS"))+"]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA["+(rs.getString("TAX_CHAP")==null?"":rs.getString("TAX_CHAP"))+"]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA["+(rs.getString("TAX_ENV")==null?"":rs.getString("TAX_ENV"))+"]]></tax_env>");
				xmlBuff.append("<loc_code><![CDATA["+(rs.getString("LOC_CODE")==null?"":rs.getString("LOC_CODE"))+"]]></loc_code>");
				xmlBuff.append("<lot_no><![CDATA["+(rs.getString("LOT_NO")==null?"":rs.getString("LOT_NO"))+"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+(rs.getString("LOT_SL")==null?"":rs.getString("LOT_SL"))+"]]></lot_sl>");
				xmlBuff.append("<pack_code><![CDATA["+(rs.getString("PACK_CODE")==null?"":rs.getString("PACK_CODE"))+"]]></pack_code>");
				xmlBuff.append("<rate><![CDATA["+rs.getDouble("RATE")+"]]></rate>");
				xmlBuff.append("<amount><![CDATA["+rs.getDouble("AMOUNT")+"]]></amount>");
				xmlBuff.append("<tax_amt><![CDATA["+rs.getDouble("TAX_AMT")+"]]></tax_amt>");
				xmlBuff.append("<net_amt><![CDATA["+rs.getDouble("NET_AMT")+"]]></net_amt>");
				xmlBuff.append("<item_descr><![CDATA["+(rs.getString("ITEMDESCR")==null?"":rs.getString("ITEMDESCR"))+"]]></item_descr>");
				xmlBuff.append("<location_desc/>");
				xmlBuff.append("<site_code__mfg/>");
				SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
				String mfgdateStr ="" , expdateStr ="";
				Timestamp mfgDate =null,expDate =null;
				mfgDate = rs.getTimestamp("MFG_DATE");
				if(mfgDate != null)
				{
					mfgdateStr = sdf1.format(mfgDate);
				}
				expDate = rs.getTimestamp("EXP_DATE");
				if(mfgDate != null)
				{
					expdateStr = sdf1.format(expDate);
				}
				xmlBuff.append("<mfg_date><![CDATA["+mfgdateStr+"]]></mfg_date>");
				xmlBuff.append("<exp_date><![CDATA["+expdateStr+"]]></exp_date>");
				xmlBuff.append("<potency_perc><![CDATA["+rs.getDouble("POTENCY_PERC")+"]]></potency_perc>");
				xmlBuff.append("<no_art><![CDATA["+rs.getInt("NO_ART")+"]]></no_art>");
				xmlBuff.append("<gross_weight><![CDATA["+rs.getDouble("GROSS_WEIGHT")+"]]></gross_weight>");
				xmlBuff.append("<tare_weight><![CDATA["+rs.getDouble("TARE_WEIGHT")+"]]></tare_weight>");
				xmlBuff.append("<net_weight><![CDATA["+rs.getDouble("NET_WEIGHT")+"]]></net_weight>");
				xmlBuff.append("<pack_instr><![CDATA["+(rs.getString("PACK_INSTR")==null?"":rs.getString("PACK_INSTR"))+"]]></pack_instr>");
				xmlBuff.append("<dimension><![CDATA["+(rs.getString("DIMENSION")==null?"":rs.getString("DIMENSION"))+"]]></dimension>");
				xmlBuff.append("<supp_code__mfg><![CDATA["+(rs.getString("SUPP_CODE__MFG")==null?"":rs.getString("SUPP_CODE__MFG"))+"]]></supp_code__mfg>");
				xmlBuff.append("<batch_no><![CDATA["+(rs.getString("BATCH_NO")==null?"":rs.getString("BATCH_NO"))+"]]></batch_no>");
				xmlBuff.append("<grade><![CDATA["+(rs.getString("GRADE")==null?"":rs.getString("GRADE"))+"]]></grade>");
				xmlBuff.append("<retest_date><![CDATA[]]></retest_date>");
				xmlBuff.append("<rate__clg><![CDATA["+rs.getDouble("RATE__CLG")+"]]></rate__clg>");
				xmlBuff.append("<discount><![CDATA["+rs.getDouble("DISCOUNT")+"]]></discount>");
				xmlBuff.append("<disc_amt><![CDATA["+rs.getDouble("DISC_AMT")+"]]></disc_amt>");
				xmlBuff.append("<remarks><![CDATA["+(rs.getString("REMARKS")==null?"":rs.getString("REMARKS"))+"]]></remarks>");
				xmlBuff.append("<cost_rate><![CDATA["+rs.getDouble("COST_RATE")+"]]></cost_rate>");
				xmlBuff.append("<qty_details><![CDATA[]]></qty_details>");
				xmlBuff.append("<unit__alt><![CDATA["+(rs.getString("UNIT__ALT")==null?"":rs.getString("UNIT__ALT"))+"]]></unit__alt>");
				xmlBuff.append("<conv__qty__alt><![CDATA["+rs.getDouble("CONV__QTY__ALT")+"]]></conv__qty__alt>");
				xmlBuff.append("<qty_order__alt><![CDATA["+rs.getDouble("QTY_ORDER__ALT")+"]]></qty_order__alt>");
				xmlBuff.append("<pallet_wt><![CDATA["+rs.getDouble("PALLET_WT")+"]]></pallet_wt>");
				xmlBuff.append("</Detail2>");

			}//end of while(rs.next())
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			xmlBuff.append("</Root>");
			xmlString = xmlBuff.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return xmlString;
	}
	 private String getCurrdateAppFormat()
    {
        String s = "";
        //GenericUtility genericUtility = GenericUtility.getInstance();
        try
        {
            java.util.Date date = null;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println(genericUtility.getDBDateFormat());

            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
            s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
        }
        catch(Exception exception)
        {
            System.out.println("Exception in [MPSOrder] getCurrdateAppFormat " + exception.getMessage());
        }
        return s;
    }
}