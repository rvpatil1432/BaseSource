/**
 DEVELOPED BY RITESH TIWARI ON 27/03/14 
 PURPOSE: WS3LSUN003 (StarClub Employee details.)
 */
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// added for ejb3
@Stateless
public class EmpTravelInfoIC extends ValidatorEJB implements EmpTravelInfoICLocal,EmpTravelInfoICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	UtilMethods utlmethd=new UtilMethods();
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

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String tranId="";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		//GenericUtility genericUtility = null;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		String errCode = "";
		String errorType = "";
		String errString = "";
		String userId="";
		String itemSer="",sql="",empCode="";
		String empFname="",empMname="",empLname="";
		String passportNo="",stateCodeWork="",mobNo="",telNo="";
		String givenName="",bithPlace="",placeOfIss="",location="",title="";
		int count = 0;
		String nationality="",countName="",code = "", tranDate = "",currentCode = "";
		Timestamp fromDate = null,previousDay = null, tranDateTimeStmp = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{ 
			//genericUtility = GenericUtility.getInstance();
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
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
				tranId= genericUtility.getColumnValue("tran_id", dom);
				System.out.println("tran id from boqdet --4-->>>>["+tranId+"]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("CURRENT COLUMN IN  VALIDATION ["+childNodeName+"]");
					
					if (childNodeName.equalsIgnoreCase("emp_code"))
					{
						empCode= genericUtility.getColumnValue("emp_code", dom);
						tranId= genericUtility.getColumnValue("tran_id", dom);
						tranDate= genericUtility.getColumnValue("tran_date", dom);
						System.out.println(" emp_code :: received ["+empCode +"]");
						if (empCode != null  && empCode.trim().length() > 0)
						{
							if (tranDate != null)
							{
							 tranDateTimeStmp = Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							}
							sql = " select fr_date,code from acctprd where ? between fr_date and to_date "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, tranDateTimeStmp);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								fromDate = rs.getTimestamp("fr_date"); 
								currentCode = rs.getString("code");
							}
							pstmt.close();
							pstmt = null;					
							rs.close();
							rs = null;
							if(fromDate != null)
							{
								previousDay = utlmethd.RelativeDate(fromDate, -1);

								sql = " select code from acctprd where ? between fr_date and to_date ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setTimestamp(1, previousDay);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									code = rs.getString("code");
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
							}
							
							sql = " select count(*) from STARCLUB_ELIGIBILITY_WORKING where emp_code = ? and (case when active is null then 'N' else active end =  'Y') " +
									" and acct_prd = ? "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							pstmt.setString(2, code);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							pstmt.close();
							pstmt = null;					
							rs.close();
							rs = null;
							if(count == 0)
							{
								errCode = "VTINPRV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" emp_code not valid :"+empCode);
							}
							if(tranId == null || tranId.trim().length()==0)
							{
								tranId="X";
							}
								count=0;
								sql = " SELECT count(*)  FROM EMP_TRAVEL_INFO einfo , acctprd acprd WHERE einfo.EMP_CODE= ? "+ 
										" and  einfo.tran_id<> ?     "+
										" and acprd.code= ?  and einfo.tran_date between acprd.fr_date and acprd.to_date ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, empCode);
								pstmt.setString(2, tranId);
								pstmt.setString(3, currentCode);
								//pstmt.setTimestamp(4, tranDateTimeStmp);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									count = rs.getInt(1);
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if (count > 0)
								{
									errCode = "VTINEMP2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println(" emp_code not valid 2:" + empCode);
								}
							/*sql = " select count(*) from STARCLUB_ELIGIBILITY_WORKING where emp_code = ? and (case when active is null then 'N' else active end =  'Y') " +
									" and acct_prd in (select max(acct_prd) from STARCLUB_ELIGIBILITY_WORKING) "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							pstmt.close();
							pstmt = null;					
							rs.close();
							rs = null;
							if(count == 0)
							{
								errCode = "VTINPRV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" emp_code not valid :"+empCode);
							}*/
							/*if("A".equalsIgnoreCase(editFlag))
							{
								sql = " SELECT count(*) FROM EMP_TRAVEL_INFO WHERE EMP_CODE= ? "; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, empCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt(1);
								}
								pstmt.close();
								pstmt = null;					
								rs.close();
								rs = null;
								if(count > 0)
								{
									errCode = "VTINEMP2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println(" emp_code not valid :"+empCode);
								}
							}*/
						}
					}
					
