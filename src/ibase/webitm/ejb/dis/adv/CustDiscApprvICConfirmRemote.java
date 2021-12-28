package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.ejb.ActionHandler;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.ejb.ActionHandlerRemote;// added for ejb3

@Remote // added for ejb3
public interface CustDiscApprvICConfirmRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException;
}