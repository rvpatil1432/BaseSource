package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.text.*; 
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import javax.ejb.Stateless; // added for ejb3


//public class RepldlvEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class Repldlv extends ValidatorEJB implements RepldlvLocal, RepldlvRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
/*
	public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Enter in to the the Repldlv........................");
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
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		System.out.println("Validation start ..............................");
		try
		{
			System.out.println("xmlString................"+xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Repl_dlv_schEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		String errCode = "";
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		String sql = "";
		String userId = ""; 
		String loginSite = "";
		String siteCode = "";
		String currCode = "";
		String itemSer = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = ""; 
		int ctr,currentFormNo=0;
		int childNodeListLength;
		double sumReplperc = 0.0;
		int save = 0;
		NodeList parentNodeList1 = null;
		NodeList childNodeList1 = null;
		int parentNodeListLength1 = 0 ;
		int childNodeListLength1 = 0 ;
		Node parentNode1 = null;
		Node childNode1 = null;
		String childNodeName1 = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); 
		try
		{
			conn = getConnection(); 
			stmt = conn.createStatement();
			userId = getValueFromXTRA_PARAMS(xtraParams,"userId");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			//GenericUtility genericUtility = GenericUtility.getInstance(); 
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			parentNodeList1 = dom2.getElementsByTagName("Detail2");
			parentNodeListLength1 = parentNodeList1.getLength(); 
			System.out.println("parentNodeListLength1------------------->"+parentNodeListLength1);
			for (int selectedRow = 0; selectedRow < parentNodeListLength1 - 1; selectedRow++)
			{
				
				parentNode1 = parentNodeList1.item(selectedRow);
				childNodeList1 = parentNode1.getChildNodes();
				childNodeListLength1 = childNodeList1.getLength();
				System.out.println("childNodeListLength1---->>> "+ childNodeListLength1);
				for (int childRow = 0; childRow < childNodeListLength1; childRow++)
				{
					childNode1 = childNodeList1.item(childRow);
					childNodeName1 = childNode1.getNodeName();
					if (childNode1.getFirstChild() != null)
					{
						if(childNodeName1.trim().equals("repl_perc"))
						{
							String s =childNode1.getFirstChild().getNodeValue();
							sumReplperc  =sumReplperc + ((Double.valueOf(s)).doubleValue());
						}
					}
				}
			}
			System.out.println("parentNodeListLength1 val"+parentNodeListLength1);
			switch(currentFormNo)
			{
				case 2:
				String repldlv = "";
				System.out.println(" sumReplperc................."+sumReplperc);
				System.out.println("enter case2..................");
				//	***************************************
				if(parentNodeListLength1 >6)
				{
					System.out.println("enter val");
					errCode = "UTAMR";
					errString = getErrorString("",errCode,userId);
					save = 1;
					break;
				}
			/*	if(sumReplperc == 100)
				{
					System.out.println("enter val");
					errCode = "UTAMR";
					errString = getErrorString("",errCode,"");
					save = 1;
					break;
				}*/
				
				//	*****************************************
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					int cnt;
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals("repl_dlv_sch"))
					{
						repldlv = getColumnValue("repl_dlv_sch",dom,"2");
						System.out.println("repldlv:="  + repldlv);
						if (childNode.getFirstChild() == null)
						{
							errCode = "VMREPLDLVS";
							errString = getErrorString("repl_dlv_sch",errCode,userId);
							save = 1;
							break;
						}
					
						/*sql = "SELECT COUNT(*) AS COUNT FROM repl_dlv_sch WHERE repl_dlv_sch = '"+repldlv+"'";
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							cnt = rs.getInt("COUNT");
							System.out.println("cnt :"+cnt);
							if (cnt == 0)
							{
								errCode = "VMREPLDLVS";
								errString = getErrorString("repl_dlv_sch",errCode,"");
								save = 1;
								break;
							}
						}*/
	
					}
					if (childNodeName.equals("repl_perc"))
					{
						double replPerc = ((Double.valueOf(getColumnValue("repl_perc",dom,"2"))).doubleValue()) ;
						System.out.println("repldlv:="  + replPerc);
						System.out.println("sumReplperc:="  + sumReplperc);
						System.out.println("min:="  + (int)(replPerc-sumReplperc));
						
						if (replPerc<0) 
						{
							System.out.println("enter sum");
							errCode = "UTAMR";//VTRCONT2
							errString = getErrorString("",errCode,userId);
							save = 1;
							break;
						}
						
						if (replPerc+sumReplperc>100)
						{
							System.out.println("enter sum");
							errCode = "UTAMR";//VTRCONT2
							errString = getErrorString("",errCode,userId);
							save = 1;
							break;
						}
						
				
					}
						
					/*	else if (childNodeName.equals("dlv_day"))
				{
						String dlvDay = getColumnValue("dlv_day",dom,"2");
						System.out.println("dlvDay:="  + dlvDay);
						if (childNode.getFirstChild() == null)
						{
							errCode = "REPLDLVNN";
							errString = getErrorString("dlv_day",errCode,userId);
							save = 1;
							break;
						}
						System.out.println("dlvDay="+dlvDay);
						if (dlvDay == null)
						{
							errCode = "VMREPLDLVS";
							errString = getErrorString("dlv_day",errCode,userId);
							save = 1;
							break;
						}
						cnt = Integer.parseInt(repldlv);
						System.out.println("cnt :"+cnt);
						if (cnt == 0)
						{
							errCode = "REPLDLVNN";
							errString = getErrorString("repl_dlv_sch",errCode,"");
							save = 1;
							break;
						}
		
					}*/
					/*else if (childNodeName.equals("repl_perc"))
					{
						repldlv = getColumnValue("repl_perc",dom,"2");
						System.out.println("repldlv:="  + repldlv);
						if (childNode.getFirstChild() == null)
						{
							errCode = "REPLDLVNN";
							errString = getErrorString("repl_dlv_sch",errCode,userId);
							save = 1;
							break;
						}
						cnt = Integer.parseInt(repldlv);
						System.out.println("cnt :"+cnt);
						if (cnt == 0)
						{
							errCode = "REPLDLVNN";
							errString = getErrorString("repl_dlv_sch",errCode,"");
							save = 1;
							break;
						}
					
					}*/
					
					else if (childNodeName.equals("dlv_day_type"))
					{
						int dlvdayType ;
						
						String dlvdayType1 = getColumnValue("dlv_day_type",dom,"2");
						
					
						dlvdayType = Integer.parseInt(dlvdayType1);
						String dlvDay1 = getColumnValue("dlv_day",dom,"2");
						System.out.println("dlvDay:="  + dlvDay1);
						
						if (childNode.getFirstChild() == null)
						{
							errCode = "REPLDLVNN";
							errString = getErrorString("dlv_day",errCode,userId);
							save = 1;
							break;
						}
						if (dlvDay1 == null)
						{
							errCode = "VMREPLDLVS";
							errString = getErrorString("dlv_day",errCode,userId);
							save = 1;
							break;
						}
						System.out.println("dlvdayTtype:="  + dlvdayType);
						if(dlvdayType == 1)
						{
							int dlvDay =  Integer.parseInt(getColumnValue("dlv_day",dom,"2"));
							if(!(dlvDay <= 7 &&  dlvDay > 0))
							{
								errCode = "DOW";
								errString = getErrorString("",errCode,userId);
								save = 1;
								break;
							}
							
						}
						else if(dlvdayType == 2)
						{
							dlvdayType = Integer.parseInt(getColumnValue("dlv_day_type",dom,"2"));
							System.out.println("dlvdayTtype:="  + dlvdayType);
							int dlvDay =  Integer.parseInt(getColumnValue("dlv_day",dom,"2"));
							if(dlvDay > 31 || dlvDay < 0)
							{
								errCode = "DAY";
								errString = getErrorString("repl_dlv_sch",errCode,userId);
								save = 1;
								break;
							}
						}
				
					}
							
				}//END FOR
				if(save == 0)
				{
						errString = getErrorString(" ","VTCOMMIT","");
				}
			
				
			}//END SWITCH
		}//END TRY


		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception se){}
		}
		System.out.println("ErrString ::"+errString);

		return errString;
	}//END OF VALIDATION 
	//**************************************

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("itemchange occur ..............................");
		try
		{
			System.out.println("xmlString :"+xmlString);
			System.out.println("xmlString1 :"+xmlString1);
			System.out.println("xmlString2 :"+xmlString2);
			
			dom = parseString(xmlString); //returns the DOM Object for the passed XML Stirng
			
			dom1 = parseString(xmlString1); //returns the DOM Object for the passed XML Stirng
			
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [PayablesOpeningEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
        return valueXmlString; 
	}


	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		
		System.out.println("entering itemchange..........................................");
		int n = 0;
		int currentFormNo = 0;
		String errCode = ""; 
		Connection conn = null;
		Statement stmt = null; 
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		String sql = "";
		String columnValue = "";
		String loginSite = "";
		String format = "";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName ="";
		
		NodeList parentNodeList1 = null;
		NodeList childNodeList1 = null;
		int parentNodeListLength1 = 0 ;
		int childNodeListLength1 = 0 ;
		Node parentNode1 = null;
		Node childNode1 = null;
		String childNodeName1 = "";
		int ctr = 0;
				 
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String userId = getValueFromXTRA_PARAMS(xtraParams,"userId");
		String empCode = getValueFromXTRA_PARAMS(xtraParams,"empCode");
		loginSite = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		StringBuffer retString = new StringBuffer();

		
		try
		{
			conn = getConnection(); //This function is to connect with oracle....
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}

			System.out.println("[PayablesOpeningEJB] [itemChanged] :currentFormNo ....." +currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			valueXmlString.append("<Detail>");	
			System.out.println("FORM NO-------------"+currentFormNo);								
			switch (currentFormNo)
			{
				case 1:
				valueXmlString.append("</Detail>");
				break;
				case 2:
				String currCode = "";
				String currDecr = "";
				String sundryType= "";
				String finEnt = "";
				String tranId = "";
				//searching the dom for the incoming column value start
				parentNodeList = dom.getElementsByTagName("Detail2");
				System.out.println("parent length="+parentNodeList.getLength());
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				int childListLength = childNodeList.getLength();
				do
				{
					System.out.println("ChildListLength............."+childListLength);
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue=childNode.getFirstChild().getNodeValue();
						}
					}
					ctr++;
				}
				while(ctr < childListLength && !childNodeName.equals(currentColumn));
				//searching the dom for the incoming column value end
				if (currentColumn.trim().equals("itm_default"))
				{
					
					System.out.println("ENTER THE ITM DEFAULT..................");
					String sqlEx = "";
					String replDlvsch = "";
					double sumReplperc = 0.0;
					String replPrd = getColumnValue("repl_prd",dom1);
					replDlvsch = getColumnValue("repl_dlv_sch",dom1);
					System.out.println("replPrd...............:"+replPrd);
					if(replPrd.trim().equals("M"))
					{
						System.out.println("enter if..............:"+replPrd);
						valueXmlString.append("<dlv_day_type protect = '1'>").append("2").append("</dlv_day_type>");
					}
					else
					{
						valueXmlString.append("<dlv_day_type protect = '1'>").append("1").append("</dlv_day_type>");
					}
					valueXmlString.append("<repl_dlv_sch>").append(replDlvsch).append("</repl_dlv_sch>");
				///	***********************************************************************
				
					//////////////////////////////////////////////
					System.out.println("sumReplperc....................."+sumReplperc);
					
				/*sqlEx =	"SELECT SUM(REPL_PERC) FROM REPL_DLV_SCH_DET  WHERE REPL_DLV_SCH='"+ replDlvsch +"' ORDER BY REPL_DLV_SCH";


				
				
					System.out.println("EXCHANGE SQL-----"+sqlEx);
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sqlEx);
					if (rs.next())
					{
						sumReplperc = rs.getDouble(1);
						sumReplperc = 100 - sumReplperc;
						
						
					}
					else
					{
						sumReplperc = 100;
						
					}
					System.out.println("SUM OF REPLPERC="+sumReplperc);
					rs.close();
					stmt.close();*/
					//if(sumReplperc==0)
					//{
					//sumReplperc = 100;
					//}
					//else 
					//{
					//sumReplperc = 100-sumReplperc;
					//}
					//if(sumReplperc <= 0)
					//{
					
					//errCode = "	";
					///String errString = getErrorString("repl_dlv_sch",errCode,userId);
					//return errString;
					//}
				
					parentNodeListLength1 = 0; 
					parentNodeList1 = dom2.getElementsByTagName("Detail2");
					parentNodeListLength1 = parentNodeList1.getLength(); 
					System.out.println("parentNodeListLength1------------------->"+parentNodeListLength1);
					for (int selectedRow = 0; selectedRow < parentNodeListLength1 - 1; selectedRow++)
					{
						
						parentNode1 = parentNodeList1.item(selectedRow);
						childNodeList1 = parentNode1.getChildNodes();
						childNodeListLength1 = childNodeList1.getLength();
						System.out.println("childNodeListLength1---->>> "+ childNodeListLength1);
						for (int childRow = 0; childRow < childNodeListLength1; childRow++)
						{
							childNode1 = childNodeList1.item(childRow);
							childNodeName1 = childNode1.getNodeName();
							
								if (childNode1.getFirstChild() != null)
								{
									if(childNodeName1.trim().equals("repl_perc"))
									{
										String s =childNode1.getFirstChild().getNodeValue();
										sumReplperc  =sumReplperc + ((Double.valueOf(s)).doubleValue());
									}
								}
				
					}
				}
				
					sumReplperc = 100 - sumReplperc;
					valueXmlString.append("<repl_perc>").append(sumReplperc).append("</repl_perc>");								
					
						
				}
				
				
				valueXmlString.append("</Detail>");
				break;
			}//Switch
			valueXmlString.append("</Root>");				
		}//try
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}	
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception s){}
		}
		return valueXmlString.toString();
	}
	//**************************************



}