package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import javax.ejb.SessionBean;//commented for ejb3
import javax.ejb.CreateException;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.dis.DistCommon;
import java.util.ArrayList;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless;//added for ejb3

@Stateless//added for ejb3
public class DistOrderAmdIC extends ValidatorEJB implements DistOrderAmdICLocal , DistOrderAmdICRemote //SessionBean//commented for ejb3
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String userId = null;
	String loginSite = null;
	

	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String columnValue = null;
		String childNodeName = null;
		String errCode = null;
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		ResultSet rs1 = null;
		String sql1 = null;		
		PreparedStatement pstmt1=null;
		DistCommon distCommon = null;	
		Timestamp orderAmdDate = null;
	   // ConnDriver connDriver = new ConnDriver();
    	try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			distCommon = new DistCommon();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//genericUtility = GenericUtility.getInstance(); 
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						
						if ( childNodeName.equalsIgnoreCase( "dist_order" ) )
						{
							String distOrder = null;
							distOrder = genericUtility.getColumnValue( "dist_order", dom1 );
							if(	distOrder == null || distOrder.trim().length() == 0 )
							{
								errCode = "DISTORDER";
								errString = getErrorString( "dist_order", errCode, userId );
							}		
							if(	distOrder != null && distOrder.trim().length() > 0 )
							{
								
								sql = " SELECT COUNT(*) FROM distorder WHERE dist_order = ? ";
								
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,distOrder.trim());
								rs = pstmt.executeQuery();
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
								if( cnt == 0 )
								{
									errCode = "INDISTORD";
									errString = getErrorString( "dist_order", errCode, userId );
									return errString;
								}
								//ADDED BY RITESH FOR DI3HSUP004 START
								else{
								
								sql = " SELECT COUNT(*) FROM DISTORDER WHERE DIST_ORDER = ? AND STATUS NOT IN ('X','C') ";
								
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,distOrder.trim());
								rs = pstmt.executeQuery();
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
									if( cnt == 0 )
									{
										errCode = "VTDISORDXC";
										errString = getErrorString( "dist_order", errCode, userId );
										return errString;
									}
								}
								//ADDED BY RITESH FOR DI3HSUP004 END
							}
						}
						if ( childNodeName.equalsIgnoreCase( "purc_order" ) )
						{
							String purOrder = null;
							purOrder = genericUtility.getColumnValue( "purc_order", dom1 );
							
							if(	purOrder != null && purOrder.trim().length() > 0 )
							{
								sql = " SELECT COUNT(*) FROM porddet WHERE purc_order = ? ";
								
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,purOrder.trim());
								rs = pstmt.executeQuery();
								cnt = 0;
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								if( cnt == 0 )
								{
									errCode = "INPURORD";
									errString = getErrorString( "purc_order", errCode, userId );
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "order_date" ) )
						{
							String orderDate = null;
							orderDate = genericUtility.getColumnValue( "order_date", dom1 );
							if(	orderDate == null || orderDate.trim().length() == 0 )
							{
								errCode = "ORDERDBLK";
								errString = getErrorString( "order_date", errCode, userId );
							}	
						}
						if ( childNodeName.equalsIgnoreCase( "order_date_o" ) )
						{
							String orderDate = null;
							orderDate = genericUtility.getColumnValue( "order_date_o", dom1 );
							if(	orderDate == null || orderDate.trim().length() == 0 )
							{
								errCode = "ORDERDBLK";
								errString = getErrorString( "order_date_o", errCode, userId );
							}	
						}
						if ( childNodeName.equalsIgnoreCase( "site_code__ship" ) )
						{
							String siteShip = null;
							siteShip = genericUtility.getColumnValue( "site_code__ship", dom1 );
							if(	siteShip == null || siteShip.trim().length() == 0 )
							{
								errCode = "SITESHBLK";
								errString = getErrorString( "site_code__ship", errCode, userId );
							}	
						}
						if ( childNodeName.equalsIgnoreCase( "site_code__ship_o" ) )
						{
							String siteShip = null;
							siteShip = genericUtility.getColumnValue( "site_code__ship_o", dom1 );
							if(	siteShip == null || siteShip.trim().length() == 0 )
							{
								errCode = "SITESHBLK";
								errString = getErrorString( "site_code__ship_o", errCode, userId );
							}	
						}
					} //END OF FOR LOOP OF CASE1
					break;
					case 2 :
						parentNodeList = dom.getElementsByTagName("Detail2");
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						for(ctr = 0; ctr < childNodeListLength; ctr++)
						{
							cnt = 0;
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							errCode = null;
							
							if ( childNodeName.equalsIgnoreCase( "line_no_distord" ) )
							{
								String lineNoOrder = null ;
								String distOrder = null;
								lineNoOrder = genericUtility.getColumnValue( "line_no_distord", dom );
								distOrder = genericUtility.getColumnValue( "dist_order", dom1 );
									
								if(	lineNoOrder != null && lineNoOrder.trim().length() > 0 )
								{
									//	SQL CHANGE BY RITESH FOR DI3HSUP004
									sql = " SELECT COUNT(*) FROM distorder_det WHERE dist_order = ? and line_no = ? ";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,distOrder.trim());
									pstmt.setInt(2,Integer.parseInt(lineNoOrder.trim()));
									rs = pstmt.executeQuery();
									cnt = 0;
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );
									} 
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if( cnt == 0 )
									{
										errCode = "INLINENO";
										errString = getErrorString( "line_no_distord", errCode, userId );
										return errString;
									}

									sql = " SELECT COUNT(*) FROM distorder_det WHERE dist_order = ? and line_no = ? and case when status is null then 'O' else status end = 'O' ";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,distOrder.trim());
									pstmt.setInt(2,Integer.parseInt(lineNoOrder.trim()));
									rs = pstmt.executeQuery();
									cnt = 0;
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );
									} 
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if( cnt == 0 )
									{
										errCode = "VTINDISORD";
										errString = getErrorString( "line_no_distord", errCode, userId );
										return errString;
									}
								}
							}
							if ( childNodeName.equalsIgnoreCase( "dist_order" ) )
							{
								String distOrder = null;
								distOrder = genericUtility.getColumnValue( "dist_order", dom );
								if(	distOrder == null || distOrder.trim().length() == 0 )
								{
									errCode = "DISTORDER";
									errString = getErrorString( "dist_order", errCode, userId );
								}		
							}
							if ( childNodeName.equalsIgnoreCase( "tax_class" ) )
							{
								String taxClass = null;
								taxClass = genericUtility.getColumnValue( "tax_class", dom );
								if(	taxClass != null && taxClass.trim().length() > 0 )
								{
									sql = " SELECT COUNT(*) FROM taxclass WHERE tax_class = ? ";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,taxClass.trim());
									rs = pstmt.executeQuery();
									cnt = 0;
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );
									} 
									if( cnt == 0 )
									{
										errCode = "INTAXCLASS";
										errString = getErrorString( "tax_class", errCode, userId );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}
							if ( childNodeName.equalsIgnoreCase( "tax_chap" ) )
							{
								String taxChap = null;
								taxChap = genericUtility.getColumnValue( "tax_chap", dom );
								if(	taxChap != null && taxChap.trim().length() > 0 )
								{
									sql = " SELECT COUNT(*) FROM taxchap WHERE tax_chap = ? ";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,taxChap.trim());
									rs = pstmt.executeQuery();
									cnt = 0;
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );
									} 
									if( cnt == 0 )
									{
										errCode = "INTAXCHAP";
										errString = getErrorString( "tax_chap", errCode, userId );
									}
									rs.close();
									rs = null;

									pstmt.close();
									pstmt = null;
								}
							}
							if ( childNodeName.equalsIgnoreCase( "tax_env" ) )
							{
								String taxEnv = null;
								//taxEnv = genericUtility.getColumnValue( "tax_env", dom );
								taxEnv = distCommon.getParentColumnValue( "tax_env", dom, "2" );
								System.out.println("DOAmd 2 atxenv ["+taxEnv+"]");
								if(	taxEnv != null && taxEnv.trim().length() > 0 )
								{
									sql = " SELECT COUNT(*) FROM taxenv WHERE tax_env = ? ";
									pstmt = conn.prepareStatement( sql );
									pstmt.setString(1,taxEnv.trim());
									rs = pstmt.executeQuery();
									cnt = 0;
									if( rs.next() )
									{
										cnt = rs.getInt( 1 );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if( cnt == 0 )
									{
										errCode = "INTAXENV";
										errString = getErrorString( "tax_env", errCode, userId );
									}//Pavan R 17sept19 start[to validate tax environment]
									else 
									{	
										String orderDate = genericUtility.getColumnValue( "order_date", dom1 );
										if (orderDate != null && orderDate.trim().length() > 0) {
											orderAmdDate = Timestamp.valueOf(genericUtility.getValidDateString(orderDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
										}
											errCode = distCommon.getCheckTaxEnvStatus( taxEnv, orderAmdDate,"D", conn );
										if(errCode != null && errCode.trim().length() > 0) {
											errString = getErrorString( "tax_env", errCode, userId );
											return errString;
										}
									}
									//Pavan R 17sept19 end[to validate tax environment]
																	}
							} 
							 //  ADDED BY RITESH FOR DI3HSUP004 START
							if ( childNodeName.equalsIgnoreCase( "qty_order" ) )
							{
								String qtyorderstr1 = genericUtility.getColumnValue( "qty_order", dom );
								if(qtyorderstr1 == null)
								{
									errCode = "VTQTYNULL";
									errString = getErrorString( "qty_order", errCode, userId );
									return errString;
								}
								double qtyorder = 0.0;
								if(qtyorderstr1 != null)qtyorder = Double.parseDouble(qtyorderstr1);
								if(	qtyorder <= 0.0 )
								{
									errCode = "VTQTYNVE";
									errString = getErrorString( "qty_order", errCode, userId );
									return errString;
								}		
							}
						//  ADDED BY ABHIJIT FOR D15DSUN019 END
							if ( childNodeName.equalsIgnoreCase( "item_code" ) )  // ADDED BY ABHIJIT FOR D15DSUN019
							{
								String lineNoOrder1 = "" ;
								String distOrder1 = "";
								String itemcode = "";
								String itemCode = "";
								itemCode = genericUtility.getColumnValue("item_code", dom);
								System.out.println("Item code is:"+itemCode);
								lineNoOrder1 = genericUtility.getColumnValue("line_no_distord",dom);
								distOrder1 = genericUtility.getColumnValue("dist_order", dom1);
								if(itemCode == null || itemCode.length() == 0)
								{
									errCode = "WORDWIICCB";
									errString = getErrorString( "item_code", errCode, userId );
									return errString;
								}
								
								if( lineNoOrder1 != null && lineNoOrder1.length() > 0 )	
								{
								sql = " SELECT item_code FROM distorder_det WHERE dist_order = ? and line_no = ? ";
								pstmt1 = conn.prepareStatement( sql );
								pstmt1.setString(1,distOrder1.trim());
								pstmt1.setString(2,lineNoOrder1.trim());
								System.out.println("Line no Dist Order"+lineNoOrder1);
								System.out.println("Dist Order is:"+distOrder1);
								rs1 = pstmt1.executeQuery();
								if( rs1.next() )
								{
									itemcode = rs1.getString("item_code");
									System.out.println("Distribution Item code is:"+itemcode);
								} 
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								if(!itemcode.trim().equals(itemCode.trim() ))
								{
									System.out.println("@@@@@@@@@ENTER LOOP@@@@@@@");
									errCode = "VTINDRQ1";
									errString = getErrorString( "item_code", errCode, userId );
									return errString;
								}
								}
							}
							
						//  ADDED BY ABHIJIT FOR D15DSUN019 END
							 
						}//END FOR LOOP OF CASE2
			}//END SWITCH 
		}//END TRY
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException ( e );
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pstmt != null )
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
			  throw new ITMException ( d );
			}			
		}
		return errString;
	}//END OF VALIDATION 

	public String itemChanged( String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try
		{
			dom = parseString(xmlString); 
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
        return valueXmlString; 
	}
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement vPStmt = null,pstmt =  null;
		ResultSet vRs = null , rs= null;
		String sql = null;
		int currentFormNo = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = null;
		NodeList parentNodeList = null;
		Node parentNode = null; 
		Node childNode = null;
		NodeList childNodeList = null;
		String childNodeName = null;
		int childNodeListLength = 0;
		int ctr = 0;
		String  loginSite = null; 
		ArrayList List = null;
		DistCommon discommon = new DistCommon();
		double mrate= 0, mrateclg = 0;
		String taxchap="", taxclass="", taxenv="", sitecodeship = "",sitecodedlv ="",stationfr ="",stationto ="",itemser = "" ;
		String itemcode1 = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			switch (currentFormNo)
			{
				case 1:
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
					valueXmlString.append("<Detail1>");	
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					if (currentColumn.trim().equals( "itm_default" ))
					{
						String siteDescr = null;
						sql = " select descr from site WHERE site_code = ? ";
						vPStmt = conn.prepareStatement(sql);
						vPStmt.setString(1,loginSite.trim());
						vRs = vPStmt.executeQuery();
						if( vRs.next() )
						{
							siteDescr = vRs.getString("descr");
						}
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						
						valueXmlString.append("<site_code>").append("<![CDATA[" + ( loginSite != null ? loginSite.trim() : ""  )+ "]]>").append("</site_code>");
						valueXmlString.append("<site_descr>").append("<![CDATA[" + ( siteDescr != null ? siteDescr.trim() : ""  )+ "]]>").append("</site_descr>");
						valueXmlString.append("<amd_date>").append("<![CDATA[" + getCurrdateAppFormat() + "]]>").append("</amd_date>");
					}
					if (currentColumn.trim().equals( "dist_order" ))
					{
						String vSql = null;
						String distOrder = null;
						String orderDate = null;
						String siteCodeShip = null;
						String aDescr = null;
						String aAdd1 = null;
						String aAdd2 = null;
						String aCity = null;
						String aPin = null;
						String aStateCode = null;
						String siteCodeDLV = null;
						String bDescr = null;
						String bAdd1 = null;
						String bAdd2 = null;
						String bCity = null;
						String bPin = null;
						String bStateCode = null;
						String shipDate = null;
						String dueDate = null;
						String remarks = null;
						String distRoute = null;
						String priceList = null;
						String targetWgt = null;
						String targetVol = null;
						String locCodeGit = null;
						String projCode = null;
						String mrprunId = null;
						String saleOrder = null;
						String remarks1 = null;
						String remarks2 = null;
						String currCode = null;
						String orderType = null;
						String locCodeCons = null;
						String sundryType = null;
						String sundryCode = null;
						String autoReceipt = null;
						String tranType = null;
						String salesPers = null;
						String locCodeGitbf = null;
						String custCodeDlv = null;
						String dlvTo = null;
						String dlvAdd1 = null;
						String dlvAdd2 = null;
						String dlvAdd3 = null;
						String dlvCity = null;
						String stateCodeDlv = null;
						String countCodeDlv = null;
						String dlvPin = null;
						String stanCode = null;
						String tel1Dlv = null;
						String tel2Dlv = null;
						String tel3Dlv = null;
						String faxDlv = null;
						String avaliableYn = null;
						String purcOrder = null;
						String totAmt = null;
						String taxAmt = null;
						String netAmt = null;
						String tranSer = null;
						String parenTranId = null;
						String revTran = null;
						String priceListClg = null;
						String sordNo = null;
						String policyNo = null;
						String locCodeDmgd = null;
						String siteCodeBil = null;
						String cDescr = null;
						String cAdd1 = null;
						String cAdd2 = null;
						String cCity = null;
						String cPin = null;
						String tranMode = null;
						String cStateCode = null;
						double exchRate=0;
						Timestamp distDateTime = null;
						Timestamp shipDateTime = null;
						Timestamp dueDateTime = null;
						distOrder = genericUtility.getColumnValue( "dist_order", dom1 );
						if( distOrder != null && distOrder.trim().length() > 0 )
						{
							vSql = "SELECT DIST.ORDER_DATE,DIST.SITE_CODE__SHIP,S1.DESCR A_DESCR,S1.ADD1 A_ADD1, "        
								+"	S1.ADD2 A_ADD2,S1.CITY A_CITY,S1.PIN A_PIN,S1.STATE_CODE A_STATE_CODE, "
								+"	DIST.SITE_CODE__DLV,S2.DESCR B_DESCR,S2.ADD1 B_ADD1,S2.ADD2 B_ADD2, "
								+"	S2.CITY B_CITY,S2.PIN B_PIN,S2.STATE_CODE B_STATE_CODE, "
								+"	DIST.SHIP_DATE,DIST.DUE_DATE,DIST.REMARKS,DIST.DIST_ROUTE,DIST.PRICE_LIST, "
								+"	DIST.TARGET_WGT,DIST.TARGET_VOL,DIST.LOC_CODE__GIT,DIST.PROJ_CODE, "                
								+"	DIST.MRP_RUN_ID,DIST.SALE_ORDER, DIST.REMARKS1, "
								+"	DIST.REMARKS2,DIST.CURR_CODE,DIST.ORDER_TYPE,DIST.LOC_CODE__CONS, "
								+"	DIST.SUNDRY_TYPE,DIST.SUNDRY_CODE,DIST.AUTO_RECEIPT,DIST.TRAN_TYPE,DIST.SALES_PERS, "
								+"	DIST.LOC_CODE__GITBF,DIST.CUST_CODE__DLV,DIST.DLV_TO,DIST.DLV_ADD1,DIST.DLV_ADD2, "
								+"	DIST.DLV_ADD3,DIST.DLV_CITY,DIST.STATE_CODE__DLV,DIST.COUNT_CODE__DLV,DIST.DLV_PIN, "
								+"	DIST.STAN_CODE,DIST.TEL1__DLV,DIST.TEL2__DLV,DIST.TEL3__DLV,DIST.FAX__DLV,DIST.AVALIABLE_YN, "          
								+"	DIST.PURC_ORDER,DIST.TOT_AMT,DIST.TAX_AMT,DIST.NET_AMT,DIST.TRAN_SER,DIST.PARENT__TRAN_ID, "
								+"	DIST.REV__TRAN,DIST.PRICE_LIST__CLG,DIST.SORD_NO,DIST.POLICY_NO,DIST.LOC_CODE__DAMAGED, "
								+"	DIST.SITE_CODE__BIL, DIST. TRANS_MODE,S3.DESCR C_DESCR,S3.ADD1 C_ADD1,S3.ADD2 C_ADD2, "
								+"	DIST.TRANS_MODE,S3.CITY C_CITY,S3.PIN C_PIN,S3.STATE_CODE C_STATE_CODE , DIST.exch_rate "
								+"	FROM DISTORDER DIST,SITE S1 ,SITE S2,SITE S3 "
								+"	WHERE DIST.DIST_ORDER = ? "
								+"	AND DIST.SITE_CODE__SHIP = S1.SITE_CODE(+) " 
								+"	AND DIST.SITE_CODE__DLV = S2.SITE_CODE(+) "
								+"	AND DIST.SITE_CODE__BIL = S3.SITE_CODE(+) ";
							// added by ritesh on 07/jan/13 for supreme start
							if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ))
							{
								vSql = "SELECT DIST.ORDER_DATE,DIST.SITE_CODE__SHIP,S1.DESCR A_DESCR,S1.ADD1 A_ADD1, "        
										+"	S1.ADD2 A_ADD2,S1.CITY A_CITY,S1.PIN A_PIN,S1.STATE_CODE A_STATE_CODE, "
										+"	DIST.SITE_CODE__DLV,S2.DESCR B_DESCR,S2.ADD1 B_ADD1,S2.ADD2 B_ADD2, "
										+"	S2.CITY B_CITY,S2.PIN B_PIN,S2.STATE_CODE B_STATE_CODE, "
										+"	DIST.SHIP_DATE,DIST.DUE_DATE,DIST.REMARKS,DIST.DIST_ROUTE,DIST.PRICE_LIST, "
										+"	DIST.TARGET_WGT,DIST.TARGET_VOL,DIST.LOC_CODE__GIT,DIST.PROJ_CODE, "                
										+"	DIST.MRP_RUN_ID,DIST.SALE_ORDER, DIST.REMARKS1, "
										+"	DIST.REMARKS2,DIST.CURR_CODE,DIST.ORDER_TYPE,DIST.LOC_CODE__CONS, "
										+"	DIST.SUNDRY_TYPE,DIST.SUNDRY_CODE,DIST.AUTO_RECEIPT,DIST.TRAN_TYPE,DIST.SALES_PERS, "
										+"	DIST.LOC_CODE__GITBF,DIST.CUST_CODE__DLV,DIST.DLV_TO,DIST.DLV_ADD1,DIST.DLV_ADD2, "
										+"	DIST.DLV_ADD3,DIST.DLV_CITY,DIST.STATE_CODE__DLV,DIST.COUNT_CODE__DLV,DIST.DLV_PIN, "
										+"	DIST.STAN_CODE,DIST.TEL1__DLV,DIST.TEL2__DLV,DIST.TEL3__DLV,DIST.FAX__DLV,DIST.AVALIABLE_YN, "          
										+"	DIST.PURC_ORDER,DIST.TOT_AMT,DIST.TAX_AMT,DIST.NET_AMT,DIST.TRAN_SER,DIST.PARENT__TRAN_ID, "
										+"	DIST.REV__TRAN,DIST.PRICE_LIST__CLG,DIST.SORD_NO,DIST.POLICY_NO,DIST.LOC_CODE__DAMAGED, "
										+"	DIST.SITE_CODE__BIL, DIST. TRANS_MODE,S3.DESCR C_DESCR,S3.ADD1 C_ADD1,S3.ADD2 C_ADD2, "
										+"	DIST.TRANS_MODE,S3.CITY C_CITY,S3.PIN C_PIN,S3.STATE_CODE C_STATE_CODE, DIST.exch_rate "
								        +"  FROM DISTORDER DIST left outer join SITE S3  on DIST.SITE_CODE__BIL = S3.SITE_CODE ,SITE S1 ,SITE S2 "
								        +"  WHERE DIST.DIST_ORDER = ? 	AND DIST.SITE_CODE__SHIP = S1.SITE_CODE AND DIST.SITE_CODE__DLV = S2.SITE_CODE ";	
							}
							// added by ritesh on 07/jan/13 for supreme end	
							vPStmt = conn.prepareStatement(vSql);
							vPStmt.setString(1,distOrder.trim());
							vRs = vPStmt.executeQuery();
							if( vRs.next() )
							{
								orderDate = vRs.getString( "ORDER_DATE" );
								distDateTime = Timestamp.valueOf(orderDate);
								orderDate= (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(distDateTime).toString();
								siteCodeShip = vRs.getString( "SITE_CODE__SHIP" );
								aDescr = vRs.getString( "A_DESCR" );
								aAdd1 = vRs.getString( "A_ADD1" );
								aAdd2 = vRs.getString( "A_ADD2" );
								aCity = vRs.getString( "A_CITY" );
								aPin = vRs.getString( "A_PIN" );
								aStateCode = vRs.getString( "A_STATE_CODE" );
								siteCodeDLV = vRs.getString( "SITE_CODE__DLV" );
								bDescr = vRs.getString( "B_DESCR" );
								bAdd1 = vRs.getString( "B_ADD1" );
								bAdd2 = vRs.getString( "B_ADD2" );
								bCity = vRs.getString( "B_CITY" );
								bPin = vRs.getString( "B_PIN" );
								bStateCode = vRs.getString( "B_STATE_CODE" );
								shipDate = vRs.getString( "SHIP_DATE" );
								if( shipDate != null )
								{
									shipDateTime = Timestamp.valueOf(shipDate);
									shipDate= (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(shipDateTime).toString();
								}
								dueDate = vRs.getString( "DUE_DATE" );
								if( dueDate != null )
								{
									dueDateTime = Timestamp.valueOf(dueDate);
									dueDate= (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(dueDateTime).toString();
								}
								remarks = vRs.getString( "REMARKS" );
								distRoute = vRs.getString( "DIST_ROUTE" );
								priceList = vRs.getString( "PRICE_LIST" );
								targetWgt = vRs.getString( "TARGET_WGT" );
								targetVol = vRs.getString( "TARGET_VOL" );
								locCodeGit = vRs.getString( "LOC_CODE__GIT" );
								projCode = vRs.getString( "PROJ_CODE" );
								mrprunId = vRs.getString( "MRP_RUN_ID" );
								saleOrder = vRs.getString( "SALE_ORDER" );
								remarks1 = vRs.getString( "REMARKS1" );
								remarks2 = vRs.getString( "REMARKS2" );
								currCode = vRs.getString( "CURR_CODE" );
								orderType = vRs.getString( "ORDER_TYPE" );
								locCodeCons = vRs.getString( "LOC_CODE__CONS" );
								sundryType = vRs.getString( "SUNDRY_TYPE" );
								autoReceipt = vRs.getString( "AUTO_RECEIPT" );				
								tranType = vRs.getString( "TRAN_TYPE" );
								salesPers = vRs.getString( "SALES_PERS" );
								locCodeGitbf = vRs.getString( "LOC_CODE__GITBF" );
								custCodeDlv = vRs.getString( "CUST_CODE__DLV" );
								dlvTo = vRs.getString( "DLV_TO" );
								dlvAdd1 = vRs.getString( "DLV_ADD1" );
								dlvAdd2 = vRs.getString( "DLV_ADD2" );
								dlvAdd3 = vRs.getString( "DLV_ADD3" );
								dlvCity = vRs.getString( "DLV_CITY" );
								stateCodeDlv = vRs.getString( "STATE_CODE__DLV" );
								countCodeDlv = vRs.getString( "COUNT_CODE__DLV" );
								dlvPin = vRs.getString( "DLV_PIN" );
								stanCode = vRs.getString( "STAN_CODE" );
								tel1Dlv = vRs.getString( "TEL1__DLV" );
								tel2Dlv = vRs.getString( "TEL2__DLV" );
								tel3Dlv = vRs.getString( "TEL3__DLV" );
								faxDlv = vRs.getString( "FAX__DLV" );
								avaliableYn = vRs.getString( "AVALIABLE_YN" );
								purcOrder = vRs.getString( "PURC_ORDER" );
								totAmt = vRs.getString( "TOT_AMT" );
								taxAmt = vRs.getString( "TAX_AMT" );
								netAmt = vRs.getString( "NET_AMT" );
								tranSer = vRs.getString( "TRAN_SER" );
								parenTranId = vRs.getString( "PARENT__TRAN_ID" );
								revTran = vRs.getString( "REV__TRAN" );
								priceListClg = vRs.getString( "PRICE_LIST__CLG" );
								sordNo = vRs.getString( "SORD_NO" );
								policyNo = vRs.getString( "POLICY_NO" );
								locCodeDmgd = vRs.getString( "LOC_CODE__DAMAGED" );
								siteCodeBil = vRs.getString( "SITE_CODE__BIL" );
								cDescr = vRs.getString( "C_DESCR" );
								cAdd1 = vRs.getString( "C_ADD1" );
								cAdd2 = vRs.getString( "C_ADD2" );
								cCity = vRs.getString( "C_CITY" );
								cPin = vRs.getString( "C_PIN" );
								cStateCode = vRs.getString( "C_STATE_CODE" );
								sundryCode = vRs.getString( "SUNDRY_CODE" );
								tranMode = vRs.getString( "TRANS_MODE" );
								exchRate= vRs.getDouble("exch_rate"); // // ADDED BY ABHIJIT FOR D15DSUN019
							}
							vRs.close();
							vRs = null;
							vPStmt.close();
							vPStmt = null;
							
							valueXmlString.append("<trans_mode_o>").append("<![CDATA[" + ( tranMode != null ? tranMode.trim() : ""  )+ "]]>").append("</trans_mode_o>");
							valueXmlString.append("<trans_mode>").append("<![CDATA[" + ( tranMode != null ? tranMode.trim() : ""  )+ "]]>").append("</trans_mode>");
							valueXmlString.append("<site_code__dlv_o>").append("<![CDATA[" + ( siteCodeDLV != null ? siteCodeDLV.trim() : ""  )+ "]]>").append("</site_code__dlv_o>");
							valueXmlString.append("<sundry_code_o>").append("<![CDATA[" + ( sundryCode != null ? sundryCode.trim() : ""  )+ "]]>").append("</sundry_code_o>");
							valueXmlString.append("<tax_amt_o>").append("<![CDATA[" + ( taxAmt != null ? taxAmt.trim() : ""  )+ "]]>").append("</tax_amt_o>");
							valueXmlString.append("<tax_amt>").append("<![CDATA[" + ( taxAmt != null ? taxAmt.trim() : ""  )+ "]]>").append("</tax_amt>");
							valueXmlString.append("<net_amt_o>").append("<![CDATA[" + ( netAmt != null ? netAmt.trim() : ""  )+ "]]>").append("</net_amt_o>");
							valueXmlString.append("<net_amt>").append("<![CDATA[" + ( netAmt != null ? netAmt.trim() : ""  )+ "]]>").append("</net_amt>");
							valueXmlString.append("<tran_ser_o>").append("<![CDATA[" + ( tranSer != null ? tranSer.trim() : ""  )+ "]]>").append("</tran_ser_o>");
							valueXmlString.append("<parent__tran_id_o>").append("<![CDATA[" + ( parenTranId != null ? parenTranId.trim() : ""  )+ "]]>").append("</parent__tran_id_o>");
							valueXmlString.append("<rev__tran_o>").append("<![CDATA[" + ( revTran != null ? revTran.trim() : ""  )+ "]]>").append("</rev__tran_o>");
							valueXmlString.append("<price_list__clg_o>").append("<![CDATA[" + ( priceListClg != null ? priceListClg.trim() : ""  )+ "]]>").append("</price_list__clg_o>");
							valueXmlString.append("<sord_no_o>").append("<![CDATA[" + ( sordNo != null ? sordNo.trim() : ""  )+ "]]>").append("</sord_no_o>");
							valueXmlString.append("<policy_no_o>").append("<![CDATA[" + ( policyNo != null ? policyNo.trim() : ""  )+ "]]>").append("</policy_no_o>");
							valueXmlString.append("<loc_code__damaged_o>").append("<![CDATA[" + ( locCodeDmgd != null ? locCodeDmgd.trim() : ""  )+ "]]>").append("</loc_code__damaged_o>");
							valueXmlString.append("<site_code__bil_o>").append("<![CDATA[" + ( siteCodeBil != null ? siteCodeBil.trim() : ""  )+ "]]>").append("</site_code__bil_o>");
							valueXmlString.append("<dlv_pin_o>").append("<![CDATA[" + ( dlvPin != null ? dlvPin.trim() : ""  )+ "]]>").append("</dlv_pin_o>");
							valueXmlString.append("<stan_code_o>").append("<![CDATA[" + ( stanCode != null ? stanCode.trim() : ""  )+ "]]>").append("</stan_code_o>");
							valueXmlString.append("<tel1_dlv_o>").append("<![CDATA[" + ( tel1Dlv != null ? tel1Dlv.trim() : ""  )+ "]]>").append("</tel1_dlv_o>");
							valueXmlString.append("<tel2_dlv_o>").append("<![CDATA[" + ( tel2Dlv != null ? tel2Dlv.trim() : ""  )+ "]]>").append("</tel2_dlv_o>");
							valueXmlString.append("<tel3_dlv_o>").append("<![CDATA[" + ( tel3Dlv != null ? tel3Dlv.trim() : ""  )+ "]]>").append("</tel3_dlv_o>");
							valueXmlString.append("<fax_dlv_o>").append("<![CDATA[" + ( faxDlv != null ? faxDlv.trim() : ""  )+ "]]>").append("</fax_dlv_o>");
							valueXmlString.append("<avaliable_yn_o>").append("<![CDATA[" + ( avaliableYn != null ? avaliableYn.trim() : ""  )+ "]]>").append("</avaliable_yn_o>");
							valueXmlString.append("<purc_order_o>").append("<![CDATA[" + ( purcOrder != null ? purcOrder.trim() : ""  )+ "]]>").append("</purc_order_o>");
							valueXmlString.append("<purc_order>").append("<![CDATA[" + ( purcOrder != null ? purcOrder.trim() : ""  )+ "]]>").append("</purc_order>");
							valueXmlString.append("<tot_amt_o>").append("<![CDATA[" + ( totAmt != null ? totAmt.trim() : ""  )+ "]]>").append("</tot_amt_o>");
							valueXmlString.append("<tot_amt>").append("<![CDATA[" + ( totAmt != null ? totAmt.trim() : ""  )+ "]]>").append("</tot_amt>");
							valueXmlString.append("<tran_type_o>").append("<![CDATA[" + ( tranType != null ? tranType.trim() : ""  )+ "]]>").append("</tran_type_o>");
							valueXmlString.append("<sales_pers_o>").append("<![CDATA[" + ( salesPers != null ? salesPers.trim() : ""  )+ "]]>").append("</sales_pers_o>");
							valueXmlString.append("<loc_code__gitbf_o>").append("<![CDATA[" + ( locCodeGitbf != null ? locCodeGitbf.trim() : ""  )+ "]]>").append("</loc_code__gitbf_o>");
							valueXmlString.append("<cust_code__dlv_o>").append("<![CDATA[" + ( custCodeDlv != null ? custCodeDlv.trim() : ""  )+ "]]>").append("</cust_code__dlv_o>");
							valueXmlString.append("<dlv_to_o>").append("<![CDATA[" + ( dlvTo != null ? dlvTo.trim() : ""  )+ "]]>").append("</dlv_to_o>");
							valueXmlString.append("<dlv_add1_o>").append("<![CDATA[" + ( dlvAdd1 != null ? dlvAdd1.trim() : ""  )+ "]]>").append("</dlv_add1_o>");
							valueXmlString.append("<dlv_add2_o>").append("<![CDATA[" + ( dlvAdd2 != null ? dlvAdd2.trim() : ""  )+ "]]>").append("</dlv_add2_o>");
							valueXmlString.append("<dlv_add3_o>").append("<![CDATA[" + ( dlvAdd3 != null ? dlvAdd3.trim() : ""  )+ "]]>").append("</dlv_add3_o>");
							valueXmlString.append("<dlv_city_o>").append("<![CDATA[" + ( dlvCity != null ? dlvCity.trim() : ""  )+ "]]>").append("</dlv_city_o>");
							valueXmlString.append("<state_code__dlv_o>").append("<![CDATA[" + ( stateCodeDlv != null ? stateCodeDlv.trim() : ""  )+ "]]>").append("</state_code__dlv_o>");
							valueXmlString.append("<count_code__dlv_o>").append("<![CDATA[" + ( countCodeDlv != null ? countCodeDlv.trim() : ""  )+ "]]>").append("</count_code__dlv_o>");
							valueXmlString.append("<remarks1_o>").append("<![CDATA[" + ( remarks1 != null ? remarks1.trim() : ""  )+ "]]>").append("</remarks1_o>");
							valueXmlString.append("<remarks1>").append("<![CDATA[" + ( remarks1 != null ? remarks1.trim() : ""  )+ "]]>").append("</remarks1>");
							valueXmlString.append("<remarks2_o>").append("<![CDATA[" + ( remarks2 != null ? remarks2.trim() : ""  )+ "]]>").append("</remarks2_o>");
							valueXmlString.append("<remarks2>").append("<![CDATA[" + ( remarks2 != null ? remarks2.trim() : ""  )+ "]]>").append("</remarks2>");
							valueXmlString.append("<curr_code_o>").append("<![CDATA[" + ( currCode != null ? currCode.trim() : ""  )+ "]]>").append("</curr_code_o>");
							valueXmlString.append("<order_type_o>").append("<![CDATA[" + ( orderType != null ? orderType.trim() : ""  )+ "]]>").append("</order_type_o>");
							valueXmlString.append("<loc_code__cons>").append("<![CDATA[" + ( locCodeCons != null ? locCodeCons.trim() : ""  )+ "]]>").append("</loc_code__cons>");
							valueXmlString.append("<sundry_type_o>").append("<![CDATA[" + ( sundryType != null ? sundryType.trim() : ""  )+ "]]>").append("</sundry_type_o>");
							valueXmlString.append("<auto_receipt_o>").append("<![CDATA[" + ( autoReceipt != null ? autoReceipt.trim() : ""  )+ "]]>").append("</auto_receipt_o>");
							valueXmlString.append("<proj_code_o>").append("<![CDATA[" + ( projCode != null ? projCode.trim() : ""  )+ "]]>").append("</proj_code_o>");
							valueXmlString.append("<mrp_run_id_o>").append("<![CDATA[" + ( mrprunId != null ? mrprunId.trim() : ""  )+ "]]>").append("</mrp_run_id_o>");
							valueXmlString.append("<sale_order_o>").append("<![CDATA[" + ( saleOrder != null ? saleOrder.trim() : ""  )+ "]]>").append("</sale_order_o>");
							valueXmlString.append("<loc_code__git_o>").append("<![CDATA[" + ( locCodeGit != null ? locCodeGit.trim() : ""  )+ "]]>").append("</loc_code__git_o>");
							valueXmlString.append("<target_vol_o>").append("<![CDATA[" + ( targetVol != null ? targetVol.trim() : ""  )+ "]]>").append("</target_vol_o>");
							valueXmlString.append("<target_wgt_o>").append("<![CDATA[" + ( targetWgt != null ? targetWgt.trim() : ""  )+ "]]>").append("</target_wgt_o>");
							valueXmlString.append("<price_list_o>").append("<![CDATA[" + ( priceList != null ? priceList.trim() : ""  )+ "]]>").append("</price_list_o>");
							valueXmlString.append("<dist_route_o>").append("<![CDATA[" + ( distRoute != null ? distRoute.trim() : ""  )+ "]]>").append("</dist_route_o>");
							valueXmlString.append("<remarks_o>").append("<![CDATA[" + ( remarks != null ? remarks.trim() : ""  )+ "]]>").append("</remarks_o>");
							valueXmlString.append("<remarks>").append("<![CDATA[" + ( remarks != null ? remarks.trim() : ""  )+ "]]>").append("</remarks>");
							if( dueDate != null )
							{
								valueXmlString.append("<due_date_o>").append("<![CDATA[" + ( dueDate != null ? dueDate.trim() : ""  )+ "]]>").append("</due_date_o>");
							}
							if( shipDate != null )
							{
								valueXmlString.append("<ship_date_o>").append("<![CDATA[" + ( shipDate != null ? shipDate.trim() : ""  )+ "]]>").append("</ship_date_o>");
							}
							valueXmlString.append("<order_date>").append("<![CDATA[" + ( orderDate != null ? orderDate.trim() : ""  )+ "]]>").append("</order_date>");
							valueXmlString.append("<order_date_o>").append("<![CDATA[" + ( orderDate != null ? orderDate.trim() : ""  )+ "]]>").append("</order_date_o>");
							valueXmlString.append("<site_code__ship_o>").append("<![CDATA[" + ( siteCodeShip != null ? siteCodeShip.trim() : ""  )+ "]]>").append("</site_code__ship_o>");
							valueXmlString.append("<site_code__ship>").append("<![CDATA[" + ( siteCodeShip != null ? siteCodeShip.trim() : ""  )+ "]]>").append("</site_code__ship>");
							valueXmlString.append("<ship_site_descr>").append("<![CDATA[" + ( aDescr != null ? aDescr.trim() : ""  )+ "]]>").append("</ship_site_descr>");
							valueXmlString.append("<ship_add1>").append("<![CDATA[" + ( aAdd1 != null ? aAdd1.trim() : ""  )+ "]]>").append("</ship_add1>");
							valueXmlString.append("<ship_add2>").append("<![CDATA[" + ( aAdd2 != null ? aAdd2.trim() : ""  )+ "]]>").append("</ship_add2>");
							valueXmlString.append("<ship_city>").append("<![CDATA[" + ( aCity != null ? aCity.trim() : ""  )+ "]]>").append("</ship_city>");
							valueXmlString.append("<ship_pin>").append("<![CDATA[" + ( aPin != null ? aPin.trim() : ""  )+ "]]>").append("</ship_pin>");
							valueXmlString.append("<ship_state_code>").append("<![CDATA[" + ( aStateCode != null ? aStateCode.trim() : ""  )+ "]]>").append("</ship_state_code>");
							valueXmlString.append("<ship_site_descr>").append("<![CDATA[" + ( aDescr != null ? aDescr.trim() : ""  )+ "]]>").append("</ship_site_descr>");
							valueXmlString.append("<dlv_add1>").append("<![CDATA[" + ( bAdd1 != null ? bAdd1.trim() : ""  )+ "]]>").append("</dlv_add1>");
							valueXmlString.append("<dlv_add2>").append("<![CDATA[" + ( bAdd2 != null ? bAdd2.trim() : ""  )+ "]]>").append("</dlv_add2>");
							valueXmlString.append("<dlv_city>").append("<![CDATA[" + ( bCity != null ? bCity.trim() : ""  )+ "]]>").append("</dlv_city>");
							valueXmlString.append("<dlv_pin>").append("<![CDATA[" + ( bPin != null ? bPin.trim() : ""  )+ "]]>").append("</dlv_pin>");
							valueXmlString.append("<dlv_state_code>").append("<![CDATA[" + ( bStateCode != null ? bStateCode.trim() : ""  )+ "]]>").append("</dlv_state_code>");
							valueXmlString.append("<dlv_site_descr>").append("<![CDATA[" + ( bDescr != null ? bDescr.trim() : ""  )+ "]]>").append("</dlv_site_descr>");
							valueXmlString.append("<bil_add1>").append("<![CDATA[" + ( cAdd1 != null ? bAdd1.trim() : ""  )+ "]]>").append("</bil_add1>");
							valueXmlString.append("<bil_add2>").append("<![CDATA[" + ( cAdd2 != null ? cAdd2.trim() : ""  )+ "]]>").append("</bil_add2>");
							valueXmlString.append("<bil_city>").append("<![CDATA[" + ( cCity != null ? cCity.trim() : ""  )+ "]]>").append("</bil_city>");
							valueXmlString.append("<bil_pin>").append("<![CDATA[" + ( cPin != null ? cPin.trim() : ""  )+ "]]>").append("</bil_pin>");
							valueXmlString.append("<bil_state_code>").append("<![CDATA[" + ( cStateCode != null ? cStateCode.trim() : ""  )+ "]]>").append("</bil_state_code>");
							valueXmlString.append("<bil_site_descr>").append("<![CDATA[" + ( cDescr != null ? cDescr.trim() : ""  )+ "]]>").append("</bil_site_descr>");
							valueXmlString.append("<exch_rate>").append("<![CDATA["+exchRate+"]]>").append("</exch_rate>"); // ADDED BY ABHIJIT FOR D15DSUN019
						}
					}
					valueXmlString.append("</Detail1>");					
					valueXmlString.append("</Root>");
					break;
					case 2:
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
					valueXmlString.append("<Detail2>");	
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					if (currentColumn.trim().equals( "itm_default" ))
					{	
						String vSql = null;
						String distOrder = null;
						String status = null;
						String qtyOrder = null;
						String rate = null;
					    distOrder = genericUtility.getColumnValue( "dist_order", dom1 );
						qtyOrder = genericUtility.getColumnValue( "qty_order", dom );
						rate = genericUtility.getColumnValue( "rate", dom );
					    vSql = " select status  from distorder where dist_order = ? ";
					    
					    vPStmt = conn.prepareStatement( vSql );
						vPStmt.setString(1,distOrder.trim());
						vRs = vPStmt.executeQuery();
						if( vRs.next() )
						{
							status = vRs.getString( "status" );
						}
						vRs.close();
						vRs = null;
						vPStmt.close();
						vPStmt = null;
						if(status.equals("C"))
						{
							valueXmlString.append("<qty_order protect =\"1\">").append(qtyOrder).append("</qty_order>");
							valueXmlString.append("<rate protect =\"1\">").append(rate).append("</rate>");
						}
						valueXmlString.append("<status>").append("O").append("</status>");
						
					}
					if (currentColumn.trim().equals( "itm_defaultedit" ))
					{
					   String status = genericUtility.getColumnValue( "status", dom1 );
					  // valueXmlString.append("<status protect = '0'>").append(status).append("</status>");

					}
					if (currentColumn.trim().equals( "line_no_distord" ))
					{
						String lineNoOrder = null;
						String distOrder = null;
						String vSql = null;
						String tranIdDemand = null;
						String qtyOrder = null;
						String qtyConfirm = null;
						String qtyReceived = null;
						String qtyShiped = null;
						String dueDate = null;
						String taxClass = null;
						String taxChap = null;
						String taxEnv = null;
						String unit = null;
						String lineNoSord = null;
						String rate = null;
						String qtyReturn = null;
						String rateClg = null;
						String discount = null;
						String remarks = null;
						String totAmt = null;
						String taxAmt = null;
						String netAmt = null;
						String overShipPerc = null;
						String rateCLG = null;
						String qtyAlloc = null;
						String dateAlloc = null;
						String unitAlt = null;
						String convQtyAlt = null;
						String qtyOrderAlt = null;
						String shipDate = null;
						String packInstr = null;
						String custItemRef = null;
						String customDescr = null;
						String reasCode = null;
						String workOrder = null;
						String itemCode = null;
						String itemDescr = null;
						String saleOrder = null;
						String custSpecNo = null;
						Timestamp dueDateTime = null;
						Timestamp shipDateTime = null;
						Timestamp allocDateTime = null;
						String status = "";
						String lineno1 = null;
						boolean isLine = false;
						
						lineNoOrder = genericUtility.getColumnValue( "line_no_distord", dom );
						distOrder = genericUtility.getColumnValue( "dist_order", dom1 );
						
						if( lineNoOrder != null && lineNoOrder.trim().length() > 0 ) 
						{
							vSql = " SELECT DET.TRAN_ID__DEMAND,  DET.SALE_ORDER,  DET.QTY_ORDER,DET.ITEM_CODE, I.DESCR, DET.QTY_CONFIRM, DET.QTY_RECEIVED, DET.QTY_SHIPPED, "
								 +"	 DET.DUE_DATE, DET.TAX_CLASS, DET.TAX_CHAP, DET.TAX_ENV, DET.UNIT, DET.LINE_NO__SORD,DET.CUST_SPEC__NO, "
								 +"	 DET.RATE, DET.QTY_RETURN, DET.RATE_CLG, DET.DISCOUNT, DET.REMARKS, DET.TOT_AMT, DET.TAX_AMT, DET.NET_AMT, "
								 +"	 DET.OVER_SHIP_PERC, DET.RATE__CLG, DET.QTY_ALLOC, DET.DATE_ALLOC, DET.UNIT__ALT, DET.CONV__QTY__ALT, "         
								 +"	 DET.QTY_ORDER__ALT, DET.SHIP_DATE, DET.PACK_INSTR, DET.CUST_ITEM__REF, DET.CUSTOM_DESCR, "
								 +"	 DET.REAS_CODE, DET.WORK_ORDER "
								 +"  ,DET.STATUS "                       // ADDED BY RITESH FOR DI3HSUP004
								 +"	 FROM DISTORDER_DET DET, ITEM I "
								 +"  WHERE DIST_ORDER  = ? "
								 +"	 and line_no  = ? " 
								 +"	 and DET.ITEM_CODE = I.ITEM_CODE(+) " ;    
							// added by ritesh on 07/jan/13 for supreme start	
							if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ))
							{
								vSql = " SELECT DET.TRAN_ID__DEMAND,  DET.SALE_ORDER,  DET.QTY_ORDER,DET.ITEM_CODE, I.DESCR, DET.QTY_CONFIRM, DET.QTY_RECEIVED, DET.QTY_SHIPPED, "
										 +"	 DET.DUE_DATE, DET.TAX_CLASS, DET.TAX_CHAP, DET.TAX_ENV, DET.UNIT, DET.LINE_NO__SORD,DET.CUST_SPEC__NO, "
										 +"	 DET.RATE, DET.QTY_RETURN, DET.RATE_CLG, DET.DISCOUNT, DET.REMARKS, DET.TOT_AMT, DET.TAX_AMT, DET.NET_AMT, "
										 +"	 DET.OVER_SHIP_PERC, DET.RATE__CLG, DET.QTY_ALLOC, DET.DATE_ALLOC, DET.UNIT__ALT, DET.CONV__QTY__ALT, "         
										 +"	 DET.QTY_ORDER__ALT, DET.SHIP_DATE, DET.PACK_INSTR, DET.CUST_ITEM__REF, DET.CUSTOM_DESCR, "
										 +"	 DET.REAS_CODE, DET.WORK_ORDER "
										 +"  ,DET.STATUS "  
										 +"  FROM DISTORDER_DET DET left outer join ITEM I on DET.ITEM_CODE = I.ITEM_CODE "
										 +"  WHERE DIST_ORDER  = ? "
										 +"  and line_no  = ? ";
							}
							// added by ritesh on 07/jan/13 for supreme end	
							vPStmt = conn.prepareStatement( vSql );
							vPStmt.setString(1,distOrder.trim());
							vPStmt.setInt(2,Integer.parseInt(lineNoOrder.trim()));
							vRs = vPStmt.executeQuery();
							if( vRs.next() )
							{
											
								taxEnv = vRs.getString( "TAX_ENV" );
								taxChap = vRs.getString( "TAX_CHAP" );
								taxClass = vRs.getString( "TAX_CLASS" );
								qtyShiped = vRs.getString( "QTY_SHIPPED" );
								tranIdDemand = vRs.getString( "TRAN_ID__DEMAND" );
								qtyOrder = vRs.getString( "QTY_ORDER" );
								qtyConfirm = vRs.getString( "QTY_CONFIRM" );
								qtyReceived = vRs.getString( "QTY_RECEIVED" );
								totAmt = vRs.getString( "TOT_AMT" );
								remarks = vRs.getString( "REMARKS" );
								discount = vRs.getString( "DISCOUNT" );
								rateClg = vRs.getString( "RATE_CLG" );
								qtyReturn = vRs.getString( "QTY_RETURN" );
								rate = vRs.getString( "RATE" );
								lineNoSord = vRs.getString( "LINE_NO__SORD" );
								unit = vRs.getString( "UNIT" );
								rateCLG = vRs.getString( "RATE__CLG" );
								qtyAlloc = vRs.getString( "QTY_ALLOC" );
								overShipPerc = vRs.getString( "OVER_SHIP_PERC" );
								netAmt = vRs.getString( "NET_AMT" );
								taxAmt = vRs.getString( "TAX_AMT" );
								itemCode = vRs.getString( "ITEM_CODE" );
								itemDescr = vRs.getString( "DESCR" );
								
								dueDate = vRs.getString( "DUE_DATE" );

								if( dueDate != null )
								{
									dueDateTime = Timestamp.valueOf(dueDate);
									dueDate= ( new SimpleDateFormat( genericUtility.getApplDateFormat() ) ).format( dueDateTime ).toString();
								}
								shipDate = vRs.getString( "SHIP_DATE" );
								if( shipDate != null )
								{
									shipDateTime = Timestamp.valueOf(shipDate);
									shipDate= ( new SimpleDateFormat( genericUtility.getApplDateFormat() ) ).format( shipDateTime ).toString();
								}
								dateAlloc = vRs.getString( "DATE_ALLOC" );
								if( dateAlloc != null )
								{
									allocDateTime = Timestamp.valueOf(dateAlloc);
									dateAlloc= ( new SimpleDateFormat( genericUtility.getApplDateFormat() ) ).format( allocDateTime ).toString();
								}
								qtyOrderAlt = vRs.getString( "QTY_ORDER__ALT" );
								convQtyAlt = vRs.getString( "CONV__QTY__ALT" );
								unitAlt = vRs.getString( "UNIT__ALT" );
								
								workOrder = vRs.getString( "WORK_ORDER" );
								reasCode = vRs.getString( "REAS_CODE" );
								customDescr = vRs.getString( "CUSTOM_DESCR" );
								custItemRef = vRs.getString( "CUST_ITEM__REF" );
								packInstr = vRs.getString( "PACK_INSTR" );
								saleOrder = vRs.getString( "SALE_ORDER" );
								custSpecNo = vRs.getString( "CUST_SPEC__NO" );
								status = vRs.getString( "STATUS" );     // ADDED BY RITESH FOR DI3HSUP004
																
							}
							vRs.close();
							vRs = null;
							vPStmt.close();
							vPStmt = null;
							
					   
							
							valueXmlString.append("<cust_spec__no>").append("<![CDATA[" + ( custSpecNo != null ? custSpecNo.trim() : ""  )+ "]]>").append("</cust_spec__no>");
							valueXmlString.append("<cust_spec__no_o>").append("<![CDATA[" + ( custSpecNo != null ? custSpecNo.trim() : ""  )+ "]]>").append("</cust_spec__no_o>");
							valueXmlString.append("<sale_order>").append("<![CDATA[" + ( saleOrder != null ? saleOrder.trim() : ""  )+ "]]>").append("</sale_order>");
							valueXmlString.append("<dist_order>").append("<![CDATA[" + ( distOrder != null ? distOrder.trim() : ""  )+ "]]>").append("</dist_order>");
							valueXmlString.append("<item_code>").append("<![CDATA[" + ( itemCode != null ? itemCode.trim() : ""  )+ "]]>").append("</item_code>");
							valueXmlString.append("<item_descr>").append("<![CDATA[" + ( itemDescr != null ? itemDescr.trim() : ""  )+ "]]>").append("</item_descr>");
							valueXmlString.append("<work_order_o>").append("<![CDATA[" + ( workOrder != null ? workOrder.trim() : ""  )+ "]]>").append("</work_order_o>");
							valueXmlString.append("<reas_code_o>").append("<![CDATA[" + ( reasCode != null ? reasCode.trim() : ""  )+ "]]>").append("</reas_code_o>");
							valueXmlString.append("<custom_descr_o>").append("<![CDATA[" + ( customDescr != null ? customDescr.trim() : ""  )+ "]]>").append("</custom_descr_o>");
							valueXmlString.append("<cust_Item__ref_o>").append("<![CDATA[" + ( custItemRef != null ? custItemRef.trim() : ""  )+ "]]>").append("</cust_Item__ref_o>");
							valueXmlString.append("<pack_instr_o>").append("<![CDATA[" + ( packInstr != null ? packInstr.trim() : ""  )+ "]]>").append("</pack_instr_o>");
							valueXmlString.append("<pack_instr>").append("<![CDATA[" + ( packInstr != null ? packInstr.trim() : ""  )+ "]]>").append("</pack_instr>");
							valueXmlString.append("<qty_order__alt_o>").append("<![CDATA[" + ( qtyOrderAlt != null ? qtyOrderAlt.trim() : ""  )+ "]]>").append("</qty_order__alt_o>");
							if( shipDate != null )
							{
								valueXmlString.append("<ship_date_o>").append("<![CDATA[" + ( shipDate != null ? shipDate.trim() : ""  )+ "]]>").append("</ship_date_o>");
							}
							valueXmlString.append("<conv__qty__alt_o>").append("<![CDATA[" + ( convQtyAlt != null ? convQtyAlt.trim() : ""  )+ "]]>").append("</conv__qty__alt_o>");
							valueXmlString.append("<unit__alt_o>").append("<![CDATA[" + ( unitAlt != null ? unitAlt.trim() : ""  )+ "]]>").append("</unit__alt_o>");
							if( dateAlloc != null )
							{
								valueXmlString.append("<date_alloc_o>").append("<![CDATA[" + ( dateAlloc != null ? dateAlloc.trim() : ""  )+ "]]>").append("</date_alloc_o>");
							}
							valueXmlString.append("<rate__clg_o>").append("<![CDATA[" + ( rateCLG != null ? rateCLG.trim() : ""  )+ "]]>").append("</rate__clg_o>");
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + ( rateCLG != null ? rateCLG.trim() : ""  )+ "]]>").append("</rate__clg>");
							valueXmlString.append("<qty_alloc_o>").append("<![CDATA[" + ( qtyAlloc != null ? qtyAlloc.trim() : ""  )+ "]]>").append("</qty_alloc_o>");
							valueXmlString.append("<over_ship_perc_o>").append("<![CDATA[" + ( overShipPerc != null ? overShipPerc.trim() : ""  )+ "]]>").append("</over_ship_perc_o>");
							valueXmlString.append("<net_amt_o>").append("<![CDATA[" + ( netAmt != null ? netAmt.trim() : ""  )+ "]]>").append("</net_amt_o>");
							valueXmlString.append("<net_amt>").append("<![CDATA[" + ( netAmt != null ? netAmt.trim() : ""  )+ "]]>").append("</net_amt>");
							valueXmlString.append("<tax_amt_o>").append("<![CDATA[" + ( taxAmt != null ? taxAmt.trim() : ""  )+ "]]>").append("</tax_amt_o>");
							valueXmlString.append("<tax_amt>").append("<![CDATA[" + ( taxAmt != null ? taxAmt.trim() : ""  )+ "]]>").append("</tax_amt>");
							valueXmlString.append("<remarks_o>").append("<![CDATA[" + ( remarks != null ? remarks.trim() : ""  )+ "]]>").append("</remarks_o>");
							valueXmlString.append("<remarks>").append("<![CDATA[" + ( remarks != null ? remarks.trim() : ""  )+ "]]>").append("</remarks>");
							valueXmlString.append("<tot_amt_o>").append("<![CDATA[" + ( totAmt != null ? totAmt.trim() : ""  )+ "]]>").append("</tot_amt_o>");
							valueXmlString.append("<tot_amt>").append("<![CDATA[" + ( totAmt != null ? totAmt.trim() : ""  )+ "]]>").append("</tot_amt>");
							valueXmlString.append("<discount_o>").append("<![CDATA[" + ( discount != null ? discount.trim() : ""  )+ "]]>").append("</discount_o>");
							valueXmlString.append("<rate_clg_o>").append("<![CDATA[" + ( rateClg != null ? rateClg.trim() : ""  )+ "]]>").append("</rate_clg_o>");
							valueXmlString.append("<qty_return_o>").append("<![CDATA[" + ( qtyReturn != null ? qtyReturn.trim() : ""  )+ "]]>").append("</qty_return_o>");
							valueXmlString.append("<rate_o>").append("<![CDATA[" + ( rate != null ? rate.trim() : ""  )+ "]]>").append("</rate_o>");
							valueXmlString.append("<rate>").append("<![CDATA[" + ( rate != null ? rate.trim() : ""  )+ "]]>").append("</rate>");
							valueXmlString.append("<line_no__sord_o>").append("<![CDATA[" + ( lineNoSord != null ? lineNoSord.trim() : ""  )+ "]]>").append("</line_no__sord_o>");
							valueXmlString.append("<unit_o>").append("<![CDATA[" + ( unit != null ? unit.trim() : ""  )+ "]]>").append("</unit_o>");
							//valueXmlString.append("<tax_env_o>").append("<![CDATA[" + ( taxEnv != null ? taxEnv.trim() : ""  )+ "]]>").append("</tax_env_o>");
							valueXmlString.append("<tax_env_o>").append("<![CDATA[" + ( taxEnv != null ? taxEnv : ""  )+ "]]>").append("</tax_env_o>");  // remove trim by cpatil 
							//valueXmlString.append("<tax_env>").append("<![CDATA[" + ( taxEnv != null ? taxEnv.trim() : ""  )+ "]]>").append("</tax_env>");
							valueXmlString.append("<tax_env>").append("<![CDATA[" + ( taxEnv != null ? taxEnv : ""  )+ "]]>").append("</tax_env>");
							valueXmlString.append("<tax_chap_o>").append("<![CDATA[" + ( taxChap != null ? taxChap.trim() : ""  )+ "]]>").append("</tax_chap_o>");
							valueXmlString.append("<tax_chap>").append("<![CDATA[" + ( taxChap != null ? taxChap.trim() : ""  )+ "]]>").append("</tax_chap>");
							valueXmlString.append("<tax_class_o>").append("<![CDATA[" + ( taxClass != null ? taxClass.trim() : ""  )+ "]]>").append("</tax_class_o>");
							valueXmlString.append("<tax_class>").append("<![CDATA[" + ( taxClass != null ? taxClass.trim() : ""  )+ "]]>").append("</tax_class>");
							if( dueDate != null )
							{
								valueXmlString.append("<due_date_o>").append("<![CDATA[" + ( dueDate != null ? dueDate.trim() : ""  )+ "]]>").append("</due_date_o>");
							}
							valueXmlString.append("<qty_shiped_o>").append("<![CDATA[" + ( qtyShiped != null ? qtyShiped.trim() : ""  )+ "]]>").append("</qty_shiped_o>");
							valueXmlString.append("<tran_id__demand_o>").append("<![CDATA[" + ( tranIdDemand != null ? tranIdDemand.trim() : ""  )+ "]]>").append("</tran_id__demand_o>");
							valueXmlString.append("<tran_id__demand>").append("<![CDATA[" + ( tranIdDemand != null ? tranIdDemand.trim() : ""  )+ "]]>").append("</tran_id__demand>");
							valueXmlString.append("<qty_order_o>").append("<![CDATA[" + ( qtyOrder != null ? qtyOrder.trim() : ""  )+ "]]>").append("</qty_order_o>");
							valueXmlString.append("<qty_order>").append("<![CDATA[" + ( qtyOrder != null ? qtyOrder.trim() : ""  )+ "]]>").append("</qty_order>");
							valueXmlString.append("<qty_received_o>").append("<![CDATA[" + ( qtyReceived != null ? qtyReceived.trim() : ""  )+ "]]>").append("</qty_received_o>");
							valueXmlString.append("<qty_confirm_o>").append("<![CDATA[" + ( qtyConfirm != null ? qtyConfirm.trim() : ""  )+ "]]>").append("</qty_confirm_o>");
						
						//	valueXmlString.append("<status>").append("<![CDATA[" + ( status != null ? status.trim() : ""  )+ "]]>").append("</status>"); // ADDED BY RITESH FOR DI3HSUP004


						}
					}
					if (currentColumn.trim().equals( "qty_order" ))
					{
						String qtyOrder = null;
						double mqty = 0.0;
						double lc_factDouble = 0.0;
						double lc_qty_order__alt = 0.0;
						double ac_shipper_qty = 0.0;
						double ac_integral_qty = 0.0;
						int ll_no_art = 0;
						int ll_no_art1 = 0;
						String lc_fact = null;
						String unitAlt = null;
						String unit = null;
						String itemCode = null;
						String sundryCode = null;
						String siteCodeShip = null;
						ArrayList returnValue = null;
						double isQtyOrder = 0.0;
						double lc_bal_qty = 0.0;
						returnValue = new ArrayList();
						DistCommon distComm = new DistCommon(); 
						qtyOrder = checkNull(genericUtility.getColumnValue( "qty_order", dom));
						unitAlt = checkNull(genericUtility.getColumnValue( "unit__alt_o", dom1));
						unit = checkNull(genericUtility.getColumnValue( "unit_o", dom1));
						itemCode = checkNull(genericUtility.getColumnValue( "item_code", dom1));
						System.out.println("Froum"+unit);
						System.out.println("Touom"+unitAlt);
						System.out.println("For Item COde"+itemCode);
						//if(qtyOrder != null) isQtyOrder = Double.parseDouble( qtyOrder );
						isQtyOrder = qtyOrder != null ? qtyOrder.trim().length() > 0 ? Double.parseDouble(qtyOrder.trim()) : 0 : 0;
						
						System.out.println("Qty Order is"+isQtyOrder);
						returnValue = distComm.getConvQuantityFact( unit, unitAlt, itemCode, isQtyOrder, 0.0, conn );
						lc_fact = (String)returnValue.get(0);
						qtyOrder = (String)returnValue.get(1);
						mqty = Double.parseDouble(qtyOrder);
						System.out.println("Qty is"+mqty);
						lc_factDouble = Double.parseDouble(lc_fact);
						System.out.println("Factdouble is"+lc_factDouble);
						
						if( lc_factDouble > 0.0 ) 
						{
							lc_qty_order__alt = mqty / lc_factDouble;
						}
						System.out.println("qtyorderalto is"+lc_qty_order__alt);
						valueXmlString.append("<qty_confirm_o>").append("<![CDATA[" +mqty+ "]]>").append("</qty_confirm_o>");
						valueXmlString.append("<conv__qty__alt_o>").append("<![CDATA[" + lc_factDouble + "]]>").append("</conv__qty__alt_o>");
						valueXmlString.append("<qty_order__alt_o>").append("<![CDATA[" + lc_qty_order__alt + "]]>").append("</qty_order__alt_o>");
						
					}
					if (currentColumn.trim().equals( "rate" ))
					{
						String rate = null;
						String rateClg = null;
						double doubleRateClg = 0.0;
						
						rate = genericUtility.getColumnValue( "rate", dom );
						rateClg = genericUtility.getColumnValue( "rate__clg", dom );
						if (rateClg == null || rateClg.trim().length() == 0)
						{
							rateClg = "0";
						}
						doubleRateClg = Double.parseDouble( rateClg );
						
						if( rateClg == null ||  doubleRateClg == 0.0 || doubleRateClg == 0 ) 
						{
							rateClg = rate;
						}
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + ( rateClg != null ? rateClg.trim() : ""  )+ "]]>").append("</rate__clg>");
					}	
					 // ADDED BY ABHIJIT FOR D15DSUN019 
					if (currentColumn.trim().equals( "item_code" ))
					{
						
						String mpricelist = null;
						String mpricelistclg = null;
						String morderdate = null;
						String sitecode = null;
						String exchrate1 = null;
						String trantype = null;
						String saleorder = null;
						String itemDescr = null;
						String unit = null;
						String unitsal = null;
						String custcode = null;
						String descr = null;
						String type = null;
						String distOrder= null;
						double exchrate = 0;
						double rate = 0;
						double rate1 = 0;
						distOrder = genericUtility.getColumnValue( "dist_order", dom1 );
						itemcode1 = genericUtility.getColumnValue("item_code", dom);
						mpricelist =genericUtility.getColumnValue("price_list_o",dom1);
						morderdate = genericUtility.getColumnValue("order_date",dom1);
						sitecode = genericUtility.getColumnValue("site_code__ship",dom1);
						exchrate1 = genericUtility.getColumnValue("exch_rate",dom1);
						trantype = genericUtility.getColumnValue("tran_type_o",dom1);
					//	saleorder= genericUtility.getColumnValue("sale_order",dom1);
						mpricelistclg = genericUtility.getColumnValue("price_list__clg_o",dom1);
						
						System.out.println("Distribution Order is"+distOrder);
						System.out.println("Item Code is "+itemcode1);
						System.out.println("Price List is :"+mpricelist);
						System.out.println("Order date is :"+morderdate);
						System.out.println("Site code is :"+sitecode);
						System.out.println("Exchange rate is :"+exchrate1);
						System.out.println("Tran Type is :"+trantype);
						System.out.println("Price List is :"+mpricelistclg);
						
						sql = "select SALE_ORDER FROM DISTORDER  WHERE DIST_ORDER  = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,distOrder.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							saleorder = rs.getString( "SALE_ORDER" );
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						System.out.println("Sale Order is :"+saleorder);
						
						sql = " select  descr, unit,unit__sal  from item where item_code = ? ";
						vPStmt =  conn.prepareStatement(sql);
						vPStmt.setString(1,itemcode1);
						vRs = vPStmt.executeQuery();
						if(vRs.next())
						{
							itemDescr = checkNull( vRs.getString("descr"));
							unit = checkNull(vRs.getString("unit"));
							unitsal = checkNull( vRs.getString("unit__sal"));
							System.out.println("##### Item Code"+itemcode1);
							System.out.println("##### Description"+itemDescr);
							System.out.println("##### Unit"+unit);
							System.out.println("#####Unit Sal"+unitsal);
						}
						vRs.close();
						vRs = null;
						vPStmt.close(); 
						vPStmt = null;
						if( unitsal == null || unitsal.trim().length()<= 0 )
						{
							unitsal=unit;	
						}
				
						valueXmlString.append("<item_descr >").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");
						valueXmlString.append("<unit_o >").append("<![CDATA["+unit+"]]>").append("</unit_o>");
						valueXmlString.append("<unit__alt_o >").append("<![CDATA["+unitsal+"]]>").append("</unit__alt_o>");
						valueXmlString.append("<dist_order >").append("<![CDATA["+distOrder+"]]>").append("</dist_order>");
						
						if( saleorder != null && saleorder.trim().length() > 0 )
						{
							sql = " select cust_code from sorder where sale_order = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,itemcode1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								custcode = checkNull( rs.getString("cust_code"));
								System.out.println("#####cust code"+custcode);
								System.out.println("itemcode"+itemcode1);
							}
							rs.close();
							rs = null;
							pstmt.close(); 
							pstmt = null;
							
							sql = "select descr from customeritem where cust_code =? and item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,custcode);
							pstmt.setString(2,itemcode1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								descr = checkNull( rs.getString("descr"));
								System.out.println("#####Description"+descr);
								System.out.println("#####itemcode"+itemcode1);
							}
							rs.close();
							rs = null;
							pstmt.close(); 
							pstmt = null;
							//dw_detedit[ii_currformno].SetItem(1,"custom_descr", ls_cust_descr)
							valueXmlString.append("<custom_descr_o >").append("<![CDATA["+descr+"]]>").append("</custom_descr_o>");
						}
						
						if( mpricelist == null || mpricelist.trim().length()<= 0 )
						{
							mrate = 0;
						}
						else
						{
							mrate = discommon.pickRate(mpricelist,morderdate, itemcode1,"","L",conn);
							System.out.println("#####rate"+mrate);
						}
						type =  discommon.getPriceListType(mpricelist, conn);
						if(type != "L" && mrate < 0)
						{
							 mrate = 0;
						}
						 exchrate = Double.parseDouble(exchrate1);
						 System.out.println("@@@@@Exchange Rate"+exchrate);
						 rate =  mrate * exchrate;
						 System.out.println("Rate is "+rate);
						 valueXmlString.append("<rate >").append("<![CDATA["+ rate +"]]>").append("</rate>");
						 valueXmlString.append("<rate_o >").append("<![CDATA["+ rate +"]]>").append("</rate_o>");
						 
						if( mpricelistclg == null || mpricelistclg.trim().length() == 0 )
						{
							mrateclg = 0;
						}
						else
						{
							mrateclg = discommon.pickRate(mpricelistclg,morderdate, itemcode1,"","L",conn);
							System.out.println("#####rate"+mrateclg);
						}
						
						if(mrateclg == -1)
						{
							mrateclg = 0;
						}
						if(mrateclg == 0)
						{
							 rate =  mrate * exchrate;
							 System.out.println("Rate clg is "+rate);
							 valueXmlString.append("<rate__clg >").append("<![CDATA["+ rate +"]]>").append("</rate__clg>");
							valueXmlString.append("<rate__clg_o >").append("<![CDATA["+ rate +"]]>").append("</rate__clg_o>");
						}
						else
						{
							 rate1 =  mrate * exchrate;
							 System.out.println("Rate clg is22222222222222 "+rate1);
							 valueXmlString.append("<rate__clg>").append("<![CDATA["+ rate +"]]>").append("</rate__clg>");
							valueXmlString.append("<rate__clg_o >").append("<![CDATA["+ rate1 +"]]>").append("</rate__clg_o>");
						}
						
						sql=" select tax_class, tax_chap,tax_env from DISTORDER_DET  WHERE DIST_ORDER  = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,distOrder.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							taxclass = checkNull(rs.getString("tax_class"));
							taxchap = checkNull(rs.getString("tax_chap"));
							taxenv = checkNull(rs.getString("tax_env"));
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						System.out.println("#####Tax Class"+taxclass);
						System.out.println("#####Tax Chap"+taxchap);
						System.out.println("#####Tax env"+taxenv);
						
						valueXmlString.append("<tax_chap >").append("<![CDATA["+ taxchap +"]]>").append("</tax_chap>");
						valueXmlString.append("<tax_chap_o >").append("<![CDATA["+ taxchap +"]]>").append("</tax_chap_o>");
						valueXmlString.append("<tax_class >").append("<![CDATA["+ taxclass +"]]>").append("</tax_class>");
						valueXmlString.append("<tax_class_o >").append("<![CDATA["+ taxclass +"]]>").append("</tax_class_o>");
						valueXmlString.append("<tax_env >").append("<![CDATA["+ taxenv +"]]>").append("</tax_env>");
						valueXmlString.append("<tax_env_o >").append("<![CDATA["+ taxenv +"]]>").append("</tax_env_o>");
						
						sitecodeship =genericUtility.getColumnValue("site_code__ship_o",dom1);
						sitecodedlv  = genericUtility.getColumnValue("site_code__dlv_o",dom1);
						System.out.println("#####sitecode ship"+sitecodeship);
						System.out.println("#####sitecodedlv"+sitecodedlv);
						
						sql=" select stan_code from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,sitecodeship);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stationfr = checkNull( rs.getString("stan_code"));
							System.out.println("#####Station From:"+stationfr);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						
						sql=" select stan_code from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,sitecodedlv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stationto = checkNull( rs.getString("stan_code"));
							System.out.println("#####Station to:"+stationto);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						
						sql=" select item_ser from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemcode1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemser = checkNull( rs.getString("item_ser"));
							System.out.println("##### Item Series:"+itemser);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
					
					if ( taxchap == null || taxchap.trim().length()== 0 ) 
					{
						taxchap =  discommon.getTaxChap(itemcode1, itemser,"S",sitecodedlv,sitecodeship, conn);
						System.out.println("Tax Chap"+taxchap);
						valueXmlString.append("<tax_chap >").append("<![CDATA["+ taxchap +"]]>").append("</tax_chap>");
						valueXmlString.append("<tax_chap_o >").append("<![CDATA["+ taxchap +"]]>").append("</tax_chap_o>");
					}
					if (  taxclass == null || taxclass.trim().length()== 0  ) // isnull(ls_taxclass) or len(trim(ls_taxclass)) = 0 then	
					{
						taxclass =  discommon.getTaxClass("S", sitecodedlv, itemcode1, sitecodeship, conn);
						System.out.println("Tax Classsss"+taxclass);
						valueXmlString.append("<tax_class >").append("<![CDATA["+ taxclass +"]]>").append("</tax_class>");
						valueXmlString.append("<tax_class_o >").append("<![CDATA["+ taxclass +"]]>").append("</tax_class_o>");
					}
					if (  taxenv == null || taxenv.trim().length()== 0   )
					{
					taxenv =  discommon.getTaxEnv(stationfr, stationto, taxchap, taxclass, sitecodeship,conn);
					System.out.println("Tax Env"+taxenv);
					valueXmlString.append("<tax_env >").append("<![CDATA["+ taxclass +"]]>").append("</tax_env>");
					valueXmlString.append("<tax_env_o >").append("<![CDATA["+ taxenv +"]]>").append("</tax_env_o>");

					}
					
					}
					//ADDED BY ABHIJIT FOR D15DSUN019 END
					
					valueXmlString.append("</Detail2>");					
					valueXmlString.append("</Root>");
					break;
			}
			//END OF SWITCH
		}//END OF TRY
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if ( vRs != null )
				{
					vRs.close();
					vRs = null;
				}
				if ( vPStmt != null )
				{
					vPStmt.close();
					vPStmt = null;					
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}//END OF ITEMCHANGE
	
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private String getCurrdateAppFormat()
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
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
			exception.printStackTrace();
		}
		return s;
	}	
}
