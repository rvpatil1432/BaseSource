package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import ibase.system.config.ConnDriver;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.ActionHandlerEJB;
import org.w3c.dom.Document;
import org.w3c.dom.*;
import java.util.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DefaultSorder extends ActionHandlerEJB implements DefaultSorderLocal,DefaultSorderRemote//SessionBean
{	
	/* public void ejbCreate() throws RemoteException, CreateException 
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
	} */
    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}
	public String actionHandler( String actionType, String xmlString, String objContext, String xtraParams ) throws RemoteException,ITMException
	{
		String retString = "";
		Document dom = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			if( xmlString != null && xmlString.trim().length() != 0 )
			{
				dom = genericUtility.parseString(xmlString);
			}
		}
		catch( Exception e )
		{
			throw new ITMException(e);
		}
		return retString;
	}
	public String actionHandler( String actionType, String xmlString, String xmlString1, String objContext, String xtraParams ) throws RemoteException,ITMException
	{
		Connection conn=null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		Document dom = null;
		Document dom1 = null;
		String  retString = null;
		try
		{
			if( xmlString != null && xmlString.trim().length() != 0 )
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
			}
			if( xmlString1 != null && xmlString1.trim().length() != 0 )
			{
				System.out.println("xmlString1 is====>"+xmlString1);
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			if (actionType.equalsIgnoreCase("Default"))
			{
				try
				{
					if( conn == null )
					{
						ConnDriver connDriver = new ConnDriver();
						//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
						conn.setAutoCommit(false);
						connDriver = null;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					e.printStackTrace();
				}
				retString = actionDefault(dom, dom1, objContext, xtraParams);
			}
		}
	   	catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	private String actionDefault( Document dom, Document dom1, String objContext, String xtraParams ) throws RemoteException, ITMException
	{
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = null;
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");	
		int currentFormNo = 0;
		String sql = "";
		String retString = "";
		String update_flag = "", errorId = "";
		String termTableNo = "";
		NodeList parentNodeList = null;
		int parentNodeListLen = 0;
		String childNodeName = null;
		Node childNode = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		
		try
		{	
			if( conn == null )
			{
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
			}
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			parentNodeList = dom1.getElementsByTagName("Detail4");
			parentNodeListLen = parentNodeList.getLength();
			System.out.println("Parent node length is ==>"+parentNodeListLen);
			if( parentNodeListLen == 1 )
			{
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				for(int childctr = 0; childctr < childNodeList.getLength(); childctr++)
				{
					childNode = childNodeList.item( childctr );
					childNodeName = childNode.getNodeName();
					if ( childNodeName.equalsIgnoreCase( "attribute" ) )
					{
						update_flag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
					}
				}
				System.out.println("update_flag is ==>"+update_flag);
				if( !update_flag.equals("N") )
				{
					termTableNo = genericUtility.getColumnValue("term_table__no",dom1);
					if( termTableNo != null && termTableNo.trim().length() > 0 )
					{
						sql = "SELECT stermtbl.TERM_CODE, sterm.DESCR "
						     +" from SALE_TERM_TABLE stermtbl, SALE_TERM sterm "
						     +" WHERE stermtbl.TERM_CODE = sterm.TERM_CODE "
							 +" AND stermtbl.TERM_TABLE = ? ";
					    pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, termTableNo );			
					    rs = pstmt.executeQuery();
						while ( rs.next() )
						{
							String termCode = rs.getString("TERM_CODE");				
							String descr =	rs.getString("DESCR");
							
							valueXmlString.append("<Detail>\r\n");
							valueXmlString.append("<term_code>").append("<![CDATA[").append(termCode.trim()).append("]]>").append("</term_code>\r\n");
							valueXmlString.append("<descr>").append("<![CDATA[").append(descr.trim()).append("]]>").append("</descr>\r\n");
							valueXmlString.append("</Detail>\r\n");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
					}
				}
				else
				{
					errorId = "DATAPRSNT";
					retString = itmDBAccessEJB.getErrorString("",errorId,userId,"",conn);
					return retString;
				}
			}
			else
			{
				errorId = "DATAPRSNT";
				retString = itmDBAccessEJB.getErrorString("",errorId,userId,"",conn);
				return retString;
			}
			valueXmlString.append("</Root>\r\n");
			retString = valueXmlString.toString();
		}	
		catch( SQLException sqx )
		{
			sqx.printStackTrace();
			throw new ITMException( sqx );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch( Exception e )
			{
				e.printStackTrace();
				throw new ITMException( e );
			}
		}
		return retString;
	}
}//end of Ejb