/********************************************************
	Title 	 : IncidentChargeIC[DI3LSUN001]
	Date  	 : 12/MAR/14
	Developer: Sagar Mane.
 ********************************************************/
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class IncidentChargeIC extends ValidatorEJB implements IncidentChargeICLocal, IncidentChargeICRemote
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
				System.out.println("xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2["+xmlString2+"]");
				System.out.println("dom2["+dom2+"]");
			}
			
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [IncidentChargeIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
    {
			String childNodeName = null;
			String errString = "";
			String errCode = "";
			String userId = "";
			String sql = "";
			String errorType = "";
			int cnt = 0,count=0;
			int ctr=0;
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
			int currentFormNo =0;
			String fromDay="",toDay="",incCharge="",penaltyType="",transistType="";
            double fromdayVal=0.0,todayVal=0.0,chargesAmt=0.0;
			try
			{
				System.out.println("@@@@@@@@ wfvaldata called");
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
				userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				if(objContext != null && objContext.trim().length()>0)
				{
					currentFormNo = Integer.parseInt(objContext);
				}
				switch(currentFormNo)
				{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
	
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
					  
					   if(childNodeName.equalsIgnoreCase("inc_chg"))
					   {    
						   incCharge=checkNull(genericUtility.getColumnValue("inc_chg", dom));
						   System.out.println(">>>>>incCharge:"+incCharge);
							//Inc. Charge code should not be Empty.
		                    if(incCharge==null || incCharge.trim().length()==0)
		                    {
		                    	errCode = "VMNULINCCD"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
		                    }
		                    else
		                    {
		                    	//check for valid Incidental Charges Code...
		                    	System.out.println(">>>>Check for gencode exist W_INC_CHARGES:"+incCharge);
		                    	sql="select count(1) from gencodes where MOD_NAME = ? and fld_name = ? and trim(fld_value) = trim(?)";		 				
		             			pstmt=conn.prepareStatement(sql);
		             			pstmt.setString(1, "W_INC_CHARGES");
		             			pstmt.setString(2, "INC_CHG");
		             			pstmt.setString(3, incCharge);
		             			rs=pstmt.executeQuery();
		             			if(rs.next())
		             			{
		             				count = rs.getInt(1);
		             			}
		             			rs.close();
		             			rs = null;
		             			pstmt.close();
		             			pstmt = null;
		             			if(count==0)
		             			{
		             				System.out.println(">>>>Check for gencode exist X:"+incCharge);
		             				sql="select count(1) from gencodes where MOD_NAME = ? and fld_name = ? and trim(fld_value) = trim(?)";						
		             				pstmt=conn.prepareStatement(sql);
		             				pstmt.setString(1, "X");
		             				pstmt.setString(2, "INC_CHG");
		             				pstmt.setString(3, incCharge);
		             				rs=pstmt.executeQuery();
		             				if(rs.next())
		             				{
		             					count = rs.getInt(1);
		             				}
		             			 }
		             			 if(count==0)
		             			 {
		             				errCode = "VMVLDCHGCD"; 
									errList.add(errCode);
								    errFields.add(childNodeName.toLowerCase());	
		             		     }
		                    }
					   }
					   if(childNodeName.equalsIgnoreCase("from_day"))
					   {    
							incCharge=checkNull(genericUtility.getColumnValue("inc_chg", dom));
							fromDay=checkNull(genericUtility.getColumnValue("from_day", dom));
						    System.out.println(">>>>>fromDay"+fromDay);
							System.out.println(">>>>>incCharge["+incCharge+"]");
							 if("A".equalsIgnoreCase(editFlag))
							 {
								 sql = "SELECT count(1) FROM inc_charges WHERE  inc_chg =? AND ? BETWEEN from_day AND to_day";	
								 pstmt =  conn.prepareStatement(sql);
							     pstmt.setString(1,incCharge);
								 pstmt.setString(2,fromDay);
								 rs = pstmt.executeQuery();
								 if(rs.next())
								 {
									cnt =  rs.getInt(1);
									System.out.println(">>>>>>>>>custName already exist cnt:"+cnt);
									
								  }
								  if(cnt > 0) 
								  {
									   // Error for From Day/Time already exists for the same Incidental Charges defined
									   System.out.println("//From Day/Time already exists for the same Incidental Charges defined:"+fromDay);
									   errCode = "VMFRMTMVLD"; 
									   errList.add(errCode);
									   errFields.add(childNodeName.toLowerCase());	
								  }
								  rs.close();
								  rs = null;
								  pstmt.close();
								  pstmt = null;
							  }
						 }
						 if(childNodeName.equalsIgnoreCase("to_day"))
						 {    
							toDay=checkNull(genericUtility.getColumnValue("to_day", dom));
							fromDay=checkNull(genericUtility.getColumnValue("from_day", dom));
							todayVal=Double.parseDouble(toDay);
							fromdayVal=Double.parseDouble(fromDay);
							System.out.println(">>>>>todayVal:"+todayVal);
							System.out.println(">>>>>fromdayVal:"+fromdayVal);
							if(todayVal!=0)
							{
								 if(todayVal < fromdayVal)
		                    	 {
		                    		//To Day/Time Can not be less then From Day/Time.
		                    		errCode = "VTFMTOTMVD"; 
									errList.add(errCode);
								    errFields.add(childNodeName.toLowerCase());	
		                    	 }
							}
						  }
						  if(childNodeName.equalsIgnoreCase("penalty_type"))
						  {    
							  penaltyType=checkNull(genericUtility.getColumnValue("penalty_type", dom));
							  System.out.println(">>>>>penaltyType:"+penaltyType);
							  //Penalty Type should not be Empty.
		                      if(penaltyType==null || penaltyType.trim().length()==0)
		                      {
		                    	 errCode = "VMNLPENLTY"; 
								 errList.add(errCode);
								 errFields.add(childNodeName.toLowerCase());	
		                      }
						  }
						  if(childNodeName.equalsIgnoreCase("transit_type"))
						  {   
							 transistType=checkNull(genericUtility.getColumnValue("transit_type", dom));
							 System.out.println(">>>>>transistType:"+transistType);
							 //Transit Type should not be Empty.
		                     if(transistType==null || transistType.trim().length()==0)
		                     {
		                    	 errCode = "VMNULTRTYP"; 
								 errList.add(errCode);
								 errFields.add(childNodeName.toLowerCase());	
		                     }
						  }
						  if(childNodeName.equalsIgnoreCase("charges"))
						  {   
							 chargesAmt=checkDoubleNull(genericUtility.getColumnValue("charges", dom));
							 System.out.println(">>>>>chargesAmt:"+chargesAmt);
							 //chargesAmt can not be negative.
		                     if(chargesAmt < 0)
		                     {
		                    	 errCode = "VMNGTVCHGS"; 
								 errList.add(errCode);
								 errFields.add(childNodeName.toLowerCase());	
		                     }
						  }
					}// end for
					break;  // case 1 end
				}
	
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
		}
	//end of validation
	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............."+xmlString);
		System.out.println("xmlString1............"+xmlString);
		System.out.println("xmlString2............"+xmlString);
		try
		{   
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [IncidentChargeIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}


	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
    {
	StringBuffer valueXmlString = new StringBuffer();
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null ;
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ConnDriver connDriver = new ConnDriver();
	
	int currentFormNo =0;
	String incCharge="",incDescr="";
	String columnValue="";
	
	try
	{
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String sysDate = sdf.format(currentDate.getTime());
		System.out.println("Now the date is :=>  " + sysDate);

		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		conn.setAutoCommit(false);
		connDriver = null;

		if(objContext != null && objContext.trim().length()>0)
		{
			currentFormNo = Integer.parseInt(objContext.trim());
		}

		valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
		valueXmlString.append(editFlag).append("</editFlag> </header>");

		System.out.println("**********NEW ITEMCHANGE FOR CASE"+currentFormNo+"**************");
		switch(currentFormNo)
		{
		case 1 :
		   valueXmlString.append("<Detail1>");
			System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
			if(currentColumn.trim().equalsIgnoreCase("inc_chg"))
			{	
				System.out.println(">>>>>>>>>>>>>>>>ITEMCHANGE FOR inc_chg>>>>>>>>");
				incCharge=checkNull(this.genericUtility.getColumnValue("inc_chg", dom));
				incDescr= this.getGenCodeDescr(conn, "INC_CHG", incCharge);
				valueXmlString.append("<descr>").append("<![CDATA["+incDescr+"]]>").append("</descr>");
			}
			valueXmlString.append("</Detail1>");
			break;
	    }	
		valueXmlString.append("</Root>");
	 }
	 catch(Exception e)
	 {
		e.printStackTrace();
		System.out.println("Exception ::"+ e.getMessage());
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
		}			
	 }
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
  
   private String getGenCodeDescr(Connection conn,String fieldName,String fieldVal) throws ITMException
   {
		String descr = "";
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try
		{ 
			System.out.println(">>>>Check for gencode W_FRT_LIST:"+fieldVal);
		    sql="select descr from gencodes where MOD_NAME = ? and fld_name = ? and trim(fld_value) = trim(?)";		 				
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, "W_INC_CHARGES");
			pstmt.setString(2, fieldName);
			pstmt.setString(3, fieldVal);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				descr=rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(descr==null || descr.trim().length() == 0 )
			{
				System.out.println(">>>>Check for gencode X:"+fieldVal);
				sql="select descr from gencodes where MOD_NAME = ? and fld_name = ? and trim(fld_value) = trim(?)";						
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, "X");
				pstmt.setString(2, fieldName);
				pstmt.setString(3, fieldVal);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					descr=rs.getString(1);
				}
			}
			descr=checkNull(descr);
			descr=descr.trim();
			System.out.println(">>>>>>getGenCodeDescr"+descr);
		}
		catch (SQLException se) 
		{
			System.out.println("[FreightListIC]	SQLException : getGenCodeDescr(): \n SQLException Occured =>>"+se.getMessage());
			se.printStackTrace();
			throw new ITMException(se);
		}
		catch (Exception e) 
		{
			System.out.println("[FreightListIC]	Exception : getGenCodeDescr(): \n Exception Occured =>>"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs!=null)
				{
					rs.close();
					rs=null;
				}
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (SQLException se) 
			{
				se.printStackTrace();
				throw new ITMException(se);
			}
		}
		return descr;
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
}	
