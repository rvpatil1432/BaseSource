/**
 * DEVELOP BY RITESH ON 02/JAN/14
 * PURPOSE: ROAD PERMIT RECORDS GENERATION PROCESS (DI3ISUN015)
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
public class RpermitGenPrc extends ProcessEJB implements RpermitGenPrcLocal,RpermitGenPrcRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	static
	{
		System.out.println("--***** RpermitGenPrc loaded *****-- ");
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
			System.out.println("Exception :WoTransferPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return retStr;

	}
	public String process(Document dom, Document dom2, String windowName,String xtraParams) throws RemoteException, ITMException 
	{
		System.out.println("***********RpermitGenPrc process called*********........");
		String errString = "";
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		//GenericUtility genericUtility = null;
		String transmode="",validuptostr="",efffromstr="",stateCodeTo="";
		String finEntity="",sql="",rpermitFr="",rpermitTo="",remarks="";
		StringBuffer getter1 = new StringBuffer("");
		StringBuffer pattern1 = new StringBuffer("");
		StringBuffer getter2 = new StringBuffer("");
		StringBuffer pattern2 = new StringBuffer("");
		boolean flagTo = false,flagFr = false;;
		HashMap hmap  = null;
		int countCharFr = 0,countCharTo = 0,cnt=0;
		String numStr2="",numStr1="",comp1="",comp2="";
		int numFr = 0, numTo = 0;
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

			finEntity = (checkNull(genericUtility.getColumnValue("fin_entity", dom))).trim();
			rpermitFr = (checkNull(genericUtility.getColumnValue("road_permit__from", dom))).trim();
			rpermitTo = (checkNull(genericUtility.getColumnValue("road_permit__to", dom))).trim();
			stateCodeTo = (checkNull(genericUtility.getColumnValue("state_code__to", dom))).trim();
			efffromstr = checkNull(genericUtility.getColumnValue("eff_date", dom));
			validuptostr = checkNull(genericUtility.getColumnValue("valid_upto", dom));
			transmode = checkNull(genericUtility.getColumnValue("trans_mode", dom));
			remarks = checkNull(genericUtility.getColumnValue("remarks", dom));

			hmap.put("finEntity", finEntity);
			hmap.put("rpermitFr", rpermitFr);
			hmap.put("rpermitTo", rpermitTo);
			hmap.put("stateCodeTo", stateCodeTo);
			hmap.put("efffromstr", efffromstr);
			hmap.put("validuptostr", validuptostr);
			hmap.put("transmode", transmode);
			hmap.put("remarks", remarks);

			if(finEntity ==null || finEntity.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPFINENT1    ","","",conn);
				return errString;
			}
			if(finEntity != null && finEntity.trim().length() > 0)
			{
				sql = "select count(*) from finent where fin_entity = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, finEntity);
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
					errString = itmDBAccessEJB.getErrorString("","VPFINENTIN    ","","",conn);
					return errString;
				}
			}
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
			if(efffromstr ==null || efffromstr.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPEDATE1    ","","",conn);
				return errString;
			}
			if(validuptostr ==null || validuptostr.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPVDATE1    ","","",conn);
				return errString;
			}
			if(stateCodeTo ==null || stateCodeTo.trim().length() == 0)
			{
				errString = itmDBAccessEJB.getErrorString("","VPSTATCD1    ","","",conn);
				return errString;
			}
			if(stateCodeTo != null && stateCodeTo.trim().length() > 0)
			{
				sql = "select count(*) from state where state_code =  ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, stateCodeTo);
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
					errString = itmDBAccessEJB.getErrorString("","VPSTATCDIN","","",conn);
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
				String incNumFr = "",incNumTo = ""; // ADDED ON 22/07/14 START
				boolean countnum = true;
				System.out.println("229_numStr1:["+numStr1+"]numStr2:["+numStr2+"]");
				for(int i = 0; i <=numStr2.length()-1;i++)
				{
					try{
						if(Character.toString(numStr1.charAt(i)).equals(Character.toString(numStr2.charAt(i))) && countnum)
						{
							System.out.println("@@@@@ inside if");
							comp1 =  comp1+numStr1.charAt(i);
							comp2 =  comp2+numStr2.charAt(i);
						}else
						{
							System.out.println("@@@@@ inside else incNumFr["+incNumFr+"]");
							//						if(countnum)
							//						{
							//							comp1 =  comp1.substring(0,comp1.length()-1);
							//							comp2 =  comp2.substring(0,comp2.length()-1);
							//						}
							countnum = false;
							incNumFr = incNumFr + numStr1.charAt(i);
							incNumTo = incNumTo + numStr2.charAt(i);
						}
					}
					catch(StringIndexOutOfBoundsException ex)
					{
						incNumTo = incNumTo + numStr2.charAt(i);
						//						continue;
					}
				}
				//				if(numStr1.trim().length() > 0 && numStr2.trim().length() > 0 )
				//				{  
				////					numFr = Integer.parseInt(numStr1); 
				////					numTo = Integer.parseInt(numStr2);
				//				}
				System.out.println("comp1:["+comp1+"]comp2:["+comp2+"]");
				System.out.println("incNumFr:["+incNumFr+"]incNumTo:["+incNumTo+"]");
				System.out.println("264_numStr1:["+numStr1+"]numStr2:["+numStr2+"]");
				if(!comp1.equals(comp2))
				{
					errString = itmDBAccessEJB.getErrorString("","VPPATERN1    ","","",conn);
					return errString;
				}
				if( incNumFr.trim().length() > 0 && incNumTo.trim().length() > 0 )
				{  
					System.out.println("inside if ...incNumFr:["+incNumFr+"]incNumTo:["+incNumTo+"]");
					numFr = Integer.parseInt(incNumFr); 
					numTo = Integer.parseInt(incNumTo);
				}													// ADDED ON 22/07/14 START
				else if( !(numStr1.equalsIgnoreCase(numStr2)) )
				{
					errString = itmDBAccessEJB.getErrorString("","VPINVRANG2    ","","",conn);
					return errString;	
				}
				//if(!comp1.equals(comp2) || countCharTo!= countCharFr)

				if(numFr > numTo)
				{
					errString = itmDBAccessEJB.getErrorString("","VPINVRANG   ","","",conn);
					return errString;
				}
				else
				{
					hmap.put("pattern1", comp1);
					hmap.put("pattern2", comp2);
					//					hmap.put("getter1", numStr1);
					//					hmap.put("getter2", numStr2);
					hmap.put("getter1", incNumFr);
					hmap.put("getter2", incNumTo);

					//System.out.println("@@@@@@@@hmap["+hmap+"]");
				}
			}
			if(errString == null || errString.trim().length() == 0)
			{
				System.out.println(" method calling ....");
				errString = generateRPermit(hmap,xtraParams,conn);
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
				Logger.getLogger(RpermitGenPrc.class.getName()).log(Level.SEVERE, null, ex);
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
				else if((errString != null ) &&  (errString.indexOf("VPRANGNT1") > -1))
				{
					conn.rollback();
					System.out.println("--error in generate permit no.--");
					errString = itmDBAccessEJB.getErrorString("","VPRANGNT1    ","","",conn);
				}
				else 
				{
					conn.rollback();
					System.out.println("--process not completed--");
					errString = itmDBAccessEJB.getErrorString("","VTEPFAIL  ","","",conn);
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;					
				}		
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
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
	private String generateRPermit(HashMap gmap,String xtraParams,Connection conn) throws RemoteException, ITMException
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
		String tranmode="";
		Timestamp currDate = null,effFrom=null,validUpto=null;
		java.util.Date dateEffFr = null,dateValidUpTo=null;
		int rpermitNoTo = 0,rpermitNoFr = 1;
		int inRows=0,range= 0,num = 0,cnt=0;
		String zeros="",numStr="",finEntity="",remarks="";

		String stateCodeTo = "",rpermitFr="",rpermitTo="",pattern1="";
		try   
		{
			System.out.println("@@@@@@@ generateRPermit called::gmap["+gmap+"]");
			//genericUtility = GenericUtility.getInstance();   
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			dateEffFr = genericUtility.getDateObject((String)gmap.get("efffromstr"));
			effFrom =  java.sql.Timestamp.valueOf(sdf1.format(dateEffFr).toString() + " 00:00:00.0");
			dateValidUpTo = genericUtility.getDateObject((String)gmap.get("validuptostr"));
			validUpto =  java.sql.Timestamp.valueOf(sdf1.format(dateValidUpTo).toString() + " 00:00:00.0");
			currDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); System.out.println("--term id--"+termId);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");System.out.println("--term id--"+termId);
			//loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			rpermitFr = (String)gmap.get("getter1");
			rpermitTo = (String)gmap.get("getter2");
			pattern1 = (String)gmap.get("pattern1");
			stateCodeTo  = (String)gmap.get("stateCodeTo");
			finEntity  = (String)gmap.get("finEntity");
			remarks = (String)gmap.get("remarks");
			tranmode  = (String)gmap.get("transmode");
			String pattern2 = (String)gmap.get("pattern2");

			System.out.println("rpermitFr::["+rpermitFr+"]rpermitTo:["+rpermitTo+"]pattern1["+pattern1+"]");

			if(rpermitFr != null && rpermitFr.trim().length()>0)
			{
				System.out.println(" inside rpermitFr["+rpermitFr+"]");
				rpermitNoFr = Integer.parseInt(rpermitFr);
			}
			if(rpermitTo != null && rpermitTo.trim().length()>0)
			{
				System.out.println(" inside rpermitTo["+rpermitTo+"]");
				rpermitNoTo = Integer.parseInt(rpermitTo);
			}
			range = rpermitNoTo - rpermitNoFr;
			
			System.out.println("gmap.get(rpermitFr)::["+gmap.get("rpermitFr")+"]gmap.get(rpermitTo):["+gmap.get("rpermitTo")+"]range["+range+"]");

			if( gmap.get("rpermitFr").toString().equalsIgnoreCase(gmap.get("rpermitTo").toString()) )
			{
				range = 0;
			}

			System.out.println("@@@@@@ range["+range+"]pattern1+rpermitFr["+pattern1+""+rpermitFr+"]");

			for(int k = 0;k<= range;k++)
			{
				sql = "select COUNT(*) from roadpermit where RD_PERMIT_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pattern1+rpermitFr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(cnt > 0)
				{
					conn.rollback();
					System.out.println("--transaction rollback--");
					retString = "VPRANGNT1";
					return retString;
				}

				sql = " Insert into roadpermit (RD_PERMIT_NO,DESCR,STAN_CODE__FROM,STAN_CODE__TO,PERMIT_DATE,EXPIRY_DATE, " +
						" STATUS,CHG_DATE,CHG_USER,CHG_TERM,REMARKS,FIN_ENTITY,STATE_CODE__TO,tran_mode)  " +
						" values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, (pattern1+rpermitFr).trim());
				pstmt.setString(2, "");
				pstmt.setString(3, "");
				pstmt.setString(4, "");
				pstmt.setTimestamp(5, effFrom);
				pstmt.setTimestamp(6, validUpto);
				pstmt.setString(7, "O"); 
				pstmt.setTimestamp(8, currDate);
				pstmt.setString(9, chgUser);
				pstmt.setString(10,chgTerm);
				pstmt.setString(11,remarks);
				pstmt.setString(12,finEntity);
				pstmt.setString(13,stateCodeTo);
				pstmt.setString(14,tranmode);
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
				System.out.println(" result string "+rpermitFr);
			}
			System.out.println("No. Of Rows Inserted ::"+inRows);
			if(inRows > 0)
			{
				retString = "Success";
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
	class A
	{
		static final int i = 0;
		String str = "Itna bada raskala ralakala ";
	}
}
