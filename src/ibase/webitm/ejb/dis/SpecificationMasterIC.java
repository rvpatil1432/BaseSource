package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless

public class SpecificationMasterIC extends ValidatorEJB implements SpecificationMasterICRemote, SpecificationMasterICLocal {

E12GenericUtility genericUtility = new E12GenericUtility();
ITMDBAccessEJB itmdbAccessEJB = new ITMDBAccessEJB();
	
@Override
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams)
			throws RemoteException, ITMException {

	String rtStr = "";
	Document dom = null;
	Document dom1 = null;
	Document dom2 = null;
	
	try {
		System.out.println("::: in wfValData String");
		System.out.println("::::xmlString" + xmlString);
		System.out.println(":::: xmlString1" + xmlString1);
		System.out.println(":::: xmlString2" + xmlString2);
		
		if(xmlString != null && xmlString.length() > 0){
			dom = genericUtility.parseString(xmlString);
		}
		
		if(xmlString1 != null && xmlString1.length() > 0){
			dom1 = genericUtility.parseString(xmlString1);
		}
		
		if(xmlString2 != null && xmlString2.length() > 0){
			dom = genericUtility.parseString(xmlString2);
		}
		
		rtStr = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
	} catch (Exception e) {
		System.out.println(":::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
		e.printStackTrace();
		throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
	}
	return rtStr;
	}

@Override
	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

	String errString = "";
	Connection conn = null;
	String specRef = "", specCode = "";
	NodeList parentNodeList = null, childNodeList = null;
	Node parentNode = null, childNode = null;
	String childNodeName = "",userId = "";
	int currentFormNo = 0, childNodeLength = 0, ctr = 0;
	// Changed By Nasruddin Start 21-SEp-16
	String sql= "";
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	int count = 0 ;
	// Changed By Nasruddin  21-SEp-16 END
	try {
		System.out.println("::in wfvalData doc::");
		ConnDriver con = new ConnDriver();
		//Changes and Commented By Bhushan on 13-06-2016 :START
		//conn = con.getConnectDB("DriverITM");
		conn = getConnection();
		//Changes and Commented By Bhushan on 13-06-2016 :END
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
		if(objContext != null && objContext.trim().length() > 0){
			currentFormNo = Integer.parseInt(objContext);
		}
		switch(currentFormNo)
		{
		case 1:
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeLength = childNodeList.getLength();
			for(ctr = 0;ctr<childNodeLength;ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("spec_ref"))
				{
					specRef = genericUtility.getColumnValue("spec_ref", dom);
					specRef = specRef == null ? "" : specRef.trim();
					System.out.println(":::specification ref" + specRef);
					if(specRef.isEmpty())
					{
						errString = itmdbAccessEJB.getErrorString("spec_ref", "VTSPECREF", userId);
						return errString;
					}
				}
				if(childNodeName.equalsIgnoreCase("spec_code"))
				{
					specCode = genericUtility.getColumnValue("spec_code", dom);
					specCode = specCode == null ? "" : specCode.trim();
					System.out.println(":::specification code" + specCode);
					if(specCode.isEmpty())
					{
						errString = itmdbAccessEJB.getErrorString("spec_code", "VMCODNULL", userId);
						return errString;
					}
					// Changed By Nasruddin 21-SEP-16 Start
					else
					{
						if("A".equals(editFlag))
						{
							sql = "SELECT COUNT(1) FROM  SPECIFICATION WHERE SPEC_CODE = ?";
						    pstmt = conn.prepareStatement(sql);
						    pstmt.setString(1, specCode);
						    rs = pstmt.executeQuery();
							if( rs.next() )
							{
								count = rs.getInt(1);
							}
						   pstmt.close();
						   pstmt =  null;
						   rs.close();
						   rs = null;
						}
						if( count > 0)
						{
						errString = itmdbAccessEJB.getErrorString("spec_code", "VMPMKY", userId);
						return errString;
						}
					}
					// Changed By Nasruddin 21-SEP-16 END
				}
			}
		}
		/* Comment By Nasruddin 21-SEP-16 START
		if(conn != null) 
		{
			conn.close();
			conn = null;
		}
	   Comment By Nasruddin 21-SEP-16 END */

	} 
	catch (Exception e)
	{
		System.out.println("::::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
		e.printStackTrace();
		throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
	}
	//Changed By Nasruddin 21-SEP-16 STart
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
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
	
	}
	//Changed By Nasruddin 21-SEP-16 END
	return errString;
	}
}
