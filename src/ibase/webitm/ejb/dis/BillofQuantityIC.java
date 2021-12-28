/********************************************************
        Title : BillofQuantityIC
        Date  : 20/09/12
        Developer: Akhilesh Sikarwar

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
@Stateless
public class BillofQuantityIC extends ValidatorEJB

implements BillofQuantityICLocal, BillofQuantityICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	String winName = null;
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String sql = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String currCode = "";
		String suppCodePref = "";
		String projCode = "";
		String itemCode = "",ditemCode = "";
		String packCode = "";
		String hitemCode = "",tranId = "";
		String rate = "",drate = "";
		Double rate1 = 0.0;
		String indentNo = "";
		String tranDateS = "";
		String unitRate="";
		String acctCode = "",cctrCode = "";
		System.out.println("editFlag ---->>>["+editFlag+"]");
		String unit = "";
		String updateFlag = "",status = ""; 
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";

		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{ 
			this.finCommon = new FinCommon();
			this.validator = new ValidatorEJB();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				tranId=checkNull(this.genericUtility.getColumnValue("tran_id", dom));
				System.out.println("tran id from boqdet --4-->>>>["+tranId+"]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("CURRENT COLUMN IN  VALIDATION ["+childNodeName+"]");
					if (childNodeName.equalsIgnoreCase("tran_date"))
					{
						tranDateS=this.genericUtility.getColumnValue("tran_date", dom);
						if (tranDateS == null || tranDateS.equals("DD/MM/YY"))
						{
							errCode = "VMTRANDT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("indent_no"))
					{
						indentNo=this.genericUtility.getColumnValue("indent_no", dom);
						if (indentNo != null && indentNo.trim().length() > 0 )
						{
							itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
							// added by cpandey Validation for duplicate tran id in boqhdr on 07/03/13
							/*sql = "select count(*) from boqhdr where tran_id = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 1 ) 
							{
								errCode = "VTDUPTRID";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}*/
							// end of validation
							//added by cpandey validation for duplicate entry of indent no in boqhdr on 07/03/13
							if("A".equalsIgnoreCase(editFlag))
							{
							sql = "select count(*) from boqhdr where  status != 'X' and indent_no = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, indentNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt > 0 ) 
							{
								errCode = "VTDUPINDNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}		
							}
							//end of addition

							//changes done by cpandey for approved indent no status = 'A' 
							sql = "select count(*) from indent where status = 'A' and item_code = ? and ind_no = ? ";  //add joint of ind_no
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, indentNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "VTINDNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}  							//added by cpandey for approved indent no on 06/11/12
							else 
							{
								sql = "select status from indent where item_code = ? and ind_no = ? ";  //add joint of ind_no
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, indentNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									status = rs.getString("status");
									System.out.println("Status of indent no-->> ["+status+"]");
								}
								rs.close();   		rs = null;
								pstmt.close();      pstmt = null;	
								if(!("A".equalsIgnoreCase(status.trim()))) 
								{
									errCode = "VTINDNONAP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//end of changes 
						}
					}

					else if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						System.out.println("Tran id from current application ----["+tranId+"]-->>");
						ditemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom2));
						System.out.println("Detail item code --on header part--> ["+ditemCode+"] and detail item code---on header part--> ["+itemCode+"]");
						itemCode = itemCode.trim();
						ditemCode = ditemCode.trim();
						if (itemCode != null && itemCode.trim().length() > 0 )
						{	
							sql = "select count(*) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "VMITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMITEMBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
					}
					//validation for rate can not be null on 28/02/13
					else if (childNodeName.equalsIgnoreCase("rate"))
					{
						rate=checkNull(this.genericUtility.getColumnValue("rate", dom));
						System.out.println("Tran id from current application ----["+tranId+"]-and rate ["+rate+"]->>");
						System.out.println("Detail item code --on header part--> ["+ditemCode+"] and detail item code---on header part--> ["+itemCode+"]");
						rate1 =Double.parseDouble(rate);
						if (rate1 == 0)
						{
							System.out.println("rate can not be null or 0-->>");
							errCode = "VMRATEBLNK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					//end of changes on 05/11/12
					//end of changes on 05/11/12
					else if (childNodeName.trim().equalsIgnoreCase("unit__rate"))
					{
						unitRate=checkNull(this.genericUtility.getColumnValue("unit__rate", dom));		
						unit=checkNull(this.genericUtility.getColumnValue("unit", dom));
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						if(!unit.trim().equalsIgnoreCase(unitRate.trim()))
						{
							sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unit);
							pstmt.setString(2, unitRate);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
								System.out.println("Count........1 "+cnt);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unit);
								pstmt.setString(2, unitRate);
								pstmt.setString(3, "X");
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									System.out.println("Count........2 "+cnt);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(cnt == 0)
								{
									sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, unitRate);
									pstmt.setString(2, unit);
									pstmt.setString(3, itemCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if(cnt == 0)
									{
										sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, unitRate);
										pstmt.setString(2, unit);
										pstmt.setString(3, "X");
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											cnt = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(cnt == 0)
										{

											errCode = "VTUNIT3";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode=this.genericUtility.getColumnValue("curr_code", dom);
						if(currCode != null && currCode.trim().length() > 0)
						{
							sql = "select count(*) from currency where curr_code= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt == 0)
							{
								errCode = "VTCURRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						else
						{
							errCode = "VECUR2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("supp_code__pref"))
					{
						suppCodePref=this.genericUtility.getColumnValue("supp_code__pref", dom);
						if(suppCodePref != null && suppCodePref.trim().length() > 0)
						{
							sql = "select count(*) from supplier where supp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, suppCodePref);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt == 0)
							{
								errCode = "VMSUPP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}

					else if (childNodeName.equalsIgnoreCase("proj_code"))
					{
						projCode=this.genericUtility.getColumnValue("proj_code", dom);	
						if (projCode != null && projCode.trim().length() > 0 )
						{

							sql = "select count(*) from project where proj_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								errCode = "VMPROJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}						
					}	
					else if (childNodeName.trim().equalsIgnoreCase("pack_code"))
					{
						packCode=checkNull(this.genericUtility.getColumnValue("pack_code", dom));
						if(packCode != null && packCode.trim().length() > 0)
						{
							sql = "select count(*) from packing where pack_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, packCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "VMPACKCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}					
						}
					}
				}

				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				System.out.println("editFlag  -->>["+editFlag+"]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.trim().equalsIgnoreCase("item_code"))
					{
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						hitemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom1));
						System.out.println("Header item code ["+hitemCode+"] and detail item code ["+itemCode+"]");
						itemCode = itemCode.trim();
						hitemCode = hitemCode.trim();

						if (itemCode != null && itemCode.trim().length() > 0 )
						{
							sql = "select count(*) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "VMITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMITEMBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//added by cpandey validation for matching item code from header and detial on 05/11/12
						/*if(itemCode.trim().equalsIgnoreCase(hitemCode.trim()))
						 {
							 System.out.println("edit flag --4>>["+editFlag+"]");
							 if(("E").equalsIgnoreCase(editFlag) || ("A").equalsIgnoreCase(editFlag))
							 {
								 System.out.println("edit flag --2>>["+editFlag+"]");
								 System.out.println("EDIT VALIDATION IN CASE OF--EDIT--MODE -->>["+editFlag+"]");
								 errCode = "VMITMSM";
								 errList.add(errCode);
								 errFields.add(childNodeName.toLowerCase());
							 }
						 }*/
					}
					//end of changes on 05/11/12 
					else if (childNodeName.trim().equalsIgnoreCase("rate"))
					{
						drate=checkNull(this.genericUtility.getColumnValue("rate", dom));
						System.out.println("dRate from detail window ----["+drate+"]-and rate ["+drate+"]->>");
						//System.out.println("Detail item code --on header part--> ["+ditemCode+"] and detail item code---on header part--> ["+itemCode+"]");
						//rate1 =Double.parseDouble(rate);
						if (drate==null || drate.trim().length()==0)
						{
							System.out.println("drate can not be null or 0-->>");
							errCode = "VMRATEBLNK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					else if (childNodeName.trim().equalsIgnoreCase("unit__rate"))
					{
						unitRate=checkNull(this.genericUtility.getColumnValue("unit__rate", dom));		
						unit=checkNull(this.genericUtility.getColumnValue("unit", dom));
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						if(!unit.trim().equalsIgnoreCase(unitRate.trim()))
						{
							sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unit);
							pstmt.setString(2, unitRate);
							pstmt.setString(3, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
								System.out.println("Count........1 "+cnt);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unit);
								pstmt.setString(2, unitRate);
								pstmt.setString(3, "X");
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									System.out.println("Count........2 "+cnt);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(cnt == 0)
								{
									sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, unitRate);
									pstmt.setString(2, unit);
									pstmt.setString(3, itemCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt = rs.getInt(1);
										System.out.println("Count........3 "+cnt);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if(cnt == 0)
									{
										sql =	"SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? AND ITEM_CODE = ?"; 
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, unitRate);
										pstmt.setString(2, unit);
										pstmt.setString(3, "X");
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											cnt = rs.getInt(1);
											System.out.println("Count........4 "+cnt);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(cnt == 0)
										{

											errCode = "VTUNIT3";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("pack_code"))
					{
						packCode=checkNull(this.genericUtility.getColumnValue("pack_code", dom));
						if(packCode != null && packCode.trim().length() > 0)
						{
							sql = "select count(*) from packing where pack_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, packCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "VMPACKCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("tax_class"))
					{
						taxClass=checkNull(this.genericUtility.getColumnValue("tax_class", dom));
						if(taxClass != null && taxClass.trim().length() > 0)
						{
							sql = "select count(*) from taxClass where tax_class = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
						/*else
						{
							errCode = "VMTXCLNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}
					else if (childNodeName.trim().equalsIgnoreCase("tax_chap"))
					{
						taxChap=checkNull(this.genericUtility.getColumnValue("tax_chap", dom));
						if(taxChap != null && taxChap.trim().length() > 0)
						{
							sql = "select count(*) from taxChap where tax_chap = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(cnt == 0 ) 
							{
								errCode = "INTAXCHAP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
						/*else
						{
							errCode = "VMTXCPNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}
					else if (childNodeName.trim().equalsIgnoreCase("tax_env"))
					{
						taxEnv=checkNull(this.genericUtility.getColumnValue("tax_env", dom));
						if(taxEnv != null && taxEnv.trim().length() > 0)
						{
							sql = "select count(*) from taxEnv where tax_env = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxEnv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	

							if(cnt == 0 ) 
							{
								errCode = "INTAXENV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
						/*else
						{
							errCode = "VMTAXENV";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}
					else if (childNodeName.trim().equalsIgnoreCase("acct_code__dr"))
					{
						acctCode = checkNull(this.genericUtility.getColumnValue("acct_code__dr", dom));
						if(acctCode != null && acctCode.trim().length() > 0)
						{
							sql = " SELECT COUNT(*) FROM ACCOUNTS WHERE ACCT_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	

							if(cnt == 0 ) 
							{
								errCode = "VMACCTBK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("cctr_code__dr"))
					{
						cctrCode = checkNull(this.genericUtility.getColumnValue("cctr_code__dr", dom));
						if(cctrCode != null && cctrCode.trim().length() > 0)
						{
							sql = " SELECT COUNT(*) FROM  COSTCTR WHERE CCTR_CODE = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	

							if(cnt == 0 ) 
							{
								errCode = "VMCCTRMT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
						}
					}
					

				}
				valueXmlString.append("</Detail1>");

			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 
								8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + 
								errString.substring(errString.indexOf("</trace>") + 
										8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
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

					conn.close();
				}
				conn = null;
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}


	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("hELLO PRINT");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("VALUE HELLO PRINT["+valueXmlString+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA["+valueXmlString+"]");
		return valueXmlString;
			}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		System.out.println("sTART PRINT ");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";
		String currCode = "";
		String shName = "";
		String salesPers = "";
		String indentNo = "";
		String descrItem = "";
		String itemCode = "";
		String packCode = "";
		String projCode = "";
		String suppCodePref = "";
		String descrCurr = "";
		String descrProjCode = "";
		String itemSer = "";
		String unit = "";
		String unitRate = "";
		String unitPur ="";
		String descrPackCode = "";
		String status = "";
		String loginSite = "";
		String unitStd="";
		String descr = "";
		String siteCode = "";
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";
		int ctr = 0;
		int currentFormNo = 0;
		double  quantity = 0.0;
		double stdExrt = 0.0;
		double amount = 0.0;
		double amountBc = 0.0;
		double rate = 0.0;
		double quantityStduom = 0.0 ;
		double rateStduom = 0.0 ;
		double convQtuomStduom = 0.0;
		java.util.Date reqDate = null;
		int childNodeListLength = 0;
		java.util.Date statusDate = null;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = new DistCommon();
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			this.finCommon = new FinCommon();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}

					ctr++;
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN ["+currentColumn+"]");

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					valueXmlString.append("<site_code>").append("<![CDATA["+loginSite+"]]>").append("</site_code>");
					sql = "select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					valueXmlString.append("<site_descr>").append("<![CDATA["+descr+"]]>").append("</site_descr>");

					String currAppdate ="";
					java.sql.Timestamp currDate = null;
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate);
					valueXmlString.append("<tran_date>").append("<![CDATA["+currAppdate+"]]>").append("</tran_date>");

				}

				else if (currentColumn.trim().equalsIgnoreCase("indent_no"))
				{
					indentNo=checkNull(this.genericUtility.getColumnValue("indent_no", dom));
					sql = "select item_code ,quantity ,supp_code__pref,proj_code ,status,status_date  from indent where ind_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indentNo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemCode = checkNull(rs.getString(1));
						quantity = rs.getDouble(2);
						suppCodePref = checkNull(rs.getString(3));
						projCode = checkNull(rs.getString(4));
						status = checkNull(rs.getString(5));
						statusDate = rs.getDate(6);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_code >").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
					valueXmlString.append("<quantity >").append("<![CDATA["+quantity+"]]>").append("</quantity>");
					valueXmlString.append("<supp_code__pref >").append("<![CDATA["+suppCodePref+"]]>").append("</supp_code__pref>");
					valueXmlString.append("<proj_code >").append("<![CDATA["+projCode+"]]>").append("</proj_code>");
					valueXmlString.append("<status >").append("<![CDATA["+status+"]]>").append("</status>");
					if(statusDate == null)
					{
						valueXmlString.append("<status_date >").append("<![CDATA["+""+"]]>").append("</status_date>");
					}
					else
					{
						valueXmlString.append("<status_date >").append("<![CDATA["+sdf.format(statusDate)+"]]>").append("</status_date>");
					}
					sql = "select descr ,item_ser ,unit ,unit__rate from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descrItem = checkNull(rs.getString(1));
						itemSer = checkNull(rs.getString(2));
						//changes done by cpandey on 06/11/12
						unit = checkNull(rs.getString(3));
						unitRate = checkNull(rs.getString(4));
						System.out.println("item code dedscr--> ["+descrItem+"] and item series--> ["+itemSer+"] and unit--> ["+unit+"] and unitRate--> ["+unitRate+"]");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_descr >").append("<![CDATA["+descrItem+"]]>").append("</item_descr>");
					valueXmlString.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");
					//changes done by cpandey on 06/11/12 according to qc required
					valueXmlString.append("<unit>").append("<![CDATA["+unit+"]]>").append("</unit>");
					valueXmlString.append("<unit__rate>").append("<![CDATA["+unitRate+"]]>").append("</unit__rate>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));					
					sql = "select descr ,item_ser ,unit ,unit__pur ,unit__rate from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descrItem = checkNull(rs.getString(1));
						itemSer = checkNull(rs.getString(2));
						unit = checkNull(rs.getString(3));
						unitPur = rs.getString(4)== null?"":rs.getString(3);
						unitRate = rs.getString(5)== null?"":rs.getString(3);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_descr >").append("<![CDATA["+descrItem+"]]>").append("</item_descr>");
					valueXmlString.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");

					valueXmlString.append("<unit>").append("<![CDATA["+unitPur+"]]>").append("</unit>");
					valueXmlString.append("<unit__std >").append("<![CDATA["+unit+"]]>").append("</unit__std>");
					valueXmlString.append("<unit__rate>").append("<![CDATA["+unitRate+"]]>").append("</unit__rate>");

					/*sql = "select unit from indent_det where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						unit = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;	
					valueXmlString.append("<unit >").append("<![CDATA["+unit+"]]>").append("</unit>");*/
				}	
				else if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = checkNull(this.genericUtility.getColumnValue("site_code",dom));
					sql = "select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");	
					valueXmlString.append("<site_descr>").append("<![CDATA["+descr+"]]>").append("</site_descr>");	
				}

				else if (childNodeName.trim().equalsIgnoreCase("unit__rate"))
				{
					unitRate=checkNull(this.genericUtility.getColumnValue("unit__rate", dom));
					quantity=checkDoubleNull(this.genericUtility.getColumnValue("quantity", dom));
					unit=checkNull(this.genericUtility.getColumnValue("unit", dom));
					itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
					rate=checkDoubleNull(this.genericUtility.getColumnValue("rate", dom));
					if(unitRate.equalsIgnoreCase(unit))
					{
						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+1+"]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+1+"]]>").append("</conv__rate_stduom>");
						valueXmlString.append("<rate__stduom >").append("<![CDATA["+rate+"]]>").append("</rate__stduom>");
						valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantity+"]]>").append("</quantity__stduom>");
					}
					else
					{

						System.out.print("unit unitStd itemCode quantity"+unit+""+unitStd+""+quantity);
						ArrayList convQtyList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, quantity, 0, conn);					
						System.out.println("convQtyList = "+convQtyList);

						convQtuomStduom = Double.parseDouble(convQtyList.get(0).toString());
						System.out.println("meffQty ["+convQtuomStduom+"]");

						quantityStduom = Double.parseDouble(convQtyList.get(1).toString());
						System.out.println("meffQty ["+quantityStduom+"]");

						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</quantity__stduom>");
						//valueXmlString.append("<rate__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</rate__stduom>");

						ArrayList convRateList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, 0, conn);					
						System.out.println("convRateList = "+convRateList);

						convQtuomStduom = Double.parseDouble(convRateList.get(0).toString());
						System.out.println("meffQty ["+convQtuomStduom+"]");

						rateStduom = Double.parseDouble(convRateList.get(1).toString());
						System.out.println("meffQty ["+quantityStduom+"]");

						valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__rate_stduom>");
						//valueXmlString.append("<quantity__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</quantity__stduom>");
						valueXmlString.append("<rate__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");

					}
				}

				else if (currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					quantity=checkDoubleNull(this.genericUtility.getColumnValue("quantity", dom));
					rate=checkDoubleNull(this.genericUtility.getColumnValue("rate", dom));
					sql = "select descr,std_exrt from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, currCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descrCurr = checkNull(rs.getString(1));
						stdExrt = rs.getDouble(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					amount = quantity*rate;
					amountBc = amount*stdExrt;

					System.out.println("amountsssssssss"+quantity+""+rate+""+stdExrt+""+amount+""+amountBc);
					valueXmlString.append("<amount>").append("<![CDATA["+amount+"]]>").append("</amount>");
					valueXmlString.append("<amount__bc>").append("<![CDATA["+amountBc+"]]>").append("</amount__bc>");	

					valueXmlString.append("<currency_descr>").append("<![CDATA["+descrCurr+"]]>").append("</currency_descr>");
					valueXmlString.append("<exch_rate>").append("<![CDATA["+stdExrt+"]]>").append("</exch_rate>");
					valueXmlString.append("<rate__stduom >").append("<![CDATA["+rate+"]]>").append("</rate__stduom>");
					valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantity+"]]>").append("</quantity__stduom>");

				}	
				else if(currentColumn.trim().equalsIgnoreCase("quantity") || currentColumn.trim().equalsIgnoreCase("rate"))
				{
					System.out.println(" 333333333333amountsssssssss"+quantity+""+rate+""+stdExrt+""+amount+""+amountBc);
					quantity=checkDoubleNull(this.genericUtility.getColumnValue("quantity", dom));
					rate=checkDoubleNull(this.genericUtility.getColumnValue("rate", dom));
					stdExrt=checkDoubleNull(this.genericUtility.getColumnValue("exch_rate", dom));
					amount = quantity*rate;
					amountBc = amount*stdExrt;

					System.out.println("amountsssssssss"+quantity+""+rate+""+stdExrt+""+amount+""+amountBc);

					valueXmlString.append("<amount>").append("<![CDATA["+amount+"]]>").append("</amount>");
					valueXmlString.append("<amount__bc>").append("<![CDATA["+amountBc+"]]>").append("</amount__bc>");	
					unitRate=checkNull(this.genericUtility.getColumnValue("unit__rate", dom));
					unit=checkNull(this.genericUtility.getColumnValue("unit", dom));
					itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));

					if(unitRate.equalsIgnoreCase(unit))
					{
						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+1+"]]>").append("</conv__qty_stduom>");
						valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+1+"]]>").append("</conv__rate_stduom>");
						valueXmlString.append("<rate__stduom >").append("<![CDATA["+rate+"]]>").append("</rate__stduom>");
						valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantity+"]]>").append("</quantity__stduom>");
					}
					else
					{

						System.out.print("unit unitStd itemCode quantity"+unit+""+unitStd+""+quantity);
						ArrayList convQtyList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, quantity, 0, conn);					
						System.out.println("convQtyList = "+convQtyList);

						convQtuomStduom = Double.parseDouble(convQtyList.get(0).toString());
						System.out.println("meffQty ["+convQtuomStduom+"]");

						quantityStduom = Double.parseDouble(convQtyList.get(1).toString());
						System.out.println("meffQty ["+quantityStduom+"]");

						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__qty_stduom>");
						//valueXmlString.append("<rate__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</rate__stduom>");
						valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</quantity__stduom>");

						ArrayList convRateList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, 0, conn);					
						System.out.println("convRateList = "+convRateList);

						convQtuomStduom = Double.parseDouble(convRateList.get(0).toString());
						System.out.println("meffQty ["+convQtuomStduom+"]");

						rateStduom = Double.parseDouble(convRateList.get(1).toString());
						System.out.println("meffQty ["+quantityStduom+"]");

						valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__rate_stduom>");
						//valueXmlString.append("<quantity__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</quantity__stduom>");
						valueXmlString.append("<rate__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");
					}

				}

				else if (currentColumn.trim().equalsIgnoreCase("supp_code__pref"))
				{

					salesPers = checkNull(genericUtility.getColumnValue("supp_code__pref", dom));
					sql = "select sh_name from supplier where supp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salesPers);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						shName = checkNull(rs.getString(1));									
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<supplier_sh_name>").append("<![CDATA["+shName+"]]>").append("</supplier_sh_name>");

				}
				else if (childNodeName.equalsIgnoreCase("proj_code"))
				{
					projCode=checkNull(this.genericUtility.getColumnValue("proj_code", dom));					
					sql = "select descr from project where proj_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, projCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descrProjCode = checkNull(rs.getString(1));									
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<project_descr>").append("<![CDATA["+descrProjCode+"]]>").append("</project_descr>");				
				}
				else if (currentColumn.trim().equalsIgnoreCase("pack_code"))
				{
					packCode=checkNull(this.genericUtility.getColumnValue("pack_code", dom));					
					sql = "select descr  from packing where pack_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, packCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descrPackCode = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<packing_descr >").append("<![CDATA["+descrPackCode+"]]>").append("</packing_descr>");
				}
				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}

					ctr++;
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN ["+currentColumn+"]");

				//for (ctr = 0; ctr < childNodeListLength; ctr++) //comment by kunal on 5/04/13
				//{
					//childNode = childNodeList.item(ctr);
					//childNodeName = childNode.getNodeName();
					
					if (currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						indentNo=checkNull(this.genericUtility.getColumnValue("indent_no", dom1));
						System.out.println("indent no is "+indentNo);
						sql = "SELECT REQ_DATE FROM INDENT WHERE IND_NO = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, indentNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							reqDate  = rs.getDate(1);	
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(reqDate == null)
						{
							valueXmlString.append("<req_date>").append("<![CDATA["+""+"]]>").append("</req_date>");
						}
						else
						{					
							valueXmlString.append("<req_date>").append("<![CDATA["+sdf.format(reqDate)+"]]>").append("</req_date>");

						}
					}
					else if (currentColumn.trim().equalsIgnoreCase("item_code"))
					{
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));	
						
						sql = "select descr ,item_ser ,unit ,unit__pur,unit__rate ,pack_code,tax_chap,tax_class from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descrItem = checkNull(rs.getString(1));
							itemSer = checkNull(rs.getString(2));
							unit = checkNull(rs.getString(3));
							unitPur = rs.getString(4)== null?"":rs.getString(3);
							unitRate = rs.getString(5)== null?"":rs.getString(3);
							packCode = rs.getString("pack_code")== null?"":rs.getString("pack_code");
							taxChap = rs.getString("tax_chap")== null?"":rs.getString("tax_chap");
							taxClass = rs.getString("tax_class")== null?"":rs.getString("tax_class");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<item_descr>").append("<![CDATA["+descrItem+"]]>").append("</item_descr>");
						valueXmlString.append("<item_ser>").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");
						valueXmlString.append("<unit>").append("<![CDATA["+unit+"]]>").append("</unit>");
						valueXmlString.append("<unit__std >").append("<![CDATA["+unit+"]]>").append("</unit__std>");
						valueXmlString.append("<unit__rate>").append("<![CDATA["+unitRate+"]]>").append("</unit__rate>");
						valueXmlString.append("<pack_code>").append("<![CDATA["+packCode+"]]>").append("</pack_code>");
						valueXmlString.append("<tax_chap>").append("<![CDATA["+taxChap+"]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class>").append("<![CDATA["+taxClass+"]]>").append("</tax_class>");

						sql = "select descr  from packing where pack_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, packCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descrPackCode = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<packing_descr >").append("<![CDATA["+descrPackCode+"]]>").append("</packing_descr>");

						
					}
					else if (currentColumn.trim().equalsIgnoreCase("unit__rate"))
					{
						System.out.println("unit__rate ITEM CHANGE  =");
						unitRate=checkNull(this.genericUtility.getColumnValue("unit__rate", dom));
						quantity=checkDoubleNull(this.genericUtility.getColumnValue("quantity", dom));
						unit=checkNull(this.genericUtility.getColumnValue("unit", dom));
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						rate=checkDoubleNull(this.genericUtility.getColumnValue("rate", dom));
						if(unitRate.equalsIgnoreCase(unit))
						{
							valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+1+"]]>").append("</conv__qty_stduom>");
							valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+1+"]]>").append("</conv__rate_stduom>");
							//changes done by cpandey  rate__stduom - rate,  quantity__stduom - quantity
							//valueXmlString.append("<rate__stduom >").append("<![CDATA["+quantity+"]]>").append("</rate__stduom>");
							valueXmlString.append("<rate__stduom >").append("<![CDATA["+rate+"]]>").append("</rate__stduom>");
							//valueXmlString.append("<quantity__stduom >").append("<![CDATA["+rate+"]]>").append("</quantity__stduom>");
							valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantity+"]]>").append("</quantity__stduom>");
						}
						else
						{

							System.out.print("unit unitStd itemCode quantity"+unit+""+unitStd+""+quantity);
							ArrayList convQtyList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, quantity, 0, conn);					
							System.out.println("convQtyList = "+convQtyList);

							convQtuomStduom = Double.parseDouble(convQtyList.get(0).toString());
							//arraylist for rate stduom
							ArrayList convRateList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, 0, conn);
							convQtuomStduom = Double.parseDouble(convRateList.get(0).toString());
							rateStduom = Double.parseDouble(convRateList.get(1).toString());
							System.out.println("meffQty ["+convQtuomStduom+"]");
							//changes done by cpandey for  rate__stduom -- rateStduom
							System.out.println("meffQty ["+rateStduom+"]");
							valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__qty_stduom>");
							//valueXmlString.append("<rate__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</rate__stduom>");
							valueXmlString.append("<rate__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");

							System.out.println("convRateList = "+convRateList);						
							System.out.println("meffQty ["+convQtuomStduom+"]");

							quantityStduom = Double.parseDouble(convQtyList.get(1).toString());
							System.out.println("meffQty ["+quantityStduom+"]");
							//changes done by cpandey for  quantity__stduom -- quantityStduom
							valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__rate_stduom>");
							//	valueXmlString.append("<quantity__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</quantity__stduom>");
							valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</quantity__stduom>");

						}
					}

					else if (currentColumn.trim().equalsIgnoreCase("quantity") || currentColumn.trim().equalsIgnoreCase("rate") )
					{
						quantity=checkDoubleNull(this.genericUtility.getColumnValue("quantity", dom));
						rate=checkDoubleNull(this.genericUtility.getColumnValue("rate", dom));
						amount = quantity*rate;
						System.out.println("amountsssssssss"+quantity+""+rate+""+stdExrt+""+amount+""+amountBc);
						valueXmlString.append("<amount>").append("<![CDATA["+amount+"]]>").append("</amount>");	


						unitRate=checkNull(this.genericUtility.getColumnValue("unit__rate", dom));
						unit=checkNull(this.genericUtility.getColumnValue("unit", dom));
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						if(unitRate.equalsIgnoreCase(unit))
						{
							valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+1+"]]>").append("</conv__qty_stduom>");
							valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+1+"]]>").append("</conv__rate_stduom>");
							valueXmlString.append("<rate__stduom >").append("<![CDATA["+rate+"]]>").append("</rate__stduom>");
							valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantity+"]]>").append("</quantity__stduom>");
						}
						else
						{

							System.out.print("unit unitStd itemCode quantity"+unit+""+unitStd+""+quantity);
							ArrayList convQtyList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, quantity, 0, conn);					
							System.out.println("convQtyList = "+convQtyList);

							convQtuomStduom = Double.parseDouble(convQtyList.get(0).toString());
							System.out.println("meffQty ["+convQtuomStduom+"]");

							quantityStduom = Double.parseDouble(convQtyList.get(1).toString());
							System.out.println("meffQty ["+quantityStduom+"]");

							valueXmlString.append("<conv__qty_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__qty_stduom>");
							//valueXmlString.append("<rate__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</rate__stduom>");
							valueXmlString.append("<quantity__stduom >").append("<![CDATA["+quantityStduom+"]]>").append("</quantity__stduom>");

							ArrayList convRateList  = distCommon.getConvQuantityFact(unit, unitRate, itemCode, rate, 0, conn);					
							System.out.println("convRateList = "+convRateList);

							convQtuomStduom = Double.parseDouble(convRateList.get(0).toString());
							System.out.println("meffQty ["+convQtuomStduom+"]");

							rateStduom = Double.parseDouble(convRateList.get(1).toString());
							System.out.println("meffQty ["+quantityStduom+"]");

							valueXmlString.append("<conv__rate_stduom >").append("<![CDATA["+convQtuomStduom+"]]>").append("</conv__rate_stduom>");
							//valueXmlString.append("<quantity__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</quantity__stduom>");
							valueXmlString.append("<rate__stduom >").append("<![CDATA["+rateStduom+"]]>").append("</rate__stduom>");
						}
					}

					else if (currentColumn.trim().equalsIgnoreCase("pack_code"))
					{
						packCode=checkNull(this.genericUtility.getColumnValue("pack_code", dom));					
						sql = "select descr  from packing where pack_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, packCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descrPackCode = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<packing_descr >").append("<![CDATA["+descrPackCode+"]]>").append("</packing_descr>");
					}


				//}


				valueXmlString.append("</Detail2>");

			}
			valueXmlString.append("</Root>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					if (pstmt != null)
						pstmt.close();
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return valueXmlString.toString();
			}



	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}

	private double checkDoubleNull(String str)
	{
		if(str == null || str.trim().length() == 0)
		{
			return 0.0;
		}
		else
		{
			return Double.parseDouble(str) ;
		}

	}

	private String errorType(Connection conn, String errorCode)throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
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
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}

}

