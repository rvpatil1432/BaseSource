
/********************************************************
	Title : PurMilstnValidatorEJB
	Date  : 24/07/2015
	Author: Aniket D. Bibave

 ********************************************************/

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless; // added for ejb3

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless // added for ejb3
public class PurMilstnValidatorEJB extends ValidatorEJB 
implements PurMilstnValidatorEJBLocal,PurMilstnValidatorEJBRemote {
	
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData() throws RemoteException,ITMException {
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException {
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException {
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";

		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 ) {
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			if(objContext != null && Integer.parseInt(objContext) == 1) {
				parentNodeList = dom2.getElementsByTagName("Header0");
				parentNode = parentNodeList.item(1);
				childNodeList = parentNode.getChildNodes();						
				for(int x=0;x<childNodeList.getLength();x++) {				
					childNode = childNodeList.item(x);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("Detail1")) {
						errString = wfValData(dom,dom1,dom2,"1",editFlag,xtraParams);
						if(errString != null && errString.trim().length()>0)
							break;
					} else if(childNodeName.equalsIgnoreCase("Detail2")) {
						errString = wfValData(dom,dom1,dom2,"2",editFlag,xtraParams);
						break;
					}
				}
			} else {
				errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return (errString);
	}


	public String itemChanged(Document dom, Document dom1,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException {
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag) throws RemoteException,ITMException {
		return "";
	}

	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException {
		System.out.println(":::IN wfValData():::"+this.getClass().getSimpleName());
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String userId = "";
		int currentFormNo=0;
		int childNodeListLength;
		ConnDriver connDriver = new ConnDriver();
		String compl_date ="";
		String tran_date = "";
		String date_format = "";
		String taskStatus="";

		double amount=0;
		String purcOrder="";
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			if(objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}

			switch(currentFormNo) {
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("compl_date")) 
					{
						compl_date = getColumnValue("compl_date",dom);
						tran_date = getColumnValue("tran_date",dom);
						date_format = getColumnValue("date_format",dom);
						date_format = date_format==null ? genericUtility.getApplDateFormat() : date_format.trim();
						
						taskStatus = getColumnValue("task_status",dom); // Code Added by Sagar on 24/11/15
						System.out.println(">>taskStatus:"+taskStatus);
						if(taskStatus== null || taskStatus.trim().length()== 0)
						{
							taskStatus="P";
						}
						if("C".equalsIgnoreCase(taskStatus.trim())) // Condition Added by Sagar on 24/11/15
						{
							if (compl_date == null || compl_date.trim().length() == 0) 
							{
								errCode = "VTCPLDTINV";//This is a pre existing err code
								errString = getErrorString("compl_date",errCode,userId);
								break;
							}
							else 
							{
								if(date_format.length() > 0)
								{
									boolean isBetweenDate = isBetweenDate(tran_date, compl_date, date_format);
									if(!isBetweenDate)
									{
										errCode = "VTCPLDTGRT";//This is a pre existing err code
										errString = getErrorString("compl_date",errCode,userId);
										break;
									}
								}
							}
						}
						
					}// end of compl_date

					else if (childNodeName.equalsIgnoreCase("amount")) 
					{
						System.out.println("@@@@@@ voucher amount Validate ............");

						amount = Double.parseDouble(getColumnValue("amount",dom)==null?"0":getColumnValue("amount",dom));

						purcOrder = getColumnValue("purc_order",dom);

						System.out.println("purcOrder["+purcOrder+"]amount["+amount+"]");

						String sql="" ; // tranIdForVoucher="",
						double vouchTotAmt=0;
						double ordAmt=0 ;

						sql = " select sum(net_amt)  from voucher where purc_order = ? " ;
						// "and confirmed = 'Y' " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							vouchTotAmt = rs.getDouble(1);
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select ord_amt from porder where purc_order= ? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ordAmt = rs.getDouble("ord_amt");
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						double differenceAmt = ordAmt - vouchTotAmt ;

						System.out.println("ordAmt["+ordAmt+"]-vouchNetAmt["+vouchTotAmt+"]=amount["+amount+"]>differenceAmt["+differenceAmt+"]");

						if(amount > differenceAmt)
						{
							errCode = "RELAMTGRT";//This is a pre existing err code
							errString = getErrorString("amount",errCode,userId);
							break;
						}
					}// end of compl_date
				}
				break;
			}			
		} catch(Exception e) {
			throw new ITMException(e);
		} finally {
			try {
				if(conn != null) {	
					conn.close();
					conn = null;
				}
				if(pstmt != null) {
					pstmt.close();
					pstmt =null;
				}
				if(rs != null) {
					rs.close();
					rs = null;
				}					
			} catch(Exception d) {
				throw new ITMException(d);
			}			
		}		
		return errString;
	}
	
	private boolean isBetweenDate(String tran_date, String compl_date, String date_format) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(date_format);
		Calendar cal = Calendar.getInstance();
		
		Date currentDate = cal.getTime();
		Date complDate = sdf.parse(compl_date);
		Date tranDate = sdf.parse(tran_date);
		
		//Changed Condition
		System.out.println("futureDate["+complDate+"].before["+currentDate+"]:::"+complDate.before(currentDate)+":::");
		if(complDate.before(tranDate) || complDate.after(currentDate)){
			return false;
		}
		return true;
	}
}