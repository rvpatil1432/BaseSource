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
public class SFrcastSitesCrossTabPrc extends ProcessEJB implements SFrcastSitesCrossTabPrcLocal, SFrcastSitesCrossTabPrcRemote
{
  	String[] siteArr;
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
		String periodFrom = "";
		String periodTo = "";
		String itemSeries = "";
		String sql = "";
		String allSites = "";
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
			periodFrom = genericUtility.getColumnValue("prd_from",dom);
			periodTo = genericUtility.getColumnValue("prd_to",dom);
			itemSeries = genericUtility.getColumnValue("item_ser",dom);
			ArrayList allDataList = null;
			siteArr = new String[50];
			
			allDataList = new ArrayList();
			getAllDataBean dataBean = null;
			int i = 0;
			
			sql =  "select a.item_code , b.descr , c.site_code , a.quantity "
				  +" from salesforecast_det a , item b , salesforecast_hdr c "
				  +" where a.tran_id = c.tran_id "
				  +" and a.item_code = b.item_code "
				  +" and a.prd_code__for = ? "
				  +" and c.item_ser = ? "
				  +" and c.prd_code__from = ? "
				  +" and c.prd_code__to = ? order by a.item_code";
				  
			pstmt = conn.prepareStatement( sql );
		
			pstmt.setString( 1, periodFrom.trim() );
			pstmt.setString( 2, itemSeries.trim() );
			pstmt.setString( 3, periodFrom.trim() );
			pstmt.setString( 4, periodTo.trim() );
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				dataBean = new getAllDataBean();
				
				dataBean.itemCdBn = rs.getString( "item_code" );
				dataBean.itemDcrBn = rs.getString( "descr" );
				dataBean.siteCdBn = rs.getString( "site_code" );
				dataBean.quantity = rs.getDouble( "quantity" );
				
				allDataList.add(dataBean);
				
