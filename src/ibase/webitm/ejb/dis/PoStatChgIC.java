package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
@Stateless
public class PoStatChgIC extends ValidatorEJB  implements PoStatChgICLocal,PoStatChgICRemote{


	
	E12GenericUtility genericUtility=new E12GenericUtility();

	public String wfValData() throws RemoteException, ITMException {
		return "";
	}

	public String itemChanged() throws RemoteException, ITMException {
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		System.out.println("Validation Start..........");
		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : EmpPoLimit : wfValData(String xmlString) : ==>\n"+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		String errString = " ",confirmed="";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errCode = null;
		String userId = null;
		int cnt = 0,gimCnt=0;
		int ctr = 0;
		String purcOrder="";
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Timestamp effDate=null;
		Timestamp validDate=null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String status="";
		//Timestamp eff_from1=null, valid_upto1=null;
		ConnDriver connDriver = new ConnDriver();
		try {
			System.out.println("wfValData called");
			conn = getConnection();
			/*conn = connDriver.getConnectDB("DriverITM");*/
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					System.out.println("COUNT OF CHILD NODE LIST"+childNodeListLength);
					System.out.println("COUNT OF CHILD NODE LIST(ctr<childnodelist)"+ctr);
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNode@@"+childNode);
					int seriCount = 0;
				      
				    				  	
					
				    if (childNodeName.equalsIgnoreCase("purc_order")) 
					{
				    	purcOrder = genericUtility.getColumnValue("purc_order",dom);
				    	if (purcOrder == null) 
					     {
					    	 errString = getErrorString(" ", "VTPUREPT", userId);
								break;
					     }
				    	
				    	if(purcOrder != null && purcOrder.trim().length() > 0)
						{
				    		
							sql = "select count(1) from porder where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							
							sql = "select confirmed from PORDER_STAT_CHG where purc_order = ? and confirmed='U'";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								//status = rs.getString(1);
								confirmed=rs.getString(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							System.out.println("confirmed@postatchg@@"+confirmed);
							if(confirmed.equals("U")||confirmed.equalsIgnoreCase("U"))
							{
								errCode = "VMUNCPO";
								errString = getErrorString("purc_order",
								errCode, userId);
								break;
								
							}
							
							sql = "select status,confirmed from porder where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								status = rs.getString(1);
								confirmed=rs.getString(2);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							sql = "select count(1) from porddet where purc_order = ? and (dlv_qty<quantity) and status='C' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								gimCnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							
							
							if (cnt == 0) {
								errCode = "VMPURINV";
								errString = getErrorString("purc_order",
								errCode, userId);
								break;
							}
							if (confirmed.equals("N")) {
								System.out.println("not confirmed po");
								errCode = "VTPOUNC";
								errString = getErrorString("purc_order",
								errCode, userId);
								break;
							}
							
							
							
							if (status.equals("O") ) {
								
								if(gimCnt>0)
								{
									System.out.println("allow to open@gimCnt>0");
									
									break;
								}
								System.out.println("Already open");
								errCode = "VMPOENC";
								errString = getErrorString("purc_order",
								errCode, userId);
								break;
							}
							
							
							if(gimCnt==0 && status.equals("C")){
								//only for one indent and one line of PO with full RCP
								System.out.println("Gim count found");
								errCode = "VTLNDNF";
								errString = getErrorString("purc_order",
								errCode, userId);
								break;
							}
							
							
						}
				    	
				    	
				    	
				    	
					} /*				    
				    if (!editFlag.equalsIgnoreCase("E")) {
				    	
				    	System.out.println("Comming to !editFlag condition");
				    sql=null;
				    sql=" select COUNT(1) from emp_po_limit where emp_code=? and item_ser=? and ( ( ? BETWEEN eff_from AND valid_upto ) OR (? BETWEEN eff_from AND valid_upto ))"; 
                  	pstmt = conn.prepareStatement(sql);
					 pstmt.setString(1,empCode);
			         pstmt.setString(2,itemSeries);
			         pstmt.setTimestamp(3,effDate);	         
			         pstmt.setTimestamp(4,validDate);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}					
					 System.out.println("CHECKING DUPLICATE PERIOD"+cnt);
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
					if(cnt > 0) {
						
						  errString = itmDBAccessEJB.getErrorString("","VMRECD ",userId);
                            break ;
					}
				    }*/
				     					
				}
				break;
				}
			

		} catch (Exception e) {
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			errCode = "VALEXCEP";
			errString = getErrorString("", errCode, userId);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (conn != null) {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}

					if (rs != null) {
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
			System.out.println(" < PoStatChgEJB > CONNECTION IS CLOSED");
		}
		System.out.println("@@@@@@@@dhiraj@@@@@@@@@@ErrString ::" + errString);

		return errString;
	}// END OF VALIDATION

