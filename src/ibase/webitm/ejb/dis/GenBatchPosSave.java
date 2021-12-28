/********************************************************
	Title 	 : 	GenBatchPosSave[D16ESUN003]
	Date  	 : 	23/AUG/16
	Developer:  Poonam Gole.

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.utility.ITMException;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.sys.CreateRCPXML;
import java.rmi.RemoteException;
import java.sql.*;

import org.w3c.dom.*;

@javax.ejb.Stateless
public class GenBatchPosSave extends ValidatorEJB implements GenBatchPosSaveLocal, GenBatchPosSaveRemote
{
	@Override
	public String postSave() throws RemoteException, ITMException
	{
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String postSave(String xmlStringAll, String tranId, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("PostSave Action Called:::");
		E12GenericUtility genericUtility = new E12GenericUtility();
		ITMDBAccessEJB dbEjb = new ITMDBAccessEJB() ;
		Document dom = null;
		PreparedStatement pstmt = null;		
		ResultSet rs = null ;
		boolean isError = false ;
		String sql = "", ediOption = "" , winName = "" ,dataStr = "" ,retString = "",tranIdCol = "";
		
		try
		{
			
			dom = genericUtility.parseString(xmlStringAll);
			/*System.out.println("dom>>"+dom);
			System.out.println("xmlStringAll>>"+xmlStringAll);
			System.out.println(" Tran id = "+tranId);
			System.out.println("xtraParams:::" +xtraParams);*/
			
			NodeList parentNodeList1 = dom.getElementsByTagName("Detail1");
			Node parentNode1 = parentNodeList1.item(0);
			
			winName = getWinName(parentNode1);
			
			System.out.println("winName in posSave"+ winName);
			
			sql = "select edi_option,tran_id_col from transetup where tran_window= ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, winName);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				ediOption = checkNull(rs.getString("edi_option"));
				tranIdCol = checkNull(rs.getString("tran_id_col")).toLowerCase();
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
				
			if(tranIdCol != null)
			{
				tranIdCol = tranIdCol.trim();
			}
			
			System.out.println("ediOption["+ediOption+"]tranIdCol["+tranIdCol + "]");
			tranId = checkNull(genericUtility.getColumnValue(tranIdCol, dom));
			
			System.out.println("tranId >>>>"+tranId);
					
			System.out.println("ediOption"+ediOption);
			ediOption = ediOption != null ? ediOption:"0";
			int ediOpt = Integer.parseInt(ediOption);
			System.out.println("ediOpt"+ediOpt  +"ediOption"+ediOption);
			
			if(ediOpt > 0)
			{
				CreateRCPXML createRCPXML = new CreateRCPXML(winName,tranIdCol);
				dataStr = createRCPXML.getTranXML(tranId, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);
				System.out.println("ediDataDom =[ " + ediDataDom + "]");
				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, winName,ediOption, xtraParams, conn);
				System.out.println("e12CreateBatchLoad from batchload = [" + retString + "]");
				createRCPXML = null;
				e12CreateBatchLoad = null;
				if (retString != null && retString.contains("SUCCESS"))
				{
					System.out.println("retString from batchload = [" + retString + "]");
					isError = false;
				}
				else
				{
					isError = true;
				}
				System.out.println("isError"+isError);
			}
		}
		catch(Exception e)
		{
			System.out.println("iNSIDE CATCH......");
			e.printStackTrace();
			retString = dbEjb.getErrorString("", "POSFAIL", "");
			throw new ITMException( e );
			
		}
		finally
		{
			try
			{		
				if(isError)
				{
					retString = dbEjb.getErrorString("", "POSFAIL", "");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	private String getWinName(Node node) throws Exception
    {
        String objName = "";
        NamedNodeMap attrMap = node.getAttributes();
        objName = attrMap.getNamedItem("objName").getNodeValue();
        System.out.println(" Object Name is==>" + objName);
        return "w_" + objName;
    }
	
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
}
