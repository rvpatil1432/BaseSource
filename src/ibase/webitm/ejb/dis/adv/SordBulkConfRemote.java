package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface SordBulkConfRemote extends ActionHandlerRemote
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
}
