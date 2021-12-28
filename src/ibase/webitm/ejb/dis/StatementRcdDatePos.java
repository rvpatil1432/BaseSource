package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class StatementRcdDatePos extends ValidatorEJB implements StatementRcdDatePosLocal , StatementRcdDatePosRemote{

	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println("------------ postSave method called-111111111----------------StatementRcdDatePos : ");
		System.out.println("tranId111--->>["+tranId+"]");
		System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="";
		
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);				
				errString = postSave(dom,tranId,xtraParams,conn);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : StatementRcdDatePos.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}	
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("in StatementRcdDatePos tran_id---->>["+tranId+"]");
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		SimpleDateFormat sdf =null;
		Timestamp currDate = null , stmtrcdDate=null , stmtRecdDate = null;
		String sql = "" , errorString = "" , stmtRecdDateStr = "" ;
	
		String chgUser = "" , chgTerm = "" ,  userId =  "";
	   	String errString = "";
	   	String selectedValue = "";
		int cnt=0 , updateCount=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		boolean isSelect=false;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			
			chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
						
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new Timestamp(System.currentTimeMillis());
			System.out.println("TimeStamp>>>>>>>>>>"+currDate);
	
			NodeList parentList = dom.getElementsByTagName("Detail2");		
			System.out.println("parentList==="+parentList);
			System.out.println("len===["+parentList.getLength()+"]");
			if(parentList.getLength()==0  )
			{
				
				System.out.println(">No Data Selected"+isSelect);
				errString = itmDBAccessEJB.getErrorString("","VTNODATA","","",conn);
				return errString;
			}
			
			for (int ctr = 0; ctr < parentList.getLength(); ctr++)
			{
				Node detailListNode = parentList.item(ctr);
				NodeList detail2List= detailListNode.getChildNodes(); 
				System.out.println("detailListNode===="+detailListNode);
				if("Detail2".equalsIgnoreCase(detailListNode.getNodeName()))
				{
					System.out.println("detail2List===="+detail2List);
					for (int cntr = 0; cntr < detail2List.getLength(); cntr++) 
					{
						Node detail2Node = detail2List.item(cntr); 
						System.out.println("detail2Node===="+detail2Node);
						
						if(detail2Node != null &&  detail2Node.getNodeName().equalsIgnoreCase("attribute"))
						{
							System.out.println("Check for selected Value######");
							selectedValue = detail2Node.getAttributes().getNamedItem("selected").getNodeValue();
							System.out.println("selectedValue=========="+selectedValue);
						}
						if("tran_id".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								tranId =  detail2List.item(cntr).getFirstChild().getNodeValue();
								
							}
						}
						if("stmt_recd_date".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								stmtRecdDateStr =  detail2List.item(cntr).getFirstChild().getNodeValue();
								System.out.println("stmtrcdDate ::::::: "+stmtRecdDateStr);
								if(stmtRecdDateStr.trim().length() > 0) 
								{
									stmtrcdDate = Timestamp.valueOf(genericUtility.getValidDateString(stmtRecdDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								}
							}
						}
				  }
					System.out.println("Tran_id ::: "+tranId + " stmt_recd_date ::::::::::"+stmtrcdDate);
					sql = " UPDATE CUST_STOCK SET STMT_RECD_DATE = ? , CHG_TERM = ? , CHG_USER = ? , CHG_DATE = ? WHERE TRAN_ID = ? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setTimestamp(1, stmtrcdDate);
					pstmt.setString(2, chgTerm);
					pstmt.setString(3, chgUser);
					pstmt.setTimestamp(4, currDate);
					pstmt.setString(5, tranId);
					updateCount = pstmt.executeUpdate();
					cnt++;
					//updateCount++;
					pstmt.close();
					pstmt = null;
					System.out.println("cnt::::"+cnt+ "  updateCount::::::"+updateCount);
				}
			}
			if(cnt == parentList.getLength())
			{
				 errString = "";						
			}
			else
			{
				return errString = getErrorString("","VTFAILUPD", userId);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println(">>>>>In finally errString:"+errString);
				
				if(errString == null || errString.trim().length() == 0)
				{
					conn.commit();
				}
				else
				{
					conn.rollback();
				}
				if(rs != null)
				{	
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
}
