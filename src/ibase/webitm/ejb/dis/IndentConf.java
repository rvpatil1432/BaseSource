/**
 * PURPOSE : IndentConf component
 * AUTHOR : Sneha Mestry
 * DATE : 04-01-2017
 */

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import java.sql.Timestamp;

import javax.ejb.Stateless;

import org.w3c.dom.*;

@Stateless
public class IndentConf extends ActionHandlerEJB implements IndentConfLocal, IndentConfRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	DistCommon discommon = new DistCommon();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();

	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
		System.out.println(" ========= Inside IndentConf confirm ============= ");
		System.out.println(" =========  tranId ============= "+tranId);
		System.out.println("xtraParams ::::::::::::: " + xtraParams);

		String errString = "", sql = "", childNodeName = "", userId = "", errorType = "", errCode = "", ld_conf_date = "", ls_runopt = "";
		boolean isError = false;
		int cnt = 0;
		
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String> errFields = new ArrayList <String>();

		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		
		String ls_confirm = "", mstatus = "", ls_emp_code__iapr = "", ls_empcodepur = "", ls_sitecode = "", ls_itemcode = "", ls_login_emp = "",
				li_level = "", ls_ecode = "";

		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try
		{
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));
			ls_runopt = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "runopt" ));

			if ( conn == null )
			{
				conn = getConnection();
			}
					
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currDate = new Date();
			ld_conf_date = sdf.format(currDate);
				
			sql = "SELECT STATUS, EMP_CODE__IAPR, EMP_CODE__PUR, SITE_CODE, ITEM_CODE FROM INDENT WHERE IND_NO = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mstatus = checkNullAndTrim(rs.getString("STATUS"));
				ls_emp_code__iapr = checkNullAndTrim(rs.getString("EMP_CODE__IAPR"));
				ls_empcodepur = checkNullAndTrim(rs.getString("EMP_CODE__PUR"));
				ls_sitecode = checkNullAndTrim(rs.getString("SITE_CODE"));
				ls_itemcode = checkNullAndTrim(rs.getString("ITEM_CODE"));
				
			}
			else
			{
				errCode = "VTMCONF20";		
				errList.add( errCode );
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
			
			//Added by sarita on 07 JUN 2018 to show error message if status is A=Approved , O=Ordered , C=Close , L=Complete and X=Cancelled [START]
			if("A".equalsIgnoreCase(mstatus))
			{
				errCode = "VTINDCNF01";	 //Indent is Already Approved , Cannot Approve Again.	
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}
			else if ("O".equalsIgnoreCase(mstatus))
			{
				errCode = "VTINDCNF02";	//Selected Indent is Ordered , Cannot Approved.	
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}
			else if("C".equalsIgnoreCase(mstatus))
			{
				errCode = "VTINDCNF03";	//Select Indent is Closed , Cannot Approve.	
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}
			else if("L".equalsIgnoreCase(mstatus))
			{
				errCode = "VTINDCNF04";	//Select Indent is Completed , Cannot Approve.	 		
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}
			else if("X".equalsIgnoreCase(mstatus))
			{
				errCode = "VTINDCNF05";	//Selected Indent is Cancelled , Cannot Approve.	
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}			
			//Added by sarita on 07 JUN 2018 to show error message if status is A=Approved , O=Ordered , C=Close , L=Complete and X=Cancelled [END]
			//if("U".equalsIgnoreCase(mstatus))
			else if("U".equalsIgnoreCase(mstatus))
			{
				sql = "SELECT EMP_CODE, USR_LEV FROM USERS WHERE CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, userId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_login_emp = checkNullAndTrim(rs.getString("EMP_CODE"));
					li_level = checkNullAndTrim(rs.getString("USR_LEV"));
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
				
				//commented  by Nandkumar gadkari on 26/03/18 To remove mandatory purchaser and approver employee code------------Start-------------
				/*if(ls_emp_code__iapr.length() == 0)
				{
					errCode = "VTNOAPRRHT";
					errList.add( errCode );
					errFields.add(childNodeName.toLowerCase());
					//ls_errcode = 'VTNOAPRRHT~t'+'Not Authorised For Approval or Indent Approver is null'
				}*/
				//commented by Nandkumar gadkari on 26/03/18 To remove mandatory purchaser and approver employee code------------end-------------
			
				//Added by sarita as this code is aaplicable only for mstatus = 'U' hense shifted in this condition on 07 JUN 2018 [START]
				sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, userId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_ecode = checkNullAndTrim(rs.getString("EMP_CODE"));
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
				if(ls_ecode.equalsIgnoreCase(ls_emp_code__iapr))
				{
					mstatus = "A";
				}
				
				if(errCode.length() == 0)
				{	// CHANGES IN IF CONDITION by Nandkumar gadkari on 26/03/18 ---
					if((ls_emp_code__iapr.length() > 0 && li_level.length() > 0 )&& !ls_login_emp.equalsIgnoreCase(ls_emp_code__iapr))
					{
						errCode = roleUser(ls_sitecode, ls_itemcode, userId, "IND", ls_runopt, conn);
						System.out.println("errCode for roleUser ["+errCode+"]");
					}
				}
				
				if(errCode.length() == 0)
				{
					errCode = approveIndent(tranId, "1", userId, xtraParams, conn);
					System.out.println("errCode for approveIndent ["+errCode+"]");				
				}
				
				if(errCode.length() != 0)
				{
					isError = true;
					//Added by sarita on 06 FEBRUARY 2019 [START]
					errList.add( errCode );
					errFields.add(childNodeName.toLowerCase());
					//Added by sarita on 06 FEBRUARY 2019 [END]
				}
				else if(errCode.length() == 0)
				{
					isError = false;
					//Changed by wasim on 13-JUN-17 to change proper messages code.
					//errCode = "CONFSUCESS";
					errCode = "VTINDAPR";
					errList.add( errCode );
					errFields.add(childNodeName.toLowerCase());
				}
				//Added by sarita as this code is aplicable only for mstatus = 'U' hense shifted in this condition on 07 JUN 2018 [END]		
			}
			//Commented by sarita as this code is aplicable only for mstatus = 'U' hense shifted in particular 'else if("U".equalsIgnoreCase(mstatus))' condition on 07 JUN 2018 [START] 
			/*sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_ecode = checkNullAndTrim(rs.getString("EMP_CODE"));
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
			if(ls_ecode.equalsIgnoreCase(ls_emp_code__iapr))
			{
				mstatus = "A";
			}*/
			//Commented by sarita as this code is aplicable only for mstatus = 'U' hense shifted in particular 'else if("U".equalsIgnoreCase(mstatus))' condition on 07 JUN 2018 [END]
			
			//commented  by Nandkumar gadkari on 26/03/18 To remove mandatory purchaser and approver employee code------------Start-------------

			/*else
			{
				errCode = "VTEAINAPP";
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}*/
			//commented  by Nandkumar gadkari on 26/03/18 To remove mandatory purchaser and approver employee code------------end-------------
			
			//Commented by sarita as this code is aaplication only for mstatus = 'U' hense shifted in particular 'else if("U".equalsIgnoreCase(mstatus))' condition on 07 JUN 2018 [START]
			/*if(errCode.length() == 0)
			{	// CHANGES IN IF CONDITION by Nandkumar gadkari on 26/03/18 ---
				if((ls_emp_code__iapr.length() > 0 && li_level.length() > 0 )&& !ls_login_emp.equalsIgnoreCase(ls_emp_code__iapr))
				{
					errCode = roleUser(ls_sitecode, ls_itemcode, userId, "IND", ls_runopt, conn);
				}
			}
			
			if(errCode.length() == 0)
			{
				errCode = approveIndent(tranId, "1", userId, xtraParams, conn);
			}
			
			if(errCode.length() != 0)
			{
				isError = true;
			}
			else if(errCode.length() == 0)
			{
				isError = false;
				//Changed by wasim on 13-JUN-17 to change proper messages code.
				//errCode = "CONFSUCESS";
				errCode = "VTINDAPR";
				errList.add( errCode );
				errFields.add(childNodeName.toLowerCase());
			}*/
			//Commented by sarita as this code is aplicable only for mstatus = 'U' hense shifted in particular 'else if("U".equalsIgnoreCase(mstatus))' condition on 07 JUN 2018 [END]
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
		catch(Exception e)
		{
			try
			{
				System.out.println("Exception "+e.getMessage());
				e.printStackTrace();			
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				isError = true;
				e1.printStackTrace();
				throw new ITMException(e1);
			}
		}
		finally
		{
			try
			{				
				if( !isError  )
				{
					System.out.println("----------commmit-----------");
					conn.commit(); 
				}
				else if ( isError )
				{
					System.out.println("--------------rollback------------");
					conn.rollback();
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
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	
	}

	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input.trim();
	}

	private String checkNullAndTrim( String inputVal )
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

	public String errorType( Connection conn, String errorCode )
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " select msg_type from messages where msg_no =  ? ";
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

	public String roleUser(String ls_sitecode, String ls_itemcode, String userId, String val, String ls_runopt, Connection conn) throws Exception
	{
		System.out.println("----------- Inside roleUser ----------------");
		String sql = "", sql1 = "", ls_errcode = "";
		PreparedStatement pstmt = null,  pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		int cnt = 0;

		String ls_aprvrole = "";
		try
		{
			if(!"*".equalsIgnoreCase(ls_runopt))
			{
				if("IND".equalsIgnoreCase(val))
				{
					sql = "SELECT ROLE_CODE__INDAPRV FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sitecode);
					pstmt.setString(2, ls_itemcode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_aprvrole = checkNullAndTrim(rs.getString("ROLE_CODE__INDAPRV"));
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
					
					if(ls_aprvrole.length() == 0)
					{
						sql = "SELECT ROLE_CODE__INDAPRV FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_aprvrole = checkNullAndTrim(rs.getString("ROLE_CODE__INDAPRV"));
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
				else if("QC".equalsIgnoreCase(val))
				{
					sql = "SELECT ROLE_CODE__QCAPRV FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sitecode);
					pstmt.setString(2, ls_itemcode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_aprvrole = checkNullAndTrim(rs.getString("ROLE_CODE__QCAPRV"));
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
					
					if(ls_aprvrole.length() == 0)
					{
						sql = "SELECT ROLE_CODE__QCAPRV  FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_itemcode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ls_aprvrole = checkNullAndTrim(rs.getString("ROLE_CODE__QCAPRV"));
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
				
				//Commented and Added by sarita on 13 FEBRUARY 2019 [START]
				//if(ls_aprvrole.length() == 0)
				if(ls_aprvrole != null && ls_aprvrole.length() > 0)
				{
				//Commented and Added by sarita on 13 FEBRUARY 2019 [END]
					sql = "SELECT COUNT(*) FROM WF_ROLE_USERS WHERE ROLE_CODE = ? AND USERID = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_aprvrole);
					pstmt.setString(2, userId);
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
						//ls_errcode = "VTNOAPRRHT";Commented by Nandkumar Gadkari on 07/08/18
						//Commented and Added by sarita on 06 FEBRUARY 2018 [START] 
						ls_errcode = "VTNOAPRRHT";
						//Commented and Added by sarita on 06 FEBRUARY 2018 [END] 
						/*ls_errcode = itmDBAccess.getErrorString("","VTNOAPRRHT","","",conn);
						return ls_errcode;*/
						// ls_errcode = 'VTNOAPRRHT~t'+'No valid role exist for role code: '+ls_aprvrole+' for userID: '+as_userid
					}
				}
				else
				{
					//ls_errcode = "VTNOAPRRHT"; Commented by Nandkumar Gadkari on 07/08/18
					//Commented and Added by sarita on 06 FEBRUARY 2018 [START] 
					ls_errcode = "VTNOAPRRHT";
					//Commented and Added by sarita on 06 FEBRUARY 2018 [END] 
					/*ls_errcode = itmDBAccess.getErrorString("","VTNOAPRRHT","","",conn);
					return ls_errcode;*/
					//ls_errcode = 'VTNOAPRRHT~t'+'Not Authorised For Approval or Indent Approver is null'
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in roleUser ============>> "+e);
		}
		finally
		{

			try
			{				
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
				e.printStackTrace();
				throw new ITMException(e);
			}
		
		}
		System.out.println("return error code =========>> "+ls_errcode);
		return ls_errcode;
	}
	
	public String approveIndent(String tranId, String val, String userId, String xtraParams, Connection conn) throws Exception
	{
		System.out.println("----------- Inside approveIndent ----------------");
		
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null; 
		
		String sql = "", sql1 = "", ls_errcode = "", ecode = "", ls_task_code = "", ls_item_code = "", ls_proj_code = "", ls_indtype = "", 
			ls_specific_instr = "", ls_act_code = "", ls_fl_id = "", ls_conf_unit = "", ls_ind_type = "", ls_type_allow_projbudgt_list = "", 
			ls_old_indno = "", currDate = "";
		/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
		int cnt = 0;// lc_ind_qty = 0, lc_porder_qty = 0, lc_indent_qty = 0, lc_total_qty = 0, lc_proj_est_qty = 0;
		double lc_ind_qty = 0, lc_porder_qty = 0, lc_indent_qty = 0, lc_total_qty = 0, lc_proj_est_qty = 0;
		String siteCode = "";
		Timestamp reqDate = null;
		HashMap demandSupplyMap = null;
		InvDemSuppTraceBean invDemSupTrcBean = null;
		/**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
		double mrate = 0.0, mmax_rate = 0.0, lc_tot_poamt = 0.0, lc_po_amt1 = 0.0, lc_porcp_amt = 0.0, lc_pret_amt = 0.0, lc_tot_poamt11 = 0.0, 
			lc_po_amt12 = 0.0, lc_porcp_amt11 = 0.0, lc_pret_amt11 = 0.0, lc_tot_amt_proj = 0.0, lc_tot_amt = 0.0, lc_ind_amount = 0.0, 
			lc_approxcost = 0.0, lc_exceed_amt = 0.0;
		boolean lb_ord_flag = false;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = sdf.format(new java.util.Date());
		
			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ecode = checkNullAndTrim(rs.getString("EMP_CODE"));
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
			/**Modified by Pavan Rane 24dec19 start[to get qty in double instead of integer as to be update demand/supply in summary table(RunMRP process) related changes]*/
			invDemSupTrcBean = new InvDemSuppTraceBean();
			demandSupplyMap = new HashMap();
			//sql = "SELECT TASK_CODE, ITEM_CODE, QUANTITY, PROJ_CODE, IND_TYPE, SPECIFIC_INSTR FROM INDENT WHERE IND_NO = ? AND STATUS = ? ";
			sql = "SELECT TASK_CODE, ITEM_CODE, QUANTITY, PROJ_CODE, IND_TYPE, SPECIFIC_INSTR, SITE_CODE, REQ_DATE FROM INDENT WHERE IND_NO = ? AND STATUS = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2, "U");
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_task_code = checkNullAndTrim(rs.getString("TASK_CODE"));
				ls_item_code = checkNullAndTrim(rs.getString("ITEM_CODE"));
				//lc_ind_qty = rs.getInt("QUANTITY");
				lc_ind_qty = rs.getDouble("QUANTITY");
				ls_proj_code = checkNullAndTrim(rs.getString("PROJ_CODE"));
				ls_indtype = checkNullAndTrim(rs.getString("IND_TYPE"));
				ls_specific_instr = checkNullAndTrim(rs.getString("SPECIFIC_INSTR"));
				
				siteCode = checkNullAndTrim(rs.getString("SITE_CODE"));
				reqDate = rs.getTimestamp("REQ_DATE");
				/**Modified by Pavan Rane 24dec19 end[to get qty in double instead of integer as to be update demand/supply in summary table(RunMRP process) related changes]*/
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
		
			if(ls_task_code.length() > 0)
			{
				sql = "SELECT SUM(B.QUANTITY) FROM PORDER A ,PORDDET B WHERE A.PURC_ORDER = B.PURC_ORDER AND A.CONFIRMED = ? AND A.TASK_CODE = ? " 
					+" AND B.ITEM_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "Y");
				pstmt.setString(2, ls_task_code);
				pstmt.setString(3, ls_item_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
				
					//lc_porder_qty = rs.getInt(1);/**Modified by Pavan Rane 24dec19 [to get qty in double instead of integer as to be update demand/supply in summary table(RunMRP process) related changes]*/
					lc_porder_qty = rs.getDouble(1);
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
				
				sql = "SELECT SUM(QUANTITY) FROM INDENT WHERE TASK_CODE = ? AND STATUS = ? AND ITEM_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_task_code);
				pstmt.setString(2, "A");
				pstmt.setString(3, ls_item_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//lc_indent_qty = rs.getInt(1);/**Modified by Pavan Rane 24dec19 start[to get qty in double instead of integer as to be update demand/supply in summary table(RunMRP process) related changes]*/
					lc_indent_qty = rs.getDouble(1);
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
				
				lc_total_qty= lc_indent_qty + lc_porder_qty + lc_ind_qty;
				
				sql = "SELECT SUM(QUANTITY) FROM PROJ_EST_BSL_ITEM WHERE TASK_CODE = ? AND ITEM_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_task_code);
				pstmt.setString(2, ls_item_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//lc_proj_est_qty = rs.getInt(1);/**Modified by Pavan Rane 24dec19 start[to get qty in double instead of integer as to be update demand/supply in summary table(RunMRP process) related changes]*/
					lc_proj_est_qty = rs.getDouble(1);
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
					ls_errcode = "VTTASK2";
				}
				
				int i = 0;
				while(ls_specific_instr.length() > 0)
				{
					i++;
					
					String[] ls_token = ls_specific_instr.split(":");
					
					
					for(int j = 0; j < ls_token.length; j++)
					{
						System.out.println("ls_token-------->>"+ls_token[j]);
					}
					
					if(i == 1)
					{
						ls_act_code = ls_token[0];
					}
					
					if(i == 2)
					{
						ls_fl_id = ls_token[1];
					}
					
					if(i == 3)
					{
						ls_conf_unit = ls_token[2];
					}
				}
								
				sql = "UPDATE PROJ_EST_BSL_ITEM SET IND_NO = ? WHERE TASK_CODE = ? AND ITEM_CODE = ? AND ACTIVITY_CODE = ? AND FLOOR_ID = ? " +
					"AND CONFIG_UNIT = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				pstmt.setString(2, ls_task_code);
				pstmt.setString(3, ls_item_code);
				pstmt.setString(4, ls_act_code);
				pstmt.setString(5, ls_fl_id);
				pstmt.setString(6, ls_conf_unit);
				pstmt.executeUpdate();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				
				if("S".equalsIgnoreCase(ls_indtype))
				{
					sql = "UPDATE PROJ_ACT_MILESTONE SET IND_NO = ? WHERE TASK_CODE_S = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					pstmt.setString(2, ls_task_code);
					pstmt.executeUpdate();
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				}
				else
				{
					sql = "UPDATE PROJ_ACT_MILESTONE SET IND_NO = ? WHERE TASK_CODE_S = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					pstmt.setString(2, ls_task_code);
					pstmt.executeUpdate();
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
				}
			}

			if(ls_errcode.length() == 0)
			{
				sql = "SELECT IND_TYPE, PROJ_CODE, PURC_RATE, MAX_RATE FROM INDENT WHERE IND_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_ind_type = checkNullAndTrim(rs.getString("IND_TYPE"));
					ls_proj_code = checkNullAndTrim(rs.getString("PROJ_CODE"));
					mrate = rs.getDouble("PURC_RATE");
					mmax_rate = rs.getDouble("MAX_RATE");
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
				
				ls_type_allow_projbudgt_list = checkNullAndTrim(discommon.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn));
				System.out.println("ls_type_allow_projbudgt_list -------------->> "+ls_type_allow_projbudgt_list);
				
				if(ls_type_allow_projbudgt_list.length() == 0 || "NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
				{
					ls_type_allow_projbudgt_list = "";
				}
				
				lb_ord_flag = false;
				if(ls_type_allow_projbudgt_list.length() > 0)
				{
					String [] ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");
					
					for(int i = 0; i <  ls_type_allow_projbudgt.length; i++)
					{
						if(ls_ind_type.equalsIgnoreCase(ls_type_allow_projbudgt[i]))
						{
							lb_ord_flag = true;
						}
					}
				}
			}
			if(lb_ord_flag == true)
			{				
				sql = "SELECT APPROX_COST FROM PROJECT WHERE PROJ_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_proj_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					lc_approxcost = rs.getDouble("APPROX_COST");
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
				
				
				sql = "SELECT SUM((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) * (CASE WHEN MAX_RATE IS NULL THEN 0 ELSE MAX_RATE END)) " +
					"AS  QUANTITY FROM INDENT WHERE PROJ_CODE = ? AND STATUS IN ('A' ,'O') ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_proj_code);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					lc_ind_amount = rs.getDouble("QUANTITY");
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
				
				sql1 = "SELECT IND_NO FROM INDENT WHERE PROJ_CODE = ? AND STATUS IN ('L','C') AND ORD_QTY <> 0 ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, ls_proj_code);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					ls_old_indno = checkNullAndTrim(rs1.getString("IND_NO"));
					
					sql = "SELECT SUM(A.TOT_AMT * B.EXCH_RATE) FROM PORDDET A, PORDER B WHERE ( A.PURC_ORDER = B.PURC_ORDER ) AND B.CONFIRMED = ? " 
							+ "AND A.PROJ_CODE = ? AND A.IND_NO = ? AND B.STATUS <> ? AND A.STATUS <> ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "Y");
					pstmt.setString(2, ls_proj_code);
					pstmt.setString(3, ls_old_indno);
					pstmt.setString(4, "X");
					pstmt.setString(5, "C");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lc_po_amt1 = rs.getDouble(1);
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
					
					sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B, PORDDET C WHERE (A.PURC_ORDER = C.PURC_ORDER) AND " +
						" (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO = ? AND " +
						" B.STATUS <> ? AND C.STATUS = ? AND B.TRAN_SER = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "Y");
					pstmt.setString(2, ls_proj_code);
					pstmt.setString(3, ls_old_indno);
					pstmt.setString(4, "X");
					pstmt.setString(5, "C");
					pstmt.setString(6, "P-RCP");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lc_porcp_amt = rs.getDouble(1);
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
						
					sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE ( A.PURC_ORDER = C.PURC_ORDER ) AND " 
						+ " (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO = ? AND " 
						+ " B.STATUS <> ? AND C.STATUS = ? AND B.TRAN_SER = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "Y");
					pstmt.setString(2, ls_proj_code);
					pstmt.setString(3, ls_old_indno);
					pstmt.setString(4, "X");
					pstmt.setString(5, "C");
					pstmt.setString(6, "P-RET");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lc_pret_amt = rs.getDouble(1);
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
					
					lc_tot_poamt = lc_tot_poamt + lc_po_amt1 + lc_porcp_amt -lc_pret_amt;
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
					
				
				lc_tot_poamt11 = lc_tot_poamt;
				
				sql = "SELECT SUM(A.TOT_AMT * B.EXCH_RATE) FROM PORDDET A, PORDER B WHERE ( A.PURC_ORDER = B.PURC_ORDER ) AND B.CONFIRMED = ? AND " +
					" A.PROJ_CODE = ? AND A.IND_NO IS NULL AND B.STATUS <> ? AND A.STATUS <> ? ";
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
				
				sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B, PORDDET C WHERE ( A.PURC_ORDER = C.PURC_ORDER ) AND " +
					" (A.TRAN_ID = B.TRAN_ID ) AND A.LINE_NO__ORD = C.LINE_NO AND B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO IS NULL AND " +
					" B.STATUS <> ? AND C.STATUS = ? AND B.TRAN_SER = ? ";
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
				
				
				sql = "SELECT SUM(A.NET_AMT * B.EXCH_RATE ) FROM PORCPDET A, PORCP B ,PORDDET C WHERE ( A.PURC_ORDER = C.PURC_ORDER ) AND " +
					" (A.TRAN_ID = B.TRAN_ID ) AND  A.LINE_NO__ORD = C.LINE_NO AND B.CONFIRMED = ? AND C.PROJ_CODE = ? AND C.IND_NO IS NULL AND " +
					" B.STATUS <> ? AND C.STATUS = ? AND B.TRAN_SER = ?  ";
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
				
				lc_tot_poamt = lc_tot_poamt11  + lc_po_amt12 + lc_porcp_amt11 -lc_pret_amt11;
				lc_ind_amount = lc_ind_amount + lc_tot_poamt;

				sql = "SELECT SUM((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) * (CASE WHEN MAX_RATE IS NULL THEN 0 ELSE MAX_RATE END)) " +
					" FROM INDENT WHERE PROJ_CODE = ? AND IND_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, ls_proj_code);
				pstmt.setString(2, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					lc_tot_amt = rs.getDouble(1);
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
				
				lc_tot_amt_proj = lc_ind_amount + lc_tot_amt;
				lc_exceed_amt = lc_tot_amt_proj - lc_approxcost;
				
				if(lc_tot_amt_proj > lc_approxcost)
				{
					ls_errcode = "VTPROJCNF";
					//ls_errcode = 'VTPROJCNF~t'  + '~r~n' +  ' Amount Exceeded Project Code: ' +  ls_proj_code + '~r~n' +  ' Project Approved Amount: ' +  string(lc_approxcost) + '~r~n' +  ' Consumed Amount: '  +  string(lc_ind_amount)  + '~r~n' +  ' Current Indent Amount : ' + string(lc_tot_amt)  + '~r~n' +  ' Exceeded Amount: '  +  string(lc_exceed_amt)
				}
			}
			
			java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
			sql = "UPDATE INDENT SET STATUS = ?, STATUS_DATE = ?, EMP_CODE__APRV = ?, APR_DATE = ? WHERE INDENT.IND_NO = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "A");
			pstmt.setDate(2, currentDate);
			pstmt.setString(3, ecode);
			pstmt.setDate(4, currentDate);
			pstmt.setString(5, tranId);
			cnt = pstmt.executeUpdate();
            
            if(cnt > 0)
            {
            	/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
			    demandSupplyMap.put("site_code", siteCode);
				demandSupplyMap.put("item_code", ls_item_code);		
				demandSupplyMap.put("ref_ser", "IND");
				demandSupplyMap.put("ref_id", tranId);
				demandSupplyMap.put("ref_line", "NA");
				demandSupplyMap.put("due_date", reqDate);		
				demandSupplyMap.put("demand_qty", 0.0);
				demandSupplyMap.put("supply_qty", lc_ind_qty);
				demandSupplyMap.put("change_type", "A");
				demandSupplyMap.put("chg_process", "T");
				demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
			    ls_errcode = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);			    
			    if(ls_errcode != null && ls_errcode.trim().length() > 0)
			    {
			    	System.out.println("errString["+ls_errcode+"]");
	                return "VTDEMSUPER";
			    }
			    /**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
            	String ediOption = "";
                String dataStr = "";
                sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_indent' ";
                pstmt1 = conn.prepareStatement(sql);
                rs1 = pstmt1.executeQuery();
                if ( rs1.next() )
                {
                    ediOption = rs1.getString("EDI_OPTION");
                    if(ediOption == null)
                	{
                		ediOption = "";
                	}
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

                if ( "1".equals(ediOption.trim()) )
                {
                    CreateRCPXML createRCPXML = new CreateRCPXML("w_indent","tran_id");
                    dataStr = createRCPXML.getTranXML( tranId, conn );
                    System.out.println( "dataStr =[ "+ dataStr + "]" );
                    Document ediDataDom = genericUtility.parseString(dataStr);
                    E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
                    String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_indent", "0", xtraParams, conn);
                    createRCPXML = null;
                    e12CreateBatchLoad = null;
                    if( retString != null && "SUCCESS".equals(retString) )
                    {
                        System.out.println("retString from batchload = 	["+retString+"]");
                    }
                }
                
           	}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in approveIndent ============>> "+e);
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
				e.printStackTrace();
				throw new ITMException(e);
			}
		
		}
		System.out.println("return error code =========>> "+ls_errcode);
		return ls_errcode;
	}
}


