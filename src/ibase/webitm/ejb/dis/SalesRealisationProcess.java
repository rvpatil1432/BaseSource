package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;

import java.util.*;
import java.sql.*;
import java.io.*;

import java.rmi.RemoteException;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.annotation.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.ejb.Stateless; // added for ejb3


/**
 * <p>Title:</p>
 * <p>Description: The getErrorString() method returns a error String and creates a XML file which describes 
 * about the error in detail.The parseString method takes the XML file in string format and return a DOM 
 * object. The setDom() method sets the DOM object. The getColumnValue() method retrives the column value from
 * the DOM object and it takes column name as argument.
 * </p>
 *<p>Company: Base Information Managment Ltd.</p>
 */

//public class  SalesRealisationProcessEJB extends ProcessEJB implements SessionBean 
@Stateless // added for ejb3
public class  SalesRealisationProcess extends ProcessEJB implements SalesRealisationProcessLocal, SalesRealisationProcessRemote 
{
	SessionContext cSessionContext;
	protected static String DB_NAME = null;
	int insertedRows = 0;
    //GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	ProcessRec rec = null;
	RateMasterRec rateRec = null;
	
	ArrayList processRecList = null;
	ArrayList rateMasterList = null;
	ArrayList defaultRateList = null;
	
	HashMap rateMasterMap = new HashMap();
	HashMap rateDefaultMap = new HashMap();
	HashMap genCodeMap = new HashMap();
	/*
  	public void ejbCreate() throws RemoteException, CreateException
	{
		try
		{
			//BaseLogger.info("ProcessorEJB ejbCreate called.........");			
		}
		catch (Exception e)
		{
			//BaseLogger.error("Exception :ProcessorEJB :ejbCreate :==>"+e);
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
	}*/
	@Resource
	public void setSessCont(SessionContext mSessionContext)
	{
		try
		{
			////BaseLogger.info("ProcessorEJB setSessionContext called.........");
			CommonConstants.setIBASEHOME();
			ProcessEJB.DB_NAME = CommonConstants.DB_NAME; // for ejb3
			this.cSessionContext = mSessionContext;
		}
		catch (Exception e)
		{
			////BaseLogger.error("Exception :ProcessorEJB :setSessionContext :==>"+e);
		}

	}
	
	/*public void setSessionContext(SessionContext mSessionContext) 
	{
		try
		{
			////BaseLogger.info("ProcessorEJB setSessionContext called.........");
			CommonConstants.setIBASEHOME();
			ProcessEJB.DB_NAME = CommonConstants.DB_NAME; // for ejb3
			this.cSessionContext = mSessionContext;
		}
		catch (Exception e)
		{
			////BaseLogger.error("Exception :ProcessorEJB :setSessionContext :==>"+e);
		}
	}*/
	
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		//BaseLogger.info("******* Process Method Called ***********");
		Document dom = null;
		Document dom2 = null;		
		String errString = "";	
		
