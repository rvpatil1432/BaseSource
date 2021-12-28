package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;

import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SchemeApplAct extends ActionHandlerEJB implements SchemeApplActLocal, SchemeApplActRemote
{

	//LogWriter log = new LogWriter();
	//ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();

	/*public void ejbCreate() throws RemoteException, CreateException
	{
		//log.setNameOfLog("Action");
		//log.setLogOn(true);
		//log.write("================ejbCreate() method called=====================");
		System.out.println("================ejbCreate() method called=====================");
	}

   	public void ejbRemove()
	{
		//log.write("================ejbRemove() method called=====================");
		System.out.println("================ejbRemove() method called=====================");
	}

   	public void ejbActivate()
	{
		//log.write("================ejbActivate() method called=====================");
		System.out.println("================ejbActivate() method called=====================");
	}

   	public void ejbPassivate()
	{
		//log.write("================ejbPassivate() method called=====================");
		System.out.println("================ejbPassivate() method called=====================");
	}*/

    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String xmlString1,String objContext, String xtraParams) throws RemoteException,ITMException
	{
	    System.out.println("Action called..............");
		//log.write("..................Action called..............");
		Document dom = null;
		Document dom1 = null;

		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println( "XML String :" + xmlString );
				dom = genericUtility.parseString( xmlString );
			}
			if( xmlString1 != null && xmlString1.trim().length() !=0 )
			{
				System.out.println( "XML String1 :" + xmlString1 );
				dom1 = genericUtility.parseString( xmlString1 );
			}
			System.out.println( "actionType:" + actionType + ":" );
			resString = actionDefault( dom, dom1,objContext, xtraParams );
		}catch(Exception e)
		{
			System.out.println("Exception :Action :actionHandler(String xmlString):" + e.getMessage() + ":");
			//log.write("Exception :Action :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			//log.writeException(e);
			throw new ITMException(e);
		}
		System.out.println("returning from actionMETHOD :actionHandler"+resString);
		//log.write("returning from actionMETHOD :actionHandler"+resString);
	    return resString;
	}

	private String actionDefault( Document dom, Document dom1, String objContext, String xtraParams ) throws RemoteException , ITMException
	{
		String schemeCode = null;
		String returnString = "";
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String siteCode = null;
		String siteDescr = null;
		String stateCode = null;
		String stateDescr = null;
		int cnt = 0;
		String sql = null;
		StringBuffer valueXmlString = null;
		NodeList nlist = null;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();

		System.out.println("\t =====================================================");
		System.out.println("\t actionDefault Starts .........");
		System.out.println("\t =====================================================");

		//log.write("===================actionDefault() method called=============================");
		ConnDriver connDriver = new ConnDriver();
		try{
			//nlist = dom1.getElementsByTagName( "Detail2" );
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			//System.out.println("[Action] detail2 length==================>"+nlist.getLength());
			//log.write("[Action] dom2 length==================>"+nlist.getLength());
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
			//if(nlist.getLength() > 1)
			//{
			//	valueXmlString.append("</Root>\r\n");
			//	return valueXmlString.toString();
			//}
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			schemeCode = genericUtility.getColumnValue( "scheme_code", dom1 );
			sql = "Select s.site_code site_code, s.descr descr, s.state_code state_code, st.descr state_descr from site s left outer join state st on s.state_code = st.state_code  order by s.site_code";
			System.out.println("Site sql :::-" + sql );
			rs = stmt.executeQuery(sql);
			while( rs.next() )
			{
				siteCode = rs.getString("site_code");
				siteDescr = rs.getString("descr");
				stateCode = rs.getString("state_code");
				stateDescr = rs.getString("state_descr");

				siteCode = ( siteCode == null || siteCode.trim().length() == 0 ? "":siteCode.trim());
				siteDescr = ( siteDescr == null || siteDescr.trim().length() == 0 ? "":siteDescr.trim());
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<site_code>").append("<![CDATA["+ siteCode + "]]>").append("</site_code>");
				valueXmlString.append("<site_descr>").append("<![CDATA["+ siteDescr + "]]>").append("</site_descr>");
				//valueXmlString.append("<state_code>").append("<![CDATA["+ stateCode + "]]>").append("</state_code>");
				//valueXmlString.append("<state_descr>").append("<![CDATA["+ stateDescr + "]]>").append("</state_descr>");
				valueXmlString.append("</Detail>\r\n");
			}
			rs.close();
			rs = null;
			valueXmlString.append("</Root>\r\n");
			//log.write("[Action] final valueXmlString=============>"+valueXmlString+"<====");
		}catch( Exception e ){
			e.printStackTrace();
		}
		return valueXmlString.toString();
	}

	/* public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString);
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
				}
			}
			System.out.println("actionType:"+actionType+":");
			retString = actionTransform(dom, dom1, objContext, xtraParams, selDataDom);
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :AdjIssAct :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from AdjIssAct : actionHandlerTransform"+retString);
	    return retString;
	}

	private String actionTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String schemeCode = null;
		String returnString = "";
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String siteCode = null;
		String siteDescr = null;
		String stateCode = null;
		String stateDescr = null;
		int cnt = 0;
		NodeList detailList = null;
		int detailListLength = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccess itmDbAccess = new ITMDBAccess();
		String userId = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			userId = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			siteCode = new  ibase.utility.E12GenericUtility().getColumnValue("siteCode",dom);
			siteDescr = new  ibase.utility.E12GenericUtility().getColumnValue("descr",dom);

			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();


			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				siteCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("siteCode",detailList.item(ctr));
				siteDescr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("descr",detailList.item(ctr));
				stateCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("stateCode",detailList.item(ctr));
				stateDescr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("stateDescr",detailList.item(ctr));
				System.out.println( "SiteCode : " + siteCode + "  Descr : " + siteDescr );
				valueXmlString.append("<Detail>");
				valueXmlString.append("<site_code isSrvCallOnChg='1'>").append(siteCode).append("</site_code>");
				valueXmlString.append("<site_descr>").append(siteDescr).append("</site_descr>");
				valueXmlString.append("<state_code>").append(stateCode).append("</state_code>");
				valueXmlString.append("<state_descr>").append(stateDescr).append("</state_descr>");
				valueXmlString.append("</Detail>");

			}
			valueXmlString.append("</Root>\r\n");
		}
		catch (Exception e)
		{
			System.out.println("Exception SchemeApplAct "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception se){}
		}
		return valueXmlString.toString();
	} */


}