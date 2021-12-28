/********************************************************
	Title : ItemLotOwnCopyPrc
	Date  : 06/01/15
	Developer: Priyanka

 ********************************************************/
package ibase.webitm.ejb.dis;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.sql.*;

import org.w3c.dom.*;
import java.text.SimpleDateFormat; 
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ItemLotOwnCopyPrc extends ProcessEJB implements ItemLotOwnCopyPrcLocal,ItemLotOwnCopyPrcRemote
{	
String loginSiteCode = null;
//GenericUtility genericUtility = GenericUtility.getInstance();
E12GenericUtility genericUtility= new  E12GenericUtility();
String currDateTs = null;
String chgUser = "";
String chgTerm = ""; 
ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
	throws RemoteException,ITMException
{
	Document detailDom = null;
	Document headerDom = null;
	String retStr = "";
	/*Connection conn = null;	
	ConnDriver connDriver = null;
	*/
	boolean isConn= false;
	System.out.println("Process method called......");	
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
		 /*connDriver = new ConnDriver();
         conn = connDriver.getConnectDB("Driver");
         conn.setAutoCommit(false);*/
		retStr = process(headerDom, detailDom, windowName, xtraParams, null,isConn );
	}
	catch (Exception e)
	{			
		System.out.println("Exception :ItemLotOwnCopyPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
		e.printStackTrace();
		throw new ITMException(e);
	}
	return retStr;
}//END OF PROCESS (1)

public String process(Document headerDom, Document detailDom, String windowName, String xtraParams ,Connection conn, boolean connStatus) throws RemoteException,ITMException
{
	//Connection conn = null;	
	String resultString = "", errString = "";
	boolean isError = false;
	PreparedStatement pstmt = null;	
	PreparedStatement pstmt1 = null;	
	ResultSet rs = null;
	ResultSet rs1 = null;
	String sql = "",sql1="";		
	String siteCode = "";	
	String itemCodeFrom = "",lotNoFrom = "",lotNoTo = "",itemCodeCopy = "",lotNoFromCopy = "",lotNoToCopy="";
	String siteCodeSupp = "",itemSer = "",salesGrp = "",existFlag="";	
	int cnt=0,updCnt=0;	
	int  lenlotNoFrom = 0, counter = 0, oldLen = 0, newLotNoFrom  = 0, newLotLen = 0;
	String set = "", newLotNoStr = "",lotNoFromNew="",lotNoFrom1="",lotNoTo1="";	
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();	
	String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
	loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
	chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
	chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");	
	
	try
	{
		System.out.println("conn::"+conn);
		if (conn==null)
		{
			ConnDriver connDriver = new ConnDriver();			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 			
			connDriver = null;
			conn.setAutoCommit(false);
			connStatus = true;
		}
				
		SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
		java.util.Date currentDate = new java.util.Date();
		Timestamp newsysDate = java.sql.Timestamp.valueOf( sdf1.format(currentDate)+" 00:00:00.0");
		
		itemCodeFrom = genericUtility.getColumnValue("item_code",headerDom );	
		lotNoFrom = genericUtility.getColumnValue("lot_no__from",headerDom );
		lotNoTo = genericUtility.getColumnValue("lot_no__to",headerDom );
		itemCodeCopy = genericUtility.getColumnValue("item_code_copy",headerDom );
		lotNoFromCopy   = genericUtility.getColumnValue("lot_no__from_copy",headerDom );
		lotNoToCopy   = genericUtility.getColumnValue("lot_no__to_copy",headerDom );
		
		System.out.println("Item Code========="+itemCodeFrom);
		System.out.println("lot No From========="+lotNoFrom);
		System.out.println("lot No To========="+lotNoTo);
		System.out.println("Item Code Copy========="+itemCodeCopy);
		System.out.println("lot No From Copy========="+lotNoFromCopy);
		System.out.println("lot No To Copy========="+lotNoToCopy);
		
		
		
		if(itemCodeFrom==null ||itemCodeFrom.trim().length()<=0)
		{
			errString = itmDBAccessEJB.getErrorString( "", "VEITEM2", userId ,"",conn);
			System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
			return errString;
		}
		else if(itemCodeFrom!=null ||itemCodeFrom.trim().length()>0)
		{
			sql="select count(*) from item where item_code = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCodeFrom);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt(1);
				System.out.println("Count of item Code===="+cnt);
			}
			pstmt.close();
	    	rs.close();
	    	pstmt = null;
	    	rs = null;
	    	if(cnt==0)
	    	{
	    		errString = itmDBAccessEJB.getErrorString( "", "VTITMCNM", userId ,"",conn);
				System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
				return errString;
	    	}
		}
		
			//lot no from
		
		   if(lotNoFrom==null ||lotNoFrom.trim().length()<=0)
			{
				errString = itmDBAccessEJB.getErrorString( "", "VELOTNO1", userId ,"",conn);
				System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
				return errString;
			}
		    else if(lotNoFrom!=null && lotNoFrom.trim().length()>0)
			{
				sql="select count(*) from item_lot_own where lot_no__from = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, lotNoFrom);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);
					System.out.println("Count of item Code===="+cnt);
				}
				pstmt.close();
		    	rs.close();
		    	pstmt = null;
		    	rs = null;
		    	if(cnt==0)
		    	{
		    		errString = itmDBAccessEJB.getErrorString( "", "VTIMLOT01", userId ,"",conn);
					System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
					return errString;
		    	}
			}
		   //lot no to
		   
			
			if(lotNoTo==null ||lotNoTo.trim().length()<=0)
			{
				errString = itmDBAccessEJB.getErrorString( "", "VELOTNO3", userId ,"",conn);
				System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
				return errString;
			}
			else if(lotNoTo!=null && lotNoTo.trim().length()>0)
			{
				sql="select count(*) from item_lot_own where lot_no__to = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, lotNoTo);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					cnt=rs.getInt(1);
					System.out.println("Count of item Code===="+cnt);
				}
				pstmt.close();
		    	rs.close();
		    	pstmt = null;
		    	rs = null;
		    	if(cnt==0)
		    	{
		    		errString = itmDBAccessEJB.getErrorString( "", "VTITMLOT02", userId ,"",conn);
					System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
					return errString;
		    	}
			}
		
		//itemCodeCopy is null or length <=0
		if(itemCodeCopy==null ||itemCodeCopy.trim().length()<=0)
		{
			errString = itmDBAccessEJB.getErrorString( "", "VTIMCPNULL", userId ,"",conn);
			System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
			return errString;
		}
		else if(itemCodeCopy!=null ||itemCodeCopy.trim().length()>0)
		{
			sql="select count(*) from item where item_code = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCodeCopy);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt(1);
				System.out.println("Count of itemCodeCopy===="+cnt);
			}
			pstmt.close();
	    	rs.close();
	    	pstmt = null;
	    	rs = null;
	    	if(cnt==0)
	    	{
	    		errString = itmDBAccessEJB.getErrorString( "", "VTITMCNM", userId ,"",conn);
				System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
				return errString;
	    	}
		}
		
		
		 if(itemCodeFrom.trim().equals(itemCodeCopy))//Item code similar to source item code!
		{
			errString = itmDBAccessEJB.getErrorString( "", "VEITEM3", userId ,"",conn);
			System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
			return errString;
		}
		 	    
	    
