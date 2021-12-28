
/********************************************************
	Title : DiststrgIC
	Date  : 03/08/2012
	Developer: Akhilesh Sikarwar

 ********************************************************/
package ibase.webitm.ejb.dis;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3
@Stateless 

public class DiststrgIC extends ValidatorEJB implements DiststrgICLocal, DiststrgICRemote
{
	//Comment By Nasruddin 07-10-16
   //GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
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

		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String errorType = "";
		String userId = "";
		String sql = "";
		String scCode = "";
		String itemSer = "";
		String effFromStr = "";
		String validUptoStr = "";
		int ctr = 0;
		int cnt = 0;
		int discPerc = 0;
		int childNodeListLength;
		java.util.Date effDate = null;
		java.util.Date validDate = null;

		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		ArrayList<String> errList = new ArrayList();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			finCommon = new FinCommon();
			validator = new ValidatorEJB();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changed By Nasruddin 07-10-16
			//SimpleDateFormat dbDateFormat = new SimpleDateFormat(genericUtility.getInstance().getDBDateFormat());
			SimpleDateFormat dbDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
             
                /* Comment By Nasruddin khan Start [16/SEP/16] Start
				if(childNodeName.trim().equalsIgnoreCase("item_ser"))
				{
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					System.out.println("itemSer----------->"+itemSer);
					if(itemSer.endsWith("-1") || itemSer.trim().length() == 0)
					{
						errCode = "VTISER";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}

				}
				    Comment By Nasruddin khan Start [16/SEP/16] END*/
				 if(childNodeName.trim().equalsIgnoreCase("sc_code"))
				{
					scCode = genericUtility.getColumnValue("sc_code", dom);
					if(scCode != null && scCode.trim().length() > 0)
					{
						sql = "select count(*) from  strg_customer where sc_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,scCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1); 
						}
						if(cnt == 0)
						{
							errCode = "VTSTRCUST1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						errCode = "VTSTRGCUST";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				else if(childNodeName.trim().equalsIgnoreCase("eff_from"))
				{
					effFromStr = genericUtility.getColumnValue("eff_from", dom);
					if (effFromStr != null && !effFromStr.matches("\\d{4}-[01]\\d-[0-3]\\d") && !effFromStr.equals("DD/MM/YY"))
					{
						effDate = dateFormat2.parse(effFromStr);
						if(editFlag.equalsIgnoreCase("A"))
						{
							scCode = genericUtility.getColumnValue("sc_code", dom);
							itemSer = genericUtility.getColumnValue("ls_item_ser", dom);
						}
						if(scCode == null)
						{
							scCode = "";
						}

						sql = "select count(1)  from disc_apr_strg where item_ser = ? and sc_code	= ? and	eff_from = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemSer);
						pstmt.setString(2,scCode);
						pstmt.setDate(3,new java.sql.Date(effDate.getTime()));
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						if(cnt > 0)
						{
							errCode = "VTDUPREC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						System.out.println("EEERROR IN EFF DATE  -------------->"+effFromStr);
						errCode = "VTDATEFF";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());

					}

				}
				else if(childNodeName.trim().equalsIgnoreCase("valid_upto"))
				{	
					Date todayDate = new Date();
					effFromStr = genericUtility.getColumnValue("eff_from", dom);
					if (effFromStr != null && !effFromStr.matches("\\d{4}-[01]\\d-[0-3]\\d") && !effFromStr.equals("DD/MM/YY"))
					{
						effDate = dateFormat2.parse(effFromStr);
					}
					validUptoStr = genericUtility.getColumnValue("valid_upto", dom);

					if (validUptoStr != null && !validUptoStr.matches("\\d{4}-[01]\\d-[0-3]\\d") && !validUptoStr.equals("DD/MM/YY"))
					{
						validDate = dateFormat2.parse(validUptoStr);

						if(validDate.compareTo(effDate) <= 0 || todayDate.compareTo(validDate) > 0)
						{
							errCode = "VEFFDTVLER";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}	
					else
					{
						errCode = "VTDATEUPTO";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				else if(childNodeName.trim().equalsIgnoreCase("disc_perc"))
				{
					discPerc = checkIntNull(genericUtility.getColumnValue("disc_perc", dom));
					if(discPerc <= 0)
					{
						errCode = "VTDISCPERC";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
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
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString +errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
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
	}

	private int checkIntNull(String columnValue) {
		if(columnValue == null || columnValue.trim().length() == 0)
		{
			return 0 ;
		}
		else
		{
			return Integer.parseInt(columnValue);
		}
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			if(xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;


	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{

		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Node parentNode1 = null ; 
		String childNodeName = null;
		String currCode = "";
		String defCrTerm = "";
		String sql = "";
		String itemSer ="";
		String scCode ="";
		String unitFr ="";
		String DescritemSer="";
		String unitDescrFr="";
		String round = "";
		String itemCode ="";
		String firstName = ""; 
		String middleName = ""; 
		String lastName = ""; 
		int ctr = 0;
		SimpleDateFormat sdf;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//Comment By Nasruddin 07-10-16
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
			finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			defCrTerm = finCommon.getFinparams("999999","DEF_CR_TERM",conn);
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
				sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				itemSer = getValueFromXTRA_PARAMS(xtraParams, "item_ser");


				sql = "select descr from itemser where item_ser = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemSer);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					DescritemSer= rs.getString(1); 
				}
				valueXmlString.append("<descr>").append("<![CDATA[" + DescritemSer +"]]>").append("</descr>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<eff_from>").append("<![CDATA[" + sdf.format(new java.util.Date())+ "]]>").append("</eff_from>");
				valueXmlString.append("<item_ser protect = \"1\">").append("<![CDATA["+itemSer+"]]>").append("</item_ser>");
			}

			if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))	
			{		
			}

			else if(currentColumn.trim().equalsIgnoreCase("sc_code"))
			{
				scCode = genericUtility.getColumnValue("sc_code", dom);
				sql = "select first_name, middle_name, last_name from strg_customer where sc_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,scCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					firstName = rs.getString("first_name"); 
					middleName = rs.getString("middle_name"); 
					lastName = rs.getString("last_name"); 
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				valueXmlString.append("<first_name>").append("<![CDATA[" + firstName +"]]>").append("</first_name>");
				valueXmlString.append("<middle_name>").append("<![CDATA[" + middleName +"]]>").append("</middle_name>");
				valueXmlString.append("<last_name>").append("<![CDATA[" + lastName +"]]>").append("</last_name>");

			}
			valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");

		}catch(Exception e)
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
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}                        
		}
		return valueXmlString.toString();
	}         
	private String errorType(Connection conn , String errorCode) throws ITMException
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