//					if (childNodeName.equalsIgnoreCase("tran_id"))
//					{
//						tranId= genericUtility.getColumnValue("tran_id", dom);
//						System.out.println(" tran_id :: received ["+tranId +"]");
//						if (tranId == null  || tranId.trim().length() == 0)
//						{
//								errCode = "VTTRANNL1";
//								errList.add(errCode);
//								errFields.add(childNodeName.toLowerCase());
//								System.out.println(" passportNo not valid :"+passportNo);
//						}
//					}
					
					if (childNodeName.equalsIgnoreCase("item_ser"))
					{
						itemSer= genericUtility.getColumnValue("item_ser", dom);
						System.out.println(" item_ser :: received ["+itemSer +"]");
						if (itemSer != null  && itemSer.trim().length() > 0)
						{
							sql = " select count(*) from nsm_email where division =  ? and (nsm_code is not null or length(trim(nsm_code)) > 0) "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							pstmt.close();
							pstmt = null;					
							rs.close();
							rs = null;
							if(count == 0)
							{
								errCode = "VTITMSERNT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" itemSer not valid :"+itemSer);
							}
						}
						else
						{
							errCode = "VTDIVNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(" itemSer not valid :"+itemSer);
						}
					}
					if (childNodeName.equalsIgnoreCase("passport_no"))
					{
						passportNo= genericUtility.getColumnValue("passport_no", dom);
						System.out.println(" passport_no :: received ["+passportNo +"]");
						if (passportNo != null  && passportNo.trim().length() > 0)
						{
							String pattern= "^[a-zA-Z0-9]*$";
							if(!passportNo.matches(pattern))
							{
								errCode = "VTPASSNO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" passportNo not valid :"+passportNo);

							}
						}else
						{
							errCode = "VTPASSNO2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(" passportNo blank :"+passportNo);
						}
					}
					if (childNodeName.equalsIgnoreCase("mobile_no"))
					{
						mobNo= genericUtility.getColumnValue("mobile_no", dom);
						System.out.println(" mobile_no :: received ["+mobNo +"]");
						if (mobNo != null  && mobNo.trim().length() > 0)
						{
							String pattern= "^[0-9]*$";
							if(!mobNo.matches(pattern))
							{
								errCode = "VTMOBNO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" mobile_no not valid :"+mobNo);

							}
						}
						else
						{
							errCode = "VTMOBNO2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(" mobile_no null:"+mobNo);
						}
					}
					if (childNodeName.equalsIgnoreCase("cur_tel1"))
					{
						telNo= genericUtility.getColumnValue("cur_tel1", dom);
						System.out.println(" cur_tel1 :: received ["+telNo +"]");
						if (telNo != null  && telNo.trim().length() > 0)
						{
							String pattern= "^[0-9]*$";
							if(!telNo.matches(pattern))
							{
								errCode = "VTTELNO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" cur_tel1 not valid :"+telNo);

							}
						}
					}
					
					if (childNodeName.equalsIgnoreCase("state_code__work"))
					{
						stateCodeWork= genericUtility.getColumnValue("state_code__work", dom);
						System.out.println("state_code__work :: received ["+stateCodeWork +"]");
						if (stateCodeWork != null  && stateCodeWork.trim().length() > 0)
						{
							sql = " select count(*) from state where state_code = ?  "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stateCodeWork);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							pstmt.close();
							pstmt = null;					
							rs.close();
							rs = null;
							if(count == 0)
							{
								errCode = "VTSTCDINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" stateCodeWork not valid :"+itemSer);
							}
						}else
						{
							errCode = "VTSTCDNL1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(" stateCodeWork not valid :"+itemSer);
						}
					}
					
					if (childNodeName.equalsIgnoreCase("count_name"))
					{
						countName= genericUtility.getColumnValue("count_name", dom);
						System.out.println(" count_name :: received ["+countName +"]");
						if (countName == null  || countName.trim().length() == 0)
						{
								errCode = "VTCNTRY1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" count_name not valid :"+countName);
						}
					}
					
					if (childNodeName.equalsIgnoreCase("nationality"))
					{
						nationality= genericUtility.getColumnValue("nationality", dom);
						System.out.println(" nationality :: received ["+nationality +"]");
						if (nationality == null  || nationality.trim().length() == 0)
						{
								errCode = "VTNAT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" nationality not valid :"+nationality);
						}
					}
					
					if (childNodeName.equalsIgnoreCase("birth_place"))
					{
						bithPlace= genericUtility.getColumnValue("birth_place", dom);
						System.out.println(" birth_place :: received ["+bithPlace +"]");
						if (bithPlace == null  || bithPlace.trim().length() == 0)
						{
								errCode = "VTBIRPLC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" birth_place not valid :"+bithPlace);
						}
					}
					
					if (childNodeName.equalsIgnoreCase("place_of_issue"))
					{
						placeOfIss= genericUtility.getColumnValue("place_of_issue", dom);
						System.out.println(" place_of_issue :: received ["+placeOfIss +"]");
						if (placeOfIss == null  || placeOfIss.trim().length() == 0)
						{
								errCode = "VTPLCISS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" place_of_issue not valid :"+placeOfIss);
						}
					}
					if (childNodeName.equalsIgnoreCase("location"))
					{
						location= genericUtility.getColumnValue("location", dom);
						System.out.println(" location :: received ["+location +"]");
						if (location == null  || location.trim().length() == 0)
						{
								errCode = "VTLOC11";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" location not valid :"+location);
						}
					}
					if (childNodeName.equalsIgnoreCase("name_prefix"))
					{
						title= genericUtility.getColumnValue("name_prefix", dom);
						System.out.println(" name_prefix :: received ["+title +"]");
						if (title == null  || title.trim().length() == 0)
						{
								errCode = "VTPRFX1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" name_prefix not valid :"+title);
						}
					}
					if (childNodeName.equalsIgnoreCase("date_of_issue"))
					{
						String dateOfIssueStr= genericUtility.getColumnValue("date_of_issue", dom);
						System.out.println(" date_of_issue :: received ["+dateOfIssueStr +"]");
						if (dateOfIssueStr == null  || dateOfIssueStr.trim().length() == 0)
						{
								errCode = "VTVUPTO2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" date_of_issue not valid :"+dateOfIssueStr);
						}
					}
					if (childNodeName.equalsIgnoreCase("valid_upto"))
					{
						String validUpToStr= genericUtility.getColumnValue("valid_upto", dom);
						String dateOfIssueStr= genericUtility.getColumnValue("date_of_issue", dom);

						System.out.println(" valid_upto :: received ["+validUpToStr +"]");
						if (validUpToStr == null  || validUpToStr.trim().length() == 0)
						{
								errCode = "VTVUPTO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" validUpToStr not valid :"+validUpToStr);
						}
						else if (dateOfIssueStr != null || dateOfIssueStr.trim().length() > 0)
						{
							Timestamp effDate = Timestamp.valueOf(genericUtility.getValidDateString(dateOfIssueStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							Timestamp validUpto = Timestamp.valueOf(genericUtility.getValidDateString(validUpToStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if(validUpto != null && validUpto.before(effDate))
							{
								errCode = "VTVUPTO3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								System.out.println(" validUpToStr not valid :"+validUpToStr);
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


	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ itemChanged called");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
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
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println(" Exception :[itemChanged( String, String )] :==>\n"+ e.getMessage());
		}
		return valueXmlString;

	}

	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("@@@@@@@ itemChanged called");
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		//Node childNode = null;
		//Node parentNode1 = null;
		//String childNodeName = null;
		String sql = "";
		String nsmName = "",itemSer="",nsmCode="";
		String hub="",zoneCode="",stateCode="",loginSite="";
		String chgUser="",chgTerm="";
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = null;
		ConnDriver connDriver = null;
		String currAppdate ="";
		String userId = "",conf="";
		java.sql.Timestamp currDate = null;
		String empCode="",nmPrf="",empFname="",empLname="",desig="",reportTo="",emailId="";
		String placeOfIss="",passNo="",birthPlace="",pinPass="",empMname="";
		String posCode = "",add1="",add2="",add3="",cityPass="",stateCodePass="",mobNo="",telNo="";
		java.util.Date  dateOfIss= null,validUpTo=null,birthDate=null,travelDateTo=null,travelDateFrom= null;
		String state="",countCode="",country="",siteDescr="";
		String	grpCode  ="";
		String tranId="";
		String curState="";
		try
		{
			//genericUtility = GenericUtility.getInstance();
			connDriver =  new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				empCode = rs.getString("EMP_CODE")== null ?"":rs.getString("EMP_CODE");
			}
			
			pstmt.close();
			pstmt = null;					
			rs.close();
			rs = null;
			switch (currentFormNo)
			{
			 case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				//int childNodeListLength = childNodeList.getLength();
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					chgTerm  = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); System.out.println("--term id--"+chgTerm);
					chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgUser");
					loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate);
//					valueXmlString.append("<conf_date>").append("<![CDATA["+currAppdate+"]]>").append("</conf_date>");
					valueXmlString.append("<site_code>").append("<![CDATA["+loginSite+"]]>").append("</site_code>");
					valueXmlString.append("<chg_date>").append("<![CDATA["+currAppdate+"]]>").append("</chg_date>");
					valueXmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
					valueXmlString.append("<emp_code>").append("<![CDATA["+empCode+"]]>").append("</emp_code>");
					valueXmlString.append("<tran_date>").append("<![CDATA["+currAppdate+"]]>").append("</tran_date>");

					sql = " select NAME_PREFIX,EMP_FNAME,EMP_MNAME,EMP_LNAME,DESIGNATION,REPORT_TO,EMAIL_ID_OFF, " +
							" PASSPORT_NO,DATE_OF_ISSUE,PLACE_OF_ISSUE,VALID_UPTO,BIRTH_PLACE,BIRTH_DATE, "+
							" PIN__PASS,POS_CODE,CUR_ADD1,CUR_ADD2,CUR_ADD3,CITY__PASS,STATE_CODE__PASS,MOBILE_NO,CONTACT_TEL,CUR_CITY,CUR_PIN  FROM EMPLOYEE WHERE EMP_CODE  =  ? "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								nmPrf = rs.getString("NAME_PREFIX")==null?"": rs.getString("NAME_PREFIX");
								empFname = rs.getString("EMP_FNAME")==null?"": rs.getString("EMP_FNAME");
								empMname = rs.getString("EMP_MNAME")==null?"": rs.getString("EMP_MNAME");
								empLname = rs.getString("EMP_LNAME")==null?"": rs.getString("EMP_LNAME");
								desig = rs.getString("DESIGNATION")==null?"": rs.getString("DESIGNATION");
								reportTo = rs.getString("REPORT_TO")==null?"": rs.getString("REPORT_TO");
								emailId = rs.getString("EMAIL_ID_OFF")==null?"": rs.getString("EMAIL_ID_OFF");
								passNo = rs.getString("PASSPORT_NO")==null?"": rs.getString("PASSPORT_NO");
								dateOfIss = rs.getTimestamp("DATE_OF_ISSUE");
								placeOfIss = rs.getString("PLACE_OF_ISSUE")==null?"": rs.getString("PLACE_OF_ISSUE");
								validUpTo = rs.getTimestamp("VALID_UPTO");
								birthPlace = rs.getString("BIRTH_PLACE")==null?"": rs.getString("BIRTH_PLACE");
								birthDate = rs.getTimestamp("BIRTH_DATE");
								pinPass = rs.getString("CUR_PIN")==null?"": rs.getString("CUR_PIN");
								posCode = rs.getString("POS_CODE")==null?"": rs.getString("POS_CODE");
								add1 = rs.getString("CUR_ADD1")==null?"": rs.getString("CUR_ADD1");
								add2 = rs.getString("CUR_ADD2")==null?"": rs.getString("CUR_ADD2");
								add3 = rs.getString("CUR_ADD3")==null?"": rs.getString("CUR_ADD3");
								cityPass = rs.getString("CUR_CITY")==null?"": rs.getString("CUR_CITY");
								stateCodePass = rs.getString("STATE_CODE__PASS")==null?"": rs.getString("STATE_CODE__PASS");
								mobNo = rs.getString("MOBILE_NO")==null?"": rs.getString("MOBILE_NO");
								telNo = rs.getString("CONTACT_TEL")==null?"": rs.getString("CONTACT_TEL");
//								curState = rs.getString("STATE_CODE__PASS")==null?"": rs.getString("STATE_CODE__PASS");

							}
							if (pstmt != null)
								pstmt.close();
							pstmt = null;
							if (rs != null)
								rs.close();
							rs = null;
						
							sql = " SELECT DESCR,COUNT_CODE FROM STATE WHERE STATE_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stateCodePass);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								state = rs.getString("DESCR")==null?"": rs.getString("DESCR");

								countCode = rs.getString("COUNT_CODE")==null?"": rs.getString("COUNT_CODE");

							}
							if (pstmt != null)
								pstmt.close();
							pstmt = null;
							if (rs != null)
								rs.close();
							rs = null;
							
							sql = " SELECT DESCR FROM COUNTRY WHERE COUNT_CODE= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
							country = rs.getString("DESCR")==null?"": rs.getString("DESCR");
							}
							if (pstmt != null)
								pstmt.close();
							pstmt = null;
							if (rs != null)
								rs.close();
							rs = null;
							
							sql = " SELECT DESCR FROM SITE WHERE SITE_CODE= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, loginSite);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
							siteDescr = rs.getString("DESCR")==null?"": rs.getString("DESCR");
							}
							if (pstmt != null)
								pstmt.close();
							pstmt = null;
							if (rs != null)
								rs.close();
							rs = null;
						
							/*sql = " SELECT TRAN_ID FROM EMP_TRAVEL_INFO WHERE EMP_CODE= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								tranId = rs.getString("TRAN_ID")==null?"": rs.getString("TRAN_ID");
							}
							if (pstmt != null)
								pstmt.close();
							pstmt = null;
							if (rs != null)
								rs.close();
							rs = null;*/
							
							//valueXmlString.append("<tran_id>").append("<![CDATA["+ tranId +"]]>").append("</tran_id>");

							valueXmlString.append("<site_descr>").append("<![CDATA["+ siteDescr +"]]>").append("</site_descr>");
							valueXmlString.append("<name_prefix>").append("<![CDATA["+ nmPrf +"]]>").append("</name_prefix>");
							valueXmlString.append("<emp_fname>").append("<![CDATA["+ empFname +"]]>").append("</emp_fname>");
							valueXmlString.append("<emp_mname>").append("<![CDATA["+ empMname +"]]>").append("</emp_mname>");
							valueXmlString.append("<emp_lname>").append("<![CDATA["+ empLname +"]]>").append("</emp_lname>");
							valueXmlString.append("<report_to>").append("<![CDATA["+ reportTo +"]]>").append("</report_to>");
							//valueXmlString.append("<designation>").append("<![CDATA["+ desig +"]]>").append("</designation>");
							valueXmlString.append("<birth_place>").append("<![CDATA["+ birthPlace  +"]]>").append("</birth_place>");
							if(birthDate!=null)
							valueXmlString.append("<birth_date>").append("<![CDATA["+ sdf.format(birthDate).toString() +"]]>").append("</birth_date>");
							valueXmlString.append("<email_id>").append("<![CDATA["+ emailId +"]]>").append("</email_id>");
							if(passNo!=null)
							valueXmlString.append("<passport_no>").append("<![CDATA["+ passNo.trim() +"]]>").append("</passport_no>");
							if(validUpTo!=null)
							valueXmlString.append("<valid_upto>").append("<![CDATA["+ sdf.format(validUpTo).toString() +"]]>").append("</valid_upto>");
							valueXmlString.append("<place_of_issue>").append("<![CDATA["+ placeOfIss +"]]>").append("</place_of_issue>");
							if(dateOfIss!=null)
							valueXmlString.append("<date_of_issue>").append("<![CDATA["+ sdf.format(dateOfIss).toString() +"]]>").append("</date_of_issue>");
							valueXmlString.append("<pin__pass>").append("<![CDATA["+ pinPass  +"]]>").append("</pin__pass>");
							valueXmlString.append("<addr1__pass>").append("<![CDATA["+ add1 +"]]>").append("</addr1__pass>");
							valueXmlString.append("<addr2__pass>").append("<![CDATA["+ add2 +"]]>").append("</addr2__pass>");
							valueXmlString.append("<addr3__pass>").append("<![CDATA["+ add3 +"]]>").append("</addr3__pass>");
							valueXmlString.append("<city__pass>").append("<![CDATA["+ cityPass +"]]>").append("</city__pass>");
							valueXmlString.append("<cur_tel1>").append("<![CDATA["+  telNo +"]]>").append("</cur_tel1>");
							valueXmlString.append("<mobile_no>").append("<![CDATA["+ mobNo +"]]>").append("</mobile_no>");
							valueXmlString.append("<emp_name__pass>").append("<![CDATA["+ empFname+" "+empMname+"]]>").append("</emp_name__pass>");
							valueXmlString.append("<state_descr__pass>").append("<![CDATA["+ state  +"]]>").append("</state_descr__pass>");
