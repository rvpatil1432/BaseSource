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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.CDATASection;

@javax.ejb.Stateless
public class IndentReqIC extends ValidatorEJB implements IndentICLocal, IndentICRemote 
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
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)throws ITMException
	{   				
		System.out.println("wfValData inside ----->>");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		E12GenericUtility genericUtility;
		String errString = "", userId = "",  sql = "", sql1 = "";
		int count = 0;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;

		String mdate1 = "", mval = "", ls_stop_busi = "", mval1 = "", lc_conv = "", ls_itemcode = "", mqty = "",  ls_task_code = "", loginSite = "", ls_type_allow_projbudgt_list = "", ls_indent = "",
				ls_ind_type = "", ls_item_ser = "", ls_proj_code = "", ls_dis_ind_type_list = "", ls_dis_dept_code_list = "", ls_proj_type = "", 
				ls_proj_type_opt = "", msite = "", ll_line = "", ls_del_site = "", ls_eou = "", ls_itemser = "", ls_stk_opt = "", mval2 = "", 
				mrate = "",   mmax_rate = "", ll_lineno = "", lc_approxcost = "", ls_old_indno = "", ls_temp = "", lc_purc_rate = "", 
				lc_last_pur_rate = "", ls_emp = "", ls_item = "", ls_qc_reqd = "", mstat = "", ldt_consdate = "", ldt_reqdate = "", ls_lop_reqd = "" ;
		boolean lb_ord_flag = true, lb_ind_type_flag = true;
		int ll_proj_cnt = 0, ll_count = 0,  ll_cnt = 0;
		double mqty2 = 0, lc_tot_amt = 0, lc_ind_amount = 0, lc_po_amt1 = 0, lc_porcp_amt = 0, lc_pret_amt = 0, lc_tot_poamt = 0, lc_po_amt = 0, 
				lc_tot_poamt11 = 0, lc_po_amt12 = 0, lc_porcp_amt11 = 0 , lc_pret_amt11 = 0, lc_prev_indent = 0, lc_tot_amt_proj = 0, 
				lc_exceed_amt = 0 ;
		String [] ls_type_allow_projbudgt = null, ls_dis_dept_code = null, ls_dis_ind_type = null, ls_proj_type_opt_list = null;

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		Date ldt_consdate1 = null, mdate = null;

		try
		{	
			int currentFormNo = 0, childNodeListLength = 0, ctr = 0, cnt = 0;
			String childNodeName = "", errorType = "", errCode = "";
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();

			conn = getConnection();

			genericUtility = new E12GenericUtility();	
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currDate = new Date();
			loginSite = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));	
			//Changes by nandkumar gadkari on 29/03/18 change in date  format -----start----------
			/*currDate = new java.sql.Timestamp(System.currentTimeMillis());
			currDate = sdf.parse(currDate.toString());*/
			Timestamp	currDate1 = new java.sql.Timestamp(System.currentTimeMillis());
			currDate = sdf.parse(sdf.format(currDate1));
			//Changes by nandkumar gadkari on 29/03/18 change in date  format -----end----------


			System.out.println("xtraParam----->>["+xtraParams+"]");
			System.out.println("editFlag ------------>>["+editFlag+"]");


			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo  = Integer.parseInt(objContext);
			}	

			switch (currentFormNo)  
			{
			case 1:
				System.out.println("------Inside IndentReqIC for detail1 validation----------------");
				/*				System.out.println("DOM in case 1 wfValData ---->>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1 in case 1 wfValData ----->>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 in case 1 wfValData ----->>["+genericUtility.serializeDom(dom2).toString()+"]");*/	

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength  = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{					
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					//System.out.println("childNodeName ------->>["+childNodeName+"]");

					if("ind_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside ind_date -----------------");
						mdate1 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom));
						mval = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom));
						//Changed by wasim on 06-04-2017 if indent date is blank then it will give error so commented.
						//Date ind_date = sdf.parse(mdate1);	
						//count = currDate.compareTo(ind_date);

						if(mdate1.length() == 0)
						{
							errCode = "VTINDDATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(mdate1.length() > 0)
						{
							Date ind_date = sdf.parse(mdate1);	
							//Changed by wasim on 03-05-2017 for indent date validation as if selected previous date from current date [START]
							//count = currDate.compareTo(ind_date);
							//if(count > 1 && "A".equalsIgnoreCase(editFlag))
							System.out.println("@@Ind_date["+ind_date+"] currDate["+currDate+"]");

							if(ind_date.before(currDate) && "A".equalsIgnoreCase(editFlag))
							{
								//Changed by wasim on 03-05-2017 for indent date validation as if selected previous date from current date [END]	
								errCode = "VTLESSDATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								java.sql.Timestamp  indDate = Timestamp.valueOf(genericUtility.getValidDateString( mdate1, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");//getDateInAppFormat( mdateStr );
								//Changes and Commented By Ajay on 20-12-2017 :START
								//errCode = SysCommon.nfCheckPeriod( "PUR", indDate, mval, conn ); 
								errCode=finCommon.nfCheckPeriod("PUR", indDate, mval, conn);
								//Changes and Commented By Ajay on 20-12-2017 :END
								if(errCode.length() > 0)
								{
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}

					if("site_code__ori".equalsIgnoreCase(childNodeName) || "site_code__des".equalsIgnoreCase(childNodeName) || 
							"site_code__del".equalsIgnoreCase(childNodeName) || "site_code__bil".equalsIgnoreCase(childNodeName) || 
							"site_code__acct".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside site_code__acct -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));	
						cnt = getCount("SITE", "SITE_CODE", mval, conn);

						if(cnt == 0)
						{
							errCode = "VMSITE1";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}

					if("dept_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside dept_code -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("dept_code", dom));
						ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", dom));
						ls_proj_code = checkNullAndTrim(genericUtility.getColumnValue("proj_code", dom));

						cnt = getCount("DEPARTMENT", "DEPT_CODE", mval, conn);
						if(cnt == 0)
						{
							errCode = "VTDEPT1";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						ls_dis_ind_type_list = checkNullAndTrim(discommon.getDisparams("999999", "IND_TYPE_PARAM", conn));
						ls_dis_dept_code_list = checkNullAndTrim(discommon.getDisparams("999999", "IND_DEPTCD_PARAM", conn));
						System.out.println("ls_dis_ind_type_list==========>>["+ls_dis_ind_type_list+"]");
						System.out.println("ls_dis_dept_code_list==========>>["+ls_dis_dept_code_list+"]");

						if(ls_dis_dept_code_list.length() > 0)
						{
							ls_dis_dept_code  = ls_dis_dept_code_list.split(",");
							for(String s: ls_dis_dept_code)
							{
								if(mval.trim().equalsIgnoreCase(s))
								{
									if(ls_dis_ind_type_list.length() > 0)
									{
										ls_dis_ind_type  = ls_dis_ind_type_list.split(",");
										for(String str: ls_dis_ind_type)
										{
											if(ls_ind_type.trim().equalsIgnoreCase(str))
											{
												if(ls_proj_code.length() == 0)
												{
													errCode = "VTPROJ4";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
										}
									}
								}
							}
						}
					}

					if("item_ser".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside item_ser -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("item_ser", dom));

						if(mval.length() == 0)
						{
							errCode = "VTITEMSER5";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if(mval.length() > 0)
						{
							sql = " SELECT ITEM_SER FROM ITEMSER WHERE ITEM_SER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_item_ser = checkNullAndTrim(rs.getString("ITEM_SER"));
							}
							else
							{
								errCode = "VTITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
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
					}
					if("emp_code__req".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside emp_code__req -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("emp_code__req", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom));

						cnt = getCount("EMPLOYEE", "EMP_CODE", mval, conn);
						if(cnt == 0)
						{
							errCode = "VMEMP1";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if(errCode.length() == 0)
						{
							sql = " SELECT RELIEVE_DATE FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mdate1 = checkNullAndTrim(rs.getString("RELIEVE_DATE"));
							}
							else
							{
								errCode = "VMEMP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
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

							//if(mdate1.length() == 0)
							if(mdate1.length() > 0) // Changed By PriyankaC on 05JAN18.
							{
								errCode = "VMEMP2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}	   
					if("work_order".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside work_order -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("work_order", dom));

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

							if("C".equalsIgnoreCase(mval1) || "X".equalsIgnoreCase(mval1))
							{
								errCode = "VTWORDER2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if("proj_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside proj_code -----------------");

						mval = checkNullAndTrim(genericUtility.getColumnValue("proj_code", dom));
						mdate1 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom));
						ls_indent = checkNullAndTrim(genericUtility.getColumnValue("ind_no", dom));
						ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", dom));

						if(ls_indent.length() == 0)
						{
							ls_indent = "@@@";
						}

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
								errCode = "VTPROJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
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

							if(!"A".equalsIgnoreCase(editFlag))
							{
								errCode = "VTPROJ3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						if(errCode.length() == 0)
						{
							ls_type_allow_projbudgt_list = checkNullAndTrim(discommon.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn));
							if("NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
							{
								ls_type_allow_projbudgt_list = "";
							}
							System.out.println("ls_type_allow_projbudgt_list ==================>>["+ls_type_allow_projbudgt_list+"]");

							lb_ord_flag = false;
							if(ls_type_allow_projbudgt_list.length() > 0)
							{
								ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");

								for(String str : ls_type_allow_projbudgt)
								{
									if(ls_ind_type.equalsIgnoreCase(str))
									{
										lb_ord_flag = true;
									}
								}
							}

							if(lb_ord_flag)
							{
								if(mval.length() == 0)
								{
									errCode = "VEPRJ1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

								if(errCode.length() == 0)
								{
									sql = "SELECT COUNT(*) FROM PROJECT WHERE PROJ_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, mval);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ll_proj_cnt = rs.getInt(1);
									}
									else
									{
										errCode = "VTPROJ1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
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

								if(errCode.length() == 0)
								{
									java.sql.Date ind_date = java.sql.Date.valueOf(genericUtility .getValidDateString(mdate1, genericUtility .getApplDateFormat(), genericUtility .getDBDateFormat()));
									sql = " SELECT COUNT(*) FROM PROJECT WHERE PROJ_CODE = ? AND " +
											"(? BETWEEN START_DATE AND END_DATE OR ? BETWEEN START_DATE AND EXT_END_DATE) ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, mval);
									pstmt.setDate(2, ind_date);
									pstmt.setDate(3, ind_date);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ll_count = rs.getInt(1);
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

									if(ll_count == 0)
									{
										errCode = "VTPROJ1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							else if(!lb_ord_flag)
							{
								if(ls_proj_code.length() > 0)
								{
									sql = " SELECT PROJ_TYPE  FROM PROJECT WHERE PROJ_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ls_proj_type = checkNullAndTrim(rs.getString("PROJ_TYPE"));
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

									ls_proj_type_opt = checkNullAndTrim(discommon.getDisparams("999999", "PROJECT_TYPE_OPT", conn));
									if("NULLFOUND".equalsIgnoreCase(ls_proj_type_opt))
									{
										ls_proj_type_opt = "";
									}
									System.out.println("ls_proj_type_opt ==================>>["+ls_proj_type_opt+"]");
									System.out.println("ls_proj_type ==================>>["+ls_proj_type+"]");

									if(ls_proj_type_opt.length() > 0)
									{
										ls_proj_type_opt_list = ls_proj_type_opt.split(",");

										for(String str : ls_proj_type_opt_list)
										{
											if(ls_proj_type.equalsIgnoreCase(str))
											{
												errCode = "VTINVPROJ3";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
							}
						}
					}

					if("task_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside task_code -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("task_code", dom));

						//Added by wasim on 07-APR-17 if task code is present then only validate 
						if(mval.length() > 0)
						{	
							sql = " SELECT COUNT(*) FROM PROJ_EST_MILESTONE WHERE TASK_CODE = ? ";
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
								errCode = "VTTASK1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}

					if("pind_apprv".equalsIgnoreCase(childNodeName))
					{
						mval = checkNullAndTrim(genericUtility.getColumnValue("pind_apprv", dom));
						ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", dom));

						ls_dis_ind_type_list = checkNullAndTrim(discommon.getDisparams("999999", "IND_TYPE_PARAM", conn));


						sql = " SELECT PROJ_TYPE  FROM PROJECT WHERE PROJ_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_proj_code);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_proj_type = checkNullAndTrim(rs.getString("PROJ_TYPE"));
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

						lb_ind_type_flag = false;

						if(ls_dis_ind_type_list.length() > 0)
						{
							ls_dis_ind_type = ls_dis_ind_type_list.split(",");

							for(String s : ls_dis_ind_type)
							{
								if(ls_ind_type.equalsIgnoreCase(s))
								{
									lb_ind_type_flag=true;
								}
							}
						}

						if(lb_ind_type_flag && "PR01".equalsIgnoreCase(ls_proj_type))
						{
							if(mval.length() == 0)
							{
								errCode = "VTPROJMGR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					//Added by wasim on 10-04-17 for validation of anal_code for anal_code validation [START]
					if("anal_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("-------- inside anal_code -----------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("anal_code", dom));

						if(mval.length() > 0)
						{	
							sql = " SELECT ANAL_CODE,DESCR FROM ANALYSIS WHERE ANAL_CODE = ? ";
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
								errCode = "VTINVANAL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	
					}
					//Added by wasim on 10-04-17 for validation of anal_code for anal_code validation [END]
				}
				break;	

			case 2:
				System.out.println("------in detail2 validation----------------");
				System.out.println("DOM in case 2 wfValData ---->>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1 in case 2 wfValData ----->>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 in case 2 wfValData ----->>["+genericUtility.serializeDom(dom2).toString()+"]");	

				parentNodeList = dom2.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					//System.out.println("value of child node name ["+childNodeName + "]");

					if("item_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside item_code wfValData -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						msite = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom1));
						ll_line = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));
						ls_indent = checkNullAndTrim(genericUtility.getColumnValue("ind_no", dom1));
						ls_item_ser = checkNullAndTrim(genericUtility.getColumnValue("item_ser", dom1));
						ls_del_site = checkNullAndTrim(genericUtility.getColumnValue("site_code__del", dom1));

						/*						System.out.println("mval===========>>"+mval);
						System.out.println("msite===========>>"+msite);
						System.out.println("ll_line===========>>"+ll_line);
						System.out.println("ls_indent===========>>"+ls_indent);
						System.out.println("ls_item_ser===========>>"+ls_item_ser);
						System.out.println("ls_del_site===========>>"+ls_del_site);*/

						if(mval.length() == 0)
						{
							errCode = "VTITMCODE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}


						if("E".equalsIgnoreCase(editFlag))
						{
							sql = "SELECT COUNT(*) FROM INDENT_DET WHERE IND_NO = ? AND LINE_NO = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_indent);
							pstmt.setString(2, ll_line);
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

							if(cnt > 0)
							{
								sql = " SELECT ITEM_CODE FROM INDENT_DET WHERE IND_NO = ? AND LINE_NO = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_indent);
								pstmt.setString(2, ll_line);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									ls_itemcode = checkNullAndTrim(rs.getString("ITEM_CODE"));
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


								if(ls_itemcode.length() > 0 && mval.length() > 0)
								{
									if(!ls_itemcode.equalsIgnoreCase(mval))
									{
										errCode = "VTINDRQ1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

						if(errCode.length() == 0)
						{
							ls_task_code = checkNullAndTrim(genericUtility.getColumnValue("task_code", dom1));
							System.out.println("ls_task_code ----------------------"+ ls_task_code);

							if(ls_task_code.length() > 0)
							{
								sql = " SELECT COUNT(*) FROM PROJ_EST_BSL_ITEM WHERE TASK_CODE = ? AND ITEM_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_task_code);
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
									errCode = "VTTASK4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

						if(errCode.length() == 0)
						{
							sql = "SELECT CASE WHEN STOP_BUSINESS IS NULL THEN 'N' ELSE STOP_BUSINESS END AS STOP_BUSINESS, ITEM_SER " +
									"FROM ITEM WHERE ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_stop_busi = checkNullAndTrim(rs.getString("STOP_BUSINESS"));
								ls_itemser = checkNullAndTrim(rs.getString("ITEM_SER"));
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

						if(errCode.length() == 0)
						{
							sql = "SELECT (CASE WHEN EOU IS NULL THEN 'N' ELSE EOU END) AS EOU FROM SITE WHERE SITE_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_del_site);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_eou = checkNullAndTrim(rs.getString("EOU"));
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


							if("Y".equalsIgnoreCase(ls_eou))
							{
								sql = "SELECT LOP_REQD FROM ITEMSER WHERE ITEM_SER = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_itemser);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									ls_lop_reqd = checkNullAndTrim(rs.getString("LOP_REQD"));
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

								if("Y".equalsIgnoreCase(ls_lop_reqd))
								{
									mdate1 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom));
									java.sql.Date ind_date = java.sql.Date.valueOf(genericUtility .getValidDateString(mdate1, genericUtility .getApplDateFormat(), genericUtility .getDBDateFormat()));

									sql = "SELECT COUNT(1) FROM LOP_HDR A, LOP_DET B WHERE A.LOP_REF_NO = B.LOP_REF_NO AND A.SITE_CODE = ? " +
											" AND A.CONFIRMED = ? AND B.ITEM_CODE = ? AND B.ITEM_STATUS = ?  AND ? >= A.VALID_FROM AND ? <= A.VALID_TO " +
											" AND B.BUY_SELL_FLAG IN ('P','B') ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_del_site);
									pstmt.setString(2, "Y");
									pstmt.setString(3, mval);
									pstmt.setString(4, "A");
									pstmt.setDate(5, ind_date);
									pstmt.setDate(6, ind_date);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ll_cnt = rs.getInt(1);
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
									if(ll_cnt == 0)
									{
										errCode = "VTLOPITEM";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

						if(errCode.length() == 0)
						{
							ls_itemser = ls_itemser.trim();

							if(!ls_itemser.equalsIgnoreCase(ls_item_ser))
							{
								errCode = "VTSERCHDIF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

						if(errCode.length() == 0)
						{
							errCode = checkRefCode(mval, msite, conn);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						if(errCode.length() == 0)
						{
							if((ls_item_ser.length() == 0 || ls_item_ser.length() > 0) && ls_item_ser.equalsIgnoreCase(ls_itemser))
							{
								sql = " SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mval);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									ls_stk_opt = checkNullAndTrim(rs.getString("STK_OPT"));
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
							else
							{
								errCode = "VTINSERDIF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							if(errCode.length() == 0)
							{
								sql = "SELECT COUNT(1) FROM INDENT_HDR A, INDENT_DET B WHERE A.IND_NO = B.IND_NO AND A.IND_NO <> ? " +
										" AND A.SITE_CODE__DEL = ? AND B.ITEM_CODE = ? AND A.CONFIRMED <> ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ls_indent);
								pstmt.setString(2, ls_del_site);
								pstmt.setString(3, mval);
								pstmt.setString(4, "Y");
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
								if(cnt > 0)
								{
									errCode = "VTITMIND";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

						if(errCode.length() == 0)
						{
							sql = " SELECT COUNT(A.ITEM_CODE), SUM(CASE WHEN A.MAX_QTY IS NULL THEN 0 ELSE A.MAX_QTY END - CASE WHEN A.QUANTITY_REL IS NULL THEN 0 ELSE A.QUANTITY_REL END) " +
									" FROM PCONTRACT_DET A, PCONTRACT_HDR B WHERE A.CONTRACT_NO =  B.CONTRACT_NO AND  A.ITEM_CODE = ? AND " +
									" CASE WHEN A.STATUS IS NULL THEN ' ' ELSE A.STATUS END <> ? AND " +
									" CASE WHEN B.STATUS IS NULL THEN ' ' ELSE B.STATUS END <> ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							pstmt.setString(2, "C");
							pstmt.setString(3, "C");
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
								mqty2 = rs.getInt(2);
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
							if(cnt > 0)
							{
								if(mqty2 > 0)
								{
									errCode = "VTUNRELCON";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if("unit".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside unit wfValData -------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("unit", dom));
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
								}
							}
						}
					}

					if("quantity".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside quantity wfValData-------------- ");
						mqty = checkNullAndTrim(genericUtility.getColumnValue("quantity", dom));
						mrate = checkNullAndTrim(genericUtility.getColumnValue("purc_rate", dom));
						mmax_rate = checkNullAndTrim(genericUtility.getColumnValue("max_rate", dom));
						mval = checkNullAndTrim(genericUtility.getColumnValue("site_code__del", dom1));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						ls_proj_code = checkNullAndTrim(genericUtility.getColumnValue("proj_code", dom1));
						ls_indent = checkNullAndTrim(genericUtility.getColumnValue("ind_no", dom));
						ll_lineno = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));
						ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", dom1));

						lc_tot_amt = 0;
						mqty = (mqty.length() == 0) ? "0" : mqty;
						mrate = (mrate.length() == 0) ? "0" : mrate;
						mmax_rate = (mmax_rate.length() == 0) ? "0" : mmax_rate;
						ls_indent = (ls_indent.length() == 0) ? "@@@" : ls_indent;

						/*						System.out.println("-------------mqty------------------"+mqty);
						System.out.println("-------------mrate------------------"+mrate);
						System.out.println("-------------mmax_rate------------------"+mmax_rate);
						System.out.println("-------------mval------------------"+mval);
						System.out.println("-------------mval1------------------"+mval1);
						System.out.println("-------------mqty------------------"+mqty);
						System.out.println("-------------ls_proj_code------------------"+ls_proj_code);
						System.out.println("-------------ls_indent------------------"+ls_indent);
						System.out.println("-------------ll_lineno------------------"+ll_lineno);
						System.out.println("-------------ls_ind_type------------------"+ls_ind_type);*/

						//Modified by Anjali R. on[23/04/2018][To take quantity in decimal][Start]
						//if(Integer.parseInt(mqty) <= 0)
						if(Double.parseDouble(mqty) <= 0)
							//Modified by Anjali R. on[23/04/2018][To take quantity in decimal][End]
						{
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						else
						{
							ls_type_allow_projbudgt_list = checkNullAndTrim(discommon.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn));


							if("NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
							{
								ls_type_allow_projbudgt_list = "";
							}

							lb_ord_flag=false;
							if(ls_type_allow_projbudgt_list.length() > 0)
							{
								ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");

								for(String s : ls_type_allow_projbudgt)
								{
									if(ls_ind_type.equalsIgnoreCase(s))
									{
										lb_ord_flag=true;
									}
								}
							}

							System.out.println("------------- ls_type_allow_projbudgt_list --------------"+ls_type_allow_projbudgt_list);
							System.out.println("-------------ls_ind_type------------------"+ls_ind_type);
							System.out.println("-------------lb_ord_flag------------------"+lb_ord_flag);

							if(lb_ord_flag)
							{
								if(Double.parseDouble(mmax_rate) <= Double.parseDouble(mrate))
								{
									errCode = "VTMAXRATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(Double.parseDouble(mmax_rate) > Double.parseDouble(mrate))
								{
									sql = "SELECT APPROX_COST FROM PROJECT WHERE PROJ_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_approxcost = checkNullAndTrim(rs.getString("APPROX_COST"));
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


									sql = "SELECT SUM((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) * (CASE WHEN MAX_RATE IS NULL THEN 0 ELSE MAX_RATE END))  " +
											" FROM INDENT WHERE PROJ_CODE = ? AND STATUS IN ('A' ,'O') ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_ind_amount = rs.getDouble(1);
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

									sql = "SELECT IND_NO FROM INDENT WHERE PROJ_CODE = ? AND STATUS IN ('L','C') AND ORD_QTY <> 0 ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									while(rs.next())
									{
										ls_old_indno = checkNullAndTrim(rs.getString("IND_NO"));

										sql1 = "SELECT SUM(A.TOT_AMT * B.EXCH_RATE) FROM PORDDET A, PORDER B WHERE ( A.PURC_ORDER = B.PURC_ORDER ) " +
												"AND B.CONFIRMED = 'Y' AND A.PROJ_CODE = ? AND A.IND_NO = ? AND B.STATUS <> 'X' AND A.STATUS <> 'C' ";
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, ls_proj_code);
										pstmt1.setString(2, ls_old_indno);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lc_po_amt1 = rs1.getDouble(1);
										}
										if(pstmt1 != null)
										{
											pstmt1.close();
											pstmt1 = null;
										}
										if(rs1 != null)
										{
											rs1.close();
											rs1 = null;
										}

										sql1 = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
												" ( A.PURC_ORDER = C.PURC_ORDER ) AND (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND " +
												" B.CONFIRMED = 'Y' AND C.PROJ_CODE = ? AND C.IND_NO = ? AND B.STATUS <> 'X' AND C.STATUS = 'C' AND B.TRAN_SER = 'P-RCP' ";
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, ls_proj_code);
										pstmt1.setString(2, ls_old_indno);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lc_porcp_amt = rs1.getDouble(1);
										}

										if(pstmt1 != null)
										{
											pstmt1.close();
											pstmt1 = null;
										}
										if(rs1 != null)
										{
											rs1.close();
											rs1 = null;
										}

										sql1 = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
												" (A.PURC_ORDER = C.PURC_ORDER) AND (A.TRAN_ID = B.TRAN_ID) AND A.LINE_NO__ORD = C.LINE_NO AND " +
												" B.CONFIRMED = 'Y' AND C.PROJ_CODE = ? AND C.IND_NO = ? AND B.STATUS <> 'X' AND C.STATUS = 'C' AND " +
												" B.TRAN_SER = 'P-RET' ";
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, ls_proj_code);
										pstmt1.setString(2, ls_old_indno);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lc_pret_amt = rs1.getDouble(1);
										}

										if(pstmt1 != null)
										{
											pstmt1.close();
											pstmt1 = null;
										}
										if(rs1 != null)
										{
											rs1.close();
											rs1 = null;
										}

										lc_tot_poamt = lc_tot_poamt + lc_po_amt1 + lc_porcp_amt - lc_pret_amt;
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
									lc_tot_poamt11 = lc_tot_poamt;

									sql = "SELECT SUM(A.TOT_AMT * B.EXCH_RATE) FROM PORDDET A, PORDER B WHERE ( A.PURC_ORDER = B.PURC_ORDER ) " +
											"AND B.CONFIRMED = ?  AND A.PROJ_CODE = ? AND A.IND_NO IS NULL AND B.STATUS <> ?  AND A.STATUS <> ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "Y");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, "X");
									pstmt.setString(4, "C");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_po_amt12 = rs.getDouble(1);
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

									sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
											"( A.PURC_ORDER = C.PURC_ORDER ) AND (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND " +
											" B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO IS NULL AND B.STATUS <> ? AND " +
											" C.STATUS = ? AND B.TRAN_SER = ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "Y");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, "X");
									pstmt.setString(4, "C");
									pstmt.setString(5, "P-RCP");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_porcp_amt11 = rs.getDouble(1);
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

									sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
											" ( A.PURC_ORDER = C.PURC_ORDER ) AND (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND " +
											" B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO IS NULL AND B.STATUS <> ? AND " +
											" C.STATUS = ? AND B.TRAN_SER = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "Y");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, "X");
									pstmt.setString(4, "C");
									pstmt.setString(5, "P-RET");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_pret_amt11 = rs.getDouble(1);
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

									//The value ls_temp is not being further used, so the below code is commented [Start]
									/*ls_temp = "Quantity:->lc_tot_poamt11 without null" + lc_tot_poamt11;
									sql = " SELECT ? FROM DUAL ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_temp);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ls_temp = rs.getString(1);
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
									 */
									//The value ls_temp is not being further used, so the below code is commented [End]

									lc_tot_poamt = lc_tot_poamt11 + lc_po_amt12 + lc_porcp_amt11 - lc_pret_amt11;

									//The value ls_temp is not being further used, so the below code is commented [Start]
									/*ls_temp = "Quantity:->lc_tot_poamt total amount"+lc_tot_poamt;
									sql = " SELECT ? FROM DUAL ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_temp);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ls_temp = rs.getString(1);
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
									}*/
									//The value ls_temp is not being further used, so the below code is commented [End]

									lc_ind_amount = lc_ind_amount + lc_tot_poamt;

									sql = " SELECT SUM((CASE WHEN B.QUANTITY IS NULL THEN 0 ELSE B.QUANTITY END) * (CASE WHEN B.MAX_RATE IS NULL THEN 0 ELSE B.MAX_RATE END)) " +
											" FROM INDENT_HDR A, INDENT_DET B WHERE A.IND_NO = B.IND_NO AND A.CONFIRMED = ? AND A.PROJ_CODE = ? AND" +
											" A.IND_NO <> ? AND A.STATUS <> ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "N");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, ls_indent);
									pstmt.setString(4, "C");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_prev_indent = rs.getDouble(1);
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

									String domId = getObjNameFromDom( dom, "domID",  "2" );

									String mqty_brow = "", lc_rate_brow = "";
									for(ll_count = 1; ll_count < Integer.parseInt(domId); ll_count++)
									{
										if(!domId.equalsIgnoreCase(ll_lineno))
										{
											mqty_brow = checkNullAndTrim(genericUtility.getColumnValue("quantity", dom2, "2", domId));
											lc_rate_brow = checkNullAndTrim(genericUtility.getColumnValue("max_rate", dom2, "2", domId));

											lc_tot_amt = lc_tot_amt + (Double.parseDouble(mqty_brow) * Double.parseDouble(lc_rate_brow));  
											System.out.println("domId ==========>>"+domId);		
											System.out.println("-------------lc_tot_amt -----------------"+lc_tot_amt);

											//The value ls_temp is not being further used, so the below code is commented [Start]
											/*ls_temp = "in loop Current Tot_amt"+lc_tot_poamt;
											sql = " SELECT ? FROM DUAL ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, ls_temp);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												ls_temp = rs.getString(1);
												System.out.println("-------------ls_temp 444444444444 -----------------"+ls_temp);
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
											}*/
											//The value ls_temp is not being further used, so the below code is commented [End]
										}
									}

									lc_tot_amt = lc_tot_amt + (Double.parseDouble(mqty) * Double.parseDouble(mmax_rate));

									lc_tot_amt_proj = lc_ind_amount +  lc_tot_amt + lc_prev_indent;
									lc_approxcost = (lc_approxcost.length() == 0) ? "0" : lc_approxcost;
									lc_exceed_amt = lc_tot_amt_proj - Double.parseDouble(lc_approxcost);
									if(lc_tot_amt_proj > Double.parseDouble(lc_approxcost))
									{
										errCode = "VTPROJCOST";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}
								}
							}

							if(errCode.length() == 0)
							{
								sql = " SELECT CASE WHEN MAX_QTY IS NULL THEN 0 ELSE MAX_QTY END FROM SITEITEM WHERE SITE_CODE = ? " +
										" AND ITEM_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mval);
								pstmt.setString(2, mval1);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									mqty2 = rs.getDouble(1);
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

								if(mqty2 == 0)
								{
									sql = " SELECT CASE WHEN MAX_QTY IS NULL THEN 0 ELSE MAX_QTY END FROM ITEM WHERE ITEM_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, mval1);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										mqty2 = rs.getDouble(1);
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

									if(Double.parseDouble(mqty) > mqty2)
									{
										errCode = "VTQMAXI";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else if(Double.parseDouble(mqty) > mqty2)
								{
									errCode = "VTQMAXSI";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}					
					if("acct_code".equalsIgnoreCase(childNodeName))
					{
						mval = checkNullAndTrim(genericUtility.getColumnValue("acct_code", dom));
						msite = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom1));

						errCode = finCommon.isAcctCode(msite, mval, "" , conn);
						if(errCode.length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("cctr_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside cctr_code wfValData-------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("cctr_code", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("acct_code", dom));

						if(mval.length() == 0)
						{
							errCode = "VTINDCTRCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							errCode = finCommon.isCctrCode(mval1, mval, "", conn);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if("supp_code__pref".equalsIgnoreCase(childNodeName))		// Pref Supplier:               
					{
						System.out.println("----------- Inside supp_code__pref wfValData-------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("supp_code__pref", dom));

						if(mval.length() > 0)
						{	
							msite = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom1));

							errCode = finCommon.isSupplier(msite, mval, "",conn);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if("emp_code__iapr".equalsIgnoreCase(childNodeName) || "emp_code__pur".equalsIgnoreCase(childNodeName)) //Indent Appr / Purchaser :               
					{
						System.out.println("----------- Inside emp_code_iapr wfValData-------------- "+childNodeName);
						mval = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));

						if(mval.length() > 0)
						{
							msite = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom1));

							errCode = getEmployeeResig(mval, msite, conn);
							if(errCode.length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//commented by nandkumar gadkari  on 29/03/18
						/*else
						{
							errCode = "VTPUINDAPP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
					}

					if("pack_code".equalsIgnoreCase(childNodeName))
					{
						System.out.println("------- Inside pack_code wfValData-------------");
						mval = checkNullAndTrim(genericUtility.getColumnValue("pack_code", dom));

						sql = "SELECT COUNT(*) FROM PACKING WHERE PACK_CODE = ? ";
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
							errCode = "VTPKCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if("specific_instr".equalsIgnoreCase(childNodeName)) //Specific Inst:               
					{
						System.out.println("----------- Inside specific_instr wfValData-------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("specific_instr", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						msite = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", dom1));

						sql = "SELECT SPEC_REQD FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msite);
						pstmt.setString(2, mval1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							mval2 = checkNullAndTrim(rs.getString("SPEC_REQD"));
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

						if(mval2.length() == 0)
						{
							mval2 = "N";
						}
						if("Y".equalsIgnoreCase(mval2))
						{
							if(mval.length() == 0)
							{
								errCode = "VTSPEC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else if("N".equalsIgnoreCase(mval2))
						{ 
							sql = "SELECT SPEC_REQD FROM ITEM WHERE  ITEM_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mval2 = checkNullAndTrim(rs.getString("SPEC_REQD"));
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
							if("Y".equalsIgnoreCase(mval2))
							{
								if(mval.length() == 0)
								{
									errCode = "VTSPEC";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}

					if("conv__qty_stduom".equalsIgnoreCase(childNodeName))	//UOM Con :               
					{
						System.out.println("----------- Inside conv__qty_stduom wfValData-------------- ");
						lc_conv = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", dom));
						mval = checkNullAndTrim(genericUtility.getColumnValue("unit", dom));
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
							double conQty = discommon.convQtyFactor(mval, mval1, ls_itemcode, Double.parseDouble(lc_conv), conn);

							// how to throw error code, because the convQtyFactor method returns the double value 
							//errcode = gf_check_conv_fact(ls_item, mval, mval1, lc_convqtystduom)

						}
					}

					if("max_rate".equalsIgnoreCase(childNodeName))
					{

						System.out.println("----------- Inside max_rate wfValData-------------- ");
						mmax_rate = checkNullAndTrim(genericUtility.getColumnValue("max_rate", dom));
						mrate = checkNullAndTrim(genericUtility.getColumnValue("purc_rate", dom));
						mqty = checkNullAndTrim(genericUtility.getColumnValue("quantity", dom));
						ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", dom1));
						ls_proj_code = checkNullAndTrim(genericUtility.getColumnValue("proj_code", dom1));
						ls_indent = checkNullAndTrim(genericUtility.getColumnValue("ind_no", dom));
						ll_lineno = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));
						lc_tot_amt = 0;
						lc_ind_amount = 0;
						lc_tot_poamt = 0;

						mrate = (mrate.length() == 0) ? "0" : mrate;
						mmax_rate = (mmax_rate.length() == 0) ? "0" : mmax_rate;
						ls_indent = (ls_indent.length() == 0) ? "@@@" : ls_indent;


						if(errCode.length() == 0)
						{
							ls_type_allow_projbudgt_list = checkNullAndTrim(discommon.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn));
							if("NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
							{
								ls_type_allow_projbudgt_list = "";
							}

							lb_ord_flag=false;
							if(ls_type_allow_projbudgt_list.length() > 0)
							{
								ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");

								for(String s : ls_type_allow_projbudgt)
								{
									if(ls_ind_type.equalsIgnoreCase(s))
									{
										lb_ord_flag=true;
									}
								}
							}
							System.out.println("----------- ls_type_allow_projbudgt_list -------------- "+ls_type_allow_projbudgt_list);
							System.out.println("----------- ls_ind_type -------------- "+ls_ind_type);
							System.out.println("----------- lb_ord_flag after -------------- "+lb_ord_flag);

							if(lb_ord_flag)
							{
								if(Double.parseDouble(mmax_rate) <= Double.parseDouble(mrate))
								{
									errCode = "VTMAXRATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(Double.parseDouble(mmax_rate) > Double.parseDouble(mrate))
								{
									sql = "SELECT APPROX_COST FROM PROJECT WHERE PROJ_CODE = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_approxcost = checkNullAndTrim(rs.getString("APPROX_COST"));
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

									sql = "SELECT SUM((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) * (CASE WHEN MAX_RATE IS NULL THEN 0 ELSE MAX_RATE END))  " +
											" FROM INDENT WHERE PROJ_CODE = ? AND STATUS IN ('A' ,'O') ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_ind_amount = rs.getDouble(1);
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

									sql = "SELECT IND_NO FROM INDENT WHERE PROJ_CODE = ? AND STATUS IN ('L','C') AND ORD_QTY <> 0 ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_proj_code);
									rs = pstmt.executeQuery();
									while(rs.next())
									{
										ls_old_indno = checkNullAndTrim(rs.getString("IND_NO"));

										sql1 = "SELECT SUM(A.TOT_AMT * B.EXCH_RATE) FROM PORDDET A, PORDER B WHERE ( A.PURC_ORDER = B.PURC_ORDER ) " +
												"AND B.CONFIRMED = 'Y' AND A.PROJ_CODE = ? AND A.IND_NO = ? AND B.STATUS <> 'X' AND A.STATUS <> 'C' ";
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, ls_proj_code);
										pstmt1.setString(2, ls_old_indno);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lc_po_amt1 = rs1.getDouble(1);
										}

										if(pstmt1 != null)
										{
											pstmt1.close();
											pstmt1 = null;
										}
										if(rs1 != null)
										{
											rs1.close();
											rs1 = null;
										}


										sql1 = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
												" ( A.PURC_ORDER = C.PURC_ORDER ) AND (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND " +
												" B.CONFIRMED = 'Y' AND C.PROJ_CODE = ? AND C.IND_NO = ? AND B.STATUS <> 'X' AND C.STATUS = 'C' AND B.TRAN_SER = 'P-RCP' ";
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, ls_proj_code);
										pstmt1.setString(2, ls_old_indno);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lc_porcp_amt = rs1.getDouble(1);
										}

										if(pstmt1 != null)
										{
											pstmt1.close();
											pstmt1 = null;
										}
										if(rs1 != null)
										{
											rs1.close();
											rs1 = null;
										}

										sql1 = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
												" (A.PURC_ORDER = C.PURC_ORDER) AND (A.TRAN_ID = B.TRAN_ID) AND A.LINE_NO__ORD = C.LINE_NO AND " +
												" B.CONFIRMED = 'Y' AND C.PROJ_CODE = ? AND C.IND_NO = ? AND B.STATUS <> 'X' AND C.STATUS = 'C' AND " +
												" B.TRAN_SER = 'P-RET' ";
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, ls_proj_code);
										pstmt1.setString(2, ls_old_indno);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											lc_pret_amt = rs1.getDouble(1);
										}

										if(pstmt1 != null)
										{
											pstmt1.close();
											pstmt1 = null;
										}
										if(rs1 != null)
										{
											rs1.close();
											rs1 = null;
										}

										lc_tot_poamt = lc_tot_poamt + lc_po_amt1 + lc_porcp_amt - lc_pret_amt;
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
									lc_tot_poamt11 = lc_tot_poamt;

									sql = "SELECT SUM(A.TOT_AMT * B.EXCH_RATE) FROM PORDDET A, PORDER B WHERE ( A.PURC_ORDER = B.PURC_ORDER ) " +
											"AND B.CONFIRMED = ?  AND A.PROJ_CODE = ? AND A.IND_NO IS NULL AND B.STATUS <> ?  AND A.STATUS <> ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "Y");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, "X");
									pstmt.setString(4, "C");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_po_amt12 = rs.getDouble(1);
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

									sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
											"( A.PURC_ORDER = C.PURC_ORDER ) AND (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND " +
											" B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO IS NULL AND B.STATUS <> ? AND " +
											" C.STATUS = ? AND B.TRAN_SER = ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "Y");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, "X");
									pstmt.setString(4, "C");
									pstmt.setString(5, "P-RCP");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_porcp_amt11 = rs.getDouble(1);
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

									sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE " +
											" ( A.PURC_ORDER = C.PURC_ORDER ) AND (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND " +
											" B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO IS NULL AND B.STATUS <> ? AND " +
											" C.STATUS = ? AND B.TRAN_SER = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "Y");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, "X");
									pstmt.setString(4, "C");
									pstmt.setString(5, "P-RET");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_pret_amt11 = rs.getDouble(1);
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

									//The value ls_temp is not being further used, so the below code is commented [Start]
									/*ls_temp = "Max Rate :->lc_tot_poamt11 without null" + lc_tot_poamt11;
									sql = " SELECT ? FROM DUAL ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_temp);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ls_temp = rs.getString(1);
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
									}*/
									//The value ls_temp is not being further used, so the below code is commented [End]

									lc_tot_poamt = lc_tot_poamt11 + lc_po_amt12 + lc_porcp_amt11 - lc_pret_amt11;

									//The value ls_temp is not being further used, so the below code is commented [Start]
									/*ls_temp = "Max Rate :-> lc_tot_poamt total amount" + lc_tot_poamt;
									sql = " SELECT ? FROM DUAL ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, ls_temp);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ls_temp = rs.getString(1);
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
									}*/
									//The value ls_temp is not being further used, so the below code is commented [End]

									lc_ind_amount = lc_ind_amount + lc_tot_poamt;

									sql = " SELECT SUM((CASE WHEN B.QUANTITY IS NULL THEN 0 ELSE B.QUANTITY END) * (CASE WHEN B.MAX_RATE IS NULL THEN 0 ELSE B.MAX_RATE END)) " +
											" FROM INDENT_HDR A, INDENT_DET B WHERE A.IND_NO = B.IND_NO AND A.CONFIRMED = ? AND A.PROJ_CODE = ? AND " +
											" A.IND_NO <> ? AND A.STATUS <> ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, "N");
									pstmt.setString(2, ls_proj_code);
									pstmt.setString(3, ls_indent);
									pstmt.setString(4, "C");
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										lc_prev_indent = rs.getDouble(1);
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

									String domId = getObjNameFromDom( dom, "domID",  "2" );
									System.out.println("domId ==========>>"+domId);

									String mqty_brow = "", lc_rate_brow = "";
									for(ll_count = 1; ll_count < Integer.parseInt(domId); ll_count++)
									{
										if(!domId.equalsIgnoreCase(ll_lineno))
										{
											mqty_brow = checkNullAndTrim(genericUtility.getColumnValue("quantity", dom2, "2", domId));
											lc_rate_brow = checkNullAndTrim(genericUtility.getColumnValue("max_rate", dom2, "2", domId));

											lc_tot_amt = lc_tot_amt + (Double.parseDouble(mqty_brow) * Double.parseDouble(lc_rate_brow));  
											System.out.println("domId ==========>>"+domId);
											System.out.println("-------------lc_tot_amt -----------------"+lc_tot_amt);

											//The value ls_temp is not being further used, so the below code is commented [Start]
											/*ls_temp = "in loop Current Tot_amt"+lc_tot_poamt;
											sql = " SELECT ? FROM DUAL ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, ls_temp);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												ls_temp = rs.getString(1);
												System.out.println("-------------ls_temp 444444444444 -----------------"+ls_temp);
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
											}*/
											//The value ls_temp is not being further used, so the below code is commented [End]
										}
									}
									lc_tot_amt = lc_tot_amt + (Double.parseDouble(mqty) * Double.parseDouble(mmax_rate));

									lc_tot_amt_proj = lc_ind_amount +  lc_tot_amt + lc_prev_indent;
									lc_approxcost = (lc_approxcost.length() == 0) ? "0" : lc_approxcost;
									lc_exceed_amt = lc_tot_amt_proj - Double.parseDouble(lc_approxcost);

									if(lc_tot_amt_proj > Double.parseDouble(lc_approxcost))
									{
										errCode = "VTPROJCOST";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}

							if(Double.parseDouble(mmax_rate) == 0)
							{
								if(Double.parseDouble(mmax_rate) < Double.parseDouble(mrate))
								{
									errCode = "VTMAXRATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}
					}

					if("purc_rate".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside purc_rate wfValData-------------- ");
						lc_purc_rate = checkNullAndTrim(genericUtility.getColumnValue("purc_rate", dom));
						lc_last_pur_rate = checkNullAndTrim(genericUtility.getColumnValue("last_pur_rate", dom));

						if(!lc_purc_rate.equalsIgnoreCase(lc_last_pur_rate))
						{
							errCode = "VTCHGRATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					if("emp_code__qcaprv".equalsIgnoreCase(childNodeName))	// QC Approvar:               
					{
						System.out.println("----------- Inside emp_code__qcaprv wfValData-------------- ");
						ls_emp = checkNullAndTrim(genericUtility.getColumnValue("emp_code__qcaprv", dom));
						ls_item = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));

						sql = "SELECT QC_REQD FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_item);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_qc_reqd = checkNullAndTrim(rs.getString("QC_REQD"));
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

						if("Y".equalsIgnoreCase(ls_qc_reqd))
						{
							errCode = getEmployeeResig(ls_emp, "", conn);
							if(errCode.length() != 0)
							{
								errCode = "VERREMPL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if("item_code__mfg".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside item_code__mfg  wfValData-------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("item_code__mfg", dom));

						if(mval.length() > 0)
						{
							sql = "SELECT COUNT(*) FROM ITEM WHERE ITEM_CODE = ? ";
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
								errCode = "VMITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if("work_order".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside work_order wfValData-------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("work_order", dom));

						if(mval.length() > 0)
						{
							sql = "SELECT STATUS FROM WORKORDER WHERE WORK_ORDER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mval);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mstat = checkNullAndTrim(rs.getString("STATUS"));
							}
							else
							{
								errCode = "VTWORD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
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

							if("C".equalsIgnoreCase(mstat) || "X".equalsIgnoreCase(mstat))
							{
								errCode = "VTWORDER2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if("exp_cons_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside exp_cons_date wfValData-------------- ");
						ldt_consdate = checkNullAndTrim(genericUtility.getColumnValue("exp_cons_date", dom));
						ls_indent = checkNullAndTrim(genericUtility.getColumnValue("ind_no", dom1));
						ll_line = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom));

						java.sql.Date ldt_reqdate_temp = null;
						sql = "SELECT INDENTITEM_DET.REQ_DATE FROM  INDENTITEM_DET, ITEM, INDENT_HDR WHERE INDENTITEM_DET.ITEM_CODE = ITEM.ITEM_CODE " +
								"AND INDENTITEM_DET.IND_NO = INDENT_HDR.IND_NO AND  INDENTITEM_DET.IND_NO = ? AND INDENTITEM_DET.LINE_NO = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_indent);
						pstmt.setString(2, ll_line);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ldt_reqdate_temp = rs.getDate("REQ_DATE");
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

						if(ldt_consdate.trim().length() > 0 && ldt_reqdate_temp != null)
						{
							String temp = sdf.format(ldt_reqdate_temp);
							mdate = sdf.parse(temp);
							ldt_consdate1 = sdf.parse(ldt_consdate);
							if(ldt_consdate1.before(mdate))
							{
								errCode = "VTCONREQDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("ldt_consdate errCode ============>>"+errCode);
							}
						}
					}    

					if("req_date".equalsIgnoreCase(childNodeName))
					{
						System.out.println("----------- Inside req_date wfValData-------------- ");
						ldt_consdate = checkNullAndTrim(genericUtility.getColumnValue("req_date", dom));
						mdate1 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom1));

						ldt_consdate = (ldt_consdate == null || ldt_consdate.length() == 0) ? "" : ldt_consdate;
						mdate1 = (mdate1 == null || mdate1.length() == 0) ? "" : mdate1;

						//Timestamp tempTestDate = Timestamp.valueOf("1900-01-01 00:00:00");   || ldt_consdate.equals(tempTestDate)
						if(ldt_consdate.trim().length() == 0)
						{
							errCode = "VTINDREQDT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(ldt_consdate.trim().length() > 0)
						{
							ldt_consdate1 = sdf.parse(ldt_consdate);
							mdate = sdf.parse(mdate1);

							if(ldt_consdate1.before(mdate))
							{
								errCode = "VTINDDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}

					if("anal_code".equalsIgnoreCase(childNodeName))	
					{
						System.out.println("----------- Inside anal_code wfValData-------------- ");
						mval = checkNullAndTrim(genericUtility.getColumnValue("anal_code", dom));
						mval1 = checkNullAndTrim(genericUtility.getColumnValue("acct_code", dom));	

						if(mval.length() > 0)
						{
							errCode = finCommon.isAnalysis(mval1, mval, "", conn);
							// commented by nandkumar gadkari on 12/10/19 for wrong condition
							//errCode = (errCode.length() == 0 || errCode == null) ? "" : errCode;
							//if(errCode.length() > 0 || errCode != null)
							errCode = (errCode == null || errCode.length() == 0  ) ? "" : errCode;
							if(errCode != null  && errCode.length() > 0  )
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					//Changed by wasim on 07-APR-2017 for consumption date validation [START]
					if("exp_cons_date".equalsIgnoreCase(childNodeName))	
					{
						Timestamp date1 = null, date2 = null;
						String reqDate = "",expConsDate = "";
						reqDate = checkNullAndTrim(genericUtility.getColumnValue("req_date", dom));
						expConsDate = checkNullAndTrim(genericUtility.getColumnValue("exp_cons_date", dom));	

						if(expConsDate.length() > 0)
						{
							date1 = Timestamp.valueOf(genericUtility.getValidDateString( reqDate , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;
							date2 = Timestamp.valueOf(genericUtility.getValidDateString( expConsDate , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");;

							if(date2.before(date1))
							{
								errCode = "VTCONSDATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Changed by wasim on 07-APR-2017 for consumption date validation [END]
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
					errString = itmDBAccessEJB.getErrorString( errFldName, errCode, userId, "",conn);
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
			throw new ITMException(e);
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

		ArrayList qty = new ArrayList();
		ArrayList lc_tempqty = new ArrayList();

		String sql = "", currDate = "", loginSite = "", loginEmpCode = "", ecode = "", efname = "", elname = "", deptcode = "", descr1 = "", 
				descr = "", mval = "", mval1 = "", mval2 = "", ls_anal = "", msite = "", ls_dept = "", ls_budget_amt_anal = "", 
				ls_consumed_amt_anal = "", itemunit = "", ls_unit_std = "", qtyStr = "", lc_conv1 = "", ls_ind_type = "", ls_site_code__del = "", 
				ls_proj_code = "", ls_proj_type = "", ls_dis_ind_type_list = "", ls_pind_apprv_ge = "", 
				ls_pind_apprv = "", ls_var_name = "", ls_pind_apprv_ph = "", ls_pind_apprv_bc = "", ls_WorkOrder = "", mdate2 = "", li_line = "",
				ld_avgqty = "", msite_del = "", lc_qty1 = "", itemdescr = "", pckcode = "", aprcode = "", itemser = "", lead = "", ld_pur_lead = "", ls_emp_iapr_i = "", ls_supp_pref = "", ls_emp_pur_i = "",
				ld_purc_rate = "", ld_reo_qty = "", ls_unitpur = "", ls_emp_code__qcaprv = "", ls_fname = "",ls_mname = "", ls_lname = "", ld_pur_lead_i = "", ls_supp_pref_i = "", ls_emp_pur_si = "", 
				ls_emp_iapr_si = "", ls_emp_pur_is = "", ls_emp_iapr_is = "", ls_emp_pur = "", ls_emp_iapr = "", pckinstr = "", indent_type = "", 
				ls_cctr_dept = "", cctr = "", ic_pur_lead_time = "", ld_ind_date = "", ls_track_shelf_life = "", ls_qc_reqd = "", ls_site_code__ori = "", 
				ls_smnfr_code = "", ls_allsmnfr_code = "", ls_emppurc = "", ls_itemser = "";

		int qty1 = 0, li_cnt = 0, li_ctr = 0, ld_maxval = 0, cnt = 0, lc_conv = 0, itemCnt = 0;
		Date ind_date = null;
		boolean lb_ind_type_flag = false;
		double ld_last_purrate = 0, lc_budget_amt = 0, ld_rate = 0, ld_amt = 0;
		String [] acct = null;

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
				/*				System.out.println("hdrDataDom in itemchanged case 1------->>["+genericUtility.serializeDom(hdrDataDom)+"]");	
				System.out.println("currFormDataDom in itemchanged case1 ------>>["+genericUtility.serializeDom(currFormDataDom)+"]");
				System.out.println("allFormDataDom in itemchanged case1 ------>>["+genericUtility.serializeDom(allFormDataDom)+"]");*/

				valueXmlString.append( "<Detail1>\r\n" );
				if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )
				{
					sql = "SELECT COUNT(*) FROM PROJ_EST_MILESTONE ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginEmpCode);
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

					if(cnt > 0)
					{
						valueXmlString.append( "<task_code protect = '0'><![CDATA[" ).append( "" ).append( "]]></task_code>\r\n" );
					}
					else
					{
						valueXmlString.append( "<task_code protect = '1'><![CDATA[" ).append( "" ).append( "]]></task_code>\r\n" );
					}

					valueXmlString.append( "<ind_date protect = '1'><![CDATA[" ).append( "" ).append( "]]></ind_date>\r\n" );

					ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", hdrDataDom));
					ls_site_code__del = checkNullAndTrim(genericUtility.getColumnValue("site_code__del", hdrDataDom));
					ls_proj_code = checkNullAndTrim(genericUtility.getColumnValue("proj_code", hdrDataDom));


					sql = "SELECT PROJ_TYPE FROM PROJECT WHERE PROJ_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_proj_code);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_proj_type = checkNullAndTrim(rs.getString("PROJ_TYPE"));
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

					ls_dis_ind_type_list = checkNullAndTrim(discommon.getDisparams("999999", "IND_TYPE_PARAM", conn));
					lb_ind_type_flag = false;

					while(ls_dis_ind_type_list.length() > 0)
					{
						String[] ls_dis_ind_type = ls_dis_ind_type_list.split(",");
						for(int i = 0; i < ls_dis_ind_type.length; i++)
						{
							if(ls_ind_type.equalsIgnoreCase(ls_dis_ind_type[i]))
							{
								lb_ind_type_flag = true;
							}
						}
					}

					if(lb_ind_type_flag && "PR01".equalsIgnoreCase(ls_proj_type))
					{
						valueXmlString.append( "<pind_apprv><![CDATA[" ).append( "" ).append( "]]></pind_apprv>\r\n" );
						valueXmlString.append( "<pind_apprv__ge><![CDATA[" ).append( "" ).append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph><![CDATA[" ).append( "" ).append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc><![CDATA[" ).append( "" ).append( "]]></pind_apprv__bc>\r\n" );
						valueXmlString.append( "<emp_fname_1><![CDATA[" ).append( "" ).append( "]]></emp_fname_1>\r\n" );
						valueXmlString.append( "<emp_lname_1><![CDATA[" ).append( "" ).append( "]]></emp_lname_1>\r\n" );
						valueXmlString.append( "<emp_fname_2><![CDATA[" ).append( "" ).append( "]]></emp_fname_2>\r\n" );
						valueXmlString.append( "<emp_lname_2><![CDATA[" ).append( "" ).append( "]]></emp_lname_2>\r\n" );
						valueXmlString.append( "<emp_fname_3><![CDATA[" ).append( "" ).append( "]]></emp_fname_3>\r\n" );
						valueXmlString.append( "<emp_lname_3><![CDATA[" ).append( "" ).append( "]]></emp_lname_3>\r\n" );
						valueXmlString.append( "<emp_fname_4><![CDATA[" ).append( "" ).append( "]]></emp_fname_4>\r\n" );
						valueXmlString.append( "<emp_lname_4><![CDATA[" ).append( "" ).append( "]]></emp_lname_4>\r\n" );

						ls_var_name=ls_site_code__del+"_PIND_APPRV";

						sql = "SELECT COUNT(1) FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, "999999");
						pstmt.setString(2, ls_var_name);
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

						if(cnt > 0)
						{
							sql = "SELECT VAR_VALUE FROM DISPARM WHERE VAR_NAME = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_var_name);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_pind_apprv = checkNullAndTrim(rs.getString("VAR_VALUE"));
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

							if(ls_pind_apprv.length() == 0)
							{
								ls_pind_apprv = "";
							}
							valueXmlString.append( "<pind_apprv><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );

						}

						if("SP105".equalsIgnoreCase(ls_site_code__del))
						{
							valueXmlString.append( "<pind_apprv protect = '0'><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}
						else
						{
							valueXmlString.append( "<pind_apprv protect = '1'><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}

						ls_pind_apprv_ge = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__GE", conn));
						ls_pind_apprv_ge = (ls_pind_apprv_ge.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_ge)) ? ls_pind_apprv_ge = " " : ls_pind_apprv_ge;

						ls_pind_apprv_ph = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__PH", conn));
						ls_pind_apprv_ph = (ls_pind_apprv_ph.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_ph)) ? ls_pind_apprv_ph = " " : ls_pind_apprv_ph;

						ls_pind_apprv_bc = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__BC", conn));
						ls_pind_apprv_bc = (ls_pind_apprv_bc.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_bc)) ? ls_pind_apprv_bc = " " : ls_pind_apprv_bc;

						valueXmlString.append( "<pind_apprv__ge protect = '1'><![CDATA[" ).append(ls_pind_apprv_ge).append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph protect = '1'><![CDATA[" ).append(ls_pind_apprv_ph).append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc protect = '1'><![CDATA[" ).append(ls_pind_apprv_bc).append( "]]></pind_apprv__bc>\r\n" );

						if(ls_pind_apprv.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_3><![CDATA[" ).append(efname).append( "]]></emp_fname_3>\r\n" );
							valueXmlString.append( "<emp_lname_3><![CDATA[" ).append(elname).append( "]]></emp_lname_3>\r\n" );
						}

						if(ls_pind_apprv_ge.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_ge);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_1><![CDATA[" ).append(efname).append( "]]></emp_fname_1>\r\n" );
							valueXmlString.append( "<emp_lname_1><![CDATA[" ).append(elname).append( "]]></emp_lname_1>\r\n" );
						}
						if(ls_pind_apprv_ph.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_ph);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_2><![CDATA[" ).append(efname).append( "]]></emp_fname_2>\r\n" );
							valueXmlString.append( "<emp_lname_2><![CDATA[" ).append(elname).append( "]]></emp_lname_2>\r\n" );
						}
						if(ls_pind_apprv_bc.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_bc);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_4><![CDATA[" ).append(efname).append( "]]></emp_fname_4>\r\n" );
							valueXmlString.append( "<emp_lname_4><![CDATA[" ).append(elname).append( "]]></emp_lname_4>\r\n" );
						}
					}
					else
					{
						valueXmlString.append( "<pind_apprv protect = '0'><![CDATA[" ).append("").append( "]]></pind_apprv>\r\n" );
						valueXmlString.append( "<pind_apprv__ge><![CDATA[" ).append("").append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph><![CDATA[" ).append("").append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc><![CDATA[" ).append("").append( "]]></pind_apprv__bc>\r\n" );
						valueXmlString.append( "<emp_fname_1><![CDATA[" ).append("").append( "]]></emp_fname_1>\r\n" );
						valueXmlString.append( "<emp_lname_1><![CDATA[" ).append("").append( "]]></emp_lname_1>\r\n" );
						valueXmlString.append( "<emp_fname_2><![CDATA[" ).append("").append( "]]></emp_fname_2>\r\n" );
						valueXmlString.append( "<emp_lname_2><![CDATA[" ).append("").append( "]]></emp_lname_2>\r\n" );
						valueXmlString.append( "<emp_fname_3><![CDATA[" ).append("").append( "]]></emp_fname_3>\r\n" );
						valueXmlString.append( "<emp_lname_3><![CDATA[" ).append("").append( "]]></emp_lname_3>\r\n" );
						valueXmlString.append( "<emp_fname_4><![CDATA[" ).append("").append( "]]></emp_fname_4>\r\n" );
						valueXmlString.append( "<emp_lname_4><![CDATA[" ).append("").append( "]]></emp_lname_4>\r\n" );
					}
				}
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("------- Inside itm_default ------------->>");

					valueXmlString.append( "<ind_date protect = '0'><![CDATA[" ).append(currDate).append( "]]></ind_date>\r\n" );
					valueXmlString.append( "<site_code__ori><![CDATA[" ).append(loginSite).append( "]]></site_code__ori>\r\n" );
					valueXmlString.append( "<site_code__des><![CDATA[" ).append(loginSite).append( "]]></site_code__des>\r\n" );
					valueXmlString.append( "<site_code__del><![CDATA[" ).append(loginSite).append( "]]></site_code__del>\r\n" );
					valueXmlString.append( "<site_code__bil><![CDATA[" ).append(loginSite).append( "]]></site_code__bil>\r\n" );
					valueXmlString.append( "<site_code__acct><![CDATA[" ).append(loginSite).append( "]]></site_code__acct>\r\n" );


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
				}

				if( currentColumn.trim().equalsIgnoreCase( "site_code__ori" ) )
				{
					System.out.println("------- Inside site_code__ori ------------->>" );

					mval = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", hdrDataDom));

					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
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
					valueXmlString.append( "<site_descr><![CDATA[" ).append(descr).append( "]]></site_descr>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase( "dept_code" ) )
				{
					System.out.println("------- Inside dept_code ------------->>" );
					mval = checkNullAndTrim(genericUtility.getColumnValue("dept_code", hdrDataDom));

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
					System.out.println("------- Inside emp_code__req ------------->>" );
					mval = checkNullAndTrim(genericUtility.getColumnValue("emp_code__req", hdrDataDom));

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
				if( currentColumn.trim().equalsIgnoreCase( "task_code" ) )
				{
					System.out.println("------- Inside task_code ------------->>" );
					mval = checkNullAndTrim(genericUtility.getColumnValue("task_code", hdrDataDom));

					sql = "SELECT TASK_DESC FROM PROJ_EST_MILESTONE WHERE TASK_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mval1 = checkNullAndTrim(rs.getString("TASK_DESC"));
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
					valueXmlString.append( "<ls_task_desc><![CDATA[" ).append(mval1).append( "]]></ls_task_desc>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase( "proj_code" ) )
				{
					System.out.println("------- Inside proj_code ------------->>" );
					mval = checkNullAndTrim(genericUtility.getColumnValue("proj_code", hdrDataDom));
					ls_site_code__del = checkNullAndTrim(genericUtility.getColumnValue("site_code__del", hdrDataDom));
					ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", hdrDataDom));

					sql = "SELECT DESCR FROM PROJECT WHERE PROJ_CODE = ? ";
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
					valueXmlString.append( "<descr><![CDATA[" ).append(descr).append( "]]></descr>\r\n" );

					sql = "SELECT PROJ_TYPE FROM PROJECT WHERE PROJ_CODE = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_proj_type = checkNullAndTrim(rs.getString("PROJ_TYPE"));
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

					ls_dis_ind_type_list = checkNullAndTrim(discommon.getDisparams("999999", "IND_TYPE_PARAM", conn));
					System.out.println("ls_dis_ind_type_list==================>>["+ls_dis_ind_type_list+"]");

					lb_ind_type_flag = false;

					if(ls_dis_ind_type_list.length() > 0)
					{
						String[] ls_dis_ind_type = ls_dis_ind_type_list.split(",");
						for(int i = 0; i < ls_dis_ind_type.length; i++)
						{
							if(ls_ind_type.equalsIgnoreCase(ls_dis_ind_type[i]))
							{
								lb_ind_type_flag = true;
							}
						}
					}
					System.out.println("lb_ind_type_flag after==================>>["+lb_ind_type_flag+"]");
					System.out.println("ls_proj_type after==================>>["+ls_proj_type+"]");


					if(lb_ind_type_flag && "PR01".equalsIgnoreCase(ls_proj_type))
					{
						valueXmlString.append( "<pind_apprv><![CDATA[" ).append( "" ).append( "]]></pind_apprv>\r\n" );
						valueXmlString.append( "<pind_apprv__ge><![CDATA[" ).append( "" ).append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph><![CDATA[" ).append( "" ).append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc><![CDATA[" ).append( "" ).append( "]]></pind_apprv__bc>\r\n" );
						valueXmlString.append( "<emp_fname_1><![CDATA[" ).append( "" ).append( "]]></emp_fname_1>\r\n" );
						valueXmlString.append( "<emp_lname_1><![CDATA[" ).append( "" ).append( "]]></emp_lname_1>\r\n" );
						valueXmlString.append( "<emp_fname_2><![CDATA[" ).append( "" ).append( "]]></emp_fname_2>\r\n" );
						valueXmlString.append( "<emp_lname_2><![CDATA[" ).append( "" ).append( "]]></emp_lname_2>\r\n" );
						valueXmlString.append( "<emp_fname_3><![CDATA[" ).append( "" ).append( "]]></emp_fname_3>\r\n" );
						valueXmlString.append( "<emp_lname_3><![CDATA[" ).append( "" ).append( "]]></emp_lname_3>\r\n" );
						valueXmlString.append( "<emp_fname_4><![CDATA[" ).append( "" ).append( "]]></emp_fname_4>\r\n" );
						valueXmlString.append( "<emp_lname_4><![CDATA[" ).append( "" ).append( "]]></emp_lname_4>\r\n" );

						ls_var_name=ls_site_code__del+"_PIND_APPRV";

						sql = "SELECT COUNT(1) FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, "999999");
						pstmt.setString(2, ls_var_name);
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

						if(cnt > 0)
						{
							sql = "SELECT VAR_VALUE FROM DISPARM WHERE VAR_NAME = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_var_name);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_pind_apprv = checkNullAndTrim(rs.getString("VAR_VALUE"));
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

							if(ls_pind_apprv.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv))
							{
								ls_pind_apprv = " ";
							}
							valueXmlString.append( "<pind_apprv><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}

						if("SP105".equalsIgnoreCase(ls_site_code__del))
						{
							valueXmlString.append( "<pind_apprv protect = '0'><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}
						else
						{
							valueXmlString.append( "<pind_apprv protect = '1'><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}

						ls_pind_apprv_ge = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__GE", conn));
						ls_pind_apprv_ge = (ls_pind_apprv_ge.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_ge)) ? ls_pind_apprv_ge = " " : ls_pind_apprv_ge;

						ls_pind_apprv_ph = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__PH", conn));
						ls_pind_apprv_ph = (ls_pind_apprv_ph.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_ph)) ? ls_pind_apprv_ph = " " : ls_pind_apprv_ph;

						ls_pind_apprv_bc = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__BC", conn));
						ls_pind_apprv_bc = (ls_pind_apprv_bc.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_bc)) ? ls_pind_apprv_bc = " " : ls_pind_apprv_bc;

						valueXmlString.append( "<pind_apprv__ge protect = '1'><![CDATA[" ).append(ls_pind_apprv_ge).append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph protect = '1'><![CDATA[" ).append(ls_pind_apprv_ph).append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc protect = '1'><![CDATA[" ).append(ls_pind_apprv_bc).append( "]]></pind_apprv__bc>\r\n" );


						if(ls_pind_apprv.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_3><![CDATA[" ).append(efname).append( "]]></emp_fname_3>\r\n" );
							valueXmlString.append( "<emp_lname_3><![CDATA[" ).append(elname).append( "]]></emp_lname_3>\r\n" );
						}

						if(ls_pind_apprv_ge.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_ge);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_1><![CDATA[" ).append(efname).append( "]]></emp_fname_1>\r\n" );
							valueXmlString.append( "<emp_lname_1><![CDATA[" ).append(elname).append( "]]></emp_lname_1>\r\n" );
						}
						if(ls_pind_apprv_ph.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_ph);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_2><![CDATA[" ).append(efname).append( "]]></emp_fname_2>\r\n" );
							valueXmlString.append( "<emp_lname_2><![CDATA[" ).append(elname).append( "]]></emp_lname_2>\r\n" );
						}
						if(ls_pind_apprv_bc.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_bc);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_4><![CDATA[" ).append(efname).append( "]]></emp_fname_4>\r\n" );
							valueXmlString.append( "<emp_lname_4><![CDATA[" ).append(elname).append( "]]></emp_lname_4>\r\n" );
						}
					}
					else	
					{
						valueXmlString.append( "<pind_apprv protect = '1'><![CDATA[" ).append("").append( "]]></pind_apprv>\r\n" );
						valueXmlString.append( "<pind_apprv__ge><![CDATA[" ).append("").append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph><![CDATA[" ).append("").append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc><![CDATA[" ).append("").append( "]]></pind_apprv__bc>\r\n" );
						valueXmlString.append( "<emp_fname_1><![CDATA[" ).append("").append( "]]></emp_fname_1>\r\n" );
						valueXmlString.append( "<emp_lname_1><![CDATA[" ).append("").append( "]]></emp_lname_1>\r\n" );
						valueXmlString.append( "<emp_fname_2><![CDATA[" ).append("").append( "]]></emp_fname_2>\r\n" );
						valueXmlString.append( "<emp_lname_2><![CDATA[" ).append("").append( "]]></emp_lname_2>\r\n" );
						valueXmlString.append( "<emp_fname_3><![CDATA[" ).append("").append( "]]></emp_fname_3>\r\n" );
						valueXmlString.append( "<emp_lname_3><![CDATA[" ).append("").append( "]]></emp_lname_3>\r\n" );
						valueXmlString.append( "<emp_fname_4><![CDATA[" ).append("").append( "]]></emp_fname_4>\r\n" );
						valueXmlString.append( "<emp_lname_4><![CDATA[" ).append("").append( "]]></emp_lname_4>\r\n" );
					}
				}
				if( currentColumn.trim().equalsIgnoreCase( "pind_apprv" ) )
				{
					System.out.println("------- Inside pind_apprv ------------->>" );
					mval = checkNullAndTrim(genericUtility.getColumnValue("pind_apprv", hdrDataDom));

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
					valueXmlString.append( "<emp_fname_3><![CDATA[" ).append(mval1).append( "]]></emp_fname_3>\r\n" );
					valueXmlString.append( "<emp_lname_3><![CDATA[" ).append(mval2).append( "]]></emp_lname_3>\r\n" );
				}
				if( currentColumn.trim().equalsIgnoreCase( "site_code__del" ) || currentColumn.trim().equalsIgnoreCase( "ind_type" ))
				{
					System.out.println("------- Inside site_code__del || ind_type ------------->>" );
					mval = checkNullAndTrim(genericUtility.getColumnValue("site_code__del", hdrDataDom));
					ls_ind_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", hdrDataDom));
					ls_proj_code = checkNullAndTrim(genericUtility.getColumnValue("proj_code", hdrDataDom));

					sql = "SELECT PROJ_TYPE FROM PROJECT WHERE PROJ_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_proj_code);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_proj_type = checkNullAndTrim(rs.getString("PROJ_TYPE"));
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

					ls_dis_ind_type_list = checkNullAndTrim(discommon.getDisparams("999999", "IND_TYPE_PARAM", conn));
					lb_ind_type_flag = false;

					if(ls_dis_ind_type_list.length() > 0)
					{
						String[] ls_dis_ind_type = ls_dis_ind_type_list.split(",");
						for(int i = 0; i < ls_dis_ind_type.length; i++)
						{
							if(ls_ind_type.equalsIgnoreCase(ls_dis_ind_type[i]))
							{
								lb_ind_type_flag = true;
							}
						}
					}

					if(lb_ind_type_flag && "PR01".equalsIgnoreCase(ls_proj_type))
					{
						valueXmlString.append( "<pind_apprv><![CDATA[" ).append( "" ).append( "]]></pind_apprv>\r\n" );
						valueXmlString.append( "<pind_apprv__ge><![CDATA[" ).append( "" ).append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph><![CDATA[" ).append( "" ).append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc><![CDATA[" ).append( "" ).append( "]]></pind_apprv__bc>\r\n" );
						valueXmlString.append( "<emp_fname_1><![CDATA[" ).append( "" ).append( "]]></emp_fname_1>\r\n" );
						valueXmlString.append( "<emp_lname_1><![CDATA[" ).append( "" ).append( "]]></emp_lname_1>\r\n" );
						valueXmlString.append( "<emp_fname_2><![CDATA[" ).append( "" ).append( "]]></emp_fname_2>\r\n" );
						valueXmlString.append( "<emp_lname_2><![CDATA[" ).append( "" ).append( "]]></emp_lname_2>\r\n" );
						valueXmlString.append( "<emp_fname_3><![CDATA[" ).append( "" ).append( "]]></emp_fname_3>\r\n" );
						valueXmlString.append( "<emp_lname_3><![CDATA[" ).append( "" ).append( "]]></emp_lname_3>\r\n" );
						valueXmlString.append( "<emp_fname_4><![CDATA[" ).append( "" ).append( "]]></emp_fname_4>\r\n" );
						valueXmlString.append( "<emp_lname_4><![CDATA[" ).append( "" ).append( "]]></emp_lname_4>\r\n" );

						ls_var_name=ls_site_code__del+"_PIND_APPRV";

						sql = "SELECT COUNT(1) FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, "999999");
						pstmt.setString(2, ls_var_name);
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

						if(cnt > 0)
						{
							sql = "SELECT VAR_VALUE FROM DISPARM WHERE VAR_NAME = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_var_name);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_pind_apprv = checkNullAndTrim(rs.getString("VAR_VALUE"));
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

							if(ls_pind_apprv.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv))
							{
								ls_pind_apprv = " ";
							}
							valueXmlString.append( "<pind_apprv><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}

						if("SP105".equalsIgnoreCase(ls_site_code__del))
						{
							valueXmlString.append( "<pind_apprv protect = '0'><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}
						else
						{
							valueXmlString.append( "<pind_apprv protect = '1'><![CDATA[" ).append(ls_pind_apprv).append( "]]></pind_apprv>\r\n" );
						}

						ls_pind_apprv_ge = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__GE", conn));
						ls_pind_apprv_ge = (ls_pind_apprv_ge.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_ge)) ? ls_pind_apprv_ge = " " : ls_pind_apprv_ge;

						ls_pind_apprv_ph = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__PH", conn));
						ls_pind_apprv_ph = (ls_pind_apprv_ph.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_ph)) ? ls_pind_apprv_ph = " " : ls_pind_apprv_ph;

						ls_pind_apprv_bc = checkNullAndTrim(discommon.getDisparams("999999", "PIND_APPRV__BC", conn));
						ls_pind_apprv_bc = (ls_pind_apprv_bc.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_pind_apprv_bc)) ? ls_pind_apprv_bc = " " : ls_pind_apprv_bc;

						valueXmlString.append( "<pind_apprv__ge protect = '1'><![CDATA[" ).append(ls_pind_apprv_ge).append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph protect = '1'><![CDATA[" ).append(ls_pind_apprv_ph).append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc protect = '1'><![CDATA[" ).append(ls_pind_apprv_bc).append( "]]></pind_apprv__bc>\r\n" );


						if(ls_pind_apprv.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_3><![CDATA[" ).append(efname).append( "]]></emp_fname_3>\r\n" );
							valueXmlString.append( "<emp_lname_3><![CDATA[" ).append(elname).append( "]]></emp_lname_3>\r\n" );
						}

						if(ls_pind_apprv_ge.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_ge);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_1><![CDATA[" ).append(efname).append( "]]></emp_fname_1>\r\n" );
							valueXmlString.append( "<emp_lname_1><![CDATA[" ).append(elname).append( "]]></emp_lname_1>\r\n" );
						}
						if(ls_pind_apprv_ph.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_ph);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_2><![CDATA[" ).append(efname).append( "]]></emp_fname_2>\r\n" );
							valueXmlString.append( "<emp_lname_2><![CDATA[" ).append(elname).append( "]]></emp_lname_2>\r\n" );
						}
						if(ls_pind_apprv_bc.length() > 0)
						{
							sql = "SELECT EMP_FNAME, EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_pind_apprv_bc);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								efname = checkNullAndTrim(rs.getString("EMP_FNAME"));
								elname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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
							valueXmlString.append( "<emp_fname_4><![CDATA[" ).append(efname).append( "]]></emp_fname_4>\r\n" );
							valueXmlString.append( "<emp_lname_4><![CDATA[" ).append(elname).append( "]]></emp_lname_4>\r\n" );
						}
					}
					else
					{
						valueXmlString.append( "<pind_apprv protect = '1'><![CDATA[" ).append("").append( "]]></pind_apprv>\r\n" );
						valueXmlString.append( "<pind_apprv__ge><![CDATA[" ).append("").append( "]]></pind_apprv__ge>\r\n" );
						valueXmlString.append( "<pind_apprv__ph><![CDATA[" ).append("").append( "]]></pind_apprv__ph>\r\n" );
						valueXmlString.append( "<pind_apprv__bc><![CDATA[" ).append("").append( "]]></pind_apprv__bc>\r\n" );
						valueXmlString.append( "<emp_fname_1><![CDATA[" ).append("").append( "]]></emp_fname_1>\r\n" );
						valueXmlString.append( "<emp_lname_1><![CDATA[" ).append("").append( "]]></emp_lname_1>\r\n" );
						valueXmlString.append( "<emp_fname_2><![CDATA[" ).append("").append( "]]></emp_fname_2>\r\n" );
						valueXmlString.append( "<emp_lname_2><![CDATA[" ).append("").append( "]]></emp_lname_2>\r\n" );
						valueXmlString.append( "<emp_fname_3><![CDATA[" ).append("").append( "]]></emp_fname_3>\r\n" );
						valueXmlString.append( "<emp_lname_3><![CDATA[" ).append("").append( "]]></emp_lname_3>\r\n" );
						valueXmlString.append( "<emp_fname_4><![CDATA[" ).append("").append( "]]></emp_fname_4>\r\n" );
						valueXmlString.append( "<emp_lname_4><![CDATA[" ).append("").append( "]]></emp_lname_4>\r\n" );
					}
				}
				valueXmlString.append( "</Detail1>\r\n" );
			} //Case 1. End
			break;

			case 2:
			{
				/*				System.out.println("hdrDataDom in itemchanged case 2------->>["+genericUtility.serializeDom(hdrDataDom)+"]");	
				System.out.println("currFormDataDom in itemchanged case2 ------>>["+genericUtility.serializeDom(currFormDataDom)+"]");
				System.out.println("allFormDataDom in itemchanged case2 ------>>["+genericUtility.serializeDom(allFormDataDom)+"]");*/

				valueXmlString.append( "<Detail2>\r\n" );

				if( currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{	
					//chnages by nandkumar gadkari on 28/03/18 
					mval = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					valueXmlString.append( "<item_code protect = '1'><![CDATA[" ).append(mval).append( "]]></item_code>\r\n" );
				}

				if( currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("----------------- Inside itm_default -----------------------");	

					mval = checkNullAndTrim(genericUtility.getColumnValue("ind_no", hdrDataDom));
					ls_WorkOrder = checkNullAndTrim(genericUtility.getColumnValue("work_order", hdrDataDom));
					mdate2 = checkNullAndTrim(genericUtility.getColumnValue("ind_date", hdrDataDom));
					ls_anal = checkNullAndTrim(genericUtility.getColumnValue("anal_code", hdrDataDom));

					valueXmlString.append( "<ind_no><![CDATA[" ).append(mval).append( "]]></ind_no>\r\n" );
					valueXmlString.append( "<item_code protect = '0'><![CDATA[" ).append("").append( "]]></item_code>\r\n" );
					valueXmlString.append( "<req_date><![CDATA[" ).append(mdate2).append( "]]></req_date>\r\n" );

					if(ls_WorkOrder.length() > 0)
					{
						valueXmlString.append( "<work_order><![CDATA[" ).append(ls_WorkOrder).append( "]]></work_order>\r\n" );
					}
					if(ls_anal.length() > 0)
					{
						valueXmlString.append( "<anal_code><![CDATA[" ).append(ls_anal).append( "]]></anal_code>\r\n" );
					}
				}

				if( currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					System.out.println("------- Inside item_code -------------");
					mval = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					ls_anal = checkNullAndTrim(genericUtility.getColumnValue("anal_code", hdrDataDom));
					msite = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", hdrDataDom));
					msite_del = checkNullAndTrim(genericUtility.getColumnValue("site_code__del", hdrDataDom));
					ls_dept = checkNullAndTrim(genericUtility.getColumnValue("dept_code", hdrDataDom));
					lc_qty1 = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));

					/*System.out.println("item_code  --------------->>"+mval+"]");
					System.out.println("anal_code  --------------->>"+ls_anal+"]");
					System.out.println("site_code__ori --------------->>"+msite+"]");
					System.out.println("site_code__del  --------------->>"+msite_del+"]");
					System.out.println("ls_dept  --------------->>"+ls_dept+"]");
					System.out.println("lc_qty1  --------------->>"+lc_qty1+"]");*/

					sql = "SELECT PURC_REQ_AVGCONS (?) FROM DUAL ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ld_avgqty = checkNullAndTrim(rs.getString(1));
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

					itemCnt = 0;
					sql = "SELECT DESCR, UNIT, PACK_CODE, APR_CODE, ITEM_SER, MFG_LEAD, STK_OPT, PUR_LEAD_TIME, EMP_CODE__IAPR, SUPP_CODE__PREF, " +
							" EMP_CODE__PUR, PURC_RATE, REO_QTY , UNIT__PUR, EMP_CODE__QCAPRV FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemdescr = checkNullAndTrim(rs.getString("DESCR"));
						itemunit = checkNullAndTrim(rs.getString("UNIT"));
						pckcode = checkNullAndTrim(rs.getString("PACK_CODE"));
						aprcode = checkNullAndTrim(rs.getString("APR_CODE"));

						itemser = checkNullAndTrim(rs.getString("ITEM_SER"));
						lead = checkNullAndTrim(rs.getString("MFG_LEAD"));
						mval1 = checkNullAndTrim(rs.getString("STK_OPT"));
						ld_pur_lead = checkNullAndTrim(rs.getString("PUR_LEAD_TIME"));

						ls_emp_iapr_i = checkNullAndTrim(rs.getString("EMP_CODE__IAPR"));
						ls_supp_pref = checkNullAndTrim(rs.getString("SUPP_CODE__PREF"));
						ls_emp_pur_i = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
						ld_purc_rate = checkNullAndTrim(rs.getString("PURC_RATE"));

						ld_reo_qty = checkNullAndTrim(rs.getString("REO_QTY"));
						ls_unitpur = checkNullAndTrim(rs.getString("UNIT__PUR"));
						ls_emp_code__qcaprv = checkNullAndTrim(rs.getString("EMP_CODE__QCAPRV"));
						itemCnt++;
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

					sql = "SELECT RTRIM(EMPLOYEE.EMP_FNAME) AS EMP_FNAME, RTRIM(EMPLOYEE.EMP_MNAME) AS EMP_MNAME, RTRIM(EMPLOYEE.EMP_LNAME) " +
							"AS EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_emp_code__qcaprv);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_fname = checkNullAndTrim(rs.getString("EMP_FNAME"));
						ls_mname = checkNullAndTrim(rs.getString("EMP_MNAME"));
						ls_lname = checkNullAndTrim(rs.getString("EMP_LNAME"));
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

					sql = " SELECT PUR_LEAD_TIME, SUPP_CODE__PREF, EMP_CODE__PUR, EMP_CODE__IAPR FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msite);
					pstmt.setString(2, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ld_pur_lead_i = checkNullAndTrim(rs.getString("PUR_LEAD_TIME"));
						ls_supp_pref_i = checkNullAndTrim(rs.getString("SUPP_CODE__PREF"));
						ls_emp_pur_si = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
						ls_emp_iapr_si = checkNullAndTrim(rs.getString("EMP_CODE__IAPR"));
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

					sql = " SELECT EMP_CODE__PUR, EMP_CODE__IAPR FROM ITEMSER WHERE ITEM_SER = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemser);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_emp_pur_is = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
						ls_emp_iapr_is = checkNullAndTrim(rs.getString("EMP_CODE__IAPR"));
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

					if(ld_pur_lead_i.length() > 0)
					{
						ld_pur_lead = ld_pur_lead_i;
					}
					if(ls_supp_pref_i.length() > 0)
					{
						ls_supp_pref = ls_supp_pref_i;
					}
					if(ls_emp_pur_si.length() > 0)
					{
						ls_emp_pur = ls_emp_pur_si;
					}
					else if(ls_emp_pur_i.length() > 0)
					{
						ls_emp_pur = ls_emp_pur_i;
					}
					else if(ls_emp_pur_is.length() > 0)
					{
						ls_emp_pur = ls_emp_pur_is;
					}

					if(ls_emp_iapr_si.length() > 0)
					{
						ls_emp_iapr = ls_emp_iapr_si;
					}
					else if(ls_emp_iapr_i.length() > 0)
					{
						ls_emp_iapr = ls_emp_iapr_i;
					}
					else if(ls_emp_iapr_is.length() > 0)
					{
						ls_emp_iapr = ls_emp_iapr_is;
					}

					sql = " SELECT DESCR FROM PACKING WHERE PACK_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, pckcode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						pckinstr = checkNullAndTrim(rs.getString("DESCR"));
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

					if(itemCnt > 0)
					{
						indent_type = checkNullAndTrim(genericUtility.getColumnValue("ind_type", hdrDataDom));
						// Sneha, split the cctr using "," but which acct to set in acct_code tag
						cctr = finCommon.getAcctDetrTtype(mval, itemser, "IN", indent_type, conn);
						System.out.println("cctr==============>>"+cctr);
						if(cctr.length() > 0)
						{
							acct = cctr.split(",");
						}
					}


					sql = " SELECT CCTR_CODE FROM DEPARTMENT WHERE DEPT_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_dept);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_cctr_dept = checkNullAndTrim(rs.getString("CCTR_CODE"));
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

					if(ls_cctr_dept.length() == 0)
					{
						cctr = ls_cctr_dept;
					}

					if(ld_pur_lead.length() == 0)
					{
						ld_pur_lead = "0";
					}
					ic_pur_lead_time = ld_pur_lead;

					valueXmlString.append( "<aveconsqty_atsite><![CDATA[" ).append(ld_avgqty).append( "]]></aveconsqty_atsite>\r\n" );	// Avg. Cons.
					valueXmlString.append( "<reorderqty_atsite><![CDATA[" ).append(ld_reo_qty).append( "]]></reorderqty_atsite>\r\n" );	// Reorder qty :
					valueXmlString.append( "<item_descr><![CDATA[" ).append(itemdescr).append( "]]></item_descr>\r\n" );

					if(ls_unitpur.length() == 0)
					{
						ls_unitpur = itemunit;
					}

					valueXmlString.append( "<unit><![CDATA[" ).append(ls_unitpur).append( "]]></unit>\r\n" );
					valueXmlString.append( "<unit__std><![CDATA[" ).append(itemunit).append( "]]></unit__std>\r\n" );

					if(itemunit.equalsIgnoreCase(ls_unitpur))
					{
						lc_conv = 1;
					}
					else
					{
						//  lc_tempqty is not used anywhere else.
						//lc_tempqty = gf_conv_qty_fact(ls_unitpur,itemunit, mval, 999, lc_conv)
						lc_tempqty = discommon.convQtyFactor(ls_unitpur, itemunit, mval, 999, lc_conv, conn);
					}

					valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(lc_conv).append( "]]></conv__qty_stduom>\r\n" );	// UOM Con :
					valueXmlString.append( "<pack_code><![CDATA[" ).append(pckcode).append( "]]></pack_code>\r\n" );
					valueXmlString.append( "<packing_descr><![CDATA[" ).append(pckinstr).append( "]]></packing_descr>\r\n" );

					valueXmlString.append( "<apr_code><![CDATA[" ).append(aprcode).append( "]]></apr_code>\r\n" );  // Appr Code :
					valueXmlString.append( "<acct_code><![CDATA[" ).append(acct[0]).append( "]]></acct_code>\r\n" );	// Account :
					valueXmlString.append( "<cctr_code><![CDATA[" ).append(acct[1]).append( "]]></cctr_code>\r\n" );	// Cost Centre :
					valueXmlString.append( "<emp_code__iapr><![CDATA[" ).append(ls_emp_iapr).append( "]]></emp_code__iapr>\r\n" );	// Indent Appr 
					valueXmlString.append( "<emp_code__pur><![CDATA[" ).append(ls_emp_pur).append( "]]></emp_code__pur>\r\n" );		// Purchaser :
					valueXmlString.append( "<pur_lead_time><![CDATA[" ).append(ld_pur_lead).append( "]]></pur_lead_time>\r\n" );	// Lead Time:
					valueXmlString.append( "<supp_code__pref><![CDATA[" ).append(ls_supp_pref).append( "]]></supp_code__pref>\r\n" );	// Pref Supplier:

					ld_ind_date = checkNullAndTrim(genericUtility.getColumnValue("ind_date", hdrDataDom));
					//Added by wasim on 07-apr-2017 for getting indent date in DB formate
					String indDateStr = genericUtility.getValidDateTimeString(checkNullAndTrim(ld_ind_date), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());

					sql = "SELECT TRACK_SHELF_LIFE, QC_REQD FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_track_shelf_life = checkNullAndTrim(rs.getString("TRACK_SHELF_LIFE"));
						ls_qc_reqd = checkNullAndTrim(rs.getString("QC_REQD"));
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
					System.out.println("----------------------- ls_track_shelf_life --------------------->>"+ls_track_shelf_life);
					System.out.println("----------------------- ls_qc_reqd --------------------->>" + ls_qc_reqd);

					if("Y".equalsIgnoreCase(ls_track_shelf_life) && "Y".equalsIgnoreCase(ls_qc_reqd))
					{
						sql = "SELECT SUM(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END - CASE WHEN A.HOLD_QTY IS NULL " +
								" THEN 0 ELSE A.HOLD_QTY END) AS QTY FROM STOCK A, INVSTAT B WHERE A.INV_STAT  = B.INV_STAT AND A.ITEM_CODE = ? AND	" +
								" A.SITE_CODE = ? AND B.AVAILABLE = ? AND " +
								" ( A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END - CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) > 0 " +
								" AND (A.EXP_DATE >= ? OR A.EXP_DATE IS NULL) AND A.RETEST_DATE >= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval);
						pstmt.setString(2, msite_del);
						pstmt.setString(3, "Y");
						//Changed by wasim on 07-APR-17 for setting pstmt into Timestamp [START]
						//pstmt.setString(4, ld_ind_date);
						//pstmt.setString(5, ld_ind_date);
						pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(indDateStr));
						pstmt.setTimestamp(5, java.sql.Timestamp.valueOf(indDateStr));
						//Changed by wasim on 07-APR-17 for setting pstmt into Timestamp [END]
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							qty1 = rs.getInt("QTY");
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
					else if("Y".equalsIgnoreCase(ls_track_shelf_life))
					{	
						sql = "SELECT SUM(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END - CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) AS QTY " +
								" FROM STOCK A, INVSTAT B WHERE	A.INV_STAT  = B.INV_STAT AND A.ITEM_CODE = ? AND A.SITE_CODE = ? AND B.AVAILABLE = ? AND " +
								" (A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END - CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END ) > 0  " +
								" AND (A.EXP_DATE >= ? OR A.EXP_DATE IS NULL) AND A.RETEST_DATE >= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval);
						pstmt.setString(2, msite_del);
						pstmt.setString(3, "Y");
						//pstmt.setString(4, ld_ind_date);
						//pstmt.setString(5, ld_ind_date);
						pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(indDateStr));
						pstmt.setTimestamp(5, java.sql.Timestamp.valueOf(indDateStr));
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							qty1 = rs.getInt("QTY");
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
					else
					{	
						sql = " SELECT SUM(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END - CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) AS QTY " +
								" FROM STOCK A, INVSTAT B WHERE	A.INV_STAT  = B.INV_STAT AND A.ITEM_CODE = ? AND A.SITE_CODE = ? AND B.AVAILABLE = ? " +
								" AND ( A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END - CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END ) > 0 ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval);
						pstmt.setString(2, msite_del);
						pstmt.setString(3, "Y");
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							qty1 = rs.getInt("QTY");
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

					java.sql.Date indDate = java.sql.Date.valueOf(genericUtility .getValidDateString(ld_ind_date, genericUtility .getApplDateFormat(), genericUtility .getDBDateFormat()));
					sql = " SELECT PORDDET.RATE FROM PORDDET, PORDER WHERE ( PORDDET.PURC_ORDER  = PORDER.PURC_ORDER ) AND " +
							"(( PORDDET.SITE_CODE  = ? ) AND ( PORDER.ORD_DATE < ? ) AND ( PORDDET.ITEM_CODE  = ? ) AND ( PORDER.CONFIRMED 	 = ?)) " +
							"ORDER BY PORDER.ORD_DATE DESC ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msite_del);
					pstmt.setDate(2, indDate);
					pstmt.setString(3, mval);
					pstmt.setString(4, "Y");
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						ld_last_purrate = rs.getDouble("RATE");
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

					lc_qty1 = (lc_qty1.length() == 0) ? "0" : lc_qty1;          
					double tempQty = Double.parseDouble(lc_qty1) * lc_conv;

					/*System.out.println("qty1  --------------->>["+qty1+"]");
					System.out.println("ld_last_purrate  --------------->>["+ld_last_purrate+"]");
					System.out.println("lc_qty1  --------------->>["+lc_qty1+"]");
					System.out.println("tempQty  --------------->>["+tempQty+"]");
					System.out.println("ls_emp_code__qcaprv  --------------->>["+ls_emp_code__qcaprv+"]");
					System.out.println("ls_fname  --------------->>["+ls_fname+"]");
					System.out.println("ls_mname  --------------->>["+ls_mname+"]");
					System.out.println("ls_lname  --------------->>["+ls_lname+"]");*/

					valueXmlString.append( "<quantity_atsite><![CDATA[" ).append(qty1).append( "]]></quantity_atsite>\r\n" );	// Stock at Site:   
					valueXmlString.append( "<purc_rate><![CDATA[" ).append(ld_last_purrate).append( "]]></purc_rate>\r\n" );	// Apprx Rate :               
					valueXmlString.append( "<quantity><![CDATA[" ).append(lc_qty1).append( "]]></quantity>\r\n" );
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(tempQty).append( "]]></quantity__stduom>\r\n" );	// Std Qty :
					//Changed by wasim on 07-APR-17 if QC_REQD is Y then QC approver field should be editable [START]
					//valueXmlString.append( "<emp_code__qcaprv><![CDATA[" ).append(ls_emp_code__qcaprv).append( "]]></emp_code__qcaprv>\r\n" );	// QC Approvar:
					if("Y".equals(ls_qc_reqd))
					{	
						valueXmlString.append( "<emp_code__qcaprv protect = '0'><![CDATA[" ).append(ls_emp_code__qcaprv).append( "]]></emp_code__qcaprv>\r\n" );	// QC Approvar:
					}
					else
					{
						valueXmlString.append( "<emp_code__qcaprv protect = '1'><![CDATA[" ).append(ls_emp_code__qcaprv).append( "]]></emp_code__qcaprv>\r\n" );
					}
					//Changed by wasim on 07-APR-17 if QC_REQD is Y then QC approver field should be editable [END]
					valueXmlString.append( "<emp_fname><![CDATA[" ).append(ls_fname).append( "]]></emp_fname>\r\n" );	// QC Approvar First name:      
					valueXmlString.append( "<emp_mname><![CDATA[" ).append(ls_mname).append( "]]></emp_mname>\r\n" );	// QC Approvar middle name:     
					valueXmlString.append( "<emp_lname><![CDATA[" ).append(ls_lname).append( "]]></emp_lname>\r\n" );	// QC Approvar last name:     
					valueXmlString.append( "<last_pur_rate><![CDATA[" ).append(ld_last_purrate).append( "]]></last_pur_rate>\r\n" );	// Last Pur Rate:               

					ls_site_code__ori = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", hdrDataDom));
					sql = " SELECT COUNT(1) FROM ITEMREGNO WHERE ITEM_CODE = ? AND SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					pstmt.setString(2, ls_site_code__ori);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						li_cnt = rs.getInt(1);
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

					if(li_cnt > 0)
					{
						sql = " SELECT DISTINCT ITEMMNFR_DET.SUPP_CODE__MNFR FROM ITEMMNFR_DET,ITEMMNFR WHERE " +
								"ITEMMNFR.ITEM_CODE = ITEMMNFR_DET.ITEM_CODE AND ITEMMNFR.SUPP_CODE__MNFR =ITEMMNFR_DET.SUPP_CODE__MNFR AND " +
								"ITEMMNFR.STATUS = ?  AND ITEMMNFR.ITEM_CODE = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, "A");
						pstmt.setString(2, mval);
						rs = pstmt.executeQuery();
						li_ctr = 1;
						while(rs.next())
						{
							ls_smnfr_code = checkNullAndTrim(rs.getString("SUPP_CODE__MNFR"));
							if(ls_smnfr_code.length() > 0)
							{
								if(li_ctr == 1)
								{
									ls_allsmnfr_code = ls_smnfr_code; 
								}
								else
								{
									ls_allsmnfr_code = ls_allsmnfr_code + ',' + ls_smnfr_code;
								}
								li_ctr++;
							}
							ls_smnfr_code = " ";
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

						// Sneha
						//ls_allsmnfr_code =  mid(ls_allsmnfr_code,1,len(ls_allsmnfr_code)-1) 
						if(ls_allsmnfr_code.trim().length() > 0)
						{
							ls_allsmnfr_code = ls_allsmnfr_code.substring(1, ls_allsmnfr_code.length() -1);
						}
					}

					valueXmlString.append( "<supp_code__mnfr><![CDATA[" ).append(ls_allsmnfr_code).append( "]]></supp_code__mnfr>\r\n" );

					sql = "SELECT FN_GET_BUDGET_AMT(?, ?, ?, ?, ?, ?, ?) , FN_GET_CONS_AMT(?,?,?,?,?,?,?) FROM DUAL ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "R-IND");
					pstmt.setString(2, msite);
					pstmt.setString(3, acct[0]);
					pstmt.setString(4, acct[1]);
					pstmt.setString(5, ls_anal);
					pstmt.setString(6, ls_dept);
					pstmt.setString(7, "A");
					pstmt.setString(8, "R-IND");
					pstmt.setString(9, msite);
					pstmt.setString(10, acct[0]);
					pstmt.setString(11, acct[1]);
					pstmt.setString(12, ls_anal);
					pstmt.setString(13, ls_dept);
					pstmt.setString(14, "A");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_budget_amt_anal = checkNullAndTrim(rs.getString(1));
						ls_consumed_amt_anal = checkNullAndTrim(rs.getString(2));
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

					ls_budget_amt_anal = (ls_budget_amt_anal.length() == 0) ? "0" : ls_budget_amt_anal;
					ls_consumed_amt_anal = (ls_consumed_amt_anal.length() == 0) ? "0" : ls_consumed_amt_anal;
					lc_budget_amt = Double.parseDouble(ls_budget_amt_anal) - Double.parseDouble(ls_consumed_amt_anal);

					valueXmlString.append( "<budget_amt_anal><![CDATA[" ).append(ls_budget_amt_anal).append( "]]></budget_amt_anal>\r\n" );
					valueXmlString.append( "<consumed_amt_anal><![CDATA[" ).append(ls_consumed_amt_anal).append( "]]></consumed_amt_anal>\r\n" );
					valueXmlString.append( "<budget_amt><![CDATA[" ).append(lc_budget_amt).append( "]]></budget_amt>\r\n" );

				}
				if( currentColumn.trim().equalsIgnoreCase("quantity"))
				{		
					System.out.println("------- Inside quantity -------------");
					qtyStr = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));
					mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					mval = checkNullAndTrim(genericUtility.getColumnValue("site_code__ori", hdrDataDom));
					ls_unit_std = checkNullAndTrim(genericUtility.getColumnValue("unit__std", currFormDataDom));
					lc_conv1 = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", currFormDataDom));
					itemunit = checkNullAndTrim(genericUtility.getColumnValue("unit", currFormDataDom));
					String lc_tempconv = lc_conv1;
					qtyStr = (qtyStr.length() == 0) ? "0" : qtyStr;  

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
						valueXmlString.append( "<unit><![CDATA[" ).append(itemunit).append( "]]></unit>\r\n" );
						qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr), Double.parseDouble(lc_conv1), conn);
						System.out.println("qty ============>>"+qty);
					}
					else
					{
						qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr), Double.parseDouble(lc_conv1), conn);
						System.out.println("qty 1111111111 ============>>"+qty);
					}

					System.out.println("lc_tempconv  ============>>"+lc_tempconv);
					if("0".equalsIgnoreCase(lc_tempconv))
					{
						// start - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
						//valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(lc_conv1).append( "]]></conv__qty_stduom>\r\n" );
						valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></conv__qty_stduom>\r\n" );
					}
					//valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></quantity__stduom>\r\n" );
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );
					// End - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]

					sql = "SELECT PURC_RATE FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval1);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ld_rate = rs.getDouble("PURC_RATE");
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

					System.out.println("ld_rate  ============>>"+ld_rate);
					double temp =  (Double) qty.get(0);
					ld_amt = temp * ld_rate;

					System.out.println("ld_amt  ============>>"+ld_amt);

					sql = "SELECT MAX(MAX_VALUE) FROM SITEITEM_PUR_LMT WHERE SITE_CODE = ? AND ITEM_CODE = ? AND MAX_VALUE <= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mval);
					pstmt.setString(2, mval1);
					pstmt.setDouble(3, ld_amt);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ld_maxval = rs.getInt(1);
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

					System.out.println("ld_maxval  ============>>"+ld_maxval);
					System.out.println("ld_amt aaaaa ============>>"+ld_amt);
					if(ld_maxval > ld_amt)
					{
						sql = "SELECT EMP_CODE__PUR FROM SITEITEM_PUR_LMT WHERE SITE_CODE = ? AND ITEM_CODE = ? AND MAX_VALUE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval);
						pstmt.setString(2, mval1);
						pstmt.setInt(3, ld_maxval);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_emppurc = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
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

					System.out.println("ls_emppurc  ============>>"+ls_emppurc);
					if(ls_emppurc.length() == 0)
					{
						sql = "SELECT EMP_CODE__PUR, ITEM_SER FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mval1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_emppurc = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
							ls_itemser = checkNullAndTrim(rs.getString("ITEM_SER"));
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

						if(ls_emppurc.length() == 0)
						{
							sql = "SELECT EMP_CODE__PUR FROM ITEMSER WHERE ITEM_SER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_itemser);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								ls_emppurc = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
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
					}
					valueXmlString.append( "<emp_code__pur><![CDATA[" ).append(ls_emppurc).append( "]]></emp_code__pur>\r\n" );
				}	

				if( currentColumn.trim().equalsIgnoreCase( "unit" ) )
				{
					System.out.println("------- Inside unit -------------");
					itemunit = checkNullAndTrim(genericUtility.getColumnValue("unit", currFormDataDom));
					ls_unit_std = checkNullAndTrim(genericUtility.getColumnValue("unit__std", currFormDataDom));
					mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					qtyStr = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));

					System.out.println("itemunit  --------------->>["+itemunit+"]");
					System.out.println("ls_unit_std  --------------->>["+ls_unit_std+"]");
					System.out.println("mval1  --------------->>["+mval1+"]");
					System.out.println("qtyStr  --------------->>["+qtyStr+"]");

					qtyStr = (qtyStr.length() == 0) ? "0" : qtyStr;
					lc_conv = 0;
					qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr), lc_conv, conn);
					System.out.println("------- qty -------------"+qty);
					//valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(lc_conv).append( "]]></conv__qty_stduom>\r\n" );	//// UOM Con :
					// start - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
					valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></conv__qty_stduom>\r\n" );
					// End - Modify by Kailasg on 4-feb-2020 [PO Qty is not showing as per Indent Qty]
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );	//// Std Qty :  qty.get(1) set as 1 by nandkumar gadkari on 05/07/19

				}

				if( currentColumn.trim().equalsIgnoreCase( "conv__qty_stduom" ) )	//// UOM Con :
				{
					System.out.println("------- Inside conv__qty_stduom -------------");
					lc_conv1 = checkNullAndTrim(genericUtility.getColumnValue("conv__qty_stduom", currFormDataDom));
					itemunit = checkNullAndTrim(genericUtility.getColumnValue("unit", currFormDataDom));
					ls_unit_std = checkNullAndTrim(genericUtility.getColumnValue("unit__std", currFormDataDom));
					mval1 = checkNullAndTrim(genericUtility.getColumnValue("item_code", currFormDataDom));
					qtyStr = checkNullAndTrim(genericUtility.getColumnValue("quantity", currFormDataDom));

					qtyStr = (qtyStr.length() == 0) ? "0" : qtyStr;
					qty = discommon.convQtyFactor(itemunit, ls_unit_std, mval1, Double.parseDouble(qtyStr), Double.parseDouble(lc_conv1), conn);

					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );	//// Std Qty :  qty.get(1) set as 1 by nandkumar gadkari on 05/07/19
				}	
				if( currentColumn.trim().equalsIgnoreCase( "pack_code" ) )
				{
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
				if( currentColumn.trim().equalsIgnoreCase( "supp_code__pref" ) )	//// Pref Supplier:
				{
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
					valueXmlString.append( "<supp_name><![CDATA[" ).append(mval1).append( "]]></supp_name>\r\n" );
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
						itemdescr = checkNullAndTrim(rs.getString("DESCR"));
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
					valueXmlString.append( "<item_descr_mfg><![CDATA[" ).append(itemdescr).append( "]]></item_descr_mfg>\r\n" );
				}

				//added by manish mhatre on 17-03-2021
				//start manish
				if(currentColumn.trim().equalsIgnoreCase( "no_art" ) || currentColumn.trim().equalsIgnoreCase( "dimension" ) )
				{
					System.out.println("Inside no_art block or dimension block");
					String itemCode="",unit="",dimension="",noArtStr="";
					double quantity=0,noArt=0;
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
							System.out.println("after quantity itemchanged 4711.......");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
					}
				}
				//end manish


				valueXmlString.append( "</Detail2>\r\n" );
			} //Case 2. End
			break;

			}//End of switch block
			valueXmlString.append( "</Root>\r\n" );	 
		}
		catch (Exception e)
		{
			System.out.println("Exception inside Item change IndentReqIC==>"+e.getMessage());		
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


	public String checkRefCode(String as_item_code, String as_site_code, Connection conn)
	{
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int count = 0, icnt = 0;
		String errCode = "";
		ArrayList<String> irefcode = new ArrayList<String>();
		try
		{
			System.out.println("------------ Inside checkRefCode -------------------");

			sql = "SELECT COUNT(*) FROM ITEMREGNO WHERE ITEM_CODE = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, as_item_code);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				icnt = rs.getInt(1);
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
			if(icnt == 0)
			{
				errCode = "VMNOIREF";
				return errCode;
			}

			sql = "SELECT REF_CODE FROM ITEMREGNO WHERE SITE_CODE = ? AND ITEM_CODE = ? ORDER BY REF_CODE ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, as_site_code);
			pstmt.setString(2, as_item_code);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				irefcode.add(checkNullAndTrim(rs.getString("REF_CODE")));				
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

			System.out.println("irefcode.length----------->>"+irefcode.size());
			count = 0;
			for(int i = 0; i < irefcode.size(); i++)
			{				
				sql = "SELECT COUNT(*)  FROM SITEREGNO WHERE SITE_CODE = ? AND REF_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, as_site_code);
				pstmt.setString(2, (String) irefcode.get(i));
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
					errCode = "VMNOREF";
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


	private String getObjNameFromDom( Document dom, String attribute, String objContext ) throws RemoteException,ITMException
	{
		NodeList detailList = null;
		Node currDetail = null, reqDetail = null;
		String objName = "";
		int	detailListLength = 0;

		try
		{
			detailList = dom.getElementsByTagName("Detail"+objContext);
			detailListLength = detailList.getLength();			
			for (int ctr = 0; ctr < detailListLength; ctr++)
			{				
				currDetail = detailList.item(ctr);
				objName = currDetail.getAttributes().getNamedItem(attribute).getNodeValue();
			}			
		}
		catch ( Exception e )
		{
			throw new ITMException(e);
		}
		return objName;
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
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}


}
