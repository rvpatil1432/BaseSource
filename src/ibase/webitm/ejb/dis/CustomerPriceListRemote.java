package ibase.webitm.ejb.dis;

import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3

public interface CustomerPriceListRemote extends ValidatorRemote
{
	//public String wfValData() throws RemoteException,ITMException;
	
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(Document dom, Document dom1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
		
}
