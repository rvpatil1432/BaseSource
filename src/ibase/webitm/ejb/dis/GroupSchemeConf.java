package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

@Stateless
public class GroupSchemeConf extends ActionHandlerEJB implements GroupSchemeConfLocal, GroupSchemeConfRemote {
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String confirm(String schemeCode, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println(">>>>confirm called");
		String confirmed = "";
		String sql = "",sql1="",sql2="",sql3="";
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
	    String errString = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		System.out.println("forcedFlag["+forcedFlag+"]");
		String loginEmpCode="",siteCode="",sordbulkNo="";
		String loginSiteCode="",custCode="";
		String itemSer="",orderType="",retailerCode="";
		String pricelist="",pricelistClg="",crTerm="",custCodeBil="",salespers="",salespers1="",salespers2="",frtTerm="";
		String groupCode="",salesPersTmp="",crTermTmp="",salesPersTmp1="",salesPersTmp2="",transMode="";
		String deliveryTerm="",tranCode="",add1="",add2="",add3="",tele1="",tele2="",tele3="";
		String stanCode="",city="",countCode="",pin="",stateCode="",dlvDescr="";
		String userId="",termId="";	
		String currCode1="",currCode2="",discount="";
		String linenoStr="",quantityStr="";
		
		String linedetnew=null,quantitydetNew=null;
		StringBuffer xmlString = new StringBuffer();
	
		String sysDate="";
	    int cnt = 0,lineNo=0,lN=1,lN1=1;
		double exchRateFr=0;
		double quantity=0;
		Timestamp currDate = null;
		Timestamp tranDate = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		String custCodeDet="",unit="",itemDescr="";;
		FinCommon finCommon = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		  String retString = "" ,errCode = "";
	
		 String prodCodePur= "" ,prodCodeOff= "" ;
		 int updst = 0;
	
		int currentFormNo =0,recCnt=0,ct=0 , ct1=0,ct2=0,cnt1=0,cnt2=0;
		try 
		{

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdf.format(currDate);
				
			Timestamp toDate= Timestamp.valueOf(genericUtility.getValidDateString(currDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("itemcodecheckDate!!"+toDate);
			
		
			conn = getConnection();
		
			sql = " SELECT  PROD_CODE__PUR,PROD_CODE__OFF  FROM SCH_GROUP_DEF WHERE SCHEME_CODE= ? ";
			
			pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schemeCode);
            rs = pstmt.executeQuery();
            System.out.println("sql....."+sql);
            
            while (rs.next())		  
			{ 
				    
            	prodCodePur=rs.getString("PROD_CODE__PUR");
            	prodCodeOff=rs.getString("PROD_CODE__OFF");
				
			}
			rs.close();
			pstmt.close();
			
			
			
	
            sql = "SELECT  CONFIRMD FROM SCH_GROUP_DEF WHERE  SCHEME_CODE = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schemeCode);
            rs = pstmt.executeQuery();
            System.out.println("sql....."+sql);
           
            if(rs.next())
            {
                confirmed =checkNull(rs.getString("CONFIRMD"));
            }
            rs.close();
            pstmt.close();
            if(confirmed.equalsIgnoreCase("Y"))
            {
           
                errCode = "VTMCONF1";
                errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
                return retString = errString;
            }
            else
            {
			xmlString.append ("<?xml version=\"1.0\"?><Root>");
			xmlString.append("<Detail>");
		
		
			
			conn = getConnection();
	 
			conn.setAutoCommit(false);
			SimpleDateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			System.out.println("Confirm GROUP SCHEME Called.....");

			sql = " SELECT COUNT(*) FROM SCH_PUR_ITEMS WHERE SCHEME_CODE = ?  " ;
					
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, schemeCode);
			rs = pstmt.executeQuery();
			System.out.println("sql....."+sql);
	
			if(rs.next())
			{
				ct = rs.getInt(1);	
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			
			if(ct == 0)
			{
				
				
				
				sql = " SELECT  ITEM_CODE  FROM ITEM WHERE PRODUCT_CODE= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,prodCodePur);
				
				rs = pstmt.executeQuery();
				System.out.println("sql......."+sql);
				
				while (rs.next())		  
				{ 
					
					  System.out.println("LINE NO......."+	lN);
					  
					String itemCode= rs.getString("ITEM_CODE");
			
					
					sql = " select count (*) from scheme_applicability a ,sch_pur_items b where a.scheme_code =  b. scheme_code and  a.app_from<= ? and a.valid_upto>= ? and a.prod_sch='Y' AND b.item_code = ? ";
					 
					pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setTimestamp(1, toDate);
					 pstmt1.setTimestamp(2, toDate);
							 pstmt1.setString(3, itemCode);
						
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							ct1 = rs1.getInt(1);	
						}
						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;
						if(ct1 == 0)
						{
					
				
								sql ="INSERT INTO SCH_PUR_ITEMS (SCHEME_CODE,LINE_NO,ITEM_CODE) VALUES(?,?,?)";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, schemeCode);
								pstmt1.setInt(2, lN);
								pstmt1.setString(3, itemCode);
								int s =pstmt1.executeUpdate();
								
								if(s>0)
								{
									System.out.println("values INSERTED INTO SCH_PUR_ITEMS TABLE.....");
								}
								
								System.out.println("sql....."+sql);
								
								lN++;
								
								pstmt1.close();
								pstmt1=null;
							}
							
					}
					
				
					
					
				
				rs.close();
				pstmt.close();
				
			}
			//Get Detail2
			sql = " SELECT COUNT(*) FROM SCH_OFFER_ITEMS WHERE SCHEME_CODE = ?  " ;
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, schemeCode);
			rs = pstmt.executeQuery();
			System.out.println("sql....."+sql);
	
			if(rs.next())
			{
				ct1 = rs.getInt(1);	
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			
			if(ct1 == 0)
			{
				
				
				
				sql = " SELECT  ITEM_CODE  FROM ITEM WHERE PRODUCT_CODE= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,prodCodeOff);
				
				rs = pstmt.executeQuery();
				System.out.println("sql......."+sql);
				
				while (rs.next())		  
				{ 
					 
					String itemCode= rs.getString("ITEM_CODE");
					
					
					
					
									sql ="INSERT INTO SCH_OFFER_ITEMS (SCHEME_CODE,LINE_NO,ITEM_CODE) VALUES(?,?,?)";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, schemeCode);
									pstmt2.setInt(2, lN1);
									pstmt2.setString(3, itemCode);
									int s1 =pstmt2.executeUpdate();
									
									if(s1>0)
									{
										System.out.println("values INSERTED INTO SCH_OFFER_ITEMS TABLE.....");
									}
									
									System.out.println("sql....."+sql);
									
									lN1++;
									
									pstmt2.close();
									pstmt2=null;
					
							
					
					
					
					
				}
				rs.close();
				pstmt.close();
				
			}
			
			xmlString.append("</Detail>");
			xmlString.append("</Root>");
			System.out.println("xmlString..... ::"+xmlString.toString());
		  
			
	
		/*	if(retString.indexOf("Success") == -1)
			{
				conn.rollback();
			}
			if(retString.indexOf("Success") != -1)
			{*/
			    sql	="UPDATE SCH_GROUP_DEF SET  CONFIRMD ='Y' , CONF_DATE = ?,EMP_CODE__APRV = ? WHERE  SCHEME_CODE = ? ";
				
				pstmt = conn.prepareStatement(sql);
				System.out.println("sql......."+sql);
			    
				pstmt.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
				pstmt.setString(2,genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode"));
			    pstmt.setString(3, schemeCode);
			    updst = pstmt.executeUpdate();
				if(updst == 1)
				{
					System.out.println("CONFIRMED UPDATED ::"+updst);
					
					errCode = "CONFSUCC";
					errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
					retString = errString;
				}
				pstmt.close();
			
			/*}*/
			
			System.out.println("retString ::"+retString);
            }	
		
			
			
			
			
			
			
			
		} catch (Exception e) 
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{		
			try
			{
				
				conn.commit();
				if(conn != null && !conn.isClosed())
				{
					conn.close();
					conn = null;
				}
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
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	
}
