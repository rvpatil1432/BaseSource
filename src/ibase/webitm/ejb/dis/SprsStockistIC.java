package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import ibase.utility.E12GenericUtility;

@Stateless

public class SprsStockistIC  extends ValidatorEJB implements SprsStockistICLocal,SprsStockistICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("------------ wfvalData method called-----------------");
		System.out.println("xmlString --->>>  [["+xmlString+"  ]]");
		System.out.println("xmlString1 --->>>  [["+xmlString1+"  ]]");
		System.out.println("xmlString2 --->>>  [["+xmlString2+"  ]]");
		System.out.println("editFlag --->>>  [["+editFlag+"  ]]");
		
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : SprsStockist.java : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		
		return errString;
	} //end of wfValData 

	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;	
		int ctr=0;
		int cnt=0;	
		String childNodeName = null;
		String custCode = "",sprsCode = "",routeId="",userId = "",sql = "";
		String errCode="",errString="",errorType="";
		int childNodeListLength;
	
		Connection conn = null;
		PreparedStatement pstmt = null;		
		ResultSet rs=null;
		
		int currentFormNo=0;
		
		ConnDriver connDriver = new ConnDriver();
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		try
		{  
			System.out.println("wfvaldata called!!!!! (SprsStockistIC) ");			
			conn = getConnection();
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		  
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
					System.out.println("Child name --->> "+childNodeName);	
					//Validation for cust code
					if (childNodeName.equalsIgnoreCase("cust_code")) 
							{
							custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
							sprsCode = checkNull(genericUtility.getColumnValue("sprs_code",dom));
							
							System.out.println("custCode--->["+custCode+"]");
							System.out.println("sprsCode--->["+sprsCode+"]");
							if(custCode == null || custCode.length()==0)
							{
								errCode = "NULLCUSTCD";//customer code is left blank
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							else
							{
								sql = "select count(*) from customer where cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);					  
								rs = pstmt.executeQuery();
								
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Count value for cust_code--->"+cnt);	
								if (cnt == 0) 
								  {
							      errCode = "VTINCOD"; //Customer code does not exists in the customer master.Please enter another cust code
							      errList.add(errCode);
							      errFields.add(childNodeName.toLowerCase());
						          }
								
								if ("A".equalsIgnoreCase(editFlag)) 
								{
								sql = "select count(*) from sprs_stockist where sprs_code = ? and cust_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, sprsCode);
						        pstmt.setString(2, custCode);
								rs = pstmt.executeQuery();
								
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt > 0) 
								  {
							      errCode = "VTCUSTCD"; //Customer code with the same sprs code already exists.Please enter another cust code
							      errList.add(errCode);
							      errFields.add(childNodeName.toLowerCase());
						          }
								}//end of if loop for edit flag E
								
						      }//end of else for cust code
						    }//end of if loop for cust code
			
					
					if (childNodeName.equalsIgnoreCase("sprs_code"))
					{
						sprsCode = checkNull(genericUtility.getColumnValue("sprs_code",dom));
						System.out.println("sprsCode--->["+sprsCode+"]");
					
						if(sprsCode == null || sprsCode.length()==0)
						{
							errCode = "VMSLPERNLL";//sales person code is left blank
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}//end of if inside sprs_code
						else {
							

							sql= "select count(*) from sales_pers where sales_pers = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,sprsCode);
							rs=pstmt.executeQuery();
							if(rs.next()){
								cnt=rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count value for sprsCode--->"+cnt);	
							if(cnt == 0)
							{
							errCode = "VMSLPERS1 ";//Invalid sales person code.Enter a sales person code
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );	
							}
							
						
							}											
					}//end of if loop for sprs_code
			
					 if (childNodeName.equalsIgnoreCase("route_id"))
					 {
					        routeId = checkNull(genericUtility.getColumnValue("route_id",dom));
					        sprsCode = checkNull(genericUtility.getColumnValue("sprs_code",dom));
					        
					        
							System.out.println("routeId--->["+routeId+"]");
							System.out.println("sprsCode--->["+sprsCode+"]");
							
							if(routeId == null || routeId.length()==0)
							{
								System.out.println("---Inside error code of VTRTNL1----");
								errCode = "VTRTNL1";//route id is left blank
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}//end of if inside route id
							else {
								
								sql= "select count(*) from sprs_route where route_id = ? and sprs_code=?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,routeId);
								pstmt.setString(2,sprsCode);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt=rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Count value for routeId--->"+cnt);	
								if(cnt == 0)
								{
								errCode = "VTROUTID";//Invalid routeID.Enter invalid id
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );	
								}
																		
								
							}
						 
					 }//end of if loop for route_id
			      }//end of for loop
				break;
		}//end of switch case
			
			int errListSize = errList.size();
			cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					errString = getErrorString( errFldName, errCode, userId );
					errorType =  errorType( conn, errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				
				errStringXml.append("</Errors></Root>\r\n");
			} 
			else
			{
				errStringXml = new StringBuffer( "" );
			}	
			
}//end of try 	
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
	        throw new ITMException(e);
		}//end of catch block
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}	
				if(conn!=null)
				{
					conn.close();
					conn = null;
				}
				if(connDriver!=null)
				{
					connDriver = null;
				}	
				if(rs !=null)
				{
				rs.close();
			    rs=null;
				}				
		      }//end of try inside finally block
			     catch(Exception d)
						{
						  d.printStackTrace();
						}
		    }//end of finally block
					
					System.out.println("ErrString ::[ "+errStringXml.toString()+" ]");
					return errStringXml.toString();
  }//end of wfValdata	

	
