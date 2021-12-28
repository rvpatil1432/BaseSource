package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import javax.ejb.*;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;

import javax.annotation.*;
import javax.ejb.Stateless; // added for ejb3


//public class DistIssuePrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class DistIssuePrs extends ValidatorEJB implements DistIssuePrsLocal, DistIssuePrsRemote
{
	String objName="",detailNode="";
	E12GenericUtility genericUtility= new  E12GenericUtility();
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
	
	/*public String preSaveRecWiz(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		System.out.println("DistIssuePrs Ejb called preSaveRec for wizard......");
		System.out.println("DistIssuePrsEJB called xmlString [" + xmlString + "]");
		System.out.println(" @#xmlString1 [" + xmlString1 + "]");
		Document dom = null,dom1=null;
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				if( (xmlString1.toUpperCase()).contains("DETAIL3") )
				{
					dom = GenericUtility.getInstance().parseString(xmlString1);									
					executepreSaveRec(dom,dom1,objContext,editFlag,xtraParams,conn);
				}
			}		
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistIssuePrsEJB :preSaveRec() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}*/
	

    public String preSaveRec() throws RemoteException,ITMException
	{
    	return "";
	}
    	
	
	
	public String preSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	//public String preSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		System.out.println("DistIssuePrsEJB called domId [" + domId + "] xmlString1 [" + xmlString1 + "]");
		Document dom = null, dom1 = null;
		try
		{
		
		
		
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				if (xmlString1 != null && xmlString1.trim().length() > 0)
				{
					dom = genericUtility.parseString(xmlString1);
					//if( (xmlString1.toUpperCase()).contains("DETAIL2") ||  (xmlString1.toUpperCase()).contains("DETAIL3") )
					if( (xmlString1.toUpperCase()).contains("DETAIL3") )
					{
						objName="distisswiz";
						detailNode="detail3";
						
					}
					else
					{
						objName="";
						detailNode="detail2";
					}
					executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
			}			

			/*if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = GenericUtility.getInstance().parseString(xmlString);
				//executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
			
				if (xmlString1 != null && xmlString1.trim().length() > 0)
				{
					dom1 = GenericUtility.getInstance().parseString(xmlString1);
					//if( (xmlString1.toUpperCase()).contains("DETAIL2") ||  (xmlString1.toUpperCase()).contains("DETAIL3") )
					if( (xmlString1.toUpperCase()).contains("DETAIL3") )
					{
						objName="distisswiz";
						detailNode="detail3";
						
					}
					else
					{
						objName="";
						detailNode="detail2";
					}
					executepreSaveRec(dom,dom1,objContext,editFlag,xtraParams,conn);
				}
			*/	
				
						
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :DistIssuePrsEJB :preSaveRec() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	//private String executepreSaveRec(Document dom, Document dom1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null,stmt1 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", line = "", tranType = "", line1 = "", stockOpt = "", sqlItem = "";
		HashMap strAllocate = new HashMap();
		double quantity = 0d;
		int retVal = 0;
		java.sql.Date tranDate = null;
		DistStkUpd distStkUpd = null;
		String updateStatus = "",quantityStr = "",tranId = ""; 
		NodeList hdrDom = null;
		Node currDetail = null;
		try
		{
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();			
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("\n tranDate :"+tranDate);
			
			//hdrDom = dom1.getElementsByTagName("Detail1");
			if("distisswiz".equalsIgnoreCase(objName))
			{
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			else
			{
				hdrDom = dom.getElementsByTagName("Detail1");
			}
			
			/*if("distisswiz".equalsIgnoreCase(objName))
			{
				System.out.println("distisswiz condition............#$#$#");
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			else
			{
				hdrDom = dom.getElementsByTagName("Detail1");
			}*/
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
				
			System.out.println("siteCode :"+siteCode);
			System.out.println("\n tranId :"+tranId);

			//currDetail = getCurrentDetailFromDom(dom,domID);
			//currDetail = dom.getElementsByTagName("Detail2").item(0);
			/*if("distisswiz".equalsIgnoreCase(objName))
			{
				currDetail = dom.getElementsByTagName("Detail3").item(0);;
			}
			else
			{
				currDetail = dom.getElementsByTagName("Detail2").item(0);
			}
			updateStatus = getCurrentUpdateFlag(currDetail);
			*/
			currDetail	 = getCurrentDetailFromDom(dom,domID);
			if (tranId == null || tranId.trim().length() == 0)
			{
				tranId  = genericUtility.getColumnValueFromNode("tran_id",currDetail);
			}
			System.out.println("Tran ID from currDetail  ["+tranId + "]");
			updateStatus = getCurrentUpdateFlag(currDetail);
			
			System.out.println("updateStatus :"+updateStatus);
			if (currDetail != null && !updateStatus.equalsIgnoreCase("A") && !updateStatus.equalsIgnoreCase("N"))//&& !updateStatus.equalsIgnoreCase("N") CONDITION ADDED BY NANDKUMAR GADKARI ON 14/01/18
			{
				System.out.println("in condition.................@@@@@@");
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				System.out.println("lineNo------->>["+lineNo+"]");
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				System.out.println("itemCode------->>["+itemCode+"]");
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				System.out.println("locCode------->>["+locCode+"]");
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				System.out.println("lotNo------->>["+lotNo+"]");
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				System.out.println("lotSl------->>["+lotSl+"]");
				quantityStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
				System.out.println("quantityStr------->>["+quantityStr+"]");
				if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))
				{
					//Commented And Changes Below - Gulzar 17/05/07
					//sql = "SELECT QUANTITY FROM DISTORD_ISSDET WHERE TRAN_ID ='"+tranId+"' AND LINE_NO ="+lineNo+""; // Gulzar 17/05/07
					sql = "SELECT ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY FROM DISTORD_ISSDET WHERE TRAN_ID ='"+tranId+"' AND LINE_NO ="+lineNo+""; // Gulzar 17/05/07
					System.out.println("sql dist 11212sql-->>["+sql+"]");
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						itemCode	= rs.getString("ITEM_CODE"); //Gulzar 17/05/07
						locCode		= rs.getString("LOC_CODE"); //Gulzar 17/05/07
						lotNo		= rs.getString("LOT_NO"); //Gulzar 17/05/07
						lotSl		= rs.getString("LOT_SL"); //Gulzar 17/05/07
						quantityStr = rs.getString("QUANTITY");
					}
					//End Changes - Gulzar 17/05/07
				}
				System.out.println("sql dist sql-->>["+sql+"]");
				quantity = Double.parseDouble(quantityStr);
				if (lotNo == null || lotNo.trim().length() == 0)
				{
					lotNo = "               ";
				}
				if (lotSl == null || lotSl.trim().length() == 0)
				{
					lotSl = "     ";
                }
                
                //Commented by Anagha R on 12/01/2021 for Error while confirmation of Dist Issue Transaction START
				//sqlItem = "SELECT STK_OPT FROM SITEITEM WHERE ITEM_CODE = '"+itemCode+"' "
                //		 +"AND SITE_CODE = '"+siteCode+"'";
                //System.out.println("The select sql sqlItem:"+sqlItem);
				//rs1 = stmt1.executeQuery(sqlItem);
				//if (rs1.next())
				//{
				//	stockOpt = rs1.getString(1);
				//	System.out.println("stockOpt :"+stockOpt);
				//}
				//if (stockOpt == null)
				//{
                //Commented by Anagha R on 12/01/2021 for Error while confirmation of Dist Issue Transaction END
					sqlItem = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
					System.out.println("The select sql sqlItem:"+sqlItem);
					rs2 = stmt1.executeQuery(sqlItem);
					if (rs2.next())
					{
						stockOpt = rs2.getString(1);
						System.out.println("stockOpt :"+stockOpt);
					}
				//}
				if (!stockOpt.equalsIgnoreCase("0"))
				{
					line = "   " + lineNo;
					System.out.println("line1  :"+line);
					
					strAllocate.put("tran_date",tranDate);
					//strAllocate.put("ref_ser","D-ISS");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));					
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(-1*quantity)); 
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					//strAllocate.put("chg_win","W_DIST_ISSUE");
					
					if("distisswiz".equalsIgnoreCase(objName))
					{
						strAllocate.put("chg_win","W_DISTISSWIZ");
						//strAllocate.put("ref_ser","WI-ISS");
						strAllocate.put("ref_ser","D-ISS");
					}
					else
					{
						strAllocate.put("chg_win","W_DIST_ISSUE");
						strAllocate.put("ref_ser","D-ISS");
					}
					
					System.out.println("Calling DistStkUpdEJB.....");
					//if (distStkUpd.updAllocTrace(strAllocate) > 0)
					/*Changed by HATIM on 13/01/2006*/
					distStkUpd = new DistStkUpd();
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0) 
					{
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}
					distStkUpd = null;
					/*END*/
				}// end if
			}// end if
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in DistIssuePrs :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in DistIssuePrsEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if(stmt1 != null)
				{
					stmt1.close();
					stmt1 = null;
				}				
			}
			catch(Exception e)
			{
				System.err.println("Exception :DBAccessEJB :getITMVersion :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return "";
	}//ConsumeIssuePrs end

	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;

		//----  Added by Sandesh 2-Oct-2014  -------
		if("distisswiz".equalsIgnoreCase(objName))
		{
			detailList = dom.getElementsByTagName("Detail3");
		}
		else
		{
			detailList = dom.getElementsByTagName("Detail2");
		}		
		//----------------------------
		
		//detailList = dom.getElementsByTagName("Detail2");		// commented by Sandesh 2-Oct-2014
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
			currDomId = currDetail.getAttributes().getNamedItem("domID").getNodeValue();
			if (currDomId.equals(domId))
			{
				reqDetail = currDetail;
				break;
			}			
		}
		return reqDetail;
	}

	private String getCurrentUpdateFlag(Node currDetail)
	{
		NodeList currDetailList = null;
		String updateStatus = "",nodeName = "";
		int currDetailListLength = 0;

		currDetailList = currDetail.getChildNodes();
		currDetailListLength = currDetailList.getLength();
		for (int i=0;i< currDetailListLength;i++)
		{
			nodeName = currDetailList.item(i).getNodeName();
			if (nodeName.equalsIgnoreCase("Attribute"))
			{
				updateStatus = currDetailList.item(i).getAttributes().getNamedItem("updateFlag").getNodeValue();
				break;
			}
		}
		return updateStatus;		
	}	

}