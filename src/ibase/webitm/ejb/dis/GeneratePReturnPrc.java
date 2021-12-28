/*Developer : Kunal Mandhre
 * Date : 04/07/13
 * Purpose = auto generate purchase return from sales return  
 * */
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import javax.naming.InitialContext;
import javax.ejb.Stateless; 
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.XMLType;
import javax.xml.rpc.ParameterMode;

@Stateless
public class GeneratePReturnPrc extends ProcessEJB implements GeneratePReturnPrcLocal, GeneratePReturnPrcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :GenerateReceiptPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("process retStr:::"+retStr);
		return retStr;
	}		    
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String tranId = "",itemCode = "",siteCode = "",invoiceId = "",tranType = "",saleOrder = "";
		String porderRef = "",locationCode = "",lotNo = "",lotSl = "",confirmed = "",purchOrder = "";
		String xmlString = "";
		String retString = "",sql = "";
		int lineNo = 0,lineNoOrd = 0,cnt=0;
		double quantity = 0;
		Date tranDate = null;
		StringBuffer xmlBuff = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		SimpleDateFormat sdf = null;
		ConnDriver connDriver = new ConnDriver();
		//java.sql.Timestamp chgDate = new java.sql.Timestamp(System.currentTimeMillis());
		//String chgUser = "",chgTerm = "";
		PreparedStatement pstmt = null,pstmt1 = null;
		Connection conn = null;
		ResultSet rs = null,rs1 = null;
		String userId = "";
		try
		{
			if(conn==null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
			}
			xmlBuff = new StringBuffer();

			//chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			//chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");

			//java.util.Date currentDate = new java.util.Date();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//String sysDate = sdf.format(currentDate.getTime());
			//Timestamp newsysDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
			//System.out.println("Now the date is :=>  ["+newsysDate+"]");

			tranId = genericUtility.getColumnValue("tran_id",headerDom);

			System.out.println("tranId["+tranId);

			if( tranId != null && tranId.trim().length() > 0 )
			{
				sql = " select count(*) from sreturn where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1) ;
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;	

				if( cnt == 0 )
				{
					retString =itmDBAccessEJB.getErrorString("","VTINVSRID1","","",conn); 
					return retString;
				}
				else
				{
					sql = " select case when confirmed is null then 'N' else confirmed end from sreturn where tran_id = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						confirmed = rs.getString(1) ;
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if( !"Y".equalsIgnoreCase(confirmed) )
					{
						retString =itmDBAccessEJB.getErrorString("","VTINVSRID2","","",conn); 
						return retString;
					}
				}
			}
			else
			{
				retString =itmDBAccessEJB.getErrorString("","VTTRANIDNL","","",conn); 
				return retString;
			}

			sql = " select invoice_id ,tran_date  from sreturn where tran_id = ?  " ;				  
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				invoiceId = checkNull(rs.getString("invoice_id"));
				tranDate = rs.getDate("tran_date");

			}
			rs.close();
			pstmt.close();
			rs = null;
			pstmt = null;
			System.out.println("invoiceId="+invoiceId);

			if(invoiceId != null && invoiceId.trim().length() > 0)
			{
				sql = " select sord_no from invoice_trace where invoice_id = ?  " ;				  
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,invoiceId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					saleOrder = checkNull(rs.getString("sord_no"));
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;

				sql = " select tran_id__porcp,purc_order  from sorder where sale_order = ?  " ;				  
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					porderRef = checkNull(rs.getString("tran_id__porcp"));
					purchOrder = checkNull(rs.getString("purc_order"));
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				System.out.println("porderRef="+porderRef);
				if(porderRef == null || porderRef.trim().length() == 0)
				{
					retString =itmDBAccessEJB.getErrorString("","VTPOREFBK","","",conn); 
					return retString;
				}
				else
				{
					sql = " select count(*) from porcp where tran_id__ref = ? and  tran_ser = 'P-RET' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,porderRef);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt(1) ;
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;	
					if(cnt > 0)
					{
						retString =itmDBAccessEJB.getErrorString("","VTPOREFGEN","","",conn); 
						return retString;
					}
				}

			}
			else
			{
				retString =itmDBAccessEJB.getErrorString("","VTINVIDBK","","",conn); 
				return retString;
			}

			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("preturn").append("]]></objName>");  
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
			xmlBuff.append("<description>").append("Header0 members").append("</description>");		
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"preturn\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_type><![CDATA["+ tranType +"]]></tran_type>");
			xmlBuff.append("<tran_date><![CDATA["+ sdf.format(tranDate).toString() +"]]></tran_date>");
			xmlBuff.append("<tran_id__ref><![CDATA["+ porderRef  +"]]></tran_id__ref>");

			//xmlBuff.append("<item_ser><![CDATA["+ siteCode  +"]]></item_ser>");
			//xmlBuff.append("<supp_code><![CDATA["+ siteCode  +"]]></supp_code>");
			//xmlBuff.append("<invoice_no><![CDATA["+ invoiceId +"]]></invoice_no>");
			//xmlBuff.append("<invoice_date><![CDATA["+ sdf.format(invoiceDate).toString() +"]]></invoice_date>");
			xmlBuff.append("<remarks><![CDATA[ Auto generated from Sales Return :"+ tranId +"]]></remarks>");
			xmlBuff.append("</Detail1>");

			sql = " select line_no,item_code,quantity ,loc_code,lot_no,lot_sl from sreturndet where tran_id = ?   " ;				  
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				lineNo = rs.getInt("line_no");
				itemCode = rs.getString("item_code");
				quantity = rs.getDouble("quantity");
				locationCode = rs.getString("loc_code");
				lotNo = rs.getString("lot_no");
				lotSl = rs.getString("lot_sl");
				xmlBuff.append("<Detail2 dbID='' domID='"+lineNo+"' objName=\"preturn\" objContext=\"2\">"); 
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<line_no><![CDATA["+lineNo+"]]></line_no>");
				xmlBuff.append("<purc_order><![CDATA["+purchOrder+"]]></purc_order>");

				sql = " select porcpdet.line_no__ord  from porcp,porcpdet  where porcp.tran_id = porcpdet.tran_id "
					+" and porcp.tran_id = ? and porcp.tran_ser = 'P-RCP'  and porcpdet.purc_order = ?  and  porcpdet.item_code = ?  " ;				  
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,porderRef);
				pstmt1.setString(2,purchOrder);
				pstmt1.setString(3,itemCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lineNoOrd = rs1.getInt("line_no__ord");
				}
				rs1.close();
				pstmt1.close();
				rs1 = null;
				pstmt1 = null;

				xmlBuff.append("<line_no__ord><![CDATA["+lineNoOrd+"]]></line_no__ord>");
				xmlBuff.append("<item_code><![CDATA["+itemCode+"]]></item_code>");
				xmlBuff.append("<quantity><![CDATA["+quantity+"]]></quantity>");
				xmlBuff.append("<loc_code><![CDATA["+locationCode+"]]></loc_code>");
				xmlBuff.append("<lot_no><![CDATA["+lotNo+"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+lotSl+"]]></lot_sl>");
				xmlBuff.append("</Detail2>");

			}
			rs.close();
			pstmt.close();
			rs = null;
			pstmt = null;

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
			System.out.println("...............just before savdata()");
			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("==site code =="+siteCode);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("userId::["+userId+"]");			
			retString = saveData(siteCode,xmlString,userId,conn);
			System.out.println("@@@@@2: retString:"+retString);
			if (retString.indexOf("Success") > -1)
			{
				//System.out.println("@@@@@@3: retString"+retString);
				conn.commit();
				String[] arrayForTranId = retString.split("<TranID>");
				int endIndex = arrayForTranId[1].indexOf("</TranID>");

				String tranIdForIssue = arrayForTranId[1].substring(0,endIndex);
				retString = confirmPReturn("preturn",tranIdForIssue,xtraParams,"",conn);
				System.out.println("retString from conf ::"+retString);
			}
			else
			{
				conn.rollback();
				return retString;
			}

		}//outer try
		catch (Exception e)
		{
			System.out.println("Exception ::GenerateReceiptPrcEJB ::process():" + e.getMessage() + ":");
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new ITMException(e1);
			}
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{

			System.out.println("Closing Connection2....");
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception se){}
		}//

		return retString;

	}//process()



	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
			System.out.println("--retString - -"+retString);
		}
		catch(ITMException itme)
		{
			System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}//end save data
	public String confirmPReturn(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println("confirmPReturn(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");

		try
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,businessObj);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = "+serviceCode+" compName "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,serviceCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			Service service = new Service();
			Call call = (Call)service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String(forcedFlag);
			//System.out.println("@@@@@@@@@@loginEmpCode:" +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);

			retString = (String)call.invoke(aobj);

			System.out.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>["+retString+"]");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{		
			try{


				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				/*if( conn != null ){
					conn.close();
					conn = null;
				}*/
			}
			catch(Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try{
					conn.rollback();

				}catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}

		}
		return retString;
	}//end confirmPReturn

}