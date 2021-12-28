package ibase.webitm.ejb.dis;

import java.sql.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import java.util.HashMap;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * @author base
 *
 */
public class InvAllocTraceBean
{
	//FileWriter f = null;// commented  all log writing by nandkumar gadkari on 19/04/19
    /**
     * Allocates/deallocates stock
     *
     * @param     invallocTraceMap  HashMap with allocation details
     * @param	  conn   Database connection
     * @return    Error code if fails
     * @exception ITMException 
     */
	public String updateInvallocTrace(HashMap invallocTraceMap, Connection conn) throws ITMException,Exception
	{
		//String fileDest = "";
		double tempAllocQty = 0;
		try
		{
			//changes by gulzar on 12/12/2011 to write the log file in jboss log location  
			//f = new FileWriter("invalloc.log", true); 
		//	fileDest = CommonConstants.JBOSSHOME + File.separator + "server" + File.separator + "default" + File.separator + "log" + File.separator + "invalloc.log";
			//f = new FileWriter( fileDest, true); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			//writeException(e, fileDest );
		}
		String errString = "";
		String errCode = "" ;
		String sql = null ;
		String sqlUpdate = null ;
		String keyStringQuery = null ;
		CommonConstants commonConstants = new CommonConstants();
		String refSer = (String)invallocTraceMap.get("ref_ser");
		String refId = (String)invallocTraceMap.get("ref_id");
		String refLine = (String)invallocTraceMap.get("ref_line");
		String siteCode = (String)invallocTraceMap.get("site_code");
		String itemCode = (String)invallocTraceMap.get("item_code");
		String locCode = (String)invallocTraceMap.get("loc_code");
		String lotNo = (String)invallocTraceMap.get("lot_no");
		String lotSl = (String)invallocTraceMap.get("lot_sl");
		double allocQty = ((Double)invallocTraceMap.get("alloc_qty")).doubleValue();
		String chgUser = (String)invallocTraceMap.get("chg_user");
	    String chgTerm = (String)invallocTraceMap.get("chg_term");
	    String chgWin = (String)invallocTraceMap.get("chg_win");
	  //added by nandkumar gadkari on 17/04/19-------start=----------
	    String allocRef = (String)invallocTraceMap.get("alloc_ref") == null ?  " " : (String)invallocTraceMap.get("alloc_ref") ;
		//added by nandkumar gadkari on 17/04/19-------end=----------
	    if(lotNo == null)
		lotNo = " ";
		if(lotSl == null)
		lotSl = " ";

		//Changed by gulzar on 12/12/2011
		if ( chgUser == null || chgUser.trim().length() == 0 )
		{
			chgUser = "SYSTEM";
		}
		if ( chgTerm == null || chgTerm.trim().length() == 0 )
		{
			chgTerm = "SYSTEM";
		}
		//End changes by gulzar on 12/12/2011
	    
		java.sql.Date chgDate = new java.sql.Date(System.currentTimeMillis());
		java.sql.Date tranDate = new java.sql.Date(System.currentTimeMillis());
		PreparedStatement pstmt = null ;
		ResultSet rSet = null, rs = null ;
		Statement stmt = null ;
		String tranId = null ;
	   
	    try
	    {
	    	keyStringQuery = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'T_ALLOCTRACE'";
	    	System.out.println("keyStringQuery--------->>"+keyStringQuery);
	 
			stmt = conn.createStatement();
			rSet = stmt.executeQuery(keyStringQuery);
			System.out.println("keyString :"+rSet.toString());
			String tranSer1 = "";
			String keyString = "";
			String keyCol = "";
			if (rSet.next())
			{
				keyString = rSet.getString(1);
				keyCol = rSet.getString(2);
				tranSer1 = rSet.getString(3);				
			}
			rSet.close();
			stmt.close();
			rSet = null;
			stmt = null;
			// 31/10/13 manoharan locking to be done before allocation
			if (CommonConstants.DB_NAME.equalsIgnoreCase("DB2") || CommonConstants.DB_NAME.equalsIgnoreCase("MYSQL") ){
				sql =" select alloc_qty from stock where item_code = ?"
			  	+" and site_code = ?" 
				+" and loc_code  = ?" 
				+" and lot_no = ?"
				+"and lot_sl = ? for update ";
			}
			
			//changed by manish add space before and lot_sl on 31/08/2015
			else if (CommonConstants.DB_NAME.equalsIgnoreCase("MSSQL")){
				sql =" select alloc_qty from stock (updlock) where item_code = ?"
			  	+" and site_code = ?" 
				+" and loc_code  = ?" 
				+" and lot_no = ?"
				+" and lot_sl = ?  ";
			}
			//changed by manish add space before and lot_sl on 31/08/2015
			
			else
			{
				sql =" select alloc_qty from stock where item_code = ?"
			  	+" and site_code = ?" 
				+" and loc_code  = ?" 
				+" and lot_no = ?"
				+"and lot_sl = ? for update nowait ";
			}
			
            pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);
			pstmt.setString(2,siteCode);
			pstmt.setString(3,locCode);
			pstmt.setString(4,lotNo);
			pstmt.setString(5,lotSl);
			
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tempAllocQty = rs.getDouble(1);
			}
			else
			{
				errString = "VTLCKERR";
			}
			pstmt.close();
			rs.close();
			if (errString != null && errString.trim().length() > 0)
			{
				return errString;
			}

				
			// end 
			
