/********************************************************
        Title : ConsumeOrderIC
        Date  : 08/05/2020
        Developer: Manish Mhatre

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
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

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ConsumeOrderIC extends ValidatorEJB

implements ConsumeOrderICLocal, ConsumeOrderICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String winName = null;
	FinCommon finCommon = new FinCommon();
	DistCommon discommon = new DistCommon();
	ValidatorEJB validator = new ValidatorEJB();
	DistCommon distcommon=new DistCommon();

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

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		int childNodeListLength=0;
		Node childNode = null;
		String childNodeName = null,childNodeValue="";
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String sql = "";
		String errCode = "";
		String errorType = "";
		String errString = ""; 
		String taxChap = "";
		String taxClass = "";
		String taxEnv = "";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		//ConnDriver connDriver = new ConnDriver();
		String siteCodeOrd="",siteCodeReq="", empCode="", deptCode="",itemSer="",mcCode="",itemSeries="";
		String itemCode="", currCode="",  tranCode="", othSeries="",locCode="",unit="",
				acctCode="" ,cctrCode="",availableYn="" , available="",modName="",projCode="";
		//datetime mdate1, mdate2
		Timestamp orderDate=null;
		SimpleDateFormat sdf = null;
		double qty=0;
		Timestamp orderDateT=null;
		String transer = "C-ORD";
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{ 

			orderDateT = new java.sql.Timestamp(System.currentTimeMillis());
			sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String orderDateStr = sdfAppl.format(orderDateT);
			conn = getConnection(); 
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
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					System.out.println("CURRENT COLUMN IN  VALIDATION ["+childNodeName+"]");

					if (childNodeName.equalsIgnoreCase("order_date"))
					{
						orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom));
						siteCodeOrd=checkNull(this.genericUtility.getColumnValue("site_code__ord", dom));
						orderDate= Timestamp.valueOf(genericUtility.getValidDateString(orderDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						errCode=finCommon.nfCheckPeriod("PUR", orderDate, siteCodeOrd, conn);
						if(errCode != null && errCode.trim().length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("site_code__req")|| childNodeName.equalsIgnoreCase("site_code__ord"))
					{
						//siteCodeReq=checkNull(this.genericUtility.getColumnValue("site_code__req", dom));
						//siteCodeOrd=checkNull(this.genericUtility.getColumnValue("site_code__ord", dom));
						childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));

						/*
						 * if (childNodeValue != null && childNodeValue.trim().length() > 0 ) {
						 */                                                              
						//commented  By vrushabh joshi
						if (childNodeValue == null || childNodeValue.trim().length()==0 )  // added By vrushabh joshi start
						{
							errCode= "VTNULSITE"; 
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{         // added By vrushabh Joshi end                                                          
							errCode = finCommon.isSiteCode(childNodeValue, transer,conn);
							System.out.println("SiteCode Error code is"+errCode);
							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}                                                                  

						}

					}


					else if(childNodeName.equalsIgnoreCase("emp_code"))
					{
						empCode=checkNull(this.genericUtility.getColumnValue("emp_code", dom));
						siteCodeOrd=checkNull(this.genericUtility.getColumnValue("site_code__ord", dom));
						deptCode=checkNull(this.genericUtility.getColumnValue("dept_code", dom));
						itemSer=checkNull(this.genericUtility.getColumnValue("item_ser", dom));
						errCode=finCommon.isEmployee(siteCodeOrd, empCode, transer, conn);
						//Added by Anagha R on 26/11/2020 for (Lloyd) Consumption issue emp_code validation from master
						if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Added by Anagha R on 26/11/2020 for (Lloyd) Consumption issue emp_code validation from master
						if (errCode == null || errCode.trim().length() == 0) 
						{
							sql= " Select count(*) from sales_pers "+ 
									" where emp_code = ? and item_ser = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							pstmt.setString(2, itemSer);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}

							if(cnt==0)
							{
								errCode="VTSPIS";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						if(errCode==null || errCode.trim().length()==0)
						{
							sql=" Select count(*) from employee " + 
									" where emp_code = ? and dept_code = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, empCode);
							pstmt.setString(2, deptCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(cnt==0)
							{
								errCode="VTEMPDEPT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}

					}

					else if(childNodeName.equalsIgnoreCase("mc_code"))
					{
						mcCode=checkNull(this.genericUtility.getColumnValue("mc_code", dom));
						System.out.println("Machin cooooooooooooode111 >>>>"+mcCode);
						//Modified by Sana S on 25/06/20 [start][to validate mc_code only if entered by user suggested by Piyush sir]
						//if(mcCode != null || mcCode.trim().length() > 0)
						if(mcCode != "" || mcCode.trim().length() > 0)//modified by Sana S on 30/06/20
						{
							System.out.println("Machin cooooooooooooode222 >>>>"+mcCode);
							sql="select count(*) from machines where mc_code = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, mcCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
							if(cnt==0)
							{
								errCode="VTMC01";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Modified by Sana S on 25/06/20 [end][to validate mc_code only if entered by user suggested by Piyush sir]
					}
					else if(childNodeName.equalsIgnoreCase("proj_code"))
					{
						projCode=checkNull(genericUtility.getColumnValue("proj_code", dom));
						if(projCode!=null && projCode.trim().length()>0)
						{
							sql="Select count(*) from project where proj_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							if(cnt==0)
							{

								errCode = "VTPROJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;							

						}
					}
					else if(childNodeName.equalsIgnoreCase("dept_code"))
					{
						deptCode=checkNull(this.genericUtility.getColumnValue("dept_code", dom));
						sql = " SELECT COUNT(1) FROM DEPARTMENT WHERE dept_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, deptCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VTDEPT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

					}
					else if(childNodeName.equalsIgnoreCase("item_ser"))
					{
						itemSer=checkNull(this.genericUtility.getColumnValue("item_ser", dom));
						if(itemSer!=null && itemSer.trim().length()>0)
						{
							sql="Select count(1) from itemser where item_ser= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							if (cnt==0) 
							{
								errCode = "VTITEMSER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode=checkNull(this.genericUtility.getColumnValue("curr_code", dom));
						sql="Select count(*) from currency where curr_code= ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, currCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							cnt=rs.getInt(1);
						}
						if(cnt==0)
						{
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;
					}
					else if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode=checkNull(this.genericUtility.getColumnValue("tran_code", dom));
						if(tranCode!=null && tranCode.trim().length()>0)
						{
							sql="Select count(*) from transporter where tran_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							if(cnt==0)
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
						}

					}
					else if(childNodeName.equalsIgnoreCase("loc_code"))
					{
						locCode=checkNull(this.genericUtility.getColumnValue("loc_code", dom));
						if(locCode!=null && locCode.trim().length()>0)
						{
							availableYn=checkNull(this.genericUtility.getColumnValue("available_yn", dom));
							sql="Select b.available from location a,invstat b where a.inv_stat=b.inv_stat and a.loc_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								available=rs.getString(1);
							}
							if(available.equalsIgnoreCase(availableYn))
							{
								errCode = "VTAVAIL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
						}
					}	
				}		

				valueXmlString.append("</Detail1>");
				break;
			case 2:

				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				System.out.println("editFlag  -->>["+editFlag+"]");
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode=checkNull(genericUtility.getColumnValue("item_code", dom));
						siteCodeReq=checkNull(genericUtility.getColumnValue("site_code__req", dom1));
						orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom1));
						//	orderDate= Timestamp.valueOf(genericUtility.getValidDateString(orderDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");


						System.out.println("inside itemchanged detail order date"+orderDateStr);
						itemSer=checkNull(genericUtility.getColumnValue("item_ser", dom1));
						errCode=isItem(siteCodeReq, itemCode, transer, conn);
						System.out.println("item code errCode"+errCode);
						if(errCode != null || errCode.trim().length()>0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						System.out.println("item series header ["+itemSer+ "]");
						//if(errCode==null || errCode.trim().length()==0)
						//{
						if(itemSer!=null && itemSer.trim().length()>0)
						{
							sql="Select oth_series from itemser where item_ser= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								othSeries=rs.getString("oth_series");
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
							if(othSeries==null || othSeries.trim().length()==0)
							{
								othSeries="N";
							}
							System.out.println("Other series>>"+othSeries);

							sql="Select item_ser from item where item_code= ? ";
							pstmt1=conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							rs1=pstmt1.executeQuery();
							if(rs1.next())
							{
								itemSeries=rs1.getString("item_ser");
							}
							pstmt1.close();
							pstmt1=null;
							rs1.close();
							rs1=null;
							itemSer=itemSer.trim();
							itemSeries=itemSeries.trim();
							System.out.println("itemser"+itemSer+"itemseries"+itemSeries);
							if((!itemSer.equalsIgnoreCase(itemSeries)) && "N".equalsIgnoreCase(othSeries))
							{
								errCode = "VTITEM2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
						//}
					}
					else if(childNodeName.equalsIgnoreCase("unit"))
					{
						unit=checkNull(genericUtility.getColumnValue("unit", dom));
						sql="Select count(*) from uom where unit= ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, unit);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							cnt=rs.getInt(1);
						}
						if(cnt==0)
						{
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;
					}
					else if(childNodeName.equalsIgnoreCase("acct_code"))
					{
						acctCode=checkNull(genericUtility.getColumnValue("acct_code", dom));
						siteCodeOrd=checkNull(genericUtility.getColumnValue("site_code__ord", dom1));
						errCode=finCommon.isAcctCode(siteCodeOrd, acctCode, modName, conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code"))
					{
						cctrCode=checkNull(genericUtility.getColumnValue("cctr_code", dom));
						if(cctrCode!=null && cctrCode.trim().length()>0)
						{
							acctCode=checkNull(genericUtility.getColumnValue("acct_code", dom));
							errCode=finCommon.isCctrCode(acctCode, cctrCode, modName, conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}

					}
					else if(childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap=checkNull(genericUtility.getColumnValue("tax_chap", dom));
						if(taxChap!=null && taxChap.trim().length()>0) 
						{
							sql="Select count(*) from taxchap where tax_chap= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,taxChap);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							if(cnt==0)
							{
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;							
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxClass=checkNull(genericUtility.getColumnValue("tax_class", dom));
						if(taxClass!=null && taxClass.trim().length()>0)
						{
							sql="Select count(*) from taxclass where tax_class= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							if(cnt==0)
							{
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;							
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_env"))
					{
						taxEnv=checkNull(genericUtility.getColumnValue("tax_env", dom));
						//orderDate = sdf.parse(genericUtility.getColumnValue("order_date",dom));
						orderDateStr = checkNull(genericUtility.getColumnValue("order_date", dom1));
						if(taxClass!=null && taxClass.trim().length()>0)
						{
							sql="Select count(*) from taxenv where tax_env= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, taxEnv);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;							

							if(cnt==0)
							{
								errCode = "VTTAXENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							else
							{
								if (orderDateStr != null && orderDateStr.trim().length() > 0) {
									orderDateT = Timestamp.valueOf(genericUtility.getValidDateString(orderDateStr,
											genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
											+ " 00:00:00.0");
								}
								errCode = distcommon.getCheckTaxEnvStatus(taxEnv, orderDateT, "C", conn);
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}

						}
					}
					else if(childNodeName.equalsIgnoreCase("proj_code"))
					{
						projCode=checkNull(genericUtility.getColumnValue("proj_code", dom));
						if(projCode!=null && projCode.trim().length()>0)
						{
							sql="Select count(*) from project where proj_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, projCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							if(cnt==0)
							{

								errCode = "VTPROJ1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;							

						}
					}
					else if(childNodeName.equalsIgnoreCase("quantity"))
					{
						qty=Double.parseDouble(genericUtility.getColumnValue("quantity", dom));
						if(qty<=0)
						{
							errCode = "VTQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
					}
					else if(childNodeName.equalsIgnoreCase("loc_code"))
					{
						locCode=checkNull(genericUtility.getColumnValue("loc_code", dom));
						if(locCode!=null && locCode.trim().length()>0)
						{
							availableYn=checkNull(genericUtility.getColumnValue("available_yn", dom1));
							sql="Select b.available from location a,invstat b where a.inv_stat=b.inv_stat and a.loc_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								available=rs.getString(1);
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
							if(!(available.equalsIgnoreCase(availableYn)))
							{
								errCode = "VTAVAIL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("mc_code"))
					{
						mcCode=checkNull(genericUtility.getColumnValue("mc_code", dom));
						//Modified by Sana S on 25/06/20 [start][to validate mc_code not compulsory and validate it only if entered by user,suggested by Piyush sir]
						/*if(mcCode == null || mcCode.trim().length()==0)
						{
							errCode = "VTMCCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						else*/
						//if(mcCode != null || mcCode.trim().length() > 0)

						if(mcCode != "" || mcCode.trim().length() > 0)//Modified by Sana s on 30/06/20
							//Modified by Sana S on 25/06/20 [end][to validate mc_code not compulsory and validate it only if entered by user,suggested by Piyush sir]
						{
							sql="Select count(*) from machines where mc_code= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, mcCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);

							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;

							if(cnt==0)
							{
								errCode = "VTMC01";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}

					}

				}

				valueXmlString.append("</Detail2>");

			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					int pos = errCode.indexOf("~");
					System.out.println("pos :"+pos);
					if(pos>-1)
					{
						errCode=errCode.substring(0,pos);
					}

					System.out.println("error code is :"+errCode);
					errFldName = (String)errFields.get(cnt);
					if (errCode != null && errCode.trim().length() > 0) {
						errString = getErrorString(errFldName, errCode, userId);
						errorType = errorType(conn, errCode);
					}

					if (errString != null && errString.trim().length() > 0)
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
					if (rs1 != null)
					{
						rs1.close();
						rs1 = null;
					}
					if (pstmt1 != null)
					{
						pstmt1.close();
						pstmt1 = null;
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
			System.out.println("Xml String value["+valueXmlString+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("ValueXmlString["+valueXmlString+"]");
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException
	{
		System.out.println("Inside consume order Itemchanged>>>>>>>");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";
		String columnValue="";
		String currCode = "";
		String descrItem = "";
		String itemCode = "",tranType="";
		String itemSer = "",itemDescr="",orderType="";
		String cctr="",acct="";
		String unit = "";
		String loginSite = "",loginCode="";
		String descr = "";
		String finEntity="",reasCode="",rsCodeDescr="",siteCodeOrd="",siteDescr="",siteCodeReq="",siteStr="";
		String empCode="",deptCode="",deptDescr="",empFname="",empLname="",designation="",tranCode="",tranName="",currency="",currencyCode="";
		String lineNo="",consumeOrder="",cctrCode="",locCode="",mcCode="",lsStr="";
		int ctr = 0;
		double exchRate=0.0,availStock=0.0;
		double shipperQty=0.0,acShipperQty=0.0,integralQty=0.0,acintegralQty=0.0;
		int noArt=0,noArt1=0,noArt2=0;
		int currentFormNo = 0;
		double  quantity = 0.0,balQty=0.0,looseQty=0.0;
		double stdExrt = 0.0;
		int childNodeListLength = 0;
		String protectval="0";
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = new DistCommon();
		NodeList detail2List=null,childDetilList=null;
		Node detailNode=null,chidDetailNode=null;
		String updateFlag="";
		try
		{

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Calendar currentDate = Calendar.getInstance();

			String orderDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + orderDate);
			conn = getConnection();

			conn.setAutoCommit(false);
			connDriver = null;
			this.finCommon = new FinCommon();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				valueXmlString.append("<Detail1>");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();

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
					loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

					sql="Select descr from site where site_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						descr=rs.getString("descr");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					sql = "select emp_code from users where code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						empCode = checkNull(rs.getString("emp_code"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select dept_code from employee where emp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, empCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						deptCode = checkNull(rs.getString("dept_code"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select descr from department where dept_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						deptDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select emp_fname,emp_lname,designation from employee where emp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, empCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						empFname = checkNull(rs.getString("emp_fname"));
						empLname = checkNull(rs.getString("emp_lname"));
						designation = checkNull(rs.getString("designation"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select tran_code from sales_pers where emp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, empCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						tranCode = checkNull(rs.getString("tran_code"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select tran_name from transporter where tran_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						tranName = checkNull(rs.getString("tran_name"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<dept_code >").append("<![CDATA["+deptCode+"]]>").append("</dept_code>");
					valueXmlString.append("<department_descr >").append("<![CDATA["+deptDescr+"]]>").append("</department_descr>");
					valueXmlString.append("<emp_code >").append("<![CDATA["+empCode+"]]>").append("</emp_code>");
					valueXmlString.append("<employee_emp_fname >").append("<![CDATA["+empFname+"]]>").append("</employee_emp_fname>");
					valueXmlString.append("<employee_emp_lname >").append("<![CDATA["+empLname+"]]>").append("</employee_emp_lname>");
					valueXmlString.append("<employee_designation >").append("<![CDATA["+designation+"]]>").append("</employee_designation>");
					valueXmlString.append("<Site_code__ord >").append("<![CDATA["+loginSite+"]]>").append("</Site_code__ord>");
					valueXmlString.append("<Site_code__req >").append("<![CDATA["+loginSite+"]]>").append("</Site_code__req>");
					valueXmlString.append("<Site_descr >").append("<![CDATA["+descr+"]]>").append("</Site_descr>");
					valueXmlString.append("<order_date >").append("<![CDATA["+orderDate+"]]>").append("</order_date>");
					valueXmlString.append("<chg_date >").append("<![CDATA["+orderDate+"]]>").append("</chg_date>");
					valueXmlString.append("<conf_date >").append("<![CDATA["+orderDate+"]]>").append("</conf_date>");
					valueXmlString.append("<status >").append("<![CDATA["+"P"+"]]>").append("</status>");
					valueXmlString.append("<status_date >").append("<![CDATA["+orderDate+"]]>").append("</status_date>");
					valueXmlString.append("<tran_code >").append("<![CDATA["+tranCode+"]]>").append("</tran_code>");
					valueXmlString.append("<transporter_tran_name >").append("<![CDATA["+tranName+"]]>").append("</transporter_tran_name>");

					sql="Select fin_entity from site where site_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						finEntity=rs.getString("fin_entity");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					sql="Select curr_code from finent where fin_entity= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						currCode=rs.getString("curr_code");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					valueXmlString.append("<curr_code >").append("<![CDATA["+currCode+"]]>").append("</curr_code>");

					exchRate=finCommon.getDailyExchRateSellBuy(currCode, currCode, loginSite, orderDate, " ", conn);
					System.out.println("Exchange Rate>>>>>"+exchRate);
					valueXmlString.append("<exch_rate >").append("<![CDATA["+exchRate+"]]>").append("</exch_rate>");


				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					reasCode=checkNull(genericUtility.getColumnValue("reas_code", dom));
					orderType=checkNull(genericUtility.getColumnValue("order_type", dom));
					sql="Select descr from gencodes where mod_name='W_CONSUME_ORDER' and fld_name='REAS_CODE' and fld_value= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, reasCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						rsCodeDescr=rs.getString("descr");
					}
					else
					{
						sql="Select descr from gencodes where mod_name='X' and fld_name='REAS_CODE' and fld_value= ? ";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1, reasCode);
						if(rs1.next())
						{
							rsCodeDescr=rs1.getString("descr");
						}
						pstmt1.close();
						pstmt1=null;
						rs1.close();
						rs1=null;

					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					if(rsCodeDescr!=null && rsCodeDescr.trim().length()>0)
					{
						valueXmlString.append("<gencodes_descr >").append("<![CDATA["+rsCodeDescr+"]]>").append("</gencodes_descr>");
					}

					//when detail entry present then header order type will non editable else editable
					//start
					//System.out.println("before order type protect in case 1 itm default edit");
					detail2List = dom2.getElementsByTagName("Detail2");

					for(int t =0; t < detail2List.getLength(); t++ )
					{

						detailNode = detail2List.item(t);
						childDetilList = detailNode.getChildNodes();
						for(int p =0; p < childDetilList.getLength(); p++ )
						{

							chidDetailNode = childDetilList.item(p);
							if(chidDetailNode.getNodeName().equalsIgnoreCase("attribute") )
							{

								updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
							}

							if(chidDetailNode.getNodeName().equalsIgnoreCase("item_code") )
							{
								if(chidDetailNode.getFirstChild() != null )
								{

									if(!updateFlag.equalsIgnoreCase("D"))
									{  

										protectval="1";
									}
								}
								else
								{
									protectval="0";
								}
							}
						}
					}
					valueXmlString.append("<order_type protect=\"" + protectval + "\">").append("<![CDATA[" + orderType + "]]>").append("</order_type>");
					//end
				}

				else if (currentColumn.trim().equalsIgnoreCase("site_code__ord"))
				{
					siteCodeOrd=checkNull(genericUtility.getColumnValue("site_code__ord", dom));
					sql="Select descr from site where site_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeOrd);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						siteDescr=rs.getString("descr");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					valueXmlString.append("<site_descr >").append("<![CDATA["+siteDescr+"]]>").append("</site_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("site_code__req"))
				{
					siteCodeReq=checkNull(genericUtility.getColumnValue("site_code__req", dom));
					sql="Select fin_entity from site where site_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeReq);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						finEntity=rs.getString("fin_entity");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					sql="Select curr_code from finent where fin_entity= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						currCode=rs.getString("curr_code");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					valueXmlString.append("<curr_code >").append("<![CDATA["+currCode+"]]>").append("</curr_code>");

					exchRate=finCommon.getDailyExchRateSellBuy(currCode, currCode, loginSite, orderDate, " ", conn);
					System.out.println("Exchange Rate>>>>>"+exchRate);
					valueXmlString.append("<exch_rate >").append("<![CDATA["+exchRate+"]]>").append("</exch_rate>");

				}

				else if (currentColumn.trim().equalsIgnoreCase("item_ser"))
				{
					itemSer=checkNull(genericUtility.getColumnValue("item_ser", dom));

					sql="Select descr from itemser where item_ser= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						itemDescr=rs.getString("descr");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					valueXmlString.append("<itemser_descr >").append("<![CDATA["+itemDescr+"]]>").append("</itemser_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("emp_code"))
				{
					empCode=checkNull(genericUtility.getColumnValue("emp_code", dom));

					sql="Select emp_fname,emp_lname,dept_code,designation from employee where emp_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, empCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						empFname=rs.getString("emp_fname");
						empLname=rs.getString("emp_lname");
						deptCode=rs.getString("dept_code");
						designation=rs.getString("designation");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					valueXmlString.append("<employee_emp_fname >").append("<![CDATA["+empFname+"]]>").append("</employee_emp_fname>");
					valueXmlString.append("<employee_emp_lname >").append("<![CDATA["+empLname+"]]>").append("</employee_emp_lname>");
					valueXmlString.append("<employee_designation >").append("<![CDATA["+designation+"]]>").append("</employee_designation>");
					valueXmlString.append("<dept_code >").append("<![CDATA["+deptCode+"]]>").append("</dept_code>");

					sql="Select descr from department where dept_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						deptDescr=rs.getString("descr");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					valueXmlString.append("<department_descr >").append("<![CDATA["+deptDescr+"]]>").append("</department_descr>");

					sql="Select tran_code from sales_pers where emp_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, empCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						tranCode=rs.getString("tran_code");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;



					sql="Select tran_name from transporter where tran_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, tranCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						tranName=rs.getString("tran_name");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					valueXmlString.append("<tran_code >").append("<![CDATA["+tranCode+"]]>").append("</tran_code>");
					valueXmlString.append("<transporter_tran_name >").append("<![CDATA["+tranName+"]]>").append("</transporter_tran_name>");

				}
				else if (currentColumn.trim().equalsIgnoreCase("dept_code"))
				{
					deptCode=checkNull(genericUtility.getColumnValue("dept_code", dom));
					sql="Select descr from department where dept_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						deptDescr=rs.getString("descr");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					valueXmlString.append("<department_descr >").append("<![CDATA["+deptDescr+"]]>").append("</department_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currencyCode=checkNull(genericUtility.getColumnValue("curr_code", dom));
					siteCodeReq=checkNull(genericUtility.getColumnValue("site_code__req", dom));

					sql="Select fin_entity from site where site_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeReq);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						finEntity=rs.getString("fin_entity");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;


					sql="Select curr_code from finent where fin_entity= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						currency=rs.getString("curr_code");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					sql="Select curr_code from finent f,site s where s.site_code= ? and f.fin_entity= s.fin_entity ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeReq);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						currCode=rs.getString("curr_code");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					if(currency.trim()==currencyCode.trim())
					{
						valueXmlString.append("<exch_rate protect = \"1\" >").append("<![CDATA[" + exchRate + "]]>")
						.append("</exch_rate>");
					}
					else
					{
						valueXmlString.append("<exch_rate protect = \"0\" >").append("<![CDATA[" + exchRate + "]]>")
						.append("</exch_rate>");
					}
					exchRate=finCommon.getDailyExchRateSellBuy(currencyCode, currency, siteCodeReq, orderDate, " ", conn);
					valueXmlString.append("<exch_rate >").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode=checkNull(genericUtility.getColumnValue("tran_code", dom));

					sql="Select tran_name,curr_code from transporter where tran_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, tranCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						tranName=rs.getString("tran_name");
						currCode=rs.getString("curr_code");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					valueXmlString.append("<transporter_tran_name >").append("<![CDATA[" + tranName + "]]>").append("</transporter_tran_name>");
					valueXmlString.append("<curr_code >").append("<![CDATA[" + currCode + "]]>").append("</curr_code>");

					sql="Select std_exrt from currency where curr_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, currCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						stdExrt=rs.getDouble("std_exrt");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					valueXmlString.append("<exch_rate >").append("<![CDATA[" + stdExrt + "]]>").append("</exch_rate>");

				}
				else if (currentColumn.trim().equalsIgnoreCase("reas_code"))
				{
					reasCode=checkNull(genericUtility.getColumnValue("reas_code", dom));

					sql="Select descr from gencodes where mod_name='W_CONSUME_ORDER'  and rtrim(fld_value) = ? and fld_name = 'REAS_CODE' ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, reasCode);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						rsCodeDescr=rs.getString("descr");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					valueXmlString.append("<gencodes_descr >").append("<![CDATA[" + rsCodeDescr + "]]>").append("</gencodes_descr>");
				}
				
				

				valueXmlString.append("</Detail1>");
				break;
			case 2:

				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();

				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}

					ctr++;
				}
				while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));
				System.out.println("CURRENT COLUMN ["+currentColumn+"]");


				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					if (lineNo != null && lineNo.trim().length() > 0) 
					{
						valueXmlString.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
					}
					consumeOrder=checkNull(genericUtility.getColumnValue("cons_order", dom));
					valueXmlString.append("<cons_order>").append("<![CDATA["+consumeOrder+"]]>").append("</cons_order>");

					cctrCode=checkNull(genericUtility.getColumnValue("cctr_code", dom));

					if(cctrCode!=null && cctrCode.trim().length()>0)
					{
						valueXmlString.append("<cctr_code>").append("<![CDATA["+cctrCode+"]]>").append("</cctr_code>");
					}

					locCode=checkNull(genericUtility.getColumnValue("loc_code", dom));
					if(locCode!=null && locCode.trim().length()>0)
					{
						valueXmlString.append("<loc_code>").append("<![CDATA["+locCode+"]]>").append("</loc_code>");
					}
					mcCode= checkNull(genericUtility.getColumnValue("mc_code", dom));
					valueXmlString.append("<mc_code>").append("<![CDATA["+mcCode+"]]>").append("</mc_code>");

					//added by manish mhatre on 1-3-2021
					//start manish
					orderType=checkNull(this.genericUtility.getColumnValue("order_type", dom1));
					System.out.println("case 2 order type>>>>" +orderType);
					//end manish

					//getting acct code,cctr code from item_acct_detr table
					//start
					if(itemSer!=null && itemSer.trim().length()>0)
					{
						sql="Select tran_type from item_acct_detr where item_ser = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							tranType=rs.getString("tran_type");
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;

						//commented by manish mhatre on 1-3-2021
						/*sql="Select  acct_code__in,cctr_code__in from item_acct_detr where item_ser= ?  and tran_type= ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						pstmt.setString(2, tranType);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							acct=rs.getString("acct_code__in");
							cctr=rs.getString("cctr_code__in");
						}
						pstmt.close();
						pstmt=null;
						rs.close();
                        rs=null;*/
						//end manish

						//added by manish mhatre on 1-3-2021[For getting acct code and cctr code from item_acct_detr table for CISS]
						//start manish
						cctr= finCommon.getAcctDetrTtype(itemCode, itemSer, "CISS", orderType, conn);

						if(cctr!=null && cctr.trim().length()>0)
						{
							String mcctrArray[] = cctr.split(",");
							System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
							if (mcctrArray.length > 0) {
								acct = mcctrArray[0];
								cctr = "";
							}
							if (mcctrArray.length > 1) {
								acct = mcctrArray[0];
								cctr = mcctrArray[1];
							}
							//end manish

						}
						System.out.println("acct code in itm default case2>>>>" + acct);
						System.out.println("cctr code in itm default case2>>>>" + cctr);

						valueXmlString.append("<cctr_code>").append("<![CDATA["+cctr+"]]>").append("</cctr_code>");
						valueXmlString.append("<acct_code>").append("<![CDATA["+acct+"]]>").append("</acct_code>");
					}   //end
				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					orderType=checkNull(this.genericUtility.getColumnValue("order_type", dom1));
					//when detail entry present then header order type will non editable else editable
					//start				
					detail2List = dom2.getElementsByTagName("Detail2");
					for(int t =0; t < detail2List.getLength(); t++ )
					{

						detailNode = detail2List.item(t);
						childDetilList = detailNode.getChildNodes();
						for(int p =0; p < childDetilList.getLength(); p++ )
						{

							chidDetailNode = childDetilList.item(p);
							if(chidDetailNode.getNodeName().equalsIgnoreCase("attribute") )
							{

								updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
							}

							if(chidDetailNode.getNodeName().equalsIgnoreCase("item_code") )
							{
								if(chidDetailNode.getFirstChild() != null )
								{

									if(!updateFlag.equalsIgnoreCase("D"))
									{
										protectval="1";
									}
								}
								else
								{
									protectval="0";
								}
							}
						}
					}
					valueXmlString.append("<order_type protect=\"" + protectval + "\">").append("<![CDATA[" + orderType + "]]>").append("</order_type>");
					//end
				}
				else if (currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));	

					sql = "select descr ,unit ,item_ser from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descrItem = checkNull(rs.getString(1));
						unit = checkNull(rs.getString(2));
						itemSer = checkNull(rs.getString(3));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					orderType=checkNull(this.genericUtility.getColumnValue("order_type", dom));
					cctr= finCommon.getAcctDetrTtype(itemCode, itemSer, "CISS", orderType, conn);

					if(cctr!=null && cctr.trim().length()>0)
					{
						String mcctrArray[] = cctr.split(",");
						System.out.println("@@@@@ mcctrArray.length[" + mcctrArray.length + "]");
						if (mcctrArray.length > 0) {
							acct = mcctrArray[0];
							cctr = "";
						}
						if (mcctrArray.length > 1) {
							acct = mcctrArray[0];
							cctr = mcctrArray[1];
						}

					}

					valueXmlString.append("<item_descr>").append("<![CDATA["+descrItem+"]]>").append("</item_descr>");
					valueXmlString.append("<unit>").append("<![CDATA["+unit+"]]>").append("</unit>");
					//	valueXmlString.append("<acct_code>").append("<![CDATA["+acct+"]]>").append("</acct_code>");

					cctrCode=checkNull(this.genericUtility.getColumnValue("cctr_code", dom));

					if(cctrCode!=null && cctrCode.trim().length()>0)
					{
						valueXmlString.append("<cctr_code>").append("<![CDATA["+cctrCode+"]]>").append("</cctr_code>");
					}
					else
					{
						deptCode=checkNull(this.genericUtility.getColumnValue("dept_code", dom));

						sql="Select cctr_code from department where dept_code= ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, deptCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							cctrCode=rs.getString("cctr_code");
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;

						if(cctrCode!=null && cctrCode.trim().length()>0)
						{
							valueXmlString.append("<cctr_code>").append("<![CDATA["+cctrCode+"]]>").append("</cctr_code>");
						}
						else
						{
							valueXmlString.append("<cctr_code>").append("<![CDATA["+cctr+"]]>").append("</cctr_code>");
						}
					}
					siteCodeReq=checkNull(this.genericUtility.getColumnValue("site_code__req", dom1));

					sql="Select (case when udf2 is null then ' ' else udf2 end) from site where site_code= ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, siteCodeReq);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						siteStr=rs.getString(1);
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					if(siteStr== null || siteStr.trim().length()==0)
					{
						sql="Select sum(quantity - alloc_qty - case when hold_qty is null then 0 else hold_qty end) from stock a, invstat b"
								+ " where a.inv_stat = b.inv_stat and a.item_code= ? and b.available= 'Y' ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							availStock=rs.getDouble(1);
						}
					}
					else
					{
						if((siteStr != null) && (siteStr.length() >0))
						{
							String tempArray[]=siteStr.split(",");
							System.out.println("tempArray[].length :-["+tempArray.length+"]");

							for(int itrArr=0; itrArr < tempArray.length;itrArr++)
							{
								siteStr=siteStr+"'"+tempArray[itrArr]+"',";
								//String siteToken=siteStr+"'"+tempArray[itrArr]+"',";
							}
							System.out.println("line1 before substring method :- ["+siteStr+"]");
							siteStr=siteStr.substring(0, siteStr.length()-1);
							System.out.println("line1 After substring method :-- ["+siteStr+"]");
						}
						if(siteStr == null ||siteStr.trim().length() == 0)
						{
							siteStr= "''";
						}

						//siteStr="("+siteToken+"'"+siteStr + "'"+")";

						sql="Select (case when sum((case when quantity is null then 0 else quantity end) - (case when alloc_qty is null then 0 else alloc_qty end) - (case when hold_qty is null then 0 else hold_qty end) ) is null then 0 else sum((case when quantity is null then 0 else quantity end) - (case when alloc_qty is null then 0 else alloc_qty end) - (case when hold_qty is null then 0 else hold_qty end)) end) "
								+" from stock a,invstat b "
								+" where a.inv_stat=b.inv_stat and a.item_code= ? and a.site_code in ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, siteStr);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							availStock=rs.getDouble(1);
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;

						integralQty=distCommon.getIntegralQty(" ", itemCode, siteCodeReq, conn);
						//	valueXmlString.append("<st_scheme>").append("<![CDATA["+" "+"]]>").append("</st_scheme>");
						valueXmlString.append("<st_scheme>").append("<![CDATA["+ integralQty+"]]>").append("</st_scheme>");//			dw_detedit[ii_currformno].setitem(1,"st_scheme", 'Integral Quantity : ' + string(lc_integral_qty,'#,##0.00') + '   Current Stock :' + string(lc_avail_stk))

					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("quantity"))
				{
					quantity=checkDoubleNull(this.genericUtility.getColumnValue("quantity", dom));
					itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
					siteCodeReq=checkNull(this.genericUtility.getColumnValue("site_code__req", dom1));

					noArt=distCommon.getNoArt(siteCodeReq, " ", itemCode, null, quantity, 'B', acShipperQty, acintegralQty, conn);
					shipperQty=acShipperQty;
					integralQty=acintegralQty;

					noArt1=distCommon.getNoArt(siteCodeReq, " ", itemCode, null, quantity, 'S', acShipperQty, acintegralQty, conn);
					balQty=quantity-(shipperQty * noArt1);

					noArt1=distCommon.getNoArt(siteCodeReq, " ", itemCode, null, balQty, 'I', acShipperQty, acintegralQty, conn);
					integralQty=acintegralQty;

					shipperQty= shipperQty * noArt1;
					integralQty= integralQty * noArt2;

					looseQty=quantity - (shipperQty + integralQty);

					lsStr="  Shipper Quantity =  "+ shipperQty + " Integral Quantity = " + integralQty + " Loose Quantity = "+ looseQty;

					//valueXmlString.append("<st_shipper >").append("<![CDATA["+" "+"]]>").append("</st_shipper>");
					valueXmlString.append("<st_shipper >").append("<![CDATA["+lsStr+"]]>").append("</st_shipper>");
				}
				
				//added by kailasG on 28-04-21 for [Require provision to the system that should auto calculate Quantity based on Dimension  ( Length, Width and Thickness ) and No of Pieces]
				//start 
				if(currentColumn.trim().equalsIgnoreCase("no_art") || currentColumn.trim().equalsIgnoreCase("dimension"))
				{
					System.out.println("Inside no_art block or dimension block");
					String noArtStr="",dimension="";
					double noArt3=0,quantity1=0;
					String reStr="";
					int pos=0;

					itemCode= genericUtility.getColumnValue("item_code", dom);
					dimension=genericUtility.getColumnValue("dimension", dom);
					noArtStr= genericUtility.getColumnValue("no_art", dom);


					System.out.println("item code>>"+itemCode+"\ndimension>>"+dimension+"\nno of articles>>"+noArtStr);


					if(dimension!=null && dimension.trim().length()>0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							unit = rs.getString("UNIT");
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						System.out.println("unit>>"+unit);

						if(noArtStr!=null && noArtStr.trim().length()>0)
						{
							noArt3=Double.parseDouble(noArtStr);
						}
						else
						{
							noArt3=1;
						}
						System.out.println("dimension>>"+dimension+"\n no of articles>>"+noArt3);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							quantity1=discommon.getQuantity(dimension,noArt3,unit,conn);
							System.out.println("quantity in dimension block>>"+quantity1);
							valueXmlString.append("<quantity>").append("<![CDATA["+quantity1+"]]>").append("</quantity>");
							setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity1)));
							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("after quantity itemchanged 1440.......");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
						}
					}
				}
				//end kailas



				valueXmlString.append("</Detail2>");
				break;
			}
			valueXmlString.append("</Root>");
			System.out.println("valueXmlString[" + valueXmlString.toString() + "]");

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
					if (rs1 != null)
					{
						rs1.close();
						rs1 = null;
					}
					if (pstmt1 != null)
					{
						pstmt1.close();
						pstmt1 = null;
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
	private String checkNullAndTrim(String value) {
		return value == null ? "" : value.trim();
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
	
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}


}

