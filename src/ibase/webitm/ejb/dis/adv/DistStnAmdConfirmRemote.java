package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

public interface DistStnAmdConfirmRemote extends ActionHandlerRemote{

	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
}
