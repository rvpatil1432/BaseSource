/* develop by ritesh on 26/sep/13 for request DI3FSUN020
 * Purpose : tax item stan : master functionality  */

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessLocal;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3
import ibase.webitm.ejb.ITMDBAccessEJB;

@Stateless // added for ejb3
public class TaxItemStanIC extends ValidatorEJB implements TaxItemStanICLocal,TaxItemStanICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1,  String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			System.out.println("@@@@@@@@  wfValData called");
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);

		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return (errString);
	}

	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		String errString = "";
		Connection conn = null;
		PreparedStatement pstmt = null ;

		ResultSet rs = null;

		String sql = "";
		int cnt=0;
		int currentFormNo=0;
		int childNodeListLength;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String stanCode=  "", taxType = "", taxPerc = "",itemCatagory = "";
		String userId="";
		String effDatestr = "",validUptostr = "";
		Timestamp validUpto = null;
		Timestamp effDate = null;

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

					if(childNodeName.equalsIgnoreCase("stan_code") && "A".equalsIgnoreCase(editFlag))
					{
						System.out.println("validation stan_code executed");
						stanCode = checkNull(genericUtility.getColumnValue("stan_code",dom));		
						effDatestr = checkNull(genericUtility.getColumnValue("eff_from",dom));
						validUptostr = checkNull(genericUtility.getColumnValue("valid_upto",dom));
						itemCatagory = checkNull(genericUtility.getColumnValue("item_category",dom));
						
						if(stanCode == null || stanCode.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMSTANCOD ",userId,"",conn);
							break ;
						}
						else
						{
							sql = "SELECT COUNT(1) FROM station WHERE STAN_CODE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, stanCode.trim());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt  == 0 )
							{
								errString = itmDBAccessEJB.getErrorString("","VTSTAN1   ",userId,"",conn);
								break ;
							}
							else if(effDatestr.trim().length() > 0 && validUptostr.trim().length() > 0)
							{
								cnt = 0;
								
								effDate = Timestamp.valueOf(genericUtility.getValidDateString(effDatestr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								validUpto = Timestamp.valueOf(genericUtility.getValidDateString(validUptostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
								
								sql = "SELECT COUNT(1) FROM tax_item_stan WHERE STAN_CODE = ? and  EFF_FROM = ? and VALID_UPTO = ? " +
										" and ITEM_CATEGORY = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString( 1, stanCode );
								pstmt.setTimestamp( 2, effDate );
								pstmt.setTimestamp( 3, validUpto );
								pstmt.setString( 4, itemCatagory );

								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								
								if( cnt  > 0 )
								{
									errString = itmDBAccessEJB.getErrorString("","VTSTNCDNT1   ",userId,"",conn);
									break ;
								}
							}

						}
					}
					
					else if(childNodeName.equalsIgnoreCase("item_category"))
					{
						System.out.println("validation ITEM_CATEGORY  executed");
						itemCatagory = checkNull(genericUtility.getColumnValue("item_category",dom));
						if(itemCatagory == null || itemCatagory.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VTITMCAT1 ",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_perc"))
					{
						System.out.println("validation TAX_PERC  executed");
						taxPerc = checkNull(genericUtility.getColumnValue("tax_perc",dom));
						if(taxPerc == null || taxPerc.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VTTAXPERC1 ",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_type"))
					{
						System.out.println("validation TAX_TYPE  executed");
						taxType = checkNull(genericUtility.getColumnValue("tax_type",dom));
						if(taxType == null || taxType.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VTTAXTYP1 ",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("eff_from"))
					{
						System.out.println("validation EFF_FROM  executed");
						effDatestr = genericUtility.getColumnValue("eff_from",dom);
						if(effDatestr == null || effDatestr.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VTEFFDATE1 ",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("valid_upto"))
					{
						System.out.println("validation VALID_UPTO  executed");
						validUptostr = checkNull(genericUtility.getColumnValue("valid_upto",dom));
						effDatestr = checkNull(genericUtility.getColumnValue("eff_from",dom));
						if(validUptostr == null || validUptostr.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VTVUPDATE1 ",userId,"",conn);
							break ;
						}
						else if(effDatestr !=null || effDatestr.trim().length() > 0)
						{
							effDate = Timestamp.valueOf(genericUtility.getValidDateString(effDatestr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							validUpto = Timestamp.valueOf(genericUtility.getValidDateString(validUptostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							if(validUpto != null && validUpto.before(effDate))
							{
								errString = itmDBAccessEJB.getErrorString("","VTDATE6 ",userId,"",conn);
								break ;
							}
						}
					}
					
				} // end for
				break;

			} //END switch
			
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException(e);
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
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}
		}
		System.out.println("ErrString ::"+errString);


		return errString;
	}//end of wfvalData
	public String itemChanged(String xmlString, String xmlString1, String xmlString2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ default itemChanged called");
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
			System.out.println("Exception : [TrainingEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
		return valueXmlString;
	}
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ itemChanged called");
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String childNodeName = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		String sql="";
		int currentFormNo =0;
		String columnValue="",chgUser="",chgTerm="",stanCode="",stanCodeDescr = "";
		int ctr=0;
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
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");System.out.println(":: chg term"+chgTerm);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chg_user");System.out.println(":: chg USER"+chgUser);
			//chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");

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
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("--------------------ITM_DEFAULT-----------------------");
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String sysDate = sdf.format(currentDate.getTime());
					System.out.println("Now the date is :=>  " + sysDate);

					valueXmlString.append( "<eff_from><![CDATA[" ).append(sysDate).append( "]]></eff_from>\r\n" );
					valueXmlString.append( "<valid_upto><![CDATA[" ).append( sysDate ).append( "]]></valid_upto>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append(chgUser).append( "]]></chg_user>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
					valueXmlString.append( "<chg_date><![CDATA[" ).append( sysDate ).append( "]]></chg_date>\r\n" );

				}
				if( currentColumn.trim().equalsIgnoreCase("stan_code") )
				{
					stanCode = checkNull(genericUtility.getColumnValue("stan_code",dom));

					System.out.println("--------------------STAN_CODE-----------------------"+stanCode);
					
					sql = "select descr from station where stan_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, stanCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						stanCodeDescr = checkNull(rs.getString("descr"));
					}
					
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					valueXmlString.append( "<descr><![CDATA[" ).append( stanCodeDescr ).append( "]]></descr>\r\n" );
				}

				valueXmlString.append("</Detail1>");
				break;

			}
			valueXmlString.append("</Root>");

		}// end try
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
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}//End of itemChanged	 

	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	public String preSaveRec() throws RemoteException, ITMException
	{
		return "";
	}
	@Override
	public String preSaveRec(String xmlString1, String domId, String objContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("@@@@@@@@********* taxItemStan preSaveRec Called *******@@@@@@@@");
		Document dom = null;
		String errString = "";
		try
		{
			System.out.println("taxItemStan xmlString1 [" + xmlString1 + "]");
			System.out.println("taxItemStan domId [" + domId + "]");
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				errString = executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :taxItemStan :preSaveRec(): " + e.getMessage()+ ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	private String executepreSaveRec(Document dom, String domID, String ObjContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String sql="",errString="",userId = "";
		NodeList hdrDom = null;
		Node currDetail = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null,pstmt1 = null;
		String effFromstr = "",validUptostr = "",stanCode = "",itemCategory = "";
		String taxPerc = "",taxType = "";
		Timestamp effFrom=null,validUpto = null,newsysdate=null,yesterday = null;
		int ctr=0;
		int count =0;
		String itemCategoryDB = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int childNodeListLength=0;
		String childNodeName = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		
		try
		{
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			newsysdate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");
			Calendar today = Calendar.getInstance();  
			today.add(Calendar.DATE, -1);  
			java.sql.Date dt2 = new java.sql.Date(today.getTimeInMillis());
			yesterday = java.sql.Timestamp.valueOf(sdf1.format(dt2)+" 00:00:00.0");
			
			System.out.println("\n yesterday :"+yesterday);
			//hdrDom = dom.getElementsByTagName("Detail1");
			//GenericUtility genericUtility = GenericUtility.getInstance();
			System.out.println("po po popopo");
			currDetail 	= getCurrentDetailFromDom(dom,domID);
		
			if (conn == null)
			{
				System.out.println("<-----------Connection is null----------------------->");
			}
		
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
		
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
			  childNode = childNodeList.item(ctr);
			  childNodeName = childNode.getNodeName();

			  if(childNodeName.equalsIgnoreCase("stan_code") && ("A".equalsIgnoreCase(editFlag)))	
			  {
				effFromstr = genericUtility.getColumnValue("eff_from",dom);
				validUptostr = genericUtility.getColumnValue("valid_upto",dom);
				stanCode = genericUtility.getColumnValue("stan_code",dom);
				itemCategory = genericUtility.getColumnValue("item_category",dom);
				taxPerc= genericUtility.getColumnValue("tax_perc",dom);
				taxType= genericUtility.getColumnValue("tax_type",dom);
				effFrom= Timestamp.valueOf(genericUtility.getValidDateString(effFromstr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				validUpto= Timestamp.valueOf(genericUtility.getValidDateString(validUptostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				
				System.out.println("@@@@@@@ effFromstr ::::"+effFromstr);
				System.out.println("@@@@@@@ validUptostr ::::"+validUptostr);
				System.out.println("@@@@@@@ stanCode ::::"+stanCode);
				System.out.println("@@@@@@@ itemCategory ::::"+itemCategory);
				System.out.println("@@@@@@@ taxPerc ::::"+taxPerc);
				System.out.println("@@@@@@@ taxType ::::"+taxType);
		
				sql = " select count(1) from tax_item_stan where stan_code = ? and item_category = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,stanCode);
				pstmt.setString(2,itemCategory);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
				   count = rs.getInt(1);
				}
				pstmt.close();pstmt = null;
				rs.close();rs = null;
				if(count > 0)
		        {
				
					Timestamp validuptoDB = null,effFromDB = null;
					
					sql = " select valid_upto,eff_from,item_category from tax_item_stan where stan_code = ? and item_category = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,stanCode);
					pstmt.setString(2,itemCategory);
					rs = pstmt.executeQuery();
					int cntR = 0;
					while(rs.next())
					{
						validuptoDB = rs.getTimestamp("valid_upto");
						//java.util.Date effFromDB1 = rs1.getDate("eff_from");
						effFromDB = rs.getTimestamp("eff_from");
						itemCategoryDB = rs.getString("item_category");
						
						Calendar cal = Calendar.getInstance();  
						Date effFromDate = new Date(effFrom.getTime());
						cal.setTime(effFromDate);
						cal.add(Calendar.DATE, -1);  
						java.sql.Date effFromDB2 = new java.sql.Date(cal.getTimeInMillis());
						Timestamp expiryDate = java.sql.Timestamp.valueOf(sdf1.format(effFromDB2)+" 00:00:00.0");
						//if(effFromDB.after(expiryDate) && itemCategoryDB.equalsIgnoreCase(itemCategory))
						if(effFromDB.after(expiryDate))
					    {
					    	errString = itmDBAccessEJB.getErrorString("","VTEXPDTNT1",userId,"",conn);
							return errString ;
					    }

						else if(effFrom.after(effFromDB)  && validUpto.before(validuptoDB))
						{
							errString = itmDBAccessEJB.getErrorString("","VTEXPDTNT1",userId,"",conn);
							return errString ;
						}
						else if(effFromDB.equals(validuptoDB))
						{
							if(effFromDB.equals(effFrom) && !validUpto.equals(validuptoDB))
							{
								errString = itmDBAccessEJB.getErrorString("","VTEXPDTNT1",userId,"",conn);
								return errString ;
							}
							
						}
						else if(effFrom.before(effFromDB) && validUpto.before(effFromDB) )
						{
							errString = itmDBAccessEJB.getErrorString("","VTEXPDTNT1",userId,"",conn);
							return errString ;
						}
						if(expiryDate.after(validuptoDB)){
					    	continue;
					    }else{
						sql = "update tax_item_stan set valid_upto = ? where stan_code = ? and eff_from = ? and item_category = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setTimestamp(1,expiryDate);
						pstmt1.setString(2,stanCode);
						pstmt1.setTimestamp(3,effFromDB);
						pstmt1.setString(4,itemCategory);
						int rows = pstmt1.executeUpdate();
						cntR = cntR + rows;
						pstmt1.close();pstmt1 = null;
					    }
					
			     	}
					System.out.println("&&&&&& row updated :: ( "+cntR+" ) for stan_code::::"+stanCode+"");
				
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
			       }
				}
				
			  }
			
		}
		catch (SQLException sqe)
		{
			System.out.println(" SQLException @@  :"+sqe);
			sqe.printStackTrace();
			errString=sqe.getMessage();
			throw new ITMException(sqe);
		}
		catch(Exception e)
		{
			System.out.println("Exception @@ :"+e);			
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				
				}if(rs != null)
				{
					rs.close();
					rs = null;
				}if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
			}
			catch(Exception e)
			{
				System.err.println("Exception :SReturnPrsEJB :\n"+e.getMessage());
				e.printStackTrace();
				errString=e.getMessage();
				throw new ITMException(e);
			}
		}
		
	return errString;
	}
	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail2");
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
			currDomId = currDetail.getAttributes().getNamedItem("domID").getNodeValue();
			if (currDomId.equals(domId))
			{
				reqDetail = currDetail;
				break;
			}			
		}
		return reqDetail;
	}

}



