/********************************************************
	Title 	 : FreightListIC[DI3LSUN004]
	Date  	 : 15/MAR/14
	Developer: Sagar
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

public class FreightListIC extends ValidatorEJB implements FreightListICLocal, FreightListICRemote
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
			System.out.println("Exception : [FreightListIC][wfValData( String, String )] :==>\n" + e.getMessage());
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
			int cnt = 0;
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
			String currCode="",frtCharges="",loadType="",incCall="",incDlv="",incDtn="",effFrom="",validUpto="";
			String frtList="";
			Timestamp effFromDate=null, validUptoDate=null;
			try
			{
				System.out.println(">>>>>>>>>>wfvaldata called FreightListIC");
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
						
						
						if(childNodeName.equalsIgnoreCase("frt_list"))
						{    
							frtList=checkNull(genericUtility.getColumnValue("frt_list", dom));
						    System.out.println(">>>>>frtList Validation"+frtList);
							if(frtList==null || frtList.trim().length()==0 )
							{
								errCode = "VMNULLFRTL"; 
								errList.add(errCode);
							    errFields.add(childNodeName.toLowerCase());	
								
							}
							else
							{
								System.out.println(">>>>>frtList editFlag:"+editFlag);
								 if("A".equalsIgnoreCase(editFlag))
								 {
									 cnt= getDBRowCount(conn,"freight_list","frt_list",frtList.trim());
										System.out.println(">>>>>frtList Count"+cnt);
										if(cnt > 0)
										{
											//Error for.. Duplicate Freight List
											errCode = "VMDUPFRTLT"; 
											errList.add(errCode);
										    errFields.add(childNodeName.toLowerCase());	
										}
								 }
							}
							
						}
						if(childNodeName.equalsIgnoreCase("curr_code"))
						{    
							currCode=checkNull(genericUtility.getColumnValue("curr_code", dom));
							System.out.println(">>>>>currCode Validation:"+currCode);
		                    if(currCode!=null && currCode.trim().length()>0 )
		                    {
		                    	cnt= getDBRowCount(conn,"currency","curr_code",currCode);
		        				if(cnt==0)
		        				{
		        					errCode = "VMVLDCURNC"; 
									errList.add(errCode);
								    errFields.add(childNodeName.toLowerCase());	
		        				}
		                    }
		                    else
		                    {
		                    	//Currency code should not be Empty.
		                    	errCode = "VMNULLCURN"; 
								errList.add(errCode);
							    errFields.add(childNodeName.toLowerCase());	
		                    }
							
						}
						if(childNodeName.equalsIgnoreCase("frt_chgs_table"))
						{ 
							
					     	frtCharges=checkNull(genericUtility.getColumnValue("frt_chgs_table", dom));
							System.out.println(">>>>>frtCharges Validation:"+frtCharges);
		                    if(frtCharges==null || frtCharges.trim().length()==0)
		                    {
		                    	//Freight Charges Table should not be Empty.
		                    	errCode = "VMNULFRTBL"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
		                    }
		                    else
		                    {
		                    	//Check Freight Charges Table should be exist in freight_charges_table Table master.
		                    	cnt= getDBRowCount(conn,"freight_charges_table","frt_chgs_table",frtCharges);
		                    	if(cnt==0)
		                    	{
			        			    errCode = "VMVLDFRTTB"; 
								    errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
		                    	}
		                    }
		                    
						}
						if(childNodeName.equalsIgnoreCase("load_type"))
						{   
							loadType=checkNull(genericUtility.getColumnValue("load_type", dom));
							System.out.println(">>>>>loadType Validation:"+loadType);
							
		                    if(loadType==null || loadType.trim().length()==0)
		                    {  
		                    	//Load Type should not be Empty.
		                    	errCode = "VMNULLDTYP"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
		                    }
		                    else
		                    {
		                    	  frtCharges=checkNull(genericUtility.getColumnValue("frt_chgs_table", dom));
		                    	  if(frtCharges!=null && frtCharges.trim().length()>0 )
		                    	  {
		                    		    sql ="select count(1) from freight_charges_table where frt_chgs_table= ? and load_type = ? ";
				        				pstmt = conn.prepareStatement(sql);			
				        				pstmt.setString(1,frtCharges);
				        				pstmt.setString(2,loadType);			
				        				rs = pstmt.executeQuery();
				        				if(rs.next())
				        				{
				        					cnt =  rs.getInt(1);
				        				}
				        				rs.close();
				        				rs = null;
				        				pstmt.close();
				        				pstmt = null;
				        				if(cnt==0)
				        				{
				        					//Error for Freight Charges Table does not exists in master.
				        					errCode = "VMVLDFRTYP"; 
											errList.add(errCode);
										    errFields.add(childNodeName.toLowerCase());	
				        				}
		                    	  }
		                    }
						}
						if(childNodeName.equalsIgnoreCase("inc_chg__call"))
						{ 
							incCall=checkNull(genericUtility.getColumnValue("inc_chg__call", dom));
							System.out.println(">>>>>incCall Validation:"+incCall);
							if(incCall!=null && incCall.trim().length()>0 )
			                {
								cnt= getDBRowCount(conn,"inc_charges","inc_chg",incCall);
			        			if(cnt==0)
			        			{
			        				//Error for.. Call Incidental Charges doesnot exists in master.
			        			    errCode = "VMVLDCALI1"; 
								    errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
			        			}
			        				
			                 }
			                 else
			                 {
			           				//Error for.. Call Incidental Charges should not be Empty.
			                    	errCode = "VMNULCALI2"; 
									errList.add(errCode);
								    errFields.add(childNodeName.toLowerCase());	
			                  }
						}
						if(childNodeName.equalsIgnoreCase("inc_chg__dlv"))
						{ 
							incDlv=checkNull(genericUtility.getColumnValue("inc_chg__dlv", dom));
							System.out.println(">>>>>incDlv Validation:"+incCall);
							if(incDlv!=null && incDlv.trim().length()>0 )
			                {
								cnt= getDBRowCount(conn,"inc_charges","inc_chg",incDlv);
			        			if(cnt==0)
			        			{
			        			    errCode = "VMVLDDLVCG"; 
								    errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
			        			}
			        				
			                  }
			                  else
			                  {
			                	 //Error for Delivery Incidental Charges should not be Empty.
			                    	errCode = "VMNULDLVCG"; 
									errList.add(errCode);
								    errFields.add(childNodeName.toLowerCase());	
			                  }
		                    
						 }
						if(childNodeName.equalsIgnoreCase("inc_chg__dtn"))
						{ 
							incDtn=checkNull(genericUtility.getColumnValue("inc_chg__dtn", dom));
							System.out.println(">>>>>incDtn Validation:"+incDtn);
							if(incDtn!=null && incDtn.trim().length()>0 )
			                {
								cnt= getDBRowCount(conn,"inc_charges","inc_chg",incDtn);
			        			if(cnt==0)
			        			{
			        				errCode = "VMVLDDETCG"; 
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
			        			}
			                 }
			                 else
			                 {
			                    errCode = "VMNULDETCG"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
			                 }
						}
						if(childNodeName.equalsIgnoreCase("eff_from"))
						{ 
					     	effFrom=checkNull(genericUtility.getColumnValue("eff_from", dom));
							System.out.println(">>>>>effFrom Date Validation:"+effFrom);
		                    if(effFrom==null || effFrom.trim().length()==0)
		                    {
		                    	errCode = "VMEFROMDTE"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
		                    }
		                    
						}
						if(childNodeName.equalsIgnoreCase("valid_upto"))
						{ 
					     	validUpto=checkNull(genericUtility.getColumnValue("valid_upto", dom));
					    	effFrom=checkNull(genericUtility.getColumnValue("eff_from", dom));
							System.out.println(">>>>>validUpto Date Validation:"+validUpto);
		                    if(validUpto==null || validUpto.trim().length()==0)
		                    {
		                    	errCode = "VMVLDTODTE"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
		                    }
		                    else
		                    {
		                    	if(effFrom!=null && effFrom.trim().length()>0)
		                    	{
		                    		effFromDate = Timestamp.valueOf(genericUtility.getValidDateString(effFrom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								    validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(validUpto, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
									System.out.println("comapring effFrom>>>>>>>>"+effFromDate);
									System.out.println("comapring validUpto>>>>>>>>"+validUptoDate);
				                    if(validUptoDate.before(effFromDate))
				                    {
				                      //Valid Upto Date should not be less than Effective From Date.
				                       System.out.println(">>>>Valid upto date is less than Eff From date:");
					                   errCode = "VMVLDFRMTO"; 
									   errList.add(errCode);
									   errFields.add(childNodeName.toLowerCase());	
				                     }
		                    	  }
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
	// Start method for item change
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
			System.out.println("Exception : [FreightListIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
	   return valueXmlString;
	}
	
	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
	String childNodeName = null;
	String sql = "";
	StringBuffer valueXmlString = new StringBuffer();
	int ctr = 0;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null ;
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ConnDriver connDriver = new ConnDriver();
	
	int currentFormNo =0;
	String currCode="",currDescr="",loadType="",loadDescr="",incCall="",incCallDescr="",incDlv="",incDlvDescr="",incDtn="",incDtnDescr="";
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
		System.out.println(">>>>>>>>>>wfvaldata called FreightListIC");
		System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
		switch(currentFormNo)
		{
		case 1 :
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			ctr = 0; 
			valueXmlString.append("<Detail1>");
			int childNodeListLength = childNodeList.getLength();
			do
			{ 
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equals(currentColumn))
				{
					if (childNode.getFirstChild()!= null)
					{
						columnValue = childNode.getFirstChild().getNodeValue().trim();
					}
				}
				ctr++;
			}
			while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
			System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
			if(currentColumn.trim().equalsIgnoreCase("curr_code"))
			{	
				System.out.println(">>>>>>>>>>>>>>>>ITEMCHANGE FOR curr_code>>>>>>>>");
				currCode=checkNull(this.genericUtility.getColumnValue("curr_code", dom));
				sql = "select descr from currency where curr_code =?";
				pstmt = conn.prepareStatement(sql);			
				pstmt.setString(1,currCode);			
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					currDescr = rs.getString("descr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<currency_descr>").append("<![CDATA[").append(checkNull(currDescr)).append("]]>").append("</currency_descr>\r\n");
				
								
			}
			if(currentColumn.trim().equalsIgnoreCase("load_type"))
			{	
				System.out.println(">>>>>>>>>>>>>>>>ITEMCHANGE FOR load_type>>>>>>>>");
				loadType=checkNull(this.genericUtility.getColumnValue("load_type", dom));
				loadDescr= this.getGenCodeDescr(conn, "LOAD_TYPE", loadType);
				valueXmlString.append("<gencodes_descr_3>").append("<![CDATA[" + loadDescr + "]]>").append("</gencodes_descr_3>");
			}
			if(currentColumn.trim().equalsIgnoreCase("inc_chg__call"))
			{	
				System.out.println(">>>>Item change inc_chg__call:");
				incCall=checkNull(this.genericUtility.getColumnValue("inc_chg__call", dom));
				incCallDescr= this.getGenCodeDescr(conn, "INC_CHG", incCall);
				valueXmlString.append("<gencodes_descr>").append("<![CDATA[" + incCallDescr + "]]>").append("</gencodes_descr>");
			}
			if(currentColumn.trim().equalsIgnoreCase("inc_chg__dlv"))
			{	
				incDlv=checkNull(this.genericUtility.getColumnValue("inc_chg__dlv", dom));
				incDlvDescr= this.getGenCodeDescr(conn, "INC_CHG", incDlv);
				valueXmlString.append("<gencodes_descr_1>").append("<![CDATA[" + incDlvDescr + "]]>").append("</gencodes_descr_1>");
			}
			if(currentColumn.trim().equalsIgnoreCase("inc_chg__dtn"))
			{	
				incDtn=checkNull(this.genericUtility.getColumnValue("inc_chg__dtn", dom));
				incDtnDescr= this.getGenCodeDescr(conn, "INC_CHG", incDtn);
				valueXmlString.append("<gencodes_descr_2>").append("<![CDATA[" + incDtnDescr + "]]>").append("</gencodes_descr_2>");
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
			pstmt.setString(1, "W_FRT_LIST");
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
				if(rs!=null){
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
    
   private int getDBRowCount(Connection conn, String table_name, String whrCondCol, String whrCondVal) throws ITMException
   {
		int count=-1;
		if(conn!=null)
		{
			ResultSet rs=null;
			PreparedStatement pstmt = null;
			String sql="select count(1) from "+table_name+" where "+whrCondCol+" = ?";
			System.out.println("SQL in getDBRowCount method : "+sql);
			try
			{
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,whrCondVal);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
			}
			catch(SQLException e)
			{
				System.out.println("SQL Exception In getDBRowCount method of FreightListIC Class : "+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			}
			catch(Exception ex)
			{
				System.out.println("Exception In getDBRowCount method of FreightListIC Class : "+ex.getMessage());
				ex.printStackTrace();
				throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
			}
			finally
			{
				
				try{
					
					if(pstmt!=null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs!=null)
					{
						rs.close();
						rs = null;
					}
				}catch (SQLException se) 
				{
					se.printStackTrace();
				}
			}
		}
		else
		{
			try 
			{
				throw new SQLException("Connection passed to FreightListIC.getDBRowCount() method is null");
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		 }
	return count;
  }
}	
