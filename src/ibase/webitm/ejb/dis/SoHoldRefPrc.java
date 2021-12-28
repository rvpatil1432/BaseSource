
/********************************************************
	Title : RecoveryPatternEJB
	Date  : 19/01/09
	Author: Manazir Hasan

********************************************************/
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
import java.text.DecimalFormat;
import javax.ejb.Stateless; // added for ejb3


//public class SoHoldRefPrcEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class SoHoldRefPrc extends ProcessEJB implements SoHoldRefPrcLocal, SoHoldRefPrcRemote
{
	Connection sqlConn = null;
	Connection conn = null;
	String errorString = null;
	NodeList parentNodeList = null;
	NodeList childNodeList = null;
	Node parentNode = null;
	Node childNode = null;
	int ctr = 0,cnt = 0;
	String childNodeName = null;
	String errCode = "";
	int currentFormNo=0;
	int childNodeListLength;
	String selectQry = "";
	String insertQry = "";	
	String upDateQry = "";	
	String selectDtl1Qry = "";	
	String selectDtl2Qry = "";	
	String insertDtl1Qry = "";	
	String insertDtl2Qry = "";	
	String selectItem = "";	
	ResultSet rs = null;
	ResultSet detail1Rs = null;
	ResultSet detail2Rs = null;
	PreparedStatement psmt = null;
	PreparedStatement dtl1Psmt = null;
	PreparedStatement dtl2Psmt = null;
	Statement stmt = null;
	Statement dtl2Stmt = null;
	ResultSet selectRs = null;
	ResultSet dtlRs = null;
	boolean recFound = false;	
	PreparedStatement oraPsmt = null;
	Statement st = null;
	String exprNo  = null;
	//String confirmed = null;
	String confirmation = "";
	SimpleDateFormat simpleDateFrormat  = null;
	int upd = 0;
	String procCode = "",procDateStr = "",template = "",mcCode = "",procesName = "",mcName = "",stageNo = "",procDate = "";
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	/*
	public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Create Method Called....");
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
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
		throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";
		try
		{				
			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :SoHoldRefPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{		
		String retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "" ,sql1 = "",errString = "";
        String retString = "" ,errCode = "",tranId="";
		String loginSiteCode="",empCode="",chgUser="",chgTerm="",loginCode="";
		String 	policyNo="",insCertNo="",agent_code="",invoiceId="";
		int count =0,cnt =0;
		String curr_code="";
	    double exch_rate=0,netAmt=0;	
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		int no = 0;		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null; 
		ConnDriver connDriver = new ConnDriver();
		String specReason = "";
		String status = "";
		String saleOrder = "";
		String  confirmation = "";
		String confirmed = "";	
		String conf = "";
		Timestamp todayDate = null; 
		int statusNo = 1;

		try
		{
			System.out.println("xtraParams :::::::::::::::::::::::::::::::::: "+xtraParams);
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver=null;	
			todayDate =  new java.sql.Timestamp(System.currentTimeMillis()) ;	
			//-------------detail 1--------------------------
			parentNodeList = detailDom.getElementsByTagName("Detail1");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("parentNodeListLength:::::::::"+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength::: "+ childNodeListLength+"\n");			
				
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{					
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();					
												
					if (childNodeName.equals("sale_order"))
					{
						if(childNode.getFirstChild()!=null)
						{
							saleOrder = childNode.getFirstChild().getNodeValue();					
													
						}					
						
					}
					if(childNodeName.equalsIgnoreCase("confirmation"))
					{
						if(childNode.getFirstChild()!=null)
						{
							confirmation = childNode.getFirstChild().getNodeValue();						
							if(confirmation != null && confirmation.trim().length() > 0 && confirmation.equalsIgnoreCase("N"))
							{								
								errCode = "VMUNCONCOD";
								errString =itmDBAccessEJB.getErrorString("confirmation",errCode,chgUser,"",conn);
								return (errString);
															
							}
						}
					}
					if(childNodeName.equalsIgnoreCase("status"))
					{	
						if(childNode.getFirstChild()!=null)
						{
							status = childNode.getFirstChild().getNodeValue();							
							if(status != null && status.trim().length() > 0 && status.equalsIgnoreCase("C"))
							{								
								errCode = "VMSTANTCAN";
								errString =itmDBAccessEJB.getErrorString("status",errCode,chgUser,"",conn);
								return (errString);								
							}
						}	
					}
					if (childNodeName.equals("spec_reason"))
					{
						
						if(childNode.getFirstChild()!=null)
						{
							specReason = childNode.getFirstChild().getNodeValue();
																			
						}
					}					
					
				}//inner for loop]	lotNoFromStr	
				System.out.println("confirmation [" + confirmation + "]" );
				if(specReason == null ||  specReason.trim().length() ==0 )
				{								
					errCode = "VMRSCODNNL";
					errString =itmDBAccessEJB.getErrorString("spec_reason",errCode,chgUser,"",conn);
					return (errString);							
				}		
				if(confirmation.equalsIgnoreCase("Y") && (status.equalsIgnoreCase("P") || status.equalsIgnoreCase("H")) && (specReason != null || specReason.trim().length()!=0))
				{
					
					sql = "update sorder  set  status_remarks = ? ,status = ? ,status_date = ? where sale_order = '"+saleOrder+"'  and status = '"+status.trim()+"' and confirmed = '"+confirmation+"' " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,specReason);
					if(status.equalsIgnoreCase("H"))
					{
						pstmt.setString(2,"P");
						statusNo = 4;
					}
					if(status.equalsIgnoreCase("P"))
					{
						pstmt.setString(2,"H");
						statusNo = 3;
					}
					pstmt.setTimestamp(3,todayDate);
					int i = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					String errStr = sorderStatusLog(saleOrder,todayDate,statusNo,"",conn);
					if(i >0 )
					{
						if(errStr.length()==0)
						{
							conn.commit();
							errCode = "SUCCESSPRC";
							errString =itmDBAccessEJB.getErrorString("updated",errCode,chgUser,"",conn);
							System.out.println("sucess call");
							break ;	
						}
						
					}						
				}
				else
				{
					errCode = "VMUPDATPRC";
					errString =itmDBAccessEJB.getErrorString("VMUPDATPRC",errCode,chgUser,"",conn);
					break ;	
				
				}
					
			} // outer for loop		
			// end of code 
	
		}  // end try 
	   	catch(Exception e)
		{
			try{
			conn.rollback();
			 System.out.println("Exception in SoHoldRefPrcEJB..."+e.getMessage());
			 e.printStackTrace();
  		  errString = e.getMessage();			
			}catch(Exception ei){}
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
				conn = null;
				pstmt.close();
			}catch(Exception e){}
		}
		System.out.println("returning from  SoHoldRefPrcEJB   "+errString);
	    return (errString);
	} //end process
	private String sorderStatusLog(String saleOrder,Timestamp todayDate,int  statusNo,String dummy1,Connection conn)throws Exception
	{
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs= null;
		String siteCode= "";
		String window = "w_sorder_stat_log" ;
		String key = "";
		String ediOption = "";
		String tranId = "";
		String merrcode ="";
		try
		{

			sql ="select site_code  from sorder where sale_order = '"+saleOrder+"' ";
			pstmt =conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
			 siteCode = rs.getString(1) == null ? "" : rs.getString(1) ;
			}
			System.out.println("siteCode>>>>"+siteCode);			
			pstmt.close();
			pstmt = null;
			rs.close();
			rs =  null;
			sql= "select key_string  from transetup where tran_window ='"+window+"' ";
			pstmt =conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				key = rs.getString(1) == null ? "" : rs.getString(1) ;
			}
			System.out.println("key>>>>>>>"+key);
			pstmt.close();
			pstmt = null;
			rs.close();
			rs =  null;
			tranId = generateTranId("w_sorder_stat_log", saleOrder ,conn) ;

			System.out.println("tranId>>>>>>>"+tranId);

			sql= " insert into sorder_stat_log(tran_id,sorder,event_type,log_date,chg_date,chg_user,chg_term ) values ('"+tranId+"', '"+saleOrder+"',?,?,?,?,? ) " ;
			pstmt =conn.prepareStatement(sql);
			pstmt.setInt(1,statusNo);
			pstmt.setTimestamp(2,todayDate);
			pstmt.setTimestamp(3,todayDate);
			pstmt.setString(4,"BASE");
			pstmt.setString(5,"BASE");
			pstmt.executeUpdate();
			sql = "select edi_option  from transetup  where tran_window = 'w_sorder_stat_log' ";
			pstmt =conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ediOption = rs.getString(1) == null ? "" : rs.getString(1) ;
			}
			System.out.println("ediOption>>>>"+ediOption);
			pstmt.close();
			pstmt = null;
			rs.close();
			rs =  null;
			/*if(ediOption.equalsIgnoreCase("1"))
			{
				//merrcode = nvo_functions_adv.nf_create_edi_multi('w_sorder_stat_log', &
						//							 lds_hdr_edit.describe("datawindow.syntax") &
									//		 + '~r' + lds_hdr_edit.describe("datawindow.syntax.data"), &
					//								 'A', '2', 1, '' + '~r' + '', '', '', '', '', '', ltr__sqlca)//2//added ltr__sqlca by bhagyashree 0n 07-06-04
			//
			}
			if(ediOption.equalsIgnoreCase("2"))
			{
			
			
				//nvo_functions_adv.post nf_create_edi_multi('w_sorder_stat_log', &
						//					 lds_hdr_edit.describe("datawindow.syntax") &
						//				 + '~r' + lds_hdr_edit.describe("datawindow.syntax.data"), &
							//			 'A', '2', 1, '' + '~r' + '', '', '', '', '', '', ltr__sqlca)//2//added ltr__sqlca by bhagyashree 0n 07-06-04
			}*/
		}//  end of try 

		 catch(SQLException se)
        {
            System.out.println("SQLException :Generating id[failed] : " + "\n" +se.getMessage());
            se.printStackTrace();
        }
        catch(Exception ex)
        {
            System.out.println("Exception:Generating id [failed]:" + "\n" +ex.getMessage());
        }
		return merrcode ;
	
	} // END OF START LOG

	public String  generateTranId( String winName, String saleOrder, Connection conn ) throws Exception
    {
        Statement lstmt = null;
        ResultSet lrs = null;
        String keyStringQuery = null;
		String tranId = "";
       
        String tranDate = null;
		CommonConstants commonConstants = null;
        try
        {
			System.out.println("Welcome ur in tranid generator.......!");
			keyStringQuery = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = '"+ winName +"'";
			System.out.println("keyStringQuery.......!" + keyStringQuery);
            lstmt = conn.createStatement();
            lrs = lstmt.executeQuery(keyStringQuery);
            String tranSer = "";
            String keyString = "";
            String keyCol = "";
            if (lrs.next())
            {
                keyString = lrs.getString( 1 );
                keyCol = lrs.getString( 2 );
                tranSer = lrs.getString( 3 );
            }
			lrs.close();
			lrs = null;          
            System.out.println("keyString.......!" + keyString + "\nkeyCol:- " + keyCol + "\ntranSer:- " + tranSer );         
            System.out.println( "tranDate :: " + tranDate );  
            String xmlValues = "";
            xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
            xmlValues = xmlValues + "<Header></Header>";
            xmlValues = xmlValues + "<Detail1>";
            xmlValues = xmlValues + "<sales_order>" + saleOrder + "</sales_order>";           
            xmlValues = xmlValues +"</Detail1></Root>";
            System.out.println( "xmlValues :: " + xmlValues );
            TransIDGenerator tg = new TransIDGenerator(xmlValues, "LCK", commonConstants.DB_NAME);
            tranId = tg.generateTranSeqID( tranSer, keyCol, keyString, conn );
            System.out.println( "tranId :: " + tranId );
        }
        catch(SQLException se)
        {
            System.out.println("SQLException :Generating id[failed] : " + "\n" +se.getMessage());
            se.printStackTrace();
        }
        catch(Exception ex)
        {
            System.out.println("Exception:Generating id [failed]:" + "\n" +ex.getMessage());
        }
        finally
        {
            if (lstmt != null)
            {
				lstmt.close();
                lstmt = null;
            }
			if(lrs != null)lrs.close();
			lrs = null;
        }
        return tranId;
    }//trnId 
	
	
	
	 
}