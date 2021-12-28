/********************************************************
	Title : ShipmentLocDefaultAct
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ITMDBAccessLocal;
import ibase.webitm.ejb.sys.CreateRCPXML;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.sound.midi.Sequence;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//Changed by sumit on 06/10/12
import org.w3c.dom.*;


@Stateless
public class ShipmentLocDefaultAct extends ActionHandlerEJB implements ShipmentLocDefaultActLocal, ShipmentLocDefaultActRemote
{
	ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();
	public String actionHandler( String actionType, String xmlString, String xmlString1, String xmlString2, String objContext, String xtraParams) throws RemoteException,ITMException
//	public String actionHandler(String tranId, String xtraParam, String forcedFlag) throws RemoteException,ITMException
	
	{
		//int cnt = 0;
		String errString = "";
		//String sql = "";
		//String lorryNo = "";
		//String tranCode = "";
		//String lrNo = "";
		//String confirmed = "";
		//ArrayList<String> errList = new ArrayList<String>();
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		//PreparedStatement pstmt = null;
		//ResultSet rs = null; 
		//String errCode="",recallFrt="";
		//double totalFreight=0;
		//Timestamp lrDate=null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String tranId="";
		
		try
		{
			//Changes and Commented By Bhushan on 13-06-2016 :START
			  //conn = connDriver.getConnectDB("DriverITM");
			  conn = getConnection();
			  //Changes and Commented By Bhushan on 13-06-2016 :END
			String userID = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "LoginCode" );
			System.out.println(" userID ["+userID+"]");

			//ITMDBAccessLocal itmDBAccessLocal = new ITMDBAccessEJB();

			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				System.out.println("@@@@@@@@@xmlString[["+xmlString+"]]");
				dom = genericUtility.parseString(xmlString);
			}

			if (xmlString1 != null && xmlString1.trim().length() != 0) 
			{
				System.out.println("@@@@@@@@@xmlString1[["+xmlString1+"]]");
				dom1 = genericUtility.parseString(xmlString1);
			}

			if (xmlString2 != null && xmlString2.trim().length() != 0) 
			{
				System.out.println("@@@@@@@@@xmlString2[["+xmlString2+"]]");
				dom2 = genericUtility.parseString(xmlString2);
			}
			System.out.println("actionType:" + actionType + ":");

			tranId = genericUtility.getColumnValue("shipment_id", dom1);
			System.out.println("@@@@@@@@@shipment_id["+tranId+"]");
		
		
			if("Default".equalsIgnoreCase(actionType))
			{
				System.out.println("@@@@@@@@@ Default action called...........");

				errString = actionDefault(dom, dom1, dom2,objContext, xtraParams);
				System.out.println("@@@@@@@@@@@errString[[[["+errString+"]]]]]");
			/*	
				if( errCode != null && errCode.trim().length() > 0 )
				{
					errCode = "";
					errString = itmDBAccessLocal.getErrorString("",errCode,"");
				}
				else
				{

					System.out.println("@@@@@@@@@  Default action called successfully...........");

				}
			*/	
			}
		}
		catch(Exception e)
		{
			try{
				conn.rollback();
			}
			catch(SQLException es)
			{
				System.out.println("Rollback exception : " + es.getMessage() + ":");
			}
			System.out.println("Exception : Shipment Confirm():" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			System.out.println("!! Closing Connection in Shipment Confirm() !!");
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception se){
				System.out.println(se.getMessage());
				throw new ITMException(se);
			}
		}
		return errString;
	}


	private String actionDefault(Document dom, Document dom1, Document dom2,String objContext, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null,pstmt1 = null;
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		ResultSet rs = null,rs1 = null;
		String sql = "",sql1 = "";
		String tranId = "";
		ArrayList<String> chargeCodeTempList = new ArrayList<String>();
		Node dtlNode = null, childNode = null;
		NodeList childNodeList = null;
		NodeList dtlNodeList = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		
		String freightList="",chargeCode="",chargesMode="",chargeCodeAdd="",sequence="";
		int lineNo=0;
		double amount=0;
		String gencodesDescr="";
		
		try
		{
			
			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			freightList = checkNull(genericUtility.getColumnValue("frt_list", dom1));
			tranId = checkNull(genericUtility.getColumnValue("shipment_id", dom1));

			System.out.println("freightList["+freightList+"]shipment_id["+tranId+"]");
			
			dtlNodeList = dom2.getElementsByTagName("Detail2");
			for (int dtlCnt = 0; dtlCnt < dtlNodeList.getLength(); dtlCnt++)
			{
				dtlNode = dtlNodeList.item(dtlCnt);
				childNodeList = dtlNode.getChildNodes();
				for (int chldCnt = 0; chldCnt < childNodeList.getLength(); chldCnt++)
				{
					childNode = childNodeList.item(chldCnt);
					if ("charge_code".equalsIgnoreCase(childNode.getNodeName()))
					{
						String chargeCodeTemp=genericUtility.getColumnValueFromNode("charge_code", dtlNode);
						
						chargeCodeTempList.add(chargeCodeTemp+""+freightList);
					}
				}
			}
			
			System.out.println("@@@@@@chargeCodeTempList["+chargeCodeTempList+"]");
			
			sql = " select charge_code,charges_mode,amount,charge_code__add,sequence" +
				  " from  freight_rate_det where frt_list = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, freightList);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				chargeCode = rs.getString("charge_code")==null?"":rs.getString("charge_code");
				chargesMode = rs.getString("charges_mode")==null?"":rs.getString("charges_mode");
				amount = rs.getDouble("amount");
				chargeCodeAdd = rs.getString("charge_code__add")==null?"":rs.getString("charge_code__add");
				sequence = rs.getString("sequence")==null?"":rs.getString("sequence");
				
				sql1 = " select descr from gencodes " +
					   " where  fld_name  = ?  and   mod_name  = 'X'  " +
						" and   fld_value  = ?  ";

				pstmt1 =  conn.prepareStatement(sql1);
				pstmt1.setString(1,"CHARGE_CODE" );
				pstmt1.setString(2,chargeCode.trim() );
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					gencodesDescr = rs1.getString("descr")==null?"":rs1.getString("descr");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close(); 
				pstmt1 = null;
				
				if ( !chargeCodeTempList.contains(chargeCode+""+freightList) )
				{
					lineNo++;
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<shipment_id>").append("<![CDATA[").append(tranId).append("]]>").append("</shipment_id>");
					valueXmlString.append("<line_no>").append("<![CDATA[").append(lineNo).append("]]>").append("</line_no>");
					valueXmlString.append("<charges_mode>").append("<![CDATA[").append(chargesMode).append("]]>").append("</charges_mode>");
					valueXmlString.append("<amount>").append("<![CDATA[").append(amount).append("]]>").append("</amount>");
					valueXmlString.append("<charge_code__add>").append("<![CDATA[").append(chargeCodeAdd).append("]]>").append("</charge_code__add>");
					valueXmlString.append("<sequence>").append("<![CDATA[").append(sequence).append("]]>").append("</sequence>");
					valueXmlString.append("<charge_code>").append("<![CDATA[").append(chargeCode).append("]]>").append("</charge_code>");
					valueXmlString.append("<gencodes_descr>").append("<![CDATA["+gencodesDescr+"]]>").append("</gencodes_descr>");
					valueXmlString.append("</Detail>\r\n");
				}
			}
			rs.close();
			rs = null;
			pstmt.close(); 
			pstmt = null;
			
			System.out.println("valueXmlString =" + valueXmlString.toString());
			valueXmlString.append("</Root>\r\n");

		} catch (SQLException sqx)
		{
			System.out.println("The Exception caught from actionDefault(Default) :" + sqx);
			throw new ITMException(sqx);
		} catch (Exception e)
		{
			System.out.println("The Exception caught from actionDefault (Default) :" + e);
			throw new ITMException(e);
		} finally
		{
			try
			{
				conn.close();
				conn = null;
			} catch (Exception e)
			{
			}
		}

		return valueXmlString.toString();

	}
	
	
	
	
	
private String checkNull( String input )	
{
	if ( input == null )
	{
		input = "";
	}
	return input;
}

}
