/********************************************************
	Title 	 : DistRcpExShIC
	Date  	 : 11/MAR/15
	Developer: Pankaj R.
 ********************************************************/


package ibase.webitm.ejb.dis;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.ejb.dis.DistCommon;
//import ibase.webitm.ejb.fin.FinCommon;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.util.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
//import java.security.AllPermission;
//import java.security.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ejb.Stateless; // added for ejb3


@Stateless // added for ejb3

public class DistRcpExShIC extends ValidatorEJB implements DistRcpExShICLocal, DistRcpExShICRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();

	//method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlStrling :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}
	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "",sql1="";
		String siteCode = "";
		String updateStatus="";
		String tranCode="",remarks="";
		String discrIdDate="",discrResDate="",noArtActual="";
		String keyFlag="",fldValue="";
		String tranIdRcp="",tranId="",reasCode="",status="";
		String tranIdRcpHdr = "",lineNoRcp="",remarks2="",objName="",tempActualQty="";
		
		int ctr=0;
		int cnt = 0;
		int noArt=0;
		double qtyRcp=0.0,qtyActual=0.0;
		double actualQtyInt=0.0;
		int currentFormNo = 0;
		int childNodeListLength;

		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		//String siteCodeShip="";  
				
		System.out.println(">><<<<<<<<<<<<<<editFlag"+editFlag);
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					
					if(childNodeName.equalsIgnoreCase("tran_id"))
					{ 
						sql1="select key_flag from transetup where tran_window='w_distrcp_exsh'";
						pstmt1=conn.prepareStatement(sql1);
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							keyFlag=rs1.getString("key_flag");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
											
						if(keyFlag.equalsIgnoreCase("M"))
						{
							tranId= genericUtility.getColumnValue("tran_id", dom);
							if(tranId == null || (tranId.length() == 0))
							{
								errList.add("VMTRANID");
								errFields.add(childNodeName.toLowerCase());
							}
							if(tranId!= null && (tranId.trim().length() > 0))
							{
								sql = " select count(*) from distord_rcp where tran_id = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,tranId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt =  rs.getInt(1);
									if(cnt == 0) 
									{
										errCode = "VMTRNIDINV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());	
									}									
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("tran_id__rcp"))
					{
						tranIdRcp =genericUtility.getColumnValue("tran_id__rcp", dom);
						if(tranIdRcp == null || tranIdRcp.trim().length() == 0)
						{
							errCode = "VMTRIDRCP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from distord_rcp where tran_id= ? and confirmed='Y'";  
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRcp);
							rs = pstmt.executeQuery();
							if(rs.next())
								{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VTPRCPID1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						System.out.println("Edit Flag>>>>"+editFlag);
						if(editFlag.equalsIgnoreCase("A"))
						{
							System.out.println("Edit Flag"+editFlag);
							sql = " select count(1) from distrcp_exsh_hdr where tran_id__rcp= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRcp);
							rs = pstmt.executeQuery();
							if(rs.next())
								{
								cnt =  rs.getInt(1);
								if(cnt > 0) 
								{
									errCode = "VTPRCPID2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						/*else if (editFlag.equalsIgnoreCase("E"))
						{
							String tranIdRcpOld="";
							tranIdRcp=genericUtility.getColumnValue("tran_id__rcp", dom).trim();
							tranId=genericUtility.getColumnValue("tran_id", dom);
							System.out.println("Edit Flag"+editFlag);
							System.out.println("Tran Id Rcp"+tranIdRcp);
							System.out.println("Tran Id"+tranId);
							sql="select count(1) from distrcp_exsh_hdr where tran_id__rcp= ? and tran_id not in (?)";
							//sql="select tran_id__rcp from Distrcp_Exsh_Hdr where tran_id=?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRcp);
							pstmt.setString(2,tranId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								//tranIdRcpOld=rs.getString("tran_id__rcp").trim();
								cnt =  rs.getInt(1);
								if(cnt > 0) 
								{
									errCode = "VTPRCPID";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
							}
							//System.out.println("Tran Id Rcp Old :"+tranIdRcpOld);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(!tranIdRcp.equalsIgnoreCase(tranIdRcpOld))
							{
								errCode = "VTPRCPID2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
								
						}*/
						
					}
					else if(childNodeName.equalsIgnoreCase("site_code"))
					{ 
						siteCode = genericUtility.getColumnValue("site_code", dom);
						if(siteCode == null || (siteCode.length() == 0))
						{
							errList.add("NULLSITECD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCode != null && (siteCode.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("tran_code"))
					{ 
						tranCode = genericUtility.getColumnValue("tran_code", dom);
						
						if(tranCode != null && (tranCode.trim().length() > 0))
						{
							sql = " select count(*) from transporter where tran_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VTTRANCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				}			
				break;
				
			case 2 : 
				parentNodeList = dom1.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				System.out.println("parentNode["+parentNode+"]");
				objName = this.getObjName(parentNode);
				System.out.println("objName["+objName+"]");
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				
				tranIdRcpHdr = genericUtility.getColumnValue("tran_id__rcp", dom1 );
				System.out.println("tranIdRcpHdr==*****==>"+tranIdRcpHdr);
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("tran_id__rcp"))
					{    
						tranIdRcp = genericUtility.getColumnValue("tran_id__rcp", dom);
						if(tranIdRcp == null || tranIdRcp.trim().length() == 0)
						{
							errCode = "VMTRIDRCP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*)from distord_rcp where tran_id= ?";      
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRcp);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMDISRCP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close(); rs = null;
							pstmt.close(); 
							pstmt = null;
						}
						if(!(tranIdRcp.equalsIgnoreCase(tranIdRcpHdr)))
						{
							errCode = "VMDISRCP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
							
						
						
					}
					else if(childNodeName.equalsIgnoreCase("line_no__rcp"))
					{    
						lineNoRcp = genericUtility.getColumnValue("line_no__rcp", dom);
						tranIdRcp = genericUtility.getColumnValue("tran_id__rcp", dom);
						if(lineNoRcp == null || lineNoRcp.trim().length() == 0)
						{
							errCode = "VTLINEBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}else{	
						sql = "select count(1) from distord_rcpdet where tran_id  = ? and line_no = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,tranIdRcp);
						pstmt.setString(2,lineNoRcp);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( cnt == 0 )
						{
							errCode = "VTVOULN1";										
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							java.util.HashSet<String> rcpData=new java.util.HashSet<String>();
							for(int i =0; i< dom2.getElementsByTagName("Detail2").getLength();i++)
							{
								updateStatus = checkNull(getCurrentUpdateFlag(dom2.getElementsByTagName("Detail2").item(i)));
								System.out.println("lineno ="+genericUtility.getColumnValueFromNode("line_no",dom2.getElementsByTagName("Detail2").item(i)));
								tranIdRcp =  checkNull(genericUtility.getColumnValueFromNode("tran_id__rcp",dom2.getElementsByTagName("Detail2").item(i)));
								lineNoRcp =  checkNull(genericUtility.getColumnValueFromNode("line_no__rcp",dom2.getElementsByTagName("Detail2").item(i)));
								System.out.println("tranIdRcp = ["+tranIdRcp+"]"+"Update Status =[ "+updateStatus+"]"+"lineNoRcp["+lineNoRcp+"]");
																							
								if(!updateStatus.equalsIgnoreCase("D"))
								{								
									if ( ! rcpData.contains(tranIdRcp.trim()+"@"+lineNoRcp.trim()))
									{
										rcpData.add(tranIdRcp.trim()+"@"+lineNoRcp.trim());										
									}
									else
									{	errCode = "VTDUPRCPLN";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									}										
									System.out.println("rcpData LIST["+rcpData+"]");
								}
							}
						}
						}
					}
					else if(childNodeName.equalsIgnoreCase("qty_actual"))
					{    
						tempActualQty = genericUtility.getColumnValue("qty_actual", dom); 
						if(tempActualQty!=null && tempActualQty.trim().length() > 0)
						{
							actualQtyInt=Double.parseDouble(tempActualQty);
						}
						if((tempActualQty == null || tempActualQty.trim().length() == 0) || (actualQtyInt <= 0)) 
						{
							errCode = "INVQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}	
					}
					else if(childNodeName.equalsIgnoreCase("reas_code"))
					{   
						System.out.println("XTRAPARAM>>>>>>>>"+xtraParams);
						reasCode = genericUtility.getColumnValue("reas_code", dom);
						qtyRcp=Double.valueOf(genericUtility.getColumnValue("qty_rcp", dom)==null?"0":genericUtility.getColumnValue("qty_rcp", dom));
						qtyActual = Double.valueOf(genericUtility.getColumnValue("qty_actual", dom)==null?"0":genericUtility.getColumnValue("qty_actual", dom));
						System.out.println("Reason code :"+reasCode);
						
						if(((qtyRcp-qtyActual) > 0 || (qtyRcp-qtyActual) < 0) && (reasCode == null || reasCode.trim().length() == 0))
						{
							errCode = "VTREASCOD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						if(reasCode != null && (reasCode.trim().length() > 0))
						{
							sql = " select fld_value from gencodes where fld_name='REAS_CODE' and mod_name='W_DISTRCP_EXSH' ";
							pstmt =  conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								fldValue =  rs.getString("fld_value");
								System.out.println("FIELD VALUE"+fldValue);
								if(!fldValue.equalsIgnoreCase(reasCode)) 
								{
									errCode = "VTREA01";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("remarks"))
					{
						remarks=genericUtility.getColumnValue("remarks", dom);
						status = genericUtility.getColumnValue("status", dom);
						qtyRcp=Double.valueOf(genericUtility.getColumnValue("qty_rcp", dom)==null?"0":genericUtility.getColumnValue("qty_rcp", dom));
						qtyActual = Double.valueOf(genericUtility.getColumnValue("qty_actual", dom)==null?"0":genericUtility.getColumnValue("qty_actual", dom));
						System.out.println("Status@@@@@@@@@@@@@ :"+status);
						if(( (qtyRcp-qtyActual) > 0) && ((remarks==null || remarks.trim().length()<=0)))
						{
							errCode = "VMREMARK1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
					}
					else if(childNodeName.equalsIgnoreCase("no_art__act"))
					{
						noArtActual = genericUtility.getColumnValue("no_art__act", dom); 
						if(noArtActual!=null && noArtActual.trim().length() > 0)
						{
							noArt=Integer.parseInt(noArtActual);
						}
						if((noArtActual == null || noArtActual.trim().length() == 0) || (noArt <= 0)) 
						{
							errCode = "VMNOART";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}						
					}
					else if(childNodeName.equalsIgnoreCase("status"))
					{
						System.out.println("In status validation");
						objName = this.getObjName(parentNode);
						if(objName.equalsIgnoreCase("distrcpcexsh_cl"))
						{
						remarks2=genericUtility.getColumnValue("remarks2", dom);
						status = genericUtility.getColumnValue("status", dom);
						System.out.println("Status@@@@@@@@@@@@@ :"+status);
						if((status.equalsIgnoreCase("O")) && (remarks2!=null || remarks.trim().length()> 0))
						{
							errCode = "VMSTOPEN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						}
					}
					else if(childNodeName.equalsIgnoreCase("remarks2"))
					{
						System.out.println("parentNode["+parentNode+"]");
						objName = this.getObjName(parentNode);
						System.out.println("DistRcp_Exsh ObjName>>>>>>>>>>>>>"+objName);
						if(objName.equalsIgnoreCase("distrcp_exsh"))
						{
						remarks2=genericUtility.getColumnValue("remarks2", dom);
						status = genericUtility.getColumnValue("status", dom);
						qtyRcp=Double.valueOf(genericUtility.getColumnValue("qty_rcp", dom)==null?"0":genericUtility.getColumnValue("qty_rcp", dom));
						qtyActual = Double.valueOf(genericUtility.getColumnValue("qty_actual", dom)==null?"0":genericUtility.getColumnValue("qty_actual", dom));
						System.out.println("Status@@@@@@@@@@@@@ :"+status);
						if(( (qtyRcp-qtyActual) > 0) && (!(status.equalsIgnoreCase("O"))) && ((remarks2==null || remarks2.trim().length()<=0)))
						{
							errCode = "VMREMARK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if(status.equalsIgnoreCase("O")&& (remarks2!=null && remarks2.trim().length()>0))
						{
							errCode = "VMSTATOPN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						}
						if(objName.equalsIgnoreCase("distrcpcexsh_cl"))
						{
							if(status.equalsIgnoreCase("C")&& (remarks2==null || remarks2.trim().length() <= 0))
							{
								errCode = "VMREMARK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if(status.equalsIgnoreCase("O")&& (remarks2!=null && remarks2.trim().length()>0))
							{
								errCode = "VMSTATOPN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
													
					}
					else if(childNodeName.equalsIgnoreCase("discr_id_date"))
					{
						remarks=genericUtility.getColumnValue("remarks", dom);
						discrIdDate=genericUtility.getColumnValue("discr_id_date", dom);
						if((remarks!=null && remarks.trim().length() > 0) && (discrIdDate==null || discrIdDate.trim().length() < 0))
						{
							errCode = "VTDISDATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("discr_res_date"))
					{
						remarks2=genericUtility.getColumnValue("remarks2", dom);
						discrResDate=genericUtility.getColumnValue("discr_res_date", dom);
						System.out.println("Remarks@@@@@@@@@@@@@ :"+remarks2);
						System.out.println("Discrepancy Res Date@@@@@@@@@@@@@ :"+discrResDate);
						if((remarks2!=null && remarks2.trim().length() > 0) && (discrResDate==null || discrResDate.trim().length() < 0))
						{
							errCode = "VTDISRDATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
			}//end switch
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
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
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		}//end try
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
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
				}
				conn = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("XML STRING@@@@@@@@@");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("XML STRING@@@@@@@@@@@@@!1"+xmlString1);
			dom2 = parseString(xmlString2);
			System.out.println("XML STRING@@@@@@@@@@@@@@@2"+xmlString2);
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [JvVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("valueXmlStringvalueXmlStrin ST"+valueXmlString);
		return valueXmlString;
	}
	
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		SimpleDateFormat sdf =null;
		Timestamp timestamp = null,expDate=null,mfgDate=null;
		Timestamp lrDate=null,tranDate=null,gpDate=null,tranDate2=null;
		String currDate="";
		String childNodeName = null;
		String siteCode = "";
		String itemCode = "";
		String itemDescr = "";
		String qtyActualTemp="";
		//String lineNoRcp = "";
		String chgUser = "";
		String chgTerm = "";
		String sql = "",sql1="";
		String siteDescr = "",remarks2="",status="";
		String locCode = "",lrDateStr="",gpDateStr="";
		String locDescr = "",locType="",descr1="";
		String siteCodeMfg="",suppCodeMfg="";
		String tranIdRcp="",distOrder="",tranId="",tranCode="",lrNo="",transMode="",grossWeight="";
		String tareWeight="",netWeight="",noArt="",gpNo="",tranName="",tranIdIss="",noArtAct="";
		String lotNo = "",lotSl="";
		double quantity=0.0,qtyActual=0.0,qtyRcp=0.0,rate=0.0,shortVal=0.0,shortQty=0;
		String lineNo = "";
		int ctr = 0;
		int currentFormNo = 0;
		String siteCodeShip="",siteCodeShipDescr="",unit="",columnValue="";  
		
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null ;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
				
		try
		{			 
			System.out.println("@@@@@@@@@@@ itemchange method called for ---->>>>["+currentColumn+"]");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());	
		
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}

			siteCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));

			siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
			System.out.println("@@@@@@ 1 siteCode["+siteCode+"]::::::siteDescr["+siteDescr+"]");
			chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
			currDate = (sdf.format(timestamp).toString()).trim();
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			switch(currentFormNo)
			{
			case 1 : 
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if(childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();  
						}
					}
					ctr++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("@@@@@@  itm_default itemchange called");
					System.out.println("@@@@@@ siteCode["+siteCode+"]");
					siteCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode")); 
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
					System.out.println("@@@@@@ 1 siteCode["+siteCode+"]::::::siteDescr["+siteDescr+"]");
					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</descr>"); // 25/3/15end
					valueXmlString.append("<tran_date>").append("<![CDATA[" + currDate + "]]>").append("</tran_date>");
					valueXmlString.append("<add_date>").append("<![CDATA[" + currDate + "]]>").append("</add_date>");
					valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
					valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
					
				}//end of if
				if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					valueXmlString.append("<chg_date>").append("<![CDATA[" + currDate + "]]>").append("</chg_date>");
					tranIdRcp = genericUtility.getColumnValue("tran_id__rcp", dom);
					System.out.println("Tran Id Rcp>>>>>>>>"+tranIdRcp);
					valueXmlString.append("<tran_id__rcp protect = \"1\">").append("<![CDATA[" + tranIdRcp + "]]>").append("</tran_id__rcp>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_id__rcp"))
				{
					System.out.println("@@@@@@  tran_id__rcp itemchange called");
					tranId = genericUtility.getColumnValue("tran_id__rcp", dom);
				
					distOrder = findValue(conn, "dist_order" ,"distord_rcp", "tran_id", tranId);
					valueXmlString.append("<dist_order>").append("<![CDATA[" +  distOrder + "]]>").append("</dist_order>");
					
					siteCode = findValue(conn, "site_code" ,"distord_rcp", "tran_id",tranId);
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);

					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</descr>");
					System.out.println("@@@@@ siteCode["+siteCode+"]siteCodeDescr["+siteDescr+"]");
					
					siteCodeShip = findValue(conn, "site_code__ship" ,"distord_rcp", "tran_id",tranId);
					siteCodeShipDescr = findValue(conn, "descr" ,"site", "site_code", siteCodeShip);
					
					valueXmlString.append("<site_code__ship>").append("<![CDATA[" +  siteCodeShip + "]]>").append("</site_code__ship>");
					valueXmlString.append("<descr_1>").append("<![CDATA[" +  siteCodeShipDescr + "]]>").append("</descr_1>");
					System.out.println("@@@@@ siteCodeShip["+siteCodeShip+"]siteCodeShipDescr["+siteCodeShipDescr+"]");
					
					tranCode = findValue(conn, "tran_code", "distord_rcp", "tran_id", tranId);
					tranName = findValue(conn, "tran_name", "transporter", "tran_code", tranCode);
					valueXmlString.append("<tran_code>").append("<![CDATA[" +  tranCode + "]]>").append("</tran_code>");
					valueXmlString.append("<tran_name>").append("<![CDATA[" +  tranName+ "]]>").append("</tran_name>");
										
					sql = "select tran_date,lr_no,lr_date,gp_date,gp_no,trans_mode,gross_weight,tare_weight,net_weight,no_art,case when DISTORD_RCP.tran_id__iss is null then DISTORD_RCP.issue_ref else DISTORD_RCP.tran_id__iss end as issue_ref from distord_rcp where tran_id=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						tranDate=rs.getTimestamp("tran_date");
						lrNo= checkNull(rs.getString("lr_no"));
						lrDate= rs.getTimestamp("lr_date");
						gpDate=rs.getTimestamp("gp_date");
						gpNo=checkNull(rs.getString("gp_no"));
						transMode= rs.getString("trans_mode");
						grossWeight = rs.getString("gross_weight");
						tareWeight = rs.getString("tare_weight");
						netWeight = rs.getString("net_weight");
						noArt = rs.getString("no_art");
//						tranIdIss = rs.getString("tran_id__iss");
						tranIdIss = rs.getString("issue_ref");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
									
					if(lrDate != null)
					{
						lrDateStr=sdf.format(lrDate).toString();
						System.out.println("lrDateStr@@@@@@@"+lrDateStr);
					}
					else
					{
//						String s1="";
						lrDateStr="";						
						System.out.println("lrDateStr@@@@@@@"+lrDateStr);
					}
					valueXmlString.append("<lr_date>").append("<![CDATA[" +  lrDateStr + "]]>").append("</lr_date>");
					
					if(gpDate != null)
					{
						gpDateStr=sdf.format(gpDate).toString();
					}
					else
					{
						String s1="";
						gpDateStr=s1;						
						System.out.println("gpDateStr@@@@@@@"+gpDateStr);
					}
					valueXmlString.append("<gp_date>").append("<![CDATA[" +  gpDateStr + "]]>").append("</gp_date>");
					if(tranDate == null)
					{
						valueXmlString.append("<tran_date_1>").append("<![CDATA[" + currDate + "]]>").append("</tran_date_1>");
					}
					else
					{
						valueXmlString.append("<tran_date_1>").append("<![CDATA[" +  sdf.format(tranDate) + "]]>").append("</tran_date_1>");
					}
					valueXmlString.append("<lr_no>").append("<![CDATA[" +  lrNo + "]]>").append("</lr_no>");
					valueXmlString.append("<gp_no>").append("<![CDATA[" +  gpNo + "]]>").append("</gp_no>");
					valueXmlString.append("<trans_mode>").append("<![CDATA[" +  transMode + "]]>").append("</trans_mode>");
					valueXmlString.append("<gross_weight>").append("<![CDATA[" +  grossWeight + "]]>").append("</gross_weight>");
					valueXmlString.append("<tare_weight>").append("<![CDATA[" +  tareWeight + "]]>").append("</tare_weight>");
					valueXmlString.append("<net_weight>").append("<![CDATA[" +  netWeight + "]]>").append("</net_weight>");
					valueXmlString.append("<no_art>").append("<![CDATA[" +  noArt + "]]>").append("</no_art>");
					valueXmlString.append("<site_code__ship>").append("<![CDATA[" +  siteCodeShip + "]]>").append("</site_code__ship>");
					valueXmlString.append("<descr_1>").append("<![CDATA[" +  siteCodeShipDescr + "]]>").append("</descr_1>");
//					valueXmlString.append("<tran_id__iss>").append("<![CDATA[" +  tranIdIss + "]]>").append("</tran_id__iss>");
					valueXmlString.append("<issue_ref>").append("<![CDATA[" +  tranIdIss + "]]>").append("</issue_ref>");
					
					sql1="Select tran_date from distord_iss where tran_id= ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, tranIdIss);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						tranDate2=rs1.getTimestamp("tran_date");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					if(tranDate2 == null)
					{
						valueXmlString.append("<tran_date_2>").append("<![CDATA[" + currDate + "]]>").append("</tran_date_2>");
					}
					else
					{
						valueXmlString.append("<tran_date_2>").append("<![CDATA[" +  sdf.format(tranDate2) + "]]>").append("</tran_date_2>");
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = genericUtility.getColumnValue("site_code", dom);
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
					System.out.println("@@@@@ siteCode["+siteCode+"]siteDescr["+siteDescr+"]");
					valueXmlString.append("<descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode = genericUtility.getColumnValue("tran_code", dom);
					tranName = findValue(conn, "tran_name", "transporter", "tran_code", tranCode);
					System.out.println("@@@@@ Tran Code["+tranCode+"]Tran Name["+tranName+"]");
					valueXmlString.append("<tran_name>").append("<![CDATA[" +  tranName+ "]]>").append("</tran_name>");
				}
				valueXmlString.append("</Detail1>");
				break;
				
			case 2 : 
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if(childNode.getFirstChild() != null)
						{
						}
					}
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				System.out.println("Entered in CASE2");
				tranId = genericUtility.getColumnValue("tran_id", dom1);
				tranIdRcp=genericUtility.getColumnValue("tran_id__rcp", dom1);
				System.out.println("tranID >>>>>>>> "+tranId);
				System.out.println("TRANIDRCP@ >>>>>>>> "+tranIdRcp);
			
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
//					status = genericUtility.getColumnValue("status", dom);
//					valueXmlString.append("<remarks2 protect = \"1\">").append("<![CDATA[" + remarks2 + "]]>").append("</remarks2>");
					tranIdRcp = genericUtility.getColumnValue("tran_id__rcp", dom1, "1");
					valueXmlString.append("<tran_id__rcp protect = \"1\">").append("<![CDATA[" + tranIdRcp + "]]>").append("</tran_id__rcp>");
					valueXmlString.append("<no_art__act>").append("<![CDATA[0]]>").append("</no_art__act>");
				}//end of if
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("line_no__rcp"))
				{
					tranIdRcp = checkNull(genericUtility.getColumnValue("tran_id__rcp", dom));
					lineNo = checkNull(genericUtility.getColumnValue("line_no__rcp", dom));
					System.out.println("Line NO.>>>>>>>>>"+lineNo);
					
					if (lineNo!=null && lineNo.trim().length() > 0 )
					{
						sql = "select unit,quantity,lot_no,lot_sl,no_art,site_code__mfg,mfg_date,exp_date,gross_weight,tare_weight,net_weight,trans_mode,supp_code__mfg,item_code,loc_code,rate from distord_rcpdet where tran_id= ? and line_no= ? ";//pankaj 6/5/15
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,tranIdRcp);
						pstmt.setString(2,lineNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							unit = rs.getString("unit");
							quantity = rs.getDouble("quantity");
							lotNo = checkNull(rs.getString("lot_no"));
							lotSl = checkNull(rs.getString("lot_sl"));
							noArt = rs.getString("no_art");
							siteCodeMfg= rs.getString("site_code__mfg");
							mfgDate= rs.getTimestamp("mfg_date");
							expDate= rs.getTimestamp("exp_date");
							grossWeight= rs.getString("gross_weight");
							tareWeight= rs.getString("tare_weight");
							netWeight= rs.getString("net_weight");
							transMode= rs.getString("trans_mode");
							suppCodeMfg= rs.getString("supp_code__mfg");	
							itemCode=rs.getString("item_code");
							locCode=rs.getString("loc_code");
							rate=rs.getDouble("rate");
							itemDescr = findValue(conn, "descr" ,"item", "item_code", itemCode);
							locDescr=findValue(conn, "descr", "location", "loc_code", locCode);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						
						valueXmlString.append("<item_code>").append("<![CDATA[" +  itemCode + "]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" +  itemDescr + "]]>").append("</item_descr>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" +  locCode + "]]>").append("</loc_code>");
						valueXmlString.append("<descr>").append("<![CDATA[" +  locDescr + "]]>").append("</descr>");
						valueXmlString.append("<unit>").append("<![CDATA[" +  unit + "]]>").append("</unit>");
						valueXmlString.append("<qty_rcp>").append("<![CDATA[" +  quantity + "]]>").append("</qty_rcp>");
						valueXmlString.append("<qty_actual>").append("<![CDATA[" +  quantity + "]]>").append("</qty_actual>");
						valueXmlString.append("<lot_no>").append("<![CDATA[" +  lotNo + "]]>").append("</lot_no>");
						valueXmlString.append("<lot_sl>").append("<![CDATA[" +  lotSl + "]]>").append("</lot_sl>");
						valueXmlString.append("<no_art>").append("<![CDATA[" +  noArt + "]]>").append("</no_art>");
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[" +  siteCodeMfg + "]]>").append("</site_code__mfg>");
						if(mfgDate == null)
						{
							valueXmlString.append("<mfg_date>").append("<![CDATA[ ]]>").append("</mfg_date>");
						}
						else
						{
						valueXmlString.append("<mfg_date>").append("<![CDATA[" +  sdf.format(mfgDate) + "]]>").append("</mfg_date>");
						}
						if(expDate == null)
						{
							valueXmlString.append("<exp_date>").append("<![CDATA[ ]]>").append("</exp_date>");
						}
						else
						{
							valueXmlString.append("<exp_date>").append("<![CDATA[" +  sdf.format(expDate) + "]]>").append("</exp_date>");
						}
						valueXmlString.append("<gross_weight>").append("<![CDATA[" +  grossWeight + "]]>").append("</gross_weight>");
						valueXmlString.append("<tare_weight>").append("<![CDATA[" +  tareWeight + "]]>").append("</tare_weight>");
						valueXmlString.append("<net_weight>").append("<![CDATA[" +  netWeight + "]]>").append("</net_weight>");
						valueXmlString.append("<trans_mode>").append("<![CDATA[" +  transMode + "]]>").append("</trans_mode>");
						valueXmlString.append("<supp_code__mfg>").append("<![CDATA[" +  suppCodeMfg + "]]>").append("</supp_code__mfg>");
						valueXmlString.append("<rate>").append("<![CDATA[" +  rate + "]]>").append("</rate>");
						
						shortVal=((qtyRcp - qtyActual) * rate);
						shortQty=((quantity) - (quantity));
						System.out.println("shortQty>>>>>>>>>>"+shortQty);
						valueXmlString.append("<short_val>").append("<![CDATA[" +  shortVal + "]]>").append("</short_val>");
						valueXmlString.append("<short_qty>").append("<![CDATA[" +  shortQty + "]]>").append("</short_qty>");
						
						locType = findValue(conn, "loc_type" ,"item", "item_code", itemCode);
						System.out.println(">>>>>>Loc Type"+locType);
						
						sql = "select descr from gencodes where fld_value=?  and fld_name= ?"; 
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,locType);
						pstmt.setString(2,"LOC_TYPE");
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr1=rs.getString("descr");
							System.out.println("Description>>>>"+descr1);
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						valueXmlString.append("<loc_type>").append("<![CDATA[" +  locType + "]]>").append("</loc_type>");
						valueXmlString.append("<gencodes_descr>").append("<![CDATA[" +  descr1 + "]]>").append("</gencodes_descr>");
						valueXmlString.append("<no_art__act>").append("<![CDATA[0]]>").append("</no_art__act>");
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("qty_actual"))
				{
					qtyActualTemp=genericUtility.getColumnValue("qty_actual", dom);
					if(qtyActualTemp==null || qtyActualTemp.trim().length() <= 0)
					{
						valueXmlString.append("<qty_actual>").append("<![CDATA[0]]>").append("</qty_actual>");
					}
					qtyRcp=Double.valueOf(genericUtility.getColumnValue("qty_rcp", dom)==null?"0":genericUtility.getColumnValue("qty_rcp", dom));
					qtyActual = Double.valueOf(genericUtility.getColumnValue("qty_actual", dom)==null?"0":genericUtility.getColumnValue("qty_actual", dom));
					rate = Double.valueOf(genericUtility.getColumnValue("rate", dom)==null?"0":genericUtility.getColumnValue("rate", dom));;
					
					System.out.println("Quantity RCp["+qtyRcp+"]Quantity Actual["+qtyActual+"]Rate["+rate+"]");
					
					shortVal=((qtyRcp - qtyActual) * rate);
					shortQty=((qtyRcp) - (qtyActual));
					
					System.out.println("Short Value>>>>>>>>>"+shortVal);
					System.out.println("Short Quantity>>>>>>>>>"+shortQty);
					
					valueXmlString.append("<short_val>").append("<![CDATA[" +  getRequiredDecimal(shortVal,3) + "]]>").append("</short_val>");
					valueXmlString.append("<short_qty>").append("<![CDATA[" +  getRequiredDecimal(shortQty,-1) + "]]>").append("</short_qty>");
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("no_art__act"))
				{
					noArtAct=genericUtility.getColumnValue("no_art__act", dom);
					if(noArtAct==null || noArtAct.trim().length() <= 0)
					{
						valueXmlString.append("<no_art__act>").append("<![CDATA[0]]>").append("</no_art__act>");
					}
				}
				valueXmlString.append("</Detail2>");
				break;
			}//end of switch-case
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
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
	}//end of itemChanged
	
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
	}//end of errorType

	private String findValue(Connection conn, String columnName ,String tableName, String columnName2, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		String findValue = "";
		try
		{			
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{					
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
			if (findValue == null )
			{
				findValue = "";
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from findValue " + findValue);
		return findValue;
	}//end of findValue
	
	private String checkNull( String input )
	{
		if (input == null )
		{
			input = "";
		}
		return input;
	}//end of checkNull
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
				updateStatus =currDetailList.item(i).getAttributes().getNamedItem("updateFlag").getNodeValue();
				break;
			}
		}
		return updateStatus;
	}
	private String getObjName(Node node) throws Exception
	{
		String objName = "";
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem("objName").getNodeValue();
		System.out.println(" Object Name is-->" + objName);
		return objName;
	}
	public String getRequiredDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		String strValue = null;
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return decFormat.format(actVal);
	}

}//class ends