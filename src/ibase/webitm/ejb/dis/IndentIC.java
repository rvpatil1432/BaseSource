package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.SysCommon;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;
import javax.swing.text.html.parser.ParserDelegator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.CDATASection;

@javax.ejb.Stateless
public class IndentIC extends ValidatorEJB implements IndentICLocal, IndentICRemote 
{
	E12GenericUtility genericUtility = new E12GenericUtility();	
	DistCommon discommon = new DistCommon();
	FinCommon finCommon = new FinCommon();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	SysCommon sysCommon = new SysCommon();

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
		String errString = "", userId = "",  sql = "";
		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String mdate1 = "", mval = "", ls_site_code__budgets = "", ls_budget = "", ls_stop_busi = "", mval1 = "", mdate2 = "", lc_conv = "",
				ls_itemcode = "", mqty = "", mqty1 = "", ls_task_code = "", loginSite = "";

		int lc_porder_qty = 0, lc_indent_qty = 0, lc_total_qty = 0, lc_proj_est_qty = 0;

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");

		try
		{	
			int currentFormNo = 0, childNodeListLength = 0, ctr = 0, cnt = 0;
			String childNodeName = "", errorType = "", errCode = "";
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();

			conn = getConnection();

			genericUtility = new E12GenericUtility();	
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			loginSite = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));	

			System.out.println("xtraParam----->>["+xtraParams+"]");
			System.out.println("editFlag ------------>>["+editFlag+"]");


			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo  = Integer.parseInt(objContext);
			}	

			switch (currentFormNo)  
			{
			case 1:
				System.out.println("------in detail1 validation----------------");
				System.out.println("DOM in case 1 wfValData ---->>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1 in case 1 wfValData ----->>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 in case 1 wfValData ----->>["+genericUtility.serializeDom(dom2).toString()+"]");	

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength  = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{					
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName ------->>["+childNodeName+"]");


					if("ind_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside ind_date wfValData -------------- ");
						mdate1 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom));
						mval = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));

						java.sql.Timestamp indDate = Timestamp.valueOf(genericUtility.getValidDateString( mdate1, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//getDateInAppFormat( mdateStr );
						//Changes and Commented By Ajay on 20-12-2017 :START
						//errCode = SysCommon.nfCheckPeriod( "PUR", indDate, mval, conn ); 
						errCode=finCommon.nfCheckPeriod( "PUR", indDate, mval, conn);
						//Changes and Commented By Ajay on 20-12-2017 :END
						System.out.println("----------- Inside ind_date errCode -------------- "+errCode);
						if(errCode.length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if("site_code".equalsIgnoreCase(childNodeName) || "site_code__dlv".equalsIgnoreCase(childNodeName) || 
							"site_code__acct".equalsIgnoreCase(childNodeName) || "site_code__bil".equalsIgnoreCase(childNodeName) || 
							"site_code__des".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside site-code wfValData -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));						
						cnt = getCount("SITE", "SITE_CODE", mval, conn);

						if(cnt == 0)
						{
							errCode = "VMSITE1";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if(cnt > 0)
						{
							mval = checkNullAndTrim(genericUtility.getColumnValue("site_code__acct", dom));

							if(mval.length() > 0)
							{
								sql = "SELECT SITE_CODE__BUDGET FROM SITE WHERE SITE_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mval);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									ls_site_code__budgets = checkNullAndTrim(rs.getString("SITE_CODE__BUDGET"));
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

								if(ls_site_code__budgets.length() > 0)
								{
									sql = "SELECT BUDGET_ID FROM SITE WHERE SITE_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_site_code__budgets);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ls_budget = checkNullAndTrim(rs.getString("BUDGET_ID"));
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

									if(ls_budget.length() == 0)
									{
										errCode = "VTBUSITEAC";		
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}

					if("dept_code".equalsIgnoreCase(childNodeName))
					{
						mval = checkNullAndTrim(genericUtility.getColumnValue("dept_code", dom));
						cnt = getCount("DEPARTMENT", "DEPT_CODE", mval, conn);

						if(cnt == 0)
						{
							errCode = "VTDEPT1";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("item_code__mfg".equalsIgnoreCase(childNodeName))
					{
						cnt = 0;
						mval = checkNullAndTrim(genericUtility.getColumnValue("item_code__mfg", dom));

						if(mval.length() > 0)
						{	
							cnt = getCount("ITEM", "ITEM_CODE", mval, conn);

							if(cnt == 0)
							{
								//Changed by wasim on 13-04-2017 for changing error code for item_code__mfg
								//errCode = "VMITEM1";
								errCode = "VMITEMMFG";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}
					if("item_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside item_code -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));

						sql = "SELECT (CASE WHEN STOP_BUSINESS IS NULL THEN 'N' ELSE STOP_BUSINESS END) AS STOP_BUSINESS FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_stop_busi = checkNullAndTrim(rs.getString("STOP_BUSINESS"));
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

						if("N".equalsIgnoreCase(ls_stop_busi))
						{
							errCode = itmDBAccessEJB.isItem(mval1, mval,  "", conn); 
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}							
						}
						else
						{
							errCode = "VTIIC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("unit__ind".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside unit -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("unit__ind", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("unit__std", dom));

						sql = "SELECT COUNT(*) FROM UOM WHERE UNIT = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval);
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
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println("----------- Inside unit__ind errCode -------------- "+errCode);

						}
						else if(!mval.trim().equalsIgnoreCase(mval1.trim()))
						{
							sql = "SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							pstmt.setString(2, mval1);
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
								sql = "SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mval1);
								pstmt.setString(2, mval);
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
									errCode = "VTUNIT3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("----------- Inside unit__ind errCode 1111111 -------------- "+errCode);
								}
							}
						}
					}
					if("req_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside req_date -------------- ");
						mdate1 = checkNullAndTrim(genericUtility.getColumnValue("req_date", dom));
						mdate2 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom1));

						mdate1 = (mdate1 == null || mdate1.length() == 0) ? "" : mdate1;
						mdate2 = (mdate2 == null || mdate2.length() == 0) ? "" : mdate2;

						if(mdate1.trim().length() > 0 && mdate2.length() > 0)
						{
							Date req_date = sdf.parse(mdate1);
							Date ind_date = sdf.parse(mdate2);

							if(req_date.before(ind_date))
							{
								errCode = "VLINDENT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("----------- Inside req_date errCode 1111111 -------------- "+errCode);
							}
						}
					}

					if("proj_code".equalsIgnoreCase(childNodeName))
					{

						mval = checkNullAndTrim(genericUtility.getColumnValue("proj_code", dom));
						System.out.println("----------- Inside proj_code -------------- " + mval);

						if(mval.length() > 0)
						{
							sql = "SELECT PROJ_STATUS FROM PROJECT WHERE PROJ_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mval1 = checkNullAndTrim(rs.getString("PROJ_STATUS"));
							}
							else
							{
								errCode = "PROJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("----------- Inside proj_code errCode 1111111 -------------- "+errCode);
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

							if("C".equalsIgnoreCase(mval1) && "A".equalsIgnoreCase(editFlag))
							{
								errCode = "PROJ2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("----------- Inside proj_code errCode 222222222 -------------- "+errCode);
							}
						}
					}

					if("work_order".equalsIgnoreCase(childNodeName))
					{
						mval = checkNullAndTrim(genericUtility.getColumnValue("work_order", dom));

						System.out.println("----------- Inside work_order -------------- " + mval);

						if(mval.length() > 0)
						{
							sql = "SELECT STATUS FROM WORKORDER WHERE WORK_ORDER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mval1 = checkNullAndTrim(rs.getString("STATUS"));
							}
							else
							{
								errCode = "VTWORDER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("----------- Inside work_order errCode 1111111 -------------- "+errCode);
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

							if("C".equalsIgnoreCase(mval1) && "A".equalsIgnoreCase(editFlag))
							{
								errCode = "VTWORDER2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("----------- Inside work_order errCode 222222222 -------------- "+errCode);
							}
						}
					}

					if("acct_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside acct_code -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("acct_code", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));

						errCode = finCommon.isAcctCode(mval1, mval, "" , conn);
						if(errCode.length() > 0)
						{
							System.out.println("----------- ErrCode inside acct_code ---------------"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("cctr_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside cctr_code -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("cctr_code", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("acct_code", dom));

						errCode = finCommon.isCctrCode(mval1, mval, "", conn);
						System.out.println("----------- ErrCode inside cctr_code ---------------"+errCode);
						if(errCode.length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("anal_code".equalsIgnoreCase(childNodeName))	
					{/*
						System.out.println("----------- Inside anal_code -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("anal_code", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("acct_code", dom));	

						if(mval.length() > 0)
						{
							errCode = finCommon.isAnalysis(mval1, mval, "", conn);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					 */}

					if("emp_code__iapr".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside emp_code_iapr -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("emp_code__iapr", dom));
						//Changes by Nandkumar gadkari on 23/03/18 To remove mandatory purchaser and approver employee code------------Start-------------
						/*if(mval.length() == 0)
						{
							//Changed by wasim on 13-apr-2017 for changing error code 
							//errCode = "VMEMP1";
							errCode = "VMINDAPRV";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{*/

						if(mval.length() > 0)
						{
							mval1 = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
							errCode = getEmployeeResig(mval, mval1, conn);
							System.out.println("----------- ErrCode inside emp_code__iapr ---------------"+errCode);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}

					if("emp_code__pur".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside emp_code__pur -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("emp_code__pur", dom));

						/*if(mval.length() == 0)
						{
							//Changed by wasim on 13-apr-2017 for changing error code 
							//errCode = "VMEMP2";
							errCode = "VMINEMPPUR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{*/	
						if(mval.length() > 0)
						{
							mval1 = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
							errCode = getEmployeeResig(mval, mval1, conn);
							System.out.println("----------- ErrCode inside emp_code__pur ---------------"+errCode);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Changes by Nandkumar gadkari on 23/03/18 To remove mandatory purchaser and approver employee code-------End-------------
					}

					if("conv__qty_stduom".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside conv__qty_stduom -------------- ");
						lc_conv = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", dom));
						mval = checkNullAndTrim(genericUtility.getColumnValue("unit__ind", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("unit__std", dom));

						if(mval.equalsIgnoreCase(mval1) && !"1".equalsIgnoreCase(lc_conv))
						{
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(!mval.equalsIgnoreCase(mval1))
						{
							ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
							//double conQty = discommon.convQtyFactor(mval, mval1, ls_itemcode, Double.parseDouble(lc_conv), conn);
							//errcode = gf_check_conv_fact(ls_itemcode, mval, mval1, lc_conv)
						}
					}
					if("max_rate".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside max_rate -------------- ");
						mqty = checkNullAndTrim(genericUtility.getColumnValue("max_rate", dom));
						mqty1 = checkNullAndTrim(genericUtility.getColumnValue("purc_rate", dom));

						mqty = (mqty.length() == 0) ? "0" : mqty;
						mqty1 = (mqty1.length() == 0) ? "0" : mqty1;

						if(Integer.parseInt(mqty) == 0)
						{
							if(Integer.parseInt(mqty) < Integer.parseInt(mqty1))
							{
								errCode = "VTMAXRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if("quantity".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside quantity -------------- ");
						mqty = checkNullAndTrim(genericUtility.getColumnValue("quantity", dom));
						ls_task_code = checkNullAndTrim(genericUtility.getColumnValue("task_code", dom));

						System.out.println(" ls_task_code ---------->>"+ls_task_code);

						if(ls_task_code.length() > 0 )
						{
							ls_itemcode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));

							sql = "SELECT SUM(B.QUANTITY) AS QTY FROM PORDER A , PORDDET B WHERE A.PURC_ORDER = B.PURC_ORDER AND A.CONFIRMED = ? " +
									"AND A.TASK_CODE = ? AND B.ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, "Y");
							pstmt.setString(2, ls_task_code);
							pstmt.setString(3, ls_itemcode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								lc_porder_qty = rs.getInt("QTY");
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


							sql = "SELECT SUM(QUANTITY) AS QTY FROM INDENT WHERE TASK_CODE = ? AND STATUS = ? AND ITEM_CODE = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_task_code);
							pstmt.setString(2, "A");
							pstmt.setString(3, ls_itemcode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								lc_indent_qty = rs.getInt("QTY");
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
							mqty = (mqty.length() == 0) ? "0" : mqty;
							lc_total_qty= lc_indent_qty + lc_porder_qty + Integer.parseInt(mqty);

							sql = "SELECT SUM(QUANTITY) AS QTY FROM PROJ_EST_BSL_ITEM WHERE TASK_CODE = ? AND ITEM_CODE = ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_task_code);
							pstmt.setString(2, ls_itemcode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								lc_proj_est_qty = rs.getInt("QTY");
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

							if(lc_total_qty > lc_proj_est_qty)
							{
								errCode = "VTTASK2";
								errList.add(errCode);
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
					errString = itmDBAccessEJB.getErrorString( errFldName, errCode, userId ,"",conn);
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
			System.out.println ( "Exception: IndentIC: wfValData( Document currFormDataDom ): " + e.getMessage() + ":" );
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
				System.out.println( "Exception : IndentIC:wfValData : " + e.getMessage() );
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
			System.out.println ( "Exception : IndentIC:itemChanged(String,String):" + e.getMessage() + ":" );
			throw new ITMException(e);
		}
		System.out.println ( "returning from IndentIC: itemChanged \n[" + errString + "]" );

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
		String sql = "", currDate = "", loginSite = "", loginEmpCode = "", ecode = "", efname = "", elname = "", deptcode = "", descr1 = "", 
				descr = "", mval = "", mval1 = "", mval2 = "", ls_anal = "", msite = "", ls_acct = "", ls_cctr = "", ls_dept = "", ld_prate = "", 
				ls_val3 = "", ls_budget_amt_anal = "", ls_consumed_amt_anal = "", itemunit = "", ls_unit_std = "", qtyStr = "", lc_conv1 = "";
		double lc_conv = 0.0;
		ArrayList qty = new ArrayList();


		System.out.println("xtraParams=["+xtraParams+"]");
		System.out.println("currentColumn inside itemChanged................. : ["+currentColumn+"]");
		System.out.println("currentFormNo inside itemChanged................. : ["+currentFormNo+"]");

		valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?><Root><Header><editFlag>" );
		valueXmlString.append( editFlag ).append( "</editFlag></Header>" );
		try
		{
			conn = getConnection();
			loginSite = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));	
			loginEmpCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));	

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
					System.out.println("------- Inside itm_default -------------");
					valueXmlString.append( "<ind_date><![CDATA[" ).append(currDate).append( "]]></ind_date>\r\n" );
					valueXmlString.append( "<req_date><![CDATA[" ).append(currDate).append( "]]></req_date>\r\n" );
					valueXmlString.append( "<site_code><![CDATA[" ).append(loginSite).append( "]]></site_code>\r\n" );

					sql = "SELECT EMP_CODE, EMP_FNAME, EMP_LNAME, DEPT_CODE FROM EMPLOYEE WHERE EMP_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginEmpCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ecode = checkNullAndTrim(rs.getString("EMP_CODE"));
						efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
						elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
						deptcode = checkNullAndTrim(rs.getString("DEPT_CODE"));

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

					valueXmlString.append( "<emp_code__req><![CDATA[" ).append(ecode).append( "]]></emp_code__req>\r\n" );
					valueXmlString.append( "<employee_emp_fname><![CDATA[" ).append(efname).append( "]]></employee_emp_fname>\r\n" );
					valueXmlString.append( "<employee_emp_lname><![CDATA[" ).append(elname).append( "]]></employee_emp_lname>\r\n" );
					valueXmlString.append( "<dept_code><![CDATA[" ).append(deptcode).append( "]]></dept_code>\r\n" );

					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr1 = checkNullAndTrim(rs.getString("DESCR"));
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
					valueXmlString.append( "<site_descr><![CDATA[" ).append(descr1).append( "]]></site_descr>\r\n" );

					sql = "SELECT DESCR FROM DEPARTMENT WHERE DEPT_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, deptcode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr1 = checkNullAndTrim(rs.getString("DESCR"));
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
					valueXmlString.append( "<department_descr><![CDATA[" ).append(descr1).append( "]]></department_descr>\r\n" );
				}
				if( currentColumn.trim().equalsIgnoreCase( "site_code" ) )
				{

					mval = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));

					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr1 = checkNullAndTrim(rs.getString("DESCR"));
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
					valueXmlString.append( "<site_descr><![CDATA[" ).append(descr).append( "]]></site_descr>\r\n" );
				}
				if( currentColumn.trim().equalsIgnoreCase( "item_code__mfg" ) )
				{
					mval = checkNullAndTrim(genericUtility.getColumnValue("item_code__mfg", currFormDataDom));

					sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr1 = checkNullAndTrim(rs.getString("DESCR"));
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
					//Changed by wasim on 17-04-17 for setting item code mfg description 
					//valueXmlString.append( "<itemmfg_desc><![CDATA[" ).append(descr).append( "]]></itemmfg_desc>\r\n" );
					valueXmlString.append( "<itemmfg_desc><![CDATA[" ).append(descr1).append( "]]></itemmfg_desc>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase("item_code")) // Field found non editable.
				{		
					System.out.println("------- Inside item_code -------------");
					mval = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					msite = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					ls_anal = checkNullAndTrim(genericUtility.getColumnValue("anal_code", currFormDataDom));
					ls_acct = checkNullAndTrim(genericUtility.getColumnValue("acct_code", currFormDataDom));
					ls_cctr = checkNullAndTrim(genericUtility.getColumnValue("cctr_code", currFormDataDom));
					ls_dept = checkNullAndTrim(genericUtility.getColumnValue("dept_code", currFormDataDom));


					sql = "SELECT DESCR, UNIT, PURC_RATE, SUPP_CODE__PREF FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mval1 = checkNullAndTrim(rs.getString("DESCR"));
						mval2 = checkNullAndTrim(rs.getString("UNIT"));
						ld_prate = checkNullAndTrim(rs.getString("PURC_RATE"));
						ls_val3 = checkNullAndTrim(rs.getString("SUPP_CODE__PREF"));
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
					valueXmlString.append( "<item_descr><![CDATA[" ).append(mval1).append( "]]></item_descr>\r\n" );
					valueXmlString.append( "<unit__ind><![CDATA[" ).append(mval2).append( "]]></unit__ind>\r\n" );
					valueXmlString.append( "<purc_rate><![CDATA[" ).append(ld_prate).append( "]]></purc_rate>\r\n" );
					valueXmlString.append( "<supp_code__pref><![CDATA[" ).append(ls_val3).append( "]]></supp_code__pref>\r\n" );

					sql = " SELECT FN_GET_BUDGET_AMT(?,?,?,?,?,?,?) AS LS_BUDGET_AMT_ANAL, FN_GET_CONS_AMT(?,?,?,?,?,?,?) AS LS_CONSUMED_AMT_ANAL " 
							+ " FROM DUAL ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "R-IND");
					pstmt.setString(2, msite);
					pstmt.setString(3, ls_acct);
					pstmt.setString(4, ls_cctr);
					pstmt.setString(5, ls_anal);
					pstmt.setString(6, ls_dept);
					pstmt.setString(7, "A");
					pstmt.setString(8, "R-IND");
					pstmt.setString(9, msite);
					pstmt.setString(10, ls_acct);
					pstmt.setString(11, ls_cctr);
					pstmt.setString(12, ls_anal);
					pstmt.setString(13, ls_dept);
					pstmt.setString(14, "A");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_budget_amt_anal = checkNullAndTrim(rs.getString("LS_BUDGET_AMT_ANAL"));
						ls_consumed_amt_anal = checkNullAndTrim(rs.getString("LS_CONSUMED_AMT_ANAL"));
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

					double lc_budget_amt = Double.parseDouble(ls_budget_amt_anal) - Double.parseDouble(ls_consumed_amt_anal);

					valueXmlString.append( "<budget_amt_anal><![CDATA[" ).append(ls_budget_amt_anal).append( "]]></budget_amt_anal>\r\n" );
					valueXmlString.append( "<consumed_amt_anal><![CDATA[" ).append(ls_consumed_amt_anal).append( "]]></consumed_amt_anal>\r\n" );
					valueXmlString.append( "<budget_amt><![CDATA[" ).append(lc_budget_amt).append( "]]></budget_amt>\r\n" );

				}
				if( currentColumn.trim().equalsIgnoreCase( "dept_code" ) )
				{
					mval = checkNullAndTrim(genericUtility.getColumnValue("dept_code", currFormDataDom));

					sql = "SELECT DESCR FROM DEPARTMENT WHERE DEPT_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = checkNullAndTrim(rs.getString("DESCR"));
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
					valueXmlString.append( "<department_descr><![CDATA[" ).append(descr).append( "]]></department_descr>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase( "emp_code__req" ) )
				{
					mval = checkNullAndTrim(genericUtility.getColumnValue("emp_code__req", currFormDataDom));

					sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mval1 = checkNullAndTrim(rs.getString("EMP_FNAME"));
						mval2 = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
					valueXmlString.append( "<employee_emp_fname><![CDATA[" ).append(mval1).append( "]]></employee_emp_fname>\r\n" );
					valueXmlString.append( "<employee_emp_lname><![CDATA[" ).append(mval2).append( "]]></employee_emp_lname>\r\n" );
				}	
				if( currentColumn.trim().equalsIgnoreCase( "pack_code" ) )
				{
					System.out.println("------- Inside pack_code -------------");
					mval = checkNullAndTrim(genericUtility.getColumnValue("pack_code", currFormDataDom));

					sql = "SELECT DESCR FROM PACKING WHERE PACK_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mval1 = checkNullAndTrim(rs.getString("DESCR"));
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
					valueXmlString.append( "<packing_descr><![CDATA[" ).append(mval1).append( "]]></packing_descr>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase( "supp_code__pref" ) )
				{
					System.out.println("------- Inside supp_code__pref -------------");
					mval = checkNullAndTrim(genericUtility.getColumnValue("supp_code__pref", currFormDataDom));

					sql = "SELECT SUPP_NAME FROM SUPPLIER WHERE SUPP_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mval1 = checkNullAndTrim(rs.getString("SUPP_NAME"));
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
					valueXmlString.append( "<supplier_supp_name><![CDATA[" ).append(mval1).append( "]]></supplier_supp_name>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase("unit__ind"))
				{
					System.out.println("------- Inside unit__ind -------------");

					itemunit = checkNullAndTrim(genericUtility.getColumnValue("unit__ind", currFormDataDom));
					ls_unit_std = checkNullAndTrim(genericUtility.getColumnValue("unit__std", currFormDataDom));
					mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					qtyStr = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));
					lc_conv = 0;

					qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr),  lc_conv, conn);

					/*valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></quantity__stduom>\r\n" );
					valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(lc_conv).append( "]]></conv__qty_stduom>\r\n" );*/

					// start - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );
					valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></conv__qty_stduom>\r\n" );
					// End - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]


				}

				if( currentColumn.trim().equalsIgnoreCase( "conv__qty_stduom" ) )
				{
					System.out.println("------- Inside conv__qty_stduom -------------");
					lc_conv1 = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", currFormDataDom));
					itemunit = checkNullAndTrim(genericUtility.getColumnValue("unit__ind", currFormDataDom));
					ls_unit_std = checkNullAndTrim(genericUtility.getColumnValue("unit__std", currFormDataDom));
					mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					qtyStr = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));

					qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr), lc_conv, conn);

					//valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></quantity__stduom>\r\n" );
					// start - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );
					// End - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]

				}	

				if( currentColumn.trim().equalsIgnoreCase("quantity"))
				{
					System.out.println("------- Inside quantity -------------");
					qtyStr = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));
					mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					ls_unit_std = checkNullAndTrim(genericUtility.getColumnValue("unit__std", currFormDataDom));
					lc_conv1 = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", currFormDataDom));
					itemunit = checkNullAndTrim(genericUtility.getColumnValue("unit__ind", currFormDataDom));
					String mNum2 = lc_conv1;

					if(itemunit.length() == 0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemunit = checkNullAndTrim(rs.getString("UNIT"));
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
						qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr),  Double.parseDouble(lc_conv1), conn);
						valueXmlString.append( "<unit__ind><![CDATA[" ).append(itemunit).append( "]]></unit__ind>\r\n" );
					}
					else
					{
						qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr),  Double.parseDouble(lc_conv1), conn);
					}

					if("0".equalsIgnoreCase(mNum2))
					{
						// start - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
						//valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(lc_conv1).append( "]]></conv__qty_stduom>\r\n" );
						valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></conv__qty_stduom>\r\n" );
					}
					//valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></quantity__stduom>\r\n" );
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );
					// End - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
				}	

				//added by manish mhatre on 16-04-21
				//start manish
				if(currentColumn.trim().equalsIgnoreCase("no_art") || currentColumn.trim().equalsIgnoreCase("dimension"))
				{
					System.out.println("Inside no_art block or dimension block");
					String itemCode="",noArtStr="",unit="",dimension="";
					double noArt=0,quantity=0;
					String reStr="";
					int pos=0;

					itemCode= genericUtility.getColumnValue("item_code", currFormDataDom);
					dimension=genericUtility.getColumnValue("dimension", currFormDataDom);
					noArtStr= genericUtility.getColumnValue("no_art", currFormDataDom);


					System.out.println("item code>>"+itemCode+"\ndimension>>"+dimension+"\nno of articles>>"+noArtStr);


					if(dimension!=null && dimension.trim().length()>0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							unit = rs.getString("UNIT");
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
						System.out.println("unit>>"+unit);

						if(noArtStr!=null && noArtStr.trim().length()>0)
						{
							noArt=Double.parseDouble(noArtStr);
						}
						else
						{
							noArt=1;
						}
						System.out.println("dimension>>"+dimension+"\n no of articles>>"+noArt);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							quantity=discommon.getQuantity(dimension,noArt,unit,conn);

							System.out.println("quantity in dimension block>>"+quantity);
							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(currFormDataDom,"quantity" , getAbsString(String.valueOf(quantity)));
							reStr = itemChanged(currFormDataDom, hdrDataDom, allFormDataDom, objContext, "quantity", editFlag, xtraParams);
							System.out.println("after quantity itemchanged 1440.......");
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
					}
				}
				//end manish

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

	//added by manish mhatre on 16-4-21
	//start manish
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
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}
	//end manish

	public int getCount(String tableName, String columnName, String columnVal, Connection conn)
	{
		int count = 0;
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			sql = "SELECT COUNT(1) FROM "+tableName+" WHERE "+columnName+" = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, columnVal);
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

		}
		catch(Exception e)
		{
			System.out.println( "Exception : getCount : " + e.getMessage() );
		}
		finally
		{
			try
			{
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
				System.out.println( "Exception : getCount : " + e.getMessage() );
			}
		}
		return count;
	}

	/*	public String getAcct(String siteCode, String acctCode, Connection conn)
	{
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int count = 0;
		String ls_sitespec = "", errCode = "", mvar_value = "", mVal = "";

		try
		{		
			sql = "SELECT VAR_VALUE FROM FINPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "999999");
			pstmt.setString(2, "SITE_SPECIFIC_ACCT");
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_sitespec = checkNullAndTrim(rs.getString("VAR_VALUE"));
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

			sql = "SELECT COUNT(*) FROM ACCOUNTS WHERE ACCT_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, acctCode);
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

			if(count != 0)
			{
				sql = "SELECT ACTIVE FROM ACCOUNTS WHERE ACCT_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, acctCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					 mVal = checkNullAndTrim(rs.getString("ACTIVE"));
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

				if(!"Y".equalsIgnoreCase(mVal))
				{
					errCode = "VMACCTA";
				}
				else
				{
					if("Y".equalsIgnoreCase(ls_sitespec) && ls_sitespec.length() > 0)
					{
						sql = "SELECT COUNT(*) FROM SITE_ACCOUNT WHERE SITE_CODE = ? AND ACCT_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, acctCode);
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
							errCode = "VMACCT3";
						}
					}
				}
			}
			else
			{
				errCode = "VMACCT1";
			}
		}
		catch(Exception e)
		{
			System.out.println( "Exception : getAcct : " + e.getMessage() );
		}
		finally
		{
			try
			{
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
				System.out.println( "Exception : getAcct : " + e.getMessage() );
			}
		}
		return errCode;
	}

	public String getCctr(String ccrtCode, String acctCode, Connection conn)
	{
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int count = 0;
		String ls_finparam_ccterm = "", errCode = "";

		try
		{

			ls_finparam_ccterm = finCommon.getFinparams("999999", "CCTR_CHECK", conn);

			if("Y".equalsIgnoreCase(ls_finparam_ccterm))
			{
				if(ccrtCode.length() > 0)
				{
					sql = "SELECT COUNT(*) FROM COSTCTR WHERE CCTR_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ccrtCode);
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
						errCode = "VMCCTR1";
					}


					sql = "SELECT COUNT(*) FROM ACCOUNTS_CCTR WHERE ACCT_CODE = ? AND CCTR_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, acctCode);
					pstmt.setString(2, ccrtCode);
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
					if(count > 0)
					{
						errCode = "VMCCTR2";
					}
					else if(count == 0)
					{
						if(ccrtCode.length() > 0)
						{
							errCode = "VMCCTR2";
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println( "Exception : getCctr : " + e.getMessage() );
		}
		finally
		{
			try
			{
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
				System.out.println( "Exception : getAcct : " + e.getMessage() );
			}
		}
		return errCode;
	}

	public String getAnalysis(String analCode, String acctCode, Connection conn)
	{
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int count = 0;
		String errCode = "";

		try
		{
			if(analCode.length() > 0)
			{
				sql = "SELECT COUNT(*) FROM ANALYSIS WHERE ANAL_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, analCode);
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
					errCode = "VMANAL1";
				}
			}

			sql = "SELECT COUNT(*) FROM ACCOUNTS_ANALYSIS WHERE ACCT_CODE = ? AND ANAL_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, acctCode);
			pstmt.setString(2, analCode);
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
				sql = "SELECT COUNT(*) FROM ACCOUNTS_ANALYSIS WHERE ACCT_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, acctCode);
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
				if(count > 0)
				{
					errCode = "VMANAL2";
				}
				else if(count > 0)
				{
					if(analCode.length() > 0)
					{
						errCode = "VMANAL2";
					}
				}
			}

		}
		catch(Exception e)
		{
			System.out.println( "Exception : getAnalysis : " + e.getMessage() );
		}
		finally
		{
			try
			{
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
				System.out.println( "Exception : getAnalysis : " + e.getMessage() );
			}
		}
		return errCode;
	}*/

	public String getEmployeeResig(String empCodeIapr, String siteCode, Connection conn)
	{
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int count = 0;
		String errCode = "", ld_resig_date = "", ls_with_held = "";

		try
		{
			sql = "SELECT COUNT(*) FROM EMPLOYEE WHERE EMP_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, empCodeIapr);
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
				//Changed by wasim on 13-apr-2017 for changing error code 
				//errCode = "VMEMP9";
				errCode = "VMEMP1";
			}
			else
			{
				sql = "SELECT RESI_DATE, WITH_HELD FROM EMPLOYEE WHERE EMP_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, empCodeIapr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ld_resig_date = checkNullAndTrim(rs.getString("RESI_DATE"));
					ls_with_held = checkNullAndTrim(rs.getString("WITH_HELD"));

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
				if(ld_resig_date != null && ld_resig_date.length() > 0)
				{
					errCode = "VMEMP7";
				}
				else if("Y".equalsIgnoreCase(ls_with_held))
				{
					errCode = "VMEMP9";
				}
			}


		}
		catch(Exception e)
		{
			System.out.println( "Exception : getEmployeeResig : " + e.getMessage() );
		}
		finally
		{
			try
			{
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
				System.out.println( "Exception : getEmployeeResig : " + e.getMessage() );
			}
		}
		return errCode;
	}
}
