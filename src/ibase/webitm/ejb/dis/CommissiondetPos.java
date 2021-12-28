
package ibase.webitm.ejb.dis;

//import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
@javax.ejb.Stateless
public class CommissiondetPos extends ValidatorEJB implements  CommissiondetPosLocal, CommissiondetPosRemote
{
	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave(String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException
	//public String postSave(String domString, String tranId, String objContext, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		PreparedStatement pstmt = null,pstmt1= null,pstmt2= null;		
		ResultSet rs = null,rs1 = null,rs2 = null;		
		String sql = "",sql1="",sql2="";
		String itemCode = "";

		System.out.println(" Tran id = "+tranId);
		System.out.println("dom Strig = "+domString);
		int noOfParent = 0,i = 0;

		boolean found = false,isError = false;
		java.sql.Timestamp  effDate = null,effDateLast = null;
		java.sql.Timestamp validUpto = null, validUptoLast=null;
		SimpleDateFormat sdf1 = null;
		String effDateStr="",itemSer="",validUptostr="",commTable="",retString="",lineNo="",lineNoLast="";
		String customError = "",mainStr =  "", begPart  = "" ,endPart = "",userId="";
		int cnt=0;
		
		//ConnDriver connDriver = null;
		//Connection conn=null;
		System.out.println("Connection = "+conn);
		try
		{
			//System.out.println("@@@@@@@8");
			conn.setAutoCommit(false);
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			dom = genericUtility.parseString(domString);
			NodeList detail3List = dom.getElementsByTagName("Detail2");
			NodeList detail1List = dom.getElementsByTagName("Detail1");
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			System.out.println("@@@@@ postSave called");
			System.out.println("@@@@@@@@ detail3List [[[[[[[["+detail3List.toString()+"]]]]]]]]]]]]]");
			System.out.println("@@@@@@@@ detail1List [[[[[[[["+detail1List.toString()+"]]]]]]]]]]]]]");

			if(detail3List != null && detail3List.getLength() > 0)
			{
				commTable = genericUtility.getColumnValueFromNode("comm_table", dom.getElementsByTagName("Detail2").item(0));
				//effDateStr = genericUtility.getColumnValueFromNode("eff_date", dom.getElementsByTagName("Detail2").item(0));
				//validUptostr = genericUtility.getColumnValueFromNode("valid_upto", dom.getElementsByTagName("Detail2").item(0));
				//lineNo = genericUtility.getColumnValueFromNode("line_no", dom.getElementsByTagName("Detail2").item(0));
				//itemCode = genericUtility.getColumnValueFromNode("item_code", dom.getElementsByTagName("Detail2").item(0));
				//itemSer = genericUtility.getColumnValueFromNode("item_ser", dom.getElementsByTagName("Detail2").item(0));

				noOfParent = detail3List.getLength();


				sql2 = " select  distinct item_code, item_ser from comm_det where comm_table= ? ";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1,commTable);
				rs2 = pstmt2.executeQuery();
				while(rs2.next())
				{
					itemCode = rs2.getString("item_code");
					itemSer= rs2.getString("item_ser");
					validUptoLast = null;
					lineNoLast =  null;
					sql="select line_no,eff_date,valid_upto FROM" +
							" (select line_no,eff_date,valid_upto from comm_det where comm_table =? and item_code=?" +
							" UNION ALL " +
							"select line_no,eff_date,valid_upto from comm_det where comm_table = ? and item_ser=? " +
							" )  ORDER BY eff_date asc";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,commTable);
					pstmt.setString(2,itemCode);
					pstmt.setString(3,commTable);
					pstmt.setString(4,itemSer);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						//	int cnt =  rs.getInt(1);
						lineNo = rs.getString("line_no"); 
						effDate = rs.getTimestamp("eff_date");
						validUpto = rs.getTimestamp("valid_upto");

						if( validUptoLast != null && ( lineNoLast != null )  )
						{

							System.out.println("EffDate = "+effDate +" valid up last = "+validUptoLast+"       valid Upto="+validUpto);
							//if(! (validUpto.after(validUptoLast))  )
							System.out.println("TEST 1= "+validUptoLast.before(validUpto));
							if(! (validUptoLast.before(validUpto)) && ((int)( (validUpto.getTime() - validUptoLast.getTime()) / (1000 * 60 * 60 * 24))) != 0  )
							{
								conn.rollback();
								customError = "Invalid Effective Date and Valid Upto Date for Line No = "+lineNo+" .";
								retString = itmDBAccessEJB.getErrorString("","INVLDT01","","",conn);
								begPart = retString.substring( 0, retString.indexOf("<trace>") + 7 );
								endPart = retString.substring( retString.indexOf("</trace>"));
								mainStr = begPart + customError + endPart;
								errString = mainStr;
								System.out.println("mainStr-----"+mainStr);
								
								return errString;
							}
							if( effDate.before(validUptoLast) || ((int)( (effDate.getTime() - validUptoLast.getTime()) / (1000 * 60 * 60 * 24))) == 0 )
							{
								Calendar c = Calendar.getInstance();
								c.setTime((effDate));
								c.add(Calendar.DATE, -1);
								validUptoLast = new Timestamp( c.getTimeInMillis());
								System.out.println("@@@@@@@@@@@@ new date into validUpto new : ["+validUptoLast+"]");
								// System.out.println("@@@@@updating:::.["+commTable+"]...["+lineNo+"]....["+validUptoLast+"]....["+ cnt+"]record update successfully.");
								if( effDateLast.after(validUptoLast))   //added cpatil
								{
									errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId,"",conn);
									return errString;
								}
								sql1="UPDATE COMM_DET SET valid_upto= ? WHERE COMM_TABLE= ? and LINE_NO = ? ";
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setTimestamp(1,validUptoLast);
								pstmt1.setString(2,commTable);
								pstmt1.setString(3,lineNoLast);
								cnt = pstmt1.executeUpdate();
								pstmt1.close();
								pstmt1 = null;		
								if ( cnt > 0 )
								{
									System.out.println("@@@@@@update sucess:::.["+commTable+"]...["+lineNoLast+"]....["+validUptoLast+"]....["+ cnt+"]record update successfully.");
								}
							}
							if(effDate.after(validUptoLast) )
							{
								
								int diffInDays = (int)( (effDate.getTime() - validUptoLast.getTime()) / (1000 * 60 * 60 * 24) );
								System.out.println("Diff Days="+diffInDays);
								if(diffInDays > 1)
								{
									Calendar c = Calendar.getInstance();
									c.setTime((validUptoLast));
									c.add(Calendar.DATE, (diffInDays - 1));
									validUptoLast = new Timestamp( c.getTimeInMillis());
									System.out.println("@@@@@@@@@@@@ new date into validUpto new 2: ["+validUptoLast+"]::::effDateLast["+effDateLast+"]");
									if( effDateLast.after(validUptoLast))         //added cpatil  // && !(effDateLast.equals(validUptoLast))
									{
										errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId,"",conn);
										return errString;
									}
									sql1="UPDATE COMM_DET SET valid_upto= ? WHERE COMM_TABLE= ? and LINE_NO = ? ";
									pstmt1 = conn.prepareStatement(sql1);
									pstmt1.setTimestamp(1,validUptoLast);
									pstmt1.setString(2,commTable);
									pstmt1.setString(3,lineNoLast);
									cnt = pstmt1.executeUpdate();
									pstmt1.close();
									pstmt1 = null;		
									if ( cnt > 0 )
									{
										System.out.println("@@@@@@update sucess2::record update successfully.");
									}
								}

							}
						}
						validUptoLast =  validUpto;
						effDateLast = effDate;//added by kunal on 22/02/13
						lineNoLast = lineNo;
						System.out.println("@@@@ validUptoLast"+validUptoLast+"::::lineNoLast"+lineNoLast);
					}  // inside querry

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


				}  //  outside querry	
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
			}
			
			// System.out.println("Called Inside Commistion Post det ===>>>>>>>>>>>");
			// NodeList detail4List = dom.getElementsByTagName("Detail3");
			// changed by Nasruddin khan [19/JUL/16 D16DFOR001] START
			/*if(detail4List != null && detail4List.getLength() > 0)
			{
				validUptoLast = null;
				lineNoLast =  null;
				commTable = genericUtility.getColumnValueFromNode("comm_table", dom.getElementsByTagName("Detail3").item(0));
				String eff_date_dom = genericUtility.getColumnValueFromNode("eff_date", dom.getElementsByTagName("Detail3").item(0));
				String validUpTo_dom = genericUtility.getColumnValueFromNode("valid_upto", dom.getElementsByTagName("Detail3").item(0));
                 
				System.out.println("eff_date_dom ["+eff_date_dom+"]");
				System.out.println("eff_date_dom ["+validUpTo_dom+"]");
				noOfParent = detail3List.getLength();
				
				sql = " select line_no,eff_date,valid_upto FROM COMM_BY_VALUE where comm_table = ?  ORDER BY eff_date asc " ;
				//sql = "select line_no,eff_date,valid_upto FROM (select line_no,eff_date,valid_upto from comm_by_value where comm_table = ? union all select line_no,eff_date,valid_upto from comm_by_value where comm_table = ? )  ORDER BY eff_date asc";
				System.out.println("sql :::::::::"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,commTable);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					lineNo = rs.getString("line_no"); 
					effDate = rs.getTimestamp("eff_date");
					validUpto = rs.getTimestamp("valid_upto");
					
					if( validUptoLast != null && ( lineNoLast != null )  )
					{
						System.out.println("EffDate ::::::::= "+effDate +" valid up last :::::::::::::::= "+validUptoLast+"       valid Upto :::::="+validUpto);
						System.out.println("TEST 1= "+validUptoLast.before(validUpto));
						if(! (validUptoLast.before(validUpto)) && ((int)( (validUpto.getTime() - validUptoLast.getTime()) / (1000 * 60 * 60 * 24))) != 0  )
						{
							conn.rollback();
							customError = "Invalid Effective Date and Valid Upto Date for Line No = "+lineNo+" .";
							retString = itmDBAccessEJB.getErrorString("","INVLDT01","","",conn);
							begPart = retString.substring( 0, retString.indexOf("<trace>") + 7 );
							endPart = retString.substring( retString.indexOf("</trace>"));
							mainStr = begPart + customError + endPart;
							errString = mainStr;
							System.out.println("mainStr-----"+mainStr);
							
							return errString;
						}
						if( effDate.before(validUptoLast) || ((int)( (effDate.getTime() - validUptoLast.getTime()) / (1000 * 60 * 60 * 24))) == 0 )
						{
							Calendar c = Calendar.getInstance();
							c.setTime((effDate));
							c.add(Calendar.DATE, -1);
							validUptoLast = new Timestamp( c.getTimeInMillis());
							System.out.println("@@@@@@@@@@@@ new date into validUpto new : ["+validUptoLast+"]");
							if( effDateLast.after(validUptoLast))   
							{
								errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId);
								return errString;
							}
							sql1="UPDATE COMM_BY_VALUE SET valid_upto= ? WHERE COMM_TABLE= ? and LINE_NO = ? ";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setTimestamp(1,validUptoLast);
							pstmt1.setString(2,commTable);
							pstmt1.setString(3,lineNoLast);
							cnt = pstmt1.executeUpdate();
							pstmt1.close();
							pstmt1 = null;		
							if ( cnt > 0 )
							{
								System.out.println("@@@@@@update sucess:::.["+commTable+"]...["+lineNoLast+"]....["+validUptoLast+"]....["+ cnt+"]record update successfully.");
							}
						}
						if(effDate.after(validUptoLast) )
						{
							
							int diffInDays = (int)( (effDate.getTime() - validUptoLast.getTime()) / (1000 * 60 * 60 * 24) );
							System.out.println("Diff Days="+diffInDays);
							if(diffInDays > 1)
							{
								Calendar c = Calendar.getInstance();
								c.setTime((validUptoLast));
								c.add(Calendar.DATE, (diffInDays - 1));
								validUptoLast = new Timestamp( c.getTimeInMillis());
								System.out.println("@@@@@@@@@@@@ new date into validUpto new 2: ["+validUptoLast+"]::::effDateLast["+effDateLast+"]");
								if( effDateLast.after(validUptoLast))        
								{
									errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId);
									return errString;
								}
								sql1="UPDATE COMM_BY_VALUE SET valid_upto= ? WHERE COMM_TABLE= ? and LINE_NO = ? ";
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setTimestamp(1,validUptoLast);
								pstmt1.setString(2,commTable);
								pstmt1.setString(3,lineNoLast);
								cnt = pstmt1.executeUpdate();
								pstmt1.close();
								pstmt1 = null;		
								if ( cnt > 0 )
								{
									System.out.println("@@@@@@update sucess2::record update successfully.");
								}
							}

						}
					}
					validUptoLast =  validUpto;
					effDateLast = effDate;
					lineNoLast = lineNo;
					System.out.println("@@@@ validUptoLast"+validUptoLast+"::::lineNoLast"+lineNoLast);
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}// changed by Nasruddin khan [19/JUL/16 D16DFOR001] END
*/			
		
		}
		catch(Exception e)
		{
			try
			{
				isError = true;
				if(conn != null)
				{
					//System.out.println("@@@@@@@5");
					conn.rollback();
				}
				if( errString != null && errString.trim().length() >  0 )
				{
					//System.out.println("@@@@@@@6");
					conn.rollback();
				}
				System.out.println("Exception.. "+e.getMessage());
				e.printStackTrace();			
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				throw new ITMException(e1);
			}
		}
		finally
		{
			try
			{		//System.out.println("@@@@@@@1");
				if(isError)
				{
					//System.out.println("@@@@@@@2");
					conn.rollback();
					System.out.println("Transaction rollback... ");
				}
				else if( errString != null && errString.trim().length() >  0 )
				{
					//System.out.println("@@@@@@@3");
					conn.rollback();
					System.out.println("Transaction rollback... ");
				}
				else
				{
					//System.out.println("@@@@@@@4");
					conn.commit();
					System.out.println("@@@@ Transaction commit... ");
				}


			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}	

}


