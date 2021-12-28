package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

@javax.ejb.Stateless
public class StockTransferEJB extends ValidatorEJB implements StockTransferEJBRemote, StockTransferEJBLocal  
{
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = "";

		try
		{			
			if(currFrmXmlStr != null && currFrmXmlStr.trim().length()!=0)
			{
				currDom = parseString(currFrmXmlStr); 
			}
			if(hdrFrmXmlStr != null && hdrFrmXmlStr.trim().length()!=0)
			{
				hdrDom = parseString(hdrFrmXmlStr); 
			}
			if(allFrmXmlStr != null && allFrmXmlStr.trim().length()!=0)
			{
				allDom = parseString(allFrmXmlStr);
			}
			errString = wfValData(currDom,hdrDom,allDom,objContext,editFlag,xtraParams);
		}//end of try
		catch(Exception e)
		{
			System.out.println("Exception : [StockTransferIC][wfValData(String currFrmXmlStr)] : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return (errString); 
	}
	public String wfValData(Document currDom, Document hdrDom, Document allDom, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 	
	{
		String errString = "";
		String columnValue = "";
		String sql = "";	
		String refSer = "";
		String refId = "";
		String itemCode = "";
		String itemSer = "";
		String siteCode = "";
		String lotNo = "";
		String lpnNo = "";
		String tranId = "";
		String lineNo = "";
		String ls_iss_criteria = "";
		String ls_Val = "";
		String ls_loccode = "";
		String ls_item_code = "";
		String ls_available_fr = "";
		String ls_available_to = "";
		String ls_invstat_to = "";
		String ls_faci_loc_code = "";
		String ls_faci_site_code = "";
		String ls_loc_code__fr = "";
		String ls_lot_no__fr = "";
		String ls_lot_sl__fr = "";
		String ls_loc_code__to = "";
		String ls_lot_no__to = "";
		String ls_lot_sl__to = "";
		String ls_loc_group_fr = "";
		String ls_loc_group_to = "";
		String ls_disparminvstat = "";
		String acctCodeDr = "";
		String acctCodeCr = "";
		String cctrCodeDr = "";
		String cctrCodeCr = "";
		String cctrCodeInv = "";
		String active = "";
		String childNodeName = "";
		String locCode = "";
		int noOfChilds = 0;
		int noOfParent = 0;                            

		double lc_Num = 0d,lc_Num2 = 0d,lc_old_qty = 0d,lc_stk_qty = 0d,shipperSize = 0;        
		double qty = 0d;

		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String errorType = "",errCode = "";
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		DistCommon distComm = new DistCommon();
		FinCommon finCommon=new FinCommon();
		try
		{
			int currentFormNo = 0, cnt = 0;		
			conn = getConnection();
			Node childNode =null;
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			if ( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}

			NodeList parentList = currDom.getElementsByTagName( "Detail" + currentFormNo );
			NodeList childList = null;	
			noOfParent = parentList.getLength();

			switch(currentFormNo)
			{
			case 1 :
			{
				System.out.println("------------Case 1 Validation------------");
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++)
				{	
					childNode = childList.item( ctr );
					if( childNode.getNodeType() != Node.ELEMENT_NODE )
					{
						continue;
					}
					childNodeName = childNode.getNodeName();						
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
					if ( "ref_id__for".equalsIgnoreCase( childNodeName ) )	// Transfer No:               
					{
						if ( childNode.getFirstChild() == null )
						{
							errList.add( "VTREFSER2" );
							errFields.add( childNodeName.toLowerCase() );
						}

						refId = genericUtility.getColumnValue( "ref_id__for", currDom );
						refSer = genericUtility.getColumnValue( "ref_ser__for", currDom );
						siteCode = genericUtility.getColumnValue( "site_code", currDom );
						sql = "SELECT COUNT(*) AS COUNT FROM invtrace WHERE site_code = ? and REF_SER = ? and ref_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, refSer);
						pstmt.setString(3, refId);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt > 0)
						{
							errList.add( "VTREFSER1" );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					else if ( "ref_ser__for".equalsIgnoreCase( childNodeName ) )	// Ref Series:               
					{							
						refSer = genericUtility.getColumnValue( "ref_ser__for", currDom );
						sql = "SELECT COUNT(*) AS COUNT FROM REFSER WHERE REF_SER = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, refSer);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0)
						{
							errList.add( "VTREFSER1" );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
					else if ( "site_code".equalsIgnoreCase( childNodeName ) )
					{
						siteCode = genericUtility.getColumnValue( "site_code", currDom );
						sql = "SELECT COUNT(*) AS COUNT FROM SITE WHERE SITE_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0)
						{
							errList.add( "VMSITE" );
							errFields.add( childNodeName.toLowerCase() );
						}
					}
				}// for loop end
			}//case 1 end
			break;
			case 2 :
			{
				System.out.println("Case 2 Validation");
				childList = parentList.item( 0 ).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++)
				{	
					childNode = childList.item( ctr );
					if( childNode.getNodeType() != Node.ELEMENT_NODE )
					{
						continue;
					}
					childNodeName = childNode.getNodeName();						
					if ( childNode != null && childNode.getFirstChild() != null )
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");

					if ( "item_code".equalsIgnoreCase( childNodeName ) )
					{	
						itemCode = genericUtility.getColumnValue( "item_code", currDom );
						sql = "SELECT COUNT(*) AS COUNT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0)
						{
							errList.add( "VMITEM" );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							itemSer = genericUtility.getColumnValue( "item_ser", hdrDom );
							sql = "SELECT COUNT(*) AS COUNT FROM ITEM WHERE ITEM_CODE = ? AND ITEM_SER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("COUNT");
							}							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt == 0)
							{
								errList.add( "VTITEM2" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
					}
					else if ( "quantity".equalsIgnoreCase( childNodeName ) )
					{	
						lc_Num = Double.parseDouble(genericUtility.getColumnValue( "quantity", currDom ));
						System.out.println("Input quantity["+lc_Num+"]");
						
						if (lc_Num <= 0)
						{
							errList.add( "VTQTY" );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							itemCode = genericUtility.getColumnValue( "item_code", currDom );
							siteCode = genericUtility.getColumnValue( "site_code", hdrDom );
							locCode = genericUtility.getColumnValue( "loc_code__fr", currDom );
							lotNo = genericUtility.getColumnValue( "lot_no__fr", currDom );
							lpnNo = genericUtility.getColumnValue( "lot_sl__fr", currDom );
							tranId = genericUtility.getColumnValue( "tran_id", currDom );
							lineNo = genericUtility.getColumnValue( "line_no", currDom );
							if(lotNo==null)  // Modified by Rohini T on 20/04/2021[Start] for setting of lotno,lpnNo as space
			                {
								lotNo= " ";    
			                }               
							if(lpnNo==null)  
			                {
								lpnNo= " ";
			                }  // Modified by Rohini T on 20/04/2021[End] for setting of lotno,lpnNo as space
							//Changes by mayur on 04-June-2018----start
							//Commented by sarita as refSer coming with space and if condition ("S-REQ".equalsIgnoreCase(refSer)) not getting satisfied on 14 JUN 18 [START]
							//refSer = genericUtility.getColumnValue( "ref_ser__for", hdrDom );
							refSer = checkNullAndTrim(genericUtility.getColumnValue( "ref_ser__for", hdrDom ));
							//Commented by sarita as refSer coming with space and if condition ("S-REQ".equalsIgnoreCase(refSer)) not getting satisfied on 14 JUN 18 [END]
							System.out.println("Reference series::::::::["+refSer+"]");
     
							refId = genericUtility.getColumnValue("ref_id__for", hdrDom);
							System.out.println("Reference id::::::::["+refId+"]"); 

							if(tranId == null || tranId.trim().length() == 0)
							{
								tranId = "@@@";
							}
							
							if("S-REQ".equalsIgnoreCase(refSer))
							{
							sql = "SELECT sum(quantity) As qty from ser_req_item where req_id = ? AND item_code = ? ";
							System.out.println("SQL ::"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,refId);
							pstmt.setString(2,itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								qty = rs.getDouble("qty");
								System.out.println("Service order item quantity:["+qty+"]");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(lc_Num > qty)
							{
								System.out.println("If input quantity > service order quantity["+lc_Num+">"+qty+"]");
								errList.add( "INVQTY1" );
								errFields.add( childNodeName.toLowerCase() );
							}
							
							} //end of if 
							//Changes by mayur on 04-June-2018----end
							else {
							sql = "SELECT quantity FROM stock_transfer_det WHERE tran_id = ? AND line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							pstmt.setString(2, lineNo);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lc_old_qty = rs.getDouble("quantity");
							}							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sql = "Select quantity - case when alloc_qty is null then 0 else alloc_qty end  - case when hold_qty is null then 0 " +
							" else hold_qty end , quantity FROM stock WHERE item_code = ? AND site_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, siteCode);
							pstmt.setString(3, locCode);
							pstmt.setString(4, lotNo);
							pstmt.setString(5, lpnNo);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lc_Num2 = rs.getDouble(1);
								lc_stk_qty = rs.getDouble(1);
							}							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if((lc_Num2 + lc_old_qty) < lc_Num)
							{
								errList.add( "VXSTK2" );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sql = "select (case when iss_criteria is null then 'I' else  iss_criteria end) from item where item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_iss_criteria = rs.getString(1);
								}							
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								// 31-jan-2019 manoharan invalid syntax corrected
								//if (ls_iss_criteria == "W" && lc_Num != lc_stk_qty )
									if ("W".equals(ls_iss_criteria) && lc_Num != lc_stk_qty )
								{
									errList.add( "VTSTK02" );
									errFields.add( childNodeName.toLowerCase() );
								}
							  }
						    }
						}
							
					}
					else if ( "loc_code__fr".equalsIgnoreCase( childNodeName ) )
					{
						ls_Val = genericUtility.getColumnValue( "loc_code__fr", currDom );		
						ls_loccode = genericUtility.getColumnValue( "loc_code__to", currDom );
						ls_item_code = genericUtility.getColumnValue( "item_code", currDom );

						sql = "Select count(*) as count From location Where loc_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_Val);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0) 
						{
							errList.add( "VMLOC" );
							errFields.add( childNodeName.toLowerCase() );
						}

						sql = "Select count(*) as count From location Where loc_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_loccode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0) 
						{
							errList.add( "VMLOC1" );
							errFields.add( childNodeName.toLowerCase() );
						}

						sql = "select a.available from invstat a, location b where  a.inv_stat = b.inv_stat and b.loc_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_Val);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ls_available_fr = rs.getString(1);
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select a.available, a.inv_stat, b.facility_code from invstat a, location b where  a.inv_stat = b.inv_stat and b.loc_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_loccode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ls_available_to = rs.getString(1);
							ls_invstat_to = rs.getString(2);
							ls_faci_loc_code = rs.getString(3);
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						ls_disparminvstat = distComm.getDisparams("999999","STK_XFRX_INVSTAT",conn);
						
						if(ls_disparminvstat == null && ls_disparminvstat.trim().length()==0)
						{
							ls_disparminvstat = " ";
						}
						// 31-jan-2019 manoharan invalid syntax corrected
						if (ls_available_fr.trim().equals(ls_available_to.trim()))
						{
						}
						//if (ls_available_fr == "Y") 
						else if ("Y".equals(ls_available_fr)) 
						{
							// 31-jan-2019 manoharan invalid syntax corrected
							//if ((ls_invstat_to.trim()) != (ls_disparminvstat.trim())) 
							if (!ls_invstat_to.trim().equals(ls_disparminvstat.trim())) 
							{
								errList.add( "VMLOCSTAT" );
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						// 31-jan-2019 manoharan invalid syntax corrected
						//else if (ls_available_fr == "N") 
						else if ("N".equals(ls_available_fr)) 
						{
							errList.add( "VMLOCSTAT" );
							errFields.add( childNodeName.toLowerCase() );
						}
						// 31-jan-2019 manoharan invalid syntax corrected
						//if ((ls_invstat_to.trim()) != (ls_disparminvstat.trim())) 
						if (!ls_invstat_to.trim().equals(ls_disparminvstat.trim())) 
						{
							errCode = getLocType(ls_item_code,ls_loccode,conn) ;
							//if(errCode == null && errCode.trim().length() == 0)
							//{
								ls_loc_code__fr 	= 	genericUtility.getColumnValue( "loc_code__fr", currDom );
								ls_lot_no__fr 		= 	genericUtility.getColumnValue( "lot_no__fr", currDom );		
								ls_lot_sl__fr 		= 	genericUtility.getColumnValue( "lot_sl__fr", currDom );				
								ls_loc_code__to 	= 	genericUtility.getColumnValue( "loc_code__to", currDom );
								ls_lot_no__to 		= 	genericUtility.getColumnValue( "lot_no__to", currDom );			
								ls_lot_sl__to 		= 	genericUtility.getColumnValue( "lot_sl__to", currDom );	
								System.out.println("manish ls_Val"+ls_Val+"ls_loccode"+ls_loccode+"ls_lot_no__fr"+ls_lot_no__fr+"ls_lot_no__to"+ls_lot_no__to+"ls_lot_sl__fr"+ls_lot_sl__fr+"ls_lot_sl__to"+ls_lot_sl__to);
								// 31-jan-2019 manoharan invalid syntax corrected
								//if ((ls_Val.trim() == ls_loccode.trim()) &&	(ls_lot_no__fr.trim() == ls_lot_no__to.trim()) && (ls_lot_sl__fr.trim() == ls_lot_sl__to.trim())) 
								if (ls_Val.trim().equals(ls_loccode.trim()) && ls_lot_no__fr.trim().equals(ls_lot_no__to.trim()) && ls_lot_sl__fr.trim().equals(ls_lot_sl__to.trim())) 
								{
									errList.add( "VTITRF" );
									errFields.add( childNodeName.toLowerCase() );
								}
								sql = "Select loc_group From location Where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_loc_code__fr);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_loc_group_fr = rs.getString("loc_group");
								}
								else
								{
									errList.add( "VMLOC" );
									errFields.add( childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(ls_loc_group_fr == null && ls_loc_group_fr.trim().length() == 0) 
								{
									ls_loc_group_fr = " ";
								}

								sql = "Select loc_group From location Where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_loc_code__to);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_loc_group_to = rs.getString("loc_group");
								}
								else
								{
									errList.add( "VMLOC" );
									errFields.add( childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(ls_loc_group_to == null && ls_loc_group_to.trim().length() == 0) 
								{
									ls_loc_group_to = " ";
								}
								// 31-jan-2019 manoharan invalid syntax corrected
								//if(ls_loc_group_fr.trim() != ls_loc_group_to.trim())
								if(!ls_loc_group_fr.trim().equals(ls_loc_group_to.trim()))
								{
									errList.add( "VTLOCGRP2" );
									errFields.add( childNodeName.toLowerCase());
								}
							//}

						}
					}
					else if ( "loc_code__to".equalsIgnoreCase( childNodeName ) )
					{
						errCode= getLocType(ls_item_code,ls_Val,conn);
						siteCode = genericUtility.getColumnValue( "site_code", currDom );

						if ((errCode == null) || (errCode.trim().length()==0))
						{
							sql = "select facility_code from site where site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_faci_site_code = rs.getString("facility_code");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (((ls_faci_loc_code != null) && (ls_faci_loc_code.trim().length()) > 0 ) && ((ls_faci_site_code != null)  && (ls_faci_loc_code.trim().length()) > 0 ) ) 
							{
								// 31-jan-2019 manoharan invalid syntax corrected
								if (!ls_faci_loc_code.trim().equals(ls_faci_site_code.trim()) )
								{
									errList.add( "VMFACI2" );
									errFields.add( childNodeName.toLowerCase());
								}
							}  	
						}
					}
					else if ( "acct_code__dr".equalsIgnoreCase( childNodeName ) ) 
					{
						acctCodeDr = genericUtility.getColumnValue( "acct_code__dr", currDom );

						if(acctCodeDr != null && acctCodeDr.trim().length()>0)
						{
							sql = "Select count(*) as count From accounts Where acct_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeDr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt != 0)
							{
								sql = "Select active From accounts Where acct_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, acctCodeDr);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									active = rs.getString("active");
									System.out.println("active@@["+active+"]");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								//Changes by mayur on 05-June-2018
								//if(active != "Y")
								if(!"Y".equalsIgnoreCase(active))
								{
									errList.add( "VMACCTA" );
									errFields.add( childNodeName.toLowerCase());
								}
							}
							else
							{
								errList.add( "VMACCT1" );
								errFields.add( childNodeName.toLowerCase());
							}
						}
					}
					else if ( "acct_code__cr".equalsIgnoreCase( childNodeName ) ) 
					{
						acctCodeCr = genericUtility.getColumnValue( "acct_code__cr", currDom );

						if(acctCodeCr != null && acctCodeCr.trim().length()>0)
						{
							sql = "Select count(*) as count From accounts Where acct_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeCr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt != 0)
							{
								sql = "Select active From accounts Where acct_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, acctCodeCr);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									active = rs.getString("active");
									System.out.println("active@@["+active+"]");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								//Changes by mayur on 05-June-2018
								//if(active != "Y")
								if(!"Y".equalsIgnoreCase(active))
								{
									System.out.println("@@Inside errorcode of account code cr");
									errList.add( "VMACCTA" );
									errFields.add( childNodeName.toLowerCase());
								}
							}
							else
							{
								errList.add( "VMACCT1" );
								errFields.add( childNodeName.toLowerCase());
							}
						}
					}
					else if ( "cctr_code__dr".equalsIgnoreCase( childNodeName ) ) 
					{
						cctrCodeDr = genericUtility.getColumnValue( "cctr_code__dr", currDom );

						if(cctrCodeDr != null && cctrCodeDr.trim().length()>0)
						{
							//added by manish mhatre  on 2-jan-2019
							errCode = finCommon.isCctrCode(acctCodeDr, cctrCodeDr, " ", conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}//end manish
							
//							sql = "Select count(*) as count From costctr where cctr_code = ? ";
//							pstmt = conn.prepareStatement(sql);
//							pstmt.setString(1, cctrCodeDr);
//							rs = pstmt.executeQuery();
//							if (rs.next())
//							{
//								cnt = rs.getInt("count");
//							}
//							rs.close();
//							rs = null;
//							pstmt.close();
//							pstmt = null;
//
//							if(cnt == 0)
//							{
//								errList.add( "VMCCTR1" );
//								errFields.add( childNodeName.toLowerCase());
//							}
						}
					}
					else if ( "cctr_code__cr".equalsIgnoreCase( childNodeName ) ) 
					{
						cctrCodeCr = genericUtility.getColumnValue( "cctr_code__cr", currDom );
						
						if(cctrCodeCr != null && cctrCodeCr.trim().length()>0)
						{
							//added by manish mhatre  on 2-jan-2019
							errCode = finCommon.isCctrCode(acctCodeCr, cctrCodeCr, " ", conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
//							sql = "Select count(*) as count From costctr where cctr_code = ? ";
//							pstmt = conn.prepareStatement(sql);
//							pstmt.setString(1, cctrCodeCr);
//							rs = pstmt.executeQuery();
//							if (rs.next())
//							{
//								cnt = rs.getInt("count");
//							}
//							rs.close();
//							rs = null;
//							pstmt.close();
//							pstmt = null;
//
//							if(cnt == 0)
//							{
//								errList.add( "VMCCTR1" );
//								errFields.add( childNodeName.toLowerCase());
//							}
						}
					}
					/* // commented by manohar 31-jan-19 already same validation exists (loc_code__fr)
					else if ( "lot_sl__to".equalsIgnoreCase( childNodeName ) ) 
					{
						ls_loc_code__fr 	= 	genericUtility.getColumnValue( "loc_code__fr", currDom );
						ls_lot_no__fr 		= 	genericUtility.getColumnValue( "lot_no__fr", currDom );		
						ls_lot_sl__fr 		= 	genericUtility.getColumnValue( "lot_sl__fr", currDom );				
						ls_loc_code__to 	= 	genericUtility.getColumnValue( "loc_code__to", currDom );
						ls_lot_no__to 		= 	genericUtility.getColumnValue( "lot_no__to", currDom );			
						ls_lot_sl__to 		= 	genericUtility.getColumnValue( "lot_sl__to", currDom );			
						if((ls_loc_code__fr.trim() == ls_loc_code__to.trim()) && (ls_lot_no__fr.trim() == ls_lot_no__to.trim()) && (ls_lot_sl__fr.trim() == ls_lot_sl__to.trim()))
						{
							errList.add( "VTITRF" );
							errFields.add( childNodeName.toLowerCase());
						}
					}
					else if ( "lot_sl__fr".equalsIgnoreCase( childNodeName ) ) 
					{
						ls_loc_code__fr 	= 	genericUtility.getColumnValue( "loc_code__fr", currDom );
						ls_lot_no__fr 		= 	genericUtility.getColumnValue( "lot_no__fr", currDom );		
						ls_lot_sl__fr 		= 	genericUtility.getColumnValue( "lot_sl__fr", currDom );				
						ls_loc_code__to 	= 	genericUtility.getColumnValue( "loc_code__to", currDom );
						ls_lot_no__to 		= 	genericUtility.getColumnValue( "lot_no__to", currDom );			
						ls_lot_sl__to 		= 	genericUtility.getColumnValue( "lot_sl__to", currDom );			

						if((ls_loc_code__fr.trim() == ls_loc_code__to.trim()) && (ls_lot_no__fr.trim() == ls_lot_no__to.trim()) && (ls_lot_sl__fr.trim() == ls_lot_sl__to.trim()))
						{
							errList.add( "VTITRF" );
							errFields.add( childNodeName.toLowerCase());
						}
					}
					*/
					else if ( "cctr_code__inv".equalsIgnoreCase( childNodeName ) ) 
					{
						cctrCodeInv = genericUtility.getColumnValue( "cctr_code__inv", currDom );

						sql = "Select count(*) as count From costctr where cctr_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, cctrCodeInv);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("count");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 0)
						{
							errList.add( "VMCCTR1" );
							errFields.add( childNodeName.toLowerCase());
						}
//						errCode = finCommon.isCctrCode(acctCodeDr, cctrCodeInv, " ", conn);
//						if (errCode != null && errCode.trim().length() > 0) {
//							errList.add(errCode);
//							errFields.add(childNodeName.toLowerCase());
//						}
					}

				}// for loop end
				break;
			}//case 2 end
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
						bifurErrString =bifurErrString;//+"<trace>"+errMsg+"</trace>";
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
		}//try block end
		catch(Exception e)
		{
			System.out.println("Exception in StockTransferIC  == >");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				//Commented and added by sarita on 13NOV2017
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
				/*if( conn != null && ! conn.isClosed() )
				{
					conn.close();
				}*/
			}
			catch(Exception e)
			{
				System.out.println( "Exception :StockTransferEJB:wfValData :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		return errString;
	}

	// Item Change Functionality. Start from here....		
	/**
	 * The public method is defined without any parameters and returns blank string
	 */
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}

	/**
	 * The public method is used for converting the current form data into a document(dom)
	 * The currDom is then given as argument to the overloaded function wfValData to perform validation
	 * Returns validation string if exists else returns null in XML format
	 * @param currFrmXmlStr contains the current form data in XML format
	 * @param hdrFrmXmlStr contains all the header information in the XML format
	 * @param allFrmXmlStr contains the data of all the forms in XML format
	 * @param objContext represents the form number
	 * @param currentColumn represents the value of current field.
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr, String allFrmXmlStr, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;

		String errString = null;

		E12GenericUtility genericUtility = new E12GenericUtility();

		try
		{
			if (currFrmXmlStr != null && currFrmXmlStr.trim().length()!=0)
			{
				currDom = genericUtility.parseString(currFrmXmlStr); 
			}
			if (hdrFrmXmlStr != null && hdrFrmXmlStr.trim().length()!=0)
			{
				hdrDom = genericUtility.parseString(hdrFrmXmlStr); 
			}
			if (allFrmXmlStr != null && allFrmXmlStr.trim().length()!=0)
			{
				allDom = genericUtility.parseString(allFrmXmlStr); 
			}
			errString = itemChanged( currDom, hdrDom, allDom, objContext, currentColumn, editFlag, xtraParams );
			System.out.println ( "ErrString :" + errString);
		}
		catch (Exception e)
		{
			System.out.println ( "Exception : StockTransferIC:defaul_ItemChanged(String,String):" + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println ( "returning from StockTransferIC default_Itemchanged" );
		return errString;
	}	

	/**
	 * The public overloaded method takes a document as input and is used for the validation of required fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currDom contains the current form data as a document object model
	 * @param hdrDom contains all the header information
	 * @param allDom contains the field data of all the forms 
	 * @param objContext represents form number
	 * @param currentColumn represents the current field 
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information
	 * 
	 */
	public String itemChanged( Document currDom, Document hdrDom, Document allDom, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		String retValue = "";
		try
		{
			retValue = default_ItemChanged( currDom, hdrDom, allDom,  objContext, currentColumn, editFlag, xtraParams );
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return retValue;

	}
	public String default_ItemChanged( Document currDom, Document hdrDom, Document allDom, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		System.out.println("Call stockTransferEJB default_ItemChanged ");
		String sql = "";
		String itemCode = "";
		String itemDescr = "";
		String locCodeRcv = "";
		String tranId = "";		
		String tranDate = "";				
		String columnValue = "";		
		String refSerFor = "";
		String reasCode = "";
		String confirmed = "";
		String confDate = "";
		String siteCode = "";	
		String siteDescr = "";	
		String empCodeAprv = "";
		String fullName = "";
		String lpnNo = "";
		String itemCodeIn = "";
		String lotSl = "";
		String lc_qty_per_art = "", lc_capacity = "", lc_integral_qty = "";
		double mqty = 0, lc_modqty = 0;
		int lc_noart = 0;
		String locDescr = "";
		String acctCode = "";
		String cctrCode = "";
		String lotNo = "";
		String locCode = "";
		String locCodeFrom = "";
		String locCodeTo = "";
		String lineNo = "";
		String itemSer = "";
		String siteCodeMfg = "";
		String packCode = "";
		String potPerc = "";
		String acctCodeInv = "";
		String cctrCodeInv = "";
		String itemAcctDetr = "";
		String cctrCodeDr = "";
		String acctCodeDr = "";
		Date mfgDate = null;
		String tranType = "";

		int currentFormNo = 0;
		int domID = 0;	

		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;

		StringBuffer valueXmlString = new StringBuffer();		

		System.out.println("hello:");

		E12GenericUtility genericUtility = new E12GenericUtility();
		FinCommon finCommon = new FinCommon();
		try
		{
			String userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );

			columnValue = genericUtility.getColumnValue( currentColumn, currDom );

			DateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String dbDateFormat = genericUtility.getDBDateFormat();
			String applDateFormat = genericUtility.getApplDateFormat();

			conn = getConnection();

			DistCommon dComm = new DistCommon();

			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );

			switch ( currentFormNo )
			{
			case 1:
			{
				System.out.println("Call stockTransferEJB default_ItemChanged case 1 ");
				valueXmlString.append( "<Detail1 domID='1'>\r\n" );
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{	
					java.util.Date currDate = new java.util.Date();
					SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
					String currDateStr = sdf.format(currDate);
					String loginCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));
					String chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" ));
					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));

					System.out.println("siteCode = ["+siteCode+"]");

					//valueXmlString.append( "<tran_id/>" );
					valueXmlString.append( "<tran_date><![CDATA[" ).append( currDateStr ).append( "]]></tran_date>\r\n" );
					valueXmlString.append( "<ref_ser__for><![CDATA[" ).append( "XFRX" ).append( "]]></ref_ser__for>\r\n" );
					valueXmlString.append( "<site_code><![CDATA[" ).append(  checkNull ( siteCode)).append( "]]></site_code>\r\n" );

					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						siteDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append( loginCode ).append( "]]></chg_user>\r\n" );
					valueXmlString.append( "<chg_date><![CDATA[" ).append( currDateStr ).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
				}
				else if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )							
				{
					siteCode = genericUtility.getColumnValue("site_code", currDom);
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						siteDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<site_code><![CDATA[" ).append(  checkNull ( siteCode)).append( "]]></site_code>\r\n" );
					valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
				}
				else if( currentColumn.trim().equalsIgnoreCase( "site_code" ) )							
				{
					siteCode = genericUtility.getColumnValue("site_code", currDom);
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						siteDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<site_code><![CDATA[" ).append(  checkNull ( siteCode)).append( "]]></site_code>\r\n" );
					valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
				}
				//Changes by mayur on 05-06-2018---[start]
				else if( currentColumn.trim().equalsIgnoreCase( "item_ser" ) )							
				{
					itemSer = genericUtility.getColumnValue("item_ser", currDom);
					sql = "SELECT DESCR FROM itemser WHERE item_ser = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemSer );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						itemDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<item_ser><![CDATA[" ).append(  checkNull ( itemSer)).append( "]]></item_ser>\r\n" );
					valueXmlString.append( "<descr><![CDATA[" ).append( checkNull( itemDescr )).append( "]]></descr>\r\n" );
				}
				//Changes by mayur on 05-06-2018---[end]
				else if( currentColumn.trim().equalsIgnoreCase( "emp_code__aprv" ) )	// field is non editable						
				{
					empCodeAprv = genericUtility.getColumnValue("emp_code__aprv", currDom);
					sql = "Select emp_fname||' '||emp_mname||' '||emp_lname FROM Employee WHERE emp_code = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, empCodeAprv );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						fullName = rs.getString(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<full_name><![CDATA[" ).append(  checkNull ( fullName)).append( "]]></full_name>\r\n" );
				}
				valueXmlString.append("</Detail1>/r/n");
				break;
			}//case 1 end here

			case 2:
			{
				System.out.println("Call stockTransferIC default_ItemChanged case 2");
				//Changed by Prasad on 17/10/18 [to resolve itemchange related issue, removing domId from return string]
				//valueXmlString.append("<Detail2 domID='"+1+"' selected = 'N'>\r\n");
				valueXmlString.append("<Detail2 selected ='N'>\r\n");
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{
					tranId = genericUtility.getColumnValue("tran_id", currDom);
					//li_line_no = integer(gbf_get_argval(is_extra_arg, "line_no"))
					lineNo = genericUtility.getColumnValue("line_no", currDom);
					//Commented by Varsha V On 29-08-18 for GTPL point 623 [ unique constraint] 
					/*if(lineNo != null && lineNo.trim().length()>0)
					{
						valueXmlString.append( "<line_no><![CDATA[" ).append(  checkNull ( lineNo)).append( "]]></line_no>\r\n" );
					}*/
					//Ended by Varsha V On 29-08-18 for GTPL point 623 [ unique constraint] 
				}//outer if
				else if( currentColumn.trim().equalsIgnoreCase( "item_code" ) )							
				{
					itemCode = genericUtility.getColumnValue("item_code", currDom);
					sql = "SELECT DESCR, LOC_CODE__RECV FROM ITEM WHERE  ITEM_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						itemDescr = rs.getString("DESCR");
						locCodeRcv = rs.getString("LOC_CODE__RECV");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					sql = "SELECT DESCR FROM LOCATION WHERE LOC_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, locCodeRcv );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						locDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<item_descr><![CDATA[" ).append(  checkNull ( itemDescr)).append( "]]></item_descr>\r\n" );
					valueXmlString.append( "<loc_code__to><![CDATA[" ).append(  checkNull ( locCodeRcv)).append( "]]></loc_code__to>\r\n" );
					valueXmlString.append( "<loc_descr__to><![CDATA[" ).append(  checkNull ( locDescr)).append( "]]></loc_descr__to>\r\n" );
				}
				else if( currentColumn.trim().equalsIgnoreCase( "loc_code__fr" ) )							
				{
					locCodeFrom = genericUtility.getColumnValue("loc_code__fr", currDom);

					sql = "SELECT DESCR FROM LOCATION WHERE LOC_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, locCodeFrom );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						locDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<location_descr__fr><![CDATA[" ).append(  checkNull ( locDescr)).append( "]]></location_descr__fr>\r\n" );

					itemCode = genericUtility.getColumnValue("item_code", currDom);
					sql = "SELECT LOC_CODE__RECV FROM ITEM WHERE  ITEM_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						locCodeRcv = rs.getString("LOC_CODE__RECV");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(locCodeRcv == null || locCodeRcv.trim().length()==0)
					{
						valueXmlString.append( "<loc_code__to><![CDATA[" ).append(  checkNull ( locCodeFrom)).append( "]]></loc_code__to>\r\n" );
						valueXmlString.append( "<loc_descr__to><![CDATA[" ).append(  checkNull ( locDescr)).append( "]]></loc_descr__to>\r\n" );
					}
					
					//Added by Varsha V to set no of art on loc_code__fr itemchange on 08-09-18
					//mqty = Double.parseDouble(genericUtility.getColumnValue( "quantity", currDom ));
					 //  System.out.println("TESTqty:["+qty+"]");
				    //  mqty=qty==null?0:Double.parseDouble(qty);
					//System.out.println("qty:["+qty+"]");
				   //	System.out.println("mqty:["+mqty+"]");
					//Changed by chaitali on 07-06-2019//
					String qty=E12GenericUtility.checkNull(genericUtility.getColumnValue( "quantity", currDom));
		     	    if (qty == null || "null".equals(qty) || qty.trim().length() == 0 )
				    {
				        qty = "0";
				    }
				    else
				    {
				    mqty = Double.parseDouble(qty);    
				    }
					System.out.println("qty:["+qty+"]");
					System.out.println("mqty:["+mqty+"]");
					//changed by chaitali on 07-06-2019//
					itemCode = genericUtility.getColumnValue("item_code", currDom);
					siteCode = genericUtility.getColumnValue("site_code", hdrDom);
					lotNo = genericUtility.getColumnValue( "lot_no__fr", currDom );		
					lpnNo = genericUtility.getColumnValue( "lot_sl__fr", currDom );	
					if(lotNo==null)  // Modified by Rohini T on 16/04/2021[Start] for setting of lotno,lpnNo as space
	                {
						lotNo= " ";    
						valueXmlString.append( "<lot_no__fr><![CDATA[" ).append(lotNo).append( "]]></lot_no__fr>\r\n" );
	                }               
					if(lpnNo==null)  
	                {
						lpnNo= " ";
						valueXmlString.append( "<lot_sl__fr><![CDATA[" ).append(lpnNo).append( "]]></lot_sl__fr>\r\n" );
	                }                // Modified by Rohini T on 16/04/2021[End] for setting of lotno,lpnNo as space
					sql = "Select qty_per_art,pack_code FROM STOCK WHERE  ITEM_CODE  = ? " +
					"And site_code = ? And loc_code  = ? And lot_no = ?	And lot_sl = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					pstmt.setString( 2, siteCode );						
					pstmt.setString( 3, locCodeFrom );						
					pstmt.setString( 4, lotNo );						
					pstmt.setString( 5, lpnNo );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						lc_qty_per_art =  rs.getString("qty_per_art");
						packCode =  rs.getString("pack_code");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(lc_qty_per_art == null || lc_qty_per_art.trim().length()==0)
					{
						lc_qty_per_art = "0";
					}
					else if(lc_qty_per_art != null || lc_qty_per_art.trim().length()>0)
					{
						sql = "select mod(?,?) result from dual";
						pstmt = conn.prepareStatement( sql );

						pstmt.setDouble( 1, mqty );
						pstmt.setDouble( 2, Double.parseDouble(lc_qty_per_art) );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lc_modqty = rs.getDouble("result"); 
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if (lc_modqty > 0) 
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art)) + 1;
						}
						else
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art));
						}
					}
					valueXmlString.append( "<no_art><![CDATA[" ).append(lc_noart).append( "]]></no_art>\r\n" );
					System.out.println("valueXmlString in case 2 loc_code__fr ::"+valueXmlString.toString());
					//Ended by Varsha V to set no of art on loc_code__fr itemchange on 08-09-18
				}
				else if( currentColumn.trim().equalsIgnoreCase( "loc_code__to" ) )							
				{
					locCodeTo = genericUtility.getColumnValue("loc_code__to", currDom);
					sql = "SELECT DESCR FROM LOCATION WHERE LOC_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, locCodeTo );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						locDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<loc_descr__to><![CDATA[" ).append(  checkNull ( locDescr)).append( "]]></loc_descr__to>\r\n" );

				}
				else if( currentColumn.trim().equalsIgnoreCase( "lot_no__fr" ) )							
				{
					lotNo = genericUtility.getColumnValue("lot_no__fr", currDom);
					valueXmlString.append( "<lot_no__to><![CDATA[" ).append(  checkNull ( lotNo)).append( "]]></lot_no__to>\r\n" );
					//Added by Varsha V to set no of art on loc_code__fr itemchange on 08-09-18
					//mqty = Double.parseDouble(genericUtility.getColumnValue( "quantity", currDom ));   
					   // System.out.println("TESTqty:["+qty+"]");
					   // mqty=qty==null?0:Double.parseDouble(qty);
					   // System.out.println("qty:["+qty+"]");
					    //System.out.println("mqty:["+mqty+"]");
					// Changed by chaitali on 10-06-2019
					    String qty=E12GenericUtility.checkNull(genericUtility.getColumnValue( "quantity", currDom));
			     	    if (qty == null || "null".equals(qty) || qty.trim().length() == 0 )
					    {
					        qty = "0";
					    }
					    else
					    {
					    mqty = Double.parseDouble(qty);    
					    }
						System.out.println("qty:["+qty+"]");
						System.out.println("mqty:["+mqty+"]");
					    
					//Changed by chaitali on 10-06-2019
					locCodeFrom = genericUtility.getColumnValue("loc_code__fr", currDom);
					itemCode = genericUtility.getColumnValue("item_code", currDom);
					siteCode = genericUtility.getColumnValue("site_code", hdrDom);	
					lpnNo = genericUtility.getColumnValue( "lot_sl__fr", currDom );	
					if(lotNo==null)  // Modified by Rohini T on 16/04/2021[Start] for setting of lotno,lpnNo as space
	                {
						lotNo= " ";   
						valueXmlString.append( "<lot_no__fr><![CDATA[" ).append(lotNo).append( "]]></lot_no__fr>\r\n" );
	                }               
					if(lpnNo==null)  
	                {
						lpnNo= " ";  
						valueXmlString.append( "<lot_sl__fr><![CDATA[" ).append(lpnNo).append( "]]></lot_sl__fr>\r\n" );
	                }                // Modified by Rohini T on 16/04/2021[End] for setting of lotno,lpnNo as space
					sql = "Select qty_per_art,pack_code FROM STOCK WHERE  ITEM_CODE  = ? " +
					"And site_code = ? And loc_code  = ? And lot_no = ?	And lot_sl = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					pstmt.setString( 2, siteCode );						
					pstmt.setString( 3, locCodeFrom );						
					pstmt.setString( 4, lotNo );						
					pstmt.setString( 5, lpnNo );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						lc_qty_per_art =  rs.getString("qty_per_art");
						packCode =  rs.getString("pack_code");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(lc_qty_per_art == null || lc_qty_per_art.trim().length()==0)
					{
						lc_qty_per_art = "0";
					}
					else if(lc_qty_per_art != null || lc_qty_per_art.trim().length()>0)
					{
						sql = "select mod(?,?) result from dual";
						pstmt = conn.prepareStatement( sql );

						pstmt.setDouble( 1, mqty );
						pstmt.setDouble( 2, Double.parseDouble(lc_qty_per_art) );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lc_modqty = rs.getDouble("result"); 
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if (lc_modqty > 0) 
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art)) + 1;
						}
						else
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art));
						}
					}
					valueXmlString.append( "<no_art><![CDATA[" ).append(lc_noart).append( "]]></no_art>\r\n" );
					System.out.println("valueXmlString in case 2 lot_no__fr ::"+valueXmlString.toString());
					//Ended by Varsha V to set no of art on loc_code__fr itemchange on 08-09-18
				}
				else if( currentColumn.trim().equalsIgnoreCase( "lot_sl__fr" ) )							
				{
					lpnNo = genericUtility.getColumnValue("lot_sl__fr", currDom);
					valueXmlString.append( "<lot_sl__to><![CDATA[" ).append(  checkNull ( lpnNo)).append( "]]></lot_sl__to>\r\n" );
					//Added by Varsha V to set no of art on loc_code__fr itemchange on 08-09-18
					//mqty = Double.parseDouble(genericUtility.getColumnValue( "quantity", currDom ));
					 //System.out.println("TESTqty:["+qty+"]");
				    //mqty=qty==null?0:Double.parseDouble(qty);
					//System.out.println("qty:["+qty+"]");
					//System.out.println("mqty:["+mqty+"]");
					//Changed by chaitali on 10-06-2019
					String qty=E12GenericUtility.checkNull(genericUtility.getColumnValue( "quantity", currDom));
		     	    if (qty == null || "null".equals(qty) || qty.trim().length() == 0 )
				    {
				        qty = "0";
				    }
				    else
				    {
				    mqty = Double.parseDouble(qty);    
				    }
					System.out.println("qty:["+qty+"]");
					System.out.println("mqty:["+mqty+"]");
					//Changed by chaitali on 10-06-2019
					locCodeFrom = genericUtility.getColumnValue("loc_code__fr", currDom);
					itemCode = genericUtility.getColumnValue("item_code", currDom);
					siteCode = genericUtility.getColumnValue("site_code", hdrDom);	
					lotNo = genericUtility.getColumnValue("lot_no__fr", currDom);
					if(lotNo==null)  // Modified by Rohini T on 16/04/2021[Start] for setting of lotno,lpnNo as space
	                {
						lotNo= " ";     
						valueXmlString.append( "<lot_no__fr><![CDATA[" ).append(lotNo).append( "]]></lot_no__fr>\r\n" );
	                }               
					if(lpnNo==null)  
	                {
						lpnNo= " "; 
						valueXmlString.append( "<lot_sl__fr><![CDATA[" ).append(lpnNo).append( "]]></lot_sl__fr>\r\n" );
	                }                // Modified by Rohini T on 16/04/2021[End] for setting of lotno,lpnNo as space
					sql = "Select qty_per_art,pack_code FROM STOCK WHERE  ITEM_CODE  = ? " +
					"And site_code = ? And loc_code  = ? And lot_no = ?	And lot_sl = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					pstmt.setString( 2, siteCode );						
					pstmt.setString( 3, locCodeFrom );						
					pstmt.setString( 4, lotNo );						
					pstmt.setString( 5, lpnNo );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						lc_qty_per_art =  rs.getString("qty_per_art");
						packCode =  rs.getString("pack_code");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(lc_qty_per_art == null || lc_qty_per_art.trim().length()==0)
					{
						lc_qty_per_art = "0";
					}
					else if(lc_qty_per_art != null || lc_qty_per_art.trim().length()>0)
					{
						sql = "select mod(?,?) result from dual";
						pstmt = conn.prepareStatement( sql );

						pstmt.setDouble( 1, mqty );
						pstmt.setDouble( 2, Double.parseDouble(lc_qty_per_art) );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lc_modqty = rs.getDouble("result"); 
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if (lc_modqty > 0) 
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art)) + 1;
						}
						else
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art));
						}
					}
					valueXmlString.append( "<no_art><![CDATA[" ).append(lc_noart).append( "]]></no_art>\r\n" );
					System.out.println("valueXmlString in case 2 lot_sl__fr ::"+valueXmlString.toString());
					//Ended by Varsha V to set no of art on loc_code__fr itemchange on 08-09-18
				}
				
				else if( currentColumn.trim().equalsIgnoreCase( "quantity" ) )							
				{
					//mqty = Double.parseDouble(genericUtility.getColumnValue( "quantity", currDom ));
					//Changed by chaitali on 11-06-2019
					String qty=E12GenericUtility.checkNull(genericUtility.getColumnValue( "quantity", currDom));
		     	    if (qty == null || "null".equals(qty) || qty.trim().length() == 0 )
				    {
				        qty = "0";
				    }
				    else
				    {
				    mqty = Double.parseDouble(qty);    
				    }
					System.out.println("qty:["+qty+"]");
					System.out.println("mqty:["+mqty+"]");
					//Changed by chaitali on 11-06-2019
					itemCode = genericUtility.getColumnValue("item_code", currDom);
					siteCode = genericUtility.getColumnValue("site_code", hdrDom);
					locCodeFrom = genericUtility.getColumnValue( "loc_code__fr", currDom );
					lotNo = genericUtility.getColumnValue( "lot_no__fr", currDom );		
					lpnNo = genericUtility.getColumnValue( "lot_sl__fr", currDom );	
					if(lotNo==null)  // Modified by Rohini T on 16/04/2021[Start] for setting of lotno,lpnNo as space
	                {
						lotNo= " ";  
						valueXmlString.append( "<lot_no__fr><![CDATA[" ).append(lotNo).append( "]]></lot_no__fr>\r\n" );
	                }               
					if(lpnNo==null)  
	                {
						lpnNo= " "; 
						valueXmlString.append( "<lot_sl__fr><![CDATA[" ).append(lpnNo).append( "]]></lot_sl__fr>\r\n" );
	                }                // Modified by Rohini T on 16/04/2021[End] for setting of lotno,lpnNo as space
					sql = "Select qty_per_art,pack_code FROM STOCK WHERE  ITEM_CODE  = ? " +
					"And site_code = ? And loc_code  = ? And lot_no = ?	And lot_sl = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					pstmt.setString( 2, siteCode );						
					pstmt.setString( 3, locCodeFrom );						
					pstmt.setString( 4, lotNo );						
					pstmt.setString( 5, lpnNo );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						lc_qty_per_art =  rs.getString("qty_per_art");
						packCode =  rs.getString("pack_code");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(lc_qty_per_art == null || lc_qty_per_art.trim().length()==0)
					{
						lc_qty_per_art = "0";
					}
					else if(lc_qty_per_art != null || lc_qty_per_art.trim().length()>0)
					{
						sql = "select mod(?,?) result from dual";
						pstmt = conn.prepareStatement( sql );

						pstmt.setDouble( 1, mqty );
						pstmt.setDouble( 2, Double.parseDouble(lc_qty_per_art) );

						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							lc_modqty = rs.getDouble("result"); 
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						
						if (lc_modqty > 0) 
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art)) + 1;
						}
						else
						{
							lc_noart = (int)(mqty / Double.parseDouble(lc_qty_per_art));
						}
					}
					else
					{
						sql = "select capacity from packing where pack_code = ?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, packCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							lc_capacity = rs.getString("capacity");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;

						if(lc_capacity == null || lc_capacity.trim().length()==0)
						{
							lc_capacity = "0";
						}
						else if(lc_capacity != null || lc_capacity.trim().length()>0)
						{
							sql = "select mod(?,?) result from dual";
							pstmt = conn.prepareStatement( sql );

							pstmt.setDouble( 1, mqty );
							pstmt.setDouble( 2, Double.parseDouble(lc_capacity) );

							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lc_modqty = rs.getDouble("result"); 
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							if (lc_modqty > 0) 
							{
								lc_noart = (int)(mqty / Double.parseDouble(lc_capacity)) + 1;
							}
							else
							{
								lc_noart = (int)(mqty / Double.parseDouble(lc_capacity));
							}
						}
						else
						{
							sql = "Select integral_qty From item Where item_code = ?";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, itemCode );						
							rs = pstmt.executeQuery();	
							if( rs.next() )
							{
								lc_integral_qty = rs.getString("integral_qty");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;

							if(lc_integral_qty == null || lc_integral_qty.trim().length()==0)
							{
								lc_integral_qty = "0";
							}
							else if(lc_integral_qty != null || lc_integral_qty.trim().length()>0)
							{
								sql = "select mod(?,?) result from dual";
								pstmt = conn.prepareStatement( sql );

								pstmt.setDouble( 1, mqty );
								pstmt.setDouble( 2, Double.parseDouble(lc_integral_qty) );

								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lc_modqty = rs.getDouble("result"); 
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								if (lc_modqty > 0) 
								{
									lc_noart = (int)(mqty / Double.parseDouble(lc_integral_qty)) + 1;
								}
								else
								{
									lc_noart = (int)(mqty / Double.parseDouble(lc_integral_qty));
								}
							}
						}

					}
					valueXmlString.append( "<no_art><![CDATA[" ).append(lc_noart).append( "]]></no_art>\r\n" );
				}

				siteCode = genericUtility.getColumnValue("site_code", hdrDom);
				itemSer = genericUtility.getColumnValue("item_ser", hdrDom);
				tranType = genericUtility.getColumnValue("tran_type", hdrDom);
				itemCode = genericUtility.getColumnValue("item_code", currDom);
				locCodeFrom = genericUtility.getColumnValue( "loc_code__fr", currDom );
				lotNo = genericUtility.getColumnValue( "lot_no__fr", currDom );		
				lpnNo = genericUtility.getColumnValue( "lot_sl__fr", currDom );		
				if(lotNo==null)  // Modified by Rohini T on 16/04/2021[Start] for setting of lotno,lpnNo as space
                {
					System.out.println("lotNo.......@@"+lotNo);
					lotNo= " ";  
					valueXmlString.append( "<lot_no__fr><![CDATA[" ).append(lotNo).append( "]]></lot_no__fr>\r\n" );
                }               
				if(lpnNo==null)  
                {
					System.out.println("lpnNo......@@."+lpnNo);
					lpnNo= " "; 
					valueXmlString.append( "<lot_sl__fr><![CDATA[" ).append(lpnNo).append( "]]></lot_sl__fr>\r\n" );
                }                // Modified by Rohini T on 16/04/2021[End] for setting of lotno,lpnNo as space
				sql = "Select site_code__mfg, mfg_date, pack_code, potency_perc, acct_code__inv, cctr_code__inv FROM STOCK WHERE  ITEM_CODE  = ? " +
				"And site_code = ? And loc_code  = ? And lot_no = ?	And lot_sl = ? ";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, itemCode );						
				pstmt.setString( 2, siteCode );						
				pstmt.setString( 3, locCodeFrom );						
				pstmt.setString( 4, lotNo );						
				pstmt.setString( 5, lpnNo );						
				rs = pstmt.executeQuery();	
				if( rs.next() )
				{
					siteCodeMfg = rs.getString("site_code__mfg");
					mfgDate = rs.getDate("mfg_date");
					packCode = rs.getString("pack_code");
					potPerc = rs.getString("potency_perc");
					acctCodeInv = rs.getString("acct_code__inv");
					cctrCodeInv = rs.getString("cctr_code__inv");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				System.out.println("acctCodeInv......."+acctCodeInv+"cctrCodeInv....."+cctrCodeInv);
				valueXmlString.append( "<acct_code__cr><![CDATA[" ).append(  checkNull ( acctCodeInv)).append( "]]></acct_code__cr>\r\n" );
				valueXmlString.append( "<cctr_code__cr><![CDATA[" ).append(  checkNull ( cctrCodeInv)).append( "]]></cctr_code__cr>\r\n" );

				itemAcctDetr  = finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINV", tranType, conn);
				if (itemAcctDetr != null && itemAcctDetr.trim().length() > 0) 
				{
					String tokens [] = itemAcctDetr.split(",");

					System.out.println("Length="+tokens.length);

					if ( tokens.length >= 2)
					{
						acctCodeDr = tokens[0];
						cctrCodeDr = tokens[1];

						acctCodeDr = checkNullAndTrim(acctCodeDr);
						cctrCodeDr = checkNullAndTrim(cctrCodeDr);
						System.out.println("acctCodeDr="+acctCodeDr);
						System.out.println("cctrCodeDr="+cctrCodeDr);
					}
					else
					{
						acctCodeDr = itemAcctDetr.substring(0,itemAcctDetr.indexOf(","));
						cctrCodeDr = itemAcctDetr.substring(itemAcctDetr.indexOf(",") + 1);
					}
					tokens = null;
				}
				valueXmlString.append( "<acct_code__dr><![CDATA[" ).append(  checkNull ( acctCodeDr)).append( "]]></acct_code__dr>\r\n" );
				valueXmlString.append( "<cctr_code__dr><![CDATA[" ).append(  checkNull ( cctrCodeDr)).append( "]]></cctr_code__dr>\r\n" );

				valueXmlString.append("</Detail2>/r/n");
				break;				  		
			}// case 2
			}//switch  end
		}//try block end
		catch(Exception e)
		{
			System.out.println( "Exception :StockTransferIC :default_ItemChanged(Document,String):" + e.getMessage() + ":" );
			valueXmlString = valueXmlString.append( genericUtility.createErrorString( e ) );
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception :StockTransferIC:default_ItemChanged :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append( "</Root>\r\n" );	
		System.out.println( "\n****ValueXmlString :" + valueXmlString.toString() + ":********" );
		return valueXmlString.toString();
	}

	private String checkNull( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		return inputVal;
	}

	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}

	public String getLocType(String itemCode, String locCode, Connection conn) throws ITMException
	{
		String ls_item_loctype = "", ls_loc_loctype ="", sql = "" , errCode ="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			sql = "Select loc_type From Item Where item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ls_item_loctype = rs.getString("loc_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "Select loc_type From location Where loc_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, locCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ls_loc_loctype = rs.getString("loc_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(ls_item_loctype.trim() != ls_loc_loctype.trim())
			{
				errCode = itmDBAccess.getErrorString("", "VMLOCTYP1", "","",conn);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return errCode;
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
}
