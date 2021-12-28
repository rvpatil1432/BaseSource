package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.*;

import org.w3c.dom.*;

//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless//PomadPrc
public class PomadPrc extends ProcessEJB implements PomadPrcLocal, PomadPrcRemote
{

	DistCommon distCommonObj = new DistCommon();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();


	@Override
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	//getData Method

	@Override
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;	

		try
		{

			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 				
			}
			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{

			System.out.println("Exception :InvHoldRelGenEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();
			throw new ITMException(e);

		}
		return rtrStr; 
	}//END OF GETDATA(1)

	public String blanknull(String s)
	{
		if( s==null )
			return " ";
		else
			return s;
	}


	@Override
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		String errString = "";
		String getDataSql= "" ;

		ResultSet rs1 = null;
		PreparedStatement pstmt = null;
		StringBuffer retTabSepStrBuff = new StringBuffer();
		int cnt=0,count=0;
		String sql="";
		String amdNo = "",acctCodeDr = "",acctCodeCr = "",cctrCodeDr = "",cctrCodeCr = "",confirmed = "" ;
		ConnDriver connDriver = new ConnDriver();
		Connection conn= null;
		try
		{
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);	
			}
			DatabaseMetaData dbmd = conn.getMetaData();
			System.out.println("DriverName["+dbmd.getDriverName() + "]");
			System.out.println("DriverURI["+dbmd.getURL()  + "]");
			System.out.println("DriverUSER["+dbmd.getUserName() +"]");

			System.out.println("InvHoldRelGen : getData() Method Called");

			amdNo = genericUtility.getColumnValue("amd_no",headerDom);
			acctCodeDr = genericUtility.getColumnValue("acct_code__dr",headerDom);//
			acctCodeCr = genericUtility.getColumnValue("acct_code__cr",headerDom);
			cctrCodeDr = genericUtility.getColumnValue("cctr_code__dr",headerDom);
			cctrCodeCr = genericUtility.getColumnValue("cctr_code__cr",headerDom);
			System.out.println("data from process header-->>["+amdNo+"] and ["+acctCodeDr+"] and ["+acctCodeCr+"] and ["+cctrCodeDr+"] and ["+cctrCodeCr+"]");
			if ( amdNo == null || amdNo.trim().length() == 0 )
			{
				System.out.println("amdNo is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMAMDNULL","","",conn);
				return errString;
			}
			  else 
			{
				sql = "select count(1) from poamd_det where amd_no = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,amdNo);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				pstmt.close();
				rs1.close();
				pstmt = null;
				rs1 = null;
				if (cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VMAMDINVL","","",conn);
					return errString;
				}
				else
				{
					sql = "select confirmed from poamd_hdr where amd_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,amdNo);
					rs1 = pstmt.executeQuery();
					if (rs1.next())
					{
						 confirmed = blanknull(rs1.getString(1));
						 System.out.println("confirmed-->>  1["+confirmed.trim()+"]");
					}
					pstmt.close();
					rs1.close();
					pstmt = null;
					rs1 = null;//VMAMDINVL
					if (confirmed.trim().equalsIgnoreCase("Y"))
					{
						errString = itmDBAccessEJB.getErrorString("","VMCONFRM","","",conn);
						return errString;
					}
				}
			}
			
			System.out.println("acctCodeDr-->.["+acctCodeDr+"]");
		
			if(acctCodeDr!=null)
			{
				sql = "select count(1) from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,acctCodeDr);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				pstmt.close();
				rs1.close();
				pstmt = null;
				rs1 = null;
				if (cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VMACTRINVL","","",conn);
					return errString;
				}
			}
			else
			{
				System.out.println("amdNo is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMACCDNULL","","",conn);
				return errString;
			}
			//end of validation for acctCodeCr
			if(acctCodeCr!=null)
			{
				sql = "select count(1) from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,acctCodeCr);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				pstmt.close();
				rs1.close();
				pstmt = null;
				rs1 = null;
				if (cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VMACCRINVL","","",conn);
					return errString;
				}
			}
			else
			{
				System.out.println("amdNo is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMACCCNULL","","",conn);
				return errString;
//		
			}
				//end of validation for acctCodeCr
			System.out.println("cctrCodeDr-->.["+cctrCodeDr+"]");
		
			if(cctrCodeDr!=null)
			{
				System.out.println("from accounts 3--->>");
				sql = "select count(1) from costctr where cctr_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,cctrCodeDr);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				pstmt.close();
				rs1.close();
				pstmt = null;
				rs1 = null;
				if (cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","CCTRDRINVL","","",conn);
					return errString;
				}
			}
			else
			{
				System.out.println("amdNo is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMCCDNULL","","",conn);
				return errString;
		
			}
			System.out.println("cctrCodeCr-->.["+cctrCodeCr+"]");
			
			if(cctrCodeCr!=null)
			{
				System.out.println("from accounts 3--->>");
				sql = "select count(1) from costctr where cctr_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,cctrCodeCr);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				pstmt.close();
				rs1.close();
				pstmt = null;
				rs1 = null;
				if (cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","CCTRCRINVL","","",conn);
					return errString;
				}
			}
			else 
			{
				System.out.println("amdNo is Null...");
				errString = itmDBAccessEJB.getErrorString("","VMCCCNULL","","",conn);
				return errString;	
			}
			//end of validation of cctrCodeCr
			getDataSql= "SELECT POAMD_DET.AMD_NO AMD_NO,POAMD_DET.LINE_NO LINE_NO,POAMD_DET.PURC_ORDER PURC_ORDER," +
					"POAMD_DET.LINE_NO__ORD LINE_NO__ORD,POAMD_DET.SITE_CODE SITE_CODE,POAMD_DET.IND_NO IND_NO,POAMD_DET.ITEM_CODE ITEM_CODE," +
					"POAMD_DET.QUANTITY QUANTITY,POAMD_DET.RATE RATE,POAMD_DET.DISCOUNT DISCOUNT,POAMD_DET.NO_ART NO_ART," +
					"POAMD_DET.ACCT_CODE__DR ACCT_CODE__DR,POAMD_DET.CCTR_CODE__DR CCTR_CODE__DR, POAMD_DET.ACCT_CODE__CR ACCT_CODE__CR," +
					"POAMD_DET.CCTR_CODE__CR CCTR_CODE__CR,POAMD_HDR.AMD_DATE AMD_DATE,POAMD_HDR.AMD_TYPE AMD_TYPE," +
					"POAMD_HDR.ORD_DATE ORD_DATE,POAMD_HDR.SUPP_CODE SUPP_CODE,POAMD_HDR.SITE_CODE__DLV SITE_CODE__DLV,POAMD_HDR.SITE_CODE__ORD SITE_CODE__ORD," +
					"POAMD_HDR.SITE_CODE__BILL SITE_CODE__BILL,POAMD_HDR.DEPT_CODE DEPT_CODE,SITE.DESCR DESCR,SUPPLIER.SH_NAME SH_NAME,ITEM.DESCR DESCR1,POAMD_DET.ACCT_CODE__DR__O ACCT_CODE__DR__O," +
					"POAMD_DET.CCTR_CODE__DR__O CCTR_CODE__DR__O,POAMD_DET.ACCT_CODE__CR__O ACCT_CODE__CR__O,POAMD_DET.CCTR_CODE__CR__O CCTR_CODE__CR__O"+
					" FROM POAMD_DET, POAMD_HDR,SITE,SUPPLIER,ITEM " +
					" WHERE (POAMD_DET.AMD_NO = POAMD_HDR.AMD_NO ) and ( POAMD_HDR.SITE_CODE__DLV = SITE.SITE_CODE )" +
					" and ( POAMD_HDR.SUPP_CODE= SUPPLIER.SUPP_CODE) and POAMD_DET.ITEM_CODE = ITEM.ITEM_CODE and " +
					" ( (POAMD_DET.AMD_NO = ? ) ) ";

			pstmt = conn.prepareStatement(getDataSql);
			pstmt.setString(1,amdNo);
			rs1 = pstmt.executeQuery();
			while(rs1.next()) 
			{
				System.out.println("xml appending starts ");
				retTabSepStrBuff.append((rs1.getString("amd_no")==null?" ":rs1.getString("amd_no"))).append("\t");
				//SITE_CODE
				System.out.println(" rs1.getString(amd_no)-->>"+rs1.getString("amd_no"));
				retTabSepStrBuff.append((rs1.getString("line_no")==null?" ":rs1.getString("line_no"))).append("\t");
				//purc_order
				retTabSepStrBuff.append((rs1.getString("purc_order")==null?" ":rs1.getString("purc_order"))).append("\t");
				//line_no__ord
				retTabSepStrBuff.append((rs1.getString("line_no__ord")==null?" ":rs1.getString("line_no__ord"))).append("\t");
				//site_code
				retTabSepStrBuff.append((rs1.getString("site_code")==null?" ":rs1.getString("site_code"))).append("\t");
				//site_code__o
				retTabSepStrBuff.append((rs1.getString("ind_no")==null?" ":rs1.getString("ind_no"))).append("\t");
				//ind_no
				retTabSepStrBuff.append((rs1.getString("item_code")==null?" ":rs1.getString("item_code"))).append("\t");
				//item_code
				retTabSepStrBuff.append((rs1.getString("quantity")==null?" ":rs1.getString("quantity"))).append("\t");
				//quantity
				retTabSepStrBuff.append((rs1.getString("rate")==null?" ":rs1.getString("rate"))).append("\t");
				// quantity__o
				retTabSepStrBuff.append((rs1.getString("discount")==null?" ":rs1.getString("discount"))).append("\t");
				//unit
				retTabSepStrBuff.append((rs1.getString("no_art")==null?" ":rs1.getString("no_art"))).append("\t");
				//ratereq_date,"+//rs1.getString("acct_code__dr")==null?" ":rs1.getString("acct_code__dr")
				retTabSepStrBuff.append((acctCodeDr)).append("\t");
				//,Mrate__o,rs1.getString("cctr_code__dr")==null?" ":rs1.getString("cctr_code__dr")
				retTabSepStrBuff.append((cctrCodeDr)).append("\t");
				//discount//acctCodeCr//rs1.getString("acct_code__cr")==null?" ":rs1.getString("acct_code__cr")
				retTabSepStrBuff.append((acctCodeCr)).append("\t");
				//tax_amtrs1.getString("cctr_code__cr")==null?" ":rs1.getString("cctr_code__cr")
				retTabSepStrBuff.append((cctrCodeCr)).append("\t");
				//DLV_DATE//cctrCodeDr
				//retTabSepStrBuff.append((rs1.getString("dlv_date__o")==null?" ":rs1.getString("dlv_date__o"))).append("\t");
				//tot_amt
				retTabSepStrBuff.append((rs1.getDate("amd_date")==null?" ":rs1.getDate("amd_date"))).append("\t");
				//loc_code
				retTabSepStrBuff.append((rs1.getString("amd_type")==null?" ":rs1.getString("amd_type"))).append("\t");
				//req_date__o
				retTabSepStrBuff.append((rs1.getDate("ord_date")==null?" ":rs1.getDate("ord_date"))).append("\t");
				//dlv_date
				retTabSepStrBuff.append((rs1.getString("supp_code")==null?" ":rs1.getString("supp_code"))).append("\t");
				//dlv_qty
				retTabSepStrBuff.append((rs1.getString("site_code__dlv")==null?" ":rs1.getString("site_code__dlv"))).append("\t");
				//tax_class//amd_no
				retTabSepStrBuff.append((rs1.getString("site_code__ord")==null?" ":rs1.getString("site_code__ord"))).append("\t");
				// tax_chap
				retTabSepStrBuff.append((rs1.getString("site_code__bill")==null?" ":rs1.getString("site_code__bill"))).append("\t");
				//tax_env
				retTabSepStrBuff.append((rs1.getString("dept_code")==null?" ":rs1.getString("dept_code"))).append("\t");
				//remarks
				retTabSepStrBuff.append((rs1.getString("descr")==null?" ":rs1.getString("descr"))).append("\t");
				//work_order
				retTabSepStrBuff.append((rs1.getString("sh_name")==null?" ":rs1.getString("sh_name"))).append("\t");
				System.out.println("xml end data appending -->.");
				retTabSepStrBuff.append((rs1.getString("descr1")==null?" ":rs1.getString("descr1"))).append("\t");
				//ACCT_CODE_DR__O
				retTabSepStrBuff.append((rs1.getString("acct_code__dr__o")==null?" ":rs1.getString("acct_code__dr__o"))).append("\t");
				//acct_code__cr__o
				retTabSepStrBuff.append((rs1.getString("cctr_code__dr__o")==null?" ":rs1.getString("cctr_code__dr__o"))).append("\t");
				//cctr_code__cr__o
				retTabSepStrBuff.append((rs1.getString("acct_code__cr__o")==null?" ":rs1.getString("acct_code__cr__o"))).append("\t");
				//
				retTabSepStrBuff.append((rs1.getString("cctr_code__cr__o")==null?" ":rs1.getString("cctr_code__cr__o"))).append("\t");
				System.out.println("appending sql from sql query --->>");
				retTabSepStrBuff.append("\n");				
				count++;
				System.out.println("#####Counter:["+count+"]");
			}//end while
			rs1.close();
			pstmt.close();
			rs1=null;
			pstmt=null;
			System.out.println("retTabSepStrBuff:::["+retTabSepStrBuff.toString()+"]");
		}	//end of try	
		catch (SQLException e)
		{ 
			e.printStackTrace();
			System.out.println("SQLException :InvHoldRelGenEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :InvHoldRelGenEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{		
			try
			{		
				conn.close();
				conn = null;
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		if (getDataSql.trim().length()>0 && count > 0)
		{
			return retTabSepStrBuff.toString();	
		}
		else 
		{
			errString = itmDBAccessEJB.getErrorString("","VTNOREC1","","",conn);
			return errString;
		}
	}	


	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";
		Connection conn=null;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Connection conn= null;
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

				conn.setAutoCommit(false);	
			}
			//DatabaseMetaData dbmd = conn.getMetaData();

			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}

		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}

			retStr = process(xmlString, xmlString2, windowName, xtraParams,conn);

		}
		catch (Exception e)
		{

			System.out.println("Exception : InvHoldRelGenEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			retStr = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String sql="",errString="";
		//Connection conn= null;
		PreparedStatement pRelHdr = null,pstmt1 = null;
		PreparedStatement pRelDet = null;
		String amdNo = "",lineNo1 = "",acctCodeDr = "",acctCodeDr1 = "",cctrCodeDr = "",cctrCodeDr1 = "",acctCodeCr = "",acctCodeCr1 = "",cctrCodeCr = "",cctrCodeCr1 = "",lineNo = "";
		Document headerDom=null,detailDom=null;
		ResultSet rs1 = null;
		PreparedStatement pstmt = null;
		int cnt=0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		int parentNodeListLength = 0;
		int childNodeListLength = 0;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";

		try
		{	
			System.out.println("method : process(xmlString, xmlString2, windowName, xtraParams,conn) called");
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("xmlString[process]::::::::::;;;"+xmlString);
				System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
				headerDom = genericUtility.parseString(xmlString); 
				detailDom= genericUtility.parseString(xmlString2);
				//amd_no and line_no from detail dom
				amdNo = genericUtility.getColumnValue("amd_no",detailDom);
				lineNo1 = genericUtility.getColumnValue("line_no",detailDom);
				acctCodeDr = genericUtility.getColumnValue("acct_code__dr",headerDom);
				cctrCodeDr = genericUtility.getColumnValue("cctr_code__dr",headerDom);
				acctCodeCr = genericUtility.getColumnValue("acct_code__cr",headerDom);
				cctrCodeCr = genericUtility.getColumnValue("cctr_code__cr",headerDom);
				parentNodeList = detailDom.getElementsByTagName("Detail2");
				parentNodeListLength = parentNodeList.getLength(); 
				for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
				{
					parentNode = parentNodeList.item(selectedRow);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("childNodeListLength---->>> "+ childNodeListLength);
					for (int childRow = 0; childRow < childNodeListLength; childRow++)
					{
						childNode = childNodeList.item(childRow);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName---->>> "+ childNodeName);
						if (childNodeName.equals("amd_no"))
						{
							amdNo = childNode.getFirstChild().getNodeValue();
							System.out.println("(amdNo-->>["+amdNo.length()+"]");
						}
						if (childNodeName.equals("line_no"))
						{
							lineNo = childNode.getFirstChild().getNodeValue();//
							System.out.println("lineNo1 -->>["+lineNo.length()+"]");
						}
						if (childNodeName.equals("acct_code__dr"))
						{
							if(childNode.getFirstChild()!= null)
							{
								acctCodeDr = childNode.getFirstChild().getNodeValue();
							System.out.println("acctCodeDr-->.["+acctCodeDr+"]");
							if(acctCodeDr==null)
							{
								System.out.println("VMACCDNULL is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMACCDNULL","","",conn);
								return errString;
								
							}
							else
							{
								sql = "select count(1) from accounts where acct_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,acctCodeDr);
								rs1 = pstmt.executeQuery();
								if (rs1.next())
								{
									cnt = rs1.getInt(1);
								}
								pstmt.close();
								rs1.close();
								pstmt = null;
								rs1 = null;
								if (cnt == 0)
								{
									errString = itmDBAccessEJB.getErrorString("","VMACTRINVL","","",conn);
									return errString;
								}
							}
							}
							else
							{
								System.out.println("VMACCDNULL is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMACCDNULL","","",conn);
								return errString;
							}
							
						}
						if (childNodeName.equals("cctr_code__dr"))
						{
							if(childNode.getFirstChild()!= null)
							{
							cctrCodeDr = childNode.getFirstChild().getNodeValue();
							System.out.println("cctrCodeDr-->.["+cctrCodeDr+"]");
							if(cctrCodeDr==null)
							{
								System.out.println("VMCCDNULL is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMCCDNULL","","",conn);
								return errString;
							}
							else
							{
								System.out.println("from accounts 3--->>");
								sql = "select count(1) from costctr where cctr_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,cctrCodeDr);
								rs1 = pstmt.executeQuery();
								if (rs1.next())
								{
									cnt = rs1.getInt(1);
								}
								pstmt.close();
								rs1.close();
								pstmt = null;
								rs1 = null;
								if (cnt == 0)
								{
									errString = itmDBAccessEJB.getErrorString("","CCTRDRINVL","","",conn);
									return errString;
								}
							}
							}
							else
							{
								System.out.println("VMCCDNULL is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMCCDNULL","","",conn);
								return errString;
							}
							
						}
						if (childNodeName.equals("acct_code__cr"))
						{
							if(childNode.getFirstChild()!=null)
							{
							acctCodeCr = childNode.getFirstChild().getNodeValue();
							System.out.println("acctCodeCr-->.["+acctCodeCr+"]");
							if(acctCodeCr ==null)
							{
								System.out.println("VMACCCNULL is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMACCCNULL","","",conn);
								return errString;
							}
							else//
							{
								sql = "select count(1) from accounts where acct_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,acctCodeCr);
								rs1 = pstmt.executeQuery();
								if (rs1.next())
								{
									cnt = rs1.getInt(1);
								}
								pstmt.close();
								rs1.close();
								pstmt = null;
								rs1 = null;
								if (cnt == 0)
								{
									errString = itmDBAccessEJB.getErrorString("","VMACCRINVL","","",conn);
									return errString;
								}
							}
							}
							else
							{
								System.out.println("VMACCCNULL is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMACCCNULL","","",conn);
								return errString;
								
							}
							
						}
						if (childNodeName.equals("cctr_code__cr"))
						{
							if(childNode.getFirstChild()!=null)
							{
							cctrCodeCr = childNode.getFirstChild().getNodeValue();
							System.out.println("cctrCodeCr--->>.["+cctrCodeCr+"]");
							 if(cctrCodeCr==null)
							{
								System.out.println("amdNo is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMCCCNULL","","",conn);
								return errString;
							}
							 else
							{
								sql = "select count(1) from costctr where cctr_code = ?";
								System.out.println("cctrCodeCr-->>["+cctrCodeCr+"]");
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,cctrCodeCr);
								rs1 = pstmt.executeQuery();
								if (rs1.next())
								{
									cnt = rs1.getInt(1);//
								}
								pstmt.close();
								rs1.close();
								pstmt = null;
								rs1 = null;
								if (cnt == 0)
								{
									errString = itmDBAccessEJB.getErrorString("","CCTRCRINVL","","",conn);
									return errString;
								}
							}
							}
							else
							{
								System.out.println("amdNo is Null...");
								errString = itmDBAccessEJB.getErrorString("","VMCCCNULL","","",conn);
								return errString;								
							}
							
						}
						System.out.println("updation 34--->>");
					}
					System.out.println("updation 34---["+lineNo1.length()+"] AND amdNo -- ["+amdNo+"]>>");
					if(amdNo!= null && lineNo!= null)
					{
						sql = "update poamd_det set ACCT_CODE__DR = ? ,CCTR_CODE__DR = ?,ACCT_CODE__CR = ?,CCTR_CODE__CR = ? where AMD_NO = ? and LINE_NO = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,acctCodeDr.trim());
						pstmt1.setString(2,cctrCodeDr.trim());
						pstmt1.setString(3,acctCodeCr.trim());
						pstmt1.setString(4,cctrCodeCr.trim());
						pstmt1.setString(5,amdNo.trim());
						pstmt1.setString(6,lineNo.trim());
						cnt = pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;
						if ( cnt > 0)
						{
							System.out.println("@@@@@@@@@ update successfully............");
						}
					}			
				}
				if (errString == null || errString.trim().length() == 0)//
				{
					conn.commit();
					System.out.println("transaction update succesfully........");
					errString = itmDBAccessLocal.getErrorString("","PROCCOMPLE","","",conn);
				}
				else
				{
					conn.rollback();
					System.out.println("Process failed........");
					errString = itmDBAccessLocal.getErrorString("","VTDESNCONF","","",conn);
				}
			}

		}
		catch (Exception e)
		{
			errString = e.getMessage();
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		finally
		{

			try
			{
				if(pRelHdr != null)
				{
					pRelHdr.close();
					pRelHdr = null;	
				}
				if(pRelDet != null)
				{
					pRelDet.close();
					pRelDet = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return errString;
	}

}