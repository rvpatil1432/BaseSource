package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote;

@Remote
public interface PriceListTranConfRemote extends ActionHandlerRemote{

	@Override
	public String confirm(String arg0, String arg1, String arg2)
			throws RemoteException, ITMException;
}
