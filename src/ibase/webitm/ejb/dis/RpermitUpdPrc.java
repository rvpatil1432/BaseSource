/**
 * DEVELOP BY RITESH ON 06/JAN/14
 * PURPOSE: ROAD PERMIT RECORDS UPDATION PROCESS (DI3JSUN001)
 */
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.w3c.dom.Document;

@Stateless
public class RpermitUpdPrc extends ProcessEJB implements RpermitUpdPrcLocal,RpermitUpdPrcRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	static
	{
		System.out.println("-- RpermitUpdPrc compiled -- ");
	}
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
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
			System.out.println("Exception :RpermitUpdPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return retStr;

	} 
	//076RFE0009
	public String process(Document dom, Document dom2, String windowName,String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("RpermitUpdPrc process called........");
		String errString = "";
		Connection conn = null;
		ConnDriver connDriver = null;
		//PreparedStatement pstmt = null;
		//ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		//GenericUtility genericUtility = null;
		String siteCodeFr="",allocDateStr="",stateCodeFr="";
		String rcpDateStr="",rpermitFr="",rpermitTo="",remarks="";
		StringBuffer getter1 = new StringBuffer("");
		StringBuffer pattern1 = new StringBuffer("");
		StringBuffer getter2 = new StringBuffer("");
		StringBuffer pattern2 = new StringBuffer("");
		boolean flagTo = false,flagFr = false;;
		HashMap hmap  = null;
		int countCharFr = 0,countCharTo = 0,cnt=0;
		int numFr = 0, numTo = 0;
		String numStr2="",numStr1="",comp1="",comp2="",sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			//genericUtility = new GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;            			
			conn.setAutoCommit(false);
			hmap  = new HashMap();

			rpermitFr = (checkNull(genericUtility.getColumnValue("road_permit__from", dom))).trim();
			rpermitTo = (checkNull(genericUtility.getColumnValue("road_permit__to", dom))).trim();
			siteCodeFr = (checkNull(genericUtility.getColumnValue("site_code__fr", dom))).trim();
			allocDateStr = checkNull(genericUtility.getColumnValue("alloc_date", dom));
			stateCodeFr= (checkNull(genericUtility.getColumnValue("state_code__fr", dom))).trim();
			rcpDateStr = checkNull(genericUtility.getColumnValue("recpt_date", dom));
			remarks = checkNull(genericUtility.getColumnValue("remarks", dom));

			hmap.put("rpermitFr", rpermitFr);
			hmap.put("rpermitTo", rpermitTo);
			hmap.put("siteCodeFr", siteCodeFr);
			hmap.put("allocDateStr", allocDateStr);
			hmap.put("stateCodeFr", stateCodeFr);
			hmap.put("rcpDateStr", rcpDateStr);
			hmap.put("remarks", remarks);


			if(rpermitFr ==null || rpermitFr.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPRPERFR1    ","","",conn);
				return errString;
			}
			if(rpermitTo ==null || rpermitTo.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPRPERTO1    ","","",conn);
				return errString;
			}
			if(stateCodeFr ==null || stateCodeFr.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPSTATCD2    ","","",conn);
				return errString;
			}
			if(rpermitFr !=null && rpermitFr.trim().length() > 0)
			{
				sql= " select count(*) from roadpermit where rd_permit_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, rpermitFr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VPRDFR1","","",conn);
					return errString;
				}
			}
			if(rpermitTo !=null & rpermitTo.trim().length() > 0)
			{
				sql= " select count(*) from roadpermit where rd_permit_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, rpermitTo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VPRDTO1","","",conn);
					return errString;
				}
			}
			if(rpermitFr.trim().length() > 0 && rpermitTo.trim().length() > 0)
			{

				for(int i= rpermitFr.length()-1; i >= 0; i--)
				{
					countCharFr ++;
					if((rpermitFr.charAt(i) <= 57 && rpermitFr.charAt(i) >= 48) && flagFr == false)
					{
						getter1.append(rpermitFr.charAt(i));
					}
					else
					{
						flagFr = true;
						pattern1.append(rpermitFr.charAt(i));
					}
				}
				for(int i= rpermitTo.length()-1; i >= 0; i--)
				{
					countCharTo ++;
					if((rpermitTo.charAt(i) <= 57 && rpermitTo.charAt(i) >= 48) && flagTo == false)
					{
						getter2.append(rpermitTo.charAt(i));
					}
					else
					{
						flagTo = true;
						pattern2.append(rpermitTo.charAt(i));
					}
				}
				comp1 = pattern1.reverse().toString();
				comp2= pattern2.reverse().toString();
				numStr1 = getter1.reverse().toString();
				numStr2 = getter2.reverse().toString();

				///
				// String a1= "ABC2071025177533";
				// String a2= "ABC2071025177600";
				String common = "";

				for( int i = 0 ; i< rpermitFr.length() && i< rpermitTo.length(); i++)
				{
					if( rpermitFr.charAt(i) == rpermitTo.charAt(i) )
					{
						common = common +rpermitFr.charAt(i);
					}

				}

				String rpermitFrremaining = rpermitFr.substring(common.length(), rpermitFr.length());
				String rpermitToremaining = rpermitTo.substring(common.length(), rpermitTo.length());

				if(rpermitFrremaining.trim().length() > 0 && rpermitToremaining.trim().length() > 0 )
				{
					numFr = Integer.parseInt(rpermitFrremaining);
					numTo = Integer.parseInt(rpermitToremaining);
				}
				///

				System.out.println("numStr1:"+numStr1+"numStr2:"+numStr2+"common["+common+"]");

				//				if(numStr1.trim().length() > 0 && numStr2.trim().length() > 0 )
				//				{
				//				numFr = Integer.parseInt(numStr1);
				//				numTo = Integer.parseInt(numStr2);
				//				}
				//	if(!comp1.equals(comp2) || countCharTo!= countCharFr)
				if(!comp1.equals(comp2))
				{
					errString = itmDBAccessEJB.getErrorString("","VPPATERN1    ","","",conn);
					return errString;
				}
				if(numFr > numTo)
				{
					errString = itmDBAccessEJB.getErrorString("","VPINVRANG   ","","",conn);
					return errString;
				}
				else
				{
					//					hmap.put("pattern1", comp1);
					//					hmap.put("pattern2", comp2);
					//					hmap.put("getter1", numStr1);
					//					hmap.put("getter2", numStr2);

					hmap.put("pattern1", common);
					hmap.put("pattern2", common);
					hmap.put("getter1", rpermitFrremaining);
					hmap.put("getter2", rpermitToremaining);

				}
			}
			if(siteCodeFr.trim().length() > 0)
			{
				sql = "select count(*) from site where site_code =  ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeFr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VPSITECD1","","",conn);
					return errString;
				}
			}
			if(stateCodeFr.trim().length() > 0)
			{
				sql = "select count(*) from state where state_code =  ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, stateCodeFr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cnt == 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VPSTATCDFR","","",conn);
					return errString;
				}
			}
			if(errString == null || errString.trim().length() == 0)
			{
				System.out.println(" method calling ....");
				errString = UpdateRPermit(hmap,xtraParams,conn);
			}
		}
		catch( Exception e)
		{			
			try 
			{
				conn.rollback();
			} 
			catch (SQLException ex) 
			{
				Logger.getLogger(RpermitUpdPrc.class.getName()).log(Level.SEVERE, null, ex);
			}
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		finally
		{		
			try  
			{
				if((errString != null ) &&  (errString.indexOf("Success") > -1))
				{
					conn.commit();
					System.out.println("--process completed--");
					errString = itmDBAccessEJB.getErrorString("","VPSUCC1    ","","",conn);
				}
				else if((errString != null ) &&  (errString.indexOf("VTNOREC") > -1))
				{
					conn.rollback();
					System.out.println("--no record found--");
					errString = itmDBAccessEJB.getErrorString("","VTNOREC1    ","","",conn);
				}
				else 
				{
					conn.rollback();
					System.out.println("--process not completed--");
					errString = itmDBAccessEJB.getErrorString("","VTEPFAIL  ","","",conn);
				}
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
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
	private String UpdateRPermit(HashMap gmap,String xtraParams,Connection conn) throws RemoteException, ITMException
	{	
		String  userId = "";
		String  termId = "";
		String chgTerm="",chgUser="";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = null;
		String retString  = null;
		//String loginSite ="";
		String siteCodeFr="";
		Timestamp currDate = null,rcpDate=null,allocDate=null;
		java.util.Date datealloc = null,datercp=null;
		int rpermitNoTo = 0,rpermitNoFr = 1;
		int inRows=0,range= 0,num = 0;
		String zeros="",numStr="",remarks="";
		String stateCodeFr = "",rpermitFr="",rpermitTo="",pattern1="";
		try   
		{
			System.out.println("@@@@@@@ UpdateRPermit called::gmap["+gmap+"]");
			//genericUtility = GenericUtility.getInstance();   
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			if(((String)gmap.get("allocDateStr")) != null && ((String)gmap.get("allocDateStr")).trim().length() > 0)
			{
				datealloc = genericUtility.getDateObject((String)gmap.get("allocDateStr"));
				allocDate =  java.sql.Timestamp.valueOf(sdf1.format(datealloc).toString() + " 00:00:00.0");
			}
			if(((String)gmap.get("rcpDateStr")) != null && ((String)gmap.get("rcpDateStr")).trim().length() > 0)
			{
				datercp = genericUtility.getDateObject((String)gmap.get("rcpDateStr"));
				rcpDate =  java.sql.Timestamp.valueOf(sdf1.format(datercp).toString() + " 00:00:00.0");
			}
			currDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); System.out.println("--term id--"+termId);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");System.out.println("--term id--"+termId);
			//loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			rpermitFr = (String)gmap.get("getter1");
			rpermitTo = (String)gmap.get("getter2");
			pattern1 = (String)gmap.get("pattern1");
			siteCodeFr  = checkNull((String)gmap.get("siteCodeFr"));
			stateCodeFr  = checkNull((String)gmap.get("stateCodeFr"));
			remarks = (String)gmap.get("remarks");
			//String pattern2 = (String)gmap.get("pattern2");
			if(rpermitFr != null && rpermitFr.trim().length()>0)
				rpermitNoFr = Integer.parseInt(rpermitFr);
			if(rpermitTo != null && rpermitTo.trim().length()>0)
				rpermitNoTo = Integer.parseInt(rpermitTo);
			range = rpermitNoTo - rpermitNoFr;
			System.out.println("rpermitNoFr::"+rpermitNoFr+"rpermitNoTo:"+rpermitNoTo);

			if( gmap.get("rpermitFr").toString().equalsIgnoreCase(gmap.get("rpermitTo").toString()) )
			{
				range = 0;
			}

			System.out.println("@@@@@@ range["+range+"]pattern1+rpermitFr["+pattern1+""+rpermitFr+"]");


			for(int k = 0;k<= range;k++)
			{
				sql = "update roadpermit set site_code__fr = ? , alloc_date = ?,state_code__fr = ?,recpt_date = ?, remarks = ?,  " +
						" chg_term = ? , chg_user = ? , chg_date = ? where rd_permit_no = ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCodeFr);
				pstmt.setTimestamp(2, allocDate);
				pstmt.setString(3, stateCodeFr);
				pstmt.setTimestamp(4, rcpDate);
				pstmt.setString(5, remarks);
				pstmt.setString(6, chgTerm);
				pstmt.setString(7, chgUser);
				pstmt.setTimestamp(8, currDate);
				pstmt.setString(9, pattern1+rpermitFr);
				inRows = inRows+ pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				zeros="";

				if( rpermitFr != null && (!"".equalsIgnoreCase(rpermitFr)) )
				{ 	
					System.out.println("@@@@@ inside if...rpermitFr["+rpermitFr+"].");
					num = Integer.parseInt(rpermitFr);
					num ++;
					numStr = Integer.toString(num);
					for(int i = 1; i <= rpermitFr.length() - numStr.length();i++)
					{
						zeros = zeros+0 ;
					}
					rpermitFr = zeros+num;
				}
				System.out.println("result string "+rpermitFr);
			}
			System.out.println("No.Of Rows updated ::"+inRows);
			if(inRows > 0)
			{
				retString = "Success";
			}else{
				retString = "VTNOREC";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			retString = e.getMessage();
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
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}	
			}
			catch(Exception e)
			{
				e.printStackTrace();
				retString = e.getMessage();
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result [" + retString + "]");
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
}