public String itemChanged(String xmlString,String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
{
	System.out.println("------------------ itemChanged called------------------");
	System.out.println("xmlString DOM-->>["+xmlString+"]");
	System.out.println("xmlString DOM1-->>["+xmlString1+"]");
	System.out.println("xmlString DOM2-->>["+xmlString2+"]");
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	String valueXmlString = "";
	try
	{   
		dom = parseString(xmlString);
		dom1 = parseString(xmlString1);
		dom2 = parseString(xmlString2);
		valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
	}
	catch(Exception e)
	{
		System.out.println("Exception : [SprsStockistIC][itemChanged(String,String)] :==>\n"+e.getMessage());
		throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
	}
	return valueXmlString;
}
	
public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
{		
	System.out.println("@@@@@@@itemChanged called@@@@@@");
	StringBuffer valueXmlString = new StringBuffer();
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	E12GenericUtility genericUtility = new E12GenericUtility();
	//ConnDriver connDriver = new ConnDriver();
	int currentFormNo =0;
	String columnValue = null;
	String custCode="",custDescr="",sprsCode="",sprsDescr="";

	
	try
	{	
		SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(genericUtility.getApplDateFormat());
	    Calendar currentDate = Calendar.getInstance();		
	    String tranDate = simpleDateFormatObj.format(currentDate.getTime());	
	
	    
	        conn = getConnection();
	  		conn.setAutoCommit(false);
	  		//connDriver=null;
	  		if(objContext != null && objContext.trim().length()>0)
	  		{
	  			currentFormNo = Integer.parseInt(objContext.trim());
	  		}
	  		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
	  		valueXmlString.append(editFlag).append("</editFlag></header>");
	  		System.out.println("-------- currentFormNo : "+currentFormNo);
	  		

			switch(currentFormNo)
			{	
				case 1 :
				valueXmlString.append("<Detail1>");				
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				System.out.println("editFlag =>" +editFlag);
				
				if( currentColumn.trim().equalsIgnoreCase("itm_default") )
				{
					System.out.println("------------in itm_default--------->");
					currentDate = Calendar.getInstance();					
					tranDate = simpleDateFormatObj.format(currentDate.getTime());
					
					String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
					String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
					
					valueXmlString.append("<chg_date>").append("<![CDATA[" + tranDate + "]]>").append("</chg_date>");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
				
				}//end of if condition for itm_default
				
			    if (currentColumn.trim().equalsIgnoreCase("sprs_code"))//FOR SALES PERSON MASTER
				{
					sprsCode = checkNull(genericUtility.getColumnValue("sprs_code",dom));
					System.out.println("sprs_code ----->>["+sprsCode+"]");
					if(sprsCode.length() > 0)
					{
						sprsDescr =getColumnDescr(conn, "SP_NAME", "sales_pers", "SALES_PERS", sprsCode);
					}			
					valueXmlString.append("<sp_name>").append("<![CDATA[" + sprsDescr + "]]>").append("</sp_name>");
						
				}	//end of if condition for sales person code	
				
				 if (currentColumn.trim().equalsIgnoreCase("cust_code"))//FOR CUSTOMER MASTER
					{
						custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
						System.out.println("cust_code ----->>["+custCode+"]");
						if(custCode.length() > 0)
						{				
							custDescr =getColumnDescr(conn, "CUST_NAME", "customer", "CUST_CODE", custCode);
						}
						valueXmlString.append("<cust_name>").append("<![CDATA[" + custDescr + "]]>").append("</cust_name>");
						
					}	//end of if condition for customer code		
			
				 
				valueXmlString.append("</Detail1>");	
				
			}//end of switch case
			valueXmlString.append("</Root>");	
	}//end of try
	catch(Exception e)
	{
		e.printStackTrace();
		System.out.println("Exception ::"+e.getMessage());			
		throw new ITMException(e);
	}
	finally
	{
		try
		{
			if(conn!=null)
			{
				conn.close();
				conn = null;
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
		catch(Exception d)
		{
		  d.printStackTrace();
		}			
	}
	
	return valueXmlString.toString();

}//end of itemChanged
private String errorType( Connection conn , String errorCode ) throws ITMException
{
	String msgType = "";
	PreparedStatement pstmt = null ; 
	ResultSet rs = null;
	try
	{			
		String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
		
		pstmt = conn.prepareStatement( sql );			
		pstmt.setString(1, checkNull(errorCode));			
		rs = pstmt.executeQuery();
		while( rs.next() )
		{
			msgType = rs.getString("MSG_TYPE");
		}			
	}
	catch (Exception ex)
	{
		ex.printStackTrace();
		throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
	}		
	finally
	{
		try
		{
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			if ( pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}		
	return msgType;
}//end of errorType method
	
	public String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input.trim();
	}//end of checkNull method
	
	public String getColumnDescr(Connection conn, String columnName ,String tableName, String columnName2, String value) throws ITMException 
	{

			PreparedStatement pstmt = null ;
			ResultSet rs = null ; 
			String sql = "";
			String findValue = "";
			try
			{			
				sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,value);
				rs = pstmt.executeQuery();
				if(rs.next())
				{					
					findValue = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	        
			}
			catch(Exception e)
			{
				System.out.println("Exception in getColumnDescr ");
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
			System.out.println("returning String from getColumnDescr " + findValue);
			return findValue;
		 
	}

}

