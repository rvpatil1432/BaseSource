package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.ejb.ActionHandler;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.ejb.ActionHandlerLocal;// added for ejb3

@Local // added for ejb3
public interface CustDiscApprvICConfirmLocal extends ActionHandlerLocal
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException;
}