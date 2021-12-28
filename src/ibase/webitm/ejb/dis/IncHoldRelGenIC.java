/* develop by Ritesh  on 08/05/13
purpose : set default value in inv hold process   */ 

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
@Stateless // added for ejb3
public class IncHoldRelGenIC extends ValidatorEJB implements IncHoldRelGenICLocal,IncHoldRelGenICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String itemChanged(String xmlString,String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ default itemChanged called");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [TrainingEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;
	}
   public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	   {
		System.out.println("@@@@@@@ itemChanged called@@@@@");
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String childNodeName = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo =0;
		int childNodeListLength = 0;
		int ctr=0;
		DistCommon distcommon=null;
		String relAutoConfirm="";
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver=null;
			distcommon=new DistCommon();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");				
			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
			switch(currentFormNo)
			{
			case 1:
			    	parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					valueXmlString.append("<Detail1>");
					childNodeListLength = childNodeList.getLength();
					do
					{   
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						ctr ++;
					}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
	
	
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						Calendar currentDate = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						String sysDate = sdf.format(currentDate.getTime());
						System.out.println("Now the date is :=>  " + sysDate);
						
						relAutoConfirm = distcommon.getDisparams("999999","REL_AUTO_CONF", conn);//Manoj dtd 15/04/2014 Set Auto_confirmed from disparm
						
						if (relAutoConfirm == null|| "NULLFOUND".equalsIgnoreCase(relAutoConfirm))
						{
							relAutoConfirm = "N";
						}
						
						
						
						valueXmlString.append( "<tran_id__from><![CDATA[" ).append( "00").append( "]]></tran_id__from>\r\n" );
						valueXmlString.append( "<tran_id__to><![CDATA[" ).append("ZZ").append( "]]></tran_id__to>\r\n" );
						valueXmlString.append( "<ref_id__from><![CDATA[" ).append( "00").append( "]]></ref_id__from>\r\n" );
						valueXmlString.append( "<ref_id__to><![CDATA[" ).append("ZZ").append( "]]></ref_id__to>\r\n" );
						valueXmlString.append( "<lock_code__from><![CDATA[" ).append( "00").append( "]]></lock_code__from>\r\n" );
						valueXmlString.append( "<lock_code__to><![CDATA[" ).append("ZZ").append( "]]></lock_code__to>\r\n" );
						valueXmlString.append( "<lot_no__from><![CDATA[" ).append( "00").append( "]]></lot_no__from>\r\n" );//Added by Manoj dtd 15/04/2014 to set default value
						valueXmlString.append( "<lot_no__to><![CDATA[" ).append("ZZ").append( "]]></lot_no__to>\r\n" );//Added by Manoj dtd 15/04/2014 to set default value
						valueXmlString.append( "<auto_confirmed><![CDATA[" ).append(relAutoConfirm).append( "]]></auto_confirmed>\r\n" );	
						
						
						
					}
					
					
					valueXmlString.append("</Detail1>");
					
			}
			valueXmlString.append("</Root>");
			System.out.println("valueXmlString @@@@@    "+valueXmlString);
		}// end try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
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
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
			  d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
}
public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
{
	System.out.println("@@@@@@@ Validation called");
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	String errString = "";
	try
	{   
		dom = parseString(xmlString);
		dom1 = parseString(xmlString1);
		dom2 = parseString(xmlString2);
		System.out.println("xmlString--------"+xmlString);
		System.out.println("xmlString1--------"+xmlString1);
		System.out.println("xmlString2--------"+xmlString2);
		errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
	}
	catch(Exception e)
	{
		System.out.println("Exception : [TrainingEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
	}
	return errString;
}
public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
{
	String errString="";
	String tranIdFrom="",tranIdTo="",schRelDateFromstr="", schRelDateTostr="",refIdFrom="",refIdTo="",lockCodeFrom="",lockCodeTo="",lotNoFrom="",lotNoTo="",sql="";
	Timestamp schRelDateFrom=null,schRelDateTo=null;
	String errCode = "";
	String errorType = "";
	ArrayList<String> errList = new ArrayList();
	ArrayList<String> errFields = new ArrayList<String>();StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
	int count = 0;
	String userId="";
	ConnDriver connDriver = new ConnDriver();
	Connection conn = null;
	PreparedStatement pstmt = null,pstmt1=null,pstmt2=null ;
	ResultSet rs = null,rs1=null;
	String getDataSql="";
	int i=7;
	String unconfInvHolddet="",unconfInvReldet="",releaseTranId="",releaseLineNo="";
	String confirmed="";
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	String invHoldId="";
	String invHoldLine="";
	int unconfInvHoldCnt=0;
	int unconfInvRelCnt=0;
	String allowMultipleLot="";// added by priyanka on 19/8/14
	DistCommon distCommon=new DistCommon();// added by priyanka on 19/8/14
	try
	{
		
		System.out.println("@@@@@@@@@@INVHOLDRELGENIC@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		
		userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		
		tranIdFrom = genericUtility.getColumnValue("tran_id__from",dom);
		tranIdTo = genericUtility.getColumnValue("tran_id__to",dom);
		schRelDateFromstr = genericUtility.getColumnValue("sch_rel_date__from",dom);
		schRelDateTostr = genericUtility.getColumnValue("sch_rel_date__to",dom);
		refIdFrom = genericUtility.getColumnValue("ref_id__from",dom);
		refIdTo = genericUtility.getColumnValue("ref_id__to",dom);
		lockCodeFrom = genericUtility.getColumnValue("lock_code__from",dom);    
		lockCodeTo = genericUtility.getColumnValue("lock_code__to",dom);		 
		lotNoFrom = genericUtility.getColumnValue("lot_no__from",dom);
		lotNoTo = genericUtility.getColumnValue("lot_no__to",dom);
		
		System.out.println("schRelDateFromstr : ["+schRelDateFromstr+"]:::schRelDateTostr:["+schRelDateTostr+"]");
		if(schRelDateFromstr != null)
		{
			System.out.println("schRelDateFromstr : ["+schRelDateFromstr);
			schRelDateFrom= Timestamp.valueOf(genericUtility.getValidDateString(schRelDateFromstr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
		}
		if(schRelDateTostr != null)
		{
			System.out.println(" :::schRelDateTostr:["+schRelDateTostr+"]");
			//schRelDateTo= Timestamp.valueOf(genericUtility.getValidDateString(schRelDateTostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");//Commented by Manoj dtd 04/10/2013 to set hrs and minutes also
			schRelDateTo= Timestamp.valueOf(genericUtility.getValidDateString(schRelDateTostr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 23:59:59.0");
		}
		System.out.println("schRelDateFrom : ["+schRelDateFrom+"]:::schRelDateTo:["+schRelDateTo+"]");
		System.out.println("tranIdFrom=>"+tranIdFrom);
		System.out.println("tranIdTo=>"+tranIdTo);
		System.out.println("refIdFrom =>"+refIdFrom);
		System.out.println("refIdTo =>"+refIdTo);
		System.out.println("lockCodeFrom=>"+lockCodeFrom);			  
		System.out.println("lockCodeTo=>"+lockCodeTo);
		
		System.out.println("lotNoFrom=>"+lotNoFrom);			 
		System.out.println("lotNoTo=>"+lotNoTo);				 
		
		if ( tranIdFrom == null || tranIdFrom.trim().length() == 0 )
		{
			System.out.println("tran id from is Null...");
			errCode = "VMTRIDFRNU";
			errList.add(errCode);
			errFields.add("tran_id__from");
		}
		if ( tranIdTo == null || tranIdTo.trim().length() == 0 )
		{
			System.out.println("tran id to is Null...");
			errCode = "VMTRIDTONU";
			errList.add(errCode);
			errFields.add("tran_id__to");
		}
		if ( schRelDateFrom == null )
		{
			System.out.println("schedule release date from is Null...");
			errCode = "VMSCDTFRNU";
			errList.add(errCode);
			errFields.add("sch_rel_date__from");
		}
		else
		{
			if(schRelDateTo != null)
			{
				if(schRelDateFrom.after(schRelDateTo))
				{
					errCode = "SCDTTOINV";
					errList.add(errCode);
					errFields.add("sch_rel_date__to");
				}
			}
		}
		if ( schRelDateTo == null )
		{
			System.out.println("schedule release date to is Null...");
			errCode = "VMSCDTTONU";
			errList.add(errCode);
			errFields.add("sch_rel_date__to");
		}
		else
		{
			if(schRelDateTo != null)
			{
				if(schRelDateTo.before(schRelDateFrom))
				{
					errCode = "SCDTTOINV";
					errList.add(errCode);
					errFields.add("sch_rel_date__to");
				}
			}
		}
		if ( lotNoFrom == null || lotNoFrom.trim().length() == 0 )
		{
			System.out.println("lotNoFrom is Null...");
			errCode = "VMINVLTFR";
			errList.add(errCode);
			errFields.add("lot_no__from");
		}
		if ( lotNoTo == null || lotNoTo.trim().length() == 0 )
		{
			System.out.println("lotNoTo is Null...");
			errCode = "VMINVLTTO";
			errList.add(errCode);
			errFields.add("lot_no__from");
			
		}
		System.out.println("Checking 1111111111");
		
		/// added by priyanka as per manoj sharma instruction on 19/08/14
		// validation for lotNo From and LotNoTo
		//if LotNoFrom and LotNoTo are not equal then it will show error
		allowMultipleLot = checkNull(distCommon.getDisparams("999999", "ALLOW_MULTIPLE_LOT", conn));
		System.out.println("allowMultipleLot="+allowMultipleLot);
		if(allowMultipleLot == null || allowMultipleLot.trim().equalsIgnoreCase("NULLFOUND"))
		{
			allowMultipleLot = "N";
		}
		if(allowMultipleLot.equalsIgnoreCase("N"))
		{
			 if(!lotNoFrom.equalsIgnoreCase(lotNoTo))
			   {
				   System.out.println("lotNoFrom and LotNoTo should be same");
					errCode = "VMLOTFRTO ";
					errList.add(errCode);
					errFields.add("lot_no__from");												
			   }
		}
		
		
		System.out.println("Displaying -----------------");
		System.out.println("refIdFrom["+refIdFrom+"]:::::::refIdTo["+refIdTo+"]");
		//ih.lock_code ,ihd.lot_no,ihd.lot_sl,ihd.remarks  ADDED BY RITESH 
		getDataSql= " select ihd.tran_id,ihd.line_no,ihd.lot_no,ihd.lot_sl," +
					" case when ihd.hold_status is null then 'H' else ihd.hold_status end as hold_status ," +
					" ihd.item_code,ihd.loc_code,ihd.site_code,ih.confirmed " +					
					" from inv_hold_det ihd,inv_hold ih where ( ihd.tran_id = ih.tran_id )" +
					" and ( ihd.tran_id >= ? and ihd.tran_id <= ? ) and ( ih.lock_code >= ? and ih.lock_code <= ? )" +
					" and ( ihd.sch_rel_date between ? and ? ) and ihd.hold_status <> 'R'  "
				   +" and ( ihd.lot_no >= '"+lotNoFrom+"' and ihd.lot_no <= '"+lotNoTo+"' ) "; 
					
		//and ( ihd.lock_code between ? and ? )
		if(refIdFrom != null )	
		{	
			getDataSql = getDataSql + " and ( ih.ref_id >= ? or ih.ref_id is null ) ";
		}
		
		if(refIdTo != null )	
		{	
			getDataSql = getDataSql + " and ( ih.ref_id <= ?  or ih.ref_id is null ) ";
		}
		
		System.out.println("Sql Fired :::::"+getDataSql.trim().length());
		if (getDataSql.trim().length()>0)
		{
			System.out.println("Sql Fired :::::"+getDataSql);					
			pstmt = conn.prepareStatement(getDataSql);
			pstmt.setString(1,tranIdFrom);
			pstmt.setString(2,tranIdTo);
			pstmt.setString(3,lockCodeFrom); 						 //added by Ritesh on 08/05/13
			pstmt.setString(4,lockCodeTo);						    //added by Ritesh on 08/05/13				
			pstmt.setTimestamp(5,schRelDateFrom); 
			pstmt.setTimestamp(6,schRelDateTo);		
			
			if(refIdFrom != null )	
			{
				pstmt.setString(i,refIdFrom);
				i++;
			}
			
			if( refIdTo != null)	
			{
				pstmt.setString(i,refIdTo);
			}
			
			System.out.println("QUERY IS IN PROCESS..........");
			rs = pstmt.executeQuery();
			System.out.println("QUERY  PROCESS FINISED...!!!!!!....");
			
			while(rs.next()) 
			{
				confirmed=rs.getString("confirmed")==null?"N":rs.getString("confirmed");
				System.out.println("");
				if("N".equalsIgnoreCase(confirmed))
				{
					//unconfInvHolddet+=rs.getString("lot_no")+",";
					unconfInvHoldCnt++;
					System.out.println("unConfirmedCnt  : "+unconfInvHoldCnt);
					//changed by mahendra dated 01-Aug-2014
					if(unconfInvHoldCnt <= 5)
					{
					unconfInvHolddet+="TranId :"+rs.getString("tran_id")+" Line No:"+rs.getString("line_no")+" Lot No:"+rs.getString("lot_no")+"\n ";
					}
					if(unconfInvHoldCnt == 6)
					{
						unconfInvHolddet+= "more....";
					}
						
				}
				else
				{
					invHoldId=rs.getString("tran_id");
					invHoldLine=rs.getString("line_no");
					System.out.println("invHoldId ::"+invHoldId);
					System.out.println("invHoldLine ::"+invHoldLine);
					//pstmt1=conn.prepareStatement("select count(1) from inv_hold_rel where tran_id in( select tran_id from inv_hold_rel_det where tran_id__hold=? and line_no__hold=?) and confirmed='N' ");
					sql="select ihr.tran_id,ihrd.line_no from inv_hold_rel ihr ,inv_hold_rel_det ihrd where " +
							" ihr.tran_id=ihrd.tran_id and " +
							" ihr.tran_id in( select tran_id from inv_hold_rel_det where tran_id__hold=? and line_no__hold=?)" +
							"  and line_no__hold=? and ihr.confirmed='N' order by ihr.tran_id,ihrd.line_no";
					
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,invHoldId);
					pstmt1.setString(2,invHoldLine);
					pstmt1.setString(3,invHoldLine);
					rs1=pstmt1.executeQuery();
					while(rs1.next())
					{
						
						     releaseTranId=rs1.getString(1);
						     releaseLineNo=rs1.getString(2);
						     System.out.println("releaseTranId  ::"+releaseTranId);
						     System.out.println("releaseLineNo  ::"+releaseLineNo);
							//changed by mahendra dated 1-Aug-2014
							unconfInvRelCnt++;
							System.out.println("unconfInvRelCnt!!!!!!!  "+unconfInvRelCnt);
							if(unconfInvRelCnt <= 5)
							{
								//unconfInvReldet+= "Tran Id :"+rs.getString("tran_id")+"  Line No :"+rs.getString("line_no")+"  Lot No:"+rs.getString("lot_no")+"\n ";
								unconfInvReldet+= "Tran Id :"+releaseTranId +"  Line No :"+releaseLineNo+"  Lot No:"+rs.getString("lot_no")+"\n ";
							}
							if(unconfInvRelCnt == 6)
							{
								unconfInvReldet+= "more....";
							}
							
						
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
					
				}
				
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			System.out.println("unconfInvHolddet-------"+unconfInvHolddet);
			System.out.println("unconfInvReldet-------"+unconfInvReldet);
			if(unconfInvHolddet.trim().length()>0)
			{
				errCode = "VMUNCONFHD";
				errList.add(errCode);
				errFields.add("lot_no__from");
			}
			if(unconfInvReldet.trim().length()>0)
			{
				errCode = "VMUNCONFRL";
				errList.add(errCode);
				errFields.add("lot_no__from");
			}
			
		}
		
		
		
		
		int errListSize = errList.size();
		System.out.println("errListSize : "+errListSize);
		count = 0;
		String errFldName = null;
		if(errList != null && errListSize > 0)
		{
			for(count = 0; count < errListSize; count ++)
			{
				errCode = errList.get((int) count);
				errFldName = errFields.get((int) count);
				System.out.println("errCode .........." + errCode);
				//errString = getErrorString(errFldName, errCode, userId);
				System.out.println("errString-----"+errString);
				errString=itmDBAccessEJB.getErrorString("",errCode,userId,"",conn);
				errorType =  errorType(conn , errCode);
				if(errString.length() > 0)
				{
					String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
					if(errCode.equalsIgnoreCase("VMUNCONFHD"))
					{
						bifurErrString = bifurErrString +"<trace>"+unconfInvHolddet.substring(0,unconfInvHolddet.length()-1)+"</trace>"+errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						
					} 
					else if(errCode.equalsIgnoreCase("VMUNCONFRL"))
					{
					
						bifurErrString = bifurErrString +"<trace> "+unconfInvReldet.substring(0,unconfInvReldet.length()-1)+"</trace>"+errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						
					}
					else
					{
						bifurErrString = bifurErrString +errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
					}
					errStringXml.append(bifurErrString);
					errString = "";
				}
				if(errorType.equalsIgnoreCase("E"))
				{
					break;
				}
			}
			errList.clear();
			errList = null;
			errFields.clear();
			errFields = null;
			errStringXml.append("</Errors> </Root> \r\n");
			System.out.println("errStringXml-----"+errStringXml);
		}
		else
		{
			errStringXml = new StringBuffer("");
		}	
		
	}catch(Exception e)
	{
		e.printStackTrace();
		throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
	}
	finally
	{
		try {
			conn.close();
			conn=null;
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		
	}
	errString = errStringXml.toString();
	return errString;
}

private String checkNull(String str)
{
	if(str == null)
	{
		return "";
	}
	else
	{
		return str ;
	}

}
private String errorType(Connection conn , String errorCode) throws ITMException
{
	String msgType = "";
	PreparedStatement pstmt = null ; 
	ResultSet rs = null;
	try
	{                        
		String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
		pstmt = conn.prepareStatement(sql);                        
		pstmt.setString(1,errorCode);                        
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			msgType = rs.getString("MSG_TYPE");
		}                        
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
		throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
	}                
	finally
	{
		try
		{
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
			e.printStackTrace();
		}
	}                
	return msgType;
}


}
