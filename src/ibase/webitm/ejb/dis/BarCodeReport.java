/*
* Item Change for Bar Code Report
* Developer - Jiten
* 20/12/06 -
*
*/ 
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

//public class BarCodeReportEJB extends ValidatorEJB implements SessionBean // commented  for ejb3
@Stateless // added for ejb3
public class BarCodeReport extends ValidatorEJB implements BarCodeReportLocal, BarCodeReportRemote 
{
	/* commented  for ejb3
	public void ejbCreate() throws RemoteException, CreateException 
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
	}*/
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
	    Document dom = null,dom1 = null,dom2 = null;
	    String retString = "";
	    //GenericUtility genericUtility = new GenericUtility();
	    E12GenericUtility genericUtility= new  E12GenericUtility();
	    try
	    {
	        if(xmlString != null && xmlString.trim().length() > 0)
	            dom = genericUtility.parseString(xmlString);
	        if(xmlString1 != null && xmlString1.trim().length() > 0)
	            dom1 = genericUtility.parseString(xmlString1);
	        if(xmlString2 != null && xmlString2.trim().length() > 0)
	            dom2 = genericUtility.parseString(xmlString2);
	        
	        retString = wfValData(dom, dom1,dom2, objContext,editFlag,xtraParams);	        
	    }catch(ITMException ite){
	        System.out.println("ITMException : "+ite);
	        throw ite;
	    }
	    catch(Exception e){
	        System.out.println("Exception BarCodeEJB.."+e);
	        throw new ITMException(e);
	    }
		return retString;
	}
	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
	    Connection conn = null;
	    Statement stmt = null;
	    ResultSet rs = null;
	    int currentFormNo = 0;
	    NodeList detailNodes = null;
	    Node parentNode = null;
	    NodeList childNodeList = null;
	    Node childNode = null;
	    String childNodeName = "",sql = "",itemCode = "";
	    int childNodeListLength = 0, count = 0;
	    String errString = "",userId = "";
	    ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	    try{	        
	        conn = getConnection();
	        stmt = conn.createStatement();
	        if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
	        userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
	        System.out.println("userId :: "+userId);
	        System.out.println("  currentFormNo "+currentFormNo);
	        switch(currentFormNo){
	        	case 1:
	        	    detailNodes = dom.getElementsByTagName("Detail1");
	        	    parentNode = detailNodes.item(0);
	        	    childNodeList = parentNode.getChildNodes();
	        	    childNodeListLength = childNodeList.getLength(); 
	        	    
	        	    for(int ctr = 0;ctr < childNodeListLength; ctr++){
	        	        childNode = childNodeList.item(ctr);
	        	        childNodeName = childNode.getNodeName();
	        	        System.out.println("childNodeName :: "+childNodeName);
	        	        if(childNodeName.equalsIgnoreCase("item_code")){
	        	            if(childNode.getFirstChild() == null){
	        	                errString = itmDBAccessEJB.getErrorString("item_code","VMITEMCD1",userId,"",conn);
								System.out.println("errString :: "+errString);
	        	                break;
	        	            }else{
	        	                itemCode = childNode.getFirstChild().getNodeValue();
	        	                sql = "SELECT COUNT(*) FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
	        	                System.out.println("sql :: "+sql);
		        	            rs = stmt.executeQuery(sql);
		        	            if(rs.next()){
		        	                count = rs.getInt(1);
		        	                if(count == 0){
		        	                    errString = itmDBAccessEJB.getErrorString("item_code","VMITEM1",userId,"",conn);
		        	                    break; 
			        	            }
		        	            }		        	            
	        	            }
	        	        }
	        	    }
	        }			
	    }catch(Exception e){
	        System.out.println("Exception "+e);
	        e.printStackTrace();
	        throw new ITMException(e);
	    }finally{
	        try{
	            if(rs != null) rs.close();
	            if(stmt != null) stmt.close();
	            if(conn != null) conn.close();
	        }catch(Exception e){
	        	System.out.println("Exception :: "+e);	
	        }
	    }
	    System.out.println("errString :: "+errString);
	    return errString;
	}
	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String retString = "";
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			System.out.println("xmlString : "+xmlString);
			System.out.println("xmlString1 : "+xmlString1);
			System.out.println("xmlString2 : "+xmlString2);
			if(xmlString != null){
				//dom = GenericUtility.getInstance().parseString(xmlString);
				dom = genericUtility.parseString(xmlString);
			}
			if(xmlString1 != null){
				//dom1 = GenericUtility.getInstance().parseString(xmlString1);
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if(xmlString2 != null){
//				dom2 = GenericUtility.getInstance().parseString(xmlString2);
				dom2 = genericUtility.parseString(xmlString2); 
			}
			retString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(ITMException ie){
			System.out.println("Exception : [BarCodeReport][itemChanged] :==>\n"+ie);
			ie.printStackTrace();
			throw ie; //Added By Mukesh Chauhan on 02/08/19
		}
		catch(Exception e)
		{
			System.out.println("Exception : [BarCodeReport][itemChanged] :==>\n"+e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19s
		}
        return retString; 
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		NodeList detailNodes = null;
		NodeList childNodes = null;
		Node detailNode = null,currNode = null;
		int detailNodeListLength = 0;
		int currentFormNo=0;	
		String currNodeName = "", columnValue = "",sql = "";
		StringBuffer retStringBuf = null;
		try{
			
			conn = getConnection();
			
			if(objContext != null){
				currentFormNo = Integer.parseInt(objContext);
			}
			
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			
			columnValue = genericUtility.getColumnValue(currentColumn,dom);
			
			System.out.println("currentColumn : "+currentColumn+"columnValue"+columnValue);
			retStringBuf = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			retStringBuf.append(editFlag).append("</editFlag>\r\n</Header>\r\n<Detail1></Detail1>\r\n");				
			switch(currentFormNo){
				case 1:
					retStringBuf.append("<Detail1>\r\n");
					if(currentColumn.equalsIgnoreCase("item_code")){						
						sql = "select descr,phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4,phy_attrib_5 from item where item_code = '"+columnValue+"'";
						System.out.println("SQL ::"+sql);
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
						if(rs.next()){
							retStringBuf.append("<item_descr>").append("<![CDATA["+((rs.getString("descr") == null) ? "":rs.getString("descr"))+"]]>").append("</item_descr>\r\n");
							retStringBuf.append("<phy_attrib_1>").append("<![CDATA["+((rs.getString("phy_attrib_1") == null) ? "":rs.getString("phy_attrib_1"))+"]]>").append("</phy_attrib_1>\r\n");
							retStringBuf.append("<phy_attrib_2>").append("<![CDATA["+((rs.getString("phy_attrib_2") == null) ? "":rs.getString("phy_attrib_2"))+"]]>").append("</phy_attrib_2>\r\n");
							retStringBuf.append("<phy_attrib_3>").append("<![CDATA["+((rs.getString("phy_attrib_3") == null) ? "":rs.getString("phy_attrib_3"))+"]]>").append("</phy_attrib_3>\r\n");
							retStringBuf.append("<phy_attrib_4>").append("<![CDATA["+((rs.getString("phy_attrib_4") == null) ? "":rs.getString("phy_attrib_4"))+"]]>").append("</phy_attrib_4>\r\n");
							retStringBuf.append("<phy_attrib_5>").append("<![CDATA["+((rs.getString("phy_attrib_5") == null) ? "":rs.getString("phy_attrib_5"))+"]]>").append("</phy_attrib_5>\r\n");
							retStringBuf.append("<batch_no>").append("<![CDATA[]]>").append("</batch_no>\r\n");
							retStringBuf.append("<mc_no>").append("<![CDATA[]]>").append("</mc_no>\r\n");
							retStringBuf.append("<quantity>").append("<![CDATA[]]>").append("</quantity>\r\n");
							retStringBuf.append("<logo_no>").append("<![CDATA[]]>").append("</logo_no>\r\n");
						}
						else{
						    retStringBuf.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>\r\n");
							retStringBuf.append("<phy_attrib_1>").append("<![CDATA[]]>").append("</phy_attrib_1>\r\n");
							retStringBuf.append("<phy_attrib_2>").append("<![CDATA[]]>").append("</phy_attrib_2>\r\n");
							retStringBuf.append("<phy_attrib_3>").append("<![CDATA[]]>").append("</phy_attrib_3>\r\n");
							retStringBuf.append("<phy_attrib_4>").append("<![CDATA[]]>").append("</phy_attrib_4>\r\n");
							retStringBuf.append("<phy_attrib_5>").append("<![CDATA[]]>").append("</phy_attrib_5>\r\n");
							retStringBuf.append("<batch_no>").append("<![CDATA[]]>").append("</batch_no>\r\n");
							retStringBuf.append("<mc_no>").append("<![CDATA[]]>").append("</mc_no>\r\n");
							retStringBuf.append("<quantity>").append("<![CDATA[]]>").append("</quantity>\r\n");
							retStringBuf.append("<logo_no>").append("<![CDATA[]]>").append("</logo_no>\r\n");
						}
					}
					retStringBuf.append("</Detail1>\r\n");
					break;
			}
			retStringBuf.append("</Root>\r\n");	
		}catch(ITMException ie){
			throw ie;
		}catch(Exception e){
			throw new ITMException(e);
		}
		finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(stmt != null){
					stmt.close();
					stmt = null;
				}
				if(conn != null){
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		return retStringBuf.toString();
	}	
}