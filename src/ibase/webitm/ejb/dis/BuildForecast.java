package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

public class BuildForecast extends ValidatorEJB
{
	private E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			BaseLogger.log( "2", null, null, "xmlString :: ["+ xmlString +"]" );
			BaseLogger.log( "2", null, null, "xmlString1 :: ["+ xmlString1 +"]" );
			BaseLogger.log( "2", null, null, "xmlString2 :: ["+ xmlString2 +"]" );
			dom = genericUtility.parseString(xmlString);
			dom1 = genericUtility.parseString(xmlString1);
			if ( xmlString2.trim().length() > 0 )
			{
				dom2 = genericUtility.parseString("<Root>"+ xmlString2 +"</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		BaseLogger.log( "2", null, null, "errString : ["+ errString +"]" );
		return errString;
	}

	public String wfValData( Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams ) throws ITMException 
	{
		String fldname = "", errCode = "", fieldValue = "";
		String fromDate = "";
		String toDate = "";
		String siteCodeFrom="";
		String siteCodeTo="";
		String itemSerFrom="";
		String itemSerTo="";
		int childNodeLength=0;
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String userId="";

		try 
		{
			conn = getConnection();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeLength = childNodeList.getLength();

			for ( int ctr = 0; ctr < childNodeLength; ctr++ ) 
			{
				childNode = childNodeList.item(ctr);
				fldname = childNode.getNodeName();
				BaseLogger.log("3", null,null,"fldname name of the child node is ["+fldname+"]");
				if( "prd_code__fr".equalsIgnoreCase( fldname ) )
				{
					fieldValue = checkNull( genericUtility.getColumnValue( "prd_code__fr", dom ) );
					BaseLogger.log( "3", null, null, "prd_code__fr ["+ fieldValue +"]" );
					if( fieldValue.length() > 0)
					{
						String sql = "SELECT FR_DATE FROM PERIOD WHERE CODE = ?";
						pstm = conn.prepareStatement(sql);
						pstm.setString(1, fieldValue);
						rs = pstm.executeQuery();
						if ( rs.next() ) 
						{
							fromDate = checkNull( rs.getString( "FR_DATE" ) );
							if( fromDate.length() == 0 )
							{
								errCode = getErrorString( "prd_code__fr", "VMPRD1", userId );
								break;
							}
						}
						else
						{
							errCode = getErrorString( "prd_code__fr", "VMPRD1", userId );
							break;
						}
						rs.close();
						rs = null;
						pstm.close();
						pstm = null;
					}
					else
					{
						errCode = getErrorString("prd_code__fr", "VMPRD1", userId);
						break;
					}
				}
				else if( "prd_code__to".equalsIgnoreCase( fldname ) )
				{
					fieldValue = checkNull( genericUtility.getColumnValue(fldname, dom) );
					BaseLogger.log( "3", null, null, "prd_code__to ["+ fieldValue +"]" );
					if( fieldValue.length() > 0 )
					{
						String sql1 = "SELECT TO_DATE FROM PERIOD WHERE CODE = ?";
						pstm = conn.prepareStatement(sql1);
						pstm.setString(1, fieldValue);
						rs = pstm.executeQuery();
						if ( rs.next() ) 
						{
							toDate = checkNull( rs.getString("TO_DATE") );
							if( toDate.length() == 0 )
							{
								errCode = getErrorString( "prd_code__to", "VMPRD1", userId );
								break;
							}
						}
						else
						{
							errCode = getErrorString( "prd_code__to", "VMPRDSGE", userId );
							break;
						}
						rs.close();
						rs = null;
						pstm.close();
						pstm = null;
						
						BaseLogger.log( "3", null, null, "prd_code__to fromDate ["+ fromDate +"]" );
						BaseLogger.log( "3", null, null, "prd_code__to toDate ["+ toDate +"]" );

						errCode = validateDate( fromDate, toDate, userId );
						if( errCode != null && errCode.trim().length() > 0 )
						{
							break;
						}
					}
					else
					{
						errCode = getErrorString( "prd_code__to", "VMPRDSGE", userId );
						break;
					}
				}
				else if ( "site_code__fr".equalsIgnoreCase( fldname ) )
				{
					fieldValue = checkNull( genericUtility.getColumnValue(fldname, dom) );
					BaseLogger.log("3", null,null,"fieldValue = "+fieldValue);
					if( fieldValue.length() == 0 )
					{
						errCode = getErrorString("site_code__fr", "VTINSSCN", userId);
						break;
					}
					else
					{
						String sql1 = "SELECT SITE_CODE FROM SITE WHERE SITE_CODE= ?";
						pstm = conn.prepareStatement(sql1);
						pstm.setString(1, fieldValue);
						rs = pstm.executeQuery();
						if ( rs.next() ) 
						{
							siteCodeFrom = checkNull( rs.getString("SITE_CODE") );
						}
						else
						{
							errCode = getErrorString( "site_code__fr", "VTSITECD1", userId );
							break;
						}
						rs.close();
						rs = null;
						pstm.close();
						pstm = null;
					}
				}
				else if ( "site_code__to".equalsIgnoreCase( fldname ) )
				{
					fieldValue = checkNull( genericUtility.getColumnValue(fldname, dom) );
					BaseLogger.log("3", null,null,"fieldValue = "+fieldValue);
					if(fieldValue.equalsIgnoreCase("")  || fieldValue.trim().length()==0)
					{
						errCode = getErrorString("site_code__to", "VTINSSCN", userId);
						break;
					}
					else
					{
						String sql1 = "SELECT SITE_CODE FROM SITE where SITE_CODE= ?";
						pstm = conn.prepareStatement(sql1);
						pstm.setString(1, fieldValue);
						rs = pstm.executeQuery();
						if ( rs.next() ) 
						{
							siteCodeTo = checkNull( rs.getString("SITE_CODE") );
							BaseLogger.log( "3", null, null, "site_code__to siteCodeTo ["+ siteCodeTo +"]" );

						}
						else
						{
							errCode = getErrorString( "site_code__to", "VTSITECD1", userId );
							break;
						}
						rs.close();
						rs = null;
						pstm.close();
						pstm = null;
					}
				}
				else if( "item_ser__fr".equalsIgnoreCase( fldname ) )
				{
					fieldValue = checkNull( genericUtility.getColumnValue(fldname, dom) );
					BaseLogger.log("3", null,null,"fieldValue = "+fieldValue);
					if( fieldValue.length() > 0 )
					{
						String sql1 = "SELECT ITEM_SER FROM ITEM WHERE ITEM_SER= ?";
						pstm = conn.prepareStatement(sql1);
						pstm.setString(1, fieldValue);
						rs = pstm.executeQuery();
						if ( rs.next() ) 
						{
							itemSerFrom = checkNull( rs.getString("ITEM_SER") );
							BaseLogger.log( "3", null, null, "item_ser__fr itemSerFrom ["+ itemSerFrom +"]" );
						}
						else
						{
							errCode = getErrorString( "item_ser__fr", "VTIVIS", userId );
							break;
						}
						rs.close();
						rs = null;
						pstm.close();
						pstm = null;
					}
				}
				else if( "item_ser__to".equalsIgnoreCase( fldname ) )
				{
					fieldValue = checkNull( genericUtility.getColumnValue(fldname, dom) );
					BaseLogger.log("3", null,null,"fieldValue = "+fieldValue);
					if( fieldValue.length() > 0 )
					{
						String sql1 = "SELECT ITEM_SER FROM ITEM WHERE ITEM_SER= ?";
						pstm = conn.prepareStatement(sql1);
						pstm.setString(1, fieldValue);
						rs = pstm.executeQuery();
						if ( rs.next() ) 
						{
							itemSerTo = checkNull( rs.getString("ITEM_SER") );
							BaseLogger.log( "3", null, null, "item_ser__to itemSerTo ["+ itemSerTo +"]" );
						}
						else
						{
							errCode = getErrorString( "item_ser__to", "VTIVIS", userId );
							break;
						}
						rs.close();
						rs = null;
						pstm.close();
						pstm = null;
					}
				}

			}
		}
		catch (Exception e)
		{
			BaseLogger.log("0", null,  null,"ITMException : [BuildForecast.gbf_valdata_logic() :==>\n"+e.getMessage());
			throw new ITMException( e );
		}
		finally 
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		BaseLogger.log( "3", null, null, "errCode ["+ errCode +"]" );
		return errCode;
	}

	private String validateDate( String fromDate, String toDate, String userId ) throws ITMException
	{
		String errCode = "";
		try 
		{
			if( toDate.length() > 0 && fromDate.length() > 0 )
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				if( sdf.parse(toDate).before( sdf.parse(fromDate) ) )
				{
					errCode = getErrorString( "prd_code__to", "VMPRDSGE", userId );
				}
			}
		}
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}
		catch (ITMException e) 
		{
			e.printStackTrace();
			throw e; //Added  By Mukesh Chauhan on 02/08/19
		}
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		BaseLogger.log( "3", null, null, "In validateDate :: errCode ["+ errCode +"]" );
		return errCode;
	}

	private String checkNull( String input )
	{
		return E12GenericUtility.checkNull( input );
	}
}