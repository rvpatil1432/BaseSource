
/********************************************************
	Title :  Indent Amendment
	Date  : 01/04/21
	Author: Manish Mhatre

 ********************************************************/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
//import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import org.w3c.dom.CDATASection;
import java.util.Properties;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.sys.SysCommon;
import javax.ejb.Stateless; // added for ejb3
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;

@Stateless // added for ejb3

public class IndentAmendmentIC extends ValidatorEJB implements IndentAmendmentICLocal,IndentAmendmentICRemote
{
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	E12GenericUtility genericUtility = new E12GenericUtility();
	UtilMethods utilMethods = new UtilMethods();
	DistCommon disCommon =  new DistCommon();
	FinCommon finCommon = new FinCommon();

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("<====== VALIDATION START =====>");
		System.out.println("xmlString............." + xmlString);
		System.out.println("xmlString1............" + xmlString1);
		System.out.println("xmlString2............" + xmlString2);

		try
		{
			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0)
			{
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) 
			{
				dom2 = genericUtility.parseString(xmlString2);
			}
			retString = wfValData(dom, dom1, dom2, objContext, editFlag,xtraParams);
			System.out.println("ErrString :" + retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception --["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;

	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String startDate = null,endDate = null;
		String columnValue = null;
		String childNodeName = null;
		String errString = "";
		//String errCode = null;
		String userId = null,loginSite = null;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		String sql = null;
		String errCode = "", errorType = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int cnt=0;
		String transer = "I-AMD";
		String itemCode="",stopBusiness="",maxRateStr="",purcRateStr="",unitInd="",unitStd="",empCodePur="",empCodeReq="";
		String quantityOStr="",indentNoT="",analCode="",cctrCodeO="",acctCode="",acctCodeO="",projCode="",indType="",amdNo="",projStatus="",ls_type_allow_projbudgt_list = "",projType="",projTypeOpt="",workOrder="",suppCodePref="",reqDateStr="",indDateStr="";
		double maxRate=0,purcRate=0,quantityO=0,quantity=0,approxCost=0,indentAmt=0,poAmt=0,poRcpAmt=0,poRetAmt=0,totPoAmt=0,prevIndent=0,currentAmount=0,totAmtProj=0,exceedAmt=0,purcRateO=0;		
		boolean ordFlag = true;
		int cnt1=0;
		String [] ls_type_allow_projbudgt = null,projTypeOptlist = null;		
		Date reqDate=null;
		String siteCodeAcct="",siteCodeDlv="",indentNo="",status="",amdDateStr="",siteCode="",quantityStr="",purcRateOStr="";
		Timestamp amdDate = null,indDate=null;
		double poAmt12=0,poRcpAmt11=0,poRetAmt11=0,totPoAmtll=0;

		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		try
		{
			System.out.println("Inside wfValdata ***********");
			conn = getConnection();
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
			loginSite = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}


			switch( currentFormNo )
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

					if(childNodeName.equalsIgnoreCase("ind_no"))
					{
						indentNo = genericUtility.getColumnValue( "ind_no", dom );
						System.out.println( "indent No ::[" + indentNo + "]" );
						if (indentNo == null || indentNo.trim().length() == 0)
						{
							errCode = "VTINDNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select status from indent where ind_no = ? ";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1, indentNo.trim());
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								status = rs.getString( "status" );
							}
							else
							{
								errCode = "VTNOIND";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if("X".equalsIgnoreCase(status.trim()))
							{
								errCode = "VTINDCANC2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if("U".equalsIgnoreCase(status.trim()))
							{
								errCode = "VTINDENT3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if("O".equalsIgnoreCase(status.trim()))
							{
								errCode = "VTINDENT7";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if("C".equalsIgnoreCase(status.trim()))
							{
								errCode = "VTINDENT6";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if("L".equalsIgnoreCase(status.trim()))
							{
								errCode = "VTINDENT4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("amd_date"))
					{
						amdDateStr = genericUtility.getColumnValue("amd_date",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);

						System.out.println( "amdDateStr ::[" + amdDateStr + "] siteCode::[" + siteCode + "]" );

						if (amdDateStr != null && amdDateStr.trim().length() > 0) 
						{
							amdDate = Timestamp.valueOf(genericUtility.getValidDateString(amdDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println( "amd date ::[" + amdDate + "]" );
							errCode = finCommon.nfCheckPeriod("PUR",amdDate,siteCode,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase( "site_code__dlv" ))
					{

						siteCodeDlv = genericUtility.getColumnValue( "site_code__dlv", dom );

						System.out.println( "site code dlv ::[" + siteCodeDlv + "]" );

						if(siteCodeDlv==null || siteCodeDlv.trim().length()==0)
						{
							errCode = "VTBKSITECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							errCode = finCommon.isSiteCode(siteCodeDlv,transer,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if(childNodeName.equalsIgnoreCase( "site_code__acct" ))
					{

						siteCodeAcct = genericUtility.getColumnValue( "site_code__acct", dom );
						System.out.println( "site code acct ::[" + siteCodeAcct + "]" );

						if(siteCodeAcct==null || siteCodeAcct.trim().length()==0)
						{
							errCode = "VTBKSITECD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							errCode = finCommon.isSiteCode(siteCodeAcct,transer,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("req_date"))
					{

						/*reqDateStr = genericUtility.getColumnValue("req_date", dom);
						indDateStr = genericUtility.getColumnValue("ind_date", dom);

						if(reqDateStr!=null && reqDateStr.trim().length()>0)
						{
						reqDate = Timestamp.valueOf(genericUtility.getValidDateString(reqDateStr,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if(indDateStr!=null && indDateStr.trim().length()>0)
						{
						indDate = Timestamp.valueOf(genericUtility.getValidDateString(indDateStr,
								genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}

						if( reqDate.compareTo(indDate) < 0 )
						{
							errCode = "VTINDRQDT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/

						System.out.println("----------- Inside req_date -------------- ");
						reqDateStr = checkNullAndTrim(genericUtility.getColumnValue("req_date", dom));
						indDateStr = checkNullAndTrim(genericUtility.getColumnValue("ind_date", dom1));

						System.out.println( "req date str::[" + reqDateStr + "]" );
						System.out.println( "ind date str ::[" + indDateStr + "]" );

						reqDateStr = (reqDateStr == null || reqDateStr.length() == 0) ? "" : reqDateStr;
						indDateStr = (indDateStr == null || indDateStr.length() == 0) ? "" : indDateStr;

						if(reqDateStr!=null && reqDateStr.trim().length()>0 && indDateStr!=null && indDateStr.trim().length() > 0)
						{
							Date req_date = sdf.parse(reqDateStr);
							Date ind_date = sdf.parse(indDateStr);

							System.out.println( "req date ::[" + req_date + "]" );
							System.out.println( "ind date ::[" + ind_date + "]" );
							if(req_date.before(ind_date))
							{
								errCode = "VLINDENT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println("----------- Inside req_date errCode 1111111 -------------- "+errCode);
							}
						}

					}
					else if (childNodeName.equalsIgnoreCase("supp_code__pref") )
					{

						suppCodePref = genericUtility.getColumnValue( "supp_code__pref", dom );
						siteCode = genericUtility.getColumnValue( "site_code", dom );

						System.out.println( "supp code pref::[" + suppCodePref + "]" );
						System.out.println( "site code::[" + siteCode + "]" );

						if(suppCodePref != null && suppCodePref.trim().length()>0)
						{
							errCode = finCommon.isSupplier(siteCode,suppCodePref,transer,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("work_order"))
					{
						workOrder = genericUtility.getColumnValue( "work_order", dom );

						System.out.println( "work order ::[" + workOrder + "]" );
						if(workOrder != null && workOrder.trim().length()>0)
						{
							sql = "Select status from workorder where work_order = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,workOrder);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								status=rs.getString("status");
							}
							else
							{
								errCode = "VTWORDER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if("C".equalsIgnoreCase(status.trim()))
							{
								errCode = "VTWORDER2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("proj_code"))
					{
						projCode = genericUtility.getColumnValue("proj_code",dom);
						indType = genericUtility.getColumnValue("ind_type",dom);
						amdNo = genericUtility.getColumnValue("amd_no",dom);
						indDateStr = genericUtility.getColumnValue("ind_date",dom);

						System.out.println( "proj code ::[" + projCode + "]" );
						System.out.println( "ind type ::[" + indType + "]" );
						System.out.println( "amd no ::[" + amdNo + "]" );
						System.out.println( "ind date str ::[" + indDateStr + "]" );
						//java.sql.Date ind_date = java.sql.Date.valueOf(genericUtility .getValidDateString(mdate1, genericUtility .getApplDateFormat(), genericUtility .getDBDateFormat()));
						if(indDateStr!=null && indDateStr.trim().length()>0)
						{
							indDate = Timestamp.valueOf(genericUtility.getValidDateString(indDateStr,
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println( "ind date ::[" + indDate + "]" );
						}

						if(amdNo==null || amdNo.trim().length()==0)
						{
							amdNo="@@@";
						}
						if(projCode!=null && projCode.trim().length()>0)
						{
							sql = "Select proj_status from project where proj_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,projCode);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								projStatus=rs.getString("proj_status");
							}
							else
							{
								errCode = "VTPROJCD1";  //PROJ1
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if("C".equalsIgnoreCase(projStatus.trim()))
							{
								errCode = "VTPROJ3";   //PROJ2
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}

						ls_type_allow_projbudgt_list = checkNullAndTrim(disCommon.getDisparams("999999","TYPE_ALLOW_PROJBUDGET",conn));
						System.out.println( "ls_type_aloow_projbudgt_list 444>>[" + ls_type_allow_projbudgt_list + "]" );
						if("NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
						{
							ls_type_allow_projbudgt_list = "";
						}
						System.out.println("ls_type_allow_projbudgt_list ==================>>["+ls_type_allow_projbudgt_list+"]");

						ordFlag = false;
						if(ls_type_allow_projbudgt_list.length() > 0)
						{
							ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");

							for(String str : ls_type_allow_projbudgt)
							{
								if(indType.equalsIgnoreCase(str))
								{
									ordFlag = true;
								}
							}
						}

						System.out.println("ordFlag ==================>>["+ordFlag+"]");
						if(ordFlag)
						{
							if(projCode==null || projCode.trim().length() == 0)
							{
								errCode = "VTPRJCONVL";  //VEPRJ1
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							sql = "SELECT COUNT(*) FROM PROJECT WHERE PROJ_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
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



							sql = " SELECT COUNT(*) FROM PROJECT WHERE PROJ_CODE = ? AND " +
									"(? BETWEEN START_DATE AND END_DATE OR ? BETWEEN START_DATE AND EXT_END_DATE) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							pstmt.setTimestamp(2, indDate);
							pstmt.setTimestamp(3, indDate);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt1 = rs.getInt(1);
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

							if(cnt1 == 0)
							{
								errCode = "VTINVINDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}


						}
						else if(!ordFlag)
						{
							if(projCode!=null && projCode.trim().length() > 0)
							{
								sql = " SELECT PROJ_TYPE  FROM PROJECT WHERE PROJ_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									projType = checkNullAndTrim(rs.getString("PROJ_TYPE"));
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

								projTypeOpt = checkNullAndTrim(disCommon.getDisparams("999999", "PROJECT_TYPE_OPT", conn));
								if("NULLFOUND".equalsIgnoreCase(projTypeOpt))
								{
									projTypeOpt = "";
								}
								System.out.println("projTypeOpt ==================>>["+projTypeOpt+"]");
								System.out.println("projType ==================>>["+projType+"]");

								if(projTypeOpt.length() > 0)
								{
									projTypeOptlist = projTypeOpt.split(",");

									for(String str : projTypeOptlist)
									{
										if(projType.equalsIgnoreCase(str))
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
					else if (childNodeName.equalsIgnoreCase("acct_code"))
					{

						acctCode = genericUtility.getColumnValue("acct_code",dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);

						System.out.println( "acctCode[" + acctCode + "]" );
						System.out.println( "siteCode[" + siteCode + "]" );

						errCode = finCommon.isAcctCode(siteCode,acctCode,transer,conn);

						if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}


					}
					else if (childNodeName.equalsIgnoreCase("cctr_code" ))
					{
						cctrCodeO = genericUtility.getColumnValue( "cctr_code__o", dom );
						acctCodeO = genericUtility.getColumnValue("acct_code__o",dom);

						System.out.println( "cctrCodeO[" + cctrCodeO + "]" );
						System.out.println( "acctCodeO[" + acctCodeO + "]" );
						if(cctrCodeO!=null && cctrCodeO.trim().length()>0)
						{
							errCode = finCommon.isCctrCode(acctCodeO,cctrCodeO,transer,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("anal_code"))
					{
						analCode = genericUtility.getColumnValue("anal_code",dom);
						acctCode = genericUtility.getColumnValue("acct_code",dom);

						System.out.println( "analCode[" + analCode + "]" );
						System.out.println( "acctCode[" + acctCode + "]" );

						if(analCode!=null && analCode.trim().length()>0)
						{
							errCode = finCommon.isAnalysis(acctCode,analCode,transer,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if( childNodeName.equalsIgnoreCase("quantity"))
					{

						System.out.println("----------- Inside quantity -------------- ");
						quantityOStr = genericUtility.getColumnValue("quantity__o", dom);
						indType= genericUtility.getColumnValue("ind_type", dom);
						projCode=genericUtility.getColumnValue("proj_code", dom);
						quantityStr=genericUtility.getColumnValue("quantity", dom);
						purcRateStr=genericUtility.getColumnValue("purc_rate",dom);
						maxRateStr=genericUtility.getColumnValue("max_rate", dom);
						purcRateOStr=genericUtility.getColumnValue("purc_rate__o", dom);		
						amdNo=genericUtility.getColumnValue("amd_no", dom);
						indentNo=genericUtility.getColumnValue("ind_no", dom);
						indDateStr=genericUtility.getColumnValue("ind_date", dom);


						System.out.println( "quantityOStr[" + quantityOStr + "]" );
						System.out.println( "indType[" + indType + "]" );
						System.out.println( "projCode[" + projCode + "]" );
						System.out.println( "quantityStr[" + quantityStr + "]" );
						System.out.println( "purcRateStr[" + purcRateStr + "]" );
						System.out.println( "maxRateStr[" + maxRateStr + "]" );
						System.out.println( "purcRateOStr[" + purcRateOStr + "]" );
						System.out.println( "amdNo[" + amdNo + "]" );
						System.out.println( "indentNo[" + indentNo + "]" );
						System.out.println( "indDateStr[" + indDateStr + "]" );

						if(quantityOStr!=null && quantityOStr.trim().length()>0)
						{
							quantityO=Double.parseDouble(quantityOStr);
						}
						else
						{
							quantityO=0;
						}
						System.out.println( "quantityO[" + quantityO + "]" );

						if(quantityStr!=null && quantityStr.trim().length()>0)
						{
							quantity=Double.parseDouble(quantityStr);
						}
						else
						{
							quantity=0;
						}
						System.out.println( "quantity[" + quantity + "]" );

						if(purcRateStr!=null && purcRateStr.trim().length()>0)
						{
							purcRate=Double.parseDouble(purcRateStr);
						}
						else
						{
							purcRate=0;
						}
						System.out.println( "purcRate[" + purcRate + "]" );

						if(maxRateStr!=null && maxRateStr.trim().length()>0)
						{
							maxRate=Double.parseDouble(maxRateStr);
						}
						else
						{
							maxRate=0;
						}
						System.out.println( "maxRate[" + maxRate + "]" );

						if(purcRateOStr!=null && purcRateOStr.trim().length()>0)
						{
							purcRateO=Double.parseDouble(purcRateOStr);
						}
						else
						{
							purcRateO=0;
						}
						System.out.println( "purcRateO[" + purcRateO + "]" );

						if(indDateStr!=null && indDateStr.trim().length()>0)
						{
							indDate = Timestamp.valueOf(genericUtility.getValidDateString(indDateStr,
									genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						System.out.println( "indDate[" + indDate + "]" );

						if(quantityO <= 0)
						{
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if(amdNo==null || amdNo.trim().length()==0)
						{
							amdNo="@@@";
						}

						ls_type_allow_projbudgt_list = checkNullAndTrim(disCommon.getDisparams("999999","TYPE_ALLOW_PROJBUDGET",conn));
						if("NULLFOUND".equalsIgnoreCase(ls_type_allow_projbudgt_list))
						{
							ls_type_allow_projbudgt_list = "";
						}
						System.out.println("ls_type_allow_projbudgt_list ==================>>["+ls_type_allow_projbudgt_list+"]");

						ordFlag = false;
						if(ls_type_allow_projbudgt_list.length() > 0)
						{
							ls_type_allow_projbudgt = ls_type_allow_projbudgt_list.split(",");

							for(String str : ls_type_allow_projbudgt)
							{
								if(indType.equalsIgnoreCase(str))
								{
									ordFlag = true;
								}
							}
						}

						System.out.println("ordFlag in quantity ==================>>["+ordFlag+"]");
						if(ordFlag)
						{
							if(projCode==null || projCode.trim().length() == 0)
							{
								errCode = "VTPRJCONVL";  //VEPRJ1
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							sql = "SELECT COUNT(*) FROM PROJECT WHERE PROJ_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
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



							sql = " SELECT COUNT(*) FROM PROJECT WHERE PROJ_CODE = ? AND " +
									"(? BETWEEN START_DATE AND END_DATE OR ? BETWEEN START_DATE AND EXT_END_DATE) ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							pstmt.setTimestamp(2, indDate);
							pstmt.setTimestamp(3, indDate);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt1 = rs.getInt(1);
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

							if(cnt1 == 0)
							{
								errCode = "VTINVINDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							if(maxRate<=purcRate)
							{
								errCode = "VTMAXRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(maxRate>purcRate)
							{
								sql = "select approx_cost from project where proj_code= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									approxCost = rs.getDouble("approx_cost");
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

								sql = "select sum((case when quantity is null then 0 else quantity end) * (case when max_rate is null then 0 else max_rate end)) as indent_amt " +
										"from indent where proj_code= ?  and status in ('A','O') and ind_no <> ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								pstmt.setString(2, indentNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									indentAmt = rs.getDouble("indent_amt");
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

								sql = "select ind_no from indent where proj_code= ?  and status in ('L','C') and ord_qty <> 0 ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, projCode);
								rs1 = pstmt1.executeQuery();
								while(rs1.next())
								{
									indentNoT=rs1.getString("ind_no");


									sql = "select sum(a.tot_amt * b.exch_rate) as po_amt from porddet a,porder b " +
											"where (a.purc_order=b.purc_order) " +
											"and b.confirmed='Y' " +
											"and a.proj_code= ? " +
											"and a.ind_no= ? " +
											"and b.status <> 'X' " +
											"and a.status <> 'C' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, projCode);
									pstmt.setString(2, indentNoT);
									rs = pstmt.executeQuery();
									//while(rs.next())   //commented on 15-4-21
									if(rs.next())       //added on 15-4-21
									{
										poAmt=rs.getDouble("po_amt");
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

									sql = "select sum(a.net_amt  * b.exch_rate) as porcp_amt from porcpdet a, porcp b ,porddet c " +
											"where (a.purc_order=c.purc_order) " +
											"and (a.tran_id = b.tran_id ) " +
											"and (a.line_no__ord = c.line_no ) " +
											"and b.confirmed= 'Y' " +
											"and c.proj_code= ? " +
											"and c.ind_no= ? " +
											"and b.status <> 'X' " +
											"and c.status='C' " +
											"and b.tran_ser= 'P-RCP' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, projCode);
									pstmt.setString(2, indentNoT);
									rs = pstmt.executeQuery();
									//while(rs.next())   //commented on 15-4-21
									if(rs.next())        //added on 15-4-21
									{
										poRcpAmt=rs.getDouble("porcp_amt");
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

									sql = "select sum(a.net_amt  * b.exch_rate) as poret_amt from porcpdet a, porcp b ,porddet c " +
											"where (a.purc_order=c.purc_order) " +
											"and (a.tran_id = b.tran_id ) " +
											"and (a.line_no__ord = c.line_no ) " +
											"and b.confirmed= 'Y' " +
											"and c.proj_code= ? " +
											"and c.ind_no= ? " +
											"and b.status <> 'X' " +
											"and c.status='C' " +
											"and b.tran_ser= 'P-RET' ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, projCode);
									pstmt.setString(2, indentNoT);
									rs = pstmt.executeQuery();
									//while(rs.next())   //commented on 15-4-21
									if(rs.next())        //added on 15-4-21
									{
										poRetAmt=rs.getDouble("poret_amt");
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

									totPoAmt = totPoAmt + poAmt + poRcpAmt - poRetAmt;
								}
								//changed on 15-4-21
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

								/*ls_temp = 'lc_tot_poamt total amount'+string(lc_tot_poamt)
									select :ls_temp into :ls_temp from dual;*/

								//6-4-21 [start]

								totPoAmtll=totPoAmt;

								sql = "select sum(a.tot_amt * b.exch_rate) as po_amt from porddet a,porder b " +
										"where (a.purc_order=b.purc_order) " +
										"and b.confirmed='Y' " +
										"and a.proj_code= ? " +
										"and a.ind_no is null " +
										"and b.status <> 'X' " +
										"and a.status <> 'C' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								//while(rs.next())   //commented on 15-4-21
								if(rs.next())        //added on 15-4-21
								{
									poAmt12=rs.getDouble("po_amt");
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

								sql = "select sum(a.net_amt  * b.exch_rate) as porcp_amt from porcpdet a, porcp b ,porddet c " +
										"where (a.purc_order=c.purc_order) " +
										"and (a.tran_id = b.tran_id ) " +
										"and (a.line_no__ord = c.line_no ) " +
										"and b.confirmed= 'Y' " +
										"and c.proj_code= ? " +
										"and c.ind_no is null " +
										"and b.status <> 'X' " +
										"and c.status='C' " +
										"and b.tran_ser= 'P-RCP' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								//while(rs.next())   //commented on 15-4-21
								if(rs.next())       //added on 15-4-21
								{
									poRcpAmt11=rs.getDouble("porcp_amt");
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

								sql = "select sum(a.net_amt  * b.exch_rate) as poret_amt from porcpdet a, porcp b ,porddet c " +
										"where (a.purc_order=c.purc_order) " +
										"and (a.tran_id = b.tran_id ) " +
										"and (a.line_no__ord = c.line_no ) " +
										"and b.confirmed= 'Y' " +
										"and c.proj_code= ? " +
										"and c.ind_no is null " +
										"and b.status <> 'X' " +
										"and c.status='C' " +
										"and b.tran_ser= 'P-RET' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs = pstmt.executeQuery();
								//while(rs.next())   //commented on 15-4-21
								if(rs.next())        //added on 15-4-21
								{
									poRetAmt11=rs.getDouble("poret_amt");
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

								/*ls_temp = 'lc_tot_poamt11 without null'+string(lc_tot_poamt11)
	 								select :ls_temp into :ls_temp from dual;*/

								totPoAmt = totPoAmtll + poAmt12 + poRcpAmt11 - poRetAmt11;

								/*ls_temp = 'lc_tot_poamt total amount'+string(lc_tot_poamt)
									select :ls_temp into :ls_temp from dual;*/

								//6-4-21[end]

								indentAmt= indentAmt+ totPoAmt;

								sql = "select sum((case when quantity is null then 0 else quantity end) * (case when max_rate is null then 0 else max_rate end)) as prev_indent " +
										"from indent_amd " +  
										"where confirmed= 'N' " +
										"and proj_code= ? " +
										"and amd_no= ? " ;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								pstmt.setString(2,amdNo );
								rs = pstmt.executeQuery();
								//while(rs.next())   //commented on 15-4-21
								if(rs.next())        //added on 15-4-21
								{
									prevIndent=rs.getDouble("prev_indent");
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


								currentAmount = quantity * maxRate;

								totAmtProj = indentAmt + currentAmount + prevIndent;

								exceedAmt = totAmtProj - approxCost;

								if (totAmtProj > approxCost)
								{ 
									//errCode = 'VTPROJCOST~t'  + '~r~n' +  ' Project Approved Amount: ' +  string(lc_approxcost) + '~r~n' +  ' Consumed Amount: '  +  string(lc_ind_amount)  + '~r~n' +  ' Current Indent Amd Amount : ' + string(lc_current_amount)  + '~r~n' +  ' Exceeded Amount: '  +  string(lc_exceed_amt)
									errCode = "VTPROJCOST";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}           
							}
						}

					}
					else if ( childNodeName.equalsIgnoreCase( "emp_code__req" ) )
					{

						empCodeReq = genericUtility.getColumnValue( "emp_code__req",dom );
						siteCode = genericUtility.getColumnValue( "site_code",dom );

						System.out.println( "empCodeReq[" + empCodeReq + "]" );
						System.out.println( "siteCode[" + siteCode + "]" );

						if(empCodeReq == null || empCodeReq.trim().length() == 0 )
						{
							errCode = "VTSPCDNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							errCode = getEmployeeResig(empCodeReq,siteCode,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if ( childNodeName.equalsIgnoreCase( "emp_code__pur" ) )
					{
						empCodePur = genericUtility.getColumnValue( "emp_code__pur",dom );
						siteCode = genericUtility.getColumnValue( "site_code",dom );

						System.out.println( "empCodePur[" + empCodePur + "]" );
						System.out.println( "siteCode[" + siteCode + "]" );

						if(empCodePur == null || empCodePur.trim().length() == 0 )
						{
							errCode = "VTSPCDNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							errCode = getEmployeeResig(empCodePur,siteCode,conn);

							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if(childNodeName.equalsIgnoreCase("unit__ind"))
					{
						unitInd = genericUtility.getColumnValue( "unit__ind", dom );
						unitStd = genericUtility.getColumnValue( "unit__std", dom );

						System.out.println( "unitInd[" + unitInd + "]" );
						System.out.println( "unitStd[" + unitStd + "]" );

						if(unitInd==null || unitInd.trim().length()==0)
						{
							errCode = "VTINVUNT1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql ="select count(*) from uom where unit = ? ";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,unitInd);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt==0)
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(!unitInd.trim().equalsIgnoreCase(unitStd.trim()))
							{
								sql = "SELECT COUNT(*) FROM UOMCONV WHERE UNIT__FR = ? AND UNIT__TO = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unitInd);
								pstmt.setString(2, unitStd);
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
									pstmt.setString(1, unitStd);
									pstmt.setString(2, unitInd);
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

					}
					else if(childNodeName.equalsIgnoreCase("max_rate"))
					{
						maxRateStr= genericUtility.getColumnValue("max_rate",dom);
						purcRateStr= genericUtility.getColumnValue("purc_rate",dom);

						System.out.println( "maxRateStr[" + maxRateStr + "]" );
						System.out.println( "purcRateStr[" + purcRateStr + "]" );

						if(maxRateStr!=null && maxRateStr.trim().length()>0)
						{
							maxRate=Double.parseDouble(maxRateStr);
						}
						else
						{
							maxRate=0;
						}

						if(purcRateStr!=null && purcRateStr.trim().length()>0)
						{
							purcRate=Double.parseDouble(purcRateStr);
						}
						else
						{
							purcRate=0;

						}

						System.out.println( "maxRate[" + maxRate + "]" );
						System.out.println( "purcRate[" + purcRate + "]" );

						if(maxRate!=0)
						{
							if(purcRate>=maxRate)
							{
								errCode = "VTMAXRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("item_code"))
					{

						itemCode= genericUtility.getColumnValue("item_code",dom);
						siteCode= genericUtility.getColumnValue("site_code",dom);

						System.out.println( "itemCode[" + itemCode + "]" );
						System.out.println( "siteCode[" + siteCode + "]" );

						sql = "SELECT (CASE WHEN STOP_BUSINESS IS NULL THEN 'N' ELSE STOP_BUSINESS END) AS STOP_BUSINESS FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stopBusiness = rs.getString("STOP_BUSINESS");
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

						if("N".equalsIgnoreCase(stopBusiness))
						{
							errCode = itmDBAccessEJB.isItem(siteCode,itemCode, "", conn); 
							if(errCode!=null && errCode.trim().length()>0)
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
				} //END OF CASE1
				break;
			}//END SWITCH

			int errListSize = errList.size();
			System.out.println("errListSize::::::::::"+errListSize);
			int count = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0)
			{
				for (count = 0; count < errListSize; count++)
				{
					errCode = errList.get(count);
					errFldName = errFields.get(count);
					System.out.println(" testing :errCode .:" + errCode+"errString>>>>>>>>>"+errString);
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
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}//end of if for errList 
			else
			{
				errStringXml = new StringBuffer("");
			}	
			errString = errStringXml.toString();

		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::" +e);
			e.printStackTrace();
			errString = e.getMessage();
		}
		finally
		{
			try
			{
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}

				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				System.out.println("Exception --["+d.getMessage()+"]");
				d.printStackTrace();
				throw new ITMException(d);
			}
			System.out.println(" <IndentAmendMentIC> CONNECTION IS CLOSED");
		}
		System.out.println("ErrString ::" + errString);
		errString = errStringXml.toString();
		System.out.println("testing : final errString : " + errString);
		return errString;
	}//END OF VALIDATION

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;

		String errString = null;
		System.out.println("currFrmXmlStr::"+xmlString);
		System.out.println("hdrFrmXmlStr::"+xmlString1);
		System.out.println("allFrmXmlStr::"+xmlString2);
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
		catch(Exception e)
		{
			System.out.println("Exception : [Indent Amendment][itemChanged(String,String)] :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String childNodeName = null;
		String columnValue = null;
		String loginCode = null;
		String loginCodeName = null;
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String  errString = "";
		String loginSite = null;
		ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat sdf =null;
		String currentDateTime = "";
		Timestamp todaysDate = null;

		String convQtyStduomStr="",empLName="",empFName="",itemDescr="",deptDescr="",budgetAmtAnal="",consumedAmtAnal="",siteDescr="";
		String indentNo="",indType="",deptCode="",itemCode="",unitInd="",quantityStr="";
		String unitOrd="",empCodeAprv="",empCodeReq="",siteCode="",workOrder="",aprCode="";
		String aprUser="",projCode="",priority="",siteCodeDlv="",acctCode="",cctrCode="",empCodePur="";
		String siteCodeAcct="",suppCodePref="",specificInstr="",specialInstr="",remarks="";
		String unitStd="",packInstr="",siteCodeDes="",analCode="",indDateStr="",reqDateStr="",aprDateStr="";
		Timestamp indDate=null,reqDate=null,aprDate=null;
		double quantity=0,ordQty=0,purcRate=0,quantityStduom=0,convQtyStduom=0,maxRate=0,budgetAmt=0,convQtyStduomDp=0;
		ArrayList qty = new ArrayList();
		String dimension="";  //added by manish mhatre on 16-4-21
		double noArt=0;       //added by manish mhatre on 16-4-21

		try
		{
			conn = getConnection();

			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode" );
			sdf =  new SimpleDateFormat(genericUtility.getApplDateFormat());
			todaysDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currentDateTime = sdf.format(todaysDate);

			System.out.println("currentDateTime=>" + currentDateTime);
			System.out.println("todaysDate=>" + todaysDate);
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO:::"+currentFormNo);

			valueXmlString.append( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );

			switch(currentFormNo)
			{
			case 1 :
				valueXmlString.append("<Detail1>");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				int childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println(" Column Value=>" + columnValue);

				if(currentColumn.trim().equals("itm_default"))
				{ 
					valueXmlString.append("<amd_date>").append("<![CDATA["+currentDateTime+"]]>").append("</amd_date>");
				}
				else if(currentColumn.trim().equals("ind_no"))
				{
					indentNo=genericUtility.getColumnValue("ind_no", dom);
					System.out.println( "indentNo[" + indentNo + "]" );


					sql = " Select ind_date, ind_type, dept_code, req_date, item_code, item_descr, quantity, unit__ind, " +
							" ord_qty, unit__ord, emp_code__aprv, apr_date, emp_code__req, site_code, work_order, apr_code, " +
							" apr_user, proj_code, priority, site_code__dlv, acct_code, cctr_code, emp_code__pur, site_code__acct, " +
							" supp_code__pref, purc_rate, specific_instr, special_instr,remarks, quantity__stduom, unit__std, conv__qty_stduom, " +
							" pack_instr, site_code__des, anal_code, dimension, no_art "+    //dimension and no_art added by manish mhatre on 16-4-21
							" from  indent where ind_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indentNo);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						indDate=rs.getTimestamp("ind_date");
						indType=rs.getString("ind_type");
						deptCode=rs.getString("dept_code");
						reqDate=rs.getTimestamp("req_date");
						itemCode=rs.getString("item_code");
						itemDescr=rs.getString("item_descr");
						quantity=rs.getDouble("quantity");
						unitInd=rs.getString("unit__ind");
						ordQty=rs.getDouble("ord_qty");
						unitOrd=rs.getString("unit__ord");
						empCodeAprv=rs.getString("emp_code__aprv");
						aprDate=rs.getTimestamp("apr_date");
						empCodeReq=rs.getString("emp_code__req");
						siteCode=rs.getString("site_code");
						workOrder=rs.getString("work_order");
						aprCode=rs.getString("apr_code");
						aprUser=rs.getString("apr_user");
						projCode=rs.getString("proj_code");
						priority=rs.getString("priority");
						siteCodeDlv=rs.getString("site_code__dlv");
						acctCode=rs.getString("acct_code");
						cctrCode=rs.getString("cctr_code");
						empCodePur=rs.getString("emp_code__pur");
						siteCodeAcct=rs.getString("site_code__acct");
						suppCodePref=rs.getString("supp_code__pref");
						purcRate=rs.getDouble("purc_rate");
						specificInstr=rs.getString("specific_instr");
						specialInstr=rs.getString("special_instr");
						remarks=rs.getString("remarks");
						quantityStduom=rs.getDouble("quantity__stduom");
						unitStd=rs.getString("unit__Std");
						convQtyStduom=rs.getDouble("conv__qty_stduom");
						packInstr=rs.getString("pack_instr");
						siteCodeDes=rs.getString("site_code__des");
						analCode=rs.getString("anal_code");
						dimension=rs.getString("dimension");   //added by manish mhatre on 16-4-21 [getting dimension from indent table]
						noArt=rs.getDouble("no_art");          //added by manish mhatre on 16-4-21 [getting no_art from indent table]
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


					sql = "Select max_rate from indent where ind_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indentNo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						maxRate=rs.getDouble("max_rate");
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

					valueXmlString.append("<max_rate>").append("<![CDATA["+maxRate+"]]>").append("</max_rate>");
					valueXmlString.append("<max_rate__o>").append("<![CDATA["+maxRate+"]]>").append("</max_rate__o>");

					System.out.println("@@@@@indDate in timestamp" + indDate);
					if (indDate != null) 
					{
						indDateStr = sdf.format(indDate.getTime());
					}

					System.out.println("@@@@@indDateStr" + indDateStr);
					if (indDateStr != null && indDateStr.trim().length() > 0) 
					{	
						valueXmlString.append("<ind_date>").append("<![CDATA["+ indDateStr +"]]>").append("</ind_date>");
					} 
					else 
					{
						valueXmlString.append("<ind_date>").append("<![CDATA[]]>").append("</ind_date>");
					}
					//valueXmlString.append("<ind_date>").append("<![CDATA["+indDate+"]]>").append("</ind_date>");
					valueXmlString.append("<ind_type>").append("<![CDATA["+indType+"]]>").append("</ind_type>");
					valueXmlString.append("<dept_code>").append("<![CDATA["+deptCode+"]]>").append("</dept_code>");

					System.out.println("@@@@@req_date in timestamp" + reqDate);
					if (reqDate != null) 
					{
						reqDateStr = sdf.format(reqDate.getTime());
					}

					System.out.println("@@@@@req_date str" + reqDateStr);
					if (reqDateStr != null && reqDateStr.trim().length() > 0) 
					{	
						valueXmlString.append("<req_date>").append("<![CDATA["+ reqDateStr +"]]>").append("</req_date>");
						valueXmlString.append("<req_date__o>").append("<![CDATA["+reqDateStr+"]]>").append("</req_date__o>");
					} 
					else 
					{
						valueXmlString.append("<req_date>").append("<![CDATA[]]>").append("</req_date>");
						valueXmlString.append("<req_date__o>").append("<![CDATA[]]>").append("</req_date__o>");
					}

					//valueXmlString.append("<req_date>").append("<![CDATA["+reqDate+"]]>").append("</req_date>");
					//valueXmlString.append("<req_date__o>").append("<![CDATA["+reqDate+"]]>").append("</req_date__o>");
					valueXmlString.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
					valueXmlString.append("<item_descr>").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");
					valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
					valueXmlString.append("<quantity__o>").append("<![CDATA["+quantity+"]]>").append("</quantity__o>");

					valueXmlString.append("<unit__ind>").append("<![CDATA["+unitInd+"]]>").append("</unit__ind>");
					valueXmlString.append("<ord_qty>").append("<![CDATA["+ordQty+"]]>").append("</ord_qty>");
					valueXmlString.append("<unit__ord>").append("<![CDATA["+unitOrd+"]]>").append("</unit__ord>");
					valueXmlString.append("<emp_code__aprv>").append("<![CDATA["+empCodeAprv+"]]>").append("</emp_code__aprv>");
					valueXmlString.append("<emp_code__aprv__o>").append("<![CDATA["+empCodeAprv+"]]>").append("</emp_code__aprv__o>");

					System.out.println("@@@@@aprDate in timestamp" + aprDate);
					if (aprDate != null) 
					{
						aprDateStr = sdf.format(aprDate.getTime());
					}

					System.out.println("@@@@@aprDate str" + aprDateStr);
					if (aprDateStr != null && aprDateStr.trim().length() > 0) 
					{	
						valueXmlString.append("<apr_date>").append("<![CDATA["+aprDateStr+"]]>").append("</apr_date>");
						valueXmlString.append("<apr_date__o>").append("<![CDATA["+aprDateStr+"]]>").append("</apr_date__o>");
					} 
					else 
					{
						valueXmlString.append("<apr_date>").append("<![CDATA[]]>").append("</apr_date>");
						valueXmlString.append("<apr_date__o>").append("<![CDATA[]]>").append("</apr_date__o>");
					}

					//valueXmlString.append("<apr_date>").append("<![CDATA["+aprDate+"]]>").append("</apr_date>");
					//valueXmlString.append("<apr_date__o>").append("<![CDATA["+aprDate+"]]>").append("</apr_date__o>");
					valueXmlString.append("<emp_code__req>").append("<![CDATA["+empCodeReq+"]]>").append("</emp_code__req>");
					valueXmlString.append("<emp_code__req__o>").append("<![CDATA["+empCodeReq+"]]>").append("</emp_code__req__o>");

					valueXmlString.append("<site_code>").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
					valueXmlString.append("<work_order>").append("<![CDATA["+workOrder+"]]>").append("</work_order>");
					valueXmlString.append("<work_order__o>").append("<![CDATA["+workOrder+"]]>").append("</work_order__o>");
					valueXmlString.append("<apr_code>").append("<![CDATA["+aprCode+"]]>").append("</apr_code>");
					valueXmlString.append("<apr_user>").append("<![CDATA["+aprUser+"]]>").append("</apr_user>");
					valueXmlString.append("<proj_code>").append("<![CDATA["+projCode+"]]>").append("</proj_code>");
					valueXmlString.append("<proj_code__o>").append("<![CDATA["+projCode+"]]>").append("</proj_code__o>");
					valueXmlString.append("<priority>").append("<![CDATA["+priority+"]]>").append("</priority>");
					valueXmlString.append("<priority__o>").append("<![CDATA["+priority+"]]>").append("</priority__o>");

					valueXmlString.append("<site_code__dlv>").append("<![CDATA["+siteCodeDlv+"]]>").append("</site_code__dlv>");
					valueXmlString.append("<site_code__dlv__o>").append("<![CDATA["+siteCodeDlv+"]]>").append("</site_code__dlv__o>");
					valueXmlString.append("<acct_code>").append("<![CDATA["+acctCode+"]]>").append("</acct_code>");
					valueXmlString.append("<acct_code__o>").append("<![CDATA["+acctCode+"]]>").append("</acct_code__o>");
					valueXmlString.append("<cctr_code>").append("<![CDATA["+cctrCode+"]]>").append("</cctr_code>");
					valueXmlString.append("<cctr_code__o>").append("<![CDATA["+cctrCode+"]]>").append("</cctr_code__o>");
					valueXmlString.append("<emp_code__pur>").append("<![CDATA["+empCodePur+"]]>").append("</emp_code__pur>");
					valueXmlString.append("<emp_code__pur__o>").append("<![CDATA["+empCodePur+"]]>").append("</emp_code__pur__o>");
					valueXmlString.append("<site_code__acct>").append("<![CDATA["+siteCodeAcct+"]]>").append("</site_code__acct>");

					valueXmlString.append("<site_code__acct__o>").append("<![CDATA["+siteCodeAcct+"]]>").append("</site_code__acct__o>");
					valueXmlString.append("<supp_code__pref>").append("<![CDATA["+suppCodePref+"]]>").append("</supp_code__pref>");
					valueXmlString.append("<supp_code__pref__o>").append("<![CDATA["+suppCodePref+"]]>").append("</supp_code__pref__o>");
					valueXmlString.append("<purc_rate>").append("<![CDATA["+purcRate+"]]>").append("</purc_rate>");
					valueXmlString.append("<purc_rate__o>").append("<![CDATA["+purcRate+"]]>").append("</purc_rate__o>");
					valueXmlString.append("<specific_instr>").append("<![CDATA["+specificInstr+"]]>").append("</specific_instr>");
					valueXmlString.append("<specific_instr__o>").append("<![CDATA["+specificInstr+"]]>").append("</specific_instr__o>");
					valueXmlString.append("<anal_code__o>").append("<![CDATA["+analCode+"]]>").append("</anal_code__o>");
					valueXmlString.append("<anal_code>").append("<![CDATA["+analCode+"]]>").append("</anal_code>");

					valueXmlString.append("<special_instr>").append("<![CDATA["+specialInstr+"]]>").append("</special_instr>");
					valueXmlString.append("<special_instr__o>").append("<![CDATA["+specialInstr+"]]>").append("</special_instr__o>");
					valueXmlString.append("<remarks>").append("<![CDATA["+remarks+"]]>").append("</remarks>");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA["+quantityStduom+"]]>").append("</quantity__stduom>");
					valueXmlString.append("<unit__std>").append("<![CDATA["+unitStd+"]]>").append("</unit__std>");
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA["+convQtyStduom+"]]>").append("</conv__qty_stduom>");
					valueXmlString.append("<pack_instr__o>").append("<![CDATA["+packInstr+"]]>").append("</pack_instr__o>");
					valueXmlString.append("<pack_instr>").append("<![CDATA["+packInstr+"]]>").append("</pack_instr>");
					valueXmlString.append("<site_code__des>").append("<![CDATA["+siteCodeDes+"]]>").append("</site_code__des>");

					//added by manish mhatre on 16-4-21
					//start manish
					System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);
					valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>");
					valueXmlString.append("<dimension__o>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension__o>"); 

					valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>");
					valueXmlString.append("<no_art__o>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art__o>");

					//end manish

					//commented by manish mhatre on 16-4-21
					/*
					//added by manish mhatre on 06-04-21[For getting old dimension and no_art in indent amd]
					//start manish
					int lineNo=0;
					indentNo=indentNo.trim();
					System.out.println("indent No>>"+indentNo);

					//int indentNoLength=indNo.length();
					System.out.println("ind No lngth"+indentNo.length()); 

					String indentNoReq=indentNo.substring(0,indentNo.length()-2);
					System.out.println("indentNo Req"+indentNoReq); 

					String lineNoStr=indentNo.substring(indentNo.length()-2);
					System.out.println("line no str in string "+lineNoStr);

					if(lineNoStr!=null && lineNoStr.trim().length()>0)
					{
						lineNo=Integer.parseInt(lineNoStr);
						System.out.println("line no in int after parse "+lineNo);

						lineNo=lineNo+1;  //because lineNo 1 in indent_det getting as 0 from indent so added 1
						System.out.println("line no in int after addition "+lineNo);
					}

					System.out.println("indent no from indent det"+indentNoReq+"\n line nostr from indent det"+lineNoStr);
					System.out.println("line no in int from indent det"+lineNo);


					String dimension="";
					double noArt=0;
					sql="select dimension,no_art From indent_det Where ind_no = ? and Line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indentNoReq);
					pstmt.setInt(2, lineNo);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						dimension=rs.getString("dimension");
						noArt=rs.getDouble("no_art");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);

					if(dimension!=null && dimension.trim().length()>0)
					{
						valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>");
						valueXmlString.append("<dimension__o>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension__o>"); 
					}
					if(noArt!=0)
					{
						valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>");
						valueXmlString.append("<no_art__o>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art__o>");
					}
					//end manish
					 */

					//added by manish mhatre on 7-4-21[For getting emp name]
					//start manish
					if(empCodeReq!=null && empCodeReq.trim().length()> 0)
					{
						sql = "select emp_lname,emp_fname from employee where emp_code= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, empCodeReq.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							empLName = rs.getString("emp_lname");
							empFName=rs.getString("emp_fname");
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
						valueXmlString.append( "<employee_emp_lname><![CDATA[" ).append(empLName).append( "]]></employee_emp_lname>\r\n" );
						valueXmlString.append( "<employee_emp_fname><![CDATA[" ).append(empFName).append( "]]></employee_emp_fname>\r\n" );
					}
					//end manish
				}

				else if(currentColumn.trim().equals("site_code"))
				{

					siteCode=genericUtility.getColumnValue("site_code", dom);
					System.out.println( "siteCode[" + siteCode + "]" );

					sql = "select descr from site where site_code= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteDescr = rs.getString("descr");
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
					valueXmlString.append("<site_descr>").append("<![CDATA["+siteDescr+"]]>").append("</site_descr>");
				}

				else if(currentColumn.trim().equals("dept_code"))
				{

					deptCode=genericUtility.getColumnValue("dept_code", dom);
					System.out.println( "deptCode[" + deptCode + "]" );

					sql = "select descr from department where dept_code= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						deptDescr = rs.getString("descr");
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
					valueXmlString.append("<department_descr>").append("<![CDATA["+deptDescr+"]]>").append("</department_descr>");
				}
				else if(currentColumn.trim().equals("item_code"))
				{

					itemCode=genericUtility.getColumnValue("item_code", dom);
					itemDescr=genericUtility.getColumnValue("item_descr", dom);

					analCode=genericUtility.getColumnValue("anal_code", dom);
					siteCode=genericUtility.getColumnValue("site_code", dom);
					acctCode=genericUtility.getColumnValue("acct_code", dom);
					cctrCode=genericUtility.getColumnValue("cctr_code", dom);
					deptCode=genericUtility.getColumnValue("dept_code", dom);

					System.out.println( "itemCode[" + itemCode + "]" );
					System.out.println( "itemDescr[" + itemDescr + "]" );
					System.out.println( "analCode[" + analCode + "]" );
					System.out.println( "siteCode[" + siteCode + "]" );
					System.out.println( "acctCode[" + acctCode + "]" );
					System.out.println( "cctrCode[" + cctrCode + "]" );
					System.out.println( "deptCode[" + deptCode + "]" );

					if(itemDescr==null || itemDescr.trim().length()==0)
					{
						sql = "select descr,apr_code from item where item_code= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemDescr = rs.getString("descr");
							aprCode=rs.getString("apr_code");
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
						valueXmlString.append("<item_descr>").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");
						valueXmlString.append("<apr_code>").append("<![CDATA["+aprCode+"]]>").append("</apr_code>");
					}

					sql = "SELECT FN_GET_BUDGET_AMT(?, ?, ?, ?, ?, ?, ?) , FN_GET_CONS_AMT(?,?,?,?,?,?,?) FROM DUAL ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "I-AMD");
					pstmt.setString(2, siteCode);
					pstmt.setString(3, acctCode);
					pstmt.setString(4, cctrCode);
					pstmt.setString(5, analCode);
					pstmt.setString(6, deptCode);
					pstmt.setString(7, "A");
					pstmt.setString(8, "I-AMD");
					pstmt.setString(9, siteCode);
					pstmt.setString(10, acctCode);
					pstmt.setString(11, cctrCode);
					pstmt.setString(12, analCode);
					pstmt.setString(13, deptCode);
					pstmt.setString(14, "A");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						budgetAmtAnal = checkNullAndTrim(rs.getString(1));
						consumedAmtAnal = checkNullAndTrim(rs.getString(2));
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

					budgetAmtAnal = (budgetAmtAnal.length() == 0) ? "0" : budgetAmtAnal;
					consumedAmtAnal = (consumedAmtAnal.length() == 0) ? "0" : consumedAmtAnal;

					budgetAmt = Double.parseDouble(budgetAmtAnal) - Double.parseDouble(consumedAmtAnal);

					valueXmlString.append( "<budget_amt_anal><![CDATA[" ).append(budgetAmtAnal).append( "]]></budget_amt_anal>\r\n" );
					valueXmlString.append( "<consumed_amt_anal><![CDATA[" ).append(consumedAmtAnal).append( "]]></consumed_amt_anal>\r\n" );
					valueXmlString.append( "<budget_amt><![CDATA[" ).append(budgetAmt).append( "]]></budget_amt>\r\n" );

				}
				else if(currentColumn.trim().equals("emp_code__req"))
				{

					empCodeReq=genericUtility.getColumnValue("emp_code__req", dom);
					System.out.println( "empCodeReq[" + empCodeReq + "]" );

					sql = "select emp_lname,emp_fname from employee where emp_code= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, empCodeReq);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						empLName = rs.getString("emp_lname");
						empFName=rs.getString("emp_fname");
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
					valueXmlString.append( "<employee_emp_lname><![CDATA[" ).append(empLName).append( "]]></employee_emp_lname>\r\n" );
					valueXmlString.append( "<employee_emp_fname><![CDATA[" ).append(empFName).append( "]]></employee_emp_fname>\r\n" );
				}
				else if(currentColumn.trim().equals("quantity"))
				{

					quantityStr=genericUtility.getColumnValue("quantity", dom);
					System.out.println( "quantityStr[" + quantityStr + "]" );
					if(quantityStr!=null && quantityStr.trim().length()>0)
					{
						quantity=Double.parseDouble(quantityStr);
					}
					else
					{
						quantity=0;
					}
					System.out.println( "quantity[" + quantity + "]" );

					itemCode=genericUtility.getColumnValue("item_code", dom);
					unitStd=genericUtility.getColumnValue("unit__std", dom);
					convQtyStduomStr=genericUtility.getColumnValue("conv__qty_stduom", dom);

					System.out.println( "itemCode[" + itemCode + "]" );
					System.out.println( "unitStd[" + unitStd + "]" );
					System.out.println( "convQtyStduomStr[" + convQtyStduomStr + "]" );

					if(convQtyStduomStr!=null && convQtyStduomStr.trim().length()>0)
					{
						convQtyStduom=Double.parseDouble(convQtyStduomStr);
					}
					else
					{
						convQtyStduom=0;
					}
					System.out.println( "convQtyStduom[" + convQtyStduom + "]" );

					unitInd=genericUtility.getColumnValue("unit__ind", dom);
					System.out.println( "unitInd[" + unitInd + "]" );

					convQtyStduomDp=convQtyStduom;

					if(unitInd==null || unitInd.trim().length()==0)
					{
						sql = "select unit from item where item_code= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							unitInd=rs.getString("unit__ind");
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


						qty = disCommon.convQtyFactor(unitInd, unitStd, itemCode, quantity, convQtyStduom, conn);
						System.out.println("qty inside unit ind null ============>>"+qty);

						valueXmlString.append( "<unit__ind><![CDATA[" ).append(unitInd).append( "]]></unit__ind>" );
					}
					else
					{
						qty = disCommon.convQtyFactor(unitInd, unitStd, itemCode, quantity, convQtyStduom, conn);
						System.out.println("qty inside unit ind not null ============>>"+qty);
					}

					/*if(convQtyStduomDp=0)
						{
							valueXmlString.append( "<conv__qty__stduom><![CDATA[" ).append(convQtyStduom).append( "]]></conv__qty__stduom>" );
						}

						valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(quantity).append( "]]></quantity__stduom>" );*/

					if(convQtyStduomDp==0)
					{
						valueXmlString.append( "<conv__qty_stduom><![CDATA[" ).append(qty.get(0).toString()).append( "]]></conv__qty_stduom>\r\n" );
					}
					valueXmlString.append( "<quantity__stduom><![CDATA[" ).append(qty.get(1).toString()).append( "]]></quantity__stduom>\r\n" );

				}

				//added by manish mhatre on 06-04-21
				//start manish
				else if(currentColumn.trim().equals("no_art") || currentColumn.trim().equals("dimension"))
				{
					System.out.println("Inside no_art block or dimension block");
					//String itemCode="",unit="";
					String noArtStr="",unit="";
					//double noArt=0;
					String reStr="";
					int pos=0;

					itemCode= genericUtility.getColumnValue("item_code", dom);
					dimension=genericUtility.getColumnValue("dimension", dom);
					noArtStr= genericUtility.getColumnValue("no_art", dom);

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

							quantity=disCommon.getQuantity(dimension,noArt,unit,conn);

							System.out.println("quantity in dimension block>>"+quantity);
							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity)));
							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("after quantity itemchanged 1877.......");
							pos = reStr.indexOf("<Detail1>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail1>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
					}
				}
				//end manish

				valueXmlString.append("</Detail1>");
				break;

			}//end of switch
			valueXmlString.append("</Root>");
		}//END OF TRY
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
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt!= null)
				{
					pstmt.close();
					pstmt = null;
				}
				if( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}
			System.out.println("[IndentAmendment] Connection is Closed");
		}
		System.out.println("valueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
	}//END OF ITEMCHANGE

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
				msgType = rs.getString("MSG_TYPE");
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
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

	public String checkNull(String inputStr) throws ITMException
	{
		try
		{
			if(inputStr != null && inputStr.trim().length() > 0)
			{
				return inputStr.trim();
			}
			else
			{
				return "";
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in checknull--["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
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

}// END OF MAIN CLASS


