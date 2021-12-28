
package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import ibase.webitm.ejb.mfg.ExplodeBom;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3


public class GenDistIssuePrc extends ProcessEJB //implements SessionBean
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	//ArrayList itemQtyList = new ArrayList();
	StringBuffer failMsg = null;
	HashMap<String,String> errMap=new HashMap<String, String>();
	String errCode1=" \n Stock does not exist!\n";
	String errCode2=" \n Stock does not exist for few items!\n";//Manoj dtd 03102013 message changed
	String errCode3=" \n Unconfirmed Issue Exists!\n";
	String errCode4=" \n All Quantity Issued.!\n";
	String errCode5=" \n Unconfirmed Dist order or closed!\n";
	String errCode6=" \n Dist order is cancelled!\n";//New error message is added by manoj dtd 07/03/16 to validate cancelled Dist order
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		//System.out.println("Create Method Called....");
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
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		Document dom = null;
		String retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		CreateDistIssue createDistIss = null;
		DistOrdRec rec = null;
		
		String excpMsg = null;
		String sql = "";
		String retString =  null;
		String errCode = "";
		String errMsgStr = null;
		String distOrdFrom = null;
		String distOrdTo = null;
		String fromDateStr = null;
		String toDateStr = null;
		
		Timestamp fromDate = null;
		Timestamp toDate = null;
		String resultStr = null;
		
		ArrayList distOrderList = null;
		
		Connection conn = null;

		StringBuffer comXmlString = new StringBuffer("<Root>");
		String clubRequired=""; 
		String club="";
		String dOrder="";
		boolean isStockExist=false;
		String  siteCode="";
		String siteCodeDlv="";
		String orderType="";
		String locType="";
		HashMap<String, ArrayList<DistOrderClubBean>> hm = new HashMap<String,ArrayList<DistOrderClubBean>>();
		String udfStr1="";
		int cntIssue=0;
		String rateClg = "",rateFmDistOrd="";
