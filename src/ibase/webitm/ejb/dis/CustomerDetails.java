package ibase.webitm.ejb.dis;

import ibase.utility.CommonConstants;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.util.gst.AESEncryption;
import ibase.webitm.util.gst.GSPSignature;
import ibase.webitm.utility.ITMException;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;


public class CustomerDetails extends ValidatorEJB
{
	//Added by sarita on 02nd JAN 2018 to ser userInfo [start]
	public CustomerDetails(UserInfoBean userInfoBean)
	{
		setUserInfo(userInfoBean);
	}
	//Added by sarita on 02nd JAN 2018 to ser userInfo [end]
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList getCustomerDetails(String custCode) 
	{
		String sql = "";
		Connection conn = null;
		//added by Varsha V on 14-09-18 to seperate subqueries
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		//Ended by Varsha V on 14-09-18 to seperate subqueries
		ArrayList custList = new ArrayList();
		boolean isExists = false;
		String totalOrdersTillDate = "", openOrders = "",gstin="";
		//added by nandkumar gadkari on 25-02-20
		SimpleDateFormat gspTokenTimeFormatter = null;
		FinCommon finCommon =null;
		AESEncryption aesEncryption =null;
		String gspTokenTimeStampFormat="",appKey="",gspPrivateKeyName="",privateKeyPath="",clientId="",BASEURL="",timeStamp="",gspAuthString="",gspAuthSignature="";
		GSPSignature	gspSignature =null;
		PrivateKey aspPrivateKey = null;
		String gstrIRNdata="";
		byte[] respJsonInBytes=null;
		JSONObject gstIRNObj =null,object=null,gstIRNObjAdd=null,gstIRNObjPadd=null;
		try 
		{
			conn = getConnection();
			
			/*sql = "SELECT  CUSTOMER.CUST_NAME ,  CUSTOMER.CURR_CODE,(SELECT COUNT(*) FROM SORDER WHERE CUST_CODE = ? AND ORDER_DATE <= SYSDATE) AS TOTAL_ORDER_TILL_DATE, "
			+ "  CONTACT.TELE1  AS CONTACT, CONTACT.NAME AS CONTACT_NAME, CONTACT.EMAIL_ADDR , "
			+ " (SELECT COUNT(*) FROM SORDER SORDER, DESPATCH DESPATCH WHERE SORDER.CUST_CODE = ? AND SORDER.STATUS NOT IN ('C','X','H')  "
				 + "	AND DESPATCH.SORD_NO = SORDER.SALE_ORDER AND DESPATCH.CONFIRMED = 'N') AS OPENORDERS, CUSTOMER.CUST_PRIORITY AS RANK, "
			     + "     CASE WHEN CRTERM.DESCR  IS NULL THEN CUSTOMER.CR_TERM ELSE CRTERM.DESCR END AS CRTERM_DESC ,CASE WHEN DELIVERY_TERM.DESCR IS NULL THEN CUSTOMER.DLV_TERM ELSE DELIVERY_TERM.DESCR END "
			     + "    AS DLVTERM_DESC, SALES_PERS.SP_NAME , SALES_PERS.EMAIL_ADDR AS SALES_EMAIL, CUSTOMER.BLACK_LISTED, 		CUSTOMER.BLACK_LISTED_DATE, (SELECT FN_OUVERDUE_CHECK(?) FROM DUAL) AS DUE_AMOUNT_TILL_DATE, CUSTOMER.CREDIT_LMT, CUSTOMER.WAVE_TYPE  "
				 + "	FROM CUSTOMER  CUSTOMER, CONTACT CONTACT ,CRTERM CRTERM, DELIVERY_TERM DELIVERY_TERM, SALES_PERS SALES_PERS "
				 + "	WHERE CUSTOMER.CONTACT_CODE = CONTACT.CONTACT_CODE "
			     + "	AND CUSTOMER.CR_TERM = CRTERM.CR_TERM(+) "
				 + "	AND CUSTOMER.DLV_TERM = DELIVERY_TERM.DLV_TERM(+) "
			     + "	AND SALES_PERS.SALES_PERS = CUSTOMER.SALES_PERS(+) "
				 + "	AND CUSTOMER.CUST_CODE = ? " ;*/
			sql="SELECT CUSTOMER.CUST_NAME , " +
					"  CUSTOMER.CURR_CODE, " +
					// commented and changed by Varsha V on 12-09-18
					//"  (SELECT COUNT(*) " +
					//Commented by Varsha V on 14-09-18 to seperate subqueries
					//"  (SELECT COUNT(1) " +
					//"  FROM SORDER " +
					//"  WHERE CUST_CODE = ? " +
					//Commented by Santosh on 24/03/2017
					/*"  AND ORDER_DATE <= SYSDATE " +*/
					//"  )             AS TOTAL_ORDER_TILL_DATE, " +
					//End by Varsha V on 14-09-18 to seperate subqueries
					"  CONTACT.TELE1 AS CONTACT, " +
					"  CONTACT.NAME  AS CONTACT_NAME, " +
					"  CONTACT.EMAIL_ADDR , " +
					// commented and changed by Varsha V on 12-09-18
					//"  (SELECT COUNT(*) " +
					//Commented by Varsha V on 14-09-18 to seperate subqueries
					//"  (SELECT COUNT(1) " +
					//Commented by Santosh on 24/03/2017
					/*"  FROM SORDER SORDER, " +
					"    DESPATCH DESPATCH " +*/
					//"  FROM SORDER SORDER " +
					//"  WHERE SORDER.CUST_CODE = ? " +
					//"  AND SORDER.STATUS NOT IN ('C','X','H') " +
					//Commented by Santosh on 24/03/2017
					/*"  AND DESPATCH.SORD_NO   = SORDER.SALE_ORDER " +
					"  AND DESPATCH.CONFIRMED = 'N' " +*/
					//"  )                      AS OPENORDERS, " +
					//End by Varsha V on 14-09-18 to seperate subqueries
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
					"  (SELECT FN_OUVERDUE_CHECK(?) FROM DUAL " +
					"  ) AS DUE_AMOUNT_TILL_DATE, " +
					"  CUSTOMER.CREDIT_LMT, " +
					"  CUSTOMER.WAVE_TYPE, " +
					"  (SELECT FN_PAYAMT_CHECK(?) FROM DUAL " +
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
					" AND CUSTOMER.CUST_CODE      = ?";
			pstmt = conn.prepareStatement(sql);
			// 07-sep-2020 manoharan  used with multiple queries before closing so taken inside
			/*sql = "select count(SALE_ORDER) as TOTAL_ORDER_TILL_DATE , "
					+ " count(case when status not in ('C','X','H') then SALE_ORDER else null end) as OPENORDERS "
					+ " from sorder where cust_code = ?";
			pstmt1 = conn.prepareStatement(sql);
			*/
			
			pstmt.setString(1, checkNull(custCode));
			pstmt.setString(2, checkNull(custCode));
			pstmt.setString(3, checkNull(custCode));
			//Commented by Varsha V on 14-09-18 to seperate subqueries
			//pstmt.setString(4, checkNull(custCode));
			//pstmt.setString(5, checkNull(custCode));
			//End by Varsha V on 14-09-18 to seperate subqueries

			rs = pstmt.executeQuery();
			if ( rs.next())
			{
				custList.add(checkNull(rs.getString("CUST_NAME")));
				custList.add(checkNull(rs.getString("CURR_CODE")));
				//Added by Varsha V on 14-09-18 to seperate subqueries
				sql = "select count(SALE_ORDER) as TOTAL_ORDER_TILL_DATE , "
				+ " count(case when status not in ('C','X','H') then SALE_ORDER else null end) as OPENORDERS "
				+ " from sorder where cust_code = ?";
				pstmt1 = conn.prepareStatement(sql);
		
		
				pstmt1.setString(1, checkNull(custCode));
				rs1 = pstmt1.executeQuery();
				if ( rs1.next())
				{
					totalOrdersTillDate = checkNull(rs1.getString("TOTAL_ORDER_TILL_DATE"));
					openOrders			= checkNull(rs1.getString("OPENORDERS"));
				}
				if (pstmt1 != null) {
					pstmt1.close();
					pstmt1 = null;
				}
				if (rs1 != null) 
				{					
					rs1.close();
					rs1= null;
				}
				
				custList.add(totalOrdersTillDate);
				//Ended by Varsha V on 14-09-18 to seperate subqueries
				custList.add(checkNull(rs.getString("CONTACT")));
				custList.add(checkNull(rs.getString("EMAIL_ADDR")));
				custList.add(openOrders);
				custList.add(checkNull(rs.getString("RANK")));
				custList.add(checkNull(rs.getString("CRTERM_DESC")));
				custList.add(checkNull(rs.getString("DLVTERM_DESC")));
				custList.add(checkNull(rs.getString("SP_NAME")));
				custList.add(checkNull(rs.getString("SALES_EMAIL")));
				custList.add(checkNull(rs.getString("BLACK_LISTED")));
				custList.add(rs.getDate("BLACK_LISTED_DATE")== null?"":rs.getDate("BLACK_LISTED_DATE"));
				System.out.println("rs.getDate(BLACK_LISTED_DATE) ::::: "+rs.getDate("BLACK_LISTED_DATE"));
				custList.add(checkNull(rs.getString("DUE_AMOUNT_TILL_DATE")));
				System.out.println("Due Amount");
				custList.add(checkNull(rs.getString("CREDIT_LMT")));
				custList.add(checkNull(rs.getString("CONTACT_NAME")));
				custList.add(checkNull(rs.getString("WAVE_TYPE")));
				custList.add(checkNull(rs.getString("PAY_AMOUNT")));
				custList.add(checkNull(rs.getString("GST_TAX_REG")));
				
				gstin=rs.getString("GST_TAX_REG");
				
				if(gstin != null && gstin.trim().length() > 0)
				{
					finCommon = new FinCommon();
					aesEncryption = new AESEncryption();

					gspTokenTimeStampFormat = finCommon.getFinparams("999999", "GSP_TKN_TIMSTMP_FRMT", conn);
					gspTokenTimeFormatter = new SimpleDateFormat(gspTokenTimeStampFormat);

					appKey = UUID.randomUUID().toString().replaceAll("-", "");

					appKey = appKey.substring(appKey.length() - 32, appKey.length());

					gspPrivateKeyName = finCommon.getFinparams("999999", "GSP_PRI_KEY_NAME", conn);
					gspSignature = new GSPSignature();
					privateKeyPath = CommonConstants.JBOSSHOME + File.separator + "server" + File.separator
							+ "default/deploy/ibase.ear/ibase.war/webitm/resource/gst" + File.separator
							+ gspPrivateKeyName + ".pem";
					clientId = finCommon.getFinparams("999999", "GST_CLIENT_ID", conn);

					aspPrivateKey = gspSignature.loadPrivateKey(new FileInputStream(privateKeyPath));

					sql = "SELECT SERVICE_CODE, SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE METHOD_NAME = 'EINVOICE_API_URL'";
					pstmt1 = conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					while (rs1.next()) {
						switch (rs1.getString("SERVICE_CODE")) {
						case "gstin_detail_url": {
							BASEURL = rs1.getString("SERVICE_URI");
						}
							break;

						}
					}
					if (pstmt1 != null) {
						pstmt1.close();
						pstmt1 = null;
					}
					if (rs1 != null) {
						rs1.close();
						rs1 = null;
					}
					timeStamp = gspTokenTimeFormatter.format(new Date());
					gspAuthString = "v2.0::" + clientId + ":" + appKey + ":" + timeStamp + ":" + gstin + ":";
					gspAuthSignature = gspSignature.sign(gspAuthString, aspPrivateKey);

					HttpRequest genIRNReq = Unirest.get(BASEURL + gstin)
							.header("Content-Type", "application/json")
							.header("Gstin", gstin)
							.header("X-Asp-Auth-Token", gspAuthString)
							.header("X-Asp-Auth-Signature", gspAuthSignature)
							.getHttpRequest();

					System.out.println("authtoken url[" + genIRNReq.getUrl() + "]");
					System.out.println("authtoken method[" + genIRNReq.getHttpMethod() + "]");
					System.out.println("authtoken request header[" + genIRNReq.getHeaders() + "]");
					
					HttpResponse<JsonNode> genIRNRes = genIRNReq.asJson();
					System.out.println(String.format("authTokenResp Request : Status[%s] Response[%s]",
							genIRNRes.getStatus(), genIRNRes.getBody()));
					if (genIRNRes.getStatus() == 200) {

						 object = genIRNRes.getBody().getObject();

						
						if (object.has("data")) {

							 gstrIRNdata = object.getString("data");

							
							respJsonInBytes = aesEncryption.decodeBase64StringTOByte(gstrIRNdata);

							gstIRNObj = new JSONObject(new String(respJsonInBytes));

							//System.out.println("rgdt response:" + gstIRNObj.getString("rgdt"));
							if(gstIRNObj.has("rgdt"))
							{
								custList.add(gstIRNObj.getString("rgdt"));//19
							}
							else
							{
								custList.add("");
							}
							//System.out.println("tradeNam response:" + gstIRNObj.getString("tradeNam"));
							if(gstIRNObj.has("tradeNam"))
							{
								custList.add(gstIRNObj.getString("tradeNam"));//20
							}
							else
							{
								custList.add("");
							}
							//System.out.println("sts response:" + gstIRNObj.getString("sts"));
							if(gstIRNObj.has("sts"))
							{
								custList.add(gstIRNObj.getString("sts"));//21
							}
							else
							{
								custList.add("");
							}
							//System.out.println("dty response:" + gstIRNObj.getString("dty"));
							if(gstIRNObj.has("dty"))
							{
								custList.add(gstIRNObj.getString("dty"));//22
							}
							else
							{
								custList.add("");
							}
							
							
							
							if(gstIRNObj.has("pradr"))
							{
								gstIRNObjPadd = new JSONObject();
								gstIRNObjPadd=gstIRNObj.getJSONObject("pradr");
								
								if(gstIRNObjPadd.has("addr"))
								{
									gstIRNObjAdd = gstIRNObjPadd.getJSONObject("addr");
									custList.add(gstIRNObjAdd.getString("stcd"));//23
								}
								else
								{
									custList.add("");
								}
								
							}
							else
							{
								custList.add("");
							}
							
						}
						else
						{
							custList.add("");
							custList.add("");
							custList.add("");
							custList.add("");
							custList.add("");
						}
					}
					else
					{
						custList.add("");
						custList.add("");
						custList.add("");
						custList.add("");
						custList.add("");
					}
				}
				else
				{
					custList.add("");
					custList.add("");
					custList.add("");
					custList.add("");
					custList.add("");
				}
				
				
				
				isExists = true;
			}
			if(!isExists)
			{
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				//added by nandkumar gadkari on 25-02-20
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
				custList.add("");
			}
			if (rs != null) 
			{					
				rs.close();
				rs = null;
			}
			if (pstmt != null ) 
			{					
				pstmt.close();
				pstmt = null;
			}
			if (pstmt1 != null ) 
			{					
				pstmt1.close();
				pstmt1 = null;
			}
		}
		catch (Exception e) 
		{
			System.out.println(" Exception in CustomerDetails.getCustomerDetails()["+e.getMessage()+"]");
			e.printStackTrace();
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
				if (pstmt != null ) 
				{					
					pstmt.close();
					pstmt = null;
				}
				if (rs1 != null) 
				{					
					rs1.close();
					rs1= null;
				}
				if (pstmt1 != null ) 
				{					
					pstmt1.close();
					pstmt1 = null;
				}
				if (conn != null ) 
				{					
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("Exception in CustomerDetails.getCustomerDetails()");
				e.printStackTrace();
			}
		}
		System.out.println("return String :" + custList);
		return custList;
	}
	//Added and replace by sarita on 2nd JAN 2018
	//public String getCustomerImage(String custCode, String custName) throws ITMException
	public String getCustomerImage(String custCode, String custName,UserInfoBean userInfoBean) throws ITMException
	{
		String custImg = "";
		try
		{
			//Changed by Santosh on 24/03/2017
			/*WavegenWizEJB wavegenWizEJB = new WavegenWizEJB();
			
			custImg = wavegenWizEJB.getCustomImagePath(custCode, custName, "customer");*/
			DistUtility distUtility = new DistUtility();
			//Added and replace by sarita on 2nd JAN 2018
			//custImg = distUtility.getImagePath("w_customer", custCode, custName, "customer", null);
			custImg = distUtility.getImagePath("w_customer", custCode, custName, "customer", null,userInfoBean);
			System.out.println("ItemListBean.getItemListDetails()["+custImg+"]");
		}
		catch (Exception e)
		{
			throw new ITMException(e);
		}
		return custImg;
	}
	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input.trim();
	}

}
