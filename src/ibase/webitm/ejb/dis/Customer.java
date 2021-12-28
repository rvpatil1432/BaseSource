/********************************************************
        Title : Customer
        Date  : 20/10/14
        Developer: Vallabh Kadam

 ********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays; // added By Vrushabh on 23-3-20 
import javax.ejb.Stateless;

import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class Customer extends ValidatorEJB

implements CustomerLocal, CustomerRemote
{
	// GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
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
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
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
		String tranId = "";
		String custCode = "", ignoreCredit = "", ignoreDays = "", acctCodeAdv = "", cctrCodeAdv = "", groupCode = "", custCodeBil = "", stanCode = "", terrCode = "";
		String stateCode = "", countCode = "", crTerm = "", currCode = "", currCode1 = "", acctCodeAr = "", cctrCodeAr = "", taxClass = "", taxChap = "", priceList = "";
		String salesPers = "", tranCode = "", siteCodeRcp = "", siteCode = "", channelPartner = "", blackListing = "", contactCode = "", bankCode = "", priceListDisc = "";
		String salesOption = "", dlvTerm = "", lossPerc = "", adhocReplPerc = "", termTableNo = "", priceListClg = "", salesPers1 = "", salesPers2 = "";
		String keyFlag = "", active = "", lsVal3 = "", empCodeOrd = "", empCodeOrd1 = "", custCodePd = "", custCodeDisc = "", sgroupCode = "", custCodeAr = "", currCodeFrt = "";
		String blankVar = "", custName = "", shName = "", availableYn = "", creditLmt = "", round = "", roundTo = "", currCodeIns = "";
		String regCode = "", validUpto = "", regDate = "", priBusinessSite = "";
        // Changed By ANIK 25th-JAN-2021 START
        String mobile_no_tax_reg = "";
        String tele1 = "";
        String tele2 = "";
        String tele3 = "";
        String cont_pers_tele1 = "";
        String cont_pers_alt_tele1 = "";
        String tax_reg_2 = "";

        String mobile_no_secform = "";
        String tele1_secform = "";
        String org_tele1_secform = "";

        String mobile_no_thirdform = "";
        // Changed By ANIK 25th-JAN-2021 END
		String pin="",pin_pattern="";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0, cnt1 = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		String commTable = "";// changed by Nasruddin [19/JUL/16 ]
		String discList = "";   
	    String sGroupCode ="";   // added By Vrushabh on 23-3-20 for declaration of variable start
        String custType = "";  
		
		String mobileNo = "";
		String panNo="";
		
		String custName1 = "";
		
		//StringBuffer custName = new StringBuffer();
		String prosCustCode = "";


		double sequence = 0;
		double sequenceArry [] = null; 
		String custNameArry[] = null;   // added By Vrushabh on 23-3-20 for declaration of variable End
		
		
		double applOrder = 0;
		int noApplicant = 0;
		
		java.util.Date orderDate = null;
		try
		{
			this.finCommon = new FinCommon();
			this.validator = new ValidatorEJB();
			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
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
				tranId = checkNull(this.genericUtility.getColumnValue("tran_id", dom));
				System.out.println("tran id from boqdet --4-->>>>[" + tranId + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("CURRENT COLUMN IN  VALIDATION [" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("cust_code"))
					{
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						sql = "select key_flag from transetup where tran_window = 'w_customer'";
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

						if (keyFlag == null)
						{
							keyFlag = "M";
						}
						System.out.println("Key flag is :-[" + keyFlag + "]");
						if ("M".equalsIgnoreCase(keyFlag) && (custCode == null || custCode.trim().length() <= 0))
						{
							System.out.print("Throw error....");
							errCode = "VMCODNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if ("A".equalsIgnoreCase(editFlag))
						{
							cnt = 0;
							sql = "select count(*) as ll_count from customer where cust_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("ll_count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt > 0)
							{
								errCode = "VMDUPL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Changed by Sneha on 10-11-2016, for validating the field customer priority [Start]
					else if (childNodeName.equalsIgnoreCase("cust_priority"))
					{
						String custPriority = checkNull(genericUtility.getColumnValue("cust_priority", dom));
						/*						System.out.println("custPriority======>>"+custPriority);
						System.out.println("custPriority length======>>"+custPriority.length());*/

						if (custPriority.length() > 1)
						{
							// System.out.println("custPriority found larger value--------->>");
							errCode = "VMCUSTPRIT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (!custPriority.matches("[0-9A-Za-z-]+"))
						{
							// System.out.println("Special character found for custPriority ---------");
							errCode = "VMCUSTINVD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					//Changed by Sneha on 10-11-2016, for validating the field customer priority [End]

					// Comment By Nkhan [16-SEP-16] START
					/*	else if (childNodeName.equalsIgnoreCase("cust_name"))
					{
						custName = this.genericUtility.getColumnValue("cust_name", dom);

						if (custName == null || custName.trim().length() <= 0)
						{
							errCode = "VMCNAMENLL";// Name is blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("sh_name"))
					{
						shName = this.genericUtility.getColumnValue("sh_name", dom);

						if (shName == null || shName.trim().length() <= 0)
						{
							errCode = "VMSHNAMNLL";// Short Name is blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("available_yn"))
					{
						availableYn = this.genericUtility.getColumnValue("available_yn", dom);

						if (availableYn == null || availableYn.trim().length() <= 0)
						{
							errCode = "VMAVLBNULL";// Available Name is blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("round"))
					{
						round = this.genericUtility.getColumnValue("round", dom);

						if (round == null || round.trim().length() <= 0)
						{
							errCode = "VMROUNDNLL";// Round Name is blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("round_to"))
					{
						roundTo = this.genericUtility.getColumnValue("round_to", dom);

						if (roundTo == null || roundTo.trim().length() <= 0)
						{
							errCode = "VMRNDTONLL";// Round To Name is blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("curr_code__frt"))
					{
						currCodeFrt = this.genericUtility.getColumnValue("curr_code__frt", dom);

						if (currCodeFrt == null || currCodeFrt.trim().length() <= 0)
						{
							errCode = "VMCRCDFRNL";// Fright Currency is blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("curr_code__ins"))
					{
						currCodeIns = this.genericUtility.getColumnValue("curr_code__ins", dom);

						if (currCodeIns == null || currCodeIns.trim().length() <= 0)
						{
							errCode = "VMINSCRRNL";// Insurance Currency is
							                       // blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} Comment By Nkhan [16-SEP-16] END */
					else if (childNodeName.equalsIgnoreCase("ignore_credit"))
					{
						ignoreCredit = checkNull(genericUtility.getColumnValue("ignore_credit", dom));

						cnt = 0;
						if (ignoreCredit != null && ignoreCredit.trim().length() > 0)
						{
							cnt = Integer.parseInt(ignoreCredit.trim());
							if (cnt < 0)
							{
								errCode = "VMIGCRT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VMIGCRTNLL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					/* Comment By Nkhan [16-SEP-16] START
					else if (childNodeName.equalsIgnoreCase("credit_lmt"))
					{
						creditLmt = this.genericUtility.getColumnValue("credit_lmt", dom);

						if (creditLmt == null)
						{
							errCode = "VMCRTLMNLL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} Comment By Nkhan [16-SEP-16] END */
					else if (childNodeName.equalsIgnoreCase("ignore_days"))
					{
						ignoreDays = checkNull(genericUtility.getColumnValue("ignore_days", dom));

						cnt = 0;
						if (ignoreDays != null && ignoreDays.trim().length() > 0)
						{
							cnt = Integer.parseInt(ignoreDays.trim());
							if (cnt < 0)
							{
								errCode = "VMCRDYS";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("acct_code__adv"))
					{
						acctCodeAdv = checkNull(genericUtility.getColumnValue("acct_code__adv", dom));

						// if (acctCodeAdv != null)
						// {
						cnt = 0;
						sql = "select count(*) as cnt from accounts where acct_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, acctCodeAdv);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt != 0)
						{
							sql = "select active from accounts where acct_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeAdv);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								active = rs.getString("active");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (!active.equalsIgnoreCase("Y"))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						// Changed By PriyankaC on 09/01/2018..[START]
						/*else
						{
							errCode = "VTACCTCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
						// //Changed By PriyankaC on 09/01/2018..[END]
						// }
						/*	Comment By Nasruddin [16-SEP-16] START
						else
						{
							errCode = "VTACCTNLL";// Account adv null
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						Comment By Nasruddin [16-SEP-16] END*/
					}
					else if (childNodeName.equalsIgnoreCase("cctr_code__adv"))
					{
						cctrCodeAdv = checkNull(genericUtility.getColumnValue("cctr_code__adv", dom));
						cnt = 0;
						if (cctrCodeAdv != null && cctrCodeAdv.trim().length() > 0)
						{
/*						sql = "select count(*) as cnt from costctr where cctr_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCodeAdv);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTCCTRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}*/
							//added by manish mhatre on 3-jan-2020
							errCode = finCommon.isCctrCode(acctCodeAdv, cctrCodeAdv, " ", conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}//end manish
						}
					}
					else if (childNodeName.equalsIgnoreCase("group_code"))
					{
						groupCode = checkNull(genericUtility.getColumnValue("group_code", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

						if (groupCode != null && groupCode.trim().length() > 0)
						{
							if (!groupCode.equalsIgnoreCase(custCode))
							{
								cnt = 0;
								sql = "select count(*) as cnt from customer where cust_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, groupCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select key_flag from transetup where tran_window = 'w_customer'";
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

								if (cnt == 0 && !("A".equalsIgnoreCase(keyFlag)))
								{
									errCode = "VMCUST1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						/* /Comment By Nasruddin  [16-SEP-16] START
						else
						{
							errCode = "VMGRPCNULL";// Group Code is null
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
					}
						//Comment By Nasruddin  [16-SEP-16] END*/
					}
					else if (childNodeName.equalsIgnoreCase("cust_code__bil"))
					{
						custCodeBil = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

						sql = "select key_flag from transetup where tran_window = 'w_customer'";
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

						if ((custCodeBil.trim().length() == 0  || custCodeBil == null) && !("A".equalsIgnoreCase(keyFlag)))
							//if ((custCodeBil != null && custCodeBil.trim().length() > 0) && !("A".equalsIgnoreCase(keyFlag))) // Comment By Nasruddin  [16-SEP-16]
						{
							errCode = "VEBILLTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (!custCodeBil.equalsIgnoreCase(custCode))
						{
							cnt = 0;
							sql = "select count(*) as cnt from customer where cust_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCodeBil);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMBILLTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						/* Comment By Nasruddin [16-SEP-16] START
						else if (custCodeBil == null || custCodeBil.trim().length() <= 0)
						{
							errCode = "VMBLLTONLL";// Bill To is null
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						Comment By Nasruddin [16-SEP-16] END*/
					}
					else if (childNodeName.equalsIgnoreCase("stan_code"))
					{
						stanCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("stan_code", dom));// added by kailas Gaikwad on 28 june 2019
						cnt = 0;

						if (stanCode != null)
						{
							sql = "select count(*) as cnt from station where stan_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)  
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						/* Comment By Nasruddin [16-SEP-16] START*/
						/*else   
						{
							//errCode = "VMSTNCDNLL";
							errCode = "VTAMDSTCOD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
						/*Comment By Nasruddin [16-SEP-16] END*/
						
						else    // added by kailas Gaikwad on 28 june 2019
						{
							
							errCode = "VMSTANCOD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
						

					}
					else if (childNodeName.equalsIgnoreCase("terr_code"))
					{
						terrCode = checkNull(genericUtility.getColumnValue("terr_code", dom));

						if (terrCode != null && terrCode.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as ll_count from territory where terr_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, terrCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("ll_count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTERRCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("state_code"))
					{
						stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));

						if (stateCode != null && stateCode.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from state where state_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stateCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTSTATE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("count_code"))
					{
						countCode = checkNull(genericUtility.getColumnValue("count_code", dom));

						if (countCode != null && countCode.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from country where count_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTCONTCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("cr_term"))
					{
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));

						if (crTerm != null && crTerm.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from crterm where cr_term =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, crTerm);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTCRTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						cnt = 0;

						sql = "select count(*) as cnt from currency where curr_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, currCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 0)
						{
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							if (editFlag.equalsIgnoreCase("E"))
							{
								custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

								sql = "select curr_code from customer where cust_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									currCode1 = rs.getString("curr_code");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!currCode.equalsIgnoreCase(currCode1) && currCode1 != null)
								{
									lsVal3 = "C";
									cnt = 0;

									sql = "select count(distinct curr_code__ac) as ll_count from sundrybal" + " where sundry_code =?" + " and sundry_type =?" + " and (dr_amt != 0 or cr_amt != 0)";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, lsVal3);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt = rs.getInt("ll_count");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									cnt1 = 0;
									sql = "select count(*) as cnt from sundrybal" + " where sundry_code = ?" + " and sundry_type = ?" + " and (dr_amt != 0 or cr_amt != 0)" + " and curr_code__ac = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, lsVal3);
									pstmt.setString(3, currCode1);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt1 = rs.getInt("cnt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if (cnt1 != 0 && cnt > 1)
									{
										errCode = "VXCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("acct_code__ar"))
					{
						acctCodeAr = checkNull(genericUtility.getColumnValue("acct_code__ar", dom));

						if (acctCodeAr != null && acctCodeAr.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from accounts where acct_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeAr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt != 0)
							{
								sql = "select active from accounts where acct_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, acctCodeAr);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									active = rs.getString("active");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (!"Y".equalsIgnoreCase(active))
								{
									errCode = "VMACCTA";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						// Changed By PriyankaC on 09/01/2018
						/*else
						{
							errCode = "VTACCTCD1";// A/C Rec is null
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
						// //Changed By PriyankaC on 09/01/2018..[END]
						// cHANGED By Nasruddin [30-SEP-16] END

					}
					else if (childNodeName.equalsIgnoreCase("cctr_code__ar"))
					{
						cctrCodeAr = checkNull(genericUtility.getColumnValue("cctr_code__ar", dom));

						if (cctrCodeAr != null && cctrCodeAr.trim().length() > 0)
						{
							/*cnt = 0;
							sql = "select count(*) as cnt from costctr where cctr_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCodeAr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTCCTRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}*/
							//added by manish mhatre on 3-jan-2020
							errCode = finCommon.isCctrCode(acctCodeAr, cctrCodeAr, " ", conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}//end manish
						}
						// //Changed By PriyankaC on 09/01/2018..[START]
						/*else
						{
							acctCodeAr = checkNull(genericUtility.getColumnValue("acct_code__ar", dom));
							if (acctCodeAr != null && acctCodeAr.trim().length() > 0)
							{
								valueXmlString.append("<cctr_code__ar ><![CDATA[" + blankVar + "]]></cctr_code__ar>");
							}
							errCode = "VTCCTRCD1";// Cost Center null//cHANGED bY nASRUDDIN 20-SEP-16
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//	//Changed By PriyankaC on 09/01/2018..[END]
						 */ }
					else if (childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));

						if (taxClass != null && taxClass.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from taxclass where tax_class =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));

						if (taxChap != null && taxChap.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from taxchap where tax_chap =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("price_list"))
					{
						priceList = checkNull(genericUtility.getColumnValue("price_list", dom));

						if (priceList != null && priceList.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from pricelist_mst where price_list =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTPLIST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("price_list__clg"))
					{
						priceListClg = checkNull(genericUtility.getColumnValue("price_list__clg", dom));

						if (priceListClg != null && priceListClg.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from pricelist_mst where price_list =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceListClg);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTPLIST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					// Changed by Mahesh Saggam on 29-05-2019 [Start]
					
					/*else if (childNodeName.equalsIgnoreCase("sales_pers"))
					{
						salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));

						if (salesPers != null && salesPers.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from sales_pers where sales_pers = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, salesPers);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						 Comment By Nasruddin [16-SEP-16] START
						else
						{
							errCode = "VMSLPERNLL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						Comment By Nasruddin [16-SEP-16] END
					}*/
					// Changed by Mahesh Saggam on 29-05-2019 [End]
					
					else if (childNodeName.equalsIgnoreCase("sales_pers__1"))
					{
						salesPers1 = checkNull(genericUtility.getColumnValue("sales_pers__1", dom));
						if (salesPers1 != null && salesPers1.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from sales_pers where sales_pers = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, salesPers1);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("sales_pers__2"))
					{
						salesPers2 = checkNull(genericUtility.getColumnValue("sales_pers__2", dom));
						if (salesPers2 != null && salesPers2.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from sales_pers where sales_pers = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, salesPers2);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMSLPERS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));

						if (tranCode != null && tranCode.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from transporter where tran_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("site_code__rcp"))
					{
						siteCodeRcp = checkNull(genericUtility.getColumnValue("site_code__rcp", dom));

						if (siteCodeRcp != null && siteCodeRcp.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from site where site_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeRcp);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTSITECD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("site_code"))
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						channelPartner = checkNull(genericUtility.getColumnValue("channel_partner", dom));

						if ("Y".equalsIgnoreCase(channelPartner) && (siteCode == null || siteCode.trim().length() <= 0))
							//if ("Y".equalsIgnoreCase(channelPartner) && (siteCode == null && siteCode.trim().length() <= 0)) Comment By Nasruddin [16-SEP-16]
						{
							errCode = "VMSITECD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							//if ("N".equalsIgnoreCase(channelPartner) && (siteCode != null)) Comment By Nasruddin
							if ("N".equalsIgnoreCase(channelPartner) && (siteCode.trim().length() > 0))
							{
								errCode = "VNRSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if ("Y".equalsIgnoreCase(channelPartner) && (siteCode != null && siteCode.trim().length() > 0))
							{
								cnt = 0;
								sql = "select count(*) as cnt from site where site_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt == 0)
								{
									errCode = "VTSITECD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("black_listing"))
					{
						blackListing = checkNull(genericUtility.getColumnValue("black_listing", dom));

						if (!"P".equalsIgnoreCase(blackListing))
						{
							errCode = "VMBLACKLIS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("contact_code"))
					{
						contactCode = checkNull(genericUtility.getColumnValue("contact_code", dom));

						cnt = 0;
						sql = "select count(*) as cnt from contact where contact_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, contactCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (cnt == 0)
						{
							errCode = "VMCONTCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("bank_code"))
					{
						bankCode = checkNull(genericUtility.getColumnValue("bank_code", dom));

						// if (bankCode != null) Comment By Nasruddin [16-SEP-16]
						if (bankCode != null && bankCode.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from bank where bank_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, bankCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt <= 0)
							{
								errCode = "VMBANK1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("price_list__disc"))
					{
						priceListDisc = checkNull(genericUtility.getColumnValue("price_list__disc", dom));

						if (priceListDisc != null && priceListDisc.trim().length() > 0)
						{
							// cnt1=Integer.parseInt(priceListDisc.trim());
							// }
							// if(cnt1>0){
							cnt = 0;
							sql = "select count(*) as cnt from pricelist where price_list =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceListDisc);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTPLIST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("sales_option"))
					{
						salesOption = checkNull(genericUtility.getColumnValue("sales_option", dom));

						if (salesOption == null || salesOption.trim().length() <= 0)
						{
							errCode = "VTSLOPT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("dlv_term"))
					{
						dlvTerm = checkNull(genericUtility.getColumnValue("dlv_term", dom));
						System.out.println("@@@@@@@@@@@@@@@@@ added check null fro dlvterm@@@@@@@@@" + dlvTerm);
						if (dlvTerm != null && dlvTerm.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from delivery_term where dlv_term =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, dlvTerm);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMDLVTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("loss_perc"))
					{
						lossPerc = genericUtility.getColumnValue("loss_perc", dom);
						if (lossPerc != null && lossPerc.trim().length() > 0)
						{
							cnt1 = Integer.parseInt(lossPerc);
						}
						if (cnt1 < 0)
						{
							errCode = "VTLOSSPERC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("adhoc_repl_perc"))
					{
						adhocReplPerc = checkNull(genericUtility.getColumnValue("adhoc_repl_perc", dom));
						double adhocvalue = 0.00;

						if (adhocReplPerc != null && adhocReplPerc.trim().length() > 0)
						{

							adhocvalue = Double.valueOf(adhocReplPerc.trim());
							// cnt1=Integer.parseInt(adhocReplPerc.trim());
							// if(cnt1<0 || cnt1>100){
							if (adhocvalue < 0 || adhocvalue > 100)
							{
								errCode = "VTADH";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("term_table__no"))
					{
						termTableNo = checkNull(genericUtility.getColumnValue("term_table__no", dom));

						// if( termTableNo != null) Changed By Nasruddin [16-sep-16]
						if (termTableNo != null && termTableNo.trim().length() > 0)
						{
							sql = "select count(1) as cnt from sale_term_table where term_table  = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, termTableNo);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMSALETERM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("emp_code__ord"))
					{
						empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord", dom));

						if (empCodeOrd != null && empCodeOrd.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from employee where emp_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMEMPORD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					} else if (childNodeName.equalsIgnoreCase("emp_code__ord1"))
					{

						empCodeOrd1 = checkNull(genericUtility.getColumnValue("emp_code__ord1", dom));
						if (empCodeOrd1 != null && empCodeOrd1.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from employee where emp_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCodeOrd1);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMEMPORD2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} // Changed By Nasruddin khan [19/JUL/16] START
					else if( childNodeName.equalsIgnoreCase("comm_table")){

						commTable = checkNull(genericUtility.getColumnValue("comm_table", dom));

						if( commTable != null && commTable.trim().length() > 0){
							cnt = 0;

							sql = "select count(1) as cnt from  comm_hdr where comm_table = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, commTable);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VMCOMMTBCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					} // Changed By Nasruddin khan [19/JUL/16] END

					// Changed By Nasruddin khan [16/SEP/16] START
					else if (childNodeName.equalsIgnoreCase("disc_list"))
					{

						discList = checkNull(genericUtility.getColumnValue("disc_list", dom));

						if (discList != null && discList.trim().length() > 0)
						{
							cnt = 0;

							sql = "select count(1) as cnt from  disc_list where disc_list = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, discList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTDISCLT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					// Added by Pankaj R on 12-Apr-19 [START]
					else if (childNodeName.equalsIgnoreCase("site_code__pbus"))
					{
						priBusinessSite = checkNull(genericUtility.getColumnValue("site_code__pbus", dom));

						if (priBusinessSite != null && priBusinessSite.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from site where site_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priBusinessSite);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTPBSITECD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
                    
                    // Changed By ANIK 25th-JAN-2021 START
                    else if(childNodeName.equalsIgnoreCase("mobile_no_tax_reg"))
					{
                        mobile_no_tax_reg = checkNull(genericUtility.getColumnValue("mobile_no_tax_reg", dom));
                        // System.out.println("mobile_no_tax_reg "+mobile_no_tax_reg);
                        // String mobile_no_tax_reg_result = ""+mobile_no_tax_reg;
						if (mobile_no_tax_reg.length() > 0 && mobile_no_tax_reg != null && !((mobile_no_tax_reg.trim()).matches("^([0-9]{6,12})$")) )
						{
								errCode = "VMMOBNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("tele1"))
					{
                        tele1 = checkNull(genericUtility.getColumnValue("tele1", dom));
                        // System.out.println("tele 1 "+tele1);
                        // String tele1_result = ""+tele1;
						if (tele1.length() > 0 && tele1 != null && !((tele1.trim()).matches("^([0-9]{6,12})$")) )
						{
								errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("tele2"))
					{
                        tele2 = checkNull(genericUtility.getColumnValue("tele2", dom));
                        // System.out.println("tele 2 "+tele2);
                        // String tele2_result = ""+tele2;
						if (tele2.length() > 0 && tele2 != null && !((tele2.trim()).matches("^([0-9]{6,12})$")) )
						{
								errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("tele3"))
					{
                        tele3 = checkNull(genericUtility.getColumnValue("tele3", dom));
                        // System.out.println("tele 3 "+tele3);
                        // String tele3_result = ""+tele3;
						if (tele3.length() > 0 && tele3 != null && !((tele3.trim()).matches("^([0-9]{6,12})$")) )
						{
								errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("cont_pers_tele1"))
					{
                        cont_pers_tele1 = checkNull(genericUtility.getColumnValue("cont_pers_tele1", dom));
                        // System.out.println("cont_pers_tele 1 "+cont_pers_tele1);
                        // String cont_pers_tele1_result = ""+cont_pers_tele1;
						if (cont_pers_tele1.length() > 0 && cont_pers_tele1 != null && !((cont_pers_tele1.trim()).matches("^([0-9]{6,12})$")) )
						{
								errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							
						}
                    }
                    
                    else if(childNodeName.equalsIgnoreCase("cont_pers_alt_tele1"))
					{
                        cont_pers_alt_tele1 = checkNull(genericUtility.getColumnValue("cont_pers_alt_tele1", dom));
                        // System.out.println("cont_pers_alt_tele 1 "+cont_pers_alt_tele1);
                        // String cont_pers_alt_tele1_result = ""+cont_pers_alt_tele1;
						if (cont_pers_alt_tele1.length() > 0 && cont_pers_alt_tele1 != null && !((cont_pers_alt_tele1.trim()).matches("^([0-9]{6,12})$")) )
						{
								errCode = "VMTELNOIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							
						}
					}

                    // Changed By ANIK - ADD TAX_REG_2 Validation Added on 4th Feb
                    else if(childNodeName.equalsIgnoreCase("tax_reg_2")){
                        tax_reg_2 = checkNull(genericUtility.getColumnValue("tax_reg_2",dom));
                        System.out.println("TAX REGISTER NUMBER 2 : "+tax_reg_2);
                        if(tax_reg_2.length() > 0 && (!tax_reg_2.equalsIgnoreCase("UNREGISTER")) && !((tax_reg_2.trim()).matches("([0-9]{2}[0-9A-Z]{13})")) ){
                                errCode = "VMTXREGIVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
                        }

                    }
    
                    // Changed By ANIK 25th-JAN-2021 END

					//Added by Shubham.S.B on 01-02-2021
					// pinPattern Validation
					else if (childNodeName.equalsIgnoreCase("pin"))
					{
						pin = checkNull(genericUtility.getColumnValue("pin", dom));
						countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
						
						sql = "select pin_pattern from country where count_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, countCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							pin_pattern = checkNull(rs.getString("pin_pattern"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("pin_pattern is::::::::"+pin_pattern);
						if (pin != null && pin.trim().length() > 0)
						{
							if (!pin.trim().matches(pin_pattern)) 
							{
								errCode = "VTINVLPIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
					}
					
					// pinPattern Validation ended
					// Added by Pankaj R on 12-Apr-19 [END] 
					// Changed By Nasruddin khan [16/SEP/16] END

					// else if(childNodeName.equalsIgnoreCase("cust_code__pd")){
					// custCodePd =
					// checkNull(this.genericUtility.getColumnValue(
					// "cust_code__pd", dom));
					//
					// if(custCodePd!=null){
					// cnt=0;
					// sql =
					// "select count(*) as cnt from customer where cust_code__pd=?";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, custCodePd);
					// rs = pstmt.executeQuery();
					// if (rs.next()) {
					// cnt = rs.getInt("cnt");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// if(cnt==0){
					// errCode = "";
					// errList.add(errCode);
					// errFields.add(childNodeName.toLowerCase());
					// }
					// }
					// }
					// else
					// if(childNodeName.equalsIgnoreCase("cust_code__disc")){
					// custCodeDisc =
					// checkNull(this.genericUtility.getColumnValue(
					// "cust_code__disc", dom));
					//
					// if(custCodeDisc!=null){
					// cnt=0;
					// sql =
					// "select count(*) as cnt from customer where cust_code__disc=?";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, custCodeDisc);
					// rs = pstmt.executeQuery();
					// if (rs.next()) {
					// cnt = rs.getInt("cnt");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// if(cnt==0){
					// errCode = "";
					// errList.add(errCode);
					// errFields.add(childNodeName.toLowerCase());
					// }
					// }
					// }
					// else if(childNodeName.equalsIgnoreCase("sgroup_code")){
					// sgroupCode =
					// checkNull(this.genericUtility.getColumnValue(
					// "sgroup_code", dom));
					//
					// if(sgroupCode!=null){
					// cnt=0;
					// sql =
					// "select count(*) as cnt from acctsgrp where sgroup_code=?";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, sgroupCode);
					// rs = pstmt.executeQuery();
					// if (rs.next()) {
					// cnt = rs.getInt("cnt");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// if(cnt==0){
					// errCode = "";
					// errList.add(errCode);
					// errFields.add(childNodeName.toLowerCase());
					// }
					// }
					// }
					// else if(childNodeName.equalsIgnoreCase("cust_code__ar")){
					// custCodeAr =
					// checkNull(this.genericUtility.getColumnValue(
					// "cust_code__ar", dom));
					//
					// if(custCodeAr!=null){
					// cnt=0;
					// sql =
					// "select count(*) as cnt from customer where cust_code__ar=?";
					// pstmt = conn.prepareStatement(sql);
					// pstmt.setString(1, custCodeAr);
					// rs = pstmt.executeQuery();
					// if (rs.next()) {
					// cnt = rs.getInt("cnt");
					// }
					// rs.close();
					// rs = null;
					// pstmt.close();
					// pstmt = null;
					//
					// if(cnt==0){
					// errCode = "";
					// errList.add(errCode);
					// errFields.add(childNodeName.toLowerCase());
					// }
					// }
					// }
				}

				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.trim().equalsIgnoreCase("reg_code"))
					{
						regCode = checkNull(genericUtility.getColumnValue("reg_code", dom));

						if (regCode != null && regCode.trim().length() > 0)
						{
							cnt = 0;
							sql = "select count(*) as cnt from reg_requirements where reg_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, regCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTRCODEXT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VTRCODNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("reg_date"))
					{
						regDate = genericUtility.getColumnValue("reg_date", dom);
						if (regDate == null)
						{
							errCode = "VTREGNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("valid_upto"))
					{
						validUpto = genericUtility.getColumnValue("valid_upto", dom);
						regDate = genericUtility.getColumnValue("reg_date", dom);
						if (validUpto == null)
						{
							errCode = "VTVALNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (regDate != null)
						{
							Timestamp validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(validUpto, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							Timestamp regDateNew = Timestamp.valueOf(genericUtility.getValidDateString(regDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

							if (validUptoDate.compareTo(regDateNew) <= 0)
							{
								errCode = "VTVALREGDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				valueXmlString.append("</Detail2>");
				
				break;  // added By Vrushabh on 23-3-20
			case 3:         // added By Vrushabh on 23-3-20 for detail of co-applicant start
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				//valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("appl_order"))
					{
						sequence = Double.parseDouble((checkNull(genericUtility.getColumnValue("appl_order", dom)).trim().length() == 0 ?"0": genericUtility.getColumnValue("appl_order", dom).trim()));
						System.out.println("sequence="+sequence);
						boolean isDuplicate = false;
						boolean isDuplicust = false;
						double maxSequence = 0;
						double maxcust = 0;
						if(sequence < 1)
						{
							errCode = "VMAPORNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							int length = dom2.getElementsByTagName("Detail2").getLength();
							System.out.println("length="+length);
							sequenceArry = new double [length];
							custNameArry = new String [length];
							for(int i =0; i< dom2.getElementsByTagName("Detail2").getLength();i++)
							{
								//prvSequence = Integer.parseInt( checkNull(genericUtility.getColumnValueFromNode("appl_order",dom2.getElementsByTagName("Detail2").item(0))).trim().length() == 0 ?"0":genericUtility.getColumnValueFromNode("appl_order",dom2.getElementsByTagName("Detail2").item(0)).trim()) ;

								sequence = Double.parseDouble( checkNull(genericUtility.getColumnValueFromNode("appl_order",dom2.getElementsByTagName("Detail2").item(i))).trim().length() == 0 ?"0":genericUtility.getColumnValueFromNode("appl_order",dom2.getElementsByTagName("Detail2").item(i)).trim()) ;
								custName1 = checkNull(genericUtility.getColumnValueFromNode("cust_name",dom2.getElementsByTagName("Detail2").item(i)));

								custNameArry [i] = custName1;
								sequenceArry [i] = sequence;
								//custName.append("'+custName1+'"+",");

								System.out.println("prvSequence = "+i+"   Sequence"+sequence);
								System.out.println("prvcustName1 = "+i+"   custName1"+custName1);
								//if("P".equalsIgnoreCase(milestoneType))
								//{
								/*										if(sequence <= maxSequence)
										{
											errCode = "VMMSSEQ3";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}*/
								//}

								/*if(custName > maxcust)
								{
									maxSequence = sequence ; 
								}*/

								if(sequence > maxSequence)
								{
									maxSequence = sequence ; 
								}

							}

							Arrays.sort(sequenceArry);
							for(int i = 1; i < sequenceArry.length; i++) 
							{
								if(sequenceArry[i] == sequenceArry[i - 1]) 
								{
									System.out.println("Duplicate: " + sequenceArry[i]);
									isDuplicate = true;
								}
							}
							if(isDuplicate)
							{
								errCode = "VMAPORDUL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//$newarray = implode(", ", $myarray);

							Arrays.sort(custNameArry);
							for(int i = 1; i < custNameArry.length; i++) 
							{
								if(custNameArry[i].equalsIgnoreCase(custNameArry[i - 1])) 
								{
									System.out.println("Duplicate CUST: " + custNameArry[i]);
									isDuplicust = true;
								}
							}
							if(isDuplicust)
							{
								errCode = "VMACUSTDUL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							/*sql = "select count(*) from customer_det  where cust_code IN ["+custName1+"]";
							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);									
							}
							if(cnt > 0)
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;


						}*/

						}
						/*if(childNodeName.trim().equalsIgnoreCase("appl_order"))
					{
						sequence = Double.parseDouble((checkNull(genericUtility.getColumnValue("appl_order", dom)).trim().length() == 0 ?"0": genericUtility.getColumnValue("appl_order", dom).trim()));
						System.out.println("sequence="+sequence);
						boolean isDuplicate = false;
						double maxSequence = 0;
						if(sequence < 1)
						{
							errCode = "VMAPORDUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							for(int i =0; i< dom2.getElementsByTagName("Detail2").getLength();i++)
							{
								int length = dom2.getElementsByTagName("Detail2").getLength();
								System.out.println("length="+length);
								sequenceArry = new double [length];
								sequence = Double.parseDouble( checkNull(genericUtility.getColumnValueFromNode("appl_order",dom2.getElementsByTagName("Detail2").item(i))).trim().length() == 0 ?"0":genericUtility.getColumnValueFromNode("appl_order",dom2.getElementsByTagName("Detail2").item(i)).trim()) ;
								Arrays.sort(sequenceArry);
								sequenceArry [i] = sequence;

					System.out.println("ARRAYYYYYYYYYYYYYY"+sequenceArry[i]);

							Arrays.sort(sequenceArry);
							for(int i = 1; i < sequenceArry.length; i++) 
							{
								if(sequenceArry[i] == sequenceArry[i - 1]) 
								{
									System.out.println("Duplicate: " + sequenceArry[i]);
									isDuplicate = true;
								}
							}
							if(isDuplicate)
							{
								errCode = "VMMSSEQ2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}*/
					}
					else if(childNodeName.trim().equalsIgnoreCase("cust_type"))
					{
						System.out.println("PRINTTTTTTTTTTTTTTTTTTTTTTTTTTT");
						custType=checkNull(this.genericUtility.getColumnValue("cust_type", dom));

						if(custType == null || custType.trim().length() == 0)
						{		

							errCode = "VTTRANTYP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
					if(childNodeName.trim().equalsIgnoreCase("cust_name"))
					{
						System.out.println("PRINTTTTTTTTTTTTTTTTTTTTTTTTTTT");
						custName1=checkNull(this.genericUtility.getColumnValue("cust_name", dom));

						if(custName1 == null || custName1.trim().length() == 0)
						{		

							errCode = "VTCUSTNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						/*else
						{

							noApplicant = Integer.parseInt(this.genericUtility.getColumnValue("no_of_appl", dom1));
							System.out.println("no_of_appl"+noApplicant);


							sql = "select cust_code from customer where no_of_appl = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setInt(1, noApplicant);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								custCode = rs.getString(1);

								sql = "select cust_name from customer_det where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									Custcode.add(rs.getString(1));
								}

								if(custNameArry.equals(Custcode))
								{
									errCode = "VTSTAN1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}

							}						

						}*/				

					}

					else if (childNodeName.equalsIgnoreCase("stan_code"))
					{
						System.out.println("PRINTTTTTTTTTTTTTTTTTTTTTTTTTTT");
						stanCode=this.genericUtility.getColumnValue("stan_code", dom);
						if (stanCode != null && stanCode.trim().length() > 0 )
						{
							sql = "select count(*) from station  where stan_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
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
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("mobile_no"))
					{

						mobileNo=this.genericUtility.getColumnValue("mobile_no", dom);
						System.out.println("MOB NO IS "+mobileNo);
						if (mobileNo != null && mobileNo.trim().length() < 10 )
						{


							errCode = "VMMOBINVD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("org_stan_code"))
					{
						//System.out.println("PRINTTTTTTTTTTTTTTTTTTTTTTTTTTT");
						stanCode=this.genericUtility.getColumnValue("org_stan_code", dom);
						if (stanCode != null && stanCode.trim().length() > 0 )
						{
							sql = "select count(*) from station  where stan_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
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
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}else
						{
							errCode = "VMSTAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("pan_no")) //Added by Sagar on 05/MAR/14
					{
					    panNo =checkNull(this.genericUtility.getColumnValue("pan_no", dom));
						System.out.println(">>>>panNo IS "+panNo);
						if (panNo == null || panNo.trim().length()==0)
						{
							errCode ="VTCUSTPNCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

				}
				break;
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
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
		catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				// Commented and Added by sarita on 13NOV2017
				/*if (conn != null)
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
				conn = null;*/
				if (conn != null)
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
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}   //  added By Vrushabh on 23-3-20 for detail of co-applicant End

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
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
			System.out.println("valueXmlString[" + valueXmlString + "]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA[" + valueXmlString + "]");
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "", sql1 = "", sql2 = "",sql5 = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		System.out.println("currentColumn [" + currentColumn + "]");
		String colName = "", colValue = "", fullName = "", siteCode1 = "";
		String custType = "", priceList = "", countCode1 = "", priceList1 = "", siteCodePay = "", partQty = "",
				siteCode = "", siteCodeRcp = "", priceListDisc = "";
		String resBKList = "", lsDescr = "", lsBKListed = "";
		String lsNull = "", nullVarr = "", ldDate = "";
		String custCodeBill = "", custCode = "", stanCode = "", currCode = "", countCode = "", currCode2 = "";
		String cust_code__dlv="";
		String lsStateCode = "", lsCity = "", lsPin = "";
		String custName = "", chqName = "", terrCode = "", terrDesc = "", salesPers = "", spName = "", contactCode = "",
				groupCode = "",custCodeDlv = "";
		String bankCode = "", bankName = "";
		String channelPartner = "";
		String crTerm = "", crDays = "";
		String rateRound = "";
		String name = "", shName = "", contPers = "", contPfx = "", addr1 = "", addr2 = "", addr3 = "", stateCode = "",
				tele1 = "", tele2 = "", tele3 = "", teleExt = "", fax = "", emailAddr = "", ediAddr = "";
		String contPfx1 = "", contPers1 = "", Add1 = "", emailAddr1 = "", ediAddr1 = "", contactType="", keyFlag = "";
		String regCode = "", regDescr = "", empCodeOrd = "", empFname = "", empLname = "", empCodeOrd1 = "",
				deptCode = "";
		int ctr = 0;
		int currentFormNo = 0;
		java.util.Date reqDate = null;
		int childNodeListLength = 0;
		java.util.Date statusDate = null;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		// GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();

		
		
		
        
		
		//String contactTime="";
			
		
		String pin = "",city="";  // added By Vrushabh on 23-3-20 for declaration of variable start
		String prosCustCode = "";
		String stancode = "";
		String organisationName="";       	
		String organisationStatus="";      	
		String orgDesignation="";        	                      
		String orgAddr1="";        	
		String orgAddr2="";        	
		String orgAddr3="";       	
		String orgCity="";       	
		String orgStateCode="";        	
		String orgstanCode="";
		String orgpin="";       	
		String orgCountCode="";  // added By Vrushabh on 23-3-20 for declaration of variable End		
		String descr ="";
		String add2 = "";
	
		String blackListed ="";
	
		String reasCodeBklist = "";
		String loginSite = "";
		String custCodeBil = "";
		
		String terrdescr = "";
		
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			// Changes and Commented By Bhushan on 09-06-2016 :START
			// conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			// Changes and Commented By Bhushan on 09-06-2016 :END
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
				} while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN [" + currentColumn + "]");
				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					resBKList = checkNull(genericUtility.getColumnValue("reas_code__bklist", dom));
					sql = "select descr from   gencodes where  fld_name = 'REAS_CODE__BKLIST' " + "and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, resBKList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<bklist_reason ><![CDATA[" + lsDescr + "]]></bklist_reason>");
					lsBKListed = checkNull(genericUtility.getColumnValue("black_listed", dom));
					if (lsBKListed.equalsIgnoreCase("N"))
					{
						valueXmlString.append("<reas_code__bklist protect = \"1\"><![CDATA[" + lsNull + "]]></reas_code__bklist>");
						valueXmlString.append("<bklist_reason ><![CDATA[" + lsNull + "]]></bklist_reason>");
					}
					else
					{
						valueXmlString.append("<reas_code__bklist><![CDATA[" + lsNull + "]]></reas_code__bklist>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					resBKList = checkNull(genericUtility.getColumnValue("reas_code__bklist", dom));
					sql = "select descr from   gencodes where  fld_name = 'REAS_CODE__BKLIST' " + "and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, resBKList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<bklist_reason ><![CDATA[" + lsDescr + "]]></bklist_reason>");
					lsBKListed = checkNull(genericUtility.getColumnValue("black_listed", dom));
					if ("N".equalsIgnoreCase(lsBKListed))
					{
						valueXmlString.append("<reas_code__bklist protect = \"1\"><![CDATA[" + lsNull + "]]></reas_code__bklist>");
						valueXmlString.append("<bklist_reason ><![CDATA[" + lsNull + "]]></bklist_reason>");
					}
					else
					{
						valueXmlString.append("<reas_code__bklist><![CDATA[" + lsNull + "]]></reas_code__bklist>");
					}
					//Change by Pooja S.Start
					String sitecode1 = checkNull(genericUtility.getColumnValue("site_code", dom));
					if(sitecode1==null || "".equals(sitecode1)||sitecode1.trim().length() == 0)
					{
						sitecode1 = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
						System.out.print("value of sitecode " + sitecode1);
						valueXmlString.append("<site_code><![CDATA[" + sitecode1 + "]]></site_code>");
					}
				}
				//Change by Pooja S.End
				else if (currentColumn.trim().equalsIgnoreCase("cust_code"))
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
					
					// if(custCodeBill==null &&
					// custCodeBill.trim().length()<=0)
					//changed by chaitali on 12-06-2019
					
					//valueXmlString.append("<cust_code__dlv><![CDATA[" + custCode + "]]></cust_code__dlv>"); //Commented by Anagha R on 17/08/2020 for Copy Transaction not setting the values of original customer.
					
					//changed by chaitali on 12-06-2019
					
					//valueXmlString.append("<cust_code__bil><![CDATA[" + custCode + "]]></cust_code__bil>"); //Commented by Anagha R on 17/08/2020 for Copy Transaction not setting the values of original customer.
					//valueXmlString.append("<group_code><![CDATA[" + custCode + "]]></group_code>"); //Commented by Anagha R on 17/08/2020 for Copy Transaction not setting the values of original customer.
					// }
                    //Added by Anagha R on 17/08/2020 for Copy Transaction not setting the values of original customer. START
                    groupCode = checkNull(genericUtility.getColumnValue("group_code", dom));
                    if(groupCode == null || groupCode.trim().length()<=0){
                        valueXmlString.append("<group_code><![CDATA[" + custCode + "]]></group_code>");
                    }else{
                        valueXmlString.append("<group_code><![CDATA[" + groupCode + "]]></group_code>");
                    }

                    custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
                    if(custCodeBill == null || custCodeBill.trim().length()<=0){
                        valueXmlString.append("<cust_code__bil><![CDATA[" + custCode + "]]></cust_code__bil>");
                    }else{
                        valueXmlString.append("<cust_code__bil><![CDATA[" + custCodeBill + "]]></cust_code__bil>");
                    }

                    custCodeDlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));
                    if(custCodeDlv == null || custCodeDlv.trim().length()<=0){
                        valueXmlString.append("<cust_code__dlv><![CDATA[" + custCode + "]]></cust_code__dlv>");
                    }else{
                        valueXmlString.append("<cust_code__dlv><![CDATA[" + custCodeDlv + "]]></cust_code__dlv>");
                    }

                    System.out.println("groupCode: "+groupCode+" cust_code_bill: "+custCodeBill+" cust_code_dlv: "+custCodeDlv);
                    //Added by Anagha R on 17/08/2020 for Copy Transaction not setting the values of original customer. END

				}
				else if (currentColumn.trim().equalsIgnoreCase("stan_code"))
				{
					stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
					sql = "select state_code, city, pin from station where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsStateCode = checkNull(rs.getString("state_code"));
						lsCity = checkNull(rs.getString("city"));
						lsPin = checkNull(rs.getString("pin"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (lsStateCode != null && lsStateCode.trim().length() > 0)
					{
						sql1 = "select count_code from state where state_code =?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, lsStateCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							countCode = checkNull(rs1.getString("count_code"));
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					if (countCode != null && countCode.trim().length() > 0)
					{
						sql2 = "select curr_code from country where count_code =?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, countCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							currCode = checkNull(rs2.getString("curr_code"));
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
					}
					valueXmlString.append("<state_code><![CDATA[" + lsStateCode + "]]></state_code>");
					valueXmlString.append("<count_code><![CDATA[" + countCode + "]]></count_code>");
					valueXmlString.append("<curr_code><![CDATA[" + currCode + "]]></curr_code>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("cust_name"))
				{
					custName = checkNull(genericUtility.getColumnValue("cust_name", dom));
					chqName = checkNull(genericUtility.getColumnValue("chq_name", dom));
					//changed by Pooja.S on[2-Nov-2018] to set the address and fields by using Json START
					/*if (chqName == null || chqName.trim().length() <= 0)
					{
						valueXmlString.append("<chq_name><![CDATA[" + custName + "]]></chq_name>");
					}*/

					System.out.println("custName of Customer [" + custName + "]");
					if ("".equals(custName) || custName.trim().length() == 0)
					{
						valueXmlString.append("<addr1><![CDATA[]]></addr1>");
						valueXmlString.append("<addr2><![CDATA[]]></addr2>");
						valueXmlString.append("<addr3><![CDATA[]]></addr3>");
						valueXmlString.append("<city><![CDATA[]]></city>");
						valueXmlString.append("<state_code><![CDATA[]]></state_code>");
						valueXmlString.append("<count_code><![CDATA[]]></count_code>");
						valueXmlString.append("<pin><![CDATA[]]></pin>");
						valueXmlString.append("<tele1><![CDATA[]]></tele1>");
					}
					else
					{
						int i, j;
						String key = "", value = "", val = "", val1 = "", name1 = "";
						String keyaddr1 = "", valueaddr1 = "", shortName="",state="",country="",pinCode="";
						JSONParser parser = new JSONParser();
						try
						{
							org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(custName);
							System.out.println("JsonObj1   " + json);
							Iterator<String> JsonObjKey = json.keySet().iterator();
							while (JsonObjKey.hasNext())
							{
								key = JsonObjKey.next();
								System.out.println("Key [" +key +"]");
								value = json.get(key).toString();
								System.out.println("Value [" + value +"]");

								if (key.equalsIgnoreCase("address_components"))
								{
									org.json.simple.JSONArray adddrcomp = (org.json.simple.JSONArray) parser.parse(value);
									HashMap<String, String> fields= getAddressDetailMap( adddrcomp, conn );
									String cityAdr = fields.get("city");
									valueXmlString.append("<city><![CDATA[" + cityAdr + "]]></city>");
									String stateCodeAdr = fields.get("state_code");
									valueXmlString.append("<state_code><![CDATA[" + stateCodeAdr + "]]></state_code>");
									String countCodeAdr =  fields.get("count_code");
									valueXmlString.append("<count_code><![CDATA[" + countCodeAdr + "]]></count_code>");
									String pinAdr =  fields.get("pin");
									valueXmlString.append("<pin><![CDATA[" + pinAdr + "]]></pin>");
								}
								else if (key.equalsIgnoreCase("adr_address"))
								{
									System.out.println("value in adr_address"+value);
									HashMap<String, String> Address = getAddressMap( value );
									String Adr1=Address.get("addr1");
									valueXmlString.append("<addr1><![CDATA[" + Adr1 + "]]></addr1>");
									String Adr2=Address.get("addr2");
									valueXmlString.append("<addr2><![CDATA["+ Adr2 +"]]></addr2>");
									String Adr3=Address.get("addr3");
									valueXmlString.append("<addr3><![CDATA["+ Adr3  +"]]></addr3>");
								}
								else if (key.equalsIgnoreCase("formatted_phone_number"))
								{
									System.out.println("Value formatted_phone_number" + (value));
									valueXmlString.append("<tele1><![CDATA[" + value + "]]></tele1>");
								}
								else if (key.equalsIgnoreCase("name"))
								{
									System.out.println("Value name" + (value));
									custName = value;
									valueXmlString.append("<cust_name><![CDATA[" + value + "]]></cust_name>");
								}
							}
						}
						catch (Exception e)
						{
							System.out.println("exeception in the cust_name "+e.getMessage());
						}
						if(chqName == null || chqName.trim().length() <= 0)
						{
							valueXmlString.append("<chq_name><![CDATA[" + custName + "]]></chq_name>");
						}
					}
					//changed by Pooja.S on[2-Nov-2018] to set the address and fields by using Json End
				}
				else if (currentColumn.trim().equalsIgnoreCase("terr_code"))
				{
					terrCode = checkNull(genericUtility.getColumnValue("terr_code", dom));
					if (terrCode != null && terrCode.trim().length() > 0)
					{
						sql = "select descr from territory where terr_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, terrCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							terrDesc = checkNull(rs.getString("descr"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<territory_descr><![CDATA[" + terrDesc + "]]></territory_descr>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("sales_pers"))
				{
					salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom));
					sql = "select sp_name from sales_pers where sales_pers=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, salesPers);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						spName = checkNull(rs.getString("sp_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<sp_name><![CDATA[" + spName + "]]></sp_name>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("contact_code"))
				{
					contactCode = checkNull(genericUtility.getColumnValue("contact_code", dom));

					sql = "select name, sh_name, cont_pers, cont_pfx, addr1, addr2,addr3, city, pin, state_code," +
					" count_code, tele1, tele2, tele3,tele_ext, fax, email_addr, edi_addr,contact_type from contact where contact_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, contactCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						name = checkNull(rs.getString("name"));
						shName = checkNull(rs.getString("sh_name"));
						contPers = checkNull(rs.getString("cont_pers"));
						contPfx = checkNull(rs.getString("cont_pfx"));
						addr1 = checkNull(rs.getString("addr1"));
						addr2 = checkNull(rs.getString("addr2"));
						addr3 = checkNull(rs.getString("addr3"));
						lsCity = checkNull(rs.getString("city"));
						lsPin = checkNull(rs.getString("pin"));
						stateCode = checkNull(rs.getString("state_code"));
						countCode = checkNull(rs.getString("count_code"));
						tele1 = checkNull(rs.getString("tele1"));
						tele2 = checkNull(rs.getString("tele2"));
						tele3 = checkNull(rs.getString("tele3"));
						teleExt = checkNull(rs.getString("tele_ext"));
						fax = checkNull(rs.getString("fax"));
						emailAddr = checkNull(rs.getString("email_addr"));
						ediAddr = checkNull(rs.getString("edi_addr"));
						//Added by Amey W 25/9/2018 [To get contact_type from contact master]
						contactType = checkNull(rs.getString("contact_type"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					custName = checkNull(genericUtility.getColumnValue("cust_name", dom));
					if (custName == null || custName.trim().length() <= 0)
					{
						valueXmlString.append("<cust_name>").append("<![CDATA[" + name + "]]>").append("</cust_name>");
						valueXmlString.append("<chq_name><![CDATA[" + name + "]]></chq_name>");
						valueXmlString.append("<sh_name><![CDATA[" + shName + "]]></sh_name>");
					}

					contPfx1 = checkNull(genericUtility.getColumnValue("cont_pfx", dom));

					if (contPfx1 == null || contPfx1.trim().length() <= 0)
					{
						valueXmlString.append("<cont_pfx><![CDATA[" + contPfx + "]]></cont_pfx>");
					}

					contPers1 = checkNull(genericUtility.getColumnValue("cont_pers", dom));
					if (contPers1 == null || contPers1.trim().length() <= 0)
					{
						valueXmlString.append("<cont_pers><![CDATA[" + contPers + "]]></cont_pers>");
					}

					Add1 = checkNull(genericUtility.getColumnValue("addr1", dom));
					if (Add1 == null || Add1.trim().length() <= 0)
					{
						valueXmlString.append("<addr1><![CDATA[" + addr1 + "]]></addr1>");
						valueXmlString.append("<addr2><![CDATA[" + addr2 + "]]></addr2>");
						valueXmlString.append("<addr3><![CDATA[" + addr3 + "]]></addr3>");

						valueXmlString.append("<city><![CDATA[" + lsCity + "]]></city>");
						valueXmlString.append("<pin><![CDATA[" + lsPin + "]]></pin>");

						valueXmlString.append("<state_code><![CDATA[" + stateCode + "]]></state_code>");
						valueXmlString.append("<count_code><![CDATA[" + countCode + "]]></count_code>");

						valueXmlString.append("<tele1><![CDATA[" + tele1 + "]]></tele1>");
						valueXmlString.append("<tele2><![CDATA[" + tele2 + "]]></tele2>");
						valueXmlString.append("<tele3><![CDATA[" + tele3 + "]]></tele3>");

						valueXmlString.append("<tele_ext><![CDATA[" + teleExt + "]]></tele_ext>");
						valueXmlString.append("<fax ><![CDATA[" + fax + "]]></fax>");
					}

					emailAddr1 = checkNull(genericUtility.getColumnValue("email_addr", dom));
					if (emailAddr1 == null || emailAddr1.trim().length() <= 0)
					{
						valueXmlString.append("<email_addr ><![CDATA[" + emailAddr + "]]></email_addr>");
					}

					ediAddr1 = checkNull(genericUtility.getColumnValue("edi_addr", dom));
					if (ediAddr1 == null || ediAddr1.trim().length() <= 0)
					{
						valueXmlString.append("<edi_addr ><![CDATA[" + ediAddr + "]]></edi_addr>");
					}

					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					sql2 = "select key_flag from transetup where tran_window = 'w_customer'";
					pstmt2 = conn.prepareStatement(sql2);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						keyFlag = checkNull(rs2.getString("key_flag"));
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;

					if (custCode == null && !(keyFlag.equalsIgnoreCase("A")))
					{
						contactCode = checkNull(genericUtility.getColumnValue("contact_code", dom));
						valueXmlString.append("<cust_code ><![CDATA[" + contactCode + "]]></cust_code>");
					}

					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));
					if (custCodeBill == null || custCodeBill.trim().length() <= 0)
					{
						contactCode = checkNull(genericUtility.getColumnValue("contact_code", dom));
						valueXmlString.append("<cust_code__bil ><![CDATA[" + contactCode + "]]></cust_code__bil>");

						sql = "select cust_name from customer where cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, contactCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							custName = checkNull(rs.getString("cust_name"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (custName != null)
						{
							valueXmlString.append("<customer_cust_name_1 ><![CDATA[" + custName + "]]></customer_cust_name_1>");
						}
					}

					groupCode = checkNull(genericUtility.getColumnValue("group_code", dom));
					if (groupCode == null || groupCode.trim().length() <= 0)
					{
						contactCode = checkNull(genericUtility.getColumnValue("contact_code", dom));
						valueXmlString.append("<group_code ><![CDATA[" + contactCode + "]]></group_code>");

						sql = "select cust_name from customer where cust_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, contactCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							custName = checkNull(rs.getString("cust_name"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (custName != null)
						{
							valueXmlString.append("<customer_cust_name ><![CDATA[" + custName + "]]></customer_cust_name>");
						}
					}

					//Added by AMEY on 24/09/2018 [start] to get data from supplier default master from cust_type from respective contact code
					if ( contactType != null || contactType.trim().length() > 0 )
					{
						 StringBuffer customerDefaultData = setCustomerDefaultData( contactType, conn, xtraParams, dom );
						 if( customerDefaultData != null && customerDefaultData.length() > 0 )
						 {
							 valueXmlString.append( customerDefaultData );
						 }
					 }
					//Added by AMEY on 24/09/2018 [end] to get data from supplier default master from cust_type from respective contact code

				}
				else if (currentColumn.trim().equalsIgnoreCase("bank_code"))
				{
					bankCode = checkNull(genericUtility.getColumnValue("bank_code", dom));

					sql = "select bank_name from bank where bank_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, bankCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						bankName = checkNull(rs.getString("bank_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<bank_name ><![CDATA[" + bankName + "]]></bank_name>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("channel_partner"))
				{
					channelPartner = checkNull(genericUtility.getColumnValue("channel_partner", dom));

					if (channelPartner.equalsIgnoreCase("Y"))
					{
						valueXmlString.append("<site_code protect = \"0\"><![CDATA[]]></site_code>");
						valueXmlString.append("<fin_link protect = \"0\"><![CDATA[]]></fin_link>");
						valueXmlString.append("<dis_link protect = \"0\"><![CDATA[]]></dis_link>");
					}
					else
					{
						valueXmlString.append("<site_code protect = \"1\"><![CDATA[" + nullVarr + "]]></site_code>");
						valueXmlString.append("<fin_link protect = \"1\"><![CDATA[" + nullVarr + "]]></fin_link>");
						valueXmlString.append("<dis_link protect = \"1\"><![CDATA[" + nullVarr + "]]></dis_link>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("black_listed"))
				{
					lsBKListed = checkNull(genericUtility.getColumnValue("black_listed", dom));

					System.out.println("black_listed :- " + lsBKListed);
					if (lsBKListed.equalsIgnoreCase("N"))
					{
						valueXmlString.append("<black_listed_date protect = \"1\"><![CDATA[" + ldDate + "]]></black_listed_date>");
						valueXmlString.append("<reas_code__bklist protect = \"1\"><![CDATA[" + lsNull + "]]></reas_code__bklist>");
						valueXmlString.append("<bklist_reason protect = \"1\"><![CDATA[" + lsNull + "]]></bklist_reason>");
					} else
					{
						valueXmlString.append("<black_listed_date protect = \"0\"><![CDATA[]]></black_listed_date>");
						valueXmlString.append("<reas_code__bklist protect = \"0\"><![CDATA[]]></reas_code__bklist>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("reas_code__bklist"))
				{
					resBKList = checkNull(genericUtility.getColumnValue("reas_code__bklist", dom));

					sql = "select descr from gencodes where  fld_name = 'REAS_CODE__BKLIST' and fld_value =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, resBKList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<bklist_reason >").append("<![CDATA[" + lsDescr + "]]>").append("</bklist_reason>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("cr_term"))
				{
					crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));

					sql = "select cr_days from crterm where cr_term =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, crTerm);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						crDays = checkNull(rs.getString("cr_days"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<credit_prd ><![CDATA[" + crDays + "]]></credit_prd>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("rate_round"))
				{
					rateRound = checkNull(this.genericUtility.getColumnValue("rate_round", dom));

					if (rateRound.equalsIgnoreCase("N"))
					{
						valueXmlString.append("<rate_round_to protect = \"1\"><![CDATA[]]></rate_round_to>");
					}
					else
					{
						valueXmlString.append("<rate_round_to protect = \"0\"><![CDATA[]]></rate_round_to>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("group_code"))
				{
					groupCode = checkNull(genericUtility.getColumnValue("group_code", dom));

					sql = "select cust_name from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, groupCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						custName = checkNull(rs.getString("cust_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<customer_cust_name ><![CDATA[" + custName + "]]></customer_cust_name>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("cust_code__bil")) {
					custCodeBill = checkNull(genericUtility.getColumnValue("cust_code__bil", dom));

					sql = "select cust_name from customer where cust_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCodeBill);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						custName = checkNull(rs.getString("cust_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<customer_cust_name_1 ><![CDATA[" + custName + "]]></customer_cust_name_1>");
				}
						else if (currentColumn.trim().equalsIgnoreCase("cust_code__dlv")) {
					cust_code__dlv = checkNull(genericUtility.getColumnValue("cust_code__dlv", dom));

				//	sql = "select cust_name from customer where cust_code =?";
				//	pstmt = conn.prepareStatement(sql);
				//	pstmt.setString(1, custCodeBill);
				//	rs = pstmt.executeQuery();
				//	if (rs.next())
				//	{
				//		custName = checkNull(rs.getString("cust_name"));
				//	}
				//	rs.close();
				//	rs = null;
				//	pstmt.close();
				//	pstmt = null;
					// changed by chaitali on 12-06-2019 //
				 valueXmlString.append("<cust_code__bil><![CDATA[" + cust_code__dlv + "]]></cust_code__bil>");
					// changed by chaitali on 12-06-2019 //
				//	valueXmlString.append("<customer_cust_name_1 ><![CDATA[" + custName + "]]></customer_cust_name_1>");
				}
				
			
				
			
		         else if (currentColumn.trim().equalsIgnoreCase("emp_code__ord"))
				{
					empCodeOrd = checkNull(genericUtility.getColumnValue("emp_code__ord", dom));
					if (empCodeOrd != null)
					{
						sql = "select emp_fname,emp_lname,dept_code from employee where emp_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCodeOrd);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							empFname = checkNull(rs.getString("po"));
							empLname = checkNull(rs.getString("emp_lname"));
							deptCode = checkNull(rs.getString("dept_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<emp_fname ><![CDATA[" + empFname + "]]></emp_fname>");
						valueXmlString.append("<emp_lname ><![CDATA[" + empLname + "]]></emp_lname>");
						valueXmlString.append("<dept_code ><![CDATA[" + deptCode + "]]></dept_code>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("emp_code__ord1"))
				{
					empCodeOrd1 = checkNull(genericUtility.getColumnValue("emp_code__ord1", dom));
					if (empCodeOrd1 != null)
					{
						sql = "select emp_fname,emp_lname,dept_code from employee where emp_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCodeOrd1);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							empFname = checkNull(rs.getString("emp_fname"));
							empLname = checkNull(rs.getString("emp_lname"));
							deptCode = checkNull(rs.getString("dept_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<employee_emp_fname ><![CDATA[" + empFname + "]]></employee_emp_fname>");
						valueXmlString.append("<employee_emp_lname ><![CDATA[" + empLname + "]]></employee_emp_lname>");
						valueXmlString.append("<dept_code ><![CDATA[" + deptCode + "]]></dept_code>");
					}
				}
				//Added by AMEY on 24/09/2018 [start] to get data from supplier default master from cust_type
				else if ( currentColumn.trim().equalsIgnoreCase("cust_type") )
				{
					custType = checkNull(genericUtility.getColumnValue("cust_type", dom));
					if ( custType != null || custType.trim().length() > 0 )
					{
						StringBuffer customerDefaultData = setCustomerDefaultData( custType, conn, xtraParams, dom );
						if( customerDefaultData!=null && customerDefaultData.length() > 0 )
						{
							valueXmlString.append(customerDefaultData);
						}
					}
				}
				//Added by AMEY on 24/09/2018 [end] to get data from supplier default master from cust_type

				else if (currentColumn.trim().equalsIgnoreCase("full_name"))
				{
					String fullName1 = checkNull(genericUtility.getColumnValue("full_name", dom));
					if (fullName1 != null && fullName1.length()>=0)
					{
						valueXmlString.append("<chq_name ><![CDATA[" + fullName1 + "]]></chq_name>");
						valueXmlString.append("<eng_name ><![CDATA[" + fullName1 + "]]></eng_name>");
					}
				}
				//Added by Pooja S. End
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
				} while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN [" + currentColumn + "]");

				if (currentColumn.trim().equalsIgnoreCase("reg_code"))
				{
					regCode = checkNull(genericUtility.getColumnValue("reg_code", dom));

					sql = "select descr from reg_requirements where reg_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, regCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						regDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<descr ><![CDATA[" + regDescr + "]]></descr>");
				}
				valueXmlString.append("</Detail2>");
				
				break; // added By Vrushabh on 23-3-20 
			case 3:  //added By Vrushabh on 23-3-20 for co-applicant detail start

				valueXmlString.append("<Detail3>\r\n");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					String lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					prosCustCode = genericUtility.getColumnValue("pros_cust_code", dom1);
					System.out.println("lineNo AND prosCustCode ARE------------>"+lineNo+""+prosCustCode);
				/*	if(lineNo != null && lineNo.trim().equals("1"))
					{

						valueXmlString.append("<cust_name>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("cust_name", dom1))).append("]]>").append("</cust_name>\r\n");
						valueXmlString.append("<org_addr2>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("work_addr1", dom1))).append("]]>").append("</org_addr2>\r\n");
						valueXmlString.append("<org_addr2>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("work_addr2", dom1))).append("]]>").append("</org_addr2>\r\n");
						valueXmlString.append("<org_addr3>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("work_addr3", dom1))).append("]]>").append("</org_addr3>\r\n");
						valueXmlString.append("<email_id>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("email_addr", dom1))).append("]]>").append("</email_id>\r\n");
						valueXmlString.append("<email_id_1>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("email_addr_1", dom1))).append("]]>").append("</email_id_1>\r\n");
						valueXmlString.append("<email_id_2>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("email_addr_2", dom1))).append("]]>").append("</email_id_2>\r\n");
						valueXmlString.append("<pan_no>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("pan_no", dom1))).append("]]>").append("</pan_no>\r\n"); //Added by Sagar on 04/MAR/14
                           

						sql = "select * from PROSPECTIVE_CUSTOMER WHERE CUST_CODE=?";
						System.out.println("Purchase SQL :=" + sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,prosCustCode);
						rs=pstmt.executeQuery();
						if (rs.next())
						{						
							organisationName=rs.getString("organisation_name");       	
							organisationStatus=rs.getString("organisation_status");      	
							orgDesignation=rs.getString("org_designation");        								      	
							orgCity=rs.getString("org_city");       	
							orgStateCode=rs.getString("org_state_code");        	
							orgstanCode=rs.getString("org_stan_code");
							orgpin=rs.getString("org_pin");       	
							orgCountCode=rs.getString("org_count_code");               

							valueXmlString.append("<org_name>").append("<![CDATA[").append(checkNull(organisationName)).append("]]>").append("</org_name>\r\n");
							valueXmlString.append("<org_status>").append("<![CDATA[").append(checkNull(organisationStatus)).append("]]>").append("</org_status>\r\n");
							valueXmlString.append("<org_addr2>").append("<![CDATA[").append(checkNull(orgAddr1)).append("]]>").append("</org_addr2>\r\n");
							valueXmlString.append("<org_addr2>").append("<![CDATA[").append(checkNull(addr2)).append("]]>").append("</org_addr2>\r\n");
							valueXmlString.append("<org_addr3>").append("<![CDATA[").append(checkNull(addr3)).append("]]>").append("</org_addr3>\r\n");
							valueXmlString.append("<org_stan_code>").append("<![CDATA[").append(checkNull(orgstanCode)).append("]]>").append("</org_stan_code>\r\n");
							valueXmlString.append("<org_pin>").append("<![CDATA[").append(checkNull(orgpin)).append("]]>").append("</org_pin>\r\n");
							valueXmlString.append("<org_city>").append("<![CDATA[").append(checkNull(orgCity)).append("]]>").append("</org_city>\r\n");
							valueXmlString.append("<org_state_code>").append("<![CDATA[").append(checkNull(orgStateCode)).append("]]>").append("</org_state_code>\r\n");
							valueXmlString.append("<designation>").append("<![CDATA[").append(checkNull(orgDesignation)).append("]]>").append("</designation>\r\n");
							valueXmlString.append("<org_count_code>").append("<![CDATA[").append(checkNull(orgCountCode)).append("]]>").append("</org_count_code>\r\n");


						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		

					} */
					//Added by Sagar on 04/MAR/14 Start
					if(lineNo != null && lineNo.trim().length()>0)
					{
						System.out.println(">>>>>>>>Add new in Detail>>>>>>>>>>>>>>>>>");
						valueXmlString.append("<addr1>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("addr1", dom1))).append("]]>").append("</addr1>\r\n");
						valueXmlString.append("<addr2>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("addr2", dom1))).append("]]>").append("</addr2>\r\n");
						valueXmlString.append("<addr3>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("addr3", dom1))).append("]]>").append("</addr3>\r\n");	
						valueXmlString.append("<stan_code>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("stan_code", dom1))).append("]]>").append("</stan_code>\r\n");
						valueXmlString.append("<pin>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("pin", dom1))).append("]]>").append("</pin>\r\n");
						valueXmlString.append("<city>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("city", dom1))).append("]]>").append("</city>\r\n");
						valueXmlString.append("<state_code>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("state_code", dom1))).append("]]>").append("</state_code>\r\n");
						valueXmlString.append("<count_code>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("count_code", dom1))).append("]]>").append("</count_code>\r\n");
						
					}
					//Added by Sagar on 04/MAR/14 End

				}
				else if (currentColumn.trim().equalsIgnoreCase("stan_code"))
				{
					stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
					sql = "select state_code, city, pin from station where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stateCode = rs.getString("state_code")==null?"":rs.getString("state_code");
						city = rs.getString("city")==null?"":rs.getString("city");
						pin = rs.getString("pin")==null?"":rs.getString("pin");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select count_code from state where state_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stateCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						countCode = rs.getString("count_code")==null?"":rs.getString("count_code");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;



					valueXmlString.append("<state_code>").append("<![CDATA["+stateCode+"]]>").append("</state_code>");
					valueXmlString.append("<count_code>").append("<![CDATA["+countCode+"]]>").append("</count_code>");
					valueXmlString.append("<city>").append("<![CDATA["+city+"]]>").append("</city>");
					valueXmlString.append("<pin>").append("<![CDATA["+pin+"]]>").append("</pin>");


				}
				else if (currentColumn.trim().equalsIgnoreCase("org_stan_code"))
				{
					stanCode = checkNull(genericUtility.getColumnValue("org_stan_code", dom));
					sql = "select state_code, city, pin from station where stan_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stateCode = rs.getString("state_code")==null?"":rs.getString("state_code");
						city = rs.getString("city")==null?"":rs.getString("city");
						pin = rs.getString("pin")==null?"":rs.getString("pin");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select count_code from state where state_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stateCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						countCode = rs.getString("count_code")==null?"":rs.getString("count_code");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					valueXmlString.append("<org_state_code>").append("<![CDATA["+stateCode+"]]>").append("</org_state_code>");
					valueXmlString.append("<org_count_code>").append("<![CDATA["+countCode+"]]>").append("</org_count_code>");				
					valueXmlString.append("<org_city>").append("<![CDATA["+city+"]]>").append("</org_city>");
					valueXmlString.append("<org_pin>").append("<![CDATA["+pin+"]]>").append("</org_pin>");


				}
				else if (currentColumn.trim().equalsIgnoreCase("appl_order"))
				{
					System.out.println(">>>>>SET appl_order in case 2");
					String lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					String applOrder = checkNull(genericUtility.getColumnValue("appl_order", dom));
					prosCustCode = genericUtility.getColumnValue("pros_cust_code", dom1);
					System.out.println(">>>>lineNo AND prosCustCode ARE------------>"+lineNo+""+prosCustCode);
					System.out.println(">>>applOrder ARE------------>"+applOrder);
					if(applOrder != null && applOrder.trim().equals("1"))
					{

						valueXmlString.append("<cust_name>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("cust_name", dom1))).append("]]>").append("</cust_name>\r\n");
						valueXmlString.append("<org_addr1>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("work_addr1", dom1))).append("]]>").append("</org_addr1>\r\n");
						valueXmlString.append("<org_addr2>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("work_addr2", dom1))).append("]]>").append("</org_addr2>\r\n");
						valueXmlString.append("<org_addr3>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("work_addr3", dom1))).append("]]>").append("</org_addr3>\r\n");
						valueXmlString.append("<email_id>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("email_addr", dom1))).append("]]>").append("</email_id>\r\n");
						valueXmlString.append("<email_id_1>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("email_addr_1", dom1))).append("]]>").append("</email_id_1>\r\n");
						valueXmlString.append("<email_id_2>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("email_addr_2", dom1))).append("]]>").append("</email_id_2>\r\n");
						valueXmlString.append("<pan_no>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValue("pan_no", dom1))).append("]]>").append("</pan_no>\r\n"); //Added by Sagar on 04/MAR/14
                           

						sql = "select * from PROSPECTIVE_CUSTOMER WHERE CUST_CODE=?";
						System.out.println("Purchase SQL :=" + sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,prosCustCode);
						rs=pstmt.executeQuery();
						if (rs.next())
						{						
							organisationName=rs.getString("organisation_name");       	
							organisationStatus=rs.getString("organisation_status");      	
							orgDesignation=rs.getString("org_designation");        								      	
							orgCity=rs.getString("org_city");       	
							orgStateCode=rs.getString("org_state_code");        	
							orgstanCode=rs.getString("org_stan_code");
							orgpin=rs.getString("org_pin");       	
							orgCountCode=rs.getString("org_count_code");               

							valueXmlString.append("<org_name>").append("<![CDATA[").append(checkNull(organisationName)).append("]]>").append("</org_name>\r\n");
							valueXmlString.append("<org_status>").append("<![CDATA[").append(checkNull(organisationStatus)).append("]]>").append("</org_status>\r\n");
							valueXmlString.append("<org_addr1>").append("<![CDATA[").append(checkNull(orgAddr1)).append("]]>").append("</org_addr1>\r\n");
							valueXmlString.append("<org_addr2>").append("<![CDATA[").append(checkNull(addr2)).append("]]>").append("</org_addr2>\r\n");
							valueXmlString.append("<org_addr3>").append("<![CDATA[").append(checkNull(addr3)).append("]]>").append("</org_addr3>\r\n");
							valueXmlString.append("<org_stan_code>").append("<![CDATA[").append(checkNull(orgstanCode)).append("]]>").append("</org_stan_code>\r\n");
							valueXmlString.append("<org_pin>").append("<![CDATA[").append(checkNull(orgpin)).append("]]>").append("</org_pin>\r\n");
							valueXmlString.append("<org_city>").append("<![CDATA[").append(checkNull(orgCity)).append("]]>").append("</org_city>\r\n");
							valueXmlString.append("<org_state_code>").append("<![CDATA[").append(checkNull(orgStateCode)).append("]]>").append("</org_state_code>\r\n");
							valueXmlString.append("<designation>").append("<![CDATA[").append(checkNull(orgDesignation)).append("]]>").append("</designation>\r\n");
							valueXmlString.append("<org_count_code>").append("<![CDATA[").append(checkNull(orgCountCode)).append("]]>").append("</org_count_code>\r\n");


						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		

					}

				}
				
				valueXmlString.append("</Detail3>");
				break;
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
	}    //added By Vrushabh on 23-3-20 for co-applicant detail End

	private String checkNull(String str)
	{
		if (str == null)
		{
			return "";
		}
		else
		{
			return str;
		}
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

	//Added by AMEY on 24/09/2018 [start] to get data from supplier default master from cust_type
	/**
	 *
	 *
	 * @param custType
	 * @param conn
	 * @param xtraParams
	 * @param dom
	 * @return
	 */
	private StringBuffer setCustomerDefaultData( String custType, Connection conn, String xtraParams, Document dom )
	{
		String colName="";
		String colValue="";
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs = null ;
		StringBuffer valueXmlString = new StringBuffer();

		try
		{
			//Added By Pooja S    STRAT
			sql = "select * from Customer_default where cust_type = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custType);
			rs = pstmt.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			ResultSet rset = null;
			PreparedStatement pstm = null;

			if (rs.next())
			{
				for (int i = 1; i <= columnCount; i++)
				{
					colName = rsmd.getColumnName(i).toLowerCase();
					colValue = checkNull(rs.getString(colName));
					if (colValue != null && colValue.trim().length() > 0)
					{
						valueXmlString.append("<" + colName + ">").append("<![CDATA[" + colValue + "]]>").append("</" + colName + ">");
					}
					if (colName.equals("site_code__pay"))
					{
						if (colValue == null || colValue.trim().length() == 0)
						{
							String siteCodePay = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
							valueXmlString.append("<" + colName + ">").append("<![CDATA[" + siteCodePay + "]]>").append("</" + colName + ">");
						}

					}
					/*else if (colName.equals("part_Qty"))
					{
						if (colValue == null || colValue.trim().length() == 0)
						{
							partQty = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
							valueXmlString.append("<" + colName + ">").append("<![CDATA[" + partQty + "]]>").append("</" + colName + ">");
						}
					}*/
					else if (colName.equals("site_code__rcp"))
					{
						if (colValue == null || colValue.trim().length() == 0)
						{
							String siteCodeRcp = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
							valueXmlString.append("<" + colName + ">").append("<![CDATA[" + siteCodeRcp + "]]>").append("</" + colName + ">");
						}
					}
					else if (colName.equals("curr_code"))
					{
						if (colValue == null || (colValue != null && (colValue.equals("") || colValue.trim().length() == 0)))
						{
							String countCode2 = checkNull(genericUtility.getColumnValue("count_code", dom));
							String stateCode1 = checkNull(genericUtility.getColumnValue("state_code", dom));
							sql = "select curr_code from COUNTRY where count_code =? and state_code=?";
							pstm = conn.prepareStatement(sql);
							pstm.setString(1, countCode2);
							pstm.setString(2, stateCode1);
							rset = pstm.executeQuery();
							if (rset.next())
							{
								String currCode2 = checkNull(rset.getString("curr_code"));
								valueXmlString.append("<curr_code ><![CDATA[" + currCode2 + "]]></curr_code>");
							}

							rset.close();
							rset = null;
							pstm.close();
							pstm = null;
						}

					}
					else if (colName.equals("price_list"))
					{
						System.out.print("Value colvalue5  \n [" + colValue + "]");
						if (colValue == null || (colValue != null && (colValue.equals("") || colValue.trim().length() == 0)))
						{
							String countCode3 = checkNull(genericUtility.getColumnValue("count_code", dom));
							String stateCode1 = checkNull(genericUtility.getColumnValue("state_code", dom));
							sql = "select price_list from STATE where count_code =? and state_code=?";
							pstm = conn.prepareStatement(sql);
							pstm.setString(1, countCode3);
							pstm.setString(2, stateCode1);
							rset = pstm.executeQuery();
							if (rset.next())
							{
								String priceList = checkNull(rset.getString("price_list"));
								valueXmlString.append("<price_list ><![CDATA[" + priceList + "]]></price_list>");
								valueXmlString.append("<price_list__disc ><![CDATA[" + priceList + "]]></price_list__disc>");
							}

							rset.close();
							rset = null;
							pstm.close();
							pstm = null;
						}
					}
				}
			}
			//Added By Pooja S  END
		}
		catch(Exception e)
		{
			System.out.println("Customer.setCustomerDefaultData() "+ e.getStackTrace());
		}
		finally
		{

			try
			{
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			catch (SQLException e)
			{
				System.out.println(e);
			}
		}
		return valueXmlString;
	}
	//Added by AMEY on 24/09/2018 [start] to get data from supplier default master from cust_type

	//changed by Pooja.S on[2-Nov-2018] to set the address anf fields by using Json Start
	private HashMap<String, String> getAddressMap(String value )
	{
		HashMap<String, String> map=new HashMap();
		try 
		{
			String firstString = "",finalString="", secondString = "", secString = "", thirdString = "", thiString = "",addr="";
			String Address1="",Address2="",Address3="";
			String FinalS= value.substring(0,value.indexOf("<span class=\"locality\">"));
			String [] arr2=FinalS.split("\\<span.*?\\>");
			for(int k=0;k<arr2.length;k++)
			{
				String[] concatString=arr2[k].split("</span>");
				finalString=finalString.concat(concatString[0]);
			}

			if (finalString.length() > 40)
			{
				addr = finalString.substring(0, 40);
				firstString = addr.substring(0, addr.lastIndexOf(","));
				Address1=firstString;

				String address = finalString.substring(finalString.indexOf(firstString) + firstString.length());

				if (finalString.length() > firstString.length() && address.length() > 40)
				{
					secondString = finalString.substring( firstString.length() +1 ,( firstString.length() +1 )+ 40);
					if(secondString.contains(","))
					{
						secString = secondString.substring(0, secondString.lastIndexOf(","));
						Address2=secString;
					}
					else
					{
						secString = secondString.substring(0, secondString.lastIndexOf(" "));
						Address2=secString;
					}
					String address1 = finalString.substring(finalString.indexOf(secString) + secString.length());

					if (finalString.length() > ((firstString.length()) + (secString.length()))&& address1.length() > 40)
					{
						thirdString = finalString.substring(((finalString.indexOf(secString)) + secString.length()) + 1,
								(((finalString.indexOf(secString)) + secString.length()) + 1) + 40);
						if(thirdString.contains(","))
						{
						thiString = thirdString.substring(0, thirdString.lastIndexOf(","));
						Address3=thiString;
						}
						else
						{
							thiString = thirdString.substring(0, thirdString.lastIndexOf(" "));
							Address3=thiString;
						}
					}
					else
					{
						Address3=address1;

					}
				}
				else
				{
					Address2=address;
					Address3="";
				}
			}
			else
			{
				Address1=finalString;
				Address2="";
				Address3="";
			}
			map.put("addr1", Address1);
			map.put("addr2", Address2);
			map.put("addr3", Address3);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return map;
	}

	private HashMap< String, String > getAddressDetailMap( org.json.simple.JSONArray adddrcomp, Connection conn )
	{
		HashMap<String, String> map=new HashMap();
		String keyaddr = "", valueaddr = "", longName = "";
		String sql="";
		ResultSet rs=null;
		PreparedStatement pstm=null;
		try
		{
			System.out.println("valueaddr " + (adddrcomp.size()));

			org.json.simple.JSONObject addrJson = null;
			for (int j = 0; j < adddrcomp.size(); j++)
			{
				System.out.println("valueaddr ["+ j +"]" + (adddrcomp.get(j)));
				addrJson = (org.json.simple.JSONObject) adddrcomp.get(j);
				System.out.println("json object [ "+ addrJson +"]" );
				JSONParser parser = new JSONParser();
				Iterator<String> addrobjtyp = addrJson.keySet().iterator();
				org.json.simple.JSONArray typeArr = null;
				while (addrobjtyp.hasNext())
				{
					keyaddr = addrobjtyp.next();
					valueaddr = addrJson.get(keyaddr).toString();
					System.out.println( "key : [" + keyaddr + "], value : [" + valueaddr +"]" );

					if( keyaddr.equalsIgnoreCase("long_name") )
					{
						longName = valueaddr;
						System.out.println("value of long term "+longName);
					}
					else if (keyaddr.equalsIgnoreCase("types"))
					{
						typeArr=(org.json.simple.JSONArray) parser.parse(valueaddr);
					}
				}
				if ( longName == null || longName.trim().length() == 0)
				{
					continue;
				}
				String type = typeArr.get(0).toString();
				System.out.println( "longName : ["+ longName +"], type : ["+ type +"]" );
				if (type.equalsIgnoreCase("locality"))
				{
					map.put("city", longName);
				}
				else if ( type.equalsIgnoreCase("administrative_area_level_1") )
				{
					String region="";
					sql = "select state_code from state where DESCR = ?";
					pstm = conn.prepareStatement(sql);
					pstm.setString(1, longName);
					rs = pstm.executeQuery();
					if (rs.next())
					{
						region=rs.getString("state_code");
					}
					rs.close();
					rs = null;
					pstm.close();
					pstm = null;
					if ( region.length() > 0 )
					{
						map.put( "state_code" , region );
					}
				}
				else if (type.equalsIgnoreCase("country"))
				{
					System.out.println("in country " + longName);

					String countryC="";
					sql = "select count_code from Country where DESCR =?";
					pstm = conn.prepareStatement(sql);
					pstm.setString(1, longName);
					rs = pstm.executeQuery();
					if (rs.next()) 
					{
						countryC=rs.getString("count_code");
					}
					rs.close();
					rs = null;
					pstm.close();
					pstm = null;

					if(countryC.length() > 0)
					{
						map.put("count_code", countryC);
					}
				}
				else if (type.equalsIgnoreCase("postal_code"))
				{
					map.put("pin", longName );
					System.out.println("in postal_code " + longName);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception of the SetFields Method"+e.getMessage());
		}
		return map;
	}
	//changed by Pooja.S on[2-Nov-2018] to set the address and fields by using Json End

	private String errorType(Connection conn, String errorCode) throws ITMException
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
			try {
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
			} catch (Exception e)
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
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}
}