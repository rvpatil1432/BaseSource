/********************************************************
        Title : ItemLotPackSizeIC
        Date  : 16/08/12
        Developer: Akhilesh Sikarwar

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ItemLotPackSizeIC extends ValidatorEJB implements ItemLotPackSizeICLocal, ItemLotPackSizeICRemote
{
	//changed by nasruddin 07-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	String winName = null;
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

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

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String sql = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String itemCode = "";
		String lotNofrom = "";
		String lotNoTo = "";
		String unitPack = "";
		String siteCodeMfg = "";
		String siteCodeOwn = "";
		double netWeight = 0.0;
		double grossWeight = 0.0;
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;

		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();


		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{ 
			this.finCommon = new FinCommon();
			this.validator = new ValidatorEJB();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
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
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					System.out.println("CURRENT COLUMN IN  VALIDATION ["+childNodeName+"]");
					if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode=this.genericUtility.getColumnValue("item_code", dom);
						//Changed By Nasruddin 19-SEP-16 START
						//if (itemCode != null && itemCode.trim().length() > 0 )
						//{
						
							sql = "select count(*) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	


							if(cnt == 0 ) 
							{
								errCode = "VTITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						/*}else
						//{
							errCode = "VMITEMCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}//Changed By Nasruddin 19-SEP-16 END*/
					}	
					/*
					 * else if (childNodeName.equalsIgnoreCase("lot_no__from"))
					{
						lotNofrom=this.genericUtility.getColumnValue("lot_no__from", dom);
						if (lotNofrom == null || lotNofrom.trim().length() == 0 )
						{
							errCode = "VTNULLOTE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}				

					else if (childNodeName.equalsIgnoreCase("lot_no__to"))
					{
						lotNoTo=this.genericUtility.getColumnValue("lot_no__to", dom);
						if (lotNoTo == null || lotNoTo.trim().length() == 0 )
						{
							errCode = "VTNULLOTE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}	//Changed By Nasruddin 19-SEP-16 END*/	
					else if (childNodeName.equalsIgnoreCase("unit__pack"))
					{
						unitPack=checkNull(this.genericUtility.getColumnValue("unit__pack", dom));	

						sql = "select count(*) from uom where unit = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, unitPack);
						rs = pstmt.executeQuery();
						if(rs.next())
						{	
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	

						if(cnt == 0)
						{
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
				/*	Comment By Nasruddin 19-SEP-16 STARt
				  else if (childNodeName.equalsIgnoreCase("site_code__mfg"))
					{
						siteCodeMfg=this.genericUtility.getColumnValue("site_code__mfg", dom);
						if (siteCodeMfg == null || siteCodeMfg.trim().length() == 0 )
						{
							errCode = "VMSITECD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}else
						{
							sql = "SELECT COUNT(*) FROM site WHERE site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCodeMfg);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							if(cnt == 0) 
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}                                                                        
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

					}
					else if (childNodeName.equalsIgnoreCase("site_code__own"))
					{
						siteCodeOwn=this.genericUtility.getColumnValue("site_code__own", dom);
						if (siteCodeOwn == null || siteCodeOwn.trim().length() == 0 )
						{
							errCode = "VMSITECD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}else
						{
							sql = "SELECT COUNT(*) FROM site WHERE site_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCodeOwn);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							if(cnt == 0) 
							{
								errCode = "VMSITE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}                                                                        
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}

					}
					Comment By Nasruddin 19-SEP-16 END*/
					else if (childNodeName.equalsIgnoreCase("net_weight"))
					{
						netWeight=checkDoubleNull(this.genericUtility.getColumnValue("net_weight", dom));
						grossWeight=checkDoubleNull(this.genericUtility.getColumnValue("gross_weight", dom));

						if(grossWeight < netWeight)
						{
							errCode = "VTGRNET";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}


					valueXmlString.append("</Detail1>");
				}

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

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)
	throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("hELLO PRINT");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("VALUE HELLO PRINT["+valueXmlString+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINT----->["+valueXmlString+"]");
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
	throws RemoteException, ITMException
	{
		System.out.println("sTART PRINT ");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";
		String unitPack = "";
		String itemCode = "";
		String descr ="";
		String unit = "";
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		ArrayList convQtyFactList = new ArrayList();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy");
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// changed by nasruddin 07-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			this.finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
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
				childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						childNode.getFirstChild();
					}

					ctr++;
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN ---------->["+currentColumn+"]");

				if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					sql = "Select descr,unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode );
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = rs.getString("descr")==null?"":rs.getString("descr");
						unit = rs.getString("unit")==null?"":rs.getString("unit");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					valueXmlString.append("<item_descr protect = \"1\">").append("<![CDATA["+descr+"]]>").append("</item_descr>");
					valueXmlString.append("<unit__inner_label protect = \"1\">").append("<![CDATA["+unit+"]]>").append("</unit__inner_label>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("unit__pack"))
				{
					unitPack = checkNull(genericUtility.getColumnValue("unit__pack", dom));
					sql = "select descr from uom where unit = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitPack);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						descr = rs.getString(1)==null?"":rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<uom_descr protect = \"1\">").append("<![CDATA["+descr+"]]>").append("</uom_descr>");

				}

				valueXmlString.append("</Detail1>");

			}
			valueXmlString.append("</Root>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					if (pstmt != null)
						pstmt.close();
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}

	private double checkDoubleNull(String str)
	{
		if(str == null || str.trim().length() == 0)
		{
			return 0.0;
		}
		else
		{
			return Double.parseDouble(str) ;
		}

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
}

