 /*
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 03/11/2005
*/

package ibase.webitm.ejb.dis.adv;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

@Stateless // added for ejb3
public class SalesReturnAct extends ActionHandlerEJB implements SalesReturnActLocal, SalesReturnActRemote
{
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

    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String xmlString1, String xmlString2, String objContext, String xtraParams) throws RemoteException,ITMException
	{
	    System.out.println("SalesReturn called");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString2);
				dom2 = genericUtility.parseString(xmlString2);
			}

			System.out.println("actionType:"+actionType+":");
			if (actionType.equalsIgnoreCase("Allocate"))
			{
			  resString = actionAllocate(dom,dom1,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("FullSR"))
			{
			  resString = actionFullSr(dom,dom1,dom2,objContext,xtraParams);
			}
			//condition added by nandkumar gadkari on 25/06/19
			if (actionType.equalsIgnoreCase("Split"))
			{
				resString = splitMinRateHistBalAct(dom,dom1,objContext,xtraParams);
			}
			
		}catch(Exception e)
		{
			System.out.println("Exception :SalesReturn :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionMETHOD :actionHandler"+resString);
	    return resString;
	}

	private String actionAllocate(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String errCode = "";
		String errString = "";
		String flag = "";
		String  invoiceId = "";
		String	siteCode = "";
		String	itemCode = "";
		String	locCode = "";
		String	lotNoInv = "";
		String	lotSlInv = "";
		String	taxClass = "";
		String	taxChap = "";
		String	taxEnv = "";
		String	reason = "";
		String	itemSer = "";
		String	siteCodeMfgFn = "";
		String	packCodeFn = "";
		String	chkDate = "";
		String	quantity = "";
		String stkMfgDate = "";
		String stkExpDate = "";
		String lotNo ="               ";
		String lotSl ="               ";
		String siteCodeMfg ="";
		String packCode ="";
		String	unit = "";
		java.sql.Date expDate = null;
		java.sql.Date mfgDate = null;
		int	minShelfLife = 0;
	  	int detCnt =0;
		double totStk =0.0;
		double inputQty = 0.0;
		double qty =0;
		double qtyStk = 0;

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		/*ADDED BY HATIM ON 16/01/2006*/
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		/*END*/
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			//Getting values from dom (current form), dom1(form no 1)
			if(detCnt == 0)
			{
				flag = genericUtility.getColumnValue("ret_rep_flag",dom);
				System.out.println("\n******************** S T A R T  ALLOCATE ****************************");
				System.out.println("SalesReturn:actionAllocate:flag:"+flag+":");
			   	if (flag.equalsIgnoreCase("P"))
				{
					siteCode = genericUtility.getColumnValue("site_code",dom1);
					invoiceId = genericUtility.getColumnValue("invoice_id",dom);
					itemCode = genericUtility.getColumnValue("item_code",dom);
					quantity = genericUtility.getColumnValue("quantity",dom);
					locCode	= genericUtility.getColumnValue("loc_code",dom);
					lotNoInv = genericUtility.getColumnValue("lot_no",dom);
					lotSlInv = genericUtility.getColumnValue("lot_sl",dom);
					taxClass = genericUtility.getColumnValue("tax_class",dom);
					taxChap = genericUtility.getColumnValue("tax_chap",dom);
					taxEnv = genericUtility.getColumnValue("tax_env",dom);
					reason = genericUtility.getColumnValue("reas_code",dom);
					lotNoInv = lotNoInv == null ? "              " : lotNoInv;
					lotSlInv = lotSlInv == null ? "              " : lotSlInv;
					System.out.println("Values From DOM :siteCode:"+siteCode+":invoiceId:"+invoiceId+":quantity:"+quantity+":");
					System.out.println("Values From DOM :lotNoInv:"+lotNoInv+":lotSlInv:"+lotSlInv+":itemCode:"+itemCode+":");
					System.out.println("Values From DOM :taxClass:"+taxClass+":taxChap:"+taxChap+":taxEnv:"+taxEnv+":reason:"+reason+":");

					//Added By Gulzar 06/01/07
					System.out.println("Checking For NULL values of taxClass, taxChap, taxEnv");
					if (taxClass == null)
					{
						taxClass = "";
					}
					if (taxChap == null)
					{
						taxChap = "";
					}
					if (taxEnv == null)
					{
						taxEnv = "";
					}
					System.out.println("Values Of :taxClass:"+taxClass+":taxChap:"+taxChap+":taxEnv:"+taxEnv+":");
					//End Add Gulzar 06/01/07
					if(quantity != null && quantity.trim().length() > 0)
					{
						qty = Double.parseDouble(quantity);
					}
				   	sql="SELECT MIN_SHELF_LIFE FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
					System.out.println("ITEM:sql:"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						minShelfLife = rs.getInt(1);
					}
					System.out.println("\n minShelfLife:"+minShelfLife+":");

					chkDate = getRelativeDate(minShelfLife);
					System.out.println("SalesReturn:actionAllocate:chkDate:"+chkDate+":");
					System.out.println("SalesReturn:actionAllocate:lotNoInv:"+lotNoInv+":");
					if(lotNoInv == null || lotNoInv.trim().length() == 0)
					{
						sql="SELECT SUM(A.QUANTITY - A.ALLOC_QTY) "+
						  "	FROM STOCK A, INVSTAT B, LOCATION C	 "+
						  "	WHERE A.LOC_CODE = C.LOC_CODE "+
						  "	AND B.INV_STAT  = C.INV_STAT  "+
						  "	AND A.ITEM_CODE ='"+itemCode+"'" +
						  "	AND A.SITE_CODE = '"+siteCode+"'" +
						  "	AND A.LOC_CODE  ='"+locCode+"'"+
						  "	AND A.QUANTITY  > 0	"+
						  "	AND B.AVAILABLE = 'Y' "+
						  "	AND A.EXP_DATE >= ? ";
						pstmt = conn.prepareStatement(sql);
						System.out.println("IF::STOCK a, INVSTAT b, LOCATION c:sql:"+sql);
						pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(chkDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							 totStk = rs.getDouble(1);
						}
					}
					else
					{
						sql="SELECT SUM(A.QUANTITY - A.ALLOC_QTY) "+
							" FROM STOCK A, INVSTAT B, LOCATION C "+
							" WHERE A.LOC_CODE = C.LOC_CODE	"+
							" AND B.INV_STAT  = C.INV_STAT "+
							" AND A.ITEM_CODE ='"+itemCode+"'"+
							" AND A.SITE_CODE ='"+siteCode+"'"+
							" AND A.LOC_CODE  ='"+locCode+"'"+
							" AND A.LOT_NO ='"+lotNoInv+"'"+
							" AND A.LOT_SL ='"+lotSlInv+"'" +
							" AND (A.QUANTITY - A.ALLOC_QTY) >= '"+qty+"'"+
							" AND B.AVAILABLE = 'Y'	"+
							" AND A.EXP_DATE >= ? ";
						System.out.println("ELSE ::STOCK a, INVSTAT b, LOCATION c:sql:"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(chkDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							totStk = rs.getDouble(1);
						}
					}//End else
					System.out.println("SalesReturn:totStk:"+totStk+":quantity:"+qty+":");

					if(totStk < qty )
					{
						errCode = "VTDIST16";
					}
					System.out.println("SalesReturn:ERRCODE :"+errCode+":");
					if(errCode == null || errCode.trim().length() == 0)
					{
						System.out.println("I N S I D E ERR = N U L  L ");
						sql="SELECT A.LOC_CODE, A.LOT_NO, A.LOT_SL, A.QUANTITY - A.ALLOC_QTY, A.EXP_DATE,"+
							" A.UNIT, A.ITEM_SER,A.SITE_CODE__MFG, A.MFG_DATE, A.PACK_CODE " +
							" FROM STOCK A, INVSTAT B, LOCATION C " +
							" WHERE A.LOC_CODE = C.LOC_CODE " +
							" AND B.INV_STAT  = C.INV_STAT " +
							" AND A.ITEM_CODE = '" + itemCode + "'" +
							" AND A.SITE_CODE = '" + siteCode +"'" +
							" AND A.LOC_CODE  = '" + locCode + "' " +
							" AND (A.QUANTITY - A.ALLOC_QTY)  > 0 " +
							" AND B.AVAILABLE = 'Y' " +
							" AND A.EXP_DATE >= ? ";
						System.out.println("CURSOR SQL:"+sql);
						if(lotNoInv != null && lotNoInv.trim().length() > 0)
						{
							System.out.println("I N S I D lotNoInv.trim().length() > 0 ");
							sql =  sql + "AND A.LOT_NO ='"+lotNoInv+ "' " +
									" AND A.LOT_SL='" +lotSlInv+"' " +
									" AND (A.QUANTITY - A.ALLOC_QTY) >= "+qty;//string(lc_quantity)
						}
						sql =  sql + " ORDER BY A.EXP_DATE, A.LOT_NO, A.LOT_SL ";
						//chkDate ="26/02/03";
						System.out.println("LAST CURSOR SQL:"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(chkDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						System.out.println("chkDate ::"+chkDate);
						rs = pstmt.executeQuery();
						SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						while(rs.next())
						{
							locCode =  rs.getString(1);
							lotNo =	 rs.getString(2);
							lotSl = rs.getString(3);
							qtyStk = rs.getDouble(4);
							expDate = rs.getDate(5);
							unit = rs.getString(6);
							itemSer = rs.getString(7);
							siteCodeMfg = rs.getString(8);
							mfgDate = rs.getDate(9);
							packCode =rs.getString(10);

							inputQty = 0;
							System.out.println(" **** qtyStk:"+qtyStk+":quantity:"+qty+":");
							if(qtyStk <= qty)
							{
								inputQty = qtyStk;
								qty = qty - inputQty;
								System.out.println(" *** qty:"+qty+":");
							}
							else
							{
								inputQty = qty;
								qty = 0;
							}
							System.out.println(" *** mfgDate:"+mfgDate+":");
							if(mfgDate != null)
							{
								stkMfgDate = sdf.format(mfgDate);
							}
							System.out.println("stkMfgDate:"+stkMfgDate+":");
							System.out.println(" *** expDate:"+expDate+":");
							if(expDate != null)
							{
								stkExpDate = sdf.format(expDate);
							}
							System.out.println("SalesReturn:inputQty:"+inputQty+":");
							//inputQty = 1.0;
							if(inputQty > 0)
							{
								valueXmlString.append("<Detail>\r\n");
									valueXmlString.append("<ret_rep_flag>").append("<![CDATA[").append(flag).append("]]>").append("</ret_rep_flag>\r\n");
									valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
									valueXmlString.append("<quantity>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
									valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
									valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
									valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
									valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
									valueXmlString.append("<mfg_date>").append("<![CDATA[").append(stkMfgDate).append("]]>").append("</mfg_date>\r\n");
									valueXmlString.append("<exp_date>").append("<![CDATA[").append(stkExpDate).append("]]>").append("</exp_date>\r\n");
									valueXmlString.append("<item_ser>").append("<![CDATA[").append(itemSer).append("]]>").append("</item_ser>\r\n");
							//calling function(method) gf_get_mfg_site()
									siteCodeMfgFn = itmDBAccess.getMfgSiteCode(itemCode,siteCode,locCode,lotNo,lotSl,"M",conn);
									if(siteCodeMfgFn.indexOf("DS000") != -1)
									{
										valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append(siteCodeMfg.trim()).append("]]>").append("</site_code__mfg>\r\n");
									}
									else
									{
										valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append(siteCodeMfgFn).append("]]>").append("</site_code__mfg>\r\n");
									}
									packCodeFn = itmDBAccess.getMfgSiteCode(itemCode,siteCode,locCode,lotNo,lotSl,"P",conn);
									if(packCodeFn.indexOf("DS000") != -1)
									{
										valueXmlString.append("<pack_code>").append("<![CDATA[").append(packCode.trim()).append("]]>").append("</pack_code>\r\n");
									}
									else
									{
										valueXmlString.append("<pack_code>").append("<![CDATA[").append(packCodeFn).append("]]>").append("</pack_code>\r\n");
									}
									valueXmlString.append("<tax_amt>").append("<![CDATA[").append(0).append("]]>").append("</tax_amt>\r\n");
									valueXmlString.append("<tax_class>").append("<![CDATA[").append(taxClass).append("]]>").append("</tax_class>\r\n");
									valueXmlString.append("<tax_chap>").append("<![CDATA[").append(taxChap).append("]]>").append("</tax_chap>\r\n");
									valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv).append("]]>").append("</tax_env>\r\n");
									valueXmlString.append("<reas_code>").append("<![CDATA[").append(reason).append("]]>").append("</reas_code>\r\n");
								valueXmlString.append("</Detail>\r\n");
							}
							if(qty <= 0)
							{
								break;
							}
						}//loop end
					}//errCode null end if
				}//flag P end if
			}
			valueXmlString.append("</Root>\r\n");
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("SalesReturn:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				conn.close();
				conn = null;
				return errString;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SalesReturn :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("SalesReturn :actionAllocate:Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionFullSr(Document dom,Document dom1,Document dom2, String objContext, String xtraParams) throws RemoteException , ITMException
	{

		System.out.println("\n***********************");
		Connection conn = null;
		Statement stmt = null , stmtMain = null;
		ResultSet rs = null;
		ResultSet rsMain = null;
		String sql = "";
		String errCode = "";
		String errString = "";
		String tranId = "";
		String conf = "";
		String fullSr = "";
		String reasonCode = "";
		String invoiceId = "";
		String lotNo = "             ";
		String lotSl = "             ";
		String itemCode = "";
		String setFlag = "";
		//Added on 08/06/06 as code changes in PB
		String despID = "",despLineNo = "";
		int invTraceLineNo = 0;
		//
		int detCnt = 0;
		long invLine =0;
		double qty  = 0;
		double invQty = 0;
		double prevSrqty = 0;
		double sreturnQty = 0;
		double rate = 0;
		double rateStduom = 0;
		double discount = 0;

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		/*ADDED BY HATIM ON 16/01/2006*/
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		/*END*/
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			
			//Added by Shripad on 28/12/2009----START
			//System should retrive data only when there is no any detail present in the deatil window.
			NodeList parentNodeList = null;
			Node parentNode = null;
			NodeList childNodeList = null;
			int childNodeListLength = 0;
			int parentNodeListLength = 0;
			Node childNode = null;
			String childNodeName = null;
			boolean dtlPstFlag = false;
			
			parentNodeList = dom2.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			System.out.println("parentNodeListLength====>"+parentNodeListLength);
			for(int ptr = 0; ptr < parentNodeListLength; ptr++)
			{
				System.out.println("ptr ====>"+ptr);
				parentNode = parentNodeList.item(ptr);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for( int ctr = 0; ctr < childNodeListLength; ctr++ )
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					/* if ( childNodeName.equalsIgnoreCase( "lot_no" ) )
					{
						if(childNode.getFirstChild() != null)
						{
							dtlPstFlag = true;
							System.out.println("lot_no==>dtlPstFlag is ==>"+dtlPstFlag);
						}
					}
					if ( childNodeName.equalsIgnoreCase( "lot_sl" ) )
					{
						if(childNode.getFirstChild() != null)
						{
							dtlPstFlag = true;
							System.out.println("lot_sl==>dtlPstFlag is ==>"+dtlPstFlag);
						}
					} 
					 if ( childNodeName.equalsIgnoreCase( "rate" ) )
					{
						if(childNode.getFirstChild() != null)
						{
							dtlPstFlag = true;
							System.out.println("rate==>dtlPstFlag is ==>"+dtlPstFlag);
						}
					} 
					 if ( childNodeName.equalsIgnoreCase( "loc_code" ) )
					{
						if(childNode.getFirstChild() != null)
						{
							dtlPstFlag = true;
							System.out.println("loc_code==>dtlPstFlag is ==>"+dtlPstFlag);
						}
					} */
					if ( childNodeName.equalsIgnoreCase( "item_code" ) )
					{
						if(childNode.getFirstChild() != null )
						{
							dtlPstFlag = true;
						}
					}
					/* if ( childNodeName.equalsIgnoreCase( "quantity" ) )
					{
						if(childNode.getFirstChild() != null)
						{
							dtlPstFlag = true;
							System.out.println("quantity==>dtlPstFlag is ==>"+dtlPstFlag);
						}
					} */
				}
			}
			System.out.println("dtlPstFlag is ==>"+dtlPstFlag);
			if( dtlPstFlag )
			{
				String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				return itmDBAccess.getErrorString("","VTDATAPRST",userId,"",conn);
				//valueXmlString.append("</Root>\r\n");
				//return valueXmlString.toString();
			}
			
			//Added by Shripad on 28/12/2009----END

		  //IF SRETURN IS ALREADY CONFIRMED RETURN
			tranId= genericUtility.getColumnValue("tran_id",dom1);
			System.out.println("tranId:"+tranId+":");
			if (tranId == null)
			{
				tranId = "";
			}
			stmt = conn.createStatement();
			sql = "SELECT CONFIRMED FROM SRETURN WHERE TRAN_ID ='"+tranId+"'";
			System.out.println("SalesReturn :actionFullSr:SRETURN:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				conf =rs.getString(1);
			}
			rs.close();//Gulzar - 21/11/06
			stmt.close();
		    if(conf != null && !conf.equalsIgnoreCase("Y"))
			{
				fullSr = genericUtility.getColumnValue("full_ret",dom1);
				System.out.println("fullSr:"+fullSr+":");
				if(fullSr != null &&(!fullSr.equalsIgnoreCase("N")))
				{
					//detCnt = Integer.parseInt(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt")==null?"0":genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"));
					if(detCnt == 0)
					{
						reasonCode = genericUtility.getColumnValue("reas_code",dom);
						System.out.println("reasonCode:"+reasonCode+":");
						if (reasonCode == null || reasonCode.trim().length() == 0)
						{
							sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'REAS_CODE_SRET'";
							System.out.println("SQL :: "+sql);
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								reasonCode = rs.getString(1);
							}
							rs.close();//Gulzar - 21/11/06
							stmt.close();
						}
						if(reasonCode != null && !reasonCode.equalsIgnoreCase("N"))
						{
							invoiceId = genericUtility.getColumnValue("invoice_id",dom1);
							if (invoiceId == null)
							{
								invoiceId = "";
							}
							System.out.println("invoiceId:"+invoiceId+":");
							sql ="SELECT INV_LINE_NO, LOT_NO, LOT_SL, QUANTITY, ITEM_CODE, RATE, "+
							  " RATE__STDUOM, DISCOUNT, "+
							  " DESP_ID, DESP_LINE_NO, LINE_NO "+ //Added on 08/06/06 as code changes in PB
							  " FROM INVOICE_TRACE  WHERE INVOICE_ID='" +invoiceId+"'";
							//invoice_id using lt_obj;
							System.out.println("SalesReturn :actionFullSr:INVOICE_TRACE:1:sql:"+sql);
							stmtMain = conn.createStatement();
							rsMain = stmtMain.executeQuery(sql);
							while (rsMain.next())
							{
								invLine	=  rsMain.getLong(1);
								lotNo =rsMain.getString(2);
								lotSl =rsMain.getString(3);
								qty =rsMain.getDouble(4);
								itemCode = rsMain.getString(5);
								rate = rsMain.getDouble(6);
								rateStduom = rsMain.getDouble(7);
								discount = rsMain.getDouble(8);
								//Added on 08/06/06 as code changes in PB
								despID = rsMain.getString(9);
								despLineNo = rsMain.getString(10);
								invTraceLineNo = rsMain.getInt(11);
								lotNo = lotNo == null ? "              " : lotNo;
								lotSl = lotSl == null ? "              " : lotSl;

								System.out.println("invLine:"+invLine+":lotNo:"+lotNo+":lotSl:"+lotSl+":qty:"+qty+":");
								System.out.println("rate:"+rate+":rateStduom:"+rateStduom+":discount:"+discount+":");
								System.out.println("Desp ID :"+despID+": despLineNo :"+despLineNo+":invTraceLineNo :"+invTraceLineNo+":");

								//Picked total qty from invoice and line no of invoice
								invQty = 0;
								sql="SELECT CASE WHEN (SUM(CASE WHEN QUANTITY__STDUOM IS NULL THEN 0 ELSE QUANTITY__STDUOM END))"+
									"IS NULL THEN 0 ELSE SUM(CASE WHEN QUANTITY__STDUOM IS NULL THEN 0 ELSE QUANTITY__STDUOM END)"+
									"END FROM INVOICE_TRACE "+
									" WHERE INVOICE_ID ='"+invoiceId+"'  AND INV_LINE_NO = "+invLine+""+ //Single quote removed as it does not work in PB.
									" AND LINE_NO =	"+invTraceLineNo+""; // Added on 08/06/06 as code changes in PB

								System.out.println("SalesReturn :actionFullSr:INVOICE_TRACE:2:sql:"+sql);
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									invQty =rs.getDouble(1)	;
								}
								rs.close();//Gulzar - 21/11/06
								stmt.close();
								System.out.println("invQty:"+invQty+":");
								//Picked total qty from sreturn for invoice and line no of invoice
								sreturnQty = 0;
								sql=" SELECT CASE WHEN (SUM(CASE WHEN QUANTITY__STDUOM IS NULL THEN 0 ELSE QUANTITY__STDUOM END)) "+
									" IS NULL THEN 0 ELSE SUM(CASE WHEN QUANTITY__STDUOM IS NULL THEN 0 ELSE QUANTITY__STDUOM END) END"+
									" FROM SRETURN A, SRETURNDET B "+
									"  WHERE A.TRAN_ID = B.TRAN_ID "+
									"  AND B.INVOICE_ID ='"+invoiceId+"'"+
									"  AND B.LINE_NO__INV = "+invLine+" "+
									"  AND (B.LINE_NO__INVTRACE = "+invTraceLineNo+ "  OR  B.LINE_NO__INVTRACE IS NULL OR B.LINE_NO__INVTRACE = 0)"+ //Gulzar 02/09/06
									"  AND B.LOT_NO ='"+lotNo+"'"+ //Gulzar 02/09/06
									"  AND B.LOT_SL ='"+lotSl+"'"+ //Gulzar 02/09/06
									"  AND A.STATUS <> 'X' " ;
								System.out.println("SalesReturn :actionFullSr: SRETURN & SRETURNDET:1:sql:"+sql);
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									sreturnQty =rs.getDouble(1);
								}
								rs.close();//Gulzar - 21/11/06
								stmt.close();
								System.out.println("sreturnQty:"+sreturnQty+":");
								sql=" SELECT SUM(B.QUANTITY__STDUOM) FROM SRETURN A, SRETURNDET B "+
									"	WHERE B.TRAN_ID = A.TRAN_ID "+
									"	AND B.INVOICE_ID ='"+invoiceId+"' "+
									"	AND B.LINE_NO__INV = "+invLine+" "+
									"	AND (B.LINE_NO__INVTRACE = "+invTraceLineNo+ "  OR  B.LINE_NO__INVTRACE IS NULL OR B.LINE_NO__INVTRACE = 0)"+ //Gulzar 02/09/06
									"	AND B.LOT_NO ='"+lotNo+"' "+ //UnComment by Gulzar 02/09/06
									"	AND B.LOT_SL ='"+lotSl+"' "+ //UnComment by Gulzar 02/09/06
									"	AND B.TRAN_ID <> '"+tranId+"' "+
									"	AND A.STATUS <> 'X' " ;
								System.out.println("SalesReturn :actionFullSr: SRETURN & SRETURNDET:2:sql:"+sql);
								//into :lc_prev_srqty
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									prevSrqty =rs.getDouble(1)	;
								}
								rs.close();//Gulzar - 21/11/06
								stmt.close();
								System.out.println("prevSrqty:"+prevSrqty+":");
								/*if(prevSrqty != 0 )//check for null
								{
									prevSrqty = 0 ;
								} */
								qty = qty - prevSrqty ;
								if (qty == 0)
								{
									continue;
								}
								sreturnQty = sreturnQty + qty ;
								setFlag = "Y"; // Changed by Sharon in PB script on 24th nov,05.
								/*** Commented in PB script by Sharon on 24th nov,05.
								if(sreturnQty < invQty)
								{
									setFlag = "N";
								}
								else
								{
									setFlag = "Y";
								}
								***/
								System.out.println("setFlag:"+setFlag+":");
								valueXmlString.append("<Detail>\r\n");
								//valueXmlString.append("<line_no__inv isSrvCallOnChg='1'>").append("<![CDATA[").append(invLine).append("]]>").append("</line_no__inv>\r\n"); //Gulzar 02/09/06
								valueXmlString.append("<line_no__invtrace isSrvCallOnChg='1'>").append("<![CDATA[").append(invTraceLineNo).append("]]>").append("</line_no__invtrace>\r\n"); //Gulzar 02/09/06
								valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(qty).append("]]>").append("</quantity>\r\n");

								//Added on 08/06/06 as code changes in PB
								sql = "SELECT LOC_CODE FROM DESPATCHDET WHERE DESP_ID = '"+despID+"' AND LTRIM(RTRIM(LINE_NO)) = LTRIM(RTRIM('"+despLineNo+"'))";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									String locCode = "",locDescr = "";
									locCode = rs.getString("LOC_CODE");
									rs.close();
									stmt.close();
									if (locCode != null && locCode.trim().length() > 0)
									{
										valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
										sql = "SELECT DESCR FROM LOCATION WHERE LOC_CODE = '"+locCode+"'";
										stmt = conn.createStatement();
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											locDescr = rs.getString("DESCR");
											valueXmlString.append("<location_descr>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
										}
										rs.close();
										stmt.close();
									}
								}
								// End
								//rs.close();//Gulzar - 21/11/06
								//stmt.close();
								valueXmlString.append("<full_ret>").append("<![CDATA[").append(setFlag.trim()).append("]]>").append("</full_ret>\r\n");
								valueXmlString.append("<reas_code>").append("<![CDATA[").append(reasonCode.trim()).append("]]>").append("</reas_code>\r\n");
								valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");// Removied .trim() by Nandkumar Gadkari on 08/08/18
								//valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");//Gulzar 02/09/06
								//valueXmlString.append("<rate__stduom>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n"); //Gulzar 02/09/06
								valueXmlString.append("<discount>").append("<![CDATA[").append(discount).append("]]>").append("</discount>\r\n");
								//valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
								// 08/08/08 manoharan itemchange not required
								valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n"); // Removied .trim() by Nandkumar Gadkari on 08/08/18
								rate = 0;
								sql="SELECT RATE FROM INVOICE_TRACE "+
									" WHERE INVOICE_ID ='"+invoiceId+"'"+
									" AND LINE_NO =	"+invTraceLineNo+"";

								//into :lc_prev_srqty
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									rate = rs.getDouble(1)	;
								}
								rs.close();//Gulzar - 21/11/06
								stmt.close();
								valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");

								// end 08/08/08 manoharan itemchange not required
								//ls_old_item_code = ls_item_code
								valueXmlString.append("</Detail>\r\n");
							}//loop closing
							rsMain.close();//Gulzar - 21/11/06
							stmtMain.close();
						}//reasonCode != N (End if)
						else
						{
							 System.out.println("reasonCode is null");
						}
					}//detcnt = o (End if)
				}//fullSr != N (End if)
		 	}//conf != Y (End if)
			valueXmlString.append("</Root>\r\n");
		}
		catch(Exception e)
		{
			System.out.println("Exception :SalesReturn :actionFullSR:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");

				conn.close();
			}catch(Exception e){}
	    }
		System.out.println("SalesReturn :actionFullSR:Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	//method added by nandkumar gadkari on 25/06/19------------------------------------start--------------------------------------
	private String splitMinRateHistBalAct(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null,pstmt2 = null, pstmt1 = null;
		ResultSet rs = null,rs2 = null, rs1 = null;
		String sql = "";
		String errCode = "";
		String errString = "";
		String flag = "";
		String  invoiceId = "";
		String	siteCode = "";
		String	itemCode = "";
		String	locCode = "";
		String	lotNoInv = "           ";
		String	lotSlInv = "           ";
		String	sreturnAdjOpt = "";
		String	orderByStr = "";
		String	custCode = "";
		String	reason = "";
		String	docKey = "";
		String	siteCodeMfgFn = "";
		String	packCodeFn = "";
		String	chkDate = "";
		String	quantity = "";
		String stkMfgDate = "";
		String stkExpDate = "";
		String lotNo ="         ";
		String lotSl ="         ";
		String siteCodeMfg ="";
		String packCode ="";
		String	unit = "";
		java.sql.Date expDate = null;
		java.sql.Date mfgDate = null;
		int	minShelfLife = 0;
	  	int detCnt =0,cnt=0,lotCnt=0;
		double totStk =0.0;
		double inputQty = 0.0,lotQty=0;
		double qty =0;
		double qtyStk = 0,invoiceQty=0,adjQty=0,rate=0;
		DistCommon distCommon = null;
		distCommon = new DistCommon();	
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{
		   	
			conn = getConnection();
				
				System.out.println("\n******************** S T A R T   *******splitMinRateHistBalAct*********************");
				
					siteCode = genericUtility.getColumnValue("site_code",dom1);
					itemCode = genericUtility.getColumnValue("item_code",dom);
					quantity = genericUtility.getColumnValue("quantity",dom);
					lotNo = genericUtility.getColumnValue("lot_no",dom);
					custCode = genericUtility.getColumnValue("cust_code",dom1);
					
					System.out.println("Values From DOM :siteCode:"+siteCode+":invoiceId:"+invoiceId+":quantity:"+quantity+":");
				
					if(quantity != null && quantity.trim().length() > 0)
					{
						qty = Double.parseDouble(quantity); 
						sreturnAdjOpt = distCommon.getDisparams("999999", "SRETURN_ADJ_OPT", conn);
						System.out.println("sreturnAdjOpt:::["+sreturnAdjOpt+"]");
						if (sreturnAdjOpt == null || sreturnAdjOpt.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
						{
							sreturnAdjOpt="M";
						}
						if ("M".equalsIgnoreCase(sreturnAdjOpt))
						{
							orderByStr = " ORDER BY MRH.EFF_COST ";
						}
						else if("E".equalsIgnoreCase(sreturnAdjOpt))
						{
							orderByStr = " ORDER BY MRH.INVOICE_DATE ASC,MRH.INVOICE_ID ASC ";
						}
						else if("L".equalsIgnoreCase(sreturnAdjOpt))
						{
							orderByStr = " ORDER BY MRH.INVOICE_DATE DESC,MRH.INVOICE_ID DESC ";
						}
						//if(lotNo != null && lotNo.trim().length() > 0 && qty > 0 && itemCode != null && itemCode.trim().length() > 0)
						if(lotNo != null  && qty > 0 && itemCode != null && itemCode.trim().length() > 0)
						{
						
							sql = " SELECT MRH.DOC_KEY,MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
									+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
									+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
									+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
									+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
									+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
									+ " AND MRH.QUANTITY IS NOT NULL"
									+ " GROUP BY MRH.DOC_KEY,MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
									+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0 "
									+ orderByStr;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lotNo);
							pstmt.setString(4, siteCode);
							rs = pstmt.executeQuery();
							
							while(rs.next())
							{
								
								if(qty >0)
								{
									invoiceId = checkNullandTrim(rs.getString("INVOICE_ID"));
									invoiceQty =rs.getDouble("QUANTITY");
									adjQty = rs.getDouble("QTY_ADJ");
									docKey = checkNull(rs.getString("DOC_KEY"));
									rate = rs.getDouble("EFF_COST");
									cnt=0;
									/*if (docKey.trim().length() > 0) {
										
										String[] docKeyStr = docKey.split(",");
										
										for(int j=0; j<docKeyStr.length; j++)
										{	
											System.out.println( "docKeyStr :: " + docKeyStr[j]);
											System.out.println( "docKeyStrlength :: " + docKeyStr.length);
											cnt++;
										}
									}
										System.out.println( "cnt :: " + cnt );
									
									if(cnt==5)*/
									//ADDED BY NANDKUMAR GADKARI ON 14/08/19----------------------START-------------------
									System.out.println( " docKey.indexOf(invoiceId)" +  docKey.indexOf(invoiceId) );
									if( docKey.indexOf(invoiceId) != -1)
									{
										
										sql = "select count(*) from invoice_trace where invoice_id = ?  and item_code = ? and lot_no = ? ";
										 pstmt2 = conn.prepareStatement(sql);
				                         pstmt2.setString(1, invoiceId);
				                         pstmt2.setString(2, itemCode);
				                         pstmt2.setString(3, lotNo);
	                                     rs2 = pstmt2.executeQuery();
				                         if (rs2.next())
				                         {
				                         	lotCnt = rs2.getInt(1);
				                         }
				                         pstmt2.close();
				                         pstmt2 = null;
				                         rs2.close();
				                         rs2 = null;
										
										if(lotCnt > 1 && adjQty == 0)
										{
											sql = "select quantity from invoice_trace where invoice_id = ?  and item_code = ? and lot_no = ? ";
					                    	 pstmt2 = conn.prepareStatement(sql);
					                         pstmt2.setString(1, invoiceId);
					                         pstmt2.setString(2, itemCode);
					                         pstmt2.setString(3, lotNo);
		                                     rs2 = pstmt2.executeQuery();
					                         while (rs2.next())
					                         {
					                         	lotQty = rs2.getDouble(1);
					                         	
					                         	valueXmlString.append("<Detail>\r\n");
												valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
												if(qty > lotQty)
												{
													valueXmlString.append("<quantity>").append("<![CDATA[").append(lotQty).append("]]>").append("</quantity>\r\n");
												}
												else {
													valueXmlString.append("<quantity>").append("<![CDATA[").append(qty).append("]]>").append("</quantity>\r\n");
												}
												valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
												valueXmlString.append("<invoice_ref>").append("<![CDATA[").append(invoiceId).append("]]>").append("</invoice_ref>\r\n");
												valueXmlString.append("</Detail>\r\n");
												qty=qty-lotQty;
												if(qty <= 0)
												{
													break;
												}
					                         	
					                         }
					                         pstmt2.close();
					                         pstmt2 = null;
					                         rs2.close();
					                         rs2 = null;
											
										}
										else
										{
										//ADDED BY NANDKUMAR GADKARI ON 14/08/19-----------------------END--------------------
										
										inputQty=invoiceQty-adjQty;
										System.out.println( "inputQty :: " + inputQty);System.out.println( "qty :: " + qty);
										if(inputQty > 0)
										{
											valueXmlString.append("<Detail>\r\n");
												valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
												if(qty > inputQty)
												{
													valueXmlString.append("<quantity>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
												}
												else {
													valueXmlString.append("<quantity>").append("<![CDATA[").append(qty).append("]]>").append("</quantity>\r\n");
												}
												valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
												valueXmlString.append("<invoice_ref>").append("<![CDATA[").append(invoiceId).append("]]>").append("</invoice_ref>\r\n");
												valueXmlString.append("</Detail>\r\n");
												qty=qty-inputQty;
										}
										}
									}
									if(qty <= 0)
									{
										break;
									}
								}
							}//loop end
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
							if (rs != null) {
								rs.close();
								rs = null;
							}
						}
						
					}
									
			
			valueXmlString.append("</Root>\r\n");
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("SReturnMinRateHistBalAct:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				conn.close();
				conn = null;
				return errString;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SalesReturn :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("SReturnMinRateHistBalAct :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	//method added by nandkumar gadkari on 25/06/19------------------------------------end--------------------------------------
	public String getRelativeDate(int no) throws RemoteException , ITMException
	{
		String relativeDate = "";
		Calendar  calObject = Calendar.getInstance();
		java.util.Date  dateId =new java.util.Date();
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			calObject.setTime(dateId);
			calObject.add(Calendar.DATE,no);
			dateId = calObject.getTime();
			System.out.println("getRelativeDate():dateId:"+dateId.toString());
			relativeDate = sdf.format(dateId);
		}catch(Exception e)
		{
			System.out.println("Exception :SalesReturn:getRelativeDate() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
			System.out.println("getRelativeDate():relativeDate:"+relativeDate);
		return relativeDate;
	}
	//Added By Mukesh Chauhan on 23/08/19
	
	public String invoicePickaction(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;

		String  retString = null;
		try
		{
			System.out.println("xmlString :::"+xmlString);
			System.out.println("xmlString1 :::"+xmlString1);
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			System.out.println("actionType:"+actionType+":");
						
			if (actionType.equalsIgnoreCase("splitInvoice"))
			{
				retString = getInvoicePickBtn(dom, dom1, objContext, xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Porcp :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from Porcp : actionHandlerTransform"+retString);
	    return retString;

	}
	
	public String getInvoicePickBtn(Document dom, Document dom1,String objContext, String xtraParams)throws ITMException 
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null, pstmt2 = null, pstmtLot = null, pstmt1 = null;
		ResultSet rs = null, rs2 = null, rsLot = null, rs1 = null;
		String sql = "";
		String errCode = "";
		String lotNo ="         ";
		String	siteCode = "";
		String	itemCode = "";
		String errString = "";
		String flag = "";
		String  invoiceId = "";
		String	locCode = "";
		String	lotNoInv = "";
		String	lotSlInv = "";
		String	sreturnAdjOpt = "";
		String	orderByStr = "";
		String	custCode = "";
		String	reason = "";
		String	docKey = "";
		String	siteCodeMfgFn = "";
		String	packCodeFn = "";
		String	chkDate = "";
		String	quantity = "";
		String stkMfgDate = "";
		String stkExpDate = "";
		String lotSl ="";
		String siteCodeMfg ="",newDomXml="";
		String packCode ="";
		String	unit = "", refNo = "", refLineNo = "", sqlCnt="", sqlDetData = "", scheme = "";
		java.sql.Date expDate = null;
		java.sql.Date mfgDate = null;
		int	minShelfLife = 0;
	  	int detCnt =0,cnt=0,lotCnt=0;
		double totStk =0.0;
		double inputQty = 0.0,lotQty=0;
		double qty =0;
		double qtyStk = 0,invoiceQty=0,adjQty=0,rate=0;
		DistCommon distCommon = null;
		distCommon = new DistCommon();
		E12GenericUtility genericUtility= new E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		int domID = 0,domID1=0;
		java.util.Date invoiceDate= null;
		double invTraceRate=0, avgRate=0, invQty = 0.0, totQty = 0, totItemLotQuantity = 0, totAvgRate = 0;
        int schemeCnt=0;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			conn = getConnection();
				
				System.out.println("\n******************** S T A R T   *******getInvoicePickBtn*********************TEST");
				
					siteCode = genericUtility.getColumnValue("site_code",dom1);
					itemCode = genericUtility.getColumnValue("item_code",dom);
					quantity = genericUtility.getColumnValue("quantity",dom);
					lotNo = genericUtility.getColumnValue("lot_no",dom);
					lotNo = lotNo == null ? "              " : lotNo;					
					custCode = genericUtility.getColumnValue("cust_code",dom1);
					
					System.out.println("Values From DOM :siteCode:"+siteCode+":invoiceId:"+invoiceId+":quantity:"+quantity+":");
				
					if(quantity != null && quantity.trim().length() > 0)
					{
						qty = Double.parseDouble(quantity); 
						sreturnAdjOpt = distCommon.getDisparams("999999", "SRETURN_ADJ_OPT", conn);
						System.out.println("sreturnAdjOpt:::["+sreturnAdjOpt+"]");
						if (sreturnAdjOpt == null || sreturnAdjOpt.trim().length()== 0 || "NULLFOUND".equalsIgnoreCase(sreturnAdjOpt))
						{
							sreturnAdjOpt="M";
						}
						if ("M".equalsIgnoreCase(sreturnAdjOpt))
						{
							orderByStr = " ORDER BY MRH.EFF_COST ";
						}
						else if("E".equalsIgnoreCase(sreturnAdjOpt))
						{
							orderByStr = " ORDER BY MRH.INVOICE_DATE ASC,MRH.INVOICE_ID ASC ";
						}
						else if("L".equalsIgnoreCase(sreturnAdjOpt))
						{
							orderByStr = " ORDER BY MRH.INVOICE_DATE DESC,MRH.INVOICE_ID DESC ";
						}
						if(lotNo != null && lotNo.trim().length() > 0 && qty > 0 && itemCode != null && itemCode.trim().length() > 0)
						{
						
							sql = " SELECT MRH.DOC_KEY,MRH.INVOICE_ID,MRH.QUANTITY, MRH.CUST_CODE, MRH.ITEM_CODE, MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE,"
									+ "	SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) AS QTY_ADJ, MRH.EFF_COST"
									+ " FROM MIN_RATE_HISTORY MRH,  SRETURNDET SRDET"
									+ " WHERE MRH.DOC_KEY =SRDET.DOC_KEY(+) AND MRH.CUST_CODE = ?"
									+ " AND MRH.ITEM_CODE = ? AND MRH.LOT_NO = ?"
									+ " AND MRH.SITE_CODE = ? AND MRH.QUANTITY - CASE WHEN MRH.QUANTITY_ADJ IS NULL THEN 0 ELSE MRH.QUANTITY_ADJ END > 0"
									+ " AND MRH.QUANTITY IS NOT NULL"
									+ " GROUP BY MRH.DOC_KEY,MRH.INVOICE_ID,  MRH.QUANTITY,  MRH.EFF_COST,MRH.CUST_CODE,MRH.ITEM_CODE,MRH.LOT_NO, MRH.SITE_CODE, MRH.INVOICE_DATE"
									+ " HAVING MRH.QUANTITY-SUM( CASE WHEN SRDET.QUANTITY IS NULL THEN 0 ELSE SRDET.QUANTITY END) > 0 "
									+ orderByStr;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lotNo);
							pstmt.setString(4, siteCode);
							rs = pstmt.executeQuery();
							
							while(rs.next())
							{
								System.out.println("Inside while");
								
                                    invoiceId = checkNullandTrim(rs.getString("INVOICE_ID"));
                                    System.out.println("invoiceId:: "+invoiceId);
									invoiceQty =rs.getDouble("QUANTITY");
                                    System.out.println("invoiceQty:: "+invoiceQty);
                                    adjQty = rs.getDouble("QTY_ADJ");
                                    System.out.println("adjQty:: "+adjQty);
									docKey = checkNull(rs.getString("DOC_KEY"));
                                    System.out.println("docKey:: "+docKey);
                                    rate = rs.getDouble("EFF_COST");
                                    System.out.println("rate:: "+rate);
                                    invoiceDate = rs.getDate("INVOICE_DATE");
                                    System.out.println("invoiceDate:: "+invoiceDate);
									cnt=0;
									
									System.out.println( " docKey.indexOf(invoiceId)" +  docKey.indexOf(invoiceId) );
									if( docKey.indexOf(invoiceId) != -1)
									{
										
										//-----added by nandkumar gadkari on 24/05/19---------start------------
										sql = "select rate from invoice_trace where invoice_id = ?  and item_code = ? and lot_no=? and rate > 0 ";
										 pstmt2 = conn.prepareStatement(sql);
				                         pstmt2.setString(1, invoiceId);
				                         pstmt2.setString(2, itemCode);
				                         pstmt2.setString(3, lotNo);
	                                     rs2 = pstmt2.executeQuery();
				                         if (rs2.next())
				                         {
                                             invTraceRate = rs2.getDouble(1);
                                             System.out.println("invTraceRate:: "+invTraceRate);
				                         }
				                         pstmt2.close();
				                         pstmt2 = null;
				                         rs2.close();
				                         rs2 = null;
                                         
                                         //Added by Anagha R on 17/03/2021 for PARTY DETAILS FOR SALABLE CN INV REF(PointNo. 2906 & 2941) START
                                         sqlDetData = "select SORD_NO, SORD_LINE_NO from invoice_trace where invoice_id = ?  and item_code = ? and lot_no=?"; 
                                         pstmt1 = conn.prepareStatement(sqlDetData);
                                         pstmt1.setString(1, invoiceId);
                                         pstmt1.setString(2, itemCode);
                                         pstmt1.setString(3, lotNo);
                                         rs1 = pstmt1.executeQuery();
                                         if (rs1.next())
                                         {
                                             refNo = rs1.getString("SORD_NO");
                                             System.out.println("refNo:: "+refNo);
                                             refLineNo = rs1.getString("SORD_LINE_NO");
                                             System.out.println("refNo:: "+refNo+" refLineNo:: "+refLineNo);
                                         }
                                         rs1.close();
                                         rs1 = null;
                                         pstmt1.close();
                                         pstmt1 = null;    
                                                        
                                         sql = "select count(distinct EXP_LEV) as ll_scheme from invoice_trace	where invoice_id = ? "
                    			            + " and SORD_NO =?	and SORD_LINE_NO = ? and item_code = ? ";
                                         
                                         pstmt2 = conn.prepareStatement(sql);
                                         pstmt2.setString(1, invoiceId);
                                         pstmt2.setString(2, refNo);
                                         pstmt2.setString(3, refLineNo);
                                         pstmt2.setString(4, itemCode);
                                         
                                         rs2 = pstmt2.executeQuery();
                                         if (rs2.next())
                                         {
                                            schemeCnt = rs2.getInt(1);
                                         }
                                         System.out.println("schemeCnt:: "+schemeCnt);
                                         pstmt2.close();
                                         pstmt2 = null;
                                         rs2.close();
                                         rs2 = null;

                                         if(schemeCnt == 1)
                                         {
                    	                    sql = "select lot_no,sum(quantity),Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),4) as inv_rate 	from invoice_trace 	"//change round 2 to 4  by nandkumar gadkari on 26-05-2020
                                              + "	where invoice_id = ? 	and SORD_NO = ? 	and SORD_LINE_NO = ? 		and item_code = ? 		group by lot_no";
                                            System.out.println("schemeCnt_sql:: "+sql);  
                                         }else
                                         {
                                             sql = "select distinct lot_no ,quantity from invoice_trace where invoice_id = ?  and sord_no = ?"//,quantity COLUMN ADDED BY NANDKUMAR GADKARI ON 10/07/19
                                            + " and sord_line_no = ?  and item_code = ? order by lot_no";
                    	                    lotCnt =0;
                    	                    sqlCnt = "select count(distinct lot_no) from invoice_trace where invoice_id = ?  and sord_no = ? and sord_line_no = ?  and item_code = ? ";
                    	                    pstmt2 = conn.prepareStatement(sqlCnt);
                                            pstmt2.setString(1, invoiceId);
                                            pstmt2.setString(2, refNo);
                                            pstmt2.setString(3, refLineNo);
                                            pstmt2.setString(4, itemCode);
                                            rs2 = pstmt2.executeQuery();
                                            if (rs2.next())
                                            {
                                                 lotCnt = rs2.getInt(1);
                                                 System.out.println("lotCnt:: "+lotCnt);
                                            }
                                            pstmt2.close();
                                            pstmt2 = null;
                                            rs2.close();
                                            rs2 = null;
                                        }
                                            pstmtLot = conn.prepareStatement(sql);
                                            pstmtLot.setString(1, invoiceId);
                                            pstmtLot.setString(2, refNo);
                                            pstmtLot.setString(3, refLineNo);
                                            pstmtLot.setString(4, itemCode);

                                            rsLot = pstmtLot.executeQuery();
                                            while (rsLot.next())
                                            {
                                                lotNo = rsLot.getString("lot_no");
                                                System.out.println("lotNo1362:: "+lotNo);
                                                invQty =rsLot.getDouble(2);
                                                System.out.println("invQty:: "+invQty);
                                                if(schemeCnt == 1)
                                                {
                        	                        avgRate = rsLot.getDouble("inv_rate");
                                                    System.out.println("avgRate:: "+avgRate);
                                                }
                       
                                                if(lotCnt == 1)
                                                {
                    	                            invQty=totQty;
                                                }

                                                sql = " select SUM(quantity), Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),2) from invoice_trace where "
								                        + " invoice_id = ?   and  item_code = ? and lot_no = ? and line_type not in ('I','V','P')  ";//commented by nandkumar gadkari on 20/01/20
                    		                    pstmt2 = conn.prepareStatement(sql);
							                    pstmt2.setString(1, invoiceId);
							                    pstmt2.setString(2, itemCode);
							                    pstmt2.setString(3, lotNo);
							                    rs2 = pstmt2.executeQuery();
							                    if (rs2.next())
							                    {
								                    totItemLotQuantity = rs2.getDouble(1);
		                		                    if (schemeCnt == 1)
								                    {
														totAvgRate = rs2.getDouble(2); //commented by nandkumar gadkari on 20/01/20
								                    }
												}
							                    pstmt2.close();
							                    pstmt2 = null;
							                    rs2.close();
                                                rs2 = null;
                                                
                                                if(totItemLotQuantity > invQty)
							                    {
								                    invQty=totItemLotQuantity;
			            		                    if (schemeCnt == 1)
								                    {
									                    avgRate =totAvgRate; //commented by nandkumar gadkari on 20/01/20
								                    }
												}
                                                    
                                                if (schemeCnt > 1)
							                    {
	                								sql = " select Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),4) from invoice_trace where "//change round 2 to 4  by nandkumar gadkari on 26-05-2020
					                					+ " invoice_id = ?   and  item_code = ? and line_type not in ('I','V','P')  ";
									                pstmt2 = conn.prepareStatement(sql);
									                pstmt2.setString(1, invoiceId);
									                pstmt2.setString(2, itemCode);
									
									                rs2 = pstmt2.executeQuery();
									                if (rs2.next())
									                {
										                totAvgRate = rs2.getDouble(1);
													}
									                pstmt2.close();
									                pstmt2 = null;
									                rs2.close();
									                rs2 = null;
                                                }
                                                avgRate =totAvgRate;							
                                            }
                                            pstmtLot.close();
                                            pstmtLot = null;
                                            rsLot.close();
                                            rsLot = null;
                                         }
                                        
                    	                 //Added by Anagha R on 17/03/2021 for PARTY DETAILS FOR SALABLE CN INV REF(PointNo. 2906 & 2941) END
                                        
                                         //Commented by Anagha R on 17/03/2021 for PARTY DETAILS FOR SALABLE CN INV REF(PointNo. 2906 & 2941) START
                                         /*sql = "select Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),2)  	from invoice_trace where invoice_id = ?  and item_code = ? ";
										 pstmt2 = conn.prepareStatement(sql);
				                         pstmt2.setString(1, invoiceId);
				                         pstmt2.setString(2, itemCode);
				                         rs2 = pstmt2.executeQuery();
				                         if (rs2.next())
				                         {
				                         	avgRate = rs2.getDouble(1);
				                         }  
				                         pstmt2.close();
				                         pstmt2 = null;
				                         rs2.close();
                                         rs2 = null;*/
                                         //Commented by Anagha R on 17/03/2021 for PARTY DETAILS FOR SALABLE CN INV REF(PointNo. 2906 & 2941) END
                                         
				                       //-----added by nandkumar gadkari on 24/05/19---------end------------
										sql = "select count(*) from invoice_trace where invoice_id = ?  and item_code = ? and lot_no = ? ";
										 pstmt2 = conn.prepareStatement(sql);
				                         pstmt2.setString(1, invoiceId);
				                         pstmt2.setString(2, itemCode);
				                         pstmt2.setString(3, lotNo);
	                                     rs2 = pstmt2.executeQuery();
				                         if (rs2.next())
				                         {
                                             lotCnt = rs2.getInt(1);
                                             System.out.println("lotCnt1459:: "+lotCnt);
				                         }
				                         pstmt2.close();
				                         pstmt2 = null;
				                         rs2.close();
				                         rs2 = null;
										
                                        //if(lotCnt > 1 && adjQty == 0)
                                        if(lotCnt > 1)//Changed by Anagha R on 15/03/2021 for PARTY DETAILS FOR SALABLE CN INV REF(PointNo. 2906 & 2941) adjQty to be shown as per the quantity_adj on FIFO basis
										{
											sql = "select quantity,rate from invoice_trace where invoice_id = ?  and item_code = ? and lot_no = ? ";// rate added by nanadkumar gadkari on 26/09/19
					                    	 pstmt2 = conn.prepareStatement(sql);
					                         pstmt2.setString(1, invoiceId);
					                         pstmt2.setString(2, itemCode);
					                         pstmt2.setString(3, lotNo);
		                                     rs2 = pstmt2.executeQuery();
					                         while (rs2.next())
					                         {
					                         	lotQty = rs2.getDouble(1);
					                         	invTraceRate = rs2.getDouble(2);
					                         	domID++;
					                         	valueXmlString.append("<Detail>\r\n");	
					                         	
					                         	valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
												valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
												valueXmlString.append("<quantity>").append("<![CDATA[").append(lotQty).append("]]>").append("</quantity>\r\n");
												
												valueXmlString.append("<invoice_ref>").append("<![CDATA[").append(invoiceId).append("]]>").append("</invoice_ref>\r\n");
												if(invoiceDate != null)
												{
													valueXmlString.append("<invoice_date>").append("<![CDATA[").append(sdf.format(invoiceDate)).append("]]>").append("</invoice_date>\r\n");
												}
												else
												{
													valueXmlString.append("<invoice_date>").append("<![CDATA[").append("").append("]]>").append("</invoice_date>\r\n");
												}
					                         	valueXmlString.append("<effective_cost>").append("<![CDATA[").append(rate).append("]]>").append("</effective_cost>\r\n");
					                         	valueXmlString.append("<invoice_quantity>").append("<![CDATA[").append(lotQty).append("]]>").append("</invoice_quantity>\r\n");
					                         	//valueXmlString.append("<used_quantity>").append("<![CDATA[").append(0.0).append("]]>").append("</used_quantity>\r\n");
                                                valueXmlString.append("<used_quantity>").append("<![CDATA[").append(adjQty).append("]]>").append("</used_quantity>\r\n");//Changed by Anagha R on 15/03/2021 for PARTY DETAILS FOR SALABLE CN INV REF(PointNo. 2906 & 2941) adjQty to be shown as per the quantity_adj on FIFO basis
					                         //  added by nanadkumar gadkari on 26/09/19
					                         	valueXmlString.append("<invoice_trace_rate>").append("<![CDATA[").append(invTraceRate).append("]]>").append("</invoice_trace_rate>\r\n");
					                         	valueXmlString.append("<average_rate>").append("<![CDATA[").append(avgRate).append("]]>").append("</average_rate>\r\n");
					                         	valueXmlString.append("</Detail>\r\n");
												
					                         	
					                         }
					                         pstmt2.close();
					                         pstmt2 = null;
					                         rs2.close();
					                         rs2 = null;
											
										}
										else
										{
										
											
											inputQty=invoiceQty-adjQty;
                                            System.out.println( "inputQty :: " + inputQty);
                                            System.out.println( "qty :: " + qty);
											if(inputQty > 0)
											{
												
												valueXmlString.append("<Detail>\r\n");	
												valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
												valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
												valueXmlString.append("<quantity>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
													
													valueXmlString.append("<invoice_ref>").append("<![CDATA[").append(invoiceId).append("]]>").append("</invoice_ref>\r\n");
													if(invoiceDate != null)
													{
														valueXmlString.append("<invoice_date>").append("<![CDATA[").append(sdf.format(invoiceDate)).append("]]>").append("</invoice_date>\r\n");
													}
						                         	else
													{
														valueXmlString.append("<invoice_date>").append("<![CDATA[").append("").append("]]>").append("</invoice_date>\r\n");
													}
						                         	valueXmlString.append("<effective_cost>").append("<![CDATA[").append(rate).append("]]>").append("</effective_cost>\r\n");
						                         	valueXmlString.append("<invoice_quantity>").append("<![CDATA[").append(invoiceQty).append("]]>").append("</invoice_quantity>\r\n");
						                         	valueXmlString.append("<used_quantity>").append("<![CDATA[").append(adjQty).append("]]>").append("</used_quantity>\r\n");
						                         	 //  added by nanadkumar gadkari on 26/09/19
						                         	valueXmlString.append("<invoice_trace_rate>").append("<![CDATA[").append(invTraceRate).append("]]>").append("</invoice_trace_rate>\r\n");
						                         	valueXmlString.append("<average_rate>").append("<![CDATA[").append(avgRate).append("]]>").append("</average_rate>\r\n");
						                         	valueXmlString.append("</Detail>\r\n");
													
											}
										}
									//}
									
								
							}//loop end
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}
							if (rs != null) {
								rs.close();
								rs = null;
							}
						}
						
					}
									
			
			valueXmlString.append("</Root>\r\n");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println(" retXmlString 4 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
        }
        finally
		{
			try
			{
				if (distCommon != null) { distCommon = null;}
				if(conn!=null)
				{
					if(rs != null)rs.close();
					rs = null;
					if(pstmt != null)pstmt.close();
					pstmt = null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException( d );
			}
			//System.out.println("[SOrderForm] CONNECTION is CLOSED");
		}
		return valueXmlString.toString();
	}
	//End of Invoicepickbutton method
	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
				}
			}
			System.out.println("actionType:"+actionType+":");

			if (actionType.equalsIgnoreCase("invoices"))
			{
				retString = invoiceTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception :SalesReturnAct :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		System.out.println("returning String from SalesReturnAct.................."); 
		return retString;
	}
	private String invoiceTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null,pstmt2 = null;
		ResultSet rs = null,rs2 = null;
		String sql = "";
		String errCode = "";
		String lotNo ="";
		String	siteCode = "";
		String	itemCode = "";
		String errString = "";
		String flag = "";
		String  invoiceId = "";
		String	locCode = "";
		String	lotNoInv = "";
		String	lotSlInv = "";
		String	sreturnAdjOpt = "";
		String	orderByStr = "";
		String	custCode = "";
		String	reason = "";
		String	docKey = "";
		String	siteCodeMfgFn = "";
		String	packCodeFn = "";
		String	chkDate = "";
		String	quantity = "";
		String stkMfgDate = "";
		String inputQtyStr = "";
		String lotSl ="";
		String siteCodeMfg ="",newDomXml="";
		String packCode ="";
		String	unit = "";
		java.sql.Date expDate = null;
		java.sql.Date mfgDate = null;
		int	minShelfLife = 0;
	  	int detCnt =0,cnt=0,lotCnt=0;
		double totStk =0.0;
		double inputQty = 0.0,lotQty=0;
		double qty =0;
		double qtyStk = 0,invoiceQty=0,adjQty=0,rate=0;
		DistCommon distCommon = null;
		distCommon = new DistCommon();
		E12GenericUtility genericUtility= new E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		DecimalFormat df = new DecimalFormat("#########.###");

		try
		{
			conn = getConnection();
			quantity = genericUtility.getColumnValue("quantity",dom);
			if(quantity != null && quantity.trim().length() > 0)
			{
				qty = Double.parseDouble(quantity); 
			}

			siteCode = genericUtility.getColumnValue("site_code",dom1);
			itemCode = genericUtility.getColumnValue("item_code",dom);
			lotNo = genericUtility.getColumnValue("lot_no",dom);
			custCode = genericUtility.getColumnValue("cust_code",dom1);
			lotNo = lotNo == null ? "              " : lotNo;
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				if (qty > 0)
				{
					Node currDetail = detailList.item(ctr);

					invoiceId	= genericUtility.getColumnValueFromNode("invoice_ref", currDetail);
					inputQtyStr =   genericUtility.getColumnValueFromNode("quantity", currDetail);
					inputQty = inputQtyStr == null || inputQtyStr.trim().length() == 0 ? 0 : Double.parseDouble(inputQtyStr);

					System.out.println("inputQty Transform :: " + inputQty);
					System.out.println("invoiceId Transform :: " + invoiceId);
					
					valueXmlString.append("<Detail>");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
					if(qty > inputQty)
					{
						valueXmlString.append("<quantity>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
					}
					else {
						valueXmlString.append("<quantity>").append("<![CDATA[").append(qty).append("]]>").append("</quantity>\r\n");
					}
					valueXmlString.append("<invoice_ref>").append("<![CDATA[").append(invoiceId).append("]]>").append("</invoice_ref>\r\n");
					valueXmlString.append("</Detail>");
					
					qty=qty-inputQty;

				}//if (icQtyOrd > 0)
			}
			valueXmlString.append("</Root>");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println(" retXmlString 5 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
		catch(ITMException itme)
		{
			throw itme;
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection................");
				
				if (pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				throw new ITMException(e);
			}
		}
		System.out.println("Print valueXmlString.toString() \n["+valueXmlString.toString()+"]");
		return valueXmlString.toString();
	}
	private String serializeDom(Node dom) throws Exception
	{
		String retString = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
			retString = out.toString();
			out.flush();
			out.close();
			out = null;
		}
		catch (Exception e)
		{
			System.out.println("Exception : In : serializeDom :"+e);
			e.printStackTrace();

		}
		return retString;
	}
	private String checkNullandTrim(String input) {
		if (input == null) 
		{
			input = "";
		}
		else
		{
			input=input.trim();
		}
		return input;
    }    
}