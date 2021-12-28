package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import javax.ejb.Local;
@Local
public interface ProofOfDeliveryConfLocal extends ActionHandlerLocal
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
