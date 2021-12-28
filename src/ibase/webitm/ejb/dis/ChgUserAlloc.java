
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ChgUserAlloc extends ValidatorEJB implements ChgUserAllocLocal, ChgUserAllocRemote {

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String userId = null;
	String chgUser = null;
	String chgTerm = null;
	NumberFormat nf = null;
	boolean isError=false;



	public ChgUserAlloc() 
	{
		System.out.println("^^^^^^^ inside ChgUserAlloc Wizard ^^^^^^^");
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside ChgUserAlloc Wizard >^^^^^^^");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";

		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			if (objContext != null && Integer.parseInt(objContext) == 1) 
			{
				parentNodeList = dom2.getElementsByTagName("Header0");
				parentNode = parentNodeList.item(1);
				childNodeList = parentNode.getChildNodes();
				for (int x = 0; x < childNodeList.getLength(); x++) 
				{
					childNode = childNodeList.item(x);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("Detail1")) 
					{
						errString = wfValData(dom, dom1, dom2, "1", editFlag, xtraParams);
						if (errString != null && errString.trim().length() > 0)
							break;
					} else if (childNodeName.equalsIgnoreCase("Detail2")) 
					{
						errString = wfValData(dom, dom1, dom2, "2", editFlag, xtraParams);
						break;
					}
				}
			} else 
			{
				errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			}
		} catch (Exception e) {
			System.out.println("Exception : Inside ChgUserAlloc wfValData Method ..> " + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside ChgUserAlloc wfValData >^^^^^^^");
		//GenericUtility genericUtility;
		E12GenericUtility genericUtility;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0, currentFormNo = 0, childNodeListLength = 0, cnt = 0, cnt2 = 0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		Connection conn = null;
		String userId = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String sql = "";

		String distOrderNo = "", chguser = "", chgUserNew = "", issDspNo = "";
		try {

			System.out.println("editFlag>>>>wf"+editFlag);
			System.out.println("xtraParams>>>wf"+xtraParams);


			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//genericUtility = GenericUtility.getInstance();
			 genericUtility= new  E12GenericUtility();
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {


					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("dist_order"))
					{

						distOrderNo = genericUtility.getColumnValue("dist_order",dom);

						if (distOrderNo == null || distOrderNo.trim().length() == 0)
						{
							errCode = "DISISSNULL";
							errString = getErrorString("dist_order",errCode,userId);
							break;
						}
						else
						{

							sql = "select count(1) from distorder dord join distord_iss diss on dord.dist_order = diss.dist_order and dord.dist_order = ? and dord.status = 'P' and dord.confirmed = 'Y'";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrderNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							sql = "select count(1) from sorder sord join despatch desp on sord.sale_order = desp.sord_no and sord.status = 'P' and sord.confirmed = 'Y' and sord.sale_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrderNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt2 = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							if(cnt == 0 && cnt2 == 0)
							{
								errCode = "DISNOTEX";
								errString = getErrorString("dist_order",errCode,userId);
								break;
							}
						}

					}
					// Added by mahesh on 26-09-2014 
					else if(childNodeName.equalsIgnoreCase("iss_dsp_no")){
						
						issDspNo = genericUtility.getColumnValue("iss_dsp_no",dom);

						if (issDspNo == null || issDspNo.trim().length() == 0)
						{
							errCode = "ISSDSPNULL";
							errString = getErrorString("iss_dsp_no",errCode,userId);
							break;
						}
						else
						{

							sql = "select count(1) from distord_iss diss join distorder dord on dord.dist_order = diss.dist_order and dord.dist_order = ? and diss.tran_id = ? and (diss.confirmed is null or diss.confirmed = 'N') ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrderNo);
							pstmt.setString(2,issDspNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							sql = "select count(1) from sorder sord join despatch desp on sord.sale_order = desp.sord_no and sord.sale_order = ? and desp.desp_id = ? and (desp.confirmed is null or desp.confirmed = 'N') ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrderNo);
							pstmt.setString(2,issDspNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt2 = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							if(cnt == 0 && cnt2 == 0)
							{
								errCode = "ISDSPNOTEX";
								errString = getErrorString("iss_dsp_no",errCode,userId);
								break;
							}
						}
					}
				}
				break;
			case 2:
			/*	System.out.println("DOM>>>> Elements>>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1>> Elements>>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2>> Elements>>["+genericUtility.serializeDom(dom2).toString()+"]");	

				parentNodeList = dom2.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				System.out.println("parentNode >>>{"+parentNode+"}");
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();


				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("code"))
					{

						chguser = genericUtility.getColumnValue("code",dom2);
						chgUserNew = genericUtility.getColumnValue("chg_user",dom2);
						
						System.out.println("chguser >>>>>>>>>> "+chguser+" chgusernew >>>>>>>>>>>>> "+chgUserNew);
						
						if (distOrderNo == null || distOrderNo.trim().length() == 0)
						{
							errCode = "USERNULL";
							errString = getErrorString("code",errCode,userId);
							break;
						}
						else
						{

							sql = "select count(1) from users where code = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,chguser);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if(cnt == 0)
							{
								errCode = "USERNOTEXT";
								errString = getErrorString("code",errCode,userId);
								break;
							}else if(chguser.equals(chgUserNew)){
								errCode = "SAMEUSER";
								errString = getErrorString("code",errCode,userId);
								break;
							}
						}

					}
				}
				break;*/
			}
		} catch (Exception e) {
			e.printStackTrace();			
			errString = e.getMessage();
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
			System.out.println("currentColumn"+currentColumn);
			System.out.println("editFlag"+editFlag);
			System.out.println("xtraParams"+xtraParams);


			System.out.println("xmlString111>>"+xmlString);
			System.out.println("xmlString222>>"+xmlString1);
			System.out.println("xmlString333>>"+xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [itemChanged(String,String)] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}


	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {

		StringBuffer valueXmlString = null;
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		System.out.println("DOM111 Elements>>>********************************["+genericUtility.serializeDom(dom).toString()+"]");
		System.out.println("DOM222 Elements>>>********************************["+genericUtility.serializeDom(dom1).toString()+"]");
		System.out.println("DOM322 Elements>>>********************************["+genericUtility.serializeDom(dom2).toString()+"]");
		String sql="";
		int cnt = 0, cnt2 = 0;
		String distOrderNo = "", issDspNo = "";

		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userId");

			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO IS"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("lot sl in begin from dom ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom)+"lot sl from dom1 ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom1)+"lot sl from dom2 ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom2));
			switch (currentFormNo) {

			case 1 :
				break;

			case 2 : 
				System.out.println("DOM2 Elements["+genericUtility.serializeDom(dom2).toString()+"]");

			

				distOrderNo = genericUtility.getColumnValue("dist_order", dom1);
				issDspNo = genericUtility.getColumnValue("iss_dsp_no", dom1);
				
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						
						sql = "select count(1) from distord_iss where tran_id = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,issDspNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						
						sql = "select count(1) from despatch where desp_id = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,issDspNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt2 = rs.getInt(1);
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						
						if(cnt > 0){
							sql =   "select add_user from distord_iss where tran_id = ?";
						}else{
							sql =   "select add_user from despatch where desp_id = ?";
						}
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, issDspNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							valueXmlString.append("<Detail2  domID='1' objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
							valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
							valueXmlString.append("<chg_user><![CDATA["+(rs.getString("add_user")==null?"":rs.getString("add_user").trim())+"]]></chg_user>");
							valueXmlString.append("</Detail2>");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					}
				break;
			case 3 : 
				
				break;
			}

/*			if(("lot_sl".equalsIgnoreCase(currentColumn)))
			{ 
				System.out.println("CHK VAL");
				String currDomStr = genericUtility.serializeDom(dom);
				System.out.println("currDomStr[" + currDomStr + "]");
				StringBuffer valueXmlStr = new StringBuffer(currDomStr);
				System.out.println("@@@@@@@@@@@ after serialize : valueXmlStr ["+valueXmlStr+"]");
				StringBuffer valueXmlString1 = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
				valueXmlString1.append(editFlag).append("</editFlag></header>");
				valueXmlString1.append(valueXmlStr);
				valueXmlString = valueXmlString1;
			}
*/
			valueXmlString.append("</Root>"); 
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
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

}
