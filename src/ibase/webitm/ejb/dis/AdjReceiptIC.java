/********************************************************
 Title : AdjReceiptIC[D16ASUN020]
 Date  : 28/04/16
 Developer: Sachin Satre

 ********************************************************/
package ibase.webitm.ejb.dis;

import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.GenVal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLPermission;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import bsh.Capabilities;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.GenericUtility;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Session Bean implementation class AdjReceiptIC
 */
@Stateless
public class AdjReceiptIC extends ValidatorEJB implements AdjReceiptICRemote,AdjReceiptICLocal 
{

	DistCommon distcommon=new DistCommon();
	/**
	 * Default constructor.
	 */
	public AdjReceiptIC()
	{
		// TODO Auto-generated constructor stub
	}

	E12GenericUtility genericUtility = new E12GenericUtility();
	//Commented by sarita on 12 OCT 2018 [START]
	//GenVal genval =new GenVal();
	//Commented by sarita on 12 OCT 2018 [START]
	//ValidatorEJB validatorEJB=new ValidatorEJB();
	

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			dom = parseString(xmlString);
			System.out.println("xmlString in WFval : " +xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0)
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} 
		catch (Exception e)
		{
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,	String objContext, String editFlag, String xtraParams)	throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat simpleDateFormat1 = null;
		java.sql.Timestamp currDate = null;
		
		int ctr = 0;
		int childNodeListLength;
		int currentFormNo = 0;

		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String sql = "";
		String userId = "";
		String errFldName = "";
		String columnValue = "",accCode="";
		String division = "",qty="";
		String fromDate = "",rate="",sundryCode="",sundryType="",refId="",refSer="",expDate="";
		String toDate = "",valueXmlString="",itemCode="",lotSl="",lotNo="",cctrCode="";
		String currAppdate = "",locCode="",siteCode="",faciLoc="",faciSite="",active="",mfgDate="";
		String sqlLoc = "",sqlFaciLoc="",sqlActive="",sqlFaciSit="",finParam="N",tranDate1="",deptCode="",trackShelfLife="";
		String itemSer="",priceList="";
		String stkOpt="",itemSerMst="",othSeries="",noArtStr="";
		String	transfer="",tranDateStr="",effDate="";
		String suppCodeMfg="",unitAlt="";
		String packCode="";//Added By Mukesh Chauhan on 11/10/19
		String chkDec="";
		String unit="";
		int cnt = 0;
		int cntv = 0,cntLoc=0;
		int cntItmSer = 0,noArt=0;
		double detQty= 0.0,detRate=0.0;
		Date tranDate=null;
		String acctCodeCr="",acctCodeDr="",errcode="";    //added by manish mhatre
		
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			simpleDateFormat1 = new SimpleDateFormat(genericUtility	.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			
			FinCommon finCommon = new FinCommon();
			finParam = finCommon.getFinparams("999999", "INVENTORY_ACCT", conn);
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("tran_date"))
					{
						tranDateStr=genericUtility.getColumnValue("tran_date",dom);
						if(tranDateStr==null || tranDateStr.trim().length()==0)
						{
							errCode = "VTTRDT01";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							tranDate = sdf.parse(genericUtility.getColumnValue("tran_date",dom));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
							System.out.println("tranDate  fire== " + tranDate);
							System.out.println("siteCode  fire== " + siteCode);
							if(siteCode == null || siteCode.trim().length()==0)
							{
								errCode = "NULLSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}else
							{
								cnt=0;
								sql = "select count(*) from site where site_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if (cnt == 0)
								{
									errCode = "VMSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}else
								{
									//Changes and Commented By Ajay on 20-12-2017 :START
									//errCode = nfCheckPeriod("SAL",tranDate,siteCode);
									errCode=finCommon.nfCheckPeriod("SAL", tranDate, siteCode, conn);
									//Changes and Commented By Ajay on 20-12-2017 :END
									if(errCode != null && errCode.trim().length() > 0)
									{
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							  }
							}
            		}else if (childNodeName.equalsIgnoreCase("eff_date"))
					{
            			effDate = genericUtility.getColumnValue("eff_date",dom);
            			if(effDate==null || effDate.trim().length()==0)
						{
							errCode = "EFFDTBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

					else if (childNodeName.equalsIgnoreCase("site_code"))
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						// 08-Jul-16 manoharan this itself is validator
						//errCode = validatorEJB.isSiteCode(siteCode,"ADJRCP"); 
						errCode = isSiteCode(siteCode,"ADJRCP"); 
						if(errCode!=null && errCode.trim().length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} 
					else if (childNodeName.equalsIgnoreCase("dept_code"))
					{
						deptCode = checkNull(genericUtility.getColumnValue("dept_code", dom));
						
						if(deptCode!=null && deptCode.trim().length()>0)
						{
							sql = "SELECT COUNT(*) FROM DEPARTMENT WHERE DEPT_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, deptCode);
							rs = pstmt.executeQuery();
							while (rs.next()) 
							{
								cnt = rs.getInt(1);
								System.out.println("loc inside while  count fire== "+ cnt);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("cnt aftr while  count fire== " + cnt);
							if (cnt == 0)
							{
								System.out.println("cnt site code validatioon fire");
								errCode = "VTDEPT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							
						}
						
					}else if (childNodeName.equalsIgnoreCase("item_ser"))
					{
						itemSer = this.genericUtility.getColumnValue("item_ser", dom);
						if(itemSer == null || itemSer.trim().length() ==0)
						{
							errCode = "VMITSER";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							cnt=0;
							sql = "select count(*) from itemser where item_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if (cnt == 0)
							{
								errCode = "VTITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}else if (childNodeName.equalsIgnoreCase("price_list"))
					{
						priceList = this.genericUtility.getColumnValue("price_list", dom);
						if (priceList != null && priceList.trim().length()>0)
						{
							cnt=0;
							sql = "select count(*) from pricelist where price_list = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if (cnt == 0)
							{
								errCode = "VTPLIST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				break;
				
			case 2 :
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("loc_code"))
					{

						locCode = checkNull(genericUtility.getColumnValue("loc_code", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));

								sqlLoc = "select count(*)  from location where loc_code = ?";
								pstmt = conn.prepareStatement(sqlLoc);
								pstmt.setString(1, locCode);
								rs = pstmt.executeQuery();
								while (rs.next()) 
								{						
									cntLoc = rs.getInt(1);
									System.out.println("locCode inside while  count fire== "+ cntLoc);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("cntLoc aftr while  count fire== " + cntLoc);

								if (cntLoc == 0)
								{
									System.out.println("cntLoc is not present in Location validatioon fire");
									errCode = "VMLOC1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}

								sqlFaciLoc = "SELECT FACILITY_CODE AS FACI_LOC_CODE FROM LOCATION WHERE LOC_CODE= ? ";
								pstmt = conn.prepareStatement(sqlFaciLoc);
								pstmt.setString(1, locCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									faciLoc = rs.getString("FACI_LOC_CODE");
									System.out.println("faciLoc inside  fire== "+ faciLoc);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("faciLoc aftr == "	+ faciLoc);
								
								
								sqlFaciSit = "SELECT FACILITY_CODE AS FACI_SITE_CODE FROM SITE WHERE SITE_CODE= ? ";
								pstmt = conn.prepareStatement(sqlFaciSit);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									faciSite = rs.getString("FACI_SITE_CODE");
									System.out.println("faciSite inside  fire== "+ faciSite);
								}
								rs.close();
								rs = null;						
								pstmt.close();
								pstmt = null;
								System.out.println("faciSite aftr == "	+ faciSite);
								
								if ((faciLoc!=null && faciLoc.trim().length()> 0) && (faciSite!=null && faciSite.trim().length()> 0)) 
								{
									if(!faciLoc.equals(faciSite))
									{
										System.out.println("not match  validatioon fire");
										errCode = "VMFACI2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
					}else if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode = this.genericUtility.getColumnValue("item_code", dom);
						System.out.println("itemCode : " +itemCode);
						siteCode = this.genericUtility.getColumnValue("site_code", dom1);
						itemSer = this.genericUtility.getColumnValue("item_ser", dom1);
						if(itemCode ==null || itemCode.trim().length() ==0)
						{
							errCode = "STKVALITCO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							cnt=0;
							sql = "select count(*) from item where item_code= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if (cnt == 0)
							{
								errCode = "VTITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							sql = "select (case when stk_opt is null then '0' else stk_opt end) as stk_opt, item_ser  from item where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								stkOpt = checkNull(rs.getString("stk_opt")).trim();
								itemSerMst = rs.getString("item_ser");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							
							
							if("0".equalsIgnoreCase(stkOpt))
							{
								errCode = "NONSTK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						//	if(!itemSer.equalsIgnoreCase(itemSerMst))
							if(!itemSer.trim().equals(itemSerMst.trim()))
							{
								sql = "select oth_series from itemser where item_ser = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									othSeries = rs.getString("oth_series");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if("N".equalsIgnoreCase(othSeries))
								{
									errCode = "VTITEM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
						}
						if(errCode ==null || errCode.trim().length()==0)
						{
							errCode = gbfItem(siteCode,itemCode,transfer,conn);
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						
					}
					else if ( (childNodeName.equalsIgnoreCase("rate") ) || (childNodeName.equalsIgnoreCase("gross_rate") ) )
					{
						rate = checkNull(genericUtility.getColumnValue("rate", dom));
						if(rate != null && rate.trim().length() > 0)
						{
							try
							{
								detRate = Double.parseDouble(rate);
							} catch (NumberFormatException n)
							{
								detRate = 0;
							}
						}
						if(detRate < 0)
						{
							errCode = "VTRATE2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("sundry_code"))
					{
						sundryCode = checkNull(genericUtility.getColumnValue("sundry_code", dom));
						if(sundryCode !=null && sundryCode.trim().length()>0)
						{
							sundryType = checkNull(genericUtility.getColumnValue("sundry_type", dom));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
							System.out.println("siteCode ==++"+ siteCode + "sundryType == ++"+sundryType + "sundryCode== "+sundryCode + "transfer=="+transfer );
							errCode = finCommon.isSundryCode(siteCode,sundryType,sundryCode,transfer,conn); //done
							
							if(errCode!=null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								
							}
						}	
					}
					else if( (childNodeName.equalsIgnoreCase("ref_id__for") ) || (childNodeName.equalsIgnoreCase("ref_ser__for") ) )
					{
						refId=checkNull(genericUtility.getColumnValue("ref_id__for",dom));
						refSer=checkNull(genericUtility.getColumnValue("ref_ser__for",dom));
						itemCode=checkNull(genericUtility.getColumnValue("item_code",dom));
						siteCode=checkNull(genericUtility.getColumnValue("site_code",dom));
						lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
						lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
						locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
						
						if(( (refSer.trim().length() > 0) && (refId.trim().length() ==0 || refId==null)) && (( refId.trim().length() > 0) && (refSer.trim().length()==0 || refSer==null)) )
						{
							errCode = "VTINV001";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							if(refSer.trim().length() > 0 && refId.trim().length() > 0)
							{
								sql = "SELECT COUNT(*)  FROM INVTRACE	WHERE " +
								"SITE_CODE = ? AND " +
								" ITEM_CODE = ? AND " +
								" LOC_CODE = ? AND " +
								" LOT_NO = ? AND " +
								"LOT_SL = ? AND " +
								"REF_SER__FOR = ? AND " +
								"REF_ID__FOR = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, itemCode);
								pstmt.setString(3, locCode);
								pstmt.setString(4, lotNo);
								pstmt.setString(5, lotSl);
								pstmt.setString(6, refSer);
								pstmt.setString(7, refId);
								rs = pstmt.executeQuery();
								while (rs.next()) 
								{
									cnt = rs.getInt(1);
									System.out.println("INVTRACE inside while  count fire== "+ cnt);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("cnt  INVTRACE aftr while  count fire== " + cnt);
								if (cnt == 0)
								{
									System.out.println("cnt  INVTRACE site code validatioon fire");
									errCode = "VTINV002";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
		
								}
						  }
					   }
						
					}
					else if (childNodeName.equalsIgnoreCase("lot_no"))
					{
						lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
						// Change by Manish on 13/09/16 for DDUK
						//if(lotNo==null || lotNo.trim().length()==0)
						if(lotNo==null || lotNo.length()==0)	
						{
							System.out.println("lot number site code validatioon fire");
							errCode = "VTLOTEMPTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
					}
					else if (childNodeName.equalsIgnoreCase("lot_sl"))
					{
						lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
						// Change by Manish on 13/09/16 for DDUK
						//if(lotSl==null || lotSl.trim().length()==0 )
						if(lotSl==null || lotSl.length()==0 )
						{
							System.out.println("lot sl  number site code validatioon fire");
							errCode = "VMLOTSL1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if (childNodeName.equalsIgnoreCase("quantity"))
					{
						qty=checkNull(genericUtility.getColumnValue("quantity",dom));
						itemCode=checkNull(genericUtility.getColumnValue("item_code",dom));
						siteCode=checkNull(genericUtility.getColumnValue("site_code",dom));
						lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
						lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
						locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
						unit=checkNull(genericUtility.getColumnValue("unit",dom));   //added by manish mhatre on 2-dec-2019
						
						if(qty != null && qty.trim().length() > 0)
						{
							try
							{
								detQty = Double.parseDouble(qty);
							} catch (NumberFormatException n)
							{
								detQty = 0;
							}
						}
						if(detQty <= 0)
						{
							errCode = "VTRCP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//added by manish mhatre on 02-dec-2019
						//start manish
						else
						{
							errCode=distcommon.checkDecimal(detQty, unit, conn);
							if(errCode!=null && errCode.trim().length()>0)
							{
								errCode = "VTUOMDEC3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}   //end manish	
					}
					else if (childNodeName.equalsIgnoreCase("acct_code__dr"))
					{
						accCode=checkNull(genericUtility.getColumnValue("acct_code__dr",dom));
						
						
						if("Y".equalsIgnoreCase(finParam))
						{
							cnt=0;
							sql = "select count(*) from  accounts where acct_code  = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, accCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
							cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (cnt > 0)
							{
								sql = "select active from  accounts where acct_code  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, accCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
								active = rs.getString("active");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(!"Y".equalsIgnoreCase(active))
								{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								}
							}else
							{
							errCode = "VMACCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							}
						}
						
					}
					else if (childNodeName.equalsIgnoreCase("acct_code__cr"))
					{
						
						cctrCode=checkNull(genericUtility.getColumnValue("acct_code__cr",dom));
						if("Y".equalsIgnoreCase(finParam))
						{
							cnt=0;
							sql = "select count(*) from  accounts where acct_code  = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cctrCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
							cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (cnt > 0)
							{
								sql = "select active from  accounts where acct_code  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, cctrCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
								active = rs.getString("active");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(!"Y".equalsIgnoreCase(active))
								{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								}
							}else
							{
							errCode = "VMACCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							}
						}
						
					}
					else if (childNodeName.equalsIgnoreCase("cctr_code__dr"))
					{
						cctrCode=checkNull(genericUtility.getColumnValue("cctr_code__dr",dom));
						acctCodeDr=checkNull(genericUtility.getColumnValue("acct_code__dr",dom));
						if(cctrCode == null && "Y".equalsIgnoreCase(finParam) )
						{
							System.out.println("Account  validatioon fire");
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
							if(cctrCode!=null && cctrCode.trim().length() > 0)
							{
							//added by manish mhatre on 3-jan-2020
							errcode = finCommon.isCctrCode(acctCodeDr, cctrCode, " ", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							} //end manish
								/*sql = "SELECT COUNT(*) FROM COSTCTR WHERE CCTR_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, cctrCode);
								rs = pstmt.executeQuery();
								while (rs.next()) 
								{
									cnt = rs.getInt(1);
									System.out.println("accCode cnt inside while  count fire== "+ cnt);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("cnt accCode  aftr while  count fire== " + cnt);
							   if (cnt == 0)
								{
									System.out.println("cctr Code  validatioon fire");
									errCode = "VMCCTR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}*/
							}
					 }
					else if (childNodeName.equalsIgnoreCase("cctr_code__cr"))
					{
						cctrCode=checkNull(genericUtility.getColumnValue("cctr_code__cr",dom));
						acctCodeCr=checkNull(genericUtility.getColumnValue("acct_code__cr",dom));
						if(cctrCode ==null  && "Y".equalsIgnoreCase(finParam) )
						{
							System.out.println("Account  validatioon fire");
							errCode = "VMCCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
							if(cctrCode!=null && cctrCode.trim().length() > 0)
							{
							//added by manish mhatre on 3-jan-2020
							errcode = finCommon.isCctrCode(acctCodeCr, cctrCode, " ", conn);
							if (errcode != null && errcode.trim().length() > 0) {
								errList.add(errcode);
								errFields.add(childNodeName.toLowerCase());
							}//end manish
								/*sql = "SELECT COUNT(*) FROM COSTCTR WHERE CCTR_CODE = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, cctrCode);
								rs = pstmt.executeQuery();
								while (rs.next()) 
								{
									cnt = rs.getInt(1);
									System.out.println("accCode cnt inside while  count fire== "+ cnt);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("cnt accCode  aftr while  count fire== " + cnt);
							   if (cnt == 0)
								{
									System.out.println("cctr Code  validatioon fire");
									errCode = "VMCCTR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}*/
							}
					  }
					else if (childNodeName.equalsIgnoreCase("site_code__mfg"))
					{
						siteCode=checkNull(genericUtility.getColumnValue("site_code__mfg",dom));
						// 08-jul-16 manoharan this itself is validator
						//errCode = validatorEJB.isSiteCode(siteCode,"ADJRCP"); //done
						//If condition added by Varsha V on 19-03-2018 for site_code__mfg column
						if(siteCode.length()>0)
						{
							errCode = isSiteCode(siteCode,"ADJRCP"); //done
							if(errCode!=null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("mfg_date"))
					{
						itemCode=checkNull(genericUtility.getColumnValue("item_code",dom));
						mfgDate=checkNull(genericUtility.getColumnValue("mfg_date",dom));
						
						sql = "SELECT TRACK_SHELF_LIFE  FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						//while (rs.next()) 
						if (rs.next()) //changes done by kailasg on 24/9/20 [suggested by SM sir] 
						{
							trackShelfLife= rs.getString("TRACK_SHELF_LIFE");
							System.out.println("trackShelfLife cnt inside while  count fire== "+ trackShelfLife);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("cnt trackShelfLife  aftr while  count fire== " + trackShelfLife);
						
						if(trackShelfLife.equalsIgnoreCase("Y"))
						{
							//if(mfgDate==null)
							if(mfgDate==null  || mfgDate.trim().length() == 0) //changed by kailasg on 24/09/20 [ Accepting Lot no. Details without MFG  Date]
							{
								System.out.println("mfg  validatioon fire");
								errCode = "VTMFGDTNVL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								
                            }
                            //Added by Anagha R on 15/01/2020 for validation rquired on posting or despatch making START
                            System.out.println("Stk_opt:: "+stkOpt);
                            if(mfgDate != null && mfgDate.trim().length()> 0 && stkOpt != "0"){
                                Date date1 = sdf.parse(mfgDate);
                                Date date2 = sdf.parse(currAppdate);
                                System.out.println("mfg_date:: "+mfgDate+"Today:: "+currAppdate);
								if(date1.compareTo(date2)>0){
                                    System.out.println("mfg date could not be greater than current date");
                                    errCode = "VTMFGDATE6";
                                    errList.add(errCode);
                                    errFields.add(childNodeName.toLowerCase());
                                }	
                            }//Added by Anagha R on 15/01/2020 for validation rquired on posting or despatch making END
						}
                    }                    
					else if (childNodeName.equalsIgnoreCase("exp_date"))
					{
						itemCode=checkNull(genericUtility.getColumnValue("item_code",dom));
						mfgDate=checkNull(genericUtility.getColumnValue("mfg_date",dom));
						

						sql = "SELECT TRACK_SHELF_LIFE  FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
					//	while (rs.next()) 
						if (rs.next()) //changes done by kailasg on 24/9/20 [suggested by SM sir] 
						{
							trackShelfLife= rs.getString("TRACK_SHELF_LIFE");
							System.out.println("trackShelfLife cnt inside while  count fire== "+ trackShelfLife);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("cnt trackShelfLife  aftr while  count fire== " + trackShelfLife);
						
						if(trackShelfLife.equalsIgnoreCase("Y"))
						{
							expDate=checkNull(genericUtility.getColumnValue("exp_date",dom));
							//if(expDate==null)
							if(expDate==null || expDate.trim().length() == 0)//changed by kailasg on 24/09/20 [ Accepting Lot no. Details without exp  Date]
							{
								System.out.println("exp  validatioon fire");
								errCode = "VTEXPDTNVL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								mfgDate=checkNull(genericUtility.getColumnValue("mfg_date",dom));
								if(expDate.length() > 0 && mfgDate.length() > 0)
								{
									Date date1 = sdf.parse(mfgDate);
									Date date2 = sdf.parse(expDate);
									
									if(date1.compareTo(date2)>0)
									{
									System.out.println("exp  validatioon fire");
									errCode = "VMEXPDT2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}else if(childNodeName.equalsIgnoreCase("no_art"))
					{
						noArtStr = this.genericUtility.getColumnValue("no_art", dom);
						if(noArtStr != null && noArtStr.trim().length() > 0)
						{
							try
							{
								noArt = Integer.parseInt(noArtStr);
							} catch (NumberFormatException n)
							{
								noArt = 0;
							}
						}else
						{
							noArt = 0;
						}
						if(noArt < 0)
						{
							errCode = "VTNARTNEG";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
                        }
                        //Added by Anagha R on 23/12/2020 for SYNCOM Reciept transaction vlidation for qty_per art can not be zero START
                        else if(noArt == 0){
                            errCode = "UVNOARTNZ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
                        }
                        //Added by Anagha R on 23/12/2020 for SYNCOM Reciept transaction vlidation for qty_per art can not be zero END
					}else if(childNodeName.equalsIgnoreCase("supp_code__mfg"))
					{
						suppCodeMfg = this.genericUtility.getColumnValue("supp_code__mfg", dom);
						siteCode=checkNull(genericUtility.getColumnValue("site_code",dom1));
						if(suppCodeMfg != null && suppCodeMfg.trim().length() > 0)
						{
							errCode = finCommon.isSupplier(siteCode,suppCodeMfg,transfer,conn); //done
							if(errCode !=null && errCode.trim().length()>0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}else if(childNodeName.equalsIgnoreCase("unit__alt"))
					{
						unitAlt=checkNull(genericUtility.getColumnValue("unit__alt",dom));
						if(unitAlt != null && unitAlt.trim().length() > 0)
						{
							cnt=0;
							sql = "select count(*) from uom where unit=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unitAlt);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VTALTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Added By Mukesh Chauhan On 11/10/19
					else if (childNodeName.equalsIgnoreCase("pack_code"))
					{
						
						packCode = this.genericUtility.getColumnValue("pack_code", dom);
						cnt=0;
						if(packCode == null || packCode.trim().length()==0)
						{
							errCode = "VTPCKCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*) from packing where pack_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, packCode);
							rs = pstmt.executeQuery();
							System.out.println("PACK CODE>>>>>"+packCode);
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VTPCKCDM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}//END
				}
				break;
			}
			int errListSize = errList.size();
			cnt = 0;
			if (errList != null && errListSize > 0) 
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
		catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} 
		finally 
		{
			try {
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
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}


	public String itemChanged(String xmlString, String xmlString1,	String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,	ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try 
		{
			dom = parseString(xmlString);
			System.out.println("xmlString["+xmlString+"]");
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} 
		catch (Exception e)
		{
			System.out
					.println("Exception : [TransporterIC][itemChanged( String, String )] :==>\n"
							+ e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("intered in itemchnage method ADJ RECEIPT..");
		
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		String logInEmpCode = "";
		String columnValue = "";
		int childNodeListLength = 0,cntTrn=0,stkCnt=0;
		Connection conn = null;
		//Add on 04/09/18
		PreparedStatement pstmt = null;
		//END
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null ;
		ResultSet rs1 = null ;
		ResultSet rs2 = null ;
		String divisionDom ="",rateOpt="",sqlItem="",reStr="";
		String division ="",sql="",itemType="",unitRate="",unitNetWt="";     
		String siteDescr="",deptDescr="",siteItmDescr="",itemDescr="",locDescr="",netWtItem="",integrlQty="",netWt="";
		String sqlSiteDescr="",sqlTran="",sqlDeptDescr="",sqlStockDe="",sqlLocDescr="",sqlTrackshelf="",sqlNetwt="",sqlNetwtItem="",sqlTracklife="",sqlCapacity="",sqlLcMode="",capacity="",unit="",unitPur="",packCode="",stkOpt="";
		String sysDate="",effDate="",mfgDate="",expDate="",tranDate="",mExpDate="",mMfgDate="",lcMode="";
		String siteCode="",siteCodeDom="", line="",itemCodeDom="", batchNO="",supCodeMfg="",retestDate="",grossWeight="",netWeight="";
		String priceList="",priceListDom="", tranId="",deptCode="",lotSL="",locCode="",lotNo="",lotSl="",qty="",trackShelfLife="",trackShelflife="",trackShelf="",rate="";
		String previousLineLot ="",lineNoStr="",tareWeight="";
		//Add
		String itemSer="";
		long lineNo=0,trackLife=0;
		double itmQty=0,mQty=0,mTrackShelflife=0,mAmount=0,mRate=0,mCapacity=0,mNetWt=0,mNetWtItem=0,mNoart=0,mGrossWeight=0,mNetWeight=0,mGrosswt=0,mTareWt=0,mIntegrlQty=0;
		double shelflife=0.0,convQtyFact=0.0,potectRate=0.0;
		Timestamp MfgDateTimeStamp=null,mExpdateTimeStamp=null,retestDateTmStmp=null;
		ArrayList convAr = null;
		int currentFormNo = 0,pos=0;  
		ConnDriver connDriver = new ConnDriver();
		try
		{  
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
	      
			columnValue = genericUtility.getColumnValue(currentColumn,dom);
			logInEmpCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(new java.util.Date());
			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			DistCommon distCommon = new DistCommon();
			FinCommon finCommon = new FinCommon();
			System.out.println("editFlag@@ : ["+editFlag+"]");
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			  case 1:
				System.out.println("ADJ RECEIPT case 1 itemchanged case 1");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
							
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					sqlSiteDescr="SELECT descr FROM SITE WHERE SITE_CODE= ?  ";
					pstmt1=conn.prepareStatement(sqlSiteDescr);
					pstmt1.setString(1,siteCode);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						//System.out.println("intered in result set hhho..");
						 siteDescr= checkNull(rs1.getString("descr"));	
						 System.out.println("item_default::"+siteDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					priceList = distCommon.getDisparams("999999", "DEFAULT_PRICELIST", conn);
					System.out.println("custType>>>>>>>"+priceList);
					if ( (priceList != null && priceList.trim().length()>0) && !"NULLFOUND".equalsIgnoreCase(priceList) )
					{
						valueXmlString.append("<price_list ><![CDATA[").append(checkNull(priceList)).append("]]></price_list>\r\n");
					}
					
					System.out.println("intered in itm default mode hhho..");
					//valueXmlString.append("<conf_date ><![CDATA[").append(checkNull(sysDate)).append("]]></conf_date>\r\n");
					valueXmlString.append("<tran_date ><![CDATA[").append(checkNull(sysDate)).append("]]></tran_date>\r\n");
					valueXmlString.append("<ref_ser protect=\"1\"><![CDATA[").append("ADJRCP").append("]]></ref_ser>\r\n");
					valueXmlString.append("<eff_date><![CDATA[").append(checkNull(sysDate)).append("]]></eff_date>\r\n");
					valueXmlString.append("<site_descr protect=\"1\" ><![CDATA[").append(siteDescr).append("]]></site_descr>\r\n");
					valueXmlString.append("<site_code><![CDATA[").append(siteCode).append("]]></site_code>\r\n");
					
				}
				
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{
					tranId = checkNull(genericUtility.getColumnValue("tran_id",dom));	
					priceListDom = checkNull(genericUtility.getColumnValue("price_list",dom));
					siteCodeDom = checkNull(genericUtility.getColumnValue("site_code",dom));
										
					sqlTran = "select Count(*) from adj_issrcpdet where TRAN_ID= ? ";
						pstmt1 = conn.prepareStatement(sqlTran);
						pstmt1.setString(1, tranId);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							cntTrn = rs1.getInt(1);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
		    	
						if(cntTrn >0)
						{	
							valueXmlString.append("<price_list protect=\"1\" ><![CDATA[").append(checkNull(priceListDom)).append("]]></price_list>\r\n");
							valueXmlString.append("<site_code protect=\"1\" ><![CDATA[").append(checkNull(siteCodeDom)).append("]]></site_code>\r\n");
						}
						else 
						{
							valueXmlString.append("<price_list protect=\"0\" ><![CDATA[").append("").append("]]></price_list>\r\n");
							valueXmlString.append("<site_code protect=\"0\" ><![CDATA[").append(siteCode).append("]]></site_code>\r\n");
							
						}
						
						
						
				}
				else if (currentColumn.trim().equalsIgnoreCase("site_code")) 
				{
					siteCodeDom = checkNull(genericUtility.getColumnValue("site_code",dom));
					sqlSiteDescr="SELECT descr FROM SITE WHERE SITE_CODE= ?  ";
					pstmt1=conn.prepareStatement(sqlSiteDescr);
					pstmt1.setString(1,siteCodeDom);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 //System.out.println("intered in result set hhho..");
						 siteItmDescr= checkNull(rs1.getString("descr"));	
						 System.out.println("site_code."+siteItmDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					valueXmlString.append("<site_descr protect=\"1\" ><![CDATA[").append(siteItmDescr).append("]]></site_descr>\r\n");
				}
				else if (currentColumn.trim().equalsIgnoreCase("dept_code")) 
				{
					deptCode=checkNull(genericUtility.getColumnValue("dept_code",dom));
					sqlDeptDescr="select descr from department where dept_code = ? ";
					pstmt1=conn.prepareStatement(sqlDeptDescr);
					pstmt1.setString(1,deptCode);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 //System.out.println("intered in result set hhho..");
						 deptDescr= checkNull(rs1.getString("descr"));	
						 System.out.println("dept_code"+deptDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					valueXmlString.append("<department_descr protect=\"1\" ><![CDATA[").append(deptDescr).append("]]></department_descr>\r\n");
				}
				else if (currentColumn.trim().equalsIgnoreCase("tran_date")) 
				{
					effDate=checkNull(genericUtility.getColumnValue("tran_date",dom));
					valueXmlString.append("<eff_date><![CDATA[").append(checkNull(effDate)).append("]]></eff_date>\r\n");
				}
		
			valueXmlString.append("</Detail1>");
			break;

			case 2 :
			    System.out.println("detail case 2 itemchanged case 2");
			 	parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
					
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
						line=getValueFromXTRA_PARAMS(xtraParams, "line_no");
	                    lineNo=line.length();
	                    tranId = checkNull(genericUtility.getColumnValue("tran_id",dom));
	                    mfgDate = checkNull(genericUtility.getColumnValue("mfg_date",dom));
	                    expDate = checkNull(genericUtility.getColumnValue("exp_date",dom));
	                   
	                    lineNoStr = checkNull(genericUtility.getColumnValue("line_no",dom));
	                    NodeList parentList = null;
	                    parentList = dom2.getElementsByTagName("Detail2");
	        			int parentNodeListLength = parentList.getLength();
	        			if(parentNodeListLength > 1)
	        			{
	        				previousLineLot= previousLot( dom2, lineNoStr);
	        				valueXmlString.append("<lot_sl><![CDATA[").append(previousLineLot).append("]]></lot_sl>\r\n");
	        			}
                        valueXmlString.append("<tran_id ><![CDATA[").append(tranId).append("]]></tran_id>\r\n");
                        valueXmlString.append("<mfg_date ><![CDATA[").append(mfgDate).append("]]></mfg_date>\r\n");
                        valueXmlString.append("<exp_date ><![CDATA[").append(expDate).append("]]></exp_date>\r\n");
                        
                        lotSL = distCommon.getDisparams("999999", "DEFAULT_LOT_SL", conn);
    					System.out.println("custTypesatre>>>>>>>"+lotSL);
    					
    					if ( !"NULLFOUND".equalsIgnoreCase(lotSL) && lotSL != null && lotSL.trim().length()>0 )
    					{
    						valueXmlString.append("<lot_sl ><![CDATA[").append(checkNull(lotSL)).append("]]></lot_sl>\r\n");
    					} 
    					
				}
				//Add by Ajay on 10/04/2018/:START

				if("itm_defaultedit".equalsIgnoreCase(currentColumn.trim()))
				{
				    
					tranId = checkNull(genericUtility.getColumnValue("tran_id",dom));	
					priceListDom = checkNull(genericUtility.getColumnValue("price_list",dom));
					siteCodeDom = checkNull(genericUtility.getColumnValue("site_code",dom));
										
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					itemSer = this.genericUtility.getColumnValue("item_ser", dom);
					System.out.println("itemcodedom"+itemCodeDom);
					sql = "select (case when stk_opt is null then '0' else stk_opt end) as stk_opt, item_ser  from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeDom);
					rs = pstmt.executeQuery();
					System.out.println("default edit sql ");
					if (rs.next())
					{
						stkOpt = checkNull(rs.getString("stk_opt")).trim();
						itemSer = rs.getString("item_ser");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if("1".equalsIgnoreCase(stkOpt))
					{
						System.out.println("itemdefault sql");
						valueXmlString.append("<lot_no protect=\"1\"><![CDATA[").append("               ").append("]]></lot_no>\r\n");
                        valueXmlString.append("<lot_sl protect=\"1\" ><![CDATA[").append("               ").append("]]></lot_sl>\r\n");
                        valueXmlString.append("<mfg_date protect=\"1\"><![CDATA[").append("").append("]]></mfg_date>\r\n");
                        valueXmlString.append("<exp_date protect=\"1\"><![CDATA[").append("").append("]]></exp_date>\r\n");
                        
					}
							
				}
				////Add by Ajay on 10/04/2018/:END 
				else if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					siteCodeDom=checkNull(genericUtility.getColumnValue("site_code",dom1));
					tranDate=checkNull(genericUtility.getColumnValue("tran_date",dom));
					priceListDom=checkNull(genericUtility.getColumnValue("price_list",dom));
					
					deptCode=checkNull(genericUtility.getColumnValue("dept_code",dom));
					sqlDeptDescr="select descr, unit ,unit__pur ,stk_opt ,pack_code FROM ITEM where  item_code = ? ";
					pstmt1=conn.prepareStatement(sqlDeptDescr);
					pstmt1.setString(1,itemCodeDom);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 System.out.println("intered in result set hhho..");
						 itemDescr= checkNull(rs1.getString("descr"));	
						 unit=checkNull(rs1.getString("unit"));
						 unitPur=checkNull(rs1.getString("unit__pur"));
						 stkOpt=checkNull(rs1.getString("stk_opt")).trim();
						 packCode=checkNull(rs1.getString("pack_code"));
						 
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					stkCnt=stkOpt.length();
					//Changes Add by Ajay Jadhav on 03/04/2018:START	
					if("1".equalsIgnoreCase(stkOpt))
					{
						valueXmlString.append("<lot_no protect=\"1\"><![CDATA[").append("               ").append("]]></lot_no>\r\n");
                        valueXmlString.append("<lot_sl protect=\"1\" ><![CDATA[").append("               ").append("]]></lot_sl>\r\n");
                        valueXmlString.append("<mfg_date protect=\"1\"><![CDATA[").append("").append("]]></mfg_date>\r\n");
                        valueXmlString.append("<exp_date protect=\"1\"><![CDATA[").append("").append("]]></exp_date>\r\n"); 
					}/*
					else
					{
						valueXmlString.append("<lot_no protect=\"0\"><![CDATA[").append(" ").append("]]></lot_no>\r\n");
                        valueXmlString.append("<lot_sl protect=\"0\" ><![CDATA[").append(" ").append("]]></lot_sl>\r\n");
						
					}*/
					//Changes Add by Ajay Jadhav on 03/04/2018:END
						
					
					
					locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
					lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
					qty=checkNull(genericUtility.getColumnValue("quantity",dom));
					
					valueXmlString.append("<item_descr ><![CDATA[").append(itemDescr).append("]]></item_descr>\r\n");
					valueXmlString.append("<unit protect=\"1\"><![CDATA[").append(unit).append("]]></unit>\r\n");
					valueXmlString.append("<pack_code ><![CDATA[").append(packCode).append("]]></pack_code>\r\n");
					
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][Start].
					
					//valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);//done
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [START]
					//String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);
					String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,dom1,dom,conn);
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [END]
					valueXmlString.append(valAcct);
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][End].
					
					if(unitPur!=null)
					{
						valueXmlString.append("<unit__alt ><![CDATA[").append(unitPur).append("]]></unit__alt>\r\n");
					}
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][Start].
					
					//protectRate(dom,valueXmlString,conn);//done
					//ArrayList<String> tempList= protectRate(dom2,valueXmlString,conn);
					//Changes by sarita on 12 SEP 18 [START]
					//ArrayList<String> tempList= protectRate(dom,valueXmlString,conn);
					ArrayList<String> tempList= protectRate(dom,dom1,valueXmlString,conn);
					//Changes by sarita on 12 SEP 18 [START]
					
					//rate =  tempList.get(0);
					valueXmlString.append(tempList.get(1));
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][End].
					
					setNodeValue( dom, "quantity", qty );
					reStr=itemChanged(dom,dom1, dom2, objContext,"quantity",editFlag,xtraParams); //done
					
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr); 
					
					
					
					sqlStockDe="SELECT BATCH_NO , SUPP_CODE__MFG , RETEST_DATE FROM STOCK  WHERE ITEM_CODE = ?	 AND SITE_CODE	= ?	AND LOC_CODE =	? AND  LOT_NO =	?	and lot_sl=	? ";
					pstmt1=conn.prepareStatement(sqlStockDe);
					pstmt1.setString(1,itemCodeDom);
					pstmt1.setString(2,siteCodeDom);
					pstmt1.setString(3,locCode);
					pstmt1.setString(4,lotNo);
					pstmt1.setString(5,lotSl);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						
						 System.out.println("intered in result set hhho..");
						 batchNO= checkNull(rs1.getString("BATCH_NO"));	
						 supCodeMfg=checkNull(rs1.getString("SUPP_CODE__MFG"));
						 retestDateTmStmp=rs1.getTimestamp("RETEST_DATE");
						
						 System.out.println("intered in rs BATCH_NO  hhho.."+batchNO);
						 System.out.println("intered in rs SUPP_CODE__MFGt  hhho.."+supCodeMfg);
						 System.out.println("intered in rs retestDateTmStmp  hhho.."+retestDateTmStmp);
						
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					if(!(batchNO==null || batchNO.trim().length()==0))
					{
						valueXmlString.append("<batch_no ><![CDATA[").append(batchNO).append("]]></batch_no>\r\n");
					}
					if(!(supCodeMfg==null || supCodeMfg.trim().length()==0))
					{
						valueXmlString.append("<supp_code__mfg ><![CDATA[").append(supCodeMfg).append("]]></supp_code__mfg>\r\n");
					}
					if(retestDateTmStmp !=null)
					{
						valueXmlString.append("<retest_date >").append("<![CDATA["+sdf.format(retestDateTmStmp).toString()+"]]>").append("</retest_date>");
						
					}else
					{
						valueXmlString.append("<retest_date ><![CDATA[").append("").append("]]></retest_date>\r\n");
					}
					valueXmlString.append("<pack_code ><![CDATA[").append(packCode).append("]]></pack_code>\r\n");
					//Start Added by chandra shekar on 25-may-2016
					sqlDeptDescr="select track_shelf_life  from item where item_code = ?  ";
					pstmt1=conn.prepareStatement(sqlDeptDescr);
					pstmt1.setString(1,itemCodeDom);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 trackShelfLife= checkNull(rs1.getString("track_shelf_life"));	
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					if ("Y".equalsIgnoreCase(trackShelfLife.trim()))
					{
						valueXmlString.append("<exp_date protect=\"0\"><![CDATA[").append("").append("]]></exp_date>\r\n");
						valueXmlString.append("<mfg_date protect=\"0\"><![CDATA[").append("").append("]]></mfg_date>\r\n");	
						
					}else
					{
						valueXmlString.append("<mfg_date protect=\"1\"><![CDATA[").append("").append("]]></mfg_date>\r\n");
						valueXmlString.append("<exp_date protect=\"1\"><![CDATA[").append("").append("]]></exp_date>\r\n");
					}
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("loc_code"))
				{
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					siteCodeDom=checkNull(genericUtility.getColumnValue("site_code",dom1));
					lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
					locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
					deptCode=checkNull(genericUtility.getColumnValue("dept_code",dom));
					
					sqlLocDescr="SELECT DESCR FROM LOCATION  WHERE LOC_CODE = ? ";
					pstmt1=conn.prepareStatement(sqlLocDescr);
					pstmt1.setString(1,locCode);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 System.out.println("intered in result set hhho.."); 
						 locDescr= checkNull(rs1.getString("descr"));	
						 System.out.println("intered in rs item descr  hhho.."+itemDescr); 
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					valueXmlString.append("<location_descr ><![CDATA[").append(locDescr).append("]]></location_descr>\r\n");
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][Start].
					
					//valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);//done
					
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [START]
					//String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);
					String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,dom1,dom,conn);
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [END]
					valueXmlString.append(valAcct);
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][End].
					
					sqlStockDe="SELECT BATCH_NO , SUPP_CODE__MFG , RETEST_DATE FROM STOCK  WHERE ITEM_CODE = ?	 AND SITE_CODE	= ?	AND LOC_CODE =	? AND  LOT_NO =	?	and lot_sl=	? ";
					pstmt1=conn.prepareStatement(sqlStockDe);
					pstmt1.setString(1,itemCodeDom);
					pstmt1.setString(2,siteCodeDom);
					pstmt1.setString(3,locCode);
					pstmt1.setString(4,lotNo);
					pstmt1.setString(5,lotSl);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						
						 System.out.println("intered in result set hhho..");
						 batchNO= checkNull(rs1.getString("BATCH_NO"));	
						 supCodeMfg=checkNull(rs1.getString("SUPP_CODE__MFG"));
						 retestDateTmStmp=rs1.getTimestamp("RETEST_DATE");
						
						 System.out.println("intered in rs BATCH_NO  hhho.."+batchNO);
						 System.out.println("intered in rs SUPP_CODE__MFGt  hhho.."+supCodeMfg);
						 System.out.println("intered in rs retestDateTmStmp  hhho.."+retestDateTmStmp);
						
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					if(!(batchNO==null || batchNO.trim().length()==0))
					{
						valueXmlString.append("<batch_no ><![CDATA[").append(batchNO).append("]]></batch_no>\r\n");
					}
					if(!(supCodeMfg==null || supCodeMfg.trim().length()==0))
					{
						valueXmlString.append("<supp_code__mfg ><![CDATA[").append(supCodeMfg).append("]]></supp_code__mfg>\r\n");
					}
					if(retestDateTmStmp !=null)
					{
						valueXmlString.append("<retest_date >").append("<![CDATA["+sdf.format(retestDateTmStmp).toString()+"]]>").append("</retest_date>");
						
					}else
					{
						valueXmlString.append("<retest_date ><![CDATA[").append("").append("]]></retest_date>\r\n");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("quantity") || currentColumn.trim().equalsIgnoreCase("rate"))
				{
					qty=checkNull(genericUtility.getColumnValue("quantity",dom));
					rate=checkNull(genericUtility.getColumnValue("rate",dom));
					if(rate.trim().length() > 0)
					{
						mRate=Double.parseDouble(rate);
					}
					if(qty.trim().length() > 0)
					{
						mQty=Double.parseDouble(qty);
					}
					mAmount=mQty * mRate;
					System.out.println("RATE ITEM CHANGE CALLED "+rate);
					valueXmlString.append("<amount ><![CDATA[").append(mAmount).append("]]></amount>\r\n");
					
					if (currentColumn.trim().equalsIgnoreCase("quantity"))
					{
							itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
							lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
							packCode=checkNull(genericUtility.getColumnValue("pack_code",dom));
							System.out.println("Pavan Rane DOM[  "+genericUtility.serializeDom(dom)+"]\n\n\n");
							//Pavan R on may19[no of art to consider from item_lot_packsize, item and if not found then packing master]
							double noOfArt = 0d, shipperSize = 0d;				
							sql = "select (case when shipper_size is null then 0 else shipper_size end) as shipper_size, "
								+ "(case when gross_weight is null then 0 else gross_weight end) as lc_gross_weigth, "
								+ "(case when net_weight is null then 0 else net_weight end) as lc_net_weight "
								+ "from item_lot_packsize where item_code = ? "
								+ "and ? >= lot_no__from and ? <= lot_no__to";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeDom);
							pstmt.setString(2, lotNo);
							pstmt.setString(3, lotNo);
							rs = pstmt.executeQuery();				
							if(rs.next())
							{
								shipperSize = rs.getDouble("shipper_size");							
								mGrossWeight= rs.getDouble("LC_GROSS_WEIGTH");
								mNetWeight= rs.getDouble("LC_NET_WEIGHT");
								System.out.println("Pavan R shipperSize["+shipperSize+"] grossWeight [ "+grossWeight+" ] netWeight[ "+netWeight+" ]");	
								if (shipperSize > 0) 
								{		
									double mod = mQty/shipperSize;
									noOfArt = getRndamt(mod , "X", 1);
									System.out.println("Pavan R noOfArt["+noOfArt+"]");
									valueXmlString.append("<no_art><![CDATA[").append(noOfArt).append("]]></no_art>\r\n");
									if(noOfArt > 0)
									{
										//mGrosswt = noOfArt *  mGrossWeight;
										mGrosswt = ((mGrossWeight/shipperSize) * mQty); 
										//mNetWt = noOfArt * mNetWeight;
										mNetWt = ((mNetWeight/shipperSize) * mQty);
										System.out.println("Pavan R mGrosswt [ "+mGrosswt+" ]");
										if(mGrossWeight > 0)
										{
											valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimal(mGrosswt,3)).append("]]></gross_weight>\r\n");
											valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWt,3)).append("]]></net_weight>\r\n");										
											mTareWt=mGrosswt - mNetWt;
											System.out.println("Pavan R mGrossWeight > 0 mTareWt [ "+mTareWt+" ]");
											valueXmlString.append("<tare_weight ><![CDATA[").append(getRequiredDecimal(mTareWt, 3)).append("]]></tare_weight>\r\n");
										}									
									} //if(noOfArt > 0)																
								}//if(shipperSize > 0)  						
							}						
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;															
							if(shipperSize == 0)
							{							
								System.out.println("Pavan Rane After ShipperSize ==0 PackingCode ["+packCode+"]");
								//if(!(packCode==null || packCode.trim().length()>0))
								//Pavan R end
								if(packCode != null && packCode.trim().length()>0)
								{
									double mlcMode = 0d;
									sqlCapacity="select capacity from packing where pack_code = ? ";
									pstmt1=conn.prepareStatement(sqlCapacity);
									pstmt1.setString(1,packCode);
									rs1 = pstmt1.executeQuery();								
									if( rs1.next())
									{	
										 //capacity= checkNull(rs1.getString("capacity"));	
										mCapacity= rs1.getDouble("capacity");
										 System.out.println("Pavan R Capacity::["+mCapacity+"]");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close(); 
							        pstmt1=null;
							        /*if(capacity.trim().length() > 0 )
							        {
							        	mCapacity=Double.parseDouble(capacity);
							        }*/					       													
								}
								//if((packCode!=null && packCode.trim().length()>0) && (capacity !=null && capacity.trim().length()>0))
								if((packCode!=null && packCode.trim().length()>0) && (mCapacity > 0))
								{
									/*sqlLcMode="SELECT FN_MOD( ? ,? ) AS LC_MODE FROM DUAL ";
									pstmt1=conn.prepareStatement(sqlLcMode);
									pstmt1.setDouble(1,mQty);//pstmt1.setString(1,qty);
									pstmt1.setDouble(2, mCapacity);//pstmt1.setString(2,capacity);
									rs1 = pstmt1.executeQuery();								
									if( rs1.next())
									{									 
										 //lcMode= checkNull(rs1.getString("LC_MODE"));	
										lcMode= checkNull(rs1.getString("LC_MODE"));
										 System.out.println("Pavan Rane FN_MOD for qty["+qty+"] capacity["+capacity+"] = "+lcMode+"]");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close(); 
									pstmt1=null;*/								
									double mod = mQty/mCapacity;
									mNoart = getRndamt(mod, "X", 1);
									System.out.println("Pavan R mNoart["+mNoart+"]");
									/*if(lcMode.length()>0)
									{
										mNoart=(mQty / mCapacity) + 1; //in pb int is used before open brackets
										//mNoart=(getRndamt(mQty, "X", 1));//
										 System.out.println("Pavan Rane Mod>0 mNoart["+mNoart+"]");
									}
									else 
									{
										mNoart=(mQty / mCapacity) ; //in pb int is used before open brackets
										System.out.println("Pavan Rane Mod<0 less mNoart["+mNoart+"]");
									}*/
								
								valueXmlString.append("<no_art ><![CDATA[").append(mNoart).append("]]></no_art>\r\n");						
								if(( mNoart > 0))
								{
									sqlLcMode="SELECT (CASE WHEN GROSS_WEIGHT IS NULL THEN 0 ELSE GROSS_WEIGHT END) AS LC_GROSS_WEIGTH, (CASE WHEN NET_WEIGHT IS NULL THEN 0 ELSE NET_WEIGHT END) AS LC_NET_WEIGHT FROM 	ITEM_LOT_PACKSIZE 	WHERE ITEM_CODE = ?	AND  ? BETWEEN LOT_NO__FROM AND LOT_NO__TO ";
									pstmt1=conn.prepareStatement(sqlLcMode);
									pstmt1.setString(1,itemCodeDom);
									pstmt1.setString(2,lotNo);
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next())
									{									 
										 grossWeight= checkNull(rs1.getString("LC_GROSS_WEIGTH"));
										 netWeight= checkNull(rs1.getString("LC_NET_WEIGHT"));
										 System.out.println("Pavan Rane grossWeight ["+grossWeight+"]netWeight["+netWeight+"]");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close(); 
									pstmt1=null;
									if(grossWeight.trim().length() > 0)
									{
										mGrossWeight=Double.parseDouble(grossWeight);
									}
									if(netWeight.trim().length() > 0)
									{
										mNetWeight=Double.parseDouble(netWeight);
									}
									
									mGrosswt = ((mGrossWeight/mCapacity) * mQty); 								
									mNetWt = ((mNetWeight/mCapacity) * mQty);
									
									//mGrosswt= mNoart *  mGrossWeight;
									System.out.println("Pavan Rane total mGrosswt["+mGrosswt+"]  mGrossWeight["+mGrossWeight+"]");
									if(mGrossWeight > 0)
									{
										valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimal(mGrosswt, 3)).append("]]></gross_weight>\r\n");
										//valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWeight, 3)).append("]]></net_weight>\r\n");
										valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWt, 3)).append("]]></net_weight>\r\n");
										//mTareWt=mGrosswt - mNetWeight;
										mTareWt=mGrosswt - mNetWt;
										valueXmlString.append("<tare_weight ><![CDATA[").append(getRequiredDecimal(mTareWt, 3)).append("]]></tare_weight>\r\n");
									}
									
								  }
								}
								else 
								{
									sqlNetwtItem="SELECT (CASE WHEN NET_WEIGHT IS NULL THEN 0 ELSE NET_WEIGHT END) AS NET_WT_ITEM, INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ? ";
									pstmt1=conn.prepareStatement(sqlNetwtItem);
									pstmt1.setString(1,itemCodeDom);
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next())
									{									 
										 netWtItem= checkNull(rs1.getString("NET_WT_ITEM"));
										 netWtItem= checkNull(rs1.getString("INTEGRAL_QTY"));
										 System.out.println("Pavan Rane else netWtItem["+netWtItem+"]  netWtItem["+netWtItem+"]");	 
									}
									rs1.close();
									rs1 = null;
									pstmt1.close(); 
									pstmt1=null;
									
									sqlNetwt="SELECT (CASE WHEN NET_WEIGHT IS NULL THEN 0 ELSE NET_WEIGHT END) AS NET_WT FROM ITEM_LOT_PACKSIZE	WHERE ITEM_CODE = ?	AND	? BETWEEN LOT_NO__FROM AND LOT_NO__TO ";
									pstmt1=conn.prepareStatement(sqlNetwt);
									pstmt1.setString(1,itemCodeDom);
									pstmt1.setString(2,lotNo);
									rs1 = pstmt1.executeQuery();
									
									if( rs1.next())
									{	
										// System.out.println("intered in result set hhho..");
										 netWt= checkNull(rs1.getString("NET_WT"));
										 //System.out.println("intered in rs netWt  hhho.."+netWt);
										 System.out.println("Pavan Rane netWt["+netWt+"]");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close(); 
									pstmt1=null;
									
									
									if(netWtItem.trim().length() > 0)
									{
										mNetWtItem=Double.parseDouble(netWtItem);
									}
									if(netWt.trim().length() > 0)
									{
										mNetWt=Double.parseDouble(netWt);
									}
									if(integrlQty.trim().length()>0)
									{
										mIntegrlQty=Double.parseDouble(integrlQty);
									}
									
									if(mNetWt==0)
									{
										mNetWt=mNetWtItem;
									}
									
									if(mNetWt==0)
									{
										
									}
									else
									{
										mNetWt=mNetWt * mQty;
										if(mIntegrlQty>0)
										{
											sqlLcMode="SELECT FN_MOD( ? ,? ) AS LC_MODE FROM DUAL ";
											pstmt1=conn.prepareStatement(sqlLcMode);
											pstmt1.setString(1,qty);
											pstmt1.setString(2,integrlQty);
											rs1 = pstmt1.executeQuery();
											
											if( rs1.next())
											{	
												 System.out.println("intered in result set hhho..");
												 lcMode= checkNull(rs1.getString("LC_MODE"));	
												 System.out.println("intered in rs item descr  hhho.."+lcMode);
											}
											rs1.close();
											rs1 = null;
											pstmt1.close(); 
											pstmt1=null;
											if(lcMode.length()>0)
											{
												mNoart=(mQty / mIntegrlQty) + 1; //in pb int is used before open brackets
											}
											else 
											{
												mNoart=(mQty / mIntegrlQty) ; //in pb int is used before open brackets
											}
										}
										else
										{
											mNoart=mQty;
										}
									}
									valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimal(mNetWt, 3)).append("]]></gross_weight>\r\n");
									valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWt, 3)).append("]]></net_weight>\r\n");
									valueXmlString.append("<tare_weight ><![CDATA[").append("0").append("]]></tare_weight>\r\n");
									valueXmlString.append("<no_art ><![CDATA[").append(mNoart).append("]]></no_art>\r\n");
									
								}
							}
							itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
							
							sql="SELECT ITEM_TYPE, UNIT__NETWT, UNIT__RATE FROM ITEM WHERE ITEM_CODE = ? ";
							pstmt1=conn.prepareStatement(sql);
							pstmt1.setString(1,itemCodeDom);
							rs1 = pstmt1.executeQuery();
							
							if( rs1.next())
							{	
								 System.out.println("intered in result set hhho..");
								 itemType= checkNull(rs1.getString("ITEM_TYPE"));
								 unitNetWt= checkNull(rs1.getString("UNIT__NETWT"));	
								 unitRate= checkNull(rs1.getString("UNIT__RATE"));	
								 
								 System.out.println("ITEM_TYPE in rs item descr  hhho.."+itemType);
								 System.out.println("UNIT__NETWT in rs item descr  hhho.."+unitNetWt);
								 System.out.println("UNIT__RATE in rs item descr  hhho.."+unitRate);
							}
							rs1.close();
							rs1 = null;
							pstmt1.close(); 
							pstmt1=null;
							
							if(itemType!=null && itemType.trim().length() > 0)
							{
								sqlItem="SELECT RATE_OPT  FROM ITEM_TYPE WHERE  ITEM_TYPE = ? ";
								pstmt1=conn.prepareStatement(sqlItem);
								pstmt1.setString(1,itemType);
								rs1 = pstmt1.executeQuery();
								
								if( rs1.next())
								{	
									 System.out.println("intered in result set hhho..");
									 rateOpt= checkNull(rs1.getString("RATE_OPT"));	
									 System.out.println("intered in rs rateOpt hhho.."+rateOpt);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close(); 
								pstmt1=null;
								if(rateOpt.trim().length()==1)
								{
									if(unitNetWt.trim().equalsIgnoreCase(unitRate.trim()))
									{
										unit=checkNull(genericUtility.getColumnValue("unit",dom));
										if(!unit.trim().equalsIgnoreCase(unitRate.trim()))
										{
											valueXmlString.append("<no_art protect=\"1\"><![CDATA[").append(mQty).append("]]></no_art>\r\n");
										}
										else
										{
											valueXmlString.append("<no_art protect=\"0\"><![CDATA[").append(mQty).append("]]></no_art>\r\n");
										}
									}
								}
						}
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("lot_no"))
				{
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					siteCodeDom=checkNull(genericUtility.getColumnValue("site_code",dom1));
					lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
					locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
					priceListDom=checkNull(genericUtility.getColumnValue("price_list",dom));
					qty=checkNull(genericUtility.getColumnValue("quantity",dom));
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][Start].
					
					//valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);
					
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [START]
					//String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);
					String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,dom1,dom,conn);
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [END]
					valueXmlString.append(valAcct);
					
					//protectRate(dom,valueXmlString,conn);
					//ArrayList<String> tempList= protectRate(dom2,valueXmlString,conn);
					
					//Changes by sarita on 12 SEP 18 [START]
					//ArrayList<String> tempList= protectRate(dom,valueXmlString,conn);
					ArrayList<String> tempList= protectRate(dom,dom1,valueXmlString,conn);
					//Changes by sarita on 12 SEP 18 [END]			
					
					//rate =  tempList.get(0);
					valueXmlString.append(tempList.get(1));
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][End].
					
					setNodeValue( dom, "quantity", qty );
					reStr=itemChanged(dom,dom1, dom2, objContext,"quantity",editFlag,xtraParams); //done
					
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
					
					sqlStockDe="SELECT BATCH_NO , SUPP_CODE__MFG , RETEST_DATE FROM STOCK  WHERE ITEM_CODE = ?	 AND SITE_CODE	= ?	AND LOC_CODE =	? AND  LOT_NO =	?	and lot_sl=	? ";
					pstmt1=conn.prepareStatement(sqlStockDe);
					pstmt1.setString(1,itemCodeDom);
					pstmt1.setString(2,siteCodeDom);
					pstmt1.setString(3,locCode);
					pstmt1.setString(4,lotNo);
					pstmt1.setString(5,lotSl);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						
						 System.out.println("intered in result set hhho..");
						 batchNO= checkNull(rs1.getString("BATCH_NO"));	
						 supCodeMfg=checkNull(rs1.getString("SUPP_CODE__MFG"));
						 retestDateTmStmp=rs1.getTimestamp("RETEST_DATE");
						
						 System.out.println("intered in rs BATCH_NO  hhho.."+batchNO);
						 System.out.println("intered in rs SUPP_CODE__MFGt  hhho.."+supCodeMfg);
						 System.out.println("intered in rs retestDateTmStmp  hhho.."+retestDateTmStmp);
						
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					
					if(!(batchNO==null || batchNO.trim().length()==0))
					{
						valueXmlString.append("<batch_no ><![CDATA[").append(batchNO).append("]]></batch_no>\r\n");
					}
					if(!(supCodeMfg==null || supCodeMfg.trim().length()==0))
					{
						valueXmlString.append("<supp_code__mfg ><![CDATA[").append(supCodeMfg).append("]]></supp_code__mfg>\r\n");
					}
					if(retestDateTmStmp !=null)
					{
						valueXmlString.append("<retest_date >").append("<![CDATA["+sdf.format(retestDateTmStmp).toString()+"]]>").append("</retest_date>");
						
					}else
					{
						valueXmlString.append("<retest_date ><![CDATA[").append("").append("]]></retest_date>\r\n");
					}
				}
				
				else if (currentColumn.trim().equalsIgnoreCase("lot_sl"))
				{
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					siteCodeDom=checkNull(genericUtility.getColumnValue("site_code",dom1));
					lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
					locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
					rate=checkNull(genericUtility.getColumnValue("rate",dom));
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][Start].
					
					//valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);
					
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [START]
					//String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,conn);
					String valAcct = valAcct(itemCodeDom,siteCodeDom,locCode,lotNo,lotSl,valueXmlString,dom2,dom1,dom,conn);
					//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [END]
					valueXmlString.append(valAcct);
					
					//rate=protectRate(dom2,valueXmlString,conn);
					
					//Changes by sarita on 12 SEP 18 [START]
					//ArrayList<String> tempList= protectRate(dom2,valueXmlString,conn);
					ArrayList<String> tempList= protectRate(dom,dom1,valueXmlString,conn);
					//Changes by sarita on 12 SEP 18 [END]
					rate =  tempList.get(0);
					valueXmlString.append(tempList.get(1));
					
					//Modified by Rupesh on[24/10/2017][As Instructed by Piyush Sir][End].
					
					setNodeValue( dom, "rate",rate );
					reStr=itemChanged(dom,dom1, dom2, objContext,"rate",editFlag,xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
					
					
					sqlStockDe="SELECT BATCH_NO ,SUPP_CODE__MFG , RETEST_DATE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE	= ?	AND LOC_CODE =	? AND  LOT_NO =	? and lot_sl= ? ";
					pstmt1=conn.prepareStatement(sqlStockDe);
					pstmt1.setString(1,itemCodeDom);
					pstmt1.setString(2,siteCodeDom);
					pstmt1.setString(3,locCode);
					pstmt1.setString(4,lotNo);
					pstmt1.setString(5,lotSl);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						
						 System.out.println("intered in result set hhho..");
						 batchNO= checkNull(rs1.getString("BATCH_NO"));	
						 supCodeMfg=checkNull(rs1.getString("SUPP_CODE__MFG"));
						 retestDateTmStmp=rs1.getTimestamp("RETEST_DATE");
						
						 System.out.println("intered in rs BATCH_NO  hhho.."+batchNO);
						 System.out.println("intered in rs SUPP_CODE__MFGt  hhho.."+supCodeMfg);
						 System.out.println("intered in rs retestDateTmStmp  hhho.."+retestDateTmStmp);
						
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					if(!(batchNO==null || batchNO.trim().length()==0))
					{
						valueXmlString.append("<batch_no ><![CDATA[").append(batchNO).append("]]></batch_no>\r\n");
					}
					if(!(supCodeMfg==null || supCodeMfg.trim().length()==0))
					{
						valueXmlString.append("<supp_code__mfg ><![CDATA[").append(supCodeMfg).append("]]></supp_code__mfg>\r\n");
					}
					if(retestDateTmStmp !=null)
					{
						valueXmlString.append("<retest_date >").append("<![CDATA["+sdf.format(retestDateTmStmp).toString()+"]]>").append("</retest_date>");
						
					}else
					{
						valueXmlString.append("<retest_date ><![CDATA[").append("").append("]]></retest_date>\r\n");
					}
				}
				
				else if ((currentColumn.trim().equalsIgnoreCase("gross_weight"))|| (currentColumn.trim().equalsIgnoreCase("net_weight")) || (currentColumn.trim().equalsIgnoreCase("tare_weight")))
				{
					grossWeight=checkNull(genericUtility.getColumnValue("gross_weight",dom));
					netWeight=checkNull(genericUtility.getColumnValue("net_weight",dom));
					tareWeight=checkNull(genericUtility.getColumnValue("tare_weight",dom));
					if(grossWeight !=null && grossWeight.trim().length() > 0 )
					{
						mGrossWeight=Double.parseDouble(grossWeight);
					}
					if(netWeight !=null && netWeight.trim().length() > 0)
					{
						mNetWeight=Double.parseDouble(netWeight);
					}
					if(tareWeight !=null && tareWeight.trim().length() > 0)
					{
						mTareWt=Double.parseDouble(tareWeight);
					}
					
					
					if(currentColumn.trim().equalsIgnoreCase("net_weight"))
					{
						mGrossWeight=mNetWeight + mTareWt;
						valueXmlString.append("<gross_weight ><![CDATA[").append(mGrossWeight).append("]]></gross_weight>\r\n");
					}
					if(currentColumn.trim().equalsIgnoreCase("gross_weight"))
					{
						mNetWeight=  mGrossWeight - mTareWt;
						valueXmlString.append("<net_weight ><![CDATA[").append(mNetWeight).append("]]></net_weight>\r\n");
					}
					if(currentColumn.trim().equalsIgnoreCase("tare_weight"))
					{
						mNetWeight=  mGrossWeight - mTareWt;
						valueXmlString.append("<net_weight ><![CDATA[").append(mNetWeight).append("]]></net_weight>\r\n");
					}
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("unit__alt"))
				{
					unitPur=checkNull(genericUtility.getColumnValue("unit__alt",dom));
					unit=checkNull(genericUtility.getColumnValue("unit",dom));
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					qty=checkNull(genericUtility.getColumnValue("quantity",dom));
					if(qty.trim().length() > 0)
					{
						mQty=Double.parseDouble(qty);
					}
					convAr=distCommon.convQtyFactor(unitPur, unit, itemCodeDom, mQty, itmQty, conn);
					convQtyFact = Double.parseDouble( convAr.get(0).toString() );
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append(convQtyFact).append("]]></conv__qty_stduom>\r\n");
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("mfg_date"))
				{
					mfgDate=checkNull(genericUtility.getColumnValue("mfg_date",dom));
					expDate=checkNull(genericUtility.getColumnValue("exp_date",dom));
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
					
					sqlDeptDescr="select track_shelf_life  from item where item_code = ?  ";
					pstmt1=conn.prepareStatement(sqlDeptDescr);
					pstmt1.setString(1,itemCodeDom);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 trackShelfLife= checkNull(rs1.getString("track_shelf_life"));	
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					if ("Y".equalsIgnoreCase(trackShelfLife.trim()))
					{
						if(!(expDate==null) && expDate.trim().length()>0 )
						{
							mExpdateTimeStamp=Timestamp.valueOf(genericUtility.getValidDateString(expDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						else if(!(mfgDate==null) && mfgDate.trim().length()>0)
						{
							sqlTrackshelf="select (case when shelf_life is null then 0 else shelf_life end) as mshlife from 	item_lot_packsize	WHERE ITEM_CODE = ?	and  ?  between lot_no__from and lot_no__to";
							pstmt1=conn.prepareStatement(sqlTrackshelf);
							pstmt1.setString(1,itemCodeDom);
							pstmt1.setString(2,lotNo);
							rs1 = pstmt1.executeQuery();
							
							if( rs1.next())
							{	
								shelflife =rs1.getDouble("mshlife");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close(); 
							pstmt1=null;		
					
							if(shelflife==0)
							{
								sqlTracklife="select ( case when shelf_life is null then 0 else shelf_life end ) as mshlife from item where item_code = ? ";
								pstmt1=conn.prepareStatement(sqlTracklife);
								pstmt1.setString(1,itemCodeDom);
								rs1 = pstmt1.executeQuery();
								
								if( rs1.next())
								{	
									 shelflife =rs1.getDouble("mshlife");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close(); 
								pstmt1=null;	
								
								/*MfgDateTimeStamp=Timestamp.valueOf(genericUtility.getValidDateString(mfgDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
								mExpdateTimeStamp=distCommon.CalcExpiry(MfgDateTimeStamp,shelflife);*/
								
							}
							// Changed by Sneha on 14-02-2017, to calculate mfg date [Start]
							MfgDateTimeStamp=Timestamp.valueOf(genericUtility.getValidDateString(mfgDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
							mExpdateTimeStamp=distCommon.CalcExpiry(MfgDateTimeStamp,shelflife);
							// Changed by Sneha on 14-02-2017, to calculate mfg date [End]
						}
						else 
						{
							mExpdateTimeStamp=null;
						}
						if(mExpdateTimeStamp !=null)
						{
							valueXmlString.append("<exp_date >").append("<![CDATA["+sdf.format(mExpdateTimeStamp).toString()+"]]>").append("</exp_date>");
						}else
						{
							valueXmlString.append("<exp_date ><![CDATA[").append("").append("]]></exp_date>\r\n");
						}
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("exp_date"))
				{
					mMfgDate=checkNull(genericUtility.getColumnValue("mfg_date",dom));
					expDate=checkNull(genericUtility.getColumnValue("exp_date",dom));
					itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
					lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
					 if(mMfgDate !=null && mMfgDate.trim().length()>0)
					 {
						 mExpdateTimeStamp=Timestamp.valueOf(genericUtility.getValidDateString(mMfgDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"); 
					 }

					sqlDeptDescr="select track_shelf_life  from item where item_code = ?  ";
					pstmt1=conn.prepareStatement(sqlDeptDescr);
					pstmt1.setString(1,itemCodeDom);
					rs1 = pstmt1.executeQuery();
					
					if( rs1.next())
					{	
						 trackShelfLife= checkNull(rs1.getString("track_shelf_life"));	
						 
					}
					rs1.close();
					rs1 = null;
					pstmt1.close(); 
					pstmt1=null;
					
					if ("Y".equalsIgnoreCase(trackShelfLife.trim()))
					{
						if(!(expDate==null) && expDate.trim().length()>0)
						{

							sqlTrackshelf="select (case when shelf_life is null then 0 else shelf_life end) as mshlife from 	item_lot_packsize	WHERE ITEM_CODE = ?	and  ?  between lot_no__from and lot_no__to";
							pstmt1=conn.prepareStatement(sqlTrackshelf);
							pstmt1.setString(1,itemCodeDom);
							pstmt1.setString(2,lotNo);
							rs1 = pstmt1.executeQuery();
							
							if( rs1.next())
							{	
								 shelflife =rs1.getDouble("mshlife");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close(); 
							pstmt1=null;		
							

							if(shelflife ==0)
							{
								sqlTracklife="select ( case when shelf_life is null then 0 else shelf_life end ) as mshlife from item where item_code = ? ";
								pstmt1=conn.prepareStatement(sqlTracklife);
								pstmt1.setString(1,itemCodeDom);
								rs1 = pstmt1.executeQuery();
								
								if( rs1.next())
								{	
									 shelflife =rs1.getDouble("mshlife");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close(); 
								pstmt1=null;
								MfgDateTimeStamp=distCommon.CalcExpiry(mExpdateTimeStamp, shelflife * -1 ); //done
							}	
						}
						else 
						{
							MfgDateTimeStamp=mExpdateTimeStamp;
						}
					}
					if(MfgDateTimeStamp !=null)
					{
						valueXmlString.append("<mfg_date >").append("<![CDATA["+sdf.format(MfgDateTimeStamp).toString()+"]]>").append("</mfg_date>");
					
					}else
					{
						valueXmlString.append("<mfg_date ><![CDATA[").append("").append("]]></mfg_date>\r\n");
					}
					
				}
				
//				Modified By Aniket C. on[20th/APR/2021] Calculate Quantity On Basis Dimension and No_Art[Start]
				if (currentColumn.trim().equalsIgnoreCase("no_art") || currentColumn.trim().equalsIgnoreCase("dimension")) {
					System.out.println("Inside no_art block or dimension block");
					String itemCode = "", dimension = "", noArtStr = "";
					double quantity = 0.0d , noArt = 0.0d;

					itemCode = genericUtility.getColumnValue("item_code", dom);
					noArtStr = genericUtility.getColumnValue("no_art", dom);
					dimension = genericUtility.getColumnValue("dimension", dom);

					System.out.println("item code>>" + itemCode + "\ndimension>>" + dimension + "\nno of articles>>" + noArtStr);

					if (dimension != null && dimension.trim().length() > 0) {
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							unit = rs.getString("UNIT");
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
						if (rs != null) {
							rs.close();
							rs = null;
						}
						System.out.println("unit>>" + unit);

						if (noArtStr != null && noArtStr.trim().length() > 0) {
							noArt = Double.parseDouble(noArtStr);
						} else {
							noArt = 1;
						}
						
						System.out.println("dimension>>" + dimension + "\n no of articles>>" + noArt);

						if ("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit)) {

							quantity = distCommon.getQuantity(dimension, noArt, unit, conn);

							System.out.println("quantity in dimension block>>" + quantity);
							
							valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
							setNodeValue(dom, "quantity", getAbsString(String.valueOf(quantity)));
								reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
								/*System.out.println("quantity after itemchanged");
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0, pos);
								valueXmlString.append(reStr);	*/							
						}
					}
				}
//				Modified By Aniket C. on[20th/APR/2021] Calculate Quantity On Basis Dimension and No_Art[End]
					
			valueXmlString.append("</Detail2>");
			break;
			}
			
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
					if(pstmt1 != null)
						pstmt1.close();
					if(pstmt2 != null)
						pstmt2.close();
					if(rs1 != null)
						rs1.close();
					rs1 = null;
					if(rs2 != null)
						rs2.close();
					rs2 = null;
					pstmt1 = null;
					pstmt2 = null;
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

	//Modified by Rupesh on [24/10/2017][as instructed by Piyush sir]
	//private String protectRate(Document dom,StringBuffer valueXmlString,Connection conn) throws RemoteException, ITMException
	//private ArrayList<String> protectRate(Document dom,StringBuffer valueXmlString,Connection conn) throws RemoteException, ITMException
	//Modified by Rupesh on [02/11/2017][As Instructed by Piyush Sir].
	//Changes by sarita on 12 SEP 2018 [START]
	//private ArrayList<String> protectRate(Document dom,StringBuffer sb,Connection conn) throws RemoteException, ITMException
	private ArrayList<String> protectRate(Document dom,Document dom1,StringBuffer sb,Connection conn) throws RemoteException, ITMException
	//Changes by sarita on 12 SEP 2018 [END]
	{
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs1 = null ;
		ResultSet rs2 = null ;
		String itemCodeDom="",siteCodeDom="",sql="",lotNo="" , lotSl="", locCode="",tranDate="",priceList="",qty="",rate="";
		String pType="";
		double mQty=0,mRate=0;
		DistCommon distCommon = new DistCommon();
		ArrayList<String> retList = new ArrayList<String>();//added by Rupesh on[24/10/2017]
		StringBuffer valueXmlString = new StringBuffer();//added by Rupesh on[02/11/2017]
		
		try
		{	
			System.out.println("DOM SARITA::5555" +genericUtility.serializeDom(dom));
			System.out.println("DOM 1 SARITA::5555" +genericUtility.serializeDom(dom1));
			itemCodeDom=checkNull(genericUtility.getColumnValue("item_code",dom));
			//siteCodeDom=checkNull(genericUtility.getColumnValue("site_code",dom));
			siteCodeDom=checkNull(genericUtility.getColumnValue("site_code",dom1));
			lotNo=checkNull(genericUtility.getColumnValue("lot_no",dom));
			lotSl=checkNull(genericUtility.getColumnValue("lot_sl",dom));
			locCode=checkNull(genericUtility.getColumnValue("loc_code",dom));
			tranDate=checkNull(genericUtility.getColumnValue("tran_date",dom1));	// DOM TO DOM1 CHANGE BY NANDKUMAR GADKARI ON 15-05-20
			priceList=checkNull(genericUtility.getColumnValue("price_list",dom1));// DOM TO DOM1 CHANGE BY NANDKUMAR GADKARI ON 15-05-20	
			qty=checkNull(genericUtility.getColumnValue("quantity",dom));
			System.out.println("priceList>>>"+priceList);
			
			if(qty.trim().length() > 0)
			{
				mQty=Double.parseDouble(qty);
			}
			if(priceList != null && priceList.trim().length()>0)
			{
				pType=distCommon.getPriceListType(priceList, conn);//done
			}
			
			sql=" SELECT RATE FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ?	AND LOT_NO  = ?	AND LOT_SL = ? ";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1,itemCodeDom);
			pstmt1.setString(2,siteCodeDom);
			pstmt1.setString(3,locCode);
			pstmt1.setString(4,lotNo);
			pstmt1.setString(5,lotSl);
			
			rs1 = pstmt1.executeQuery();
			
			if( rs1.next())
			{	
				 System.out.println("intered in result set hhho..");
				 rate= checkNull(rs1.getString("RATE"));	
				 System.out.println("intered in rs Rate hhho.."+rate);
			}
			rs1.close();
			rs1 = null;
			pstmt1.close(); 
			pstmt1=null;
			if(rate.trim().length() > 0)
			{
				mRate=Double.parseDouble(rate);
			}
			
			
			valueXmlString.append("<rate protect=\"1\"><![CDATA[").append(mRate).append("]]></rate>\r\n");
			valueXmlString.append("<gross_rate ><![CDATA[").append(mRate).append("]]></gross_rate>\r\n");
			
			if(rate.trim().length() <=0)
			{
				if(priceList !=null && priceList.trim().length() > 0 )
				{
					if(lotSl==null || lotSl.trim().length() ==0 )
					{
						lotSl=" ";
					}
					mRate=distCommon.pickRate(priceList, tranDate, itemCodeDom, siteCodeDom + "~t" + locCode + "~t" + lotNo + "~t" + lotSl, " ", mQty, conn);//done
					valueXmlString.append("<rate protect=\"1\"><![CDATA[").append(mRate).append("]]></rate>\r\n");
					valueXmlString.append("<gross_rate ><![CDATA[").append(mRate).append("]]></gross_rate>\r\n");
					if("B".equalsIgnoreCase(pType))
					{
						valueXmlString.append("<rate protect=\"1\"><![CDATA[").append(mRate).append("]]></rate>\r\n");
					}
					else
					{
						valueXmlString.append("<rate protect=\"0\"><![CDATA[").append(mRate).append("]]></rate>\r\n");
					}
				}
				else
				{
					valueXmlString.append("<rate protect=\"0\"><![CDATA[").append(mRate).append("]]></rate>\r\n");
				}
			}
			
			//Modified by Rupesh on [24/10/2017][instructed by Piyush Sir]Start.
			//return rate;
			//retList.add(rate);// commented and added by nandkumar gadkari on 15/05/20
			setNodeValue( dom, "rate", String.valueOf(mRate) );
			retList.add(String.valueOf(mRate));
			retList.add(valueXmlString.toString());
			return retList;
			//Modified by Rupesh on [24/10/2017][instructed by Piyush Sir]End.
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
					if(pstmt1 != null)
						pstmt1.close();
					if(pstmt2 != null)
						pstmt2.close();
					if(rs1 != null)
						rs1.close();
					rs1 = null;
					if(rs2 != null)
						rs2.close();
					rs2 = null;
					pstmt1 = null;
					pstmt2 = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
	}
	
	//Modified by Rupesh on [24/10/2017][As Instructed by Piyush Sir].
	//private Void valAcct(String itemCode, String siteCode, String locCode, String lotNo, String lotSl,StringBuffer valueXmlString ,Document dom,Connection conn) throws ITMException
	//private String valAcct(String itemCode, String siteCode, String locCode, String lotNo, String lotSl,StringBuffer valueXmlString ,Document dom,Connection conn) throws ITMException
	//Modified by Rupesh on [02/11/2017][As Instructed by Piyush Sir].
	//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [START]
	//private String valAcct(String itemCode, String siteCode, String locCode, String lotNo, String lotSl,StringBuffer sb ,Document dom,Connection conn) throws ITMException
	private String valAcct(String itemCode, String siteCode, String locCode, String lotNo, String lotSl,StringBuffer sb ,Document dom,Document header,Document detail,Connection conn)throws ITMException
	//Commented by sarita to added dom1[to get pricelist value from header] & dom [to get detail value of rate] on 10 SEP 2018 [END]
	{
		//Connection conn = null;
		PreparedStatement pstmt1 = null,pstmt2 = null;
		ResultSet rs1 = null,rs2 = null;
		//ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat simpleDateFormat1 = null;
		java.sql.Timestamp currDate = null;
		
		String errString = "";
		String sql = "",sql1="";
		String cctrCodeCr="";
		String newRate="";
		String tranType="";
		String rateStr="",siteCodeMfg="",acctCodeArCr="",cctrCodeArCr="",packCode="";
		String convQtyStdm="",cctr="",acct="";
		String accCodeInv="",cctrCodeInv="",grossRate="",grade="",dimension="",noArt="",unitAlt="",potencyPerc="";
		String tranDate="",unit="",itemSer="",acctCodeCr="",acctCodeAr="",cctrCodeAr="";
		String ldtmfgdate1="",ldtexpirydate1="",ldtretestdate1="";   //added by manish on 22-aug-2019
		int cnt = 0,stock=0;
		double rate=0.0;
		Timestamp mfgDate=null,expDate=null,retestDate=null;   //added by manish on 22-aug-2019
		
		FinCommon finCommon=new FinCommon();
		DistCommon distCommon=new DistCommon();
		SimpleDateFormat sdf;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		StringBuffer valueXmlString = new StringBuffer();//added by Rupesh on[02/11/2017]
		//Added by sarita on 11 SEP 18 [START]
		String priceList = "", rateVal = "";
		double currentRate = 0.0;
		//Added by sarita on 11 SEP 18 [END]
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			tranType=checkNull(genericUtility.getColumnValue("tran_type",dom));
			System.out.println("tranType>>>>"+tranType);
			if(lotSl==null || lotSl.trim().length()==0)
			{
				//added by manish mhatre on 22-aug-2019 [For Expiry and mfg date  pick up in item_lot_info table]
				//start manish
				sql="select mfg_date, exp_date, retest_date from item_lot_info where item_code = ? and lot_no = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, lotNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					mfgDate = rs1.getTimestamp(1);
					expDate =rs1.getTimestamp(2);
					retestDate = rs1.getTimestamp(3);
					
					if( mfgDate != null ){
						ldtmfgdate1 = sdf.format(mfgDate);
						System.out.println("ldtmfgdate1"+ldtmfgdate1);
						valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
					}
					if(expDate != null)
					{
						ldtexpirydate1=sdf.format(expDate);
						System.out.println("ldtexpirydate1"+ldtexpirydate1);
						valueXmlString.append("<exp_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</exp_date>");
					}
					if(retestDate != null)
					{
						ldtretestdate1=sdf.format(retestDate);
						System.out.println("ldtretestdate1"+ldtretestdate1);
						valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
					}

				} //end manish
			
				else 
				{
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				sql="SELECT	ACCT_CODE__INV,	CCTR_CODE__INV,	RATE,GROSS_RATE	,GRADE,DIMENSION,NO_ART," +
						"UNIT__ALT,CONV__QTY_STDUOM,POTENCY_PERC,MFG_DATE,EXP_DATE,SITE_CODE__MFG,PACK_CODE " +
						"FROM STOCK where item_code = ? and site_code = ? AND LOC_CODE  = ? and lot_no	 = ? ";
				
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,siteCode);
				pstmt1.setString(3,locCode);
				pstmt1.setString(4,lotNo);
				rs1 = pstmt1.executeQuery();
				//commnted and changed by sarita to use if block instead while loop on 03 SEPT 2018 [START]
				//while( rs1.next())
				if(rs1.next())
				{
				//commnted and changed by sarita to use if block instead while loop on 03 SEPT 2018 [END]	
					cnt++;
					System.out.println("intered in result set hhho..");
					 
					accCodeInv= checkNull(rs1.getString("ACCT_CODE__INV"));
					cctrCodeInv= checkNull(rs1.getString("CCTR_CODE__INV"));
					rate= rs1.getDouble("RATE");
					grossRate= checkNull(rs1.getString("GROSS_RATE"));
					grade= checkNull(rs1.getString("GRADE"));
					dimension= checkNull(rs1.getString("DIMENSION"));
					noArt= checkNull(rs1.getString("NO_ART"));
					unitAlt= checkNull(rs1.getString("UNIT__ALT"));
					convQtyStdm= checkNull(rs1.getString("CONV__QTY_STDUOM"));
					potencyPerc= checkNull(rs1.getString("POTENCY_PERC"));
					mfgDate= rs1.getTimestamp("MFG_DATE");
					expDate= rs1.getTimestamp("EXP_DATE");
					siteCodeMfg= checkNull(rs1.getString("SITE_CODE__MFG"));
					packCode= checkNull(rs1.getString("PACK_CODE"));
					//mfgDate= rs1.getTimestamp("MFG_DATE");
		
					
				//Commented part by sarita as the code will execute if(cnt ==0) on 03 SEPT 2018	[START]
				/*	sql1="select item_ser ,unit FROM ITEM where item_code = ? ";
					pstmt2=conn.prepareStatement(sql1);
					pstmt2.setString(1,itemCode);
					rs2 = pstmt2.executeQuery();
					
					if( rs2.next())
					{	
						 System.out.println("intered in result set djfkvfklj hhho..");
						itemSer= checkNull(rs2.getString("item_ser"));
						unit= checkNull(rs2.getString("unit"));
						 
					}
					rs2.close();
					rs2 = null;
					pstmt2.close(); 
					pstmt2=null;
					
					cctrCodeCr=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINVRCP", tranType, conn);
					System.out.println("cctrCodeCr>>>>"+cctrCodeCr);
					if(cctrCodeCr.trim().length() >0)
					{
						String[] arrStr =cctrCodeCr.split(",");
						if(arrStr.length>0)
						{
							acctCodeArCr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeArCr =arrStr[1];
						}
						else
						{
							cctrCodeArCr=" ";
						}
					}
					accCodeInv=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINV", tranType, conn);
					System.out.println("accCodeInv>>>>"+accCodeInv);
					if(accCodeInv.trim().length() >0)
					{
						String[] arrStr =accCodeInv.split(",");
						if(arrStr.length>0)
						{
							acctCodeAr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeAr =arrStr[1];
						}
						else
						{
							cctrCodeAr=" ";
						}
					}
					
					
					if(cctrCodeCr==null || cctrCodeCr.trim().length()==0)
					{
						cctrCodeCr="";
					}
					if(acctCodeCr==null || acctCodeCr.trim().length()==0)
					{
						acctCodeCr="";
					}
					
					valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
					valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeArCr).append("]]></acct_code__cr>\r\n");
					valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeArCr).append("]]></cctr_code__cr>\r\n");
					
					
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append("1").append("]]></conv__qty_stduom>\r\n");
					valueXmlString.append("<unit__alt ><![CDATA[").append(unit).append("]]></unit__alt>\r\n");
					valueXmlString.append("<potency_perc ><![CDATA[").append("0").append("]]></potency_perc>\r\n");
					stock=0;*/
					//Commented part by sarita as the code will execute if(cnt ==0) on 03 SEPT 2018 [END]
				}
				if(rs1 !=  null )
				{
					rs1.close();					
					rs1 = null;
				}
				if(pstmt1!=null)
				{
					pstmt1.close(); 
					pstmt1=null;
				}
				
				System.out.println("cnt@@@@@@@@@1"+cnt);
				
				//Added above commented code for cnt is 0 on 03 SEP 2018 [START]
				if(cnt == 0)
				{
					sql1="select item_ser ,unit FROM ITEM where item_code = ? ";
					pstmt2=conn.prepareStatement(sql1);
					pstmt2.setString(1,itemCode);
					rs2 = pstmt2.executeQuery();
					
					if( rs2.next())
					{	
						 System.out.println("intered in result set djfkvfklj hhho..");
						itemSer= checkNull(rs2.getString("item_ser"));
						unit= checkNull(rs2.getString("unit"));
						 
					}
					rs2.close();
					rs2 = null;
					pstmt2.close(); 
					pstmt2=null;
					
					cctrCodeCr=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINVRCP", tranType, conn);
					System.out.println("cctrCodeCr>>>>"+cctrCodeCr);
					if(cctrCodeCr.trim().length() >0)
					{
						String[] arrStr =cctrCodeCr.split(",");
						if(arrStr.length>0)
						{
							acctCodeArCr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeArCr =arrStr[1];
						}
						else
						{
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [START]
							//cctrCodeArCr=" ";
							cctrCodeArCr="";
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [END]
						}
					}
					accCodeInv=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINV", tranType, conn);
					System.out.println("accCodeInv>>>>"+accCodeInv);
					if(accCodeInv.trim().length() >0)
					{
						String[] arrStr =accCodeInv.split(",");
						if(arrStr.length>0)
						{
							acctCodeAr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeAr =arrStr[1];
						}
						else
						{
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [START]
							//cctrCodeAr=" ";
							cctrCodeAr="";
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [END]
						}
					}
					
					
					if(cctrCodeCr==null || cctrCodeCr.trim().length()==0)
					{
						cctrCodeCr="";
					}
					if(acctCodeCr==null || acctCodeCr.trim().length()==0)
					{
						acctCodeCr="";
					}
					
					valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
					valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeArCr).append("]]></acct_code__cr>\r\n");
					valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeArCr).append("]]></cctr_code__cr>\r\n");
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [START]
					setNodeValue(detail, "acct_code__dr", acctCodeAr);
					setNodeValue(detail, "cctr_code__dr", cctrCodeAr);
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
					setNodeValue(detail, "acct_code__cr", acctCodeArCr);
					setNodeValue(detail, "cctr_code__cr", cctrCodeArCr);
					//Added by sarita to store acct_code__cr and cctr_code__cr values in dom on 03 SEP 2018 [START]
					
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append("1").append("]]></conv__qty_stduom>\r\n");
					valueXmlString.append("<unit__alt ><![CDATA[").append(unit).append("]]></unit__alt>\r\n");
					valueXmlString.append("<potency_perc ><![CDATA[").append("0").append("]]></potency_perc>\r\n");
					stock=0;
					//Added by sarita on 11 SEP 18 [START]
					rate = 0;
					valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
					setNodeValue( detail, "rate", String.valueOf(rate));
					//Added by sarita on 11 SEP 18 [END]
				}
				else
				//Added above commented code for cnt is 0 on 03 SEP 2018 [END]	
				{
					stock = 1;
					//Added by sarita to set value of rate on 10 SEP 2018 [START]
					priceList=checkNull(genericUtility.getColumnValue("price_list",header));
					rateVal = checkNull(genericUtility.getColumnValue("rate",detail));
					System.out.println("Header PriceList is ["+priceList+"] && Detail Rate Value is ["+rateVal+"]");
					if(priceList != null && priceList.trim().length() > 0)
					{				
						try
						{
							currentRate = Double.valueOf(rateVal); 
							System.out.println("currentRate" +currentRate);
						}
						catch(Exception e)
						{
							currentRate = 0;
						}																    
						if(currentRate <=0)
						{
							System.out.println("cnt000 is == ["+cnt+"] \t rate is ["+rate+"]]");
							
							valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
							setNodeValue( detail, "rate", String.valueOf(rate));
							valueXmlString.append("<gross_rate ><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
						}
					}
					else
					{
						System.out.println("cnt is == ["+cnt+"] \t rate is ["+rate+"]]");
						
						valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
						setNodeValue( detail, "rate", String.valueOf(rate));
						valueXmlString.append("<gross_rate ><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
					}
					
					/*if(rate<=0)
					{
						valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
						//Added by sarita to set rate in dom [START]
						setNodeValue( dom, "rate", String.valueOf(rate));
						//Added by sarita to set rate in dom [END]
						valueXmlString.append("<gross_rate ><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
					}*/
					//Added by sarita to set value of rate on 10 SEP 2018 [END]
					
					
					valueXmlString.append("<grade ><![CDATA[").append(grade).append("]]></grade>\r\n");
					valueXmlString.append("<dimension ><![CDATA[").append(dimension).append("]]></dimension>\r\n");
					
					valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
					valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeCr).append("]]></acct_code__cr>\r\n");
					valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeCr).append("]]></cctr_code__cr>\r\n");
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append("1").append("]]></conv__qty_stduom>\r\n");
					valueXmlString.append("<unit__alt ><![CDATA[").append(unit).append("]]></unit__alt>\r\n");
					valueXmlString.append("<potency_perc ><![CDATA[").append("0").append("]]></potency_perc>\r\n");
					if(mfgDate != null)
					{
						//valueXmlString.append("<mfg_date protect='0'>").append("<![CDATA["+sdf.format(mfgDate).toString()+"]]>").append("</mfg_date>");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<mfg_date protect='1'>").append("<![CDATA["+sdf.format(mfgDate).toString()+"]]>").append("</mfg_date>");
					}else
					{
						//valueXmlString.append("<mfg_date protect='0'><![CDATA[").append("").append("]]></mfg_date>\r\n");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<mfg_date><![CDATA[").append("").append("]]></mfg_date>\r\n");
					}
					if(mfgDate != null)
					{
						//valueXmlString.append("<exp_date protect='0'>").append("<![CDATA["+sdf.format(expDate).toString()+"]]>").append("</exp_date>");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<exp_date protect='1'>").append("<![CDATA["+sdf.format(expDate).toString()+"]]>").append("</exp_date>");
						
					}else
					{
						//valueXmlString.append("<exp_date protect='0'><![CDATA[").append("").append("]]></exp_date>\r\n");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<exp_date><![CDATA[").append("").append("]]></exp_date>\r\n");
					}
					valueXmlString.append("<site_code__mfg ><![CDATA[").append(siteCodeMfg).append("]]></site_code__mfg>\r\n");
					if(packCode == null || packCode.trim().length()==0)
					{
						valueXmlString.append("<pack_code ><![CDATA[").append(packCode).append("]]></pack_code>\r\n");
					}
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [START]
					setNodeValue(detail, "acct_code__dr", acctCodeAr);
					setNodeValue(detail, "cctr_code__dr", cctrCodeAr);
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
				
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
					setNodeValue(detail, "acct_code__cr", acctCodeCr);
					setNodeValue(detail, "cctr_code__cr", cctrCodeCr);
					//Added by sarita to store acct_code__cr and cctr_code__cr values in dom on 03 SEP 2018 [START]		
				}
			}	
			}
			else
			{
				//added by manish mhatre on 22-aug-2019 [For Expiry and mfg date  pick up in item_lot_info table]
				//start manish
				sql="select mfg_date, exp_date, retest_date from item_lot_info where item_code = ? and lot_no = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, lotNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					mfgDate = rs1.getTimestamp(1);
					expDate =rs1.getTimestamp(2);
					retestDate = rs1.getTimestamp(3);
					
					if( mfgDate != null ){
						ldtmfgdate1 = sdf.format(mfgDate);
						System.out.println("ldtmfgdate1"+ldtmfgdate1);
						valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
					}
					if(expDate != null)
					{
						ldtexpirydate1=sdf.format(expDate);
						System.out.println("ldtexpirydate1"+ldtexpirydate1);
						valueXmlString.append("<exp_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</exp_date>");
					}
					if(retestDate != null)
					{
						ldtretestdate1=sdf.format(retestDate);
						System.out.println("ldtretestdate1"+ldtretestdate1);
						valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
					}

				} //end manish
			
				else 
				{
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				sql="SELECT	ACCT_CODE__INV,CCTR_CODE__INV,RATE,GROSS_RATE,GRADE,DIMENSION,NO_ART,UNIT__ALT,CONV__QTY_STDUOM," +
						"POTENCY_PERC,MFG_DATE,EXP_DATE,SITE_CODE__MFG,PACK_CODE FROM STOCK " +
						"WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO	= ? AND LOT_SL = ? ";
				
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,siteCode);
				pstmt1.setString(3,locCode);
				pstmt1.setString(4,lotNo);
				pstmt1.setString(5,lotSl);
				rs1 = pstmt1.executeQuery();
				//commnted and changed by sarita to use if block instead while loop on 03 SEPT 2018 [START]
				//while( rs1.next())
				if(rs1.next())
				//commnted and changed by sarita to use if block instead while loop on 03 SEPT 2018 [END]
				{	
					cnt++;
					System.out.println("intered in result set hhho..");
					 
					accCodeInv= checkNull(rs1.getString("ACCT_CODE__INV"));
					cctrCodeInv= checkNull(rs1.getString("CCTR_CODE__INV"));
					rate= rs1.getDouble("RATE");
					grossRate= checkNull(rs1.getString("GROSS_RATE"));
					grade= checkNull(rs1.getString("GRADE"));
					dimension= checkNull(rs1.getString("DIMENSION"));
					noArt= checkNull(rs1.getString("NO_ART"));
					unitAlt= checkNull(rs1.getString("UNIT__ALT"));
					convQtyStdm= checkNull(rs1.getString("CONV__QTY_STDUOM"));
					potencyPerc= checkNull(rs1.getString("POTENCY_PERC"));
					mfgDate= rs1.getTimestamp("MFG_DATE");
					expDate= rs1.getTimestamp("EXP_DATE");
					siteCodeMfg= checkNull(rs1.getString("SITE_CODE__MFG"));
					packCode= checkNull(rs1.getString("PACK_CODE"));
					
				//Commented part by sarita as the code will execute if(cnt ==0) on 03 SEPT 2018	[START]
				/*	sql1="SELECT ITEM_SER,UNIT FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt2=conn.prepareStatement(sql1);
					pstmt2.setString(1,itemCode);
					rs2 = pstmt2.executeQuery();
					
					if( rs2.next())
					{	
						System.out.println("intered in result set djfkvfklj hhho..");
						itemSer= checkNull(rs2.getString("item_ser"));
						unit= checkNull(rs2.getString("unit"));
						 
					}
					rs2.close();
					rs2 = null;
					pstmt2.close(); 
					pstmt2=null;
					
					
					cctrCodeCr=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINVRCP", tranType, conn);
					System.out.println("cctrCodeCr>>>>"+cctrCodeCr);
					if(cctrCodeCr.trim().length() >0)
					{
						String[] arrStr =cctrCodeCr.split(",");
						if(arrStr.length>0)
						{
							acctCodeArCr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeArCr =arrStr[1];
						}
						else
						{
							cctrCodeArCr=" ";
						}
					}
					accCodeInv=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINV", tranType, conn);
					System.out.println("cctrCodeCr>>>>"+acct);
					if(accCodeInv.trim().length() >0)
					{
						String[] arrStr =accCodeInv.split(",");
						if(arrStr.length>0)
						{
							acctCodeAr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeAr =arrStr[1];
						}
						else
						{
							cctrCodeAr=" ";
						}
					}
					valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
					valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeArCr).append("]]></acct_code__cr>\r\n");
					valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeArCr).append("]]></cctr_code__cr>\r\n");
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append("0").append("]]></conv__qty_stduom>\r\n");
					valueXmlString.append("<unit__alt ><![CDATA[").append(unit).append("]]></unit__alt>\r\n");
					valueXmlString.append("<potency_perc ><![CDATA[").append("0").append("]]></potency_perc>\r\n");
					
					stock=0;*/
					//Commented part by sarita as the code will execute if(cnt ==0) on 03 SEPT 2018	[END]					
				}
				if(rs1 !=  null )
				{
					rs1.close();					
					rs1 = null;
				}
				if(pstmt1!=null)
				{
					pstmt1.close(); 
					pstmt1=null;
				}
				
				System.out.println("cnt@@@@@@@@@2"+cnt);
				//Added above commented code for cnt is 0 on 03 SEP 2018 [START]
				if( cnt == 0)
				{
					sql1="SELECT ITEM_SER,UNIT FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt2=conn.prepareStatement(sql1);
					pstmt2.setString(1,itemCode);
					rs2 = pstmt2.executeQuery();
					
					if( rs2.next())
					{	
						System.out.println("intered in result set djfkvfklj hhho..");
						itemSer= checkNull(rs2.getString("item_ser"));
						unit= checkNull(rs2.getString("unit"));
						 
					}
					rs2.close();
					rs2 = null;
					pstmt2.close(); 
					pstmt2=null;
					
					
					cctrCodeCr=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINVRCP", tranType, conn);
					System.out.println("cctrCodeCr>>>>"+cctrCodeCr);
					if(cctrCodeCr.trim().length() >0)
					{
						String[] arrStr =cctrCodeCr.split(",");
						if(arrStr.length>0)
						{
							acctCodeArCr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeArCr =arrStr[1];
						}
						else
						{
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [START]
							//cctrCodeArCr=" ";
							cctrCodeArCr="";
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [END]
						}
					}
					accCodeInv=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINV", tranType, conn);
					System.out.println("cctrCodeCr>>>>"+acct);
					if(accCodeInv.trim().length() >0)
					{
						String[] arrStr =accCodeInv.split(",");
						if(arrStr.length>0)
						{
							acctCodeAr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeAr =arrStr[1];
						}
						else
						{
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [START]
							//cctrCodeAr=" ";
							  cctrCodeAr="";
							//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [END]
						}
					}
					valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
					valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeArCr).append("]]></acct_code__cr>\r\n");
					valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeArCr).append("]]></cctr_code__cr>\r\n");
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append("0").append("]]></conv__qty_stduom>\r\n");
					valueXmlString.append("<unit__alt ><![CDATA[").append(unit).append("]]></unit__alt>\r\n");
					valueXmlString.append("<potency_perc ><![CDATA[").append("0").append("]]></potency_perc>\r\n");					
					stock=0;
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [START]
					setNodeValue(detail, "acct_code__dr", acctCodeAr);
					setNodeValue(detail, "cctr_code__dr", cctrCodeAr);
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
					//Added by sarita on 11 SEP 18 [START]
					rate = 0;
					valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
					setNodeValue( detail, "rate", String.valueOf(rate));
					//Added by sarita on 11 SEP 18 [END]
					
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
					setNodeValue(detail, "acct_code__cr", acctCodeArCr);
					setNodeValue(detail, "cctr_code__cr", cctrCodeArCr);
					//Added by sarita to store acct_code__cr and cctr_code__cr values in dom on 03 SEP 2018 [START]
				}
				else
				{	
				//Added above commented code for cnt is 0 on 03 SEP 2018 [END]	
					stock = 1;
					//Added by sarita to set value of rate on 10 SEP 2018 [START]
					priceList=checkNull(genericUtility.getColumnValue("price_list",header));
					rateVal = checkNull(genericUtility.getColumnValue("rate",detail));
					System.out.println("Header PriceList is ["+priceList+"] && Detail Rate Value is ["+rateVal+"]");
					if(priceList != null && priceList.trim().length() > 0)
					{				
						try
						{
							currentRate = Double.valueOf(rateVal); 
							System.out.println("currentRate" +currentRate);
						}
						catch(Exception e)
						{
							currentRate = 0;
						}																    
						if(currentRate <=0)
						{
							System.out.println("cnt000 is == ["+cnt+"] \t rate is ["+rate+"]]");
							
							valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
							setNodeValue( detail, "rate", String.valueOf(rate));
							valueXmlString.append("<gross_rate ><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
						}
					}
					else
					{
						System.out.println("cnt is == ["+cnt+"] \t rate is ["+rate+"]]");
						
						valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
						setNodeValue( detail, "rate", String.valueOf(rate));
						valueXmlString.append("<gross_rate ><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
					}
					
					/*if(rate<=0)
					{
						valueXmlString.append("<rate ><![CDATA[").append(rate).append("]]></rate>\r\n");
						//Added by sarita to set rate in dom [START]
						setNodeValue( dom, "rate", String.valueOf(rate));
						//Added by sarita to set rate in dom [END]
						valueXmlString.append("<gross_rate ><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
					}*/
					//Added by sarita to set value of rate on 10 SEP 2018 [END]
					
					
					valueXmlString.append("<grade ><![CDATA[").append(grade).append("]]></grade>\r\n");
					valueXmlString.append("<dimension ><![CDATA[").append(dimension).append("]]></dimension>\r\n");
					
					valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
					valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeCr).append("]]></acct_code__cr>\r\n");
					valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeCr).append("]]></cctr_code__cr>\r\n");
					valueXmlString.append("<conv__qty_stduom ><![CDATA[").append("1").append("]]></conv__qty_stduom>\r\n");
					valueXmlString.append("<unit__alt ><![CDATA[").append(unit).append("]]></unit__alt>\r\n");
					valueXmlString.append("<potency_perc ><![CDATA[").append("0").append("]]></potency_perc>\r\n");
					if(mfgDate != null)
					{					
						//valueXmlString.append("<mfg_date protect='0'>").append("<![CDATA["+sdf.format(mfgDate).toString()+"]]>").append("</mfg_date>"); Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<mfg_date protect='1'>").append("<![CDATA["+sdf.format(mfgDate).toString()+"]]>").append("</mfg_date>");
					}else
					{
						//valueXmlString.append("<mfg_date protect='0'><![CDATA[").append("").append("]]></mfg_date>\r\n");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<mfg_date><![CDATA[").append("").append("]]></mfg_date>\r\n");
					}
					if(mfgDate != null)
					{
						//valueXmlString.append("<exp_date protect='0'>").append("<![CDATA["+sdf.format(expDate).toString()+"]]>").append("</exp_date>");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<exp_date protect='1'>").append("<![CDATA["+sdf.format(expDate).toString()+"]]>").append("</exp_date>");
						
					}else
					{
						//valueXmlString.append("<exp_date protect='0'><![CDATA[").append("").append("]]></exp_date>\r\n");Conmmented and added by sarita on 03 SEP 18
						valueXmlString.append("<exp_date><![CDATA[").append("").append("]]></exp_date>\r\n");
					}
					valueXmlString.append("<site_code__mfg ><![CDATA[").append(siteCodeMfg).append("]]></site_code__mfg>\r\n");
					if(packCode == null || packCode.trim().length()==0)
					{
						valueXmlString.append("<pack_code ><![CDATA[").append(packCode).append("]]></pack_code>\r\n");
					}
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [START]
					setNodeValue(detail, "acct_code__dr", acctCodeAr);
					setNodeValue(detail, "cctr_code__dr", cctrCodeAr);
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
					
					//Added by sarita to store acct_code__dr and cctr_code__dr values in dom on 03 SEP 2018 [END]
					setNodeValue(detail, "acct_code__cr", acctCodeCr);
					setNodeValue(detail, "cctr_code__cr", cctrCodeCr);
					//Added by sarita to store acct_code__cr and cctr_code__cr values in dom on 03 SEP 2018 [START]
				}
			}
			rateStr=checkNull(genericUtility.getColumnValue("rate",detail));
			System.out.println("rateStr ["+rateStr+"]");
			if(rateStr != null && rateStr.trim().length() > 0)
			{
				try
				{
					rate = Double.parseDouble(rateStr);
				} catch (NumberFormatException n)
				{
					rate = 0;
				}
			}else
			{
				rate = 0;
			}
			if(rate<=0)
			{
				sql1="SELECT (CASE WHEN ITEM.COST_RATE IS NULL THEN 0 ELSE ITEM.COST_RATE END ) AS NEWRATE  FROM 	ITEM  WHERE  ITEM_CODE = ? ";
				pstmt2=conn.prepareStatement(sql1);
				pstmt2.setString(1,itemCode);
				rs2 = pstmt2.executeQuery();
				
				if( rs2.next())
				{	
					 System.out.println("intered in result set djfkvfklj hhho..");
					 newRate= checkNull(rs2.getString("NEWRATE"));
				}
				rs2.close();
				rs2 = null;
				pstmt2.close(); 
				pstmt2=null;
				valueXmlString.append("<rate ><![CDATA[").append(newRate).append("]]></rate>\r\n");
			}
			

			if(acctCodeAr==null || acctCodeAr.trim().length()==0)
			{
				acctCodeAr="";
			}
			if(cctrCodeAr==null || cctrCodeAr.trim().length()==0)
			{
				cctrCodeAr="";
			}
			if(cctrCodeAr.trim().length()> 0 && acctCodeAr.trim().length() > 0 && stock == 1)
			{
				//commented and added by sarita to set value of acct_code__dr & cctr_code__dr if value exist on 03 SEP 2018 [START]
				/*valueXmlString.append("<acct_code__dr protect=\"1\" ><![CDATA[").append("").append("]]></acct_code__dr>\r\n");
				valueXmlString.append("<cctr_code__dr protect=\"1\" ><![CDATA[").append("").append("]]></cctr_code__dr>\r\n");*/
				valueXmlString.append("<acct_code__dr protect=\"1\" ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
				valueXmlString.append("<cctr_code__dr protect=\"1\" ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
				//commented and added by sarita to set value of acct_code__dr & cctr_code__dr if value exist on 03 SEP 2018 [END]
				
			}
			else if(cctrCodeAr.trim().length() == 0 && acctCodeAr.trim().length() == 0 && stock == 1)
			{
				sql1="SELECT ITEM_SER FROM ITEM	WHERE ITEM_CODE = ? ";
				pstmt2=conn.prepareStatement(sql1);
				pstmt2.setString(1,itemCode);
				rs2 = pstmt2.executeQuery();
				
				if( rs2.next())
				{	
					 System.out.println("intered in result set djfkvfklj hhho..");
					itemSer= checkNull(rs2.getString("ITEM_SER"));
				}
				rs2.close();
				rs2 = null;
				pstmt2.close(); 
				pstmt2=null;
				
				cctrCodeCr=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINVRCP", tranType, conn);
				System.out.println("cctrCodeCr>>>>"+cctrCodeCr);
				if(cctrCodeCr.trim().length() >0)
				{
					String[] arrStr =cctrCodeCr.split(",");
					if(arrStr.length>0)
					{
						acctCodeArCr =arrStr[0];
					}
					if(arrStr.length>1)
					{
						cctrCodeArCr =arrStr[1];
					}
					else
					{
						//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [START]
						//cctrCodeArCr=" ";
						cctrCodeArCr="";
						//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [END]
					}
				}
				accCodeInv=	finCommon.getAcctDetrTtype(itemCode, itemSer, "STKINV", tranType, conn);
				System.out.println("cctrCodeCr>>>>"+acct);
				if(accCodeInv.trim().length() >0)
				{
					String[] arrStr =accCodeInv.split(",");
					if(arrStr.length>0)
					{
						acctCodeAr =arrStr[0];
					}
					if(arrStr.length>1)
					{
						cctrCodeAr =arrStr[1];
					}
					else
					{
						//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [START]
						//cctrCodeAr=" ";
						cctrCodeAr="";
						//Done changes by sarita as cctr_code__cr is setting with space and giving exception on save on 06 SEP 2018 [END]
					}
				}
				valueXmlString.append("<acct_code__dr ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
				valueXmlString.append("<cctr_code__dr ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
				valueXmlString.append("<acct_code__cr ><![CDATA[").append(acctCodeArCr).append("]]></acct_code__cr>\r\n");
				valueXmlString.append("<cctr_code__cr ><![CDATA[").append(cctrCodeArCr).append("]]></cctr_code__cr>\r\n");			
			}
			else
			{
				//Commented and added by sarita to set acct_code__dr & cctr_code__dr values using dom on 03 SEP 2018 [START]
				//Commented and added below two lines to get acct_code_dr from detail on 04-02-19
				//acctCodeAr=checkNull(genericUtility.getColumnValue("acct_code__dr",dom));
				//cctrCodeAr=checkNull(genericUtility.getColumnValue("cctr_code__dr",dom));
				acctCodeAr=checkNull(genericUtility.getColumnValue("acct_code__dr",detail));
				cctrCodeAr=checkNull(genericUtility.getColumnValue("cctr_code__dr",detail));
				//Ended below two lines to get acct_code_dr from detail on 04-02-19
				System.out.println("acctCodeAr ["+acctCodeAr+"] \t cctrCodeAr ["+cctrCodeAr+"]");
				if((acctCodeAr != null && acctCodeAr.trim().length() > 0) && (cctrCodeAr != null && cctrCodeAr.trim().length() > 0))
				{
					valueXmlString.append("<acct_code__dr protect=\"0\" ><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr protect=\"0\" ><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
				}
				else
				{
					valueXmlString.append("<acct_code__dr protect=\"0\" ><![CDATA[").append("").append("]]></acct_code__dr>\r\n");
					valueXmlString.append("<cctr_code__dr protect=\"0\" ><![CDATA[").append("").append("]]></cctr_code__dr>\r\n");
				}
				//valueXmlString.append("<acct_code__dr protect=\"0\" ><![CDATA[").append("").append("]]></acct_code__dr>\r\n");
				//valueXmlString.append("<cctr_code__dr protect=\"0\" ><![CDATA[").append("").append("]]></cctr_code__dr>\r\n");
				//Commented and added by sarita to set acct_code__dr & cctr_code__dr values using dom on 03 SEP 2018 [END]
			}			
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} 
		return valueXmlString.toString();//Added by Rupesh on [24/10/2017][As Instructed by Piyush Sir].
	}
	
	
	private String errorType(Connection conn, String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
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
			}
		}
		return msgType;
	}

	private String checkNull(String str)
	{
		if (str == null) 
		{
			return "";
		} 
		else 
		{
			return str;
		}

	}
	
	
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);
	
		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	
	}
	private String checkDouble(String input)	
	{
		if (input == null || input.trim().length() == 0)
		{
			input="0";
		}
		return input;
	}
	
	private String previousLot(Document dom,String  lineNo) throws ITMException
    {
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;

		String porderNoDom = "",previousLot="",lotSlDom="",lineNoDom="";
		long lineNo1=0;
		boolean ispreviousLot = false;
		String refNoDom= "",refSerDom="";
		System.out.println("---inside previousLot--");
		try
		{
			parentList = dom.getElementsByTagName("Detail2");
			int parentNodeListLength = parentList.getLength();
			for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr-- )
			{	
				parentNode = parentList.item(prntCtr-1);
				childList = parentNode.getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);
					if(childNode != null &&  childNode.getNodeName().equalsIgnoreCase("attribute"))
					{
						String updateFlag = "";
						updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
						System.out.println("updateFlag>>>>"+updateFlag);
						if (updateFlag.equalsIgnoreCase("D"))
						{
							System.out.println("Break from here as the record is deleted");
						}
					}	
					
					if ( childNode != null && childNode.getFirstChild() != null &&  
					childNode.getNodeName().equalsIgnoreCase("lot_sl") )
					{
						lotSlDom = childNode.getFirstChild().getNodeValue().trim();
					}
					if ( childNode != null && childNode.getFirstChild() != null &&  
							childNode.getNodeName().equalsIgnoreCase("line_no") )
							{
								lineNoDom = childNode.getFirstChild().getNodeValue().trim();
							}
					
				}
				if (Integer.parseInt(lineNoDom) == Integer.parseInt(lineNo)-1 )
				{
					previousLot = lotSlDom;
					break;
				}
				
			}//for loop
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		System.out.println("previousLot>>>>>> ["+previousLot+"]");
		return previousLot;
    }
	private String gbfItem(String siteCode, String itemCode, String transfer, Connection conn) throws ITMException
    {
		String errorCode="",sql="";
		String active="",siteSepecificItem="",siteActive="";
		int cnt=0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ibase.webitm.ejb.dis.DistCommon disCommon = new ibase.webitm.ejb.dis.DistCommon();
	   try
	   {
		   	sql = "	select (case when active is null then 'Y' else active end) as active   from item where item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				active = rs.getString("active");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(!"Y".equalsIgnoreCase(active))
			{
				if("S-RET".equalsIgnoreCase(transfer) || "SRFRM".equalsIgnoreCase(transfer))
				{
					errorCode="VTITEM9";
				}else
				{
					errorCode="VTITEM4";
				}
				
			}
			
			siteSepecificItem = disCommon.getDisparams("999999", "SITE_SPECIFIC_ITEM", conn);
			if("Y".equalsIgnoreCase(siteSepecificItem))
			{
				sql = "	select (case when active is null then 'Y' else active end) as active   from siteitem where item_code = ?  and site_code=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setString(2, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					siteActive = rs.getString("active");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if("D-ORD".equalsIgnoreCase(transfer))
				{
					errorCode="VTITEM3A";
				}else
				{
					errorCode="VTITEM3";
				}
				if("N".equalsIgnoreCase(siteActive))
				{
					errorCode="VTITEM4";
				}
			}else
			{
				sql = "select count(*) from item where item_code= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if (cnt == 0)
				{
					errorCode = "VTITEM1";
				}
			}
	   }  
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			try
           {
				System.out.println("@@@@@@@connection roll back@@@@");
	            conn.rollback();
           } catch (SQLException e1)
           {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
           }
			throw new ITMException(e);
		}
	    return errorCode;
    }
	
	public double getRndamt(double newQty, String round, double roundTo) {
		System.out.println(newQty+"~~~"+round+"~~~"+roundTo);
		double lcMultiply = 1;
		try {
			round = round.toUpperCase();
			if (newQty < 0) {
				lcMultiply = -1;
				newQty = Math.abs(newQty);
			} else if (newQty == 0) {
				return newQty;
			} else if (round.trim().equals("N")) {
				return newQty;
			} else if (roundTo == 0) {
				return newQty;
			}
			if (round.trim().equals("X")) {
				
				if(newQty == (newQty - (newQty % roundTo)))
				{
					return newQty;
				}
				else 
				{
					newQty = ((newQty - (newQty % roundTo)) + roundTo);
				}				
			}
			if (round.trim().equals("P")) {
				newQty = (newQty - (newQty % roundTo));
			}
			if (round.trim().equals("R")) {
				if ((newQty % roundTo) < (roundTo / 2)) {
					newQty = (newQty - (newQty % roundTo));
				} else {
					newQty = (newQty - (newQty % roundTo) + roundTo);
				}
			}
			System.out.println("newQty[" + newQty + "]");
			System.out.println("lcMultiply[" + lcMultiply + "]");
			newQty = newQty * lcMultiply;
			System.out.println("newQty * lcMultiply[" + newQty + "]");
			return newQty;
		} catch (Exception e) {
			System.out.println("Exception :Conversion Qty ::" + e.getMessage()
					+ ":");

		}
		if (roundTo == 1) {
			newQty = getRequiredDecimal(newQty, 0);
		} else if (roundTo == .1) {
			newQty = getRequiredDecimal(newQty, 1);
		} else if (roundTo == .01) {
			newQty = getRequiredDecimal(newQty, 2);
		} else if (roundTo == .001) {
			newQty = getRequiredDecimal(newQty, 3);
		} else if (roundTo == .0001) {
			newQty = getRequiredDecimal(newQty, 4);
		}
		return newQty;
	}
	
	public double getRequiredDecimal(double actVal, int prec) {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		Double DoubleValue = new Double(actVal);
		numberFormat.setMaximumFractionDigits(3);
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",", "");
		double reqVal = Double.parseDouble(strValue);
		return reqVal;
	}
	
//	Modified By Aniket C. on[20th/APR/2021] Calculate Quantity On Basis Dimension and No_Art[Start]

		private static String getAbsString(String str) {
			return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
		}
//	Modified By Aniket C. on[20th/APR/2021] Calculate Quantity On Basis Dimension and No_Art[End]


}
