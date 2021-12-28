package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote;
import java.rmi.RemoteException;


@Remote
public interface ProofOfDeliveryConfRemote extends ActionHandlerRemote
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