			//f.write(keyString + " " + keyCol + " " + tranSer1+ "\r\n");
			System.out.println("keyString :"+ keyString);
			System.out.println("keyCol :"+ keyCol);
			System.out.println("tranSer1 :"+ tranSer1);
			String xmlValues = "";
			String tranDateStr = getCurrdateAppFormat(); //added by rajendra on 17/04/09 for tran date
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>"; //added by rajendra on 17/04/09
			xmlValues = xmlValues +"</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
		//	f.write("trace tran id ... " +tranId + "\r\n");
			System.out.println("tranId :"+tranId);
		
			sql = "INSERT INTO INVALLOC_TRACE (TRAN_ID, TRAN_DATE, REF_SER, REF_ID,"
				+"REF_LINE,ITEM_CODE, SITE_CODE, LOC_CODE,LOT_NO, LOT_SL, ALLOC_QTY, CHG_WIN," 
				+"CHG_USER, CHG_TERM, CHG_DATE ,ALLOC_REF )VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";//ALLOC_REF COLUMN ADDED BY NANDKUMAR GADKARI ON 17/04/19
				
			pstmt = conn.prepareStatement(sql);
		
			System.out.println("trans id="+ tranId);
			System.out.println("tranDate="+ tranDate);
			System.out.println("refSer="+ refSer);
			System.out.println("refId="+ refId);
			System.out.println("refLine="+ refLine);
			System.out.println("itemCode="+ itemCode);
			System.out.println("siteCodeShip="+ siteCode);
			System.out.println("locCode="+ locCode);
			System.out.println("lotNo="+ lotNo);
			System.out.println("LOT_SL="+ lotSl);
			System.out.println("ALLOC_QTY="+ allocQty);
			System.out.println("chgWin="+ chgWin);
			System.out.println("chgUser="+ chgUser);
			System.out.println("chgTerm="+ chgTerm);
			System.out.println("chgDate="+ chgDate);
			/*f.write("trans id="+ tranId+"\n");
			f.write("tranDate="+ tranDate+"\n");
			f.write("refSer="+ refSer+"\n");
			f.write("refId="+ refId+"\n");
			f.write("refLine="+ refLine+"\n");
			f.write("itemCode="+ itemCode+"\n");
			f.write("locCode="+ locCode+"\n");
			f.write("lotNo="+ lotNo+"\n");
			f.write("lotSl="+ lotSl+"\n");
			f.write("ALLOC_QTY="+ allocQty+"\n");*/
			
			
		
			pstmt.setString(1,tranId);
			pstmt.setDate(2,tranDate);
			pstmt.setString(3,refSer);
			pstmt.setString(4,refId);
			pstmt.setString(5,refLine);
			pstmt.setString(6,itemCode );
			pstmt.setString(7,siteCode);
			pstmt.setString(8,locCode);
			pstmt.setString(9,lotNo );
			pstmt.setString(10,lotSl);
			pstmt.setDouble(11,allocQty);
			pstmt.setString(12,chgWin);
			pstmt.setString(13,chgUser);
			pstmt.setString(14,chgTerm);
			pstmt.setDate(15,chgDate);
			pstmt.setString(16,allocRef);//updateRef COLUMN ADDED BY NANDKUMAR GADKARI ON 17/04/19
			pstmt.executeUpdate();
			
			//added by msalam on 281209 start
			pstmt.close();
			pstmt = null;
			//added by msalam on 281209 end
			System.out.println("insertion of sql inside updateInvallocTrace success on date "+ chgDate);
			
			System.out.println("Allocate trace Updated...............................");
		
