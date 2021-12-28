package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
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
public class StockStatusWizIC extends ValidatorEJB implements StockStatusWizICLocal, StockStatusWizICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String winName = null;
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) 
			throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
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
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "", sql = "", errCode = "", errorType = "", errString = "", tranId = "", blankVar = "";
		String siteCode = "", itemCode = "", lotNo = "", lotSl = "", itemDesc = "", locType = "", qOrderNo = "", cStatus = "";
		int ctr = 0, currentFormNo = 0, cnt = 0, cnt1 = 0;
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
				tranId = checkNull(this.genericUtility.getColumnValue("tran_id", dom));
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("CURRENT COLUMN IN  VALIDATION [" + childNodeName + "]");
					if (childNodeName.equalsIgnoreCase("lot_no"))
					{
						lotNo = this.genericUtility.getColumnValue("lot_no", dom);
						System.out.println("@@@ Validation Lot No:-[" + lotNo + "]");

						if (lotNo == null || lotNo.trim().length() == 0)
						{
							errCode = "NULLLOTNO ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				valueXmlString.append("</Detail1>");
				// break;
				// case 2:

			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errStringXml.append("</Errors> </Root> \r\n");
			} else
			{
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally
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
			} catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException
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
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;

	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException
	{

		System.out.println("@@@@@@@ itemChanged called");
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Node parentNode1 = null;
		String childNodeName = null;
		String sql = "", sql1 = "", sql2 = "";
		String chgTerm = "", chgUser = "";
		int ctr = 0, cnt = 0, totCnt = 0;
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String siteCode = "", itemCode = "", lotNo = "", lotSl = "", itemDesc = "", locType = "", qOrderNo = "", cStatus = "";
		String quantity = "", invStatus = "", purcOrd = "", porcpNo = "", locCode = "",tranId="";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		
		String invStat="", invstatDescr="", itemdescr="";
		
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("In case 1 item default &&&&&&&&&&");
					valueXmlString.append("<Detail1 domID='1'>\r\n");
					valueXmlString.append("<lot_no><![CDATA[").append(" ").append("]]></lot_no>\r\n");
					valueXmlString.append("<lot_sl><![CDATA[").append(" ").append("]]></lot_sl>\r\n");
					valueXmlString.append("<line_no_sl><![CDATA[").append(" ").append("]]></line_no_sl>\r\n");
					valueXmlString.append("</Detail1>\r\n");
				}
				break;

			case 2:
				System.out.println("In case 2 item default &&&&&&&&&&");
				valueXmlString.append("<Detail2>");

				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					lotNo = genericUtility.getColumnValue("lot_no", dom1);
					lotSl = genericUtility.getColumnValue("lot_sl", dom1);		
					siteCode =  (genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
					
					/*  // comment by cpatil on 02/04/15
					sql = "select QORDER_NO,PORCP_NO,site_code,item_code from qc_order where lot_no=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lotNo);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						qOrderNo = rs.getString("QORDER_NO");
						porcpNo = rs.getString("PORCP_NO");
						siteCode = rs.getString("site_code");
						itemCode = rs.getString("item_code");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select descr from item where item_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemDesc = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("&&&&&& QC_Order_No is:- [" + qOrderNo + "]");
					System.out.println("&&&&&& PORCP_NO is:- [" + porcpNo + "]");

					sql = "select count(*)as cnt, loctype from qc_order_lots where item_code=? AND lot_no=? AND lot_sl=? "
					+ "AND qc_order=? group by loctype";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, lotNo);
					pstmt.setString(3, lotSl);
					pstmt.setString(4, qOrderNo);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						cnt = rs.getInt("cnt");
						locType = rs.getString("loctype");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (cnt > 0)
					{
						System.out.println("In if condition QC Approve/Reject count:-[" + cnt + "]");
						cnt = 0;
						if (locType.equalsIgnoreCase("A"))
						{
							cStatus = "APPROVED";
						} else
						{
							cStatus = "REJECT";
						}
					} else
					{
//						sql = "select count(*) as cnt from qc_sample_stk qss,qc_sample qs where qss.tran_id = qs.tran_id AND qss.lot_no=? "
//					+ "AND qss.lot_sl=? AND  qs.site_code=? AND qs.item_code=? AND qs.lot_no=? AND qs.qorder_no=?";
//						pstmt = conn.prepareStatement(sql);
//						pstmt.setString(1, lotNo);
//						pstmt.setString(2, lotSl);
//						pstmt.setString(3, siteCode);
//						pstmt.setString(4, itemCode);
//						pstmt.setString(5, lotNo);
//						pstmt.setString(6, qOrderNo);
//						rs = pstmt.executeQuery();
//						if (rs.next())
//						{
//							cnt = rs.getInt("cnt");
//						}
//						rs.close();
//						rs = null;
//						pstmt.close();
//						pstmt = null;									
						
						sql = "select TRAN_ID from qc_sample qs where qs.site_code=? AND qs.item_code=? AND qs.lot_no=? AND qs.qorder_no=?";
						pstmt = conn.prepareStatement(sql);
//						pstmt.setString(1, lotNo);
//						pstmt.setString(2, lotSl);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCode);
						pstmt.setString(3, lotNo);
						pstmt.setString(4, qOrderNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							tranId = rs.getString("TRAN_ID");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select count(*) as cnt from qc_sample_stk qss where qss.tran_id =? AND qss.lot_no=? AND qss.lot_sl=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						pstmt.setString(2, lotNo);
						pstmt.setString(3, lotSl);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt("cnt");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						
						if (cnt > 0)
						{
							System.out.println("In if QC sampling Count:-[" + cnt + "]");
							cStatus = "SAMPLED";
						} 
						else
						{
							sql1 = "select count(*) as cnt from stock where site_code=? AND item_code=? AND lot_no=? AND lot_sl=?"
						+ " AND quantity > 0 AND inv_stat='TEST'";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, siteCode);
							pstmt1.setString(2, itemCode);
							pstmt1.setString(3, lotNo);
							pstmt1.setString(4, lotSl);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								cnt = rs1.getInt("cnt");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							if (cnt > 0)
							{
								cStatus = "ON TEST";
							} else
							{
								cnt = 0;
								sql = "SELECT LOC_CODE FROM porcpdet WHERE tran_id=? AND item_code=? AND lot_no=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, porcpNo);
								pstmt.setString(2, itemCode);
								pstmt.setString(3, lotNo);
								rs = pstmt.executeQuery();
								while (rs.next())
								{
									locCode = rs.getString("LOC_CODE");

									sql1 = "select count(*) as cnt from stock where site_code=? AND item_code=? AND lot_no=? AND lot_sl=? "
									+ "AND loc_code=? AND quantity > 0";
									pstmt1 = conn.prepareStatement(sql1);
									pstmt1.setString(1, siteCode);
									pstmt1.setString(2, itemCode);
									pstmt1.setString(3, lotNo);
									pstmt1.setString(4, lotSl);
									pstmt1.setString(5, locCode);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										cnt += rs1.getInt("cnt");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if (cnt > 0)
								{
									cStatus = "RECEIVED";
								} else
								{
									cStatus = "QUARANTINE";
								}
							}
						}
					}
					valueXmlString.append("<status><![CDATA[" + cStatus + "]]></status>");
					valueXmlString.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
					valueXmlString.append("<descr><![CDATA[" + itemDesc + "]]></descr>");
					valueXmlString.append("<qorder_no><![CDATA[" + qOrderNo + "]]></qorder_no>");
					valueXmlString.append("<site_code><![CDATA[" + siteCode + "]]></site_code>");
					valueXmlString.append("<lot_no><![CDATA[" + lotNo + "]]></lot_no>");
					valueXmlString.append("<lot_sl><![CDATA[" + lotSl + "]]></lot_sl>");
					*/
					
					//added by cpatil on 02/04/15
					
					sql = " select a.inv_stat,c.descr invstatDescr,A.item_code,b.descr itemdescr " +
						  " from stock a,item b,invstat c where a.item_code=b.item_code " +
						  " and a.inv_stat=c.inv_stat and a.site_code = ? and a.lot_no = ? " +
						  " and a.lot_sl = ? and a.quantity > 0  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, lotNo);
					pstmt.setString(3, lotSl);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						invStat = checkNull( rs.getString("inv_stat") );
						invstatDescr = checkNull( rs.getString("invstatDescr") );
						itemCode = checkNull( rs.getString("item_code") );
						itemdescr = checkNull( rs.getString("itemdescr") );
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					valueXmlString.append("<status><![CDATA[" + invStat + "( "+invstatDescr+" )"  + "]]></status>");
					valueXmlString.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
					valueXmlString.append("<descr><![CDATA[" + itemdescr + "]]></descr>");
					valueXmlString.append("<qorder_no><![CDATA[]]></qorder_no>");
					valueXmlString.append("<site_code><![CDATA[" + siteCode + "]]></site_code>");
					valueXmlString.append("<lot_no><![CDATA[" + lotNo + "]]></lot_no>");
					valueXmlString.append("<lot_sl><![CDATA[" + lotSl + "]]></lot_sl>");
					
				}
				valueXmlString.append("</Detail2>");
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
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
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

	private String checkNull(String str)
	{
		if (str == null)
		{
			return "";
		} else
		{
			return str;
		}
	}

	private String errorType(Connection conn, String errorCode) throws ITMException
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
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		} finally
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
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return msgType;
	}
}
