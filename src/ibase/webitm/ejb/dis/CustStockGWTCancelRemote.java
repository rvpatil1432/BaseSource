package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

@Remote
public interface CustStockGWTCancelRemote  extends ActionHandlerRemote{
	public String cancel(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
