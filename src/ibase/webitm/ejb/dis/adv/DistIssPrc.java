package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import ibase.utility.CommonConstants;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import ibase.webitm.ejb.*;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.*;
@Stateless

public class DistIssPrc extends ProcessEJB implements  DistIssPrcLocal,DistIssPrcRemote //SessionBean
{
	String errorString=null;
	String loginCode=null;
	String chgTerm=null;
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	CommonConstants commonConstants = new CommonConstants();

	Connection conn = null;

	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("DistIssPrc :getData() function called");
		String rtrStr = "";
		Document headerDom = null;
		Document detailDom = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				headerDom = genericUtility.parseString(xmlString);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString2);
				detailDom = genericUtility.parseString(xmlString2);
			}

			rtrStr = getData(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :DistIssPrcEJB :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			rtrStr = e.getMessage();
			throw new ITMException(e);
		}
		return rtrStr;

	}//END OF GETDATA(1)
	public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String errString=null;
		String sql= "" ;
		String resultString = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		StringBuffer retDistOrdIssBuff = new StringBuffer();

		String siteCodeFr="", siteCodeTo="",toDateStr="",frDateStr="";
		java.sql.Date toDate = null;
		java.sql.Date frDate = null;
		/*
		String tranid="",distOrder="",siteCode="",siteCodeDlv="",distRoute="",tranCode="" ;
		String lrNo="",lorryNo="",locCodeGit="",remarks="",frtType="",chgUser="",chgTerm="";
		String confirmed="",orderType="",availableYN="",stockStatus="",gpSer="";
		String shipmentId="",currCodeFrt="",projCode="",currCode="",gpNo="";
		double  grossWeight=0.0,tareWeight=0.0,netWeight=0.0,frtAmt=0.0;
		double amount=0.0,taxAmt=0.0,netAmt=0.0,discount=0.0,exchRateFrt=0.0;
		double dcNo=0.0;
		 */
		SimpleDateFormat sdf = null;
		java.sql.Date tranDate = null;
		java.sql.Date effDate = null;
		java.sql.Date lrDate = null;
		java.sql.Date chgDate = null;
		java.sql.Date confDate = null;
		java.sql.Date gpDate = null;
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
		}
		catch (Exception e)
		{
			System.out.println("Exception :DistIssPrcEJB :ejbCreate :==>"+e);
			e.printStackTrace();
		}

		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			siteCodeFr = genericUtility.getColumnValue("site_code__from",headerDom);
			siteCodeTo = genericUtility.getColumnValue("site_code__to",headerDom);
			frDateStr = genericUtility.getColumnValue("from_date",headerDom);
			toDateStr = genericUtility.getColumnValue("to_date",headerDom);
			System.out.println("siteCodeFrom is ::"+siteCodeFr+"and siteCodeTo is ::"+siteCodeTo);
			System.out.println("frDateStr is ::"+frDateStr+"and toDateStr is ::"+toDateStr);

			if ((siteCodeFr == null || siteCodeFr.trim().length() == 0) ||(siteCodeTo == null || siteCodeTo.trim().length() == 0))
			{
				errString = itmDBAccessEJB.getErrorString("","VTMNULSITE","","",conn);
				return errString;
			}
			else
			{
				sql =" select count(*) from site where site_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeFr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					int cnt = rs.getInt(1);
					if(cnt == 0) 
					{
						errString = itmDBAccessEJB.getErrorString("","VMSITE1","","",conn);
						return errString;
					}									
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql =" select count(*) from site where site_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCodeTo);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					int cnt = rs.getInt(1);
					if(cnt == 0) 
					{
						errString = itmDBAccessEJB.getErrorString("","VMSITE1","","",conn);
						return errString;
					}									
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			if (frDateStr != null && frDateStr.trim().length() > 0 )
			{
				//frDate=java.sql.Date.valueOf(frDateStr.trim());
				frDate= new java.sql.Date(sdf.parse(frDateStr).getTime());
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","VEDAT2","","",conn);
				return errString;
			}
			if (toDateStr != null && toDateStr.trim().length() > 0 )
			{
				//toDate=java.sql.Date.valueOf(toDateStr.trim());
				toDate= new java.sql.Date(sdf.parse(toDateStr).getTime());
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","VEDAT2","","",conn);
				return errString;
			}
			System.out.println("frDate is ::"+frDate+"and toDate is ::"+toDate);

			if (frDate.after(toDate)) //frmdate is grater than todate
			{
				errString = itmDBAccessEJB.getErrorString("","VTDATE12","","",conn);
				return errString;
			}

			sql =" SELECT TRAN_ID,TRAN_DATE,EFF_DATE,DIST_ORDER,SITE_CODE, "   
				+" SITE_CODE__DLV,DIST_ROUTE,TRAN_CODE,LR_NO,LR_DATE,LORRY_NO, "   
				+" GROSS_WEIGHT,TARE_WEIGHT,NET_WEIGHT,FRT_AMT,AMOUNT,TAX_AMT, "   
				+" NET_AMT,LOC_CODE__GIT,REMARKS,FRT_TYPE,CHG_USER,CHG_TERM , "   
				+" CURR_CODE,CHG_DATE,CONFIRMED,CONF_DATE,ORDER_TYPE,AVAILABLE_YN ,"   
				+" STOCK_STATUS,GP_NO,GP_DATE,GP_SER,DISCOUNT,SHIPMENT_ID, "
				+" CURR_CODE__FRT,EXCH_RATE__FRT,PROJ_CODE,DC_NO "
				+" FROM DISTORD_ISS "  
				+" WHERE (tran_date >= ? ) AND "  
				+" (tran_date <= ?  ) AND "  
				+" (site_code between ? and ? )AND "
				+" CONFIRMED = ? ";

			System.out.println("sql..."+ sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1,frDate );
			pstmt.setDate(2,toDate);
			pstmt.setString(3,siteCodeFr);
			pstmt.setString(4,siteCodeTo);
			pstmt.setString(5,"N");
			rs = pstmt.executeQuery();

			while(rs.next())
			{
				System.out.println("Inside while...");
				//tranid
				retDistOrdIssBuff.append(rs.getString(1)).append("\t");
				//tranDate
				tranDate= rs.getDate("TRAN_DATE");
				retDistOrdIssBuff.append(tranDate).append("\t");
				//effDate
				effDate= rs.getDate("EFF_DATE");
				retDistOrdIssBuff.append(effDate).append("\t");
				//distOrder
				retDistOrdIssBuff.append(rs.getString(4)).append("\t");
				//siteCode
				retDistOrdIssBuff.append(rs.getString(5)).append("\t");
				//siteCodeDlv
				retDistOrdIssBuff.append(rs.getString(6)).append("\t");
				//distRoute
				retDistOrdIssBuff.append(rs.getString(7)).append("\t");
				//tranCode
				retDistOrdIssBuff.append(rs.getString(8)).append("\t");
				//lrNo
				retDistOrdIssBuff.append(rs.getString(9)).append("\t");
				//lrDate
				lrDate= rs.getDate("LR_DATE");
				retDistOrdIssBuff.append(lrDate).append("\t");
				//lorryNo
				retDistOrdIssBuff.append(rs.getString(11)).append("\t");
				//grossWeight
				retDistOrdIssBuff.append(rs.getDouble(12)).append("\t");
				//tareWeight
				retDistOrdIssBuff.append(rs.getDouble(13)).append("\t");
				//netWeight
				retDistOrdIssBuff.append(rs.getDouble(14)).append("\t");
				//frtAmt
				retDistOrdIssBuff.append(rs.getDouble(15)).append("\t");
				//amount
				retDistOrdIssBuff.append(rs.getDouble(16)).append("\t");
				//taxAmt
				retDistOrdIssBuff.append(rs.getDouble(17)).append("\t");
				//netAmt
				retDistOrdIssBuff.append(rs.getDouble(18)).append("\t");
				//locCodeGit
				retDistOrdIssBuff.append(rs.getString(19)).append("\t");
				//remarks
				retDistOrdIssBuff.append(rs.getString(20)).append("\t");
				//frtType
				retDistOrdIssBuff.append(rs.getString(21)).append("\t");
				//chgUser
				retDistOrdIssBuff.append(rs.getString(22)).append("\t");
				//chgTerm
				retDistOrdIssBuff.append(rs.getString(23)).append("\t");
				//currCode
				retDistOrdIssBuff.append(rs.getString(24)).append("\t");
				//chgDate
				chgDate= rs.getDate("CHG_DATE");
				retDistOrdIssBuff.append(chgDate).append("\t");
				//confirmed
				retDistOrdIssBuff.append(rs.getString(26)).append("\t");
				//confDate
				confDate= rs.getDate("CONF_DATE");
				retDistOrdIssBuff.append(confDate).append("\t");
				//orderType
				retDistOrdIssBuff.append(rs.getString(28)).append("\t");
				//availableYN
				retDistOrdIssBuff.append(rs.getString(29)).append("\t");
				//stockStatus
				retDistOrdIssBuff.append(rs.getString(30)).append("\t");
				//gpNo
				retDistOrdIssBuff.append(rs.getString(31)).append("\t");
				//gpDate
				gpDate= rs.getDate("GP_DATE");
				retDistOrdIssBuff.append(gpDate).append("\t");
				//gpSer
				retDistOrdIssBuff.append(rs.getString(33)).append("\t");
				//discount
				retDistOrdIssBuff.append(rs.getDouble(34)).append("\t");
				//shipmentId
				retDistOrdIssBuff.append(rs.getString(35)).append("\t");
				//currCodeFrt
				retDistOrdIssBuff.append(rs.getString(36)).append("\t");
				//exchRateFrt
				retDistOrdIssBuff.append(rs.getDouble(37)).append("\t");
				//projCode
				retDistOrdIssBuff.append(rs.getString(38)).append("\t");
				//dcNo
				retDistOrdIssBuff.append(rs.getDouble(39)).append("\t");
				retDistOrdIssBuff.append("\n");
			}
			resultString = retDistOrdIssBuff.toString();
			System.out.println("ResultString....." + resultString);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		}
		catch (SQLException e)
		{
			System.out.println("SQLException :DistIssPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		catch (Exception e)
		{
			System.out.println("Exception :DistIssPrcEJB :getData(Document headerDom, Document detailDom, String windowName, String xtraParams):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//		retTabSepStrBuff = null;
				if(conn != null && !conn.isClosed())
				{
					if(rs != null)
					{
						rs.close();
						rs=null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					conn.rollback();
					conn.close();
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return resultString;
	}//END OF GETDATA(2)
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		String retStr = "";
		try
		{
			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString);
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2);
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :DistIssPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			retStr = e.getMessage();
			throw new ITMException(e);
		}
		return retStr;
	}//END OF PROCESS (1)
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		//Connection conn = null;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;

		NodeList parentNodeList = null;
		NodeList childNodeList = null;

		Node parentNode = null;
		Node childNode = null;
		ArrayList tranList = new ArrayList();
		String errString="";
		String tranIdFr="";
		String childNodeName = "";
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			System.out.println( "loginCode :: " + loginCode );
			chgTerm=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			System.out.println( "chgTerm :: " + chgTerm );

			System.out.println("[DistIssPrcEJB][loginCode]=>"+loginCode);
			System.out.println("[DistIssPrcEJB][chgTerm]=>"+chgTerm);
			genericUtility = new  ibase.utility.E12GenericUtility();

			parentNodeList = detailDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			System.out.println("parentNodeListLength:::::::::"+parentNodeListLength);

			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item( selectedRow );
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				//rejectionExist = false;
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{

					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					System.out.println( "childNodeName.........:-"+ childNodeName );
					if (childNodeName.equals("tran_id"))
					{
						System.out.println( "tranIdFr before.........:-"+ tranIdFr );
						tranIdFr = childNode.getFirstChild().getNodeValue();
						System.out.println( "tranIdFr after.........:-"+ tranIdFr );

					}
				}
				System.out.println( "tranIdFr final.........:-"+ tranIdFr );
				System.out.println( "xtraParams final.........:-"+ xtraParams );
				errString=confirmDistIssue("dist_issue",tranIdFr,xtraParams,"",conn);
				if((errString !=null && errString.trim().length() > 0 ) && (!(errString.contains("VTSUCC1"))))
				{
					try
					{
						conn.rollback();
						System.out.println("Connection is roll back");
						break;
					}
					catch(Exception d)
					{
						System.out.println("Exception : DistIssPrcEJB =>"+d.toString());
						d.printStackTrace();
						//break;
					}
				}//end of if
			} //end for
		}//try end
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception d)
			{
				System.out.println("Exception : WorkorderPrcEJB =>"+d.toString());
				d.printStackTrace();
				System.out.println("Exception :WorkorderPrcEJB :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
				e.printStackTrace();
				errString = e.getMessage();
				throw new ITMException(e);
			}
		}
		finally
		{
			System.out.println("errorString...." + errString);
			System.out.println("Closing Connection....");
			try
			{
				if(conn != null && !conn.isClosed())
				{
					//conn.rollback();
					conn.commit();
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString ;
			}
		}
		System.out.println("Error Message=>"+errString);
		return errString;
	}//END OF PROCESS(2)
	private String confirmDistIssue(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		System.out.println("confirmReceipt(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");
		try
		{
			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm'";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1,businessObj);
			rs = pStmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = "+serviceCode+" compName "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1,serviceCode);
			rs = pStmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
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

			call.setReturnType(XMLType.XSD_STRING);

			retString = (String)call.invoke(aobj);

			System.out.println("Confirm Complete @@@@@@@@@@@Return string from NVO is:==>["+retString+"]");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (pStmt != null )
				{
					pStmt.close();
					pStmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
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
			}
		}
		return retString;
	}

}
