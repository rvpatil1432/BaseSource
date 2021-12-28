package ibase.webitm.ejb.dis;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import org.w3c.dom.Document;

@javax.ejb.Local
public interface SalesReturnVerifyPasswordICLocal extends ValidatorLocal
{
	public String itemChanged() throws RemoteException, ITMException;
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	
	public String itemChanged( Document currFormDataDom, Document hdrDataDom, Document allFormDataDom, String objContext, String currentColumn,String editFlag, String xtraParams ) throws RemoteException, ITMException;
}


