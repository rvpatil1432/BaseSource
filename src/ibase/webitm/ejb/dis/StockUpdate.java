package ibase.webitm.ejb.dis;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import ibase.webitm.ejb.dis.DistCommon;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.reports.sun.PackingSlipSOrderFormulation;
import ibase.webitm.utility.TransIDGenerator;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

/**
 * @author base
 *
 */
public class StockUpdate
{
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	CommonConstants commonConstants  = new CommonConstants();
	DistCommon disCommon = new DistCommon();
	ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
	String userId = null;
	String termId = null;
	double quantity=0,qtyStduom=0,rate = 0,potencyPerc=0,netAmt= 0,grossWeight=0;
	double tareWeight=0,netWeight=0,rateOh=0,grossRate=0,convQtyStduom=0,noArt=0;
	double actualRate=0,batchSize = 0;
	String errString ="",itemCode = "",unit="",tranType = "",siteCode="",locationCode="";
	String lotNo="",lotSl="",checkExpiry="",tranSer="",tranId="",acctCodeCr="",packRef = "";
	String acctCodeDr="",cctrCodeCr = "",cctrCodeDr = "",lineNo="",sorderNo ="",siteCodeMfg="";
	String packCode="",itemSer="",reasCode="",invStat="",sundryType="",sundryCode="";
	String packInstr="",dimension="",acctCodeInv="",cctrCodeInv="",acctCodeOh="",cctrCodeOh="";
	String suppCodeMfg="",grade="",batchNo="",unitAlt="",remarks="",refIdFor="",refSerFor="";
	Timestamp creaDateStr=null,date=null,mfgDate=null,expDate = null,currDate=null,retestDateDate=null;
	Timestamp lastPhycDate=null,tranDate= null,retestDate =null;
	String suppCode="";  // added by cpatil
	String chgTerm="",chgUser="";        // added by cpatil
	String shelfLifeType = ""; // 26/02/14 manoharan
	//
	String considerAllocate = "Y",	partialUsed = "";
	//
	String lotSlOrg="";
	int cnt=0;
	int cnt1=0,cnt2=0;
	int cntHold=0;
	double holdQty=0;
	String quarLockCode="",genratedTranId="";
	String holdLock="";//addad by priyanka
	DistCommon distCommon = new DistCommon();// added by priyanka
	String updateRef="";//added by nandkumar gadkari on 22/04/19
	
    /**
     * Updates stock return error if fails
     *
     * @param     updateStockMap  HashMap with stock details
     * @param     xtraParams Extra parameters
     * @param	  conn   Database connection
     * @return    Error code if fails
     * @exception ITMException 
     */
	public  String updateStock(HashMap updateStockMap,String xtraParams, Connection conn) throws ITMException, Exception
	{
        PreparedStatement pstmt = null;
        ResultSet rs=null;
        
        String sql=null,errString=null,mbaseUnit=null,stkOpt=null,qcReqd=null;
		String orderType = null,stockValuation=null,minvStat=null,moverIssue=null,minvtraceNo=null;
		String mfinEntity = null,mcurrCode=null,issCriteria = null,value=null,invAcct=null;		
        Timestamp stkRetestDate=null,creaDt=null;
		String parmValueStr=null;
		double mrate=0,meffQty=0,mbefQty=0,mafterQty =0,effQty=0,mamount=0,mexcRate=0,mstkQty=0;
		double mallocQty=0,oldGrossValue=0,oldvalue=0,oldQty=0,newValue=0,newGrossValue=0;
		double totalValue=0,totalGrossValue=0,oldValue=0,qtyPerArt=0,tarewtPerArt = 0;
		double grosswtPerArt = 0,parmValue=0;
		double convFact=1;
		int count=0,mshLife=0,update=0;
		String acctCodeDr = "",cctrCodeDr = "", sqlState ="", errorCode = "";
        Timestamp mtoday = null;
        Timestamp tempTestDate = null;
        double qtyPerArtStk = 0d,qtyStk=0d;
        String quarantineLock="",lockCode="";
        ArrayList< String> lockCodeList= new ArrayList<String>();
        UtilMethods utilMethods =  UtilMethods.getInstance();
        String mamountStr="", qtyPerArtStr ="", grosswtPerArtStr ="",tarewtPerArtStr ="";
		try
		{
			DistCommon distCommon = new DistCommon();
            FinCommon finCommon = new FinCommon();
           //added by monika for stock transfer
            String lineNo=(String)updateStockMap.get("line_no");
            System.out.println(" update line no from stock transfer conf123 :: "+lineNo);

 System.out.println(" update line no from s :: "+lineNo);
            puplateCommonParameters(updateStockMap);
            
            //ADDED BY RITESH ON 31/07/14 START
            
        	sql = "select quantity , qty_per_art "
        						+ "from stock "
								+ "where item_code = ? "
								+ "and site_code = ? "
								+ "and loc_code = ? "
								+ "and lot_no = ? "
								+ "and lot_sl = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);
			pstmt.setString(2,siteCode);
			pstmt.setString(3,locationCode);
			pstmt.setString(4,lotNo);
			pstmt.setString(5,lotSl);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				qtyStk= rs.getDouble("quantity");

				qtyPerArtStk= rs.getDouble("qty_per_art");
				if((int) (qtyStk/qtyPerArtStk) == 0)
				{
					this.noArt = 1;
				}
			}
			//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
			if(rs!=null)
			{
				rs.close();rs=null;
			}
			if(pstmt!=null)
			{
				pstmt.close();pstmt=null;
			}
			//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
            System.out.println(" no_art accepted 16092014:: "+this.noArt);
            //ADDEDBY RITESH ON 31/07/14 END
            tempTestDate = Timestamp.valueOf("1900-01-01 00:00:00");
            userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  
			termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			chgUser = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgUser" );
			
			if (userId == null || userId.trim().length() == 0)
			{
				userId = "SYSTEM";
			}
			if (termId == null || termId.trim().length() == 0)
			{
				termId = "SYSTEM";
			}
			System.out.println("this.tranType--["+this.tranType+"]");
			if(this.tranType.equalsIgnoreCase("R"))
			{
				sql="SELECT MIN(CREA_DATE) FROM STOCK WHERE ITEM_CODE = '"+this.itemCode+"' AND SITE_CODE = '"+this.siteCode+"'"
				+" AND LOT_NO = '"+this.lotNo+"'";
				System.out.println("Query :::- ["+sql+"]");
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					creaDt = rs.getTimestamp(1);
					System.out.println("creaDt Initialized with  MIN(CREA_DATE) of stock:::- ["+creaDt+"]");
				}
				pstmt.close();
				rs.close();
			}
			else
			{
				creaDt = new Timestamp(System.currentTimeMillis()) ;
				System.out.println("creaDt Initialized Current date:::- ["+creaDt+"]");
			}
			
			// commented by Kunal 12/04/17 stock Validation not Required
			// 05/02/13 manoharan check for stock if issue
			/*if (this.tranType.equalsIgnoreCase("I") || this.tranType.equalsIgnoreCase("ID") )
			{
				sql =" select count(1) 	from stock 	where item_code = ? "
				    +" and site_code = ? " 
					+" and loc_code  = ? " 
					+" and lot_no = ? "
					+" and lot_sl =  ? ";

                    pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,this.itemCode);
					pstmt.setString(2,this.siteCode);
					pstmt.setString(3,this.locationCode);
					pstmt.setString(4,this.lotNo);
					pstmt.setString(5,this.lotSl);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if (count == 0)
					{
						errString = "VTSTOCK1";
						errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						return errString;
					}
					
			}*/
			// end 05/02/13 manoharan
			//end by Kunal 12/04/17 
            
            /*if gs_run_mode <> 'B' then//Batch load to be checked, currently the current Date 
                s_updatestock.currdate = ldt_today // and create date is taken as current timestamp
            else
                s_updatestock.currdate = s_updatestock.trandate
            end if*/
            
