package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3
import ibase.webitm.ejb.ITMDBAccessEJB;

@Stateless // added for ejb3
public class CommissiondetIC extends ValidatorEJB implements CommissiondetICLocal,CommissiondetICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1,  String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			System.out.println("Inside  Dom :: =======>>>>"+xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("Inside  Dom1 :: =======>>>>"+xmlString1);
			dom2 = parseString(xmlString2);
			System.out.println("Inside  Dom2 :: =======>>>>"+xmlString2);
			System.out.println("@@@@@@@@  wfValData called !!!!!!!");
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);

		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		String errString = "";
		Connection conn = null;
		PreparedStatement pstmt = null ;

		ResultSet rs = null;

		String sql = "";
		int cnt=0;
		int currentFormNo=0;
		int childNodeListLength;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

		String userId="";
		Timestamp validUpto = null ,effFrom2=null;
		Timestamp effDate = null, newSysdate = null;
		String lineNo="",commTable="",itemSer="",itemCode="",effDateStr="",validUptoStr="",commType="",commPercOn="",commPerc="";
		String  effDateTemp=null ;	
		String descr = "",shDescr="";
		Timestamp  effDateTemp2=null ,ValidUptoLastTemp=null, ValidUptoLastTemp2=null;

		int ctr1=0,k=0,l=1;
		String dateStr=null,itemCodeTemp="",itemSerTemp="",validUptoTemp="",itemSerTempLast="",itemCodeTempLast="";
		String itminfo ="";
		java.sql.Timestamp  validUptoTemp2=null;

        String commCriteria = "";
        double maxAmount = 0.0;
        double minAmount = 0.0;
        double commPercentage = 0.0;
    	NodeList detailNodeList = null;
		NodeList detail3childNodeList = null;
		Node detail3parentNode = null;

		try
		{
			System.out.println("@@@@@@@@ wfvaldata called !!!!!!!!!!!!!!!!!!!!!!");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			 conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			if(objContext != null && objContext.trim().length()>0)
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

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();


					System.out.println("childNodeName ::["+childNodeName);

					if(childNodeName.equalsIgnoreCase("comm_table") && ("A".equalsIgnoreCase(editFlag))  )
					{
						System.out.println("validation comm_table executed");
						commTable = genericUtility.getColumnValue("comm_table",dom);
						if(commTable == null || commTable.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOMTBNUL",userId,"",conn);
							break ;
						}
						else
						{
							sql = "SELECT COUNT(1) FROM COMM_HDR WHERE COMM_TABLE = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, commTable );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt > 0 )
							{
								errString = itmDBAccessEJB.getErrorString("","VMCOMTBINV",userId,"",conn);
								break ;
							}

						}
					}
					if(childNodeName.equalsIgnoreCase("descr") && ("A".equalsIgnoreCase(editFlag)) )
					{

						descr = genericUtility.getColumnValue("descr",dom);
						System.out.println("descr === "+descr);
						if(descr == null || descr.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMDESCR",userId,"",conn);
							break ;
						}
						else
						{
							sql = "SELECT COUNT(1) FROM COMM_HDR WHERE descr = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, descr );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt > 0 )
							{
								errString = itmDBAccessEJB.getErrorString("","VMDESCEXIS",userId,"",conn);
								break ;
							}


						}
					}

					if(childNodeName.equalsIgnoreCase("sh_descr") && ("A".equalsIgnoreCase(editFlag)) )
					{

						shDescr = genericUtility.getColumnValue("sh_descr",dom);
						System.out.println("sh_descr === "+shDescr);
						if(shDescr != null && shDescr.trim().length() > 0)
						{
							sql = "SELECT COUNT(1) FROM COMM_HDR WHERE sh_descr = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, shDescr );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt > 0 )
							{
								errString = itmDBAccessEJB.getErrorString("","VMSHDECEXS",userId,"",conn);
								break ;
							}
						}
					}
					
					  if (childNodeName.equalsIgnoreCase("comm_criteria"))
					  {
						  commCriteria = this.genericUtility.getColumnValue("comm_criteria", dom);
						  System.out.println("Commision Criteria [" + commCriteria);
						  if ((commCriteria != null) && (commCriteria.trim().length() > 0))
						  {
							  detailNodeList = dom2.getElementsByTagName("Detail3");
							  System.out.println("detailNodeList ::" + detailNodeList);
							  if ((detailNodeList != null) && (detailNodeList.getLength() > 0) && (commCriteria.equalsIgnoreCase("I")))
							  {
								  errString = itmDBAccessEJB.getErrorString("", "VTRECINCOM", userId,"",conn);
								  break;
							  }
						  }

						  if ((commCriteria != null) && (commCriteria.trim().length() > 0))
						  {
							  detailNodeList = dom2.getElementsByTagName("Detail2");
							  if ((detailNodeList != null) && (detailNodeList.getLength() > 0) && (commCriteria.equalsIgnoreCase("V")))
							  {
								  errString = itmDBAccessEJB.getErrorString("", "VTRECINDET", userId,"",conn);
								  break;
							  }
						  }
					  }

				} // end for
				break;	

			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("comm_table"))
					{
						System.out.println("validation comm_table executed");
						commTable = genericUtility.getColumnValue("comm_table",dom);
						if(commTable == null || commTable.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOMTBNUL",userId,"",conn);
							break ;
						}
					}

					if(childNodeName.equalsIgnoreCase("line_no"))
					{
						System.out.println("validation line no executed");
						lineNo = genericUtility.getColumnValue("line_no",dom);
						if(lineNo == null || lineNo.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VNLINENONU",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("item_ser"))
					{
						System.out.println("validation item series executed");
						itemSer = genericUtility.getColumnValue("item_ser",dom);
						itemCode = genericUtility.getColumnValue("item_code",dom);
						if( itemSer == null || itemSer.trim().length() == 0)
						{
							if( ( itemSer == null || itemSer.trim().length() == 0 ) && ( itemCode == null || itemCode.trim().length() == 0))
							{
								errString = itmDBAccessEJB.getErrorString("","VMICOSERNU",userId,"",conn);
								break ;
							}
						}
						else
						{
							if( itemSer != null && itemSer.trim().length() > 0  && itemCode != null && itemCode.trim().length() > 0)
							{
								errString = itmDBAccessEJB.getErrorString("","VMICOSERIN",userId,"",conn);
								break ;
							}
							
							sql = "SELECT COUNT(1) FROM ITEMSER WHERE ITEM_SER = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString( 1, itemSer );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errString = itmDBAccessEJB.getErrorString("","VMITMSRIN2",userId,"",conn);
								break ;
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("item_code"))
					{
						System.out.println("validation item_code executed");
						itemCode = genericUtility.getColumnValue("item_code",dom);
						itemSer = genericUtility.getColumnValue("item_ser",dom);

						if( itemCode == null || itemCode.trim().length() == 0)
						{
							if( ( itemSer == null || itemSer.trim().length() == 0 ) && ( itemCode == null || itemCode.trim().length() == 0))
							{
								errString = itmDBAccessEJB.getErrorString("","VMICOSERNU",userId,"",conn);
								break ;
							}
						}
						else
						{
								if( itemSer != null && itemSer.trim().length() > 0  && itemCode != null && itemCode.trim().length() > 0)
								{
									errString = itmDBAccessEJB.getErrorString("","VMICOSERIN",userId,"",conn);
									break ;
								}
								
								sql = "SELECT COUNT(1) FROM ITEM WHERE ITEM_CODE = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString( 1, itemCode );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
									errString = itmDBAccessEJB.getErrorString("","VMITMSRINV",userId,"",conn);
									break ;
								}
							
						}
					}

					else if(childNodeName.equalsIgnoreCase("eff_date"))
					{
						effDateStr =genericUtility.getColumnValue("eff_date",dom);
						validUptoStr =genericUtility.getColumnValue("valid_upto",dom);
						System.out.println("@@@@@@@@@@ date validation : eff date");

						if(effDateStr==null || effDateStr.trim().length()==0)
						{
							errString = getErrorString("eff_from","VMEFFDTNUL",userId);
							break;
						}
						else
						{
							if(validUptoStr == null || validUptoStr.trim().length()==0)
							{
								errString = itmDBAccessEJB.getErrorString("","VMVALDTNUL",userId,"",conn);
								break ;
							}
							effDate= Timestamp.valueOf(genericUtility.getValidDateString(effDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							validUpto= Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

							commTable = genericUtility.getColumnValue("comm_table", dom);
							lineNo = genericUtility.getColumnValue("line_no",dom);

							System.out.println("effDate date is :=>  " + effDate);
							System.out.println("validUpto date is :=>  " + validUpto);

							if( !validUpto.after(effDate) && !( validUpto.equals(effDate) ) )
							{
								System.out.println("effFrom.after(validUpto");
								errString = getErrorString("","INVEFDATE ",userId);
								System.out.println("errString befor break" +errString);
								break;
							}							
						}
					}

					else if(childNodeName.equalsIgnoreCase("valid_upto"))
					{
						System.out.println("@@@@@@@@@@ date validation : valid upto");
						effDateStr =genericUtility.getColumnValue("eff_date",dom);
						validUptoStr =genericUtility.getColumnValue("valid_upto",dom);

						if((effDateStr != null) && (validUptoStr != null))
						{
							effDate= Timestamp.valueOf(genericUtility.getValidDateString(effDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							validUpto= Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("effDate date is :=>  " + effDate);
							System.out.println("validUpto date is :=>  " + validUpto);

							if( !validUpto.after(effDate) && !( validUpto.equals(effDate) ) )
							{
								//Valid upto cannot be less than effective date
								errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId,"",conn);
								break ;
							}
							
						}
						else
						{
							//Date cannot be empty. Please enter valid date.
							errString = itmDBAccessEJB.getErrorString("","VMVALDTNUL",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("comm_type"))
					{
						System.out.println("validation comm_type executed");
						commType = genericUtility.getColumnValue("comm_type",dom);
						if(commType == null || commType.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOMTYPNU",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("comm_perc"))
					{
						System.out.println("validation comm perc executed");
						commPerc = genericUtility.getColumnValue("comm_perc",dom);
						if(commPerc == null || commPerc.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOMPERNU",userId,"",conn);
							break ;
						}
					}
					else if(childNodeName.equalsIgnoreCase("comm_perc__on"))
					{

						System.out.println("validation comm_perc__on executed");
						commPercOn = genericUtility.getColumnValue("comm_perc__on",dom);
						if(commPercOn == null || commPercOn.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOPONNUL",userId,"",conn);
							break ;
						}
					}
					   //changed by Nasruddin khan 20/JUL/2016  [D16DFOR001] Start
					commCriteria =	genericUtility.getColumnValue("comm_criteria", dom1);
					System.out.println("commCriteria ::: ["+commCriteria);
					if(commCriteria != null && commCriteria.trim().length()>0){
						
						if(!"I".equalsIgnoreCase(commCriteria)){
							errString = itmDBAccessEJB.getErrorString("","VMINTRNREC",userId,"",conn);
						    break;
						}
					}
					System.out.println("Inside detail 2 validation :::::::::");
				} 
				break;
				// changed by Nasruddin khan [20/JUL/16 D16DFOR001] END
				
				// changed by Nasruddin khan [19/JUL/16 D16DFOR001] START
			case 3:
				
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{

					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("comm_table"))//start comm_table
					{
						System.out.println("validation comm_table executed");
						commTable = genericUtility.getColumnValue("comm_table",dom);
						if(commTable == null || commTable.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOMTBNUL",userId,"",conn);
							break ;
						}
					} // end comm_table
                   
					if(childNodeName.equalsIgnoreCase("line_no")) // start line_no
					{
						System.out.println("validation line no executed");
						lineNo = genericUtility.getColumnValue("line_no",dom);
						if(lineNo == null || lineNo.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VNLINENONU",userId,"",conn);
							break ;
						}
					} //end line_no
					else if(childNodeName.equalsIgnoreCase("comm_type")) // start  comm_type
					{
						System.out.println("validation comm_type executed");
						commType = genericUtility.getColumnValue("comm_type",dom);
						if(commType == null || commType.trim().length() == 0)
						{
							errString = itmDBAccessEJB.getErrorString("","VMCOMTYPNU",userId,"",conn);
							break ;
						}
					} // end  comm_type
					else if(childNodeName.equalsIgnoreCase("comm_perc")) // start  comm_perc
					{
						System.out.println("validation comm perc executed");
						commPercentage = Double.parseDouble(checkNllOrBlank(genericUtility.getColumnValue("comm_perc",dom)));
						if("P".equalsIgnoreCase(commType)){
							
							if( commPercentage <= 0 || commPercentage > 100 ){
		 						System.out.println("commission  persentace should be greter then zero and less then hundread");
		 						errString = itmDBAccessEJB.getErrorString("","VTCOMAMTPR",userId,"",conn);
								break ;
							  }
						}
						if("F".equalsIgnoreCase(commType)){
							
							if ( commPercentage <= 0){
								
								System.out.println("Fixed   vALUE sHOULD BE GREATER THEN ZERO ");
		 						errString = itmDBAccessEJB.getErrorString("","VTCOMAMTFX",userId,"",conn);
								break ;
							}
							
						}
						
					}// end  comm_perc
					else if(childNodeName.equalsIgnoreCase("eff_date")) //start eff_date
					{
						effDateStr =genericUtility.getColumnValue("eff_date",dom);
						validUptoStr =genericUtility.getColumnValue("valid_upto",dom);
						commTable =	genericUtility.getColumnValue("comm_table", dom1);
						System.out.println("@@@@@@@@@@ date validation : eff date");

						if(effDateStr==null || effDateStr.trim().length()==0)
						{
							errString = getErrorString("eff_from","VMEFDTNULL",userId);
							break;
						}
						else
						{
							if(validUptoStr == null || validUptoStr.trim().length()==0)
							{
								errString = itmDBAccessEJB.getErrorString("","VMVALDTNUL",userId,"",conn);
								break ;
							}
							effDate= Timestamp.valueOf(genericUtility.getValidDateString(effDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							validUpto= Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

							

							System.out.println("effDate date is :=>  " + effDate);
							System.out.println("validUpto date is :=>  " + validUpto);

							if( !validUpto.after(effDate) && !( validUpto.equals(effDate) ) )
							{
								System.out.println("effFrom.after(validUpto");
								errString = getErrorString("","INVEFDATE ",userId);
								System.out.println("errString befor break" +errString);
								break;
							}

			 				 String effdatestr = genericUtility.getColumnValue( "eff_date", dom ) != null ? genericUtility.getColumnValue( "eff_date", dom ) : null;
							 String vuptodatestr = genericUtility.getColumnValue( "valid_upto", dom ) != null ? genericUtility.getColumnValue( "valid_upto", dom ) : null;
							//if(effdatestr != null && vuptodatestr!=null)
							//{
								String updateFlag ="";
								String lineNo1 = genericUtility.getColumnValue( "line_no", dom ) != null ? genericUtility.getColumnValue( "line_no", dom ) : "";
								
								lineNo1 = lineNo1.trim();
								String lineNoDet=""; 
							
								String effdateDet = null;
								String valuptoDet = null;
								System.out.println("lineNo1.......["+lineNo1+"] ");
								System.out.println("effdatestr.......["+effdatestr+"] ");
								System.out.println("vuptodatestr.......["+vuptodatestr+"] ");
								//System.out.println("lineNo1.......["+lineNo1+"] ");
								java.sql.Timestamp efDtDomTimeStp = null;
								java.sql.Timestamp vuptodtDomTimeStp = null;
								java.sql.Timestamp efDtDetTimeStp = null;
								java.sql.Timestamp vuptodtDetTimeStp = null;
								
									NodeList detail2List = dom2.getElementsByTagName("Detail3");
									
									int detail2ListLen = detail2List.getLength();
									for(int detail2Idx = 0; detail2Idx < detail2ListLen ; detail2Idx++ )
									{
										Node currDetail = detail2List.item( detail2Idx );
										NodeList currDetNodes = currDetail.getChildNodes();
										
										updateFlag = getNodeValue( currDetail, "updateFlag", true );
										effdateDet = getNodeValue( currDetail, "eff_date", false );
										valuptoDet = getNodeValue( currDetail, "valid_upto", false );	
										lineNoDet = getNodeValue( currDetail, "line_no", false );
										
										System.out.println("updateFlag.......["+updateFlag+"] ");
										System.out.println("effdateDet.......["+effdateDet+"] ");
										System.out.println("lineNoDet.......["+lineNoDet+"] ");
										if( (!lineNoDet.equals(lineNo1)) && (!updateFlag.equals("D") ) )
										{
											if( (effdateDet != null) && (valuptoDet != null) )
											{
												System.out.println("Inside first if condition.......");
												efDtDomTimeStp = Timestamp.valueOf(genericUtility.getValidDateString(effdatestr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
												vuptodtDomTimeStp = Timestamp.valueOf(genericUtility.getValidDateString(vuptodatestr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
												efDtDetTimeStp = Timestamp.valueOf(genericUtility.getValidDateString(effdateDet, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
												vuptodtDetTimeStp = Timestamp.valueOf(genericUtility.getValidDateString(valuptoDet, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
												
												if( efDtDomTimeStp.compareTo ( efDtDetTimeStp ) >= 0 &&  efDtDomTimeStp.compareTo ( vuptodtDetTimeStp ) <= 0)
												{
													
													errString = itmDBAccessEJB.getErrorString("","VMEFVUTDAT",userId,"",conn);
													break ;
												}
												if( efDtDomTimeStp.compareTo ( efDtDetTimeStp ) <= 0 &&  vuptodtDomTimeStp.compareTo ( vuptodtDetTimeStp ) >= 0)
												{
													
													System.out.println(" vuptodtDetTimeStp ::"+vuptodtDetTimeStp);
													errString = itmDBAccessEJB.getErrorString("","VMEFVUTDAT",userId,"",conn);
													break ;
												}
												if( vuptodtDomTimeStp.compareTo ( efDtDetTimeStp ) >= 0 &&  vuptodtDomTimeStp.compareTo ( vuptodtDetTimeStp ) < 0)
												{
													errString = itmDBAccessEJB.getErrorString("","VMEFVUTDAT",userId,"",conn);
													break ;
												}
												if( efDtDomTimeStp.compareTo ( efDtDetTimeStp ) == 0 ||  vuptodtDomTimeStp.compareTo ( vuptodtDetTimeStp ) == 0)
												{
													errString = itmDBAccessEJB.getErrorString("","VMEFVUTDAT",userId,"",conn);
													break ;
												}
												
											}
										}
									}
							
						}
					} //end eff_date

					else if(childNodeName.equalsIgnoreCase("valid_upto")) // start valid_upto
					{
						System.out.println("@@@@@@@@@@ date validation : valid upto");
						
						effDateStr =genericUtility.getColumnValue("eff_date",dom);
						validUptoStr =genericUtility.getColumnValue("valid_upto",dom);
						lineNo = genericUtility.getColumnValue("line_no",dom);
						if((effDateStr != null) && (validUptoStr != null))
						{
							effDate= Timestamp.valueOf(genericUtility.getValidDateString(effDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							validUpto= Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("effDate date is :=>  " + effDate);
							System.out.println("validUpto date is :=>  " + validUpto);

							if( !validUpto.after(effDate) && !( validUpto.equals(effDate) ) )
							{
								//Valid upto cannot be less than effective date
								errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId,"",conn);
								break ;
							}
							if(effDate.equals(validUpto)){
							
								errString = itmDBAccessEJB.getErrorString("","INVEFDATE",userId,"",conn);
								break ;
							}
						}
						else
						{
							//Date cannot be empty. Please enter valid date.
							errString = itmDBAccessEJB.getErrorString("","VMVALDTNUL",userId,"",conn);
							break ;
						}
						
					
					} // end valid_upto
					
					else if(childNodeName.equalsIgnoreCase("min_amt"))
					{ // start if min_amt

			 			minAmount  = Double.parseDouble(checkNllOrBlank(genericUtility.getColumnValue("min_amt",dom)));
			 			maxAmount  = Double.parseDouble(checkNllOrBlank(genericUtility.getColumnValue("max_amt",dom))); 
                        System.out.println("Minimum amount ["+ minAmount + "]Minimum amount ["+ minAmount + "]" );
			 			if( minAmount <= 0 ){
			 				errString = itmDBAccessEJB.getErrorString("","VTMINAMT",userId,"",conn);
							break ;
			 			 }
			 			if(minAmount >= maxAmount){
			 				
			 				errString = itmDBAccessEJB.getErrorString("","VTMINAMT",userId,"",conn);
							break ;
			 			}
			 		
			 		} // end min_amt
					else if(childNodeName.equalsIgnoreCase("max_amt")){ // start if max_amt
			 			minAmount  = Double.parseDouble(checkNllOrBlank(genericUtility.getColumnValue("min_amt",dom)));
			 			maxAmount  = Double.parseDouble(checkNllOrBlank(genericUtility.getColumnValue("max_amt",dom))); 
			 		
			 			 if(minAmount >= maxAmount )
			 			 {
			 				System.out.println("Max Amount should be greater then Zero");
			 				errString = itmDBAccessEJB.getErrorString("VTMXMNTPR","",userId,"",conn);
							break ;
			 			 }
			 		    if( maxAmount <= 0 ){
	 						System.out.println("Max Amount should be greater then Zero");
	 						errString = itmDBAccessEJB.getErrorString("","VTMXAMTPR",userId,"",conn);
							break ;
			 			}
			 		} //  end if max_amt
					commCriteria =	genericUtility.getColumnValue("comm_criteria", dom1);
					System.out.println("commCriteria ::: ["+commCriteria);
					if(commCriteria != null && commCriteria.trim().length()>0){
						
						if(!"V".equalsIgnoreCase(commCriteria)){
							errString = itmDBAccessEJB.getErrorString("","VMINTRNSEC",userId,"",conn);
						    break;
						}
					}
				} 
				// changed by Nasruddin khan [19/JUL/16 D16DFOR001] END
				break;
			} //END switch

	    
		itminfo = 	itemCode + itemSer;
		boolean res =  duplicateCheck (dom,dom2,itminfo,effDateStr,validUptoStr,lineNo); //comment by kunal on 22/02/13	
			System.out.println("@@@@ res::"+res);
			if( res == true )
			{
				errString = itmDBAccessEJB.getErrorString("","VMVALDTWR",userId,"",conn);
				return errString;
			}
			
		/*	// changed by Nasruddin khan [22/JUL/16 D16DFOR001] start
			boolean result = isDulplicateDate(dom2,effDateStr,validUptoStr);
			if( result == true){
				errString = itmDBAccessEJB.getErrorString("","VMEFVUTDAT",userId,"",conn);
				return errString;
			}*/
		
			// changed by Nasruddin khan [22/JUL/16 D16DFOR001] end
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					rs = null;
					if(pstmt != null )pstmt.close();
					pstmt =null;
					conn.close();
				}
				conn = null;
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}
		}
		System.out.println("ErrString ::"+errString);


		return errString;
	}//END OF VALIDATION
	


	public String itemChanged(String xmlString, String xmlString1, String xmlString2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return valueXmlString;
	}
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("@@@@@@@ itemChanged called");
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
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		String sql="";
		int currentFormNo =0;
		String columnValue="",chgUser="",chgTerm="",itemCode="",itemDescr="", fSysDate = "";
		//String custCode="",custDescr="";
		//String col_name,scustName="",custType="",crTerm="",Descr="",custName="";
		int ctr=0;
		try
		{   
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver=null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			chgUser = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgUser" );

			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");

			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");


				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("--------------------ITM_DEFAULT-----------------------");
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String sysDate = sdf.format(currentDate.getTime());
					System.out.println("Now the date is :=>  " + sysDate);

					valueXmlString.append( "<chg_date><![CDATA[" ).append(sysDate).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append( chgUser ).append( "]]></chg_user>\r\n" );
				}

				valueXmlString.append("</Detail1>");
				break;


				// case 2 start
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
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");


				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("--------------------ITM_DEFAULT-----------------------");
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String sysDate = sdf.format(currentDate.getTime());
					System.out.println("Now the date is :=>  " + sysDate);

					currentDate.add(Calendar.DAY_OF_MONTH, +1);
					fSysDate = sdf.format(currentDate.getTime());
					System.out.println("Now the future date is :=>  " + fSysDate);

					valueXmlString.append( "<eff_date><![CDATA[" ).append(sysDate).append( "]]></eff_date>\r\n" );
					valueXmlString.append( "<valid_upto><![CDATA[" ).append( fSysDate ).append( "]]></valid_upto>\r\n" );
				}
				else if( currentColumn.trim().equalsIgnoreCase( "item_code" ) )
				{
					System.out.println("-------------------item_code-----------------------");
					
					itemCode = genericUtility.getColumnValue("item_code", dom);
					System.out.println("item_code = "+itemCode);
					
					if(itemCode != null && itemCode.trim().length() > 0 )
					{
						sql = "select descr from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							itemDescr = rs.getString(1);
							valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr +"]]>").append("</item_descr>");
						}
						else
						{
							valueXmlString.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						valueXmlString.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>");
					}
					
				}
				valueXmlString.append("</Detail2>");
				break;
				// case 2 end
			}
			valueXmlString.append("</Root>");

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
	}//End of itemChanged	 

	public boolean duplicateCheck (Document dom,Document dom2,String pitminfo,String peffDateStr,String pvalidUptoStr,String plineNo) throws Exception
	{
		String lineNo="",commTable="",itemSer="",itemCode="",effDateStr="",validUptoStr="",itminfo="";
		String  effDateTemp=null ;	
		Timestamp  effDateTemp2=null ,ValidUptoLastTemp=null, ValidUptoLastTemp2=null,peffDate=null,pvalidUpto=null;
		String errString = "",editFlag="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//String userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		int ctr1=0,k=0,l=1,noOfParent=0;
		String dateStr=null,itemCodeTemp="",itemSerTemp="",validUptoTemp="",itemSerTempLast="",itemCodeTempLast="";

		java.sql.Timestamp  validUptoTemp2=null;

		NodeList detail2List = dom2.getElementsByTagName("Detail2");
		NodeList detail1List = dom2.getElementsByTagName("Detail1");
		
		
		
		
		ArrayList arrLstDate=new ArrayList();

		if(detail2List != null && detail2List.getLength() > 0)
		{
			noOfParent = detail2List.getLength();
			System.out.println("@@@@@@@@ noOfParent [["+noOfParent +"]]");
			System.out.println("@@@@ pitminfo:["+pitminfo+"]::lineNo:["+lineNo+"]");
			
			if( peffDateStr != null && pvalidUptoStr !=null && peffDateStr.trim().length() > 0 && pvalidUptoStr.trim().length() > 0 )
			{
			peffDate = Timestamp.valueOf(genericUtility.getValidDateString(peffDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			pvalidUpto = Timestamp.valueOf(genericUtility.getValidDateString(pvalidUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			System.out.println("@@@@@parent date@@@peffDate["+peffDate+"] ::pvalidUpto["+pvalidUpto+"]");
			
			for (  ctr1 = 0; ctr1 < noOfParent; ctr1++ )  //Loop for each node of current detail
			{

				itemCode = genericUtility.getColumnValueFromNode( "item_code", detail2List.item(ctr1) );
				effDateStr = genericUtility.getColumnValueFromNode( "eff_date", detail2List.item(ctr1) );
				validUptoStr = genericUtility.getColumnValueFromNode( "valid_upto", detail2List.item(ctr1) );
				itemSer = genericUtility.getColumnValueFromNode( "item_ser", detail2List.item(ctr1) );
				
				editFlag =  genericUtility.getColumnValueFromNode( "edit_flag", detail2List.item(ctr1) );
				
				lineNo =  genericUtility.getColumnValueFromNode( "line_no", detail2List.item(ctr1) );
				System.out.println("@@@@ itemCode:["+itemCode+"]::::itemSer["+itemSer+"]:::editFlag["+editFlag+"]");
				dateStr = validUptoStr+"@"+effDateStr ; 
				System.out.println("dateStr[[[[[[[["+dateStr+"]]]]]]]]]");
				
				itminfo = itemCode + itemSer;
				
				if(( ( pitminfo.equalsIgnoreCase( itminfo )))  && ( !( plineNo.trim().equalsIgnoreCase(lineNo.trim() ) )))
				{
					arrLstDate.add(dateStr);
					System.out.println("@@@@ added in arraylist");
				}
			}

			Collections.sort(arrLstDate);
			System.out.println("@@@@@arrLstDate.size()["+arrLstDate.size()+"]]]:: Sorted::::"+arrLstDate);

			for( k=0;k< arrLstDate.size();k++)
			{
				String temp = (String) arrLstDate.get(k);
				StringTokenizer st = new StringTokenizer(temp,"@");
				while(st.hasMoreTokens()) 
				{
					if( l== 1)
					{	
						validUptoTemp = st.nextToken();
						l++;
					}
					else if( l== 2)
					{
						effDateTemp = st.nextToken();
						l=1;	
					}


				}

				if( effDateTemp != null && validUptoTemp !=null && effDateTemp.trim().length() > 0 && validUptoTemp.trim().length() > 0  && !("D".equalsIgnoreCase(editFlag)))
				{
				effDateTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(effDateTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				validUptoTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(validUptoTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
				System.out.println("@@@@@date@@@effDateTemp2["+effDateTemp2+"] ::validUptoTemp2["+validUptoTemp2+"]");

				if( (effDateTemp2 != null ) && (validUptoTemp2 != null )) 
				{
					//if( (effDateTemp2.equals( peffDate )) )
					if( (effDateTemp2.equals( peffDate ))  && ( validUptoTemp2.equals(pvalidUpto)) )
					{
						System.out.println("@@@@@@@ error in date");
						return true; 
					}
				}
				}
			
			/*
			for( k=0;k< arrLstDate.size();k++)
			{
				String temp = (String) arrLstDate.get(k);
				StringTokenizer st = new StringTokenizer(temp,"@");
				while(st.hasMoreTokens()) 
				{
					if( l== 1)
					{	
						validUptoTemp = st.nextToken();
						l++;
					}
					else if( l== 2)
					{
						effDateTemp = st.nextToken();
						l=1;	
					}


				}

				effDateTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(effDateTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				validUptoTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(validUptoTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

				System.out.println("@@@@@date111@@@ValidUptoLastTemp["+ValidUptoLastTemp+"] ::validUptoTemp2["+validUptoTemp2+"]");

				if( ValidUptoLastTemp != null  )
				{
					SimpleDateFormat sdf = new SimpleDateFormat(getApplDateFormat());
					Calendar c = Calendar.getInstance();
					c.setTime((effDateTemp2));
					c.add(Calendar.DATE, -1);
					effDateTemp2 = new Timestamp( c.getTimeInMillis());
					System.out.println("@@@@@date@@@effDateTemp2["+effDateTemp2+"] ::ValidUptoLastTemp["+ValidUptoLastTemp+"]");
					if( !(effDateTemp2.equals( ValidUptoLastTemp )))
					{
						System.out.println("@@@@@@@ error in date");
						return true; 
					}
				}
				//else
				//{
				//	 itemCodeTempLast = itemCodeTemp;
				//	 itemSerTempLast = itemSerTemp;
				//}

				ValidUptoLastTemp = validUptoTemp2;
			} 
			  
			*/
		}
		return false;
	}
	// changed by Nasruddin khan [19/JUL/16 D16DFOR001] START
	/*private boolean isDulplicateDate(Document dom,Document dom2,String peffDateStr,String pvalidUptoStr,String plineNo) throws ITMException, Exception  {
		

		System.out.println("Call Check dublicate value ::::::");
		String lineNo="",commTable="",effDateStr="",validUptoStr="";
		String  effDateTemp=null ;	
		Timestamp  effDateTemp2=null ,ValidUptoLastTemp=null, ValidUptoLastTemp2=null,peffDate=null,pvalidUpto=null;
		String errString = "",editFlag="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//String userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		int ctr1=0,k=0,l=1,noOfParent=0;
		String dateStr=null,itemCodeTemp="",itemSerTemp="",validUptoTemp="",itemSerTempLast="",itemCodeTempLast="";

		java.sql.Timestamp  validUptoTemp2=null;

		NodeList detail3List = dom2.getElementsByTagName("Detail3");
		//NodeList detail1List = dom2.getElementsByTagName("Detail1");
		
		
		
		
		ArrayList arrLstDate=new ArrayList();

		if(detail3List != null && detail3List.getLength() > 0)
		{
			noOfParent = detail3List.getLength();
			System.out.println("@@@@@@@@ noOfParent [["+noOfParent +"]]");
			System.out.println("@@@@ ::lineNo:["+lineNo+"]");
			
			if( peffDateStr != null && pvalidUptoStr !=null && peffDateStr.trim().length() > 0 && pvalidUptoStr.trim().length() > 0 )
			{
			peffDate = Timestamp.valueOf(genericUtility.getValidDateString(peffDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			pvalidUpto = Timestamp.valueOf(genericUtility.getValidDateString(pvalidUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			System.out.println("@@@@@parent date@@@peffDate["+peffDate+"] ::pvalidUpto["+pvalidUpto+"]");
			
			for (  ctr1 = 0; ctr1 < noOfParent; ctr1++ )  //Loop for each node of current detail
			{

				effDateStr = genericUtility.getColumnValueFromNode( "eff_date", detail3List.item(ctr1) );
				validUptoStr = genericUtility.getColumnValueFromNode( "valid_upto", detail3List.item(ctr1) );
				
				editFlag =  genericUtility.getColumnValueFromNode( "edit_flag", detail3List.item(ctr1) );
				
				lineNo =  genericUtility.getColumnValueFromNode( "line_no", detail3List.item(ctr1) );
				dateStr = validUptoStr+"@"+effDateStr ; 
				System.out.println("dateStr[[[[[[[["+dateStr+"]]]]]]]]]");
				
				
				if(!( plineNo.trim().equalsIgnoreCase(lineNo.trim() ) ))
				{
					arrLstDate.add(dateStr);
					System.out.println("@@@@ added in arraylist");
				}
			}

			Collections.sort(arrLstDate);
			System.out.println("@@@@@arrLstDate.size()["+arrLstDate.size()+"]]]:: Sorted::::"+arrLstDate);

			for( k=0;k< arrLstDate.size();k++)
			{
				String temp = (String) arrLstDate.get(k);
				StringTokenizer st = new StringTokenizer(temp,"@");
				while(st.hasMoreTokens()) 
				{
					if( l== 1)
					{	
						validUptoTemp = st.nextToken();
						l++;
					}
					else if( l== 2)
					{
						effDateTemp = st.nextToken();
						l=1;	
					}


				}

				if( effDateTemp != null && validUptoTemp !=null && effDateTemp.trim().length() > 0 && validUptoTemp.trim().length() > 0  && !("D".equalsIgnoreCase(editFlag)))
				{
				effDateTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(effDateTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				validUptoTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(validUptoTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
				System.out.println("@@@@@date@@@effDateTemp2["+effDateTemp2+"] ::validUptoTemp2["+validUptoTemp2+"]");

				if( (effDateTemp2 != null ) && (validUptoTemp2 != null )) 
				{
					//if( (effDateTemp2.equals( peffDate )) )
					if( (effDateTemp2.equals( peffDate ))  && ( validUptoTemp2.equals(pvalidUpto)) )
					{
						System.out.println("@@@@@@@ error in date");
						return true; 
					}
				}
				}
		}
		return false;
	}*/


	private String checkNllOrBlank(String input) {

			if(input == null || input.trim().equals(""))
			{
				return "0";
			}
			else
			{
				return input ;
			}

		}
	//changed by Nasruddin khan [19/JUL/16 D16DFOR001] END
	//changed by Nasruddin khan [25/JUL/16 D16DFOR001] START
	private String getNodeValue( Node currDet, String fldName, boolean isAttribute )
	{
		String fldValue = null;
		boolean isFound = false;
		NodeList currNodes = currDet.getChildNodes();
		int currDetLen = currNodes.getLength();
		for(int detIdx = 0; detIdx < currDetLen && !isFound ; detIdx++ )
		{
			Node currNode = currNodes.item( detIdx );
			String nodeName = currNode.getNodeName();

			if( isAttribute == true )
			{
				if ( nodeName.equalsIgnoreCase( "attribute" ) )
				{
					fldValue = currNode.getAttributes().getNamedItem( fldName ).getNodeValue();
					isFound = true;
				}				
			}
			else if ( currNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equalsIgnoreCase( fldName ) )
			{
				fldValue = currNode.getFirstChild() != null ? currNode.getFirstChild().getNodeValue().trim() : null;
				isFound = true;
			}
		}
		return fldValue;
		//changed by Nasruddin khan [25/JUL/16 D16DFOR001] START
	}
}






