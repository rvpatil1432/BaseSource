/********************************************************
	Title : ShipmentLocIC
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/


package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
// import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
import java.sql.Timestamp;
@Stateless  

public class ShipmentLocIC extends ValidatorEJB implements ShipmentLocICLocal, ShipmentLocICRemote
{
	
	// GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();

	//method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("wfValdata() called for ShipmentLocalIC");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("@@@@@@@@@xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("@@@@@@@@@xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
				System.out.println("@@@@@@@@@xmlString2["+xmlString2+"]");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int cnt = 0;
		int ctr=0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		int currentFormNo =0,recCnt=0;
		String shipmentDateStr="",siteCode="",tranCode="",stanCodeFrom="",stanCodeTo="",frtList="",loadType="",currCode="";
		Timestamp shipmentDate=null;
		String isExistFlag="",exchRateStr="",chargeCode="",stanCodeShip="",stanCodeDlv="",shipmentId="";
		double exchRate=0;
		
		String licenceNo="",distRoute="",saleOrder="";

		try
		{
			System.out.println("@@@@@@@@ wfvaldata called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("shipment_id"))
					{    
						shipmentId = checkNull(genericUtility.getColumnValue("shipment_id", dom));
						//Shipment Date should not be blank.

						String keyFlag = setDescription("key_flag", "transetup", "tran_window", "w_shipment_loc", conn);

						if( "M".equalsIgnoreCase(keyFlag) && "A".equalsIgnoreCase(editFlag) )
						{
							isExistFlag = isExist("shipment", "shipment_id", shipmentId, conn);

							if("TRUE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMINVSHPID";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}


					if(childNodeName.equalsIgnoreCase("shipment_date"))
					{    
						shipmentDateStr = genericUtility.getColumnValue("shipment_date", dom);
						//Shipment Date should not be blank.
						if( shipmentDateStr == null)	
						{
							errCode = "VTSHPDT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//if isnull(ldt_val) then errcode = 'VTSHPDT'

					}
					if(childNodeName.equalsIgnoreCase("site_code"))
					{  
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if( siteCode != null && siteCode.trim().length() > 0 )
						{
							isExistFlag = isExist("site", "site_code", siteCode, conn);

							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VTSHSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "NULLSITECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if(childNodeName.equalsIgnoreCase("tran_code"))
					{ 
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						if( tranCode != null && tranCode.trim().length() > 0 )
						{
							isExistFlag = isExist("transporter", "tran_code", tranCode, conn);

							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMTRANCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(childNodeName.equalsIgnoreCase("stan_code__from"))
					{ 

						stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from", dom));

						if( stanCodeFrom != null && stanCodeFrom.trim().length() > 0 )
						{
							isExistFlag = isExist("station", "stan_code", stanCodeFrom, conn);

							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMSTAN2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(childNodeName.equalsIgnoreCase("stan_code__to"))
					{ 
						stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));

						if( stanCodeTo != null && stanCodeTo.trim().length() > 0 )
						{
							isExistFlag = isExist("station", "stan_code", stanCodeTo, conn);

							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMSTAN2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(childNodeName.equalsIgnoreCase("frt_list"))
					{ 
						frtList = checkNull(genericUtility.getColumnValue("frt_list", dom));

						if( frtList != null && frtList.trim().length() > 0 )
						{
							isExistFlag = isExist("freight_list", "frt_list", frtList, conn);

							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VTFRLEXT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								//ldt_val = dw_edit.GetItemDatetime(1, 'shipment_date')
								shipmentDateStr = checkNull(genericUtility.getColumnValue("shipment_date", dom));

								if( shipmentDateStr != null && shipmentDateStr.length() > 0 ) 
								{
									shipmentDate= Timestamp.valueOf(genericUtility.getValidDateString(shipmentDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								}

								sql = " Select count(1) from freight_list  where  frt_list = ? " +
										"	and    ?  between eff_from and valid_upto  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString( 1, frtList );
								pstmt.setTimestamp(2, shipmentDate );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null; 

								if( cnt == 0 ) 
								{				//Freight List is not valid for the given Shipment Date.
									errCode = "VTFRTVLD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						else
						{
							errCode = "VMFRTLST";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(childNodeName.equalsIgnoreCase("load_type"))
					{ 
						loadType = checkNull(genericUtility.getColumnValue("load_type", dom));

						if( loadType != null && loadType.trim().length() > 0 )
						{
							stanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__from", dom));
							stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
							frtList = checkNull(genericUtility.getColumnValue("frt_list", dom));

							if( loadType != null && stanCodeFrom != null && stanCodeTo != null && frtList != null )
							{
								sql = " select count(1) from freight_rate " +
										" where  frt_list = ?  " +
										" and  load_type = ? " +
										" and  stan_code__from 	= ? " +
										" and  stan_code__to  = ? " ;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString( 1, frtList );
								pstmt.setString( 2, loadType );
								pstmt.setString( 3, stanCodeFrom );
								pstmt.setString( 4, stanCodeTo );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null; 

								if( cnt == 0 )
								{
									errCode = "VTSHPFRT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						else
						{
							errCode = "VMLOADTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(childNodeName.equalsIgnoreCase("curr_code"))
					{ 
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));

						if( currCode != null && currCode.trim().length() > 0 )  
						{
							isExistFlag = isExist("currency", "curr_code", currCode, conn);
							if ("FALSE".equalsIgnoreCase(isExistFlag) )	
							{
								errCode = "VMCURR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	 
						}
						else
						{	//Currency code should not be Empty.

							errCode = "VMCUR2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}							

					if(childNodeName.equalsIgnoreCase("exch_rate"))
					{ 
						exchRateStr = checkNull(genericUtility.getColumnValue("exch_rate", dom));

						exchRateStr = exchRateStr==null?"0":exchRateStr;
						exchRate = Double.parseDouble(exchRateStr);
						if( exchRate <= 0 )
						{	
							errCode = "VTEXCHRATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}			

					if(childNodeName.equalsIgnoreCase("sale_order"))
					{ 
						saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));

						if ( saleOrder != null && saleOrder.trim().length() > 0 )
						{
							isExistFlag = isExist("sorder", "sale_order", saleOrder, conn);
							if ("FALSE".equalsIgnoreCase(isExistFlag) )	
							{
								errCode = "VTSORD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}	
					
					if(childNodeName.equalsIgnoreCase("dist_route"))
					{ 
						distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom));

						if ( distRoute != null && distRoute.trim().length() > 0 )
						{
							isExistFlag = isExist("distroute", "dist_route", distRoute, conn);
							if ("FALSE".equalsIgnoreCase(isExistFlag) )	
							{
								errCode = "VTDISTRT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					if(childNodeName.equalsIgnoreCase("licence_no"))
					{ 
						licenceNo = checkNull(genericUtility.getColumnValue("licence_no", dom));
						if ( licenceNo != null && licenceNo.trim().length() > 0 )
						{
							cnt=0;
							sql = "select count(1) from gencodes where fld_name ='LICENCE_NO' and fld_value = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, licenceNo );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null; 
							
							if (cnt == 0 )	
							{
								errCode = "INVLICENNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					
				} // end for
				break;  // case 1 end

			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("@@@@@@@@@@@@childNodeListLength["+childNodeListLength+"]");
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("charge_code"))
					{
						chargeCode = checkNull(genericUtility.getColumnValue("charge_code", dom));
						if( chargeCode == null || chargeCode.trim().length()== 0 )
						{	
							errCode = "VMCHGCODE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							cnt=0;
							sql = " select count(1) from gencodes where fld_name = 'CHARGE_CODE' and mod_name = 'X' and rtrim(fld_value) = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, chargeCode.trim() );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null; 
							
							if (cnt == 0 )	
							{
								errCode = "VMCHGCDINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}


				} // end for
				break;  // case 1 end

			case 3:
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(   childNodeName.equalsIgnoreCase("stan_code__ship") ) 
					{
						stanCodeShip  = checkNull(genericUtility.getColumnValue("stan_code__ship", dom));
						stanCodeDlv  = checkNull(genericUtility.getColumnValue("stan_code__dlv", dom));
						if( stanCodeShip != null && stanCodeShip.trim().length() > 0 ) 
						{	
							isExistFlag = isExist("station", "stan_code", stanCodeShip, conn);
							if( "FALSE".equalsIgnoreCase(isExistFlag) )	
							{		
								errCode = "VMSTAN2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 
						}
						else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if(  stanCodeDlv != null && stanCodeDlv.trim().length() > 0  )
						{	
							isExistFlag = isExist("station", "stan_code", stanCodeDlv, conn);
							if( "FALSE".equalsIgnoreCase(isExistFlag) )	
							{
								errCode = "VMSTAN2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 
						}
						else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(    childNodeName.equalsIgnoreCase("tran_code")  )
					{
						tranCode  = checkNull(genericUtility.getColumnValue("tran_code", dom));

						if( tranCode != null && tranCode.trim().length() > 0 ) 
						{	
							isExistFlag = isExist("transporter", "tran_code", tranCode, conn);
							if( "FALSE".equalsIgnoreCase(isExistFlag) )	
							{		
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 
						}
						else
						{
							errCode = "VMTRANCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}

				}// end for case 3
				break;  // case 3 end

			case 4:
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("Detail4@@@@@@@@@@@@childNodeListLength["+childNodeListLength+"]");
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if( childNodeName.equalsIgnoreCase("stan_code__ship") )
					{
						stanCodeShip = checkNull(genericUtility.getColumnValue("stan_code__ship", dom));

						if( stanCodeShip != null && stanCodeShip.trim().length() > 0 )
						{	
							isExistFlag = isExist("station", "stan_code", stanCodeShip, conn);
							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMSTAN2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if(  childNodeName.equalsIgnoreCase("stan_code__dlv"))
					{
						stanCodeDlv = checkNull(genericUtility.getColumnValue("stan_code__dlv", dom));

						if( stanCodeDlv != null && stanCodeDlv.trim().length() > 0 )
						{	
							isExistFlag = isExist("station", "stan_code", stanCodeDlv, conn);
							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMSTAN2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
					
					if( childNodeName.equalsIgnoreCase("curr_code") )
					{

						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));

						if( currCode != null && currCode.trim().length() > 0 )
						{	
							isExistFlag = isExist("currency", "curr_code", currCode, conn);
							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMCURR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMCUR2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}

					if( childNodeName.equalsIgnoreCase("curr_code__frt") )
					{
						currCode="";
						currCode = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));

						if( currCode != null && currCode.trim().length() > 0 )
						{	
							isExistFlag = isExist("currency", "curr_code", currCode, conn);
							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMCURR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMCUR2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					
					if( childNodeName.equalsIgnoreCase("curr_code__bc") )
					{
						currCode="";
						currCode = checkNull(genericUtility.getColumnValue("curr_code__bc", dom));

						if( currCode != null && currCode.trim().length() > 0 )
						{	
							isExistFlag = isExist("currency", "curr_code", currCode, conn);
							if("FALSE".equalsIgnoreCase(isExistFlag))
							{
								errCode = "VMCURR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMCUR2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					
					if( childNodeName.equalsIgnoreCase("tran_code") )
					{


						tranCode  = checkNull(genericUtility.getColumnValue("tran_code", dom));

						if( tranCode != null && tranCode.trim().length() > 0 ) 
						{	
							isExistFlag = isExist("transporter", "tran_code", tranCode, conn);
							if( "FALSE".equalsIgnoreCase(isExistFlag) )	
							{		
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 
						}
						else
						{
							errCode = "VMTRANCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
					if( childNodeName.equalsIgnoreCase("ref_id") )
					{

						String refSer  = checkNull(genericUtility.getColumnValue("ref_ser", dom));
						String refId  = checkNull(genericUtility.getColumnValue("ref_id", dom));
						System.out.println("@@@@@@@@ refSer["+refSer+"]refId["+refId+"]");
						if( refSer != null && refSer.trim().length() > 0 &&  refId != null && refId.trim().length() > 0 ) 
						{
							refSer = refSer.trim();
							refId = refId.trim();
							if("S-DSP".equalsIgnoreCase(refSer) )
							{
								System.out.println("@@@@@@@@@ for despatch validation....");
								sql = " select count(1) from despatch where desp_id = ? and confirmed = 'N' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,refId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;
								System.out.println("COUNT["+cnt+"]");
								if( cnt > 0 )
								{
									errCode = "VMREFIDCNF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else if("P-ORD".equalsIgnoreCase(refSer) )
							{
								System.out.println("@@@@@@@@@ for porder validation....");
								sql = " select count(1) from porder where purc_order = ? and confirmed = 'N' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,refId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;
								System.out.println("COUNT["+cnt+"]");
								if( cnt > 0 )
								{
									errCode = "VMREFIDCNF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else if("D-ISS".equalsIgnoreCase(refSer) )
							{
								System.out.println("@@@@@@@@@ for distord_iss validation....");
								sql = " select count(1) from distord_iss where tran_id = ? and confirmed = 'N' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,refId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;
								System.out.println("COUNT["+cnt+"]");
								if( cnt > 0 )
								{
									errCode = "VMREFIDCNF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else if("C-ISS".equalsIgnoreCase(refSer) )
							{
								System.out.println("@@@@@@@@@ for consume_iss validation....");
								sql = " select count(1) from consume_iss where cons_issue = ? and confirmed = 'N' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,refId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;
								System.out.println("COUNT["+cnt+"]");
								if( cnt > 0 )
								{
									errCode = "VMREFIDCNF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
							
						}
						else
						{
							errCode = "VMREFIDNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

				} // end for
				break;  // case 4 end

			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
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
					conn.close();
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation

	private String isExist(String table, String field, String value,Connection conn) throws SQLException
	{
		String sql = "",retStr="";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int cnt=0;

		sql = " SELECT COUNT(1) FROM "+ table + " WHERE " + field + " = ? ";
		pstmt =  conn.prepareStatement(sql);
		pstmt.setString(1,value);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close(); 
		pstmt = null;
		System.out.println("COUNT["+cnt+"]");
		if( cnt > 0)
		{
			retStr = "TRUE";
		}
		if( cnt == 0 )
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist["+value+"]:::["+retStr+"]:::["+cnt+"]");
		return retStr;
	}

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("itemChanged() called for ShipmentLocalIC");
		String valueXmlString = "";
		try
		{   
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
				System.out.println("@@@@@@@@@xmlString[["+xmlString+"]]");
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
				System.out.println("@@@@@@@@@xmlString1[["+xmlString1+"]]");
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
				System.out.println("@@@@@@@@@xmlString2[["+xmlString2+"]]");
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ShipmentLocalIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String childNodeName = null;
		String sql = "",purcOrder="",amdNo="";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0,cnt1 = 0,cnt=0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		// GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility =  new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();

		int currentFormNo =0;
		String columnValue="";

		String siteCode="",siteCodeDescr="",tranCode="",tranName="",currCode="",frtList="",reStr="",stanCodeFrom="",stanCodeTo="";
		String stanCodeDescr="",distance="",frtDescr="",loadType="",frtTerm="",loadTypeDescr="";
		int pos = 0,cnt2=0;
		double stdPickUp=0,stdTransitTime=0,basicFreight=0,minValue=0,grossWeight=0,freightAmt=0,grossDist=0,addChgs=0;
		String transitType="", freightType="", shipmentId="",currencyDescr="",currCodeBase="",loginSiteCode="";
		double exchRate=0,totalFreight=0;
		String chargeCode="" ;
		String gencodesDescr="",mode="",chargeAdd="",sequence="",chargeAdd1="",stanCodeShip="",stationaDescr="";
		String tranCodeDescr="",stanCodeDlv="",stanCodeDlvDescr="",currCodeBc="",currCodeFrt="",currCodeFrtDescr="";
		String currCodeBcDescr="",exchRateDescr="",transporterTranName="",refId="",refSer="",stanCode="",siteCodeDlv="";
		double amount=0,noOfLr=0,extraDlv=0,netWeight=0,nettWeight=0,exchRateFrt=0,tareWeight=0,grossWeightCIss=0 ;
		int i=0,noArt=0;
		Timestamp despDate=null;

		double netWeightCIss=0;
		String stnCode="";
		
		try
		{

			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDate);
			loginSiteCode =  getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("Now the date is["+sysDate+"]::loginSiteCode["+loginSiteCode+"]");

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");


				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("@@@@@@@@ itemchange itm_default called @@@@@@@@");
					valueXmlString.append( "<shipment_date><![CDATA[" ).append(sysDate).append("]]></shipment_date>\r\n" );

					valueXmlString.append("<site_code>").append("<![CDATA["+loginSiteCode+"]]>").append("</site_code>");
					siteCodeDescr = setDescription("descr", "site" , "site_code", loginSiteCode, conn);
					valueXmlString.append("<descr>").append("<![CDATA["+siteCodeDescr+"]]>").append("</descr>");
				
					valueXmlString.append("<lr_date><![CDATA[" ).append(sysDate).append("]]></lr_date>\r\n" );
					
					stnCode = setDescription("stan_code", "site" , "site_code", loginSiteCode, conn);
					valueXmlString.append("<stan_code__from>").append("<![CDATA["+stnCode+"]]>").append("</stan_code__from>");
				
					stanCodeDescr = setDescription("descr", "station" , "stan_code", stnCode, conn);
					valueXmlString.append("<station_a_descr>").append("<![CDATA["+stanCodeDescr+"]]>").append("</station_a_descr>");

					
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					System.out.println("@@@@@@@@ itemchange site_code called @@@@@@@@");

					siteCode = genericUtility.getColumnValue("site_code", dom);
					if ( siteCode != null && siteCode.trim().length() > 0  )
					{
						siteCodeDescr = setDescription("descr", "site" , "site_code", siteCode, conn);
						valueXmlString.append("<descr>").append("<![CDATA["+siteCodeDescr+"]]>").append("</descr>");
					}

				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					System.out.println("@@@@@@@@ itemchange tran_code called @@@@@@@@");

					tranCode = genericUtility.getColumnValue("tran_code", dom);

					sql = " select tran_name,curr_code,frt_list from transporter " +
							" where tran_code = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,tranCode );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranName = rs.getString("tran_name")==null?"":rs.getString("tran_name");
						currCode = rs.getString("curr_code")==null?"":rs.getString("curr_code");
						frtList = rs.getString("frt_list")==null?"":rs.getString("frt_list");
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;

					valueXmlString.append("<transporter_tran_name>").append("<![CDATA["+tranName+"]]>").append("</transporter_tran_name>");
					valueXmlString.append("<curr_code>").append("<![CDATA["+currCode+"]]>").append("</curr_code>");

					setNodeValue(dom, "curr_code", checkNull(currCode));
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

					valueXmlString.append("<frt_list>").append("<![CDATA["+frtList+"]]>").append("</frt_list>");
					
					setNodeValue(dom, "frt_list", checkNull(frtList));
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);

				}

				else if( currentColumn.trim().equalsIgnoreCase("stan_code__from"))
				{
					System.out.println("@@@@@@@@ itemchange stan_code__from called @@@@@@@@");

					stanCodeFrom = genericUtility.getColumnValue("stan_code__from", dom);

					stanCodeDescr = setDescription("descr", "station" , "stan_code", stanCodeFrom, conn);
					valueXmlString.append("<station_a_descr>").append("<![CDATA["+stanCodeDescr+"]]>").append("</station_a_descr>");

					stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);

					if( stanCodeTo != null && stanCodeTo.trim().length() > 0 )
					{	
						sql = " select distance from distance " +
								" where stan_code__from = ? and stan_code__to = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeFrom );
						pstmt.setString(2,stanCodeTo );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							distance = rs.getString("distance")==null?"":rs.getString("distance");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						valueXmlString.append("<distance>").append("<![CDATA["+distance+"]]>").append("</distance>");
						
						loadType = genericUtility.getColumnValue("load_type", dom);
						setNodeValue(dom, "load_type", checkNull(loadType));
						reStr = itemChanged(dom, dom1, dom2, objContext, "load_type", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						
					}


				}
				else if( currentColumn.trim().equalsIgnoreCase("stan_code__to"))
				{

					System.out.println("@@@@@@@@ itemchange stan_code__to called @@@@@@@@");

					stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);

					stanCodeDescr = setDescription("descr", "station" , "stan_code", stanCodeTo, conn);
					valueXmlString.append("<station_b_descr>").append("<![CDATA["+stanCodeDescr+"]]>").append("</station_b_descr>");

					stanCodeFrom = genericUtility.getColumnValue("stan_code__from", dom);

					if( stanCodeFrom != null && stanCodeFrom.trim().length() > 0 )
					{	
						sql = " select distance from distance " +
								" where stan_code__from = ? and stan_code__to = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeFrom );
						pstmt.setString(2,stanCodeTo );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							distance = rs.getString("distance")==null?"":rs.getString("distance");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						valueXmlString.append("<distance>").append("<![CDATA["+distance+"]]>").append("</distance>");
						
						loadType = genericUtility.getColumnValue("load_type", dom);
						setNodeValue(dom, "load_type", checkNull(loadType));
						reStr = itemChanged(dom, dom1, dom2, objContext, "load_type", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
						
					}


				}

				else if( currentColumn.trim().equalsIgnoreCase("frt_list"))
				{
					System.out.println("@@@@@@@@ itemchange frt_list called @@@@@@@@");

					frtList = genericUtility.getColumnValue("frt_list", dom);

					sql = " Select descr,load_type,frt_term " +
							" from freight_list where frt_list = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,frtList );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						frtDescr = rs.getString("descr")==null?"":rs.getString("descr");
						loadType = rs.getString("load_type")==null?"":rs.getString("load_type");
						frtTerm = rs.getString("frt_term")==null?"":rs.getString("frt_term");
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;

					valueXmlString.append("<frt_term>").append("<![CDATA["+frtTerm+"]]>").append("</frt_term>");
					valueXmlString.append("<freight_list_descr>").append("<![CDATA["+frtDescr+"]]>").append("</freight_list_descr>");
					valueXmlString.append("<load_type>").append("<![CDATA["+loadType+"]]>").append("</load_type>");
					
					setNodeValue(dom, "load_type", checkNull(loadType));
					reStr = itemChanged(dom, dom1, dom2, objContext, "load_type", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);


				}
				else if( currentColumn.trim().equalsIgnoreCase("load_type"))
				{
					System.out.println("@@@@@@@@ itemchange load_type called @@@@@@@@");

					loadType = genericUtility.getColumnValue("load_type", dom);

					sql = " select descr from gencodes " +
							"where fld_name = 'LOAD_TYPE' and fld_value = ? and mod_name = 'X'  ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,loadType );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						loadTypeDescr = rs.getString("descr")==null?"":rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;

					valueXmlString.append("<gencodes_descr>").append("<![CDATA["+loadTypeDescr+"]]>").append("</gencodes_descr>");

					frtList = genericUtility.getColumnValue("frt_list", dom);
					stanCodeFrom = genericUtility.getColumnValue("stan_code__from", dom);
					stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);
					loadType = genericUtility.getColumnValue("load_type", dom);

					if( frtList != null && stanCodeFrom != null  && stanCodeTo != null && loadType != null  )
					{

						sql = " select std_pick_up,std_transit_time,transit_type,basic_freight,freight_type,min_value" +
								" from   freight_rate  where  frt_list = ?  and  load_type = ? " +
								" and    stan_code__from = ? and    stan_code__to   = ?  ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,frtList );
						pstmt.setString(2,loadType );
						pstmt.setString(3,stanCodeFrom );
						pstmt.setString(4,stanCodeTo );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stdPickUp = rs.getDouble("std_pick_up");
							stdTransitTime = rs.getDouble("std_transit_time");
							transitType = rs.getString("transit_type")==null?"":rs.getString("transit_type");
							basicFreight = rs.getDouble("basic_freight");
							freightType = rs.getString("freight_type")==null?"":rs.getString("freight_type");
							minValue = rs.getDouble("min_value");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						valueXmlString.append("<transit_type>").append("<![CDATA["+transitType+"]]>").append("</transit_type>");
						valueXmlString.append("<std_pick_up>").append("<![CDATA["+stdPickUp+"]]>").append("</std_pick_up>");
						valueXmlString.append("<std_transit_time>").append("<![CDATA["+stdTransitTime+"]]>").append("</std_transit_time>");
						valueXmlString.append("<freight_type>").append("<![CDATA["+freightType+"]]>").append("</freight_type>");
						valueXmlString.append("<freight_rate>").append("<![CDATA["+basicFreight+"]]>").append("</freight_rate>");
						valueXmlString.append("<min_value>").append("<![CDATA["+minValue+"]]>").append("</min_value>");

						if("F".equalsIgnoreCase(freightType))
						{
							valueXmlString.append("<freight_amt>").append("<![CDATA["+basicFreight+"]]>").append("</freight_amt>");
						}
						else if("G".equalsIgnoreCase(freightType))  //ls_frt_type = 'G' then
						{
							shipmentId = genericUtility.getColumnValue("shipment_id", dom);

							if( shipmentId != null ) //not isnull(ls_shipment) then
							{
								sql = " select nvl(sum(gross_weight),0) from despatch " +
										" where  shipment_id = ?	and    confirmed = 'Y' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,shipmentId );
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									grossWeight = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;


								sql = " select nvl(sum(gross_weight),0) from   distord_iss " +
										" where  shipment_id = ? and  confirmed = 'Y' ";

								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,shipmentId );
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									grossDist = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;

								
								sql = "	 select nvl(sum(gross_weight),0)   from   CONSUME_ISS " +
									  "  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) and confirmed = 'Y' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, shipmentId );
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
								
									
								freightAmt = basicFreight * ( grossWeight + grossDist + grossWeightCIss );

								if( freightAmt > minValue )   //  lc_val > lc_minval then
								{	
									valueXmlString.append("<freight_amt>").append("<![CDATA["+freightAmt+"]]>").append("</freight_amt>");
									basicFreight = freightAmt ;
								}
								else
								{
									valueXmlString.append("<freight_amt>").append("<![CDATA["+minValue+"]]>").append("</freight_amt>");
									basicFreight = minValue ;
								} //end if
							} //end if
						}
						else
						{
							valueXmlString.append("<freight_amt>").append("0.000").append("</freight_amt>");
						} //end if

						String addChgsStr = genericUtility.getColumnValue("add_chgs", dom)==null?"0":genericUtility.getColumnValue("add_chgs", dom);	
						addChgs = Double.parseDouble(addChgsStr);
						freightAmt = basicFreight + addChgs;
						valueXmlString.append("<total_freight>").append("<![CDATA["+freightAmt+"]]>").append("</total_freight>");

					} //end if
					else  // set null if null
					{
						valueXmlString.append("<transit_type>").append("").append("</transit_type>");
						valueXmlString.append("<std_pick_up>").append("").append("</std_pick_up>");
						valueXmlString.append("<std_transit_time>").append("").append("</std_transit_time>");
						valueXmlString.append("<freight_type>").append("").append("</freight_type>");
						valueXmlString.append("<freight_rate>").append("").append("</freight_rate>");
						valueXmlString.append("<min_value>").append("").append("</min_value>");
						valueXmlString.append("<freight_amt>").append("0.000").append("</freight_amt>");
						valueXmlString.append("<total_freight>").append("0.000").append("</total_freight>");
					}
				}


				else if( currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					System.out.println("@@@@@@@@ itemchange curr_code called @@@@@@@@");

					currCode = genericUtility.getColumnValue("curr_code", dom);

					currencyDescr = setDescription("descr", "currency", "curr_code", currCode, conn);
					valueXmlString.append("<currency_descr>").append("<![CDATA["+currencyDescr+"]]>").append("</currency_descr>");

					String shipmentDateStr =  genericUtility.getColumnValue("shipment_date", dom);

					sql = " select a.curr_code from finent a, site b " +
							" where b.fin_entity = a.fin_entity  and b.site_code = ? ";

					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,loginSiteCode );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt2++;
						currCodeBase = rs.getString("curr_code");
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;


					if( cnt2 > 0 )  // get_sqlcode() = 0 then
					{	// get exchange rate 
						if( currCode != null && currCode.trim().length() > 0 ) //(not isnull(ls_code)) and (len(trim(ls_code)) > 0) then
						{	

							FinCommon finCommon = new FinCommon();

							exchRate = finCommon.getDailyExchRateSellBuy(currCode, currCodeBase, loginSiteCode, shipmentDateStr, "S", conn);

							// exchRate = gf_get_daily_exch_rate_sell_buy(ls_code, &ls_currcode_base, &login_site, &ldt_trandt, 'S')

							valueXmlString.append("<exch_rate>").append("<![CDATA["+exchRate+"]]>").append("</exch_rate>");		
						}  //end if
					}//end if
					//end if

				}	

				else if( currentColumn.trim().equalsIgnoreCase("freight_amt"))
				{
					System.out.println("@@@@@@@@ itemchange freight_amt called @@@@@@@@");
					String freightAmtStr = genericUtility.getColumnValue("freight_amt", dom)==null?"0":genericUtility.getColumnValue("freight_amt", dom);
					String addChgsStr = genericUtility.getColumnValue("add_chgs", dom)==null?"0":genericUtility.getColumnValue("add_chgs", dom);
					totalFreight = Double.parseDouble(freightAmtStr) + Double.parseDouble(addChgsStr);
					System.out.println("@@@@@@@@ totalFreight["+totalFreight+"]");
					valueXmlString.append("<total_freight>").append("<![CDATA["+totalFreight+"]]>").append("</total_freight>");
				}	

				else if( currentColumn.trim().equalsIgnoreCase("gross_weight") ||  currentColumn.trim().equalsIgnoreCase("tare_weight"))
				{
					// 	no code in pb		
					System.out.println("@@@@@@@@ itemchange gross_weight / tare_weight called @@@@@@@@");
					grossWeight = Double.parseDouble(genericUtility.getColumnValue("gross_weight", dom)==null?"0":genericUtility.getColumnValue("gross_weight", dom));
					tareWeight = Double.parseDouble(genericUtility.getColumnValue("tare_weight", dom)==null?"0":genericUtility.getColumnValue("tare_weight", dom));
					netWeight = grossWeight - tareWeight;
					System.out.println("@@@@@@@@ grossWeight["+grossWeight+"]- tareWeight["+tareWeight+"]===netWeight["+netWeight+"]");
					valueXmlString.append("<net_weight>").append("<![CDATA["+netWeight+"]]>").append("</net_weight>");
				}

				// case 1 end
				valueXmlString.append("</Detail1>");
				break;
				// case 2 start
			case 2 :
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");


				if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )
				{
					System.out.println("@@@@@@@@ itemchange itm_defaultedit called @@@@@@@@");
					chargeCode = genericUtility.getColumnValue("charge_code", dom);
					valueXmlString.append("<charge_code protect = \"1\">").append("<![CDATA["+chargeCode+"]]>").append("</charge_code>");
				}

				else if ( currentColumn.trim().equalsIgnoreCase("itm_default") )
				{
					System.out.println("@@@@@@@@ itemchange itm_default called @@@@@@@@");
					chargeCode = genericUtility.getColumnValue("charge_code", dom)==null?"":genericUtility.getColumnValue("charge_code", dom);
					valueXmlString.append("<charge_code protect = \"0\">").append("<![CDATA["+chargeCode+"]]>").append("</charge_code>");

					shipmentId = genericUtility.getColumnValue("shipment_id", dom1);
					valueXmlString.append("<shipment_id>").append("<![CDATA["+shipmentId+"]]>").append("</shipment_id>");
				}

				else if(  currentColumn.trim().equalsIgnoreCase("charge_code") )
				{
					System.out.println("@@@@@@@@ itemchange charge_code called @@@@@@@@");

					chargeCode = genericUtility.getColumnValue("charge_code", dom)==null?"":genericUtility.getColumnValue("charge_code", dom).trim();
					shipmentId = genericUtility.getColumnValue("shipment_id", dom1)==null?"":genericUtility.getColumnValue("shipment_id", dom1).trim();
					
					frtList = genericUtility.getColumnValue("frt_list", dom1)==null?"":genericUtility.getColumnValue("frt_list", dom1).trim();
					stanCodeFrom = genericUtility.getColumnValue("stan_code__from", dom1)==null?"":genericUtility.getColumnValue("stan_code__from", dom1).trim();
					stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom1)==null?"":genericUtility.getColumnValue("stan_code__to", dom1).trim();
					loadType = genericUtility.getColumnValue("load_type", dom1)==null?"":genericUtility.getColumnValue("load_type", dom1).trim();
/*
					sql = " select c.descr,b.charges_mode,b.amount,b.charge_code__add,sequence " +
							" from shipment a, freight_rate_det b, gencodes c " +
							" where  b.frt_list = a.frt_list 	" +
							" and 	 b.stan_code__from = a.stan_code__from " +
							" and    b.stan_code__to 	= a.stan_code__to " +
							" and 	b.load_type 		= a.load_type " +
							" and    trim(b.charge_code) = c.fld_value   " +
							" and 	 a.shipment_id 		= ?  " +
							" and    c.fld_name 			= ? 		" +
							" and   c.mod_name 			= 'X'  " +
							" and    c.fld_value 		= ?  ";
*/
					sql = " select c.descr,b.charges_mode,b.amount,b.charge_code__add,sequence " +
							" from " +
						//	" shipment a, " +
							" freight_rate_det b, gencodes c " +
							" where  b.frt_list = ?  	" +
							" and 	 b.stan_code__from = ? " +
							" and    b.stan_code__to 	= ? " +
							" and 	 b.load_type 		=  ? " +
							" and    trim(b.charge_code) = c.fld_value   " +
						//	" and 	 a.shipment_id 		= ?  " +
							" and    c.fld_name 			= ? 		" +
							" and    c.mod_name 			= 'X'  " +
							" and    c.fld_value 		= ?  ";

					
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,frtList  );
					pstmt.setString(2,stanCodeFrom );
					pstmt.setString(3,stanCodeTo );
					pstmt.setString(4,loadType );
					
				//	pstmt.setString(5,shipmentId  );
					pstmt.setString(5,"CHARGE_CODE" );
					pstmt.setString(6,chargeCode );
					
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						gencodesDescr = rs.getString("descr");
						mode = rs.getString("charges_mode");
						amount = rs.getDouble("amount");
						chargeAdd = rs.getString("charge_code__add");
						sequence = rs.getString("sequence");
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;

					///and 	c.fld_name 			= upper(:colname)///commented & added - Vanita - 06/12/06 remove upper


					if("P".equalsIgnoreCase(mode) ) //ls_mode = 'P' then//percentage on basic freight
					{	
						String freightAmtStr = genericUtility.getColumnValue("freight_amt", dom1)==null?"0":genericUtility.getColumnValue("freight_amt", dom1);
						freightAmt = Double.parseDouble(freightAmtStr);

						if( chargeAdd != null )  // not isnull(ls_chgs_add) then
						{	
							String[] tempChargeAdd;
							double tempAmount=0;
							i= 1;
							chargeAdd1 = chargeAdd;
							tempChargeAdd = chargeAdd1.split(",");
							for( i = 0; i < tempChargeAdd.length ; i++ )
							{
								sql = " select amount " +
										" from 	 shipment_det where  shipment_id =  ?  " +
										" and    charge_code =  ?  ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,shipmentId  );
								pstmt.setString(2,tempChargeAdd[i] );
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									tempAmount = rs.getDouble("amount");
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt = null;		

								freightAmt = freightAmt + tempAmount;
							} //end if

							//	lc_val = (lc_val * lc_amt)/100
							freightAmt = ( freightAmt * amount )/100;

							valueXmlString.append("<amount>").append("<![CDATA["+freightAmt +"]]>").append("</amount>");
						}
					}
					else if ("F".equalsIgnoreCase(mode) )   ////fixed price
					{
						valueXmlString.append("<amount>").append("<![CDATA["+amount+"]]>").append("</amount>");
					}
					else if ("D".equalsIgnoreCase(mode) )  //delivery
					{
						String extraDlvStr = genericUtility.getColumnValue("extra_dlv", dom1)==null?"0":genericUtility.getColumnValue("extra_dlv", dom1);
						extraDlv = Double.parseDouble(extraDlvStr) * amount;
						valueXmlString.append("<amount>").append("<![CDATA["+extraDlv+"]]>").append("</amount>");
					}
					else if ("W".equalsIgnoreCase(mode) )   //weight
					{	
						sql = " select sum(nett_weight) " +
								" from 	despatch  where shipment_id = ? 	and 	confirmed = 'Y' ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,shipmentId  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							nettWeight = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						sql = " select sum(net_weight) " +
								" from 	distord_iss  where shipment_id = ?  and 	confirmed = 'Y'  ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,shipmentId  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							netWeight = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						sql = "	 select nvl(sum(gross_weight),0)   from   CONSUME_ISS " +
								"  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) and confirmed = 'Y' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, shipmentId);
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
						
						
						
						nettWeight = (nettWeight + netWeight + grossWeightCIss ) * amount;
						valueXmlString.append("<amount>").append("<![CDATA["+nettWeight+"]]>").append("</amount>");
					}
					else if ("V".equalsIgnoreCase(mode) )  //volume
					{
						valueXmlString.append("<amount>").append("0.000").append("</amount>");
					}
					else if ("L".equalsIgnoreCase(mode) )   //lr numbers
					{
						String noOfLrStr = genericUtility.getColumnValue("no_of_lr", dom1)==null?"0":genericUtility.getColumnValue("no_of_lr", dom1);
						noOfLr = Double.parseDouble(noOfLrStr) * amount;
						valueXmlString.append("<amount>").append("<![CDATA["+noOfLr+"]]>").append("</amount>");
					}//end if

					valueXmlString.append("<gencodes_descr>").append("<![CDATA["+gencodesDescr+"]]>").append("</gencodes_descr>");
					valueXmlString.append("<charges_mode>").append("<![CDATA["+mode+"]]>").append("</charges_mode>");
					valueXmlString.append("<sequence>").append("<![CDATA["+sequence+"]]>").append("</sequence>");
					valueXmlString.append("<charge_code__add>").append("<![CDATA["+chargeAdd+"]]>").append("</charge_code__add>");
					valueXmlString.append("<charge_rate>").append("<![CDATA["+amount+"]]>").append("</charge_rate>");

				}

				valueXmlString.append("</Detail2>");
				break;

			case 3 :
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail3>");
				childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					shipmentId = genericUtility.getColumnValue("shipment_id", dom1);
					valueXmlString.append("<shipment_id protect = \"1\">").append("<![CDATA["+shipmentId+"]]>").append("</shipment_id>");

				}
				else if( currentColumn.trim().equalsIgnoreCase( "stan_code__ship" ) )
				{
					stanCodeShip = genericUtility.getColumnValue("stan_code__ship", dom);
					System.out.println("@@@@@2 stanCodeShip ["+ stanCodeShip + "]");
					stationaDescr = setDescription("descr", "station", "stan_code", stanCodeShip, conn);
					//valueXmlString.append("<stationa_descr protect = \"1\">").append("<![CDATA["+stationaDescr+"]]>").append("</stationa_descr>");
					valueXmlString.append("<descr protect = \"1\">").append("<![CDATA["+stationaDescr+"]]>").append("</descr>");
				}                         //stationa_descr


				else if( currentColumn.trim().equalsIgnoreCase( "stan_code__dlv" ) )
				{
					stanCodeDlv = genericUtility.getColumnValue("stan_code__dlv", dom);
					System.out.println("@@@@@2 stanCodeDlv ["+ stanCodeDlv + "]");
					stationaDescr = setDescription("descr", "station", "stan_code", stanCodeDlv, conn);
					//valueXmlString.append("<stationb_descr protect = \"1\">").append("<![CDATA["+stationaDescr+"]]>").append("</stationb_descr>");
					valueXmlString.append("<descr_1 protect = \"1\">").append("<![CDATA["+stationaDescr+"]]>").append("</descr_1>");

				}
				else if( currentColumn.trim().equalsIgnoreCase( "tran_code" ) )
				{
					tranCode = genericUtility.getColumnValue("tran_code", dom);
					System.out.println("@@@@@2 tranCode ["+ tranCode + "]");
					tranCodeDescr = setDescription("TRAN_NAME", "TRANSPORTER", "tran_code", tranCode, conn);
					//valueXmlString.append("<transporter_tran_name protect = \"1\">").append("<![CDATA["+tranCodeDescr+"]]>").append("</transporter_tran_name>");
					valueXmlString.append("<tran_name protect = \"1\">").append("<![CDATA["+tranCodeDescr+"]]>").append("</tran_name>");
				}

				valueXmlString.append("</Detail3>");
				break;



			case 4 :
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail4>");
				childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("@@@@@@@@@@@case4[" + currentColumn + "] ==> '" + columnValue + "'");

				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("@@@@@@@@@@@case4- itm_default");

					shipmentId = genericUtility.getColumnValue("shipment_id", dom1);
					valueXmlString.append("<shipment_id protect = \"1\">").append("<![CDATA["+shipmentId+"]]>").append("</shipment_id>");
					

					// 	no code in pb		
					System.out.println("@@@@@@@@ setting gross_weight / tare_weight called @@@@@@@@");
					grossWeight = Double.parseDouble(genericUtility.getColumnValue("gross_weight", dom)==null?"0":genericUtility.getColumnValue("gross_weight", dom));
					tareWeight = Double.parseDouble(genericUtility.getColumnValue("tare_weight", dom)==null?"0":genericUtility.getColumnValue("tare_weight", dom));
					netWeight = grossWeight - tareWeight;
					System.out.println("@@@@@@@@ grossWeight["+grossWeight+"]- tareWeight["+tareWeight+"]===netWeight["+netWeight+"]");
					valueXmlString.append("<net_weight>").append("<![CDATA["+netWeight+"]]>").append("</net_weight>");
				
					//valueXmlString.append("<descr protect = \"1\">").append("ABCD").append("</descr>");
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )
				{
					System.out.println("@@@@@@@@@@@case4- itm_defaultedit");

					shipmentId = genericUtility.getColumnValue("shipment_id", dom1);
					valueXmlString.append("<shipment_id protect = \"1\">").append("<![CDATA["+shipmentId+"]]>").append("</shipment_id>");

					// 	no code in pb		
					System.out.println("@@@@@@@@ setting gross_weight / tare_weight called @@@@@@@@");
					grossWeight = Double.parseDouble(genericUtility.getColumnValue("gross_weight", dom)==null?"0":genericUtility.getColumnValue("gross_weight", dom));
					tareWeight = Double.parseDouble(genericUtility.getColumnValue("tare_weight", dom)==null?"0":genericUtility.getColumnValue("tare_weight", dom));
					netWeight = grossWeight - tareWeight;
					System.out.println("@@@@@@@@ grossWeight["+grossWeight+"]- tareWeight["+tareWeight+"]===netWeight["+netWeight+"]");
					valueXmlString.append("<net_weight>").append("<![CDATA["+netWeight+"]]>").append("</net_weight>");
				
					//valueXmlString.append("<descr protect = \"1\">").append("ABCD").append("</descr>");
				/*	currCode = genericUtility.getColumnValue("curr_code", dom);
					System.out.println("@@@@@2 currCode ["+ currCode + "]");
					currencyDescr = setDescription("descr", "currency", "curr_code", currCode, conn);
					valueXmlString.append("<currency_descr protect = \"1\">").append("<![CDATA["+currencyDescr+"]]>").append("</currency_descr>");
				*/	
				}
				
				else if( currentColumn.trim().equalsIgnoreCase( "curr_code" ) )
				{
					currCode = genericUtility.getColumnValue("curr_code", dom);
					System.out.println("@@@@@2 currCode ["+ currCode + "]");
					currencyDescr = setDescription("descr", "currency", "curr_code", currCode, conn);
					valueXmlString.append("<currency_a_descr protect = \"1\">").append("<![CDATA["+currencyDescr+"]]>").append("</currency_a_descr>");
					//valueXmlString.append("<descr protect = \"1\">").append("<![CDATA["+currencyDescr+"]]>").append("</descr>");
					//valueXmlString.append("<currency_descr protect = \"1\">").append("ABC").append("</currency_descr>");
					//valueXmlString.append("<descr protect = \"1\">").append("ABCD").append("</descr>");
				}

				else if( currentColumn.trim().equalsIgnoreCase( "stan_code__ship" ) )
				{	
					stanCodeShip = genericUtility.getColumnValue("stan_code__ship", dom);
					System.out.println("@@@@@2 stanCodeShip ["+ stanCodeShip + "]");
					stanCodeDescr = setDescription("descr", "station", "stan_code", stanCodeShip, conn);
					valueXmlString.append("<station_a_descr protect = \"1\">").append("<![CDATA["+stanCodeDescr+"]]>").append("</station_a_descr>");
				}
				else if( currentColumn.trim().equalsIgnoreCase( "stan_code__dlv" ) )
				{
					stanCodeDlv = genericUtility.getColumnValue("stan_code__dlv", dom);
					System.out.println("@@@@@2 stanCodeDlv ["+ stanCodeDlv + "]");
					stanCodeDlvDescr = setDescription("descr", "station", "stan_code", stanCodeDlv, conn);
					valueXmlString.append("<station_b_descr protect = \"1\">").append("<![CDATA["+stanCodeDlvDescr+"]]>").append("</station_b_descr>");
				}
				else if( currentColumn.trim().equalsIgnoreCase( "curr_code__frt" ) )
				{
					currCodeFrtDescr="";
					currCodeFrt = genericUtility.getColumnValue("curr_code__frt", dom);
					System.out.println("@@@@@2 currCodeFrt ["+ currCodeFrt + "]");
					currCodeFrtDescr = setDescription("descr", "currency", "curr_code", currCodeFrt, conn);
					valueXmlString.append("<currency_b_descr protect = \"1\">").append("<![CDATA["+currCodeFrtDescr+"]]>").append("</currency_b_descr>");

				}
				else if( currentColumn.trim().equalsIgnoreCase( "curr_code__bc" ) )
				{
					currCodeBcDescr =  ""; 
					currCodeBc = genericUtility.getColumnValue("curr_code__bc", dom);
					System.out.println("@@@@@2 currCodeBc ["+ currCodeBc + "]");
					currCodeBcDescr = setDescription("descr", "currency", "curr_code", currCodeBc, conn);
					valueXmlString.append("<currency_c_descr protect = \"1\">").append("<![CDATA["+currCodeBcDescr+"]]>").append("</currency_c_descr>");

					exchRateDescr = setDescription("std_exrt", "currency", "curr_code", currCodeBc, conn);
					valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA["+exchRateDescr+"]]>").append("</exch_rate>");
				}

				else if( currentColumn.trim().equalsIgnoreCase("tran_code") )
				{
					transporterTranName= "";
					tranCode = genericUtility.getColumnValue("tran_code", dom);
					System.out.println("@@@@@2 tranCode ["+ tranCode + "]");
					transporterTranName = setDescription("TRAN_NAME", "TRANSPORTER", "tran_code", tranCode, conn);
					valueXmlString.append("<transporter_tran_name protect = \"1\">").append("<![CDATA["+transporterTranName+"]]>").append("</transporter_tran_name>");
				}	

				else if( currentColumn.trim().equalsIgnoreCase("gross_weight") ||  currentColumn.trim().equalsIgnoreCase("tare_weight"))
				{
					// 	no code in pb		
					System.out.println("@@@@@@@@ itemchange gross_weight / tare_weight called @@@@@@@@");
					grossWeight = Double.parseDouble(genericUtility.getColumnValue("gross_weight", dom)==null?"0":genericUtility.getColumnValue("gross_weight", dom));
					tareWeight = Double.parseDouble(genericUtility.getColumnValue("tare_weight", dom)==null?"0":genericUtility.getColumnValue("tare_weight", dom));
					netWeight = grossWeight - tareWeight;
					System.out.println("@@@@@@@@ grossWeight["+grossWeight+"]- tareWeight["+tareWeight+"]===netWeight["+netWeight+"]");
					valueXmlString.append("<net_weight>").append("<![CDATA["+netWeight+"]]>").append("</net_weight>");
				}
				
				
				else if ( currentColumn.trim().equalsIgnoreCase("ref_id")  )
				{
					refId = genericUtility.getColumnValue("ref_id", dom);
					refSer = genericUtility.getColumnValue("ref_ser", dom);
					System.out.println("@@@@@2 refId ["+ refId + "]::refSer["+refSer+"]");

					if("S-DSP".equalsIgnoreCase(refSer)) //ls_refser = 'S-DSP' Then
					{	

						sql = " select tran_code, curr_code, no_art, curr_code__frt , desp_date,site_code, exch_rate__frt," +
								" stan_code,gross_weight, nett_weight	, tare_weight " +
								"	From   Despatch Where  desp_id = ?  ";

						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,refId  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranCode = rs.getString("tran_code");
							currCode = rs.getString("curr_code");
							noArt = rs.getInt("no_art");
							currCodeFrt  = rs.getString("curr_code__frt");
							despDate = rs.getTimestamp("desp_date");
							siteCode = rs.getString("site_code");
							exchRateFrt = rs.getDouble("exch_rate__frt");
							stanCode = rs.getString("stan_code");
							grossWeight = rs.getDouble("gross_weight");
							nettWeight	 = rs.getDouble("nett_weight");
							tareWeight = rs.getDouble("tare_weight");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						sql = " select A.stan_code , B.curr_code " +
								" From   Site A, Finent B 	Where  A.fin_entity = B.fin_entity " +
								" And	 A.site_code  =  ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

					}
					else if("P-ORD".equalsIgnoreCase(refSer))  // ls_refser = 'P-ORD' Then
					{
						sql = " select tran_code 		, curr_code 		, 0 				, curr_code__frt , " +
								"	 ord_date		, site_code__ord  , exch_rate		, site_code__dlv " +
								" 	From   Porder 	Where  purc_order = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,refId  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranCode = rs.getString("tran_code");
							currCode = rs.getString("curr_code");
							noArt = rs.getInt("no_art");
							currCodeFrt  = rs.getString("curr_code__frt");
							despDate = rs.getTimestamp("ord_date");
							siteCode = rs.getString("site_code__ord");
							exchRateFrt = rs.getDouble("exch_rate");
							siteCodeDlv = rs.getString("site_code__dlv");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						sql = " Select A.stan_code 		, B.curr_code " +
								"	From   Site A, Finent B  Where  A.fin_entity = B.fin_entity " +
								" And	 A.site_code  =  ?  ";

						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;


						sql = " select A.stan_code " +
								" From   Site A, Finent B  Where  A.fin_entity = B.fin_entity" +
								"	And	 A.site_code  =  ? ";

						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeDlv  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							//currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;


					}
					else if("D-ISS".equalsIgnoreCase(refSer))  // ls_refser = 'D-ISS' Then
					{	
						sql = " Select tran_code 		, curr_code 		, no_art			, curr_code__frt ," +
								" tran_date		, site_code			, exch_rate		, site_code__dlv , " +
								"  gross_weight	, net_weight		, tare_weight " +
								" From   Distord_iss  	Where  tran_id =  ?  ";

						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,refId  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranCode = rs.getString("tran_code");
							currCode = rs.getString("curr_code");
							noArt = rs.getInt("no_art");
							currCodeFrt  = rs.getString("curr_code__frt");
							despDate = rs.getTimestamp("tran_date");
							siteCode = rs.getString("site_code");
							exchRateFrt = rs.getDouble("exch_rate");
							siteCodeDlv = rs.getString("site_code__dlv");
							grossWeight = rs.getDouble("gross_weight");
							nettWeight	 = rs.getDouble("nett_weight");
							tareWeight = rs.getDouble("tare_weight");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						sql = " Select A.stan_code 		, B.curr_code " +
								"	From   Site A, Finent B Where  A.fin_entity = B.fin_entity " +
								"	And	 A.site_code  = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;


						sql = " Select A.stan_code " +
								" From   Site A, Finent B 	Where  A.fin_entity = B.fin_entity" +
								" And	 A.site_code  = ?  ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeDlv  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							//	currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

					} 
					else if ("C-ISS".equalsIgnoreCase(refSer))   // for c-iss
					{
						
						sql = " Select tran_code, curr_code, no_art, " +
							//  " curr_code__frt, " +
							  " issue_date, site_code__ord," +
							  " exch_rate, site_code__req, gross_weight" +
							//	", net_weight , tare_weight " +
								" From  CONSUME_ISS Where  cons_issue =  ?  ";

						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,refId  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranCode = rs.getString("tran_code");
							currCode = rs.getString("curr_code");
							noArt = rs.getInt("no_art");
						//	currCodeFrt  = rs.getString("curr_code__frt");
							despDate = rs.getTimestamp("issue_date");
							siteCode = rs.getString("site_code__ord");
							exchRateFrt = rs.getDouble("exch_rate");
							siteCodeDlv = rs.getString("site_code__req");
							grossWeight = rs.getDouble("gross_weight");
						//	nettWeight	 = rs.getDouble("nett_weight");
						//	tareWeight = rs.getDouble("tare_weight");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;

						sql = " Select A.stan_code 		, B.curr_code " +
								"	From   Site A, Finent B Where  A.fin_entity = B.fin_entity " +
								"	And	 A.site_code  = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;


						sql = " Select A.stan_code " +
								" From   Site A, Finent B 	Where  A.fin_entity = B.fin_entity" +
								" And	 A.site_code  = ?  ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeDlv  );
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCode = rs.getString("stan_code");
							//	currCode = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						
					}
					//End if

					// below code commented in PB code
					//  	This.Setitem(1,'tran_code',ls_tran_code)
					//		This.SetColumn('tran_code')
					//		This.triggerevent(itemchanged!)
					//		This.Setitem(1,'curr_code',ls_curr_code)
					//		This.SetColumn('curr_code')
					//		This.triggerevent(itemchanged!)
					//		This.Setitem(1,'no_art',ll_noart)
					//		This.Setitem(1,'curr_code__frt',ls_curr_frt)
					//		This.SetColumn('curr_code__frt')
					//		This.triggerevent(itemchanged!)
					//		This.Setitem(1,'exch_rate',ldexchrate)
					//		This.Setitem(1,'start_dt',ldt_despdt)
					//		This.Setitem(1,'start_dt_farsi',ls_date)
					//		This.SetColumn('start_dt')
					//		This.triggerevent(itemchanged!)
					//		This.Setitem(1,'stan_code__dlv',ls_stan_code_dlv)
					//		This.SetColumn('stan_code__dlv')
					//		This.triggerevent(itemchanged!)
					//		This.Setitem(1,'stan_code__ship',ls_stan_code_fr)
					//		This.SetColumn('stan_code__ship')
					//		This.triggerevent(itemchanged!)
					//		This.Setitem(1,'curr_code__bc',ls_curr_bc)
					//		This.SetColumn('curr_code__bc')
					//		This.triggerevent(itemchanged!)
					//		This.SetColumn("ref_id")
					
				}

				valueXmlString.append("</Detail4>");
				break;

			}	// case 3 end
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
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
					conn.close();
				}
				conn = null;	
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}

	private String setDescription(String descr, String table,String field, String value,Connection conn) throws SQLException 
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String sql="",descr1="";
		System.out.println("@@@@@@@@table["+table+"]:::field["+field+"]::value["+value+"]");
		sql = "select "+ descr +" from "+ table +" where " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			descr1 =  checkNull( rs.getString(1));									
		}
		rs.close();
		rs = null;
		pstmt.close(); 
		pstmt = null;
		System.out.print("========>::descr1["+descr1+"]");
		return descr1;
	}

	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}

	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = checkNull( rs.getString("MSG_TYPE"));
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}

	private static void setNodeValue(Document dom, String nodeName, String nodeVal) throws Exception
	{
		Node tempNode = dom.getElementsByTagName(nodeName).item(0);

		if (tempNode != null)
		{
			if (tempNode.getFirstChild() == null)
			{
				CDATASection cDataSection = dom.createCDATASection(nodeVal);
				tempNode.appendChild(cDataSection);
			} else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}



}	
