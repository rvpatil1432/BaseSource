/********************************************************
	Title : InvoiceAmendmentIC
	Date  : 01/06/2012
	Developer: Mahesh Patidar
 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@javax.ejb.Stateless
public class InvoiceAmendmentIC extends ValidatorEJB implements InvoiceAmendmentICLocal,InvoiceAmendmentICRemote
{

	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println("WFVALDATA 111111");
		try
		{
			System.out.println("xmlString [" + xmlString + "]");
			System.out.println("xmlString1 [" + xmlString1 + "]");
			System.out.println("xmlString2 [" + xmlString2 + "]");
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams );
			System.out.println ( "ErrString: " + errString);
		}
		catch(Exception e)
		{
			System.out.println ( "Exception: InvoiceAmendmentIC: wfValData(String xmlString): " + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println ( "Returning from InvoiceAmendmentIC wfValData" );
		return ( errString ); 
	}
	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext,String editFlag, String xtraParams ) throws RemoteException, ITMException
	{
		String errString = "";
		String siteCode1 = "";
		String siteCode = "";
		String invoiceId = "",despatchId = "",sorder = "",customerStatecode = "",siteCodeShip = "" ;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		FinCommon finCommon = new FinCommon();
      //  GenericUtility genericUtility = GenericUtility.getInstance();
		
		
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String errorType = "", errCode = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		NodeList parentList = null;
		NodeList childList = null;
		int noOfChilds ;
		String childNodeName = "";
		Node childNode =null;
		String userId = "";

		int cnt = 0;
		String columnValue = "";
		String columnValue1 = "";
		String confirmed = "",tranDateStr="";
		String sql = "";		
		Timestamp date1 = null;
		Timestamp date2 = null,TranDate=null;
		int currentFormNo = 0;		
		ConnDriver connDriver = null;

		try
		{
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );

			
			if ( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			switch(currentFormNo)
			{
				case 1 :
				{
					parentList = dom.getElementsByTagName( "Detail" + currentFormNo );
					childList = parentList.item( 0 ).getChildNodes();
					noOfChilds = childList.getLength();
					for (int ctr = 0; ctr < noOfChilds; ctr++)
					{	
						childNode = childList.item( ctr );
						childNodeName = childNode.getNodeName();
						if ( childNode != null && childNode.getFirstChild() != null )
						{
							columnValue = childNode.getFirstChild().getNodeValue();
						}
						System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
						
						if ( childNodeName.equalsIgnoreCase("invoice_id") )
						{
							columnValue = genericUtility.getColumnValue("invoice_id", dom);
							columnValue1 = genericUtility.getColumnValue("status", dom);
							System.out.println("iNSIDE INVOICE ID["+columnValue+"]");
							if(columnValue != null && columnValue.trim().length() > 0)
							{
								sql = "select confirmed from invoice where invoice_id = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									confirmed = checkNull(rs.getString(1));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if(! confirmed.trim().equals("Y"))
								{
									errList.add( "VMINVCD1" );
									errFields.add( childNodeName.toLowerCase() );
								}
								else
								{
									sql = "select count(*) from invoice_amendment where invoice_id = ? and status = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, columnValue);
									pstmt.setString(2, columnValue1);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt(1);
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										confirmed = "";
										sql = "select (case when a.confirmed is null then 'N' else a.confirmed end) " +
												" from receipt a, rcpdet b where a.tran_id =  b.tran_id and b.ref_ser = 'S-INV' and b.ref_no = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, columnValue);
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											confirmed = rs.getString(1);
										}
										if(confirmed.trim().equals("Y"))
										{
											errList.add( "VTRCPCON" );
											errFields.add( childNodeName.toLowerCase() );
										}
										if(errList == null || errList.size() == 0)
										{
											if(cnt == 1 && (! editFlag.equals("E")))
											{
												errList.add( "VTINVCD01" );
												errFields.add( childNodeName.toLowerCase() );
											}
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								//if(errList == null || errList.size() == 0)
								//{
									siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
									sql = "select site_code from invoice where invoice_id =? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, columnValue);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										siteCode1 = checkNull(rs.getString(1));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									if(! siteCode.trim().equals(siteCode1.trim()))
									{
										errList.add( "VTDIFFST" );
										errFields.add( childNodeName.toLowerCase() );
									}
								//}
							}
							else
							{
								errList.add( "VMINVIDNL" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("tran_code") )
						{
							columnValue = genericUtility.getColumnValue("tran_code", dom);
							if(columnValue != null && columnValue.trim().length() >0)
							{
								columnValue1 = genericUtility.getColumnValue("status", dom);
								if(columnValue1.equals("S"))
								{
									sql = " Select Count(*) from transporter where tran_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, columnValue);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt(1);
										if(cnt == 0)
										{
											errList.add( "VMTRAN1" );
											errFields.add( childNodeName.toLowerCase() );		
										}
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							/*else
							{
								errList.add( "VMTRANCD1" );
								errFields.add( childNodeName.toLowerCase() );
							}*/
						}
						
						else if ( childNodeName.equalsIgnoreCase("trans_mode") )
						{
							columnValue = genericUtility.getColumnValue("trans_mode", dom);
							if(columnValue == null || columnValue.trim().length() == 0)
							{
								errList.add( "VTITMOD" );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sql = " select count(*) from gencodes where fld_value = ? and fld_name = 'TRANS_MODE' and mod_name = 'W_INVOICE_AMENDMENT' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql = " select count(*) from gencodes where fld_value = ? and fld_name = 'TRANS_MODE' and mod_name = 'X' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, columnValue);
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											cnt = rs.getInt(1);
											if(cnt == 0)
											{
												errList.add( "VTITMOD" );
												errFields.add( childNodeName.toLowerCase() );
											}
										}
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("eff_date") )
						{
							columnValue1 = genericUtility.getColumnValue("status", dom);
							//if(columnValue1.equals("B")) // 31/12/13 manoharan - commented this condition as per instruction from KB (UAT issue tracker)
							//{
							
								if(genericUtility.getColumnValue("eff_date", dom) != null)
								{
									date1 = Timestamp.valueOf(genericUtility.getValidDateString(  genericUtility.getColumnValue("eff_date", dom) , genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
								}
								columnValue = genericUtility.getColumnValue("invoice_id", dom);
							
								sql = "select eff_date from invoice where invoice_id = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									date2 = rs.getTimestamp(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(date1 == null || date2 == null || date2.before(date1))
								{
									errList.add( "VTEFF02" );
									errFields.add( childNodeName.toLowerCase() );	
								}
							//}
						}
						
						else if ( childNodeName.equalsIgnoreCase("rd_permit_no") )
						{
							if(genericUtility.getColumnValue("eff_date", dom) != null)
							{
								date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("eff_date", dom) , genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
							}
							columnValue = genericUtility.getColumnValue("rd_permit_no", dom);
							
							invoiceId = genericUtility.getColumnValue("invoice_id", dom);

							sql = "select desp_id  from invoice where invoice_id = ?   ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, invoiceId);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								despatchId = rs.getString("desp_id");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sql = "select sord_no from despatch where desp_id = ?     ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, despatchId);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								sorder = rs.getString("sord_no");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


							sql = "select  sorder.site_code__ship ,customer.state_code  from sorder ,  customer where   sale_order =  ?    and sorder.cust_code__dlv = customer.cust_code  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, sorder);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								siteCodeShip= checkNull(rs.getString(1));
								customerStatecode = checkNull(rs.getString(2));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("399::"+siteCodeShip+"  "+customerStatecode );
							if(columnValue == null || columnValue.trim().length() == 0 )
							{
								sql = " select count(*) from state where state_code = ? and rd_permit_reqd = 'Y' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, customerStatecode);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)////added by kunal on 18/jan/13
								{
									errList.add( "VTRDPT" );
									errFields.add( childNodeName.toLowerCase() );		
								}
							}
							else if ( date1 != null)
							{

								sql = " select  count(*) from roadpermit where rd_permit_no = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt == 0)
								{
									errList.add( "VTRDPT1" );
									errFields.add( childNodeName.toLowerCase() );		
								}
								else
								{
									/*
									invoiceId = genericUtility.getColumnValue("invoice_id", dom);

									sql = "select desp_id  from invoice where invoice_id = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, invoiceId);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										despatchId = rs.getString("desp_id");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									sql = "select sord_no from despatch where desp_id = ?     ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, despatchId);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										sorder = rs.getString("sord_no");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;


									sql = "select  sorder.site_code__ship ,customer.state_code  from sorder ,  customer where   sale_order =  ?    and sorder.cust_code__dlv = customer.cust_code  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, sorder);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										siteCodeShip= checkNull(rs.getString(1));
										customerStatecode = checkNull(rs.getString(2));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									*/

									sql = "select expiry_date, status ,state_code__fr,state_code__to from roadpermit where rd_permit_no = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, columnValue);
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										date2 = rs.getTimestamp(1);
										columnValue1 = checkNull(rs.getString(2));

									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									System.out.println("502::"+date1+"  "+date2+"        "+date1.after(date2));
									if(date1.after(date2) && ((int)( (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24))) != 0 )
									{
										errList.add( "VTRDPT5" );
										errFields.add( childNodeName.toLowerCase() );	
									}
									else
									{
										if(columnValue1.equals("C"))
										{
											errList.add( "VTRDPT6" );
											errFields.add( childNodeName.toLowerCase() );
										}
										else
										{
											//added by kunal on 17/jan/13 
											sql = " select  count(*) from roadpermit where rd_permit_no = ? and site_code__fr  = ?  ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, columnValue);
											pstmt.setString(2, siteCodeShip);
											rs = pstmt.executeQuery();
											if( rs.next() )
											{
												cnt = rs.getInt(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											if(cnt == 0)
											{
												errList.add( "VTRDPT3" );
												errFields.add( childNodeName.toLowerCase() );		
											}
											sql = " select  count(*) from roadpermit where rd_permit_no = ? and state_code__to =  ?   ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, columnValue);
											pstmt.setString(2, customerStatecode);
											rs = pstmt.executeQuery();
											if( rs.next() )
											{
												cnt = rs.getInt(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											if(cnt == 0)
											{
												errList.add( "VTRDPT4" );
												errFields.add( childNodeName.toLowerCase() );		
											}
											//added by kunal on 17/jan/13 end
										}
									}
								}
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("curr_code__frt") )
						{
							columnValue = genericUtility.getColumnValue("curr_code__frt", dom);
							if(columnValue != null && columnValue.trim().length() > 0)
							{
								sql = "Select Count(*) from currency where curr_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errList.add( "VTCURRCD1" );
										errFields.add( childNodeName.toLowerCase() );		
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("curr_code__ins") )
						{
							columnValue = genericUtility.getColumnValue("curr_code__ins", dom);
							if(columnValue != null && columnValue.trim().length() > 0)
							{
								sql = "Select Count(*) from currency where curr_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errList.add( "VTCURRCD1" );
										errFields.add( childNodeName.toLowerCase() );		
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("lr_date") )
						{
							columnValue1 = genericUtility.getColumnValue("status", dom);
							if(!"D".equals(columnValue1) )
							{
								if(genericUtility.getColumnValue("lr_date", dom) != null)
								{
									date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("lr_date", dom) , genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
								}
								columnValue = genericUtility.getColumnValue("invoice_id", dom);
								sql = "select var_value from   disparm	where  prd_code = '999999'" +
										" and var_name = 'MAX_DELAY_IN_SHIP' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
								}
								else
								{
									cnt = 7;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(date1 != null)
								{
									
									if(errList == null || errList.size() == 0)
									{
										sql = "select tran_date	from   invoice where  invoice_id = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, columnValue);
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											date2 = rs.getTimestamp(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										int diffDays = Math.abs(date1.compareTo(date2));
										if(date1 != null && (date1.before(date2) || diffDays > cnt))
										{
											errList.add( "VTILRD" );
											errFields.add( childNodeName.toLowerCase() );	
										}
									}
								}
								else
								{
									errList.add( "VTILRDATE" );
									errFields.add( childNodeName.toLowerCase() );		
								}
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("shipment_id") )
						{
							columnValue = genericUtility.getColumnValue("shipment_id", dom);
							if(columnValue != null && columnValue.trim().length() > 0)
							{
								sql = "select count(1) from   shipment where  shipment_id = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errList.add( "VTSHIPIDEX" );
										errFields.add( childNodeName.toLowerCase() );		
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							//commented  by Nandkumar gadkari on 28/03/18
							/*else
							{
								errList.add( "VTSHIPID" );
								errFields.add( childNodeName.toLowerCase() );
							}*/
						}
						
						else if ( childNodeName.equalsIgnoreCase("stan_code__init") )
						{
							columnValue = genericUtility.getColumnValue("stan_code__init", dom);
							if(columnValue != null && columnValue.trim().length() > 0)
							{
								sql = "select count(*) from STATION where  STAN_CODE = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, columnValue);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt(1);
									if(cnt == 0)
									{
										errList.add( "VESTACD2" );
										errFields.add( childNodeName.toLowerCase() );		
									}
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

						else if ( childNodeName.equalsIgnoreCase("date_dep") )
						{
							if(genericUtility.getColumnValue("date_dep", dom) != null)
							{
								date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("date_dep", dom) , genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
							}
							if(genericUtility.getColumnValue("date_arr", dom) != null)
							{
								date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("date_arr", dom) , genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
							}
							if(date1 != null && date2 != null && date1.before(date2))
							{
								System.out.println("Date ["+date1+"]["+date2+"]");
								errList.add( "VTETDETA" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("date_arr") )
						{
							if(genericUtility.getColumnValue("date_dep", dom) != null)
							{
								date1 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("date_dep", dom) , genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
							}
							if(genericUtility.getColumnValue("date_arr", dom) != null)
							{
								date2 = Timestamp.valueOf(genericUtility.getValidDateString( genericUtility.getColumnValue("date_arr", dom), genericUtility.getApplDateFormat(),getDBDateFormat()) + " 00:00:00.0");;
							}
							if(date1 != null && date2 != null && date1.before(date2))
							{
								errList.add( "VTETDETA" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						//added by nandkumar gadkari on 17/01/19-------start------------------
						else if (childNodeName.equalsIgnoreCase("site_code")) {
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							System.out.println("siteCode: " + siteCode);
							if (siteCode == null || siteCode.trim().length() == 0) {
								errCode = "VMSITECD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("Site Code can not be blank!!");
							} else {
								if (siteCode != null && siteCode.trim().length() > 0) {
									if (!(isExist(conn, "site", "site_code", siteCode))) {
										errCode = "VMSITE1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								columnValue = null;
								columnValue = genericUtility.getColumnValue( "invoice_id", dom ); 
								
								if( columnValue != null && columnValue.trim().length() > 0 )
								{
									sql = " select Count(*) cnt from invoice where invoice_id = ? and site_code = ? "; 
								
									pstmt = conn.prepareStatement( sql );
									
									pstmt.setString( 1, columnValue );
									pstmt.setString( 2, siteCode );
									
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt( "cnt" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( cnt == 0 )
									{
										errCode = "VTINVSITE1";										
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );
									}
								}
							}
							
						}
						else if (childNodeName.equalsIgnoreCase("tran_date"))
						{
							tranDateStr = (genericUtility.getColumnValue("tran_date", dom));
							if(tranDateStr == null || tranDateStr.trim().length() ==0)
							{
								errCode ="VTTRANDT";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
								
							}
							else
							{
								
								tranDateStr=checkNullAndTrim(tranDateStr);
								siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
								if(siteCode !=null && siteCode.trim().length() > 0)
								{	
									System.out.println("@@@@ Tran Date[" + tranDateStr + "]");
									TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
									System.out.println("Trandate is"+TranDate);
									errCode = finCommon.nfCheckPeriod("FIN", TranDate,siteCode, conn);
									System.out.println("Errorcode in TranDate"+errCode);
									if (errCode != null && errCode.trim().length() > 0)
									  {
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
									  }	 
								}
							}
							
						}
						//added by nandkumar gadkari on 17/01/19-------end------------------
					}
				}	
				break;
				
      			case 2 :
				{
					parentList = dom.getElementsByTagName( "Detail2" );
					childList = parentList.item( 0 ).getChildNodes();
					noOfChilds = childList.getLength();
					for (int ctr = 0; ctr < noOfChilds; ctr++)
					{	
						childNode = childList.item( ctr );
						childNodeName = childNode.getNodeName();
						if ( childNode != null && childNode.getFirstChild() != null )
						{
							columnValue = childNode.getFirstChild().getNodeValue();
						}
						
						else if ( childNodeName.equalsIgnoreCase("status") )
						{
							
							columnValue = genericUtility.getColumnValue("status", dom);
							System.out.println("cURRENT COLUNM STATUS["+columnValue+"]");
							if(columnValue == null || columnValue.trim().length() == 0)
							{
								errList.add( "VMSTAT" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						
						if ( childNodeName.equalsIgnoreCase("tracking_no") )
						{
							columnValue = genericUtility.getColumnValue("tracking_no", dom);
							System.out.println("cURRENT COLUNM TRACKING NO["+columnValue+"]");
							if(columnValue == null || columnValue.trim().length() == 0)
							{
								errList.add( "VMTRACKCD" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						
						else if ( childNodeName.equalsIgnoreCase("status_date") )
						{
							columnValue = genericUtility.getColumnValue("status_date", dom);
							System.out.println("cURRENT COLUNM status datee["+columnValue+"]");
							if(columnValue == null || columnValue.trim().length() == 0)
							{
								errList.add( "VMSTSCD1" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
				}
			}
			int errListSize = errList.size();
			cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					//String errMsg = hashMap.get(errCode)!=null ? hashMap.get(errCode).toString():"";
					//System.out.println("errMsg .........."+errMsg);
					errString = getErrorString( errFldName, errCode, userId );
					errorType =  errorType( conn , errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				
				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}
			errString = errStringXml.toString();
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception in invoice amendmentIC  == >");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if( conn != null && !conn.isClosed() )
				{
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception :invoice amendmentIC:wfValData :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		return errString;
	}
	private String errorType( Connection conn , String errorCode ) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		}		
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		return msgType;
	}

public String itemChanged() throws RemoteException, ITMException
{
	return "";
}
public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
{
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	String errString = null;
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	try
	{
		if (xmlString != null && xmlString.trim().length()!=0)
		{
			dom = genericUtility.parseString(xmlString); 
		}
		if (xmlString1 != null && xmlString1.trim().length()!=0)
		{
			dom1 = genericUtility.parseString(xmlString1); 
		}
		if (xmlString2 != null && xmlString2.trim().length()!=0)
		{
			dom2 = genericUtility.parseString(xmlString2); 
		}
		errString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams );
		System.out.println ( "ErrString :" + errString);
	}
	catch (Exception e)
	{
		System.out.println ( "Exception :invoice amendmentIC :itemChanged(String,String):" + e.getMessage() + ":" );
		errString = genericUtility.createErrorString(e);
		throw new ITMException(e);
	}
	System.out.println ( "returning from invoice amendmentIC itemChanged" );
	return errString;
}
public String itemChanged( Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
{
	int grossWeight = 0;
	int frtAmt = 0;
	int exchRateFrt = 0;
	int exchRateIns = 0;
	int currentFormNo = 0;
	int InsAmt = 0;
	int noArt = 0;
	int pos = 0;
	int amt1 = 0;
	int childNodeListLength = 0;
	int ctr = 0;
	double amt4 = 0.0;
	String sql = "";
	String columnValue = "";
	String columnValue1 = "";
	String itemCode = "";
	String childNodeName = "";
	String descr = "";
	String descr1 = "";
	String reStr = "";
	String currCodeFrt = "";
	String currCodeIns = "";
	String lrNo = "";
	String octroiRcpNo = "";
	String lorryNo = "";
	String gpNo = "";
	String grNo = "";
	String sbNo = "";
	String stanCodeInit = "";
	String shipmentId = "";
	String custCode = "";
	String custName = "";
	String salesPers = "";
	String spName = "";
	String saleOrder = "";
	String tranCode = "";
	String transMode = ""; 
	String despId = "";
	String remark = "";
	String chequeNo = "";
	String custPord = "";
	String siteCode = "";
	Date effFrom = null;
	Date sbDate = null;
	Date grDate = null;
	Date sailDate = null;
	Date gpDate = null;
	Date lrDate = null;
	Date octroiRcpDate = null;
	Date chequeDate = null;
	Date pordDate = null;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	String currAppdate ="";
	String transModeVal = "";//Modified by Anjali R. on [16/01/2019]
	
	//SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
	StringBuffer valueXmlString = new StringBuffer();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();//Added by chandra shekar on 08-01-2014
	try
	{	SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());//Added by chandra shekar on 08-01-2014
		columnValue = genericUtility.getColumnValue( currentColumn, dom );
		ConnDriver connDriver = null;
		connDriver = new ConnDriver();
		//Changes and Commented By Bhushan on 13-06-2016 :START
		//conn = connDriver.getConnectDB("DriverValidator");
		conn = getConnection();
		//Changes and Commented By Bhushan on 13-06-2016 :END
		if( objContext != null && objContext.trim().length() > 0 )
		{
			currentFormNo = Integer.parseInt( objContext );
		}
		valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
		valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );

		switch ( currentFormNo )
		{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append( "<Detail1>\r\n" );
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				//itm_default added by nandkumar gadkari on 18/09/19------------
				if (currentColumn.trim().equalsIgnoreCase("itm_default") )			
				{	
					System.out.println("----------- inside itm_default ---------------");
					siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
					sql = " select descr from site where site_code = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString( "descr" ); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA["+descr+"]]>").append("</site_descr>");
					java.sql.Timestamp currDate = null;
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
					valueXmlString.append("<tran_date protect =\"0\">").append("<![CDATA["+currAppdate.trim()+"]]>").append("</tran_date>");
				
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "invoice_id" ) )
				{
					columnValue = genericUtility.getColumnValue("invoice_id",dom);
					/*Modified by Anjali R. on[18/01/2018][To add table with remarks field][Start]*/
					/*sql = "select invoice.frt_amt, invoice.curr_code__frt, invoice.exch_rate__frt, " +
							" invoice.curr_code__ins, invoice.exch_rate__ins, invoice.ins_amt, " +
							" invoice.cust_code,customer.cust_name,invoice.sales_pers,sales_pers.sp_name, " +
							" invoice.sale_order,invoice.tran_code,invoice.tran_mode ,invoice.desp_id, remarks " +
							" From  invoice LEFT OUTER JOIN customer ON invoice.cust_code = customer.cust_code " +
							" LEFT OUTER JOIN sales_pers ON invoice.sales_pers = sales_pers.sales_pers " +
							" where invoice.invoice_id = ?";*/
					//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
					/*sql = "select invoice.frt_amt, invoice.curr_code__frt, invoice.exch_rate__frt, " +
							" invoice.curr_code__ins, invoice.exch_rate__ins, invoice.ins_amt, " +
							" invoice.cust_code,customer.cust_name,invoice.sales_pers,sales_pers.sp_name, " +
							" invoice.sale_order,invoice.tran_code,invoice.tran_mode ,invoice.desp_id, invoice.remarks " +
							" From  invoice LEFT OUTER JOIN customer ON invoice.cust_code = customer.cust_code " +
							" LEFT OUTER JOIN sales_pers ON invoice.sales_pers = sales_pers.sales_pers " +
							" where invoice.invoice_id = ?";*/
					/*Modified by Anjali R. on[18/01/2018][To add table with remarks field][End]*/
					
					sql = "select invoice.frt_amt, invoice.curr_code__frt, invoice.exch_rate__frt, " +
							" invoice.curr_code__ins, invoice.exch_rate__ins, invoice.ins_amt, " +
							" invoice.cust_code,customer.cust_name,invoice.sales_pers,sales_pers.sp_name, " +
							" invoice.sale_order,invoice.tran_code,invoice.tran_mode ,invoice.desp_id, invoice.remarks ,invoice.trans_mode" +
							" From  invoice LEFT OUTER JOIN customer ON invoice.cust_code = customer.cust_code " +
							" LEFT OUTER JOIN sales_pers ON invoice.sales_pers = sales_pers.sales_pers " +
							" where invoice.invoice_id = ?";
					//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][End]
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						frtAmt = rs.getInt(1);
						currCodeFrt = checkNull(rs.getString(2));
						exchRateFrt = rs.getInt(3);
						currCodeIns = checkNull(rs.getString(4));
						exchRateIns = rs.getInt(5);
						InsAmt = rs.getInt(6);
						custCode = checkNull(rs.getString(7));
						custName = checkNull(rs.getString(8));
						salesPers = checkNull(rs.getString(9));
						spName = checkNull(rs.getString(10));
						saleOrder = checkNull(rs.getString(11));
						tranCode = checkNull(rs.getString(12));
						transMode = checkNull(rs.getString(13));
						despId = checkNull(rs.getString(14));
						remark = checkNull(rs.getString(15));
						//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
						transModeVal = checkNull(rs.getString("trans_mode"));
						//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][End]
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = "select gross_weight from despatch where desp_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, despId);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						grossWeight = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<gross_weight><![CDATA[" ).append( grossWeight ).append( "]]></gross_weight>\r\n" );

					// Query modified by sandesh on 21/May/2013 . cust_pord, pord_date added
					
					sql = "select cheque_no, cheque_date, cust_pord, pord_date from sorder where sale_order = ?";
					
					System.out.println("query => select cheque_no, cheque_date, cust_pord, pord_date from sorder where sale_order = '"+saleOrder+"'");
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrder);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						chequeNo = checkNull(rs.getString(1));
						chequeDate = rs.getDate(2);
						custPord = rs.getString("cust_pord");
						pordDate = rs.getDate("pord_date");
					}
					
					System.out.println("chequeNo : "+chequeNo+" chequeDate : ["+chequeDate+"] custPord : "+custPord+" pordDate : ["+pordDate+"]");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<ref_no><![CDATA[" ).append( chequeNo==null?"":chequeNo).append( "]]></ref_no>\r\n" );
					valueXmlString.append( "<ref_date><![CDATA[" ).append( chequeDate==null?"":sdf.format(chequeDate) ).append( "]]></ref_date>\r\n" );

					// Added by sandesh on 21/May/2013 . cust_pord, pord_date added
					
					valueXmlString.append( "<cust_pord><![CDATA[" ).append( custPord==null?"":custPord).append( "]]></cust_pord>\r\n" );
					valueXmlString.append( "<pord_date><![CDATA[" ).append( pordDate==null?"":sdf.format(pordDate) ).append( "]]></pord_date>\r\n" );
					
					sql = "select descr from gencodes where fld_value = ? and mod_name='W_INVOICE_AMENDMENT'";
					pstmt = conn.prepareStatement(sql);
					//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
					//pstmt.setString(1,transMode );
					pstmt.setString(1,transModeVal );
					//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1));
					}
					else
					{
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select descr from gencodes where fld_value = ? and fld_name = 'TRANS_MODE' and mod_name = 'X'";
						pstmt = conn.prepareStatement(sql);
						//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
						//pstmt.setString(1,transMode );
						pstmt.setString(1,transModeVal );
						//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							descr = checkNull(rs.getString(1));
						}
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][Start]
					//valueXmlString.append( "<trans_mode><![CDATA[" ).append( transMode ).append( "]]></trans_mode>\r\n" );
					valueXmlString.append( "<trans_mode><![CDATA[" ).append( transModeVal ).append( "]]></trans_mode>\r\n" );
					//Modified by Anjali R. on [16/01/2019][Added trans_mode column to set invoice trans_mode in Invoice Amendment trans_mode][End]
					valueXmlString.append( "<trans_descr><![CDATA[" ).append( descr ).append( "]]></trans_descr>\r\n" );
					descr = "";
					/*sql = "select tran_name from transporter where tran_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranCode );
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1));
					}
					
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;*/
					valueXmlString.append( "<invoice_cust_code><![CDATA[" ).append( custCode ).append( "]]></invoice_cust_code>\r\n" );
					valueXmlString.append( "<customer_cust_name><![CDATA[" ).append( custName ).append( "]]></customer_cust_name>\r\n" );
					valueXmlString.append( "<frt_amt><![CDATA[" ).append( frtAmt ).append( "]]></frt_amt>\r\n" );
					valueXmlString.append( "<invoice_sales_pers><![CDATA[" ).append( salesPers ).append( "]]></invoice_sales_pers>\r\n" );
					valueXmlString.append( "<sales_pers_sp_name><![CDATA[" ).append( spName ).append( "]]></sales_pers_sp_name>\r\n" );
					valueXmlString.append( "<curr_code__frt><![CDATA[" ).append( currCodeFrt ).append( "]]></curr_code__frt>\r\n" );
					valueXmlString.append( "<exch_rate__frt><![CDATA[" ).append( exchRateFrt ).append( "]]></exch_rate__frt>\r\n" );
					valueXmlString.append( "<ins_amt><![CDATA[" ).append( InsAmt ).append( "]]></ins_amt>\r\n" );
					valueXmlString.append( "<curr_code__ins><![CDATA[" ).append( currCodeIns ).append( "]]></curr_code__ins>\r\n" );
					valueXmlString.append( "<exch_rate__ins><![CDATA[" ).append( exchRateIns ).append( "]]></exch_rate__ins>\r\n" );
					valueXmlString.append( "<remarks><![CDATA[" ).append( remark ).append( "]]></remarks>\r\n" );
					
					reStr = itemChanged(dom, dom1, dom2, objContext, "status", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
					
					descr = "";
					descr1 = "";
					sql = "select 	distinct invdet.item_code, rtrim(ltrim(item.descr)), sum(invdet.quantity) " +
							" from invdet,item where invdet.item_code = item.item_code and invdet.invoice_id = ? " +
							" group by invdet.item_code, rtrim(ltrim(item.descr)) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					while( rs.next() )
					{
						itemCode = checkNull(rs.getString(1));
						descr = checkNull(rs.getString(2));
						amt1 = rs.getInt(3);
						if(descr.indexOf('/') > 0)
						{
							descr = descr.replace('/', '\'');
						}
						descr1 = descr1 + itemCode +"  "+ descr + "  "+ amt1 +"/"; 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<item_info><![CDATA[" ).append( descr1 ).append( "]]></item_info>\r\n" );
					valueXmlString.append( "<ref_no><![CDATA[" ).append( chequeNo==null?"":chequeNo).append( "]]></ref_no>\r\n" );
					valueXmlString.append( "<ref_date><![CDATA[" ).append( chequeDate==null?"":sdf.format(chequeDate) ).append( "]]></ref_date>\r\n" );
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "status" ) )
				{
					columnValue = genericUtility.getColumnValue("status",dom).trim();
					columnValue1 = genericUtility.getColumnValue("invoice_id",dom);
					if(columnValue.equals("S"))
					{
						sql = "select desp_id, eff_date from invoice where invoice_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, columnValue1);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							despId = checkNull(rs.getString(1));
							effFrom = rs.getDate(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select tran_code, lr_no, lr_date, octroi_rcp_no, orctoi_rcp_date," +
								" case when no_art is null then 0 else no_art end, lorry_no,gp_no, " +
								" gp_date,gr_no,gr_date,sail_date, sb_no,sb_date,stan_code__init, " +
								" SHIPMENT_ID from   despatch where  desp_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, despId);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							tranCode = checkNull(rs.getString(1));
							lrNo = checkNull(rs.getString(2));
							lrDate = rs.getDate(3);
							octroiRcpNo = checkNull(rs.getString(4));
							octroiRcpDate = rs.getDate(5);
							noArt = rs.getInt(6);
							lorryNo = checkNull(rs.getString(7));
							gpNo = checkNull(rs.getString(8));
							gpDate = rs.getDate(9);
							grNo = checkNull(rs.getString(10));
							grDate = rs.getDate(11);
							sailDate = rs.getDate(12);
							sbNo = checkNull(rs.getString(13));
							sbDate = rs.getDate(14);
							stanCodeInit = checkNull(rs.getString(15));
							shipmentId = checkNull(rs.getString(16)); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						descr = "";
						
						sql = "select tran_name from transporter where tran_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							descr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						valueXmlString.append( "<shipment_id><![CDATA[" ).append( shipmentId ).append( "]]></shipment_id>\r\n" );
						valueXmlString.append( "<tran_code protect = \"0\"><![CDATA[" ).append( tranCode ).append( "]]></tran_code>\r\n" );
						valueXmlString.append( "<transporter_tran_name><![CDATA[" ).append( descr ).append( "]]></transporter_tran_name>\r\n" );
						valueXmlString.append( "<lr_no protect = \"0\"><![CDATA[" ).append( lrNo ).append( "]]></lr_no>\r\n" );
						valueXmlString.append( "<lr_date protect = \"0\"><![CDATA[" ).append( lrDate==null?"":sdf.format(lrDate) ).append( "]]></lr_date>\r\n" );
						valueXmlString.append( "<no_art><![CDATA[" ).append( noArt ).append( "]]></no_art>\r\n" );
						valueXmlString.append( "<octroi_rcp_no protect = \"0\"><![CDATA[" ).append( octroiRcpNo ).append( "]]></octroi_rcp_no>\r\n" );
						valueXmlString.append( "<octroi_rcp_date protect = \"0\"><![CDATA[" ).append( octroiRcpDate==null?"":sdf.format(octroiRcpDate) ).append( "]]></octroi_rcp_date>\r\n" );
						valueXmlString.append( "<desp_id><![CDATA[" ).append( despId ).append( "]]></desp_id>\r\n" );
						valueXmlString.append( "<eff_date protect = \"0\"><![CDATA[" ).append( effFrom==null?"":sdf.format(effFrom) ).append( "]]></eff_date>\r\n" );
						valueXmlString.append( "<lorry_no><![CDATA[" ).append( lorryNo ).append( "]]></lorry_no>\r\n" );
						valueXmlString.append( "<gp_no><![CDATA[" ).append( gpNo ).append( "]]></gp_no>\r\n" );
						valueXmlString.append( "<gp_date><![CDATA[" ).append( gpDate==null?"":sdf.format(gpDate) ).append( "]]></gp_date>\r\n" );
						valueXmlString.append( "<gr_no><![CDATA[" ).append( grNo ).append( "]]></gr_no>\r\n" );
						valueXmlString.append( "<gr_date><![CDATA[" ).append( grDate==null?"":sdf.format(grDate) ).append( "]]></gr_date>\r\n" );
						valueXmlString.append( "<sail_date><![CDATA[" ).append( sailDate==null?"":sdf.format(sailDate) ).append( "]]></sail_date>\r\n" );
						valueXmlString.append( "<sb_no><![CDATA[" ).append( sbNo ).append( "]]></sb_no>\r\n" );
						valueXmlString.append( "<sb_date><![CDATA[" ).append( sbDate==null?"":sdf.format(sbDate) ).append( "]]></sb_date>\r\n" );
						valueXmlString.append( "<stan_code__init><![CDATA[" ).append( stanCodeInit ).append( "]]></stan_code__init>\r\n" );
						valueXmlString.append( "<ref_no><![CDATA[" ).append( genericUtility.getColumnValue("ref_no", dom) ).append( "]]></ref_no>\r\n" );
						valueXmlString.append( "<ref_date><![CDATA[" ).append( checkDate(genericUtility.getColumnValue("ref_date", dom))  ).append( "]]></ref_date>\r\n" );
						setNodeValue( dom,"stan_code__init" ,stanCodeInit );
						reStr = itemChanged(dom, dom1, dom2, objContext, "stan_code__init", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);
					}
					else
					{
						valueXmlString.append( "<tran_code protect = \"1\"><![CDATA[]]></tran_code>\r\n" );
						valueXmlString.append( "<lr_no protect = \"1\"><![CDATA[]]></lr_no>\r\n" );
						valueXmlString.append( "<lr_date protect = \"1\"><![CDATA[" ).append( checkDate(genericUtility.getColumnValue("lr_date", dom)) ).append( "]]></lr_date>\r\n" );
						if(columnValue.equals("B"))
						{
							valueXmlString.append( "<octroi_rcp_no protect = \"1\"><![CDATA[]]></octroi_rcp_no>\r\n" );
							valueXmlString.append( "<octroi_rcp_date protect = \"1\"><![CDATA[" ).append( checkDate(genericUtility.getColumnValue("octroi_rcp_date", dom)) ).append( "]]></octroi_rcp_date>\r\n" );
						}
						else
						{
							valueXmlString.append( "<octroi_rcp_no protect = \"0\"><![CDATA[]]></octroi_rcp_no>\r\n" );
							valueXmlString.append( "<octroi_rcp_date protect = \"0\"><![CDATA[" ).append( checkDate(genericUtility.getColumnValue("octroi_rcp_date", dom))  ).append( "]]></octroi_rcp_date>\r\n" );
						}
						valueXmlString.append( "<transporter_tran_name><![CDATA[]]></transporter_tran_name>\r\n" );
						valueXmlString.append( "<desp_id><![CDATA[]]></desp_id>\r\n" );
						valueXmlString.append( "<eff_date protect = \"0\"><![CDATA[" ).append( checkDate(genericUtility.getColumnValue("eff_date", dom)) ).append( "]]></eff_date>\r\n" );
						valueXmlString.append( "<ref_no protect = \"1\"><![CDATA[]]></ref_no>\r\n" );
						valueXmlString.append( "<ref_date protect = \"1\"><![CDATA[" ).append( checkDate(genericUtility.getColumnValue("ref_date", dom)) ).append( "]]></ref_date>\r\n" );
					}
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "tran_code" ) )
				{
					columnValue = genericUtility.getColumnValue("tran_code",dom);
					sql = "select tran_name from transporter where tran_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<transporter_tran_name><![CDATA[" ).append( descr ).append( "]]></transporter_tran_name>\r\n" );
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "trans_mode" ) )
				{
					columnValue = genericUtility.getColumnValue("trans_mode",dom);
					sql = "select descr from gencodes where fld_value = ? and fld_name = 'TRANS_MODE' and mod_name = 'W_INVOICE_AMENDMENT'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1));
					}
					else
					{
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select descr from gencodes where fld_value = ? and fld_name = 'TRANS_MODE' and mod_name = 'X'";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, columnValue);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							descr = checkNull(rs.getString(1));
						}
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<trans_descr><![CDATA[" ).append( descr ).append( "]]></trans_descr>\r\n" );
				}

				
				if( currentColumn.trim().equalsIgnoreCase( "curr_code__frt" ) )
				{
					columnValue = genericUtility.getColumnValue("curr_code__frt",dom);
					sql = "select std_exrt from currency where curr_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						amt4 = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<exch_rate__frt><![CDATA[" ).append( amt4 ).append( "]]></exch_rate__frt>\r\n" );
				}

				
				if( currentColumn.trim().equalsIgnoreCase( "curr_code__ins" ) )
				{
					columnValue = genericUtility.getColumnValue("curr_code__ins",dom);
					sql = "select std_exrt from currency where curr_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						amt4 = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<exch_rate__ins><![CDATA[" ).append( amt4 ).append( "]]></exch_rate__ins>\r\n" );
				}

				
				if( currentColumn.trim().equalsIgnoreCase( "stan_code__init" ) )
				{
					columnValue = genericUtility.getColumnValue("stan_code__init",dom);
					sql = "select descr from station where stan_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, columnValue);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append( "<station_descr><![CDATA[" ).append( descr ).append( "]]></station_descr>\r\n" );
				}

				
				if( currentColumn.trim().equalsIgnoreCase( "shipment_id" ) )
				{
					columnValue = genericUtility.getColumnValue("shipment_id",dom);
					if(columnValue != null && columnValue.trim().length() > 0)
					{
						sql = "select lr_no,lr_date	from shipment where shipment_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, columnValue);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lrNo = checkNull(rs.getString(1));
							lrDate = rs.getDate(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(lrNo.trim().length() > 0)
						{
							valueXmlString.append( "<lr_no><![CDATA[" ).append( lrNo ).append( "]]></lr_no>\r\n" );
						}
						if(lrDate != null)
						{
							valueXmlString.append( "<lr_date><![CDATA[" ).append( lrDate==null?"":sdf.format(lrDate) ).append( "]]></lr_date>\r\n" );
						}
					}
					else
					{
						columnValue1 = genericUtility.getColumnValue("desp_id",dom);
						if(columnValue1 != null && columnValue1.trim().length() > 0)
						{
							sql = "select lr_no, lr_date from  despatch	where  desp_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, columnValue1);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lrNo = checkNull(rs.getString(1));
								lrDate = rs.getDate(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(lrNo.trim().length() > 0)
							{
								valueXmlString.append( "<lr_no><![CDATA[" ).append( lrNo ).append( "]]></lr_no>\r\n" );
							}
							if(lrDate != null)
							{
								valueXmlString.append( "<lr_date><![CDATA[" ).append( lrDate==null?"":sdf.format(lrDate) ).append( "]]></lr_date>\r\n" );
							}
						}
					}
				}
				//added by nandkumar gadkari on 18/01/19-----------------------start---------------
				 if (currentColumn.trim().equalsIgnoreCase("site_code") )			
				{
					siteCode = genericUtility.getColumnValue("site_code",dom);
					sql = " select descr from site where site_code = ?"; 
				    pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = rs.getString(1); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					
					valueXmlString.append("<site_descr>").append("<![CDATA[" +descr+ "]]>").append("</site_descr>");
					
				}
				//added by nandkumar gadkari on 18/01/19-----------------------end---------------
				valueXmlString.append( "</Detail1>\r\n" );
				break;
			
			
			case 2:
			{
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					columnValue = genericUtility.getColumnValue("tran_id", dom1);
					valueXmlString.append( "<tran_id><![CDATA[" ).append( checkNull(columnValue) ).append( "]]></tran_id>\r\n" );
					valueXmlString.append( "<status_date><![CDATA[" ).append( sdf.format(new java.util.Date()) ).append( "]]></status_date>\r\n" );
				}
				valueXmlString.append( "</Detail2>\r\n" );
				System.out.println("------------------------COMPLETE DESCRIPTION-------------------");
				break;
			}
		}
	}
	catch(Exception e)
	{
		System.out.println( "Exception :invoice amendment :itemChanged(Document,String):" + e.getMessage() + ":" );
		valueXmlString = valueXmlString.append( genericUtility.createErrorString( e ) );
		throw new ITMException(e);
	}
	finally
	{
		try
		{
			if( conn != null && ! conn.isClosed() )
			{
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				
				conn.close();
			}
			//System.out.println("------------------------INSIDE FINALLY-------------------");
		}
		catch(Exception e)
		{
			System.out.println( "Exception :invoice amendment:itemChanged :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
	}
	valueXmlString.append( "</Root>\r\n" );	
	System.out.println( "\n****ValueXmlString :" + valueXmlString.toString() + ":********" );
	return valueXmlString.toString();
}
private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
{
	Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

	if( tempNode != null )
	{
		if( tempNode.getFirstChild() == null )
		{
			CDATASection cDataSection = dom.createCDATASection( nodeVal );
			tempNode.appendChild( cDataSection );
		}
		else
		{
			tempNode.getFirstChild().setNodeValue(nodeVal);
		}
	}
	tempNode = null;
}	

private String checkNull( String input )
{
	if ( input == null )
	{
		input = "";
	}
	return input;
}

private String checkDate( String input ) throws ITMException
{
	try
	{
		E12GenericUtility genericUtility= new  E12GenericUtility();
		if(input != null && input.trim().length() > 0 && (! input.trim().equals("null")))
		{
			input =	genericUtility.getValidDateString(input, getApplDateFormat());
		}
		else
			input = "";
	}
	catch (Exception e) 
	{
		input = "";
		System.out.println("Error in date format at 1283"+e.getMessage());
		e.printStackTrace();
		throw new ITMException(e);
		
	}
	

	return input;
}
private boolean isExist(Connection conn, String tableName, String columnName, String value)
		throws ITMException, RemoteException {
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	String sql = "";
	boolean status = false;
	try {
		sql = "SELECT count(*) from " + tableName + " where " + columnName + "  = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();

		if (rs.next()) {
			if (rs.getBoolean(1)) {
				status = true;
			}
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
	} catch (Exception e) {
		System.out.println("Exception in isExist ");
		e.printStackTrace();
		throw new ITMException(e);
	}
	System.out.println("returning String from isExist ");
	return status;
}
public String checkNullAndTrim( String inputVal )
{
	if ( inputVal == null )
	{
		inputVal = "";
	}
	else
	{
		inputVal = inputVal.trim();
	}
	return inputVal;
}
private static String getAbsString( String str )
{
	return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
}

 
}