				if( allSites.indexOf( dataBean.siteCdBn.trim() ) == -1 )
				{
					allSites = allSites + dataBean.siteCdBn.trim();
					if( i < 50 )
					{
						siteArr[i] = dataBean.siteCdBn.trim();
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
				resultString = getXmlData( allDataList, siteArr, conn );
			}
			if( resultString.equals("") )
			{
				errCode ="VTNORECFND";
				resultString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
			}
		}
		catch (SQLException e)
		{
			System.out.println("Exception :SFrcastSitesCrossTab :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :SFrcastSitesCrossTab :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
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
			System.out.println("Exception :SFrcastSitesCrossTab :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return retStr;
	}

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String resultString = "";
		String childNodeName = "";
		String errString = "";
		String procQuantity = "";
		String destSite = "";
		boolean errFlag = true;
		Connection conn = null; 
		int childNodeListLength = 0;		
		int parentNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String distDemandType = "";
		ConnDriver connDriver = new ConnDriver();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
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
			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
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
					if (childNodeName.equals("item_code"))
					{
						procdb.itemCdBn = childNode.getFirstChild().getNodeValue();
						
						if( procdb.itemCdBn != null && procdb.itemCdBn.trim().length() > 0 )	
						{
							if( procdb.itemCdBn.trim().equals("Site Codes") || procdb.itemCdBn.trim().equals("Description") )
							{
								System.out.println("Executing break ......");
								break;
							}
						}
					}
					else if (childNodeName.equals("total"))
					{
						//total = childNode.getFirstChild().getNodeValue();
					}
					else if (childNodeName.equals("rate"))
					{
						//rate = childNode.getFirstChild().getNodeValue();	
					}
					else if (childNodeName.equals("value"))
					{
						//value = childNode.getFirstChild().getNodeValue();	
					}
					else if (childNodeName.equals("pack_size"))
					{
						//packSize = childNode.getFirstChild().getNodeValue();	
					}
					else if (childNodeName.equals("item_descr"))
					{
						procdb.itemDcrBn = childNode.getFirstChild().getNodeValue();	
					}
					else
					{
						for( int i = 1; i <= 50; i++ )
						{
							if(childNodeName.equals("site"+i))
							{
								procQuantity = childNode.getFirstChild() == null ? "0" : childNode.getFirstChild().getNodeValue();
								procdb.SiteQtyArr[i-1] = procQuantity;
								//procdb.siteCdBn[i-1] = siteArr[i-1];
							}
						}
					}
				}
				procList.add(procdb);
			}
			errString = updateSalesForecast( procList, headerDom, conn );
			if( errString.equals("") )
			{
				errFlag = false;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SFrcastSitesCrossTab :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
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
				}
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception se){}
		}
		return resultString;
	}
	private String updateSalesForecast( ArrayList procList, Document headerDom, Connection conn ) throws RemoteException,ITMException
	{
		ResultSet rs = null;
		PreparedStatement updtPstmt = null;
		PreparedStatement pstmt = null;
		String quantity = "", tranId = "", siteCode = "";
		double updtQty = 0.0;
		procDataBean procBn = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		procBn = new procDataBean();
		try
		{
			String periodFrom = genericUtility.getColumnValue("prd_from",headerDom);
			String periodTo = genericUtility.getColumnValue("prd_to",headerDom);
			String itemSeries = genericUtility.getColumnValue("item_ser",headerDom);
			
			for( int listCtr = 0; listCtr < procList.size(); listCtr++)
			{
				procBn = (procDataBean)procList.get(listCtr);
				for( int qtyCnt = 0; qtyCnt < 50; qtyCnt++)
				{
					quantity = procBn.SiteQtyArr[qtyCnt];
					siteCode = siteArr[qtyCnt];
					System.out.println("quantity is ==>"+quantity);
					System.out.println("siteCode is ==>"+siteCode);
					if( quantity != null && quantity.trim().length() > 0 && !quantity.trim().equals("0") && siteCode != null )
					{
						System.out.println("Inside quantity if......");
						updtQty = Double.parseDouble(quantity);
						String sql = "Select a.tran_id from salesforecast_hdr a, salesforecast_det b "
									+" where a.tran_id = b.tran_id "
									+" and b.item_code = ? "
									+" and b.prd_code__for = ? "
									+" and a.item_ser = ? "
									+" and a.prd_code__from = ? "
									+" and a.prd_code__to = ? "
									+" and a.site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString( 1, procBn.itemCdBn );
						pstmt.setString( 2, periodFrom );
						pstmt.setString( 3, itemSeries );
						pstmt.setString( 4, periodFrom );
						pstmt.setString( 5, periodTo );
						pstmt.setString( 6, siteCode );
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							tranId = rs.getString("tran_id");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						
						sql = "Update salesforecast_det set quantity = ? "
									+" where tran_id = ? "
									+" and item_code = ? "
									+" and prd_code__for = ? ";
						updtPstmt = conn.prepareStatement(sql);
						updtPstmt.setDouble( 1, updtQty );
						updtPstmt.setString( 2, tranId );
						updtPstmt.setString( 3, procBn.itemCdBn );
						updtPstmt.setString( 4, periodFrom );
						int updtCnt = updtPstmt.executeUpdate();
						updtPstmt.close();
						updtPstmt = null;
					}
				}
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
				if(updtPstmt != null)updtPstmt.close();
				updtPstmt = null;
			}
			catch(SQLException sqle)
			{
				sqle.printStackTrace();
			}
		}
		return "";
	}
	
	public String getXmlData( ArrayList dataList, String[] siteArr, Connection conn ) throws RemoteException,ITMException
	{
		StringBuffer salesForecastStrBuff = new StringBuffer();
		getAllDataBean dataBean = new getAllDataBean();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String packSize = "";
		
		try
		{
			String allItems = "";
			double total = 0.0;
			double value = 0.0;
			double rate = 0.0;
			String sql = "";
			
			salesForecastStrBuff.append("Site Codes").append("\t");
			salesForecastStrBuff.append("").append("\t");   //item description
			salesForecastStrBuff.append("").append("\t");   //pack size
			for( int Sitectr = 0; Sitectr < 50; Sitectr++ )
			{
				String siteClm = siteArr[Sitectr];
				salesForecastStrBuff.append( siteClm == null ? "" : siteClm ).append("\t");
			}
			salesForecastStrBuff.append("").append("\t");   //value
			salesForecastStrBuff.append("").append("\t");   //total
			salesForecastStrBuff.append("").append("\t");   //rate
			salesForecastStrBuff.append("\n");
			salesForecastStrBuff.append("Description").append("\t");
			salesForecastStrBuff.append("").append("\t");
			salesForecastStrBuff.append("").append("\t");
			for( int Sitectr = 0; Sitectr < 50; Sitectr++ )
			{
				String siteClm = siteArr[Sitectr];
				String siteDescr = "";
				if( siteClm != null && siteClm.trim().length() > 0 )
				{
					sql = "Select city from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString( 1, siteClm.trim());
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
				salesForecastStrBuff.append( siteDescr == null ? "" : siteDescr ).append("\t");
			}
			salesForecastStrBuff.append("").append("\t");
			salesForecastStrBuff.append("").append("\t");
			salesForecastStrBuff.append("").append("\t");
			salesForecastStrBuff.append("\n");
			
			for( int listCtr1 = 0; listCtr1 < dataList.size(); listCtr1++ )
			{
				dataBean = ( getAllDataBean )dataList.get( listCtr1 );
				String itemCode = dataBean.itemCdBn;
				String itemDescr = dataBean.itemDcrBn;
				if( allItems.indexOf(itemCode) == -1 )
				{
					total = 0.0;
					salesForecastStrBuff.append( itemCode ).append("\t");
					salesForecastStrBuff.append( itemDescr ).append("\t");
					salesForecastStrBuff.append( packSize ).append("\t");
					for( int Sitectr = 0; Sitectr < 50; Sitectr++ )
					{
						String siteCode = siteArr[Sitectr];
						for( int listCtr = 0; listCtr < dataList.size(); listCtr++ )
						{
							dataBean = ( getAllDataBean )dataList.get( listCtr );
							if( dataBean.itemCdBn.equals(itemCode) && dataBean.siteCdBn.equals(siteCode) )
							{
								salesForecastStrBuff.append( dataBean.quantity );
								total = total + dataBean.quantity;
							}
						}
						salesForecastStrBuff.append("\t");
					}
					rate = getRate( itemCode, conn );
					value = rate * total;
					salesForecastStrBuff.append( value ).append("\t");
					salesForecastStrBuff.append( total ).append("\t");
					salesForecastStrBuff.append( rate ).append("\t");
					salesForecastStrBuff.append("\n");
					allItems = allItems + itemCode;
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return salesForecastStrBuff.toString();
	}
	private double getRate( String itemCode, Connection conn ) throws RemoteException,ITMException
	{
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		double rate = 0;
		String sql = "", priceList = "", lotNoFrom = "";
		String parentPrLst = "";
		
		try
		{
			sql = "select var_value from disparm where prd_code ='999999' and var_name ='DEFAULT_PRICE_LIST'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				priceList = rs.getString("var_value");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			
			System.out.println("priceList is ==>"+priceList);
			System.out.println("Inside while loop....");
			sql = " select lot_no__from, price_list__parent from pricelist "
				 +" where price_list = ? "
				 +" and item_code  = ? "
				 +" and eff_from   = (select max(eff_from) from pricelist "
				 +" where price_list = ? "
				 +" and item_code  = ? )"
				 +" and valid_upto >= ? "
				 +" and rownum = '1'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, priceList );
			pstmt.setString( 2, itemCode );
			pstmt.setString( 3, priceList );
			pstmt.setString( 4, itemCode );
			pstmt.setTimestamp( 5, getCurrdateTsFormat() );
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				lotNoFrom = rs.getString("lot_no__from");
				parentPrLst = rs.getString("price_list__parent");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			System.out.println("lotNoFrom is .."+lotNoFrom);
			System.out.println("parentPrLst is .."+parentPrLst);
			sql = " select rate from pricelist"
				 +" where price_list = ? "
				 +" and item_code  = ? "
				 +" and lot_no__from <= ? "
				 +" and lot_no__to >= ? "
				 +" and list_type = 'B'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, priceList );
			pstmt.setString( 2, itemCode );
			pstmt.setString( 3, lotNoFrom );
			pstmt.setString( 4, lotNoFrom );
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				rate = rs.getDouble("rate");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			System.out.println("rate is .."+rate);
			if( rate == 0 )
			{
				System.out.println("Inside rate == 0  .....");
				sql = " select lot_no__from, price_list__parent from pricelist "
				 +" where price_list = ? "
				 +" and item_code  = ? "
				 +" and eff_from   = (select max(eff_from) from pricelist "
				 +" where price_list = ? "
				 +" and item_code  = ? )"
				 +" and valid_upto >= ? "
				 +" and rownum = '1'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, parentPrLst );
				pstmt.setString( 2, itemCode );
				pstmt.setString( 3, parentPrLst );
				pstmt.setString( 4, itemCode );
				pstmt.setTimestamp( 5, getCurrdateTsFormat() );
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					lotNoFrom = rs.getString("lot_no__from");
					//parentPrLst = rs.getString("price_list__parent");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				
				sql = " select rate from pricelist"
				 +" where price_list = ? "
				 +" and item_code  = ? "
				 +" and lot_no__from <= ? "
				 +" and lot_no__to >= ? "
				 +" and list_type = 'B'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, parentPrLst );
				pstmt.setString( 2, itemCode );
				pstmt.setString( 3, lotNoFrom );
				pstmt.setString( 4, lotNoFrom );
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					rate = rs.getDouble("rate");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		return rate;
	}
	private class getAllDataBean
	{
		public String itemCdBn = "";
		public String itemDcrBn = "";
		public String siteCdBn = "";
		public double quantity = 0.0;
	}
	private class procDataBean
	{
		public String itemCdBn = "";
		public String itemDcrBn = "";
		public String siteCdBn = "";
		public String[] SiteQtyArr = new String[50];
	}
	private Timestamp getCurrdateTsFormat() throws ITMException
    {
        String s = "";	
		Timestamp timestamp = null;		
        //GenericUtility genericUtility = GenericUtility.getInstance();
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
            throw new ITMException(exception); //Added By Mukesh Chauhan on 05/08/19
        }
        return timestamp;
    }
}
