/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :09/11/2005
 */

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;
import java.text.SimpleDateFormat;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.utility.E12GenericUtility;
import ibase.utility.CommonConstants;
import ibase.system.config.*;
import java.util.Date;
import javax.ejb.Stateless; // added for ejb3
import ibase.webitm.ejb.sys.UtilMethods;

@Stateless // added for ejb3
public class PorderAct extends ActionHandlerEJB implements PorderActLocal, PorderActRemote
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

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("PorderAct called");
		Document dom = null;
		String  resString = null;

		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 
			}
			if (actionType.equalsIgnoreCase("Quotation"))
			{
				resString = actionQuotation(dom, objContext, xtraParams) ;
			}

			if (actionType.equalsIgnoreCase("Indent"))
			{
				resString = actionIndent(dom, objContext, xtraParams);
			}
			if (actionType.equalsIgnoreCase("AllItems"))
			{
				resString = actionAllItems(dom,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("TermTable"))
			{
				resString = actionTermTable(dom,objContext,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Porder :actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		System.out.println("returning from action[Method] actionHandler"+resString);
		return resString;
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			System.out.println("xmlString ::"+xmlString);
			System.out.println("xmlString1 ::"+xmlString1);
			System.out.println("selDataStr ::"+selDataStr);

			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString);				
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);				
			}
			if(selDataStr != null && selDataStr.length() > 0)
			{
				selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
			}
			System.out.println("actionType:"+actionType+":");

			if (actionType.equalsIgnoreCase("Quotation"))
			{
				retString = quotationTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("Indent"))
			{
				retString = indentTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistIssueAct :actionHandlerTransform(String xmlString):" +e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from DistIssueAct : actionHandlerTransform"+retString);
		return retString;
	}

	private String actionQuotation(Document dom, String objContext, String xtraParams) throws ITMException
	{
		String quotationNo = "";
		String errCode = "";
		String errString = "";
		String sql = "";
		ResultSet rs = null;
		String reqDate1 = ""; 
		java.sql.Date reqDate = null;
		Connection conn = null;
		Statement stmt = null;
		String indNo = "";
		char c = 32;  // Ascii charecter of empty space or whitespace
		String dummy = null;
		String varStr = " ";
		//String emptyStr = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			quotationNo = genericUtility.getColumnValue("quot_no",dom);	
			System.out.println("quotationNo  :"+quotationNo);
			if(quotationNo != null)
			{				
				sql = "SELECT PQUOT_DET.ENQ_NO, "   
						+" PQUOT_DET.IND_NO," 
						+"PQUOT_DET.ITEM_CODE,"    
						+"PQUOT_DET.QUANTITY, "   
						+"PQUOT_DET.RATE,"    
						+"PQUOT_DET.DISCOUNT,"    
						+"PQUOT_DET.UNIT,"    
						+"INDENT.REQ_DATE,"     
						+"INDENT.PACK_INSTR,"    
						+"INDENT.SPECIAL_INSTR,"    
						+"INDENT.SPECIFIC_INSTR,"    
						+"INDENT.REMARKS,ENQ_DET.REQ_DATE "   
						+" FROM PQUOT_DET LEFT OUTER JOIN INDENT ON " 
						+" PQUOT_DET.IND_NO = INDENT.IND_NO LEFT OUTER JOIN ENQ_DET ON  "  
						+"(PQUOT_DET.ENQ_NO = ENQ_DET.ENQ_NO AND PQUOT_DET.LINE_NO__ENQ = ENQ_DET.LINE_NO ) "
						+" WHERE PQUOT_DET.QUOT_NO = '"+quotationNo+"' AND PQUOT_DET.STATUS = 'A' ";							
				System.out.println("Purchase SQL :="+sql);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				while (rs.next())
				{
					reqDate = rs.getDate(13);  
					if(reqDate != null)
					{
						reqDate1 = sdf.format(reqDate);
					}

					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<enq_no>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</enq_no>\r\n");
					//valueXmlString.append("<ind_no>").append("<![CDATA[").append((rs.getString(2)== null)? "":rs.getString(2).trim()).append("]]>").append("</ind_no>\r\n");

					indNo = rs.getString(2);

					if(indNo!=null && indNo.trim().length()>0)
					{
						valueXmlString.append("<ind_no>").append("<![CDATA[" + indNo.trim() + "]]>").append("</ind_no>\r\n");
					}
					else
					{
						valueXmlString.append("<ind_no>").append("<![CDATA["+ String.valueOf(c) +"]]>").append("</ind_no>\r\n");  //  added by manazir on 2/11/2009 inplace of junk  char in popup add whitspace  String.valueOf(c)
					}					
					if(rs.getString(3) != null && rs.getString(3).trim().length()>0)
					{
						valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</item_code>\r\n");
					}
					else
					{
						valueXmlString.append("<item_code>").append("<![CDATA["+ String.valueOf(c) +"]]>").append("</item_code>\r\n");
					}
					if(rs.getString(4) != null && rs.getString(4).trim().length()>0)
					{
						//valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getString(4)).append("]]>").append("</quantity>\r\n");
						double qty =0.0;
						qty = Double.parseDouble(rs.getString(4));
						valueXmlString.append("<quantity>").append("<![CDATA[").append(utilMethods.getReqDecString((qty), 3)).append("]]>").append("</quantity>\r\n");//Changed by Anagha R on 26/10/2020 for PO with Quotation Error
					}
					else
					{
						valueXmlString.append("<quantity>").append("<![CDATA[").append("0").append("]]>").append("</quantity>\r\n");

					}		

					//valueXmlString.append("<rate>").append("<![CDATA[").append((rs.getString(5) == null) ?"0.00":rs.getString(5)).append("]]>").append("</rate>\r\n");
					valueXmlString.append("<rate>").append("<![CDATA[").append((rs.getString(5) == null) ?"0.00":utilMethods.getReqDecString(Double.parseDouble(rs.getString(5)), 4)).append("]]>").append("</rate>\r\n");//Changed by Anagha R on 26/10/2020 for PO with Quotation Error
					valueXmlString.append("<discount>").append("<![CDATA[").append((rs.getString(6) == null) ? "0.00 ":rs.getString(6)).append("]]>").append("</discount>\r\n");

					if(rs.getString(7)!=null && rs.getString(7).length() >0 )
					{
						valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(7)).append("]]>").append("</unit>\r\n");
					}
					else
					{

						valueXmlString.append("<unit><![CDATA["+   String.valueOf(c) +"]]></unit>\r\n");
					}
					if(reqDate1!=null && reqDate1.trim().length()>0)
					{
						valueXmlString.append("<req_date>").append("<![CDATA[").append(reqDate1).append("]]>").append("</req_date>\r\n");
					}
					else
					{
						valueXmlString.append("<req_date>").append("<![CDATA["+ String.valueOf(c) +"]]>").append("</req_date>\r\n");
					}

					if(rs.getString(9)!=null && rs.getString(9).length() >0 ) 
					{

						valueXmlString.append("<pack_instr>").append("<![CDATA[").append(rs.getString(9)).append("]]>").append("</pack_instr>\r\n");

					}
					else
					{
						valueXmlString.append("<pack_instr>").append("<![CDATA["+  String.valueOf(c) +"]]>").append("</pack_instr>\r\n");
					}
					if(rs.getString(10)!=null && rs.getString(10).length() >0 )
					{
						valueXmlString.append("<special_instr>").append("<![CDATA[").append(rs.getString(10)).append("]]>").append("</special_instr>\r\n");
					}
					else
					{
						valueXmlString.append("<special_instr>").append("<![CDATA["+ String.valueOf(c) +"]]>").append("</special_instr>\r\n");
					}
					if(rs.getString(11)!=null && rs.getString(11).length() >0 )
					{
						valueXmlString.append("<specific_instr>").append("<![CDATA[").append(rs.getString(11)).append("]]>").append("</specific_instr>\r\n");

					}
					else
					{
						valueXmlString.append("<specific_instr>").append("<![CDATA["+String.valueOf(c)+"]]>").append("</specific_instr>\r\n");
					}
					if(rs.getString(12)!=null && rs.getString(12).length() >0 )
					{
						valueXmlString.append("<remarks>").append("<![CDATA[").append(rs.getString(12)).append("]]>").append("</remarks>\r\n");

					}
					else
					{
						valueXmlString.append("<remarks>").append("<![CDATA["+ String.valueOf(c) +"]]>").append("</remarks>\r\n");
					}				

					valueXmlString.append("</Detail>\r\n");
				}
				stmt.close();
				valueXmlString.append("</Root>\r\n");			
			}
			else
			{
				System.out.println("Quotation found null");
				errCode = "VTQUOTNULL";
			}
			if (!errCode.equals(""))
			{
				errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				System.out.println("Errcode found not null");
				return errString;
			}
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Purchase : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Purchase : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}catch(Exception e){}
		}		
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String quotationTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		NodeList detailList = null;
		Node currentDetail = null;
		int detailListLength = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		String enqNo = "",indNo = "",itemCode = "",quantity = "",rate = "",discount = "",unit = "",reqDateStr = "",reqDateStr1 = "";
		String packInstr = "",splInstr = "",spfInstr = "",remarks = "",locCode = "",itemSer = "",empCodeQcAprv = "";
		String siteCodeDlv = "",pordType = "",acctDetrType = "",invAcct = "",sql = "",suppCode = "",siteCode = "";
		String taxChapHdr = "",taxClassHdr = "",taxEnvHdr = "",taxChap = "",taxClass = "",taxEnv = "";
		String stationFr = "",stationTo = "";
		java.sql.Date reqDate = new java.sql.Date(System.currentTimeMillis());
		ArrayList acctDetrList = new ArrayList();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			siteCodeDlv = new  ibase.utility.E12GenericUtility().getColumnValue("site_code__dlv",dom1);
			pordType = new  ibase.utility.E12GenericUtility().getColumnValue("pord_type",dom1);
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				currentDetail = detailList.item(ctr);
				enqNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("enq_no",currentDetail);
				indNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("ind_no",currentDetail);
				itemCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("item_code",currentDetail);
				quantity = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity",currentDetail);
				rate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate",currentDetail);
				discount = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("discount",currentDetail);
				unit = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit",currentDetail);
				reqDateStr1 = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("req_date",currentDetail);
				packInstr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("pack_instr",currentDetail);
				splInstr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("special_instr",currentDetail);
				spfInstr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("specific_instr",currentDetail);
				remarks = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("remarks",currentDetail);	
				// added by manazir on 2/11/2009 if in indent_no column is set to whitespace  
				/*char d= indNo.charAt(0);
				if(Character.isWhitespace(d))
				{
					indNo = "";
				}*/

				//Pavan on 13-oct-17
				if(indNo!=null && indNo.trim().length()>0)
				{
					indNo = indNo.trim();
					System.out.println("####indNo ::["+indNo+"]");
				}
				else
				{
					indNo = "";
				}
				// end of code 


				valueXmlString.append("<Detail>");
				valueXmlString.append("<ind_no isSrvCallOnChg='0'>").append((indNo == null)? "":indNo).append("</ind_no>");				
				valueXmlString.append("<item_code isSrvCallOnChg='1'>").append(itemCode).append("</item_code>");
				sql = "SELECT EMP_CODE__QCAPRV FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					empCodeQcAprv = rs.getString("EMP_CODE__QCAPRV");
					valueXmlString.append("<emp_code__qcaprv isSrvCallOnChg='0'>").append(empCodeQcAprv).append("</emp_code__qcaprv>");
				}				
				sql = "SELECT EMP_FNAME,EMP_MNAME,EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE = '"+empCodeQcAprv+"'";
				stmt.close();
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					valueXmlString.append("<emp_fname isSrvCallOnChg='0'>").append(rs.getString(1)).append("</emp_fname>");
					valueXmlString.append("<emp_mname isSrvCallOnChg='0'>").append(rs.getString(2)).append("</emp_mname>");
					valueXmlString.append("<emp_lname isSrvCallOnChg='0'>").append(rs.getString(3)).append("</emp_lname>");
				}				
				stmt.close();
				sql = "SELECT DESCR,LOC_CODE,ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					locCode = rs.getString(2);
					itemSer = rs.getString(3);
				}				
				valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append(rs.getString(1)).append("</item_descr>");
				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append(locCode).append("</loc_code>");
				//Changed by Anagha R on 26/10/2020 for PO with Quotation Error START
				//valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(quantity).append("</quantity>");
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(quantity), 3)).append("</quantity>");
				//valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append(quantity).append("</quantity__stduom>");
				valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(quantity), 3)).append("</quantity__stduom>");
				//valueXmlString.append("<rate isSrvCallOnChg='0'>").append(rate).append("</rate>");
				valueXmlString.append("<rate isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(rate), 4)).append("</rate>");
				//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append(rate).append("</rate__stduom>");
				valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(rate), 4)).append("</rate__stduom>");
				//Changed by Anagha R on 26/10/2020 for PO with Quotation Error END
				valueXmlString.append("<discount isSrvCallOnChg='0'>").append(discount).append("</discount>");
				stmt.close();
				if (reqDateStr1 == null || reqDateStr1.trim().length() == 0)
				{
					sql = "SELECT REQ_DATE FROM ENQ_DET WHERE ENQ_NO = '"+enqNo+"' AND ITEM_CODE = '"+itemCode+"'";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						reqDate = rs.getDate("REQ_DATE");
						if(reqDate != null)
						{
							reqDateStr = simpleDateFormat.format(reqDate);
						}
					}		

					stmt.close();
				}else{// add by neelam salunkhe 28/4/2012
					Date date = simpleDateFormat.parse(reqDateStr1);
					reqDateStr = simpleDateFormat.format(date);
				}
				valueXmlString.append("<req_date isSrvCallOnChg='0'>").append(reqDateStr).append("</req_date>");
				valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append(packInstr).append("</pack_instr>");
				valueXmlString.append("<special_instr isSrvCallOnChg='0'>").append(splInstr).append("</special_instr>");
				valueXmlString.append("<specific_instr isSrvCallOnChg='0'>").append(spfInstr).append("</specific_instr>");
				valueXmlString.append("<remarks isSrvCallOnChg='0'>").append(remarks).append("</remarks>");
				valueXmlString.append("<unit isSrvCallOnChg='0'>").append(unit).append("</unit>");
				valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append(unit).append("</unit__std>");
				valueXmlString.append("<unit__rate isSrvCallOnChg='0'>").append(unit).append("</unit__rate>");
				valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("1").append("</conv__qty_stduom>");
				valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("1").append("</conv__rtuom_stduom>");
				acctDetrType = acctDetrTType(itemCode,itemSer,"IN",pordType); //return acctCode and cctrCode
				acctDetrList = new  ibase.utility.E12GenericUtility().getTokenList(acctDetrType,"\t");
				valueXmlString.append("<acct_code__dr isSrvCallOnChg='0'>").append(acctDetrList.get(0)).append("</acct_code__dr>");
				valueXmlString.append("<cctr_code__dr isSrvCallOnChg='0'>").append(acctDetrList.get(1)).append("</cctr_code__dr>");
				invAcct = itmDBAccessEJB.getEnvFin("999999","INV_ACCT_PORCP",conn);
				if (invAcct != null && invAcct.trim().equalsIgnoreCase("Y"))
				{
					acctDetrType = acctDetrTType(itemCode,itemSer,"PORCP",pordType); //return acctCode and cctrCode
				}
				else
				{	
					acctDetrType = acctDetrTType(itemCode,itemSer,"PO",pordType); //return acctCode and cctrCode
				}
				acctDetrList.clear();
				acctDetrList = new  ibase.utility.E12GenericUtility().getTokenList(acctDetrType,"\t");
				valueXmlString.append("<acct_code__cr isSrvCallOnChg='0'>").append(acctDetrList.get(0)).append("</acct_code__cr>");
				valueXmlString.append("<cctr_code__cr isSrvCallOnChg='0'>").append(acctDetrList.get(1)).append("</cctr_code__cr>");

				suppCode = new  ibase.utility.E12GenericUtility().getColumnValue("supp_code",dom1);
				siteCode = new  ibase.utility.E12GenericUtility().getColumnValue("site_code",dom);

				/* //  12/10/09 manoharan commented as same is set from pb component

				taxChapHdr = new  ibase.utility.E12GenericUtility().getColumnValue("tax_chap",dom1);
				taxClassHdr = new  ibase.utility.E12GenericUtility().getColumnValue("tax_class",dom1);
				taxEnvHdr = new  ibase.utility.E12GenericUtility().getColumnValue("tax_env",dom1);

				if ( (taxClassHdr == null || taxClassHdr.trim().length() == 0 ) && (taxChapHdr == null || taxChapHdr.trim().length() == 0 ) || (taxEnvHdr == null || taxEnvHdr.trim().length() == 0 ))
				{
					taxChap = itmDBAccessEJB.getTaxChapter(itemCode,itemSer,'S',suppCode,siteCode,conn);
					taxClass = itmDBAccessEJB.getTaxClass('S',suppCode,itemCode,siteCode,conn);
					stationFr = new  ibase.utility.E12GenericUtility().getColumnValue("station_stan_code",dom1);
					sql = "SELECT STAN_CODE FROM SITE WHERE SITE_CODE = '"+siteCode+"'";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						stationTo = rs.getString(1);
					}					
					taxEnv = itmDBAccessEJB.getTaxEnv(stationFr,stationTo,taxChap,taxClass,siteCode,conn);
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append(taxChap).append("</tax_chap>");
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append(taxClass).append("</tax_class>");
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append(taxEnv).append("</tax_env>");
				}
				else
				{
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append(taxChapHdr).append("</tax_chap>");
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append(taxClassHdr).append("</tax_class>");
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append(taxEnvHdr).append("</tax_env>");
				}
				 */

				acctDetrList.clear();
				valueXmlString.append("</Detail>");
			}
			valueXmlString.append("</Root>");			
		}
		catch (Exception e)
		{
			System.out.println("Exception PorderActEJB quotationTransform :: "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection......");
				conn.close();
				conn = null;
			}
			catch (Exception se){}
		}
		return valueXmlString.toString();
	}

	private String actionIndent(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String errCode = "";
		String errString = "";
		String sql = "",sql1="";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt1=null;
		ResultSet rs1=null;
		java.sql.Date reqDate = null,indDate = null;
		String reqDate1 = "",indDate1 = "";
		int cnt = 0;
		double balance = 0,bal1 = 0,bal2 = 0, convFactor = 0, qtyStdUom = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		UtilMethods utilMethods = UtilMethods.getInstance();
		String indNo="";
		String dimension="";   //added by manish mhatre on 16-4-21
		double noArt=0;    //added by manish mhatre on 16-4-21
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			sql="SELECT INDENT.IND_NO, "   
					+"INDENT.IND_DATE, "   
					+"INDENT.DEPT_CODE, "   
					+"INDENT.REQ_DATE, "   
					+"INDENT.ITEM_CODE, "   
					+"INDENT.ITEM_DESCR, "   
					+"INDENT.QUANTITY, "   
					+"INDENT.UNIT__IND, "   
					+"INDENT.SITE_CODE, "   
					+"INDENT.WORK_ORDER, "   
					+"INDENT.PROJ_CODE, "   
					+"INDENT.ORD_QTY, "   
					+"INDENT.SITE_CODE__DLV, "   
					+"INDENT.QUANTITY__STDUOM, "   
					+"INDENT.CONV__QTY_STDUOM, "  
					+"INDENT.DIMENSION, "     //added by manish mhatre on 16-4-21
					+"INDENT.NO_ART "        //added by manish mhatre on 16-4-21
					+"FROM INDENT "  
					+"WHERE INDENT.STATUS IN ( 'A','O' ) AND  INDENT.QUANTITY > INDENT.ORD_QTY"; 
			System.out.println("Indent SQL :="+sql);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			while (rs.next())
			{
				indDate = rs.getDate(2);
				reqDate = rs.getDate(4);
				if(reqDate != null)
				{
					reqDate1 = sdf.format(reqDate);
				}
				if (indDate != null)
				{
					indDate1 = sdf.format(indDate);
				}
				bal1 = rs.getDouble(7);
				bal2 = rs.getDouble(12);
				//Added by Varsha V on 22-11-18 for calculating quantity__stduom
				convFactor = rs.getDouble(15);
				if(convFactor == 0)
				{
					convFactor = 1;
				}
				//Ended by Varsha V on 22-11-18 for calculating quantity__stduom
				System.out.println("bal1 :"+bal1+" :: bal2 ::"+bal2 +" :: convFactor :: "+convFactor);//+" bal1 - bal2 :: "+bal1-bal2+" bal2-bal1 :: "+bal2-bal1);
				balance = bal1 - bal2;
				System.out.println("balance :: "+balance);
				//Added by Varsha V on 22-11-18 for calculating quantity__stduom
				qtyStdUom = balance * convFactor;
				System.out.println("qtyStdUom :: "+qtyStdUom);
				//Ended by Varsha V on 22-11-18 for calculating quantity__stduom

				//commented by manish mhatre on 16-4-21

				/*//added by manish mhatre on 25-03-2021
				//start manish
				int lineNo=0;
				indNo=rs.getString(1).trim();
				System.out.println("ind No"+indNo);

				//int indentNoLength=indNo.length();
				System.out.println("ind No lngth"+indNo.length()); 

				String indentNo=indNo.substring(0,indNo.length()-2);
				System.out.println("indentNo"+indentNo); 

				String lineNoStr=indNo.substring(indNo.length()-2);
				System.out.println("line no str in int "+lineNoStr);

				if(lineNoStr!=null && lineNoStr.trim().length()>0)
				{
					lineNo=Integer.parseInt(lineNoStr);
					System.out.println("line no in int after parse "+lineNo);

					lineNo=lineNo+1;  //because lineNo 1 in indent_det getting as 0 from indent so added 1
					System.out.println("line no in int after addition "+lineNo);
				}

				System.out.println("indent no from indent det"+indentNo+"\n line nostr from indent det"+lineNoStr);
				System.out.println("line no in int from indent det"+lineNo);


				String dimension="";
				double noArt=0;
				sql1="select dimension,no_art From indent_det Where ind_no = ? and Line_no    =? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, indentNo);
				pstmt1.setInt(2, lineNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					dimension=rs1.getString("dimension");
					noArt=rs1.getDouble("no_art");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);

				//end manish*/

				valueXmlString.append("<Detail>\r\n");
				//valueXmlString.append("<ind_no>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</ind_no>\r\n");  //commented by manish mhatre on 25-03-2021
				//valueXmlString.append("<ind_no>").append("<![CDATA[").append(indNo).append("]]>").append("</ind_no>\r\n");    //added by manish mhatre on 25-03-2021
				valueXmlString.append("<ind_no>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</ind_no>\r\n");  //added by manish mhatre on 16-4-21
				valueXmlString.append("<ind_date>").append("<![CDATA[").append(indDate1).append("]]>").append("</ind_date>\r\n");
				valueXmlString.append("<req_date>").append("<![CDATA[").append(reqDate1).append("]]>").append("</req_date>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<item_descr>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</item_descr>\r\n");
				valueXmlString.append("<balance_qty>").append("<![CDATA[").append(balance).append("]]>").append("</balance_qty>\r\n");
				valueXmlString.append("<unit__ind>").append("<![CDATA[").append(rs.getString(8)).append("]]>").append("</unit__ind>\r\n");
				valueXmlString.append("<dept_code>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</dept_code>\r\n");
				//Changes by mayur on 19-June-2018--[start]
				//valueXmlString.append("<site_code>").append("<![CDATA[").append(rs.getString(9)).append("]]>").append("</site_code>\r\n");
				valueXmlString.append("<site_code>").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</site_code>\r\n");
				//Changes by mayur on 19-June-2018--[end]
				valueXmlString.append("<work_order>").append("<![CDATA[").append((rs.getString(10) == null) ? "":rs.getString(10)).append("]]>").append("</work_order>\r\n");
				valueXmlString.append("<proj_code>").append("<![CDATA[").append(rs.getString(11)).append("]]>").append("</proj_code>\r\n");
				//commented by mayur on 19-June-2018---[start]
				//valueXmlString.append("<site_code__dlv>").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</site_code__dlv>\r\n");
				//commented by mayur on 19-June-2018---[end]
				//Commented and added by Varsha V on 22-10-18 for setting correct quantity standard
				//valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(rs.getString(14)).append("]]>").append("</quantity__stduom>\r\n");
				//valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(qtyStdUom).append("]]>").append("</quantity__stduom>\r\n");
				valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(utilMethods.getReqDecString(qtyStdUom, 3)).append("]]>").append("</quantity__stduom>\r\n");//Changed by Anagha R on 26/10/2020 for PO with Quotation Error
				//Commented and ended by Varsha V on 22-10-18 for setting correct quantity standard
				//valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(rs.getString(15)).append("]]>").append("</conv__qty_stduom>\r\n");
				valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(utilMethods.getReqDecString(Double.parseDouble(rs.getString(15)), 7)).append("]]>").append("</conv__qty_stduom>\r\n");//Changed by Anagha R on 26/10/2020 for PO with Quotation Error
				//valueXmlString.append("<ord_qty>").append("<![CDATA[").append(rs.getString(12)).append("]]>").append("</ord_qty>\r\n");

				//commented by manish mhatre on 16-4-21
				/*//added by manish mhatre on 25-03-2021
				//start manish
				//valueXmlString.append("<quantity>").append("<![CDATA[").append(balance).append("]]>").append("</quantity>\r\n");    //added by manish mhatre on 25-03-2021
				if(dimension!=null && dimension.trim().length()>0)
				{
					valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");    //added by manish mhatre on 25-03-2021
				}
				if(noArt!=0)
				{
					valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");    //added by manish mhatre on 25-03-2021
				}
                //end manish*/

				//added by manish mhatre on 16-4-21
				//start manish 
				dimension=rs.getString(16);
				noArt=rs.getDouble(17);
				System.out.println("dimesnion>>>>"+dimension+"\nno_art>>>>>"+noArt);
				if(dimension!=null && dimension.trim().length()>0)
				{
					valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
				}
				if(noArt!=0)
				{
					valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
				}

				//end manish

				valueXmlString.append("</Detail>\r\n");
				indDate1 = "";
				reqDate1 = "";
				cnt++; 
			}
			stmt.close();
			System.out.println("cnt :"+cnt);
			valueXmlString.append("</Root>\r\n");			
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Porder : actionIndent " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Porder : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String indentTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		NodeList detailList = null;
		Node currentDetail = null;
		int detailListLength = 0,cnt = 0,detCnt = 0;
		String indNo = "",itemCode = "",siteCode = "",reqDate = "",reqDate1 = "",balanceQty = "",unitInd = "",workOrder = "";
		String convQtyStdUom = "",qtyStdUom = "",purcOrder = "",itemSer = "",pordType = "",sql = "";
		String specialInstr = "",specificInstr = "",remarks = "",empCodeQcAprv = "",descr = "",locCode = "";
		String acctDetrType = "",invAcct = "",unit = "",suppCode = "",prate = "",acctCode = "",cctrCode = "";
		String taxClassHdr = "",taxChapHdr = "",taxEnvHdr = "",taxChap = "",taxClass = "",taxEnv = "";
		String stationFr = "",stationTo = "";
		ArrayList acctDetrList = new ArrayList();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//added by Varsha V on 24-05-18
		DistCommon distComm = new DistCommon();
		UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			for (int ctr=0;ctr < detailListLength;ctr++ )
			{
				currentDetail = detailList.item(ctr);
				indNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("ind_no",currentDetail);
				itemCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("item_code",currentDetail);
				siteCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("site_code",currentDetail);
				reqDate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("req_date",currentDetail);
				balanceQty = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("balance_qty",currentDetail);
				unitInd = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit__ind",currentDetail);
				workOrder = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("work_order",currentDetail);
				convQtyStdUom = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("conv__qty_stduom",currentDetail);
				qtyStdUom = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity__stduom",currentDetail);
				purcOrder = new  ibase.utility.E12GenericUtility().getColumnValue("purc_order",dom1);
				itemSer = new  ibase.utility.E12GenericUtility().getColumnValue("item_ser",dom1);
				pordType = new  ibase.utility.E12GenericUtility().getColumnValue("pord_type",dom1);
				sql = "SELECT COUNT(*) FROM PORDDET WHERE PURC_ORDER = '"+purcOrder+"' AND IND_NO = '"+indNo+"'";
				System.out.println("SQL ::"+sql);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					cnt = rs.getInt(1);
				}
				stmt.close();
				if (cnt == 0 || detCnt == 0)
				{
					valueXmlString.append("<Detail>");
					sql = "SELECT SPECIAL_INSTR,SPECIFIC_INSTR,REMARKS,EMP_CODE__QCAPRV FROM INDENT WHERE IND_NO = '"+indNo+"'";
					System.out.println("SQL ::"+sql);
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						specialInstr = rs.getString("SPECIAL_INSTR");
						specificInstr = rs.getString("SPECIFIC_INSTR");
						remarks = rs.getString("REMARKS");
						empCodeQcAprv = rs.getString("EMP_CODE__QCAPRV");
					}
					valueXmlString.append("<ind_no isSrvCallOnChg='0'>").append(indNo).append("</ind_no>");
					valueXmlString.append("<remarks isSrvCallOnChg='0'>").append(remarks).append("</remarks>");
					valueXmlString.append("<special_instr isSrvCallOnChg='0'>").append(specialInstr).append("</special_instr>");
					valueXmlString.append("<specific_instr isSrvCallOnChg='0'>").append(specificInstr).append("</specific_instr>");
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append(itemCode).append("</item_code>");
					stmt.close();
					sql = "SELECT DESCR,LOC_CODE,UNIT FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("SQL ::"+sql);
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						descr = rs.getString("DESCR");
						locCode = rs.getString("LOC_CODE");
						unit = rs.getString("UNIT");
					}
					suppCode = new  ibase.utility.E12GenericUtility().getColumnValue("supp_code",dom1);
					stmt.close();
					stmt = conn.createStatement();
					sql = "SELECT RATE__REF FROM SUPPLIERITEM WHERE SUPP_CODE = '"+suppCode+"' AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("SQL ::"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						prate = rs.getString("RATE__REF");
					}
					//Added by sarita on 14NOV2017
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					if (prate == null || prate.trim().length() == 0)
					{
						prate = "0";
					}		
					if(reqDate != null){// add by neelam salunkhe 28/4/2012
						Date date = simpleDateFormat.parse(reqDate);
						reqDate1=simpleDateFormat.format(date);
					}
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append(descr).append("</item_descr>");
					//Changed by Anagha R on 26/10/2020 for PO with Quotation Error START
					//valueXmlString.append("<rate isSrvCallOnChg='0'>").append(prate).append("</rate>");
					valueXmlString.append("<rate isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(prate), 4)).append("</rate>");
					//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append(prate).append("</rate__stduom>");
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(prate), 4)).append("</rate__stduom>");
					//Changed by Anagha R on 26/10/2020 for PO with Quotation Error END
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append(locCode).append("</loc_code>");
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append(unit).append("</unit__std>");
					valueXmlString.append("<unit__rate isSrvCallOnChg='0'>").append(unit).append("</unit__rate>");
					//Changed by Anagha R on 26/10/2020 for PO with Quotation Error START
					//valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append(convQtyStdUom).append("</conv__qty_stduom>");
					valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(convQtyStdUom), 7)).append("</conv__qty_stduom>");
					//valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append(convQtyStdUom).append("</conv__rtuom_stduom>");
					valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(convQtyStdUom), 7)).append("</conv__rtuom_stduom>");
					//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append(convQtyStdUom).append("</rate__stduom>");
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(convQtyStdUom), 4)).append("</rate__stduom>");
					//Changed by Anagha R on 26/10/2020 for PO with Quotation Error END
					valueXmlString.append("<site_code isSrvCallOnChg='0'>").append(siteCode).append("</site_code>");
					valueXmlString.append("<req_date isSrvCallOnChg='0'>").append(reqDate1).append("</req_date>");	//To be Checked later
					valueXmlString.append("<dlv_date isSrvCallOnChg='0'>").append(reqDate1).append("</dlv_date>");	//To be Checked later
					//Changed by Anagha R on 26/10/2020 for PO with Quotation Error START
					//valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(balanceQty).append("</quantity>");
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(balanceQty), 3)).append("</quantity>");
					//valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append(qtyStdUom).append("</quantity__stduom>");
					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append(utilMethods.getReqDecString(Double.parseDouble(qtyStdUom), 3)).append("</quantity__stduom>");
					//Changed by Anagha R on 26/10/2020 for PO with Quotation Error END
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append(unitInd).append("</unit>");
					valueXmlString.append("<work_order isSrvCallOnChg='0'>").append(workOrder).append("</work_order>");
					valueXmlString.append("<emp_code__qcaprv isSrvCallOnChg='0'>").append(empCodeQcAprv).append("</emp_code__qcaprv>");
					//commented by sarita on 14NOV2017
					//stmt.close();
					stmt = conn.createStatement();
					sql = "SELECT ACCT_CODE,CCTR_CODE FROM INDENT WHERE IND_NO = '"+indNo+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString("ACCT_CODE");
						cctrCode = rs.getString("CCTR_CODE");
					}
					//Added by sarita on 14NOV2017
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					if ((acctCode == null || acctCode.trim().length() == 0) || (cctrCode == null || cctrCode.trim().length() == 0))
					{
						acctDetrType = acctDetrTType(itemCode,itemSer,"IN",pordType); //return acctCode and cctrCode
						acctDetrList = new  ibase.utility.E12GenericUtility().getTokenList(acctDetrType,"\t");
						acctCode = acctDetrList.get(0).toString();
						cctrCode = acctDetrList.get(1).toString();
						acctDetrList.clear();
					}
					invAcct = itmDBAccess.getEnvFin("999999","INV_ACCT_PORCP",conn); 
					System.out.println("invAcct :: "+invAcct);
					if (invAcct != null && invAcct.trim().equalsIgnoreCase("Y"))
					{
						System.out.println("PORCP");
						acctDetrType = acctDetrTType(itemCode,itemSer,"PORCP",pordType); //return acctCode and cctrCode										
					}
					else
					{
						System.out.println("PO");
						acctDetrType = acctDetrTType(itemCode,itemSer,"PO",pordType); //return acctCode and cctrCode	
					}
					System.out.println("acctDetrType :: "+acctDetrType);
					acctDetrList = new  ibase.utility.E12GenericUtility().getTokenList(acctDetrType,"\t");
					valueXmlString.append("<acct_code__dr isSrvCallOnChg='0'>").append(acctCode).append("</acct_code__dr>");
					valueXmlString.append("<cctr_code__dr isSrvCallOnChg='0'>").append(cctrCode).append("</cctr_code__dr>");
					valueXmlString.append("<acct_code__cr isSrvCallOnChg='0'>").append(acctDetrList.get(0)).append("</acct_code__cr>");
					valueXmlString.append("<cctr_code__cr isSrvCallOnChg='0'>").append(acctDetrList.get(1)).append("</cctr_code__cr>");

					//Added by Varsha V on 24-05-18 to set tax_class , tax_chapter and tax_env
					taxClassHdr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_class",dom1);
					taxChapHdr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_chap",dom1);
					taxEnvHdr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_env",dom1);
					//tax_class
					if ((taxClassHdr == null || taxClassHdr.trim().length() == 0))
					{
						taxClass = distComm.getTaxClass("S",suppCode,itemCode,siteCode,conn);
						valueXmlString.append("<tax_class>").append((taxClass == null) ? "":taxClass).append("</tax_class>");
					}
					else
					{
						valueXmlString.append("<tax_class>").append(taxClassHdr).append("</tax_class>");
					}
					//tax_chap
					taxChap = distComm.getTaxChap(itemCode,itemSer,"S",suppCode,siteCode,conn);
					valueXmlString.append("<tax_chap>").append((taxChap == null) ? "":taxChap).append("</tax_chap>");
					//tax_env
					if ((taxEnvHdr == null || taxEnvHdr.trim().length() == 0))
					{
						stationFr = new  ibase.utility.E12GenericUtility().getColumnValue("station_stan_code",dom1);
						sql = "SELECT STAN_CODE FROM SITE WHERE SITE_CODE = '"+siteCode+"'";
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							stationTo = rs.getString(1); System.out.println("stationTo :["+stationTo+"]");
						}
						rs.close();
						rs = null;
						stmt.close();
						stmt = null;

						taxEnv = distComm.getTaxEnv(stationFr,stationTo,taxChap,taxClass,siteCode,conn);
						valueXmlString.append("<tax_env>").append((taxEnv == null) ? "":taxEnv).append("</tax_env>");
					}
					else
					{
						valueXmlString.append("<tax_env>").append(taxEnvHdr).append("</tax_env>");
					}
					//Ended by Varsha V on 24-05-18 to set tax_class , tax_chapter and tax_env

					/* //  27/04/2012 Neelam salulnkhe commented as same is set from pb component
					taxClassHdr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_class",dom1);
					taxChapHdr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_chap",dom1);
					taxEnvHdr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_env",dom1);

					if ((taxClassHdr == null || taxClassHdr.trim().length() == 0) && (taxChapHdr == null || taxChapHdr.trim().length() == 0) && (taxEnvHdr == null || taxEnvHdr.trim().length() == 0))
					{
						taxChap = itmDBAccess.getTaxChapter(itemCode,itemSer,'S',suppCode,siteCode,conn);		
						taxClass = itmDBAccess.getTaxClass('S',suppCode,itemCode,siteCode,conn);
						stationFr = new  ibase.utility.E12GenericUtility().getColumnValue("station_stan_code",dom1);
						sql = "SELECT STAN_CODE FROM SITE WHERE SITE_CODE = '"+siteCode+"'";
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							stationTo = rs.getString(1);
						}					
						taxEnv = itmDBAccess.getTaxEnv(stationFr,stationTo,taxChap,taxClass,siteCode,conn);
						valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append((taxChap == null) ? "":taxChap).append("</tax_chap>");
						valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append((taxClass == null) ? "":taxClass).append("</tax_class>");
						valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append((taxEnv == null) ? "":taxEnv).append("</tax_env>");
					}
					else
					{
						valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append(taxChapHdr).append("</tax_chap>");
						valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append(taxClassHdr).append("</tax_class>");
						valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append(taxEnvHdr).append("</tax_env>");
					}	*/
					valueXmlString.append("</Detail>");
				}
			}
			valueXmlString.append("</Root>");	
		}
		catch (Exception e)
		{
			System.out.println("Exception in PorderAct :: indentTransform ::"+e);
			e.printStackTrace();
		}
		//Added by sarita on 14NOV2017 to close connection on 14NOV2017[start]
		finally
		{
			try
			{
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				if(rs != null) 
				{
					rs.close();
					rs = null;
				}
				if(stmt != null) 
				{
					stmt.close();
					stmt = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		//Added by sarita on 14NOV2017 to close connection on 14NOV2017[end]

		return valueXmlString.toString();
	}

	private String actionAllItems(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "",errCode = "",errString = "", quotNo = "", salesOrder = "",empCodeQcaprv = "",empFname ="" ;
		String empMname = "",empLname = "",itemDescr = "", locCode = "",itemSer = "",acctDr = "",cctrDr = "";
		String acctCr = "", cctrCr = "";
		java.sql.Date dspDate = null;
		int count = 0;
		int rnt = 0;
		//int det = 0;
		String saleLine = "",saleItem = "",saleUnit ="";
		double saleQty = 0.0 ;

		String enqNo = "",indNo ="",itemCode ="";
		double quantity =0.0;
		double rate = 0.0 ;
		double discount = 0.0 ;
		String unit ="";
		java.sql.Date reqDate = null ;
		String packInstr="";
		String specialInstr ="" ;
		String specificInstr="" ;
		String remarks = "" ;
		String stkReqDate = "" ;
		String stkDspDate = "" ;

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//CommonConstants commonConstants = new CommonConstants();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		UtilMethods utilMethods = UtilMethods.getInstance();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//commonConstants.setIBASEHOME();
			//System.out.println("commonConstants.DB_NAME:"+commonConstants.DB_NAME+":");

			quotNo = genericUtility.getColumnValue("quot_no",dom);
			salesOrder = genericUtility.getColumnValue("sale_order",dom);

			System.out.println("Porder:From DOM :salesOrder:"+salesOrder+":quotNo:"+quotNo+":");
			if((salesOrder != null && salesOrder.trim().length() > 0) && (quotNo == null || quotNo.trim().length() == 0))
			{
				System.out.println(" \n********I N S I D E 1ST IF PART ******** ");
				sql = "SELECT COUNT(1) FROM SORDITEM WHERE SALE_ORDER ='"+salesOrder+"'";

				System.out.println("Porder:actionAllItems:sql:"+sql);
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				sql="SELECT LINE_NO,ITEM_CODE,QUANTITY,UNIT FROM SORDITEM "+
						" WHERE SALE_ORDER ='"+salesOrder+"'";
				System.out.println("Porder:actionAllItems:<SORDITEM>:sql:"+sql);
				//if get_sqlcode() <> 0 then
				if(count > 0)
				{
					rs1 = stmt1.executeQuery(sql);
					while(rs1.next())
					{
						saleLine = rs1.getString(1);
						saleItem=rs1.getString(2);
						saleQty=rs1.getDouble(3);
						saleUnit=rs1.getString(4);

						valueXmlString.append("<Detail>\r\n");

						valueXmlString.append("<item_code>").append("<![CDATA[").append((saleItem == null) ? "" :saleItem).append("]]>").append("</item_code>\r\n");

						sql="SELECT EMP_CODE__QCAPRV FROM ITEM WHERE ITEM_CODE ='"+saleItem+"'";
						System.out.println("Porder:actionAllItems:<ITEM>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							empCodeQcaprv = rs.getString(1);
						}
						valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[").append((empCodeQcaprv == null)? "":empCodeQcaprv).append("]]>").append("</emp_code__qcaprv>\r\n");

						sql="SELECT EMP_FNAME,EMP_MNAME,EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE ='"+empCodeQcaprv+"'";
						System.out.println("Porder:actionAllItems:<EMPLOYEE>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							empFname = rs.getString(1);
							empMname = rs.getString(2);
							empLname = rs.getString(3);
						}
						valueXmlString.append("<emp_fname>").append("<![CDATA[").append((empFname == null) ? "":empFname).append("]]>").append("</emp_fname>\r\n");
						valueXmlString.append("<emp_mname>").append("<![CDATA[").append((empMname == null)? "":empMname).append("]]>").append("</emp_mname>\r\n");
						valueXmlString.append("<emp_lname>").append("<![CDATA[").append((empLname == null)? "":empLname).append("]]>").append("</emp_lname>\r\n");

						valueXmlString.append("<line_no__sord>").append("<![CDATA[").append((saleLine == null)? "":saleLine).append("]]>").append("</line_no__sord>\r\n");

						sql="SELECT DESCR,LOC_CODE,ITEM_SER FROM ITEM WHERE ITEM_CODE ='"+saleItem+"'";
						System.out.println("Porder:actionAllItems:<ITEM>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							itemDescr = rs.getString(1);
							locCode = rs.getString(2);
							itemSer = rs.getString(3);
						}

						sql="SELECT ACCT_CODE__IN,CCTR_CODE__IN,ACCT_CODE__AP,CCTR_CODE__AP "+
								"FROM ITEMSER WHERE ITEM_SER ='"+itemSer+"'";
						System.out.println("Porder:actionAllItems:<ITEMSER>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							acctDr = rs.getString(1);
							cctrDr = rs.getString(2);
							acctCr = rs.getString(3);
							cctrCr = rs.getString(4);
						}

						sql="SELECT DSP_DATE FROM SORDDET "+
								" WHERE SALE_ORDER ='"+salesOrder+"'" +
								" AND LINE_NO ='"+saleLine+"'";
						System.out.println("Porder:actionAllItems:<SORDDET>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							dspDate = rs.getDate(1);
						}
						System.out.println("Porder:actionAllItems:dspDate:"+dspDate+":");
						if(dspDate != null)
						{
							stkDspDate = sdf.format(dspDate);
						}
						System.out.println("Porder:actionAllItems:stkDspDate:"+stkDspDate+":");
						valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
						valueXmlString.append("<req_date>").append("<![CDATA[").append(stkDspDate).append("]]>").append("</req_date>\r\n");
						valueXmlString.append("<dlv_date>").append("<![CDATA[").append(stkDspDate).append("]]>").append("</dlv_date>\r\n");
						valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
						//Changed by Anagha R on 26/10/2020 for PO with Quotation Error START
						//valueXmlString.append("<quantity>").append("<![CDATA[").append(saleQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity>").append("<![CDATA[").append(utilMethods.getReqDecString(saleQty, 3)).append("]]>").append("</quantity>\r\n");
						//valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(saleQty).append("]]>").append("</quantity__stduom>\r\n");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(utilMethods.getReqDecString(saleQty, 3)).append("]]>").append("</quantity__stduom>\r\n");
						//Changed by Anagha R on 26/10/2020 for PO with Quotation Error END
						valueXmlString.append("<acct_code__dr>").append("<![CDATA[").append(acctDr).append("]]>").append("</acct_code__dr>\r\n");
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA[").append(cctrDr).append("]]>").append("</cctr_code__dr>\r\n");

						valueXmlString.append("<unit>").append("<![CDATA[").append((saleUnit == null) ? "":saleUnit).append("]]>").append("</unit>\r\n");
						valueXmlString.append("<unit__std>").append("<![CDATA[").append((saleUnit == null) ? "":saleUnit).append("]]>").append("</unit__std>\r\n");
						valueXmlString.append("<unit__rate>").append("<![CDATA[").append((saleUnit == null) ? "":saleUnit).append("]]>").append("</unit__rate>\r\n");
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(1).append("]]>").append("</conv__qty_stduom>\r\n");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[").append(1).append("]]>").append("</conv__rtuom_stduom>\r\n");

						valueXmlString.append("</Detail>\r\n");
					}//end For Loop
				}
			}//end if
			else if((salesOrder == null || salesOrder.trim().length() == 0) && (quotNo != null && quotNo.trim().length() > 0))
			{
				System.out.println(" \n********I N S I D E 1ST ELSE IF PART ******** ");
				sql = "SELECT COUNT(*) FROM PQUOT_DET WHERE QUOT_NO ='"+quotNo+"'  AND STATUS = 'A'";
				System.out.println("Porder:actionAllItems:<PQUOT_DET>:sql:"+sql);
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				System.out.println("Porder:actionAllItems:count:"+count+":");
				//if(commonConstants.DB_NAME.equalsIgnoreCase("db2")) // By Piyush - 12/01/2006
				if(CommonConstants.DB_NAME.equalsIgnoreCase("db2")) // Changed by Piyush - 12/01/2006
				{
					sql= " SELECT PQUOT_DET.ENQ_NO,PQUOT_DET.IND_NO, PQUOT_DET.ITEM_CODE,"+
							" PQUOT_DET.QUANTITY,PQUOT_DET.RATE, PQUOT_DET.DISCOUNT, "+
							" PQUOT_DET.UNIT,	INDENT.REQ_DATE, INDENT.PACK_INSTR, "+ 
							" INDENT.SPECIAL_INSTR, INDENT.SPECIFIC_INSTR, INDENT.REMARKS "+
							" FROM {OJ PQUOT_DET LEFT OUTER JOIN INDENT ON  PQUOT_DET.IND_NO = INDENT.IND_NO} "+
							" WHERE PQUOT_DET.QUOT_NO ="+"'"+quotNo + "'"+
							" AND PQUOT_DET.STATUS = 'A'" ;
				}
				else
				{
					sql =" SELECT PQUOT_DET.ENQ_NO,PQUOT_DET.IND_NO, PQUOT_DET.ITEM_CODE,"+
							" PQUOT_DET.QUANTITY,PQUOT_DET.RATE, PQUOT_DET.DISCOUNT, "+
							" PQUOT_DET.UNIT,	INDENT.REQ_DATE, INDENT.PACK_INSTR, "+ 
							" INDENT.SPECIAL_INSTR, INDENT.SPECIFIC_INSTR, INDENT.REMARKS "+
							" FROM PQUOT_DET , INDENT "+
							" WHERE PQUOT_DET.IND_NO = INDENT.IND_NO (+) AND " +
							" PQUOT_DET.QUOT_NO ="+"'"+quotNo+"' "+
							" AND PQUOT_DET.STATUS = 'A'" ;
				}
				System.out.println("Porder:actionAllItems:<PQUOT_DET,INDENT>:sql:"+sql);
				//if get_sqlcode() <> 0 then
				if(count > 0)
				{
					rs1 = stmt1.executeQuery(sql);
					while(rs1.next())
					{
						enqNo=rs1.getString(1);
						indNo=rs1.getString(2);
						itemCode=rs1.getString(3);
						quantity=rs1.getDouble(4);
						rate=rs1.getDouble(5);
						discount=rs1.getDouble(6);
						unit=rs1.getString(7);
						reqDate=rs1.getDate(8);
						packInstr=rs1.getString(9);
						specialInstr=rs1.getString(10);
						specificInstr=rs1.getString(11);
						remarks=rs1.getString(12);
						System.out.println("Porder:actionAllItems:reqDate:"+reqDate+":");
						System.out.println("Porder:actionAllItems:indNo:"+indNo+":");
						if(reqDate != null)
						{
							stkReqDate = sdf.format(reqDate);
						}
						System.out.println("Porder:actionAllItems:stkReqDate:"+stkReqDate+":");
						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<item_code isSrvCallOnChg='1'>").append("<![CDATA[").append((itemCode == null)? "":itemCode).append("]]>").append("</item_code>\r\n");

						sql="SELECT EMP_CODE__QCAPRV FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
						System.out.println("Porder:actionAllItems:<ITEM>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							empCodeQcaprv = rs.getString(1);
						}
						valueXmlString.append("<emp_code__qcaprv>").append("<![CDATA[").append((empCodeQcaprv == null)? "":empCodeQcaprv).append("]]>").append("</emp_code__qcaprv>\r\n");

						sql="SELECT EMP_FNAME,EMP_MNAME,EMP_LNAME FROM EMPLOYEE WHERE EMP_CODE='"+empCodeQcaprv+"'";
						System.out.println("Porder:actionAllItems:<EMPLOYEE>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							empFname = rs.getString(1);
							empMname = rs.getString(2);
							empLname = rs.getString(3);
						}
						valueXmlString.append("<emp_fname>").append("<![CDATA[").append((empFname == null)? "":empFname).append("]]>").append("</emp_fname>\r\n");
						valueXmlString.append("<emp_mname>").append("<![CDATA[").append((empMname == null)? "":empMname).append("]]>").append("</emp_mname>\r\n");
						valueXmlString.append("<emp_lname>").append("<![CDATA[").append((empLname == null)? "":empLname).append("]]>").append("</emp_lname>\r\n");

						sql="SELECT DESCR,LOC_CODE,ITEM_SER FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
						System.out.println("Porder:actionAllItems:<ITEM>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							itemDescr = rs.getString(1);
							locCode = rs.getString(2);
							itemSer = rs.getString(3);
						}
						System.out.println("Porder:actionAllItems:locCode:"+locCode+":");
						sql="SELECT ACCT_CODE__IN,CCTR_CODE__IN,ACCT_CODE__AP,CCTR_CODE__AP "+
								" FROM ITEMSER WHERE ITEM_SER ='"+itemSer+"'";	
						System.out.println("Porder:actionAllItems:<ITEMSER>:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							acctDr = rs.getString(1);
							cctrDr = rs.getString(2);
							acctCr = rs.getString(3);
							cctrCr = rs.getString(4);
						}
						valueXmlString.append("<item_descr>").append("<![CDATA[").append((itemDescr == null)? "":itemDescr).append("]]>").append("</item_descr>\r\n");
						valueXmlString.append("<loc_code>").append("<![CDATA[").append((locCode == null)? "":locCode).append("]]>").append("</loc_code>\r\n");

						//Changed by Anagha R on 26/10/2020 for PO with Quotation Error START
						//valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity>").append("<![CDATA[").append(utilMethods.getReqDecString(quantity, 3)).append("]]>").append("</quantity>\r\n");
						//valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity__stduom>\r\n");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(utilMethods.getReqDecString(quantity, 3)).append("]]>").append("</quantity__stduom>\r\n");
						//valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
						valueXmlString.append("<rate>").append("<![CDATA[").append(utilMethods.getReqDecString(rate, 4)).append("]]>").append("</rate>\r\n");
						//valueXmlString.append("<rate__stduom>").append("<![CDATA[").append(rate).append("]]>").append("</rate__stduom>\r\n");
						valueXmlString.append("<rate__stduom>").append("<![CDATA[").append(utilMethods.getReqDecString(rate, 4)).append("]]>").append("</rate__stduom>\r\n");
						//Changed by Anagha R on 26/10/2020 for PO with Quotation Error END
						valueXmlString.append("<discount>").append("<![CDATA[").append(discount).append("]]>").append("</discount>\r\n");

						valueXmlString.append("<acct_code__dr>").append("<![CDATA[").append((acctDr == null) ? "":acctDr).append("]]>").append("</acct_code__dr>\r\n");
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA[").append((cctrDr == null)? "":cctrDr).append("]]>").append("</cctr_code__dr>\r\n");

						valueXmlString.append("<ind_no>").append("<![CDATA[").append((indNo == null)? "":indNo).append("]]>").append("</ind_no>\r\n");				
						valueXmlString.append("<req_date>").append("<![CDATA[").append((stkReqDate == null)? "":stkReqDate).append("]]>").append("</req_date>\r\n");				
						valueXmlString.append("<pack_instr>").append("<![CDATA[").append((packInstr == null)? "":packInstr).append("]]>").append("</pack_instr>\r\n");				
						valueXmlString.append("<special_instr>").append("<![CDATA[").append((specialInstr == null)? "":specialInstr).append("]]>").append("</special_instr>\r\n");				
						valueXmlString.append("<specific_instr>").append("<![CDATA[").append((specificInstr == null)? "":specificInstr).append("]]>").append("</specific_instr>\r\n");				
						valueXmlString.append("<remarks>").append("<![CDATA[").append((remarks == null)? "":remarks).append("]]>").append("</remarks>\r\n");				

						valueXmlString.append("<unit>").append("<![CDATA[").append((unit == null)? "":unit).append("]]>").append("</unit>\r\n");
						valueXmlString.append("<unit__std>").append("<![CDATA[").append((unit == null)? "":unit).append("]]>").append("</unit__std>\r\n");
						valueXmlString.append("<unit__rate>").append("<![CDATA[").append((unit == null)? "":unit).append("]]>").append("</unit__rate>\r\n");
						valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(1).append("]]>").append("</conv__qty_stduom>\r\n");
						valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[").append(1).append("]]>").append("</conv__rtuom_stduom>\r\n");
						valueXmlString.append("</Detail>\r\n");			
					} //for loop end
				}
			}//end Else
			else if((quotNo == null || quotNo.trim().length() == 0) && (salesOrder == null || salesOrder.trim().length()== 0))
			{
				System.out.println(" \n********I N S I D E 2ND ELSE IF PART ******** ");
				errCode = "VTNOSAQT";
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Porder : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Porder : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String actionTermTable(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String errCode = "";
		String errString = "";
		String termTable = "";
		String termCode = "";
		int count = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");

		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(); 
			termTable = genericUtility.getColumnValue("term_table",dom);
			System.out.println("actionTermTable :value From DOM :termTable :"+termTable);
			if(termTable == null || termTable.trim().length()== 0)
			{
				sql="SELECT VAR_VALUE FROM DISPARM WHERE VAR_NAME ='DEF_PTERM_PO' ";
				System.out.println("actionTermTable :<DISPARM>sql:"+sql);
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					termTable = rs.getString(1);
				}
			}
			System.out.println("actionTermTable :value From Query:termTable :"+termTable);
			sql="SELECT COUNT(*) FROM PUR_TERM_TABLE WHERE TERM_TABLE ='"+termTable+"'";
			System.out.println("actionTermTable:<PUR_TERM_TABLE>sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				count =rs.getInt(1);
			}
			// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [Start]			
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [End]
			System.out.println(" ***** count *****:"+count+":");
			sql=" SELECT TERM_CODE FROM PUR_TERM_TABLE WHERE TERM_TABLE ='"+termTable+"'";
			System.out.println("actionTermTable:Fetching Records <PUR_TERM_TABLE>sql:"+sql);
			if(count > 0)
			{
				rs = stmt.executeQuery(sql);
				while(rs.next())
				{
					termCode = rs.getString(1);
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<term_code>").append("<![CDATA[").append((termCode == null) ? "":termCode).append("]]>").append("</term_code>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [Start]
				if ( stmt != null )
				{
					stmt.close();
					stmt = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [End]
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Porder : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Porder : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();	
	}

	private String acctDetrTType(String itemCode, String itemSer, String purpose, String tranType)throws Exception
	{
		System.out.println("acctDetrTType Calling................");
		System.out.println("The values of parameters are :\n itemCode :"+itemCode+" \n itemSer :"+itemSer+" \n purpose :"+purpose+" \n tranType :"+tranType);
		String sql = "", stkOption = "", acctCode = "", cctrCode = "", itemSer1 = "", retStr = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			if (purpose.equals("IN"))
			{ 
				sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					stkOption = rs.getString(1);
					System.out.println("stkOption :"+stkOption);
				}
				if (stkOption.equals("0"))
				{
					sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
							+"WHERE ITEM_CODE = '"+itemCode+"' "
							+"AND ITEM_SER = '"+itemSer+"' "
							+"AND TRAN_TYPE = '"+tranType+"'";
					System.out.println("sql from if part :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString(1);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(2);
						System.out.println("cctrCode :"+cctrCode);
					}
					if (acctCode == null || acctCode.equals(""))
					{
						sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
								+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
								+"AND TRAN_TYPE = '"+tranType+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString(1);
							System.out.println("acctCode :"+acctCode);
							cctrCode = rs.getString(2);
							System.out.println("cctrCode :"+cctrCode);
						}
						if (acctCode == null || acctCode.equals(""))
						{
							sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
									+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
									+"AND TRAN_TYPE = ' '";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString(1);
								System.out.println("acctCode :"+acctCode);
								cctrCode = rs.getString(2);
								System.out.println("cctrCode :"+cctrCode);
							}
							if (acctCode == null || acctCode.equals(""))
							{
								if (itemSer == null && itemSer.trim().length() == 0)
								{
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										itemSer1 = rs.getString(1);
										System.out.println("itemSer1 :"+itemSer1);
									}
								}
								else
								{
									itemSer1 = itemSer;
									System.out.println("itemSer1 :"+itemSer1);
								}
								sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
										+"WHERE ITEM_SER = '"+itemSer1+"' "
										+"AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
								System.out.println("sql :"+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString(1);
									System.out.println("acctCode :"+acctCode);
									cctrCode = rs.getString(2);
									System.out.println("cctrCode :"+cctrCode);
								}
								if (acctCode == null || acctCode.equals(""))
								{
									sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
											+"WHERE ITEM_SER = '"+itemSer1+"' "
											+"AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString(1);
										System.out.println("acctCode :"+acctCode);
										cctrCode = rs.getString(2);
										System.out.println("cctrCode :"+cctrCode);
									}
									if (acctCode == null || acctCode.equals(""))
									{
										sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEMSER "
												+"WHERE ITEM_SER = '"+itemSer;
										System.out.println("sql :"+sql);
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											acctCode = rs.getString(1);
											System.out.println("acctCode :"+acctCode);
											cctrCode = rs.getString(2);
											System.out.println("cctrCode :"+cctrCode);
										}
									}
								}
							}
						}						
					} // end if III
				} // end if II
				else
				{
					sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
							+"WHERE ITEM_CODE = '"+itemCode+"' "
							+"AND ITEM_SER = '"+itemSer+"' "
							+"AND TRAN_TYPE = '"+tranType+"'";
					System.out.println("sql from else part :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString(1);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(2);
						System.out.println("cctrCode :"+cctrCode);
					}
					if (acctCode == null || acctCode.trim().length() == 0)
					{// if I
						sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
								+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
								+"AND TRAN_TYPE = '"+tranType+"'";
						System.out.println("sql from else part :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString(1);
							System.out.println("acctCode :"+acctCode);
							cctrCode = rs.getString(2);
							System.out.println("cctrCode :"+cctrCode);
						}
						if (acctCode == null || acctCode.trim().length() == 0)
						{// if II
							sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
									+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
									+"AND TRAN_TYPE = ' '";
							System.out.println("sql from else part :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString(1);
								System.out.println("acctCode :"+acctCode);
								cctrCode = rs.getString(2);
								System.out.println("cctrCode :"+cctrCode);
							}
							if (acctCode == null || acctCode.trim().length() == 0)
							{// if III
								if (itemSer == null || itemSer.trim().length() == 0)
								{
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										itemSer1 = rs.getString(1);
										System.out.println("itemSer1 :"+itemSer1);
									}
								}
								else
								{
									itemSer1 = itemSer;
									System.out.println("itemSer1 :"+itemSer1);
								}
								sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
										+"WHERE ITEM_SER = '"+itemSer1+"' "
										+"AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
								System.out.println("sql from else part :"+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString(1);
									System.out.println("acctCode :"+acctCode);
									cctrCode = rs.getString(2);
									System.out.println("cctrCode :"+cctrCode);
								}
								if (acctCode == null || acctCode.trim().length() == 0)
								{// if IV
									sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
											+"WHERE ITEM_SER = '"+itemSer1+"' "
											+"AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString(1);
										System.out.println("acctCode :"+acctCode);
										cctrCode = rs.getString(2);
										System.out.println("cctrCode :"+cctrCode);
									}
									if (acctCode == null || acctCode.trim().length() == 0)
									{// IF V
										sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEMSER "
												+"WHERE ITEM_SER = '"+itemSer+"'";
										System.out.println("sql :"+sql);
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											acctCode = rs.getString(1);
											System.out.println("acctCode :"+acctCode);
											cctrCode = rs.getString(2);
											System.out.println("cctrCode :"+cctrCode);
										}
									}// end if V
								}// end if IV
							}//end if III
						}// end if II
					}// end if I
				}//end else
			}// end if I
			else if (purpose.equals("PO"))
			{
				sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '"+itemCode+"' AND "+
						"ITEM_SER = '"+itemSer+"' AND TRAN_TYPE = '"+tranType+"'";		
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					acctCode = rs.getString("ACCT_CODE__AP");
					cctrCode = rs.getString("CCTR_CODE__AP");
				}
				stmt.close();
				if (acctCode == null || acctCode.trim().length() == 0)
				{
					sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = '"+tranType+"'";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString("ACCT_CODE__AP");
						cctrCode = rs.getString("CCTR_CODE__AP");
					}
					stmt.close();
					if (acctCode == null || acctCode.trim().length() == 0)
					{
						sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = ' '";
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString("ACCT_CODE__AP");
							cctrCode = rs.getString("CCTR_CODE__AP");
						}
						stmt.close();
						if (acctCode == null || acctCode.trim().length() == 0)
						{
							if (itemSer == null || itemSer.trim().length() == 0)
							{
								sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									itemSer1 = rs.getString("ITEM_SER");
								}
							}
							else
							{
								itemSer1 = itemSer;
							}
							stmt = conn.createStatement();
							sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString("ACCT_CODE__AP");
								cctrCode = rs.getString("CCTR_CODE__AP");
							}
							stmt.close();
							if (acctCode == null || acctCode.trim().length() == 0)
							{
								sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString("ACCT_CODE__AP");
									cctrCode = rs.getString("CCTR_CODE__AP");	
								}
								stmt.close();
								if (acctCode == null || acctCode.trim().length() == 0)
								{
									sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEMSER WHERE ITEM_SER = '"+itemSer1+"'";
									stmt = conn.createStatement();
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{	
										acctCode = rs.getString("ACCT_CODE__AP");
										cctrCode = rs.getString("CCTR_CODE__AP");
									}
								}
							}
						}
					}
				}				
			}
			else if (purpose.equals("PORCP"))
			{
				sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '"+itemCode+"' AND ITEM_SER = '"+itemSer+"' AND TRAN_TYPE = '"+tranType+"'";		
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					acctCode = rs.getString("ACCT_CODE__PR");
					cctrCode = rs.getString("CCTR_CODE__PR");
				}
				stmt.close();
				if (acctCode == null || acctCode.trim().length() == 0)
				{
					stmt = conn.createStatement();
					sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = '"+tranType+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString("ACCT_CODE__PR");
						cctrCode = rs.getString("CCTR_CODE__PR");
					}
					stmt.close();
					if (acctCode == null || acctCode.trim().length() == 0)
					{
						stmt = conn.createStatement();
						sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = ' '";
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString("ACCT_CODE__PR");
							cctrCode = rs.getString("CCTR_CODE__PR");
						}
						stmt.close();
						if (acctCode == null || acctCode.trim().length() == 0)
						{
							if (itemSer == null || itemSer.trim().length() == 0)
							{
								stmt = conn.createStatement();
								sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									itemSer1 = rs.getString("ITEM_SER");
								}
								stmt.close();
							}
							else
							{
								itemSer1 = itemSer;	
							}
							sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString("ACCT_CODE__PR");
								cctrCode = rs.getString("CCTR_CODE__PR");
							}
							stmt.close();
							if (acctCode == null || acctCode.trim().length() == 0)
							{
								sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString("ACCT_CODE__PR");
									cctrCode = rs.getString("CCTR_CODE__PR");
								}
								stmt.close();
								if (acctCode == null || acctCode.trim().length() == 0)
								{
									sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEMSER WHERE ITEM_SER = '"+itemSer1+"'";
									stmt = conn.createStatement();
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString("ACCT_CODE__PR");
										cctrCode = rs.getString("CCTR_CODE__PR");
									}
								}
							}
						}
					}
				}
			}			
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The exception occurs in acctDetrTType() :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The exception occurs in acctDetrTType() :"+e);
			throw new ITMException(e);
		}
		finally 
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		if (acctCode == null)
		{
			acctCode = "";
		}
		if (cctrCode == null)
		{
			cctrCode = "";
		}
		retStr = acctCode + "\t" + cctrCode;
		System.out.println("retStr :"+retStr);
		return retStr;
	}

	public static String checkNull(String str)
	{
		if(str == null || str.equals(""))
		{
			str = "";
		}
		else 
		{
			str = str.trim();
		}
		return str;
	}
}