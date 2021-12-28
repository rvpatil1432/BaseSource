package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;

import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3

public interface DefaultSorderRemote extends ActionHandlerRemote//,EJBHome
{
	//public ActionHandler create() throws RemoteException, CreateException;
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
}