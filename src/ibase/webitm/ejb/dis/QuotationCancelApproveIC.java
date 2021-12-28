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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;	




import javax.ejb.Stateless; // added for ejb3

import ibase.webitm.ejb.ITMDBAccessEJB;

@Stateless // added for ejb3
public class QuotationCancelApproveIC extends ValidatorEJB implements QuotationCancelApproveICLocal,QuotationCancelApproveICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

//	
	
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
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
		ConnDriver connDriver = new ConnDriver();
		String sql="";
		int currentFormNo =0;
		String columnValue="",chgUser="",chgTerm="",itemCode="",itemDescr="", fSysDate = "",quotNo="";
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
					quotNo = checkNull(genericUtility.getColumnValue("quot_no", dom));
					System.out.println("Quot no :- ["+quotNo+"]");
//					valueXmlString.append( "<chg_date><![CDATA[" ).append(sysDate).append( "]]></chg_date>\r\n" );
//					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
//					valueXmlString.append( "<chg_user><![CDATA[" ).append( chgUser ).append( "]]></chg_user>\r\n" );
				}

				valueXmlString.append("</Detail1>");
				break;


				// case 2 start
//			case 2 :
//				parentNodeList = dom.getElementsByTagName("Detail2");
//				parentNode = parentNodeList.item(0);
//				childNodeList = parentNode.getChildNodes();
//				ctr = 0; 
//				valueXmlString.append("<Detail2>");
//				childNodeListLength = childNodeList.getLength();
//				do
//				{ 
//					childNode = childNodeList.item(ctr);
//					childNodeName = childNode.getNodeName();
//					if(childNodeName.equals(currentColumn))
//					{
//						if (childNode.getFirstChild()!= null)
//						{
//							columnValue = childNode.getFirstChild().getNodeValue().trim();
//						}
//					}
//					ctr++;
//				}
//				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
//				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
//
//
//				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
//				{
//					System.out.println("--------------------ITM_DEFAULT-----------------------");
//					Calendar currentDate = Calendar.getInstance();
//					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
//					String sysDate = sdf.format(currentDate.getTime());
//					System.out.println("Now the date is :=>  " + sysDate);
//
//					currentDate.add(Calendar.DAY_OF_MONTH, +1);
//					fSysDate = sdf.format(currentDate.getTime());
//					System.out.println("Now the future date is :=>  " + fSysDate);
//
//					valueXmlString.append( "<eff_date><![CDATA[" ).append(sysDate).append( "]]></eff_date>\r\n" );
//					valueXmlString.append( "<valid_upto><![CDATA[" ).append( fSysDate ).append( "]]></valid_upto>\r\n" );
//
//				}
//				else if( currentColumn.trim().equalsIgnoreCase( "item_code" ) )
//				{
//					System.out.println("-------------------item_code-----------------------");
//					
//					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
//					System.out.println("item_code = "+itemCode);
//					
//					if(itemCode != null && itemCode.trim().length() > 0 )
//					{
//						sql = "select descr from item where item_code = ?";
//						pstmt =  conn.prepareStatement(sql);
//						pstmt.setString(1,itemCode);
//						rs = pstmt.executeQuery();
//						if(rs.next())
//						{
//
//							itemDescr = rs.getString(1);
//							valueXmlString.append("<item_descr>").append("<![CDATA[" + itemDescr +"]]>").append("</item_descr>");
//						}
//						else
//						{
//							valueXmlString.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>");
//						}
//						rs.close();
//						rs = null;
//						pstmt.close();
//						pstmt = null;
//					}
//					else
//					{
//						valueXmlString.append("<item_descr>").append("<![CDATA[]]>").append("</item_descr>");
//					}
//
//					
//				}
//
//				valueXmlString.append("</Detail2>");
//				break;
//				// case 2 end
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