//lotNoFromCopy
	    
		if(lotNoFromCopy==null|| lotNoFromCopy.trim().length()<=0)//Lot no. from left blank!
		{
			errString = itmDBAccessEJB.getErrorString( "", "VELOTNO1", userId ,"",conn);
			System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
			return errString;
		}
		
		else if(lotNoFromCopy!=null && lotNoFromCopy.trim().length()>0)
	     {
			sql="select count(*) from item_lot_own where lot_no__from = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, lotNoFromCopy);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt(1);
				System.out.println("Count of lotNoFromCopy===="+cnt);
			}
			pstmt.close();
	    	rs.close();
	    	pstmt = null;
	    	rs = null;
		    	if(cnt>0)
		    	{
		    		errString = itmDBAccessEJB.getErrorString( "", "VTLOTEXT1", userId ,"",conn);
					System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
					return errString;
		    	}
	      }
		//lotNoToCopy
		
	    if(lotNoToCopy==null|| lotNoToCopy.trim().length()<=0)//Lot no. from left blank!
		{
			errString = itmDBAccessEJB.getErrorString( "", "VELOTNO3", userId ,"",conn);
			System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
			return errString;
		}
	    else if(lotNoToCopy!=null && lotNoToCopy.trim().length()>0)
		{
			sql="select count(*) from item_lot_own where lot_no__from = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, lotNoToCopy);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt(1);
				System.out.println("Count of lotNoToCopy===="+cnt);
			}
			pstmt.close();
	    	rs.close();
	    	pstmt = null;
	    	rs = null;
	    	if(cnt>0)
	    	{
	    		errString = itmDBAccessEJB.getErrorString( "", "VTLOTEXT2", userId ,"",conn);
				System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
				return errString;
	    	}
		}
				
		
		
		//sql="  select item_code,item_ser,lot_no__from,lot_no__to,site_code,site_code__supp,sales_grp,chg_date,chg_term,chg_user from item_lot_own where item_code= ?  and lot_no__from= ? and lot_no__to= ?";
		sql="  select item_code,item_ser,lot_no__from,lot_no__to,site_code,site_code__supp,sales_grp from item_lot_own where item_code= ?  and lot_no__from= ? and lot_no__to= ?";
		pstmt=conn.prepareStatement(sql);
		pstmt.setString(1,itemCodeFrom);
		pstmt.setString(2,lotNoFrom);
		pstmt.setString(3,lotNoTo);		
		rs=pstmt.executeQuery();
		while(rs.next())
		{
			itemCodeFrom=checkNull(rs.getString("item_code"));
			itemSer=checkNull(rs.getString("item_ser"));
			lotNoFrom=checkNull(rs.getString("lot_no__from").trim());
			lotNoTo=checkNull(rs.getString("lot_no__to").trim());
			siteCode=checkNull(rs.getString("site_code"));
			siteCodeSupp=checkNull(rs.getString("site_code__supp"));  			
	     	salesGrp=checkNull(rs.getString("sales_grp"));
							
			System.out.println("lotNoFrom========"+lotNoFrom);
			System.out.println("lotNoTo========"+lotNoTo);
		
			System.out.println("siteCode========"+siteCode);
			System.out.println("siteCodeSupp========"+siteCodeSupp);
			System.out.println("itemSer========"+itemSer);
			System.out.println("salesGrp========"+salesGrp);
		
			if(lotNoFrom!=null && lotNoFrom.trim().length()>0)
			{
				lotNoFrom=lotNoFromCopy;
			}
			
			System.out.println("Value of lotNoFrom=lotNoFromCopy===="+lotNoFrom);
			
			sql1="select count(*)  from item_lot_own where item_code = ? and lot_no__from <= ?" +
					" and lot_no__to >= ? and site_code = ? and item_ser  = ?  and site_code__supp = ?";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1,itemCodeCopy);				
			pstmt1.setString(2,lotNoFrom.trim());
			pstmt1.setString(3,lotNoFrom.trim());
			pstmt1.setString(4,siteCode);
			pstmt1.setString(5,itemSer);
			pstmt1.setString(6,siteCodeSupp);							
			rs1 = pstmt1.executeQuery();
			if (rs1.next())
			{
				cnt = rs1.getInt(1);				
			}			
			System.out.println("@@@cnt========="+cnt);
			pstmt1.close();
			pstmt1 = null;
			rs1.close();
			rs1 = null;
			
			if(cnt>0)
			{	
				sql1="select distinct lot_no__to,lot_no__from from item_lot_own where item_code = ?  and lot_no__from <= ?	" +
						" and lot_no__to >= ?	 and site_code = ?	 and item_ser  = ?" +
						" and site_code__supp = ? ";				
				pstmt1=conn.prepareStatement(sql1);
				pstmt1.setString(1,itemCodeCopy);
				pstmt1.setString(2,lotNoFrom.trim());
				pstmt1.setString(3,lotNoFrom.trim());		
				pstmt1.setString(4,siteCode);
				pstmt1.setString(5,itemSer);	
				pstmt1.setString(6,siteCodeSupp);	
				rs1=pstmt1.executeQuery();
				while(rs1.next())
				{					
					lotNoTo1=rs1.getString("lot_no__to").trim();
					lotNoFrom1=rs1.getString("lot_no__from").trim();							
					System.out.println("Distinct Lot No======"+lotNoFrom1);
					System.out.println("Distinct Lot No To======"+lotNoTo1);					
				
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
			   	System.out.println("Distinct Lot No To>>>======"+lotNoTo1);
			   	System.out.println("Distinct Lot No To>>>======"+lotNoFrom1);
			
				System.out.println("Lot no from>>>>>>>>>>>>>>"+lotNoFrom);
				if (lotNoFrom != null)
				{
					lenlotNoFrom = lotNoFrom.trim().length();
				}
				System.out.println("lenlotNoFrom========"+lenlotNoFrom);
				counter = 1;
				while (counter <= lenlotNoFrom)
				{
					if (checkIsNumber(lotNoFrom))
					{
						
						System.out.println("counter ["+counter+"] lotNoFrom  ["+lotNoFrom+"]");
						if (lotNoFrom != null && lotNoFrom.startsWith("0"))
						{
							System.out.println("If ===========");
							lotNoFrom = lotNoFrom.substring(1);
							System.out.println("After getting mid lotNoFrom :"+lotNoFrom);
							set = set + "0";
							System.out.println("Set=========="+set);
						}
						else
						{
							System.out.println("else ===========");
							oldLen = lotNoFrom.trim().length();
							System.out.println("oldLen=============="+oldLen);
							newLotNoFrom = Integer.parseInt((lotNoFrom == null || lotNoFrom == "") ?"0":lotNoFrom.trim()) - 1; 
							System.out.println("newLotNoFrom=============="+newLotNoFrom);
							newLotLen = String.valueOf(newLotNoFrom).trim().length();
							System.out.println("newLotLen :"+newLotLen+"oldLen  :"+oldLen+"newLotNoFrom :"+newLotNoFrom);
							newLotNoStr = "";
							if (oldLen != newLotLen)
							{
								int cnt1 = oldLen - newLotLen;
								System.out.println("cnt1============"+cnt1);
								for (int i = 0; i< cnt1; i++)
								{
									newLotNoStr = newLotNoStr + "0";
									System.out.println("newLotNoStr Inside For====:"+newLotNoStr);

								}
								System.out.println("newLotNoStr ===:"+newLotNoStr);
							}
						}//end else
					}//end if
					else
					{
						System.out.println("Enter in Else checkIsNumber=========");
						String original = lotNoFrom.substring(0,counter);
						System.out.println("original========="+original);
						set = set + original;
						System.out.println("set in else part==========="+set);
						System.out.println("counter============="+counter);
						//lotNoFrom = lotNoFrom.substring(counter+1);			
						lotNoFrom = lotNoFrom.substring(counter);	
						System.out.println("Lot no from from else==========="+lotNoFrom);
					}
					counter ++;
				}//end while inside
			//
				//lotNoFrom = set + newLotNoStr.trim() + String.valueOf(newLotNoFrom);
				lotNoFromNew = set + newLotNoStr.trim() + String.valueOf(newLotNoFrom);
				System.out.println("lotNoFromNew  :"+lotNoFromNew);
					//for (int i = 0; i < lotNoList.size(); i++) 
					//{
					//	oldLotNo = (String) lotNoList.get(i);
					//	System.out.println("Getting oldLotNo======== "+oldLotNo);

						sql = "UPDATE ITEM_LOT_OWN " + "SET LOT_NO__TO = ? "
								+ "WHERE ITEM_CODE = ? "
								+ "AND LOT_NO__FROM <= ? "
								+ "AND LOT_NO__TO >= ? " + "AND SITE_CODE = ? "
								+ "AND ITEM_SER = ? "
								+ "AND SITE_CODE__SUPP = ? ";
						System.out.println("sql :" + sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lotNoFromNew);
						pstmt.setString(2, itemCodeCopy);
						pstmt.setString(3, lotNoFromCopy);					
						pstmt.setString(4, lotNoFromCopy);
						pstmt.setString(5, siteCode);
						pstmt.setString(6, itemSer);
						pstmt.setString(7, siteCodeSupp);
						updCnt = pstmt.executeUpdate();
						System.out.println(updCnt + " Records Updated");
					//}
			}// end of if
					
			
			sql1=" insert into item_lot_own(site_code,site_code__supp,item_ser,item_code,lot_no__from,lot_no__to,chg_user," +
					"chg_term,chg_date,sales_grp)  values(?,?,?,?,?,?,?,?,?,?)";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1,siteCode);				
			pstmt1.setString(2,siteCodeSupp);
			pstmt1.setString(3,itemSer);
			pstmt1.setString(4,itemCodeCopy);
			pstmt1.setString(5,lotNoFromCopy.trim());
			pstmt1.setString(6,lotNoToCopy.trim());	
			pstmt1.setString(7,chgUser);
			pstmt1.setString(8,chgTerm);
			pstmt1.setTimestamp(9,newsysDate);
			pstmt1.setString(10,salesGrp);											
			pstmt1.executeUpdate();									
			pstmt1.close();
			pstmt1 = null;	
			
		}//end of while
		pstmt.close();
    	rs.close();
    	pstmt = null;
    	rs = null;
		
	} // end of try code 
   	catch(Exception e)
	{
		isError = true;
		e.printStackTrace();
		errString = e.getMessage();
		throw  new ITMException(e);
	}		
	finally
	{
		try
		{
			if(rs != null)rs.close();
			rs = null;
			if(pstmt != null)pstmt.close();
			pstmt = null;				
			if(conn != null)
			{
				if(isError)
				{
					conn.rollback();
					System.out.println("connection rollback.............");
					resultString = itmDBAccessEJB.getErrorString("","PROCFAILED",userId,"",conn);
				}	
				else
				{
					if(connStatus)
					{
						conn.commit();
						System.out.println("commiting connection.............");
					}
					if(errString.equals(""))
					{
						errString = "PROCSUCC";
					}
					resultString = itmDBAccessEJB.getErrorString("",errString,userId,"",conn);
					
				}
				if(conn!=null && connStatus)
				{
					conn.close();
					conn = null;
				}
			}
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
	}	
	System.out.println("returning from     "+resultString);
	return resultString;
	} //end process

