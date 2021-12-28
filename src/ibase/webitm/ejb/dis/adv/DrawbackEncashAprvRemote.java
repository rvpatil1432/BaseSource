package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface DrawbackEncashAprvRemote extends ActionHandlerRemote//,EJBObject
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String tranIDIns, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}