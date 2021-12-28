/*	
		Developed by	: Hatim Laxmidhar
		Started On		: 23/12/2005
		Purpose  		: This EJB will validate the data entered in the  Consume Issue Window.
*/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;

import org.w3c.dom.*;
import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3
import java.util.ArrayList;
import ibase.webitm.ejb.fin.FinCommon;

//public class ConsumeIssueEJB extends ValidatorEJB implements SessionBean // commented for ejb3
@Stateless // added for ejb3
public class ConsumeIssue extends ValidatorEJB implements ConsumeIssueLocal, ConsumeIssueRemote // added for ejb3
{
    E12GenericUtility genericUtility= new  E12GenericUtility();
    FinCommon finCommon = new FinCommon();
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
	}
	
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}*/
	
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
			Document dom = null;
			Document dom1 = null;
			String errString = "";
			try
			{
				dom = parseString(xmlString);
				dom1 = parseString(xmlString1); 
				errString = wfValData(dom,dom1,null,objContext,editFlag,xtraParams);
			}
			catch(Exception e)
			{
				System.out.println("Exception : ConsumeIssueEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			}
			return (errString);
	}


	public String wfValData(Document dom,Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException
	{

		String tranType = "", itemCode =  "", consOrd =  "", lineOrd =  "";
		String consIss =  "", lotNo =  "", lotSl =	 "", lineNo =   "", tranIdIss =  "";
		double quantity =  0d, ordQuantity = 0d, issQty = 0d, retQty = 0d, browQty = 0d;
		String userId = "";
		String errString = "";
		String sql =  "";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int currentFormNo = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
        int ctr;
        int childNodeListLength=0;
        String transer = "C-ISS", errCode = "", empCode = "", siteCodeOrd = "";
        ArrayList<String> errList = new ArrayList<String>();
        ArrayList<String> errFields = new ArrayList<String>();
        int cnt = 0;
        String errorType = "";
        StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
        
		try
		{
			userId = getValueFromXTRA_PARAMS(xtraParams,"userId");

			conn = getConnection(); 
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();

			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			stmt = conn.createStatement();
			switch (currentFormNo)
			{
//Added by Anagha R on 26/11/2020 for Consumption Order not showing on front
            case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
                childNodeList = parentNode.getChildNodes();
                childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();


					System.out.println("childNodeName ::["+childNodeName);

					if(childNodeName.equalsIgnoreCase("emp_code"))
					{
						empCode=checkNull(this.genericUtility.getColumnValue("emp_code", dom));
						siteCodeOrd=checkNull(this.genericUtility.getColumnValue("site_code__ord", dom));
                        errCode=finCommon.isEmployee(siteCodeOrd, empCode, transer, conn);						
                        if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} // end for
				break;	
//Added by Anagha R on 26/11/2020 for Consumption Order not showing on front
                case 2:
                System.out.println("Detail2_::"+childNodeListLength);
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("Child Node::"+childNodeName);

						if(childNodeName.equals("quantity"))
						{
							System.out.println("###################In the quantity####################");
							String sQty = genericUtility.getColumnValue("quantity", dom);
							quantity = (sQty == null?0:Double.parseDouble(sQty));
							itemCode = genericUtility.getColumnValue("item_code", dom);
							consOrd = genericUtility.getColumnValue("cons_order", dom);
							lineOrd = genericUtility.getColumnValue("line_no__ord", dom);
							consIss = genericUtility.getColumnValue("cons_issue", dom1);
							tranType = genericUtility.getColumnValue("tran_type", dom1);
							lotNo = genericUtility.getColumnValue("lot_no", dom);
							lotSl =	genericUtility.getColumnValue("lot_sl", dom);
							lineNo =  genericUtility.getColumnValue("line_no", dom);
							tranIdIss = genericUtility.getColumnValue("tran_id__iss", dom1);
							/*CHECKING FOR NULL (16/01/2006)*/
							/*if (itemCode ==null || itemCode.trim().length() ==0)
							{
								errString = getErrorString("item_code","",userId,errString);
								return errString;
							} 
							if (consOrd ==null || consOrd.trim().length() ==0)
							{
								errString = getErrorString("cons_order","VTINVCO",userId,errString);
								return errString;
							} 
							if (lineOrd ==null || lineOrd.trim().length() ==0)
							{
								errString = getErrorString("line_no__ord","VTINVCOLIN",userId,errString);
								return errString;
							} 
							if (consIss ==null)
							{
								consIss = "";
							} 
							if (tranType ==null || tranType.trim().length() ==0)
							{
								errString = getErrorString("tran_type","",userId,errString);
								return errString;
							} 
							if (lineNo ==null || lineNo.trim().length() ==0)
							{
								errString = getErrorString("line_no","",userId,errString);
								return errString;
                            } */
                            System.out.println("lotNo: "+lotNo+" lotSl"+lotSl);
							if (lotNo == null)
							{
                                //lotNo = "";
                                lotNo = "               ";//Changed by Anagha R on 25/11/2020 for Consumption Order not showing on front
							} 
							if (lotSl == null)
							{
                                //lotSl = "";
                                lotSl = "               "; //Changed by Anagha R on 25/11/2020 for Consumption Order not showing on front
							} 
							
							/*END*/
							System.out.println("Quantity : " + quantity );
	
							if (quantity <= 0)
							{
								errString = getErrorString("quantity","VTQTY",userId,errString);
								break;
							}
							else 
							{
								if (tranType.equalsIgnoreCase("I"))
								{
									System.out.println("TRAN TYPE : Issue");
									sql = "SELECT (CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END)" +
										" FROM CONSUME_ORD_DET WHERE CONS_ORDER = '" + consOrd + "'" + 
										" AND LINE_NO = " + lineOrd + "";
									System.out.println("sql:  " + sql);
									rs =  stmt.executeQuery(sql);
									if (rs.next())
									{
										ordQuantity = rs.getDouble(1);
										System.out.println("Order Quantity : " + ordQuantity);
										issQty = getConsIssRetQty(consOrd, lineOrd, consIss, lineNo, "I", lotNo, lotSl, conn);
										System.out.println("Issued Quantity : " + issQty);
										retQty = getConsIssRetQty(consOrd, lineOrd, consIss, lineNo, "R", lotNo, lotSl, conn);
										System.out.println("Return Quantity : " + retQty);
									}						
									//browQty takes value from brow. here it is assumed as zero
									if ((issQty - retQty - browQty + quantity) > ordQuantity)
									{
										if ((issQty - retQty + quantity) > ordQuantity)
										{
											errString = getErrorString("quantity","VTORDQTY",userId,errString);
											break;
										}
									}
									stmt.close();
								}
								else if (tranType.equalsIgnoreCase("R"))
								{
									System.out.println("TRAN TYPE : Return ");
									//To consider issue id for qty validation
									if ((tranIdIss == null) || (tranIdIss.trim().length() == 0))
									{
										
										stmt = conn.createStatement();
										sql = "SELECT SUM(CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) " +
											"FROM CONSUME_ISS_DET A, CONSUME_ISS B " + 
											"WHERE A.CONS_ISSUE = B.CONS_ISSUE " +
											"AND A.CONS_ORDER = '" + consOrd + "' " +
											"AND A.LINE_NO__ORD = " + lineOrd + " " +
											"AND A.LOT_NO = '" +  lotNo + "' " +
											"AND A.LOT_SL = '" +  lotSl	+ "' " +
											"AND (CASE WHEN B.TRAN_TYPE IS NULL THEN 'I' ELSE B.TRAN_TYPE END)  = 'I' " +
											"AND (CASE WHEN B.TRAN_TYPE IS NULL THEN 'N' ELSE B.TRAN_TYPE END)  = 'N' ";
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											issQty = rs.getDouble(1);
											System.out.println("Issue Quantity : " + issQty);
										}										
									}
									else
									{
										stmt = conn.createStatement();
										sql = "SELECT SUM(CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) " +
											"FROM CONSUME_ISS_DET A, CONSUME_ISS B " + 
											"WHERE A.CONS_ISSUE = B.CONS_ISSUE " +
											"AND A.CONS_ORDER = '" + consOrd + "' " +
											"AND A.LINE_NO__ORD = " + lineOrd + " " +
											"AND A.LOT_NO = '" +  lotNo + "' " +
											"AND A.LOT_SL = '" +  lotSl	+ "' " +
											"AND (CASE WHEN B.TRAN_TYPE IS NULL THEN 'I' ELSE B.TRAN_TYPE END)  = 'I' " +
											"AND (CASE WHEN B.TRAN_TYPE IS NULL THEN 'N' ELSE B.TRAN_TYPE END)  = 'N' " +
											"AND B.CONS_ISSUE = '" + tranIdIss + "' ";
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											issQty = rs.getDouble(1);
											System.out.println("Issue Quantity : " + issQty);
										}										
									}
									retQty = getConsIssRetQty(consOrd, lineOrd, consIss, lineNo, "R", lotNo, lotSl, conn);
									System.out.println("Return Quantity : " + retQty);
									System.out.println("retQty ::"+retQty+" quantity :: "+quantity+" :: issQty "+issQty);
									if ((retQty + quantity) > issQty)
									{
										errString = getErrorString("quantity","VTQTY4",userId,errString);
										break;
									}
								}								
							}
						}
					}
					break;
            }
//Added by Anagha R on 26/11/2020 for Consumption Order not showing on front            
            int errListSize = errList.size();
				cnt = 0;
				String errFldName = null;
				if(errList != null && errListSize > 0)
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
//Added by Anagha R on 26/11/2020 for Consumption Order not showing on front			
		}
		catch(Exception e)
		{
			System.out.println("Exception: [ConsumeIssueEJB]:wfValData(Document dom, Document dom1, String xtraParams) :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			
			if (conn != null)
			{
				try
				{
					conn.close();				
					conn=null;
				}
				catch (SQLException sqx)
				{
					conn=null;
					System.out.println("SQLException: [ConsumeIssueEJB]:wfValData(Document dom, Document dom1, String xtraParams) Finally:==>\n" + sqx.getMessage());
					throw new ITMException(sqx);

				}
			}
		}
		return errString;
	}

	// IF TRANTYPE == "I"
	private double getConsIssRetQty(String consOrd, String lineOrd, String consIss, String lineNo, String IssRet, String lotNo, String lotSl, Connection conn) throws SQLException, ITMException
	{
		double consIssRetQty = 0d;
		Statement stmt = null;
		ResultSet rs = null;
		String sql="";

		try
		{
			if (IssRet.equalsIgnoreCase("I"))
			{

				sql = "SELECT SUM(CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) " +
					"FROM CONSUME_ISS_DET A,	CONSUME_ISS B " + 
					"WHERE A.CONS_ISSUE = B.CONS_ISSUE " +
					"AND A.CONS_ORDER = '" + consOrd + "' " +
					"AND A.LINE_NO__ORD = " + lineOrd + " " +
					"AND (CASE WHEN B.TRAN_TYPE IS NULL THEN 'I' ELSE B.TRAN_TYPE END)  = 'I' " +
					"AND A.LOT_NO = '" +  lotNo + "' " +
					"AND A.LOT_SL = '" +  lotSl	+ "' " +
					"AND ((A.CONS_ISSUE <> '" + consIss + "') OR " +
					"		(A.CONS_ISSUE = '" + consIss + "' AND A.LINE_NO <> " + lineNo + "))";

				System.out.println("Tran Type Issue SQL :: "+sql);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					consIssRetQty = rs.getDouble(1);
					System.out.println("Tran Type : Issue :: consIssRetQty: " + consIssRetQty);
				}

			}
			else if (IssRet.equalsIgnoreCase("R"))
			{
				sql = "SELECT SUM(CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) " +
					"FROM CONSUME_ISS_DET A, CONSUME_ISS B " + 
					"WHERE A.CONS_ISSUE = B.CONS_ISSUE " +
					"AND A.CONS_ORDER = '" + consOrd + "' " +
					"AND A.LINE_NO__ORD = " + lineOrd + " " +
					"AND (CASE WHEN B.TRAN_TYPE IS NULL THEN 'I' ELSE B.TRAN_TYPE END)  = 'R' " +
					"AND A.LOT_NO = '" +  lotNo + "' " +
					"AND A.LOT_SL = '" +  lotSl	+ "' " +
					"AND ((A.CONS_ISSUE <> '" + consIss + "') OR " +
					"		(A.CONS_ISSUE = '" + consIss + "' AND A.LINE_NO <> " + lineNo + "))";

				System.out.println("Tran Type Return SQL :: "+sql);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					consIssRetQty = rs.getDouble(1);
					System.out.println("Tran Type : Return :: consIssRetQty: " + consIssRetQty);
				}
			}
		}
		catch (SQLException sqx)
		{
			System.out.println("SQLException : ConsumeIsueEJB : getConsIssRetQty(String consOrd, String lineOrd, String consIss, String lineNo, String IssRet, String lotNo, String lotSl, Connection conn)");
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("Exception : ConsumeIsueEJB : getConsIssRetQty(String consOrd, String lineOrd, String consIss, String lineNo, String IssRet, String lotNo, String lotSl, Connection conn)");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			if (rs!=null)
			{
				rs.close();
				rs = null;
			}
			if (stmt != null)
			{
				stmt.close();
				stmt=null;
			}
		}
		return consIssRetQty;
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
}