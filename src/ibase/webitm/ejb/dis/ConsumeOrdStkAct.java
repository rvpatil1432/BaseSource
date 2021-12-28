package ibase.webitm.ejb.dis;



import java.rmi.RemoteException;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; 

@Stateless //

public class ConsumeOrdStkAct extends ActionHandlerEJB implements ConsumeOrdStkActLocal,ConsumeOrdStkActRemote//SessionBean
{
	
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String act_type=null;

	/*public void ejbCreate() throws RemoteException, CreateException 
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

  public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}



	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		
		System.out.println(">>>>>>>>>>>>>In actionHandler getStockDetails:");
		Document dom = null;
		Document dom1 = null;
        
		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println(">>>>>>>>>>>>>XML String>:"+xmlString);
				dom = genericUtility.parseString(xmlString); 
				System.out.println(">>>>>>>>>>>>>Dom:"+dom);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println(">>>>>>>>>>>>>>XML String1:"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1);
				System.out.println(">>>>>>>>>>>>>>dom1:"+dom1);
				
			}
			System.out.println(">>>>>>>>>>>>>>>>actionType:"+actionType+":");
						
		
			if (actionType.equalsIgnoreCase("def_items"))
			{
				System.out.println("*********************Before Call getStockDetails*********************");
				retString = getStockDetails(dom,dom1,objContext,xtraParams);//function to fetch data
				
			}
			
		
			 
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :ModelHandler :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from ModelHandler : actionHandler"+retString);
	    return retString;
	}

	 
	 
	private String getStockDetails(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
	    String siteCode="";
	    String locationCode="";
		String itemCode = "";
		String itemDescr = "";
		String unit=null;
		double quantity=0.0;
		String sql="";
		int lineCntr=0;
		int totalCnt=0;
		String stScheme="";
	    String finVarValues="";
		String finValues[] = null;
		String siteValues[] = null;
		String acctCode="";
		String cctrCode="";
		String itemSer="";
		String siteString="";
		String siteCodeValues="";
		boolean flagIntgrlQty=false;
		double currentStock=0.0;
		double integralQty=0.0;
		
		FinCommon finCommon=new FinCommon();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		Connection conn = null;
	    ConnDriver connDriver = new ConnDriver();		
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		NodeList parentNodeList = null;
		Node parentNode=null;
		String winName="";
		
      
		try
		{
			System.out.println("*********************In getStockDetails*********************");
			System.out.println("dom1--> "+dom1);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			itemSer = genericUtility.getColumnValue("item_ser",dom1);
			System.out.println("ItemSeries "+itemSer);
			siteCode = genericUtility.getColumnValue("site_code__req",dom1);
		    locationCode = genericUtility.getColumnValue("loc_code",dom1);	
			System.out.println(">>>>>>>>>>>>>>>siteCode==>"+siteCode);
			System.out.println(">>>>>>>>>>>>>>>LocationCode==>"+locationCode);
			
			
			parentNodeList = dom1.getElementsByTagName("Detail");
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>parentNodeList Detail:"+parentNodeList);
			parentNodeList = dom1.getElementsByTagName("Detail1");
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>parentNodeList Detail1:"+parentNodeList);
			//parentNode = dom.getElementsByTagName("Detail2")[0];
			parentNode = parentNodeList.item(0);
			winName=getObjName(parentNode);					
			System.out.println(">>>>>>>>>>>>>>>winName==>"+winName);
			//get udf2
			sql="SELECT (CASE WHEN udf2 IS NULL THEN ' ' ELSE udf2 END) as udf2 FROM site WHERE site_code =?";	
            pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,siteCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteString=rs.getString("udf2");
				System.out.println(">>>>>>>>>>>>>>>siteString in if==>"+siteString);
				
				 System.out.println(">>>>>>>>>>>>>>>>>>>>>>>In item while integral_qty:"+integralQty);
			}//end of while(rs.next();
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			siteValues = siteString.split(",");
			System.out.println(">>>>>>>>>>>>>>>siteValues:" + siteValues.toString());
			int siteValuesCnt=siteValues.length;
			for(int i=0;i<siteValuesCnt;i++)
			{
				if(i==0)
				{
					siteCodeValues="('"+siteValues[i];
				
			    }
			    else
				{
					siteCodeValues=siteCodeValues+"','"+siteValues[i];
			    
				}
			}
			siteCodeValues=siteCodeValues+"')";
			//siteCodeValues="('SP110','SP110')";
			System.out.println(">>>>>>>>>>>>>>>siteVar:" + siteCodeValues);
			
		  //  sql= "SELECT stk.item_code,itm.descr,itm.unit,sum(quantity) AS quantity FROM item itm ,stock stk WHERE itm.item_code=stk.item_code AND stk.site_code=? and stk.loc_code=? GROUP BY stk.item_code, itm.descr, itm.unit";
		  //  sql="SELECT stk.item_code,itm.descr,itm.unit,itm.item_ser,(CASE WHEN sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END)) IS NULL THEN 0 ELSE sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END)) END) AS current_qty FROM item itm ,stock stk WHERE itm.item_code=stk.item_code AND stk.site_code=? AND stk.loc_code=? GROUP BY stk.item_code, itm.descr,itm.unit,itm.item_ser";
			sql="SELECT stk.item_code,itm.descr,itm.unit,itm.item_ser,(CASE WHEN sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END)) IS NULL THEN 0 ELSE sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END)) END) AS current_qty FROM item itm ,stock stk WHERE itm.item_code=stk.item_code AND stk.site_code=? AND stk.loc_code=? GROUP BY stk.item_code, itm.descr,itm.unit,itm.item_ser " +
					"having (CASE WHEN sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END)) IS NULL THEN 0 ELSE sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END)) END)>0";//Added by chandrashekar on 04-Feb-2015
			if(itemSer!=null && itemSer.trim().length()>0)
			{
				sql+=" AND itm.item_ser = '"+itemSer+"'";
			}
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, locationCode);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				 stScheme="";
			     finVarValues="";
			     finValues=null;
				 acctCode="";
				 cctrCode="";
				 itemSer="";
				 flagIntgrlQty=false;
			     currentStock=0.0;
				 integralQty=0.0;
				 quantity=0.0;
				
				itemCode = rs.getString("item_code");
				itemDescr = rs.getString("descr");
				unit = rs.getString("unit");
				quantity = rs.getDouble("current_qty");
				itemSer = rs.getString("item_ser");
				totalCnt++;
			   //get integral quantity
				//if(currentStock > 0)
				//{  
					lineCntr++;
					sql="SELECT integral_qty FROM siteitem WHERE site_code=? AND  item_code=?";	
	                pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,siteCode);
					pstmt1.setString(2,itemCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						integralQty=rs1.getDouble("integral_qty");
						flagIntgrlQty=true;
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>In siteitem while integral_qty :"+integralQty);
					}//end of while(rs.next();
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(flagIntgrlQty==false)
					{
						//check for integral qty not found in site item..
						sql="SELECT integral_qty FROM item WHERE site_code=? AND  item_code=?";	
		                pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,siteCode);
						pstmt1.setString(2,itemCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							integralQty=rs1.getDouble("integral_qty");
							
							 System.out.println(">>>>>>>>>>>>>>>>>>>>>>>In item while integral_qty:"+integralQty);
						}//end of while(rs.next();
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
											
					}
					//get current stock..
					if(siteString==null || siteString.trim().length()==0)
					{
						sql="SELECT sum(quantity - alloc_qty - CASE WHEN hold_qty IS NULL THEN 0 ELSE hold_qty END )  AS current_stock FROM stock stk, invstat istat where stk.inv_stat = istat.inv_stat and stk.item_code =? and istat.available = 'Y'"; 			
		                pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							currentStock=rs1.getDouble("current_stock");
							
							 System.out.println(">>>>>>>>>>>>>>>>>>>>>>>In if currentStock:"+currentStock);
						}//end of while(rs.next();
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					
					}
					else
					{
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>In ELSE If siteCodeValues:"+siteCodeValues);
						sql="SELECT (CASE WHEN sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END) - (CASE WHEN hold_qty IS NULL THEN 0 ELSE hold_qty END) ) IS NULL THEN 0 ELSE sum((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END) - (CASE WHEN alloc_qty IS NULL THEN 0 ELSE alloc_qty END) - (CASE WHEN hold_qty IS NULL THEN 0 ELSE hold_qty END)) END) as current_stock FROM   stock stk, invstat istat WHERE stk.inv_stat = istat.inv_stat AND stk.item_code =?  and stk.site_code IN "+siteCodeValues+""; 			
		                pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
					//	pstmt1.setString(2, siteCodeValues);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							currentStock=rs1.getDouble("current_stock");
							
							 System.out.println(">>>>>>>>>>>>>>>>>>>>>>>In if currentStock:"+currentStock);
						}//end of while(rs.next();
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						
					}
					
					
					//set scheme using integral qty and current stock..
					 stScheme="Integral Quantity:"+integralQty+"         "+"Current Stock:"+currentStock;
					 System.out.println(">>>>>>>>>>>>>>>>>>>>>>>stScheme of integral qty and current stock:"+stScheme);
					
					// get acct_code and cctr_code form getFromAcctDetr() method...
					
					finVarValues=finCommon.getFromAcctDetr(itemCode,itemSer,"CISS", conn);
					System.out.println(">>>>>>>>>>>>>>>finVar:"+finVarValues);
					finValues = finVarValues.split(",");
					System.out.println(">>>>>>>>>>>>>>>disParamItemSer:" + finValues.toString());
				 	System.out.println(">>>>>>>>>>>>>>>1.acctCode and cctrCode:" +acctCode+","+cctrCode);
					if (finVarValues != null && finVarValues.length() > 1)
					{
	 			     	System.out.println(">>>>>>>>>>>>>>>In finValues>>>>>>>>>>>:");
						int finValuesCnt=finValues.length;
						System.out.println(">>>>>>>>>>>>>>>finValues finValuesCnt:"+finValuesCnt);
						for(int i=0; i<finValuesCnt;i++)
						{
							
							if(i==0)
							{
								 acctCode =finValues[0];
								 System.out.println(">>>>>>>>>>>>>>>finValues for loop if:"+finValues[0]);
							}
							else if(i==1)
							{
								cctrCode =finValues[1];	
								System.out.println(">>>>>>>>>>>>>>>finValues for loop if:"+finValues[1]);
							}
						}
									
					}

					
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no>").append("<![CDATA[").append(lineCntr).append("]]>").append("</line_no>\r\n");
			    	valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
			    	valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr.trim()).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(unit.trim()).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locationCode.trim()).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<acct_code>").append("<![CDATA[").append(acctCode).append("]]>").append("</acct_code>\r\n");
					valueXmlString.append("<cctr_code>").append("<![CDATA[").append(cctrCode).append("]]>").append("</cctr_code>\r\n");
					valueXmlString.append("<st_scheme>").append("<![CDATA[").append(stScheme).append("]]>").append("</st_scheme>\r\n");
					valueXmlString.append("</Detail>");

					
				//}
								
			}//end of while(rs.next();
			System.out.println("*********************In getStockDetails total item found:"+totalCnt);
			System.out.println("*********************In getStockDetails total add item:"+lineCntr);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			valueXmlString.append("</Root>\r\n");		
		}
		catch(Exception e)
		{
			System.out.println("Exception in getStockDetails actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}
			catch(Exception e){}
		}
		System.out.println("******************In Final getStockDetails****************************");
		return valueXmlString.toString();
		
	}//End of getStockDetails

	
	
	
	private String getObjName(Node node) throws Exception

	{

		String objName = null;

		NodeList nodeList = null;

		Node detaulNode = null;

		Node detailNode = null;

		nodeList = node.getChildNodes();

		NamedNodeMap attrMap = node.getAttributes();

		objName = attrMap.getNamedItem( "objName" ).getNodeValue();

		/*

		for(int ctr = 0; ctr < nodeList.getLength(); ctr++ )

		{

			detailNode = nodeList.item(ctr);

			if(detailNode.getNodeName().equalsIgnoreCase("attribute") )

			{

				objName = detailNode.getAttributes().getNamedItem("objName").getNodeValue();

			}

		}

		*/

		return "w_" + objName;
	}
	

}