private String checkNull(String input)
{
	if(input == null)
	{
		input = "";
	}
	return input.trim();
}



private String isExist(String table, String field, String value, Connection conn) throws SQLException
{
	String sql = "", retStr = "";
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	int cnt = 0;

	sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
	pstmt = conn.prepareStatement(sql);
	pstmt.setString(1, value);
	rs = pstmt.executeQuery();
	if (rs.next())
	{
		cnt = rs.getInt(1);
	}
	rs.close();
	rs = null;
	pstmt.close();
	pstmt = null;
	if (cnt > 0)
	{
		retStr = "TRUE";
	}
	if (cnt == 0)
	{
		retStr = "FALSE";
	}
	System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::[" + cnt + "]");
	return retStr;
}


private boolean checkIsNumber(String checkStr)
{
	char ch = ' ';
	int len = 0;
	len = checkStr.length();
	boolean retBool = true;
	System.out.println("len :"+len);
	for (int i = 0; i < len; i++)
	{
		ch = checkStr.charAt(i);
		System.out.println("Character return===="+ch);
		System.out.println("checkStr [" + checkStr + "] Character [" + ch + "] counter  [" + i + "]");
		if (Character.isLetter(ch))
		{
			retBool = false;
			break;
		}
		else
		{
			retBool = true;
		}
	}//end for
	System.out.println("After break========"+retBool);
	return retBool;
}
}

 