//		boolean errFlag = false;
		String tranType = "";
		//System.out.println("xmlString[process]::::::::::;;;"+xmlString);
		//System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
		//System.out.println("windowName[process]::::::::::;;;"+windowName);
		//System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString);
			}
		}
		catch (Exception e)
		{
			//System.out.println("Exception PrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			excpMsg = e.getMessage();
		}
		try
		{
			 ConnDriver connDriver = new ConnDriver();
			// conn = connDriver.getConnectDB( "DriverITM" ); //Commented by Nandkumar Gadkari on 03/07/18 
			 connDriver = null; 
			 conn = getConnection();//changes by Nandkumar Gadkari on 03/07/18 
			 conn.setAutoCommit( false );
			 
			 createDistIss = new CreateDistIssue();
			 distOrderList = new ArrayList();
			 
			 failMsg = new StringBuffer( "" );
			 
			 distOrdFrom = genericUtility.getColumnValue( "dist_order__fr", dom );
			 distOrdTo = genericUtility.getColumnValue( "dist_order__to", dom );
			 fromDateStr = genericUtility.getColumnValue( "order_date__fr", dom );
			 toDateStr = genericUtility.getColumnValue( "order_date__to", dom );
			 fromDateStr = genericUtility.getValidDateString( fromDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
			 fromDate = java.sql.Timestamp.valueOf( fromDateStr + " 00:00:00.0" );
			 //System.out.println( "fromDate..............." + fromDate );
			 toDateStr = genericUtility.getValidDateString( toDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
			 toDate =java.sql.Timestamp.valueOf( toDateStr + " 00:00:00.0");
			 //System.out.println( "toDate..............." + toDate);
             
			 // changes done by ManojSharma  4/Apr/12
             clubRequired=genericUtility.getColumnValue( "club", dom );
//             if(clubRequired.equalsIgnoreCase("N"))
             if("N".equalsIgnoreCase(clubRequired))
			 {	
			
            	sql = "SELECT DIST_ORDER, SITE_CODE "
			        + " FROM DISTORDER "
			        + " WHERE DIST_ORDER >= '" + distOrdFrom + "' "
				    + " AND DIST_ORDER <= '" + distOrdTo + "' "
				   // + "	AND CONFIRMED = 'Y' "
				    //+ "	AND	STATUS <> 'C' "
				    + " AND ORDER_DATE between ? AND ? ";
				   
				//System.out.println("sql..............."+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp( 1, fromDate );
			    pstmt.setTimestamp( 2, toDate );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					rec = new DistOrdRec();
					
					rec.distOrder = rs.getString( "DIST_ORDER" );
					rec.siteCode = rs.getString( "SITE_CODE" );
					distOrderList.add( rec );
				}
			
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				int distOrderListLen = distOrderList.size();
				
				resultStr = null;
				for( int distIdx = 0; distIdx < distOrderListLen; distIdx++ )
				{
					conn.setAutoCommit(false);//Added By Manoj 15/01/2013 to set Autocommit false in loop
					rec = ( DistOrdRec ) distOrderList.get( distIdx );
					if( isStockExist( rec.distOrder, conn ) )
					{
						resultStr = createDistIss.createDistIssue( rec.siteCode, "<Root><TranID>" + rec.distOrder + "</TranID></Root>", xtraParams, conn );
						System.out.println("172---resultStr----"+resultStr);
						/*Commented by manoj dtd 03102013 not required
						if ( resultStr == null || resultStr.trim().length() == 0  )
						{
							resultStr = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);
							cntIssue++;
						}*/
						if( resultStr != null && !( resultStr.toLowerCase().indexOf( "success" ) > -1 ) )
						{
//							errFlag = true;
							System.out.println("----Rollbacking----"+resultStr);
							 if(resultStr.contains("NODETAILEXIST"))
							 {
								 failMsg.append( rec.distOrder + " Detail Records couldn't Inserted!\n" );
							 }
							conn.rollback();//Added By Manoj 15/01/2013 to rollback transaction
							//break;
						}
						if( resultStr != null && ( resultStr.toLowerCase().indexOf( "success" ) > -1 ) )
						{
							System.out.println("----Commiting----"+resultStr);
							conn.commit(); //Added By Manoj 15/01/2013 to commit transaction in loop
//							if(errFlag)
//								resultStr = itmDBAccessEJB.getErrorString("","VTDENTCONF","BASE","",conn);
//							else
							resultStr = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);//Added by manoj dtd 03102013 to show success message
							cntIssue++;
						}
					}
					else
					{
						//chandni error stock does not exist
						errMsgStr = itmDBAccessEJB.getErrorString("","VTDESNCONF","BASE","",conn);
					}
					System.out.println("335---resultStr---"+resultStr);
					if( resultStr != null && !( resultStr.toLowerCase().indexOf( "success" ) > -1 ) )
					{
						//msalam on 220609 for showing actual error coming from createDistIssue.
						//retString = "VTFAIL";
						retString = resultStr;
					}
					else
					{
						System.out.println("in else after create dist iss exp ::  "+retString);	
						//commented for test on 220609
						retString = null;
					}
				}
				
			}
             //added else part by ManojSharma for adding logic for clubbing dist_order Req.Id DI1ISUN005
			else  
			{
				 sql=" SELECT D.DIST_ORDER,D.LINE_NO AS LINE_NO,D.TRAN_ID__DEMAND,D.ITEM_CODE AS ITEM_CODE,D.QTY_ORDER AS QTY_ORDER,D.QTY_CONFIRM AS QTY_CONFIRM,"
					+" D.QTY_RECEIVED AS QTY_RECEIVED,D.QTY_SHIPPED AS QTY_SHIPPED,D.DUE_DATE AS DUE_DATE,D.TAX_CLASS AS TAX_CLASS,D.TAX_CHAP AS TAX_CHAP,D.TAX_ENV AS TAX_ENV,D.UNIT AS UNIT,ITEM.DESCR AS ITEM_DESCR,"
					+" D.SALE_ORDER AS SALE_ORDER,D.LINE_NO__SORD AS LINE_NO__SORD,D.RATE AS RATE,D.QTY_RETURN AS QTY_RETURN,D.RATE__CLG AS RATE__CLG,D.DISCOUNT AS DISCOUNT,D.REMARKS AS REMARKS,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,"
					+" D.NET_AMT AS NET_AMT,D.OVER_SHIP_PERC AS OVER_SHIP_PERC,SPACE(300) AS QTY_DETAILS,D.UNIT__ALT AS UNIT__ALT,D.CONV__QTY__ALT AS CONV__QTY__ALT,"
					+" D.QTY_ORDER__ALT AS QTY_ORDER__ALT,D.SHIP_DATE AS SHIP_DATE,D.PACK_INSTR AS PACK_INSTR ,"
					+" ( CASE WHEN ITEM.DEPT_CODE__ISS IS NULL then ' ' else ITEM.DEPT_CODE__ISS END ) AS DEPT_CODE, "
					+" H.AVALIABLE_YN AS AVALIABLE_YN, H.TRAN_TYPE AS TRAN_TYPE,ITEM.LOC_TYPE LOC_TYPE,H.SITE_CODE SITE_CODE,H.SITE_CODE__DLV,H.ORDER_TYPE  "
					+" FROM DISTORDER_DET  D,ITEM  ITEM, DISTORDER H "
					+" WHERE D.DIST_ORDER = H.DIST_ORDER "
					+" AND D.ITEM_CODE = ITEM.ITEM_CODE "
					+" AND H.DIST_ORDER     >= '" + distOrdFrom + "' and H.DIST_ORDER<='" + distOrdTo + "'"
					+" AND ORDER_DATE between ? AND ? "
					//+" AND CONFIRMED = 'Y' "
					+" AND	CASE WHEN D.STATUS IS NULL THEN 'O' ELSE D.STATUS END <> 'C' "//Added by manoj dtd 24/12/2013 to exclude closed lines
					+" ORDER BY item.dept_code__iss ASC,"   //added by rajendra on 02/09/08
					+" D.LINE_NO ASC ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp( 1, fromDate );
			        pstmt.setTimestamp( 2, toDate );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
				    dOrder=rs.getString("DIST_ORDER");
				    distOrderList.add(dOrder);
				    siteCode = rs.getString("SITE_CODE");
				    siteCodeDlv = rs.getString("SITE_CODE__DLV");
				    orderType = rs.getString("ORDER_TYPE");
				    locType = rs.getString("LOC_TYPE");
				    tranType = rs.getString("TRAN_TYPE");//including tran type added by vishakha 07/05/2015
				    rateClg = Double.toString(rs.getDouble( "RATE__CLG" )); // ADDED BY RITESH ON 17/SEP/2014
				    rateFmDistOrd = Double.toString(rs.getDouble( "RATE" )); // ADDED BY RITESH ON 18/SEP/2014
				    if (siteCode == null ) siteCode ="";
				    if (siteCodeDlv == null ) siteCodeDlv ="";
				    if (locType == null ) locType ="";
				    if (orderType == null ) orderType ="";
				    if(tranType == null) tranType="";//added by vishakha  on 07/05/2015
				    pstmt1=conn.prepareStatement("select udf_str1 from gencodes where fld_name='LOC_TYPE' AND fld_value=?");
				    pstmt1.setString(1, locType);
				    rs1=pstmt1.executeQuery();
				    if(rs1.next())
				    {
				    	udfStr1=rs1.getString(1);
				    	
				    	if (udfStr1 == null )
				    		udfStr1 ="";
				    }
				    rs1.close();
				    rs1=null;
				    pstmt1.close();
				    pstmt1=null;
				    /*Commented by manoj dtd 20/02/2013 to change clubbing criteria based on udf_str1 column defined ingencodes for location type
				     if((siteCode.length()>0) && (siteCodeDlv.length()>0 ) && (orderType.length() >0) && (locType.length() >0))
					{
						club = siteCode.trim()+","+siteCodeDlv.trim()+","+orderType.trim()+","+locType.trim();
					}
					else
					{
						club = siteCode+","+siteCodeDlv+","+orderType+","+locType;
					}*/
				    if((siteCode.length()>0) && (siteCodeDlv.length()>0 ) && (orderType.length() >0) && tranType.length() > 0) // vishakha Changes for D14ASUN007 - 6/May/2015
					{
						club = siteCode.trim()+","+siteCodeDlv.trim()+","+orderType.trim()+","+udfStr1.trim()+","+tranType.trim();//vishakha Changes for D14ASUN007 - 6/May/2015
					}
					else
					{
						club = siteCode+","+siteCodeDlv+","+orderType+","+udfStr1+","+tranType.trim();//vishakha Changes for D14ASUN007 - 6/May/2015
					}
					
				    DistOrderClubBean db = new DistOrderClubBean();
					db.setSiteCode(siteCode);
					db.setTranType(rs.getString("TRAN_TYPE"));
					db.setAvailableYn(rs.getString("AVALIABLE_YN"));
					db.setDeptCode(rs.getString("DEPT_CODE"));
					db.setLineNo(rs.getInt("LINE_NO"));
					db.setUnit(rs.getString("UNIT"));
					db.setUnitAlt(rs.getString("UNIT__ALT"));
					db.setItemCode(rs.getString("ITEM_CODE"));
					db.setQtyConfirm(rs.getDouble("QTY_CONFIRM"));
					db.setQtyShipped(rs.getDouble("QTY_SHIPPED"));
					db.setDiscount(rs.getDouble("DISCOUNT"));
					db.setDistOrdNo(dOrder);
					db.setRateFmDistOrd(rateFmDistOrd);
					db.setRateClgFmDistOrd(rateClg);
					ArrayList<DistOrderClubBean> inputList =new ArrayList<DistOrderClubBean>();
					inputList.add(db);
					System.out.println("The content of arraylist is: " + inputList);
			
					ArrayList<DistOrderClubBean> clubList =new ArrayList<DistOrderClubBean>();
					if (hm.containsKey(club))
					{	
						clubList= hm.get(club);
						clubList.add(db);
						hm.put(club,clubList);
					}
					else
					{
						hm.put(club,inputList);
					}
					System.out.println("The size of hm is:"+hm.size());
					System.out.println("printing hm:"+hm);
					
				} // end of while 
				
				for(int i=0;i<distOrderList.size();i++)
				{
					dOrder=(String) distOrderList.get(i);
					if(!isStockExist( dOrder, conn ) )
					{
						isStockExist=false;
						break;
					}
					else
					{
						isStockExist=true;
					}

				} // end of for 
				System.out.println("is stock exist::"+isStockExist);
				if(isStockExist==true)
				{
					Set<Map.Entry<String, ArrayList<DistOrderClubBean>>> set = hm.entrySet();
					for (Map.Entry<String, ArrayList<DistOrderClubBean>> me : set)
					{
						conn.setAutoCommit(false);//Added By Manoj 15/01/2013 to set Autocommit false in loop
						ArrayList<DistOrderClubBean> distOrdBean = me.getValue();	
						
						//resultStr = createDistIss.createDistIssue( "",distOrdBean,xtraParams, conn,rateClg,rateFmDistOrd );   
						resultStr = createDistIss.createDistIssue( "",distOrdBean,xtraParams, conn );
						System.out.println("return resultStr---"+resultStr);
						/*if ( resultStr == null || resultStr.trim().length() == 0  )
						{
							resultStr = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);
							cntIssue++;
						}*/
						if( resultStr != null && !( resultStr.toLowerCase().indexOf( "success" ) > -1 ) )
						{
//							errFlag = true;
							if(resultStr.contains("NODETAILEXIST"))
							 {
								 failMsg.append( resultStr.substring(14, resultStr.length()) + " Detail Records couldn't Inserted!\n" );
								 //resultStr=itmDBAccessEJB.getErrorString( "", "VTDESNCONF", "BASE" ,"",conn);
							 }
							System.out.println("----Rollbacking----");
							conn.rollback();//Added By Manoj 15/01/2013 to rollback transaction
							//break;
						}
						if( resultStr != null && ( resultStr.toLowerCase().indexOf( "success" ) > -1 ) )
						{
							System.out.println("----Commiting----");
							conn.commit(); //Added By Manoj 15/01/2013 to commit transaction in loop
//							if(errFlag)
//								resultStr = itmDBAccessEJB.getErrorString("","VTDENTCONF","BASE","",conn);
//							else
							resultStr = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);
							cntIssue++;
						}
					}
				}
				else
				{
					//chandni error stock does not exist
					errMsgStr = itmDBAccessEJB.getErrorString("","VTDESNCONF","BASE","",conn);
				}
				System.out.println("335---resultStr---"+resultStr);
				if( resultStr != null && !( resultStr.toLowerCase().indexOf( "success" ) > -1 ) )
				{
					//msalam on 220609 for showing actual error coming from createDistIssue.
					//retString = "VTFAIL";
					retString = resultStr;
				}
				else
				{
					System.out.println("in else after create dist iss exp ::  "+retString);	
					//commented for test on 220609
					retString = null;
				}
			} // end of else ended by ManojSharma  
		}
		catch(ITMException itme)
		{
		   System.out.println("returning in itme ::  "+retString);
		   retString = "ERROR";
		  /* excpMsg = itme.getMessage();*/ //Commented By Mukesh Chauhan on 07/08/19
		   //System.out.println("Exception in...."+e.getMessage());
		   itme.printStackTrace();
		   throw itme; //Added By Mukesh Chauhan on 07/08/19
		}
	   	catch(Exception e)
		{
		   //System.out.println("returning in exp ::  "+retString);
		   retString = "ERROR";
		   /*excpMsg = e.getMessage();*/ //Commented By Mukesh Chauhan on 07/08/19
		   //System.out.println("Exception in...."+e.getMessage());
		   e.printStackTrace();
		   throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		finally
		{
			try
			{
				if (createDistIss != null)
				{
					//createDistIss.clear();
					createDistIss = null;
				}
				if (distOrderList != null)
				{
					//distOrderList.clear();
					distOrderList = null;
				}
				System.out.println( "retString :: " + retString );
				//if ( retString == null || retString.trim().length() == 0 || retString.toLowerCase().indexOf( "success" ) > -1 )
				//{
					//conn.commit();
					//System.out.println("commiting connection.............");
					System.out.println("cntIssue----"+cntIssue);
					if(cntIssue>0)
					{
						conn.commit();
//						if(errFlag)
//							resultStr = itmDBAccessEJB.getErrorString("","VTDENTCONF","BASE","",conn);
//						else
						retString = itmDBAccessEJB.getErrorString("","VTDESPCONF","BASE","",conn);
					}
					else
					{
						retString = itmDBAccessEJB.getErrorString( "", "VTDESNCONF", "BASE" ,"",conn);
						conn.rollback();
					}
					//errString =	itmDBAccessEJB.getErrorString("", errString, "","",conn);
					
					//if( failMsg != null && failMsg.toString().trim().length() > 0 )
					if( errMap != null && errMap.size() > 0 )
					{
					
						
						String begPart = retString.substring( 0, retString.indexOf("<trace>") + 7 );
						String endPart = retString.substring( retString.indexOf("</trace>"));
						String errorFoundstr="Following Dist Orders can not be processded due to :\n";
						if(errMap.containsKey(errCode1))
						{
							errorFoundstr+=errCode1+errMap.get(errCode1);
						}
						/*if(errMap.containsKey(errCode2))
						{
							errorFoundstr+=errCode2+errMap.get(errCode2);
						}*/
						if(errMap.containsKey(errCode3))
						{
							errorFoundstr+=errCode3+errMap.get(errCode3);
						}
						if(errMap.containsKey(errCode4))
						{
							errorFoundstr+=errCode4+errMap.get(errCode4);
						}
						if(errMap.containsKey(errCode5))
						{
							errorFoundstr+=errCode5+errMap.get(errCode5);
						}
						//New error message is added by manoj dtd 07/03/16 to validate cancelled Dist order
						if(errMap.containsKey(errCode6))
						{
							errorFoundstr+=errCode6+errMap.get(errCode6);
						}
						
						
						//String mainStr = begPart + "Following Dist Orders can not be processded due to \n unavailability of stock \n or unconfirmed Distribution Issue already exist :\n" + failMsg.toString() + endPart;
						System.out.println("errorFoundstr-----"+errorFoundstr);
						String mainStr = begPart + errorFoundstr + endPart;
						retString = mainStr;
						begPart =null;
						endPart =null;
						mainStr =null;
					}
				//}
				/*Commented by Manoj dtd 27/09/2013 not Required
				else
			    {
					conn.rollback();
					//System.out.println("connection rollback.............");
					//msalam on 220609 for showing actual error coming from createDistIssue
					//System.out.println( "excpMsg in else :: " + excpMsg );
					//retString = itmDBAccessEJB.getErrorString( "", "VTDESNCONF", "BASE" ,"",conn);
					errMsgStr = itmDBAccessEJB.getErrorString( "", "VTDESNCONF", "BASE" ,"",conn);
					//end by msalam on 220609
				}*/
				cntIssue=0;
				errMap.clear();
				if( rs != null )
				{
					rs.close();
				}
				rs = null;
				if (pstmt != null)
				{
					pstmt.close();
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
			   //System.out.println("Error In closing connection::==> "+e);
			   excpMsg = e.getMessage();
		       e.printStackTrace();
			}
		}
		System.out.println("Printing excpMsg----"+excpMsg);
		System.out.println("Printing errMsgStr----"+errMsgStr);
		if( excpMsg != null && excpMsg.trim().length() > 0 )
		{
			String begPart = retString.substring( 0, retString.indexOf("<trace>") + 7 );
			String endPart = retString.substring( retString.indexOf("</trace>"));
			String mainStr = begPart + "Following Exception has occured\n" + ( excpMsg == null ? "" : excpMsg ) + endPart;
			retString = mainStr;
			begPart =null;
			endPart =null;
			mainStr =null;		
		}else if( errMsgStr != null && errMsgStr.trim().length() > 0 )
		{
			System.out.println("retString i else condition---"+retString);
			String errStr = retString.substring( retString.indexOf("<trace>") + 7, retString.indexOf("</trace>") );
			
			String begPart = errMsgStr.substring( 0, errMsgStr.indexOf("<trace>") + 7 );
			String endPart = errMsgStr.substring( errMsgStr.indexOf("</trace>"));
			String mainStr = begPart + errStr + endPart;
			retString = mainStr;

			begPart =null;
			endPart =null;
			mainStr =null;				
		}
		
		//System.out.println("returning from     "+retString);
	    return (retString);
	}
	private boolean isStockExist( String distOrder, Connection conn ) throws Exception
	{
		String locCodeDamaged = null;
		String res = "";
		String sql = null;
		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String distOrderIss = null;
		String tranIdIss = null;
		String confirmed = null;
		double qtyOrder = 0.0;
		double quantity = 0.0;
		String availableYN = null;
		int countOrdItem=0;
		String subSQL="";
		String locGroupJwiss="";
		// 28/05/09 manoharan avialable_yn added
		sql = "SELECT LOC_CODE__DAMAGED, AVALIABLE_YN "
			+ " FROM DISTORDER WHERE DIST_ORDER = '"+distOrder+"' ";
		pstmt= conn.prepareStatement( sql );
		rs = pstmt.executeQuery();
		if ( rs.next() )
		{
			locCodeDamaged = rs.getString( "LOC_CODE__DAMAGED" );
			//System.out.println("locCodeDamaged :"+locCodeDamaged);

			if( locCodeDamaged == null )
			{
				locCodeDamaged = "";
			}
			// 28/05/09 manoharan available_yn added
			availableYN = rs.getString( "AVALIABLE_YN" );
			if( availableYN == null )
			{
				availableYN = "Y";
			}
			// end 28/05/09 manoharan available_yn added
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;	
		
		if ( locCodeDamaged != null && locCodeDamaged.trim().length() > 0 )
		{
			StringTokenizer st = new StringTokenizer( locCodeDamaged, "," );
			while ( st.hasMoreTokens() )
			{
				res = res + "'" + st.nextToken() + "',";
			}
			res = res.substring( 0, res.length() - 1 );
			//System.out.println("res ::" + res);
			locCodeDamaged = res;
			//System.out.println("locCodeDamaged After String Tockenized ::"+locCodeDamaged);
		}
		
	/*	sql = "SELECT count( 1 ) cnt"
			+"	FROM STOCK A, INVSTAT B, LOCATION C "
			+" WHERE C.INV_STAT = B.INV_STAT "
			+"	AND A.LOC_CODE = C.LOC_CODE "
			+"	AND ( A.ITEM_CODE, A.SITE_CODE ) in ( SELECT D.item_code, H.SITE_CODE__SHIP "
			+"	FROM DISTORDER H, DISTORDER_DET  D "
			+"	WHERE h.dist_order = d.dist_order "
			+"	and D.DIST_ORDER = '" + distOrder + "' ) "   
			+"	AND B.AVAILABLE = ? " 
			+"	AND B.USABLE = ? "
			+"	AND B.STAT_TYPE <> 'S' "
			+"	AND A.QUANTITY - A.ALLOC_QTY > 0 "
			+" 	AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";
		
		if( locCodeDamaged != null && locCodeDamaged.trim().length() > 0 )
		{
			sql = sql + " AND A.LOC_CODE IN (" + locCodeDamaged + ")";
		}*/
		sql="SELECT CASE WHEN LOC_GROUP__JWISS IS NULL THEN ' ' ELSE LOC_GROUP__JWISS END AS LOC_GROUP FROM DISTORDER WHERE DIST_ORDER = ? ";
		pstmt=conn.prepareStatement(sql);
		pstmt.setString(1, distOrder);
		rs=pstmt.executeQuery();
		if(rs.next())
		{
			locGroupJwiss=rs.getString( "LOC_GROUP" );
		}
		rs.close();
		rs=null;
		pstmt.close();
		pstmt=null;
		System.out.println("(locGroupJwiss.trim()).length()----"+(locGroupJwiss.trim()).length());
		if((locGroupJwiss.trim()).length()>0)
		{
			subSQL=" AND C.LOC_GROUP ='"+locGroupJwiss+"' ";
		}
		else
		{
			subSQL="";
		}
		//SQL changed by manoj dtd 11/07/2013 to check stock for location froup
		sql="SELECT COUNT(1) as cnt FROM DISTORDER H, DISTORDER_DET  D WHERE h.dist_order = d.dist_order " +
				" and D.DIST_ORDER = '" + distOrder + "' AND " +
				" (D.item_code, H.SITE_CODE__SHIP) IN " +
				" (SELECT A.ITEM_CODE,A.SITE_CODE    FROM STOCK A, INVSTAT B, LOCATION C " +
				" WHERE C.INV_STAT = B.INV_STAT  AND A.LOC_CODE = C.LOC_CODE " +
				" AND B.AVAILABLE = ? "+
				" AND  B.USABLE = ? " +
				" AND B.STAT_TYPE <> 'S' " +
				  subSQL+
				" AND A.QUANTITY - A.ALLOC_QTY > 0 " +
				  " AND   CASE WHEN D.STATUS IS NULL THEN 'O' ELSE D.STATUS END <>'C' "+//Added by manoj dtd 24/12/2013 to exclude closed lines
				"  AND NOT EXISTS " +
				" (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS')" ;
		if( locCodeDamaged != null && locCodeDamaged.trim().length() > 0 )
		{
			sql = sql + " AND A.LOC_CODE IN (" + locCodeDamaged + ")";
		}
		sql=sql+" )";
		pstmt= conn.prepareStatement( sql );
		// 28/05/09 manoharan available_yn dynamic bind
		pstmt.setString(1,availableYN);
		pstmt.setString(2,availableYN);
		rs = pstmt.executeQuery();
		if ( rs.next() )
		{
			count = rs.getInt( "cnt" );
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;	
		
		if( count == 0 )
		{
			if(errMap!=null && errMap.size()>0)
			{
				if(errMap.containsKey(errCode1))
				{
					errMap.put(errCode1,errMap.get(errCode1)+","+distOrder);
				}
				else
				{
					errMap.put(errCode1, distOrder);
				}
			}
			else
			{
				errMap.put(errCode1, distOrder);
			}
			
			failMsg.append( distOrder + " Stock does not exist!\n" );
		}
		pstmt=conn.prepareStatement("select count(1) from  distorder_det where dist_order=? and status<>'C'");
		pstmt.setString(1,distOrder);
		rs=pstmt.executeQuery();
		if(rs.next())
		{
			countOrdItem=rs.getInt(1);
		}
		rs.close();
		rs=null;
		pstmt.close();
		pstmt=null;
		/*Commented by manoj dtd 03/10/2013 to required to show dist order no. for which stock does not exist for few items
		if(count>0)
		{
			if(count<countOrdItem)
			{
				if(errMap!=null && errMap.size()>0)
				{
					if(errMap.containsKey(errCode2))
					{
						errMap.put(errCode2,errMap.get(errCode2)+","+distOrder);
					}
					else
					{
						errMap.put(errCode2, distOrder);
					}
				}
				else
				{
					errMap.put(errCode2, distOrder);
				}
				
				failMsg.append( distOrder + " Stock does not exist for all items!\n" );
			}
		}
		*/
		int distIssCount = 0;
		/* sql changed as it was not allowing partial processing msalam on 230609
		sql = " select h.dist_order, h.tran_id, h.confirmed, DH.QTY_ORDER, D.QUANTITY "
			+" from DISTORD_ISS H, distord_Issdet D, DISTORDER_DET DH "
			+" where H.dist_order = '" + distOrder + "' "
			+"  AND H.TRAN_ID = D.TRAN_ID "
			+"  AND D.DIST_ORDER = DH.DIST_ORDER "
			+"  AND D.LINE_NO_DIST_ORDER = DH.LINE_NO ";
		*/
		// 21/04/10 manoharan 1st check unconfirmed issues
		sql = " select count(1) as cnt "
			+"	from DISTORD_ISS "
			+"  where dist_order = ? "
			+ " and (case when confirmed is null then 'N' else confirmed end) = 'N' ";
			
		pstmt= conn.prepareStatement( sql );
		pstmt.setString( 1, distOrder );
		
		rs = pstmt.executeQuery();
		
		if ( rs.next() )
		{
			distIssCount = rs.getInt( "cnt" );
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		sql = null;
		boolean issToBeGenerated = true;
		if (distIssCount > 0 )
		{
			issToBeGenerated = false;
			if( errMap!=null && errMap.size()>0)
			{
				if(errMap.containsKey(errCode3))
				{
					errMap.put(errCode3,errMap.get(errCode3)+","+distOrder);
				}
				else
				{
					errMap.put(errCode3, distOrder);
				}
			}
			else
			{
				errMap.put(errCode3, distOrder);
			}
			
			failMsg.append( distOrder + " Unconfirmed Issue Exists!\n" );
		}
		else
		{

			/* 21/04/10 manoharan wrong logic for pending order checking
			sql = " select h.confirmed, D.QTY_ORDER AS QTY_ORDER, D.QUANTITY AS QUANTITY"
				+"	from DISTORD_ISS H, "
				+"			( select sum( DH.QTY_ORDER ) QTY_ORDER, sum( D.QUANTITY )  QUANTITY "
				+"					from DISTORD_ISS H, distord_Issdet D, DISTORDER_DET DH "
				+"			 where H.dist_order = ? " 
				+"					  AND H.TRAN_ID = D.TRAN_ID " 
				+"					  AND D.DIST_ORDER = DH.DIST_ORDER "
				+"					  AND D.LINE_NO_DIST_ORDER = DH.LINE_NO "
				+"			  ) d "                                       
				+"  where H.dist_order = ? ";
				//+"   AND H.TRAN_ID = D.TRAN_ID ";
			*/
			sql = " select sum(ord_qty) ord_qty, sum(iss_qty) iss_qty from ( "
				+ " select d.line_no, d.qty_order ord_qty, "
				+ " (select sum(id.quantity) from distord_issdet id where id.dist_order =  d.dist_order "
				+ " and id.line_no_dist_order = d.line_no ) iss_qty "
				+ " from distorder_det d "
				+ " where d.dist_order = ?" 
				+ " and   CASE WHEN D.STATUS IS NULL THEN 'O' ELSE D.STATUS END<>'C' "//Added by manoj dtd 24/12/2013 to exclude closed lines
				+ " ) " ;
			
			pstmt= conn.prepareStatement( sql );
			pstmt.setString( 1, distOrder );
			//pstmt.setString( 2, distOrder );
			
			rs = pstmt.executeQuery();
			
			if ( rs.next() )
			{
				//confirmed = rs.getString( "confirmed" );
				qtyOrder = rs.getDouble( "ord_qty" );
				quantity = rs.getDouble( "iss_qty" );
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = null;
			
			if( qtyOrder - quantity <= 0 ) 
			{
				issToBeGenerated = false;
				if(errMap!=null && errMap.size()>0)
				{
					if(errMap.containsKey(errCode4))
					{
						errMap.put(errCode4,errMap.get(errCode4)+","+distOrder);
					}
					else
					{
						errMap.put(errCode4, distOrder);
					}
				}
				else
				{
					errMap.put(errCode4, distOrder);
				}
				
				failMsg.append( distOrder + " All Quantity Issued.!\n" );			
			} 
		}
		// end 21/04/10 manoharan 1st check unconfirmed issues
		
		
		//check for unconfirmed dist order not to be processed
		int unConf = 0;
		sql = "SELECT count( 1 ) cnt "
			   +" FROM DISTORDER "
			   +" WHERE DIST_ORDER = '" + distOrder + "' "
			   +"	AND ( CONFIRMED = 'N' OR STATUS = 'C' ) ";
		pstmt= conn.prepareStatement( sql );
		rs = pstmt.executeQuery();
		if ( rs.next() )
		{
			unConf = rs.getInt( "cnt" );
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		sql = null;
		if( unConf > 0 )
		{
			if( errMap!=null && errMap.size()>0)
			{
				System.out.println("1--errMap.get(errCode5)-----"+errMap.get(errCode5));
				if(errMap.containsKey(errCode5))
				{
					errMap.put(errCode5,errMap.get(errCode5)+","+distOrder);
				}
				else
				{
					errMap.put(errCode5, distOrder);
				}
			}
			else
			{
				System.out.println("2--errMap.get(errCode5)-----"+errMap.get(errCode5));
				errMap.put(errCode5, distOrder);
			}
			failMsg.append( distOrder + " Unconfirmed Dist order or closed!\n" );
			
		}
		//New error message is added by manoj dtd 07/03/16 to validate cancelled Dist order
		int canRec=0;
		sql = "SELECT count( 1 ) cnt "
				   +" FROM DISTORDER "
				   +" WHERE DIST_ORDER = '" + distOrder + "' "
				   +"	AND STATUS = 'X'  ";
		pstmt= conn.prepareStatement( sql );
		rs = pstmt.executeQuery();
		if ( rs.next() )
		{
			
			canRec = rs.getInt( "cnt" );
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		sql = null;
		if( canRec > 0 )
		{
			if( errMap!=null && errMap.size()>0)
			{
				System.out.println("1--errMap.get(errCode6)-----"+errMap.get(errCode6));
				if(errMap.containsKey(errCode6))
				{
					errMap.put(errCode6,errMap.get(errCode6)+","+distOrder);
				}
				else
				{
					errMap.put(errCode6, distOrder);
				}
			}
			else
			{
				System.out.println("2--errMap.get(errCode6)-----"+errMap.get(errCode6));
				errMap.put(errCode6, distOrder);
			}
			failMsg.append( distOrder + "Dist order is cancelled!\n" );
			
		}
		System.out.println("inside isstock count:"+count+"issToBeGenerated:"+issToBeGenerated+"unConf:"+unConf);
		return count > 0 && issToBeGenerated && unConf == 0 && canRec==0;
	}
	private class DistOrdRec
	{
		String distOrder = null;
		String siteCode = null;
	}
}
