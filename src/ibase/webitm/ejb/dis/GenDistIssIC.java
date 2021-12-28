package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.util.Date;
import java.sql.*;

import org.w3c.dom.*;

import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class GenDistIssIC extends ValidatorEJB //implements SessionBean
{
	//GenericUtility genericUtility = GenericUtility.getInstance(); 
	E12GenericUtility genericUtility = new E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("EpaymentICEJB is in Process..........");
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
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		System.out.println("Validation Start..........");
		try
		{
			System.out.println("xmlString:::"+xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : EpaymentICEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String startDate = null,endDate = null;
		String columnValue = null;
		String childNodeName = null;
		
		String errCode = null;
		String userId = null,loginSite = null;
		String prdtCode = null;
		
		int cnt = 0;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		
		String orderDateStr = null;
		String tranDateStr =  null;
		
		int resultCount = 0;
		String templateFile = null;
		String exprNo = null;

		
	    ConnDriver connDriver = new ConnDriver();
    	try
		{
			System.out.println( "wfValData called" );
			//Changes and Commented By Poonam on 08-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Poonam on 08-06-2016 :END

			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//genericUtility = GenericUtility.getInstance(); 
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
					System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
					} 
					//END OF CASE1
					break;
				case 2 :
					System.out.println("VALIDATION FOR DETAIL [ 2 ]..........");
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						cnt = 0;
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();	
					}//END FOR OF CASE2
					break;
			}//END SWITCH
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::" +e);
			e.printStackTrace();
			errCode = "VALEXCEP";
			errString = getErrorString( "", errCode, userId );	
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}
					
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			}
			catch(Exception d)
				{
				  d.printStackTrace();
				}			
			System.out.println(" < ExprProcessIcEJB > CONNECTION IS CLOSED");
		}
		System.out.println("ErrString ::" + errString);

		return errString;
	}//END OF VALIDATION 

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try
		{
			dom = parseString(xmlString); 
			System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1); 
			System.out.println("xmlString1" + xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [EpaymentICEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		int currentFormNo = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = null;

		NodeList parentNodeList = null;
		Node parentNode = null; 
		Node childNode = null;
		NodeList childNodeList = null;
		String childNodeName = null;
		int childNodeListLength = 0;
		int ctr = 0;
		
		String  loginSite = null; 
		
		try
		{
			//Changes and Commented By Poonam on 08-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Poonam on 08-06-2016 :END

			connDriver = null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			
			System.out.println("[EpaymentICEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			System.out.println("Current Form No ["+currentFormNo+"]");							
			switch (currentFormNo)
			{
				case 1:
					valueXmlString.append("<Detail1>");	
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					
					if ( currentColumn.trim().equals( "dist_order__fr" ) )
					{
						java.sql.Timestamp orderDate = null;
						if( columnValue != null )
						{
							sql = "select ORDER_DATE from distorder where DIST_ORDER = '" + columnValue + "'";

							pStmt = conn.prepareStatement( sql );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								orderDate = rs.getTimestamp( "ORDER_DATE" );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<order_date__fr>").append("<![CDATA[" + ( orderDate != null ? genericUtility.getValidDateString(orderDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) : ""  )+ "]]>").append("</order_date__fr>");
					}	
					if ( currentColumn.trim().equals( "dist_order__to" ) )
					{
						java.sql.Timestamp orderDate = null;
						if( columnValue != null )
						{
							sql = "select ORDER_DATE from distorder where DIST_ORDER = '" + columnValue + "'";

							pStmt = conn.prepareStatement( sql );
							rs = pStmt.executeQuery();
							if( rs.next() )
							{
								orderDate = rs.getTimestamp( "ORDER_DATE" );
							}
							rs.close();
							rs = null;
							pStmt.close();
							pStmt = null;
						}
						valueXmlString.append("<order_date__to>").append("<![CDATA[" + ( orderDate != null ? genericUtility.getValidDateString(orderDate.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat() ) : ""  )+ "]]>").append("</order_date__to>");
					}	
					valueXmlString.append("</Detail1>");
					valueXmlString.append("</Root>");	
					break;
				///////////////
				case 2:
					valueXmlString.append("<Detail2>");	
					//SEARCHING THE DOM FOR THE INCOMING COLUMN VALUE START
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue=childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					if (currentColumn.trim().equals( "itm_default" ))
					{
						valueXmlString.append("<qty_perc isSrvCallOnChg='0'>").append("<![CDATA[]]>").append("</qty_perc>");											
					}
					if (currentColumn.trim().equals( "itm_defaultedit" ))
					{
						String calcType = genericUtility.getColumnValue( "calc_type", dom );
						String qtyPerc = genericUtility.getColumnValue( "qty_perc", dom ); 
						String calMethod = genericUtility.getColumnValue( "cal_method", dom );
						String calInput = genericUtility.getColumnValue( "cal_input", dom );
						if( calcType != null && ( calcType.indexOf( "P" ) > -1 || calcType.indexOf( "W" ) > -1 ) )
						{
							valueXmlString.append("<qty_perc protect='0'>").append("<![CDATA[" + qtyPerc + "]]>").append("</qty_perc>");
						}
						else
						{
							valueXmlString.append("<qty_perc protect='0'>").append("<![CDATA[" + qtyPerc + "]]>").append("</qty_perc>");						
						}
						if( calcType != null && calcType.indexOf( "U" ) > -1  )
						{
							valueXmlString.append("<cal_method protect='0'>").append("<![CDATA[" + calMethod + "]]>").append("</cal_method>");
							valueXmlString.append("<cal_input protect='0'>").append("<![CDATA[" + calInput + "]]>").append("</cal_input>");
						}						
						else
						{
							valueXmlString.append("<cal_method protect='1'>").append("<![CDATA[]]>").append("</cal_method>");
							valueXmlString.append("<cal_input protect='1'>").append("<![CDATA[]]>").append("</cal_input>");
						}
					}
					if (currentColumn.trim().equals( "item_code" ))
					{
						String descr = null;
						sql = "select descr from item where item_code = '" + ( columnValue != null ? columnValue.trim() : "" ) + "'";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							descr = rs.getString( "descr" );
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<item_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</item_descr>");
					}	
					if (currentColumn.trim().equals( "site_code" ))
					{
						String descr = null;
						sql = "select descr from site where site_code = '" + ( columnValue != null ? columnValue.trim() : "" ) + "'";
						pStmt = conn.prepareStatement( sql );
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							descr = rs.getString( "descr" );
						}
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<site_descr>").append("<![CDATA[" + ( descr != null ? descr.trim() : ""  )+ "]]>").append("</site_descr>");
					}	
					if (currentColumn.trim().equals( "calc_type" ))
					{
						System.out.println( "calc_type :: " + columnValue );
						String calcType = genericUtility.getColumnValue( "calc_type", dom );
						System.out.println( "calc_type :: " + calcType );
						String qtyPerc = genericUtility.getColumnValue( "qty_perc", dom ); 
						System.out.println( "qtyPerc :: " + qtyPerc );
						String calMethod = genericUtility.getColumnValue( "cal_method", dom );
						System.out.println( "calMethod :: " + calMethod );
						String calInput = genericUtility.getColumnValue( "cal_input", dom );
						System.out.println( "calInput :: " + calInput );
						
						if( currentColumn != null && ( columnValue.indexOf( "P" ) > -1 || columnValue.indexOf( "W" ) > -1 ) )
						{
							valueXmlString.append("<qty_perc protect='0'>").append("<![CDATA[" + (qtyPerc == null?"":qtyPerc) + "]]>").append("</qty_perc>");
						}
						else
						{
							valueXmlString.append("<qty_perc protect='1'>").append("<![CDATA[]]>").append("</qty_perc>");						
						}
						if( currentColumn != null && columnValue.indexOf( "U" ) > -1  )
						{
							valueXmlString.append("<cal_method protect='0'>").append("<![CDATA[" + (calMethod == null?"":calMethod) + "]]>").append("</cal_method>");
							valueXmlString.append("<cal_input protect='0'>").append("<![CDATA[" + (calInput == null?"":calInput) + "]]>").append("</cal_input>");
						}						
						else
						{
							valueXmlString.append("<cal_method protect='1'>").append("<![CDATA[]]>").append("</cal_method>");
							valueXmlString.append("<cal_input protect='1'>").append("<![CDATA[]]>").append("</cal_input>");
						}						
					}	
					valueXmlString.append("</Detail2>");					
					valueXmlString.append("</Root>");					
				////////////////
			}//END OF TRY
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
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
				if ( pStmt != null )
				{
					pStmt.close();
					pStmt = null;					
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}//END OF ITEMCHANGE	
	private String getCurrdateAppFormat() throws ITMException
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(genericUtility.getDBDateFormat());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		}
		catch(Exception exception)
		{
			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
			throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
		}
		return s;
	}
}
