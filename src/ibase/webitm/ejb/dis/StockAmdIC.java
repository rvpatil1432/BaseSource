package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@javax.ejb.Stateless
public class StockAmdIC extends ValidatorEJB implements StockAmdICLocal, StockAmdICRemote 
{
	E12GenericUtility genericUtility = new E12GenericUtility();	
	DistCommon discommon = new DistCommon();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
	@Override
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}

	/**
	 * The public method is used for converting the current form data into a document(DOM)
	 * The dom is then given as argument to the overloaded function wfValData to perform validation
	 * Returns validation string if exists else returns null in XML format
	 * @param xmlString contains the current form data in XML format
	 * @param xmlString1 contains all the header information in the XML format
	 * @param xmlString2 contains the data of all the forms in XML format
	 * @param objContext represents the form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */	
	@Override
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		try
		{
			System.out.println( "xmlString inside wfValData :::::::" + xmlString);
			System.out.println( "xmlString1 inside wfValData :::::::" + xmlString1);
			System.out.println( "xmlString2 inside wfValData :::::::" + xmlString2);
			
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
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams);
			System.out.println( "ErrString: " + errString);
		}
		catch(Exception e)
		{
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return (errString); 
	}
	/**
	 * The public overloaded method takes a document as input and is used for the validation of required fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currFormDataDom contains the current form data as a document object model
	 * @param hdrDataDom contains all the header information
	 * @param allFormDataDom contains the field data of all the forms 
	 * @param objContext represents form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
	{   				
		System.out.println("wfValData inside ----->>");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		E12GenericUtility genericUtility;
		String errString = "", userId = "";
		
		
		String ls_itemcode = "", ls_sitecode = "", ls_track_shelf_life = "", ld_mfg_date = "", ld_exp_date = "", ls_autoreqc = "", 
			ld_retest = "", ls_supp_sour = "", ls_loc_code = "", ls_lot_no = "", ls_lot_sl = "", mdate1 = "", ls_suppcodemfg = "", 
			ls_apprsupp = "", ls_pack_code = "", ld_mfg_date_o = "", ld_exp_date_o = "", ls_sitecode_o = "", ld_retest_o = "", lc_potency_o = "",
			ls_suppcodemfg_o = "", ls_dim_o = "", lc_conv_qty_o = "", ls_consider_allocate__o = "", lc_qty_per_art_o = "",
			lc_no_art_o = "", ls_pack_code_o = "", lc_potency = "", ls_dim = "", lc_conv_qty = "", ls_consider_allocate = "",
			lc_qty_per_art = "", lc_no_art = "";
		
		String sql = "";
		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int currentFormNo = 0, childNodeListLength = 0, ctr = 0, cnt = 0;
		String childNodeName = "", errorType = "", errCode = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		
		try
		{	
			conn = getConnection();
			
			genericUtility = new E12GenericUtility();	
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			System.out.println("xtraParam----->>["+xtraParams+"]");
			System.out.println("editFlag ------------>>["+editFlag+"]");
			
/*			System.out.println("DOM---->>["+genericUtility.serializeDom(dom).toString()+"]");
			System.out.println("DOM1----->>["+genericUtility.serializeDom(dom1).toString()+"]");
			System.out.println("DOM2----->>["+genericUtility.serializeDom(dom2).toString()+"]");	*/

			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo  = Integer.parseInt(objContext);
			}	
			
			switch (currentFormNo)  
			{
			case 1:
				System.out.println("------in detail1 validation----------------");
				System.out.println("DOM in case 1---->>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1 in case 1----->>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 in case 1 ----->>["+genericUtility.serializeDom(dom2).toString()+"]");	

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength  = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{					
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName ------->>["+childNodeName+"]");

					if("item_code".equalsIgnoreCase(childNodeName))
					{
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						System.out.println("ls_itemcode inside wfvaladta========>>["+ls_itemcode+"]");
						
						if(ls_itemcode.length() == 0)
						{
							errCode = "VTITMNUL";		                               
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) FROM ITEM WHERE ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_itemcode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
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
							if(count == 0)
							{
								errCode = "VMITEM1";		
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("site_code".equalsIgnoreCase(childNodeName))
					{
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
						System.out.println("ls_sitecode inside wfvaladta========>>["+ls_sitecode+"]");
						
						if(ls_sitecode.length() == 0)
						{
							errCode = "NULLSITE";		                               
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) FROM SITE WHERE SITE_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_sitecode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
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
							if(count == 0)
							{
								errCode = "VMSITE";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
							
							//Changed by wasim on 18-APR-2017 for period status validation [START]
							String prdCode = "",statIC = "";
							count = 0;
							sql = " SELECT CODE FROM PERIOD WHERE ? BETWEEN FR_DATE AND TO_DATE ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, getCurrtDate());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								prdCode = checkNullAndTrim(rs.getString("CODE"));
								count++;
							}
							if (rs != null) 
							{					
								rs.close();rs = null;
							}
							if (pstmt != null ) 
							{					
								pstmt.close();pstmt = null;
							}
							
							if(count == 0)
							{
								errList.add( "VTSITEPD" );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{	
								sql = " SELECT STAT_IC FROM PERIOD_STAT WHERE PRD_CODE = ? AND SITE_CODE = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, prdCode);
								pstmt.setString(2, ls_sitecode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									statIC = checkNullAndTrim(rs.getString("STAT_IC"));
								}
								if (rs != null) 
								{					
									rs.close();rs = null;
								}
								if (pstmt != null ) 
								{					
									pstmt.close();pstmt = null;
								}
								
								System.out.println("Inventory Status["+statIC+"]");
								
								if("N".equalsIgnoreCase(statIC))
								{
									errList.add( "VTPRDINV" );
									errFields.add( childNodeName.toLowerCase() );
								}	
							}
							//Changed by wasim on 18-APR-2017 for period status validation [END]
						}
						
					}
					else if("loc_code".equalsIgnoreCase(childNodeName))
					{
						ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom));
						System.out.println("ls_loc_code inside wfvaladta========>>["+ls_loc_code+"]");
						
						if(ls_loc_code.length() == 0)
						{
							errCode = "DIDOLCNULL";		                               
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "SELECT COUNT(*) FROM LOCATION WHERE LOC_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_loc_code);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
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
							if(cnt == 0)
							{
								errCode = "VTINVLLC";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("lot_no".equalsIgnoreCase(childNodeName))
					{
						cnt = 0;
						System.out.println("------------ Inside lot_no ...... =================== ");
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
						ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom));
						ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
						ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", dom));	
						
						if(ls_lot_no.length() == 0)
						{
							errCode = "NULLLOTNO";		                               
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						else if(ls_lot_no.length() > 0)
						{
							
							sql = "SELECT COUNT(*) FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_itemcode);
							pstmt.setString(2, ls_sitecode);
							pstmt.setString(3, ls_loc_code);
							pstmt.setString(4, ls_lot_no);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
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
							if(cnt == 0)
							{
								errCode = "VTLOTASN";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
						if(cnt > 0)
						{
							errCode = chkQcOrd(ls_itemcode, ls_sitecode, ls_loc_code, ls_lot_no, ls_lot_sl, conn);
							if(errCode.length() > 0)
							{
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("lot_sl".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------------ Inside lot_sl ...... =================== ");
						cnt = 0;
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom));
						ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", dom));
						ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", dom));
						
						if(ls_lot_sl.length() == 0)
						{
							errCode = "NULLLOTSL";		                               
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						else if(ls_lot_sl.length() > 0)
						{
							if(ls_sitecode.length() == 0)
							{
								ls_sitecode = "     ";
							}
							if(ls_itemcode.length() == 0)
							{
								ls_itemcode = "          ";
							}
							if(ls_loc_code.length() == 0)
							{
								ls_loc_code = "        ";
							}
							if(ls_lot_no.length() == 0)
							{
								ls_lot_no = "               ";
							}
							if(ls_lot_sl.length() == 0)
							{
								ls_lot_sl = "     ";
							}
							sql = "SELECT COUNT(*) FROM STOCK WHERE SITE_CODE = ? AND ITEM_CODE = ? AND LOC_CODE  = ? AND LOT_NO = ?  AND LOT_SL = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_sitecode);
							pstmt.setString(2, ls_itemcode);
							pstmt.setString(3, ls_loc_code);
							pstmt.setString(4, ls_lot_no);
							pstmt.setString(5, ls_lot_sl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
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
							if(cnt == 0)
							{
								errCode = "VMLOTSV";		
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
						if(cnt > 0)
						{
							ld_mfg_date_o = checkNullAndTrim(genericUtility.getColumnValue("mfg_date__o", dom));
							ld_exp_date_o = checkNullAndTrim(genericUtility.getColumnValue("exp_date__o", dom));
							ls_sitecode_o = checkNullAndTrim(genericUtility.getColumnValue("site_code__mfg__o", dom));
							ld_retest_o = checkNullAndTrim(genericUtility.getColumnValue("retest_date_o", dom));
							lc_potency_o = checkNullAndTrim(genericUtility.getColumnValue("potency_perc_o", dom));
							ls_suppcodemfg_o = checkNullAndTrim(genericUtility.getColumnValue("supp_code__mfg__o", dom));
							ls_dim_o = checkNullAndTrim(genericUtility.getColumnValue("dimension__o", dom));
							lc_conv_qty_o = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom_o", dom));
							ls_consider_allocate__o = checkNullAndTrim(genericUtility.getColumnValue("consider_allocate__o", dom));
							lc_qty_per_art_o = checkNullAndTrim(genericUtility.getColumnValue("qty_per_art_o", dom));
							lc_no_art_o = checkNullAndTrim(genericUtility.getColumnValue("no_art_o", dom));
							ls_pack_code_o = checkNullAndTrim(genericUtility.getColumnValue("pack_code_o", dom));

							ld_mfg_date = checkNullAndTrim(genericUtility.getColumnValue("mfg_date", dom));
							ld_exp_date = checkNullAndTrim(genericUtility.getColumnValue("exp_date", dom));
							ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code__mfg", dom));
							ld_retest = checkNullAndTrim(genericUtility.getColumnValue("retest_date", dom));
							lc_potency = checkNullAndTrim(genericUtility.getColumnValue("potency_perc", dom));
							ls_suppcodemfg = checkNullAndTrim(genericUtility.getColumnValue("supp_code__mfg", dom));				
							ls_dim = checkNullAndTrim(genericUtility.getColumnValue("dimension", dom));
							lc_conv_qty = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", dom));
							ls_consider_allocate = checkNullAndTrim(genericUtility.getColumnValue("consider_allocate", dom));
							lc_qty_per_art = checkNullAndTrim(genericUtility.getColumnValue("qty_per_art", dom));
							lc_no_art = checkNullAndTrim(genericUtility.getColumnValue("no_art", dom));
							ls_pack_code = checkNullAndTrim(genericUtility.getColumnValue("pack_code", dom));
		
							if(ld_mfg_date_o.equalsIgnoreCase(ld_mfg_date) && ld_exp_date_o.equalsIgnoreCase(ld_exp_date) &&
								ls_sitecode_o.equalsIgnoreCase(ls_sitecode) && ld_retest_o.equalsIgnoreCase(ld_retest) &&
								lc_potency_o.equalsIgnoreCase(lc_potency) && ls_suppcodemfg_o.equalsIgnoreCase(ls_suppcodemfg) &&
								ls_dim.equalsIgnoreCase(ls_dim_o) && lc_conv_qty_o.equalsIgnoreCase(lc_conv_qty) &&
								ls_consider_allocate.equalsIgnoreCase(ls_consider_allocate__o) && lc_qty_per_art.equalsIgnoreCase(lc_qty_per_art_o) &&
								lc_no_art.equalsIgnoreCase(lc_no_art_o) && ls_pack_code.equalsIgnoreCase(ls_pack_code_o))
							{
								errCode = "VTOLDNEW";		                               
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("mfg_date".equalsIgnoreCase(childNodeName))
					{
						ld_mfg_date = checkNullAndTrim(genericUtility.getColumnValue("mfg_date", dom));
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						System.out.println("ld_mfg_date==========>>"+ld_mfg_date);
						
						sql = "SELECT TRACK_SHELF_LIFE  FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_track_shelf_life = checkNullAndTrim(rs.getString("TRACK_SHELF_LIFE"));
						}
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
						if("Y".equalsIgnoreCase(ls_track_shelf_life) && ld_mfg_date.length() == 0)         
						{
							errCode = "VTMFGDATE3";	
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if("exp_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------------ Inside exp_date ........... =================== ");
						
						ld_exp_date = checkNullAndTrim(genericUtility.getColumnValue("exp_date", dom));
						ld_mfg_date = checkNullAndTrim(genericUtility.getColumnValue("mfg_date", dom));
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						
						sql = "SELECT TRACK_SHELF_LIFE FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_track_shelf_life = checkNullAndTrim(rs.getString("TRACK_SHELF_LIFE"));
						}
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
						if("Y".equalsIgnoreCase(ls_track_shelf_life) && ld_exp_date.length() == 0)
						{
							errCode = "VMEXPDATE1";		
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}

						if(ld_exp_date.length() != 0 && ld_mfg_date.length() != 0)
						{
							Date ld_exp_date1 = sdf.parse(ld_exp_date);
							Date ld_mfg_date1 = sdf.parse(ld_mfg_date);
							
							if(ld_exp_date1.before(ld_mfg_date1))
							{
								errCode = "VMEXPDT2";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if("site_code__mfg".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------------ Inside site_code__mfg ...... =================== ");
						
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code__mfg", dom));
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						ls_suppcodemfg = checkNullAndTrim(genericUtility.getColumnValue("supp_code__mfg", dom));
						sql = "SELECT SUPP_SOUR FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_supp_sour = checkNullAndTrim(rs.getString("SUPP_SOUR"));
						}
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
						
						if("M".equalsIgnoreCase(ls_supp_sour) && ls_sitecode.length() == 0 && ls_suppcodemfg.length()==0)
						{
							errCode = "VTSITEMFG1";		
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}	
						
						sql = "SELECT COUNT(*) FROM SITE WHERE SITE_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_sitecode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
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
						if(cnt == 0 && ls_suppcodemfg.length()==0)
						{
							errCode = "VMSITE";		
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if("retest_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------------ Inside retest_date ........... ");

						ld_retest = checkNullAndTrim(genericUtility.getColumnValue("retest_date", dom));
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
						ld_exp_date = checkNullAndTrim(genericUtility.getColumnValue("exp_date", dom));
						ld_mfg_date = checkNullAndTrim(genericUtility.getColumnValue("mfg_date", dom));

						sql = "SELECT AUTO_REQC FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_sitecode);
						pstmt.setString(2, ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_autoreqc = checkNullAndTrim(rs.getString("AUTO_REQC"));
						}
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
						
						if(ls_autoreqc.length() == 0)
						{
							ls_autoreqc = "N";
						}
						
						if("N".equalsIgnoreCase(ls_autoreqc))
						{
							sql = "SELECT AUTO_REQC FROM ITEM WHERE ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_itemcode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_autoreqc = checkNullAndTrim(rs.getString("AUTO_REQC"));
							}
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
						}
						
						if("Y".equalsIgnoreCase(ls_autoreqc))
						{
							Timestamp tempTestDate = Timestamp.valueOf("1900-01-01 00:00:00");
							
							if(ld_retest.length() == 0 || ld_retest.equals(tempTestDate))
							{
								errCode = "VTRETSTDT";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if(ld_exp_date.length() != 0 && ld_mfg_date.length() != 0 && ld_retest.length() != 0)
						{
							Date ld_retest_date = sdf.parse(ld_retest);
							Date ld_mfg_date1 = sdf.parse(ld_mfg_date);
							Date ld_exp_date1 = sdf.parse(ld_exp_date);
							
							
							if (ld_retest_date.before(ld_mfg_date1) || (ld_retest_date.after(ld_exp_date1)))
							{
								errCode = "VTRETSTDT1";
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
					}
					else if("supp_code__mfg".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------------ Inside ssupp_code__mfg ...... =================== ");
						mdate1 = sdf.format(new java.util.Date());

						ls_suppcodemfg = checkNullAndTrim(genericUtility.getColumnValue("supp_code__mfg", dom));
						ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
						
						sql = "SELECT (CASE WHEN APPR_SUPP IS NULL THEN 'N' ELSE APPR_SUPP END) AS LS_APPRSUPP FROM SITEITEM " +
							"WHERE SITE_CODE = ? AND   ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_sitecode);
						pstmt.setString(2, ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_apprsupp = checkNullAndTrim(rs.getString("LS_APPRSUPP"));
						}
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
						
						if("N".equalsIgnoreCase(ls_apprsupp) && ls_suppcodemfg.length() > 0)
						{
							sql = "SELECT COUNT(1) FROM SUPPLIER WHERE SUPP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_suppcodemfg);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
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
						
							if(cnt == 0)
							{
								errCode = "VTSUPP2";		
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// added by nandkumar gadkari on 15/01/19-------start-----------
						ls_sitecode = checkNullAndTrim(genericUtility.getColumnValue("site_code__mfg", dom));
						if(ls_suppcodemfg.length() == 0 && ls_sitecode.length() == 0)
						{
							errCode = "VTSUPPNULL";		
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						// added by nandkumar gadkari on 15/01/19-------end -----------
					}
					else if("pack_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------------ Inside pack_code ...... =================== ");
						ls_pack_code = checkNullAndTrim(genericUtility.getColumnValue("pack_code", dom));
						
						if(ls_pack_code.length() > 0)
						{
							sql = "SELECT COUNT(*) FROM PACKING WHERE PACK_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pack_code);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
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
							if(cnt == 0)
							{
								errCode = "VTPKCD1";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				break;			

			}//End of switch statement			
			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = itmDBAccess.getErrorString( errFldName, errCode, userId ,"",conn);
					errorType =  errorType( conn, errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
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
		}
		catch ( Exception e )
		{
			System.out.println ( "Exception: StockAmdIC: wfValData( Document currFormDataDom ): " + e.getMessage() + ":" );
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
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
			}
			catch(Exception e)
			{
				System.out.println( "Exception : StockAmdIC:wfValData : " + e.getMessage() );
			}
		}
		System.out.println( "errString>>>>>>>::" + errString );
		return errString;
	}

	private String errorType( Connection conn , String errorCode )
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
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
			}
		}		
		return msgType;
	}


	@Override
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}

	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document currFormDataDom = null;
		Document hdrDataDom = null;
		Document allFormDataDom = null;
		String errString = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		System.out.println("xmlString ["+xmlString+"]");
		System.out.println("xmlString1 ["+xmlString1+"]");
		System.out.println("xmlString2 ["+xmlString2+"]");
		try
		{
			if (xmlString != null && xmlString.trim().length()!=0)
			{
				currFormDataDom = genericUtility.parseString(xmlString); 
			}
			if (xmlString1 != null && xmlString1.trim().length()!=0)
			{
				hdrDataDom = genericUtility.parseString(xmlString1); 
			}
			if (xmlString2 != null && xmlString2.trim().length()!=0)
			{
				allFormDataDom = genericUtility.parseString(xmlString2); 
			}
			System.out.println ( "Calling  itemChanged( currFormDataDom, hdrDataDom, allFormDataDom, objContext, currentColumn, editFlag, xtraParams )");
			errString = itemChanged( currFormDataDom, hdrDataDom, allFormDataDom, objContext, currentColumn, editFlag, xtraParams );
			System.out.println ( "ErrString :" + errString);
		}
		catch (Exception e)
		{
			System.out.println ( "Exception : StockAmdIC:itemChanged(String,String):" + e.getMessage() + ":" );
			throw new ITMException(e);
		}
		System.out.println ( "returning from StockAmdIC: itemChanged \n[" + errString + "]" );

		return errString;
	}

	@Override
	public String itemChanged( Document currFormDataDom, Document hdrDataDom, Document allFormDataDom, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{	
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;		
		int currentFormNo = 0;
		StringBuffer valueXmlString;
		String sql = "", currDate = "", ls_item_code = "", ls_descr = "", ls_site_code = "", ls_loc_code = "", ls_lot_no = "", ls_lot_sl = "", 
			ls_loccode = "", ld_mfg_date = "", ls_track_shelf_life = "", li_shelf_life = "", li_lot_shelf_life = "", lc_qtyperart = "",
			ld_exp_date = "";
		
		double ll_modqty = 0.0, ll_noart = 0.0, lc_divqty = 0.0;
		int lc_quantity = 0;
		Date ld_exp_date1 = null;
		java.sql.Timestamp mfgDateTs = null;
		
		System.out.println("xtraParams=["+xtraParams+"]");
		System.out.println("currentColumn inside itemChanged................. : ["+currentColumn+"]");
		System.out.println("currentFormNo inside itemChanged................. : ["+currentFormNo+"]");
				
		valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?><Root><Header><editFlag>" );
		valueXmlString.append( editFlag ).append( "</editFlag></Header>" );
		try
		{
			conn = getConnection();
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = sdf.format(new java.util.Date());
			
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}

			switch ( currentFormNo )  
			{
			case 1:
			{
				System.out.println("hdrDataDom in itemchanged case 1------->>["+genericUtility.serializeDom(hdrDataDom)+"]");	
				System.out.println("currFormDataDom in itemchanged case1 ------>>["+genericUtility.serializeDom(currFormDataDom)+"]");
				System.out.println("allFormDataDom in itemchanged case1 ------>>["+genericUtility.serializeDom(allFormDataDom)+"]");
				
				valueXmlString.append( "<Detail1>\r\n" );
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("------------ Inside itm_default ------------------");
					valueXmlString.append( "<tran_date><![CDATA[" ).append( currDate ).append( "]]></tran_date>\r\n" );
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "item_code" ) )
				{
					System.out.println("------------ Inside item_code ------------------");
					ls_item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					
					sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_item_code);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_descr = checkNullAndTrim(rs.getString("DESCR"));
					}
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
					
					valueXmlString.append( "<item_descr><![CDATA[" ).append( ls_descr ).append( "]]></item_descr>\r\n" );

					ls_site_code = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", currFormDataDom));
					ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", currFormDataDom));
					ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", currFormDataDom));

					//valueXmlString = setValue(ls_item_code, ls_site_code, ls_loc_code, ls_lot_no, ls_lot_sl, valueXmlString, conn);
				}
				
				if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					System.out.println("------------ Inside site_code ------------------");
					ls_site_code = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site_code);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_descr = checkNullAndTrim(rs.getString("DESCR"));
					}
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
					valueXmlString.append( "<site_descr><![CDATA[" ).append( ls_descr ).append( "]]></site_descr>\r\n" );
					
					//valueXmlString = setValue(ls_item_code, ls_site_code, ls_loc_code, ls_lot_no, ls_lot_sl, valueXmlString, conn);
				}
				if( currentColumn.trim().equalsIgnoreCase( "loc_code" ) )
				{
					ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", currFormDataDom));
					
					sql = "SELECT DESCR FROM LOCATION WHERE LOC_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_loc_code);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_descr = checkNullAndTrim(rs.getString("DESCR"));
					}
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
					
					valueXmlString.append( "<location_descr><![CDATA[" ).append( ls_descr ).append( "]]></location_descr>\r\n" );
					/*
					ls_item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", hdrDataDom));
					ls_site_code = checkNullAndTrim(genericUtility.getColumnValue("site_code", hdrDataDom));
					ls_lot_no = checkNullAndTrim(genericUtility.getColumnValue("lot_no", hdrDataDom));
					ls_lot_sl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl", hdrDataDom));

					valueXmlString = setValue(ls_item_code, ls_site_code, ls_loc_code, ls_lot_no, ls_lot_sl, valueXmlString, conn);*/
				}

				if( currentColumn.trim().equalsIgnoreCase( "lot_no" ) )
				{
					ls_item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					ls_site_code = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", currFormDataDom));
					ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", currFormDataDom));
					ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", currFormDataDom));

					valueXmlString = setValue(ls_item_code, ls_site_code, ls_loc_code, ls_lot_no, ls_lot_sl, valueXmlString, conn);
				}
				
				if( currentColumn.trim().equalsIgnoreCase( "lot_sl" ) )
				{
					ls_lot_sl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl", currFormDataDom));
					
					if(ls_lot_sl.length() == 0)
					{
						valueXmlString.append( "<qty_per_art protect = '1'><![CDATA[" ).append( "" ).append( "]]></qty_per_art>\r\n" ); 
					}
					else
					{
						valueXmlString.append( "<qty_per_art protect = '0'><![CDATA[" ).append( "" ).append( "]]></qty_per_art>\r\n" ); 
					}

					ls_item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					ls_site_code = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", currFormDataDom));
					ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", currFormDataDom));
					
					valueXmlString = setValue(ls_item_code, ls_site_code, ls_loc_code, ls_lot_no, ls_lot_sl, valueXmlString, conn);
				}
				if( currentColumn.trim().equalsIgnoreCase( "mfg_date" ) )
				{
					System.out.println("------------------ inside case 1 itemchange for mfg_date --------------- ");
					
					ls_item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					ld_mfg_date = checkNullAndTrim(genericUtility.getColumnValue("mfg_date", currFormDataDom));
					
					if(ld_mfg_date.length() > 0)
					{
						sql = "SELECT TRACK_SHELF_LIFE, SHELF_LIFE FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_item_code);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_track_shelf_life = checkNullAndTrim(rs.getString("TRACK_SHELF_LIFE"));
							li_shelf_life = checkNullAndTrim(rs.getString("SHELF_LIFE"));
						}
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
						
						if(ls_track_shelf_life.equalsIgnoreCase("Y"))
						{
							ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", currFormDataDom));
							
							sql = "SELECT SHELF_LIFE FROM ITEM_LOT_PACKSIZE WHERE ITEM_CODE = ? AND ? BETWEEN LOT_NO__FROM AND LOT_NO__TO ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_item_code);
							pstmt.setString(2, ls_lot_no);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								li_lot_shelf_life = checkNullAndTrim(rs.getString("SHELF_LIFE"));
							}
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
														
							if(li_lot_shelf_life.length() != 0 &&  !li_lot_shelf_life.equalsIgnoreCase("0"))
							{
								li_shelf_life = li_lot_shelf_life;
							}
							
							if(li_shelf_life.length() == 0)
							{
								ld_exp_date = "";
							}
							else
							{					
								if(li_shelf_life.length() == 0)
								{
									li_shelf_life = "0.0";
								}
								
								mfgDateTs = Timestamp.valueOf(genericUtility.getValidDateString(ld_mfg_date, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
								ld_exp_date1 = discommon.CalcExpiry(mfgDateTs, Double.parseDouble(li_shelf_life));
								ld_exp_date = (ld_exp_date1 == null) ? "" :  sdf.format(ld_exp_date1);
							}
						}
						else
						{
							ld_exp_date = "";
						}

						valueXmlString.append( "<exp_date><![CDATA[" ).append( ld_exp_date ).append( "]]></exp_date>\r\n" ); 						
					}
				}
				if( currentColumn.trim().equalsIgnoreCase( "qty_per_art" ) )
				{
					System.out.println("---------------- Inside qty_per_art ----------------- ");
					
					ls_item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					ls_site_code = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					ls_loc_code = checkNullAndTrim(genericUtility.getColumnValue("loc_code", currFormDataDom));
					ls_lot_no = checkNull(genericUtility.getColumnValue("lot_no", currFormDataDom));
					ls_lot_sl = checkNull(genericUtility.getColumnValue("lot_sl", currFormDataDom));
					
					lc_qtyperart = checkNullAndTrim(genericUtility.getColumnValue("qty_per_art", currFormDataDom));
					
					sql = "SELECT QUANTITY FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_item_code);
					pstmt.setString(2, ls_site_code);
					//Changed by wasim on 18-APR-2017 to set proper varaible in pstmt
					//pstmt.setString(3, ls_loccode);
					pstmt.setString(3, ls_loc_code);
					pstmt.setString(4, ls_lot_no);
					pstmt.setString(5, ls_lot_sl);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lc_quantity = rs.getInt("QUANTITY");
					}
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
					
					//Changed by wasim on 18-APR-2017 as per NVO
					//ll_modqty = lc_quantity - (Integer.parseInt(lc_qtyperart) * (lc_quantity/Integer.parseInt(lc_qtyperart)));
					//lc_divqty = lc_quantity / Integer.parseInt(lc_qtyperart);
					ll_modqty = lc_quantity - (Double.parseDouble(lc_qtyperart) * (int) (lc_quantity/Double.parseDouble(lc_qtyperart)));
					lc_divqty = lc_quantity / Double.parseDouble(lc_qtyperart);
					
					System.out.println("ll_modqty ========= >>"+ll_modqty);
					System.out.println("lc_divqty ========= >>"+lc_divqty);
					
					//Changed by wasim on 18-APR-2017 for getting no art as per NVO
					//if(lc_divqty > 0)
					if(ll_modqty > 0)
					{	
						//Changed by wasim on 18-APR-2017 as per NVO
						//ll_noart = lc_divqty;
						ll_noart = (int) lc_divqty;
						ll_noart++;
					}
					else
					{
						//Changed by wasim on 18-APR-2017 as per NVO
						//ll_noart = lc_divqty;
						ll_noart = (int) lc_divqty;
					}
					
					valueXmlString.append( "<no_art><![CDATA[" ).append( ll_noart ).append( "]]></no_art>\r\n" ); 						
				}
				//Changed by wasim on 18-APR-2017 for calculating net weight as issue given by QC [START] 
				if(currentColumn.trim().equalsIgnoreCase("gross_weight") || currentColumn.trim().equalsIgnoreCase("tare_weight") )
				{
					double grossWeight = 0, tareWeight = 0, netWeight = 0;
					
					grossWeight = checkDoubleNull(genericUtility.getColumnValue("gross_weight", currFormDataDom));
					tareWeight = checkDoubleNull(genericUtility.getColumnValue("tare_weight", currFormDataDom));
					netWeight = grossWeight - tareWeight;
					
					System.out.println("Gross["+grossWeight+"] Tare["+tareWeight+"] Net["+netWeight+"]");

					valueXmlString.append("<net_weight protect = '1'><![CDATA[" ).append( netWeight ).append( "]]></net_weight>\r\n" ); 
				}
				//Changed by wasim on 18-APR-2017 for calculating net weight as issue given by QC [END] 
				
				valueXmlString.append( "</Detail1>\r\n" );
			} //Case 1. End
			break;
			}//End of switch block
			valueXmlString.append( "</Root>\r\n" );	 
		}
		catch (Exception e)
		{				
			e.printStackTrace();			

		}
		finally
		{	
			try
			{				
				if(rs!=null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println( "valueXmlString.toString()>>>>>>>::"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String checkNull(String input)	
	{
		if ( input == null )
		{
			input = "";
		}
		return input.trim();
	}

	private String checkNullAndTrim(String inputVal)
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

	
	public String chkQcOrd(String ls_item_code, String ls_site_code, String ls_loc_code, String ls_lot_no, String ls_lot_sl, Connection conn) throws ITMException
	{
		String sql = "", errCode = "";
		int cnt = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			System.out.println("-------------- Inside chkQcOrd ----------------");
			if(ls_item_code.length() == 0)
			{
				ls_item_code = " ";
			}
			if(ls_site_code.length() == 0)
			{
				ls_site_code = " ";
			}
			
			if(ls_loc_code.length() == 0)
			{
				ls_loc_code = " ";
			}
			if(ls_lot_no.length() == 0)
			{
				ls_lot_no = " ";
			}
			if(ls_lot_sl.length() == 0)
			{
				ls_lot_sl = " ";
			}
			if(ls_item_code.length() > 0 && ls_site_code.length() > 0 && ls_loc_code.length() > 0 && ls_lot_no.length() > 0)
			{
				sql = "SELECT COUNT(*) FROM QC_ORDER WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND STATUS = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_item_code);
				pstmt.setString(2, ls_site_code);
				pstmt.setString(3, ls_loc_code);
				pstmt.setString(4, ls_lot_no);
				pstmt.setString(5, "U");				
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
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
				
				if(cnt > 0 )
				{
					errCode = "VTQCPND";
				}
			}
			
		}
		catch (Exception e)
		{				
			e.printStackTrace();			

		}
		finally
		{	
			try
			{				
				if(rs!=null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println(" Return value from chkQcOrd ==========>>["+errCode+"]");
		return errCode;
	}
	
	public StringBuffer setValue(String ls_item_code, String ls_site_code, String ls_loc_code, String ls_lot_no, String ls_lot_sl, StringBuffer valueXmlString, Connection conn) throws ITMException
	{
		System.out.println("------------------ Inside setValue------------------");
		String sql = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String ls_site_code__mfg = "", lc_potency_perc = "",  ls_supp_code_mfg = "", ls_dim = "", lc_conv = "", lc_gross_weight = "", 
			lc_tare_weight = "", lc_net_weight = "", ls_consider_allocate = "", ls_pack_code = "", lc_qtyperart = "", ll_noart = "",
			ld_retest = "", ld_mfg = "", ld_exp = "";
		Date ld_mfg_date = null, ld_exp_date = null, ld_retest_date = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			if(ls_item_code.length() == 0)
			{
				ls_item_code = " ";
			}
			else if(ls_site_code.length() == 0)
			{
				ls_site_code = " ";
			}
			else if(ls_loc_code.length() == 0)
			{
				ls_loc_code = " ";
			}
			else if(ls_lot_no.length() == 0)
			{
				ls_lot_no = " ";
			}
			
			if(ls_lot_sl.length() == 0 || ls_lot_sl.equalsIgnoreCase(" "))
			{
				sql = " SELECT MFG_DATE, EXP_DATE, SITE_CODE__MFG, (CASE WHEN POTENCY_PERC IS NULL THEN 0 ELSE POTENCY_PERC END) AS POTENCY_PERC, " 
					+ " RETEST_DATE, SUPP_CODE__MFG, DIMENSION, CONV__QTY_STDUOM, GROSS_WEIGHT, TARE_WEIGHT,NET_WEIGHT, " 
					+ " (CASE WHEN CONSIDER_ALLOCATE IS NULL THEN 'Y' ELSE CONSIDER_ALLOCATE END) AS CONSIDER_ALLOCATE, PACK_CODE FROM STOCK " 
					+ " WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_item_code);
				pstmt.setString(2, ls_site_code);
				pstmt.setString(3, ls_loc_code);
				pstmt.setString(4, ls_lot_no);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ld_mfg_date = rs.getDate("MFG_DATE");
					ld_exp_date = rs.getDate("EXP_DATE");
					ls_site_code__mfg = checkNullAndTrim(rs.getString("SITE_CODE__MFG"));
					lc_potency_perc = checkNullAndTrim(rs.getString("POTENCY_PERC"));
					ld_retest_date = rs.getDate("RETEST_DATE");
					ls_supp_code_mfg = checkNullAndTrim(rs.getString("SUPP_CODE__MFG"));
					ls_dim = checkNullAndTrim(rs.getString("DIMENSION"));
					lc_conv = checkNullAndTrim(rs.getString("CONV__QTY_STDUOM"));
					lc_gross_weight = checkNullAndTrim(rs.getString("GROSS_WEIGHT"));
					lc_tare_weight = checkNullAndTrim(rs.getString("TARE_WEIGHT"));
					lc_net_weight = checkNullAndTrim(rs.getString("NET_WEIGHT"));
					ls_consider_allocate = checkNullAndTrim(rs.getString("CONSIDER_ALLOCATE"));
					ls_pack_code = checkNullAndTrim(rs.getString("PACK_CODE"));
					
					
				}
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
				ld_mfg = (ld_mfg_date == null) ? "" :  sdf.format(ld_mfg_date);
				ld_exp = (ld_exp_date == null) ? "" :  sdf.format(ld_exp_date);
				ld_retest = (ld_retest_date == null) ? "" :  sdf.format(ld_retest_date);
				
				valueXmlString.append( "<mfg_date__o><![CDATA[" ).append( ld_mfg ).append( "]]></mfg_date__o>\r\n" );
				valueXmlString.append( "<mfg_date><![CDATA[" ).append( ld_mfg ).append( "]]></mfg_date>\r\n" );
				valueXmlString.append( "<exp_date__o><![CDATA[" ).append( ld_exp ).append( "]]></exp_date__o>\r\n" );
				valueXmlString.append( "<exp_date><![CDATA[" ).append( ld_exp ).append( "]]></exp_date>\r\n" );
				valueXmlString.append( "<site_code__mfg__o><![CDATA[" ).append( ls_site_code__mfg ).append( "]]></site_code__mfg__o>\r\n" );
				valueXmlString.append( "<site_code__mfg><![CDATA[" ).append( ls_site_code__mfg ).append( "]]></site_code__mfg>\r\n" );
				valueXmlString.append( "<potency_perc_o><![CDATA[" ).append( lc_potency_perc ).append( "]]></potency_perc_o>\r\n" );
				valueXmlString.append( "<potency_perc><![CDATA[" ).append( lc_potency_perc ).append( "]]></potency_perc>\r\n" );
				valueXmlString.append( "<retest_date_o><![CDATA[" ).append(ld_retest).append( "]]></retest_date_o>\r\n" );
				valueXmlString.append( "<retest_date><![CDATA[" ).append(ld_retest).append( "]]></retest_date>\r\n" );
				valueXmlString.append( "<supp_code__mfg__o><![CDATA[" ).append( ls_supp_code_mfg ).append( "]]></supp_code__mfg__o>\r\n" );
				valueXmlString.append( "<supp_code__mfg><![CDATA[" ).append( ls_supp_code_mfg ).append( "]]></supp_code__mfg>\r\n" );
				valueXmlString.append( "<dimension__o><![CDATA[" ).append( ls_dim ).append( "]]></dimension__o>\r\n" );
				valueXmlString.append( "<dimension><![CDATA[" ).append( ls_dim  ).append( "]]></dimension>\r\n" );
				valueXmlString.append( "<conv__qty_stduom_o><![CDATA[" ).append( lc_conv ).append( "]]></conv__qty_stduom_o>\r\n" );
				valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append( lc_conv ).append( "]]></conv__qty_stduom>\r\n" );
				valueXmlString.append( "<gross_weight><![CDATA[" ).append( lc_gross_weight ).append( "]]></gross_weight>\r\n" );
				valueXmlString.append( "<tare_weight><![CDATA[" ).append( lc_tare_weight ).append( "]]></tare_weight>\r\n" );
				valueXmlString.append( "<net_weight><![CDATA[" ).append( lc_net_weight ).append( "]]></net_weight>\r\n" );
				valueXmlString.append( "<consider_allocate__o><![CDATA[" ).append( ls_consider_allocate ).append( "]]></consider_allocate__o>\r\n" );
				valueXmlString.append( "<consider_allocate><![CDATA[" ).append( ls_consider_allocate ).append( "]]></consider_allocate>\r\n" );
				valueXmlString.append( "<pack_code_o><![CDATA[" ).append( ls_pack_code ).append( "]]></pack_code_o>\r\n" );
				valueXmlString.append( "<pack_code><![CDATA[" ).append( ls_pack_code ).append( "]]></pack_code>\r\n" );

				valueXmlString.append( "<qty_per_art><![CDATA[" ).append( lc_qtyperart ).append( "]]></qty_per_art>\r\n" );
				valueXmlString.append( "<qty_per_art_o><![CDATA[" ).append( lc_qtyperart ).append( "]]></qty_per_art_o>\r\n" );
				valueXmlString.append( "<no_art><![CDATA[" ).append( ll_noart ).append( "]]></no_art>\r\n" );
				valueXmlString.append( "<no_art_o><![CDATA[" ).append( ll_noart ).append( "]]></no_art_o>\r\n" );

			}
			else
			{		
				sql = " SELECT MFG_DATE, EXP_DATE, SITE_CODE__MFG, (CASE WHEN POTENCY_PERC IS NULL THEN 0 ELSE POTENCY_PERC END) AS POTENCY_PERC, " 
					+ " RETEST_DATE, SUPP_CODE__MFG, DIMENSION, CONV__QTY_STDUOM, GROSS_WEIGHT, TARE_WEIGHT, NET_WEIGHT, " 
					+ " (CASE WHEN CONSIDER_ALLOCATE IS NULL THEN 'Y' ELSE CONSIDER_ALLOCATE END) AS CONSIDER_ALLOCATE, QTY_PER_ART, NO_ART, PACK_CODE " 
					+ " FROM STOCK WHERE ITEM_CODE	= ? AND	SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_item_code);
				pstmt.setString(2, ls_site_code);
				pstmt.setString(3, ls_loc_code);
				pstmt.setString(4, ls_lot_no);
				pstmt.setString(5, ls_lot_sl);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ld_mfg_date = rs.getDate("MFG_DATE");
					ld_exp_date = rs.getDate("EXP_DATE");
					ls_site_code__mfg = checkNullAndTrim(rs.getString("SITE_CODE__MFG"));
					lc_potency_perc = checkNullAndTrim(rs.getString("POTENCY_PERC"));
					ld_retest_date = rs.getDate("RETEST_DATE");
					ls_supp_code_mfg = checkNullAndTrim(rs.getString("SUPP_CODE__MFG"));
					ls_dim = checkNullAndTrim(rs.getString("DIMENSION"));
					lc_conv = checkNullAndTrim(rs.getString("CONV__QTY_STDUOM"));
					lc_gross_weight = checkNullAndTrim(rs.getString("GROSS_WEIGHT"));
					lc_tare_weight = checkNullAndTrim(rs.getString("TARE_WEIGHT"));
					lc_net_weight = checkNullAndTrim(rs.getString("NET_WEIGHT"));
					ls_consider_allocate = checkNullAndTrim(rs.getString("CONSIDER_ALLOCATE"));
					lc_qtyperart = checkNullAndTrim(rs.getString("QTY_PER_ART"));
					ll_noart = checkNullAndTrim(rs.getString("NO_ART"));
					ls_pack_code = checkNullAndTrim(rs.getString("PACK_CODE"));
				}
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

				ld_mfg = (ld_mfg_date == null) ? "" :  sdf.format(ld_mfg_date);
				ld_exp = (ld_exp_date == null) ? "" :  sdf.format(ld_exp_date);
				ld_retest = (ld_retest_date == null) ? "" :  sdf.format(ld_retest_date);
				
				valueXmlString.append( "<mfg_date__o><![CDATA[" ).append( ld_mfg ).append( "]]></mfg_date__o>\r\n" );
				valueXmlString.append( "<mfg_date><![CDATA[" ).append( ld_mfg ).append( "]]></mfg_date>\r\n" );
				valueXmlString.append( "<exp_date__o><![CDATA[" ).append( ld_exp ).append( "]]></exp_date__o>\r\n" );
				valueXmlString.append( "<exp_date><![CDATA[" ).append( ld_exp ).append( "]]></exp_date>\r\n" );
				valueXmlString.append( "<site_code__mfg__o><![CDATA[" ).append( ls_site_code__mfg ).append( "]]></site_code__mfg__o>\r\n" );
				valueXmlString.append( "<site_code__mfg><![CDATA[" ).append( ls_site_code__mfg ).append( "]]></site_code__mfg>\r\n" );
				valueXmlString.append( "<potency_perc_o><![CDATA[" ).append( lc_potency_perc ).append( "]]></potency_perc_o>\r\n" );
				valueXmlString.append( "<potency_perc><![CDATA[" ).append( lc_potency_perc ).append( "]]></potency_perc>\r\n" );
				valueXmlString.append( "<retest_date_o><![CDATA[" ).append(ld_retest).append( "]]></retest_date_o>\r\n" );
				valueXmlString.append( "<retest_date><![CDATA[" ).append(ld_retest).append( "]]></retest_date>\r\n" );
				valueXmlString.append( "<supp_code__mfg__o><![CDATA[" ).append( ls_supp_code_mfg ).append( "]]></supp_code__mfg__o>\r\n" );
				valueXmlString.append( "<supp_code__mfg><![CDATA[" ).append( ls_supp_code_mfg ).append( "]]></supp_code__mfg>\r\n" );
				valueXmlString.append( "<dimension__o><![CDATA[" ).append( ls_dim ).append( "]]></dimension__o>\r\n" );
				valueXmlString.append( "<dimension><![CDATA[" ).append( ls_dim  ).append( "]]></dimension>\r\n" );
				valueXmlString.append( "<conv__qty_stduom_o><![CDATA[" ).append( lc_conv ).append( "]]></conv__qty_stduom_o>\r\n" );
				valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append( lc_conv ).append( "]]></conv__qty_stduom>\r\n" );
				valueXmlString.append( "<gross_weight><![CDATA[" ).append( lc_gross_weight ).append( "]]></gross_weight>\r\n" );
				valueXmlString.append( "<tare_weight><![CDATA[" ).append( lc_tare_weight ).append( "]]></tare_weight>\r\n" );
				valueXmlString.append( "<net_weight><![CDATA[" ).append( lc_net_weight ).append( "]]></net_weight>\r\n" );
				valueXmlString.append( "<consider_allocate__o><![CDATA[" ).append( ls_consider_allocate ).append( "]]></consider_allocate__o>\r\n" );
				valueXmlString.append( "<consider_allocate><![CDATA[" ).append( ls_consider_allocate ).append( "]]></consider_allocate>\r\n" );
				valueXmlString.append( "<pack_code_o><![CDATA[" ).append( ls_pack_code ).append( "]]></pack_code_o>\r\n" );
				valueXmlString.append( "<qty_per_art><![CDATA[" ).append( lc_qtyperart ).append( "]]></qty_per_art>\r\n" );
				valueXmlString.append( "<qty_per_art_o><![CDATA[" ).append( lc_qtyperart ).append( "]]></qty_per_art_o>\r\n" );
				valueXmlString.append( "<no_art><![CDATA[" ).append( ll_noart ).append( "]]></no_art>\r\n" );
				valueXmlString.append( "<no_art_o><![CDATA[" ).append( ll_noart ).append( "]]></no_art_o>\r\n" );
				valueXmlString.append( "<pack_code_o><![CDATA[" ).append( ls_pack_code ).append( "]]></pack_code_o>\r\n" );
				valueXmlString.append( "<pack_code><![CDATA[" ).append( ls_pack_code ).append( "]]></pack_code>\r\n" );
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{				
				if(rs!=null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return valueXmlString;
		
	}
	
	//Changed by wasim on 18-APR-17 to get current date in Timestamp format [START]
	private java.sql.Timestamp getCurrtDate() throws RemoteException,ITMException 
	{
		java.sql.Timestamp currDate = null;
		try 
		{
			Object date = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString()+ " 00:00:00.0");
		} 
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (currDate);
	}
	private double checkDoubleNull(String str)
	{
		if (str == null || str.trim().length() == 0)
		{
			return 0.0;
		} 
		else
		{
			return Double.parseDouble(str);
		}
	}
   //Changed by wasim on 18-APR-17 to get current date in Timestamp format [END]
}
