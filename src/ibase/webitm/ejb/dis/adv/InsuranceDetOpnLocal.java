package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface InsuranceDetOpnLocal extends ActionHandlerLocal//,EJBObject
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String tranIDIns, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}