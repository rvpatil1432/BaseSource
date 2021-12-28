/*
 * Author:Varsha V
 * Date:21-02-2019
 * Request ID:D18JMES001 (Free Offer on multiple Products Wizard)
 */
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;


/**
 * Session Bean implementation class EtaWizardPosEJB
 */
@Stateless
public class SchemeDefWizPostSave extends ValidatorEJB implements SchemeDefWizPostSaveRemote,SchemeDefWizPostSaveLocal 
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	DistCommon distComm = new DistCommon();
	ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	DecimalFormat deciFormater = new DecimalFormat("0.00");
    /**
     * Default constructor. 
     */
    public SchemeDefWizPostSave() {  /* TODO Auto-generated constructor stub*/ }

    @Override
	public String postSave(String xmlStringAll, String tranID, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException 
	{
		String retString="";
		Document dom = null; 
		try 
		{
			System.out.println("Inside Post Save Method of SchemeDefWizPostSave!!!!!!\nxmlStringAll::::["+xmlStringAll+"]\ntranID::::["+tranID
					+"]\neditFlag::::["+editFlag+"]\nxtraParams::::["+xtraParams+"]");
			if(xmlStringAll != null && xmlStringAll.trim().length()>0)
	   		{
	   			dom = genericUtility.parseString(xmlStringAll);		
	   		}
			retString = postSave(dom, xtraParams, "", conn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
    public String postSave(Document dom, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
    	System.out.println("Inside Post Save Method !!!!!!");
		System.out.println("SchemeDefWizPostSave xtraParams ["+xtraParams+"] \n forcedFlag["+forcedFlag+"]");
		boolean isLocCon = false, isError = false, deleteFlag;
		String returnString = "", todayDate = "", loginSiteCode = "", userId = "", chgTerm = "", loginEmpCode = "", sql = "";
		ResultSet rs = null, rs3 = null;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null, pstmt3 = null, pstmt4 = null, pstmt5 = null, pstmt6 = null;
		
		String tranId = "", customerCode = "", siteCodeVal = "", effFromStr = "", validUptoStr = "", statusFlag = "", itemCode = "";
		String unit = "", tranIdScheme = "", descr = "", stateCode = "", stanCode = "", countryCode = "";
		Timestamp effFromDate = null, validUptoDate = null;
		double baseQty = 0.0, freeQty = 0.0, chargeDetQty = 0.0, freeDetQty = 0.0;
		int[] updCount = null;
		int noOfRows  = 0, domId = 0, lineNo = 0, lineNoApplDet = 0;
		String refNo="",schemeCode="",validUptoDateStr="";//ref_no added by nandkumar gadkari on 22/08/19
		ArrayList<String> schemeCodeList = new ArrayList<String>();// added by nandkumar gadkari on 22/08/19
		PreparedStatement pstmt7 = null, pstmt8 = null, pstmt9 = null, pstmt10 = null;
		Iterator<String> itr = null;
		int cnt=0,insertCnt=0,updateCnt=0;
		try
		{	
			if(conn == null || conn.isClosed())
			{
				System.out.println("@@Connection is null");
				conn = getConnection();
				isLocCon = true;
			}
			else
			{
				System.out.println("@@Connection is not null");
			}
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
		    userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			System.out.println("loginSiteCode:: ["+loginSiteCode+"] userId :: ["+userId+"] chgTerm:: ["+chgTerm+"] loginEmpCode:: ["+loginEmpCode+"]");
			
			NodeList detail1NodeList =dom.getElementsByTagName("Detail1");
			int detail1NodeListlen = detail1NodeList.getLength();
			System.out.println("detail1NodeListlen ["+detail1NodeListlen+"]");
			
			NodeList detail2NodeList =dom.getElementsByTagName("Detail2");
			int detail2NodeListlen = detail2NodeList.getLength();
			System.out.println("detail2NodeListlen ["+detail2NodeListlen+"]");
			for(int ctrH = 0; ctrH < detail1NodeListlen ; ctrH++)
			{
				NodeList childNodeList = detail1NodeList.item(ctrH).getChildNodes();
				int childNodeListlen = childNodeList.getLength();	
				for(int ctrD = 0; ctrD < childNodeListlen ; ctrD++)
				{					
					Node childNode = childNodeList.item(ctrD);
					
					if(childNode != null && "tran_id".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						tranId = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("tran_id is===="+tranId);
					}
					else if(childNode != null && "cust_code".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						customerCode = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("customerCode is===="+customerCode);
					}
					else if(childNode != null && "site_code".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						siteCodeVal = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("siteCode is===="+siteCodeVal);
					}
					else if(childNode != null && "eff_from".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						effFromStr = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("effFromStr is===="+effFromStr);
						if(effFromStr != null && effFromStr.trim().length() > 0)
						{
							effFromDate = Timestamp.valueOf(genericUtility.getValidDateString(effFromStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+" 00:00:00.0"));
						}
					}
					else if(childNode != null && "valid_upto".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						validUptoStr = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("validUptoStr is===="+validUptoStr);
						if(validUptoStr != null && validUptoStr.trim().length() > 0)
						{
							validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+" 00:00:00.0"));
						}
					}
					else if(childNode != null && "base_quantity".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						baseQty = Double.parseDouble(checkDouble(childNode.getFirstChild().getNodeValue()));
						System.out.println("baseQty is===="+baseQty);
					}
					else if(childNode != null && "free_quantity".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						freeQty = Double.parseDouble(checkDouble(childNode.getFirstChild().getNodeValue()));
						System.out.println("freeQty is===="+freeQty);
					}
					else if(childNode != null && "ref_no".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)//ref_no added by nandkumar gadkari on 22/08/19
					{
						refNo = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("refNo is===="+refNo);
					}
				}
			}
			noOfRows = 0;
			// added by nandkumar gadkari on 22/08/19----------------------------start----------------------
			sql = "SELECT SCHEME_CODE FROM SCHEME_APPLICABILITY WHERE  REF_NO= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,refNo);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				schemeCode = rs.getString(1);	
				if(schemeCode != null && schemeCode.trim().length() > 0)
				{
					schemeCodeList.add(schemeCode.trim());
				}
			}
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
			// added by nandkumar gadkari on 22/08/19----------------------------end------------------------
			sql = "select s.state_code,s.stan_code, st.count_code from site s, state st where "
					+ "s.state_code = st.state_code and site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeVal);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				stateCode = checkNullAndTrim(rs.getString("state_code"));
				stanCode = checkNullAndTrim(rs.getString("stan_code"));
				countryCode = checkNullAndTrim(rs.getString("count_code"));
			}
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
			
			sql = "insert into bom(bom_code,descr,type,chg_date,chg_user,chg_term,batch_qty,"
					+ "scheme_flag,unit,min_qty,apply_price,price_var,usage_type,max_qty,"
					+ "batch_value,min_batch_value,max_batch_value,ref_no) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; //ref_no added by nandkumar gadkari on 22/08/19
			pstmt1 = conn.prepareStatement(sql);
			
			sql = "insert into bomdet(bom_code,line_no,item_code,item_ref,eff_from,valid_upto,req_type,"
					+ "app_min_qty,app_max_qty,nature,qty_per,min_qty,crit_item,auto_ord,eff_cost,value_per,"
					+ "app_min_value,app_max_value,min_value,round,round_to) "
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt2 = conn.prepareStatement(sql);
			
			sql = "select unit, descr from item where item_code = ?";
			pstmt3 = conn.prepareStatement(sql);
			
			sql = "insert into scheme_applicability(scheme_code,item_code,chg_date,chg_user,chg_term,slab_on,"
					+ "app_from,valid_upto,apply_cust_list,min_value,max_value,grace_days,prod_sch,ref_no) " //ref_no added by nandkumar gadkari on 22/08/19
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt4 = conn.prepareStatement(sql);
			
			sql = "insert into scheme_applicability_det(scheme_code,line_no,site_code,state_code,count_code,"
					+ "stan_code) values(?,?,?,?,?,?)";
			pstmt5 = conn.prepareStatement(sql);
			
			sql = "update scheme_def_det set scheme_code = ? ,ref_no = ? where tran_id = ? and item_code = ?";//ref_no added by nandkumar gadkari on 22/08/19
			pstmt6 = conn.prepareStatement(sql);
			//update added by nandkumar gadkari on 22/08/19---------------------------------start----------------------
			sql = "update bom set descr = ? ,batch_qty = ? ,min_qty= ? where bom_code = ? ";
			pstmt7 = conn.prepareStatement(sql);
			
			sql = "update bomdet set qty_per = ?  where bom_code = ? and nature = ?";
			pstmt8 = conn.prepareStatement(sql);
			
			sql = "update bomdet set qty_per = ? ,min_qty = ? where bom_code = ? and nature = ?";
			pstmt9 = conn.prepareStatement(sql);
			//update added by nandkumar gadkari on 22/08/19----------------------------------end-----------------------
			
			
			for(int ctrH = 0; ctrH < detail2NodeListlen ; ctrH++)
			{
				deleteFlag = false;
				lineNo = 0;
				lineNo++;
				lineNoApplDet = 0;
				lineNoApplDet++;
				domId++;
				NodeList childNodeList = detail2NodeList.item(ctrH).getChildNodes();
				int childNodeListlen = childNodeList.getLength();	
				//scheme_code added by nandkumar gadkari on 22/08/19--START--
				schemeCode="";
				freeDetQty=0;
				chargeDetQty=0;
				itemCode="";
				//scheme_code added by nandkumar gadkari on 22/08/19--EBD---
				for(int ctrD = 0; ctrD < childNodeListlen ; ctrD++)
				{					
					Node childNode = childNodeList.item(ctrD);
					if("attribute".equalsIgnoreCase(childNode.getNodeName()))
					{
						statusFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
						System.out.println("Post Save Status for Detail2["+statusFlag+"]");
						
						if("D".equalsIgnoreCase(statusFlag))
						{
							deleteFlag = true;
							break;
						}
					}
					else if(childNode != null && "item_code".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						itemCode = checkNullAndTrim(childNode.getFirstChild().getNodeValue());
						System.out.println("item_code is===="+itemCode);
						pstmt3.setString(1, itemCode);
						rs3 = pstmt3.executeQuery();
						if(rs3.next())
						{
							unit = checkNullAndTrim(rs3.getString("unit"));
							descr = checkNullAndTrim(rs3.getString("descr"));
						}
						if(rs3 != null)
						{
							rs3.close();
							rs3 = null;
						}
						pstmt3.clearParameters();
					}
					else if(childNode != null && "chargeable_qty".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						chargeDetQty = Double.parseDouble(checkDouble(childNode.getFirstChild().getNodeValue()));
						System.out.println("chargeable_qty is===="+chargeDetQty);
					}
					else if(childNode != null && "free_qty".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)
					{
						freeDetQty = Double.parseDouble(checkDouble(childNode.getFirstChild().getNodeValue()));
						System.out.println("free_qty is===="+freeDetQty);
					}
					else if(childNode != null && "scheme_code".equalsIgnoreCase(childNode.getNodeName()) && childNode.getFirstChild() != null)//scheme_code added by nandkumar gadkari on 22/08/19
					{
						schemeCode = (checkNullAndTrim(childNode.getFirstChild().getNodeValue()));
						System.out.println("scheme_code is===="+schemeCode);
					}
					
				}
				System.out.println("schemeCode ::::["+schemeCode+"]" );
				System.out.println("freeQty ::::["+freeDetQty+"]" );
				System.out.println("baseQty ::::["+chargeDetQty+"]" );
				System.out.println("itemCode ::::["+itemCode+"]" );
				
				if(schemeCode.trim().length() == 0)//scheme_code condition added by nandkumar gadkari on 22/08/19
				{
					tranIdScheme = generateTranID(xtraParams, "w_bom", conn);
					System.out.println("Tran ID for Scheme Definition :::["+tranId+"]");
					System.out.println("Tran ID for Scheme BOM :::["+tranIdScheme+"]");
					if(tranIdScheme == null || tranIdScheme.trim().length() == 0)
					{
						returnString = itmDBAccessLocal.getErrorString("","VTTRANID","","",conn);
					}
					pstmt1.setString(1, tranIdScheme);
					pstmt1.setString(2, chargeDetQty+"+"+freeDetQty);
					pstmt1.setString(3, "S");
					pstmt1.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					pstmt1.setString(5, userId);
					pstmt1.setString(6, chgTerm);
					pstmt1.setDouble(7, chargeDetQty);
					pstmt1.setString(8, "Q");
					pstmt1.setString(9, unit);
					pstmt1.setDouble(10, chargeDetQty);
					pstmt1.setString(11, "P");
					pstmt1.setString(12, "A");
					pstmt1.setString(13, "A");
					pstmt1.setLong(14, 99999999999L);
					pstmt1.setInt(15, 0);
					pstmt1.setInt(16, 0);
					pstmt1.setInt(17, 9999999);				
					pstmt1.setString(18, refNo);	//ref_no added by nandkumar gadkari on 22/08/19
					pstmt1.addBatch();
					pstmt1.clearParameters();
					
					pstmt2.setString(1, tranIdScheme);
					pstmt2.setInt(2, lineNo);
					pstmt2.setString(3, itemCode);
					pstmt2.setString(4, itemCode+"C");
					pstmt2.setTimestamp(5, effFromDate);
					pstmt2.setTimestamp(6, validUptoDate);
					pstmt2.setString(7, "P");
					//Commented and added below line by Varsha V to set app_min_qty in bomdet as 0
					//pstmt2.setDouble(8, chargeDetQty);
					pstmt2.setInt(8, 1);
					pstmt2.setInt(9, 99999999);
					pstmt2.setString(10, "C");
					pstmt2.setDouble(11, chargeDetQty);
					pstmt2.setDouble(12, 0);
					pstmt2.setString(13, "N");
					pstmt2.setString(14, "N");
					pstmt2.setDouble(15, 0);
					pstmt2.setDouble(16, 0);
					pstmt2.setDouble(17, 0);
					pstmt2.setDouble(18, 0);
					pstmt2.setDouble(19, 0);
					pstmt2.setString(20, "R");
					pstmt2.setDouble(21, 0.001);
					pstmt2.addBatch();
					pstmt2.clearParameters();
					
					lineNo++;
					pstmt2.setString(1, tranIdScheme);
					pstmt2.setInt(2, lineNo);
					pstmt2.setString(3, itemCode);
					pstmt2.setString(4, itemCode+"F");
					pstmt2.setTimestamp(5, effFromDate);
					pstmt2.setTimestamp(6, validUptoDate);
					pstmt2.setString(7, "P");
					//pstmt2.setDouble(8, freeDetQty); commented and quantity 0 set  by nandkumar gadkari on 25/07/19
					pstmt2.setDouble(8, 1);
					pstmt2.setInt(9, 99999999);
					pstmt2.setString(10, "F");
					pstmt2.setDouble(11, freeDetQty);
					pstmt2.setDouble(12, freeDetQty);
					pstmt2.setString(13, "N");
					pstmt2.setString(14, "N");
					pstmt2.setDouble(15, 0);
					pstmt2.setDouble(16, 0);
					pstmt2.setDouble(17, 0);
					pstmt2.setDouble(18, 0);
					pstmt2.setDouble(19, 0);
					pstmt2.setString(20, "R");
					pstmt2.setDouble(21, 0.001);
					pstmt2.addBatch();
					pstmt2.clearParameters();
					
					pstmt4.setString(1, tranIdScheme);//0.00 0.00 0 N
					pstmt4.setString(2, itemCode);
					pstmt4.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					pstmt4.setString(4, userId);
					pstmt4.setString(5, chgTerm);
					pstmt4.setString(6, "N");
					pstmt4.setTimestamp(7, effFromDate);
					pstmt4.setTimestamp(8, validUptoDate);
					pstmt4.setString(9, customerCode);
					pstmt4.setDouble(10, 0.00);
					pstmt4.setDouble(11, 0.00);
					pstmt4.setInt(12, 0);
					pstmt4.setString(13, "N");
					pstmt4.setString(14, refNo);//ref_no added by nandkumar gadkari on 22/08/19
					
					pstmt4.addBatch();
					pstmt4.clearParameters();
					
					pstmt5.setString(1, tranIdScheme);
					pstmt5.setInt(2, lineNoApplDet);
					pstmt5.setString(3, siteCodeVal);
					/*pstmt5.setString(4, stateCode);
					pstmt5.setString(5, countryCode);
					pstmt5.setString(6, stanCode);*/ //commented and added by nandkumar gadkari on 03/01/20
					pstmt5.setString(4, null);
					pstmt5.setString(5, null);
					pstmt5.setString(6, null);
					
					pstmt5.addBatch();
					pstmt5.clearParameters();
					
					pstmt6.setString(1, tranIdScheme);
					pstmt6.setString(2, refNo);//ref_no added by nandkumar gadkari on 22/08/19
					pstmt6.setString(3, tranId);
					pstmt6.setString(4, itemCode);
					
					pstmt6.addBatch();
					pstmt6.clearParameters();
					
					insertCnt++;
				}
				else//added by nandkumar gadkari on 22/08/19
				{
					schemeCodeList.remove(schemeCode);
					
					pstmt7.setString(1, chargeDetQty+"+"+freeDetQty);
					pstmt7.setDouble(2, chargeDetQty);
					pstmt7.setDouble(3, chargeDetQty);
					pstmt7.setString(4, schemeCode);
					
					pstmt7.addBatch();
					pstmt7.clearParameters();
					
					pstmt8.setDouble(1, chargeDetQty);
					pstmt8.setString(2, schemeCode);
					pstmt8.setString(3, "C");
					
					pstmt8.addBatch();
					pstmt8.clearParameters();
				
					pstmt9.setDouble(1, freeDetQty);
					pstmt9.setDouble(2, freeDetQty);
					pstmt9.setString(3, schemeCode);
					pstmt9.setString(4, "F");
					
					pstmt9.addBatch();
					pstmt9.clearParameters();
					
					updateCnt++;
					
				}
			}
			
			if(insertCnt > 0)
			{
				if(pstmt1 != null)
				{
					updCount = null;
					updCount = pstmt1.executeBatch();
					System.out.println("Update count on BOM insert::"+updCount);
					pstmt1.close();
					pstmt1 = null;
				}
				if(pstmt3 != null)
				{
					pstmt3.close();
					pstmt3 = null;
				}
				if(pstmt2 != null && lineNo > 0)
				{
					updCount = null;
					updCount = pstmt2.executeBatch();
					System.out.println("Update count on BOMDET insert::"+updCount);
					pstmt2.close();
					pstmt2 = null;
				}
				if(pstmt4 != null)
				{
					updCount = null;
					updCount = pstmt4.executeBatch();
					System.out.println("Update count on SCHEME_APPLICABILITY insert::"+updCount);
					pstmt4.close();
					pstmt4 = null;
				}
				if(pstmt5 != null && lineNoApplDet > 0)
				{
					updCount = null;
					updCount = pstmt5.executeBatch();
					System.out.println("Update count on SCHEME_APPLICABILITY_DET insert::"+updCount);
					pstmt5.close();
					pstmt5 = null;
				}
				if(pstmt6 != null)
				{
					updCount = null;
					updCount = pstmt6.executeBatch();
					System.out.println("Update count on SCHEME_DEF_DET update::"+updCount);
					pstmt6.close();
					pstmt6 = null;
				}
			}
			//added by nandkumar gadkari on 22/08/19-------------------START------------------------
			if(updateCnt > 0)
			{
				if(pstmt7 != null)
				{
					updCount = null;
					updCount = pstmt7.executeBatch();
					System.out.println("Update count on BOM update::"+updCount);
					pstmt7.close();
					pstmt7 = null;
				}
				if(pstmt8 != null)
				{
					updCount = null;
					updCount = pstmt8.executeBatch();
					System.out.println("Update count on BOMDET CHARGABLE update::"+updCount);
					pstmt8.close();
					pstmt8 = null;
				}
				if(pstmt9 != null)
				{
					updCount = null;
					updCount = pstmt9.executeBatch();
					System.out.println("Update count on BOMDET FREE update::"+updCount);
					pstmt9.close();
					pstmt9 = null;
				}
			}
			cnt =schemeCodeList.size();
			System.out.println("^^^^^^^schemeCodeList schemeCodeList["+schemeCodeList);
			
			if(cnt> 0)
			{
				SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(genericUtility.getApplDateFormat()); 
				validUptoDateStr=simpleDateFormatObj.format(yesterday());
				validUptoDate=Timestamp.valueOf(genericUtility.getValidDateString(validUptoDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+" 00:00:00"));
				sql = "update scheme_applicability set VALID_UPTO = ? ,REF_NO ='' where SCHEME_CODE = ? ";
				pstmt9 = conn.prepareStatement(sql);
				itr = schemeCodeList.iterator();
				while(itr.hasNext())
				{
					schemeCode =(String) itr.next();
					pstmt9.setTimestamp(1, validUptoDate);
					pstmt9.setString(2, schemeCode);
					
					pstmt9.addBatch();
					pstmt9.clearParameters();
					
				}
				if(pstmt9 != null)
				{
					updCount = null;
					updCount = pstmt9.executeBatch();
					System.out.println("Update count on scheme_applicability  update::"+updCount);
					pstmt9.close();
					pstmt9 = null;
				}
			}
			//added by nandkumar gadkari on 22/08/19-------------------END-------------------
			
			
		}
		catch(Exception e)
		{
			System.out.println("Exception :: SchemeDefWizPostSave :: postSave method"+e);
			e.printStackTrace();
			throw new ITMException(e);
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
				if ( rs3 != null )
				{
					rs3.close();
					rs3 = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt3 != null)
				{
					pstmt3.close();
					pstmt3 = null;
				}
				if(pstmt2 != null)
				{
					pstmt2.close();
					pstmt2 = null;
				}
				if(pstmt4 != null)
				{
					pstmt4.close();
					pstmt4 = null;
				}
				if(pstmt5 != null)
				{
					pstmt5.close();
					pstmt5 = null;
				}
				if(pstmt6 != null)
				{
					pstmt6.close();
					pstmt6 = null;
				}
				if(isLocCon)
				{
					if(isError)
					{
						System.out.println("Inside rollbacking....");
						conn.rollback();
					}
					else
					{
						System.out.println("Inside committing....");
						conn.commit();
					}
					if (conn != null )
					{
						conn.close();conn = null;
					}
				}
			}
			catch( Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("returnString at the end of SchemeDefWizPostSave::::["+returnString+"]");
		return returnString;
	}
	
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}//end of method checkNullAndTrim
	
	private static String checkDouble(String input)
	{
		if (input==null || input.trim().length() == 0)
		{
			input="0";
		}
		return input.trim();
	}
	
	
	private  String getError(String errorstr,String Code,Connection conn)  throws ITMException, Exception
    {
        String mainStr ="";
        
        try
        {
        	String errString = "";
        	errString =  new ITMDBAccessEJB().getErrorString("",Code,"","",conn);
			String begPart = errString.substring(0,errString.indexOf("<message>")+9);
			String endDesc = errString.substring(errString.indexOf("</description>"));
			mainStr= begPart+"Invalid Data"+"</message><description>";
			mainStr= mainStr+"Process Failed ["+errorstr+""+endDesc;
			System.out.println("mainStr:::::::::::::::::: "+mainStr);
			begPart = null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ITMException(e);
        }
        return mainStr;
    }//end of method getError
	
	private String generateTranID(String xtraParams, String tranWindow, Connection conn) throws ITMException
	{
		String sprsTravelTranID = "";
		PreparedStatement pStmt = null;
		ResultSet rSet = null;
		String refSer="";
		String tranIdCol="";
		String keyString="", loginSiteCode= "", loginEmpCode = "";
		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		
		try
		{
			String transSql = "SELECT REF_SER,TRAN_ID_COL,KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW  = ?";

			pStmt = conn.prepareStatement(transSql);
			pStmt.setString(1, tranWindow);
			rSet = pStmt.executeQuery();
			
			if( rSet.next() )
			{
				refSer = (rSet.getString("REF_SER") == null) ?"":rSet.getString("REF_SER").trim();
				tranIdCol = (rSet.getString("TRAN_ID_COL") == null) ?"":rSet.getString("TRAN_ID_COL").trim();
				keyString = (rSet.getString("KEY_STRING") == null) ?"":rSet.getString("KEY_STRING").trim();
			}
			if( rSet != null )
			{
				rSet.close();
				rSet.close();
			}
			if( pStmt != null )
			{
				pStmt.close();
				pStmt.close();
			}
			System.out.println("refSer["+refSer+"]tranIdCol["+tranIdCol+"]keyString["+keyString+"]");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginEmpCode" );
			TransIDGenerator tranIDGenerator = new TransIDGenerator("<Root><Detail1><appl_date></appl_date><site_code>" + loginSiteCode+ 
					"</site_code>" + "<sub_type>" + "FRESH" + "</sub_type></Detail1></Root>", getValueFromXTRA_PARAMS(xtraParams, "loginCode"), CommonConstants.DB_NAME);
			sprsTravelTranID = tranIDGenerator.generateTranSeqID( refSer,tranIdCol,keyString,conn) ;
		}
		catch( Exception exp )
		{
			System.out.println("Exception In generateTranID......");
			exp.printStackTrace();
			throw new ITMException(exp); //Added By Mukesh Chauhan on 09/08/19
		}
		finally
		{
			try
			{
				if( rSet != null )
				{
					rSet.close();
					rSet.close();
				}
				if( pStmt != null )
				{
					pStmt.close();
					pStmt.close();
				}
			}
			catch( Exception expRsc )
			{
				expRsc.printStackTrace();
			}
		}
		return sprsTravelTranID;
	}
	public java.util.Date yesterday() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
}