			sqlUpdate = "UPDATE STOCK SET ALLOC_QTY =(CASE WHEN ALLOC_QTY IS NULL THEN 0 ELSE ALLOC_QTY END) + ? ,ALLOC_REF='"+allocRef+"' "//updateRef column added by nandkumar gadkari on 17/04/19
					  +"WHERE ITEM_CODE = '"+itemCode+"' AND SITE_CODE = '"+ siteCode+"' AND LOC_CODE ='"+locCode+"'  AND LOT_NO = '"+lotNo+"' AND LOT_SL = '"+lotSl+"'";
			pstmt = conn.prepareStatement(sqlUpdate);
			pstmt.setDouble(1,allocQty) ;
			//pstmt.setString(2,itemCode.trim()) ;
			//pstmt.setString(3, siteCode.trim()) ;
			///pstmt.setString(4,locCode.trim()) ;
			//pstmt.setString(5,lotNo.trim()) ;
			//pstmt.setString(6,lotSl.trim()) ; 
			
//			if(allocQty >= 0)  // Changed by Alka 14/03/2007 allocQty had to be updated in stock for any case.
//			{
				System.out.println("sqlUpdate "+ sqlUpdate);
				int i = pstmt.executeUpdate() ;
				//added by msalam on 281209 start
				pstmt.close();
				pstmt = null;
				//added by msalam on 281209 end

				System.out.println("Records updated "+ i);
				if(i>0)
				{
					//f.write("sqlUpdate="+ sqlUpdate+"\n"+"Stock update success fully........................."+i+"\n");
				}
				else
				{
					throw new ITMException(new Exception("Unable to update stock for [" + sqlUpdate + "]"));
				}
					
//			}
			System.out.println("Updated End.") ;
			//f.write("Updated End..........");
		}
		catch(SQLException e)
		{
			//f.write("TRANSATION ROLL BACK IN updateInvallocTrace"+"\n");
			try{conn.rollback();}catch(Exception ee2){}
			//writeException(e, fileDest);
			System.out.println("SQLException :updateInvallocTrace : "  + sqlUpdate + "\n" +e.getMessage());
			System.out.println("ALLOC_QTY : " + allocQty);
			System.out.println("ITEM_CODE : " + itemCode);
			System.out.println("SITE_CODE : " + siteCode);
			System.out.println("LOC_CODE : " + locCode);
			System.out.println("LOT_NO : " + lotNo);
			System.out.println("LOT_SL : " + lotSl);
			errString = e.getMessage();
			e.printStackTrace();
			//return errString;
			throw new ITMException(e);
					
		}
		catch(Exception e)
		{
			//writeException(e, fileDest);
			System.out.println("Exception :updateInvallocTrace :"  + sqlUpdate + "\n" +e.getMessage());
			errString = e.getMessage();
			e.printStackTrace();
			try
			{
				//f.write("TRANSATION ROLL BACK IN updateInvallocTrace"+"\n");
				try{conn.rollback();}catch(Exception ee2){}
			
			}
			catch(Exception e1)
			{
				//writeException(e, fileDest);
				errString = e1.getMessage();
				e = e1;				
			}
			//return errString;	
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//f.close();
				if(conn != null)
				{	
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(stmt != null)
					{
						stmt.close();
						stmt = null;
					}
				//	conn.close();
				//	conn = null;
				}
			}
			catch(Exception e)
			{
				try{conn.rollback();
				//f.write("TRANSATION ROLL BACK IN updateInvallocTrace"+"\n");
				}catch(Exception ee2){}
				errString = e.getMessage(); 
				e.printStackTrace();
				//writeException(e, fileDest);
				return errString;	
			}
			//return errString;	
		}
		return errString;
	}
	/**
	 * @param e
	 * @param fileDest
	 * @throws Exception
	 */
	private void writeException(Exception e, String fileDest) throws Exception
	{
		try
		{
			//f.write(e.getMessage() + "\r\n");
			//PrintStream t = new PrintStream(new FileOutputStream(new File("C:\\invalloc.log"),true));
			PrintStream t = new PrintStream(new FileOutputStream(new File(fileDest),true));
			e.printStackTrace(t);	
		}	
		catch(Exception t){throw t;}
	}
	/**
	 * @return
	 */
	private String getCurrdateAppFormat()
	{
        String s = "";
        //GenericUtility genericUtility = GenericUtility.getInstance();
        E12GenericUtility genericUtility= new  E12GenericUtility();
        try
        {
            java.util.Date date = null;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println(genericUtility.getDBDateFormat());

            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
            s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
        }
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
        }
        return s;
    }
}