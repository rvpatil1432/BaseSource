/********************************************************
 Title : SOItemSpecPrs
 Date  : 14/01/21
 Developer: Rohini Telang

 ********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
@javax.ejb.Stateless
public class SOItemSpecPrs extends ValidatorEJB implements SOItemSpecPrsLocal, SOItemSpecPrsRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String preSave() throws RemoteException,ITMException
	{
		return "";
	}
	public String preSave(String xmlString, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("SOItemSpecPrs EJB called");
		Document dom = null;
		try
		{
			System.out.println("xmlString in SOItemSpecPrs : preSaveRec \n"+xmlString);			
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);
				errString = executepreSave( dom, editFlag, xtraParams, conn );
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SOItemSpecPrs :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	public String executepreSave( Document dom, String editFlag, String xtraParams, Connection conn ) throws RemoteException, ITMException, SQLException
	{

		System.out.println("executepreSave method called \t editFlag is ["+editFlag+"]");
		String errString = "";
		NodeList parentNodeList = null;
		Node parentNode = null;
		int parentNodeListLength=0 ,cnt =0;
		double allocPercVal = 0.0;
		boolean isLocCon = false;
		boolean isError = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String sql="";
		double quantity = 0;
		double totalQty = 0;
		String qty = "";
		String quants = "";
		try
		{
			if (conn == null ||  conn.isClosed())
			{
				conn = getConnection();
				System.out.println("connection is not null :"+conn);
				isLocCon = true;
			}
			System.out.println("connection is :"+conn);
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNodeListLength = parentNodeList.getLength();
			//quantity = Double.parseDouble( genericUtility.getColumnValue("quantity", dom)== null ?"0":genericUtility.getColumnValue("quantity", dom));
			quants = genericUtility.getColumnValueFromNode("quantity",parentNodeList.item(0));
			//quantity = Double.parseDouble( genericUtility.getColumnValue("quantity", parentNodeList.item(0))== null ?"0":genericUtility.getColumnValue("quantity", parentNodeList.item(0)));
			quantity = Double.parseDouble(quants);
			System.out.println("quantity is.... :"+quantity);
			
			
			parentNodeList = dom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			for(int row = 0; row < parentNodeListLength; row++)
			{
				qty = checkNull(genericUtility.getColumnValueFromNode("quantity", dom.getElementsByTagName("Detail2").item(row)));
				System.out.println("qty :"+qty);
				if(qty != null && qty.trim().length() >0)
				{
					totalQty = totalQty + Double.valueOf(qty);
				}
			}
			System.out.println("totalQty is :"+totalQty);
			if(quantity != totalQty)
			{
				System.out.println("quantity MIsmatch :"+totalQty);
				errString = itmDBAccessEJB.getErrorString("","VSOQANMS","","",conn);
				isError = true;
			}
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in SOItemSpecPrs :"+e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Inside finally SOItemSpecPrs isError["+isError+"] connStatus["+isLocCon+"]");


				if(isLocCon)
				{
					if(isError)
					{
						System.out.println("Inside rollbacking....");
						conn.rollback();
					}
					else
					{
						System.out.println("Inside committing....");
						conn.commit();
					}
					if (conn != null )
					{
						conn.close();
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return errString;
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