		//BaseLogger.debug("xmlString : \n"  + xmlString);
		//BaseLogger.debug("xmlString2 :\n "  + xmlString2);
		
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = process( dom, dom2, windowName, xtraParams );			
		}
		catch(Exception e)
		{
			errString = "EXCEPTION";
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		//BaseLogger.debug("SalesRealisationProcessEJB :process() :Return String : [" + errString+"]");
		return ( errString );
	}
	
	public String process(Document dom, Document dom2, String windowName, String xtraParams ) throws RemoteException,ITMException
	{
		String lsSiteCode=null;
		String lsProdLine=null;
		String lsWsheetCode=null;
		String lsReprocess=null;
		String lsCustCodeFr=null,lsItemSerFr=null,lsStateCodeFr=null,lsAreaCodeFr=null;      
		String lsCustCodeTo=null,lsItemSerTo=null,lsStateCodeTo=null,lsAreaCodeTo=null;

		String ldtTranDateFrom = null;
		String ldtTranDateTo = null;

        String userId = null;  
		String errString = "";
		String returnValue=null;
		String insertSql = null;

        ConnDriver conndriver = null;
		Connection connectionObject = null;
		PreparedStatement stmtInsert = null;

		
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int ctr;
		int noOfChilds = -1;
		int currentFormNo = 1;

		ArrayList acctList = new ArrayList();
		
		insertSql = "INSERT INTO SALES_REALISATION (WSHEET_CODE ,SITE_CODE ,TRAN_DATE ,EXP_CODE ,EXP_DESCR ,HIER_REF ,HIER_VALUE ,EXP_TYPE ,EXP_RATE ,EXP_APPLY_ON ,EXP_VALUE , CUST_CODE , STATE_CODE ,AREA_CODE ,PRODUCT_LINE ,ITEM_SER ,ITEM_CODE ,QUANTITY ,NET_WEIGHT ,TOT_WEIGHT ,COMM_AMT ,BASIC_VALUE , DISCOUNT ,OTHER_DISC ,CASH_DISC ,ASS_VALUE ,TAXABLE_VALUE ,INV_VALUE ,EXP_UNIT ,TRAN_ID ,LINE_NO ,EXP_RECO,FRT_RECO,TOD_PERC,FRT_PERC,COMP_VAL,TERR_CODE ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		String sql=null;

		ResultSet rsAcct = null;
		ResultSet rsGencode = null;

		//ITMDBAccessHome itmDBAccessHome = null;
		ITMDBAccessEJB itmDBAccess = null;
		
		ArrayList rateMasterList = null;
		ArrayList rateDefaultList = null;

		//GenericUtility genericUtility=GenericUtility.getInstance();
		long timeReq = System.currentTimeMillis();
		try
		{
			itmDBAccess = new ITMDBAccessEJB();
			conndriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//connectionObject = conndriver.getConnectDB("DriverITM");
			connectionObject = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
	        conndriver = null;
			connectionObject.setAutoCommit( false );
				
			parentList = dom.getElementsByTagName("Detail" + 1);
			parentNode = parentList.item(0);
			childList = parentNode.getChildNodes();
			noOfChilds = childList.getLength();
			for(ctr = 0; ctr < noOfChilds; ctr++)
			{
				childNode = childList.item(ctr);
				childNodeName = childNode.getNodeName();
				switch(currentFormNo)
				{
					case 1:
						if (childNodeName.equals("site_code"))
						{
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue() == null  )
							{
								errString = itmDBAccess.getErrorString("site_code","DSSRSITECD",userId ,"",connectionObject); 
								break;
							}
							else
							{
								String str=childNode.getFirstChild().getNodeValue().trim();
								StringBuffer strBuff = new StringBuffer();
								ArrayList tokenList = genericUtility.getTokenList(str, ",");
								for (int i = 0; i < tokenList.size(); i++ )
								{
									String currToken = (String)tokenList.get(i);
									strBuff.append("'").append(currToken).append("',");
								}
								if (strBuff.charAt(strBuff.length()-1) == ',')
								{
									strBuff.deleteCharAt(strBuff.length() - 1);
								}
								lsSiteCode = strBuff.toString();
							}
						}
						else if (childNodeName.equals("from_date"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								ldtTranDateFrom = childNode.getFirstChild().getNodeValue().trim();
							}
							else
							{
								errString = itmDBAccess.getErrorString("from_date","DSSRDATE",userId,"",connectionObject); 
								break;
							}
						}
						else if (childNodeName.equals("to_date"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
							{
								ldtTranDateTo = childNode.getFirstChild().getNodeValue().trim();
							}	
							else
							{
								errString = itmDBAccess.getErrorString("to_date","DSSRDATE",userId,"",connectionObject); 
								break;
							}
						}
						else if (childNodeName.equals("cust_code_fr"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsCustCodeFr = childNode.getFirstChild().getNodeValue().trim();
							}
							else
							{
								lsCustCodeFr = "00";
							}
						}
						else if (childNodeName.equals("cust_code_to"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsCustCodeTo = childNode.getFirstChild().getNodeValue().trim();
							}
							else
							{
								lsCustCodeFr = "ZZ";
							}
						}
						else if (childNodeName.equals("item_ser_fr"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsItemSerFr = childNode.getFirstChild().getNodeValue().trim();
							}	
							else
							{
								lsCustCodeFr = "00";
							}
						}
						else if (childNodeName.equals("item_ser_to"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsItemSerTo = childNode.getFirstChild().getNodeValue().trim();
							}
							else
							{
								lsCustCodeFr = "ZZ";
							}
						}
						else if (childNodeName.equals("state_code_fr"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsStateCodeFr = childNode.getFirstChild().getNodeValue().trim();
							}
							else
							{
								lsCustCodeFr = "00";
							}
						}
						else if (childNodeName.equals("state_code_to"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsStateCodeTo = childNode.getFirstChild().getNodeValue().trim();
							}	
							else
							{
								lsCustCodeFr = "ZZ";
							}
						}
						else if (childNodeName.equals("area_code_fr"))
						{			
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsAreaCodeFr = childNode.getFirstChild().getNodeValue().trim();							
							}
							else
							{
								lsCustCodeFr = "00";
							}

						}
						else if (childNodeName.equals("area_code_to"))
						{
							if (childNode.getFirstChild() != null)
							{
								lsAreaCodeTo = childNode.getFirstChild().getNodeValue().trim();					
							}
							else
							{
								lsCustCodeFr = "ZZ";
							}
						}
						else if (childNodeName.equals("product_line"))
						{
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsProdLine = childNode.getFirstChild().getNodeValue().trim();							
							}	
							else
							{
								errString = itmDBAccess.getErrorString( "product_line","DSPRODLN",userId ,"",connectionObject); 
								 break;
							}
						}
						else if (childNodeName.equals("worksheet_code"))
						{	
							if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null)
							{
								lsWsheetCode = childNode.getFirstChild().getNodeValue().trim();						
							}
							else
							{
								errString = itmDBAccess.getErrorString("worksheet_code","DSWSHEETCD",userId ,"",connectionObject); 
								 break;
							}
						}
						break;
				}					
			} 
		
            userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
			int processedRows = 0;
			this.insertedRows = 0;
			
			processRecList = getProcessDateList( lsSiteCode,
												lsProdLine,
												lsWsheetCode,
												lsReprocess,
												lsCustCodeFr,
												lsItemSerFr,
												lsStateCodeFr,
												lsAreaCodeFr,
												lsCustCodeTo,
												lsItemSerTo,
												lsStateCodeTo,
												lsAreaCodeTo,
												ldtTranDateFrom,
												ldtTranDateTo,
												connectionObject );
			
			genCodeMap = getGenCodesMap( connectionObject );
			printMap( genCodeMap, "GEN" );
			
			stmtInsert = connectionObject.prepareStatement(insertSql);
			
			DB_NAME =  CommonConstants.DB_NAME;				
			String recordType = "DEFINED";
			rateMasterList = getRateMasterRec( lsWsheetCode, ldtTranDateFrom, ldtTranDateTo, DB_NAME, recordType, connectionObject );
			recordType = "DEFAULT";
			rateDefaultList = getRateMasterRec( lsWsheetCode, ldtTranDateFrom, ldtTranDateTo, DB_NAME, recordType, connectionObject );
			acctList = getAcctList( lsWsheetCode, connectionObject );
			int acctListLen = 0;
			if( acctList != null )
			{
				acctListLen = acctList.size();
			}

			printMap( rateMasterMap, "Rate" );
			printMap( rateDefaultMap, "Def" );
			printMap( genCodeMap, "GEN" );
			
			if( processRecList != null && processRecList.size() > 0 )  
			{                				
				int processRecListLen = processRecList.size();
				
				for( int processRecIdx = 0; processRecIdx < processRecListLen && !( "RecordNotFound".equals( returnValue ) ) ; processRecIdx++ )
				{
					processedRows++;
					
					for( int acctListIdx = 0; acctListIdx < acctListLen && !( "RecordNotFound".equals( returnValue ) ); acctListIdx++ )
					{
						returnValue = calculateExpenseForEachAccount( lsWsheetCode, 
																	lsProdLine,
																	( String )acctList.get( acctListIdx ), 
																	( ProcessRec )processRecList.get( processRecIdx ),
																	stmtInsert );

					}
				}
				stmtInsert.executeBatch();
				stmtInsert.close();
				stmtInsert = null;
			}

			timeReq=System.currentTimeMillis()-timeReq;

			if( this.insertedRows  > 0 && !( "RecordNotFound".equals( returnValue ) ) )
			{
				System.out.println( "***********COMMIT*********" );
				connectionObject.commit();
				errString = itmDBAccess.getErrorString("", "DSSRPROC", userId,"",connectionObject); 
			}
			else
			{
				System.out.println( "***********ROLLBACK*********" );
				errString = itmDBAccess.getErrorString( "", "DSSRPROCZ", userId ,"",connectionObject); 
				connectionObject.rollback();
			}

		}
		catch(Exception e)
		{
			errString = genericUtility.createErrorString( e );
			errString = itmDBAccess.getErrorString( "", "DSSRPROCZ", userId ,"",connectionObject); 
			try{
				System.out.println( "***********ROLLBACK*********" );
				connectionObject.rollback();
			}catch( Exception ex ){}
		}finally
		{
			try
			{

				if( stmtInsert != null )
				{
					stmtInsert.close();
					stmtInsert=null;
				}
				if( connectionObject != null )
				{					
					connectionObject.close();
					connectionObject = null;
				}				
			}
			catch(Exception e){ e.printStackTrace();}
		}

		//BaseLogger.debug("SalesRealisationProcessEJB :process() :Return String : [" + errString+"]");
		return errString;
	}
	private String calculateExpenseForEachAccount(
		String lsWsheetCode,
		String lsProductLine,
		String lsAcctCode,
		ProcessRec processRec,
		PreparedStatement stmtInsert )throws RemoteException,ITMException
	{

		String returnValue = "";
		String columnName = "";   
		String columnValue = "";
		//Milind 
		String columnName1 = "";   
		String columnValue1 = "";
		String columnName2 = "";   
		String columnValue2 = "";
		String columnName3 = "";   
		String columnValue3 = "";
		String hierValue = null;
		String hierValue1 = null;
		String hierValue2 = null;
		String hierValue3 = null;

		double lcNetWght = 0.0;
		double lcTotWght = 0.0;
		double lcQty = 0.0;
		double netWT = 0.0;
					
		boolean recFound = false;
		
		try
		{
			int noOfRec = 0;
			double expValue = 0;
			RateMasterRec rateRec = null;
			String rateKey = null;

			boolean foundFlag = false;

			java.util.Iterator iteratorMap = null;
			java.util.Set setMap = null;
			java.util.Map.Entry entry = null;
			setMap = rateMasterMap.entrySet();
			iteratorMap = setMap.iterator();
			RateMasterRec rec = null;
			//if( iteratorMap == null )
			//{
			//	System.out.println("Null iterator......");
			//}

			while( iteratorMap.hasNext() )
			{
				entry = ( java.util.Map.Entry )iteratorMap.next();
				String tKey = null;
				String supplierCode = null;
				tKey = (String)entry.getKey();
				rateRec = ( RateMasterRec )entry.getValue();
				//System.out.println(" In Process : ( tKey, acctCode, dataValue ) :: ( " + tKey + ", " + rateRec.acctCode + ", " + rateRec.dataValue + " )" );
				foundFlag = false;
				//System.out.println( "rateRec.hierRef " + rateRec.hierRef );
				columnName = genCodeMap.containsKey( (rateRec.hierRef).toString() ) ? genCodeMap.get( (rateRec.hierRef).toString() ).toString() : null;
				columnValue = ( columnName != null ) ? processRec.getColumnVal( columnName ):"";
				
				columnName1 = genCodeMap.containsKey( (rateRec.hierRef1).toString() ) ? genCodeMap.get( (rateRec.hierRef1).toString() ).toString() : null;
				columnValue1 = ( columnName1 != null ) ? processRec.getColumnVal( columnName1 ) : "";												
				
				if( rateRec.hierRef2 != null )
				{
					columnName2 = genCodeMap.containsKey( (rateRec.hierRef2).toString() ) ? genCodeMap.get( (rateRec.hierRef2).toString() ).toString() : null;
					columnValue2 = ( columnName2 != null ) ? processRec.getColumnVal( columnName2 ) : "";												
				}
				if( rateRec.hierRef3 != null )
				{				
					columnName3 = genCodeMap.containsKey( (rateRec.hierRef3).toString() ) ? genCodeMap.get( (rateRec.hierRef3).toString() ).toString() : null;
					columnValue3 = ( columnName3 != null ) ? processRec.getColumnVal( columnName3 ) : "";
				}
				rateKey = ( lsAcctCode == null || lsAcctCode.trim().length() == 0 ? "" : lsAcctCode )
						 + "-" + ( columnValue == null || columnValue.trim().length() == 0 ? "" : columnValue.trim() )
						 + "-" + ( columnValue1 == null || columnValue1.trim().length() == 0 ? "" : columnValue1.trim() )
						 + "-" + ( columnValue2 == null || columnValue2.trim().length() == 0 ? "" : columnValue2.trim() )
						 + "-" + ( columnValue3 == null || columnValue3.trim().length() == 0 ? "" : columnValue3.trim() );
				//System.out.println( "rateKey:: " + rateKey );
				System.out.println( "rateKey :: " + rateKey );
				if( rateMasterMap.containsKey( rateKey ) )
				{
					foundFlag = true;
					break;
				}
			}
			rateRec = null;
			System.out.println( "foundFlag :: " + foundFlag ); 
			if( foundFlag == true )
			{
				rateRec = ( RateMasterRec )rateMasterMap.get( rateKey );
			}
			if( rateRec == null ) 
			{
				rateRec = ( RateMasterRec )rateDefaultMap.get( lsAcctCode );
			}
			if( rateRec == null ) 
			{
				returnValue = "RecordNotFound";
			}
			if( rateRec != null ) 
			{
				netWT = processRec.wt;
				lcQty = processRec.quantityStduom;
				lcTotWght = processRec.totWeight * lcQty;

				if( "A".equals( rateRec.applyOn ) )
				{	
					lcNetWght = netWT;
					lcTotWght = lcTotWght * lcQty;
					expValue = rateRec.dataValue * lcNetWght;								
				}
				else if ( "S".equals( rateRec.applyOn ) )
				{	
					lcNetWght = netWT;

					lcTotWght = lcTotWght * lcQty;
					expValue = rateRec.dataValue * lcNetWght * lcQty;	
				}
				else
				{				
					expValue = calculateExpenseValue( rateRec.dataType, rateRec.dataValue, rateRec.applyOn, processRec.quantityStduom, processRec.basicValue, processRec.assValue, processRec.taxableValue, processRec.invValue );
				}

				stmtInsert.clearParameters();
				stmtInsert.setString( 1, lsWsheetCode );
				stmtInsert.setString( 2, processRec.siteCode );
				stmtInsert.setTimestamp( 3, java.sql.Timestamp.valueOf( processRec.tranDate + " 00:00:00" ) );
				stmtInsert.setString( 4, rateRec.acctCode );
				stmtInsert.setString( 5, rateRec.descr );
				stmtInsert.setString( 6, rateRec.hierRef );
				stmtInsert.setString( 7, rateRec.hierValue );
				stmtInsert.setString( 8, rateRec.dataType );
				stmtInsert.setDouble( 9, rateRec.dataValue );
				stmtInsert.setString( 10, rateRec.applyOn );
				stmtInsert.setDouble( 11, expValue);
				stmtInsert.setString( 12, processRec.custCode);
				stmtInsert.setString( 13, processRec.stateCode);
				stmtInsert.setString( 14, processRec.areaCode);
				stmtInsert.setString( 15, processRec.productLine );
				stmtInsert.setString( 16, processRec.itemSer );
				stmtInsert.setString( 17, processRec.itemCode);
				stmtInsert.setDouble( 18, processRec.quantityStduom );
				stmtInsert.setDouble( 19, lcNetWght );
				stmtInsert.setDouble( 20, lcTotWght );
				stmtInsert.setDouble( 21, processRec.commAmt );
				stmtInsert.setDouble( 22, processRec.basicValue );
				stmtInsert.setDouble( 23, processRec.discount );
				stmtInsert.setDouble( 24, processRec.otherDisc );
				stmtInsert.setDouble( 25, processRec.discount );
				stmtInsert.setDouble( 26, processRec.assValue );
				stmtInsert.setDouble( 27, processRec.taxableValue );
				stmtInsert.setDouble( 28, processRec.invValue );
				stmtInsert.setString( 29, processRec.unit );
				stmtInsert.setString( 30, processRec.tranId );
				String lsLineNo = null;
				lsLineNo = processRec.lineNo;
				lsLineNo = lsLineNo != null ? lsLineNo.trim() : "";
				lsLineNo = lsLineNo.length() > 3 ? lsLineNo.substring( 0, 3 ) : lsLineNo;
				stmtInsert.setString( 31,lsLineNo );	
				stmtInsert.setDouble( 32, processRec.expReco );
				stmtInsert.setDouble( 33, processRec.frtReco );
				stmtInsert.setDouble( 34, processRec.todPerc );
				stmtInsert.setDouble( 35, processRec.frtPerc );
				stmtInsert.setDouble( 36, processRec.compVal );
				stmtInsert.setString( 37, processRec.terrCode );
				
				stmtInsert.addBatch();	 
				insertedRows++;
			}
			//end logic by me
		}catch(Exception e)
		{
			returnValue = "RecordNotFound";
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return returnValue;
	}

	
	private double calculateExpenseValue(String data_type,double data_value, String apply_on,double lc_qty,double lc_basic_value,double assessable_val,double lc_taxable_value,double lc_inv_value)throws RemoteException,ITMException
	{
		double expVal=0;
		//System.out.println( "****************** In calculateExpenseValue *******************" ); 
		//System.out.println( "data_type = " + data_type + "\n data_value = " + data_value + " apply_on = " + apply_on + " lc_qty = " + lc_qty + " lc_basic_value = " + lc_basic_value + " assessable_val = " + assessable_val + " lc_taxable_value =" + lc_taxable_value + " lc_inv_value = " + lc_inv_value ); 
		switch(apply_on.charAt(0))
		{
			case 'Q':
					if(data_type.equals("F"))
					{
						expVal = data_value * lc_qty;
					}
					break;
			case 'B':
					if(data_type.equals("F"))
					{
						expVal = data_value;  
					}
					else
					{
						expVal =(lc_basic_value * data_value)/100;  
					}  
					break;
			case 'V':
					if(data_type.equals("F"))
					{    
						expVal = data_value;  
					}            
					else
					{
						expVal =(assessable_val * data_value)/100;   
					}   
					break;
			case 'T':
					if(data_type.equals("F"))
					{
						expVal = data_value;  
					}
					else
					{  
						expVal = (lc_taxable_value * data_value)/100;  
					}
					break;
			case 'I':
					if(data_type.equals("F"))
					{    
						expVal = data_value;  
					} 
					else
					{   
						expVal = (lc_inv_value * data_value)/100;   
					}   
					break;
			    
			case 'F':
					if(data_type.equals("F"))
					{ 
						expVal = data_value;  
					}   
					break;

		}
		return expVal;
	}
	private ArrayList getProcessDateList( String lsSiteCode,
		String lsProdLine,
		String lsWsheetCode,
		String lsReprocess,
		String lsCustCodeFr,
		String lsItemSerFr,
		String lsStateCodeFr,
		String lsAreaCodeFr,      
		String lsCustCodeTo,
		String lsItemSerTo,
		String lsStateCodeTo,
		String lsAreaCodeTo,
		String ldtTranDateFrom,
		String ldtTranDateTo,
		Connection connectionObject	) throws Exception
	{
		ProcessRec processRec = null;
		processRecList = new ArrayList();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		System.out.println( "Time start :: " + ( new java.util.Date() ).toString() )	;
		
		sql =  "select iv.SITE_CODE,'S-INV' REF_SER, iv.CUST_CODE, st.AREA_CODE, "
				+"		m.ITEM_SER, ivt.ITEM_CODE, m.DESCR, s.qty__stduom, "
				+"		iv.TRAN_DATE, NVL( m.NET_WEIGHT, 0 ) item_net_wt, s.TOT_WEIGHT, s.ivtCOMM_AMT COMM_AMT, "
				+"		s.BASIC_VALUE, ivt.DISCOUNT, s.OTHER_DISC, s.CASH_DISC, "
				+"		s.ASS_VALUE, s.TAXABLE_VALUE,	s.INV_VALUE, st.STATE_CODE, "
				+"		st.DESCR STATE_DESCR, m.UNIT, ivt.DESP_ID TRAN_ID, ivt.DESP_LINE_NO LINE_NO, "
				+"		msr.PRODUCT_LINE, s.EXP_RECO, s.FRT_RECO, s.TOD_PERC, "
				+"		s.FRT_PERC, "
				+"		DDF_GET_COMP_VAL( ivt.ITEM_CODE, s.qty__stduom ) COMP_VAL, " 
				+"		DDF_GET_TERR_CODE(iv.SITE_CODE, iv.CUST_CODE) TERR_CODE, "
				+"		des.Nett_Weight wt "
				+"	from ( "
				+"			SELECT ivt.invoice_id, ivt.line_no, "
				+"					SUM(ivt.QUANTITY__STDUOM) qty__stduom, "
				+"					SUM(ivt.QUANTITY__STDUOM * NVL(m.NET_WEIGHT,0)) TOT_WEIGHT, "
				+"					SUM(ivt.COMM_AMT) ivtCOMM_AMT, "
				+"					SUM(ivt.QUANTITY__STDUOM * ivt.RATE__STDUOM) BASIC_VALUE, "
				+"					SUM(DDF_GET_TAX_VALUE('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'D')) OTHER_DISC, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'CASH_DISC','T')) CASH_DISC, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'EXC_TAX_CODE','A')) ASS_VALUE, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'SALES_TAX_CODE','A')) TAXABLE_VALUE, "
				+"					SUM(ivt.NET_AMT) INV_VALUE, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'EXP_RECO','T')) EXP_RECO, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'FRT_RECO','T')) FRT_RECO, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'TOD_PERC','T')) TOD_PERC, "
				+"					SUM(DDF_GET_TAX_DETAIL('S-INV',ivt.INVOICE_ID,TO_CHAR(ivt.LINE_NO),'FRT_PERC','T')) FRT_PERC "
				+"				FROM INVOICE_TRACE ivt, ITEM m, INVOICE iv "
				+"			WHERE  ivt.INVOICE_ID = iv.invoice_id "
				+"				AND ivt.ITEM_CODE = m.ITEM_CODE "
				+"				AND iv.CONFIRMED = 'Y' " 				
				+"				AND iv.SITE_CODE IN ( " + lsSiteCode + " ) " 
				+"				AND iv.TRAN_DATE BETWEEN ? AND ? "
				+"				AND iv.CUST_CODE BETWEEN '" + lsCustCodeFr + "' AND '" + lsCustCodeTo + "' " 
				+"				AND m.ITEM_SER BETWEEN '" + lsItemSerFr + "' AND '" + lsItemSerTo + "' " 
				+"			GROUP BY ivt.invoice_id, ivt.line_no "
				+"		)s, invoice_trace ivt, invoice iv, item m, ITEMSER msr, CUSTOMER cu, STATE st, DESPATCHDET des "
				+"	where s.INVOICE_ID = ivt.INVOICE_ID " 
				+"		and s.line_no = ivt.line_no "
				+"		and ivt.INVOICE_ID = iv.INVOICE_ID " 
				+"		AND ivt.ITEM_CODE = m.ITEM_CODE "
				+"		AND m.ITEM_SER = msr.ITEM_SER "
				+"		AND iv.CUST_CODE = cu.CUST_CODE "
				+"		AND cu.STATE_CODE = st.STATE_CODE "
				+"		AND IVT.DESP_ID = DES.desp_id "
				+"		AND IVT.DESP_LINE_NO = DES.LINE_NO "
				+"		and iv.CONFIRMED = 'Y' "
				+"		AND iv.SITE_CODE IN ( " + lsSiteCode + " ) " 
				+"		AND iv.TRAN_DATE BETWEEN ? AND ? "
				+"		AND iv.CUST_CODE BETWEEN '" + lsCustCodeFr + "' AND '" + lsCustCodeTo + "' " 
				+"		AND m.ITEM_SER BETWEEN '" + lsItemSerFr + "' AND '" + lsItemSerTo + "' " 
				+"		AND cu.STATE_CODE BETWEEN '" + lsStateCodeFr + "' AND '" + lsStateCodeTo + "' " 
				+"		AND st.AREA_CODE BETWEEN '" + lsAreaCodeFr + "' AND '" + lsAreaCodeTo + "' " 
				+"		AND msr.PRODUCT_LINE = '" + lsProdLine + "' "
				+" union all "
				+"	SELECT diss.SITE_CODE, 'D-ISS' REF_sER, diss.SITE_CODE__DLV CUST_CODE, st.AREA_CODE, "
				+"		m.ITEM_SER, issdet.ITEM_CODE,	m.DESCR, s.issdetQty qty__stduom, "
				+"		diss.TRAN_DATE, NVL( m.NET_WEIGHT, 0 ) item_net_wt, s.TOT_WEIGHT, "   
				+"		0 COMM_AMT,	s.BASIC_VALUE, issdet.DISCOUNT, s.OTHER_DISC, s.CASH_DISC, "
				+"		s.ASS_VALUE, s.TAXABLE_VALUE, s.INV_VALUE, st.STATE_CODE, "
				+"		st.DESCR STATE_DESCR, m.UNIT, issdet.TRAN_ID, to_char(issdet.LINE_NO) LINE_NO, "
				+"		msr.PRODUCT_LINE, s.EXP_RECO, "
				+"		s.FRT_RECO, s.TOD_PERC, "
				+"		s.FRT_PERC, "
				+"		DDF_GET_COMP_VAL( issdet.ITEM_CODE, s.issdetQty ) COMP_VAL, "
				+"		'IU' TERR_CODE, "
				+"		issdet.net_weight wt "
				+" from 	( "
				+"			SELECT issdet.TRAN_ID, issdet.LINE_NO, "
				+"					SUM( issdet.QUANTITY ) issdetQty, "
				+"					SUM( issdet.QUANTITY * NVL( m.NET_WEIGHT, 0 )) TOT_WEIGHT, "
				+"					SUM( issdet.QUANTITY * issdet.RATE ) BASIC_VALUE, "
				+"					SUM(DDF_GET_TAX_VALUE( 'D-ISS', issdet.TRAN_ID, TO_CHAR(issdet.LINE_NO), 'D' ) ) OTHER_DISC, "
				+"					SUM(DDF_GET_TAX_DETAIL( 'D-ISS',issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO), 'CASH_DISC','T')) CASH_DISC, "
				+"					SUM(DDF_GET_TAX_DETAIL( 'D-ISS',issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO), 'EXC_TAX_CODE','A')) ASS_VALUE, "
				+"					SUM(DDF_GET_TAX_VALUE( 'D-ISS',issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO),'E')) TAXABLE_VALUE, "
				+"					SUM(issdet.NET_AMT) INV_VALUE, "
				+"					SUM(DDF_GET_TAX_DETAIL( 'D-ISS', issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO), 'EXP_RECO','T' ) ) EXP_RECO, "
				+"					SUM(DDF_GET_TAX_DETAIL( 'D-ISS', issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO), 'FRT_RECO','T' ) ) FRT_RECO, "
				+"					SUM(DDF_GET_TAX_DETAIL( 'D-ISS', issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO), 'TOD_PERC','T' ) ) TOD_PERC, "
				+"					SUM(DDF_GET_TAX_DETAIL( 'D-ISS', issdet.TRAN_ID,TO_CHAR(issdet.LINE_NO), 'FRT_PERC','T' ) ) FRT_PERC "
				+"				FROM DISTORD_ISSDET issdet, ITEM m, DISTORD_ISS diss "
				+"			WHERE diss.TRAN_ID = issdet.TRAN_ID "
				+"				AND issdet.ITEM_CODE = m.ITEM_CODE " 
				+"				AND diss.CONFIRMED = 'Y' "
				+"				AND diss.SITE_CODE IN ( " + lsSiteCode + " ) " 
				+"				AND diss.TRAN_DATE BETWEEN ? AND ? "
				+"				AND diss.SITE_CODE__DLV BETWEEN '" + lsCustCodeFr + "' AND '" + lsCustCodeTo + "' " 
				+"				AND m.ITEM_SER BETWEEN '" + lsItemSerFr + "' AND '" + lsItemSerTo + "' " 
				+"			GROUP BY issdet.TRAN_ID, issdet.LINE_NO "
				+"		) s, DISTORD_ISSDET issdet, DISTORD_ISS diss, ITEM m, ITEMSER msr, SITE s, STATE st "
				+"	WHERE s.tran_id = issdet.tran_id "
				+"		and s.line_no = issdet.line_no " 
				+"		and issdet.TRAN_ID = diss.TRAN_ID "
				+"		and issdet.ITEM_CODE = m.item_code "
				+"		AND m.ITEM_SER = msr.ITEM_SER "
				+"		AND diss.SITE_CODE__DLV = s.SITE_CODE "
				+"		AND s.STATE_CODE = st.STATE_CODE "
				+"		and diss.CONFIRMED = 'Y' "
				+"		AND diss.SITE_CODE IN ( " + lsSiteCode + " ) " 
				+"		AND diss.TRAN_DATE BETWEEN ? AND ? " 
				+"		AND diss.SITE_CODE__DLV BETWEEN '" + lsCustCodeFr + "' AND '" + lsCustCodeTo + "' " 
				+"		AND m.ITEM_SER BETWEEN '" + lsItemSerFr + "' AND '" + lsItemSerTo + "' " 
				+"		AND s.STATE_CODE BETWEEN '" + lsStateCodeFr + "' AND '" + lsStateCodeTo + "' " 
				+"		AND st.AREA_CODE BETWEEN '" + lsAreaCodeFr + "' AND '" + lsAreaCodeTo + "' " 
				+"		AND msr.PRODUCT_LINE = '" + lsProdLine + "' "; 
		System.out.println( "Time start :: " + ( new java.util.Date() ).toString() )	;
		System.out.println("SQL 1 :: " + sql );		 
		System.out.println( "ldt_tran_date_from :: " + ldtTranDateFrom );
		System.out.println( "ldt_tran_date_to :: " + ldtTranDateTo );
		
		pstmt = connectionObject.prepareStatement( sql );
		
		pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateTo,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateTo,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(5,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(6,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateTo,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(7,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		pstmt.setTimestamp(8,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldtTranDateTo,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));

		rs = pstmt.executeQuery();		

		while( rs.next() )
		{
			processRec = new ProcessRec();
			processRec.siteCode = rs.getString( "SITE_CODE" );
			processRec.refSer = rs.getString( "REF_SER" );;
			processRec.custCode = rs.getString( "CUST_CODE" );;
			processRec.areaCode = rs.getString( "AREA_CODE" );;
			processRec.itemSer = rs.getString( "ITEM_SER" );;
			processRec.itemCode = rs.getString( "ITEM_CODE" );;
			processRec.descr = rs.getString( "DESCR" );;
			processRec.quantityStduom = rs.getDouble( "qty__stduom" );
			processRec.tranDate = rs.getDate( "TRAN_DATE" );
			processRec.netWeight = rs.getDouble( "item_net_wt" );
			processRec.totWeight = rs.getDouble( "TOT_WEIGHT" );
			processRec.commAmt = rs.getDouble( "COMM_AMT" );
			processRec.basicValue = rs.getDouble( "BASIC_VALUE" );
			processRec.discount = rs.getDouble( "DISCOUNT" );
			processRec.otherDisc = rs.getDouble( "OTHER_DISC" );
			processRec.cashDisc = rs.getDouble( "CASH_DISC" );
			processRec.assValue = rs.getDouble( "ASS_VALUE" );
			processRec.taxableValue = rs.getDouble( "TAXABLE_VALUE" );
			processRec.invValue = rs.getDouble( "INV_VALUE" );
			processRec.stateCode = rs.getString( "STATE_CODE" );
			processRec.stateDescr = rs.getString( "STATE_DESCR" );
			processRec.unit = rs.getString( "UNIT" );
			processRec.tranId = rs.getString( "TRAN_ID" );
			processRec.lineNo = rs.getString( "LINE_NO" );
			processRec.productLine = rs.getString( "PRODUCT_LINE" );
			processRec.expReco = rs.getDouble( "EXP_RECO" );
			processRec.frtReco = rs.getDouble( "FRT_RECO" );
			processRec.todPerc = rs.getDouble( "TOD_PERC" );
			processRec.frtPerc = rs.getDouble( "FRT_PERC" );
			processRec.compVal = rs.getDouble( "COMP_VAL" );
			processRec.terrCode = rs.getString( "TERR_CODE" );
			processRec.wt = rs.getDouble( "WT" );
			
			processRecList.add( processRec );
			processRec = null;
		}
		System.out.println( "End Time :: " + ( new java.util.Date() ).toString() )	;
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		
		return processRecList;
	}

	private HashMap getGenCodesMap( Connection connectionObject ) throws Exception
	{
		HashMap genCdMap = new HashMap();
		
		Statement stmtGencode = null;
		ResultSet rsGencode = null;
		
		String sqlStr = "SELECT FLD_VALUE,UDF_STR2 FROM GENCODES WHERE FLD_NAME ='HIER_REF'"; 
	
		stmtGencode = connectionObject.createStatement();
		
		rsGencode = stmtGencode.executeQuery( sqlStr );
		
		while( rsGencode.next() )
		{
			genCdMap.put( rsGencode.getString("FLD_VALUE"), rsGencode.getString("UDF_STR2") );
		}
		rsGencode.close();
		rsGencode = null;
		stmtGencode.close();
		stmtGencode=null;  
		
		return genCdMap;
	}
	private ArrayList getRateMasterRec( String lsWsheetCode, String ldtTranDateFrom, String ldtTranDateTo, String DB_NAME, String recordType, Connection connectionObject ) throws Exception
	{
		ArrayList rateMasList = new ArrayList();
		RateMasterRec rateMasRec = null;
		PreparedStatement stmtMaster = null;
		ResultSet rsMaster = null;
		String rateKey = null;
		String sql = null;
		if( DB_NAME.equalsIgnoreCase("oracle"))
		{
			sql = "SELECT B.ACCT_CODE,E.DESCR,D.EFF_FROM,D.VALID_UPTO,C.HIER_REF,D.HIER_VALUE,D.DATA_TYPE,D.DATA_VALUE,D.APPLY_ON,C.HIER_REF_1,D.HIER_VALUE_1,C.HIER_REF_2,D.HIER_VALUE_2,C.HIER_REF_3,D.HIER_VALUE_3 FROM PROF_WSHEET A, PROF_WSHEET_COMP B,PROF_WSHEET_MSTSEQ C,PROF_WSHEET_MSTDET D,PROF_ACCOUNTS E WHERE A.WSHEET_CODE = B.WSHEET_CODE AND B.WSHEET_CODE = C.WSHEET_CODE(+) AND B.ACCT_CODE = C.ACCT_CODE(+) AND C.HIER_KEY = D.HIER_KEY(+) AND B.ACTIVE_YN = 'Y' AND A.WSHEET_CODE = ? AND D.HIER_VALUE IS NOT NULL  AND D.EFF_FROM <= ? AND D.VALID_UPTO >= ? AND B.ACCT_CODE = E.ACCT_CODE ORDER BY B.SEQ_NO,C.LINE_NO,D.LINE_NO";
			if( recordType.equalsIgnoreCase( "DEFAULT" ) )
			{
				sql = "SELECT B.ACCT_CODE,E.DESCR,D.EFF_FROM,D.VALID_UPTO,C.HIER_REF,D.HIER_VALUE,D.DATA_TYPE,D.DATA_VALUE,D.APPLY_ON,C.HIER_REF_1,D.HIER_VALUE_1,C.HIER_REF_2,D.HIER_VALUE_2,C.HIER_REF_3,D.HIER_VALUE_3 FROM PROF_WSHEET A, PROF_WSHEET_COMP B,PROF_WSHEET_MSTSEQ C,PROF_WSHEET_MSTDET D,PROF_ACCOUNTS E WHERE A.WSHEET_CODE = B.WSHEET_CODE AND B.WSHEET_CODE = C.WSHEET_CODE(+) AND B.ACCT_CODE = C.ACCT_CODE(+) AND C.HIER_KEY = D.HIER_KEY(+) AND B.ACTIVE_YN = 'Y' AND A.WSHEET_CODE= ? AND D.HIER_VALUE IS NULL  and D.EFF_FROM <= ? AND D.VALID_UPTO >= ? AND B.ACCT_CODE = E.ACCT_CODE ORDER BY B.SEQ_NO,C.LINE_NO,D.LINE_NO";
			}
		}
		else
		{
			sql =   "SELECT B.ACCT_CODE, D.DATA_VALUE, C.HIER_REF, D.HIER_VALUE, C.HIER_REF_1, "
					+"		 D.HIER_VALUE_1, C.HIER_REF_2, D.HIER_VALUE_2, C.HIER_REF_3, D.HIER_VALUE_3, "
					+"		 E.DESCR, D.EFF_FROM, D.VALID_UPTO, "
					+"		 D.DATA_TYPE, D.APPLY_ON "
					+" FROM PROF_WSHEET A, "
					+"	  PROF_WSHEET_COMP B LEFT OUTER JOIN PROF_WSHEET_MSTSEQ C ON B.WSHEET_CODE = C.WSHEET_CODE "
					+"																				AND B.ACCT_CODE = C.ACCT_CODE "
					+"								LEFT OUTER JOIN PROF_WSHEET_MSTDET D ON C.HIER_KEY = D.HIER_KEY, "
					+"	  PROF_ACCOUNTS E "												  
					+" WHERE A.WSHEET_CODE = B.WSHEET_CODE "
					+"	AND B.ACTIVE_YN = 'Y' "
					+"	AND A.WSHEET_CODE = '" + lsWsheetCode + "' AND " 
					+"	(D.HIER_VALUE IS NOT NULL OR D.HIER_VALUE_1 IS NOT NULL OR D.HIER_VALUE_2 IS NOT NULL OR D.HIER_VALUE_3 IS NOT NULL) " 
					+"	AND D.EFF_FROM <= ? "
					+"	AND D.VALID_UPTO >= ? "
					+"	AND B.ACCT_CODE = E.ACCT_CODE ORDER BY B.SEQ_NO,C.LINE_NO,D.LINE_NO";
					
			if( recordType.equalsIgnoreCase( "DEFAULT" ) )
			{
				sql = "SELECT B.ACCT_CODE, D.DATA_VALUE, C.HIER_REF, D.HIER_VALUE, C.HIER_REF_1, D.HIER_VALUE_1, "
					+"		 C.HIER_REF_2, D.HIER_VALUE_2, C.HIER_REF_3, D.HIER_VALUE_3, D.EFF_FROM, D.VALID_UPTO, "
					+"		 E.DESCR, D.DATA_TYPE,  D.APPLY_ON "
					+" FROM PROF_WSHEET A, "
					+"	  PROF_WSHEET_COMP B LEFT OUTER JOIN PROF_WSHEET_MSTSEQ C ON B.WSHEET_CODE = C.WSHEET_CODE "
					+"																				AND B.ACCT_CODE = C.ACCT_CODE "
					+"								LEFT OUTER JOIN PROF_WSHEET_MSTDET D ON C.HIER_KEY = D.HIER_KEY, "
					+"	  PROF_ACCOUNTS E "
					+" WHERE A.WSHEET_CODE = B.WSHEET_CODE AND B.ACTIVE_YN = 'Y' AND A.WSHEET_CODE= '" + lsWsheetCode + "' AND "
					+" (D.HIER_VALUE IS NULL AND D.HIER_VALUE_1 IS NULL AND  D.HIER_VALUE_2 IS NULL AND D.HIER_VALUE_3 IS NULL ) "
					+" and D.EFF_FROM <= ? "  
					+" AND D.VALID_UPTO >= ? "
					+" AND B.ACCT_CODE = E.ACCT_CODE ORDER BY B.SEQ_NO,C.LINE_NO,D.LINE_NO";
			}
					
		}
		if( recordType.equalsIgnoreCase( "DEFAULT" ) )
		{
			rateDefaultMap.clear();			
		}
		else
		{
			rateMasterMap.clear();			
		}
		
		System.out.println( "sql :: " + sql ); 
		stmtMaster = connectionObject.prepareStatement( sql );
		stmtMaster.setTimestamp( 1, java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString( ldtTranDateFrom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		stmtMaster.setTimestamp( 2, java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString( ldtTranDateTo, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
		
		rsMaster = stmtMaster.executeQuery();
		
		while( rsMaster.next() )
		{
			rateMasRec = new RateMasterRec();
			
			/*
			rateMasRec.rateMap.put( "ACCT_CODE", rsMaster.getString("ACCT_CODE" ) );
			rateMasRec.rateMap.put( "DESCR", rsMaster.getString( "DESCR" ) );
			rateMasRec.rateMap.put( "EFF_FROM", rsMaster.getDate( "EFF_FROM" ) );
			rateMasRec.rateMap.put( "VALID_UPTO", rsMaster.getDate("VALID_UPTO") );
			rateMasRec.rateMap.put( "HIER_REF", rsMaster.getString("HIER_REF") );
			rateMasRec.rateMap.put( "HIER_VALUE", rsMaster.getString( "HIER_VALUE" ) );
			rateMasRec.rateMap.put( "DATA_TYPE", rsMaster.getString( "DATA_TYPE" ) );
			rateMasRec.rateMap.put( "DATA_VALUE", rsMaster.getString( "DATA_VALUE" ) );
			rateMasRec.rateMap.put( "APPLY_ON", rsMaster.getString("APPLY_ON" ) );
			rateMasRec.rateMap.put( "HIER_REF_1", rsMaster.getString( "HIER_REF_1" ) );
			rateMasRec.rateMap.put( "HIER_VALUE_1", rsMaster.getString( "HIER_VALUE_1" ) );
			rateMasRec.rateMap.put( "HIER_REF_2", rsMaster.getString( "HIER_REF_2" ) );
			rateMasRec.rateMap.put( "HIER_VALUE_2", rsMaster.getString( "HIER_VALUE_2" ) );
			rateMasRec.rateMap.put( "HIER_REF_3", rsMaster.getString( "HIER_REF_3" ) );
			rateMasRec.rateMap.put( "HIER_VALUE_3", rsMaster.getString( "HIER_VALUE_3" ) );
			*/
			rateMasRec.acctCode = rsMaster.getString("ACCT_CODE" );
			rateMasRec.descr = rsMaster.getString( "DESCR" );
			rateMasRec.effFrom = rsMaster.getDate( "EFF_FROM" );
			rateMasRec.validUpto = rsMaster.getDate("VALID_UPTO");
			rateMasRec.hierRef = rsMaster.getString("HIER_REF");
			rateMasRec.hierValue = rsMaster.getString( "HIER_VALUE" );
			rateMasRec.dataType = rsMaster.getString( "DATA_TYPE" );
			rateMasRec.dataValue = rsMaster.getDouble( "DATA_VALUE" );
			rateMasRec.applyOn = rsMaster.getString("APPLY_ON" );
			rateMasRec.hierRef1 = rsMaster.getString( "HIER_REF_1" );
			rateMasRec.hierValue1 = rsMaster.getString( "HIER_VALUE_1" );
			rateMasRec.hierRef2 = rsMaster.getString( "HIER_REF_2" );
			rateMasRec.hierValue2 = rsMaster.getString( "HIER_VALUE_2" );
			rateMasRec.hierRef3 = rsMaster.getString( "HIER_REF_3" );
			rateMasRec.hierValue3 = rsMaster.getString( "HIER_VALUE_3" );
			
			if( recordType.equalsIgnoreCase( "DEFAULT" ) )
			{
				rateKey = rateMasRec.acctCode;			
				rateDefaultMap.put( rateKey, rateMasRec ); 
			}
			else
			{
				rateKey = rateMasRec.acctCode + "-" + ( rateMasRec.hierValue == null || rateMasRec.hierValue.trim().length() == 0 ? "" : rateMasRec.hierValue.trim() )
											  + "-" + ( rateMasRec.hierValue1 == null || rateMasRec.hierValue1.trim().length() == 0  ? "" : rateMasRec.hierValue1.trim() )
											  + "-" + ( rateMasRec.hierValue2 == null || rateMasRec.hierValue2.trim().length() == 0  ? "" : rateMasRec.hierValue2.trim() )
											  + "-" + ( rateMasRec.hierValue3 == null || rateMasRec.hierValue3.trim().length() == 0  ? "" : rateMasRec.hierValue3.trim() );			
				rateMasterMap.put( rateKey, rateMasRec ); 		
			}
			rateMasList.add( rateMasRec );
		}
		return rateMasList;
	}

	private ArrayList getAcctList( String lsWsheetCode, Connection connectionObject ) throws Exception
	{
		String acctSql = null;
		ArrayList acctList = new ArrayList();
		ResultSet rsAcct = null;
		Statement stmtAcct = null;
		acctSql="SELECT DISTINCT ACCT_CODE FROM PROF_WSHEET_COMP WHERE WSHEET_CODE = '" + lsWsheetCode + "' ORDER BY ACCT_CODE";
		stmtAcct = connectionObject.createStatement();
		rsAcct = stmtAcct.executeQuery( acctSql );
		
		while( rsAcct.next() )
		{
			acctList.add( rsAcct.getString( "ACCT_CODE" ) );
		}
		rsAcct.close();
		rsAcct = null;
		stmtAcct.close();
		stmtAcct = null;
		
		return acctList;
	}
	private class ProcessRec 
	{
		String siteCode = null;
		String refSer = null;
		String custCode = null;
		String areaCode = null;
		String itemSer = null;
		String itemCode = null;
		String descr = null;
		double quantityStduom;
		java.sql.Date tranDate;
		double netWeight;
		double totWeight;
		double commAmt;
		double basicValue;
		double discount;
		double otherDisc;
		double cashDisc;
		double assValue;
		double taxableValue;
		double invValue;
		String stateCode = null;
		String stateDescr = null;
		String unit = null;
		String tranId = null;
		String lineNo = null;
		String productLine = null;
		double expReco;
		double frtReco;
		double todPerc;
		double frtPerc;
		double compVal;
		String terrCode = null;
		double wt;
		
		public String getColumnVal( String colName )
		{
			if( colName.equalsIgnoreCase( "SITE_CODE" ) )
			{
				return siteCode;
			}else if( colName.equalsIgnoreCase( "REF_SER" ) )
			{
				return refSer;			
			}else if( colName.equalsIgnoreCase( "REF_SER" ) )
			{
				return refSer;
			}else if( colName.equalsIgnoreCase( "CUST_CODE" ) )
			{
				return custCode;
			}else if( colName.equalsIgnoreCase( "AREA_CODE" ) )
			{
				return areaCode;
			}else if( colName.equalsIgnoreCase( "ITEM_SER" ) )
			{
				return itemSer;
			}else if( colName.equalsIgnoreCase( "ITEM_CODE" ) )
			{
				return itemCode;
			}else if( colName.equalsIgnoreCase( "DESCR" ) )
			{
				return descr;
			}else if( colName.equalsIgnoreCase( "QUANTITY__STDUOM" ) )
			{
				return Double.toString( quantityStduom );
			}else if( colName.equalsIgnoreCase( "TRAN_DATE" ) )
			{
				return tranDate.toString();
			}else if( colName.equalsIgnoreCase( "NET_WEIGHT" ) )
			{
				return Double.toString( netWeight );
			}else if( colName.equalsIgnoreCase( "TOT_WEIGHT" ) )
			{
				return Double.toString( totWeight );
			}else if( colName.equalsIgnoreCase( "COMM_AMT" ) )
			{
				return Double.toString( commAmt );
			}else if( colName.equalsIgnoreCase( "DISCOUNT" ) )
			{
				return Double.toString( discount );
			}else if( colName.equalsIgnoreCase( "OTHER_DISC" ) )
			{
				return Double.toString( otherDisc );
			}else if( colName.equalsIgnoreCase( "CASH_DISC" ) )
			{
				return Double.toString( cashDisc );
			}else if( colName.equalsIgnoreCase( "ASS_VALUE" ) )
			{
				return Double.toString( assValue );
			}else if( colName.equalsIgnoreCase( "TAXABLE_VALUE" ) )
			{
				return Double.toString( taxableValue );
			}else if( colName.equalsIgnoreCase( "INV_VALUE" ) )
			{
				return Double.toString( invValue );
			}else if( colName.equalsIgnoreCase( "STATE_CODE" ) )
			{
				return stateCode;
			}else if( colName.equalsIgnoreCase( "STATE_DESCR" ) )
			{
				return stateDescr;
			}else if( colName.equalsIgnoreCase( "UNIT" ) )
			{
				return unit;
			}else if( colName.equalsIgnoreCase( "TRAN_ID" ) )
			{
				return tranId;
			}else if( colName.equalsIgnoreCase( "LINE_NO" ) )
			{
				return lineNo;
			}else if( colName.equalsIgnoreCase( "PRODUCT_LINE" ) )
			{
				return productLine;
			}else if( colName.equalsIgnoreCase( "EXP_RECO" ) )
			{
				return Double.toString( expReco );
			}else if( colName.equalsIgnoreCase( "FRT_RECO" ) )
			{
				return Double.toString( frtReco );
			}else if( colName.equalsIgnoreCase( "TOD_PERC" ) )
			{
				return Double.toString( todPerc );
			}else if( colName.equalsIgnoreCase( "FRT_PERC" ) )
			{
				return Double.toString( frtPerc );
			}else if( colName.equalsIgnoreCase( "COMP_VAL" ) )
			{
				return Double.toString( compVal );
			}else if( colName.equalsIgnoreCase( "TERR_CODE" ) )
			{
				return terrCode;
			}else if( colName.equalsIgnoreCase( "WT" ) )
			{
				return Double.toString( wt );
			}
			else 
			{
				return null;
			}
		}
	}
	private class RateMasterRec
	{
		/*
		HashMap rateMap = null;
		public ()
		{
			rateMap = HashMap(); 
		}
		*/
		String acctCode = null;
		String descr = null;
		java.sql.Date effFrom = null;
		java.sql.Date validUpto = null;
		String hierRef = null;
		String hierValue = null;
		String dataType = null;
		double dataValue;
		String applyOn = null;
		String hierRef1 = null;
		String hierValue1 = null;
		String hierRef2 = null;
		String hierValue2 = null;
		String hierRef3 = null;
		String hierValue3 = null;
	}	
	private void printMap( HashMap hmap, String mapType )
	{
		java.util.Iterator iteratorMap = null;
		java.util.Set setMap = null;
		java.util.Map.Entry entry = null;
		setMap = hmap.entrySet();
		iteratorMap = setMap.iterator();
		RateMasterRec rec = null;
		if( iteratorMap == null )
		{
			System.out.println("Null iterator......");
		}

		while( iteratorMap.hasNext() )
		{
			entry = ( java.util.Map.Entry )iteratorMap.next();
			String tKey = null;
			String supplierCode = null;
			tKey = (String)entry.getKey();

			if ( mapType.equalsIgnoreCase( "GEN" ) )
			{
				System.out.println(" mapType : ( tKey, value ) :: " + mapType + " : ( " + tKey + ", " + ( String )entry.getValue() + " )" );
			}
			else
			{
				rec = ( RateMasterRec )entry.getValue();
				System.out.println(" mapType : ( tKey, acctCode, dataValue ) :: ( " + tKey + ", " + rec.acctCode + ", " + rec.dataValue + " )" );
			}
	
			
		}	
	}
}