            if(currDate == null){
                this.currDate = new Timestamp(System.currentTimeMillis());
            }
			if(creaDt == null){
				creaDt = this.currDate;                
			}
            System.out.println("this.currDate "+this.currDate);
			System.out.println("creaDt :::- ["+creaDt+"]");
            if(this.invStat == null || this.invStat.trim().length()==0)
			{
				sql="SELECT INV_STAT FROM LOCATION 	WHERE LOC_CODE = '"+this.locationCode+"'";
				System.out.println("Query :::- ["+sql+"]");
                pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					this.invStat = rs.getString(1);
					if(this.invStat!=null)
                        System.out.println("INV_STAT FROM LOCATION :::- ["+this.invStat+"]");
				}
				pstmt.close();
				rs.close();
			}
			
			sql =" SELECT COST_RATE,UNIT,SHELF_LIFE,(CASE WHEN STK_OPT IS NULL THEN '0' ELSE STK_OPT END ),"
			+" (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE QC_REQD END ) FROM ITEM WHERE ITEM_CODE = '"+this.itemCode+"'";
			System.out.println("Query ::- ["+sql+"]");
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mrate = rs.getDouble(1);
				mbaseUnit = rs.getString(2);
				mshLife = rs.getInt(3);
				stkOpt = rs.getString(4);
				qcReqd =  rs.getString(5);
				
			}
			pstmt.close();
			rs.close();
			
			sql = "SELECT ORDER_TYPE FROM WORKORDER WHERE WORK_ORDER ='"+this.sorderNo+"'";
			System.out.println("Query :::- ["+sql+"]");
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				orderType = rs.getString(1);
			}
			pstmt.close();
			rs.close();
						
			if(orderType != null && orderType.equalsIgnoreCase("T"))
			{
				stkOpt = checkStkOptRnd(this.itemCode,this.siteCode,conn);
			}
			if(stkOpt == null || stkOpt.trim().length() == 0)
			{
				stkOpt = checkStkOpt(this.itemCode,this.siteCode,conn);
				System.out.println("stkOpt[gfChkStkOpt] :::- ["+stkOpt+"]");
			}else{
                System.out.println("[gbfChkStkOptRnd] :::- ["+stkOpt+"]");
			}
			if(this.itemSer == null || this.itemSer.trim().length()==0)
			{
				sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+this.itemCode+"'" ; 
				System.out.println("Query :::- ["+sql+"]");
                pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					this.itemSer = rs.getString(1);
				}
				pstmt.close();
				rs.close();
			}
			sql = "	SELECT 	(CASE WHEN STOCK_VALUATION IS NULL THEN 'N' ELSE STOCK_VALUATION END )"
			+" FROM ITEMSER WHERE 	ITEM_SER = '"+this.itemSer+"'" ;
			System.out.println("Query :::- ["+sql+"]");
            pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				stockValuation = rs.getString(1);
			}
			pstmt.close();
			rs.close();
			if(stockValuation == null) stockValuation="N";
			if (this.grade == null || this.grade.trim().length() == 0) this.grade = "   ";
			System.out.println("stkOpt :::- ["+stkOpt+"]");
			if(stkOpt.equalsIgnoreCase("0"))
			{
				System.out.println("Not To Update Stock>>>>>>>>>>>>>>>>>>>>>>>>");
                return errString;//errString to be made - Jiten
			}
			if(stkOpt.equalsIgnoreCase("1"))
			{
				if (this.tranType.equalsIgnoreCase("R"))
				{
					String lotNoChg = this.grade +"               ";
					this.lotNo = lotNoChg.substring(0,15);
					this.lotSl= "     ";
				}
			}
			//*********Calculating conversion quantity
			meffQty =0;
			if(this.qtyStduom == 0)
			{
				if(!this.unit.equalsIgnoreCase(mbaseUnit))
				{
					if(this.convQtyStduom != 0)
					{
						convFact = this.convQtyStduom;
					}
					ArrayList convQtyList = disCommon.getConvQuantityFact(this.unit, mbaseUnit, this.itemCode, this.quantity, convFact,conn);
					meffQty = Double.parseDouble(convQtyList.get(1).toString());
					if(meffQty == -999999999)
					{
						errString = "VMUCNV1";
						errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						return errString;
					
					}
					if (this.convQtyStduom == 0)
					{
						this.convQtyStduom= Double.parseDouble(convQtyList.get(0).toString());
					}
					
				}
				else
				{
					meffQty = this.quantity ;
					if (this.convQtyStduom == 0) 
					{
						this.convQtyStduom=1;
					}
					
				}
				this.qtyStduom = meffQty;
			}
			if(this.lotNo == null || this.lotNo.trim().length() == 0)
			{
				this.lotNo ="               ";
			}
			if(this.lotSl == null || this.lotSl.trim().length()==0)
			{
				this.lotSl = "     ";
			}

			if ((!this.tranSer.equalsIgnoreCase("QC-ORD")) &&  (!this.tranSer.equalsIgnoreCase("I-PKR")) && (!this.tranSer.equalsIgnoreCase("I-PKI")) && (!this.tranType.equalsIgnoreCase("R")) && (!this.tranSer.equalsIgnoreCase("S-ISS")) && (!this.tranSer.equalsIgnoreCase("QC-TRF") )&& (!this.tranSer.equalsIgnoreCase("PR-AMD")) && (!this.tranSer.equalsIgnoreCase("W-QCS")) )
			{
				sql=" SELECT COUNT(*) FROM QC_ORDER WHERE  SITE_CODE = '"+this.siteCode+"'"
					+"AND	 LOC_CODE  = '"+this.locationCode+"'"
					+"AND	 ITEM_CODE = '"+this.itemCode+"'"
					+"AND	 LOT_NO 	  = '"+this.lotNo+"'"
					+"AND	 LOT_SL 	  = '"+this.lotSl+"'"
					+"AND	 STATUS 	 <> 'C'"; 
				System.out.println("Query :::- ["+sql+"]");
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next()){
                    count = rs.getInt(1);
				}
				pstmt.close();
				rs.close();
                System.out.println("Record from QC_ORDER:::- ["+count+"]");
                
                if(count == 0)
                {
                    sql =" SELECT COUNT(*)  FROM  QC_ORDER WHERE  SITE_CODE = '"+this.siteCode+"'"
                        +" AND LOC_CODE  = '"+this.locationCode+"'"
                        +"AND ITEM_CODE = '"+this.itemCode+"'"
                        +"AND  LOT_NO     = '"+this.lotNo+"'"
                        +"AND    LOT_SL IS NULL "
                        +"  AND  STATUS      <> 'C' ";
                        System.out.println("Query :::- ["+sql+"]");
                        pstmt = conn.prepareStatement(sql);
                        rs = pstmt.executeQuery();
                        if(rs.next())
                        {
                            count = rs.getInt(1);
                        }
                        pstmt.close();
                        rs.close();
                        System.out.println("Record from QC_ORDER:::- ["+count+"]");
                }
			/*	//Commented n Added by Jasmina-05/05/08-DI89SUN004,Script for updating loc_code & lot_sl as null if qc order is found during stock transfer
				//Error Uncommented by Jasmina 08/07/08 -DI89SUN004, 
				//Error While be bypass if transer = 'XFRX' and trantype = 'ID' i.e Stock transfer and quantity < 0 i.e Not Receipt 
				if s_updatestock.transer = 'XFRX' and s_updatestock.trantype = 'ID' and s_updatestock.quantity < 0 Then 
				else
					populateerror(9999,'populateerror')
					ls_errcode = 'VTUNCFQC'
					ls_errcode = gf_error_location(ls_errcode)
					return ls_errcode
				end if
				//Error Uncommented end by Jasmina 08/07/08 -DI89SUN004
			*/
				// 24/10/13 manoharan added this condition missing from PB source
				if( (count > 0) && !("XFRX".equalsIgnoreCase(this.tranSer) && "ID".equalsIgnoreCase(this.tranType)  && meffQty < 0) )
				{
                    errString = "VTUNCFQC";
                    errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
                    return errString;
                }
			}

			if (this.tranType.equalsIgnoreCase("I") || this.tranType.equalsIgnoreCase("ID") )
			{
				boolean stkcheck = false;
				sql =" select rate 	from stock 	where item_code = '"+this.itemCode+"'"
				    +" and site_code = '"+this.siteCode+"'" 
					+" and loc_code  = '"+this.locationCode+"'" 
					+" and lot_no = '"+this.lotNo+"'"
					+" and lot_sl = '"+this.lotSl+"'";
					System.out.println("Query :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						stkcheck = true;
						this.rate = rs.getDouble(1);
					}
					pstmt.close();
					rs.close();
					if(!stkcheck)
					{												
						errString = itmDBAccessEJB.getErrorString("","VSTOCKER","","",conn);
	                    return errString;	                    
					}
                    ///////////////////
                    errString = calcWeights(conn);
                    if(errString != null && errString.trim().length() >0){ 
                        errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
                        return errString;
                    }
			}
			if (CommonConstants.DB_NAME.equalsIgnoreCase("DB2")){
				sql =" select quantity, inv_stat from stock where item_code = '"+this.itemCode+"'"
			  	+" and site_code = '"+this.siteCode+"'" 
				+" and loc_code  = '"+this.locationCode+"'" 
				+" and lot_no = '"+this.lotNo+"'"
				+"and lot_sl = '"+this.lotSl+"' for update ";
			}
			else if (CommonConstants.DB_NAME.equalsIgnoreCase("MSSQL")){
				sql =" select quantity, inv_stat from stock (updlock) where item_code = '"+this.itemCode+"'"
			  	+" and site_code = '"+this.siteCode+"'" 
				+" and loc_code  = '"+this.locationCode+"'" 
				+" and lot_no = '"+this.lotNo+"'"
				+"and lot_sl = '"+this.lotSl+"'  ";
			}
			else
			{
				sql =" select quantity, inv_stat from stock where item_code = '"+this.itemCode+"'"
			  	+" and site_code = '"+this.siteCode+"'" 
				+" and loc_code  = '"+this.locationCode+"'" 
				+" and lot_no = '"+this.lotNo+"'"
				+"and lot_sl = '"+this.lotSl+"' for update nowait ";
			}
			System.out.println("Query :::- ["+sql+"]");
            pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mbefQty=rs.getDouble(1);
				minvStat=rs.getString(2);				
			}
			else  
			{  				
				if (this.tranType.equalsIgnoreCase("I") || this.tranType.equalsIgnoreCase("ID") )// condiotn added by nandkumar gadkari on 15/04/19
				{
					rs.close();
					pstmt.close();				
					errString = itmDBAccessEJB.getErrorString("","VTLCKERR","","",conn);
	                return errString;
				}
			}
			pstmt.close();
			rs.close();
			if (this.tranType.equalsIgnoreCase("R"))
			{
				mafterQty = mbefQty + this.qtyStduom;
				effQty = this.qtyStduom;
			}
			else
			{
				mafterQty = mbefQty - this.qtyStduom;
				effQty = this.qtyStduom * -1;
			}
			//****************added by priyanka
			if((this.tranType.equalsIgnoreCase("R")))
			{
				System.out.println(">>>>>>>>>>>>>tranType.equalsIgnoreCase R");
				sql="select count(1) From inv_hold a, inv_hold_det b Where a.tran_id = b.tran_id And b.item_code  = ?	And (b.site_code = ? or b.site_code is null )" +
							"	And (b.loc_code  = ?  or b.loc_code is null )	" +
							"	And (b.lot_no    =? or b.lot_no is null )	" +
							"	And (b.lot_sl    = ? or b.lot_sl is null )		" +
							"And (b.line_no_sl = 0 or b.line_no_sl is null)	" +
							"	And a.confirmed='Y' And b.hold_status ='H'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,this.itemCode);
					pstmt.setString(2,this.siteCode);
					pstmt.setString(3,this.locationCode);
					pstmt.setString(4,this.lotNo);
					pstmt.setString(5,this.lotSl);
					
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cntHold = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println(">>>>>>>>>>>cntHold===="+cntHold);
					
			}
			if(updateStockMap.get("hold_lock")!= null)
    		{		
				holdLock=(String) updateStockMap.get("hold_lock");
    		}
			
			System.out.println("holdLock========="+holdLock);
			//Start added by chandrashekar on 08-jan-2015
		/*	quarantineLock = distCommon.getDisparams("999999", "QUARNTINE_LOCKCODE", conn);
			System.out.println("quarantineLock:::::" + quarantineLock);
			if (quarantineLock == null || "NULLFOUND".equalsIgnoreCase(quarantineLock) || quarantineLock.trim().length() == 0)
			{
				errString = "VTQUARLOCK";
				errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				return errString;
			}*/
			//End added by chandrashekar on 08-jan-2015
			if(holdLock==null||holdLock.trim().length()==0)
			{
					holdLock="N";
			}
					
			if(holdLock.equals("N") && cntHold==0)
					//if(holdLock.equals("N") && !(this.tranType.equalsIgnoreCase("R")) )
			{
				/*sql="select count(1) as count ,a.lock_code as lock_code From inv_hold a, inv_hold_det b	Where a.tran_id = b.tran_id And b.item_code  = ?	" +
								"And (b.site_code = ? or b.site_code is null ) " +
								"And (b.loc_code  = ?  or b.loc_code is null )	" +
								"And (b.lot_no    = ? or b.lot_no is null )	" +
								"And (b.lot_sl    = ? or b.lot_sl is null )" +
								"And (b.line_no_sl = 0 or b.line_no_sl is null)" +
								"And a.confirmed='Y' And b.hold_status ='H' group by a.lock_code";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,this.itemCode);
						pstmt.setString(2,this.siteCode);
						pstmt.setString(3,this.locationCode);
						pstmt.setString(4,this.lotNo);
						pstmt.setString(5,this.lotSl);
						
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							//cnt1 = rs.getInt(1);
							cnt1 = rs.getInt("count");
							lockCode =rs.getString("lock_code") == null ? " " : rs.getString("lock_code").trim();
							lockCodeList.add(lockCode);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//Start added by chandrashekar on 08-jan-2015
						System.out.println("locCodeList:::["+lockCodeList+"]size:"+lockCodeList.size());
						*/
						quarantineLock = distCommon.getDisparams("999999", "QUARNTINE_LOCKCODE", conn);
						System.out.println("quarantineLock:::::" + quarantineLock);
						/*if (quarantineLock == null || "NULLFOUND".equalsIgnoreCase(quarantineLock) || quarantineLock.trim().length() == 0)
						{
							errString = "VTQUARLOCK";
							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
							return errString;
						}*/
						//Start Change by chandrashekar on20-MAR-2015
						sql="select count(1) as count  From inv_hold a, inv_hold_det b	Where a.tran_id = b.tran_id And b.item_code  = ?	" +
								"And (b.site_code = ? or b.site_code is null ) " +
								"And (b.loc_code  = ?  or b.loc_code is null )	" +
								"And (b.lot_no    = ? or b.lot_no is null )	" +
								"And (b.lot_sl    = ? or b.lot_sl is null )" +
								"And (b.line_no_sl = 0 or b.line_no_sl is null)" +
								" And a.lock_code != ? "+
								" And a.confirmed='Y' And b.hold_status ='H'";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,this.itemCode);
						pstmt.setString(2,this.siteCode);
						pstmt.setString(3,this.locationCode);
						pstmt.setString(4,this.lotNo);
						pstmt.setString(5,this.lotSl);
						pstmt.setString(6,quarantineLock);
						
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							cnt2 = rs.getInt("count");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("cnt2>>>>"+cnt2);
						if(cnt2>0)
						{
							errString = "VTSTKHOLD";
							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
							return errString;
						}
						// End Change by chandrashekar on20-MAR-2015
						/*for(int i = 0; i < lockCodeList.size() ; i++)
						{
							lockCode=lockCodeList.get(i);
							if (lockCode.trim().length() > 0 && !("NULLFOUND".equalsIgnoreCase(quarantineLock)) && quarantineLock.trim().length() > 0	)
							{
								if(!(lockCode.equalsIgnoreCase(quarantineLock.trim())))
								{
									if(cnt1>0)
									{
										errString = "VTSTKHOLD";
										errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
										return errString;
									}
								}
							}
						}*/
						//End added by chandrashekar on 08-jan-2015
						
						/*if(cnt1>0)
						{
							cnt1=0;
							sql="select count(1)From inv_hold a, inv_hold_det b	Where a.tran_id = b.tran_id And b.item_code  = ?	" +
									"And (b.site_code = ? or b.site_code is null ) " +
									"And (b.loc_code  = ?  or b.loc_code is null )	" +
									"And (b.lot_no    = ? or b.lot_no is null )	" +
									"And (b.lot_sl    = ? or b.lot_sl is null )" +
									"And (case when b.line_no_sl is null then 0 else b.line_no_sl end ) > 0	" +
									"And a.confirmed='Y' And b.hold_status ='H'";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,this.itemCode);
							pstmt.setString(2,this.siteCode);
							pstmt.setString(3,this.locationCode);
							pstmt.setString(4,this.lotNo);
							pstmt.setString(5,this.lotSl);
							
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt1 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						
						
						if(cnt1==0)
						{
							errString = "VTSTKHOLD";
							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
							return errString;
						}
							
					}*/
				}
			if((!this.tranType.equalsIgnoreCase("R")) && (!this.tranType.equalsIgnoreCase("D")))
			{
				if(mbefQty < this.qtyStduom)
				{
					if(this.invStat != null && this.invStat.trim().length() > 0){
						sql = "select overiss  from invstat where inv_stat = '"+this.invStat+"'";
					}
					else{
						sql = "select overiss  from invstat	where inv_stat = '"+minvStat+"'";
					}
                    System.out.println("Query :::- ["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next()){
						moverIssue=(rs.getString(1)== null ? "":rs.getString(1));
					}
					pstmt.close();
					rs.close();
					if(!moverIssue.equalsIgnoreCase("Y")){
						errString = "VTOVERISS1";
						errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						return errString;
					}
				
				}
			}
            if(this.grade != null && this.grade.trim().length() == 0){
                this.grade = "   ";
            }
			if(!this.tranType.equalsIgnoreCase("R")){
				if((this.acctCodeCr==null )|| this.acctCodeCr.trim().length()==0 || this.cctrCodeCr== null || this.cctrCodeCr.trim().length()==0 ){
					sql = "select acct_code__inv,cctr_code__inv from stock where item_code = '"+this.itemCode+"'"
				  	+" and site_code = '"+this.siteCode+"'" 
					+" and loc_code  = '"+this.locationCode+"'" 
					+" and lot_no = '"+this.lotNo+"'"
					+" and lot_sl = '"+this.lotSl+"'  ";
                    
    				System.out.println("Query :::- ["+sql+"]");
    				pstmt = conn.prepareStatement(sql);
    				rs = pstmt.executeQuery();
    				if(rs.next())
    				{
    					this.acctCodeCr=rs.getString(1);
    					this.cctrCodeCr = rs.getString(2);
    				}
    				pstmt.close();
    				rs.close();
                }
			}
			
			if (!this.tranSer.equalsIgnoreCase("SCRRCP")){
				System.out.println("Transer "+tranSer);
			    String tranDateStr = genericUtility.getValidDateString(this.tranDate.toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
				String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues +	"<tran_id></tran_id>";
				xmlValues = xmlValues + "<site_code>" + this.siteCode + "</site_code>";
				xmlValues = xmlValues + "<tran_date>"+ tranDateStr + "</tran_date>";
				xmlValues = xmlValues + "</Detail1></Root>";
	            minvtraceNo= generateTranId("T_INVTRACE",xmlValues,conn);
                System.out.println("Generated Tran id ::- ["+minvtraceNo+"]");
	            // Changed By Nasruddin 21-11-16 Start bug Fix    
	            mamountStr = utilMethods.getReqDecString(mamount,3);
	            mamount = Double.parseDouble(mamountStr);
	            System.out.println("mamount @@@@@@@@ ::"+mamount);
	         // Changed By Nasruddin 21-11-16 END BUG FIX
	            sql = "Insert into invtrace (tran_id, tran_date, ref_ser, ref_id, ref_line, order_id, item_code, site_code, loc_code, unit,"
	             	+" lot_no, quantity, eff_qty, eff_date, chg_win,  chg_user, chg_term, lot_sl, qty_before, qty_after,"
					+" reas_code,sundry_type,sundry_code, ref_date, rate, gross_rate, grade, acct_code__dr, cctr_code__dr, acct_code__cr, cctr_code__cr,no_art, amount,remarks,"
					+ " gross_weight,tare_weight,net_weight, chg_date, inv_stat,ref_ser__for, ref_id__for) "
					+" values (?, ?, ?, ?,?,?, ?,?,?,?,?, ?,?,?,?, ?,?, ?,?, ?, ?,?,?,?,?,?,?,?,?,?,?, ?,?,?,"
					+"?,?,?, ?, ?, ?,?) ";
             		pstmt = conn.prepareStatement(sql);
                    System.out.println("Query :::- ["+sql+"]");
	             	pstmt.setString(1,minvtraceNo);
	             	pstmt.setTimestamp(2,this.tranDate);
	             	pstmt.setString(3,this.tranSer);
	             	pstmt.setString(4,this.tranId);
	             	pstmt.setString(5,this.lineNo);
	             	pstmt.setString(6,this.sorderNo);
	             	pstmt.setString(7,this.itemCode);
	             	pstmt.setString(8,this.siteCode);
	             	pstmt.setString(9,this.locationCode);
	             	pstmt.setString(10,mbaseUnit);
	             	pstmt.setString(11,this.lotNo);
	             	pstmt.setDouble(12,Math.abs(this.qtyStduom));
	             	pstmt.setDouble(13,effQty);
	             	pstmt.setTimestamp(14,this.tranDate);
	             	pstmt.setString(15," ");
	             	pstmt.setString(16,userId);
	             	pstmt.setString(17,termId);
	             	pstmt.setString(18,this.lotSl);
	             	pstmt.setDouble(19,mbefQty);
	             	pstmt.setDouble(20,mafterQty);
	             	pstmt.setString(21,this.reasCode);
	             	pstmt.setString(22,this.sundryType);
	             	pstmt.setString(23,this.sundryCode);
	             	pstmt.setTimestamp(24,this.tranDate);
	             	pstmt.setDouble(25,this.rate);
	             	pstmt.setDouble(26,this.grossRate);
	             	pstmt.setString(27,this.grade);
	             	pstmt.setString(28,this.acctCodeDr);
	             	pstmt.setString(29,this.cctrCodeDr);
	             	pstmt.setString(30,this.acctCodeCr);
	             	pstmt.setString(31,this.cctrCodeCr);
	             	pstmt.setDouble(32,this.noArt); 
	             	//pstmt.setDouble(33,mamount);
	             	pstmt.setString(33,mamountStr);
	             	pstmt.setString(34,this.remarks);
	             	pstmt.setDouble(35,this.grossWeight);
	             	pstmt.setDouble(36,this.tareWeight);
	             	pstmt.setDouble(37,this.netWeight);
	             	pstmt.setTimestamp(38,new Timestamp(System.currentTimeMillis()));//chgdate
	             	pstmt.setString(39,this.invStat);
	             	pstmt.setString(40,this.refSerFor);
	             	pstmt.setString(41,this.refIdFor);
					
					update = pstmt.executeUpdate();
                    System.out.println("No recore Insert invtrace ::- ["+update+"]");
					pstmt.close();
					/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
					if(update > 0 )
					{
						InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
						HashMap demandSupplyMap = null;
						if( !("D-ISS".equalsIgnoreCase(this.tranSer.trim()) || "S-DSP".equalsIgnoreCase(this.tranSer.trim())) )
						{	
							int invStCnt = 0;
							sql = "select count(*) from location, invstat "	
							+ " where location.inv_stat = invstat.inv_stat " 
							+ " and location.loc_code = ? "
							+ " and invstat.stat_type <> 'S'"; 	
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, this.locationCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								invStCnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							if( invStCnt > 0 )
							{
							    demandSupplyMap = new HashMap();
							    demandSupplyMap.put("site_code", this.siteCode);
								demandSupplyMap.put("item_code", this.itemCode);		
								demandSupplyMap.put("ref_ser", "STK");
								demandSupplyMap.put("ref_id", "NA");
								demandSupplyMap.put("ref_line", "NA");
								demandSupplyMap.put("due_date", this.tranDate);		
								demandSupplyMap.put("demand_qty", 0.0);
								demandSupplyMap.put("supply_qty", effQty);
								demandSupplyMap.put("change_type", "A");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", userId);
							    demandSupplyMap.put("chg_term", termId);	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("retString["+errString+"]");
							    	return errString;
							    }
							}
						}												
					}
				  /**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
		
			}
            errString = updateStockMovement(conn);
            if(errString != null && errString.trim().length() > 0) 
                return errString;
            
			
			sql = "select fin_entity  from site where site_code = '"+this.siteCode+"'";
            System.out.println("Query :::- ["+sql+"]");
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mfinEntity=rs.getString(1);				
			}
			pstmt.close();
			rs.close();
			
			sql = "select curr_code  from parameter";
            System.out.println("Query :::- ["+sql+"]");
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mcurrCode=rs.getString(1);				
			}
			pstmt.close();
			rs.close();
			
			mexcRate = 1;
            
            //This is commented as this is exp date not used further in PB logic - Jiten 10/04/07
            //if required the code will be added
			/*if isnull(mshlife) or mshlife = 0 then
			setnull(mexpdate)
			else
			mnewexpdt = relativedate(date(s_updatestock.trandate), mshlife)
			mexpdate  = datetime(mnewexpdt)
			end if*/
			//End
            
            mamount = this.quantity * this.rate;
         // Changed By Nasruddin 21-11-16 Start bug Fix    
            mamountStr = utilMethods.getReqDecString(mamount,3);
            mamount = Double.parseDouble(mamountStr);
         // Changed By Nasruddin 21-11-16 END
			count=0;
//			*********xxxxxxxxxxxxxxxx********************
			//changes by Dadaso pawar on 25/02/15 [start] (Pragyan sir suggestion)
			if(this.lotNo == null || this.lotNo.trim().length() == 0)
			{
				this.lotNo ="               ";
			}
			if(this.lotSl == null || this.lotSl.trim().length()==0)
			{
				this.lotSl = "     ";
			}
			 
			sql = "select count(*) from stock where item_code ='"+this.itemCode.trim()+"'" 
					+" and site_code ='"+this.siteCode.trim()+"'"
					+" and loc_code = '"+this.locationCode.trim()+"'"
					+" and lot_no = '"+this.lotNo+"'" 
					+" and lot_sl = '"+this.lotSl+"'";
			/*sql = "select count(*) from stock where item_code ='"+this.itemCode.trim()+"'" 
			+" and site_code ='"+this.siteCode.trim()+"'"
			+" and loc_code = '"+this.locationCode.trim()+"'"
			+" and lot_no = '"+this.lotNo.trim()+"'" 
			+" and lot_sl = '"+this.lotSl.trim()+"'";*/
			
			//changes by Dadaso pawar on 25/02/15 [End]
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count=rs.getInt(1);				
			}
			System.out.println("@@@@test :count::::["+count+"]");
			pstmt.close();
			rs.close();
			if(count != 0)
			{
				sql =" select quantity,alloc_qty, inv_stat 	from stock 	where item_code = '"+this.itemCode+"'"
			  	+" and site_code = '"+this.siteCode+"'" 
				+" and loc_code  = '"+this.locationCode+"'" 
				+" and lot_no = '"+this.lotNo+"'"
				+"and lot_sl = '"+this.lotSl+"'  ";
                System.out.println("Query :::- ["+sql+"]");
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					mstkQty=rs.getDouble(1);
					mallocQty = rs.getDouble(2);
					minvStat = rs.getString(3);					
				}
                pstmt.close();
                rs.close();				
			}
			mstkQty = finCommon.getRequiredDecimal(mstkQty,4);
            mallocQty = finCommon.getRequiredDecimal(mallocQty,4);
            meffQty = finCommon.getRequiredDecimal(meffQty,4);
			if(this.tranType.equalsIgnoreCase("R"))
			{
				if (stkOpt.equalsIgnoreCase("1") || stockValuation.equalsIgnoreCase("M"))
				{
					sql ="Select   (Case when (sum(Case When quantity IS NULL Then 0 Else quantity End)) is null Then 0 else (sum(Case When quantity IS NULL Then 0 Else quantity End)) end),"
						+"(case when (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) is null Then 0 else (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) end),"
						+"(case when sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) is null then 0 else sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) end )"
						+"	from 	 stock"
						+" where  item_code = '"+this.itemCode+"'"
						+" and 	 site_code = '"+this.siteCode+"'" 
						+" and 	 grade 	  = '"+this.grade+"'" ;
					System.out.println("Query Fired :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						oldQty=rs.getDouble(1);
						oldvalue = rs.getDouble(2);
						oldGrossValue =rs.getDouble(3);
					}
					System.out.println("oldQty::["+oldQty+"]::oldvalue::["+oldvalue+"]::oldGrossValue::["+oldGrossValue+"]");
                    pstmt.close();
					rs.close();
					newValue = Math.abs(this.qtyStduom) * this.rate;//13*11=143
					newGrossValue = Math.abs(this.qtyStduom) * this.grossRate;
					//totalValue = oldValue + newValue;//0+143 = 143 // commented by azhar as variable oldValue was incorrect
					totalValue = oldvalue + newValue;//0+143 = 143
					totalGrossValue = oldGrossValue + newGrossValue	;
					if((oldQty + Math.abs(this.qtyStduom)) != 0)
					{
						this.rate = (totalValue)/(oldQty+Math.abs(this.qtyStduom));
						this.grossRate = totalGrossValue / (oldQty + Math.abs(qtyStduom));
					}
					System.out.println("rate::["+this.rate+"]:::gross rate::["+this.grossRate+"]");
				}
				else if(stockValuation.equalsIgnoreCase("C")){
					sql =" Select   (Case when (sum(Case When quantity IS NULL Then 0 Else quantity End)) is null Then 0 else (sum(Case When quantity IS NULL Then 0 Else quantity End)) end),"
						+" (case when (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) is null Then 0 else (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) end),"
						+" (case when sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) is null then 0 else sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) end )"
						+" from stock "
						+" where  item_code = '"+this.itemCode+"'"
						+" and 	 site_code = '"+this.siteCode+"'" 
						+" and 	 grade 	  = '"+this.grade+"'" 
						+" And	cctr_code__inv = :'"+this.cctrCodeInv+"'";
						System.out.println("Query :::- ["+sql+"]");
                        pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							oldQty=rs.getDouble(1);
							oldvalue = rs.getDouble(2);
							oldGrossValue =rs.getDouble(3);
						}
                        pstmt.close();
						rs.close();
						newValue = Math.abs(this.qtyStduom) * this.rate;
						newGrossValue = Math.abs(this.qtyStduom) * this.grossRate;
						//totalValue = oldValue + newValue;//commented by azhar as variable oldValue was incorrect
						totalValue = oldvalue + newValue;
						totalGrossValue = oldGrossValue + newGrossValue	;
						if((oldQty + Math.abs(this.qtyStduom))!= 0)
						{
							this.rate = totalValue / (oldQty + Math.abs(this.qtyStduom));
							this.grossRate = totalGrossValue / (oldQty + Math.abs(this.qtyStduom));
				
						}
				}				
				//Pavan Rane 25Jul19 Start [Migrated code from pb DI78GIM001]				
				else if (stkOpt.equalsIgnoreCase("2") && count > 0 )
				{
					  sql = "Select (Case when (sum(Case When quantity IS NULL Then 0 Else quantity End)) is null Then 0 else (sum(Case When quantity IS NULL Then 0 Else quantity End)) end),"
                       +" (case when (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) is null Then 0 else (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) end),"
                       +" (case when sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) is null then 0 else sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) end )"                      
                       +" from     stock,location,invstat"
                       +" where    stock.loc_code         =location.loc_code"
                       +" and      location.inv_stat     = invstat.inv_stat"
                       +" and      invstat.stat_type     <> 'S'"
                       +" and      stock.item_code         = '"+this.itemCode+"'"
                       +" and      stock.site_code         ='"+this.siteCode+"'"  
                       +" and      stock.grade               = '"+this.grade+"'" 
                       +" and      stock.lot_no               = '"+this.lotNo+"'  "; 
				
					System.out.println("Query Fired :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						oldQty=rs.getDouble(1);
						oldvalue = rs.getDouble(2);
						oldGrossValue =rs.getDouble(3);
					}					
                    pstmt.close(); pstmt = null;
					rs.close(); rs= null;
					System.out.println("oldQty::["+oldQty+"]::oldvalue::["+oldvalue+"]::oldGrossValue::["+oldGrossValue+"]");
					
					newValue = Math.abs(this.qtyStduom) * this.rate;
					newGrossValue = Math.abs(this.qtyStduom) * this.grossRate;					
					totalValue = oldvalue + newValue;
					totalGrossValue = oldGrossValue + newGrossValue	;
					if((oldQty + Math.abs(this.qtyStduom)) != 0)
					{
						this.rate = (totalValue)/(oldQty+Math.abs(this.qtyStduom));
						this.grossRate = totalGrossValue / (oldQty + Math.abs(qtyStduom));
					}
					System.out.println("rate::["+this.rate+"]:::gross rate::["+this.grossRate+"]");
				}																							
			//}//end of tranType =R				
			} 
			else
			{
				if (stkOpt.equalsIgnoreCase("1") || stockValuation.equalsIgnoreCase("M"))
				{									
		            sql = "Select   (Case when (sum(Case When quantity IS NULL Then 0 Else quantity End)) is null Then 0 else (sum(Case When quantity IS NULL Then 0 Else quantity End)) end),"
		            	+" (case when (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) is null Then 0 else (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) end),"
		                +" (case when sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) is null then 0 else sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) end )"
			            +" from     stock,location,invstat"
			            +" where 	stock.loc_code         =location.loc_code"
			            +" and    	location.inv_stat     = invstat.inv_stat"
			            +" and    	invstat.stat_type     <> 'S' "
			            +" and    	stock.item_code         = '"+this.itemCode+"'"
			            +" and      stock.site_code         = '"+this.siteCode+"'" 
			            +" and      stock.grade               = '"+this.grade+"'" ;		           
					System.out.println("Query Fired :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						oldQty=rs.getDouble(1);
						oldvalue = rs.getDouble(2);
						oldGrossValue =rs.getDouble(3);
					}
					System.out.println("oldQty::["+oldQty+"]::oldvalue::["+oldvalue+"]::oldGrossValue::["+oldGrossValue+"]");                    
					rs.close(); rs = null;
					pstmt.close(); pstmt =null;					
					newValue = Math.abs(this.qtyStduom) * this.rate;
					newGrossValue = Math.abs(this.qtyStduom) * this.grossRate;						
					totalValue = oldvalue + newValue;
					totalGrossValue = oldGrossValue + newGrossValue	;
					if((oldQty + Math.abs(this.qtyStduom)) != 0)
					{
						this.rate = (totalValue)/(oldQty+Math.abs(this.qtyStduom));
						this.grossRate = totalGrossValue / (oldQty + Math.abs(qtyStduom));
					}
					System.out.println("rate::["+this.rate+"]:::gross rate::["+this.grossRate+"]");
				}
				else if(stockValuation.equalsIgnoreCase("C"))
				{
				     sql = "Select   (Case when (sum(Case When quantity IS NULL Then 0 Else quantity End)) is null Then 0 else (sum(Case When quantity IS NULL Then 0 Else quantity End)) end),"
			        	+" (case when (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) is null Then 0 else (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) end),"
			        	+" (case when sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) is null then 0 else sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) end )"
				        +" from          stock,location,invstat"
				        +" where      stock.loc_code         = location.loc_code"
				        +" and      location.inv_stat         = invstat.inv_stat"
				        +" and      invstat.stat_type       <> 'S' "
				        +" and       stock.item_code         = '"+this.itemCode+"'"
				        +" and         stock.site_code         = '"+this.siteCode+"'"
				        +" and         stock.grade              = '"+this.grade+"'"
				        +" And         stock.cctr_code__inv = '"+this.cctrCodeInv+"'";
					System.out.println("Query :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						oldQty=rs.getDouble(1);
						oldvalue = rs.getDouble(2);
						oldGrossValue =rs.getDouble(3);
					}
                    pstmt.close();
					rs.close();
					System.out.println("oldQty::["+oldQty+"]::oldvalue::["+oldvalue+"]::oldGrossValue::["+oldGrossValue+"]");
					
					newValue = Math.abs(this.qtyStduom) * this.rate;
					newGrossValue = Math.abs(this.qtyStduom) * this.grossRate;						
					totalValue = oldvalue + newValue;
					totalGrossValue = oldGrossValue + newGrossValue	;
					if((oldQty + Math.abs(this.qtyStduom))!= 0)
					{
						this.rate = totalValue / (oldQty + Math.abs(this.qtyStduom));
						this.grossRate = totalGrossValue / (oldQty + Math.abs(this.qtyStduom));				
					}
				}
				else if (stkOpt.equalsIgnoreCase("2") && count > 0 )
				{
					sql = "Select   (Case when (sum(Case When quantity IS NULL Then 0 Else quantity End)) is null Then 0 else (sum(Case When quantity IS NULL Then 0 Else quantity End)) end),"
	                  +" (case when (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) is null Then 0 else (sum((Case When quantity IS NULL Then 0 Else quantity End )  * (Case When rate IS NULL Then 0 Else rate End ))) end),"
	                  +" (case when sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) is null then 0 else sum((Case When quantity IS NULL Then 0 Else quantity End ) * (Case When gross_rate IS NULL Then 0 Else gross_rate End )) end )"
	                  +" from      stock,location,invstat"
					  +" where  stock.loc_code         =location.loc_code"
					  +" and    location.inv_stat     = invstat.inv_stat"
					  +" and    invstat.stat_type     <> 'S' "     
					  +" and    stock.item_code         = '"+this.itemCode+"'"
					  +" and      stock.site_code       = '"+this.siteCode+"'"
					  +" and      stock.grade           = '"+this.grade+"'"
					  +" and      stock.lot_no          = '"+this.lotNo+"'  ";      
					
					System.out.println("Query Fired :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						oldQty=rs.getDouble(1);
						oldvalue = rs.getDouble(2);
						oldGrossValue =rs.getDouble(3);
					}					
                    pstmt.close(); pstmt = null;
					rs.close(); rs= null;
					System.out.println("oldQty::["+oldQty+"]::oldvalue::["+oldvalue+"]::oldGrossValue::["+oldGrossValue+"]");
					
					newValue = Math.abs(this.qtyStduom) * this.rate;
					newGrossValue = Math.abs(this.qtyStduom) * this.grossRate;					
					totalValue = oldvalue + newValue;
					totalGrossValue = oldGrossValue + newGrossValue	;
					if((oldQty + Math.abs(this.qtyStduom)) != 0)
					{
						this.rate = (totalValue)/(oldQty+Math.abs(this.qtyStduom));
						this.grossRate = totalGrossValue / (oldQty + Math.abs(qtyStduom));
					}
					System.out.println("rate::["+this.rate+"]:::gross rate::["+this.grossRate+"]");
				}
			}
				//Pavan Rane 25Jul19 end
			if(this.tranType.equalsIgnoreCase("I")){
				mstkQty = (mstkQty - meffQty);
				if (count > 0)
				{
					System.out.println("@V@ tanser>>>>"+this.tranSer);
					if(this.tranSer.equalsIgnoreCase("QC-ORD"))
					{
						System.out.println("@V@ QC-ORD.......");
						sql=" update stock set quantity   = quantity - ? ,"
						+" ltran_date = ? ,"
						 +" last_iss_date = ? ,"
						 +" chg_user   = ? ," 
						 +" chg_term   = ? , " 
						 +" chg_date   = ? ,"
						 +" gross_weight = gross_weight - ? ,"
						 +" net_weight = net_weight - ? ,"
						 +" tare_weight = tare_weight - ? ,"
						 +" last_phyc_date = ? ,"
						 +" potency_perc = ? ,"
						 +" no_art 			= no_art - ? " 					// VALLABH KADAM
						 +" where item_code = '"+this.itemCode+"'"
						 +" and site_code = '"+this.siteCode+"'"
						 +" and loc_code  = '"+this.locationCode+"'" 
						 +" and lot_no 	 = '"+this.lotNo+"'"
						 +" and lot_sl 	 = '"+this.lotSl+"'";
                        System.out.println("Query :::- ["+sql+"]");
    					pstmt = conn.prepareStatement(sql);
    					pstmt.setDouble(1,this.qtyStduom);
    					pstmt.setTimestamp(2,this.tranDate);
    					pstmt.setTimestamp(3,this.tranDate);
    					pstmt.setString(4,userId);
    					pstmt.setString(5,termId);
    					pstmt.setTimestamp(6,new java.sql.Timestamp(System.currentTimeMillis()));
    					pstmt.setDouble(7,this.grossWeight);
    					pstmt.setDouble(8,this.netWeight);
    					pstmt.setDouble(9,this.tareWeight);
    					pstmt.setTimestamp(10,this.lastPhycDate);
    					pstmt.setDouble(11,this.potencyPerc);
    					pstmt.setDouble(12,this.noArt);
    	
    					
    					pstmt.executeUpdate();
    					pstmt.close();
    					//rs.close(); Pavan Rane 26jul19 [unnecessary open cursor closed]
    					
                    }
                    else
                    {
						System.out.println("@V@ Not QC- ORD..........");
                        sql=" update stock set quantity   = quantity - ? ,"
                            +" ltran_date = ? ,"
                            +" last_iss_date = ? ,"
                            +" chg_user   = ? ," 
                            +" chg_term   = ? , " 
                            +" chg_date   = ? ,"
                            +" gross_weight = gross_weight - ? ,"
                            +" net_weight = net_weight - ? ,"
                            +" tare_weight = tare_weight - ? ,"
                            +" last_phyc_date = ? ,"
                            +" no_art 			= no_art - ? "
                            +" where item_code = '"+this.itemCode+"'"
                            +" and site_code = '"+this.siteCode+"'"
                            +" and loc_code  = '"+this.locationCode+"'" 
                            +" and lot_no   = '"+this.lotNo+"'"
                            +" and lot_sl   = '"+this.lotSl+"'";
                            System.out.println("@V@ Query :::- ["+sql+"]");
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setDouble(1,this.qtyStduom);
                            pstmt.setTimestamp(2,this.tranDate);
                            pstmt.setTimestamp(3,this.tranDate);
                            pstmt.setString(4,userId);
                            pstmt.setString(5,termId);
                            pstmt.setTimestamp(6,new java.sql.Timestamp(System.currentTimeMillis()));
                            pstmt.setDouble(7,this.grossWeight);
                            pstmt.setDouble(8,this.netWeight);
                            pstmt.setDouble(9,this.tareWeight);
                            pstmt.setTimestamp(10,this.lastPhycDate);
                            pstmt.setDouble(11,this.noArt);
                            
                            pstmt.executeUpdate();
                            pstmt.close();
                           // rs.close(); Pavan Rane 26jul19 [unnecessary open cursor closed]
                            
                          //Added by chandrashekar dtd 05/02/2015 to update no_art as 1 if no_art<0
            				double stkQty=0,noArticle=0;
            				sql="select quantity,no_art from stock " +
            						" where item_code = '"+this.itemCode+"'" 
            					+"and site_code = '"+this.siteCode +"'"
            					+"and loc_code = '"+this.locationCode +"'"
            					+"and lot_no = '"+this.lotNo+"'"
            					+"and lot_sl = '"+this.lotSl+"'  ";   
            				System.out.println("from sql--- "+sql);
            				pstmt=conn.prepareStatement(sql);
            				rs=pstmt.executeQuery();
            				if(rs.next())
            				{
            					stkQty=rs.getDouble(1);
            					noArticle=rs.getDouble(2);
            				}
            				rs.close();
            				rs=null;
            				pstmt.close();
            				pstmt=null;
            				System.out.println("QcstkQty::["+stkQty+"]");
            				System.out.println("QcnoArticle::["+noArticle+"]");
            				if(stkQty>0 && noArticle<=0)
            				{
            					sql="update stock set no_art=1 " +
            							" where item_code = '"+this.itemCode+"'" 
            					+"and site_code = '"+this.siteCode +"'"
            					+"and loc_code = '"+this.locationCode +"'"
            					+"and lot_no = '"+this.lotNo+"'"
            					+"and lot_sl = '"+this.lotSl+"' ";   
            					System.out.println("sql---"+sql);
            					pstmt=conn.prepareStatement(sql);
            					pstmt.executeUpdate();
            					pstmt.close();
            					pstmt=null;
            				}
            				if(stkQty==0 && noArticle!=0)
            				{
            					sql="update stock set no_art=0 " +
            							" where item_code = '"+this.itemCode+"'" 
            					+"and site_code = '"+this.siteCode +"'"
            					+"and loc_code = '"+this.locationCode +"'"
            					+"and lot_no = '"+this.lotNo+"'"
            					+"and lot_sl = '"+this.lotSl+"' ";   
            					System.out.println("sql---"+sql);
            					pstmt=conn.prepareStatement(sql);
            					pstmt.executeUpdate();
            					pstmt.close();
            					pstmt=null;
            				}
            				//End by chandrashekar dtd 05/02/2015 to update no_art 
                    }
                
                }else{
					sql = "select inv_stat from location where loc_code = '"+this.locationCode+"'";
                    pstmt = conn.prepareStatement(sql);
                    rs = pstmt.executeQuery();
                    if(rs.next()){
                        this.invStat = rs.getString(1);
                    }
                    rs.close();
                    pstmt.close();
                    
                    sql= "select (case when iss_criteria is null then 'I' else iss_criteria end) "
							+" from item where item_code = ? "; //'"+this.itemCode+"'" ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,this.itemCode);
					rs = pstmt.executeQuery();
					if(rs.next()){
						issCriteria=rs.getString(1);
					}
                    rs.close();
                    pstmt.close();
					if (this.noArt==0)
					{
						this.noArt = 1;
					}
					if( issCriteria != null && issCriteria.equalsIgnoreCase("W"))
					{
						qtyPerArt=this.qtyStduom;
						tarewtPerArt = this.tareWeight;
						grosswtPerArt = this.grossWeight;
					}
					else
					{
						qtyPerArt = this.qtyStduom/this.noArt;
						tarewtPerArt = this.tareWeight/this.noArt;
						grosswtPerArt = this.grossWeight/this.noArt;
					}
					 // Changed By Nasruddin 21-11-16 Start bug Fix
					qtyPerArtStr = utilMethods.getReqDecString(qtyPerArt,3);
					qtyPerArt = Double.parseDouble(qtyPerArtStr);
					
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of tare_weight_per_art][Start]
					//tarewtPerArtStr = utilMethods.getReqDecString(qtyPerArt,3);
					tarewtPerArtStr = utilMethods.getReqDecString(tarewtPerArt,3);
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of tare_weight_per_art][End]
					tarewtPerArt = Double.parseDouble(tarewtPerArtStr);
					
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of gross_weight_per_art][Start]
					//grosswtPerArtStr = utilMethods.getReqDecString(qtyPerArt,3);
					grosswtPerArtStr = utilMethods.getReqDecString(grosswtPerArt,3);
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of gross_weight_per_art][end]
					grosswtPerArt = Double.parseDouble(grosswtPerArtStr);
		         // Changed By Nasruddin 21-11-16 END
					sql = "insert into stock  (item_code,site_code,loc_code, lot_no,lot_sl,unit, quantity,crea_date,chg_user,"
					 	+" chg_date,chg_term,exp_date, alloc_qty,item_ser,mfg_date, site_code__mfg,potency_perc,pack_code,"
						+" inv_stat,ltran_date,last_iss_date,gross_weight,tare_weight,net_weight,pack_instr,dimension,retest_date,"
						+"rate,rate__oh,acct_code__inv, cctr_code__inv,acct_code__oh,cctr_code__oh,supp_code__mfg,grade,gross_rate,"
						+" conv__qty_stduom,unit__alt,batch_no,last_phyc_date,qty_per_art,gross_wt_per_art,tare_wt_per_art,actual_rate,remarks, no_art,pack_ref" 
						+" ,lot_sl__org )"    // cpatil
						+" values( ?,?,?, ?,?,?, ?,?,?, ?,?,?,?,?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?,"
						+" ?,?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?)" ;
					 pstmt = conn.prepareStatement(sql);

					 pstmt.setString(1,this.itemCode);
					 pstmt.setString(2,this.siteCode);
					 pstmt.setString(3,this.locationCode);
					 pstmt.setString(4,this.lotNo);
					 pstmt.setString(5,this.lotSl);
					 pstmt.setString(6,mbaseUnit);
					 pstmt.setDouble(7,(-1 * this.qtyStduom));
					 pstmt.setTimestamp(8,creaDt);
					 pstmt.setString(9,userId);
					 pstmt.setTimestamp(10,new java.sql.Timestamp(System.currentTimeMillis()));

					 pstmt.setString(11,termId);
					 pstmt.setTimestamp(12,this.expDate);
					 pstmt.setString(13,"0");
					 pstmt.setString(14,this.itemSer);
					 pstmt.setTimestamp(15,this.mfgDate);
					 pstmt.setString(16,this.siteCodeMfg);
					 pstmt.setDouble(17,this.potencyPerc);
					 pstmt.setString(18,this.packCode);
					 pstmt.setString(19,this.invStat);

					 pstmt.setTimestamp(20,this.currDate);
					 pstmt.setTimestamp(21,this.tranDate);
					 pstmt.setDouble(22,this.grossWeight);
					 pstmt.setDouble(23,this.tareWeight);
					 pstmt.setDouble(24,this.netWeight);
					 pstmt.setString(25,this.packInstr);
					 pstmt.setString(26,this.dimension);
					 pstmt.setTimestamp(27,this.retestDate);

					 pstmt.setDouble(28,this.rate);
					 pstmt.setDouble(29,this.rateOh);
					 pstmt.setString(30,this.acctCodeInv);
					 pstmt.setString(31,this.cctrCodeInv);
					 pstmt.setString(32,this.acctCodeOh);
					 pstmt.setString(33,this.cctrCodeOh);
					 pstmt.setString(34,this.suppCodeMfg);
					 pstmt.setString(35,this.grade);
					 pstmt.setDouble(36,this.grossRate);
					 pstmt.setDouble(37,this.convQtyStduom);
					 pstmt.setString(38,this.unitAlt);


					 pstmt.setString(39,this.batchNo);
					 pstmt.setTimestamp(40,this.lastPhycDate);
					 //pstmt.setDouble(41,qtyPerArt);//qtyPerArtStr
					 pstmt.setString(41,qtyPerArtStr);
					 pstmt.setDouble(42,grosswtPerArt);
					 pstmt.setString(42,grosswtPerArtStr);//grosswtPerArtStr
					 //pstmt.setDouble(43,tarewtPerArt);//tarewtPerArtStr
					 pstmt.setString(43,tarewtPerArtStr);
					 pstmt.setDouble(44,this.actualRate);
					 pstmt.setString(45,this.remarks);
					 pstmt.setString(46,"0");
                    pstmt.setString(47,this.packRef);
                    pstmt.setString(48,this.lotSlOrg);
                     System.out.println("Query ::- ["+sql+"]");
		             
					update = pstmt.executeUpdate();
                    System.out.println("No recore Insert invtrace ::- ["+update+"]");
					pstmt.close();
					
                }
            }
			else if(this.tranType.equalsIgnoreCase("L")){
				mstkQty = (mstkQty - meffQty);
				if (count != 0){
					sql =" update stock  set quantity = ? ," 
						+" ltran_date = ? "
						+" where item_code = '"+this.itemCode+"'" 
						+" and site_code = '"+this.siteCode +"'"
						+" and loc_code = '"+this.locationCode +"'"
						+" and lot_no = '"+this.lotNo+"'"
						+" and lot_sl = '"+this.lotSl+"'";
                    System.out.println("Query :::- ["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1,mstkQty);
					pstmt.setTimestamp(2,this.tranDate);
					pstmt.executeUpdate();
                    //rs.close(); Pavan Rane 26jul19 [unnecessary open cursor closed]
                    pstmt.close();					
                }
			}//end of tranType (L)
    		else if(this.tranType.equalsIgnoreCase("A")){
    		    mallocQty = mallocQty + meffQty;
    			if (count != 0){
    				sql =" update stock  set alloc_qty = ? ," 
    					+" ltran_date = ? "
    					+" where item_code = '"+this.itemCode+"'" 
    					+" and site_code = '"+this.siteCode +"'"
    					+" and loc_code = '"+this.locationCode +"'"
    					+" and lot_no = '"+this.lotNo+"'"
    					+" and lot_sl = '"+this.lotSl+"'";
                    System.out.println("Query :::- ["+sql+"]");
    				pstmt = conn.prepareStatement(sql);
    				pstmt.setDouble(1,mallocQty);
    				pstmt.setTimestamp(2,this.tranDate);
    				pstmt.executeUpdate();
                    //rs.close(); Pavan Rane 26jul19 [unnecessary open cursor closed]
    				pstmt.close();
    			}
    		}//end of tranType (A)
    		else if(this.tranType.equalsIgnoreCase("D")){
    			mallocQty = mallocQty - meffQty;
    			if (count != 0){
    				sql =" update stock  set alloc_qty = ? ," 
    					+" ltran_date = ? "
    					+" where item_code = '"+this.itemCode+"'" 
    					+" and site_code = '"+this.siteCode +"'"
    					+" and loc_code = '"+this.locationCode +"'"
    					+" and lot_no = '"+this.lotNo+"'"
    					+" and lot_sl = '"+this.lotSl+"'";
    				System.out.println("Query :::- ["+sql+"]");
                    pstmt = conn.prepareStatement(sql);
    				pstmt.setDouble(1,mallocQty);
    				pstmt.setTimestamp(2,this.tranDate);
    				pstmt.executeUpdate();
    				pstmt.close();
    				//rs.close(); Pavan Rane 26jul19 [unnecessary open cursor closed]
    			}
    		}//end of tranType (D)
    		else if(this.tranType.equalsIgnoreCase("ID")){
    			if (count != 0){
                    HashMap invAllocTraceMap = new HashMap();
                    invAllocTraceMap.put("ref_ser",this.tranSer);
                    invAllocTraceMap.put("ref_id",this.tranId);
                    invAllocTraceMap.put("ref_line",this.lineNo);
                    invAllocTraceMap.put("site_code",this.siteCode);
                    invAllocTraceMap.put("item_code",this.itemCode);
                    invAllocTraceMap.put("loc_code",this.locationCode);
                    invAllocTraceMap.put("lot_no",this.lotNo);
                    invAllocTraceMap.put("lot_sl",this.lotSl);
                    invAllocTraceMap.put("alloc_qty",new Double(-1 * this.qtyStduom));
                    invAllocTraceMap.put("chg_user",userId);
                    invAllocTraceMap.put("chg_term",termId);
                    invAllocTraceMap.put("chg_win","  ");
                  //added by nandkumar gadkari on 17/04/19-------start=----------
					String logMsg= this.tranId +" "+this.lineNo + " "+"Deallocation of stock";
					invAllocTraceMap.put("alloc_ref",logMsg);	
					//added by nandkumar gadkari on 17/04/19-------end=----------
                    
                    InvAllocTraceBean invBean = new InvAllocTraceBean(); 
                    errString = invBean.updateInvallocTrace(invAllocTraceMap,conn);
                    if(errString != null && errString.trim().length() > 0){
                        errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
                        return errString;
                    }
                    sql ="update stock set quantity = (case when quantity is null then 0 else quantity end) - ? ," 
    				+" ltran_date 	= ? ,"
    				+" last_iss_date = ?,"
    				+" no_art 			= no_art - ?, "
    				+" chg_user   	= ?, "
    				+" chg_term   	= ?, "
    				+" chg_date   	= ?,"
    				+" gross_weight 	= (case when gross_weight is null then 0 else gross_weight end) - ?,"
    				+" net_weight 	= (case when net_weight is null then 0 else net_weight end) - ?,"
    				+" tare_weight 	= (case when tare_weight is null then 0 else tare_weight end) - ? ,"
    				//added by nandkumar gadkari on 22/04/19--------start------------
    				+" update_ref   	= '"+this.updateRef+"'" 
    				//added by nandkumar gadkari on 22/04/19--------end------------
    				+" where item_code = '"+this.itemCode+"'" 
    					+"and site_code = '"+this.siteCode +"'"
    					+"and loc_code = '"+this.locationCode +"'"
    					+"and lot_no = '"+this.lotNo+"'"
    					+"and lot_sl = '"+this.lotSl+"'";
                    System.out.println("Query :::- ["+sql+"]");
    				pstmt = conn.prepareStatement(sql);
    				pstmt.setDouble(1,this.qtyStduom);
    				pstmt.setTimestamp(2,this.tranDate);
    				pstmt.setTimestamp(3,this.tranDate);
    				pstmt.setDouble(4,this.noArt);
    				pstmt.setString(5,userId);
    				pstmt.setString(6,termId);
    				pstmt.setTimestamp(7,new java.sql.Timestamp(System.currentTimeMillis()));
    				pstmt.setDouble(8,this.grossWeight);
    				pstmt.setDouble(9,this.netWeight);
    				pstmt.setDouble(10,this.tareWeight);
    				pstmt.executeUpdate();
    				pstmt.close();
    				//rs.close(); Pavan Rane 26jul19 [unnecessary open cursor closed]
    				
    					//Added by Manoj dtd 19/08/2014 to update no_art as 1 if no_art<0
    				double stkQty=0,noArticle=0;
    				sql="select quantity,no_art from stock " +
    						" where item_code = '"+this.itemCode+"'" 
    					+"and site_code = '"+this.siteCode +"'"
    					+"and loc_code = '"+this.locationCode +"'"
    					+"and lot_no = '"+this.lotNo+"'"
    					+"and lot_sl = '"+this.lotSl+"'  ";   
    				System.out.println("sql---"+sql);
    				pstmt=conn.prepareStatement(sql);
    				rs=pstmt.executeQuery();
    				if(rs.next())
    				{
    					//countnoArt=rs.getDouble(1);
    					stkQty=rs.getDouble(1);
    					noArticle=rs.getDouble(2);
    				}
    				rs.close();
    				rs=null;
    				pstmt.close();
    				pstmt=null;
    				System.out.println("stkQty::["+stkQty+"]");
    				System.out.println("noArticle::["+noArticle+"]");
    				if(stkQty>0 && noArticle<=0)
    				{
    					sql="update stock set no_art=1 " +
    							" where item_code = '"+this.itemCode+"'" 
    					+"and site_code = '"+this.siteCode +"'"
    					+"and loc_code = '"+this.locationCode +"'"
    					+"and lot_no = '"+this.lotNo+"'"
    					+"and lot_sl = '"+this.lotSl+"' ";   
    					System.out.println("sql---"+sql);
    					pstmt=conn.prepareStatement(sql);
    					pstmt.executeUpdate();
    					pstmt.close();
    					pstmt=null;
    				}
    				//Added by Manoj dtd 29/10/2014 to set no_art=0 if stock =0
    				if(stkQty==0 && noArticle!=0)
    				{
    					sql="update stock set no_art=0 " +
    							" where item_code = '"+this.itemCode+"'" 
    					+"and site_code = '"+this.siteCode +"'"
    					+"and loc_code = '"+this.locationCode +"'"
    					+"and lot_no = '"+this.lotNo+"'"
    					+"and lot_sl = '"+this.lotSl+"' ";   
    					System.out.println("sql---"+sql);
    					pstmt=conn.prepareStatement(sql);
    					pstmt.executeUpdate();
    					pstmt.close();
    					pstmt=null;
    				}
    				
    			}
            }
    		else if(this.tranType.equalsIgnoreCase("R"))
    		{ 
    			System.out.println("before update lot info::");
    			System.out.println("rate::["+this.rate+"]:::gross rate::["+this.grossRate+"]");
    			//added by azhar[Start][04-May-2017]
    			updateStockMap.put("no_art",this.noArt);
    			updateStockMap.put("curr_date",this.currDate);
    			updateStockMap.put("inv_stat",this.invStat);
    			updateStockMap.put("item_ser",this.itemSer);
    			updateStockMap.put("grade",this.grade);
    			updateStockMap.put("lot_no",this.lotNo);
    			updateStockMap.put("lot_sl",this.lotSl);
    			updateStockMap.put("conv__qty_stduom",this.convQtyStduom);
    			updateStockMap.put("qty_stduom",this.qtyStduom);
    			updateStockMap.put("rate",this.rate);
    			updateStockMap.put("acct_code__cr",this.acctCodeCr);
    			updateStockMap.put("cctr_code__cr",this.cctrCodeCr);
    			updateStockMap.put("gross_rate",this.grossRate);
    			//added by azhar[End][04-May-2017]
    			
    			System.out.println("map before update lot info:: " + updateStockMap);
    			//Add by manoj sir
    			if(stkOpt.equalsIgnoreCase("2"))
    			{
    			updateStockMap = updateLotInfo( updateStockMap , conn);  // added by cpatil calling function
    			}
    			//End
    			
    			//if( errString != null && errString.trim().length() > 0)
    			//{
    			//	return errString;
    			//}
    			
    			if (count > 0)
				{
    				sql ="select (case when acct_code__inv is null then '    ' else acct_code__inv end), (case when cctr_code__inv is null then '    ' else cctr_code__inv end), qty_per_art"
    					+" from stock "
    					+" where item_code = '"+this.itemCode+"'" 
    					+" and site_code = '"+this.siteCode +"'"
    					+" and loc_code = '"+this.locationCode +"'"
    					+" and lot_no = '"+this.lotNo+"'"
    					+" and lot_sl = '"+this.lotSl+"'";
                    System.out.println("Query ::- ["+sql+"]");
    				pstmt = conn.prepareStatement(sql);
    				rs = pstmt.executeQuery();
    				if(rs.next())
    				{
    					acctCodeDr = rs.getString(1);
    					cctrCodeDr = rs.getString(2);
    					qtyPerArt = rs.getDouble(3);
    				}
    				//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
    				if(rs!=null)
    				{
    					rs.close();rs=null;
    				}
    				if(pstmt!=null)
    				{
    					pstmt.close();pstmt=null;
    				}
    				//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
    				value = distCommon.getDisparams("999999","STKTRFR_SUBSTORE_CCTR_CHECK",conn);
    				if (value.equalsIgnoreCase("NULLFOUND")) value = "Y";
    				if(value.equalsIgnoreCase("Y"))
    				{
    					if (!(acctCodeDr.equalsIgnoreCase(this.acctCodeInv)) ||(!(cctrCodeDr.equalsIgnoreCase(this.cctrCodeInv))))
    					{
    						invAcct = finCommon.getFinparams("999999", "INVENTORY_ACCT",conn); 
    						if(invAcct.equalsIgnoreCase("Y"))
    						{
                                errString = "VTACTMIS";
    							errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
    							return errString;
    						}
    					}
    				}
    				
    				sql = "select retest_date  from stock"
    					+" where item_code = '"+this.itemCode+"'" 
    					+" and site_code = '"+this.siteCode +"'"
    					+" and loc_code = '"+this.locationCode +"'"
    					+" and lot_no = '"+this.lotNo+"'"
    					+" and lot_sl = '"+this.lotSl+"'";
                    System.out.println("Query ::- ["+sql+"]");
    				pstmt = conn.prepareStatement(sql);
    				rs = pstmt.executeQuery();
    				if(rs.next())
    				{
    					stkRetestDate=rs.getTimestamp(1);
    				}
    				//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
    				if(rs!=null)
    				{
    					rs.close();rs=null;
    				}
    				if(pstmt!=null)
    				{
    					pstmt.close();pstmt=null;
    				}
    				//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
                    if(this.retestDate != null && this.retestDate.compareTo(tempTestDate) == 0){
                        this.retestDate = stkRetestDate;
                    }
                    if(this.retestDate != null && this.retestDate.compareTo(tempTestDate) == 0){
                        this.retestDate = null;
                    }
    				
    				if (this.tranSer.equalsIgnoreCase("QC-ORD"))
    				{
    					sql ="update stock 	set quantity   	 = quantity + ? ,"
    						+"ltran_date 	 = ?,"
    						+"last_rcp_date  = ? ,"
    						+"rate 		     = ?,"
    						+"gross_rate  	 = ?,"
    						+"chg_user    	 = ?,"
    						+"chg_term    	 = ?,"
    						+"chg_date    	 = ?,"
    						+"gross_weight	 = gross_weight + ?,"
    						+"net_weight  	 = net_weight + ?,"
    						+"tare_weight 	 = tare_weight + ?,"
    						+"retest_date 	 = ?,"
    						+"no_art		 = ?,"
    						+"dimension   	 = ?,"
    						+"last_phyc_date = ?,"
    						+"potency_perc 	 = ? "
    						+"where item_code = '"+this.itemCode+"'" 
    						+"and site_code = '"+this.siteCode +"'"
    						+"and loc_code = '"+this.locationCode +"'"
    						+"and lot_no = '"+this.lotNo+"'"
    						+"and lot_sl = '"+this.lotSl+"'";
                        System.out.println("Query :::- ["+sql+"]");
    					pstmt = conn.prepareStatement(sql);
    					
                        pstmt.setDouble(1,this.qtyStduom);
    					pstmt.setTimestamp(2,this.tranDate);
    					pstmt.setTimestamp(3,this.tranDate);
    					pstmt.setDouble(4,this.rate);
    					pstmt.setDouble(5,this.grossRate);
    					pstmt.setString(6,userId);
    					pstmt.setString(7,termId);
    					pstmt.setTimestamp(8,new java.sql.Timestamp(System.currentTimeMillis()));
    					pstmt.setDouble(9,this.grossWeight);
    					pstmt.setDouble(10,this.netWeight);
    					pstmt.setDouble(11,this.tareWeight);
    					pstmt.setTimestamp(12,this.retestDate);
    					pstmt.setDouble(13,this.noArt);
    					pstmt.setString(14,this.dimension);
    					pstmt.setTimestamp(15,this.lastPhycDate);
    					pstmt.setDouble(16,this.potencyPerc);
    					
                        pstmt.executeUpdate();
    					pstmt.close();
    					//rs.close();Changed by wasim on 17-03-2016 to comment as it is already closed
    				}
    				else
    				{
						// 15-may-2019 manoharan not to update rate in case of sales return
						if ((this.tranSer.equalsIgnoreCase("S-RET")) &&   (this.tranType.equalsIgnoreCase("R"))  )
						{
							sql ="update stock 	set quantity   	 = quantity + ? ,"
								+"ltran_date 	 = ?,"
								+"last_rcp_date  = ? ,"
								+"chg_user    	 = ?,"
								+"chg_term    	 = ?,"
								+"chg_date    	 = ?,"
								+"gross_weight	 = gross_weight + ?,"
								+"net_weight  	 = net_weight + ?,"
								+"tare_weight 	 = tare_weight + ?,"
								+"retest_date 	 = ?,"
								+"no_art		 	 = no_art + ?,"
								+"dimension   	 = ?,"
								+"last_phyc_date = ?,"
								+"supp_code__mfg 	 = ?," 
								+"batch_no = ? ,"
								//Added by saurabh as hold_qty is not get updated if the value is null[25/10/16|Start]
								+"hold_qty=(case when hold_qty is null then 0 else hold_qty end) + ? "
								//Added by saurabh as hold_qty is not get updated if the value is null[25/10/16|End]
								+"where item_code = '"+this.itemCode+"'" 
								+"and site_code = '"+this.siteCode+"'"
								+"and loc_code = '"+this.locationCode+"'"
								+"and lot_no = '"+this.lotNo+"'"
								+"and lot_sl = '"+this.lotSl+"'";
							System.out.println("Query :::- ["+sql+"]");
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,this.qtyStduom);
							pstmt.setTimestamp(2,this.tranDate);
							pstmt.setTimestamp(3,this.tranDate);
							pstmt.setString(4,userId);
							pstmt.setString(5,termId);
							pstmt.setTimestamp(6,new java.sql.Timestamp(System.currentTimeMillis()));
							pstmt.setDouble(7,this.grossWeight);
							pstmt.setDouble(8,this.netWeight);
							pstmt.setDouble(9,this.tareWeight);
							pstmt.setTimestamp(10,this.retestDate);
							pstmt.setDouble(11,this.noArt);
							pstmt.setString(12,this.dimension);
							pstmt.setTimestamp(13,this.lastPhycDate);
							pstmt.setString(14,this.suppCodeMfg);
							pstmt.setString(15,this.batchNo);
							pstmt.setDouble(16,this.holdQty);//added by priyanka    
							pstmt.executeUpdate();
							pstmt.close();
						}
						else
						{

							sql ="update stock 	set quantity   	 = quantity + ? ,"
								+"ltran_date 	 = ?,"
								+"last_rcp_date  = ? ,"
								+"rate 			 = ?,"
								+"gross_rate  	 = ?,"
								+"chg_user    	 = ?,"
								+"chg_term    	 = ?,"
								+"chg_date    	 = ?,"
								+"gross_weight	 = gross_weight + ?,"
								+"net_weight  	 = net_weight + ?,"
								+"tare_weight 	 = tare_weight + ?,"
								+"retest_date 	 = ?,"
								+"no_art		 	 = no_art + ?,"
								+"dimension   	 = ?,"
								+"last_phyc_date = ?,"
								+"supp_code__mfg 	 = ?," 
								+"batch_no = ? ,"
								//Added by saurabh as hold_qty is not get updated if the value is null[25/10/16|Start]
								+"hold_qty=(case when hold_qty is null then 0 else hold_qty end) + ? "
								//Added by saurabh as hold_qty is not get updated if the value is null[25/10/16|End]
								+"where item_code = '"+this.itemCode+"'" 
								+"and site_code = '"+this.siteCode+"'"
								+"and loc_code = '"+this.locationCode+"'"
								+"and lot_no = '"+this.lotNo+"'"
								+"and lot_sl = '"+this.lotSl+"'";
							System.out.println("Query :::- ["+sql+"]");
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,this.qtyStduom);
							pstmt.setTimestamp(2,this.tranDate);
							pstmt.setTimestamp(3,this.tranDate);
							pstmt.setDouble(4,this.rate);
							pstmt.setDouble(5,this.grossRate);
							pstmt.setString(6,userId);
							pstmt.setString(7,termId);
							pstmt.setTimestamp(8,new java.sql.Timestamp(System.currentTimeMillis()));
							pstmt.setDouble(9,this.grossWeight);
							pstmt.setDouble(10,this.netWeight);
							pstmt.setDouble(11,this.tareWeight);
							pstmt.setTimestamp(12,this.retestDate);
							pstmt.setDouble(13,this.noArt);
							pstmt.setString(14,this.dimension);
							pstmt.setTimestamp(15,this.lastPhycDate);
							pstmt.setString(16,this.suppCodeMfg);
							pstmt.setString(17,this.batchNo);
							pstmt.setDouble(18,this.holdQty);//added by priyanka    
							pstmt.executeUpdate();
							pstmt.close();
						}
						// end 15-may-2019 manoharan not to update rate in case of sales return
    					//rs.close();Changed by wasim on 17-03-2016 to comment as it is already closed
    				}
    			}//end of count
    			else
    			{
    				sql = "select inv_stat from location where loc_code = '"+this.locationCode+"'";                    
    				pstmt = conn.prepareStatement(sql);
    				rs = pstmt.executeQuery();
                    System.out.println("Query :::- ["+sql+"]");
                    if(rs.next())
    				{
                        this.invStat =rs.getString(1);
    				}
    				pstmt.close();
    				rs.close();
    				if(this.tranSer.equalsIgnoreCase("W-IRTN") || this.tranSer.equalsIgnoreCase("C-IRTN") || this.tranSer.equalsIgnoreCase("XFRX"))
    				{
    					sql = " select Min(crea_date)  from stock "
    						+"where item_code = '"+this.itemCode+"'" 
    						+"and site_code = '"+this.siteCode +"'"
    						+"and lot_no = '"+this.lotNo+"'"
    						//+"and lot_sl = '"+this.lotSl+"'" //Commented by gulzar as discussed with Manoharan sir on 05/01/12
    						+"and crea_date is not null ";                            
    						pstmt = conn.prepareStatement(sql);
    						rs = pstmt.executeQuery();
                            System.out.println("Query :::- ["+sql+"]");
                            if(rs.next())
    						{
    							creaDt = rs.getTimestamp(1) == null ? this.currDate : rs.getTimestamp(1); //handle null date ,change done by kunal on 07/nov/13 
    						}
    						pstmt.close();
    						rs.close();
    						if(creaDt != null && creaDt.compareTo(tempTestDate) != 0){
    							mtoday = creaDt;
    						}
    				}
    				sql="select (case when iss_criteria is null then 'I' else iss_criteria end) "
    					+" from item where item_code = '"+this.itemCode+"'" ;
                    System.out.println("Query :::- ["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						issCriteria = rs.getString(1);
					}
					pstmt.close();
					rs.close();
    					
					if (issCriteria != null && issCriteria.equalsIgnoreCase("W")){
						qtyPerArt		= this.qtyStduom;
						tarewtPerArt	= this.tareWeight;
						grosswtPerArt	= this.grossWeight;
					}else{
                        if(this.noArt == 0){
							qtyPerArt		= this.qtyStduom;
							tarewtPerArt	= this.tareWeight;
							grosswtPerArt	= this.grossWeight;
						}
						else{
							qtyPerArt		= this.qtyStduom / this.noArt;
							tarewtPerArt	= this.tareWeight / this.noArt;
							grosswtPerArt	= this.grossWeight / this.noArt;		
						}
					}
					// Changed By Nasruddin 21-11-16 Start bug Fix
					qtyPerArtStr = utilMethods.getReqDecString(qtyPerArt,3);
					qtyPerArt = Double.parseDouble(qtyPerArtStr);
					
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of tare_weight_per_art][Start]
					//tarewtPerArtStr = utilMethods.getReqDecString(qtyPerArt,3);
					tarewtPerArtStr = utilMethods.getReqDecString(tarewtPerArt,3);
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of tare_weight_per_art][end]
					tarewtPerArt = Double.parseDouble(tarewtPerArtStr);
					
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of gross_weight_per_art][Start]
					//grosswtPerArtStr = utilMethods.getReqDecString(qtyPerArt,3);
					grosswtPerArtStr = utilMethods.getReqDecString(grosswtPerArt,3);
					//Modified by Anjali R. on[16/05/2019][Wrong variable passed to getreqdecstring method for calculation of gross_weight_per_art][end]
					grosswtPerArt = Double.parseDouble(grosswtPerArtStr);
		         	// Changed By Nasruddin 21-11-16 END
										
					sql="insert into stock "
						+" (item_code , site_code, loc_code," 
						+" lot_no, lot_sl, unit,"
						+" quantity,crea_date, chg_user,"
						+" chg_date,chg_term, exp_date,"
						+" alloc_qty,item_ser, mfg_date,"
						+" site_code__mfg,potency_perc,pack_code,"
						+" inv_stat,ltran_date,last_rcp_date,"
						+" rate,gross_weight,tare_weight,"
						+" net_weight,pack_instr,dimension,"
						+" retest_date,rate__oh,acct_code__inv,"
						+" cctr_code__inv,acct_code__oh,cctr_code__oh,"
						+" supp_code__mfg,grade, gross_rate,"
						+" conv__qty_stduom,unit__alt,batch_no,"
						+" no_art,last_phyc_date,qty_per_art,"
						+" gross_wt_per_art,tare_wt_per_art,actual_rate,"
						+" remarks,pack_ref,consider_allocate,	partial_used, batch_size" 
						+" ,lot_sl__org " 
						+" ,hold_qty ) values "    ////added by saurabh to set hold qty same as quantity[25/10/16]
						+"  (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
					    +" ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
					    +" ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					//System.out.println("SQL :"+sql);    
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,this.itemCode);
					pstmt.setString(2,this.siteCode);
					pstmt.setString(3,this.locationCode);

					pstmt.setString(4,this.lotNo);
					pstmt.setString(5,this.lotSl);
					pstmt.setString(6,mbaseUnit);

					pstmt.setDouble(7,this.qtyStduom);
					pstmt.setTimestamp(8,creaDt);
					pstmt.setString(9,userId);

					pstmt.setTimestamp(10,new Timestamp(System.currentTimeMillis()));
					pstmt.setString(11,termId);
					pstmt.setTimestamp(12,this.expDate);

					pstmt.setString(13,"0");
					pstmt.setString(14,this.itemSer);
					pstmt.setTimestamp(15,this.mfgDate);

					pstmt.setString(16,this.siteCodeMfg);
					pstmt.setDouble(17,this.potencyPerc);
					pstmt.setString(18,this.packCode);
    
					pstmt.setString(19,this.invStat);
					pstmt.setTimestamp(20,this.currDate);
					pstmt.setTimestamp(21,this.tranDate);

					pstmt.setDouble(22,this.rate);
					pstmt.setDouble(23,this.grossWeight);
					pstmt.setDouble(24,this.tareWeight);

					pstmt.setDouble(25,this.netWeight);
					pstmt.setString(26,this.packInstr);
					pstmt.setString(27,this.dimension);

					pstmt.setTimestamp(28,this.retestDate);
					pstmt.setDouble(29,this.rateOh);
					pstmt.setString(30,this.acctCodeInv);

					pstmt.setString(31,this.cctrCodeInv);
					pstmt.setString(32,this.acctCodeOh);
					pstmt.setString(33,this.cctrCodeOh);

					pstmt.setString(34,this.suppCodeMfg);
					pstmt.setString(35,this.grade);
					pstmt.setDouble(36,this.grossRate);

					pstmt.setDouble(37,this.convQtyStduom);
					pstmt.setString(38,this.unitAlt);
					pstmt.setString(39,this.batchNo);

					pstmt.setDouble(40,this.noArt);
					pstmt.setTimestamp(41,this.lastPhycDate);
					
					
					//pstmt.setDouble(42,qtyPerArt);
					pstmt.setString(42,qtyPerArtStr);

					//pstmt.setDouble(43,grosswtPerArt);
					pstmt.setString(43,grosswtPerArtStr);
					//pstmt.setDouble(44,tarewtPerArt);
					pstmt.setString(44,tarewtPerArtStr);
					pstmt.setDouble(45,this.actualRate);

					pstmt.setString(46,this.remarks);
                    pstmt.setString(47,this.packRef);
                    pstmt.setString(48,this.considerAllocate);
                    pstmt.setString(49,this.partialUsed);
					pstmt.setDouble(50,this.batchSize);
					pstmt.setString(51,this.lotSlOrg);        // cpatil
					
					pstmt.setDouble(52,this.holdQty); //added by saurabh to set hold qty same as quantity[25/10/16]
					
                    System.out.println("ITEM_CODE :"+this.itemCode);
                    System.out.println("SITE_CODE :"+this.siteCode);
                    System.out.println("LOC_CODE :"+this.locationCode);
                    System.out.println("LOT_NO :"+this.lotNo);
                    System.out.println("LOT_SL :"+this.lotSl);
                    System.out.println("QUANTITY :"+this.qtyStduom);
                    System.out.println("UNIT :"+mbaseUnit);
                    System.out.println("CREA_DATE :"+creaDt);
                    System.out.println("HOLD_QTY :"+this.holdQty);
					pstmt.executeUpdate();
					System.out.println("@@@@@@@ executed sucessfully............");
					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
					if(pstmt!=null)
					{
						pstmt.close();pstmt=null;
					}
					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
    			}
    		}
    		if(this.tranType.equalsIgnoreCase("I") || this.tranType.equalsIgnoreCase("ID"))
    		{
    			sql ="select (case when grade is null then '   ' else grade end ),rate "
    				+" from stock"
    				+" where item_code = '"+this.itemCode+"'" 
    				+" and site_code = '"+this.siteCode +"'"
    				+" and loc_code = '"+this.locationCode +"'"
    				+" and lot_no = '"+this.lotNo+"'"
    				+" and lot_sl = '"+this.lotSl+"'";
                System.out.println("Query :::- ["+sql+"]");
    			pstmt = conn.prepareStatement(sql);
    			rs = pstmt.executeQuery();
    			if(rs.next())
    			{
                    this.grade =rs.getString(1);
                    this.rate = rs.getDouble(2);
    			}
    			pstmt.close();
    			rs.close(); 
    		}
    		if(!stockValuation.equalsIgnoreCase("N")){
    			if(this.tranType.equalsIgnoreCase("R")){
    				if (stkOpt.equalsIgnoreCase("1") || stockValuation.equalsIgnoreCase("M")) 
    				{
    					sql =" update stock set  rate = (case when "+this.rate+" is null then 0 else "+this.rate+" end),"
    						+" gross_rate 	= (case when "+this.grossRate+" is null then 0 else "+this.grossRate+" end)"
    						+" where item_code ='"+this.itemCode+"'" 
    						+" and   site_code ='"+this.siteCode+"'" 
    						+" and 	grade 	 = '"+this.grade+"'";
                            System.out.println("Query :::- ["+sql+"]");
    						pstmt = conn.prepareStatement(sql);
    						pstmt.executeUpdate();
    						//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
    						if(pstmt!=null)
    						{
    							pstmt.close();pstmt=null;
    						}
    						//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
    				}
    				else if (stockValuation.equalsIgnoreCase("C"))
    				{
    					sql = " update stock set rate = (case when "+this.rate+" is null then 0 else "+this.rate+" end) ,"
    					+" gross_rate 	=  (case when "+this.grossRate+" is null then 0 else "+this.grossRate+" end) "
    					+" where item_code ='"+this.itemCode+"'" 
    					+" and   site_code ='"+this.siteCode+"'" 
    					+" and 	grade 	 = '"+this.grade+"'"
    					+" And 	cctr_code__inv = '"+this.cctrCodeInv+"'";
                        System.out.println("Query :::- ["+sql+"]");
    					pstmt = conn.prepareStatement(sql);
    					pstmt.executeUpdate();
    					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
    					if(pstmt!=null)
    					{
    						pstmt.close();pstmt=null;
    					}
    					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
    				}else if (stkOpt.equalsIgnoreCase("2")) 
    				{
    					sql = " update stock set rate = (case when "+this.rate+" is null then 0 else "+this.rate+" end) ,"
    	    					+" gross_rate 	=  (case when "+this.grossRate+" is null then 0 else "+this.grossRate+" end) "
    	    					+" where item_code ='"+this.itemCode+"'" 
    	    					+" and   site_code ='"+this.siteCode+"'" 
    	    					+" and 	grade 	 = '"+this.grade+"'"
    	    					+" And 	lot_no = '"+this.lotNo+"'";
    	                        System.out.println("Query :::- ["+sql+"]");
    	    					pstmt = conn.prepareStatement(sql);
    	    					pstmt.executeUpdate();
    	    					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
    	    					if(pstmt!=null)
    	    					{
    	    						pstmt.close();pstmt=null;
    	    					}
    				}
                }
    			sql = "select count(*) from stockvalue "
				+" where item_code ='"+this.itemCode+"'" 
				+" and   site_code ='"+this.siteCode+"'" 
				+" and VALUE_KEY = '"+this.grade+"'";
                System.out.println("Query ::- ["+sql+"]");
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count =rs.getInt(1);
				}
				pstmt.close();
				rs.close();
				if(count == 1){
					if (stkOpt.equalsIgnoreCase("1") || stockValuation.equalsIgnoreCase("M")){
						if (this.tranType.equalsIgnoreCase("R")){
							sql ="update stockvalue set quantity = (case when quantity is null then 0 else quantity end) + (case when "+effQty+" is null then 0 else "+effQty+" end), "
                            +"value = ((case when "+oldQty+" is null then 0 else "+oldQty+" end) * (case when "+this.rate+" is null then 0 else "+this.rate+" end)) + ((case when "+effQty+" is null then 0 else "+effQty+" end) * (case when "+this.rate+" is null then 0 else "+this.rate+" end))"
							+" where item_code ='"+this.itemCode+"'" 
							+" and   site_code ='"+this.siteCode+"'" 
							+" and 	VALUE_KEY 	 = '"+this.grade+"'";
                            System.out.println("Query :::- ["+sql+"]");
							pstmt = conn.prepareStatement(sql);
							pstmt.executeUpdate();
							//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
							if(pstmt!=null)
							{
								pstmt.close();pstmt=null;
							}
							//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
						}else{
							sql =" update stockvalue set quantity = (case when quantity is null then 0 else quantity end) + (case when "+effQty+" is null then 0 else "+effQty+" end),	"
                            +"value = (case when value is null then 0 else value end) + ((case when "+effQty+" is null then 0 else "+effQty+" end) * (case when "+this.rate+" is null then 0 else "+this.rate+" end)) "
							+" where item_code ='"+this.itemCode+"'" 
							+" and   site_code ='"+this.siteCode+"'" 
							+" and 	VALUE_KEY 	 = '"+this.grade+"'";
                            System.out.println("Query :::- ["+sql+"]");
							pstmt = conn.prepareStatement(sql);
							pstmt.executeUpdate();
							//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
							if(pstmt!=null)
							{
								pstmt.close();pstmt=null;
							}
							//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
						}
					}else{
						sql ="update stockvalue set quantity = (case when quantity is null then 0 else quantity end) + (case when "+effQty+" is null then 0 else "+effQty+" end),"
                        +"value = (case when value is null then 0 else value end) + ((case when "+effQty+" is null then 0 else "+effQty+" end) * (case when "+this.rate+" is null then 0 else "+this.rate+" end)) " 
						+" where item_code ='"+this.itemCode+"'" 
						+" and   site_code ='"+this.siteCode+"'" 
						+" and 	VALUE_KEY 	 = '"+this.grade+"'";
                        System.out.println("Query :::- ["+sql+"]");
						pstmt = conn.prepareStatement(sql);
						pstmt.executeUpdate();
						//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
						if(pstmt!=null)
						{
							pstmt.close();pstmt=null;
						}
						//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
					}
					sql =" select (case when quantity is null then 0 else quantity end), (case when value is null then 0 else value end)"
					+" from stockvalue"
					+" where item_code ='"+this.itemCode+"'" 
					+" and   site_code ='"+this.siteCode+"'" 
					+" and 	VALUE_KEY 	 = '"+this.grade+"'";
                    System.out.println("Query :::- ["+sql+"]");
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					double value1=0;
					double quantity1=0;
					if(rs.next())
					{
						quantity1 = rs.getDouble(1);
						value1 = rs.getDouble(2);
					}
					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
					if(rs!=null)
					{
						rs.close();rs=null;
					}
					if(pstmt!=null)
					{
						pstmt.close();pstmt=null;
					}
					//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
                    System.out.println("Stock Value Quantity : "+quantity1+" Stock Value Value :"+value1);
					if (quantity1 == 0 && value1 != 0 ){
					    parmValueStr = distCommon.getDisparams("999999","ON_STKVALUE_ADJENTRY",conn);
						if (!parmValueStr.equalsIgnoreCase("NULLFOUND")){
							parmValue = Double.parseDouble(parmValueStr);
							System.out.println("Param Value "+parmValue);
                            if (Math.abs(value1) > parmValue){
							    errString = "VTSTKVERR";	
							    errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
							    return errString;
							}
                        }else{
							if(Math.abs(value1) > 1){
								errString = "VTSTKVERR"	;
								errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
								return errString;
							}
                        }
                        errString = adjustStkValue(conn);
                        if(errString.trim().length() > 0){
                            return errString;
                        }
                        // Changed by Pragyan Date 10/04/14 Bug fix Integer fld value set with character values
                        
    					/*sql =" update stockvalue " 
                        +"set value = (case when value is null then 0 else value end) + (case when '"+this.netAmt+"' is null then 0 else '"+this.netAmt+"' end) "
    					+" where item_code ='"+this.itemCode+"'" 
    					+" and   site_code ='"+this.siteCode+"'" 
    					+" and 	VALUE_KEY 	 = '"+this.grade+"'";*/
                        
                        sql =" update stockvalue " 
                                +"set value = (case when value is null then 0 else value end) + (case when "
                                + "? is null then 0 else ? end) "
            					+" where item_code =?" 
            					+" and   site_code =?" 
            					+" and 	VALUE_KEY 	 = ?";
                        
                        System.out.println("Query :::- ["+sql+"]");
                        
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setDouble(1, this.netAmt);
                        pstmt.setDouble(2, this.netAmt);
                        pstmt.setString(3, this.itemCode);
                        pstmt.setString(4, this.siteCode);
                        pstmt.setString(5, this.grade);
                        int K = pstmt.executeUpdate();
                       //Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
            			if(pstmt!=null)
            			{
            				pstmt.close();pstmt=null;
            			}
            			//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
                        
                        if(K > 0)
                        {
                        	 System.out.println("Data Updated Successfully at StockValue");
                        }
                        
                        
                    }
                }else if(count == 0){
    				if (this.tranType.equalsIgnoreCase("R")){
					   sql =" insert into stockvalue (site_code,item_code, value_key, quantity,	value )"
					   	+" values (?,?,?,?,?) ";
                       System.out.println("Query :::- ["+sql+"]");
					   pstmt = conn.prepareStatement(sql);
					   pstmt.setString(1,this.siteCode);
					   pstmt.setString(2,this.itemCode);
                       pstmt.setString(3,this.grade);
					   pstmt.setDouble(4,effQty);
					   pstmt.setDouble(5,effQty*this.rate);
					   pstmt.executeUpdate();
					    //Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [START]
						if(pstmt!=null)
						{
							pstmt.close();pstmt=null;
						}
						//Changed by wasim on 16-03-2015 to close Prepared Statement/Result Set [END]
					}else{
                        System.out.println("StockValue update failed, there is no record in stockvalue for " + this.siteCode);
                        errString = "VTSTKVERR";    
                        errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
                        return errString;// STOCK VALUE ERROR
                    }
    			}
			}//end of stockValuation="N"
		}
		catch (SQLException sqx) // 04/02/12 manoharan this catch block added for trapping record lock
		{
			//Change by Rohan on 18-02-13 for throwing exception
			SQLException ex ;
			
			while (sqx != null)
			{
				if((CommonConstants.DB_NAME).equalsIgnoreCase("oracle") && (sqx).toString().indexOf("ORA-00054") > -1)
				{
					System.out.println("The SQLException occurs in UpdatStock [Stockupdate] getSQLState [" + sqlState + "] getErrorCode [" + errorCode + "]  Exception[Recod is locked try after some time]");
					errString = itmDBAccessEJB.getErrorString("","VTLCKERR","");
				}
				else
				{
					System.out.println("The SQLException occurs in UpdatStock [Stockupdate] getSQLState [" + sqlState + "] getErrorCode [" + errorCode + "]  Exception["+sqx + "]");
				}
				//Change by Rohan on 18-02-13 for throwing exception
				ex = sqx;
				
				sqx.printStackTrace();
				sqx = sqx.getNextException();
				//Change by Rohan on 18-02-13 for throwing exception.start
				if(sqx == null)
				{
					sqx = ex;
					break;
				}
				//Change by Rohan on 18-02-13 for throwing exception.end
			}
			
            throw new ITMException(sqx);
		}
		catch(Exception se12){
		    System.out.println("Exception in UpdatStock [Stockupdate]"+se12);
            se12.printStackTrace();
            throw new ITMException(se12);
		}finally{
		    try{
                if(rs != null){rs.close();rs = null;}
                if(pstmt != null){pstmt.close();pstmt = null;}
            }catch(Exception t){}
        }
		return errString;
	}//END OF UPDATE METHOD
	
	/**
	 * @param updateStockMap
	 * @throws ITMException
	 * @throws Exception
	 */
	private void puplateCommonParameters(HashMap updateStockMap) throws ITMException, Exception
	{		
        System.out.println("Comming Map :::- ["+updateStockMap+"]");
        try{
            
        
    		if(updateStockMap.get("item_code")!= null)
    		{
    			this.itemCode = updateStockMap.get("item_code").toString();
    		}
    		if(updateStockMap.get("site_code")!= null)
    		{
    			this.siteCode = updateStockMap.get("site_code").toString();
    		}
    		if(updateStockMap.get("loc_code")!= null)
    		{
    			this.locationCode = updateStockMap.get("loc_code").toString();
    		}
    		if(updateStockMap.get("lot_no")!= null)
    		{
    			this.lotNo = updateStockMap.get("lot_no").toString();
    		}
    		if(updateStockMap.get("lot_sl")!= null)
    		{
    			this.lotSl = updateStockMap.get("lot_sl").toString();
    		}
    		if(updateStockMap.get("quantity")!= null)
    		{
    			this.quantity = Double.parseDouble(updateStockMap.get("quantity").toString());
    		}
    		if(updateStockMap.get("unit") != null)
    		{
    			this.unit = updateStockMap.get("unit").toString();
    		}
    		if(updateStockMap.get("tran_type")!= null)
    		{
    			this.tranType = updateStockMap.get("tran_type").toString();
    		}
    		if(updateStockMap.get("check_expiry")!= null)
    		{
    			this.checkExpiry = updateStockMap.get("check_expiry").toString();
    		}
    		if(updateStockMap.get("tran_date")!= null)
    		{
    			this.tranDate = (Timestamp)updateStockMap.get("tran_date");			
    		}
    		if(updateStockMap.get("tran_ser") != null)
    		{
    			this.tranSer = updateStockMap.get("tran_ser").toString();
    		}
    		if(updateStockMap.get("tran_id")!=null)
    		{
    			this.tranId = updateStockMap.get("tran_id").toString();
    		}
    		if(updateStockMap.get("acct_code__cr")!=null)
    		{
    			this.acctCodeCr = updateStockMap.get("acct_code__cr").toString();
    		}
    		if(updateStockMap.get("acct_code__dr") != null)
    		{
    			this.acctCodeDr = updateStockMap.get("acct_code__dr").toString();
    		}
    		if(updateStockMap.get("cctr_code__cr") != null)
    		{
    			this.cctrCodeCr = 	updateStockMap.get("cctr_code__cr").toString();
    		}
    		if(updateStockMap.get("cctr_code__dr") != null)
    		{
    			this.cctrCodeDr = updateStockMap.get("cctr_code__dr").toString();
    		}
    		if(updateStockMap.get("line_no")!=null)
    		{
    			this.lineNo = updateStockMap.get("line_no").toString();
    		}
    		if(updateStockMap.get("sorder_no")!=null)
    		{
    			this.sorderNo = updateStockMap.get("sorder_no").toString();
    		}
    		if(updateStockMap.get("qty_stduom")!=null)
    		{
    			this.qtyStduom = Double.parseDouble(updateStockMap.get("qty_stduom").toString());
    		}
    		if(updateStockMap.get("rate")!=null)
    		{
    			this.rate = Double.parseDouble(updateStockMap.get("rate").toString());
    		}
    		if(updateStockMap.get("site_code__mfg")!= null)
    		{
    			this.siteCodeMfg = 	updateStockMap.get("site_code__mfg").toString();
    		}
    		if(updateStockMap.get("mfg_date")!=null)
    		{
    			this.mfgDate = 	(Timestamp)updateStockMap.get("mfg_date");
    		}
    		if(updateStockMap.get("potency_perc")!=null)
    		{
    			this.potencyPerc = 	Double.parseDouble(updateStockMap.get("potency_perc").toString());
    		}
    		if(updateStockMap.get("exp_date")!=null)
    		{
    			this.expDate = 	(Timestamp)updateStockMap.get("exp_date");
    		}
    		if(updateStockMap.get("pack_code")!=null)
    		{
    			this.packCode = 	updateStockMap.get("pack_code").toString();
    		}
    		if(updateStockMap.get("item_ser")!=null)
    		{
    			this.itemSer = 	updateStockMap.get("item_ser").toString();
    		}
    		if(updateStockMap.get("reas_code")!=null)
    		{
    			this.reasCode = updateStockMap.get("reas_code").toString();
    		}
    		if(updateStockMap.get("inv_stat")!=null)
    		{
    			this.invStat = 	updateStockMap.get("inv_stat").toString();
    		}
    		if(updateStockMap.get("net_amt")!=null)
    		{
    			this.netAmt = 	Double.parseDouble(updateStockMap.get("net_amt").toString());
    		}
    		if(updateStockMap.get("sundry_type")!=null)
    		{
    			this.sundryType = 	updateStockMap.get("sundry_type").toString();
    		}
    		if(updateStockMap.get("sundry_code")!=null)
    		{
    			this.sundryCode = 	updateStockMap.get("sundry_code").toString();
    		}
    		if(updateStockMap.get("curr_date")!= null)
    		{
    			this.currDate = (Timestamp)	updateStockMap.get("curr_date");
    		}
    		if(updateStockMap.get("gross_weight")!=null)
    		{
    			this.grossWeight = Double.parseDouble(updateStockMap.get("gross_weight").toString());
    		}
    		if(updateStockMap.get("tare_weight")!=null)
    		{
    			this.tareWeight = Double.parseDouble(updateStockMap.get("tare_weight").toString());
    		}
    			if(updateStockMap.get("net_weight")!=null)
    		{
    				this.netWeight = Double.parseDouble(updateStockMap.get("net_weight").toString());
    		}
    		if(updateStockMap.get("pack_instr")!=null)
    		{
    			this.packInstr = updateStockMap.get("pack_instr").toString();
    		}
    		if(updateStockMap.get("dimension")!=null)
    		{
    			this.dimension = 	updateStockMap.get("dimension").toString();
    		}
    		if(updateStockMap.get("acct_code_inv")!=null)
    		{
    			this.acctCodeInv = 	updateStockMap.get("acct_code_inv").toString();
    		}
    		if(updateStockMap.get("cctr_code_inv")!=null)
    		{
    			this.cctrCodeInv = 	updateStockMap.get("cctr_code_inv").toString();
    		}
    		if(updateStockMap.get("rate_oh")!= null)
    		{
    			this.rateOh = 	Double.parseDouble(updateStockMap.get("rate_oh").toString());
    		}
    		if(updateStockMap.get("acct_code_oh")!= null)
    		{
    			this.acctCodeOh = 	updateStockMap.get("acct_code_oh").toString();
    		}
    		if(updateStockMap.get("cctr_code_oh")!=null)
    		{
    			this.cctrCodeOh = 	updateStockMap.get("cctr_code_oh").toString();
    		}
    		if(updateStockMap.get("retest_date")!=null)
    		{
    			this.retestDate = (Timestamp)updateStockMap.get("retest_date");
    		}
    		if(updateStockMap.get("supp_code__mfg")!=null)
    		{
    			this.suppCodeMfg = 	updateStockMap.get("supp_code__mfg").toString();
    		}
    		if(updateStockMap.get("grade")!=null)
    		{
    			this.grade = updateStockMap.get("grade").toString();
    		}
    		if(updateStockMap.get("gross_rate")!=null)
    		{
    			this.grossRate = Double.parseDouble(updateStockMap.get("gross_rate").toString());
    		}
    		if(updateStockMap.get("conv__qty_stduom")!=null)
    		{
    			this.convQtyStduom = Double.parseDouble(updateStockMap.get("conv__qty_stduom").toString());
    		}
    		if(updateStockMap.get("batch_no")!= null)
    		{
    			this.batchNo = 	updateStockMap.get("batch_no").toString();
    		}
    		if(updateStockMap.get("unit__alt")!= null)
    		{
    			this.unitAlt = 	updateStockMap.get("unit__alt").toString();
    		}
    		if(updateStockMap.get("no_art")!=null)
    		{
    			this.noArt = Double.parseDouble(updateStockMap.get("no_art").toString());
    		}
    		if(updateStockMap.get("last_phyc_date")!=null)
    		{
    			this.lastPhycDate = (Timestamp)updateStockMap.get("last_phyc_date");
    		}
    		if(updateStockMap.get("remarks")!=null)
    		{
    			this.remarks = 	updateStockMap.get("remarks").toString();
    		}
    		if(updateStockMap.get("ref_id__for")!=null)
    		{
    			this.refIdFor = updateStockMap.get("ref_id__for").toString();
    		}
    		if(updateStockMap.get("ref_ser__for")!=null)
    		{
    			this.refSerFor = updateStockMap.get("ref_ser__for").toString();
    		}
    		if(updateStockMap.get("actual_rate")!=null)
    		{
    			this.actualRate = Double.parseDouble(updateStockMap.get("actual_rate").toString());
    		}
            if(updateStockMap.get("pack_ref")!= null)
            {
                this.packRef = updateStockMap.get("pack_ref").toString();
            }
            if(updateStockMap.get("consider_allocate")!= null)
            {
                this.considerAllocate = updateStockMap.get("consider_allocate").toString();
            }
            if(updateStockMap.get("partial_used")!= null)
            {
                this.partialUsed = updateStockMap.get("partial_used").toString();
            }
    		if(updateStockMap.get("batch_size")!=null)
    		{
    			this.batchSize = 	Double.parseDouble(updateStockMap.get("batch_size").toString());
    		}
    		if(updateStockMap.get("lot_sl__org")!=null)
    		{
    			this.lotSlOrg = 	updateStockMap.get("lot_sl__org").toString();
    		}
    		if(updateStockMap.get("shelf_life_type")!=null)
    		{
    			this.shelfLifeType = 	updateStockMap.get("shelf_life_type").toString();
    		}
			else
			{
				this.shelfLifeType = "E";
			}
    		//added by saurabh to set hold qty[25/10/16|Start]
    		if(updateStockMap.get("hold_qty")!=null)
    		{
    			this.holdQty = Double.parseDouble(updateStockMap.get("hold_qty").toString());
    		}
			//added by saurabh to set hold qty[25/10/16|End]
    		//added by nandkumar gadkari on 22/04/19--------start------------
    		if(updateStockMap.get("update_ref")!= null)
            {
                this.updateRef = updateStockMap.get("update_ref").toString();
            }
    		//added by nandkumar gadkari on 22/04/19--------start------------
        }catch(Exception e){
            System.out.println("Exception "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }
	}
    //END OF PUPLATECOMMONPARAMETERS 
	
	//********************************************Generate the TranId
	/**
	 * @param windowName
	 * @param xmlValues
	 * @param conn
	 * @return
	 * @throws ITMException
	 * @throws Exception
	 */
	private String generateTranId(String windowName,String xmlValues,Connection conn) throws ITMException, Exception
	{
        System.out.println("Generating tran ID.....");
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String tranId = "";
		String newKeystring = "";
		String srType = "RS";
		 try
	     {
	    	sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)= '"+windowName+"'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rs.next())
			{
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);				
			}
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer1 :"+tranSer1);
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "SYSTEM", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
			ex.printStackTrace();		
            throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
            throw new ITMException(e);
		}finally{
            try{
                if(rs != null){rs.close();rs = null;}
                if(stmt != null){stmt.close();stmt = null;}
            }catch(Exception t){throw new ITMException(t);}
        }
		return tranId;
	}//generateTranTd()
    
    /**
     * @param conn
     * @return
     * @throws ITMException
     * @throws Exception
     */
    private String adjustStkValue(Connection conn) throws ITMException, Exception
    {
        String prdCode = "",sql = "";
        String invLink = "",errString = "",acctAdj = "",cctrAdj = "";
        double effQty = 0,afterQty = 0,beforeQty = 0;
        String updStkCctrCodeInv = "";
        String remarks = "";
        String refSer = "",adjWin = "";
        String acctCodeCr = "",cctrCodeCr = "",acctCodeDr = "",cctrCodeDr = "";
        FinCommon finCommon = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        String keyString = "",keyCol = "",tranSer = "";
        String invTraceNo = "",tranIDAdj = "",finEntity = "",currCode = "";
        double exchRate = 0;
        HashMap glTrace = null;
        try{
            finCommon = new FinCommon();
            updStkCctrCodeInv = this.cctrCodeInv;
            stmt = conn.createStatement();
            /*if gs_run_mode <> 'B' then
                s_updatestock.currdate = ldt_today
                s_updatestock.trandate = ldt_today
            else
                if isnull(s_updatestock.currdate) then
                    s_updatestock.currdate = ldt_today
                    s_updatestock.trandate = ldt_today
                else
                    s_updatestock.trandate = s_updatestock.currdate
                    end if
            end if 
            ls_prdcode = string(s_updatestock.trandate,'yyyymm')*/
            
            invLink = finCommon.getFinparams("999999", "INV_ACCT_AISS",conn);
            System.out.println("invLink :"+invLink);
            if(invLink == null || invLink.equalsIgnoreCase("NULLFOUND")){
                errString = itmDBAccessEJB.getErrorString("","VTFINPARM1","","",conn);
                return errString;
            }
            acctAdj = finCommon.getFinparams("999999", "CONS_ACCT_RNDOFF",conn);
            System.out.println("acctAdj :"+acctAdj);
            if(acctAdj == null || acctAdj.equalsIgnoreCase("") || acctAdj.equalsIgnoreCase("NULLFOUND")){
                errString = itmDBAccessEJB.getErrorString("","VTFINPARM1","","",conn);
                return errString;
            }
            cctrAdj = finCommon.getFinparams("999999", "CONS_CCTR_RNDOFF",conn);
            if(cctrAdj == null || cctrAdj.equalsIgnoreCase("") || cctrAdj.equalsIgnoreCase("NULLFOUND")){
                cctrAdj = "    ";
            }
            if(updStkCctrCodeInv == null || updStkCctrCodeInv.trim().length() == 0){
                updStkCctrCodeInv = "   ";
            }
            remarks = "Adjustment of Stock Value by System";
            
            if(this.netAmt > 0){ // adj_rcp
                refSer = "ADJRCP";
                adjWin = "w_adj_rcp";
                acctCodeDr = this.acctCodeInv;
                cctrCodeDr = this.cctrCodeInv;
                acctCodeCr = acctAdj;   
                cctrCodeCr =  cctrAdj;  
            }else{//adj_rcp
                refSer = "ADJISS";
                adjWin = "w_adj_iss";
                acctCodeDr = acctAdj;
                cctrCodeDr = cctrAdj;
                acctCodeCr = this.acctCodeInv;   
                cctrCodeCr = this.cctrCodeInv;  
            }
            sql = "select key_string from transetup where tran_window = '"+adjWin+"'";
            System.out.println("SQL :"+sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                keyString = rs.getString(1);
            }
            rs.close(); //Pavan Rane 26jul19 [unnecessary open cursor closed]
            if(keyString == null || keyString.trim().length() == 0){
                sql = "select key_string from transetup where tran_window = 'GENERAL'";
                System.out.println("SQL :"+sql);
                rs = stmt.executeQuery(sql);
                if(rs.next()){
                    keyString = rs.getString(1);
                }
                rs.close(); //Pavan Rane 26jul19 [unnecessary open cursor closed]
            }
                        
            String tranDate = genericUtility.getValidDateString(new java.sql.Date(this.tranDate.getTime()).toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
            String XMLString = "";
            XMLString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>"+
                        "\r\n</header><Detail1><tran_id></tran_id><tran_date>"+tranDate+"</tran_date>"+
                        "\r\n<site_code>"+this.siteCode+"</site_code>"+
                        "</Detail1></Root>";
            TransIDGenerator tg = new TransIDGenerator(XMLString, userId, CommonConstants.DB_NAME);
            tranIDAdj = tg.generateTranSeqID(refSer, "tran_id", keyString, conn);
            
            keyString = "";
            sql = "select key_string from transetup where tran_window = 'T_INVTRACE'";
            System.out.println("SQL :"+sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                keyString = rs.getString(1);                
            }
            rs.close();
            
            
            TransIDGenerator tg1 = new TransIDGenerator(XMLString, userId, CommonConstants.DB_NAME);
            invTraceNo = tg1.generateTranSeqID("ITRACE", "tran_id", keyString, conn);
            
           sql = "Insert into invtrace (tran_id, tran_date, ref_ser, ref_id, ref_line, order_id, " +
                "item_code, site_code, loc_code, unit, lot_no, quantity, eff_qty, eff_date, " +
                "chg_win,rate, gross_rate, amount,chg_user, chg_term, lot_sl, qty_before, " +
                "qty_after,reas_code,sundry_type,sundry_code, ref_date,remarks," +
                "acct_code__dr, cctr_code__dr, acct_code__cr, cctr_code__cr, grade) " +
                "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
               
           System.out.println("SQL :"+sql);
           pstmt = conn.prepareStatement(sql);
           pstmt.setString(1,invTraceNo);
           pstmt.setTimestamp(2,this.currDate);
           pstmt.setString(3,refSer);
           pstmt.setString(4,this.tranId);
           pstmt.setString(5,this.lineNo);
           pstmt.setString(6,this.sorderNo);
           pstmt.setString(7,this.itemCode);
           pstmt.setString(8,this.siteCode);
           pstmt.setString(9,this.locationCode);
           pstmt.setString(10,this.unit);
           pstmt.setString(11,this.lotNo);
           pstmt.setDouble(12,this.qtyStduom);
           pstmt.setDouble(13,effQty); 
           pstmt.setTimestamp(14,this.tranDate);
           pstmt.setString(15," ");
           pstmt.setDouble(16,this.rate);
           pstmt.setDouble(17,this.rate);
           pstmt.setDouble(18,this.netAmt);
           pstmt.setString(19,userId);
           pstmt.setString(20,termId);
           pstmt.setString(21,this.lotSl);
           pstmt.setDouble(22,beforeQty);
           pstmt.setDouble(23,afterQty);
           pstmt.setString(24,this.reasCode);
           pstmt.setString(25,this.sundryType);
           pstmt.setString(26,this.sundryCode);
           pstmt.setTimestamp(27,this.tranDate);
           pstmt.setString(28,remarks);
           pstmt.setString(29,acctCodeDr);
           pstmt.setString(30,cctrCodeDr);
           pstmt.setString(31,acctCodeCr);
           pstmt.setString(32,cctrCodeCr);
           pstmt.setString(33,this.grade);
           
           System.out.println("TRAN_ID :"+invTraceNo);
           System.out.println("TRAN_DATE :"+this.currDate);
           System.out.println("ITEM_CODE :"+this.itemCode);
           System.out.println("SITE_CODE :"+this.siteCode);
           System.out.println("LOC_CODE :["+this.locationCode+"]");
           System.out.println("UNIT :"+this.unit);
           System.out.println("EFF_DATE :"+this.tranDate);
           System.out.println("CHG_USER :");
           System.out.println("CHG_TERM :");
           
         	int updCnt =  pstmt.executeUpdate();
         	pstmt.close();
			/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
			if( updCnt > 0 )
			{
				InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
				HashMap demandSupplyMap = null;
				if( !("D-ISS".equalsIgnoreCase(this.tranSer.trim()) || "S-DSP".equalsIgnoreCase(this.tranSer.trim())) )
				{
					int invStCnt = 0;
					sql = "select count(*) from location, invstat "	
					+ " where location.inv_stat = invstat.inv_stat " 
					+ " and location.loc_code = ? "
					+ " and invstat.stat_type <> 'S'"; 	
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, this.locationCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						invStCnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					if( invStCnt > 0 )
					{
					    demandSupplyMap = new HashMap();
					    demandSupplyMap.put("site_code", this.siteCode);
						demandSupplyMap.put("item_code", this.itemCode);		
						demandSupplyMap.put("ref_ser", "STK");
						demandSupplyMap.put("ref_id", "NA");
						demandSupplyMap.put("ref_line", "NA");
						demandSupplyMap.put("due_date", this.tranDate);		
						demandSupplyMap.put("demand_qty", 0.0);
						demandSupplyMap.put("supply_qty", effQty);
						demandSupplyMap.put("change_type", "A");
						demandSupplyMap.put("chg_process", "T");
						demandSupplyMap.put("chg_user", userId);
					    demandSupplyMap.put("chg_term", termId);	
					    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
					    if(errString != null && errString.trim().length() > 0)
					    {
					    	System.out.println("retString["+errString+"]");
					    	return errString;
					    }
					}
				}
			}
		  /**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
           
           sql = "select fin_entity from site where site_code = '"+this.siteCode+"'";
           System.out.println("SQL :"+sql);
           rs = stmt.executeQuery(sql);
           if(rs.next()){
               finEntity = rs.getString(1);
           }
           rs.close();
           
           sql = "select curr_code from parameter";
           System.out.println("SQL :"+sql);
           rs = stmt.executeQuery(sql);
           if(rs.next()){
               currCode = rs.getString(1);
           }
           rs.close();
           exchRate = 1;
           System.out.println("this.netAmt : "+this.netAmt);
           if(invLink.equalsIgnoreCase("Y")){
               if(this.netAmt != 0){
                   glTrace = new HashMap();
                   glTrace.put("tran_date",this.tranDate);
                   glTrace.put("eff_date",this.tranDate);
                   glTrace.put("fin_entity",finEntity);
                   glTrace.put("site_code",this.siteCode);
                   glTrace.put("sundry_type","O");
                   glTrace.put("sundry_code","");
                   glTrace.put("emp_code","");
                   glTrace.put("anal_code","");
                   glTrace.put("curr_code",currCode);
                   glTrace.put("exch_rate","1");
                   
                   if(this.netAmt > 0){
                       glTrace.put("dr_amt","0");
                       glTrace.put("cr_amt",Double.toString(this.netAmt));
                       glTrace.put("acct_code",acctAdj);
                       glTrace.put("cctr_code",cctrAdj);
                   }else{
                       glTrace.put("dr_amt","0");
                       glTrace.put("cr_amt",Double.toString( 0 - this.netAmt));
                       glTrace.put("acct_code",this.acctCodeInv);
                       glTrace.put("cctr_code",this.cctrCodeInv);
                   }
                   glTrace.put("ref_type","D");
                   glTrace.put("remarks",remarks);
                   glTrace.put("ref_ser",refSer);
                   glTrace.put("ref_id",tranIDAdj);
                   
                   errString = finCommon.glTraceUpdate(glTrace,conn);
                   System.out.println("Returning String :"+errString);
                   if(errString.trim().length() > 0){
                       return errString;
                   }
                   if(this.netAmt > 0){
                       glTrace.put("dr_amt",Double.toString(this.netAmt));
                       glTrace.put("cr_amt","0");
                       glTrace.put("acct_code",this.acctCodeInv);
                       glTrace.put("cctr_code",this.cctrCodeInv);
                   }else{
                       glTrace.put("dr_amt",Double.toString(0 - this.netAmt));
                       glTrace.put("cr_amt","0");
                       glTrace.put("acct_code",acctAdj);
                       glTrace.put("cctr_code",cctrAdj);
                   }
                   errString = finCommon.glTraceUpdate(glTrace,conn);
                   System.out.println("Returning String :"+errString);
                   if(errString.trim().length() > 0){
                       return errString;
                   }
                   
                   errString = finCommon.checkGlTranDrCr(refSer,tranIDAdj,conn);
                   
               }
           }
           
        }catch(Exception e){
            System.out.println("Exception e "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }finally{
            try{
                if(rs != null){rs.close();rs = null;}
                if(stmt != null){stmt.close();stmt = null;}
                if(pstmt != null){pstmt.close();pstmt = null;}
            }catch(Exception t){throw new ITMException(t);}
        }
        System.out.println("Returning Value from adjustStkValue : "+errString);
        return errString;
    }
    /**
     * @param conn
     * @return
     * @throws ITMException
     * @throws Exception
     */
    private String updateStockMovement(Connection conn) throws ITMException, Exception
    {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        String prdCode = "",errCode = "", sql = "";
        String tranDateStr="", dbName = "";
		Timestamp tranDateTm=null;
        double qty = 0,amount = 0;
        double poRcpQty = 0,disRcpQty = 0,woIssQty = 0,disIssQty = 0,salIssQty = 0;
        double poRcpValue = 0,disRcpValue = 0,woIssValue = 0,disIssValue = 0,salIssValue = 0;
        double poRetQty = 0,conIssQty = 0,sretRcpQty = 0,sretIssQty = 0,adjQty = 0;
        double poRetValue = 0,conIssValue = 0,sretRcpValue = 0,sretIssValue = 0,adjValue = 0;
        double totRcpQty = 0,totIssQty = 0,openQty = 0,closeQty = 0;
        double totRcpValue = 0,totIssValue = 0,openValue = 0,closeValue = 0;
        try{
        	//Modified by Varsha V. on [18/12/2018][To take database name][Start]
			dbName = (CommonConstants.DB_NAME).trim();
			System.out.println("dbName in updateStockMovement--["+dbName+"]");
			//Modified by Varsha V. on [18/12/2018][To take database name][End]
            qty = this.qtyStduom;
            amount = qty * this.rate;
            if(this.tranSer.equalsIgnoreCase("P-RCP")){
                poRcpQty = poRcpQty + qty;
                poRcpValue = poRcpValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("D-RCP")){
                disRcpQty = disRcpQty + qty;
                disRcpValue = disRcpValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("W-ISS")){
                woIssQty = woIssQty + qty;
                woIssValue = woIssValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("D-ISS")){
                disIssQty = disIssQty + qty;
                disIssValue = disIssValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("S-DSP")){
                salIssQty = salIssQty + qty;
                salIssValue = salIssValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("P-RET")){
                poRetQty = poRetQty + qty;
                poRetValue = poRetValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("C-ISS")){
                conIssQty = conIssQty + qty;
                conIssValue = conIssValue + amount;
            }else if(this.tranSer.equalsIgnoreCase("S-RET")){
                if(this.tranType.equalsIgnoreCase("R")){
                    sretRcpQty = sretRcpQty + qty;
                    sretRcpValue = sretRcpValue + amount;
                }else{
                    sretIssQty = sretIssQty + qty;
                    sretIssValue = sretIssValue + amount;
                }
            }else if(this.tranSer.equalsIgnoreCase("ADJRCP")){
                adjQty = adjQty + qty;
                adjValue = adjValue + amount;
            }
            if(this.tranType.equalsIgnoreCase("R")){
                totRcpQty = totRcpQty + qty;
                totRcpValue = totRcpValue + amount;
            }else{
                totIssQty = totIssQty + qty;
                totIssValue = totIssValue + amount;
            }
            //commented by chandrashekar on 27-may-2016
          /*  sql = "select sum(qty),sum(val) from (select sum(a.quantity) qty,sum(a.quantity * case when a.rate is null then 0 else a.rate end) val     " +
                    "from  stock a , invstat b , location c " +
                    "where a.loc_code   = c.loc_code " +
                    "and   b.inv_stat       = c.inv_stat " +
                    "and   a.item_code      = '"+this.itemCode+"' " +
                    "and   a.site_code      = '"+this.siteCode+"' " +
                    "and   a.lot_no     = '"+this.lotNo+"' " +
                    "union all " +
                    "select ( case when (sum(eff_qty) * -1) is null then 0 else (sum(eff_qty) * -1) end ) qty , " +
                    "( case when (sum(eff_qty * case when rate is null then 0 else rate end) * -1) is null then 0 else (sum(eff_qty * case when rate is null then 0 else rate end) * -1) end ) val " +
                    "from  invtrace a , location b , invstat c , refser d " +
                    "where a.loc_code       = b.loc_code " +
                    "and   a.ref_ser    = d.ref_ser " +
                    "and   b.inv_stat   = c.inv_stat " +
                    "and   a.item_code  = '"+this.itemCode+"' " +
                    "and   a.site_code  = '"+this.siteCode+"' " +
                    "and   a.lot_no     = '"+this.lotNo+"' " +
                    "and   a.tran_date  > ? )";
                    //"and   a.tran_date  > ? ) as stk";
                    System.out.println("SQL :"+sql);
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setTimestamp(1,this.tranDate);
                    rs = pstmt.executeQuery();
                    if(rs.next()){
                        openQty = rs.getDouble(1);
                        openValue = rs.getDouble(2);
                    }
                    closeQty = openQty + qty;
                    closeValue = openValue+ amount;
                    rs.close();
                    pstmt.close();*/
		            if(this.tranDate !=null)
		    		{
		    			Format format = new SimpleDateFormat(genericUtility.getApplDateFormat());
		        		tranDateStr=format.format(this.tranDate);
		        		if(tranDateStr!=null && tranDateStr.trim().length()>0)
		        		{
		        			tranDateTm = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
		        		}
		        		System.out.println("TranDate After>>"+tranDateTm);
		    		}
		            
                    sql = "SELECT CODE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE";
                    System.out.println("SQL :"+sql);
                    pstmt = conn.prepareStatement(sql);
                    //pstmt.setTimestamp(1,this.tranDate);//change by chandrashekar on 31-aug-2016
                    pstmt.setTimestamp(1,tranDateTm);
                    rs = pstmt.executeQuery();
                    if(rs.next()){
                        prdCode = rs.getString("CODE");
                    }
                    rs.close();
                    pstmt.close();
                    
                    //Start added by chandrashekar on 27-may-2016
                    if(prdCode ==  null || prdCode.trim().length()==0)
                    {
                    	errString = itmDBAccessEJB.getErrorString("","VTPRD1","","",conn);
                    	return errString;
                    }
                    count = 0;
                    //End added by chandrashekar on 27-may-2016
                    /*Commented and added one more where clause by Varsha V on 18-12-18
 					sql = "select count(*) from stock_movement " +
                            "where site_code = '"+this.siteCode+"' and period_code = '"+prdCode+"' " +
                            "and item_code = '"+this.itemCode+"' and lot_no = '"+this.lotNo+"'";
                    */
                    sql = "select count(*) as cnt from stock_movement " +
                            "where site_code = '"+this.siteCode+"' and period_code = '"+prdCode+"' " +
                            "and item_code = '"+this.itemCode+"' and lot_no = '"+this.lotNo+"' and inv_stat = '"+this.invStat+"' ";
                    System.out.println("SQL :"+sql);
                    pstmt = conn.prepareStatement(sql);
                    rs = pstmt.executeQuery();
                    if(rs.next()){
                        count = rs.getInt(1);
                    }
                    rs.close();
                    pstmt.close();
                    System.out.println("COunt in stock_movement: "+count);
                    if(count == 0)
                    {
                    	
                    	//Start added by chandrashekar on 27-may-2016
                    	 sql = "select case when close_qty is null then 0 else close_qty end as close_qty," +
                    	 		"case when close_value is null then 0 else close_value end as  close_value from   stock_movement where  " +
                    	 		" site_code = '"+this.siteCode+"' and item_code 	= '"+this.itemCode+"' " +
                    	 		" and lot_no =  '"+this.lotNo+"' and inv_stat ='"+this.invStat+"' " +
                    	 		" and period_code = ( select max(period_code) from stock_movement" +
                    	 		" where  site_code = '"+this.siteCode+"' and period_code < ? " +
                    	 		" and item_code	= '"+this.itemCode+"' and  lot_no = '"+this.lotNo+"' " +
                    	 		" and inv_stat ='"+this.invStat+"')";
                         pstmt = conn.prepareStatement(sql);
                         pstmt.setString(1,prdCode);
                         rs = pstmt.executeQuery();
                         System.out.println("SQL :"+sql);
                         if(rs.next()){
                        	 openQty = rs.getDouble(1);
                        	 openValue = rs.getDouble(2);
                         }
                         rs.close();
                         pstmt.close();
                         if(this.tranType.equalsIgnoreCase("R")){
                        	 closeQty = openQty + qty;
                             closeValue = openValue+ amount;
                         }else
                         {
                        	 closeQty = openQty - qty;
                             closeValue = openValue - amount;
                         }
                         
                    	//End added by chandrashekar on 27-may-2016
                        sql = "insert into stock_movement (site_code,period_code,item_code,lot_no," +
                                "open_qty,open_value,po_rcp_qty,po_rcp_value,dis_rcp_qty,dis_rcp_value," +
                                "sret_rcp_qty,sret_rcp_value,tot_rcp_qty,tot_rcp_value,wo_iss_qty,wo_iss_value," +
                                "dis_iss_qty,dis_iss_value,sal_iss_qty,sal_iss_value,po_ret_qty,po_ret_value," +
                                "sret_iss_qty,sret_iss_value,con_iss_qty,con_iss_value,pm_iss_qty,pm_iss_value," +
                                "tot_iss_qty,tot_iss_value,adj_qty,adj_value,close_qty,close_value,inv_stat) values (?,?,?,?,?,?," +
                                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        System.out.println("SQL :"+sql);
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1,this.siteCode);
                        pstmt.setString(2,prdCode);
                        pstmt.setString(3,this.itemCode);
                        pstmt.setString(4,this.lotNo);
                        pstmt.setDouble(5,openQty);
                        pstmt.setDouble(6,openValue);
                        pstmt.setDouble(7,poRcpQty);
                        pstmt.setDouble(8,poRcpValue);
                        pstmt.setDouble(9,disRcpQty);
                        pstmt.setDouble(10,disRcpValue);
                        pstmt.setDouble(11,sretRcpQty);
                        pstmt.setDouble(12,sretRcpValue);
                        pstmt.setDouble(13,totRcpQty);
                        pstmt.setDouble(14,totRcpValue);
                        pstmt.setDouble(15,woIssQty);
                        pstmt.setDouble(16,woIssValue);
                        pstmt.setDouble(17,disIssQty);
                        pstmt.setDouble(18,disIssValue);
                        pstmt.setDouble(19,salIssQty);
                        pstmt.setDouble(20,salIssValue);
                        pstmt.setDouble(21,poRetQty);
                        pstmt.setDouble(22,poRetValue);
                        pstmt.setDouble(23,sretIssQty);
                        pstmt.setDouble(24,sretIssValue);
                        pstmt.setDouble(25,conIssQty);
                        pstmt.setDouble(26,conIssValue);
                        pstmt.setDouble(27,0);
                        pstmt.setDouble(28,0);
                        pstmt.setDouble(29,totIssQty);
                        pstmt.setDouble(30,totIssValue);
                        pstmt.setDouble(31,adjQty);
                        pstmt.setDouble(32,adjValue);
                        pstmt.setDouble(33,closeQty);
                        pstmt.setDouble(34,closeValue);
                        pstmt.setString(35,this.invStat);//added by chandrashekar on 27-may-2016
                        
                        pstmt.executeUpdate();
                        
                    }
                    else{
                    	//Start added by chandrashekar on 27-may-2016
                    	if(!"R".equalsIgnoreCase(this.tranType))
                    	{
                       	 	closeQty = qty * (-1);
                            closeValue = amount * (-1);
                        }
                    	//Added by Varsha V on 14-12-18 for locking changes suggested by SManoharan
                        if (("db2".equalsIgnoreCase(dbName)) || ("mysql".equalsIgnoreCase(dbName)))
    					{
                        	sql = "select site_code, period_code, item_code, lot_no, inv_stat from stock_movement " +
                                    "where site_code = ? and period_code = ? " +
                                    "and item_code = ? and lot_no = ? and inv_stat = ? for update";
    					}
    					else if("mssql".equalsIgnoreCase(dbName))
    					{
    						sql = "select site_code, period_code, item_code, lot_no, inv_stat from stock_movement " +
                                    "where site_code = ? and period_code = ? " +
                                    "and item_code = ? and lot_no = ? and inv_stat = ? ";
    					}
    					else
    					{
    						sql = "select site_code, period_code, item_code, lot_no, inv_stat from stock_movement " +
                                    "where site_code = ? and period_code = ? " +
                                    "and item_code = ? and lot_no = ? and inv_stat = ? for update nowait";
    					}
                        String siteCode = "", periodCode = "", itemCode = "", lotNo = "", invStat = "";
                        System.out.println("this.siteCode: "+this.siteCode + " prdCode: "+prdCode+" this.itemCode: "+this.itemCode+
                        		" this.lotNo: "+this.lotNo+" this.invStat: "+this.invStat);
                        count = 0;
                        System.out.println("SQL :"+sql);
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1,this.siteCode);
                        pstmt.setString(2,prdCode);
                        pstmt.setString(3,this.itemCode);
                        pstmt.setString(4,this.lotNo);
                        pstmt.setString(5,this.invStat);
                        rs = pstmt.executeQuery();
                        if(rs.next())
    					{
                        	count++;
                        	siteCode = rs.getString("site_code");
                        	periodCode = rs.getString("period_code");
                        	itemCode = rs.getString("item_code");
                        	lotNo = rs.getString("lot_no");
                        	invStat = rs.getString("inv_stat");
    					}
    					else
    					{
    						errString = itmDBAccessEJB.getErrorString("","VTLCKERR","","",conn);
                        	return errString;
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
    					//Ended by Varsha V on 14-12-18 for locking changes suggested by SManoharan
                    	//End added by chandrashekar on 27-may-2016
                        sql = "update stock_movement set " +
                                //" open_qty = ?, " +
                                //"open_value = ?," +
                                "po_rcp_qty = (case when po_rcp_qty is null then 0 else po_rcp_qty end)+ ?," +
                                "po_rcp_value = (case when po_rcp_value is null then 0 else po_rcp_value end)+?," +
                                "dis_rcp_qty = (case when dis_rcp_qty is null then 0 else dis_rcp_qty end)+ ?," +
                                "dis_rcp_value = (case when dis_rcp_value is null then 0 else dis_rcp_value end)+?," +
                                "sret_rcp_qty = (case when sret_rcp_qty is null then 0 else sret_rcp_qty end)+ ?," +
                                "sret_rcp_value = (case when sret_rcp_value is null then 0 else sret_rcp_value end) +?," +
                                "tot_rcp_qty = (case when tot_rcp_qty is null then 0 else tot_rcp_qty end)+ ?," +
                                "tot_rcp_value =(case when tot_rcp_value is null then 0 else tot_rcp_value end)+ ?," +
                                "wo_iss_qty = (case when wo_iss_qty is null then 0 else wo_iss_qty end) + ?," +
                                "wo_iss_value = (case when wo_iss_value is null then 0 else wo_iss_value end) + ?," +
                                "dis_iss_qty = (case when dis_iss_qty is null then 0 else dis_iss_qty end) + ?," +
                                "dis_iss_value = (case when dis_iss_value is null then 0 else dis_iss_value end) + ?," +
                                "sal_iss_qty = (case when sal_iss_qty is null then 0 else sal_iss_qty end)+ ?," +
                                "sal_iss_value = (case when sal_iss_value is null then 0 else sal_iss_value end)+ ?," +
                                "po_ret_qty = (case when po_ret_qty is null then 0 else po_ret_qty end) + ?," +
                                "po_ret_value = (case when po_ret_value is null then 0 else po_ret_value end) + ?," +
                                "sret_iss_qty = (case when sret_iss_qty is null then 0 else sret_iss_qty end) + ?," +
                                "sret_iss_value = (case when sret_iss_value is null then 0 else sret_iss_value end)+ ?," +
                                "con_iss_qty = (case when con_iss_qty is null then 0 else con_iss_qty end) + ?," +
                                "con_iss_value = (case when con_iss_value is null then 0 else con_iss_value end) + ?," +
                                "pm_iss_qty = (case when pm_iss_qty is null then 0 else pm_iss_qty end) + ?," +
                                "pm_iss_value = (case when pm_iss_value is null then 0 else pm_iss_value end) + ?," +
                                "tot_iss_qty = (case when tot_iss_qty is null then 0 else tot_iss_qty end) + ?," +
                                "tot_iss_value = (case when tot_iss_value is null then 0 else tot_iss_value end) + ?," +
                                "adj_qty = (case when adj_qty is null then 0 else adj_qty end)+ ?," +
                                "adj_value = (case when adj_value is null then 0 else adj_value end) + ?," +
                                "close_qty = (case when close_qty is null then 0 else close_qty end)+ ?," +
                                "close_value = (case when close_value is null then 0 else close_value end) + ? " +
                                "where site_code = ? " +
                                "and period_code = ? " +
                                "and item_code = ? " +
                                "and lot_no = ? and inv_stat = ? ";
                        System.out.println("SQL :: "+sql);
                        pstmt = conn.prepareStatement(sql);
                       // pstmt.setDouble(1,openQty);
                       // pstmt.setDouble(2,openValue);
                        pstmt.setDouble(1,poRcpQty);
                        pstmt.setDouble(2,poRcpValue);
                        pstmt.setDouble(3,disRcpQty);
                        pstmt.setDouble(4,disRcpValue);
                        pstmt.setDouble(5,sretRcpQty);
                        pstmt.setDouble(6,sretRcpValue);
                        pstmt.setDouble(7,totRcpQty);
                        pstmt.setDouble(8,totRcpValue);
                        pstmt.setDouble(9,woIssQty);
                        pstmt.setDouble(10,woIssValue);
                        pstmt.setDouble(11,disIssQty);
                        pstmt.setDouble(12,disIssValue);
                        pstmt.setDouble(13,salIssQty);
                        pstmt.setDouble(14,salIssValue);
                        pstmt.setDouble(15,poRetQty);
                        pstmt.setDouble(16,poRetValue);
                        pstmt.setDouble(17,sretIssQty);
                        pstmt.setDouble(18,sretIssValue);
                        pstmt.setDouble(19,conIssQty);
                        pstmt.setDouble(20,conIssValue);
                        pstmt.setDouble(21,0);
                        pstmt.setDouble(22,0);
                        pstmt.setDouble(23,totIssQty);
                        pstmt.setDouble(24,totIssValue);
                        pstmt.setDouble(25,adjQty);
                        pstmt.setDouble(26,adjValue);
                        pstmt.setDouble(27,closeQty);
                        pstmt.setDouble(28,closeValue);
                        pstmt.setString(29,this.siteCode);
                        pstmt.setString(30,prdCode);
                        pstmt.setString(31,this.itemCode);
                        pstmt.setString(32,this.lotNo);
                        pstmt.setString(33,this.invStat);//added by chandrashekar on 27-may-2016
                        
                        pstmt.executeUpdate();
                    }
        }catch(Exception e){
            System.out.println("Exception "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }finally{
            try{
                if(rs != null){rs.close();rs = null;}
                if(pstmt != null){pstmt.close();pstmt = null;}
            }catch(Exception t){throw new ITMException(t);}
        }
        return  "";
    }
    /**
     * @param conn
     * @return
     * @throws ITMException
     * @throws Exception
     */
    private String calcWeights(Connection conn) throws ITMException, Exception
    {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String issCriteria = "";
        String sql = "";
        double stockQty = 0,stkGrossWt = 0,stkNetWt = 0,stkTareWt = 0,qtyPerArt = 0;
        double grossWtPerArt = 0,tareWtPerArt = 0;
        long stkNoArt = 0;
        try{
            if(this.tranSer.equalsIgnoreCase("S-ISS")){
                return "";
            }
            sql = "select case when iss_criteria is null then 'I' else iss_criteria end from item where item_code = '"+this.itemCode+"'";
            System.out.println("SQL : "+sql);
            pstmt = conn.prepareStatement(sql);
            
            rs = pstmt.executeQuery();
            if(rs.next()){
                issCriteria = rs.getString(1);
            }
            rs.close();
            pstmt.close();
            sql = "select quantity,gross_weight, net_weight, tare_weight, no_art," +
                    "qty_per_art, gross_wt_per_art, tare_wt_per_art " +
                    "from stock " +
                    "where item_code = '"+this.itemCode+"' and " +
                    "site_code = '"+this.siteCode+"' and loc_code = '"+this.locationCode+"' "+
                    "and lot_no = '"+this.lotNo+"' and lot_sl = '"+this.lotSl+"'";        
                
            System.out.println("SQL :"+sql);
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if(rs.next()){
                stockQty = rs.getDouble(1);
                stkGrossWt = rs.getDouble(2);
                stkNetWt = rs.getDouble(3);
                stkTareWt = rs.getDouble(4);
                stkNoArt = rs.getLong(5);
                qtyPerArt = rs.getDouble(6);
                grossWtPerArt = rs.getDouble(7);
                tareWtPerArt = rs.getDouble(8);
            }
            rs.close();
            pstmt.close();
            if(issCriteria != null && issCriteria.trim().equalsIgnoreCase("W")){
                if(stockQty > 0){
                    this.netWeight = stkNetWt / stockQty * this.qtyStduom;
                    this.grossWeight = this.netWeight + this.tareWeight;
                }
            }else{
                if(qtyPerArt == 0){
                    if(stkNoArt == 0) stkNoArt = 1;
                    qtyPerArt = stockQty / stkNoArt;
                }
                if(qtyPerArt != 0){
                    if(stockQty > 0 && this.qtyStduom > 0){
                        this.netWeight = stkNetWt / stockQty * this.qtyStduom;
                    }
                    this.tareWeight = tareWtPerArt * this.noArt;
                    this.grossWeight = this.netWeight + this.tareWeight;
                }
            }
        }catch(Exception e){
            System.out.println("Exception e "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }finally{
            try{
                if(rs != null){rs.close();rs = null;}
                if(pstmt != null){pstmt.close();pstmt = null;}
            }catch(Exception t){throw new ITMException(t);}
        }
        return "";
    }
    
	/**
	 * @param siteCode
	 * @param itemCode
	 * @param conn
	 * @return
	 * @throws ITMException
	 * @throws Exception
	 */
	public String checkStkOptRnd(String siteCode,String itemCode,Connection conn) throws ITMException, Exception
	{
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		String varValue=null;
		String lsStkOpt = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
        ArrayList siteArrList = null;
        String paramSite = "";
    	try{
            siteArrList = new ArrayList();
    		String sql="SELECT VAR_VALUE FROM MFGPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'RND_NO_STK_SITE' ";
            System.out.println("SQL :"+sql);
    		pstmt = conn.prepareStatement(sql);
    		rs = pstmt.executeQuery();
            if(rs.next()){
                varValue = rs.getString(1);
            }
            if(varValue != null && varValue.trim().length() > 0){
                siteArrList = genericUtility.getTokenList(varValue,",");
            }
            if(siteArrList.size() > 0){
                for(int i = 0;i < siteArrList.size();i++){
                    paramSite = siteArrList.get(i).toString();
                    if(paramSite.equalsIgnoreCase(siteCode)){
                        lsStkOpt = "0";
                        break;
                    }
                }
            }
        }catch(Exception e){
            System.out.println("Exception checkStkOptRnd "+e.getMessage());
            e.printStackTrace();      
            throw new ITMException(e);
    	}finally{
    	    try{
                if(rs != null){rs.close();rs = null;}
                if(pstmt != null){pstmt.close();pstmt = null;}
            }catch(Exception t){throw new ITMException(t);}
        }
        System.out.println("Returning Stock Option RND :"+lsStkOpt);
        return lsStkOpt;
	}
	
	//*****************************
    /**
     * checks stock update option
     *
     * @param     itemCode  Item code
     * @param     siteCode Site code
     * @param	  conn   Database connection
     * @return    Error code if fails
     * @exception ITMException 
     */
	public String checkStkOpt(String itemCode,String siteCode,Connection conn) throws ITMException, Exception
	{
		String stkOpt=null;
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		String sql=null;
		try{
            sql = "SELECT CASE WHEN STK_OPT IS NULL THEN 'N' ELSE STK_OPT END "
			+" FROM SITEITEM WHERE ITEM_CODE='"+itemCode+"' AND SITE_CODE='"+siteCode+"'";
            System.out.println("SQL :"+sql);
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				stkOpt=rs.getString(1);
			}
			rs.close();
			pstmt.close();
			if(stkOpt == null || stkOpt.equals("") || stkOpt.equalsIgnoreCase("N")){
				sql = "SELECT CASE WHEN STK_OPT IS NULL THEN '0' ELSE STK_OPT END "
					+" FROM ITEM WHERE ITEM_CODE='"+itemCode+"' ";
                System.out.println("SQL :"+sql);
				pstmt=conn.prepareStatement(sql);
				rs=pstmt.executeQuery();
				if(rs.next()){
					stkOpt=rs.getString(1);
				}
				rs.close(); //Pavan Rane 26jul19 [unnecessary open cursor closed]
				pstmt.close();
			}
		}catch (Exception e){
			System.out.println("Exception in checkStkOpt "+e);            
            e.printStackTrace();
            throw new ITMException(e);
		}finally{
		    try{
                if(rs != null){rs.close();rs = null;}
                if(pstmt != null){pstmt.close();pstmt = null;}
            }catch(Exception t){throw new ITMException(t);}
        }
        System.out.println("Returning Stock Option : "+stkOpt);
		return stkOpt;
	}	
	
	// added by cpatil on 16/11/12 start adding stock update function
	/**
	 * @param updateStockMap
	 * @param conn
	 * @return
	 * @throws ITMException
	 * @throws Exception
	 */
	private HashMap updateLotInfo(HashMap updateStockMap ,Connection conn) throws ITMException, Exception
	{		
       String sql="",errCode="",sql2="";
       ResultSet rs=null, rs2=null;
       PreparedStatement pstmt=null, pstmt2=null;
       //String shelfLifeType=""
	   String lotNo="",itemCode="",grade="",stkOpt="";
       int cnt=0,cnt1=0;
       Timestamp expDate=null , mfgDate=null, retestDate=null ,retestDate_upd3=null;
       //Date today=null;
       double grossrate=0;
       String packCode="",siteCodeMfg="",acctCodeInv="",cctrCodeInv="",batchNo="",suppCodeMfg="",suppCode="";
       double  potencyPerc=0 , grossRate=0 , rate=0 , batchSize=0;
       java.util.Date retestDate3=null,expDate3=null,mfgDate3=null,retestDate_upd2=null;
       Calendar currentDate = Calendar.getInstance();
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
       Timestamp today = Timestamp.valueOf(simpleDateFormat.format(simpleDateFormat.parse(simpleDateFormat.format(currentDate.getTime()))).toString() + " 00:00:00.0");
       System.out.println("Now the date is :=>  " + today);
		String available="";
      // today  = new java.sql.Date(System.currentTimeMillis());
       System.out.println("today:::["+today+"]");
       
       
       
       System.out.println("@@@@@ updateLotInfo() method called..............");
       
       System.out.println("Comming Map :::- ["+updateStockMap+"]");

	try{
                    
    		if(updateStockMap.get("item_code")!= null)
    		{
    			this.itemCode = updateStockMap.get("item_code").toString();
    		}
    		if(updateStockMap.get("site_code")!= null)
    		{
    			this.siteCode = updateStockMap.get("site_code").toString();
    		}
    		if(updateStockMap.get("loc_code")!= null)
    		{
    			this.locationCode = updateStockMap.get("loc_code").toString();
    		}
    		if(updateStockMap.get("lot_no")!= null)
    		{
    			this.lotNo = updateStockMap.get("lot_no").toString();
    		}
    		if(updateStockMap.get("lot_sl")!= null)
    		{
    			this.lotSl = updateStockMap.get("lot_sl").toString();
    		}
    		if(updateStockMap.get("quantity")!= null)
    		{
    			this.quantity = Double.parseDouble(updateStockMap.get("quantity").toString());
    		}
    		if(updateStockMap.get("unit") != null)
    		{
    			this.unit = updateStockMap.get("unit").toString();
    		}
    		if(updateStockMap.get("tran_type")!= null)
    		{
    			this.tranType = updateStockMap.get("tran_type").toString();
    		}
    		if(updateStockMap.get("check_expiry")!= null)
    		{
    			this.checkExpiry = updateStockMap.get("check_expiry").toString();
    		}
    		if(updateStockMap.get("tran_date")!= null)
    		{
    			this.tranDate = (Timestamp)updateStockMap.get("tran_date");			
    		}
    		if(updateStockMap.get("tran_ser") != null)
    		{
    			this.tranSer = updateStockMap.get("tran_ser").toString();
    		}
    		if(updateStockMap.get("tran_id")!=null)
    		{
    			this.tranId = updateStockMap.get("tran_id").toString();
    		}
    		if(updateStockMap.get("acct_code__cr")!=null)
    		{
    			this.acctCodeCr = updateStockMap.get("acct_code__cr").toString();
    		}
    		if(updateStockMap.get("acct_code__dr") != null)
    		{
    			this.acctCodeDr = updateStockMap.get("acct_code__dr").toString();
    		}
    		if(updateStockMap.get("cctr_code__cr") != null)
    		{
    			this.cctrCodeCr = 	updateStockMap.get("cctr_code__cr").toString();
    		}
    		if(updateStockMap.get("cctr_code__dr") != null)
    		{
    			this.cctrCodeDr = updateStockMap.get("cctr_code__dr").toString();
    		}
    		if(updateStockMap.get("line_no")!=null)
    		{
    			this.lineNo = updateStockMap.get("line_no").toString();
    		}
    		if(updateStockMap.get("sorder_no")!=null)
    		{
    			this.sorderNo = updateStockMap.get("sorder_no").toString();
    		}
    		if(updateStockMap.get("qty_stduom")!=null)
    		{
    			this.qtyStduom = Double.parseDouble(updateStockMap.get("qty_stduom").toString());
    		}
    		if(updateStockMap.get("rate")!=null)
    		{
    			this.rate = Double.parseDouble(updateStockMap.get("rate").toString());
    		}
    		if(updateStockMap.get("site_code__mfg")!= null)
    		{
    			this.siteCodeMfg = 	updateStockMap.get("site_code__mfg").toString();
    		}
    		if(updateStockMap.get("mfg_date")!=null)
    		{
    			this.mfgDate = 	(Timestamp)updateStockMap.get("mfg_date");
    		}
    		if(updateStockMap.get("potency_perc")!=null)
    		{
    			this.potencyPerc = 	Double.parseDouble(updateStockMap.get("potency_perc").toString());
    		}
    		if(updateStockMap.get("exp_date")!=null)
    		{
    			this.expDate = 	(Timestamp)updateStockMap.get("exp_date");
    		}
    		if(updateStockMap.get("pack_code")!=null)
    		{
    			this.packCode = 	updateStockMap.get("pack_code").toString();
    		}
    		if(updateStockMap.get("item_ser")!=null)
    		{
    			this.itemSer = 	updateStockMap.get("item_ser").toString();
    		}
    		if(updateStockMap.get("reas_code")!=null)
    		{
    			this.reasCode = updateStockMap.get("reas_code").toString();
    		}
    		if(updateStockMap.get("inv_stat")!=null)
    		{
    			this.invStat = 	updateStockMap.get("inv_stat").toString();
    		}
    		if(updateStockMap.get("net_amt")!=null)
    		{
    			this.netAmt = 	Double.parseDouble(updateStockMap.get("net_amt").toString());
    		}
    		if(updateStockMap.get("sundry_type")!=null)
    		{
    			this.sundryType = 	updateStockMap.get("sundry_type").toString();
    		}
    		if(updateStockMap.get("sundry_code")!=null)
    		{
    			this.sundryCode = 	updateStockMap.get("sundry_code").toString();
    		}
    		if(updateStockMap.get("curr_date")!= null)
    		{
    			this.currDate = (Timestamp)	updateStockMap.get("curr_date");
    		}
    		if(updateStockMap.get("gross_weight")!=null)
    		{
    			this.grossWeight = Double.parseDouble(updateStockMap.get("gross_weight").toString());
    		}
    		if(updateStockMap.get("tare_weight")!=null)
    		{
    			this.tareWeight = Double.parseDouble(updateStockMap.get("tare_weight").toString());
    		}
    			if(updateStockMap.get("net_weight")!=null)
    		{
    				this.netWeight = Double.parseDouble(updateStockMap.get("net_weight").toString());
    		}
    		if(updateStockMap.get("pack_instr")!=null)
    		{
    			this.packInstr = updateStockMap.get("pack_instr").toString();
    		}
    		if(updateStockMap.get("dimension")!=null)
    		{
    			this.dimension = 	updateStockMap.get("dimension").toString();
    		}
    		if(updateStockMap.get("acct_code_inv")!=null)
    		{
    			this.acctCodeInv = 	updateStockMap.get("acct_code_inv").toString();
    		}
    		if(updateStockMap.get("cctr_code_inv")!=null)
    		{
    			this.cctrCodeInv = 	updateStockMap.get("cctr_code_inv").toString();
    		}
    		if(updateStockMap.get("rate_oh")!= null)
    		{
    			this.rateOh = 	Double.parseDouble(updateStockMap.get("rate_oh").toString());
    		}
    		if(updateStockMap.get("acct_code_oh")!= null)
    		{
    			this.acctCodeOh = 	updateStockMap.get("acct_code_oh").toString();
    		}
    		if(updateStockMap.get("cctr_code_oh")!=null)
    		{
    			this.cctrCodeOh = 	updateStockMap.get("cctr_code_oh").toString();
    		}
    		if(updateStockMap.get("retest_date")!=null)
    		{
    			this.retestDate = (Timestamp)updateStockMap.get("retest_date");
    		}
    		if(updateStockMap.get("supp_code__mfg")!=null)
    		{
    			this.suppCodeMfg = 	updateStockMap.get("supp_code__mfg").toString();
    		}
    		if(updateStockMap.get("grade")!=null)
    		{
    			this.grade = updateStockMap.get("grade").toString();
    		}
    		if(updateStockMap.get("gross_rate")!=null)
    		{
    			this.grossRate = Double.parseDouble(updateStockMap.get("gross_rate").toString());
    		}
    		if(updateStockMap.get("conv__qty_stduom")!=null)
    		{
    			this.convQtyStduom = Double.parseDouble(updateStockMap.get("conv__qty_stduom").toString());
    		}
    		if(updateStockMap.get("batch_no")!= null)
    		{
    			this.batchNo = 	updateStockMap.get("batch_no").toString();
    		}
    		if(updateStockMap.get("unit__alt")!= null)
    		{
    			this.unitAlt = 	updateStockMap.get("unit__alt").toString();
    		}
    		if(updateStockMap.get("no_art")!=null)
    		{
    			this.noArt = Double.parseDouble(updateStockMap.get("no_art").toString());
    		}
    		if(updateStockMap.get("last_phyc_date")!=null)
    		{
    			this.lastPhycDate = (Timestamp)updateStockMap.get("last_phyc_date");
    		}
    		if(updateStockMap.get("remarks")!=null)
    		{
    			this.remarks = 	updateStockMap.get("remarks").toString();
    		}
    		if(updateStockMap.get("ref_id__for")!=null)
    		{
    			this.refIdFor = updateStockMap.get("ref_id__for").toString();
    		}
    		if(updateStockMap.get("ref_ser__for")!=null)
    		{
    			this.refSerFor = updateStockMap.get("ref_ser__for").toString();
    		}
    		if(updateStockMap.get("actual_rate")!=null)
    		{
    			this.actualRate = Double.parseDouble(updateStockMap.get("actual_rate").toString());
    		}
            if(updateStockMap.get("pack_ref")!= null)
            {
                this.packRef = updateStockMap.get("pack_ref").toString();
            }
            if(updateStockMap.get("consider_allocate")!= null)
            {
                this.considerAllocate = updateStockMap.get("consider_allocate").toString();
            }
            if(updateStockMap.get("partial_used")!= null)
            {
                this.partialUsed = updateStockMap.get("partial_used").toString();
            }
    		if(updateStockMap.get("batch_size")!=null)
    		{
    			this.batchSize = Double.parseDouble(updateStockMap.get("batch_size").toString());
    		}
    		if(updateStockMap.get("supp_code")!=null)
    		{
    			this.suppCode = updateStockMap.get("supp_code").toString();
    		}
    		if(updateStockMap.get("shelf_life_type")!=null)
    		{
    			this.shelfLifeType = updateStockMap.get("shelf_life_type").toString();
    		}
    		

    		stkOpt = checkStkOpt(this.itemCode,this.siteCode,conn);
			System.out.println("@@@@@ stkOpt[gfChkStkOpt] :::- ["+stkOpt+"]");
    		
			if( "2".equalsIgnoreCase(stkOpt))
			{	                     
				 //Change shelf_life_type TO shelf_life__type.  CHANGE DONE  BY RITESH TIWARI ON 15/11/13
				/* sql = " select shelf_life__type from item where item_code = ?  ";
		           pstmt = conn.prepareStatement(sql);
		   		   pstmt.setString(1,this.itemCode);
		   		   rs = pstmt.executeQuery();
		           if(rs.next())
		           {
		        	  this.shelfLifeType = rs.getString(1);
		           	  System.out.println("@@@@ shelfLifeType:"+shelfLifeType);
		           }
		           rs.close();
		   		   rs = null;
		   		   pstmt.close();
		   		   pstmt = null;
				*/
		   		   
		   sql = " select count (*) from item_lot_info where item_code = ? and lot_no = ? ";
           pstmt = conn.prepareStatement(sql);
   		   pstmt.setString(1,this.itemCode);
   		   pstmt.setString(2,this.lotNo);
   		   rs = pstmt.executeQuery();
           if(rs.next())
           {
           	cnt = rs.getInt(1);
           	System.out.println("@@@@ cnt:"+cnt);
           }
           rs.close();
   		   rs = null;
   		   pstmt.close();
   		   pstmt = null;
           
           if( cnt > 0 )
           {
        	     sql = " select item_code,lot_no,grade,exp_date,mfg_date,retest_date,pack_code,potency_perc,site_code__mfg,rate," +
        	     	   " gross_rate,acct_code__inv,cctr_code__inv,batch_no,batch_size,supp_code__mfg,shelf_life_type,supp_code " +
        	    	   " from item_lot_info where item_code = ? and lot_no = ? " ; 
        	    pstmt = conn.prepareStatement(sql);
         		pstmt.setString(1,this.itemCode);
         		pstmt.setString(2,this.lotNo);
         		rs = pstmt.executeQuery();
                if(rs.next())
                 {
                	 itemCode  = rs.getString("item_code");
                	 lotNo 	   =  rs.getString("lot_no");
                	 grade 	   =  rs.getString("grade");
                	 expDate   =  rs.getTimestamp("exp_date");
                	 mfgDate   =  rs.getTimestamp("mfg_date");
                	 retestDate  = rs.getTimestamp("retest_date");
                	 packCode    = rs.getString("pack_code");
                	 potencyPerc = rs.getDouble("potency_perc");
                	 siteCodeMfg = rs.getString("site_code__mfg");
                	 rate  		 = rs.getDouble("rate");
                	 grossRate 	 = rs.getDouble("gross_rate");
                	 acctCodeInv = rs.getString("acct_code__inv");
                	 cctrCodeInv = rs.getString("cctr_code__inv");
                	 batchNo  	 = rs.getString("batch_no");
                	 batchSize 	 = rs.getDouble("batch_size");
                	 suppCodeMfg   = rs.getString("supp_code__mfg");
                	 this.shelfLifeType = rs.getString("shelf_life_type");
                	 suppCode = rs.getString("supp_code");
                	 
                 }
                rs.close();
        		rs = null;
        		pstmt.close();
        		pstmt = null;
               
                //if isnull(s_updatestock.itemcode) then s_updatestock.itemcode =  ls_item_code
                if(updateStockMap.get("item_code")== null)
        		{
        			updateStockMap.put("item_code", itemCode);
        			this.itemCode = itemCode;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.mfg_date) then s_updatestock.mfg_date = ldt_mfgdt
                if(updateStockMap.get("mfg_date")== null)
        		{
        			updateStockMap.put("mfg_date", mfgDate);
        			this.mfgDate = mfgDate;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.potency_perc) then s_updatestock.potency_perc =  lc_potency_perc
                if(updateStockMap.get("potency_perc")== null)
        		{
        			updateStockMap.put("potency_perc", potencyPerc);
        			this.potencyPerc = potencyPerc;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.exp_date) then s_updatestock.exp_date = mexpdate
                if(updateStockMap.get("exp_date")== null)
        		{
        			updateStockMap.put("exp_date", expDate);
        			this.expDate = expDate;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.retest_date) then s_updatestock.retest_date =  mretest_date
               // System.out.println("@@@@ updateStockMap.get(retest_date) ["+updateStockMap.get("retest_date").toString()+"]"); // added by cpatil on 05-03-13
                //change done by kunal on 30/04/13 comment SOP bug fixing  
               if(  updateStockMap.get("retest_date")!= null  &&  updateStockMap.get("retest_date").toString() != "01/01/1990" ) 
               {
            	    sql2 = " select available from invstat where inv_stat = ( select inv_stat from location where loc_code =  ? ) ";
            	    pstmt2 = conn.prepareStatement(sql2);
             		pstmt2.setString(1,this.locationCode);
             		rs2 = pstmt2.executeQuery();
                    if(rs2.next())
                     { 	
                       available = rs2.getString("available");	
                     }
                    rs2.close();
            		rs2 = null;
            		pstmt2.close();
            		pstmt2 = null;
            	       System.out.println("@@@@ this.tranSer ["+this.tranSer+"]::::available ["+available+"]");	  
            	      if  ( "QC-ORD".equalsIgnoreCase(this.tranSer) && "Y".equalsIgnoreCase( available ) )
            	      {
            	          sql2 = " update item_lot_info set retest_date = ? where item_code = ? and lot_no = ?  " ;
            	          pstmt2=conn.prepareStatement(sql2);
            	          String retestDate_upd = updateStockMap.get("retest_date")==null?null:updateStockMap.get("retest_date").toString();
            	          if( retestDate_upd != null )
            	          {
            	           	   SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
            	               retestDate_upd2 = simpleDateFormat1.parse(retestDate_upd);
            	               System.out.println("@@@@@@@@@retestDate_upd2["+retestDate_upd2+"]");
            	               retestDate_upd3 = new Timestamp(retestDate_upd2.getTime());
             	               System.out.println("@@@@@@@@@retestDate_upd3["+retestDate_upd3+"]");
            	          }
            	          pstmt2.setTimestamp(1,retestDate_upd3);
            	          //pstmt2.setDate(1,retestDate_upd2 == null?null:new java.sql.Date(retestDate_upd2.getTime()));
            	          pstmt2.setString(2,updateStockMap.get("item_code")==null?null:updateStockMap.get("item_code").toString());
            	          pstmt2.setString(3,updateStockMap.get("lot_no")==null?null:updateStockMap.get("lot_no").toString());
            	          cnt1 = pstmt2.executeUpdate();
            	          pstmt2.close();
            	          pstmt2 = null;
            	          System.out.println("@@@@ cnt1:::"+cnt1);
            	      }
               }           // end by cpatil
               else
               {
            	if(updateStockMap.get("retest_date")== null)
        		{
        			updateStockMap.put("retest_date", retestDate);
        			this.retestDate = retestDate;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
               }
               //if isnull(s_updatestock.lotno) then s_updatestock.lotno = ls_lot_no
                if(updateStockMap.get("lotno")== null)
        		{
        			updateStockMap.put("lotno", lotNo);
        			this.lotNo = lotNo;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.grade) then s_updatestock.grade = ls_grade
                if(updateStockMap.get("grade")== null)
        		{
        			updateStockMap.put("grade", grade);
        			this.grade = grade;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.pack_code) then s_updatestock.pack_code =  ls_pack_code
                if(updateStockMap.get("pack_code")== null)
        		{
        			updateStockMap.put("pack_code", packCode);
        			this.packCode = packCode;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.sitecode_mfg) then s_updatestock.sitecode_mfg =  ls_site_code__mfg
                if(updateStockMap.get("site_code__mfg")== null)
        		{
        			updateStockMap.put("site_code__mfg", siteCodeMfg);
        			this.siteCodeMfg = siteCodeMfg;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.rate) then s_updatestock.rate = mrate
                if(updateStockMap.get("rate")== null)
        		{
        			updateStockMap.put("rate", rate);
        			this.rate = rate;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.acct_code_inv) then s_updatestock.acct_code_inv  = ls_acct_code__inv
                if(updateStockMap.get("acct_code_inv")== null)
        		{
        			updateStockMap.put("acct_code_inv", acctCodeInv);
        			this.acctCodeInv = acctCodeInv;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.cctr_code_inv) then s_updatestock.cctr_code_inv  = ls_ctr_code__inv
                if(updateStockMap.get("cctr_code_inv")== null)
        		{
        			updateStockMap.put("cctr_code_inv", cctrCodeInv);
        			this.cctrCodeInv = cctrCodeInv;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.batch_no) then s_updatestock.batch_no = ls_batch_no
                if(updateStockMap.get("batch_no")== null)
        		{
        			updateStockMap.put("batch_no", batchNo);
        			this.batchNo = batchNo;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.gross_rate) then s_updatestock.gross_rate =  ld_grossrate
                if(updateStockMap.get("gross_rate")== null)
        		{
        			updateStockMap.put("gross_rate", grossRate);
        			this.grossRate = grossrate;//added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.batch_size) then s_updatestock.batch_size =  ld_batch_size
                if(updateStockMap.get("batch_size")== null)
        		{
        			updateStockMap.put("batch_size", batchSize);
        			this.batchSize = batchSize; //added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                //if isnull(s_updatestock.supp_code__mfg) then  s_updatestock.supp_code__mfg = ls_supp_code__mfg
                if(updateStockMap.get("supp_code__mfg")== null)
        		{
        			updateStockMap.put("supp_code__mfg", suppCodeMfg);
        			this.suppCodeMfg = suppCodeMfg; //added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                if(updateStockMap.get("supp_code")== null)
        		{
        			updateStockMap.put("supp_code", suppCode);
        			this.suppCode = suppCode; //added by azhar[17-May-2017] to reinitialize class variables to values from item lot info
        		}
                
                //int mapsize = updateStockMap.size();
                
               // for(int i=0;i<mapsize;i++)
                {
                	System.out.println("@@@@@ for update map ..........");
                	System.out.println("itemCode::["+updateStockMap.get("item_code")+"]");
             	   	System.out.println("lotNo["+updateStockMap.get("lot_no") +"]");
     				System.out.println("grade["+updateStockMap.get("grade")+"]");
     				System.out.println("retestDate["+updateStockMap.get("retest_date")+"]");
     				System.out.println("expDate["+updateStockMap.get("exp_date")+"]");
     				System.out.println("mfgDate["+updateStockMap.get("mfg_date")+"]");
     				System.out.println("packCode["+updateStockMap.get("pack_code")+"]");
     				System.out.println("potencyPerc["+updateStockMap.get("potency_perc")+"]");
     				System.out.println("siteCodeMfg["+updateStockMap.get("site_code__mfg")+"]");
     				System.out.println("rate ["+updateStockMap.get("rate")+"]");
     				System.out.println("grossRate["+updateStockMap.get("gross_rate")+"]");
     				System.out.println("acctCodeInv["+updateStockMap.get("acct_code_inv")+"]");
     				System.out.println("cctrCodeInv["+updateStockMap.get("cctr_code_inv")+"]");
     				System.out.println("batchNo["+updateStockMap.get("batch_no")+"]");
     				System.out.println("batchSize["+updateStockMap.get("batch_size")+"]");
     				System.out.println("suppCodeMfg["+updateStockMap.get("supp_code__mfg")+"]");
     				System.out.println("suppCode["+updateStockMap.get("supp_code")+"]");
              /*  	
                
               
                sql = " select count(1) from stock where item_code = ? and lot_no = ?  and site_code= ? and loc_code = ? and lot_sl = ? ";
                pstmt = conn.prepareStatement(sql);
         		pstmt.setString(1,this.itemCode);
         		pstmt.setString(2,this.lotNo);
         		pstmt.setString(3,this.siteCode);
         		pstmt.setString(4,this.lotCode);
         		pstmt.setString(5,this.lotSl);
         		rs = pstmt.executeQuery();
                if(rs.next())
                 {
                	 cnt6  = rs.getInt(1);
                	 
                 }
                rs.close();
        		rs = null;
        		pstmt.close();
        		pstmt = null;
                
                if(cnt6 > 0 )
     			{
     			
     				sql = " update stock set " +
                			" mfg_date = ?,potency_perc = ?,exp_date = ?,retest_date = ?,lot_no = ?,grade = ?,pack_code = ?," +
                			" site_code__mfg = ?,rate = ?,acct_code__inv = ?," +
                            " cctr_code__inv = ? , batch_no = ? ,gross_rate = ? , " +
         					" batch_size = ? , supp_code__mfg = ?  " +
         					" where item_code = ? and lot_no = ?  and site_code= ? and loc_code = ? and lot_sl = ? ";

     				

                	pstmt=conn.prepareStatement(sql);
                	String mfgDate2 = updateStockMap.get("mfg_date")==null?null:updateStockMap.get("mfg_date").toString();
                	if( mfgDate2 != null )
                	{
                		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                        mfgDate3 = simpleDateFormat1.parse(mfgDate2);
                    }
                	pstmt.setDate(1,mfgDate3 ==null?null:new java.sql.Date(mfgDate3.getTime()));
            		System.out.println("@@@@ mfgDate3::"+mfgDate3);
                	
                	pstmt.setString(2,updateStockMap.get("potency_perc")==null?null:updateStockMap.get("potency_perc").toString());
                	
                	String expDate2 = updateStockMap.get("exp_date")==null?null:updateStockMap.get("exp_date").toString();
                	if( expDate2 != null )
                	{
                		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                        expDate3 = simpleDateFormat1.parse(expDate2);
                    }
                	pstmt.setDate(3,expDate3 ==null?null:new java.sql.Date(expDate3.getTime()));
            		System.out.println("@@@@ expDate3::"+expDate3);
            	
                	String retestDate2 = updateStockMap.get("retest_date")==null?null:updateStockMap.get("retest_date").toString();
                	if( retestDate2 != null )
                	{
                		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                        retestDate3 = simpleDateFormat1.parse(retestDate2);
                    }
                	pstmt.setDate(4,retestDate3 ==null?null:new java.sql.Date(retestDate3.getTime()));
            		System.out.println("@@@@ retestDate3::"+retestDate3);
                	
                	pstmt.setString(5,updateStockMap.get("lot_no")==null?null:updateStockMap.get("lot_no").toString());
 					pstmt.setString(6,updateStockMap.get("grade")==null?null:updateStockMap.get("grade").toString());
 					pstmt.setString(7,updateStockMap.get("pack_pode")==null?null:updateStockMap.get("pack_pode").toString());
 					pstmt.setString(8,updateStockMap.get("site_code__mfg")==null?null:updateStockMap.get("site_code__mfg").toString());
 					pstmt.setString(9,updateStockMap.get("rate")==null?null:updateStockMap.get("rate").toString());
 					pstmt.setString(10,updateStockMap.get("acct_code_inv")==null?null:updateStockMap.get("acct_code_inv").toString());
 					pstmt.setString(11,updateStockMap.get("cctr_code_inv")==null?null:updateStockMap.get("cctr_code_inv").toString());
 					pstmt.setString(12,updateStockMap.get("batch_no")==null?null:updateStockMap.get("batch_no").toString());
 					pstmt.setString(13,updateStockMap.get("gross_rate ")==null?null:updateStockMap.get("gross_rate").toString());
 					pstmt.setString(14,updateStockMap.get("batch_size")==null?null:updateStockMap.get("batch_size").toString());
 					pstmt.setString(15,updateStockMap.get("supp_code__mfg")==null?null:updateStockMap.get("supp_code__mfg").toString());
 					pstmt.setString(16,updateStockMap.get("item_code")==null?null:updateStockMap.get("item_code").toString());
 					pstmt.setString(17,updateStockMap.get("lot_no")==null?null:updateStockMap.get("lot_no").toString());
 					pstmt.setString(18,updateStockMap.get("site_code")==null?null:updateStockMap.get("site_code").toString());
 					pstmt.setString(19,updateStockMap.get("loc_code")==null?null:updateStockMap.get("loc_code").toString());
 					pstmt.setString(20,updateStockMap.get("lot_sl")==null?null:updateStockMap.get("lot_sl").toString());
 					
     					cnt1 = pstmt.executeUpdate();
    					pstmt.close();
    		     		pstmt = null;
    		     		System.out.println("@@@@ cnt1:::"+cnt1);
    					if ( cnt1 == 0)
    					{
    						 errCode = "VTUPDDET";
    						 errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
    						 return errString;
    					}
    				}  // if cnt6
    			*/
 					}
           }
           else
           {
        	    System.out.println("@@@@@ for insert..........");
        	    System.out.println("itemCode::["+updateStockMap.get("item_code")+"]");
         	    System.out.println("lotNo["+updateStockMap.get("lot_no") +"]");
 				System.out.println("grade["+updateStockMap.get("grade")+"]");
 				System.out.println("retestDate[today]"+today+"]");
 				System.out.println("expDate["+updateStockMap.get("exp_date")+"]");
 				System.out.println("mfgDate["+updateStockMap.get("mfg_date")+"]");
 				System.out.println("packCode["+updateStockMap.get("pack_code")+"]");
 				System.out.println("potencyPerc["+updateStockMap.get("potency_perc")+"]");
 				System.out.println("siteCodeMfg["+updateStockMap.get("site_code__mfg")+"]");
 				System.out.println("rate ["+updateStockMap.get("rate")+"]");
 				System.out.println("grossRate["+updateStockMap.get("gross_rate")+"]");
 				System.out.println("acctCodeInv["+updateStockMap.get("acct_code_inv")+"]");
 				System.out.println("cctrCodeInv["+updateStockMap.get("cctr_code_inv")+"]");
 				System.out.println("batchNo["+updateStockMap.get("batch_no")+"]");
 				System.out.println("batchSize["+updateStockMap.get("batch_size")+"]");
 				System.out.println("suppCodeMfg["+updateStockMap.get("supp_code__mfg")+"]");
 				System.out.println("shelfLifeType value ["+shelfLifeType+"]");
 				System.out.println("suppCode ["+suppCode+"]");
 				
        	  sql = " insert into item_lot_info (item_code,lot_no,grade,crea_date,exp_date,mfg_date,retest_date,pack_code," +
        	  		" potency_perc,site_code__mfg,rate,gross_rate,acct_code__inv," +
                    " cctr_code__inv,batch_no,batch_size,supp_code__mfg,shelf_life_type,supp_code,chg_date,chg_term,chg_user) " +
                    " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";           // added by cpatil on 07-03-13 start for change term,date,user
        	  
              pstmt=conn.prepareStatement(sql);
                
                pstmt.setString(1,updateStockMap.get("item_code")==null?null:updateStockMap.get("item_code").toString());
				pstmt.setString(2,updateStockMap.get("lot_no")==null?null:updateStockMap.get("lot_no").toString());
				pstmt.setString(3,updateStockMap.get("grade")==null?null:updateStockMap.get("grade").toString());
				pstmt.setTimestamp(4,today);
				
				String expDate2 = updateStockMap.get("exp_date")==null?null:updateStockMap.get("exp_date").toString();
            	if( expDate2 != null )
            	{
            		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                    expDate3 = simpleDateFormat1.parse(expDate2);
                }
            	pstmt.setDate(5,expDate3 ==null?null:new java.sql.Date(expDate3.getTime()));
        		System.out.println("@@@@ expDate3::"+expDate3);
				
        		String mfgDate2 = updateStockMap.get("mfg_date")==null?null:updateStockMap.get("mfg_date").toString();
            	if( mfgDate2 != null )
            	{
            		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                   mfgDate3 = simpleDateFormat1.parse(mfgDate2);
                }
            	pstmt.setDate(6,mfgDate3 ==null?null:new java.sql.Date(mfgDate3.getTime()));
        		System.out.println("@@@@ mfgDate3::"+mfgDate3);
            	
        		String retestDate2 = updateStockMap.get("retest_date")==null?null:updateStockMap.get("retest_date").toString();
            	if( retestDate2 != null )
            	{
            		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                    retestDate3 = simpleDateFormat1.parse(retestDate2);
                }
            	pstmt.setDate(7,retestDate3 ==null?null:new java.sql.Date(retestDate3.getTime()));
        		System.out.println("@@@@ retestDate3::"+retestDate3);
            	
        		pstmt.setString(8,updateStockMap.get("pack_pode")==null?null:updateStockMap.get("pack_pode").toString());
				pstmt.setString(9,updateStockMap.get("potency_perc")==null?null:updateStockMap.get("potency_perc").toString());
				pstmt.setString(10,updateStockMap.get("site_code__mfg")==null?null:updateStockMap.get("site_code__mfg").toString());
				pstmt.setString(11,updateStockMap.get("rate")==null?null:updateStockMap.get("rate").toString());
				pstmt.setString(12,updateStockMap.get("gross_rate")==null?null:updateStockMap.get("gross_rate").toString());
				pstmt.setString(13,updateStockMap.get("acct_code_inv")==null?null:updateStockMap.get("acct_code_inv").toString());
				pstmt.setString(14,updateStockMap.get("cctr_code_inv")==null?null:updateStockMap.get("cctr_code_inv").toString());
				pstmt.setString(15,updateStockMap.get("batch_no")==null?null:updateStockMap.get("batch_no").toString());
				pstmt.setString(16,updateStockMap.get("batch_size")==null?null:updateStockMap.get("batch_size").toString());
				pstmt.setString(17,updateStockMap.get("supp_code__mfg")==null?null:updateStockMap.get("supp_code__mfg").toString());
				pstmt.setString(18,this.shelfLifeType);
				pstmt.setString(19,updateStockMap.get("supp_code")==null?null:updateStockMap.get("supp_code").toString());
				
				pstmt.setTimestamp(20,today);
				//pstmt.setDate(20,today);
				pstmt.setString(21,chgTerm);
				pstmt.setString(22,chgUser);
				
				cnt1 = pstmt.executeUpdate();
				pstmt.close();
		     	pstmt = null;
				//	if ( cnt1 == 0)
				//	{
				//		 errCode = "VINSERT5";
				//		 errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
				//		 return errString;
				//	}
			
           }
			//} //add
		 } 
        }
        catch(Exception e)
        {
            System.out.println("Exception "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }
        return updateStockMap;
	}

/*	
	private String updateStockAmd(HashMap updateStockMap ,Connection conn) throws ITMException, Exception
	{
		String stkOpt="", sql = "",errCode="",errString="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		java.util.Date retestDate3=null,expDate3=null,mfgDate3=null;
		int cnt1=0,cnt=0;
		
		if ( updateStockMap.get("item_code") != null )
		{
			this.itemCode = updateStockMap.get("item_code").toString();
		}
		if ( updateStockMap.get("lot_no") != null )
		{
			this.lotNo = updateStockMap.get("lot_no").toString();
		}
		if ( updateStockMap.get("lot_no") != null )
		{
			this.siteCode = updateStockMap.get("site_code").toString();
		}
		if ( updateStockMap.get("tran_ser") != null )
		{
			this.tranSer = updateStockMap.get("tran_ser").toString();
		}
		
		stkOpt = checkStkOpt(this.itemCode,this.siteCode,conn);
		System.out.println("@@@@@ stkOpt[gfChkStkOpt] :::- ["+stkOpt+"]");
		
		if( "2".equalsIgnoreCase(stkOpt) )
		{	
			System.out.println("tran_ser::::::::["+this.tranSer+"]");
			if( "STKAMD".equalsIgnoreCase( this.tranSer ))
			{
				sql = " select count(1) from item_lot_info where item_code = ? and lot_no = ?  ";
                pstmt = conn.prepareStatement(sql);
         		pstmt.setString(1,this.itemCode);
         		pstmt.setString(2,this.lotNo);
         		rs = pstmt.executeQuery();
                if(rs.next())
                 {
                	 cnt  = rs.getInt(1);
                 }
                rs.close();
        		rs = null;
        		pstmt.close();
        		pstmt = null;
				
        		if( cnt > 0)
				{
        			sql = " update item_lot_info set " +
					      " mfg_date = ?, exp_date = ? , site_code__mfg = ?, retest_date = ? , potency_perc = ?, " +
            		      " supp_code__mfg = ?  where item_code = ? and lot_no = ? ";

				pstmt=conn.prepareStatement(sql);
            	
				String mfgDate2 = updateStockMap.get("mfg_date")==null?null:updateStockMap.get("mfg_date").toString();
            	if( mfgDate2 != null )
            	{
            		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                    mfgDate3 = simpleDateFormat1.parse(mfgDate2);
                }
            	pstmt.setDate(1,mfgDate3 ==null?null:new java.sql.Date(mfgDate3.getTime()));
        		System.out.println("@@@@ mfgDate3---::"+mfgDate3);
            	
        		String expDate2 = updateStockMap.get("exp_date")==null?null:updateStockMap.get("exp_date").toString();
            	if( expDate2 != null )
            	{
            		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                    expDate3 = simpleDateFormat1.parse(expDate2);
                }
            	pstmt.setDate(2,expDate3 ==null?null:new java.sql.Date(expDate3.getTime()));
        		System.out.println("@@@@ expDate3------::"+expDate3);
        	          		
        		pstmt.setString(3,updateStockMap.get("site_code__mfg")==null?null:updateStockMap.get("site_code__mfg").toString());
        		
            	String retestDate2 = updateStockMap.get("retest_date")==null?null:updateStockMap.get("retest_date").toString();
            	if( retestDate2 != null )
            	{
            		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
                    retestDate3 = simpleDateFormat1.parse(retestDate2);
                }
            	pstmt.setDate(4,retestDate3 ==null?null:new java.sql.Date(retestDate3.getTime()));
        		System.out.println("@@@@ retestDate3::"+retestDate3);
            	
        		pstmt.setString(5,updateStockMap.get("potency_perc")==null?null:updateStockMap.get("potency_perc").toString());            		
        		pstmt.setString(6,updateStockMap.get("supp_code__mfg")==null?null:updateStockMap.get("supp_code__mfg").toString());
				pstmt.setString(7,updateStockMap.get("item_code")==null?null:updateStockMap.get("item_code").toString());
				pstmt.setString(8,updateStockMap.get("lot_no")==null?null:updateStockMap.get("lot_no").toString());
					
 				cnt1 = pstmt.executeUpdate();
				pstmt.close();
		     	pstmt = null;
		     	System.out.println("@@@@ cnt1-----:::"+cnt1);
					
		     		if ( cnt1 == 0)
					{
						 errCode = "VTUPDDET";
						 errString = itmDBAccessEJB.getErrorString("",errString,"","",conn);
						 return errString;
					}
			    }
        	}
	    }
		return errString;
	}
	*/ //for amm end
	
	// added by cpatil end
	
	
}//END OF CLASS
