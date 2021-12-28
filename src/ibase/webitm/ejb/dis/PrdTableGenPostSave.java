package ibase.webitm.ejb.dis;
import org.w3c.dom.*;

import ibase.utility.CommonConstants;
//import ibase.utility.GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import ibase.webitm.ejb.ITMDBAccessEJB;
import javax.ejb.Stateless;

@Stateless
public class PrdTableGenPostSave extends ValidatorEJB implements PrdTableGenPostSaveLocal,PrdTableGenPostSaveRemote {

	
	//E12GenericUtility genericUtility = new E12GenericUtility();
	
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println("------------ postSave method called-111111111----------------PrdTableGenPostSave : ");
		System.out.println("tranId111--->>["+tranId+"]");
		System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="";
		
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);				
				errString = postSave(dom,tranId,xtraParams,conn);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : PrdTableGenPostSave.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}	
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("in PrdTableGenPostSave tran_id---->>["+tranId+"]");
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		SimpleDateFormat sdf =null;
		Timestamp currDate = null,fromDate=null,tooDate=null,entryStartDtTimestmp=null,entryEndDtTimestmp=null;
		String sql="",errorString="";
	   String prdCode="",prdTblNo="",frDate="",toDate="",prdClose="";
	   String entryStartDt="",entryEndDt="";
	   String chgUser="",chgTerm="";
	   String errString = "";
	   String selectedValue = "",status="",isChanged="";
		int cnt=0,updateCount=0,detCnt=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		boolean value=false;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		boolean isSelect=false;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
						
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			currDate = new Timestamp(System.currentTimeMillis());
			System.out.println("TimeStamp>>>>>>>>>>"+currDate);
			
			
			NodeList hdrDommList = dom.getElementsByTagName("Detail2");		
			System.out.println("hdrDommList==="+hdrDommList);
			System.out.println("len===["+hdrDommList.getLength()+"]");
			if(hdrDommList.getLength()==0  )
			{
				
				System.out.println(">No Data Selected"+isSelect);
				errString = itmDBAccessEJB.getErrorString("","VTNODATA","","",conn);
				return errString;
			}
			
			for (int dtlCtr = 0; dtlCtr < hdrDommList.getLength(); dtlCtr++)
			{
				
				Node detailListNode = hdrDommList.item(dtlCtr);
				NodeList detail2List= detailListNode.getChildNodes(); 
				//	System.out.println("@@@@@@@@@node name[" + detailListNode.getNodeName()+"]");
				System.out.println("detailListNode===="+detailListNode);
				if("Detail2".equalsIgnoreCase(detailListNode.getNodeName()))
				{
					System.out.println("detail2List===="+detail2List);
//					System.out.println("@@@@inside detail4----------------");
					for (int cntr = 0; cntr < detail2List.getLength(); cntr++) 
					{
						Node	detail2Node = detail2List.item(cntr); 
						System.out.println("detail2Node===="+detail2Node);
						
						if(detail2Node != null &&  detail2Node.getNodeName().equalsIgnoreCase("attribute"))
						{
							System.out.println("Check for selected Value######");
							
							selectedValue = detail2Node.getAttributes().getNamedItem("selected").getNodeValue();
							System.out.println("selectedValue=========="+selectedValue);
							
							System.out.println("Check for STATUS Value######");
							status = detail2Node.getAttributes().getNamedItem("status").getNodeValue();
							System.out.println("status=========="+status);
							
							System.out.println("Check for IS_CHANGE Value######");
							isChanged = detail2Node.getAttributes().getNamedItem("IS_CHANGE").getNodeValue();
							System.out.println("isChanged=========="+isChanged);
							
						}
						
					
						
						if("prd_code".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								prdCode =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								prdCode= "";
							}
						}
						if("prd_tblno".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								prdTblNo =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								prdTblNo= "";
							}
						}
						
						if("fr_date".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								frDate =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								frDate= "";
							}
						}
						if("to_date".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								toDate =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								toDate= "";
							}
						}
						if("prd_closed".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								prdClose =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								prdClose= "";
							}
						}
						if("entry_start_dt".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								entryStartDt =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								entryStartDt= "";
							}
						}
						if("entry_end_dt".equalsIgnoreCase( detail2Node.getNodeName()))
						{
							if( detail2List.item(cntr).getFirstChild() != null)
							{
								entryEndDt =  detail2List.item(cntr).getFirstChild().getNodeValue();
							}
							else
							{
								entryEndDt= "";
							}
						}
						
					}
				
					
					System.out.println("@@@@ctr["+dtlCtr+"]Period code["+prdCode+"]-Period table Code["+prdTblNo+"]-from date["+frDate+"]To Date["+toDate+"]period closed["+prdClose+"]@@@");
					
					
					if(frDate!=null && frDate.trim().length()>0 )
					{
						fromDate = Timestamp.valueOf(genericUtility.getValidDateString(frDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					     System.out.println("Date to be set as fromDate@@@@@@@==="+fromDate);
					}
					 
					if(toDate!=null && toDate.trim().length()>0 )
					{
						tooDate = Timestamp.valueOf(genericUtility.getValidDateString(toDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					     System.out.println("Date to be set as tooDate @@@@@@@==="+tooDate);
					}
				     if(entryStartDt != null)
				     {
				    	 entryStartDtTimestmp = Timestamp.valueOf(genericUtility.getValidDateString(entryStartDt, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				     }
				     if(entryStartDt != null)
				     {
				    	 entryEndDtTimestmp = Timestamp.valueOf(genericUtility.getValidDateString(entryEndDt, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				     }
					System.out.println("isSelect============"+isSelect);
					
					System.out.println("status..1 "+status);
					
					if("N".equalsIgnoreCase(status)|| "O".equalsIgnoreCase(status))
					{
						
					System.out.println("selected Value for record===="+selectedValue);
					
					sql="select count(1) from period_tbl where prd_code= ? and prd_tblno= ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, prdCode);
					pstmt.setString(2, prdTblNo);
					rs=pstmt.executeQuery();
					if(rs.next())
					{						
						cnt = rs.getInt(1);					
						System.out.println("CNT==="+cnt);	
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(cnt==0)
					{
						System.out.println("New Exist@@@@@@@");
						sql="Insert into period_tbl (PRD_CODE,PRD_TBLNO,FR_DATE,TO_DATE,PRD_CLOSED,CHG_DATE,CHG_USER,CHG_TERM,ADD_DATE,ADD_USER,ADD_TERM,ENTRY_START_DT,ENTRY_END_DT ) " +
								"values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
						System.out.println("header sql :"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,prdCode);
						pstmt.setString(2,prdTblNo);
						pstmt.setTimestamp(3,fromDate);
						pstmt.setTimestamp(4,tooDate);
						pstmt.setString(5,prdClose);
						pstmt.setTimestamp(6,currDate);
						pstmt.setString(7,chgUser);
						pstmt.setString(8,chgTerm);	
						pstmt.setTimestamp(9,currDate);
						pstmt.setString(10,chgUser);
						pstmt.setString(11,chgTerm);
						pstmt.setTimestamp(12,entryStartDtTimestmp);//Added by chandra shekar on 15-feb-2016
						pstmt.setTimestamp(13,entryEndDtTimestmp);//Added by chandra shekar on 15-feb-2016
						detCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;	
						
						System.out.println("Insert Count==="+detCnt);
					}
					else
					{
						System.out.println("Record Exist@@@@@@@");
						sql = "update period_tbl set FR_DATE= ? ,TO_DATE=?, PRD_CLOSED=?,CHG_DATE=?,CHG_USER=?,CHG_TERM=?, " +
								" ENTRY_START_DT=?,ENTRY_END_DT=?  where prd_code= ? and prd_tblno= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1,fromDate);
						pstmt.setTimestamp(2,tooDate);
						pstmt.setString(3,prdClose);
						pstmt.setTimestamp(4,currDate);
						pstmt.setString(5,chgUser);
						pstmt.setString(6,chgTerm);	
						pstmt.setTimestamp(7,entryStartDtTimestmp);//Added by chandra shekar on 15-feb-2016
						pstmt.setTimestamp(8,entryEndDtTimestmp);//Added by chandra shekar on 15-feb-2016
						pstmt.setString(9,prdCode);
						pstmt.setString(10,prdTblNo);
						updateCount = pstmt.executeUpdate();
						System.out.println("no of row update: "+updateCount);
						
						pstmt.close();
						pstmt = null;
					}
					
					
					if(detCnt>0 ||updateCount>0)
					{
						System.out.println(">>The selected transaction is confirmed");
						isSelect=true;
						errString="";
						//errString = itmDBAccessEJB.getErrorString("","VTCONSUCF","","",conn);
					}
					
				}//end
					
					else if(!isSelect   )
					{
						
						System.out.println(">No Data Selected"+isSelect);
						errString = itmDBAccessEJB.getErrorString("","VTNODATA","","",conn);
					}
				   				
				}
								
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println(">>>>>In finally errString:"+errString);
				
				if(errString == null || errString.trim().length() == 0)
				{
					conn.commit();
				}
				else
				{
					conn.rollback();
				}
				
				
			/*	if(errString != null && errString.trim().length() > 0)
				{
					if(errString.indexOf("VTCONSUCF") > -1)
					{
						conn.commit();
						System.out.println("Commit Completed");
					}
					else
					{
						conn.rollback();
					}
				}*/
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
				//conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	
	
	private String checkNull( String input )
	{
		if (input == null )
		{
			input = "";
		}
		return input;
	}//end of
}
