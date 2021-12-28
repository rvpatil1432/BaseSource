/********************************************************
        Title : ItemAcctAnalysisIC
        Date  : 28/02/13

 ********************************************************/
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
@Stateless
public class ItemAcctAnalysisIC extends ValidatorEJB

implements ItemAcctAnalysisICLocal, ItemAcctAnalysisICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
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

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
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
		String itemCode = "",ditemCode = "";
		String tranId = "";   //hitemCode = "",
		String tranSer = "";
		String tranType = "";
		System.out.println("editFlag ---->>>["+editFlag+"]");
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		String analysis1__dr="",analysis2__dr="",analysis3__dr="",analysis1__cr="",analysis2__cr="",analysis3__cr="";    // added by cpatil on 11-03-13 
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
				tranId=checkNull(this.genericUtility.getColumnValue("tran_id", dom));
				System.out.println("tran id from boqdet --4-->>>>["+tranId+"]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("CURRENT COLUMN IN  VALIDATION ["+childNodeName+"]");
					if (childNodeName.equalsIgnoreCase("tran_ser"))
					{
						tranSer=this.genericUtility.getColumnValue("tran_ser", dom);
						if (tranSer == null || tranSer.trim().length()==0)
						{
							errCode = "VMTRANSR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{

							sql = "select count(*) from refser where ref_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranSer);
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
								errCode = "VMINVTSER";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("tran_type"))
					{
						tranType=checkNull(this.genericUtility.getColumnValue("tran_type", dom));
						//if (tranType != null && tranType.trim().length() > 0 )
						//						{
						//							sql = "select count(*) from  where trans_type = ?";
						//							pstmt = conn.prepareStatement(sql);
						//							pstmt.setString(1, tranType);
						//							rs = pstmt.executeQuery();
						//							if(rs.next())
						//							{
						//								cnt = rs.getInt(1);
						//							}
						//							rs.close();
						//							rs = null;
						//							pstmt.close();
						//							pstmt = null;	
						//							if(cnt == 0 ) 
						//							{
						//								errCode = "VMINVTYPE";
						//								errList.add(errCode);
						//								errFields.add(childNodeName.toLowerCase());
						//							}
						//						}

						// added by cpatil on 13/03/13 start
						if ( !( tranType.equalsIgnoreCase(" ") || tranType.trim().length() > 0 ) )
						{
							errCode = "VMNTRTYPE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// added by cpatil on 13/03/13 end
						
						/* commented by cpatil on 13/03/13 start
						{
							if(tranType==null)
							{
								errCode = "VMNTRTYPE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						*/
					}

					else if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						System.out.println("Tran id from current application ----["+tranId+"]-->>");
						System.out.println("Detail item code --on header part--> ["+ditemCode+"] and detail item code---on header part--> ["+itemCode+"]");
						// commented by cpatil on 13/03/13
						/*
						if(itemCode == null)
						{
							errCode = "VMITEMBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						*/
						// added by cpatil start
						if ( !( itemCode.equalsIgnoreCase(" ") || itemCode.trim().length() > 0 ) )
						{
							errCode = "VMITEMBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// added by cpatil end
						else
						{
							if( (itemCode != null && itemCode.trim().length() > 0 ))
							{
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
									errCode = "VMITEM1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}

					}
					
					// added by cpatil on 11-03-13 start
					else if (childNodeName.equalsIgnoreCase("analysis1__dr"))
					{
						analysis1__dr=checkNull(this.genericUtility.getColumnValue("analysis1__dr", dom));
						analysis2__dr=checkNull(this.genericUtility.getColumnValue("analysis2__dr", dom));
						analysis3__dr=checkNull(this.genericUtility.getColumnValue("analysis3__dr", dom));
						analysis1__cr=checkNull(this.genericUtility.getColumnValue("analysis1__cr", dom));
						analysis2__cr=checkNull(this.genericUtility.getColumnValue("analysis2__cr", dom));
						analysis3__cr=checkNull(this.genericUtility.getColumnValue("analysis3__cr", dom));
						System.out.println("@@@@ analysis1__dr["+analysis1__dr+"]::analysis2__dr["+analysis2__dr+"]::analysis3__dr["+analysis3__dr+"]");
						System.out.println("@@@@ analysis1__cr["+analysis1__cr+"]::analysis2__cr["+analysis2__cr+"]::analysis3__cr["+analysis3__cr+"]");					
						
						if ((analysis1__dr == null && analysis2__dr == null && analysis3__dr == null && analysis1__cr == null && analysis2__cr == null && analysis3__cr== null  ) || ( (analysis1__dr.trim().length() == 0 && analysis2__dr .trim().length() == 0 && analysis3__dr .trim().length() == 0 && analysis1__cr .trim().length() == 0 && analysis2__cr .trim().length() == 0 && analysis3__cr.trim().length() == 0  ) ) )
						{
							errCode = "VMANADRCRN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
					}
					// added by cpatil on 11-03-13 start

				}

				valueXmlString.append("</Detail1>");
				break;
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
		System.out.println("VALUE HELLO PRINTA["+valueXmlString+"]");
		return valueXmlString;
			}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		System.out.println("START PRINT ");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";
		String currCode = "";
		String shName = "";
		String salesPers = "";
		String indentNo = "";
		String descrItem = "";
		String itemCode = "";
		//String packCode = "";
		//String projCode = "";
		//String suppCodePref = "";
		//String descrCurr = "";
		//String descrProjCode = "";
		//String itemSer = "";
		//String unit = "";
		//String unitRate = "";
		//String unitPur ="";
		//String descrPackCode = "";
		String status = "";
		String loginSite = "";
		String unitStd="";
		String descr = "";
		String siteCode = "";
		int ctr = 0;
		int currentFormNo = 0;
		//double  quantity = 0.0;
		//double stdExrt = 0.0;
		//double amount = 0.0;
		//double amountBc = 0.0;
		//double rate = 0.0;
		//double quantityStduom = 0.0 ;
		//double rateStduom = 0.0 ;
		//double convQtuomStduom = 0.0;

		int childNodeListLength = 0;
		java.util.Date statusDate = null;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		//DistCommon distCommon = new DistCommon();
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
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
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
			System.out.println("CURRENT COLUMN ["+currentColumn+"]");

			if (currentColumn.trim().equalsIgnoreCase("item_code"))
			{
				itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
				if(itemCode != null && itemCode.trim().length() > 0 )
				{
					sql = "select descr from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descrItem = checkNull(rs.getString("descr") ==null?"":rs.getString("descr"));
						System.out.println("descrItem ---->>>["+descrItem +"]");
						valueXmlString.append("<descr>").append("<![CDATA["+descrItem+"]]>").append("</descr>");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				else
				{
					valueXmlString.append("<descr>").append("<![CDATA[ ]]>").append("</descr>");
				}

			}
			valueXmlString.append("</Detail1>");
			//break;

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

