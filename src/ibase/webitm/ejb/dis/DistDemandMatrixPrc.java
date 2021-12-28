package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import java.text.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DistDemandMatrixPrc extends ProcessEJB implements DistDemandMatrixPrcLocal, DistDemandMatrixPrcRemote
{
  	String[] siteArr;
	String loginSite = "";
	String loginCode = "";
	String chgTerm = "";
	StringBuffer msgString = null;
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/* public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			System.out.println("ProcessEJB ejbCreate called.........");
			
		}
		catch (Exception e)
		{
			System.out.println("Exception :ProcessEJB :ejbCreate :==>"+e);
			throw new CreateException();
		}
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
	public void remove()
	{
	}
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtrStr = "";
		Document dom = null;
		Document dom1 = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 				
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				dom1 = genericUtility.parseString(xmlString2); 				
			}
			rtrStr = getData(dom, dom1, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :BankRecoEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return rtrStr; 
	}
	public String getData(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String resultString = "";
		String errCode = "";
		String distDemandType = "";
		String dueDateTo = "";
		String dueDateFrm = "";
		String sql = "";
		String allSites = "";
		java.sql.Timestamp dateFrmTs = null;
		java.sql.Timestamp dateToTs = null;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		StringBuffer disDemMatrixStrBuff = new StringBuffer();
	    ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			distDemandType = genericUtility.getColumnValue("dist_demand_type",dom);
			dueDateFrm = genericUtility.getColumnValue("due_date_from",dom);
			dueDateTo = genericUtility.getColumnValue("due_date_to",dom);
			ArrayList allDataList = null;
			siteArr = new String[50];
			dateFrmTs = Timestamp.valueOf(genericUtility.getValidDateString(dueDateFrm, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			dateToTs = Timestamp.valueOf(genericUtility.getValidDateString(dueDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			if( distDemandType.equals("I") )
			{
				String itemCode = genericUtility.getColumnValue("item_code",dom);
				allDataList = new ArrayList();
				getAllDataBean dataBean = null;
				int i = 0;
				
				sql =  "SELECT a.site_code__source , a.site_code , a.item_code , "
				      +" SUM ( a.qty_required - nvl ( a.qty_ord , 0 ) ) as quantity "
					  +" from dist_demand a , item "
					  +" WHERE a.item_code = item.item_code "
					  +" and a.item_code = ? "
					  +" and a.status ='P' "
					  +" and a.due_date between ? and ? "
					  +" GROUP BY a.site_code__source , a.site_code , loc_type , a.item_code "
					  +" ORDER BY a.site_code__source , a.site_code , loc_type ";
					  
				pstmt = conn.prepareStatement( sql );
			
				pstmt.setString( 1, itemCode.trim() );
				pstmt.setTimestamp( 2, dateFrmTs );
				pstmt.setTimestamp( 3, dateToTs );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					dataBean = new getAllDataBean();
					
					dataBean.siteCodeSrc = rs.getString( "site_code__source" );
					dataBean.siteCode = rs.getString( "site_code" );
					dataBean.itemCodeDb = rs.getString( "item_code" );
					dataBean.quantity = rs.getDouble( "quantity" );
					
					allDataList.add(dataBean);
					
					if( allSites.indexOf( dataBean.siteCode.trim() ) == -1 )
					{
						allSites = allSites + dataBean.siteCode.trim();
						if( i < 50 )
						{
							siteArr[i] = dataBean.siteCode.trim();
							i++;
						}
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if( allDataList.size() > 0 )
				{
					resultString = getXmlData( allDataList, siteArr, distDemandType, conn );
				}
			}
			
			if( distDemandType.equals("SF") )
			{
				String issuingSite = genericUtility.getColumnValue("issuing_site",dom);
				allDataList = new ArrayList();
				getAllDataBean dataBean = null;
				int i = 0;
				
				sql =  "SELECT a.item_code , a.site_code , a.site_code__source , "
				      +" SUM ( a.qty_required - nvl ( a.qty_ord , 0 ) ) as quantity "
					  +" from dist_demand a , item "
					  +" WHERE a.item_code = item.item_code "
					  +" and a.site_code__source = ? "
					  +" and a.status ='P' "
					  +" and a.due_date between ? and ? "
					  +" GROUP BY loc_type , a.item_code,  a.site_code, a.site_code__source"
					  +" ORDER BY loc_type, a.item_code , a.site_code";
					  
				pstmt = conn.prepareStatement( sql );
			
				pstmt.setString( 1, issuingSite.trim() );
				pstmt.setTimestamp( 2, dateFrmTs );
				pstmt.setTimestamp( 3, dateToTs );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					dataBean = new getAllDataBean();
					
					dataBean.itemCodeDb = rs.getString( "item_code" );
					dataBean.siteCode = rs.getString( "site_code" );
					dataBean.siteCodeSrc = rs.getString( "site_code__source" );
					dataBean.quantity = rs.getDouble( "quantity" );
					
					allDataList.add(dataBean);
					
					if( allSites.indexOf( dataBean.siteCode.trim() ) == -1 )
					{
						allSites = allSites + dataBean.siteCode.trim();
						if( i < 50 )
						{
							siteArr[i] = dataBean.siteCode.trim();
							i++;
						}
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if( allDataList.size() > 0 )
				{
					resultString = getXmlData( allDataList, siteArr, distDemandType, conn );
				}
			}
			if( distDemandType.equals("ST") )
			{
				String recvngSite = genericUtility.getColumnValue("receiving_site",dom);
				allDataList = new ArrayList();
				getAllDataBean dataBean = null;
				int i = 0;
				
				sql =  "SELECT a.item_code , a.site_code__source ,a.site_code ,  "
				      +" SUM ( a.qty_required - nvl ( a.qty_ord , 0 ) ) as quantity "
					  +" from dist_demand a , item "
					  +" WHERE a.item_code = item.item_code "
					  +" and a.site_code = ? "
					  +" and a.status ='P' "
					  +" and a.due_date between ? and ? "
					  +" GROUP BY loc_type , a.item_code, a.site_code__source, a.site_code"
					  +" ORDER BY loc_type, a.item_code , a.site_code__source";
					  
				pstmt = conn.prepareStatement( sql );
			
				pstmt.setString( 1, recvngSite.trim() );
				pstmt.setTimestamp( 2, dateFrmTs );
				pstmt.setTimestamp( 3, dateToTs );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					dataBean = new getAllDataBean();
					
					dataBean.itemCodeDb = rs.getString( "item_code" );
					dataBean.siteCodeSrc = rs.getString( "site_code" );
					dataBean.siteCode = rs.getString( "site_code__source" );
					dataBean.quantity = rs.getDouble( "quantity" );
					
					allDataList.add(dataBean);
					
					if( allSites.indexOf( dataBean.siteCode.trim() ) == -1 )
					{
						allSites = allSites + dataBean.siteCode.trim();
						if( i < 50 )
						{
							siteArr[i] = dataBean.siteCode.trim();
							i++;
						}
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if( allDataList.size() > 0 )
				{
					resultString = getXmlData( allDataList, siteArr, distDemandType, conn );
				}
			}
			if( resultString.equals("") )
			{
				errCode ="VTNORECFND";
				resultString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
			}
		}
		catch (SQLException e)
		{
			System.out.println("Exception :DistDemandMatrix :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :DistDemandMatrix :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}			
		}
		return resultString;	
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{	
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("Detail XML String  :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2); 
			}
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("Header XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString); 
			}
		   retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :DistDemandMatrix :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return retStr;
	}

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String resultString = "";
		String childNodeName = "";
		String errString = "";
		String srcCode = "";
		String clsngStock = "";
		String totSpply = "";
		String balStock = "";
		String destSite = "";
		double reqQty = 0.0;
		boolean errFlag = true;
		Connection conn = null; 
		int childNodeListLength = 0;		
		int parentNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String distDemandType = "";
		msgString = new StringBuffer( "" );
		ConnDriver connDriver = new ConnDriver();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
		String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			String[] destSiteQtyArr = new String[50];
			ArrayList procList = new ArrayList();
			distDemandType = genericUtility.getColumnValue("dist_demand_type",headerDom);
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			if( distDemandType.equals("I") && parentNodeListLength > 1 )
			{
				errString =itmDBAccessEJB.getErrorString("","INVROWSLTN",userId,"",conn);
				return errString;//Invalid no of rows selected
			}
			for (int header = 0; header < parentNodeListLength; header++)
			{
				parentNode = parentNodeList.item(header);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				procDataBean procdb = new procDataBean();
				for (int cntr = 0; cntr < childNodeListLength; cntr++)
				{
					childNode = childNodeList.item(cntr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals("site_item_code"))
					{
						procdb.srcCode = childNode.getFirstChild().getNodeValue();
						
						if( procdb.srcCode != null && procdb.srcCode.trim().length() > 0 )	
						{
							if( procdb.srcCode.trim().equals("Item Code") || procdb.srcCode.trim().equals("Site Code") || procdb.srcCode.trim().equals("Description") )
							{
								System.out.println("Executing break ......");
								break;
							}
							procdb.srcCode = procdb.srcCode.substring( 0, procdb.srcCode.indexOf(":") ).trim();
						}
					}
					else if (childNodeName.equals("closing_stock"))
					{
						clsngStock = childNode.getFirstChild().getNodeValue();
					}
					else if (childNodeName.equals("total_supply"))
					{
						totSpply = childNode.getFirstChild().getNodeValue();	
					}
					else if (childNodeName.equals("bal_stock"))
					{
						balStock = childNode.getFirstChild().getNodeValue();	
					}
					else
					{
						for( int i = 1; i <= 50; i++ )
						{
							if(childNodeName.equals("site"+i))
							{
								procdb.destSiteQtyArr[i-1] = childNode.getFirstChild() == null ? "" : childNode.getFirstChild().getNodeValue();
							}
						}
					}
				}
				procList.add(procdb);
			}
			errString = createDistOrdr( procList, headerDom, conn );
			if( !errString.equals("") )
			{
				errFlag = true;
			}
			else
			{
				errFlag = false;
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistDemandMatrix :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			errFlag = true;
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(errFlag)
				{
					conn.rollback();
					System.out.println("connection rollback.............");
					errString = "PROCFAIL";
					resultString = itmDBAccessEJB.getErrorString("",errString,userId,"",conn);
				}
				else
				{
					conn.commit();
					System.out.println("commiting connection.............");
					if(errString.equals(""))
					{
						errString = "PROCSUCC";
					}
					resultString = itmDBAccessEJB.getErrorString("",errString,userId,"",conn);
					if( msgString != null && msgString.toString().trim().length() > 0 )
					{	
						String begPart = resultString.substring( 0, resultString.indexOf("<trace>") + 7 );
						String endPart = resultString.substring( resultString.indexOf("</trace>"));
						String mainStr = begPart + "Fallowing are the Distribution Order generated. :\n" + msgString.toString() + endPart;
						resultString = mainStr;
						begPart =null;
						endPart =null;
						mainStr =null;
					}
					
				}
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception se){}
		}
		return resultString;
	}
	
	public String getXmlData( ArrayList dataList, String[] siteArr, String distDemandType, Connection conn ) throws RemoteException,ITMException
	{
		StringBuffer disDemMatrixStrBuff = new StringBuffer();
		getAllDataBean dataBean = new getAllDataBean();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		
		try
		{
			String allSrcItems = "";
			String sourceItem = "";
			double clsngStock = 0.0;
			double totSupply = 0.0;
			double balStock = 0.0;
			String sql = "";
			if( distDemandType.equals("I") )
			{
				disDemMatrixStrBuff.append("Site Code").append("\t");
			}
			else
			{
				disDemMatrixStrBuff.append("Item Code").append("\t");
			}
			for( int destSitectr = 0; destSitectr < 50; destSitectr++ )
			{
				String destSite = siteArr[destSitectr];
				disDemMatrixStrBuff.append( destSite == null ? "" : destSite ).append("\t");
			}
			disDemMatrixStrBuff.append("").append("\t");
			disDemMatrixStrBuff.append("").append("\t");
			disDemMatrixStrBuff.append("").append("\t");
			disDemMatrixStrBuff.append("\n");
			disDemMatrixStrBuff.append("Description").append("\t");
			for( int destSitectr = 0; destSitectr < 50; destSitectr++ )
			{
				String destSite = siteArr[destSitectr];
				String siteDescr = "";
				if( destSite != null && destSite.trim().length() > 0 )
				{
					sql = "Select city from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, destSite.trim());
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						siteDescr = rs.getString("city");
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				}
				disDemMatrixStrBuff.append( siteDescr == null ? "" : siteDescr ).append("\t");
			}
			disDemMatrixStrBuff.append("").append("\t");
			disDemMatrixStrBuff.append("").append("\t");
			disDemMatrixStrBuff.append("").append("\t");
			disDemMatrixStrBuff.append("\n");
			for( int listCtr1 = 0; listCtr1 < dataList.size(); listCtr1++ )
			{
				dataBean = ( getAllDataBean )dataList.get( listCtr1 );
				String sourceItemDescr = "";
				if( distDemandType.equals("I") )
				{
					sourceItem = dataBean.siteCodeSrc;
					
					if( sourceItem != null && sourceItem.trim().length() > 0 )
					{
						sql = "Select descr from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, sourceItem.trim());
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							sourceItemDescr = rs.getString("descr");
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
					}
				}
				else
				{
					sourceItem = dataBean.itemCodeDb;
					if( sourceItem != null && sourceItem.trim().length() > 0 )
					{
						sql = "Select descr from item where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, sourceItem.trim());
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							sourceItemDescr = rs.getString("descr");
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
					}
				}
				if( allSrcItems.indexOf(sourceItem) == -1 )
				{
					disDemMatrixStrBuff.append( sourceItem + " : " + sourceItemDescr ).append("\t");
					clsngStock = 0.0;
					totSupply = 0.0;
					balStock = 0.0;
					clsngStock = getClosingStock( distDemandType.equals("ST") ? dataBean.siteCode : dataBean.siteCodeSrc, dataBean.itemCodeDb, conn);
					for( int destSitectr = 0; destSitectr < 50; destSitectr++ )
					{
						String destSite = siteArr[destSitectr];
						for( int listCtr = 0; listCtr < dataList.size(); listCtr++ )
						{
							dataBean = ( getAllDataBean )dataList.get( listCtr );
							if( distDemandType.equals("I") )
							{
								if( dataBean.siteCodeSrc.equals(sourceItem) && dataBean.siteCode.equals(destSite) )
								{
									disDemMatrixStrBuff.append( dataBean.quantity );
									totSupply = totSupply + dataBean.quantity;
								}
							}
							else
							{
								if( dataBean.itemCodeDb.equals(sourceItem) && dataBean.siteCode.equals(destSite) )
								{
									disDemMatrixStrBuff.append( dataBean.quantity );
									totSupply = totSupply + dataBean.quantity;
								}
							}
						}
						disDemMatrixStrBuff.append("\t");
					}
					balStock = clsngStock - totSupply;
					disDemMatrixStrBuff.append( clsngStock ).append("\t");
					disDemMatrixStrBuff.append( totSupply ).append("\t");
					disDemMatrixStrBuff.append( balStock ).append("\t");
					disDemMatrixStrBuff.append("\n");
					allSrcItems = allSrcItems + sourceItem;
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return disDemMatrixStrBuff.toString();
	}
	
	private double getClosingStock( String siteCode, String itemCode, Connection conn ) throws RemoteException,ITMException
	{
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		double clsngStock = 0.0;
		try
		{
			String sql = "SELECT ddf_inv_clstk( ?, ?, ' ', ?, 'I', 'Y' ) as clStk from dual";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, siteCode );
			pstmt.setString( 2, itemCode );
			pstmt.setTimestamp( 3, getCurrdateTsFormat() );
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				clsngStock = rs.getDouble("clStk");
			}
			pstmt.close();
			rs.close();
			pstmt = null;
			rs = null;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return clsngStock;
	}
	
	private String createDistOrdr( ArrayList procList, Document headerDom, Connection conn ) throws RemoteException,ITMException
	{
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		PreparedStatement insertHdrPstmt = null;
		PreparedStatement insertDtlPstmt = null;
		java.sql.Timestamp dueDateFrmTs = null;
		java.sql.Timestamp dueDateToTs = null;
		java.sql.Timestamp shipDate = null;
		String retString = "";
		String taxEnv = "", taxClass = "";
		double reqQty = 0.0;
		String sql = "", currCode = "";
		String locCodeGit = "";
		String locCodeGitBf = "", destSite = "";
		String sourceCode = "";
	    String locCodeCons = "";
		String distDemandType = "";
		String hdrItem = "", unit = "";
		String distOrder = "";
		String tranType = "";
		String priceList = "", retUpdt = "";
		String taxChap = "";
		int lineNo = 0;
		boolean flag = false;
		StringBuffer insDstordBuff = new StringBuffer();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		DistCommon dstCmn = new DistCommon();
		
		try
		{
			String insertHdr = "Insert into distorder ( "
						+" dist_order, order_date, site_code__ship, site_code__dlv, ship_date, "
						+" due_date, remarks, confirmed, chg_user, chg_term,"
						+" chg_date, site_code, status, order_type, avaliable_yn, "
						+" curr_code, exch_rate, tran_type, loc_code__git, loc_code__cons,"
						+" loc_code__gitbf, auto_receipt, price_list"
						//+" tot_amt, tax_amt, net_amt, trans_mode"
						+" ) Values("
						+" ?, ?, ?, ?, ?,"
						+" ?, ?, ?, ?, ?,"
						+" ?, ?, ?, ?, ?,"
						+" ?, ?, ?, ?, ?,"
						+" ?, ?, ?)";
						
			String insertDtl = "Insert into distorder_det ( "
					 +" dist_order, line_no, item_code, qty_order, qty_confirm,"
					 +" qty_received, qty_shipped, due_date, tax_class, tax_env,"
					 +" unit, tax_chap"
					// +" rate, qty_return, rate__clg, discount, tot_amt, tax_amt, net_amt, over_ship_perc, conv__qty__alt, qty_order__alt, quantity__fc "
					 +" ) Values("
					 +" ?, ?, ?, ?, ?,"
					 +" ?, ?, ?, ?, ?,"
					// +" ?, ?, ?, ?, ?,"
					// +" ?, ?, ?, ?, ?,"
					+" ?, ?)";
				
			insertHdrPstmt = conn.prepareStatement( insertHdr );
			insertDtlPstmt = conn.prepareStatement( insertDtl );
			
			if( procList.size() > 0 )
			{
				distDemandType = genericUtility.getColumnValue("dist_demand_type",headerDom);
				String dueDateFrm = genericUtility.getColumnValue("due_date_from",headerDom);
				String dueDateTo = genericUtility.getColumnValue("due_date_to",headerDom);
				
				dueDateFrmTs = Timestamp.valueOf(genericUtility.getValidDateString(dueDateFrm, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				dueDateToTs = Timestamp.valueOf(genericUtility.getValidDateString(dueDateTo, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				
				tranType = dstCmn.getDisparams("999999", "DEFAULT_DEST_TYPE", conn);
				
				if( distDemandType.equals("I") )
				{
					hdrItem = genericUtility.getColumnValue("item_code",headerDom);
				}
				else if( distDemandType.equals("SF") )
				{
					hdrItem = genericUtility.getColumnValue("issuing_site",headerDom);
				}
				else
				{
					hdrItem = genericUtility.getColumnValue("receiving_site",headerDom);
				}
				
				sql = "Select curr_code from finent where fin_entity = "
				     +" (select fin_entity from site where site_code = ?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, loginSite );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					currCode = rs.getString("curr_code");
				}
				pstmt.close();
				rs.close();
				
				sql = "select loc_code__git,loc_code__gitbf,loc_code__cons "
				     +" from distorder_type where tran_type = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranType );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locCodeGit = rs.getString("loc_code__git");
					locCodeGitBf = rs.getString("loc_code__gitbf");
					locCodeCons = rs.getString("loc_code__cons");
				}
				pstmt.close();
				rs.close();
				
				if( locCodeGit == null || locCodeGit.trim().length() == 0 )
				{
					sql = "Select var_value from disparm where prd_code = ? and var_name = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "999999" );
					pstmt.setString(2, "TRANSIT_LOC" );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						locCodeGit = rs.getString("var_value");
					}
					pstmt.close();
					rs.close();
				}
				System.out.println("locCodeGit==>"+locCodeGit);
				procDataBean procData = new procDataBean();
				if( distDemandType.equals("I") )
				{
					System.out.println("Inside   distDemandType.equals I.....");
					int z = 40;
					sql = "select unit from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, hdrItem );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						unit = rs.getString(1);
					}
					pstmt.close();
					rs.close();
					
					for( int i = 0; i < procList.size(); i++ )
					{
						System.out.println("Inside   distDemandType.equals I first for loop.....");
						procData = ( procDataBean )procList.get(i);
						sourceCode = procData.srcCode;
						for( int j = 0; j < 50; j++ )
						{
							System.out.println("Inside   distDemandType.equals I.second for loop.....");
							if( procData.destSiteQtyArr[j] != null && procData.destSiteQtyArr[j].trim().length() > 0 )
							{
								System.out.println("Inside   distDemandType.equalsI.Inside if.....");
								reqQty = Double.parseDouble(procData.destSiteQtyArr[j]);
								if( reqQty > 0 )
								{
									destSite = siteArr[j];
									sql = "select min(due_date) from dist_demand "
										 +" where site_code__source = ? "
										 +" and	site_code = ? and"
										 +" status = 'P' and due_date between ? and ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, sourceCode );
									pstmt.setString(2, destSite );
									pstmt.setTimestamp(3, dueDateFrmTs );
									pstmt.setTimestamp(4, dueDateToTs );
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										shipDate = rs.getTimestamp(1);
									}
									pstmt.close();
									rs.close();
									
									if( shipDate.compareTo(getCurrdateTsFormat()) < 1 )
									{
										shipDate = getCurrdateTsFormat();
									}
									
									priceList = dstCmn.setPlistTaxClassEnv(sourceCode, destSite, hdrItem, tranType, " ", "PRICE_LIST", conn);
									taxEnv = dstCmn.setPlistTaxClassEnv(sourceCode, destSite, hdrItem, tranType, " ", "TAX_ENV", conn);    
									taxClass = dstCmn.setPlistTaxClassEnv(sourceCode, destSite, hdrItem, tranType, " ", "TAX_CLASS", conn);
									distOrder = generateTranId("W_DIST_ORDER", getCurrdateInAppFormat(), loginSite, loginCode, tranType );
									taxChap = getTaxChap( destSite, hdrItem, conn );
									//distOrder = "00010000"+z;
									//z++;
									insertHdrPstmt.setString( 1, distOrder);
									insertHdrPstmt.setTimestamp( 2, getCurrdateTsFormat());
									insertHdrPstmt.setString( 3, sourceCode );
									insertHdrPstmt.setString( 4, destSite);
									insertHdrPstmt.setTimestamp( 5, shipDate);
									insertHdrPstmt.setTimestamp( 6, shipDate);
									insertHdrPstmt.setString( 7, "D1");
									insertHdrPstmt.setString( 8, "N");
									insertHdrPstmt.setString( 9, loginCode);
									insertHdrPstmt.setString( 10, chgTerm);
									insertHdrPstmt.setTimestamp( 11, getCurrdateTsFormat());
									insertHdrPstmt.setString( 12, loginSite);
									insertHdrPstmt.setString( 13, "P");
									insertHdrPstmt.setString( 14, "F");
									insertHdrPstmt.setString( 15, "Y");
									insertHdrPstmt.setString( 16, currCode);
									insertHdrPstmt.setDouble( 17, 1 );
									insertHdrPstmt.setString( 18, "IT");//change it when given to QC tranType
									insertHdrPstmt.setString( 19, locCodeGit);
									insertHdrPstmt.setString( 20, locCodeCons);
									insertHdrPstmt.setString( 21, locCodeGitBf);
									insertHdrPstmt.setString( 22, "N");
									insertHdrPstmt.setString( 23, priceList );
									
									insertHdrPstmt.addBatch();
									
									insertDtlPstmt.setString( 1, distOrder );
									insertDtlPstmt.setInt( 2, 1 );
									insertDtlPstmt.setString( 3, hdrItem );
									insertDtlPstmt.setDouble( 4, reqQty );
									insertDtlPstmt.setDouble( 5, reqQty );
									insertDtlPstmt.setDouble( 6, 0.0 );
									insertDtlPstmt.setDouble( 7, 0.0 );
									insertDtlPstmt.setTimestamp( 8, shipDate );
									insertDtlPstmt.setString( 9, taxClass );
									insertDtlPstmt.setString( 10, taxEnv );
									insertDtlPstmt.setString( 11, unit );
									insertDtlPstmt.setString( 12, taxChap );
									
									insertDtlPstmt.addBatch();
									
									retUpdt = updtDistDemand(sourceCode, destSite, hdrItem, reqQty, dueDateFrmTs, dueDateToTs, conn  );
									msgString.append( "Distribution order No -->"+distOrder+"\n" );
								}
							}
						}
					}
					insertHdrPstmt.executeBatch();
					insertDtlPstmt.executeBatch();
				}
				else// if( distDemandType.equals("SF") )
				{
					int z = 10;
					for( int i = 0; i < 50; i++ )
					{
						flag = true;
						lineNo = 0;
						for( int j = 0; j < procList.size(); j++ )
						{
							procData = ( procDataBean )procList.get(j);
							if( procData.destSiteQtyArr[i] != null && procData.destSiteQtyArr[i].trim().length() > 0 )
							{
								reqQty = Double.parseDouble(procData.destSiteQtyArr[i]);
								if( reqQty > 0 )
								{
									destSite = siteArr[i];
									sourceCode = procData.srcCode;
									lineNo = lineNo + 1;
									System.out.println("destSite is ************==>["+destSite+"]");
									System.out.println("hdrItem is ************==>["+hdrItem+"]");
									if( distDemandType.equals("SF") )
									{
										taxEnv = dstCmn.setPlistTaxClassEnv(hdrItem, destSite, sourceCode, tranType, " ", "TAX_ENV", conn);    
										taxClass = dstCmn.setPlistTaxClassEnv(hdrItem, destSite, sourceCode, tranType, " ", "TAX_CLASS", conn);
										taxChap = getTaxChap( hdrItem, sourceCode, conn );
									}
									else
									{
										taxEnv = dstCmn.setPlistTaxClassEnv(destSite, hdrItem, sourceCode, tranType, " ", "TAX_ENV", conn);    
										taxClass = dstCmn.setPlistTaxClassEnv(destSite, hdrItem, sourceCode, tranType, " ", "TAX_CLASS", conn);
										taxChap = getTaxChap( destSite, sourceCode, conn );
									}
									System.out.println("taxEnv is ************==>["+taxEnv+"]");
									/* if( distDemandType.equals("SF") )
										taxClass = dstCmn.setPlistTaxClassEnv(hdrItem, destSite, sourceCode, tranType, " ", "TAX_CLASS", conn);
									else
										taxClass = dstCmn.setPlistTaxClassEnv(destSite, hdrItem, sourceCode, tranType, " ", "TAX_CLASS", conn); */
									System.out.println("taxClass is ************==>["+taxClass+"]");
									System.out.println("taxChap is ************==>["+taxChap+"]");
									sql = "select min(due_date) from dist_demand "
										 +" where site_code__source = ? "
										 +" and	site_code = ? and"
										 +" status = 'P' and due_date between ? and ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, distDemandType.equals("SF") ? hdrItem : destSite );
									pstmt.setString(2, distDemandType.equals("SF") ? destSite : hdrItem );
									pstmt.setTimestamp(3, dueDateFrmTs );
									pstmt.setTimestamp(4, dueDateToTs );
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										shipDate = rs.getTimestamp(1);
									}
									pstmt.close();
									rs.close();
									rs = null;
									pstmt = null;
									System.out.println("shipDate is ==>"+shipDate);
									
									if( shipDate == null || shipDate.compareTo(getCurrdateTsFormat()) < 1 )
									{
										shipDate = getCurrdateTsFormat();
									}
									System.out.println("Calculating unit");
									sql = "select unit from item where item_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, sourceCode );
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										unit = rs.getString(1);
									}
									pstmt.close();
									rs.close();
									rs = null;
									pstmt = null;
									
									priceList = dstCmn.setPlistTaxClassEnv(hdrItem, destSite, sourceCode, tranType, " ", "PRICE_LIST", conn);
									
									if(flag)
									{
										distOrder = generateTranId("W_DIST_ORDER", getCurrdateInAppFormat(), loginSite, loginCode, tranType );
										//distOrder = "00000000"+z;
										//z++;
										insertHdrPstmt.setString( 1, distOrder);
										insertHdrPstmt.setTimestamp( 2, getCurrdateTsFormat());
										insertHdrPstmt.setString( 3, distDemandType.equals("SF") ? hdrItem : destSite );
										insertHdrPstmt.setString( 4, distDemandType.equals("SF") ? destSite : hdrItem );
										insertHdrPstmt.setTimestamp( 5, shipDate);
										insertHdrPstmt.setTimestamp( 6, shipDate);
										insertHdrPstmt.setString( 7, "D1");
										insertHdrPstmt.setString( 8, "N");
										insertHdrPstmt.setString( 9, loginCode);
										insertHdrPstmt.setString( 10, chgTerm);
										insertHdrPstmt.setTimestamp( 11, getCurrdateTsFormat());
										insertHdrPstmt.setString( 12, loginSite);
										insertHdrPstmt.setString( 13, "P");
										insertHdrPstmt.setString( 14, "F");
										insertHdrPstmt.setString( 15, "Y");
										insertHdrPstmt.setString( 16, currCode);
										insertHdrPstmt.setDouble( 17, 1 );
										insertHdrPstmt.setString( 18, "IT");//change this by tranType when given to qc
										insertHdrPstmt.setString( 19, locCodeGit);
										insertHdrPstmt.setString( 20, locCodeCons);
										insertHdrPstmt.setString( 21, locCodeGitBf);
										insertHdrPstmt.setString( 22, "N");
										insertHdrPstmt.setString( 23, priceList );
									
										int r = insertHdrPstmt.executeUpdate();
									//	insertHdrPstmt.addBatch();
										flag = false;
										msgString.append( "Distribution order No -->"+distOrder+"\n" );
									}
									System.out.println("Adding batch indetail*****************");
									System.out.println("distOrder==>["+distOrder+"]");
									System.out.println("lineNo==>["+lineNo+"]");
									System.out.println("sourceCode==>["+sourceCode+"]");
									System.out.println("reqQty==>["+reqQty+"]");
									System.out.println("shipDate==>["+shipDate+"]");
									System.out.println("taxEnv==>["+taxEnv+"]");
									System.out.println("taxClass==>["+taxClass+"]");
									System.out.println("unit==>["+unit+"]");
									
									insertDtlPstmt.setString( 1, distOrder.trim() );
									insertDtlPstmt.setInt( 2, lineNo );
									insertDtlPstmt.setString( 3, sourceCode.trim() );
									insertDtlPstmt.setDouble( 4, reqQty );
									insertDtlPstmt.setDouble( 5, reqQty );
									insertDtlPstmt.setDouble( 6, 0.0 );
									insertDtlPstmt.setDouble( 7, 0.0 );
									insertDtlPstmt.setTimestamp( 8, shipDate );
									insertDtlPstmt.setString( 9, taxClass );
									insertDtlPstmt.setString( 10, taxEnv );
									insertDtlPstmt.setString( 11, unit );
									insertDtlPstmt.setString( 12, taxChap );
									
									int t = insertDtlPstmt.executeUpdate();
									//insertDtlPstmt.addBatch();
									System.out.println("ending batch indetail*****************");
									if( distDemandType.equals("SF") )
										retUpdt = updtDistDemand(hdrItem, destSite, sourceCode, reqQty,  dueDateFrmTs, dueDateToTs, conn  );
									else
										retUpdt = updtDistDemand(destSite, hdrItem, sourceCode, reqQty, dueDateFrmTs, dueDateToTs, conn  );
								}
							}
						}
					}
					//insertHdrPstmt.executeBatch();
					//insertDtlPstmt.executeBatch();
				}
				insertHdrPstmt.close();
				insertDtlPstmt.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null)rs.close();
				rs = null;
				if(pstmt != null)pstmt.close();
				pstmt = null;
				if(insertHdrPstmt != null)insertHdrPstmt.close();
				insertHdrPstmt = null;
				if(insertDtlPstmt != null)insertDtlPstmt.close();
				insertDtlPstmt = null;
			}
			catch(SQLException sqle)
			{
				sqle.printStackTrace();
			}
		}
		return "";
	}
	
	private String generateTranId(String windowName, String tranDate, String siteCode ,String signBy,String tranType) throws ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String generateTranIdSql = null;
		String tranId = null;
		String xmlValues = null;
		StringBuffer xmlValuesBuff = new StringBuffer();
		String refSer = "";
		String keyString = "";
		String tranIdCol = "";
		 try
	     {
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			generateTranIdSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)= ?";
			System.out.println("[DistDemandMatrixEJB :: generateTranId : input Paramaters][windowName]["+windowName+"][tranDate]["+tranDate+"][siteCode]["+siteCode+"]");
	    	System.out.println( "[DistDemandMatrixEJB : generateTranId : Tran generator Sql[" + generateTranIdSql+"]" );
			pstmt = conn.prepareStatement( generateTranIdSql );
			pstmt.setString(1, windowName);
			rs = pstmt.executeQuery();

			if( rs.next() )
			{
				keyString = rs.getString("KEY_STRING");
				tranIdCol = rs.getString("TRAN_ID_COL");
				refSer = rs.getString("REF_SER");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			System.out.println("[Output of Tran generator Sql ][keyString]["+keyString+"][tranIdCol]["+tranIdCol+"][refSer]["+refSer+"]");	
			
			xmlValuesBuff.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>");
			xmlValuesBuff.append("<Header></Header>");
			xmlValuesBuff.append("<Detail1>");
			xmlValuesBuff.append("<tran_id></tran_id>");
			xmlValuesBuff.append("<site_code><![CDATA["+siteCode+"]]></site_code>");
			xmlValuesBuff.append("<tran_date><![CDATA["+tranDate+"]]></tran_date>");
			xmlValuesBuff.append("<vouch_type><![CDATA[F]]></vouch_type>");
			xmlValuesBuff.append("<tran_type><![CDATA["+tranType+"]]></tran_type>");
			xmlValuesBuff.append("</Detail1></Root>");
			xmlValues = xmlValuesBuff.toString();
			System.out.println("xmlValues  :[" + xmlValues + "]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues,signBy, CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(refSer, tranIdCol, keyString, conn);
			System.out.println("tranId :"+tranId);
		}
		catch (SQLException ex)
		{			
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
		}
		catch (Exception e)
		{		
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		finally
		{
			try
			{
				if( conn != null ){
					conn.close();
					conn = null;
				}
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
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
		return tranId;
	}
	
	private String updtDistDemand( String srcSiteCode, String destSiteCode, String itemCode, double reqQty, Timestamp dueDtF, Timestamp dueDtTo, Connection conn ) throws RemoteException,ITMException
	{
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String updtTranId = "";
		String sql = "";
		java.sql.Timestamp updtDueDate = null;
		double updtQty = 0.0;
		try
		{
			sql = "select tran_id,due_date,(qty_required - qty_ord) as updtQty from dist_demand"
					   +" where site_code = ?"
					   +" and site_code__source = ?"
					   +" and item_code = ?"
					   +" and due_date between ? and ?"
					   +" and status = 'P'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, destSiteCode );
			pstmt.setString( 2, srcSiteCode );
			pstmt.setString( 3, itemCode );
			pstmt.setTimestamp( 4, dueDtF );
			pstmt.setTimestamp( 5, dueDtTo );
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				updtTranId = rs.getString("tran_id");
				updtDueDate = rs.getTimestamp("due_date");
				updtQty = rs.getDouble("updtQty");
			}
			pstmt.close();
			rs.close();
			rs = null;
			pstmt = null;
			
			if( reqQty >= updtQty )
			{
				sql = "update dist_demand set qty_ord = qty_required, status = 'C'"
				 +" where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, updtTranId );
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
			}
			else
			{				
				sql = "update dist_demand set qty_ord = ( "+ reqQty +" + qty_ord )"
					 +" where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, updtTranId );
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return "";		
	}
	
	private String getTaxChap( String siteCode, String itemCode, Connection conn ) throws RemoteException,ITMException
	{
		String sql = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String taxChap = "";
		try
		{
			sql=" select tax_chap "		
				+"	from siteitem " 
				+"	where site_code = ? " 
				+"	and item_code =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,siteCode);
			pstmt.setString(2,itemCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				taxChap=rs.getString("tax_chap") != null ? rs.getString("tax_chap") :"";
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;	
			if(taxChap==null || taxChap.trim().length()==0)
			{
				sql="select tax_chap  from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);				
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					taxChap=rs.getString("tax_chap") != null ? rs.getString("tax_chap") :"";
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(taxChap==null || taxChap.trim().length()==0)
				{
					sql="select tax_chap  from itemser "
					   +" where item_ser IN (Select item_ser from item where item_code = ?)";
					pstmt = conn.prepareStatement(sql);				
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						taxChap=rs.getString("tax_chap") != null ? rs.getString("tax_chap") :"";
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return taxChap;
	}

	private class getAllDataBean
	{
		public String itemCodeDb = "";
		public String siteCodeSrc = "";
		public String siteCode = "";
		public double quantity = 0.0;
	}
	private class procDataBean
	{
		public String hdrItem = "";
		public String srcCode = "";
		/* public String clsngStock = "";
		public String totSpply = "";
		public String balStock = ""; */
		//public String destSite = "";
		public double reqQty = 0.0;
		public String[] destSiteQtyArr = new String[50];
	}
	
	private Timestamp getCurrdateTsFormat() throws ITMException
    {
        String s = "";	
		Timestamp timestamp = null;		
       // GenericUtility genericUtility = GenericUtility.getInstance();
        try
        {
            java.util.Date date = null;
            timestamp = new Timestamp(System.currentTimeMillis());
            
			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		}
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
            throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
        }
        return timestamp;
    }
	
	private String getCurrdateInAppFormat() throws ITMException
	{
		String currAppdate =null;
		java.sql.Timestamp currDate = null;
		Object date = null;
		SimpleDateFormat DBDate=null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
				currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
				System.out.println( genericUtility.getDBDateFormat());
			 	DBDate= new SimpleDateFormat(genericUtility.getDBDateFormat());
				date = DBDate.parse(currDate.toString());
				currDate =	java.sql.Timestamp.valueOf(DBDate.format(date).toString() + " 00:00:00.0");
				currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
			System.out.println("Exception in  getCurrdateInAppFormat:::"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return (currAppdate);
	}
}
/* insDstordBuff.append("<?xml	version='1.0' encoding='UTF-8'?>\n");
	insDstordBuff.append("<DocumentRoot>");
	insDstordBuff.append("<description>").append("Datawindow Root").append("</description>");
	insDstordBuff.append("<group0>");
	insDstordBuff.append("<description>").append("Group0description").append("</description>");
	insDstordBuff.append("<Header0>");
	insDstordBuff.append("<objName><![CDATA[").append("inv_hold").append("]]></objName>");				
	insDstordBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
	insDstordBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
	insDstordBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
	insDstordBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
	insDstordBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
	insDstordBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
	insDstordBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
	insDstordBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
	insDstordBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
	insDstordBuff.append("<forcedSave><![CDATA[").append("true").append("]]></forcedSave>");
	insDstordBuff.append("<taxInFocus><![CDATA[").append("false").append("]]></taxInFocus>");
	insDstordBuff.append("<description>").append("Header0 members").append("</description>"); */
/* insDstordBuff.append("<Detail1 objContext =\"1\"").append(" objName=\"inv_hold\" domID=\"1\"	dbID=\"\">");
	insDstordBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
	insDstordBuff.append("<order_date><![CDATA[").append(sdf.format(currDate)).append("]]></order_date>");								
	insDstordBuff.append("<site_code__ship><![CDATA[").append(sourceSite).append("]]></site_code__ship>");
	insDstordBuff.append("<site_code__dlv><![CDATA[").append(destnSite).append("]]></site_code__dlv>");
	insDstordBuff.append("<ship_date><![CDATA[").append(if<todays,then todays).append("]]></ship_date>");
	insDstordBuff.append("<due_date><![CDATA[").append(if<todays,then todays)).append("]]></due_date>");
	insDstordBuff.append("<remarks><![CDATA[").append("D1").append("]]></remarks>");
	insDstordBuff.append("<confirmed><![CDATA[").append("N").append("]]></confirmed>");
	insDstordBuff.append("<chg_user><![CDATA[").append(youknow).append("]]></chg_user>");
	insDstordBuff.append("<chg_term><![CDATA[").append(youknow).append("]]></chg_term>");
	insDstordBuff.append("<loc_code__git><![CDATA[").append(locCodeGit).append("]]></loc_code__git>");
	insDstordBuff.append("<chg_date><![CDATA[").append(youknow).append("]]></chg_date>");
	insDstordBuff.append("<site_code><![CDATA[").append(loginSite).append("]]></site_code>");
	insDstordBuff.append("<status><![CDATA[").append("P").append("]]></status>");
	insDstordBuff.append("<order_type><![CDATA[").append("F").append("]]></order_type>");
	insDstordBuff.append("<loc_code__cons><![CDATA[").append(locCodeCons).append("]]></loc_code__cons>");
	insDstordBuff.append("<auto_receipt><![CDATA[").append().append("]]></auto_receipt>");
	insDstordBuff.append("<tran_type><![CDATA[").append().append("]]></tran_type>");
	insDstordBuff.append("<curr_code><![CDATA[").append(currCode).append("]]></curr_code>");
	insDstordBuff.append("<exch_rate><![CDATA[").append("1").append("]]></exch_rate>");
	insDstordBuff.append("<loc_code__gitbf><![CDATA[").append(locCodeGitBf).append("]]></loc_code__gitbf>");
	insDstordBuff.append("<avaliable_yn><![CDATA[").append("Y").append("]]></avaliable_yn>");
	insDstordBuff.append("<tot_amt><![CDATA[").append().append("]]></tot_amt>");
	insDstordBuff.append("<tax_amt><![CDATA[").append().append("]]></tax_amt>");
	insDstordBuff.append("<net_amt><![CDATA[").append().append("]]></net_amt>");
	insDstordBuff.append("<trans_mode><![CDATA[").append().append("]]></trans_mode>");
insDstordBuff.append("</Detail1>");
		
//Coding for distorder detail data

insDstordBuff.append("<Detail2 objContext =\"1\"").append(" objName=\"inv_hold\" domID=\"1\"	dbID=\"\">");
insDstordBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
	insDstordBuff.append("<item_code>").append(itemCode).append("</item_code>");
	insDstordBuff.append("<qty_order>").append(rs.getString("site_code")).append("</qty_order>");					
	insDstordBuff.append("<qty_confirm>").append(rs.getString("loc_code")).append("</qty_confirm>");
	insDstordBuff.append("<qty_received>").append(lotNo).append("</qty_received>");
	insDstordBuff.append("<qty_shipped>").append("H").append("</qty_shipped>");
	insDstordBuff.append("<due_date>").append(sdf.format(releaseDate.getTime())).append("</due_date>");
	insDstordBuff.append("<tax_class>").append(lotSl).append("</tax_class>");
	insDstordBuff.append("<tax_env>").append(lineNoSl).append("</tax_env>");
	insDstordBuff.append("<unit>").append(itemCode).append("</unit>");
	insDstordBuff.append("<rate>").append(rs.getString("site_code")).append("</rate>");					
	insDstordBuff.append("<qty_return>").append(rs.getString("loc_code")).append("</qty_return>");
	insDstordBuff.append("<rate__clg>").append(lotNo).append("</rate__clg>");
	insDstordBuff.append("<discount>").append("H").append("</discount>");
	insDstordBuff.append("<tot_amt>").append(sdf.format(releaseDate.getTime())).append("</tot_amt>");
	insDstordBuff.append("<tax_amt>").append(lotSl).append("</tax_amt>");
	insDstordBuff.append("<net_amt>").append(lineNoSl).append("</net_amt>");
	insDstordBuff.append("<over_ship_perc>").append("H").append("</over_ship_perc>");
	insDstordBuff.append("<conv__qty__alt>").append(sdf.format(releaseDate.getTime())).append("</conv__qty__alt>");
	insDstordBuff.append("<qty_order__alt>").append(lotSl).append("</qty_order__alt>");
	insDstordBuff.append("<quantity__fc>").append(lineNoSl).append("</quantity__fc>");
insDstordBuff.append("</Detail2>");
			
insDstordBuff.append("</Header0>");
insDstordBuff.append("</group0>");
insDstordBuff.append("</DocumentRoot>");
xmlString= insDstordBuff.toString();
retString = masterStateful.processRequest( authencate, siteCode, true, xmlString, true, conn );		
if(retString.indexOf( "Success" ) == -1)
	throw new Exception("Exception:confirm incident>>putstock on hold method:Exception while calling MasterStateful.processRequest");
else
	retString = "";
insDstordBuff.delete(0,insDstordBuff.length()); */