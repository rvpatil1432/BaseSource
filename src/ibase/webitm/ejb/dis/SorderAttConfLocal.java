package ibase.webitm.ejb.dis; 

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

//public interface SorderAttConf extends ActionHandlerLocal,EJBObject
@Local // added for ejb3
public interface SorderAttConfLocal extends ActionHandlerLocal
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException;
}