package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;

import javax.ejb.Local;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

@Local
public interface SorderConfWfEJBLocal extends ActionHandlerLocal 
{
	public String confirm(String paramString1, String paramString2,String paramString3) throws RemoteException, ITMException;
	public String confirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
	public String rejection(String paramString1, String paramString2,String paramString3) throws RemoteException, ITMException;
	public String rejection(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
}
