package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import javax.ejb.Local;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

@Local
public interface CustStockGWTCancelLocal extends ActionHandlerLocal {

	public String cancel(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
