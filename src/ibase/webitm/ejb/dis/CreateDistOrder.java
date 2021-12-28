// Name of Developer : Nisar
package ibase.webitm.ejb.dis;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.*;
import org.omg.CORBA.ORB;
import org.w3c.dom.*;
import java.util.Properties;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreateDistOrder
{
	CommonConstants commonConstants = new CommonConstants();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	HashMap tagChkMapForDetail1 = new HashMap();
	HashMap tagChkMapForDetail2 = new HashMap();
	MaterialtransferBean materialtransferobj = new MaterialtransferBean();
	Connection conn = null;
	String currCode = null;
	String tranDate = null;
	StringBuffer msgString = null;

	public CreateDistOrder()
	{
	}
	//public String createDistributionOrder(String siteCode,HashMap siteCodeSuppList,ArrayList arList,String xtraParams, Connection conn)throws RemoteException,ITMException
	public String createDistributionOrder(String siteCode,Vector siteCodeSuppList,ArrayList arList,String xtraParams,String workorder, Connection conn)throws Exception,ITMException
	{//TO CREATE DIST ORDER FOR EACH SUPPLIER SITE
		//arList contains collection of materialtransferobject of type MaterialtransferBean
		String retString = null;
		String siteCodeSupp = null;
		System.out.println("[CreateDistOrder] ----------createDistributionOrder() method is invocked------------");
		MaterialtransferBean materialtransferobj = null;
		msgString = new StringBuffer( "" );
		
		try
		{
			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				conn = connDriver.getConnectDB("DriverITM");
				connDriver = null;
			}
			//Iterator iter =  (siteCodeSuppList.keySet()).iterator();
			for (int tCtr = 0 ; tCtr < siteCodeSuppList.size() ; tCtr++ )
			{
				System.out.println("[manohar] tCtr--------->[" + tCtr + "][" + siteCodeSuppList.get(tCtr) + "]");

			}

			//while(iter.hasNext())
			for (int siteCtr =0 ; siteCtr < siteCodeSuppList.size() ; siteCtr++ )
			{
				//siteCodeSupp = iter.next().toString();
				siteCodeSupp = (String) siteCodeSuppList.get(siteCtr);
				System.out.println("[CreateDistOrder] Distribution order created for site--------->[" + siteCtr + "][" +siteCodeSupp + "]");
				HashMap itemDetailMap = new HashMap();
				for(int ctr = 0; ctr < arList.size();  ctr++)
				{
					materialtransferobj = (MaterialtransferBean)arList.get(ctr);

					HashMap siteQtyMap = materialtransferobj.getSiteQtyMap();

					if(siteQtyMap.containsKey(siteCodeSupp))
					{
						itemDetailMap.put(materialtransferobj.getItemCode(),siteQtyMap.get(siteCodeSupp));
						System.out.println("Item [" + materialtransferobj.getItemCode() + "]["+ siteQtyMap.get(siteCodeSupp) + "]");
						//break;
					}
				}//end of for
				retString = createDistributionOrder(siteCode,siteCodeSupp,itemDetailMap,conn,workorder,xtraParams);
				if (retString == null || retString.trim().length() ==0 || retString.indexOf("Success") > -1 )
				{
					System.out.println("Distribution order Generated Successfully");

				}
				else
				{
					System.out.println("Distribution order NOT Generated");
					retString="ERROR";
				}
			}//end of 	while(iter.hasNext())
			// Changes made to show in message which distribution order nos are generated.--- start
			retString = retString + msgString.toString();
			// Changes made to show in message which distribution order nos are generated.--- end
		}
		catch(Exception e)
		{
			e.printStackTrace();
			retString="ERROR";
			//throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (tagChkMapForDetail1 != null)
				{
					tagChkMapForDetail1 = null;
				}
				if (tagChkMapForDetail2 != null)
				{
					tagChkMapForDetail2 = null;
				}
			}
			catch(Exception e)
			{
		       e.printStackTrace();
		       throw new ITMException(e); 
			}
			
		}

		return retString;
	}

	public String createDistributionOrder(String siteCode,String siteCodeSupp,HashMap hmap,Connection conn,String workorder, String xtraParams)throws RemoteException,ITMException
	{
		StringBuffer xmlBuff = new StringBuffer();
		String fileName = null;
		String xmlString = null,retString  = null,transMode = null;
		String saleOrder = "",taxEnv= "",taxChap="",taxClass="",itemCode = "",autoReceipt ="";
		String sundryType = "",sundryCode = "",orderType = "",status = "",distRoute="",priceList = "";
		String descr = "",unit="",sql = null,locCodeGit ="",locCodeGitbf ="",avaliableYN ="";
		double demandQty= 0,supplyQty  = 0,balQty = 0;
		double rate = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			System.out.println("-------------------------createDistributionOrder() method called-----------------------------");

			//SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//tranDate = sdf.format(new Timestamp(System.currentTimeMillis()).toString());
			tranDate = getCurrdateAppFormat() ;

			fileName = commonConstants.JBOSSHOME+"\\resource\\MATERIAL_TRANSFER_TEMPLATE\\dist_order.xml";
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(fileName);

			retString = setToCheckTagExistance(document,"1");//this method is used to check default value is defined for field or not for detail1
			retString = setToCheckTagExistance(document,"2");//this method is used to check default value is defined for field or not for detail2
			System.out.println("tagChkMapForDetail1..........."+tagChkMapForDetail1);
			if("ERROR".equals(retString))
			{
				return retString;
			}

			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("dist_order").append("]]></objName>");
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
			xmlBuff.append("<description>").append("Header0 members").append("</description>");

			orderType = (String)tagChkMapForDetail1.get("order_type");
			sql = "select loc_code__git,loc_code__gitbf,AVALIABLE_YN ,AUTO_RECEIPT from distorder_type where tran_type = '"+orderType+"' ";
			//System.out.println("sql ------------------"+sql);
			//pstmt = new BasePreparedStatement(conn,sql);
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1,orderType);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				locCodeGit = rs.getString("loc_code__git")==null?"":rs.getString("loc_code__git").trim();
				locCodeGitbf = rs.getString("loc_code__gitbf")==null?"":rs.getString("loc_code__gitbf").trim();
				avaliableYN = rs.getString("AVALIABLE_YN")==null?"N":rs.getString("AVALIABLE_YN").trim();
				autoReceipt = rs.getString("AUTO_RECEIPT")==null?"N":rs.getString("AUTO_RECEIPT").trim();
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			/////////////////////////////////////////////////////////////
			// 13/05/08 manoharan to get curr_code from site
			sql = "select a.curr_code from finent a, site b where a.fin_entity = b.fin_entity and b.site_code = '"+siteCodeSupp+"' ";
			//System.out.println("sql ------------------"+sql);
			//pstmt = new BasePreparedStatement(conn,sql);
			pstmt= conn.prepareStatement(sql);
		//	pstmt.setString(1,siteCodeSupp);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				this.currCode = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			/////////////////////////////////////////////////////////////
			orderType = orderType == null ? "" :orderType;
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_order\" objContext=\"1\">");
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<dist_order/>");
			xmlBuff.append("<order_date><![CDATA["+tranDate+"]]></order_date>");
			xmlBuff.append("<site_code__ship><![CDATA[").append((tagChkMapForDetail1.containsKey("site_code__ship")?tagChkMapForDetail1.get("site_code__ship"):siteCodeSupp)).append("]]></site_code__ship>");
			xmlBuff.append("<site_code__dlv><![CDATA[").append((tagChkMapForDetail1.containsKey("site_code__dlv")?tagChkMapForDetail1.get("site_code__dlv"):siteCode)).append("]]></site_code__dlv>");
			xmlBuff.append("<ship_date><![CDATA["+tranDate+"]]></ship_date>");
			xmlBuff.append("<due_date><![CDATA["+tranDate+"]]></due_date>");
			xmlBuff.append("<remarks/>");
			xmlBuff.append("<dist_route><![CDATA[").append((tagChkMapForDetail1.containsKey("dist_route")?tagChkMapForDetail1.get("dist_route"):distRoute)).append("]]></dist_route>");
			xmlBuff.append("<price_list><![CDATA[").append((tagChkMapForDetail1.containsKey("price_list")?tagChkMapForDetail1.get("price_list"):priceList)).append("]]></price_list>");
			xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
			xmlBuff.append("<chg_user><![CDATA[BASE      ]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA[07BASE221      ]]></chg_term>");
			xmlBuff.append("<target_wgt><![CDATA[0]]></target_wgt>");
			xmlBuff.append("<target_vol><![CDATA[0]]></target_vol>");
			xmlBuff.append("<loc_code__git><![CDATA[").append((tagChkMapForDetail1.containsKey("loc_code__git")?tagChkMapForDetail1.get("loc_code__git"):locCodeGit)).append("]]></loc_code__git>");
			//xmlBuff.append("<loc_code__git/>");
			xmlBuff.append("<chg_date><![CDATA["+tranDate+"]]></chg_date>");
			xmlBuff.append("<site_from_descr/>");
			xmlBuff.append("<site_to_descr/>");
			xmlBuff.append("<location_descr/>");
			xmlBuff.append("<conf_date><![CDATA["+tranDate+"]]></conf_date>");
			xmlBuff.append("<site_code><![CDATA["+siteCode+"]]></site_code>");
			xmlBuff.append("<status><![CDATA[").append((tagChkMapForDetail1.containsKey("status")?tagChkMapForDetail1.get("status"):priceList)).append("]]></status>");
			xmlBuff.append("<sale_order><![CDATA[").append((tagChkMapForDetail1.containsKey("sale_order")?tagChkMapForDetail1.get("sale_order"):saleOrder)).append("]]></sale_order>");
			xmlBuff.append("<remarks1/>");
			xmlBuff.append("<remarks2/>");
			xmlBuff.append("<order_type><![CDATA[").append((tagChkMapForDetail1.containsKey("order_type")?tagChkMapForDetail1.get("order_type"):orderType)).append("]]></order_type>");
			xmlBuff.append("<a_site_add1/>");
			xmlBuff.append("<a_site_add2/>");
			xmlBuff.append("<a_site_city/>");
			xmlBuff.append("<a_site_pin/>");
			xmlBuff.append("<a_site_state_code/>");
			xmlBuff.append("<b_site_add1/>");
			xmlBuff.append("<b_site_add2/>");
			xmlBuff.append("<b_site_city/>");
			xmlBuff.append("<b_site_pin/>");
			xmlBuff.append("<b_site_state_code/>");
			xmlBuff.append("<loc_code__cons/>");
			xmlBuff.append("<sundry_type><![CDATA[").append((tagChkMapForDetail1.containsKey("sundry_type")?tagChkMapForDetail1.get("sundry_type"):sundryType)).append("]]></sundry_type>");
			xmlBuff.append("<sundry_code><![CDATA[").append((tagChkMapForDetail1.containsKey("sundry_code")?tagChkMapForDetail1.get("sundry_code"):sundryCode)).append("]]></sundry_code>");
			//xmlBuff.append("<auto_receipt><![CDATA[Y]]></auto_receipt>");
			xmlBuff.append("<auto_receipt><![CDATA[").append(autoReceipt).append("]]></auto_receipt>");
			xmlBuff.append("<tran_type><![CDATA[IT]]></tran_type>");
			xmlBuff.append("<curr_code><![CDATA[" + this.currCode + "]]></curr_code>");
			xmlBuff.append("<exch_rate><![CDATA[1]]></exch_rate>");
			xmlBuff.append("<sales_pers><![CDATA[]]></sales_pers>");
			xmlBuff.append("<sales_pers_sp_name><![CDATA[]]></sales_pers_sp_name>");
			xmlBuff.append("<loc_code__gitbf><![CDATA[").append((tagChkMapForDetail1.containsKey("loc_code__gitbf")?tagChkMapForDetail1.get("loc_code__gitbf"):locCodeGitbf)).append("]]></loc_code__gitbf>");
			//xmlBuff.append("<loc_code__gitbf><![CDATA[]]></loc_code__gitbf>");
			xmlBuff.append("<cust_code__dlv><![CDATA[]]></cust_code__dlv>");
			xmlBuff.append("<dlv_to></dlv_to>");
			xmlBuff.append("<dlv_add1><![CDATA[]]></dlv_add1>");
			xmlBuff.append("<dlv_add2><![CDATA[]]></dlv_add2>");
			xmlBuff.append("<dlv_add3><![CDATA[]]></dlv_add3>");
			xmlBuff.append("<dlv_city><![CDATA[]]></dlv_city>");
			xmlBuff.append("<state_code__dlv><![CDATA[]]></state_code__dlv>");
			xmlBuff.append("<count_code__dlv><![CDATA[]]></count_code__dlv>");
			xmlBuff.append("<dlv_pin><![CDATA[]]></dlv_pin>");
			xmlBuff.append("<stan_code><![CDATA[]]></stan_code>");
			xmlBuff.append("<tel1__dlv><![CDATA[]]></tel1__dlv>");
			xmlBuff.append("<tel2__dlv><![CDATA[]]></tel2__dlv>");
			xmlBuff.append("<tel3__dlv><![CDATA[]]></tel3__dlv>");
			xmlBuff.append("<fax__dlv><![CDATA[]]></fax__dlv>");
			xmlBuff.append("<avaliable_yn><![CDATA[").append(avaliableYN).append("]]></avaliable_yn>");
			//xmlBuff.append("<avaliable_yn><![CDATA[Y]]></avaliable_yn>");
			xmlBuff.append("<purc_order><![CDATA[]]></purc_order>");
			xmlBuff.append("<tot_amt><![CDATA[0]]></tot_amt>");
			xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
			xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
			xmlBuff.append("<tran_ser><![CDATA[]]></tran_ser>");
			xmlBuff.append("<price_list__clg><![CDATA[]]></price_list__clg>");
			xmlBuff.append("<loc><![CDATA[                         ]]></loc>");
			xmlBuff.append("<sundry_name><![CDATA[]]></sundry_name>");
			xmlBuff.append("<proj_code><![CDATA[]]></proj_code>");
			xmlBuff.append("<descr/>");
			xmlBuff.append("<policy_no><![CDATA[]]></policy_no>");
			xmlBuff.append("<loc_code__damaged><![CDATA[]]></loc_code__damaged>");
			xmlBuff.append("<site_code__bil><![CDATA[]]></site_code__bil>");
			xmlBuff.append("<d_site_descr><![CDATA[]]></d_site_descr>");
			xmlBuff.append("<d_site_add1><![CDATA[]]></d_site_add1>");
			xmlBuff.append("<d_site_add2><![CDATA[]]></d_site_add2>");
			xmlBuff.append("<d_site_city><![CDATA[]]></d_site_city>");
			xmlBuff.append("<d_site_pin><![CDATA[]]></d_site_pin>");
			xmlBuff.append("<d_site_state_code><![CDATA[]]></d_site_state_code>");
			xmlBuff.append("<trans_mode><![CDATA[").append((tagChkMapForDetail1.containsKey("trans_mode")?tagChkMapForDetail1.get("trans_mode"):transMode)).append("]]></trans_mode>");

			Set sethdr	=tagChkMapForDetail1.entrySet();
			Iterator  iterator = sethdr.iterator() ;
			while(iterator.hasNext())
				{
					Map.Entry me = (Map.Entry)iterator.next();
					xmlBuff.append("<"+me.getKey()+"><![CDATA["+me.getValue()+"]]></"+me.getKey()+">");
				}

			xmlBuff.append("</Detail1>");


			Iterator iter =  (hmap.keySet()).iterator();

			int ctr = 0;
			while(iter.hasNext())
			{
				ctr ++;
				itemCode = iter.next().toString();
				balQty = Double.parseDouble(hmap.get(itemCode).toString());
				System.out.println("balQty......................."+balQty);
				sql="SELECT  ITEM_CODE , DESCR,  UNIT , SALE_RATE , TAX_CHAP , TAX_CLASS  FROM ITEM where item_code= '"+itemCode+"' ";
				//System.out.println("========sql=>"+sql);
				//pstmt = new BasePreparedStatement(conn,sql);
				pstmt= conn.prepareStatement(sql);
				//pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("DESCR")==null?"":rs.getString("DESCR").trim();
					unit = rs.getString("UNIT")==null?"":rs.getString("UNIT");
					rate = rs.getDouble("SALE_RATE");
					taxChap = rs.getString("TAX_CHAP")==null?"":rs.getString("TAX_CHAP");
					taxClass = rs.getString("TAX_CLASS")==null?"":rs.getString("TAX_CLASS");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"dist_order\" objContext=\"2\">");
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<dist_order/>");
				xmlBuff.append("<line_no>"+(ctr)+"</line_no>");
				xmlBuff.append("<tran_id__demand><![CDATA[]]></tran_id__demand>");
				xmlBuff.append("<item_code><![CDATA[").append((tagChkMapForDetail2.containsKey("item_code")?tagChkMapForDetail1.get("item_code"):itemCode)).append("]]></item_code>");
				xmlBuff.append("<qty_order><![CDATA["+balQty+"]]></qty_order>");
				xmlBuff.append("<qty_confirm><![CDATA["+balQty+"]]></qty_confirm>");
				xmlBuff.append("<qty_received><![CDATA["+0+"]]></qty_received>");
				xmlBuff.append("<qty_shipped><![CDATA["+0+"]]></qty_shipped>");
				xmlBuff.append("<due_date><![CDATA["+tranDate+"]]></due_date>");
				xmlBuff.append("<tax_class><![CDATA[").append((tagChkMapForDetail2.containsKey("tax_class")?tagChkMapForDetail1.get("tax_class"):taxClass)).append("]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA[").append((tagChkMapForDetail2.containsKey("tax_chap")?tagChkMapForDetail1.get("tax_chap"):taxChap)).append("]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA[").append((tagChkMapForDetail2.containsKey("tax_env")?tagChkMapForDetail1.get("tax_env"):taxEnv)).append("]]></tax_env>");
				xmlBuff.append("<unit><![CDATA["+unit+"]]></unit>");
				xmlBuff.append("<item_descr><![CDATA["+descr+"]]></item_descr>");
				xmlBuff.append("<sale_order><![CDATA[").append((tagChkMapForDetail2.containsKey("sale_order")?tagChkMapForDetail1.get("sale_order"):saleOrder)).append("]]></sale_order>");
				xmlBuff.append("<line_no__sord/>");
				xmlBuff.append("<rate><![CDATA["+rate+"]]></rate>");
				xmlBuff.append("<qty_return><![CDATA[0]]></qty_return>");
				xmlBuff.append("<rate__clg><![CDATA[0]]></rate__clg>");
				xmlBuff.append("<discount><![CDATA[0]]></discount>");
				xmlBuff.append("<remarks><![CDATA[]]></remarks>");
				xmlBuff.append("<tot_amt><![CDATA[0]]></tot_amt>");
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
				xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
				xmlBuff.append("<rate__clg><![CDATA[0]]></rate__clg>");
				xmlBuff.append("<qty_alloc><![CDATA[0]]></qty_alloc>");
				xmlBuff.append("<over_ship_perc><![CDATA[0]]></over_ship_perc>");
				//xmlBuff.append("<qty_details><![CDATA[]]></qty_details>");
				xmlBuff.append("<unit__alt><![CDATA["+unit+"]]></unit__alt>");
				xmlBuff.append("<conv__qty__alt><![CDATA[0]]></conv__qty__alt>");
				xmlBuff.append("<qty_order__alt><![CDATA[0]]></qty_order__alt>");
				xmlBuff.append("<ship_date><![CDATA["+tranDate+"]]></ship_date>");
				xmlBuff.append("<work_order><![CDATA["+workorder+"]]></work_order>");
				xmlBuff.append("<pack_instr><![CDATA[]]></pack_instr>");

				iterator = null;
				Set setdet	=tagChkMapForDetail2.entrySet();
				iterator = setdet.iterator() ;
				while(iterator.hasNext())
				{
					Map.Entry medet = (Map.Entry)iterator.next();
					xmlBuff.append("<"+medet.getKey()+"><![CDATA["+( medet.getValue() == null ? "" : medet.getValue() )+"]]></"+medet.getKey()+">");
				}
				xmlBuff.append("</Detail2>");

				// Changes done due to multiple ditribution orders creation on pressing of 
				//Process button multiple times.(transfer_status column has been added in workorder table)
				//START
				sql = "update workorder set transfer_status = 'Y' where work_order = ? ";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1, workorder);
				int wrkOrdupd =0;
				wrkOrdupd = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				// Changes done due to multiple ditribution orders creation on pressing of 
				//Process button multiple times.(transfer_status column has been added in workorder table)
				//END	

			}//end of while loop

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");

			xmlString = xmlBuff.toString();
			retString = saveData(siteCode,xmlString,xtraParams,conn);//xtraParams added  by Nandkumar Gadkari on 04/07/18 

			if (retString.indexOf("Success") > -1)
			{
				int  d =retString.indexOf("<TranID>");
				int f =	 retString.indexOf("</TranID>");
				String distOrder = retString.substring(d+8,f);
				System.out.println("distOrder......... "+distOrder);
				String empCodeAprv = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
				sql = "update distorder set confirmed = 'Y',conf_date = ? ,emp_code__aprv = ? where dist_order = '"+distOrder+"' ";
				//System.out.println("SQL : "+sql);
				//pstmt = new BasePreparedStatement(conn,sql);
				pstmt= conn.prepareStatement(sql);
				pstmt.setTimestamp(1,new Timestamp(System.currentTimeMillis()));
				pstmt.setString(2,empCodeAprv);
			//	pstmt.setString(3,distOrder);
				int upd =0;
				upd = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("distorder Confirm :: "+upd);
				System.out.println("Distribution order Generated Successfully");
				// Changes made to show in message which distribution order nos are generated.--- start
				msgString.append( "Distribution order No -->"+distOrder+" For Workorder No -->"+workorder+"\n" );
				// Changes made to show in message which distribution order nos are generated.--- end
				retString = (new CreateDistIssue()).createDistIssue(siteCode,retString,xtraParams,conn);
			}
			else
			{
				System.out.println("Distribution order not Generated ..... ");
				//throw new Exception ("Distribution order not Generated ");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			retString="ERROR";
			//throw new ITMException(e);
		}
		System.out.println("retString  create dist order :: "+retString);
		return retString;
	}

	public String setToCheckTagExistance(Document dom,String formNo)throws RemoteException,ITMException
	{
		int ctr = 0;
		String retString=null;
		String childNodeName = null,columnValue=null;
		NodeList parentNodeList = null,childNodeList = null;
		Node parentNode = null,childNode = null;
		try
		{
			parentNodeList = dom.getElementsByTagName("Detail"+formNo);
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			ctr = 0;
			int childListLength = childNodeList.getLength();
			do
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(!childNodeName.equals("#text"))
				{
					if("1".equals(formNo))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue=childNode.getFirstChild().getNodeValue();
						}
						tagChkMapForDetail1.put(childNodeName,columnValue);
					}
					else if("2".equals(formNo))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue=childNode.getFirstChild().getNodeValue();
						}
						tagChkMapForDetail2.put(childNodeName,columnValue);
					}
				}
				ctr++;
			}while(ctr < childListLength);
		}
		catch(Exception e)
		{
			retString="ERROR";
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}

	public String saveData(String siteCode,String xmlString,String xtraParams, Connection conn) throws ITMException
	{
		  System.out.println("saving data...........");
		  InitialContext ctx = null;
		  String retString = null;
		  String loginCode=""; //declare by Nandkumar Gadkari on 04/07/18 
		  MasterStatefulLocal masterStateful = null; // for ejb3
		  try
		  {		
			  loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");//added by Nandkumar Gadkari on 04/07/18 
			   AppConnectParm appConnect = new AppConnectParm();
			   ctx = new InitialContext(appConnect.getProperty());
			    masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			  // MasterStateful masterStateful = masterStatefulHome.create();
			   String [] authencate = new String[2];
			   authencate[0] = loginCode; //changes by Nandkumar Gadkari on 04/07/18 
			   authencate[1] = "";
			   System.out.println("xmlString :::: " + xmlString);
			   retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
		 }
		 catch(ITMException itme)
		 {
		   	System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		 }
		 catch(Exception e)
		 {
		  	System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		 }
	  	 return retString;
	}

	private String createDistIssue()
	{
		StringBuffer xmlBuff = new StringBuffer();
		try
		{
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("dist_issue").append("]]></objName>");
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
			xmlBuff.append("<description>").append("Header0 members").append("</description>");

			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"1\">");
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_date><![CDATA[12/03/08]]></tran_date>");
			xmlBuff.append("<eff_date><![CDATA[12/03/08]]></eff_date>");
			xmlBuff.append("<dist_order><![CDATA[000623    ]]></dist_order>");
			xmlBuff.append("<site_code><![CDATA[SP612]]></site_code>");
			xmlBuff.append("<site_code__dlv><![CDATA[SP612]]></site_code__dlv>");
			xmlBuff.append("<dist_route><![CDATA[]]></dist_route>");
			xmlBuff.append("<tran_code><![CDATA[]]></tran_code>");
			xmlBuff.append("<lr_no><![CDATA[]]></lr_no>");
			xmlBuff.append("<lr_date><![CDATA[]]></lr_date>");
			xmlBuff.append("<lorry_no><![CDATA[]]></lorry_no>");
			xmlBuff.append("<gross_weight><![CDATA[0]]></gross_weight>");
			xmlBuff.append("<tare_weight><![CDATA[0]]></tare_weight>");
			xmlBuff.append("<net_weight><![CDATA[0]]></net_weight>");
			xmlBuff.append("<frt_amt><![CDATA[0]]></frt_amt>");
			xmlBuff.append("<amount><![CDATA[0]]></amount>");
			xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
			xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
			xmlBuff.append("<remarks><![CDATA[]]></remarks>");
			xmlBuff.append("<frt_type><![CDATA[T]]></frt_type>");
			xmlBuff.append("<chg_user><![CDATA[BASE      ]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA[01BASE228      ]]></chg_term>");
			xmlBuff.append("<curr_code><![CDATA[" + this.currCode + "]]></curr_code>");
			xmlBuff.append("<chg_date><![CDATA[12/03/08 15:48:40]]></chg_date>");
			xmlBuff.append("<site_descr><![CDATA[TEST INDUSTRIES ]]></site_descr>");
			xmlBuff.append("<site_to_descr><![CDATA[TEST INDUSTRIES ]]></site_to_descr>");
			xmlBuff.append("<location_descr><![CDATA[GOODS IN TRANSIT]]></location_descr>");
			xmlBuff.append("<tran_name><![CDATA[]]></tran_name>");
			xmlBuff.append("<currency_descr><![CDATA[INDIAN RUPEE]]></currency_descr>");
			xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
			xmlBuff.append("<loc_code__git><![CDATA[INTR    ]]></loc_code__git>");
			xmlBuff.append("<conf_date><![CDATA[]]></conf_date>");
			xmlBuff.append("<no_art><![CDATA[0]]></no_art>");
			xmlBuff.append("<trans_mode><![CDATA[D]]></trans_mode>");
			xmlBuff.append("<gp_no><![CDATA[]]></gp_no>");
			xmlBuff.append("<gp_date><![CDATA[12/03/08 15:43:38]]></gp_date>");
			xmlBuff.append("<conf_passwd><![CDATA[2413]]></conf_passwd>");
			xmlBuff.append("<order_type><![CDATA[F  ]]></order_type>");
			xmlBuff.append("<gp_ser><![CDATA[I     ]]></gp_ser>");
			xmlBuff.append("<ref_no><![CDATA[]]></ref_no>");
			xmlBuff.append("<ref_date><![CDATA[]]></ref_date>");
			xmlBuff.append("<available_yn><![CDATA[N]]></available_yn>");
			xmlBuff.append("<site_add1><![CDATA[E-200, FIRST FLOOR,]]></site_add1>");
			xmlBuff.append("<site_add2><![CDATA[TRANSPORT NAGAR,]]></site_add2>");
			xmlBuff.append("<site_city><![CDATA[LUCKNOW]]></site_city>");
			xmlBuff.append("<site_pin><![CDATA[ ]]></site_pin>");
			xmlBuff.append("<site_state_code><![CDATA[UP   ]]></site_state_code>");
			xmlBuff.append("<exch_rate><![CDATA[1]]></exch_rate>");
			xmlBuff.append("<tran_type><![CDATA[IT]]></tran_type>");
			xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
			xmlBuff.append("<discount><![CDATA[]]></discount>");
			xmlBuff.append("<permit_no><![CDATA[]]></permit_no>");
			xmlBuff.append("<shipment_id><![CDATA[]]></shipment_id>");
			xmlBuff.append("<curr_code__frt><![CDATA[" + this.currCode + "]]></curr_code__frt>");
			xmlBuff.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
			xmlBuff.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
			xmlBuff.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
			xmlBuff.append("<dc_no><![CDATA[]]></dc_no>");
			xmlBuff.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
			xmlBuff.append("<part_qty><![CDATA[A]]></part_qty>");
			xmlBuff.append("<sundry_details><![CDATA[]]></sundry_details>");
			xmlBuff.append("<sundry_name><![CDATA[]]></sundry_name>");
			xmlBuff.append("<proj_code><![CDATA[]]></proj_code>");
			xmlBuff.append("<site_tele1><![CDATA[]]></site_tele1>");
			xmlBuff.append("<site_tele2><![CDATA[]]></site_tele2>");
			xmlBuff.append("<site_tele3><![CDATA[]]></site_tele3>");
			xmlBuff.append("<site_code__bil><![CDATA[]]></site_code__bil>");
			xmlBuff.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
			xmlBuff.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
			xmlBuff.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
			xmlBuff.append("<site_city_bill><![CDATA[]]></site_city_bill>");
			xmlBuff.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
			xmlBuff.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
			xmlBuff.append("<pallet_wt><![CDATA[]]></pallet_wt>");
			xmlBuff.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
			xmlBuff.append("</Detail1>");
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}

	private void createDistReceipt()
	{
		StringBuffer xmlBuff = new StringBuffer();
		try
		{
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("dist_issue").append("]]></objName>");
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
			xmlBuff.append("<description>").append("Header0 members").append("</description>");

			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\"  objName=\"dist_receipt\"  objContext=\"1\">");
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_date><![CDATA[27/10/06]]></tran_date>");
			xmlBuff.append("<eff_date><![CDATA[27/10/06]]></eff_date>");
			xmlBuff.append("<dist_order><![CDATA[000026    ]]></dist_order>");
			xmlBuff.append("<tran_id__iss><![CDATA[000000031 ]]></tran_id__iss>");
			xmlBuff.append("<dist_route><![CDATA[]]></dist_route>");
			xmlBuff.append("<site_code><![CDATA[SP612]]></site_code>");
			xmlBuff.append("<site_code__ship><![CDATA[SP612]]></site_code__ship>");
			xmlBuff.append("<tran_code><![CDATA[]]></tran_code>");
			xmlBuff.append("<lr_no><![CDATA[]]></lr_no>");
			xmlBuff.append("<lr_date><![CDATA[]]></lr_date>");
			xmlBuff.append("<lorry_no><![CDATA[]]></lorry_no>");
			xmlBuff.append("<gross_weight><![CDATA[0]]></gross_weight>");
			xmlBuff.append("<tare_weight><![CDATA[0]]></tare_weight>");
			xmlBuff.append("<net_weight><![CDATA[0]]></net_weight>");
			xmlBuff.append("<frt_amt><![CDATA[0]]></frt_amt>");
			xmlBuff.append("<amount><![CDATA[62.94]]></amount>");
			xmlBuff.append("<tax_amt><![CDATA[5.715]]></tax_amt>");
			xmlBuff.append("<net_amt><![CDATA[68.655]]></net_amt>");
			xmlBuff.append("<remarks><![CDATA[]]></remarks>");
			xmlBuff.append("<frt_type><![CDATA[T]]></frt_type>");
			xmlBuff.append("<chg_user><![CDATA[BASE      ]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA[07BASE227      ]]></chg_term>");
			xmlBuff.append("<curr_code><![CDATA[" + this.currCode + "]]></curr_code>");
			xmlBuff.append("<chg_date><![CDATA[10/01/07 17:12:28]]></chg_date>");
			xmlBuff.append("<site_descr><![CDATA[TEST INDUSTRIES ]]></site_descr>");
			xmlBuff.append("<site_to_descr><![CDATA[TEST INDUSTRIES ]]></site_to_descr>");
			xmlBuff.append("<location_descr><![CDATA[GOODS IN TRANSIT]]></location_descr>");
			xmlBuff.append("<tran_name><![CDATA[]]></tran_name>");
			xmlBuff.append("<currency_descr><![CDATA[INDIAN RUPEE]]></currency_descr>");
			xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
			xmlBuff.append("<loc_code__git><![CDATA[INTR    ]]></loc_code__git>");
			xmlBuff.append("<volume><![CDATA[0]]></volume>");
			xmlBuff.append("<conf_date><![CDATA[]]></conf_date>");
			xmlBuff.append("<no_art><![CDATA[0]]></no_art>");
			xmlBuff.append("<trans_mode><![CDATA[R]]></trans_mode>");
			xmlBuff.append("<conf_passwd><![CDATA[]]></conf_passwd>");
			xmlBuff.append("<order_type><![CDATA[F  ]]></order_type>");
			xmlBuff.append("<exch_rate><![CDATA[1]]></exch_rate>");
			xmlBuff.append("<tran_type><![CDATA[IT]]></tran_type>");
			xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
			xmlBuff.append("<qc_reqd><![CDATA[N]]></qc_reqd>");
			xmlBuff.append("<gp_no><![CDATA[00002]]></gp_no>");
			xmlBuff.append("<gp_date><![CDATA[27/10/06]]></gp_date>");
			xmlBuff.append("<loc_code__gitbf><![CDATA[INTR    ]]></loc_code__gitbf>");
			xmlBuff.append("<location_descr_gitbf><![CDATA[GOODS IN TRANSIT]]></location_descr_gitbf>");
			xmlBuff.append("<issue_ref><![CDATA[]]></issue_ref>");
			xmlBuff.append("<proj_code><![CDATA[]]></proj_code>");
			xmlBuff.append("<site_code__bil><![CDATA[SP612]]></site_code__bil>");
			xmlBuff.append("<site_descr_bill><![CDATA[TEST INDUSTRIES ]]></site_descr_bill>");
			xmlBuff.append("<site_add1><![CDATA[E-200, FIRST FLOOR,]]></site_add1>");
			xmlBuff.append("<site_add2><![CDATA[TRANSPORT NAGAR,]]></site_add2>");
			xmlBuff.append("<site_city><![CDATA[LUCKNOW]]></site_city>");
			xmlBuff.append("<site_pin><![CDATA[ ]]></site_pin>");
			xmlBuff.append("<site_state_code><![CDATA[UP   ]]></site_state_code>");
			xmlBuff.append("<pallet_wt><![CDATA[]]></pallet_wt>");
			xmlBuff.append("</Detail1>");
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	 private String getCurrdateAppFormat()
    {
        String s = "";
        //GenericUtility genericUtility = GenericUtility.getInstance();
        E12GenericUtility genericUtility = new E12GenericUtility();
        try
        {
            java.util.Date date = null;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println(genericUtility.getDBDateFormat());

            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
            s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
        }
        catch(Exception exception)
        {
            System.out.println("Exception in [MPSOrder] getCurrdateAppFormat " + exception.getMessage());
        }
        return s;
    }

}