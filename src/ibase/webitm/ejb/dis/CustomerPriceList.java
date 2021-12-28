
/********************************************************
	Title : CustomerPriceList
	Date  : 11/04/11
	Author: Janardhan.B
 ********************************************************/

package ibase.webitm.ejb.dis;

//import ibase.webitm.utility.GenericUtility;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;

import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class CustomerPriceList extends ValidatorEJB implements CustomerPriceListLocal, CustomerPriceListRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString );
			System.out.println("Val xmlString1 :: " + xmlString1 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int childNodeListLength;
		int currentFormNo=0;
		String childNodeName = null;
		int ctr=0;
		int cnt = 0;
		String sql = "";
		String custCode = "";
		String orderType ="";
		String priceList = "";
		String priceListClg = "";

		String edit_flag=editFlag;
		StringBuffer errStringXml = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root><Errors>" ); 
		String errorType = "";
		String objName = "";
		String errString = "";
		String errCode = "";
		String groupCode = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();

		try
		{
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			connDriver = null;
			groupCode = getValueFromXTRA_PARAMS( xtraParams, "code" );
			if( objContext != null && objContext.trim().length()>0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			switch( currentFormNo )
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName( "Detail1" );
				parentNode = parentNodeList.item(0);
				objName = getObjName( parentNode );
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for( ctr = 0; ctr < childNodeListLength; ctr++ )
				{
					childNode = childNodeList.item( ctr );
					childNodeName = childNode.getNodeName();
					if( childNodeName.equalsIgnoreCase( "cust_code" ) )
					{
						custCode = getColumnValue( "cust_code", dom ); 
						if ( custCode == null || custCode.trim().length() == 0 )
						{
							System.out.println("Customer Code is null");
							errList.add( "VECUST2" );
							errFields.add( "cust_code" );
						}
						else // if( edit_flag == "A" || edit_flag.equalsIgnoreCase( "A" )) 
						{
							sql=" SELECT COUNT (*) FROM customer  WHERE  TRIM (cust_code) = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs= pstmt.executeQuery();
							if(rs.next())
							{
								if(!rs.getBoolean(1))
								{
									System.out.println("Customer code not exist");
									errList.add( "VTMSG" );
									errFields.add( "cust_code" );								
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if( childNodeName.equalsIgnoreCase( "order_type" ) )
					{
						orderType = getColumnValue( "order_type", dom); 
						if ( orderType == null || orderType.trim().length() == 0 )
						{
							System.out.println("order Type is coming as null");
							errList.add( "OTNUL" );
							errFields.add( "order_type" );
						}
						else if( edit_flag == "A" || edit_flag.equalsIgnoreCase( "A" )) 
						{
							sql=" SELECT COUNT (*) FROM cust_plist  WHERE  TRIM (cust_code) = ? AND TRIM (order_type) = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(2,orderType);
							rs= pstmt.executeQuery();
							if(rs.next())
							{
								if(rs.getBoolean(1))
								{
									System.out.println("Customer code and Order type combination already exist");
									errList.add( "CCOTAE" );
									errFields.add( "order_type" );								
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if( childNodeName.equalsIgnoreCase( "price_list" ) )
					{
						priceList = getColumnValue( "price_list", dom); 
						if ( priceList == null || priceList.trim().length() == 0 )
						{
							System.out.println("priceList is coming as null");
							errList.add( "UDPRLTNUL" );
							errFields.add( "price_list" );
						}
						else 
						{
							sql="SELECT COUNT (*) FROM pricelist  WHERE  TRIM (price_list) = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,priceList);
							rs= pstmt.executeQuery();
							if(rs.next())
							{
								if(!rs.getBoolean(1))
								{
									System.out.println("Pice List Not found");
									errList.add( "VMPRLIST" );
									errFields.add( "price_list" );								
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if( childNodeName.equalsIgnoreCase( "price_list__clg" ) )
					{
						priceListClg = getColumnValue( "price_list__clg", dom); 
						if ( priceListClg == null || priceListClg.trim().length() == 0 )
						{
							System.out.println("priceListClg is coming as null");
							errList.add( "UDPRLTNUL" );
							errFields.add( "price_list__clg" );
						}
						else 
						{
							sql="SELECT COUNT (*) FROM pricelist  WHERE  TRIM (price_list) = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,priceListClg);
							rs= pstmt.executeQuery();
							if(rs.next())
							{
								if(!rs.getBoolean(1))
								{
									System.out.println("Price List not found");
									errList.add( "VMPRLIST" );
									errFields.add( "price_list__clg" );								
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				} // END OF FOR
				break;					
			} //END OF SWITCH

			int errListSize = errList.size();
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					System.out.println("errCode .........."+errCode);
					errString = getErrorString( errFldName, errCode, groupCode );
					errorType =  errorType( conn , errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
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
		} //END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException( e );
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
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException( d );
			}
		}
		errString = errStringXml.toString();
		return errString;
	} //END OF VALIDATION

	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("In itemChangedXml:");
		Document dom = null;
		Document dom1 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("In itemChangeddom:"+dom);
			System.out.println("In itemChangeddom1:"+dom1);
			valueXmlString = itemChanged(dom,dom1,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [TrainingEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, String objContext,String currentColumn, String editFlag, String xtraParams)throws RemoteException, ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String columnValue = null;
		String priceList = "";
		String custCode = "";
		String sql = "";
		String custName = "";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int ctr=0;
		int currentFormNo = 0 ;
		ConnDriver connDriver = new ConnDriver();

		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver=null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			valueXmlString.append("<Detail>");

			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				int childNodeListLength = childNodeList.getLength();
				do
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));

				// START - itemchange for customer code
				if(currentColumn.trim().equalsIgnoreCase("cust_code"))
		        {
			        custCode = genericUtility.getColumnValue("cust_code",dom);
			        sql = "select  cust_name from customer where cust_code= ? ";
	                pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,custCode.trim());
	                rs = pstmt.executeQuery();
	                if(rs.next())
	                {
	                	custName = rs.getString(1);
	                	System.out.println("^^^custmer name AND customer code^^^:"+custName+"\t"+custCode);
	                }
	                rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<cust_name>").append("<![CDATA["+custName+"]]>").append("</cust_name>");
		        } //end : ITEM CHANGE FOR Customer Name 				
				
				valueXmlString.append("</Detail>");
				break;
			}
			valueXmlString.append("</Root>");
		}
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

			}catch(Exception d)
			{
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	} //END OF ITEMCHANGE	 

	private String errorType( Connection conn , String errorCode )
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString( 1, errorCode );			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
	}

	private String getObjName(Node node) throws Exception
	{
		String objName = null;
		NodeList nodeList = null;
		nodeList = node.getChildNodes();
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem( "objName" ).getNodeValue();
		return "w_" + objName;
	}
} // END OF MAIN CLASS
