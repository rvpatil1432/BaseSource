/********************************************************
	Title : PreturnRefData
	Date  : 08/04/11
	Author: vpatil
********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import org.w3c.dom.*;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class PReturnAct extends ActionHandlerEJB implements PReturnActLocal, PReturnActRemote
{
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	UtilMethods utilMethods = UtilMethods.getInstance();
	
	public String actionHandler(String actionType, String xmlString,String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		
		Document dom = null;
		Document dom1 = null;
		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1); 
			}
		
			System.out.println("actionType:"+actionType);
			if (actionType.equalsIgnoreCase("REFDATA"))
			{
				resString = importRefData(dom,dom1,objContext,xtraParams);
			}
			
			
		}
	   	catch(Exception e)
		{
			
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from action actionHandler"+resString);
	    return (resString);
	}
	
	private String importRefData(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String sql = "",errString = "",userId = "";
		String smfgDate = "", sexpiryDate  = "",sretestDate = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String refTranId = "";
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		
		try
		{
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			refTranId = genericUtility.getColumnValue("tran_id__ref",dom1);
			System.out.println("Tran Refernce = " + refTranId);
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ); 
			if(refTranId==null||refTranId.trim().length()==0)
			{
				
					errString = itmDBAccessEJB.getErrorString( "", "TRID_REFNL", userId ,"",conn);
					System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
					return errString;
				
			}
			// Commented by Mahesh Saggam on 13 June 2019
			/*sql="select line_no,purc_order,line_no__ord,item_code,quantity,unit,rate,"+   
				"discount,tax_amt,net_amt,loc_code,lot_no,lot_sl,canc_bo,tax_class,"+   
				"tax_chap,tax_env,acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr,"+   
				"unit__rate,conv__qty_stduom,conv__rtuom_stduom,unit__std,quantity__stduom,"+   
				"rate__stduom,pack_code,no_art,pack_instr,batch_no,mfg_date,expiry_date,"+   
				"status,vouch_qty,gross_weight,tare_weight,net_weight,potency_perc,supp_code__mnfr,"+   
				"site_code__mfg,reas_code,remarks,challan_qty,grade,specific_instr,"+   
				"special_instr,loc_code__excess_short,excess_short_qty,additional_cost,"+
				"rate__clg,supp_challan_qty,realised_qty,item_code__mfg,spec_ref,std_rate,"+
				"dept_code,effect_stock,physical_status,benefit_type,licence_no,"+   
				"acct_code__prov_dr,cctr_code__prov_dr,acct_code__prov_cr,cctr_code__prov_cr,"+   
				"form_no,retest_date,duty_paid,batch_size,sample_qty,damage_qty from porcpdet "+ 
				" where tran_id=?";*/
			
			// Added by Mahesh Saggam on 13 June 2019 [Start]
			sql = "SELECT porcpdet.line_no as line_no, porcpdet.purc_order, porcpdet.line_no__ord,porcpdet.item_code,porcpdet.quantity,porcpdet.unit,porcpdet.rate,"+
			"porcpdet.discount,porcpdet.tax_amt,porcpdet.net_amt,porcpdet.loc_code,porcpdet.lot_no,porcpdet.lot_sl,porcpdet.canc_bo,porcpdet.tax_class,porcpdet.tax_chap,"+
			"porcpdet.tax_env,porcpdet.acct_code__dr,porcpdet.cctr_code__dr,porcpdet.acct_code__cr,porcpdet.cctr_code__cr,porcpdet.unit__rate,porcpdet.conv__qty_stduom,"+
			"porcpdet.conv__rtuom_stduom,porcpdet.unit__std,porcpdet.quantity__stduom,porcpdet.rate__stduom,porcpdet.pack_code,porcpdet.no_art,porcpdet.pack_instr,porcpdet.batch_no,"+
			"porcpdet.mfg_date,porcpdet.expiry_date,porcpdet.status,porcpdet.vouch_qty,porcpdet.gross_weight,porcpdet.tare_weight,porcpdet.net_weight,porcpdet.potency_perc,"+
			"porcpdet.supp_code__mnfr,porcpdet.site_code__mfg,porcpdet.reas_code,porcpdet.remarks,porcpdet.challan_qty,porcpdet.grade,porcpdet.specific_instr,porcpdet.special_instr,"+
			"porcpdet.loc_code__excess_short,porcpdet.excess_short_qty,porcpdet.additional_cost,porcpdet.rate__clg,porcpdet.supp_challan_qty,porcpdet.realised_qty,"+
			"porcpdet.item_code__mfg,porcpdet.spec_ref,porcpdet.std_rate,porcpdet.dept_code,porcpdet.effect_stock,porcpdet.physical_status,porcpdet.benefit_type,porcpdet.licence_no,"+
			"porcpdet.acct_code__prov_dr,porcpdet.cctr_code__prov_dr,porcpdet.acct_code__prov_cr,porcpdet.cctr_code__prov_cr,porcpdet.form_no,porcpdet.retest_date,porcpdet.duty_paid,"+
			"porcpdet.batch_size,porcpdet.sample_qty,porcpdet.damage_qty ,A.descr"+
			" FROM porcpdet ,item A"+
			" WHERE porcpdet.item_code = A.item_code"+
			" AND tran_id = ?";
			// Added by Mahesh Saggam on 13 June 2019  [End]
			
			
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, refTranId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<line_no isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("line_no"))+ "]]>").append("</line_no>");
				valueXmlString.append("<purc_order isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("purc_order"))+ "]]>").append("</purc_order>");
				valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("line_no__ord"))+ "]]>").append("</line_no__ord>");
				//Added by Mahesh Saggam on 13-June-2019 [Start]
				valueXmlString.append("<line_no__rcp isSrvCallOnChg='0'>").append("<![CDATA[" + rs.getString("line_no") + "]]>").append("</line_no__rcp>");
				valueXmlString.append("<tran_id__rcp isSrvCallOnChg='0'>").append("<![CDATA[" + refTranId + "]]>").append("</tran_id__rcp>");
				valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[" + rs.getString("descr") + "]]>").append("</item_descr>");
				//Added by Mahesh Saggam on 13-June-2019 [End]
				valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("item_code"))+ "]]>").append("</item_code>");
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("quantity")+ "]]>").append("</quantity>");
				valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("unit"))+ "]]>").append("</unit>");
				valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("rate")+ "]]>").append("</rate>");
				
				valueXmlString.append("<discount isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("discount")+ "]]>").append("</discount>");
				valueXmlString.append("<tax_amt isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("tax_amt")+ "]]>").append("</tax_amt>");
				valueXmlString.append("<net_amt isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("net_amt")+ "]]>").append("</net_amt>");
				valueXmlString.append("<loc_code>").append("<![CDATA[" +checkNull(rs.getString("loc_code"))+ "]]>").append("</loc_code>");
				valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("lot_no"))+ "]]>").append("</lot_no>");
				valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("lot_sl"))+ "]]>").append("</lot_sl>");
				valueXmlString.append("<canc_bo isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("canc_bo"))+ "]]>").append("</canc_bo>");
				valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("tax_class"))+ "]]>").append("</tax_class>");
				
				valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("tax_chap"))+ "]]>").append("</tax_chap>");
				valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("tax_env"))+ "]]>").append("</tax_env>");
				valueXmlString.append("<acct_code__dr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("acct_code__dr"))+ "]]>").append("</acct_code__dr>");
				valueXmlString.append("<cctr_code__dr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("cctr_code__dr"))+ "]]>").append("</cctr_code__dr>");
				valueXmlString.append("<acct_code__cr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("acct_code__cr"))+ "]]>").append("</acct_code__cr>");
				valueXmlString.append("<cctr_code__cr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("cctr_code__cr"))+ "]]>").append("</cctr_code__cr>");
				
				valueXmlString.append("<unit__rate isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("unit__rate"))+ "]]>").append("</unit__rate>");
				valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("conv__qty_stduom")+ "]]>").append("</conv__qty_stduom>");
				valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("conv__rtuom_stduom")+ "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("unit__std"))+ "]]>").append("</unit__std>");
				valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("quantity__stduom")+ "]]>").append("</quantity__stduom>");
				
				valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("rate__stduom")+ "]]>").append("</rate__stduom>");
				valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("pack_code"))+ "]]>").append("</pack_code>");
				valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("no_art")+ "]]>").append("</no_art>");
				valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("pack_instr"))+ "]]>").append("</pack_instr>");
				valueXmlString.append("<batch_no isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("batch_no"))+ "]]>").append("</batch_no>");
				smfgDate = (rs.getTimestamp("mfg_date")!=null)?(sdf.format(rs.getTimestamp("mfg_date")).toString()):"";
				sexpiryDate =(rs.getTimestamp("expiry_date")!=null)?(sdf.format(rs.getTimestamp("expiry_date")).toString()):"";
				if (smfgDate != null && smfgDate.trim().length() > 0)
				{
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[" +smfgDate+ "]]>").append("</mfg_date>");
				}
				else
				{
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'/>");
				}

				if (sexpiryDate != null && sexpiryDate.trim().length() > 0)
				{
					valueXmlString.append("<expiry_date isSrvCallOnChg='0'>").append("<![CDATA[" +sexpiryDate+ "]]>").append("</expiry_date>");
				}
				else
				{
					valueXmlString.append("<expiry_date isSrvCallOnChg='0'/>");
				}
				//valueXmlString.append("<status>").append("<![CDATA[" +checkNull(rs.getString("status"))+ "]]>").append("</status>");
				//valueXmlString.append("<vouch_qty>").append("<![CDATA[" +rs.getDouble("vouch_qty")+ "]]>").append("</vouch_qty>");
				valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("gross_weight")+ "]]>").append("</gross_weight>");
				valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("tare_weight")+ "]]>").append("</tare_weight>");
				valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("net_weight")+ "]]>").append("</net_weight>");
				valueXmlString.append("<potency_perc isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("potency_perc")+ "]]>").append("</potency_perc>");
				valueXmlString.append("<supp_code__mnfr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("supp_code__mnfr"))+ "]]>").append("</supp_code__mnfr>");
				
				valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("site_code__mfg"))+ "]]>").append("</site_code__mfg>");
				valueXmlString.append("<reas_code isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("reas_code"))+ "]]>").append("</reas_code>");
				valueXmlString.append("<remarks isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("remarks"))+ "]]>").append("</remarks>");
				valueXmlString.append("<challan_qty isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("challan_qty")+ "]]>").append("</challan_qty>");
				valueXmlString.append("<grade isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("grade"))+ "]]>").append("</grade>");
				valueXmlString.append("<specific_instr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("specific_instr"))+ "]]>").append("</specific_instr>");
	
				valueXmlString.append("<special_instr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("special_instr"))+ "]]>").append("</special_instr>");
				valueXmlString.append("<loc_code__excess_short isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("loc_code__excess_short"))+ "]]>").append("</loc_code__excess_short>");
				valueXmlString.append("<excess_short_qty isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("excess_short_qty")+ "]]>").append("</excess_short_qty>");
				valueXmlString.append("<additional_cost isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("additional_cost")+ "]]>").append("</additional_cost>");
				
				valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("rate__clg")+ "]]>").append("</rate__clg>");
				valueXmlString.append("<supp_challan_qty isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("supp_challan_qty")+ "]]>").append("</supp_challan_qty>");
				valueXmlString.append("<realised_qty isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("realised_qty")+ "]]>").append("</realised_qty>");
				valueXmlString.append("<item_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("item_code__mfg"))+ "]]>").append("</item_code__mfg>");
				valueXmlString.append("<spec_ref isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("spec_ref"))+ "]]>").append("</spec_ref>");
				valueXmlString.append("<std_rate isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("std_rate")+ "]]>").append("</std_rate>");
				
				valueXmlString.append("<dept_code isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("dept_code"))+ "]]>").append("</dept_code>");
				valueXmlString.append("<effect_stock isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("effect_stock"))+ "]]>").append("</effect_stock>");
				valueXmlString.append("<physical_status isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("physical_status"))+ "]]>").append("</physical_status>");
				valueXmlString.append("<benefit_type isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("benefit_type"))+ "]]>").append("</benefit_type>");
				valueXmlString.append("<licence_no isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("licence_no"))+ "]]>").append("</licence_no>");
				
				valueXmlString.append("<acct_code__prov_dr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("acct_code__prov_dr"))+ "]]>").append("</acct_code__prov_dr>");
				valueXmlString.append("<cctr_code__prov_dr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("cctr_code__prov_dr"))+ "]]>").append("</cctr_code__prov_dr>");
				valueXmlString.append("<acct_code__prov_cr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("acct_code__prov_cr"))+ "]]>").append("</acct_code__prov_cr>");
				valueXmlString.append("<cctr_code__prov_cr isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("cctr_code__prov_cr"))+ "]]>").append("</cctr_code__prov_cr>");
				valueXmlString.append("<form_no isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("form_no"))+ "]]>").append("</form_no>");
				
				sretestDate = (rs.getTimestamp("retest_date")!=null)?(sdf.format(rs.getTimestamp("retest_date")).toString()):"";
				if (sretestDate != null && sretestDate.trim().length() > 0)
				{
					valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[" +sretestDate+ "]]>").append("</retest_date>");
				}
				else
				{
					valueXmlString.append("<retest_date isSrvCallOnChg='0'/>");
				}
				valueXmlString.append("<duty_paid isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("duty_paid"))+ "]]>").append("</duty_paid>");
				valueXmlString.append("<batch_size isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("batch_size"))+ "]]>").append("</batch_size>");
				valueXmlString.append("<sample_qty isSrvCallOnChg='0'>").append("<![CDATA[" +checkNull(rs.getString("sample_qty"))+ "]]>").append("</sample_qty>");
				valueXmlString.append("<damage_qty isSrvCallOnChg='0'>").append("<![CDATA[" +rs.getDouble("damage_qty")+ "]]>").append("</damage_qty>");
				
				valueXmlString.append("</Detail>\r\n");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			valueXmlString.append("</Root>\r\n");			
		
		}
		catch(SQLException e)
		{
			System.out.println("Exception :SQLException in default :getDATA " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : in default : getDATA" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					System.out.println("Closing Connection........");
					conn.close();
					conn = null;
				}
			}
			catch(SQLException se)
			{
				throw new ITMException(se);
			}
		}
		System.out.println("valueXmlString.toString() :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	private String checkNull(String inputString)
	{
		return (inputString==null)?"":inputString;
	}
	
}
