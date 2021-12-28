package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public interface IndentReqConfWfEJBLocal extends ActionHandlerLocal
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	public String rejection(String tranID, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
}
