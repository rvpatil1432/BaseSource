package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class SRLContainerIC extends ValidatorEJB implements SRLContainerICRemote, SRLContainerICLocal 
{	
	E12GenericUtility genericUtility = new E12GenericUtility();
	
   	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException  
    {
   		Document dom = null; 
   		Document dom1 = null; 
   		Document dom2 = null;
   		String retString = "";	
   		try
   		{
   			System.out.println("Value of xmlStrings in Validation method :::  [xmlString ["+xmlString+"]] \n [xmlString1 ["+xmlString1+"]] \n [xmlString2 ["+xmlString2+"]]");
   			if(xmlString != null && xmlString.trim().length()>0)
	   		{
	   			dom = genericUtility.parseString(xmlString);		
	   		}
	   		if(xmlString1 != null && xmlString1.trim().length()>0)
	   		{
	   			dom1 = genericUtility.parseString(xmlString1);
	   		}
	   		if(xmlString2 != null && xmlString2.trim().length()>0)
	   		{
	   			dom2 = genericUtility.parseString(xmlString2);
	   		}
	   		retString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
	   		System.out.println("retString [wfValData]"+retString);
		}
        catch(Exception e)
        {
	        System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			e.getMessage(); 
			throw new ITMException(e);
        }
        return retString;	    
	}//end of method wfValData
   	
	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		String errorType = "" , errCode = "" ,  sql = "" , errString = "" , userId = "" , childNodeName = "";
		String itemCode = "" , inventoryType = "" , lotNo = "" , lotSl = "" , noArt = "", serialNoContent = "",inverntoryType = "";
		NodeList parentNodeList = null;
		Node parentNode = null;
		NodeList childNodeList = null;
		Node childNode = null;
		int childNodeLength = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int currentFormNo = 0 , cnt = 0 , detlCnt = 0, stkCnt = 0;
		String updateFlag = "", detItemCode="", contentType = "", itemCodeDtl = "", quantity = "",lotSlExist="";
		List<String> DetItemCodeList = new ArrayList<String>();
		//Added by sarita on 18MARCH2018
		String invSrnoReqd = "";
		HashMap<Integer,String> lotSlVal = null;
		String avalibleFrLocCode = "";//Added by sarita on 29 OCT 2018
		//Added by sarita on 02 NOV 2018 [START]
		ArrayList stockDataList = new ArrayList();
		//Added by sarita on 02 NOV 2018 [END]
		try
		{
			conn = getConnection();
			//Added by sarita on 18MARCH2018
			lotSlVal = new HashMap<Integer,String>();
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("[CurrentFormNo [Validation]==========> ["+currentFormNo+"]]");
				
				switch(currentFormNo)
				{
					case 1: 
					{
						parentNodeList = dom.getElementsByTagName("Detail1");
		    			parentNode = parentNodeList.item(0);
		    			childNodeList = parentNode.getChildNodes();
		    			childNodeLength = childNodeList.getLength();
		    			
		    			for(int ctr = 0; ctr < childNodeLength; ctr++)
		    			{
		    				childNode =  childNodeList.item(ctr);
		    				childNodeName = childNode.getNodeName();
		    				System.out.println("[childNodeName[Validation][Detail1]===========> ]"+childNodeName);
		    				
		    				if("serial_date".equalsIgnoreCase(childNodeName))
		    				{
		    					String serialDate = checkNullAndTrim(genericUtility.getColumnValue("serial_date",dom)).trim();
		    					System.out.println("Date is :::["+serialDate+"]");
		    					if(serialDate == null || serialDate.trim().length() == 0)
		    					{
		    						errCode = "DSSRDATE";
				    				errList.add(errCode);
				    				errFields.add("serial_date");
		    					}
		    				}
		    				else if("item_code".equalsIgnoreCase(childNodeName))
		    				{
		    					itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code",dom)).trim();
		    					inventoryType = checkNullAndTrim(genericUtility.getColumnValue("inventory_type",dom)).trim();	    					
		    					System.out.println("[ITEM CODE ["+itemCode+"]] \n [Inventory Type ["+inventoryType+"]] ");
		    					
		    					//if("0".equalsIgnoreCase(inventoryType))
		    					//{
		    						if(itemCode == null || itemCode.trim().length() == 0)
		    						{
		    							errCode = "VTITEMBLK";
					    				errList.add(errCode);
					    				errFields.add("item_code");
		    						}
		    						else if(itemCode != null && itemCode.trim().length() > 0)
		    						{
		    							cnt = 0;
		    							//changes by sarita on 14APR2018 [start] - to validate item_code as inv_srno_reqd is 'Y' or 'N'
		    							//sql = "select count(*) as cnt from item where item_code=?";
		    							sql = "select case when inv_srno_reqd is null then 'N' else inv_srno_reqd end as inv_srno_reqd , 1 as cnt from item where item_code = ?";
		    							pstmt = conn.prepareStatement(sql);
						    			pstmt.setString(1,itemCode);
						    			rs = pstmt.executeQuery();
						    			if(rs.next())
						    			{
						    				cnt = rs.getInt("cnt");	
						    				//Added by sarita on 14APR2018 to get inv_srno_reqd val [start]
						    				invSrnoReqd = rs.getString("inv_srno_reqd");				
						    				//System.out.println("Cnt..item_code :"+cnt);
						    				System.out.println("Cnt..item_code : & inv_srno_reqd.. item code is ("+cnt+" , "+invSrnoReqd+")");
						    				//Added by sarita on 14APR2018 to get inv_srno_reqd val [end]
						    			}
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
						    			if(cnt == 0)
						    			{
				    					   errCode = "IVITMCDASN";
				    					   errList.add(errCode);
				    					   errFields.add("item_code");
						    			}
						    			//Added by sarita on 14APR2018[start] - to validate item_code as inv_srno_reqd is 'Y' or 'N'
						    			if("Y".equalsIgnoreCase(invSrnoReqd) == false)
						    			{
						    				   errCode = "IVITMSRRQD";
					    					   errList.add(errCode);
					    					   errFields.add("item_code");
						    			}
						    			//Added by sarita on 14APR2018[end] - to validate item_code as inv_srno_reqd is 'Y' or 'N'
		    						}
		    					//}		    					
		    				}//End of Validation for item_code 
		    				
		    				else if("lot_no".equalsIgnoreCase(childNodeName))
		    				{
		    					//inventoryType = checkNullAndTrim(genericUtility.getColumnValue("inventory_type",dom)).trim();
		    					lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom)).trim();
		    					System.out.println("[Lot Number ["+lotNo+"]]");
		    					//if("2".equalsIgnoreCase(inventoryType))
		    					//{
		    						if(lotNo == null || lotNo.trim().length() == 0)
		    						{
		    							errCode = "VMLOTNONUL";
					    				errList.add(errCode);
					    				errFields.add("lot_no");
		    						}
		    						//Deleted by sarita on 18MARCH2018
		    					/*	else if(lotNo != null && lotNo.length() > 0)
				    				{
		    							sql = "select count(*) as cnt from Stock where lot_no=?";
				    					pstmt = conn.prepareStatement(sql);
						    			pstmt.setString(1,lotNo);
						    			rs = pstmt.executeQuery();
						    			if(rs.next())
						    			{
						    				cnt = rs.getInt("cnt");
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
						    			if(cnt == 0)
						    			{
				    					   errCode = "VTLOTASN";
				    					   errList.add(errCode);
				    					   errFields.add("lot_no");
						    			}
				    				}
		    					//}	*/
		    			   }//end of validation for lot_no
		    				
		    			   else if("lot_sl".equalsIgnoreCase(childNodeName))
		    			   {
		    				   itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code",dom)).trim();
		    				   lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom)).trim();
		    				   lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl",dom)).trim();
		    				   
		    				   System.out.println("Lot SL Validation of Header foritemCode["+itemCode+"] , lotNo["+lotNo+"], lotSl["+lotSl+"]");
		    				   
		    				   if(lotSl == null || lotSl.trim().trim().length() == 0)
		    				   {
		    					    errCode = "VTLTSLBLNK";
				    				errList.add(errCode);
				    				errFields.add("lot_sl");
		    				   }
		    				   //Added by sarita on 18MARCH2018
		    				   else if(lotSl != null && lotSl.length() > 0)
		    				   {
		    					   if(lotNo != null && lotNo.trim().length() >0)
		    					   {
		    						   if(itemCode != null && itemCode.trim().length() > 0)
		    						   {
		    							   //Commented & Added by sarita to perform validation on location as location should not be GIT Location on 29 OCT 2018 [START]   
		    							   /*sql = "select count(*) as cnt from stock where item_code=? and lot_no=? and lot_sl=? and quantity > 0";
		    								pstmt = conn.prepareStatement(sql);
		    								pstmt.setString(1, itemCode);
		    								pstmt.setString(2, lotNo);
		    								pstmt.setString(3, lotSl);
		    								rs = pstmt.executeQuery();
		    								if(rs.next())
		    								{
		    									cnt = rs.getInt("cnt");
		    									System.out.println("Count of Stock for item_code , lotNo and lotSL is ["+cnt+"]");
		    								}

		    								if(rs != null)
		    								{
		    									rs.close();
		    									rs = null;
		    								} 
		    								if(pstmt != null)
		    								{
		    									pstmt.close(); 
		    									pstmt = null;
		    								}*/		   
		    							   sql = "select a.available "
		    									   + "from invstat a , Location b , Stock c "
		    									   + "where a.inv_stat = b.inv_stat "
		    									   + "AND b.loc_code = c.loc_code "
		    									   + "AND c.ITEM_CODE = ? "
		    									   + "AND c.LOT_NO = ? "
		    									   + "AND c.LOT_SL = ? "
		    									   + "AND c.quantity > 0 "
		    									   + "group by a.available ,c.loc_code,c.item_code,c.lot_no,c.lot_sl,c.quantity";
		    							   pstmt = conn.prepareStatement(sql);
		    							   pstmt.setString(1, itemCode);
		    							   pstmt.setString(2, lotNo);
		    							   pstmt.setString(3, lotSl);
		    							   rs = pstmt.executeQuery();
		    							   while(rs.next())
		    							   {
		    								   stkCnt++;
		    								   avalibleFrLocCode = rs.getString("available");   
		    								   //Added by sarita to store data into ArrayList on 02 NOV 2018 [START]
		    								   stockDataList.add(stkCnt);
		    								   stockDataList.add(avalibleFrLocCode);
		    								   //Added by sarita to store data into ArrayList on 02 NOV 2018 [END]
		    							   }		    				
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
		    							   System.out.println("stockDataList ["+stockDataList+"] \t ["+stockDataList.size()+"]");
		    							   //Commented and Added by sarita on 02 NOV 2018 [START]
		    							   if(stockDataList.isEmpty() == true)
		    							   {
		    								   errCode = "VTNOSTK";//No records of stock for item_code,lot_no and lot_sl
		    								   errList.add(errCode);
		    								   errFields.add("lot_sl");
		    							   }
		    							   else if(stockDataList.size() > 2)
		    							   {
		    								   errCode = "INVRCDSTOC";//Multiple Records Found in stock for item_code,lot_no and lot_sl
		    				    			   errList.add(errCode);
		    				    			   errFields.add("lot_sl");
		    							   }
		    							   else
		    							   {
		    								   stkCnt = (Integer)stockDataList.get(0);
		    								   avalibleFrLocCode = (String)stockDataList.get(1);
	    									   System.out.println("avalibleFrLocCode ["+avalibleFrLocCode+"]");
	    									   if((stkCnt == 1) && ("N".equalsIgnoreCase(avalibleFrLocCode)))
			    		    				   {
			    		    					   errCode = "VTINVGIT";//Please check the GIT Location Code, it does not exists or not for internal use.
			    				    			   errList.add(errCode);
			    				    			   errFields.add("lot_sl");
			    		    				   }						   
		    							   }
		    							/*   if(cnt == 0)
		    		    				   {
		    		    					    errCode = "VTNOSTK";//No records of stock for item_code,lot_no and lot_sl
		    				    				errList.add(errCode);
		    				    				errFields.add("lot_sl");
		    		    				   }
		    		    				   else if(cnt > 1)     
		    		    				   {
		    		    					   errCode = "INVRCDSTOC";//Multiple Records Found in stock for item_code,lot_no and lot_sl
		    				    			   errList.add(errCode);
		    				    			   errFields.add("lot_sl");
		    		    				   }
		    		    				   else if("N".equalsIgnoreCase(avalibleFrLocCode))
		    		    				   {
		    		    					   System.out.println("COUNT if available is N ["+cnt+"]");
		    		    					   errCode = "VTINVGIT";//Please check the GIT Location Code, it does not exists or not for internal use.
		    				    			   errList.add(errCode);
		    				    			   errFields.add("lot_sl");
		    		    				   }*/
		    							 //Commented and Added by sarita on 02 NOV 2018 [END]
		    						   }
		    					   }
		    				   }   
		    				   //Commented and Added by sarita to perform validation on location as location should not be GIT Location on 29 OCT 2018 [END]
		    				/*  else
		    				   {
		    					   String serialNo = checkNullAndTrim(genericUtility.getColumnValue("serial_no",dom)).trim();
		    					   if(serialNo == null || serialNo.length() == 0)
		    					   {
		    						   serialNo = "*";	    						   
		    					   }
		    					   
		    				   }*/  // Need to work on it
		    			   }//end of validation for lot_sl
		    				
		    			}//-------------------- End of For Loop -----------------------------
					}// ----------------------- End of Case1 --------------------------------
					break;
					
					case 2:
					{
						parentNodeList = dom.getElementsByTagName("Detail2");
		    			parentNode = parentNodeList.item(0);
		    			childNodeList = parentNode.getChildNodes();
		    			childNodeLength = childNodeList.getLength();
		    			
		    			
		    			for(int ctr = 0; ctr < childNodeLength; ctr++)
		    			{
		    				childNode =  childNodeList.item(ctr);
		    				childNodeName = childNode.getNodeName();
		    				System.out.println("[childNodeName[Validation][Detail2]===========> ]"+childNodeName);
		    							
		    				if("serial_no__content".equalsIgnoreCase(childNodeName))
		    				{
		    					serialNoContent = checkNullAndTrim(genericUtility.getColumnValue("serial_no__content",dom)).trim();
		    					System.out.println("[serial_no__content ["+serialNoContent+"]]");
		    					
		    					if(serialNoContent == null || serialNoContent.length() == 0)
		    					{
		    						errCode = "VMSRNO";
		  							errList.add(errCode);
		  							errFields.add("serial_no__content");
		    					}
		    				}//end of validation for serial_no__content
		    				
		    				else if("item_code".equalsIgnoreCase(childNodeName))
		    				{
		    					inverntoryType = checkNullAndTrim(genericUtility.getColumnValue("inventory_type", dom1));
		    					contentType = checkNullAndTrim(genericUtility.getColumnValue("content_type", dom));
		    					itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom1));
		    					itemCodeDtl = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
		    					
		    					System.out.println("Inventory Type ====> ["+inverntoryType+"] \t  Item Code Header=====> ["+itemCode+"]\t  Content Type ======>["+contentType+"]\t Item Code Detail ["+itemCodeDtl+"] ");
		    					
		    					//if("0".equalsIgnoreCase(contentType) && "0".equalsIgnoreCase(contentType))
		    					//{
		    						if(itemCodeDtl == null || itemCodeDtl.trim().length() == 0)
		    						{
		    							errCode = "VTITEMCD";
			  							errList.add(errCode);
			  							errFields.add("item_code");
		    						}
		    						else if(itemCode != null && itemCode.trim().length() > 0)
		    						{
		    							sql = "select count(*) as cnt from item where item_code=?";
		    							pstmt = conn.prepareStatement(sql);
						    			pstmt.setString(1,itemCodeDtl);
						    			rs = pstmt.executeQuery();
						    			if(rs.next())
						    			{
						    				cnt = rs.getInt("cnt");
						    				System.out.println("Cnt..item_code :"+cnt);
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
						    			if(cnt == 0)
						    			{
				    					   errCode = "IVITMCDASN";
				    					   errList.add(errCode);
				    					   errFields.add("item_code");
						    			}
		    						}
		    					//}
		    				    // Commented by sarita due to performance issue inverntoryType validation shifted to post save on [24 JUL 18] [START]		
		    				/*	if("0".equalsIgnoreCase(inverntoryType))
		    					{
		    						int detlCnt1 = getNumOfNonDelDetail(dom2,2); System.out.println("Detal Count in Itemchange ::::["+detlCnt1+"]");
		    						if(detlCnt1 >= 1)
		    						{
		    							NodeList detailNoteList = dom2.getElementsByTagName("Detail2");
		    							for(int cntr = 0;cntr<detailNoteList.getLength();cntr++)
		    							{
		    								Node pNode=detailNoteList.item(cntr);
											childNodeName = pNode.getNodeName();
											updateFlag = getAttributeVal(pNode,"updateFlag");
											detItemCode = checkNullAndTrim(genericUtility.getColumnValueFromNode("item_code", pNode));																																																																																																																																																																																																																																																																																																																																																																																				
											if((detItemCode != null && detItemCode.length() > 0) && ("A".equalsIgnoreCase(updateFlag)))
											{
												DetItemCodeList.add(detItemCode);
											}		
		    							}
		    							if(!(DetItemCodeList.contains(itemCode)))
			    						{ 
			    							errCode = "VTITEMINVT";//"VTITEMINVT";//Selected Header Item is mandatory to Insert for Inventory Type Item.
				  							errList.add(errCode);
				  							errFields.add("item_code");
			    						}
		    						}		    						
		    					}*/
		    						// Commented by sarita due to performance issue inverntoryType validation shifted to post save on [24 JUL 18] [END]
		    				}//end of validation for item_code
		    				
		    				/*else if("lot_no".equalsIgnoreCase(childNodeName))
		    				{
		    					lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom)).trim();
		    					contentType = checkNullAndTrim(genericUtility.getColumnValue("content_type", dom));
		    					System.out.println("[Lot Number ["+lotNo+"]] \t [Content Type ["+contentType+"]]");
		    					if("0".equalsIgnoreCase(contentType))
		    					{
		    						if(lotNo == null || lotNo.trim().length() == 0)
		    						{
		    							errCode = "VMLOTNONUL";
		    							errList.add(errCode);
		    							errFields.add("lot_no");
		    						}
		    					}
		    					if(lotNo != null && lotNo.length() > 0)
			    				{
	    							sql = "select count(*) as cnt from Stock where lot_no=?";
			    					pstmt = conn.prepareStatement(sql);
					    			pstmt.setString(1,lotNo);
					    			rs = pstmt.executeQuery();
					    			if(rs.next())
					    			{
					    				cnt = rs.getInt("cnt");
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
					    			if(cnt == 0)
					    			{
			    					   errCode = "VTLOTASN";
			    					   errList.add(errCode);
			    					   errFields.add("lot_no");
					    			}
			    				}
		    				}//end of validation for lot_no*/
		    				
		    				else if("lot_sl".equalsIgnoreCase(childNodeName))
			    			{
		    					   itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom1));
			    				   lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl",dom)).trim();
			    				   lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom)).trim();
			    				   System.out.println("Lot SL Validation in Detail :::: [itemCode["+itemCode+"] , lotSl["+lotSl+"] , lotNo["+lotNo+"]]");
			    				   
			    				  //changes and commented by sarita on 24 JULY 18  
			    				  // if(lotSl == null || lotSl.trim().length() == 0)
			    				   if(lotSl == null || lotSl.length() == 0)
			    				   {
			    					    errCode = "VTLTSLBLNK";
					    				errList.add(errCode);
					    				errFields.add("lot_sl");
			    				   }
			    				    //changes and commented by sarita on 24 JULY 18  
			    				   //if(lotSl != null && lotSl.length() > 0)
			    				   else
			    				   {
			    					    sql = "select count(*) as cnt from stock where item_code=? and lot_no=? and lot_sl=? and quantity > 0";
	    								pstmt = conn.prepareStatement(sql);
	    								pstmt.setString(1, itemCode);
	    								pstmt.setString(2, lotNo);
	    								pstmt.setString(3, lotSl);
	    								rs = pstmt.executeQuery();
					    				if(rs.next())
					    				{
					    					cnt = rs.getInt("cnt");
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
					    				if(cnt > 0)
					    				{
			    					    errCode = "VTINVSRIAL";//"VTINVLOTSL"; 
					    				errList.add(errCode);
					    				errFields.add("lot_sl");
					    				}
					    				//Added by sarita on 18MARCH2018
					    				// Commented by sarita due to performance issue lot_sl validation shifted to post save on [24 JUL 18] [START]
					    			/*	else
					    				{
					    					System.out.println("Value of ItemCode and LotNumber in Details Screen : +itemCode["+itemCode+"] , lotNo["+lotNo+"]");
					    					System.out.println("detailString value is =="+genericUtility.serializeDom(dom2));
					    					NodeList detailNoteList = dom2.getElementsByTagName("Detail2");
					    					for(int cntr = 0;cntr<detailNoteList.getLength();cntr++)
					    					{
					    						Node pNode=detailNoteList.item(cntr);
												childNodeName = pNode.getNodeName();
												updateFlag = getAttributeVal(pNode,"updateFlag");
												if("D".equalsIgnoreCase(updateFlag))
												{
													continue;
												}
												else
												{
													lotSlExist = checkNullAndTrim(genericUtility.getColumnValueFromNode("lot_sl", pNode));				
													System.out.println("Lot SL Value is XML:::"+lotSlExist);
													System.out.println("Current Lot SL Value is :::"+lotSl);
													if(!(lotSlVal.isEmpty()) && (lotSlVal.containsValue(lotSlExist) ) && !("D".equalsIgnoreCase(updateFlag)))
													{
														errCode = "INVDTLRCD";
									    				errList.add(errCode);
									    				errFields.add(childNodeName.toLowerCase());
													}
													else
													{
														lotSlVal.put(cntr, lotSlExist);
													}
												}		
					    					}
					    				}*/
					    				// Commented by sarita due to performance issue lot_sl validation shifted to post save on [24 JUL 18] [END]
			    				   }	    				  
			    			}//end of validation for lot_sl	
		    				
		    				else if("quantity".equalsIgnoreCase(childNodeName))
		    				{
		    					quantity = checkNullAndTrim(genericUtility.getColumnValue("quantity",dom)).trim();
		    					System.out.println("Quantity is ::: ["+quantity+"]");
		    					if(quantity == null || quantity.trim().length() == 0)
		    					{
		    						errCode = "VTQUNT2";
    								errList.add(errCode);
    								errFields.add("quantity");
		    					}
		    					else if(quantity != null && quantity.length() > 0)
		    					{		    						
		    						try
		    						{
		    							int qty = Integer.parseInt(quantity);
		    							if(qty == 0 || qty < 0)
		    							{
		    								errCode = "VMNOART";
		    								errList.add(errCode);
		    								errFields.add("quantity");
		    							}
		    						}
		    						catch(NumberFormatException e)
		    						{
		    							errCode = "SRWIZQTY1";
	    								errList.add(errCode);
	    								errFields.add("quantity");
		    						}
		    					}
		    				}//end of validation for quantity
		    			}
					}//----------------------- End of Case2 ---------------------------------
					break;			
				}//------------------------End of switch satatement---------------------------
				int errListSize = errList.size();
			    System.out.println("errListSize::::::::::"+errListSize);
			    int count = 0;
			    String errFldName = null;
			    if (errList != null && errListSize > 0)
			    {
			    	for (count = 0; count < errListSize; count++)
			    	{
			    		errCode = errList.get(count);
			    		errFldName = errFields.get(count);
			    		System.out.println(" testing :errCode .:" + errCode);
			    		errString = getErrorString(errFldName, errCode, userId);
			    		System.out.println("errString>>>>>>>>>"+errString);
			    		errorType = errorType(conn, errCode);
			    		if (errString.length() > 0)
			    		{
			    			String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
			    			bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
			    			errStringXml.append(bifurErrString);
			    			errString = "";
			    		}
			    		if (errorType.equalsIgnoreCase("E"))
			    		{
			    			break;
			    		}
			    	}//end of for loop
			    	errList.clear();
			    	errList = null;
			    	errFields.clear();
			    	errFields = null;
			    	errStringXml.append("</Errors> </Root> \r\n");
			    } 
			    else
			    {
			    	errStringXml = new StringBuffer("");
			    }
			}
		}//----------------------End of Try Block -------------------------------
		catch(Exception e)
		{
			System.out.println("[Inside wfValdata [SRLContainerIC]]"+e);
			e.printStackTrace();
			throw new ITMException(e);
		}//----------------------End of Catch Block ---------------------------
		finally
		{
			try
			{
				if (conn != null)
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
						conn.close();
					}
					conn = null;
				} 
				catch (Exception d)
				{
					d.printStackTrace();
					throw new ITMException(d);
				}
		}//----------------------End of Finally Block ---------------------------
		return errStringXml.toString();
		
	}// end of wfvalData method
	
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}//end of method checkNullAndTrim
	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document dom=null;
		Document domhr=null;
		Document domAll=null;
		String retString="";
		try
		{
			System.out.println("Value of xmlStrings in Itemchange method :::  [xmlString ["+xmlString+"]] \n [xmlString1 ["+xmlString1+"]] \n [xmlString2 ["+xmlString2+"]]");
			if(xmlString != null && xmlString.trim().length()>0)
	   		{
				dom = genericUtility.parseString(xmlString);		
	   		}
			if(xmlString1 != null && xmlString1.trim().length()>0)
	   		{
				domhr = genericUtility.parseString(xmlString1);		
	   		}
			if(xmlString2 != null && xmlString2.trim().length()>0)
	   		{
				domAll = genericUtility.parseString(xmlString2);		
	   		}
			retString = itemChanged(dom,domhr,domAll,objContext,currentColumn,editFlag,xtraParams);
			System.out.println("retString [itemChanged]"+retString);
		}
		catch(Exception e)
		{
			System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			e.getMessage(); 	
			throw new ITMException(e);
		}
		return retString;
	}
	
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();	
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null ;
		ResultSet rs1 = null ;
		int currentFormNo=0,ctr = 0;	
		String loginSite = "" , childNodeName = "" , sql = "",item_descr="", site_descr="";
		NodeList parentNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		NodeList childNodeList = null;
		int childNodeListLength = 0 , cnt = 0;
		SimpleDateFormat sdf;
		double qty = 0;
		String serialNo = "", serialDate = "", description = "", numOfArticle="", inverntoryType="", contentLevel="",itemCode="",lotNo="",lotSl="",createSource="",srlNoSource="";
		try
		{
			conn = getConnection();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			loginSite =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");System.out.println(" loginSite::"+loginSite);
			if(objContext != null && objContext.trim().length()>0)
				{
					currentFormNo = Integer.parseInt(objContext);
					System.out.println("[CurrentFormNo [Itemchange]==========> ["+currentFormNo+"]]");
				}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			switch(currentFormNo)
			{
				case 1:
				{
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					System.out.println("childNodeList [Case1]====> ["+childNodeList+"]");
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					valueXmlString.append("<Detail1>\r\n");
					childNodeListLength = childNodeList.getLength();
					System.out.println("currentColumn-----for case1--->>[" + currentColumn + "]");
					
					if("itm_default".equalsIgnoreCase(currentColumn.trim()))
					{
						itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom1)); 
						lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom1)).trim();
    					lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl",dom1)).trim();
    					
						valueXmlString.append("<serial_date>").append("<![CDATA[" + sdf.format(new Date())+ "]]>").append("</serial_date>");
						valueXmlString.append("<site_code>").append("<![CDATA[" +loginSite+ "]]>").append("</site_code>");
						valueXmlString.append("<no_art protect = \"1\">").append("<![CDATA[]]>").append("</no_art>");
						
						sql = "select descr from site where site_code=?";
					    pstmt = conn.prepareStatement(sql);
	    				pstmt.setString(1,loginSite);
	    				rs = pstmt.executeQuery();
	    				if(rs.next())
	    				{
	    					site_descr = rs.getString("descr");
	    					System.out.println("Item Description is ===========>"+site_descr);
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
	    				valueXmlString.append("<site_descr><![CDATA[" ).append(site_descr).append( "]]></site_descr>\r\n" );
					}
										
					else if("itm_defaultedit".equalsIgnoreCase(currentColumn.trim()))
					{
						serialDate = checkNullAndTrim(genericUtility.getColumnValue("serial_date", dom));						
						valueXmlString.append("<serial_date>").append("<![CDATA[" + serialDate + "]]>").append("</serial_date>");			
					}
					else if("item_code".equalsIgnoreCase(currentColumn.trim()))
					{
						itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						System.out.println("Entered Item code is :::::::::::::"+itemCode);
						
						sql = "select descr from item where item_code=?";
					    pstmt = conn.prepareStatement(sql);
	    				pstmt.setString(1,itemCode);
	    				rs = pstmt.executeQuery();
	    				if(rs.next())
	    				{
	    					item_descr = rs.getString("descr");
	    					System.out.println("Item Description is ===========>"+item_descr);
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
	    				valueXmlString.append("<item_descr><![CDATA[" ).append(item_descr).append( "]]></item_descr>\r\n" );
					}
					else if("lot_sl".equalsIgnoreCase(currentColumn.trim()))
					{
						itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)).trim(); 
						lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom)).trim();
    					lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl",dom)).trim();
    					
    					System.out.println("itemCode ["+itemCode+"] , lotNo ["+lotNo+"] , lotSl["+lotSl+"]");
    					if(lotSl != null && lotSl.trim().length() > 0)
    					{
    						if(lotNo != null && lotNo.trim().length() >0)
    						{
    							if(itemCode != null && itemCode.trim().length() > 0)
                                {
    								sql = "select quantity from stock where item_code=? and lot_no=? and lot_sl=? and quantity > 0";
    								pstmt1 = conn.prepareStatement(sql);
    								pstmt1.setString(1, itemCode);
    								pstmt1.setString(2, lotNo);
    								pstmt1.setString(3, lotSl);
    								rs1 = pstmt1.executeQuery();
    								if(rs1.next())
    								{
    									qty = rs1.getDouble("quantity");
    									System.out.println("Quantity of Stock for item_code , lotNo and lotSL is ["+qty+"]");
    								}
    								if(pstmt1 != null)
    								{
    									pstmt1.close(); 
    									pstmt1 = null;
    								}
    								if(rs1 != null)
    								{
    									rs1.close();
    									rs1 = null;
    								}
    								valueXmlString.append("<no_art protect = \"1\">").append("<![CDATA[" + qty + "]]>").append("</no_art>");      					
                                }
    						}
    					}
					}
				}
				valueXmlString.append("</Detail1>\r\n");
				break;
				
				case 2:
				{
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					System.out.println("childNodeList [Case2]====> ["+childNodeList+"]");
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					valueXmlString.append("<Detail2>\r\n");
					childNodeListLength = childNodeList.getLength();
					System.out.println("currentColumn-----for case2--->>[" + currentColumn + "]");
					//Added by sarita on 15MARCH2018
					if("itm_default".equalsIgnoreCase(currentColumn.trim()))
					{
						itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom1));
						valueXmlString.append("<item_code>").append(itemCode).append("</item_code>");
						
						sql = "select descr from item where item_code=?";
					    pstmt = conn.prepareStatement(sql);
	    				pstmt.setString(1,itemCode);
	    				rs = pstmt.executeQuery();
	    				if(rs.next())
	    				{
	    					item_descr = rs.getString("descr");
	    					System.out.println("Item Description is ===========>"+item_descr);
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
	    				valueXmlString.append("<item_descr><![CDATA[" ).append(item_descr).append( "]]></item_descr>\r\n" );
						
	    				inverntoryType = checkNullAndTrim(genericUtility.getColumnValue("inventory_type", dom1));
	    				System.out.println("Inventory Type in Header is ["+inverntoryType+"]");//1 is for multiple item
	    				if(!("1".equalsIgnoreCase(inverntoryType)))
	    				{
	    					itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom1)).trim();
	    					lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no",dom1)).trim();
	    					//lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl",dom1)).trim();
	    					
	    					valueXmlString.append("<lot_no protect = \"1\"><![CDATA[" ).append(lotNo).append( "]]></lot_no>\r\n" );
	    					valueXmlString.append("<item_code protect = \"1\"><![CDATA[" ).append(itemCode).append( "]]></item_code>\r\n" );
	    				}			
					}
					else if("item_code".equalsIgnoreCase(currentColumn.trim()))
					{
						itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						System.out.println("Entered Item code is :::::::::::::"+itemCode);
						
						sql = "select descr from item where item_code=?";
					    pstmt = conn.prepareStatement(sql);
	    				pstmt.setString(1,itemCode);
	    				rs = pstmt.executeQuery();
	    				if(rs.next())
	    				{
	    					item_descr = rs.getString("descr");
	    					System.out.println("Item Description is ===========>"+item_descr);
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
	    				valueXmlString.append("<item_descr><![CDATA[" ).append(item_descr).append( "]]></item_descr>\r\n" );
					}					
				}
				valueXmlString.append("</Detail2>\r\n");
			}
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			System.out.println(":::" + getClass().getSimpleName() + "::"+ e.getMessage());
			e.getMessage(); 
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} 
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		System.out.println("ValueXmlString ["+valueXmlString.toString()+"]");
		return valueXmlString.toString();
	}

	private String errorType(Connection conn, String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
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
		catch (Exception ex)
		{
			System.out.println("Exception inside errorType method"+ex);
			ex.printStackTrace();
			throw new ITMException(ex);
		} 
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}//end of method errorType	
	
	//Commented by sarita on 24 JULY 18 [START]
