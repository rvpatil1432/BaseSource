/********************************************************
	Title : TaxRevision
	Date  : 19/04/12
	Developer:Rakesh kumar

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless
public class TaxRevision extends ValidatorEJB implements TaxRevisionLocal,TaxRevisionRemote 
{
	//changed by nasruddin 05-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	String winName = null;
	ValidatorEJB validator = null;
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}
	//@SuppressWarnings("deprecation")
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		String childNodeName = null;
		Node parentNode = null;
		Node childNode = null;
		int childNodeListLength=0;
		int currentFormNo = 0;
		long cnt = 0;
		int ctr=0;
		String userId = "";
		String sql="";
		String errCode="";
		String tranId="";
		String taxChapNew="";
		String taxChapOld="";
		String errorType = "";
		String errString = "";	
		Timestamp date1 = null;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			validator = new ValidatorEJB();
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
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
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					/* Comment By Nasruddin 21-SEp-16 Start
					if(childNodeName.equalsIgnoreCase("tran_id"))
					{   
						tranId = genericUtility.getColumnValue("tran_id", dom);
						if ( tranId == null ||  tranId.trim().length() == 0)
						{
							errCode = "VMSTCDNUL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						if ( editFlag != null && editFlag.trim().length() > 0 && editFlag.equalsIgnoreCase("A") )
						{
							sql = "select count(*) from tax_revision where tran_id = ?";     
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranId);
							rs = pstmt.executeQuery();

							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
							if(cnt > 0) 
							{
								errCode = "VTTAXCHAP2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}																
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							}
						}
					
					else if(childNodeName.equalsIgnoreCase("tran_date"))
                    {
						    date1 = Timestamp.valueOf(genericUtility.getValidDateString(genericUtility.getColumnValue("tran_date", dom) ,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
                            sql = "select stat_sal from period where fr_date <= ? and to_date>= ? ";
                            pstmt =  conn.prepareStatement(sql);
                            pstmt.setTimestamp(1, date1);
                            pstmt.setTimestamp(2, date1);
                            rs = pstmt.executeQuery();
                            if(rs.next())
                            {
                                    String statSal = rs.getString(1);
                                    if(!statSal.equals("Y"))
                                    {
                                            errCode = "VTPRDSAL";
                                            errList.add(errCode);
                                            errFields.add(childNodeName.toLowerCase());
                                    }
                            }
                            else
                            {
                                    errCode = "VTSAL1";
                                    errList.add(errCode);
                                    errFields.add(childNodeName.toLowerCase());
                            }
                            rs.close();
                            rs = null;
                            pstmt.close();
                            pstmt = null;
                    }
                    Comment By Nasruddin 21-SEp-16 End;
                    */
					
					if(childNodeName.equalsIgnoreCase("tax_chap__old"))
					{    
						/*
						Comment By Nasruddin  21-Sep-16 Start
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VMSTCDNULL";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						Comment By Nasruddin Start 21-Sep-16 END
						*/
						taxChapOld = genericUtility.getColumnValue("tax_chap__old", dom);
						sql = "select count(*) from taxchap where tax_chap = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxChapOld);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	
						if(cnt <= 0) 
						{
							errCode = "VTTAXCHAP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}																
						
					}
					else if(childNodeName.equalsIgnoreCase("tax_chap__new"))
					{
						/*
						 * Comment By Nasruddin 21-SEp-16 Start
						if ( childNode.getFirstChild() == null )
						{
							errCode = "VMSTCDNUL1";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						 Comment By Nasruddin 21-SEp-16 END */
						taxChapNew = genericUtility.getColumnValue("tax_chap__new", dom);
						sql = "select count(*) from  taxchap where tax_chap = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxChapNew);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);
						}
															
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt <=0) 
						{
							errCode = "VTTAXCHAP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Changed By nasruddin  21-Sep-16 Start
						else
						{
							sql = "SELECT COUNT(1)  FROM TAXSET WHERE TAX_CHAP = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,taxChapNew);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
							}
																
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt >0) 
							{
								errCode = "VTTAXCHAP2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								
								taxChapOld = genericUtility.getColumnValue("tax_chap__old", dom);
								tranId = genericUtility.getColumnValue("tran_id", dom);
								cnt = 0;
								if(tranId != null  && tranId.trim().length() > 0)
								{
									sql = "SELECT COUNT(1)  FROM TAX_REVISION 	WHERE ( TAX_REVISION.TAX_CHAP__OLD = ?) AND  ( TAX_REVISION.TAX_CHAP__NEW = ? ) AND  ( TAX_REVISION.TRAN_ID <> ?)";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, taxChapOld);
									pstmt.setString(2, taxChapNew);
									pstmt.setString(3, tranId);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt =  rs.getInt(1);
									}
																		
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								else
								{	

									sql = "SELECT COUNT(1)  FROM TAX_REVISION 	WHERE ( TAX_REVISION.TAX_CHAP__OLD = ?) AND  ( TAX_REVISION.TAX_CHAP__NEW = ? )";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, taxChapOld);
									pstmt.setString(2, taxChapNew);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cnt =  rs.getInt(1);
									}
																		
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								
								}
								if( cnt > 0)
								{
									errCode = "VTTAXRDUP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						// Changed By nasruddin 21-SEP-16 ENd
					}	 
				}
				break;
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
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
		}// End of try
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
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String valueXmlString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [taxchap][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}
	
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String taxChapNew="";
		int currentFormNo = 0;
		String taxChapOld="";
		String sql = "";
		String descr="";
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null ;
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer();
		ConnDriver connDriver = new ConnDriver();
		try
		{  
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			valueXmlString.append("<Detail1>");
			if(currentColumn.trim().equalsIgnoreCase("tax_chap__old"))
			{
				taxChapOld = genericUtility.getColumnValue("tax_chap__old", dom);
				sql = " select taxchap.descr from taxchap  where taxchap.tax_chap = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,taxChapOld);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = checkNull(rs.getString(1));	
				}
				valueXmlString.append("<old_descr>").append("<![CDATA[" + descr +"]]>").append("</old_descr>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			
			else if(currentColumn.trim().equalsIgnoreCase("tax_chap__new"))
			{
				taxChapNew = genericUtility.getColumnValue("tax_chap__old", dom);
				sql = "select taxchap.descr from taxchap  where taxchap.tax_chap = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,taxChapNew);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = checkNull(rs.getString(1));
				}
				valueXmlString.append("<new_descr>").append("<![CDATA[" + descr +"]]>").append("</new_descr>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			valueXmlString.append("</Detail1>");
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
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}	 
	private String errorType(Connection conn , String errorCode)
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
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
			}
		}		
		return msgType;
	}
	private String checkNull(String value)
	{
		if ( value == null )
		{
			value = "";
		}
		return value;
	}
}


