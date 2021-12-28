package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local
public interface PoamdDlvDefaultLocal extends ActionHandlerLocal 
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
}
