package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;

@Stateless
public class PriceListMasterWfEJB extends ValidatorEJB implements PriceListMasterWfEJBLocal, PriceListMasterWfEJBRemote 
{

	@Override
	public String insertPriceListMaster(String objName,String tranId,String transInfoXml,String xtraParams,String entityCode)throws RemoteException, ITMException {
		Connection conn      = null;
		String retString     = "";
		String siteCode      = "";
		E12GenericUtility genericUtility = null;
		Document xmlDataAll  = null;
		MasterStatefulLocal masterStatefulLocal = null;  
		System.out.println("objName: "+tranId+" tranId: "+tranId+" xtraParams: "+xtraParams+" entityCode: "+entityCode+" transInfoXml: "+transInfoXml);
		String returnString = "";
		
	try {
		System.out.println("Inside Try block of PriceListMasterWfEJB.insertPriceListMaster()");
		conn = getConnection();
		conn.setAutoCommit(false);
		AppConnectParm appConnect = new AppConnectParm();
		Properties p = appConnect.getProperty();
		InitialContext ctx = new InitialContext(p);
		genericUtility = new E12GenericUtility();
		//added by Pavan R on 27/DEC/17 userId passed to savData() and processRequest()
		String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		System.out.println("userId::["+userId+"]");
		String [] authencate = new String[2];
		authencate[0]        = userId;
		authencate[1]        = "";
		SimpleDateFormat dt = new SimpleDateFormat(genericUtility.getApplDateFormat());
		
		if (transInfoXml != null && transInfoXml.trim().length() != 0) {
			xmlDataAll = genericUtility.parseString(transInfoXml);
		}
		
		StringBuffer xmlString= new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xmlString.append("<DocumentRoot><description>Datawindow Root</description><group0><description>Group0 escription</description>");
		xmlString.append("<Header0>");
		xmlString.append("<description>Header0 members</description>");
		//enter proper object name/transaction screen name
		xmlString.append("<objName><![CDATA[").append("pricelist_mst_wf").append("]]></objName>");
		xmlString.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
		xmlString.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
		xmlString.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
		xmlString.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
		xmlString.append("<action><![CDATA[").append("SAVE").append("]]></action>");
		xmlString.append("<elementName><![CDATA[").append("").append("]]></elementName>");
		xmlString.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
		xmlString.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
		xmlString.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
		xmlString.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
		xmlString.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
		xmlString.append("<Detail1 dbID='' domID=\"1\" objName=\"pricelist_mst_wf\" objContext=\"1\">");
		//enter primary key column name
		xmlString.append("<attribute pkNames=\"price_list:\" status=\"N\" updateFlag=\"A\" selected=\"N\" />");
		
		
		if(xmlDataAll.getElementsByTagName("Detail1")!=null && xmlDataAll.getElementsByTagName("Detail1").getLength()>0)
		{
				String priceList = checkNull(genericUtility.getColumnValueFromNode("price_list", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				System.out.println("Price List"+priceList);
				String descr = checkNull(genericUtility.getColumnValueFromNode("descr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String listType = checkNull(genericUtility.getColumnValueFromNode("list_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String priceListParent = checkNull(genericUtility.getColumnValueFromNode("price_list__parent", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String orderType = checkNull(genericUtility.getColumnValueFromNode("order_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String manageType = checkNull(genericUtility.getColumnValueFromNode("manage_type", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList1 = checkNull(genericUtility.getColumnValueFromNode("plist_1", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList2 = checkNull(genericUtility.getColumnValueFromNode("plist_2", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList3 = checkNull(genericUtility.getColumnValueFromNode("plist_3", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList4 = checkNull(genericUtility.getColumnValueFromNode("plist_4", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate1Formula = checkNull(genericUtility.getColumnValueFromNode("rate1_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate2Formula = checkNull(genericUtility.getColumnValueFromNode("rate2_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate3Formula = checkNull(genericUtility.getColumnValueFromNode("rate3_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate4Formula = checkNull(genericUtility.getColumnValueFromNode("rate4_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chgDate = checkNull(genericUtility.getColumnValueFromNode("chg_date", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chgUser = checkNull(genericUtility.getColumnValueFromNode("chg_user", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String chgTerm = checkNull(genericUtility.getColumnValueFromNode("chg_term", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList5 = checkNull(genericUtility.getColumnValueFromNode("plist_5", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList6 = checkNull(genericUtility.getColumnValueFromNode("plist_6", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList7 = checkNull(genericUtility.getColumnValueFromNode("plist_7", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList8 = checkNull(genericUtility.getColumnValueFromNode("plist_8", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList9 = checkNull(genericUtility.getColumnValueFromNode("plist_9", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList10 = checkNull(genericUtility.getColumnValueFromNode("plist_10", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList11 = checkNull(genericUtility.getColumnValueFromNode("plist_11", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String pList12 = checkNull(genericUtility.getColumnValueFromNode("plist_12", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate5Formula = checkNull(genericUtility.getColumnValueFromNode("rate5_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate6Formula = checkNull(genericUtility.getColumnValueFromNode("rate6_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate7Formula = checkNull(genericUtility.getColumnValueFromNode("rate7_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate8Formula = checkNull(genericUtility.getColumnValueFromNode("rate8_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate9Formula = checkNull(genericUtility.getColumnValueFromNode("rate9_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate10Formula = checkNull(genericUtility.getColumnValueFromNode("rate10_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate11Formula = checkNull(genericUtility.getColumnValueFromNode("rate11_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String rate12Formula = checkNull(genericUtility.getColumnValueFromNode("rate12_formula", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String calcMethod = checkNull(genericUtility.getColumnValueFromNode("calc_method", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String calcMethodDescr = checkNull(genericUtility.getColumnValueFromNode("calc_method_descr", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				String methodApplicable = checkNull(genericUtility.getColumnValueFromNode("method_applicable", xmlDataAll.getElementsByTagName("Detail1").item(0)));
				
				
				xmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
				xmlString.append("<descr>").append("<![CDATA["+descr+"]]>").append("</descr>");
				xmlString.append("<list_type>").append("<![CDATA["+listType+"]]>").append("</list_type>");
				xmlString.append("<price_list__parent>").append("<![CDATA["+priceListParent+"]]>").append("</price_list__parent>");
				xmlString.append("<order_type>").append("<![CDATA["+orderType+"]]>").append("</order_type>");
				xmlString.append("<manage_type>").append("<![CDATA["+manageType+"]]>").append("</manage_type>");
				xmlString.append("<plist_1>").append("<![CDATA["+pList1+"]]>").append("</plist_1>");
				xmlString.append("<plist_2>").append("<![CDATA["+pList2+"]]>").append("</plist_2>");
				xmlString.append("<plist_3>").append("<![CDATA["+pList3+"]]>").append("</plist_3>");
				xmlString.append("<plist_4>").append("<![CDATA["+pList4+"]]>").append("</plist_4>");
				xmlString.append("<rate1_formula>").append("<![CDATA["+rate1Formula+"]]>").append("</rate1_formula>");
				xmlString.append("<rate2_formula>").append("<![CDATA["+rate2Formula+"]]>").append("</rate2_formula>");
				xmlString.append("<rate3_formula>").append("<![CDATA["+rate3Formula+"]]>").append("</rate3_formula>");
				xmlString.append("<rate4_formula>").append("<![CDATA["+rate4Formula+"]]>").append("</rate4_formula>");
				xmlString.append("<chg_date>").append("<![CDATA["+chgDate+"]]>").append("</chg_date>");
				xmlString.append("<chg_user>").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
				xmlString.append("<chg_term>").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
				xmlString.append("<plist_5>").append("<![CDATA["+pList5+"]]>").append("</plist_5>");
				xmlString.append("<plist_6>").append("<![CDATA["+pList6+"]]>").append("</plist_6>");
				xmlString.append("<plist_7>").append("<![CDATA["+pList7+"]]>").append("</plist_7>");
				xmlString.append("<plist_8>").append("<![CDATA["+pList8+"]]>").append("</plist_8>");
				xmlString.append("<plist_9>").append("<![CDATA["+pList9+"]]>").append("</plist_9>");
				xmlString.append("<plist_10>").append("<![CDATA["+pList10+"]]>").append("</plist_10>");
				xmlString.append("<plist_11>").append("<![CDATA["+pList11+"]]>").append("</plist_11>");
				xmlString.append("<plist_12>").append("<![CDATA["+pList12+"]]>").append("</plist_12>");
				xmlString.append("<rate5_formula>").append("<![CDATA["+rate5Formula+"]]>").append("</rate5_formula>");
				xmlString.append("<rate6_formula>").append("<![CDATA["+rate6Formula+"]]>").append("</rate6_formula>");
				xmlString.append("<rate7_formula>").append("<![CDATA["+rate7Formula+"]]>").append("</rate7_formula>");
				xmlString.append("<rate8_formula>").append("<![CDATA["+rate8Formula+"]]>").append("</rate8_formula>");
				xmlString.append("<rate9_formula>").append("<![CDATA["+rate9Formula+"]]>").append("</rate9_formula>");
				xmlString.append("<rate10_formula>").append("<![CDATA["+rate10Formula+"]]>").append("</rate10_formula>");
				xmlString.append("<rate11_formula>").append("<![CDATA["+rate11Formula+"]]>").append("</rate11_formula>");
				xmlString.append("<rate12_formula>").append("<![CDATA["+rate12Formula+"]]>").append("</rate12_formula>");
				xmlString.append("<calc_method>").append("<![CDATA["+calcMethod+"]]>").append("</calc_method>");
				xmlString.append("<calc_method_descr>").append("<![CDATA["+calcMethodDescr+"]]>").append("</calc_method_descr>");
				xmlString.append("<method_applicable>").append("<![CDATA["+methodApplicable+"]]>").append("</method_applicable>");
				
				xmlString.append("</Detail1>");
				
		}
		
		
		if(xmlDataAll.getElementsByTagName("Detail2")!=null && xmlDataAll.getElementsByTagName("Detail2").getLength()>0)
		{
			int j=1;
			for(int i=0; i<xmlDataAll.getElementsByTagName("Detail2").getLength(); i++)
			{
			
			xmlString.append("<Detail2 dbID=''  objContext=\"2\"  domID='"+j+"' objName=\"pricelist_mst_wf\">");
			
			String priceList = checkNull(genericUtility.getColumnValueFromNode("price_list", xmlDataAll.getElementsByTagName("Detail2").item(0)));
			String lineNo = checkNull(genericUtility.getColumnValueFromNode("line_no", xmlDataAll.getElementsByTagName("Detail2").item(0)));
			String calcMethod = checkNull(genericUtility.getColumnValueFromNode("calc_method", xmlDataAll.getElementsByTagName("Detail2").item(0)));
			String priceListTar = checkNull(genericUtility.getColumnValueFromNode("price_list_tar", xmlDataAll.getElementsByTagName("Detail2").item(0)));
			String priceListParent = checkNull(genericUtility.getColumnValueFromNode("price_list_parent", xmlDataAll.getElementsByTagName("Detail2").item(0)));
			
			xmlString.append("<price_list>").append("<![CDATA["+priceList+"]]>").append("</price_list>");
			xmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
			xmlString.append("<calc_method>").append("<![CDATA["+calcMethod+"]]>").append("</calc_method>");
			xmlString.append("<price_list_tar>").append("<![CDATA["+priceListTar+"]]>").append("</price_list_tar>");
			xmlString.append("<price_list_parent>").append("<![CDATA["+priceListParent+"]]>").append("</price_list_parent>");
			
				xmlString.append("</Detail2>");
				j++;
		}
		}
		
		xmlString.append("</Header0></group0></DocumentRoot>");
		
		try
		{
			System.out.println("Inside nested try block of PriceListMasterWfEJB.insertPriceListMaster()");
			masterStatefulLocal = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local"); 
			retString = masterStatefulLocal.processRequest( authencate, siteCode, true, xmlString.toString(),false,conn);
			if(retString.contains("Success")){
				returnString = "Y";
			}else{
				returnString = "N";
			}
		}
		catch(Exception ee)
		{
			System.out.println("Inside nested catch block of PriceListMasterWfEJB.insertPriceListMaster()");
			ee.printStackTrace();
		}
				
	}
	
	catch(Exception e)
	{
		System.out.println("Inside catch block of PriceListMasterWfEJB.insertPriceListMaster()"+e);
		throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
	}
	
	
	finally{
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	return returnString;
}

	
	private String checkNull(String str) {
		if(str == null){
			str = "";
		}
		else{
			str = str.trim();
		}
		return str;
	}

	@SuppressWarnings("unused")
	private void closePstmtRs(PreparedStatement pStmt, ResultSet rs) {
		if (pStmt != null) {
			try {
				pStmt.close();
			} catch (SQLException localSQLException1) {
			}
			pStmt = null;
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
	}

}