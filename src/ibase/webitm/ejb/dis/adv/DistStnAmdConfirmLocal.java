package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

public interface DistStnAmdConfirmLocal extends ActionHandlerLocal {

	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
}
