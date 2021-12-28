/********************************************************
	Title : TaxChapterIC[DI3HSUN022]
	Date  : 22/11/13
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
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

public class TaxChapterIC extends ValidatorEJB implements TaxChapterICLocal, TaxChapterICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
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
			System.out.println("Exception : [TaxChapter][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String description = "";
		String taxchap="";
		String childNodeName = null;
		String keyFlag="";
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

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();


				for( ctr = 0; ctr < childNodeListLength;ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("descr"))
					{   
						description = checkNull(genericUtility.getColumnValue("descr", dom));
						System.out.println("description="+description);
						if(description == null || description.trim().length() == 0)
						{
							errCode = "VMDESCR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}					
					}else
						if (childNodeName.equalsIgnoreCase("tax_chap"))
						{
					
							taxchap= checkNull(this.genericUtility.getColumnValue("tax_chap", dom));

							sql = "select key_flag from transetup where tran_window='w_taxchap' ";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyFlag = rs.getString("key_flag");									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if("M".equalsIgnoreCase(keyFlag) && "A".equalsIgnoreCase(editFlag))
							{
								if(taxchap == null || taxchap.trim().length() == 0  )
								{
									errCode = "VMTCHAP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									sql = " select count ( *) from taxchap where tax_chap = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, taxchap);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct = rs.getInt(1);									
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(ct > 0)
									{
										errCode = "VMDUPTCHP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							
						}
				}
				int errListSize = errList.size();
				count = 0;
				String errFldName = null;
				if(errList != null && errListSize > 0)
				{
					for(count = 0; count < errListSize; count ++)
					{
						errCode = errList.get(count);
						errFldName = errFields.get(count);
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
				break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
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
					conn.close();
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation


	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input.trim();
	}

	private String errorType(Connection conn , String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		}		
		finally
		{
			try
			{
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
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		return msgType;
	}



}	