/*	public int getNumOfNonDelDetail(Document dom2,int detailNo) throws ITMException
	{
		Node childNode = null;
		NodeList updateList;
		String childNodeName = "";
		String updateFlag="";
		int cntr=0;
		System.out.println("Inside getXmlDocument method!!!!!!!!!!!!!!");
		try
		{
			System.out.println("detailString value is =="+genericUtility.serializeDom(dom2));
			NodeList detailNoteList = dom2.getElementsByTagName("Detail"+detailNo);
			for(int cnt = 0;cnt<detailNoteList.getLength();cnt++)
			{
				Node pNode=detailNoteList.item(cnt);		
				childNodeName = pNode.getNodeName();
				updateFlag = getAttributeVal(pNode,"updateFlag");
				System.out.println("Before updateFlag counter is ===["+cntr+"]"+"\t"+"updateFlag [" + updateFlag + "]");
				
				if("A".equalsIgnoreCase(updateFlag))
				{
					cntr++;
				}
				System.out.println("After updateFlag counter is ===["+cntr+"]"+"\t"+"updateFlag [" + updateFlag + "]");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : : getNumOfNonDelDetail :"+e); 
			e.printStackTrace();
			throw new ITMException(e);
		}
		return cntr;
	}//end of method getNumOfNonDelDetail
	
	public String getAttributeVal(Node dom, String attribName )throws ITMException
	{
		System.out.println("Inside getAttributeVal method is !!!!!!!!!!!!!!");
		String AttribValue = null;
		try
		{
			//NodeList detailList = dom2.getChildNodes();
			NodeList detailList = dom.getChildNodes();
			System.out.println("Details NodeList is ====["+detailList+"]" + "Length is ======"+detailList.getLength());
			int detListLength = detailList.getLength();
			for(int ctr = 0; ctr < detListLength; ctr++)
			{
				Node curDetail = detailList.item(ctr);
				if(curDetail.getNodeName().equals("attribute")) 
				{
					AttribValue = curDetail.getAttributes().getNamedItem(attribName).getNodeValue();
					System.out.println("Attribute Value is =====["+AttribValue+"]");
					break;
				}
				else
				{
					continue;
				}
			}		
		}
		catch (Exception e)
		{
			System.out.println("Exception ::: searchNode :"+e); 
			throw new ITMException(e);
		}
		return AttribValue;
	}//end of method getAttributeVal*/
	//Commented by sarita on 24 JULY 18 [END]
	
