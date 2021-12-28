package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.wms.ShipmentConf;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.Document;

@javax.ejb.Stateless
public class LoadCloseConf extends ActionHandlerEJB implements LoadCloseConfLocal,LoadCloseConfRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	@Override
	public String confirm(String xmlString,String tranId,String xtraParams) throws RemoteException,ITMException
	{

		System.out.println(">>>>>>>>>>>>>>xmlStringtranId"+tranId);
	
		System.out.println("------------ LoadCloseConf confirm method called-----------------tranId : "+ tranId);		
		Document dom = null;
		String errString="";

		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
				System.out.println("xmlString d" + xmlString);
			}
			                                                      
			tranId = genericUtility.getColumnValue("shipment_id1",dom);

			System.out.println("------------ LoadCloseConf confirm method called-----------------tranId from dom: "+ tranId);
			
	        ShipmentConf conf = new ShipmentConf();
			
			System.out.println("before calling confirm > >");
			// 10/10/14 manoharan to bypass some validation in case sO/DO loading option, isLoading=Y added
			xtraParams = xtraParams + "~~isLoading=Y";
			
			errString = conf.confirm(tranId,xtraParams,"F");
			
			System.out.println("errString value > >"+errString);
			
}
		catch(Exception e)
		{
			System.out.println("Exception : LoadCloseConf : confirm : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}

	public String confirm(String xmlString, String editFlag, String tranId,
			String xtraParams, Connection conn) throws RemoteException,
			ITMException {
		// TODO Auto-generated method stub
		return null;
	}

}
