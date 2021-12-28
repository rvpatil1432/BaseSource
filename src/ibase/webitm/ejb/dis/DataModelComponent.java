package ibase.webitm.ejb.dis;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.json.JSONArray;
import org.json.JSONObject;

import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.E12DataModelComponent;
import ibase.webitm.ejb.InfinispanLocal;

public class DataModelComponent extends E12DataModelComponent 
{
	static InfinispanLocal infinispanLocalObj = null;
	static Hashtable jndiProperties = null;
	static InitialContext ctx = null;

	static
	{
		try 
		{
			if( CommonConstants.CACHE_OPT != 0 )
			{
				jndiProperties = new Hashtable();
				jndiProperties.put(Context.URL_PKG_PREFIXES,"org.jboss.ejb.client.naming");
				ctx=new InitialContext(jndiProperties);
				infinispanLocalObj = ((InfinispanLocal)ctx.lookup("ibase/InfinispanEJB/local"));
				BaseLogger.log("1", null, null,"MasterStatefulEJB infinispanLocalObj created.....");
			}
		}
		catch (Exception e)
		{
			BaseLogger.log("0", null, null,"Exception in MasterStatefulEJB static block:["+e.getMessage()+"]");
			e.printStackTrace();
		}
	}
	
	public String getDMDataJson( String dataModelName, String fieldValue, String userInfoStr)throws Exception
	{
		BaseLogger.log("2", null, null,"DataModelComponent 2835 dataModelName= "+dataModelName+" fieldValue= "+fieldValue+" userInfoStr= "+userInfoStr );
		String returnJsonData = "";
		String imgPath ="";
		try
		{
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			Connection myCon = null;
			PreparedStatement pstmt = null;
			String finalQuery="";
			JSONArray jsonArray = new JSONArray();
			fieldValue = checkNull(fieldValue);
			String dataModelKey = "";
			if( "".equals(fieldValue) )
			{
				//Added By Pankaj T. on 19-02-19 add enterprise in key for uniqueness to avoid data conflict
				dataModelKey = dataModelName.concat("_"+userInfo.getEnterprise());
			}
			else
			{
				//Added By Pankaj T. on 19-02-19 add enterprise in key for uniqueness to avoid data conflict
				dataModelKey = dataModelName.concat("_"+fieldValue).concat("_"+userInfo.getEnterprise());
			}
			BaseLogger.log("3", userInfo, null,"inside getDMDataJson dataModelKey ==> ["+dataModelKey+"]");
			if( CommonConstants.CACHE_OPT != 0 && infinispanLocalObj.FormDataContains(dataModelKey) )
			{
				returnJsonData = infinispanLocalObj.getFormData(dataModelKey).toString();
			}
			else
			{

				//switch (dataModelName)
				if ( dataModelName.equalsIgnoreCase("account") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT  acct.ACCT_CODE, acct.DESCR, acct.SH_DESCR, acct.DET_DESCR, acct.ACCT_TYPE, acct.ACTIVE, acct.TYP_BAL, acct.CURR_CODE, \n" +
							"acct.FORMAT_CODE, acct.LEDG_POST, acct.SUNDRY_TYPE,\n" +
							"ACCTGRP.GROUP_CODE as GRP_CODE, ACCTGRP.DESCR as GRP_CODE_DESCR, acct.SGROUP_CODE, ACCTSGRP.DESCR as SUB_GRP_CODE_DESCR\n" +
							"FROM ACCOUNTS acct\n" +
							"INNER JOIN ACCTSGRP ON acct.SGROUP_CODE = ACCTSGRP.SGROUP_CODE\n" +
							"INNER JOIN ACCTGRP ON ACCTGRP.GROUP_CODE = ACCTSGRP.GROUP_CODE\n" +
							"where acct.ACCT_CODE = '"+fieldValue+"'" :  "SELECT * FROM ACCOUNTS";
				}
				else if ( dataModelName.equalsIgnoreCase("cost-center") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM COSTCTR WHERE CCTR_CODE = '"+fieldValue+"'" :  "SELECT * FROM COSTCTR";
				}
				else if ( dataModelName.equalsIgnoreCase("site") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT SITE.*,SITEREGNO.REG_NO,(SELECT DESCR FROM GENCODES WHERE FLD_NAME = 'SITE_TYPE' and FLD_VALUE = SITE.SITE_TYPE) as SITE_TYPE_DESCR FROM SITE INNER JOIN SITEREGNO \n" +
							"ON SITE.SITE_CODE=SITEREGNO.SITE_CODE AND SITE.SITE_CODE ='"+fieldValue+"'" :  "SELECT * FROM SITE";
				}
				else if ( dataModelName.equalsIgnoreCase("credit-term") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM CRTERM WHERE CR_TERM= '"+fieldValue+"'" :  "SELECT * FROM CRTERM";
				}
				else if ( dataModelName.equalsIgnoreCase("speciality") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM SPECIALITY WHERE SPL_CODE= '"+fieldValue+"'" :  "SELECT * FROM SPECIALITY";
				}
				else if ( dataModelName.equalsIgnoreCase("strg-customer") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT sc_code, UPPER(FIRST_NAME)||' '||UPPER(LAST_NAME) AS SC_NAME, addr1, addr2, addr3, email_addr, mobile_no\n" +
							" FROM STRG_CUSTOMER where SC_CODE= '"+fieldValue+"'" :  "SELECT * FROM STRG_CUSTOMER";
				}
				else if ( dataModelName.equalsIgnoreCase("employee") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT EMP_CODE, UPPER(EMP_FNAME)||' '||UPPER(EMP_LNAME) AS EMP_NAME, DESIGNATION, DEPT_CODE, "
							+ "REPORT_TO, CUR_ADD1, CUR_ADD2, CUR_ADD3, CUR_CITY, CUR_STATE, CUR_PIN FROM EMPLOYEE where EMP_CODE= '"+fieldValue+"'" :  "SELECT * FROM EMPLOYEE";
				}
				else if ( dataModelName.equalsIgnoreCase("grade") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM GRADE WHERE GRADE_CODE= '"+fieldValue+"'" :  "SELECT * FROM GRADE";
				}
				else if ( dataModelName.equalsIgnoreCase("item-series") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT ITEM_SER, descr, SH_DESCR, SITE_CODE,LOC_CODE, UNIT FROM ITEMSER WHERE ITEM_SER = '"+fieldValue+"'" :  "SELECT * FROM ITEMSER";
				}

					//By Sainath T. on 13/12/2018 [To add columns in supplier details]-START
					/*case "supplier":
						finalQuery = (!"".equals(fieldValue)) ? "SELECT SUPP_CODE, SUPP_NAME, FULL_NAME, SH_NAME, GROUP_CODE, ADDR1, ADDR2, ADDR3, CITY, STATE_CODE, COUNT_CODE, STAN_CODE, SITE_CODE, EMAIL_ADDR\n" +
								                                " FROM SUPPLIER WHERE SUPP_CODE='"+fieldValue+"'" :  "SELECT * FROM SUPPLIER";
						break;*/
					//Changed by Amey W on 28/01/2019 [To display PAN_NO in side panel] START
				else if ( dataModelName.equalsIgnoreCase("supplier") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT S.SUPP_CODE,S.SUPP_NAME,S.SUPP_TYPE,S.FULL_NAME,S.SH_NAME,S.GROUP_CODE,S.ADDR1, S.ADDR2, S.ADDR3,S.CITY,S.PIN, \n " +
							"S.EMAIL_ADDR,S.TELE1,S.TELE2,S.TELE3, \n"+
							"S.STATE_CODE,STAT.DESCR AS STATE_DECR,S.COUNT_CODE,C.DESCR COUNTRY_DESCR,S.STAN_CODE, \n"+
							"ST.DESCR AS STATION_DESCR,S.SITE_CODE,S.EMAIL_ADDR,S.CR_TERM,S.PAN_NO,S.TAX_REG_2,CRD.DESCR AS CR_TERM_DESCR, \n"+
							"S.DLV_TERM,D.DESCR AS DLV_TERM_DESCR \n"+
							"FROM SUPPLIER S,STATE STAT,COUNTRY C,STATION ST,CRTERM CRD,DELIVERY_TERM D \n"+
							"where STAT.STATE_CODE = S.STATE_CODE and C.COUNT_CODE = S.COUNT_CODE \n"+
							"and ST.STAN_CODE = S.STAN_CODE and CRD.CR_TERM = S.CR_TERM and D.DLV_TERM = S.DLV_TERM \n"+
							"and S.SUPP_CODE='"+fieldValue+"'" :  "SELECT * FROM SUPPLIER";
				}

					/*	case "customer":
						finalQuery = (!"".equals(fieldValue)) ? "SELECT S.CUST_CODE,S.CUST_NAME,S.CUST_TYPE,S.FULL_NAME,S.SH_NAME,S.GROUP_CODE,S.ADDR1, S.ADDR2, S.ADDR3,S.CITY,S.PIN, \n " +
								"S.EMAIL_ADDR,S.TELE1,S.TELE2,S.TELE3, \n"+
								"S.STATE_CODE,STAT.DESCR AS STATE_DECR,S.COUNT_CODE,C.DESCR COUNTRY_DESCR,S.STAN_CODE, \n"+
								"ST.DESCR AS STATION_DESCR,S.SITE_CODE,S.EMAIL_ADDR,S.CR_TERM,S.PAN_NO,S.TAX_REG_2,CRD.DESCR AS CR_TERM_DESCR, \n"+
								"S.DLV_TERM,D.DESCR AS DLV_TERM_DESCR \n"+
								"FROM CUSTOMER S,STATE STAT,COUNTRY C,STATION ST,CRTERM CRD,DELIVERY_TERM D \n"+
								"where STAT.STATE_CODE = S.STATE_CODE and C.COUNT_CODE = S.COUNT_CODE \n"+
								"and ST.STAN_CODE = S.STAN_CODE and CRD.CR_TERM = S.CR_TERM and D.DLV_TERM = S.DLV_TERM \n"+
								"and S.CUST_CODE='"+fieldValue+"'" :  "SELECT * FROM CUSTOMER";
						break;*/
					//Changed by Amey W on 28/01/2019 [To display PAN_NO in side panel] END
					//By Sainath T. on 13/12/2018 [To add columns in supplier details]-END
				/*else if(  dataModelName.equalsIgnoreCase("customer") )
				{
					finalQuery = "SELECT CUSTOMER.CUST_NAME ,CUSTOMER.CURR_CODE,CONTACT.TELE1 AS CONTACT, CONTACT.NAME AS CONTACT_NAME,CONTACT.EMAIL_ADDR, CUSTOMER.CUST_PRIORITY AS RANK,CASE WHEN CRTERM.DESCR IS NULL THEN CUSTOMER.CR_TERM ELSE CRTERM.DESCR END AS CRTERM_DESC ,CASE  WHEN DELIVERY_TERM.DESCR IS NULL THEN CUSTOMER.DLV_TERM ELSE DELIVERY_TERM.DESCR END AS DLVTERM_DESC, SALES_PERS.SP_NAME , SALES_PERS.EMAIL_ADDR AS SALES_EMAIL, CUSTOMER.BLACK_LISTED, CUSTOMER.BLACK_LISTED_DATE, (SELECT FN_OUVERDUE_CHECK(null) FROM DUAL ) AS DUE_AMOUNT_TILL_DATE, CUSTOMER.CREDIT_LMT, CUSTOMER.WAVE_TYPE, (SELECT FN_PAYAMT_CHECK(null) FROM DUAL ) AS PAY_AMOUNT, CUSTOMER.TAX_REG_2 AS GST_TAX_REG FROM CUSTOMER CUSTOMER, CONTACT CONTACT,  CRTERM CRTERM, DELIVERY_TERM DELIVERY_TERM, SALES_PERS SALES_PERS WHERE CUSTOMER.CONTACT_CODE = CONTACT.CONTACT_CODE  AND CUSTOMER.CR_TERM = CRTERM.CR_TERM(+)  AND CUSTOMER.DLV_TERM  = DELIVERY_TERM.DLV_TERM(+)  AND CUSTOMER.SALES_PERS  =  SALES_PERS.SALES_PERS (+)  AND CUSTOMER.CUST_CODE = '"+fieldValue+"'" : "SELECT * FROM CUSTOMER";
				}*/
				else if( dataModelName.equalsIgnoreCase("customer") )
				{
					System.out.println("xxx========> in customer");
					
					finalQuery = (!"".equals(fieldValue)) ? "SELECT CUSTOMER.CUST_NAME , " +
				"  CUSTOMER.CURR_CODE, " +
				"  CONTACT.TELE1 AS CONTACT, " +
				"  CONTACT.NAME  AS CONTACT_NAME, " +
				"  CONTACT.EMAIL_ADDR , " +
				"  CUSTOMER.CUST_PRIORITY AS RANK, " +
				"  CASE " +
				"    WHEN CRTERM.DESCR IS NULL " +
				"    THEN CUSTOMER.CR_TERM " +
				"    ELSE CRTERM.DESCR " +
				"  END AS CRTERM_DESC , " +
				"  CASE " +
				"    WHEN DELIVERY_TERM.DESCR IS NULL " +
				"    THEN CUSTOMER.DLV_TERM " +
				"    ELSE DELIVERY_TERM.DESCR " +
				"  END AS DLVTERM_DESC, " +
				"  SALES_PERS.SP_NAME , " +
				"  SALES_PERS.EMAIL_ADDR AS SALES_EMAIL, " +
				"  CUSTOMER.BLACK_LISTED, " +
				"  CUSTOMER.BLACK_LISTED_DATE, " +
				"  (SELECT FN_OUVERDUE_CHECK('"+fieldValue+"') FROM DUAL " +
				"  ) AS DUE_AMOUNT_TILL_DATE, " +
				"  CUSTOMER.CREDIT_LMT, " +
				"  CUSTOMER.WAVE_TYPE, " +
				"  (SELECT FN_PAYAMT_CHECK('"+fieldValue+"') FROM DUAL " +
				"  ) AS PAY_AMOUNT, " +
				" CUSTOMER.TAX_REG_2 AS GST_TAX_REG"+//Added by PriyankaC on 28July2017.
				" FROM CUSTOMER CUSTOMER, " +
				"  CONTACT CONTACT , " +
				"  CRTERM CRTERM, " +
				"  DELIVERY_TERM DELIVERY_TERM, " +
				"  SALES_PERS SALES_PERS " +
				" WHERE CUSTOMER.CONTACT_CODE = CONTACT.CONTACT_CODE " +
				" AND CUSTOMER.CR_TERM        = CRTERM.CR_TERM(+) " +
				" AND CUSTOMER.DLV_TERM       = DELIVERY_TERM.DLV_TERM(+) " +
				" AND CUSTOMER.SALES_PERS     =  SALES_PERS.SALES_PERS (+) " +
				" AND CUSTOMER.CUST_CODE      = '"+fieldValue+"'" :  "SELECT * FROM CUSTOMER";
					
				
				}
				
				else if ( dataModelName.equalsIgnoreCase("taxclass") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM TAXCLASS WHERE TAX_CLASS = '"+fieldValue+"'" :  "SELECT * FROM TAXCLASS";
				}
				else if ( dataModelName.equalsIgnoreCase("taxchap") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM TAXCHAP WHERE TAX_CHAP = '"+fieldValue+"'" :  "SELECT * FROM TAXCHAP";
				}
				else if ( dataModelName.equalsIgnoreCase("taxenv") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM TAXENV WHERE TAX_ENV = '"+fieldValue+"'" :  "SELECT * FROM TAXENV";
				}
				else if ( dataModelName.equalsIgnoreCase("finentity") )
				{
					finalQuery = (!"".equals(fieldValue)) ? "SELECT * FROM FINENT WHERE FIN_ENTITY = '"+fieldValue+"'" :  "SELECT * FROM FINENT";
				}
				else
				{
					BaseLogger.log("3", null, null,"dataModelName["+dataModelName+"] Doesn't match !");
				}

				if( !"".equalsIgnoreCase(finalQuery) )
				{
					try
					{
						//BaseLogger.log("3", userInfo, null,"Executing Query...........");
						ConnDriver connDriver = new ConnDriver();
						//UserInfoBean userInfo = new UserInfoBean(userInfoStr);
						//JSONArray jsonArray = new JSONArray();
						String transDB = userInfo.getTransDB();
						BaseLogger.log("3", userInfo, null,"inside createDataJson transDB ==> "+transDB);
						myCon = connDriver.getConnectDB(transDB);
						E12GenericUtility genericUtility = new E12GenericUtility();
						String dbDataFormat = genericUtility.getDBDateFormat();
						String applDataFormat = genericUtility.getApplDateFormat();

						pstmt = myCon.prepareStatement( finalQuery );
						ResultSet rs = pstmt.executeQuery();
						ResultSetMetaData rsmd = rs.getMetaData();
						int numberOfColumns = rsmd.getColumnCount();
						while(rs.next())
						{

							JSONObject jsonObject=new JSONObject();
							for(int colCount = 1; colCount <= numberOfColumns; colCount++)
							{

								String columnValue = "";
								String columnName = rsmd.getColumnName(colCount);
								if( rsmd.getColumnType(colCount) == java.sql.Types.DATE )
								{
									java.sql.Date currDate = rs.getDate(colCount);
									columnValue = (currDate != null )? genericUtility.getValidDateString( currDate.toString(), dbDataFormat, applDataFormat ): null;
								}
								else if( rsmd.getColumnType(colCount) == java.sql.Types.TIMESTAMP )//HARD CODE FOR DATE 91
								{

									java.sql.Timestamp currDate = rs.getTimestamp(colCount);
									if (currDate != null)
									{
										columnValue = genericUtility.getValidDateTimeString(currDate.toString(), dbDataFormat, applDataFormat );
									}
								}
								//TODO further processing has to do
								else if( rsmd.getColumnType(colCount) == java.sql.Types.BLOB )
								{
									Blob blobData = rs.getBlob(colCount);

									BaseLogger.log("3", userInfo, null,"For dataModelName["+dataModelName+"] blobData [" + blobData +"]");
								}
								else
								{
									columnValue = rs.getString(colCount);
								}

								jsonObject.put(columnName, columnValue);
								//System.out.println("colCount["+colCount+"]columnName[" + columnName + "]columnValue[" + columnValue + "]");
							}
							
							if( dataModelName.equalsIgnoreCase("customer") )
							{
								System.out.println(" DM == Customer");
								Object custImgObj = jsonObject.get("CUST_NAME");
								if (custImgObj == null)
								{
									//BaseLogger.log("3", userInfo, null,"custImgObj is null");
									custImgObj = "";
								}
								else
								{
									String custImg = checkNull(custImgObj.toString());
									CustomerDetails customerDetail = new CustomerDetails( userInfo );
									imgPath = customerDetail.getCustomerImage(fieldValue.trim(), custImg.trim(),userInfo);
									System.out.println("imgPath is ::: ["+ imgPath +"].... now stroring in jsonObject...");	
									jsonObject.put("img_path", imgPath);
								}
							}
							jsonArray.put(jsonObject);
						}

						returnJsonData = jsonArray.toString();

						if( CommonConstants.CACHE_OPT != 0 )
						{
							infinispanLocalObj.putFormData(dataModelKey, returnJsonData);
						}

						BaseLogger.log("3", userInfo, null,"E12DataModelComponent createDataJson :returnFileData ==>[" + returnJsonData + "]");
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						myCon.close();
						myCon = null;

					}
					catch(Exception e)
					{
						BaseLogger.log("0", null, null,"Exception :E12DataModelComponent :createDataJson in==>\n");
						e.printStackTrace();
						throw e;
					}
					finally
					{
						try
						{
							if (pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							if(myCon != null)
							{
								myCon.close();
								myCon = null;
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							throw e;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			BaseLogger.log("0", null, null,"Exception :E12DataModelComponent :createDataJson out==>\n");
			e.printStackTrace();
			throw e;
		}

		return returnJsonData;
	}

	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input) )
		{
			input= "";
		}
		return input.trim();
	}

}