	@Override
	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		Document dom1 = null;
		Document dom = null;
		Document dom2 = null;
		String valueXmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0) {
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception :PoStatChgEJBValid:itemChanged(String,String,String,String,String,String):"
							+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("returning from PoStatChgEJBValid itemChanged");
		return (valueXmlString);
	}

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		StringBuffer valueXmlString = new StringBuffer();

		Connection conn = null;
		PreparedStatement pStmt = null;
		Statement Stmt=null;
		ResultSet rs = null;
		String sql = "";
		int currentFormNo = 0;

		//GenericUtility genericUtility;
		String siteCode = "", sitedescr = "";
		String purcOrder="",suppName="";
		
		String tranDate="",empCodeApr="",suppCode="",purcOrder1="",totAmt="";
		String empFname="",empMname="",empLname="",confirmed="",status="";
		double totAmt1=0.0;
		String itemSerDescr="",empCode="",descr="",empCode1="",siteDescr="";
		String siteCodeOrd="",ordDescrSite="",siteCodeDlv="",dlvDescrSite="";
		//SimpleDateFormat sdf  = null;
		Timestamp today = null;
		java.sql.Timestamp confDate=null;
		String tran_Date=null;
		try {
			//genericUtility = genericUtility.getInstance();
			// siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDate);
			ConnDriver conndriver = new ConnDriver();
			//conn = conndriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			conndriver = null;
			if (objContext != null && objContext.trim().length() > 0)
				currentFormNo = Integer.parseInt(objContext);

			currentColumn = currentColumn == null ? "" : currentColumn.trim();
			System.out.println("currentColumn : " + currentColumn);
			valueXmlString = new StringBuffer(
					"<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("current form no: " + currentFormNo);
			System.out.println("dom:::::::::: : "
					+ genericUtility.serializeDom(dom));
			System.out.println("dom11111111111111:::::::::: : "
					+ genericUtility.serializeDom(dom1));
			System.out.println("dom222222222222222:::::::::: : "
					+ genericUtility.serializeDom(dom2));
			String loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			String chguserhdr = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			String chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			//loginEmpCode
			Timestamp currDate = getCurrdateAppFormat();
			String loginEmpCode = getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			
		
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				if ("itm_default".equalsIgnoreCase(currentColumn)) {
					System.out.println("itm_default : ");
					System.out.println("Content of xtraParams ..> "
							+ xtraParams);
					
				/*	String tranId="";
					try
				     {
						
						String windowName="W_POSTAT_CHG";
				    	sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)= '"+windowName+"'";
				    	Stmt = conn.createStatement();
						rs = Stmt.executeQuery(sql);
						
						String tranSer1 = "";
						String keyString = "";
						String keyCol = "";
						if (rs.next())
						{
							keyString = rs.getString(1);
							keyCol = rs.getString(2);
							tranSer1 = rs.getString(3);				
						}
						System.out.println("keyString :"+keyString);
						System.out.println("keyCol :"+keyCol);
						System.out.println("tranSer1 :"+tranSer1);
						//System.out.println("xmlValues  :["+xmlValues+"]");
						TransIDGenerator tg = new TransIDGenerator(dom, "SYSTEM", CommonConstants.DB_NAME);
						tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
						System.out.println("tranId :"+tranId);
						
						rs.close();
						rs = null;
						Stmt.close();
						Stmt = null;
					}
					catch (SQLException ex)
					{
						System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
						ex.printStackTrace();		
			            throw new ITMException(ex);
					}
					catch (Exception e)
					{
						System.out.println("Exception ::" + e.getMessage() + ":");
						e.printStackTrace();
			            throw new ITMException(e);
					}
					*/
					
					
					
					
					
					if (loginSite != null  || loginEmpCode!=null  )
					
					{
						sql = "select descr from site where site_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, loginSite);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							siteDescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						sql = "select emp_fname,emp_mname,emp_lname from employee where emp_code = ? ";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, loginEmpCode);
						rs = pStmt.executeQuery();
						if (rs.next())
						{
							empFname = rs.getString(1);
							empMname = rs.getString(2);
							empLname = rs.getString(3);
							
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
					}
					
					/*StockUpdate stockupd=new StockUpdate();
					TransIDGenerator tg=new TransIDGenerator(xmlString, loginUser, database);
				*/
					
					//loginEmpCode tran_date
					///tranId
					
					//valueXmlString.append("<tran_id>").append("<![CDATA[" + tranId + "]]>").append("</tran_id>");
					
					valueXmlString.append("<tran_date>").append("<![CDATA[" + sysDate + "]]>").append("</tran_date>");
					
					valueXmlString.append("<site_code>").append("<![CDATA[" + loginSite + "]]>").append("</site_code>");
					
					valueXmlString.append("<descr>").append("<![CDATA[" + siteDescr + "]]>").append("</descr>");
				
					valueXmlString.append("<emp_code__aprv>").append(loginEmpCode)
					.append("</emp_code__aprv>\r\n");
					
					valueXmlString.append("<emp_code__aprv>").append("<![CDATA[" + loginEmpCode + "]]>").append("</emp_code__aprv>");
					
					valueXmlString.append("<fname>").append("<![CDATA[" + empFname + "]]>").append("</fname>");
					valueXmlString.append("<mname>").append("<![CDATA[" + empMname + "]]>").append("</mname>");
					valueXmlString.append("<lname>").append("<![CDATA[" + empLname + "]]>").append("</lname>");
					
		
					valueXmlString.append("<conf_date>").append("<![CDATA[" + sysDate + "]]>").append("</conf_date>");
					
					valueXmlString.append("<add_date>").append("<![CDATA[" + sysDate + "]]>").append("</add_date>");
					
					
					// tranDate = (sdf.format(timestamp).toString()).trim();
				}
				
				
				 if (currentColumn.trim().equalsIgnoreCase("purc_order")) {
					purcOrder = genericUtility.getColumnValue("purc_order", dom);
					tranDate = genericUtility.getColumnValue("tran_date", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom);
					empCodeApr = genericUtility.getColumnValue("emp_code_apr", dom);
					suppCode= genericUtility.getColumnValue("supp_code", dom);
					
					
					sql = " select tot_amt,emp_code__aprv,supp_code,site_code__dlv,confirmed,conf_date,status from porder where purc_order=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, purcOrder);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						 totAmt1 = rs.getDouble(1);
						 empCodeApr=rs.getString(2);
										 
						 suppCode=rs.getString(3);
						 siteCode=rs.getString(4);
						 confirmed=rs.getString(5);
						 confDate=rs.getTimestamp(6);
						 status=rs.getString(7);
						 
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;

					
					
					sql = " select emp_fname,emp_mname,emp_lname from employee where emp_code=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, empCodeApr);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						 empFname = rs.getString(1);
						 empMname = rs.getString(2);
						 empLname = rs.getString(3);
						
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					sql = " select supp_name from supplier where supp_code=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, suppCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						 suppName = rs.getString(1);
						 
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					
					sql = " select site_code__ord,site_code__dlv from porder where purc_order=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, purcOrder);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						siteCodeOrd = rs.getString(1);
						siteCodeDlv = rs.getString(2);
						 
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					sql = " select site_code,descr from site where site_code=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeOrd);
					
					rs = pStmt.executeQuery();
					if (rs.next()) {
						siteCodeOrd = rs.getString(1);
						ordDescrSite = rs.getString(2);
						 
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					sql = " select site_code,descr from site where site_code=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, siteCodeDlv);
					
					rs = pStmt.executeQuery();
					if (rs.next()) {
						siteCodeDlv = rs.getString(1);
						dlvDescrSite = rs.getString(2);
						 
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
				
					
					
					System.out.println("totAmt1@@" + totAmt1);
					
					
                    valueXmlString.append("<site_code>").append("<![CDATA[" + siteCodeDlv + "]]>").append("</site_code>");
					
					valueXmlString.append("<descr>").append("<![CDATA[" + dlvDescrSite + "]]>").append("</descr>");
				
					valueXmlString.append("<site_code__ord>").append("<![CDATA[" + siteCodeOrd + "]]>").append("</site_code__ord>");
						
					valueXmlString.append("<site_descr>").append("<![CDATA[" + ordDescrSite + "]]>").append("</site_descr>");
					
					
                    valueXmlString.append("<emp_code__aprv>").append("<![CDATA[" + loginEmpCode + "]]>").append("</emp_code__aprv>");
					
					valueXmlString.append("<fname>").append("<![CDATA[" + empFname + "]]>").append("</fname>");
					valueXmlString.append("<mname>").append("<![CDATA[" + empMname + "]]>").append("</mname>");
					valueXmlString.append("<lname>").append("<![CDATA[" + empLname + "]]>").append("</lname>");
					
					valueXmlString.append("<supp_code>").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");
					
					valueXmlString.append("<supp_name>").append("<![CDATA[" + suppName + "]]>").append("</supp_name>");
					
					valueXmlString.append("<tot_amt>").append("<![CDATA[" + totAmt1 + "]]>").append("</tot_amt>");
					
					/*valueXmlString.append("<status>").append("<![CDATA[" + status + "]]>").append("</status>");
					
					valueXmlString.append("<confirmed>").append("<![CDATA[" + confirmed + "]]>").append("</confirmed>");
					*/
					valueXmlString.append("<conf_date>").append("<![CDATA[" + sysDate + "]]>").append("</conf_date>");
					
			

				}
				
				valueXmlString.append("</Detail1 >");
				break;
			}
			valueXmlString.append("</Root>\r\n");
		} catch (Exception e) {
			System.out
					.println("Exception :PoStatChgIC•@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@••••••(Document,String):"
							+ e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}
		}
		System.out.println("\n***** ValueXmlString :" + valueXmlString
				+ ":*******");
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
		
		private java.sql.Timestamp getCurrdateAppFormat() throws ITMException
		{
			String currAppdate ="";
			java.sql.Timestamp currDate = null;
			try
			{
					Object date = null;
					currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
					System.out.println(genericUtility.getDBDateFormat());
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
					date = sdf.parse(currDate.toString());
					currDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			}
			catch(Exception e)
			{
				System.out.println("Exception in :::calcFrsBal"+e.getMessage());
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
			return currDate;
			//LINE COMMENTED BY MSALAM AS TIMESTAMP IS NEEDED
			//return (currAppdate);
		}




}
