package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
//import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.ejb.ActionHandlerLocal;

@Local // added for ejb3
public interface CustStockActLocal extends ActionHandlerLocal//, EJBObject
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
}