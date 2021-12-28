package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote;

@Remote
public interface ProofOfDeliveryRejectRemote extends ActionHandlerRemote
{
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
