/********************************************************
        Title : TaxRateIC
        Date  : 18/12/12
        Developer: Akhilesh Sikarwar

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
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class TaxRateIC extends ValidatorEJB
implements TaxRateICLocal, TaxRateICRemote
{
	//changed by nasruddin 05-10-16
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

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String cctrCode = "";
		String errCode = "";
		String errorType = "";
		String errString = "";
		String siteCode = "";
		String taxCode = "", taxSet ="";
		String taxClass = "";
		String sql = "";
		String taxChap = "";
		String taxBase = "";
		String acctCode = "";
		String active = "";
		String slabBase = "";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		java.util.Date effFrom = null;
		java.util.Date TodayDate = null;
		java.util.Date validUpto = null;
		java.util.Date recdDate = null;
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		FinCommon finCommon = new FinCommon();
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
					if (childNodeName.equalsIgnoreCase("tax_code"))
					{
						taxCode=checkNull(this.genericUtility.getColumnValue("tax_code", dom));
						sql="Select Count(*) from tax where tax_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, taxCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						if(cnt==0)
						{
							errCode = "VTTAX1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxClass=checkNull(this.genericUtility.getColumnValue("tax_class", dom));
						
						sql="Select Count(*)  from taxclass where tax_class = ?";
						if(taxClass.trim().length() > 0)//cHANGED bY nasruddin 21-SEP16
						{
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,taxClass);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);;

							}
							if(cnt==0)
							{
								errCode = "VTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					
					}
					/*    Comment By Nasruddin End 21-SEP-16 START
					else if(childNodeName.equalsIgnoreCase("slab_base"))
					{
						slabBase=this.genericUtility.getColumnValue("slab_base", dom);
						if(slabBase == null || slabBase.trim().length() == 0)
						{
							errCode = "VTTCLASS1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
					}
                     Comment By Nasruddin End 21-SEP-16 END*/
					else if(childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap=checkNull(this.genericUtility.getColumnValue("tax_chap", dom));
						if(taxChap.trim().length() > 0)//cHANGED bY nasruddin 21-SEP16
						{
							sql="Select Count(*)   from taxchap where tax_chap = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,taxChap);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);;

							}
							if(cnt==0)
							{
								errCode = "VTTCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_base"))
					{
						taxBase=checkNull(this.genericUtility.getColumnValue("tax_base", dom));
						if(taxBase.trim().length() == 0)
						{
							errCode = "VTTAXBASE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql="Select Count(*) from taxbase where tax_base = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,taxBase);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);;

							}
							if(cnt==0)
							{

								errCode = "VTTBASE1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}

					}
					// Changed By Nasruddin 21-SEP-16 START 
					else if(childNodeName.equalsIgnoreCase("acct_code__reco") || childNodeName.equalsIgnoreCase("acct_code") ||  childNodeName.equalsIgnoreCase("acct_code__revr"))
					//else if(childNodeName.equalsIgnoreCase("acct_code__reco") || childNodeName.equalsIgnoreCase("acct_code"))
					{
						if(childNodeName.equalsIgnoreCase("acct_code__reco"))
						{
							acctCode = checkNull(this.genericUtility.getColumnValue("acct_code__reco", dom));
						}
						else if(childNodeName.equalsIgnoreCase("acct_code"))
						{
							acctCode= checkNull(this.genericUtility.getColumnValue("acct_code", dom));
						}
						else if(childNodeName.equalsIgnoreCase("acct_code__revr"))
						{
							acctCode= checkNull(this.genericUtility.getColumnValue("acct_code__revr", dom));
						}
						if(acctCode != null && acctCode.trim().length() > 0)
						{
							sql = "select count(*)  from accounts where acct_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCode);				
							rs =pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							if(cnt >0)
							{
								sql = "select active from accounts where acct_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, acctCode);				
								rs =pstmt.executeQuery();
								if(rs.next())
								{
									active = rs.getString(1);
								}
								if(!active.equalsIgnoreCase("Y"))
								{		                
									errCode = "VMACCTA";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else
							{
								errCode = "VMACCTCDX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					// Changed By Nasruddin 21-SEP-16
					else if(childNodeName.equalsIgnoreCase("cctr_code__reco") || childNodeName.equalsIgnoreCase("cctr_code")  || childNodeName.equalsIgnoreCase("cctr_code__revr"))
					//else if(childNodeName.equalsIgnoreCase("cctr_code__reco") || childNodeName.equalsIgnoreCase("cctr_code"))
					{
						if(childNodeName.equalsIgnoreCase("cctr_code__reco"))
						{
							cctrCode = checkNull(this.genericUtility.getColumnValue("cctr_code__reco", dom));
						}
						else if(childNodeName.equalsIgnoreCase("cctr_code"))
						{
							cctrCode= checkNull(this.genericUtility.getColumnValue("cctr_code", dom));
						}
						else if(childNodeName.equalsIgnoreCase("cctr_code"))
						{
							cctrCode= checkNull(this.genericUtility.getColumnValue("cctr_code__revr", dom));
						}

						if(acctCode != null && acctCode.trim().length() > 0)
						{
							sql = "select count(*)  from costctr where cctr_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCode);				
							rs =pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							if(cnt ==0)
							{

								errCode = "VMCCTRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.trim().equalsIgnoreCase("eff_from"))
					{
						TodayDate = new Date();
						if(this.genericUtility.getColumnValue("eff_from", dom) != null &&  !this.genericUtility.getColumnValue("eff_from", dom).equals("DD/MM/YY"))
						{
							effFrom = dateFormat2.parse(this.genericUtility.getColumnValue("eff_from", dom));

							if(effFrom.compareTo(TodayDate) >= 0)
							{
								errCode = "VTLICEFTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}
					else if (childNodeName.trim().equalsIgnoreCase("valid_upto"))
					{
						if(this.genericUtility.getColumnValue("valid_upto", dom) != null &&  !this.genericUtility.getColumnValue("valid_upto", dom).equals("DD/MM/YY"))
						{
							effFrom = dateFormat2.parse(this.genericUtility.getColumnValue("eff_from", dom));
							validUpto = dateFormat2.parse(this.genericUtility.getColumnValue("valid_upto", dom));


							if(effFrom.compareTo(validUpto) > 0)
							{
								errCode = "VTLICVLDUP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				valueXmlString.append("</Detail1>");
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
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
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
		String city = "";
		String descr ="";
		String licenceDscr = "";
		String itemCode = "";
		String suppCodeMnfr = "";
		String suppName = "";
		String suppCode = "";
		String licenceNo = "";
		String itemDescr = "";
		String refNo = "";
		String taxSet = "";
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//changed by nasruddin 05-10-16
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
				System.out.println("CURRENT COLUMN ["+currentColumn+"]");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					taxSet = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "tax_set"));
					valueXmlString.append("<tax_set>").append("<![CDATA["+taxSet+"]]>").append("</tax_set>");
				}


				valueXmlString.append("</Detail1>");
				break;			

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