//	public boolean duplicateCheck (Document dom,Document dom2,String pitminfo,String peffDateStr,String pvalidUptoStr,String plineNo) throws Exception
//	{
//		String lineNo="",commTable="",itemSer="",itemCode="",effDateStr="",validUptoStr="",itminfo="";
//		String  effDateTemp=null ;	
//		Timestamp  effDateTemp2=null ,ValidUptoLastTemp=null, ValidUptoLastTemp2=null,peffDate=null,pvalidUpto=null;
//		String errString = "",editFlag="";
//		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
//		//String userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
//		int ctr1=0,k=0,l=1,noOfParent=0;
//		String dateStr=null,itemCodeTemp="",itemSerTemp="",validUptoTemp="",itemSerTempLast="",itemCodeTempLast="";
//
//		java.sql.Timestamp  validUptoTemp2=null;
//
//		NodeList detail2List = dom2.getElementsByTagName("Detail2");
//		NodeList detail1List = dom2.getElementsByTagName("Detail1");
//		ArrayList arrLstDate=new ArrayList();
//
//		if(detail2List != null && detail2List.getLength() > 0)
//		{
//			noOfParent = detail2List.getLength();
//			System.out.println("@@@@@@@@ noOfParent [["+noOfParent +"]]");
//			System.out.println("@@@@ pitminfo:["+pitminfo+"]::lineNo:["+lineNo+"]");
//			
//			if( peffDateStr != null && pvalidUptoStr !=null && peffDateStr.trim().length() > 0 && pvalidUptoStr.trim().length() > 0 )
//			{
//			peffDate = Timestamp.valueOf(genericUtility.getValidDateString(peffDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
//			pvalidUpto = Timestamp.valueOf(genericUtility.getValidDateString(pvalidUptoStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
//			}
//			System.out.println("@@@@@parent date@@@peffDate["+peffDate+"] ::pvalidUpto["+pvalidUpto+"]");
//			
//			for (  ctr1 = 0; ctr1 < noOfParent; ctr1++ )  //Loop for each node of current detail
//			{
//
//				itemCode = genericUtility.getColumnValueFromNode( "item_code", detail2List.item(ctr1) );
//				effDateStr = genericUtility.getColumnValueFromNode( "eff_date", detail2List.item(ctr1) );
//				validUptoStr = genericUtility.getColumnValueFromNode( "valid_upto", detail2List.item(ctr1) );
//				itemSer = genericUtility.getColumnValueFromNode( "item_ser", detail2List.item(ctr1) );
//				
//				editFlag =  genericUtility.getColumnValueFromNode( "edit_flag", detail2List.item(ctr1) );
//				
//				lineNo =  genericUtility.getColumnValueFromNode( "line_no", detail2List.item(ctr1) );
//				System.out.println("@@@@ itemCode:["+itemCode+"]::::itemSer["+itemSer+"]:::editFlag["+editFlag+"]");
//				dateStr = validUptoStr+"@"+effDateStr ; 
//				System.out.println("dateStr[[[[[[[["+dateStr+"]]]]]]]]]");
//				
//				itminfo = itemCode + itemSer;
//				
//				if(( ( pitminfo.equalsIgnoreCase( itminfo )))  && ( !( plineNo.trim().equalsIgnoreCase(lineNo.trim() ) )))
//				{
//					arrLstDate.add(dateStr);
//					System.out.println("@@@@ added in arraylist");
//				}
//			}
//
//			Collections.sort(arrLstDate);
//			System.out.println("@@@@@arrLstDate.size()["+arrLstDate.size()+"]]]:: Sorted::::"+arrLstDate);
//
//			for( k=0;k< arrLstDate.size();k++)
//			{
//				String temp = (String) arrLstDate.get(k);
//				StringTokenizer st = new StringTokenizer(temp,"@");
//				while(st.hasMoreTokens()) 
//				{
//					if( l== 1)
//					{	
//						validUptoTemp = st.nextToken();
//						l++;
//					}
//					else if( l== 2)
//					{
//						effDateTemp = st.nextToken();
//						l=1;	
//					}
//
//
//				}
//
//				if( effDateTemp != null && validUptoTemp !=null && effDateTemp.trim().length() > 0 && validUptoTemp.trim().length() > 0  && !("D".equalsIgnoreCase(editFlag)))
//				{
//				effDateTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(effDateTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
//				validUptoTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(validUptoTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
//				}
//				System.out.println("@@@@@date@@@effDateTemp2["+effDateTemp2+"] ::validUptoTemp2["+validUptoTemp2+"]");
//
//				if( (effDateTemp2 != null ) && (validUptoTemp2 != null )) 
//				{
//					//if( (effDateTemp2.equals( peffDate )) )
//					if( (effDateTemp2.equals( peffDate ))  && ( validUptoTemp2.equals(pvalidUpto)) )
//					{
//						System.out.println("@@@@@@@ error in date");
//						return true; 
//					}
//				}
//				}
//			
//			/*
//			for( k=0;k< arrLstDate.size();k++)
//			{
//				String temp = (String) arrLstDate.get(k);
//				StringTokenizer st = new StringTokenizer(temp,"@");
//				while(st.hasMoreTokens()) 
//				{
//					if( l== 1)
//					{	
//						validUptoTemp = st.nextToken();
//						l++;
//					}
//					else if( l== 2)
//					{
//						effDateTemp = st.nextToken();
//						l=1;	
//					}
//
//
//				}
//
//				effDateTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(effDateTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
//				validUptoTemp2 = Timestamp.valueOf(genericUtility.getValidDateString(validUptoTemp, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
//
//				System.out.println("@@@@@date111@@@ValidUptoLastTemp["+ValidUptoLastTemp+"] ::validUptoTemp2["+validUptoTemp2+"]");
//
//				if( ValidUptoLastTemp != null  )
//				{
//					SimpleDateFormat sdf = new SimpleDateFormat(getApplDateFormat());
//					Calendar c = Calendar.getInstance();
//					c.setTime((effDateTemp2));
//					c.add(Calendar.DATE, -1);
//					effDateTemp2 = new Timestamp( c.getTimeInMillis());
//					System.out.println("@@@@@date@@@effDateTemp2["+effDateTemp2+"] ::ValidUptoLastTemp["+ValidUptoLastTemp+"]");
//					if( !(effDateTemp2.equals( ValidUptoLastTemp )))
//					{
//						System.out.println("@@@@@@@ error in date");
//						return true; 
//					}
//				}
//				//else
//				//{
//				//	 itemCodeTempLast = itemCodeTemp;
//				//	 itemSerTempLast = itemSerTemp;
//				//}
//
//				ValidUptoLastTemp = validUptoTemp2;
//			} 
//			  
//			*/
//		}
//		return false;
//	}

	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}


}



