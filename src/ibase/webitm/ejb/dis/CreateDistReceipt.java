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
//import ibase.webitm.ejb.dis.adv.DistOrderRcpConf;
import ibase.webitm.ejb.dis.DistOrderRcpConf;

public class CreateDistReceipt
{
	public String createDistributionReceipt(String xmlString,String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		String errString = "";
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
			dom = (DocumentBuilderFactory.newInstance()).newDocumentBuilder().parse(bais);
			errString = createDistributionReceipt(dom,xtraParams, conn);
		}
		catch(Exception e)
		{
			System.out.println("Exception : CreateDistReceipt : "+e.getMessage());
		}
		return (errString);
	}
	public String createDistributionReceipt(Document dom,String xtraParams,Connection conn)throws RemoteException,ITMException
	{
		//Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer xmlBuff = new StringBuffer();
		String sql = "",valType = "",fldMin ="" , modName = "";
		String distOrder = "",siteCodeTo ="",siteCodeFr ="";
		String distRoute ="",tranCode ="",lrDateStr ="",lorryNo ="",gpNo ="";
		String gpDateStr ="",frtType ="",currCode ="",locCodeGit ="";
		String remarks = "", physicalStatus = "", grade = "", batchNo = "", suppCodeMfg = "";
		String dimension = "",tranIdIss = "";
		String tranDate = "",tranCodeIss = "",lrNo = "",itemCode = "",unit = "",taxEnv = "";
		String taxClass ="", taxChap = "",locCode = "",lotNo = "", lotSl ="",packCode = "";
		String siteCodeMfg = "",expDate = "",packInstr = "",mfgDate = "";
		String xmlString ="",projCode  = "",tranType ="",qcReqd ="",confPasswd = "",transMode = "";
		String locCodeCons = "",orderType = "",retString ="",sitetype = "",chgTerm ="",chgUser = "";
		String tranTypeParent = "",autoReceipt = "";

		double palletWt = 0,actualQty  = 0,	discount = 0,costRate = 0,grossWeight = 0,tareWeight = 0,frtAmt =0;
		double netWeight   = 0, rate =0,amount = 0, taxAmt	= 0, netAmt = 0, quantity = 0,potencyPerc =0;
		double rateClg =0, exchRate =0;
		long noArt = 0L,ctr =0;
		int lineNoDistOrder	= 0 ;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;


		try
		{	if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				conn = connDriver.getConnectDB("DriverITM");
				connDriver = null;
			}

			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			for(int i=0; i<childNodeList.getLength(); i++)
			{
					childNode = childNodeList.item(i);
					childNodeName = childNode.getNodeName();
					if(childNode.getNodeName().equalsIgnoreCase("tran_id"))
					{
						if (childNode.getFirstChild() != null)
						{
							tranIdIss = childNode.getFirstChild().getNodeValue();
							System.out.println("tran_id : " + tranIdIss);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("dist_order"))
					{
						if (childNode.getFirstChild() != null)
						{
							distOrder = childNode.getFirstChild().getNodeValue();
							System.out.println("dist_order : " + distOrder);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("tran_date"))
					{
						if (childNode.getFirstChild() != null)
						{
							tranDate = childNode.getFirstChild().getNodeValue();
							System.out.println("tran_date : " + tranDate);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("site_code"))
					{
						if (childNode.getFirstChild() != null)
						{
							siteCodeFr = childNode.getFirstChild().getNodeValue();
							System.out.println("Node Value1 : " + siteCodeFr);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("site_code__dlv"))
					{
						if (childNode.getFirstChild() != null)
						{
							siteCodeTo = childNode.getFirstChild().getNodeValue();
							System.out.println("siteCodeTo : " + siteCodeTo);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("dist_route"))
					{
						if (childNode.getFirstChild() != null)
						{
							distRoute = childNode.getFirstChild().getNodeValue();
							System.out.println("distRoute : " + distRoute);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("tran_code"))
					{
						if (childNode.getFirstChild() != null)
						{
							tranCodeIss = childNode.getFirstChild().getNodeValue();
							System.out.println("tranCodeIss : " + tranCodeIss);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("lr_no"))
					{
						if (childNode.getFirstChild() != null)
						{
							lrNo = childNode.getFirstChild().getNodeValue();
							System.out.println("lrNo : " + lrNo);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("lr_date"))
					{
						if (childNode.getFirstChild() != null)
						{
							lrDateStr = childNode.getFirstChild().getNodeValue();
							System.out.println("lrDateStr : " + lrDateStr);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("lorry_no"))
					{
						if (childNode.getFirstChild() != null)
						{
							lorryNo = childNode.getFirstChild().getNodeValue();
							System.out.println("lorryNo : " + lorryNo);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("gp_no"))
					{
						if (childNode.getFirstChild() != null)
						{
							gpNo = childNode.getFirstChild().getNodeValue();
							System.out.println("gp_no : " + gpNo);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("gp_date"))
					{
						{
							gpDateStr = childNode.getFirstChild().getNodeValue();
							System.out.println("gp_date : " + gpDateStr);
						}				}
					if(childNode.getNodeName().equalsIgnoreCase("gross_weight"))
					{
					   if (childNode.getFirstChild() != null)
						{
							grossWeight = Double.parseDouble(childNode.getFirstChild().getNodeValue());
							System.out.println("gross_weight : " + grossWeight);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("tare_weight"))
					{
						if (childNode.getFirstChild() != null)
						{
							tareWeight =Double.parseDouble( childNode.getFirstChild().getNodeValue());
							System.out.println("tare_weight " + tareWeight);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("net_weight"))
					{
						if (childNode.getFirstChild() != null)
						{
							netWeight = Double.parseDouble(childNode.getFirstChild().getNodeValue());
							System.out.println("net_weight : " + netWeight);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("pallet_wt"))
					{
						if (childNode.getFirstChild() != null)
						{
							palletWt = Double.parseDouble(childNode.getFirstChild().getNodeValue());
							System.out.println("pallet_wt : " + palletWt);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("frt_amt"))
					{
						if (childNode.getFirstChild() != null)
						{
							frtAmt = Double.parseDouble( childNode.getFirstChild().getNodeValue());
							System.out.println("frt_amt : " + frtAmt);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("amount"))
					{
						if (childNode.getFirstChild() != null)
						{
							amount =  Double.parseDouble( childNode.getFirstChild().getNodeValue());
							System.out.println("amount : " + amount);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("tax_amt"))
					{
						if (childNode.getFirstChild() != null)
						{
							taxAmt =  Double.parseDouble(childNode.getFirstChild().getNodeValue());
							System.out.println("tax_amt : " + taxAmt);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("remarks"))
					{
						if (childNode.getFirstChild() != null)
						{
							remarks = childNode.getFirstChild().getNodeValue();
							System.out.println("remarks : " + remarks);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("frt_type"))
					{
						if (childNode.getFirstChild() != null)
						{
							frtType = childNode.getFirstChild().getNodeValue();
							System.out.println("frt_type : " + frtType);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("curr_code"))
					{
						if (childNode.getFirstChild() != null)
						{
							currCode = childNode.getFirstChild().getNodeValue();
							System.out.println("curr_code : " + currCode);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("loc_code__git"))
					{
						if (childNode.getFirstChild() != null)
						{
							locCodeGit = childNode.getFirstChild().getNodeValue();
							System.out.println("locCodeGit : " + locCodeGit);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("no_art"))
					{
						if (childNode.getFirstChild() != null)
						{
							noArt = Long.parseLong(childNode.getFirstChild().getNodeValue());
							System.out.println("no_art : " + noArt);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("trans_mode"))
					{
						if (childNode.getFirstChild() != null)
						{
							transMode = childNode.getFirstChild().getNodeValue();
							System.out.println("transMode : " + transMode);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("order_type"))
					{
						if (childNode.getFirstChild() != null)
						{
							orderType = childNode.getFirstChild().getNodeValue();
							System.out.println("order_type : " + orderType);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("tran_type"))
					{
						if (childNode.getFirstChild() != null)
						{
							tranType = childNode.getFirstChild().getNodeValue();
							//tranType = tranType == null ? "":tranType.trim();
							System.out.println("tranType : " + tranType);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("exch_rate"))
					{
						if (childNode.getFirstChild() != null)
						{
							exchRate = Double.parseDouble(childNode.getFirstChild().getNodeValue());
							System.out.println("exch_rate : " + exchRate);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("conf_passwd"))
					{
						if (childNode.getFirstChild() != null)
						{
							confPasswd = childNode.getFirstChild().getNodeValue();
							System.out.println("confPasswd : " + confPasswd);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("proj_code"))
					{
						if (childNode.getFirstChild() != null)
						{
							projCode = childNode.getFirstChild().getNodeValue();
							System.out.println("proj_code : " + projCode);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("net_amt"))
					{
						if (childNode.getFirstChild() != null)
						{
							netAmt = Double.parseDouble(childNode.getFirstChild().getNodeValue());
							System.out.println("net_amt : " + projCode);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("chg_user"))
					{
						if (childNode.getFirstChild() != null)
						{
							chgUser = childNode.getFirstChild().getNodeValue();
							System.out.println("chgUser : " + chgUser);
						}
					}
					if(childNode.getNodeName().equalsIgnoreCase("chg_term"))
					{
						if (childNode.getFirstChild() != null)
						{
							chgTerm = childNode.getFirstChild().getNodeValue();
							System.out.println("chgTerm : " + chgTerm);
						}
					}
			}
			sql="SELECT  tran_type__parent  FROM distorder_type "
				+" where tran_type= ?";
			//System.out.println("Sql................"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranType);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranTypeParent = rs.getString("tran_type__parent");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(! tranTypeParent.trim().equalsIgnoreCase(tranType))
			{
				sql="SELECT  loc_code__cons  FROM distorder_type "
					+" where tran_type= ? ";
				//System.out.println("Sql................"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranType);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locCodeCons = rs.getString("loc_code__cons");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else
			{
				sql="SELECT  loc_code__cons  FROM distorder "
					+" where dist_order = ?";
				//System.out.println("Sql................"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locCodeCons = rs.getString("loc_code__cons");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			sql="select qc_reqd  FROM distorder_type "
				+" where tran_type = ? ";
			//System.out.println("Sql................"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranType);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				qcReqd = rs.getString("qc_reqd");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(qcReqd == null ||qcReqd.trim().length() ==0)
			{
				sql="select case when site_type is null then ' ' else site_type end "
				+" from site where site_code = ?";
				//System.out.println("Sql................"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeTo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					sitetype = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("F".equalsIgnoreCase(sitetype.trim()) || "L".equalsIgnoreCase(sitetype.trim()) || "T".equalsIgnoreCase(sitetype.trim()))
				{
					qcReqd ="Y";
				}
				else
				{
					qcReqd ="N";
				}
			}
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("dist_receipt").append("]]></objName>");
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
			xmlBuff.append("<tran_date><![CDATA["+tranDate+"]]></tran_date>");
			xmlBuff.append("<eff_date><![CDATA["+tranDate+"]]></eff_date>");
			xmlBuff.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
			xmlBuff.append("<tran_id__iss><![CDATA["+tranIdIss+"]]></tran_id__iss>");
			xmlBuff.append("<dist_route><![CDATA["+distRoute+"]]></dist_route>");
			xmlBuff.append("<site_code><![CDATA["+siteCodeTo+"]]></site_code>");
			xmlBuff.append("<site_code__ship><![CDATA["+siteCodeFr+"]]></site_code__ship>");
			xmlBuff.append("<tran_code><![CDATA["+tranCodeIss+"]]></tran_code>");
			xmlBuff.append("<lr_no><![CDATA["+lrNo+"]]></lr_no>");
			xmlBuff.append("<lr_date><![CDATA["+lrDateStr+"]]></lr_date>");
			xmlBuff.append("<lorry_no><![CDATA["+lorryNo+"]]></lorry_no>");
			xmlBuff.append("<gross_weight><![CDATA["+grossWeight+"]]></gross_weight>");
			xmlBuff.append("<tare_weight><![CDATA["+tareWeight+"]]></tare_weight>");
			xmlBuff.append("<net_weight><![CDATA["+netWeight+"]]></net_weight>");
			xmlBuff.append("<frt_amt><![CDATA["+frtAmt+"]]></frt_amt>");
			xmlBuff.append("<amount><![CDATA["+amount+"]]></amount>");
			xmlBuff.append("<tax_amt><![CDATA["+taxAmt+"]]></tax_amt>");
			xmlBuff.append("<net_amt><![CDATA["+netAmt+"]]></net_amt>");
			xmlBuff.append("<remarks><![CDATA["+remarks+"]]></remarks>");
			xmlBuff.append("<frt_type><![CDATA["+frtType+"]]></frt_type>");
			xmlBuff.append("<chg_user><![CDATA["+chgUser+"]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA["+chgTerm+"]]></chg_term>");
			xmlBuff.append("<curr_code><![CDATA["+currCode+"]]></curr_code>");
			xmlBuff.append("<chg_date><![CDATA["+getCurrdateAppFormat()+"]]></chg_date>");
			xmlBuff.append("<site_descr><![CDATA[]]></site_descr>");//??
			xmlBuff.append("<site_to_descr><![CDATA[]]></site_to_descr>"); //??
			xmlBuff.append("<location_descr><![CDATA[]]></location_descr>");//??

			xmlBuff.append("<tran_name><![CDATA[]]></tran_name>");
			xmlBuff.append("<currency_descr><![CDATA[INDIAN RUPEE]]></currency_descr>");

			xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
			xmlBuff.append("<loc_code__git><![CDATA["+locCodeGit+"]]></loc_code__git>");
			xmlBuff.append("<volume><![CDATA[0]]></volume>");
			xmlBuff.append("<conf_date><![CDATA[]]></conf_date>");
			xmlBuff.append("<no_art><![CDATA["+noArt+"]]></no_art>");
			xmlBuff.append("<trans_mode><![CDATA["+transMode+"]]></trans_mode>");
			xmlBuff.append("<conf_passwd><![CDATA["+confPasswd+"]]></conf_passwd>");
			orderType = orderType == null ? "" :orderType.trim();
			xmlBuff.append("<order_type><![CDATA["+orderType+"]]></order_type>");
			xmlBuff.append("<exch_rate><![CDATA["+exchRate+"]]></exch_rate>");
			xmlBuff.append("<tran_type><![CDATA["+tranType.trim()+"]]></tran_type>");
			xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>"); //???
			xmlBuff.append("<qc_reqd><![CDATA["+qcReqd+"]]></qc_reqd>");
			xmlBuff.append("<gp_no><![CDATA["+gpNo+"]]></gp_no>");
			xmlBuff.append("<gp_date><![CDATA["+gpDateStr+"]]></gp_date>");
			xmlBuff.append("<loc_code__gitbf><![CDATA[]]></loc_code__gitbf>");
			xmlBuff.append("<location_descr_gitbf><![CDATA[]]></location_descr_gitbf>");
			xmlBuff.append("<issue_ref><![CDATA[]]></issue_ref>");
			xmlBuff.append("<proj_code><![CDATA["+projCode+"]]></proj_code>");
			xmlBuff.append("<site_code__bil><![CDATA[]]></site_code__bil>");
			xmlBuff.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
			xmlBuff.append("<site_add1><![CDATA[]]></site_add1>");
			xmlBuff.append("<site_add2><![CDATA[]]></site_add2>");
			xmlBuff.append("<site_city><![CDATA[]]></site_city>");
			xmlBuff.append("<site_pin><![CDATA[ ]]></site_pin>");
			xmlBuff.append("<site_state_code><![CDATA[]]></site_state_code>");
			xmlBuff.append("<pallet_wt><![CDATA["+palletWt+"]]></pallet_wt>");
			xmlBuff.append("</Detail1>");


			parentNodeList = dom.getElementsByTagName("Detail2");
			int detlen = parentNodeList.getLength();
			for (int detctr =0;detctr < detlen;detctr++)
			{
				System.out.println("detctr : " + detctr);
				parentNode = parentNodeList.item(detctr);
				childNodeList = parentNode.getChildNodes();
				for(int i=0; i<childNodeList.getLength(); i++)
				{
						childNode = childNodeList.item(i);
						childNodeName = childNode.getNodeName();
						if(childNode.getNodeName().equalsIgnoreCase("dist_order"))
						{
							if (childNode.getFirstChild() != null)
							{
								distOrder = childNode.getFirstChild().getNodeValue();
								System.out.println("distOrder : " + distOrder);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("line_no_dist_order"))
						{
							if (childNode.getFirstChild() != null)
							{
								lineNoDistOrder =Integer.parseInt( childNode.getFirstChild().getNodeValue());
								System.out.println("lineNoDistOrder : " + lineNoDistOrder);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("item_code"))
						{
							if (childNode.getFirstChild() != null)
							{
								itemCode = childNode.getFirstChild().getNodeValue();
								System.out.println("item_code : " + itemCode);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("quantity"))
						{
							if (childNode.getFirstChild() != null)
							{
								quantity = Double.parseDouble( childNode.getFirstChild().getNodeValue());
								System.out.println("quantity : " + quantity);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("unit"))
						{
							if (childNode.getFirstChild() != null)
							{
								unit = childNode.getFirstChild().getNodeValue();
								System.out.println("unit : " + unit);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("tax_class"))
						{
							if (childNode.getFirstChild() != null)
							{
								taxClass = childNode.getFirstChild().getNodeValue();
								System.out.println("tax_class : " + taxClass);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("tax_chap"))
						{
							if (childNode.getFirstChild() != null)
							{
								taxChap = childNode.getFirstChild().getNodeValue();
								System.out.println("tax_chap : " + taxChap);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("tax_env"))
						{
							if (childNode.getFirstChild() != null)
							{
								taxEnv = childNode.getFirstChild().getNodeValue();
								System.out.println("tax_env : " + taxEnv);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("loc_code"))
						{
							if (childNode.getFirstChild() != null)
							{
								locCode = childNode.getFirstChild().getNodeValue();
								System.out.println("loc_code : " + locCode);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("lot_no"))
						{
							if (childNode.getFirstChild() != null)
							{
								lotNo = childNode.getFirstChild().getNodeValue();
								System.out.println("lot_no : " + lotNo);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("lot_sl"))
						{
							if (childNode.getFirstChild() != null)
							{
								lotSl = childNode.getFirstChild().getNodeValue();
								System.out.println("lot_sl : " + lotSl);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("pack_code"))
						{
							if (childNode.getFirstChild() != null)
							{
								packCode = childNode.getFirstChild().getNodeValue();
								System.out.println("pack_code : " + packCode);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("rate"))
						{
							if (childNode.getFirstChild() != null)
							{
								rate = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("rate : " + rate);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("rate__clg"))
						{
							if (childNode.getFirstChild() != null)
							{
								rateClg = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("rate__clg : " + rateClg);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("amount"))
						{
							if (childNode.getFirstChild() != null)
							{
								amount = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("amount : " + amount);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("tax_amt"))
						{
							if (childNode.getFirstChild() != null)
							{
								taxAmt = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("tax_amt : " + taxAmt);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("net_amt"))
						{
							if (childNode.getFirstChild() != null)
							{
								netAmt = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("net_amt : " + netAmt);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("site_code__mfg"))
						{
							if (childNode.getFirstChild() != null)
							{
								siteCodeMfg = childNode.getFirstChild().getNodeValue();
								System.out.println("site_code__mfg : " + siteCodeMfg);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("mfg_date"))
						{
							if (childNode.getFirstChild() != null)
							{
								mfgDate = childNode.getFirstChild().getNodeValue();
								System.out.println("mfg_date : " + mfgDate);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("exp_date"))
						{
							if (childNode.getFirstChild() != null)
							{
								expDate = childNode.getFirstChild().getNodeValue();
								System.out.println("exp_date : " + expDate);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("potency_perc"))
						{
							if (childNode.getFirstChild() != null)
							{
								potencyPerc = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("potency_perc : " + potencyPerc);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("no_art"))
						{
							if (childNode.getFirstChild() != null)
							{
								noArt = Long.parseLong(childNode.getFirstChild().getNodeValue());
								System.out.println("no_art : " + noArt);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("gross_weight"))
						{
							if (childNode.getFirstChild() != null)
							{
								grossWeight = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("gross_weight : " + grossWeight);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("tare_weight"))
						{
							if (childNode.getFirstChild() != null)
							{
								tareWeight = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("tare_weight : " + tareWeight);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("net_weight"))
						{
							if (childNode.getFirstChild() != null)
							{
								netWeight = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("net_weight : " + netWeight);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("pallet_wt"))
						{
							if (childNode.getFirstChild() != null)
							{
								palletWt = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("pallet_wt : " + palletWt);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("pack_instr"))
						{
							if (childNode.getFirstChild() != null)
							{
								packInstr = childNode.getFirstChild().getNodeValue();
								System.out.println("pack_instr : " + packInstr);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("dimension"))
						{
							if (childNode.getFirstChild() != null)
							{
								dimension = childNode.getFirstChild().getNodeValue();
								System.out.println("dimension : " + dimension);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("supp_code__mfg"))
						{
							if (childNode.getFirstChild() != null)
							{
								suppCodeMfg = childNode.getFirstChild().getNodeValue();
								System.out.println("supp_code__mfg : " + suppCodeMfg);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("batch_no"))
						{
							if (childNode.getFirstChild() != null)
							{
								batchNo = childNode.getFirstChild().getNodeValue();
								System.out.println("batch_no : " + batchNo);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("grade"))
						{
							if (childNode.getFirstChild() != null)
							{
								grade = childNode.getFirstChild().getNodeValue();
								System.out.println("grade : " + grade);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("cost_rate"))
						{
							if (childNode.getFirstChild() != null)
							{
								costRate = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("cost_rate : " + costRate);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("discount"))
						{
							if (childNode.getFirstChild() != null)
							{
								discount = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("discount : " + discount);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("physical_status"))
						{
							if (childNode.getFirstChild() != null)
							{
								physicalStatus = childNode.getFirstChild().getNodeValue();
								System.out.println("physical_status : " + physicalStatus);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("actual_qty"))
						{
							if (childNode.getFirstChild() != null)
							{
								actualQty = Double.parseDouble(childNode.getFirstChild().getNodeValue());
								System.out.println("actual_qty : " + actualQty);
							}
						}
						if(childNode.getNodeName().equalsIgnoreCase("remarks"))
						{
							if (childNode.getFirstChild() != null)
							{
								remarks = childNode.getFirstChild().getNodeValue();
								System.out.println("remarks : " + remarks);
							}
						}
				}
				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"dist_receipt\" objContext=\"2\">");
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<line_no>"+(detctr+1)+"</line_no>");
				xmlBuff.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
				xmlBuff.append("<line_no_dist_order><![CDATA["+lineNoDistOrder+"]]></line_no_dist_order>");
				xmlBuff.append("<item_code><![CDATA["+itemCode+"]]></item_code>");
				xmlBuff.append("<quantity><![CDATA["+quantity+"]]></quantity>");
				xmlBuff.append("<unit><![CDATA["+unit+"]]></unit>");
				xmlBuff.append("<tax_class><![CDATA["+taxClass+"]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA["+taxChap+"]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA["+taxEnv+"]]></tax_env>");
				xmlBuff.append("<loc_code><![CDATA["+locCode+"]]></loc_code>");
				xmlBuff.append("<lot_no><![CDATA["+lotNo+"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+lotSl+"]]></lot_sl>");
				xmlBuff.append("<pack_code><![CDATA["+packCode+"]]></pack_code>");
				xmlBuff.append("<rate><![CDATA["+rate+"]]></rate>");
				xmlBuff.append("<amount><![CDATA["+amount+"]]></amount>");
				xmlBuff.append("<tax_amt><![CDATA["+taxAmt+"]]></tax_amt>");
				xmlBuff.append("<net_amt><![CDATA["+netAmt+"]]></net_amt>");
				xmlBuff.append("<item_descr><![CDATA["+amount+"]]></item_descr>");
				xmlBuff.append("<location_descr><![CDATA[]]></location_descr>");
				xmlBuff.append("<site_code__mfg><![CDATA["+siteCodeMfg+"]]></site_code__mfg>");
				xmlBuff.append("<mfg_date><![CDATA["+mfgDate+"]]></mfg_date>");
				System.out.println("manohar exp_date [" + expDate + "]");
				xmlBuff.append("<exp_date><![CDATA["+expDate+"]]></exp_date>");
				xmlBuff.append("<potency_perc><![CDATA["+potencyPerc+"]]></potency_perc>");

				xmlBuff.append("<no_art><![CDATA["+noArt+"]]></no_art>");
				xmlBuff.append("<gross_weight><![CDATA["+grossWeight+"]]></gross_weight>");
				xmlBuff.append("<tare_weight><![CDATA["+tareWeight+"]]></tare_weight>");
				xmlBuff.append("<net_weight><![CDATA["+netWeight+"]]></net_weight>");
				xmlBuff.append("<pack_instr><![CDATA["+packInstr+"]]></pack_instr>");
				xmlBuff.append("<dimension><![CDATA["+dimension+"]]></dimension>");
				xmlBuff.append("<supp_code__mfg><![CDATA["+suppCodeMfg+"]]></supp_code__mfg>");
				xmlBuff.append("<batch_no><![CDATA["+batchNo+"]]></batch_no>");
				xmlBuff.append("<grade><![CDATA["+grade+"]]></grade>");
				xmlBuff.append("<rate__clg><![CDATA["+rateClg+"]]></rate__clg>");
				xmlBuff.append("<cost_rate><![CDATA["+costRate+"]]></cost_rate>");
				xmlBuff.append("<discount><![CDATA["+discount+"]]></discount>");
				xmlBuff.append("<physical_status><![CDATA["+physicalStatus+"]]></physical_status>");
				xmlBuff.append("<actual_qty><![CDATA["+actualQty+"]]></actual_qty>");
				xmlBuff.append("<remarks><![CDATA["+remarks+"]]></remarks>");
				xmlBuff.append("<pallet_wt><![CDATA["+palletWt+"]]></pallet_wt>");
				xmlBuff.append("</Detail2>");

			}
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");

			xmlString = xmlBuff.toString();
			retString = saveData(siteCodeFr,xmlString,conn);

			if (retString.indexOf("Success") != -1)
			{
				//<Root><Detail>Success</Detail><TranID>0028</TranID></Root>:
				int  d =retString.indexOf("<TranID>");
				int f =	 retString.indexOf("</TranID>");
				String TranIDRCP = retString.substring(d+8,f);
				System.out.println("TranIDRCP......... "+TranIDRCP);
				System.out.println("Distribution Receipt Generated Successfully");

				sql="select auto_receipt  FROM distorder "
				+" where dist_order = ? ";
				//System.out.println("Sql................"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					autoReceipt = rs.getString("auto_receipt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("C".equalsIgnoreCase(autoReceipt))
				{
					DistOrderRcpConf distOrderRcpConfEJB =new DistOrderRcpConf();
					retString = distOrderRcpConfEJB.actionConfirm(TranIDRCP,xtraParams,"", conn);
					distOrderRcpConfEJB = null;
				}
			}
			else
			{
				System.out.println("Distribution Receipt not Generated ");
			}

		}
		catch (Exception e)
		{

			 e.printStackTrace();
			System.out.println(" Error calling getXMLdata");
		}
		return retString;

	}
	public String saveData(String siteCode,String xmlString, Connection conn) throws ITMException
	{
		  System.out.println("saving data...........");
		  InitialContext ctx = null;
		  String retString = null;
		  MasterStatefulLocal masterStateful = null;
		  try
		  {
			   AppConnectParm appConnect = new AppConnectParm();
			   ctx = new InitialContext(appConnect.getProperty());
			    masterStateful = (MasterStatefulLocal)ctx.lookup("MasterStateful");
			   //MasterStateful masterStateful = masterStatefulHome.create();
			   String [] authencate = new String[2];
			   authencate[0] = "";
			   authencate[1] = "";
			   System.out.println("xmlString :::: " + xmlString);
			   retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
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
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
        }
        return s;
    }

}