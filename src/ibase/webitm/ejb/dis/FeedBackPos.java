package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import ibase.webitm.utility.GenericUtility;
import java.sql.*;
import javax.ejb.*;
import ibase.webitm.ejb.*;
import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.util.Calendar;
import java.util.Date;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.text.SimpleDateFormat;
import java.lang.String;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import java.util.*;
import java.text.*;
import java.io.*;
import org.w3c.dom.*; 
import javax.xml.parsers.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import javax.naming.InitialContext;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import java.io.File;
import ibase.system.config.ConnDriver;
//import org.apache.axis.Constants;
//import org.apache.axis.client.Call;
//import org.apache.axis.client.Service;
//import org.apache.axis.encoding.XMLType;
//import java.net.URL;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class FeedBackPos extends ValidatorEJB implements FeedBackPosLocal,FeedBackPosRemote //SessionBean
{
	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
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
    public String postSave() throws RemoteException,ITMException
	{
		return "";
	} 
    public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String returnVal = "";
		Document dom = null;
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = parseString(xmlString1);
				returnVal = feedBackSingle(dom , editFlag, conn );
			}		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return returnVal;
	}
	private String feedBackSingle(Document dom , String editFlag, Connection conn ) throws RemoteException,ITMException
	{
		String retString = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();		
		Node currDetail = null;
		String childNodeName = null;
		Node childNode = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		int childNodeListLength;
		PreparedStatement pstmt = null;
		int ctr = 0;
		String sql = "";
		String despid = null;
		String returnString = null;
		ResultSet rs = null;
		String userId ="";
		String update_flag = null;
		String errorId = null;
		String errorType = null;
		String errorDescr = null;		
		String requestId = "",actionCode="",actionStatus="",currDate="",preActCode="";	
		double serialNo= 0 ,count = 0,preSerial = 0,feedbkSteps = 0;			
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			conn.setAutoCommit(false);
			SimpleDateFormat sdf = null;
			sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			currDate = sdf.format(currDateTs).toString();
			if("E".equalsIgnoreCase(editFlag)|| "A".equalsIgnoreCase(editFlag))
			{
				requestId = genericUtility.getColumnValue("req_id",dom);
				actionCode= genericUtility.getColumnValue("action_code",dom);
				actionStatus = genericUtility.getColumnValue("action_status",dom);				
				sql="select serial_no, feedbk_steps "
					+"	from feedbk_status where status_code = ? "	;					
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,actionCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
				  serialNo = rs.getDouble("serial_no");
				  feedbkSteps = rs.getDouble("feedbk_steps");
				}					
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql="select count(1)  from feedbk_single "
					+"	where req_id = ? ";					
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,requestId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
				  count = rs.getInt(1);
				  
				}					
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(count>1)
				{
					sql="select o.action_code, s.serial_no  "
						+"	from feedbk_single o, feedbk_status s "
						+"	where o.action_code = s.status_code "
						+"	and o.req_id = ? "
						+"	and s.serial_no =( "
						+"	select max(b.serial_no) from feedbk_single a, feedbk_status b "
						+"	where a.action_code = b.status_code "
						+"	and a.req_id = ? ) "
						+"	and rownum = 1 " ;					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,requestId);
					pstmt.setString(2,requestId);					
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						preActCode = rs.getString(1)==null ? "":rs.getString(1);
						preSerial = rs.getDouble(2);
					}					
					else
					{
						 preActCode="";
						 preSerial = 0; 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(!("C".equalsIgnoreCase(preActCode.substring(0,1))) &&  !("V".equalsIgnoreCase(preActCode.substring(0,1))) &&  preSerial <= serialNo	 ) 
					{
						if(feedbkSteps==2)
						{
							if("P".equalsIgnoreCase(actionStatus))
							{
								ctr=1;
							}
							else
							{
								ctr=2;
							}
							actionCode = actionCode.trim() + new Integer(ctr).toString() ;
						}
						sql="update ser_request "
							+"	set comp_stat = ? , "
							+"	comp_stat_dt = ?  "
							+"	where req_id = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,actionCode);
							pstmt.setTimestamp(2,currDateTs);
							pstmt.setString(3,requestId);
							pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							conn.commit();
					}				
				}			
			}// end of IF 	
		}
		catch (Exception e)
		{
			try
			{
				conn.rollback();
				e.printStackTrace();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		finally
		{
			try
			{
				if ( rs != null)
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}				
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
					       
		}		
		return "" ;
		
	}
	private Timestamp getCurrdateAppFormat() throws ITMException
    {
        String s = "";	
		 Timestamp timestamp = null;		
       // GenericUtility genericUtility = GenericUtility.getInstance();
		 E12GenericUtility genericUtility= new  E12GenericUtility();
        try
        {
            java.util.Date date = null;
            timestamp = new Timestamp(System.currentTimeMillis());
            
            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		}
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
            throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
        }
        return timestamp;
    }
			
}// end of feedbackPosEJB class 
