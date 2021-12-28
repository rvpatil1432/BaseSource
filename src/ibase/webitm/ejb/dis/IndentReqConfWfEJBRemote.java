package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;

@Remote
public interface IndentReqConfWfEJBRemote extends ActionHandlerRemote
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	public String rejection(String tranID, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
}