//							valueXmlString.append("<count_name>").append("<![CDATA["+ country +"]]>").append("</count_name>");
							valueXmlString.append("<count_name>").append("<![CDATA["+ "INDIA" +"]]>").append("</count_name>");
							valueXmlString.append("<nationality>").append("<![CDATA["+ "INDIAN" +"]]>").append("</nationality>");
							valueXmlString.append("<state_code__pass>").append("<![CDATA["+ stateCodePass +"]]>").append("</state_code__pass>");
							valueXmlString.append("<status>").append("<![CDATA["+ "O" +"]]>").append("</status>");
							valueXmlString.append("<meal_pref>").append("<![CDATA["+ "V" +"]]>").append("</meal_pref>");
							valueXmlString.append("<status_date>").append("<![CDATA["+currAppdate+"]]>").append("</status_date>");
							
				}
				if (currentColumn.trim().equalsIgnoreCase("item_ser"))
				{
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					System.out.println("::item_ser::received -" + itemSer);
					sql = " select NSM_CODE,NSM_NAME, group_code,travel_date__to,travel_date__from from nsm_email where division = ? "; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						nsmCode = rs.getString("NSM_CODE")==null?"": rs.getString("NSM_CODE");
						nsmName = rs.getString("NSM_NAME")==null?"": rs.getString("NSM_NAME");
						grpCode = rs.getString("group_code")==null?"": rs.getString("group_code");
						travelDateTo = rs.getTimestamp("travel_date__to");
						travelDateFrom = rs.getTimestamp("travel_date__from");
					}
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					if (rs != null)
						rs.close();
					rs = null;
					
					valueXmlString.append("<emp_code__team_head>").append("<![CDATA["+ nsmCode +"]]>").append("</emp_code__team_head>");
					valueXmlString.append("<descr>").append("<![CDATA["+ nsmName +"]]>").append("</descr>");
					valueXmlString.append("<group_code>").append("<![CDATA["+ grpCode +"]]>").append("</group_code>");
					if(travelDateTo!=null)
					valueXmlString.append("<travel_date__to>").append("<![CDATA["+ sdf.format(travelDateTo) +"]]>").append("</travel_date__to>");
					if(travelDateFrom!=null)
					valueXmlString.append("<travel_date__from>").append("<![CDATA["+ sdf.format(travelDateFrom) +"]]>").append("</travel_date__from>");

				}
				if (currentColumn.trim().equalsIgnoreCase("emp_code__team_head"))
				{
					nsmCode = genericUtility.getColumnValue("emp_code__team_head", dom);
					System.out.println("::emp_code__team_head::received -" + nsmCode);
					
					sql = " select NSM_NAME from nsm_email where NSM_CODE  =  ? "; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, nsmCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						nsmName = rs.getString("NSM_NAME")==null?"": rs.getString("NSM_NAME");
					}
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					if (rs != null)
						rs.close();
					rs = null;
					valueXmlString.append("<descr>").append("<![CDATA["+ nsmName +"]]>").append("</descr>");

				}
				if (currentColumn.trim().equalsIgnoreCase("state_code__work"))
				{
					stateCode = genericUtility.getColumnValue("state_code__work", dom);
					System.out.println("::state_code__work::received -" + stateCode);
					
					sql = " select ZONE_CODE,HUB from starclub_hub_master where state_code  =  ? "; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stateCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						zoneCode = rs.getString("ZONE_CODE")==null?"": rs.getString("ZONE_CODE");
						hub = rs.getString("HUB")==null?"": rs.getString("HUB");
					}
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					if (rs != null)
						rs.close();
					rs = null;
					valueXmlString.append("<zone_code>").append("<![CDATA["+ zoneCode +"]]>").append("</zone_code>");
					valueXmlString.append("<hub>").append("<![CDATA["+ hub +"]]>").append("</hub>");

				}
