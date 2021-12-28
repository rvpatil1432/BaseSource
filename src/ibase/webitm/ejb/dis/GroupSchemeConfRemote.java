package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;


import javax.ejb.Remote;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

@Remote
public interface GroupSchemeConfRemote extends ActionHandlerRemote
{
	
	public String confirm(String schemeCode, String xtraParams, String forcedFlag) throws RemoteException, ITMException;

}