/*	private int duplicateDetailRecoeds(String itemCode , String lotNo, String lotSl,Document dom2) throws ITMException
	{
		int duplicateRecord = 0;
		String updateFlag = "";
		NodeList parentNodeList = null;
		int parentNodeListLength = 0;
		//Set<List<String>> listSet = null;
		try
		{
			parentNodeList = dom2.getElementsByTagName("Detail2");	
			parentNodeListLength = parentNodeList.getLength();
			String itemCodeTmp = "",lotNoTemp="",lotSlTemp="";
			for(int ptr = 0; ptr < parentNodeListLength; ptr++)
			{
				Node pNode=parentNodeList.item(ptr);
				updateFlag = getAttributeVal(pNode,"updateFlag");
				System.out.println("Update Flag is ::"+updateFlag);
				if("D".equalsIgnoreCase(updateFlag) == false)
				{
					itemCodeTmp = genericUtility.getColumnValueFromNode("item_code", dom2);
					lotNoTemp = genericUtility.getColumnValueFromNode("lot_no", dom2);
					lotSlTemp = genericUtility.getColumnValueFromNode("lot_sl", dom2);
					
					if((lotSlTemp.equalsIgnoreCase(lotSl)))
					{
						duplicateRecord = 1;
					}
					
						
					System.out.println("Values in duplicateDetailRecoeds [itemCode ["+itemCode+"] , lotNo ["+lotNo+"] , [lotSl ["+lotSl+"]] ");
				}			
			}		
		}
		catch(Exception e)
		{
			System.out.println("Exception in method duplicateDetailRecoeds :"+e);
			throw new ITMException(e);
		}
		return duplicateRecord;
	}*/
	
}//end of class SRLContainerIC














