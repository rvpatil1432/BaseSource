package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local;

@Local
public interface SorderGroupSchemeActLocal extends ActionHandlerLocal
{
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
