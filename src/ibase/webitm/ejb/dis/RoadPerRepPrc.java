


package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.ejb.Stateless;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class RoadPerRepPrc extends ProcessEJB implements RoadPerRepPrcLocal ,RoadPerRepPrcRemote
{
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ Inside RoadPerRepPrc EJB -> Process Method 1 ^^^^^^^");
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String retString = "";

		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				headerDom = genericUtility.parseString(xmlString);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				detailDom = genericUtility.parseString(xmlString2);
			}
			retString = process(headerDom, detailDom, windowName, xtraParams);
			
		} catch (Exception e) {
			System.out.println("Exception RoadPerRepPrc Method 1");
			e.printStackTrace();
			retString = itmDBAccessEJB.getErrorString("", "VTCBFAIL", "", 1);
			throw new ITMException(e);
		}
		return retString;
	}

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("^^^^^^^ Inside RoadPerRepPrc Process -> Process Method 2 ^^^^^^^");
		String retString = "";

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		String sql = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		boolean isError = false;
		int counter=0;
		int cntExist=0;
		
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String oldPermNo="", newPermNo="",status=""; 
		int count = 0,count1=0,count2=0;
		ArrayList distIssueList = new ArrayList(); 
		ArrayList despatchList = new ArrayList();
		
		try {

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			parentNodeList = detailDom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			for (int i = 0; i < childNodeList.getLength(); i++) 
			{
				childNode = childNodeList.item(i);
			
				if ("old_per__no".equalsIgnoreCase(childNode.getNodeName())) 
				{
					oldPermNo = childNode.getFirstChild() == null ? "" : childNode.getFirstChild().getNodeValue();
				}
				else if ("new_per__no".equalsIgnoreCase(childNode.getNodeName())) 
				{
					newPermNo = childNode.getFirstChild() == null ? "" : childNode.getFirstChild().getNodeValue();
				}

			}

			if( oldPermNo == null || oldPermNo.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("", "VMOLDPNONU", "", "", conn);
				return retString;
			}
			else
			{
				cntExist = 0;
				sql = "select count(1) from roadpermit where rd_permit_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,oldPermNo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cntExist = rs.getInt(1) ;
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				
				if( cntExist == 0 )
				{
					retString = itmDBAccessEJB.getErrorString("", "VMRDPNINV1", "", "", conn);
					return retString;
				}
			}

			if( newPermNo == null || newPermNo.trim().length() == 0 )
			{
				retString = itmDBAccessEJB.getErrorString("", "VMNEWPNONU", "", "", conn);
				return retString;
			}
			else
			{
				cntExist = 0;
				sql = "select count(1) from roadpermit where rd_permit_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,newPermNo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cntExist = rs.getInt(1) ;
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
			
				if( cntExist == 0 )
				{
					retString = itmDBAccessEJB.getErrorString("", "VMRDPNINV2", "", "", conn);
					return retString;
				}
			}
			
			
			sql = "select status from roadpermit where rd_permit_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,oldPermNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				status = rs.getString("status") ;
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	

			if("C".equalsIgnoreCase(status))
			{
				retString = itmDBAccessEJB.getErrorString("", "VMRSTCLOS1", "", "", conn);
				return retString;
			}
			
			sql = "select status from roadpermit where rd_permit_no = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,newPermNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				status = rs.getString("status") ;
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	

			if("C".equalsIgnoreCase(status))
			{
				retString = itmDBAccessEJB.getErrorString("", "VMRSTCLOS2", "", "", conn);
				return retString;
			}
			
			// select count ( distinct lr_no )  from distord_iss where rd_permit_no = 'TEST2' ;
			sql = " select count ( distinct lr_no )  from distord_iss where rd_permit_no = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,newPermNo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1) ;
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				
			if( count > 1 )
			{
				retString = itmDBAccessEJB.getErrorString("", "VMNEWPNOI2", "", "", conn);
				return retString;
			}
		/*	
			sql = " select count ( distinct rd_permit_no )  from distord_iss where lr_no = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,newPermNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1) ;
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	
			
		if( count > 1 )
		{
			retString = itmDBAccessEJB.getErrorString("", "VMNEWPNOI3", "", "", conn);
			return retString;
		}
		*/	
		sql = " select count(1) from distord_iss where rd_permit_no = ? " +
				  " and lr_no not in ( select lr_no from distord_iss where rd_permit_no = ? ) ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,newPermNo);
			pstmt.setString(2,oldPermNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1) ;
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	
			
			if( count > 0 )
			{
				retString = itmDBAccessEJB.getErrorString("", "VMNEWPNOI1", "", "", conn);
				return retString;
			}
				
			
			System.out.println("@@@@newPermNo["+newPermNo+"]::status["+status+"]::oldPermNo["+oldPermNo+"]@@@@@@@");

			if( retString == null ||  retString.trim().length() == 0 )
			{
				retString = distIssueValidate(oldPermNo,newPermNo,conn);
			}
			if( retString == null ||  retString.trim().length() == 0 )
			{
				retString = despatchValidate(oldPermNo,newPermNo,conn);
			}
			
			if( retString == null ||  retString.trim().length() == 0 )
			{
				sql = " select tran_id from distord_iss where rd_permit_no = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,oldPermNo);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					distIssueList.add(rs.getString(1)) ;
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	
				
				
				sql = " select desp_id from despatch where rd_permit_no = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,oldPermNo);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					despatchList.add(rs.getString(1)) ;
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				sql = " update distord_iss set rd_permit_no=  ?  where rd_permit_no = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,newPermNo);
				pstmt.setString(2,oldPermNo);
				count1 = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				
				sql = " update despatch set rd_permit_no=  ?  where rd_permit_no = ?  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,newPermNo);
				pstmt.setString(2,oldPermNo);
				count2 = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				
				System.out.println("@@@@@@@@@@distIssueList["+distIssueList.size()+"]["+distIssueList+"]");
				System.out.println("@@@@@@@@@@despatchList["+despatchList.size()+"]["+despatchList+"]");
				
				if( count1 > 0  || count2 > 0 )
				{
					//wronu upd info
					sql = " update roadpermit set status = ? , remarks = ?   where rd_permit_no = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,"I");
					pstmt.setString(2,"wrong entry");
					pstmt.setString(3,oldPermNo);
					count2 = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					
					String refSer="",refId="";
					
					
					for(int ctr=0; ctr < distIssueList.size() ; ctr++ )
					{
						refId = distIssueList.get(ctr).toString();
						refSer = "D-ISS";
						int recInt = insertIntoTrace( refId, refSer,newPermNo, oldPermNo , xtraParams, conn );
						if( recInt == 0 )
						{
							System.out.println("@@@@@@@@@ not inserted ["+refId+"]");
						}
					}
					
					for(int ctr=0; ctr < despatchList.size() ; ctr++ )
					{
						refId = despatchList.get(ctr).toString();
						refSer = "S-DSP";
						int recInt = insertIntoTrace( refId, refSer,newPermNo, oldPermNo , xtraParams, conn );
						if( recInt == 0 )
						{
							System.out.println("@@@@@@@@@ not inserted ["+refId+"]");
						}
					}
					
				}
				
				
				
			}
			

		} 


		catch (Exception e)
		{
			isError = true;
			System.out.println("@@@@@ ret str in catch ["+retString+"]@@@@@@@@");
			retString = itmDBAccessEJB.getErrorString("", "VMMPFAIL9", "", "", conn);
			throw new ITMException(e);
		} 

		finally {
			try {
				System.out.println("@@@@@ ret str in finally ["+retString+"]@@@@@@@@");
				if (conn != null)
				{
					if ( retString != null &&  retString.trim().length() > 0 ) 
					{
						conn.rollback();
					//	retString = itmDBAccessEJB.getErrorString("", "VMMPFAIL9", "", "", conn);  // ibase3-webitm-dis5-6-0-57
					}
					else
					{
							conn.commit();
							retString = itmDBAccessEJB.getErrorString("", "VMMPSUCCES", "", "", conn);
					}
					
					conn.close();
					conn = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}

			} catch (Exception e) {
				throw new ITMException(e);
			}
		}
		System.out.println("@@@@@@@@@@@@ final return ::::retString["+retString+"]");
		return retString;
	}
	
	private int insertIntoTrace(String refId, String refSer, String newPermNo,String oldPermNo, String xtraParams, Connection conn)  throws ITMException, Exception 
	{

		String loginSiteCode="",tranId="",chgUser="",chgTerm="";
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		String sql = "";
		int count=0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		
		java.util.Date currentDate = new java.util.Date();
		SimpleDateFormat sdf;
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			Timestamp newsysDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
			System.out.println("Now the date is :=>  ["+newsysDate+"]");
		
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			tranId = generateTranId( "T_RPTRACE", loginSiteCode, conn );
			//chgUser = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgUser");
			
			System.out.println("@@@@@@@@@@@ chgTerm-["+chgTerm+"]::chgUser-["+chgUser+"]:loginSiteCode["+loginSiteCode+"]::");
			
			
			sql = " insert into roadpermit_trace (tran_id,ref_ser,ref_id,RD_PERMIT_NO,RD_PERMIT_NO__old,CHG_DATE,CHG_USER,CHG_TERM )" +
					  " values (?,?,?,?,?,?,?,?) ";
				
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tranId);
						pstmt.setString(2,refSer);
						pstmt.setString(3,refId);
						pstmt.setString(4,newPermNo);
						pstmt.setString(5,oldPermNo);
						pstmt.setTimestamp(6,newsysDate);
						pstmt.setString(7,chgUser);
						pstmt.setString(8,chgTerm);
						count = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
			
		}
		catch (ITMException e) 
		{
			throw new ITMException(e);
		}
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		System.out.println("@@@@@@ count["+count+"]:::tranId["+tranId+"]");
			
	 return count;		
		
	}

	private String generateTranId( String windowName, String siteCode, Connection conn )throws ITMException
    {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		 try
         {

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE  UPPER( TRAN_WINDOW ) = ? ";
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
					keyString = rs.getString("KEY_STRING");
					keyCol = rs.getString("TRAN_ID_COL");
					tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
         }
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +selSql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
						rs.close();
						rs = null;
				}
				if (pstmt != null)
				{
						pstmt.close();
						pstmt = null;
				}
			}
			catch(Exception e){}
		}
        return tranId;
     }//generateTranTd()
	

	private String despatchValidate(String oldPermNo, String newPermNo,	Connection conn) throws SQLException, RemoteException, ITMException 
	{
		System.out.println("@@@@@@@@ calling-------despatchValidate --------");
		String retString="";
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		String sql = "",	stateCodeDespatch="",siteCodeShipDespatch="",siteCodeToRDPermit="",stateCodeToRDPermit="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Timestamp despDate= null,expiryDate=null;		
		int despatchCntr = 0 ;
		//sql = " select  sorder.site_code__ship  ,customer.state_code from sorder , customer where sale_order in ( select sord_no from despatch" +
		//  " where desp_id in ( select desp_id from despatch where rd_permit_no = ? ))  and  sorder.cust_code__dlv = customer.cust_code " ;
		sql = " select  sorder.site_code__ship  ,customer.state_code from sorder , customer " +
			  " where sale_order in ( select sord_no from despatch where rd_permit_no = ? ) " +
			  " and  sorder.cust_code__dlv = customer.cust_code ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,oldPermNo);   // ch
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			despatchCntr++;
			siteCodeShipDespatch = rs.getString("site_code__ship") ;
			stateCodeDespatch = rs.getString("state_code") ;
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;	
		
		sql = " select site_code__fr,state_code__to,expiry_date from  roadpermit where rd_permit_no = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,newPermNo);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			siteCodeToRDPermit = rs.getString("site_code__fr") ;
			stateCodeToRDPermit = rs.getString("state_code__to") ;
			expiryDate = rs.getTimestamp("expiry_date") ;
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;	
		System.out.println("@@@@@@@siteCodeShipDespatch["+siteCodeShipDespatch+"]:::siteCodeToRDPermit["+siteCodeToRDPermit+"]");
		if( despatchCntr > 0 && (! siteCodeShipDespatch.equalsIgnoreCase(siteCodeToRDPermit) ))
		{
			retString = itmDBAccessEJB.getErrorString("", "VMSITDCDMI", "", "", conn);
			return retString;
		}
		System.out.println("@@@@@@@stateCodeDespatch["+stateCodeDespatch+"]:::stateCodeToRDPermit["+stateCodeToRDPermit+"]");
		if( despatchCntr > 0 && (! stateCodeDespatch.equalsIgnoreCase(stateCodeToRDPermit) ))
		{
			retString = itmDBAccessEJB.getErrorString("", "VMSTADCDMI", "", "", conn);
			return retString;
		}
		
		sql = "select desp_date from despatch where rd_permit_no = ?  ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,oldPermNo);   //ch
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			despDate = rs.getTimestamp("desp_date") ;
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		System.out.println("@@@@@@@despDate["+despDate+"]:::expiryDate["+expiryDate+"]");
		if( despatchCntr > 0 &&  despDate == null )
		{
			retString = itmDBAccessEJB.getErrorString("", "VMDATEDMI7", "", "", conn);
			return retString;
		}
		if(  expiryDate == null)
		{
			retString = itmDBAccessEJB.getErrorString("", "VMDATEDM13", "", "", conn);
			return retString;
		}
		if( despatchCntr > 0 && despDate.after(expiryDate))
		{
			retString = itmDBAccessEJB.getErrorString("", "VMDATEDMIS", "", "", conn);
			return retString;
		}
		
		System.out.println("@@@@@@@@ despatchValidate retString["+retString+"]");
		return retString;
	}

	private String distIssueValidate(String oldPermNo, String newPermNo,Connection conn) throws SQLException, RemoteException, ITMException 
	{
		System.out.println("@@@@@@@@ calling-------distIssueValidate --------");
		String retString="";
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		String sql = "",	siteCodeIssue="",stateCodeIssue="",siteCodeFromRDPermit="",stateCodeToRDPermit="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Timestamp tranDate= null,expiryDate=null;		
		int distIssCntr = 0;
		
		sql = "select site_code, state_code from site  where site_code in ( select site_code__dlv  from distord_iss where rd_permit_no = ? ) ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,oldPermNo);   //ch
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			distIssCntr++;
			siteCodeIssue = rs.getString("site_code") ;
			stateCodeIssue = rs.getString("state_code") ;
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;	
		
		sql = " select site_code__fr,state_code__to,expiry_date from  roadpermit where rd_permit_no = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,newPermNo);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			siteCodeFromRDPermit = rs.getString("site_code__fr") ;
			stateCodeToRDPermit = rs.getString("state_code__to") ;
			expiryDate = rs.getTimestamp("expiry_date") ;
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;	
		
		System.out.println("@@@@@@@siteCodeIssue["+siteCodeIssue+"]:::siteCodeFromRDPermit["+siteCodeFromRDPermit+"]");
		if( distIssCntr > 0   && ( ! siteCodeIssue.equalsIgnoreCase(siteCodeFromRDPermit) ))
		{
			retString = itmDBAccessEJB.getErrorString("", "VMSITECDDM", "", "", conn);
			return retString;
		}
		System.out.println("@@@@@@@stateCodeIssue["+stateCodeIssue+"]:::stateCodeToRDPermit["+stateCodeToRDPermit+"]");
		if( distIssCntr > 0 && ( ! stateCodeIssue.equalsIgnoreCase(stateCodeToRDPermit) ))
		{
			retString = itmDBAccessEJB.getErrorString("", "VMSTATCDDM", "", "", conn);
			return retString;
		}
		
		sql = "select tran_date from distord_iss where rd_permit_no = ?  ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,oldPermNo);    //ch
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			tranDate = rs.getTimestamp("tran_date") ;
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		
		System.out.println("@@@@@@@tranDate["+tranDate+"]:::expiryDate["+expiryDate+"]");
		if( distIssCntr > 0 && tranDate == null )
		{
			retString = itmDBAccessEJB.getErrorString("", "VMDATEDM12", "", "", conn);
			return retString;
		}
		
		if( expiryDate == null)
		{
			retString = itmDBAccessEJB.getErrorString("", "VMDATEDM13", "", "", conn);
			return retString;
		}
		
		if( distIssCntr > 0 &&  tranDate.after(expiryDate))
		{
			retString = itmDBAccessEJB.getErrorString("", "VMDATEDMI", "", "", conn);
			return retString;
		}
		
		System.out.println("@@@@@@@@ distIssueValidate retString["+retString+"]");
		return retString;
	}

	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}

	 
	private String findValue(Connection conn, String columnName ,String tableName, String columnName2, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		String findValue = "";
		try
		{			
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{					
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from findValue " + findValue);
		return findValue;
	}


}