//				if (currentColumn.trim().equalsIgnoreCase("emp_code"))
//				{
//					empCode = genericUtility.getColumnValue("emp_code", dom);
//					System.out.println("::emp_code::received -" + empCode);
//					
//						}
				valueXmlString.append("</Detail1>");
		    }
			valueXmlString.append("</Root>");

		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
				{
					if (pstmt != null)
						pstmt.close();
					pstmt = null;
					if (rs != null)
						rs.close();
					rs = null;
					conn.close();
					conn = null;
				}
			} catch (Exception d)
			{
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
	//for flight code pophelp added by chandrashear
	public String getFlightCodeList(String flightCode) throws ITMException 
	{
		System.out.println("flightCode cal------------------["+flightCode+"]");
		String sql = "";
		
		StringBuffer valueXmlString = new StringBuffer("<Root>\r\n");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try 
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				sql = " select fld_name,descr,fld_value from gencodes where fld_name='FLIGHT_CODE' AND MOD_NAME='W_EMP_TRAVEL_INFO' order by fld_value ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				int num = 1;
				while (rs.next()) 
				{
					valueXmlString.append("<flight domID='" + num	+ "' selected = 'N'>\r\n");
					valueXmlString.append("<fld_name><![CDATA[").append(rs.getString("fld_name")).append("]]></fld_name>\r\n");
					valueXmlString.append("<descr><![CDATA[").append(rs.getString("descr")).append("]]></descr>\r\n");
					valueXmlString.append("<fld_value><![CDATA[").append(rs.getString("fld_value")).append("]]></fld_value>\r\n");
					
					valueXmlString.append("</flight>\r\n");
					num++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null ;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception :EmpTravelInfoIC :getFlightCodeList(String,String):"	+ e.getMessage() + ":");
			valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		} 
		finally 
		{
			try 
			{
				if (conn != null && !conn.isClosed()) 
				{					
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("Exception :EmpTravelInfoIC :getFlightCodeList(String,String) :==>\n"+ e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("\n****ValueXmlString ::" + valueXmlString.toString()	+ ":********");
		return valueXmlString.toString();
	}
}
