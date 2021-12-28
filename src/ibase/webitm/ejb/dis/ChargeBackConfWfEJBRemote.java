package ibase.webitm.ejb.dis;



import java.rmi.RemoteException;

import javax.ejb.Remote;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

@Remote
public interface ChargeBackConfWfEJBRemote extends ActionHandlerRemote 
{
	public String confirm(String paramString1, String paramString2,String paramString3) throws RemoteException, ITMException;
	public String confirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
	public String rejection(String paramString1, String paramString2,String paramString3) throws RemoteException, ITMException;
	public String rejection(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
}
