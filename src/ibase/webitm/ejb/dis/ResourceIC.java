
	/********************************************************
	Title ResourceIC[D16ASUN003]
	Date  : 12/04/16
	Developer: Abhijit Gaikwad

 ********************************************************/
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class ResourceIC extends ValidatorEJB implements TaxChapterICLocal, TaxChapterICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}

			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ResourceIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String lineNo = "";
		String ResId="";
		String childNodeName = null;
		String profileId="";
		String sql="";

		String errString = "";
		String errCode = "";
		String userId = "";
		String errorType = "";

		int ct=0;
		int count = 0;
		int ctr=0;
		int currentFormNo = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		System.out.println("editFlag="+editFlag);
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("currentFormNo>>>>>>>>>>>>>>>>>:"+currentFormNo);
			}
			switch(currentFormNo)
			{
			
			case 1 :
				System.out.println( "Detail 1 Validation called " );
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for( ctr = 0; ctr < childNodeListLength;ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					  if (childNodeName.equalsIgnoreCase("res_id"))
						{
					
							ResId = checkNull(this.genericUtility.getColumnValue("res_id", dom));
								if(ResId == null || ResId.trim().length() == 0  )
								{
									errString = itmDBAccessEJB.getErrorString("","VMNURESID ",userId,"",conn);
									break ;
								}
								else
								{

										sql = "select count ( *) from resources where res_id= ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, ResId);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											ct = rs.getInt(1);									
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										System.out.println("ct######"+ct);
										System.out.println("editFlag######"+editFlag);
										if (!editFlag.equalsIgnoreCase("E")) 
										{
											System.out.println("Flag is "+ editFlag);
										if (ct > 0)
										{
											errString = getErrorString(" ", "VMTRANIDEX", userId);				
											break;
										}
										}
								}
							}
				}
				break;
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					rs = null;
					if(pstmt != null )pstmt.close();
					pstmt =null;
					conn.close();
				}
				conn = null;
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}
		}
		System.out.println("ErrString ::"+errString);


		return errString;
	}
	
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input.trim();
	}


}	


