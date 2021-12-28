package ibase.webitm.ejb.dis; 

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;

//public interface SorderAttConf extends ActionHandlerRemote,EJBObject
import javax.ejb.Remote; // added for ejb3
public interface SorderAttConfRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException;
}