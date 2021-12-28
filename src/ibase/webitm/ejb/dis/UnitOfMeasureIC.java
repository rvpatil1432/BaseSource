package ibase.webitm.ejb.dis;



import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless

public class UnitOfMeasureIC extends ValidatorEJB implements UnitOfMeasureICRemote, UnitOfMeasureICLocal {

	E12GenericUtility genericUtility =  new E12GenericUtility();
	  ITMDBAccessEJB itmdbAccessEJB = new ITMDBAccessEJB();
	  
	  public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

			String rtStr = "";
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			try {
				System.out.println("wfValdata string :::::");
				System.out.println("::: xmlString" + xmlString);
				System.out.println("::: xmlString1" + xmlString1);
				System.out.println("::: xmlString2" + xmlString2);

				if (xmlString != null && xmlString.trim().length() > 0) {
					dom = genericUtility.parseString(xmlString);
				}
				if (xmlString1 != null && xmlString1.trim().length() > 0) {
					dom1 = genericUtility.parseString(xmlString1);
				}
				if (xmlString2 != null && xmlString2.trim().length() > 0) {
					dom2 = genericUtility.parseString(xmlString2);
				}
				rtStr = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			} catch (Exception e) {
				System.out.println(":::" + this.getClass().getSimpleName() + "::" + e.getMessage());
				e.getMessage();
				throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
			}
			return rtStr;
		}
	  @Override
		public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

			String errString = "";
			String sql = "", unit = "", descr = "";
			Connection conn = null;
			String userId = "";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			NodeList parentNodeList = null, childNodeList = null;
			Node parentNode = null, childNode = null;
			int ctr = 0, childNodeLength = 0, currentFormNo = 0;
			String childNodeName = "";
			try {
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
				if (objContext != null && objContext.trim().length() > 0) {
					currentFormNo = Integer.parseInt(objContext);
				}
				System.out.println("in wfValdata doc :::::");
				switch (currentFormNo) 
				{
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("unit"))
						{
							unit = genericUtility.getColumnValue("unit", dom);
							unit = unit == null ? "" : unit.trim();
							System.out.println("unit ::::" + unit);
							if(unit.isEmpty())
							{
								errString = itmdbAccessEJB.getErrorString("unit", "VTCRTERM", userId);
								return errString;
							}
							else if(editFlag.equalsIgnoreCase("A"))
							{
								int count = 0;
								if(unit !=null && unit.trim().length() > 0)
								{
									sql = "select count(*) as count from uom where unit = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, unit);
									rs = pstmt.executeQuery();
									if(rs.next()){
										count = rs.getInt("count");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(count > 0){
										errString = itmdbAccessEJB.getErrorString("unit", "VMPMKY", userId);
										return errString;
									}
								}
							}
						}
						if(childNodeName.equalsIgnoreCase("descr"))
						{
							descr = genericUtility.getColumnValue("descr", dom);
							descr = descr == null ? "" : descr.trim();
							System.out.println("Description :::" + descr);
							if(descr.isEmpty()){
								errString = itmdbAccessEJB.getErrorString("descr", "VMDESCR", userId);
								return errString;
							}
							else
							{
								int count = 0;
								sql = "select count(*) as count from uom where unit != ? and trim(descr) = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unit);
								pstmt.setString(2, descr.trim());
								rs = pstmt.executeQuery();
								if(rs.next()){
									count = rs.getInt("count");
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(count > 0)
								{
									errString = itmdbAccessEJB.getErrorString("descr", "VMDUPDESCR", userId);
									return errString;
								}
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
				e.printStackTrace();
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
					if(pstmt != null){
						pstmt.close();
						pstmt = null;
					}
					if(conn != null){
						conn.close();
						conn = null;
					}
				}catch(Exception e1){
					System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e1.getMessage());
					e1.printStackTrace();	
				}
			}
			return errString;
			}
	  
	  public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, 
				String xtraParams) throws RemoteException, ITMException {
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			String rtStr = "";
			E12GenericUtility genericUtility = new E12GenericUtility();
			System.out.println("In Itemchange String:::");

			try {
				System.out.println("::: xmlString" + xmlString);
				System.out.println("::: xmlString1" + xmlString1);
				System.out.println("::: xmlString2" + xmlString2);
				
				if (xmlString != null && xmlString.trim().length() > 0) {
					dom = genericUtility.parseString(xmlString);
				}
				if (xmlString1 != null && xmlString1.trim().length() > 0) {
					dom1 = genericUtility.parseString(xmlString1);
				}
				if (xmlString2 != null && xmlString2.trim().length() > 0) {
					dom2 = genericUtility.parseString(xmlString2);
				}
				rtStr = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			} catch (Exception e) {
				System.out.println(":::" + this.getClass().getSimpleName() + "::" + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
			}
			return rtStr;
		}
	  public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag,
				String xtraParams) throws RemoteException, ITMException {
			
			Connection conn = null;
			String round = "";
			StringBuffer valueXmlString = new StringBuffer();
			int currentFormNo = 0;

			try {
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
				valueXmlString.append(editFlag).append("</editFlag></header>");
				String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				System.out.println("loginsitecode.....=" + loginSiteCode);
				String unit="",descr="",shDescr="",udfStr=""; //added by manish mhatre on 9-dec-2019
				int decPlaces=0;         //added by manish mhatre on 9-dec-2019
				double roundTo=0.0D;     //added by manish mhatre on 9-dec-2019
				String  roundToStr="",decPlacesStr="";   //added by manish mhatre on 9-dec-2019
				if (objContext != null && objContext.trim().length() > 0) {
					currentFormNo = Integer.parseInt(objContext);
				}
				System.out.println("itemchange document ::::");
				switch (currentFormNo) {
				case 1:
					valueXmlString.append("<Detail1>");
					System.out.println("currentColumn: " + currentColumn);
					if (currentColumn != null) {
						//added by manish mhatre on 9-dec-2019
						//start manish
						if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
						{
							unit = checkNull(genericUtility.getColumnValue("unit", dom));
							descr = checkNull(genericUtility.getColumnValue("descr", dom));
							shDescr = checkNull(genericUtility.getColumnValue("sh_descr", dom));
							decPlacesStr= genericUtility.getColumnValue("dec_opt", dom);
							if(decPlacesStr != null && decPlacesStr.trim().length()>0)
							{
								decPlaces=Integer.parseInt(decPlacesStr);
							}
							else
							{
								decPlaces=0;
							}
							roundToStr = genericUtility.getColumnValue("round_to", dom);
							if(roundToStr != null && roundToStr.trim().length()>0)
							{
								roundTo=Double.parseDouble(roundToStr);
							}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
							else
							{
								roundTo=0.0;
							}
							udfStr = checkNull(genericUtility.getColumnValue("udf_str2", dom));
						
							valueXmlString.append("<unit >").append("<![CDATA[" + unit + "]]>").append("</unit>");
							valueXmlString.append("<descr>").append("<![CDATA[" + descr + "]]>").append("</descr>");
							valueXmlString.append("<sh_descr>").append("<![CDATA[" + shDescr + "]]>").append("</sh_descr>");
							valueXmlString.append("<dec_opt>").append("<![CDATA[" + decPlaces + "]]>").append("</dec_opt>");
							valueXmlString.append("<round_to>").append("<![CDATA[" + roundTo + "]]>").append("</round_to>");
							valueXmlString.append("<udf_str2>").append("<![CDATA[" + udfStr + "]]>").append("</udf_str2>");
						}//end manish

						else if(currentColumn.equalsIgnoreCase("round")){
							round = genericUtility.getColumnValue("round", dom);
							round = round == null ? "" : round.trim();
							System.out.println(":::Round :: " + round);

							if(round.equalsIgnoreCase("N") || round.isEmpty()){

								valueXmlString.append("<round_to  protect = \"1\"><![CDATA[0]]></round_to>");

							}else{

								valueXmlString.append("<round_to  protect = \"0\"><![CDATA[0]]></round_to>");

							}
						}
						//added by manish mhatre on 9-dec-2019
						//start manish
						else if(currentColumn.trim().equalsIgnoreCase("dec_opt"))
						{
							decPlacesStr= E12GenericUtility.checkNull(getColumnValue("dec_opt", dom));
							if(decPlacesStr != null && decPlacesStr.trim().length()>0)
							{
								decPlaces=Integer.parseInt(decPlacesStr);
							}
							else
							{
								decPlaces=0;
							}
							System.out.println("dec>>>>>"+decPlaces);
							if(decPlaces == 0)
							{
								valueXmlString.append("<round_to><![CDATA[0]]></round_to>");
							}
							else if(decPlaces == 1)
							{
								valueXmlString.append("<round_to><![CDATA[0.1]]></round_to>");
							}
							else if(decPlaces == 2)
							{
								valueXmlString.append("<round_to><![CDATA[0.01]]></round_to>");
							}
							else if(decPlaces == 3)
							{
								valueXmlString.append("<round_to><![CDATA[0.001]]></round_to>");
							}
							else if(decPlaces == 4)
							{
								valueXmlString.append("<round_to><![CDATA[0.0001]]></round_to>");
							}
							else if(decPlaces == 5)
							{
								valueXmlString.append("<round_to><![CDATA[0.00001]]></round_to>");
							}
							else if(decPlaces == 6)
							{
								valueXmlString.append("<round_to><![CDATA[0.000001]]></round_to>");
							}
							else if(decPlaces == 7)
							{
								valueXmlString.append("<round_to><![CDATA[0.0000001]]></round_to>");
							}

						}  //end manish

					}
					System.out.println(":::::generated xml" + valueXmlString.toString());
					valueXmlString.append("</Detail1>\r\n");
				}
			}catch(Exception e){
				System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
			}finally {
				try {
					if (conn != null)
						conn.close();
					conn = null;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			valueXmlString.append("</Root>\r\n");
			System.out.println("ValueXmlString:::::" + valueXmlString.toString());
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
}
