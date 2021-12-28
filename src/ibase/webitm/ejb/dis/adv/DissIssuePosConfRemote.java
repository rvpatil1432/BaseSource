
//new component for post confirm
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

public interface DissIssuePosConfRemote extends ValidatorRemote
{
	public String postConfirm() throws RemoteException,ITMException;

    public String postConfirm(String xmlStringAll, String tranId, String xtraParams) throws RemoteException,ITMException;
 
}
