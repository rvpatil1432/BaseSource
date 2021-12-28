/*********
 * Created By : Vishakha
 * Date : 06/07/2015
 * Request ID : D15CSUN014 
 * For Recalculation of Due date due to delay.
 */
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import java.sql.Timestamp;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class POrderMilestone extends ValidatorEJB {
	
	E12GenericUtility genericUtility = new E12GenericUtility();
	UtilMethods utilmethod = new UtilMethods();
	public String recalcDueDate(String tranId,String xmlDataAll,Connection conn) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ Recalculating Due Date ****** for tran_id **"+tranId);
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String sql = "";
		int delayDays=0,count = 0,count1 = 0;
		Timestamp parsedReschDate = null;
		int lineNo = 0;
		String complDate="";
		String dueDateOrgStr = "",taskCode = "",taskCodeCurr = "";
		Document dom = null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null;		
		ResultSet rs=null,rs2=null;
		int ctr=0;		
		int childNodeListLength;
		String childNodeName = null,sqlMax = "",tranIdPending = "";
		Timestamp dueDateHeader = null,reschDate = null;
		Timestamp complDateNew = null,dueDateOrg = null;
		String retString = "SUCCESS";
		String sqlResch ="",dateFormat = "";
	   System.out.println("xmlString DOM-->>["+xmlDataAll+"]");
		try
		{	
			System.out.println("recalcDueDate called................");
			dom = parseString(xmlDataAll);// read xmldataAll
	
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("Child name --->> "+childNodeName);	
					
					if(childNodeName.equalsIgnoreCase("compl_date")) 
					{
						complDate = checkNull(genericUtility.getColumnValue("compl_date",dom));
						System.out.println("complDate--->["+complDate+"]");
						
					}
					if(childNodeName.equalsIgnoreCase("task_code")) 
					{
						taskCodeCurr = checkNull(genericUtility.getColumnValue("task_code",dom));
						System.out.println("taskCode--->["+taskCodeCurr+"]");
						
					}
					if(childNodeName.equalsIgnoreCase("due_date")) 
					{
						dueDateOrgStr = checkNull(genericUtility.getColumnValue("due_date",dom));
						System.out.println("dueDateOrgStr--->["+dueDateOrgStr+"]");
						
					}
					if(childNodeName.equalsIgnoreCase("date_format")) 
					{
						dateFormat = checkNull(genericUtility.getColumnValue("date_format",dom));
						System.out.println("dateFormat--->["+dateFormat+"]");
						
					}
					
				}
			
				
			/*-----------changed dated 25/07/2015------------------*/
				
				sql ="select due_date from pur_milstn where tran_id=?";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1, tranId);
				rs2 = pstmt2.executeQuery();
				if(rs2.next())
				{
					dueDateOrg=rs2.getTimestamp("due_date");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				System.out.println("dueDateOrg : "+dueDateOrg);
				
				
				
			/*-------------------------------------------------------------------*/	
				
			if(complDate.length() > 0)//completion date is not null
			{
			 complDateNew =  Timestamp.valueOf(genericUtility.getValidDateString(complDate, dateFormat,genericUtility.getDBDateFormat()) + " 00:00:00.0");
		
			 System.out.println("completionDate ====parsed into sql format:::::"+complDateNew);
			//}
			/*if(dueDateOrgStr.length() > 0)
			{
				dueDateOrg = Timestamp.valueOf(genericUtility.getValidDateString(dueDateOrgStr,dateFormat,genericUtility.getDBDateFormat()) + " 00:00:00.0");
			
				System.out.println("Due Date Original ====parsed into sql format:::==="+dueDateOrg);
			}*/
			if(dueDateOrg.before(complDateNew))//if completion date is greater than  due date original
			{
				System.out.println("days-----"+delayDays);
				delayDays =  (int)utilmethod.DaysAfter(dueDateOrg, complDateNew); //(int)countDaysBetween(dueDateOrg,complDateNew);
				System.out.println("delayed days-----"+delayDays);
				//added the delayed days to the date to get the next date
				parsedReschDate = utilmethod.RelativeDate(dueDateOrg, delayDays);//addDays(dueDateOrg, delayDays);
			
				 System.out.println("completionDate ====parsed into sql format:::::"+parsedReschDate);
				
					
				 /**selecting pending tasks from header table **inserting records after rescheduling due dates in detail table***/
				 sqlResch = "Insert into pur_milstn_resch (TRAN_ID,LINE_NO,DELAY_DAYS,TASK_CODE__DELAY,DUE_DATE_ORG,DUE_DATE_RESCH) values (?,?,?,?,?,?)";
					
				 sql = "select task_code,due_date,tran_id from pur_milstn where purc_order in(select purc_order from pur_milstn  where tran_id = ?) and wf_status <> 'C' " +
						   " and tran_type <> 'D' " + // added by cpatil for at the time debit note creation it should not be run.
						   " and tran_id <> ? ";   // added by cpatil on 23/07/15
				 
				 pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					pstmt.setString(2,tranId);
					rs = pstmt.executeQuery();
					while ( rs.next() )
					{
						taskCode = rs.getString("task_code");
						dueDateHeader = rs.getTimestamp("due_date");
						tranIdPending =  rs.getString("tran_id");//tran id of pending records
						 //added code for finding the max line no of detail
					    sqlMax = "select max(line_no) as lineNoTot from pur_milstn_resch  where tran_id = ?";
						pstmt2 = conn.prepareStatement(sqlMax);
						pstmt2.setString(1,tranIdPending);
						rs2 = pstmt2.executeQuery();
						if ( rs2.next() )
						{
						lineNo = rs2.getInt("lineNoTot");
						}
						rs2.close();
						rs2=null;
						pstmt2.close();
						pstmt2=null;
						
						System.out.println("Line NO :::initial value:::"+lineNo);
						lineNo = lineNo+1;
						System.out.println("Line NO ::plus one::::::::"+(lineNo + 1));
					
					
						System.out.println("Inserting into detail table for Task Codes in Header Tables which are pending");
						
						System.out.println("delayed days-----"+delayDays);
					//added the delayed days to the date to get the next date
					    reschDate = utilmethod.RelativeDate(dueDateHeader, delayDays);//addDays(dueDateHeader, delayDays);
						System.out.println("getdays==into detail=============="+reschDate);
						
						
						pstmt1 = conn.prepareStatement(sqlResch);
						pstmt1.setString(1,tranIdPending);
						pstmt1.setInt(2,lineNo);
						pstmt1.setInt(3,delayDays);
						pstmt1.setString(4,taskCodeCurr);
						pstmt1.setTimestamp(5,dueDateOrg);
						pstmt1.setTimestamp(6,parsedReschDate);
						   count = pstmt1.executeUpdate();
							pstmt1.close(); 
							pstmt1 = null;  
							System.out.println("NO. OF RECORDS INSERTED==== "+count);
					
							
							//*****Updating the due date for the ongoing task in header table***//*
							sql = "update pur_milstn set due_date = ? where tran_id = ?";
							pstmt1 = conn.prepareStatement(sql);
						
							pstmt1.setTimestamp(1,reschDate);
							pstmt1.setString(2,tranIdPending);
						
						    count1 = pstmt1.executeUpdate();
							pstmt1.close(); 
							pstmt1 = null;  
							System.out.println("NO. OF RECORDS updated==== "+count1);
							if(count1 > 0)
							{
								conn.commit();
							}
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				 
				 
				
				
			}
		}
			
			} //end try
		
			catch (SQLException se) {
				retString = se.toString();
				System.out.println("SQLException ::"+se);
				se.printStackTrace();
	            throw new ITMException(se);
			}
			catch(Exception e)
			{
				retString = e.toString();
				System.out.println("Exception ::"+e);
				e.printStackTrace();
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
					if(pstmt1 != null)
					{
						pstmt1.close();
						pstmt1 = null;
					}	
					if(pstmt2 != null)
					{
						pstmt2.close();
						pstmt2 = null;
					}	
				/*	if(conn!=null)
					{
						conn.close();
						conn = null;
					}
					if(connDriver!=null){
						connDriver = null;
					}	*/
					if(rs !=null)
					{
						rs.close();
						rs=null;
					}				
				}
				catch(Exception d)
				{
				  d.printStackTrace();
				}
			}

	return retString ;
	

	}
	
	/*-------------added by mahendra dated 09/07/2015---------------*/
	
	
	//added completed activity in details screen with reschedule due date
	public String insertDetMstnSch(String purcOrder,String tranIdCurr,Connection conn) throws ITMException
	{
		String resultString="",sql="",taskDelay="",sqlMax="";
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs2=null;
		int lineNo=0,delayDays=0,count=0;
		Timestamp dueDateOrg = null,compDateOrg=null,reschDate=null;
		
		
		try
		{
			System.out.println("insertDetMstnSch called.......");
			
			sql = "select task_code,due_date,compl_date,LINE_NO__ORD from pur_milstn where purc_order  = ?  and wf_status='C' and tran_type='P' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,purcOrder);
			rs = pstmt.executeQuery();
			while ( rs.next() )
			{
				 System.out.println("count for details inserted record :"+lineNo);
				
				sqlMax = "select max(line_no) as lineNoTot from pur_milstn_resch  where tran_id = ?";
				pstmt2 = conn.prepareStatement(sqlMax);
				pstmt2.setString(1,tranIdCurr);
				rs2 = pstmt2.executeQuery();
				if ( rs2.next() )
				{
				lineNo = rs2.getInt("lineNoTot");
				}
				rs2.close();
				rs2=null;
				pstmt2.close();
				pstmt2=null;
				
				
				lineNo = lineNo + 1;
				
				
			    System.out.println("lineNo :"+lineNo);
			   
				dueDateOrg=rs.getTimestamp("due_date");
				compDateOrg=rs.getTimestamp("compl_date");
				taskDelay=rs.getString("task_code");
				System.out.println("dueDateOrg :"+dueDateOrg);
				System.out.println("compDateOrg :"+compDateOrg);
				System.out.println("taskDelay :"+taskDelay);
				
				delayDays =  (int)utilmethod.DaysAfter(dueDateOrg, compDateOrg); //(int)countDaysBetween(dueDateOrg,complDateNew);
				System.out.println("delayDays :"+delayDays);
				
				if(delayDays > 0)
				{
				
					reschDate = utilmethod.RelativeDate(dueDateOrg, delayDays);
					System.out.println("reschDate :"+reschDate);
				
				
					String sqlResch = "Insert into pur_milstn_resch (TRAN_ID,LINE_NO,DELAY_DAYS,TASK_CODE__DELAY,DUE_DATE_ORG,DUE_DATE_RESCH) values (?,?,?,?,?,?)";
					pstmt1 = conn.prepareStatement(sqlResch);
					pstmt1.setString(1,tranIdCurr);
					pstmt1.setInt(2,lineNo);
					pstmt1.setInt(3,delayDays);
					pstmt1.setString(4,taskDelay);
					pstmt1.setTimestamp(5,dueDateOrg);
					pstmt1.setTimestamp(6,reschDate);
					count = pstmt1.executeUpdate();
					pstmt1.close(); 
					pstmt1 = null; 
					
					System.out.println("count @@@@@@"+count);
					if(count > 0)
					{
						conn.commit();
					}
				
				}
			
			
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			resultString="Success";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resultString="failed";
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		
		return resultString;
		
	}
	
	
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	
	
	public String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input.trim();
	}
	
}
/*	public  long countDaysBetween(Timestamp complDateNew, Timestamp dueDateOrg)
	{
			   
			    //reset all hours mins and secs to zero on start date
			    Calendar startCal = GregorianCalendar.getInstance();
			    startCal.setTime(complDateNew);
			    startCal.set(Calendar.HOUR_OF_DAY, 0);
			    startCal.set(Calendar.MINUTE, 0);
			    startCal.set(Calendar.SECOND, 0);
			    long startTime = startCal.getTimeInMillis();

			    //reset all hours mins and secs to zero on end date
			    Calendar endCal = GregorianCalendar.getInstance();
			    endCal.setTime(dueDateOrg);
			    endCal.set(Calendar.HOUR_OF_DAY, 0);
			    endCal.set(Calendar.MINUTE, 0);
			    endCal.set(Calendar.SECOND, 0);
			    long endTime = endCal.getTimeInMillis();

			    return (endTime - startTime)/(1000 * 60 * 60 * 24) ;
	}
	*/
	/*public  Timestamp addDays(Timestamp date, int days) {

		Timestamp parsedDate = null;
		
		Calendar c = Calendar.getInstance();
		c.setTime((date));
		c.add(Calendar.DATE, days);
		parsedDate = new Timestamp( c.getTimeInMillis());
	    return parsedDate;
	   
	}*/



