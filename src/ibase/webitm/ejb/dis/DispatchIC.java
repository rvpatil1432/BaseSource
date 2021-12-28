package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;
import ibase.utility.BaseLogger;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Stateless
public class DispatchIC extends ValidatorEJB implements DispatchICLocal,DispatchIcRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	DistCommon distCommon = new DistCommon();		
	FinCommon finCommon = new FinCommon();

	// method for validation
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		//System.out.println("wfValdata() called for DespatchManualIC");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try 
		{
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				//System.out.println("xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				//System.out.println("xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				//System.out.println("xmlString2["+xmlString2+"]");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		double quantityReal=0;
		String unit;
		String unitStd;
		String itemCodeOrd;
		String quantity;
		String convQtyStduom;
		int cnt = 0;
		int ctr = 0;
		int childNodeListLength=0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		ArrayList quantityRealList=new ArrayList();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer( "<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		int currentFormNo = 0;
		String sordNo="",despId="",despDateStr="",confirmed="",allocFlag="",status="",siteCode="",lrDateStr="";
		String custCode="",tranCode="",stanCode="",stanCodeInit="",stopBusiness="",itemSer="",holdShipment="";
		String benefitTypeDesp="",benefitType="",shipmentId="",rdPermitNo="",tranId="",custCodeDlv="",lrNo="",stateCodeTo="";
		String stateTo="",siteCodeFr="",stateFrom="",permitReqd="",gpDateStr="";
		Timestamp despDate=null,orderDate=null,lrDate=null,expiryDate=null,gpDate=null;
		boolean isExist = false,lb_ord_flag=false;


		String modesp="";
		String mVal="",msite="",mVal2="",mVal3="",mstatus_cd="",mstatus="",ls_hdr_sord="",msaleord="",mval1="",mexplev="",mVal1="";
		String ls_line_no__sord="",ls_itemcode="",ls_sitecode="",ls_sordatt_no="",ld_desp_date_str="",ls_sord_no="",ls_item_code="";
		String mSingleSer="",ls_site_code="",ls_unit="",mdate1Str="",itmSer="",ls_benefit_type="",mlotno="",ls_hdr_val="",ls_dtl_val="";
		String ls_nxt_yr="",ls_available_yn="",ls_cust_code="",ls_cust_code__dlv="",ls_channel_partner="",ls_available="",ls_item_ser="";
		Timestamp ld_desp_date=null,ld_sch_date=null,mdate1=null,ldt_nxt_yr=null,ld_desp_dt=null;		
		int ll_count=0,li_mth=0,ll_cnt=0,mcnt=0,ll_min_shelf_life=0,ll_max_shelf_life=0,li_cnt=0;
		double mNum=0,lc_quantity__stduom=0,lc_qty=0;
		String mlineno="",mitemcode="",mlotsl="",mloccode="",mitem_code__ord="",Ls_DespId="",ld_trandate_str="";
		double lc_sord_qty = 0,lc_tot_qty = 0,mNum1 = 0,mNum2 = 0,lc_stkqty=0,lc_PendingQty=0,lc_overshipperc=0,lc_ceilqty=0,modespperc=0;//mstkqty = 0,	
		String ls_iss_criteria="",ls_item_code__parent="",ls_nature="",mitemord="",ls_stk_opt="";	
		double netQuantity = 0 ,curNetQuentity =0; //PC
		Timestamp ld_rest_upto=null,ld_trandate=null,ld_exp_date=null,ld_chk_date=null;
		double mstkQty=0,lc_conv=0,lc_int_qty=0,mmodqty=0,lc_free_value=0,lc_prv_bonus_value=0,lc_prv_sample_value=0;
		String ls_unit_desp="",ls_unit_std="",ls_order_type="",ls_track_shelf_life="";
		String ls_state_code="",ls_count_code="",ls_pricelist="",ls_scheme_code="",ls_scheme_flag="",ls_cur_lineno="";
		Timestamp ldt_order_date=null,ld_mfg_date=null,ld_chk_date1=null,ld_chk_date2=null,ld_alloc_date=null;
		double lc_tot_charge_qty =0,lc_tot_free_qty=0,lc_tot_bonus_qty=0,lc_tot_sample_qty=0,lc_rate=0,lc_prv_charge_qty = 0,lc_prv_free_qty = 0,lc_prv_bonus_qty = 0,lc_prv_sample_qty = 0;
		double lc_tot_charge_value =0,lc_tot_free_value=0,lc_tot_bonus_value=0,lc_tot_sample_value=0,mvalue=0,lc_charge_qty=0;
		double lc_batch_size=0,lc_batqty=0,lc_qtyper=0,lc_app_min_qty=0,lc_app_max_qty=0,lc_app_min_value=0,lc_app_max_value=0,lc_batvalue=0,lc_valueper=0,lc_minvalue=0,lc_prv_charge_value=0,lc_charge_value=0;
		String ls_round = "",ls_site_mfg="",ls_cust_item="",ls_line_type="",ldt_order_date_str="";
		double lc_free_qty=0,lc_prv_free_value=0,ld_roundto=0,lc_mbatch_size=0,lc_qty_real=0,lc_desp_qty=0,lc_rate_clg=0;
		String priceListDisc="",priceList="",lineType="",priceListType="";//added by nandkumar gadkari on 05/08/19 
		double rate=0;//added by nandkumar gadkari on 05/08/19 

		try {

			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDate);

			java.util.Date today = new java.util.Date();
			Timestamp sysDateTm =  new java.sql.Timestamp(today.getTime());

			//System.out.println("@@@@@@@@ wfvaldata called sysDateTm["+sysDateTm+"]");

			ld_alloc_date = sysDateTm;

			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection() ;
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					//System.out.println("@@@@@@@@@ childNodeName["+childNodeName+"]..........");
					if (childNodeName.equalsIgnoreCase("desp_id")) 
					{
						sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
						despId = checkNull(genericUtility.getColumnValue("desp_id", dom));

						if( isnull(despId) || despId.trim().length() == 0 )
						{
							despId = "@@@@@@@@@@";
						}
						sql = " Select count(1) " +
								"	From	 despatch" +
								" Where sord_no   = ? and desp_id <> ? and confirmed = 'N' ";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sordNo);
						pstmt.setString(2, despId);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ( cnt > 0)
						{
							errCode = "VTINVDI";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						/// start for duplicate despatch id in manual......
						despId = checkNull(genericUtility.getColumnValue("desp_id", dom));
						String keyFlag = "";
						sql = "select key_flag from transetup where tran_window='w_despatch' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							keyFlag = rs.getString("key_flag");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						//System.out.println("Key Flag>>>>>>>>>[" + keyFlag+"]editFlag["+editFlag+"] despId[" + despId + "]");
						if (keyFlag.equalsIgnoreCase("M")) 
						{
							if (despId == null || despId.length() == 0) 
							{
								errList.add("VTDESIDNUL");
								errFields.add(childNodeName.toLowerCase());
							}
							//System.out.println("Edit Flag>>>>" + editFlag);
							if ("A".equalsIgnoreCase(editFlag))
							{
								cnt=0;
								sql = " SELECT COUNT(1) FROM despatch "
										+ "WHERE desp_id = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, despId);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt > 0)
								{
									errList.add("VTDESIDINV");
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						//end

					}
					//changed by Nasruddin Start 09/01/17 start
					else if (childNodeName.equalsIgnoreCase("desp_id")) 
					{
						custCodeDlv = checkNull(genericUtility.getColumnValue("count_code__dlv", dom));
						isExist = isExist("country", "count_code", custCodeDlv, conn);
						if(!isExist)
						{
							errCode = "VTCONTCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					//changed by Nasruddin Start 09/01/17 End
					else if (childNodeName.equalsIgnoreCase("sord_no")) 
					{
						sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						despDateStr = checkNull(genericUtility.getColumnValue("desp_date", dom));
						if( despDateStr != null && despDateStr.trim().length() > 0 )
						{
							despDate = Timestamp.valueOf(genericUtility.getValidDateString(despDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						cnt=0;
						sql = " select	alloc_flag, (case when confirmed is null then 'N' else confirmed end), " +
								"	(case when status is null then 'N' else status end),order_date " +
								" from  sorder 	where		sale_order =  ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt++;
							allocFlag = checkNull(rs.getString(1));
							confirmed = checkNull(rs.getString(2));
							status = checkNull(rs.getString(3));
							orderDate = rs.getTimestamp(4);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0 || "N".equalsIgnoreCase(status) || "N".equalsIgnoreCase(confirmed))
						{
							errCode = "VTSORD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if( "C".equalsIgnoreCase(status) ||  "X".equalsIgnoreCase(status) ||  "H".equalsIgnoreCase(status) )				
						{
							errCode = "VTSORDCX";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(despDate != null && despDate.before(orderDate) ) 
						{
							errCode = "VTSORD6";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{	
							isExist = isExist("sorditem", "sale_order", sordNo, conn);
							if(!isExist)
							{
								errCode = "VTSORD8";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
					}
					else if (childNodeName.equalsIgnoreCase("desp_date")) 
					{
						despDateStr = checkNull(genericUtility.getColumnValue("desp_date", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if (despDateStr == null || despDateStr.trim().length() == 0) 
						{
							errCode = "VMDATEDMI7";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							despDate = Timestamp.valueOf(genericUtility.getValidDateString(despDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							//Changes and Commented By Ajay on 20-12-2017 :START
							//errCode = nfCheckPeriod("SAL", despDate,siteCode);
							errCode=finCommon.nfCheckPeriod("SAL",despDate,siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017 :END
							//System.out.println("@@@@ despDate["+despDate+"]errCode["+errCode+"]");
							if (errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("lr_date")) 
					{
						lrDateStr = checkNull(genericUtility.getColumnValue("lr_date", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if (lrDateStr != null && lrDateStr.length() > 0) 
						{
							lrDate = Timestamp.valueOf(genericUtility.getValidDateString(lrDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							//errCode = this.nfCheckPeriod("SAL", lrDate,siteCode);
							errCode = finCommon.nfCheckPeriod("SAL", lrDate,siteCode,conn);
							//System.out.println("@@@@ lrDate["+lrDate+"]errCode["+errCode+"]");
							if (errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("site_code")) 
					{
						sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						cnt=0;
						sql = " select count(1) from sorddet "+
								" where sale_order = ? and site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sordNo);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if( cnt == 0 )
						{
							errCode = "VTDESP3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
					}
					else if (childNodeName.equalsIgnoreCase("tran_code")) 
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						if( tranCode.length() > 0 )
						{
							isExist = isExist("transporter", "tran_code", tranCode, conn);
							if(!isExist)
							{
								errCode = "VMTRAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//changed by nasruddin 9-1-17 start
						//Commented by sarita as tran_code is not mandatory in despatch transporter[start]
						/*else
						{
							errCode = "VTTRANCODE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
						//Commented by sarita as tran_code is not mandatory in despatch transporter[end]
						//changed by nasruddin 9-1-17 end

					}
					else if (childNodeName.equalsIgnoreCase("stan_code")) 
					{
						stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
						isExist = isExist("station", "stan_code", stanCode, conn);
						if(!isExist)
						{
							errCode = "VTSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					else if (childNodeName.equalsIgnoreCase("stan_code__init")) 
					{
						stanCodeInit = checkNull(genericUtility.getColumnValue("stan_code__init", dom));
						if( stanCodeInit.trim().length() > 0 )
						{
							isExist = isExist("station", "stan_code", stanCodeInit, conn);
							if(!isExist)
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("cust_code") || childNodeName.equalsIgnoreCase("cust_code__dlv")) 
					{
						custCode = checkNull(genericUtility.getColumnValue(childNodeName, dom)).trim();
						//System.out.println("@@@@@@ custCode["+custCode+"]childNodeName["+childNodeName+"]");
						stopBusiness = getValue("stop_business", "customer", "cust_code", custCode, conn);
						if("Y".equalsIgnoreCase(stopBusiness))
						{
							errCode = "VTICC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom)).trim();
						itemSer = checkNull(getValue("item_ser", "sorder", "sale_order", sordNo, conn)).trim();
						cnt=0;
						sql = " select (case when hold_shipment is null then 'N' else hold_shipment end) "+
								" from customer_series "+
								" where cust_code = ? "+
								" and item_ser = ? ";				 
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, itemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt++;
							holdShipment = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0 )
						{	 
							sql = " select (case when hold_shipment is null then 'N' else hold_shipment end) " +
									"	from customer " +
									" where cust_code = ? ";				 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								holdShipment = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if("Y".equalsIgnoreCase(holdShipment))
						{
							errCode = "VTDESPH";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("benefit_type")) 
					{
						benefitTypeDesp = checkNull(genericUtility.getColumnValue("benefit_type", dom));
						despId = checkNull(genericUtility.getColumnValue("desp_id", dom));
						cnt = 0;
						sql = "select count(1) from benefit_trace where ref_ser = 'S-DSP' and ref_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, despId);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if( cnt > 0 )
						{	
							sql = " select distinct benefit_type from benefit_trace " +
									" where ref_ser = 'S-DSP'  and ref_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, despId);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								benefitType = checkNull(rs.getString("benefit_type"));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//System.out.println("@@@@@@ benefitTypeDesp["+benefitTypeDesp+"]benefitType["+benefitType+"]");
							if(! benefitTypeDesp.trim().equalsIgnoreCase(benefitType.trim()))
							{
								errCode = "VTCAVBT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("shipment_id")) 
					{
						shipmentId = checkNull(genericUtility.getColumnValue("shipment_id", dom));
						if( shipmentId.trim().length() > 0 )
						{
							cnt=0;
							sql = " select count(1) " +
									" from   shipment where  shipment_id = ? and confirmed = 'N' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, shipmentId);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if( cnt == 0 )
							{
								errCode = "VTSHPID";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("rd_permit_no")) 
					{
						rdPermitNo = checkNull(genericUtility.getColumnValue("rd_permit_no", dom));
						
						if (  rdPermitNo.trim().length() > 0) 
						{
							isExist = isExist("ROADPERMIT", "RD_PERMIT_NO", rdPermitNo, conn);
							if(!isExist)
							{
								errCode = "VMRDPNINV2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
								siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
								lrNo = checkNull(genericUtility.getColumnValue("lr_no", dom));
								despId = checkNull(genericUtility.getColumnValue("desp_id", dom));

								//ldt_today = datetime(today(),time("00:00:00"))
								if( isnull(tranId))
								{
									tranId = "@@@@@@@@@@";
								}

								stateFrom = getValue("state_code", "site", "site_code", siteCode, conn);
								if ( lenTrim(stateFrom) > 0 )
								{	
									stateTo = getValue("state_code", "customer", "cust_code", custCodeDlv, conn);
									if ( lenTrim(stateTo) > 0 )
									{
										if(! stateFrom.trim().equalsIgnoreCase(stateTo))
										{	
											permitReqd = getValue("rd_permit_reqd", "state", "state_code", stateTo, conn);

											if("Y".equalsIgnoreCase(permitReqd))
											{	//if len(trim(ls_rdpermit_no)) < 1 then
												if( isnull(rdPermitNo ) || lenTrim(rdPermitNo) == 0 )
												{
													errCode = "VTRDPT";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
												else
												{
													cnt=0;
													sql = " select  expiry_date, status, site_code__fr, state_code__to "+ 
															" from roadpermit "+ 
															" where rd_permit_no = ? ";
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, rdPermitNo);
													rs = pstmt.executeQuery();
													if (rs.next()) 
													{
														cnt++;
														expiryDate = rs.getTimestamp("expiry_date");
														status = checkNull(rs.getString("status"));
														siteCodeFr = checkNull(rs.getString("site_code__fr"));
														stateCodeTo = checkNull(rs.getString("state_code__to"));
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;

													if( cnt == 0 )
													{
														errCode = "VTRDPT1";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
													else if(! siteCode.trim().equalsIgnoreCase(siteCodeFr.trim()))
													{
														errCode = "VTRDPT3";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
													else if(! stateTo.trim().equalsIgnoreCase(stateCodeTo.trim()))
													{
														errCode = "VTRDPT4";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
													else if( expiryDate.before(sysDateTm) )
													{
														errCode = "VTRDPT5";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
													else if("O".equalsIgnoreCase(status)) 
													{
														errCode = "VTRDPT6";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
													else
													{	// check for lr_no other than current lr_no

														sql = " select sum(count) " +
																" from ( select count(1) as count from distord_iss " +
																" where lr_no <> ? and rd_permit_no = ?  " +
																" union all  " +
																" select count(1) as count from consume_iss " +
																" where lr_no <> ? and rd_permit_no = ? " +
																" union all  " +
																" select count(1) as count from despatch " +
																" where desp_id <> ? 	and lr_no <> ?  " +
																" and rd_permit_no = ? ) ";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, lrNo);
														pstmt.setString(2, rdPermitNo );
														pstmt.setString(3, lrNo);
														pstmt.setString(4, rdPermitNo );
														pstmt.setString(5, tranId);
														pstmt.setString(6, lrNo);
														pstmt.setString(7, rdPermitNo );
														rs = pstmt.executeQuery();
														if (rs.next()) 
														{
															cnt++;
															expiryDate = rs.getTimestamp("expiry_date");
															status = checkNull(rs.getString("status"));
															siteCodeFr = checkNull(rs.getString("site_code__fr"));
															stateCodeTo = checkNull(rs.getString("state_code__to"));
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;

														if( cnt > 0 )
														{	
															errCode = "VTRDPT7";
															errList.add(errCode);
															errFields.add(childNodeName.toLowerCase());
														}//end if
													}//end if
												}//end if
											}//			end if	
										}//				end if
									}				
									else if( lenTrim(rdPermitNo) > 0 )
									{	
										errCode = "VTRDPT0";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									} //				end if
								}//end if
							}
						}//end if	
						//}//end if
					}// end wf 

					//added from UT
					else if (childNodeName.equalsIgnoreCase("state_code__dlv")) 
					{
						String stateCodeDlv = checkNull(genericUtility.getColumnValue("state_code__dlv", dom));
						if( stateCodeDlv.trim().length() > 0 )
						{
							isExist = isExist("state", "state_code", stateCodeDlv, conn);
							if(!isExist)
							{
								errCode = "VESTATCD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("unit__ship")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("unit__ship", dom));
						if( mVal.trim().length() > 0 )
						{					
							isExist = isExist("uom", "unit", mVal, conn);           
							if(! isExist)
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("gp_date")) 
					{
						gpDateStr = checkNull(genericUtility.getColumnValue("gp_date", dom));
						despDateStr = checkNull(genericUtility.getColumnValue("desp_date", dom));
						if (gpDateStr != null && gpDateStr.length() > 0 && despDateStr != null && despDateStr.length() > 0) 
						{
							gpDate = Timestamp.valueOf(genericUtility.getValidDateString(gpDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							despDate = Timestamp.valueOf(genericUtility.getValidDateString(despDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							//System.out.println("@@@@ despDate["+despDate+"]gpDate["+gpDate+"]");
							if ( gpDate.before(despDate))
							{
								errCode = "VMGPDATEIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("dist_route")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("dist_route", dom));
						if( mVal.trim().length() > 0 )
						{					
							isExist = isExist("distroute", "dist_route", mVal, conn);           
							if(! isExist)   
							{
								errCode = "VTDISTRT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("curr_code")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("curr_code", dom));
						if( mVal.trim().length() > 0 )
						{					
							isExist = isExist("currency", "curr_code", mVal, conn);           
							if(! isExist)   
							{
								errCode = "INVCURRCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("trans_mode")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("trans_mode", dom));
						if( mVal.length() == 0 )
						{					
								errCode = "VMTRMODENU";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
						}
					}
					else if (childNodeName.equalsIgnoreCase("stan_code__dest")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("stan_code__dest", dom));
						if( mVal.trim().length() > 0 )
						{					
							isExist = isExist("station", "stan_code", mVal, conn);           
							if(! isExist)   
							{
								errCode = "INVSTNDEST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					
					

				} // end for
				break; // case 1 end

			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				//System.out.println("@@@@@@@@@@@@childNodeListLength["+ childNodeListLength + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("sord_no")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("sord_no", dom));
						msite = checkNull(genericUtility.getColumnValue("site_code", dom1));

						cnt=0;
						sql = " select alloc_flag, (case when confirmed is null then 'N' else confirmed end), " +
								"	(case when status is null then 'N' else status end) " +
								"	from   sorder where  sale_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mVal);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt++;
							mVal2 = checkNull(rs.getString(1));
							mVal3 = checkNull(rs.getString(2));
							mstatus_cd = checkNull(rs.getString(3));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 || "N".equalsIgnoreCase(mVal3))	
						{
							errCode = "VTSORD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if("C".equalsIgnoreCase(mstatus_cd) || "X".equalsIgnoreCase(mstatus) )
						{
							errCode = "VTSORDCX";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{	
							isExist = isExist("sorditem", "sale_order", mVal, conn);

							if(!isExist)
							{	
								errCode = "VTSORD8";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{	
								ls_hdr_sord = checkNull(genericUtility.getColumnValue("sord_no", dom1));
								//Code is missing manoj  ????cpatil
								ls_hdr_val  = checkNull( gbf_comp_sord(ls_hdr_sord,dom,conn));
								ls_dtl_val  = checkNull( gbf_comp_sord(mVal,dom,conn));

								if(! ls_hdr_val.equalsIgnoreCase(ls_dtl_val))
								{
									errCode = "VTSORD5";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//end if
							}//end if
						}//end if
						//if( isnull(errCode) || lenTrim(errCode) == 0 ) 				
						//{	
							if( lenTrim(mVal ) > 0 )
							{	

								ls_hdr_sord = checkNull(genericUtility.getColumnValue("sord_no", dom1));
								if(! mVal.equalsIgnoreCase(ls_hdr_sord))
								{
									mVal2 = getValue("cust_code", "sorder", "sale_order", mVal, conn);
									mVal3 = getValue("cust_code", "sorder", "sale_order", ls_hdr_sord, conn);

									if(! mVal2.equalsIgnoreCase(mVal3))
									{
										errCode = "VTSORDPT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());		
									}
								}//end if
							}//end if
						//}//end if
					}
					else if (childNodeName.equalsIgnoreCase("line_no__sord")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

						mVal = "   "+mVal;
						mVal = mVal.substring(mVal.length()-3,mVal.length());

						if( mVal == null || mVal.trim().length() == 0 )
						{
							errCode = "VMEMTLINO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());		
						}

						msaleord = checkNull(genericUtility.getColumnValue("sord_no", dom));
						msite = checkNull(genericUtility.getColumnValue("site_code", dom1));// Change dom to dom1  by NANDKUMAR GADKARI ON 13/07/18 
						mval1 = checkNull(genericUtility.getColumnValue("item_code", dom));
						mexplev = checkNull(genericUtility.getColumnValue("exp_lev", dom));
						//ls_line_no__sord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

						//ls_line_no__sord = "   "+ls_line_no__sord;
						//ls_line_no__sord = ls_line_no__sord.substring(ls_line_no__sord.length()-3,ls_line_no__sord.length());
						//Changes in sql by NANDKUMAR GADKARI ON 13/07/18 -------start--------
						/*sql = " select item_code, site_code " +
								" from sorditem where  sale_order = ? " +
								" and line_no = ? and site_code = ? and exp_lev = ? ";*/ 
						sql = " select item_code, site_code " +
								" from sorditem where  sale_order = ? " +
								" and line_no = ?  and exp_lev = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msaleord);
						pstmt.setString(2, mVal);
						//pstmt.setString(3, msite); 
						pstmt.setString(3, mexplev);
						//Changes in sql by NANDKUMAR GADKARI ON 13/07/18 -------end--------
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ls_itemcode = checkNull(rs.getString(1));
							ls_sitecode = checkNull(rs.getString(2));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;



						if( !msite.trim().equalsIgnoreCase(ls_sitecode.trim()) )
						{
							errCode = "VTDESP4";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else if( !mval1.trim().equalsIgnoreCase(ls_itemcode.trim()) )
						{
							ls_sordatt_no = getValue("sordatt_no", "sorder", "sale_order", msaleord, conn);
							if( isnull(ls_sordatt_no) || lenTrim(ls_sordatt_no) == 0 ) 
							{
								errCode = "VTDSPITEM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	

							}
						}//								end if
						sql = " select count (1)  from sorditem " +
								" where sale_order = ?  and line_no = ? ";			
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msaleord);
						pstmt.setString(2, mVal);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if( cnt == 0 ) 
						{
							errCode = "VTLINEXT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						//Checking For Schedule Date.
						ld_desp_date_str = checkNull(genericUtility.getColumnValue("desp_date", dom1));
						ls_sord_no = checkNull(genericUtility.getColumnValue("sord_no", dom));
						ls_line_no__sord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

						ls_line_no__sord = "   "+ls_line_no__sord;
						ls_line_no__sord = ls_line_no__sord.substring(ls_line_no__sord.length()-3,ls_line_no__sord.length());

						ls_item_code = checkNull(genericUtility.getColumnValue("item_code", dom));

						sql = " select dsp_date " +
								"	from	 sorddet where  sale_order = ? " +
								"	and    line_no = ?  and    item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_sord_no);
						pstmt.setString(2, ls_line_no__sord);
						pstmt.setString(3, ls_item_code);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ld_sch_date = rs.getTimestamp(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( ld_desp_date_str != null &&   lenTrim(ld_desp_date_str) > 0 )
						{
							ld_desp_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_desp_date_str,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}

						if( ld_sch_date != null && ld_sch_date.after(ld_desp_date))
						{
							errCode = "VTSCH3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}

					else if (childNodeName.equalsIgnoreCase("item_code")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("item_code", dom));
						isExist = isExist("item", "item_code", mVal, conn);

						if(! isExist ) 
						{
							errCode = "VTITEM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						/* Not Required manoj
						else
						{	
							mVal1 = getValue("item_ser", "item", "item_code", mVal, conn);
							if("Y".equalsIgnoreCase(mSingleSer))
							{
								itmSer = checkNull(genericUtility.getColumnValue("Item_ser", dom1));
								if(! itmSer.equalsIgnoreCase(mVal1))
								{
									errCode = "VTITEM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}//end if
							}//end if
						}//end if
						 */
						//validate against Benefit Type.			
						ls_benefit_type = checkNull(genericUtility.getColumnValue("benefit_type", dom1)).trim();
						mdate1Str = checkNull(genericUtility.getColumnValue("desp_date", dom1));
						ls_site_code = checkNull(genericUtility.getColumnValue("site_code", dom1));
						ls_item_code = getValue("item_parnt", "item", "item_code", mVal, conn);
						ls_unit = checkNull(genericUtility.getColumnValue("unit", dom));

						if( mdate1Str != null && lenTrim(mdate1Str) > 0 )
						{
							mdate1 = Timestamp.valueOf(genericUtility.getValidDateString(mdate1Str,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						//System.out.println("@@@@ ls_benefit_type["+ls_benefit_type+"]");
						if("AL".equalsIgnoreCase(ls_benefit_type))
						{	
							sql = " select count(1) " +
									"	from   adv_licence al, adv_licence_exp ale " +
									"	where  al.tran_id 	= ale.tran_id " +
									"	and	 al.site_code	=  ? " +
									"	and	 al.eff_from 	<= ? " +
									"	and	( " +
									"	(al.valid_upto		>= ? and al.valid_upto is not null) " +
									"	OR  " +
									"	(al.ext_valid1		>= ? and al.ext_valid1 is not null) " +
									"   OR  " +
									"   (al.ext_valid2		>= ? and al.ext_valid2 is not null) " +
									"	OR " +
									"   (al.ext_valid3		>= ? and al.ext_valid3 is not null) " +
									"	)  " +
									"	and	ale.item_code  in  ( select item_code from item where item_parnt = ? ) " +
									"   and	ale.unit = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_site_code);
							pstmt.setTimestamp(2, mdate1);
							pstmt.setTimestamp(3, mdate1);
							pstmt.setTimestamp(4, mdate1);
							pstmt.setTimestamp(5, mdate1);
							pstmt.setTimestamp(6, mdate1);
							pstmt.setString(7, ls_item_code);
							pstmt.setString(8, ls_unit);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								ll_count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else if("DR".equalsIgnoreCase(ls_benefit_type))
						{
							//Drawback.
						}
						else if("DP".equalsIgnoreCase(ls_benefit_type))
						{		
							//DEPB.
							sql = " select count(1) " +
									"	from	depb_rate " +
									"	where	item_code__depb 	in " +
									" ( select item_code from item where item_parnt = ? ) " +
									" and	eff_from		<= ?  and	valid_upto 	>= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_item_code);
							pstmt.setTimestamp(2, mdate1);
							pstmt.setTimestamp(3, mdate1);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								ll_count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}



						if("AL".equalsIgnoreCase(ls_benefit_type) && ll_count == 0 )
						{
							/*errcode = "VNOAL" + "~t" + "Site: " + ls_site_code + " Transaction Date:" + string(mdate1) + " Item Code: " +& 
									ls_item_code + " Unit:" + ls_unit + "~r" + "Check Eff. From , Valid Upto, Extended Dates for Advance Licence for item"
							 */		
							errCode = "VNOAL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());			

						}
						else if("DP".equalsIgnoreCase(ls_benefit_type) && ll_count == 0 )
						{
							/*errcode = "VNODEPB" + "~t" + " Transaction Date: " + string(mdate1) +&
									" Item: " + ls_item_code */

							errCode = "VNODEPB";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());		
						}

						//To restrict if cenvat entry exists in next yr. 
						cnt = 0;

						msite = checkNull(genericUtility.getColumnValue("site_code", dom1));
						mVal = checkNull(genericUtility.getColumnValue("item_code", dom));
						ld_desp_date_str = checkNull(genericUtility.getColumnValue("desp_date", dom1));
						mlotno = checkNull(genericUtility.getColumnValue("lot_no", dom));

						if( ld_desp_date_str != null && lenTrim(ld_desp_date_str) > 0 )
						{
							ld_desp_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_desp_date_str,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}


						long timestamp = ld_desp_date.getTime();
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(timestamp);

						//System.out.println("@@@@@@@ ld_desp_date["+ld_desp_date+"]");
						// 02/dec-16 manoharan avoid deprecated
						//li_mth = ld_desp_date.getMonth();
						li_mth = cal.get(Calendar.MONTH);
						
						//System.out.println("@@@@@@@ li_mth["+li_mth+"]");

						String year = ""+cal.get(Calendar.YEAR);
						year = year.substring(2,4);
						if( li_mth >=1 && li_mth <=3 )
						{
							//ls_nxt_yr = '01-04-' + string(year(date(ld_desp_date)))	;				
							//ls_nxt_yr = "01-04-" + year	;				
							ls_nxt_yr = "01/04/" + year	;				
						}
						else
						{
							//ls_nxt_yr = "01-04-" + (Integer.parseInt(year)+1);
							ls_nxt_yr = "01/04/" + (Integer.parseInt(year)+1);
						}
						//System.out.println("@@@@@@@ ls_nxt_yr["+ls_nxt_yr+"]");
						/*
						//System.out.println("@@@@@@@ ld_desp_date["+ld_desp_date+"]");
						//UtilMethods utilMethods = new UtilMethods();
						//li_mth 	= Month(date(ld_desp_date))
						li_mth = ld_desp_date.getMonth();

						//System.out.println("@@@@@@@ li_mth["+li_mth+"]ld_desp_date.getYear()["+ld_desp_date.getYear()+"]");

						if( li_mth >=1 && li_mth <=3 )
						{
							//ls_nxt_yr = '01-04-' + string(year(date(ld_desp_date)))	;				
							ls_nxt_yr = "01-04-" + ld_desp_date.getYear()	;				
						}
						else
						{
							ls_nxt_yr = "01-04-" + (Integer.parseInt(""+ld_desp_date.getYear())+1);
						}
						//System.out.println("@@@@@@@ ls_nxt_yr["+ls_nxt_yr+"]");
						 */

						//ldt_nxt_yr = Datetime(Date(ls_nxt_yr));

						if( ls_nxt_yr != null && lenTrim(ls_nxt_yr) > 0 )
						{
							ldt_nxt_yr = Timestamp.valueOf(genericUtility.getValidDateString(ls_nxt_yr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}

						sql = "	select count(1) " +
								" from cenvat " +
								"	where site_code = ? and item_code = ? " +
								"	and tran_date >= ? and lot_no = ? and tran_type = 'C' ";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msite);
						pstmt.setString(2, mVal);
						pstmt.setTimestamp(3, ldt_nxt_yr);
						pstmt.setString(4, mlotno);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						if( cnt > 0 )
						{
							errCode = "VTCENITM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}	
					else if (childNodeName.equalsIgnoreCase("unit")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("unit", dom));

						isExist = isExist("uom", "unit", mVal, conn);           

						if(! isExist)
						{
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					else if (childNodeName.equalsIgnoreCase("loc_code")) 
					{
						mVal = checkNull(genericUtility.getColumnValue("loc_code", dom));
						mVal1 = checkNull(genericUtility.getColumnValue("item_code", dom));
						ls_line_no__sord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

						isExist = isExist("location", "loc_code", mVal, conn);

						if( !isExist ) 
						{	
							//added condition by Nandkumar Gadkari on 19/06/18 for dispatch entry for Non Inventory Item  
							ls_stk_opt = getValue("stk_opt", "item", "item_code", mVal1, conn);
							if(!"0".equalsIgnoreCase(ls_stk_opt) 	)
							{
							errCode = "VTLOC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
							//added for showing loc_code error....@testing 
							//System.out.println("..... loc code not exist...");
							//errString = getErrorString("loc_code","VTLOC1",userId);
							//return errString ;
							}
						}
						else
						{
							ls_available_yn = checkNull(genericUtility.getColumnValue("available_yn", dom));
							if( isnull(ls_available_yn) || lenTrim(ls_available_yn) == 0)
							{
								ls_available_yn="Y";
							}
							msite = checkNull(genericUtility.getColumnValue("site_code", dom));	
							ls_cust_code = checkNull(genericUtility.getColumnValue("cust_code", dom));	
							ls_cust_code__dlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));	

							if("N".equalsIgnoreCase(ls_available_yn)) 
							{

								cnt=0;
								sql = " select channel_partner from site_customer " +
										" where cust_code= ? and site_code= ? and available_yn='N' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_cust_code__dlv);
								pstmt.setString(2, msite);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt++;
									ls_channel_partner = checkNull(rs.getString("channel_partner"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

							}

							if( cnt == 0  || isnull(ls_channel_partner) )	
							{
								sql = " select channel_partner  " +
										" from customer where cust_code= ? " +
										" and available_yn='N' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_cust_code__dlv);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt++;
									ls_channel_partner = checkNull(rs.getString("channel_partner"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;


								if ( cnt==0 || isnull(ls_channel_partner) )	
								{
									ls_channel_partner="N";
								}//end if	
							}//end if
						}//	end if

						sql = " select available   "+
								" from location, invstat  "+
								" where location.inv_stat = invstat.inv_stat "+ 
								" and location.loc_code = ? "; 	
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mVal);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ls_available = checkNull(rs.getString("available"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( "N".equalsIgnoreCase(ls_available))
						{		
							if( (!"N".equalsIgnoreCase(ls_available_yn)) && (! "Y".equalsIgnoreCase(ls_channel_partner))) 
							{
								errCode = "VTAVAIL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}//	end if		 
					}//		end if	
					//}//			end if
					//}//					end if
					//}
					else if (childNodeName.equalsIgnoreCase("quantity")) 
					{
						ll_cnt=0;
						mNum = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));

						/*if( mNum < 0 )
						{
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}*/

						msaleord = checkNull(genericUtility.getColumnValue("sord_no", dom));
						mlineno = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						mlineno = "   "+mlineno;
						mlineno = mlineno.substring(mlineno.length()-3,mlineno.length());
						mexplev = checkNull(genericUtility.getColumnValue("exp_lev", dom));

						lc_quantity__stduom = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));

						mitemcode = checkNull(genericUtility.getColumnValue("item_code", dom));
						msite = checkNull(genericUtility.getColumnValue("site_code", dom));
						mlotno = checkNull(genericUtility.getColumnValue("lot_no", dom));
						mlotsl = checkNull(genericUtility.getColumnValue("lot_sl", dom));
						mloccode = checkNull(genericUtility.getColumnValue("loc_code", dom));
						mitem_code__ord = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						Ls_DespId = checkNull(genericUtility.getColumnValue("desp_id", dom));

						ls_cust_code = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						ld_trandate_str = checkNull(genericUtility.getColumnValue("desp_date", dom1));

						if( ld_trandate_str != null && lenTrim(ld_trandate_str) > 0 )
						{
							ld_trandate = Timestamp.valueOf(genericUtility.getValidDateString(ld_trandate_str,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}

						ls_site_code = msite;
						lc_qty = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));

						lc_sord_qty = 0;
						lc_tot_qty = 0;
						mstkQty = 0;
						mNum1 = 0;
						mNum2 = 0;

						sql = "	select iss_criteria, item_code__parent " +
								" from item where item_code = ? "; 					 
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitemcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ls_iss_criteria = rs.getString("iss_criteria");
							ls_item_code__parent = rs.getString("item_code__parent");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if("W".equalsIgnoreCase(ls_iss_criteria.trim()))
						{		
							sql = " select quantity  from	 stock " +
									" where	 site_code = ? and	 item_code = ? " +
									" and loc_code  = ? 	and	 lot_no	 = ? " +
									" and lot_sl    = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, msite);
							pstmt.setString(2, mitemcode);
							pstmt.setString(3, mloccode);
							pstmt.setString(4, mlotno);
							pstmt.setString(5, mlotsl);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								lc_stkqty = rs.getDouble("quantity");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							if( mNum > lc_stkqty )
							{
								errCode = "VTPACKQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	

							}//end if
						}//end if 

						//if( errCode == null || lenTrim(errCode) == 0 )
						//{
							//lc_PendingQty = gbf_GetPendingQty(errCode)	;

							lc_PendingQty = getPendingQty(dom,dom2 ,conn);
							BaseLogger.log("3", null, null, "In valdata sm ---1--- lc_PendingQty ["+lc_PendingQty + "]");
							//if( errCode != null && lenTrim(errCode) > 0 )
							//{
							//	errList.add(errCode);
							//	errFields.add(childNodeName.toLowerCase());					
							//}

							if( lc_PendingQty < 0 ) 
							{
								sql = " select quantity from sorditem " +
										" where  sale_order = ? and    line_no 	= ? " +
										"	and    exp_lev 	= ? ";					
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, msaleord);
								pstmt.setString(2, mlineno);
								pstmt.setString(3, mexplev);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									lc_sord_qty = rs.getDouble("quantity");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								ls_cust_code = checkNull(genericUtility.getColumnValue("cust_code", dom));

								sql = "	Select over_ship_perc  From   sorddet " +
										"	Where  sale_order = ? 	And	 line_no 	= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, msaleord);
								pstmt.setString(2, mlineno);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									lc_overshipperc =  rs.getDouble("over_ship_perc");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;


								if( lc_overshipperc == 0 )						
								{
									ls_item_ser = getValue("item_ser", "sorder", "sale_order", msaleord, conn);

									//modespperc = nvo_dis_sals.gbf_get_qty_variance_perc(mitemcode, ls_cust_code, ls_item_ser, ls_errcode );
									modespperc = get_qty_variance_perc(mitemcode, ls_cust_code, ls_item_ser, conn );
								}
								else
								{
									modespperc = lc_overshipperc;
								}//end If
								
								lc_ceilqty = (lc_sord_qty * (modespperc/100));
								/*if gs_database = "oracle" then
										{
											select ceil(:lc_ceilqty) into :lc_overship from dual;
										}
										else
										{
											select ceiling(:lc_ceilqty) into :lc_overship from dual;
										}*/
								//end if

								////if lc_overship > abs(lc_PendingQty) then		//Comented Ruchira 18/05/2k6
								//if( lc_overship < abs(lc_PendingQty) then		//Added Ruchira 18/05/2k6
								
								double totDespQty = getDespQty(dom,dom2 ,conn);
								double extraQty = totDespQty - lc_sord_qty;
								
								BaseLogger.log("3", null, null, "In valdata sm ---2--- totDespQty ["+totDespQty + "] extraQty [" + extraQty + "] lc_ceilqty [" + lc_ceilqty + "] modespperc [" + modespperc + "] ");
								
								//if( Math.ceil(lc_ceilqty) < Math.abs(lc_PendingQty) )
								if( Math.ceil(lc_ceilqty) < Math.abs(extraQty) )
								{
									errCode = "VTDESP10";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
							}//end if								
						//}//end if

						sql = " select site_code, nature from sorddet " +
								" where sale_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msaleord);
						pstmt.setString(2, mlineno);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							msite = checkNull(rs.getString("site_code"));
							ls_nature = checkNull(rs.getString("nature"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( mNum <= 0 )  //==
						{
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else     // if mnum = 0 
						{
							mitemord = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
							mitemcode = checkNull(genericUtility.getColumnValue("item_code", dom));
							mlotno = checkNull(genericUtility.getColumnValue("lot_no", dom));
							mlotsl = checkNull(genericUtility.getColumnValue("lot_sl", dom));
							mloccode = checkNull(genericUtility.getColumnValue("loc_code", dom));

							sql = " select quantity, qty_desp " +
									"	from sorditem 	where sale_order = ?  " +
									"	and line_no = ? 	and site_code = ? 	and exp_lev = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, msaleord);
							pstmt.setString(2, mlineno);
							pstmt.setString(3, msite);
							pstmt.setString(4, mexplev);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								mNum1 = rs.getDouble("quantity");
								mNum2 = rs.getDouble("qty_desp");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;




							if( mNum1 < (mNum + mNum2) )
							{// Error : Despatched qty more than sale order item qty.
								/*
								if( "NULLFOUND".equalsIgnoreCase(modesp))   //to be chk
								{
									errCode = "VTQTYD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
								else
								{*/	
								//ceiling is added for next max value.
								if ((((mNum + mNum2) - mNum1)/mNum1 * 100) > Math.ceil(modespperc))
								{
									errCode = "VTQTYD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
								//}//end if
							}//end if
							else	
							{		//To ignore stock check if stock option is No Stock Maintainence
								ls_stk_opt = getValue("stk_opt", "item", "item_code", mitemcode, conn);
								if(!"0".equalsIgnoreCase(ls_stk_opt) 	)
								{//CHECKING WHETHER ITEM IS ALLOCATED OR NOT IF ALLOCATED CHECK ONLY QTY ELSE CHK QTY - QTY_ALLOC

									sql = "	select count(1) from sordalloc " +
											"	where  sale_order = ? and line_no = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, msaleord);
									pstmt.setString(2, mlineno);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										ll_cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;


									ls_available_yn = checkNull(genericUtility.getColumnValue("available_yn", dom));

									if( isnull(ls_available_yn) || lenTrim(ls_available_yn) == 0 )
									{
										ls_available_yn="Y";
									}
									msite = checkNull(genericUtility.getColumnValue("site_code", dom));
									ls_cust_code = checkNull(genericUtility.getColumnValue("cust_code", dom));
									ls_cust_code__dlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));

									if("N".equalsIgnoreCase(ls_available_yn)) 
									{
										sql = " select channel_partner from site_customer where cust_code= ? " +
												" and site_code= ? and available_yn= 'N' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, ls_cust_code__dlv);
										pstmt.setString(2, msite);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											ls_channel_partner = checkNull(rs.getString("channel_partner"));
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}

									if( isnull(ls_channel_partner) )	
									{
										sql = " select channel_partner from customer where cust_code= ?  and available_yn='N' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, ls_cust_code__dlv);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											ls_channel_partner = checkNull(rs.getString("channel_partner"));
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if( isnull(ls_channel_partner) )	
										{
											ls_channel_partner="N";
										}
										//end if	
									}//end if
								}//end if

								cnt=0;
								if( ll_cnt > 0 ) 
								{
									if("N".equalsIgnoreCase(ls_available_yn) && "Y".equalsIgnoreCase(ls_channel_partner))
									{
										sql = "	select quantity from   stock where  item_code 	= : " +
												"	and site_code = ? and loc_code = ? " +
												" and lot_no = ? and lot_sl = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, mitemcode);
										pstmt.setString(2, msite);
										pstmt.setString(3, mloccode);
										pstmt.setString(4, mlotno);
										pstmt.setString(5, mlotsl);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt++;
											mstkQty = rs.getDouble("quantity");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;


									}
									else
									{
										sql = " select quantity - (case when hold_qty is null then 0 else hold_qty end ) " +
												" from   stock  " +
												" where  item_code 	= ?  " + 
												" and    site_code 	= ?  " +
												" and 	 loc_code 	= ?  " +
												" and 	 lot_no 	= ?  " +
												" and 	 lot_sl 	= ? ";

										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, mitemcode);
										pstmt.setString(2, msite);
										pstmt.setString(3, mloccode);
										pstmt.setString(4, mlotno);
										pstmt.setString(5, mlotsl);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt++;
											mstkQty = rs.getDouble(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;


									}//	end if
									//end of the code added by ajit on date 23/7/2014
								}	
								else
								{		
									if("N".equalsIgnoreCase(ls_available_yn) && "Y".equalsIgnoreCase(ls_channel_partner))
									{
										sql = "	select quantity  from stock where  item_code 	= ? " +
												"	and site_code 	= ? and loc_code 	= ? " +
												" and lot_no = ? 	and  lot_sl 	= ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, mitemcode);
										pstmt.setString(2, msite);
										pstmt.setString(3, mloccode);
										pstmt.setString(4, mlotno);
										pstmt.setString(5, mlotsl);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt++;
											mstkQty = rs.getDouble(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

									}
									else
									{
										sql = " select quantity - (case when hold_qty is null then 0 else hold_qty end ) from stock "+ 
												" where  item_code 	= ?  "+
												" and 	 site_code 	= ?  "+
												" and 	 loc_code 	= ?  "+
												" and 	 lot_no 	= ?  "+
												" and 	 lot_sl 	= ?  ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, mitemcode);
										pstmt.setString(2, msite);
										pstmt.setString(3, mloccode);
										pstmt.setString(4, mlotno);
										pstmt.setString(5, mlotsl);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt++;
											mstkQty = rs.getDouble(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

									}	//end if	
									//end of the code added by ajit on date 23/7/2014
									if("0".equalsIgnoreCase(ls_stk_opt)) //Added by Nandkumar Gadkari on 19/06/18 for dispatch entry for Non Inventory Item  
									{
										cnt++;
									}

								}//	end if
								if(cnt == 0)
								{
									errCode = "VTSTOCK2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
								else
								{	

									ls_unit_desp = checkNull(genericUtility.getColumnValue("unit", dom)).trim();
									ls_unit_std = checkNull(genericUtility.getColumnValue("unit__std", dom)).trim();
									lc_conv = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("conv__qty_stduom", dom)));
									if(! ls_unit_desp.equalsIgnoreCase(ls_unit_std)) 
									{
										mNum	=	mNum * lc_conv;
									}
									//System.out.println("@@@@@1643  mNum["+mNum+"]mstkQty["+mstkQty+"]");
									if( mNum > mstkQty && !"0".equalsIgnoreCase(ls_stk_opt))  //Added stkopt condition  by Nandkumar Gadkari on 19/06/18 for dispatch entry for Non Inventory Item  
									{
										mVal1 = getValue("inv_stat", "location", "loc_code", mloccode, conn);

										mVal2 = getValue("overiss", "invstat", "inv_stat", mVal1, conn);

										if("N".equalsIgnoreCase(mVal2)) 												
										{
											errCode = "VTQTYA2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										else												
										{
											errCode = "VTQTYA1";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										//end if	// end of if mVal2 = 'N' then
									}
									//PriyankaC TO check stock quantity and despatch net quantity for validation...[Start] on 15NOV2018.
									     netQuantity = GetNumberOfQuantityFordespatch(dom2 ,mlotno,mlotsl,msite,mitemcode,mloccode);
									     //System.out.println("Quentityofdispatch.." +netQuantity + "mstkQty.."+mstkQty + "ls_stk_opt.."+ls_stk_opt);
	                                     //Commented and Added by sarita as In case of stock option "0" then system should not check stock on 17 JULY 2019 [START]
									     // if(netQuantity > mstkQty)
									     if(netQuantity > mstkQty && !"0".equalsIgnoreCase(ls_stk_opt))
									    	//Commented and Added by sarita as In case of stock option "0" then system should not check stock on 17 JULY 2019 [END]	 
	                                     {
	                                    	     errCode = "VTSTOCK2";
												 errList.add(errCode);
												 errFields.add(childNodeName.toLowerCase());
	                                     }
									   //PriyankaC TO check stock quantity and despatch net quantity for validation...[END]
								}//	end if	// end if get_sqlcode(), stock quantity 
							}//			end if
						}//end if	// end if get_sqlcode(), sum(qty_alloc)
						//}//end if	// end if get_sqlcode(), qty_alloc
						//test purpose	}//end if	// end if mNum = 0
						//test purpose	}//end if	

						//if(  isnull(errCode) || lenTrim(errCode) == 0 ) 
						//{
							//Check For Integral Quantity Added.
							ls_item_code = checkNull(genericUtility.getColumnValue("item_code", dom));
							sql = "	select count(*)  from   customeritem " +
									"	where  cust_code = ?	and item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_cust_code);
							pstmt.setString(2, ls_item_code);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								ll_count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;



							if( ll_count > 0 )
							{
								sql = " select integral_qty, restrict_upto " +
										"  from customeritem 	where  cust_code = ? and item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_cust_code);
								pstmt.setString(2, ls_item_code);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									lc_int_qty = rs.getDouble("integral_qty");
									ld_rest_upto = rs.getTimestamp("restrict_upto");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;


								if(ld_rest_upto != null )
								{
									if( ld_trandate.compareTo(ld_rest_upto) < 1 ) 
									{
										errCode = "VTRESDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}//end if
								if( lc_int_qty > 0 )
								{
									//Select mod(:lc_qty,:lc_int_qty) into :mmodqty from dual; // Added By Subu
									//System.out.println("@@@@@@ mmodqty["+mmodqty+"]lc_qty["+lc_qty+"]lc_int_qty["+lc_int_qty+"]");
									mmodqty = lc_qty % lc_int_qty;
									//System.out.println("@@@@@@ mmodqty["+mmodqty+"]");
									if( mmodqty > 0 )
									{
										errCode = "VTINTQTY";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									//end if
									//Addition ended by Raj (SY89SUN097) - 20/11/2009
								}//end if
							}
							else if( ll_count == 0 )
							{
								//lc_int_qty = gf_integral_qty("",ls_item_code,ls_site_code);
								lc_int_qty = distCommon.getIntegralQty("", ls_item_code, ls_site_code, conn);
								//System.out.println("@@@@@@@ lc_int_qty["+lc_int_qty+"]");
								if( lc_int_qty > 0 )
								{
									//Select mod(:lc_qty,:lc_int_qty) into :mmodqty from dual; // Added By Subu

									//System.out.println("@@@@@@ mmodqty["+mmodqty+"]lc_qty["+lc_qty+"]lc_int_qty["+lc_int_qty+"]");
									mmodqty = lc_qty % lc_int_qty;
									//System.out.println("@@@@@@ mmodqty["+mmodqty+"]");

									if( mmodqty > 0 )
									{
										errCode = "VTINTQTY1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									//end if
									//Addition ended by Raj (SY89SUN097) - 20/11/2009
								}//end if
							}//end if

						//}//end if

						//if(  isnull(errCode) || lenTrim(errCode)  == 0 ) 
						//{
							ls_order_type = getValue("order_type", "sorder", "sale_order", msaleord, conn);

							ArrayList ls_dis_pob_ordtype_list = new ArrayList();
							String disPobOrdtype = checkNull(distCommon.getDisparams("999999", "POB_ORD_TYPE", conn)).trim();

							String disPobOrdtypeArray[]=disPobOrdtype.split(",");
							//System.out.println("@@@@@ disPobOrdtypeArray.length["+disPobOrdtypeArray.length+"]");

							lb_ord_flag=false;

							/*do while len(trim(ls_dis_pob_ordtype_list)) > 0
							ls_dis_pob_ord_type = f_get_token(ls_dis_pob_ordtype_list,',')
							if trim(ls_order_type) = trim(ls_dis_pob_ord_type) then
							lb_ord_flag=true 
							end if
							loop*/

							for( String ls_dis_pob_ord_type: disPobOrdtypeArray)
							{
								if( ls_order_type.trim().equalsIgnoreCase(ls_dis_pob_ord_type.trim())) 
								{
									lb_ord_flag=true; 
								}
							}

							if( ("F".equalsIgnoreCase(ls_nature) || "B".equalsIgnoreCase(ls_nature) || "S".equalsIgnoreCase(ls_nature) ) && lb_ord_flag == false )
							{
								sql = " select order_type, state_code__dlv, count_code__dlv,order_date,price_list " +
										"	from sorder where sale_order = ? "; //? ADDED BY NANDKUMAR GADKARI ON 10/06/19
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, msaleord);
								/*pstmt.setString(2, ls_item_code);*/
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									ls_order_type = checkNull(rs.getString("order_type"));
									ls_state_code = checkNull(rs.getString("state_code__dlv"));
									ls_count_code = checkNull(rs.getString("count_code__dlv"));
									ldt_order_date = rs.getTimestamp("order_date");
									ls_pricelist = checkNull(rs.getString("price_list"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( ldt_order_date != null)
								{
									//ldt_order_date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
									ldt_order_date_str =   sdf.format(ldt_order_date.getTime());
								}


								if( isnull(ls_item_code__parent) || lenTrim(ls_item_code__parent) == 0 )
								{		
									isExist = isExist("item", "item_code__parent", mitemcode, conn);

									if(isExist)
									{
										ls_item_code__parent = mitemcode;
									}
								}//end if

								PostOrderActivity poActivity = new PostOrderActivity();
								//ls_scheme_code = nvo_dis_sals.gbf_check_scheme(ls_item_code__parent,ls_order_type, ls_cust_code, ls_site_code, ls_state_code, ls_count_code,ld_trandate);
								ls_scheme_code = poActivity.checkScheme(ls_item_code__parent, ls_order_type, ls_cust_code, ls_site_code, ls_state_code, ls_count_code, ld_trandate, conn);

								if( isnull(ls_scheme_code) || lenTrim(ls_scheme_code) == 0  )
								{
									errCode = "VTFREEQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									ls_scheme_flag = getValue("scheme_flag", "bom", "bom_code", ls_scheme_code, conn);

									sql = " select tot_charge_qty, tot_free_qty ,tot_bonus_qty,tot_sample_qty,rate "+ 
											" from prd_scheme_trace  "+
											" where site_code= ? "+
											" and cust_code	= ? "+
											" and item_code	= ? "+
											" and scheme_code= ? "+
											" and ? between eff_from and valid_upto";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_site_code);
									pstmt.setString(2, ls_cust_code);
									pstmt.setString(3, ls_item_code__parent);
									pstmt.setString(4, ls_scheme_code);
									pstmt.setTimestamp(5, ld_trandate);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										lc_tot_charge_qty =  rs.getDouble("tot_charge_qty");
										lc_tot_free_qty = rs.getDouble("tot_free_qty");
										lc_tot_bonus_qty = rs.getDouble("tot_bonus_qty");
										lc_tot_sample_qty = rs.getDouble("tot_sample_qty");
										lc_rate = rs.getDouble("rate");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									lc_qty = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));
									ls_cur_lineno = checkNull(genericUtility.getColumnValue("line_no", dom));
									lc_prv_charge_qty = 0;
									lc_prv_free_qty = 0;
									lc_prv_bonus_qty = 0;
									lc_prv_sample_qty = 0;
									if( lc_rate ==  0 )
									{
										if( lenTrim(ls_pricelist) > 0 )
										{
											//lc_rate = i_nvo_gbf_func.gbf_pick_rate(ls_pricelist,ldt_order_date,ls_item_code__parent,'','L',lc_qty);
											lc_rate = distCommon.pickRateGSM(ls_pricelist, ldt_order_date_str, ls_item_code__parent, "", "L", lc_qty, conn);
										}
										else
										{	
											lc_rate = 0;
										}	//end if

										if( lc_rate == 0 )	
										{
											ls_pricelist = checkNull(distCommon.getDisparams("999999", "STD_SO_PL", conn));

											if( isnull(ls_pricelist) || "NULLFOUND".equalsIgnoreCase(ls_pricelist) )
											{
												lc_rate = 0; 
											}
											else		
											{		
												//lc_rate = i_nvo_gbf_dis.gbf_pick_rate (ls_pricelist,ldt_order_date,ls_item_code__parent,' ','L');
												lc_rate = distCommon.pickRate(ls_pricelist, ldt_order_date_str, ls_item_code__parent, "", "L", conn);

												/*if (isnull(string(lc_rate))) 
														{
															lc_rate = 0;		
														}*/
											}//end if
										}//	end if	

									}//			end if



									lc_tot_charge_value = lc_tot_charge_qty * lc_rate;
									lc_tot_free_value = lc_tot_free_qty * lc_rate;
									lc_tot_bonus_value = lc_tot_bonus_qty * lc_rate;
									lc_tot_sample_value = lc_tot_sample_qty * lc_rate;
									mvalue = lc_qty * lc_rate ;


									ls_cur_lineno = checkNull(genericUtility.getColumnValue("line_no", dom));

									HashMap<String, Double> sampleValueMap = new HashMap<String, Double>();
									sampleValueMap = sampleValueCal(dom2,ls_item_code__parent,ls_cur_lineno,ls_scheme_flag,lc_rate,conn);

									lc_prv_free_qty = sampleValueMap.get("lc_prv_free_qty");
									lc_prv_bonus_qty = sampleValueMap.get("lc_prv_bonus_qty");
									lc_prv_sample_qty = sampleValueMap.get("lc_prv_sample_qty");
									lc_prv_charge_qty = sampleValueMap.get("lc_prv_charge_qty");

									sql = " select nature from sorddet " +
											" where sale_order = ? and line_no = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, msaleord);
									pstmt.setString(2, mlineno);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										ls_nature = checkNull(rs.getString("nature"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;


									lc_charge_qty = lc_prv_charge_qty + lc_tot_charge_qty ;
									if("V".equalsIgnoreCase(ls_scheme_flag))	
									{
										lc_charge_value =  lc_prv_charge_value + lc_tot_charge_value ;
									}

									int cnt2=0;
									//quantity slab
									if("V".equalsIgnoreCase(ls_scheme_flag))	
									{
										sql = "	select 	bom.batch_value	,bomdet.value_per, bomdet.min_value	," +
												" bomdet.app_min_value,	bomdet.app_max_value, bomdet.round, bomdet.round_to " +
												" from bom, bomdet where bom.bom_code = bomdet.bom_code " +
												" and bomdet.bom_code 	= ? " +
												" and bomdet.nature 		= ?  " +
												" and ? between case when min_batch_value is null then 0 else min_batch_value end " +
												" and case when max_batch_value is null then 0 else max_batch_value end ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, ls_scheme_code);
										pstmt.setString(2, ls_nature);
										pstmt.setDouble(3, lc_charge_value);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt2++;
											lc_batvalue = rs.getDouble("batch_value");
											lc_valueper = rs.getDouble("value_per");
											lc_minvalue = rs.getDouble("min_value");
											lc_app_min_value = rs.getDouble("app_min_value");
											lc_app_max_value = rs.getDouble("app_max_value");
											ls_round = checkNull(rs.getString("round"));
											ld_roundto = rs.getDouble("round_to");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

									}
									else
									{
										sql = " select 	bom.batch_qty,bomdet.qty_per,bomdet.app_min_qty,bomdet.app_max_qty, bomdet.round, bomdet.round_to " +
												"	from bom, bomdet where bom.bom_code = bomdet.bom_code and " +
												" bomdet.bom_code 	= ? and " +
												" bomdet.nature 		= ?  and ? between case when bom.min_qty is null then 0 else bom.min_qty end " +
												" and case when bom.max_qty is null then 0 else bom.max_qty end ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, ls_scheme_code);
										pstmt.setString(2, ls_nature);
										pstmt.setDouble(3, lc_charge_qty);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt2++;
											lc_batqty = rs.getDouble("batch_qty");
											lc_qtyper = rs.getDouble("qty_per");
											lc_app_min_qty = rs.getDouble("app_min_qty");
											lc_app_max_qty = rs.getDouble("app_max_qty");
											ls_round = checkNull(rs.getString("round"));
											ld_roundto = rs.getDouble("round_to");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;



									}//end if

									if(cnt2 == 0)
									{
										errCode = "VTFREEQTY2"; //Chargeable quantity of group of items not eligible for the free quantity
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{	
										if("V".equalsIgnoreCase(ls_scheme_flag))			
										{
											if( lc_charge_value >= lc_app_min_value && lc_charge_value <= lc_app_max_value )
											{
												//lc_free_value = truncate(lc_charge_value / lc_batvalue,0) * lc_valueper ;//Calculating free qty based on slab
												lc_free_value = (lc_charge_value / lc_batvalue) * lc_valueper ;//Calculating free qty based on slab
											}
											else
											{
												lc_free_value = 0;
											}//	end if
										}
										else
										{
											if( lc_charge_qty >= lc_app_min_qty && lc_charge_qty <= lc_app_max_qty )
											{
												lc_free_qty = (lc_charge_qty / lc_batqty) * lc_qtyper; //Calculating free qty based on slab
											}
											else
											{
												lc_free_qty = 0;
											}
										}//end if	

										if( lc_free_qty > 0 )
										{	
											//if( (! isnull(ls_round)) && ( ! isnull(ld_roundto) ))
											if( (! isnull(ls_round))  )
											{
												// lc_free_qty = gf_get_rndamt(lc_free_qty,ls_round,ld_roundto);
												lc_free_qty = distCommon.getRndamt(lc_free_qty, ls_round, ld_roundto);
											}//end if
										}//end if

										if( lc_free_value > 0 )
										{
											//if( (! isnull(ls_round)) && (! isnull(ld_roundto) ))
											if( (! isnull(ls_round)) )
											{
												//lc_free_value = gf_get_rndamt(lc_free_value,ls_round,ld_roundto);
												lc_free_value = distCommon.getRndamt(lc_free_value, ls_round, ld_roundto);
											}//end if
										}//end if

										//string ls_temp1,ls_temp2
										//ls_temp1 = "Pravin eligible lc_free_value  :" + string(lc_free_value) +"lc_charge_value :"+string(lc_charge_value)
										//ls_temp2 = "Pravin 2 actual toal free value  :" + string(mvalue + lc_tot_free_value + lc_prv_free_value) 
										//select :ls_temp1 into :ls_temp1 from dual	;
										//select :ls_temp2 into :ls_temp2 from dual	;

										if("V".equalsIgnoreCase(ls_scheme_flag))			
										{		
											if("F".equalsIgnoreCase(ls_nature))									
											{
												if( (mvalue + lc_tot_free_value + lc_prv_free_value) > lc_free_value )  
												{
													errCode = "VTFREEVAL1"; //Entered free vaLUE is greater than scheme's free value
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}//end if
											}//end if
											if("B".equalsIgnoreCase(ls_nature))
											{
												if( (mvalue + lc_tot_bonus_value  + lc_prv_bonus_value) > lc_free_value )  
												{
													errCode = "VTBONUVAL1"; //Entered free vaLUE is greater than scheme's free value
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}//end if	
											if("S".equalsIgnoreCase(ls_nature))
											{
												if( (mvalue + lc_tot_sample_value + lc_prv_sample_value) > lc_free_value )  
												{
													errCode = "VTSAMPVAL1"; //Entered free vaLUE is greater than scheme's free value
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}//end if
											}//end if	
										}
										else            
										{
											if("F".equalsIgnoreCase(ls_nature))
											{	
												if( (lc_qty + lc_tot_free_qty + lc_prv_free_qty) > lc_free_qty )
												{
													errCode = "VTFREEQTY1"; //Entered free quantity is greater than scheme's free quantity
												}//end if
											}//end if
											if("B".equalsIgnoreCase(ls_nature))
											{
												if( (lc_qty + lc_tot_bonus_qty + lc_prv_bonus_qty) > lc_free_qty )  
												{
													errCode = "VTBONSQTY1"; //Entered free quantity is greater than scheme's free quantity
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}//end if
											}//END IF
											if("S".equalsIgnoreCase(ls_nature))
											{
												if( (lc_qty + lc_tot_sample_qty + lc_prv_sample_qty) > lc_free_qty )  
												{
													errCode = "VTSAMPQTY1"; //Entered free quantity is greater than scheme's free quantity
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}//end if
											}//END IF
										}//end if	
									}//end if 


								}//				end if //if len(trim(ls_scheme_code)) = 0
							//}//					end if
						}//								end if

					} // end of quantity wf

					else if (childNodeName.equalsIgnoreCase("lot_no")) 
					{
						msaleord = checkNull(genericUtility.getColumnValue("sord_no", dom));
						mlineno = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						mlineno = "   "+mlineno;
						mlineno = mlineno.substring(mlineno.length()-3,mlineno.length());
						mdate1Str = checkNull(genericUtility.getColumnValue("desp_date", dom1));
						mitemcode = checkNull(genericUtility.getColumnValue("item_code", dom));
						mlotno = checkNull(genericUtility.getColumnValue("lot_no", dom));
						mloccode = checkNull(genericUtility.getColumnValue("loc_code", dom));

						if( mdate1Str != null && lenTrim(mdate1Str) > 0 )
						{
							mdate1 = Timestamp.valueOf(genericUtility.getValidDateString(mdate1Str,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						sql = " select site_code from sorddet " +
								" where sale_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msaleord);
						pstmt.setString(2, mlineno);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							msite = checkNull(rs.getString("site_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						ls_stk_opt = getValue("stk_opt", "item", "item_code", mitemcode, conn); 

						if(!"0".equalsIgnoreCase(ls_stk_opt))
						{	
							sql = " select count(*) from 	stock "+ 
									" where  item_code 	= ?  "+
									" and 	 site_code 	= ?  "+
									" and 	 loc_code 	= ?  "+
									" and 	 lot_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mitemcode);
							pstmt.setString(2, msite);
							pstmt.setString(3, mloccode);
							pstmt.setString(4, mlotno);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								mcnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							if( mcnt == 0 )
							{
								//errcode = "VMLOTNO1" + ' ~t ' + 'for item : ' + mitemcode + ' for site : ' + msite + ' for location : ' + mloccode + ' for lot no. : ' + mlotno
								errCode = "VMLOTNO1"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}//end if 		//Ignore stock check for no stockable items	

					}

					else if (childNodeName.equalsIgnoreCase("lot_sl")) 
					{
						msaleord = checkNull(genericUtility.getColumnValue("sord_no", dom));
						mlineno = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						mlineno = "   "+mlineno;
						mlineno = mlineno.substring(mlineno.length()-3,mlineno.length());
						mdate1Str = checkNull(genericUtility.getColumnValue("desp_date", dom1));
						mlotno = genericUtility.getColumnValue("lot_no", dom);
						mlotsl = genericUtility.getColumnValue("lot_sl", dom);
						mitemcode = checkNull(genericUtility.getColumnValue("item_code", dom));
						mloccode = checkNull(genericUtility.getColumnValue("loc_code", dom));


						if( mdate1Str != null && lenTrim(mdate1Str) > 0 )
						{
							ld_desp_dt = Timestamp.valueOf(genericUtility.getValidDateString(mdate1Str,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}

						sql = " select site_code from sorddet " +
								" where sale_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msaleord);
						pstmt.setString(2, mlineno);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							msite = checkNull(rs.getString("site_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						ls_stk_opt = getValue("stk_opt", "item", "item_code", mitemcode, conn);

						if(!"0".equalsIgnoreCase(ls_stk_opt))
						{	
							sql = " select count(*) from 	stock "+ 
									" where  item_code 	= ?  "+
									" and 	 site_code 	= ?  "+
									" and 	 loc_code 	= ?  "+
									" and 	 lot_no = ? " +
									" and lot_sl = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mitemcode);
							pstmt.setString(2, msite);
							pstmt.setString(3, mloccode);
							pstmt.setString(4, mlotno);
							pstmt.setString(5, mlotsl);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								mcnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							if( mcnt == 0 )
							{
								errCode = "VMLOTSL1"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = " select (case when track_shelf_life is null then 'N' else track_shelf_life end) " +
										"	from item where item_code = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mitemcode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									ls_track_shelf_life = checkNull(rs.getString(1));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;


								sql = " select exp_date, mfg_date, site_code__mfg " +
										" from stock  " +
										" where item_code = ?  " +
										" and site_code = ?  " +
										" and loc_code = ?  " +
										" and lot_no = ?  " +
										" and lot_sl = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mitemcode);
								pstmt.setString(2, msite);
								pstmt.setString(3, mloccode);
								pstmt.setString(4, mlotno);
								pstmt.setString(5, mlotsl);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									ld_exp_date = rs.getTimestamp("exp_date");
									ld_mfg_date = rs.getTimestamp("mfg_date");
									ls_site_mfg = checkNull(rs.getString("site_code__mfg"));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;



								//Check for Expiry Date for Near Expiry Order.
								if("Y".equalsIgnoreCase(ls_track_shelf_life))
								{
									ls_order_type = getValue("order_type", "sorder", "sale_order", msaleord, conn);

									if("NE".equalsIgnoreCase(ls_order_type.trim()))
									{
										mexplev = checkNull(genericUtility.getColumnValue("exp_lev", dom));

										sql = " select min_shelf_life,max_shelf_life " +
												"	from sorditem where sale_order = ? " +
												"	and line_no = ? 	and exp_lev = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, msaleord);
										pstmt.setString(2, mlineno);
										pstmt.setString(3, mexplev);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											ll_min_shelf_life = rs.getInt("min_shelf_life");
											ll_max_shelf_life = rs.getInt("max_shelf_life");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										//System.out.println("@@@@@2393 ld_desp_dt["+ld_desp_dt+"]ll_min_shelf_life["+ll_min_shelf_life+"]ll_max_shelf_life["+ll_max_shelf_life+"]");

										//ld_chk_date1 = gf_calc_expiry(ld_desp_dt,ll_min_shelf_life + 1);
										ld_chk_date1 = distCommon.CalcExpiry(ld_desp_dt,ll_min_shelf_life + 1);

										ld_chk_date2 = distCommon.CalcExpiry(ld_desp_dt,ll_max_shelf_life);
										//Timestamp ld_exp_date=null;
										//if not(ld_exp_date >= ld_chk_date1 and ld_exp_date <= ld_chk_date2) then //Code commented by ajit on date 30-Jan-2015
										//if (ld_exp_date >= ld_chk_date1 && ld_exp_date <= ld_chk_date2) then // added by ajit on date 30-Jan-2015 instructed by pravin
										//if ((ld_exp_date.compareTo(ld_chk_date1) > -1) && (ld_exp_date .compareTo(ld_chk_date2) < 1 ))
										//System.out.println("minShelfDate ["+ld_chk_date1+"] maxShelfDate["+ld_chk_date2+"]");
										//if ((ld_exp_date.compareTo(ld_chk_date1) > -1) && (ld_exp_date .compareTo(ld_chk_date2) < 1 )) 
										//Change done by kunal on 10/1/2019 to remove max shelf date validation as suggested by piyush sir
										/*if ((ld_exp_date.after(ld_chk_date1))) 
										{
											errCode = "VTNEXPDT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}*/ //COMMENTED BY NANDKUMAR GADKARI ON 25/07/19
										//changes by mukesh on 18/06/19----start
										//if ld_exp_date < ld_alloc_date  and ls_track_shelf_life = 'Y' then 
										/*if( ld_exp_date.before(ld_alloc_date)  && "Y".equalsIgnoreCase(ls_track_shelf_life)) 
										{
											errCode="VTLOTAEUR";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}*/
										
									    if(!(ld_exp_date.after(ld_chk_date1) && ld_exp_date.before(ld_chk_date2)))
										{
										        errCode="VTNEXPDT";
										        errList.add(errCode);
										        errFields.add(childNodeName.toLowerCase());
										}
									  //changes by mukesh on 18/06/19----END
									}
									else
									{

										mexplev = checkNull(genericUtility.getColumnValue("exp_lev", dom));

										sql = " select min_shelf_life "+
												//	" into :ll_min_shelf_life  "+
												" from sorditem  "+
												" where sale_order = ? "+ 
												" and line_no = ?  "+
												" and exp_lev = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, msaleord);
										pstmt.setString(2, mlineno);
										pstmt.setString(3, mexplev);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											ll_min_shelf_life = rs.getInt("min_shelf_life");
											//ll_max_shelf_life = rs.getInt("max_shelf_life");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										//System.out.println("@@@@@2393 ld_alloc_date["+ld_alloc_date+"]ll_min_shelf_life["+ll_min_shelf_life+"]");


										ld_chk_date = distCommon.CalcExpiry(ld_alloc_date,ll_min_shelf_life);
										if(ld_exp_date!=null)
										{
											if( ld_chk_date.after(ld_exp_date))
											{
												errCode = "VTNEXPDT";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
											//Code added by ajit on date 30_jan-2015 instructed by pravin
											if( ld_exp_date.before(ld_alloc_date) && "Y".equalsIgnoreCase(ls_track_shelf_life)) 
											{
												errCode="VTLOTAEUR";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
											//End of the code added by ajit on date 30-Jan-2015
										}

									}//end if
								}//				end if
								//Check for Expiry Date for Near Expiry Order.


								if( "Y".equalsIgnoreCase(ls_track_shelf_life) && ld_exp_date == null )
								{
									errCode="VMEXPDATE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}



							}//													end if

						}//end if //Ignore stock check for no stockable items.

						sql = " select case when batch_size is null then 0 else batch_size  end " +
								" from stock  "+ 
								" where item_code = ? "+ 
								" and 	site_code = ?  "+
								" and 	loc_code  = ? "+ 
								" and 	lot_no 	 = ? "+
								" and 	lot_sl 	 = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mitemcode);
						pstmt.setString(2, msite);
						pstmt.setString(3, mloccode);
						pstmt.setString(4, mlotno);
						pstmt.setString(5, mlotsl);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lc_batch_size = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						if( lc_batch_size > 0 ) 
						{
							sql = " select batch_size " +
									" from batchsize_aprv "+
									" where item_code 	= ?" +
									" and 	site_code 	= ? " +
									" and 	eff_from   <= ? " +
									" and 	valid_upto >= ? " +
									" and 	confirmed 	= 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mitemcode);
							pstmt.setString(2, msite);
							pstmt.setTimestamp(3, ld_desp_dt);
							pstmt.setTimestamp(4, ld_desp_dt);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								lc_mbatch_size = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							if( lc_batch_size > lc_mbatch_size ) 
							{
								errCode = "VTBTHSIZE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}//	end if
					}

					else if (childNodeName.equalsIgnoreCase("quantity_real")) 
					{

						//changes by mayur on 22/11/17----start
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						itemCodeOrd = checkNull(genericUtility.getColumnValue("item_code", dom));
						quantity = checkNull(genericUtility.getColumnValue("quantity", dom));
						convQtyStduom = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						
						
						//ArrayList quantityRealList=new ArrayList();
					    quantityRealList = distCommon.convQtyFactor(unit, unitStd,itemCodeOrd, Double.parseDouble(quantity),Double.parseDouble(convQtyStduom), conn);
						quantityReal = Double.parseDouble( quantityRealList.get(1)== null ?"0": quantityRealList.get(1).toString()  );
						//System.out.println("quantityReal["+quantityReal+"]");
						
	                    //changes by mayur on 22/11/17----end	
						
						lc_desp_qty = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));
						lc_conv = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("conv__qty_stduom", dom)));

						//System.out.println("Realized Quantity["+quantityReal+"]");
						//System.out.println("Despatch Quantity["+lc_desp_qty+"]");
						//System.out.println("Conversion Quantity["+lc_conv+"]");
						
						//quantityReal	=(quantityReal * lc_conv);
						
						//System.out.println("Realized Quantity"+quantityReal);
						//System.out.println("Despatch Quantity"+lc_desp_qty);
						//System.out.println("Conversion Quantity"+lc_conv);
						
						if( quantityReal != lc_desp_qty )
						{
							errCode = "VTIRQ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					else if (childNodeName.equalsIgnoreCase("cust_item__ref")) 
					{
						ls_cust_code = checkNull(genericUtility.getColumnValue("cust_code", dom1));
						ls_cust_item = checkNull(genericUtility.getColumnValue("cust_item__ref", dom));

						if( (! isnull(ls_cust_item) ) && lenTrim(ls_cust_item) >0 ) 
						{
							sql = " select count(1) from customeritem " +
									" where cust_code = ? and  item_code__ref  = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_cust_code);
							pstmt.setString(2, ls_cust_item);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								li_cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( li_cnt  == 0 )
							{
								errCode = "VTCUSTITM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//end if 
						}//END IF 

					}

					else if (childNodeName.equalsIgnoreCase("rate__clg")) 
					{
						lc_rate_clg = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__clg", dom)));
						ls_line_type = checkNull(genericUtility.getColumnValue("line_type", dom));

						if( isnull(ls_line_type) || lenTrim(ls_line_type) == 0 ) 
						{
							ls_line_type = "C";
						}
						if(  lc_rate_clg < 0 && ( "C".equalsIgnoreCase(ls_line_type) || "I".equalsIgnoreCase(ls_line_type)))
						{
							errCode = "VTRATE1"; 
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					else if (childNodeName.equalsIgnoreCase("no_art")) 
					{
						String noArt = checkNull(genericUtility.getColumnValue("no_art", dom));
						
						if( isnull(noArt) || lenTrim(noArt) == 0 ) 
						{
							noArt = "0";
						}
						if( Double.parseDouble( noArt ) < 0 )
						{
							errCode = "VTNOARTNEG"; 
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					
					}
					// validation added for not allow zero rate  by nandkumar gadkari on 05/08/19---------------start--------------------------
					else if (childNodeName.equalsIgnoreCase("rate__stduom")) 
					{
						rate = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__stduom", dom)));
						lineType = checkNull(genericUtility.getColumnValue("line_type", dom));
						lineType= lineType == null || lenTrim(lineType) == 0 ? "C" : lineType;
						msaleord = checkNull(genericUtility.getColumnValue("sord_no", dom));	
						sql = " select price_list,price_list__disc from sorder where sale_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msaleord);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							priceList = checkNull(rs.getString("price_list"));
							priceListDisc = checkNull(rs.getString("price_list__disc"));	
			
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						priceListType= distCommon.getPriceListType(priceList, conn);
						if(priceListDisc==null||priceListDisc.trim().length()==0)
						{
							//Commented By Mukesh Chauhan
							/*if(  rate <= 0 && ( "C".equalsIgnoreCase(lineType) ) && "B".equalsIgnoreCase(priceListType))*/
							if(  rate <= 0 && ( "C".equalsIgnoreCase(lineType)))//END
							{
								errCode = "VTRATE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					// validation added by nandkumar gadkari on 05/08/19---------------end--------------------------
					//Pavan R 17sept19 start[to validate tax environment]
					else if (childNodeName.equalsIgnoreCase("tax_env")) 
					{
						int envCnt = 0 ;					
						//String taxEnv =  checkNull(genericUtility.getColumnValue("tax_env", dom));
						String taxEnv = distCommon.getParentColumnValue("tax_env", dom, "2");
						despDateStr = checkNull(genericUtility.getColumnValue("desp_date", dom1));
						if (despDateStr != null && despDateStr.trim().length() > 0) 
						{	
							despDate = Timestamp.valueOf(genericUtility.getValidDateString(despDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						if (taxEnv != null && taxEnv.trim().length() > 0) 
						{							
							sql = "Select Count(*) from taxenv  where tax_env = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxEnv);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								envCnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;							
							if (envCnt == 0) {
								errCode = "VTTAXENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} else 
							{															
								errCode = distCommon.getCheckTaxEnvStatus(taxEnv, despDate, "S", conn);
								if(errCode != null && errCode.trim().length() > 0)
								{
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					//Pavan R 17sept19 end[to validate tax environment]

				} // end for
				break; // case 2 end



			}

			//System.out.println("@@@@@@@@@@@@ errList["+errList+"]");
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					//System.out.println("@@@@@@@errCode ..........[" + errCode+"]");
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) {
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}// end of validation



	private double get_qty_variance_perc(String as_item_code,
			String as_cust_code, String as_item_ser, Connection conn) throws SQLException, ITMException
			{

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql="";
		double lc_var_perc= 0;
		String ls_val="";

		sql = " SELECT varience_qtyper  FROM customeritem " +
				"	WHERE ( cust_code = ? ) AND ( item_code = ? )   ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, as_cust_code);
		pstmt.setString(2, as_item_code);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			lc_var_perc = rs.getDouble(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if (lc_var_perc==0)
		{
			sql = " SELECT varience_qtyper  FROM CUSTOMER_SERIES " +
					" WHERE ( CUST_CODE = ? ) AND ( ITEM_SER = ? )   ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, as_cust_code);
			pstmt.setString(2, as_item_ser);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				lc_var_perc = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}

		if (lc_var_perc==0)
		{
			sql = " SELECT varience_qtyper  FROM item " +
					"	WHERE  item_code = ?   ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, as_item_code);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				lc_var_perc = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}

		if (lc_var_perc==0)
		{
			sql = " SELECT varience_qtyper	FROM itemser WHERE  item_ser = ?   ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, as_item_ser);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				lc_var_perc = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}

		if (lc_var_perc==0)
		{
			ls_val= distCommon.getDisparams("999999", "OVER_DESP_PERC", conn);
			if( !  "NULLFOUND".equalsIgnoreCase(ls_val)) 
			{
				lc_var_perc = Double.parseDouble(checkDoubleNull(ls_val));
			}
		}

		return lc_var_perc;
			}

	private double getPendingQty( Document dom,Document dom2,Connection conn) throws NumberFormatException, ITMException, SQLException 
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql="";

		String ls_SaleOrd="", ls_LineNoSord="", ls_ExpLev="", ls_SaleOrd1="", ls_LineNoSord1="", ls_ExpLev1="", Ls_DespId="", ls_ItemCode="";
		String ls_CustCode="", ls_ItemSer="", ls_lineno="", ls_LineNo1="";
		double lc_Qty=0, lc_Qty1=0, lc_PendingQty=0, lc_TotQty=0, lc_SordQty=0, lc_OverShipPerc=0, unConfirmDespQty = 0;
		int ll_i=0, ll_CurrRow=0;

		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		boolean isUpdated=false;
		lc_Qty = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));
		ls_SaleOrd = checkNull(genericUtility.getColumnValue("sord_no", dom));
		ls_LineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

		ls_LineNoSord = "   "+ls_LineNoSord;
		ls_LineNoSord = ls_LineNoSord.substring(ls_LineNoSord.length()-3,ls_LineNoSord.length());

		ls_ExpLev = checkNull(genericUtility.getColumnValue("exp_lev", dom));
		ls_lineno = checkNull(genericUtility.getColumnValue("line_no", dom));
		Ls_DespId = checkNull(genericUtility.getColumnValue("desp_id", dom));

		if( isnull(Ls_DespId) || Ls_DespId.trim().length() == 0)
		{
			Ls_DespId = "@@@@@@";
		}
		//changes by nandkumar Gadkari on 10/04/18----------START ----------
		sql = " select sum(quantity),sum(qty_desp) from sorditem " +
				"	where  sale_order = ? " +
				"	and    line_no 	= ? " +
				"	and    exp_lev 	= ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ls_SaleOrd);
		pstmt.setString(2, ls_LineNoSord);
		pstmt.setString(3, ls_ExpLev);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			lc_SordQty = rs.getDouble(1);
			lc_TotQty = rs.getDouble(2);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		//System.out.println("@@@@@2754 lc_SordQty["+lc_SordQty+"]");
		// 02-Feb-2021 manoharan consider unconfirmed despatch quantity other then current despatch
		sql = "	select sum(d.quantity)  from   despatchdet d, despatch h  " +
				" where  d.desp_id =  h.desp_id and d.sord_no = ? 	and    d.line_no__sord = ? " +
				"	and  h.confirmed = 'N' and  d.exp_lev = ? 	and 	 h.desp_id <> ?  ";						
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ls_SaleOrd);
		pstmt.setString(2, ls_LineNoSord);
		pstmt.setString(3, ls_ExpLev);
		pstmt.setString(4, Ls_DespId);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			unConfirmDespQty = rs.getDouble(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		lc_TotQty =  lc_TotQty + unConfirmDespQty;
		// end 02-Feb-2021 manoharan consider unconfirmed despatch quantity other then current despatch
		
		//changes by nandkumar Gadkari on 10/04/18--------------END-----------------
		//System.out.println("@@@@@2754 lc_TotQty["+lc_TotQty+"]");

		parentList = dom2.getElementsByTagName("Detail2");
		int parentNodeListLength = parentList.getLength();
		for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr-- )
		{	
			parentNode = parentList.item(prntCtr-1);
			childList = parentNode.getChildNodes();
			for (int ctr = 0; ctr < childList.getLength(); ctr++)
			{
				childNode = childList.item(ctr);
				//added by nandkumar gadkari on 03/01/19-------------added attribute condition 
				if ( childNode.getNodeName().equalsIgnoreCase("attribute") )
				{
						String updateNodeValue = childNode.getAttributes().getNamedItem("updateFlag").getFirstChild().getNodeValue();
						//System.out.println("updateNodeValue["+updateNodeValue+"]");
						if(! "D".equalsIgnoreCase(updateNodeValue))
					{
						isUpdated = true;
					}
						else
					{
						isUpdated = false;
						lc_Qty1=0;
					}
				}

				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("sord_no") )
				{
					ls_SaleOrd1 = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("line_no__sord") )
				{
					ls_LineNoSord1 = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("exp_lev") )
				{
					ls_ExpLev1 = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("quantity") )
				{
					if(isUpdated)
					{
						isUpdated=false;
						lc_Qty1 = Double.parseDouble(checkDoubleNull( childNode.getFirstChild().getNodeValue().trim()));
					}
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("line_no") )
				{
					ls_LineNo1 = childNode.getFirstChild().getNodeValue().trim();
				}

				ls_LineNoSord1 = "   "+ls_LineNoSord1;
				ls_LineNoSord1 = ls_LineNoSord1.substring(ls_LineNoSord1.length()-3,ls_LineNoSord1.length());

			}

			//System.out.println("@@@@@2826 lc_TotQty["+lc_TotQty+"]lc_Qty1["+lc_Qty1+"]");
			//System.out.println("@@@@@ ls_SaleOrd["+ls_SaleOrd+"]ls_SaleOrd1["+ls_SaleOrd1+"]");
			//System.out.println("@@@@@ ls_LineNoSord["+ls_LineNoSord+"]ls_LineNoSord1["+ls_LineNoSord1+"]");
			//System.out.println("@@@@@ ls_ExpLev["+ls_ExpLev+"]ls_ExpLev1["+ls_ExpLev1+"]");
			//System.out.println("@@@@@ ls_LineNoSord1["+ls_LineNoSord1+"]ls_LineNo1["+ls_LineNo1+"]");

			if( ls_SaleOrd.trim().equalsIgnoreCase(ls_SaleOrd1.trim()) && ls_LineNoSord.trim().equalsIgnoreCase(ls_LineNoSord1.trim()) && ls_ExpLev.trim().equalsIgnoreCase(ls_ExpLev1.trim()) && (!ls_lineno.trim().equalsIgnoreCase(ls_LineNo1.trim()) ) )														
			{
				lc_TotQty	= lc_TotQty + lc_Qty1;
				//System.out.println("@@@@@2821 lc_TotQty["+lc_TotQty+"]");
			}
		}//for loop

		//System.out.println("@@@@@2826 lc_TotQty["+lc_TotQty+"]lc_Qty["+lc_Qty+"]");
		lc_TotQty = lc_TotQty + lc_Qty;
		//System.out.println("@@@@@2826 lc_SordQty["+lc_SordQty+"]lc_TotQty["+lc_TotQty+"]");
		lc_PendingQty = lc_SordQty - lc_TotQty;
		//System.out.println("@@@@@2826 lc_PendingQty["+lc_PendingQty+"]");
		return lc_PendingQty;
	}
	private double getDespQty( Document dom,Document dom2,Connection conn) throws NumberFormatException, ITMException, SQLException 
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql="";

		String ls_SaleOrd="", ls_LineNoSord="", ls_ExpLev="", ls_SaleOrd1="", ls_LineNoSord1="", ls_ExpLev1="", Ls_DespId="", ls_ItemCode="";
		String ls_CustCode="", ls_ItemSer="", ls_lineno="", ls_LineNo1="";
		double lc_Qty=0, lc_Qty1=0, lc_PendingQty=0, lc_TotQty=0, lc_SordQty=0, lc_OverShipPerc=0, unConfirmDespQty = 0;
		int ll_i=0, ll_CurrRow=0;

		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		boolean isUpdated=false;
		lc_Qty = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));
		ls_SaleOrd = checkNull(genericUtility.getColumnValue("sord_no", dom));
		ls_LineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

		ls_LineNoSord = "   "+ls_LineNoSord;
		ls_LineNoSord = ls_LineNoSord.substring(ls_LineNoSord.length()-3,ls_LineNoSord.length());

		ls_ExpLev = checkNull(genericUtility.getColumnValue("exp_lev", dom));
		ls_lineno = checkNull(genericUtility.getColumnValue("line_no", dom));
		Ls_DespId = checkNull(genericUtility.getColumnValue("desp_id", dom));

		if( isnull(Ls_DespId) || Ls_DespId.trim().length() == 0)
		{
			Ls_DespId = "@@@@@@";
		}
		//changes by nandkumar Gadkari on 10/04/18----------START ----------
		sql = " select sum(quantity),sum(qty_desp) from sorditem " +
				"	where  sale_order = ? " +
				"	and    line_no 	= ? " +
				"	and    exp_lev 	= ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ls_SaleOrd);
		pstmt.setString(2, ls_LineNoSord);
		pstmt.setString(3, ls_ExpLev);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			lc_SordQty = rs.getDouble(1);
			lc_TotQty = rs.getDouble(2);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		//System.out.println("@@@@@2754 lc_SordQty["+lc_SordQty+"]");
		// 02-Feb-2021 manoharan consider unconfirmed despatch quantity other then current despatch
		sql = "	select sum(d.quantity)  from   despatchdet d, despatch h  " +
				" where  d.desp_id =  h.desp_id and d.sord_no = ? 	and    d.line_no__sord = ? " +
				"	and  h.confirmed = 'N' and  d.exp_lev = ? 	and 	 h.desp_id <> ?  ";						
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ls_SaleOrd);
		pstmt.setString(2, ls_LineNoSord);
		pstmt.setString(3, ls_ExpLev);
		pstmt.setString(4, Ls_DespId);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			unConfirmDespQty = rs.getDouble(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		lc_TotQty =  lc_TotQty + unConfirmDespQty;
		// end 02-Feb-2021 manoharan consider unconfirmed despatch quantity other then current despatch
		
		//changes by nandkumar Gadkari on 10/04/18--------------END-----------------
		//System.out.println("@@@@@2754 lc_TotQty["+lc_TotQty+"]");

		parentList = dom2.getElementsByTagName("Detail2");
		int parentNodeListLength = parentList.getLength();
		for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr-- )
		{	
			parentNode = parentList.item(prntCtr-1);
			childList = parentNode.getChildNodes();
			for (int ctr = 0; ctr < childList.getLength(); ctr++)
			{
				childNode = childList.item(ctr);
				//added by nandkumar gadkari on 03/01/19-------------added attribute condition 
				if ( childNode.getNodeName().equalsIgnoreCase("attribute") )
				{
						String updateNodeValue = childNode.getAttributes().getNamedItem("updateFlag").getFirstChild().getNodeValue();
						//System.out.println("updateNodeValue["+updateNodeValue+"]");
						if(! "D".equalsIgnoreCase(updateNodeValue))
					{
						isUpdated = true;
					}
						else
					{
						isUpdated = false;
						lc_Qty1=0;
					}
				}

				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("sord_no") )
				{
					ls_SaleOrd1 = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("line_no__sord") )
				{
					ls_LineNoSord1 = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("exp_lev") )
				{
					ls_ExpLev1 = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("quantity") )
				{
					if(isUpdated)
					{
						isUpdated=false;
						lc_Qty1 = Double.parseDouble(checkDoubleNull( childNode.getFirstChild().getNodeValue().trim()));
					}
				}
				if ( childNode != null && childNode.getFirstChild() != null 
						&&  childNode.getNodeName().equalsIgnoreCase("line_no") )
				{
					ls_LineNo1 = childNode.getFirstChild().getNodeValue().trim();
				}

				ls_LineNoSord1 = "   "+ls_LineNoSord1;
				ls_LineNoSord1 = ls_LineNoSord1.substring(ls_LineNoSord1.length()-3,ls_LineNoSord1.length());

			}

			//System.out.println("@@@@@2826 lc_TotQty["+lc_TotQty+"]lc_Qty1["+lc_Qty1+"]");
			//System.out.println("@@@@@ ls_SaleOrd["+ls_SaleOrd+"]ls_SaleOrd1["+ls_SaleOrd1+"]");
			//System.out.println("@@@@@ ls_LineNoSord["+ls_LineNoSord+"]ls_LineNoSord1["+ls_LineNoSord1+"]");
			//System.out.println("@@@@@ ls_ExpLev["+ls_ExpLev+"]ls_ExpLev1["+ls_ExpLev1+"]");
			//System.out.println("@@@@@ ls_LineNoSord1["+ls_LineNoSord1+"]ls_LineNo1["+ls_LineNo1+"]");

			if( ls_SaleOrd.trim().equalsIgnoreCase(ls_SaleOrd1.trim()) && ls_LineNoSord.trim().equalsIgnoreCase(ls_LineNoSord1.trim()) && ls_ExpLev.trim().equalsIgnoreCase(ls_ExpLev1.trim()) && (!ls_lineno.trim().equalsIgnoreCase(ls_LineNo1.trim()) ) )														
			{
				lc_TotQty	= lc_TotQty + lc_Qty1;
				//System.out.println("@@@@@2821 lc_TotQty["+lc_TotQty+"]");
			}
		}//for loop

		//System.out.println("@@@@@2826 lc_TotQty["+lc_TotQty+"]lc_Qty["+lc_Qty+"]");
		lc_TotQty = lc_TotQty + lc_Qty;
		//System.out.println("@@@@@2826 lc_PendingQty["+lc_PendingQty+"]");
		return lc_TotQty;
	}


	private String gbf_comp_sord(String as_sale_order, Document dom, Connection conn) throws ITMException, SQLException 
	{

		String ls_value="", ls_errcode="", ls_site_code="",ls_trans_mode="",sql="";
		String ls_cust_code="",ls_cust_code__dlv="",ls_item_ser="",ls_cr_term="",ls_dlv_term="",ls_order_type="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		sql = " select cust_code , cust_code__dlv , item_ser , cr_term , dlv_term , order_type , trans_mode " +
				"	from   sorder where  sale_order = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, as_sale_order);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			ls_cust_code = checkNull(rs.getString("cust_code"));
			ls_cust_code__dlv = checkNull(rs.getString("cust_code__dlv"));
			ls_item_ser = checkNull(rs.getString("item_ser"));
			ls_cr_term = checkNull(rs.getString("cr_term"));
			ls_dlv_term = checkNull(rs.getString("dlv_term"));
			ls_order_type = checkNull(rs.getString("order_type"));
			ls_trans_mode = checkNull(rs.getString("trans_mode"));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;


		ls_value = ls_cust_code +  ls_cust_code__dlv + ls_item_ser + ls_cr_term  + ls_dlv_term  + ls_order_type  + ls_trans_mode;

		ls_site_code = checkNull(genericUtility.getColumnValue("site_code", dom));

		ls_value = ls_value + ls_site_code;

		return ls_value ;

	}

	private boolean isExist(String table, String field, String value, Connection conn) throws SQLException 
	{
		String sql = "";
		boolean retStr = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if (cnt > 0) {
			retStr = true;
		}
		if (cnt == 0) {
			retStr = false;
		}
		//System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::["+ cnt + "]");
		return retStr;
	}

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		//System.out.println("itemChanged() called for DespatchManualIC.........");
		String valueXmlString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				//System.out.println("itemChanged() xmlString ["+xmlString+"].........");
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				//System.out.println("itemChanged() xmlString1 ["+xmlString1+"].........");
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				//System.out.println("itemChanged() xmlString2 ["+xmlString2+"].........");
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			//System.out.println("Exception : [DespatchManualIC][itemChanged( String, String )] :==>\n"	+ e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		String childNodeName = null,childNodeName1="";
		String sql = "",columnValue="";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0,cnt=0;

		String autoConf="N",descr="",sordNo="",despId="",allocFlag="",siteCode="",partQty="";
		boolean isExistFlag = false;
		Timestamp orderDate=null,licenceDate1=null,licenceDate2=null,licenceDate3=null,drugLicNo1Upto=null;
		String custCode="",custCodeDlv="",tranCode="",  stanCode="", dlvAdd1="", dlvCity="", dlvAdd2="", dlvPin="", countCodeDlv="";
		String currCode="",  stanCodeInit="", orderType="", transMode="", currCodeFrt="", remarks="", remarks2="", remarks3="";
		String dlvAdd3="",  dlvTo="", distRoute="",currCodeIns="", licenceNo1="", licenceNo2="", licenceNo3="";
		String siteCodeShip="", stateCodeDlv="", availableYn="",gpSer="",stanDescInit="",siteDescr="",siteFinEntity="";		
		double exchRate=0, exchRateFrt=0, frtAmt=0,exchRateIns=0,insAmt=0,exchRateF=0;
		String finentCurrCode="", despDateStr="",custCodeDlv1="",tranName="",varValue="",gpNo="",stanDescr="",rdPermitNo="",stateTo="";
		String stateFrom="",custName="",tranCode1="",stanCode1="",shName="",stationDescr="",exchRateStr="",roadpermitDescr="";
		String lorryNo="",billNo="",lrNo="",shipmentId="",tranCodeDesp="",stanCodeDest="",stationDescrDest="",licenceDate2Str="",licenceDate3Str="";
		double nettWeight=0,tareWeight=0,grossWeight=0,despatchedQty=0,orderQty=0,pendingQty=0,minusQty=0,balQty=0;
		Timestamp lrDate= null,billDate=null;	
		String lorryNoDesp="",lrNoDesp="",lrDateDesp="",billNoDesp="",billDateDesp="",orderDateStr="",billDateStr="",licenceDate1Str="";

		String lineNoSord="",reStr="",custItemRef="",SOalloc="",itemCodeOrd="",expLev="",unit="",lotSl="",lotNo="",unitStd="",packInstr="",noArt="";
		String nature="",lineType="",itemcodeFree="",partNo="";
		String siteCodeDet ="",taxClass="",taxChap="",taxChap1="",taxEnv="",itemCode="",packCode="",loc="",listType="",itemRef="",applyPrice="",priceVar="",_priceVar="";		
		double discount=0,rateStduom=0,rateClg=0,quantity=0,qtyAlloc=0,convQtyStduom=0,ordQty=0,quantityStduom=0,quantityReal=0,packQty=0,rate=0,totRate=0;
		int pos=0;
		double diffRate=0,itemRate=0,discAmt=0,effCost=0,offRate=0,bbRate=0;

		String mcode="",mVal1="",mVal="",mdescr2="",mstunit="",mUnit="",itemcode="",mitemdesc="",mdescr1="";
		double mNum2=0,mNum=0,mNum1=0,lc_pack_qty=0,mNum3=0,ld_despatched_qty=0,ld_order_qty=0;
		double ld_pending_qty=0,ld_minus_qty=0,lc_item_rate=0,lc_tot_rate=0,lc_ratestd=0,lc_diff_rate=0;
		String ls_desp_id="",ls_list_type="",ls_item_ref="",ls_apply_price="",ls_price_var="";
		double lc_disc_amt=0,lc_eff_cost=0,lc_qty=0,ll_no_art=0,lc_qty_per_art=0,lc_no_article=0,lc_shipper_qty=0,lc_disc_merge=0;
		String ls_nature="",ls_sale_order="",ls_site_code="",ls_line_no__sord="",ls_sordatt_no="",ls_item_code="",ls_lot_no="";
		String ls_cust_code="",ls_line_no="",ls_alt_item="",ls_site_code_det="",ls_pack_code="",ls_loc_code="",ls_lot_sl="";
		double ac_shipper_qty=0,lc_gross_wt=0,lc_nett_wt=0,lc_gross_weight=0,lc_disc_perc=0,mdesc_offinv_amt=0,mdesc_bb_amt=0,lc_rate=0;
		double lc_int_qty=0,ll_no_art1=0,ll_no_art2=0,lc_bal_qty=0,ac_int_qty=0,lc_loose_qty=0,lc_tare_weight=0,lc_nett_weight=0;
		String ls_str="",ls_item_code__ord="",ls_saleord="",ls_linenosord="",ls_saleorder="",ls_saleord_line="",ls_unitstd="";
		String ls_pack_instr="",ls_dimension="",ls_site_mfg  =  "", ls_stk_opt="",ls_track_shelf_life="",ld_exp_date_str="",ld_mfg_date_str="";
		double lc_stcrate=0,lc_gross_weight_art=0,lc_tare_weight_art=0,lc_pallet_wt =  0,lc_pick_rate__clg=0;
		String ldt_retest_date_str="",ls_explev="",ls_item_cd="",ls_item="",ls_custitemdesc="",mdescr="";
		double lc_rate_std=0,lc_sord_rate=0,lc_sord_exc_rate=0,lc_sord_quantity=0,lc_pick_rate=0,lc_plist_disc=0,lc_conv=0;
		Timestamp ld_desp_date=null,ldt_retest_date =null,ld_mfg_date=null,ld_exp_date =null,ld_plist_date=null,ld_order_date=null;
		String ls_price_list="",ls_plist_disc="",ls_custcode="",ls_price_list__clg="",ls_price_list__parent="" ;
		int ll_count=0;	
		double lc_qty_stduom=0;
		DistCommon distCommon = new DistCommon();		
		FinCommon finCommon = new FinCommon();
		ArrayList mNumList = new ArrayList();
		ArrayList getNoArtList = new ArrayList();
		ArrayList quantityStduomList = new ArrayList();
		ArrayList quantityRealList = new ArrayList();

		String lrDateStr="";
		
		
		double tempTotalNetAmt=0, totalNetAmt=0;
		NodeList childList = null;
		NodeList parentList = null;
		NodeList childList1 = null;
		Node parentNode1 = null;
		Node childNode1 = null;
		int childNodeListLength1=0,curlineno=0;
		String linenoStr="";
		double quantityTemp = 0,rateStduomTemp = 0,taxAmtTemp = 0,discAmtTemp = 0,currtotalNetAmt=0;
		boolean cpFlag = false;
		
		
		try {
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDate);

			SimpleDateFormat sdf1 = new SimpleDateFormat(
					genericUtility.getApplDateTimeFormat());
			String sysDate1 = sdf1.format(currentDate.getTime());
			//System.out.println("Now the date with time :=>  [" + sysDate1+"]");


			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection() ;
			conn.setAutoCommit(false);
			connDriver = null;

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			valueXmlString = new StringBuffer(
					"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			//System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo+ "**************");
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail1>");
				//int childNodeListLength = childNodeList.getLength();

				//System.out.println("[" + currentColumn + "] ==> '"+ columnValue + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					valueXmlString.append("<desp_date>").append("<![CDATA[" + sysDate + "]]>").append("</desp_date>");
					valueXmlString.append("<eff_date>").append("<![CDATA[" + sysDate + "]]>").append("</eff_date>");
					valueXmlString.append("<gp_date>").append("<![CDATA[" + sysDate1 + "]]>").append("</gp_date>");
					if("Y".equalsIgnoreCase(autoConf)) 
					{
						valueXmlString.append("<confirmed>").append("<![CDATA[Y]]>").append("</confirmed>");
						valueXmlString.append("<conf_date>").append("<![CDATA[" + sysDate + "]]>").append("</conf_date>");
					}
					else
					{
						valueXmlString.append("<confirmed>").append("<![CDATA[N]]>").append("</confirmed>");
					}

				} 
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					despId = genericUtility.getColumnValue("desp_id", dom);
					sordNo = genericUtility.getColumnValue("sord_no", dom);
					
					isExistFlag =  isExist("despatch", "desp_id", despId, conn);

					if(isExistFlag)
					{
						valueXmlString.append("<sord_no protect = '1'>").append("<![CDATA[" + sordNo + "]]>").append("</sord_no>");
					}
					else
					{
						valueXmlString.append("<sord_no protect = '0'>").append("<![CDATA[" + sordNo + "]]>").append("</sord_no>");
					}
					//Modified by Rohini T on 29/06/2021[Start]
					boolean isExistLine =  isExist("despatchdet", "desp_id", despId, conn);
					System.out.println("isExistLine...."+isExistLine);
					if(isExistLine)
					{
						valueXmlString.append("<sord_no protect = '1'>").append("<![CDATA[" + sordNo + "]]>").append("</sord_no>");
						System.out.println("Protected..");
					}
					else
					{
						valueXmlString.append("<sord_no protect = '0'>").append("<![CDATA[" + sordNo + "]]>").append("</sord_no>");
					}
					//Modified by Rohini T on 29/06/2021[End]
				} 
				else if (currentColumn.trim().equalsIgnoreCase("sord_no"))
				{
					sordNo = genericUtility.getColumnValue("sord_no", dom);
					despId = genericUtility.getColumnValue("desp_id", dom);

					sql = " select alloc_flag, part_qty, site_code " +
							"	from sorder where confirmed = 'Y' " +
							" and status = 'P' " +
							" and sale_order = ?  ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						allocFlag = checkNull(rs.getString("alloc_flag"));
						partQty = checkNull(rs.getString("part_qty"));
						siteCode = checkNull(rs.getString("site_code"));

					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					/*
					if("Y".equalsIgnoreCase(descr))
					{	
						sql = " select count(1) " +
								" from sordalloc" +
								" where sale_order = :mcode " +
								" and status <> 'D' " +
								" and site_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, siteCode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							cnt =  rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						cnt = 1	;
					}	
					else
					{		
						descr = "";
						cnt = 1;
					}

					 */
					sql = " select  order_date,	cust_code		,	cust_code__dlv	,	tran_code, "+
							" stan_code		,	dlv_add1			,	dlv_add2			,	dlv_city	, "+
							" dlv_pin			,	count_code__dlv,	curr_code		,	exch_rate,	"+
							" stan_code__init, 	order_type		,	trans_mode		,	curr_code__frt,"+	
							" exch_rate__frt	,	frt_amt			,	curr_code__ins	,	exch_rate__ins,	"+
							" ins_amt			, 	remarks			,	remarks2			,	remarks3,	dlv_add3 ,"+
							" dlv_to			,	dist_route		,	licence_no_1	,	licence_date_1,"+
							" licence_no_2	,	licence_date_2 ,	licence_no_3	,licence_date_3, "+
							" site_code__ship,STATE_CODE__DLV  ,  available_yn "+
							//" from sorder where sale_order = :mcode  "+
							" from sorder where sale_order = ?  "+ // change by Nasruddin 
							" and confirmed = 'Y' "; 
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						orderDate = rs.getTimestamp("order_date");
						custCode = checkNull(rs.getString("cust_code"));
						custCodeDlv = checkNull(rs.getString("cust_code__dlv"));
						tranCode = checkNull(rs.getString("tran_code")); 
						stanCode = checkNull(rs.getString("stan_code"));
						dlvAdd1 = checkNull(rs.getString("dlv_add1"));
						dlvAdd2 = checkNull(rs.getString("dlv_add2"));
						dlvCity = checkNull(rs.getString("dlv_city")); 
						dlvPin = checkNull(rs.getString("dlv_pin"));
						countCodeDlv = checkNull(rs.getString("count_code__dlv"));
						currCode = checkNull(rs.getString("curr_code"));
						exchRate = rs.getDouble("exch_rate");	
						stanCodeInit = checkNull(rs.getString("stan_code__init"));
						orderType = checkNull(rs.getString("order_type"));
						transMode = checkNull(rs.getString("trans_mode"));
						currCodeFrt = checkNull(rs.getString("curr_code__frt"));	
						exchRateFrt = rs.getDouble("exch_rate__frt");
						frtAmt = rs.getDouble("frt_amt");
						currCodeIns = checkNull(rs.getString("curr_code__ins"));
						exchRateIns = rs.getDouble("exch_rate__ins");	
						insAmt = rs.getDouble("ins_amt");
						remarks = checkNull(rs.getString("remarks"));
						remarks2 = checkNull(rs.getString("remarks2"));
						remarks3 = checkNull(rs.getString("remarks3"));
						dlvAdd3 = checkNull(rs.getString("dlv_add3"));
						dlvTo = checkNull(rs.getString("dlv_to"));
						distRoute = checkNull(rs.getString("dist_route"));
						licenceNo1 = checkNull(rs.getString("licence_no_1"));
						licenceDate1 = rs.getTimestamp("licence_date_1");
						licenceNo2 = checkNull(rs.getString("licence_no_2"));
						licenceDate2 = rs.getTimestamp("licence_date_2");
						licenceNo3 = checkNull(rs.getString("licence_no_3"));
						licenceDate3 = rs.getTimestamp("licence_date_3"); 
						siteCodeShip = checkNull(rs.getString("site_code__ship"));
						stateCodeDlv = checkNull(rs.getString("state_code__dlv"));
						availableYn = checkNull(rs.getString("available_yn"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if( orderDate != null )
					{
						orderDateStr = sdf.format(orderDate.getTime());
						valueXmlString.append("<sord_date>").append("<![CDATA[" + orderDateStr + "]]>").append("</sord_date>");
					}
					else
					{
						valueXmlString.append("<sord_date>").append("<![CDATA[]]>").append("</sord_date>");

					}
					valueXmlString.append("<cust_code>").append("<![CDATA[" + custCode + "]]>").append("</cust_code>");
					valueXmlString.append("<available_yn>").append("<![CDATA[" + availableYn + "]]>").append("</available_yn>");
					valueXmlString.append("<cust_code__dlv>").append("").append("</cust_code__dlv>");

					custName = getValue("cust_name", "customer", "cust_code", custCode, conn);
					valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");
					valueXmlString.append("<curr_code>").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");
					valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + currCodeFrt + "]]>").append("</curr_code__frt>");
					valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + exchRateFrt + "]]>").append("</exch_rate__frt>");
					valueXmlString.append("<freight>").append("<![CDATA[" + frtAmt + "]]>").append("</freight>");
					valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + currCodeIns + "]]>").append("</curr_code__ins>");
					valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + exchRateIns + "]]>").append("</exch_rate__ins>");
					valueXmlString.append("<insurance>").append("<![CDATA[" + insAmt + "]]>").append("</insurance>");
					valueXmlString.append("<remarks2>").append("<![CDATA[" + remarks2 + "]]>").append("</remarks2>");
					valueXmlString.append("<remarks3>").append("<![CDATA[" + remarks3 + "]]>").append("</remarks3>");
					valueXmlString.append("<dist_route>").append("<![CDATA[" + distRoute + "]]>").append("</dist_route>");

					if( isnull(licenceNo1) || lenTrim(licenceNo1)== 0 )
					{
						sql = " select c.drug_lic_no_1,c.drug_licno1_upto" +
								"	from sorder s,customer c " +
								" where s.cust_code__dlv = c.cust_code " +
								" and s.sale_order = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							licenceNo1 = checkNull(rs.getString("drug_lic_no_1"));
							licenceDate1 = rs.getTimestamp("drug_licno1_upto");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					}

					if( isnull(licenceNo2) || lenTrim(licenceNo2)== 0 )
					{ 
						sql = " select c.drug_lic_no_1,c.drug_licno1_upto "+
								" from sorder s,customer c "+
								" where s.cust_code__bil = c.cust_code "+ 
								" and s.sale_order = ?  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							licenceNo2 = checkNull(rs.getString("drug_lic_no_1"));
							licenceDate2 = rs.getTimestamp("drug_licno1_upto");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					}

					valueXmlString.append("<licence_no_1>").append("<![CDATA[" + licenceNo1 + "]]>").append("</licence_no_1>");
					if( licenceDate1 != null )
					{
						licenceDate1Str = sdf.format(licenceDate1.getTime());
						valueXmlString.append("<licence_date_1>").append("<![CDATA[" + licenceDate1Str + "]]>").append("</licence_date_1>");
					}
					else
					{
						valueXmlString.append("<licence_date_1>").append("<![CDATA[]]>").append("</licence_date_1>");
					}
					valueXmlString.append("<licence_no_2>").append("<![CDATA[" + licenceNo2 + "]]>").append("</licence_no_2>");
					if( licenceDate2 != null )
					{
						licenceDate2Str = sdf.format(licenceDate2.getTime());
						valueXmlString.append("<licence_date_2>").append("<![CDATA[" + licenceDate2Str + "]]>").append("</licence_date_2>");
					}
					else
					{
						valueXmlString.append("<licence_date_2>").append("<![CDATA[]]>").append("</licence_date_2>");
					}
					valueXmlString.append("<licence_no_3>").append("<![CDATA[" + licenceNo3 + "]]>").append("</licence_no_3>");
					if( licenceDate3 != null )
					{
						licenceDate3Str = sdf.format(licenceDate3.getTime());
						valueXmlString.append("<licence_date_3>").append("<![CDATA[" + licenceDate3Str + "]]>").append("</licence_date_3>");
					}
					else
					{
						valueXmlString.append("<licence_date_3>").append("<![CDATA[]]>").append("</licence_date_3>");
					}
					valueXmlString.append("<state_code__dlv>").append("<![CDATA[" + stateCodeDlv + "]]>").append("</state_code__dlv>");

					gpSer = getValue("gp_ser", "sordertype", "order_type", orderType, conn);
					//System.out.println("@@@@ gpSer["+gpSer+"]");
					if(( isnull(gpSer) || lenTrim(gpSer) == 0 ) && ( orderType != null && lenTrim(orderType) > 0 ))
					{
						//ls_gp_ser = left(trim(ls_order_type),1)
						gpSer = orderType.substring(0, 1);
					}
					//System.out.println("@@@@ gpSer["+gpSer+"]");
					valueXmlString.append("<gp_ser>").append("<![CDATA[" + gpSer + "]]>").append("</gp_ser>");

					valueXmlString.append("<trans_mode>").append("<![CDATA[" + transMode + "]]>").append("</trans_mode>");

					if( ! isnull(stanCodeInit) && lenTrim(stanCodeInit) > 0 )
					{
						valueXmlString.append("<stan_code__init>").append("<![CDATA[" + stanCodeInit + "]]>").append("</stan_code__init>");

						stanDescInit = getValue("descr", "station", "stan_code", stanCodeInit, conn);
						valueXmlString.append("<station_desc__init>").append("<![CDATA[" + stanDescInit + "]]>").append("</station_desc__init>");
					}	  

					valueXmlString.append("<site_code>").append("<![CDATA[" + siteCodeShip + "]]>").append("</site_code>");

					siteDescr = getValue("descr", "site", "site_code", siteCode, conn);
					valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>");

					siteFinEntity = getValue("fin_entity", "site", "site_code", siteCodeShip, conn);
					finentCurrCode = getValue("curr_code", "finent", "fin_entity", siteFinEntity, conn);

					despDateStr = genericUtility.getColumnValue("desp_date", dom);

					exchRateF = finCommon.getDailyExchRateSellBuy(currCode, finentCurrCode, siteCode, despDateStr, "S", conn); 		

					if(exchRateF == 0 )
					{
						valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
					}
					else
					{
						valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRateF + "]]>").append("</exch_rate>");
					}

					exchRateF = finCommon.getDailyExchRateSellBuy(currCodeFrt, finentCurrCode, siteCode, despDateStr, "S", conn);		

					if (exchRateF > 0 )
					{	
						valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + exchRateF + "]]>").append("</exch_rate__frt>");
					}							

					exchRateF = finCommon.getDailyExchRateSellBuy(currCodeIns, finentCurrCode, siteCode, despDateStr, "S", conn);		
					if( exchRateF > 0 )
					{
						valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + exchRateF + "]]>").append("</exch_rate__ins>");
					}


					custCodeDlv1 = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					if( isnull(custCodeDlv1) || lenTrim(custCodeDlv1) == 0 ) 
					{		
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + custCodeDlv + "]]>").append("</cust_code__dlv>");
						valueXmlString.append("<dlv_add1>").append("<![CDATA[" + dlvAdd1 + "]]>").append("</dlv_add1>");
						valueXmlString.append("<dlv_add2>").append("<![CDATA[" + dlvAdd2 + "]]>").append("</dlv_add2>");
						valueXmlString.append("<dlv_add3>").append("<![CDATA[" + dlvAdd3 + "]]>").append("</dlv_add3>");
						valueXmlString.append("<dlv_city>").append("<![CDATA[" + dlvCity + "]]>").append("</dlv_city>");
						valueXmlString.append("<dlv_pin>").append("<![CDATA[" + dlvPin + "]]>").append("</dlv_pin>");
						valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countCodeDlv + "]]>").append("</count_code__dlv>");

						valueXmlString.append("<dlv_to>").append("<![CDATA[" + dlvTo + "]]>").append("</dlv_to>");
					}	
					else 
					{		
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + custCodeDlv + "]]>").append("</cust_code__dlv>");
						valueXmlString.append("<dlv_add1>").append("<![CDATA[" + dlvAdd1 + "]]>").append("</dlv_add1>");
						valueXmlString.append("<dlv_add2>").append("<![CDATA[" + dlvAdd2 + "]]>").append("</dlv_add2>");
						valueXmlString.append("<dlv_add3>").append("<![CDATA[" + dlvAdd3 + "]]>").append("</dlv_add3>");
						valueXmlString.append("<dlv_city>").append("<![CDATA[" + dlvCity + "]]>").append("</dlv_city>");
						valueXmlString.append("<dlv_pin>").append("<![CDATA[" + dlvPin + "]]>").append("</dlv_pin>");
						valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countCodeDlv + "]]>").append("</count_code__dlv>");
						
						valueXmlString.append("<dlv_to>").append("<![CDATA[" + dlvTo + "]]>").append("</dlv_to>");
					}	
					valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");

					tranName = getValue("tran_name", "transporter", "tran_code", tranCode, conn);
					valueXmlString.append("<sh_name>").append("<![CDATA[" + tranName + "]]>").append("</sh_name>");

					valueXmlString.append("<stan_code>").append("<![CDATA[" + stanCode + "]]>").append("</stan_code>");

					stanDescr = getValue("descr", "station", "stan_code", stanCode, conn);
					valueXmlString.append("<station_descr>").append("<![CDATA[" + stanDescr + "]]>").append("</station_descr>");
					valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");

					varValue = distCommon.getDisparams("999999", "GP_NO_PROTECT", conn);
					gpNo = checkNull(genericUtility.getColumnValue("gp_no", dom));

					if("Y".equalsIgnoreCase(varValue))
					{		
						valueXmlString.append("<gp_no protect = '1'>").append("<![CDATA[" + gpNo + "]]>").append("</gp_no>");
					}
					else if("N".equalsIgnoreCase(varValue))
					{
						valueXmlString.append("<gp_no protect = '0'>").append("<![CDATA[" + gpNo + "]]>").append("</gp_no>");
					}

					siteCode = checkNull( genericUtility.getColumnValue("site_code", dom));
					stateTo = checkNull(genericUtility.getColumnValue("state_code__dlv", dom));
					rdPermitNo = checkNull(genericUtility.getColumnValue("rd_permit_no", dom));

					stateFrom = getValue("state_code", "site", "site_code", siteCode, conn);

					if( stateFrom.equalsIgnoreCase(stateTo))
					{	
						valueXmlString.append("<rd_permit_no protect = '1'>").append("<![CDATA[" + rdPermitNo + "]]>").append("</rd_permit_no>");
					}else
					{
						valueXmlString.append("<rd_permit_no protect = '0'>").append("<![CDATA[" + rdPermitNo + "]]>").append("</rd_permit_no>");
					}
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");						
					}
					//Pavan Rane 11jun19 start
				} // end ic
				else if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
					despId = checkNull(genericUtility.getColumnValue("desp_id", dom));
					custCodeDlv1 = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
					tranCode1 = checkNull(genericUtility.getColumnValue("tran_code", dom));
					stanCode1 = checkNull(genericUtility.getColumnValue("stan_code", dom));

					siteDescr = getValue("descr", "site", "site_code", siteCode, conn);	
					valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>");

					sql = " Select alloc_flag, part_qty " +
							" from sorder "+
							" where sale_order = ? ";	
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						allocFlag = checkNull(rs.getString("alloc_flag"));
						partQty = checkNull(rs.getString("part_qty"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					/*if("Y".equalsIgnoreCase(allocFlag)) 
					{	

						sql = " select count(1) " +
								" from sordalloc " +
								" where sale_order = ? and status <> 'D' " +
								" and site_code = ? ";	
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, siteCode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;


						cnt = 1;
					}else
					{	
						cnt = 1;
					}
					 */
					sql = "	select order_date, cust_code, cust_code__dlv, tran_code, stan_code, dlv_add1, dlv_add2, dlv_city, dlv_pin, count_code__dlv, dlv_to " +
							" from sorder where sale_order = ? and confirmed = 'Y' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						orderDate = rs.getTimestamp("order_date");
						custCode = checkNull(rs.getString("cust_code"));
						custCodeDlv = checkNull(rs.getString("cust_code__dlv"));
						tranCode = checkNull(rs.getString("tran_code"));
						stanCode = checkNull(rs.getString("stan_code"));
						dlvAdd1 = checkNull(rs.getString("dlv_add1"));
						dlvAdd2 = checkNull(rs.getString("dlv_add2"));
						dlvCity = checkNull(rs.getString("dlv_city"));
						dlvPin = checkNull(rs.getString("dlv_pin"));
						countCodeDlv = checkNull(rs.getString("count_code__dlv"));
						dlvTo = checkNull(rs.getString("dlv_to"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if( orderDate != null )
					{
						orderDateStr = sdf.format(orderDate.getTime());
						valueXmlString.append("<sord_date>").append("<![CDATA[" + orderDateStr + "]]>").append("</sord_date>");
					}
					else
					{
						valueXmlString.append("<sord_date>").append("<![CDATA[]]>").append("</sord_date>");

					}
					valueXmlString.append("<cust_code>").append("<![CDATA[" + custCode + "]]>").append("</cust_code>");

					custName = getValue("cust_name", "customer", "cust_code", custCode, conn);

					valueXmlString.append("<cust_name>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");

					if( isnull(siteCode) || lenTrim(siteCode) == 0 ) 
					{
						valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode + "]]>").append("</site_code>");

						siteDescr = getValue("descr", "site", "site_code", siteCode, conn);
						valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>");
					} 


					if( isnull(custCodeDlv1) || lenTrim( custCodeDlv1 ) == 0 ) 
					{
						valueXmlString.append("<cust_code__dlv>").append("<![CDATA[" + custCodeDlv + "]]>").append("</cust_code__dlv>");
						valueXmlString.append("<dlv_add1>").append("<![CDATA[" + dlvAdd1 + "]]>").append("</dlv_add1>");
						valueXmlString.append("<dlv_add2>").append("<![CDATA[" + dlvAdd2 + "]]>").append("</dlv_add2>");
						valueXmlString.append("<dlv_city>").append("<![CDATA[" + dlvCity + "]]>").append("</dlv_city>");
						valueXmlString.append("<dlv_pin>").append("<![CDATA[" + dlvPin + "]]>").append("</dlv_pin>");
						valueXmlString.append("<count_code__dlv>").append("<![CDATA[" + countCodeDlv + "]]>").append("</count_code__dlv>");
						valueXmlString.append("<dlv_to>").append("<![CDATA[" + dlvTo + "]]>").append("</dlv_to>");

					}

					if( isnull(tranCode1) || lenTrim(tranCode1) == 0 ) 
					{		
						shName = getValue("sh_name", "transporter", "tran_code", tranCode, conn);
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
						valueXmlString.append("<sh_name>").append("<![CDATA[" + tranCode + "]]>").append("</sh_name>");
					}
					if( isnull(stanCode1) || lenTrim(stanCode1) == 0 ) 
					{		
						stationDescr = getValue("descr", "station", "stan_code", stanCode, conn);
						valueXmlString.append("<stan_code>").append("<![CDATA[" + stanCode + "]]>").append("</stan_code>");
						valueXmlString.append("<station_descr>").append("<![CDATA[" + stationDescr + "]]>").append("</station_descr>");
					}	

					stateFrom = getValue("state_code", "site", "site_code", siteCode, conn);

					stateTo = checkNull(genericUtility.getColumnValue("state_code__dlv", dom));
					rdPermitNo = checkNull(genericUtility.getColumnValue("rd_permit_no", dom));

					if( stateFrom.equalsIgnoreCase(stateTo))
					{				
						valueXmlString.append("<rd_permit_no protect = '1'>").append("<![CDATA[" + rdPermitNo + "]]>").append("</rd_permit_no>");
					}
					else
					{		
						valueXmlString.append("<rd_permit_no protect = '0'>").append("<![CDATA[" + rdPermitNo + "]]>").append("</rd_permit_no>");
					}										
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");						
					}
					//Pavan Rane 11jun19 end
				}
				else if (currentColumn.trim().equalsIgnoreCase("cust_code"))
				{
					custCode = genericUtility.getColumnValue("cust_code", dom);
					custName =  getValue("cust_name", "customer", "cust_code", custCode, conn);
					valueXmlString.append("<cust_name protect = '1'>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");										
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					siteCode = genericUtility.getColumnValue("site_code", dom);
					cpFlag = isChannelPartnerCust(custCode, siteCode, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");						
					}
					//Pavan Rane 11jun19 end
				} 
				else if (currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currCode = genericUtility.getColumnValue("curr_code", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom);
					sordNo = genericUtility.getColumnValue("sord_no", dom);
					exchRateStr = genericUtility.getColumnValue("exch_rate", dom);
					despDateStr = genericUtility.getColumnValue("desp_date", dom);

					custName =  getValue("cust_name", "customer", "cust_code", custCode, conn);
					valueXmlString.append("<cust_name protect = '1'>").append("<![CDATA[" + custName + "]]>").append("</cust_name>");

					siteFinEntity =  getValue("fin_entity", "site", "site_code", siteCode, conn);
					finentCurrCode =  getValue("curr_code", "finent", "fin_entity", siteFinEntity, conn);

					exchRateF = finCommon.getDailyExchRateSellBuy(currCode, finentCurrCode, siteCode, despDateStr, "S", conn); 
					if( exchRateF == 0 )
					{
						valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRateStr + "]]>").append("</exch_rate>");
					}
					else
					{
						valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRateF + "]]>").append("</exch_rate>");
					}

				} 

				else if (currentColumn.trim().equalsIgnoreCase("cust_code__dlv"))
				{
					custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));

					sql = "	select cust_name, tran_code " +
							" from customer where cust_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, custCodeDlv);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						custName = checkNull(rs.getString("cust_name"));
						tranCode = checkNull(rs.getString("tran_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<dlv_to protect = '1'>").append("<![CDATA[" + custName + "]]>").append("</dlv_to>");
					valueXmlString.append("<tran_code protect = '1'>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("stan_code"))
				{
					stanCode = genericUtility.getColumnValue("stan_code", dom);
					stationDescr =  getValue("descr", "station", "stan_code", stanCode, conn);
					valueXmlString.append("<station_descr protect = '1'>").append("<![CDATA[" + stationDescr + "]]>").append("</station_descr>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("stan_code__init"))
				{
					stanCodeInit = genericUtility.getColumnValue("stan_code__init", dom);
					stationDescr =  getValue("descr", "station", "stan_code", stanCodeInit, conn);
					valueXmlString.append("<station_desc__init protect = '1'>").append("<![CDATA[" + stationDescr + "]]>").append("</station_desc__init>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("state_code__dlv"))
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					stateTo = checkNull(genericUtility.getColumnValue("state_code__dlv", dom));
					rdPermitNo = checkNull(genericUtility.getColumnValue("rd_permit_no", dom));
					stateFrom =  getValue("state_code", "site", "site_code", siteCode, conn);

					if(stateFrom != null && stateFrom.equalsIgnoreCase(stateTo) )
					{
						valueXmlString.append("<rd_permit_no protect = '1'>").append("").append("</rd_permit_no>");
					}
					else
					{
						valueXmlString.append("<rd_permit_no protect = '0'>").append("<![CDATA[" + rdPermitNo + "]]>").append("</rd_permit_no>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("rd_permit_no"))
				{
					rdPermitNo = checkNull(genericUtility.getColumnValue("rd_permit_no", dom));

					if( ! isnull(rdPermitNo) && lenTrim(rdPermitNo) > 0 )
					{
						roadpermitDescr =  getValue("descr", "roadpermit", "rd_permit_no", rdPermitNo, conn);
						valueXmlString.append("<roadpermit_descr>").append("<![CDATA[" + roadpermitDescr + "]]>").append("</roadpermit_descr>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("desp_date"))
				{
					despDateStr = checkNull(genericUtility.getColumnValue("desp_date", dom));
					if( despDateStr != null && despDateStr.trim().length() > 0)
					{
						valueXmlString.append("<eff_date>").append("<![CDATA[" + despDateStr + "]]>").append("</eff_date>");
						valueXmlString.append("<gp_date>").append("<![CDATA[" + despDateStr+" 00:00:00.0"+"]]>").append("</gp_date>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
					shName =  getValue("tran_name", "transporter", "tran_code", tranCode, conn);
					valueXmlString.append("<sh_name>").append("<![CDATA[" + shName + "]]>").append("</sh_name>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("shipment_id"))
				{
					shipmentId = checkNull(genericUtility.getColumnValue("shipment_id", dom));
					sql = " select lorry_no, lr_no, lr_date, tran_code , "+
							"	gross_weight,tare_weight, bill_no, bill_date "+
							" from shipment "+
							" where shipment_id = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, shipmentId);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						lorryNo = checkNull(rs.getString("lorry_no"));
						lrNo = checkNull(rs.getString("lr_no"));
						lrDate = rs.getTimestamp("lr_date");
						tranCode = checkNull(rs.getString("tran_code"));
						grossWeight = rs.getDouble("gross_weight");
						tareWeight = rs.getDouble("tare_weight");
						billNo = checkNull(rs.getString("bill_no"));
						billDate = rs.getTimestamp("bill_date");
						nettWeight = grossWeight - tareWeight;

						valueXmlString.append("<gross_wt__ship>").append("<![CDATA[" + grossWeight + "]]>").append("</gross_wt__ship>");
						valueXmlString.append("<tare_wt__ship>").append("<![CDATA[" + tareWeight + "]]>").append("</tare_wt__ship>");
						valueXmlString.append("<nett_wt__ship>").append("<![CDATA[" + nettWeight + "]]>").append("</nett_wt__ship>");

						lorryNoDesp = checkNull(genericUtility.getColumnValue("lorry_no", dom));
						lrNoDesp = checkNull(genericUtility.getColumnValue("lr_no", dom));
						lrDateDesp = genericUtility.getColumnValue("lr_date", dom);
						billNoDesp = checkNull(genericUtility.getColumnValue("sb_no", dom));
						billDateDesp = genericUtility.getColumnValue("sb_date", dom);

						if( isnull(lorryNoDesp) || lenTrim(lorryNoDesp) == 0 )
						{
							valueXmlString.append("<lorry_no>").append("<![CDATA[" + lorryNo + "]]>").append("</lorry_no>");
						}
						if( isnull(lrNoDesp) || lenTrim(lrNoDesp) == 0 )
						{
							valueXmlString.append("<lr_no>").append("<![CDATA[" + lrNo + "]]>").append("</lr_no>");
						}
						if( isnull(lrDateDesp) )
						{
							if( lrDate != null )
							{
								lrDateStr = sdf.format(lrDate.getTime());
								valueXmlString.append("<lr_date>").append("<![CDATA[" + lrDateStr + "]]>").append("</lr_date>");
							}
							else
							{
								valueXmlString.append("<lr_date>").append("<![CDATA[" + lrDateStr + "]]>").append("</lr_date>");
							}
						}
						if( isnull(billNoDesp) || lenTrim(billNoDesp) == 0 )
						{
							valueXmlString.append("<sb_no>").append("<![CDATA[" + billNo + "]]>").append("</sb_no>");
						}

						if( isnull(billDateDesp) )
						{
							if( billDate != null )
							{
								billDateStr = sdf.format(billDate.getTime());
								valueXmlString.append("<sb_date>").append("<![CDATA[" + billDateStr + "]]>").append("</sb_date>");
							}
							else
							{
								valueXmlString.append("<sb_date>").append("<![CDATA[]]>").append("</sb_date>");

							}
						}
						if( isnull(tranCodeDesp) || lenTrim(tranCodeDesp) == 0 )
						{
							valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
							tranName =  getValue("tran_name", "transporter", "tran_code", tranCode, conn);

							if( isnull(tranName))
							{
								tranName = "";
							}
							valueXmlString.append("<sh_name>").append("<![CDATA[" + tranName + "]]>").append("</sh_name>");
						}
					}
					//changed by nasruddin start
					else
					{
						valueXmlString.append("<gross_wt__ship>").append("<![CDATA[]]>").append("</gross_wt__ship>");
						valueXmlString.append("<tare_wt__ship>").append("<![CDATA[]]>").append("</tare_wt__ship>");
						valueXmlString.append("<nett_wt__ship>").append("<![CDATA[]]>").append("</nett_wt__ship>");
						valueXmlString.append("<sh_name>").append("<![CDATA[]]>").append("</sh_name>");
						valueXmlString.append("<tran_code>").append("<![CDATA[]]>").append("</tran_code>");
						valueXmlString.append("<sb_date>").append("<![CDATA[]]>").append("</sb_date>");
						valueXmlString.append("<sb_no>").append("<![CDATA[]]>").append("</sb_no>");
						valueXmlString.append("<lr_date>").append("<![CDATA[]]>").append("</lr_date>");
						valueXmlString.append("<lr_no>").append("<![CDATA[]]>").append("</lr_no>");
					}
					//changed by nasruddin end
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
				} 
				
				else if (currentColumn.trim().equalsIgnoreCase("stan_code__dest"))
				{
					stanCodeDest = genericUtility.getColumnValue("stan_code__dest", dom);
					stationDescrDest =  getValue("descr", "station", "stan_code", stanCodeDest, conn);
					valueXmlString.append("<station_descr__dest>").append("<![CDATA[" + stationDescrDest + "]]>").append("</station_descr__dest>");
				} 
		
				else if (currentColumn.trim().equalsIgnoreCase("gross_weight") || currentColumn.trim().equalsIgnoreCase("tare_weight")) 
				{ 
					lc_gross_weight =  Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("gross_weight", dom)));
					lc_nett_weight = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("tare_weight", dom)));
					lc_tare_weight = lc_gross_weight - lc_nett_weight;
					valueXmlString.append("<nett_weight>").append("<![CDATA["+lc_tare_weight+"]]>").append("</nett_weight>");
				}
				// changed by Nasruddin 12-01-17 End
				// case 1 end
				valueXmlString.append("</Detail1>");
				break;
				// case 2 start
			case 2:
				//System.out.println("**********************In case 2 ***********************8");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				
				
				valueXmlString = new StringBuffer(
						"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
				valueXmlString.append(editFlag).append("</editFlag> </header>");
				sordNo = genericUtility.getColumnValue("sord_no", dom1);
				valueXmlString.append("<Detail1>");
				valueXmlString.append("<sord_no protect = '1'>").append("<![CDATA[" + sordNo + "]]>").append("</sord_no>");
				valueXmlString.append("</Detail1>");
				valueXmlString.append("<Detail2>");
				//childNodeListLength = childNodeList.getLength();
				
				

				//System.out.println("IN DETAIL column name is %%%%%%%%%%%%%["+ currentColumn + "] ==> '" + columnValue + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{
					//System.out.println("@@@@ itm_defaultedit called............");
					/*
					valueXmlString.append("<qty_details protect = '1'>").append("<![CDATA[]]>").append("</qty_details>");

					if dw_detbrow[ii_currformno].RowCount() > 0 then
					ll_cnt = dw_detbrow[ii_currformno].GetSelectedRow(0)
					if ll_cnt > 0 then
					mtot_net_amt = dw_detbrow[ii_currformno].GetItemNumber(ll_cnt,"total_net_amt")
					dw_detedit[ii_currformno].SetItem(1,"tot_net_amt",mtot_net_amt)
					end if
					end if

				 */
					/*
					String lineNoCurrent = checkNull(genericUtility.getColumnValue("line_no", dom)).trim();
					
					double preQuantityTemp = 0,preRateStduomTemp = 0,preTaxAmtTemp = 0,preDiscAmtTemp = 0,pretotalNetAmtTemp=0;
					
					parentList = dom.getElementsByTagName("Detail2");
					int parentNodeListLength1 = parentList.getLength();
					for (int prntCtr = parentNodeListLength1; prntCtr > 0; prntCtr-- )
					{	
						parentNode1 = parentList.item(prntCtr-1);
						childList1 = parentNode1.getChildNodes();
						for (int ctr1 = 0; ctr1 < childList1.getLength(); ctr1++)
						{
							childNode1 = childList1.item(ctr1);
						
						childNodeName1 = childNode1.getNodeName();

						if (childNodeName1.equalsIgnoreCase("line_no")) 
						{
							linenoStr = checkNull(genericUtility.getColumnValue("line_no", dom2)).trim();
							//System.out.println("@@@@ previous line linenoStr["+linenoStr+"] ");
						}
						if (childNodeName1.equalsIgnoreCase("quantity")) 
						{
							preQuantityTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom2)));
								//System.out.println("@@@@ previous line linenoStr["+linenoStr+"] preQuantityTemp["+preQuantityTemp+"]");
						}
						if (childNodeName1.equalsIgnoreCase("rate__stduom")) 
						{
							preRateStduomTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__stduom", dom2)));
								//System.out.println("@@@@ previous line linenoStr["+linenoStr+"] preRateStduomTemp["+preRateStduomTemp+"]");
						}
						if (childNodeName1.equalsIgnoreCase("tax_amt")) 
						{
							preTaxAmtTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("tax_amt", dom2)));
							//System.out.println("@@@@ previous line linenoStr["+linenoStr+"] preTaxAmtTemp["+preTaxAmtTemp+"]");
						}
						if (childNodeName1.equalsIgnoreCase("disc_amt")) 
						{
							preDiscAmtTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("disc_amt", dom2)));
							//System.out.println("@@@@ previous line linenoStr["+linenoStr+"] preDiscAmtTemp["+preDiscAmtTemp+"]");
						}
						
						}
						if( linenoStr!= null && linenoStr.trim().length() > 0 && ( Integer.parseInt(linenoStr) < Integer.parseInt(lineNoCurrent)   ))
						{
							pretotalNetAmtTemp = (preQuantityTemp * preRateStduomTemp) + preTaxAmtTemp - preDiscAmtTemp ;
							totalNetAmt = totalNetAmt + pretotalNetAmtTemp;
						}
				}
					//System.out.println("@@@@ previous totalNetAmt["+totalNetAmt+"]");
					
					quantityTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));
					rateStduomTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__stduom", dom)));
					taxAmtTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("tax_amt", dom)));
					discAmtTemp = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("disc_amt", dom)));
					
					//System.out.println("@@@@ current line quantityTemp["+quantityTemp+"]rateStduomTemp["+rateStduomTemp+"]taxAmtTemp["+taxAmtTemp+"]discAmtTemp["+discAmtTemp+"]");
					
					currtotalNetAmt = (quantityTemp * rateStduomTemp) + taxAmtTemp - discAmtTemp ;
					//System.out.println("@@@@ current line currtotalNetAmt["+currtotalNetAmt+"]totalNetAmt["+totalNetAmt+"]");
					
					totalNetAmt = totalNetAmt + currtotalNetAmt;
					//System.out.println("@@@@ teoal currtotalNetAmt["+currtotalNetAmt+"]");
					valueXmlString.append("<total_net_amt>").append("<![CDATA["+totalNetAmt+"]]>").append("</total_net_amt>");
				*/
				}

				else if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					despId = genericUtility.getColumnValue("desp_id", dom1);
					sordNo = genericUtility.getColumnValue("sord_no", dom1);

					valueXmlString.append("<desp_id>").append("<![CDATA["+despId+"]]>").append("</desp_id>");
					valueXmlString.append("<sord_no>").append("<![CDATA["+sordNo+"]]>").append("</sord_no>");
				} 
				else if (currentColumn.trim().equalsIgnoreCase("sord_no")) 
				{ 
					sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
					lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					if( lenTrim(lineNoSord) > 0  )
					{
						//valueXmlString.append("<line_no__sord>").append("<![CDATA[" + getAbsString(lineNoSord)+ "]]>").append("</line_no__sord>");
						//setNodeValue(dom, "curr_code", getAbsString(currCode));
						reStr = itemChanged(dom, dom1, dom2, objContext,"line_no__sord", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
					
				} 
				else if (currentColumn.trim().equalsIgnoreCase("line_no__sord")) 
				{ 
					sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
					lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

					lineNoSord	= "   " + lineNoSord;
					lineNoSord = lineNoSord.substring(lineNoSord.length()-3, lineNoSord.length());

					valueXmlString.append("<line_no__sord>").append("<![CDATA["+lineNoSord+"]]>").append("</line_no__sord>");
					setNodeValue(dom, "line_no__sord", getAbsString(lineNoSord));

					sql = " select site_code,tax_class, tax_chap, tax_env, discount, rate__stduom," +
							" rate__clg ,cust_item__ref,item_code " +
							" ,pack_code"+ 
							"	from sorddet where sale_order = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					pStmt.setString(2, lineNoSord);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						siteCodeDet = checkNull(rs.getString("site_code"));
						taxClass = checkNull(rs.getString("tax_class"));
						taxChap = checkNull(rs.getString("tax_chap"));
						//System.out.println("@@@ taxChap2:-["+taxChap+"]");		
						taxEnv = checkNull(rs.getString("tax_env"));
						discount = rs.getDouble("discount");
						rateStduom = rs.getDouble("rate__stduom");
						rateClg = rs.getDouble("rate__clg");
						custItemRef = checkNull(rs.getString("cust_item__ref"));
						itemCode = checkNull(rs.getString("item_code"));
						packCode = checkNull(rs.getString("pack_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					if(  isnull( custItemRef ) ||  lenTrim( custItemRef) == 0 ) 
					{
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));

						sql = " select item_code__ref,descr " +
								" from customeritem " +
								"	where cust_code = ? " +
								"	and  item_code  = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, custCode);
						pStmt.setString(2, itemCode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							custItemRef = checkNull(rs.getString("item_code__ref"));
							descr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						valueXmlString.append("<cust_item__ref>").append("<![CDATA["+custItemRef+"]]>").append("</cust_item__ref>");
						valueXmlString.append("<custitem_desc>").append("<![CDATA["+descr+"]]>").append("</custitem_desc>");

						setNodeValue(dom, "cust_item__ref", getAbsString(custItemRef));
						setNodeValue(dom, "custitem_desc", getAbsString(descr));
					}
					else
					{
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));

						sql = " select descr  from customeritem " +
								"	where cust_code = ? " +
								"	and item_code  = ? " +
								" and item_code__ref = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, custCode);
						pStmt.setString(2, itemCode);
						pStmt.setString(3, custItemRef);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							descr = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						valueXmlString.append("<cust_item__ref>").append("<![CDATA["+custItemRef+"]]>").append("</cust_item__ref>");
						valueXmlString.append("<custitem_desc>").append("<![CDATA["+descr+"]]>").append("</custitem_desc>");

						setNodeValue(dom, "cust_item__ref", getAbsString(custItemRef));
						setNodeValue(dom, "custitem_desc", getAbsString(descr));
					} 

					sql = " select sum(case when qty_desp is null then 0 else qty_desp end), sum(case when quantity is null then 0 else quantity end) " +
							"	from sorditem  where  " +
							"	sale_order = ? and	line_no = ?  and line_type  <> 'B' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					pStmt.setString(2, lineNoSord);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						despatchedQty = rs.getDouble(1);
						orderQty = rs.getDouble(2);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;



					pendingQty =	orderQty - despatchedQty;
					//System.out.println("pendingQty["+pendingQty+"]orderQty["+orderQty+"]despatchedQty["+despatchedQty+"]");
					despId = checkNull(genericUtility.getColumnValue("desp_id", dom1));

					minusQty = 0;

					sql = " select sum(quantity) from despatchdet  " +
							" where	sord_no = ? " +
							" and  desp_id = ? " +
							" and  line_no__sord    = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					pStmt.setString(2, despId);
					pStmt.setString(3, lineNoSord);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						minusQty = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					valueXmlString.append("<tran_id__invpack>").append("<![CDATA["+packCode+"]]>").append("</tran_id__invpack>");
					
					valueXmlString.append("<pending_qty>").append("<![CDATA["+(pendingQty - minusQty)+"]]>").append("</pending_qty>");
					setNodeValue(dom, "pending_qty", (pendingQty - minusQty));
					//System.out.println("pendingQty["+pendingQty+"]minusQty["+minusQty+"]");
					balQty = pendingQty - minusQty;
					//System.out.println("balQty["+balQty+"]");

					if("Y".equalsIgnoreCase(SOalloc))
					{	
						cnt=0;
						sql = " Select item_code__ord, quantity, exp_lev, item_code, "+
								"	qty_alloc, lot_no, lot_sl, unit__std, conv__qty_stduom, unit "+
								"	from sordalloc where sale_order = ? and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							cnt++;
							itemCodeOrd = checkNull(rs.getString("item_code__ord"));
							quantity = rs.getDouble("quantity");
							expLev = checkNull(rs.getString("exp_lev"));
							itemCode = checkNull(rs.getString("item_code"));
							qtyAlloc = rs.getDouble("qty_alloc");
							lotNo = checkNull(rs.getString("lot_no"));
							lotSl = checkNull(rs.getString("lot_sl"));
							unitStd = checkNull(rs.getString("unit__std"));
							convQtyStduom = rs.getDouble("conv__qty_stduom");
							unit = checkNull(rs.getString("unit"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					}		
					if("Y".equalsIgnoreCase(SOalloc) && cnt == 0) 
					{		
						sql = " Select pack_instr, (case when no_art is null then 0 else no_art end) " +
								"	from sorddet where sale_order = ? and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							packInstr = checkNull(rs.getString(1));
							noArt = checkNull(rs.getString(2));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}								
					else
					{
						sql = " Select item_code__ord, quantity, exp_lev, item_code, qty_alloc " +
								"	from sorditem where sale_order =  ? " +
								" and line_no = ? and line_type = 'I' "; 
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							itemCodeOrd = checkNull(rs.getString("item_code__ord"));
							quantity = rs.getDouble("quantity");
							expLev = checkNull(rs.getString("exp_lev"));
							itemCode = checkNull(rs.getString("item_code"));
							qtyAlloc = rs.getDouble("qty_alloc");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

						sql = " Select unit__std, conv__qty_stduom, unit, pack_instr, (case when no_art is null then 0 else no_art end) " +
								" from sorddet where sale_order = ? and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							unitStd = checkNull(rs.getString("unit__std"));
							convQtyStduom = rs.getDouble("conv__qty_stduom");
							unit = checkNull(rs.getString("unit"));
							packInstr = checkNull(rs.getString("pack_instr"));
							noArt = checkNull(rs.getString(5));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;

					}

					String itemDesc = getValue("descr", "item", "item_code", itemCodeOrd, conn);  

					isExistFlag = isExist("bom", "bom_code", itemCodeOrd, conn);

					ordQty = quantity;
					//System.out.println("balQty["+balQty+"]quantity["+quantity+"]");
					if( balQty != quantity )
					{
						quantity = balQty;
					}
					/*else
					{
						quantity = quantity;
					}*/

					if( isnull(lotNo) || lotNo.length() == 0 ) 
					{
						lotNo = "     ";
					}
					if( isnull(lotSl) || lotSl.length() == 0 ) 
					{
						lotSl = "     ";
					}
					//System.out.println("@@@@@4023 orderQty["+orderQty+"]");
					valueXmlString.append("<item_code__ord>").append("<![CDATA["+itemCodeOrd+"]]>").append("</item_code__ord>");
					valueXmlString.append("<quantity__ord>").append("<![CDATA["+orderQty+"]]>").append("</quantity__ord>");
					valueXmlString.append("<site_code>").append("<![CDATA["+siteCodeDet+"]]>").append("</site_code>");
					valueXmlString.append("<exp_lev>").append("<![CDATA["+expLev+"]]>").append("</exp_lev>");
					//commented by kunal as instructed by manoharan sir
					//valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
					//valueXmlString.append("<quantity_real>").append("<![CDATA["+quantity+"]]>").append("</quantity_real>");
					valueXmlString.append("<lot_no>").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");

					setNodeValue(dom, "item_code__ord", getAbsString(itemCodeOrd));
					setNodeValue(dom, "quantity__ord", orderQty);
					setNodeValue(dom, "site_code", getAbsString(siteCodeDet));
					setNodeValue(dom, "exp_lev", getAbsString(expLev));
					//commented by kunal as instructed by manoharan sir
					//setNodeValue(dom, "quantity", quantity);
					//setNodeValue(dom, "quantity_real", quantity);
					setNodeValue(dom, "lot_no", getAbsString(lotNo));

					lotSl = checkNull(genericUtility.getColumnValue("lot_sl", dom));

					if( isnull(loc) || lenTrim(loc) == 0 )
					{
						valueXmlString.append("<lot_sl>").append("<![CDATA["+lotSl+"]]>").append("</lot_sl>");
					}
					if(lotSl== null ||lotSl.trim().length() == 0) //Added by Nandkumar Gadkari on 19/06/18
					{
						lotSl="    ";
					}
					valueXmlString.append("<lot_sl>").append("<![CDATA["+lotSl+"]]>").append("</lot_sl>");
					valueXmlString.append("<unit__std>").append("<![CDATA["+unitStd+"]]>").append("</unit__std>");
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA["+convQtyStduom+"]]>").append("</conv__qty_stduom>");
					valueXmlString.append("<item_descr>").append("<![CDATA["+itemDesc+"]]>").append("</item_descr>");
					valueXmlString.append("<unit>").append("<![CDATA["+unit+"]]>").append("</unit>");
					valueXmlString.append("<rate__stduom>").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");
					valueXmlString.append("<rate__clg>").append("<![CDATA["+rateClg+"]]>").append("</rate__clg>");

					setNodeValue(dom, "lot_sl", getAbsString(lotSl));
					setNodeValue(dom, "unit__std", getAbsString(unitStd));
					setNodeValue(dom, "conv__qty_stduom", convQtyStduom);
					setNodeValue(dom, "item_descr", getAbsString(itemDesc));
					setNodeValue(dom, "unit", getAbsString(unit));
					setNodeValue(dom, "rate__stduom", rateStduom);
					setNodeValue(dom, "rate__clg", rateClg);

					if(isExistFlag)
					{
						itemDesc  = getValue("descr", "item", "item_code", itemCodeOrd, conn);
						valueXmlString.append("<item_code>").append("<![CDATA["+itemCodeOrd+"]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA["+itemDesc+"]]>").append("</item_descr>");

						setNodeValue(dom, "item_code", getAbsString(itemCodeOrd));
						setNodeValue(dom, "item_descr", getAbsString(itemDesc));
					}
					else
					{	
						valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
						setNodeValue(dom, "item_code", getAbsString(itemCode));
					}

					valueXmlString.append("<discount>").append("<![CDATA["+discount+"]]>").append("</discount>");
					setNodeValue(dom, "discount", discount);

					quantityStduomList = distCommon.convQtyFactor(unit, unitStd, itemCodeOrd, quantity,convQtyStduom, conn);

					quantityStduom = Double.parseDouble( quantityStduomList.get(1)== null ?"0": quantityStduomList.get(1).toString()  );
					//System.out.println("@@@@@4084 quantityStduom["+quantityStduom+"]");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA["+quantityStduom+"]]>").append("</quantity__stduom>");
					setNodeValue(dom, "quantity__stduom", quantityStduom);
                     
					 // changes by mayur on 22/11/17----start
					quantityRealList =  distCommon.convQtyFactor(unit, unitStd, itemCodeOrd, quantity,convQtyStduom, conn);
					quantityReal = Double.parseDouble( quantityRealList.get(1)== null ?"0": quantityRealList.get(1).toString()  );
					//System.out.println("@@@@@4084 quantityReal["+quantityReal+"]");
					valueXmlString.append("<quantity_real>").append("<![CDATA["+quantityReal+"]]>").append("</quantity_real>");
					setNodeValue(dom, "quantity_real", quantityReal);
                   //   changes by mayur on 22/11/17----end*/
				
					
					
					String noArticle = checkNull(genericUtility.getColumnValue("no_art", dom));

					if( ( ! isnull(noArticle)) && lenTrim(noArticle) > 0   && Double.parseDouble(noArticle) > 0 )
					{		
						packQty = quantityStduom / Double.parseDouble(noArticle);
						valueXmlString.append("<pack_qty>").append("<![CDATA["+packQty+"]]>").append("</pack_qty>");
					}

					sql = "	select distinct list_type " +
							" from pricelist " +
							" where price_list = (select price_list  from sorder  where sale_order = ? ) ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						listType = checkNull(rs.getString("list_type"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					sql = " select item_ref,tax_chap " +
							"	from sorditem " +
							"	where sale_order = ? and line_no = ? and exp_lev = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					pStmt.setString(2, lineNoSord);
					pStmt.setString(3, expLev);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						itemRef = checkNull(rs.getString("item_ref"));
						taxChap1 = checkNull(rs.getString("tax_chap"));
						//System.out.println("@@@ taxChap1:["+taxChap1+"]");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					// exp level & next code repeat
					sql = " select apply_price,price_var " +
							//"	into :ls_apply_price,:ls_price_var " +
							"	from bom where bom_code = ? "; 
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCodeOrd);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						applyPrice = checkNull(rs.getString("apply_price"));
						priceVar = checkNull(rs.getString("price_var"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  
					//System.out.println("@@@@@@@ priceVar["+priceVar+"] applyPrice["+applyPrice+"]");


					if("L".equalsIgnoreCase(listType) && "P".equalsIgnoreCase(applyPrice))
					{
						sql = " select rate  from pricelist where price_list = (select price_list " +
								"	from sorder 	where sale_order = ? ) and item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, itemCodeOrd);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							itemRate = rs.getDouble("rate");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;  

						sql = " select sum(rate) " +
								//"	into :lc_tot_rate " +
								"	from pricelist 	where price_list = (select price_list  from sorder " +
								" where sale_order =  ?  ) " +
								" and item_code in (select item_code from sorditem  " +
								" where sale_order = ?  and line_no = ?   and line_type = 'I') ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, sordNo);
						pStmt.setString(3, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							totRate = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;  

						sql = " select rate__stduom " +
								"	from sorddet " +
								"	where sale_order = ?  and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							rateStduom = rs.getDouble("rate__stduom");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;  


						diffRate = 	totRate - rateStduom ;
						//System.out.println("@@@@@@@ priceVar["+priceVar+"] diffRate["+diffRate+"]*(itemRate["+itemRate+"]/totRate["+totRate+"]");
						if("A".equalsIgnoreCase(priceVar))
						{
							rateStduom = itemRate - ( diffRate * ( itemRate / totRate));

							valueXmlString.append("<rate__stduom>").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");
							valueXmlString.append("<rate__clg>").append("<![CDATA["+rateStduom+"]]>").append("</rate__clg>");
							valueXmlString.append("<conf_diff_amt>").append("<![CDATA["+(diffRate * (itemRate/totRate))+"]]>").append("</conf_diff_amt>");

							setNodeValue(dom, "rate__stduom", rateStduom);
							setNodeValue(dom, "rate__clg", rateStduom);
							setNodeValue(dom, "conf_diff_amt", (diffRate * (itemRate/totRate)));
						}
						else if("D".equalsIgnoreCase(priceVar))
						{		
							discAmt = diffRate * ( itemRate/ totRate);
							valueXmlString.append("<disc_amt>").append("<![CDATA["+discAmt+"]]>").append("</disc_amt>");
							valueXmlString.append("<rate__stduom>").append("<![CDATA["+itemRate+"]]>").append("</rate__stduom>");
							valueXmlString.append("<rate__clg>").append("<![CDATA["+itemRate+"]]>").append("</rate__clg>");
							valueXmlString.append("<conf_diff_amt>").append("<![CDATA[0]]>").append("</conf_diff_amt>");
							//System.out.println("@@@@@ discAmt["+discAmt+"]");
							setNodeValue(dom, "disc_amt", discAmt);
							setNodeValue(dom, "rate__stduom", itemRate);
							setNodeValue(dom, "rate__clg", itemRate);
							setNodeValue(dom, "conf_diff_amt", 0);
						}
					}
					else if("L".equalsIgnoreCase(listType) && "E".equalsIgnoreCase(applyPrice))
					{		
						sql = "	select (case when eff_cost is null then 0 else eff_cost end) " +
								"	from bomdet  " +
								"	where bom_code = ? 	and item_ref = ? ";		
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCodeOrd);
						pStmt.setString(2, itemRef);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							effCost = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;  

						valueXmlString.append("<rate__stduom>").append("<![CDATA["+effCost+"]]>").append("</rate__stduom>");
						valueXmlString.append("<rate__clg>").append("<![CDATA["+effCost+"]]>").append("</rate__clg>");

						setNodeValue(dom, "rate__stduom", effCost);
						setNodeValue(dom, "rate__clg", effCost);
					}
					
					if( isnull(applyPrice) || applyPrice.trim().length() == 0 )
					{
						
						mNum1 = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));
						mNum2 = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__stduom", dom)));
								
						sql = " select discount   from sorddet where sale_order =  ? and line_no = ? "; 
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, sordNo);
						pStmt.setString(2, lineNoSord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_disc_perc =  Double.parseDouble(checkDoubleNull(rs.getString("discount")));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;  
						//System.out.println("@@@@@@@ lc_disc_perc["+lc_disc_perc+"]mNum1["+mNum1+"] mNum2["+mNum2+"]");

					lc_disc_amt = (lc_disc_perc/100) * (mNum1 * mNum2);
					
					//System.out.println("@@@@@4444 lc_disc_amt["+lc_disc_amt+"]");
					valueXmlString.append("<disc_amt>").append("<![CDATA["+lc_disc_amt+"]]>").append("</disc_amt>");
					setNodeValue(dom, "disc_amt", lc_disc_amt);
				}
					
                     //Changes by mayur on 06-03-18-------[start]					
					if("null".equalsIgnoreCase(taxClass) || taxClass == null || taxClass.trim().length()==0)
					{
						valueXmlString.append("<tax_class>").append("<![CDATA[ ]]>").append("</tax_class>");
						
					}
					else 
					{
						valueXmlString.append("<tax_class>").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");
						
					}
					
					if("null".equalsIgnoreCase(taxChap1) || taxChap1 == null || taxChap1.trim().length()==0)
					{
						//System.out.println("Inside if of Sorderitem tax chapter");
						//System.out.println("tax chapter 1:-------["+taxChap1+"]");
						valueXmlString.append("<tax_chap>").append("<![CDATA[ ]]>").append("</tax_chap>");
						
					}
					else 
					{
						//System.out.println("Inside else of Sorderitem tax chapter");
						//System.out.println("tax chapter 1:-------["+taxChap1+"]");
						valueXmlString.append("<tax_chap>").append("<![CDATA["+taxChap1+"]]>").append("</tax_chap>");
							
					}
					
					if("null".equalsIgnoreCase(taxEnv) || taxEnv == null || taxEnv.trim().length()==0)
					{
						valueXmlString.append("<tax_env>").append("<![CDATA[ ]]>").append("</tax_env>");
						
					}
					else 
					{
						valueXmlString.append("<tax_env>").append("<![CDATA["+taxEnv+"]]>").append("</tax_env>");
							
					}
										
					//Changes by mayur on 06-03-18-------[end]
					
					//valueXmlString.append("<tax_class>").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");
					//valueXmlString.append("<tax_chap>").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
					//valueXmlString.append("<tax_env>").append("<![CDATA["+taxEnv+"]]>").append("</tax_env>");

					setNodeValue(dom, "tax_class", getAbsString(taxClass));
					setNodeValue(dom, "tax_chap", getAbsString(taxChap1));
					setNodeValue(dom, "tax_env", getAbsString(taxEnv));

					loc = checkNull(genericUtility.getColumnValue("loc_code", dom));

					valueXmlString.append("<pack_instr>").append("<![CDATA["+packInstr+"]]>").append("</pack_instr>");
					valueXmlString.append("<no_art>").append("<![CDATA["+noArt+"]]>").append("</no_art>");

					setNodeValue(dom, "pack_instr", getAbsString(packInstr));
					setNodeValue(dom, "no_art", getAbsString(noArt));

					//ll_mcode=long(mcode)

					sql = " select rate__std  from sorddet where sale_order = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					pStmt.setString(2, lineNoSord );
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						rate = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					if( rate > 0 )
					{
						valueXmlString.append("<rate__std>").append("<![CDATA["+rateStduom+"]]>").append("</rate__std>");
						setNodeValue(dom, "rate__std", rateStduom);
					}

					//gbf_itemchangeddet("exp_lev")

					reStr = itemChanged(dom, dom1, dom2, objContext,"exp_lev", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);


					expLev = checkNull(genericUtility.getColumnValue("exp_lev", dom));
					sordNo = checkNull(genericUtility.getColumnValue("sord_no", dom));
					lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

					lineNoSord	= "   " + lineNoSord;
					lineNoSord = lineNoSord.substring(lineNoSord.length()-3, lineNoSord.length());

					sql = " select nature,line_type,item_code " +
							" from sorditem where	sale_order = ?  " +
							" and	line_no    = ? and 	exp_lev	   = ? " +
							"	and 	line_type  <> 'B' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, sordNo);
					pStmt.setString(2, lineNoSord );
					pStmt.setString(3, expLev );
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						nature = checkNull( rs.getString("nature"));
						lineType = checkNull( rs.getString("line_type"));
						itemcodeFree = checkNull( rs.getString("item_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  
					// C and P nature added by nandkumar gadkari on 06/08/19
					if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature) || "V".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature) )
					{
						valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
						valueXmlString.append("<line_type>").append("<![CDATA["+nature+"]]>").append("</line_type>");

						setNodeValue(dom, "rate__stduom", 0);
						setNodeValue(dom, "line_type", getAbsString(nature));
					}
					else
					{
						valueXmlString.append("<line_type>").append("<![CDATA["+lineType+"]]>").append("</line_type>");
					}
					if("C".equalsIgnoreCase(nature))//added by nandkumar gadkari on 19/08/19
					{
						valueXmlString.append("<line_type>").append("<![CDATA["+nature+"]]>").append("</line_type>");
						setNodeValue(dom, "line_type", getAbsString(nature));
					}

					if("F".equalsIgnoreCase(nature)) 
					{
						taxChap = getValue("tax_chap", "item", "item_code", itemcodeFree, conn);
						//System.out.println("@@@@ taxChap3["+taxChap+"]");

						if ((! isnull(taxChap)) && lenTrim(taxChap) > 0 )
						{
							//System.out.println("Inside if of tax chapter 3:-["+taxChap+"]");
							valueXmlString.append("<tax_chap>").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
							setNodeValue(dom, "tax_chap", getAbsString(taxChap));
						}
						//dw_detedit[ii_currformno].setcolumn("lot_sl")  //pending
					}		
					valueXmlString.append("<rate__std>").append("<![CDATA["+rateStduom+"]]>").append("</rate__std>");
					setNodeValue(dom, "rate__std", rateStduom);

					//offRate =  nvo_fin_inv.gbf_calc_discount_rate(ls_saleorder,ls_linenosord,ls_item_code,lc_ratestd,'O');
					//offRate =   nvo_fin_inv.gbf_calc_discount_rate(ls_saleorder,ls_linenosord,ls_item_code,lc_ratestd,'O');
					offRate =  calc_discount_rate(ls_saleorder,ls_linenosord,ls_item_code,lc_ratestd,"O",conn);
					//bbRate = nvo_fin_inv.gbf_calc_discount_rate(ls_saleorder,ls_linenosord,ls_item_code,lc_ratestd,'B');
					bbRate =  calc_discount_rate(ls_saleorder,ls_linenosord,ls_item_code,lc_ratestd,"B",conn);
					if( offRate > 0 && offRate != rate ) 
					{
						valueXmlString.append("<rate__stduom>").append("<![CDATA["+offRate+"]]>").append("</rate__stduom>");
						setNodeValue(dom, "rate__stduom", offRate);
					}
					//gbf_ic_set_bb_rate(bbRate,rate)
					if (bbRate > 0 && bbRate != rate) 
					{
						valueXmlString.append("<rate__stduom>").append("<![CDATA["+bbRate+"]]>").append("</rate__stduom>");
						setNodeValue(dom, "rate__stduom", bbRate);
					}
					//gbf_itemchangeddet("Quantity")
					reStr = itemChanged(dom, dom1, dom2, objContext,"Quantity", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					partNo = getValue("mfr_part_no", "item", "item_code", itemCodeOrd, conn);

					valueXmlString.append("<part_no>").append("<![CDATA["+partNo+"]]>").append("</part_no>");

				} // end wf 
				else if (currentColumn.trim().equalsIgnoreCase("exp_lev")) 
				{ 
					String taxChapFr=null;
					mVal1 = checkNull(genericUtility.getColumnValue("exp_lev", dom));
					mcode = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					mVal  = checkNull(genericUtility.getColumnValue("sord_no", dom));

					mcode = "    "+mcode;
					mcode = mcode.substring(mcode.length()-3, mcode.length());

					sql = " Select site_code, unit__std, conv__qty_stduom, unit "+
							" from sorddet "+
							" where sale_order = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					pStmt.setString(2, mcode );
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						mdescr2 = checkNull( rs.getString("site_code"));
						mstunit = checkNull( rs.getString("unit__std"));
						mNum2 =  rs.getDouble("conv__qty_stduom");
						mUnit = checkNull( rs.getString("unit"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					sql = " Select item_code__ord, quantity, item_code, qty_alloc ,tax_chap"+
							" from sorditem where sale_order = ? and line_no = ? and "+
							" site_code = ? and exp_lev = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					pStmt.setString(2, mcode );
					pStmt.setString(3, mdescr2 );
					pStmt.setString(4, mVal1 );
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						itemcode = checkNull( rs.getString("item_code__ord"));
						mNum =  rs.getDouble("quantity");
						mdescr1 = checkNull( rs.getString("item_code"));
						mNum1 =  rs.getDouble("qty_alloc");
						taxChapFr=checkNull(rs.getString("tax_chap"));
						//System.out.println("@@@ taxChapFr:-["+taxChapFr+"]");		
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					mitemdesc = getValue("descr", "item", "item_code", mdescr1, conn);

					valueXmlString.append("<Quantity>").append("<![CDATA[0]]>").append("</Quantity>");

					//mNum = gbf_GetPendingQty(ls_ErrorCode);
					mNum = getPendingQty(dom,dom2 ,conn);
					//System.out.println("@@@@@4445 orderQty["+mNum+"]");
					valueXmlString.append("<item_code__ord>").append("<![CDATA["+itemcode+"]]>").append("</item_code__ord>");
					//valueXmlString.append("<quantity__ord>").append("<![CDATA["+mNum+"]]>").append("</quantity__ord>");
					valueXmlString.append("<item_code>").append("<![CDATA["+mdescr1+"]]>").append("</item_code>");
					valueXmlString.append("<quantity>").append("<![CDATA["+mNum+"]]>").append("</quantity>");
					valueXmlString.append("<quantity_real>").append("<![CDATA["+mNum+"]]>").append("</quantity_real>");
					valueXmlString.append("<unit__std>").append("<![CDATA["+mstunit+"]]>").append("</unit__std>");
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA["+mNum2+"]]>").append("</conv__qty_stduom>");
					valueXmlString.append("<item_descr>").append("<![CDATA["+mitemdesc+"]]>").append("</item_descr>");
					valueXmlString.append("<unit>").append("<![CDATA["+mUnit+"]]>").append("</unit>");
					//valueXmlString.append("<tax_chap>").append("<![CDATA["+taxChapFr+"]]>").append("</tax_chap>");
					setNodeValue(dom, "quantity", mNum);
					setNodeValue(dom, "quantity_real", mNum);
					if("null".equalsIgnoreCase(taxChapFr) || taxChapFr == null || taxChapFr.trim().length()==0)
					{
						//System.out.println("Inside if tax chapter Fr");
						//System.out.println("tax chapter Fr:-------["+taxChapFr+"]");
						valueXmlString.append("<tax_chap>").append("<![CDATA[ ]]>").append("</tax_chap>");
						
					}
					else 
					{
						//System.out.println("Inside else of tax chapter Fr");
						//System.out.println("tax chapter Fr:-------["+taxChapFr+"]");						
						valueXmlString.append("<tax_chap>").append("<![CDATA["+taxChapFr+"]]>").append("</tax_chap>");
							
					}
					//mNum3 = gf_conv_qty_fact(mUnit, mstunit, mdescr1, mNum, mNum2);
					
					mNumList = distCommon.convQtyFactor(mUnit, mstunit, mdescr1, mNum,mNum2, conn);
					//System.out.println("@@@@@4458 mNumList["+mNumList+"]");
					if( mNumList.size() > 1)
					{
						mNum3 = 	(Double) mNumList.get(1);
					}
					//System.out.println("@@@@@@4463 mNum3["+mNum3+"]");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA["+mNum3+"]]>").append("</quantity__stduom>");

					lc_no_article  = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("no_art", dom)));

					if( lc_no_article > 0 )
					{
						lc_pack_qty = mNum3 / lc_no_article;
						valueXmlString.append("<pack_qty>").append("<![CDATA["+lc_pack_qty+"]]>").append("</pack_qty>");
					}

					//Create Common method for following logic , it is also in use at line_no__sord manoj
					sql = " select sum(case when qty_desp is null then 0 else qty_desp end), sum(case when quantity is null then 0 else quantity end) " +
							" from sorditem  where sale_order = ?  and line_no    = ?  " +
							"	and exp_lev	   = ?	and line_type  <> 'B' ";	
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					pStmt.setString(2, mcode );
					pStmt.setString(3, mVal1 );
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ld_despatched_qty =  rs.getDouble(1);
						ld_order_qty =  rs.getDouble(2);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					ld_pending_qty =	ld_order_qty - ld_despatched_qty;

					ls_desp_id  = checkNull(genericUtility.getColumnValue("desp_id", dom1));
					ld_minus_qty = 0;

					sql = " select sum(quantity) " +
							" from despatchdet  where sord_no  = ? and desp_id = ? " +
							"	and line_no__sord 	= ?  and exp_lev = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					pStmt.setString(2, ls_desp_id );
					pStmt.setString(3, mcode );
					pStmt.setString(4, mVal1 );
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ld_minus_qty =  rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					valueXmlString.append("<pending_qty>").append("<![CDATA["+(ld_pending_qty - ld_minus_qty)+"]]>").append("</pending_qty>");

					sql = " select distinct list_type " +
							" from pricelist where price_list = (select price_list from sorder where sale_order = ? ) ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_list_type = checkNull( rs.getString("list_type"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					sql = " select item_ref " +
							" from sorditem " +
							" where sale_order = ? and line_no = ? and exp_lev = ?  ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					pStmt.setString(2, mcode);
					pStmt.setString(3, mVal1);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_item_ref = checkNull( rs.getString("item_ref"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  

					sql = " select apply_price,price_var " +
							" from bom " +
							" where bom_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemcode);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_apply_price = checkNull( rs.getString("apply_price"));
						ls_price_var = checkNull( rs.getString("price_var"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;  


					if("L".equalsIgnoreCase(ls_list_type) && "P".equalsIgnoreCase(ls_apply_price))
					{	
						sql = " select rate from pricelist " +
								" where price_list = (select price_list from sorder " +
								" where sale_order = ? ) and item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mVal);
						pStmt.setString(2, mdescr1);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_item_rate =  rs.getDouble("rate");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;  

						sql = " select sum(rate) from pricelist " +
								" where price_list = (select price_list from sorder " +
								" where sale_order = ? ) " +
								" and item_code in (select item_code from sorditem " +
								" where sale_order = ? and line_no = ? and line_type = 'I') ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mVal);
						pStmt.setString(2, mVal);
						pStmt.setString(3, mcode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_tot_rate =  rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						sql = " select rate__stduom " +
								"  from sorddet where sale_order = ?  and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mVal);
						pStmt.setString(2, mcode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_ratestd =  rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 



						lc_diff_rate = 	lc_tot_rate - lc_ratestd ;

						if("A".equalsIgnoreCase(ls_price_var))
						{	
							lc_ratestd = lc_item_rate - (lc_diff_rate * (lc_item_rate/lc_tot_rate));

							valueXmlString.append("<rate__stduom>").append("<![CDATA["+lc_ratestd+"]]>").append("</rate__stduom>");
							valueXmlString.append("<rate__clg>").append("<![CDATA["+lc_ratestd+"]]>").append("</rate__clg>");
							valueXmlString.append("<conf_diff_amt>").append("<![CDATA["+(lc_diff_rate * (lc_item_rate/lc_tot_rate))+"]]>").append("</conf_diff_amt>");
						}
						else if("D".equalsIgnoreCase(ls_price_var))
						{	
							lc_disc_amt = lc_diff_rate * (lc_item_rate/lc_tot_rate);

							valueXmlString.append("<disc_amt>").append("<![CDATA["+lc_disc_amt+"]]>").append("</disc_amt>");
							valueXmlString.append("<rate__stduom>").append("<![CDATA["+lc_item_rate+"]]>").append("</rate__stduom>");
							valueXmlString.append("<rate__clg>").append("<![CDATA["+lc_item_rate+"]]>").append("</rate__clg>");
							valueXmlString.append("<conf_diff_amt>").append("<![CDATA[0]]>").append("</conf_diff_amt>");
						}
					}
					else if("L".equalsIgnoreCase(ls_list_type) && "E".equalsIgnoreCase(ls_apply_price) )
					{
						sql = " select (case when eff_cost is null then 0 else eff_cost end) "+
								//     " into : " +
								" from bomdet where bom_code = ? and item_ref = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemcode);
						pStmt.setString(2, ls_item_ref);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_eff_cost =  rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 


						valueXmlString.append("<rate__stduom>").append("<![CDATA["+lc_eff_cost+"]]>").append("</rate__stduom>");
						valueXmlString.append("<rate__clg>").append("<![CDATA["+lc_eff_cost+"]]>").append("</rate__clg>");
					}

					sql = " select nature " +
							" from sorditem where sale_order = ?  and line_no = ?  " +
							"	and exp_lev	   = ?  and line_type  <> 'B' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, mVal);
					pStmt.setString(2, mcode);
					pStmt.setString(3, mVal1);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_nature =  checkNull(rs.getString("nature"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 
					// C and P nature added by nandkumar gadkari on 06/08/19
					if ("F".equalsIgnoreCase(ls_nature) || "B".equalsIgnoreCase(ls_nature) || "S".equalsIgnoreCase(ls_nature) || "I".equalsIgnoreCase(ls_nature) || "V".equalsIgnoreCase(ls_nature) || "P".equalsIgnoreCase(nature) )
					{
						valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
						valueXmlString.append("<line_type>").append("<![CDATA["+ls_nature+"]]>").append("</line_type>");
					}
					if("C".equalsIgnoreCase(ls_nature))//added by nandkumar gadkari on 19/08/19
					{
						valueXmlString.append("<line_type>").append("<![CDATA["+ls_nature+"]]>").append("</line_type>");
					}

					//gbf_set_rate_conversion(); // Added by fatema - 30/06/2006
					valueXmlString = gbf_set_rate_conversion(dom,valueXmlString, conn);

				}

				else if (currentColumn.trim().equalsIgnoreCase("item_code__ord")) 
				{ 
					ls_sale_order = checkNull(genericUtility.getColumnValue("sord_no", dom));

					ls_sordatt_no = getValue("sordatt_no", "sorder", "sale_order", ls_sale_order, conn); 

					if( (! isnull(ls_sordatt_no)) && lenTrim(ls_sordatt_no) > 0 )
					{
						ls_item_code = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
						ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
						ls_line_no__sord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

						sql = " select quantity  from sorddet where  sale_order = ? " +
								" and    line_no    = ?  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_sale_order);
						pStmt.setString(2, ls_line_no__sord);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_qty =  rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						ls_site_code = getValue("site_code", "sorder", "sale_order", ls_sale_order, conn); 

						//String ls_ErrCode = gbf_stock_check(ls_item_code,ls_lot_no,"",ls_site_code,lc_qty);
						HashMap stockCheckMap = new HashMap();
						stockCheckMap = gbf_stock_check(dom,ls_item_code,ls_lot_no,"",ls_site_code,lc_qty,conn);
						String ls_ErrCode="";
						if( stockCheckMap.get("errorCode") != null )
						{
							ls_ErrCode = stockCheckMap.get("errorCode").toString() ;
						}
						if( stockCheckMap.get("valueXmlString") != null )
						{
							valueXmlString.append(stockCheckMap.get("valueXmlString").toString());
						}


						if ((! isnull(ls_ErrCode)) && lenTrim(ls_ErrCode) > 0 )
						{
							ls_alt_item = "";
							//ls_ErrCode = gbf_get_alternate_colour_item(ls_sale_order,ls_item_code,ls_lot_no,lc_qty,ls_line_no__sord,ls_alt_item);
							//skip as per suggestion
						} 

						if (( !isnull(ls_ErrCode)) && lenTrim(ls_ErrCode) > 0 )
						{
							valueXmlString.append("<loc_code>").append("<![CDATA[]]>").append("</loc_code>");
							valueXmlString.append("<lot_no>").append("<![CDATA[]]>").append("</lot_no>");
							valueXmlString.append("<lot_sl>").append("<![CDATA[]]>").append("</lot_sl>");
							valueXmlString.append("<item_code__ord>").append("<![CDATA[]]>").append("</item_code__ord>");
							valueXmlString.append("<item_code>").append("<![CDATA[]]>").append("</item_code>");
							valueXmlString.append("<quantity>").append("<![CDATA[0]]>").append("</quantity>");
							valueXmlString.append("<site_code>").append("<![CDATA[]]>").append("</site_code>");
							valueXmlString.append("<status>").append("<![CDATA[]]>").append("</status>");
							valueXmlString.append("<line_no__sord>").append("<![CDATA[]]>").append("</line_no__sord>");
							valueXmlString.append("<exp_lev>").append("<![CDATA[]]>").append("</exp_lev>");
						} 
					}
				}//end ic
				else if (currentColumn.trim().equalsIgnoreCase("quantity")) 
				{ 

					mNum = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity", dom)));		
					valueXmlString.append("<quantity_real>").append("<![CDATA["+mNum+"]]>").append("</quantity_real>");
					mVal = checkNull(genericUtility.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue("unit__std", dom));
					itemcode = checkNull(genericUtility.getColumnValue("item_code", dom));
					mNum1 = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("conv__qty_stduom", dom)));
					mNum2    = mNum1;
					if( lenTrim(mVal) == 0 )
					{
						mVal = getValue("unit", "item", "item_code", itemcode, conn);

						//mNum = gf_conv_qty_fact(mVal, mVal1, itemcode, mNum, mNum1);

						mNumList = distCommon.convQtyFactor(mVal, mVal1, itemcode, mNum,mNum1, conn);
						if( mNumList.size() > 0)
						{
							mNum = 	(Double) mNumList.get(0);
						}
						valueXmlString.append("<unit>").append("<![CDATA["+mVal+"]]>").append("</unit>");
						setNodeValue(dom, "unit", mVal);
					}
					else
					{
						//mNum = gf_conv_qty_fact(mVal, mVal1, itemcode, mNum, mNum1);	
						mNumList = distCommon.convQtyFactor(mVal, mVal1, itemcode, mNum,mNum1, conn);
						//System.out.println("@@@@@@ mNumList["+mNumList+"]");
						if( mNumList.size() > 0)
						{
							mNum = 	(Double) mNumList.get(1);
						}
					}

					if( mNum2 == 0 )
					{		
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA["+mNum1+"]]>").append("</conv__qty_stduom>");
						setNodeValue(dom, "conv__qty_stduom", mNum1);
					}
					//System.out.println("@@@@@@@4795 mNum["+mNum+"]");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA["+mNum+"]]>").append("</quantity__stduom>");
					setNodeValue(dom, "quantity__stduom", mNum);

					ls_cust_code = checkNull(genericUtility.getColumnValue("cust_code", dom1));
					ls_sale_order= checkNull(genericUtility.getColumnValue("sord_no", dom));
					ls_line_no = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

					if ((! isnull(ls_line_no)) && lenTrim(ls_line_no) > 0 )
					{
						ls_line_no = "    "+ls_line_no; 
						ls_line_no = 	ls_line_no.substring(ls_line_no.length()-3,ls_line_no.length());
					}

					sql = " select site_code " +
							"	from sorddet " +
							"	where sale_order = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_sale_order);
					pStmt.setString(2, ls_line_no);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_site_code_det =  checkNull(rs.getString("site_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 

					sql = " Select pack_code from sorddet " +
							" where sale_order = ? and line_no = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_sale_order);
					pStmt.setString(2, ls_line_no);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_pack_code =  checkNull(rs.getString("pack_code"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 

					//ll_no_art = gf_get_no_art(ls_site_code_det,ls_cust_code,itemcode,ls_pack_code,mNum,'B',ac_shipper_qty,ac_int_qty);
					//ll_no_art = distCommon.getNoArt(ls_site_code_det, ls_cust_code, itemcode, ls_pack_code, mNum, 'B', ac_shipper_qty, ac_int_qty, conn);
					getNoArtList = distCommon.getNoArtAList(ls_site_code_det, ls_cust_code, itemcode, ls_pack_code, mNum, 'B', ac_shipper_qty, ac_int_qty, conn);
					
					//System.out.println("------- getNoArtList.size() -------------"+getNoArtList.size());
					if( getNoArtList.size() > 2)
					{
						ll_no_art = Double.parseDouble(checkDoubleNull(getNoArtList.get(0).toString()));
						lc_shipper_qty = Double.parseDouble(checkDoubleNull(getNoArtList.get(1).toString()));
						lc_int_qty = Double.parseDouble(checkDoubleNull(getNoArtList.get(2).toString()));
					}	

					valueXmlString.append("<no_art>").append("<![CDATA["+ll_no_art+"]]>").append("</no_art>");
					setNodeValue(dom, "no_art", ll_no_art);

					if( ll_no_art == 0 )
					{	
						ls_item_code = checkNull(genericUtility.getColumnValue("item_code", dom));
						ls_site_code = checkNull(genericUtility.getColumnValue("site_code", dom));
						ls_loc_code = checkNull(genericUtility.getColumnValue("loc_code", dom));
						ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
						ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", dom));

						cnt=0;
						sql = " select (case when qty_per_art is null then 0 else qty_per_art end) " +
								" from stock " +
								" where item_code = ? and site_code =  ? " +
								" and loc_code  = ? and lot_no 	= ? " +
								" and lot_sl 	= ? ";			 
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_item_code);
						pStmt.setString(2, ls_site_code);
						pStmt.setString(3, ls_loc_code);
						pStmt.setString(4, ls_lot_no);
						pStmt.setString(5, ls_lot_sl);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							cnt++;
							lc_qty_per_art =  rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						if(  cnt > 0 && lc_qty_per_art > 0 )			
						{
							mNum = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));
							//dw_detedit[ii_currformno].setitem(1,"no_art",Round(mNum/lc_qty_per_art,0))
							valueXmlString.append("<no_art>").append("<![CDATA["+Math.round(mNum/lc_qty_per_art)+"]]>").append("</no_art>");
							setNodeValue(dom, "no_art", (Math.round(mNum/lc_qty_per_art)));
						} 	
					}	

					lc_no_article = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("no_art", dom)));

					if( lc_no_article > 0 )
					{
						lc_pack_qty = mNum / lc_no_article;
						valueXmlString.append("<pack_qty>").append("<![CDATA["+lc_pack_qty+"]]>").append("</pack_qty>");
						setNodeValue(dom, "pack_qty", lc_pack_qty);
					}

					//Variables are not having value manoj
					//lc_shipper_qty = ac_shipper_qty;
					//lc_int_qty = ac_int_qty;


					//ll_no_art1 = gf_get_no_art(ls_site_code_det,ls_cust_code,itemcode,ls_pack_code,mNum,'S',ac_shipper_qty,ac_int_qty);
					//getNoArtList = distCommon.getNoArt(ls_site_code_det, ls_cust_code, itemcode, ls_pack_code, mNum, 'S', ac_shipper_qty, ac_int_qty, conn);
					getNoArtList = distCommon.getNoArtAList(ls_site_code_det, ls_cust_code, itemcode, ls_pack_code, mNum, 'S', ac_shipper_qty, ac_int_qty, conn);
					if( getNoArtList.size() > 2)
					{
						ll_no_art1 = Double.parseDouble(checkDoubleNull(getNoArtList.get(0).toString()));
						lc_shipper_qty = Double.parseDouble(checkDoubleNull(getNoArtList.get(1).toString()));
						lc_int_qty = Double.parseDouble(checkDoubleNull(getNoArtList.get(2).toString()));
					}

					lc_bal_qty = mNum - (lc_shipper_qty * ll_no_art1);

					//ll_no_art2 = gf_get_no_art(ls_site_code_det,ls_cust_code,itemcode,ls_pack_code,lc_bal_qty,'I',ac_shipper_qty,ac_int_qty);		
					//ll_no_art2 = distCommon.getNoArt(ls_site_code_det, ls_cust_code, itemcode, ls_pack_code, lc_bal_qty, 'I', ac_shipper_qty, ac_int_qty, conn);
					getNoArtList = distCommon.getNoArtAList(ls_site_code_det, ls_cust_code, itemcode, ls_pack_code, lc_bal_qty, 'I', ac_shipper_qty, ac_int_qty, conn);

					if( getNoArtList.size() > 2)
					{
						ll_no_art2 = Double.parseDouble(checkDoubleNull(getNoArtList.get(0).toString()));
						lc_shipper_qty = Double.parseDouble(checkDoubleNull(getNoArtList.get(1).toString()));
						lc_int_qty = Double.parseDouble(checkDoubleNull(getNoArtList.get(2).toString()));
					}

					//lc_int_qty = ac_int_qty;

					lc_shipper_qty = lc_shipper_qty * ll_no_art1;
					lc_int_qty = lc_int_qty * ll_no_art2;
					lc_loose_qty = mNum - (lc_shipper_qty + lc_int_qty );

					ls_str = "Shipper Quantity = "+lc_shipper_qty+ "   Integral Quantity = "+lc_int_qty+"   Loose Quantity = "+lc_loose_qty;

					valueXmlString.append("<qty_details>").append("<![CDATA["+ls_str+"]]>").append("</qty_details>");

					setNodeValue(dom, "qty_details", ls_str);
					/* not required manoj
					ls_item_code = checkNull(genericUtility.getColumnValue("item_code", dom));
					ls_site_code = checkNull(genericUtility.getColumnValue("site_code", dom));
					ls_loc_code = checkNull(genericUtility.getColumnValue("loc_code", dom));
					ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
					ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", dom));
					 */
					if( lenTrim(ls_pack_code) > 0 )
					{
						sql = " select gross_weight,nett_weight " +
								" from   packing 	where  pack_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_pack_code);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_gross_wt =  rs.getDouble(1);
							lc_nett_wt =  rs.getDouble(2);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						ll_no_art = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("no_art", dom)));

						lc_gross_wt = lc_gross_wt * ll_no_art;
						lc_nett_wt  = lc_nett_wt  * ll_no_art;

						lc_gross_weight = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("gross_weight", dom)));

						if(  lc_gross_weight == 0 )
						{
							valueXmlString.append("<gross_weight>").append("<![CDATA["+lc_gross_wt+"]]>").append("</gross_weight>");
							valueXmlString.append("<nett_weight>").append("<![CDATA["+lc_nett_wt+"]]>").append("</nett_weight>");
							valueXmlString.append("<tare_weight>").append("<![CDATA["+(lc_gross_wt - lc_nett_wt)+"]]>").append("</tare_weight>");

							setNodeValue(dom, "gross_weight", lc_gross_wt);
							setNodeValue(dom, "nett_weight", lc_nett_wt);
							setNodeValue(dom, "tare_weight", lc_gross_wt);

						}	
					}    

					lc_disc_amt = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("disc_amt", dom)));
					mcode = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					mVal = checkNull(genericUtility.getColumnValue("sord_no", dom));
					mNum1 = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));
					mNum2 = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__stduom", dom)));


					ls_line_no = "   "+mcode;
					ls_line_no = ls_line_no.substring(ls_line_no.length()-3,ls_line_no.length());

					ls_item_code__ord = checkNull(genericUtility.getColumnValue("item_code__ord", dom));

					//setnull(ls_apply_price)
					ls_apply_price="";

					ls_apply_price = getValue("apply_price", "bom", "bom_code", ls_item_code__ord, conn);

					if( isnull(ls_apply_price) || ls_apply_price.trim().length() == 0 ) // OR CONDITION ADDED BY NANDKUMAR GADKARI ON 24/12/18
					{
						mcode = "   "+mcode;
						mcode = mcode.substring(mcode.length()-3,mcode.length());

						sql = " select discount  from sorddet " +
								" where sale_order = ? and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mVal);
						pStmt.setString(2, mcode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_disc_perc =  rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						lc_disc_amt = (lc_disc_perc/100) * (mNum1 * mNum2);
						valueXmlString.append("<disc_amt>").append("<![CDATA["+lc_disc_amt+"]]>").append("</disc_amt>");
						setNodeValue(dom, "disc_amt", lc_disc_amt);
						//System.out.println("@@@@@@@ lc_disc_perc["+lc_disc_perc+"]mNum1["+mNum1+"] mNum2["+mNum2+"]");
						
						//System.out.println("@@@@@4444 lc_disc_amt["+lc_disc_amt+"]");
					}		
					//mdesc_offinv_amt = nvo_fin_inv.gbf_calc_detdisc_amt(ls_sale_order,ls_line_no,itemcode,mNum2,mNum1,'O');
					mdesc_offinv_amt = gbf_calc_detdisc_amt(ls_sale_order,ls_line_no,itemcode,mNum2,mNum1,"O",conn);
					//mdesc_bb_amt = nvo_fin_inv.gbf_calc_detdisc_amt(ls_sale_order,ls_line_no,itemcode,mNum2,mNum1,'B');
					mdesc_bb_amt = gbf_calc_detdisc_amt(ls_sale_order,ls_line_no,itemcode,mNum2,mNum1,"B",conn);

					lc_rate = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__std", dom)));
					valueXmlString.append("<disc_schem_offinv_amt>").append("<![CDATA["+mdesc_offinv_amt+"]]>").append("</disc_schem_offinv_amt>");
					valueXmlString.append("<disc_schem_billback_amt>").append("<![CDATA["+mdesc_bb_amt+"]]>").append("</disc_schem_billback_amt>");

					setNodeValue(dom, "disc_schem_offinv_amt", mdesc_offinv_amt);
					setNodeValue(dom, "disc_schem_billback_amt", mdesc_bb_amt);
					//gbf_itemchangeddet("lot_no")
					reStr = itemChanged(dom, dom1, dom2, objContext,"lot_no", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

				}
				else if (currentColumn.trim().equalsIgnoreCase("gross_weight") || currentColumn.trim().equalsIgnoreCase("nett_weight")) 
				{ 
					lc_gross_weight =  Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("gross_weight", dom)));
					lc_nett_weight = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("nett_weight", dom)));
					lc_tare_weight = lc_gross_weight - lc_nett_weight;
					valueXmlString.append("<tare_weight>").append("<![CDATA["+lc_tare_weight+"]]>").append("</tare_weight>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("lot_no") || currentColumn.trim().equalsIgnoreCase("lot_sl")) 
				{ 
					//System.out.println("------ Inside lot_no ----------- ");
					ls_item_code = checkNull(genericUtility.getColumnValue("item_code", dom));
					ls_site_code = checkNull(genericUtility.getColumnValue("site_code", dom));
					ls_loc_code = checkNull(genericUtility.getColumnValue("loc_code", dom));
					ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
					ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", dom));
					ls_saleord = checkNull(genericUtility.getColumnValue("sord_no", dom));
					ls_saleord_line = checkNull(genericUtility.getColumnValue("line_no__sord", dom));

					ls_saleord_line = "   "+ls_saleord_line;
					ls_saleord_line = ls_saleord_line.substring(ls_saleord_line.length()-3,ls_saleord_line.length());

					ls_unitstd = checkNull(genericUtility.getColumnValue("unit__std", dom));
					despDateStr = genericUtility.getColumnValue("desp_date", dom1);

					if( despDateStr != null )
					{
						ld_desp_date = Timestamp.valueOf(genericUtility.getValidDateString(despDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
					}

					if( isnull(ls_lot_sl))
					{
						ls_lot_sl = ""; 
					}

					if( isnull(ls_lot_no))
					{
						ls_lot_no = ""; 
					}

					cnt=0;
					sql = " select (case when gross_weight is null then 0 else gross_weight end), " +
							"	(case when net_weight is null then 0 else net_weight end), " +
							"	(case when tare_weight is null then 0 else tare_weight end), " +
							" (case when qty_per_art is null then 1 else qty_per_art end),  " +
							" pack_instr, dimension, " +
							" (case when rate is null then 0 else rate end), " +
							" (case when gross_wt_per_art is null then 0 else gross_wt_per_art end), " +
							" (case when tare_wt_per_art is null then 0 else tare_wt_per_art end), " +
							" exp_date, mfg_date, site_code__mfg, " +
							" (case when pallet_wt is null then 0 else pallet_wt end),retest_date " +
							" from 	 stock " +
							" where  item_code = ? " +
							" and 	 site_code = ? " +
							" and 	 loc_code  = ? " +
							" and 	 lot_no 	= ? " +
							" and 	 lot_sl 	= ? ";				
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_item_code);
					pStmt.setString(2, ls_site_code);
					pStmt.setString(3, ls_loc_code);
					pStmt.setString(4, ls_lot_no);
					pStmt.setString(5, ls_lot_sl);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						cnt++;
						lc_gross_weight =  rs.getDouble(1);
						lc_nett_weight =  rs.getDouble(2);
						lc_tare_weight =  rs.getDouble(3);
						lc_qty_per_art =  rs.getDouble(4);
						ls_pack_instr   = checkNull(rs.getString(5));
						ls_dimension   =  checkNull(rs.getString(6));
						lc_stcrate     =  rs.getDouble(7);
						lc_gross_weight_art =  rs.getDouble(8);
						lc_tare_weight_art =  rs.getDouble(9);
						ld_exp_date =  rs.getTimestamp(10);
						ld_mfg_date    =  rs.getTimestamp(11);
						ls_site_mfg  =  checkNull(rs.getString(12)); 
						lc_pallet_wt =  rs.getDouble(13);
						ldt_retest_date =  rs.getTimestamp(14);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 

					if( cnt > 0 )						
					{
						mNum =  Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));
						if( lc_qty_per_art > 0 )
						{
							valueXmlString.append("<no_art>").append("<![CDATA["+Math.round(mNum/lc_qty_per_art)+"]]>").append("</no_art>");
						}

						if( lc_qty_per_art == 0 ) 
						{
							lc_qty_per_art = 1;
						}

						lc_gross_weight = (lc_gross_weight_art/lc_qty_per_art) * mNum;
						lc_tare_weight  = (lc_tare_weight_art/lc_qty_per_art) * mNum;
						lc_nett_weight  = lc_gross_weight -  lc_tare_weight;					

						sql = " select stk_opt   ,(case when track_shelf_life is null then 'N' else track_shelf_life end) " +
								"	from item "+
								"where item_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_item_code);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							ls_stk_opt   = checkNull(rs.getString(1));
							ls_track_shelf_life   =  checkNull(rs.getString(2));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						if(! "0".equalsIgnoreCase(ls_stk_opt)) 
						{

							if("Y".equalsIgnoreCase(ls_track_shelf_life) && ld_exp_date != null )
							{
								ld_exp_date_str = sdf.format(ld_exp_date.getTime());
								valueXmlString.append("<exp_date>").append("<![CDATA["+ld_exp_date_str+"]]>").append("</exp_date>");
							}//end if
							if( ld_mfg_date != null)
							{
								ld_mfg_date_str = sdf.format(ld_mfg_date.getTime());
								valueXmlString.append("<mfg_date>").append("<![CDATA["+ld_mfg_date_str+"]]>").append("</mfg_date>");
							}
							else
							{
								valueXmlString.append("<mfg_date>").append("<![CDATA["+ld_mfg_date_str+"]]>").append("</mfg_date>");
							}

							valueXmlString.append("<site_code__mfg>").append("<![CDATA["+ls_site_mfg+"]]>").append("</site_code__mfg>");
							if( ldt_retest_date != null)
							{
								ldt_retest_date_str = sdf.format(ldt_retest_date.getTime());
								valueXmlString.append("<retest_date>").append("<![CDATA["+ldt_retest_date_str+"]]>").append("</retest_date>");
							}
							else
							{
								valueXmlString.append("<retest_date>").append("<![CDATA["+ldt_retest_date_str+"]]>").append("</retest_date>");
							}
						}//end if	

					}//end if

					if( isnull(ls_pack_instr) || lenTrim(ls_pack_instr) == 0 )
					{
						sql = " Select pack_instr  " +
								"	from sorddet where sale_order = ? " +
								" and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_saleord);
						pStmt.setString(2, ls_saleord_line);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							ls_pack_instr   = checkNull(rs.getString("pack_instr"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 
					}

					valueXmlString.append("<pallet_wt>").append("<![CDATA["+lc_pallet_wt+"]]>").append("</pallet_wt>");
					valueXmlString.append("<gross_weight>").append("<![CDATA["+lc_gross_weight+"]]>").append("</gross_weight>");
					valueXmlString.append("<nett_weight>").append("<![CDATA["+lc_nett_weight+"]]>").append("</nett_weight>");
					valueXmlString.append("<tare_weight>").append("<![CDATA["+lc_tare_weight+"]]>").append("</tare_weight>");
					valueXmlString.append("<pack_instr>").append("<![CDATA["+ls_pack_instr+"]]>").append("</pack_instr>");
					valueXmlString.append("<dimension>").append("<![CDATA["+ls_dimension+"]]>").append("</dimension>");

					lc_rate_std = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("rate__stduom", dom)));
					ls_saleord = genericUtility.getColumnValue("sord_no", dom);
					ls_saleord_line = genericUtility.getColumnValue("line_no__sord", dom);

					ls_saleord_line = "   "+ls_saleord_line;
					ls_saleord_line = ls_saleord_line.substring(ls_saleord_line.length()-3,ls_saleord_line.length());

					lc_sord_rate = 0;

					sql = " select (case when rate__stduom is null then 0 else rate__stduom end), " +
							" (case when rate__clg is null then 0 else rate__clg end),quantity__stduom " +
							" from sorddet where sale_order = ? and line_no = ? ";	
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_saleord);
					pStmt.setString(2, ls_saleord_line);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						lc_sord_rate   = rs.getDouble(1);
						lc_sord_exc_rate   = rs.getDouble(2);
						lc_sord_quantity   = rs.getDouble(3);
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 

					ls_explev = genericUtility.getColumnValue("exp_lev", dom);
					ls_saleorder = genericUtility.getColumnValue("sord_no", dom);
					ls_linenosord = genericUtility.getColumnValue("line_no__sord", dom);

					ls_linenosord = "   "+ls_linenosord;
					ls_linenosord = ls_linenosord.substring(ls_linenosord.length()-3,ls_linenosord.length());


					sql = " select nature "+
							" from sorditem "+
							" where	sale_order = ? "+ 
							" and	line_no    = ? "+
							" and 	exp_lev	   = ? "+
							" and 	line_type  <> 'B' ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_saleorder);
					pStmt.setString(2, ls_linenosord);
					pStmt.setString(3, ls_explev);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_nature   = checkNull(rs.getString("nature"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 


					sql = " select price_list, price_list__disc, pl_date , order_date," +
							" cust_code, price_list__clg " +
							" from 	sorder where 	sale_order = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, ls_saleorder);
					rs = pStmt.executeQuery();
					if (rs.next())
					{
						ls_price_list   = checkNull(rs.getString("price_list"));
						ls_plist_disc   = checkNull(rs.getString("price_list__disc"));
						ld_plist_date   = rs.getTimestamp("pl_date");
						ld_order_date   = rs.getTimestamp("order_date");
						ls_custcode   = checkNull(rs.getString("cust_code"));
						ls_price_list__clg   = checkNull(rs.getString("price_list__clg"));
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null; 

					//gbf_set_rate_conversion();
					valueXmlString = gbf_set_rate_conversion(dom,valueXmlString, conn);

					if( isnull(ls_nature))
					{
						ls_nature = "C";
					}

					if ( lc_sord_rate == 0 && "C".equalsIgnoreCase(ls_nature))
					{

						if( (! isnull(ls_plist_disc)) && lenTrim(ls_plist_disc) > 0 )
						{
							PostOrdDespatchGen poDespatchGen = new PostOrdDespatchGen();
							lc_plist_disc = poDespatchGen.getDiscount(ls_plist_disc, ld_order_date, ls_custcode, ls_site_code, ls_item_code, ls_unitstd, lc_disc_merge, ld_plist_date, lc_sord_quantity, conn);
							//lc_plist_disc = i_nvo_gbf_func.gbf_get_discount(ls_plist_disc,ld_order_date,ls_custcode,ls_site_code,ls_item_code,ls_unitstd,lc_disc_merge,ld_plist_date,lc_sord_quantity);

							if("M".equalsIgnoreCase(distCommon.getPriceListType(ls_plist_disc, conn) ))
							{
								if(!"L".equalsIgnoreCase(distCommon.getPriceListType(ls_price_list, conn) ));	
								{
									//lc_pick_rate = nvo_dis.gbf_pick_rate(ls_price_list,ld_desp_date,ls_item_code,ls_lot_no,'D',lc_sord_quantity);
									lc_pick_rate = distCommon.pickRate(ls_price_list, despDateStr, ls_item_code, ls_lot_no, "D", lc_sord_quantity, conn);
								}//end if
								//lc_pick_rate = nvo_postord.gbf_calc_rate(lc_pick_rate,lc_plist_disc);
								lc_pick_rate = calRate(lc_pick_rate,lc_plist_disc);

							}
							else
							{	

								sql = " select count(1) from pricelist "+ 
										" where price_list =  ? and list_type = 'I' ";
								pStmt = conn.prepareStatement(sql);
								pStmt.setString(1, ls_price_list);
								rs = pStmt.executeQuery();
								if (rs.next())
								{
									ll_count   = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pStmt.close();
								pStmt = null; 

								if( ll_count == 0 )
								{		
									if( !"L".equalsIgnoreCase(distCommon.getPriceListType(ls_price_list,conn) ))	
									{
										//lc_pick_rate = nvo_dis.gbf_pick_rate(ls_price_list,ld_desp_date,ls_item_code,ls_lot_no,"D",lc_sord_quantity);
										lc_pick_rate = distCommon.pickRate(ls_price_list, despDateStr, ls_item_code, ls_lot_no, "D", lc_sord_quantity, conn);
									}//end if
								}
								else	//list_type = 'I'
								{
									//lc_pick_rate = nvo_dis.gbf_pick_rate(ls_price_list,ld_desp_date,ls_item_code,ls_site_code+"~t"+ls_loc_code+"~t"+ls_lot_no,"I",lc_sord_quantity);
									//lc_pick_rate = distCommon.pickRate(ls_price_list, despDateStr, ls_item_code, ls_site_code+"~t"+ls_loc_code+"~t"+ls_lot_no, "I", lc_sord_quantity, conn);
									lc_pick_rate = distCommon.pickRate(ls_price_list, despDateStr, ls_item_code,ls_lot_no, "I", lc_sord_quantity, conn);
								}//	end if

							}//	end if 
						}
						else
						{

							if( lc_sord_rate == 0 )
							{
								if(  lenTrim(ls_price_list) > 0 )
								{   

									ll_count = 0;
									sql = " select count(1)  from   pricelist " +
											" where  price_list = ? and list_type = 'I' ";
									pStmt = conn.prepareStatement(sql);
									pStmt.setString(1, ls_price_list);
									rs = pStmt.executeQuery();
									if (rs.next())
									{
										ll_count   = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pStmt.close();
									pStmt = null; 


									if( ll_count == 0 )
									{
										//ls_list_type = gbf_get_pricelist_type(ls_price_list);
										ls_list_type = distCommon.getPriceListType(ls_price_list, conn); 
										if( isnull(ls_list_type) || lenTrim(ls_list_type) == 0 )
										{
											sql = " select price_list__parent 	from pricelist "+
													" where price_list = ? ";
											pStmt = conn.prepareStatement(sql);
											pStmt.setString(1, ls_price_list);
											rs = pStmt.executeQuery();
											if (rs.next())
											{
												ls_price_list__parent   = checkNull(rs.getString("price_list__parent"));
											}
											rs.close();
											rs = null;
											pStmt.close();
											pStmt = null; 

											//ls_list_type = gbf_get_pricelist_type(ls_price_list__parent);
											ls_list_type = distCommon.getPriceListType(ls_price_list__parent, conn);
										}//	end if

										if("B".equalsIgnoreCase(ls_list_type))
										{
											//lc_pick_rate = nvo_dis.gbf_pick_rate(ls_price_list,ld_desp_date,ls_item_code,ls_lot_no,'B',lc_sord_quantity);
											lc_pick_rate = distCommon.pickRate(ls_price_list, despDateStr, ls_item_code, ls_lot_no, "B", lc_sord_quantity, conn);
										}
										else
										{	
											lc_pick_rate = -1;
										}
									}
									else // if ll_count > 0 then
									{
										//lc_pick_rate = nvo_dis.gbf_pick_rate(ls_price_list,ld_desp_date,ls_item_code,ls_site_code+"~t"+ls_loc_code+"~t"+ls_lot_no,"I",lc_sord_quantity);
										lc_pick_rate = distCommon.pickRate(ls_price_list, despDateStr, ls_item_code,ls_lot_no, "I", lc_sord_quantity, conn);
									}
									//		end if
									//added by prajakta as insisted by manoharji on 31/07/06 as it was not setting std rate
								}//		end if	
							}//		end if
						}//		end if

						lc_conv = Double.parseDouble( checkDoubleNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom)));
						//if( isnull(lc_conv) || lc_conv = 0 )
						if( lc_conv == 0 )
						{
							lc_conv = 1;
						}
						valueXmlString.append("<rate__stduom>").append("<![CDATA["+(lc_pick_rate *  lc_conv)+"]]>").append("</rate__stduom>");
					}
					lc_pick_rate__clg = 0;
					if( lenTrim(ls_price_list__clg) > 0 )
					{
						//lc_pick_rate__clg = nvo_dis.gbf_pick_rate(ls_price_list__clg ,ld_desp_date,ls_item_code,ls_site_code+"~t"+ls_loc_code+"~t"+ls_lot_no,"B",lc_sord_quantity);
						//lc_pick_rate__clg = distCommon.pickRate(ls_price_list__clg, despDateStr, ls_item_code, ls_site_code,ls_loc_code,ls_lot_no, "B", lc_sord_quantity, conn);
						lc_pick_rate__clg = distCommon.pickRate(ls_price_list__clg, despDateStr, ls_item_code,ls_lot_no, "B", lc_sord_quantity, conn);
						valueXmlString.append("<rate__clg>").append("<![CDATA["+lc_pick_rate__clg+"]]>").append("</rate__clg>");
					}
					else
					{
						mVal = genericUtility.getColumnValue("sord_no", dom);		
						mcode = genericUtility.getColumnValue("line_no__sord", dom);

						mcode = "   "+mcode;
						mcode = mcode.substring(mcode.length()-3,mcode.length());

						sql = "	select rate__clg  from sorddet " +
								" where sale_order = ? and line_no = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, mVal);
						pStmt.setString(2, mcode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							lc_pick_rate__clg   = rs.getDouble("rate__clg");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 


						if(  lc_pick_rate__clg	==  0 )
						{	
							valueXmlString.append("<rate__clg>").append("<![CDATA["+lc_pick_rate+"]]>").append("</rate__clg>");
						}
						else
						{
							valueXmlString.append("<rate__clg>").append("<![CDATA["+lc_pick_rate__clg+"]]>").append("</rate__clg>");
						}//End If
					}//end if

					// C and P nature added by nandkumar gadkari on 06/08/19
					if ("F".equalsIgnoreCase(ls_nature) || "B".equalsIgnoreCase(ls_nature) || "S".equalsIgnoreCase(ls_nature) || "I".equalsIgnoreCase(ls_nature) || "V".equalsIgnoreCase(ls_nature) || "P".equalsIgnoreCase(nature) )
					{
						
						valueXmlString.append("<rate__stduom>").append("<![CDATA[0]]>").append("</rate__stduom>");
						valueXmlString.append("<line_type>").append("<![CDATA["+ls_nature+"]]>").append("</line_type>");
					}//end if
					if("C".equalsIgnoreCase(ls_nature))//added by nandkumar gadkari on 19/08/19
					{
						valueXmlString.append("<line_type>").append("<![CDATA["+ls_nature+"]]>").append("</line_type>");
					}
				} //end ic
				else if (currentColumn.trim().equalsIgnoreCase("loc_code") ) 
				{ 
					//System.out.println("------ Inside loc_code ----------- ");
					//gbf_set_rate_conversion();
					valueXmlString = gbf_set_rate_conversion(dom,valueXmlString, conn);
				}
				else if (currentColumn.trim().equalsIgnoreCase("no_art") ) 
				{ 
					//System.out.println("------ Inside no_art ----------- ");
					lc_qty_stduom = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("quantity__stduom", dom)));
					lc_no_article = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("no_art", dom)));


					if(  lc_no_article > 0 )
					{
						lc_pack_qty = lc_qty_stduom / lc_no_article;
						valueXmlString.append("<pack_qty>").append("<![CDATA["+lc_pack_qty+"]]>").append("</pack_qty>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("cust_item__ref") ) 
				{ 

					//System.out.println("------ Inside cust_item__ref ----------- ");
					ls_cust_code = checkDoubleNull(genericUtility.getColumnValue("cust_code", dom));
					ls_item_cd = checkDoubleNull(genericUtility.getColumnValue("cust_item__ref", dom));

					if( (! isnull(ls_item_cd)) &&  lenTrim(ls_item_cd)>0 ) 
					{
						sql = " select item_code,descr " +
								"	from  customeritem " +
								"	where	cust_code = ? " +
								"	and   item_code__ref  = ?  ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, ls_cust_code);
						pStmt.setString(2, ls_item_cd);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							ls_item   =  checkNull(rs.getString("item_code"));
							ls_custitemdesc   =  checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null; 

						mdescr = getValue("descr", "item", "item_code", ls_item, conn);	

						valueXmlString.append("<item_code>").append("<![CDATA["+ls_item+"]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA["+mdescr+"]]>").append("</item_descr>");

						if( isnull(ls_item_cd ) || lenTrim(ls_item_cd) == 0 ) 
						{
							//dw_detedit[ii_currformno].SetItem(1,"",  ' ')
							valueXmlString.append("<custitem_desc>").append("<![CDATA[ ]]>").append("</custitem_desc>");
						}
						else
						{
							valueXmlString.append("<custitem_desc>").append("<![CDATA["+ls_custitemdesc+"]]>").append("</custitem_desc>");
						}
					}	//end if  /
				}		

				valueXmlString.append("</Detail2>");
				break;

			}
			valueXmlString.append("</Root>");
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pStmt != null) {
						pStmt.close();
						pStmt = null;

					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}


	// gbf_calc_rate
	public double calRate(double discPer, double adRate)
	{
		if (adRate == 0)
		{
			adRate = 0;
		}
		if (discPer == 0)
		{
			discPer = 0;
		}
		adRate = adRate - (discPer * adRate) / 100;
		if (adRate < 0)
		{
			adRate = 0;
		}
		return adRate;
	}


	private double gbf_calc_detdisc_amt(String as_sorder,
			String as_lineno, String itemcode, double as_rate, double as_qty,
			String as_flag, Connection conn) throws SQLException 
			{

		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		String sql = "",sql1 = "";

		String ls_schemeitem_cd="",ls_disctype="",ls_errcode="",ls_temp="";

		double lc_disc_rate=0;
		double lc_disc_perc=0,lc_tot_rate=0,lc_diff_rate=0,lc_schemeoffinv_hdramt=0,lc_schemebb_hdramt=0;
		double lc_schemeoffinvfix_detamt=0,lc_schemebbfix_detamt=0,lc_disc_amt = 0	;
		double lc_schemeoffinvper_detamt = 0,lc_schemebbper_detamt = 0,lc_cash_value_item=0,lc_schemeoffinvcash_detamt = 0,lc_schemebbcash_detamt = 0;
		double lc_stcrate=0, lc_no_article=0, lc_pack_qty=0, lc_qty_stduom=0,lc_rnd_to=0;
		double lc_conv = 0;
		String ls_ErrorCode="",ls_schemehdr="",ls_schemedet="",ls_dis_type="",ls_promo_term="",ls_price_var="",ls_rnd_off="";

		sql = " select scheme_code from sorderdet_scheme where tran_id = ? " +
				" and line_no_form = ?  ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, as_sorder);
		pstmt.setString(2, as_lineno);
		rs = pstmt.executeQuery();
		while (rs.next()) 
		{
			ls_schemedet = rs.getString(1);


			if(  (! isnull(ls_schemedet)) && ls_schemedet.length() > 0 )
			{

				sql1 = " select promo_term,price_var,rnd_off,rnd_to " +
						" from bom 	where bom_code = ? " ; 
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, ls_schemedet);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					ls_promo_term = checkNull(rs1.getString("promo_term"));
					ls_price_var = checkNull(rs1.getString("price_var"));
					ls_rnd_off = checkNull(rs1.getString("rnd_off"));
					lc_rnd_to = rs1.getDouble("rnd_to");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;


				sql1 = " select disc_perc ,disc_type " +
						" from bom where  bom_code = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, ls_schemedet);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					lc_disc_perc = rs1.getDouble("disc_perc");
					ls_dis_type = checkNull(rs1.getString("disc_type"));
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;



				if("F".equalsIgnoreCase(ls_dis_type))
				{
					if(!"N".equalsIgnoreCase(ls_rnd_off)) 
					{
						//lc_disc_perc = gf_get_rndamt(lc_disc_perc, ls_rnd_off, lc_rnd_to)
						lc_disc_perc = distCommon.getRndamt(lc_disc_perc, ls_rnd_off, lc_rnd_to);		
					}
					if("0".equalsIgnoreCase(ls_promo_term))
					{						
						lc_schemeoffinvfix_detamt = 	lc_schemeoffinvfix_detamt + lc_disc_perc; 	
						//ls_temp = "Discount_type:--"+ls_dis_type+"Promo_term:--"+ls_promo_term+"lc_schemeoffinvper_detamt:--"+string(lc_schemeoffinvfix_detamt);
					}
					else
					{
						if("1".equalsIgnoreCase(ls_promo_term) )
						{
							lc_schemebbfix_detamt = 	lc_schemebbfix_detamt + lc_disc_perc; 																																		
							//ls_temp = "Discount_type:--"+ls_dis_type+"Promo_term:--"+ls_promo_term+"lc_schemebbinvper_detamt:--"+string(lc_schemebbfix_detamt);
						}//end if
					}//end if
				}else if("P".equalsIgnoreCase(ls_dis_type))
				{
					if("P".equalsIgnoreCase(ls_dis_type))
					{	
						lc_disc_rate = (as_rate * lc_disc_perc/100 );

						if("0".equalsIgnoreCase(ls_promo_term))
						{	
							if(!"N".equalsIgnoreCase(ls_rnd_off))
							{
								//lc_schemeoffinvper_detamt = lc_schemeoffinvper_detamt + gf_get_rndamt((as_qty*lc_disc_rate), ls_rnd_off, lc_rnd_to); 
								lc_schemeoffinvper_detamt = lc_schemeoffinvper_detamt + distCommon.getRndamt((as_qty*lc_disc_rate), ls_rnd_off, lc_rnd_to); 
							}
							else
							{
								lc_schemeoffinvper_detamt = lc_schemeoffinvper_detamt + (as_qty*lc_disc_rate) ;
							}

							//ls_temp = "Discount_type:--"+ls_dis_type+"Promo_term:--"+ls_promo_term+"lc_schemeoffinvper_detamt:--"+string(lc_schemeoffinvper_detamt);
						}else
						{
							if("1".equalsIgnoreCase(ls_promo_term)) 
							{
								if(!"N".equalsIgnoreCase(ls_rnd_off))
								{
									lc_schemebbper_detamt = lc_schemebbper_detamt + distCommon.getRndamt((as_qty*lc_disc_rate), ls_rnd_off, lc_rnd_to); 	
								}
								else
								{
									lc_schemebbper_detamt = lc_schemebbper_detamt + (as_qty*lc_disc_rate); 	
								}
								//end if
								//ls_temp = "Discount_type:--"+ls_dis_type+"Promo_term:--"+ls_promo_term+"lc_schemebbinvper_detamt:--"+string(lc_schemebbper_detamt);
							}//end if
						}//end if
					}//end if
				}
				else
				{	
					if("C".equalsIgnoreCase(ls_dis_type))
					{

						sql1= " select cash_value_item from bom where  bom_code = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, ls_schemedet);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							lc_cash_value_item = rs1.getDouble("cash_value_item");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if("0".equalsIgnoreCase(ls_promo_term))
						{	
							if(!"N".equalsIgnoreCase(ls_rnd_off))
							{
								lc_schemeoffinvcash_detamt = lc_schemeoffinvcash_detamt + distCommon.getRndamt((lc_cash_value_item*as_rate), ls_rnd_off, lc_rnd_to); 
							}
							else
							{
								lc_schemeoffinvcash_detamt = lc_schemeoffinvcash_detamt + (lc_cash_value_item*as_rate); 								
							}
							//ls_temp = "Discount_type:--"+ls_dis_type+"Promo_term:--"+ls_promo_term+"lc_schemeoffinvcash_detamt:--"+string(lc_schemeoffinvcash_detamt)
						}
						else
						{
							if("1".equalsIgnoreCase(ls_promo_term)) 
							{
								if("0".equalsIgnoreCase(ls_promo_term))
								{
									lc_schemebbcash_detamt = lc_schemebbcash_detamt + distCommon.getRndamt((lc_cash_value_item*as_rate), ls_rnd_off, lc_rnd_to);
								}
								else
								{
									lc_schemebbcash_detamt = lc_schemebbcash_detamt + (lc_cash_value_item*as_rate);
								}
								//ls_temp = "Discount_type:--"+ls_dis_type+"Promo_term:--"+ls_promo_term+"lc_schemebbcash_detamt:--"+string(lc_schemebbcash_detamt)
							}
						}
					}
				}

			}//end if

		} //end while
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;


		if("O".equalsIgnoreCase(as_flag))
		{
			lc_disc_amt = lc_schemeoffinvfix_detamt; 
			if("D".equalsIgnoreCase(ls_price_var))
			{
				lc_disc_amt = lc_schemeoffinvfix_detamt + lc_schemeoffinvper_detamt + lc_schemeoffinvcash_detamt;
			}
		}
		else if("B".equalsIgnoreCase(as_flag))
		{
			lc_disc_amt = lc_schemebbfix_detamt;
			if("D".equalsIgnoreCase(ls_price_var))	
			{
				lc_disc_amt = lc_schemebbfix_detamt + lc_schemebbper_detamt + lc_schemebbcash_detamt;
			}
		}

		return lc_disc_amt;

			}

	private HashMap gbf_stock_check(Document dom,
			String as_item_code, String as_lot_no, String as_loc_code,
			String as_site_code, double as_order_qty, Connection conn) throws Exception 
			{
		HashMap stockCheckMap = new HashMap();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		int cnt=0;
		String   ls_lot_sl="",  ls_site_code_mfg=""	, ls_pack_code="", ls_dimension="";		
		String	ls_supp_code__mfg="" ,	ls_loc_code="", ls_Errcode="",ls_available="";
		double  lc_qty_stk=0,lc_alloc_qty, lc_gross_weight=0, lc_tare_weight=0, lc_net_weight=0;
		double	lc_grossper=0,lc_netper=0,lc_tareper=0, lc_hold_qty=0;
		double	lc_conv__qty_stduom=0;
		Timestamp ld_exp_date=null,	ld_mfg_date=null;
		String ld_exp_date_str="",ld_mfg_date_str="";

		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				genericUtility.getApplDateFormat());
		String sysDate = sdf.format(currentDate.getTime());
		//System.out.println("Now the date is :=>  " + sysDate);

		StringBuffer valueXmlString = new StringBuffer();

		if( isnull(as_loc_code) || lenTrim(as_loc_code) == 0 ) 
		{
			as_loc_code = "%";
		}
		else
		{	
			ls_loc_code = as_loc_code;
			as_loc_code = as_loc_code.trim() + "%";
		}//end if	

		lc_conv__qty_stduom = Double.parseDouble(checkDoubleNull(genericUtility.getColumnValue("conv__qty_stduom", dom)));

		sql = " select a.lot_no, a.lot_sl, a.quantity, a.exp_date, "+
				" a.site_code__mfg, a.mfg_date, a.alloc_qty, "+
				" a.pack_code, a.loc_code, "+
				" a.gross_weight, a.tare_weight, a.net_weight, "+
				" a.dimension, a.supp_code__mfg, a.hold_qty"+
				" from stock a, invstat b "+
				" where a.inv_stat  				= b.inv_stat"+
				" and a.item_code 					= ? "+
				" and a.site_code 					= ? "+
				" and a.loc_code  					like ? "+
				" and a.lot_no 						= ? "+
				" and b.available 					= 'Y' "+
				" and b.usable 						= 'Y' "+
				" and a.quantity - a.alloc_qty - case when a.hold_qty is null then 0 else a.hold_qty end	> 0 "+
				" order by a.partial_used, a.exp_date, a.lot_no, a.lot_sl "; 
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, as_item_code);
		pstmt.setString(2, as_site_code);
		pstmt.setString(3, as_loc_code);
		pstmt.setString(4, as_lot_no);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			cnt++;
			as_lot_no = checkNull(rs.getString("lot_no"));
			ls_lot_sl= checkNull(rs.getString("lot_sl")) ;
			lc_qty_stk= rs.getDouble("quantity") ;
			ld_exp_date= rs.getTimestamp("exp_date"); 
			ls_site_code_mfg= checkNull(rs.getString("site_code__mfg")) ;
			ld_mfg_date= rs.getTimestamp("mfg_date") ;
			lc_alloc_qty= rs.getDouble("alloc_qty");
			ls_pack_code= checkNull(rs.getString("pack_code")) ;
			ls_loc_code= checkNull(rs.getString("loc_code"));
			lc_gross_weight= rs.getDouble("gross_weight");
			lc_tare_weight= rs.getDouble("tare_weight");
			lc_net_weight= rs.getDouble("net_weight");
			ls_dimension= checkNull(rs.getString("dimension"));
			ls_supp_code__mfg= checkNull(rs.getString("supp_code__mfg")) ;
			lc_hold_qty= rs.getDouble("hold_qty") ;

			if( ld_exp_date != null)
			{
				ld_exp_date_str = sdf.format(ld_exp_date.getTime());
			}
			if( ld_mfg_date != null)
			{
				ld_mfg_date_str = sdf.format(ld_mfg_date.getTime());
			}

			if( (lc_qty_stk - lc_alloc_qty - lc_hold_qty) >= as_order_qty )
			{
				if( lc_qty_stk > 0 )
				{
					lc_grossper = lc_gross_weight / lc_qty_stk;
					lc_netper 	= lc_net_weight 	/ lc_qty_stk;
					lc_tareper 	= lc_tare_weight 	/ lc_qty_stk;

					lc_gross_weight = as_order_qty * lc_grossper;
					lc_net_weight   = as_order_qty * lc_netper;
					lc_tare_weight  = as_order_qty * lc_tareper;
				}//end if
				//System.out.println("@@@@@5898 as_order_qty * lc_conv__qty_stduom["+(as_order_qty * lc_conv__qty_stduom)+"]");
				valueXmlString.append("<loc_code>").append("<![CDATA["+ls_loc_code+"]]>").append("</loc_code>");
				valueXmlString.append("<quantity>").append("<![CDATA["+as_order_qty+"]]>").append("</quantity>");
				valueXmlString.append("<quantity_real>").append("<![CDATA["+as_order_qty+"]]>").append("</quantity_real>");
				valueXmlString.append("<quantity__stduom>").append("<![CDATA["+(as_order_qty * lc_conv__qty_stduom)+"]]>").append("</quantity__stduom>");
				valueXmlString.append("<lot_no>").append("<![CDATA["+as_lot_no+"]]>").append("</lot_no>");
				valueXmlString.append("<lot_sl>").append("<![CDATA["+ls_lot_sl+"]]>").append("</lot_sl>");
				valueXmlString.append("<gross_weight>").append("<![CDATA["+lc_gross_weight+"]]>").append("</gross_weight>");
				valueXmlString.append("<nett_weight>").append("<![CDATA["+lc_net_weight+"]]>").append("</nett_weight>");
				valueXmlString.append("<tare_weight>").append("<![CDATA["+lc_tare_weight+"]]>").append("</tare_weight>");
				valueXmlString.append("<dimension>").append("<![CDATA["+ls_dimension+"]]>").append("</dimension>");
				valueXmlString.append("<site_code__mfg>").append("<![CDATA["+ls_site_code_mfg+"]]>").append("</site_code__mfg>");
				valueXmlString.append("<mfg_date>").append("<![CDATA["+ld_mfg_date_str+"]]>").append("</mfg_date>");
				valueXmlString.append("<exp_date>").append("<![CDATA["+ld_exp_date_str+"]]>").append("</exp_date>");

				lc_gross_weight = 0;
				lc_tare_weight = 0;
				lc_net_weight = 0;
			}
			else
			{
				//ls_ErrCode = 'VTNOSTOCK'
				stockCheckMap.put("errorCode", "VTNOSTOCK") ;
			}//	end if



		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if(cnt==0)
		{
			stockCheckMap.put("errorCode", "VTNOSTOCK") ;
		}

		stockCheckMap.put("valueXmlString", valueXmlString) ;

		//System.out.println("@@@@@@@ stockCheckMap["+stockCheckMap+"]");
		return stockCheckMap;
			}

	private StringBuffer gbf_set_rate_conversion(Document dom, StringBuffer valueXmlString, Connection conn) throws ITMException, SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";

		double lc_rate=0,lc_rtconv=0;
		double ld_gross_wt_perart=0, ld_tare_wt_perart=0, ld_net_wt_perart=0;
		String ls_item_code="", ls_site_code="", ls_loc_code="", ls_lot_no="", ls_lot_sl="";
		String ls_saleord="", ls_saleord_line="", ls_item_type="", ls_unit_wt="", ls_unit_rate="";
		String ls_rate_opt="", ls_unit="", ls_unit_rate_sord="";

		ls_item_code = checkNull(genericUtility.getColumnValue("item_code__ord", dom));
		ls_loc_code = checkNull(genericUtility.getColumnValue("loc_code", dom));
		ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
		ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", dom));
		ls_saleord = checkNull(genericUtility.getColumnValue("sord_no", dom));
		ls_saleord_line = checkNull(genericUtility.getColumnValue("line_no__sord", dom));


		ls_saleord_line = "   "+ls_saleord_line;
		ls_saleord_line = ls_saleord_line.substring(ls_saleord_line.length()-3,ls_saleord_line.length());


		int cnt=0;
		sql = " select conv__rtuom_stduom from sorddet where sale_order = ? and line_no = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ls_saleord);
		pstmt.setString(2, ls_saleord_line);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			cnt++;
			lc_rtconv = rs.getDouble(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if(cnt > 0 )
		{
			valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA["+lc_rtconv+"]]>").append("</conv__rtuom_stduom>");
		}

		//ls_saleord_line = "   "+ls_saleord_line;
		//ls_saleord_line = ls_saleord_line.substring(ls_saleord_line.length()-3,ls_saleord_line.length());

		sql=" select item_type, unit__netwt, unit__rate from item where item_code = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ls_item_code);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			ls_item_type = rs.getString("item_type");
			ls_unit_wt = rs.getString("unit__netwt");
			ls_unit_rate = rs.getString("unit__rate");
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if( ( ! isnull(ls_item_type)) && lenTrim(ls_item_type) > 0 )
		{
			ls_rate_opt = getValue("rate_opt", "item_type", "item_type", ls_item_type, conn);

			if("-1".equalsIgnoreCase(ls_rate_opt))
			{	
				if( ls_unit_wt.trim().equalsIgnoreCase(ls_unit_rate.trim()))
				{
					ls_unit = checkDoubleNull(genericUtility.getColumnValue("unit", dom));
					ls_site_code = getValue("site_code", "sorder", "sale_order", ls_saleord, conn);

					sql = "select unit__rate from sorddet where sale_order = ? and	line_no	  = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_saleord);
					pstmt.setString(1, ls_saleord_line);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						ls_unit_rate_sord = checkNull(rs.getString("unit__rate"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(! ls_unit.trim().equalsIgnoreCase(ls_unit_rate_sord.trim())) 
					{

						sql = " select gross_wt_per_art, tare_wt_per_art " +
								"	from stock where item_code = ? " +
								"	and	  site_code = ?  " +
								"	and   loc_code	= ?  " +
								"	and	  lot_no	= ?  " +
								"	and   lot_sl	= ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_item_code);
						pstmt.setString(1, ls_site_code);
						pstmt.setString(1, ls_loc_code);
						pstmt.setString(1, ls_lot_no);
						pstmt.setString(1, ls_lot_sl);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ld_gross_wt_perart = rs.getDouble("gross_wt_per_art");
							ld_tare_wt_perart = rs.getDouble("tare_wt_per_art");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						ld_net_wt_perart = ld_gross_wt_perart - ld_tare_wt_perart;

						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA["+ld_net_wt_perart+"]]>").append("</conv__rtuom_stduom>");
						//dw_detedit[ii_currformno].SetItem(1, "rate__stduom", (ld_net_wt_perart * lc_rate)) //changed by prajakta as insisted by manoharji on 31/07/06

					}//end if
				}//		end if
			}//			end if
		}//					end if
		return valueXmlString;
			}



	private double calc_discount_rate(String as_sorder,
			String as_lineno, String ls_item_code, double as_rate, // lc_ratestd,
			String as_flag, Connection conn) throws SQLException 
			{

		String ls_schemeitem_cd="",ls_disctype="",ls_errcode="";
		double lc_disc_amt=0,lc_totdisc_amt=0,lc_disc_perc=0;
		double lc_tot_rate=0,lc_diff_rate=0,lc_disc_rate=0,lc_schemeoffinv_hdramt=0,lc_schemebb_hdramt=0,lc_schemeoffinv_rate=0,lc_schemebb_rate=0,mrate=0;
		String ls_ErrorCode="",ls_schemehdr="",ls_schemedet="",ls_dis_type=""	,ls_promo_term="",ls_price_varience="";

		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		String sql = "",sql1 = "";


		sql = " select scheme_code from sorderdet_scheme " +
				" where tran_id = ? and line_no_form = ?  ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, as_sorder);
		pstmt.setString(2, as_lineno);
		rs = pstmt.executeQuery();
		while (rs.next())
		{
			ls_schemedet = checkNull(rs.getString(1));
			//System.out.println("@@@@@ ls_schemedet["+ls_schemedet+"]");
			if(! isnull(ls_schemedet) && ls_schemedet.length() > 0 )
			{	
				sql1 = " select promo_term,price_var " +
						"	from bom where bom_code = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, ls_schemedet);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					ls_promo_term = checkNull(rs1.getString(1));
					ls_price_varience = checkNull(rs1.getString(2));
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;


				if("D".equalsIgnoreCase(ls_price_varience))
				{
					mrate = -1;
					//		exit
					break;
				}

				sql1= "select disc_perc ,disc_type from bom	where  bom_code = ? "; 
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, ls_schemedet);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					lc_disc_perc = rs1.getDouble(1);
					ls_dis_type = checkNull(rs1.getString(2));
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;



				if("0".equalsIgnoreCase(ls_promo_term) && "P".equalsIgnoreCase(ls_dis_type))
				{
					if("F".equalsIgnoreCase(ls_dis_type))
					{	
						if("0".equalsIgnoreCase(ls_promo_term))
						{
							lc_disc_rate = (as_rate - lc_disc_perc );
							lc_schemeoffinv_rate = as_rate - lc_disc_rate;
						}
						else
						{	
							if("1".equalsIgnoreCase(ls_promo_term)) 
							{
								lc_disc_rate = (as_rate - lc_disc_perc );
								lc_schemebb_rate = as_rate - lc_disc_rate;
							}//end if
						}//end if
						//exit
						break;
					}
					else
					{	
						if("P".equalsIgnoreCase(ls_dis_type))
						{
							if("0".equalsIgnoreCase(ls_promo_term))
							{
								lc_disc_rate = (as_rate * lc_disc_perc/100 );
								lc_schemeoffinv_rate = as_rate - lc_disc_rate;
							}
							else
							{
								if("1".equalsIgnoreCase(ls_promo_term)) 
								{
									lc_disc_rate = (as_rate * lc_disc_perc/100 );
									lc_schemebb_rate = as_rate - lc_disc_rate;
								}
							}
							//exit
							break;
						}
					}
					mrate = -1;
					//exit
					break;
					//					end if
				}//	end if
			}//			end if
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;




		if("O".equalsIgnoreCase(as_flag))
		{
			mrate = lc_schemeoffinv_rate;
		}
		else
		{
			mrate = lc_schemebb_rate;
		}

		return mrate;
			}

	private static String getAbsString(String str) {
		return (str == null || str.trim().length() == 0
				|| "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}

	private static void setNodeValue(Document dom, String nodeName,
			double nodeVal) throws Exception {
		setNodeValue(dom, nodeName, Double.toString(nodeVal));
	}

	private static void setNodeValue(Document dom, String nodeName,
			String nodeVal) throws Exception {
		Node tempNode = dom.getElementsByTagName(nodeName).item(0);

		if (tempNode != null) {
			if (tempNode.getFirstChild() == null) {
				CDATASection cDataSection = dom.createCDATASection(nodeVal);
				tempNode.appendChild(cDataSection);
			} else {
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}

	private String getValue(String colName, String table, String field,
			String value, Connection conn) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "",descr="";
		//System.out.println("@@@@@@@@colName["+colName+"]table["+table+"]field["+field+"]value["+value+"]");

		sql = "select " + colName + " from " + table + " where " + field
				+ " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			descr = checkNull(rs.getString(1));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		//System.out.print("========>::descr[" + descr + "]");
		return descr;
	}

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input;
	}

	private String checkDoubleNull(String input) {
		if (input == null || input.trim().length() == 0 ) 
		{
			input = "0";
		}
		return input;
	}

	private boolean isnull(String input) {
		if (input == null)
		{
			return  true;
		}
		else
		{
			return  false;	
		}
	}
	private int lenTrim(String input) {
		if (input != null)
		{
			return  input.trim().length();
		}
		else
		{
			return  0;	
		}
	}
	private HashMap<String, Double> sampleValueCal(Document dom,String ls_item_code__parent,String ls_cur_lineno, String ls_scheme_flag,double lc_rate,Connection conn) throws SQLException
	{
		//System.out.println("@@@@@@@@ inside sampleValueCal...ls_item_code__parent["+ls_item_code__parent+"]ls_cur_lineno["+ls_cur_lineno+"]ls_scheme_flag["+ls_scheme_flag+"]lc_rate["+lc_rate+"]");

		HashMap<String, Double> sampleValueMap = new HashMap<String, Double>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "",ls_line_no="",ls_brow_item_code="",ls_brow_saleorder="",ls_brow_lineno="",ls_item_code__parent_cur="";
		double quantity=0,lc_prv_free_value=0,lc_prv_bonus_value=0,lc_prv_sample_value=0,lc_prv_charge_value=0;
		double lc_prv_free_qty=0, lc_prv_bonus_qty=0, lc_prv_sample_qty=0, lc_prv_charge_qty=0;
		String ls_nature="";

		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;

		parentList = dom.getElementsByTagName("Detail2");
		int parentNodeListLength = parentList.getLength();
		for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr-- )
		{	
			parentNode = parentList.item(prntCtr-1);
			childList = parentNode.getChildNodes();
			for (int ctr = 0; ctr < childList.getLength(); ctr++)
			{
				childNode = childList.item(ctr);

				if(childNode != null &&  childNode.getNodeName().equalsIgnoreCase("attribute"))
				{
					String updateFlag = "";
					updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
					if (updateFlag.equalsIgnoreCase("D"))
					{
						//System.out.println("Break from here as the record is deleted");
						break;
					}
				}	

				if ( childNode != null && childNode.getFirstChild() != null &&  
						childNode.getNodeName().equalsIgnoreCase("line_no") )
				{
					ls_line_no = childNode.getFirstChild().getNodeValue().trim();
					//System.out.println("ls_cur_lineno["+ls_cur_lineno+"]ls_line_no["+ls_line_no+"]");
				}

				if ( childNode != null && childNode.getFirstChild() != null &&  
						childNode.getNodeName().equalsIgnoreCase("item_code") )
				{
					ls_brow_item_code = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null &&  
						childNode.getNodeName().equalsIgnoreCase("sord_no") )
				{
					ls_brow_saleorder = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null &&  
						childNode.getNodeName().equalsIgnoreCase("line_no__sord") )
				{
					ls_brow_lineno = childNode.getFirstChild().getNodeValue().trim();
				}
				if ( childNode != null && childNode.getFirstChild() != null &&  
						childNode.getNodeName().equalsIgnoreCase("quantity") )
				{
					quantity = Double.parseDouble( checkDoubleNull(childNode.getFirstChild().getNodeValue().trim()));
				}

			}

			if(! ls_cur_lineno.equalsIgnoreCase(ls_line_no))
			{
				ls_item_code__parent_cur = getValue("item_code__parent", "item", "item_code", ls_brow_item_code, conn);

				if( isnull(ls_item_code__parent_cur) || lenTrim(ls_item_code__parent_cur) == 0 ) 
				{
					boolean isExist=false;
					isExist = isExist("item", "item_code__parent", ls_brow_item_code, conn);
					if(isExist)
					{
						ls_item_code__parent_cur = ls_brow_item_code;
					}
				}//end if	

				sql = " select nature from sorddet " +
						" where sale_order = ?  and line_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_brow_saleorder);
				pstmt.setString(2, ls_brow_lineno);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					ls_nature = checkNull(rs.getString("nature"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//System.out.print("========>::ls_nature[" + ls_nature + "]");

				if( ls_item_code__parent_cur.trim().equalsIgnoreCase(ls_item_code__parent.trim()) )
				{		
					if("V".equalsIgnoreCase(ls_scheme_flag))			
					{
						if("F".equalsIgnoreCase(ls_nature))
						{
							lc_prv_free_value = lc_prv_free_value + (quantity * lc_rate);
						}
						else if("B".equalsIgnoreCase( ls_nature))
						{
							lc_prv_bonus_value = lc_prv_bonus_value +( quantity * lc_rate);
						}
						else if("S".equalsIgnoreCase( ls_nature))
						{
							lc_prv_sample_value = lc_prv_sample_value +( quantity * lc_rate);
						}
						else
						{
							lc_prv_charge_value = lc_prv_charge_value + (quantity * lc_rate );
						}

					}
					else	
					{			
						if("F".equalsIgnoreCase( ls_nature ))
						{
							lc_prv_free_qty = lc_prv_free_qty + quantity;
						}
						else if("B".equalsIgnoreCase( ls_nature ))
						{
							lc_prv_bonus_qty = lc_prv_bonus_qty + quantity;
						}
						else if("S".equalsIgnoreCase( ls_nature ))
						{
							lc_prv_sample_qty = lc_prv_sample_qty + quantity;
						}
						else
						{
							lc_prv_charge_qty = lc_prv_charge_qty + quantity; 
						}
					}//	end if	
				}//	end if
			}//		end if

		}//for loop

		sampleValueMap.put("lc_prv_free_qty", lc_prv_free_qty);
		sampleValueMap.put("lc_prv_bonus_qty", lc_prv_bonus_qty);
		sampleValueMap.put("lc_prv_sample_qty", lc_prv_sample_qty);
		sampleValueMap.put("lc_prv_charge_qty", lc_prv_charge_qty);

		//System.out.println("@@@@@@@ sampleValueMap["+sampleValueMap+"]");	
		return sampleValueMap;
	}

	private String errorType(Connection conn, String errorCode) {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				msgType = checkNull(rs.getString("MSG_TYPE"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msgType;
	}
	
	//Added By PriyankaC to get net quantity of despatch for validation on 15Nov2015. [START]
	 private double GetNumberOfQuantityFordespatch(Document dom2 ,String lotNo,String lotSl,String siteCode,String itemCode,String locCode)  throws Exception,ITMException
	    {
	        NodeList parentNodeList=null;
	        NodeList childList = null;
	        Node parentNode=null;
	        Node childNode = null;
	        String curLotNo = null,operationStr="";
	        String curLotSl = null;
	        String curLocCode = null;
	        String cursiteCode = null ,curItemCode = null ,curQuantity = null;
	        double totalNetQt=0 ;
	        double curNetQuentity = 0;
	        try
	        {
	            E12GenericUtility genericUtility= new  E12GenericUtility();
	            parentNodeList = dom2.getElementsByTagName("Detail2");
	            int childNodeListLength = parentNodeList.getLength();
	            for(int ctr = 0; ctr < childNodeListLength; ctr++)
	            {
	                parentNode = parentNodeList.item(ctr);
	                cursiteCode = genericUtility.getColumnValueFromNode("site_code", parentNode);
	                curLotNo = genericUtility.getColumnValueFromNode("lot_no", parentNode);
	                curLotSl = genericUtility.getColumnValueFromNode("lot_sl", parentNode);
	                curItemCode = genericUtility.getColumnValueFromNode("item_code", parentNode);
	                curQuantity = genericUtility.getColumnValueFromNode("quantity", parentNode);
	                curQuantity = curQuantity == null ? "0" :curQuantity;
	                curNetQuentity = Double.parseDouble(curQuantity);
	                //System.out.println("curNetWeight .........................."+curNetQuentity);
	                curLocCode = genericUtility.getColumnValueFromNode("loc_code", parentNode);
	                cursiteCode    = cursiteCode == null ? " " :cursiteCode;
	                curLotNo        = curLotNo == null ? " " :curLotNo;
	                curLotSl        = curLotSl == null ? " " :curLotSl;
	                curLocCode        = curLocCode == null ? " " :curLocCode;
	                //System.out.println("siteCode "+siteCode) ;
	                if ( cursiteCode.trim().equalsIgnoreCase(siteCode.trim())
	                     && curLotNo.trim().equalsIgnoreCase(lotNo.trim())
	                     && curLotSl.trim().equalsIgnoreCase(lotSl.trim())
	                     && curLocCode.trim().equalsIgnoreCase(locCode.trim())
	                     && curItemCode.trim().equalsIgnoreCase(itemCode.trim()) 
	                    )
	                {
	                    totalNetQt = totalNetQt + curNetQuentity;
	                    //System.out.println("totalNetQt .........................."+totalNetQt);
	                }

	            } // end for
	        }//END TRY
	        catch(Exception e)
	        {
	            //System.out.println("Exception ::"+e);
	            e.printStackTrace();
	            throw new ITMException(e);
	        }
	        		        //System.out.println("totalNetQt .....final...................."+totalNetQt);
	        return totalNetQt;
	    }
		//Added By PriyankaC to get net quantity of despatch for validation on 15Nov2015. [START]
	//Pavan Rane 11jun19 start [to validate Channel Partner customer]
	 private boolean isChannelPartnerCust(String custCode, String siteCode, Connection conn) throws ITMException
	 {
			
			String sql = "";
			String disLink = "";
			String chPartner = "";
			boolean cpFlag = false;
			ResultSet rs = null;
			PreparedStatement pstmt = null;
			try
			{
				sql="select channel_partner, dis_link from site_customer "
						+ " where cust_code = ? and site_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setString(2,siteCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					chPartner=checkNull(rs.getString("channel_partner"));
					disLink=checkNull(rs.getString("dis_link"));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(chPartner.trim().length()==0)
				{
					sql="select channel_partner, dis_link  from customer "
							+ " where cust_code = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						chPartner=checkNull(rs.getString("channel_partner"));
						disLink=checkNull(rs.getString("dis_link"));
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}	
				if("Y".equalsIgnoreCase(chPartner))
				{
					if (("A".equalsIgnoreCase(disLink)|| "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink) ))
					{
						cpFlag = true;
					}
				}
				
			} catch (SQLException se)
			{			
				BaseLogger.log("0", null, null, "SQLException :DispatchIC :IsCustChannelP()::" + se.getMessage());
				se.printStackTrace();
				
			}
			catch (Exception e)
			{
				BaseLogger.log("0", null, null, "Exception :DispatchIC :IsCustChannelP()::" + e.getMessage());
				e.printStackTrace();		
				
			} finally
			{
				try
				{
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
				} catch (Exception e)
				{
				}
			}
			return cpFlag;
		}
	//Pavan Rane 11jun19 end

}