package ibase.webitm.ejb.dis.adv;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public interface SReturnFormActLocal extends ActionHandlerLocal
{
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
