

/********************************************************
	Title : StationAmdIC
	Date  : 30/04/12
	Developer: Kunal Mandhre

 ********************************************************/


package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless  

public class StationAmdIC extends ValidatorEJB implements StationAmdICLocal, StationAmdICRemote
{
	     //changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String refId = ""; 
		String tranId = ""; 
		String siteCode = "";
		String tranCode = "";
		String roadPermitNo = "";
		String status = "";
		String shipmentId = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String loginSiteCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int cnt = 0;
		int ctr=0;
		int childNodeListLength;
		Date tranDate = null;
		Date expiryDate = null;
		Date lrDate = null;
		SimpleDateFormat simpleDateFormat = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSiteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("tran_id"))
				{
					tranId = checkNull(genericUtility.getColumnValue("tran_id", dom));
					if(tranId == null || tranId.trim().length() == 0)
					{
						errCode = "NULLTASKID";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if(editFlag != null && editFlag.equalsIgnoreCase("A") && tranId != null && tranId.trim().length() > 0)
					{
						sql = "select count(*) from stn_amd where tran_id = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,tranId);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt > 0) 
						{
							errCode = "VMTRANID";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
						
				}
				else if(childNodeName.equalsIgnoreCase("ref_id"))
				{
					refId = checkNull(genericUtility.getColumnValue("ref_id", dom));
					if(refId == null || refId.trim().length() == 0)
					{
						errCode = "VMREFIDBK";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if(refId != null && refId.trim().length() > 0 )
					{
						sql = "select count(*) from distord_iss where tran_id = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,refId);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("132 1count = "+cnt);
						if(cnt == 0) 
						{
							errCode = "VTDISTISS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(cnt > 0)
						{
							sql = "select count(*) from distord_iss where tran_id = ? and confirmed='Y'";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,refId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);

							}
							rs.close();
							rs = null;
							pstmt.close(); 
							pstmt = null;
							if(cnt == 0) 
							{
								errCode = "VTDISCONF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = "select site_code from distord_iss where tran_id = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,refId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									siteCode =  rs.getString(1);
								}
								if(siteCode != null && !siteCode.equalsIgnoreCase(loginSiteCode))
								{
									errCode = "VTDIFFST";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("tran_code"))
				{    
					tranCode = genericUtility.getColumnValue("tran_code", dom);
					if(tranCode == null || tranCode.trim().length() == 0)
					{
						errCode = "VMCOUNTCD ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if(tranCode != null && tranCode.trim().length() > 0 )
					{
						sql = "select count(*) from transporter where tran_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,tranCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						if(cnt == 0) 
						{
							errCode = "VTTRANCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("rd_permit_no"))
				{    
					roadPermitNo = genericUtility.getColumnValue("rd_permit_no", dom);
					tranDate =  simpleDateFormat.parse(genericUtility.getColumnValue("tran_date", dom));
					if(roadPermitNo != null && roadPermitNo.trim().length() > 0 )
					{
						sql = "select count(*) from roadpermit where rd_permit_no = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,roadPermitNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt == 0) 
						{
							errCode = "VTRDPR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select expiry_date, status from roadpermit where rd_permit_no = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,roadPermitNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								expiryDate = rs.getDate("expiry_date");
								status = rs.getString("status");
								if(expiryDate.compareTo(tranDate) < 0)
								{
									errCode = "VTEFF02";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									if(status != null && status.equalsIgnoreCase("C"))
									{
										errCode = "VTRDPS";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						
					}
				}
				else if(childNodeName.equalsIgnoreCase("shipment_id"))
				{    
					shipmentId = genericUtility.getColumnValue("shipment_id", dom);
					if(shipmentId != null && shipmentId.trim().length() > 0 )
					{
						sql = "select count(1) from shipment where shipment_id = ? and confirmed = 'N'";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,shipmentId);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						if(cnt == 0) 
						{
							errCode = "VTSHPID";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("lr_date"))
				{  
					if(genericUtility.getColumnValue("lr_date", dom) == null)
					{
						lrDate = null;
					}
					else
					{
					 lrDate =  simpleDateFormat.parse(genericUtility.getColumnValue("lr_date", dom));
					}
					System.out.println("264 lR DATE = "+lrDate);
					refId =  genericUtility.getColumnValue("ref_id", dom);
					if(lrDate != null )
					{
						sql = "select tran_date from distord_iss where tran_id = ? and confirmed='Y'";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,refId);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							tranDate =  rs.getDate(1);

						}
						if(lrDate != null && tranDate != null && lrDate.compareTo(tranDate) < 0) 
						{
							errCode = "VTLRDTS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}

			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
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
					conn.close();
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [StationAmdIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String refId = "";
		String descr = "";
		String siteDescrTo = "";
		String siteDescr = "";
		String siteCode = "";
		String siteCodeTo = "";
		String lrNo = "";
		String transMode = "";
		String lorryNo = "";
		String shipmentId = "";
		String gpNo = "";
		String confirmed = "";
		String tranCode = "";
		String frtType = "";
		String remarks = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		double frtAmount = 0.0;
		double noArt = 0.0;
		double grossWeight = 0.0;
		Date lrDate = null;
		Date gpDate = null;
		Date today = new Date();
		SimpleDateFormat simpleDateFormat = null;
		//SimpleDateFormat simpleDateFormat1 = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//simpleDateFormat1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			valueXmlString.append("<Detail1>");
			int childNodeListLength = childNodeList.getLength();
			do
			{   
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				ctr ++;
			}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

			if(currentColumn.trim().equalsIgnoreCase("itm_default"))
			{
				valueXmlString.append("<tran_date>").append("<![CDATA[" +  simpleDateFormat.format(today) + "]]>").append("</tran_date>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("ref_id"))
			{
				refId = genericUtility.getColumnValue("ref_id", dom);
				System.out.println("ref id = "+refId);
				if(refId != null && refId.trim().length() > 0 )
				{

					sql = "select distord_iss.lr_no as lr_no, distord_iss.lr_date as lr_date,distord_iss.site_code as site_code,"
							+"distord_iss.site_code__dlv as site_code__dlv ,a.descr as descr,b.descr as descr_b,distord_iss.confirmed as confirmed,"
							+"distord_iss.trans_mode as trans_mode,distord_iss.lorry_no as lorry_no,distord_iss.shipment_id as shipment_id,distord_iss.tran_code as tran_code,"
							+"distord_iss.frt_amt as frt_amt, distord_iss.frt_type as frt_type, distord_iss.no_art as no_art, distord_iss.remarks as remarks,"
							+"distord_iss.gp_no as gp_no,distord_iss.gp_date as gp_date,distord_iss.gross_weight as gross_weight "
							+"	from distord_iss,site a,site b 	where distord_iss.tran_id = ? and "
							+"	distord_iss.site_code = a.site_code and distord_iss.site_code__dlv = b.site_code ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,refId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{

						lrNo = rs.getString("lr_no") == null ? " " : rs.getString("lr_no");
						lrDate = rs.getDate("lr_date");
						siteCode = rs.getString("site_code") == null ? " " : rs.getString("site_code");
						siteCodeTo = rs.getString("site_code__dlv") == null ? " " : rs.getString("site_code__dlv");
						siteDescr = rs.getString("descr") == null ? " " : rs.getString("descr");
						siteDescrTo = rs.getString("descr_b") == null ? " " : rs.getString("descr_b");
						confirmed = rs.getString("confirmed") == null ? " " : rs.getString("confirmed");
						transMode = rs.getString("trans_mode") == null ? " " : rs.getString("trans_mode");
						lorryNo = rs.getString("lorry_no") == null ? " " : rs.getString("lorry_no");
						shipmentId = rs.getString("shipment_id") == null ? " " : rs.getString("shipment_id");
						tranCode = rs.getString("tran_code") == null ? " " : rs.getString("tran_code");
						frtAmount = rs.getDouble("frt_amt");
						frtType = rs.getString("frt_type") == null ? " " : rs.getString("frt_type");
						noArt = rs.getInt("no_art");
						remarks = rs.getString("remarks") == null ? " " : rs.getString("remarks");
						gpNo = rs.getString("gp_no") == null ? " " : rs.getString("gp_no");
						grossWeight = rs.getDouble("gross_weight");
						gpDate = rs.getDate("gp_date");
						//valueXmlString.append("<country_descr>").append("<![CDATA[" + descr +"]]>").append("</country_descr>");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println(lrNo );
					System.out.println(lrDate);
					System.out.println(siteCode);
					System.out.println(confirmed);
					
					if(confirmed != null && confirmed.trim().equalsIgnoreCase("Y"))
					{
						System.out.println("526 lr date =="+lrDate);
						valueXmlString.append("<site_code>").append("<![CDATA[" + siteCode +"]]>").append("</site_code>");
						valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr +"]]>").append("</site_descr>");
						valueXmlString.append("<site_code__dlv>").append("<![CDATA[" + siteCodeTo +"]]>").append("</site_code__dlv>");
						valueXmlString.append("<site_descr_b>").append("<![CDATA[" + siteDescrTo +"]]>").append("</site_descr_b>");
						valueXmlString.append("<lr_no__o>").append("<![CDATA[" + lrNo +"]]>").append("</lr_no__o>");
						valueXmlString.append("<lr_no>").append("<![CDATA[" + lrNo +"]]>").append("</lr_no>");
						if(lrDate != null)
						{ 
							System.out.println("lr date not null...");
						valueXmlString.append("<lr_date__o>").append("<![CDATA[" + simpleDateFormat.format(lrDate)+"]]>").append("</lr_date__o>");
						valueXmlString.append("<lr_date>").append("<![CDATA[" + simpleDateFormat.format(lrDate) +"]]>").append("</lr_date>");
						}
						else
						{
							System.out.println("lr date  null...");
							valueXmlString.append("<lr_date__o>").append("<![CDATA[]]>").append("</lr_date__o>");
							valueXmlString.append("<lr_date>").append("<![CDATA[]]>").append("</lr_date>");
						}
						valueXmlString.append("<trans_mode__o>").append("<![CDATA[" + transMode +"]]>").append("</trans_mode__o>");
						valueXmlString.append("<trans_mode>").append("<![CDATA[" + transMode +"]]>").append("</trans_mode>");
						valueXmlString.append("<lorry_no__o>").append("<![CDATA[" + lorryNo +"]]>").append("</lorry_no__o>");
						valueXmlString.append("<lorry_no>").append("<![CDATA[" + lorryNo +"]]>").append("</lorry_no>");
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode +"]]>").append("</tran_code>");
						valueXmlString.append("<shipment_id>").append("<![CDATA[" + shipmentId +"]]>").append("</shipment_id>");
						valueXmlString.append("<frt_amt>").append("<![CDATA[" + frtAmount +"]]>").append("</frt_amt>");
						valueXmlString.append("<frt_amt__o>").append("<![CDATA[" + frtAmount +"]]>").append("</frt_amt__o>");
						valueXmlString.append("<frt_type>").append("<![CDATA[" + frtType +"]]>").append("</frt_type>");
						valueXmlString.append("<frt_type__o>").append("<![CDATA[" + frtType +"]]>").append("</frt_type__o>");
						valueXmlString.append("<no_art>").append("<![CDATA[" + noArt +"]]>").append("</no_art>");
						valueXmlString.append("<no_art__o>").append("<![CDATA[" + noArt +"]]>").append("</no_art__o>");
						valueXmlString.append("<remarks>").append("<![CDATA[" + remarks +"]]>").append("</remarks>");
						if(gpDate != null)
						{
						valueXmlString.append("<gp_date__o>").append("<![CDATA[" + simpleDateFormat.format(gpDate) +"]]>").append("</gp_date__o>");
						valueXmlString.append("<gp_date>").append("<![CDATA[" + simpleDateFormat.format(gpDate) +"]]>").append("</gp_date>");
						}
						else
						{
							valueXmlString.append("<gp_date__o>").append("<![CDATA[]]>").append("</gp_date__o>");
							valueXmlString.append("<gp_date>").append("<![CDATA[]]>").append("</gp_date>");
						}
						
						valueXmlString.append("<gp_no__o>").append("<![CDATA[" + gpNo +"]]>").append("</gp_no__o>");
						valueXmlString.append("<gp_no>").append("<![CDATA[" + gpNo +"]]>").append("</gp_no>");
						valueXmlString.append("<gross_weight__o>").append("<![CDATA[" + grossWeight +"]]>").append("</gross_weight__o>");
						valueXmlString.append("<gross_weight>").append("<![CDATA[" + grossWeight +"]]>").append("</gross_weight>");

						sql = " select tran_name from transporter where tran_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,tranCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							descr = rs.getString(1);
							valueXmlString.append("<transporter_tran_name>").append("<![CDATA[" + descr +"]]>").append("</transporter_tran_name>");
						}
						else
						{
							valueXmlString.append("<transporter_tran_name>").append("<![CDATA[]]>").append("</transporter_tran_name>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

				}
				else
				{
					valueXmlString.append("<country_descr>").append("<![CDATA[]]>").append("</country_descr>");
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
			{
				tranCode = genericUtility.getColumnValue("tran_code", dom);
				if(tranCode != null && tranCode.trim().length() > 0 )
				{

					sql = " select tran_name from transporter where tran_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,tranCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{

						descr = rs.getString(1);
						valueXmlString.append("<transporter_tran_name>").append("<![CDATA[" + descr +"]]>").append("</transporter_tran_name>");
					}
					else
					{
						valueXmlString.append("<transporter_tran_name>").append("<![CDATA[]]>").append("</transporter_tran_name>");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				else
				{
					valueXmlString.append("<transporter_tran_name>").append("<![CDATA[]]>").append("</transporter_tran_name>");
				}
			}
			valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");
		}
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
				if(conn != null)
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
					conn.close();
				}
				conn = null;	
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}

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
}	
